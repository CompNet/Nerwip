package fr.univavignon.extractor.temp.tools.mediawiki;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser; 

import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class handles Wikidata ids, and more particularly
 * the mapping between named entities and their Wikidata
 * ids.
 * 
 * @author Sabrine Ayachi
 */

public class WikiIdTools 
{	
    /////////////////////////////////////////////////////////////////
    // LOGGING			/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /** Common object used for logging */
    protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
    
    /**
    * This method takes an entity name as parameter,
    * and retrieves its Wikidata id.
    * 
    * @param entityName
    * 		Name of the entity.
    * @return
    * 		A String describing the wikidata id.
    * @throws IOException
    *      Problem while retrieving the Wikidata types. 
    * @throws ClientProtocolException 
    *      Problem while retrieving the Wikidata types.
    * @throws org.json.simple.parser.ParseException
    *      Problem while retrieving the Wikidata types. 
    * 
    */
    public static String getId(String entityName) throws ClientProtocolException, IOException, org.json.simple.parser.ParseException 
    {	logger.increaseOffset();
    	String result = null;

    	String url ="https://www.wikidata.org/w/api.php?action=wbsearchentities&search=" +entityName + "&format=json&language=fr&type=item";

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