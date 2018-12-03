package fr.univavignon.search.engines.web;

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
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import fr.univavignon.search.tools.files.SearchFileNames;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

/**
 * This class represents a search engine that one can use to return
 * a list of articles from the general Web.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractWebEngine
{	
	/**
	 * Builds a search engine focusing on the specified Website and
	 * period. If the Website is {@code null}, then there is no specific
	 * focus. If the period is {@code null}, then there is temporal 
	 * restriction.
	 * 
	 * @param website
	 * 		Target site, or {@code null} to search the whole Web.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 */
	public AbstractWebEngine(String website, Date startDate, Date endDate)
	{	this.website = website;
		this.startDate = startDate;
		this.endDate = endDate;
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
	{	BingEngine.ENGINE_NAME,
		GoogleEngine.ENGINE_NAME,
		QwantEngine.ENGINE_NAME,
		YandexEngine.ENGINE_NAME
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
	public Map<String,URL> retrieveResults(String keywords) throws IOException
	{	Map<String,URL> result;
		
		// setup cache file path
		String cacheFilePath = SearchFileNames.FO_WEB_SEARCH_RESULTS + File.separator + getName();
		File cacheFolder = new File(cacheFilePath);
		cacheFolder.mkdirs();
		cacheFilePath = cacheFilePath + File.separator;
		if(website!=null)
			cacheFilePath = cacheFilePath + URLEncoder.encode(website,"UTF-8");
		cacheFilePath = cacheFilePath + SearchFileNames.FI_SEARCH_RESULTS;
		
		// possibly use cached results
		File cacheFile = new File(cacheFilePath);
		if(cachedSearch && cacheFile.exists())
		{	logger.log("Loading the previous search results from file "+cacheFilePath);
			result = new HashMap<String,URL>();	
			Scanner sc = FileTools.openTextFileRead(cacheFile,"UTF-8");
			while(sc.hasNextLine())
			{	String line = sc.nextLine();
				String tmp[] = line.split("\t");
				String key = tmp[0].trim();
				String urlStr = tmp[1].trim();
				URL url = new URL(urlStr);
				result.put(key,url);
			}
			logger.log("Number of URLs loaded: "+result.size());
		}
		
		// otherwise, perform the search and possibly cache results
		else
		{	logger.log("Applying search engine "+getName());
			logger.increaseOffset();
				// apply the engine
				result = search(keywords);
				
				// possibly record its results
				if(cachedSearch)
				{	logger.log("Recording all URLs in text file \""+cacheFilePath+"\"");
					PrintWriter pw = FileTools.openTextFileWrite(cacheFile,"UTF-8");
					for(Entry<String, URL> entry: result.entrySet())
					{	String key = entry.getKey();
						URL url = entry.getValue();
						pw.println(key+"\t"+url.toString());
					}
					pw.close();
				}
			logger.decreaseOffset();
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SEARCH		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Web sites on which the search focuses (or {@code null} for no specific focus) */
	protected String website = null;
	/** Start of the period on which the search focuses (or {@code null} for no period) */
	protected Date startDate = null;
	/** End of the period on which the search focuses (or {@code null} for no period) */
	protected Date endDate = null;
	
	/**
	 * Performs a search using the corresponding engine.
	 * 
	 * @param keywords
	 * 		Person we want to look for.
	 * @return
	 * 		List of results taking the form of URLs.
	 * 
	 * @throws IOException
	 * 		Problem while searching the Web.
	 */
	protected abstract Map<String,URL> search(String keywords) throws IOException;
	
	/////////////////////////////////////////////////////////////////
	// STRING		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = getName();
		if(website!=null)
			result = result + "@" + website;
		return result;
	}
}
