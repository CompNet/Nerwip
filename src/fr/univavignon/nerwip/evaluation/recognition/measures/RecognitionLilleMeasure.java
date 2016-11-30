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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.univavignon.nerwip.data.article.ArticleCategory;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.tools.file.FileNames;

/**
 * This class implements a set of measures allowing
 * to evaluate the performance of a recognizer, as described
 * in <a href="http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=6632052&amp;tag=1">
 * A Comparison of Named Entity Recognition Tools Applied to 
 * Biographical Texts</a>, S. Atdağ & V. Labatut, 2nd International 
 * Conference on Systems and Computer Science, p228-233, 2013.
 * <br/>
 * The <i>spatial evaluation</i> only concerns the position
 * of mentions. We distinguish the following counts:
 * <ul>
 * 		<li><b>Full match:</b> both mentions have exactly the same limits.</li>
 * 		<li><b>Partial match:</b> they have not exactly the same limits, but they intersect.</li>
 * 		<li><b>Wrong hit:</b> the detected mention does not correspond to any reference mention.</li>
 * 		<li><b>Complete miss:</b> a reference mention does not overlap with any detected one.</li>
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
 *  The count definitions are straightforward when considering all mentions,
 *  or when restricting to certain category of text. But less so when
 *  considering only certain types of mentions. In this case, we have the following:
 * <ul>
 * 		<li><b>Full match, partial match & complete miss:</b> counted in the reference type.</li>
 * 		<li><b>Wrong hit:</b> counted in the estimated type.</li>
 * </ul>
 * The <i>type evaluation</i> only concerns the entity type. We use the classic counts from
 * classification, expressed in function of one type of interest. They can be processed only
 * when there is a match (be it full or partial) between the considered reference and
 * estimated mentions.
 * <ul>
 * 		<li><b>True positive:</b> both mentions have the type of interest.</li>
 * 		<li><b>False positive:</b> the detected mention has the type of interest, but not the reference one.</li>
 * 		<li><b>False negative:</b> the reference mention has the type of interest, but not the detected one.</li>
 * 		<li><b>True negative:</b> (not counted) both mentions do not have the type of interest.</li>
 * </ul>
 *  These counts are then used to compute the following classic scores:
 *  <ul>
 *  	<li><b>Precision:</b> TP/(TP+FP)</li>
 *  	<li><b>Recall:</b> TP/(TP+FN)</li>
 *  	<li><b>F-measure:</b> H[(Pre;Rec), i.e.
 *  			harmonic mean of precision and recall</li>
 *  </ul>
 *  The count definitions are straightforward when considering mentions by types. For overall
 *  or category-by-category values, we simply sum all counts over the considered set of mentions,
 *  without considering their types, and process the mentionned scores.
 *  
 * @author Vincent Labatut
 */
public class RecognitionLilleMeasure extends AbstractRecognitionMeasure
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
	public RecognitionLilleMeasure(InterfaceRecognizer recognizer)
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
	public RecognitionLilleMeasure(InterfaceRecognizer recognizer, List<EntityType> types)
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
	 * @param categories
	 * 		Categories of article (military, scientist, etc.).
	 */
	public RecognitionLilleMeasure(InterfaceRecognizer recognizer, List<EntityType> types, Mentions reference, Mentions estimation, List<ArticleCategory> categories)
	{	super(recognizer,types,reference,estimation,categories);
	}	
	
	@Override
	public RecognitionLilleMeasure build(InterfaceRecognizer recognizer)
	{	RecognitionLilleMeasure result = new RecognitionLilleMeasure(recognizer);
		return result;
	}
	
	@Override
	public AbstractRecognitionMeasure build(InterfaceRecognizer recognizer, List<EntityType> types)
	{	RecognitionLilleMeasure result = new RecognitionLilleMeasure(recognizer, types);
		return result;
	}
	
	@Override
	public RecognitionLilleMeasure build(InterfaceRecognizer recognizer, List<EntityType> types, Mentions reference, Mentions estimation, List<ArticleCategory> categories)
	{	RecognitionLilleMeasure result = new RecognitionLilleMeasure(recognizer,types,reference,estimation,categories);
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
	/** Full matches: both mentions have exactly the same position */
	public static final String COUNT_FM = "FullMatches";
	/** Partial matches: overlap between the mentions positions */
	public static final String COUNT_PM = "PartialMatches";
	/** Wrong hit: mention deteected, where there's nothing at all */
	public static final String COUNT_WH = "WrongHits";
	/** Complete miss: mention not detected where there's one */
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
	public void processCounts(Mentions referenceOrig, Mentions estimationOrig, List<ArticleCategory> categories)
	{	// copy mention lists (those are going to be modified)
		List<AbstractMention<?,?>> reference = new ArrayList<AbstractMention<?,?>>(referenceOrig.getMentions());
		List<AbstractMention<?,?>> estimation = new ArrayList<AbstractMention<?,?>>(estimationOrig.getMentions());
		
		// remove the mentions whose type is not in the type list
		cleanMentions(reference);
		cleanMentions(estimation);
		
		// category mention lists
		for(ArticleCategory category: categories)
		{	for(String count: COUNTS)
			{	Map<ArticleCategory,List<AbstractMention<?,?>>> map = mentionsByCategory.get(count);
				List<AbstractMention<?,?>> list = new ArrayList<AbstractMention<?,?>>();
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
	 * Process the full match mentions.
	 * 
	 * @param reference
	 * 		List of the mentions of reference.
	 * @param estimation
	 * 		List of the mentions detected by the recognizer.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processFullMatches(List<AbstractMention<?,?>> reference, List<AbstractMention<?,?>> estimation, List<ArticleCategory> categories)
	{	Iterator<AbstractMention<?,?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractMention<?,?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			Iterator<AbstractMention<?,?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractMention<?,?> est = itEst.next();
				EntityType estType = est.getType();
				if(ref.hasSamePosition(est))
				{	found = true;
					itRef.remove();
					itEst.remove();
					
					// update spatial evaluation
						// all
						List<AbstractMention<?,?>> listAll = mentionsAll.get(COUNT_FM);
						listAll.add(est);
						// by type
						Map<EntityType,List<AbstractMention<?,?>>> mapByType = mentionsByType.get(COUNT_FM);
						List<AbstractMention<?,?>> listByType; 
						listByType = mapByType.get(refType);
						listByType.add(ref);
						if(refType!=estType)
						{	listByType = mapByType.get(estType);
							listByType.add(est);
						}
						// by category
						Map<ArticleCategory,List<AbstractMention<?,?>>> mapByCat = mentionsByCategory.get(COUNT_FM);
						for(ArticleCategory category: categories)
						{	List<AbstractMention<?,?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}
					
					// update typical evaluation
					updateTypicalEvaluation(ref, refType, est, estType, categories);
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
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processPartialMatches(List<AbstractMention<?,?>> reference, List<AbstractMention<?,?>> estimation, List<ArticleCategory> categories)
	{	Iterator<AbstractMention<?,?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractMention<?,?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			Iterator<AbstractMention<?,?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractMention<?,?> est = itEst.next();
				EntityType estType = est.getType();
				if(ref.overlapsWith(est))
				{	found = true;
					itRef.remove();
					itEst.remove();
					
					// update spatial evaluation
						// all
						List<AbstractMention<?,?>> listAll = mentionsAll.get(COUNT_PM);
						listAll.add(est);
						// by type
						Map<EntityType,List<AbstractMention<?,?>>> mapByType = mentionsByType.get(COUNT_PM);
						List<AbstractMention<?,?>> listByType; 
						listByType = mapByType.get(refType);
						listByType.add(ref);
						if(refType!=estType)
						{	listByType = mapByType.get(estType);
							listByType.add(est);
						}
						// by category
						Map<ArticleCategory,List<AbstractMention<?,?>>> mapByCat = mentionsByCategory.get(COUNT_PM);
						for(ArticleCategory category: categories)
						{	List<AbstractMention<?,?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}	
					
					// update typical evaluation
					updateTypicalEvaluation(ref, refType, est, estType, categories);
				}
			}
		}
	}
	
	/**
	 * Process the wrong hit mentions.
	 * 
	 * @param estimation
	 * 		List of the mentions detected by the recognizer.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processWrongHits(List<AbstractMention<?,?>> estimation, List<ArticleCategory> categories)
	{	for(AbstractMention<?,?> est: estimation)
		{	EntityType estType = est.getType();
	
			// update spatial evaluation
			{	// all
				List<AbstractMention<?,?>> listAll = mentionsAll.get(COUNT_WH);
				listAll.add(est);
				// by type
				Map<EntityType,List<AbstractMention<?,?>>> mapByType = mentionsByType.get(COUNT_WH);
				List<AbstractMention<?,?>> listByType = mapByType.get(estType);
				listByType.add(est);
				// by category
				Map<ArticleCategory,List<AbstractMention<?,?>>> mapByCat = mentionsByCategory.get(COUNT_WH);
				for(ArticleCategory category: categories)
				{	List<AbstractMention<?,?>> listByCat = mapByCat.get(category);
					listByCat.add(est);
				}
			}
				
			// update typical evaluation
			{	// all
				List<AbstractMention<?,?>> listAll = mentionsAll.get(COUNT_FP);
				listAll.add(est);
				// by type
				Map<EntityType,List<AbstractMention<?,?>>> mapByType = mentionsByType.get(COUNT_FP);
				List<AbstractMention<?,?>> listByType = mapByType.get(estType);
				listByType.add(est);
				// by category
				Map<ArticleCategory,List<AbstractMention<?,?>>> mapByCat = mentionsByCategory.get(COUNT_FP);
				for(ArticleCategory category: categories)
				{	List<AbstractMention<?,?>> listByCat = mapByCat.get(category);
					listByCat.add(est);
				}
			}
		}
	}
	
	/**
	 * Process the complete miss mentions.
	 * 
	 * @param reference
	 * 		List of the mentions of reference.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processCompleteMisses(List<AbstractMention<?,?>> reference, List<ArticleCategory> categories)
	{	for(AbstractMention<?,?> ref: reference)
		{	EntityType refType = ref.getType();
	
			// update spatial evaluation
			{	// all
				List<AbstractMention<?,?>> listAll = mentionsAll.get(COUNT_CM);
				listAll.add(ref);
				// by type
				Map<EntityType,List<AbstractMention<?,?>>> mapByType = mentionsByType.get(COUNT_CM);
				List<AbstractMention<?,?>> listByType = mapByType.get(refType);
				listByType.add(ref);
				// by category
				Map<ArticleCategory,List<AbstractMention<?,?>>> mapByCat = mentionsByCategory.get(COUNT_CM);
				for(ArticleCategory category: categories)
				{	List<AbstractMention<?,?>> listByCat = mapByCat.get(category);
					listByCat.add(ref);
				}
			}
				
			// update typical evaluation
			{	// all
				List<AbstractMention<?,?>> listAll = mentionsAll.get(COUNT_FN);
				listAll.add(ref);
				// by type
				Map<EntityType,List<AbstractMention<?,?>>> mapByType = mentionsByType.get(COUNT_FN);
				List<AbstractMention<?,?>> listByType = mapByType.get(refType);
				listByType.add(ref);
				// by category
				Map<ArticleCategory,List<AbstractMention<?,?>>> mapByCat = mentionsByCategory.get(COUNT_FN);
				for(ArticleCategory category: categories)
				{	List<AbstractMention<?,?>> listByCat = mapByCat.get(category);
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
	 * 		Mention of reference.
	 * @param refType
	 * 		Type of the mention of reference.
	 * @param est
	 * 		Mention detected by the recognizer.
	 * @param estType
	 * 		Type of the mention detected by the recognizer.
	 * @param categories
	 * 		Categories of the considered article.
	 * @return
	 * 		{@code true} iff both types are similar.
	 */
	private boolean updateTypicalEvaluation(AbstractMention<?,?> ref, EntityType refType, AbstractMention<?,?> est, EntityType estType, List<ArticleCategory> categories)
	{	boolean result = refType==estType;
		
	// types match
		if(result)
		{	// true positives
			{	// all
				List<AbstractMention<?,?>> listAll = mentionsAll.get(COUNT_TP);
				listAll.add(est);
				// by type
				Map<EntityType,List<AbstractMention<?,?>>> mapByType = mentionsByType.get(COUNT_TP);
				List<AbstractMention<?,?>> listByType = mapByType.get(estType);
				listByType.add(est);
				// by category
				Map<ArticleCategory,List<AbstractMention<?,?>>> mapByCat = mentionsByCategory.get(COUNT_TP);
				for(ArticleCategory category: categories)
				{	List<AbstractMention<?,?>> listByCat = mapByCat.get(category);
					listByCat.add(est);
				}
			}
		}
		
		// types don't match
		else
		{	// false negatives
			{	// all
				List<AbstractMention<?,?>> listAll = mentionsAll.get(COUNT_FN);
				listAll.add(ref);
				// by type
				Map<EntityType,List<AbstractMention<?,?>>> mapByType = mentionsByType.get(COUNT_FN);
				List<AbstractMention<?,?>> listByType = mapByType.get(refType);
				listByType.add(ref);
				// by category
				Map<ArticleCategory,List<AbstractMention<?,?>>> mapByCat = mentionsByCategory.get(COUNT_FN);
				for(ArticleCategory category: categories)
				{	List<AbstractMention<?,?>> listByCat = mapByCat.get(category);
					listByCat.add(ref);
				}
			}
			// false positives
			{	// all
				List<AbstractMention<?,?>> listAll = mentionsAll.get(COUNT_FP);
				listAll.add(est);
				// by type
				Map<EntityType,List<AbstractMention<?,?>>> mapByType = mentionsByType.get(COUNT_FP);
				List<AbstractMention<?,?>> listByType = mapByType.get(estType);
				listByType.add(est);
				// by category
				Map<ArticleCategory,List<AbstractMention<?,?>>> mapByCat = mentionsByCategory.get(COUNT_FP);
				for(ArticleCategory category: categories)
				{	List<AbstractMention<?,?>> listByCat = mapByCat.get(category);
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
