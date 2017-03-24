package fr.univavignon.nerwip.tools.wikimedia;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;

/**
 * This class is dedicated to caching 
 * WikiMedia requests and results.
 * 
 * @author Vincent Labatut
 */
public class WmCache
{	
	/**
	 * Builds a new WikiMedia cache, using
	 * the specified file name.
	 * 
	 * @param fileName
	 * 		File used for caching.
	 * 
	 * @throws FileNotFoundException 
	 * 		Problem while accessing the cache file.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle the encoding.
	 */
	public WmCache(String fileName) throws FileNotFoundException, UnsupportedEncodingException
	{	super();
		
		// setting up the cache file
		String path = FileNames.FO_CACHE_WIKIMEDIA + File.separator + fileName;
		file = new File(path);
		
		// loading the file
		loadCache();
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** File object representing the cache file */
	private File file;
	/** Separates two entries in the file */
	private final static String SEPARATOR = "-----------";
	
	/**
	 * Loads the cache from file. This allows saving access
	 * to WikiMedia.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while loading the cache.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle the encoding.
	 */
	private void loadCache() throws FileNotFoundException, UnsupportedEncodingException
	{	if(file.exists())
		{	Scanner scanner = FileTools.openTextFileRead(file, "UTF-8");
			
			while(scanner.hasNextLine())
			{	String key = scanner.nextLine().trim();
				StringBuffer tmp = new StringBuffer();
				String line;
				do
				{	line = scanner.nextLine().trim();
					if(!line.equals(SEPARATOR))
						tmp.append(line+"\n");
				}
				while(!line.equals(SEPARATOR));
				String value = tmp.toString().trim();
				map.put(key,value);
			}
			
			scanner.close();
		}
	}
	
	/**
	 * Opens the cache file in write mode, allowing
	 * to append new entries without losing the existing ones.
	 * 
	 * @return
	 * 		PrintWriter used to write in the cache.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while opening the file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while opening the file.
	 */
	private PrintWriter openCache() throws FileNotFoundException, UnsupportedEncodingException
	{	FileOutputStream fos = new FileOutputStream(file,true);	// open the file in append mode
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		PrintWriter result = new PrintWriter(osw);
		return result;
	}
	
	/**
	 * Appends a new entry at the end of the
	 * existing cache file.
	 * 
	 * @param key
	 * 		Key of the new entry.
	 * @param value
	 * 		Value associated to the entry.
	 * 
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the cache file.
	 * @throws FileNotFoundException 
	 * 		Problem while accessing the cache file.
	 */
	private void writeEntry(String key, String value) throws FileNotFoundException, UnsupportedEncodingException
	{	PrintWriter printWriter = openCache();
		
		printWriter.println(key);
		printWriter.println(value);
		printWriter.println(SEPARATOR);
		printWriter.flush(); // just a precaution
		
		printWriter.close();
	}

	
	/////////////////////////////////////////////////////////////////
	// MAP				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map used to store the data */
	private final Map<String,String> map = new HashMap<String,String>();
	 
	/**
	 * Retrieves the values associated to some key in the cache,
	 * and returns the first one, or {@code null} if the 
	 * cache does not contain the key.
	 * 
	 * @param key
	 * 		The string to look for.
	 * @return
	 * 		The associated values.
	 */
	public String getValue(String key)
	{	String result = map.get(key);
		return result;
	}
	
	/**
	 * Insert a new key in the cache,
	 * with the associated values.
	 * The cache file is automatically
	 * updated, too.
	 * 
	 * @param key
	 * 		The new key string.
	 * @param value
	 * 		The associated value.	
	 * 
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the cache file.
	 * @throws FileNotFoundException 
	 * 		Problem while accessing the cache file.
	 */
	public void putValue(String key, String value) throws FileNotFoundException, UnsupportedEncodingException
	{	if(!map.containsKey(key))
		{	// update memory cache
			map.put(key,value);
			// update file cache
			writeEntry(key,value);
		}
	}
}
