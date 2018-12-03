package fr.univavignon.search.engines.web;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.tools.keys.KeyHandler;
import fr.univavignon.tools.web.WebTools;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * This class uses Yandex to search the Web.
 * It takes advantage of the undocumented API.
 * <br/>
 * Dates are explicitly taken into account in the
 * Yandex API (unlike some other search engines).
 * <br/>
 * <b>URL used as resources:</b>
 * https://tech.yandex.com/xml/
 * https://yandex.com/support/search/robots/search-api.html
 * https://yandex.com/support/search/how-to-search/search-operators.html
 * 
 * @author Vincent Labatut
 */
public class YandexEngine extends AbstractWebEngine
{
	/**
	 * Initializes the object used to search
	 * the Web with the Yandex API.
	 * 
	 * @param website
	 * 		Target site, or {@code null} to search the whole Web.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * @param language
	 * 		Targeted language. 
	 */
	public YandexEngine(String website, Date startDate, Date endDate, ArticleLanguage language)
	{	super(website,startDate,endDate);
		
		switch(language)
		{	case EN:
				pageLanguage = "en";
				break;
			case FR:
				pageLanguage = "fr";
				break;
			
		}
	}

	/////////////////////////////////////////////////////////////////
	// SERVICE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the API user */
	private static final String API_USER_NAME = "YandexUser";
    /** API user */ 
	private static final String API_USER = KeyHandler.KEYS.get(API_USER_NAME);
	/** Name of the API key */
	private static final String API_KEY_NAME = "YandexKey";
    /** API key */ 
	private static final String API_KEY = KeyHandler.KEYS.get(API_KEY_NAME);
	/** URL of the Yandex Search service */
	private static final String SERVICE_URL = "https://yandex.com/search/xml?maxpassages=1";
    /** Service authorization */ 
	private static final String SERVICE_PARAM_AUTH = "&user="+API_USER+"&key="+API_KEY;
	/** Query to send to the service */
	private static final String SERVICE_PARAM_QUERY = "&query=";
	/** Number of the page of results */
	private static final String SERVICE_PARAM_PAGE = "&page=";
	/** Query a specific date range */
	private static final String QUERY_PARAM_DATE = "date:"; // date:20071215..20080101
	/** Date range separator */
	private static final String QUERY_PARAM_DATE_SEP = "..";
	/** Query a specific language */
	private static final String QUERY_PARAM_LANGUAGE = "lang:";
	/** Query a specific Website */
	private static final String QUERY_PARAM_WEBSITE = "site:";
	
	/////////////////////////////////////////////////////////////////
	// DATA			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Textual name of this engine */
	public static final String ENGINE_NAME = "Yandex";

	@Override
	public String getName()
	{	return ENGINE_NAME;
	}
	
	/////////////////////////////////////////////////////////////////
	// PARAMETERS	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Focus the search on pages in a certain language */
	public String pageLanguage = null;
	/** Maximal number of results (can be less if Yandex does not provide) */
	public int MAX_RES_NBR = 200;
	
	/////////////////////////////////////////////////////////////////
	// SEARCH		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Map<String,URL> search(String keywords)  throws IOException
	{	logger.log("Applying Yandex Search");
		logger.increaseOffset();
		Map<String,URL> result = new HashMap<String,URL>();
		
		// init search parameters
		logger.log("Keywords: "+keywords);
		String baseUrl = SERVICE_URL + SERVICE_PARAM_AUTH; 
//				+ SERVICE_PARAM_PAGE + "1" 
//				+ SERVICE_PARAM_QUERY + "query";
		String query = QUERY_PARAM_LANGUAGE + pageLanguage + " " + keywords;
		if(website==null)
			logger.log("No website specified");
		else
		{	logger.log("Search restricted to website: "+website);
			query = QUERY_PARAM_WEBSITE + website + " " + query;
		}
		
		// if there is a time range constraint: complete the request with the appropriate parameter
		if(startDate!=null && endDate!=null)
		{	logger.log("Dates detected: "+startDate+"-"+endDate);
			//	format: 20071215..20080101
			Calendar cal = Calendar.getInstance();
	    	cal.setTime(startDate);
	    	int year = cal.get(Calendar.YEAR);
	    	int month = cal.get(Calendar.MONTH) + 1;
	    	int day = cal.get(Calendar.DAY_OF_MONTH);
	    	String startDateStr = year+String.format("%02d",month)+String.format("%02d",day);
	    	cal.setTime(endDate);
	    	year = cal.get(Calendar.YEAR);
	    	month = cal.get(Calendar.MONTH) + 1;
	    	day = cal.get(Calendar.DAY_OF_MONTH);
	    	String endDateStr = year+String.format("%02d",month)+String.format("%02d",day);
	    	query = query + " " + QUERY_PARAM_DATE + startDateStr + QUERY_PARAM_DATE_SEP + endDateStr;	    	
		}
		
		// process the request
		try
		{	searchYandex(baseUrl, query, result);
		}
		catch(JDOMException e)
    	{	e.printStackTrace();
			throw new IOException(e.getMessage());	
    	}
		
		logger.log("Search terminated: "+result.size()+"/"+MAX_RES_NBR+" results retrieved");
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Invokes Yandex Search API using the specified search parameters,
	 * and completes the list of results accordingly.
	 * 
	 * @param baseUrl
	 * 		Base URL (to be completed).
	 * @param query
	 * 		Base query (to be completed, too).
	 * @param result
	 * 		Current list of URL.
	 * 
	 * @throws ClientProtocolException
	 * 		Problem while invoking Yandex.
	 * @throws IOException
	 * 		Problem while invoking Yandex.
	 * @throws JDOMException
	 * 		Problem while parsing Yandex JSON results.
	 */
	private void searchYandex(String baseUrl, String query, Map<String,URL> result) throws ClientProtocolException, IOException, JDOMException
	{	int resIdx = 1;
		
		// repeat because of the pagination system
		int page = 0;
		boolean goOn = true;
		do
		{	logger.log("Getting page "+ page +" of the results");
			
			// setup query
			logger.log("Query: \""+query+"\"");
			// setup url
			String url = baseUrl + SERVICE_PARAM_PAGE + page
				+ SERVICE_PARAM_QUERY + URLEncoder.encode(query, "UTF-8");
			logger.log("URL: "+url);
			
			// query the server	
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpClient.execute(request);
			
			// parse the XML response
			logger.increaseOffset();
			String answer = WebTools.readAnswer(response);
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(answer));
			Element rootElt = doc.getRootElement();
			Element responseElt = rootElt.getChild("response");
			Element errorElt = responseElt.getChild("error");
			if(errorElt==null)
			{	Element resultsElt = responseElt.getChild("results");
				Element groupingElt = resultsElt.getChild("grouping");
				List<Element> groupElts = groupingElt.getChildren("group");
				for(Element groupElt: groupElts)
				{	List<Element> docElts = groupElt.getChildren("doc");
					for(Element docElt: docElts)
					{	Element urlElt = docElt.getChild("url");
						String urlStr = urlElt.getTextTrim();
						logger.log("Processing URL: "+urlStr);
						logger.increaseOffset();
							URL val = new URL(urlStr);
							if(result.containsValue(val))
								logger.log("URL already in the result list (was returned several times by the search engine)");
							else
							{	String rankStr = Integer.toString(resIdx);
								logger.log("Adding with rank "+rankStr);
								result.put(rankStr,val);
								resIdx++;
							}
						logger.decreaseOffset();
					}
				}
				
				// go to next result page
				page++;
			}
			else
			{	goOn = false;
				String errorCode = errorElt.getAttributeValue("code");
				String errorMsg = errorElt.getTextTrim();
				logger.log("Error: \""+errorMsg+"\" (code="+errorCode+")");
			}
			logger.decreaseOffset();
		}
		while(result.size()<MAX_RES_NBR && goOn);
	}

	/////////////////////////////////////////////////////////////////
	// TEST			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Method used to test/debug this class.
	 * 
	 * @param args
	 * 		None needed.
	 * @throws Exception
	 * 		All exceptions are thrown.
	 */
	public static void main(String[] args) throws Exception
	{	
		String keywords = "François Hollande";
		String website = null;//"http://lemonde.fr";
		Date startDate = null;//new GregorianCalendar(2016,3,1).getTime();
		Date endDate = null;//new GregorianCalendar(2016,3,2).getTime();
		
		YandexEngine engine = new YandexEngine(website, startDate, endDate, ArticleLanguage.FR);
		
		Map<String,URL> result = engine.search(keywords);
		
		System.out.println(result);
	}
}
