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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import fr.univavignon.nerwip.evaluation.AbstractEvaluator;
import fr.univavignon.nerwip.evaluation.AbstractMeasure;
import fr.univavignon.nerwip.processing.InterfaceProcessor;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

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
		{	Map<String,Float> map = new HashMap<String, Float>();
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
		float weight = 1f;
		
		for(U recognizer: recognizers)
		{	logger.log("Processing recognizer "+recognizer);
			Map<String,Float> map = result.data.get(recognizer);
			map.put(UNIFORM_NAME, weight);
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
	 * @return
	 * 		New VoteWeights instance with the weights resulting from the evaluation.
	 */
	public static <U extends InterfaceProcessor, V extends  AbstractMeasure, W extends AbstractEvaluator<U,V>> VoteWeights<U> buildWeightsFromEvaluator(W recognitionEvaluator, List<String> names)
	{	logger.log("Initializes vote weights with evaluator "+recognitionEvaluator);
		logger.increaseOffset();
		
		List<U> recognizers = recognitionEvaluator.getRecognizers();
		VoteWeights<U> result = new VoteWeights<U>(recognizers);
		Collections.sort(names);
		
		for(U recognizer: recognizers)
		{	logger.log("Processing recognizer "+recognizer);
			logger.increaseOffset();
			
			V measure = recognitionEvaluator.getMeasure(recognizer);
			Map<String,Float> map = result.data.get(recognizer);
			for(String name: names)
			{	float weight = measure.getScoreAll(name);
				map.put(name, weight);
				logger.log("Processing measure "+name+": "+weight);
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
	private final Map<T,Map<String,Float>> data = new HashMap<T,Map<String,Float>>();
	/** List of recognizers (important to keep their original order) */
	private final List<T> recognizers = new ArrayList<T>();
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes the relative specified weight for the specified article,
	 * using the weight stored in this object, and for the specified recognizer.
	 *  
	 * @param recognizer
	 * 		Recognizer concerned by the processing.
	 * @param name 
	 * 		Name of the voting weight.
	 * @return
	 * 		Voting weight resulting from the process.
	 */
	public float processVotingWeight(T recognizer, String name)
	{	Map<String,Float> map = data.get(recognizer);
		float result = map.get(name);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Initializes a new VoteWeights object by
	 * reading data in the specified file, assuming
	 * it was created to describe the same recognizers
	 * as those specified as parameters.
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
		Scanner scanner = FileTools.openTextFileRead(filePath, "UTF-8");
		
		// read the list of measure names
		List<String> measureNames = new ArrayList<String>();
		{	String line = scanner.nextLine();
			String temp[] = line.split("\t");
			for(String name: temp)
			{	name = name.trim();
				if(!name.isEmpty())
					measureNames.add(name);
			}
		}
		// process each recognizer	
		for(U recognizer: recognizers)
		{	logger.log("Processing recognizer "+recognizer);
			logger.increaseOffset();
			
			String line = scanner.nextLine();
			String temp[] = line.split("\t");
			
			// read recognizer name
			String nerName = temp[0];
			if(nerName.equals(recognizer.getName().toString()))
				logger.log("WARNING: found recognizer '"+nerName+"' where it should have been '"+recognizer+"'");
			Map<String,Float> map = result.data.get(recognizer);
			
			// read weights
			logger.log("Reading values: ");
			int i = 1;
			for(String name: measureNames)
			{	Float weight = Float.parseFloat(temp[i]);
				logger.log("Measure "+name+"="+weight);
				map.put(name,weight);
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
		
		// open file
		PrintWriter writer = FileTools.openTextFileWrite(filePath, "UTF-8");
		
		// get the list of measures and write it
		logger.log("Writing the measure names");
		TreeSet<String> names;
		{	Map<String,Float> map = data.values().iterator().next();
			names = new TreeSet<String>(map.keySet());
			for(String name: names)
				writer.print("\t"+name);
			writer.println();
		}
		
		// write the values of each recognizer
		for(T recognizer: recognizers)
		{	logger.log("Processing recognizer "+recognizer);
			logger.increaseOffset();
				// write recognizer name
				writer.print(recognizer.getName());
				
				// write weights
				Map<String,Float> map = data.get(recognizer);
				for(String name: names)
				{	float weight = map.get(name);
					writer.print("\t"+weight);
				}
				
				writer.println();
			logger.decreaseOffset();
		}
		
		writer.close();
		logger.decreaseOffset();
	}
}
