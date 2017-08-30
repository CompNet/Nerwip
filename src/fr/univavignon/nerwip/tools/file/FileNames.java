package fr.univavignon.nerwip.tools.file;

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

/**
 * This class contains various constants
 * related to file and folder names.
 *  
 * @author Vincent Labatut
 */
public class FileNames
{	
	/////////////////////////////////////////////////////////////////
	// PREFIXES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Prefix for the lists of stop-words */
	public final static String PRE_EXCLUDED = "excluded-";
	/** Prefix for the lists of pronouns */
	public final static String PRE_PRONOUNS = "pronouns-";
	
	/////////////////////////////////////////////////////////////////
	// FOLDERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Log folder */
	public final static String FO_LOG = "log";
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
	
	/** Resources folder */
	public final static String FO_RESOURCES = "res";
		/** Folder used to store certain cached files */
		public final static String FO_CACHE = FO_RESOURCES + File.separator + "cache";
			/** Folder used to store Freebase cached files */
			public final static String FO_CACHE_FREEBASE = FO_CACHE + File.separator + "freebase";
			/** Folder used to store WikiMedia cached files */
			public final static String FO_CACHE_WIKIMEDIA = FO_CACHE + File.separator + "wikimedia";
		/** Folder used to store images */
		public final static String FO_IMAGES = FO_RESOURCES + File.separator + "images";
		/** Folder used to store various data */
		public final static String FO_MISC = FO_RESOURCES + File.separator + "misc";
		/** Language XML files for the GUI */
		public final static String FO_LANGUAGE = FO_RESOURCES + File.separator + "language";
		/** Ner-related resources */
		public final static String FO_NER = FO_RESOURCES + File.separator + "ner";
			/** Folder of custom resources */
			public final static String FO_CUSTOM = FO_NER + File.separator + "custom";
				/** Folder of custom lists */
				public final static String FO_CUSTOM_LISTS = FO_CUSTOM + File.separator + "lists";
			/** Folder of HeidelTime resources */
			public final static String FO_HEIDELTIME = FO_NER + File.separator + "heideltime";
			/** Folder of the Illinois tagger resources */
			public final static String FO_ILLINOIS = FO_NER + File.separator + "illinois";
				/** Folder of the Illinois tagger config files */
				public final static String FO_ILLINOIS_CONFIGS = FO_ILLINOIS + File.separator + "configs";
				/** Folder of the Illinois tagger models */
				public final static String FO_ILLINOIS_MODELS = FO_ILLINOIS + File.separator + "models";
			/** Folder of LingPipe resources */
			public final static String FO_LINGPIPE = FO_NER + File.separator + "lingpipe";
			/** Folder of Nero resources */
			public final static String FO_NERO = FO_NER + File.separator + "nero";
				/** Folder of Nero scripts*/
				public final static String FO_NERO_SCRIPTS = FO_NERO + File.separator + "scripts";
			/** Folder of OpenNLP resources */
			public final static String FO_OPENNLP = FO_NER + File.separator + "opennlp";
			/** Folder of Stanford resources */
			public final static String FO_STANFORD = FO_NER + File.separator + "stanford";
				/** Folder of Stanford models */
				public final static String FO_STANFORD_MODELS = FO_STANFORD + File.separator + "models";
				/** Folder of Stanford clusters */
				public final static String FO_STANFORD_CLUSTERS = FO_STANFORD + File.separator + "clusters";
			/** Folder of Subee resources */
			public final static String FO_SUBEE = FO_NER + File.separator + "subee";
			/** Folder of SVM combiner resources */
			public final static String FO_SVMCOMBINER = FO_NER + File.separator + "svmcombiner";
			/** Folder of TagEN resources */
			public final static String FO_TAGEN = FO_NER + File.separator + "tagen";
			/** Folder of vote combiner resources */
			public final static String FO_VOTECOMBINER = FO_NER + File.separator + "votecombiner";
		/** Folder containing retrieval-related data */
		public final static String FO_RETRIEVAL = FO_RESOURCES + File.separator + "retrieval";
		/** Folder containing the XML schemas */
		public final static String FO_SCHEMA = FO_RESOURCES + File.separator + "schemas";
	
	/////////////////////////////////////////////////////////////////
	// FILES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** HTML file containing the "index" text */
	public final static String FI_HELP_PAGE = "help" + FileNames.EX_HTML;
	/** XML schema file used to store category maps */
	public final static String FI_CATMAP_SCHEMA = "categorymaps" + FileNames.EX_SCHEMA;
	/** XML schema file used for graphml  */
	public final static String FI_GRAPHML_SCHEMA = "graphml" + FileNames.EX_SCHEMA;
	/** XML schema file used to record mentions  */
	public final static String FI_MENTION_SCHEMA = "mentions" + FileNames.EX_SCHEMA;
	/** XML schema file used to record entities  */
	public final static String FI_ENTITY_SCHEMA = "entities" + FileNames.EX_SCHEMA;
	/** XML schema file used to store keys */
	public final static String FI_KEY_SCHEMA = "keys" + FileNames.EX_SCHEMA;
	/** XML file used to store keys */
	public final static String FI_KB_NAMES = "kb_names" + FileNames.EX_TEXT;
	/** XML schema file used to record article properties  */
	public final static String FI_PROPERTY_SCHEMA = "properties" + FileNames.EX_SCHEMA;
	/** File containing the properties of the article */
	public final static String FI_PROPERTIES = "properties" + FileNames.EX_XML;
	/** File containing original page */
	public final static String FI_ORIGINAL_PAGE = "original" + FileNames.EX_HTML;
	/** File containing the raw text */
	public final static String FI_RAW_TEXT = "raw" + FileNames.EX_TEXT;
	/** File containing the text with hyperlinks */
	public final static String FI_LINKED_TEXT = "linked" + FileNames.EX_TEXT;
	/** File containing the reference mentions */
	public final static String FI_REFERENCE_TEXT = "reference" + FileNames.EX_TEXT;
	/** XML file containing the mentions estimated by a recognizer or completed by a resolver, in a normalized format */
	public final static String FI_MENTION_LIST = "mentions" + FileNames.EX_XML;
	/** XML file containing the entities detected by a resolver or linked by a linker, in a normalized format */
	public final static String FI_ENTITY_LIST = "entities" + FileNames.EX_XML;
	/** XML schema file used to store GUI texts */
	public final static String FI_LANGUAGE = "language" + FileNames.EX_SCHEMA;
	/** XML file used to store keys */
	public final static String FI_KEY_LIST = "keys" + FileNames.EX_XML;
	/** File output by a processor, using its own format */
	public final static String FI_OUTPUT_TEXT = "output" + FileNames.EX_TEXT;
	/** File containing some statistics processed on the corpus */
	public final static String FI_STATS_TEXT = "stats" + FileNames.EX_TEXT;
	/** File used to cache all types retrieved from Freebase */
	public final static String FI_ALL_TYPES = "types.all" + FileNames.EX_TEXT;
	/** File used to cache notable types retrieved from Freebase */
	public final static String FI_NOTABLE_TYPES = "types.notable" + FileNames.EX_TEXT;
	/** File used to cache the mapping between Wikipedia article titles and Freebase ids  */
	public final static String FI_IDS = "ids" + FileNames.EX_TEXT;
	/** File used to cache the mapping queries and their results in a cache */
	public final static String FI_QUERIES = "queries" + FileNames.EX_TEXT;
	/** File used to list the unknown Freebase types */
	public final static String FI_UNKNOWN_TYPES = "fb.unknown" + FileNames.EX_TEXT;
	/** List of location-related adjectives */
	public final static String FI_DEMONYMS = "demonyms" + FileNames.EX_TEXT;
	/** Configuration file */
	public final static String FI_CONFIGURATION = "config" + FileNames.EX_XML;
	/** Configuration schema */
	public final static String FI_CONFIGURATION_SCHEMA = "edconfig" + FileNames.EX_SCHEMA;
	/** Main script of the Nero tool */
	public final static String FI_NERO_BASH = "irisa_ne" + FileNames.EX_BASH;
	/** Main program of the TagEn tool */
	public final static String FI_TAGEN_EXE = "tagen";
	/** File used to cache the mapping queries and their results in a cache */
	public final static String FI_WIKIDATA = "wikidata" + FileNames.EX_TEXT;
	/** File used to cache the mapping queries and their results in a cache */
	public final static String FI_WIKIMEDIA = "wikimedia" + FileNames.EX_TEXT;
	
	/** Application icon */
	public final static String FI_ICON_APP = "icon" + FileNames.EX_PNG;
	/** Icon for add */
	public final static String FI_ICON_ADD = "plus" + FileNames.EX_PNG;
	/** Icon for remove */
	public final static String FI_ICON_REMOVE = "remove" + FileNames.EX_PNG;
	/** Icon for show */
	public final static String FI_ICON_SHOW = "view" + FileNames.EX_PNG;
	/** Icon for next */
	public final static String FI_ICON_NEXT = "next" + FileNames.EX_PNG;
	/** Icon for previous */
	public final static String FI_ICON_PREVIOUS = "previous" + FileNames.EX_PNG;
	/** Icon for larger font */
	public final static String FI_ICON_LARGER = "larger" + FileNames.EX_PNG;
	/** Icon for smaller font */
	public final static String FI_ICON_SMALLER = "smaller" + FileNames.EX_PNG;
	/** Icon for saving file */
	public final static String FI_ICON_SAVE = "disk" + FileNames.EX_PNG;
	/** Icon for opening file */
	public final static String FI_ICON_OPEN = "folder" + FileNames.EX_PNG;
	/** Icon for left shift */
	public final static String FI_ICON_LEFT = "left" + FileNames.EX_PNG;
	/** Icon for right shift */
	public final static String FI_ICON_RIGHT = "right" + FileNames.EX_PNG;
	
	/** Laboratory logo */
	public final static String FI_LOGO_LAB = "lia" + FileNames.EX_JPEG;
	/** University logo */
	public final static String FI_LOGO_UNIV = "uapv" + FileNames.EX_GIF;
	
	
//	/**
//	 * Returns the filename used to store the
//	 * specified statistic.
//	 * 
//	 * @param stat
//	 * 		Name of the statistic.
//	 * @return
//	 * 		Associated filename.
//	 */
//	public static String getStatFilename(String stat)
//	{	String result = FI_STATS_TEXT + stat + EX_TEXT;
//		return result;
//	}
	
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
