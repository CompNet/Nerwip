package tr.edu.gsu.nerwip.tools.freebase;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class handles Freebase ids, and more particularly
 * the mapping between Wikipedia article titles and Freebase
 * ids.
 * 
 * @author Vincent Labatut
 */
public class FbIdTools
{	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// WIKIPEDIA TO FREEBASE	/////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map used as a memory cache for Freebase queries */
	private static FbCache wp2fb;
	
	/**
	 * This method takes the title of a Wikipedia page,
	 * and retrieves its Freebase id.
	 * 
	 * @param wikipediaTitle
	 * 		Title of the Wikipedia article.
	 * @return
	 * 		A String describing a the FB id.
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
	public static String getId(String wikipediaTitle) throws ClientProtocolException, IOException, ParseException, org.json.simple.parser.ParseException  
	{	logger.increaseOffset();
		String result = null;
		
		// possibly get result from cache
		if(FbCommonTools.cache)
		{	if(wp2fb==null)
				wp2fb = new FbCache(FileNames.FI_IDS);
			result = wp2fb.getValue(wikipediaTitle);
		}
		
		if(result==null)
		{	// set Freebase query using the MQL-read API
			String query = "[{ \"name\": null, " +
			"\"id\": null, " + 
			"\"key\": " +
				"[{ \"namespace\": \"/wikipedia/en\", " +	// TODO this part would be different for another source than WP
				"\"value\": \"" + wikipediaTitle +"\" }] }]";
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
				{	// process id
					result = (String) ((JSONObject)object).get("id");
				}
			}
			
			// possibly cache the result
			if(FbCommonTools.cache)
			{	if(result!=null)
					wp2fb.putValue(wikipediaTitle,result);
			}
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Sets the Freebase id of the specified article.
	 * 
	 * @param wikipediaTitle
	 * 		Title of the concerned article.
	 * @param freebaseId
	 * 		Corresponding Freebase id.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the cache file. 
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the cache file. 
	 */
	public static void setId(String wikipediaTitle, String freebaseId) throws FileNotFoundException, UnsupportedEncodingException
	{	if(FbCommonTools.cache)
		{	if(wp2fb==null)
				wp2fb = new FbCache(FileNames.FI_IDS);
			wp2fb.putValue(wikipediaTitle,freebaseId);
		}
	}
}
