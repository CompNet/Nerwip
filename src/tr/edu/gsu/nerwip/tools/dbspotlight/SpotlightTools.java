package tr.edu.gsu.nerwip.tools.dbspotlight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
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



@SuppressWarnings("javadoc")
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
	    	logger.log("xmlText " + xmlText);
	    	  
	    	String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	    	xmlText = xmlText.replace(xml, "");
	    	logger.log("xmlText " + xmlText);
	    	
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
		//URI Spotlight disambiguation
        String service= "http://spotlight.dbpedia.org/rest/disambiguate";
        //double CONFIDENCE = 0.0;
   	    //int SUPPORT = 0;
        //String ID = null;
        String answer = null;
        
        try 
        {
        	HttpClient httpclient = new DefaultHttpClient();
   	        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
   	        //params.add(new BasicNameValuePair("confidence", "0.1")); 
   	        //params.add(new BasicNameValuePair("support", "10")); 
   	   
   	        params.add(new BasicNameValuePair("Accept", "application/json"));
   	        params.add(new BasicNameValuePair("output", "xml"));
   	   
   	        params.add(new BasicNameValuePair("text", text));
   	        params.add(new BasicNameValuePair("url", service));
    
   	        HttpGet httpget = new HttpGet(service+"?"+URLEncodedUtils.format(params, "utf-8"));
   	  
   	        HttpResponse response = httpclient.execute(httpget);
   	        logger.log( response.toString());
   	   
   	        InputStream stream = response.getEntity().getContent();
   		    InputStreamReader streamReader = new InputStreamReader(stream,"UTF-8");
   		    BufferedReader bufferedReader = new BufferedReader(streamReader);
   		
   		    // read Spotlight response
   		    logger.log("Spotlight answer");
   		    StringBuilder builder = new StringBuilder();
   		    String line;
   		    while((line = bufferedReader.readLine())!=null)
   		    {
   		    	builder.append(line+"\n");
   			    logger.log("Line:" +line);
   			    
   		    }
   		    answer = builder.toString();
        }
        
        catch (ClientProtocolException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	    } catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    		
    	    }
		
		 
        
        logger.log("end of disambiguation");
        return answer;
	
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
