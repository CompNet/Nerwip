package fr.univavignon.nerwip.processing.internal.modelless.opener;

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
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.MentionDate;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateRecognizer;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * This class acts as an interface with the OpeNer Web service.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code parenSplit}: {@code true}</li>
 * 		<li>{@code ignorePronouns}: {@code true}</li>
 * 		<li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * <br/>
 * Official OpeNer website: 
 * <a href="http://www.opener-project.eu/webservices/">
 * http://www.opener-project.eu/webservices/</a>
 * <br/>
 * <b>Notes:</b> the English version is able to recognize mentions
 * referring to the same entity, and to resolve coreferences. The 
 * tool also seems to be able to do entity linking (vs. a knowledge base).
 * <br/>
 * TODO OpeNer is available as a set of Java libraries. We could directly 
 * integrate them in Nerwip.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class OpeNerDelegateRecognizer extends AbstractModellessInternalDelegateRecognizer<List<String>>
{
	/**
	 * Builds and sets up an object representing
	 * the OpeNer recognizer.
	 * 
	 * @param opeNer
	 * 		Recognizer in charge of this delegate.
	 * @param parenSplit 
	 * 		Indicates whether mentions containing parentheses
	 * 		should be split (e.g. "Limoges (Haute-Vienne)" is split 
	 * 		in two distinct mentions).
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop words should be excluded from the detection.
	 */
	public OpeNerDelegateRecognizer(OpeNer opeNer, boolean parenSplit, boolean ignorePronouns, boolean exclusionOn)
	{	// it seems necessary to clean mentions with OpeNer,
		// otherwise it sometimes includes punctation in the mentions.
		super(opeNer,true,ignorePronouns,exclusionOn);
		
		setIgnoreNumbers(false);
		this.parenSplit = parenSplit;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
		result = result + "_" + "parenSplit=" + parenSplit;
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types recognized by OpeNer */
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
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Web service URL */
	private static final String SERVICE_URL = "http://opener.olery.com";
	/** Tokenizer URL */
	private static final String TOKENIZER_URL = SERVICE_URL + "/tokenizer";
	/** PoS tagger URL */
	private static final String TAGGER_URL = SERVICE_URL + "/pos-tagger";
	/** Constituent parser URL */
	private static final String PARSER_URL = SERVICE_URL + "/constituent-parser";
	/** Mention recognizer URL */
	private static final String RECOGNIZER_URL = SERVICE_URL + "/ner";
	/** Maximal request size for OpenNer (the doc recomands 1000) */
	private static final int MAX_SIZE = 1000;
	/** Sleep periods (in ms) */ // this is actually not needed
	private static final long SLEEP_PERIOD = 100;
	
	@Override
	protected List<String> detectMentions(Article article) throws ProcessorException
	{	logger.increaseOffset();
		List<String> result = new ArrayList<String>();
		String text = article.getRawText();
		
		// we need to break down the text
		List<String> parts = StringTools.splitText(text, MAX_SIZE);

		// then we process each part separately
		for(int i=0;i<parts.size();i++)
		{	logger.log("Processing OpeNer part #"+(i+1)+"/"+parts.size());
			logger.increaseOffset();
			String part = parts.get(i);
			
			try
			{	// tokenize the text
				String tokenizedText = performTokenization(part);
				Thread.sleep(SLEEP_PERIOD); // sometimes not needed

				// detect part-of-speech
				String taggedText = performTagging(tokenizedText);
				Thread.sleep(SLEEP_PERIOD); // sometimes not needed
				
				// apply the constituent parser
				String parsedText = performParsing(taggedText);
				Thread.sleep(SLEEP_PERIOD); // sometimes not needed
				
				// perform the recognition
				String nerText = performRecognition(parsedText);
				Thread.sleep(SLEEP_PERIOD); // sometimes not needed

				// clean the resulting XML // unnecessary
//				String kafOld ="<KAF xml:lang=\"fr\" version=\"v1.opener\">";
//		        String kafNew = "<KAF>";
//				nerText = nerText.replaceAll(kafOld, kafNew);
				
				// add part and corresponding answer to result
				result.add(part);
				result.add(nerText);
			}
			catch (UnsupportedEncodingException e)
			{	//e.printStackTrace();
				throw new ProcessorException(e.getMessage());
			}
			catch (ClientProtocolException e)
			{	//e.printStackTrace();
				throw new ProcessorException(e.getMessage());
			}
			catch (IOException e)
			{	//e.printStackTrace();
				throw new ProcessorException(e.getMessage());
			}
			catch (InterruptedException e)
			{	//e.printStackTrace();
				throw new ProcessorException(e.getMessage());
			}
			
			logger.decreaseOffset();
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Sends the original text to the OpenNer tokenizer,
	 * as a first processing step.
	 * 
	 * @param part
	 * 		The part of the original text to process.
	 * @return
	 * 		Corresponding tokenized text.
	 * 
	 * @throws ProcessorException
	 * 		Problem while accessing the tokenizer service.
	 * @throws ClientProtocolException
	 * 		Problem while accessing the tokenizer service.
	 * @throws IOException
	 * 		Problem while accessing the tokenizer service.
	 */
	private String performTokenization(String part) throws ProcessorException, ClientProtocolException, IOException
	{	logger.log("Perform tokenization");
		logger.increaseOffset();
		
		// define HTTP message
		logger.log("Define HTTP message for tokenizer");
		HttpPost method = new HttpPost(TOKENIZER_URL);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("input", part));
		params.add(new BasicNameValuePair("language", "fr" ));
		params.add(new BasicNameValuePair("kaf", "false" ));
		method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		// send to tokenizer and retrieve answer
		String result = sendReceiveRequest(method);
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Sends the tokenized text to the OpenNer PoS tagger,
	 * as a second processing step.
	 * 
	 * @param tokenizedText
	 * 		The previously tokenized text.
	 * @return
	 * 		Corresponding tagged text.
	 * 
	 * @throws ProcessorException
	 * 		Problem while accessing the tagger service.
	 * @throws ClientProtocolException
	 * 		Problem while accessing the tagger service.
	 * @throws IOException
	 * 		Problem while accessing the tagger service.
	 */
	private String performTagging(String tokenizedText) throws ProcessorException, ClientProtocolException, IOException
	{	logger.log("Perform PoS tagging");
		logger.increaseOffset();
		
		// define HTTP message
		logger.log("Define HTTP message for tagger");
		HttpPost method = new HttpPost(TAGGER_URL);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("input", tokenizedText));
		method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		// send to tagger and retrieve answer
		String result = sendReceiveRequest(method);
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Sends the tagged text to the OpenNer constituent parser,
	 * as a third processing step.
	 * 
	 * @param taggedText
	 * 		The previously tagged text.
	 * @return
	 * 		Corresponding parsed text.
	 * 
	 * @throws ProcessorException
	 * 		Problem while accessing the parser service.
	 * @throws ClientProtocolException
	 * 		Problem while accessing the parser service.
	 * @throws IOException
	 * 		Problem while accessing the parser service.
	 */
	private String performParsing(String taggedText) throws ProcessorException, ClientProtocolException, IOException
	{	logger.log("Perform constituent parsing");
		logger.increaseOffset();
	
		// define HTTP message
		logger.log("Define HTTP message for parser");
		HttpPost method = new HttpPost(PARSER_URL);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("input", taggedText));
		method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		// send to parser and retrieve answer
		String result = sendReceiveRequest(method);
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Sends the parsed text to the OpenNer mention recognizer,
	 * as a fourth processing step.
	 * 
	 * @param parsedText
	 * 		The previously parsed text.
	 * @return
	 * 		Text with the detected mentions.
	 * 
	 * @throws ProcessorException
	 * 		Problem while accessing the recognizer service.
	 * @throws ClientProtocolException
	 * 		Problem while accessing the recognizer service.
	 * @throws IOException
	 * 		Problem while accessing the recognizer service.
	 */
	private String performRecognition(String parsedText) throws ProcessorException, ClientProtocolException, IOException
	{	logger.log("Perform mention recognition");
		logger.increaseOffset();
		
		// define HTTP message
		logger.log("Define HTTP message for recognizer");
		HttpPost method = new HttpPost(RECOGNIZER_URL);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("input", parsedText));
		method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		// send to recognizer and retrieve answer
//		System.out.println(parsedText);	
		String result = sendReceiveRequest(method);
//		System.out.println(result);
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Sends a request to OpenNer and retrieve the answer.
	 * 
	 * @param method
	 * 		HTTP request to send to OpenNer.
	 * @return
	 * 		String representing the OpenNer answer.
	 * 
	 * @throws ClientProtocolException
	 * 		Problem while accessing the OpenNer service.
	 * @throws IOException
	 * 		Problem while accessing the OpenNer service.
	 * @throws ProcessorException
	 * 		Problem while accessing the OpenNer service.
	 */
	private String sendReceiveRequest(HttpPost method) throws ClientProtocolException, IOException, ProcessorException
	{	// send to service
		logger.log("Send message to service");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(method);
		int responseCode = response.getStatusLine().getStatusCode();
		logger.log("Response Code : " + responseCode);
		if(responseCode!=200)
		{	throw new ProcessorException("Received an error code ("+responseCode+") while accessing the service");
			//TODO maybe we should try again and issue a warning?
			//logger.log("WARNING: received an error code ("+responseCode+") from the OpenNer service");
		}
		
	    // read service answer
	 	logger.log("Read the service answer");
	    HttpEntity entity = response.getEntity();
	    InputStream inputStream = entity.getContent();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
	    StringBuffer sb = new StringBuffer();
	 	String line;
		while((line = reader.readLine()) != null)
		{	//logger.log(line);
			sb.append(line);
		}

		String result = sb.toString();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of string to mention type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("PERSON", EntityType.PERSON);
		CONVERSION_MAP.put("LOCATION", EntityType.LOCATION);
		CONVERSION_MAP.put("ORGANIZATION", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("DATE", EntityType.DATE);
	}
	/*
	 * Note: ignored mention types
	 * (cf https://github.com/opener-project/kaf/wiki/KAF-structure-overview#nerc)
	 * Time
	 * Money
	 * Percent
	 * Misc (phone number, id card, bank number, addresses...)
	 */
	
	/////////////////////////////////////////////////////////////////
	// XML NAMES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Element containing the list of all tokenized words */
	private final static String ELT_TEXT = "text";
	/** Element containing the list of all identified terms */
	private final static String ELT_TERMS = "terms";
	/** Element representing a term */
	private final static String ELT_TERM = "term";
	/** Element containing the list of all detected mentions */
	private final static String ELT_ENTITIES = "entities";
	/** Element representing a mention */
	private final static String ELT_ENTITY = "entity";
	/** Element containing the list of mentions to a mention */
	private final static String ELT_REFERENCES = "references";
	/** Element containing the list of external references for a mention */
	private final static String ELT_EXTERNAL_REFERENCES = "externalReferences";
	/** Element representing an external reference for a mention */
	private final static String ELT_EXTERNAL_REFERENCE = "externalReference";
	/** Element representing a mention to a mention (in a reference element) or a reference to a word (in a term element) */
	private final static String ELT_SPAN = "span";
	/** Element refering to a term constituting a mention mention */
	private final static String ELT_TARGET = "target";
	/** Element representing a word (word form) */
	private final static String ELT_WF = "wf";

	/** Attribute representing the id of a word */
	private final static String ATT_WID = "wid";
	/** Attribute representing the id of a term */
	private final static String ATT_TID = "tid";
	/** Attribute representing the id of a term or word in a target element */
	private final static String ATT_ID = "id";
	/** Attribute representing the type of a mention */
	private final static String ATT_TYPE = "type";
	/** Attribute representing the starting position of a word in the text */
	private final static String ATT_OFFSET = "offset";
	/** Attribute representing the length of a word in the text */
	private final static String ATT_LENGTH = "length";
	/** Attribute representing a knowledge base in an external reference */
	private final static String ATT_RESOURCE = "resource";
	/** Attribute representing a knowledge base id in an external reference */
	private final static String ATT_REFERENCE = "resource";
	/** Attribute representing a confidence score in an external reference */
	private final static String ATT_CONFIDENCE = "confidence";
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if mentions containing parentheses should be split */
	private boolean parenSplit = true;
	
	@Override
	public Mentions convert(Article article, List<String> data) throws ProcessorException
	{	logger.increaseOffset();
		Mentions result = new Mentions(recognizer.getName());
		
		logger.log("Processing each part of data and its associated answer");
		Iterator<String> it = data.iterator();
		logger.increaseOffset();
		int i = 0;
		int prevSize = 0;
		while(it.hasNext())
		{	i++;
			logger.log("Processing part "+i+"/"+data.size()/2);
			String originalText = it.next();
			String openerAnswer = it.next();
			
			try
			{	// build DOM
				logger.log("Build DOM");
				SAXBuilder sb = new SAXBuilder();
				Document doc = sb.build(new StringReader(openerAnswer));
				Element root = doc.getRootElement();
				
				// index all the detected words
				logger.log("Index all detected words");
				Map<String,Element> wordMap = new HashMap<String,Element>();
				Element textElt = root.getChild(ELT_TEXT);
				List<Element> wordElts = textElt.getChildren(ELT_WF);
				for(Element wordElt: wordElts)
				{	String wid = wordElt.getAttributeValue(ATT_WID); 
					wordMap.put(wid,wordElt);
				}
				
				// index all the detected terms
				logger.log("Index all detected terms");
				Map<String,Element> termMap = new HashMap<String,Element>();
				Element termsElt = root.getChild(ELT_TERMS);
				List<Element> termElts = termsElt.getChildren(ELT_TERM);
				for(Element termElt: termElts)
				{	String tid = termElt.getAttributeValue(ATT_TID); 
					termMap.put(tid,termElt);
				}
				
				// process all mention elements
				logger.log("Create mention objects");
				Element entitiesElt = root.getChild(ELT_ENTITIES);
				if(entitiesElt!=null)
				{	List<Element> entityElts = entitiesElt.getChildren(ELT_ENTITY);
					for(Element entityElt: entityElts)
					{	AbstractMention<?,?> mention = convertElement(entityElt, wordMap, termMap, prevSize, originalText);
						if(mention!=null)
						{	// possibly split in two distinct, smaller mentions when containing parentheses
							AbstractMention<?,?>[] temp = processParentheses(mention);
							if(temp==null)
								result.addMention(mention);
							else
							{	for(AbstractMention<?,?> t: temp)
									result.addMention(t);
							}
						}
					}
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
		return result;
	}
	
	/**
	 * Receives an XML element and extract
	 * the information necessary to create
	 * a mention.
	 *  
	 * @param element
	 * 		Element to process.
	 * @param wordMap
	 * 		List of identified words.
	 * @param termMap
	 * 		List of identified terms.
	 * @param prevSize
	 * 		Size of the parts already processed. 
	 * @param part
	 * 		Part of the original text currently processed. 
	 * @return
	 * 		The resulting mention, or {@code null} if its
	 * 		type is not supported.
	 */
	private AbstractMention<?,?> convertElement(Element element, Map<String,Element> wordMap, Map<String,Element> termMap, int prevSize, String part)
	{	AbstractMention<?,?> result = null;
		
		String typeCode = element.getAttributeValue(ATT_TYPE);
		EntityType type = CONVERSION_MAP.get(typeCode);
		
		if(type!=null)
		{	// internal references
			Element referencesElt = element.getChild(ELT_REFERENCES);
			
			// process each span element
			List<Element> spanElts = referencesElt.getChildren(ELT_SPAN);
			if(spanElts.size()>1)
				logger.log("WARNING: several mentions to the same entity, this info could be used!");
			for(Element spanElt: spanElts)
			{	// process each target (=term) in the span
				List<Element> targetElts = spanElt.getChildren(ELT_TARGET);
				int startPos = -1;
				int endPos = 0;
				for(Element targetElt: targetElts)
				{	// get the refered term id
					String id = targetElt.getAttributeValue(ATT_ID);
					// get the corresponding term element
					Element termElt = termMap.get(id);
					
					// retrieve its constituting words
					Element spanElt2 = termElt.getChild(ELT_SPAN);
					List<Element> targetElts2 = spanElt2.getChildren(ELT_TARGET);
					int startPos2 = -1;
					int endPos2 = 0;
					for(Element targetElt2: targetElts2)
					{	// get the referred word id
						String id2 = targetElt2.getAttributeValue(ATT_ID);
						// get the corresponding word element
						Element wordElt = wordMap.get(id2);
						
						// get its position
						String offsetStr = wordElt.getAttributeValue(ATT_OFFSET);
						int offset = Integer.parseInt(offsetStr);
						String lengthStr = wordElt.getAttributeValue(ATT_LENGTH);
						int length = Integer.parseInt(lengthStr);
						
						// update term position
						if(startPos2<0)
							startPos2 = offset;
						endPos2 = offset + length;
					}
					
					// update mention position
					if(startPos<0)
						startPos = startPos2;
					endPos = endPos2;
				}
				
				// create mention
				String valueStr = part.substring(startPos, endPos);
				startPos = startPos + prevSize;
				endPos = endPos + prevSize;
				result = AbstractMention.build(type, startPos, endPos, recognizer.getName(), valueStr);
				
				// TODO we could add a unique code to the several mentions of the same entity
			}
			
			// external references
			Element extReferencesElt = element.getChild(ELT_EXTERNAL_REFERENCES);
			if(extReferencesElt!=null)
			{	// TODO we could retrieve the assocated knowledge base references
				// https://github.com/opener-project/kaf/wiki/KAF-structure-overview#nerc
				List<Element> extReferenceElts = extReferencesElt.getChildren(ELT_EXTERNAL_REFERENCE);
				logger.log("Found the following external references for mention "+result);
				logger.increaseOffset();
					for(Element extReferenceElt: extReferenceElts)
					{	String resource = extReferenceElt.getAttributeValue(ATT_RESOURCE);
						String reference = extReferenceElt.getAttributeValue(ATT_REFERENCE);
						String confidence = extReferenceElt.getAttributeValue(ATT_CONFIDENCE);
						logger.log("resource:"+resource+" reference:"+reference+" confidence:"+confidence);
					}
				logger.decreaseOffset();
			}
		}
		
		return result;
	}
	
	/**
	 * On strings such as "Limoges (Haute-Vienne)", OpeNer tends to detect a single mention
	 * when there are actually two ("Limoges" and "Haute-Vienne"). This method allows to
	 * post-process such results, in order to get both mentions.
	 * 
	 * @param mention
	 * 		The original mention, containing both mentions.
	 * @return
	 * 		An array containing the two smaller mentions, or {@code null} if
	 * 		the specified mention was not of the desired form.
	 */
	private AbstractMention<?,?>[] processParentheses(AbstractMention<?,?> mention)
	{	AbstractMention<?,?>[] result = null;
		
		if(parenSplit && !(mention instanceof MentionDate))
		{	// get mention info
			String original = mention.getStringValue();
			int startPos = mention.getStartPos();
			EntityType type = mention.getType();
			ProcessorName source = mention.getSource();
	
			// analyze the original string
			int startPar = original.lastIndexOf('(');
			int endPar = original.lastIndexOf(')');
			if(startPar!=-1 && endPar!=-1 && startPar<endPar  // we need both opening and closing parentheses
					&& !(startPar==0 && endPar==original.length()-1)) // to avoid treating things like "(Paris)" 
			{	// first mention
				String valueStr1 = original.substring(0,startPar);
				int startPos1 = startPos;
				int endPos1 = startPos + startPar;
				AbstractMention<?,?> mention1 = AbstractMention.build(type, startPos1, endPos1, source, valueStr1);
//if(valueStr1.isEmpty())
//	System.out.print("");

				// second mention
				String valueStr2 = original.substring(startPar+1,endPar);
				int startPos2 = startPos + startPar + 1;
				int endPos2 = startPos + endPar;
				AbstractMention<?,?> mention2 = AbstractMention.build(type, startPos2, endPos2, source, valueStr2);
//if(valueStr2.isEmpty())
//	System.out.print("");
				
				result = new AbstractMention<?,?>[]{mention1,mention2};
			}
		}
		
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
					temp = temp + "\n>>> Part " + (i/2) + "/" + intRes.size()/2 + " - OpeNer Response <<<\n" + kafTxt + "\n";
        		}
        		catch (JDOMException e)
        		{	e.printStackTrace();
        		}
        	}
    	}
        
        writeRawResultsStr(article, temp);
    }
}
