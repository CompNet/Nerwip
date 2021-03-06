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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleCategory;
import tr.edu.gsu.nerwip.evaluation.Evaluator;
import tr.edu.gsu.nerwip.evaluation.measure.AbstractMeasure;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class is used to handle weights associated to NER tools
 * in certain combiners.
 * 
 * @author Vincent Labatut
 */
public class VoteWeights
{	
	/**
	 * Builds a new structure dedicated to storing
	 * the vote weights of the specified NER tools.
	 * 
	 * @param recognizers
	 * 		NER tools whose weights are stored in this object.
	 */
	public VoteWeights(List<AbstractRecognizer> recognizers)
	{	this.recognizers.addAll(recognizers);
		for(AbstractRecognizer recognizer: recognizers)
		{	Map<String,Map<ArticleCategory,Float>> map = new HashMap<String, Map<ArticleCategory,Float>>();
			data.put(recognizer,map);
		}
	}
	
	/**
	 * Builds a new VoteWeights object containing
	 * only uniform weights (all are 1).
	 * 
	 * @param recognizers
	 * 		NER tools to be represented in the generated object.
	 * @return
	 * 		New VoteWeights instance with uniform weights.
	 */
	public static VoteWeights buildUniformWeights(List<AbstractRecognizer> recognizers)
	{	logger.log("Builds uniform weights for recognizers "+recognizers.toString());
		logger.increaseOffset();
		
		VoteWeights result = new VoteWeights(recognizers);
		List<ArticleCategory> categories = Arrays.asList(ArticleCategory.values());
			
		for(AbstractRecognizer recognizer: recognizers)
		{	logger.log("Processing recognizer "+recognizer);
			Map<String,Map<ArticleCategory,Float>> recMap = result.data.get(recognizer);
			Map<ArticleCategory,Float> mesMap = new HashMap<ArticleCategory, Float>();
			recMap.put(UNIFORM_NAME, mesMap);
			for(ArticleCategory category: categories)
			{	float weight = 1f;
				mesMap.put(category, weight);
			}
		}
		
		logger.increaseOffset();
		return result;
	}
	
	/**
	 * Builds a new VoteWeights object based on the evaluation
	 * of the specified NER tools performance.
	 * 
	 * @param evaluator
	 * 		Object used to perform the evaluation.
	 * @param names
	 * 		Names of the scores of interest.
	 * @param byCategory
	 * 		Whether the scores should be considered category-wise ({@code true}) or overall ({@code false}).
	 * @return
	 * 		New VoteWeights instance with the weights resulting from the evaluation.
	 */
	public static VoteWeights buildWeightsFromEvaluator(Evaluator evaluator, List<String> names, boolean byCategory)
	{	logger.log("Initializes vote weights with evaluator "+evaluator);
		logger.increaseOffset();
		
		List<AbstractRecognizer> recognizers = evaluator.getRecognizers();
		VoteWeights result = new VoteWeights(recognizers);
		List<ArticleCategory> categories = Arrays.asList(ArticleCategory.values());
		Collections.sort(names);
		
		for(AbstractRecognizer recognizer: recognizers)
		{	logger.log("Processing recognizer "+recognizer);
			logger.increaseOffset();
			
			AbstractMeasure measure = evaluator.getMeasure(recognizer);
			Map<String,Map<ArticleCategory,Float>> recMap = result.data.get(recognizer);
			for(String name: names)
			{	logger.log("Processing measure "+name+":");
				logger.increaseOffset();

				String msg = "";
				Map<ArticleCategory,Float> mesMap = new HashMap<ArticleCategory, Float>();
				recMap.put(name, mesMap);
				for(ArticleCategory category: categories)
				{	float weight;
					if(byCategory)
						weight = measure.getScoreByCategory(name, category);
					else
						weight = measure.getScoreAll(name);
					mesMap.put(category, weight);
					msg = " " + msg + category + "=" + weight;
				}
				
				logger.log(msg);
				logger.decreaseOffset();
			}
			
			logger.decreaseOffset();
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// DATA				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name used for uniform weights */
	private final static String UNIFORM_NAME = "Uniform";
	/** Maps containing all the weights */
	private final Map<AbstractRecognizer,Map<String,Map<ArticleCategory,Float>>> data = new HashMap<AbstractRecognizer,Map<String,Map<ArticleCategory,Float>>>();
	/** List of NER tools (important to keep their original order) */
	private final List<AbstractRecognizer> recognizers = new ArrayList<AbstractRecognizer>();
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes the relative specified weight for the specified article
	 * and the specified category proportions, using the weight stored
	 * in this object, and for the specified NER tool.
	 * <br/>
	 * We basically look up for the category describing the article,
	 * and use the concerned category weights to modulate the concerned
	 * NER tool raw voting weights, leading to normalized voting weights. 
	 *  
	 * @param article
	 * 		Article to be considered.
	 * @param recognizer
	 * 		Recognizer concerned by the processing.
	 * @param name 
	 * 		Name of the voting weight.
	 * @param categoryWeights 
	 * 		Previously processed category relative weights for the specified article.
	 * @return
	 * 		Voting weight resulting from the process.
	 */
	public float processVotingWeight(Article article, AbstractRecognizer recognizer, String name, Map<ArticleCategory,Float> categoryWeights)
	{	float result = 0;
		List<ArticleCategory> categories = article.getCategories();
		Map<String,Map<ArticleCategory,Float>> recMap = data.get(recognizer);
		Map<ArticleCategory, Float> mesMap = recMap.get(name);
		
		for(ArticleCategory cat: categories)
		{	// normalize weight
			float modifier = categoryWeights.get(cat);
			float baseWeight = mesMap.get(cat);
			float normWeight = modifier*baseWeight;
			
			result = result + normWeight;
		}
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Initializes a new VoteWeights object by
	 * reading data in the specified file, assuming
	 * it was created to describe the same NER tools
	 * than those specified as parameters.
	 * 
	 * @param filePath
	 * 		Complete path of the file to read.
	 * @param recognizers
	 * 		List of recognizers in the appropriate order (same as when the weights were recorded). 
	 * @return
	 * 		A new VoteWeights object.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 */
	public static VoteWeights loadVoteWeights(String filePath, List<AbstractRecognizer> recognizers) throws FileNotFoundException
	{	logger.log("Loading vote weights");
		logger.increaseOffset();
		
		VoteWeights result = new VoteWeights(recognizers);
		List<ArticleCategory> categories = new ArrayList<ArticleCategory>();
	
		Scanner scanner = FileTools.openTextFileRead(filePath);
		
		// process each NER tool
		for(AbstractRecognizer recognizer: recognizers)
		{	logger.log("Processing recognizer "+recognizer);
			logger.increaseOffset();
			
			Map<String,Map<ArticleCategory,Float>> recMap = result.data.get(recognizer);
			
			// read category ordered list
			String line = scanner.nextLine();
			{	String temp[] = line.split("\t");
				String nerName = temp[0];
				if(nerName.equals(recognizer.getName()))
					logger.log("WARNING: just recognizer '"+nerName+"' where it should have been '"+recognizer+"'");
				for(int i=1;i<temp.length;i++)
				{	String catStr = temp[i];
					ArticleCategory category = ArticleCategory.valueOf(catStr);
					categories.add(category);
				}
			}
			
			// read weights
			logger.log("Reading values: ");
			while(!line.isEmpty())
			{	// setup the map
				line = scanner.nextLine();
				String temp[] = line.split("\t");
				String name = temp[0];
				Map<ArticleCategory,Float> mesMap = new HashMap<ArticleCategory, Float>();
				recMap.put(name, mesMap);
				
				logger.log("Processing measure "+name);
				logger.increaseOffset();
				String msg = "Read values:";
				
				// add all needed weights
				for(int i=1;i<temp.length;i++)
				{	ArticleCategory category = categories.get(i-1);
					Float proportion = Float.parseFloat(temp[1]);
					mesMap.put(category, proportion);
					msg = msg + " " + category + "=" + proportion;
				}
				
				logger.log(msg);
				logger.decreaseOffset();
			}
			
			logger.decreaseOffset();
		}
		
		scanner.close();
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Records these voting weights in the specified file.
	 * 
	 * @param filePath
	 * 		Complete path of the file in which to write.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the file.
	 */
	public void recordVoteWeights(String filePath) throws UnsupportedEncodingException, FileNotFoundException
	{	logger.log("Recording vote weights");
		logger.increaseOffset();
		
		PrintWriter writer = FileTools.openTextFileWrite(filePath);

		// get category list
		TreeSet<ArticleCategory> categories = new TreeSet<ArticleCategory>();
		{	Map<String,Map<ArticleCategory,Float>> recMap = data.entrySet().iterator().next().getValue();
			Map<ArticleCategory,Float> mesMap = recMap.entrySet().iterator().next().getValue();
			categories.addAll(mesMap.keySet());
		}
		
		// write each NER tool
		for(AbstractRecognizer recognizer: recognizers)
		{	logger.log("Processing recognizer "+recognizer);
			logger.increaseOffset();
		
			Map<String,Map<ArticleCategory,Float>> recMap = data.get(recognizer);
			
			// write categories
			logger.log("Writing category names: "+categories.toString());
			writer.print(recognizer.getName());
			for(ArticleCategory category: categories)
				writer.print("\t"+category.toString());
			writer.println();
			
			// write weights
			TreeSet<String> names = new TreeSet<String>(recMap.keySet());
			for(String name: names)
			{	logger.log("Processing measure "+name);
				logger.increaseOffset();
				String msg = "Read values:";
				
				writer.print(name);
				Map<ArticleCategory,Float> mesMap = recMap.get(name);
				for(ArticleCategory category: categories)
				{	float weight = mesMap.get(category);
					writer.print("\t"+weight);
					msg = msg + " " + category + "=" + weight;
				}
				writer.println();

				logger.log(msg);
				logger.decreaseOffset();
			}
			
			writer.println();
			logger.decreaseOffset();
		}
		
		writer.close();
		logger.decreaseOffset();
	}
}
