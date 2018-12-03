package fr.univavignon.common.tools.strings;

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
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.files.FileTools;

/**
 * This class handles lists of stop-words.
 *  
 * @author Vincent Labatut
 */
public class StopWordsManager
{
	/////////////////////////////////////////////////////////////////
	// DATA					/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing the stop-word lists for all supported languages */
	private final static Map<ArticleLanguage,List<String>> STOP_WORDS = new HashMap<ArticleLanguage,List<String>>();
	
	/**
	 * Returns the list of stop words for the specified language.
	 * 
	 * @param language
	 * 		Language of interest.
	 * @return
	 * 		List of stop words for the specified language.
	 */
	public static List<String> getStopWords(ArticleLanguage language)
	{	if(STOP_WORDS.isEmpty())
		{	try 
			{	loadData();
			}
			catch (FileNotFoundException e) 
			{	e.printStackTrace();
			}
			catch (UnsupportedEncodingException e) 
			{	e.printStackTrace();
			}
		}
		List<String> result = STOP_WORDS.get(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOADING				/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Loads the stop-word lists.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the stopword file. 
	 * @throws FileNotFoundException 
	 * 		Problem while accessing the stopword file. 
	 */
	private static void loadData() throws FileNotFoundException, UnsupportedEncodingException
	{	for(ArticleLanguage language: ArticleLanguage.values())
		{	List<String> list = new ArrayList<String>();
			STOP_WORDS.put(language,list);
			String filePath = FileNames.FO_MISC + File.separator + "stopwords_" + language.toString().toLowerCase() + FileNames.EX_TEXT;
			Scanner scanner = FileTools.openTextFileRead(filePath, "UTF-8");
			while(scanner.hasNextLine())
			{	String line = scanner.nextLine();
				String word = line.trim().toLowerCase();
				list.add(word);
			}
		}
	}
}
