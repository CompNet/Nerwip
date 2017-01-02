package fr.univavignon.nerwip.processing.internal.modelbased.stanford;

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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * Class representing the predefined
 * models used by Stanford NER 
 * for detecting mentions.
 * 
 * @author Vincent Labatut
 */
public enum StanfordModelName
{	/** 
	 * CoNLL 4 entity types:
	 * <ul>
	 * 		<li>Location</li>
	 * 		<li>Person</li>
	 * 		<li>Organization</li>
	 * 		<li>Misc.</li>
	 * </ul>
	 * Only handles the English language.
	 */
	CONLL_MODEL(
		"Conll",
		"english.conll.4class.distsim.crf.ser.gz",
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
	), 
	
	/** 
	 * MUC 7 entity types 
	 * <ul>
	 * 		<li>Date</li>
	 * 		<li>Location</li>
	 * 		<li>Money</li>
	 * 		<li>Organization</li>
	 * 		<li>Percent</li>
	 * 		<li>Person</li>
	 * 		<li>Time</li>
	 * </ul> 
	 * Only handles the English language.
	 */
	MUC_MODEL(
		"Muc",
		"english.muc.7class.distsim.crf.ser.gz",
		Arrays.asList(
			EntityType.DATE,
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
	), 
	
	/** 
	 * MUC & CoNLL 3 entity types 
	 * <ul>
	 * 		<li>Location</li>
	 * 		<li>Person</li>
	 * 		<li>Organization</li>
	 * </ul> 
	 * Only handles the English language.
	 */
	CONLLMUC_MODEL(
		"ConllMuc",
		"english.all.3class.distsim.crf.ser.gz",
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
	),
	
	/** 
	 * Nerwip 3 entity types 
	 * <ul>
	 * 		<li>Location</li>
	 * 		<li>Person</li>
	 * 		<li>Organization</li>
	 * </ul> 
	 * Only handles the English language.
	 */
	NERWIP_MODEL(
		"Nerwip",
		"english.nerwip.3class.distsim.crf.ser.gz",
		Arrays.asList(
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		),
		Arrays.asList(ArticleLanguage.EN)
	); 
	
	/**
	 * Builds a new value representing
	 * a Stanford model.
	 * 
	 * @param name
	 * 		User-friendly name of the model.
	 * @param fileName
	 * 		Name of the file containing the model.
	 * @param types
	 * 		List of the entity types handled by the model.
	 * @param languages
	 * 		List of the languages handled by the model.
	 */
	StanfordModelName(String name, String fileName, List<EntityType> types, List<ArticleLanguage> languages)
	{	this.name = name;
		this.fileName = fileName;
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
	/** Name of the file containing the model */
	private String fileName;
	
	/**
	 * Returns the filename of the serialized classifier
	 * used by this model.
	 * 
	 * @return
	 * 		Name of the classifier file.
	 */
	public String getModelFile()
	{	String path = FileNames.FO_STANFORD_MODELS + File.separator + fileName;
		return path;
	}
	
	/**
	 * Loads the data corresponding to the
	 * model represented by this symbol.
	 * 
	 * @return
	 * 		An array containing the corresponding model objects.
	 * 
	 * @throws IOException 
	 * 		Problem while loading the model data.
	 * @throws ClassNotFoundException 
	 * 		Problem while loading the model data.
	 * @throws ClassCastException
	 * 		Problem while loading the model data.
	 */
	public CRFClassifier<CoreLabel> loadData() throws ClassCastException, ClassNotFoundException, IOException
	{	String path = getModelFile();
		logger.log("Get the model from file "+path);
		File file = new File(path);
		CRFClassifier<CoreLabel> result = CRFClassifier.getClassifier(file);
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
