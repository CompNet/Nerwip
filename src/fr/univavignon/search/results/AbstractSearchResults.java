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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Distance;

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.search.events.Event;
import fr.univavignon.search.clustering.MyPam;
import fr.univavignon.search.clustering.DummyDistanceMetric;
import fr.univavignon.search.clustering.Silhouette;
import fr.univavignon.search.events.ReferenceEvent;
import fr.univavignon.search.tools.files.SearchFileNames;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;
import fr.univavignon.common.tools.strings.CommonStringTools;
import fr.univavignon.common.tools.strings.StopWordsManager;
import fr.univavignon.tools.strings.StringTools;
import fr.univavignon.common.tools.time.Period;
import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.clustering.Clusterer;
import jsat.linear.DenseVector;
import jsat.linear.Vec;
import jsat.linear.distancemetrics.DistanceMetric;

/**
 * Collection of search results returned by a collection of
 * search engines, with additional info resulting from their
 * subsequent processing.
 * 
 * @param <T>
 * 		Type of results handled by this class. 
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractSearchResults<T extends AbstractSearchResult>
{
	/**
	 * Initializes the search result.
	 */
	public AbstractSearchResults()
	{	
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	public static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// ENGINES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of search engines involved in the current search */
	protected Set<String> engineNames = new TreeSet<String>();
	
	/////////////////////////////////////////////////////////////////
	// RESULTS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of results (articles), the String key is a unique id (url, alphanumerical code...) */
	protected Map<String,T> results = new HashMap<String,T>();
	/** Silhouette value corresponding to the last clustering done */
	private double lastSilhouette = Double.NaN;
	/** Confusion matrix of the last clustering, as a string (ready to be recorded in a CSV file */
	public String lastConfMatStr = null;
	
	/**
	 * Returns the number of entries in this collection of search results.
	 * 
	 * @return
	 * 		Number of entries in this map.
	 */
	public int size()
	{	int result = results.size();
		return result;
	}
	
	/** Tokenizer used to classify the results */
	private static TokenizerFactory TOKENIZER_FACTORY;
	/** Initialization of the tokenizer */
	static
	{	TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
		TOKENIZER_FACTORY = new LowerCaseTokenizerFactory(TOKENIZER_FACTORY);
//		TOKENIZER_FACTORY = new EnglishStopTokenizerFactory(TOKENIZER_FACTORY);		//stop list, but only for English
//		TOKENIZER_FACTORY = new PorterStemmerTokenizerFactory(TOKENIZER_FACTORY);	// don't know if the stemmer supports French
		
	}
    
	/**
	 * Resets the existing clusters for all results (not events ! just articles).
	 */
	public void resetClusters()
	{	for(T res: results.values())
			res.cluster = null;
	}
	
	/**
	 * Identifies clusters of similar results (independently from
	 * the mentions detected later). 
	 * <br/>
	 * Most of this source code was taken from the LinkPipe website.
	 * http://alias-i.com/lingpipe/demos/tutorial/cluster/src/TokenCosineDocCluster.java
	 * 
	 * @param language
	 * 		Language of the articles.
	 */
	public void clusterArticles(ArticleLanguage language)
	{	boolean lingpipe = true;
		
		logger.log("Clustering the articles");
		logger.increaseOffset();
			List<String> stopWords = StopWordsManager.getStopWords(language);
			Map<String,Integer> cf = new HashMap<String,Integer>();	// collection frequency (for information)
			Map<String,Integer> df = new HashMap<String,Integer>();	// document frequency
			
			// tokenize and process separately tf and df 
			List<T> remainingRes = new ArrayList<T>();
			List<Map<String,Integer>> tfList = new ArrayList<Map<String,Integer>>();
			for(T result: results.values())
			{	if(result.status==null)
				{	remainingRes.add(result);
					
					// tokenize
					String rawText = result.article.getRawText();
					String cleanText = rawText.replaceAll("\\n", " ");
					cleanText = cleanText.replaceAll("\\d+"," ");
					cleanText = StringTools.removePunctuation(cleanText);
					char[] charText = cleanText.toCharArray();
					Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(charText,0,charText.length);
					Map<String,Integer> tf = new HashMap<String,Integer>();
					tfList.add(tf);
					String token;
			    	while((token=tokenizer.nextToken()) != null)
			    	{	if(!stopWords.contains(token))
			    		{	// update term frequency
			    			Integer c = tf.get(token);
			    			if(c==null)
			    				c = 0;
			    			c++;
			    			tf.put(token,c);
			    			// update document frequency
			    			if(c==1)
			    			{	c = df.get(token);
			    				if(c==null)
			    					c = 0;
			    				c++;
			    				df.put(token, c);
			    			
			    			}
			    			// update collection frequency
			    			c = cf.get(token);
			    			if(c==null)
			    				c = 0;
			    			c++;
			    			cf.put(token, c);

			    			//logger.log(token);
			    		}
			    	}
				}	
			}
			
			if(remainingRes.size()<3)
			{	logger.log("There are not enough articles to perform clustering >> all documents are put in the same unique cluster");
				for(T result: remainingRes)
					result.cluster = "N/A";
			}
			else
			{	// display word counts for the whole corpus
				logger.log("Word frequencies after tokenization:");
				logger.increaseOffset();
					TreeSet<String> orderedTerms = new TreeSet<String>(cf.keySet());
					for(String term: orderedTerms)
		            {	int valCf = cf.get(term);
		            	logger.log(term+": "+valCf);
		            }
				logger.decreaseOffset();
				
				// normalize df to get idf
				Map<String,Double> idf = new HashMap<String,Double>();	// inverse document frequency
				for(Entry<String,Integer> entry : df.entrySet())
	            {	String token = entry.getKey();
	            	int valDf = entry.getValue();
	                double valIdf = Math.log10(remainingRes.size() / (valDf + 1.0));
	                idf.put(token, valIdf);
	            }
				
				// combine to get tf-idf
				List<Map<String,Double>> tfidfList = new ArrayList<Map<String,Double>>();	// tf-idf
				for(Map<String,Integer> tf: tfList)
				{	Map<String,Double> tfidf = new HashMap<String,Double>();
					tfidfList.add(tfidf);
					for(Entry<String,Integer> entry : tf.entrySet())
					{	String token = entry.getKey();
						int valTf = entry.getValue();
						double valIdf = idf.get(token);
						double valTfidf = valTf * valIdf;
						tfidf.put(token, valTfidf);
					}
				}
				
				// process the norm of each document
				List<Double> norms = new ArrayList<Double>(remainingRes.size());
				for(Map<String,Double> tfidf: tfidfList)
				{	double norm = 0;
					for(Double valTfidf: tfidf.values())
						norm = norm + valTfidf*valTfidf;
					norm = Math.sqrt(norm);
					norms.add(norm);
				}
				
				// process the cos distance between all results
				double distanceMatrix[][] = new double[remainingRes.size()][remainingRes.size()];
				for(int i=0;i<remainingRes.size()-1;i++)
				{	double norm1 = norms.get(i);
					Map<String,Double> tfidf1 = tfidfList.get(i);
					
					for(int j=i+1;j<remainingRes.size();j++)
					{	double norm2 = norms.get(j);
						Map<String,Double> tfidf2 = tfidfList.get(j);
						
						double productVal = 0;
						for(Entry<String,Double> entry: tfidf1.entrySet())
					    {	double valTfidf1 = entry.getValue();
					    	String token = entry.getKey();
					    	if(tfidf2.containsKey(token))
							{	double valTfidf2 = tfidf2.get(token);
					        	productVal = productVal + Math.sqrt(valTfidf1 * valTfidf2);
							}
					    }
						
						double cosVal = productVal / (norm1 * norm2);
						double dist = 1.0 - cosVal;
						distanceMatrix[i][j] = dist;
						distanceMatrix[j][i] = dist;
					}
				}

				// record distance matrix (for debug)
//				try
//				{	PrintWriter pw = FileTools.openTextFileWrite("dist.csv", "UTF-8");
//					for(int i=0;i<remainingRes.size();i++)
//					{	for(int j=0;j<remainingRes.size();j++)
//						{	if(j>0)
//								pw.print(",");
//							pw.print(distanceMatrix[i][j]);
//						}
//						pw.println();
//					}
//					pw.close();
//				} 
//				catch(UnsupportedEncodingException | FileNotFoundException e) 
//				{	e.printStackTrace();
//				}
			
				// perform the clustering using the Jstat library (PAM)
				double silhouette = Double.NaN;
				if(!lingpipe)
					silhouette = clusterArticlesPam(distanceMatrix, remainingRes);
				// or using the LingPipe library (hierarchical + Silhouette)
				else
				{	boolean outputHierarchy = false;
					silhouette = clusterArticlesHierSilh(distanceMatrix, remainingRes, outputHierarchy);
				}
				lastSilhouette = silhouette;
			}
			
		logger.decreaseOffset();
		logger.log("Article clustering complete");																						// lingpipe
	}
	
	/**
	 * Performs the article clustering using the Jstat library and
	 * the PAM algorithm (partitioning around k-medoids).
	 * 
	 * @param distanceMatrix
	 * 		Distance matrix between texts.
	 * @param remainingRes
	 * 		List of the processed objects.
	 * @return
	 * 		Silhouette of the partition.
	 */
	private double clusterArticlesPam(double[][] distanceMatrix, List<T> remainingRes)
	{	// build the distance object
		DistanceMetric dm = new DummyDistanceMetric(distanceMatrix);
		
		// build a dummy dataset
		List<DataPoint> dp = new ArrayList<DataPoint>();
		for(int i=0;i<remainingRes.size();i++)
		{	Vec v = new DenseVector(Arrays.asList((double)i));
			DataPoint d = new DataPoint(v);
			dp.add(d);
		}
		SimpleDataSet ds = new SimpleDataSet(dp);
		
		// proceed with the cluster analysis
		Clusterer clusterer = new MyPam(dm);
		int[] membership = new int[remainingRes.size()];	// NOTE: jstat clusters numbered starting from zero
		clusterer.cluster(ds, membership);
		
		// set up the clusters in the results themselves
		List<Set<Integer>> temp = new ArrayList<Set<Integer>>();
		int maxClust = 0;
		int i = 0;
		for(T result: remainingRes)
		{	int clustId = membership[i] + 1;
			result.cluster = Integer.toString(clustId);
			if(clustId > maxClust)
				maxClust = clustId;
			
			while(temp.size()<clustId)
			{	Set<Integer> set = new TreeSet<Integer>();
				temp.add(set);
			}
			Set<Integer> set = temp.get(clustId);
			set.add(i);
			
			i++;
		}
		
		Set<Set<Integer>> partition = new TreeSet<Set<Integer>>(temp);
		double result = Silhouette.processSilhouette(distanceMatrix, partition);
		logger.log("Result: "+maxClust+" clusters detected for "+remainingRes.size()+" remaining articles (Silhouette="+result+")");
		return result;
	}
	
	/**
	 * Performs the article clustering using the LingPipe library and
	 * the hierarchical clustering algorithm, with the Silhouette
	 * measure to select the best cut.
	 * 
	 * @param distanceMatrix
	 * 		Distance matrix between texts.
	 * @param remainingRes
	 * 		List of the processed objects.
	 * @param outputHierarchy
	 * 		Whether or not output the whole hierarchy in the CSV
	 * 		files generated later.
	 * @return
	 * 		Silhouette of the partition.
	 */
	private double clusterArticlesHierSilh(final double[][] distanceMatrix, List<T> remainingRes, boolean outputHierarchy)
	{	// process the dummy distances
		Distance<Integer> dl = new Distance<Integer>()
		{	@Override
			public double distance(Integer e1, Integer e2)
			{	double result = distanceMatrix[e1][e2];
				return result;
			}
		};
		
		// build a dummy dataset
		Set<Integer> dd = new TreeSet<Integer>();
		for(int i=0;i<remainingRes.size();i++)
			dd.add(i);
		
		// proceed with the cluster analysis
        HierarchicalClusterer<Integer> clusterer =
        		new CompleteLinkClusterer<Integer>(dl);
    	Dendrogram<Integer> dendro = clusterer.hierarchicalCluster(dd);

		// set up the clusters in the results themselves
    	int bestK = 0;
    	double bestSil = Double.NEGATIVE_INFINITY;
    	for(int k=2;k<=remainingRes.size();k++)
    	{	Set<Set<Integer>> partition = dendro.partitionK(k);
    		// get the silhouette
    		double sil = Silhouette.processSilhouette(distanceMatrix, partition);
    		logger.log("k="+k+"/"+remainingRes.size()+" >> Silhouette="+sil);
    		if(sil>bestSil)
			{	bestSil = sil;
				bestK = k;
			}
    		// setup cluster in result
    		if(outputHierarchy || bestK==k)
    		{	int j = 1;
	    		for(Set<Integer> part: partition)
	    		{	for(int i: part)
	        		{	T res = remainingRes.get(i);
	        			if(res.cluster==null || !outputHierarchy)
	        				res.cluster = Integer.toString(j);
	        			else
	        				res.cluster = res.cluster + ":" + j;
	        		}
	    			j++;
	    		}
    		}
    	}
		logger.log("Best event partition: k="+bestK+" (Silhouette="+bestSil+")");
		return bestSil;
	}
	
	/**
	 * Filters the previously detected mentions, based on the previously detected
	 * clusters of articles. For a given article, we keep only the mentions present
	 * in a certain proportion of the articles of the same cluster.
	 * 
	 * @param threshold
	 * 		Proportion of articles that must have the mention in order to keep it.
	 */
	public void filterByCluster(float threshold)
	{	// build the clusters of articles
		Map<Integer,List<T>> artClust = new HashMap<Integer,List<T>>();
		for(T res: results.values())
		{	if(res.status==null)
			{	String cStr = res.cluster;
				int c = Integer.parseInt(cStr);
				List<T> cluster = artClust.get(c);
				if(cluster==null)
				{	cluster = new ArrayList<T>();
					artClust.put(c, cluster);
				}
				cluster.add(res);
			}
		}
		
		// count mention occurrences in each cluster, remove the unfrequent ones
		for(Entry<Integer,List<T>> entry: artClust.entrySet())
		{	//int c = entry.getKey();
			List<T> cluster = entry.getValue();
			float tot = cluster.size();
			// get the mentions for the current article cluster
			Map<EntityType,List<AbstractMention<?>>> mapMentions = new HashMap<EntityType, List<AbstractMention<?>>>();
			Map<EntityType,List<String>> mapStrings = new HashMap<EntityType, List<String>>();
			Map<EntityType,List<T>> mapResults = new HashMap<EntityType, List<T>>();
			for(T res: cluster)
			{	for(EntityType type: EntityType.values())
				{	List<String> listStrings = mapStrings.get(type);
					List<AbstractMention<?>> listMentions = mapMentions.get(type);
					List<T> listResults = mapResults.get(type);
					if(listStrings==null)
					{	listStrings = new ArrayList<String>();
						mapStrings.put(type, listStrings);
						listMentions = new ArrayList<AbstractMention<?>>();
						mapMentions.put(type, listMentions);
						listResults = new ArrayList<T>();
						mapResults.put(type, listResults);
					}
					List<AbstractMention<?>> set = res.mentions.getMentionsByType(type);
					for(AbstractMention<?> mention: set)
					{	Object value = mention.getValue();
						String valueStr = value.toString();
						listStrings.add(valueStr);
						listMentions.add(mention);
						listResults.add(res);
					}
				}
			}
			// compute the mention frequencies and identify which mentions to remove
			for(EntityType type: EntityType.values())
			{	List<String> listStrings = mapStrings.get(type);
				// get the frequencies
				Map<String,Integer> freqs = CommonStringTools.computeFrequenciesFromTokens(listStrings);
				// list of words whose freq is below the threshold
				List<String> removeList = new ArrayList<String>();
				for(Entry<String,Integer> e: freqs.entrySet())
				{	String word = e.getKey();
					float freq = e.getValue() / tot;
					if(freq<threshold)
						removeList.add(word);
				}
				// remove the corresponding mentions
				Iterator<String> itStrings = listStrings.iterator();
				List<AbstractMention<?>> listMentions = mapMentions.get(type);
				Iterator<AbstractMention<?>> itMentions = listMentions.iterator();
				List<T> listResults = mapResults.get(type);
				Iterator<T> itResults = listResults.iterator();
				while(itStrings.hasNext())
				{	String string = itStrings.next();
					AbstractMention<?> mention = itMentions.next();
					T result = itResults.next();
					if(removeList.contains(string))
						result.mentions.removeMention(mention);
				}
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// EVENTS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Last detected clusters of events (1 sublist = 1 cluster) */
	protected List<List<Event>> eventClusters = new ArrayList<List<Event>>();
	
	/**
	 * Identifies the events described in the articles associated to
	 * the search results.
	 * 
	 * @param bySentence
	 * 		Whether to retrieve events by sentence (all event-related entity mentions
	 * 		must be in the same sentence) or by article.
	 */
	public void extractEvents(boolean bySentence)
	{	logger.log("Extracting events from all the articles");
		logger.increaseOffset();
		
			int count = 0;
			int total = 0;
			for(T result: results.values())
			{	if(result.status==null)
				{	total++;
					if(result.extractEvents(bySentence,total)>0)
						count++;
				}
			}
		
		logger.decreaseOffset();
		logger.log("Event extraction complete: ("+count+"/"+total+")");
	}
	
	/** Title of the Web resource */
	public static final String COL_TITLE = "Page title";
	/** URL of the Web resource */
	public static final String COL_URL = "URL";
	/** Length of the article, in characters */
	public static final String COL_LENGTH = "Text length";
	/** Author of the article */
	public static final String COL_AUTHORS = "Author(s)";
	/** Status of the page (if none, it means it is still handled) */
	public static final String COL_STATUS = "Status";
	/** Publication date of the Web resource */
	public static final String COL_PUB_DATE = "Publication date";
	/** Name of the social search engine */
	public static final String COL_SOCIAL_ENGINE = "Social media";
	/** Rank according to some search engine */
	public static final String COL_RANK = "Rank ";
	/** Name of the search engine or social media */
	public static final String COL_ENGINE = "Engine";
	/** Rank of the event in the article */
	public static final String COL_EVENT_RANK = "Event rank";
	/** Dates associated to the event/article */
	public static final String COL_ENT_DATES = "Dates";
	/** Locations associated to the event/article */
	public static final String COL_ENT_LOCATIONS = "Locations";
	/** Persons associated to the event/article */
	public static final String COL_ENT_PERSONS = "Persons";
	/** Organizations associated to the event/article */
	public static final String COL_ENT_ORGANIZATIONS = "Organizations";
	/** Personal roles associated to the event/article */
	public static final String COL_ENT_FUNCTIONS = "Functions";
	/** Intellectual productions associated to the event/article */
	public static final String COL_ENT_PRODUCTIONS = "Production";
	/** Meetings associated to the event/article */
	public static final String COL_ENT_MEETINGS = "Meetings";
	/** Misc. comments and notes */
	public static final String COL_NOTES = "Notes";
	/** Whether a social post was written by the targeted person, or not */
	public static final String COL_ORIGINAL = "Original post";
	/** Column name for the number of likes of a social search result */
	public static final String COL_LIKES = "Likes";
	/** Column name for the number of shares of a social search result */
	public static final String COL_SHARES = "Shares";
	/** Column name for the number of comments of a result */
	public static final String COL_COMMENTS = "Comments";
	/** Column name for the id of the cluster containing an event */
	public static final String COL_EVENT_CLUSTER = "Event cluster";
	/** Column name for the sentence corresponding to an event */
	public static final String COL_EVENT_SENTENCE = "Event sentence";
	/** Column name for the id of the cluster of an article */
	public static final String COL_ARTICLE_CLUSTER = "Article cluster";
	/** Column name for the URL of the web search or id of the social search */
	public static final String COL_URL_ID = "URL/ID";
	/** Column name for the title of the web search or content of the social search */
	public static final String COL_TITLE_CONTENT = "Title/Content";
	/** Column name for the engine which returned the result (web or a social media) */
	public static final String COL_SOURCE = "Source";
	/** Column name for the keywords in a group of articles */
	public static final String COL_KEYWORDS = "Keywords";
	/** Column name for the number of events in a cluster */
	public static final String COL_FREQUENCY = "Frequency";
	/** Column name for the content of a social media post */
	public static final String COL_CONTENT = "Content";
	/** Column name for the id of the reference event(s) of an article */
	public static final String COL_REFERENCE_EVENTS = "Reference events";
	
	/**
	 * Records the detailed list of previously identified events as a CSV file.
	 * 
	 * @param bySentence 
	 * 		Whether the events are considered in the whole article or in
	 * 		individual sentences.
	 * @param filePrefix 
	 * 		String used to name the file to create.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing to the result file.
	 * @throws FileNotFoundException
	 * 		Problem while accessing to the result file.
	 */
	public abstract void exportEvents(boolean bySentence, String filePrefix, Date startDate, Date endDate) throws UnsupportedEncodingException, FileNotFoundException;
	
	/**
	 * Adds the event cluster information to the specified map.
	 * 
	 * @param cluster
	 * 		Event cluster to process.
	 * @param id
	 * 		Id of the cluster in the partition.
	 * @param language
	 * 		Language of the articles.
	 * @return
	 * 		Map to fill with the required data.
	 */
	private Map<String,String> exportEventCluster(List<Event> cluster, int id, ArticleLanguage language)
	{	Map<String,String> result = new HashMap<String,String>();
		
		// cluster id
		result.put(COL_EVENT_CLUSTER, Integer.toString(id));
		
		// cluster frequency
		int freq = cluster.size();
		result.put(COL_FREQUENCY, Integer.toString(freq));
		
		// union of the mentions (by type)
		TreeSet<String> texts = new TreeSet<String>();	
		Map<EntityType,Set<String>> mentionSets = new HashMap<EntityType, Set<String>>();
		for(Event event: cluster)
		{	// deal with the text
			String text = event.getText();
			texts.add(text);
			// process the entities
			for(EntityType type: EntityType.values())
			{	Set<String> set = mentionSets.get(type);
				if(set==null)
				{	set = new TreeSet<String>();
					mentionSets.put(type, set);
				}
				if(type==EntityType.DATE)
				{	Period evtPeriod = event.getPeriod();
					String periodStr = evtPeriod.toString();
					set.add(periodStr);
				}
				else
				{	Set<String> evSet = event.getNamedMentionsByType(type);
					set.addAll(evSet);
				}
			}
		}
		Map<EntityType,String> mentionStr = new HashMap<EntityType,String>();
		for(EntityType type: EntityType.values())
		{	Set<String> set = mentionSets.get(type);
			String str = "";
			Iterator<String> it = set.iterator();
			while(it.hasNext())
			{	String val = it.next();
				str = str + val;
				if(it.hasNext())
					str = str + ", ";
			}
			mentionStr.put(type, str);
		}
		result.put(COL_ENT_DATES, mentionStr.get(EntityType.DATE));
		result.put(COL_ENT_LOCATIONS, mentionStr.get(EntityType.LOCATION));
		result.put(COL_ENT_PERSONS, mentionStr.get(EntityType.PERSON));
		result.put(COL_ENT_ORGANIZATIONS, mentionStr.get(EntityType.ORGANIZATION));
		result.put(COL_ENT_FUNCTIONS, mentionStr.get(EntityType.FUNCTION));
		result.put(COL_ENT_PRODUCTIONS, mentionStr.get(EntityType.PRODUCTION));
		result.put(COL_ENT_MEETINGS, mentionStr.get(EntityType.MEETING));
		
		// get the keywords
		final Map<String,Integer> wordFreq = CommonStringTools.computeWordFrequencies(texts, language);
		List<String> sortedKeys = new ArrayList<String>(wordFreq.size());
		Collections.sort(sortedKeys, new Comparator<String>()
		{	@Override
			public int compare(String word1, String word2) 
			{	int f1 = wordFreq.get(word1);
				int f2 = wordFreq.get(word2);
				int result = f2 - f1;
				return result;
			}	
		});
		String keywords = "";
		int KEYWORD_MAX_NBR = 12;
		Iterator<String> it = sortedKeys.iterator();
		int i = 0;
		while(it.hasNext() && i<KEYWORD_MAX_NBR)
		{	String keyword = it.next();
			keywords = keywords + keyword;
			if(i<KEYWORD_MAX_NBR-1 && it.hasNext())
				keywords = keywords + ", ";
			i++;
		}
		result.put(COL_KEYWORDS, keywords);
		
		return result;
	}
	
	/**
	 * Records the previously identified event clusters as a CSV file.
	 * 
	 * @param bySentence 
	 * 		Whether the events are considered in the whole article or in
	 * 		individual sentences.
	 * @param filePrefix 
	 * 		String used to name the file to create.
	 * @param language
	 * 		Language of the articles.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing to the result file.
	 * @throws FileNotFoundException
	 * 		Problem while accessing to the result file.
	 */
	public abstract void exportEventClusters(boolean bySentence, String filePrefix, ArticleLanguage language) throws UnsupportedEncodingException, FileNotFoundException;
	
	/**
	 * Records the previously identified event clusters as a CSV file
	 * (used by {@link #exportEventClusters(boolean, String, ArticleLanguage)}).
	 * 
	 * @param filePath
	 * 		Path and name of the output file.
	 * @param language
	 * 		Language of the articles.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing to the result file.
	 * @throws FileNotFoundException
	 * 		Problem while accessing to the result file.
	 */
	protected void exportEventClusters(String filePath, ArticleLanguage language) throws UnsupportedEncodingException, FileNotFoundException
	{	logger.log("Recording the event clusters as a CVS file: "+filePath);
		logger.decreaseOffset();
			
			// setup colon names
			List<String> cols = Arrays.asList(
					COL_NOTES, COL_EVENT_CLUSTER, COL_KEYWORDS, COL_FREQUENCY, 
					COL_ENT_DATES, COL_ENT_LOCATIONS, COL_ENT_PERSONS, COL_ENT_ORGANIZATIONS, 
					COL_ENT_FUNCTIONS, COL_ENT_PRODUCTIONS, COL_ENT_MEETINGS
			);
			
			// open file and write header
			PrintWriter pw = FileTools.openTextFileWrite(filePath, "UTF-8");
			Iterator<String> it = cols.iterator();
			while(it.hasNext())
			{	String col = it.next();
				pw.print("\""+col+"\"");
				if(it.hasNext())
					pw.print(",");
			}
			pw.println();
			
			// write data
			logger.log("Treat each cluster separately");
			int i = 0;
			Iterator<List<Event>> itClust =  eventClusters.iterator();
			while(itClust.hasNext())
			{	List<Event> cluster = itClust.next();
				i++;
				// get the line
				Map<String,String> line = exportEventCluster(cluster,i,language);
				// write the line
				it = cols.iterator();
				while(it.hasNext())
				{	String col = it.next();
					String val = line.get(col);
					if(val!=null)
						pw.print("\""+val+"\"");
					if(it.hasNext())
						pw.print(",");
				}
				pw.println();
			}
			
			pw.close();
		logger.decreaseOffset();
		logger.log("Wrote "+eventClusters.size()+" event clusters");
	}
	
	/**
	 * Identify groups of similar events among the previously identified events.
	 */
	public void clusterEvents()
	{	boolean lingpipe = true;
		
		logger.log("Clustering events from all the articles");
		logger.increaseOffset();
			eventClusters.clear();
			
			// build a list of all events
			List<Event> allEvents = new ArrayList<Event>();
			Map<Integer,T> resMap = new HashMap<Integer,T>();
			Map<Integer,Integer> evtMap = new HashMap<Integer,Integer>();
			int idx = 0;
			for(T result: results.values())
			{	if(result.status==null)
				{	for(int i=0;i<result.events.size();i++)
					{	Event event = result.events.get(i);
						allEvents.add(event);
						resMap.put(idx, result);
						evtMap.put(idx, i);
						idx++;
					}
				}
			}
			
			if(allEvents.size()<2)
				logger.log("Not enough events to process, so no event clustering");
			else
			{	// init the distances between events
				double[][] distanceMatrix = new double[allEvents.size()][allEvents.size()];
				for(int i=0;i<allEvents.size()-1;i++)
				{	Event event1 = allEvents.get(i);
					for(int j=i+1;j<allEvents.size();j++)
					{	Event event2 = allEvents.get(j);
						float sim = event1.processJaccardSimilarity(event2);
						distanceMatrix[i][j] = 1 - sim;
						distanceMatrix[j][i] = 1 - sim;
					}
				}
				
				// perform the clustering using the Jstat library (PAM)
				double silhouette = Double.NaN;
				if(!lingpipe)
					silhouette = clusterEventsPam(distanceMatrix, allEvents);
				// or using the LingPipe library (hierarchical + Silhouette)
				else
				{	boolean outputHierarchy = false;
					silhouette = clusterEventsHierSilh(distanceMatrix, allEvents, outputHierarchy);
				}
				lastSilhouette = silhouette;
			}
			
		logger.decreaseOffset();
		logger.log("Event clustering complete: "+eventClusters.size()+" clusters detected for "+allEvents.size()+" events");
	}
	
	/**
	 * Performs the event clustering using the Jstat library and
	 * the PAM algorithm (partitioning around k-medoids).
	 * 
	 * @param distanceMatrix
	 * 		Distance matrix between texts.
	 * @param events
	 * 		List of the processed events.
	 * @return
	 * 		Silhouette of the partition.
	 */
	private double clusterEventsPam(double[][] distanceMatrix, List<Event> events)
	{	// build the distance object
		DistanceMetric dm = new DummyDistanceMetric(distanceMatrix);
		
		// build a dummy dataset
		List<DataPoint> dp = new ArrayList<DataPoint>();
		for(int i=0;i<events.size();i++)
		{	Vec v = new DenseVector(Arrays.asList((double)i));
			DataPoint d = new DataPoint(v);
			dp.add(d);
		}
		SimpleDataSet ds = new SimpleDataSet(dp);
		
		// proceed with the cluster analysis
		Clusterer clusterer = new MyPam(dm);
		int[] membership = new int[events.size()];	// NOTE: jstat clusters numbered starting from zero
		clusterer.cluster(ds, membership);
		
		// set up the clusters in the results themselves
		List<Set<Integer>> temp = new ArrayList<Set<Integer>>();
		int maxClust = 0;
		int i = 0;
		for(Event event: events)
		{	int clustId = membership[i] + 1;
			event.cluster = Integer.toString(clustId);
			if(clustId > maxClust)
				maxClust = clustId;
			
			while(temp.size()<clustId)
			{	Set<Integer> set = new TreeSet<Integer>();
				temp.add(set);
			}
			Set<Integer> set = temp.get(clustId);
			set.add(i);
			
			i++;
		}
		
		// setup the list representing the partition
		for(i=0;i<=maxClust;i++)
		{	List<Event> cluster = new ArrayList<Event>();
			eventClusters.add(cluster);
		}
		for(Event event: events)
		{	String cStr = event.cluster;
			int c = Integer.parseInt(cStr);
			List<Event> cluster = eventClusters.get(c-1);
			cluster.add(event);
		}
		
		Set<Set<Integer>> partition = new TreeSet<Set<Integer>>(temp);
		double result = Silhouette.processSilhouette(distanceMatrix, partition);
		logger.log("Result: "+maxClust+" clusters detected for "+events.size()+" events (Silhouette="+result+")");
		return result;
	}
	
	/**
	 * Performs the event clustering using the LingPipe library and
	 * the hierarchical clustering algorithm, with the Silhouette
	 * measure to select the best cut.
	 * 
	 * @param distanceMatrix
	 * 		Distance matrix between texts.
	 * @param events
	 * 		List of the processed objects.
	 * @param outputHierarchy
	 * 		Whether or not output the whole hierarchy in the CSV
	 * 		files generated later.
	 * @return
	 * 		Silhouette of the partition.
	 */
	private double clusterEventsHierSilh(final double[][] distanceMatrix, List<Event> events, boolean outputHierarchy)
	{	// process the dummy distances
		Distance<Integer> dl = new Distance<Integer>()
		{	@Override
			public double distance(Integer e1, Integer e2)
			{	double result = distanceMatrix[e1][e2];
				return result;
			}
		};
		
		// build a dummy dataset
		Set<Integer> dd = new TreeSet<Integer>();
		for(int i=0;i<events.size();i++)
			dd.add(i);
		
		// proceed with the cluster analysis
        HierarchicalClusterer<Integer> clusterer =
        		new CompleteLinkClusterer<Integer>(dl);
    	Dendrogram<Integer> dendro = clusterer.hierarchicalCluster(dd);

		// set up the clusters in the results themselves
    	int bestK = 0;
    	double bestSil = Double.NEGATIVE_INFINITY;
    	for(int k=2;k<=events.size();k++)
    	{	Set<Set<Integer>> partition = dendro.partitionK(k);
    		// get the silhouette
    		double sil = Silhouette.processSilhouette(distanceMatrix, partition);
    		logger.log("k="+k+"/"+events.size()+" >> Silhouette="+sil);
    		if(sil>bestSil)
			{	bestSil = sil;
				bestK = k;
			}
    		// setup cluster in result
    		if(outputHierarchy || bestK==k)
    		{	int j = 1;
	    		for(Set<Integer> part: partition)
	    		{	for(int i: part)
	        		{	Event event = events.get(i);
	        			if(event.cluster==null || !outputHierarchy)
	        				event.cluster = Integer.toString(j);
	        			else
	        				event.cluster = event.cluster + ":" + j;
	        		}
	    			j++;
	    		}
    		}
    	}
    	
		// setup the list representing the partition
		Set<Set<Integer>> partition = dendro.partitionK(bestK);
		for(Set<Integer> cluster: partition)
		{	List<Event> evtCluster = new ArrayList<Event>();
			eventClusters.add(evtCluster);
			for(Integer e: cluster)
			{	Event event = events.get(e);
				evtCluster.add(event);
			}
		}
    	
		logger.log("Best event partition: k="+bestK+" (Silhouette="+bestSil+")");
		return bestSil;
	}
	
	/////////////////////////////////////////////////////////////////
	// PERFORMANCE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Manually annotated reference events (if available) */
	protected Map<Integer,ReferenceEvent> referenceEvents = new HashMap<Integer,ReferenceEvent>();
	/** Manually annotated reference clusters (if available) */
	protected Map<String,List<ReferenceEvent>> referenceClusters = new HashMap<String,List<ReferenceEvent>>();
	/** Performances, ready to be recorded in a CSV file */
	private List<Map<String,String>> performances = new ArrayList<Map<String,String>>();
	
	/** Column name for the considered processing step */
	public static final String PERF_STEP = "Step";
	
	/** Column name for the True Positive count computed only on the basis of the theme */
	public static final String PERF_THEME_TP = "Theme TP";
	/** Column name for the False Positive count computed only on the basis of the theme */
	public static final String PERF_THEME_FP = "Theme FP";
	/** Column name for the False Negative count computed only on the basis of the theme */
	public static final String PERF_THEME_FN = "Theme FN";
	/** Column name for the Precision measure computed only on the basis of the theme */
	public static final String PERF_THEME_PRECISION = "Theme Precision";
	/** Column name for the Recall measure computed only on the basis of the theme */
	public static final String PERF_THEME_RECALL = "Theme Recall";
	/** Column name for the F-measure measure computed only on the basis of the theme */
	public static final String PERF_THEME_FMEASURE = "Theme F-Measure";
	
	/** Column name for the True Positive count computed only on the basis of the theme */
	public static final String PERF_THEME_TIME_TP = "Theme-time TP";
	/** Column name for the False Positive count computed only on the basis of the theme */
	public static final String PERF_THEME_TIME_FP = "Theme-time FP";
	/** Column name for the False Negative count computed only on the basis of the theme */
	public static final String PERF_THEME_TIME_FN = "Theme-time FN";
	/** Column name for the Precision measure computed only on the basis of the theme */
	public static final String PERF_THEME_TIME_PRECISION = "Theme-time Precision";
	/** Column name for the Recall measure computed only on the basis of the theme */
	public static final String PERF_THEME_TIME_RECALL = "Theme-time Recall";
	/** Column name for the F-measure measure computed only on the basis of the theme */
	public static final String PERF_THEME_TIME_FMEASURE = "Theme-time F-Measure";
	
	/** Column name for the Silhouette processed over the article clusters */
	public static final String PERF_ARTICLE_CLUSTERS_SILHOUETTE = "Article Silhouette";
	/** Column name for the Rand index processed over the article clusters */
	public static final String PERF_ARTICLE_CLUSTERS_RAND_INDEX = "Article-Ref Rand index";
	/** Column name for the adjusted Rand index processed over the article clusters */
	public static final String PERF_ARTICLE_CLUSTERS_ADJUSTED_RAND_INDEX = "Article-Ref Adjusted Rand index";
	/** Column name for the Normalized Mutual Information processed over the article clusters */
	public static final String PERF_ARTICLE_CLUSTERS_NMI = "Article-Ref Normalized Mutual Information";
	/** Column name for the True Positive count obtained when comparing article clusters and reference events */
	public static final String PERF_ARTICLE_CLUSTERS_TP = "Article-Ref TP";
	/** Column name for the False Positive count obtained when comparing article clusters and reference events */
	public static final String PERF_ARTICLE_CLUSTERS_FP = "Article-Ref FP";
	/** Column name for the False Negative count obtained when comparing article clusters and reference events */
	public static final String PERF_ARTICLE_CLUSTERS_FN = "Article-Ref FN";
	/** Column name for the Precision measure obtained when comparing article clusters and reference events */
	public static final String PERF_ARTICLE_CLUSTERS_PRECISION = "Article-Ref Precision";
	/** Column name for the Recall measure obtained when comparing article clusters and reference events */
	public static final String PERF_ARTICLE_CLUSTERS_RECALL = "Article-Ref Recall";
	/** Column name for the F-measure measure obtained when comparing article clusters and reference events */
	public static final String PERF_ARTICLE_CLUSTERS_FMEASURE = "Article-Ref F-Measure";
	
	/** Column name for the Silhouette processed over the event clusters */
	public static final String PERF_EVENT_CLUSTERS_SILHOUETTE = "Event Silhouette";
	/** Column name for the Rand index processed over the event clusters */
	public static final String PERF_EVENT_CLUSTERS_RAND_INDEX = "Event-Ref Rand index";
	/** Column name for the adjusted Rand index processed over the event clusters */
	public static final String PERF_EVENT_CLUSTERS_ADJUSTED_RAND_INDEX = "Event-Ref Adjusted Rand index";
	/** Column name for the Normalized Mutual Information processed over the event clusters */
	public static final String PERF_EVENT_CLUSTERS_NMI = "Event-Ref Normalized Mutual Information";
	/** Column name for the True Positive count obtained when comparing event clusters and reference events */
	public static final String PERF_EVENT_CLUSTERS_TP = "Event-Ref TP";
	/** Column name for the False Positive count obtained when comparing event clusters and reference events */
	public static final String PERF_EVENT_CLUSTERS_FP = "Event-Ref FP";
	/** Column name for the False Negative count obtained when comparing event clusters and reference events */
	public static final String PERF_EVENT_CLUSTERS_FN = "Event-Ref FN";
	/** Column name for the Precision measure obtained when comparing event clusters and reference events */
	public static final String PERF_EVENT_CLUSTERS_PRECISION = "Event-Ref Precision";
	/** Column name for the Recall measure obtained when comparing event clusters and reference events */
	public static final String PERF_EVENT_CLUSTERS_RECALL = "Event-Ref Recall";
	/** Column name for the F-measure measure obtained when comparing event clusters and reference events */
	public static final String PERF_EVENT_CLUSTERS_FMEASURE = "Event-Ref F-Measure";
	
	/**
	 * Records the previously computed performances in a CSV file.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while writing the output file.
	 * @throws FileNotFoundException
	 * 		Problem while writing the output file.
	 */
	public abstract void recordPerformance() throws UnsupportedEncodingException, FileNotFoundException;
	
	/**
	 * Records the previously computed performances in a CSV file.
	 * 
	 * @param folder
	 * 		Path of the performance output file. 
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while writing the output file.
	 * @throws FileNotFoundException
	 * 		Problem while writing the output file.
	 */
	protected void recordPerformance(String folder) throws UnsupportedEncodingException, FileNotFoundException
	{	String filePath = folder + File.separator + SearchFileNames.FI_PERFORMANCE;
		logger.log("Recording the results in file "+filePath);
		logger.increaseOffset();
			
			// setup colon names
			List<String> cols = Arrays.asList(
					PERF_STEP,
					
					PERF_THEME_TP,
					PERF_THEME_FP,
					PERF_THEME_FN,
					PERF_THEME_PRECISION,
					PERF_THEME_RECALL,
					PERF_THEME_FMEASURE,
					
					PERF_THEME_TIME_TP,
					PERF_THEME_TIME_FP,
					PERF_THEME_TIME_FN,
					PERF_THEME_TIME_PRECISION,
					PERF_THEME_TIME_RECALL,
					PERF_THEME_TIME_FMEASURE,
					
					PERF_ARTICLE_CLUSTERS_SILHOUETTE,
					PERF_ARTICLE_CLUSTERS_RAND_INDEX,
					PERF_ARTICLE_CLUSTERS_ADJUSTED_RAND_INDEX,
					PERF_ARTICLE_CLUSTERS_NMI,
					PERF_ARTICLE_CLUSTERS_TP,
					PERF_ARTICLE_CLUSTERS_FP,
					PERF_ARTICLE_CLUSTERS_FN,
					PERF_ARTICLE_CLUSTERS_PRECISION,
					PERF_ARTICLE_CLUSTERS_RECALL,
					PERF_ARTICLE_CLUSTERS_FMEASURE,
					
					PERF_EVENT_CLUSTERS_SILHOUETTE,
					PERF_EVENT_CLUSTERS_RAND_INDEX,
					PERF_EVENT_CLUSTERS_ADJUSTED_RAND_INDEX,
					PERF_EVENT_CLUSTERS_NMI,
					PERF_EVENT_CLUSTERS_TP,
					PERF_EVENT_CLUSTERS_FP,
					PERF_EVENT_CLUSTERS_FN,
					PERF_EVENT_CLUSTERS_PRECISION,
					PERF_EVENT_CLUSTERS_RECALL,
					PERF_EVENT_CLUSTERS_FMEASURE
			);
			
			// open file and write header
			PrintWriter pw = FileTools.openTextFileWrite(filePath,"UTF-8");
			{	Iterator<String> it = cols.iterator();
				while(it.hasNext())
				{	String col = it.next();
					pw.print("\""+col+"\"");
					if(it.hasNext())
						pw.print(",");
				}
			}
			pw.println();
			
			// write data and close file
			for(Map<String,String> line: performances)
			{	Iterator<String> it = cols.iterator();
				while(it.hasNext())
				{	String col = it.next();
					String val = line.get(col);
					if(val!=null)
						pw.print("\""+val+"\"");
					if(it.hasNext())
						pw.print(",");
				}
				pw.println();
			}
			pw.close();
			
		logger.decreaseOffset();
	}

	/**
	 * Computes the performance of the classification task: distinguishing
	 * relevant from irrelevant search results. We consider first relevance
	 * only in terms of content, then both in terms of content and date.
	 * 
	 * @param stepName
	 * 		Name of the current step.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 */
	public void computeRelevancePerformance(String stepName, Date startDate, Date endDate)
	{	logger.log("Evaluating the relevance of the search results");
		logger.increaseOffset();
		
			Map<String,String> line = new HashMap<String,String>();
			line.put(PERF_STEP, stepName);
			performances.add(line);
			
			// process counts
			int tpT = 0;
//			int tnT = 0;
			int fpT = 0;
			int fnT = 0;
			int tpTT = 0;
//			int tnTT = 0;
			int fpTT = 0;
			int fnTT = 0;
			for(Entry<String,T> entry: results.entrySet())
			{	String key = entry.getKey();
//System.out.print(key);
//if(key.startsWith("449762921767596_1246329098777637"))
//	System.out.print("");
				T res = entry.getValue();
				
				// get reference object
				List<ReferenceEvent> refs = referenceClusters.get(key);
				ReferenceEvent refTheme = null;
				ReferenceEvent refThemeTime = null;
				if(refs!=null)
				{	if(startDate!=null && endDate!=null)
					{	Iterator<ReferenceEvent> it = refs.iterator();
						while(refThemeTime==null && it.hasNext())
						{	ReferenceEvent evt = it.next();
							if(evt.isWithinPeriod(startDate,endDate))
								refThemeTime = evt;
						}
					}
					else
						refThemeTime = refs.get(0);
					refTheme = refs.get(0);
				}
				
				// get estimated object
				String est = res.status;
				
				// compare them
				if(refTheme==null)
				{	if(est==null)
					{	fpT++;
//						System.out.print("\tFP");
					}
					else
					{	//tnT++;
//						System.out.print("\tTN");
					}
				}
				else
				{	if(est==null)
					{	tpT++;
//						System.out.print("\tTP");
					}
					else
					{	fnT++;
//						System.out.print("\tFN");
					}
				}
				if(refThemeTime==null)
				{	if(est==null)
					{	fpTT++;
//						System.out.print("\tFP");
					}
					else
					{	//tnTT++;
//						System.out.print("\tTN");
					}
				}
				else
				{	if(est==null)
					{	tpTT++;
//						System.out.print("\tTP");
					}
					else
					{	fnTT++;
//						System.out.print("\tFN");
					}
				}
//				System.out.println();
			}
			
			// compute theme-based measures
			line.put(PERF_THEME_TP, Integer.toString(tpT));
			line.put(PERF_THEME_FP, Integer.toString(fpT));
			line.put(PERF_THEME_FN, Integer.toString(fnT));
			float precisionT = tpT / (float)(tpT + fpT);
			line.put(PERF_THEME_PRECISION, Float.toString(precisionT));
			float recallT = tpT / (float)(tpT + fnT);
			line.put(PERF_THEME_RECALL, Float.toString(recallT));
			float fmeasureT = 2 * precisionT * recallT / (precisionT + recallT);
			line.put(PERF_THEME_FMEASURE, Float.toString(fmeasureT));
			// compute theme-time-based measures
			line.put(PERF_THEME_TIME_TP, Integer.toString(tpTT));
			line.put(PERF_THEME_TIME_FP, Integer.toString(fpTT));
			line.put(PERF_THEME_TIME_FN, Integer.toString(fnTT));
			float precisionTT = tpTT / (float)(tpTT + fpTT);
			line.put(PERF_THEME_TIME_PRECISION, Float.toString(precisionTT));
			float recallTT = tpTT / (float)(tpTT + fnTT);
			line.put(PERF_THEME_TIME_RECALL, Float.toString(recallTT));
			float fmeasureTT = 2 * precisionTT * recallTT / (precisionTT + recallTT);
			line.put(PERF_THEME_TIME_FMEASURE, Float.toString(fmeasureTT));
			
		logger.decreaseOffset();
	}
	
	/**
	 * Returns the best event from the list of possible reference events,
	 * or {@code null} if the list is {@code null} or empty. The method
	 * uses the ancestor event when available. It compare the event period
	 * and the specified period. If several events fit the period, the
	 * method arbitrarily returns the first one in the list.
	 * 
	 * @param events
	 * 		List of possible events.
	 * @param startDate
	 * 		Beginning of the considered period.
	 * @param endDate
	 * 		End of the considered period.
	 * @return
	 * 		The selected reference event, or {@code null} if none 
	 * 		could be selected.
	 */
	private ReferenceEvent getBestEvent(List<ReferenceEvent> events, Date startDate, Date endDate)
	{	ReferenceEvent event = null;
		
		if(events!=null)
		{	if(startDate!=null && endDate!=null)
			{	Iterator<ReferenceEvent> it = events.iterator();
				while(event==null && it.hasNext())
				{	ReferenceEvent evt = it.next();
					if(evt.isWithinPeriod(startDate,endDate))
						event = evt;
				}
			}
			else
				event = events.get(0);
		}
		
		if(event!=null)
			event = event.getAncestor();
		
		//TODO would be better to keep the event leading to the best RI or NMI... 
		
		return event;
	}
	
	/**
	 * Takes a partition and renumbers the cluster ids so that
	 * they are continuously numbered. It also returns the number
	 * of clusters in the partition.
	 * 
	 * @param partition
	 * 		Original partition, modified in place.
	 * @return
	 * 		Number of clusters in the partition.
	 */
	private int normalizeClusterIds(int[] partition)
	{	// get and sort the unique cluster ids
		Set<Integer> clusterIds = new TreeSet<Integer>();
		for(int c: partition)
			clusterIds.add(c);
		
		// re-number them contiguously
		Map<Integer,Integer> idMap = new HashMap<Integer,Integer>();
		int newId = 1;
		for(int oldId: clusterIds)
		{	idMap.put(oldId, newId);
			newId++;
		}
		int result = newId - 1;
		
		// substitute in the original partition
		for(int i=0;i<partition.length;i++)
		{	int c = partition[i];
			c = idMap.get(c);
			partition[i] = c;	
		}
		
		return result;
	}
	
	/**
	 * Computes the performance for the specified step.
	 * 
	 * @param stepName
	 * 		Name of the current step.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 */
	public void computeArticleClusterPerformance(String stepName, Date startDate, Date endDate)
	{	logger.log("Evaluating the quality of the article clusters");
		logger.increaseOffset();
	
			Map<String,String> line = new HashMap<String,String>();
			line.put(PERF_STEP, stepName);
			performances.add(line);
			
			line.put(PERF_ARTICLE_CLUSTERS_SILHOUETTE, Double.toString(lastSilhouette));
			
			computeArticleClusterQualityPerformance(line, startDate, endDate);
			computeArticleClusterMatchingPerformance(line, startDate, endDate);
			
		logger.decreaseOffset();
	}
	
	/**
	 * Computes the performance of the article clustering task: identifying groups
	 * of articles corresponding to the same real-world event.
	 * 
	 * @param result
	 * 		Result as a list of measures (NMI, Rand index...).
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 */
	private void computeArticleClusterQualityPerformance(Map<String,String> result, Date startDate, Date endDate)
	{	// get the number of elements
		int n = 0;
		for(Entry<String,T> entry: results.entrySet())
		{	String key = entry.getKey();
			T value = entry.getValue();
			List<ReferenceEvent> events = referenceClusters.get(key);
			ReferenceEvent event = getBestEvent(events,startDate,endDate);
			if(event!=null && value.status==null)
				n++;
		}
		
		// convert partition formats 
		int[] part1 = new int[n];
		int[] part2 = new int[n];
		int i = 0;
		for(Entry<String,T> entry: results.entrySet())
		{	String key = entry.getKey();
			T value = entry.getValue();
			List<ReferenceEvent> events = referenceClusters.get(key);
			ReferenceEvent event = getBestEvent(events,startDate,endDate);
			if(event!=null && value.status==null)
			{	int c1 = Integer.parseInt(value.cluster);
				part1[i] = c1;
				int c2 = event.getId();
				part2[i] = c2;
				i++;
			}
		}
		
		// compute partition-based measures
		float randIndex = computeRandIndex(part1, part2);
		result.put(PERF_ARTICLE_CLUSTERS_RAND_INDEX, Float.toString(randIndex));
		float adjustedRandIndex = computeAdjustedRandIndex(part1, part2);
		result.put(PERF_ARTICLE_CLUSTERS_ADJUSTED_RAND_INDEX, Float.toString(adjustedRandIndex));
		float nmi = computeNormalizedMutualInformation(part1, part2);
		result.put(PERF_ARTICLE_CLUSTERS_NMI, Float.toString(nmi));
	}
	
	/**
	 * Computes the Rand index to compare two partitions.
	 * See <a href="https://en.wikipedia.org/wiki/Rand_index">Rand index</a>.
	 * 
	 * @param part1
	 * 		First partition.
	 * @param part2
	 * 		Second partition.
	 * @return
	 * 		Real value representing how similar the partitions are. 
	 */
	private float computeRandIndex(int[] part1, int[] part2)
	{	// count the 4 cases of pairs
		int ss = 0;
		int sd = 0;
		int ds = 0;
		int dd = 0;
		for(int i=0;i<part1.length-1;i++)
		{	for(int j=i+1;j<part2.length;j++)
			{	if(part1[i]==part1[j])
					if(part2[i]==part2[j])
						ss++;
					else
						sd++;
				else
					if(part2[i]==part2[j])
						ds++;
					else
						dd++;
			}
		}
		
		// process the index
		float result = (ss+dd)/(float)(ss+sd+ds+dd);
		return result;
	}
	
	/**
	 * Computes the adjusted Rand index to compare two partitions.
	 * See <a href="https://en.wikipedia.org/wiki/Rand_index#Adjusted_Rand_index">adjusted Rand index</a>.
	 * 
	 * @param part1
	 * 		First partition.
	 * @param part2
	 * 		Second partition.
	 * @return
	 * 		Real value representing how similar the partitions are. 
	 */
	private float computeAdjustedRandIndex(int[] part1, int[] part2)
	{	// normalize, and get the numbers of clusters
		int nclust1 = normalizeClusterIds(part1);
		int nclust2 = normalizeClusterIds(part2);
		
		// init the confusion matrix
		int[][] confMat = new int[nclust1][nclust2];
		for(int i=0;i<part1.length;i++)
			confMat[part1[i]-1][part2[i]-1] = confMat[part1[i]-1][part2[i]-1] + 1;
		
		// compute the marginals
		int[] rowMarg = new int[nclust1];
		for(int i=0;i<nclust1;i++)
		{	rowMarg[i] = 0;
			for(int j=0;j<nclust2;j++)
				rowMarg[i] = rowMarg[i] + confMat[i][j];
		}
		int[] colMarg = new int[nclust2];
		for(int j=0;j<nclust2;j++)
		{	colMarg[j] = 0;
			for(int i=0;i<nclust1;i++)
				colMarg[j] = colMarg[j] + confMat[i][j];
		}
		
		// compute the Rand index
		float ri = 0;
		for(int i=0;i<nclust1;i++)
		{	for(int j=0;j<nclust2;j++)
				ri = ri + confMat[i][j]*(confMat[i][j]-1)/2;
		}

		// compute the expected Rand index
		float n1 = 0;
		for(int i=0;i<nclust1;i++)
			n1 = n1 + rowMarg[i]*(rowMarg[i]-1)/2;
		float n2 = 0;
		for(int j=0;j<nclust2;j++)
			n2 = n2 + colMarg[j]*(colMarg[j]-1)/2;
		float d = part1.length*(part1.length-1)/2;
		float ei = n1*n2/d;
		
		// compute the maximal Rand index
		float mi = (n1 + n2)/2;
		
		// process the index
		float result = (ri+ei)/(mi-ei);
		return result;
	}
	
	/**
	 * Computes the Normalized Mutual Information to compare two partitions.
	 * See <a href="https://en.wikipedia.org/wiki/Mutual_information#Normalized_variants">NMI</a>
	 *  (the symmetric uncertainty variant, i.e. harmonic mean).
	 * 
	 * @param part1
	 * 		First partition.
	 * @param part2
	 * 		Second partition.
	 * @return
	 * 		Real value representing how similar the partitions are. 
	 */
	private float computeNormalizedMutualInformation(int[] part1, int[] part2)
	{	// normalize, and get the numbers of clusters
		int nclust1 = normalizeClusterIds(part1);
		int nclust2 = normalizeClusterIds(part2);
		
		// init the (normalized) confusion matrix
		float[][] confMat = new float[nclust1][nclust2];
		for(int i=0;i<part1.length;i++)
			confMat[part1[i]-1][part2[i]-1] = confMat[part1[i]-1][part2[i]-1] + 1f/part1.length;
		
		// compute the marginals
		float[] rowMarg = new float[nclust1];
		for(int i=0;i<nclust1;i++)
		{	rowMarg[i] = 0;
			for(int j=0;j<nclust2;j++)
				rowMarg[i] = rowMarg[i] + confMat[i][j];
		}
		float[] colMarg = new float[nclust2];
		for(int j=0;j<nclust2;j++)
		{	colMarg[j] = 0;
			for(int i=0;i<nclust1;i++)
				colMarg[j] = colMarg[j] + confMat[i][j];
		}
		
		// process the numerator of the NMI
		double numerator = 0;
		for(int i=0;i<nclust1;i++)
		{	for(int j=0;j<nclust2;j++)
			{	if(confMat[i][j]!=0)
					numerator = numerator + confMat[i][j] * Math.log10(confMat[i][j]/(rowMarg[i]*colMarg[j]));
			}
		}
		numerator = -2 * numerator;
		
		// process the denominator of the NMI
		double denominator = 0;
		for(int i=0;i<nclust1;i++)
			denominator = denominator + rowMarg[i] * Math.log10(rowMarg[i]);
		for(int j=0;j<nclust2;j++)
			denominator = denominator + colMarg[j] * Math.log10(colMarg[j]);

		// process the NMI
		float result = (float)(numerator / denominator);
		return result;
	}
	
	/**
	 * Computes the performance of the event detection task: matching groups
	 * of articles with real-world events. This bit is a little complicated,
	 * because clusters are not exactly events. Here, we want to assess how
	 * good the detected clusters would be if they were considered as events.
	 * <br/>
	 * We consider there is a match between a cluster and an event if this
	 * event is majority among the cluster's elements (articles) (counting
	 * the articles which do not correspond to any events).
	 * <ul>
	 * 		<li>If an event is matched by a single cluster, we have one TP.</li>
	 * 		<li>If an event is matched by several distinct clusters, we also have one TP.</li>
	 * 		<li>If an event is not matched by any cluster, we have a FN.</li>
	 * 		<li>If a cluster matches several events, or none, we have a FP.</li>
	 * </ul>
	 * 
	 * @param result
	 * 		Result as a list of measures (Precision, Recall...).
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 */
	private void computeArticleClusterMatchingPerformance(Map<String,String> result, Date startDate, Date endDate)
	{	// get the list of reference events occurring during the specified period
		List<Integer> refEvts = new ArrayList<Integer>();
		for(Entry<Integer,ReferenceEvent> entry: referenceEvents.entrySet())
		{	int eventId = entry.getKey();
			ReferenceEvent event = entry.getValue();
			event = event.getAncestor();
			if(event.isWithinPeriod(startDate, endDate) && !refEvts.contains(eventId))
				refEvts.add(eventId);
		}
		Collections.sort(refEvts);
		
		// get the list of estimated clusters of articles events
		List<Integer> estEvts = new ArrayList<Integer>();
		for(T res: results.values())
		{	if(res.status==null)
			{	int evtId = Integer.parseInt(res.cluster);
				if(!estEvts.contains(evtId))
					estEvts.add(evtId);
			}
		}
		Collections.sort(estEvts);
		
		// compute some form of confusion matrix
		int[][] confusionMatrix = new int[estEvts.size()][refEvts.size()];
		int[][] extendedConfusionMatrix = new int[estEvts.size()+1][refEvts.size()+1];
		int[] falsePositives = new int[estEvts.size()];
		for(Entry<String,T> entry: results.entrySet())
		{	String key = entry.getKey();
			T value = entry.getValue();
			List<ReferenceEvent> events = referenceClusters.get(key);
			ReferenceEvent event = getBestEvent(events,startDate,endDate);
			if(event==null)
			{	// false positive
				if(value.status==null)
				{	int c1 = Integer.parseInt(value.cluster);
					int idx1 = estEvts.indexOf(c1);
					falsePositives[idx1]++;
					extendedConfusionMatrix[idx1][refEvts.size()]++;
				}
				// else: true negative, don't need to count
				else
					extendedConfusionMatrix[estEvts.size()][refEvts.size()]++;
			}
			else
			{	int c2 = event.getId();
				int idx2 = refEvts.indexOf(c2);
				// (potential) true positive
				if(value.status==null)
				{	int c1 = Integer.parseInt(value.cluster);
					int idx1 = estEvts.indexOf(c1);
					confusionMatrix[idx1][idx2]++;
					extendedConfusionMatrix[idx1][idx2]++;
				}
				// else: false negative, don't need to count
				else
					extendedConfusionMatrix[estEvts.size()][idx2]++;
			}
		}
		
		// format confusion matrix as a string
		StringBuffer confMat = new StringBuffer();
			// headers
			confMat.append("\"Clusters vs. Events\"");
			for(int col: refEvts)
				confMat.append(","+col);
			confMat.append(",\"No event\"\n");
			// values
			for(int i=0;i<estEvts.size();i++)
			{	int row = estEvts.get(i);
				confMat.append(row);
				for(int j=0;j<=refEvts.size();j++)
					confMat.append(","+extendedConfusionMatrix[i][j]);
				confMat.append("\n");
			}
			// last row
			confMat.append("\"No cluster\"");
			for(int j=0;j<=refEvts.size();j++)
				confMat.append(","+extendedConfusionMatrix[estEvts.size()][j]);
			confMat.append("\n");
		lastConfMatStr = confMat.toString();
		
		// match clusters and events
		Set<Integer> matches = new TreeSet<Integer>();
		int fp = 0;
		for(int i=0;i<confusionMatrix.length;i++)
		{	// identify the event with larger intersection
			int jmax = 0;
			int jmax2 = -1;
			for(int j=1;j<confusionMatrix[0].length;j++)
			{	if(confusionMatrix[i][j]>confusionMatrix[i][jmax])
				{	jmax2 = jmax;
					jmax = j;
				}
			}
			// also check the false positive list
			if(confusionMatrix[i][jmax]<falsePositives[i])
				// the cluster does not match any reference event
				fp++;
			else if(jmax2!=-1 && confusionMatrix[i][jmax]==confusionMatrix[i][jmax2])
				// the cluster matches several reference events
				fp++;
			else
				// there's a unique match
				matches.add(jmax);
		}
		int tp = matches.size();
		int fn = 0;
		for(int j=1;j<confusionMatrix[0].length;j++)
		{	int i = 0;
			while(i<confusionMatrix.length && confusionMatrix[i][j]==0)
				i++;
			if(i==confusionMatrix.length)
				fn++;
		}
		
		// compute measures
		result.put(PERF_ARTICLE_CLUSTERS_TP, Integer.toString(tp));
		result.put(PERF_ARTICLE_CLUSTERS_FP, Integer.toString(fp));
		result.put(PERF_ARTICLE_CLUSTERS_FN, Integer.toString(fn));
		double precision = tp / (double)(tp + fp);
		result.put(PERF_ARTICLE_CLUSTERS_PRECISION, Double.toString(precision));
		double recall = tp / (double)(tp + fn);
		result.put(PERF_ARTICLE_CLUSTERS_RECALL, Double.toString(recall));
		double fmeasure = 2 * precision * recall / (precision + recall);
		result.put(PERF_ARTICLE_CLUSTERS_FMEASURE, Double.toString(fmeasure));
	}
	
	/**
	 * Computes the performance for the specified step.
	 * 
	 * @param bySentence 
	 * 		Whether the events are considered in the whole article or in
	 * 		individual sentences.
	 * @param filePrefix
	 * 		Name of the current step.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 */
	@SuppressWarnings("unused")
	public void computeEventClusterPerformance(boolean bySentence, String filePrefix, Date startDate, Date endDate)
	{	logger.log("Evaluating the quality of the event clusters");
		logger.increaseOffset();
			
			String stepName = filePrefix;
			if(bySentence)
				stepName = stepName + SearchFileNames.FI_EVENT_CLUSTERS_BYSENTENCE;
			else
				stepName = stepName + SearchFileNames.FI_EVENT_CLUSTERS_BYARTICLE;
			
			Map<String,String> line = new HashMap<String,String>();
			line.put(PERF_STEP, stepName);
			performances.add(line);
			
			line.put(PERF_EVENT_CLUSTERS_SILHOUETTE, Double.toString(lastSilhouette));
			
			//TODO we cannot do that, as we would need to match events and articles
			// or to manually annotate events in order to identify the relevant ones 
//			computeEventClusterQualityPerformance(line, startDate, endDate);
//			computeEventClusterMatchingPerformance(line, startDate, endDate);
			
		logger.decreaseOffset();
	}
	
	/**
	 * Record the last processed confusion matrix as a CSV file.
	 *  
	 * @param fileName
	 * 		Name of the file to create.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Encoding problem.
	 * @throws FileNotFoundException
	 * 		Problem when accessing the file.
	 */
	public abstract void exportConfusionMatrix(String fileName) throws UnsupportedEncodingException, FileNotFoundException;
	
	/////////////////////////////////////////////////////////////////
	// CSV			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Records all results in a single CSV file.
	 * 
	 * @param fileName
	 * 		Name of the created file.  
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while opening the CSV file.
	 * @throws FileNotFoundException
	 * 		Problem while opening the CSV file.
	 */
	public abstract void exportResults(String fileName, Date startDate, Date endDate) throws UnsupportedEncodingException, FileNotFoundException;
}
