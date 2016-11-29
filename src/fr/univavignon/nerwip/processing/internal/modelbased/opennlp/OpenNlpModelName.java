package fr.univavignon.nerwip.processing.internal.modelbased.opennlp;

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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import opennlp.tools.namefind.NameFinderME;
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
	 */ 
	ORIGINAL_MODEL(
		"Original",
		"en-sent.bin",
		"en-token.bin",
		new HashMap<String, EntityType>()
		{	/** */
			private static final long serialVersionUID = 1L;
			// data
			{	put("en-ner-date.bin", EntityType.DATE);
				put("en-ner-location.bin", EntityType.LOCATION);
				put("en-ner-organization.bin", EntityType.ORGANIZATION);
				put("en-ner-person.bin", EntityType.PERSON);
				put("en-ner-time.bin", EntityType.DATE); //TODO hour? 
			}
		},
		Arrays.asList(
			EntityType.DATE,
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
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
		"en-sent.bin",
		"en-token.bin",
		new HashMap<String, EntityType>()
		{	/** */
			private static final long serialVersionUID = 1L;
			// data
			{	put("en-wp-ner-location.bin", EntityType.LOCATION);
				put("en-wp-ner-organization.bin", EntityType.ORGANIZATION);
				put("en-wp-ner-person.bin", EntityType.PERSON);
			}
		},
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
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
	 * @param languages
	 * 		List of the languages handled by the model.
	 */
	OpenNlpModelName(String name, String sentenceDetectorFile, String tokenizerFile, Map<String,EntityType> modelFiles, List<EntityType> types, List<ArticleLanguage> languages)
	{	this.name = name;
		this.sentenceDetectorFile = sentenceDetectorFile;
		this.tokenizerFile = tokenizerFile;
		this.modelFiles = modelFiles;
		this.types = types;
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
	{	String fileName = FileNames.FO_OPENNLP + File.separator + sentenceDetectorFile;
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
	{	String fileName = FileNames.FO_OPENNLP + File.separator + tokenizerFile;
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
	public Map<NameFinderME, EntityType> loadNerModels() throws InvalidFormatException, IOException
	{	logger.log("Load name finder models");
		logger.increaseOffset();
		
		Map<NameFinderME, EntityType> result = new HashMap<NameFinderME,EntityType>();
		for(Entry<String,EntityType> entry: modelFiles.entrySet())
		{	String fileName = FileNames.FO_OPENNLP + File.separator + entry.getKey();
			EntityType type = entry.getValue();
			File file = new File(fileName);
			logger.log("Load model: "+file.toString());
			TokenNameFinderModel model = new TokenNameFinderModel(file);
			NameFinderME nameFinder = new NameFinderME(model);
			result.put(nameFinder,type);
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
			String fileName = FileNames.FO_OPENNLP + File.separator + key;
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
