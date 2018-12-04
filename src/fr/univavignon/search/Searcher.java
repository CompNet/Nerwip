package fr.univavignon.search;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.search.engines.social.AbstractSocialEngine;
import fr.univavignon.search.engines.social.FacebookEngine;
import fr.univavignon.search.engines.web.AbstractWebEngine;
import fr.univavignon.search.engines.web.BingEngine;
import fr.univavignon.search.engines.web.GoogleEngine;
import fr.univavignon.search.engines.web.QwantEngine;
import fr.univavignon.search.engines.web.YandexEngine;
import fr.univavignon.search.events.ReferenceEvent;
import fr.univavignon.search.results.AbstractSearchResults;
import fr.univavignon.search.results.CombinedSearchResults;
import fr.univavignon.search.results.SocialSearchResult;
import fr.univavignon.search.results.SocialSearchResults;
import fr.univavignon.search.results.WebSearchResults;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.combiner.straightcombiner.StraightCombiner;
import fr.univavignon.retrieval.ReaderException;
import fr.univavignon.search.tools.files.SearchFileNames;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

/**
 * This class handles the main search, i.e. it :
 * <ol>
 * 	<li>Search: determines which articles are relevant, using one or several Web search engines.</li>
 * 	<li>Retrieval: retrieves them using our article reader.</li>
 * 	<li>Detection: detects the named entities they mention.</li>
 * 	<li>Save: records the corresponding events.</li>
 * <ol>
 * 
 * @author Vincent Labatut
 */
public class Searcher
{	
	/**
	 * Builds and initializes an extractor object,
	 * using the default parameters. 
	 * <br/>
	 * Override/modify the methods called here, 
	 * in order to change these parameters.
	 * 
	 * @throws ProcessorException
	 * 		Problem while initializing the NER tool. 
	 */
	public Searcher() throws ProcessorException
	{	initRecognizer();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	public static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Launches the main search.
	 * 
	 * @param keywords
	 * 		Person we want to look up.
	 * @param websites
	 * 		List of targeted sites, including {@ode null} to search the whole Web.
	 * @param additionalSeeds
	 * 		List of secondary pages to process during the search on social media. 
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * @param filterByPubDate
	 * 		Whether or not to filter articles depending on their publication date.
	 * @param filterByEntDate
	 * 		Whether or not to filter articles depending on the dates they contain.
	 * @param compulsoryExpression
	 * 		String expression which must be present in the article,
	 * 		or {@code null} if there's no such constraint.
	 * @param doExtendedSocialSearch
	 * 		Whether the social media search should retrieve the posts published by the
	 * 		users commenting the posts of interest, for the considered period. If 
	 * 		{@code false}, only the posts on the targeted page and their direct comments
	 * 		are returned. 
	 * @param language
	 * 		Language targeted during the search.
	 * 
	 * @throws IOException 
	 * 		Problem accessing the Web or a file.
	 * @throws SAXException 
	 * 		Problem while retrieving a Web page.
	 * @throws ParseException 
	 * 		Problem while retrieving a Web page.
	 * @throws ReaderException 
	 * 		Problem while retrieving a Web page.
	 * @throws ProcessorException 
	 * 		Problem while detecting the entity mentions.
	 */
	public void performExtraction(String keywords, List<String> websites, List<String> additionalSeeds, Date startDate, Date endDate, boolean filterByPubDate, boolean filterByEntDate, String compulsoryExpression, boolean doExtendedSocialSearch, ArticleLanguage language) throws IOException, ReaderException, ParseException, SAXException, ProcessorException
	{	logger.log("Starting the information extraction");
		logger.increaseOffset();
		
		// setup the output folder
		String outFolder = keywords.replace(' ','_');
		if(websites.size()==1)
		{	String website = websites.get(0);
			if(websites.get(0)!=null)
			{	URL url = new URL(website);
				String host = url.getHost();
				outFolder = outFolder + "_" + host;
			}
		}
		SearchFileNames.setSearchFolder(outFolder);
		
		// retrieve the reference events (if any)
		logger.log("Load reference events (if any)");
		Map<Integer,ReferenceEvent> referenceEvents = loadReferenceEvents();
		
		// perform the Web search
		logger.log("Performing the Web search");
		WebSearchResults webRes = null;
		webRes = performWebExtraction(keywords, websites, startDate, endDate, filterByPubDate, filterByEntDate, compulsoryExpression, language, referenceEvents);
		
		// perform the social search
		logger.log("Performing the social media search");
		SocialSearchResults socialRes = null;
		socialRes = performSocialExtraction(keywords, additionalSeeds, startDate, endDate, compulsoryExpression, doExtendedSocialSearch, language, referenceEvents);
		
		// merge results and continue processing
		logger.log("Merging web and social media results");
		combineResults(webRes, socialRes, language, startDate, endDate);
		
		logger.decreaseOffset();
		logger.log("Information extraction over");
	}

	/////////////////////////////////////////////////////////////////
	// WEB SEARCH			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of engines used for the Web search */
	private final List<AbstractWebEngine> webEngines = new ArrayList<AbstractWebEngine>();
	
	/**
	 * Initializes the default search engines.
	 * Currently: Google, Bing, Qwant, Yandex.
	 * 
	 * @param websites
	 * 		List of target websites, can contain {@code null} to search the whole Web.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * @param language
	 * 		Targeted language. 
	 */
	private void initWebSearchEngines(List<String> websites, Date startDate, Date endDate, ArticleLanguage language)
	{	logger.log("Initializing the Web search engines");
		logger.increaseOffset();
		
		// nullify dates if the search is not strict
		if(startDate==null)
			endDate = null;
		
		// iterate over each website
		int i = 1;
		for(String website: websites)
		{	logger.log("Website "+website+" ("+i+"/"+websites.size()+")");
			i++;
			
			// set up the Google
			GoogleEngine googleEngine = new GoogleEngine(website,startDate,endDate);
			webEngines.add(googleEngine);
			
			// set up Bing
			BingEngine bingEngine = new BingEngine(website,startDate,endDate,language);
			webEngines.add(bingEngine);
			
			// set up Qwant
			QwantEngine qwantEngine = new QwantEngine(website,startDate,endDate, language);
			webEngines.add(qwantEngine);
			
			// set up Yandex
			YandexEngine yandexEngine = new YandexEngine(website,startDate,endDate, language);
			webEngines.add(yandexEngine);
		}
		
		logger.decreaseOffset();
	}
	
	/**
	 * Performs the Web search using the specified parameters and
	 * each one of the engines registered in the {@link #webEngines}
	 * list.
	 * 
	 * @param keywords
	 * 		Person we want to look for.
	 * @param referenceEvents
	 * 		Map containing the reference events, or
	 * 		empty if no such events could be found.
	 * @return
	 * 		List of web search results.
	 * 
	 * @throws IOException
	 * 		Problem accessing the Web.
	 */
	private WebSearchResults performWebSearch(String keywords, Map<Integer,ReferenceEvent> referenceEvents) throws IOException
	{	WebSearchResults result = new WebSearchResults(referenceEvents);
		
		// apply each search engine
		logger.log("Applying iteratively each search engine");
		logger.increaseOffset();
		for(AbstractWebEngine engine: webEngines)
		{	logger.log("Processing search engine "+engine);
			logger.increaseOffset();
				Map<String,URL> urls = engine.retrieveResults(keywords);
			
//				// sort the URL keys
//				TreeSet<String> keys = new TreeSet<String>(KEY_COMPARATOR);
//				keys.addAll(urls.keySet());
				
				// add to the overall map of URLs
				String engineStr = engine.toString();
				for(Entry<String,URL> entry: urls.entrySet())
				{	String rank = entry.getKey();
					URL url = entry.getValue();
					String urlStr = url.toString();
					result.addResult(urlStr, engineStr, rank);
				}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		logger.log("Total number of pages found: "+result.size());
		
		return result;
	}
	
	/**
	 * Launches the main search.
	 * 
	 * @param keywords
	 * 		Person we want to look up.
	 * @param websites
	 * 		List of targeted sites, including {@ode null} to search the whole Web.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * @param filterByPubDate
	 * 		Whether or not to filter articles depending on their publication date.
	 * @param filterByEntDate
	 * 		Whether or not to filter articles depending on the dates they contain.
	 * @param compulsoryExpression
	 * 		String expression which must be present in the article,
	 * 		or {@code null} if there is no such constraint.
	 * @param language
	 * 		Language targeted during the search.
	 * @param referenceEvents
	 * 		Map containing the reference events, or
	 * 		empty if no such events could be found.
	 * @return
	 * 		The Web search results.
	 * 
	 * @throws IOException 
	 * 		Problem accessing the Web or a file.
	 * @throws SAXException 
	 * 		Problem while retrieving a Web page.
	 * @throws ParseException 
	 * 		Problem while retrieving a Web page.
	 * @throws ReaderException 
	 * 		Problem while retrieving a Web page.
	 * @throws ProcessorException 
	 * 		Problem while detecting the entity mentions.
	 */
	private WebSearchResults performWebExtraction(String keywords, List<String> websites, Date startDate, Date endDate, boolean filterByPubDate, boolean filterByEntDate, String compulsoryExpression, ArticleLanguage language, Map<Integer,ReferenceEvent> referenceEvents) throws IOException, ReaderException, ParseException, SAXException, ProcessorException
	{	logger.log("Starting the web extraction");
		logger.increaseOffset();
			int currentStep = 1;
			String fileName;
			
			// log search parameters
			logger.log("Parameters:");
			logger.increaseOffset();
				logger.log("keywords="+keywords);
				logger.log("startDate="+startDate);
				logger.log("endDate="+endDate);
				logger.log("filterByPubDate="+filterByPubDate);
				logger.log("filterByEntDate="+filterByEntDate);
				logger.log("websites=");
				logger.increaseOffset();
					logger.log(websites);
				logger.decreaseOffset();
			logger.decreaseOffset();
			
			// initializes the Web search engines
			initWebSearchEngines(websites, startDate, endDate, language);
			
			// perform the Web search
			WebSearchResults results = performWebSearch(keywords, referenceEvents);
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_RAW;
			results.exportResults(fileName, startDate, endDate);
			results.computeRelevancePerformance(fileName, startDate, endDate);
			results.recordPerformance();
			currentStep++;
	
			// filter Web pages (remove PDFs, and so on)
			results.filterByUrl();
			
			// retrieve the corresponding articles
			results.retrieveArticles();
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_URL_FILTER;
			results.exportResults(fileName, startDate, endDate);
			results.computeRelevancePerformance(fileName, startDate, endDate);
			results.recordPerformance();
			currentStep++;
			
			// possibly filter the articles depending on the content
			results.filterByContent(startDate, endDate, filterByPubDate, compulsoryExpression, language);
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_CONTENT_FILTER;
			results.exportResults(fileName, startDate, endDate);
			results.computeRelevancePerformance(fileName, startDate, endDate);
			results.recordPerformance();
			currentStep++;
			
			// detect the entity mentions
			results.detectMentions(recognizer);
			
			// possibly filter the articles depending on the entities
			results.filterByEntity(startDate, endDate, filterByEntDate);
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_ENTITY_FILTER;
			results.exportResults(fileName, startDate, endDate);
			results.computeRelevancePerformance(fileName, startDate, endDate);
			results.recordPerformance();
			currentStep++;
			
			// displays the remaining articles with their mentions	//TODO maybe get the entities instead of the mentions, eventually?
			results.displayRemainingMentions(); // for debug only
			
			// cluster the articles by content
			results.clusterArticles(language);
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_CLUSTERING;
			results.exportResults(fileName, startDate, endDate);
			results.computeArticleClusterPerformance(fileName, startDate, endDate);
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_CLUSTERING_CONFMAT;
			results.exportConfusionMatrix(fileName);
			results.recordPerformance();
			currentStep++;
			
			// extract events from the remaining articles and mentions, and cluster them
			fileName = currentStep + "_";
			extractEvents(results, fileName, language, startDate, endDate);
			currentStep++;
			
		logger.decreaseOffset();
		logger.log("Web extraction over");
		return results;
	}
	
	/////////////////////////////////////////////////////////////////
	// SOCIAL SEARCH	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of engines used to search social medias */
	private final List<AbstractSocialEngine> socialEngines = new ArrayList<AbstractSocialEngine>();
	
	/**
	 * Initializes the default search engines for social medias.
	 * Currently: only Facebook.
	 * 
	 * @param seeds 
	 * 		List of pages to process during the search on social media. 
	 * @param startDate
	 * 		Start of the period we want to consider, or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider, or {@code null} for no constraint.
	 * @param doExtendedSearch
	 * 		Whether the social media search should retrieve the posts published by the
	 * 		users commenting the posts of interest, for the considered period. If 
	 * 		{@code false}, only the posts on the targeted page and their direct comments
	 * 		are returned. 
	 * @param language
	 * 		Targeted language. 
	 */
	private void initSocialMediaEngines(List<String> seeds, Date startDate, Date endDate, boolean doExtendedSearch, ArticleLanguage language)
	{	logger.log("Initializing the social media search engines");
		logger.increaseOffset();
		
		// iterate over each seed
		int i = 1;
		for(String seed: seeds)
		{	logger.log("Seed "+seed+" ("+i+"/"+seeds.size()+")");
			i++;
			
			// set up Facebook
			try
			{	FacebookEngine facebookEngine = new FacebookEngine(seed, startDate, endDate, doExtendedSearch, language);
				socialEngines.add(facebookEngine);
			} 
			catch (FailingHttpStatusCodeException | IOException | URISyntaxException e) 
			{	e.printStackTrace();
			}
		}
		logger.decreaseOffset();
	}
	
	/**
	 * Performs the Web search using the specified parameters and
	 * each one of the engines registered in the {@link #socialEngines}
	 * list.
	 * 
	 * @param keywords
	 * 		Person we want to look for.
	 * @param includeComments 
	 * 		Whether ({@code true}) or not ({@code false}) to include comments 
	 * 		in the proper article (or just the main post).
	 * @param referenceEvents
	 * 		Map containing the reference events, or
	 * 		empty if no such events could be found.
	 * @return
	 * 		List of results taking the form of URLs.
	 * 
	 * @throws IOException
	 * 		Problem accessing the Web.
	 */
	private SocialSearchResults performSocialSearch(String keywords, boolean includeComments, Map<Integer,ReferenceEvent> referenceEvents) throws IOException
	{	SocialSearchResults result = new SocialSearchResults(referenceEvents);
		
		// apply each search engine
		logger.log("Applying iteratively each social engine");
		logger.increaseOffset();
		for(AbstractSocialEngine engine: socialEngines)
		{	logger.log("Processing search engine "+engine);
			logger.increaseOffset();
				List<SocialSearchResult> posts = engine.retrieveResults(keywords);
			
				// add to the overall object for social media results
				String engineStr = engine.toString();
				int rank = 1;
				for(SocialSearchResult post: posts)
				{	post.rank = rank;
					post.buildArticle(includeComments);
					result.addResult(post, engineStr);
					rank++;
				}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		logger.log("Total number of posts found: "+result.size());
		
		return result;
	}
	
	/**
	 * Launches the main search.
	 * 
	 * @param keywords
	 * 		Person we want to look up.
	 * @param additionalSeeds 
	 * 		List of secondary pages to process during the search on social media. 
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * @param doExtendedSearch
	 * 		Whether the social media search should retrieve the posts published by the
	 * 		users commenting the posts of interest, for the considered period. If 
	 * 		{@code false}, only the posts on the targeted page and their direct comments
	 * 		are returned. 
	 * @param compulsoryExpression
	 * 		String expression which must be present in the groups of posts,
	 * 		or {@code null} if there is no such constraint.
	 * @param language
	 * 		Language targeted during the search.
	 * @param referenceEvents
	 * 		Map containing the reference events, or
	 * 		empty if no such events could be found.
	 * @return
	 * 		The social search results.
	 * 
	 * @throws IOException 
	 * 		Problem accessing the Web or a file.
	 * @throws SAXException 
	 * 		Problem while retrieving a Web page.
	 * @throws ParseException 
	 * 		Problem while retrieving a Web page.
	 * @throws ReaderException 
	 * 		Problem while retrieving a Web page.
	 * @throws ProcessorException 
	 * 		Problem while detecting the entity mentions.
	 */
	private SocialSearchResults performSocialExtraction(String keywords, List<String> additionalSeeds, Date startDate, Date endDate, String compulsoryExpression, boolean doExtendedSearch, ArticleLanguage language, Map<Integer,ReferenceEvent> referenceEvents) throws IOException, ReaderException, ParseException, SAXException, ProcessorException
	{	logger.log("Starting the social media extraction");
		logger.increaseOffset();
			int currentStep = 1;
			String fileName;
			
			// log search parameters
			logger.log("Parameters:");
			logger.increaseOffset();
				logger.log("keywords="+keywords);
				logger.log("startDate="+startDate);
				logger.log("endDate="+endDate);
				logger.log("doExtendedSearch="+doExtendedSearch);
				logger.log("additionalPages=");
				if(!additionalSeeds.isEmpty())
				logger.increaseOffset();
					logger.log(additionalSeeds);
				logger.decreaseOffset();
			logger.decreaseOffset();
			
			// initializes the social media search engines
			List<String> seeds = new ArrayList<String>();
			seeds.add(null);
			seeds.addAll(additionalSeeds);
			initSocialMediaEngines(seeds, startDate, endDate, doExtendedSearch, language);
			
			// perform the social search
			boolean includeComments = false;
			SocialSearchResults results = performSocialSearch(keywords, includeComments, referenceEvents);
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_RAW;
			results.exportResults(fileName, startDate, endDate);
			results.computeRelevancePerformance(fileName, startDate, endDate);
			results.recordPerformance();
			currentStep++;
			
			// convert the posts to proper articles
			results.buildArticles(includeComments);
			
			// possibly filter the articles depending on the content
			results.filterByContent(startDate, endDate, false, compulsoryExpression, language);	// false, because we suppose the targeted period is always respected when searching through the social media API
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_CONTENT_FILTER;
			results.exportResults(fileName, startDate, endDate);
			results.computeRelevancePerformance(fileName, startDate, endDate);
			results.recordPerformance();
			currentStep++;
			
			// detect the entity mentions
			results.detectMentions(recognizer);
			
			// possibly filter the articles depending on the entities
			results.filterByEntity(null,null,false); // unnecessary, unless we add other entity-based constraints than dates
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_ENTITY_FILTER;
			results.exportResults(fileName, startDate, endDate);
			results.computeRelevancePerformance(fileName, startDate, endDate);
			results.recordPerformance();
			currentStep++;
			
			// displays the remaining articles with their mentions	//TODO maybe get the entities instead of the mentions, eventually?
			results.displayRemainingMentions(); // for debug only
			
			// cluster the articles by content
			results.clusterArticles(language);
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_CLUSTERING;
			results.exportResults(fileName, startDate, endDate);
			results.computeArticleClusterPerformance(fileName, startDate, endDate);
			results.recordPerformance();
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_CLUSTERING_CONFMAT;
			results.exportConfusionMatrix(fileName);
			currentStep++;
			
			// extract events from the remaining articles and mentions, and cluster them
			fileName = currentStep + "_";
			extractEvents(results, fileName, language, startDate, endDate);
			currentStep++;
			
		logger.decreaseOffset();
		logger.log("Social media extraction over");
		return results;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITIES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Tool used to recognize named entity mentions in the text */ 
	private InterfaceRecognizer recognizer;
	
	/**
	 * Initializes the recognizer, 
	 * which will be applied to identify names and dates in
	 * the retrieved articles.
	 * 
	 * @throws ProcessorException
	 * 		Problem while initializing the recognizer. 
	 */
	private void initRecognizer() throws ProcessorException
	{	recognizer = new StraightCombiner();
		recognizer.setCacheEnabled(true);//TODO set to false for debugging
	}

	/////////////////////////////////////////////////////////////////
	// REFERENCE EVENTS		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tries to load the manually annotated reference events, if the 
	 * file exists. It is then used later to assess the system
	 * performance.
	 * <br/>
	 * We expect the following format: event id on the first column,
	 * event name on the second, event parent on the third, date on
	 * the fourth. Columns are separated by tabulations. The rest
	 * of the columns (if any) are just ignored.
	 * 
	 * @return
	 * 		A map of the predefined reference events.
	 * @throws UnsupportedEncodingException
	 * 		Problem when loading the reference events file. 
	 */
	private Map<Integer,ReferenceEvent> loadReferenceEvents() throws UnsupportedEncodingException
	{	logger.increaseOffset();
		Map<Integer,ReferenceEvent> result = new HashMap<Integer,ReferenceEvent>();
		String filePath = FileNames.FO_OUTPUT + File.separator + SearchFileNames.FI_ANNOTATED_EVENTS;

			try
			{	Scanner scanner = FileTools.openTextFileRead(filePath, "UTF-8");
				logger.log("Find a reference events file ("+filePath+"): loading it");
				while(scanner.hasNextLine())
				{	String line = scanner.nextLine();
					String[] tmp = line.split("\t");
					
					// event id
					String idStr = tmp[0].trim();
					int id = Integer.parseInt(idStr);
					// event name
					String name = tmp[1].trim();
					// parent
					String parentStr = tmp[2].trim();
					ReferenceEvent parent = null;
					if(!parentStr.isEmpty())
					{	int parentId = Integer.parseInt(parentStr);
						parent = result.get(parentId);
					}
					// event date
					String dateStr = tmp[3].trim();
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date date = null;
					try
					{	date = format.parse(dateStr);
					}
					catch (ParseException e)
					{	format = new SimpleDateFormat("yyyy-MM");
						try
						{	date = format.parse(dateStr);
						} 
						catch (ParseException e1) 
						{	format = new SimpleDateFormat("yyyy");
							try
							{	date = format.parse(dateStr);
							}
							catch (ParseException e2) 
							{	e2.printStackTrace();
							}
						}
					}
					
					// reference event
					ReferenceEvent event = new ReferenceEvent(id, name, date, parent);
					result.put(id, event);
				}
			}
			catch (FileNotFoundException e) 
			{	logger.log("Found no reference events file at "+filePath);
			}
			
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// MERGE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Extract the events by article and by sentence, for the specified results.
	 * Then export the events, cluster them, and export the resulting clusters 
	 * of events.
	 * 
	 * @param results
	 * 		Search results used for event extraction.
	 * @param filePrefix 
	 * 		String used to name the file to create.
	 * @param language
	 * 		Language of the articles.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while exporting the events.
	 * @throws FileNotFoundException
	 * 		Problem while exporting the events.
	 */
	private void extractEvents(AbstractSearchResults<?> results, String filePrefix, ArticleLanguage language, Date startDate, Date endDate) throws UnsupportedEncodingException, FileNotFoundException
	{	boolean bySentence[] = {false,true};
		for(boolean bs: bySentence)
		{	// identify the events
			results.extractEvents(bs);
			// try to group similar events together
			results.clusterEvents();
			
			// export the detailed list of events
			results.exportEvents(bs, filePrefix, startDate, endDate);
			// export the event clusters
			results.exportEventClusters(bs, filePrefix, language);
			
			// process event cluster results
			results.computeEventClusterPerformance(bs, filePrefix, startDate, endDate);
			results.recordPerformance();
		}
	}
	
	/**
	 * Combines the Web and social media results, and repeat the analysis
	 * steps on these combined results.
	 *  
	 * @param webRes
	 * 		Web search results.
	 * @param socRes
	 * 		Social media search results.
	 * @param language
	 * 		Targeted language.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while exporting the results.
	 * @throws FileNotFoundException
	 * 		Problem while exporting the results.
	 */
	private void combineResults(WebSearchResults webRes, SocialSearchResults socRes, ArticleLanguage language, Date startDate, Date endDate) throws UnsupportedEncodingException, FileNotFoundException
	{	logger.log("Combining all results in a single file.");
		logger.increaseOffset();
			int currentStep = 1;
			String fileName;
			
			// merge the results and record
			CombinedSearchResults results = new CombinedSearchResults(webRes, socRes);
			results.resetClusters();
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_MERGE;
			results.exportResults(fileName, startDate, endDate);
			results.computeRelevancePerformance(fileName, startDate, endDate);
			results.recordPerformance();
			currentStep++;
			
			// cluster the combined articles by content
			results.clusterArticles(language);
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_CLUSTERING;
			results.exportResults(fileName, startDate, endDate);
			results.computeArticleClusterPerformance(fileName, startDate, endDate);
			results.recordPerformance();
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_CLUSTERING_CONFMAT;
			results.exportConfusionMatrix(fileName);
			currentStep++;
			
			// extract events from articles and mentions
			fileName = currentStep + "_";
			extractEvents(results, fileName, language, startDate, endDate);
			currentStep++;
			
			// filter mentions based on article clusters
			results.filterByCluster(1);
			fileName = currentStep + "_" + SearchFileNames.FI_ARTICLES_CLUSTER_FILTER;
			results.exportResults(fileName, startDate, endDate);
			currentStep++;
			
			// extract events based on the filtered mentions, and cluster them
			fileName = currentStep + "_";
			extractEvents(results, fileName, language, startDate, endDate);
			currentStep++;
			
		logger.decreaseOffset();
	}
}
