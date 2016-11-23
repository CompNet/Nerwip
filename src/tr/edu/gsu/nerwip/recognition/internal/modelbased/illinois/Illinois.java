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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import LBJ2.learn.SparseNetworkLearner;
import LBJ2.parse.LinkedVector;
import edu.illinois.cs.cogcomp.LbjNer.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.LbjNer.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.Data;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.LbjNer.ParsingProcessingData.PlainTextReader;
import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ProcessorException;
import tr.edu.gsu.nerwip.recognition.ProcessorName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.AbstractModelBasedInternalProcessor;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.IllinoisConverter;

/**
 * This class acts as an interface with Illinois Named Entity Tagger.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code model}: {@link IllinoisModelName#CONLL_MODEL CONLL_MODEL} </li>
 * 		<li>{@code ignorePronouns}: {@code false}</li>
 * 		<li>{@code exclusionOn}: {@code true}</li>
 * </ul>
 * <br/>
 * Official Illinois website: <a href="http://cogcomp.cs.illinois.edu/page/software_view/4">http://cogcomp.cs.illinois.edu/page/software_view/4</a>
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class Illinois extends AbstractModelBasedInternalProcessor<Data,IllinoisConverter,IllinoisModelName>
{	
	/**
	 * Builds and sets up an object representing
	 * a Illinois NET tool.
	 * 
	 * @param modelName
	 * 		Predefined model used for mention detection.
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param trim
	 * 		Whether or not the beginings and ends of mentions should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop words should be excluded from the detection.
	 * 
	 * @throws ProcessorException
     * 		Problem while loading the model data.
	 */
	public Illinois(IllinoisModelName modelName, boolean loadModelOnDemand, boolean trim, boolean ignorePronouns, boolean exclusionOn) throws ProcessorException
	{	super(modelName,loadModelOnDemand,trim,ignorePronouns,exclusionOn);
		
		// init converter
		converter = new IllinoisConverter(getFolder());
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.ILLINOIS;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
		result = result + "_" + "model=" + modelName.toString();
		result = result + "_" + "trim=" + trim;
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void updateHandledEntityTypes()
	{	handledTypes = new ArrayList<EntityType>();
		List<EntityType> temp = modelName.getHandledTypes();
		handledTypes.addAll(temp);
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = modelName.canHandleLanguage(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PREDEFINED MODEL 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** First level predefined model, used for NER */
    private NETaggerLevel1 tagger1;
	/** Second level predefined model, used for NER */
    private NETaggerLevel2 tagger2;
    
    @Override
	protected boolean isLoadedModel()
    {	boolean result = tagger1!=null && tagger2!=null;
    	return result;
    }
    
    @Override
	protected void resetModel()
    {	tagger1 = null;
    	tagger2 = null;
    }

	@Override
	protected void loadModel() throws ProcessorException
    {	logger.increaseOffset();
		logger.log("Load model data");
		
    	SparseNetworkLearner[] models;
		try
		{	models = modelName.loadData();
		}
		catch (Exception e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
    	tagger1 = (NETaggerLevel1) models[0];
    	tagger2 = (NETaggerLevel2) models[1];
    	
    	logger.decreaseOffset();
    }
    
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Data detectMentions(Article article) throws ProcessorException
	{	logger.increaseOffset();
		Data result = null;
		String text = article.getRawText();

//		ParametersForLbjCode.currentParameters.logging = false;
		
		Vector<LinkedVector> sentences = PlainTextReader.parseText(text);
    	NERDocument doc = new NERDocument(sentences, "consoleInput");
    	result = new Data(doc);
		
		try
		{	ExpressiveFeaturesAnnotator.annotate(result);
    		Decoder.annotateDataBIO(result,tagger1,tagger2);
		}
		catch(Exception e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		
		logger.decreaseOffset();
		return result;
	}
}
