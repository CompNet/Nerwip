package fr.univavignon.nerwip.processing.external.nero;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.external.AbstractExternalDelegateRecognizer;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * This class acts as an interface with Nero.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * <li>{@code tagger}: {@code CRF}</li>
 * <li>{@code flat}: {@code true}</li>
 * <li>{@code ignorePronouns}: {@code true}</li>
 * <li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * Official Nero website: <a
 * href="https://nero.irisa.fr/">https://nero.irisa.fr/</a>
 * <br/>
 * <b>Warning:</b> it should be noted Nero was originally designed 
 * to treat speech transcriptions, and is therefore not very 
 * robust when handling punctuation. It is also very sensitive to 
 * specific characters like {@code û} or {@code ë}, or combinations 
 * of characters such as newline {@code '\n'} followed by 
 * {@code '"'}. Those should be avoided at all cost in the
 * parsed text, otherwise this delegate will not be able to process 
 * Nero's output.
 * <br/>
 * Nero was tested only on a Linux system.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class NeroDelegateRecognizer extends AbstractExternalDelegateRecognizer
{	
	/**
	 * Builds and sets up an object representing the Nero tool.
	 * 
	 * @param nero
	 * 		Recognizer in charge of this delegate.
	 * @param neroTagger
	 * 		NeroTagger used by Nero (CRF or FST).
	 * @param flat
	 * 		Whether mentions can contain other mentions ({@code false}) or
	 * 		are mutually exclusive ({@code true}).
	 * @param ignorePronouns
	 *      Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 *      Whether or not stop words should be excluded from the
	 *      detection.
	 */
	public NeroDelegateRecognizer(Nero nero, NeroTagger neroTagger, boolean flat, boolean ignorePronouns, boolean exclusionOn)
	{	super(nero, false, ignorePronouns, exclusionOn);
		
		this.neroTagger = neroTagger;
		this.flat = flat;
		
		setIgnoreNumbers(false);
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getFolder()
	{	String result = recognizer.getName().toString();

		result = result + "_" + "tagger=" + neroTagger;
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "exclude=" + exclusionOn;

		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES 	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types recognized by Nero */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList
	(
		EntityType.DATE, 
		EntityType.FUNCTION, 
		EntityType.LOCATION, 
		EntityType.ORGANIZATION,
		EntityType.PERSON,
		EntityType.PRODUCTION
	);

	@Override
	public List<EntityType> getHandledEntityTypes() 
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES 		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this recognizer can treat */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList
	(	
//		ArticleLanguage.EN, 
		ArticleLanguage.FR
	);

	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// TAGGER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** NeroTagger used by Nero */
	private NeroTagger neroTagger = null;
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING 			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether mentions can contain other mentions ({@code false}) or are mutually exclusive ({@code true}) */
	private boolean flat = false;
	/** Switch used to enable the detection of non-flat mentions */
	private final static String FLAT_SWITCH = "-f2h";
	/** Name of the temporary file generated for Nero */
	private static final String TEMP_NAME = "temp";
	/** Maximal size of text part processed at once */
	private static final int MAX_SIZE = 25000;

	/**
	 * Returns the path of the temporary file
	 * created for Nero (containing the article
	 * content).
	 * 
	 * @param article
	 * 		The concerned article.
	 * @param part
	 * 		The concerned part of the article.
	 * @return
	 * 		Path of the temporary file.
	 */
	private String getTempFile(Article article, int part)
	{	String result = article.getFolderPath()
			+ File.separator + getFolder() 
			+ File.separator + TEMP_NAME
			+ "." + part + FileNames.EX_TEXT;
		return result; 
	}
	
	@Override
	protected String detectMentions(Article article) throws ProcessorException
	{	logger.increaseOffset();
		boolean outRawResults = recognizer.doesOutputRawResults();
		StringBuffer tempRes = new StringBuffer();
		String text = article.getRawText();

		// debug
//		String val = System.getenv("PATH");
//		System.out.println(val);
//		val = System.getenv("IRISA_NE");
//		System.out.println(val);
		
		// we need to break down the text: Nero can't handle more than 100000 chars at once
		// (at least on the test computer)
		List<String> parts = StringTools.splitText(text, MAX_SIZE);

		for(int i=0;i<parts.size();i++)
		{	logger.log("Processing Nero part #"+(i+1)+"/"+parts.size());
			logger.increaseOffset();
			String part = parts.get(i);
			
			try
			{	// write article raw text in a temp file
				part = cleanText(part);
				String tempPath = getTempFile(article,i);
				File tempFile = new File(tempPath);
				logger.log("Copying the article content in partial temp file "+tempFile);
//				FileTools.writeTextFile(tempFile, part, "UTF-8");
				FileTools.writeTextFile(tempFile, part, "ISO-8859-1");
				
				// invoke the external tool and retrieve its output
				logger.log("Invoking Nero: ");
				logger.increaseOffset();
					Runtime rt = Runtime.getRuntime();
					String mainCommand = "cat " + tempPath + " | " 
						+ "." + File.separator + FileNames.FO_NERO_SCRIPTS + File.separator 
						+ FileNames.FI_NERO_BASH + " " + neroTagger.toString();
				    if(!flat)
				    	mainCommand = mainCommand + " " + FLAT_SWITCH;
			    	String[] commands = 
					{	"/bin/sh", "-c", 
						mainCommand
					};
			    	logger.log(Arrays.asList(commands));
					Process proc = rt.exec(commands);
//		    		Process proc = rt.exec("/bin/sh -c echo $PATH"); // debug
				logger.decreaseOffset();
			
				// standard error
				String error = "";
				{	//BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream(),"ISO-8859-1"));
					String line;
					while((line=stdError.readLine()) != null)
					{	System.out.println(line);
						error = error + "\n" + line;
					}
				}
				if(!error.isEmpty())
				{	logger.log("Some error(s) occured:");
					logger.increaseOffset();
						logger.log(error);
					logger.decreaseOffset();
				}
				
				// standard output
				if(error.isEmpty())
				{	String res = "";
					//BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream(),"ISO-8859-1"));
					String line;
					while((line=stdInput.readLine()) != null)
					{	System.out.println(line);
						res = res + "\n" + line;
					}
					tempRes.append(res);
					logger.log("Raw results:");
					logger.increaseOffset();
						logger.log(res);
					logger.decreaseOffset();
					
					// possibly record the raw results (for debug purposes)
					if(outRawResults)
					{	File rrF = getRawFile(article);
						logger.log("Writing the raw results in file "+rrF);
						FileTools.writeTextFile(rrF, res, "UTF-8");
					}
				}
				else
					throw new ProcessorException(error);
				
				// possibly remove the temp file
				if(!outRawResults)
					tempFile.delete();
				
				logger.decreaseOffset();
			}
			catch (IOException e)
			{	//e.printStackTrace();
				throw new ProcessorException(e.getMessage());
			}
		}
		
		logger.decreaseOffset();
		String result = tempRes.toString();
		return result;
	}
	
	/**
	 * Some characters must be cleaned from the text to be annotated by
	 * Nero, otherwise it outputs additional characters which makes the
	 * conversion much harder.
	 * 
	 * @param text
	 * 		Original text.
	 * @return
	 * 		Cleaned text.
	 */
	private String cleanText(String text)
	{	String result = text;
		
		result = result.replaceAll("ë", "e");
		result = result.replaceAll("û", "u");
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of URI to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	/** List of ignored entity types */
	private final static List<String> IGNORED_TYPES = Arrays.asList(
		"amount",
		"unk"
	);
	
	/** Initialization of the conversion map */
	static 
	{	CONVERSION_MAP.put("fonc", EntityType.FUNCTION);
		CONVERSION_MAP.put("loc", EntityType.LOCATION);
		CONVERSION_MAP.put("org", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("pers", EntityType.PERSON);
		CONVERSION_MAP.put("prod", EntityType.PRODUCTION);
		CONVERSION_MAP.put("time", EntityType.DATE);
	}
//	/** Ignored characters */
//	private final static List<Character> IGNORED_CHARS = Arrays.asList(
//		'œ','Œ', //this was put in the method used to clean article content
//		'æ','Æ'
//	);
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, String data) throws ProcessorException
	{	Mentions result = new Mentions(recognizer.getName());
		String originalText = article.getRawText();

		LinkedList<EntityType> types = new LinkedList<EntityType>();
		LinkedList<Integer> startPos1 = new LinkedList<Integer>();
//		LinkedList<Integer> startPos2 = new LinkedList<Integer>();
		LinkedList<String> tags = new LinkedList<String>();
		
		int i1 = 0;
		int i2 = 0;
		int c1 = originalText.codePointAt(i1);
		int c2 = data.codePointAt(i2);
		
		// possibly pass starting newline characters 
		while(c1=='\n')
		{	i1++;
			c1 = data.codePointAt(i1);
		}
		while(c2=='\n')
		{	i2++;
			c2 = data.codePointAt(i2);
		}
		
		while(i1<originalText.length() && i2<data.length())
		{	c1 = originalText.codePointAt(i1);
			c2 = data.codePointAt(i2);
			
			// beginning of a tag
			if(c2=='<')
			{	int k2 = i2;
				i2++; 
				c2 = data.codePointAt(i2);
				
				// closing tag
				if(c2=='/')
				{	int j2 = data.indexOf('>', i2);
					String tag = data.substring(i2+1,j2);
					String tag0 = tags.pop();
					if(!tag.equalsIgnoreCase(tag0))
					{	String msg = StringTools.highlightPosition(i2, data, 20);
						logger.log("WARNING: opening tag ("+tag0+") different from closing tag ("+tag+"):\n"+msg);
					}
					i2 = j2 + 1;
					EntityType type = types.pop();
					int sp1 = startPos1.pop();
//					int sp2 = startPos2.pop();
					if(type!=null)
					{
//						String valueStr = data.substring(sp2,k2);
						String valueStr = originalText.substring(sp1,i1);
						AbstractMention<?> mention = AbstractMention.build(type, sp1, i1, ProcessorName.NERO, valueStr);
						mention.correctMentionSpan(); // to remove some spaces located at the end of mentions
						result.addMention(mention);
					}
				}
				
				// opening tag
				else
				{	int j2 = data.indexOf('>', i2);
					String tag = data.substring(i2,j2);
					i2 = j2 + 1;
					tags.push(tag);
					EntityType type = CONVERSION_MAP.get(tag);
					if(type==null && !IGNORED_TYPES.contains(tag))
					{	if(tag.isEmpty())
						{	int end = Math.min(j2+40, data.length());
							String msg = data.substring(k2, end);
							logger.log("WARNING: found an empty tag, settling for a date ("+msg+"[...])");
							type = EntityType.DATE;
						}
						else
						{	String msg = StringTools.highlightPosition(k2, data, 20);
							throw new ProcessorException("Found an unknown tag : \""+tag+"\" at "+msg);
						}
					}
					types.push(type);
					startPos1.push(i1);
//					startPos2.push(i2);
				}
			}
			
			// other character (than '<')
			else
			{	
//if(c1=='œ') // debug
//	System.out.print("");

				// similar characters
				if(//IGNORED_CHARS.contains((char)c1) || 
						StringTools.compareCharsRelaxed(c1,c2)==0)// || c2==65533)
				{	// everything's normal
					// >> go to next chars in both texts
					i1++; 
					i2++; 
				}
				
				else
				{	boolean moved = false;
				
					// pass all non-letter and non-digit characters
					if(!Character.isLetterOrDigit(c1))//c1==' ' || c1=='\n' || StringTools.isPunctuation(c1))
					{	i1++;
						moved = true;
					}
					
					// pass all non-letter and non-digit characters
					if(!Character.isLetterOrDigit(c2))//c2==' ' || c2=='\n' || StringTools.isPunctuation(c2))
					{	i2++;
						moved = true;
					}
					
					// if both are letters or digits (but different), we have a problem
					if(!moved)
					{	String msg1 = StringTools.highlightPosition(i1, originalText, 20);
						String msg2 = StringTools.highlightPosition(i2, data, 20);
						throw new ProcessorException("Found an untreatable character:\n"+msg1+"\n"+msg2);
					}
				}
			}
		}
		
		// check if we actually processed the whole texts
		if(i1<originalText.length())
		{	
//			// possibly consume the final newline chars
//			do
//			{	c1 = originalText.codePointAt(i1);
//				i1++;
//			}
//			while(i1<originalText.length() && (c1=='\n' || c1==' '));
			
			// possibly consume all non-letter characters
			c1 = originalText.codePointAt(i1);
			while(i1<originalText.length() && !Character.isLetterOrDigit(c1))
			{	i1++;
				if(i1<originalText.length())
					c1 = originalText.codePointAt(i1);
			}
			
			if(i1<originalText.length())
			{	String msg1 = StringTools.highlightPosition(i1, originalText, 20);
				throw new ProcessorException("Didn't reach the end of the original text\n"+msg1);
			}
		}
		else if(i2<data.length())
		{	// possibly consume all non-letter characters
			boolean insideTag = false;
			c2 = data.codePointAt(i2);
			while(i2<data.length() && (!Character.isLetterOrDigit(c2)) || insideTag)
			{	if(c2=='<')
					insideTag = true;
				else if(c2=='>')
					insideTag = false;
				i2++;
				if(i2<data.length())
					c2 = data.codePointAt(i2);
			}
			
			if(i2<data.length())
			{	String msg2 = StringTools.highlightPosition(i2, data, 20);
				throw new ProcessorException("Didn't reach the end of the annotated text\n"+msg2);
			}
		}
		
		return result;
	}
}
