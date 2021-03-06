package tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * Class representing the predefined
 * model used by OpenNLP for 
 * detecting entities.
 * <br/>
 * For now, there is only one such model.
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
		}
	),
	
	/** 
	 * Model trained on our own corpus.
	 * It can handle: 
	 * <ul>
	 * 	<li>Location</li>
	 * 	<li>Person</li>
	 * 	<li>Organization</li>
	 * </ul> 
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
		}
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
	 */
	OpenNlpModelName(String name, String sentenceDetectorFile, String tokenizerFile, Map<String,EntityType> modelFiles)
	{	this.name = name;
		this.sentenceDetectorFile = sentenceDetectorFile;
		this.tokenizerFile = tokenizerFile;
		this.modelFiles = modelFiles;
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
	/**
	 * Returns the list of types
	 * supported by this predefined model.
	 * 
	 * @return
	 * 		A list of supported {@link EntityType}.
	 */
	public List<EntityType> getHandledTypes()
	{	List<EntityType> result = new ArrayList<EntityType>();
		
		switch(this)
		{	case NERWIP_MODEL:
	    		result.add(EntityType.LOCATION);
	    		result.add(EntityType.ORGANIZATION);
	    		result.add(EntityType.PERSON);
				break;
			case ORIGINAL_MODEL:
				result.add(EntityType.DATE);
				result.add(EntityType.LOCATION);
				result.add(EntityType.ORGANIZATION);
				result.add(EntityType.PERSON);
			break;				
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
}
