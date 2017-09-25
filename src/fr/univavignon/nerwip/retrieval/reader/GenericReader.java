package fr.univavignon.nerwip.retrieval.reader;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
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
 * From a specified URL, this class retrieves a Web page,
 * and gives access to the raw and linked texts. A generic
 * strategy is applied to detect which part of the page
 * corresponds to some relevant content (cf. {@link #getContentElement(Element)}.
 * 
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class GenericReader extends ArticleReader
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
		
		ArticleReader reader = new GenericReader();
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
	@Override
	public String getDomain()
	{	return "Generic reader";
	}

	/////////////////////////////////////////////////////////////////
	// RETRIEVE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////	
	/** Formats be used to parse the dates */
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
				
			
			// identify the content element
//if(url.toString().equalsIgnoreCase("http://www.lamarseillaise.fr/vaucluse/developpement-durable/58144-avignon-ca-bouge-autour-du-technopole-de-l-agroparc"))
//	System.out.println();
			logger.log("Get the main element of the document");
			List<Element> bodyElts = document.getElementsByTag(HtmlNames.ELT_BODY);
			if(bodyElts.isEmpty())
			{	logger.log("ERROR: The HTML source code does not contain a BODY tag for URL "+url);
				throw new ReaderException("The HTML source code does not contain a BODY tag for URL "+url);
			}
			Element bodyElt = bodyElts.get(0);
			Element contentElt = getContentElement(bodyElt);
			if(contentElt==null)
			{	logger.log("ERROR: Could not identify the main element of the document of URL "+url);
				throw new ReaderException("Could not identify the main element for URL "+url);
			}

			// processing each element in the content part
			logger.log("Get raw and linked texts");
			processAnyElement(contentElt, rawStr, linkedStr);
			
			// create article object
			result = new Article(name);
			result.setTitle(title);
			result.setUrl(url);
			result.initRetrievalDate();
			if(publishingDate!=null)
				result.setPublishingDate(publishingDate);
			if(modificationDate!=null)
				result.setModificationDate(modificationDate);
			
			// add the title to the content, just in case the entity appears there but not in the article body
			String rawText = rawStr.toString();
			String linkedText = linkedStr.toString();
			if(title!=null && !title.isEmpty())
			{	rawText = title + "\n" + rawText;
				linkedText = title + "\n" + linkedText;
			}
			
			// clean text
//			rawText = cleanText(rawText);
//			rawText = ArticleCleaning.replaceChars(rawText);
			result.setRawText(rawText);
			logger.log("Length of the raw text: "+rawText.length()+" chars.");			
//			linkedText = cleanText(linkedText);
//			linkedText = ArticleCleaning.replaceChars(linkedText);
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
	
	/////////////////////////////////////////////////////////////////
	// CONTENT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Minimal proportion of the document (text-wise) we want to access */
	private static final float MIN_CONTENT_RATIO = 0.5f;
	/** List of tags which are not likely to contain the content element */
	private static final List<String> STOP_TAGS = Arrays.asList(
			HtmlNames.ELT_APPLET,HtmlNames.ELT_AREA,HtmlNames.ELT_ASIDE,HtmlNames.ELT_AUDIO,
			HtmlNames.ELT_BASE,HtmlNames.ELT_BASEFONT,HtmlNames.ELT_BDI,HtmlNames.ELT_BDO,
			HtmlNames.ELT_BUTTON,HtmlNames.ELT_CANVAS,HtmlNames.ELT_CODE,HtmlNames.ELT_CONTENT,
			HtmlNames.ELT_DATA,HtmlNames.ELT_DATALIST,HtmlNames.ELT_DECORATOR,HtmlNames.ELT_DEL,
			HtmlNames.ELT_DIALOG,HtmlNames.ELT_DIR,HtmlNames.ELT_ELEMENT,HtmlNames.ELT_EMBED,
			HtmlNames.ELT_FIELDSET,HtmlNames.ELT_FORM,HtmlNames.ELT_FRAME,
			HtmlNames.ELT_FRAMESET,HtmlNames.ELT_IFRAME,HtmlNames.ELT_IMG,HtmlNames.ELT_INPUT,
			HtmlNames.ELT_ISINDEX,HtmlNames.ELT_KBD,HtmlNames.ELT_KEYGEN,HtmlNames.ELT_LABEL,
			HtmlNames.ELT_LEGEND,HtmlNames.ELT_LINK,HtmlNames.ELT_LISTING,HtmlNames.ELT_MAP,
			HtmlNames.ELT_MENU,HtmlNames.ELT_MENUITEM,HtmlNames.ELT_METER,HtmlNames.ELT_NAV,
			HtmlNames.ELT_OBJECT,HtmlNames.ELT_OPTGROUP,HtmlNames.ELT_OPTION,HtmlNames.ELT_OUTPUT,
			HtmlNames.ELT_PARAM,HtmlNames.ELT_PROGRESS,HtmlNames.ELT_RP,HtmlNames.ELT_RT,
			HtmlNames.ELT_RTC,HtmlNames.ELT_RUBY,HtmlNames.ELT_S,HtmlNames.ELT_SAMP,
			HtmlNames.ELT_SCRIPT,HtmlNames.ELT_SHADOW,HtmlNames.ELT_SELECT,HtmlNames.ELT_SOURCE,
			HtmlNames.ELT_STRIKE,HtmlNames.ELT_STYLE,HtmlNames.ELT_SUB,HtmlNames.ELT_SUP,
			HtmlNames.ELT_TEMPLATE,HtmlNames.ELT_TEXTAREA,HtmlNames.ELT_TRACK,HtmlNames.ELT_VIDEO,
			HtmlNames.ELT_WBR,HtmlNames.ELT_XMP
		);
	
	/**
	 * Identifies which part of the specified element may be the main element, containing the most
	 * relevant content.
	 * <br/>
	 * We consider an element to be a candidate (in terms of containing the relevant part of the Web page)
	 * when none of its children contain more than {@link #MIN_CONTENT_RATIO} percent of its own content.
	 * In other words, when there is a relatively uniform distribution of content amongst children. Otherwise,
	 * if the most part of the content goes to a single child, we go on exploring the document tree.
	 * <br>
	 * The idea here is that an article is generally composed of several distinct paragraphs. So, the method
	 * is not efficient for sites putting all their content in the same node, for instance a single paragraph
	 * element. TODO We could add an alternative/complementary, method, consisting in focusing on elements
	 * containing almost all the text of the document at once.
	 * 
	 * TODO we could also compute the longest text while ignoring purely formatting elements (b, i, etc.)
	 * <br/>
	 * Once several candidates have been detected, they are compared in terms of the longest uniterrupted text
	 * they contain. The idea here is that sometimes, the article consists of less text than the rest of the
	 * page (menus, navigation, etc.). But it generally contains longer sentences, hence this criterion.  
	 * 
	 * @param root
	 * 		Root element.
	 * @return
	 * 		The elected subelement.
	 */
	private Element getContentElement(Element root)
	{	Element result = null;
		int maxText = 0;
		
		String totalText = root.text();
		float totalLength = totalText.length();
		logger.increaseOffset();
		logger.log("Total text length: "+totalLength+" characters");
		
		// presence of an <article> element in the page
//		Elements articleElts = root.getElementsByTag(HtmlNames.ELT_ARTICLE);
//		if(!articleElts.isEmpty())
//		{	logger.log("Found an <article> element in this Web page >> using it as the main content element");
//			root = articleElts.first();
//			if(articleElts.size()>1)
//				logger.log("WARNING: found several <article> elements in this Web page");
//		}
		// not a good idea: <article> is often misused (as well as <section>)
		
		// now, use text size 
		{	//logger.log("No <article> element in this Web page >> using text size");
			
			// set up data structures
			Map<Element,Float> sizes = new HashMap<Element, Float>();
			sizes.put(root, totalLength);
			Queue<Element> queue = new LinkedList<Element>();
			queue.offer(root);
			
			do
			{	Element element = queue.poll();
				String name = element.tagName();
				Elements children = element.children();
				float size = sizes.get(element);
				boolean candidate = false;
				
				// if the node is in the stop list, it's not a candidate
				if(STOP_TAGS.contains(name))
					candidate = false;
				
				// otherwise, if the node has no children, it's a candidate
				else if(children.isEmpty())
					candidate = true;
				
				// if it has one or more child
				else
				{	// a list is probably not a candidate (TODO other tags?)
					candidate = !name.equals(HtmlNames.ELT_UL) && !name.equals(HtmlNames.ELT_OL);
					// it is a candidate only if none child contains the majority of its text
					for(Element child: children)
					{	String text = child.text();
						float sz = text.length();
						sizes.put(child,sz);
						candidate = candidate && sz/size<MIN_CONTENT_RATIO;
					}
					// if not a candidate, put its children in the queue
					if(!candidate)
					{	for(Element child: children)
							queue.offer(child);
					}
				}
				
				// if we found a candidate
				if(candidate)
				{	// it's the new best candidate only if it's larger than the previous best candidate
//					if(result!=null)
//					{	float size0 = sizes.get(result);
//						if(size>size0)
//							result = element;
//					}
//					else
//						result = element;
					
					// it's the new best candidate if its largest uninterrupted text is larger than for the previous best candidate
					int sz = HtmlTools.getMaxTextLength(element);
					if(result!=null)
					{	if(sz>maxText)
						{	result = element;
							maxText = sz;
						}
					}
					else
					{	result = element;
						maxText = sz;
					}
				}
			}
			while(!queue.isEmpty());
		}
		
		if(result!=null)
		{	String text = result.text();
			float size = text.length();
			logger.log("Selected element: "+size+" characters ("+size/totalLength*100+"%)");
		}
		else
			logger.log("WARNING: could not select any element");
		
		logger.decreaseOffset();
		return result;
	}
}
