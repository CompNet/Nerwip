package fr.univavignon.nerwip.processing.internal.modelless.subee;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
 * 
 * This file is part of Nerwip - Named Entity Extraction in Wikipedia Pages.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is distributed in the hope 
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Nerwip - Named Entity Extraction in Wikipedia Pages.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateRecognizer;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.freebase.FbCommonTools;
import fr.univavignon.nerwip.tools.freebase.FbTypeTools;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * This class implements our own recognizer, called Subee. It takes advantage of
 * hyperlinks present in  Wikipedia pages to identify mentions in the text, and 
 * of Freebase to select their type.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code additionalOccurrences}: {@code true}</li>
 * 		<li>{@code useTitle}: {@code true}</li>
 * 		<li>{@code notableType}: {@code true}</li>
 * 		<li>{@code useAcronyms}: {@code true}</li>
 * 		<li>{@code discardDemonyms}: {@code true}</li>
 * </ul>
 * <br/>
 * <b>Note:</b> if you use this tool, make sure you set up your Freebase key
 * in class {@link FbCommonTools}.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
class SubeeDelegateRecognizer extends AbstractModellessInternalDelegateRecognizer<List<AbstractMention<?>>>
{
	/**
	 * Builds and sets up an object representing
	 * Subee, our recognizer taking advantage of text
	 * containing hyperlinks.
	 * 
	 * @param subee
	 * 		Recognizer in charge of this delegate.
	 * @param additionalOccurrences
	 * 		Whether or not the tool should annotate the additional occurrences
	 * 		of some mention.
	 * @param useTitle
	 * 		Whether or not the tool should use the article title to infer
	 * 		the person name.
	 * @param notableType
	 * 		Whether the tool should use the single notable type provided by Freebase,
	 * 		or all available Freebase types.
	 * @param useAcronyms
	 * 		On their first occurrence, certain mentions are followed by the associated
	 * 		acronym: this option allows searching them in the rest of the text.
	 * @param discardDemonyms
	 * 		Ignore mentions whose string value corresponds to a demonym, i.e. the adjective
	 * 		associated to a place, or the name of its inhabitants. Subee generally takes them
	 * 		for the place itself, leading to an increased number of false positives.
	 */
	public SubeeDelegateRecognizer(Subee subee, boolean additionalOccurrences, boolean useTitle, boolean notableType, boolean useAcronyms, boolean discardDemonyms)
	{	super(subee,false,false,true,false);
		
		this.additionalOccurrences = additionalOccurrences;
		this.useTitle = useTitle;
		this.notableType = notableType;
		this.useAcronyms = useAcronyms;
		this.discardDemonyms = discardDemonyms;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
		result = result + "_" + "addOcc=" + additionalOccurrences;
		result = result + "_" + "useTtl=" + useTitle;
		result = result + "_" + "ntblType=" + notableType;
		result = result + "_" + "useAcro=" + useAcronyms;
		result = result + "_" + "discDemo=" + discardDemonyms;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types detected by this recognizer */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList(
		EntityType.LOCATION,
		EntityType.ORGANIZATION,
		EntityType.PERSON
	);
	
	@Override
	public List<EntityType> getHandledEntityTypes()
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this recognizer can treat */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList(
		ArticleLanguage.EN
		//TODO could be extended to french provided the ressources are translated (demonyms, WP-related stuff...)
	);

	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected List<AbstractMention<?>> detectMentions(Article article) throws ProcessorException
	{	logger.increaseOffset();
		List<AbstractMention<?>> result = new ArrayList<AbstractMention<?>>();
		
		try
		{	// detect and process hyperlinks
			logger.log("Detect and process hyperlinks");
			List<AbstractMention<?>> sureMentions = processHyperlinks(article);
			
			// look for additional occurrences of these mentions
			List<AbstractMention<?>> possibleMentions = new ArrayList<AbstractMention<?>>();
			if(additionalOccurrences)
			{	logger.log("Look for additional occurrences");
				possibleMentions = processOccurrences(article,sureMentions);
			}
			else
				logger.log("Ignore additional occurrences");
			
			// process the name of the person described in the processed article
			if(useTitle)
			{	logger.log("Process the name of this article main person");
				List<AbstractMention<?>> temp = processMainName(article);
				possibleMentions.addAll(temp);
			}
			else
				logger.log("Ignore article title");
			
			// build result list by merging both lists (sure and possible mentions)
			result = mergeMentionLists(sureMentions,possibleMentions);
		}
		catch (ParserException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		catch (ClientProtocolException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		catch (ParseException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		catch (org.json.simple.parser.ParseException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// TITLE		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not the title should be used to infer the person's name */
	private boolean useTitle;
	
	/**
	 * Handles the name of the person described in the processed article. For this purpose,
	 * we consider the article title and name, as well as the first sentence, which generally
	 * starts with the full name of the person.
	 * 
	 * @param article 
	 * 		Article to process.
	 * @return
	 * 		List of possible mentions based on the analysis of the article title and name.
	 * 
	 * @throws ClientProtocolException
	 * 		Problem while accessing Freebase.
	 * @throws ParseException
	 * 		Problem while accessing Freebase.
	 * @throws IOException
	 * 		Problem while accessing Freebase.
	 * @throws org.json.simple.parser.ParseException
	 * 		Problem while accessing Freebase.
	 */
	private List<AbstractMention<?>> processMainName(Article article) throws ClientProtocolException, ParseException, IOException, org.json.simple.parser.ParseException
	{	logger.increaseOffset();
		List<AbstractMention<?>> result = new ArrayList<AbstractMention<?>>();
		String rawText = article.getRawText();
		ArticleLanguage language = article.getLanguage();

		// init candidate strings with article name and title 
		Set<String> candidateStrings = new TreeSet<String>();
		String articleTitle = article.getTitle();
//debug
//if(articleTitle.equals("Alfred Lothar Wegener"))
//	System.out.print("");
		logger.log("Article title: "+articleTitle);
		candidateStrings.add(articleTitle);
		String articleName = article.getName();
		logger.log("Article name: "+articleName);
		articleName =  articleName.replace('_', ' ').trim();
		candidateStrings.add(articleName);
		
		// process the beginning of the first sentence
		// we look for the string before the first parenthesis (usually containing birth info)
		// if there's none, we just ignore this potential information source
		Pattern p = Pattern.compile("^[^\\.]+?\\(");
		Matcher m = p.matcher(rawText);
		if(m.find())
		{	int startPos = m.start();
			if(startPos==0)
			{	int endPos = m.end();
				String persName = rawText.substring(0,endPos-1);
				persName = persName.trim();
				int wordCount = persName.length() - persName.replaceAll(" ", "").length();
				if(wordCount>6)
					logger.log("Not able to extract person name from first sentence (too many words before the parenthesis): \""+rawText.substring(0,75)+"\"");
				else
				{	logger.log("Person name: "+persName);
					candidateStrings.add(persName);
				}
			}
		}
		else
			logger.log("Not able to extract person name from first sentence (can't find the parenthesis): \""+rawText.substring(0,75)+"\"");
		
		// possibly remove double quotes (especially for the nicknames)
		List<String> nickFull = new ArrayList<String>();
		Set<String> copy = new TreeSet<String>(candidateStrings);
		candidateStrings.clear();
		for(String candidateString: copy)
		{	if(candidateString.contains("\""))
			{	nickFull.add(candidateString);
				candidateString = candidateString.replaceAll("\"","");
			}
			candidateStrings.add(candidateString);
		}
		
		// possibly remove an indication in parenthesis at the end (especially for the titles)
		copy = new TreeSet<String>(candidateStrings);
		candidateStrings.clear();
		for(String candidateString: copy)
		{	if(candidateString.endsWith(")"))
			{	String temp[] = candidateString.split("\\(");
				candidateString = temp[0].trim();
			}
			candidateStrings.add(candidateString);
		}

		// add the lastname alone; only with the preceeding word; only with the 2 preeceding words, etc.
		copy = new TreeSet<String>(candidateStrings);
		for(String candidateString: copy)
		{	String split[] = candidateString.split(" ");
			for(int i=split.length-1;i>=0;i--)
			{	String temp = "";
				for(int j=i;j<split.length;j++)
					temp = temp + split[j] + " ";
				temp = temp.trim();
				candidateStrings.add(temp);
			}
		}
		
		// add very first and very last names (for more than 2 words)
		copy = new TreeSet<String>(candidateStrings);
		for(String candidateString: copy)
		{	String split[] = candidateString.split(" ");
			if(split.length>2)
			{	String temp = split[0] + " " + split[split.length-1];
				candidateStrings.add(temp);
			}
		}
		
		// add variants with initials instead of firstnames
		copy = new TreeSet<String>(candidateStrings);
		for(String candidateString: copy)
		{	String split[] = candidateString.split(" ");
			if(split.length>1)
			{	String initials1 = "";
				String initials2 = "";
				for(int i=0;i<split.length-1;i++)
				{	initials1 = initials1 + split[i].substring(0,1).toUpperCase(Locale.ENGLISH) + ". ";
					initials2 = initials2 + split[i].substring(0,1).toUpperCase(Locale.ENGLISH) + ".";
				}
				initials1 = initials1 + split[split.length-1];
				initials2 = initials2 + " " + split[split.length-1];
				candidateStrings.add(initials1);
				candidateStrings.add(initials2);
			}
		}
		
		// add the original version of the nicknames
		candidateStrings.addAll(nickFull);
		
		// look for similar strings in the text
		for(String expr: candidateStrings)
		{	String escapedStr = Pattern.quote(expr);
			p = Pattern.compile("\\b"+escapedStr+"\\b");
			m = p.matcher(rawText);
			while(m.find())
			{	int startPos = m.start();
				int endPos = m.end();
				String valueStr = m.group();
				AbstractMention<?> ent = AbstractMention.build(EntityType.PERSON, startPos, endPos, ProcessorName.SUBEE, valueStr, language);
				result.add(ent);
			}
		}
	
		if(result.isEmpty())
			logger.log("WARNING: title not found at all in the text, which is unusual");
		
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// HYPERLINKS	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** HTML name of hyperlink elements */
	private static final String TAG_LINK = "a";
	/** HTML start tag, used for parsing the linked text */
	private static final String TAG_PAR_START = "<p>";
	/** HTML end tag, used for parsing the linked text */
	private static final String TAG_PAR_END = "</p>";
	/** Wheter acronyms should be searched for, or not */
	private boolean useAcronyms;

	/**
	 * Takes advantage of hyperlinks in the text, in order
	 * to detect mentions. Most of the time, in a Wikipedia
	 * article, the hyperlink is defined only for the very 
	 * first occurrence of the mention. For this reason,
	 * an additional processing is required to find the possible
	 * other occurrences (cf. {@link #processOccurrences(Article, List)}). 
	 * 
	 * @param article
	 * 		Processed article.
	 * @return
	 * 		The list of mentions detected by this method.
	 * 
	 * @throws ParserException
	 * 		Problem while parsing the hyperlinks.
	 * @throws ClientProtocolException
	 * 		Problem while accessing Freebase.
	 * @throws ParseException
	 * 		Problem while accessing Freebase.
	 * @throws IOException
	 * 		Problem while accessing Freebase.
	 * @throws org.json.simple.parser.ParseException
	 * 		Problem while accessing Freebase.
	 */
	private List<AbstractMention<?>> processHyperlinks(Article article) throws ParserException, ClientProtocolException, ParseException, IOException, org.json.simple.parser.ParseException
	{	logger.increaseOffset();
		ArticleLanguage language = article.getLanguage();
		List<AbstractMention<?>> result = new ArrayList<AbstractMention<?>>();
		
		// parse linked text to automatically get hyperlink list
		logger.log("Get hyperlink list");
		String linkedText = article.getLinkedText();
		Parser parser = new Parser(TAG_PAR_START+linkedText+TAG_PAR_END);
		NodeList linkList = parser.parse(new TagNameFilter(TAG_LINK));
		int offset = TAG_PAR_START.length();
		
		// process each hyperlink
		logger.log("Process each hyperlink");
		logger.increaseOffset();
		for(int i=0; i<linkList.size(); i++)
		{	LinkTag linkTag = (LinkTag)linkList.elementAt(i);
			String valueStr = linkTag.getLinkText();
			int length = valueStr.length();
			String test = linkTag.toHtml();
			logger.log("Hyperlink '"+test+"'");

			// get type from Freebase
			EntityType type = null;
			// only process strings with uppercase initial
			if(StringTools.hasInitial(valueStr))
			{	String hyperlink = linkTag.getLink();
				String[] linkParts = hyperlink.split("/");
				String lastPart = linkParts[linkParts.length-1];
				String wikipediaTitle = URLDecoder.decode(lastPart, "UTF-8"); //TODO we may take advantage of this to automatically detect the type
				String wikipediaTitleEscaped = FbCommonTools.escapeMqlKey(wikipediaTitle);	//TODO or this
				logger.log("Wikipedia title: "+wikipediaTitle);
				logger.log("Escaped Wikipedia title: "+wikipediaTitleEscaped);
				// use only the notable type
				if(notableType)
				{	String possibleType = FbTypeTools.getNotableType(wikipediaTitleEscaped);
					if(possibleType==null)
						logger.log("No notable Freebase type found for \""+valueStr+"\"");
					else
					{	List<String> possibleTypes = new ArrayList<String>();
						possibleTypes.add(possibleType);
						type = retrieveMentionType(possibleTypes);
					}
				}
				// use all available types
				if(type==null)
				{	List<String> possibleTypes = FbTypeTools.getAllTypes(wikipediaTitleEscaped);
					logger.log("Possible types: "+possibleTypes.toString());
					if(possibleTypes.isEmpty())
						logger.log("WARNING: no Freebase type found at all for \""+valueStr+"\"");
					else
						type = retrieveMentionType(possibleTypes);
				}
			}
			
			// set up the mention position
			int startPos = linkTag.getStartPosition() - offset;
			int endPos = startPos + length;
			offset = offset + test.length() - length;
//debug								
//String text = article.getRawText();
//String valueStr2 = text.substring(startPos,endPos);
//boolean test2 = valueStr.equals(valueStr2);
//if(!test2)
//	System.out.println("ERROR: mention and article do not match (position problem)");
				
			// no type: we can't create the mention
			if(type==null)
			{	logger.log("WARNING: no mention was created, because no type could be identified for \""+valueStr+"\"");
			}
			// otherwise, we try
			else
			{	// ignore if purely numerical
				if(StringTools.hasNoLetter(valueStr))
					logger.log("The string is only numerical (no letters) so no mention is created for "+valueStr);
				
				// ignore if recognized as a location/organization but actually a demonym
				else if(discardDemonyms && (type==EntityType.LOCATION || type==EntityType.ORGANIZATION) && DEMONYMS.contains(valueStr))
					logger.log("The string is in the demonym list, so no mention is created for "+valueStr);
				
				else
				{	
//debug
//if(valueStr.equalsIgnoreCase("Irish"))
//	System.out.print("");
					
					// possibly look for an acronym
					if(useAcronyms)
					{	// only organization and locations have relevant acronyms
						// (for a person, acronyms usually correspond to titles or awards)
						if(type==EntityType.ORGANIZATION || type==EntityType.LOCATION)
						{	// check if there's an acronym inside the mention name itself
							Pattern r = Pattern.compile("\\([^\\(a-z]+?\\)$");	// must be in uppercase
							Matcher m = r.matcher(valueStr);
							if(m.find())
							{	// create an additional mention (acronym) with the same type
								int last = m.groupCount();
								String acro = m.group(last);
								int l = acro.length();
								acro = acro.substring(1,l-1);
								int s = startPos + m.start(last) + 1;
								int e = startPos + m.end(last) - 1;
								if(!StringTools.hasNoLetter(acro))
								{	
//debug								
//String valueStr3 = text.substring(s,e);
//boolean test3 = acro.equals(valueStr3);
//if(!test3)
//	System.out.println("ERROR: mention acronym and article do not match (position problem)");
									AbstractMention<?> mention = AbstractMention.build(type, s, e, ProcessorName.SUBEE, acro, language);
									result.add(mention);
									logger.log("Creation of an extra mention (acronym) "+mention);
								}
								// remove the acronym from the original string
								valueStr = valueStr.substring(0,valueStr.length()-l).trim();
								endPos = startPos + valueStr.length();
							}
							// check if there's an acronym right after the mention 
							else
							{	r = Pattern.compile("\\([^\\(a-z]+?\\)");	// must be in uppercase
								m = r.matcher(linkedText);
								if(m.find(linkTag.getEndTag().getEndPosition()-TAG_PAR_START.length()))
								{	// possibly create an additional mention (acronym) with the same type
									int last = m.groupCount();
									String acro = m.group(last);
									acro = acro.substring(1,acro.length()-1);
									int s = m.start(last)-1 - (offset-TAG_PAR_END.length()) + 1;	// actually <a/> and not <p/>, but same length...
									// the acronym must be right after the original mention
									if(s==endPos+2 && !StringTools.hasNoLetter(acro))	
									{	int e = m.end(last)-1 - (offset-TAG_PAR_END.length()) - 1 ;
//debug
//String valueStr3 = text.substring(s,e);
//boolean test3 = acro.equals(valueStr3);
//if(!test3)
//	System.out.println("ERROR: mention acronym and article do not match (position problem)");
										AbstractMention<?> mention = AbstractMention.build(type, s, e, ProcessorName.SUBEE, acro, language);
										result.add(mention);
										logger.log("Creation of an extra mention (acronym) "+mention);
									}
								}
							}
						}
					}
					
					// create the mention
					AbstractMention<?> mention = AbstractMention.build(type, startPos, endPos, ProcessorName.SUBEE, valueStr, language);
					result.add(mention);
					logger.log("Creation of the mention "+mention);
				}
			}
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not to use Freebase notable types (instead of all FB types) */ 
	private boolean notableType;
	/** Prefix used for the map files */
	protected static String FILE_PREFIX = "fb.";
	/** Name of the file containinig the list of ignored FB types */
	protected static String FILE_IGNORED = "ignored";
	/** Map tp convert Freebase types to EntityType values */
	protected static final Map<String,EntityType> TYPE_MAP = new HashMap<String,EntityType>();
	
	@Override
	protected void prepareRecognizer() throws ProcessorException
	{	try
		{	loadTypeMaps();
			loadUnknownTypes();
			if(discardDemonyms)
				loadDemonyms();
		}
		catch (FileNotFoundException e)
		{	throw new ProcessorException(e.getMessage());
		}
		catch (UnsupportedEncodingException e) 
		{	e.printStackTrace();
		}
	}
	
	/**
	 * Initializes the conversion map with some predefined
	 * files. Each file contains a list of FB types associated
	 * (mainly) to a specific type. An additional file contains
	 * a list of ignored types (for debugging purposes, and to
	 * ease the future completion of these files).
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing one of the map files.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle encoding.
	 */
	private synchronized void loadTypeMaps() throws FileNotFoundException, UnsupportedEncodingException
	{	if(TYPE_MAP.isEmpty())
		{	logger.log("Loading type maps");
			logger.increaseOffset();
			
			// set up the list of types
			String base = FileNames.FO_SUBEE + File.separator;
			List<EntityType> types = new ArrayList<EntityType>(HANDLED_TYPES);
			types.add(null);	// for the ignored types
			
			// process each corresponding file
			for(EntityType type: types)
			{	// open file
				String name = FILE_IGNORED;
				if(type!=null)
					name = type.toString().toLowerCase();
				String filePath = base + FILE_PREFIX + name + FileNames.EX_TEXT;
				logger.log("Processing file "+filePath);
				Scanner scanner = FileTools.openTextFileRead(filePath, "UTF-8");
				
				// read the content and add to the conversion map
				while(scanner.hasNextLine())
				{	String string = scanner.nextLine().trim();
					TYPE_MAP.put(string, type);
				}
				
				scanner.close();
			}
			
			logger.decreaseOffset();
			logger.log("Type maps loading complete");
		}
	}
	
	/**
	 * This method receives a list of Freebase types, and 
	 * infers the corresponding {@link EntityType}.
	 * 
	 * @param fbTypes
	 * 		List of Freebase types.
	 * @return
	 * 		Corresponding ArticleCategory, or {@code null} if node could be found.
	 */
	protected synchronized EntityType retrieveMentionType(List<String> fbTypes)
	{	logger.increaseOffset();
		Set<String> knownKeys = TYPE_MAP.keySet();
		
		// retrieve a list of EntityTypes corresponding to the FreeBase types
		List<EntityType> types = new ArrayList<EntityType>();
		for(String fbType: fbTypes)
		{	// try to use first the existing map
			EntityType type = TYPE_MAP.get(fbType);
			if(type!=null)
				types.add(type);
			
			// otherwise, try to use the type name (rough)
			else 
			{	// person
				if(fbType.endsWith("person"))
					types.add(EntityType.PERSON);

				// location
				else if(fbType.endsWith("location"))
					types.add(EntityType.LOCATION);
				
				// organization
				else if(fbType.endsWith("organization"))
					types.add(EntityType.ORGANIZATION);
				else if(fbType.endsWith("governmental_body"))
					types.add(EntityType.ORGANIZATION);
				else if(fbType.endsWith("collective"))
					types.add(EntityType.ORGANIZATION);
				
				// possibly add to the list of unknown types
				if(!knownKeys.contains(fbType))
					updateUnknownTypes(fbType);
			}
		}
		
		// determine the final type by prioritizing them
		EntityType result = null;
		if(types.contains(EntityType.ORGANIZATION))
			result = EntityType.ORGANIZATION;
		else if(types.contains(EntityType.LOCATION))
			result = EntityType.LOCATION;
		else if(types.contains(EntityType.PERSON))
			result = EntityType.PERSON;
		
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// UNKNOWN FREEBASE TYPES		/////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Freebase types not recognized by Subee (for debugging purposes) */
	protected static Set<String> UNKNOWN_TYPES = new TreeSet<String>();
	
	/**
	 * Loads the existing list of unknown Freebase types.
	 * This list is supposed to be processed manually,
	 * in order to complete the other FB-related files of
	 * Subee. The goal is to associate an EntityType value
	 * to all FB types.
	 */
	private synchronized void loadUnknownTypes()
	{	if(UNKNOWN_TYPES.isEmpty())
		{	logger.log("Loading unknown Freebase types");
			logger.increaseOffset();
			
			// set up file path
			String path = FileNames.FO_SUBEE + File.separator + FileNames.FI_UNKNOWN_TYPES;
			File file = new File(path);
			
			// retrieve existing unknown types
			try
			{	Scanner scanner = FileTools.openTextFileRead(file, "UTF-8");
				while(scanner.hasNextLine())
				{	String line = scanner.nextLine().trim();
					UNKNOWN_TYPES.add(line);
				}
				scanner.close();
			}
			catch (FileNotFoundException e)
			{	e.printStackTrace();
			}
			catch (UnsupportedEncodingException e)
			{	e.printStackTrace();
			}
			
			logger.decreaseOffset();
			logger.log("Loading complete");
		}
	}
	
	/**
	 * Adds the specified type to the list of unknown FB types,
	 * updating both the memory and the file versions of this list.
	 * 
	 * @param fbType
	 * 		New unknown Freebase type.
	 */
	protected synchronized void updateUnknownTypes(String fbType)
	{	if(!UNKNOWN_TYPES.contains(fbType)		// type not already in the list 
			&& !fbType.startsWith("/user/")		// not a user type
			&& !fbType.startsWith("/m/"))		// not a coded type
		{	// add to the memory list
			UNKNOWN_TYPES.add(fbType);

			// set up file path
			String path = FileNames.FO_SUBEE + File.separator + FileNames.FI_UNKNOWN_TYPES;
			File file = new File(path);
			
			// create the print writer
			try
			{	// open the file in append mode
				FileOutputStream fos = new FileOutputStream(file,true);
				OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
				PrintWriter printWriter = new PrintWriter(osw);

				// write the new type
				printWriter.println(fbType);
				printWriter.flush(); // just a precaution
				
				// close the stream
				printWriter.close();
			}
			catch (FileNotFoundException e)
			{	e.printStackTrace();
			}
			catch (UnsupportedEncodingException e)
			{	e.printStackTrace();
			}
		}
	}

	/////////////////////////////////////////////////////////////////
	// OCCURRENCES	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not the tool should try to detect additional occurrences of linked mentions */
	private boolean additionalOccurrences;
	
	/**
	 * Receives the mentions detected thanks to the hyperlinks, and tries 
	 * to find their other occurrences in the text.
	 * 
	 * @param article 
	 * 		Article to process.
	 * @param sureMentions 
	 * 		Mentions already detected, corresponding to hyperlinks.
	 * @return
	 * 		A new list of possible mentions, to be merged later with the sure mentions.
	 */
	private List<AbstractMention<?>> processOccurrences(Article article, List<AbstractMention<?>> sureMentions)
	{	logger.increaseOffset();
		String rawText = article.getRawText();
		ArticleLanguage language = article.getLanguage();
		List<AbstractMention<?>> result = new ArrayList<AbstractMention<?>>();
	
//		// sort mentions by type (we want to prioritize them)
//		logger.log("Sort mention by type");
//		TreeSet<AbstractEntity<?>> temp = new TreeSet<AbstractEntity<?>>(new Comparator<AbstractEntity<?>>()
//		{	@Override
//			public int compare(AbstractEntity<?> o1, AbstractEntity<?> o2)
//			{	int result = 0;
//				EntityType t1 = o1.getType();
//				EntityType t2 = o2.getType();
//				if(t1==EntityType.ORGANIZATION && t2!=EntityType.ORGANIZATION
//					|| t1==EntityType.PERSON && t2==EntityType.LOCATION)
//					result = -1;
//				else if(t2==EntityType.ORGANIZATION && t1!=EntityType.ORGANIZATION
//						|| t2==EntityType.PERSON && t1==EntityType.LOCATION)
//					result = 1;
//				else
//					result = o1.compareTo(o2);
//				return result;
//			}	
//		});
//		temp.addAll(sureMentions);
		
		// look for additional occurrences
		logger.log("Look for additional occurrences");
		for(AbstractMention<?> mention: sureMentions)
		{	String valueStr = mention.getStringValue();
			
			// look for the mention in the text
			String escapedStr = Pattern.quote(valueStr);
			Pattern p = Pattern.compile("\\b"+escapedStr+"\\b");
			Matcher m = p.matcher(rawText);
			while(m.find())
			{	int startPos = m.start();
//				// don't use the same position for several mentions
//				if(!positionAlreadyUsed(startPos, result))	// this test is now done later 
				{	int endPos = m.end();
					EntityType type = mention.getType();
					AbstractMention<?> ent = AbstractMention.build(type, startPos, endPos, ProcessorName.SUBEE, valueStr, language);
					result.add(ent);
				}
			}
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Merges two lists of mentions: <i>sure</i> mentions identified based on hyperlinks alone,
	 * and <i>possible</i> mentions identified using other means. If some possible mention overlaps
	 * with a sure one, then only the sure one is kept. If several possible mentions overlap,
	 * then the longest one (in terms of string length) is kept.
	 * 
	 * @param sureMentions
	 * 		Mentions for which we are reasonably sure.
	 * @param possibleMentions
	 * 		Mentions for which we are less sure.
	 * @return
	 * 		Result of the merging of both lists.
	 */
	private List<AbstractMention<?>> mergeMentionLists(List<AbstractMention<?>> sureMentions, List<AbstractMention<?>> possibleMentions)
	{	logger.log("Start merging sure and possible mention lists");
		logger.increaseOffset();
		ArrayList<AbstractMention<?>> result = new ArrayList<AbstractMention<?>>();
		
		// add all sure mentions
		logger.log("Add all sure mentions ("+sureMentions.size()+" mentions)");
		result.addAll(sureMentions);
		
		// remove overlapping possible mentions (keeping the longest ones)
		logger.log("Remove overlapping possible mentions ("+possibleMentions.size()+" mentions)");
		filterRedundancy(possibleMentions);
		logger.log("Removal complete ("+possibleMentions.size()+" mentions remaining)");
		
		// add to the result only the possible mentions with no overlap with sure ones
		logger.log("Adding remaining mentions to the sure ones, avoiding overlaps)");
		for(AbstractMention<?> mention: possibleMentions)
		{	AbstractMention<?> e = positionAlreadyUsed(mention, sureMentions);
			if(e==null)
				result.add(mention);
		}
	
		logger.decreaseOffset();
		logger.log("Merging complete: "+result.size()+" mentions in total");
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// DEMONYMS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether demonyms should be discarded ({@code true}) or ignored ({@code false}) */
	private boolean discardDemonyms;
	/** Set of demonyms (loaded from a file) */
	private static final Set<String> DEMONYMS = new TreeSet<String>();
	
	/**
	 * Loads the list of demonyms. It is supposed to contain only
	 * unambiguous demonyms, i.e. strings which are not at the same
	 * time the adjective and the name of the place. We want to keep
	 * locations.
	 */
	private synchronized void loadDemonyms()
	{	if(DEMONYMS.isEmpty())
		{	logger.log("Loading demonyms");
			logger.increaseOffset();
			
			// set up file path
			String path = FileNames.FO_CUSTOM_LISTS + File.separator + FileNames.FI_DEMONYMS;
			File file = new File(path);
			
			// retrieve demonyms
			try
			{	Scanner scanner = FileTools.openTextFileRead(file, "UTF-8");
				while(scanner.hasNextLine())
				{	String line = scanner.nextLine().trim();
					DEMONYMS.add(line);
				}
				scanner.close();
			}
			catch (FileNotFoundException e)
			{	e.printStackTrace();
			}
			catch (UnsupportedEncodingException e)
			{	e.printStackTrace();
			}
			
			logger.decreaseOffset();
			logger.log("Loading complete");
		}
	}

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, List<AbstractMention<?>> mentions) throws ProcessorException
	{	Mentions result = new Mentions(recognizer.getName());
		
		for(AbstractMention<?> mention: mentions)
			result.addMention(mention);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, List<AbstractMention<?>> mentions) throws IOException
	{	StringBuffer string = new StringBuffer();
		
		for(AbstractMention<?> mention: mentions)
			string.append(mention.toString() + "\n");
			
		writeRawResultsStr(article, string.toString());
	}
}
