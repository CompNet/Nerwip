package fr.univavignon.nerwip.processing.combiner.votebased;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleCategory;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.evaluation.recognition.measures.RecognitionLilleMeasure;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.combiner.AbstractCombinerDelegateRecognizer;
import fr.univavignon.nerwip.processing.combiner.CategoryProportions;
import fr.univavignon.nerwip.processing.combiner.VoteWeights;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.Illinois;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.IllinoisModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipe;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipeModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlp;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlpModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.Stanford;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.StanfordModelName;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalais;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalaisLanguage;
import fr.univavignon.nerwip.processing.internal.modelless.subee.Subee;
import fr.univavignon.nerwip.tools.file.FileNames;

/**
 * This combiner relies on a vote-based process SVM to perform its combination. 
 * The data required for votting must have been trained calculated before, in a 
 * separate process, using the class {@link VoteTrainer}, using the same settings.
 * <br/>
 * The recognizers handled by this combiner are:
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
 * 		recognizer has the same importance) or using performance-related weights
 * 		(a recognizer has more importance if it was good on the test data). 
 * 		See {@link VoteMode}.</li>
 * 		<li>{@code existVote}: whether a vote should be conducted to determine
 * 		the existence of a mention. Otherwise, if at least one recognizer detects
 * 		something, we suppose a mention exists (increases the number of false 
 * 		positves).</li>
 * 		<li>subeeMode: whether to use our recognizer {@link Subee}, and if yes,
 * 		how to use it. See {@code SubeeMode}.</li>
 * 		<li>{@code useRecall}: whether or not recall should be used to process weights.</li>
 * </ul>
 * 
 * @author Yasa Akbulut
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
class VoteCombinerDelegateRecognizer extends AbstractCombinerDelegateRecognizer
{	
	/**
	 * Builds a new vote-based combiner.
	 *
	 * @param voteCombiner
	 * 		Recognizer in charge of this delegate.
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param specific 
	 *		Whether to use the standalone recognizers with their default models 
	 *		({@code false}), or ones specifically trained on our corpus ({@code true}).
	 * @param voteMode 
	 * 		Indicates how recognizers vote.
	 * @param useRecall
	 * 		 Indicates if recall should be used when voting.
	 * @param existVote
	 * 		Indicates if mention existence should be voted. 
	 * @param subeeMode
	 * 		Indicates how our recognizer {@link Subee} is used (if it is used). 
	 *
	 * @throws ProcessorException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	public VoteCombinerDelegateRecognizer(VoteCombiner voteCombiner, boolean loadModelOnDemand, boolean specific, VoteMode voteMode, boolean useRecall, boolean existVote, SubeeMode subeeMode) throws ProcessorException
	{	super(voteCombiner);
	
		this.specific = specific;
		this.voteMode = voteMode;
		this.useRecall = useRecall;
		this.existVote = existVote;
		this.subeeMode = subeeMode;
		
		initRecognizers();
		setSubCacheEnabled(recognizer.doesCache());
		
		if(!loadModelOnDemand)
			loadModel();
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
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
	/** List of entity types recognized by this combiner */
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
	// LANGUAGES	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages recognized by this combiner */
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
	@Override
	protected void initRecognizers() throws ProcessorException
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
			OpenCalaisLanguage lang = OpenCalaisLanguage.EN;
			boolean ignorePronouns = true;
			boolean exclusionOn = false;
			OpenCalais openCalais = new OpenCalais(lang, ignorePronouns, exclusionOn);
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
	 * @throws ProcessorException
	 * 		Problem while loading the model.
	 */
	private void loadModel() throws ProcessorException
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
	/** Whether a vote should happen for mention existence, or not */
	private boolean existVote;
	
	/**
	 * Returns the mode used to combine mentions.
	 * 
	 * @return
	 * 		A symbol representing how mentions are combined.
	 */
	public VoteMode getVoteMode()
	{	return voteMode;
	}

	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Mentions combineMentions(Article article, Map<InterfaceRecognizer,Mentions> mentions, StringBuffer rawOutput) throws ProcessorException
	{	logger.increaseOffset();
		ArticleLanguage language = article.getLanguage();
		ProcessorName recognizerName = recognizer.getName();
		String text = article.getRawText();
		Mentions result = new Mentions(recognizerName);
		
		// possibly load the necessary data
		if(!isLoaded())
			loadModel();
		
		// get overlapping mentions
		logger.log("Get the list of overlapping mentions");
		List<Map<InterfaceRecognizer, AbstractMention<?>>> overlaps = Mentions.identifyOverlaps(mentions);
		
		// process the weights associated to article categories
		Map<ArticleCategory,Float> categoryWeights = categoryProportions.processCategoryWeights(article);
		
		// compare/combine them
		logger.log("Process each group of mentions");
		logger.increaseOffset();
		for(Map<InterfaceRecognizer, AbstractMention<?>> map: overlaps)
		{	logger.log(map.values().toString());
			logger.increaseOffset();
			
			// add overlap to raw output
			rawOutput.append("Overlap:\n");
			for(Entry<InterfaceRecognizer, AbstractMention<?>> entry: map.entrySet())
			{	InterfaceRecognizer recognizer = entry.getKey();
				AbstractMention<?> mention = entry.getValue();
				rawOutput.append("\t"+recognizer+": "+mention+"\n");
			}
			
			// determine mention existence
			boolean existence = voteForExistence(article,categoryWeights, map);
			rawOutput.append("Existence="+existence+"\n");
			
			if(existence)
			{	// determine mention position
				int pos[] = voteForPosition(article,categoryWeights, map);
				rawOutput.append("Position=("+pos[0]+","+pos[1]+")\n");
				
				// determine entity type
				EntityType type = voteForType(article,categoryWeights, map);
				rawOutput.append("Type="+type+"\n");
				
				// build new, appropriate mention
				int startPos = pos[0];
				int endPos = pos[1];
				String valueStr = text.substring(startPos,endPos);
				AbstractMention<?> mention;
				if(type.isNamed())
					mention = AbstractMention.build(type, startPos, endPos, recognizerName, valueStr, language);
				else
				{	Comparable<?> value = voteForValue(map, type);
					rawOutput.append(">> Value="+value+"\n");
					mention = AbstractMention.build(type, startPos, endPos, recognizerName, valueStr, value);
				}
				result.addMention(mention);
				rawOutput.append(">> Mention="+endPos+"\n\n");
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		
	    logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Combine the recognizers results, in order to determine if
	 * the group of estimated mentions corresponds to an actual
	 * mention.
	 * 
	 * @param article
	 * 		Concerned article. 
	 * @param categoryWeights
	 * 		Weights associated to the article categories. 
	 * @param map 
	 * 		Group of estimated mentions.
	 * @return 
	 * 		{@code true} iff the conclusion is that the mention is correct.
	 */
	protected boolean voteForExistence(Article article, Map<ArticleCategory,Float> categoryWeights, Map<InterfaceRecognizer, AbstractMention<?>> map)
	{	logger.log("Start voting for existence:");
		logger.increaseOffset();
		boolean result = false;
		
		if(existVote)
		{	float voteFor = 0;
			float voteAgainst = 0;
			
			for(InterfaceRecognizer recognizer: recognizers)
			{	AbstractMention<?> mention = map.get(recognizer);
				// existence
				if(mention==null)
				{	float conWeight;
					if(voteMode==VoteMode.UNIFORM)
						conWeight = 1f;
					else
					{	if(useRecall)
							conWeight = voteWeights.processVotingWeight(article, recognizer, RecognitionLilleMeasure.SCORE_TR, categoryWeights);
						else
							conWeight = voteWeights.processVotingWeight(article, recognizer, RecognitionLilleMeasure.SCORE_TP, categoryWeights);
					}
					voteAgainst = voteAgainst + conWeight;
				}
				else
				{	float proWeight;
					if(voteMode==VoteMode.UNIFORM)
						proWeight = 1f;
					else
						proWeight = voteWeights.processVotingWeight(article, recognizer, RecognitionLilleMeasure.SCORE_TP, categoryWeights);
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
	 * Combine the recognizers results, in order to determine the
	 * position of the mention represented by the specified group.
	 * 
	 * @param article
	 * 		Concerned article. 
	 * @param categoryWeights
	 * 		Weights associated to the article categories. 
	 * @param map 
	 * 		Group of estimated mentions.
	 * @return 
	 * 		An array of two integers corresponding to the mention position.
	 */
	protected int[] voteForPosition(Article article, Map<ArticleCategory,Float> categoryWeights, Map<InterfaceRecognizer, AbstractMention<?>> map)
	{	logger.log("Start voting for position:");
		logger.increaseOffset();
		Map<Integer,Float> startScores = new HashMap<Integer, Float>();
		Map<Integer,Float> endScores = new HashMap<Integer, Float>();
		
		// pro votes
		for(InterfaceRecognizer recognizer: recognizers)
		{	AbstractMention<?> mention = map.get(recognizer);
		
			// check existence
			if(mention!=null)
			{	// retrieve weight
				float proWeight;
				if(voteMode==VoteMode.UNIFORM)
					proWeight = 1f;
				else
					proWeight = voteWeights.processVotingWeight(article, recognizer, RecognitionLilleMeasure.SCORE_FP, categoryWeights);
				
				// start position
				int startPos = mention.getStartPos();
				Float startScore = startScores.get(startPos);
				if(startScore==null)
					startScore = 0f;
				startScore = startScore + proWeight;
				startScores.put(startPos,startScore);
				
				// end position
				int endPos = mention.getEndPos();
				Float endScore = endScores.get(endPos);
				if(endScore==null)
					endScore = 0f;
				endScore = endScore + proWeight;
				endScores.put(endPos,endScore);
			}
		}
		
		// con votes
		if(useRecall)
		{	for(InterfaceRecognizer recognizer: recognizers)
			{	AbstractMention<?> mention = map.get(recognizer);
			
				// check existence
				if(mention!=null)
				{	// retrieve weight
					float conWeight;
					if(voteMode==VoteMode.UNIFORM)
						conWeight = 1f;
					else
						conWeight = voteWeights.processVotingWeight(article, recognizer, RecognitionLilleMeasure.SCORE_FR, categoryWeights);
					
					// start position
					{	int startPos = mention.getStartPos();
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
					{	int endPos = mention.getEndPos();
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
	 * Combine the recognizers results, in order to determine the
	 * type of the mention represented by the specified group.
	 * 
	 * @param article
	 * 		Concerned article. 
	 * @param categoryWeights
	 * 		Weights associated to the article categories. 
	 * @param map 
	 * 		Group of estimated mentions.
	 * @return 
	 * 		Type of the mention represnted by the group.
	 */
	protected EntityType voteForType(Article article, Map<ArticleCategory,Float> categoryWeights, Map<InterfaceRecognizer, AbstractMention<?>> map)
	{	logger.log("Start voting for type: ");
		logger.increaseOffset();
		Map<EntityType,Float> typeScores = new HashMap<EntityType, Float>();
		
		// pro votes
		for(InterfaceRecognizer recognizer: recognizers)
		{	AbstractMention<?> mention = map.get(recognizer);
			
			// retrieve weight
			float proWeight;
			if(voteMode==VoteMode.UNIFORM)
				proWeight = 1f;
			else
				proWeight = voteWeights.processVotingWeight(article, recognizer, RecognitionLilleMeasure.SCORE_TP, categoryWeights);
			
			if(mention!=null)
			{	EntityType type = mention.getType();
				Float typeScore = typeScores.get(type);
				if(typeScore==null)
					typeScore = 0f;
				typeScore = typeScore + proWeight;
				typeScores.put(type,typeScore);
			}
		}
		
		// con votes
		if(useRecall)
		{	for(InterfaceRecognizer recognizer: recognizers)
			{	AbstractMention<?> mention = map.get(recognizer);
					
				// retrieve weight
				float conWeight;
				if(voteMode==VoteMode.UNIFORM)
					conWeight = 1f;
				else
					conWeight = voteWeights.processVotingWeight(article, recognizer, RecognitionLilleMeasure.SCORE_TR, categoryWeights);
					
				if(mention!=null)
				{	EntityType type = mention.getType();
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
