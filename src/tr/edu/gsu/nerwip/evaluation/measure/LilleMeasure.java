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
 * This class implements a set of measures allowing
 * to evaluate the performance of a NER tool, as described
 * in <a href="http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=6632052&amp;tag=1">
 * A Comparison of Named Entity Recognition Tools Applied to 
 * Biographical Texts</a>, S. Atdağ & V. Labatut, 2nd International 
 * Conference on Systems and Computer Science, p228-233, 2013.
 * <br/>
 * The <i>spatial evaluation</i> only concerns the position
 * of entities. We distinguish the following counts:
 * <ul>
 * 		<li><b>Full match:</b> both entities have exactly the same limits.</li>
 * 		<li><b>Partial match:</b> they have not exactly the same limits, but they intersect.</li>
 * 		<li><b>Wrong hit:</b> the detected entity does not correspond to any reference entity.</li>
 * 		<li><b>Complete miss:</b> a reference entity does not overlap with any detected one.</li>
 * </ul>
 *  These counts are used to process the following scores:
 *  <ul>
 *  	<li><b>Full precision:</b> FM/(FM+PM+WH)</li>
 *  	<li><b>Partial precision:</b> PM/(FM+PM+WH)</li>
 *  	<li><b>Full recall:</b> FM/(FM+PM+CM)</li>
 *  	<li><b>Partial recall:</b> PM/(FM+PM+CM)</li>
 *  	<li><b>Total F-measure:</b> H[(FM+PM)/(FM+PM+WH);(FM+PM)/(FM+PM+CM)), i.e.
 *  			harmonic mean of fPre+pPre and pRec+pRec</li>
 *  </ul>
 *  The count definitions are straightforward when considering all entities,
 *  or when restricting to certain category of text. But less so when
 *  considering only certain types of entities. In this case, we have the following:
 * <ul>
 * 		<li><b>Full match, partial match & complete miss:</b> counted in the reference type.</li>
 * 		<li><b>Wrong hit:</b> counted in the estimated type.</li>
 * </ul>
 * The <i>type evaluation</i> only concerns the entity type. We use the classic counts from
 * classification, expressed in function of one type of interest. They can be processed only
 * when there is a match (be it full or partial) between the considered reference and
 * estimated entities.
 * <ul>
 * 		<li><b>True positive:</b> both entities have the type of interest.</li>
 * 		<li><b>False positive:</b> the detected entity has the type of interest, but not the reference one.</li>
 * 		<li><b>False negative:</b> the reference entity has the type of interest, but not the detected one.</li>
 * 		<li><b>True negative:</b> (not counted) both entities do not have the type of interest.</li>
 * </ul>
 *  These counts are then used to compute the following classic scores:
 *  <ul>
 *  	<li><b>Precision:</b> TP/(TP+FP)</li>
 *  	<li><b>Recall:</b> TP/(TP+FN)</li>
 *  	<li><b>F-measure:</b> H[(Pre;Rec), i.e.
 *  			harmonic mean of precision and recall</li>
 *  </ul>
 *  The count definitions are straightforward when considering entities by types. For overall
 *  or category-by-category values, we simply sum all counts over the considered set of entities,
 *  without considering their types, and process the mentionned scores.
 *  
 * @author Vincent Labatut
 */
public class LilleMeasure extends AbstractMeasure
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
	public LilleMeasure(AbstractRecognizer recognizer)
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
	public LilleMeasure(AbstractRecognizer recognizer, List<EntityType> types)
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
	public LilleMeasure(AbstractRecognizer recognizer, List<EntityType> types, Entities reference, Entities estimation, List<ArticleCategory> categories)
	{	super(recognizer,types,reference,estimation,categories);
	}	
	
	@Override
	public LilleMeasure build(AbstractRecognizer recognizer)
	{	LilleMeasure result = new LilleMeasure(recognizer);
		return result;
	}
	
	@Override
	public AbstractMeasure build(AbstractRecognizer recognizer, List<EntityType> types)
	{	LilleMeasure result = new LilleMeasure(recognizer, types);
		return result;
	}
	
	@Override
	public LilleMeasure build(AbstractRecognizer recognizer, List<EntityType> types, Entities reference, Entities estimation, List<ArticleCategory> categories)
	{	LilleMeasure result = new LilleMeasure(recognizer,types,reference,estimation,categories);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Conventional name of the measure */
	private final static String NAME = "LILLE";
	
	@Override
	public String getName()
	{	return NAME;
	}
	
	/////////////////////////////////////////////////////////////////
	// COUNTS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Full matches: both entities have exactly the same position */
	public static final String COUNT_FM = "FullMatches";
	/** Partial matches: overlap between the entities positions */
	public static final String COUNT_PM = "PartialMatches";
	/** Wrong hit: entity deteected, where there's nothing at all */
	public static final String COUNT_WH = "WrongHits";
	/** Complete miss: entity not detected where there's one */
	public static final String COUNT_CM = "CompleteMisses";
	/** True positive: correct type detected */
	public static final String COUNT_TP = "TruePositives";
	/** False positive: type of interest detected, where another one shoyld have */
	public static final String COUNT_FP = "FalsePositives";
	/** False negative: other type than the type of interest detected, where the type of interest should have */
	public static final String COUNT_FN = "FalseNegatives";
	/** Names of the supported counts */
	private static final List<String> COUNTS = Arrays.asList(
		COUNT_FM,
		COUNT_PM,
		COUNT_WH,
		COUNT_CM,
		COUNT_TP,
		COUNT_FP,
		COUNT_FN
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
		
		// category entity lists
		for(ArticleCategory category: categories)
		{	for(String count: COUNTS)
			{	Map<ArticleCategory,List<AbstractEntity<?>>> map = entitiesByCategory.get(count);
				List<AbstractEntity<?>> list = new ArrayList<AbstractEntity<?>>();
				map.put(category,list);
			}
		}
		
		// look for the different cases
		processFullMatches(reference, estimation, categories);
		processPartialMatches(reference, estimation, categories);
		processWrongHits(estimation, categories);
		processCompleteMisses(reference, categories);
		
		// update counts
		updateCounts(categories);
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
	private void processFullMatches(List<AbstractEntity<?>> reference, List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
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
						// all
						List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_FM);
						listAll.add(est);
						// by type
						Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_FM);
						List<AbstractEntity<?>> listByType; 
						listByType = mapByType.get(refType);
						listByType.add(ref);
						if(refType!=estType)
						{	listByType = mapByType.get(estType);
							listByType.add(est);
						}
						// by category
						Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_FM);
						for(ArticleCategory category: categories)
						{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}
					
					// update typical evaluation
					updateTypicalEvaluation(ref, refType, est, estType, categories);
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
	private void processPartialMatches(List<AbstractEntity<?>> reference, List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
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
						// all
						List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_PM);
						listAll.add(est);
						// by type
						Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_PM);
						List<AbstractEntity<?>> listByType; 
						listByType = mapByType.get(refType);
						listByType.add(ref);
						if(refType!=estType)
						{	listByType = mapByType.get(estType);
							listByType.add(est);
						}
						// by category
						Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_PM);
						for(ArticleCategory category: categories)
						{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}	
					
					// update typical evaluation
					updateTypicalEvaluation(ref, refType, est, estType, categories);
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
	private void processWrongHits(List<AbstractEntity<?>> estimation, List<ArticleCategory> categories)
	{	for(AbstractEntity<?> est: estimation)
		{	EntityType estType = est.getType();
	
			// update spatial evaluation
			{	// all
				List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_WH);
				listAll.add(est);
				// by type
				Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_WH);
				List<AbstractEntity<?>> listByType = mapByType.get(estType);
				listByType.add(est);
				// by category
				Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_WH);
				for(ArticleCategory category: categories)
				{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
					listByCat.add(est);
				}
			}
				
			// update typical evaluation
			{	// all
				List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_FP);
				listAll.add(est);
				// by type
				Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_FP);
				List<AbstractEntity<?>> listByType = mapByType.get(estType);
				listByType.add(est);
				// by category
				Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_FP);
				for(ArticleCategory category: categories)
				{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
					listByCat.add(est);
				}
			}
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
	private void processCompleteMisses(List<AbstractEntity<?>> reference, List<ArticleCategory> categories)
	{	for(AbstractEntity<?> ref: reference)
		{	EntityType refType = ref.getType();
	
			// update spatial evaluation
			{	// all
				List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_CM);
				listAll.add(ref);
				// by type
				Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_CM);
				List<AbstractEntity<?>> listByType = mapByType.get(refType);
				listByType.add(ref);
				// by category
				Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_CM);
				for(ArticleCategory category: categories)
				{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
					listByCat.add(ref);
				}
			}
				
			// update typical evaluation
			{	// all
				List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_FN);
				listAll.add(ref);
				// by type
				Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_FN);
				List<AbstractEntity<?>> listByType = mapByType.get(refType);
				listByType.add(ref);
				// by category
				Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_FN);
				for(ArticleCategory category: categories)
				{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
					listByCat.add(ref);
				}
			}
		}
	}

	/**
	 * Compares the specified types and updates
	 * the measures accordingly.
	 * 
	 * @param ref
	 * 		Entity of reference.
	 * @param refType
	 * 		Type of the entity of reference.
	 * @param est
	 * 		Entity detected by the NER tool.
	 * @param estType
	 * 		Type of the entity detected by the NER tool.
	 * @param categories
	 * 		Categories of the considered article.
	 * @return
	 * 		{@code true} iff both types are similar.
	 */
	private boolean updateTypicalEvaluation(AbstractEntity<?> ref, EntityType refType, AbstractEntity<?> est, EntityType estType, List<ArticleCategory> categories)
	{	boolean result = refType==estType;
		
	// types match
		if(result)
		{	// true positives
			{	// all
				List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_TP);
				listAll.add(est);
				// by type
				Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_TP);
				List<AbstractEntity<?>> listByType = mapByType.get(estType);
				listByType.add(est);
				// by category
				Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_TP);
				for(ArticleCategory category: categories)
				{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
					listByCat.add(est);
				}
			}
		}
		
		// types don't match
		else
		{	// false negatives
			{	// all
				List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_FN);
				listAll.add(ref);
				// by type
				Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_FN);
				List<AbstractEntity<?>> listByType = mapByType.get(refType);
				listByType.add(ref);
				// by category
				Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_FN);
				for(ArticleCategory category: categories)
				{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
					listByCat.add(ref);
				}
			}
			// false positives
			{	// all
				List<AbstractEntity<?>> listAll = entitiesAll.get(COUNT_FP);
				listAll.add(est);
				// by type
				Map<EntityType,List<AbstractEntity<?>>> mapByType = entitiesByType.get(COUNT_FP);
				List<AbstractEntity<?>> listByType = mapByType.get(estType);
				listByType.add(est);
				// by category
				Map<ArticleCategory,List<AbstractEntity<?>>> mapByCat = entitiesByCategory.get(COUNT_FP);
				for(ArticleCategory category: categories)
				{	List<AbstractEntity<?>> listByCat = mapByCat.get(category);
					listByCat.add(est);
				}
			}
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SCORES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Full precision: precision processed using the full matches */
	public static final String SCORE_FP = "FullPrecision";
	/** Partial precision: precision processed using the partial matches */
	public static final String SCORE_PP = "PartialPrecision";
	/** Total precision: sum of the full and partial precisions */
	public static final String SCORE_TP = "TotalPrecision";
	/** Full recall: recall processed using the full matches */
	public static final String SCORE_FR = "FullRecall";
	/** Partial recall: recall processed using the partial matches */
	public static final String SCORE_PR = "PartialRecall";
	/** Total precision: sum of the full and partial recalls */
	public static final String SCORE_TR = "TotalRecall";
	/** TF-Measure: F-measure processed using both full and partial precision and recall */
	public static final String SCORE_TF = "TF-Measure";
	/** Precision: precision processed using the type counts */
	public static final String SCORE_P = "Precision";
	/** Recall: recall processed using the type counts */
	public static final String SCORE_R = "Recall";
	/** F-Measure: F-measure processed using the type counts */
	public static final String SCORE_F = "F-Measure";
	/** Names of the supported scores */
	private static final List<String> SCORES = Arrays.asList(
		SCORE_FP,
		SCORE_PP,
		SCORE_TP,
		SCORE_FR,
		SCORE_PR,
		SCORE_TR,
		SCORE_TF,
		SCORE_P,
		SCORE_R,
		SCORE_F
	);

	@Override
	public List<String> getScoreNames()
	{	return SCORES;
	}
	
	@Override
	protected void processScores(Map<String, Integer> counts, Map<String, Float> scores)
	{	// spatial scores
		{	float fm = counts.get(COUNT_FM);
			float pm = counts.get(COUNT_PM);
			float wh = counts.get(COUNT_WH);
			float cm = counts.get(COUNT_CM);
			
			// full precision
			float fPre = fm/(fm+pm+wh);
			scores.put(SCORE_FP, fPre);
			// partial precision
			float pPre = pm/(fm+pm+wh);
			scores.put(SCORE_PP, pPre);
			// total precision
			float tPre = fPre + pPre;
			scores.put(SCORE_TP, tPre);
			
			// full recall
			float fRec = fm/(fm+pm+cm);
			scores.put(SCORE_FR, fRec);
			// partial recall
			float pRec = pm/(fm+pm+cm);
			scores.put(SCORE_PR, pRec);
			// total recall
			float tRec = fRec + pRec;
			scores.put(SCORE_TR, tRec);
			
			// (total) f-measure
			float pre = fPre+pPre;
			float rec = fRec+pRec;
			float fMes = (2*pre*rec)/(pre+rec);
			scores.put(SCORE_TF, fMes);
		}
		
		// typical scores
		{	float tp = counts.get(COUNT_TP);
			float fp = counts.get(COUNT_FP);
			float fn = counts.get(COUNT_FN);
			
			// precision (FP)
			float pre = tp/(tp+fp);
			scores.put(SCORE_P, pre);
			
			// recall (FN)
			float rec = tp/(tp+fn);
			scores.put(SCORE_R, rec);
			
			// f-measure
			float fMes = (2*pre*rec)/(pre+rec);
			scores.put(SCORE_F, fMes);
		}
	}

	/////////////////////////////////////////////////////////////////
	// RECORD			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the XML file used to store results */
	private static final String FILE_NAME = "lille" + FileNames.EX_TEXT;
	
	@Override
	public String getFileName()
	{	return FILE_NAME;
	}
}
