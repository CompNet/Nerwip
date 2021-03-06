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
 * This class contains various methods used to complete pre-existing
 * articles and make them compatible with subsequent versions of Nerwip.
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class ArticleCompletion
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
	{	logger.setName("Article-Completion");
		
//		removeArticleCategories();
//		insertArticleCategories();
//		completeArticleCategories();
//		insertArticleTitles();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// CATEGORIES	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Removes the categories of all existing
	 * articles in the corpus.
	 * <br/>
	 * Note: category loading must be disabled first,
	 * by adding comments in class Article.
	 * 
	 * @throws IOException 
	 * 		Problem while accessing the files.
	 * @throws SAXException 
	 * 		Problem while accessing the files.
	 * @throws ParseException 
	 * 		Problem while accessing the files.
	 */
	private static void removeArticleCategories() throws ParseException, SAXException, IOException
	{	logger.log("Resetting categories in articles");
		logger.increaseOffset();
		
		List<File> files = ArticleLists.getArticleList();
		for(File file: files)
		{	String name = file.getName();
			logger.log("Processing article '" + name + "'");
			logger.increaseOffset();

			Article article = Article.read(name);
			article.write();
			
			logger.decreaseOffset();
		}
		
		logger.decreaseOffset();
		logger.log("Categories reset");
	}
	
	/**
	 * This methods allows setting the category of 
	 * articles already retrieved and manually annotated
	 * for evaluation. This way, the annotation can be
	 * performed overall, or in function of the category.
	 * The categories must be listed in a file in which
	 * each line contains the name of the article folder
	 * and the corresponding category.
	 * 
	 * @throws ParseException
	 * 		Problem while accessing the files.
	 * @throws SAXException
	 * 		Problem while accessing the files.
	 * @throws IOException
	 * 		Problem while accessing the files.
	 */
	private static void insertArticleCategories() throws ParseException, SAXException, IOException
	{	logger.log("Setting categories in articles");
		logger.increaseOffset();
		
		File file = new File(FileNames.FO_OUTPUT + File.separator + "cats" + FileNames.EX_TXT);	
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		Scanner scanner = new Scanner(isr);
		logger.log("Reading file " + file);
		
		logger.increaseOffset();
		while(scanner.hasNextLine())
		{	String line = scanner.nextLine().trim();
			String temp[] = line.split("\\t");
			String name = temp[0];
			String folderPath = FileNames.FO_OUTPUT + File.separator + name;
			File folderFile = new File(folderPath);
			if(folderFile.exists())
			{	
//				String gender = temp[1];
				String categoryStr = temp[2].toUpperCase(Locale.ENGLISH);
				ArticleCategory category = ArticleCategory.valueOf(categoryStr);
				//category = StringTools.initialize(category);
				logger.log("Processing '" + name + "': cat="+category);
				List<ArticleCategory> categories = new ArrayList<ArticleCategory>();
				categories.add(category);
				
				Article article = Article.read(name);
				article.setCategories(categories);
				article.write();
			}
			else
				logger.log("Processing '" + temp[0] + "': folder not found");
		}
		logger.decreaseOffset();
		
		scanner.close();
		logger.decreaseOffset();
		logger.log("Categories set");
	}
	
	/**
	 * Reads existing article, and apply the new
	 * method to set their categories.
	 * 
	 * @throws ParseException
	 * 		Problem while accessing the files.
	 * @throws SAXException
	 * 		Problem while accessing the files.
	 * @throws IOException
	 * 		Problem while accessing the files.
	 * @throws org.apache.http.ParseException
	 * 		Problem while accessing the files.
	 * @throws org.json.simple.parser.ParseException
	 * 		Problem while accessing the files.
	 */
	private static void completeArticleCategories() throws ParseException, SAXException, IOException, org.apache.http.ParseException, org.json.simple.parser.ParseException
	{	logger.log("Completing categories in articles");
		logger.increaseOffset();

		WikipediaReader reader = new WikipediaReader();
		
		List<File> files = ArticleLists.getArticleList();
		for(File file: files)
		{	String name = file.getName();
			logger.log("Processing article '" + name + "'");
			logger.increaseOffset();
			Article article = Article.read(name);
			
			List<ArticleCategory> categories = reader.getArticleCategories(article);
			article.setCategories(categories);
			logger.log("Detected categories: " + categories.toString());
			
			article.write();
			logger.log("Completed article written");
			logger.decreaseOffset();
		}
		
		logger.decreaseOffset();
		logger.log("Categories completed");
	}
	
	/////////////////////////////////////////////////////////////////
	// TITLES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * This methods allows setting the title of 
	 * articles already retrieved and manually annotated
	 * for evaluation. We use the first sentence of the
	 * article to initialize the title.
	 * 
	 * @throws ParseException
	 * 		Problem while accessing the files.
	 * @throws SAXException
	 * 		Problem while accessing the files.
	 * @throws IOException
	 * 		Problem while accessing the files.
	 */
	private static void insertArticleTitles() throws ParseException, SAXException, IOException
	{	logger.log("Setting title in articles");
		logger.increaseOffset();
		
		logger.increaseOffset();
		List<File> files = ArticleLists.getArticleList();
		for(File file: files)
		{	String name = file.getName();
			Article article = Article.read(name);
			String rawText = article.getRawText();
			String sentence = rawText.substring(0,25);
			
			logger.log("Processing article "+name+": '"+sentence+"'");
			article.setTitle(sentence);
			article.write();
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		logger.log("Titles set");
	}
}
