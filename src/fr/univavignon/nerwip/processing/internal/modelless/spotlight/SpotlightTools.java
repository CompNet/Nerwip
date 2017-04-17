package fr.univavignon.nerwip.processing.internal.modelless.spotlight;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.AbstractNamedEntity;
import fr.univavignon.nerwip.data.entity.AbstractValuedEntity;
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.KnowledgeBase;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.nerwip.tools.string.StringTools;
import fr.univavignon.nerwip.tools.web.WebTools;

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

/**
 * This class contains methods implementing 
 * some processing related to DBpedia Spotlight.
 * 
 * @author Vincent Labatut
 * @author Sabrine Ayachi
 */
public class SpotlightTools
{	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
//	/**
//	 * Tests the class.
//	 * 
//	 * @param args
//	 * 		No need.
//	 * @throws ProcessorException 
//	 * 		Whatever problem occurred...
//	 */
//	public static void main(String[] args) throws ProcessorException 
//	{	String text = "Carrie Fisher, née le 21 octobre 1956 à Beverly Hills et morte le 27 décembre 2016 à Los Angeles, est une actrice, romancière et scénariste américaine.";
//		Article article = new Article("Carrie Fisher");
//		article.setLanguage(ArticleLanguage.FR);
//		article.setRawText(text);
//		Mentions mentions = new Mentions(ProcessorName.REFERENCE);
//		mentions.addMention(new MentionPerson(0, 13, ProcessorName.REFERENCE, "Carrie Fisher"));
//		mentions.addMention(new MentionDate(22, 37, ProcessorName.REFERENCE, "27 décembre 2016"));
//		mentions.addMention(new MentionLocation(85, 96, ProcessorName.REFERENCE, "Los Angeles"));
//		mentions.addMention(new MentionFunction(106, 113, ProcessorName.REFERENCE, "actrice"));
//		mentions.addMention(new MentionFunction(115, 125, ProcessorName.REFERENCE, "romancière"));
//		mentions.addMention(new MentionFunction(129, 139, ProcessorName.REFERENCE, "scénariste"));
//		List<String> list = invokeDisambiguate(article, mentions);
//		System.out.println(list);
//	}
	
	/////////////////////////////////////////////////////////////////
	// URL			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// Old URL of the Web service 
//	private static final String SERVICE_URL = "http://spotlight.dbpedia.org/rest/";
	/** URL of the English version of the Web service */
	private static final String SERVICE_EN_URL = "http://spotlight.sztaki.hu:2222/rest/";
	/** URL of the French version of the Web service */
	private static final String SERVICE_FR_URL = "http://spotlight.sztaki.hu:2225/rest/";
	/** Recognizer URL (but no entity type) */
//	private static final String RECOGNIZER_SERVICE = "spot"; //or is it annotate ?
	/** Resolver + Linker URL (applied to already detected entities) */
	private static final String LINKER_SERVICE = "disambiguate";
	/** Recognizer+Resolver+Linker (at once) URL */
	private static final String BOTH_SERVICES = "annotate";
	
	/////////////////////////////////////////////////////////////////
	// XML			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Element containing the list of all entities resources */
	public final static String ELT_RESOURCES = "Resources";
	/** Element containing the list of informations of every entity resource */
	public final static String ELT_RESOURCE = "Resource";
	/** Element containing entities detected by another recognizer */
	public final static String ELT_ANNOTATION = "annotation";
	/** Element containing the name of an entity */
	public final static String ELT_SURFACE_FORM = "surfaceForm";

	/** Attribute representing the name of an entity */
	public final static String ATT_NAME = "name";
	/** Attribute representing the position of a mention in the text */
	public final static String ATT_OFFSET = "offset";
	/** Relevance score (how good the second entity choice is compared to the returned one) */
	private final static String ATT_SECOND_RANK = "percentageOfSecondRank";
	/** Don't know what this is */
	private final static String ATT_SIMILARITY_SCORE = "similarityScore";
	/** Number of occurrences of the entity in Wikipedia */
	private final static String ATT_SUPPORT = "support";
	/** Attribute representing the name of an entity */
	public final static String ATT_SURFACE_FORM = "surfaceForm";
	/** Attribute representing the types of an entity */
	public final static String ATT_TYPES = "types";
	/** Attribute containing the original text */
	public final static String ATT_TEXT = "text";
	/** Attribute representing the DBpedia URI of an entity */
	public final static String ATT_URI = "URI";
	

	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Maximal request size for Spotlight (official recommendation is 400000 for a POST request, cf. https://github.com/dbpedia-spotlight/dbpedia-spotlight/issues/72) */
	protected static final int MAX_SIZE = 10000;
	/** Sleep periods (in ms) */ // is this actually necessary for Spotlight?
	protected static final long SLEEP_PERIOD = 100;
	
	/**
	 * Invokes the Spotlight service for mentions recognition and coreference resolution.
	 * This method is used by several delegates of Spotlight.
	 *  
	 * @param article
	 * 		The article to process.
	 * @param minConf
	 * 		Minimal confidence parameter. 
	 * @return
	 * 		The String representation of the service answer.
	 * 
	 * @throws ProcessorException
	 * 		Problem while accessing the service.
	 */
	protected static List<String> invokeAnnotate(Article article, float minConf) throws ProcessorException
	{	logger.increaseOffset();
		
		// setup Web Service URL
		String serviceUrl = null;
		switch(article.getLanguage())
		{	case EN:
				serviceUrl = SpotlightTools.SERVICE_EN_URL;
				break;
			case FR:
				serviceUrl = SpotlightTools.SERVICE_FR_URL;
				break;
		}
		serviceUrl = serviceUrl + SpotlightTools.BOTH_SERVICES;
		
		// setup HTTP parameters
		List<NameValuePair> origParams = new ArrayList<NameValuePair>();
		{	// It is a heuristic that seeks coreference in all text and infer the surface form. When is true, no other filter will be applied.
			// Available in: /candidates, /annotate
			// Default value: true
//			origParams.add(new BasicNameValuePair("coreferenceResolution", "false"));
		}
		{	// Selects all entities that have a percentageOfSecondRank greater than the square of value informed.
			// percentageOfSecondRank measures how much the winning entity has won by taking contextualScore_2ndRank / contextualScore_1stRank, 
			// which means the lower this score, the further the first ranked entity was "in the lead".
			// Available in: /candidates, /annotate
			// Default vale: 0.1
			origParams.add(new BasicNameValuePair("confidence", Float.toString(minConf)));
		}
		{	// Selects all entities that have a support greater than informed.
			// Support expresses how prominent an entity is. Based on the number of inlinks in Wikipedia.
			// Default value: 10
			origParams.add(new BasicNameValuePair("support", "1"));
		}
		{	// Combined with policy parameter, select all entities that have the same type - if policy is whitelist. 
			// Otherwise - if policy is blacklist - select all entities that have not the same type.
			// Usage: types=DBpedia:PopulatedPlaces,DBpedia:Thing
			// Available in: /candidates, /annotate
//			origParams.add(new BasicNameValuePair("type", ""));
		}
		{	// Combined with policy parameter, select all entities that match with the query result - if policy is whitelist. 
			// Otherwise - if policy is blacklist - select all entities that no match with the query result.
			// Available in: /candidates, /annotate
//			origParams.add(new BasicNameValuePair("sparql", ""));
		}
		{	// Spotters are algorithms that select all candidates for possible annotations. There are two kind of implementations. 
			// In the language-independent implementation, the candidates are generated by traversing a finite state automaton 
			// encoding all possible sequences of tokens that form known spot candidates.
			// In the language-dependent implementation, candidates are generated using three methods: 1. identifying all sequences 
			// of capitalized tokens, 2. identifying all noun phrases, prepositional phrases and multi word units, 3. identifying 
			// all named entities. Methods 2 and 3 are performed using Apache OpenNLP6 models for phrase chunking and Named Entity Recognition.
			// Available in: /candidates, /annotate, /spot
			// Default value: Default
			// Possible values:
			//	- Default: picks the first spotter informed in the configuration file
			//	x LingPipeSpotter: uses a dictionary of known names to spot
			//	x AtLeastOneNounSelector: uses the dictionary and removes spots that do not contain a noun
			//	x CoOccurrenceBasedSelector: uses the dictionary and removes spots that "look like" a non-entity (as trained by feature co-occurrence statistics)
			//	x NESpotter: uses the OpenNLP default models for Named Entity Recognition (NER)
			//	- KeyphraseSpotter: uses the Kea default models for Keyphrase Extraction
			//	- WikiMarkupSpotter: assumes that another tool has performed spotting and encoded the spots as WikiMarkup
			//	x SpotXmlParser: assumes that another tool has performed spotting and encoded the spots as SpotXml.
			//	- AhoCorasickSpotter: undocumented...
//			origParams.add(new BasicNameValuePair("spotter", "NESpotter"));
			// Note: that only work with the previous, Lucene-based version of Spotlight
		}
//		origParams.add(new BasicNameValuePair("Accept", "application/json"));
//		origParams.add(new BasicNameValuePair("Accept", "text/xml"));
//		origParams.add(new BasicNameValuePair("output", "xml"));
//		origParams.add(new BasicNameValuePair("charset", "utf-8"));
		
		// invoke the service
		String text = article.getRawText();
		List<String> parts = StringTools.splitText(text, SpotlightTools.MAX_SIZE);
		List<String> result = invokeService(parts, serviceUrl, origParams);
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Invokes the Spotlight service for coreference resolution, on previously
	 * detected mentions.
	 *  
	 * @param article
	 * 		The article to process.
	 * @param mentions
	 * 		Previously detected mentions for the specified article.
	 * @return
	 * 		The String representation of the service answer.
	 * 
	 * @throws ProcessorException
	 * 		Problem while accessing the service.
	 */
	protected static List<String> invokeDisambiguate(Article article, Mentions mentions) throws ProcessorException
	{	logger.increaseOffset();
	
		// setup Web Service URL
		String serviceUrl = null;
		switch(article.getLanguage())
		{	case EN:
				serviceUrl = SpotlightTools.SERVICE_EN_URL;
				break;
			case FR:
				serviceUrl = SpotlightTools.SERVICE_FR_URL;
				break;
		}
		serviceUrl = serviceUrl + SpotlightTools.LINKER_SERVICE;

		// setup HTTP parameters
		List<NameValuePair> origParams = new ArrayList<NameValuePair>();

		// convert mentions to SpotLight format
		String text = article.getRawText();
		List<String> parts = StringTools.splitText(text, SpotlightTools.MAX_SIZE);
		List<String> convertedParts = new ArrayList<String>();
		int pos = 0;
		for(String part: parts)
		{	String convertedPart = convertMentionsToSpotlight(part,mentions,pos);	
			convertedParts.add(convertedPart);
			pos = pos + part.length();
		}
		
		// invoke the service
		List<String> temp = invokeService(convertedParts, serviceUrl, origParams);
		
		// add the raw text to the result
		List<String> result = new ArrayList<String>();
		Iterator<String> it1 = temp.iterator();
		Iterator<String> it2 = parts.iterator();
		while(it2.hasNext())
		{	String originalText = it2.next();
			result.add(originalText);
			String convertedText = it1.next();
			result.add(convertedText);
			String spotlightAnswer = it1.next();
			result.add(spotlightAnswer);
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Generic method processing all types of invocation of the Spotlight service:
	 * Spot, disambiguate and annotate.
	 * 
	 * @param parts
	 * 		Broken-down text we want to fetch SpotLight.
	 * @param url
	 * 		URL of the service, as a String.
	 * @param origParams
	 * 		Original parameters, used for every piece of text.
	 * @return
	 * 		List of Strings returned by the service (one for each piece of the original text).
	 * 
	 * @throws ProcessorException
	 * 		Problem while accessing the online service.
	 */
	protected static List<String> invokeService(List<String> parts, String url, List<NameValuePair> origParams) throws ProcessorException
	{	List<String> result = new ArrayList<String>();
		
		// then we process each part separately
		for(int i=0;i<parts.size();i++)
		{	logger.log("Processing Spotlight part #"+(i+1)+"/"+parts.size());
			logger.increaseOffset();
			String part = parts.get(i);
			
			try
			{	logger.log("Define HTTP message for Spotlight");
				
				List<NameValuePair> params = new ArrayList<NameValuePair>(origParams);
				HttpPost method = new HttpPost(url);
				params.add(new BasicNameValuePair("text", part));
				method.setHeader("Accept", "text/xml");
				method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

				logger.log("Send message to service");
				HttpClient client = new DefaultHttpClient();
				HttpResponse response;
				response = client.execute(method);
				int responseCode = response.getStatusLine().getStatusCode();
				logger.log("Response Code : " + responseCode);

				//read service answer
				logger.log("Read the spotlight answer");
				String answer = WebTools.readAnswer(response);
				
				result.add(part);
				result.add(answer);

				Thread.sleep(SpotlightTools.SLEEP_PERIOD); // might not be needed not needed by Spotlight
			}

			catch (UnsupportedEncodingException e) 
			{	// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ClientProtocolException e) 
			{	// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) 
			{	// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{	// a problem occured while sleeping
				e.printStackTrace();
			}
			logger.decreaseOffset();
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Prefix used to distinguish types from DBpedia classes or concepts */
	private final static String TYPE_PREFIX = "Schema:";
	/** List of strings to ignore during the type conversion */
	private final static List<String> IGNORE_LIST = Arrays.asList(
		TYPE_PREFIX+"Language",
		TYPE_PREFIX+"Product"
	);
	/** Map of string to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put(TYPE_PREFIX+"Event", EntityType.MEETING);
		CONVERSION_MAP.put(TYPE_PREFIX+"Person", EntityType.PERSON);
		CONVERSION_MAP.put(TYPE_PREFIX+"Place", EntityType.LOCATION);
		CONVERSION_MAP.put(TYPE_PREFIX+"Organization", EntityType.ORGANIZATION);
		CONVERSION_MAP.put(TYPE_PREFIX+"CreativeWork", EntityType.PRODUCTION);
	}
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Converts a list of previously identified mentions so that they
	 * can be processed by SpotLight.
	 * 
	 * @param text
	 * 		Original text.
	 * @param mentions
	 * 		Detected mentions.
	 * @param startPos
	 * 		Position of the text in the original article (for when splitting articles).
	 * @return
	 * 		A textual representation compatible with SpotLight. 
	 */
	private static String convertMentionsToSpotlight(String text, Mentions mentions, int startPos)
	{	StringBuffer result = new StringBuffer();
		
		// init with the original text
		result.append("<"+ELT_ANNOTATION+" "+ATT_TEXT+"=\""+text+"\">");
		
		for(AbstractMention<?> mention: mentions.getMentions())
		{	String valueStr = mention.getStringValue();
			int offset = mention.getStartPos() - startPos;
			result.append("<"+ELT_SURFACE_FORM+" "+ATT_NAME+"=\""+valueStr+"\" "+ATT_OFFSET+"=\""+offset+"\"/>");
		}

		// close the main tag
		result.append("</"+ELT_ANNOTATION+">");
		
		return result.toString();
	}

	/**
	 * Converts the specified objects, used internally by the associated
	 * recognizer, into the mention list used internally by Nerwip.  
	 * 
	 * @param data
	 * 		List of SpotLight answers.
	 * @param processorName
	 * 		Name of the recognizer (here: should be SpotLight).
	 * @param mentions
	 * 		Empty object, to be filled by this method.
	 * @param entities
	 * 		Empty object, to be filled by this method. Can be {@code null}
	 * 		if only recognition is performed.
	 * @param annotate
	 * 		{@code true} iff the specified {@code data} was obtained through 
	 * 		an "annotate" type of request, {@code false} if it was through
	 * 		a "disambiguate" type of request. In the former case, we must
	 * 		create mentions, whereas in the latter we must just update them. 
	 * @param language
	 * 		Language of the processed article.
	 * 
	 * @throws ProcessorException
	 * 		Problem while performing the conversion.
	 */
	protected static void convertSpotlightToNerwip(List<String> data, ProcessorName processorName, Mentions mentions, Entities entities, boolean annotate, ArticleLanguage language) throws ProcessorException
	{	logger.increaseOffset();
		
		logger.log("Processing each part of data and its associated answer");
		Iterator<String> it = data.iterator();
		logger.increaseOffset();
		int i = 0;
		int prevSize = 0;
		while(it.hasNext())
		{	i++;
			logger.log("Processing part "+i+"/"+data.size()/2);
			String originalText = it.next();
			if(!annotate)
				it.next(); // pass the converted original text  
			String spotlightAnswer = it.next();
			
			try
			{	// build DOM
				logger.log("Build DOM");
				SAXBuilder sb = new SAXBuilder();
				Document doc = sb.build(new StringReader(spotlightAnswer));
				Element root = doc.getRootElement();
				
				// process each detected mention
				logger.log("Process each detected mention");
				Element eltResources = root.getChild(ELT_RESOURCES);
				List<Element> eltResourceList = eltResources.getChildren(ELT_RESOURCE);
				for(Element eltResource: eltResourceList)
				{	//TODO maybe a type could simply be retrieved from the DBpedia URI...
					convertResourceElement(eltResource,prevSize,originalText,processorName,mentions,entities,annotate,language);
				}
				
				// update size
				prevSize = prevSize + originalText.length();
			}
			catch (JDOMException e)
			{	e.printStackTrace();
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
	}
	
	/**
	 * Receives an XML element and extract the information necessary to create
	 * a mention and possibly an entity. Both are added to the specified
	 * lists if necessary. 
	 *  
	 * @param element
	 * 		Element to process.
	 * @param prevSize
	 * 		Size of the parts already processed. 
	 * @param part
	 * 		Part of the original text currently processed. 
	 * @param processorName
	 * 		Name of the processor (here: should be SpotLight).
	 * @param mentions
	 * 		List of mentions, to be completed by this method.
	 * @param entities
	 * 		List of entities, to be completed by this method. Can be {@code null}
	 * 		if only recognition is performed.
	 * @param annotate
	 * 		{@code true} iff the specified {@code data} was obtained through 
	 * 		an "annotate" type of request, {@code false} if it was through
	 * 		a "disambiguate" type of request. In the former case, we must
	 * 		create mentions, whereas in the latter we must just update them.
	 * @param language
	 * 		Language of the processed article.  
	 */
	private static void convertResourceElement(Element element, int prevSize, String part, ProcessorName processorName, Mentions mentions, Entities entities, boolean annotate, ArticleLanguage language)
	{	logger.increaseOffset();
		
		// parse the element
		StringBuffer msg = new StringBuffer();
			// surface form (check: compare with original text)
			String surfaceForm = element.getAttributeValue(ATT_SURFACE_FORM);
			msg.append("SurfaceForm="+surfaceForm);
			// type associated to the mention/entity
			String types = element.getAttributeValue(ATT_TYPES);
			msg.append(" Types="+types);
			// URI associated to the entity
			String uri = element.getAttributeValue(ATT_URI);
			msg.append(" URI="+uri);
			// position of the mention
			String offsetStr = element.getAttributeValue(ATT_OFFSET);
			int offset = Integer.parseInt(offsetStr);
			msg.append(" Offset="+offset);
			// support of the entity in Wikipedia
			String supportStr = element.getAttributeValue(ATT_SUPPORT);
			int support = Integer.parseInt(supportStr);
			msg.append(" Support="+support);
			// relevance score 
			String secondRankStr = element.getAttributeValue(ATT_SECOND_RANK);
			Float secondRank = Float.parseFloat(secondRankStr);
			msg.append(" PerSecondRank="+secondRank);
			// don't know exactly what this is...
			String similarityScoreStr = element.getAttributeValue(ATT_SIMILARITY_SCORE);
			Float similarityScore = Float.parseFloat(similarityScoreStr);
			msg.append(" SimilarityScore="+similarityScore);
		logger.log(msg.toString());
			
		// get the mention type
		EntityType type = null;
		String[] temp = types.split(",");
		Set<EntityType> typeList = new TreeSet<EntityType>(); 
		Set<String> candidateList = new TreeSet<String>(); 
		for(String tmp: temp)
		{	if(!IGNORE_LIST.contains(tmp))
			{	EntityType t = CONVERSION_MAP.get(tmp);
				if(t==null)
				{	if(tmp.startsWith(TYPE_PREFIX))
						candidateList.add(tmp);
				}
				else
					typeList.add(t);
			}
		}
		
		// continue only if there's a type
		if(typeList.isEmpty())
		{	// this is mainly in case we miss some important types when reverse engineering Spotlight's output
			if(!candidateList.isEmpty())
				logger.log("WARNING: could not find any type, when some of them started with "+TYPE_PREFIX+": "+candidateList.toString());
			else
				logger.log("Entity without a type >> ignoring");
		}
		else
		{	// get the type
			type = typeList.iterator().next();
			if(typeList.size()>1)
				logger.log("WARNING: several different types for the same entity ("+typeList.toString()+") >> Selecting the first one ("+type+")");
			
			// get the mention position
			int startPos = prevSize + offset;
			int length = surfaceForm.length();
			int endPos = startPos + length;
			
			// compare detected surface form and original text
			String valueStr = part.substring(offset, offset+length);
			if(!valueStr.equalsIgnoreCase(surfaceForm))
				logger.log("WARNING: the original and returned texts differ: \""+valueStr+"\" vs. \""+surfaceForm+"\"");
			else
			{	AbstractMention<?> mention;
				if(annotate)
				{	mention = AbstractMention.build(type, startPos, endPos, processorName, surfaceForm, language);
					mentions.addMention(mention);
					logger.log("Created mention "+mention.toString());
				}
				else
				{	List<AbstractMention<?>> list = mentions.getMentionsIn(startPos, startPos+1);
					mention = list.get(0);
					logger.log("Retrieved mention "+mention.toString());
				}
				if(entities!=null)
				{	if(type.isNamed())
					{	AbstractNamedEntity entity = entities.getNamedEntityByExternalId(uri, KnowledgeBase.DBPEDIA_URI, type);
						if(entity==null)
						{	entity = AbstractNamedEntity.buildEntity(-1, surfaceForm, type);
							entity.setExternalId(KnowledgeBase.DBPEDIA_URI, uri);
							entities.addEntity(entity);
							logger.log("Created named entity "+entity.toString());
						}
						mention.setEntity(entity);
					}
					// valued entity: this probably never happens here (?)
					else
					{	Comparable<?> value = mention.getValue();
						AbstractValuedEntity<?> entity = entities.getValuedEntityByValue(value);
						if(entity==null)
						{	entity = AbstractValuedEntity.buildEntity(-1, value, type);
							entities.addEntity(entity);
							logger.log("Created valued entity "+entity.toString());
						}
						mention.setEntity(entity);
					}
				}
			}
		}
		
		logger.decreaseOffset();
	}
}
