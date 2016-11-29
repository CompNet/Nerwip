package fr.univavignon.nerwip.processing.internal.modelless.dateextractor;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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

import java.text.SimpleDateFormat;
import java.util.List;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelless.wikipediadater.WikipediaDater;

/**
 * This class implements our first date extractor.
 * It focuses on classic date formats (eg 31 august 1903).
 * <br/>
 * However it is not efficient enough on WP articles, because
 * it incorrectly recognizes many irrelevent expressions as 
 * dates. This is partly due to the too many {@link SimpleDateFormat} 
 * it relies uppon. A more suitable tool was tailored for
 * Wikipedia articles, cf. {@link WikipediaDater}.
 * 
 * @author Burcu Küpelioğlu
 */
public class DateExtractor extends AbstractProcessor implements InterfaceRecognizer
{
	/**
	 * Builds and sets up an object representing
	 * our date extractor.
	 */
	public DateExtractor()
	{	delegateRecognizer = new DateExtractorDelegateRecognizer(this);
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.DATEEXTRACTOR;
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
	// RECOGNIZER	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Delegate in charge of recognizing entity mentions */
	private DateExtractorDelegateRecognizer delegateRecognizer;
	
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
}
