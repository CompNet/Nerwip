package tr.edu.gsu.nerwip.recognition.internal.modelbased.heideltime;

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
import java.util.Date;
import java.util.List;

import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;
import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ProcessorException;
import tr.edu.gsu.nerwip.recognition.ProcessorName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.AbstractModelBasedInternalProcessor;

/**
 * This class acts as an interface with the HeidelTime tool.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code ignorePronouns}: {@code true}</li>
 * 		<li>{@code exclusionOn}: {@code true}</li>
 * 		TODO
 * </ul>
 * <br/>
 * Note we ignore some of the data output by HeidelTime when
 * converting to Nerwip format. Cf. {@link HeidelTimeConverter}
 * for more details. 
 * <br/>
 * Official HeidelTime website: <a href="https://code.google.com/p/heideltime/">https://code.google.com/p/heideltime/</a>
 * 
 * @author Vincent Labatut
 */
public class HeidelTime extends AbstractModelBasedInternalProcessor<String, HeidelTimeConverter, HeidelTimeModelName>
{	
	/**
	 * Builds and sets up an object representing
	 * an HeidelTime recognizer.
	 * 
	 * @param modelName
	 * 		Predefined mainModel used for mention detection.
	 * @param loadModelOnDemand
	 * 		Whether or not the mainModel should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param doIntervalTagging
	 * 		Whether intervals should be detected or ignored (?). 
	 * 
	 * @throws ProcessorException 
	 * 		Problem while loading the models or tokenizers.
	 */
	public HeidelTime(HeidelTimeModelName modelName, boolean loadModelOnDemand, boolean doIntervalTagging) throws ProcessorException
	{	super(modelName,loadModelOnDemand,false,false,false);
	
		setIgnoreNumbers(false);

		this.doIntervalTagging = doIntervalTagging; //TODO this is actually ignored when loadModelOnDemand is false
		
		// init converter
		converter = new HeidelTimeConverter(getFolder());
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.HEIDELTIME;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
		result = result + "_" + "mainModel=" + modelName.toString();
		result = result + "_" + "intervals=" + doIntervalTagging;
//		result = result + "_" + "ignPro=" + ignorePronouns;
//		result = result + "_" + "exclude=" + exclusionOn;
		
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
	// PARAMETERS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether intervals should be detected or ignored */
	private boolean doIntervalTagging = false;
	
	/////////////////////////////////////////////////////////////////
	// MODELS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Model used by HeidelTime to detect mentions */
	private HeidelTimeStandalone mainModel;
	/** Alternative mainModel, in case we have to deal with news */
	private HeidelTimeStandalone altModel;

    @Override
	protected boolean isLoadedModel()
    {	boolean result = mainModel!=null;
    	return result;
    }
    
    @Override
	protected void resetModel()
    {	mainModel = null;
    	altModel = null;
    }
	
	@Override
	protected void loadModel() throws ProcessorException
	{	logger.increaseOffset();
		
		mainModel = modelName.buildMainTool(doIntervalTagging);
		altModel = modelName.buildAltTool(doIntervalTagging);
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected String detectMentions(Article article) throws ProcessorException
	{	logger.increaseOffset();
		String result = null;
		
		logger.log("Applying HeidelTime to detect dates");
		String text = article.getRawText();
		
		Date date = article.getPublishingDate();
		try
		{	// if HeidelTime needs a reference date
			if(modelName.requiresDate())
			{	if(date!=null)
					result = mainModel.process(text, date);
				else
					result = altModel.process(text);
			}
			
			// if it doesn't need a date
			else
			{	if(date!=null)
					result = mainModel.process(text, date);
				else
					result = mainModel.process(text);
			}
		}
		catch (DocumentCreationTimeMissingException e)
		{	logger.log("ERROR: problem with the date given to HeidelTime ("+date+")");
//			e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		
	    logger.decreaseOffset();
		return result;
	}
}
