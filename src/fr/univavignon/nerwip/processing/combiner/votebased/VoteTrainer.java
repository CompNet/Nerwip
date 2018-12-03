package fr.univavignon.nerwip.processing.combiner.votebased;

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
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.SAXException;

import fr.univavignon.common.data.article.ArticleList;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.nerwip.evaluation.recognition.RecognitionEvaluator;
import fr.univavignon.nerwip.evaluation.recognition.measures.AbstractRecognitionMeasure;
import fr.univavignon.nerwip.evaluation.recognition.measures.RecognitionLilleMeasure;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.combiner.CategoryProportions;
import fr.univavignon.nerwip.processing.combiner.VoteWeights;
import fr.univavignon.retrieval.reader.ReaderException;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

/**
 * This class sets up the voting parameters for the
 * associated VoteCombiner. The resulting data is recorded,
 * and will be loaded later when the combiner will be used.
 * 
 * @author Vincent Labatut
 */
public class VoteTrainer
{	
	/**
	 * Creates a new trainer
	 * for the specified vote-based combiner.
	 * 
	 * @param combiner
	 * 		Vote-based combiner considered for the training.
	 * 
	 * @throws ProcessorException
	 * 		Problem while creating the dummy combiner.
	 */
	public VoteTrainer(VoteCombiner combiner) throws ProcessorException
	{	this.combiner = combiner;
		delegateRecognizer = combiner.getDelegateRecognizer();
	}
	
	/////////////////////////////////////////////////////////////////
	// MODEL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** An instance of the combiner object associated to this trainer */
	protected VoteCombiner combiner = null;
	/** An instance of the delegate recognizer object associated to the combiner*/
	protected VoteCombinerDelegateRecognizer delegateRecognizer = null;
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CACHING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not cache should be used */
	protected boolean cache = true;
	
	/**
	 * Changes the cache flag. If {code true}, the {@link #process(ArticleList) process}
	 * method will first check if the input data already
	 * exists as a file. In this case, they will be loaded
	 * from this file. Otherwise, the process will be
	 * conducted normally, then recorded (cached).
	 * 
	 * @param enabled
	 * 		If {@code true}, the (possibly) cached files are used.
	 */
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
	}

	/**
	 * Enable/disable the caches of each individual
	 * recognizer used by the combiner of this trainer.
	 * By default, the caches are set to the default
	 * values of the individual recognizers.
	 * 
	 * @param enabled
	 * 		Whether or not the combiner cache should be enabled.
	 */
	public void setSubCacheEnabled(boolean enabled)
	{	combiner.setCacheEnabled(enabled);
	}
	
	/////////////////////////////////////////////////////////////////
	// VOTE DATA		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes all data required for voting.
	 * 
	 * @param folders
	 * 		List of articles.
	 * 
	 * @throws ReaderException
	 * 		Problem while retrieving an article.
	 * @throws IOException
	 * 		Problem while retrieving an article.
	 * @throws ParseException
	 * 		Problem while retrieving an article.
	 * @throws SAXException
	 * 		Problem while retrieving an article.
	 * @throws ProcessorException 
	 * 		Problem applying the evaluator.
	 */
	private void processVoteData(ArticleList folders) throws ReaderException, IOException, ParseException, SAXException, ProcessorException
	{	VoteMode voteMode = delegateRecognizer.getVoteMode();
		if(voteMode.hasWeights())
		{	// vote weights
			{	// process
				List<EntityType> types = delegateRecognizer.getHandledEntityTypes();
				List<InterfaceRecognizer> recognizers = delegateRecognizer.getRecognizers();
				AbstractRecognitionMeasure measure = new RecognitionLilleMeasure(null);
				RecognitionEvaluator recognitionEvaluator = new RecognitionEvaluator(types, recognizers, folders, measure);
				recognitionEvaluator.process();
				List<String> names = Arrays.asList(
					RecognitionLilleMeasure.SCORE_FP, RecognitionLilleMeasure.SCORE_TP,
					RecognitionLilleMeasure.SCORE_FR, RecognitionLilleMeasure.SCORE_TR,
					RecognitionLilleMeasure.SCORE_P, RecognitionLilleMeasure.SCORE_R
				);
				boolean byCategory = voteMode==VoteMode.WEIGHTED_CATEGORY;
				VoteWeights<InterfaceRecognizer> voteWeights = VoteWeights.buildWeightsFromEvaluator(recognitionEvaluator,names,byCategory);
				
				// record
				String filePath = delegateRecognizer.getVoteWeightsPath();
				voteWeights.recordVoteWeights(filePath);
			}
			
			// category proportions
			if(voteMode==VoteMode.WEIGHTED_CATEGORY)
			{	// process
				CategoryProportions result = CategoryProportions.buildProportionsFromCorpus(folders);
				
				// record
				String filePath = delegateRecognizer.getCategoryProportionsPath();
				result.recordCategoryProportion(filePath);
			}
		}
		else
			logger.log("No training needed for vote weights");
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Process the weights required for voting, thanks to the specified data,
	 * and for the specified entity types.
	 * 
	 * @param folders
	 * 		List of concered articles.
	 * 
	 * @throws IOException
	 * 		Problem while accessing a file. 
	 * @throws SAXException
	 * 		Problem while accessing a mention file. 
	 * @throws ParseException
	 * 		Problem while accessing a mention file. 
	 * @throws ReaderException
	 * 		Problem while accessing a file. 
	 * @throws ProcessorException
	 * 		Problem while applying a recognizer. 
	 */
	public void process(ArticleList folders) throws IOException, SAXException, ParseException, ReaderException, ProcessorException
	{	logger.increaseOffset();
		
		// process and record the voting weights
		processVoteData(folders);
		
		logger.decreaseOffset();
	}
}
