package fr.univavignon.nerwip.recognition.external;

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
import java.io.IOException;
import java.text.ParseException;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.recognition.AbstractProcessor;
import fr.univavignon.nerwip.recognition.ConverterException;
import fr.univavignon.nerwip.recognition.ProcessorException;
import fr.univavignon.nerwip.recognition.internal.AbstractInternalProcessor;

/**
 * This class is used to represent recognizers invocable 
 * externally only, i.e. through the system and not
 * from within Nerwip, unlike {@link AbstractInternalProcessor} objects. 
 * 
 * @param <T>
 * 		Class of the converter associated to this recognizer.
 * 		 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public abstract class AbstractExternalProcessor<T extends AbstractExternalConverter> extends AbstractProcessor
{	
	/**
	 * Builds a new internal recognizer,
	 * using the specified default options.
	 * 
	 * @param trim
	 * 		Whether or not the beginings and ends of mentions should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param exclusionOn
	 * 		Whether or not stop words should be ignored.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be ignored.
	 */
	public AbstractExternalProcessor(boolean trim, boolean ignorePronouns, boolean exclusionOn)
	{	super(trim,ignorePronouns,exclusionOn);
	}
	
	/////////////////////////////////////////////////////////////////
	// CONVERTER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Converter associated to this recognizer */
	protected T converter;

	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions recognize(Article article) throws ProcessorException
	{	logger.log("Start applying "+getName()+" to "+article.getFolderPath()+" ("+article.getUrl()+")");
		logger.increaseOffset();
		Mentions result = null;
		
		try
		{	// checks if the result file already exists
			File dataFile = converter.getXmlFile(article);
			boolean processNeedeed = !dataFile.exists();
			
			// if needed, we process the text
			if(!cache || processNeedeed)
			{	// check language
				ArticleLanguage language = article.getLanguage();
				if(language==null)
					logger.log("WARNING: The article language is unknown >> it is possible this recognizer does not handle this language");
				else if(!canHandleLanguage(language))
					logger.log("WARNING: This recognizer does not handle the language of this article ("+language+")");
				
				// apply the recognizer
				logger.log("Detect the mentions");
				String mentionsStr = detectMentions(article);
				
				// convert mentions to our internal representation
				logger.log("Convert mentions to our internal representation");
				result = converter.convert(article,mentionsStr);
				
				// possibly trim mentions (remove non-digit/letter chars at beginning/end)
				logger.log("Possibly clean mentions.");
				cleanMentions(result);
				
				// possibly filter stop words and pronouns
				logger.log("Possibly filter mentions (pronouns, stop-words, etc.)");
				filterNoise(result,language);
				
				// filter overlapping mentions
				logger.log("Filter overlapping mentions");
				filterRedundancy(result);
				
				// record mentions using our xml format
				logger.log("Record mentions using our XML format");
				converter.writeXmlResults(article,result);
				
				// possibly remove the raw output file
				if(outRawResults)
					logger.log("Keep the file produced by the external recognizer");
				else
				{	logger.log("Delete the file produced by the external recognizer");
					converter.deleteRawFile(article);
				}
			}
			
			// if the results already exist, we fetch them
			else
			{	result = converter.readXmlResults(article);
			}
		}
		catch (ConverterException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
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
	
		logger.log(getName()+" over ["+article.getName()+"]");
		logger.decreaseOffset();

		return result;
	}
	
    /**
     * Takes the raw text of the article, and
     * returns a string representing the detected 
     * mentions. Those must then be converted 
     * to objects compatible with the rest of Nerwip.
     * 
     * @param article
     * 		Article to process.
     * @return
     * 		String representing the detected mentions.
     * 
     * @throws ProcessorException
     * 		Problem while applying the recognizer.
    */
	protected abstract String detectMentions(Article article) throws ProcessorException;
}
