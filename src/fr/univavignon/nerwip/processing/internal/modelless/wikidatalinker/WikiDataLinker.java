package fr.univavignon.nerwip.processing.internal.modelless.wikidatalinker;

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

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.MentionsEntities;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.InterfaceLinker;
import fr.univavignon.nerwip.processing.InterfaceResolver;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;

/**
 * This class acts as an interface with the WikiData Web service.
 * It handles entity liking.
 * See the delegate for more details: {@link WikiDataLinkerDelegateLinker}.
 * <br>
 * The approach is quite naive: we simply look for various combination
 * of the name(s) detected in the text, and try to pick the best returned
 * entity based on a series of basic heuristics. Amongst other things,
 * we check if the type of the returned entity is compatible with the one
 * estimated during mention detection / entity resolution.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class WikiDataLinker extends AbstractProcessor implements InterfaceLinker
{
	/**
	 * Builds and sets up an object representing
	 * the WikiData linker. The specified recognizer and
	 * linker as used to get the needed mentions and entities.
	 * 
	 * @param resolver
	 * 		Processor used for coreference resolution.
	 * @param revision
	 * 		Whether or not merge entities previously considered
	 * 		as distinct, but turning out to be linked to the same id.
	 */
	public WikiDataLinker(InterfaceResolver resolver, boolean revision)
	{	// resolve
		this.resolver = resolver;
		// link
		delegateLinker = new WikiDataLinkerDelegateLinker(this, revision);
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.WIKIDATA_LINKER;
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getLinkerFolder()
	{	String result = delegateLinker.getFolder();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RESOLVER		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Resolver applied before this linker */ 
	protected InterfaceResolver resolver = null;
	
	@Override
	public boolean isRecognizer()
	{	return false;
	}
	
	@Override
	public InterfaceResolver getResolver()
	{	return resolver;
	}
	
	/////////////////////////////////////////////////////////////////
	// RESOLVER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean isResolver()
	{	return false;
	}
	
	/////////////////////////////////////////////////////////////////
	// LINKER		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Delegate in charge of recognizing entity mentions */
	protected WikiDataLinkerDelegateLinker delegateLinker;
	
	@Override
	public boolean isLinker()
	{	return true;
	}
	
	@Override
	public List<EntityType> getLinkedEntityTypes()
	{	List<EntityType> result = delegateLinker.getHandledEntityTypes();
		return result;
	}

	@Override
	public boolean canLinkLanguage(ArticleLanguage language) 
	{	boolean result = delegateLinker.canHandleLanguage(language);
		return result;
	}
	
	@Override
	public MentionsEntities link(Article article) throws ProcessorException
	{	MentionsEntities result = delegateLinker.delegateLink(article);
		return result;
	}
}
