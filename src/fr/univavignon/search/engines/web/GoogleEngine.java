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

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import fr.univavignon.common.tools.keys.KeyHandler;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class uses Google to search the Web. More
 * precisely, it uses the Google Custom Search API.
 * <br/>
 * This code is inspired by the 
 * <a href="http://weblog4j.com/2014/06/03/having-fun-with-google-custom-search-api-and-java/">weblog4j.com post</a> 
 * of Niraj Singh.
 * <br/>
 * Dates are explicitly taken into account in the
 * Google API (unlike some other search engines).
 * <br/>
 * Also unlike other language, the targeted language
 * is not specified at runtime, but when configuring
 * the online Google Custom Search service. 
 * 
 * @author Vincent Labatut
 */
public class GoogleEngine extends AbstractWebEngine
{
	/**
	 * Quick test.
	 * 
	 * @param args
	 * 		Not used.
	 * 
	 * @throws Exception
	 * 		Whatever problem. 
	 */
	public static void main(String[] args) throws Exception
	{	logger.setName("Test-GoogleEngine");
		logger.log("Start testing Google Custom Search");
		logger.increaseOffset();
		
		// parameters
		String keywords = "Cécile Helle";
		String website = null;
		String sortCriterion = "date:r:20150101:20150131";
		GoogleEngine gs = new GoogleEngine(website,null,null);
		
		// launch search
		List<Result> result = gs.searchGoogle(keywords, website, sortCriterion);
		
		List<String> msgs = new ArrayList<String>();
		logger.log("Displaying results: "+result.size()+"/"+GoogleEngine.MAX_RES_NBR);
		logger.increaseOffset();
			int i = 0;
			for(Result res: result)
			{	i++;
				logger.log(Arrays.asList(
					"Result "+i+"/"+result.size(),
					res.getHtmlTitle(),
					res.getFormattedUrl(),
					"----------------------------------------")
				);
				msgs.add(res.getLink());
			}
		logger.decreaseOffset();
		
		logger.log(msgs);
		
		logger.decreaseOffset();
		logger.log("Test terminated");
	}

	/**
	 * Initializes the object used to search
	 * the Web with the Google Custom Search (GCS) API.
	 * 
	 * @param website
	 * 		Target site, or {@code null} to search the whole Web.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 */
	public GoogleEngine(String website, Date startDate, Date endDate)
	{	super(website,startDate,endDate);
		
		// Set up the HTTP transport and JSON factory
		HttpTransport httpTransport = new NetHttpTransport();
		//JsonFactory jsonFactory = new GsonFactory();
		JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
		
		// builds the builder and set the application name
		//HttpRequestInitializer initializer = (HttpRequestInitializer)new CommonGoogleClientRequestInitializer(API_KEY);
		builder = new Customsearch.Builder(httpTransport, jsonFactory, null);
		builder.setApplicationName(APP_NAME);
	}

	/////////////////////////////////////////////////////////////////
	// SERVICE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Object used to format dates in the query */
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	/** Name of the GCS application */
	private static final String APP_NAME = "TranspoloSearch";
	/** Name of the API key */
	private static final String API_KEY_NAME = "GoogleProject";
	/** Name of the application key */
	private static final String APP_KEY_NAME = "GoogleEngine";
    /** GCS API key */ 
	private static final String API_KEY = KeyHandler.KEYS.get(API_KEY_NAME);
    /** Application id */
	private static final String APP_KEY = KeyHandler.KEYS.get(APP_KEY_NAME);
	/** Number of results returned for one request */
	private static final long PAGE_SIZE = 10; // max seems to be only 10!
   
	/////////////////////////////////////////////////////////////////
	// DATA			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Textual name of this engine */
	public static final String ENGINE_NAME = "Google";

	@Override
	public String getName()
	{	return ENGINE_NAME;
	}
	
	/////////////////////////////////////////////////////////////////
	// PARAMETERS	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Focus on pages hosted in a certain country */
	public static final String PAGE_CNTRY = "countryFR";
	/** Focus on pages in a certain language */
	public static final String PAGE_LANG = "lang_fr";
//	/** Whether the result should be sorted by date, or not (in this case: by relevance). If {@link #sortByDate} is not {@code null}, only the specified time range is treated. */
//	public boolean sortByDate = false;
//	/** Date range the search should focus on. It should take the form YYYYMMDD:YYYYMMDD, or {@code null} for no limit. If {@link #sortByDate} is set to {@code false}, this range is ignored. */
//	public String dateRange = null;
	/** Maximal number of results (can be less if google does not provide) */
	public static final int MAX_RES_NBR = 100;

	/////////////////////////////////////////////////////////////////
	// BUILDER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Object used to build GoogleEngine instances */
	private Customsearch.Builder builder;
	
	/////////////////////////////////////////////////////////////////
	// SEARCH		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Map<String,URL> search(String keywords)  throws IOException
	{	logger.log("Applying Google Custom Search");
		logger.increaseOffset();
		
		// init GCS parameters
		String sortCriterion;
		if(startDate==null && endDate==null)
		{	sortCriterion = null;
		}
		else if(startDate!=null && endDate!=null)
		{	String dateRange = DATE_FORMAT.format(startDate)+":"+DATE_FORMAT.format(endDate);
			sortCriterion = "date:r:" + dateRange;
		}
		else
		{	logger.log("WARNING: one date is null, so we ignore both dates in the search");
			sortCriterion = null;
		}
		
		// display GCS parameters
		logger.log("Parameters:");
		logger.increaseOffset();
			logger.log("keywords="+keywords);
			logger.log("website="+website);
			logger.log("pageCountry="+PAGE_CNTRY);
			logger.log("pageLanguage="+PAGE_LANG);
			logger.log("PAGE_SIZE="+PAGE_SIZE);
			logger.log("resultNumber="+MAX_RES_NBR);
			logger.log("sortCriterion="+sortCriterion);
		logger.decreaseOffset();

		// perform search
		List<Result> resList = searchGoogle(keywords,website,sortCriterion);
	
		// convert result list
		logger.log("Results obtained:");
		logger.increaseOffset();
		Map<String,URL> result = new HashMap<String,URL>();
		int resIdx = 1;
		for(Result res: resList)
		{	String title = res.getHtmlTitle();
			String urlStr = res.getLink();
			logger.log("Processing URL \""+title+"\" - "+urlStr);
			logger.increaseOffset();
				URL url = new URL(urlStr);
				if(result.containsValue(url))
					logger.log("URL already in the result list (was returned several times by the search engine)");
				else
				{	String rankStr = Integer.toString(resIdx);
					logger.log("Adding with rank "+rankStr);
					result.put(rankStr,url);
					resIdx++;
				}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		
		logger.log("Search terminated: "+result.size()+"/"+MAX_RES_NBR+" results retrieved");
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Performs a search using Google Custom Search.
	 * The search is performed only on the specified site.
	 * <br/>
	 * See the public fields of this class for a
	 * description of the modifiable search parameters.
	 * 
	 * @param keywords
	 * 		Keywords to search.
	 * @param website
	 * 		Target site, or {@code null} to search the whole Web.
	 * @param sortCriterion
	 * 		Criterion used for sorting (and possibly a range),
	 * 		or {@code null} to use the default (relevance).
	 * @return
	 * 		List of results presented using Google's class.
	 * 
	 * @throws IOException
	 * 		Problem while searching Google.
	 */
	private List<Result> searchGoogle(String keywords, String website, String sortCriterion)  throws IOException
	{	// init the other variables
		List<Result> result = new ArrayList<Result>();
		long start = 1;
		
		// repeat because of the pagination system
		logger.log("Retrieving the result pages:");
		logger.increaseOffset();
			try
			{	List<Result> response = null;
				do
				{	logger.log("Starting at position "+start+"/"+MAX_RES_NBR);
					
					// create the GCS object
					Customsearch customsearch = builder.build();//new Customsearch(httpTransport, jsonFactory,null);
					Customsearch.Cse.List list = customsearch.cse().list(keywords);				
					list.setKey(API_KEY);
					list.setCx(APP_KEY);
					list.setCr(PAGE_CNTRY);
					list.setLr(PAGE_LANG);
					list.setNum(PAGE_SIZE);
					if(sortCriterion!=null)
						list.setSort(sortCriterion);
					if(website!=null)
						list.setSiteSearch(website);
					list.setStart(start);
		            
					// send the request
					logger.log("Send request");
					Search search = list.execute();
					
//TODO handle search in order to catch possible problems, such as 403					
					
					// add the results to the list
					response = search.getItems();
					if(response==null)
						logger.log("No more result could be retrieved");
					else
					{	logger.log("Retrieved "+response.size()+"/"+PAGE_SIZE+" items (total: "+result.size()+"/"+MAX_RES_NBR+")");
						result.addAll(response);
					
						// update parameter
						start = start + PAGE_SIZE;
					}
				}
				while(result.size()<MAX_RES_NBR && response!=null);
			}
			catch(IOException e)
			{	//e.printStackTrace();
				if(start<MAX_RES_NBR)
					logger.log("Could not reach the specified number of results ("+result.size()+"/"+MAX_RES_NBR+")");
			}
		logger.decreaseOffset();
		
        return result;
	}
}
