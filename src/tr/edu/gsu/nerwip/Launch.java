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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.edition.EntityEditor;
import tr.edu.gsu.nerwip.evaluation.ArticleList;
import tr.edu.gsu.nerwip.evaluation.Evaluator;
import tr.edu.gsu.nerwip.evaluation.measure.AbstractMeasure;
import tr.edu.gsu.nerwip.evaluation.measure.LilleMeasure;
import tr.edu.gsu.nerwip.evaluation.measure.IstanbulMeasure;
import tr.edu.gsu.nerwip.evaluation.measure.MucMeasure;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.combiner.AbstractCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.AbstractCombiner.SubeeMode;
import tr.edu.gsu.nerwip.recognition.combiner.fullcombiner.FullCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.fullcombiner.FullCombiner.Combiner;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner.CombineMode;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmTrainer;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner.VoteMode;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteTrainer;
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalConverter;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.Illinois;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.IllinoisModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.IllinoisTrainer;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe.LingPipe;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe.LingPipeModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe.LingPipeTrainer;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp.OpenNlp;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp.OpenNlpModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp.OpenNlpTrainer;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.Stanford;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.StanfordModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.StanfordTrainer;
import tr.edu.gsu.nerwip.recognition.internal.modelless.dateextractor.DateExtractor;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais.OpenCalais;
import tr.edu.gsu.nerwip.recognition.internal.modelless.subee.Subee;
import tr.edu.gsu.nerwip.recognition.internal.modelless.wikipediadater.WikipediaDater;
import tr.edu.gsu.nerwip.retrieval.ArticleRetriever;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.retrieval.reader.wikipedia.WikipediaReader;
import tr.edu.gsu.nerwip.tools.corpus.ArticleLists;
import tr.edu.gsu.nerwip.tools.corpus.ArticleRetrieval;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.freebase.FbCommonTools;
import tr.edu.gsu.nerwip.tools.freebase.FbIdTools;
import tr.edu.gsu.nerwip.tools.freebase.FbTypeTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.LinkTools;

/**
 * This is the main class to launch the main
 * processes implemented in Nerwip. Edit it
 * to perform a specific treatment.
 * 
 * @author Vincent Labatut
 */
@SuppressWarnings({ "unused" })
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
		// in the corpus root folder. Certain NER tools can be trained,
		// which can be performed using the training set. All tools are
		// evaluated on the testing set, whether they were trained or not.
		
		// If you didn't get the full Nerwip corpus (yet), the first
		// half of the articles in folder out are used for training,
		// and the other half for evaluation. Of course, it is not
		// enough to perform a sufficient training.
		
		// Also, note you need to get the NER tools related data from
		// FigShare (again, as explained in the README file), before
		// being able to apply most of the NER tools integrated in
		// Nerwip.
		
		
		///////////////////////////////////////////////
		// How to train standalone NER tools
		///////////////////////////////////////////////
		// Only some of the standalone tools can be trained
		// See the methods for tool-specific details 
		trainLingPipe();
		trainIllinois();
		trainOpenNlp();
		trainStanford();
		
		///////////////////////////////////////////////
		// How to apply individually standalone NER tools to a single article
		///////////////////////////////////////////////
		// See the invoked methods to see the set up of each NER tools
		// Here, each tool is applied to the first article in the corpus
		// As mentioned in the readme file, it is possible to implement
		// your own tools, or wrappers for other existing NER tools.
		applyDateExtractor();		// date-only NER tool
		applyWikipediaDater();		// another (better) date-only NER tool
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
		// is actually just a specific type of NER tool).
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
		// of the NER tools (the entities they detected).
		
		
		
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
	{	ArticleList folders = getTrainingSet();
		File folder = folders.get(0);
		String name = folder.getName();
		URL url = new URL("http://en.wikipedia.org/wiki/"+name); // fake url, enough for here
		
		// Here we ask the retriever to get an article
		// which is actually arealdy cached (i.e. in the corpus)
		// A bit convulated, I admit.
		ArticleRetriever retriever = new ArticleRetriever();
		Article result = retriever.process(url);
		
		return result;
	}
	
	/**
	 * Applies our (not very good) Date Extractor NER tool
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
		dateExtractor.process(article);

		logger.decreaseOffset();
	}

	/**
	 * Applies our Wikipedia Dater NER tool
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
		wikipediaDater.process(article);

		logger.decreaseOffset();
	}

	/**
	 * Applies the Illinois NER tool
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
		illinois.process(article);
		
		logger.decreaseOffset();
	}

	/**
	 * Applies the LingPipe NER tool
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
		lingPipe.process(article);
		
		logger.decreaseOffset();
	}

	/**
	 * Applies the OpenCalais NER tool
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
		
		boolean ignorePronouns = false;
		boolean exclusionOn = false;
		OpenCalais openCalais = new OpenCalais(ignorePronouns, exclusionOn);
		openCalais.setCacheEnabled(false);
		openCalais.process(article);

		logger.decreaseOffset();
	}

	/**
	 * Applies the OpenNLP NER tool
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
		openNlp.process(article);
		
		logger.decreaseOffset();
	}

	/**
	 * Applies the OpenNLP NER tool
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
		stanford.process(article);
			
		logger.decreaseOffset();
	}

	/**
	 * Applies our Subee NER tool
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
		subee.process(article);

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
	 * Evaluates the performances of a few standalone NER tools,
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
		
		// set the NER tools we want to evaluate (including their parameters)
		// note that by default, the entities detected by a NER are cached.
		// this means if the result file already exists, it will be loaded.
		// here, we use the same parameters than for the single-article tests.
		boolean loadOnDemand = true;
		AbstractRecognizer temp[] =
		{	new DateExtractor(),
			new WikipediaDater(),
			new Illinois(IllinoisModelName.CONLL_MODEL, true, true, false, false),
			new LingPipe(LingPipeModelName.APPROX_DICTIONARY, true, true, true, false, false),
			new OpenCalais(false, false),
			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL, true, false, false),
			new Stanford(StanfordModelName.CONLLMUC_MODEL, true, false, false),
			new Subee(true, true, true, true, true)
		};
		List<AbstractRecognizer> recognizers = Arrays.asList(temp);
		logger.log("Processed NER tools: ");
		logger.increaseOffset();
		for(AbstractRecognizer recognizer: recognizers)
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
		VoteMode voteMode = VoteMode.UNIFORM;
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
		voteCombiner.process(article);
		
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
		svmCombiner.process(article);
		
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
		
		// set the NER tools we want to evaluate (like before in evaluateStandaloneTools)
		boolean loadOnDemand = true;
		AbstractRecognizer temp[] =
		{	new VoteCombiner(true, true, VoteMode.UNIFORM, true, true, SubeeMode.NONE),
			new SvmCombiner(true, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.NONE)
		};
		List<AbstractRecognizer> recognizers = Arrays.asList(temp);
		logger.log("Processed NER tools: ");
		logger.increaseOffset();
		for(AbstractRecognizer recognizer: recognizers)
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
	// XXXXXXXXXX	/////////////////////////////////////////////////
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
		EntityEditor viewer = new EntityEditor();
		
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
