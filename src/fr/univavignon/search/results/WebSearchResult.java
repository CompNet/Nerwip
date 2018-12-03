package fr.univavignon.search.results;

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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.retrieval.ArticleRetriever;
import fr.univavignon.retrieval.reader.ReaderException;
import fr.univavignon.search.events.Event;
import fr.univavignon.search.events.ReferenceEvent;
import fr.univavignon.tools.files.FileNames;

/**
 * Represents one result of a Web search engine (a URL) and some info
 * regarding how it was subsequently processed.
 * 
 * @author Vincent Labatut
 */
public class WebSearchResult extends AbstractSearchResult
{
	/**
	 * Initializes the Web search result.
	 * 
	 * @param url
	 * 		Address associated to the search result.
	 */
	public WebSearchResult(String url)
	{	super();
		this.url = url;
	}
	
	/////////////////////////////////////////////////////////////////
	// KEY			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getKey()
	{	return url;
	}
	
	/////////////////////////////////////////////////////////////////
	// URL			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** URL associated to the result */
	public String url;
	
	/**
	 * Adds the article URL to the specified map.
	 * 
	 * @param result
	 * 		Map to fill with the required field.
	 */
	private void exportUrl(Map<String,String> result)
	{	result.put(AbstractSearchResults.COL_URL, url);
		result.put(AbstractSearchResults.COL_URL_ID, url);
	}
	
	/**
	 * Decides whether or not this result should be filtered depending on
	 * its url, and updates its status if needed.
	 * 
	 * @return
	 * 		{@code true} iff the result was filtered, i.e. it cannot be
	 * 		processed further.
	 */
	protected boolean filterUrl()
	{	boolean result = false;
		
		// we don't process PDF files
		if(url.endsWith(FileNames.EX_PDF))
		{	logger.log("The following URL points towards a PDF, we cannot currently use it: "+url);
			status = STATUS_UNSUPPORTED_FORMAT;
			result = true;
		}
		
		else
			logger.log("We keep the URL "+url);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENGINES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Ranks of this result according to the search engines who returned it */
	public Map<String,String> ranks = new HashMap<String,String>();
	
	/**
	 * Changes the rank the specified engine gave to this result.
	 * The new rank is kept only if it is better than the one
	 * possibly already registered.
	 * 
	 * @param engineName
	 * 		Name of the concerned search engine.
	 * @param rank
	 * 		Rank given by search engine.
	 */
	public void addEngine(String engineName, String rank)
	{	String oldRank = ranks.get(engineName);
	
		// check if the result is already known for the specified engine
		if(oldRank!=null)
		{	// in which case we must compare the new and old ranks
			int prevRank;
			int newRank;
			// case where the rank is composite (day-rank)
			if(rank.contains("-"))
			{	String[] oldTmp = oldRank.split("-");
				String[] newTmp = rank.split("-");
				prevRank = Integer.parseInt(oldTmp[1]);
				newRank = Integer.parseInt(newTmp[1]);
			}
			// case where the rank is simple
			else
			{	prevRank = Integer.parseInt(oldRank);
				newRank = Integer.parseInt(rank);
			}
			if(newRank<prevRank)
				ranks.put(engineName, rank);
		}
		
		// if the result is new for the engine, we just put it in the map
		else
			ranks.put(engineName,rank);
	}
	
	/**
	 * Adds the article ranks to the specified map.
	 * 
	 * @param result
	 * 		Map to fill with the required field.
	 */
	private void exportRanks(Map<String,String> result)
	{	String str = "";
		Iterator<Entry<String,String>> it = ranks.entrySet().iterator();
		while(it.hasNext())
		{	Entry<String,String> entry = it.next();
			String engineName = entry.getKey();
			String rk = entry.getValue();
			result.put(AbstractSearchResults.COL_RANK+engineName,rk);
			str = str + engineName + "[" + rk + "]";
			if(it.hasNext())
				str = str + ", ";
		}
		result.put(AbstractSearchResults.COL_SOURCE,str);
	}
	
	/////////////////////////////////////////////////////////////////
	// ARTICLE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Retrieve the article located at the URL associated to this result.
	 * 
	 * @param articleRetriever
	 * 		Object to use to retrieve the article.
	 * @param nbr
	 * 		Number of this result in the collection.
	 * @return
	 * 		{@code true} iff the article could be retrieved.
	 * 
	 * @throws ParseException
	 * 		Problem while retrieving the article.
	 * @throws SAXException
	 * 		Problem while retrieving the article.
	 * @throws IOException
	 * 		Problem while retrieving the article.
	 */
	protected boolean retrieveArticle(ArticleRetriever articleRetriever, int nbr) throws ParseException, SAXException, IOException
	{	boolean result = true;
		
		logger.log("Retrieving article #"+nbr+" at URL "+url);
		try
		{	article = articleRetriever.process(url);
		}
		catch(ReaderException e)
		{	// the targeted page is a list of articles, not a single article
			if(e.isArticleList())
			{	logger.log("WARNING: The following URL is not a single article, but rather an article list "+url.toString()+" >> removing it from the result list.");
				status = STATUS_LIST;
			}
			// we just couldn't access the targeted page
			else
			{	logger.log("WARNING: Could not retrieve the article at URL "+url.toString()+" >> removing it from the result list.");
				status = STATUS_UNAVAILABLE;
			}
			result = false;
		}
		
		return result;
	}
	
	@Override
	protected boolean filterByKeyword(String compulsoryExpression, int nbr)
	{	boolean result = true;
		
		logger.log("Processing article "+article.getTitle()+" ("+nbr+")");
		logger.increaseOffset();
		{	String text = article.getRawText().toLowerCase(Locale.ENGLISH);
			String expr = compulsoryExpression.toLowerCase(Locale.ENGLISH);
			Pattern pattern = Pattern.compile("\\b"+expr+"\\b");
	        Matcher matcher = pattern.matcher(text);
	        if(!matcher.find())
			{	logger.log("Discarding article "+article.getTitle()+" ("+article.getUrl()+")");
				status = STATUS_MISSING_KEYWORD;
				result = false;
			}
		}
		logger.decreaseOffset();
			
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CSV			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Map<String,String> exportResult(Map<String,List<ReferenceEvent>> referenceClusters, Date startDate, Date endDate)
	{	Map<String,String> result = new HashMap<String,String>();
		
		// title 
		exportTitle(result);
		// url
		exportUrl(result);
		// status
		exportStatus(result);
		// length
		exportLength(result);
		// publication date
		exportPublicationDate(result);
		// author(s)
		exportAuthors(result);
		// article cluster
		exportCluster(result, referenceClusters, startDate, endDate);
		// search engine ranks
		exportRanks(result);
		
		// mentions
		exportMentions(result, EntityType.DATE);
		exportMentions(result, EntityType.FUNCTION);
		exportMentions(result, EntityType.LOCATION);
		exportMentions(result, EntityType.MEETING);
		exportMentions(result, EntityType.ORGANIZATION);
		exportMentions(result, EntityType.PERSON);
		exportMentions(result, EntityType.PRODUCTION);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// EVENTS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected int extractEvents(boolean bySentence, int nbr)
	{	int result = extractEvents(bySentence,nbr,true);
		return result;
	}
	
	/**
	 * Returns a map of strings used above to record the results of 
	 * the web search as a CSV file.
	 * 
	 * @return
	 * 		Map representing the events associated to this Web
	 * 		search result (can be empty). 
	 */
	@Override
	protected List<Map<String,String>> exportEvents(Map<String,List<ReferenceEvent>> referenceClusters, Date startDate, Date endDate)
	{	List<Map<String,String>> result = new ArrayList<Map<String,String>>();
		
		int rank = 0;
		for(Event event: events)
		{	Map<String,String> map = new HashMap<String,String>();
			result.add(map);
			rank++;

			// title
			exportTitle(map);
			// url
			exportUrl(map);
			// length
			exportLength(map);
			// status
			exportStatus(map);
			// date
			exportPublicationDate(map);
			// author(s)
			exportAuthors(map);
			// article cluster
			exportCluster(map, referenceClusters, startDate, endDate);
			// search engine ranks
			exportRanks(map);
			// event and its stuff
			exportEvent(event, rank, map);
		}
		
		return result;
	}
}
