package fr.univavignon.nerwip.tools.corpus.archive;

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
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.ArticleList;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.external.AbstractExternalDelegateRecognizer;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.tools.corpus.ArticleLists;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

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
	
//		resetReferenceValues();
	
		updateReferenceFiles();
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
				else if(fileName.equals(folderName+"_new_reference"+FileNames.EX_TEXT))
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
	 * @throws IOException
	 * 		Problem while accessing a file.
	 * @throws ReaderException
	 * 		Problem while retrieving the article.
	 * @throws ParseException
	 * 		Problem while accessing a file.
	 * @throws SAXException
	 * 		Problem while accessing a file.
	 * @throws ProcessorException
	 * 		Problem while performing the conversion.
	 * 
	 * @deprecated
	 * 		Should not be used, not the corpus	
	 * 		has been converted to the new format.
	 */
	private static void convertReferences(AbstractExternalDelegateRecognizer converter) throws IOException, ReaderException, ParseException, SAXException, ProcessorException
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
			
			logger.log("Retrieve reference mentions");
			String refPath = folder.getPath() + File.separator + FileNames.FI_REFERENCE_TEXT;
			File refFile = new File(refPath);
			String references = FileTools.readTextFile(refFile, "UTF-8");
			Mentions mentions = converter.convert(null,references);

			logger.log("Record our XML format");
			String xmlPath = folder.getPath() + File.separator + FileNames.FI_MENTION_LIST;
			File xmlFile = new File(xmlPath);
			mentions.writeToXml(xmlFile);
			logger.decreaseOffset();
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Conversion over");
	}

	/**
	 * Removes the files produced during NER detection,
	 * as well as the reference mention file (but not the
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
			
			// remove mention file
			String mentionPath = article.getPath() + File.separator + FileNames.FI_MENTION_LIST;
			File mentionFile = new File(mentionPath);
			if(mentionFile.exists())
			{	logger.log("Removing "+mentionFile.getName());
				mentionFile.delete();
			}
			else
				logger.log("No mention file ("+mentionFile.getName()+") to remove");
			
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
	 * Removes values from the mentions in
	 * the reference files. Only used once.
	 * 
	 * @throws ParseException
	 * 		Problem while reading/writing mentions. 
	 * @throws IOException 
	 * 		Problem while reading/writing mentions. 
	 * @throws SAXException 
	 * 		Problem while reading/writing mentions. 
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
			
			logger.log("Retrieve reference mentions");
			String xmlPath = folder.getPath() + File.separator + FileNames.FI_MENTION_LIST;
			File file = new File(xmlPath);
			Mentions mentions = Mentions.readFromXml(file);
			
			logger.log("Remove their value (keep the string representation, though)");
			for(AbstractMention<?> mention: mentions.getMentions())
				mention.setValue(null);
			
			logger.log("Record the modified mentions");
			mentions.writeToXml(file);
			logger.decreaseOffset();
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Conversion over");
	}
	
	/**
	 * Fix the dates from the reference files in the
	 * old corpus.
	 * 
	 * @throws SAXException
	 * 		Problem while reading/writing mentions. 
	 * @throws IOException
	 * 		Problem while reading/writing mentions. 
	 * @throws ParseException
	 * 		Problem while reading/writing mentions. 
	 */
	private static void updateReferenceFiles() throws SAXException, IOException, ParseException
	{	File corpusFolder = new File("C:/Users/Vincent/Documents/Dropbox/Nerwip2/out");
		ArticleList list = ArticleLists.getArticleList(corpusFolder);
		boolean flag = false;
		for(File folder: list)
		{	String name = folder.getName();
			if(name.equals("Ozias_Leduc"))
				flag = true;
			else if(flag)
			{	String xmlPath = folder.getPath() + File.separator + FileNames.FI_MENTION_LIST;
				File file = new File(xmlPath);
				Mentions mentions = Mentions.readFromXml(file);
				mentions.setModificationDate(mentions.getCreationDate());
				mentions.writeToXml(file);
			}
		}
	}
}
