package tr.edu.gsu.nerwip.recognition.internal.modelbased;

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

import java.util.List;

import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalRecognizer;

/**
 * This class is used to represent or implement NER tools invocable 
 * internally, i.e. programmatically, from within Nerwip, and using
 * a model, i.e. some data stored externally as files. Those must
 * be loaded, which implies a specific processing.
 * 
 * @param <T>
 * 		Class of the converter associated to this recognizer.
 * @param <U>
 * 		Class of the internal representation of the entities resulting from the detection.
 * @param <V>
 * 		Class of the model internal representation (generally an enum type).
 * 		 
 * @author Vincent Labatut
 */
public abstract class AbstractModelBasedInternalRecognizer<U,T extends AbstractInternalConverter<U>,V> extends AbstractInternalRecognizer<U,T>
{	
	/**
	 * Builds a new internal recognizer,
	 * using the specified options.
	 * 
	 * @param modelName
	 * 		Model selected for this recognizer. 
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param trim
	 * 		Whether or not the beginings and ends of entities should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be ignored.
	 * @param exclusionOn
	 * 		Whether or not stop words should be ignored.
	 * 
	 * @throws RecognizerException
     * 		Problem while loading the model data.
	 */
	public AbstractModelBasedInternalRecognizer(V modelName, boolean loadModelOnDemand, boolean trim, boolean ignorePronouns, boolean exclusionOn) throws RecognizerException
	{	super(trim,ignorePronouns,exclusionOn);
		
		// init model and supported entity types
		this.loadModelOnDemand = loadModelOnDemand;
		setModelName(modelName);
	}
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void prepareRecognizer() throws RecognizerException
	{	if(!isLoadedModel())
			loadModel();
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITIES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entities recognized by Illinois */
	protected List<EntityType> handledTypes;
	
	@Override
	public List<EntityType> getHandledEntityTypes()
	{	return handledTypes;
	}

	/**
	 * Update the entity types handled
	 * by this recognizer, depanding on
	 * the selected model.
 	 */
	protected abstract void updateHandledEntityTypes();
	
	/////////////////////////////////////////////////////////////////
	// PREDEFINED MODEL 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Object representing the model used for detecting entities */
	protected V modelName;
	/** Indicates if the model should be loaded at initialization or only when needed */
	protected boolean loadModelOnDemand;
	
    /**
     * Changes the predefined model 
     * used for detecting entities.
     * 
     * @param modelName
     * 		Internal representation of the predefined model to use.
     * 
     * @throws RecognizerException
     * 		Problem while loading the model data.
    */
    public void setModelName(V modelName) throws RecognizerException
    {	logger.increaseOffset();
    	
    	// updat emodel name
    	this.modelName = modelName;
    	
		// possibly load model data
		if(loadModelOnDemand)
		{	logger.log("Model will be loaded on demand");
			resetModel();
		}
		else
			loadModel();
	
		// update handled types
		logger.log("Update handled types");
		updateHandledEntityTypes();
		
		logger.decreaseOffset();
	}

    /**
     * Checks whether the model has been
     * already loaded.
     * 
     * @return
     * 		{@code true} iff the model has already been loaded.
    */
	protected abstract boolean isLoadedModel();

	/**
	 * Loads the model data.
	 * 
	 * @throws RecognizerException
     * 		Problem while loading the model data.
	 */
	protected abstract void loadModel() throws RecognizerException;

	/**
	 * Resets the previously loaded model.
	 */
	protected abstract void resetModel();
}
