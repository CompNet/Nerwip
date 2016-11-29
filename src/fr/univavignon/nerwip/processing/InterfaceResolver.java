package fr.univavignon.nerwip.processing;

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
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.Mentions;

/**
 * TODO
 * 		 
 * @author Vincent Labatut
 */
public interface InterfaceResolver extends InterfaceProcessor
{	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the folder containing the results of this
	 * resolver.
	 * <br/>
	 * This name takes into account the name of the tool, but also the 
	 * parameters it uses. It can also be used just whenever a string 
	 * representation of the tool and its parameters is needed.
	 * 
	 * @return 
	 * 		Name of the appropriate folder.
	 */
	public String getResolverFolder();
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the types of entities this resolver
	 * can handle with its current model/parameters.
	 * 
	 * @return 
	 * 		A list of entity types.
	 */
	public List<EntityType> getResolvedEntityTypes();
	
	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks whether the specified language is supported by this
	 * resolver, given its current settings (parameters, model...).
	 * 
	 * @param language
	 * 		The language to be checked.
	 * @return 
	 * 		{@code true} iff this resolver supports the specified
	 * 		language, with its current parameters (model, etc.).
	 */
	public boolean canResolveLanguage(ArticleLanguage language);
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Applies this processor to the specified article,
	 * in order to resolve co-occurrences.
	 * <br/>
	 * If {@code mentions} is {@code null}, the recognizer is applied to get
	 * the mentions. If {@code recognizer} is this object and must be applied, 
	 * then Nerwip tries to perform simultaneously mention recognition and coreference 
	 * resolution, if the processor allows it. Otherwise, the same processor is applied 
	 * separately for both tasks.
	 * <br/>
	 * Note the {@code Mention} object will be completed so as to point towards 
	 * their assigned entities.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @param mentions
	 * 		List of the previously recognized mentions.
	 * @param recognizer
	 * 		Processor used to recognize the entity mentions.
	 * @return
	 * 		List of the entities associated to the mentions.
	 * 
	 * @throws ProcessorException
	 * 		Problem while resolving co-occurrences. 
	 */
	public Entities resolve(Article article, Mentions mentions, InterfaceRecognizer recognizer) throws ProcessorException;
}
