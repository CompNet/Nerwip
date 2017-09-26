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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.retrieval.reader.ArticleReader;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.tools.html.HtmlNames;
import fr.univavignon.nerwip.tools.html.HtmlTools;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * From a specified URL, this class retrieves a page
 * from the French newspaper La Voix du Nord (as of 21/08/2017),
 * and gives access to the raw and linked texts, as well
 * as other metadata (authors, publishing date, etc.).nerwip
 * 
 * @author Vincent Labatut
 */
public class LaVoixDuNordReader extends ArticleReader
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
//		URL url = new URL("http://www.lavoixdunord.fr/207069/article/2017-08-21/un-mineur-accuse-d-apologie-du-terrorisme-apres-les-attentats-espagnols");
//		URL url = new URL("http://www.lavoixdunord.fr/206352/article/2017-08-19/l-eglise-notre-dame-point-de-depart-de-la-francigena-en-france");
		URL url = new URL("http://www.lavoixdunord.fr/128575/article/2017-03-07/le-vieux-lille-peur-de-perdre-une-centaine-d-emplois-tous-secteurs-confondus");
		
		ArticleReader reader = new LaVoixDuNordReader();
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
	public static final String DOMAIN = "www.lavoixdunord.fr";
	
	@Override
	public String getDomain()
	{	return DOMAIN;
	}

	/////////////////////////////////////////////////////////////////
	// RETRIEVE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////	
	/** Format used to parse the dates */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.FRENCH);
	
	/** Class of the author names */
	private final static String CLASS_AUTHORS = "capitalize";
	/** Class of the article description */
	private final static String CLASS_DESCRIPTION = "gr-article-teaser";
	/** Class of the article body */
	private final static String CLASS_ARTICLE_END = "gr-content-footer";
	/** Class of the article body */
	private final static String CLASS_ARTICLE_MAIN = "gr-content-text";
	/** Class of the article information */
	private final static String CLASS_INFO = "gr-article-infos";
	/** Class of the dates */
	private final static String CLASS_DETAILS = "entry-details";
	
	/** Class of the restricted access */
	private final static String MSG_RESTRICTED = "Vous avez consulté vos articles gratuits ce mois-ci.";

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
			if(articleElts.size()==0)
				throw new IllegalArgumentException("No <article> element found in the Web page");
			Element articleElt = articleElts.first();
			Element headerElt = articleElt.getElementsByTag(HtmlNames.ELT_HEADER).first();
			Element infoElt = headerElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_INFO).first();
			Element detailsElt = infoElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_DETAILS).first();			
			
			// check if the access is restricted
			String text = articleElt.text();
			if(text.contains(MSG_RESTRICTED))
				logger.log("WARNING: The access to this article is limited, only the beginning is available.");
			
			// get the title
			Element titleElt = headerElt.getElementsByTag(HtmlNames.ELT_H1).first();
			title = titleElt.text(); 
			logger.log("Get title: \""+title+"\"");

			// retrieve the dates
			Elements timeElts = detailsElt.getElementsByTag(HtmlNames.ELT_TIME);
			if(!timeElts.isEmpty())
			{	Element timeElt = timeElts.get(0);
				publishingDate = HtmlTools.getDateFromTimeElt(timeElt,DATE_FORMAT);
				logger.log("Found the publishing date: "+publishingDate);
				if(timeElts.size()>1)
				{	timeElt = timeElts.get(1);
					modificationDate = HtmlTools.getDateFromTimeElt(timeElt,DATE_FORMAT);
					logger.log("Found the last modification date: "+modificationDate);
				}
				else
					logger.log("Did not find any last modification date");
			}
			else
				logger.log("Did not find any publication date");
			
			// retrieve the authors
			Element authorElt = detailsElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_AUTHORS).first();
			if(authorElt!=null)
			{	String authorName = authorElt.text();
				authorName = removeGtst(authorName);
				authors.add(authorName);
				logger.log("Authors: ");
				logger.log(authors);
			}
			else
				logger.log("Could not find any author for this article");

			// get the description
			Elements descriptionElts = headerElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_DESCRIPTION);
			if(!descriptionElts.isEmpty())
			{	Element descriptionElt = descriptionElts.first();
				processAnyElement(descriptionElt, rawStr, linkedStr);
			}
	
			// processing the article main content
			Element contentElt = articleElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_ARTICLE_MAIN).first();
			Elements parElts = contentElt.children();
			if(parElts.isEmpty())
				throw new ReaderException("Could not find the body of the article at URL "+url);
			else
			{	String classStr;
				Element parElt = parElts.first();
				do
				{	processAnyElement(parElt, rawStr, linkedStr);
					rawStr.append("\n");
					linkedStr.append("\n");
					parElt = parElt.nextElementSibling();
					classStr = parElt.attr(HtmlNames.ATT_CLASS);
				}
				while(!classStr.equalsIgnoreCase(CLASS_ARTICLE_END));
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
