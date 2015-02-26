package tr.edu.gsu.nerwip.recognition.internal;

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
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerException;

/**
 * This class is used to represent or implement NER tools invocable 
 * internally, i.e. programmatically, from within Nerwip. 
 * 
 * @param <T>
 * 		Class of the converter associated to this recognizer.
 * @param <U>
 * 		Class of the internal representation of the entities resulting from the detection.
 * 		 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public abstract class AbstractInternalRecognizer<U,T extends AbstractInternalConverter<U>> extends AbstractRecognizer
{	
	/**
	 * Builds a new internal recognizer,
	 * using the specified default options.
	 * 
	 * @param trim
	 * 		Whether or not the beginings and ends of entities should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be ignored.
	 * @param exclusionOn
	 * 		Whether or not stop words should be ignored.
	 */
	public AbstractInternalRecognizer(boolean trim, boolean ignorePronouns, boolean exclusionOn)
	{	super(trim,ignorePronouns,exclusionOn);
	}
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Possibly performs the last operations
	 * to make this recognizer ready to be applied.
	 * This method mainly concerns recognizers needing
	 * to load external data.
	 * 
	 * @throws RecognizerException
	 * 		Problem while initializing the recognizer.
	 */
	protected abstract void prepareRecognizer() throws RecognizerException;
	
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
			{	// apply the NER tool
				logger.log("Detect the entities");
				prepareRecognizer();
				U intRes = detectEntities(article);
				
				// possibly record entities as they are outputted (useful for debug)
				if(outRawResults)
				{	logger.log("Record raw "+getName()+" results");
					converter.writeRawResults(article, intRes);
				}
				else
					logger.log("Raw results not recorded (option disabled)");
				
				// convert entities to our internal representation
				logger.log("Convert entities to internal representation");
				result = converter.convert(article,intRes);
	
				// check if the entity is consistent
				String text = article.getRawText();
				for(AbstractEntity<?> entity: result.getEntities())
				{	if(!entity.checkText(article))
						logger.log("ERROR: entity text not consistant with text/position, '"+entity.getStringValue()+" vs. '"+text.substring(entity.getStartPos(),entity.getEndPos())+"'");
				}
				
				// possibly trim entities (remove non-digit/letter chars at beginning/end)
				logger.log("Possibly clean entities.");
				cleanEntities(result);
				
				// possibly filter stop words and pronouns
				logger.log("Filter entities (pronouns, stop-words, etc.)");
				filterNoise(result);
				
				// filter overlapping entities
				logger.log("Filter overlapping entities");
				filterRedundancy(result);
				
				// record entities using our xml format
				logger.log("Convert entities to our XML format");
				converter.writeXmlResults(article,result);
			}
			
			// if the results already exist, we fetch them
			else
			{	logger.log("Loading entities from cached file");
				result = converter.readXmlResults(article);
			}
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
		catch (ConverterException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
	
		int nbrEnt = result.getEntities().size();
		logger.log(getName()+" over ["+article.getName()+"], found "+nbrEnt+" entities");
		logger.decreaseOffset();

		return result;
	}
	
    /**
     * Takes an object representation of the article, 
     * and returns the internal representation of
     * the detected entities. Those must then
     * be converted to objects compatible
     * with the rest of Nerwip.
     * 
     * @param article
     * 		Article to process.
     * @return
     * 		Object representing the detected entities.
     * 
     * @throws RecognizerException
     * 		Problem while applying the NER tool.
     */
	protected abstract U detectEntities(Article article) throws RecognizerException;
}
