package fr.univavignon.nerwip.processing.combiner;

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractDelegateRecognizer;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelless.subee.Subee;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;

/**
 * This class implements a specific type of recognizer:
 * it actually combines the outputs of other tools, in order
 * to reach a higher overall performance.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractCombinerDelegateRecognizer extends AbstractDelegateRecognizer
{	
	/**
	 * Builds a new combiner,
	 * using the specified combiner.
	 * 
	 * @param recognizer
	 * 		Recognizer associated to this delegate.
	 * 
	 * @throws ProcessorException
	 * 		Problem while loading some combiner.
	 */
	public AbstractCombinerDelegateRecognizer(InterfaceRecognizer recognizer) throws ProcessorException
	{	super(recognizer, false, false, false, false);
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Gets a list of mentions and removes all those
	 * whose type is not in the list of handled types.
	 * 
	 * @param mentions
	 * 		List to be filtered.
	 */
	public void filterType(Mentions mentions)
	{	List<EntityType> handledTypes = getHandledEntityTypes();
		logger.log("Handled types: "+handledTypes.toString()+".)");
		logger.increaseOffset();
		
		List<AbstractMention<?>> mentionList = mentions.getMentions();
		Iterator<AbstractMention<?>> it = mentionList.iterator();
		while(it.hasNext())
		{	AbstractMention<?> mention = it.next();
			EntityType type = mention.getType();
			
			if(!handledTypes.contains(type))
			{	logger.log("Mention '"+mention+"' does not have one of the handled types.)");
				it.remove();
			}
		}
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// CACHING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Enables/disables the cache of each individual
	 * recognizer used by the delegate of this combiner.
	 * By default, the caches are set to the default
	 * values of the individual recognizers.
	 * 
	 * @param enabled
	 * 		Whether or not the combiner cache should be enabled.
	 */
	public void setSubCacheEnabled(boolean enabled)
	{	for(InterfaceRecognizer recognizer: recognizers)
			recognizer.setCacheEnabled(enabled);
	}
	
	/////////////////////////////////////////////////////////////////
	// GENERAL MODEL	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the folder containing
	 * all the model-related files.
	 * 
	 * @return
	 * 		A String representing the path of the model folder.
	 */
	public abstract String getModelPath();
	
	/////////////////////////////////////////////////////////////////
	// SUBEE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Describes how {@link Subee} is applied (or not applied) */
	protected SubeeMode subeeMode;
	
	/**
	 * Enumeration used to configure how
	 * {@link Subee} is applied (or not).
	 * 
	 * @author Vincent Labatut
	 */
	public enum SubeeMode
	{	/** Does not use {@link Subee} */
		NONE("none"),
		/** Uses {@link Subee} with parameter addOcc=false */
		SINGLE("single"),
		/** Uses {@link Subee} with parameter addOcc=true */
		ALL("all");
		
		/** String representing the parameter value */
		private String name;
		
		/**
		 * Builds a new Subee mode value
		 * to be used as a parameter.
		 * 
		 * @param name
		 * 		String representing the parameter value.
		 */
		SubeeMode(String name)
		{	this.name = name;
		}
		
		@Override
		public String toString()
		{	return name;
		}
	}
	
	/**
	 * Returns the mode used to apply {@link Subee}.
	 * 
	 * @return
	 * 		A symbol representing how {@code Subee} should be applied.
	 */
	public SubeeMode getSubeeMode()
	{	return subeeMode;
	}

	/////////////////////////////////////////////////////////////////
	// RECOGNIZERS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether to use the standalone recognizers with their default models ({@code false}), or ones specifically trained on our corpus ({@code true}) */
	protected boolean specific = false;
	/** Recognizers used by this combiner */
	protected final List<InterfaceRecognizer> recognizers = new ArrayList<InterfaceRecognizer>();

	/**
	 * Returns the list of recognizers used
	 * by this combiner.
	 * 
	 * @return
	 * 		A list of recognizers.
	 */
	public List<InterfaceRecognizer> getRecognizers()
	{	return recognizers;
	}
	
	/**
	 * Creates the objects representing
	 * the recognizers used by this combiner.
	 * 
	 * @throws ProcessorException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	protected abstract void initRecognizers() throws ProcessorException;
	
	/**
	 * Applies this combiner to the specified article,
	 * and returns a list of the detected mentions.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		List of the resulting mentions.
	 * 
	 * @throws ProcessorException
	 * 		Problem while applying the combiner. 
	 */
	protected Mentions applyRecognizers(Article article) throws ProcessorException
	{	logger.log("Apply each recognizer separately");
		logger.increaseOffset();
		Map<InterfaceRecognizer,Mentions> mentions = new HashMap<InterfaceRecognizer,Mentions>();
		for(InterfaceRecognizer recognizer: recognizers)
		{	// apply the recognizer
			Mentions temp = recognizer.recognize(article);
			// keep only the relevant types
			logger.log("Filter mentions by type");
			filterType(temp);
			// add to map
			mentions.put(recognizer, temp);
		}
		logger.decreaseOffset();
		
		logger.log("Combine the recognizers outputs");
		StringBuffer rawOutput = new StringBuffer();
		Mentions result = combineMentions(article,mentions,rawOutput);

		if(this.recognizer.doesOutputRawResults())
		{	if(rawOutput.length()==0)
				logger.log("Raw output is empty >> Don't record it");
			else
			{	logger.log("Record raw output");
				try
				{	writeRawResultsStr(article,rawOutput.toString());
				}
				catch (IOException e)
				{	//e.printStackTrace();
					throw new ProcessorException(e.getMessage());
				}
			}
		}
		else
			logger.log("Raw output not recorded (option disabled)");
		
		return result;
	}

    /**
     * Takes a map representing the outputs
     * of each previously applied recognizer,
     * and combine those mentions to get
     * a single set.
     * 
     * @param article
     * 		Concerned article.
     * @param mentions
     * 		Map of the mentions detected by the 
     * 		individual recognizers.
     * @param rawOutput
     * 		Empty {@code StringBuffer} the combiner can use to
     * 		write a text output for debugging purposes.
     * 		Or it can just let it empty.
     * @return
     * 		Result of the combination of those
     * 		individual mentions.
     * 
     * @throws ProcessorException
     * 		Problem while combining mentions.
    */
	protected abstract Mentions combineMentions(Article article, Map<InterfaceRecognizer,Mentions> mentions, StringBuffer rawOutput) throws ProcessorException;
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions delegateRecognize(Article article) throws ProcessorException
	{	ProcessorName recognizerName = recognizer.getName();
		logger.log("Start applying "+recognizerName+" to "+article.getFolderPath()+" ("+article.getUrl()+")");
		logger.increaseOffset();
		Mentions result = null;
		
		try
		{	// checks if the result file already exists
			File dataFile = getXmlFile(article);
			boolean processNeedeed = !dataFile.exists();
			
			// if needed, we process the text
			if(!recognizer.doesCache() || processNeedeed)
			{	// check language
				ArticleLanguage language = article.getLanguage();
				if(language==null)
					logger.log("WARNING: The article language is unknown >> it is possible this recognizer does not handle this language");
				else if(!canHandleLanguage(language))
					logger.log("WARNING: This recognizer does not handle the language of this article ("+language+")");
				
				// apply the recognizer
				logger.log("Detect the mentions");
				result = applyRecognizers(article);
				
				// record mentions using our xml format
				logger.log("Record mentions using our XML format");
				writeXmlResults(article,result);
			}
			
			// if the results already exist, we fetch them
			else
			{	logger.log("Loading mentions from cached file");
				result = readXmlResults(article);
			}
		}
		catch(FileNotFoundException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		catch (SAXException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		catch (ParseException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
		
		int nbrEnt = result.getMentions().size();
		logger.log(recognizerName+" over ["+article.getName()+"], found "+nbrEnt+" mentions");
		logger.decreaseOffset();

		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CATEGORY PROPORTIONS		/////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of articles of the given categories in the training set */
	protected CategoryProportions categoryProportions;
	
//	/**
//	 * Returns the current category proportions for this tool.
//	 * 
//	 * @return
//	 * 		Previously processed category proportions.
//	 */
//	public CategoryProportions getCategoryProportions()
//	{	return categoryProportions;
//	}
	
	/**
	 * Returns the name of the file containing
	 * the category proportions for the training set. 
	 * These values must have been processed previously, 
	 * this class is not meant to perform this calculation.
	 * 
	 * @return
	 * 		A String representing the path of the category proportions file.
	 */
	public String getCategoryProportionsPath()
	{	String result = getModelPath() + File.separator + recognizer.getRecognizerFolder() + ".catprop" + FileNames.EX_TEXT;
		return result;
	}
	
	/**
	 * Loads the previously processed category proportions,
	 * to be used by this class to combine the recognizers outputs.
	 * 
	 * @throws ProcessorException
	 * 		Problem while loading the category proportions.
	 */
	protected void loadCategoryProportions() throws ProcessorException
	{	String filename = getCategoryProportionsPath();
		try
		{	categoryProportions = CategoryProportions.loadCategoryProportions(filename);
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// VOTE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Weights used when NER must vote */
	protected VoteWeights<InterfaceRecognizer> voteWeights;

	/**
	 * Loads all the necessary weights
	 * to allow NER voting.
	 * 
	 * @throws ProcessorException 
	 * 		Problem while loading the vote weights.
	 */
	protected void loadVoteWeights() throws ProcessorException
	{	String filePath = getVoteWeightsPath();
		try
		{	voteWeights = VoteWeights.loadVoteWeights(filePath,recognizers);
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
	}
	
	/**
	 * Returns the name of the file containing
	 * the vote weights for the recognizers. 
	 * These values must have been processed previously, 
	 * this class is not meant to perform this calculation.
	 * 
	 * @return
	 * 		Path of the weights file.
	 */
	public String getVoteWeightsPath()
	{	String result = getModelPath() + File.separator + recognizer.getRecognizerFolder() + ".weights" + FileNames.EX_TEXT;
		return result;
	}

	/**
	 * Sort the keys in the map, so that the first one
	 * corresponds to the smallest associated value and
	 * the last to the greatest one.
	 * 
	 * @param <T>
	 * 		Type of the keys in the map.
	 * @param <U>
	 * 		Type of the values in the map.
	 * @param map
	 * 		Map containing the scores.
	 * @return
	 * 		Keys sorted depending on their associated value.
	 */
	protected <T,U extends Comparable<U>> List<T> getSortedKeys(final Map<T,U> map)
	{	Comparator<T> comparator = new Comparator<T>()
		{	@Override
			public int compare(T o1, T o2)
			{	U v1 = map.get(o1);
				U v2 = map.get(o2);
				int result = v1.compareTo(v2);
				return result;
			}	
		};
		
		List<T> result = new ArrayList<T>(map.keySet());
		Collections.sort(result, comparator);
		
		return result;
	}
	
	/**
	 * Receives the scores associated to start and end positions
	 * and returns a couple of positions such that their scores
	 * are maximal and they are consistent (start<=end) at the
	 * same time.
	 * 
	 * @param <U> 
	 * 		Type of the scores.
	 * @param startScores
	 * 		Scores map for the start positions.
	 * @param endScores
	 * 		Scores map for the end positions.
	 * @return
	 * 		An array containing a couple of positions.
	 */
	protected <U extends Comparable<U>> int[] getPositionFromScores(Map<Integer, U> startScores, Map<Integer, U> endScores)
	{	logger.log("Process a consistant position using votes");
		logger.increaseOffset();
		
		// sort positions depending on their respective scores
		List<Integer> starts = getSortedKeys(startScores);
		Collections.reverse(starts);
		List<Integer> ends = getSortedKeys(endScores);
		Collections.reverse(ends);
		
		// display scores
		logger.log("Propositions and votes:");
		logger.increaseOffset();
		{	// start pos
			{	String line = "start positions:";
				Iterator<Integer> itStart = starts.iterator();
				while(itStart.hasNext())
				{	int pos = itStart.next();
					line = line + pos;
					U vote = startScores.get(pos);
					line = line + "("+vote+"); ";
				}
				logger.log(line);
			}
			// end pos
			{	String line = "end positions:";
				Iterator<Integer> itEnd = ends.iterator();
				while(itEnd.hasNext())
				{	int pos = itEnd.next();
					line = line + pos;
					U vote = endScores.get(pos);
					line = line + "("+vote+"); ";
				}
				logger.log(line);
			}
		}
		logger.decreaseOffset();
		
		// solve inconsistancies (i.e. start located after end)
		Iterator<Integer> itStart = starts.iterator();
		Iterator<Integer> itEnd = ends.iterator();
		int startPos = itStart.next();
		int endPos = itEnd.next();
		logger.log("Initial proposition: ("+startPos+","+endPos+")");
		logger.increaseOffset();
		while(startPos>endPos && (itStart.hasNext() || itEnd.hasNext()))
		{	if(itEnd.hasNext())
				endPos = itEnd.next();
			else if(itStart.hasNext())
			{	startPos = itStart.next();
				itEnd = ends.iterator();
				endPos = itEnd.next();
			}
			logger.log("Proposition rejected, considering: ("+startPos+","+endPos+")");
		}
		logger.decreaseOffset();
		
		// finalize result
		if(startPos>endPos)
		{	startPos = -1;
			endPos = -1;
			logger.log("No consistant position could be found");
		}
		int result[] = {startPos, endPos};
		logger.log("Final result: ("+startPos+","+endPos+")");
		logger.decreaseOffset();
		return result;
	}

	/**
	 * Combine the recognizers results, in order to determine the
	 * value of the detected mention. We just use a uniform voting,
	 * since one cannot really distinguish the recognizers, here.
	 * 
	 * @param map 
	 * 		Group of estimated mentions.
	 * @param type 
	 * 		Estimated type for the treated mention.
	 * @return 
	 * 		Value of the mention, or {@code null} if no value was determined.
	 */
	protected Comparable<?> voteForValue(Map<InterfaceRecognizer, AbstractMention<?>> map, EntityType type)
	{	logger.log("Start voting for value:");
		logger.increaseOffset();
		
		Map<Comparable<?>,Float> scores = new HashMap<Comparable<?>, Float>();
		
		for(InterfaceRecognizer recognizer: recognizers)
		{	AbstractMention<?> mention = map.get(recognizer);
		
			// check existence
			if(mention!=null)
			{	EntityType t = mention.getType();
				if(t==type)
				{	Comparable<?> value = mention.getValue();
					Float score = scores.get(value);
					if(score==null)
						score = 0f;
					score = score + 1;
					scores.put(value,score);
				}
			}
		}
		
		Comparable<?> result = getValueFromScores(scores);
		logger.decreaseOffset();
		logger.log("Result of the vote for value: "+result);
		return result;
	}
	
	/**
	 * Receives the scores associated to values and returns the 
	 * value whose score is maximal, or {@code null} if the map
	 * is empty.
	 * 
	 * @param <U> 
	 * 		Type of the scores.
	 * @param scores
	 * 		Scores map for the values.
	 * @return
	 * 		The majority value, or {@code null} if the map is empty.
	 */
	protected <T, U extends Comparable<U>> T getValueFromScores(Map<T, U> scores)
	{	logger.log("Process the majority value using votes");
		logger.increaseOffset();
		
		// sort values depending on their respective scores
		List<T> values = getSortedKeys(scores);
		Collections.reverse(values);
		
		// display scores
		logger.log("Propositions and votes:");
		logger.increaseOffset();
		{	String line = null;
			Iterator<T> it = values.iterator();
			while(it.hasNext())
			{	T value = it.next();
				line = line + value;
				U vote = scores.get(value);
				line = line + "("+vote+"); ";
			}
			logger.log(line);
		}
		logger.decreaseOffset();
		
		// get the majority value
		Iterator<T> it = values.iterator();
		T result = it.next();
		
		logger.log("Final result: "+result);
		logger.decreaseOffset();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Splits a text using whitespaces and punctuation.
	 * Returns a list of strings corresponding (roughly)
	 * to words.
	 * 
	 * @param text
	 * 		Original text.
	 * @return
	 * 		List of words.
	 */
	public List<String> getWordListFromText(String text)
	{	List<String> result = new ArrayList<String>();
		text = text.trim();
		String temp[] = text.split("[\\p{Punct}\\s]+");
		for(String s: temp)
		{	if(!s.isEmpty())
				result.add(s);
		}
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW OUTPUT		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Write the raw results obtained for the specified article.
	 * This method is meant for combiner able to output their
	 * raw results as a text file, for further monitoring.
	 * 
	 * @param article
	 * 		Concerned article.
	 * @param results
	 * 		String representation of the recognizer result.		
	 * 
	 * @throws IOException 
	 * 		Problem while recording the file.
	 */
	protected void writeRawResultsStr(Article article, String results) throws IOException
	{	File file = getRawFile(article);
		File folder = file.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		FileTools.writeTextFile(file, results, "UTF-8");
	}
}
