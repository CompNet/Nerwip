package fr.univavignon.nerwip.processing.internal.modelbased.stanford;

import java.io.IOException;

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

import java.util.List;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;

/**
 * This class acts as a delegate for the mention recognition with 
 * Stanford Named Entity Recognizer.
 * It handles mention recognition.
 * See {@link StanfordDelegateRecognizer} for more details.
 * <br/>
 * Official Stanford website: <a href="http://nlp.stanford.edu/software/CRF-NER.shtml">http://nlp.stanford.edu/software/CRF-NER.shtml</a>
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class Stanford extends AbstractProcessor implements InterfaceRecognizer
{	
	/**
	 * Builds and sets up an object representing
	 * a Stanford recognizer.
	 * 
	 * @param modelName
	 * 		Predefined classifier used for mention detection.
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop words should be excluded from the detection.
	 * 
	 * @throws ProcessorException 
	 * 		Problem while loading the model data.
	 */
	public Stanford(StanfordModelName modelName, boolean loadModelOnDemand, boolean ignorePronouns, boolean exclusionOn) throws ProcessorException
	{	delegateRecognizer = new StanfordDelegateRecognizer(this, modelName, loadModelOnDemand, ignorePronouns, exclusionOn);
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.STANFORD;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getRecognizerFolder()
	{	String result = delegateRecognizer.getFolder();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RECOGNIZER 			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Delegate in charge of recognizing entity mentions */
	private StanfordDelegateRecognizer delegateRecognizer;
	
	@Override
	public boolean isRecognizer()
	{	return true;
	}
	
	@Override
	public List<EntityType> getRecognizedEntityTypes()
	{	List<EntityType> result = delegateRecognizer.getHandledEntityTypes();
		return result;
	}

	@Override
	public boolean canRecognizeLanguage(ArticleLanguage language) 
	{	boolean result = delegateRecognizer.canHandleLanguage(language);
		return result;
	}
	
	@Override
	public Mentions recognize(Article article) throws ProcessorException
	{	Mentions result = delegateRecognizer.delegateRecognize(article);
		return result;
	}
	
	@Override
	public void writeRecognizerResults(Article article, Mentions mentions) throws IOException 
	{	delegateRecognizer.writeXmlResults(article, mentions);
	}
	
	/////////////////////////////////////////////////////////////////
	// RESOLVER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean isResolver()
	{	return false;
	}
	
	/////////////////////////////////////////////////////////////////
	// LINKER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean isLinker()
	{	return false;
	}
}
