package tr.edu.gsu.nerwip.recognition.combiner;

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.AbstractConverter;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.internal.modelless.subee.Subee;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;

/**
 * This class implements a specific type of NER tool:
 * it actually combines the outputs of other tools, in order
 * to reach a higher overall performance.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractCombiner extends AbstractRecognizer
{	
	/**
	 * Builds a new combiner,
	 * using the specified combiner.
	 * 
	 * @throws RecognizerException
	 * 		Problem while loading some combiner.
	 */
	public AbstractCombiner() throws RecognizerException
	{	super(false, false, false);
	}
	
	/////////////////////////////////////////////////////////////////
	// CACHING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void setCacheEnabled(boolean enabled)
	{	super.setCacheEnabled(enabled);
	}
	
	/**
	 * Enable/disable the caches of each individual
	 * NER tool used by the combiner of this combiner.
	 * By default, the caches are set to the default
	 * values of the individual recognizers.
	 * 
	 * @param enabled
	 * 		Whether or not the combiner cache should be enabled.
	 */
	public void setSubCacheEnabled(boolean enabled)
	{	for(AbstractRecognizer recognizer: recognizers)
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
	// CONVERTER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Dummy converter, used only for loading/saving XML */
	protected AbstractConverter converter;
	
	/**
	 * Initializes the dummy converter,
	 * only used for accessing the generated
	 * XML file.
	 */
	protected void initConverter()
	{	converter = new AbstractConverter(getName(), getFolder(), null) {/* */};
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Gets a list of entities and removes all those
	 * whose type is not in the list of handled types.
	 * 
	 * @param entities
	 * 		List to be filtered.
	 */
	public void filterType(Entities entities)
	{	List<EntityType> handledTypes = getHandledEntityTypes();
		logger.log("Handled types: "+handledTypes.toString()+".)");
		logger.increaseOffset();
		
		List<AbstractEntity<?>> entityList = entities.getEntities();
		Iterator<AbstractEntity<?>> it = entityList.iterator();
		while(it.hasNext())
		{	AbstractEntity<?> entity = it.next();
			EntityType type = entity.getType();
			
			if(!handledTypes.contains(type))
			{	logger.log("Entity '"+entity+"' does not have one of the handled types.)");
				it.remove();
			}
		}
		
		logger.decreaseOffset();
	}

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
	// NER TOOLS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether to use the standalone NER tools with their default models ({@code false}), or ones specifically trained on our corpus ({@code true}) */
	protected boolean specific = false;
	/** NER tools used by this combiner */
	protected final List<AbstractRecognizer> recognizers = new ArrayList<AbstractRecognizer>();

	/**
	 * Returns the list of recognizers used
	 * by this combiner.
	 * 
	 * @return
	 * 		A list of recognizers.
	 */
	public List<AbstractRecognizer> getRecognizers()
	{	return recognizers;
	}
	
	/**
	 * Creates the objects representing
	 * the NER tools used by this combiner.
	 * 
	 * @throws RecognizerException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	protected abstract void initRecognizers() throws RecognizerException;
	
	/**
	 * Applies this combiner to the specified article,
	 * and returns a list of the detected entities.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		List of the resulting entities.
	 * 
	 * @throws RecognizerException
	 * 		Problem while applying the combiner. 
	 */
	protected Entities applyRecognizers(Article article) throws RecognizerException
	{	logger.log("Apply each NER tool separately");
		logger.increaseOffset();
		Map<AbstractRecognizer,Entities> entities = new HashMap<AbstractRecognizer,Entities>();
		for(AbstractRecognizer recognizer: recognizers)
		{	// apply the NER tool
			Entities temp = recognizer.process(article);
			// keep only the relevant types
			logger.log("Filter entities by type");
			filterType(temp);
			// add to map
			entities.put(recognizer, temp);
		}
		logger.decreaseOffset();
		
		logger.log("Combine the NER tools outputs");
		StringBuffer rawOutput = new StringBuffer();
		Entities result = combineEntities(article,entities,rawOutput);

		if(outRawResults)
		{	if(rawOutput.length()==0)
				logger.log("Raw output is empty >> Don't record it");
			else
			{	logger.log("Record raw output");
				try
				{	writeRawResultsStr(article,rawOutput.toString());
				}
				catch (IOException e)
				{	//e.printStackTrace();
					throw new RecognizerException(e.getMessage());
				}
			}
		}
		else
			logger.log("Raw output not recorded (option disabled)");
		
		return result;
	}

    /**
     * Takes a map representing the outputs
     * of each previously applied NER tool,
     * and combine those entities to get
     * a single set.
     * 
     * @param article
     * 		Concerned article.
     * @param entities
     * 		Map of the entities detected by the 
     * 		individual NER tools.
     * @param rawOutput
     * 		Empty {@code StringBuffer} the combiner can use to
     * 		write a text output for debugging purposes.
     * 		Or it can just let it empty.
     * @return
     * 		Result of the combination of those
     * 		individual entities.
     * 
     * @throws RecognizerException
     * 		Problem while combining entities.
    */
	protected abstract Entities combineEntities(Article article, Map<AbstractRecognizer,Entities> entities, StringBuffer rawOutput) throws RecognizerException;

	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Entities process(Article article) throws RecognizerException
	{	logger.log("Start applying "+getName()+" to "+article.getFolderPath()+" ("+article.getUrl()+")");
		logger.increaseOffset();
		Entities result = null;
		
		try
		{	// checks if the result file already exists
			File dataFile = converter.getXmlFile(article);
			boolean processNeedeed = !dataFile.exists();
			
			// if needed, we process the text
			if(!cache || processNeedeed)
			{	// apply the NER tool
				logger.log("Detect the entities");
				result = applyRecognizers(article);
				
				// record entities using our xml format
				logger.log("Record entities using our XML format");
				converter.writeXmlResults(article,result);
			}
			
			// if the results already exist, we fetch them
			else
			{	logger.log("Loading entities from cached file");
				result = converter.readXmlResults(article);
			}
		}
		catch(FileNotFoundException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
		catch (SAXException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
		catch (ParseException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
		
		int nbrEnt = result.getEntities().size();
		logger.log(getName()+" over ["+article.getName()+"], found "+nbrEnt+" entities");
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
	{	String result = getModelPath() + File.separator + getFolder() + ".catprop" + FileNames.EX_TXT;
		return result;
	}
	
	/**
	 * Loads the previously processed category proportions,
	 * to be used by this class to combine the NER tools outputs.
	 * 
	 * @throws RecognizerException
	 * 		Problem while loading the category proportions.
	 */
	protected void loadCategoryProportions() throws RecognizerException
	{	String filename = getCategoryProportionsPath();
		try
		{	categoryProportions = CategoryProportions.loadCategoryProportions(filename);
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// VOTE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Weights used when NER must vote */
	protected VoteWeights voteWeights;

	/**
	 * Loads all the necessary weights
	 * to allow NER voting.
	 * 
	 * @throws RecognizerException 
	 * 		Problem while loading the vote weights.
	 */
	protected void loadVoteWeights() throws RecognizerException
	{	String filePath = getVoteWeightsPath();
		try
		{	voteWeights = VoteWeights.loadVoteWeights(filePath,recognizers);
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
	}
	
	/**
	 * Returns the name of the file containing
	 * the vote weights for the NER tools. 
	 * These values must have been processed previously, 
	 * this class is not meant to perform this calculation.
	 * 
	 * @return
	 * 		Path of the weights file.
	 */
	public String getVoteWeightsPath()
	{	String result = getModelPath() + File.separator + getFolder() + ".weights" + FileNames.EX_TXT;
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
	/** Name of the file possibly generated by the NER tool */
	protected String rawFile = FileNames.FI_OUTPUT_TEXT;
	
	/**
	 * Returns the raw file associated to the specified
	 * article, i.e. the file representing the unprocessed
	 * output of this combiner.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		A {@code File} object representing the associated raw result file.
	 */
	public File getRawFile(Article article)
	{	String resultsFolder = article.getFolderPath();
		resultsFolder = resultsFolder + File.separator + getFolder();
		String filePath = resultsFolder + File.separator + rawFile;
	
		File result = new File(filePath);
		return result;
	}

	/**
	 * Write the raw results obtained for the specified article.
	 * This method is meant for combiner able to output their
	 * raw results as a text file, for further monitoring.
	 * 
	 * @param article
	 * 		Concerned article.
	 * @param results
	 * 		String representation of the NER tool result.		
	 * 
	 * @throws IOException 
	 * 		Problem while recording the file.
	 */
	protected void writeRawResultsStr(Article article, String results) throws IOException
	{	File file = getRawFile(article);
		File folder = file.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		FileTools.writeTextFile(file, results);
	}
}
