package fr.univavignon.retrieval.reader;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.tools.strings.CommonStringTools;
import fr.univavignon.retrieval.reader.AbstractArticleReader;
import fr.univavignon.retrieval.reader.ReaderException;
import fr.univavignon.tools.html.HtmlNames;
import fr.univavignon.tools.html.HtmlTools;

/**
 * From a specified URL, this class retrieves a Web page,
 * and gives access to the raw text. It relies mainly
 * on the <a href="https://code.google.com/archive/p/boilerpipe/">BoilerPipe</a> library.
 * 
 * @author Vincent Labatut
 */
public class BoilerpipeReader extends AbstractArticleReader
{	
	/**
	 * Method defined only for a quick test.
	 * 
	 * @param args
	 * 		Not used.
	 * 
	 * @throws Exception
	 * 		Whatever exception. 
	 */
	public static void main(String[] args) throws Exception
	{	
//		URL url = new URL("http://www.laprovence.com/article/faits-divers-justice/4581041/il-y-a-140-ans-hysterie-collective-pour-la-guillotine-publique-a-marseille.html");
		URL url = new URL("http://www.lemonde.fr/politique/article/2017/08/22/code-du-travail-la-reforme-entre-dans-sa-phase-finale_5175000_823448.html");
		
		AbstractArticleReader reader = new BoilerpipeReader();
		Article article = reader.processUrl(url, ArticleLanguage.FR);
		System.out.println(article);
		article.write();
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getName(URL url)
	{	String address = url.toString();
		
		// convert the full URL to a file-compatible name
		String result = null;
		try 
		{	result = URLEncoder.encode(address,"UTF-8");
			// reverse the transformation :
			// String original = URLDecoder.decode(result, "UTF-8");
		
			// needed if the URL is longer than the max length authorized by the OS for folder names
			if(result.length()>255)	
				result = result.substring(0,255);

		}
		catch (UnsupportedEncodingException e)
		{	e.printStackTrace();
		}
		
		// alternative : generate a random name (not reproducible, though)
//		UUID.randomUUID();

		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// DOMAIN			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getDomain()
	{	return "BoilerPipe reader";
	}

	/////////////////////////////////////////////////////////////////
	// RETRIEVE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////	
	/** Formats used to parse the dates */
	private static final DateFormat DATE_FORMATS[] = 
		{	new SimpleDateFormat("dd MMMM yyyy, HH'h'mm",Locale.FRENCH),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX",Locale.FRENCH),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX",Locale.FRENCH),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.FRENCH),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",Locale.FRENCH),
			new SimpleDateFormat("dd/MM/yyyy",Locale.FRENCH)			
		};
	
	@Override
	public Article processUrl(URL url, ArticleLanguage language) throws ReaderException
	{	Article result = null;
		String name = getName(url);
		
		try
		{	// init variables
			String title = null;
			StringBuilder rawStr = new StringBuilder();
			StringBuilder linkedStr = new StringBuilder();
			Date publishingDate = null;
			Date modificationDate = null;
			
			// get the page
			String address = url.toString();
			logger.log("Retrieving page "+address);
			long startTime = System.currentTimeMillis();
			Document document  = retrieveSourceCode(name,url);
			if(document==null)
			{	logger.log("ERROR: Could not retrieve the document at URL "+url);
				throw new ReaderException("Could not retrieve the document at URL "+url);
			}
			
			// get its title
			Elements titleElts = document.getElementsByTag(HtmlNames.ELT_TITLE);
			if(titleElts.isEmpty())
				logger.log("The page has no title");
			else
			{	Element titleElt = titleElts.get(0);
				title = titleElt.text();
				logger.log("Get title: "+title);
			}
			
			// try to get some dates
			Elements timeElts = document.getElementsByTag(HtmlNames.ELT_TIME);
			if(!timeElts.isEmpty())
			{	Element timeElt = timeElts.get(0);
				int i = 0;
				while(publishingDate==null && i<DATE_FORMATS.length)
				{	publishingDate = HtmlTools.getDateFromTimeElt(timeElt,DATE_FORMATS[i]);
					i++;
				}
				if(publishingDate==null)
					logger.log("Did not find any publishing date");
				else
					logger.log("Found the publishing date: "+publishingDate);
				if(timeElts.size()>1)
				{	timeElt = timeElts.get(1);
					i = 0;
					while(modificationDate==null && i<DATE_FORMATS.length)
					{	modificationDate = HtmlTools.getDateFromTimeElt(timeElt,DATE_FORMATS[i]);
						i++;
					}
					if(modificationDate==null)
						logger.log("Could not find the last modification date");
					else
						logger.log("Found the last modification date: "+modificationDate);
				}
				else
					logger.log("Did not find any last modification date");
			}
			else
				logger.log("Did not find any publication date");
				
			
			// get the content using boilerpipe
			try
			{	logger.log("Using BoilerPipe to identify the relevant content");
				String src = document.toString();
				String text = ArticleExtractor.INSTANCE.getText(src);
				rawStr.append(text);
				linkedStr.append(text);
			} 
			catch (BoilerpipeProcessingException e) 
			{	logger.log("ERROR: problem while applying BoilerPupe to URL "+url+" ("+e.getMessage()+")");
				e.printStackTrace();
			}
			String rawText = rawStr.toString();
			
			// create article object
			result = new Article(name);
			result.setTitle(title);
			result.setUrl(url);
			result.initRetrievalDate();
			if(publishingDate!=null)
				result.setPublishingDate(publishingDate);
			if(modificationDate!=null)
				result.setModificationDate(modificationDate);
			
			// clean text
//			rawText = cleanText(rawText);
//			rawText = ArticleCleaning.replaceChars(rawText);
			result.setRawText(rawText);
			logger.log("Length of the raw text: "+rawText.length()+" chars.");			
			
			// language
			if(language==null)
			{	language = CommonStringTools.detectLanguage(rawText,false);
				logger.log("Detected language: "+language);
			}
			result.setLanguage(language);
			
			// get original html source code
			logger.log("Get original HTML source code.");
			String originalPage = document.toString();
			result.setOriginalPage(originalPage);
			logger.log("Length of the original page: "+originalPage.length()+" chars.");

			long endTime = System.currentTimeMillis();
			logger.log("Total duration: "+(endTime-startTime)+" ms.");
		}
		catch(ClientProtocolException e)
		{	e.printStackTrace();
		} 
		catch(ParseException e)
		{	e.printStackTrace();
		}
		catch(IOException e)
		{	e.printStackTrace();
		}
		
		return result;
	}
}
