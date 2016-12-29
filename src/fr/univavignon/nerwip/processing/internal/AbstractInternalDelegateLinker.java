package fr.univavignon.nerwip.processing.internal;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractDelegateLinker;
import fr.univavignon.nerwip.processing.InterfaceLinker;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.tools.file.FileTools;

/**
 * This class is used to represent or implement recognizers invocable 
 * internally, i.e. programmatically, from within Nerwip. 
 * 
  * @param <T>
 * 		Class of the internal representation of the mentions resulting from the detection.
 * 		 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public abstract class AbstractInternalDelegateLinker<T> extends AbstractDelegateLinker
{	
	/**
	 * Builds a new internal recognizer,
	 * using the specified options.
	 * 
	 * @param linker
	 * 		Linker associated to this delegate.
	 */
	public AbstractInternalDelegateLinker(InterfaceLinker linker)
	{	super(linker);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Possibly performs the last operations
	 * to make this recognizer ready to be applied.
	 * This method mainly concerns recognizers needing
	 * to load external data.
	 * 
	 * @throws ProcessorException
	 * 		Problem while initializing the recognizer.
	 */
	protected abstract void prepareLinker() throws ProcessorException;

    /**
     * Takes an object representation of the article,  and returns the internal representation of
     * the linked entities. Those must then  be converted to objects compatible with the rest of 
     * Nerwip.
     * <br/>
     * If the processor was initialied as a recognizer and resolver in addition to a linker, then
     * the {@code mentions} and {@code entities} should be empty. They will be initialized at the
     * same time as the entities will be linked.
     * 
     * @param article
     * 		Article to process.
	 * @param mentions
	 * 		List of the previously recognized mentions.
	 * @param entities
	 * 		List of the entities associated to the mentions.
    * @return
     * 		Object representing the linked entities.
     * 
     * @throws ProcessorException
     * 		Problem while applying the linker.
     */
	protected abstract T linkEntities(Article article, Mentions mentions, Entities entities) throws ProcessorException;

	@Override
	public void delegateLink(Article article, Mentions mentions, Entities entities) throws ProcessorException
	{	ProcessorName linkerName = linker.getName();
		logger.log("Start applying "+linkerName+" to "+article.getFolderPath()+" ("+article.getUrl()+")");
		logger.increaseOffset();
		
		try
		{	// checks if the result file already exists
			File mentionsFile = getMentionsXmlFile(article);
			File entitiesFile = getEntitiesXmlFile(article);
			boolean processNeedeed = !(mentionsFile.exists() && entitiesFile.exists());
			
			// if needed, we process the text
			if(!linker.doesCache() || processNeedeed)
			{	// check language
				ArticleLanguage language = article.getLanguage();
				if(language==null)
					logger.log("WARNING: The article language is unknown >> it is possible this linker does not handle this language");
				else if(!canHandleLanguage(language))
					logger.log("WARNING: This linker does not handle the language of this article ("+language+")");
				
				// apply the linker
				logger.log("Detect the mentions");
				prepareLinker();
				T intRes = linkEntities(article, mentions, entities);
				
				// possibly record entities as they are outputted (useful for debug)
				if(linker.doesOutputRawResults())
				{	logger.log("Record raw "+linkerName+" results");
					writeRawResults(article, intRes);
				}
				else
					logger.log("Raw results not recorded (option disabled)");
				
				// convert entities to our internal representation
				logger.log("Convert entities to internal representation");
				convert(article,mentions,entities,intRes);
	
				// record results using our xml format
				logger.log("Record mentions and entities using our XML format");
				writeXmlResults(article,mentions,entities);
			}
			
			// if the results already exist, we fetch them
			else
			{	logger.log("Loading mentions from cached file");
				readXmlResults(article,entities);
			}
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		catch (SAXException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		catch (ParseException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
	
		int nbrEnt = entities.getEntities().size();
		logger.log(linkerName+" over ["+article.getName()+"], processed "+nbrEnt+" entities");
		logger.decreaseOffset();
	}

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Converts the specified objects, used internally by the associated
	 * linker, into objects used internally by Nerwip.  
	 * 
	 * @param article
	 * 		Original article (might be usefull, in order to get the full text).
	 * @param data
	 * 		Data objects to process.
	 * @param mentions
	 * 		List of the previously recognized mentions.
	 * @param entities
	 * 		List of the entities associated to the mentions.
	 * 
	 * @throws ProcessorException
	 * 		Problem while performing the conversion.
	 */
	public abstract void convert(Article article, Mentions mentions, Entities entities, T data) throws ProcessorException;

	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Reads the file generated externally by the
	 * associated recognizer to store the detected mentions.
	 * 
	 * @param article 
	 * 		Concerned article.
	 * @return 
	 * 		String representation of the file content.
	 * 
	 * @throws FileNotFoundException 
	 * 		Problem while reading the file.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle the encoding.
	 */
	protected String readRawResults(Article article) throws FileNotFoundException, UnsupportedEncodingException
	{	File file = getRawFile(article);
	
		String result = FileTools.readTextFile(file, "UTF-8");
		return result;
	}

	/**
	 * Write the raw results obtained for the specified article.
	 * This method is meant for internal tools (those executed
	 * programmatically).
	 * 
	 * @param article
	 * 		Concerned article.
	 * @param results
	 * 		String representation of the recognizer result.		
	 * 
	 * @throws IOException 
	 * 		Problem while recording the file.
	 */
	protected void writeRawResultsStr(Article article, String results) throws IOException
	{	File file = getRawFile(article);
		File folder = file.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		FileTools.writeTextFile(file, results, "UTF-8");
	}

	/**
	 * Records the results of the recognition task
	 * in a text file, for archiving purposes.
	 * 
	 * @param article
	 * 		Concerned article.
	 * @param intRes
	 * 		Result of the mention detection, represented using the format internal to the recognizer.
	 * 
	 * @throws IOException
	 * 		Problem while writing the file.
	 */
	protected abstract void writeRawResults(Article article, T intRes) throws IOException;
}
