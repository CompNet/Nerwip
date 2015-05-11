package tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois;

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
import java.util.Arrays;
import java.util.List;

import LBJ2.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.Parameters;

import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import LBJ2.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.Parameters;

/**
 * Class representing the predefined
 * model used by Illinois for 
 * detecting entities.
 * 
 * @author Vincent Labatut
 */
public enum IllinoisModelName
{	/** 
	 * CoNLL 4 entity types: 
	 * <ul>
	 * 	<li>Location</li>
	 * 	<li>Person</li>
	 * 	<li>Organization</li>
	 * 	<li>Misc.</li>
	 * </ul> 
	 */ 
	CONLL_MODEL(
		"Conll",
		"ner.conll.config",
		"finalSystemBILOU.model",
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** 
	 * Ontonotes 18 entity types: 
	 * <ul>
	 * 	<li>Cardinal</li>
	 * 	<li>Date</li>
	 * 	<li>Event</li>
	 * 	<li>Fac</li>
	 * 	<li>GPE</li>
	 * 	<li>Language</li>
	 * 	<li>Law</li>
	 * 	<li>Location</li>
	 * 	<li>Money</li>
	 * 	<li>NORP</li>
	 * 	<li>Ordinal</li>
	 * 	<li>Organization</li>
	 * 	<li>Percent</li>
	 * 	<li>Person</li>
	 * 	<li>Procuct</li>
	 * 	<li>Quantity</li>
	 * 	<li>Time</li>
	 * 	<li>WorkOfArt</li>
	 * </ul> 
	 */
	ONTONOTES_MODEL(
		"Ontonotes",
		"ner.ontonotes.config",
		"Ontonotes.model",
		Arrays.asList(
			EntityType.DATE,
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** 
	 * Model trained on Nerwip corpus,
	 * using the 3 default entity types: 
	 * <ul>
	 * 	<li>Location</li>
	 * 	<li>Person</li>
	 * 	<li>Organization</li>
	 * </ul> 
	 */ 
	NERWIP_MODEL(
		"Nerwip",
		"ner.nerwip.config",
		"nerwip.model",
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
	);
	
	/**
	 * Builds a new value representing
	 * an Illinois model.
	 * 
	 * @param name
	 * 		User-friendly name of the model.
	 * @param configFile
	 * 		Name of the configuration file.
	 * @param modelFile
	 * 		Name of the files containing the model.
	 * @param types
	 * 		List of the entity types handled by the model.
	 * @param languages
	 * 		List of the languages handled by the model.
	 */
	IllinoisModelName(String name, String configFile, String modelFile, List<EntityType> types, List<ArticleLanguage> languages)
	{	this.name = name;
		this.configFile = configFile;
		this.modelFile = modelFile;
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
	/** Name of the configuration file */
	private String configFile;
	/** Name of the files containing the model */
	private String modelFile;
	
	/**
	 * Returns the file prefix for this model.
	 * 
	 * @return
	 * 		A file prefix (all files constituting the model starts like this).
	 */
	public String getModelFilePrefix()
	{	return modelFile;
	}
	
	/**
	 * Loads the data corresponding to the
	 * model represented by this symbol.
	 * 
	 * @return
	 * 		An array containing the corresponding model objects.
	 * 
	 * @throws Exception 
	 * 		Problem while loading the model data.
	 */
	public SparseNetworkLearner[] loadData() throws Exception
	{	// first read the configuration file
		loadConfig();
		
		// then load the models
		SparseNetworkLearner result[] = new SparseNetworkLearner[2];
		{	String path = FileNames.FO_ILLINOIS_MODELS + File.separator + modelFile;
			logger.log("Read the model files associated to the selected model ("+this+") "+path);
			result[0] = new NETaggerLevel1(path+".level1",path+".level1.lex");
			result[1] = new NETaggerLevel2(path+".level2",path+".level2.lex");
		}
		
		return result;
	}
	
	/**
	 * Reads only the configuration file, and initializes
	 * the corresponding Illinois object.
	 * 
	 * @throws Exception
	 * 		Problem while loading the configuration file.
	 */
	public void loadConfig() throws Exception
	{	String path = FileNames.FO_ILLINOIS_CONFIGS + File.separator + configFile;
		logger.log("Read the configuration file associated to the selected model ("+this+") "+path);
		Parameters.readConfigAndLoadExternalData(path);
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
