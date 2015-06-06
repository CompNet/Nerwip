package tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe;

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

import com.aliasi.chunk.Chunker;
import com.aliasi.dict.ApproxDictionaryChunker;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * Class representing the method used
 * by LinkPipe for detecting entities.
 * 
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public enum LingPipeModelName
{	/** Uses an already trained model */
	PREDEFINED_MODEL(
		"PredefinedModel",
		"ne-en-news-muc6.AbstractCharLmRescoringChunker",
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** Our own model, trained on our own corpus */
	NERWIP_MODEL(
		"NerwipModel",
		"ne-en-wp-nerwip.AbstractCharLmRescoringChunker",
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** Uses dictionaries with exact match */
	EXACT_DICTIONARY(
		"ExactDictionary",
		null,
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN) //TODO could be extended to French by translating the dictionaries
	),

	/** Uses dictionaries with approximate match */
	APPROX_DICTIONARY(
		"ApproxDictionary",
		null,
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN) //TODO could be extended to French by translating the dictionaries
	);
	
	/**
	 * Builds a new value representing
	 * a detection method.
	 * 
	 * @param name
	 * 		User-friendly name of the detection method.
	 * @param modelFile
	 * 		Name of the file containing the model.
	 * @param types
	 * 		List of the entity types handled by the model.
	 * @param languages
	 * 		List of the languages handled by the model.
	 */
	LingPipeModelName(String name, String modelFile, List<EntityType> types, List<ArticleLanguage> languages)
	{	this.name = name;
		this.modelFile = modelFile;
		this.types = types;
		this.languages = languages;
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** User-friendly name of the chunking method */
	private String name;
	
	@Override
	public String toString()
	{	return name;
	}

	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** File containing the model data (not always defined) */
	private String modelFile;
	
	/**
	 * Returns the file containing the data
	 * defining this model.
	 * 
	 * @return
	 * 		File containing the model.
	 */
	public File getModelFile()
	{	String modelPath = FileNames.FO_LINGPIPE + File.separator + modelFile;
		File result = new File(modelPath);
		return result;
	}
	
	/**
	 * Loads the data corresponding to the
	 * LingPipe model represented by this symbol.
	 * 
	 * @return
	 * 		An array containing the corresponding model objects.
	 * 
	 * @throws IOException 
	 * 		Problem while loading the model data.
	 * @throws ClassNotFoundException 
	 * 		Problem while loading the model data.
	 */
	public Chunker loadData() throws ClassNotFoundException, IOException
	{	Chunker result = null;
		boolean caseSensitive = false;
		
		switch(this)
		{	case PREDEFINED_MODEL:
			case NERWIP_MODEL:
				logger.log("Get the chunker based on a predefined model");
				String modelPath = FileNames.FO_LINGPIPE + File.separator + modelFile;
				File modelFile = new File(modelPath);
				logger.log("Reading chunker from file: " + modelFile);
				result = (Chunker) AbstractExternalizable.readObject(modelFile);
				break;
	
			case EXACT_DICTIONARY:
				{	logger.log("Get the dictionary-based chunker with exact matching");
					MapDictionary<String> dictionary = new MapDictionary<String>();
					for(EntityType type: types)
					{	String typeStr = type.toString().toLowerCase(Locale.ENGLISH);
						String filePath = FileNames.FO_CUSTOM_LISTS + File.separator + typeStr + "s" + FileNames.EX_TEXT;
						File file = new File(filePath);
						Scanner scanner = new Scanner(file);
						while(scanner.hasNext())
						{	String item = scanner.nextLine();
							DictionaryEntry<String> entry = new DictionaryEntry<String>(item,type.toString(),1.0);
							dictionary.addEntry(entry);
						}
						scanner.close();
					}
					result = new ExactDictionaryChunker(dictionary,IndoEuropeanTokenizerFactory.INSTANCE,true,caseSensitive);
				}
			break;
			
			case APPROX_DICTIONARY:
				{	logger.log("Get the dictionary-based chunker with approximate matching");
					TrieDictionary<String> dictionary = new TrieDictionary<String>();
					for(EntityType type: types)
					{	String typeStr = type.toString().toLowerCase(Locale.ENGLISH);
						String filePath = FileNames.FO_CUSTOM_LISTS + File.separator + typeStr + "s" + FileNames.EX_TEXT;
						File file = new File(filePath);
						Scanner scanner = new Scanner(file);
						while (scanner.hasNext())
						{	String item = scanner.nextLine();
							DictionaryEntry<String> entry = new DictionaryEntry<String>(item,type.toString(),1.0);
							dictionary.addEntry(entry);
						}
						scanner.close();
					}
					WeightedEditDistance editDistance = new FixedWeightEditDistance(0,-1,-1,-1,Double.NaN);
					result = new ApproxDictionaryChunker(dictionary,IndoEuropeanTokenizerFactory.INSTANCE,editDistance,2);
				}
				break;
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
