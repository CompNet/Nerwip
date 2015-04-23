package tr.edu.gsu.nerwip.retrieval.reader;

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

import java.net.URL;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * All classes automatically getting articles
 * from the Web using a starting name or URL 
 * should inherit from this abstract class.
 * 
 * @author Vincent Labatut
 */
public abstract class ArticleReader
{
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CACHE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not original source code should be cached localy */
	protected boolean cache = true;
	
	/**
	 * Switches the cache flag.
	 * 
	 * @param enabled
	 * 		{@code true} to enable caching.
	 */
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
	}

	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes the name of the article
	 * from the specified URL.
	 * 
	 * @param url
	 * 		URL of the article.
	 * @return
	 * 		Name of the article.
	 */
	public abstract String getName(URL url);
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes the specified URL to get the
	 * targetted article.
	 * 
	 * @param url
	 * 		Article address.
	 * @return
	 * 		An Article object corresponding to the targetted URL.
	 * 
	 * @throws ReaderException
	 * 		Problem while retrieving the article.
	 */
	public abstract Article read(URL url) throws ReaderException;
}
