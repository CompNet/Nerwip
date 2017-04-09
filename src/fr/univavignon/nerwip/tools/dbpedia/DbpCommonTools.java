package fr.univavignon.nerwip.tools.dbpedia;

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

import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class contains methods implementing 
 * some processing related to DBpedia.
 * <br/>
 * TODO apparently, we don't need these, due to Spotlight doing
 * all this work already. So I haven't integrated Sabrine's work
 * ({@link DbIdTools} & {@link DbTypeTools}).
 * 
 * @author Vincent Labatut
 * @author Sabrine Ayachi
 */
public class DbpCommonTools
{	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
//	/////////////////////////////////////////////////////////////////
//	// CACHE		 		/////////////////////////////////////////
//	/////////////////////////////////////////////////////////////////
//	/** Whether or not DBpedia results should be cached */
//	protected static boolean cache = true;
//	
//	/**
//	 * Enable or disable the memory cache
//	 * for DBpedia requests.
//	 *  
//	 * @param enabled
//	 * 		If {@code true}, the results from DBpedia are
//	 * 		stored in memory.
//	 */
//	public static void setCacheEnabled(boolean enabled)
//	{	DbpCommonTools.cache = enabled;
//	}
	
	/////////////////////////////////////////////////////////////////
	// URL			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/* TODO the lookup service works only for english,as of april 2017 
	 * manual: https://github.com/dbpedia/lookup
	 */
//	/** URL to access the DBpedia lookup service: prefix */
//	private final static String URL_LOOKUP_PRE = "http://lookup.dbpedia.org/api/search/KeywordSearch?MaxHits=20&QueryClass=";
//	/** URL to access the DBpedia lookup service: query parameter */
//	private final static String URL_LOOKUP_QUERY = "&QueryString=";
	
//	private final static String URL_SPARQL = "http://dbpedia.org/sparql";
	
	/////////////////////////////////////////////////////////////////
	// XML			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////
	// PROCESS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////
	// TESTS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
}
