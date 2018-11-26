package fr.univavignon.nerwip.tools.freebase;

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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class contains methods used to retrieve the Freebase
 * types associated to Wikipedia articles. 
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class FbTypeTools
{	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// ALL TYPES	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map used as a memory cache for Freebase queries */
	private static FbCache allTypes;
	
	/**
	 * This method takes the title of a Wikipedia page,
	 * and retrieves all its FreeBase types.
	 * <br/>
	 * Those types must then be processed in order to
	 * get the corresponding {@link EntityType} or
	 * article category.
	 * 
	 * @param wikipediaTitle
	 * 		Title of the Wikipedia article.
	 * @return
	 * 		a List containing the FB types of the object described by the WP article.
	 * 
	 * @throws IOException 
	 * 		Problem while retrieving the FB types.
	 * @throws ClientProtocolException 
	 * 		Problem while retrieving the FB types.
	 * @throws org.json.simple.parser.ParseException 
	 * 		Problem while retrieving the FB types.
	 * @throws ParseException
	 * 		Problem while retrieving the FB types.
	 */
	public static List<String> getAllTypes(String wikipediaTitle) throws ClientProtocolException, IOException, ParseException, org.json.simple.parser.ParseException  
	{	logger.increaseOffset();
		List<String> result = null;
		
		// possibly get result from cache
		if(FbCommonTools.cache)
		{	if(allTypes==null)
				allTypes = new FbCache(FileNames.FI_ALL_TYPES);
			result = allTypes.getValues(wikipediaTitle);
		}
		
		if(result==null)
		{	
			// set Freebase query the old way
//			String query = "[{ \"name\": null, " +
//			"\"type\": [{}], " +
//			"\"key\": " +
//				"[{ \"namespace\": \"/wikipedia/en\", " +
//				"\"value\": \""+ wikipediaTitle +"\" }] }]";
//			String queryEnvelope = "{\"query\":" + query + "}";
//			String serviceUrl = "http://api.freebase.com/api/service/mqlread";
//			String url = serviceUrl  + "?query=" + URLEncoder.encode(queryEnvelope, "UTF-8");
			
			// set Freebase query the new way using the MQL-read API
			String query = "[{ \"name\": null, " +
			"\"id\": null, " + 
			"\"type\": [{}], " +
			"\"key\": " +
				"[{ \"namespace\": \"/wikipedia/en\", " +	// TODO this part would be different for another source than WP
				"\"value\": \"" + wikipediaTitle +"\" }] }]";
//			String url = URL_MQL + "?query=" + URLEncoder.encode(query, "UTF-8");
			String url = FbCommonTools.URL_MQL + "?key=" + FbCommonTools.getKey() + "&query=" + URLEncoder.encode(query, "UTF-8");
			
			logger.log(query);
			logger.log(url);
			
			result = new ArrayList<String>();
			String freebaseId = null;
			
			// get Freebase answer
			HttpClient httpclient = new DefaultHttpClient();   
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpclient.execute(request);
			
			// builds object from answer
			JSONParser parser = new JSONParser();
			HttpEntity entity = response.getEntity();
			String str = EntityUtils.toString(entity);
			JSONObject jsonData = (JSONObject)parser.parse(str);
			JSONArray answer = (JSONArray)jsonData.get("result");
			
			// extract types from the answer
			if(answer!=null)
			{	for (Object object : answer)
				{	// process id
					freebaseId = (String) ((JSONObject)object).get("id");
					// process types
					JSONArray types = (JSONArray) ((JSONObject)object).get("type");
					for(Object type: types)
					{	result.add(((JSONObject)type).get("id").toString());
					}
				}
			}
			
			// possibly cache the result
			if(FbCommonTools.cache)
			{	// cache types
				allTypes.putValues(wikipediaTitle,result);
				// cache Freebase id
				if(freebaseId!=null)
					FbIdTools.setId(wikipediaTitle,freebaseId);
			}
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// NOTABLE TYPES		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Symbol representing the absence of any notable type */
	private static final String NONE = "NONE";
	/** Map used as a memory cache for Freebase queries */
	private static FbCache notableTypes;
	
	/**
	 * This method takes the title of a Wikipedia page,
	 * and retrieves its notable type, according to FreeBase.
	 * <br/>
	 * This type must then be processed in order to
	 * get the corresponding {@link EntityType}.
	 * 
	 * @param wikipediaTitle
	 * 		Title of the Wikipedia article.
	 * @return
	 * 		A String describing a FB type.
	 * 
	 * @throws IOException 
	 * 		Problem while retrieving the FB types.
	 * @throws ClientProtocolException 
	 * 		Problem while retrieving the FB types.
	 * @throws org.json.simple.parser.ParseException 
	 * 		Problem while retrieving the FB types.
	 * @throws ParseException
	 * 		Problem while retrieving the FB types.
	 */
	public static String getNotableType(String wikipediaTitle) throws ClientProtocolException, IOException, ParseException, org.json.simple.parser.ParseException  
	{	logger.increaseOffset();
		String result = null;
		
		// possibly get result from cache
		if(FbCommonTools.cache)
		{	if(notableTypes==null)
				notableTypes = new FbCache(FileNames.FI_NOTABLE_TYPES);
			result = notableTypes.getValue(wikipediaTitle);
		}
		
		if(result==null)
		{	// set Freebase query using the Topic API
			String fbId = FbIdTools.getId(wikipediaTitle);
			String filter = "filter=/common/topic/notable_types";
			String url = FbCommonTools.URL_TOPIC + fbId + "?key=" + FbCommonTools.getKey() + "&" + filter;
			logger.log(url);
			
			// get Freebase answer
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpclient.execute(request);
			
			// check if there is an answer
			StatusLine status = response.getStatusLine();
			int code = status.getStatusCode();
			if(code<400)
			{	// builds object from answer
				JSONParser parser = new JSONParser();
				HttpEntity entity = response.getEntity();
				String str = EntityUtils.toString(entity);
				JSONObject jsonData = (JSONObject)parser.parse(str);
				
				// extract types from the answer
				if(jsonData!=null)
				{	JSONObject property = (JSONObject) jsonData.get("property");
					if(property!=null)
					{	JSONObject notable = (JSONObject) property.get("/common/topic/notable_types");
						JSONArray values = (JSONArray) notable.get("values");
						JSONObject value = (JSONObject)values.get(0);
						result = (String) value.get("id");
					}
				}
			}
			
			// possibly cache the result
			if(result==null)
				result = NONE;
			if(FbCommonTools.cache)
			{	notableTypes.putValue(wikipediaTitle,result);
			}
		}
		
		if(result.equals(NONE))
			result = null;
		if(result==null)
			logger.log("WARNING: No notable type could be retrieved for \""+wikipediaTitle+"\"");
		
		logger.decreaseOffset();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// TYPE LIST	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Sends the appropriate request in order to retrieve all the common
	 * Freebase domains and the corresponding Freebase types. Returns
	 * a collection of Freebase types.
	 * 
	 * @return
	 * 		A set of Freebase types.
	 * 
	 * @throws ClientProtocolException
	 * 		Problem while accessing Freebase.
	 * @throws IOException
	 * 		Problem while accessing Freebase.
	 * @throws org.json.simple.parser.ParseException
	 * 		Problem while accessing Freebase.
	 */
	public static Set<String> retrieveDomainTypes() throws ClientProtocolException, IOException, org.json.simple.parser.ParseException
	{	logger.increaseOffset();
		
		// get all common domains
		logger.log("Retrieve all common domains");
		logger.increaseOffset();
		Set<String> domains = new TreeSet<String>();
		{	String query = "[{ \"name\": null, " +
			"\"id\": null, " + 
			"\"name\": null, " + 
			"\"type\": \"/freebase/domain_profile\", " +
			"\"category\": " +
				"{ \"id\": \"/category/commons\" } }]";
			String url = FbCommonTools.URL_MQL + "?key=" + FbCommonTools.getKey() + "&query=" + URLEncoder.encode(query, "UTF-8");
			logger.log(query);
			logger.log(url);
			
			// get Freebase answer
			HttpClient httpclient = new DefaultHttpClient();   
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpclient.execute(request);
			
			// builds object from answer
			JSONParser parser = new JSONParser();
			HttpEntity entity = response.getEntity();
			String str = EntityUtils.toString(entity);
			JSONObject jsonData = (JSONObject)parser.parse(str);
			JSONArray answer = (JSONArray)jsonData.get("result");
			
			// extract types from the answer
			if(answer!=null)
			{	for (Object object : answer)
				{	domains.add(((JSONObject)object).get("id").toString());
				}
			}
		}
		logger.log("Retrieved "+domains.size()+" domains");
		logger.decreaseOffset();
		
		// for each domain, retrieve all the types
		logger.log("Retrieve all the types of these domains");
		logger.increaseOffset();
		Set<String> result = new TreeSet<String>();
		{	logger.log("Processing each domain separately");
			logger.increaseOffset();
			for(String domain: domains)
			{	logger.log("Processing domain '"+domain+"'");
				Set<String> types = new TreeSet<String>();
				String query = "[{ \"name\": null, " +
				"\"id\": null, " + 
				"\"name\": null, " + 
				"\"type\": \"/type/type\", " +
				"\"domain\": \""+domain+"\" }]";
				String url = FbCommonTools.URL_MQL + "?key=" + FbCommonTools.getKey() + "&query=" + URLEncoder.encode(query, "UTF-8");
				logger.log(query);
				logger.log(url);
				
				// get Freebase answer
				HttpClient httpclient = new DefaultHttpClient();   
				HttpGet request = new HttpGet(url);
				HttpResponse response = httpclient.execute(request);
				
				// builds object from answer
				JSONParser parser = new JSONParser();
				HttpEntity entity = response.getEntity();
				String str = EntityUtils.toString(entity);
				JSONObject jsonData = (JSONObject)parser.parse(str);
				JSONArray answer = (JSONArray)jsonData.get("result");
				
				// extract types from the answer
				if(answer!=null)
				{	for (Object object : answer)
					{	types.add(((JSONObject)object).get("id").toString());
					}
				}
				
				logger.log("Retrieved "+types.size()+" types for domain '"+domain+"'");
				result.addAll(types);
			}
			logger.decreaseOffset();
		}
		logger.log("Retrieved "+result.size()+" types in total");
		logger.decreaseOffset();
		
		logger.log("Type retrieval complete");
		logger.decreaseOffset();
		return result;
	}
}
