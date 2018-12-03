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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
 * This class uses Bing to search the Web. More
 * precisely, it uses the Bing Search API v5.
 * <br/>
 * <b>Note:</b> There is no parameter to specify a period,
 * so when a period is specified, we simply make a different
 * search for each day constituting the period. 
 * 
 * @author Vincent Labatut
 */
public class BingEngine extends AbstractWebEngine
{
	/**
	 * Initializes the object used to search
	 * the Web with the Bing Search API.
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
	public BingEngine(String website, Date startDate, Date endDate, ArticleLanguage language)
	{	super(website,startDate,endDate);
		
		switch(language)
		{	case EN:
				pageLanguage = "en";
				break;
			case FR:
				pageCountry = "FR";
				pageLanguage = "fr";
				break;
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// SERVICE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of results returned for one request (max: 50) */
	private static final int PAGE_SIZE = 50;
	/** URL of the Bing Search service */
	private static final String SERVICE_URL = "https://api.cognitive.microsoft.com/bing/v5.0/search";
	/** Query to send to the service */
	private static final String SERVICE_PARAM_QUERY = "&q=";
	/** Number of results to return */
	private static final String SERVICE_PARAM_COUNT = "?count="+PAGE_SIZE;
	/** Number of results to skip */
	private static final String SERVICE_PARAM_OFFSET = "&offset=";
	/** Country/language */
	private static final String SERVICE_PARAM_LANGUAGE = "&mkt=";
	/** Response filter */
	private static final String SERVICE_PARAM_FILTER = "&responseFilter=Webpages,News";
	/** Query a specific Website */
	private static final String QUERY_PARAM_WEBSITE = "site:";
	/** Object used to format dates */
	private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-mm-dd");
	/** Name of the first API key */
	private static final String API_KEY1_NAME = "MicrosoftSearch1";
//	/** Name of the second API key */
//	private static final String API_KEY2_NAME = "MicrosoftSearch2";
    /** First API key */ 
	private static final String API_KEY1 = KeyHandler.KEYS.get(API_KEY1_NAME);
//    /** Second API key */
//	private static final String APP_KEY2 = KeyHandler.KEYS.get(API_KEY2_NAME);
	
	/////////////////////////////////////////////////////////////////
	// DATA			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Textual name of this engine */
	public static final String ENGINE_NAME = "Bing";

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
	/** Maximal number of results (can be less if Bing does not provide) */
	public int MAX_RES_NBR = 200;
	
	/////////////////////////////////////////////////////////////////
	// SEARCH		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Map<String,URL> search(String keywords)  throws IOException
	{	logger.log("Applying Bing Search");
		logger.increaseOffset();
		Map<String,URL> result = new HashMap<String,URL>();
		
		// init search parameters
		logger.log("Keywords: "+keywords);
		String baseUrl = SERVICE_URL 
				+ SERVICE_PARAM_COUNT 
				+ SERVICE_PARAM_LANGUAGE + pageLanguage;
		if(pageCountry!=null)
			baseUrl = baseUrl + "-" + pageCountry;
		baseUrl = baseUrl + SERVICE_PARAM_FILTER;
		String baseQuery = "";
		if(website==null)
			logger.log("No website specified");
		else
		{	logger.log("Search restricted to website: "+website);
			baseQuery = QUERY_PARAM_WEBSITE + website + " " ;
		}
		baseQuery = baseQuery + keywords;
		
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
	    		{	searchBing(baseUrl, baseQuery, currentLocalDate, dayIdx, result);
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
			{	searchBing(baseUrl, baseQuery, null, 0, result);
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
	 * Invokes Bing Search API using the specified search parameters,
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
	 * 		Problem while invoking Bing.
	 * @throws IOException
	 * 		Problem while invoking Bing.
	 * @throws ParseException
	 * 		Problem while parsing Bing JSON results.
	 */
	private void searchBing(String baseUrl, String baseQuery, LocalDate targetedDate, int dayIdx, Map<String,URL> result) throws ClientProtocolException, IOException, ParseException
	{	int resIdx = 1;
		
		// repeat because of the pagination system
		int lastRes = 0;
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
			request.setHeader("Ocp-Apim-Subscription-Key", API_KEY1);
			HttpResponse response = httpClient.execute(request);
			
			// parse the JSON response
			String answer = WebTools.readAnswer(response);
			JSONParser parser = new JSONParser();
			JSONObject jsonData = (JSONObject)parser.parse(answer);
			
			// web results
			{	JSONObject webRes = (JSONObject)jsonData.get("webPages");
				if(webRes==null)
					logger.log("WARNING: could not find any web results for this query");
				else
				{	JSONArray valueArray = (JSONArray)webRes.get("value");
					logger.log("Found "+valueArray.size()+" web results for this query");
					logger.increaseOffset();
					int i = 1;
					for(Object val: valueArray)
					{	logger.log("Processing web result "+i+"/"+valueArray.size());
						logger.increaseOffset();
							JSONObject value = (JSONObject)val;
							String urlStr = (String)value.get("url");
							logger.log("url: "+urlStr);
							URL resUrl = convertUrl(urlStr);
							logger.log("converted to: "+resUrl);
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

			// news results
			{	JSONObject newsRes = (JSONObject)jsonData.get("news");
				if(newsRes==null)
					logger.log("WARNING: could not find any news results for this query");
				else
				{	JSONArray valueArray = (JSONArray)newsRes.get("value");
					logger.log("Found "+valueArray.size()+" news results for this query");
					logger.increaseOffset();
						int i = 1;
						for(Object val: valueArray)
						{	logger.log("Processing news result "+i+"/"+valueArray.size());
							logger.increaseOffset();
								JSONObject value = (JSONObject)val;
								String urlStr = (String)value.get("url");
								logger.log("url: "+urlStr);
								String dateStr = (String)value.get("datePublished");
								dateStr = dateStr.substring(0,10); // yyyy-mm-dd = 10 chars
								logger.log("date: "+dateStr);
								boolean keepArticle = true;
								if(dateStr!=null && targetedDate!=null)
								{	LocalDate artDate = LocalDate.parse(dateStr, DATE_FORMATTER);
									keepArticle = artDate.equals(targetedDate);
								}
								if(keepArticle)
								{	URL resUrl = convertUrl(urlStr);
									String key;
									if(dayIdx==0)
										key = Integer.toString(resIdx);
									else
										key = dayIdx+"-"+resIdx;
									result.put(key,resUrl);
									resIdx++;
									logger.log("No publication date, or equal to the targeted date >> keeping the article");
								}
								else
									logger.log("The article publication date is not compatible with the targeted date >> article ignored");
							logger.decreaseOffset();
							i++;
						}
					logger.decreaseOffset();
				}
			}
			
			// go to next result page
			lastRes = lastRes + PAGE_SIZE;
		}
		while(result.size()<MAX_RES_NBR);
	}
	
	/**
	 * Receives an URL return by Bing and retrieve
	 * the corresponding actual URL.
	 * 
	 * @param urlStr
	 * 		Bing URL.
	 * @return
	 * 		Actual URL.
	 * 
	 * @throws MalformedURLException
	 * 		Problem with the Bing URL.
	 * @throws IOException
	 * 		Problem while accessing the Bing URL.
	 */
	private URL convertUrl(String urlStr) throws MalformedURLException, IOException
	{	URL result = null; 
		URLConnection con = new URL(urlStr).openConnection();
		con.connect();
		InputStream is = null;
		try
		{	is = con.getInputStream();
		}
		catch(IOException e)
		{	//	
		}
		finally
		{	result = con.getURL();
			if(is!=null)
				is.close();
		}
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// TEST			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/**
//	 * Method used to test/debug this class.
//	 * 
//	 * @param args
//	 * 		None needed.
//	 * @throws Exception
//	 * 		All exceptions are thrown.
//	 */
//	public static void main(String[] args) throws Exception
//	{	
////		BingEngine engine = new BingEngine();
////		
////		String keywords = "François Hollande";
////		String website = null;//"http://lemonde.fr";
////		Date startDate = new GregorianCalendar(2016,3,1).getTime();//null;
////		Date endDate = new GregorianCalendar(2016,3,2).getTime();//null;
////		
////		List<URL> result = engine.search(keywords, website, startDate, endDate);
////		
////		System.out.println(result);
//		
//		
//		// check the URL returned by Bing
//		String urlStr = "http://www.bing.com/cr?IG=C688D700F2FC417AA9B10AA9F7337042&CID=24569A2FBE076C2425F49044BFE06DBD&rd=1&h=VUF8nVTYmDh6zzfje1tbK4pq9WLYMHZZsZtHW0Y5jI0&v=1&r=http%3a%2f%2fwww.closermag.fr%2farticle%2ffrancois-hollande-danse-avec-barack-obama-et-devient-la-risee-de-twitter-photo-604494&p=DevEx,5093.1";
////		HttpClient httpClient = new DefaultHttpClient();   
////		HttpGet request = new HttpGet(urlStr);
////		HttpResponse response = httpClient.execute(request);
////		String answer = WebTools.readAnswer(response);
////		PrintWriter pw = FileTools.openTextFileWrite(FileNames.FO_OUTPUT+File.separator+"test.html");
////		pw.print(answer);
////		pw.close();
//		
//		System.out.println();
//	}
}
