package fr.univavignon.nerwip.processing;

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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * TODO 
 * 		 
 * @author Yasa Akbulut
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public abstract class AbstractDelegateRecognizer
{	
	/**
	 * Builds a new delegate recognizer,
	 * using the specified default options.
	 * 
	 * @param recognizer
	 * 		Recognizer associated to this delegate.
	 * @param trim
	 * 		Whether or not the beginings and ends of mentions should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be ignored.
	 * @param exclusionOn
	 * 		Whether or not stop words should be ignored.
	 */
	public AbstractDelegateRecognizer(InterfaceRecognizer recognizer, boolean trim, boolean ignorePronouns, boolean exclusionOn)
	{	this.recognizer = recognizer;
		
		this.trim = trim;
		this.ignorePronouns = ignorePronouns;
		this.exclusionOn = exclusionOn;
		
		this.noOverlap = true;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// RECOGNIZER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Recognizer associated to this delegate */
	protected InterfaceRecognizer recognizer;
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the folder containing the results of this
	 * delegate recognizer.
	 * <br/>
	 * This name takes into account the name of the tool, but also 
	 * the parameters it uses. It can also be used just whenever a 
	 * string representation of the tool and its parameters is needed.
	 * 
	 * @return 
	 * 		Name of the appropriate folder.
	 */
	public abstract String getFolder();
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the types of entities this recognizer
	 * can handle with its current model/parameters.
	 * 
	 * @return 
	 * 		A list of entity types.
	 */
	public abstract List<EntityType> getHandledEntityTypes();
	
	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks whether the specified language is supported by this
	 * recognizer, given its current settings (parameters, model...).
	 * 
	 * @param language
	 * 		The language to be checked.
	 * @return 
	 * 		{@code true} iff this recognizer supports the specified
	 * 		language, with its current parameters (model, etc.).
	 */
	public abstract boolean canHandleLanguage(ArticleLanguage language);
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Applies this processor to the specified article,
	 * in order to recognize entity mentions.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		List of the recognized mentions.
	 * 
	 * @throws ProcessorException
	 * 		Problem while recognizing the mentions. 
	 */
	public abstract Mentions delegateRecognize(Article article) throws ProcessorException;
	
	/////////////////////////////////////////////////////////////////
	// FILTERING NOISE 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not stop words should be ignored during recognition */
	protected boolean exclusionOn = true;
	/** Whether or not pronouns should be ignored during recognition */
	protected boolean ignorePronouns = true;
	/** Whether or not numbers should be ignored during recognition */
	protected boolean ignoreNumbers = true;
	
	/** Lists of forbidden words, used for filtering mentions during recognition */	
	private static final Map<ArticleLanguage,List<String>> EXCLUSION_LISTS = new HashMap<ArticleLanguage,List<String>>();
	/** List of pronouns, used for filtering mentions during recognition */	
	private static final Map<ArticleLanguage,List<String>> PRONOUN_LISTS = new HashMap<ArticleLanguage,List<String>>();
	
	/**
	 * Loads a set of language-dependant list of words.
	 * This is only used during recognition.
	 * 
	 * @param prefix
	 * 		Prefix of the filename (will be completed with language name).
	 * @param map
	 * 		Map to populate.
	 */
	private static void loadLanguageList(String prefix, Map<ArticleLanguage,List<String>> map)
	{	for(ArticleLanguage language: ArticleLanguage.values())
		{	logger.log("Treating language "+language);
			// set up file path
			String path = FileNames.FO_CUSTOM_LISTS + File.separator + prefix + language.toString() + FileNames.EX_TEXT;
			File file = new File(path);
			
			// retrieve values
			List<String> list = new ArrayList<String>();
			map.put(language, list);
			try
			{	Scanner scanner = FileTools.openTextFileRead(file, "UTF-8");
				while(scanner.hasNextLine())
				{	String line = scanner.nextLine().trim();
					if(!line.isEmpty())
						list.add(line);
				}
				scanner.close();
			}
			catch (FileNotFoundException e)
			{	e.printStackTrace();
			}
			catch (UnsupportedEncodingException e)
			{	e.printStackTrace();
			}
		}
	}
	
	/**
	 * Loads the exclusion lists, i.e. the
	 * lists of stop-words, for each language.
	 * This is only used during recognition.
	 */
	private static void loadExclusionList()
	{	logger.log("Loading exclusion lists");
		logger.increaseOffset();
		
		loadLanguageList(FileNames.PRE_EXCLUDED, EXCLUSION_LISTS);
		
		logger.decreaseOffset();
		logger.log("Loading complete");
	}
	
	/**
	 * Enables/disables the removal of stop words.
	 * This is only used during recognition.
	 * 
	 * @param exclusionOn
	 * 		If {@code true}, stop words are ignored.
	*/
	public void setExclusionOn(boolean exclusionOn)
	{	this.exclusionOn = exclusionOn;
	}
	
	/**
	 * Whether or not stop words should be ignored.
	 * This is only used during recognition.
	 * 
	 * @return
	 * 		{@code true} iff stop words are ignored.
	 */
	public boolean isExclusionOn()
	{	return exclusionOn;
	}
	
	/**
	 * Determines if a string represents a stop-word.
	 * This is only used during recognition.
	 * 
	 * @param text
	 * 		String to check.
	 * @param language
	 * 		Language of the article currently processed. 
	 * @return
	 * 		{@code true} iff the string is a stop-word.
	 */
	private boolean isExcluded(String text, ArticleLanguage language)
	{	boolean result = false;
		
		if(EXCLUSION_LISTS.isEmpty())
			loadExclusionList();
		
		List<String> list = EXCLUSION_LISTS.get(language);
		Iterator<String> it = list.iterator();
		while(it.hasNext() && !result)
		{	String word = it.next();
			result = word.equalsIgnoreCase(text);
		}
		
		return result;
	}

	/**
	 * Loads the pronouns lists for each language.
	 * This is only used during recognition.
	 */
	private static void loadPronounList()
	{	logger.log("Loading pronoun lists");
		logger.increaseOffset();
		
		loadLanguageList(FileNames.PRE_PRONOUNS, PRONOUN_LISTS);
		
		logger.decreaseOffset();
		logger.log("Loading complete");
	}
	
	/**
	 * Enables/disables the removal of pronouns.
	 * This is only used during recognition.
	 * 
	 * @param ignorePronouns
	 * 		If {@code true}, pronouns are ignored.
	 */
	public void setIgnorePronouns(boolean ignorePronouns)
	{	this.ignorePronouns = ignorePronouns;
	}
	
	/**
	 * Whether or not pronouns should be ignored.
	 * This is only used during recognition.
	 * 
	 * @return
	 * 		{@code true} iff pronouns are ignored.
	 */
	public boolean isIgnorePronouns()
	{	return ignorePronouns;
	}

	/**
	 * Determines if a string represents a pronoun.
	 * This is only used during recognition.
	 * 
	 * @param text
	 * 		String to check.
	 * @param language
	 * 		Language of the article currently processed. 
	 * @return
	 * 		{@code true} iff the string is a pronoun.
	 */
	private boolean isPronoun(String text, ArticleLanguage language)
	{	boolean result = false;
	
		if(PRONOUN_LISTS.isEmpty())
			loadPronounList();
	
		List<String> list = PRONOUN_LISTS.get(language);
		Iterator<String> it = list.iterator();
		
		while(it.hasNext() && !result)
		{	String pronoun = it.next();
			result = pronoun.equalsIgnoreCase(text);
		}
		
		return result;
	}

	/**
	 * Disables/enables the removal of purely numerical mentions.
	 * This is only used during recognition.
	 * 
	 * @param ignoreNumbers
	 * 		If {@code true}, numbers are ignored.
	 */
	public void setIgnoreNumbers(boolean ignoreNumbers)
	{	this.ignoreNumbers = ignoreNumbers;
	}
	
	/**
	 * Whether or not purely numerical mentions should be ignored.
	 * This is only used during recognition.
	 * 
	 * @return
	 * 		{@code true} iff numbers are ignored.
	 */
	public boolean isIgnoreNumbers()
	{	return ignoreNumbers;
	}

	/**
	 * Gets a list of mentions and removes some of them,
	 * considered as noise depending on the current options: 
	 * stop-words, pronouns, numerical expressions, etc.
	 * This is only used during recognition.
	 * 
	 * @param mentions
	 * 		List to be filtered.
	 * @param language
	 * 		Language of the article currently processed. 
	 */
	protected void filterNoise(Mentions mentions, ArticleLanguage language)
	{	logger.increaseOffset();
	
		List<AbstractMention<?>> mentionList = mentions.getMentions();
		Iterator<AbstractMention<?>> it = mentionList.iterator();
		while(it.hasNext())
		{	AbstractMention<?> mention = it.next();
			String mentionStr = mention.getStringValue();
			
			// is it a stop-word?
			if(exclusionOn && isExcluded(mentionStr,language))
			{	logger.log("Mention '"+mentionStr+"' is a stop-word >> filtered.)");
				it.remove();
			}
			
			// is it a pronoun?
			else if(ignorePronouns && (mentionStr.length()<=1 || isPronoun(mentionStr,language)))
			{	logger.log("Mention '"+mentionStr+"' is a pronoun >> filtered.)");
				it.remove();
			}
			
			// is it a pure number?
			else if(ignoreNumbers && StringTools.hasNoLetter(mentionStr))
			{	logger.log("Mention '"+mentionStr+"' is a number (no letter) >> filtered.)");
				it.remove();
			}
		}
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// CLEANING		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Whether or not the beginings and ends of mentions should be cleaned from any non-letter/digit chars (used only during recognition) */
    protected boolean trim = false;

    /**
	 * Some recognizers let punctuation/space at the begining/end of the mention. This
	 * function trims the mention to remove this noise.
	 * <br/>
	 * If the consecutive trimmings remove all characters from the mention, then
	 * this method returns {@code false}, and {@code true} otherwise (non-empty
	 * string for the mention).
	 * <br/>
	 * This is only used during recognition.
	 * 
	 * @param mention
	 * 		Mention to be processed.
	 * @return
	 * 		{@code true} iff the mention is not empty after the trimming.
	 */
	public boolean cleanMentionEnds(AbstractMention<?> mention)
	{	String valueStr = mention.getStringValue();
		char c;
		
		// trim beginning
		int startPos = mention.getStartPos();
		c = valueStr.charAt(0);
		while(!valueStr.isEmpty() && !Character.isLetterOrDigit(c))
		{	startPos++;
			valueStr = valueStr.substring(1,valueStr.length());
			if(!valueStr.isEmpty())
				c = valueStr.charAt(0);
		}
		
		// trim ending
		int endPos = mention.getEndPos();
		if(!valueStr.isEmpty())
		{	c = valueStr.charAt(valueStr.length()-1);
			while(!valueStr.isEmpty() && !Character.isLetterOrDigit(c))
			{	endPos--;
				valueStr = valueStr.substring(0,valueStr.length()-1);
				if(!valueStr.isEmpty())
					c = valueStr.charAt(valueStr.length()-1);
			}
		}
		
		mention.setStringValue(valueStr);
		mention.setStartPos(startPos);
		mention.setEndPos(endPos);
		
		boolean result = !valueStr.isEmpty();
		return result;
	}

	/**
	 * Gets a list of mentions and cleans them by
	 * removing unappropriate characters possibly
	 * located at the beginning-end. Unappropriate
	 * means here neither characters nor letters.
	 * <br/>
	 * This is only used during recognition.
	 * Not all recognizers need this process. In fact, most don't!
	 * 
	 * @param mentions
	 * 		List to be cleaned.
	 */
	protected void cleanMentions(Mentions mentions)
	{	logger.increaseOffset();
		
		if(trim)
		{	logger.log("Start trimming");
			
			logger.increaseOffset();
			List<AbstractMention<?>> mentionList = mentions.getMentions();
			Iterator<AbstractMention<?>> it = mentionList.iterator();
			while(it.hasNext())
			{	AbstractMention<?> mention = it.next();
				String str = mention.getStringValue();
				if(str.isEmpty())
				{	it.remove();
					logger.log("WARNING: Mention "+mention+" was empty before trimming >> removed");
				}
				else 
				{	boolean temp = cleanMentionEnds(mention);
					if(!temp)
					{	it.remove();
						logger.log("Mention "+mention+" was empty after trimming >> removed");
					}
				}
			}
			logger.decreaseOffset();
		}
		else
			logger.log("No trimming.");
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// MENTIONS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Whether or not the recognizer can output (spatially) overlapping mentions (used only during recognition) */
    protected boolean noOverlap = true;

    /**
	 * Checks if the specified text position
	 * is already a part of an existing mention.
	 * This is only used during recognition.
	 * 
	 * @param pos
	 * 		Position to be checked
	 * @param mentions
	 * 		The mentions already built.
	 * @return
	 * 		{@code true} iff the position is already included in a mention.
	 */
	protected boolean positionAlreadyUsed(int pos, List<AbstractMention<?>> mentions)
	{	boolean result = false;
		
		Iterator<AbstractMention<?>> it = mentions.iterator();
		while(it.hasNext() && !result)
		{	AbstractMention<?> mention = it.next();
			int startPos = mention.getStartPos();
			int endPos = mention.getEndPos();
			result = pos>=startPos && pos<=endPos;
		}
		
		return result;
	}

//	/**
//	 * Checks whether a part of the specified
//	 * mention was already detected as another
//	 * mention.
//	 * This is only used during recognition.
//	 * 
//	 * @param mention
//	 * 		Newly detected mention.
//	 * @param mentions
//	 * 		List of mentions already detected.
//	 * @return
//	 * 		{@code true} iff the mention is actually new.
//	 * 
//	 * @author Vincent Labatut
//	 */
//	public boolean positionAlreadyUsed(MentionDate mention, List<AbstractEntity<?>> mentions)
//	{	boolean result = false;
//		
//		Iterator<AbstractEntity<?>> it = mentions.iterator();
//		while(!result && it.hasNext())
//		{	AbstractEntity<?> temp = it.next();
//			result = temp.overlapsWith(mention);
//		}
//		
//		return result;
//	}
	
	/**
	 * Checks whether a part of the specified mention was already detected as another
	 * mention. Returns the concerned mention.
	 * This is only used during recognition.
	 * 
	 * @param mention
	 * 		Newly detected mention.
	 * @param mentions
	 * 		List of mentions already detected.
	 * @return
	 * 		Mention intersecting the specified one,
	 * 		or {@code null} if none does.
	 */
	public AbstractMention<?> positionAlreadyUsed(AbstractMention<?> mention, List<AbstractMention<?>> mentions)
	{	AbstractMention<?> result = null;
		
		Iterator<AbstractMention<?>> it = mentions.iterator();
		while(result==null && it.hasNext())
		{	AbstractMention<?> temp = it.next();
			if(temp.overlapsWith(mention))
				result = temp;
		}
		
		return result;
	}
	
	/**
	 * Gets a Mentions object and detects the overlapping mentions.
	 * Only keeps the longest ones amongst them. This method uses
	 * {@link #filterRedundancy(List)}.
	 * This is only used during recognition.
	 * 
	 * @param mentions
	 * 		List to be filtered.
	 */
	protected void filterRedundancy(Mentions mentions)
	{	List<AbstractMention<?>> mentionList = mentions.getMentions();
		filterRedundancy(mentionList);
	}
	
	/**
	 * Gets a list of mentions and detects the overlapping ones.
	 * Only keeps the longest ones amongst them.
	 * This is only used during recognition.
	 * 
	 * @param mentions
	 * 		List to be filtered.
	 */
	protected void filterRedundancy(List<AbstractMention<?>> mentions)
	{	logger.increaseOffset();
	
		if(!noOverlap)
			logger.log("Overlapping mentions are allowed.)");
		else
		{	List<AbstractMention<?>> temp = new ArrayList<AbstractMention<?>>(mentions);
			mentions.clear();
			
			for(AbstractMention<?> mention1: temp)
			{	AbstractMention<?> mention2 = positionAlreadyUsed(mention1, mentions);
				boolean pass = false;
				while(!pass && mention2!=null)
				{	// process both mention lengths
					int length1 = mention1.getEndPos() - mention1.getStartPos();
					int length2 = mention2.getEndPos() - mention2.getStartPos();
					// keep the longest one
					if(length1>length2)
					{	logger.log("New mention "+mention1+" intersects with old mention "+mention2+" >> keep the new one");
						mentions.remove(mention2);
					}
					else
					{	logger.log("New mention "+mention1+" intersects with old mention "+mention2+" >> keep the old one");
						pass = true;
					}
					// check next overlapping mention
					mention2 = positionAlreadyUsed(mention1, mentions);
				}
				
				if(!pass)
					mentions.add(mention1);
			}
		}
		
		logger.decreaseOffset();
	}

	/////////////////////////////////////////////////////////////////
	// XML FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the XML file associated to the specified
	 * article, and representing the recognized mentions.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		A {@code File} object representing the associated XML result file.
	 */
	public File getXmlFile(Article article)
	{	String resultsFolder = article.getFolderPath();
		String recognizerFolder = getFolder();
		resultsFolder = resultsFolder + File.separator + recognizerFolder;
		String filePath = resultsFolder + File.separator + FileNames.FI_MENTION_LIST;
		
		File result = new File(filePath);
		return result;
	}
	
	/**
	 * Write the XML results obtained for the specified article.
	 * This method is meant for both internal and external tools.
	 * 
	 * @param article
	 * 		Concerned article.
	 * @param mentions
	 * 		List of the detected mentions.
	 * @throws IOException
	 * 		Problem while writing the file.
	 */
	public void writeXmlResults(Article article, Mentions mentions) throws IOException
	{	// data file
		File file = getXmlFile(article);
		
		// check folder
		File folder = file.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		mentions.writeToXml(file);
	}
	
	/**
	 * Read the XML representation of the results
	 * previously processed by the associated recognizer,
	 * for the specified article.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		The list of mentions stored in the file.
	 * 
	 * @throws SAXException
	 * 		Problem while reading the file.
	 * @throws IOException
	 * 		Problem while reading the file.
	 * @throws ParseException 
	 * 		Problem while parsing a date. 
	 */
	public Mentions readXmlResults(Article article) throws SAXException, IOException, ParseException
	{	File dataFile = getXmlFile(article);
		
		Mentions result = Mentions.readFromXml(dataFile);
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the raw result file associated to the specified
	 * article, i.e. the file possibly generated externally
	 * by the recognizer.
	 * <br/>
	 * Nothing to do with the raw <i>text</i> of the article,
	 * i.e. its plain textual content.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		A {@code File} object representing the associated raw result file.
	 */
	public File getRawFile(Article article)
	{	String resultsFolder = article.getFolderPath();
		String recognizerFolder = getFolder();
		resultsFolder = resultsFolder + File.separator + recognizerFolder;
		String filePath = resultsFolder + File.separator + FileNames.FI_OUTPUT_TEXT;
	
		File result = new File(filePath);
		return result;
	}
	
	/**
	 * Tries to delete the file containing the raw results.
	 * Returns a boolean indicating success ({@code true})
	 * or failure ({@code false}).
	 * 
	 * @param article
	 * 		Concerned article.
	 * @return
	 * 		{@code true} iff the file could be deleted.
	 */
	public boolean deleteRawFile(Article article)
	{	boolean result = false;
		File rawFile = getRawFile(article);
		if(rawFile!=null && rawFile.exists() && rawFile.isFile())
			result = rawFile.delete();
		return result;
	}
}
