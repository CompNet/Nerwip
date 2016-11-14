package tr.edu.gsu.nerwip.linking;

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

import java.util.List;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.data.entity.mention.Mentions;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class is used to represent or implement entity linkers, i.e. tools
 * to link an entity to an entry in a reference database.
 * The former case corresponds to external tools, i.e. applications
 * executed externally. The latter to tools invocable internally,
 * i.e. programmatically, from within Nerwip.
 * 		 
 * @author Vincent Labatut
 */
public abstract class AbstractLinker
{	
	/**
	 * Builds a new linker,
	 * using the specified default options.
	 */
	public AbstractLinker()
	{	
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Return the (standardized) name of this linker.
	 * 
	 * @return 
	 * 		Name of this tool.
	 */
	public abstract LinkerName getName();

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the folder
	 * containing the results of this
	 * tool.
	 * <br/>
	 * This name takes into account the
	 * name of the tool, but also the parameters
	 * it uses. It can also be used just whenever
	 * a string representation of the tool and its
	 * parameters is needed.
	 * 
	 * @return 
	 * 		Name of the appropriate folder.
	 */
	public abstract String getFolder();

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of entity types this linker
	 * can handle if it is trained for.
	 * 
	 * @return 
	 * 		A list of entity types.
	 */
	public abstract List<EntityType> getHandledMentionTypes();
	
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
	public abstract boolean canHandleLanguage(ArticleLanguage language);
	
	/////////////////////////////////////////////////////////////////
	// CACHING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not cache should be used */
	protected boolean cache = true;
	
	/**
	 * Indicates whether or not caching is
	 * enabled for this linker.
	 *  
	 * @return
	 * 		{@code true} iff caching is enabled.
	 */
	public boolean doesCache()
	{	return cache;
	}
	
	/**
	 * Changes the cache flag. If {@code true}, the {@link #process(Article,Mentions,Entities) process}
	 * method will first check if the results already
	 * exist as a file. In this case, they will be loaded
	 * from this file. Otherwise, the process will be
	 * conducted normally, then recorded.
	 * 
	 * @param enabled
	 * 		If {@code true}, the (possibly) cached files are used.
	 */
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
	}

	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Applies this linker to the specified article,
	 * and updates the lists of mentions and entities accordingly.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @param mentions
	 * 		Previously detected mentions for the considered article, to be updated.
	 * @param entities
	 * 		Known entities, to be updated.
	 * 
	 * @throws LinkerException
	 * 		Problem while applying the linker.
	 */
	public abstract void process(Article article, Mentions mentions, Entities entities) throws LinkerException;

	/////////////////////////////////////////////////////////////////
	// STRING		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	LinkerName name = getName();
		String result = name.toString();
		return result;
	}
}
