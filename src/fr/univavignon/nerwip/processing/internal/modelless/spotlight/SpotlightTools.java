package fr.univavignon.nerwip.processing.internal.modelless.spotlight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.nerwip.tools.string.StringTools;

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
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// URL			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// Old URL of the Web service 
//	private static final String SERVICE_URL = "http://spotlight.dbpedia.org/rest/";
	/** URL of the English version of the Web service */
	public static final String SERVICE_EN_URL = "http://spotlight.sztaki.hu:2222/rest/";
	/** URL of the French version of the Web service */
	public static final String SERVICE_FR_URL = "http://spotlight.sztaki.hu:2225/rest/";
	/** Recognizer URL (but no entity type) */
	public static final String RECOGNIZER_SERVICE = "spot"; //or is it annotate ?
	/** Resolver + Linker URL */
	public static final String LINKER_SERVICE = "disambiguate";
	/** Recognizer+Resolver+Linker (at once) URL */
	public static final String BOTH_SERVICES = "annotate";
	
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
		List<String> result = invokeService(article, serviceUrl, origParams);
		
		logger.decreaseOffset();
		return result;
	}
	
	protected static List<String> invokeDisambiguate(Article article, float minConf) throws ProcessorException
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
		
		// invoke the service
		List<String> result = invokeService(article, serviceUrl, origParams);
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Generic method processing all types of invocation of the Spotlight service:
	 * Spot, disambiguate and annotate.
	 * 
	 * @param article
	 * 		Article to process.
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
	protected static List<String> invokeService(Article article, String url, List<NameValuePair> origParams) throws ProcessorException
	{	List<String> result = new ArrayList<String>();
		String text = article.getRawText();

		// we need to break down the text
		List<String> parts = StringTools.splitText(text, SpotlightTools.MAX_SIZE);

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
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line;
				while((line = reader.readLine()) != null)
				{	logger.log(line);
					sb.append(line+"\n");
				}

				String answer = sb.toString();
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
	// XML			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Element containing the list of all entities resources */
	public final static String ELT_RESOURCES = "Resources";
	/** Element containing the list of informations of every entity resource */
	public final static String ELT_RESOURCE = "Resource";

	/** Attribute representing the name of an entity */
	public final static String ATT_SURFACE_FORM = "surfaceForm";
	/** Attribute representing the types of an entity */
	public final static String ATT_TYPES = "types";
	/** Attribute representing the DBpedia URI of an entity */
	public final static String ATT_URI = "URI";
	
	
}
