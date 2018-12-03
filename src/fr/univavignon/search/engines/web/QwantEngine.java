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
import fr.univavignon.tools.web.WebTools;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class uses Qwant to search the Web.
 * It takes advantage of the undocumented API.
 * <br/>
 * <b>Note:</b> There is no parameter to specify a period,
 * so when a period is specified, we simply make a different
 * search for each day constituting the period. 
 * <br/>
 * <b>URL used as resources:</b>
 * https://api.qwant.com/api/search/web?locale=fr_fr&offset=10&q=Fran%C3%A7ois%20hollande%2001%2f04%2f2016
 * https://api.qwant.com/api/search/news?locale=en_us&offset=10&q=francois%20hollande	
 * https://github.com/asciimoo/searx/blob/master/searx/engines/qwant.py
 * https://dyrk.org/2015/12/07/lorsque-qwant-offre-une-api-aux-pirates/
 * 
 * @author Vincent Labatut
 */
public class QwantEngine extends AbstractWebEngine
{
	/**
	 * Initializes the object used to search
	 * the Web with the Qwant API.
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
	public QwantEngine(String website, Date startDate, Date endDate, ArticleLanguage language)
	{	super(website,startDate,endDate);
		
		switch(language)
		{	case EN:
				pageLanguage = "en";
				break;
			case FR:
				pageLanguage = "fr";
				pageCountry = "fr";
				break;
		}
	}

	/////////////////////////////////////////////////////////////////
	// SERVICE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of results returned for one request (max: 10?) */
	private static final int PAGE_SIZE = 10;
	/** URL of the Qwant Search service */
	private static final String SERVICE_URL = "https://api.qwant.com/api/search/";
	/** Response filter */
	private static final String SERVICE_PARAM_FILTER = "web";
	/** Country/language */
	private static final String SERVICE_PARAM_LANGUAGE = "&locale=";
	/** Query to send to the service */
	private static final String SERVICE_PARAM_QUERY = "&q=";
	/** Number of results to return */
	private static final String SERVICE_PARAM_COUNT = "?count="+PAGE_SIZE;
	/** Number of results to skip */
	private static final String SERVICE_PARAM_OFFSET = "&offset=";
	/** Query a specific Website */
	private static final String QUERY_PARAM_WEBSITE = "site:";
	
	/////////////////////////////////////////////////////////////////
	// DATA			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Textual name of this engine */
	public static final String ENGINE_NAME = "Qwant";

	@Override
	public String getName()
	{	return ENGINE_NAME;
	}
	
	/////////////////////////////////////////////////////////////////
	// PARAMETERS	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Focus on pages hosted in a certain country */
	public String pageCountry = null;
	/** Focus on pages in a certain language */
	public String pageLanguage = null;
	/** Maximal number of results (can be less if Qwant does not provide) */
	public int MAX_RES_NBR = 200;
	
	/////////////////////////////////////////////////////////////////
	// SEARCH		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Map<String,URL> search(String keywords)  throws IOException
	{	logger.log("Applying Qwant Search");
		logger.increaseOffset();
		Map<String,URL> result = new HashMap<String,URL>();
		
		// init search parameters
		logger.log("Keywords: "+keywords);
		String baseUrl = SERVICE_URL 
				+ SERVICE_PARAM_FILTER 
				+ SERVICE_PARAM_COUNT
				+ SERVICE_PARAM_LANGUAGE
				+ pageLanguage;
		if(pageCountry!=null)
			baseUrl = baseUrl + "_" + pageCountry;
		String baseQuery = keywords;
		if(website==null)
			logger.log("No website specified");
		else
		{	logger.log("Search restricted to website: "+website);
			baseQuery = QUERY_PARAM_WEBSITE + website + " " + baseQuery;
		}
		
		// if there is a time range constraint
		if(startDate!=null && endDate!=null)
		{	logger.log("Dates detected: "+startDate+"-"+endDate);
			logger.increaseOffset();
			
			Calendar cal = Calendar.getInstance();
	    	cal.setTime(startDate);
	    	int year = cal.get(Calendar.YEAR);
	    	int month = cal.get(Calendar.MONTH) + 1;
	    	int day = cal.get(Calendar.DAY_OF_MONTH);
	    	LocalDate currentLocalDate = LocalDate.of(year, month, day);
	    	cal.setTime(endDate);
	    	year = cal.get(Calendar.YEAR);
	    	month = cal.get(Calendar.MONTH) + 1;
	    	day = cal.get(Calendar.DAY_OF_MONTH);
	    	LocalDate endLocalDate = LocalDate.of(year, month, day);
	    	
	    	// process separately each day of the considered time period
	    	int dayIdx = 1;
	    	while(currentLocalDate.isBefore(endLocalDate) || currentLocalDate.isEqual(endLocalDate))
	    	{	logger.log("Processing date "+currentLocalDate);
				try
	    		{	searchQwant(baseUrl, baseQuery, currentLocalDate, dayIdx, result);
	    		}
	    		catch(ParseException e)
		    	{	e.printStackTrace();
	    			throw new IOException(e.getMessage());	
		    	}
	    		
	    		// deal with the next day
	    		currentLocalDate = currentLocalDate.plusDays(1);
	    		dayIdx++;
	    	}
			logger.decreaseOffset();
		}
		
		// if there is no time constraint
		else
		{	logger.log("No date detected");
			
			try
			{	searchQwant(baseUrl, baseQuery, null, 0, result);
			}
			catch(ParseException e)
	    	{	e.printStackTrace();
				throw new IOException(e.getMessage());	
	    	}
		}
		
		logger.log("Search terminated: "+result.size()+"/"+MAX_RES_NBR+" results retrieved");
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Invokes Qwant Search API using the specified search parameters,
	 * and completes the list of results accordingly.
	 * 
	 * @param baseUrl
	 * 		Base URL (to be completed).
	 * @param baseQuery
	 * 		Base query (to be completed, too).
	 * @param targetedDate
	 * 		Date of the searched articles (can be {@code null}, if none).
	 * @param dayIdx 
	 * 		Number of the date in the targeted period.
	 * @param result
	 * 		Current list of URL.
	 * 
	 * @throws ClientProtocolException
	 * 		Problem while invoking Qwant.
	 * @throws IOException
	 * 		Problem while invoking Qwant.
	 * @throws ParseException
	 * 		Problem while parsing Qwant JSON results.
	 */
	private void searchQwant(String baseUrl, String baseQuery, LocalDate targetedDate, int dayIdx, Map<String,URL> result) throws ClientProtocolException, IOException, ParseException
	{	int resIdx = 1;
		
		// repeat because of the pagination system
		int lastRes = 0;
		boolean goOn = true;
		do
		{	logger.log("Getting results "+lastRes+"-"+(lastRes+PAGE_SIZE-1));
			
			// setup query
			String query = baseQuery;
			if(targetedDate!=null)
				query = query
				+ " " + String.format("%02d",targetedDate.getDayOfMonth()) 
				+ "/" + String.format("%02d",targetedDate.getMonthValue())
				+ "/" + targetedDate.getYear();
			logger.log("Query: \""+query+"\"");
			// setup url
			String url = baseUrl + SERVICE_PARAM_OFFSET + lastRes
				+ SERVICE_PARAM_QUERY + URLEncoder.encode(query, "UTF-8");
			logger.log("URL: "+url);
			
			// query the server	
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpClient.execute(request);
			
			// parse the JSON response
			String answer = WebTools.readAnswer(response);
			JSONParser parser = new JSONParser();
			JSONObject mainJson = (JSONObject)parser.parse(answer);
			JSONObject dataJson = (JSONObject)mainJson.get("data");
			JSONObject resultJson = (JSONObject)dataJson.get("result");
			if(resultJson==null)
			{	logger.log("WARNING: could not find any web results for this query");
				goOn = false;
			}
			else
			{	JSONArray itemsJson = (JSONArray)resultJson.get("items");
				logger.log("Found "+itemsJson.size()+" web results for this query");
				if(itemsJson.isEmpty())
				{	logger.log("WARNING: could not find any web results for this query");
					goOn = false;
				}
				else
				{	logger.increaseOffset();
					int i = 1;
					for(Object item: itemsJson)
					{	logger.log("Processing web result "+i+"/"+itemsJson.size());
						logger.increaseOffset();
						JSONObject itemJson = (JSONObject)item;
						String title = (String)itemJson.get("title");
						logger.log("title: "+title);
						String urlStr = (String)itemJson.get("url");
						logger.log("url: "+urlStr);
						URL resUrl = new URL(urlStr);
						String key;
						if(dayIdx==0)
							key = Integer.toString(resIdx);
						else
							key = dayIdx+"-"+resIdx;
						result.put(key,resUrl);
						resIdx++;
						logger.decreaseOffset();
						i++;
					}
					logger.decreaseOffset();
				}
			}
						
			// go to next result page
			lastRes = lastRes + PAGE_SIZE;
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
		
		QwantEngine engine = new QwantEngine(website, startDate, endDate, ArticleLanguage.FR);
		
		Map<String,URL> result = engine.search(keywords);
		
		System.out.println(result);
	}
}
