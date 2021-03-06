package tr.edu.gsu.nerwip.recognition.combiner.votebased;

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

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.evaluation.ArticleList;
import tr.edu.gsu.nerwip.evaluation.Evaluator;
import tr.edu.gsu.nerwip.evaluation.measure.AbstractMeasure;
import tr.edu.gsu.nerwip.evaluation.measure.LilleMeasure;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.combiner.CategoryProportions;
import tr.edu.gsu.nerwip.recognition.combiner.VoteWeights;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner.VoteMode;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

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
	 * @throws RecognizerException
	 * 		Problem while creating the dummy combiner.
	 */
	public VoteTrainer(VoteCombiner combiner) throws RecognizerException
	{	this.combiner = combiner;
	}
	
	/////////////////////////////////////////////////////////////////
	// MODEL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** An instance of the combiner object associated to this trainer */
	protected VoteCombiner combiner = null;
	
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
	 * NER tool used by the combiner of this trainer.
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
	 * @throws RecognizerException 
	 * 		Problem applying the evaluator.
	 * @throws ConverterException 
	 * 		Problem applying the evaluator.
	 */
	private void processVoteData(ArticleList folders) throws ReaderException, IOException, ParseException, SAXException, ConverterException, RecognizerException
	{	VoteMode voteMode = combiner.getVoteMode();
		if(voteMode.hasWeights())
		{	// vote weights
			{	// process
				List<EntityType> types = combiner.getHandledEntityTypes();
				List<AbstractRecognizer> recognizers = combiner.getRecognizers();
				AbstractMeasure measure = new LilleMeasure(null);
				Evaluator evaluator = new Evaluator(types, recognizers, folders, measure);
				evaluator.process();
				List<String> names = Arrays.asList(
					LilleMeasure.SCORE_FP, LilleMeasure.SCORE_TP,
					LilleMeasure.SCORE_FR, LilleMeasure.SCORE_TR,
					LilleMeasure.SCORE_P, LilleMeasure.SCORE_R
				);
				boolean byCategory = voteMode==VoteMode.WEIGHTED_CATEGORY;
				VoteWeights voteWeights = VoteWeights.buildWeightsFromEvaluator(evaluator,names,byCategory);
				
				// record
				String filePath = combiner.getVoteWeightsPath();
				voteWeights.recordVoteWeights(filePath);
			}
			
			// category proportions
			if(voteMode==VoteMode.WEIGHTED_CATEGORY)
			{	// process
				CategoryProportions result = CategoryProportions.buildProportionsFromCorpus(folders);
				
				// record
				String filePath = combiner.getCategoryProportionsPath();
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
	 * 		Problem while accessing an entity file. 
	 * @throws ParseException
	 * 		Problem while accessing an entity file. 
	 * @throws ReaderException
	 * 		Problem while accessing a file. 
	 * @throws RecognizerException
	 * 		Problem while applying a NER tool. 
	 * @throws ConverterException 
	 * 		Problem while processing a NER tool performance. 
	 */
	public void process(ArticleList folders) throws IOException, SAXException, ParseException, ReaderException, RecognizerException, ConverterException
	{	logger.increaseOffset();
		
		// process and record the voting weights
		processVoteData(folders);
		
		logger.decreaseOffset();
	}
}
