package tr.edu.gsu.nerwip.tools.file;

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
//	public final static String FO_OUTPUT = "C:/Users/Vincent/Documents/Dropbox/Nerwip2/out";
	public final static String FO_OUTPUT = "C:/Users/Vincent/Documents/Dropbox/NetExtraction/Data2";
//	public final static String FO_OUTPUT = "C:/Temp";
//	public final static String FO_OUTPUT = "/home/vlabatut/Dropbox/Nerwip2/out";
//	public final static String FO_OUTPUT = "/home/vlabatut/Nerwip2/out/Temp";
	/** Resources folder */
	public final static String FO_RESOURCES = "res";
		/** Folder used to store certain cached files */
		public final static String FO_CACHE = FO_RESOURCES + File.separator + "cache";
			/** Folder used to store Freebase cached files */
			public final static String FO_CACHE_FREEBASE = FO_CACHE + File.separator + "freebase";
		/** Folder used to store various data */
		public final static String FO_MISC = FO_RESOURCES + File.separator + "misc";
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
	public final static String FI_HELP_PAGE = "help" + FileNames.EX_WEB;
	/** XML schema file used to store category maps */
	public final static String FI_CATMAP_SCHEMA = "categorymaps" + FileNames.EX_SCHEMA;
	/** XML schema file used to record entities  */
	public final static String FI_ENTITY_SCHEMA = "entities" + FileNames.EX_SCHEMA;
	/** XML schema file used to store keys */
	public final static String FI_KEY_SCHEMA = "keys" + FileNames.EX_SCHEMA;
	/** XML schema file used to record article properties  */
	public final static String FI_PROPERTY_SCHEMA = "properties" + FileNames.EX_SCHEMA;
	/** File containing the properties of the article */
	public final static String FI_PROPERTIES = "properties" + FileNames.EX_XML;
	/** File containing original page */
	public final static String FI_ORIGINAL_PAGE = "original" + FileNames.EX_WEB;
	/** File containing the raw text */
	public final static String FI_RAW_TEXT = "raw" + FileNames.EX_TEXT;
	/** File containing the text with hyperlinks */
	public final static String FI_LINKED_TEXT = "linked" + FileNames.EX_TEXT;
	/** File containing the reference entities */
	public final static String FI_REFERENCE_TEXT = "reference" + FileNames.EX_TEXT;
	/** File containing the entities estimated by a NER tool, in a normalized format */
	public final static String FI_ENTITY_LIST = "entities" + FileNames.EX_XML;
	/** XML schema file used to store keys */
	public final static String FI_KEY_LIST = "keys" + FileNames.EX_XML;
	/** File containing the entities estimated by a NER tool, in a tool-specific format */
	public final static String FI_OUTPUT_TEXT = "output" + FileNames.EX_TEXT;
	/** File containing some statistics processed on the corpus */
	public final static String FI_STATS_TEXT = "stats" + FileNames.EX_TEXT;
	/** File used to cache all types retrieved from Freebase */
	public final static String FI_ALL_TYPES = "types.all" + FileNames.EX_TEXT;
	/** File used to cache notable types retrieved from Freebase */
	public final static String FI_NOTABLE_TYPES = "types.notable" + FileNames.EX_TEXT;
	/** File used to cache the mapping between Wikipedia article titles and Freebase ids  */
	public final static String FI_IDS = "ids" + FileNames.EX_TEXT;
	/** File used to list the unknown Freebase types */
	public final static String FI_UNKNOWN_TYPES = "fb.unknown" + FileNames.EX_TEXT;
	/** List of location-related adjectives */
	public final static String FI_DEMONYMS = "demonyms" + FileNames.EX_TEXT;
	
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
	/** PDF file extension */
	public final static String EX_PDF = ".pdf";
	/** XML Schema file extension */
	public final static String EX_SCHEMA = ".xsd";
	/** Text file extension */
	public final static String EX_TEXT = ".txt";
	/** XML file extension */
	public final static String EX_XML = ".xml";
	/** HTML file extension */
	public final static String EX_WEB = ".html";
}
