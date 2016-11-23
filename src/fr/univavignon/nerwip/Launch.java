package fr.univavignon.nerwip;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleList;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.edition.MentionEditor;
import fr.univavignon.nerwip.evaluation.Evaluator;
import fr.univavignon.nerwip.evaluation.measure.AbstractMeasure;
import fr.univavignon.nerwip.evaluation.measure.LilleMeasure;
import fr.univavignon.nerwip.recognition.AbstractProcessor;
import fr.univavignon.nerwip.recognition.combiner.AbstractCombiner.SubeeMode;
import fr.univavignon.nerwip.recognition.combiner.svmbased.SvmCombiner;
import fr.univavignon.nerwip.recognition.combiner.svmbased.SvmTrainer;
import fr.univavignon.nerwip.recognition.combiner.svmbased.SvmCombiner.CombineMode;
import fr.univavignon.nerwip.recognition.combiner.votebased.VoteCombiner;
import fr.univavignon.nerwip.recognition.combiner.votebased.VoteTrainer;
import fr.univavignon.nerwip.recognition.combiner.votebased.VoteCombiner.VoteMode;
import fr.univavignon.nerwip.recognition.internal.modelbased.illinois.Illinois;
import fr.univavignon.nerwip.recognition.internal.modelbased.illinois.IllinoisModelName;
import fr.univavignon.nerwip.recognition.internal.modelbased.illinois.IllinoisTrainer;
import fr.univavignon.nerwip.recognition.internal.modelbased.lingpipe.LingPipe;
import fr.univavignon.nerwip.recognition.internal.modelbased.lingpipe.LingPipeModelName;
import fr.univavignon.nerwip.recognition.internal.modelbased.lingpipe.LingPipeTrainer;
import fr.univavignon.nerwip.recognition.internal.modelbased.opennlp.OpenNlp;
import fr.univavignon.nerwip.recognition.internal.modelbased.opennlp.OpenNlpModelName;
import fr.univavignon.nerwip.recognition.internal.modelbased.opennlp.OpenNlpTrainer;
import fr.univavignon.nerwip.recognition.internal.modelbased.stanford.Stanford;
import fr.univavignon.nerwip.recognition.internal.modelbased.stanford.StanfordModelName;
import fr.univavignon.nerwip.recognition.internal.modelbased.stanford.StanfordTrainer;
import fr.univavignon.nerwip.recognition.internal.modelless.dateextractor.DateExtractor;
import fr.univavignon.nerwip.recognition.internal.modelless.opencalais.OpenCalais;
import fr.univavignon.nerwip.recognition.internal.modelless.opencalais.OpenCalaisLanguage;
import fr.univavignon.nerwip.recognition.internal.modelless.subee.Subee;
import fr.univavignon.nerwip.recognition.internal.modelless.wikipediadater.WikipediaDater;
import fr.univavignon.nerwip.retrieval.ArticleRetriever;
import fr.univavignon.nerwip.tools.corpus.ArticleLists;
import fr.univavignon.nerwip.tools.corpus.ArticleRetrieval;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This is the main class to launch the main
 * processes implemented in Nerwip. Edit it
 * to perform a specific treatment.
 * 
 * @author Vincent Labatut
 */
public class Launch
{	/**
	 * Basic main function, launches the
	 * required test. Designed to be modified
	 * and launched from Eclipse, no command-line
	 * options.
	 * <br/>
	 * See the comments located inside the method source 
	 * code for details.
	 * 
	 * @param args
	 * 		None needed.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	public static void main(String[] args) throws Exception
	{	///////////////////////////////////////////////
		// Set up logger
		///////////////////////////////////////////////
		//logger.setEnabled(false);	// uncomment to disable logging
		logger.setName("Test");
		
		
		///////////////////////////////////////////////
		// Notes
		///////////////////////////////////////////////
		// The Nerwip Corpus (available on FigShare, see the Installation
		// section of the README file) is split in two parts: training and
		// testing. This split is described by two lists of articles located
		// in the corpus root folder. Certain recognizers can be trained,
		// which can be performed using the training set. All tools are
		// evaluated on the testing set, whether they were trained or not.
		
		// If you didn't get the full Nerwip corpus (yet), the first
		// half of the articles in folder out are used for training,
		// and the other half for evaluation. Of course, it is not
		// enough to perform a sufficient training.
		
		// Also, note you need to get the recognizers related data from
		// FigShare (again, as explained in the README file), before
		// being able to apply most of the recognizers integrated in
		// Nerwip.
		
		
		///////////////////////////////////////////////
		// How to train standalone recognizers
		///////////////////////////////////////////////
		// Only some of the standalone tools can be trained
		// See the methods for tool-specific details 
		// Note : for certain recognizers, this step can be very long.
		// You can skip this step if you are not interested in training.
		trainLingPipe();
		trainIllinois();
		trainOpenNlp();
		trainStanford();
		
		///////////////////////////////////////////////
		// How to apply individually standalone recognizers to a single article
		///////////////////////////////////////////////
		// See the invoked methods to see the set up of each recognizers
		// Here, each tool is applied to the first article in the corpus
		// As mentioned in the readme file, it is possible to implement
		// your own tools, or wrappers for other existing recognizers.
		applyDateExtractor();		// date-only recognizer
		applyWikipediaDater();		// another (better) date-only recognizer
		applyIllinois();			// Illinois Named Entity Tagger
		applyLingPipe();			// alias-i LingPipe
		applyOpenCalais();			// Thomson-Reuters OpenCalais
		applyOpenNlp();				// Apache OpenNLP
		applyStanford();			// Stanford Named Entity Recognizer
		applySubee();				// Subee (Freebase-based custom tool)
		
		///////////////////////////////////////////////
		// How to evaluate the standalone tools performances
		///////////////////////////////////////////////
		// The evaluator included in Nerwip allows both applying
		// one or several tools on a set of articles, and processing
		// the performances obtained for the concerned tools.
		// Several groups of performance measures are available,
		// including the traditional MUC ones (as well as custom ones).
		// It is possible to add your own measures (cf. the readme file).
		evaluateStandaloneTools();
		
		
		
		
		///////////////////////////////////////////////
		// How to train and apply NER combiners
		///////////////////////////////////////////////
		// The NER combiners need the results coming from certain standalone tools
		// For this reason, they automatically apply them when needed.
		// During training, they might also need to process some performances,
		// calculated on the training set. This is to explain the possible
		// creation of the corresponding result files.
		trainVoteCombiner();
		trainSvmCombiner();
		
		///////////////////////////////////////////////
		// How to apply individually NER combiners to a single article
		///////////////////////////////////////////////
		// Applying acombiner is exactly like applying a standalone tool (a combiner
		// is actually just a specific type of recognizer).
		// Like before, each (trained) tool is applied to the first article in the corpus
		// And again, we initialize the tools with the same parameters than for training
		applyVoteCombiner();
		applySvmCombiner();
		
		///////////////////////////////////////////////
		// How to evaluate the combiners performances
		///////////////////////////////////////////////
		// Like before, we perform the evaluation only on the training set
		evaluateCombiners();
		
		
		
		
		///////////////////////////////////////////////
		// How to retrieve new articles
		///////////////////////////////////////////////
		// all the retrieved articles go to their individual folder
		// located in the "out" folder. All the associated files
		// (manual annotation, automatic annotation, performance...)
		// go to the same folder.
		URL url = new URL("http://en.wikipedia.org/wiki/Jean-Jacques_Annaud");
		retrieveArticle(url);
		// one can define a list and retrieve all the corresponding articles
		// this list can contain either Wikipedia-normalized names such as
		// "Jean-Jacques_Annaud", or directly URLs.
		ArticleRetrieval.retrieveArticles("mylist.txt");
		
		///////////////////////////////////////////////
		// How to annotate newly retrieved articles
		///////////////////////////////////////////////
		// If you do not annotate the articles, they
		// cannot be used for evaluation, since there
		// is no ground truth. Use the dedicated editor 
		// to perform the manual annotation.
		launchEditor();
		// The editor also allows visualizing the result
		// of the recognizers (the mentions they detected).
		
		
		
		logger.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// TRAINING STANDALONE TOOLS 	/////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the file containing the list of articles used for training */ 
	private final static String TRAINING_FILE_LIST = "training.set.txt";
	
	/**
	 * Returns a list of articles constituting the training
	 * set.
	 * 
	 * @return
	 * 		List of articles as an {@code ArticleList} object.
	 */
	private static ArticleList getTrainingSet()
	{	ArticleList result;
		try
		{	result = ArticleLists.getArticleList(TRAINING_FILE_LIST);
		}
		catch(FileNotFoundException e)
		{	result = ArticleLists.getArticleHalfList(true);
		}
		
		logger.log("Processed articles: ");
		logger.increaseOffset();
		for(File folder: result)
			logger.log(folder.getName());
		logger.decreaseOffset();
		
		return result;
	}	
	
	/**
	 * Trains Illinois on the training set. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void trainIllinois() throws Exception
	{	logger.log("Start training Illinois");
		logger.increaseOffset();
		
		// get the training set
		ArticleList folders = getTrainingSet();
			
		// NERWIP_MODEL is a new model, trained on our Nerwip corpus
		// it is recorded in the res/ner/illinois/models folder
		IllinoisTrainer trainer = new IllinoisTrainer(IllinoisModelName.NERWIP_MODEL);
		trainer.setCacheEnabled(false);
		trainer.process(folders);
		
		logger.decreaseOffset();
	}

	/**
	 * Trains LingPipe on the training set. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void trainLingPipe() throws Exception
	{	logger.log("Start training LingPipe");
		logger.increaseOffset();

		// get the training set
		ArticleList folders = getTrainingSet();

		// NERWIP_MODEL is a new model, trained on our Nerwip corpus
		// it is recorded in the res/ner/lingpipe folder
		LingPipeTrainer trainer = new LingPipeTrainer(LingPipeModelName.NERWIP_MODEL);
		trainer.setCacheEnabled(false);
		trainer.process(folders);
		
		logger.decreaseOffset();
	}

	/**
	 * Trains Apache OpenNLP on the training set. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void trainOpenNlp() throws Exception
	{	logger.log("Start training OpenNlp");
		logger.increaseOffset();
		
		// get the training set
		ArticleList folders = getTrainingSet();

		// NERWIP_MODEL is a new model, trained on our Nerwip corpus
		// it is recorded in the res/ner/opennlp folder
		OpenNlpTrainer trainer = new OpenNlpTrainer(OpenNlpModelName.NERWIP_MODEL);
		trainer.setCacheEnabled(false);
		trainer.process(folders);
		
		logger.decreaseOffset();
	}

	/**
	 * Trains Stanford Named Entity Recognizer on the training set. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void trainStanford() throws Exception
	{	logger.setName("Test-Stanford");
		logger.log("Start testing Stanford");
		logger.increaseOffset();
	
		// get the training set
		ArticleList folders = getTrainingSet();

		// NERWIP_MODEL is a new model, trained on our Nerwip corpus
		// it is recorded in the res/ner/stanford/models folder
		StanfordTrainer trainer = new StanfordTrainer(StanfordModelName.NERWIP_MODEL);
		trainer.setCacheEnabled(false);
		trainer.process(folders);
		
		logger.decreaseOffset();
	}

	/////////////////////////////////////////////////////////////////
	// APPLYING STANDALONE TOOLS 		/////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the first article in the corpus.
	 * 
	 * @return
	 * 		An {@code Article} object.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static Article getFirstArticle() throws Exception
	{	ArticleList folders = ArticleLists.getArticleList();
		File folder = folders.get(0);
		String name = folder.getName();
		
		// Here we ask the retriever to get an article
		// which is actually arealdy cached (i.e. in the corpus)
		// A bit convulated, I admit.
		ArticleRetriever retriever = new ArticleRetriever();
		Article result = retriever.process(name);
		
		return result;
	}
	
	/**
	 * Applies our (not very good) Date Extractor recognizer
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applyDateExtractor() throws Exception
	{	logger.log("Start testing DateExtractor");
		logger.increaseOffset();
		
		// get the first article in the corpus
		Article article = getFirstArticle();
		
		DateExtractor dateExtractor = new DateExtractor();
		dateExtractor.setCacheEnabled(false);
		dateExtractor.recognize(article);

		logger.decreaseOffset();
	}

	/**
	 * Applies our Wikipedia Dater recognizer
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applyWikipediaDater() throws Exception
	{	logger.log("Start testing WikipediaDater");
		logger.increaseOffset();
		
		// get the first article in the corpus
		Article article = getFirstArticle();
		
		WikipediaDater wikipediaDater = new WikipediaDater();
		wikipediaDater.setCacheEnabled(false);
		wikipediaDater.recognize(article);

		logger.decreaseOffset();
	}

	/**
	 * Applies the Illinois recognizer
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applyIllinois() throws Exception
	{	logger.log("Start testing Illinois");
		logger.increaseOffset();
		
		// get the first article in the corpus
		Article article = getFirstArticle();
		
		IllinoisModelName modelName = IllinoisModelName.CONLL_MODEL;
		boolean trim = true;
		boolean ignorePronouns = false;
		boolean exclusionOn = false;
		Illinois illinois = new Illinois(modelName, true, trim, ignorePronouns, exclusionOn);
		illinois.setCacheEnabled(false);
		illinois.recognize(article);
		
		logger.decreaseOffset();
	}

	/**
	 * Applies the LingPipe recognizer
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applyLingPipe() throws Exception
	{	logger.log("Start testing LingPipe");
		logger.increaseOffset();

		// get the first article in the corpus
		Article article = getFirstArticle();
		
		LingPipeModelName chunkingMethod = LingPipeModelName.APPROX_DICTIONARY;
		boolean splitSentences = true;
		boolean trim = true;
		boolean ignorePronouns = false;
		boolean exclusionOn = false;
		LingPipe lingPipe = new LingPipe(chunkingMethod, true, splitSentences, trim, ignorePronouns, exclusionOn);
		lingPipe.setCacheEnabled(false);
		lingPipe.recognize(article);
		
		logger.decreaseOffset();
	}

	/**
	 * Applies the OpenCalais recognizer
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applyOpenCalais() throws Exception
	{	logger.log("Start testing OpenCalais");
		logger.increaseOffset();
	
		// get the first article in the corpus
		Article article = getFirstArticle();
		
		OpenCalaisLanguage lang = OpenCalaisLanguage.EN;
		boolean ignorePronouns = false;
		boolean exclusionOn = false;
		OpenCalais openCalais = new OpenCalais(lang, ignorePronouns, exclusionOn);
		openCalais.setCacheEnabled(false);
		openCalais.recognize(article);

		logger.decreaseOffset();
	}

	/**
	 * Applies the OpenNLP recognizer
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applyOpenNlp() throws Exception
	{	logger.log("Start testing OpenNlp");
		logger.increaseOffset();
		
		// get the first article in the corpus
		Article article = getFirstArticle();
		
		OpenNlpModelName modelName = OpenNlpModelName.ORIGINAL_MODEL;
		boolean ignorePronouns = false;
		boolean exclusionOn = false;
		OpenNlp openNlp = new OpenNlp(modelName, true, ignorePronouns, exclusionOn);
		openNlp.setCacheEnabled(false);
		openNlp.recognize(article);
		
		logger.decreaseOffset();
	}

	/**
	 * Applies the OpenNLP recognizer
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applyStanford() throws Exception
	{	logger.log("Start testing Stanford");
		logger.increaseOffset();
	
		// get the first article in the corpus
		Article article = getFirstArticle();
	
		StanfordModelName modelName = StanfordModelName.CONLLMUC_MODEL;
		boolean ignorePronouns = false;
		boolean exclusionOn = false;
		Stanford stanford = new Stanford(modelName, true, ignorePronouns, exclusionOn);
		stanford.setCacheEnabled(false);
		stanford.recognize(article);
			
		logger.decreaseOffset();
	}

	/**
	 * Applies our Subee recognizer
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applySubee() throws Exception
	{	logger.log("Start testing Subee");
		logger.increaseOffset();
	
		// get the first article in the corpus
		Article article = getFirstArticle();

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

	/////////////////////////////////////////////////////////////////
	// EVALUATING STANDALONE TOOLS	/////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the file containing the list of articles used for testing */
	private final static String TESTING_FILE_LIST = "testing.set.txt";
	
	/**
	 * Returns a list of articles constituting the testing
	 * set.
	 * 
	 * @return
	 * 		List of articles as an {@code ArticleList} object.
	 */
	private static ArticleList getTestingSet()
	{	ArticleList result;
		try
		{	result = ArticleLists.getArticleList(TESTING_FILE_LIST);
		}
		catch(FileNotFoundException e)
		{	result = ArticleLists.getArticleHalfList(false);
		}
		
		logger.log("Processed articles: ");
		logger.increaseOffset();
		for(File folder: result)
			logger.log(folder.getName());
		logger.decreaseOffset();
		
		return result;
	}	

	/**
	 * Evaluates the performances of a few standalone recognizers,
	 * on the testing set. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void evaluateStandaloneTools() throws Exception
	{	logger.log("Start standalone tools evaluation");
		logger.increaseOffset();
		
		// set the types we want to take into account during this evaluation
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
		
		// set the recognizers we want to evaluate (including their parameters)
		// note that by default, the mentions detected by a NER are cached.
		// this means if the result file already exists, it will be loaded.
		// here, we use the same parameters than for the single-article tests.
		AbstractProcessor temp[] =
		{	new DateExtractor(),
			new WikipediaDater(),
			new Illinois(IllinoisModelName.CONLL_MODEL, true, true, false, false),
			new LingPipe(LingPipeModelName.APPROX_DICTIONARY, true, true, true, false, false),
			new OpenCalais(OpenCalaisLanguage.EN, false, false),
			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL, true, false, false),
			new Stanford(StanfordModelName.CONLLMUC_MODEL, true, false, false),
			new Subee(true, true, true, true, true)
		};
		List<AbstractProcessor> recognizers = Arrays.asList(temp);
		logger.log("Processed recognizers: ");
		logger.increaseOffset();
		for(AbstractProcessor recognizer: recognizers)
			logger.log(recognizer.getFolder());
		logger.decreaseOffset();

		// get the testing set
		ArticleList folders = getTestingSet();
		logger.log("Processed articles: ");
		logger.increaseOffset();
		for(File folder: folders)
			logger.log(folder.getName());
		logger.decreaseOffset();
		
		// set the evaluation measure (several exists)
//		AbstractMeasure evaluation = new MucMeasure(null);
		AbstractMeasure evaluation = new LilleMeasure(null);
//		AbstractMeasure evaluation = new IstanbulMeasure(null);
		logger.log("Using assmessment measure "+evaluation.getClass().getName());
		
		// launch evaluation
		logger.log("Evaluation started");
		Evaluator evaluator = new Evaluator(types, recognizers, folders, evaluation);
		evaluator.setCacheEnabled(false); // force the re-processing of the performance measures
		evaluator.process();
		
		logger.log("Evaluation finished");
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// TRAINING COMBINING TOOLS 	/////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Trains our vote combiner on the training set.
	 * <br/>
	 * This tool is likely to apply some standalone tools
	 * first, and process their performance. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void trainVoteCombiner() throws Exception
	{	logger.log("Start training VoteCombiner");
		logger.increaseOffset();

		// get the training set
		ArticleList folders = getTrainingSet();
		
		boolean loadModelOnDemand = true;
		boolean specific = true;
		VoteMode voteMode = VoteMode.UNIFORM;
		boolean useRecall = true;
		boolean existVote = true;
		SubeeMode subeeMode = SubeeMode.NONE;
		VoteCombiner voteCombiner = new VoteCombiner(loadModelOnDemand, specific, voteMode, useRecall, existVote, subeeMode);
		VoteTrainer trainer = new VoteTrainer(voteCombiner);
		trainer.setSubCacheEnabled(true);
		trainer.setCacheEnabled(false);
		trainer.process(folders);
	
		logger.decreaseOffset();
	}
	
	/**
	 * Trains our SVM combiner on the training set.
	 * <br/>
	 * This tool is likely to apply some standalone tools
	 * first, and process their performance. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void trainSvmCombiner() throws Exception
	{	logger.log("Start training SvmCombiner");
		logger.increaseOffset();

		// get the training set
		ArticleList folders = getTrainingSet();
		
		boolean loadModelOnDemand = true;
		boolean specific = true;
		boolean useCategories = true;
		CombineMode combineMode = CombineMode.CHUNK_SINGLE;
		SubeeMode subeeMode = SubeeMode.NONE;
		SvmCombiner svmCombiner = new SvmCombiner(loadModelOnDemand, specific, useCategories, combineMode, subeeMode);
		SvmTrainer trainer = new SvmTrainer(svmCombiner);
		trainer.setSubCacheEnabled(true);
		trainer.setCacheEnabled(false);
		boolean useDefaultParams = false;
		trainer.process(folders,useDefaultParams);
	
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// APPLYING COMBINING TOOLS		/////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Applies the vote-based combiner
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applyVoteCombiner() throws Exception
	{	logger.log("Start testing VoteCombiner");
		logger.increaseOffset();
		
		// get the first article in the corpus
		Article article = getFirstArticle();

		boolean loadModelOnDemand = true;
		boolean specific = true;
		VoteMode voteMode = VoteMode.UNIFORM;
		boolean useRecall = true;
		boolean existVote = true;
		SubeeMode subeeMode = SubeeMode.NONE;
		VoteCombiner voteCombiner = new VoteCombiner(loadModelOnDemand, specific, voteMode, useRecall, existVote, subeeMode);
		voteCombiner.setCacheEnabled(false);
		voteCombiner.recognize(article);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Applies the SVM-based combiner
	 * to a single article (first in the corpus).
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void applySvmCombiner() throws Exception
	{	logger.log("Start testing VoteCombiner");
		logger.increaseOffset();
		
		// get the first article in the corpus
		Article article = getFirstArticle();

		boolean loadModelOnDemand = true;
		boolean specific = true;
		boolean useCategories = true;
		CombineMode combineMode = CombineMode.CHUNK_SINGLE;
		SubeeMode subeeMode = SubeeMode.NONE;
		SvmCombiner svmCombiner = new SvmCombiner(loadModelOnDemand, specific, useCategories, combineMode, subeeMode);
		svmCombiner.setCacheEnabled(false);
		svmCombiner.recognize(article);
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// EVALUATING COMBINING TOOLS	/////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Evaluates the performances of both combiners
	 * on the testing set. The initialisation parameters
	 * are the same than for training.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void evaluateCombiners() throws Exception
	{	logger.log("Start combiners evaluation");
		logger.increaseOffset();
		
		// set the types we want to take into account during this evaluation
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
		
		// set the recognizers we want to evaluate (like before in evaluateStandaloneTools)
		AbstractProcessor temp[] =
		{	new VoteCombiner(true, true, VoteMode.UNIFORM, true, true, SubeeMode.NONE),
			new SvmCombiner(true, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.NONE)
		};
		List<AbstractProcessor> recognizers = Arrays.asList(temp);
		logger.log("Processed recognizers: ");
		logger.increaseOffset();
		for(AbstractProcessor recognizer: recognizers)
			logger.log(recognizer.getFolder());
		logger.decreaseOffset();

		// get the testing set
		ArticleList folders = ArticleLists.getArticleList();
		logger.log("Processed articles: ");
		logger.increaseOffset();
		for(File folder: folders)
			logger.log(folder.getName());
		logger.decreaseOffset();
		
		// set the evaluation measure (several exists)
//		AbstractMeasure evaluation = new MucMeasure(null);
		AbstractMeasure evaluation = new LilleMeasure(null);
//		AbstractMeasure evaluation = new IstanbulMeasure(null);
		logger.log("Using assmessment measure "+evaluation.getClass().getName());
		
		// launch evaluation
		logger.log("Evaluation started");
		Evaluator evaluator = new Evaluator(types, recognizers, folders, evaluation);
		evaluator.setCacheEnabled(false); // force the re-processing of the performance measures
		evaluator.process();
		
		logger.log("Evaluation finished");
		logger.decreaseOffset();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////
	// RETRIEVAL	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** 
	 * Retrieves the Wikipedia article for the specified URL. 
	 * 
	 * @param url
	 * 		URL of the article to retrieve.
	 *  
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void retrieveArticle(URL url) throws Exception
	{	logger.log("Start retrieving article "+url);
		logger.increaseOffset();
		
		ArticleRetriever retriever = new ArticleRetriever();
		retriever.setCacheEnabled(false); // here we force retrieval
		retriever.process(url);
		
		logger.decreaseOffset();
	}

	/////////////////////////////////////////////////////////////////
	// ANNOTATION	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Launches the editor allowing to display NER results
	 * and perform annotations. 
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	private static void launchEditor() throws Exception
	{	logger.increaseOffset();
		
		// set up viewer
		logger.log("Set up viewer");
		MentionEditor viewer = new MentionEditor();
		
		// set up article
		ArticleList articles = ArticleLists.getArticleList();
		File article = articles.get(0); // get the first of the list
		String articleName = article.getName();
		
		String articlePath = FileNames.FO_OUTPUT + File.separator + articleName;
		logger.log("Set up article: "+articlePath);
		viewer.setArticle(articlePath);
		
		logger.log("Launch viewer");
		viewer.setArticle(articlePath);
		
		logger.decreaseOffset();
	}
}
