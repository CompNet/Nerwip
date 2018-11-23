package fr.univavignon.nerwip.processing.internal.modelless.naiveresolver;

import java.io.IOException;

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

import java.util.List;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.MentionsEntities;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.InterfaceResolver;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;

/**
 * This class implements a naive resolver relying on the surface form of the 
 * mentions identified at the previous step of the process (i.e. mention recognition).
 * <br/>
 * It uses the Levenshtein distance, and the user must specify a threshold corresponding
 * to the maximal distance allowed for to strings to be considered as similar (and 
 * therefore for the corresponding mentions to be linked to the same entity). 
 * 
 * @author Vincent Labatut
 */
public class NaiveResolver extends AbstractProcessor implements InterfaceResolver
{
	/**
	 * Builds and sets up an object representing the naive resolver. 
	 * The specified recognizer is used.
	 * 
	 * @param recognizer
	 * 		Processor used to recognize the entity mentions.
	 * @param maxDist
	 * 		Maximal distance for two strings to be considered as similar.
	 */
	public NaiveResolver(InterfaceRecognizer recognizer, int maxDist)
	{	// recognize
		this.recognizer = recognizer;
		// resolve
		delegateResolver = new NaiveResolverDelegateResolver(this, maxDist);
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.NAIVE_RESOLVER;
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getResolverFolder() 
	{	String result = delegateResolver.getFolder();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RECOGNIZER	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Recognizer applied before this resolver or linker (can be different from the delegate) */
	protected InterfaceRecognizer recognizer = null;
	
	@Override
	public InterfaceRecognizer getRecognizer()
	{	return recognizer;
	}
	@Override
	public boolean isRecognizer()
	{	return true;
	}
	
	
	/////////////////////////////////////////////////////////////////
	// RESOLVER		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Delegate in charge of recognizing entity mentions */
	protected NaiveResolverDelegateResolver delegateResolver;
	
	@Override
	public List<EntityType> getResolvedEntityTypes()
	{	List<EntityType> result = delegateResolver.getHandledEntityTypes();
		return result;
	}

	@Override
	public boolean canResolveLanguage(ArticleLanguage language) 
	{	boolean result = delegateResolver.canHandleLanguage(language);
		return result;
	}
	
	@Override
	public MentionsEntities resolve(Article article) throws ProcessorException
	{	MentionsEntities result = delegateResolver.delegateResolve(article);
		return result;
	}
	
	@Override
	public boolean isResolver()
	{	return false;
	}
	
	@Override
	public void writeResolverResults(Article article, Mentions mentions, Entities entities) throws IOException 
	{	delegateResolver.writeXmlResults(article, mentions, entities);
	}
	
	/////////////////////////////////////////////////////////////////
	// LINKER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean isLinker()
	{	return false;
	}
}
