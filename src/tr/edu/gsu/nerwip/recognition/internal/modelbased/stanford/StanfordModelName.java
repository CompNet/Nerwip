package tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford;

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
import java.util.List;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * Class representing the predefined
 * models used by Stanford NER 
 * for detecting entities.
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
	 */
	CONLL_MODEL("Conll","english.conll.4class.distsim.crf.ser.gz"), 
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
	 */
	MUC_MODEL("Muc","english.muc.7class.distsim.crf.ser.gz"), 
	/** 
	 * MUC & CoNLL 3 entity types 
	 * <ul>
	 * 		<li>Location</li>
	 * 		<li>Person</li>
	 * 		<li>Organization</li>
	 * </ul> 
	 */
	CONLLMUC_MODEL("ConllMuc","english.all.3class.distsim.crf.ser.gz"),
	/** 
	 * Nerwip 3 entity types 
	 * <ul>
	 * 		<li>Location</li>
	 * 		<li>Person</li>
	 * 		<li>Organization</li>
	 * </ul> 
	 */
	NERWIP_MODEL("Nerwip","english.nerwip.3class.distsim.crf.ser.gz"); 
	
	/**
	 * Builds a new value representing
	 * a Stanford model.
	 * 
	 * @param name
	 * 		User-friendly name of the model.
	 * @param fileName
	 * 		Name of the file containing the model.
	 */
	StanfordModelName(String name, String fileName)
	{	this.name = name;
		this.fileName = fileName;
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
		{	case CONLL_MODEL:
        		result.add(EntityType.LOCATION);
        		result.add(EntityType.ORGANIZATION);
        		result.add(EntityType.PERSON);
				break;
			case MUC_MODEL:
				result.add(EntityType.DATE);
	    		result.add(EntityType.LOCATION);
	    		result.add(EntityType.ORGANIZATION);
	    		result.add(EntityType.PERSON);
				break;
			case CONLLMUC_MODEL:
	    		result.add(EntityType.LOCATION);
	    		result.add(EntityType.ORGANIZATION);
	    		result.add(EntityType.PERSON);
				break;
			case NERWIP_MODEL:
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
