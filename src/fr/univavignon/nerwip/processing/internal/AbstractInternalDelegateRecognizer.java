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
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractDelegateRecognizer;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
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
public abstract class AbstractInternalDelegateRecognizer<T> extends AbstractDelegateRecognizer
{	
	/**
	 * Builds a new internal recognizer,
	 * using the specified options.
	 * 
	 * @param recognizer
	 * 		Recognizer associated to this delegate.
	 * @param trim
	 * 		Whether or not the beginings and ends of mentions should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be ignored.
	 * @param exclusionOn
	 * 		Whether or not stop words should be ignored.
	 */
	public AbstractInternalDelegateRecognizer(InterfaceRecognizer recognizer, boolean trim, boolean ignorePronouns, boolean exclusionOn)
	{	super(recognizer,trim,ignorePronouns,exclusionOn);
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
	protected abstract void prepareRecognizer() throws ProcessorException;

    /**
     * Takes an object representation of the article, 
     * and returns the internal representation of
     * the detected mentions. Those must then
     * be converted to objects compatible
     * with the rest of Nerwip.
     * 
     * @param article
     * 		Article to process.
     * @return
     * 		Object representing the detected mentions.
     * 
     * @throws ProcessorException
     * 		Problem while applying the recognizer.
     */
	protected abstract T detectMentions(Article article) throws ProcessorException;

	@Override
	public Mentions delegateRecognize(Article article) throws ProcessorException
	{	ProcessorName recognizerName = recognizer.getName();
		logger.log("Start applying "+recognizerName+" to "+article.getFolderPath()+" ("+article.getUrl()+")");
		logger.increaseOffset();
		Mentions result = null;
		
		try
		{	// checks if the result file already exists
			File dataFile = getXmlFile(article);
			boolean processNeedeed = !dataFile.exists();
			
			// if needed, we process the text
			if(!recognizer.doesCache() || processNeedeed)
			{	// check language
				ArticleLanguage language = article.getLanguage();
				if(language==null)
					logger.log("WARNING: The article language is unknown >> it is possible this recognizer does not handle this language");
				else if(!canHandleLanguage(language))
					logger.log("WARNING: This recognizer does not handle the language of this article ("+language+")");
				
				// apply the recognizer
				logger.log("Detect the mentions");
				prepareRecognizer();
				T intRes = detectMentions(article);
				
				// possibly record mentions as they are outputted (useful for debug)
				if(recognizer.doesOutputRawResults())
				{	logger.log("Record raw "+recognizerName+" results");
					writeRawResults(article, intRes);
				}
				else
					logger.log("Raw results not recorded (option disabled)");
				
				// convert mentions to our internal representation
				logger.log("Convert mentions to internal representation");
				result = convert(article,intRes);
	
				// check if the mentions are consistent
				String text = article.getRawText();
				for(AbstractMention<?> mention: result.getMentions())
				{	if(!mention.checkText(article))
						logger.log("ERROR: mention text not consistant with text/position, '"+mention.getStringValue()+" vs. '"+text.substring(mention.getStartPos(),mention.getEndPos())+"'");
				}
				
				// possibly trim mentions (remove non-digit/letter chars at beginning/end)
				logger.log("Possibly clean mentions.");
				cleanMentions(result);
				
				// possibly filter stop-words and pronouns
				logger.log("Filter mentions (pronouns, stop-words, etc.)");
				filterNoise(result,language);
				
				// filter overlapping mentions
				logger.log("Filter overlapping mentions");
				filterRedundancy(result);
				
				// record mentions using our xml format
				logger.log("Record mentions using our XML format");
				writeXmlResults(article,result);
			}
			
			// if the results already exist, we fetch them
			else
			{	logger.log("Loading mentions from cached file");
				result = readXmlResults(article);
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
	
		int nbrEnt = result.getMentions().size();
		logger.log(recognizerName+" over ["+article.getName()+"], found "+nbrEnt+" mentions");
		logger.decreaseOffset();

		return result;
	}

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Converts the specified objects, used internally by the associated
	 * recognizer, into the mention list used internally by Nerwip.  
	 * 
	 * @param article
	 * 		Original article (might be usefull, in order to get the full text).
	 * @param data
	 * 		Data objects to process.
	 * @return
	 * 		List of mentions detected by the associated recognizer.
	 * 
	 * @throws ProcessorException
	 * 		Problem while performing the conversion.
	 */
	public abstract Mentions convert(Article article, T data) throws ProcessorException;

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
