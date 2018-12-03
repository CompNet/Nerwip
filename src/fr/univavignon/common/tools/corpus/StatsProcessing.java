package fr.univavignon.common.tools.corpus;

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
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleList;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.common.tools.files.CommonFileNames;
import fr.univavignon.retrieval.ArticleRetriever;
import fr.univavignon.retrieval.reader.ReaderException;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

/**
 * This class contains various methods dedicated to the 
 * processing of corpus statistics.
 *  
 * @author Vincent Labatut
 */
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
	/** File containing some statistics processed on the corpus */
	private final static String FI_STATS_TEXT = "stats" + FileNames.EX_TEXT;
	
	/**
	 * Calculates various stats regarding
	 * the corpus: size of the articles,
	 * number of mentions, etc. Everything
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
			Article article = retriever.process(name);
			String text = article.getRawText();
			// words
			int wordCount = text.trim().split("\\s+").length;
			wordCounts.add(wordCount);
			logger.log("Count words: "+wordCount);
			// chars
			int charCount = text.length();
			charCounts.add(charCount);
			logger.log("Count characters: "+charCount);
			
			// mentions
			String mentionsPath = folder.getPath() + File.separator + CommonFileNames.FI_MENTION_LIST;
			File mentionsFile = new File(mentionsPath);
			Map<EntityType,Integer> counts = new HashMap<EntityType, Integer>();
			if(mentionsFile.exists())
			{	Mentions mentions = Mentions.readFromXml(mentionsFile);
				List<AbstractMention<?>> mentionList = mentions.getMentions();
				for(AbstractMention<?> mention: mentionList)
				{	EntityType type = mention.getType();
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
				logger.log("Count "+type.toString()+" mentions: "+count);
				list.add(count);
			}
			
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		
		// record stats
		logger.log("Record statistics");
		String path = FileNames.FO_OUTPUT + File.separator + FI_STATS_TEXT;
		PrintWriter pw = FileTools.openTextFileWrite(path, "UTF-8");
		{	String header = "Name\tWords\tChars";
			for(EntityType type: EntityType.values())
				header = header + "\t" + type.toString();
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
			pw.println(line);
		}
		pw.close();
		
		logger.log("Corpus statistics processed");
		logger.decreaseOffset();
	}
}
