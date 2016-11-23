package fr.univavignon.nerwip.tools.freebase;

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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;

/**
 * This class contains methods implementing 
 * some processing related to Freebase.
 * 
 * @author Vincent Labatut
 */
public class FbCache
{	
	/**
	 * Builds a new FB cache, using
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
	public FbCache(String fileName) throws FileNotFoundException, UnsupportedEncodingException
	{	super();
		
		// setting up the cache file
		String path = FileNames.FO_CACHE_FREEBASE + File.separator + fileName;
		file = new File(path);
		
		// loading the file
		loadCache();
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** File object representing the cache file */
	private File file;
	
	/**
	 * Loads the cache from file. This allows saving access
	 * to Freebase.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while loading the cache.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle the encoding.
	 */
	private void loadCache() throws FileNotFoundException, UnsupportedEncodingException
	{	Scanner scanner = FileTools.openTextFileRead(file, "UTF-8");
		
		while(scanner.hasNextLine())
		{	String line = scanner.nextLine();
			String temp[] = line.split("\t");
			String key = temp[0];
			List<String> value = new ArrayList<String>();
			for(int i=1;i<temp.length;i++)
				value.add(temp[i]);
			map.put(key,value);
		}
		
		scanner.close();
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
	 * @param values
	 * 		List of values associated to the entry.
	 * 
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the cache file.
	 * @throws FileNotFoundException 
	 * 		Problem while accessing the cache file.
	 */
	private void writeEntry(String key, List<String> values) throws FileNotFoundException, UnsupportedEncodingException
	{	PrintWriter printWriter = openCache();
		
		printWriter.print(key);
		for(String value: values)
			printWriter.print("\t" + value);
		printWriter.println();
		printWriter.flush(); // just a precaution
		
		printWriter.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// MAP				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map used to store the data */
	private final Map<String,List<String>> map = new HashMap<String,List<String>>();
	 
	/**
	 * Retrieves the values associated to some key in the cache. 
	 * Or {@code null} if the cache does not contain the key.
	 * 
	 * @param key
	 * 		The string to look for.
	 * @return
	 * 		The associated list of values.
	 */
	public List<String> getValues(String key)
	{	List<String> result = map.get(key);
		return result;
	}
	
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
	{	List<String> list = map.get(key);
		String result = null;
		if(list!=null && !list.isEmpty())
			result = list.get(0);
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
	 * @param values
	 * 		The associated list of values.	
	 * 
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the cache file.
	 * @throws FileNotFoundException 
	 * 		Problem while accessing the cache file.
	 */
	public void putValues(String key, List<String> values) throws FileNotFoundException, UnsupportedEncodingException
	{	if(!map.containsKey(key))
		{	// update memory cache
			map.put(key,values);
			
			// update file cache
			writeEntry(key,values);
		}
	}
	
	/**
	 * Insert a new key in the cache,
	 * with a single associated value.
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
	{	List<String> values = new ArrayList<String>();
		values.add(value);
		putValues(key,values);
	}
}
