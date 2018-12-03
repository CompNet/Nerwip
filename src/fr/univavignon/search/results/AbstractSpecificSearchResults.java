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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.search.events.ReferenceEvent;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.tools.files.FileTools;

/**
 * Collection of search results returned by a collection of
 * search engines, with additional info resulting from their
 * subsequent processing.
 * 
 * @param <T>
 * 		Type of results handled by this class. 
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractSpecificSearchResults<T extends AbstractSearchResult> extends AbstractSearchResults<T>
{	
	/**
	 * Initializes the search result.
	 * 
	 * @param referenceEvents
	 * 		Previously loaded reference events, or an empty
	 * 		list if no reference was defined. 
	 * 
	 * @throws UnsupportedEncodingException 
	 * 		Problem while loading the reference. 
	 */
	public AbstractSpecificSearchResults(Map<Integer,ReferenceEvent> referenceEvents) throws UnsupportedEncodingException
	{	super();
		
		this.referenceEvents = referenceEvents;
	}
	
	/////////////////////////////////////////////////////////////////
	// FILTERING	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Discards results whose language does not match the targeted one.
	 *
	 * @param language
	 * 		targeted language of the articles.
	 */
	private void filterByLanguage(ArticleLanguage language)
	{	logger.log("Removing articles not matching the language constraint: "+language);
		logger.increaseOffset();
			int count = 0;
			int total = 0;
			for(T result: results.values())
			{	if(result.status==null)
				{	total++;
					if(!result.filterByLanguage(language,total))
						count++;
				}
			}
		logger.decreaseOffset();
		logger.log("Language-based filtering complete: "+count+"/"+total);
	}
	
	/**
	 * Discards results describing only events not contained 
	 * in the specified date range.
	 *  
	 * @param startDate
	 * 		Start of the time period.
	 * @param endDate
	 * 		End of the time period.
	 */
	private void filterByEntityDate(Date startDate, Date endDate)
	{	logger.log("Removing articles not fitting the entity date constraints: "+startDate+"->"+endDate);
		logger.increaseOffset();
			fr.univavignon.common.tools.time.Date start = new fr.univavignon.common.tools.time.Date(startDate);
			fr.univavignon.common.tools.time.Date end = new fr.univavignon.common.tools.time.Date(endDate);
			int count = 0;
			int total = 0;
			for(T result: results.values())
			{	if(result.status==null)
				{	total++;
					if(!result.filterByEntityDate(start,end,total))
						count++;
				}
			}
		logger.decreaseOffset();
		logger.log("Date-based filtering complete: "+count+"/"+total);
	}
	
	/**
	 * Discards results published out of the specified date range. 
	 *  
	 * @param startDate
	 * 		Start of the time period.
	 * @param endDate
	 * 		End of the time period.
	 */
	private void filterByPublicationDate(Date startDate, Date endDate)
	{	logger.log("Removing articles not fitting the publication date constraints: "+startDate+"->"+endDate);
		logger.increaseOffset();
			int count = 0;
			int total = 0;
			for(T result: results.values())
			{	if(result.status==null)
				{	total++;
					if(!result.filterByPublicationDate(startDate,endDate,total))
						count++;
				}
			}
		logger.decreaseOffset();
		logger.log("Publication date-based filtering complete: "+count+"/"+total);
	}
	
	/**
	 * Discards results corresponding only to articles not containing 
	 * the compulsory expression.
	 *  
	 * @param compulsoryExpression
	 * 		String expression which must be present in the article.
	 */
	private void filterByKeyword(String compulsoryExpression)
	{	logger.log("Discarding articles not containing the compulsory expression \""+compulsoryExpression+"\"");
		logger.increaseOffset();
			int count = 0;
			int total = 0;
			for(T result: results.values())
			{	
if(result instanceof WebSearchResult && ((WebSearchResult)result).url.equalsIgnoreCase
		("http://www.lamarseillaise.fr/vaucluse/developpement-durable/58144-avignon-ca-bouge-autour-du-technopole-de-l-agroparc"))				
	System.out.print("");
				
				if(result.status==null)
				{	total++;
					if(!result.filterByKeyword(compulsoryExpression,total))
						count++;
				}
			}
		logger.decreaseOffset();
		logger.log("Keyword-based filtering complete: "+count+"/"+total);
	}

	/**
	 * Discards results describing only events not contained 
	 * in the specified date range, or not containing the 
	 * compulsory expression.
	 *  
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * @param filterByPubDate
	 * 		Whether or not to filter articles depending on their publication date.
	 * @param compulsoryExpression
	 * 		String expression which must be present in the article,
	 * 		or {@code null} if there is no such constraint.
	 * @param language
	 * 		targeted language of the articles.
	 */
	public void filterByContent(Date startDate, Date endDate, boolean filterByPubDate, String compulsoryExpression, ArticleLanguage language)
	{	logger.log("Starting filtering the articles by content");
		logger.increaseOffset();
		
		// log stuff
		logger.log("Parameters:");
		logger.increaseOffset();
			logger.log("startDate="+startDate);
			logger.log("endDate="+endDate);
			logger.log("filterByPubDate="+filterByPubDate);
			logger.log("compulsoryExpression="+compulsoryExpression);
			logger.log("language="+language);
		logger.decreaseOffset();
		
		// filter depending on the language
		if(language!=null)
			filterByLanguage(language);
		else
			logger.log("No targeted language to process");
		
		// possibly filter the resulting texts depending on the compulsory expression
		if(compulsoryExpression!=null)
			filterByKeyword(compulsoryExpression);
		else
			logger.log("No compulsory expression to process");
		
		// possibly filter the remaining texts depending on the publication date
		if(filterByPubDate)
			filterByPublicationDate(startDate,endDate);
		else
			logger.log("No publication date filtering");
		
		logger.decreaseOffset();
		logger.log("Content-based filtering complete");
	}
	
	/**
	 * Discards results describing only events not contained
	 * in the specified time period.
	 * 
	 * @param startDate
	 * 		Start of the time period.
	 * @param endDate
	 * 		End of the time period.
	 * @param filterByEntDate
	 * 		Whether or not filtering the articles depending on the fact they contain
	 * 		a date belonging to the targeted period.
	 */
	public void filterByEntity(Date startDate, Date endDate, boolean filterByEntDate)
	{	logger.log("Starting filtering the articles by entity");
		logger.increaseOffset();
		
		// log stuff
		logger.log("Parameters:");
		logger.increaseOffset();
			logger.log("startDate="+startDate);
			logger.log("endDate="+endDate);
			logger.log("filterByEntDate="+filterByEntDate);
		logger.decreaseOffset();
		
		// possibly filter the texts depending on the dates they contain
		if(filterByEntDate)
		{	if(startDate==null || endDate==null)
				logger.log("WARNING: one date is null, so both of them are ignored");
			else
				filterByEntityDate(startDate, endDate);
		}
		
		logger.decreaseOffset();
		logger.log("Entity-based filtering complete");
	}
	
	/////////////////////////////////////////////////////////////////
	// MENTIONS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Detects the entity mentions present in each specified article.
	 * 
	 * @param recognizer
	 * 		The recognizer used to detect the mentions.
	 * @throws ProcessorException
	 * 		Problem while applying the NER tool.
	 */
	public void detectMentions(InterfaceRecognizer recognizer) throws ProcessorException
	{	logger.log("Detecting entity mentions in all the articles");
		logger.increaseOffset();
		
			int count = 0;
			int total = 0;
boolean doit = false;
			for(T result: results.values())
			{	
//if(result instanceof WebSearchResult && ((WebSearchResult)result).url.equalsIgnoreCase
//		("http://www.republicain-lorrain.fr/actualite/2017/03/11/routes-treize-stars-s-engagent"))				
//	System.out.print("");
	doit = true;
				
				if(result.status==null && doit)
				{	total++;
					if(result.detectMentions(recognizer,total)>0)
						count++;
				}
			}
		
		logger.decreaseOffset();
		logger.log("Mention detection complete: ("+count+" for "+total+" articles)");
	}

	/**
	 * Displays the entity mentions associated to each remaining article.
	 */
	public void displayRemainingMentions()
	{	logger.log("Displaying remaining articles and entity mentions");
		logger.increaseOffset();
		
		int total = 0;
		for(T result: results.values())
		{	if(result.status==null)
			{	total++;
				result.displayRemainingMentions(total);
			}
		}
		
		logger.decreaseOffset();
		logger.log("Display complete");
	}
	
	/////////////////////////////////////////////////////////////////
	// PERFORMANCE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tries to load the manually annotated reference clusters, 
	 * if the file exists. It is then used later to assess the 
	 * system performance.
	 * <br/>
	 * We expect the following format: URL on the first column,
	 * then a tabulation, then the ids of the event(s). The rest
	 * of the columns (if any) are just ignored. The ids are integers
	 * separated by semi-columns (:).
	 * 
	 * @param filePath 
	 * 		Path of the reference clusters file.
	 * @throws UnsupportedEncodingException
	 * 		Problem when loading the reference clusters file. 
	 */
	protected void loadReferenceClusters(String filePath) throws UnsupportedEncodingException
	{	logger.increaseOffset();
			
			try
			{	logger.log("Find a reference clusters file ("+filePath+"): loading it");
				Scanner scanner = FileTools.openTextFileRead(filePath, "UTF-8");
				while(scanner.hasNextLine())
				{	String line = scanner.nextLine();
					String[] tmp = line.split("\t");
					String key = tmp[0].trim();
					if(tmp.length>1)
					{	String clustName = tmp[1].trim();
						List<ReferenceEvent> clust;
						if(clustName.isEmpty())
							clust = null;	// null means the URL is irrelevant
						else
						{	clust = new ArrayList<ReferenceEvent>();
							String ctmp[] = clustName.split(":");
							for(String cn: ctmp)
							{	int id = Integer.parseInt(cn);
								ReferenceEvent event = referenceEvents.get(id);
								clust.add(event);
							}
						}
						referenceClusters.put(key, clust);
					}
				}
			}
			catch (FileNotFoundException e) 
			{	logger.log("Found no reference clusters file at "+filePath);
			}
			
		logger.decreaseOffset();
	}
}
