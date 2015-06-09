package tr.edu.gsu.nerwip;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages

 * Copyright 2011 Yasa Akbulut, Burcu Küpelioğlu & Vincent Labatut
 * Copyright 2012 Burcu Küpelioğlu, Samet Atdağ & Vincent Labatut
 * Copyright 2013 Samet Atdağ & Vincent Labatut
 * Copyright 2014-15 Vincent Labatut
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
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.edition.EntityEditor;
import tr.edu.gsu.nerwip.evaluation.ArticleList;
import tr.edu.gsu.nerwip.evaluation.Evaluator;
import tr.edu.gsu.nerwip.evaluation.measure.AbstractMeasure;
import tr.edu.gsu.nerwip.evaluation.measure.MucMeasure;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.combiner.AbstractCombiner.SubeeMode;
import tr.edu.gsu.nerwip.recognition.combiner.fullcombiner.FullCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.fullcombiner.FullCombiner.Combiner;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner.CombineMode;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmTrainer;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner.VoteMode;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteTrainer;
import tr.edu.gsu.nerwip.recognition.external.nero.Nero;
import tr.edu.gsu.nerwip.recognition.external.nero.Nero.Tagger;
import tr.edu.gsu.nerwip.recognition.external.tagen.TagEN;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.heideltime.HeidelTime;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.IllinoisModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.IllinoisTrainer;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe.LingPipeModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe.LingPipeTrainer;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp.OpenNlpModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp.OpenNlpTrainer;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.Stanford;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.StanfordModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.StanfordTrainer;
import tr.edu.gsu.nerwip.recognition.internal.modelless.dateextractor.DateExtractor;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais.OpenCalais;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opener.OpeNer;
import tr.edu.gsu.nerwip.recognition.internal.modelless.subee.Subee;
import tr.edu.gsu.nerwip.recognition.internal.modelless.wikipediadater.WikipediaDater;
import tr.edu.gsu.nerwip.retrieval.ArticleRetriever;
import tr.edu.gsu.nerwip.retrieval.reader.wikipedia.WikipediaReader;
import tr.edu.gsu.nerwip.tools.corpus.ArticleLists;
import tr.edu.gsu.nerwip.tools.dbpedia.DbIdTools;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.freebase.FbIdTools;
import tr.edu.gsu.nerwip.tools.freebase.FbTypeTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.mediawiki.WikiIdTools;
import tr.edu.gsu.nerwip.tools.mediawiki.WikiTypeTools;
import tr.edu.gsu.nerwip.tools.string.LinkTools;

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
		URL url = new URL("http://en.wikipedia.org/wiki/Aart_Kemink");
//		URL url = new URL("http://en.wikipedia.org/wiki/Ibrahim_Maalouf");
//		URL url = new URL("http://en.wikipedia.org/wiki/Catherine_Jacob_(journalist)");
		
		String name = "Émilien_Brigault";
//		String name = "Albert_Chauly";
//		String name = "Gilles_Marcel_Cachin";
//		String name = "Barack_Obama";
		
//		testArticleRetriever(url);
//		testArticlesRetriever();
//		testCategoryRetriever();
//		testFbidRetriever();
//		testTypeRetriever();
		
//		testDateExtractor(url);
//		testIllinois(url);
//		testLingPipe(url);
//		testOpenCalais(url);
//		testOpenCalais(name);
		
//	    testOpenNlp(url);
//		testStanford(url);
//		testSubee(url);
//		testWikipediaDater(url);
//		testNero(name);
//		testOpeNer(name);
//		testTagEN(name);
		
//		testVoteCombiner(url);
//		testSvmCombiner(url);
		
//		testEvaluator();
//		testEditor();
//		testWikiIdRetriever();
//		testWikiTypeRetriever();
		testDbIdRetriever();
		
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
	 * retrieve Freebase ids from Wikipedia titles.
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
	 * retrieve Freebase ids from Wikipedia titles.
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
	 * retrieve types from Freebase.
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
		List<String> types = WikiTypeTools.getAllTypes(title);
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

		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		OpenCalais openCalais = new OpenCalais(ignorePronouns, exclusionOn);
		openCalais.setCacheEnabled(false);
		openCalais.process(article);

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

		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		OpenCalais openCalais = new OpenCalais(ignorePronouns, exclusionOn);
		openCalais.setOutputRawResults(true);
		openCalais.setCacheEnabled(false);
		openCalais.process(article);

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

		Tagger tagger = Tagger.CRF;
		boolean flat = false;
		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		Nero nero = new Nero(tagger, flat, ignorePronouns, exclusionOn);
		nero.setOutputRawResults(true);
		nero.setCacheEnabled(false);
		
		// only the specified article
//		nero.process(article);

		// all the corpus
		testAllCorpus(nero,150);
		
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
	private static void testTagEN(String name) throws Exception
	{	logger.setName("Test-TagEN");
		logger.log("Start testing TagEN");
		logger.increaseOffset();
	
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);

		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		TagEN tagen = new TagEN(ignorePronouns, exclusionOn);
		tagen.setOutputRawResults(true);
		tagen.setCacheEnabled(false);
		tagen.process(article);

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
		
		boolean exclusionOn = false;
		boolean ignorePronouns = false;
		OpeNer opener = new OpeNer(ignorePronouns, exclusionOn);
		opener.setOutputRawResults(true);
		opener.setCacheEnabled(true);
		
		// only the specified article
//		opener.process(article);
		
		// all the corpus
		testAllCorpus(opener,0);
		
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
			stanford.process(article);
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
		dateExtractor.process(article);

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
		subee.process(article);

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
		wikipediaDater.process(article);

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
	private static void testOpenNlp(URL url) throws Exception
	{	logger.setName("Test-OpenNlp");
		logger.log("Start testing OpenNlp");
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

			OpenNlpTrainer trainer = new OpenNlpTrainer(OpenNlpModelName.NERWIP_MODEL);
			trainer.setCacheEnabled(false);
			trainer.process(folders);
		}
		
//		{	ArticleRetriever retriever = new ArticleRetriever();
//			Article article = retriever.process(url);
//	
//			OpenNlpModelName modelName = OpenNlpModelName.ORIGINAL_MODEL;
//			boolean exclusionOn = false;
//			boolean ignorePronouns = false;
//			OpenNlp openNlp = new OpenNlp(modelName, true, ignorePronouns, exclusionOn);
//			openNlp.setCacheEnabled(false);
//			openNlp.process(article);
//
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
			CombineMode.ENTITY_UNIFORM
//			CombineMode.ENTITY_WEIGHTED_OVERALL
//			CombineMode.ENTITY_WEIGHTED_CATEGORY
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
	 * Applies the specified NER tool to the 
	 * whole corpus.
	 * 
	 * @param recognizer
	 * 		NER tool to apply.
	 * @param start
	 * 		Which article to start from.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void testAllCorpus(AbstractRecognizer recognizer, int start) throws Exception
	{	logger.log("Process each article individually");
		logger.increaseOffset();
		
		ArticleList folders = ArticleLists.getArticleList();
		int i = 0;
		for(File folder: folders)
		{	if(i>=start)
			{	// get the results
				logger.log("Process article "+folder.getName());
				logger.increaseOffset();
				
					// get article
					logger.log("Retrieve the article");
					String name = folder.getName();
					ArticleRetriever retriever = new ArticleRetriever();
					Article article = retriever.process(name);
						
					logger.log("Apply the ner tool");
					recognizer.process(article);
					
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
	{	logger.setName("Test-Evaluator");
		logger.log("Start evaluation test ");
		logger.increaseOffset();
		
		// set types
		List<EntityType> types = Arrays.asList(
			EntityType.DATE,
			EntityType.LOCATION,
			EntityType.ORGANIZATION,
			EntityType.PERSON
		);
		logger.log("Processed types: ");
		logger.increaseOffset();
		for(EntityType type: types)
			logger.log("Type "+type);
		logger.decreaseOffset();
		
		// set NER tools
		boolean loadOnDemand = true;
		AbstractRecognizer temp[] =
		{	
//			new DateExtractor(),
//			new WikipediaDater(),
			
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
			
//			new OpenCalais(false, false),
//			new OpenCalais(false, true),
//			new OpenCalais(true,  false),	// (DATE), LOC, ORG, PERS	
//			new OpenCalais(true,  true),	
			
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, false,false),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, false,true),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, true, false),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, true, true),	// DATE, LOC, ORG, PERS
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, false,false),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, false,true),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, true, false),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, true, true),	// LOC, ORG, PERS

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
			
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.ENTITY_UNIFORM, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.ENTITY_UNIFORM, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.ENTITY_UNIFORM, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_SINGLE, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_SINGLE, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.ENTITY_UNIFORM, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.ENTITY_UNIFORM, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.ENTITY_UNIFORM, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_SINGLE, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_SINGLE, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL),

//			new SvmCombiner(loadOnDemand, true, false, CombineMode.ENTITY_UNIFORM, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.ENTITY_UNIFORM, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.ENTITY_UNIFORM, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_SINGLE, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_SINGLE, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.ENTITY_UNIFORM, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.ENTITY_UNIFORM, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.ENTITY_UNIFORM, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.ALL),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL),
				
			new FullCombiner(Combiner.SVM),
			new FullCombiner(Combiner.VOTE)
		};
		List<AbstractRecognizer> recognizers = Arrays.asList(temp);
		logger.log("Processed NER tools: ");
		logger.increaseOffset();
		for(AbstractRecognizer recognizer: recognizers)
			logger.log(recognizer.getFolder());
		logger.decreaseOffset();
		
		// cache/no cache at the recognizer level
		for(AbstractRecognizer recognizer: recognizers)
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
//		ArticleList folders = ArticleLists.getArticleList("testing.set.txt");
		ArticleList folders = ArticleLists.getArticleList();
//		ArticleList folders = new ArticleList("test", Arrays.asList(new File(FileNames.FO_OUTPUT).listFiles(FileTools.FILTER_DIRECTORY)));
		logger.log("Processed articles: ");
		logger.increaseOffset();
		for(File folder: folders)
			logger.log(folder.getName());
		logger.decreaseOffset();
		
		// set evaluation measure
		AbstractMeasure evaluation = new MucMeasure(null);
//		AbstractMeasure evaluation = new LilleMeasure(null);
//		AbstractMeasure evaluation = new IstanbulMeasure(null);
		logger.log("Using assmessment measure "+evaluation.getClass().getName());

		// launch evaluation
		logger.log("Evaluation started");
		Evaluator evaluator = new Evaluator(types, recognizers, folders, evaluation);
		evaluator.setCacheEnabled(false);
		evaluator.process();
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
		EntityEditor viewer = new EntityEditor();
		
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
}

