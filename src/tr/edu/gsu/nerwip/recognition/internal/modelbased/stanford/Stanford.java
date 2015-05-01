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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.AbstractModelBasedInternalRecognizer;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * This class acts as an interface with Stanford Named Entity Recognizer.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code model}: {@link StanfordModelName#CONLLMUC_MODEL CONLLMUC_MODEL} if dates are not needed,
 * 			{@link StanfordModelName#MUC_MODEL MUC_MODEL} if dates are needed.</li>
 * 		<li>{@code ignorePronouns}: {@code false}</li>
 * 		<li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * <br/>
 * Official Stanford website: <a href="http://nlp.stanford.edu/software/CRF-NER.shtml">http://nlp.stanford.edu/software/CRF-NER.shtml</a>
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class Stanford extends AbstractModelBasedInternalRecognizer<List<List<CoreLabel>>, StanfordConverter, StanfordModelName>
{	
	/**
	 * Builds and sets up an object representing
	 * a Stanford NER tool.
	 * 
	 * @param modelName
	 * 		Predefined classifier used for entity detection.
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param ignorePronouns
	 * 		Whether or not prnonouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop words should be excluded from the detection.
	 * 
	 * @throws RecognizerException 
	 * 		Problem while loading the model data.
	 */
	public Stanford(StanfordModelName modelName, boolean loadModelOnDemand, boolean ignorePronouns, boolean exclusionOn) throws RecognizerException
	{	super(modelName,loadModelOnDemand,false,ignorePronouns,exclusionOn);
		
		// init converter
		converter = new StanfordConverter(getFolder());
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public RecognizerName getName()
	{	return RecognizerName.STANFORD;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
		result = result + "_" + "classifier=" + modelName.toString();
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITIES TYPES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void updateHandledEntityTypes()
	{	handledTypes = new ArrayList<EntityType>();
		List<EntityType> temp = modelName.getHandledTypes();
		handledTypes.addAll(temp);
	}

	/////////////////////////////////////////////////////////////////
	// PREDEFINED MODEL 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Cache the classifier, so that we don't have to load it several times */
	private CRFClassifier<CoreLabel> classifier;

    @Override
	protected boolean isLoadedModel()
    {	boolean result = classifier!=null;
    	return result;
    }
    
    @Override
	protected void resetModel()
    {	classifier = null;
    }

	@Override
	protected void loadModel() throws RecognizerException
    {	logger.increaseOffset();
    	
    	// load classifier
		logger.log("Load model");
    	try
    	{	classifier = modelName.loadData();
		}
    	catch (ClassCastException e)
    	{	e.printStackTrace();
    		throw new RecognizerException(e.getMessage());
		}
    	catch (ClassNotFoundException e)
    	{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
    	catch (IOException e)
    	{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
    	    	
    	logger.decreaseOffset();
    }
    
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected List<List<CoreLabel>> detectEntities(Article article) throws RecognizerException
	{	logger.increaseOffset();
		List<List<CoreLabel>> result = null;
		
		// aply to raw text
		String text = article.getRawText();
		result = classifier.classify(text);
		
		logger.decreaseOffset();
		return result;
	}
}
