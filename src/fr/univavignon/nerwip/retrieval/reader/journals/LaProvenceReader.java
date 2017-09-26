package fr.univavignon.nerwip.retrieval.reader.journals;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.retrieval.reader.ArticleReader;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.html.HtmlNames;
import fr.univavignon.nerwip.tools.html.HtmlTools;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * From a specified URL, this class retrieves a page
 * from the french newspaper La Provence (as of 18/08/2017),
 * and gives access to the raw and linked texts, as well
 * as other metadata (authors, publishing date, etc.).
 * 
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class LaProvenceReader extends ArticleReader
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
		URL url = new URL("http://www.laprovence.com/article/faits-divers-justice/4581041/il-y-a-140-ans-hysterie-collective-pour-la-guillotine-publique-a-marseille.html");
//		URL url = new URL("http://www.laprovence.com/article/om/4582455/ligue-europa-lom-a-eu-tres-chaud.html");
		
		ArticleReader reader = new LaProvenceReader();
		Article article = reader.processUrl(url, ArticleLanguage.FR);
		System.out.println(article);
		article.write();
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes the name of the article
	 * from the specified URL.
	 * 
	 * @param url
	 * 		URL of the article.
	 * @return
	 * 		Name of the article.
	 */
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
	/** Text allowing to detect the domain */
	public static final String DOMAIN = "www.laprovence.com";

	@Override
	public String getDomain()
	{	return DOMAIN;
	}

	/////////////////////////////////////////////////////////////////
	// RETRIEVE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////	
	/** Format used to parse the dates */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX",Locale.FRENCH);

	/** Item prop of the publication date */
	private final static String ITEMPROP_DATE_PUB = "datePublished";
	/** Item prop of the modification date */
	private final static String ITEMPROP_DATE_UPDT = "dateModified";

	/** Class of the author names */
	private final static String CLASS_AUTHOR = "signature";
	/** Class of the article text */
	private final static String CLASS_TEXT = "p402_premium";
	/** Class of the article live event */
	private final static String CLASS_LIVE = "live_container";
	
	/** Id of the article information */
	private final static String ID_INFO = "article-infos";
	/** Id of the title */
	private final static String ID_TITLE = "divTitle";
	/** Id of the article body */
	private final static String ID_BODY = "id_article_corps";
	/** Id of the restricted access */
	private final static String ID_RESTRICTED = "textePayant";
	
	@Override
	public Article processUrl(URL url, ArticleLanguage language) throws ReaderException
	{	Article result = null;
		String name = getName(url);
		
		try
		{	// init variables
			String title = "";
			StringBuilder rawStr = new StringBuilder();
			StringBuilder linkedStr = new StringBuilder();
			Date publishingDate = null;
			Date modificationDate = null;
			List<String> authors = new ArrayList<String>();
			
			// get the page
			String address = url.toString();
			logger.log("Retrieving page "+address);
			long startTime = System.currentTimeMillis();
			Document document  = retrieveSourceCode(name,url);
			if(document==null)
			{	logger.log("ERROR: Could not retrieve the document at URL "+url);
				throw new ReaderException("Could not retrieve the document at URL "+url);
			}
	
			// get the article element
			logger.log("Get the main element of the document");
			Elements articleElts = document.getElementsByTag(HtmlNames.ELT_ARTICLE);
			Element articleElt = null;
			if(articleElts.size()==0)
				throw new IllegalArgumentException("No <article> element found in the Web page");
			else 
				articleElt = articleElts.first();
			Element infoElt = articleElt.getElementById(ID_INFO);
			Element bodyElt = articleElt.getElementById(ID_BODY);
			
			// get the title
			Element titleElt = infoElt.getElementById(ID_TITLE);
			title = titleElt.text();
			logger.log("Get title: \""+title+"\"");
	
			// retrieve the dates
			Elements pubDateElts = infoElt.getElementsByAttributeValueContaining(HtmlNames.ATT_ITEMPROP, ITEMPROP_DATE_PUB);
			if(!pubDateElts.isEmpty())
			{	Element pubDateElt = pubDateElts.first();
				publishingDate = HtmlTools.getDateFromTimeElt(pubDateElt,DATE_FORMAT);
				logger.log("Found the publishing date: "+publishingDate);
			}
			else
				logger.log("Did not find any publication date");
			Elements updtDateElts = infoElt.getElementsByAttributeValueContaining(HtmlNames.ATT_ITEMPROP, ITEMPROP_DATE_UPDT);
			if(!updtDateElts.isEmpty())
			{	Element updtDateElt = updtDateElts.first();
				modificationDate = HtmlTools.getDateFromTimeElt(updtDateElt,DATE_FORMAT);
				logger.log("Found the last modification date: "+modificationDate);
			}
			else
				logger.log("Did not find any last modification date");
			
			// retrieve the author
			Element authorElt = articleElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_AUTHOR).first();
			String authorName = authorElt.text();
			authorName = removeGtst(authorName);
			logger.log("Found the author: "+authorName);
			authors.add(authorName);
			
			// check if the access is restricted
			Element restrElt = articleElt.getElementById(ID_RESTRICTED);
			if(restrElt!=null)
			{	logger.log("WARNING: The access to this article is limited, only the beginning is available.");
				processAnyElement(restrElt, rawStr, linkedStr);
			}
			else
			{	// processing the article main content
				Elements contentElts = bodyElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_TEXT);
				Elements liveElts = bodyElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_LIVE);
				if(!contentElts.isEmpty())
				{	Element contentElt = contentElts.first();
					processAnyElement(contentElt, rawStr, linkedStr);
				}
				// can alternatively be a live stream
				else if(!liveElts.isEmpty())
				{	for(Element liveElt: liveElts)
					{	processAnyElement(liveElt, rawStr, linkedStr);
						rawStr.append("\n");
						linkedStr.append("\n");
					}
				}
				// otherwise just use the attribute-less paragraphs
				else
				{	Elements parElts = bodyElt.getElementsByTag(HtmlNames.ELT_P);
					for(Element parElt: parElts)
					{	Attributes attributes = parElt.attributes();
						if(attributes==null || attributes.size()==0)
							processAnyElement(parElt, rawStr, linkedStr);
					}
				}
			}
			
			// create and init article object
			result = new Article(name);
			result.setTitle(title);
			result.setUrl(url);
			result.initRetrievalDate();
			result.setPublishingDate(publishingDate);
			if(modificationDate!=null)
				result.setModificationDate(modificationDate);
			if(authors!=null)
				result.addAuthors(authors);
			
			// add the title to the content, just in case the entity appears there but not in the article body
			String rawText = rawStr.toString();
			String linkedText = linkedStr.toString();
			if(title!=null && !title.isEmpty())
			{	rawText = title + "\n" + rawText;
				linkedText = title + "\n" + linkedText;
			}
			
			// clean text
			result.setRawText(rawText);
			logger.log("Length of the raw text: "+rawText.length()+" chars.");
			result.setLinkedText(linkedText);
			logger.log("Length of the linked text: "+linkedText.length()+" chars.");

			// language
			if(language==null)
			{	language = StringTools.detectLanguage(rawText,false);
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
		catch (ClientProtocolException e)
		{	e.printStackTrace();
		} 
		catch (ParseException e)
		{	e.printStackTrace();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
		
		return result;
	}
}
