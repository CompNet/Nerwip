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
import java.util.Iterator;
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
 * from the french newspaper Le Figaro (as of 09/09/2017),
 * and gives access to the raw and linked texts, as well
 * as other metadata (authors, publishing date, etc.).
 * 
 * @author Vincent Labatut
 */
public class LeFigaroReader extends AbstractJournalReader
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
		URL url = new URL("http://www.lefigaro.fr/international/2017/08/16/01003-20170816ARTFIG00075-violences-a-charlottesville-la-polemique-racontee-en-quatre-episodes.php");
//		URL url = new URL("http://www.lefigaro.fr/sciences/2017/08/17/01008-20170817ARTFIG00132-daniel-zagury-l-homme-qui-se-vaccina-contre-le-sida.php");
//		URL url = new URL("http://www.lefigaro.fr/elections/presidentielles/2017/03/02/35003-20170302ARTFIG00373-fillon-les-elus-on-fera-sans-eux-les-electeurs-de-droite-ils-tiennent.php");
		
		ArticleReader reader = new LeFigaroReader();
		Article article = reader.processUrl(url, ArticleLanguage.FR);
		System.out.println(article);
		article.write();
	}
	
	/////////////////////////////////////////////////////////////////
	// DOMAIN			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Text allowing to detect the domain */
	public static final String DOMAIN = "www.lefigaro.fr";

	@Override
	public String getDomain()
	{	return DOMAIN;
	}

	/////////////////////////////////////////////////////////////////
	// RETRIEVE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////	
	/** Format used to parse the dates */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.FRENCH);
	
	/** Class of the list of author names */
	private final static String CLASS_AUTHORS = "fig-content-metas__authors";
	/** Class of one author's name */
	private final static String CLASS_AUTHOR = "fig-content-metas__author";
	/** Class of the article description panel */
	private final static String CLASS_DESCRIPTION = "fig-content__chapo";
	/** Class of the article body */
	private final static String CLASS_ARTICLE_BODY = "fig-content__body";
	/** Class of the restricted access */
	private final static String CLASS_PAYWALL = "fig-premium-paywall";
	
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
			List<String> authors = null;
			
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
			{	//throw new IllegalArgumentException("No <article> element found in the Web page");
				logger.log("WARNING: no <article> element found in this Web page: this generally means it is a list of articles, so we ignore it.");
				throw new ReaderException("No <article> element in Web page "+url);
			}
			else 
			{	if(articleElts.size()>1)
					logger.log("WARNING: found several <article> elements in the same page.");
				articleElt = articleElts.first();
			}
					
			// check if the access is restricted
			Elements promoElts = articleElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_PAYWALL);
			if(!promoElts.isEmpty())
				logger.log("WARNING: The access to this article is limited, only the beginning is available.");
	
			// get the title
			Element titleElt = articleElt.getElementsByTag(HtmlNames.ELT_H1).first();
			List<TextNode> textNodes = titleElt.textNodes();	// we need to ignore "avant-première" and other similar indications
			for(TextNode textNode: textNodes)
				title = title + " " + textNode.text();
			logger.log("Get title: \""+title+"\"");
			
			// retrieve the dates
			Elements dateElts = articleElt.getElementsByTag(HtmlNames.ELT_TIME);
			Iterator<Element> it = dateElts.iterator();
			if(it.hasNext())
			{	Element pubDateElt = it.next();
				publishingDate = HtmlTools.getDateFromTimeElt(pubDateElt,DATE_FORMAT);
				logger.log("Found the publishing date: "+publishingDate);
				if(it.hasNext())
				{	Element updtDateElt = it.next();
					modificationDate = HtmlTools.getDateFromTimeElt(updtDateElt,DATE_FORMAT);
					logger.log("Found the last modification date: "+modificationDate);
				}
				else
					logger.log("Did not find any last modification date");
			}
			else
				logger.log("Did not find any publication date");
			
			// retrieve the authors
			Elements authorElts = articleElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_AUTHOR);
			it = authorElts.iterator();
			while(it.hasNext())
			{	Element elt = it.next();
				String classStr = elt.attr(HtmlNames.ATT_CLASS);
				if(classStr.contains(CLASS_AUTHORS))
					it.remove();
			}
			if(!authorElts.isEmpty())
			{	logger.log("List of the authors found for this article:");
				logger.increaseOffset();
					authors = new ArrayList<String>();
					for(Element nameElt: authorElts)
					{	String authorName = nameElt.text();
						authorName = removeGtst(authorName);
						logger.log(authorName);
						authors.add(authorName);
					}
				logger.decreaseOffset();
			}
			else
				logger.log("WARNING: could not find any author, which is unusual");
					
			// get the description
			Element descriptionElt = articleElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_DESCRIPTION).first();
			String text = descriptionElt.text() + "\n";
			text = removeGtst(text);
			rawStr.append(text);
			linkedStr.append(text);
			
			// processing the article main content
			Element contentElt = articleElt.getElementsByAttributeValueContaining(HtmlNames.ATT_CLASS, CLASS_ARTICLE_BODY).first();
			processAnyElement(contentElt, rawStr, linkedStr);
			
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
