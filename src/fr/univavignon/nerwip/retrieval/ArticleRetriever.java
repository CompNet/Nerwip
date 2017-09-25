package fr.univavignon.nerwip.retrieval;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.retrieval.reader.ArticleReader;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * Uses a reader to retrieve both versions of a text
 * (raw and linked). Then those are recorded in local files.
 * <br>
 * It also allows directly retrieving the text from a
 * cached file, provided we are sure it was cached before.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class ArticleRetriever
{
	/**
	 * Creates a new retriever with 
	 * caching behavior enabled by default.
	 */
	public ArticleRetriever()
	{	//
	}
	
	/**
	 * Creates a new retriever with 
	 * the specified caching behavior.
	 * 
	 * @param cache
	 * 		{@code true} to enable caching.
	 */
	public ArticleRetriever(boolean cache)
	{	this.cache = cache;
	}

	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CACHE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not text should be cached localy */
	private boolean cache = true;
	/** Whether or not original source code should be cached localy */
	private boolean readerCache = true;
	
	/**
	 * Switches the cache flag. If the cache
	 * is on, then the text is stored locally.
	 * 
	 * @param enabled
	 * 		{@code true} to enable caching.
	 */
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
	}

	/**
	 * Switches the cache flag for the
	 * reader used by this {@code ArticleRetriever},
	 * which means: whether or not the HTML
	 * source code should be cached (independtly 
	 * from the textual content).
	 * 
	 * @param enabled
	 * 		{@code true} to enable caching.
	 */
	public void setReaderCache(boolean enabled)
	{	this.readerCache = enabled;
	}
	
	/////////////////////////////////////////////////////////////////
	// LANGUAGE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Language specified by the user for the retrieved articles (or {@code null} if it is unknown) */
	private ArticleLanguage language = null;
	
	/**
	 * Changes the language specified by
	 * the user for the retrieved articles.
	 * 
	 * @param language
	 * 		New language for the articles,
	 * 		or {@code null} if the language is unknown.
	 */
	public void setLanguage(ArticleLanguage language)
	{	this.language = language;
	}

	/////////////////////////////////////////////////////////////////
	// RETRIEVE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the texts corresponding to the specified URL.
	 * <br/>
	 * The appropriate reader is automatically selected depending
	 * on the URL.
	 * <br/>
	 * The cache mechanism is used depending on the {@code #cache}
	 * parameter. 
	 * 
	 * @param url
	 * 		URL to process.
	 * @return
	 * 		{@code Article} object containing all the relevant data.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the cache files. 
	 * 
	 * @throws ReaderException 
	 * 		Problem while getting the article from the web.
	 * @throws ParseException 
	 * 		Problem while reading/writing the article locally. 
	 * @throws SAXException 
	 * 		Problem while reading/writing the article locally. 
	 */
	public Article process(URL url) throws ReaderException, IOException, ParseException, SAXException
	{	String address = url.toString();
		logger.log("Retrieving article from "+address);
		logger.increaseOffset();
		Article result = null;
	
		// choose the reader depending on the URL base
		logger.log("Selecting reader: ");
		logger.increaseOffset();
			ArticleReader reader = ArticleReader.buildReader(address);
			String name = reader.getName(url);
		logger.log("Detected domain: "+reader.getDomain());
		logger.decreaseOffset();
		
		// determine if the page should be accessed
		if(!cache || !Article.isCached(name))
		{	logger.log("Article not cached, need to process the original web page ("+address+")");
			logger.increaseOffset();
			
			// use the reader to get the text
			reader.setCacheEnabled(readerCache);
			try
			{	result = reader.read(url,language);
			}
			catch(Exception e)
			{	logger.decreaseOffset();
				logger.decreaseOffset();
				throw e;
			}
			logger.decreaseOffset();
			
			// then record the contents
			logger.log("Write the resulting article in the appropriate files");
			result.write();
		}
		
		// otherwise, read the data directly from the files
		else
		{	logger.log("Get content from cache");
			logger.increaseOffset();
			
			result = Article.read(name);
			
			logger.decreaseOffset();
		}
		
		logger.decreaseOffset();
		logger.log("Retrieval done for "+address);
		return result;
	}

	/**
	 * Returns the texts corresponding to the specified name.
	 * <br/>
	 * We suppose the corresponding article was previously cached,
	 * other wise the method will fail, and you rather have to
	 * use {@link #process(URL)}. 
	 * 
	 * @param name
	 * 		Name of the folder containing the article.
	 * @return
	 * 		{@code Article} object containing all the relevant data.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the cache files. 
	 * @throws ParseException 
	 * 		Problem while reading the article locally. 
	 * @throws SAXException 
	 * 		Problem while reading the article locally. 
	 * @throws ReaderException 
	 * 		Problem while getting the article from the web.
	 */
	public Article process(String name) throws ParseException, SAXException, IOException, ReaderException
	{	logger.log("Retrieving article named "+name);
		logger.increaseOffset();
		Article result = null;
		
		// check if the string is a url
		try
		{	URL url = new URL(name);
			logger.log("This name seems to be a URL, trying to retrieve it");
			result = process(url);
		}
		
		// the string is not a URL: it's a name
		catch(MalformedURLException e)
		{	logger.log("This name is not a URL, so trying to retrieve it directly from cache");
			result = Article.read(name);
		}
		
		finally
		{	logger.decreaseOffset();
		}
		logger.log("Retrieval done for article "+name);
		return result;
	}
}
