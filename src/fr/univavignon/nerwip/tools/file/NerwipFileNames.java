package fr.univavignon.nerwip.tools.file;

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
 * This class contains various constants
 * related to file and folder names.
 *  
 * @author Vincent Labatut
 */
public class NerwipFileNames
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
	/** Folder used to store WikiMedia cached files */
	public final static String FO_CACHE_WIKIMEDIA = FileNames.FO_CACHE + File.separator + "wikimedia";

	/** Ner-related resources */
	public final static String FO_NER = FileNames.FO_RESOURCES + File.separator + "ner";
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
	
	/////////////////////////////////////////////////////////////////
	// FILES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** File output by a processor, using its own format */
	public final static String FI_OUTPUT_TEXT = "output" + FileNames.EX_TEXT;
}
