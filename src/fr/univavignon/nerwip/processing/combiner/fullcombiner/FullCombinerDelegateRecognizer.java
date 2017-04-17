package fr.univavignon.nerwip.processing.combiner.fullcombiner;

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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.combiner.AbstractCombinerDelegateRecognizer;
import fr.univavignon.nerwip.processing.combiner.svmbased.CombineMode;
import fr.univavignon.nerwip.processing.combiner.svmbased.SvmCombiner;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteCombiner;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteMode;
import fr.univavignon.nerwip.processing.internal.modelless.wikipediadater.WikipediaDater;

/**
 * This combinerName is very basic: it first applies our date
 * detector to identify mentions which are quasi-certainly
 * dates, then uses one of the other combiners for the other
 * types of entities. There is no training for this combinerName,
 * the training is performed at the level of the combinerName
 * this one is built upon. 
 * <br/>
 * The recognizers handled by this combinerName are:
 * <ul>
 * 		<li>WikipediaDater to detect dates (see {@link WikipediaDater})</li>
 * 		<li>Either SvmCombiner or VoteCombiner to detect locations, organizations 
 * 			and persons (see {@link SvmCombiner} and {@link VoteCombiner})</li>
 * </ul>
 * Various options allow changing the behavior of this combinerName:
 * <ul>
 * 		<li>{@code combinerName}: combinerName used for the location, organization
 * 			and person mentions (i.e. either the SVM- or vote-based combinerName).</li>
 * </ul>
 * 
 * @author Vincent Labatut
 */
class FullCombinerDelegateRecognizer extends AbstractCombinerDelegateRecognizer
{	
	/**
	 * Builds a new full combinerName.
	 *
	 * @param fullCombiner
	 * 		Recognizer in charge of this delegate.
	 * @param combinerName
	 * 		CombinerName used to handle locations, organizations and persons
	 * 		(either SVM- or vote-based).
	 *  
	 * @throws ProcessorException
	 * 		Problem while loading some combinerName or tokenizer.
	 */
	public FullCombinerDelegateRecognizer(FullCombiner fullCombiner, CombinerName combinerName) throws ProcessorException
	{	super(fullCombiner);
		
		this.combinerName = combinerName;
		
		initRecognizers();
		setSubCacheEnabled(recognizer.doesCache());
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
		result = result + "_" + "combi="+combinerName.toString();
		
//		result = result + "_" + "trim=" + trim;
//		result = result + "_" + "ignPro=" + ignorePronouns;
//		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types recognized by this combinerName */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList(
		EntityType.DATE,
		EntityType.LOCATION,
		EntityType.ORGANIZATION,
		EntityType.PERSON
	);
	
	@Override
	public List<EntityType> getHandledEntityTypes()
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages recognized by this combinerName */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList(
		ArticleLanguage.EN
//		ArticleLanguage.FR
	);
	
	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RECOGNIZERS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** CombinerName used for locations, organizations and persons */
	private CombinerName combinerName;
	
	@Override
	protected void initRecognizers() throws ProcessorException
	{	logger.increaseOffset();
		boolean loadModelOnDemand = true;
	
		// Wikipedia Dater
		{	logger.log("Init Wikipedia Dater (Dates only)");
			WikipediaDater wikipediaDater = new WikipediaDater();
			recognizers.add(wikipediaDater);
		}
		
		// other combinerName
		logger.log("Init the other combinerName (Loc+Org+Per)");
		if(combinerName==CombinerName.SVM)
		{	logger.log("SVM-based combinerName selected");
			boolean specific = true;
			boolean useCategories = true;
			CombineMode combineMode = CombineMode.CHUNK_PREVIOUS;
			SubeeMode subeeMode = SubeeMode.ALL;
			SvmCombiner svmCombiner = new SvmCombiner(loadModelOnDemand, specific, useCategories, combineMode, subeeMode);
			recognizers.add(svmCombiner);
		}
		else
		{	logger.log("Vote-based combinerName selected");
			boolean specific = true;
			VoteMode voteMode = VoteMode.WEIGHTED_CATEGORY;
			boolean useRecall = true;
			boolean existVote = true;
			SubeeMode subeeMode = SubeeMode.ALL;
			VoteCombiner voteCombiner = new VoteCombiner(loadModelOnDemand, specific, voteMode, useRecall, existVote, subeeMode);
			recognizers.add(voteCombiner);
		}
		
		logger.decreaseOffset();		
	}

	/////////////////////////////////////////////////////////////////
	// GENERAL MODEL	 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getModelPath()
	{	return null; // no model here
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Mentions combineMentions(Article article, Map<InterfaceRecognizer,Mentions> mentions, StringBuffer rawOutput) throws ProcessorException
	{	logger.increaseOffset();
		Mentions result = new Mentions(recognizer.getName());
		Iterator<InterfaceRecognizer> it = recognizers.iterator();
		
		// first get the dates
		InterfaceRecognizer wikipediaDater = it.next();
		Mentions dates = mentions.get(wikipediaDater);
		result.addMentions(dates);
		
		// then add the rest of the (non-overlapping) mentions
		InterfaceRecognizer combiner = it.next();
		Mentions ents = mentions.get(combiner);
		List<AbstractMention<?>> mentList = ents.getMentions();
		for(AbstractMention<?> mention: mentList)
		{	if(!result.isMentionOverlapping(mention))
				result.addMention(mention);
		}
		
		logger.decreaseOffset();
		return result;
	}
}
