package fr.univavignon.nerwip.tools.dbpedia;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class contains methods used to retrieve the DBpedia
 * types associated to entities. 
 * <br/>
 * TODO to be properly integrated in {@link DbpCommonTools}.
 * 
 * @author Sabrine Ayachi
 */
public class DbTypeTools 
{	
	/////////////////////////////////////////////////////////////////
	// PREFIXES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Prefix for representing the DBpedia RDF Schema vocabulary (RDFS) */
	private final static String PRE_RDFS = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
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
	 * This method takes a name of entity,
	 * and retrieves all its Wikidata types.
	 * <br/>
	 * Those types must then be processed in order to
	 * get the corresponding {@link EntityType} or
	 * article category.
	 * 
	 * @param entity
	 * 		Name of the entity.
	 * @return
	 * 		a List containing the DBpedia types of this entity.
	 */
	public static List<String> getAllTypes(String entity) 
	{	logger.increaseOffset();
		List<String> types = new ArrayList<String>();
   
		//adress of the SPARQL endpoint
		String service="http://fr.dbpedia.org/sparql";
   
		//SPARQL query
		String query = PRE_RDFS + PRE_DBR + PRE_DBO + "select ?type where {<http://fr.dbpedia.org/resource/" + entity + "> rdf:type ?type.}";  
   
		try
		{	HttpClient httpclient = new DefaultHttpClient();
			List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("query", query)); 
			params.add(new BasicNameValuePair("Accept", "application/json"));
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
			//logger.log("dbAnswer=" + dbAnswer);

			// build DOM
			logger.log("Build DOM");
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(dbAnswer));
			Element root = doc.getRootElement();
			List<Element> list = root.getChildren();
			//logger.log("size :" + list.size());
			Element results = list.get(1);
			//logger.log("results :" + results.getName());

			List<Element> list2 = results.getChildren();
			//logger.log("size list2 :" + list2.size()); 

			for (int i=0;  i < list2.size(); i++)
			{	Element result = list2.get(i);
				//logger.log("result :" + result.getName());
				List<Element> list3 = result.getChildren();
				Element element = list3.get(0);
				logger.log("type :" + element.getValue());
				String type = element.getValue();
				types.add(type);
			}

			logger.log("types :" + types);
		}
		catch (ClientProtocolException e) 
		{	// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) 
		{	// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JDOMException e) 
		{	// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.decreaseOffset();
		return types;
	}
}
