package fr.univavignon.tools.files;

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

/**
 * This class contains various constants
 * related to file and folder names.
 *  
 * @author Vincent Labatut
 */
public class FileNames
{	
	/////////////////////////////////////////////////////////////////
	// FOLDERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Log folder */
	public final static String FO_LOG = "log";
	/** Resources folder */
	public final static String FO_RESOURCES = "res";
		/** Folder used to store certain cached files */
		public final static String FO_CACHE = FO_RESOURCES + File.separator + "cache";
		/** Folder containing the XML schemas */
		public final static String FO_SCHEMA = FO_RESOURCES + File.separator + "schemas";
		/** Folder used to store various data */
		public final static String FO_MISC = FO_RESOURCES + File.separator + "misc";
		/** Output folder */
//		public final static String FO_OUTPUT = "out";	//actual folder
	
//		public final static String FO_OUTPUT = "/home/vlabatut/Dropbox/NetExtraction/Data";
//		public final static String FO_OUTPUT = "/home/vlabatut/Downloads/Data";
		public final static String FO_OUTPUT = "/home/vlabatut/Downloads/out_m";
//		public final static String FO_OUTPUT = "/home/vlabatut/Downloads/out_wp";
//		public final static String FO_OUTPUT = "/home/vlabatut/eclipse/workspaces/Extraction/Nerwip/out";

//		public final static String FO_OUTPUT = "D:/Users/Vincent/Documents/Dropbox/Nerwip2/out";
//		public final static String FO_OUTPUT = "D:/Users/Vincent/Documents/Dropbox/NetExtraction/Data2";
//		public final static String FO_OUTPUT = "D:/Users/Vincent/Documents/Dropbox/NetExtraction/Data";
//		public final static String FO_OUTPUT = "C:/Users/Vincent/Downloads/Data";
//		public final static String FO_OUTPUT = "D:/Users/Vincent/Downloads/Web/_ner/fr";
	
	/////////////////////////////////////////////////////////////////
	// FILES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////
	// EXTENSIONS	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Bash file extension */
	public final static String EX_BASH = ".bash";
	/** Binary file extension */
	public final static String EX_BIN = ".bin";
	/** Comma-separated values (CSV) file extension */
	public final static String EX_CSV = ".csv";
	/** PDF file extension */
	public final static String EX_PDF = ".pdf";
	/** PNG image format */
	public final static String EX_PNG = ".png";
	/** JPEG image format */
	public final static String EX_JPEG = ".jpeg";
	/** GIF image format */
	public final static String EX_GIF = ".gif";
	/** XML Schema file extension */
	public final static String EX_SCHEMA = ".xsd";
	/** Text file extension */
	public final static String EX_TEXT = ".txt";
	/** XML file extension */
	public final static String EX_XML = ".xml";
	/** HTML file extension */
	public final static String EX_HTML = ".html";
	/** Graphml file extension */
	public final static String EX_GRAPHML = ".graphml";
}
