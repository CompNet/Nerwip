package fr.univavignon.nerwip.processing.internal.modelless.spotlight;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import org.apache.http.HttpEntity;
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
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateLinker;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateRecognizer;
import fr.univavignon.nerwip.tools.dbpedia.DbpCommonTools;
import fr.univavignon.nerwip.tools.string.StringTools;

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
public class SpotlightDelegateLinker extends AbstractModellessInternalDelegateLinker<List<String>>
{
	/**
	 * Builds and sets up an object representing
	 * the Spotlight recognizer.
	 * 
	 * @param spotlight
	 * 		Recognizer in charge of this delegate.
	 * @param minConf 
	 * 		Minimal confidence for the returned entities.
	 */
	public SpotlightDelegateLinker(Spotlight spotlight, float minConf)
	{	super(spotlight);
		
		this.minConf = minConf;
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = linker.getName().toString();
		
		result = result + "_" + "minConf=" + minConf;
		
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
	public List<EntityType> getHandledEntityTypes()
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
	// MINIMAL CONFIDENCE	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Minimal confidence */
	private float minConf;

	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected List<String> linkEntities(Article article, Mentions mentions, Entities entities) throws ProcessorException
	{	logger.increaseOffset();
		List<String> result = new ArrayList<String>();
		String text = article.getRawText();
		
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
		
		// we need to break down the text
		List<String> parts = StringTools.splitText(text, SpotlightTools.MAX_SIZE);

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
					params.add(new BasicNameValuePair("confidence", Float.toString(minConf)));
				}
				{	// Selects all entities that have a support greater than informed.
					// Support expresses how prominent an entity is. Based on the number of inlinks in Wikipedia.
					// Default value: 10
					params.add(new BasicNameValuePair("support", "1"));
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
					// Note: that only work with the previous, Lucene-based version of Spotlight
				}
//				params.add(new BasicNameValuePair("Accept", "application/json"));
//				params.add(new BasicNameValuePair("Accept", "text/xml"));
//				params.add(new BasicNameValuePair("output", "xml"));
//				params.add(new BasicNameValuePair("charset", "utf-8"));
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
		
		logger.decreaseOffset();
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
	/** Map of string to mention type conversion */
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
	// XML NAMES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Element containing the list of all detected mentions */
	private final static String ELT_RESOURCES = "Resources";
	/** Element describing one detected mention */
	private final static String ELT_RESOURCE = "Resource";

	/** Attribute representing the DBpedia URI associated to a mention */
	private final static String ATT_URI = "URI";
	/** List of types associated to a mention */
	private final static String ATT_TYPES = "types";
	/** Position of a mention in the original text */
	private final static String ATT_OFFSET = "offset";
	/** Number of occurrences of the entity in Wikipedia */
	private final static String ATT_SUPPORT = "support";
	/** Surface form of the mention */
	private final static String ATT_SURFACE_FORM = "surfaceForm";
	/** Relevance score (how good the second entity choice is compared to the returned one) */
	private final static String ATT_SECOND_RANK = "percentageOfSecondRank";
	/** Don't know what this is */
	private final static String ATT_SIMILARITY_SCORE = "similarityScore";
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void convert(Article article, Mentions mentions, Entities entities, List<String> data) throws ProcessorException 
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
				{	AbstractMention<?> mention = convertElement(eltResource,prevSize,originalText);
					if(mention!=null)
						result.addMention(mention);
					//TODO maybe a type could simply be retrieved from the DBpedia URI...
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
	 * Receives an XML element and extract
	 * the information necessary to create
	 * a mention.
	 *  
	 * @param element
	 * 		Element to process.
	 * @param prevSize
	 * 		Size of the parts already processed. 
	 * @param part
	 * 		Part of the original text currently processed. 
	 * @return
	 * 		The resulting mention, or {@code null} if its
	 * 		type is not supported.
	 */
	private AbstractMention<?> convertElement(Element element, int prevSize, String part)
	{	AbstractMention<?> result = null;
		logger.increaseOffset();
		
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
			{	result = AbstractMention.build(type, startPos, endPos, recognizer.getName(), valueStr);
				logger.log("Created mention "+result.toString());
			}
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    @Override
    protected void writeRawResults(Article article, List<String> intRes) throws IOException
    {	String temp = "";
        int i = 0;
        for(String str: intRes)
        {
        	i++;
        	if(i%2==1)
        		temp = temp + "\n>>> Part " + ((i+1)/2) + "/" + intRes.size()/2 + " - Original Text <<<\n" + str + "\n";
        	else
        	{	try
        		{	// build DOM
					SAXBuilder sb = new SAXBuilder();
					Document doc = sb.build(new StringReader(str));
					Format format = Format.getPrettyFormat();
					format.setIndent("\t");
					format.setEncoding("UTF-8");
					XMLOutputter xo = new XMLOutputter(format);
					String kafTxt = xo.outputString(doc);
					
					// add formatted kaf
					temp = temp + "\n>>> Part " + (i/2) + "/" + intRes.size()/2 + " - Spotlight Response <<<\n" + kafTxt + "\n";
        		}
        		catch (JDOMException e)
        		{	e.printStackTrace();
        		}
        	}
    	}
        
        writeRawResultsStr(article, temp);
    }
}