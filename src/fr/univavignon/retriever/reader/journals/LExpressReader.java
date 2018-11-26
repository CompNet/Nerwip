package fr.univavignon.retriever.reader.journals;

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
import java.net.URL;
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
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.nerwip.tools.html.HtmlNames;
import fr.univavignon.nerwip.tools.html.HtmlTools;
import fr.univavignon.nerwip.tools.string.StringTools;
import fr.univavignon.retriever.reader.ArticleReader;
import fr.univavignon.retriever.reader.ReaderException;

/**
 * From a specified URL, this class retrieves a page
 * from the French newspaper L'Express (as of 18/08/2017),
 * and gives access to the raw and linked texts, as well
 * as other metadata (authors, publishing date, etc.).
 * 
 * @author Vincent Labatut
 */
public class LExpressReader extends AbstractJournalReader
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
		URL url = new URL("http://www.lexpress.fr/styles/vip/angelina-jolie-et-brad-pitt-condamnes-a-verser-500-000-euros-a-une-artiste_1936140.html");
//		URL url = new URL("http://www.lexpress.fr/actualite/politique/lfi/les-insoumis-ayant-chante-tout-l-ete_1935643.html");
		
		ArticleReader reader = new LExpressReader();
		Article article = reader.processUrl(url, ArticleLanguage.FR);
		System.out.println(article);
		article.write();
	}
	
	/////////////////////////////////////////////////////////////////
	// DOMAIN			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Text allowing to detect the domain */
	public static final String DOMAIN = "www.lexpress.fr";

	@Override
	public String getDomain()
	{	return DOMAIN;
	}

	/////////////////////////////////////////////////////////////////
	// RETRIEVE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////	
	/** Format used to parse the dates */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX",Locale.FRENCH);
	/** String prefix used to specify the modification date in the Web page */
	private static final String PREFIX_AUTHOR = "Par ";


	/** Class of the inter-titles */
	private final static String CLASS_INTERTITLE = "intertitre";
	/** Class of the article description panel */
	private final static String CLASS_DESCRIPTION = "chapo title_gamma";
	/** Class of the article body */
	private final static String CLASS_CONTENT = "article_content";
	/** Class of the article title */
	private final static String CLASS_TITLE = "title_alpha";
	/** Class of the restricted access */
	private final static String CLASS_RESTRICTED = "premium_content";
	/** Class of the restricted access */
	private final static String CLASS_SIGNATURE = "signature";
	
	@Override
	public Article processUrl(URL url, ArticleLanguage language) throws ReaderException
	{	Article result = null;
		
		// just a list of articles: nothing to get from that
		if(url.toString().contains("/archives/"))
		{	logger.log("ERROR: The document is not an article, but a list of articles (URL "+url+")");
			throw new ReaderException("The document is not an article, but a list of articles (URL "+url+")",true);
		}
		
		// an actual article, processed appropriately
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
//			else if(articleElts.size()>1)
//				logger.log("WARNING: found several <article> elements in the same page.");
// TODO for this journal, the main content is in an article, but the links to related content also (so several article elements in the same page)
			Element articleElt = articleElts.first();
			Element headerElt = articleElt.getElementsByTag(HtmlNames.ELT_HEADER).first();
			Element bodyElt = articleElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_CONTENT).first();
			
			// get the title
			Element titleElt = headerElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_TITLE).first();
			title = titleElt.text(); 
			logger.log("Get title: \""+title+"\"");

			// retrieve the dates
			Element signatureElt = headerElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_SIGNATURE).first();
			Elements timeElts = signatureElt.getElementsByTag(HtmlNames.ELT_TIME);
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
	
			// retrieve the author
			List<TextNode> textNodes = signatureElt.textNodes();
			if(textNodes.size()<2)
				logger.log("WARNING: Could not find the author, which is unusual");
			else
			{	TextNode authorNode = textNodes.get(1);
				String authorName = authorNode.text().trim();
				// remove "Par "
				if(authorName.startsWith(PREFIX_AUTHOR))
					authorName = authorName.substring(PREFIX_AUTHOR.length());
				// remove ", publié le..."
				int pos = authorName.indexOf(',');
				if(pos!=-1)
					authorName = authorName.substring(0,pos);
				authorName = removeGtst(authorName);
				authors.add(authorName);
			}

			// get the description
			Element descriptionElt = bodyElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_DESCRIPTION).first();
			if(descriptionElt==null)
				logger.log("Could not find any article presenstation");
			else
			{	processAnyElement(descriptionElt, rawStr, linkedStr);
				rawStr.append("\n");
				linkedStr.append("\n");
			}

			// processing the article main content
			Element contentElt = descriptionElt.nextElementSibling();
			while(contentElt!=null)
			{	String classStr = contentElt.attr(HtmlNames.ATT_CLASS);
				if(contentElt.tagName().equalsIgnoreCase(HtmlNames.ELT_P)
					|| classStr!=null && classStr.contains(CLASS_INTERTITLE))
				{	processAnyElement(contentElt, rawStr, linkedStr);
					rawStr.append("\n");
					linkedStr.append("\n");
				}
				if(classStr!=null && classStr.equalsIgnoreCase(CLASS_RESTRICTED))
					logger.log("WARNING: The access to this article is limited, only the beginning is available.");
				contentElt = contentElt.nextElementSibling();
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
