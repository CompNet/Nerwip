package fr.univavignon.common.tools.dbpedia;

import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

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
	/* TODO the lookup service works only for english, as of april 2017 
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
