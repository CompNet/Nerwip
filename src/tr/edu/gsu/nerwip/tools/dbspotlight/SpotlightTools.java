package tr.edu.gsu.nerwip.tools.dbspotlight;

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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class acts as an interface with the Dbpedia Spotlight Web service.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class SpotlightTools {
	
/////////////////////////////////////////////////////////////////
//LOGGING			/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
/** Common object used for logging */
protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();


/////////////////////////////////////////////////////////////////
// XML NAMES		/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
/** Element containing the list of all entities resources */
private final static String ELT_RESOURCES = "Resources";
/** Element containing the list of informations of every entity resource */
private final static String ELT_RESOURCE = "Resource";


/** Attribute representing the name of an entity */
private final static String ATT_NAME = "surfaceForm";
/** Attribute representing the type of an entity */
private final static String ATT_TYPE = "types";
/** Attribute representing the id of an entity */
private final static String ATT_URI = "URI";

/////////////////////////////////////////////////////////////////
// PROCESSING	 		/////////////////////////////////////////
/////////////////////////////////////////////////////////////////
/** Web service URL */
private static final String SERVICE_URL = "http://spotlight.dbpedia.org/rest/disambiguate";

/////////////////////////////////////////////////////////////////
// PROCESS			/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////

/**
 * Receives an article and entities and
 * construct the xml text needed for 
 * desambiguation.
 *  
 * @param entities
 * 		Entities detected in the article.
 * @param article
 * 		Article to process.
 * @return
 * 		The xml text.
 */
	public static String process(Entities entities, Article article) 
	{   logger.increaseOffset();
	    String textt = article.getRawText();
	   
 
        //entities
		List<AbstractEntity<?>> entityList = entities.getEntities();
		
	    //creating xml objects 
	    Element racine = new Element("annotation");
	    Document document = new Document(racine);
	    Attribute text = new Attribute("text",textt);
	    racine.setAttribute(text);
	    
	    logger.log("entitylist size= " + entityList.size() );
	     
	    for (int i=1; i<=entityList.size(); i++)
	    {
	    	ListIterator<AbstractEntity<?>> itr = entityList.listIterator(i);
	    	
	    	// get the entity	    	
		    AbstractEntity<?> entityy = itr.previous(); 

		    int startPos = entityy.getStartPos(); //offset
			String startPosition = String.valueOf(startPos);
		    String value = entityy.getStringValue();
		     
	    	Element surfaceForm = new Element(ATT_NAME);
	        Attribute name = new Attribute("name", value);
	        surfaceForm.setAttribute(name);
	      
	        Attribute offset = new Attribute("offset", startPosition);
	        surfaceForm.setAttribute(offset);
	        racine.addContent(surfaceForm);
	        
	    }
	    
	    //xml output
	   
	    	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
	    	String xmlText = outputter.outputString(document);
	    	//logger.log("xmlText " + xmlText);
	    	  
	    	String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	    	xmlText = xmlText.replace(xml, "");
	    	//logger.log("xmlText " + xmlText);
	    	
	    	logger.log("end of processing");
	    	return xmlText;
	    
	}
	
	/**
	 * Receives the xml text and
	 * return the result of disambiguation.
	 *  
	 * @param text
	 * 		Xml text.
	 * @return
	 * 		The result of disambiguation.
	 */
  
	public static String disambiguate(String text)
	
	{
		String spotlightResponse = null;

		try {
			logger.log("Define HTTP message for spotlight");
		
		    HttpPost method = new HttpPost(SERVICE_URL);
		    List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("confidence", "0.1")); 
	        params.add(new BasicNameValuePair("support", "10")); 
	        params.add(new BasicNameValuePair("Accept", "application/json"));
	        params.add(new BasicNameValuePair("output", "xml"));
            params.add(new BasicNameValuePair("text", text));
	        params.add(new BasicNameValuePair("url", SERVICE_URL));
            method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
	
		    logger.log("Send message to service");
		    HttpClient client = new DefaultHttpClient();
		    HttpResponse response;
	        response = client.execute(method);
	        int responseCode = response.getStatusLine().getStatusCode();
		    logger.log("Response Code : " + responseCode);
		
		    //read service answer
			logger.log("Read the spotlight answer");
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
			StringBuffer sb = new StringBuffer();
			String line;
		    while((line = reader.readLine()) != null)
			{
		    	//logger.log(line);
			    sb.append(line+"\n");
			    
			}
		    
		    spotlightResponse = sb.toString();
		    
		}
		
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return spotlightResponse;
		
	
	}
	
	
	
	public static List<String> getEntitySpotlight(String text)
	
	{   ArrayList<String> entityList = new ArrayList<String>();
		
		try
		{	// build DOM
			logger.log("Build DOM");
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(text));
			Element root = doc.getRootElement();
			
			Element resources = root.getChild(ELT_RESOURCES);
			List<Element> wordElts = resources.getChildren(ELT_RESOURCE);
			

			for(Element wordElt: wordElts)
			{	
			    String entityName = wordElt.getAttributeValue(ATT_NAME);
			    //logger.log("entityName= " + entityName);
			    entityList.add(entityName);
			}
			logger.log("entityList " + entityList.toString());
		}
        
        catch (JDOMException e)
		{	e.printStackTrace();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
        
        return entityList;

	}
	
	
	
	public static List<String> getIdSpotlight(String text)
	{
		
		ArrayList<String> idList = new ArrayList<String>();
		
        try
		{	// build DOM
			logger.log("Build DOM");
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(text));
			Element root = doc.getRootElement();
			
			Element resources = root.getChild(ELT_RESOURCES);
			List<Element> wordElts = resources.getChildren(ELT_RESOURCE);
			
			for(Element wordElt: wordElts)
			{	String uri = wordElt.getAttributeValue(ATT_URI); 
			    logger.log("uri= " + uri);
			    idList.add(uri);
			}	
		}
        
        catch (JDOMException e)
		{	e.printStackTrace();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
        
        
        return idList;
	}
	
	
	public static List<List<String>> getTypeSpotlight(String text)
	{
		List<List<String>> entityTypes = new ArrayList<List<String>>();
		
        try
		{	// build DOM
			logger.log("Build DOM");
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(text));
			Element root = doc.getRootElement();
			
			Element resources = root.getChild(ELT_RESOURCES);
			List<Element> wordElts = resources.getChildren(ELT_RESOURCE);
			List<String> L = new ArrayList<String>();
			for(Element wordElt: wordElts)
			{	
			    String types = wordElt.getAttributeValue(ATT_TYPE);
			    
			    //logger.log("types= " + types);
			    String[] splitArray = types.split(",");
			    
			    for(int i = 0; i< splitArray.length;i++){
			    	   L.add(splitArray[i]);

			    	  }
			   // entityTypes.add(L);
			   //logger.log("entityTypes " + entityTypes.toString());
			}
			entityTypes.add(L);
			logger.log("entityTypes " + entityTypes.toString());
	
		}
        
        catch (JDOMException e)
		{	e.printStackTrace();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
        
        
        return entityTypes;
	}
	
}
