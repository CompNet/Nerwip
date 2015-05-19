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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;
import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.AbstractModelBasedInternalRecognizer;

/**
 * This class acts as an interface with the Apache OpenNLP tool.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code ignorePronouns}: {@code true}</li>
 * 		<li>{@code exclusionOn}: {@code true}</li>
 * </ul>
 * <br/>
 * Official OpenNLP website: <a href="http://opennlp.apache.org/">http://opennlp.apache.org/</a>
 * 
 * @author Vincent Labatut
 */
public class OpenNlp extends AbstractModelBasedInternalRecognizer<Map<EntityType,List<Span>>, OpenNlpConverter, OpenNlpModelName>
{	
	/**
	 * Builds and sets up an object representing
	 * an OpenNLP NER tool.
	 * 
	 * @param modelName
	 * 		Predefined model used for entity detection.
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop words should be excluded from the detection.
	 * 
	 * @throws RecognizerException 
	 * 		Problem while loading the models or tokenizers.
	 */
	public OpenNlp(OpenNlpModelName modelName, boolean loadModelOnDemand, boolean ignorePronouns, boolean exclusionOn) throws RecognizerException
	{	super(modelName,loadModelOnDemand,false,ignorePronouns,exclusionOn);
	
		// init converter
		converter = new OpenNlpConverter(getFolder());
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public RecognizerName getName()
	{	return RecognizerName.OPENNLP;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
		result = result + "_" + "model=" + modelName.toString();
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
	// MODELS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Object used to split text into sentences */
	private SentenceDetectorME sentenceDetector;
	/** Object used to split sentences into words */
	private Tokenizer tokenizer;
	/** Models used by OpenNLP to detect entities */
	private Map<NameFinderME,EntityType> models;

    @Override
	protected boolean isLoadedModel()
    {	boolean result = sentenceDetector!=null && tokenizer!=null && models!=null;
    	return result;
    }
    
    @Override
	protected void resetModel()
    {	sentenceDetector = null;
    	tokenizer = null;
    	models = null;
    }
	
	@Override
	protected void loadModel() throws RecognizerException
	{	logger.increaseOffset();
		
		try
		{	// load secondary objects	
			sentenceDetector = modelName.loadSentenceDetector();
			tokenizer = modelName.loadTokenizer();
			
			// load main models
			models = modelName.loadNerModels();
		} 
		catch (InvalidFormatException e)
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
	/** Order in which the entities should be processed */
	private static final List<EntityType> TYPE_PRIORITIES = Arrays.asList(EntityType.ORGANIZATION,EntityType.PERSON,EntityType.LOCATION,EntityType.DATE);

	@Override
	protected Map<EntityType,List<Span>> detectEntities(Article article) throws RecognizerException
	{	logger.increaseOffset();
		Map<EntityType,List<Span>> result = new HashMap<EntityType, List<Span>>();
		
		// split sentences
		logger.log("Process each sentence");
		String text = article.getRawText();
		Span sentenceSpans[] = sentenceDetector.sentPosDetect(text);
		for(Span sentenceSpan: sentenceSpans)
		{	int startSentPos = sentenceSpan.getStart();
			int endSentPos = sentenceSpan.getEnd();
			String sentence = text.substring(startSentPos,endSentPos);
			
			// split tokens
			String tokens[] = tokenizer.tokenize(sentence);
			Span tokenSpans[] = tokenizer.tokenizePos(sentence);
			
			// apply each model according to the type priorities
//			logger.log("Process the models for each entity type");
			for(EntityType type: TYPE_PRIORITIES)
			{	List<Span> list = result.get(type);
				if(list==null)
				{	list = new ArrayList<Span>();
					result.put(type,list);
				}
				
				for(Entry<NameFinderME,EntityType> entry: models.entrySet())
				{	EntityType t = entry.getValue();
					if(type==t)
					{	// detect entities
						NameFinderME model = entry.getKey();
						Span nameSpans[] = model.find(tokens);
						
						// add them to result list
						for(Span span: nameSpans)
						{	// get start position
							int first = span.getStart();
							Span firstSpan = tokenSpans[first];
							int startPos = firstSpan.getStart() + startSentPos;
							// get end position
							int last = span.getEnd() - 1;
							Span lastSpan = tokenSpans[last];
							int endPos = lastSpan.getEnd() + startSentPos;
							// build new span
							String typeStr = span.getType(); 
							Span temp = new Span(startPos, endPos, typeStr);
//System.out.println(span.toString());
//System.out.println(text.substring(temp.getStart(),temp.getEnd()));
							// add to list
							list.add(temp);
						}
						
						// reset model for next document
						model.clearAdaptiveData();
					}
				}
			}
		}
		
	    logger.decreaseOffset();
		return result;
	}
}
