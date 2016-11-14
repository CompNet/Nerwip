package tr.edu.gsu.nerwip.linking.internal;

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
import tr.edu.gsu.nerwip.data.entity.mention.AbstractMention;
import tr.edu.gsu.nerwip.data.entity.mention.Mentions;
import tr.edu.gsu.nerwip.linking.AbstractLinker;
import tr.edu.gsu.nerwip.linking.LinkerException;
import tr.edu.gsu.nerwip.recognition.ConverterException;

/**
 * This class is used to represent or implement linkers invocable 
 * internally, i.e. programmatically, from within Nerwip. 
 * 
 * @param <T>
 * 		Class of the converter associated to this linker.
 * @param <U>
 * 		Class of the internal representation of the mentions resulting from the detection.
 * 		 
 * @author Vincent Labatut
 */
public abstract class AbstractInternalLinker<U,T extends AbstractInternalConverter> extends AbstractLinker
{	
	/**
	 * Builds a new internal linker.
	 */
	public AbstractInternalLinker()
	{	super();
	}
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Possibly performs the last operations
	 * to make this linker ready to be applied.
	 * This method mainly concerns linkers needing
	 * to load external data.
	 * 
	 * @throws LinkerException
	 * 		Problem while initializing the linker.
	 */
	protected abstract void prepareLinker() throws LinkerException;
	
	/////////////////////////////////////////////////////////////////
	// CONVERTER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Converter associated to this linker */
	protected T converter;
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void process(Article article, Mentions mentions, Entities entities) throws LinkerException
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
					logger.log("WARNING: The article language is unknown >> it is possible this linker does not handle this language");
				else if(!canHandleLanguage(language))
					logger.log("WARNING: This linker does not handle the language of this article ("+language+")");
				
				// apply the linker
				logger.log("Link the entities");
				prepareLinker();
				String intRes = linkEntities(article, mentions, entities);
				
				// possibly record mentions as they are outputted (useful for debug)
				if(outRawResults)
				{	logger.log("Record raw "+getName()+" results");
					converter.writeRawResults(article, intRes);
				}
				else
					logger.log("Raw results not recorded (option disabled)");
				
				// convert mentions to our internal representation
				logger.log("Convert mentions to internal representation");
				result = converter.convert(article,intRes);
	
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
				logger.log("Convert mentions to our XML format");
				converter.writeXmlResults(article,result);
			}
			
			// if the results already exist, we fetch them
			else
			{	logger.log("Loading mentions from cached file");
				result = converter.readXmlResults(article);
			}
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new LinkerException(e.getMessage());
		}
		catch (SAXException e)
		{	e.printStackTrace();
			throw new LinkerException(e.getMessage());
		}
		catch (ParseException e)
		{	e.printStackTrace();
			throw new LinkerException(e.getMessage());
		}
		catch (ConverterException e)
		{	e.printStackTrace();
			throw new LinkerException(e.getMessage());
		}
	
		int nbrEnt = result.getMentions().size();
		logger.log(getName()+" over ["+article.getName()+"], found "+nbrEnt+" mentions");
		logger.decreaseOffset();

		return result;
	}
	
    /**
     * Takes an object representation of the article, 
     * and returns the internal representation of
     * the detected mentions. Those must then
     * be converted to objects compatible
     * with the rest of Nerwip.
     * 
     * @param article
     * 		Article to process.
	 * @param mentions
	 * 		Previously detected mentions for the considered article, to be updated.
	 * @param entities
	 * 		Known entities, to be updated.
	 * @return
	 * 		String representation of the linker original output.
     * 
     * @throws LinkerException
     * 		Problem while applying the recognizer.
     */
	protected abstract String linkEntities(Article article, Mentions mentions, Entities entities) throws LinkerException;
}
