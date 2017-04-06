package fr.univavignon.nerwip.data.entity;

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
import java.io.UnsupportedEncodingException;

import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;

/**
 * Knowledge based used for linking (i.e. associating an URI or
 * any other unique id to an entity).
 * <br/>
 * The descriptions of the KB names are in file {@value #FILE}.
 * 
 * @author Vincent Labatut
 */
public class KnowledgeBase
{	
	/////////////////////////////////////////////////////////////////
	// NAMES		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of DBpedia */
	public static final String DBPEDIA_URI = "DBPEDIA_URI";
	/** Name of WikiData */
	public static final String WIKIDATA_ID = "WIKIDATA_ID";
	/** List of registered entity names */
	private static final Set<String> NAMES = new TreeSet<String>();
	
	/**
	 * Checks if the specified string is the name of a registered
	 * knowledge base.
	 * 
	 * @param kbName
	 * 		Name of the KB.
	 * @return
	 * 		{@code true} iff the string is registered as a KB name.
	 */
	public static boolean isRegistered(String kbName)
	{	boolean result = NAMES.contains(kbName);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE			 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the text file containing the names of all registered KB */
	public static final File FILE = new File(FileNames.FO_MISC+File.separator+FileNames.FI_KB_NAMES);
	
	/**
	 * Loads all the registered KB names, based on the list provided
	 * by Wikidata.
	 */
	static
	{	try
		{	// open the file
			Scanner sc = FileTools.openTextFileRead(FILE, "UTF-8");
			
			// read the file
			while(sc.hasNextLine())
			{	String line = sc.nextLine();
				String[] tmp = line.split("\t");
				String name = tmp[0]; // we ignore the possible other columns
				if(NAMES.contains(name))
					throw new IllegalArgumentException("Knowledge base name \""+name+"\" appears twice in "+FILE);
				else
					NAMES.add(name);
			}
			
			// close the file
			sc.close();
		}
		catch (FileNotFoundException e)
		{	e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{	e.printStackTrace();
		}
	
		// add the missing names
		NAMES.add(DBPEDIA_URI);
		NAMES.add(WIKIDATA_ID);
	}
}
