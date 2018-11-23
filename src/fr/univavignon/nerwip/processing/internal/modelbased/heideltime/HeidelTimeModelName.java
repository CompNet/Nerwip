package fr.univavignon.nerwip.processing.internal.modelbased.heideltime;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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
import java.util.Arrays;
import java.util.List;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * Class representing the predefined model used by HeidelTime for 
 * detecting mentions.
 * 
 * @author Vincent Labatut
 */
public enum HeidelTimeModelName
{	
	/** 
	 * Processes English non-standard documents, such as 
	 * Tweets or SMS.
	 * <br/>
	 * Like for all other models, HeidelTime is designed
	 * to detect only temporal enities.   
	 * <ul>
	 * 	<li>Date</li>
	 * </ul> 
	 */ 
	ENGLISH_COLLOQUIAL(
		"EnColloquial",
		Language.ENGLISHCOLL,
		DocumentType.COLLOQUIAL,
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** 
	 * Processes English documents with a <i>narrative</i>
	 * style, such as Wikipedia articles.
	 * <br/>
	 * Like for all other models, HeidelTime is designed
	 * to detect only temporal enities.   
	 * <ul>
	 * 	<li>Date</li>
	 * </ul> 
	 */ 
	ENGLISH_NARRATIVES(
		"EnNarratives",
		Language.ENGLISH,
		DocumentType.NARRATIVES,
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** 
	 * Processes English <i>news</i>, for which
	 * <b>it is necessary to specify a reference date</b>
	 * in the {@code HeidelTime} class.
	 * <br/>
	 * Like for all other models, HeidelTime is designed
	 * to detect only temporal enities.   
	 * <ul>
	 * 	<li>Date</li>
	 * </ul> 
	 */ 
	ENGLISH_NEWS(
		"EnNews",
		Language.ENGLISH,
		DocumentType.NEWS,
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** 
	 * Processes English scientific texts, i.e. texts with
	 * a local time frame.
	 * <br/>
	 * Like for all other models, HeidelTime is designed
	 * to detect only temporal enities.   
	 * <ul>
	 * 	<li>Date</li>
	 * </ul> 
	 */ 
	ENGLISH_SCIENTIFIC(
		"EnScientific",
		Language.ENGLISHSCI,
		DocumentType.SCIENTIFIC,
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** 
	 * Processes French documents with a <i>narrative</i>
	 * style, such as Wikipedia articles.
	 * <br/>
	 * Like for all other models, HeidelTime is designed
	 * to detect only temporal enities.   
	 * <ul>
	 * 	<li>Date</li>
	 * </ul> 
	 */ 
	FRENCH_NARRATIVES(
		"FrNarratives",
		Language.FRENCH,
		DocumentType.NARRATIVES,
		Arrays.asList(ArticleLanguage.FR)
	),
	
	/** 
	 * Processes French <i>news</i>, for which
	 * <b>it is necessary to specify a reference date</b>
	 * in the {@code HeidelTime} class.
	 * <br/>
	 * Like for all other models, HeidelTime is designed
	 * to detect only temporal enities.   
	 * <ul>
	 * 	<li>Date</li>
	 * </ul> 
	 */ 
	FRENCH_NEWS(
		"FrNews",
		Language.FRENCH,
		DocumentType.NEWS,
		Arrays.asList(ArticleLanguage.FR)
	);

	/**
	 * Builds a new value representing
	 * an OpenNLP model.
	 * 
	 * @param name
	 * 		User-friendly name of the model.
	 * @param language 
	 * 		Language parameter for HeidelTime.
	 * @param documentType
	 * 		Type of input document parameter for HeidelTime.
	 * @param languages
	 * 		List of the languages handled by the model.
	 */
	HeidelTimeModelName(String name, Language language, DocumentType documentType, List<ArticleLanguage> languages)
	{	this.name = name;
		this.language = language;
		this.documentType = documentType;
		this.languages = languages;
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** User-friendly name of the model */
	private String name;
	
	@Override
	public String toString()
	{	return name;
	}
	
	/////////////////////////////////////////////////////////////////
	// PARAMETERS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Language parameter used when building a HeidelTime object */
	private Language language;
	/** Document type parameter used when building a HeidelTime object */
	private DocumentType documentType;
	
	/**
	 * Indicates if this model requires the specification of a reference
	 * date when seaching for temporal mentions.
	 * 
	 * @return
	 * 		{@code true} iff this model needs a reference date.
	 */
	public boolean requiresDate()
	{	boolean result = this==ENGLISH_NEWS || this==FRENCH_NEWS;
		return result;
	}
	
	/**
	 * Returns the alternative model for when
	 * the main model requires a date and does
	 * not get one.
	 * 
	 * @return
	 * 		Alternative model (to {@code this}).
	 */
	public HeidelTimeModelName getAltModel()
	{	HeidelTimeModelName result = null;
		switch(this)
		{	case ENGLISH_NEWS:
				result = ENGLISH_NARRATIVES;
				break;
			case FRENCH_NEWS:
				result = FRENCH_NARRATIVES;
				break;
		}
		
		return result;
	}
	
	
	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Name of the configuration file */
	private final static String CONFIG_FILE = FileNames.FO_HEIDELTIME + File.separator + "config.props";
	
	/**
	 * Returns the object to use for detecting dates.
	 * 
	 * @param doIntervalTagging
	 * 		Whether intervals should be detected or ignored (?). 
	 * @return
	 * 		Instance of the HeidelTime recognizer.
	 */
	public HeidelTimeStandalone buildMainTool(boolean doIntervalTagging)
	{	OutputType outputType = OutputType.TIMEML;
		POSTagger posTagger = POSTagger.TREETAGGER;
		logger.log("Building the appropriate HeidelTime instance");
		HeidelTimeStandalone result = new HeidelTimeStandalone(language, documentType, outputType, CONFIG_FILE, posTagger, doIntervalTagging);
		
		return result;
	}
	
	/**
	 * Returns the object to use for detecting dates,
	 * in case the main object is not applicable.
	 * 
	 * @param doIntervalTagging
	 * 		Whether intervals should be detected or ignored (?). 
	 * @return
	 * 		Instance of the HeidelTime recognizer.
	 */
	public HeidelTimeStandalone buildAltTool(boolean doIntervalTagging)
	{	HeidelTimeStandalone result = null;
		
		HeidelTimeModelName altModel = getAltModel();
		if(altModel!=null)
			result = altModel.buildMainTool(doIntervalTagging);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types this model can treat */
	private static final List<EntityType> TYPES = Arrays.asList(EntityType.DATE);
	
	/**
	 * Returns the list of types
	 * supported by this predefined model.
	 * 
	 * @return
	 * 		A list of supported {@link EntityType}.
	 */
	public List<EntityType> getHandledTypes()
	{	return TYPES;
	}
	
	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this model can treat */
	private List<ArticleLanguage> languages;
	
	/**
	 * Checks whether the specified language is supported by this  model.
	 * 
	 * @param language
	 * 		The language to be checked.
	 * @return 
	 * 		{@code true} iff this model supports the specified language.
	 */
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = languages.contains(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
}
