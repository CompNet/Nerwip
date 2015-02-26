package tr.edu.gsu.nerwip.recognition.combiner.votebased;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleCategory;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.evaluation.measure.LilleMeasure;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.combiner.AbstractCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.CategoryProportions;
import tr.edu.gsu.nerwip.recognition.combiner.VoteWeights;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.Illinois;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.IllinoisModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe.LingPipe;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe.LingPipeModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp.OpenNlp;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp.OpenNlpModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.Stanford;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.StanfordModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais.OpenCalais;
import tr.edu.gsu.nerwip.recognition.internal.modelless.subee.Subee;
import tr.edu.gsu.nerwip.tools.file.FileNames;

/**
 * This combiner relies on a vote-based process SVM to perform its combination. 
 * The data required for votting must have been trained calculated before, in a 
 * separate process, using the class {@link VoteTrainer}, using the same settings.
 * <br/>
 * The NER tools handled by this combiner are:
 * <ul>
 * 		<li>Illinois NET (see {@link Illinois})</li>
 * 		<li>LingPipe (see {@link LingPipe})</li>
 * 		<li>OpenCalais (see {@link OpenCalais})</li>
 * 		<li>OpenNLP (see {@link OpenNlp})</li>
 * 		<li>Stanford NER (see {@link Stanford})</li>
 * 		<li>Subee (see {@link Subee})</li>
 * </ul>
 * Various options allow changing the behavior of this combiner:
 * <ul>
 * 		<li>{@code voteMode}: How the vote is performed: using uniform weights (each 
 * 		NER tool has the same importance) or using performance-related weights
 * 		(a NER tool has more importance if it was good on the test data). 
 * 		See {@link VoteMode}.</li>
 * 		<li>{@code existVote}: whether a vote should be conducted to determine
 * 		the existence of an entity. Otherwise, if at least one NER tool detects
 * 		something, we suppose an entity exist (increases the number of false 
 * 		positves).</li>
 * 		<li>subeeMode: whether to use our NER tool {@link Subee}, and if yes,
 * 		how to use it. See {@code SubeeMode}.</li>
 * 		<li>{@code useRecall}: whether or not recall should be used to process weights.</li>
 * </ul>
 * 
 * @author Yasa Akbulut
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public class VoteCombiner extends AbstractCombiner
{	
	/**
	 * Builds a new vote-based combiner.
	 *
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param specific 
	 *		Whether to use the standalone NER tools with their default models 
	 *		({@code false}), or ones specifically trained on our corpus ({@code true}).
	 * @param voteMode 
	 * 		Indicates how NER tools vote.
	 * @param useRecall
	 * 		 Indicates if recall should be used when voting.
	 * @param existVote
	 * 		Indicates if entity existence should be voted. 
	 * @param subeeMode
	 * 		Indicates how our NER tool {@link Subee} is used (if it is used). 
	 *
	 * @throws RecognizerException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	public VoteCombiner(boolean loadModelOnDemand, boolean specific, VoteMode voteMode, boolean useRecall, boolean existVote, SubeeMode subeeMode) throws RecognizerException
	{	super();
	
		this.specific = specific;
		this.voteMode = voteMode;
		this.useRecall = useRecall;
		this.existVote = existVote;
		this.subeeMode = subeeMode;
		
		initRecognizers();
		setSubCacheEnabled(cache);
		
		initConverter();
		
		if(!loadModelOnDemand)
			loadModel();
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public RecognizerName getName()
	{	return RecognizerName.VOTECOMBINER;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
		result = result + "_" + "spec="+specific;
		result = result + "_" + "mode="+voteMode.toString();
		result = result + "_" + "rec="+useRecall;
		result = result + "_" + "exvote="+existVote;
		result = result + "_" + "subee="+subeeMode.toString();
	
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
//		EntityType.DATE,
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
	@Override
	protected void initRecognizers() throws RecognizerException
	{	logger.increaseOffset();
		boolean loadModelOnDemand = true;
	
		// Date Extractor
//		{	logger.log("Init DateExtractor");
//			DateExtractor dateExtractor = new DateExtractor();
//			result.add(dateExtractor);
//		}
		
		// Illinois
		{	logger.log("Init Illinois NET");
			IllinoisModelName model = IllinoisModelName.CONLL_MODEL;
			if(specific)
				model = IllinoisModelName.NERWIP_MODEL;
			boolean trim = false;
			boolean ignorePronouns = false;
			boolean exclusionOn = true;
			Illinois illinois = new Illinois(model, loadModelOnDemand, trim, ignorePronouns, exclusionOn);
			recognizers.add(illinois);
		}
		
		// LingPipe
		{	logger.log("Init LingPipe");
			LingPipeModelName model = LingPipeModelName.PREDEFINED_MODEL;
			if(specific)
				model = LingPipeModelName.NERWIP_MODEL;
			boolean splitSentences = true;
			boolean trim = true;
			boolean ignorePronouns = true;
			boolean exclusionOn = false;
			LingPipe lingPipe = new LingPipe(model, loadModelOnDemand, splitSentences, trim, ignorePronouns, exclusionOn);
			recognizers.add(lingPipe);
		}
		
		// OpenCalais
		{	logger.log("Init OpenCalais");
			boolean ignorePronouns = true;
			boolean exclusionOn = false;
			OpenCalais openCalais = new OpenCalais(ignorePronouns, exclusionOn);
			recognizers.add(openCalais);
		}
		
		// OpenNLP
		{	logger.log("Init OpenNLP");
			boolean exclusionOn = true;
			boolean ignorePronouns = true;
			OpenNlpModelName model = OpenNlpModelName.ORIGINAL_MODEL;
			if(specific)
				model = OpenNlpModelName.NERWIP_MODEL;
			OpenNlp openNlp = new OpenNlp(model, loadModelOnDemand, exclusionOn, ignorePronouns);
			recognizers.add(openNlp);
		}
		
		// Stanford
		{	logger.log("Init Stanford NER");
			StanfordModelName model = StanfordModelName.CONLLMUC_MODEL;
			if(specific)
				model = StanfordModelName.NERWIP_MODEL;
			boolean ignorePronouns = false;
			boolean exclusionOn = false;
			Stanford stanford = new Stanford(model, loadModelOnDemand, ignorePronouns, exclusionOn);
			recognizers.add(stanford);
		}
		
		// Subee
		if(subeeMode!=SubeeMode.NONE)
		{	logger.log("Init Subee");
			boolean additionalOccurrences = subeeMode==SubeeMode.ALL;
			boolean useTitle = true;
			boolean notableType = true;
			boolean useAcronyms = true;
			boolean discardDemonyms = true;
			Subee subee = new Subee(additionalOccurrences,useTitle,notableType,useAcronyms,discardDemonyms);
			recognizers.add(subee);
		}
		
		logger.decreaseOffset();		
	}

	/////////////////////////////////////////////////////////////////
	// GENERAL MODEL	 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getModelPath()
	{	return FileNames.FO_VOTECOMBINER;
	}

	/**
	 * Loads the objects necessary to the combination process,
	 * including the vote weights.
	 * 
	 * @throws RecognizerException
	 * 		Problem while loading the model.
	 */
	private void loadModel() throws RecognizerException
	{	if(voteMode.hasWeights())
		{	loadVoteWeights();
			if(voteMode==VoteMode.WEIGHTED_CATEGORY)
				loadCategoryProportions();
			else
				categoryProportions = CategoryProportions.buildUniformProportions();
		}
		else
		{	voteWeights = VoteWeights.buildUniformWeights(recognizers);
			categoryProportions = CategoryProportions.buildUniformProportions();
		}
	}
	
    /**
     * Checks whether the combiner weights has been
     * already loaded.
     * 
     * @return
     * 		{@code true} iff the combiner has already been loaded.
    */
	private boolean isLoaded()
	{	boolean result = voteWeights!=null && categoryProportions!=null;
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// VOTE MODE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates how the vote should take place */
	private VoteMode voteMode;
	/** Whether the recall measures should be used or not */
	private boolean useRecall;
	/** Whether a vote should happen for entity existence, or not */
	private boolean existVote;

	/**
	 * Enumeration used to configure how
	 * the vote is performed.
	 * 
	 * @author Vincent Labatut
	 */
	public enum VoteMode
	{	/** Each NER tool accounts for one vote */
		UNIFORM("Unif"),
		/** Overall scores are used to determine vote weights */
		WEIGHTED_OVERALL("WghtOvrl"),
		/** Category-related scores are used to determine vote weights */
		WEIGHTED_CATEGORY("WghtCat");

		/** String representing the parameter value */
		private String name;
		
		/**
		 * Builds a new vote mode value
		 * to be used as a parameter.
		 * 
		 * @param name
		 * 		String representing the parameter value.
		 */
		VoteMode(String name)
		{	this.name = name;
		}
		
		/**
		 * Indicates if this vote mode requires weights.
		 * 
		 * @return
		 * 		{@code true} if vote weights are required.
		 */
		public boolean hasWeights()
		{	boolean result = this==WEIGHTED_OVERALL
				|| this==WEIGHTED_CATEGORY;
			return result;
		}
		
		@Override
		public String toString()
		{	return name;
		}
	}
	
	/**
	 * Returns the mode used to combine entities.
	 * 
	 * @return
	 * 		A symbol representing how entities are combined.
	 */
	public VoteMode getVoteMode()
	{	return voteMode;
	}

	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Entities combineEntities(Article article, Map<AbstractRecognizer,Entities> entities, StringBuffer rawOutput) throws RecognizerException
	{	logger.increaseOffset();
		String text = article.getRawText();
		Entities result = new Entities(getName());
		
		// possibly load the necessary data
		if(!isLoaded())
			loadModel();
		
		// get overlapping entities
		logger.log("Get the list of overlapping entities");
		List<Map<AbstractRecognizer, AbstractEntity<?>>> overlaps = Entities.identifyOverlaps(entities);
		
		// process the weights associated to article categories
		Map<ArticleCategory,Float> categoryWeights = categoryProportions.processCategoryWeights(article);
		
		// compare/combine them
		logger.log("Process each group of entities");
		logger.increaseOffset();
		for(Map<AbstractRecognizer, AbstractEntity<?>> map: overlaps)
		{	logger.log(map.values().toString());
			logger.increaseOffset();
			
			// add overlap to raw output
			rawOutput.append("Overlap:\n");
			for(Entry<AbstractRecognizer, AbstractEntity<?>> entry: map.entrySet())
			{	AbstractRecognizer recognizer = entry.getKey();
				AbstractEntity<?> entity = entry.getValue();
				rawOutput.append("\t"+recognizer+": "+entity+"\n");
			}
			
			// determine entity existence
			boolean existence = voteForExistence(article,categoryWeights, map);
			rawOutput.append("Existence="+existence+"\n");
			
			if(existence)
			{	// determine entity position
				int pos[] = voteForPosition(article,categoryWeights, map);
				rawOutput.append("Position=("+pos[0]+","+pos[1]+")\n");
				
				// determine entity type
				EntityType type = voteForType(article,categoryWeights, map);
				rawOutput.append("Type="+type+"\n");
				
				// build new, appropriate entity
				int startPos = pos[0];
				int endPos = pos[1];
				String valueStr = text.substring(startPos,endPos);
				AbstractEntity<?> entity = AbstractEntity.build(type, startPos, endPos, RecognizerName.VOTECOMBINER, valueStr);
				result.addEntity(entity);
				rawOutput.append(">> Entity="+endPos+"\n\n");
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		
	    logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Combine the NER tools results, in order to determine if
	 * the group of estimated entities corresponds to an actual
	 * entity.
	 * 
	 * @param article
	 * 		Concerned article. 
	 * @param categoryWeights
	 * 		Weights associated to the article categories. 
	 * @param map 
	 * 		Group of estimated entities.
	 * @return 
	 * 		{@code true} iff the conclusion is that the entity is correct.
	 */
	protected boolean voteForExistence(Article article, Map<ArticleCategory,Float> categoryWeights, Map<AbstractRecognizer, AbstractEntity<?>> map)
	{	logger.log("Start voting for existence:");
		logger.increaseOffset();
		boolean result = false;
		
		if(existVote)
		{	float voteFor = 0;
			float voteAgainst = 0;
			
			for(AbstractRecognizer recognizer: recognizers)
			{	AbstractEntity<?> entity = map.get(recognizer);
				// existence
				if(entity==null)
				{	float conWeight;
					if(voteMode==VoteMode.UNIFORM)
						conWeight = 1f;
					else
					{	if(useRecall)
							conWeight = voteWeights.processVotingWeight(article, recognizer, LilleMeasure.SCORE_TR, categoryWeights);
						else
							conWeight = voteWeights.processVotingWeight(article, recognizer, LilleMeasure.SCORE_TP, categoryWeights);
					}
					voteAgainst = voteAgainst + conWeight;
				}
				else
				{	float proWeight;
					if(voteMode==VoteMode.UNIFORM)
						proWeight = 1f;
					else
						proWeight = voteWeights.processVotingWeight(article, recognizer, LilleMeasure.SCORE_TP, categoryWeights);
					voteFor = voteFor + proWeight;
				}
			}
			
			float total = voteFor - voteAgainst;
			result = total>=0;
			logger.log("Votes: FOR="+voteFor+" - AGAINST="+voteAgainst+" = "+total+" >> "+result);
		}
		
		else
			result = true;
		
		logger.decreaseOffset();
		logger.log("Result of the vote for existence: "+result);
		return result;
	}

	/**
	 * Combine the NER tools results, in order to determine the
	 * position of the entity represented by the specified group.
	 * 
	 * @param article
	 * 		Concerned article. 
	 * @param categoryWeights
	 * 		Weights associated to the article categories. 
	 * @param map 
	 * 		Group of estimated entities.
	 * @return 
	 * 		An array of two integers corresponding to the entity position.
	 */
	protected int[] voteForPosition(Article article, Map<ArticleCategory,Float> categoryWeights, Map<AbstractRecognizer, AbstractEntity<?>> map)
	{	logger.log("Start voting for position:");
		logger.increaseOffset();
		Map<Integer,Float> startScores = new HashMap<Integer, Float>();
		Map<Integer,Float> endScores = new HashMap<Integer, Float>();
		
		// pro votes
		for(AbstractRecognizer recognizer: recognizers)
		{	AbstractEntity<?> entity = map.get(recognizer);
		
			// check existence
			if(entity!=null)
			{	// retrieve weight
				float proWeight;
				if(voteMode==VoteMode.UNIFORM)
					proWeight = 1f;
				else
					proWeight = voteWeights.processVotingWeight(article, recognizer, LilleMeasure.SCORE_FP, categoryWeights);
				
				// start position
				int startPos = entity.getStartPos();
				Float startScore = startScores.get(startPos);
				if(startScore==null)
					startScore = 0f;
				startScore = startScore + proWeight;
				startScores.put(startPos,startScore);
				
				// end position
				int endPos = entity.getEndPos();
				Float endScore = endScores.get(endPos);
				if(endScore==null)
					endScore = 0f;
				endScore = endScore + proWeight;
				endScores.put(endPos,endScore);
			}
		}
		
		// con votes
		if(useRecall)
		{	for(AbstractRecognizer recognizer: recognizers)
			{	AbstractEntity<?> entity = map.get(recognizer);
			
				// check existence
				if(entity!=null)
				{	// retrieve weight
					float conWeight;
					if(voteMode==VoteMode.UNIFORM)
						conWeight = 1f;
					else
						conWeight = voteWeights.processVotingWeight(article, recognizer, LilleMeasure.SCORE_FR, categoryWeights);
					
					// start position
					{	int startPos = entity.getStartPos();
						List<Entry<Integer,Float>> entries = new ArrayList<Entry<Integer,Float>>(startScores.entrySet());
						for(Entry<Integer,Float> entry: entries)
						{	int pos = entry.getKey();
							float score = entry.getValue();
							if(pos!=startPos)
							{	score = score - conWeight;
								startScores.put(pos,score);
							}
						}
					}
					
					// end position
					{	int endPos = entity.getEndPos();
						List<Entry<Integer,Float>> entries = new ArrayList<Entry<Integer,Float>>(endScores.entrySet());
						for(Entry<Integer,Float> entry: entries)
						{	int pos = entry.getKey();
							float score = entry.getValue();
							if(pos!=endPos)
							{	score = score - conWeight;
								endScores.put(pos,score);
							}
						}
					}
				}
			}
		}
		
		int result[] = getPositionFromScores(startScores,endScores);
		logger.decreaseOffset();
		logger.log("Result of the vote for position: startPos="+result[0]+", endPos="+result[1]);
		return result;
	}

	/**
	 * Combine the NER tools results, in order to determine the
	 * type of the entity represented by the specified group.
	 * 
	 * @param article
	 * 		Concerned article. 
	 * @param categoryWeights
	 * 		Weights associated to the article categories. 
	 * @param map 
	 * 		Group of estimated entities.
	 * @return 
	 * 		Type of the entity represnted by the group.
	 */
	protected EntityType voteForType(Article article, Map<ArticleCategory,Float> categoryWeights, Map<AbstractRecognizer, AbstractEntity<?>> map)
	{	logger.log("Start voting for type: ");
		logger.increaseOffset();
		Map<EntityType,Float> typeScores = new HashMap<EntityType, Float>();
		
		// pro votes
		for(AbstractRecognizer recognizer: recognizers)
		{	AbstractEntity<?> entity = map.get(recognizer);
			
			// retrieve weight
			float proWeight;
			if(voteMode==VoteMode.UNIFORM)
				proWeight = 1f;
			else
				proWeight = voteWeights.processVotingWeight(article, recognizer, LilleMeasure.SCORE_TP, categoryWeights);
			
			if(entity!=null)
			{	EntityType type = entity.getType();
				Float typeScore = typeScores.get(type);
				if(typeScore==null)
					typeScore = 0f;
				typeScore = typeScore + proWeight;
				typeScores.put(type,typeScore);
			}
		}
		
		// con votes
		if(useRecall)
		{	for(AbstractRecognizer recognizer: recognizers)
			{	AbstractEntity<?> entity = map.get(recognizer);
					
				// retrieve weight
				float conWeight;
				if(voteMode==VoteMode.UNIFORM)
					conWeight = 1f;
				else
					conWeight = voteWeights.processVotingWeight(article, recognizer, LilleMeasure.SCORE_TR, categoryWeights);
					
				if(entity!=null)
				{	EntityType type = entity.getType();
					List<Entry<EntityType,Float>> entries = new ArrayList<Entry<EntityType,Float>>(typeScores.entrySet());
					for(Entry<EntityType,Float> entry: entries)
					{	EntityType t = entry.getKey();
						float score = entry.getValue();
						if(t!=type)
						{	score = score - conWeight;
							typeScores.put(t,score);
						}
					}
				}
			}
		}
		
		//display votes
		String line = "vote results: ";
		List<EntityType> types = new ArrayList<EntityType>(typeScores.keySet());
		Collections.sort(types);
		for(EntityType type: types)
		{	float vote = typeScores.get(type);
			line = line + type.toString()+"("+vote+"); ";
		}
		logger.log(line);
		
		List<EntityType> keys = getSortedKeys(typeScores);
		EntityType result = keys.get(keys.size()-1);
		logger.decreaseOffset();
		logger.log("Result of the vote for type: "+result);
		return result;
	}
}
