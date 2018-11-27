package fr.univavignon.common.tools.files;

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

import fr.univavignon.tools.files.FileNames;

/**
 * This class contains various methods
 * related to file management.
 *  
 * @author Vincent Labatut
 */
public class CommonFileNames
{	
	/////////////////////////////////////////////////////////////////
	// FOLDERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Folder used to store certain cached files */
	public final static String FO_CACHE = FileNames.FO_RESOURCES + File.separator + "cache";
	/** Output folder */
//	public final static String FO_OUTPUT = "out";	//actual folder

//	public final static String FO_OUTPUT = "/home/vlabatut/Dropbox/NetExtraction/Data";
	public final static String FO_OUTPUT = "/home/vlabatut/Downloads/Data";
//	public final static String FO_OUTPUT = "/home/vlabatut/eclipse/workspaces/Extraction/Nerwip/out";
	
//	public final static String FO_OUTPUT = "D:/Users/Vincent/Documents/Dropbox/Nerwip2/out";
//	public final static String FO_OUTPUT = "D:/Users/Vincent/Documents/Dropbox/NetExtraction/Data2";
//	public final static String FO_OUTPUT = "D:/Users/Vincent/Documents/Dropbox/NetExtraction/Data";
//	public final static String FO_OUTPUT = "C:/Users/Vincent/Downloads/Data";
//	public final static String FO_OUTPUT = "D:/Users/Vincent/Downloads/Web/_ner/fr";

	/** Folder used to store various data */
	public final static String FO_MISC = FileNames.FO_RESOURCES + File.separator + "misc";

	/////////////////////////////////////////////////////////////////
	// FILES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** XML schema file used to record article properties  */
	public final static String FI_PROPERTY_SCHEMA = "properties" + FileNames.EX_SCHEMA;
	/** File containing the properties of the article */
	public final static String FI_PROPERTIES = "properties" + FileNames.EX_XML;
	/** File containing original page */
	public final static String FI_ORIGINAL_PAGE = "original" + FileNames.EX_HTML;
	/** File containing the raw text */
	public final static String FI_RAW_TEXT = "raw" + FileNames.EX_TEXT;
	/** XML schema file used to record mentions  */
	public final static String FI_MENTION_SCHEMA = "mentions" + FileNames.EX_SCHEMA;
	/** XML schema file used to record entities  */
	public final static String FI_ENTITY_SCHEMA = "entities" + FileNames.EX_SCHEMA;
	/** XML file containing the mentions estimated by a recognizer or completed by a resolver, in a normalized format */
	public final static String FI_MENTION_LIST = "mentions" + FileNames.EX_XML;
	/** XML file containing the entities detected by a resolver or linked by a linker, in a normalized format */
	public final static String FI_ENTITY_LIST = "entities" + FileNames.EX_XML;
}
