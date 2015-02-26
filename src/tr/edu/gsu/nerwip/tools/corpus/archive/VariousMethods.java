package tr.edu.gsu.nerwip.tools.corpus.archive;

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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.evaluation.ArticleList;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalConverter;
import tr.edu.gsu.nerwip.retrieval.ArticleRetriever;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.tools.corpus.ArticleLists;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.StringTools;

/**
 * This class contains various methods
 * used on some previous forms of the corpus.
 * They should not be used anymore now.
 *  
 *  @deprecated
 *  	These methods are kept as archives, to
 *  	ease the writing of new ones. They should
 *  	not be executed directly as is, anymore.
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class VariousMethods
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
	{	logger.setName("Misc-Preprocessing");
		
//		convertCorpusFileNames();
	
//		resetCorpus();
	
//		convertReferences(new SmaConversion());
	
		resetReferenceValues();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Changes the names of the files in the corpus,
	 * in order to get the expected, convenient, explicit 
	 * file names.
	 * 
	 * @deprecated
	 * 		Should not be used anymore, now that the
	 * 		corpus has been converted to a new form.
	 */
	private static void convertCorpusFileNames()
	{	File root = new File(FileNames.FO_OUTPUT);
		File folders[] = root.listFiles();
		for(File folder: folders)
		{	String folderName = folder.getName();
			String folderPath = folder.getPath();
			logger.log("Processing folder '"+folderName+"'");
			logger.increaseOffset();
			
			File files[] = folder.listFiles();
			for(File file: files)
			{	String fileName = file.getName();
				String newName = null;
				if(fileName.equals(folderName))
					newName = folderPath + File.separator + FileNames.FI_RAW_TEXT;
				else if(fileName.equals(folderName+"_new_reference"+FileNames.EX_TXT))
					newName = folderPath + File.separator + FileNames.FI_REFERENCE_TEXT;
				else if(fileName.equals(folderName+"(withLinks)"))
					newName = folderPath + File.separator + FileNames.FI_LINKED_TEXT;

				else if(fileName.equals(folderName+"(reference)"))
				{	logger.log("Removing file '"+fileName+"'");
					file.delete();
				}
				else
				{	logger.log("WARNING: file not expected '"+fileName+"' >> deleted");
					file.delete();
				}
			
				if(newName!=null)
				{	logger.log("Renaming file '"+fileName+"' to '"+newName+"'");
					File newFile = new File(newName);
					file.renameTo(newFile);
				}
			}
			logger.decreaseOffset();
		}
	}
	
	/**
	 * Converts all reference files in the corpus to
	 * our own XML format.
	 * 
	 * @param converter
	 * 		Object used for conversion (depends on the original 
	 * 		reference format, eg. StanfordOld Manual Annotation Tool)
	 * 
	 * @throws ConverterException
	 * 		Problem while performing the conversion.
	 * @throws IOException
	 * 		Problem while accessing a file.
	 * @throws ReaderException
	 * 		Problem while retrieving the article.
	 * @throws ParseException
	 * 		Problem while accessing a file.
	 * @throws SAXException
	 * 		Problem while accessing a file.
	 * 
	 * @deprecated
	 * 		Should not be used, not the corpus	
	 * 		has been converted to the new format.
	 */
	private static void convertReferences(AbstractExternalConverter converter) throws ConverterException, IOException, ReaderException, ParseException, SAXException
	{	logger.log("Converting reference files to our own XML format");
		logger.increaseOffset();
		
		List<File> folders = ArticleLists.getArticleList();
		logger.log("Get the list of articles: "+folders.size());
		
		
		logger.log("Process each article separately");
		logger.increaseOffset();
		for(File folder: folders)
		{	String name = folder.getName();
			logger.log("Process article "+name);
			logger.increaseOffset();
			
			logger.log("Retrieve reference entities");
			String refPath = folder.getPath() + File.separator + FileNames.FI_REFERENCE_TEXT;
			File refFile = new File(refPath);
			String references = FileTools.readTextFile(refFile);
			Entities entities = converter.convert(references);

			logger.log("Record our XML format");
			String xmlPath = folder.getPath() + File.separator + FileNames.FI_ENTITY_LIST;
			File xmlFile = new File(xmlPath);
			entities.writeToXml(xmlFile);
			logger.decreaseOffset();
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Conversion over");
	}

	/**
	 * Removes the files produced during NER detection,
	 * as well as the reference entity file (but not the
	 * raw text, linked text and annotated text).
	 * 
	 * @deprecated
	 * 		Should not be used anymore, this was for
	 * 		the previous version of the corpus, based on a different
	 * 		file annotation format.
	 */
	private static void resetCorpus()
	{	logger.log("Start corpus reset");
		logger.increaseOffset();
		
		// get article list
		List<File> articles = ArticleLists.getArticleList();
		
		// clean each article
		logger.log("Cleaning each article");
		logger.increaseOffset();
		for(File article: articles)
		{	logger.log("Processing article "+article.getName());
			logger.increaseOffset();
			
			// remove entity file
			String entityPath = article.getPath() + File.separator + FileNames.FI_ENTITY_LIST;
			File entityFile = new File(entityPath);
			if(entityFile.exists())
			{	logger.log("Removing "+entityFile.getName());
				entityFile.delete();
			}
			else
				logger.log("No entity file ("+entityFile.getName()+") to remove");
			
			// remove subfolders
			File subfolders[] = article.listFiles(FileTools.FILTER_DIRECTORY);
			for(File subfolder: subfolders)
			{	logger.log("Removing "+subfolder.getName());
				FileTools.delete(subfolder);
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Corpus reset over");
	}
	
	/**
	 * Removes values from the entities in
	 * the reference files. Only used once.
	 * 
	 * @throws ParseException
	 * 		Problem while reading/writing entities. 
	 * @throws IOException 
	 * 		Problem while reading/writing entities. 
	 * @throws SAXException 
	 * 		Problem while reading/writing entities. 
	 * 
	 * @deprecated
	 * 		Only used once, for cleaning purposes.
	 */
	private static void resetReferenceValues() throws SAXException, IOException, ParseException
	{	logger.log("Resetting reference values");
		logger.increaseOffset();
		
		List<File> folders = ArticleLists.getArticleList();
		logger.log("Get the list of articles: "+folders.size());
		
		
		logger.log("Process each article separately");
		logger.increaseOffset();
		for(File folder: folders)
		{	String name = folder.getName();
			logger.log("Process article "+name);
			logger.increaseOffset();
			
			logger.log("Retrieve reference entities");
			String xmlPath = folder.getPath() + File.separator + FileNames.FI_ENTITY_LIST;
			File file = new File(xmlPath);
			Entities entities = Entities.readFromXml(file);
			
			logger.log("Remove their value (keep the string representation, though)");
			for(AbstractEntity<?> entity: entities.getEntities())
				entity.setValue(null);
			
			logger.log("Record the modified entities");
			entities.writeToXml(file);
			logger.decreaseOffset();
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Conversion over");
	}
}
