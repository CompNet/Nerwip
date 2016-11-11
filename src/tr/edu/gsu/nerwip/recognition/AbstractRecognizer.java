package tr.edu.gsu.nerwip.recognition;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.data.entity.mention.AbstractMention;
import tr.edu.gsu.nerwip.data.entity.mention.Mentions;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.StringTools;

/**
 * This class is used to represent or implement NER tools.
 * The former case corresponds to external tools, i.e. applications
 * executed externally. The latter to tools invocable internally,
 * i.e. programmatically, from within Nerwip. 
 * 		 
 * @author Yasa Akbulut
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public abstract class AbstractRecognizer
{	
	/**
	 * Builds a new recognizer,
	 * using the specified default options.
	 * 
	 * @param trim
	 * 		Whether or not the beginings and ends of mentions should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be ignored.
	 * @param exclusionOn
	 * 		Whether or not stop words should be ignored.
	 */
	public AbstractRecognizer(boolean trim, boolean ignorePronouns, boolean exclusionOn)
	{	this.trim = trim;
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
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Return the (standardized) name of this NER tool.
	 * 
	 * @return 
	 * 		Name of this tool.
	 */
	public abstract RecognizerName getName();

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the folder
	 * containing the results of this
	 * tool.
	 * <br/>
	 * This name takes into account the
	 * name of the tool, but also the parameters
	 * it uses. It can also be used just whenever
	 * a string representation of the tool and its
	 * parameters is needed.
	 * 
	 * @return 
	 * 		Name of the appropriate folder.
	 */
	public abstract String getFolder();

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of entity types this NER tool
	 * can detect if it is trained for.
	 * 
	 * @return 
	 * 		A list of entity types.
	 */
	public abstract List<EntityType> getHandledMentionTypes();
	
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
	// CACHING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not cache should be used */
	protected boolean cache = true;
	
	/**
	 * Indicates whether or not caching is
	 * enabled for this NER tool.
	 *  
	 * @return
	 * 		{@code true} iff caching is enabled.
	 */
	public boolean doesCache()
	{	return cache;
	}
	
	/**
	 * Changes the cache flag. If {@code true}, the {@link #process(Article) process}
	 * method will first check if the results already
	 * exist as a file. In this case, they will be loaded
	 * from this file. Otherwise, the process will be
	 * conducted normally, then recorded.
	 * 
	 * @param enabled
	 * 		If {@code true}, the (possibly) cached files are used.
	 */
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
	}

	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Applies this NER tool to the specified article,
	 * and returns a list of the detected mentions.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		List of the resulting mentions.
	 * 
	 * @throws RecognizerException
	 * 		Problem while applying the NER tool. 
	 */
	public abstract Mentions process(Article article) throws RecognizerException;

	/////////////////////////////////////////////////////////////////
	// FILTERING NOISE 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not stop words should be ignored */
	protected boolean exclusionOn = true;
	/** Whether or not pronouns should be ignored */
	protected boolean ignorePronouns = true;
	/** Whether or not numbers should be ignored */
	protected boolean ignoreNumbers = true;
	
	/** Lists of forbidden words, used for filtering mentions */	
	private static final Map<ArticleLanguage,List<String>> EXCLUSION_LISTS = new HashMap<ArticleLanguage,List<String>>();
	/** List of pronouns, used for filtering mentions */	
	private static final Map<ArticleLanguage,List<String>> PRONOUN_LISTS = new HashMap<ArticleLanguage,List<String>>();
	
	/**
	 * Loads a set of language-dependant list of words.
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
	 * 
	 * @param exclusionOn
	 * 		If {@code true}, stop words are ignored.
	*/
	public void setExclusionOn(boolean exclusionOn)
	{	this.exclusionOn = exclusionOn;
	}
	
	/**
	 * Whether or not stop words should be ignored.
	 * 
	 * @return
	 * 		{@code true} iff stop words are ignored.
	 */
	public boolean isExclusionOn()
	{	return exclusionOn;
	}
	
	/**
	 * Determines if a string represents a stop-word.
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
	 * 
	 * @param ignorePronouns
	 * 		If {@code true}, pronouns are ignored.
	 */
	public void setIgnorePronouns(boolean ignorePronouns)
	{	this.ignorePronouns = ignorePronouns;
	}
	
	/**
	 * Whether or not pronouns should be ignored.
	 * 
	 * @return
	 * 		{@code true} iff pronouns are ignored.
	 */
	public boolean isIgnorePronouns()
	{	return ignorePronouns;
	}

	/**
	 * Determines if a string represents
	 * a pronoun.
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
	 * Disables/enables the removal of purely
	 * numerical mentions.
	 * 
	 * @param ignoreNumbers
	 * 		If {@code true}, numbers are ignored.
	 */
	public void setIgnoreNumbers(boolean ignoreNumbers)
	{	this.ignoreNumbers = ignoreNumbers;
	}
	
	/**
	 * Whether or not purely numerical mentions
	 * should be ignored.
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
    /** Whether or not the beginings and ends of mentions should be cleaned from any non-letter/digit chars */
    protected boolean trim = false;

    /**
	 * Some NER tools let punctuation/space at the begining/end of the mention. This
	 * function trims the mention to remove this noise.
	 * <br/>
	 * If the consecutive trimmings remove all characters from the mention, then
	 * this method returns {@code false}, and {@code true} otherwise (non-empty
	 * string for the mention).
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
	 * Not all NER tools need this process. In fact,
	 * most don't!
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
    /** Whether or not the NER tool can output (spatially) overlapping mentions */
    protected boolean noOverlap = true;

    /**
	 * Checks if the specified text position
	 * is already a part of an existing mention.
	 * 
	 * @param pos
	 * 		Position to be checked
	 * @param mentions
	 * 		The mentions already built.
	 * @return
	 * 		{@code true} iff the position is already included in an mention.
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
//	public boolean positionAlreadyUsed(MentionDate mention, List<AbstractMention<?>> mentions)
//	{	boolean result = false;
//		
//		Iterator<AbstractMention<?>> it = mentions.iterator();
//		while(!result && it.hasNext())
//		{	AbstractMention<?> temp = it.next();
//			result = temp.overlapsWith(mention);
//		}
//		
//		return result;
//	}
	
	/**
	 * Checks whether a part of the specified mention was already detected as another
	 * mention. Returns the concerned mention.
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
	 * Gets an Mentions object and detects the overlapping mentions.
	 * Only keeps the longest ones amongst them. This method uses
	 * {@link #filterRedundancy(List)}.
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

	/** Whether or not to write the raw results in a text file (for debug purposes) */
	protected boolean outRawResults = false;
	
	/**
	 * Changes the flag regarding the outputting of the recognizer
	 * raw results (i.e. before conversion to our format) in a text file.
	 * Useful for debugging, but it takes space. By default, this is disabled.
	 * <br/>
	 * Note that for external tools, this file generally must be produced,
	 * since it is used for communicating with the external tool. In this
	 * case, if this option is disabled, the file is deleted when not needed
	 * anymore.
	 * 
	 * @param enabled
	 * 		{@code true} to output a text file.
	 */
	public void setOutputRawResults(boolean enabled)
	{	this.outRawResults = enabled;
	}
	
	/////////////////////////////////////////////////////////////////
	// STRING		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	RecognizerName name = getName();
		String result = name.toString();
		return result;
	}
}
