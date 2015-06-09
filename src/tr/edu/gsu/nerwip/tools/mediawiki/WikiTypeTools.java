package tr.edu.gsu.nerwip.tools.mediawiki;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;

import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

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



/**
 * This class contains methods used to retrieve the Freebase
 * types associated to Wikipedia articles. 
 * 
 * @author Sabrine Ayachi
 *
 */
public class WikiTypeTools {
	
/////////////////////////////////////////////////////////////////
// LOGGING			/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
/** Common object used for logging */
protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();


/**
 * This method takes a name of entity,
 * and retrieves all its Wikidata types.
 * <br/>
 * Those types must then be processed in order to
 * get the corresponding {@link EntityType} or
 * article category.
 * 
 * @param entityy
 * 		Name of the entity.
 * @return
 * 		a List containing the Wikidata types of this entity.
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
public static List<String> getAllTypes(String entityy) throws ClientProtocolException, IOException   
{  logger.increaseOffset();
   List<String> result = null;
   
   String url ="http://www.wikidata.org/wiki/Q76";
	  
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
   
   return result;
   
}

}
