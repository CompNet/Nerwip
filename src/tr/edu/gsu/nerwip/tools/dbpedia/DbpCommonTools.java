package tr.edu.gsu.nerwip.tools.dbpedia;

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

import tr.edu.gsu.nerwip.tools.keys.KeyHandler;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class contains methods implementing 
 * some processing related to DBpedia Spotlight.
 * 
 * @author Vincent Labatut
 * @author Abir Hadda
 */
public class DbpCommonTools
{	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// CACHE		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not DBpedia results should be cached */
	protected static boolean cache = true;
	
	/**
	 * Enable or disable the memory cache
	 * for DBpedia requests.
	 *  
	 * @param enabled
	 * 		If {@code true}, the results from DBpedia are
	 * 		stored in memory.
	 */
	public static void setCacheEnabled(boolean enabled)
	{	DbpCommonTools.cache = enabled;
	}
	
	/////////////////////////////////////////////////////////////////
	// URL			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// Old URL of the Web service 
//	private static final String SERVICE_URL = "http://spotlight.dbpedia.org/rest/";
	/** URL of the English version of the Web service */
	public static final String SERVICE_EN_URL = "http://spotlight.sztaki.hu:2222/rest/";
	/** URL of the French version of the Web service */
	public static final String SERVICE_FR_URL = "http://spotlight.sztaki.hu:2225/rest/";
	/** Recognizer URL */
	public static final String RECOGNIZER_SERVICE = "spot"; //or is it annotate ?
	/** Linker URL */
	public static final String LINKER_SERVICE = "disambiguate";
	/** Recognizer+Linker (at once) URL */
	public static final String BOTH_SERVICE = "annotate";
	
	/////////////////////////////////////////////////////////////////
	// XML			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Element containing the list of all entities resources */
	public final static String ELT_RESOURCES = "Resources";
	/** Element containing the list of informations of every entity resource */
	public final static String ELT_RESOURCE = "Resource";

	/** Attribute representing the name of an entity */
	public final static String ATT_SURFACE_FORM = "surfaceForm";
	/** Attribute representing the types of an entity */
	public final static String ATT_TYPES = "types";
	/** Attribute representing the DBpedia URI of an entity */
	public final static String ATT_URI = "URI";

}
