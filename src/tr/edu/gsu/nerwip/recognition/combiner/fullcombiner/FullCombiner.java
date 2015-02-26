package tr.edu.gsu.nerwip.recognition.combiner.fullcombiner;

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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.combiner.AbstractCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner.CombineMode;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner.VoteMode;
import tr.edu.gsu.nerwip.recognition.internal.modelless.wikipediadater.WikipediaDater;

/**
 * This combiner is very basic: it first applies our very good
 * date detector to identify entities which are quasi-certainly
 * dates, then uses one of the other combiners for the other
 * types of entities. There is no training for this combiner,
 * the training is performed at the level of the combiner
 * this one is built upon. 
 * <br/>
 * The NER tools handled by this combiner are:
 * <ul>
 * 		<li>WikipediaDater to detect dates (see {@link WikipediaDater})</li>
 * 		<li>Either SvmCombiner or VoteCombiner to detect locations, organizations 
 * 			and persons (see {@link SvmCombiner} and {@link VoteCombiner})</li>
 * </ul>
 * Various options allow changing the behavior of this combiner:
 * <ul>
 * 		<li>{@code combiner}: combiner used for the location, organization
 * 			and person entities (i.e. either the SVM- or vote-based combiner).</li>
 * </ul>
 * 
 * @author Vincent Labatut
 */
public class FullCombiner extends AbstractCombiner
{	
	/**
	 * Builds a new overall combiner.
	 *
	 * @param combiner
	 * 		Combiner used to handle locations, organizations and persons
	 * 		(either SVM- or vote-based).
	 *  
	 * @throws RecognizerException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	public FullCombiner(Combiner combiner) throws RecognizerException
	{	super();
		
		this.combiner = combiner;
		
		initRecognizers();
		setSubCacheEnabled(cache);

		initConverter();
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public RecognizerName getName()
	{	return RecognizerName.SVMCOMBINER;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
		result = result + "_" + "combi="+combiner.toString();
		
//		result = result + "_" + "trim=" + trim;
//		result = result + "_" + "ignPro=" + ignorePronouns;
//		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entities recognized by this combiner */
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
	// TOOLS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Combiner used for locations, organizations and persons */
	private Combiner combiner;
	
	/**
	 * Represents the combiner used to process locations,
	 * organizations and persons.
	 * 
	 * @author Vincent Labatut
	 */
	public enum Combiner
	{	/** Use the best SVM-based combiner configuration */
		SVM("svm"),
		/** Use the best vote-based combiner configuration */
		VOTE("vote");
		
		/** String representing the parameter value */
		private String name;
		
		/**
		 * Builds a new Combiner value
		 * to be used as a parameter.
		 * 
		 * @param name
		 * 		String representing the parameter value.
		 */
		Combiner(String name)
		{	this.name = name;
		}
		
		@Override
		public String toString()
		{	return name;
		}
	}
	
	@Override
	protected void initRecognizers() throws RecognizerException
	{	logger.increaseOffset();
		boolean loadModelOnDemand = true;
	
		// Wikipedia Dater
		{	logger.log("Init Wikipedia Dater (Dates only)");
			WikipediaDater wikipediaDater = new WikipediaDater();
			recognizers.add(wikipediaDater);
		}
		
		// other combiner
		logger.log("Init the other combiner (Loc+Org+Per)");
		if(combiner==Combiner.SVM)
		{	logger.log("SVM-based combiner selected");
			boolean specific = true;
			boolean useCategories = true;
			CombineMode combineMode = CombineMode.CHUNK_PREVIOUS;
			SubeeMode subeeMode = SubeeMode.ALL;
			SvmCombiner svmCombiner = new SvmCombiner(loadModelOnDemand, specific, useCategories, combineMode, subeeMode);
			recognizers.add(svmCombiner);
		}
		else
		{	logger.log("Vote-based combiner selected");
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
	protected Entities combineEntities(Article article, Map<AbstractRecognizer,Entities> entities, StringBuffer rawOutput) throws RecognizerException
	{	logger.increaseOffset();
		Entities result = new Entities(getName());
		
		// first get the dates
		AbstractRecognizer wikipediaDater = recognizers.get(0);
		Entities dates = entities.get(wikipediaDater);
		result.addEntities(dates);
		
		// then add the rest of the (non-overlapping) entities
		AbstractRecognizer combiner = recognizers.get(1);
		Entities ents = entities.get(combiner);
		List<AbstractEntity<?>> entList = ents.getEntities();
		for(AbstractEntity<?> entity: entList)
		{	if(!result.isEntityOverlapping(entity))
				result.addEntity(entity);
		}
		
		logger.decreaseOffset();
		return result;
	}
}
