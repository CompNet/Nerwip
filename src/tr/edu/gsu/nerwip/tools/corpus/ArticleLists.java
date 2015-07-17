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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.evaluation.ArticleList;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class contains various methods to handle lists of articles.
 * These methods are mainly used when testing/debugging Nerwip, or
 * to make NER comparisons.
 *  
 * @author Vincent Labatut
 */
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
		
//		generateArticleList();
		
		File corpus = new File(FileNames.FO_OUTPUT);
		File output = new File(FileNames.FO_OUTPUT+File.separator+"annotated.txt");
		generateAnnotatedArticleList(corpus,output);
	
		logger.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// ARTICLE LISTS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of all articles in
	 * the corpus.
	 * 
	 * @return
	 * 		A list of {@code File} objects.
	 */
	public static ArticleList getArticleList()
	{	File corpusFolder = new File(FileNames.FO_OUTPUT);
		ArticleList result = getArticleList(corpusFolder);
		return result;
	}
	
	/**
	 * Returns the list of all articles in
	 * the corpus located in the specified folder.
	 * 
	 * @param corpusFolder
	 * 		Corpus folder.
	 * @return
	 * 		A list of {@code File} objects.
	 */
	public static ArticleList getArticleList(File corpusFolder)
	{	logger.log("Retrieving the list of articles");
		ArticleList result = null;
//		File articles[] = corpusFolder.listFiles(FileTools.FILTER_DIRECTORY);
		File articles[] = corpusFolder.listFiles(FileTools.FILTER_ARTICLES);
		if(articles!=null)
		{	List<File> list = Arrays.asList(articles);
			Collections.sort(list);
			result = new ArticleList("all", list);
		}
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
	
	/**
	 * Returns half the list of all articles in
	 * the corpus.
	 * 
	 * @param first
	 * 		If {@code true}, returns the first half,
	 * 		otherwise the second half.
	 * 
	 * @return
	 * 		A list of {@code File} objects.
	 */
	public static ArticleList getArticleHalfList(boolean first)
	{	logger.log("Retrieving the list of articles");
		
		// get the full list
		File folder = new File(FileNames.FO_OUTPUT);
//		File articles[] = folder.listFiles(FileTools.FILTER_DIRECTORY);
		File articles[] = folder.listFiles(FileTools.FILTER_ARTICLES);
		List<File> list = Arrays.asList(articles);
		Collections.sort(list);
		
		// retain only half the list
		ArticleList result;
		if(first)
			result = new ArticleList("half1", list.subList(0, list.size()/2+1));
		else
			result = new ArticleList("half2", list.subList(list.size()/2+1,list.size()));
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE LISTS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Format last modification date */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	/**
	 * Returns the list of the corpus articles which have been annotated
	 * manually, as well as other details (name of the editor, size in 
	 * chars, etc.).
	 * 
	 * @param corpus
	 * 		Main folder of the considered corpus.
	 * @param output
	 * 		Ouput file (a text file).
	 *  
	 * @throws ParseException
	 * 		Problem while reading the entity file. 
	 * @throws IOException 
	 * 		Problem while accessing a file. 
	 * @throws SAXException 
	 * 		Problem while accessing a file. 
	 */
	public static void generateAnnotatedArticleList(File corpus, File output) throws SAXException, IOException, ParseException
	{	String sep = ",";
		// open output file
		PrintWriter pw = FileTools.openTextFileWrite(output);
		pw.println("Number"+sep+"Title"+sep+"Size"+sep+"Date"+sep+"Editor"+sep+"Count");
		
		FilenameFilter ffEnt = FileTools.createFilter(FileNames.FI_ENTITY_LIST);
		FilenameFilter ffRaw = FileTools.createFilter(FileNames.FI_RAW_TEXT);
		Map<String,Integer> counts = new HashMap<String, Integer>();
			
		// get the list of articles
		ArticleList list = getArticleList(corpus);
		int i = 1;
		for(File folder: list)
		{	String name = folder.getName();
			File files[] = folder.listFiles(ffEnt);
			if(files.length>0)
			{	File file = files[0];
				Entities entities = Entities.readFromXml(file);
				String editor = entities.getEditor();
				if(editor!=null)
				{	files = folder.listFiles(ffRaw);
					if(files.length>0)
					{	file = files[0];
						Date date = entities.getModificationDate();
						String dateStr = DATE_FORMAT.format(date);
						String rawText = FileTools.readTextFile(file);
						int size = rawText.length();
						Integer count = counts.get(editor);
						if(count==null)
							count = 0;
						count++;
						counts.put(editor, count);
						pw.println(i+sep+name+sep+size+sep+dateStr+sep+editor+sep+count);
					}
				}
			}
			i++;
		}
		
		// close output file
		pw.close();
	}
	
	/**
	 * Returns the list of the corpus articles which have <i>not</i>been annotated
	 * manually, as well as other details (size in chars, etc.).
	 * 
	 * @param corpus
	 * 		Main folder of the considered corpus.
	 * @param output
	 * 		Ouput file (a text file).
	 *  
	 * @throws ParseException
	 * 		Problem while reading the entity file. 
	 * @throws IOException 
	 * 		Problem while accessing a file. 
	 * @throws SAXException 
	 * 		Problem while accessing a file. 
	 */
	public static void generateNonAnnotatedArticleList(File corpus, File output) throws SAXException, IOException, ParseException
	{	String sep = ",";
		// open output file
		PrintWriter pw = FileTools.openTextFileWrite(output);
		pw.println("Number"+sep+"Title"+sep+"Size");
		
		FilenameFilter ffEnt = FileTools.createFilter(FileNames.FI_ENTITY_LIST);
		FilenameFilter ffRaw = FileTools.createFilter(FileNames.FI_RAW_TEXT);
			
		// get the list of articles
		ArticleList list = getArticleList(corpus);
		int i = 1;
		for(File folder: list)
		{	String name = folder.getName();
			
			File files[] = folder.listFiles(ffRaw);
			if(files.length>0)
			{	boolean treat = false;
				File file = files[0];
				String rawText = FileTools.readTextFile(file);
				int size = rawText.length();
				
				files = folder.listFiles(ffEnt);
				if(files.length==0)
					treat = true;
				else
				{	file = files[0];
					Entities entities = Entities.readFromXml(file);
					String editor = entities.getEditor();
					if(editor==null)
						treat = true;
				}
				
				if(treat)
					pw.println(i+sep+name+sep+size);
			}
			i++;
		}
		
		// close output file
		pw.close();
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
	{	logger.log("Retrieving the list of article URLs");
		File folder = new File(FileNames.FO_OUTPUT);
//		File articles[] = folder.listFiles(FileTools.FILTER_DIRECTORY);
		File articles[] = folder.listFiles(FileTools.FILTER_ARTICLES);
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
	
	/**
	 * Creates a text file containing the list of all
	 * article names from the corpus.
	 *  
	 * @throws FileNotFoundException
	 * 		Problem while write the list file. 
	 * @throws UnsupportedEncodingException 
	 * 		Problem while write the list file. 
	 */
	public static void generateArticleList() throws UnsupportedEncodingException, FileNotFoundException
	{	logger.log("Creating a file containing the list of articles");
		logger.increaseOffset();
		
		ArticleList list = getArticleList();
		Collections.sort(list);
		
		String filePath = FileNames.FO_OUTPUT + File.separator + "generated.list.txt";
		PrintWriter pw = FileTools.openTextFileWrite(filePath);
		
		for(File file: list)
		{	String name = file.getName();
			pw.println(name);
		}
		
		pw.close();
		logger.decreaseOffset();
		logger.log("List complete ("+list.size()+" articles)");
	}
}
