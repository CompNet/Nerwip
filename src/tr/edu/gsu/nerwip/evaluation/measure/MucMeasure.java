package tr.edu.gsu.nerwip.evaluation.measure;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tr.edu.gsu.nerwip.data.article.ArticleCategory;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.tools.file.FileNames;

/**
 * This class implements the measures used in
 * the MUC conference series (Message Understanding Conference),
 * as described in <a href="http://nlp.cs.nyu.edu/sekine/papers/li07.pdf">
 * A survey of named entity recognition and classification<a>,
 * D. Nadeau & S. Sekinex, Lingvisticae Investigationes, 30:3-26, 2007.
 *  
 * @author Vincent Labatut
 */
public class MucMeasure extends AbstractMeasure
{	
	/**
	 * Builds a new instance of this measure,
	 * for the specified NER tool. This
	 * constructor is used when loading results
	 * from a file.
	 * 
	 * @param recognizer
	 * 		Concerned NER tool.
	 */
	public MucMeasure(AbstractRecognizer recognizer)
	{	super(recognizer);
	}
	
	/**
	 * Builds a new instance of measure,
	 * for the specified NER tool. This
	 * constructor is used when combining
	 * several measures into one.
	 * 
	 * @param recognizer
	 * 		Concerned NER tool.
	 * @param types
	 * 		Types to consider in the assessmnent.
	 */
	public MucMeasure(AbstractRecognizer recognizer, List<EntityType> types)
	{	super(recognizer,types);
	}

	/**
	 * Builds a new instance of this measure,
	 * for the specified NER tool and results.
	 * This constructor is used when actually processing
	 * the NER tool performance.
	 * 
	 * @param recognizer
	 * 		Concerned NER tool.
	 * @param types
	 * 		Types to consider in the assessmnent.
	 * @param reference
	 * 		Entities used as reference.
	 * @param estimation
	 * 		Entities detected by the NER tool.
	 * @param categories
	 * 		Categories of article (military, scientist, etc.).
	 */
	public MucMeasure(AbstractRecognizer recognizer, List<EntityType> types, Entities reference, Entities estimation, List<ArticleCategory> categories)
	{	super(recognizer,types,reference,estimation,categories);
	}	
	
	@Override
	public MucMeasure build(AbstractRecognizer recognizer)
	{	MucMeasure result = new MucMeasure(recognizer);
		return result;
	}
	
	@Override
	public AbstractMeasure build(AbstractRecognizer recognizer, List<EntityType> types)
	{	MucMeasure result = new MucMeasure(recognizer, types);
		return result;
	}
	
	@Override
	public MucMeasure build(AbstractRecognizer recognizer, List<EntityType> types, Entities reference, Entities estimation, List<ArticleCategory> categories)
	{	MucMeasure result = new MucMeasure(recognizer,types,reference,estimation,categories);
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
	/** Twice the number of detected entities */
	public static final String COUNT_ACTUAL = "Actual";
	/** Twice the number of entities in the reference */
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
	public void processCounts(Entities referenceOrig, Entities estimationOrig, List<ArticleCategory> categories)
	{	// copy entity lists (those are going to be modified)
		List<AbstractEntity<?>> reference = new ArrayList<AbstractEntity<?>>(referenceOrig.getEntities());
		List<AbstractEntity<?>> estimation = new ArrayList<AbstractEntity<?>>(estimationOrig.getEntities());
		
		// remove the entities whose type is not in the type list
		cleanEntities(reference);
		cleanEntities(estimation);
		
		// entity lists by category
		for(ArticleCategory category: categories)
		{	for(String count: COUNTS)
			{	Map<ArticleCategory,List<AbstractEntity<?>>> map = entitiesByCategory.get(count);
				List<AbstractEntity<?>> list = new ArrayList<AbstractEntity<?>>();
				map.put(category,list);
			}
		}
		
		// look for the different cases
		processSpatialExactMatches(reference, estimation, categories);
		processSpatialPartialMatches(reference, estimation, categories);
		processSpatialFalseAlarms(estimation, categories);
		processSpatialOmissions(reference, categories);
		
		// update counts
		updateCounts(categories);
	}
	
	/**
	 * Changes appropriately all the required structures in order to count the specified 
	 * entity, for the specified type and categories, in the specified count type.
	 * 
	 * @param entity
	 * 		Entity to count.
	 * @param type
	 * 		Type associated to the count.
	 * @param categories
	 * 		Categories associated to the count.
	 * @param count
	 * 		Type of count.
	 */
	private void addToStructures(AbstractEntity<?> entity, EntityType type, List<ArticleCategory> categories, String count)
	{	List<AbstractEntity<?>> listAll = entitiesAll.get(count);
		listAll.add(entity);
		
		Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(count);
		List<AbstractEntity<?>> listByType = mapByType.get(type);
		listByType.add(entity);
		
		Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(count);
		for(ArticleCategory category: categories)
		{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
			listByCat.add(entity);
		}
	}
	
	/**
	 * Changes appropriately all the required structures in order to count the specified 
	 * entity, for the specified types and categories, in the specified count type.
	 * 
	 * @param entity
	 * 		Entity to count.
	 * @param type1
	 * 		Type associated to the count.
	 * @param type2
	 * 		Other type associated to the count.
	 * @param categories
	 * 		Categories associated to the count.
	 * @param count
	 * 		Type of count.
	 */
	private void addToStructures(AbstractEntity<?> entity, EntityType type1, EntityType type2, List<ArticleCategory> categories, String count)
	{	List<AbstractEntity<?>> listAll = entitiesAll.get(count);
		listAll.add(entity);
		
		Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(count);
		List<AbstractEntity<?>> listByType;
		listByType = mapByType.get(type1);
		listByType.add(entity);
		if(type1!=type2)
		{	listByType = mapByType.get(type2);
			listByType.add(entity);
		}
		
		Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(count);
		for(ArticleCategory category: categories)
		{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
			listByCat.add(entity);
		}
	}
	
	/**
	 * Process the full match entities.
	 * 
	 * @param reference
	 * 		List of the entities of reference.
	 * @param estimation
	 * 		List of the entities detected by the NER tool.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processSpatialExactMatches(List<AbstractEntity<?>> reference, List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
	{	Iterator<AbstractEntity<?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractEntity<?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			Iterator<AbstractEntity<?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractEntity<?> est = itEst.next();
				EntityType estType = est.getType();
				if(ref.hasSamePosition(est))
				{	found = true;
					itRef.remove();
					itEst.remove();
					
					// update spatial evaluation
					addToStructures(ref, refType, estType, categories, COUNT_CORRECT);
					addToStructures(est, refType, estType, categories, COUNT_ACTUAL);
					addToStructures(ref, refType, estType, categories, COUNT_POSSIBLE);
					
					// update typical evaluation
					if(refType==estType)
						addToStructures(ref, refType, categories, COUNT_CORRECT);
					addToStructures(est, estType, categories, COUNT_ACTUAL);
					addToStructures(ref, refType, categories, COUNT_POSSIBLE);
				}
			}
		}
	}
	
	/**
	 * Process the partial match entities.
	 * 
	 * @param reference
	 * 		List of the entities of reference.
	 * @param estimation
	 * 		List of the entities detected by the NER tool.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processSpatialPartialMatches(List<AbstractEntity<?>> reference, List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
	{	Iterator<AbstractEntity<?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractEntity<?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			Iterator<AbstractEntity<?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractEntity<?> est = itEst.next();
				EntityType estType = est.getType();
				if(ref.overlapsWith(est))
				{	found = true;
					itRef.remove();
					itEst.remove();
					
					// update spatial evaluation
					addToStructures(est, estType, categories, COUNT_ACTUAL);
					addToStructures(ref, refType, categories, COUNT_POSSIBLE);
					
					// update typical evaluation
					if(refType==estType)
						addToStructures(ref, refType, categories, COUNT_CORRECT);
					addToStructures(est, estType, categories, COUNT_ACTUAL);
					addToStructures(ref, refType, categories, COUNT_POSSIBLE);
				}
			}
		}
	}
	
	/**
	 * Process the wrong hit entities.
	 * 
	 * @param estimation
	 * 		List of the entities detected by the NER tool.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processSpatialFalseAlarms(List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
	{	for(AbstractEntity<?> est: estimation)
		{	EntityType estType = est.getType();
	
			// update spatial evaluation
			addToStructures(est, estType, categories, COUNT_ACTUAL);
				
			// update typical evaluation
			addToStructures(est, estType, categories, COUNT_ACTUAL);
		}
	}
	
	/**
	 * Process the complete miss entities.
	 * 
	 * @param reference
	 * 		List of the entities of reference.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processSpatialOmissions(List<AbstractEntity<?>> reference, List<ArticleCategory> categories)
	{	for(AbstractEntity<?> ref: reference)
		{	EntityType refType = ref.getType();
	
			// update spatial evaluation
			addToStructures(ref, refType, categories, COUNT_POSSIBLE);
				
			// update typical evaluation
			addToStructures(ref, refType, categories, COUNT_POSSIBLE);
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
