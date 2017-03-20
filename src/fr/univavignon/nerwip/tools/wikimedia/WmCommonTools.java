package fr.univavignon.nerwip.tools.wikimedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;

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
import fr.univavignon.nerwip.tools.web.WebTools;

/**
 * This class contains methods implementing 
 * some processing related to Wikimedia Foundation services.
 * 
 * @author Vincent Labatut
 * @author Sabrine Ayachi
 */
public class WmCommonTools
{	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
//	/////////////////////////////////////////////////////////////////
//	// CACHE		 		/////////////////////////////////////////
//	/////////////////////////////////////////////////////////////////
//	/** Whether or not WikiMedia results should be cached */
//	protected static boolean cache = true;
//	
//	/**
//	 * Enable or disable the memory cache
//	 * for WikiMedia requests.
//	 *  
//	 * @param enabled
//	 * 		If {@code true}, the results from WikiMedia are
//	 * 		stored in memory.
//	 */
//	public static void setCacheEnabled(boolean enabled)
//	{	WmCommonTools.cache = enabled;
//	}
	
	/////////////////////////////////////////////////////////////////
	// URL			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** URL used to access the web search API of WikiData */
	public static final String WIKIDATA_WEBSEARCH_URL ="https://www.wikidata.org/w/api.php?action=wbsearchentities&format=xml&includexmlnamespace=true&type=item&limit=max";
	/** Name of the parameter representing the searched string for the web search API of WikiData */
	public static final String WIKIDATA_WEBSEARCH_PARAM_SEARCH = "&search=";
	/** Name of the parameter representing the targeted language for the web search API of WikiData */
	public static final String WIKIDATA_WEBSEARCH_PARAM_LANG = "&language=";

	/////////////////////////////////////////////////////////////////
	// XML			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	private static final String ELT_SEARCH = "search";
	private static final String ATT_ID = "id";
	private static final String ATT_DESC = "description";
	
	
	/////////////////////////////////////////////////////////////////
	// ENTITY ID	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	
	
	
	public static void lookupName(String name)
	{
		 // 1) perform the approximate search using the websearch API and get the ids
		 // 2) if there is a desambiguisation page, also get its ids and treat it.
		 // 3) if there is nothing, try the variants of the name (if person): this was done before. look for "firstname" in the source code.
		 // 4) use the SPARQL API to perform a more precise search among the ids
		 // 5) extract all relevant information from these results  
		
	}
	
	
	private static List<String> retrieveIdFromName(String name, EntityType type, ArticleLanguage language) throws ClientProtocolException, IOException, JDOMException
	{	List<String> result = null;
		
		String baseUrl = WIKIDATA_WEBSEARCH_URL 
				+ WIKIDATA_WEBSEARCH_PARAM_LANG + language.toString().toLowerCase(Locale.ENGLISH)
				+ WIKIDATA_WEBSEARCH_PARAM_SEARCH;
		
		// lookup the whole name
		{	// request the server
			String url = baseUrl + name;
			HttpClient httpclient = new DefaultHttpClient();   
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpclient.execute(request);
			
			// process the answer
			String answer = WebTools.readAnswer(response);
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(answer));
			Element root = doc.getRootElement();
			
			
		}
		
		// if nothing and the entity is a person, try playing with the first/lastname combinations
		//TODO do that first: generate all possible search strings by order of priority, then use a while
		
		
		return result;
	}
	
	private static List<String> retrieveIdFromDisambiguation(String disambiguationId)
	{	List<String> result = null;
		//TODO
		return result;
	}
	
	private static String filterIds(List<String> ids)
	{	String result = null;
		//TODO
		return result;
	}
	
	private static void retrieveDetailsFromId(String id)
	{
		//TODO
	}
	
    /**
    * This method takes an entity name as parameter,
    * and retrieves its WikiData id.
    * 
    * @param entityName
    * 		Name of the entity.
    * @return
    * 		A String describing the WikiData id.
    * 
    * @throws IOException
    *      Problem while retrieving the WikiData id. 
    * @throws ClientProtocolException 
    *      Problem while retrieving the WikiData id.
    * @throws org.json.simple.parser.ParseException
    *      Problem while retrieving the WikiData id. 
    */
    public static String getWikiDataId(String entityName) throws ClientProtocolException, IOException, org.json.simple.parser.ParseException 
    {	logger.increaseOffset();
    	String result = null;


    	// get Wikidata answer
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = httpclient.execute(request);
		logger.log("response=" +  response.toString());

		//builds object from answer
		JSONParser parser = new JSONParser();
		HttpEntity entity = response.getEntity();
		String str = EntityUtils.toString(entity);
		logger.log("str=" + str);
		
		JSONObject jsonData = (JSONObject)parser.parse(str);
		//logger.log("jsondata=" + jsonData.toString());
		JSONArray answer = (JSONArray)jsonData.get("search");
		//String string = answer.toString();
		//logger.log("answer=" + string);

		// extract types from the answer
		if(answer!=null)
		{	Object obj = answer.get(0);
			result = (String) ((JSONObject)obj).get("id");
			logger.log("result=" + result);

/*			for (Object object : answer)
		 	{	// process id
				result = (String) ((JSONObject)object).get("id");
				logger.log("result=" + result);
			}
*/
	    }

		logger.decreaseOffset();
		logger.log("result=" + result);

		return result;
	}
}
