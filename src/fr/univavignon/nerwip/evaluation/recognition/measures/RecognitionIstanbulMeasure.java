package fr.univavignon.nerwip.evaluation.recognition.measures;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
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
 * This class implements a first version of our evaluation
 * process, as described in Yasa's lisans thesis:
 * <a href="http://bit.gsu.edu.tr/index.php?option=com_jresearch&view=member&id=12&task=show&Itemid=48&lang=fr">
 * Reconnaissance d'entités nommées pour l'extraction automatique d'un 
 * réseau social à partir de Wikipedia</a>, Y. Akbulut, BSc Thesis, 
 * Galatasaray University, Istanbul, TR.
 * No scores are processed, only a number of counts:
 * <ul>
 * 		<li><b>True positive</b>: the limits of both mentions perfectly match.</li>
 * 		<li><b>Excess positive</b>: the detected mention contains more than the reference mention.</li>
 * 		<li><b>Partial positive</b>: the detected mention contains less than the reference mention.</li>
 * 		<li><b>False positive</b>: the detected mention does not intersect with any reference mention.</li>
 * 		<li><b>False negative</b>: the reference mention does not intersect with any detected mention.</li>
 * </ul>
 * In the 3 first cases, one can distinguish between mentions whose type was correctly
 * estimated, and those whose type is not wrong. We therefore have a total of 8 different
 * counts.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class RecognitionIstanbulMeasure extends AbstractRecognitionMeasure
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
	public RecognitionIstanbulMeasure(InterfaceRecognizer recognizer)
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
	public RecognitionIstanbulMeasure(InterfaceRecognizer recognizer, List<EntityType> types)
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
	public RecognitionIstanbulMeasure(InterfaceRecognizer recognizer, List<EntityType> types, Mentions reference, Mentions estimation, List<ArticleCategory> categories)
	{	super(recognizer,types,reference,estimation,categories);
	}	

	@Override
	public AbstractRecognitionMeasure build(InterfaceRecognizer recognizer, List<EntityType> types, Mentions reference, Mentions estimation, List<ArticleCategory> categories)
	{	RecognitionIstanbulMeasure result = new RecognitionIstanbulMeasure(recognizer, types, reference, estimation, categories);
		return result;
	}

	@Override
	public AbstractRecognitionMeasure build(InterfaceRecognizer recognizer, List<EntityType> types)
	{	RecognitionIstanbulMeasure result = new RecognitionIstanbulMeasure(recognizer,types);
		return result;
	}

	@Override
	public AbstractRecognitionMeasure build(InterfaceRecognizer recognizer)
	{	RecognitionIstanbulMeasure result = new RecognitionIstanbulMeasure(recognizer);
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
	/** Perfectly estimated mention */
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
	/** Estimated limits do not intersect with any reference mention */
	public static final String COUNT_FP = "FalsePositive";
	/** Reference limits do not intersect with any estimated mention */
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
	protected void processCounts(Mentions referenceOrig, Mentions estimationOrig, List<ArticleCategory> categories) 
	{	// copy mention lists (those are going to be modified)
		List<AbstractMention<?>> reference = new ArrayList<AbstractMention<?>>(referenceOrig.getMentions());
		List<AbstractMention<?>> estimation = new ArrayList<AbstractMention<?>>(estimationOrig.getMentions());
		
		// remove the mentions whose type is not in the type list
		cleanMentions(reference);
		cleanMentions(estimation);
		
		// category mention lists
		for(ArticleCategory category: categories)
		{	for(String count: COUNTS)
			{	Map<ArticleCategory,List<AbstractMention<?>>> map = mentionsByCategory.get(count);
				List<AbstractMention<?>> list = new ArrayList<AbstractMention<?>>();
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
	 * Process the true positive mentions.
	 * 
	 * @param reference
	 * 		List of the mentions of reference.
	 * @param estimation
	 * 		List of the mentions detected by the recognizer.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processTruePositives(List<AbstractMention<?>> reference, List<AbstractMention<?>> estimation, List<ArticleCategory> categories)
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
					
					String countName = null;
					if(refType==estType)
						countName = COUNT_TP_CT;
					else
						countName = COUNT_TP_WT;
					
					// update spatial evaluation
					{	List<AbstractMention<?>> listAll = mentionsAll.get(countName);
						listAll.add(est);
						Map<EntityType,List<AbstractMention<?>>> mapByType = mentionsByType.get(countName);
						List<AbstractMention<?>> listByType = mapByType.get(refType);
						listByType.add(est);
						Map<ArticleCategory,List<AbstractMention<?>>> mapByCat = mentionsByCategory.get(countName);
						for(ArticleCategory category: categories)
						{	List<AbstractMention<?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Process the excess positive mentions.
	 * 
	 * @param reference
	 * 		List of the mentions of reference.
	 * @param estimation
	 * 		List of the mentions detected by the recognizer.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processExcessPositives(List<AbstractMention<?>> reference, List<AbstractMention<?>> estimation, List<ArticleCategory> categories)
	{	Iterator<AbstractMention<?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractMention<?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			
			Iterator<AbstractMention<?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractMention<?> est = itEst.next();
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
					{	List<AbstractMention<?>> listAll = mentionsAll.get(countName);
						listAll.add(est);
						Map<EntityType,List<AbstractMention<?>>> mapByType = mentionsByType.get(countName);
						List<AbstractMention<?>> listByType = mapByType.get(refType);
						listByType.add(est);
						Map<ArticleCategory,List<AbstractMention<?>>> mapByCat = mentionsByCategory.get(countName);
						for(ArticleCategory category: categories)
						{	List<AbstractMention<?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}
					}
				}
			}
		}
	}

	/**
	 * Process the partial positive mentions.
	 * 
	 * @param reference
	 * 		List of the mentions of reference.
	 * @param estimation
	 * 		List of the mentions detected by the recognizer.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processPartialPositives(List<AbstractMention<?>> reference, List<AbstractMention<?>> estimation, List<ArticleCategory> categories)
	{	Iterator<AbstractMention<?>> itRef = reference.iterator();
		while(itRef.hasNext())
		{	AbstractMention<?> ref = itRef.next();
			EntityType refType = ref.getType();
			boolean found = false;
			
			Iterator<AbstractMention<?>> itEst = estimation.iterator();
			while(itEst.hasNext() && !found)
			{	AbstractMention<?> est = itEst.next();
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
					{	List<AbstractMention<?>> listAll = mentionsAll.get(countName);
						listAll.add(est);
						Map<EntityType,List<AbstractMention<?>>> mapByType = mentionsByType.get(countName);
						List<AbstractMention<?>> listByType = mapByType.get(refType);
						listByType.add(est);
						Map<ArticleCategory,List<AbstractMention<?>>> mapByCat = mentionsByCategory.get(countName);
						for(ArticleCategory category: categories)
						{	List<AbstractMention<?>> listByCat = mapByCat.get(category);
							listByCat.add(est);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Process the false positive mentions.
	 * 
	 * @param estimation
	 * 		List of the mentions detected by the recognizer.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processFalsePositives(List<AbstractMention<?>> estimation, List<ArticleCategory> categories)
	{	for(AbstractMention<?> est: estimation)
		{	EntityType estType = est.getType();
			List<AbstractMention<?>> listAll = mentionsAll.get(COUNT_FN);
			listAll.add(est);
			Map<EntityType,List<AbstractMention<?>>> mapByType = mentionsByType.get(COUNT_FN);
			List<AbstractMention<?>> listByType = mapByType.get(estType);
			listByType.add(est);
			Map<ArticleCategory,List<AbstractMention<?>>> mapByCat = mentionsByCategory.get(COUNT_FN);
			for(ArticleCategory category: categories)
			{	List<AbstractMention<?>> listByCat = mapByCat.get(category);
				listByCat.add(est);
			}
		}
	}
	
	/**
	 * Process the false negative mentions.
	 * 
	 * @param reference
	 * 		List of the mentions of reference.
	 * @param categories
	 * 		Categories of the considered article.
	 */
	private void processFalseNegatives(List<AbstractMention<?>> reference, List<ArticleCategory> categories)
	{	for(AbstractMention<?> ref: reference)
		{	EntityType refType = ref.getType();
			List<AbstractMention<?>> listAll = mentionsAll.get(COUNT_FP);
			listAll.add(ref);
			Map<EntityType,List<AbstractMention<?>>> mapByType = mentionsByType.get(COUNT_FP);
			List<AbstractMention<?>> listByType = mapByType.get(refType);
			listByType.add(ref);
			Map<ArticleCategory,List<AbstractMention<?>>> mapByCat = mentionsByCategory.get(COUNT_FP);
			for(ArticleCategory category: categories)
			{	List<AbstractMention<?>> listByCat = mapByCat.get(category);
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
	private static final String FILE_NAME = "istanbul" + FileNames.EX_TEXT;
	
	@Override
	public String getFileName()
	{	return FILE_NAME;
	}
}
