package fr.univavignon.nerwip.tools.wikimedia;

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

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.util.Combinations;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.AbstractNamedEntity;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.KnowledgeBase;
import fr.univavignon.nerwip.tools.file.FileNames;
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
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// CACHE		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not WikiMedia results should be cached */
	protected static boolean cache = true;
	
	/**
	 * Enable or disable the memory cache
	 * for WikiMedia requests.
	 *  
	 * @param enabled
	 * 		If {@code true}, the results from WikiMedia are
	 * 		stored in memory.
	 */
	public static void setCacheEnabled(boolean enabled)
	{	WmCommonTools.cache = enabled;
	}
	
	/////////////////////////////////////////////////////////////////
	// URL			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map used as a memory cache for WikiPedia queries */
	private static WmCache wikidataCache = null;
	/** URL used to access the web search API of WikiData */
	private static final String WIKIDATA_WEBSEARCH_URL ="https://www.wikidata.org/w/api.php?action=wbsearchentities&format=xml&includexmlnamespace=true&type=item&limit=max";
	/** Name of the parameter representing the searched string for the web search API of WikiData */
	private static final String WIKIDATA_WEBSEARCH_PARAM_SEARCH = "&search=";
	/** Name of the parameter representing the targeted language for the web search API of WikiData */
	private static final String WIKIDATA_WEBSEARCH_PARAM_LANG1 = "&language=";
	/** Name of the parameter representing the targeted language for the web search API of WikiData */
	private static final String WIKIDATA_WEBSEARCH_PARAM_LANG2 = "&uselang=";
	/** URL used to retrieve entities through the WikiData API */
	private static final String WIKIDATA_GETENT_URL ="https://www.wikidata.org/w/api.php?action=wbgetentities&format=xml&includexmlnamespace=true&redirects=yes";
	/** Name of the parameter representing the searched entity */
	private static final String WIKIDATA_GETENT_PARAM_SEARCH = "&ids=";
	/** URL used to query WikiData using SPARQL */
	private static final String WIKIDATA_SPARQL_URL ="https://query.wikidata.org/bigdata/namespace/wdq/sparql?query=";
	/** Second part of the URL used to query WikiData using SPARQL */
	private static final String WIKIDATA_SPARQL_URL_SUFFIX ="&format=xml";
	
	/** Map used as a memory cache for WikiPedia queries */
	private static WmCache wikimediaCache = null;
	/** Prefix for the URL used to access the links inside a Wikipedia disambiguation page */
	private static final String WIKIMEDIA_DISAMB_PREFIX = "https://";
	/** URL and parameters used to access the links inside a Wikipedia disambiguation page */
	private static final String WIKIMEDIA_DISAMB_PAGE = ".wikipedia.org/w/api.php?action=query&generator=links&format=xml&redirects=1&prop=pageprops&gpllimit=50&ppprop=wikibase_item&titles=";
	
	/////////////////////////////////////////////////////////////////
	// XML			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Element representing a result from a SPARQL query */
	private static final String ELT_BINDING = "binding";
	/** Element representing a WikiData entity */
	private static final String ELT_CLAIM = "claim";
	/** Element representing a WikiData entity */
	private static final String ELT_CLAIMS = "claims";
	/** Element representing a WikiData entity */
	private static final String ELT_DATAVALUE = "datavalue";
	/** Element representing a WikiData entity */
	private static final String ELT_ENTITIES = "entities";
	/** Element representing a WikiData entity */
	private static final String ELT_ENTITY = "entity";
	/** Element representing a value returned by WikiData */
	private static final String ELT_LITERAL = "literal";
	/** Element representing a WikiData entity */
	private static final String ELT_MAINSNAK = "mainsnak";
	/** Element representing a matching Wikipedia page */
	private static final String ELT_MATCH = "match";
	/** Element representing a link in a Wikipedia page */
	private static final String ELT_PAGE = "page";
	/** Element representing the property associated to a Wikipedia link */
	private static final String ELT_PAGEPROPS = "pageprops";
	/** Element representing a list of links in a Wikipedia page */
	private static final String ELT_PAGES = "pages";
	/** Element representing a WikiData entity */
	private static final String ELT_PROPERTY = "property";
	/** Element representing the result of a query */
	private static final String ELT_QUERY = "query";
	/** Element representing a result from a SPARQL query */
	private static final String ELT_RESULT = "result";
	/** Element representing results from a SPARQL query */
	private static final String ELT_RESULTS = "results";
	/** Element representing the result of a web search */
	private static final String ELT_SEARCH = "search";
	/** Element representing a result from a SPARQL query */
	private static final String ELT_URI = "uri";

	/** Attribute representing a WikiData id */
	private static final String ATT_ID = "id";
	/** Attribute representing the text description of a WikiData entity */
	private static final String ATT_DESC = "description";
	/** Attribute representing the title of a Wikipedia page */
	private static final String ATT_LABEL = "label";
	/** Attribute representing the language of some Wikidata text */
	private static final String ATT_LANGUAGE = "language";
	/** Attribute representing the title of a Wikipedia page */
	private static final String ATT_TEXT = "text";
	/** Attribute representing the title of a Wikipedia page */
	private static final String ATT_TITLE = "title";
	/** Attribute representing the value of a property in a WikiData response */
	private static final String ATT_VALUE = "value";
	/** Attribute representing the id associated to a Wikipedia page */
	private static final String ATT_WIKIBASE_ITEM = "wikibase_item";
	
	/** Marker of a disambiguation page in a textual description */
	private final static String VAL_DISAMB = "disambiguation";
	
	/** URI for the WikiMedia API XML namespace */
	private final static String NS_WM_API = "http://www.mediawiki.org/xml/api/";
	/** URI for the WikiMedia SPARQL API XML namespace */
	private final static String NS_SPARQL_API = "http://www.w3.org/2005/sparql-results#";
	
	/////////////////////////////////////////////////////////////////
	// WIKIDATA IDS 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Id of the "role" entity in WikiData */
	private final static String ENTITY_FUNCTION = "Q214339";		// alt: position=Q4164871
	/** Id of the "location" entity in WikiData */
	private final static String ENTITY_LOCATION = "Q17334923";	// alt: place=Q2221906
	/** Id of the "event" entity in WikiData */
	private final static String ENTITY_MEETING = "Q1656682";		// alt: meeting=Q2761147
	/** Id of the "person" entity in WikiData */
	private final static String ENTITY_PERSON = "Q215627";		// alt: human=Q5
	/** Id of the "organization" entity in WikiData */
	private final static String ENTITY_ORGANIZATION = "Q43229";
	/** Id of the "work" entity in WikiData */
	private final static String ENTITY_PRODUCTION = "Q386724";	// alt: artificial object=Q16686448 (artificial object is too general: France is considered as one).
	/** Id of the "unique identifier" entity in WikiData */
	private final static String ENTITY_IDENTIFIER = "Q6545185";	// alt: identifier=Q853614
	
	/** Id of the "instance of" property in WikiData */
	private final static String PROP_INSTANCE_OF = "P31";
	/** Id of the "subclass of" property in WikiData */
	private final static String PROP_SUBCLASS_OF = "P279";
	/** Id of the "facet of" property in WikiData */
	private final static String PROP_FACET_OF = "P1269";
	
	/////////////////////////////////////////////////////////////////
	// SPARQL QUERIES 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map used as a memory cache for Sparql queries */
	private static WmCache sparqlCache = null;
	/** First part of the SPARQL query used to retrieve the types of an entity */ 
	public static final String WIKIMEDIA_QUERY_TYPES_PREFIX = "SELECT DISTINCT ?type WHERE {"
			+ " VALUES ?type {"
				+ " wd:" + ENTITY_FUNCTION
				+ " wd:" + ENTITY_LOCATION
				+ " wd:" + ENTITY_MEETING
				+ " wd:" + ENTITY_PERSON
				+ " wd:" + ENTITY_ORGANIZATION
				+ " wd:" + ENTITY_PRODUCTION 
				+ "}. wd:";
	/** Second part of the SPARQL query used to retrieve the types of an entity */ 
	private static final String WIKIMEDIA_QUERY_TYPES_SUFFIX = " wdt:"+PROP_INSTANCE_OF+"/wdt:"+PROP_SUBCLASS_OF+"* ?type.}";
	/** First part of the SPARQL query used to retrieve the ids of an entity */ 
	private static final String WIKIMEDIA_QUERY_EXTIDS_PREFIX = "SELECT DISTINCT ?prop ?propLabel ?value WHERE {"
			+ "wd:";
	/** Second part of the SPARQL query used to retrieve the ids of an entity */ 
	private static final String WIKIMEDIA_QUERY_EXTIDS_SUFFIX = " ?p ?value. "
			+ "?prop wikibase:directClaim ?p. "
			+ "?prop wdt:"+PROP_INSTANCE_OF+"/wdt:"+PROP_SUBCLASS_OF+"*/wdt:"+PROP_FACET_OF+" wd:"+ENTITY_IDENTIFIER+". "
			+ "SERVICE wikibase:label {"
			+ "bd:serviceParam wikibase:language \"en\" ."
			+ "}}";
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION MAP 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of Knowledge base we want to completely ignore (this just affects the log) */
	private final static List<String> IGNORED_KB = Arrays.asList(
		"Digital Atlas of the Roman Empire ID",
		"MusicBrainz area ID",
		"SANDRE ID"
	);
	/** Map allowing to convert a WikiData property to a knowledge base name */
	private final static Map<String,KnowledgeBase> MAP_ID_TO_KB = new HashMap<String,KnowledgeBase>();
	/** Initialization of the conversion map */
	static
	{	MAP_ID_TO_KB.put("P268",  KnowledgeBase.BNF);
		MAP_ID_TO_KB.put("P1036", KnowledgeBase.DEWEY);
		MAP_ID_TO_KB.put("P1417", KnowledgeBase.ENCYC_BRIT);
		MAP_ID_TO_KB.put("P1997", KnowledgeBase.FACEBOOK_PLACES);
		MAP_ID_TO_KB.put("P646",  KnowledgeBase.FREEBASE);
		MAP_ID_TO_KB.put("P1566", KnowledgeBase.GEONAMES);
		MAP_ID_TO_KB.put("P227",  KnowledgeBase.GND);
		MAP_ID_TO_KB.put("P1296", KnowledgeBase.GRAN_ENCIC_CATAL);
		MAP_ID_TO_KB.put("P374",  KnowledgeBase.INSEE_MUNICIP);
		MAP_ID_TO_KB.put("P244",  KnowledgeBase.LIB_CONGR);
		MAP_ID_TO_KB.put("P281",  KnowledgeBase.POSTAL_CODE);
		MAP_ID_TO_KB.put("P1808", KnowledgeBase.SENAT_FR);
		MAP_ID_TO_KB.put("P269",  KnowledgeBase.SUDOC);
		MAP_ID_TO_KB.put("P1045", KnowledgeBase.SYCOMORE);
		MAP_ID_TO_KB.put("P214",  KnowledgeBase.VIAF);
		
	}
	
	/** Map allowing to convert a WikiData property to an entity type*/
	private final static Map<String,EntityType> MAP_ID_TO_TYPE = new HashMap<String,EntityType>();
	/** Initialization of the conversion map */
	static
	{	//MAP_ID_TO_TYPE.put("", EntityType.DATE);
		MAP_ID_TO_TYPE.put(ENTITY_FUNCTION, EntityType.FUNCTION);
		MAP_ID_TO_TYPE.put(ENTITY_LOCATION, EntityType.LOCATION);
		MAP_ID_TO_TYPE.put(ENTITY_MEETING, EntityType.MEETING);
		MAP_ID_TO_TYPE.put(ENTITY_ORGANIZATION, EntityType.ORGANIZATION);
		MAP_ID_TO_TYPE.put(ENTITY_PERSON, EntityType.PERSON);
		MAP_ID_TO_TYPE.put(ENTITY_PRODUCTION, EntityType.PRODUCTION);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Receives an incomplete entity, and try to find out its WikiData id.
	 * Also complete the entity with various details, in particular the other
	 * ids indicated on WikiData.
	 * 
	 * @param entity
	 * 		The entity to complete.
	 * @param language
	 * 		Language of the article containing a mention to this entity.
	 * 
	 * @throws ClientProtocolException
	 * 		Problem while accessing a WikiData or WikiMedia service.
	 * @throws IOException
	 * 		Problem while accessing a WikiData or WikiMedia service.
	 * @throws JDOMException
	 * 		Problem while parsing the XML file constituting the service response.
	 */
	public static void lookupNamedEntity(AbstractNamedEntity entity, ArticleLanguage language) throws ClientProtocolException, IOException, JDOMException
	{	Set<String> names = entity.getSurfaceForms();
		EntityType type = entity.getType();
		logger.log("Looking for entity "+entity.getName()+" (in "+language+", as a "+type+")");
		logger.increaseOffset();
		
		// set up the list of alternative names
		List<String> possibleNames;
		if(type==EntityType.PERSON)
			possibleNames = getPossibleNames(names);
		else
		{	possibleNames = new ArrayList<String>();
			possibleNames.addAll(names);
		}
		
		// get the entity ids associated to the possible names
		Map<String,String> candidateIds = retrieveIdsFromName(possibleNames,language);
		// filter them to keep only the most relevant one
		String selectedId = filterIds(possibleNames,candidateIds,type);
		
		// retrieve the details associated to the remaining id and complete the entity
		completeEntityWithIds(selectedId,entity);
		//TODO should we also add all the names known in WD for this entity?
		
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
				+ WIKIDATA_WEBSEARCH_PARAM_LANG1 + language.toString().toLowerCase(Locale.ENGLISH)
				+ WIKIDATA_WEBSEARCH_PARAM_LANG2 + language.toString().toLowerCase(Locale.ENGLISH)
				+ WIKIDATA_WEBSEARCH_PARAM_SEARCH;
		
		// process each possible name
		Iterator<String> it = possibleNames.iterator();
		do
		{	String candidateName = it.next();
			logger.log("Processing possible name "+candidateName);
			logger.increaseOffset();
		
			// build the url
			String url = baseUrl + URLEncoder.encode(candidateName, "UTF-8");
			logger.log("URL: "+url);
			
			// get the answer first through the cache
			String answer = null;
			if(cache)
			{	if(wikidataCache==null)
					wikidataCache = new WmCache(FileNames.FI_WIKIDATA);
				answer = wikidataCache.getValue(url);
			}
			// if it fails, actually query the server
			if(answer==null)
			{	// query the server	
				HttpClient httpclient = new DefaultHttpClient();   
				HttpGet request = new HttpGet(url);
				HttpResponse response = httpclient.execute(request);
				// parse the answer to get an XML document
				answer = WebTools.readAnswer(response);
				if(cache)
					wikidataCache.putValue(url,answer);
			}
			
			// build the XML DOM
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
				Element matchElt = entityElt.getChild(ELT_MATCH,ns);
				if(label==null)
				{	label = matchElt.getAttributeValue(ATT_TEXT);
					logger.log("Description="+description+" text="+label);
				}
				else
					logger.log("Description="+description+" label="+label);
				
				// if not in the targeted language, just skip it
				String lang = matchElt.getAttributeValue(ATT_LANGUAGE);
				if(lang.equalsIgnoreCase(language.toString()))
				{	// if it is a disambiguation page, we must retrieve the entities it contains
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
						logger.log("Not a disambiguation page: adding "+id+" to the map (if not already present)");
						if(!result.keySet().contains(id))
							result.put(id,label);
					}
				}
				else
					logger.log("Ignoring the entity because its language is "+lang+" instead of "+language);

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
		
		// get the answer first through the cache
		String answer = null;
		if(cache)
		{	if(wikimediaCache==null)
				wikimediaCache = new WmCache(FileNames.FI_WIKIMEDIA);
			answer = wikimediaCache.getValue(url);
		}
		// if it fails, actually query the server
		if(answer==null)
		{	// query the server	
			HttpClient httpclient = new DefaultHttpClient();   
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpclient.execute(request);
			// parse the answer to get an XML document
			answer = WebTools.readAnswer(response);
			if(cache)
				wikimediaCache.putValue(url,answer);
		}
		
		// build the XML DOM
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
	 * @param type
	 * 		Type of the considered mention.
	 * @return
	 * 		The best candidate id.
	 * 
	 * @throws IllegalStateException
	 * 		Problem while accessing the WikiData service.
	 * @throws IOException
	 * 		Problem while accessing the WikiData service.
	 * @throws JDOMException
	 * 		Problem while parsing the WikiData service XML response.
	 */
	private static String filterIds(List<String> possibleNames, Map<String,String> ids, EntityType type) throws IllegalStateException, IOException, JDOMException
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
			logger.log("Found "+idList.size()+" possible ids");
			
			// if more than one id, warn the user and keep the first one
			if(!idList.isEmpty())
			{	logger.log("Checking the type of each possible id");
				logger.increaseOffset();
				Iterator<String> it2 = idList.iterator();
				while(it2.hasNext())
				{	String id = it2.next();
					logger.log("Processing id "+id);
					List<EntityType> types = retrieveTypesFromId(id);
					if(types.contains(type))
						logger.log("Kept: the targeted type ("+type+") is one of the types of this entity ("+types.toString()+")");
					else
					{	logger.log("Rejected: the targeted type ("+type+") is not one of the types of this entity ("+types.toString()+")");
						it2.remove();
					}
				}
				logger.decreaseOffset();
				logger.log("Number of remaining ids: "+idList.size());
				
				result = idList.get(0);
				if(idList.size()>1)
				{	logger.log("WARNING: several ids were found for entity "+possibleNames.get(0)+"(name \""+possibleName+"\")");
					logger.increaseOffset();
						logger.log(idList);
					logger.decreaseOffset();
					logger.log("We keep the first one and go on");
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

	/**
	 * Receives an entity id and returns the entity type
	 * associated to it (usually one, but can be more, e.g.
	 * France is both a place and an organization).
	 * 
	 * @param id
	 * 		Id of the entity on WikiData.
	 * @return
	 * 		List of associated entity types.
	 * 
	 * @throws IllegalStateException
	 * 		Problem while accessing the WikiData service.
	 * @throws IOException
	 * 		Problem while accessing the WikiData service.
	 * @throws JDOMException
	 * 		Problem while parsing the WikiData service XML response.
	 */
	private static List<EntityType> retrieveTypesFromId(String id) throws IllegalStateException, IOException, JDOMException
	{	logger.log("Retrieving the types associated to id "+id);
		logger.increaseOffset();
		List<EntityType> result = new ArrayList<EntityType>();
		
		// request the server
		String query = URLEncoder.encode(WIKIMEDIA_QUERY_TYPES_PREFIX + id + WIKIMEDIA_QUERY_TYPES_SUFFIX, "UTF-8");
		String url = WIKIDATA_SPARQL_URL + query + WIKIDATA_SPARQL_URL_SUFFIX;
		logger.log("URL: "+url);
		
		// get the answer first through the cache
		String answer = null;
		if(cache)
		{	if(sparqlCache==null)
				sparqlCache = new WmCache(FileNames.FI_QUERIES);
			answer = sparqlCache.getValue(url);
		}
		// if it fails, actually query the server
		if(answer==null)
		{	// query the server	
			HttpClient httpclient = new DefaultHttpClient();   
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpclient.execute(request);
			// parse the answer to get an XML document
			answer = WebTools.readAnswer(response);
			if(cache)
				sparqlCache.putValue(url,answer);
		}
		
		// build the XML DOM
		SAXBuilder sb = new SAXBuilder();
		Document doc = sb.build(new StringReader(answer));
		Element root = doc.getRootElement();
		Namespace ns = Namespace.getNamespace(NS_SPARQL_API);
		
		// extract the type(s) from the XML doc
		Element resultsElt = root.getChild(ELT_RESULTS,ns);
		List<Element> resultElts = resultsElt.getChildren(ELT_RESULT,ns);
		for(Element resultElt: resultElts)
		{	Element bindingElt = resultElt.getChild(ELT_BINDING,ns);
			Element uriElt = bindingElt.getChild(ELT_URI,ns);
			String uri = uriElt.getText().trim();
			int pos = uri.lastIndexOf('/');
			String typeId = uri.substring(pos+1);
			EntityType type = MAP_ID_TO_TYPE.get(typeId);
			logger.log("Found URI "+uri+" (type="+type+")");
			result.add(type);
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Complete the specified entity with as many ids as can be
	 * found for the specified id on WikiData.
	 * 
	 * @param id
	 * 		Id of the entity.
	 * @param entity
	 * 		Entity to complete.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the WikiData service. 
	 * @throws ClientProtocolException 
	 * 		Problem while accessing the WikiData service. 
	 * @throws JDOMException 
	 * 		Problem while parsing the XML WikiData response. 
	 */
	private static void completeEntityWithIds(String id, AbstractNamedEntity entity) throws ClientProtocolException, IOException, JDOMException
	{	logger.log("Using ids retrieved from WikiData to complete entity "+entity+" ("+id+")");
		logger.increaseOffset();
		
		// request the server
		String query = URLEncoder.encode(WIKIMEDIA_QUERY_EXTIDS_PREFIX + id + WIKIMEDIA_QUERY_EXTIDS_SUFFIX, "UTF-8");
		String url = WIKIDATA_SPARQL_URL + query + WIKIDATA_SPARQL_URL_SUFFIX;
		logger.log("URL: "+url);
		
		// get the answer first through the cache
		String answer = null;
		if(cache)
		{	if(sparqlCache==null)
				sparqlCache = new WmCache(FileNames.FI_QUERIES);
			answer = sparqlCache.getValue(url);
		}
		// if it fails, actually query the server
		if(answer==null)
		{	// query the server	
			HttpClient httpclient = new DefaultHttpClient();   
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpclient.execute(request);
			// parse the answer to get an XML document
			answer = WebTools.readAnswer(response);
			if(cache)
				sparqlCache.putValue(url,answer);
		}
		
		// build the XML DOM
		SAXBuilder sb = new SAXBuilder();
		Document doc = sb.build(new StringReader(answer));
		Element root = doc.getRootElement();
		Namespace ns = Namespace.getNamespace(NS_SPARQL_API);
		
		// extract the external ids from the XML doc
		Element resultsElt = root.getChild(ELT_RESULTS,ns);
		List<Element> resultElts = resultsElt.getChildren(ELT_RESULT,ns);
		logger.log("Found "+resultElts.size()+" ids:");
		logger.increaseOffset();
		int i = 1;
		for(Element resultElt: resultElts)
		{	logger.log("Processing id "+i+"/"+resultElts.size());
			List<Element> bindingElts = resultElt.getChildren(ELT_BINDING,ns);
			Iterator<Element> it = bindingElts.iterator();
			while(it.hasNext())
			{	// property
				Element bindingPropElt = it.next();
				Element uriElt = bindingPropElt.getChild(ELT_URI,ns);
				String uri = uriElt.getText().trim();
				int pos = uri.lastIndexOf('/');
				String kbId = uri.substring(pos+1);
				KnowledgeBase kb = MAP_ID_TO_KB.get(kbId);
				
				// value
				Element bindingValueElt = it.next();
				Element literalValueElt = bindingValueElt.getChild(ELT_LITERAL,ns);
				String value = literalValueElt.getText().trim();
				
				// propLabel
				Element bindingLabelElt = it.next();
				Element literalLabelElt = bindingLabelElt.getChild(ELT_LITERAL,ns);
				String label = literalLabelElt.getText().trim();
				
				// decision
				if(kb==null)
				{	if(IGNORED_KB.contains(label))
						logger.log("The knowledge base \""+label+"\" is ignored.");
					else
						//TODO debug this to find all KB
						logger.log("WARNING: Found URI "+uri+" corresponding to unknown knowledge base named \""+label+"\"");
				}
				else
				{	logger.log("Found URI "+uri+" (kb="+kb+" label=\""+label+"\" value="+value+")");
					entity.setExternalId(kb, value);
				}
			}
			i++;
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
	}

	/**
	 * <b>Incomplete method</b>, aiming at completing an existing entity
	 * based on information retrieved from WikiData.
	 * 
	 * @param entity
	 * 		Entity to complete.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the WikiData service. 
	 * @throws ClientProtocolException 
	 * 		Problem while accessing the WikiData service. 
	 * @throws JDOMException 
	 * 		Problem while parsing the XML WikiData response. 
	 */
	public static void completeEntity(AbstractNamedEntity entity) throws ClientProtocolException, IOException, JDOMException
	{	String id = entity.getExternalId(KnowledgeBase.WIKIDATA);
		logger.log("Using ids retrieved from WikiData to complete entity "+entity+" ("+id+")");
		logger.increaseOffset();
		
		// request the server
		String url = WIKIDATA_GETENT_URL + WIKIDATA_GETENT_PARAM_SEARCH + id;
		logger.log("URL: "+url);
		
		// get the answer first through the cache
		String answer = null;
		if(cache)
		{	if(wikidataCache==null)
				wikidataCache = new WmCache(FileNames.FI_WIKIDATA);
			answer = wikidataCache.getValue(url);
		}
		// if it fails, actually query the server
		if(answer==null)
		{	// query the server	
			HttpClient httpclient = new DefaultHttpClient();   
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpclient.execute(request);
			// parse the answer to get an XML document
			answer = WebTools.readAnswer(response);
			if(cache)
				wikidataCache.putValue(url,answer);
		}
		
		// build the XML DOM
		SAXBuilder sb = new SAXBuilder();
		Document doc = sb.build(new StringReader(answer));
		Element root = doc.getRootElement();
		Namespace ns = Namespace.getNamespace(NS_WM_API);
		
		// extract data from the XML document
		Element entitiesElt = root.getChild(ELT_ENTITIES,ns);
		Element entityElt = entitiesElt.getChild(ELT_ENTITY,ns);
//TODO the below part must be adapted to the fields we want to retrieve		
		Element claimsElt = entityElt.getChild(ELT_CLAIMS,ns);
		List<Element> propertyElts = claimsElt.getChildren(ELT_PROPERTY,ns);
		for(Element propertyElt: propertyElts)
		{	String propId = propertyElt.getAttributeValue(ATT_ID);
			KnowledgeBase kn = MAP_ID_TO_KB.get(propId);
			if(kn!=null)
			{	Element claimElt = propertyElt.getChild(ELT_CLAIM,ns);
				Element mainsnakElt = claimElt.getChild(ELT_MAINSNAK,ns);
				Element datavalueElt = mainsnakElt.getChild(ELT_DATAVALUE,ns);
				String propValue = datavalueElt.getAttributeValue(ATT_VALUE);
				entity.setExternalId(kn, propValue);
			}
		}
		
		logger.decreaseOffset();
	}
	
	/**
	 * Generates all possible human names from a string representing
	 * the full name. This methods allows considering various combinations
	 * of lastname(s) and firstname(s).
	 * 
	 * @param names
	 * 		All the surface forms of the entity, should contain several names 
	 * 		separated by spaces.
	 * @return
	 * 		A list of strings corresponding to alternative forms of the 
	 * 		original name.
	 */
	private static List<String> getPossibleNames(Set<String> names)
	{	List<String> result = new ArrayList<String>();
		for(String name: names)
		{	if(!result.contains(name))
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
		}
		
		return result;
	}
	
//	/////////////////////////////////////////////////////////////////
//	// TESTS		 		/////////////////////////////////////////
//	/////////////////////////////////////////////////////////////////
//    public static void main(String[] args) throws Exception
//    {	// possible names
////    	System.out.println(getPossibleNames("Lastname")+"\n\n");
////    	System.out.println(getPossibleNames("Firstname Lastname")+"\n\n");
////    	System.out.println(getPossibleNames("Firstname Middlename Lastname")+"\n\n");
////    	System.out.println(getPossibleNames("Firstname Middlename Lastname1 Lastname2")+"\n\n");
////    	System.out.println(getPossibleNames("Firstname1 Firstname2 Middlename Lastname1 Lastname2")+"\n\n");
//    	
//    	// disambiguation page
////    	Map<String,String> res = retrieveIdsFromDisambiguation(ArticleLanguage.FR, "Lecointe");
////    	System.out.println(res);
//    	
//    	// retrieve the ids
////    	List<String> possibleNames = getPossibleNames("Adolphe Lucien Lecointe");
////    	Map<String,String> res = retrieveIdsFromName(possibleNames, ArticleLanguage.FR);
////    	System.out.println(res);
//    	
//    	// general lookup method
//    	AbstractNamedEntity entity = AbstractNamedEntity.buildEntity(-1, "Adolphe Lucien Lecointe", EntityType.PERSON);
////    	AbstractNamedEntity entity = AbstractNamedEntity.buildEntity(-1, "Achille Eugène Fèvre", EntityType.PERSON);
//    	lookupNamedEntity(entity, ArticleLanguage.FR);
//    	System.out.println(entity);
//    	Map<KnowledgeBase, String> extIds = entity.getExternalIds();
//    	System.out.println(extIds);
//	}
}
