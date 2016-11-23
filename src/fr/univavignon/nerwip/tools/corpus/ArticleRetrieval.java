package fr.univavignon.nerwip.tools.corpus;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Scanner;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.retrieval.ArticleRetriever;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class contains various methods to retrieve sets
 * of articles from the Web.
 *  
 * @author Vincent Labatut
 */
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
		
		retrieveArticles("article.list.s2.txt");
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
	 * Scans a list of (Wikipedia-normalized) person
	 * names and retrieve the corresponding articles.
	 * The list can alternatively contain directly
	 * the full URLs.
	 * 
	 * @param fileName
	 * 		Name of the file containing the list.
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
	public static void retrieveArticles(String fileName) throws ReaderException, IOException, ParseException, SAXException
	{	logger.log("Adding new articles to our corpus");
		logger.increaseOffset();
		
		File file = new File(FileNames.FO_OUTPUT + File.separator + fileName);	
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		Scanner scanner = new Scanner(isr);
		logger.log("Reading file " + file);
		
		ArticleRetriever retriever = new ArticleRetriever();
		retriever.setCacheEnabled(true);
		
		logger.increaseOffset();
//		boolean pass = true;	// used for degbugging purposes
		while(scanner.hasNextLine())
		{	String name = scanner.nextLine().trim();
//			if(name.contains("Franco_Maria_Malfatti"))
//				pass = false;
//			if(!pass)
			{	logger.log("Processing '" + name + "'");
				URL url = null; 
				if(name.startsWith("http"))
					url = new URL(name);
				else
				{	String urlStr = "http://en.wikipedia.org/wiki/"+name;
					try
					{	url = new URL(urlStr);
					}
					catch (MalformedURLException e)
					{	e.printStackTrace();
					}
				}
				retriever.process(url);
			}
		}
		logger.decreaseOffset();
		
		scanner.close();
		logger.decreaseOffset();
		logger.log("Corpus completion over");
	}
}
