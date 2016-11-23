package fr.univavignon.extractor.temp.tools.dbpedia;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class handles DBpedia ids, and more particularly
 * the mapping between named entities and their DBpedia
 * ids.
 * 
 * @author Sabrine Ayachi
 */
public class DbIdTools 
{  
	/////////////////////////////////////////////////////////////////
	// PREFIXES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Prefix for representing article data */
    private final static String PRE_DBR = "PREFIX res: <http://dbpedia.org/resource/>";
    /** Prefix for representing the DBpedia ontology */
	private final static String PRE_DBO = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>";
	
    /////////////////////////////////////////////////////////////////
    //LOGGING			/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/**
	* This method takes an entity as parameter,
	* and retrieves its DBpedia id.
	* 
	* @param entity
	* 		Name of the entity.
	* @return
	* 		A String describing the DBpedia id.
	*/
	public static String getId(String entity) 
	{	logger.increaseOffset();
		String ID = null;

		//adress of the french SPARQL endpoint
		String service="http://fr.dbpedia.org/sparql";
   
		//SPARQL query
		String query = PRE_DBR + PRE_DBO + "select ?wikiPageID where {" + 
				"<http://fr.dbpedia.org/resource/" + entity + ">" +  
				"dbpedia-owl:wikiPageID ?wikiPageID." +
				"}";
		try
		{	HttpClient httpclient = new DefaultHttpClient();
			List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("query", query)); 
			// params.add(new BasicNameValuePair("Accept", "application/json"));
			params.add(new BasicNameValuePair("output", "xml"));

			HttpGet httpget = new HttpGet(service+"?"+URLEncodedUtils.format(params, "utf-8"));
			HttpResponse response = httpclient.execute(httpget);
			logger.log( response.toString());

			InputStream stream = response.getEntity().getContent();
			InputStreamReader streamReader = new InputStreamReader(stream,"UTF-8");
			BufferedReader bufferedReader = new BufferedReader(streamReader);

			// read DBanswer
			logger.log("DB answer");
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = bufferedReader.readLine())!=null)
			{	builder.append(line+"\n");
				//logger.log("Line:" +line);
			}
			String dbAnswer = builder.toString();
			logger.log("DBAnswer=" + dbAnswer);

			// build DOM
			logger.log("Build DOM");
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(dbAnswer));
			Element root = doc.getRootElement();
			List<Element> list = root.getChildren();
			//logger.log("size :" + list.size());
			Element results = list.get(1);
			logger.log("results :" + results.getName());

			List<Element> list2 = results.getChildren();
			//logger.log("size list2 :" + resultt.size());

			Element result = list2.get(0);
			logger.log("result :" + result.getName());

			List<Element> list3 = result.getChildren();
			Element wikiPageID = list3.get(0);
			logger.log("wikiPageID :" + wikiPageID.getValue());
			ID = wikiPageID.getValue();
		}
		catch (ClientProtocolException e) 
		{	// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) 
		{	// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JDOMException e) 
		{	// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.decreaseOffset();
		return ID;
	}
}
