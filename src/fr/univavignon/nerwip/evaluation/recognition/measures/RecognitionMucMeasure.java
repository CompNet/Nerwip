package fr.univavignon.nerwip.evaluation.recognition.measures;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.tools.files.FileNames;

/**
 * This class implements the measures used in
 * the MUC conference series (Message Understanding Conference),
 * as described in <a href="http://nlp.cs.nyu.edu/sekine/papers/li07.pdf">
 * A survey of named entity recognition and classification<a>,
 * D. Nadeau & S. Sekinex, Lingvisticae Investigationes, 30:3-26, 2007.
 *  
 * @author Vincent Labatut
 */
public class RecognitionMucMeasure extends AbstractRecognitionMeasure
{	
	/**
	 * Builds a new instance of this measure,
	 * for the specified recognizer. This
	 * constructor is used when loading results
	 * from a file.
	 * 
	 * @param recognizer
	 * 		Concerned recognizer.
	 */
	public RecognitionMucMeasure(InterfaceRecognizer recognizer)
	{	super(recognizer);
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
	public RecognitionMucMeasure(InterfaceRecognizer recognizer, List<EntityType> types)
	{	super(recognizer,types);
	}

	/**
	 * Builds a new instance of this measure,
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
	 */
	public RecognitionMucMeasure(InterfaceRecognizer recognizer, List<EntityType> types, Mentions reference, Mentions estimation)
	{	super(recognizer,types,reference,estimation);
	}	
	
	@Override
	public RecognitionMucMeasure build(InterfaceRecognizer recognizer)
	{	RecognitionMucMeasure result = new RecognitionMucMeasure(recognizer);
		return result;
	}
	
	@Override
	public AbstractRecognitionMeasure build(InterfaceRecognizer recognizer, List<EntityType> types)
	{	RecognitionMucMeasure result = new RecognitionMucMeasure(recognizer, types);
		return result;
	}
	
	@Override
	public RecognitionMucMeasure build(InterfaceRecognizer recognizer, List<EntityType> types, Mentions reference, Mentions estimation)
	{	RecognitionMucMeasure result = new RecognitionMucMeasure(recognizer,types,reference,estimation);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Conventional name of the measure */
	private final static String NAME = "MUC";
	
	@Override
	public String getName()
	{	return NAME;
	}
	
	/////////////////////////////////////////////////////////////////
	// COUNTS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Perfect spatial matches or correct type */
	public static final String COUNT_CORRECT = "Correct";
	/** Twice the number of detected mentions */
	public static final String COUNT_ACTUAL = "Actual";
	/** Twice the number of mentions in the reference */
	public static final String COUNT_POSSIBLE = "Possible";

	/** Names of the supported counts */
	private static final List<String> COUNTS = Arrays.asList(
		COUNT_CORRECT,
		COUNT_ACTUAL,
		COUNT_POSSIBLE
	);
	
	@Override
	public List<String> getCountNames()
	{	return COUNTS;
	}

	@Override
	public void processCounts(Mentions referenceOrig, Mentions estimationOrig)
	{	// copy mention lists (those are going to be modified)
		List<AbstractMention<?>> reference = new ArrayList<AbstractMention<?>>(referenceOrig.getMentions());
		List<AbstractMention<?>> estimation = new ArrayList<AbstractMention<?>>(estimationOrig.getMentions());
		
		// remove the mentions whose type is not in the type list
		cleanMentions(reference);
		cleanMentions(estimation);
		
		// look for the different cases
		processSpatialExactMatches(reference, estimation);
		processSpatialPartialMatches(reference, estimation);
		processSpatialFalseAlarms(estimation);
		processSpatialOmissions(reference);
		
		// update counts
		updateCounts();
	}
	
	/**
	 * Changes appropriately all the required structures in order to count the specified 
	 * mention, for the specified type, in the specified count type.
	 * 
	 * @param mention
	 * 		Mention to count.
	 * @param type
	 * 		Type associated to the count.
	 * @param count
	 * 		Type of count.
	 */
	private void addToStructures(AbstractMention<?> mention, EntityType type, String count)
	{	List<AbstractMention<?>> listAll = mentionsAll.get(count);
		listAll.add(mention);
		
		Map<EntityType,List<AbstractMention<?>>> mapByType = mentionsByType.get(count);
		List<AbstractMention<?>> listByType = mapByType.get(type);
		listByType.add(mention);
	}
	
	/**
	 * Changes appropriately all the required structures in order to count the specified 
	 * mention, for the specified types, in the specified count type.
	 * 
	 * @param mention
	 * 		Mention to count.
	 * @param type1
	 * 		Type associated to the count.
	 * @param type2
	 * 		Other type associated to the count.
	 * @param count
	 * 		Type of count.
	 */
	private void addToStructures(AbstractMention<?> mention, EntityType type1, EntityType type2, String count)
	{	List<AbstractMention<?>> listAll = mentionsAll.get(count);
		listAll.add(mention);
		
		Map<EntityType,List<AbstractMention<?>>> mapByType = mentionsByType.get(count);
		List<AbstractMention<?>> listByType;
		listByType = mapByType.get(type1);
		listByType.add(mention);
		if(type1!=type2)
		{	listByType = mapByType.get(type2);
			listByType.add(mention);
		}
	}
	
	/**
	 * Process the full match mentions.
	 * 
	 * @param reference
	 * 		List of the mentions of reference.
	 * @param estimation
	 * 		List of the mentions detected by the recognizer.
	 */
	private void processSpatialExactMatches(List<AbstractMention<?>> reference, List<AbstractMention<?>> estimation)
	{	Iterator<AbstractMention<?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractMention<?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			Iterator<AbstractMention<?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractMention<?> est = itEst.next();
				EntityType estType = est.getType();
				if(ref.hasSamePosition(est))
				{	found = true;
					itRef.remove();
					itEst.remove();
					
					// update spatial evaluation
					addToStructures(ref, refType, estType, COUNT_CORRECT);
					addToStructures(est, refType, estType, COUNT_ACTUAL);
					addToStructures(ref, refType, estType, COUNT_POSSIBLE);
					
					// update typical evaluation
					if(refType==estType)
						addToStructures(ref, refType, COUNT_CORRECT);
					addToStructures(est, estType, COUNT_ACTUAL);
					addToStructures(ref, refType, COUNT_POSSIBLE);
				}
			}
		}
	}
	
	/**
	 * Process the partial match mentions.
	 * 
	 * @param reference
	 * 		List of the mentions of reference.
	 * @param estimation
	 * 		List of the mentions detected by the recognizer.
	 */
	private void processSpatialPartialMatches(List<AbstractMention<?>> reference, List<AbstractMention<?>> estimation)
	{	Iterator<AbstractMention<?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractMention<?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			Iterator<AbstractMention<?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractMention<?> est = itEst.next();
				EntityType estType = est.getType();
				if(ref.overlapsWith(est))
				{	found = true;
					itRef.remove();
					itEst.remove();
					
					// update spatial evaluation
					addToStructures(est, estType, COUNT_ACTUAL);
					addToStructures(ref, refType, COUNT_POSSIBLE);
					
					// update typical evaluation
					if(refType==estType)
						addToStructures(ref, refType, COUNT_CORRECT);
					addToStructures(est, estType, COUNT_ACTUAL);
					addToStructures(ref, refType, COUNT_POSSIBLE);
				}
			}
		}
	}
	
	/**
	 * Process the wrong hit mentions.
	 * 
	 * @param estimation
	 * 		List of the mentions detected by the recognizer.
	 */
	private void processSpatialFalseAlarms(List<AbstractMention<?>> estimation)
	{	for(AbstractMention<?> est: estimation)
		{	EntityType estType = est.getType();
	
			// update spatial evaluation
			addToStructures(est, estType, COUNT_ACTUAL);
				
			// update typical evaluation
			addToStructures(est, estType, COUNT_ACTUAL);
		}
	}
	
	/**
	 * Process the complete miss mentions.
	 * 
	 * @param reference
	 * 		List of the mentions of reference.
	 */
	private void processSpatialOmissions(List<AbstractMention<?>> reference)
	{	for(AbstractMention<?> ref: reference)
		{	EntityType refType = ref.getType();
	
			// update spatial evaluation
			addToStructures(ref, refType, COUNT_POSSIBLE);
				
			// update typical evaluation
			addToStructures(ref, refType, COUNT_POSSIBLE);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// SCORES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Precision (mixes spatial and typical aspects) */
	public static final String SCORE_PRECISION = "Precision";
	/** RECALL (mixes spatial and typical aspects) */
	public static final String SCORE_RECALL = "Recall";
	/** F-Measure: F-measure processed using the type counts */
	public static final String SCORE_FMEASURE = "F-Measure";
	/** Names of the supported scores */
	private static final List<String> SCORES = Arrays.asList(
		SCORE_PRECISION,
		SCORE_RECALL,
		SCORE_FMEASURE
	);

	@Override
	public List<String> getScoreNames()
	{	return SCORES;
	}
	
	@Override
	protected void processScores(Map<String, Integer> counts, Map<String, Float> scores)
	{	float correct = counts.get(COUNT_CORRECT);
		float actual = counts.get(COUNT_ACTUAL);
		float possible = counts.get(COUNT_POSSIBLE);
			
		// precision (FP)
		float pre = correct/actual;
		scores.put(SCORE_PRECISION, pre);
		if(pre>1)
			throw new IllegalArgumentException("Problem while processing the precision: "+correct+"/"+actual);
		
		// recall (FN)
		float rec = correct/possible;
		scores.put(SCORE_RECALL, rec);
		if(rec>1)
			throw new IllegalArgumentException("Problem while processing the recall: "+correct+"/"+possible);
		
		// f-measure
		float fMes = (2*pre*rec)/(pre+rec);
		scores.put(SCORE_FMEASURE, fMes);
	}

	/////////////////////////////////////////////////////////////////
	// RECORD			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the XML file used to store results */
	private static final String FILE_NAME = "muc" + FileNames.EX_TEXT;
	
	@Override
	public String getFileName()
	{	return FILE_NAME;
	}
}
