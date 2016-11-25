package fr.univavignon.nerwip.tools.ner;

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

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleCategory;
import fr.univavignon.nerwip.data.article.ArticleList;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ConverterException;
import fr.univavignon.nerwip.processing.external.AbstractExternalConverter;
import fr.univavignon.nerwip.retrieval.ArticleRetriever;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.retrieval.reader.wikipedia.WikipediaReader;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * This class contains various methods to manage the data files
 * associated with processors (recognizers, resolvers, linkers).
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class DataProcessing
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
	{	logger.setName("Results-Management");
		
//		mergeDictionaries(Arrays.asList(
//			"buildings.txt",
//			"capital.txt",
//			"cities.txt",
//			"countries.txt",
//			"dataenCities.txt",
//			"known_country.lst",
//			"known_nationalities.lst",
//			"known_place.lst",
//			"known_state.lst",
//			"KnownNationalities.txt",
//			"statesWithAdjAndDem.txt"
//		),"locations.txt");
		
//		mergeDictionaries(Arrays.asList(
//			"corporations.txt",
//			"known_corporations.lst",
//			"polparties.txt",
//			"universities.txt"
//		),"organizations.txt");
		
//		mergeDictionaries(Arrays.asList(
//			"people.txt"
//		),"persons.txt");
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// DICTIONARIES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * This method was used to build dictionaries using
	 * the lists from the Illinois recognizer.
	 * 
	 * @param filesStr
	 * 		Several original files.
	 * @param outputStr
	 * 		Single file containing all expressions, each one appearing only once.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the files.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the files.
	 */
	private static void mergeDictionaries(List<String> filesStr, String outputStr) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.log("Converting lists");
		logger.increaseOffset();
		Set<String> content = new TreeSet<String>();
		
		// load all the files, put their content in the set
		logger.log("Loading existing lists");
		logger.increaseOffset();
		for(String fileStr: filesStr)
		{	logger.log("Loading lists " + fileStr);
			File file = new File(FileNames.FO_CUSTOM_LISTS + File.separator + fileStr);	
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			Scanner scanner = new Scanner(isr);
			
			while(scanner.hasNextLine())
			{	String line = scanner.nextLine().trim();
				content.add(line);
			}
			
			scanner.close();
		}
		logger.decreaseOffset();
		logger.log("Loading done");
		
		// record the set
		logger.log("Recording new list");
		File file = new File(FileNames.FO_CUSTOM_LISTS + File.separator + outputStr);	
		File folder = file.getParentFile();
		if(!folder.exists()) folder.mkdirs();
		PrintWriter pw = FileTools.openTextFileWrite(file, "UTF-8");
		for(String string: content)
			pw.println(string);
		pw.close();
		logger.log("Recording done");
		
		logger.decreaseOffset();
		logger.log("Convertion done");
	}
}
