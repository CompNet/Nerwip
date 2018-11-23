package fr.univavignon.nerwip.retrieval.reader.journals;

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
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.retrieval.reader.ArticleReader;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.tools.html.HtmlNames;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * From a specified URL, this class retrieves a page
 * from the French newspaper Le Parisien (as of 17/08/2017),
 * and gives access to the raw and linked texts, as well
 * as other metadata (authors, publishing date, etc.).
 * 
 * @author Vincent Labatut
 */
public class LeParisienReader extends AbstractJournalReader
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
//		URL url = new URL("http://www.leparisien.fr/economie/tabac-pourquoi-les-francais-fument-toujours-autant-17-08-2017-7196751.php");
		URL url = new URL("http://www.leparisien.fr/economie/loi-travail-l-elysee-lache-du-lest-sur-le-timing-24-05-2017-6978660.php");
		
		ArticleReader reader = new LeParisienReader();
		Article article = reader.processUrl(url, ArticleLanguage.FR);
		System.out.println(article);
		article.write();
	}
	
	/////////////////////////////////////////////////////////////////
	// DOMAIN			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Text allowing to detect the domain */
	public static final String DOMAIN = "www.leparisien.fr";

	@Override
	public String getDomain()
	{	return DOMAIN;
	}
	
	/////////////////////////////////////////////////////////////////
	// RETRIEVE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////	
	/** Format used to parse the dates */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMMM yyyy, HH'h'mm",Locale.FRENCH);
	/** String prefix used to specify the modification date in the Web page */
	private static final String UPDT_PREFIX = "MAJ : ";
	
	/** Class of the author names */
	private final static String CLASS_AUTHORS = "article-full__infos-author";
	/** Class of the article description */
	private final static String CLASS_DESCRIPTION = "article-full__header";
	/** Class of the article body */
	private final static String CLASS_ARTICLE_BODY = "article-full__body";
	/** Class of the article body */
	private final static String CLASS_ARTICLE_MAIN = "article-full";
	/** Class of the article title */
	private final static String CLASS_TITLE = "article-full__title";
	/** Class of the article information */
	private final static String CLASS_INFO = "article-full__infos";
	/** Class of the dates */
	private final static String CLASS_DATES = "article-full__infos-date";
	
	/** Message of the restricted access */
	private final static String MSG_RESTRICTED = "Répondez à une question rapide avant de pouvoir accéder à cet article";

	@Override
	public Article processUrl(URL url, ArticleLanguage language) throws ReaderException
	{	Article result = null;
		
		// just a list of articles: nothing to get from that
		if(url.toString().contains("/actualites/"))
		{	logger.log("ERROR: The document is not an article, but a list of articles (URL "+url+")");
			throw new ReaderException("The document is not an article, but a list of articles (URL "+url+")",true);
		}
		
		// an actual article, processed appropriately
		else
		{	String name = getName(url);
			
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
				else if(articleElts.size()>1)
					logger.log("WARNING: found several <article> elements in the same page.");
				Element articleElt = articleElts.first();
				Element fullElt = articleElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_ARTICLE_MAIN).first();
				if(fullElt==null)
				{	logger.log("WARNING: Could not find the \"article-full\" element >> probably a list of articles (by opposition to a single specific article)");
					throw new ReaderException("Could not access the article content");
				}
				else
				{	Element infoElt = fullElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_INFO).first();
					
					// check if the access is restricted
					String text = articleElt.text();
					if(text.contains(MSG_RESTRICTED))
						logger.log("WARNING: The access to this article is limited, only the beginning is available.");
			
					// get the title
					Element titleElt = fullElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_TITLE).first();
					title = titleElt.text(); 
					logger.log("Get title: \""+title+"\"");
			
					// retrieve the dates
					Element datesElt = infoElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_DATES).first();
					Element spanElt = datesElt.child(0);
					List<TextNode> textNodes = spanElt.textNodes();
					String pubDateStr = textNodes.get(0).text().trim();
					String updtDateStr = null;
					if(textNodes.size()>1)
						updtDateStr = textNodes.get(1).text().trim().substring(UPDT_PREFIX.length());
					try
					{	publishingDate = DATE_FORMAT.parse(pubDateStr);
						logger.log("Found the publishing date: "+publishingDate);
						if(updtDateStr!=null)
						{	modificationDate = DATE_FORMAT.parse(updtDateStr);
							logger.log("Found the last modification date: "+modificationDate);
						}
						else
							logger.log("Did not find any last modification date");
					}
					catch (java.text.ParseException e) 
					{	e.printStackTrace();
					}
					
					// retrieve the authors
					Element authorElt = infoElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_AUTHORS).first();
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
					Elements descriptionElts = fullElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_DESCRIPTION);
					Element descriptionElt = null;
					if(!descriptionElts.isEmpty())
					{	descriptionElt = descriptionElts.first();
						processAnyElement(descriptionElt, rawStr, linkedStr);
					}
					
					// processing the article main content
					if(descriptionElt==null)
					{	Element bodyElt = fullElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_ARTICLE_BODY).first();
						descriptionElt = bodyElt.child(0);
					}
					Element contentElt = descriptionElt.nextElementSibling();
					while(contentElt!=null)
					{	Attributes attr = contentElt.attributes();
						if(attr.size()==0)
							processAnyElement(contentElt, rawStr, linkedStr);
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
				}
				
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
		}
		
		return result;
	}
}
