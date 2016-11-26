package fr.univavignon.nerwip.processing.combiner;

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

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleCategory;
import fr.univavignon.nerwip.evaluation.AbstractEvaluator;
import fr.univavignon.nerwip.evaluation.AbstractMeasure;
import fr.univavignon.nerwip.processing.InterfaceProcessor;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class is used to handle weights associated to processors
 * in certain combiners.
 * 
 * @param <T> 
 * 		Type of processing concerned by the weights: recognizer, 
 * 		resolver or linker.
 * 
 * @author Vincent Labatut
 */
public class VoteWeights<T extends InterfaceProcessor>
{	
	/**
	 * Builds a new structure dedicated to storing
	 * the vote weights of the specified recognizers.
	 * 
	 * @param recognizers
	 * 		Recognizers whose weights are stored in this object.
	 */
	public VoteWeights(List<T> recognizers)
	{	this.recognizers.addAll(recognizers);
		for(T recognizer: recognizers)
		{	Map<String,Map<ArticleCategory,Float>> map = new HashMap<String, Map<ArticleCategory,Float>>();
			data.put(recognizer,map);
		}
	}
	
	/**
	 * Builds a new VoteWeights object containing
	 * only uniform weights (all are 1).
	 * 
	 * @param recognizers
	 * 		Recognizers to be represented in the generated object.
	 * @return
	 * 		New VoteWeights instance with uniform weights.
	 */
	public static <U extends InterfaceProcessor> VoteWeights<U> buildUniformWeights(List<U> recognizers)
	{	logger.log("Builds uniform weights for recognizers "+recognizers.toString());
		logger.increaseOffset();
		
		VoteWeights<U> result = new VoteWeights<U>(recognizers);
		List<ArticleCategory> categories = Arrays.asList(ArticleCategory.values());
			
		for(U recognizer: recognizers)
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
	 * of the specified recognizers performance.
	 * 
	 * @param recognitionEvaluator
	 * 		Object used to perform the evaluation.
	 * @param names
	 * 		Names of the scores of interest.
	 * @param byCategory
	 * 		Whether the scores should be considered category-wise ({@code true}) or overall ({@code false}).
	 * @return
	 * 		New VoteWeights instance with the weights resulting from the evaluation.
	 */
	public static <U extends InterfaceProcessor, V extends  AbstractMeasure, W extends AbstractEvaluator<U,V>> VoteWeights<U> buildWeightsFromEvaluator(W recognitionEvaluator, List<String> names, boolean byCategory)
	{	logger.log("Initializes vote weights with evaluator "+recognitionEvaluator);
		logger.increaseOffset();
		
		List<U> recognizers = recognitionEvaluator.getRecognizers();
		VoteWeights<U> result = new VoteWeights<U>(recognizers);
		List<ArticleCategory> categories = Arrays.asList(ArticleCategory.values());
		Collections.sort(names);
		
		for(U recognizer: recognizers)
		{	logger.log("Processing recognizer "+recognizer);
			logger.increaseOffset();
			
			V measure = recognitionEvaluator.getMeasure(recognizer);
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
	private final Map<T,Map<String,Map<ArticleCategory,Float>>> data = new HashMap<T,Map<String,Map<ArticleCategory,Float>>>();
	/** List of recognizers (important to keep their original order) */
	private final List<T> recognizers = new ArrayList<T>();
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes the relative specified weight for the specified article
	 * and the specified category proportions, using the weight stored
	 * in this object, and for the specified recognizer.
	 * <br/>
	 * We basically look up for the category describing the article,
	 * and use the concerned category weights to modulate the concerned
	 * recognizer raw voting weights, leading to normalized voting weights. 
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
	public float processVotingWeight(Article article, T recognizer, String name, Map<ArticleCategory,Float> categoryWeights)
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
	 * it was created to describe the same recognizers
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
	 * @throws UnsupportedEncodingException
	 * 		Could not handle the encoding.
	 */
	public static <U extends InterfaceProcessor> VoteWeights<U> loadVoteWeights(String filePath, List<U> recognizers) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.log("Loading vote weights");
		logger.increaseOffset();
		
		VoteWeights<U> result = new VoteWeights<U>(recognizers);
		List<ArticleCategory> categories = new ArrayList<ArticleCategory>();
	
		Scanner scanner = FileTools.openTextFileRead(filePath, "UTF-8");
		
		// process each recognizer	
		for(U recognizer: recognizers)
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
	 * 		Could not handle the encoding.
	 */
	public void recordVoteWeights(String filePath) throws UnsupportedEncodingException, FileNotFoundException
	{	logger.log("Recording vote weights");
		logger.increaseOffset();
		
		PrintWriter writer = FileTools.openTextFileWrite(filePath, "UTF-8");

		// get category list
		TreeSet<ArticleCategory> categories = new TreeSet<ArticleCategory>();
		{	Map<String,Map<ArticleCategory,Float>> recMap = data.entrySet().iterator().next().getValue();
			Map<ArticleCategory,Float> mesMap = recMap.entrySet().iterator().next().getValue();
			categories.addAll(mesMap.keySet());
		}
		
		// write each recognizer
		for(T recognizer: recognizers)
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
