package fr.univavignon.nerwip;

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
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import fr.univavignon.extractor.data.event.Event;
import fr.univavignon.extractor.temp.eventcomparison.EventComparison;
import fr.univavignon.extractor.temp.eventextraction.EventExtraction;
import fr.univavignon.extractor.temp.tools.dbpedia.DbIdTools;
import fr.univavignon.extractor.temp.tools.dbpedia.DbTypeTools;
import fr.univavignon.extractor.temp.tools.dbspotlight.SpotlightTools;
import fr.univavignon.extractor.temp.tools.mediawiki.WikiIdTools;
import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.article.ArticleList;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.edition.MentionEditor;
import fr.univavignon.nerwip.evaluation.recognition.RecognitionEvaluator;
import fr.univavignon.nerwip.evaluation.recognition.measures.AbstractRecognitionMeasure;
import fr.univavignon.nerwip.evaluation.recognition.measures.RecognitionIstanbulMeasure;
import fr.univavignon.nerwip.evaluation.recognition.measures.RecognitionLilleMeasure;
import fr.univavignon.nerwip.evaluation.recognition.measures.RecognitionMucMeasure;
import fr.univavignon.nerwip.processing.InterfaceLinker;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.InterfaceResolver;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.combiner.AbstractCombinerDelegateRecognizer.SubeeMode;
import fr.univavignon.nerwip.processing.combiner.fullcombiner.FullCombiner;
import fr.univavignon.nerwip.processing.combiner.straightcombiner.StraightCombiner;
import fr.univavignon.nerwip.processing.combiner.svmbased.CombineMode;
import fr.univavignon.nerwip.processing.combiner.svmbased.SvmCombiner;
import fr.univavignon.nerwip.processing.combiner.svmbased.SvmTrainer;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteCombiner;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteMode;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteTrainer;
import fr.univavignon.nerwip.processing.external.nero.Nero;
import fr.univavignon.nerwip.processing.external.nero.NeroTagger;
import fr.univavignon.nerwip.processing.external.tagen.TagEn;
import fr.univavignon.nerwip.processing.external.tagen.TagEnModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.heideltime.HeidelTime;
import fr.univavignon.nerwip.processing.internal.modelbased.heideltime.HeidelTimeModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.Illinois;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.IllinoisModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.IllinoisTrainer;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipe;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipeModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipeTrainer;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlp;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlpModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlpTrainer;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.Stanford;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.StanfordModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.StanfordTrainer;
import fr.univavignon.nerwip.processing.internal.modelless.dateextractor.DateExtractor;
import fr.univavignon.nerwip.processing.internal.modelless.naiveresolver.NaiveResolver;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalais;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalaisLanguage;
import fr.univavignon.nerwip.processing.internal.modelless.opener.OpeNer;
import fr.univavignon.nerwip.processing.internal.modelless.spotlight.Spotlight;
import fr.univavignon.nerwip.processing.internal.modelless.subee.Subee;
import fr.univavignon.nerwip.processing.internal.modelless.wikipediadater.WikipediaDater;
import fr.univavignon.nerwip.retrieval.ArticleRetriever;
import fr.univavignon.nerwip.retrieval.reader.wikipedia.WikipediaReader;
import fr.univavignon.nerwip.tools.corpus.ArticleLists;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.freebase.FbIdTools;
import fr.univavignon.nerwip.tools.freebase.FbTypeTools;
import fr.univavignon.nerwip.tools.keys.KeyHandler;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.nerwip.tools.string.LinkTools;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * This class is used to launch some processes
 * testing the various features.
 * 
 * @author Vincent Labatut
 */
@SuppressWarnings({ "unused" })
public class Test
{	/**
	 * Basic main function, launches the
	 * required test. Designed to be modified
	 * and launched from Eclipse, no command-line
	 * options.
	 * 
	 * @param args
	 * 		None needed.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	public static void main(String[] args) throws Exception
	{	
//		testStringTools();
		
//		URL url = new URL("http://en.wikipedia.org/wiki/John_Zorn");
//		URL url = new URL("http://en.wikipedia.org/wiki/Fleur_Pellerin");
//		URL url = new URL("http://en.wikipedia.org/wiki/Aart_Kemink");
//		URL url = new URL("http://en.wikipedia.org/wiki/Ibrahim_Maalouf");
//		URL url = new URL("http://en.wikipedia.org/wiki/Catherine_Jacob_(journalist)");

//		String name = "Émilien_Brigault";
//		String name = "Aimé Piton";
//		String name = "Albert_Chauly";
//		String name = "Gilles_Marcel_Cachin";
//    	String name = "Alexandre_Bracke";
//    	String name = "Achille_Eugène_Fèvre";
    	String name = "Adolphe_Lucien_Lecointe";

//		String name = "Barack_Obama";
     	
//     	String S = "journaliste";
//     	String T = "socialiste";
		
//		testArticleRetriever(url);
//		testArticlesRetriever();
//		testCategoryRetriever();
//		testFbidRetriever();
//		testTypeRetriever();
//		testDbIdRetriever();
//		testDbTypeRetriever();
//		testWikiIdRetriever();
//		testWikiTypeRetriever();
		
//		testTreeTagger();
//		testHeidelTimeRaw();

//		testDbIdRetriever();
//		testDbTypeRetriever();
//		testOpeNer(name);
//		testSpotlightRecognizer(name);
//		testSpotlightResolver(name);
//     	testNLDistance(S, T);
//		testEventsExtraction();
//		testEventComparison();
//		testFunction();
//		test2();
//		testSpotlightAllCorpus();
//		testEventExtraction();

//		testTagEnRaw();

//		testDateExtractor(url);
//		testHeidelTime(url);
//		testIllinois(url);
//		testLingPipe(url);
//		testNero(name);
//		testOpenCalais(url);
//		testOpenCalais(name);
//		testOpeNer(name);
//	    testOpenNlp(name);
//		testStanford(url);
//		testSubee(url);
//		testTagEn(name);
//		testWikipediaDater(url);
    	testNaiveResolver(name);
		
//		testVoteCombiner(url);
//		testSvmCombiner(url);
//		testStraightCombiner(name);
		
//		testEvaluator();
//		testEditor();
		
		logger.close();
		
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// RETRIEVAL	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** 
	 * Tests the features related to article retrieval. 
	 * 
	 * @param url
	 * 		URL of the article to retrieve.
	 *  
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testArticleRetriever(URL url) throws Exception
	{	logger.setName("Test-ArticleRetriever");
		logger.log("Start retrieving article "+url);
		logger.increaseOffset();
		
		ArticleRetriever retriever = new ArticleRetriever();
		retriever.setCacheEnabled(false);
		retriever.process(url);
		
		logger.decreaseOffset();
	}
	
	/** 
	 * Tests the features related to article retrieval. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testArticlesRetriever() throws Exception
	{	logger.setName("Test-ArticleRetriever");
		logger.log("Start retrieving all articles from the corpus");
		logger.increaseOffset();
		
		ArticleRetriever retriever = new ArticleRetriever();
		retriever.setCacheEnabled(true);
		
		List<URL> urls = ArticleLists.getArticleUrlList();
		for(URL url: urls)
		{	logger.log("Retrieving url "+url);
			Article article = retriever.process(url);
		}
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve the activity domain of a person 
	 * thanks to Freebase.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testCategoryRetriever() throws Exception
	{	logger.setName("Test-CategoryRetriever");
		logger.log("Start retrieving categories");
		logger.increaseOffset();
		
		WikipediaReader reader = new WikipediaReader();
		
		ArticleList files = ArticleLists.getArticleList();
		for(File file: files)
		{	String name = file.getName();
			Article article = new Article(name);
			logger.log("Retrieving article "+name);
			
			reader.getArticleCategoriesFromFb(article);
		}
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve DBpedia ids of entities.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testDbIdRetriever() throws Exception
	{	logger.setName("Test-DbIdRetriever");
		logger.log("Start retrieving ids");
		logger.increaseOffset();
		
		
		DbIdTools.getId("Paris");
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve DBpedia types of entities.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testDbTypeRetriever() throws Exception
	{	logger.setName("Test-DbTypeRetriever");
		logger.log("Start retrieving types");
		logger.increaseOffset();
		DbTypeTools.getAllTypes("Barack_Obama");
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve Wikidata ids of entities.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testWikiIdRetriever() throws Exception
	{	logger.setName("Test-WikiIdRetriever");
		logger.log("Start retrieving ids");
		logger.increaseOffset();
		
		
		WikiIdTools.getId("Barack%20Obama");
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve Wikidata types of entities.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testWikiTypeRetriever() throws Exception
	{	logger.setName("Test-WikiTypeRetriever");
		logger.log("Start retrieving types");
		logger.increaseOffset();
		
		String title = "Barack%20Obama";
		
		// retrieve all types
		List<String> types = new ArrayList<String>();
		logger.log("Types retrieved for "+title+":");
		logger.increaseOffset();
		logger.log(types);
		logger.decreaseOffset();
		
		// retrieve only notable type
		//String type = FbTypeTools.getNotableType(title);
		//logger.log("Notable type for "+title+": "+type);
		
		//logger.log("Type retrieval complete");
		//logger.decreaseOffset();
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve Freebase ids from Wikipedia titles.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testFbidRetriever() throws Exception
	{	logger.setName("Test-FBIDRetriever");
		logger.log("Start retrieving ids");
		logger.increaseOffset();
		
		FbIdTools.getId("Ibrahim_Maalouf");
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve types from Freebase.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testTypeRetriever() throws Exception
	{	logger.setName("Test-TypeRetriever");
		logger.log("Start retrieving types");
		logger.increaseOffset();
		
		String title = "Ibrahim_Maalouf";
		
		// retrieve all types
		List<String> types = FbTypeTools.getAllTypes(title);
		logger.log("Types retrieved for "+title+":");
		logger.increaseOffset();
		logger.log(types);
		logger.decreaseOffset();
		
		// retrieve only notable type
		String type = FbTypeTools.getNotableType(title);
		logger.log("Notable type for "+title+": "+type);
		
		logger.log("Type retrieval complete");
		logger.decreaseOffset();
	}
		
	/////////////////////////////////////////////////////////////////
	// EVENTS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	private static void testEventExtraction() throws Exception
	{
		logger.setName("Test-EventExtraction");
		logger.log("Start testing EventExtraction");
		logger.increaseOffset();
		Article article;
		Mentions mentions;
		
		ArticleList folders = ArticleLists.getArticleList();
		int i = 0;
		List<Event> allEventsList = new ArrayList<Event>();
		for(File folder: folders)
		{	logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
			logger.increaseOffset();
			// get the article texts
			logger.log("Retrieve the article");
			String name = folder.getName();
		    InterfaceRecognizer recognizer = new StraightCombiner();
		    ArticleRetriever retriever = new ArticleRetriever();
		    article = retriever.process(name);
		    String rawText = article.getRawText();
		    // retrieve the mentions
		   logger.log("Retrieve the mentions");
		   mentions = recognizer.recognize(article);
		   
		   List<Event> extractedEvents = EventExtraction.extractEvents(article, mentions); 
		   allEventsList.addAll(extractedEvents);
		
		}
		int size = allEventsList.size();
		logger.log("size of allEvientslist " + size);
		for (int k=0; k<= size -1; k++)
		{ String name = allEventsList.get(k).getPerson().getStringValue();
		logger.log("name " + k + name);
		//String date = allEventsList.get(k).getDate().getStringValue();
		//logger.log("date " + k + date);
		}
		
	}
	
	private static void testEventComparison() throws Exception
	{
		logger.setName("Test-EventComparison");
		logger.log("Start testing EventComparison");
		logger.increaseOffset();
		Article article;
		Mentions mentions;
		
		ArticleList folders = ArticleLists.getArticleList();
		int i = 0;
		for(File folder: folders)
		{	logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
			logger.increaseOffset();
			// get the article texts
			logger.log("Retrieve the article");
			String name = folder.getName();
		    InterfaceRecognizer recognizer = new StraightCombiner();
		    ArticleRetriever retriever = new ArticleRetriever();
		    article = retriever.process(name);
		    String rawText = article.getRawText();
		    // retrieve the mentions
		   logger.log("Retrieve the mentions");
		   mentions = recognizer.recognize(article);
	    
		   //event comparison
		List<Event> extractedEvents = EventExtraction.extractEvents(article, mentions);
		String xmlText = SpotlightTools.process(mentions, article);
		logger.log("xmltext = " + xmlText);
		String answer = SpotlightTools.disambiguate(xmlText);
		logger.log("answer = " + answer);
		
		int size = extractedEvents.size();
		logger.log("size is " + size);
		
		
		if (extractedEvents.size() > 1)
		{
			logger.log("starting event comparison");
			EventComparison.compareAllPairsOfEvents(extractedEvents, answer);
		}
		else logger.log("no events found");
		
		}
				
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve DBpediaSpotlight ids and types of entities.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testSpotlight() throws Exception
	{	logger.setName("Test-Spotlight");
		logger.log("Start testing Spotlight");
		logger.increaseOffset();
	    
		Article article;
		Mentions mentions;
		
		ArticleList folders = ArticleLists.getArticleList();
		int i = 0;
//		for(File folder: folders)
File folder = folders.get(0);		
		{	logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
			logger.increaseOffset();
			// get the article texts
			logger.log("Retrieve the article");
			String name = folder.getName();
			InterfaceRecognizer recognizer = new StraightCombiner();
			ArticleRetriever retriever = new ArticleRetriever();
			article = retriever.process(name);
			String rawText = article.getRawText();
			// retrieve the entities
			logger.log("Retrieve the entities");
			mentions = recognizer.recognize(article);
			logger.log("start applying Spotlight to " + name);
			String xmlText = SpotlightTools.process(mentions, article);
			//logger.log("xmltext = " + xmlText);
			String answer = SpotlightTools.disambiguate(xmlText);
			logger.log("answer = " + answer);

			//SpotlightTools.getEntitySpotlight(answer);
			//SpotlightTools.getIdSpotlight(answer);
			//SpotlightTools.getTypeSpotlight(answer);
			logger.decreaseOffset();
		}
	}
	
	/**
	 * Tests the normalization of the Levenshtein distance.
	 * 
     * @param str1
     * 		The first string.
     * @param str2
     *       The second string.
	 */
	private static void testNLDistance(String str1, String str2) 
	{	logger.setName("Test-NLDistance");
		logger.increaseOffset();
		StringTools.getNormalizedLevenshtein(str1, str2);
		logger.decreaseOffset();
	}
	
	//List<Event> extractEvents(Article article, Mentions entities)
	private static void testEventsExtraction() throws Exception
	{   logger.setName("Test-EventComparison");
	    logger.log("Start testing EventComparison");
	    logger.increaseOffset();
	    Article article;
	    Mentions mentions;
	
	    ArticleList folders = ArticleLists.getArticleList();
	    int i = 0;
	    for(File folder: folders)
	    {	logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
		    logger.increaseOffset();
		    // get the article texts
		    logger.log("Retrieve the article");
		    String name = folder.getName();
	        InterfaceRecognizer recognizer = new StraightCombiner();
	        ArticleRetriever retriever = new ArticleRetriever();
	        article = retriever.process(name);
	        String rawText = article.getRawText();
	        // retrieve the entities
	       logger.log("Retrieve the entities");
	       mentions = recognizer.recognize(article);
	   
		   
	
		   List<Event> extractedEvents = EventExtraction.extractEvents(article, mentions);
		   int p = extractedEvents.size();
		   logger.log("size of eventsList " + p);
		   }
	    
	}
	
	/////////////////////////////////////////////////////////////////
	// RECOGNITION	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testLingPipe(URL url) throws Exception
	{	logger.setName("Test-LingPipe");
		logger.log("Start testing LingPipe");
		logger.increaseOffset();

		// training
		{	// set articles
//			List<File> folders = Arrays.asList(
//				new File(FileNames.FO_OUTPUT + File.separator + "Aart_Kemink")
//				new File(FileNames.FO_OUTPUT + File.separator + "Abraham_Adan"),
//				new File(FileNames.FO_OUTPUT + File.separator + "Adolf_hitler")
//			);
//			ArticleList folders = ArticleLists.getArticleList();
			ArticleList folders = ArticleLists.getArticleList("training.set.txt");
			logger.log("Processed articles: ");
			logger.increaseOffset();
			for(File folder: folders)
				logger.log(folder.getName());
			logger.decreaseOffset();

			LingPipeTrainer trainer = new LingPipeTrainer(LingPipeModelName.NERWIP_MODEL);
			trainer.setCacheEnabled(false);
			trainer.process(folders);
		}
	
	
		// evaluation
//		{	ArticleRetriever retriever = new ArticleRetriever();
//			Article article = retriever.process(url);
//	
//			LingPipeModelName chunkingMethod = LingPipeModelName.APPROX_DICTIONARY;
//			boolean splitSentences = true;
//			boolean trim = true;
//			boolean exclusionOn = false;
//			boolean ignorePronouns = false;
//			LingPipe lingPipe = new LingPipe(chunkingMethod, true, splitSentences, trim, ignorePronouns, exclusionOn);
//			lingPipe.setCacheEnabled(false);
//			lingPipe.process(article);
//		}
		
		logger.decreaseOffset();
	}

	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testOpenCalais(URL url) throws Exception
	{	logger.setName("Test-OpenCalais");
		logger.log("Start testing OpenCalais");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(url);

		OpenCalaisLanguage lang = OpenCalaisLanguage.EN;
		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		OpenCalais openCalais = new OpenCalais(lang, ignorePronouns, exclusionOn);
		openCalais.setCacheEnabled(false);

		// only the specified article
//		openCalais.process(article);

		// all the corpus
		testAllCorpusRecognizer(openCalais,0);

		logger.decreaseOffset();
	}

	/**
	 * Tests the features related to NER. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testOpenCalais(String name) throws Exception
	{	logger.setName("Test-OpenCalais");
		logger.log("Start testing OpenCalais");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);

		OpenCalaisLanguage lang = OpenCalaisLanguage.FR;
		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		OpenCalais openCalais = new OpenCalais(lang, ignorePronouns, exclusionOn);
		openCalais.setOutputRawResults(true);
		openCalais.setCacheEnabled(false);
		
		// only the specified article
		openCalais.recognize(article);
		
		// all the corpus
//		testAllCorpus(openCalais,0);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the features related to NER. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	
	private static void testNero(String name) throws Exception
	{	logger.setName("Test-Nero");
		logger.log("Start testing Nero");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);

		NeroTagger neroTagger = NeroTagger.CRF;
		boolean flat = true;
		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		Nero nero = new Nero(neroTagger, flat, ignorePronouns, exclusionOn);
		nero.setOutputRawResults(true);
		nero.setCacheEnabled(false);
		
		// only the specified article
//		nero.process(article);

		// all the corpus
		testAllCorpusRecognizer(nero,0);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the features related to NER. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testTagEn(String name) throws Exception
	{	logger.setName("Test-TagEN");
		logger.log("Start testing TagEN");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);

		TagEnModelName model = TagEnModelName.MUC_MODEL;
		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		TagEn tagen = new TagEn(model,ignorePronouns, exclusionOn);
		tagen.setOutputRawResults(true);
		tagen.setCacheEnabled(false);
		
		// only the specified article
		tagen.recognize(article);
		
		// all the corpus
//		testAllCorpus(tagen,0);

		logger.decreaseOffset();
	}
	
	/**
	 * Tests the features related to NER. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testOpeNer(String name) throws Exception
	{	logger.setName("Test-OpeNer");
		logger.log("Start testing OpeNer");
		logger.increaseOffset();
		
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);
		
		boolean parenSplit = true;
		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		OpeNer opener = new OpeNer(parenSplit, ignorePronouns, exclusionOn);
		opener.setOutputRawResults(true);
		opener.setCacheEnabled(false);
		
		// only the specified article
		opener.recognize(article);
		
		// all the corpus
//		testAllCorpus(opener,0);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testStanford(URL url) throws Exception
	{	logger.setName("Test-Stanford");
		logger.log("Start testing Stanford");
		logger.increaseOffset();
	
		// training
		{	// set articles
//			List<File> folders = Arrays.asList(
//				new File(FileNames.FO_OUTPUT + File.separator + "Aart_Kemink")
//				new File(FileNames.FO_OUTPUT + File.separator + "Abraham_Adan"),
//				new File(FileNames.FO_OUTPUT + File.separator + "Adolf_hitler")
//			);
//			ArticleList folders = ArticleLists.getArticleList();
			ArticleList folders = ArticleLists.getArticleList("training.set.txt");
			logger.log("Processed articles: ");
			logger.increaseOffset();
			for(File folder: folders)
				logger.log(folder.getName());
			logger.decreaseOffset();

			StanfordTrainer trainer = new StanfordTrainer(StanfordModelName.NERWIP_MODEL);
			trainer.setCacheEnabled(false);
			trainer.process(folders);
		}
	
		// evaluation
		{	ArticleRetriever retriever = new ArticleRetriever();
			Article article = retriever.process(url);
	
			boolean exclusionOn = false;
			boolean ignorePronouns = false;
			StanfordModelName modelName = StanfordModelName.CONLLMUC_MODEL;
			Stanford stanford = new Stanford(modelName, true, ignorePronouns, exclusionOn);
			stanford.setCacheEnabled(false);
			stanford.recognize(article);
		}
		
		logger.decreaseOffset();
	}

	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testDateExtractor(URL url) throws Exception
	{	logger.setName("Test-DateExtractor");
		logger.log("Start testing DateExtractor");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(url);

		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		DateExtractor dateExtractor = new DateExtractor();
		dateExtractor.setCacheEnabled(false);
		dateExtractor.recognize(article);

		logger.decreaseOffset();
	}

	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testIllinois(URL url) throws Exception
	{	logger.setName("Test-Illinois");
		logger.log("Start testing Illinois");
		logger.increaseOffset();
		
		// training
		{	// set articles
//			List<File> folders = Arrays.asList(
//				new File(FileNames.FO_OUTPUT + File.separator + "Aart_Kemink")
//				new File(FileNames.FO_OUTPUT + File.separator + "Abraham_Adan"),
//				new File(FileNames.FO_OUTPUT + File.separator + "Adolf_hitler")
//			);
//			ArticleList folders = ArticleLists.getArticleList();
			ArticleList folders = ArticleLists.getArticleList("training.set.txt");
			logger.log("Processed articles: ");
			logger.increaseOffset();
			for(File folder: folders)
				logger.log(folder.getName());
			logger.decreaseOffset();

			IllinoisTrainer trainer = new IllinoisTrainer(IllinoisModelName.NERWIP_MODEL);
			trainer.setCacheEnabled(false);
			trainer.process(folders);
		}
	
		// evaluation
//		{	ArticleRetriever retriever = new ArticleRetriever();
//			Article article = retriever.process(url);
//	
//			boolean exclusionOn = false;
//			boolean ignorePronouns = false;
//			boolean trim = true;
//			IllinoisModelName modelName = IllinoisModelName.CONLL_MODEL;
//			Illinois illinois = new Illinois(modelName, true, trim, ignorePronouns, exclusionOn);
//			illinois.setCacheEnabled(false);
//			illinois.process(article);
//		}
		
		logger.decreaseOffset();
	}

	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testSubee(URL url) throws Exception
	{	logger.setName("Test-Subee");
		logger.log("Start testing Subee");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(url);

		boolean additionalOccurrences = true;
		boolean useTitle = true;
		boolean notableType = true;
		boolean useAcronyms = true;
		boolean discardDemonyms = true;
		Subee subee = new Subee(additionalOccurrences, useTitle, notableType, useAcronyms, discardDemonyms);
		subee.setCacheEnabled(false);
		subee.recognize(article);

		logger.decreaseOffset();
	}

	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testWikipediaDater(URL url) throws Exception
	{	logger.setName("Test-WikipediaDater");
		logger.log("Start testing WikipediaDater");
		logger.increaseOffset();
		
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(url);

		WikipediaDater wikipediaDater = new WikipediaDater();
		wikipediaDater.setCacheEnabled(false);
		wikipediaDater.recognize(article);

		logger.decreaseOffset();
	}

	/**
	 * Tests the features related to NER. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testOpenNlp(String name) throws Exception
	{	logger.setName("Test-OpenNlp");
		logger.log("Start testing OpenNlp");
		logger.increaseOffset();
		
//		// training
//		{	// set articles
////			List<File> folders = Arrays.asList(
////				new File(FileNames.FO_OUTPUT + File.separator + "Aart_Kemink")
////				new File(FileNames.FO_OUTPUT + File.separator + "Abraham_Adan"),
////				new File(FileNames.FO_OUTPUT + File.separator + "Adolf_hitler")
////			);
////			ArticleList folders = ArticleLists.getArticleList();
//			ArticleList folders = ArticleLists.getArticleList("training.set.txt");
//			logger.log("Processed articles: ");
//			logger.increaseOffset();
//			for(File folder: folders)
//				logger.log(folder.getName());
//			logger.decreaseOffset();
//
//			OpenNlpTrainer trainer = new OpenNlpTrainer(OpenNlpModelName.NERWIP_MODEL);
//			trainer.setCacheEnabled(false);
//			trainer.process(folders);
//		}
		
		// evaluation
		{	ArticleRetriever retriever = new ArticleRetriever();
			Article article = retriever.process(name);
	
			OpenNlpModelName modelName = OpenNlpModelName.NERC_MODEL;
			boolean exclusionOn = false;
			boolean ignorePronouns = false;
			OpenNlp openNlp = new OpenNlp(modelName, true, ignorePronouns, exclusionOn);
			openNlp.setCacheEnabled(false);
			openNlp.setOutputRawResults(true);
			
			// only the specified article
			openNlp.recognize(article);

			// all the corpus
//			testAllCorpusRecognizer(openNlp,0);
		}
		
		logger.decreaseOffset();
	}

	/**
	 * Tests the integration of HeidelTime
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testHeidelTimeRaw() throws Exception
	{	// command line test
		//String args[] = {"res\\ner\\treetagger\\README.txt","-vv","-c","res\\ner\\heideltime\\config.props"};
		//String args[] = {"res\\ner\\heideltime\\test-en.txt","-vv","-c","res\\ner\\heideltime\\config.props"};
//		String args[] = {"res\\ner\\heideltime\\test-fr.txt","-vv","-c","res\\ner\\heideltime\\config.props","-l","FRENCH"};
//		HeidelTimeStandalone.main(args);
		
		// internal test
		Language language = Language.FRENCH;
		DocumentType typeToProcess = DocumentType.NARRATIVES; //only for english
		OutputType outputType = OutputType.TIMEML;
		String configPath = FileNames.FO_HEIDELTIME + File.separator + "config.props";
		POSTagger posTagger = POSTagger.TREETAGGER;
		boolean doIntervalTagging = false;
		HeidelTimeStandalone nerTool = new HeidelTimeStandalone(language, typeToProcess, outputType, configPath, posTagger, doIntervalTagging);
		String fileName = FileNames.FO_HEIDELTIME+File.separator+"test-fr.txt";
		String document = FileTools.readTextFile(fileName, "UTF-8");
		String result = nerTool.process(document);
		System.out.println(result);
		result = result.replace("<!DOCTYPE TimeML SYSTEM \"TimeML.dtd\">", "");
		System.out.println("\nXML version:");
		SAXBuilder sb = new SAXBuilder();
		Document doc = sb.build(new StringReader(result));
		Element root = doc.getRootElement();
		XMLOutputter xo = new XMLOutputter();
		System.out.println(xo.outputString(doc));
	}
		
	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testHeidelTime(URL url) throws Exception
	{	logger.setName("Test-HeidelTime");
		logger.log("Start testing HeidelTime");
		logger.increaseOffset();
		
		{	ArticleRetriever retriever = new ArticleRetriever();
			Article article = retriever.process(url);
	
			HeidelTimeModelName modelName = HeidelTimeModelName.FRENCH_NARRATIVES;
			boolean doIntervalTagging = false;
			HeidelTime heidelTime = new HeidelTime(modelName, true, doIntervalTagging);
			heidelTime.setCacheEnabled(false);
			
			// only the specified article
//			heidelTime.process(article);

			// all the corpus
			testAllCorpusRecognizer(heidelTime,0);
		}
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the features related to NER. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testSpotlightRecognizer(String name) throws Exception
	{	logger.setName("Test-Spotlight-Recognizer");
		logger.log("Start testing Spotlight");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);
		
		float minConf = 0.3f;
		boolean resolveHomonyms = true;
		Spotlight spotlight = new Spotlight(minConf,resolveHomonyms);
		spotlight.setOutputRawResults(true);
		spotlight.setCacheEnabled(false);
		
		// only the specified article
//		spotlight.recognize(article);
		
		// all the corpus
		testAllCorpusRecognizer(spotlight,0);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testVoteCombiner(URL url) throws Exception
	{	logger.setName("Test-VoteCombiner");
		logger.log("Start testing VoteCombiner");
		logger.increaseOffset();

		boolean loadModelOnDemand = true;
		List<Boolean> specifics = Arrays.asList(
			false,
			true
		);
		List<VoteMode> voteModes = Arrays.asList(
			VoteMode.UNIFORM,
			VoteMode.WEIGHTED_OVERALL,
			VoteMode.WEIGHTED_CATEGORY
		);
		List<Boolean> useRecalls = Arrays.asList(
			false,
			true
		);
		List<Boolean> existVotes = Arrays.asList(
			false,
			true
		);
		List<SubeeMode> subeeModes = Arrays.asList(
			SubeeMode.NONE,
			SubeeMode.SINGLE,
			SubeeMode.ALL
		);
		
		for(boolean specific: specifics)
		{	logger.log("Now dealing with specific="+specific);
			logger.increaseOffset();
			
			for(VoteMode voteMode: voteModes)
			{	logger.log("Now dealing with voteMode="+voteMode);
				logger.increaseOffset();
				
				for(boolean useRecall: useRecalls)
				{	logger.log("Now dealing with useRecall="+useRecall);
					logger.increaseOffset();
					
					for(boolean existVote: existVotes)
					{	logger.log("Now dealing with existVote="+existVote);
						logger.increaseOffset();
						
						for(SubeeMode subeeMode: subeeModes)
						{	logger.log("Now dealing with subeeMode="+subeeMode);
							logger.increaseOffset();
							
							VoteCombiner voteCombiner = new VoteCombiner(loadModelOnDemand, specific, voteMode, useRecall, existVote, subeeMode);
							
							// train
							{	// set articles
//								List<File> folders = Arrays.asList(
//									new File(FileNames.FO_OUTPUT + File.separator + "Aart_Kemink")
//									new File(FileNames.FO_OUTPUT + File.separator + "Abraham_Adan"),
//									new File(FileNames.FO_OUTPUT + File.separator + "Adolf_hitler")
//								);
//								ArticleList folders = ArticleLists.getArticleList();
								ArticleList folders = ArticleLists.getArticleList("training.set.txt");
								logger.log("Processed articles: ");
								logger.increaseOffset();
								for(File folder: folders)
									logger.log(folder.getName());
								logger.decreaseOffset();
						
								VoteTrainer trainer = new VoteTrainer(voteCombiner);
								trainer.setSubCacheEnabled(true);
								trainer.setCacheEnabled(false);
								trainer.process(folders);
							}
						
//							// apply
//							{	// retrieve article
//								ArticleRetriever retriever = new ArticleRetriever();
//								Article article = retriever.process(url);
//					
//								voteCombiner.setSubCacheEnabled(true);
//								voteCombiner.setCacheEnabled(false);
//								voteCombiner.process(article);
//							}
							
							logger.log("Done with subeeMode="+subeeMode);
							logger.decreaseOffset();
						}
						
						logger.log("Done with existVote="+existVote);
						logger.decreaseOffset();
					}
					
					logger.log("Done with useRecall="+useRecall);
					logger.decreaseOffset();
				}
				
				logger.log("Done with voteMode="+voteMode);
				logger.decreaseOffset();
			}
			
			logger.log("Done with specific="+specific);
			logger.decreaseOffset();
		}
	
		logger.decreaseOffset();
	}

	/**
	 * Tests the features related to NER. 
	 * 
	 * @param url
	 * 		URL of the article to parse.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testSvmCombiner(URL url) throws Exception
	{	logger.setName("Test-SvmCombiner");
		logger.log("Start testing SvmCombiner");
		logger.increaseOffset();

		boolean loadModelOnDemand = true;
		List<Boolean> specifics = Arrays.asList(
//			false
			true
		);
		List<Boolean> useCategories = Arrays.asList(
			false
//			true
		);
		List<CombineMode> combineModes = Arrays.asList(
			CombineMode.MENTION_UNIFORM
//			CombineMode.MENTION_WEIGHTED_OVERALL
//			CombineMode.MENTION_WEIGHTED_CATEGORY
//			CombineMode.CHUNK_SINGLE
//			CombineMode.CHUNK_PREVIOUS
		);
		List<SubeeMode> subeeModes = Arrays.asList(
			SubeeMode.NONE
//			SubeeMode.SINGLE
//			SubeeMode.ALL
		);
		
		for(boolean specific: specifics)
		{	logger.log("Now dealing with specific="+specific);
			logger.increaseOffset();
				
			for(boolean useCats: useCategories)
			{	logger.log("Now dealing with useCategories="+useCats);
				logger.increaseOffset();
				
				for(CombineMode combineMode: combineModes)
				{	logger.log("Now dealing with combineMode="+combineMode);
					logger.increaseOffset();
					
					for(SubeeMode subeeMode: subeeModes)
					{	logger.log("Now dealing with subeeMode="+subeeMode);
						logger.increaseOffset();
	
						SvmCombiner svmCombiner = new SvmCombiner(loadModelOnDemand, specific, useCats, combineMode, subeeMode);
	
						// train
						{	// set articles
//							List<File> folders = Arrays.asList(
//								new File(FileNames.FO_OUTPUT + File.separator + "Aart_Kemink")
//								new File(FileNames.FO_OUTPUT + File.separator + "Abraham_Adan"),
//								new File(FileNames.FO_OUTPUT + File.separator + "Adolf_hitler")
//							);
//							ArticleList folders = ArticleLists.getArticleList();
							ArticleList folders = ArticleLists.getArticleList("training.set.txt");
//							ArticleList folders = ArticleLists.getArticleList("training.small.set.txt");
							logger.log("Processed articles: ");
							logger.increaseOffset();
							for(File folder: folders)
								logger.log(folder.getName());
							logger.decreaseOffset();
							
							SvmTrainer trainer = new SvmTrainer(svmCombiner);
							trainer.setSubCacheEnabled(true);
							trainer.setCacheEnabled(false);
							trainer.process(folders,false);
//							double c=1.0000000000000000;	double gamma=0.13333334028720856;	//	false	EntUnif	none
//							double c=1.0000000000000000;	double gamma=0.12440440535976931;	//	false	EntWghtOvrl	none
//							double c=1.0000000000000000;	double gamma=0.13333334028720856;	//	false	EntWghtCat	none
//						double c=0.5000000000000000;	double gamma=0.07999999821186067;	//	false	ChunkSngl	none
//						double c=0.5000000000000000;	double gamma=0.06666667014360428;	//	false	ChunkPrev	none
//							double c=0.9330329915368074;	double gamma=0.11111111193895341;	//	false	EntUnif	all
//							double c=0.9330329915368074;	double gamma=0.11908594116906976;	//	false	EntWghtOvrl	all
//							double c=1.0000000000000000;	double gamma=0.027777777984738353;	//	false	EntWghtCat	all
//						double c=0.5000000000000000;	double gamma=0.06666667014360428;	//	false	ChunkSngl	all
//						double c=0.5000000000000000;	double gamma=0.05714285746216773;	//	false	ChunkPrev	all
//						double c=1.0000000000000000;	double gamma=0.07407407462596895;	//	true	EntUnif	none
//						double c=1.0000000000000000;	double gamma=0.07407407462596895;	//	true	EntWghtOvrl	none
//						double c=1.0000000000000000;	double gamma=0.07407407462596895;	//	true	EntWghtCat	none
//						double c=0.5000000000000000;	double gamma=0.027027027681469914;	//	true	ChunkSngl	none
//						double c=0.5000000000000000;	double gamma=0.02380952425301075;	//	true	ChunkPrev	none
//						double c=1.0000000000000000;	double gamma=0.06666667014360428;	//	true	EntUnif	all
//						double c=1.0000000000000000;	double gamma=0.06666667014360428;	//	true	EntWghtOvrl	all
//						double c=1.0000000000000000;	double gamma=0.06666667014360428;	//	true	EntWghtCat	all
//						double c=0.5000000000000000;	double gamma=0.02380952425301075;	//	true	ChunkSngl	all
//						double c=0.5000000000000000;	double gamma=0.021276595070958138;	//	true	ChunkPrev	all
//							trainer.process(folders,c,gamma);
						}
					
//						// apply
//						{	// retrieve article
//							ArticleRetriever retriever = new ArticleRetriever();
//							Article article = retriever.process(url);
			//	
//							svmCombiner.setSubCacheEnabled(true);
//							svmCombiner.setCacheEnabled(false);
//							svmCombiner.process(article);
//						}
	
						logger.log("Done with subeeMode="+subeeMode);
						logger.decreaseOffset();
					}
					
					logger.log("Done with combineMode="+combineMode);
					logger.decreaseOffset();
				}
				
				logger.log("Done with useCategories="+useCats);
				logger.decreaseOffset();
			}
			
			logger.log("Done with specific="+specific);
			logger.decreaseOffset();
		}
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the features related to NER. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testStraightCombiner(String name) throws Exception
	{	logger.setName("Test-StraightCombiner");
		logger.log("Start testing StraightCombiner");
		logger.increaseOffset();
		
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);
		
		StraightCombiner straightCombiner = new StraightCombiner();
		straightCombiner.setCacheEnabled(false);
		
		// only the specified article
//		opener.process(article);
		
		// all the corpus
		testAllCorpusRecognizer(straightCombiner,0);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Applies the specified recognizer to the 
	 * whole corpus.
	 * 
	 * @param recognizer
	 * 		Recognizer to apply.
	 * @param start
	 * 		Which article to start from.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testAllCorpusRecognizer(InterfaceRecognizer recognizer, int start) throws Exception
	{	logger.log("Process each article individually");
		logger.increaseOffset();
		
		ArticleList folders = ArticleLists.getArticleList();
		int i = 0;
		for(File folder: folders)
		{	if(i>=start)
			{	// get the results
				logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
				logger.increaseOffset();
				
					// get article
					logger.log("Retrieve the article");
					String name = folder.getName();
					ArticleRetriever retriever = new ArticleRetriever();
					Article article = retriever.process(name);
						
					logger.log("Apply the recognizer");
					recognizer.recognize(article);
					
				logger.decreaseOffset();
			}
			i++;
		}
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// RESOLUTION	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests the features related to a resolver. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testSpotlightResolver(String name) throws Exception
	{	logger.setName("Test-Spotlight-Resolver");
		logger.log("Start testing Spotlight");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);

		float minConf = 0.3f;
		boolean ignorePronouns = true;
		boolean exclusionOn = true;
		boolean resolveHomonyms = true;
//		Spotlight spotlight = new Spotlight(minConf,resolveHomonyms);
		InterfaceRecognizer recognizer = new OpenCalais(OpenCalaisLanguage.FR, ignorePronouns, exclusionOn);
		Spotlight spotlight = new Spotlight(recognizer,resolveHomonyms);
		spotlight.setOutputRawResults(true);
		spotlight.setCacheEnabled(false);
		
		// only the specified article
//		spotlight.resolve(article);
		
		// all the corpus
		testAllCorpusResolver(spotlight,0);
		
		logger.decreaseOffset();
	}

	/**
	 * Tests the features related to a resolver. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testNaiveResolver(String name) throws Exception
	{	logger.setName("Test-Naive-Resolver");
		logger.log("Start testing Naive resolver");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);

		int maxDist = 4;
		boolean ignorePronouns = true;
		boolean exclusionOn = true;
		boolean resolveHomonyms = true;
		InterfaceRecognizer recognizer = new OpenCalais(OpenCalaisLanguage.FR, ignorePronouns, exclusionOn);
		NaiveResolver naiveResolver = new NaiveResolver(recognizer,maxDist);
		naiveResolver.setOutputRawResults(true);
		naiveResolver.setCacheEnabled(false);
		
		// only the specified article
		naiveResolver.resolve(article);
		
		// all the corpus
//		testAllCorpusResolver(naiveResolver,0);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Applies the specified resolver to the 
	 * whole corpus.
	 * 
	 * @param resolver
	 * 		Resolver to apply.
	 * @param start
	 * 		Which article to start from.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testAllCorpusResolver(InterfaceResolver resolver, int start) throws Exception
	{	logger.log("Process each article individually");
		logger.increaseOffset();
		
		ArticleList folders = ArticleLists.getArticleList();
		int i = 0;
		for(File folder: folders)
		{	if(i>=start)
			{	// get the results
				logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
				logger.increaseOffset();
				
					// get article
					logger.log("Retrieve the article");
					String name = folder.getName();
					ArticleRetriever retriever = new ArticleRetriever();
					Article article = retriever.process(name);
						
					logger.log("Apply the recognizer");
					resolver.resolve(article);
					
				logger.decreaseOffset();
			}
			i++;
		}
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// LINKING		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests the features related to a linker. 
	 * 
	 * @param name
	 * 		Name of the (already cached) article.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testSpotlightLinker(String name) throws Exception
	{	logger.setName("Test-Spotlight-Linker");
		logger.log("Start testing Spotlight");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);

		float minConf = 0.3f;
		boolean resolveHomonyms = true;
		Spotlight spotlight = new Spotlight(minConf,resolveHomonyms);
//		InterfaceRecognizer recognizer = new OpenCalais(OpenCalaisLanguage.FR, true, true);
//		Spotlight spotlight = new Spotlight(recognizer,resolveHomonyms);
		spotlight.setOutputRawResults(true);
		spotlight.setCacheEnabled(false);
		
		// only the specified article
		spotlight.link(article);
		
		// all the corpus
//		testAllCorpusLinker(spotlight,0);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Applies the specified linker to the 
	 * whole corpus.
	 * 
	 * @param linker
	 * 		Linker to apply.
	 * @param start
	 * 		Which article to start from.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testAllCorpusLinker(InterfaceLinker linker, int start) throws Exception
	{	logger.log("Process each article individually");
		logger.increaseOffset();
		
		ArticleList folders = ArticleLists.getArticleList();
		int i = 0;
		for(File folder: folders)
		{	if(i>=start)
			{	// get the results
				logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
				logger.increaseOffset();
				
					// get article
					logger.log("Retrieve the article");
					String name = folder.getName();
					ArticleRetriever retriever = new ArticleRetriever();
					Article article = retriever.process(name);
						
					logger.log("Apply the recognizer");
					linker.link(article);
					
				logger.decreaseOffset();
			}
			i++;
		}
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// EVALUATION	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests the features related to NER evaluation. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testEvaluator() throws Exception
	{	logger.setName("Test-RecognitionEvaluator");
		logger.log("Start evaluation test ");
		logger.increaseOffset();
		
		// set types
		List<EntityType> types = Arrays.asList(
			EntityType.DATE,
			EntityType.FUNCTION,
			EntityType.LOCATION,
			EntityType.MEETING,
			EntityType.ORGANIZATION,
			EntityType.PERSON,
			EntityType.PRODUCTION
		);
		logger.log("Processed types: ");
		logger.increaseOffset();
		for(EntityType type: types)
			logger.log("Type "+type);
		logger.decreaseOffset();
		
		// set recognizers
		boolean loadOnDemand = true;
		InterfaceRecognizer temp[] =
		{	
//			new DateExtractor(),
//			new WikipediaDater(),
			
//			new HeidelTime(HeidelTimeModelName.FRENCH_NARRATIVES, loadOnDemand, false),	
//			new HeidelTime(HeidelTimeModelName.FRENCH_NARRATIVES, loadOnDemand, true),	
//			new HeidelTime(HeidelTimeModelName.FRENCH_NEWS, loadOnDemand, false),	
//			new HeidelTime(HeidelTimeModelName.FRENCH_NEWS, loadOnDemand, true),	
//			new HeidelTime(HeidelTimeModelName.ENGLISH_COLLOQUIAL, loadOnDemand, false),	
//			new HeidelTime(HeidelTimeModelName.ENGLISH_COLLOQUIAL, loadOnDemand, true),	
//			new HeidelTime(HeidelTimeModelName.ENGLISH_NARRATIVES, loadOnDemand, false),	
//			new HeidelTime(HeidelTimeModelName.ENGLISH_NARRATIVES, loadOnDemand, true),	
//			new HeidelTime(HeidelTimeModelName.ENGLISH_NEWS, loadOnDemand, false),	
//			new HeidelTime(HeidelTimeModelName.ENGLISH_NEWS, loadOnDemand, true),	
//			new HeidelTime(HeidelTimeModelName.ENGLISH_SCIENTIFIC, loadOnDemand, false),	
//			new HeidelTime(HeidelTimeModelName.ENGLISH_SCIENTIFIC, loadOnDemand, true),	
				
//			new Illinois(IllinoisModelName.CONLL_MODEL, loadOnDemand, false, false, false),
//			new Illinois(IllinoisModelName.CONLL_MODEL, loadOnDemand, false, false, true),	// LOC, ORG, PERS
//			new Illinois(IllinoisModelName.CONLL_MODEL, loadOnDemand, false, true,  false),
//			new Illinois(IllinoisModelName.CONLL_MODEL, loadOnDemand, false, true,  true),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, loadOnDemand, false, false, false),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, loadOnDemand, false, false, true),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, loadOnDemand, false, true,  false),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, loadOnDemand, false, true,  true),
//			new Illinois(IllinoisModelName.NERWIP_MODEL, loadOnDemand, false, false, false),
//			new Illinois(IllinoisModelName.NERWIP_MODEL, loadOnDemand, false, false, true),
//			new Illinois(IllinoisModelName.NERWIP_MODEL, loadOnDemand, false, true, false),	// LOC, ORG, PERS
//			new Illinois(IllinoisModelName.NERWIP_MODEL, loadOnDemand, false, true, true),
				
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, false, false, false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, false, false, true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, false, true,  false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, false, true,  true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, true,  false, false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, true,  false, true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, true,  true,  false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, true,  true,  true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  false, false, false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  false, false, true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  false, true,  false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  false, true,  true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  true,  false, false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  true,  false, true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  true,  true,  false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  true,  true,  true),	// LOC, ORG, PERS
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, false, false, false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, false, false, true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, false, true,  false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, false, true,  true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, true,  false, false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, true,  false, true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, true,  true,  false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, true,  true,  true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  false, false, false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  false, false, true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  false, true,  false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  false, true,  true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  true,  false, false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  true,  false, true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  true,  true,  false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  true,  true,  true),	// LOC, ORG, PERS
			
//			new Nero(NeroTagger.CRF, false, false, false),
//			new Nero(NeroTagger.CRF, false, false, true),
//			new Nero(NeroTagger.CRF, false, true, false),
//			new Nero(NeroTagger.CRF, false, true, true),
//			new Nero(NeroTagger.CRF, true, false, false),
//			new Nero(NeroTagger.CRF, true, false, true),
//			new Nero(NeroTagger.CRF, true, true, false),
//			new Nero(NeroTagger.CRF, true, true, true),
//			new Nero(NeroTagger.FST, false, false, false),
//			new Nero(NeroTagger.FST, false, false, true),
//			new Nero(NeroTagger.FST, false, true, false),
//			new Nero(NeroTagger.FST, false, true, true),
//			new Nero(NeroTagger.FST, true, false, false),
//			new Nero(NeroTagger.FST, true, false, true),
//			new Nero(NeroTagger.FST, true, true, false),
//			new Nero(NeroTagger.FST, true, true, true),
			
//			new OpenCalais(OpenCalaisLanguage.EN, false, false),
//			new OpenCalais(OpenCalaisLanguage.EN, false, true),
//			new OpenCalais(OpenCalaisLanguage.EN, true,  false),	// (DATE), LOC, ORG, PERS	
//			new OpenCalais(OpenCalaisLanguage.EN, true,  true),
//			new OpenCalais(OpenCalaisLanguage.FR, false, false),
//			new OpenCalais(OpenCalaisLanguage.FR, false, true),
//			new OpenCalais(OpenCalaisLanguage.FR, true,  false),	
//			new OpenCalais(OpenCalaisLanguage.FR, true,  true),
			
//			new OpeNer(false, false, false),
//			new OpeNer(false, false, true),
//			new OpeNer(false, true, false),
//			new OpeNer(false, true, true),
//			new OpeNer(true, false, false),
//			new OpeNer(true, false, true),
//			new OpeNer(true, true, false),
//			new OpeNer(true, true, true),
			
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, false,false),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, false,true),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, true, false),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, true, true),	// DATE, LOC, ORG, PERS
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, false,false),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, false,true),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, true, false),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, true, true),	// LOC, ORG, PERS

//			new Spotlight(0.1f,true),	// LOC, MEET, ORG, PERS, PROD
//			new Spotlight(0.2f,true),	// LOC, MEET, ORG, PERS, PROD
//			new Spotlight(0.3f,true),	// LOC, MEET, ORG, PERS, PROD
				
//			new Stanford(StanfordModelName.CONLL_MODEL, loadOnDemand, false, false),
//			new Stanford(StanfordModelName.CONLL_MODEL, loadOnDemand, false, true),
//			new Stanford(StanfordModelName.CONLL_MODEL, loadOnDemand, true,  false),
//			new Stanford(StanfordModelName.CONLL_MODEL, loadOnDemand, true,  true),
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, loadOnDemand, false, false),	// LOC, ORG, PERS
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, loadOnDemand, false, true),
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, loadOnDemand, true,  false),
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, loadOnDemand, true,  true),
//			new Stanford(StanfordModelName.MUC_MODEL, loadOnDemand, false, false),		// DATE, LOC, ORG, PERS
//			new Stanford(StanfordModelName.MUC_MODEL, loadOnDemand, false, true),
//			new Stanford(StanfordModelName.MUC_MODEL, loadOnDemand, true,  false),
//			new Stanford(StanfordModelName.MUC_MODEL, loadOnDemand, true,  true),
//			new Stanford(StanfordModelName.NERWIP_MODEL, loadOnDemand, false, false),	// 
//			new Stanford(StanfordModelName.NERWIP_MODEL, loadOnDemand, false, true),	// 
//			new Stanford(StanfordModelName.NERWIP_MODEL, loadOnDemand, true,  false),	// 
//			new Stanford(StanfordModelName.NERWIP_MODEL, loadOnDemand, true,  true),	// LOC, ORG, PERS
			
//			new Subee(false,false,false,false,false),
//			new Subee(false,false,false,false,true),
//			new Subee(false,false,false,true,false),
//			new Subee(false,false,false,true,true),
//			new Subee(false,false,true,false,false),
//			new Subee(false,false,true,false,true),
//			new Subee(false,false,true,true,false),
//			new Subee(false,false,true,true,true),
//			new Subee(false,true,false,false,false),
//			new Subee(false,true,false,false,true),
//			new Subee(false,true,false,true,false),
//			new Subee(false,true,false,true,true),
//			new Subee(false,true,true,false,false),
//			new Subee(false,true,true,false,true),
//			new Subee(false,true,true,true,false),
//			new Subee(false,true,true,true,true),
//			new Subee(true,false,false,false,false),
//			new Subee(true,false,false,false,true),
//			new Subee(true,false,false,true,false),
//			new Subee(true,false,false,true,true),
//			new Subee(true,false,true,false,false),
//			new Subee(true,false,true,false,true),
//			new Subee(true,false,true,true,false),
//			new Subee(true,false,true,true,true),
//			new Subee(true,true,false,false,false),
//			new Subee(true,true,false,false,true),
//			new Subee(true,true,false,true,false),
//			new Subee(true,true,false,true,true),
//			new Subee(true,true,true,false,false),
//			new Subee(true,true,true,false,true),
//			new Subee(true,true,true,true,false),
//			new Subee(true,true,true,true,true),
			
//			new TagEn(TagEnModelName.MUC_MODEL, false, false),
//			new TagEn(TagEnModelName.MUC_MODEL, false, true),
//			new TagEn(TagEnModelName.MUC_MODEL, true, false),
//			new TagEn(TagEnModelName.MUC_MODEL, true, true),
//			new TagEn(TagEnModelName.MEDICFR_MODEL, false, false),
//			new TagEn(TagEnModelName.MEDICFR_MODEL, false, true),
//			new TagEn(TagEnModelName.MEDICFR_MODEL, true, false),
//			new TagEn(TagEnModelName.MEDICFR_MODEL, true, true),
//			new TagEn(TagEnModelName.WIKI_MODEL, false, false),
//			new TagEn(TagEnModelName.WIKI_MODEL, false, true),
//			new TagEn(TagEnModelName.WIKI_MODEL, true, false),
//			new TagEn(TagEnModelName.WIKI_MODEL, true, true),
//			new TagEn(TagEnModelName.MEDICEN_MODEL, false, false),
//			new TagEn(TagEnModelName.MEDICEN_MODEL, false, true),
//			new TagEn(TagEnModelName.MEDICEN_MODEL, true, false),
//			new TagEn(TagEnModelName.MEDICEN_MODEL, true, true),
			
////////////////////			
			
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, false, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, false, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, false, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, false, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, false, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, false, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, true, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, true, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, true, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, true, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, true, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, true, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.ALL),
			
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, false, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, false, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, false, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, false, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, false, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, false, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, true, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, true, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, true, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, true, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, true, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, true, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.ALL),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.NONE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.SINGLE),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.ALL),
			
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_UNIFORM, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_UNIFORM, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_UNIFORM, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_SINGLE, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_SINGLE, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_UNIFORM, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_UNIFORM, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_UNIFORM, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_SINGLE, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_SINGLE, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL),

//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_UNIFORM, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_UNIFORM, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_UNIFORM, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_SINGLE, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_SINGLE, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_UNIFORM, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_UNIFORM, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_UNIFORM, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_WEIGHTED_OVERALL, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_WEIGHTED_CATEGORY, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL),
			
//			new FullCombiner(CombinerName.SVM),
//			new FullCombiner(CombinerName.VOTE),
			
//			new StraightCombiner()
		};
		List<InterfaceRecognizer> recognizers = Arrays.asList(temp);
		logger.log("Processed recognizers: ");
		logger.increaseOffset();
		for(InterfaceRecognizer recognizer: recognizers)
			logger.log(recognizer.getRecognizerFolder());
		logger.decreaseOffset();
		
		// cache/no cache at the recognizer level
		for(InterfaceRecognizer recognizer: recognizers)
		{	recognizer.setCacheEnabled(true);	//TODO
//			((AbstractCombiner)recognizer).setSubCacheEnabled(true);	//just to check combiner subcache
		}
		
		// set articles
//		List<File> folders = Arrays.asList(
//			new File(FileNames.FO_OUTPUT + File.separator + "Aart_Kemink"),
//			new File(FileNames.FO_OUTPUT + File.separator + "Abraham_Adan"),
//			new File(FileNames.FO_OUTPUT + File.separator + "Adolf_hitler")
//		);
//		ArticleList folders = ArticleLists.getArticleList("training.set.txt");
		ArticleList folders = ArticleLists.getArticleList("testing.set.txt");
//		ArticleList folders = ArticleLists.getArticleList();
//		ArticleList folders = new ArticleList("test", Arrays.asList(new File(FileNames.FO_OUTPUT).listFiles(FileTools.FILTER_DIRECTORY)));
		logger.log("Processed articles: ");
		logger.increaseOffset();
		for(File folder: folders)
			logger.log(folder.getName());
		logger.decreaseOffset();
		
		// set evaluation measure
//		AbstractMeasure evaluation = new RecognitionMucMeasure(null);
		AbstractRecognitionMeasure evaluation = new RecognitionLilleMeasure(null);
//		AbstractMeasure evaluation = new RecognitionIstanbulMeasure(null);
		logger.log("Using assmessment measure "+evaluation.getClass().getName());

		// launch evaluation
		logger.log("Evaluation started");
		RecognitionEvaluator recognitionEvaluator = new RecognitionEvaluator(types, recognizers, folders, evaluation);
		recognitionEvaluator.setCacheEnabled(false);
		recognitionEvaluator.process();
//		logger.log(((Subee)recognizers.get(0)).UNUSED_TYPES);
		
		logger.log("Evaluation finished");
		logger.decreaseOffset();
	}

	/////////////////////////////////////////////////////////////////
	// EDITOR		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests the editor allowing to display NER results. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testEditor() throws Exception
	{	logger.setName("Test-Viewer");
		logger.log("Start viewer test ");
		logger.increaseOffset();
		
		// set up viewer
		logger.log("Set up viewer");
		MentionEditor viewer = new MentionEditor();
		
		// set up article
//		String articleName = "Aart_Kemink";
		String articleName = "Alain_Poher";
//		String articleName = "Seamus_Brennan";
//		String articleName = "John_Zorn";
//		String articleName = "Fleur_Pellerin";
		String articlePath = FileNames.FO_OUTPUT + File.separator + articleName;
		logger.log("Set up article: "+articlePath);
		
		logger.log("Launch viewer");
		viewer.setArticle(articlePath);
		logger.decreaseOffset();
	}
	
	/**
	 * Tests some methods related to
	 * String managment. 
	 */
	private static void testStringTools()
	{	String rawText = "Abcd efgh ijkl mnopq.";
		String linkedText = "Abcd <a href=\"zzzzzz\">efgh ijkl</a> mnopq.";
		String temp;
		int pos,res,length;
		System.out.println(rawText);
		System.out.println(linkedText);
		
		// positions
		pos = 0;
		res = LinkTools.getLinkedTextPosition(linkedText,pos);
		System.out.println("position "+pos+"("+rawText.charAt(pos)+"): "+res+"("+linkedText.charAt(res)+")");

		pos = 4;
		res = LinkTools.getLinkedTextPosition(linkedText,pos);
		System.out.println("position "+pos+"("+rawText.charAt(pos)+"): "+res+"("+linkedText.charAt(res)+")");

		pos = 5;
		res = LinkTools.getLinkedTextPosition(linkedText,pos);
		System.out.println("position "+pos+"("+rawText.charAt(pos)+"): "+res+"("+linkedText.charAt(res)+")");

		pos = 6;
		res = LinkTools.getLinkedTextPosition(linkedText,pos);
		System.out.println("position "+pos+"("+rawText.charAt(pos)+"): "+res+"("+linkedText.charAt(res)+")");

		pos = 13;
		res = LinkTools.getLinkedTextPosition(linkedText,pos);
		System.out.println("position "+pos+"("+rawText.charAt(pos)+"): "+res+"("+linkedText.charAt(res)+")");

		pos = 14;
		res = LinkTools.getLinkedTextPosition(linkedText,pos);
		System.out.println("position "+pos+"("+rawText.charAt(pos)+"): "+res+"("+linkedText.charAt(res)+")");

		pos = 16;
		res = LinkTools.getLinkedTextPosition(linkedText,pos);
		System.out.println("position "+pos+"("+rawText.charAt(pos)+"): "+res+"("+linkedText.charAt(res)+")");

		pos = 20;
		res = LinkTools.getLinkedTextPosition(linkedText,pos);
		System.out.println("position "+pos+"("+rawText.charAt(pos)+"): "+res+"("+linkedText.charAt(res)+")");
		
		// deletions
		pos = 1; length = 3;
		temp = LinkTools.removeFromLinkedText(linkedText, pos, length);
		System.out.println("remove '"+rawText.substring(pos,pos+length)+"' "+pos+","+length+": \t'"+temp+"'");
		temp = LinkTools.removeEmptyLinks(temp);
		System.out.println("cleaned: '"+temp+"'");
		
		pos = 7; length = 3;
		temp = LinkTools.removeFromLinkedText(linkedText, pos, length);
		System.out.println("remove '"+rawText.substring(pos,pos+length)+"' "+pos+","+length+": \t'"+temp+"'");
		temp = LinkTools.removeEmptyLinks(temp);
		System.out.println("cleaned: '"+temp+"'");
		
		pos = 16; length = 3;
		temp = LinkTools.removeFromLinkedText(linkedText, pos, length);
		System.out.println("remove '"+rawText.substring(pos,pos+length)+"' "+pos+","+length+": \t'"+temp+"'");
		temp = LinkTools.removeEmptyLinks(temp);
		System.out.println("cleaned: '"+temp+"'");

		pos = 0; length = 21;
		temp = LinkTools.removeFromLinkedText(linkedText, pos, length);
		System.out.println("remove '"+rawText.substring(pos,pos+length)+"' "+pos+","+length+": \t'"+temp+"'");
		temp = LinkTools.removeEmptyLinks(temp);
		System.out.println("cleaned: '"+temp+"'");

		pos = 3; length = 4;
		temp = LinkTools.removeFromLinkedText(linkedText, pos, length);
		System.out.println("remove '"+rawText.substring(pos,pos+length)+"' "+pos+","+length+": \t'"+temp+"'");
		temp = LinkTools.removeEmptyLinks(temp);
		System.out.println("cleaned: '"+temp+"'");

		pos = 12; length = 4;
		temp = LinkTools.removeFromLinkedText(linkedText, pos, length);
		System.out.println("remove '"+rawText.substring(pos,pos+length)+"' "+pos+","+length+": \t'"+temp+"'");
		temp = LinkTools.removeEmptyLinks(temp);
		System.out.println("cleaned: '"+temp+"'");

		pos = 3; length = 13;
		temp = LinkTools.removeFromLinkedText(linkedText, pos, length);
		System.out.println("remove '"+rawText.substring(pos,pos+length)+"' "+pos+","+length+": \t'"+temp+"'");
		temp = LinkTools.removeEmptyLinks(temp);
		System.out.println("cleaned: '"+temp+"'");
	}


	/////////////////////////////////////////////////////////////////
	// OTHER STUFF	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Testing the Nero named entity recognition tool.
	 * 
	 * @throws Exception
	 * 		Some problem occurred...
	 */
	private static void testNeroRaw() throws Exception
	{	String neroKey = KeyHandler.KEYS.get("Nero");
		String neroId = KeyHandler.IDS.get("Nero");
	
		byte[] encodedBytes = Base64.encodeBase64((neroId+":"+neroKey).getBytes());
		String encoding = new String(encodedBytes);

		// première requête
		String url = "https://nero.irisa.fr/texts.xml";
		HttpPost post = new HttpPost(url);
		post.setHeader("Authorization", "Basic " + encoding);
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("text[content]", "Je vais à Marseille cet été voir l'Olympique de Marseille."));
		post.setEntity(new UrlEncodedFormEntity(urlParameters));
		
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(post);
		int responseCode = response.getStatusLine().getStatusCode();
		System.out.println("Response Code : " + responseCode);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
		StringBuffer res = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null)
		{	System.out.println(line);
			res.append(line);
		}
		
		SAXBuilder sb = new SAXBuilder();
		Document doc = sb.build(new StringReader(res.toString()));
		Element root = doc.getRootElement();
		Element idElt = root.getChild("id");
		String id = idElt.getValue();
		
		// seconde requête
		int i = 1;
		do
		{	System.out.println("\nRepetition "+i);
			Thread.sleep(5000);
			url = "https://nero.irisa.fr/texts/"+id+".xml";
			System.out.println("url="+url);
			HttpGet get = new HttpGet(url);
			get.setHeader("Authorization", "Basic " + encoding);

			client = new DefaultHttpClient();
			response = client.execute(get);
			responseCode = response.getStatusLine().getStatusCode();
			System.out.println("Response Code : " + responseCode);
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
			res = new StringBuffer();
			while((line = rd.readLine()) != null)
			{	System.out.println(line);
				res.append(line);
			}
			i++;
		}
		while(responseCode!=200);
		
		sb = new SAXBuilder();
		doc = sb.build(new StringReader(res.toString()));
		root = doc.getRootElement();
		Element resultElt = root.getChild("result");
		String result = resultElt.getValue();
		System.out.println("\nResult="+result);
	}
	
	/**
	 * Test the installation of TreeTagger.
	 * 
	 * @exception Exception
	 * 		Some problem occurred...
	 */
	private static void testTreeTagger() throws Exception
	{	
//		Process p = Runtime.getRuntime().exec("cmd /c echo %PATH%");
//		Process p = Runtime.getRuntime().exec("cmd /c cd");
		Process p = Runtime.getRuntime().exec("cmd /c dir res\\ner\\treetagger");
//		Process p = Runtime.getRuntime().exec("perl -h");
		int res = p.waitFor();
		System.out.println("return code="+res);
	 
	    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    StringBuffer sb = new StringBuffer();
	    String line = "";			
	    while ((line = reader.readLine())!= null)
	    	sb.append(line + "\n");
	    System.out.println(sb);
	}

	/**
	 * Test the installation of TagEn.
	 * 
	 * @exception Exception
	 * 		Some problem occurred...
	 */
	private static void testTagEnRaw() throws Exception
	{	String[] commands = 
		{	"/bin/sh", "-c", 
//			"ls -l res/ner/tagen"
//			"./res/ner/tagen/tagen --help"
			"./res/ner/tagen/tagen :mucfr -aVy ./res/ner/tagen/input.txt"
		};
		Process p = Runtime.getRuntime().exec(commands);
		
//		Process p = Runtime.getRuntime().exec("/bin/sh -c ls -l res/ner/tagen");
		int res = p.waitFor();
		System.out.println("return code="+res);
	 
		// error output
		{	BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	    	StringBuffer sb = new StringBuffer();
	    	String line = "";			
		    while((line=reader.readLine()) != null)
		    	sb.append(line + "\n");
		    System.out.println(sb);
		}
		
		// standard output
	    {	BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    	StringBuffer sb = new StringBuffer();
	    	String line = "";			
	    	while ((line = reader.readLine())!= null)
	    		sb.append(line + "\n");
	    	System.out.println(sb);
	    }
	}
}


/* TODO
 * - Note: a resolver may add new mentions, e.g. to represent pronouns
 * 
 * - See if OpenNer can be adapted to process links? 
 *   And all the other tools, too (OpenCalais is a candidate).
 *   
 * - the unification between entities over the whole corpus is performed out of the processors, as an additional thing.
 *    also, its entities are recorded in specific files, at the level of the corpus.
 *    whereas the new mentions (bc of their entities) are recorded in a different file 
 *    	in the concerned linker folders of each article (?)
 *    	>> why?
 *   
 * - When linking, set the surface forms returned by the linking tool as the official name of the entity
 *   (keep the previous one as a possible surface form. it should already be in the list, anyway)
 *  
 * - Check for French models in already working recognizers
 * 
 * - Freebase has been discontinued: remove all Freebase-related classes, possibly replace them
 *   using Wikimedia Foundation products.
 *   
 * - Once the entities have correctly been identified, we may want to switch to a more DB-oriented representation,
 *   in which we retrive a bunch of info describing each entity. This would make it easier comparing entities like
 *   places, when infering event similarity. 
 */

/*
 * Check libraries (probably added by Sabrine?)
 * - why commons-csv-1.0.jar ?
 * - what is filterbuilder.jar ?
 * - htmlexer.jar ?
 * - sitecapturer ?
 * - thumbelina ?
 */
