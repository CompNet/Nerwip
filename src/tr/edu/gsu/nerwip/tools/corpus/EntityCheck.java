package tr.edu.gsu.nerwip.tools.corpus;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleCategory;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.evaluation.ArticleList;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalConverter;
import tr.edu.gsu.nerwip.retrieval.ArticleRetriever;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.retrieval.reader.wikipedia.WikipediaReader;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.StringTools;

/**
 * This class contains various methods used to verify reference entities
 * are correct: no overlap, consistant position, no empty entities, etc.
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class EntityCheck
{	
	/**
	 * Method used to do some punctual processing.
	 * 
	 * @param args
	 * 		None needed.
	 * 
	 * @throws Exception 
	 * 		Whatever.
	 */
	public static void main(String[] args) throws Exception
	{	logger.setName("Entity-check");
		List<File> articles = ArticleLists.getArticleList();
		
//		checkEntityConsistance(articles);
//		checkEntityOverlap(articles);
		checkEntityEmptiness(articles);
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CONSISTANCE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Parse the xml reference associated to the specified
	 * articles and for each entity, checks if the string
	 * of the entity string is equal to that retrieved from the text
	 * using the start and end positions of the entity. If it's not
	 * the case, a message is displayed to allow manual correction.
	 * 
	 * @param articles
	 * 		List of folders corresponding to articles.
	 * 
	 * @throws IOException 
	 * 		Problem while accessing the articles.
	 * @throws SAXException 
	 * 		Problem while accessing the articles.
	 * @throws ParseException 
	 * 		Problem while accessing the articles.
	 */
	private static void checkEntityConsistance(List<File> articles) throws ParseException, SAXException, IOException
	{	logger.log("Checking all entities");
		logger.increaseOffset();
		
		int problems = 0;
		
		logger.log("Processing each article separately");
		logger.increaseOffset();
		for(File file: articles)
		{	String name = file.getName();
			logger.log("Processing article '" + name + "'");
			logger.increaseOffset();
			Article article = Article.read(name);
			String text = article.getRawText();
			
			Entities entities = article.getReferenceEntities();
			List<AbstractEntity<?>> list = entities.getEntities();
			
			for(AbstractEntity<?> entity: list)
			{	int start = entity.getStartPos();
				int end = entity.getEndPos();
				String textStr = text.substring(start, end);
				String entityStr = entity.getStringValue();
				if(!entityStr.equals(textStr))
				{	logger.log("Problem with entity "+entity+" entity=\""+entityStr+"\" vs. text=\""+textStr+"\"");
					problems++;
				}
			}
			
			logger.decreaseOffset();
			logger.log("Article '" + name + "' completed");
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Entity check over: "+problems+" detected");
	}

	/////////////////////////////////////////////////////////////////
	// OVERLAP			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Parses the xml reference associated to the specified
	 * articles and for each entity, checks if there exist
	 * overlapping entities. If some are detected, a warning 
	 * message is displayed.
	 * 
	 * @param articles
	 * 		List of folders corresponding to articles.
	 * 
	 * @throws IOException 
	 * 		Problem while accessing the articles.
	 * @throws SAXException 
	 * 		Problem while accessing the articles.
	 * @throws ParseException 
	 * 		Problem while accessing the articles.
	 */
	private static void checkEntityOverlap(List<File> articles) throws ParseException, SAXException, IOException
	{	logger.log("Checking all entities");
		logger.increaseOffset();
		
		int problems = 0;
		
		logger.log("Processing each article separately");
		logger.increaseOffset();
		for(File file: articles)
		{	String name = file.getName();
			logger.log("Processing article '" + name + "'");
			logger.increaseOffset();
			Article article = Article.read(name);
			String text = article.getRawText();
			
			Entities entities = article.getReferenceEntities();
			entities.sortByPosition();
			List<AbstractEntity<?>> list = entities.getEntities();
			
			Iterator<AbstractEntity<?>> it = list.iterator();
			AbstractEntity<?> current = null;
			AbstractEntity<?> prev = null;
			while(it.hasNext())
			{	prev = current;
				current = it.next();
				if(prev!=null)
				{	if(prev.overlapsWith(current))
					{	logger.log("Overlapping entities detected:");
						logger.increaseOffset();
						logger.log("Previous entity: "+prev); 
						logger.log("Current entity: "+current); 
						logger.decreaseOffset();
						problems++;
					}
				}
			}
			
			logger.decreaseOffset();
			logger.log("Article '" + name + "' completed");
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Entity check over: "+problems+" detected");
	}

	/////////////////////////////////////////////////////////////////
	// EMPTINESS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Parses the xml reference associated to the specified
	 * articles and for each entity, checks if there exist
	 * overlapping entities. If some are detected, a warning 
	 * message is displayed.
	 * 
	 * @param articles
	 * 		List of folders corresponding to articles.
	 * 
	 * @throws IOException 
	 * 		Problem while accessing the articles.
	 * @throws SAXException 
	 * 		Problem while accessing the articles.
	 * @throws ParseException 
	 * 		Problem while accessing the articles.
	 */
	private static void checkEntityEmptiness(List<File> articles) throws ParseException, SAXException, IOException
	{	logger.log("Checking all entities");
		logger.increaseOffset();
		
		int problems = 0;
		
		logger.log("Processing each article separately");
		logger.increaseOffset();
		for(File file: articles)
		{	String name = file.getName();
			logger.log("Processing article '" + name + "'");
			logger.increaseOffset();
			Article article = Article.read(name);
			String text = article.getRawText();
			
			Entities entities = article.getReferenceEntities();
			entities.sortByPosition();
			List<AbstractEntity<?>> list = entities.getEntities();
			
			for(AbstractEntity<?> entity: list)
			{	int start = entity.getStartPos();
				int end = entity.getEndPos();
				if(start==end)
				{	logger.log("Problem with entity "+entity+": it is empty.");
					problems++;
				}
			}
			
			logger.decreaseOffset();
			logger.log("Article '" + name + "' completed");
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Entity check over: "+problems+" detected");
	}
}
