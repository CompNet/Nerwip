package fr.univavignon.search.tools.files;

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

import fr.univavignon.tools.files.FileNames;

/**
 * This class contains various constants
 * related to file and folder names.
 *  
 * @author Vincent Labatut
 */
public class SearchFileNames
{	
	/////////////////////////////////////////////////////////////////
	// FOLDERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the folder containing the whole search results */
	private final static String FFO_SEARCH = "search"; 
	/** Name of the folder containing the web search results */
	private final static String FFO_WEB_SEARCH = "web_search"; 
	/** Name of the folder containing the cached web pages */
	private final static String FFO_WEB_CACHE = "_pages"; 
	/** Name of the folder containing the social search results */
	private final static String FFO_SOCIAL_SEARCH = "social_search"; 
	
	/** Folder containing search results */
	public static String FO_SEARCH = FileNames.FO_OUTPUT + File.separator + FFO_SEARCH;
	/** Folder containing web search results */
	public static String FO_WEB_SEARCH_RESULTS = FileNames.FO_OUTPUT + File.separator + FFO_WEB_SEARCH;
		/** Folder containing cached web pages */
		public static String FO_WEB_PAGES = FO_WEB_SEARCH_RESULTS + File.separator + FFO_WEB_CACHE;
	/** Folder containing social media search results */
	public static String FO_SOCIAL_SEARCH_RESULTS = FileNames.FO_OUTPUT + File.separator + FFO_SOCIAL_SEARCH;
	
	/**
	 * Changes the folder used to output the files produced during the search.
	 * It relies on the keywords used during the current search.
	 * 
	 * @param keywords
	 * 		Keywords of the current search.
	 */
	public static void setSearchFolder(String keywords)
	{	// add the keywords after the default output folder 
		if(keywords==null)
			FO_SEARCH = FileNames.FO_OUTPUT + File.separator + FFO_SEARCH;
		else
			FO_SEARCH = FO_SEARCH + File.separator + keywords;
		
		// update the related subfolders
		FO_WEB_SEARCH_RESULTS = FO_SEARCH + File.separator + FFO_WEB_SEARCH;
		FO_WEB_PAGES = FO_WEB_SEARCH_RESULTS + File.separator + FFO_WEB_CACHE;
		FO_SOCIAL_SEARCH_RESULTS = FO_SEARCH + File.separator + FFO_SOCIAL_SEARCH;
	}
		
	/////////////////////////////////////////////////////////////////
	// FILES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** File containing cached search results */
	public final static String FI_SEARCH_RESULTS = "search" + FileNames.EX_TEXT;
	/** File containing overall search results */
	public final static String FI_ARTICLES_RAW = "articles_raw" + FileNames.EX_CSV;
	/** File containing the search results after URL filtering */
	public final static String FI_ARTICLES_URL_FILTER = "articles_url_filter" + FileNames.EX_CSV;
	/** File containing the search results after content filtering */
	public final static String FI_ARTICLES_CONTENT_FILTER = "articles_content_filter" + FileNames.EX_CSV;
	/** File containing the search results after entity filtering */
	public final static String FI_ARTICLES_ENTITY_FILTER = "articles_entity_filter" + FileNames.EX_CSV;
	/** File containing the search results after article clustering */
	public final static String FI_ARTICLES_CLUSTERING = "articles_clustering" + FileNames.EX_CSV;
	/** File containing the confusion matrix resulting from the article clustering */
	public final static String FI_ARTICLES_CLUSTERING_CONFMAT = "articles_clustering_confmat" + FileNames.EX_CSV;
	/** File containing the merged Web and social media results */
	public final static String FI_ARTICLES_MERGE = "articles_merge" + FileNames.EX_CSV;
	/** File containing the search results after cluster filtering */
	public final static String FI_ARTICLES_CLUSTER_FILTER = "articles_cluster_filter" + FileNames.EX_CSV;
	/** Events file, by article */
	public final static String FI_EVENT_LIST_BYARTICLE = "event_list_byarticle" + FileNames.EX_CSV;
	/** Events file, by sentence */
	public final static String FI_EVENT_LIST_BYSENTENCE = "event_list_bysentence" + FileNames.EX_CSV;
	/** Event clusters file, by article */
	public final static String FI_EVENT_CLUSTERS_BYARTICLE = "event_clusters_byarticle" + FileNames.EX_CSV;
	/** Event clusters file, by sentence */
	public final static String FI_EVENT_CLUSTERS_BYSENTENCE = "event_clusters_bysentence" + FileNames.EX_CSV;
	/** Reference file for the events */
	public final static String FI_ANNOTATED_EVENTS = "annotated_events" + FileNames.EX_TEXT;
	/** Reference file for the information retrieval task */
	public final static String FI_ANNOTATED_CLUSTERS = "annotated_clusters" + FileNames.EX_TEXT;
	/** Performance reached for the information retrieval task */
	public final static String FI_PERFORMANCE = "performance" + FileNames.EX_CSV;
	/** List of Facebook ids */
	public final static String FI_FACEBOOK_IDS = "fb_ids" + FileNames.EX_TEXT;
}
