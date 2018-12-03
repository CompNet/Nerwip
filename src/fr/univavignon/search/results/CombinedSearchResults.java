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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.search.tools.files.SearchFileNames;
import fr.univavignon.tools.files.FileTools;

/**
 * Collection of search results returned by a collection of Web
 * and social media search engines, with additional info resulting from their
 * subsequent processing.
 * 
 * @author Vincent Labatut
 */
public class CombinedSearchResults extends AbstractSearchResults<AbstractSearchResult>
{	
	/**
	 * Combines existing web and social search results in a single object.
	 * 
	 * @param webRes
	 * 		Web search results.
	 * @param socRes
	 * 		Social search results.
	 */
	public CombinedSearchResults(WebSearchResults webRes, SocialSearchResults socRes)
	{	super();
		
		// add all results
		if(webRes!=null)
			results.putAll(webRes.results);
		if(socRes!=null)
			results.putAll(socRes.results);
		
		// and search engines
		if(webRes!=null)
			engineNames.addAll(webRes.engineNames);
		if(socRes!=null)
			engineNames.addAll(socRes.engineNames);
		
		// and reference events
		if(webRes!=null)
			referenceEvents.putAll(webRes.referenceEvents);
		else
			// (same in both cases)
			referenceEvents.putAll(socRes.referenceEvents);
		
		// and reference clusters
		if(webRes!=null)
			referenceClusters.putAll(webRes.referenceClusters);
		if(socRes!=null)
			referenceClusters.putAll(socRes.referenceClusters);
	}
	
	/////////////////////////////////////////////////////////////////
	// CSV			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void exportResults(String fileName, Date startDate, Date endDate) throws UnsupportedEncodingException, FileNotFoundException
	{	logger.log("Recording all the combined search results in file "+fileName);
		logger.increaseOffset();
		
		// setup filename
		String filePath = SearchFileNames.FO_SEARCH + File.separator + fileName;
		logger.log("Recording in CSV file \""+filePath+"\"");
		
		// setup colon names
		List<String> cols = Arrays.asList(
				COL_NOTES, COL_TITLE_CONTENT, COL_URL_ID, COL_LENGTH, COL_PUB_DATE, 
				COL_AUTHORS, COL_STATUS, COL_ARTICLE_CLUSTER, COL_REFERENCE_EVENTS, COL_SOURCE, 
				COL_ENT_DATES, COL_ENT_LOCATIONS, COL_ENT_PERSONS, COL_ENT_ORGANIZATIONS, 
				COL_ENT_FUNCTIONS, COL_ENT_PRODUCTIONS, COL_ENT_MEETINGS
		);
		
		// open file and write header
		PrintWriter pw = FileTools.openTextFileWrite(filePath,"UTF-8");
		{	Iterator<String> it = cols.iterator();
			while(it.hasNext())
			{	String col = it.next();
				pw.print("\""+col+"\"");
				if(it.hasNext())
					pw.print(",");
			}
		}
		pw.println();
		
		// write data and close file
		for(AbstractSearchResult result: results.values())
		{	Map<String,String> map = result.exportResult(referenceClusters, startDate, endDate);
			Iterator<String> it = cols.iterator();
			while(it.hasNext())
			{	String col = it.next();
				String val = map.get(col);
				if(val!=null)
					pw.print("\""+val+"\"");
				if(it.hasNext())
					pw.print(",");
			}
			pw.println();
		}
		pw.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// EVENTS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void exportEvents(boolean bySentence, String filePrefix, Date startDate, Date endDate) throws UnsupportedEncodingException, FileNotFoundException 
	{	String fileName = filePrefix;
		if(bySentence)
			fileName = fileName + SearchFileNames.FI_EVENT_LIST_BYSENTENCE;
		else
			fileName = fileName + SearchFileNames.FI_EVENT_LIST_BYARTICLE;
		String filePath = SearchFileNames.FO_SEARCH + File.separator + fileName;
		logger.log("Recording the event list as a CVS file: "+filePath);
		logger.decreaseOffset();
			
			// setup colon names
			List<String> startCols = Arrays.asList(
					COL_NOTES, COL_EVENT_CLUSTER, COL_TITLE_CONTENT, COL_URL_ID, COL_LENGTH, 
					COL_PUB_DATE, COL_AUTHORS, COL_STATUS, COL_ARTICLE_CLUSTER, COL_REFERENCE_EVENTS, COL_SOURCE
			);
			List<String> endCols = Arrays.asList(
					COL_ENT_DATES, COL_ENT_LOCATIONS, COL_ENT_PERSONS, COL_ENT_ORGANIZATIONS, 
					COL_ENT_FUNCTIONS, COL_ENT_PRODUCTIONS, COL_ENT_MEETINGS
			);
			List<String> cols = new ArrayList<String>();
			cols.addAll(startCols);
			if(bySentence)
			{	cols.add(COL_EVENT_RANK);
				cols.add(COL_EVENT_SENTENCE);
			}
			cols.addAll(endCols);
			
			// open file and write header
			PrintWriter pw = FileTools.openTextFileWrite(filePath, "UTF-8");
			Iterator<String> it = cols.iterator();
			while(it.hasNext())
			{	String col = it.next();
				pw.print("\""+col+"\"");
				if(it.hasNext())
					pw.print(",");
			}
			pw.println();
			
			// write data
			int total = 0;
			logger.log("Treat each article separately");
			for(AbstractSearchResult result: results.values())
			{	List<Map<String,String>> lines = result.exportEvents(referenceClusters, startDate, endDate);
				for(Map<String,String> line: lines)
				{	it = cols.iterator();
					while(it.hasNext())
					{	String col = it.next();
						String val = line.get(col);
						if(val!=null)
							pw.print("\""+val+"\"");
						if(it.hasNext())
							pw.print(",");
					}
					pw.println();
					total++;
				}
			}
			
			pw.close();
		logger.decreaseOffset();
		logger.log("Wrote "+total+" events");
	}

	@Override
	public void exportEventClusters(boolean bySentence, String filePrefix, ArticleLanguage language) throws UnsupportedEncodingException, FileNotFoundException 
	{	String fileName = filePrefix;
		if(bySentence)
			fileName = fileName + SearchFileNames.FI_EVENT_CLUSTERS_BYSENTENCE;
		else
			fileName = fileName + SearchFileNames.FI_EVENT_CLUSTERS_BYARTICLE;
		String filePath = SearchFileNames.FO_SEARCH + File.separator + fileName;
		
		exportEventClusters(filePath, language);
	}
	
	/////////////////////////////////////////////////////////////////
	// PERFORMANCE	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void recordPerformance() throws UnsupportedEncodingException, FileNotFoundException
	{	String folder = SearchFileNames.FO_SEARCH;
		recordPerformance(folder);
	}

	@Override
	public void exportConfusionMatrix(String fileName) throws UnsupportedEncodingException, FileNotFoundException
	{	String path = SearchFileNames.FO_SEARCH + File.separator + fileName;
		PrintWriter pw = FileTools.openTextFileWrite(path, "UTF-8");
		pw.write(lastConfMatStr);
		pw.close();
	}
}
