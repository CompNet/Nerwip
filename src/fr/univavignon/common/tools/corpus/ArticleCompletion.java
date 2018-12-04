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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.xml.sax.SAXException;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.article.ArticleList;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.tools.file.NerwipFileNames;
import fr.univavignon.retrieval.ArticleRetriever;
import fr.univavignon.retrieval.ReaderException;
import fr.univavignon.retrieval.wikipedia.WikipediaReader;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

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
		
//		insertArticleTitles();
//		insertArticleLanguages(ArticleLanguage.FR);
		reformatRetrievalDate();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
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
			
			logger.log("Processing article "+name+": \""+sentence+"\"");
			article.setTitle(sentence);
			article.write();
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		logger.log("Titles set");
	}
	
	/////////////////////////////////////////////////////////////////
	// LANGUAGE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * This methods allows setting the language of 
	 * articles already retrieved and manually annotated
	 * for evaluation.
	 * 
	 * @param language
	 * 		The language to set in the articles. 
	 * 
	 * @throws ParseException
	 * 		Problem while accessing the files.
	 * @throws SAXException
	 * 		Problem while accessing the files.
	 * @throws IOException
	 * 		Problem while accessing the files.
	 */
	private static void insertArticleLanguages(ArticleLanguage language) throws ParseException, SAXException, IOException
	{	logger.log("Setting language to "+language+" in all articles");
		logger.increaseOffset();
		
		logger.increaseOffset();
		List<File> files = ArticleLists.getArticleList();
		for(File file: files)
		{	String name = file.getName();
			Article article = Article.read(name);
			logger.log("Processing article "+name);
			article.setLanguage(language);
			article.write();
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		logger.log("Languages set");
	}

	/////////////////////////////////////////////////////////////////
	// DATES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * This methods allows setting the date format of 
	 * articles already retrieved, to the new format.
	 * 
	 * @throws ParseException
	 * 		Problem while accessing the files.
	 * @throws SAXException
	 * 		Problem while accessing the files.
	 * @throws IOException
	 * 		Problem while accessing the files.
	 */
	private static void reformatRetrievalDate() throws ParseException, SAXException, IOException
	{	logger.log("Reformatting retrieval dates in all articles");
		logger.increaseOffset();
		
		logger.increaseOffset();
		List<File> files = ArticleLists.getArticleList();
		for(File file: files)
		{	String name = file.getName();
			Article article = Article.read(name);
			logger.log("Processing article "+name);
			article.write();
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		logger.log("Dates formatted");
	}
}
