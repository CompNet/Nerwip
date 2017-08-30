package fr.univavignon.nerwip.processing.combiner.fullcombiner;

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
import fr.univavignon.nerwip.processing.combiner.svmbased.SvmCombiner;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteCombiner;
import fr.univavignon.nerwip.processing.internal.modelless.wikipediadater.WikipediaDater;

/**
 * This combiner is very basic: it first applies our date
 * detector to identify mentions which are quasi-certainly
 * dates, then uses one of the other combiners for the other
 * types of entities. There is no training for this combiner,
 * the training is performed at the level of the combiner
 * this one is built upon. 
 * <br/>
 * The recognizers handled by this combiner are:
 * <ul>
 * 		<li>WikipediaDater to detect dates (see {@link WikipediaDater})</li>
 * 		<li>Either SvmCombiner or VoteCombiner to detect locations, organizations 
 * 			and persons (see {@link SvmCombiner} and {@link VoteCombiner})</li>
 * </ul>
 * Various options allow changing the behavior of this combiner:
 * <ul>
 * 		<li>{@code combiner}: combiner used for the location, organization
 * 			and person mentions (i.e. either the SVM- or vote-based combiner).</li>
 * </ul>
 * 
 * @author Vincent Labatut
 */
public class FullCombiner extends AbstractProcessor implements InterfaceRecognizer
{	
	/**
	 * Builds a new full combiner.
	 *
	 * @param combinerName
	 * 		CombinerName used to handle locations, organizations and persons
	 * 		(either SVM- or vote-based).
	 *  
	 * @throws ProcessorException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	public FullCombiner(CombinerName combinerName) throws ProcessorException
	{	delegateRecognizer = new FullCombinerDelegateRecognizer(this, combinerName);
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.FULLCOMBINER;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER 			/////////////////////////////////////////////
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
	private FullCombinerDelegateRecognizer delegateRecognizer;
	
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
