package fr.univavignon.nerwip.processing.external;

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
import java.io.IOException;
import java.text.ParseException;

import org.xml.sax.SAXException;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractDelegateRecognizer;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;

/**
 * This class is used to represent recognizers invocable 
 * externally only, i.e. through the system and not
 * from within Nerwip, unlike {@link AbstractExternalDelegateRecognizer} objects. 
 * 		 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public abstract class AbstractExternalDelegateRecognizer extends AbstractDelegateRecognizer
{	
	/**
	 * Builds a new delegate recognizer,
	 * using the specified default options.
	 * 
	 * @param recognizer
	 * 		Recognizer associated to this delegate.
	 * @param trim
	 * 		Whether or not the beginings and ends of mentions should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be ignored.
	 * @param ignoreNumbers
	 * 		Whether or not numbers should be ignored.
	 * @param exclusionOn
	 * 		Whether or not stop words should be ignored.
	 */
	public AbstractExternalDelegateRecognizer(InterfaceRecognizer recognizer, boolean trim, boolean ignorePronouns, boolean ignoreNumbers, boolean exclusionOn)
	{	super(recognizer,trim,ignorePronouns,ignoreNumbers,exclusionOn);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
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
				String mentionsStr = detectMentions(article);
				
				// convert mentions to our internal representation
				logger.log("Convert mentions to our internal representation");
				result = convert(article,mentionsStr);
				
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
				writeXmlResults(article,result);
				
				// possibly remove the raw output file
				if(recognizer.doesOutputRawResults())
					logger.log("Keep the file produced by the external recognizer");
				else
				{	logger.log("Delete the file produced by the external recognizer");
					deleteRawFile(article);
				}
			}
			
			// if the results already exist, we fetch them
			else
			{	result = readXmlResults(article);
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
	
		logger.log(recognizerName+" over ["+article.getName()+"]");
		logger.decreaseOffset();

		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Convert the file (supposedly) externally generated by the recognizer,
	 * into the mention list used internally by Nerwip. The file to be
	 * converted is passed as a string. 
	 * 
	 * @param article
	 * 		The considered article.
	 * @param mentionsStr
	 * 		String representation of the detected mentions.
	 * @return
	 * 		List of mentions detected by the associated recognizer.
	 * 
	 * @throws ProcessorException
	 * 		Problem while performing the conversion.
	 */
	public abstract Mentions convert(Article article, String mentionsStr) throws ProcessorException;
}
