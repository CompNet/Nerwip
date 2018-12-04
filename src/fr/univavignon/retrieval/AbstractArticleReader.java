package fr.univavignon.retrieval;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.tools.files.CommonFileNames;
import fr.univavignon.retrieval.journals.LExpressReader;
import fr.univavignon.retrieval.journals.LaProvenceReader;
import fr.univavignon.retrieval.journals.LaVoixDuNordReader;
import fr.univavignon.retrieval.journals.LeFigaroReader;
import fr.univavignon.retrieval.journals.LeMondeReader;
import fr.univavignon.retrieval.journals.LeParisienReader;
import fr.univavignon.retrieval.journals.LePointReader;
import fr.univavignon.retrieval.journals.LiberationReader;
import fr.univavignon.retrieval.wikipedia.WikipediaReader;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.html.HtmlNames;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

/**
 * All classes automatically getting articles
 * from the Web using a starting name or URL 
 * should inherit from this abstract class.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractArticleReader
{	
	/////////////////////////////////////////////////////////////////
	// FACTORY		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Builds the appropriate reader to handle the specified
	 * Web address, then returns it.
	 *  
	 * @param url
	 * 		The Web address to process. 
	 * @return
	 * 		An appropriate reader for the specified address.
	 */
	public static AbstractArticleReader buildReader(String url)
	{	AbstractArticleReader result;
		
		// Wikipedia article
		if(url.contains(WikipediaReader.DOMAIN))
			result = new WikipediaReader();
		
		// handled French journals
		else if(LaProvenceReader.checkDomain(url))
			result = new LaProvenceReader();
		else if(LaVoixDuNordReader.checkDomain(url))
			result = new LaVoixDuNordReader();
		else if(LeFigaroReader.checkDomain(url))
			result = new LeFigaroReader();
		else if(LeMondeReader.checkDomain(url))
			result = new LeMondeReader();
		else if(LeParisienReader.checkDomain(url))
			result = new LeParisienReader();
		else if(LePointReader.checkDomain(url))
			result = new LePointReader();
		else if(LExpressReader.checkDomain(url))
			result = new LExpressReader();
		else if(LiberationReader.checkDomain(url))
			result = new LiberationReader();
		
		// generic reader for the other cases
		else
//			result = new GenericReader();
			result = new BoilerpipeReader();
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CACHE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not original source code should be cached localy */
	protected boolean cache = true;
	
	/**
	 * Switches the cache flag.
	 * 
	 * @param enabled
	 * 		{@code true} to enable caching.
	 */
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
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
	public abstract String getName(URL url);
	
	/////////////////////////////////////////////////////////////////
	// DOMAIN			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the Web domain handled
	 * by this reader.
	 * 
	 * @return
	 * 		A string representing the Web domain.
	 */
	public abstract String getDomain();
	
	/////////////////////////////////////////////////////////////////
	// CLEANING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Removes the signs {@code <} and {@code >}
	 * from the specified text.
	 * <br/>
	 * This method is meant to be used by article reader
	 * to clean text by removing signs that could be
	 * later mistaken for xml elements (and are <i>a
	 * priori</i> not necessary for NER).
	 * 
	 * @param text
	 * 		Original text.
	 * @return
	 * 		Same text without the signs.s
	 */
	protected String removeGtst(String text)
	{	String result = text;
		result = result.replace("<", "");
		result = result.replace(">", "");
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes the specified URL to get the
	 * targeted article. Also applies a cleaning step
	 * (removing non-breaking space, and so on).
	 * 
	 * @param url
	 * 		Article address.
	 * @param language
	 * 		Language of the retrieved article, or {@code null} if it is unknown.
	 * @return
	 * 		An Article object corresponding to the targeted URL.
	 * 
	 * @throws ReaderException
	 * 		Problem while retrieving the article.
	 */
	public Article read(URL url, ArticleLanguage language) throws ReaderException
	{	Article result = processUrl(url, language);
		
		result.cleanContent();
		
		return result;
	}

	/**
	 * Processes the specified URL to get the
	 * targeted article.
	 * 
	 * @param url
	 * 		Article address.
	 * @param language
	 * 		Language of the retrieved article, or {@code null} if it is unknown.
	 * @return
	 * 		An Article object corresponding to the targeted URL.
	 * 
	 * @throws ReaderException
	 * 		Problem while retrieving the article.
	 */
	public abstract Article processUrl(URL url, ArticleLanguage language) throws ReaderException;

	/**
	 * Loads the HTML source code from the cached file,
	 * or fetches it from the Web server if needed.
	 * 
	 * @param name
	 * 		Name of the concerned article.
	 * @param url
	 * 		URL of the concerned article.
	 * @return
	 * 		The DOM representation of the original page.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the cache or web page.
	 */
	protected Document retrieveSourceCode(String name, URL url) throws IOException
	{	Document result = null;
		logger.increaseOffset();
		logger.log("Retrieve HTML source code");
		
		// check if the cache can/must be used
		String folderPath = FileNames.FO_OUTPUT + File.separator + name;
		File originalFile = new File(folderPath + File.separator + CommonFileNames.FI_ORIGINAL_PAGE);
		if(cache && originalFile.exists())
		{	logger.log("Cache enabled and HTML already retrieved >> we use the cached file ("+originalFile.getName()+")");
			String sourceCode = FileTools.readTextFile(originalFile, "UTF-8");
			result = Jsoup.parse(sourceCode);
		}
		
		// otherwise, load and cache the html file
		else
		{	logger.log("Cache disabled or HTML never retrieved before>> we get it from the Web server");
			logger.increaseOffset();
			
			// use custom page loader
//			String sourceCode = manuallyReadUrl(url);
//			System.out.println(sourceCode.toString());
//			result = new Source(sourceCode);
			
			// use jericho page loader
			int timeOut = 5000;
			boolean again;
			do
			{	again = false;
				try
				{	logger.log("Trying to download the Web page");
//					result = Jsoup.parse(url,timeOut);
					// taken from https://stackoverflow.com/a/20284953/1254730
					Response response = Jsoup.connect(url.toString())
				           .ignoreContentType(true)
//				           .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
				           .userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0")
				           .referrer("http://www.google.fr")   
				           .timeout(timeOut) 
				           .followRedirects(true)
				           .execute();
					result = response.parse();
				}
				catch(SocketTimeoutException e)
				{	logger.log("Could not download the page (timeout="+timeOut+" ms) >> trying again");
					timeOut = timeOut + 5000;
					again = timeOut<1*10*1000;	//TODO 2*60*1000;
				}
				catch(NoRouteToHostException e)
				{	logger.log(Arrays.asList(
						"WARNING: Could not download the page, the server seems to be offline.",
						"Error message: "+e.getMessage()
					));
				}
				catch(ConnectException e)
				{	logger.log(Arrays.asList(
						"WARNING: Could not download the page, the server seems to be offline.",
						"Error message: "+e.getMessage()
					));
				}
				catch(SocketException e)
				{	logger.log(Arrays.asList(
						"WARNING: Could not download the page, the server ended the file transmission.",
						"Error message: "+e.getMessage()
					));
				}
				catch(UnsupportedMimeTypeException e)
				{	logger.log(Arrays.asList(
						"WARNING: Could not download the page, the MIME format is not supported.",
						"Error message: "+e.getMessage()
					));
				}
				catch(HttpStatusException e)
				{	logger.log(Arrays.asList(
						"WARNING: Could not download the page, the server returned an error "+e.getStatusCode()+".",
						"Error message: "+e.getMessage()
					));
				}
				catch(UnknownHostException e)
				{	logger.log(Arrays.asList(
						"WARNING: Could not download the page, the IP address of the server could not be determined.",
						"Error message: "+e.getMessage()
					));
				}
				catch(SSLHandshakeException e)
				{	logger.log(Arrays.asList(
						"WARNING: Could not download the page, security error when connecting to the URL.",
						"Error message: "+e.getMessage()
					));
				}
				catch(SSLException e)
				{	logger.log(Arrays.asList(
						"WARNING: Could not download the page, security error when connecting to the URL.",
						"Error message: "+e.getMessage()
					));
				}
				catch(IOException e)
				{	logger.log(Arrays.asList(
						"WARNING: Could not download the page, general problem while accessing the webpage.",
						"Error message: "+e.getMessage()
					));
				}
			}
			while(again);
			logger.decreaseOffset();
			
			if(result!=null)
			{	logger.log("Page downloaded");
				String sourceCode = result.toString();
				
				// cache html source code
				FileTools.writeTextFile(originalFile, sourceCode, "UTF-8");
			}
		}

		//System.out.println(source.toString());
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Reads the source code of the Web page at the specified
	 * URL.
	 * 
	 * @param url
	 * 		Address of the Web page to be read.
	 * @return
	 * 		String containing the read HTML source code.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the specified URL.
	 */
	protected String manuallyReadUrl(URL url) throws IOException
	{	boolean trad = false;
		
		BufferedReader br = null;
		
		// open page the traditional way
		if(trad)
		{	InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
		}
		
		// open with more options
		else
		{	// setup connection
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(2000);
            connection.setChunkedStreamingMode(0);
            connection.setRequestProperty("Content-Length", "0");
//			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36");
            connection.connect();
            
            // setup input stream
            // part retrieved from http://stackoverflow.com/questions/538999/java-util-scanner-and-wikipedia
            // original author: Marco Beggio
            InputStream is = null;
            String encoding = connection.getContentEncoding();
            if(connection.getContentEncoding()!=null && encoding.equals("gzip"))
            {	is = new GZIPInputStream(connection.getInputStream());
            }
            else if (encoding != null && encoding.equals("deflate"))
            {	is = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
            }
            else
            {	is = connection.getInputStream();
            }
            
// alternative to spot error details            
//			InputStream is;
//			if (connection.getResponseCode() != 200) 
//				is = connection.getErrorStream();
//			else 
//				is = connection.getInputStream();
            
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
		}
		
		// read page
		StringBuffer sourceCode = new StringBuffer();
		String line = br.readLine();
		while (line != null)
		{	sourceCode.append(line+"\n");
			line = br.readLine();
		}
		
		String result = sourceCode.toString();
		br.close();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ELEMENTS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Retrieve the text located in a paragraph (P) HTML element.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 */
	protected void processParagraphElement(Element element, StringBuilder rawStr)
	{	// possibly add a new line character first (if the last one is not already a newline)
		if(rawStr.length()>0)
		{	char c = rawStr.charAt(rawStr.length()-1);
			if(c!='\n')
				rawStr.append("\n");
		}
		
		// recursive processing
		processAnyElement(element,rawStr);
		
		// possibly add a new line character (if the last one is not already a newline)
		if(rawStr.length()>0)
		{	char c = rawStr.charAt(rawStr.length()-1);
			if(c!='\n')
				rawStr.append("\n");
		}
	}

	/**
	 * Retrieve the text located in an offline quote (BLOCKQUOTE) HTML element.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	protected boolean processQuoteElement(Element element, StringBuilder rawStr)
	{	boolean result = true;
		
		// possibly modify the previous characters 
		if(rawStr.length()>0)
		{	char c = rawStr.charAt(rawStr.length()-1);
			if(c=='\n')
				rawStr.deleteCharAt(rawStr.length()-1);
		}
		
		// insert quotes
		rawStr.append(" \"");
		
		// recursive processing
		int rawIdx = rawStr.length();
		processAnyElement(element,rawStr);

		// possibly remove characters added after quote marks
		while(rawStr.length()>rawIdx && (rawStr.charAt(rawIdx)=='\n' || rawStr.charAt(rawIdx)==' '))
			rawStr.deleteCharAt(rawIdx);
		
		// possibly modify the ending characters 
		if(rawStr.length()>0)
		{	char c = rawStr.charAt(rawStr.length()-1);
			if(c=='\n')
				rawStr.deleteCharAt(rawStr.length()-1);
		}

		// insert quotes
		rawStr.append("\"");
		
		return result;
	}
	
	/**
	 * Retrieve the text located in a span (SPAN) HTML element.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	protected boolean processSpanElement(Element element, StringBuilder rawStr)
	{	boolean result = true;
		
		processAnyElement(element,rawStr);
		
		return result;
	}
	
	/**
	 * Retrieve the text located in a hyperlink (A) HTML element.
	 * <br/>
	 * We ignore links containing no text.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	protected boolean processHyperlinkElement(Element element, StringBuilder rawStr)
	{	boolean result = true;
			
		// simple text
		String str = element.text();
		if(!str.isEmpty())
		{	str = removeGtst(str);
			rawStr.append(str);
		}
		
		return result;
	}
	
	/**
	 * Retrieve the text located in an abbreviation (ABBR) HTML element.
	 * It is put between parenthesis.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	protected boolean processAbbreviationElement(Element element, StringBuilder rawStr)
	{	boolean result = true;
	
		// get the title element if it exists
		String title = element.attr(HtmlNames.ATT_TITLE);
		title = removeGtst(title);
		
		// get the text content (we suppose there's no complex content)
		String str = element.text();
		str = removeGtst(str);
		
		// complete the result texts
		if(str.isEmpty())
		{	if(title!=null)
				rawStr.append(title);
		}
		else
		{	rawStr.append(str);
			if(title!=null)
				rawStr.append(" ("+title+")");
		}
		
		return result;
	}
	
	/**
	 * Just inserts a space.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	@SuppressWarnings("unused")
	protected boolean processSpacerElement(Element element, StringBuilder rawStr)
	{	boolean result = true;
	
		rawStr.append(" ");
		
		return result;
	}
	
	/**
	 * Retrieve the text located in a list (UL or OL) HTML element.
	 * Note that if several levels of list exist, these are lost
	 * in the produced text.  
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @param ordered
	 * 		Whether the list is numbered or not.
	 */
	protected void processListElement(Element element, StringBuilder rawStr, boolean ordered)
	{	// possibly add a new line character right before
		if(rawStr.length()>0)
		{	char c = rawStr.charAt(rawStr.length()-1);
			if(c!='\n')
				rawStr.append("\n");
		}
		
		// process each list element
		int count = 1;
		for(Element listElt: element.getElementsByTag(HtmlNames.ELT_LI))
		{	// add leading marker
			if(ordered)
				rawStr.append(count+") ");
			else
				rawStr.append("- ");
			count++;
			
			// get text and links
			processAnyElement(listElt,rawStr);
			
			// possibly add a new line character
			if(rawStr.length()>0)
			{	char c = rawStr.charAt(rawStr.length()-1);
				if(c!='\n')
					rawStr.append("\n");
			}
		}
	}
	
	/**
	 * Retrieve the text located in a description list (DL) HTML element.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 */
	protected void processDescriptionListElement(Element element, StringBuilder rawStr)
	{	// possibly add a new line character right before
		if(rawStr.length()>0)
		{	char c = rawStr.charAt(rawStr.length()-1);
			if(c!='\n')
				rawStr.append("\n");
		}
		
		// process each list element
		Elements elements = element.children();
		Iterator<Element> it = elements.iterator();
		Element tempElt = null;
		if(it.hasNext())
			tempElt = it.next();
		while(tempElt!=null)
		{	// add leading mark
			rawStr.append("- ");
			
			// get term
			String tempName = tempElt.tagName();
			if(tempName.equalsIgnoreCase(HtmlNames.ELT_DT))
			{	// process term
				processAnyElement(tempElt,rawStr);
				
				// possibly add a column and space
				if(rawStr.length()>0)
				{	char c = rawStr.charAt(rawStr.length()-1);
					if(c!='.' && c!=':' && c!=';')
						rawStr.append(": ");
				}
				
				// go to next element
				if(it.hasNext())
					tempElt = it.next();
				else
					tempElt = null;
			}
			
			// get definition
			if(tempElt!=null)
			{	// process term
				processAnyElement(tempElt,rawStr);
				
				// possibly add a new line character
				if(rawStr.length()>0)
				{	char c = rawStr.charAt(rawStr.length()-1);
					if(c!='\n')
						rawStr.append("\n");
				}
				
				// go to next element
				if(it.hasNext())
					tempElt = it.next();
				else
					tempElt = null;
			}
		}
	}
	
	/**
	 * Retrieve the text located in a division (DIV) HTML element.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	protected boolean processDivisionElement(Element element, StringBuilder rawStr)
	{	boolean result = true;
		
		processParagraphElement(element, rawStr);
		
		return result;
	}
	
	/**
	 * Just inserts a line break in both raw and linked texts.
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	@SuppressWarnings("unused")
	protected boolean processLinebreakElement(Element element, StringBuilder rawStr)
	{	boolean result = true;
		
		rawStr.append("\n");
		
		return result;
	}
	
	/**
	 * Retrieve the text located in a table (TABLE) HTML element.
	 * <br/>
	 * We process each cell in the table as a text element. 
	 * 
	 * @param element
	 * 		Element to be processed.
	 * @param rawStr
	 * 		Current raw text string.
	 * @return
	 * 		{@code true} iff the element was processed.
	 */
	protected boolean processTableElement(Element element, StringBuilder rawStr)
	{	boolean result = true;
		
		// extract the list of rows (we don't need the rest)
		Elements children = element.children();
		List<Element> rowElts = new ArrayList<Element>(); 
		for(Element child: children)
		{	String name = child.tagName();
			
			// if there's a caption, put it first
			if(name.equalsIgnoreCase(HtmlNames.ELT_CAPTION))
			{	processAnyElement(child, rawStr);
			}
			
			// if the table has a header/body/footer, extract the rows
			else if(name.equalsIgnoreCase(HtmlNames.ELT_THEAD)
				|| name.equalsIgnoreCase(HtmlNames.ELT_TBODY)
				|| name.equalsIgnoreCase(HtmlNames.ELT_TFOOT))
			{	Elements children2 = child.children();
				rowElts.addAll(children2);
			}
			
			// otherwise, just get the rows
			else if(name.equalsIgnoreCase(HtmlNames.ELT_TR))
				rowElts.add(child);
		}
		
		// extract the text from each row
		for(Element rowElt: rowElts)
		{	// process each column
			for(Element colElt: rowElt.children())
			{	// process cell content
				processAnyElement(colElt, rawStr);
				
				// possibly add final dot and space. 
				if(rawStr.length()>0)
				{	char c = rawStr.charAt(rawStr.length()-1);
					if(c!=' ')
					{	if(rawStr.charAt(rawStr.length()-1)=='.')
							rawStr.append(" ");
						else
							rawStr.append(". ");
					}
				}
			}
			
			// add new line
			rawStr.append("\n");
		}
		
		return result;
	}
	
	/**
	 * Generic method designed to process any HTML element.
	 * 
	 * @param textElement
	 * 		The element to be processed.
	 * @param rawStr
	 * 		The StringBuffer to contain the raw text.
	 */
	protected void processAnyElement(Element textElement, StringBuilder rawStr)
	{	// we process each element contained in the specified text element
		for(Node node: textElement.childNodes())
		{	// element node
			if(node instanceof Element)
			{	Element element = (Element) node;
				String eltName = element.tag().getName();
				
				// hyperlinks: must be included in the linked version of the article
				if(eltName.equalsIgnoreCase(HtmlNames.ELT_A))
				{	processHyperlinkElement(element,rawStr);
				}
				
				// abbreviations and acronyms: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_ABBR) || eltName.equalsIgnoreCase(HtmlNames.ELT_ACRONYM))
				{	processAbbreviationElement(element,rawStr);
				}
				
				// author's address: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_ADDRESS))
				{	// we could try to use that to retrieve the authors' names
					// but this seems too troublesome, because the content is not structured at all
				}
				
				// applet: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_APPLET))
				{	// nothing to do here
				}
				
				// image zone: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_AREA))
				{	// nothing to do here
				}
				
				// article: considered as a div
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_ARTICLE))
				{	processDivisionElement(element, rawStr);
				}
				
				// aside: should be ignored, since it is secondary content
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_ASIDE))
				{	// nothing to do here
				}
				
				// audio: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_AUDIO))
				{	// nothing to do here
				}
				
				// bold: just some text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_B)) //TODO 10
				{	processAnyElement(element, rawStr);
				}
				
				// base: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_BASE))
				{	// nothing to do here
				}
				
				// basefont: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_BASEFONT))
				{	// nothing to do here
				}
				
				// text orientation: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_BDI) || eltName.equalsIgnoreCase(HtmlNames.ELT_BDO))
				{	processAnyElement(element, rawStr);
				}
				
				// big: just some text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_BIG))
				{	processAnyElement(element, rawStr);
				}
				
				// blinking text: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_BLINK))
				{	processAnyElement(element, rawStr);
				}
				
				// quotes: processed recursively
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_BLOCKQUOTE) || eltName.equalsIgnoreCase(HtmlNames.ELT_QUOTE) || eltName.equalsIgnoreCase(HtmlNames.ELT_Q))
				{	processQuoteElement(element,rawStr);
				}
				
				// document body: considered as a div
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_BODY)) //TODO 20
				{	processDivisionElement(element, rawStr);
				}
				
				// line break: insert a newline
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_BR))
				{	processLinebreakElement(element, rawStr);
				}
				
				// form button: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_BUTTON))
				{	// nothing to do
				}
				
				// graphic canvas: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_CANVAS))
				{	// nothing to do
				}

				// table caption: like a paragraph
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_CAPTION))
				{	processParagraphElement(element, rawStr);
				}

				// center: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_CENTER))
				{	// nothing to do
				}
				
				// citation or title of a work: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_CITE))
				{	processAnyElement(element, rawStr);
				}
				
				// source code: we don't want that here
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_CODE))
				{	// nothing to do
				}
				
				// column properties: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_COL) || eltName.equalsIgnoreCase(HtmlNames.ELT_COLGROUP))
				{	// nothing to do
				}
				
				// Web component content: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_CONTENT)) //TODO 30
				{	// nothing to do
				}
				
				// structured data: just get the text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DATA))
				{	processAnyElement(element, rawStr);
				}
				
				// input options: no use for us
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DATALIST))
				{	// nothing to do
				}
				
				// definition in a definition list: already processed in DL
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DD))
				{	// nothing to do
				}
				
				// Web component decorator: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DECORATOR))
				{	// nothing to do
				}
				
				// deleted text: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DEL))
				{	// nothing to do
				}
				
				// details (hide/show): just get the text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DETAILS))
				{	processAnyElement(element, rawStr);
				}
				
				// term definition: like an abbreviation
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DFN))
				{	processAbbreviationElement(element, rawStr);
				}
				
				// dialog box: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DIALOG))
				{	// nothing to do
				}
				
				// directory list: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DIR))
				{	// nothing to do
				}
				
				// division: processed recursively
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DIV)) //TODO 40
				{	processDivisionElement(element,rawStr);
				}
				
				// definition list: process each item
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DL))
				{	processDescriptionListElement(element,rawStr);
				}
				
				// term in a definition list: already processed in DL
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_DT))
				{	// nothing to do
				}
				
				// emphasis: just some text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_EM))
				{	processAnyElement(element, rawStr);
				}
				
				// element: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_ELEMENT))
				{	// nothing to do
				}
				
				// embedded application: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_EMBED))
				{	// nothing to do
				}
				
				// form groups: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_FIELDSET))
				{	// nothing to do
				}
				
				// figure caption: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_FIGCAPTION))
				{	// nothing to do
				}
				
				// figure: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_FIGURE))
				{	// nothing to do
				}
				
				// font: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_FONT))
				{	processAnyElement(element, rawStr);
				}
				
				// footer: treated like a div
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_FOOTER)) //TODO 50
				{	processDivisionElement(element, rawStr);
					//TODO or maybe should be ignored...
				}
				
				// form: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_FORM))
				{	// nothing to do
				}
				
				// frame/frameset: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_FRAME) || eltName.equalsIgnoreCase(HtmlNames.ELT_FRAMESET))
				{	// nothing to do
				}
				
				// section headers: treated like paragraphs
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_H1) || eltName.equalsIgnoreCase(HtmlNames.ELT_H2) || eltName.equalsIgnoreCase(HtmlNames.ELT_H3)
					|| eltName.equalsIgnoreCase(HtmlNames.ELT_H4) || eltName.equalsIgnoreCase(HtmlNames.ELT_H5) || eltName.equalsIgnoreCase(HtmlNames.ELT_H6))
				{	processParagraphElement(element,rawStr);
				}
				
				// head: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_HEAD)) //TODO 60
				{	// nothing to do
				}
				
				// section header: treated like a div
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_HEADER))
				{	processDivisionElement(element, rawStr);
					//TODO or maybe should be ignored...
				}
				
				// title group: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_HGROUP))
				{	// nothing to do
				}
				
				// thematic break: insert a newline
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_HR))
				{	processLinebreakElement(element, rawStr);
				}
				
				// document: treat like a div
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_HTML))
				{	processDivisionElement(element, rawStr);
				}

				// italic: just some text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_I))
				{	processAnyElement(element, rawStr);
				}

				// inline frame: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_IFRAME))
				{	// nothing to do
				}
				
				// image: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_IMG))
				{	// nothing to do
				}
				
				// input control: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_INPUT))
				{	// nothing to do
				}
				
				// inserted text: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_INS))
				{	processAnyElement(element, rawStr);
				}
				
				// input text: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_ISINDEX)) //TODO 70
				{	// nothing to do
				}
				
				// keyboard input: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_KBD))
				{	// nothing to do
				}
				
				// keygen form field: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_KEYGEN))
				{	// nothing to do
				}
				
				// input label: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_LABEL))
				{	// nothing to do
				}
				
				// fieldset legend: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_LEGEND))
				{	// nothing to do
				}
				
				// list item: already processed in OL/UL
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_LI))
				{	// nothing to do
				}
	
				// stylesheet link: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_LINK))
				{	// nothing to do
				}
				
				// listing: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_LISTING))
				{	// nothing to do
				}
				
				// main content: treat like a div
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_MAIN))
				{	processDivisionElement(element, rawStr);
				}
				
				// image map: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_MAP))
				{	// nothing to do
				}
				
				// marked text: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_MARK)) //TODO 80
				{	processAnyElement(element, rawStr);
				}
				
				// menu & menuitem: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_MENU) || eltName.equalsIgnoreCase(HtmlNames.ELT_MENUITEM))
				{	// nothing to do
				}
				
				// document metadata: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_META))
				{	// nothing to do
				}
				
				// form meter: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_METER))
				{	// nothing to do
				}
				
				// navigation links: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_NAV))
				{	// nothing to do
				}
				
				// no frames alternative: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_NOFRAMES))
				{	processAnyElement(element, rawStr);
				}
				
				// no embed alternative: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_NOEMBED))
				{	processAnyElement(element, rawStr);
				}
				
				// no script alternative: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_NOSCRIPT))
				{	processAnyElement(element, rawStr);
				}
				
				// multimedia objects: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_OBJECT))
				{	// nothing to do
				}
				
				// various list types
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_OL)) //TODO 90
				{	processListElement(element,rawStr,true);
				}

				// form options: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_OPTGROUP) || eltName.equalsIgnoreCase(HtmlNames.ELT_OPTION))
				{	// nothing to do
				}
				
				// output: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_OUTPUT))
				{	// nothing to do
				}
				
				// paragraph: processed recursively
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_P))
				{	processParagraphElement(element,rawStr);
				}
				
				// object parameter: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_PARAM))
				{	// nothing to do
				}
				
				// plain text: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_PLAINTEXT) || eltName.equalsIgnoreCase(HtmlNames.ELT_PRE))
				{	processAnyElement(element, rawStr);
				}
				
				// progress bar: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_PROGRESS))
				{	// nothing to do
				}
				
				// ruby stuff: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_RP) || eltName.equalsIgnoreCase(HtmlNames.ELT_RT)  //TODO 100
					|| eltName.equalsIgnoreCase(HtmlNames.ELT_RTC) || eltName.equalsIgnoreCase(HtmlNames.ELT_RUBY))
				{	// nothing to do
				}
				
				// strikethrough: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_S) || eltName.equalsIgnoreCase(HtmlNames.ELT_STRIKE))
				{	// nothing to do
				}
				
				// sample output of computer program: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SAMP))
				{	// nothing to do
				}
				
				// script: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SCRIPT))
				{	// nothing to do
				}
				
				// section: treated as a div
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SECTION))
				{	processDivisionElement(element, rawStr);
				}
				
				// form drop-down list: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SELECT))
				{	// nothing to do
				}
				
				// small: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SMALL))
				{	processAbbreviationElement(element, rawStr);
				}
				
				// audio source: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SOURCE)) //TODO 110
				{	// nothing to do
				}
				
				// spacer: just put a space
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SPACER))
				{	processSpacerElement(element, rawStr);
				}
				
				// span: processed recursively
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SPAN))
				{	processSpanElement(element,rawStr);
				}
				
				// strong: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_STRONG))
				{	processAnyElement(element, rawStr);
				}
				
				// document style: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_STYLE))
				{	// nothing to do
				}
				
				// sub/superscripts: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SUB) || eltName.equalsIgnoreCase(HtmlNames.ELT_SUP))
				{	// nothing to do here
				}
				
				// details summary: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_SUMMARY))
				{	// nothing to do
				}
				
				// table: approximately represented as text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_TABLE))
				{	processTableElement(element, rawStr);
				}
				
				// table-related elements: already processed in the table method
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_THEAD) || eltName.equalsIgnoreCase(HtmlNames.ELT_TBODY) || eltName.equalsIgnoreCase(HtmlNames.ELT_TFOOT) //TODO 121
					|| eltName.equalsIgnoreCase(HtmlNames.ELT_TH)|| eltName.equalsIgnoreCase(HtmlNames.ELT_TR)|| eltName.equalsIgnoreCase(HtmlNames.ELT_TD))
				{	// nothing to do
				}
				
				// template: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_TEMPLATE))
				{	// nothing to do
				}
				
				// input text area: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_TEXTAREA))
				{	// nothing to do
				}
				
				// time/date: text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_TIME))
				{	processAnyElement(element, rawStr);
				}
				
				// title: treated like a pargraph
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_TITLE))
				{	processParagraphElement(element, rawStr);
				}
				
				// media track: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_TITLE))
				{	// nothing to do
				}
				
				// teletype text: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_TT)) //TODO 130
				{	processAnyElement(element, rawStr);
				}
				
				// special formatting: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_U))
				{	processAnyElement(element, rawStr);
				}
				
				// unordered list: each item is processed
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_UL))
				{	processListElement(element,rawStr,false);
				}
				
				// variable definition: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_VAR))
				{	processAnyElement(element, rawStr);
				}
				
				// video: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_VIDEO))
				{	// nothing to do
				}
				
				// word break opportuinies: just text
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_WBR))
				{	processAnyElement(element, rawStr);
				}
				
				// xmp: ignored
				else if(eltName.equalsIgnoreCase(HtmlNames.ELT_XMP)) //TODO 136
				{	// nothing to do
				}
				
				// no other elements should be encountered 
				else
				{	//throw new IllegalArgumentException("Unexpected HTML element <"+eltName+">");
					logger.log("WARNING: Unexpected HTML element <"+eltName+">");
					String text = element.text();
					text = removeGtst(text);
					rawStr.append(text);
				}
			}
			
			// text node
			else if(node instanceof TextNode)
			{	// get the text
				TextNode textNode = (TextNode) node;
				String text = textNode.text();
				text = removeGtst(text);
				
				// the text but must non-empty, and contains something else than spaces
				if(!text.trim().isEmpty())
				{	// if at the beginning of a new line, or already preceded by a space, remove leading spaces
					while(rawStr.length()>0 
							&& (rawStr.charAt(rawStr.length()-1)=='\n' || rawStr.charAt(rawStr.length()-1)==' ') 
							&& text.startsWith(" "))
						text = text.substring(1);
					
					// complete string buffers
					rawStr.append(text);
				}
			}
		}
	}
}
