package fr.univavignon.nerwip.processing.internal.modelless.wikipediadater;

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
 * Detects most date formats used in Wikipedia articles.
 * Only focuses on dates, though. The value of the dates
 * is not retrieved, just the string representing it.
 * <br/>
 * It handles the following forms:
 * <ul>
 * 		<li>{@code DD MMMM YYYY} ex: 20 April 1889</li>
 * 		<li>{@code DD MM YYYY} ex: 5 Sep 1887</li>
 * 		<li>{@code DD MMMM, YYYY} ex: 24 December, 1822</li>
 * 		<li>{@code DDth of MMMM YYYY} ex: 10th of April 2004</li>
 * 		<li>{@code MMMM DD, YYYY} ex: May 30, 1914</li>
 * 
 * 		<li>{@code MMMM DD} ex: October 6</li>
 * 		<li>{@code DD MMMM} ex: 6 October</li>
 * 		<li>{@code DDth MMMM} ex: 14 September</li>
 * 		<li>{@code DD of MMMM} ex: 10th of April</li>
 * 
 * 		<li>{@code MMMM YYYY} ex: October 1926</li>
 * 		<li>{@code MM YYYY} ex: Dec 1996</li>
 * 		<li>{@code MMMM, YYYY} ex: May, 1977</li>
 * 		<li>{@code MMMM of YYYY} ex: April of 1968</li>
 * 		<li>{@code early/mid/late MMMM, YYYY} ex: late April 1968</li>
 * 		<li>{@code early/mid/late MMMM of YYYY} ex: late April of 1968</li>
 * 
 * 		<li>{@code MMMM} ex: January</li>
 * 		<li>{@code early/mid/late MMMM} ex: late May</li>
 * 
 * 		<li>{@code dddddddddddd YYYY} ex: May Day 2001</li>
 * 
 * 		<li>{@code YYYY} ex: 1977</li>
 * 		<li>{@code 'YY} ex: '96</li>
 * 		<li>{@code YYY0s} ex: 1990s</li>
 * 		<li>{@code early/mid/late YYY0s} ex: early 1990s</li>
 * 
 * 		<li>{@code CCCC-century} ex: twentieth-century</li>
 * 		<li>{@code CCth-century} ex: 11th-century</li>
 * 		<li>{@code CCth century} ex: 15th century</li>
 * 
 * 		<li>{@code AAth anniversary} ex: 40th anniversary</li>
 * </ul>
 * It also recognizes certain forms of durations:
 * <ul>
 * 		<li>{@code MM-MM YYYY} ex: Feb-Jun 2005</li>
 * 		<li>{@code YYYY-YY} ex: 2006-07 (academic year, season)</li>
 * 		<li>{@code YYYY-YY} ex: 2002-3 (academic year, season)</li>
 * </ul>
 * <b>Note:</b> this tool might not work well on other text
 * than English Wikipedia articles.
 * 
 * @author Vincent Labatut
 */
public class WikipediaDater extends AbstractProcessor implements InterfaceRecognizer
{
	/**
	 * Builds and sets up an object representing
	 * a wikipedia dater.
	 */
	public WikipediaDater()
	{	delegateRecognizer = new WikipediaDaterDelegateRecognizer(this);
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.WIKIPEDIADATER;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = null;
		//TODO
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RECOGNIZER	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Delegate in charge of recognizing entity mentions */
	private WikipediaDaterDelegateRecognizer delegateRecognizer;
	
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
