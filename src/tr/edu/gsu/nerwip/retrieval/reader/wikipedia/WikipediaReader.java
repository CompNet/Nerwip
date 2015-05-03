package tr.edu.gsu.nerwip.retrieval.reader.wikipedia;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011 Yasa Akbulut, Burcu Küpelioğlu & Vincent Labatut
 * Copyright 2012 Burcu Küpelioğlu, Samet Atdağ & Vincent Labatut
 * Copyright 2013 Samet Atdağ & Vincent Labatut
 * Copyright 2014-15 Vincent Labatut
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleCategory;
import tr.edu.gsu.nerwip.retrieval.reader.ArticleReader;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.tools.corpus.ArticleCleaning;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.freebase.FbCommonTools;
import tr.edu.gsu.nerwip.tools.freebase.FbTypeTools;
import tr.edu.gsu.nerwip.tools.xml.XmlNames;

/**
 * From a specified URL, this class retrieves a Wikipedia page,
 * and gives access to the raw and linked texts.
 * 
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class WikipediaReader extends ArticleReader
{
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getName(URL url)
	{	String address = url.toString();
		
		// get the last part of the URL as the page name
		String temp[] = address.split("/");
		String result = temp[temp.length-1];
		
		// remove diacritics
		result = Normalizer.normalize(result, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CATEGORY		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of categories associated
	 * to the article being retrieved.
	 * First, we use the first line of text in the
	 * article, of the form "Firstname Lastname (19xx-19xx) was a politician...".
	 * If this leads to nothing, we use Freebase to retreive all
	 * the FB types associated to this Wikipedia page.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		List of categories (should be empty)
	 * 
	 * @throws ClientProtocolException
	 * 		Problem while using Freebase.
	 * @throws ParseException
	 * 		Problem while using Freebase.
	 * @throws IOException
	 * 		Problem while using Freebase.
	 * @throws org.json.simple.parser.ParseException
	 * 		Problem while using Freebase.
	 */
	public List<ArticleCategory> getArticleCategories(Article article) throws IOException, org.json.simple.parser.ParseException
	{	logger.log("Retrieving the article categories");
		logger.increaseOffset();
		
		// first we try with the first line of text in the article
		logger.log("Trying first with the first sentence of the article");
		List<ArticleCategory> result = getArticleCategoriesFromContent(article);
		
		// if we get nothing, we try with Freebase
		if(result.isEmpty())
		{	logger.log("Trying now by using Freebase (generally less efficient)");
			result = getArticleCategoriesFromFb(article);
		}
		
		if(result.isEmpty())
		{	logger.log("Could not find any category >> putting it into "+ArticleCategory.OTHER+")");
			result.add(ArticleCategory.OTHER);
		}
			
		logger.decreaseOffset();
		logger.log("Categories retrieved: "+result.toString());
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// FREEBASE CATEGORY	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** File containing the FB category map */
	private final static String FB_CONVERSION_FILE = "catmap.fb.xml";
	/** Map used to convert Freebase types to article categories */ 
	private final static CategoryMap FB_CONVERSION_MAP = new CategoryMap(FB_CONVERSION_FILE);
	
	/**
	 * Uses FreeBase to retrieve the category of
	 * this article, i.e. the activity domain
	 * of the concerned person.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		Domain of activity of this person: military, sciences, etc.
	 * 
	 * @throws org.json.simple.parser.ParseException 
	 * 		Problem while accessing Freebase.
	 * @throws IOException 
	 * 		Problem while accessing Freebase.
	 * @throws ParseException 
	 * 		Problem while accessing Freebase.
	 * @throws ClientProtocolException 
	 * 		Problem while accessing Freebase.
	 */
	public List<ArticleCategory> getArticleCategoriesFromFb(Article article) throws ClientProtocolException, ParseException, IOException, org.json.simple.parser.ParseException
	{	logger.log("Using Freebase to retrieve the article categories");
		String name = article.getName();
		logger.increaseOffset();
		
		// get all categories
		logger.log("Getting all available Freebase types");
		Set<ArticleCategory> categories = new TreeSet<ArticleCategory>();
		List<String> fbTypes = FbTypeTools.getAllTypes(name);
		
		logger.log("Processing them one by one");
		logger.increaseOffset();
		for(String fbType: fbTypes)
		{	logger.log("Processing type '"+fbType+"'");
			logger.increaseOffset();
			
			if(FB_CONVERSION_MAP.isIgnored(fbType))
			{	logger.log("Type rejected by the text/category map");
			}
			
			else
			{	logger.log("Type not rejected by the text/category map");
				String domain = fbType.substring(0,fbType.indexOf('/',1));
				ArticleCategory cat = FB_CONVERSION_MAP.get(domain);
				if(cat==null)
				{	logger.log("No category could be detected for this FB type");
				}
				else
				{	categories.add(cat);
					logger.log("Category identified for this FB type: "+cat);
				}
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		
// we used to select a single category: this doesn't work well >> now we keep them all
//		System.out.println("detected categories: " + categories.toString());
//		
//		// count categories
//		Map<ArticleCategory,Integer> counts = new HashMap<ArticleCategory, Integer>();
//		for(ArticleCategory cat: categories)
//		{	Integer count = counts.get(cat);
//			if(count==null)
//				count = 0;
//			count++;
//			counts.put(cat,count);
//		}
//		
//		// select most frequent category
//		ArticleCategory result = ArticleCategory.OTHER;
//		Collection<Integer> c = counts.values();
//		if(!c.isEmpty())
//		{	int max = Collections.max(c);
//			for(Entry<ArticleCategory,Integer> entry: counts.entrySet())
//			{	int count = entry.getValue();
//				if(count==max)
//					result = entry.getKey();
//			}
//		}
//		
//		System.out.println("Selected category: " + result);
//		System.out.println("\t"+name+"\t"+result);
		
		List<ArticleCategory> result = new ArrayList<ArticleCategory>(categories);
		Collections.sort(result);
		logger.log("detected categories: " + result.toString());
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CONTENT CATEGORY		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** File containing the FB category map */
	private final static String CONTENT_CONVERSION_FILE = "catmap.content.xml";
	/** Map of persons activities to article categories */
	private final static CategoryMap CONTENT_CONVERSION_MAP = new CategoryMap(CONTENT_CONVERSION_FILE);
	/** Verbs used to identify the part of the first sentence concerning the person's activity */
	private final static List<String> STATE_VERBS = Arrays.asList("is","was","may be","might be","should be","can be","must be","could be");
	
	/**
	 * Retrieces the category of the article
	 * from its content, and more particularly
	 * from its first sentence, which generally
	 * takes the following form in biographies:
	 * "Firstname Lastname (19xx-19xx) was/is a politician/artist/etc."
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		The identified categories, possibly an empty list if none
	 * 		could be identified.
	 */
	public List<ArticleCategory> getArticleCategoriesFromContent(Article article)
	{	logger.log("Using the article content to retrieve its categories");
		Set<ArticleCategory> categories = new TreeSet<ArticleCategory>();
		logger.increaseOffset();
	
		// get first sentence
		String text = article.getRawText();
		String firstSentence = null;
		Pattern pattern = Pattern.compile("[a-zA-Z0-9]{3,}\\. ");
		Matcher matcher = pattern.matcher(text);
		if(!matcher.find())
			logger.log("Could not find the first sentence of the article");
		else
		{	int i = matcher.end();
			firstSentence = text.substring(0,i);
			logger.log("First sentence of the article: \""+firstSentence+"\"");
		
			// identify state verb (to be)
			int index = firstSentence.length();
			String verb = null;
			for(String v: STATE_VERBS)
			{	pattern = Pattern.compile("[^a-zA-Z0-9]"+v+"[^a-zA-Z0-9]");
				matcher = pattern.matcher(firstSentence);
				if(matcher.find())
				{	i = matcher.start();
					if(i>-1 && i<index)
					{	index = i;
						verb = v;
					}
				}
			}
			if(verb==null)
				logger.log("WARNING: could not find any state verb in the first sentence");
			else
			{	logger.log("State verb detected in the sentence: '"+verb+"'");
				
				// look for key words located in the second part of the sentence (after the verb)
				firstSentence = firstSentence.substring(index+verb.length());
				logger.log("Focusing on the end of the sentence: \""+firstSentence+"\"");
				logger.increaseOffset();
				String temp[] = firstSentence.split("[^a-zA-Z0-9]");
				for(String key: temp)
				{	if(!key.isEmpty())
					{	ArticleCategory cat = CONTENT_CONVERSION_MAP.get(key);
						if(cat==null)
						{	
							logger.log(key+": no associated category");
						}
						else
						{	categories.add(cat);
							logger.log(key+": category "+cat);
						}
					}
				}
				logger.decreaseOffset();
			}
		}
		
		List<ArticleCategory> result = new ArrayList<ArticleCategory>(categories);
		Collections.sort(result);
		logger.decreaseOffset();
		logger.log("detected categories: " + result.toString());
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// RETRIEVE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////	
	/** Text allowing to detect wikipedia URL */
	public static final String DOMAIN = "wikipedia.org";
	
	/** Id of the element containing the article content in the Wikipedia page */
	private final static String ID_CONTENT = "mw-content-text";
	/** Id of the element containing the article title in the Wikipedia page */
	private final static String ID_TITLE = "firstHeading";
	
	/** Class of phonetic transcriptions */
	private final static String CLASS_IPA = "IPA";
	/** Class of WP messages */
	private final static String CLASS_DABLINK = "dablink";
	/** Class of WP edit buttons */
	private final static String CLASS_EDIT = "editsection";
	/** Class of external hyperlinks of the Wikipedia page */
	private final static String CLASS_EXTERNAL = "external";
	/** Class of image hyperlinks */
	private final static String CLASS_IMAGE = "image";
	/** Class of the element containing the infobox of the Wikipedia page */
	private final static String CLASS_INFORMATIONBOX = "infobox";
	/** Class of the element containing some language-related information */
	private final static String CLASS_LANGUAGEICON = "languageicon";
	/** Class of zoom-in buttons */
	private final static String CLASS_MAGNIFY = "magnify";
	/** Class of the element containing some media material (audio, video...) */
	private final static String CLASS_MEDIA = "mediaContainer";
	/** Class of the element containing some metadata (e.g. wikimedia link) */
	private final static String CLASS_METADATA = "metadata";
	/** Class of the element containing navigation boxes */
	private final static String CLASS_NAVIGATIONBOX = "navbox";
	/** Class of the element containing personal data box (?) */
	private final static String CLASS_PERSONDATA = "persondata";
	/** Class of the element containing the list of references */
	private final static String CLASS_REFERENCES = "reflist";
	/** Class of the element containing a related link */
	private final static String CLASS_RELATEDLINK = "rellink";
	/** Class of the element containing the table of content */
	private final static String CLASS_TABLEOFCONTENT = "toc";
	/** Class used for certain pictures */
	private final static String CLASS_THUMB = "thumb";
	/** Class of icones located at the begining of pages */
	private final static String CLASS_TOPICON = "topicon";
	/** Class of the element containing some kind of generated table */
	private final static String CLASS_WIKITABLE = "wikitable";
	
	/** Title of audio links  */
	private final static String TITLE_LISTEN = "Listen";
	/** Disambiguation link */
	private final static String PARAGRAPH_FORTHE = "For the";

	/** List of sections to be ignored */
	private final static List<String> IGNORED_SECTIONS = Arrays.asList(
		"audio books", "audio recordings", /*"awards", "awards and honors",*/
		"bibliography", "books",
		"collections", "collections (selection)",
		"directed",
		"external links",
		"film adaptations", "film and television adaptations", "filmography", "footnotes", "further reading",
		"gallery",
//		"honours",
		"main writings",
		"notes", "nudes",
		"patents", "publications",
		"quotes",
		"references",
		"secondary bibliography", "see also", "selected bibliography", "selected filmography", "selected list of works", "selected works", "self-portraits", "sources", "stage adaptations",
		"texts of songs", "theme exhibitions", "theme exhibitions (selection)",
		"works"
	);

	/**
	 * Retrieve the text located in 
	 * a paragraph (P) HTML element.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @param linkedStr
	 * 		Current text with hyperlinks.
	 */
	private void processParagraphElement(Element element, StringBuilder rawStr, StringBuilder linkedStr)
	{	// possibly add a new line character first
		if(rawStr.length()>0 && rawStr.charAt(rawStr.length()-1)!='\n')
		{	rawStr.append("\n");
			linkedStr.append("\n");
		}
		
		// recursive processing
		processTextElement(element,rawStr,linkedStr);
		
		// possibly add a new line character
		if(rawStr.charAt(rawStr.length()-1)!='\n')
		{	rawStr.append("\n");
			linkedStr.append("\n");
		}
	}

	/**
	 * Retrieve the text located in 
	 * a quote (BLOCKQUOTE) HTML element.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @param linkedStr
	 * 		Current text with hyperlinks.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	private boolean processQuoteElement(Element element, StringBuilder rawStr, StringBuilder linkedStr)
	{	boolean result = true;
		
		// possibly modify the previous characters 
		if(rawStr.length()>0 && rawStr.charAt(rawStr.length()-1)=='\n')
		{	rawStr.deleteCharAt(rawStr.length()-1);
			linkedStr.deleteCharAt(linkedStr.length()-1);
		}
		
		// insert quotes
		rawStr.append(" \"");
		linkedStr.append(" \"");
		
		// recursive processing
		int rawIdx = rawStr.length();
		int linkedIdx = linkedStr.length();
		processTextElement(element,rawStr,linkedStr);

		// possibly remove characters added after quote marks
		while(rawStr.length()>rawIdx && 
			(rawStr.charAt(rawIdx)=='\n' || rawStr.charAt(rawIdx)==' '))
		{	rawStr.deleteCharAt(rawIdx);
			linkedStr.deleteCharAt(linkedIdx);
		}
		
		// possibly modify the ending characters 
		if(rawStr.length()>0 && rawStr.charAt(rawStr.length()-1)=='\n')
		{	rawStr.deleteCharAt(rawStr.length()-1);
			linkedStr.deleteCharAt(linkedStr.length()-1);
		}

		// insert quotes
		rawStr.append("\"");
		linkedStr.append("\"");
		
		return result;
	}
	
	/**
	 * Retrieve the text located in 
	 * a span (SPAN) HTML element.
	 * <br/>
	 * We process everything but
	 * the phonetic transcriptions.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @param linkedStr
	 * 		Current text with hyperlinks.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	private boolean processSpanElement(Element element, StringBuilder rawStr, StringBuilder linkedStr)
	{	boolean result;
		String eltClass = element.attr(XmlNames.ATT_CLASS);
		
		if(eltClass==null || 
			// we don't need phonetic transcriptions, and they can mess up NER tools
			(!eltClass.contains(CLASS_IPA)
			// we also ignore WP buttons such as the "edit" links placed in certain section headers
			&& !eltClass.contains(CLASS_EDIT)
			// language indications
			&& !eltClass.contains(CLASS_LANGUAGEICON)))
			
		{	result = true;
			// otherwise, we process what's inside the span tag
			processTextElement(element,rawStr,linkedStr);
		}
		
		else
			result = false;
		
		return result;
	}
	
	/**
	 * Retrieve the text located in 
	 * a hyperlink (A) HTML element.
	 * <br/>
	 * We ignore all external links,
	 * as well as linked images.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @param linkedStr
	 * 		Current text with hyperlinks.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	private boolean processHyperlinkElement(Element element, StringBuilder rawStr, StringBuilder linkedStr)
	{	boolean result;
		String eltClass = element.attr(XmlNames.ATT_CLASS);
		
//		if(eltClass==null)
		{	result = true;
			
			// simple text
			String str = element.text();
			if(!str.isEmpty())
			{	rawStr.append(str);
			
//if(str.contains("Philadelphia, Pa."))	//debug stuff
//	System.out.print("");
				
				// hyperlink
				String eltTitle = element.attr(XmlNames.ATT_TITLE);
				if((eltClass==null
						|| (!eltClass.contains(CLASS_IMAGE) && !eltClass.contains(CLASS_EXTERNAL)))
						&& (eltTitle==null	
						|| (!eltTitle.contains(TITLE_LISTEN)))
				)
				{	String href = element.attr(XmlNames.ATT_HREF);
					String code = "<" + XmlNames.ELT_A + " " +XmlNames.ATT_HREF + "=\"" + href + "\">" + str + "</" + XmlNames.ELT_A + ">";
					linkedStr.append(code);
				}
				else
					linkedStr.append(str);
			}
		}
		
//		else
//			result = false;
		
		return result;
	}
	
	/**
	 * Retrieve the text located in 
	 * a list (UL or OL) HTML element.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @param linkedStr
	 * 		Current text with hyperlinks.
	 * @param ordered
	 * 		Whether the list is numbered or not.
	 */
	private void processListElement(Element element, StringBuilder rawStr, StringBuilder linkedStr, boolean ordered)
	{	// possibly remove the last new line character
		char c = rawStr.charAt(rawStr.length()-1);
		if(c=='\n')
		{	rawStr.deleteCharAt(rawStr.length()-1);
			linkedStr.deleteCharAt(linkedStr.length()-1);
		}
		
		// possibly remove preceeding space
		c = rawStr.charAt(rawStr.length()-1);
		if(c==' ')
		{	rawStr.deleteCharAt(rawStr.length()-1);
			linkedStr.deleteCharAt(linkedStr.length()-1);
		}
		
		// possibly add a column
		c = rawStr.charAt(rawStr.length()-1);
		if(c!='.' && c!=':' && c!=';')
		{	rawStr.append(":");
			linkedStr.append(":");
		}
		
		// process each list element
		int count = 1;
		for(Element listElt: element.getElementsByTag(XmlNames.ELT_LI))
		{	// add leading space
			rawStr.append(" ");
			linkedStr.append(" ");
			
			// possibly add number
			if(ordered)
			{	rawStr.append(count+") ");
				linkedStr.append(count+") ");
			}
			count++;
			
			// get text and links
			processTextElement(listElt,rawStr,linkedStr);
			
			// possibly remove the last new line character
			c = rawStr.charAt(rawStr.length()-1);
			if(c=='\n')
			{	rawStr.deleteCharAt(rawStr.length()-1);
				linkedStr.deleteCharAt(linkedStr.length()-1);
			}
			
			// add final separator
			rawStr.append(";");
			linkedStr.append(";");
		}
		
		// possibly remove last separator
		c = rawStr.charAt(rawStr.length()-1);
		if(c==';')
		{	rawStr.deleteCharAt(rawStr.length()-1);
			linkedStr.deleteCharAt(linkedStr.length()-1);
			c = rawStr.charAt(rawStr.length()-1);
			if(c!='.')
			{	rawStr.append(".");
				linkedStr.append(".");
			}
			rawStr.append("\n");
			linkedStr.append("\n");
		}
	}
	
	/**
	 * Retrieve the text located in 
	 * a description list (DL) HTML element.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @param linkedStr
	 * 		Current text with hyperlinks.
	 */
	private void processDescriptionListElement(Element element, StringBuilder rawStr, StringBuilder linkedStr)
	{	// possibly remove the last new line character
		char c = rawStr.charAt(rawStr.length()-1);
		if(c=='\n')
		{	rawStr.deleteCharAt(rawStr.length()-1);
			linkedStr.deleteCharAt(linkedStr.length()-1);
		}
		
		// possibly remove preceeding space
		c = rawStr.charAt(rawStr.length()-1);
		if(c==' ')
		{	rawStr.deleteCharAt(rawStr.length()-1);
			linkedStr.deleteCharAt(linkedStr.length()-1);
		}
		
		// possibly add a column
		c = rawStr.charAt(rawStr.length()-1);
		if(c!='.' && c!=':' && c!=';')
		{	rawStr.append(":");
			linkedStr.append(":");
		}
		
		// process each list element
		Elements elements = element.children();
		Iterator<Element> it = elements.iterator();
		Element tempElt = null;
		if(it.hasNext())
			tempElt = it.next();
		while(tempElt!=null)
		{	// add leading space
			rawStr.append(" ");
			linkedStr.append(" ");
			
			// get term
			String tempName = tempElt.tagName();
			if(tempName.equals(XmlNames.ELT_DT))
			{	// process term
				processTextElement(tempElt,rawStr,linkedStr);
				
				// possibly remove the last new line character
				c = rawStr.charAt(rawStr.length()-1);
				if(c=='\n')
				{	rawStr.deleteCharAt(rawStr.length()-1);
					linkedStr.deleteCharAt(linkedStr.length()-1);
				}
				
				// possibly remove preceeding space
				c = rawStr.charAt(rawStr.length()-1);
				if(c==' ')
				{	rawStr.deleteCharAt(rawStr.length()-1);
					linkedStr.deleteCharAt(linkedStr.length()-1);
				}
				
				// possibly add a column and space
				c = rawStr.charAt(rawStr.length()-1);
				if(c!='.' && c!=':' && c!=';')
				{	rawStr.append(": ");
					linkedStr.append(": ");
				}
				
				// go to next element
				if(it.hasNext())
					tempElt = it.next();
				else
					tempElt = null;
			}
			
			// get definition
//			if(tempName.equals(XmlNames.ELT_DD))
			if(tempElt!=null)
			{	// process term
				processTextElement(tempElt,rawStr,linkedStr);
				
				// possibly remove the last new line character
				c = rawStr.charAt(rawStr.length()-1);
				if(c=='\n')
				{	rawStr.deleteCharAt(rawStr.length()-1);
					linkedStr.deleteCharAt(linkedStr.length()-1);
				}
				
				// possibly remove preceeding space
				c = rawStr.charAt(rawStr.length()-1);
				if(c==' ')
				{	rawStr.deleteCharAt(rawStr.length()-1);
					linkedStr.deleteCharAt(linkedStr.length()-1);
				}
				
				// possibly add a semi-column
				c = rawStr.charAt(rawStr.length()-1);
				if(c!='.' && c!=':' && c!=';')
				{	rawStr.append(";");
					linkedStr.append(";");
				}
				
				// go to next element
				if(it.hasNext())
					tempElt = it.next();
				else
					tempElt = null;
			}
		}
		
		// possibly remove last separator
		c = rawStr.charAt(rawStr.length()-1);
		if(c==';')
		{	rawStr.deleteCharAt(rawStr.length()-1);
			linkedStr.deleteCharAt(linkedStr.length()-1);
			c = rawStr.charAt(rawStr.length()-1);
			if(c!='.')
			{	rawStr.append(".");
				linkedStr.append(".");
			}
			rawStr.append("\n");
			linkedStr.append("\n");
		}
	}
	
	/**
	 * Retrieve the text located in 
	 * a division (DIV) HTML element.
	 * <br/>
	 * We ignore some of them: table
	 * of content, reference list, related links, etc.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @param linkedStr
	 * 		Current text with hyperlinks.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	private boolean processDivisionElement(Element element, StringBuilder rawStr, StringBuilder linkedStr)
	{	boolean result;
		String eltClass = element.attr(XmlNames.ATT_CLASS);
		
//if(eltClass.contains("thumb"))
//	System.out.print("");
		
		if(eltClass==null || 
			// we ignore infoboxes
			(!eltClass.contains(CLASS_TABLEOFCONTENT)
			// list of bibiliographic references located at the end of the page
			&& !eltClass.contains(CLASS_REFERENCES)
			// WP warning links (disambiguation and such)
			&& !eltClass.contains(CLASS_DABLINK)
			// related links
			&& !eltClass.contains(CLASS_RELATEDLINK)
			// audio or video clip
			&& !eltClass.contains(CLASS_MEDIA)
			// button used to magnify images
			&& !eltClass.contains(CLASS_MAGNIFY)
			// icons located at the top of the page
			&& !eltClass.contains(CLASS_TOPICON)
			))
		{	result = true;
			processTextElement(element, rawStr, linkedStr);
		}
		
		else
			result = false;
		
		return result;
	}
	
	/**
	 * Retrieve the text located in a table (TABLE) HTML element.
	 * <br/>
	 * We process each cell in the table as a text element. 
	 * Some tables are ignored: infoboxes, wikitables, navboxes,
	 * metadata, persondata, etc. 
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @param linkedStr
	 * 		Current text with hyperlinks.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	private boolean processTableElement(Element element, StringBuilder rawStr, StringBuilder linkedStr)
	{	boolean result;
		String eltClass = element.attr(XmlNames.ATT_CLASS);
		
		if(eltClass==null || 
			// we ignore infoboxes
			(!eltClass.contains(CLASS_INFORMATIONBOX)
			// and wikitables
			&& !eltClass.contains(CLASS_WIKITABLE)
			// navigation boxes
			&& !eltClass.contains(CLASS_NAVIGATIONBOX)
			// navigation boxes, WP warnings (incompleteness, etc.)
			&& !eltClass.contains(CLASS_METADATA)
			// personal data box (?)
			&& !eltClass.contains(CLASS_PERSONDATA)))
			
		{	result = true;
			Element tbodyElt = element.children().get(0);
			
			for(Element rowElt: tbodyElt.children())
			{	for(Element colElt: rowElt.children())
				{	// process cell content
					processTextElement(colElt, rawStr, linkedStr);
					
					// possibly add final dot and space. 
					if(rawStr.charAt(rawStr.length()-1)!=' ')
					{	if(rawStr.charAt(rawStr.length()-1)=='.')
						{	rawStr.append(" ");
							linkedStr.append(" ");
						}
						else
						{	rawStr.append(". ");
							linkedStr.append(". ");
						}
					}
				}
			}
		}
		
		else
			result = false;
		
		return result;
	}
	
	/**
	 * Extract text and hyperlinks from an element
	 * supposingly containing only text.
	 * 
	 * @param textElement
	 * 		The element to be processed.
	 * @param rawStr
	 * 		The StringBuffer to contain the raw text.
	 * @param linkedStr
	 * 		The StringBuffer to contain the text with hyperlinks.
	 */
	private void processTextElement(Element textElement, StringBuilder rawStr, StringBuilder linkedStr)
	{	// we process each element contained in the specified text element
		for(Node node: textElement.childNodes())
		{	// element node
			if(node instanceof Element)
			{	Element element = (Element) node;
				String eltName = element.tag().getName();
				
				// section headers: same thing
				if(eltName.equals(XmlNames.ELT_H2) || eltName.equals(XmlNames.ELT_H3)
					|| eltName.equals(XmlNames.ELT_H4) || eltName.equals(XmlNames.ELT_H5) || eltName.equals(XmlNames.ELT_H6))
				{	processParagraphElement(element,rawStr,linkedStr);
				}
	
				// paragraphs inside paragraphs are processed recursively
				else if(eltName.equals(XmlNames.ELT_P))
				{	processParagraphElement(element,rawStr,linkedStr);
				}
				
				// superscripts are to be avoided
				else if(eltName.equals(XmlNames.ELT_SUP))
				{	// they are either external references or WP inline notes
					// cf. http://en.wikipedia.org/wiki/Template%3ACitation_needed
				}
				
				// small caps are placed before phonetic transcriptions of names, which we avoid
				else if(eltName.equals(XmlNames.ELT_SMALL))
				{	// we don't need them, and they can mess up NER tools
				}
				
				// we ignore certain types of span (phonetic trancription, WP buttons...) 
				else if(eltName.equals(XmlNames.ELT_SPAN))
				{	processSpanElement(element,rawStr,linkedStr);
				}
				
				// hyperlinks must be included in the linked string, provided they are not external
				else if(eltName.equals(XmlNames.ELT_A))
				{	processHyperlinkElement(element,rawStr,linkedStr);
				}
				
				// lists
				else if(eltName.equals(XmlNames.ELT_UL))
				{	processListElement(element,rawStr,linkedStr,false);
				}
				else if(eltName.equals(XmlNames.ELT_OL))
				{	processListElement(element,rawStr,linkedStr,true);
				}
				else if(eltName.equals(XmlNames.ELT_DL))
				{	processDescriptionListElement(element,rawStr,linkedStr);
				}
				
				// list item
				else if(eltName.equals(XmlNames.ELT_LI))
				{	processTextElement(element,rawStr,linkedStr);
				}
	
				// divisions are just processed recursively
				else if(eltName.equals(XmlNames.ELT_DIV))
				{	processDivisionElement(element,rawStr,linkedStr);
				}
				
				// quotes are just processed recursively
				else if(eltName.equals(XmlNames.ELT_BLOCKQUOTE))
				{	processQuoteElement(element,rawStr,linkedStr);
				}
				// citation
				else if(eltName.equals(XmlNames.ELT_CITE))
				{	processParagraphElement(element,rawStr,linkedStr);
				}
				
				// other elements are considered as simple text
				else
				{	String text = element.text();
					rawStr.append(text);
					linkedStr.append(text);
				}
			}
			
			// text node
			else if(node instanceof TextNode)
			{	// get the text
				TextNode textNode = (TextNode) node;
				String text = textNode.text();
				// if at the begining of a new line, or already preceeded by a space, remove leading spaces
				while(rawStr.length()>0 
						&& (rawStr.charAt(rawStr.length()-1)=='\n' || rawStr.charAt(rawStr.length()-1)==' ') 
						&& text.startsWith(" "))
					text = text.substring(1);
				// complete string buffers
				rawStr.append(text);
				linkedStr.append(text);
			}
		}
	}
	
	/**
	 * Pulls a text from a Wikipedia URL without images, tags, etc.
	 * 
	 * @param url
	 * 		Address of the targetted text.
	 * @return
	 * 		An Article object representing the retrieved text.
	 * 
	 * @throws ReaderException
	 * 		Problem while retrieving the text.
	 */
	@Override
	public Article read(URL url) throws ReaderException
	{	Article result = null;
		String name = getName(url);
		
		try
		{	// get the page
			String address = url.toString();
			logger.log("Retrieving page "+address);
			long startTime = System.currentTimeMillis();
			Document document  = retrieveSourceCode(name,url);
					
			// get its title
			Element firstHeadingElt = document.getElementsByAttributeValue(XmlNames.ATT_ID,ID_TITLE).get(0);
			String title = firstHeadingElt.text();
			logger.log("Get title: "+title);
			
			// get raw and linked texts
			logger.log("Get raw and linked texts.");
			StringBuilder rawStr = new StringBuilder();
			StringBuilder linkedStr = new StringBuilder();
			Element bodyContentElt = document.getElementsByAttributeValue(XmlNames.ATT_ID,ID_CONTENT).get(0);
			// processing each element in the content part
			boolean ignoringSection = false;
			boolean first = true;
			for(Element element: bodyContentElt.children())
			{	String eltName = element.tag().getName();
				String eltClass = element.attr(XmlNames.ATT_CLASS);
			
				// section headers
				if(eltName.equals(XmlNames.ELT_H2))
				{	first = false;
					// get section name
					StringBuilder fakeRaw = new StringBuilder();
					StringBuilder fakeLinked = new StringBuilder();
					processParagraphElement(element,fakeRaw,fakeLinked);
					String str = fakeRaw.toString().trim().toLowerCase(Locale.ENGLISH);
					// check section name
					if(IGNORED_SECTIONS.contains(str))
						ignoringSection = true;
					else
					{	ignoringSection = false;
						rawStr.append("\n-----");
						linkedStr.append("\n-----");
						processParagraphElement(element,rawStr,linkedStr);
					}
				}
			
				else if(!ignoringSection)
				{	// lower sections
					if(eltName.equals(XmlNames.ELT_H3) || eltName.equals(XmlNames.ELT_H4) 
						|| eltName.equals(XmlNames.ELT_H5) || eltName.equals(XmlNames.ELT_H6))
					{	first = false;
						processParagraphElement(element,rawStr,linkedStr);
					}
					
					// paragraph
					else if(eltName.equals(XmlNames.ELT_P))
					{	String str = element.text();
						// ignore possible initial disambiguation link
						if(!first || !str.startsWith(PARAGRAPH_FORTHE))	 
						{	first = false;
							processParagraphElement(element,rawStr,linkedStr);
						}
					}
					
					// list
					else if(eltName.equals(XmlNames.ELT_UL))
					{	first = false;
						processListElement(element,rawStr,linkedStr,false);
					}
					else if(eltName.equals(XmlNames.ELT_OL))
					{	first = false;
						processListElement(element,rawStr,linkedStr,true);
					}
					else if(eltName.equals(XmlNames.ELT_DL))
					{	first = false;
						processDescriptionListElement(element,rawStr,linkedStr);
					}
					
					// tables
					else if(eltName.equals(XmlNames.ELT_TABLE))
					{	first = !processTableElement(element, rawStr, linkedStr);
					}
					
					// divisions
					else if(eltName.equals(XmlNames.ELT_DIV))
					{	// ignore possible initial picture 
						if(!first || eltClass==null || !eltClass.contains(CLASS_THUMB))
							first = !processDivisionElement(element, rawStr, linkedStr);
					}
				
					// we ignore certain types of span (phonetic trancription, WP buttons...) 
					else if(eltName.equals(XmlNames.ELT_SPAN))
					{	first = !processSpanElement(element,rawStr,linkedStr);
					}
					
					// hyperlinks must be included in the linked string, provided they are not external
					else if(eltName.equals(XmlNames.ELT_A))
					{	first = !processHyperlinkElement(element,rawStr,linkedStr);
					}
					
					// quotes are just processed recursively
					else if(eltName.equals(XmlNames.ELT_BLOCKQUOTE))
					{	first = !processQuoteElement(element,rawStr,linkedStr);
					}
					
					// other tags are ignored
				}
			}
			
			// create article object
			result = new Article(name);
			result.setTitle(title);
			result.setUrl(url);
			result.initDate();
			
			// clean text
			String rawText = rawStr.toString();
			rawText = cleanText(rawText);
//			rawText = ArticleCleaning.replaceChars(rawText);
			result.setRawText(rawText);
			logger.log("Length of the raw text: "+rawText.length()+" chars.");
			String linkedText = linkedStr.toString();
			linkedText = cleanText(linkedText);
//			linkedText = ArticleCleaning.replaceChars(linkedText);
			result.setLinkedText(linkedText);
			logger.log("Length of the linked text: "+linkedText.length()+" chars.");
			
			// get original html source code
			logger.log("Get original HTML source code.");
			String originalPage = document.toString();
			result.setOriginalPage(originalPage);
			logger.log("Length of the original page: "+originalPage.length()+" chars.");
			
			// get the categories of the article 
			List<ArticleCategory> categories = getArticleCategories(result);
			result.setCategories(categories);

			long endTime = System.currentTimeMillis();
			logger.log("Total duration: "+(endTime-startTime)+" ms.");
		}
		catch (ClientProtocolException e)
		{	e.printStackTrace();
		} 
		catch (ParseException e)
		{	e.printStackTrace();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
		catch (org.json.simple.parser.ParseException e)
		{	e.printStackTrace();
		}
		
		return result;
	}
}
