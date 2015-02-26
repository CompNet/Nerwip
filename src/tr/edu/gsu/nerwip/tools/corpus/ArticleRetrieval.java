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
 * This class contains various methods to retrieve sets
 * of articles from the web.
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class ArticleRetrieval
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
	{	logger.setName("Article-Retrieval");
		
//		retrieveArticles();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/**
	 * Scans a list of (Wikipedia-normalized) person
	 * names and retrieve the corresponding articles.
	 * 
	 * @throws ReaderException
	 * 		Problem while accessing the list of names or an article.
	 * @throws IOException
	 * 		Problem while accessing the list of names or an article.
	 * @throws ParseException
	 * 		Problem while accessing the list of names or an article.
	 * @throws SAXException
	 * 		Problem while accessing the list of names or an article.
	 */
	private static void retrieveArticles() throws ReaderException, IOException, ParseException, SAXException
	{	logger.log("Adding new articles to our corpus");
		logger.increaseOffset();
		
		File file = new File(FileNames.FO_OUTPUT + File.separator + "article.list.s2" + FileNames.EX_TXT);	
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		Scanner scanner = new Scanner(isr);
		logger.log("Reading file " + file);
		
		ArticleRetriever retriever = new ArticleRetriever();
		retriever.setCacheEnabled(false);
		
		logger.increaseOffset();
		boolean pass = true;	// used for degbugging purposes
		while(scanner.hasNextLine())
		{	String name = scanner.nextLine().trim();
			if(name.contains("Franco_Maria_Malfatti"))
				pass = false;
			if(!pass)
			{	logger.log("Processing '" + name + "'");
				String urlStr = "http://en.wikipedia.org/wiki/"+name;
				try
				{	URL url = new URL(urlStr);
					retriever.process(url);
				}
				catch (MalformedURLException e)
				{	e.printStackTrace();
				}
			}
		}
		logger.decreaseOffset();
		
		scanner.close();
		logger.decreaseOffset();
		logger.log("Corpus completion over");
	}
}
