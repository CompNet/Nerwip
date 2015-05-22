package tr.edu.gsu.nerwip.recognition.internal.modelless.opener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * Official OpeNER website: <a href="http://www.opener-project.eu/webservices/">http://www.opener-project.eu/webservices/</a>
 * 
 * @author Sabrine Ayachi
 * 
 */


public class OpeNER extends AbstractModellessInternalRecognizer<List<String>,OpeNERConverter>
{
	/**
	 * Builds and sets up an object representing
	 * an OpeNER NER tool.
	 * 
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop words should be excluded from the detection.
	 */
	public OpeNER(boolean ignorePronouns, boolean exclusionOn)
	{	super(false,ignorePronouns,exclusionOn);

	// init converter
	converter = new OpeNERConverter(getFolder());
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
	// ENTITY TYPES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entities recognized by OpeNER */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList(
			EntityType.DATE,
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
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList(
			ArticleLanguage.EN,
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
	private static final int MAX_SIZE = 500;

	@Override
	protected List<String> detectEntities(Article article) throws RecognizerException
	{	
	//String test = new String();
	//test = "Barack Obama est né le 4 août 1961 à Honolulu (Hawaï)";
	logger.increaseOffset();
	List<String> openerAnswer = new ArrayList<String>();
	String text = article.getRawText();
	String tokenizedText = new String();
	String posTaggedText = new String();
	String parsedText = new String();
	String nerText = new String();
	String answer = new String();
	
	String line = new String();
	String test = "Né le 15 septembre 1886 à Châtellerault (Vienne) , il est mort en juillet 1900";
	
	
	
	List<String> parts = StringTools.splitText(text, MAX_SIZE);
	for(int i=0;i<parts.size();i++)
	{
		logger.log("Processing OpeNER chunk #"+(i+1)+"/"+parts.size());
		logger.increaseOffset();
		String part = parts.get(i);
	
	try
	{	// define HTTP message
		logger.log("Build tokenizer http message");
		String url = "http://opener.olery.com/tokenizer";
		HttpPost method = new HttpPost(url);
		//Request parameters 
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
	    {	
			logger.log("WARNING: received an error code ("+responseCode+") >> trying again");
	    }
	    
	    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
	    StringBuffer res = new StringBuffer();
	    // read tokenizer answer
	 	logger.log(">>>>>>>>>>>>>>>>>>>>>>>Read tokenizer answer");
		while((line = rd.readLine()) != null)
		{
			//logger.log(line);
			res.append(line);
		}
		tokenizedText = res.toString();
		
		
		try {
			Thread.sleep(5000);
			}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		// define HTTP message
		logger.log("Build pos_tagger http message");
		String url1 = "http://opener.olery.com/pos-tagger";
		HttpPost method1 = new HttpPost(url1);
		//Request parameters 
		List<NameValuePair> params1 = new ArrayList<NameValuePair>();
		params1.add(new BasicNameValuePair("input", tokenizedText));
		method1.setEntity(new UrlEncodedFormEntity(params1, "UTF-8"));
				
		// send to pos_tagger
		logger.log("Send message to pos_tagger");
	    client = new DefaultHttpClient();
		response = client.execute(method1);
		responseCode = response.getStatusLine().getStatusCode();
	    logger.log("Response Code : " + responseCode);
	    if(responseCode!=200)
	    {
	    	logger.log("WARNING: received an error code ("+responseCode+") >> trying again");
	    	}
	    rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
	    res = new StringBuffer();
		// read pos_tagger answer
		logger.log(">>>>>>>>>>>>>>>>>>>>>>>Read pos_tagger answer");
		while((line = rd.readLine()) != null)
		{
			//logger.log(line);
			res.append(line);
			}
		posTaggedText = res.toString();
		try {
			Thread.sleep(5000);
			}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		// define HTTP message
		logger.log("Build constituent_parser http message");
		String url2 = "http://opener.olery.com/constituent-parser";
		HttpPost method2 = new HttpPost(url2);
		//Request parameters 
		List<NameValuePair> params2 = new ArrayList<NameValuePair>();
		params2.add(new BasicNameValuePair("input", posTaggedText));
		method2.setEntity(new UrlEncodedFormEntity(params2, "UTF-8"));
		// send to parser
		logger.log("Send message to constituent_parser  ");
		client = new DefaultHttpClient();
		response = client.execute(method2);
		responseCode = response.getStatusLine().getStatusCode();
		logger.log("Response Code : " + responseCode);
		if(responseCode!=200)
		{
			logger.log("WARNING: received an error code ("+responseCode+") >> trying again");
			}
		rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
		res = new StringBuffer();
		// read parser answer
		logger.log(">>>>>>>>>>>>>>>>>>>>>>>Read pos_tagger answer");
		while((line = rd.readLine()) != null)
		{
			//logger.log(line);
			res.append(line);
			}
		parsedText = res.toString();
		try {
			Thread.sleep(5000);
			}
		catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							}
		// define HTTP message
		logger.log("Build ner http message");
		String url3 = "http://opener.olery.com/ner";
		HttpPost method3 = new HttpPost(url3);
		//Request parameters 
		List<NameValuePair> params3 = new ArrayList<NameValuePair>();
		params3.add(new BasicNameValuePair("input", parsedText));
		method3.setEntity(new UrlEncodedFormEntity(params3, "UTF-8"));
								
		// send to ner
		logger.log("Send message to ner  ");
		client = new DefaultHttpClient();
	    response = client.execute(method3);
		responseCode = response.getStatusLine().getStatusCode();
		logger.log("Response Code : " + responseCode);
        if(responseCode!=200)
        {
        	logger.log("WARNING: received an error code ("+responseCode+") >> trying again");
        	}
        rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
		res = new StringBuffer();
		// read ner answer
		logger.log(">>>>>>>>>>>>>>>>>>>>>>>Read ner answer");
		while((line = rd.readLine()) != null)
		{
			//logger.log(line);
			res.append(line);
			}
		nerText = res.toString();
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
	}

