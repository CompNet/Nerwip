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
 * This class implements a first version of our evaluation
 * process, as described in Yasa's lisans thesis:
 * <a href="http://bit.gsu.edu.tr/index.php?option=com_jresearch&view=member&id=12&task=show&Itemid=48&lang=fr">
 * Reconnaissance d'entités nommées pour l'extraction automatique d'un 
 * réseau social à partir de Wikipedia</a>, Y. Akbulut, BSc Thesis, 
 * Galatasaray University, Istanbul, TR.
 * No scores are processed, only a number of counts:
 * <ul>
 * 		<li><b>True positive</b>: the limits of both entities perfectly match.</li>
 * 		<li><b>Excess positive</b>: the detected entity contains more than the reference entity.</li>
 * 		<li><b>Partial positive</b>: the detected entity contains less than the reference entity.</li>
 * 		<li><b>False positive</b>: the detected entity does not intersect with any reference entity.</li>
 * 		<li><b>False negative</b>: the reference entity does not intersect with any detected entity.</li>
 * </ul>
 * In the 3 first cases, one can distinguish between entities whose type was correctly
 * estimated, and those whose type is not wrong. We therefore have a total of 8 different
 * counts.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class IstanbulMeasure extends AbstractMeasure
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
	public IstanbulMeasure(AbstractRecognizer recognizer)
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
	public IstanbulMeasure(AbstractRecognizer recognizer, List<EntityType> types)
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
	public IstanbulMeasure(AbstractRecognizer recognizer, List<EntityType> types, Entities reference, Entities estimation, List<ArticleCategory> categories)
	{	super(recognizer,types,reference,estimation,categories);
	}	

	@Override
	public AbstractMeasure build(AbstractRecognizer recognizer, List<EntityType> types, Entities reference, Entities estimation, List<ArticleCategory> categories)
	{	IstanbulMeasure result = new IstanbulMeasure(recognizer, types, reference, estimation, categories);
		return result;
	}

	@Override
	public AbstractMeasure build(AbstractRecognizer recognizer, List<EntityType> types)
	{	IstanbulMeasure result = new IstanbulMeasure(recognizer,types);
		return result;
	}

	@Override
	public AbstractMeasure build(AbstractRecognizer recognizer)
	{	IstanbulMeasure result = new IstanbulMeasure(recognizer);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Conventional name of the measure */
	private final static String NAME = "ISTANBUL";
	
	@Override
	public String getName()
	{	return NAME;
	}
	
	/////////////////////////////////////////////////////////////////
	// COUNTS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Perfectly estimated entity */
	public static final String COUNT_TP_CT = "TruePositiveCorrectType";
	/** Limits are correct, but type is wrong */
	public static final String COUNT_TP_WT = "TruePositiveWrongType";
	/** Estimated limits contain more than the reference, type is correct */
	public static final String COUNT_EP_CT = "ExcessPositiveCorrectType";
	/** Estimated limits contain more than the reference, type is wrong */
	public static final String COUNT_EP_WT = "ExcessPositiveWrongType";
	/** Estimated limits contain less than the reference, type is correct */
	public static final String COUNT_PP_CT = "PartialPositiveCorrectType";
	/** Estimated limits contain less than the reference, type is wrong */
	public static final String COUNT_PP_WT = "PartialPositiveWrongType";
	/** Estimated limits do not intersect with any reference entity */
	public static final String COUNT_FP = "FalsePositive";
	/** Reference limits do not intersect with any estimated entity */
	public static final String COUNT_FN = "FalseNegative";

	/** Names of the supported counts */
	private static final List<String> COUNTS = Arrays.asList(
		COUNT_TP_CT,
		COUNT_TP_WT,
		COUNT_EP_CT,
		COUNT_EP_WT,
		COUNT_PP_CT,
		COUNT_PP_WT,
		COUNT_FP,
		COUNT_FN
	);
	
	@Override
	public List<String> getCountNames()
	{	return COUNTS;
	}

	@Override
	protected void processCounts(Entities referenceOrig, Entities estimationOrig, List<ArticleCategory> categories) 
	{	// copy entity lists (those are going to be modified)
		List<AbstractEntity<?>> reference = new ArrayList<AbstractEntity<?>>(referenceOrig.getEntities());
		List<AbstractEntity<?>> estimation = new ArrayList<AbstractEntity<?>>(estimationOrig.getEntities());
		
		// remove the entities whose type is not in the type list
		cleanEntities(reference);
		cleanEntities(estimation);
		
		// category entity lists
		for(ArticleCategory category: categories)
		{	for(String count: COUNTS)
			{	Map<ArticleCategory,List<AbstractEntity<?>>> map = entitiesByCategory.get(count);
				List<AbstractEntity<?>> list = new ArrayList<AbstractEntity<?>>();
				map.put(category,list);
			}
		}
		
		// look for the different cases
		processTruePositives(reference, estimation, categories);
		processExcessPositives(reference, estimation, categories);
		processPartialPositives(reference, estimation, categories);
		processFalsePositives(estimation, categories);
		processFalseNegatives(reference, categories);

		// update counts
		updateCounts(categories);
	}
	
	/**
	 * Process the true positive entities.
	 * 
	 * @param reference
	 * 		List of the entities of reference.
	 * @param estimation
	 * 		List of the entities detected by the NER tool.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processTruePositives(List<AbstractEntity<?>> reference, List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
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
					
					String countName = null;
					if(refType==estType)
						countName = COUNT_TP_CT;
					else
						countName = COUNT_TP_WT;
					
					// update spatial evaluation
					{	List<AbstractEntity<?>> listAll = entitiesAll.get(countName);
						listAll.add(est);
						Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(countName);
						List<AbstractEntity<?>> listByType = mapByType.get(refType);
						listByType.add(est);
						Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(countName);
						for(ArticleCategory category: categories)
						{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Process the excess positive entities.
	 * 
	 * @param reference
	 * 		List of the entities of reference.
	 * @param estimation
	 * 		List of the entities detected by the NER tool.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processExcessPositives(List<AbstractEntity<?>> reference, List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
	{	Iterator<AbstractEntity<?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractEntity<?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			
			Iterator<AbstractEntity<?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractEntity<?> est = itEst.next();
				EntityType estType = est.getType();
				if(est.contains(ref))
				{	found = true;
					itRef.remove();
					itEst.remove();
					
					String countName = null;
					if(refType==estType)
						countName = COUNT_EP_CT;
					else
						countName = COUNT_EP_WT;
					
					// update spatial evaluation
					{	List<AbstractEntity<?>> listAll = entitiesAll.get(countName);
						listAll.add(est);
						Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(countName);
						List<AbstractEntity<?>> listByType = mapByType.get(refType);
						listByType.add(est);
						Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(countName);
						for(ArticleCategory category: categories)
						{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}
					}
				}
			}
		}
	}

	/**
	 * Process the partial positive entities.
	 * 
	 * @param reference
	 * 		List of the entities of reference.
	 * @param estimation
	 * 		List of the entities detected by the NER tool.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processPartialPositives(List<AbstractEntity<?>> reference, List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
	{	Iterator<AbstractEntity<?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractEntity<?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			
			Iterator<AbstractEntity<?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractEntity<?> est = itEst.next();
				EntityType estType = est.getType();
				if(est.overlapsWith(ref))
				{	found = true;
					itRef.remove();
					itEst.remove();
					
					String countName = null;
					if(refType==estType)
						countName = COUNT_PP_CT;
					else
						countName = COUNT_PP_WT;
					
					// update spatial evaluation
					{	List<AbstractEntity<?>> listAll = entitiesAll.get(countName);
						listAll.add(est);
						Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(countName);
						List<AbstractEntity<?>> listByType = mapByType.get(refType);
						listByType.add(est);
						Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(countName);
						for(ArticleCategory category: categories)
						{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Process the false positive entities.
	 * 
	 * @param estimation
	 * 		List of the entities detected by the NER tool.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processFalsePositives(List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
	{	for(AbstractEntity<?> est: estimation)
		{	EntityType estType = est.getType();
			List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_FN);
			listAll.add(est);
			Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_FN);
			List<AbstractEntity<?>> listByType = mapByType.get(estType);
			listByType.add(est);
			Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_FN);
			for(ArticleCategory category: categories)
			{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
				listByCat.add(est);
			}
		}
	}
	
	/**
	 * Process the false negative entities.
	 * 
	 * @param reference
	 * 		List of the entities of reference.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processFalseNegatives(List<AbstractEntity<?>> reference, List<ArticleCategory> categories)
	{	for(AbstractEntity<?> ref: reference)
		{	EntityType refType = ref.getType();
			List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_FP);
			listAll.add(ref);
			Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_FP);
			List<AbstractEntity<?>> listByType = mapByType.get(refType);
			listByType.add(ref);
			Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_FP);
			for(ArticleCategory category: categories)
			{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
				listByCat.add(ref);
			}
		}
	}

	/////////////////////////////////////////////////////////////////
	// SCORES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Names of the supported scores: none, here */
	private static final List<String> SCORES = new ArrayList<String>();

	@Override
	public List<String> getScoreNames()
	{	return SCORES;
	}

	@Override
	protected void processScores(Map<String, Integer> counts, Map<String, Float> scores)
	{	// nothing to do here
	}

	/////////////////////////////////////////////////////////////////
	// RECORD			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the XML file used to store results */
	private static final String FILE_NAME = "istanbul" + FileNames.EX_TXT;
	
	@Override
	public String getFileName()
	{	return FILE_NAME;
	}
}
