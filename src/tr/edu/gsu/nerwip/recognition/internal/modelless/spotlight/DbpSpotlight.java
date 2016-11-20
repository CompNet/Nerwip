package tr.edu.gsu.nerwip.recognition.internal.modelless.spotlight;

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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.modelless.AbstractModellessInternalRecognizer;
import tr.edu.gsu.nerwip.tools.dbpedia.DbpCommonTools;
import tr.edu.gsu.nerwip.tools.string.StringTools;

/**
 * This class acts as an interface with the DBpedia Spotlight Web service.
 * <br/>
 * Recommended parameter values:
// * <ul>
// * 		<li>{@code parenSplit}: {@code true}</li>
// * 		<li>{@code ignorePronouns}: {@code true}</li>
// * 		<li>{@code exclusionOn}: {@code false}</li>
// * </ul>
 * <br/>
 * Official Spotlight website: 
 * <a href="http://spotlight.dbpedia.org">
 * http://spotlight.dbpedia.org</a>
 * <br/>
 * TODO Spotlight is available as a set of Java libraries. We could directly 
 * integrate them in Nerwip.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class DbpSpotlight extends AbstractModellessInternalRecognizer<List<String>,DpbSpotlightConverter>
{
	/**
	 * Builds and sets up an object representing
	 * the Spotlight recognizer.
	 * 
	 * @param parenSplit 
	 * 		Indicates whether mentions containing parentheses
	 * 		should be split (e.g. "Limoges (Haute-Vienne)" is split 
	 * 		in two distinct mentions).
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop words should be excluded from the detection.
	 */
	public DbpSpotlight()
	{	super(true,false,false);
		
		setIgnoreNumbers(false);
		
		// init converter
		converter = new DpbSpotlightConverter(getFolder());
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public RecognizerName getName()
	{	return RecognizerName.SPOTLIGHT;
	}

	/////////////////////////////////////////////////////////////////
	// CONVERTER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if mentions containing parentheses should be split */
	private boolean parenSplit = true;
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
//		result = result + "_" + "parenSplit=" + parenSplit;
//		result = result + "_" + "ignPro=" + ignorePronouns;
//		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types recognized by Spotlight */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList
	(	EntityType.DATE,
		EntityType.LOCATION,
		EntityType.ORGANIZATION,
		EntityType.PERSON
	);

	@Override
	public List<EntityType> getHandledMentionTypes()
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this recognizer can treat */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList
	(	ArticleLanguage.EN,
		ArticleLanguage.FR
	);

	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Maximal request size for Spotlight (official recommendation is 400000 for a POST request, cf. https://github.com/dbpedia-spotlight/dbpedia-spotlight/issues/72) */
	private static final int MAX_SIZE = 10000;
	/** Sleep periods (in ms) */ // is this actually necessary for Spotlight?
	private static final long SLEEP_PERIOD = 100;
	
	@Override
	protected List<String> detectMentions(Article article) throws RecognizerException
	{	logger.increaseOffset();
		List<String> result = new ArrayList<String>();
		String text = article.getRawText();
		
		// setup Web Service URL
		String serviceUrl = null;
		switch(article.getLanguage())
		{	case EN:
				serviceUrl = DbpCommonTools.SERVICE_EN_URL;
				break;
			case FR:
				serviceUrl = DbpCommonTools.SERVICE_FR_URL;
				break;
		}
		serviceUrl = serviceUrl + DbpCommonTools.BOTH_SERVICE;
		
		// we need to break down the text
		List<String> parts = StringTools.splitText(text, MAX_SIZE);

		// then we process each part separately
		for(int i=0;i<parts.size();i++)
		{	logger.log("Processing Spotlight part #"+(i+1)+"/"+parts.size());
			logger.increaseOffset();
			String part = parts.get(i);
			
			try
			{	logger.log("Define HTTP message for Spotlight");

				HttpPost method = new HttpPost(serviceUrl);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				{	// It is a heuristic that seeks coreference in all text and infer the surface form. When is true, no other filter will be applied.
					// Available in: /candidates, /annotate
					// Default value: true
//					params.add(new BasicNameValuePair("coreferenceResolution", "false"));
				}
				{	// Selects all entities that have a percentageOfSecondRank greater than the square of value informed.
					// percentageOfSecondRank measures how much the winning entity has won by taking contextualScore_2ndRank / contextualScore_1stRank, 
					// which means the lower this score, the further the first ranked entity was "in the lead".
					// Available in: /candidates, /annotate
					// Default vale: 0.1
//					params.add(new BasicNameValuePair("confidence", "0.1"));
				}
				{	// Selects all entities that have a support greater than informed.
					// Support expresses how prominent an entity is. Based on the number of inlinks in Wikipedia.
					// Default value: 10
//					params.add(new BasicNameValuePair("support", "10"));
				}
				{	// Combined with policy parameter, select all entities that have the same type - if policy is whitelist. 
					// Otherwise - if policy is blacklist - select all entities that have not the same type.
					// Usage: types=DBpedia:PopulatedPlaces,DBpedia:Thing
					// Available in: /candidates, /annotate
//					params.add(new BasicNameValuePair("type", ""));
				}
				{	// Combined with policy parameter, select all entities that match with the query result - if policy is whitelist. 
					// Otherwise - if policy is blacklist - select all entities that no match with the query result.
					// Available in: /candidates, /annotate
//					params.add(new BasicNameValuePair("sparql", ""));
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
//					params.add(new BasicNameValuePair("spotter", "NESpotter"));
				}


//				params.add(new BasicNameValuePair("Accept", "application/json"));
				params.add(new BasicNameValuePair("Accept", "text/xml"));
//				params.add(new BasicNameValuePair("output", "xml"));
				params.add(new BasicNameValuePair("charset", "utf-8"));
				params.add(new BasicNameValuePair("url", serviceUrl));
				params.add(new BasicNameValuePair("text", part));
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

				Thread.sleep(SLEEP_PERIOD); // might not be needed not needed by Spotlight
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
			{	// problem while sleeping
				e.printStackTrace();
			}
			logger.decreaseOffset();
		}
		
		logger.decreaseOffset();
		return result;
	}
}