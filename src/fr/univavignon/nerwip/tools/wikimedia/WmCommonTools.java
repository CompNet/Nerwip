package fr.univavignon.nerwip.tools.wikimedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.commons.math3.util.Combinations;
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
import org.jdom2.Namespace;
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

	/** Prefix for the URL used to access the links inside a Wikipedia disambiguation page */
	public static final String WIKIMEDIA_DISAMB_PREFIX = "https://";
	/** URL and parameters used to access the links inside a Wikipedia disambiguation page */
	public static final String WIKIMEDIA_DISAMB_PAGE = ".wikipedia.org/w/api.php?action=query&generator=links&format=xml&redirects=1&prop=pageprops&gpllimit=50&ppprop=wikibase_item&titles=";
	
	/////////////////////////////////////////////////////////////////
	// XML			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Element representing a WikiData entity */
	private static final String ELT_ENTITY = "entity";
	/** Element representing a matching Wikipedia page */
	private static final String ELT_MATCH = "match";
	/** Element representing a link in a Wikipedia page */
	private static final String ELT_PAGE = "page";
	/** Element representing the property associated to a Wikipedia link */
	private static final String ELT_PAGEPROPS = "pageprops";
	/** Element representing a list of links in a Wikipedia page */
	private static final String ELT_PAGES = "pages";
	/** Element representing the result of a query */
	private static final String ELT_QUERY = "query";
	/** Element representing the result of a web search */
	private static final String ELT_SEARCH = "search";
	
	/** Attribute representing a WikiData id */
	private static final String ATT_ID = "id";
	/** Attribute representing the text description of a WikiData entity */
	private static final String ATT_DESC = "description";
	/** Attribute representing the title of a Wikipedia page */
	private static final String ATT_LABEL = "label";
	/** Attribute representing the title of a Wikipedia page */
	private static final String ATT_TEXT = "text";
	/** Attribute representing the title of a Wikipedia page */
	private static final String ATT_TITLE = "title";
	/** Attribute representing the id associated to a Wikipedia page */
	private static final String ATT_WIKIBASE_ITEM = "wikibase_item";
	
	/** Marker of a disambiguation page in a textual description */
	private final static String VAL_DISAMB = "disambiguation";
	
	/** URI for the WikiMedia API XML namespace */
	private final static String NS_WM_API = "http://www.mediawiki.org/xml/api/";
	
	/////////////////////////////////////////////////////////////////
	// ENTITY ID	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	
	
	
	public static void lookupName(String name, EntityType type, ArticleLanguage language) throws ClientProtocolException, IOException, JDOMException
	{	logger.log("Looking for entity "+name+" (in "+language+", as a "+type+")");
		logger.increaseOffset();
		
		 // 1) perform the approximate search using the websearch API and get the ids
		 // 2) if there is a desambiguisation page, also get its ids and treat it.
		 // 3) if there is nothing, try the variants of the name (if person): this was done before. look for "firstname" in the source code.
		 // 4) use the SPARQL API to perform a more precise search among the ids
		 // 5) extract all relevant information from these results  
	
		// set up the list of alternative names
		List<String> possibleNames;
		if(type==EntityType.PERSON)
			possibleNames = getPossibleNames(name);
		else
		{	possibleNames = new ArrayList<String>();
			possibleNames.add(name);
		}
		
		// get the entity ids associated to the possible names
		Map<String,String> candidateIds = retrieveIdsFromName(possibleNames,language);
		// filter them to keep only the most relevant one
		String selectedId = filterIds(possibleNames,candidateIds);
		
		// retrieve the details associated to the remaining id
		retrieveDetailsFromId(selectedId);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Returns a map of WikiData ids likely to correspond to the specified entity,
	 * described by its name (surface form), type and language.
	 * 
	 * @param possibleNames
	 * 		List of possible surface form representing the entity.
	 * @param language
	 * 		Language of the surface form.
	 * @return
	 * 		A map of strings associating WikiData ids to Wikipedia page titles, 
	 * 		in no particular order.
	 * 
	 * @throws ClientProtocolException
	 * 		Problem while accessing the service.
	 * @throws IOException
	 * 		Problem while accessing the service.
	 * @throws JDOMException
	 * 		Problem while parsing the service response.
	 */
	private static Map<String,String> retrieveIdsFromName(List<String> possibleNames, ArticleLanguage language) throws ClientProtocolException, IOException, JDOMException
	{	logger.log("Retrieving ids for entity "+possibleNames.get(0)+" (in "+language+")");
		logger.increaseOffset();
		Map<String,String> result = new HashMap<String,String>();
		
		String baseUrl = WIKIDATA_WEBSEARCH_URL 
				+ WIKIDATA_WEBSEARCH_PARAM_LANG + language.toString().toLowerCase(Locale.ENGLISH)
				+ WIKIDATA_WEBSEARCH_PARAM_SEARCH;
		
		// process each possible name
		Iterator<String> it = possibleNames.iterator();
		do
		{	String candidateName = it.next();
			logger.log("Processing possible name "+candidateName);
			logger.increaseOffset();
		
			// request the server
			String url = baseUrl + URLEncoder.encode(candidateName, "UTF-8");
			logger.log("URL: "+url);
			HttpClient httpclient = new DefaultHttpClient();   
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpclient.execute(request);
			
			// parse the answer to get an XML document
			String answer = WebTools.readAnswer(response);
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(answer));
			Element root = doc.getRootElement();
			
			// retrieve the ids and check for disambiguation pages
			Namespace ns = Namespace.getNamespace(NS_WM_API);
			Element searchElt = root.getChild(ELT_SEARCH,ns);
			List<Element> entityElts = searchElt.getChildren(ELT_ENTITY,ns);
			int i = 1;
			for(Element entityElt: entityElts)
			{	logger.log("Entity "+i+"/"+entityElts.size());
				logger.increaseOffset();

				String description = entityElt.getAttributeValue(ATT_DESC);
				String label = entityElt.getAttributeValue(ATT_LABEL);
				if(label==null)
				{	Element matchElt = entityElt.getChild(ELT_MATCH,ns);
					label = matchElt.getAttributeValue(ATT_TEXT);
					logger.log("Description="+description+" text="+label);
				}
				else
					logger.log("Description="+description+" label="+label);

				// if it is a disambiguation page, we must retrieve the entities it contains
				if(description!=null && description.toLowerCase().contains(VAL_DISAMB))
				{	logger.log("It is a description page");
					Map<String,String> tmpMap = retrieveIdsFromDisambiguation(language,label);
					for(Entry<String,String> tmpEntry: tmpMap.entrySet())
					{	String tmpKey = tmpEntry.getKey();
						String tmpVal = tmpEntry.getValue();
						if(!result.keySet().contains(tmpKey))
							result.put(tmpKey,tmpVal);
					}
				}
				// if not a disambiguation page, we directly add the entity to the map
				else
				{	String id = entityElt.getAttributeValue(ATT_ID);
					logger.log("Not a description page: adding "+id+" to the map (if not already present)");
					if(!result.keySet().contains(id))
						result.put(id,label);
				}

				i++;
				logger.decreaseOffset();
			}
			
			logger.decreaseOffset();
		}
		while(result.isEmpty() && it.hasNext());
//		while(it.hasNext()); //for testing
		
		logger.log("Done: "+result.size()+" ids found in total");
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Receives the label (title) of the Wikipedia disambiguation page, as well as its language.
	 * Returns a map containing all the entities listed in this page, described by their Wikidata
	 * id and title. 
	 * 
	 * @param language
	 * 		Language of the disambiguation page.
	 * @param label
	 * 		Title of the disambiguation page.
	 * @return
	 * 		Map containing the entities listed in the disambiguation page. The keys are Wikidata
	 * 		ids and the values are Wikipedia titles.
	 * 
	 * @throws IOException 
	 * 		Problem while accessing the service.
	 * @throws IllegalStateException 
	 * 		Problem while accessing the service.
	 * @throws JDOMException 
	 * 		Problem while parsing the service response.
	 */
	private static Map<String,String> retrieveIdsFromDisambiguation(ArticleLanguage language, String label) throws IllegalStateException, IOException, JDOMException
	{	logger.log("Processing the disambiguation page "+label+" (in "+language+")");
		logger.increaseOffset();
		Map<String,String> result = new HashMap<String,String>();
		
		// query WikiMedia
		String url = WIKIMEDIA_DISAMB_PREFIX + language.toString().toLowerCase() + WIKIMEDIA_DISAMB_PAGE + URLEncoder.encode(label,"UTF-8");
		logger.log("URL: "+url);
		HttpClient httpclient = new DefaultHttpClient();   
		HttpGet request = new HttpGet(url);
		HttpResponse response = httpclient.execute(request);
		
		// read the answer as an XML document
		String answer = WebTools.readAnswer(response);
		SAXBuilder sb = new SAXBuilder();
		Document doc = sb.build(new StringReader(answer));
		Element root = doc.getRootElement();
		
		// extract the ids from the XML document
		Element queryElt = root.getChild(ELT_QUERY);
		Element pagesElt = queryElt.getChild(ELT_PAGES);
		List<Element> pageElts = pagesElt.getChildren(ELT_PAGE);
		for(Element pageElt: pageElts)
		{	Element propsElt = pageElt.getChild(ELT_PAGEPROPS);
			if(propsElt!=null)
			{	String id = propsElt.getAttributeValue(ATT_WIKIBASE_ITEM);
				String title = pageElt.getAttributeValue(ATT_TITLE);
				result.put(id,title);
				logger.log("Found a possible entity, title="+title+" id="+id);
			}
		}
		
		logger.log("Finished: "+result.size()+" ids found");
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Filters the map of ids previously processed. Those ids are all likely
	 * to represent the targeted entity. We use the (ordered) list of possible
	 * names to select the best candidate.
	 * 
	 * @param possibleNames
	 * 		List of possible names for the entity, order by decreasing relevance.
	 * @param ids
	 * 		Maps of ids retrieved from Wikipedia/Wikidata.
	 * @return
	 * 		The best candidate id.
	 */
	private static String filterIds(List<String> possibleNames, Map<String,String> ids)
	{	logger.log("Filtering the "+ids.size()+" ids found earlier");
		logger.increaseOffset();
		String result = null;
		
		// possibly process each variant of the entity name
		Iterator<String> it = possibleNames.iterator();
		while(it.hasNext() && result==null)
		{	String possibleName = it.next();
			logger.log("Checking the map for name "+possibleName);
		
			// look for the possible name in the map of available ids
			List<String> idList = new ArrayList<String>();
			for(Entry<String,String> entry: ids.entrySet())
			{	String id = entry.getKey();
				String name = entry.getValue();
				if(name.equalsIgnoreCase(possibleName) && !idList.contains(id))
					idList.add(id);
			}
			
			// if more than one id, warn the user and keep the first one
			if(!idList.isEmpty())
			{	result = idList.get(0);
				if(idList.size()>1)
				{	logger.log("WARNING: several ids were found for entity "+possibleNames.get(0)+"(name \""+possibleName+"\")");
					logger.increaseOffset();
						logger.log(idList);
					logger.decreaseOffset();
				}
			}
		}
		
		if(result==null)
			logger.log("Done: no appropriate id found");
		else
			logger.log("Done: kept id "+result);
		logger.decreaseOffset();
		return result;
	}
	
	private static void retrieveDetailsFromId(String id)
	{
		//TODO
	}
	
	/**
	 * Generates all possible human names from a string representing
	 * the full name. This methods allows considering various combinations
	 * of lastname(s) and firstname(s).
	 * 
	 * @param name
	 * 		The full name a string (should contain several names separated
	 * 		by spaces).
	 * @return
	 * 		A list of strings corresponding to alternative forms of the 
	 * 		original name.
	 */
	private static List<String> getPossibleNames(String name)
	{	List<String> result = new ArrayList<String>();
		result.add(name);
		String split[] = name.split(" ");
		
		for(int i=1;i<split.length;i++)
		{	// fix the last names
			String lastnames = "";
			for(int j=i;j<split.length;j++)
				lastnames = lastnames + split[j].trim() + " ";
			lastnames = lastnames.trim();
			
			// we try to fix the last names and get all combinations of firstnames 
			for(int j=1;j<i;j++)
			{	Combinations combi = new Combinations(i,j);
				Iterator<int[]> it = combi.iterator();
				while(it.hasNext())
				{	int indices[] = it.next();
					String firstnames = "";
					for(int index: indices)
						firstnames = firstnames + split[index].trim() + " ";
					String fullname = firstnames+lastnames;
					if(!result.contains(fullname))
						result.add(fullname);
				}
			}
			
			// we also try only the lastnames
			if(!result.contains(lastnames))
				result.add(lastnames);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// TESTS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    public static void main(String[] args) throws Exception
    {	// possible names
//    	System.out.println(getPossibleNames("Lastname")+"\n\n");
//    	System.out.println(getPossibleNames("Firstname Lastname")+"\n\n");
//    	System.out.println(getPossibleNames("Firstname Middlename Lastname")+"\n\n");
//    	System.out.println(getPossibleNames("Firstname Middlename Lastname1 Lastname2")+"\n\n");
//    	System.out.println(getPossibleNames("Firstname1 Firstname2 Middlename Lastname1 Lastname2")+"\n\n");
    	
    	// disambiguation page
//    	Map<String,String> res = retrieveIdsFromDisambiguation(ArticleLanguage.FR, "Lecointe");
//    	System.out.println(res);
    	
    	// retrieve the ids
//    	List<String> possibleNames = getPossibleNames("Adolphe Lucien Lecointe");
//    	Map<String,String> res = retrieveIdsFromName(possibleNames, ArticleLanguage.FR);
//    	System.out.println(res);
    	
    	// general lookup method
    	lookupName("Adolphe Lucien Lecointe", EntityType.PERSON, ArticleLanguage.FR);
	}
}
