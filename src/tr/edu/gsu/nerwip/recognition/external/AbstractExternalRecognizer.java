package tr.edu.gsu.nerwip.recognition.external;

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
import java.io.IOException;
import java.text.ParseException;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalRecognizer;

/**
 * This class is used to represent NER tools invocable 
 * externally only, i.e. through the system and not
 * from within Nerwip, unlike {@link AbstractInternalRecognizer} objects. 
 * 
 * @param <T>
 * 		Class of the converter associated to this recognizer.
 * 		 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public abstract class AbstractExternalRecognizer<T extends AbstractExternalConverter> extends AbstractRecognizer
{	
	/**
	 * Builds a new internal recognizer,
	 * using the specified default options.
	 * 
	 * @param trim
	 * 		Whether or not the beginings and ends of entities should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param exclusionOn
	 * 		Whether or not stop words should be ignored.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be ignored.
	 */
	public AbstractExternalRecognizer(boolean trim, boolean ignorePronouns, boolean exclusionOn)
	{	super(trim,ignorePronouns,exclusionOn);
	}
	
	/////////////////////////////////////////////////////////////////
	// CONVERTER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Converter associated to this NER tool */
	protected T converter;

	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Entities process(Article article) throws RecognizerException
	{	logger.log("Start applying "+getName()+" to "+article.getFolderPath()+" ("+article.getUrl()+")");
		logger.increaseOffset();
		Entities result = null;
		
		try
		{	// checks if the result file already exists
			File dataFile = converter.getXmlFile(article);
			boolean processNeedeed = !dataFile.exists();
			
			// if needed, we process the text
			if(!cache || processNeedeed)
			{	// check language
				ArticleLanguage language = article.getLanguage();
				if(language==null)
					logger.log("WARNING: The article language is unknown >> it is possible this NER tool does not handle this language");
				else if(!canHandleLanguage(language))
					logger.log("WARNING: This NER tool does not handle the language of this article ("+language+")");
				
				// apply the NER tool
				logger.log("Detect the entities");
				String text = article.getRawText();
				String entitiesStr = detectEntities(text);
				
				// convert entities to our internal representation
				logger.log("Convert entities to our internal representation");
				result = converter.convert(entitiesStr);
				
				// possibly trim entities (remove non-digit/letter chars at beginning/end)
				logger.log("Possibly clean entities.");
				cleanEntities(result);
				
				// possibly filter stop words and pronouns
				logger.log("Possibly filter entities (pronouns, stop-words, etc.)");
				filterNoise(result,language);
				
				// filter overlapping entities
				logger.log("Filter overlapping entities");
				filterRedundancy(result);
				
				// record entities using our xml format
				logger.log("Record entities using our XML format");
				converter.writeXmlResults(article,result);
				
				// possibly remove the raw output file
				if(outRawResults)
					logger.log("Keep the file procuded by the external NER tool");
				else
				{	logger.log("Delete the file procuded by the external NER tool");
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
			throw new RecognizerException(e.getMessage());
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
		catch (SAXException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
		catch (ParseException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
	
		logger.log(getName()+" over ["+article.getName()+"]");
		logger.decreaseOffset();

		return result;
	}
	
    /**
     * Takes the raw text of the article, and
     * returns a string representing the detected 
     * entities. Those must then be converted 
     * to objects compatible with the rest of Nerwip.
     * 
     * @param inputText
     * 		Raw text of the article.
     * @return
     * 		String representing the detected entities.
     * 
     * @throws RecognizerException
     * 		Problem while applying the NER tool.
    */
	protected abstract String detectEntities(String inputText) throws RecognizerException;
}
