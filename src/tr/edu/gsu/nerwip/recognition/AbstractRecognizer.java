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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
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
	 * 		Whether or not the beginings and ends of entities should be 
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
	// ENTITIES TYPES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of entity types this NER tool
	 * can detect if it is trained for.
	 * 
	 * @return 
	 * 		A list of entity types.
	 */
	public abstract List<EntityType> getHandledEntityTypes();
	
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
	 * and returns a list of the detected entities.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		List of the resulting entities.
	 * 
	 * @throws RecognizerException
	 * 		Problem while applying the NER tool. 
	 */
	public abstract Entities process(Article article) throws RecognizerException;

	/////////////////////////////////////////////////////////////////
	// FILTERING NOISE 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not stop words should be ignored */
	protected boolean exclusionOn = true;
	/** Whether or not pronouns should be ignored */
	protected boolean ignorePronouns = true;
	/** Whether or not numbers should be ignored */
	protected boolean ignoreNumbers = true;
	
	/** List of forbidden words, used for filtering entities */	
	private static final List<String> EXCLUSION_LIST = Arrays.asList(
		"a", "an",
		"i", "in",
		"the"
	);
	
	/**
	 * Enables/disables the removal of stop words 
	 * should be ignored.
	 * 
	 * @param exclusionOn
	 * 		If {@code true}, stop words are ignored.
	*/
	public void setExclusionOn(boolean exclusionOn)
	{	this.exclusionOn = exclusionOn;
	}
	
	/**
	 * Whether or not stop words 
	 * should be ignored.
	 * 
	 * @return
	 * 		{@code true} iff stop words are ignored.
	 */
	public boolean isExclusionOn()
	{	return exclusionOn;
	}
	
	/**
	 * Determines if a string represents
	 * a stop-word.
	 * 
	 * @param text
	 * 		String to check.
	 * @return
	 * 		{@code true} iff the string is a stop-word.
	 */
	private boolean isExcluded(String text)
	{	boolean result = false;
		Iterator<String> it = EXCLUSION_LIST.iterator();
		
		while(it.hasNext() && !result)
		{	String word = it.next();
			result = word.equalsIgnoreCase(text);
		}
		
		return result;
	}

	/**
	 * Enables/disables the removal of pronouns
	 * should be ignored.
	 * 
	 * @param ignorePronouns
	 * 		If {@code true}, pronouns are ignored.
	 */
	public void setIgnorePronouns(boolean ignorePronouns)
	{	this.ignorePronouns = ignorePronouns;
	}
	
	/**
	 * Whether or not pronouns
	 * should be ignored.
	 * 
	 * @return
	 * 		{@code true} iff pronouns are ignored.
	 */
	public boolean isIgnorePronouns()
	{	return ignorePronouns;
	}

	/** List of pronouns, used for filtering entities */	
	private static final List<String> PRONOUN_LIST = Arrays.asList(
		"i", "me", "my", "mine",
		"you", "your", "yours",
		"he", "him", "himself", "his",
		"she", "her", "herself", "hers", 
		"it", "its",
		"they", "them", "their"
	);
	
	/**
	 * Determines if a string represents
	 * a pronoun.
	 * 
	 * @param text
	 * 		String to check.
	 * @return
	 * 		{@code true} iff the string is a pronoun.
	 */
	private boolean isPronoun(String text)
	{	boolean result = false;
		Iterator<String> it = PRONOUN_LIST.iterator();
		
		while(it.hasNext() && !result)
		{	String pronoun = it.next();
			result = pronoun.equalsIgnoreCase(text);
		}
		
		return result;
	}

	/**
	 * Disables/enables the removal of purely
	 * numerical entities.
	 * 
	 * @param ignoreNumbers
	 * 		If {@code true}, numbers are ignored.
	 */
	public void setIgnoreNumbers(boolean ignoreNumbers)
	{	this.ignoreNumbers = ignoreNumbers;
	}
	
	/**
	 * Whether or not purely numerical entities
	 * should be ignored.
	 * 
	 * @return
	 * 		{@code true} iff numbers are ignored.
	 */
	public boolean isIgnoreNumbers()
	{	return ignoreNumbers;
	}

	/**
	 * Gets a list of entities and removes some of them,
	 * considered as noise depending on the current options: 
	 * stop-words, pronouns, numerical expressions, etc.
	 * 
	 * @param entities
	 * 		List to be filtered.
	 */
	protected void filterNoise(Entities entities)
	{	logger.increaseOffset();
	
		List<AbstractEntity<?>> entityList = entities.getEntities();
		Iterator<AbstractEntity<?>> it = entityList.iterator();
		while(it.hasNext())
		{	AbstractEntity<?> entity = it.next();
			String entityStr = entity.getStringValue();
			
			// is it a stop-word?
			if(exclusionOn && isExcluded(entityStr))
			{	logger.log("Entity '"+entityStr+"' is a stop-word >> filtered.)");
				it.remove();
			}
			
			// is it a pronoun?
			else if(ignorePronouns && (entityStr.length()<=1 || isPronoun(entityStr)))
			{	logger.log("Entity '"+entityStr+"' is a pronoun >> filtered.)");
				it.remove();
			}
			
			// is it a pure number?
			else if(ignoreNumbers && StringTools.hasNoLetter(entityStr))
			{	logger.log("Entity '"+entityStr+"' is a number (no letter) >> filtered.)");
				it.remove();
			}
		}
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// CLEANING		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Whether or not the beginings and ends of entities should be cleaned from any non-letter/digit chars */
    protected boolean trim = false;

    /**
	 * Some NER tools let punctuation/space at 
	 * the begining/end of the entity. This
	 * function appropriately trims the entity
	 * to remove this noise.
	 * <br/>
	 * If the consecutive trimmings remove
	 * all characters from the entity, then
	 * this method returns {@code false},
	 * and {@code true} otherwise (non-empty
	 * string for the entity).
	 * 
	 * @param entity
	 * 		Entity to be processed.
	 * @return
	 * 		{@code true} if the entity survived the trimming.
	 */
	public boolean cleanEntityEnds(AbstractEntity<?> entity)
	{	String valueStr = entity.getStringValue();
		char c;
		
		// trim beginning
		int startPos = entity.getStartPos();
		c = valueStr.charAt(0);
		while(!valueStr.isEmpty() && !Character.isLetterOrDigit(c))
		{	startPos++;
			valueStr = valueStr.substring(1,valueStr.length());
			if(!valueStr.isEmpty())
				c = valueStr.charAt(0);
		}
		
		// trim ending
		int endPos = entity.getEndPos();
		if(!valueStr.isEmpty())
		{	c = valueStr.charAt(valueStr.length()-1);
			while(!valueStr.isEmpty() && !Character.isLetterOrDigit(c))
			{	endPos--;
				valueStr = valueStr.substring(0,valueStr.length()-1);
				if(!valueStr.isEmpty())
					c = valueStr.charAt(valueStr.length()-1);
			}
		}
		
		entity.setStringValue(valueStr);
		entity.setStartPos(startPos);
		entity.setEndPos(endPos);
		
		boolean result = !valueStr.isEmpty();
		return result;
	}

	/**
	 * Gets a list of entities and cleans them by
	 * removing unappropriate characters possibly
	 * located at the beginning-end. Unappropriate
	 * means here neither characters nor letters.
	 * <br/>
	 * Not all NER tools need this process. In fact,
	 * most don't!
	 * 
	 * @param entities
	 * 		List to be cleaned.
	 */
	protected void cleanEntities(Entities entities)
	{	logger.increaseOffset();
		
		if(trim)
		{	logger.log("Start trimming");
			
			logger.increaseOffset();
			List<AbstractEntity<?>> entityList = entities.getEntities();
			Iterator<AbstractEntity<?>> it = entityList.iterator();
			while(it.hasNext())
			{	AbstractEntity<?> entity = it.next();
				String str = entity.toString();
				boolean temp = cleanEntityEnds(entity);
				if(!temp)
				{	it.remove();
					logger.log("Entity "+str+" was empty after trimming >> removed");
				}
			}
			logger.decreaseOffset();
		}
		else
			logger.log("No trimming.");
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITIES		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Whether or not the NER tool can output (spatially) overlapping entities */
    protected boolean noOverlap = true;

    /**
	 * Checks if the specified text position
	 * is already a part of an existing entity.
	 * 
	 * @param pos
	 * 		Position to be checked
	 * @param entities
	 * 		The entities already built.
	 * @return
	 * 		{@code true} iff the position is already included in an entity.
	 */
	protected boolean positionAlreadyUsed(int pos, List<AbstractEntity<?>> entities)
	{	boolean result = false;
		
		Iterator<AbstractEntity<?>> it = entities.iterator();
		while(it.hasNext() && !result)
		{	AbstractEntity<?> entity = it.next();
			int startPos = entity.getStartPos();
			int endPos = entity.getEndPos();
			result = pos>=startPos && pos<=endPos;
		}
		
		return result;
	}

//	/**
//	 * Checks whether a part of the specified
//	 * entity was already detected as another
//	 * entity.
//	 * 
//	 * @param entity
//	 * 		Newly detected entity.
//	 * @param entities
//	 * 		List of entities already detected.
//	 * @return
//	 * 		{@code true} iff the entity is actually new.
//	 * 
//	 * @author Vincent Labatut
//	 */
//	public boolean positionAlreadyUsed(EntityDate entity, List<AbstractEntity<?>> entities)
//	{	boolean result = false;
//		
//		Iterator<AbstractEntity<?>> it = entities.iterator();
//		while(!result && it.hasNext())
//		{	AbstractEntity<?> temp = it.next();
//			result = temp.overlapsWith(entity);
//		}
//		
//		return result;
//	}
	
	/**
	 * Checks whether a part of the specified entity was already detected as another
	 * entity. Returns the concerned entity.
	 * 
	 * @param entity
	 * 		Newly detected entity.
	 * @param entities
	 * 		List of entities already detected.
	 * @return
	 * 		Entity intersecting the specified one,
	 * 		or {@code null} if none does.
	 */
	public AbstractEntity<?> positionAlreadyUsed(AbstractEntity<?> entity, List<AbstractEntity<?>> entities)
	{	AbstractEntity<?> result = null;
		
		Iterator<AbstractEntity<?>> it = entities.iterator();
		while(result==null && it.hasNext())
		{	AbstractEntity<?> temp = it.next();
			if(temp.overlapsWith(entity))
				result = temp;
		}
		
		return result;
	}
	
	/**
	 * Gets an Entities object and detects the overlapping entities.
	 * Only keeps the longest ones amongst them. This method uses
	 * {@link #filterRedundancy(List)}.
	 * 
	 * @param entities
	 * 		List to be filtered.
	 */
	protected void filterRedundancy(Entities entities)
	{	List<AbstractEntity<?>> entityList = entities.getEntities();
		filterRedundancy(entityList);
	}
	
	/**
	 * Gets a list of entities and detects the overlapping ones.
	 * Only keeps the longest ones amongst them.
	 * 
	 * @param entities
	 * 		List to be filtered.
	 */
	protected void filterRedundancy(List<AbstractEntity<?>> entities)
	{	logger.increaseOffset();
	
		if(!noOverlap)
			logger.log("Overlapping entities are allowed.)");
		else
		{	List<AbstractEntity<?>> temp = new ArrayList<AbstractEntity<?>>(entities);
			entities.clear();
			
			for(AbstractEntity<?> entity1: temp)
			{	AbstractEntity<?> entity2 = positionAlreadyUsed(entity1, entities);
				boolean pass = false;
				while(!pass && entity2!=null)
				{	// process both entity lengths
					int length1 = entity1.getEndPos() - entity1.getStartPos();
					int length2 = entity2.getEndPos() - entity2.getStartPos();
					// keep the longest one
					if(length1>length2)
					{	logger.log("New entity "+entity1+" intersects with old entity "+entity2+" >> keep the new one");
						entities.remove(entity2);
					}
					else
					{	logger.log("New entity "+entity1+" intersects with old entity "+entity2+" >> keep the old one");
						pass = true;
					}
					// check next overlapping entity
					entity2 = positionAlreadyUsed(entity1, entities);
				}
				
				if(!pass)
					entities.add(entity1);
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
