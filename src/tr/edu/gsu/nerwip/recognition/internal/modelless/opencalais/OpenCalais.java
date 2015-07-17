package tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais;

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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.modelless.AbstractModellessInternalRecognizer;
import tr.edu.gsu.nerwip.tools.keys.KeyHandler;
import tr.edu.gsu.nerwip.tools.string.StringTools;

/**
 * This class acts as an interface with the OpenCalais Web service.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code ignorePronouns}: {@code true}</li>
 * 		<li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * Official OpenCalais website: <a href="http://www.opencalais.com/">http://www.opencalais.com/</a>
 * <br/>
 * <b>Note:</b> if you use this tool, make sure you set up your license key
 * in the file res/misc/key.xml using the exact name "OpenCalais".
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class OpenCalais extends AbstractModellessInternalRecognizer<List<String>,OpenCalaisConverter>
{	
	/**
	 * Builds and sets up an object representing
	 * an OpenCalais NER tool.
	 * 
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop-words should be excluded from the detection.
	 */
	public OpenCalais(boolean ignorePronouns, boolean exclusionOn)
	{	super(false,ignorePronouns,exclusionOn);
		
		setIgnoreNumbers(false);
		
		// init converter
		converter = new OpenCalaisConverter(getFolder());
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public RecognizerName getName()
	{	return RecognizerName.OPENCALAIS;
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
	/** List of entities detected by this recognizer */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList
	(	EntityType.DATE,		// only EN
		EntityType.FUNCTION,	// only EN
		EntityType.LOCATION,
		EntityType.ORGANIZATION,
		EntityType.PERSON,
		EntityType.PRODUCTION	// only EN
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
	/** Web service URL */
	private static final String SERVICE_URL = "http://api.opencalais.com/tag/rs/enrich";
	/** Key name for OpenCalais */
	public static final String KEY_NAME = "OpenCalais";
	/** Maximal request size */
	private static final int MAX_SIZE = 10000;
	
	@Override
	protected List<String> detectEntities(Article article) throws RecognizerException
	{	logger.increaseOffset();
		List<String> result = new ArrayList<String>();
		String text = article.getRawText();

		// check if the key was set
		String key = KeyHandler.KEYS.get(KEY_NAME);
		if(key==null)
			throw new NullPointerException("In order to use OpenCalais, you first need to set up your user key in file res/misc/keys.xml using the exact name \"OpenCalais\".");
		
		// we need to break down the text: OpenCalais can't handle more than 100000 chars at once
//		List<String> parts = new ArrayList<String>();
//		while(text.length()>95000)
//		{	int index = text.indexOf("\n",90000) + 1;
//			String part = text.substring(0, index);
//			parts.add(part);
//			text = text.substring(index);
//		}
//		parts.add(text);
		List<String> parts = StringTools.splitText(text, MAX_SIZE);
		
		for(int i=0;i<parts.size();i++)
		{	logger.log("Processing OpenCalais part #"+(i+1)+"/"+parts.size());
			logger.increaseOffset();
			String part = parts.get(i);
			
			try
			{	// define HTTP message
				logger.log("Build OpenCalais HTTP message");
				HttpPost method = new HttpPost(SERVICE_URL);
				method.setHeader("x-calais-licenseID", key);
				method.setHeader("Content-Type", "text/raw; charset=UTF-8");
				method.setHeader("Accept", "xml/rdf");
				method.setEntity(new StringEntity(part, "UTF-8"));
				
				// send to open calais
				logger.log("Send message to OpenCalais");
				HttpClient client = new DefaultHttpClient();
				HttpResponse response = client.execute(method);
				InputStream stream = response.getEntity().getContent();
				InputStreamReader streamReader = new InputStreamReader(stream,"UTF-8");
				BufferedReader bufferedReader = new BufferedReader(streamReader);
				
				// read answer
				logger.log("Read OpenCalais answer");
				StringBuilder builder = new StringBuilder();
				String line;
				int nbr = 0;
				while((line = bufferedReader.readLine())!=null)
				{	builder.append(line+"\n");
					nbr++;
					//logger.log("Line:" +line);
				}
				logger.log("Lines read: "+nbr);
				
				String answer = builder.toString();
				result.add(part);
				result.add(answer);
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
			
			logger.decreaseOffset();
		}
	
		logger.decreaseOffset();
		return result;
	}
}
