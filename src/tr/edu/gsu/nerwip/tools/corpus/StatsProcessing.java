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
import java.util.HashMap;
import java.util.List;
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
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalConverter;
import tr.edu.gsu.nerwip.retrieval.ArticleRetriever;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.StringTools;

/**
 * This class contains various methods dedicated to the 
 * processing of corpus statistics.
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class StatsProcessing
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
	{	logger.setName("Stat-Processing");
	
		processCorpusStats();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Calculates various stats regarding
	 * the corpus: size of the articles,
	 * number of entities, etc. Everything
	 * is recorded in a file in the corpus
	 * folder.
	 * 
	 * @throws SAXException 
	 * 		Problem while accessing a file.
	 * @throws ParseException 
	 * 		Problem while accessing a file.
	 * @throws IOException 
	 * 		Problem while accessing a file.
	 * @throws ReaderException 
	 * 		Problem while accessing a file.
	 */
	private static void processCorpusStats() throws ReaderException, IOException, ParseException, SAXException
	{	logger.log("Processing corpus statistics");
		logger.increaseOffset();
		
		// get articles
		ArticleList folders = ArticleLists.getArticleList();
//		ArticleList folders = ResultsManagement.getArticleList("training.set.txt");
//		ArticleList folders = ResultsManagement.getArticleList("testing.set.txt");
		
		// init stat variables
		List<String> names = new ArrayList<String>();
		List<Integer> wordCounts = new ArrayList<Integer>();
		List<Integer> charCounts = new ArrayList<Integer>();
		List<List<ArticleCategory>> categories = new ArrayList<List<ArticleCategory>>();
		Map<EntityType,List<Integer>> entityTypes = new HashMap<EntityType, List<Integer>>();
		
		// process each article
		logger.log("Process each article");
		logger.increaseOffset();
		ArticleRetriever retriever = new ArticleRetriever();
		for(File folder: folders)
		{	logger.log("Process article "+folder.getName());
			logger.increaseOffset();
			
			// get text
			logger.log("Retrieve text");
			String name = folder.getName();
			names.add(name);
			URL url = new URL("http://en.wikipedia.org/wiki/"+name);
			Article article = retriever.process(url);
			String text = article.getRawText();
			// categories
			List<ArticleCategory> cats = article.getCategories();
			categories.add(cats);
			logger.log("Categories: "+cats.toString());
			// words
			int wordCount = text.trim().split("\\s+").length;
			wordCounts.add(wordCount);
			logger.log("Count words: "+wordCount);
			// chars
			int charCount = text.length();
			charCounts.add(charCount);
			logger.log("Count characters: "+charCount);
			
			// entities
			String entitiesPath = folder.getPath() + File.separator + FileNames.FI_ENTITY_LIST;
			File entitiesFile = new File(entitiesPath);
			Map<EntityType,Integer> counts = new HashMap<EntityType, Integer>();
			if(entitiesFile.exists())
			{	Entities entities = Entities.readFromXml(entitiesFile);
				List<AbstractEntity<?>> entityList = entities.getEntities();
				for(AbstractEntity<?> entity: entityList)
				{	EntityType type = entity.getType();
					Integer count = counts.get(type);
					if(count==null)
						count = 0;
					count++;
					counts.put(type,count);
				}
			}
			for(EntityType type: EntityType.values())
			{	List<Integer> list = entityTypes.get(type);
				if(list==null)
				{	list = new ArrayList<Integer>();
					entityTypes.put(type,list);
				}
				Integer count = counts.get(type);
				if(count==null)
					count = 0;
				logger.log("Count "+type.toString()+" entities: "+count);
				list.add(count);
			}
			
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		
		// record stats
		logger.log("Record statistics");
		String path = FileNames.FO_OUTPUT + File.separator + FileNames.FI_STATS_TEXT;
		PrintWriter pw = FileTools.openTextFileWrite(path);
		{	String header = "Name\tWords\tChars";
			for(EntityType type: EntityType.values())
				header = header + "\t" + type.toString();
			for(ArticleCategory cat: ArticleCategory.values())
				header = header + "\t" + cat.toString();
			pw.println(header);
		}
		for(int i=0;i<folders.size();i++)
		{	String line = names.get(i);
			line = line + "\t" + wordCounts.get(i);
			line = line + "\t" + charCounts.get(i);
			for(EntityType type: EntityType.values())
			{	List<Integer> list = entityTypes.get(type);
				line = line + "\t" + list.get(i);
			}
			List<ArticleCategory> cats = categories.get(i);
			for(ArticleCategory cat: ArticleCategory.values())
			{	if(cats.contains(cat))
					line = line + "\t1";
				else
					line = line + "\t0";
			}
			pw.println(line);
		}
		pw.close();
		
		logger.log("Corpus statistics processed");
		logger.decreaseOffset();
	}
}
