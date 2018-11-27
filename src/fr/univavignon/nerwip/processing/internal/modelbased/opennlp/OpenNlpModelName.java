package fr.univavignon.nerwip.processing.internal.modelbased.opennlp;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.nerwip.tools.file.NerwipFileNames;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

/**
 * Class representing the predefined model used by OpenNLP for 
 * detecting mentions.
 * <br/>
 * For now, there is one original model, and one trained on
 * our own Wikipedia corpus.
 * 
 * @author Vincent Labatut
 */
public enum OpenNlpModelName
{	
	/** 
	 * Original NLP model.
	 * More of a combination of processing
	 * tools, actually: 
	 * <ul>
	 * 	<li>Date</li>
	 * 	<li>Location</li>
	 * 	<li>Person</li>
	 * 	<li>Organization</li>
	 * </ul> 
	 * Only handles the English language.
	 * TODO check if ignoreNumbers removes year-only dates.
	 */ 
	ORIGINAL_MODEL(
		"Original",
		"en"+File.separator+"en-sent.bin",
		"en"+File.separator+"en-token.bin",
		new HashMap<String, EntityType>()
		{	/** */
			private static final long serialVersionUID = 1L;
			// data
			{	put("en"+File.separator+"en-ner-date.bin", EntityType.DATE);
				put("en"+File.separator+"en-ner-location.bin", EntityType.LOCATION);
				put("en"+File.separator+"en-ner-organization.bin", EntityType.ORGANIZATION);
				put("en"+File.separator+"en-ner-person.bin", EntityType.PERSON);
				put("en"+File.separator+"en-ner-time.bin", EntityType.DATE); //TODO hour? 
			}
		},
		Arrays.asList(
			EntityType.DATE,
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		new HashMap<String, EntityType>(),
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** 
	 * Model trained on our own corpus.
	 * It can handle: 
	 * <ul>
	 * 	<li>Location</li>
	 * 	<li>Person</li>
	 * 	<li>Organization</li>
	 * </ul> 
	 * Only handles the English language. 
	 */ 
	NERWIP_MODEL(
		"Nerwip",
		"en"+File.separator+"en-sent.bin",
		"en"+File.separator+"en-token.bin",
		new HashMap<String, EntityType>()
		{	/** */
			private static final long serialVersionUID = 1L;
			// data
			{	put("en"+File.separator+"en-wp-ner-location.bin", EntityType.LOCATION);
				put("en"+File.separator+"en-wp-ner-organization.bin", EntityType.ORGANIZATION);
				put("en"+File.separator+"en-wp-ner-person.bin", EntityType.PERSON);
			}
		},
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		new HashMap<String, EntityType>(),
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** 
	 * French model trained by <a href="https://sites.google.com/site/nicolashernandez/resources/opennlp">Olivier Grisel</a>.
	 * It can handle: 
	 * <ul>
	 * 	<li>Location</li>
	 * 	<li>Person</li>
	 * 	<li>Organization</li>
	 * </ul> 
	 * Only handles the French language. 
	 */ 
	GRISEL_MODEL(
		"Grisel",
		"fr"+File.separator+"fr-sent.bin",
		"fr"+File.separator+"fr-token.bin", 
		/* Other available resources:
		 * pos   = part of speech tagging
		 * mph   = morphological inflection analysis
		 * chunk = chunking
		 */
		new HashMap<String, EntityType>()
		{	/** */
			private static final long serialVersionUID = 1L;
			// data
			{	put("fr"+File.separator+"fr-ner-location.bin", EntityType.LOCATION);
				put("fr"+File.separator+"fr-ner-organization.bin", EntityType.ORGANIZATION);
				put("fr"+File.separator+"fr-ner-person.bin", EntityType.PERSON);
			}
		},
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		new HashMap<String, EntityType>(),
		Arrays.asList(ArticleLanguage.FR)
	),
	
	/** 
	 * French model from the <a href="https://github.com/opener-project/nerc-fr">Nerc</a>
	 * software.
	 * It can handle: 
	 * <ul>
	 * 	<li>Date</li>
	 * 	<li>Location</li>
	 * 	<li>Person</li>
	 * 	<li>Organization</li>
	 * </ul> 
	 * Only handles the French language. Can also handle Money and Time entities.
	 * <br/>
	 * For this model, it is recommended to set {@code ignoreNumbers} to {@code false},
	 * otherwise it will miss year-only dates.
	 */ 
	NERC_MODEL(
		"Nerc",
		"fr"+File.separator+"fr-sent.bin",
		"fr"+File.separator+"fr-token.bin", 
		new HashMap<String, EntityType>()
		{	/** */
			private static final long serialVersionUID = 1L;
			// data
			{	put("fr"+File.separator+"nerc-fr.bin", null);
			}
		},
		Arrays.asList(
			EntityType.DATE,
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		new HashMap<String, EntityType>()
		{	/** */
			private static final long serialVersionUID = 1L;
			// data
			{	put("date", EntityType.DATE);
				put("location", EntityType.LOCATION);
				put("person", EntityType.PERSON);
				put("organization", EntityType.ORGANIZATION);
//				put("time", EntityType.DATE);
//				put("money", EntityType.MONEY);
			}
		},
		Arrays.asList(ArticleLanguage.FR)
	);
	
	/**
	 * Builds a new value representing
	 * an OpenNLP model.
	 * 
	 * @param name
	 * 		User-friendly name of the model.
	 * @param sentenceDetectorFile 
	 * 		Name of the file containing the sentence detector.
	 * @param tokenizerFile 
	 * 		Name of the file containing the tokenizer.
	 * @param modelFiles 
	 * 		File names of OpenNLP NER models.
	 * @param types
	 * 		List of the entity types handled by the model.
	 * @param typeMap
	 * 		Map of the OpenNlp types into the Nerwip types.
	 * @param languages
	 * 		List of the languages handled by the model.
	 */
	OpenNlpModelName(String name, String sentenceDetectorFile, String tokenizerFile, Map<String,EntityType> modelFiles, List<EntityType> types, Map<String,EntityType> typeMap, List<ArticleLanguage> languages)
	{	this.name = name;
		this.sentenceDetectorFile = sentenceDetectorFile;
		this.tokenizerFile = tokenizerFile;
		this.modelFiles = modelFiles;
		this.types = types;
		this.typeMap = typeMap;
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
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Name of the file containing the sentence detector */
	private String sentenceDetectorFile;
	/** Name of the file containing the tokenizer */
	private String tokenizerFile;
	/** File names of OpenNLP NER models */
	private Map<String,EntityType> modelFiles;
	
	/**
	 * Loads the sentence detector used by the
	 * model represented by this symbol.
	 * 
	 * @return
	 * 		The OpenNLP sentence detector used by this model.
	 * 
	 * @throws IOException
	 * 		Problem while loading the sentence detector. 
	 * @throws InvalidFormatException 
	 * 		Problem while loading the sentence detector.
	 */
	public SentenceDetectorME loadSentenceDetector() throws InvalidFormatException, IOException
	{	String fileName = NerwipFileNames.FO_OPENNLP + File.separator + sentenceDetectorFile;
		File file = new File(fileName);
		
		logger.log("Load sentence detector: "+file.toString());
		SentenceModel sentenceModel;
		sentenceModel = new SentenceModel(file);
		SentenceDetectorME result = new SentenceDetectorME(sentenceModel);
		
		return result;
	}

	/**
	 * Loads the tokenizer used by the
	 * model represented by this symbol.
	 * 
	 * @return
	 * 		The OpenNLP tokenizer used by this model.
	 * 
	 * @throws IOException
	 * 		Problem while loading the tokenizer.
	 * @throws InvalidFormatException 
	 * 		Problem while loading the tokenizer.
	 */
	public TokenizerME loadTokenizer() throws InvalidFormatException, IOException
	{	String fileName = NerwipFileNames.FO_OPENNLP + File.separator + tokenizerFile;
		File file = new File(fileName);
		
		logger.log("Load tokenizer: "+file.toString());
		TokenizerModel model = new TokenizerModel(file);
		TokenizerME result = new TokenizerME(model);
		
		return result;
	}
	
	/**
	 * Loads the map of name finders used by the
	 * model represented by this symbol.
	 * 
	 * @return
	 * 		The OpenNLP name finders used by this model.
	 * 
	 * @throws IOException 
	 * 		Problem while loading the models.
	 * @throws InvalidFormatException
	 * 		Problem while loading the models.
	 */
	public Map<EntityType,List<TokenNameFinder>> loadNerModels() throws InvalidFormatException, IOException
	{	logger.log("Load name finder models");
		logger.increaseOffset();
		
		Map<EntityType,List<TokenNameFinder>> result = new HashMap<EntityType,List<TokenNameFinder>>();
		for(Entry<String,EntityType> entry: modelFiles.entrySet())
		{	String fileName = NerwipFileNames.FO_OPENNLP + File.separator + entry.getKey();
			EntityType type = entry.getValue();
			File file = new File(fileName);
			logger.log("Load model: "+file.toString());
			TokenNameFinder nameFinder;
			if(type==null)
			{	org.vicomtech.opennlp.tools.namefind.TokenNameFinderModel model = new org.vicomtech.opennlp.tools.namefind.TokenNameFinderModel(file);
				nameFinder = new org.vicomtech.opennlp.tools.namefind.NameFinderME(model);
			}
			else
			{	TokenNameFinderModel model = new TokenNameFinderModel(file);
				nameFinder = new NameFinderME(model);
			}
			List<TokenNameFinder> list = result.get(type);
			if(list==null)
			{	list = new ArrayList<TokenNameFinder>();
				result.put(type,list);
			}
			list.add(nameFinder);
		}
		logger.decreaseOffset();
	
		return result;
	}

	/**
	 * Returns the files containing the model objects for
	 * this model name.
	 * <br/>
	 * Note: in this method, we suppose there is only one model
	 * file for each type.
	 * 
	 * @return
	 * 		Map of filename associated to entity types.
	 */
	public Map<EntityType,File> getModelFiles()
	{	Map<EntityType,File> result = new HashMap<EntityType,File>();
		for(Entry<String, EntityType> entry: modelFiles.entrySet())
		{	String key = entry.getKey();
			EntityType value = entry.getValue();
			String fileName = NerwipFileNames.FO_OPENNLP + File.separator + key;
			File file = new File(fileName);
			result.put(value,file);
		}
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types this model can treat */
	private List<EntityType> types;
	/** Map of the OpenNlp types into the Nerwip types */
	private Map<String,EntityType> typeMap;
	
	/**
	 * Returns the list of types
	 * supported by this predefined model.
	 * 
	 * @return
	 * 		A list of supported {@link EntityType}.
	 */
	public List<EntityType> getHandledTypes()
	{	return types;
	}
	
	/**
	 * Converts the specified string to an internal
	 * {@link EntityType}.
	 * 
	 * @param typeStr
	 * 		String code for the OpenNlp type.
	 * @return
	 * 		Corresponding internal {@code EntityType} value.
	 */
	public EntityType convertType(String typeStr)
	{	EntityType result = typeMap.get(typeStr);
		return result;
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
