package tr.edu.gsu.nerwip.recognition.internal.modelless.opener;

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
import tr.edu.gsu.nerwip.tools.string.StringTools;

/**
 * This class acts as an interface with the OpeNER Web service.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code ignorePronouns}: {@code true}</li>
 * 		<li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * <br/>
 * Official OpeNER website: 
 * <a href="http://www.opener-project.eu/webservices/">
 * http://www.opener-project.eu/webservices/</a>
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class OpeNer extends AbstractModellessInternalRecognizer<List<String>,OpeNerConverter>
{
	/**
	 * Builds and sets up an object representing
	 * the OpeNER NER tool.
	 * 
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop words should be excluded from the detection.
	 */
	public OpeNer(boolean ignorePronouns, boolean exclusionOn)
	{	super(false,ignorePronouns,exclusionOn);
		
		// init converter
		converter = new OpeNerConverter(getFolder());
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public RecognizerName getName()
	{	return RecognizerName.OPENER;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entities recognized by OpeNER */
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
	/** Entity recognizer URL */
	private static final String RECOGNIZER_URL = SERVICE_URL + "/ner";
	/** Maximal request size */
	private static final int MAX_SIZE = 1000;
	/** Sleep periods (in ms) */
	private static final long SLEEP_PERIOD = 5000;


	@Override
	protected List<String> detectEntities(Article article) throws RecognizerException
	{	logger.increaseOffset();
		List<String> openerAnswer = new ArrayList<String>();
		String text = article.getRawText();
		
		// we need to break down the text
		List<String> parts = StringTools.splitText(text, MAX_SIZE);
	
		
		
		
	
	
		
		String answer = new String();
		
		String line = new String();
	
		for(int i=0;i<parts.size();i++)
		{	logger.log("Processing OpeNer part #"+(i+1)+"/"+parts.size());
			logger.increaseOffset();
			String part = parts.get(i);
		
			try
			{	// tokenize the text
				String tokenizedText = performTokenization(part);
				Thread.sleep(SLEEP_PERIOD); //TODO is it really necessary to sleep like this ?

				// apply the pos tagger
				String taggedText = performTagging(tokenizedText);
				Thread.sleep(SLEEP_PERIOD);
				
				// apply the constituent parser
				String parsedText = performParsing(taggedText);
				Thread.sleep(SLEEP_PERIOD);
				
				// perform the NER
				String nerText = performRecognition(parsedText);


				String kaf ="<KAF xml:lang=\"fr\" version=\"v1.opener\">";
		        String kaf1 = "<KAF>";
				answer = nerText.replaceAll(kaf, kaf1);
				openerAnswer.add(part);
				openerAnswer.add(answer);
				
				
			}
			catch (UnsupportedEncodingException e)
			{	e.printStackTrace();
				throw new RecognizerException(e.getMessage());
			}
			catch (ClientProtocolException e)
			{	e.printStackTrace();
				throw new RecognizerException(e.getMessage());
			}
			catch (IOException e)
			{	e.printStackTrace();
				throw new RecognizerException(e.getMessage());
			} 
		}
		
		return openerAnswer;
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
	 * @throws RecognizerException
	 * 		Problem while accessing the tokenizer service.
	 * @throws ClientProtocolException
	 * 		Problem while accessing the tokenizer service.
	 * @throws IOException
	 * 		Problem while accessing the tokenizer service.
	 */
	private String performTokenization(String part) throws RecognizerException, ClientProtocolException, IOException
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
		
		// send to tokenizer
		logger.log("Send message to tokenizer");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(method);
		int responseCode = response.getStatusLine().getStatusCode();
		logger.log("Response Code : " + responseCode);
		if(responseCode!=200)
		{	throw new RecognizerException("Received an error code ("+responseCode+") while accessing the tokenizer");
			//TODO maybe we should try again and issue a warning?
			//logger.log("WARNING: received an error code ("+responseCode+") from the tokenizer");
		}
	    
	    // read tokenizer answer
	    HttpEntity entity = response.getEntity();
	    InputStream inputStream = entity.getContent();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
	    StringBuffer res = new StringBuffer();
	 	logger.log("Read the tokenizer answer");
	 	String line;
		while((line = reader.readLine()) != null)
		{	//logger.log(line);
			res.append(line);
		}
		
		logger.decreaseOffset();
		String result = res.toString();
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
	 * @throws RecognizerException
	 * 		Problem while accessing the tagger service.
	 * @throws ClientProtocolException
	 * 		Problem while accessing the tagger service.
	 * @throws IOException
	 * 		Problem while accessing the tagger service.
	 */
	private String performTagging(String tokenizedText) throws RecognizerException, ClientProtocolException, IOException
	{	logger.log("Perform PoS tagging");
		logger.increaseOffset();
		
		// define HTTP message
		logger.log("Define HTTP message for tagger");
		HttpPost method = new HttpPost(TAGGER_URL);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("input", tokenizedText));
		method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		// send to tagger
		logger.log("Send message to tagger");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(method);
		int responseCode = response.getStatusLine().getStatusCode();
		logger.log("Response Code : " + responseCode);
		if(responseCode!=200)
		{	throw new RecognizerException("Received an error code ("+responseCode+") while accessing the tokenizer");
			//TODO maybe we should try again and issue a warning?
			//logger.log("WARNING: received an error code ("+responseCode+") from the tagger");
		}
		
	    // read tagger answer
	    HttpEntity entity = response.getEntity();
	    InputStream inputStream = entity.getContent();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
	    StringBuffer res = new StringBuffer();
	 	logger.log("Read the tagger answer");
	 	String line;
		while((line = reader.readLine()) != null)
		{	//logger.log(line);
			res.append(line);
		}
		
		logger.decreaseOffset();
		String result = res.toString();
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
	 * @throws RecognizerException
	 * 		Problem while accessing the parser service.
	 * @throws ClientProtocolException
	 * 		Problem while accessing the parser service.
	 * @throws IOException
	 * 		Problem while accessing the parser service.
	 */
	private String performParsing(String taggedText) throws RecognizerException, ClientProtocolException, IOException
	{	logger.log("Perform constituent parsing");
		logger.increaseOffset();
	
		// define HTTP message
		logger.log("Define HTTP message for parser");
		HttpPost method = new HttpPost(PARSER_URL);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("input", taggedText));
		method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		// send to parser
		logger.log("Send message to parser");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(method);
		int responseCode = response.getStatusLine().getStatusCode();
		logger.log("Response Code : " + responseCode);
		if(responseCode!=200)
		{	throw new RecognizerException("Received an error code ("+responseCode+") while accessing the tokenizer");
			//TODO maybe we should try again and issue a warning?
			//logger.log("WARNING: received an error code ("+responseCode+") from the parser");
		}
		
	    // read tagger answer
	    HttpEntity entity = response.getEntity();
	    InputStream inputStream = entity.getContent();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
	    StringBuffer res = new StringBuffer();
	 	logger.log("Read the parser answer");
	 	String line;
		while((line = reader.readLine()) != null)
		{	//logger.log(line);
			res.append(line);
		}
		
		logger.decreaseOffset();
		String result = res.toString();
		return result;
	}

	/**
	 * Sends the parsed text to the OpenNer entity recognizer,
	 * as a fourth processing step.
	 * 
	 * @param parsedText
	 * 		The previously parsed text.
	 * @return
	 * 		Text with the detected entities.
	 * 
	 * @throws RecognizerException
	 * 		Problem while accessing the recognizer service.
	 * @throws ClientProtocolException
	 * 		Problem while accessing the recognizer service.
	 * @throws IOException
	 * 		Problem while accessing the recognizer service.
	 */
	private String performRecognition(String parsedText) throws RecognizerException, ClientProtocolException, IOException
	{	logger.log("Perform entity recognition");
		logger.increaseOffset();
		
		// define HTTP message
		logger.log("Define HTTP message for recognizer");
		HttpPost method = new HttpPost(RECOGNIZER_URL);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("input", parsedText));
		method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		// send to recognizer
		logger.log("Send message to recognizer");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(method);
		int responseCode = response.getStatusLine().getStatusCode();
		logger.log("Response Code : " + responseCode);
		if(responseCode!=200)
		{	throw new RecognizerException("Received an error code ("+responseCode+") while accessing the recognizer");
			//TODO maybe we should try again and issue a warning?
			//logger.log("WARNING: received an error code ("+responseCode+") from the recognizer");
		}
		
	    // read recognizer answer
	    HttpEntity entity = response.getEntity();
	    InputStream inputStream = entity.getContent();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
	    StringBuffer res = new StringBuffer();
	 	logger.log("Read the parser recognizer");
	 	String line;
		while((line = reader.readLine()) != null)
		{	//logger.log(line);
			res.append(line);
		}
		
		logger.decreaseOffset();
		String result = res.toString();
		return result;
	}
}
