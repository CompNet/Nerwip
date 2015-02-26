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
 * This class contains various methods to handle lists of articles.
 * These methods are mainly used when testing/debugging Nerwip, or
 * to make NER comparisons.
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class ArticleLists
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
	{	logger.setName("Article-Lists");
		
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// ARTICLELIST		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of all articles in
	 * the corpus.
	 * 
	 * @return
	 * 		A list of {@code File} objects.
	 */
	public static ArticleList getArticleList()
	{	logger.log("Retrieving the list of articles");
		File folder = new File(FileNames.FO_OUTPUT);
		File articles[] = folder.listFiles(FileTools.FILTER_DIRECTORY);
		List<File> list = Arrays.asList(articles);
		Collections.sort(list);
		ArticleList result = new ArticleList("all", list);
		return result;
	}
	
	/**
	 * Returns the list of all articles listed 
	 * in the specified file.
	 * 
	 * @param listFile
	 * 		File containing the article names. 
	 * @return
	 * 		A list of {@code File} objects.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the list file.
	 */
	public static ArticleList getArticleList(String listFile) throws FileNotFoundException
	{	String path = FileNames.FO_OUTPUT + File.separator + listFile;
		logger.log("Reading the list of articles from "+path);
		logger.increaseOffset();
		
		logger.log("Opening the file");
		File file = new File(path);
		Scanner scanner = FileTools.openTextFileRead(file);
		
		ArticleList result = new ArticleList(listFile);
		while(scanner.hasNextLine())
		{	String line = scanner.nextLine().trim();
			if(!line.isEmpty())
			{	String fn = FileNames.FO_OUTPUT + File.separator + line;
				File f = new File(fn);
				result.add(f);
			}
		}
		logger.log("Articles in the list: "+result.size());
		
		logger.log("Closing the file");
		scanner.close();
		Collections.sort(result);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// URL				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of the URL of all articles in
	 * the corpus.
	 * 
	 * @return
	 * 		A list of URLs.
	 */
	public static List<URL> getArticleUrlList()
	{	logger.log("Retrieving the list of articles url");
		File folder = new File(FileNames.FO_OUTPUT);
		File articles[] = folder.listFiles(FileTools.FILTER_DIRECTORY);
		List<File> files = new ArrayList<File>(Arrays.asList(articles));
		Collections.sort(files);

		List<URL> result = new ArrayList<URL>();
		for(File file: articles)
		{	String name = file.getName();
			String urlStr = "http://en.wikipedia.org/wiki/"+name;
			try
			{	URL url = new URL(urlStr);
				result.add(url);
			}
			catch (MalformedURLException e)
			{	e.printStackTrace();
			}
		}
		
		return result;
	}
}
