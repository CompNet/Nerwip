package fr.univavignon.search.engines.social;

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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import fr.univavignon.search.results.SocialSearchResult;
import fr.univavignon.search.tools.files.SearchFileNames;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

/**
 * This class represents a search engine one can use to return
 * a list of posts from a specific social media.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractSocialEngine
{	
	/**
	 * Initializes an object to search a social media.
	 * 
	 * @param seed
	 * 		Name of the page/user/account used to start the search.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * @param doExtendedSearch
	 * 		If {@code true}, the search returns the posts by the commenting
	 * 		users, for the specified period. 
	 */
	public AbstractSocialEngine(String seed, Date startDate, Date endDate, boolean doExtendedSearch)
	{	this.seed = seed;
		this.startDate = startDate;
		this.endDate = endDate;
		this.doExtendedSearch = doExtendedSearch;
	}
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	public static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// NAME			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of search engine names */
	public final static String[] ENGINE_NAMES = 
	{	FacebookEngine.ENGINE_NAME
	};
	
	/**
	 * Returns a String representing the name
	 * of this search engine.
	 * 
	 * @return
	 * 		Name of this search engine.
	 */
	public abstract String getName();
	
	/////////////////////////////////////////////////////////////////
	// CACHE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not to cache the search results */
	private boolean cachedSearch = true;
	
	/**
	 * Tries to get the results from cache, and otherwise to perform
	 * the actual search in order to get them.
	 * 
	 * @param keywords
	 * 		Researched person.
	 * @return
	 * 		Map of URLs resulting from the retrieval.
	 *  
	 * @throws IOException
	 * 		Problem while accessing the results.
	 */
	public List<SocialSearchResult> retrieveResults(String keywords) throws IOException
	{	List<SocialSearchResult> result;
		
		// setup cache file path
		String cacheFilePath = SearchFileNames.FO_SOCIAL_SEARCH_RESULTS + File.separator + getName();
		File cacheFolder = new File(cacheFilePath);
		cacheFolder.mkdirs();
		cacheFilePath = cacheFilePath + File.separator;
		if(seed==null)
			cacheFilePath = cacheFilePath + keywords.replace(' ', '_') + "_";
		else
			cacheFilePath = cacheFilePath + seed.replace(' ', '_') + "_";
		cacheFilePath = cacheFilePath + SearchFileNames.FI_SEARCH_RESULTS;
		
		// possibly use cached results
		File cacheFile = new File(cacheFilePath);
		if(cachedSearch && cacheFile.exists())
		{	logger.log("Loading the previous search results from file "+cacheFilePath);
			result = new ArrayList<SocialSearchResult>();
			Scanner sc = FileTools.openTextFileRead(cacheFile,"UTF-8");
			while(sc.hasNextLine())
			{	SocialSearchResult post = SocialSearchResult.readFromText(sc);
				result.add(post);
			}
			logger.log("Number of posts loaded (not counting the comments): "+result.size());
		}
		
		// search the results
		else
		{	logger.log("Applying search engine "+getName());
			logger.increaseOffset();
				// apply the engine
				result = search(keywords);
				
				// possibly record its results
				if(cachedSearch)
				{	logger.log("Recording all posts in text file \""+cacheFilePath+"\"");
					PrintWriter pw = FileTools.openTextFileWrite(cacheFile,"UTF-8");
					for(SocialSearchResult post: result)
						post.writeAsText(pw);
					pw.close();
				}
			logger.decreaseOffset();
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SEARCH		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Starting page of the search ({@code null} if similar to the searched person) */
	protected String seed = null;
	/** Start of the period on which the search focuses (or {@code null} for no period) */
	protected Date startDate = null;
	/** End of the period on which the search focuses (or {@code null} for no period) */
	protected Date endDate = null;
	/** Whether to search among the people commenting the posts of the seed page ({@code true}) or not ({@code false}) */
	protected boolean doExtendedSearch = false;
	
	/**
	 * Performs a search using the corresponding engine. The method
	 * returns the posts published on the page corresponding to the 
	 * specified keywords, as weel as the direct comments of these
	 * posts. If {@code extendedSearch} is {@code true}, the method
	 * also returns the posts of the commenting users for the same
	 * period.
	 * 
	 * @param keywords
	 * 		Person we want to look for.
	 * @return
	 * 		List of posts and comments.
	 * 
	 * @throws IOException
	 * 		Problem while searching the Web.
	 */
	protected abstract List<SocialSearchResult> search(String keywords)  throws IOException;
	
	/////////////////////////////////////////////////////////////////
	// STRING		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = getName();
		if(seed!=null)
			result = result + "@" + seed;
		return result;
	}
}
