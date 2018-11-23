package fr.univavignon.nerwip.processing;

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

/**
 * Interface implemented by all classes able to perform
 * entity linking, i.e. identify which entity one mention
 * refers to.
 * 		 
 * @author Vincent Labatut
 */
public interface InterfaceLinker extends InterfaceProcessor
{	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the folder containing the results of this
	 * linker.
	 * <br/>
	 * This name takes into account the name of the tool, but also the 
	 * parameters it uses. It can also be used just whenever a string 
	 * representation of the tool and its parameters is needed.
	 * 
	 * @return 
	 * 		Name of the appropriate folder.
	 */
	public String getLinkerFolder();
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the types of entities this linker
	 * can handle with its current model/parameters.
	 * 
	 * @return 
	 * 		A list of entity types.
	 */
	public List<EntityType> getLinkedEntityTypes();
	
	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks whether the specified language is supported by this
	 * linker, given its current settings (parameters, model...).
	 * 
	 * @param language
	 * 		The language to be checked.
	 * @return 
	 * 		{@code true} iff this linker supports the specified
	 * 		language, with its current parameters (model, etc.).
	 */
	public boolean canLinkLanguage(ArticleLanguage language);
	
	/////////////////////////////////////////////////////////////////
	// RESOLVER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the resolver applied before this linker.
	 * 
	 * @return
	 * 		Resolver used before this linker.
	 */
	public InterfaceResolver getResolver();
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Applies this processor to the specified article,
	 * in order to link entities to unique identifiers in 
	 * databases such as DBpedia or Freelink.
	 * <br/>
	 * The recognizer and resolver that were set up for this linker will 
	 * automatically be applied, or their results will be loaded if their
	 * cache is enabled (and the results are cached). The corresponding 
	 * {@code Mentions} object will be completed and returned with the
	 * {@link Entities}.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		Sets of the updated mentions and their associated entities.
	 * 
	 * @throws ProcessorException
	 * 		Problem while resolving co-occurrences. 
	 */
	public MentionsEntities link(Article article) throws ProcessorException;
}
 