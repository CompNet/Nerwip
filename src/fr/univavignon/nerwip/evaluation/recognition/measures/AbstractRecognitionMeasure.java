package fr.univavignon.nerwip.evaluation.recognition.measures;

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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.nerwip.data.article.ArticleCategory;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.evaluation.AbstractMeasure;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.time.TimeFormatting;

/**
 * This class contains a set of methods useful for
 * implementing performance measures.
 * <br/>
 * Those measures are used to assess how well
 * a recognizer performs on a given (annotated)
 * dataset.
 * <br/>
 * In these classes, we consider two kinds of
 * numerical values: counts and scores. Counts
 * are integers, such as the number of true positives,
 * false positives, etc. Scores are floats,
 * corresponding to calculations performed on
 * the counts, such as F-measure, percent correct,
 * etc.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractRecognitionMeasure extends AbstractMeasure
{	
	/**
	 * Builds a new instance of measure,
	 * for the specified recognizer. This
	 * constructor is used when loading results
	 * from a file.
	 * 
	 * @param recognizer
	 * 		Concerned recognizer.
	 */
	public AbstractRecognitionMeasure(InterfaceRecognizer recognizer)
	{	this.recognizer = recognizer;
		
		initialize();
	}
	
	/**
	 * Builds a new instance of measure,
	 * for the specified recognizer. This
	 * constructor is used when combining
	 * several measures into one.
	 * 
	 * @param recognizer
	 * 		Concerned recognizer.
	 * @param types
	 * 		Types to consider in the assessmnent.
	 */
	public AbstractRecognitionMeasure(InterfaceRecognizer recognizer, List<EntityType> types)
	{	this.recognizer = recognizer;
		this.types.addAll(types);
		
		initialize();
	}
	
	/**
	 * Builds a new instance of measure,
	 * for the specified recognizer and results. 
	 * This constructor is used when actually processing
	 * the recognizer performance.
	 * 
	 * @param recognizer
	 * 		Concerned recognizer.
	 * @param types
	 * 		Types to consider in the assessmnent.
	 * @param reference
	 * 		Mentions used as reference.
	 * @param estimation
	 * 		Mentions detected by the recognizer.
	 * @param categories
	 * 		Categories of article (military, scientist, etc.).
	 */
	public AbstractRecognitionMeasure(InterfaceRecognizer recognizer, List<EntityType> types, Mentions reference, Mentions estimation, List<ArticleCategory> categories)
	{	this.recognizer = recognizer;
		this.types.addAll(types);
		
		initialize();
		
		processCounts(reference, estimation, categories);
	}
	
	/**
	 * Builds the appropriate measure
	 * for the specified parameters.
	 * 
	 * @param recognizer
	 * 		Concerned recognizer.
	 * @param types
	 * 		Types to consider in the assessmnent.
	 * @param reference
	 * 		Mentions used as reference.
	 * @param estimation
	 * 		Mentions detected by the recognizer.
	 * @param categories
	 * 		Categories of article (military, scientist, etc.).
	 * @return
	 * 		The created measure. 
	 */
	public abstract AbstractRecognitionMeasure build(InterfaceRecognizer recognizer, List<EntityType> types, Mentions reference, Mentions estimation, List<ArticleCategory> categories);
	
	/**
	 * Builds the appropriate measure,
	 * for the specified recognizer. This
	 * class is used when creating measures
	 * describing whole collections.
	 * 
	 * @param recognizer
	 * 		Concerned recognizer.
	 * @param types
	 * 		Types to consider in the assessmnent.
	 * @return
	 * 		The created measure. 
	 */
	public abstract AbstractRecognitionMeasure build(InterfaceRecognizer recognizer, List<EntityType> types);

	/**
	 * Builds the appropriate measure,
	 * for the specified recognizer. This
	 * class is used when loading results
	 * from a file.
	 * 
	 * @param recognizer
	 * 		Concerned recognizer.
	 * @return
	 * 		The created measure. 
	 */
	public abstract AbstractRecognitionMeasure build(InterfaceRecognizer recognizer);
	
	/**
	 * Initializes the lists used during processing.
	 */
	private void initialize()
	{	initializeCounts();
		initializeMentions();
	}

	/////////////////////////////////////////////////////////////////
	// RECOGNIZER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Recognizer assessed by this measure */
	protected InterfaceRecognizer recognizer;
	
	/**
	 * Returns the recognizer
	 * assessed by this measure.
	 * 
	 * @return
	 * 		Concerned recognizer.
	 */
	public InterfaceRecognizer getRecognizer()
	{	return recognizer;
	}
	
	/////////////////////////////////////////////////////////////////
	// TYPES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of types to consider during the assessment */
	protected final List<EntityType> types = new ArrayList<EntityType>();
	
	/**
	 * Removes from the specified list
	 * all the mentions whose type is not
	 * in the type list.
	 * 
	 * @param mentions
	 * 		The list of mentions to process.
	 */
	protected void cleanMentions(List<AbstractMention<?>> mentions)
	{	Iterator<AbstractMention<?>> it = mentions.iterator();
		while(it.hasNext())
		{	AbstractMention<?> mention = it.next();
			EntityType type = mention.getType();
			if(!types.contains(type))
				it.remove();
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// CATEGORIES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Return the categories (military, scientist, etc.) represented 
	 * during this assessment.
	 * 
	 * @return
	 * 		A list of categories.
	 */
	public List<ArticleCategory> getCategories()
	{	Set<ArticleCategory> temp = new TreeSet<ArticleCategory>();
		
		for(String count: getCountNames())
		{	Map<ArticleCategory,Integer> map = countsByCategory.get(count);
			Set<ArticleCategory> categories = map.keySet();
			temp.addAll(categories);
		}
		
		List<ArticleCategory> result = new ArrayList<ArticleCategory>(temp);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// MENTIONS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of all processed mention */
	protected Map<String,List<AbstractMention<?>>> mentionsAll = null;
	/** List of mentions by type */
	protected Map<String,Map<EntityType,List<AbstractMention<?>>>> mentionsByType = null;
	/** List of mentions by category */
	protected Map<String,Map<ArticleCategory,List<AbstractMention<?>>>> mentionsByCategory = null;

	/**
	 * Initializes the lists of mentions.
	 */
	private void initializeMentions()
	{	mentionsAll = new HashMap<String,List<AbstractMention<?>>>();
		mentionsByType = new HashMap<String,Map<EntityType,List<AbstractMention<?>>>>();
		mentionsByCategory = new HashMap<String,Map<ArticleCategory,List<AbstractMention<?>>>>();

		for(String measure: getCountNames())
		{	// overall 
			List<AbstractMention<?>> mentionsAllList = new ArrayList<AbstractMention<?>>();
			mentionsAll.put(measure,mentionsAllList);
			// by type 
			Map<EntityType,List<AbstractMention<?>>> mentionsByTypeMap = new HashMap<EntityType, List<AbstractMention<?>>>();
			for(EntityType type: types)
				mentionsByTypeMap.put(type, new ArrayList<AbstractMention<?>>());
			mentionsByType.put(measure, mentionsByTypeMap);
			// by category 
			Map<ArticleCategory,List<AbstractMention<?>>> mentionsByCatMap = new HashMap<ArticleCategory, List<AbstractMention<?>>>();
			mentionsByCategory.put(measure, mentionsByCatMap);
		}
	}
	
	/**
	 * Returns the list of all mentions,
	 * for the specified count.
	 * 
	 * @param count
	 * 		Count of interest.
	 * @return
	 * 		List of corresponding mentions.
	 */
	public List<AbstractMention<?>> getMentionsAll(String count)
	{	List<AbstractMention<?>> result = mentionsAll.get(count);
		return result;
	}
	
	/**
	 * Returns the list of mention,
	 * for the specified count,
	 * and considering only the specified
	 * entity type.
	 * 
	 * @param count
	 * 		Count of interest.
	 * @param type
	 * 		Type of interest. 
	 * @return
	 * 		List of corresponding mentions.
	 */
	public List<AbstractMention<?>> getMentionsByType(String count, EntityType type)
	{	Map<EntityType,List<AbstractMention<?>>> map = mentionsByType.get(count);
		List<AbstractMention<?>> result = map.get(type);
		return result;
	}
	
	/**
	 * Returns the list of mentions,
	 * for the specified count,
	 * and considering only the specified
	 * article category.
	 * 
	 * @param count
	 * 		Count of interest.
	 * @param category
	 * 		Category of interest. 
	 * @return
	 * 		List of corresponding mentions.
	 */
	public List<AbstractMention<?>> getMentionsByCategory(String count, ArticleCategory category)
	{	Map<ArticleCategory,List<AbstractMention<?>>> map = mentionsByCategory.get(count);
		List<AbstractMention<?>> result = map.get(category);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// COUNTS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Total numbers of mentions */
	protected Map<String,Integer> countsAll = null;
	/** Numbers of mentions by entity type */
	protected Map<String,Map<EntityType,Integer>> countsByType = null;
	/** Numbers of mentions by article category */
	protected Map<String,Map<ArticleCategory,Integer>> countsByCategory = null;

	/**
	 * Initializes the lists of counts.
	 */
	private void initializeCounts()
	{	countsAll = new HashMap<String,Integer>();
		countsByType = new HashMap<String,Map<EntityType,Integer>>();
		countsByCategory = new HashMap<String,Map<ArticleCategory,Integer>>();
	
		for(String count: getCountNames())
		{	// overall 
			countsAll.put(count,0);
			
			// by type
			Map<EntityType,Integer> countsByTypeMap = new HashMap<EntityType,Integer>();
			for(EntityType type: types)
				countsByTypeMap.put(type, 0);
			countsByType.put(count, countsByTypeMap);
			
			// by category
			Map<ArticleCategory,Integer> countsByCategoryMap = new HashMap<ArticleCategory,Integer>();
			countsByCategory.put(count, countsByCategoryMap);
		}
	}
	
	/**
	 * Returns the list of the counts
	 * supported by this class.
	 * 
	 * @return
	 * 		List of strings representing count names.
	 */
	public abstract List<String> getCountNames();
	
	/**
	 * Get the total value for
	 * the specified count.
	 * 
	 * @param count
	 * 		Count required by the user.
	 * @return
	 * 		Associated total value.
	 */
	public int getCountAll(String count)
	{	int result = countsAll.get(count);
		return result;
	}
	
	/**
	 * Get the value for
	 * the specified count,
	 * when considering only
	 * the specified mention type.
	 * 
	 * @param count
	 * 		Count required by the user.
	 * @param type
	 * 		Type of interest.
	 * @return
	 * 		Associated value.
	 */
	public int getCountByType(String count, EntityType type)
	{	Map<EntityType,Integer> map = countsByType.get(count);
		int result = map.get(type);
		return result;
	}

	/**
	 * Get the value for
	 * the specified count,
	 * when considering only
	 * the specified article category.
	 * 
	 * @param count
	 * 		Count required by the user.
	 * @param category
	 * 		Category of interest.
	 * @return
	 * 		Associated value.
	 */
	public int getCountByCategory(String count, ArticleCategory category)
	{	Map<ArticleCategory,Integer> map = countsByCategory.get(count);
		int result = map.get(category);
		return result;
	}

	/**
	 * Processes all counts for the specified
	 * estimated mentions (detected by a NER
	 * tool), relatively to reference mentions 
	 * (usually manual annotations).
	 * 
	 * @param referenceOrig
	 * 		List of mentions of reference.
	 * @param estimationOrig
	 * 		List of mentions detected by the recognizer.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	protected abstract void processCounts(Mentions referenceOrig, Mentions estimationOrig, List<ArticleCategory> categories);
	
	/**
	 * Updates the counts of this measure, by adding the counts from the
	 * specified measure.
	 * <br/>
	 * This method is used when aggregating article-related results to get 
	 * the performances over a collection of articles.
	 * <br/>
	 * The specified object must use the exact same count names, score 
	 * names, entity types. It is preferable to use an instance of the 
	 * exact same class.
	 * 
	 * @param result 
	 * 		Values to add to this object.
	 */
	public void updateCounts(AbstractRecognitionMeasure result)
	{	List<ArticleCategory> categories = result.getCategories();
		for(String count: getCountNames())
		{	// total counts
			int countAll0 = countsAll.get(count);
			int countAll1 = result.getCountAll(count);
			countAll0 = countAll0 + countAll1;
			countsAll.put(count,countAll0);
			
			// counts by type
			for(EntityType type: types)
			{	Map<EntityType,Integer> map = countsByType.get(count);
				int countType0 = map.get(type);
				int countType1 = result.getCountByType(count,type);
				countType0 = countType0 + countType1;
				map.put(type, countType0);
			}
			
			// counts by category
			for(ArticleCategory category: categories)
			{	Map<ArticleCategory,Integer> map = countsByCategory.get(count);
				Integer countCat0 = map.get(category);
				if(countCat0==null)
					countCat0 = 0;
				int countCat1 = result.getCountByCategory(count,category);
				countCat0 = countCat0 + countCat1;
				map.put(category, countCat0);
			}
		}
	}
	
	/**
	 * After the mentions have been processed,
	 * this method processes the counts.
	 * 
	 * @param categories
	 * 		Categories of the considered article.
	 */
	protected void updateCounts(List<ArticleCategory> categories)
	{	for(String count: getCountNames())
		{	// total counts
			{	List<AbstractMention<?>> list = mentionsAll.get(count);
				int value = list.size();
				countsAll.put(count, value);
			}
			
			// counts by type
			{	Map<EntityType, Integer> countsMap = countsByType.get(count);
				Map<EntityType, List<AbstractMention<?>>> mentionsMap = mentionsByType.get(count);
				for(EntityType type: types)
				{	List<AbstractMention<?>> list = mentionsMap.get(type);
					int value = list.size();
					countsMap.put(type, value);
				}
			}
			
			// counts by category
			{	Map<ArticleCategory, Integer> countsMap = countsByCategory.get(count);
				Map<ArticleCategory, List<AbstractMention<?>>> mentionsMap = mentionsByCategory.get(count);
				for(ArticleCategory category: categories)
				{	List<AbstractMention<?>> list = mentionsMap.get(category);
					int value = list.size();
					countsMap.put(category, value);
				}
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// SCORES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Total scores */
	protected Map<String,Float> scoresAll = null;
	/** Scores by entity type */
	protected Map<String,Map<EntityType,Float>> scoresByType = null;
	/** Scores by article category */
	protected Map<String,Map<ArticleCategory,Float>> scoresByCategory = null;
	
//	protected void initScores()
//	{	scoresAll = new HashMap<String,Integer>();
//		scoresByType = new HashMap<String,Map<EntityType,Integer>>();
//	
//		for(String measure: getScoreNames())
//		{	scoresAll.put(measure,0);
//			Map<EntityType,Integer> scoresByTypeMap = new HashMap<EntityType,Integer>();
//			for(EntityType type: types)
//				scoresByTypeMap.put(type, 0);
//			scoresByType.put(measure, scoresByTypeMap);
//		}
//	}
//	

	@Override
	public float getScoreAll(String score)
	{	float result = scoresAll.get(score);
		return result;
	}
	
	@Override
	public float getScoreByType(String score, EntityType type)
	{	Map<EntityType,Float> map = scoresByType.get(score);
		float result = map.get(type);
		return result;
	}

	@Override
	public float getScoreByCategory(String score, ArticleCategory category)
	{	Map<ArticleCategory,Float> map = scoresByCategory.get(score);
Float temp = map.get(category);
if(temp==null)
	temp = 0f;
return temp;
//		float result = map.get(category);
//		return result;
	}

	/**
	 * Uses the current counts to
	 * process all score values.
	 */
	private void processScores()
	{	
		// overall values
		processScoresAll();
		
		// values by type
		processScoresByType();
		
		// values by category
		processScoresByCategory();
	}
	
	/**
	 * Uses the current counts to
	 * process all total score values.
	 */
	private void processScoresAll()
	{	scoresAll = new HashMap<String, Float>();
		processScores(countsAll, scoresAll);
	}	
	
	/**
	 * Uses the current counts to
	 * process score values by type.
	 */
	private void processScoresByType()
	{	scoresByType = new HashMap<String, Map<EntityType,Float>>();
		for(EntityType type: types)
		{	// get the appropriate maps
			Map<String, Float> scores = new HashMap<String, Float>();
			Map<String, Integer> counts = new HashMap<String, Integer>();
			for(String c: getCountNames())
			{	Map<EntityType, Integer> map = countsByType.get(c);
				int value = map.get(type);
				counts.put(c,value);
			}
			
			// process scores
			processScores(counts, scores);
			
			// update maps
			for(String s: getScoreNames())
			{	Map<EntityType, Float> map = scoresByType.get(s);
				if(map==null)
				{	map = new HashMap<EntityType, Float>();
					scoresByType.put(s,map);
				}
				float value = scores.get(s);
				map.put(type,value);
			}
		}
	}
	
	/**
	 * Uses the current counts to
	 * process score values by category.
	 */
	private void processScoresByCategory()
	{	List<ArticleCategory> categories = getCategories();
		scoresByCategory = new HashMap<String, Map<ArticleCategory,Float>>();
		for(ArticleCategory category: categories)
		{	// get the appropriate maps
			Map<String, Float> scores = new HashMap<String, Float>();
			Map<String, Integer> counts = new HashMap<String, Integer>();
			for(String c: getCountNames())
			{	Map<ArticleCategory, Integer> map = countsByCategory.get(c);
				int value = map.get(category);
				counts.put(c,value);
			}
			
			// process scores
			processScores(counts, scores);
			
			// update maps
			for(String s: getScoreNames())
			{	Map<ArticleCategory, Float> map = scoresByCategory.get(s);
				if(map==null)
				{	map = new HashMap<ArticleCategory, Float>();
					scoresByCategory.put(s,map);
				}
				float value = scores.get(s);
				map.put(category,value);
			}
		}
	}
	
	/**
	 * Processes the scores for the
	 * specified counts, and complete
	 * the existing scores list with
	 * the resulting value.
	 * 
	 * @param counts
	 * 		Count to use for score processing.
	 * @param scores
	 * 		Structure meant to store the resulting scores.
	 */
	protected abstract void processScores(Map<String, Integer> counts, Map<String, Float> scores);

	/////////////////////////////////////////////////////////////////
	// RECORD			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used when reading/writing results in files */
	private static final String OVERALL_STRING = "Overall";
	/** String used when reading/writing results in files */
	private static final String COUNTS_STRING = "Counts";
	/** String used when reading/writing results in files */
	private static final String SCORES_STRING = "Scores";
	
	@Override
	public void writeNumbers(File folder, String dataName) throws FileNotFoundException, UnsupportedEncodingException
	{	String filePath = folder.getPath() + File.separator + getFileName();
		PrintWriter pw = FileTools.openTextFileWrite(filePath,"UTF-8");
		
		// write header
		pw.println("# tool evaluated: "+recognizer.getRecognizerFolder());
		pw.println("# evaluation data: "+dataName);
		pw.println("# date of evaluation: "+TimeFormatting.formatCurrentFileTime());
		pw.println("# evaluation measure: "+getName());
		pw.println();
		
		// write data
		writeValues(pw,getCountNames(),COUNTS_STRING,countsAll,countsByType,countsByCategory);
		pw.println();
		processScores();
		writeValues(pw,getScoreNames(),SCORES_STRING,scoresAll,scoresByType,scoresByCategory);
		
		pw.close();
	}
	
	/**
	 * Secondary method used by {@link #writeNumbers(File,String)}.
	 * 
	 * @param <T>
	 * 		Type of the values to be writen.
	 * @param pw
	 * 		Print writer used for writing.
	 * @param names
	 * 		Names of the counts/scores to write.
	 * @param header
	 * 		String representing count or score.
	 * @param mapAll
	 * 		Total values.
	 * @param mapByType
	 * 		Values by type.
	 * @param mapByCategory
	 * 		Values by category.
	 */
	private <T extends Number> void writeValues(PrintWriter pw, List<String> names, String header, Map<String, T> mapAll, Map<String, Map<EntityType, T>> mapByType, Map<String, Map<ArticleCategory, T>> mapByCategory)
	{	StringBuffer line;
		List<ArticleCategory> categories = getCategories();
	
		// init lines
		List<StringBuffer> lines = new ArrayList<StringBuffer>();
		lines.add(new StringBuffer(header));
		lines.add(new StringBuffer("# Overall "+header+" ##########"));
		lines.add(new StringBuffer(OVERALL_STRING));
		lines.add(new StringBuffer("# "+header+" by type ##########"));
		for(EntityType type: types)
			lines.add(new StringBuffer(type.toString()));
		lines.add(new StringBuffer("# "+header+" by category ######"));
		for(ArticleCategory category: categories)
			lines.add(new StringBuffer(category.toString()));
		lines.add(new StringBuffer("# "+header+" done #############"));
			
		// complete lines
		for(String name: names)
		{	Iterator<StringBuffer> it = lines.iterator();
			line = it.next(); 
				line.append("\t" + name);
			line = it.next();
			line = it.next();
			{	T value = mapAll.get(name);
				line.append("\t" + value);
			}
			line = it.next();
			for(EntityType type: types)
			{	line = it.next();
				Map<EntityType, T> map = mapByType.get(name);
				T value = map.get(type);
				line.append("\t" + value);
			}
			line = it.next();
			for(ArticleCategory category: categories)
			{	line = it.next();
				Map<ArticleCategory, T> map = mapByCategory.get(name);
				T value = map.get(category);
				line.append("\t" + value);
			}
			line = it.next(); 
		}
		
		// write lines
		for(StringBuffer l: lines)
			pw.println(l.toString());
	}
	
	/**
	 * Reads the scores and counts stored
	 * in a file, and build a new measure object.
	 * 
	 * @param folder
	 * 		Where to write the file.
	 * @param recognizer 
	 * 		Recognizer concerned by these results.
	 * @return 
	 * 		Measure object containing the results.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while writing the file.
	 */
	public AbstractRecognitionMeasure readNumbers(File folder, InterfaceRecognizer recognizer) throws FileNotFoundException
	{	AbstractRecognitionMeasure result = build(recognizer);

		// open file
		String filePath = folder.getPath() + File.separator + getFileName();
		try
		{	Scanner scanner = FileTools.openTextFileRead(filePath,"UTF-8");
			
			// retrieve data
			@SuppressWarnings("unused")
			String line;
			line = scanner.nextLine();
			line = scanner.nextLine();
			line = scanner.nextLine();
			line = scanner.nextLine();
			line = scanner.nextLine();
			readCounts(scanner, getCountNames(), result.countsAll, result.countsByType, result.countsByCategory);
			scanner.nextLine();
			result.scoresAll = new HashMap<String,Float>();
			result.scoresByType = new HashMap<String,Map<EntityType,Float>>();
			result.scoresByCategory = new HashMap<String,Map<ArticleCategory,Float>>();
			for(String score: getScoreNames())
			{	Map<EntityType,Float> scoresByTypeMap = new HashMap<EntityType,Float>();
				result.scoresByType.put(score,scoresByTypeMap);
				Map<ArticleCategory,Float> scoresByCategoryMap = new HashMap<ArticleCategory,Float>();
				result.scoresByCategory.put(score, scoresByCategoryMap);
			}
			readScores(scanner, getScoreNames(), result.scoresAll, result.scoresByType, result.scoresByCategory);
			
			scanner.close();
		}
		catch (UnsupportedEncodingException e)
		{	e.printStackTrace();
		}
		
		return result;
	}

	/**
	 * Secondary method used by {@link #readNumbers}.
	 * <br/>
	 * Unlike for writing, it was not possible to
	 * define a generic function. This one deals
	 * with integers (counts) only.
	 * 
	 * @param scanner
	 * 		Scanner used for reading.
	 * @param names
	 * 		Names of the counts to read.
	 * @param mapAll
	 * 		Total values.
	 * @param mapByType
	 * 		Values by type.
	 * @param mapByCategory
	 * 		Values by category.
	 */
	private void readCounts(Scanner scanner, List<String> names, Map<String, Integer> mapAll, Map<String, Map<EntityType, Integer>> mapByType, Map<String, Map<ArticleCategory, Integer>> mapByCategory)
	{	List<ArticleCategory> categories = new ArrayList<ArticleCategory>();
		types.clear();
		List<String> lines = new ArrayList<String>();
		String line;
		
		// read lines
		line = scanner.nextLine();
		line = scanner.nextLine();
		line = scanner.nextLine();
		lines.add(line);
		line = scanner.nextLine();
		line = scanner.nextLine();
		do
		{	lines.add(line);
			String temp[] = line.split("\\t");
			EntityType type = EntityType.valueOf(temp[0]);
			types.add(type);
			line = scanner.nextLine();
		}
		while(!line.startsWith("#"));
		line = scanner.nextLine();
		do
		{	lines.add(line);
			String temp[] = line.split("\\t");
			ArticleCategory cat = ArticleCategory.valueOf(temp[0].toUpperCase(Locale.ENGLISH));
			categories.add(cat);
			line = scanner.nextLine();
		}
		while(!line.startsWith("#"));
		
		// analyze lines
		for(int i=0;i<names.size();i++)
		{	String name = names.get(i);
			Iterator<String> it = lines.iterator();
			{	line = it.next(); 
				String temp[] = line.split("\\t");
				int value = Integer.parseInt(temp[i+1]); 
				mapAll.put(name,value);
			}
			for(EntityType type: types)
			{	line = it.next();
				String temp[] = line.split("\\t");
				int value = Integer.parseInt(temp[i+1]); 
				Map<EntityType, Integer> map = mapByType.get(name);
				map.put(type,value);
			}
			for(ArticleCategory category: categories)
			{	line = it.next();
				String temp[] = line.split("\\t");
				int value = Integer.parseInt(temp[i+1]); 
				Map<ArticleCategory, Integer> map = mapByCategory.get(name);
				map.put(category,value);
			}
		}
	}
	
	/**
	 * Secondary method used by {@link #readNumbers}.
	 * <br/>
	 * Unlike for writing, it was not possible to
	 * define a generic function. This one deals
	 * with floats (scores) only.
	 * 
	 * @param scanner
	 * 		Scanner used for reading.
	 * @param names
	 * 		Names of the scores to read.
	 * @param mapAll
	 * 		Total values.
	 * @param mapByType
	 * 		Values by type.
	 * @param mapByCategory
	 * 		Values by category.
	 */
	private void readScores(Scanner scanner, List<String> names, Map<String, Float> mapAll, Map<String, Map<EntityType, Float>> mapByType, Map<String, Map<ArticleCategory, Float>> mapByCategory)
	{	List<ArticleCategory> categories = new ArrayList<ArticleCategory>();
		types.clear();
		List<String> lines = new ArrayList<String>();
		String line;
		
		// read lines
		line = scanner.nextLine();
		line = scanner.nextLine();
		line = scanner.nextLine();
		lines.add(line);
		line = scanner.nextLine();
		line = scanner.nextLine();
		do
		{	lines.add(line);
			String temp[] = line.split("\\t");
			EntityType type = EntityType.valueOf(temp[0]);
			types.add(type);
			line = scanner.nextLine();
		}
		while(!line.startsWith("#"));
		line = scanner.nextLine();
		do
		{	lines.add(line);
			String temp[] = line.split("\\t");
			ArticleCategory cat = ArticleCategory.valueOf(temp[0].toUpperCase(Locale.ENGLISH));
			categories.add(cat);
			line = scanner.nextLine();
		}
		while(!line.startsWith("#"));
		
		// analyze lines
		for(int i=0;i<names.size();i++)
		{	String name = names.get(i);
			Iterator<String> it = lines.iterator();
			{	line = it.next(); 
				String temp[] = line.split("\\t");
				float value = Float.parseFloat(temp[i+1]); 
				mapAll.put(name,value);
			}
			for(EntityType type: types)
			{	line = it.next();
				String temp[] = line.split("\\t");
				float value = Float.parseFloat(temp[i+1]); 
				Map<EntityType, Float> map = mapByType.get(name);
				map.put(type,value);
			}
			for(ArticleCategory category: categories)
			{	line = it.next();
				String temp[] = line.split("\\t");
				float value = Float.parseFloat(temp[i+1]); 
				Map<ArticleCategory, Float> map = mapByCategory.get(name);
				map.put(category,value);
			}
		}
	}

	/////////////////////////////////////////////////////////////////
	// STRING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = getName();
		return result;
	}
}
