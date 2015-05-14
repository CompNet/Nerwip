package tr.edu.gsu.nerwip.recognition.internal.modelless.opener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.modelless.AbstractModellessInternalRecognizer;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais.OpenCalaisConverter;
import tr.edu.gsu.nerwip.tools.keys.KeyHandler;

public class OpeNER extends AbstractModellessInternalRecognizer<String,OpeNERConverter>
{
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
	// ENTITIES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entities recognized by OpenCalais */
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
//			ArticleLanguage.EN,//TODO à compléter
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


	@Override
	protected String detectEntities(Article article) throws RecognizerException
	{	logger.increaseOffset();
	String test = new String();
	test = "Barack Obama est né le 4 août 1961 à Honolulu (Hawaï)";
	String text = article.getRawText();
	String text1 = new String();
	String text2 = new String();
	String text3 = new String();
	String openerAnswer = new String();
	
	String line = new String();
	String sbresult = new String();
	try
	{	// define HTTP message
		logger.log("Build tokenizer http message");
		String url = "http://opener.olery.com/tokenizer";
		HttpPost method = new HttpPost(url);
		//Request parameters 
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("input", test));
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
	    // read answer
	 	logger.log(">>>>>>>>>>>>>>>>>>>>>>>Read tokenizer answer");
		while((line = rd.readLine()) != null)
		{
			//logger.log(line);
			res.append(line);
		}
		text1 = res.toString();
		//logger.log("text1>>>>>>>>>>>>>>>>>>>>>>>" + text1);
		
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
				params1.add(new BasicNameValuePair("input", text1));
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
			    // read answer
			 	logger.log(">>>>>>>>>>>>>>>>>>>Read pos_tagger answer");
				while((line = rd.readLine()) != null)
				{
					//logger.log(line);
					res.append(line);
				}
				text2 = res.toString();
				//logger.log("text2>>>>>>>>>>>>>>>>>>>>>>>>>" + text2);
				
				
				
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
						params2.add(new BasicNameValuePair("input", text2));
						method2.setEntity(new UrlEncodedFormEntity(params2, "UTF-8"));
						
						// send to pos_tagger
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
					    // read answer
					 	logger.log(">>>>>>>>>>>>>>>>>>>Read pos_tagger answer");
						while((line = rd.readLine()) != null)
						{
							//logger.log(line);
							res.append(line);
						}
						text3 = res.toString();
						//logger.log("text3>>>>>>>>>>>>>>>>>>>>>>>>>" + text3);
						
						
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
								params3.add(new BasicNameValuePair("input", text3));
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
							    // read answer
							 	logger.log(">>>>>>>>>>>>>>>>>>>>>>>>>> Read ner answer");
								while((line = rd.readLine()) != null)
								{
									logger.log(line);
									res.append(line);
								}
								openerAnswer = res.toString();
								//logger.log("text3>>>>>>>>>>>>>>>>>>>>>>>>>" + text3);
								
								
								logger.log("Build DOM");
								  String entite = new String();
								  
								  SAXBuilder sb = new SAXBuilder();
								  Document doc = sb.build(new StringReader(openerAnswer));
								  Element root = doc.getRootElement();
								  Element entityElt = root.getChild("entity");
								  entite = entityElt.getValue();
									logger.log(">>>>>>>>>>>>>>>>>>>entities =" + entite);
						
				
		
		
		
		

	
	
	
	
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
	} catch (JDOMException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	logger.decreaseOffset();


	// logger.decreaseOffset();
	return openerAnswer;
	}
}

