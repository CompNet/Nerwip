package fr.univavignon.search.results;

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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.tools.strings.CommonStringTools;
import fr.univavignon.search.events.Event;
import fr.univavignon.search.events.ReferenceEvent;
import fr.univavignon.search.tools.files.SearchFileNames;

/**
 * Represents one result of a social search engine and some info 
 * regarding how it was subsequently processed.
 * 
 * @author Vincent Labatut
 */
public class SocialSearchResult extends AbstractSearchResult
{
	/**
	 * Initializes the social search result.
	 * 
	 * @param id
	 * 		Unique id of this post. 
	 * @param author
	 * 		Author of this post. 
	 * @param date 
	 * 		Date of publication of this post.
	 * @param source
	 * 		Name of the social media publishing this post.
	 * @param content 
	 * 		Textual content of this post.
	 * @param original
	 * 		Whether the post was written by the targeted author, 
	 * 		or by one of the commenters.
	 */
	public SocialSearchResult(String id, String author, Date date, String source, String content, boolean original)
	{	this.id = id;
		this.author = author;
		this.date = date;
		this.source = source;
		this.content = content;
		this.original = original;
	}
	
	/////////////////////////////////////////////////////////////////
	// KEY			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getKey()
	{	String result = id + "@" + source;
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ID			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Unique ID of the post */
	public String id;
	
	/////////////////////////////////////////////////////////////////
	// ORIGINAL		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether the post was written by the targeted author, or by one of the commenters */
	public boolean original = false;
	
	/**
	 * Adds the original flag of this post to the specified map.
	 * 
	 * @param result
	 * 		Map to fill with the required field.
	 */
	private void exportOriginal(Map<String,String> result)
	{	result.put(AbstractSearchResults.COL_ORIGINAL,Boolean.toString(original));
	}
	
	/////////////////////////////////////////////////////////////////
	// CONTENT		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Textual content of this post */
	public String content = null;
	
	/**
	 * Adds the content of this post to the specified map.
	 * 
	 * @param result
	 * 		Map to fill with the required field.
	 */
	private void exportContent(Map<String,String> result)
	{	if(content!=null)
			result.put(AbstractSearchResults.COL_CONTENT,content);
	}
	
	/////////////////////////////////////////////////////////////////
	// AUTHOR		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the author of this post */
	public String author = null;
	
	/////////////////////////////////////////////////////////////////
	// DATE			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Publication date of this post */
	public Date date = null;
	
	/////////////////////////////////////////////////////////////////
	// SOURCE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the social media on which this post has been published */
	public String source = null;
	
	/**
	 * Adds the social media of this post to the specified map.
	 * 
	 * @param result
	 * 		Map to fill with the required field.
	 */
	private void exportSource(Map<String,String> result)
	{	if(source!=null)
		{	result.put(AbstractSearchResults.COL_SOCIAL_ENGINE,source);
			result.put(AbstractSearchResults.COL_SOURCE,source);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// LIKES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of likes for this post */
	public Integer likes = null;
	
	/**
	 * Adds the number of likes of this post to the specified map.
	 * 
	 * @param result
	 * 		Map to fill with the required field.
	 */
	private void exportLikeNumber(Map<String,String> result)
	{	if(likes!=null)
		result.put(AbstractSearchResults.COL_LIKES,Integer.toString(likes));
	}
	
	/////////////////////////////////////////////////////////////////
	// SHARES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of shares for this post */
	public Integer shares = null;
	
	/**
	 * Adds the number of shares of this post to the specified map.
	 * 
	 * @param result
	 * 		Map to fill with the required field.
	 */
	private void exportShareNumber(Map<String,String> result)
	{	if(shares!=null)
			result.put(AbstractSearchResults.COL_SHARES,Integer.toString(shares));
	}
	
	/////////////////////////////////////////////////////////////////
	// RANK			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Rank of the result according to the source */
	public int rank = -1;
	
	/////////////////////////////////////////////////////////////////
	// COMMENTS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Posts commenting this post */ 
	public List<SocialSearchResult> comments = new ArrayList<SocialSearchResult>();
	
	/**
	 * Adds the number of comments of this post to the specified map.
	 * 
	 * @param result
	 * 		Map to fill with the required field.
	 */
	private void exportCommentNumber(Map<String,String> result)
	{	int commentNbr = comments.size();
		result.put(AbstractSearchResults.COL_COMMENTS,Integer.toString(commentNbr));
	}
	
	/////////////////////////////////////////////////////////////////
	// URL			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Direct URL of the post */ 
	public URL url;
	
	/**
	 * Adds the url of this post to the specified map.
	 * 
	 * @param result
	 * 		Map to fill with the required field.
	 */
	private void exportUrl(Map<String,String> result)
	{	String urlStr = null;
		if(url!=null)
			urlStr = url.toString();
		else if(article!=null)
		{	URL url = article.getUrl();
			if(url!=null)
				urlStr = url.toString();
		}
		
		if(urlStr!=null)
			result.put(AbstractSearchResults.COL_URL,urlStr);

		result.put(AbstractSearchResults.COL_URL_ID, id);
	}

	/////////////////////////////////////////////////////////////////
	// ARTICLE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Converts this post under the form of a proper {@code Article}. 
	 * The comments can be added as paragraphs after the actual post content
	 * depending on the {@code includeComments} parameter.
	 * 
	 * @param includeComments 
	 * 		Whether ({@code true}) or not ({@code false}) to include comments 
	 * 		in the proper article (or just the main post).
	 * 
	 * @throws IOException 
	 * 		Problem while recording the article.
	 */
	public void buildArticle(boolean includeComments) throws IOException
	{	String corpusFolder = SearchFileNames.FO_SOCIAL_SEARCH_RESULTS + File.separator + source;
		article = new Article(id, corpusFolder);

		// content
		String text = content;
		if(includeComments)
		{	for(SocialSearchResult com: comments)
				text = text + "\n\n" + com.content;
		}
		article.setRawText(text);
		article.cleanContent();
		
		// metadata
		article.setTitle(id);
		if(author!=null)
			article.addAuthor(author);
		ArticleLanguage language = CommonStringTools.detectLanguage(text,true);
//		if(language==null)
//			language = ArticleLanguage.FR;
		article.setLanguage(language);
		article.setPublishingDate(date);
		Calendar cal = Calendar.getInstance();
		Date currentDate = cal.getTime();
		article.setRetrievalDate(currentDate);
		article.setUrl(url);
		
		// record to file
		article.write();
	}
	
	@Override
	protected boolean filterByKeyword(String compulsoryExpression, int nbr)
	{	boolean result = true;
		
		logger.log("Processing article "+article.getTitle()+" ("+nbr+")");
		logger.increaseOffset();
		{	// filter only if the article was not authored by the target
			if(!original)
			{	String text = article.getRawText().toLowerCase(Locale.ENGLISH);
				String expr = compulsoryExpression.toLowerCase(Locale.ENGLISH);
				Pattern pattern = Pattern.compile("\\b"+expr+"\\b");
		        Matcher matcher = pattern.matcher(text);
		        if(!matcher.find())
				{	logger.log("Discarding article "+article.getTitle()+" ("+article.getUrl()+")");
					status = STATUS_MISSING_KEYWORD;
					result = false;
				}
			}
			else
				logger.log("This article was written by the targeted person, and we therefore keep it");
		}
		logger.decreaseOffset();
			
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Date format used to write/read comments in text files */
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	/** 
	 * Write this post (and its comments) to 
	 * a text file.
	 * 
	 * @param writer
	 * 		Writer on the currently open text file.
	 */
	public void writeAsText(PrintWriter writer)
	{	writer.println(id);
		writer.println(author);
		writer.println(DATE_FORMAT.format(date));
		writer.println(source);
		writer.println(original);
		if(likes!=null)
			writer.println(likes);
		else
			writer.println();
		if(shares!=null)
			writer.println(shares);
		else
			writer.println();
		writer.println(content);
		if(url==null)
			writer.println("N/A");
		else
			writer.println(url.toString());
		writer.println(comments.size());
		for(SocialSearchResult comment: comments)
			comment.writeAsText(writer);
	}
	
	/**
	 * Retrieve a post from a text file, as well as its comments
	 * (if any).
	 * 
	 * @param scanner
	 * 		Scanner on the previously opened text file.
	 * @return
	 * 		An object representing the post read from the text file.
	 */
	public static SocialSearchResult readFromText(Scanner scanner)
	{	SocialSearchResult result = null;
	
		try
		{	// init the post
			String id = scanner.nextLine().trim();
			String author = scanner.nextLine().trim();
			String dateStr = scanner.nextLine().trim();
			Date date = DATE_FORMAT.parse(dateStr);
			String source = scanner.nextLine().trim();
			String originalStr = scanner.nextLine().trim();
			boolean original = Boolean.parseBoolean(originalStr);
			String likesStr = scanner.nextLine().trim();
			Integer likes = null;
			if(!likesStr.isEmpty())
				likes = Integer.parseInt(likesStr);
			String sharesStr = scanner.nextLine().trim();
			Integer shares = null;
			if(!sharesStr.isEmpty())
				shares = Integer.parseInt(sharesStr);
			String content = scanner.nextLine().trim();
			result = new SocialSearchResult(id, author, date, source, content, original);
			String urlStr = scanner.nextLine().trim();
			if(!urlStr.equals("N/A"))
			{	URL url = new URL(urlStr);
				result.url = url;
			}
			
			// add its comments
			String nbrStr = scanner.nextLine().trim();
			int nbr = Integer.parseInt(nbrStr);
			for(int i=0;i<nbr;i++)
			{	SocialSearchResult comment = readFromText(scanner);
				result.comments.add(comment);
			}
			
			// add its share/like counts
			result.likes = likes;
			result.shares = shares;
		}
		catch(ParseException e)
		{	e.printStackTrace();
		} 
		catch(MalformedURLException e) 
		{	e.printStackTrace();
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CSV			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Map<String, String> exportResult(Map<String,List<ReferenceEvent>> referenceClusters, Date startDate, Date endDate) 
	{	Map<String,String> result = new HashMap<String,String>();
	
		// title 
		exportTitle(result);
		// status
		exportStatus(result);
		// publication date
		exportPublicationDate(result);
		// content
		exportContent(result);
		// likes
		exportLikeNumber(result);
		// shares
		exportShareNumber(result);
		// comments
		exportCommentNumber(result);
		// author(s)
		exportAuthors(result);
		// original flag
		exportOriginal(result);
		// social media engine
		exportSource(result);
		// length
		exportLength(result);
		// article cluster
		exportCluster(result, referenceClusters, startDate, endDate);
		
		// mentions
		exportMentions(result, EntityType.DATE);
		exportMentions(result, EntityType.FUNCTION);
		exportMentions(result, EntityType.LOCATION);
		exportMentions(result, EntityType.MEETING);
		exportMentions(result, EntityType.ORGANIZATION);
		exportMentions(result, EntityType.PERSON);
		exportMentions(result, EntityType.PRODUCTION);
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// EVENTS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected int extractEvents(boolean bySentence, int nbr)
	{	int result = extractEvents(bySentence,nbr,true);
		return result;
	}
	
	/**
	 * Returns a map of strings used above to record the results of 
	 * the social search as a CSV file.
	 * 
	 * @return
	 * 		Map representing the events associated to this social
	 * 		search result (can be empty). 
	 */
	@Override
	protected List<Map<String,String>> exportEvents(Map<String,List<ReferenceEvent>> referenceClusters, Date startDate, Date endDate)
	{	List<Map<String,String>> result = new ArrayList<Map<String,String>>();
		
		int rank = 0;
		for(Event event: events)
		{	Map<String,String> map = new HashMap<String,String>();
			result.add(map);
			rank++;

			// title
			exportTitle(map);
			// url
			exportUrl(map);
			// length
			exportLength(map);
			// author(s)
			exportAuthors(map);
			// original or not
			exportOriginal(map);
			// status
			exportStatus(map);
			// date
			exportPublicationDate(map);
			// article cluster
			exportCluster(map, referenceClusters, startDate, endDate);
			// search engine
			exportSource(map);
			// number of likes
			exportLikeNumber(map);
			// number of shares
			exportShareNumber(map);
			// number of comments
			exportCommentNumber(map);
			// event and its stuff
			exportEvent(event, rank, map);
		}
		
		return result;
	}
}
