package fr.univavignon.nerwip.processing.internal.modelbased.illinois;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import LBJ2.learn.SparseNetworkLearner;
import LBJ2.parse.LinkedVector;
import edu.illinois.cs.cogcomp.LbjNer.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.LbjNer.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.Data;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.LbjNer.ParsingProcessingData.PlainTextReader;
import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelbased.AbstractModelbasedInternalDelegateRecognizer;

/**
 * This class acts as a delegate for the mention recognition with 
 * Illinois Named Entity Tagger.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code model}: {@link IllinoisModelName#CONLL_MODEL CONLL_MODEL} </li>
 * 		<li>{@code ignorePronouns}: {@code false}</li>
 * 		<li>{@code exclusionOn}: {@code true}</li>
 * </ul>
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
class IllinoisDelegateRecognizer extends AbstractModelbasedInternalDelegateRecognizer<Data, IllinoisModelName>
{	
	/**
	 * Builds and sets up an object representing
	 * a Illinois NET tool.
	 * 
	 * @param illinois
	 * 		Recognizer in charge of this delegate.
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
	public IllinoisDelegateRecognizer(Illinois illinois, IllinoisModelName modelName, boolean loadModelOnDemand, boolean trim, boolean ignorePronouns, boolean exclusionOn) throws ProcessorException
	{	super(illinois,modelName,loadModelOnDemand,trim,ignorePronouns,true,exclusionOn);
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
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

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of Illinois type to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("DATE", EntityType.DATE);
		CONVERSION_MAP.put("LOC", EntityType.LOCATION);
		CONVERSION_MAP.put("ORG", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("PER", EntityType.PERSON);
	}
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, Data data) throws ProcessorException
	{	ProcessorName recognizerName = recognizer.getName();
		ArticleLanguage language = article.getLanguage();
		Mentions result = new Mentions(recognizerName);
	
		String text = article.getRawText();
		int position = 0;
		Vector<NERDocument> documents = data.documents;
		// processing each Illinois document separately
	    for(NERDocument document: documents)
	    {	// an Illinois document is a list of sentences
	    	Vector<LinkedVector> sentences = document.sentences;
	    	
	    	// processing Illinois sentence separately
	        for(LinkedVector sentence: sentences)
	        {	boolean open = false;
	            
	        	// initializing structures to represent each Illinois word in the sentence
	            String[] predictions = new String[sentence.size()];
	            String[] words = new String[sentence.size()];
	            for(int j=0;j<sentence.size();j++)
	            {	NEWord word = (NEWord)sentence.get(j);
	            	predictions[j] = word.neTypeLevel2;
	            	words[j] = word.form;
	            }
	            
	            // identifying series of Illinois words corresponding to (single) mentions
	            EntityType type = null;
	            int startPos = -1;
	            for(int j=0;j<sentence.size();j++)
	            {	// look for the word in the original text
	            	int temp = position;
	            	position = text.indexOf(words[j], temp);
    				if(position==-1)
    					throw new ProcessorException("Cannot find \""+words[j]+"\" in the text, from position "+temp);
	            	
    				// possibly start a new mention if we find a B- marker
	            	if (predictions[j].startsWith("B-")
	            			// of if the previous word is internal, but the previous word was of a different type
	            			|| (j>0 && predictions[j].startsWith("I-") && (!predictions[j-1].endsWith(predictions[j].substring(2)))))
	            	{	String typeStr = predictions[j].substring(2);
	            		type = CONVERSION_MAP.get(typeStr);
	            		if(type!=null)
	            		{	open = true;
	            			startPos = position;
	            		}
	            	}
	            	
	            	// updating current position in the original text
	            	position = position + words[j].length();
	            	
	            	// complete the current mention (which possibly contains several words)
	            	if(open)
	            	{	boolean close = false;
	            		if(j==sentence.size()-1)
	            		{	close = true;
	            		}
	            		else
	            		{	if(predictions[j+1].startsWith("B-"))
	            				close = true;
	            			else if(predictions[j+1].equals("O"))
	            				close = true;
	            			else if(predictions[j+1].indexOf('-')>-1 && (!predictions[j].endsWith(predictions[j+1].substring(2))))
	            				close = true;
	            		}
	            		if(close)
	            		{	// consider all detected words to constitute the mention
	            			String valueStr = text.substring(startPos,position);
	            			AbstractMention<?> mention = AbstractMention.build(type, startPos, position, recognizerName, valueStr, language);
	            			result.addMention(mention);
	            			// reset variables to (possibly) start a new mention
	            			open = false;
	            			type = null;
	            			startPos = -1;
	            		}
	            	}
	            }
		    }
	    }

		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, Data data) throws IOException
	{	StringBuffer buffer = new StringBuffer();
		
		Vector<NERDocument> documents = data.documents;
	    for(NERDocument document: documents)
	    {	 Vector<LinkedVector> sentences = document.sentences;
	    	
	        for(LinkedVector vector: sentences)
	        {	boolean open = false;
	            
	            String[] predictions = new String[vector.size()];
	            String[] words = new String[vector.size()];
	            for(int j=0;j<vector.size();j++)
	            {	NEWord word = (NEWord)vector.get(j);
	            	predictions[j] = word.neTypeLevel2;
	            	words[j] = word.form;
	            }
	            
	            for(int j=0;j<vector.size();j++)
	            {	if (predictions[j].startsWith("B-") || (j>0&&predictions[j].startsWith("I-") && (!predictions[j-1].endsWith(predictions[j].substring(2)))))
	            	{	buffer.append("[" + predictions[j].substring(2) + " ");
	            		open = true;
	            	}
	            	
	            	buffer.append(words[j]+ " ");
	            	if(open)
	            	{	boolean close = false;
	            		if(j==vector.size()-1)
	            		{	close = true;
	            		}
	            		else
	            		{	if(predictions[j+1].startsWith("B-"))
	            				close = true;
	            			else if(predictions[j+1].equals("O"))
	            				close = true;
	            			else if(predictions[j+1].indexOf('-')>-1&&(!predictions[j].endsWith(predictions[j+1].substring(2))))
	            				close = true;
	            		}
	            		if(close)
	            		{	buffer.append(" ] ");
	            			open=false;
	            		}
	            	}
	            }
		    }
	    }
		
		writeRawResultsStr(article, buffer.toString());
	}
}
