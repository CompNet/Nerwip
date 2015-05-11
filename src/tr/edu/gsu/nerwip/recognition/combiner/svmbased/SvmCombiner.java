package tr.edu.gsu.nerwip.recognition.combiner.svmbased;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleCategory;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.data.entity.PositionRelation;
import tr.edu.gsu.nerwip.evaluation.measure.LilleMeasure;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.combiner.AbstractCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.CategoryProportions;
import tr.edu.gsu.nerwip.recognition.combiner.VoteWeights;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner;
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
 * This combiner relies on a SVM to perform its combination. 
 * The SVM must have been trained before, in a separate process, 
 * using the class {@link SvmTrainer} on the same settings.
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
 * 		<li>{@code combineMode}: How the combination is performed. It can be
 * 		entity-by-entity or word-by-word. In the former case, the SVM
 * 		cannot handle entity positions, so it is resolved through a
 * 		voting process, not unlike what is performed by {@link VoteCombiner}.
 * 		Various vote processes are proposed, see {@link CombineMode}.</li>
 * 		<li>{@code useCategories}: whether the SVM should use article categories
 * 		as input, to try improving its prediction. It is independent from
 * 		whether categories are used or not during the voting process.</li>
 * 		<li>{@code subeeMode}: whether to use our NER tool {@link Subee}, and if yes,
 * 		how to use it. See {@code SubeeMode}.</li>
 * 		<li>{@code useRecall}: whether or not recall should be used, in the case
 * 		there is some voting involved in the combination.</li>
 * </ul>
 * 
 * @author Vincent Labatut
 */
public class SvmCombiner extends AbstractCombiner
{	
	/**
	 * Builds a new SVM-based combiner.
	 *
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param specific 
	 *		Whether to use the standalone NER tools with their default models 
	 *		({@code false}), or ones specifically trained on our corpus ({@code true}).
	 * @param useCategories 
	 * 		Indicates if categories should be used when combining entities.
	 * @param combineMode
	 * 		 Indicates how entities should be combined.
	 * @param subeeMode
	 * 		Indicates how our NER tool {@link Subee} is used (if it's used). 
	 *
	 * @throws RecognizerException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	public SvmCombiner(boolean loadModelOnDemand, boolean specific, boolean useCategories, CombineMode combineMode, SubeeMode subeeMode) throws RecognizerException
	{	super();
		
		this.specific = specific;
		this.useCategories = useCategories;
		this.combineMode = combineMode;
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
	{	return RecognizerName.SVMCOMBINER;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
		result = result + "_" + "spec="+specific;
		result = result + "_" + "cat="+useCategories;
		result = result + "_" + "combine="+combineMode.toString();
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
	// LANGUAGES	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entities recognized by this combiner */
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
	{	return FileNames.FO_SVMCOMBINER;
	}

	/**
	 * Loads the objects necessary to the combination process,
	 * including the SVM model.
	 * 
	 * @throws RecognizerException
	 * 		Problem while loading the model.
	 */
	private void loadModel() throws RecognizerException
	{	loadSvmModel();
		if(combineMode.hasWeights())
		{	loadVoteWeights();
			if(combineMode==CombineMode.ENTITY_WEIGHTED_CATEGORY)
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
     * Checks whether the combiner has been
     * already loaded.
     * 
     * @return
     * 		{@code true} iff the combiner has already been loaded.
    */
	private boolean isLoaded()
	{	boolean result = svmModel!=null;
		
		if(result && combineMode.hasWeights())
			result = voteWeights!=null && categoryProportions!=null;
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SVM MODEL	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Object representing the previously trained SVM combiner */
	private svm_model svmModel;
	
	/**
	 * Returns the name of the file containing
	 * the SVM model. This model must have been
	 * trained previously, this class is not meant
	 * to perform the training.
	 * 
	 * @return
	 * 		A String representing the path of the SVM model.
	 */
	protected String getSvmModelPath()
	{	String result = getModelPath() + File.separator + getFolder() + ".svm" + FileNames.EX_TEXT;
		return result;
	}
	
	/**
	 * Loads the previously trained SVM model,
	 * to be used by this class to combine
	 * the NER tools outputs.
	 * 
	 * @throws RecognizerException
	 * 		Problem while loading the SVM model.
	 */
	private void loadSvmModel() throws RecognizerException
	{	String filename = getSvmModelPath();
		try
		{	svmModel = svm.svm_load_model(filename);
		}
		catch (IOException e)
		{	e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// SVM CONVERSION 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not to consider recall when voting */
	private boolean useRecall = true;
	
	/**
	 * Populates the specified SVM data object in order
	 * to represent the categories of the specified article.
	 * 
	 * @param index
	 * 		Where to start populating the SVM data object.
	 * @param article
	 * 		Article whose categories must be represented.
	 * @param result
	 * 		SVM data object to complete.
	 * @return
	 * 		Position in the object at the end of the update.
	 */
	private int convertCategoryToSvm(int index, Article article, svm_node result[])
	{	ArticleCategory categoryValues[] = ArticleCategory.values();
		int catSize = categoryValues.length;
		
		List<ArticleCategory> categories = article.getCategories();
		int idx;
		for(idx=index;idx<index+catSize;idx++)
		{	result[idx] = new svm_node();
			result[idx].index = idx + 1;
			ArticleCategory category = categoryValues[idx];
			if(categories.contains(category))
				result[idx].value = +1;
			else
				result[idx].value = -1;
		}
		
		return idx;
	}
	
	/**
	 * Converts the outputs of the selected NER tools,
	 * into an object the SVM can process as an input.
	 * <br/>
	 * This method is used when using the entity-by-entity mode.
	 * 
	 * @param group
	 * 		Our internal representation of the NER tools outputs.
	 * @param article
	 * 		Processed article. 
	 * @return
	 * 		The SVM representation of the same data.
	 */
	protected svm_node[] convertEntityGroupToSvm(Map<AbstractRecognizer, AbstractEntity<?>> group, Article article)
	{	ArticleCategory categoryValues[] = ArticleCategory.values();
		int inSize;													// total number of SVM inputs
		int nerSize = recognizers.size() * HANDLED_TYPES.size();	// NER-related inputs
		int catSize = categoryValues.length;						// category-related inputs
		if(useCategories)
			inSize = nerSize + catSize;
		else
			inSize = nerSize;
		
		// init SVM data structure
		svm_node result[] = new svm_node[inSize];
		int j = 0;
		
		// convert category
		if(useCategories)
			j = convertCategoryToSvm(j, article, result);

		// convert NER outputs
		for(AbstractRecognizer recognizer: recognizers)
		{	AbstractEntity<?> entity = group.get(recognizer);
			EntityType type = null;
			if(entity!=null)
				type = entity.getType();
			for(EntityType t: HANDLED_TYPES)
			{	result[j] = new svm_node();
				result[j].index = j + 1;
				if(type==t)
					result[j].value = +1;
				else
					result[j].value = -1;
				j++;
			}
		}
		
		return result;
	}
	
	/**
	 * Method internally used by {@code #convertEntityWordToSvm} to complete
	 * the specified SVM data object with the specified type and beginning state,
	 * at the specified position (in the SVM data object).
	 * 
	 * @param index
	 * 		Position to start from in the SVM data object.
	 * @param type
	 * 		Type of the entity (or {@code null} for no entity).
	 * @param beginning
	 * 		BIO state of the entity (B={@code true}, I={@code false} and O={@code null}). 
	 * @param result
	 * 		SVM data object to complete.
	 * @return
	 * 		Position after the process is over.
	 */
	private int convertEntityWordSglToSvm(int index, EntityType type, Boolean beginning, svm_node result[])
	{	int idx = index;
		
		// setup the type
		for(EntityType t: HANDLED_TYPES)
		{	result[idx] = new svm_node();
			result[idx].index = idx + 1;
			if(type==t)
				result[idx].value = +1;
			else
				result[idx].value = -1;
			idx++;
		}
		
		// setup the BIO stuff
		if(beginning==null)
		{	// outside
			result[idx] = new svm_node();
			result[idx].value = -1;
			idx++;
			result[idx] = new svm_node();
			result[idx].value = -1;
			idx++;
		}
		else if(beginning)
		{	// beginning
			result[idx] = new svm_node();
			result[idx].value = +1;
			idx++;
			result[idx] = new svm_node();
			result[idx].value = -1;
			idx++;
		}
		else if(!beginning)
		{	// inside
			result[idx] = new svm_node();
			result[idx].value = -1;
			idx++;
			result[idx] = new svm_node();
			result[idx].value = +1;
			idx++;
		}
		
		return idx;
	}
	
	/**
	 * Converts the outputs of the selected NER tools,
	 * into an object the SVM can process as an input.
	 * <br/>
	 * This method is used when using the word-by-word mode.
	 * 
	 * @param previousType
	 * 		Type used for the previous chunk (optional).
	 * @param previousBeginning
	 * 		BIO state used for the previous chunk (optional).
	 * @param wordEntities
	 * 		Word-entity couples to be converted.
	 * @param article
	 * 		Processed article. 
	 * @return
	 * 		The SVM representation of the same data.
	 */
	protected svm_node[] convertEntityWordToSvm(EntityType previousType, Boolean previousBeginning, Map<AbstractRecognizer,WordEntity> wordEntities, Article article)
	{	ArticleCategory categoryValues[] = ArticleCategory.values();
		int nerSize = recognizers.size() * (HANDLED_TYPES.size()+2);	// NER-related inputs (2 extras for BIO)
		int catSize = categoryValues.length;							// category-related inputs
		int prevSize = HANDLED_TYPES.size()+2;							// previous chunk
		int inSize = nerSize;											// total number of SVM inputs
		if(useCategories)
			inSize = inSize + catSize;
		if(combineMode==CombineMode.CHUNK_PREVIOUS)
			inSize = inSize + prevSize;
		
		// init SVM data structure
		svm_node result[] = new svm_node[inSize];
		int j = 0;
		
		// convert category
		if(useCategories)
			j = convertCategoryToSvm(j, article, result);

		// convert previous chunk
		if(combineMode==CombineMode.CHUNK_PREVIOUS)
			j = convertEntityWordSglToSvm(j, previousType, previousBeginning, result);
		
		// convert NER outputs
		for(AbstractRecognizer recognizer: recognizers)
		{	WordEntity wordEntity = wordEntities.get(recognizer);
		
			EntityType type = null;
			Boolean beginning = null;
			if(wordEntity!=null)
			{	type = wordEntity.getType();
				beginning = wordEntity.isBeginning();
			}
			
			j = convertEntityWordSglToSvm(j, type, beginning, result);
		}
		
		return result;
	}

	/**
	 * Produces an entity depending on the class 
	 * outputed by the SVM, and the entities estimated
	 * by the selected NER tools. The entities have
	 * to vote to determine the exact position of the
	 * entity, which was not outputted by the SVM.
	 * <br/>
	 * This method is used when using the entity-by-entity mode.
	 * 
	 * @param group
	 * 		Entities detected by the NER tools.
	 * @param y
	 * 		Output of the SVM.
	 * @param article
	 * 		Processed article. 
	 * @return
	 * 		An entity, or {@code null} if none was detected.
	 */
	private AbstractEntity<?> convertSvmToEntity(Map<AbstractRecognizer, AbstractEntity<?>> group, double y, Article article)
	{	logger.log("Vote-based conversion of an entity group");
		logger.increaseOffset();
		logger.log(group.values().toString());
		
		AbstractEntity<?> result = null;
		String rawText = article.getRawText();
		Map<ArticleCategory,Float> categoryWeights = categoryProportions.processCategoryWeights(article);
	
		// identify entity type
		EntityType type = null;
		int idx = (int)y;
		if(idx>1)
			type = HANDLED_TYPES.get(idx-2);
		
		// 'no type' means 'no entity'
		if(type==null)
			logger.log("No entity was detected by the SVM");
		
		else
		{	logger.log("Entity detected, type: "+type);
			logger.increaseOffset();
			
			// get entity position
			Map<Integer,Float> startScores = new HashMap<Integer, Float>();
			Map<Integer,Float> endScores = new HashMap<Integer, Float>();
			
			// first: pro votes
			for(AbstractRecognizer recognizer: recognizers)
			{	AbstractEntity<?> entity = group.get(recognizer);
				
				// check existence
				if(entity!=null)
				{	float weight;
					if(combineMode==CombineMode.ENTITY_UNIFORM)
						weight = 1f;
					else
						weight = voteWeights.processVotingWeight(article, recognizer, LilleMeasure.SCORE_FP, categoryWeights);
					
					// start position
					{	int startPos = entity.getStartPos();
						Float startScore = startScores.get(startPos);
						if(startScore==null)
							startScore = 0f;
						startScore = startScore + weight;
						startScores.put(startPos,startScore);
					}
					
					// end position
					{	int endPos = entity.getEndPos();
						Float endScore = endScores.get(endPos);
						if(endScore==null)
							endScore = 0f;
						endScore = endScore + weight;
						endScores.put(endPos,endScore);
					}
				}
			}
			logger.log("'For' start votes: "+startScores.toString());
			logger.log("'For' end votes: "+startScores.toString());
			
			// second: against votes
			if(useRecall)
			{	for(AbstractRecognizer recognizer: recognizers)
				{	AbstractEntity<?> entity = group.get(recognizer);
					
					// check existence
					if(entity!=null)
					{	float weight;
						if(combineMode==CombineMode.ENTITY_UNIFORM)
							weight = 1f;
						else
							weight = voteWeights.processVotingWeight(article, recognizer, LilleMeasure.SCORE_FR, categoryWeights);
						
						// start position
						{	int startPos = entity.getStartPos();
							List<Entry<Integer,Float>> entries = new ArrayList<Entry<Integer,Float>>(startScores.entrySet());
							for(Entry<Integer,Float> entry: entries)
							{	int pos = entry.getKey();
								float score = entry.getValue();
								if(pos!=startPos)
								{	score = score - weight;
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
								{	score = score - weight;
									endScores.put(pos,score);
								}
							}
						}
					}
				}
				logger.log("'Against' start votes: "+startScores.toString());
				logger.log("'Against' end votes: "+startScores.toString());
			}
			logger.decreaseOffset();
			
			// identify final positions
			int positions[] = getPositionFromScores(startScores,endScores);
			int startPos = positions[0];
			int endPos = positions[1];
			logger.log("Final position: ("+startPos+","+endPos+")");
			
			// set other attributes
			String valueStr = rawText.substring(startPos,endPos);
			RecognizerName source = getName();
			
			// build entity
			result = AbstractEntity.build(type, startPos, endPos, source, valueStr);
			logger.log("Final entity: "+result);
		}
		
		logger.decreaseOffset();
		return result;
	}

	/**
	 * Builds a new entity from the specified data.
	 * <br/>
	 * This method is used when using the word-by-word mode.
	 * 
	 * @param startPos
	 * 		Start position of the entity to be created.
	 * @param endPos
	 * 		End position of the entity to be created.
	 * @param type
	 * 		Type of the entity to be created.
	 * @param article
	 * 		Article containing the entity.
	 * @return
	 * 		Created entity.
	 */
	private AbstractEntity<?> convertSvmToEntity(int startPos, int endPos, EntityType type, Article article)
	{	String rawText = article.getRawText();
		String valueStr = rawText.substring(startPos, endPos);
		AbstractEntity<?> result = AbstractEntity.build(type, startPos, endPos, getName(), valueStr);
		return result;
	}

	/**
	 * Produces an entity type depending on the class 
	 * outputed by the SVM.
	 * <br/>
	 * This method is used when using the word-by-wprd mode.
	 * 
	 * @param y
	 * 		Output of the SVM.
	 * @return
	 * 		An entity type, or {@code null} if no entity was detected.
	 */
	private EntityType convertSvmToEntityType(double y)
	{	EntityType result;
	
		if(y==1)
			result = null;
		else
		{	int index = ((int)y)/2 - 1;
			result = HANDLED_TYPES.get(index);
		}
	
		return result;
	}
	
	/**
	 * Produces a boolean indicating if the class 
	 * outputed by the SVM means the considerd word
	 * is at the beginning ({@code true} or inside
	 * {@code false} of an entity.
	 * <br/>
	 * This method is used when using the entity-by-entity mode.
	 * 
	 * @param y
	 * 		Output of the SVM.
	 * @return
	 * 		{@code true} iff the considered word is at the beginning of an entity.
	 */
	private Boolean convertSvmToEntityBio(double y)
	{	Boolean result = null;
	
		if(y==1)
			result = null;
		else
		{	int index = ((int)y)%2;
			result = index==0;
		}
	
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// USE CATEGORIES			/////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if categories should be used as SVM inputs */
	private boolean useCategories;

	/**
	 * Indicates whether categories should be used when
	 * combining the entities.
	 * 
	 * @return
	 * 		{@code true} iff categories should be used for entity combination.
	 */
	public boolean getUseCategories()
	{	return useCategories;
	}

	/////////////////////////////////////////////////////////////////
	// COMBINE MODE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if entities should be combined by entity (in which case several vote modes are possible) or by word */
	private CombineMode combineMode;

	/**
	 * Enumeration used to configure how
	 * the entity combination is performed.
	 */
	public enum CombineMode
	{	/** Each NER tool accounts for one vote */
		ENTITY_UNIFORM("EntUnif"),
		/** Overall scores are used to determine vote weights */
		ENTITY_WEIGHTED_OVERALL("EntWghtOvrl"),
		/** Category-related scores are used to determine vote weights */
		ENTITY_WEIGHTED_CATEGORY("EntWghtCat"),
		/** No vote at all (the SVM handles everything), processing one chunk at a time */
		CHUNK_SINGLE("ChunkSngl"),
		/** No vote at all (the SVM handles everything), using the previous chunk*/
		CHUNK_PREVIOUS("ChunkPrev");

		/** String representing the parameter value */
		private String name;
		
		/**
		 * Builds a new combine mode value
		 * to be used as a parameter.
		 * 
		 * @param name
		 * 		String representing the parameter value.
		 */
		CombineMode(String name)
		{	this.name = name;
		}
		
		/**
		 * Indicates if this combine mode requires
		 * vote weights.
		 * 
		 * @return
		 * 		{@code true} if vote weights are required.
		 */
		public boolean hasWeights()
		{	boolean result = this==ENTITY_WEIGHTED_OVERALL
				|| this==ENTITY_WEIGHTED_CATEGORY;
			return result;
		}
		
		/**
		 * Indicates if this combine mode is chunk-based
		 * vote weights.
		 * 
		 * @return
		 * 		{@code true} if no vote is used (only the SVM).
		 */
		public boolean isChunkBased()
		{	boolean result = this==CHUNK_SINGLE
				|| this==CHUNK_PREVIOUS;
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
	public CombineMode getCombineMode()
	{	return combineMode;
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Entities combineEntities(Article article, Map<AbstractRecognizer,Entities> entities, StringBuffer rawOutput) throws RecognizerException
	{	logger.increaseOffset();
		Entities result = new Entities(getName());
		
		// possibly load the SVM combiner
		if(!isLoaded())
			loadModel();
				
		if(combineMode.isChunkBased())
			combineEntitiesByWord(article, entities, rawOutput, result);
		else
			combineEntitiesByEntity(article, entities, rawOutput, result);
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Takes a map representing the outputs of each previously applied NER tool, 
	 * and combine those entities to get a single set. The combination is 
	 * performed entity by entity. 
	 * 
     * @param article
     * 		Concerned article.
     * @param entities
     * 		Map of the entities detected by the 
     * 		individual NER tools.
     * @param rawOutput
     * 		Empty {@code StringBuffer} the combiner can use to
     * 		write a text output for debugging purposes.
     * 		Or it can just let it empty.
	 * @param result
     * 		Result of the combination of those
     * 		individual entities.
     * 
	 * @throws RecognizerException
     * 		Problem while combining entities.
	 */
	private void combineEntitiesByEntity(Article article, Map<AbstractRecognizer,Entities> entities, StringBuffer rawOutput, Entities result) throws RecognizerException
	{	// get overlapping entities
		logger.log("Get list of overlapping entities");
		List<Map<AbstractRecognizer, AbstractEntity<?>>> overlaps = Entities.identifyOverlaps(entities);
		
		// process each group of entities
		logger.log("Process each group of entities");
		logger.increaseOffset();
		for(Map<AbstractRecognizer, AbstractEntity<?>> overlap: overlaps)
		{	// add overlap to raw output
			rawOutput.append("Overlap:\n");
			for(Entry<AbstractRecognizer, AbstractEntity<?>> entry: overlap.entrySet())
			{	AbstractRecognizer recognizer = entry.getKey();
				AbstractEntity<?> entity = entry.getValue();
				rawOutput.append("\t"+recognizer.getName()+": "+entity+"\n");
			}
			
			// convert to SVM input
			logger.log("Convert to SVM format");
			svm_node[] x = convertEntityGroupToSvm(overlap,article);
			rawOutput.append("x={");
			for(svm_node xx: x)
				rawOutput.append(xx.index+":"+xx.value+" ");
			rawOutput.replace(rawOutput.length()-1, rawOutput.length(), "}\n");
			
			// process SVM prediction
			double y = svm.svm_predict(svmModel,x);
			rawOutput.append("y="+y);
			logger.log("Process SVM prediction: "+y);
			
			// convert to actual entity
			AbstractEntity<?> entity = convertSvmToEntity(overlap,y,article);
			rawOutput.append(">> entity="+entity+"\n");
			logger.log("Convert to entity object: "+entity);
			
			// possibly add to result
			if(entity!=null)
				result.addEntity(entity);
			
			rawOutput.append("\n");
		}
		logger.decreaseOffset();
	}

	/**
	 * Process the list of word-entity couples for the specified article.
	 * Those are used when the combination mode is set to word-by-word.
	 * 
	 * @param article
	 * 		Article to process.
	 * @param entities
	 * 		Previously detected entities, to be compared with the article words.
	 * @return
	 * 		List of word-entity maps: each map corresponds to one word with at least
	 * 		an overlap with a previously detected entity.
	 */
	protected List<Map<AbstractRecognizer,WordEntity>> identifyWordEntityOverlaps(Article article, Map<AbstractRecognizer,Entities> entities)
	{	List<Map<AbstractRecognizer,WordEntity>> result = new ArrayList<Map<AbstractRecognizer, WordEntity>>();
		
		// break text into words
		String rawText = article.getRawText();
		List<String> words = getWordListFromText(rawText);
		logger.log("Break raw text into words: "+words.size()+" words found");
		Iterator<String> itWord = words.iterator();
		int wordStart = 0; 
		int wordEnd = 0;
		
		// build an iterator for each NER tool
		logger.log("Build all needed structures");
		List<AbstractRecognizer> recognizers = new ArrayList<AbstractRecognizer>(entities.keySet());
		Map<AbstractRecognizer,Iterator<AbstractEntity<?>>> iterators = new HashMap<AbstractRecognizer,Iterator<AbstractEntity<?>>>();
		Map<AbstractRecognizer,AbstractEntity<?>> currentEntities = new HashMap<AbstractRecognizer,AbstractEntity<?>>();
		Map<AbstractRecognizer,Integer> currentIndices = new HashMap<AbstractRecognizer,Integer>();
		Iterator<AbstractRecognizer> itR = recognizers.iterator();
		while(itR.hasNext())
		{	AbstractRecognizer recognizer = itR.next();
			Entities e = entities.get(recognizer);
			Iterator<AbstractEntity<?>> it = e.getEntities().iterator();
			if(it.hasNext())
			{	AbstractEntity<?> entity = it.next();
				currentEntities.put(recognizer, entity);
				currentIndices.put(recognizer, 0);
				iterators.put(recognizer,it);
			}
			else
			{	itR.remove();
				logger.log("WARNING: NER tool "+recognizer+" did not find any entity for this article, which is possibly worth checking");
			}
		}
		
		// process each word in the text
		logger.log("Process separately each word in the text");
		logger.increaseOffset();
		while(itWord.hasNext() && !iterators.isEmpty())
		{	// get the next word
			String word = itWord.next();
			wordStart = rawText.indexOf(word,wordEnd);
			wordEnd = wordStart + word.length();
//			logger.log("Processing word \""+word+"\" ("+wordStart+"-"+wordEnd+")");
			logger.increaseOffset();
			
			// get entities containing this word or overlapping it
//			logger.log("Comparing the word to each NER output");
			logger.increaseOffset();
			Map<AbstractRecognizer,WordEntity> wordEntities = new HashMap<AbstractRecognizer,WordEntity>();
//			Map<AbstractRecognizer,EntityType> ovTypes = new HashMap<AbstractRecognizer,EntityType>();
//			Map<AbstractRecognizer,AbstractEntity<?>> ovEntities = new HashMap<AbstractRecognizer,AbstractEntity<?>>();
//			Map<AbstractRecognizer,Boolean> ovBeginnings = new HashMap<AbstractRecognizer,Boolean>();
			Iterator<AbstractRecognizer> itRec = recognizers.iterator();
			while(itRec.hasNext())
			{	AbstractRecognizer recognizer = itRec.next();
//				logger.log("Managing NER "+recognizer);
				logger.increaseOffset();
				AbstractEntity<?> currentEntity = currentEntities.get(recognizer);
				Iterator<AbstractEntity<?>> it = iterators.get(recognizer);
				PositionRelation posRel;
				
				// go to the appropriate entity for the current NER tool
				do
				{	posRel = PositionRelation.getRelation(currentEntity,wordStart,wordEnd);
//					logger.log("Current entity [posRel="+posRel+"]: "+currentEntity);
					if(posRel==PositionRelation.COMPLETE_PRECEDES && it.hasNext())
					{	currentEntity = it.next();
						currentEntities.put(recognizer, currentEntity);
					}
				}
				while(posRel==PositionRelation.COMPLETE_PRECEDES && it.hasNext());
				
				// if no more entities, remove NER tool from list
				if(posRel==PositionRelation.COMPLETE_PRECEDES)
				{	
//					logger.log(posRel+" >> no more entity >> "+recognizer+" is removed from the list");
					itRec.remove();
					iterators.remove(recognizer);
					currentEntities.remove(recognizer);
					currentIndices.remove(recognizer);
				}
				
				// if the entity does not overlap, ignore it for now
				else if(posRel==PositionRelation.COMPLETE_SUCCEDES)
				{	
//					logger.log(posRel+" >> no overlap >> nothing to do ");
				}
				
				// otherwise we work on the overlap
				else
				{	WordEntity wordEntity = new WordEntity();
					int entityStart = currentEntity.getStartPos();
					if(wordStart<=entityStart)
					{	wordEntity.setBeginning(true);
//						logger.log(posRel+" word is at the beginning of the entity");
					}
					else
					{	wordEntity.setBeginning(false);
//						logger.log(posRel+" word is inside the entity");
					}
					wordEntity.setStartPosition(wordStart);
					wordEntity.setEndPosition(wordEnd);
					wordEntity.setEntity(currentEntity);
					wordEntities.put(recognizer, wordEntity);
				}
				
				logger.decreaseOffset();
			}
			
//			if(wordEntities.isEmpty())
//				logger.log("No entity at all found for the considered word");
//			else
				result.add(wordEntities);
			
			logger.decreaseOffset();
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		
		return result;
	}
	
	/**
	 * Takes a map representing the outputs of each previously applied NER tool, 
	 * and combine those entities to get a single set. The combination is 
	 * performed chunk-wise (i.e. word-by-word). 
	 * 
     * @param article
     * 		Concerned article.
     * @param entities
     * 		Map of the entities detected by the 
     * 		individual NER tools.
     * @param rawOutput
     * 		Empty {@code StringBuffer} the combiner can use to
     * 		write a text output for debugging purposes.
     * 		Or it can just let it empty.
	 * @param result
     * 		Result of the combination of those
     * 		individual entities.
     * 
	 * @throws RecognizerException
     * 		Problem while combining entities.
	 */
	private void combineEntitiesByWord(Article article, Map<AbstractRecognizer,Entities> entities, StringBuffer rawOutput, Entities result) throws RecognizerException
	{	// identify word-entity couples for each NER tool
		List<Map<AbstractRecognizer,WordEntity>> wordEntities = identifyWordEntityOverlaps(article,entities);
		String rawText = article.getRawText();
		
		// process each word in the text
		logger.log("Process each word-entity map detected in the text");
		int currentStartPos = -1;
		int currentEndPos = -1;
		EntityType currentType = null;
		Boolean previousBeginning = null;
		EntityType previousType = null;
		Iterator<Map<AbstractRecognizer,WordEntity>> it = wordEntities.iterator();
		logger.increaseOffset();
		while(it.hasNext())
		{	// get the next word-entity map
			Map<AbstractRecognizer,WordEntity> weMap = it.next();
			
			// no overlapping entity at all >> it is an outside word, we treat it as such
			if(weMap.isEmpty())
			{	
//				logger.log("Word does not overlap with any entity >> outside word");
				logger.increaseOffset();
				// if there's a current entity
				if(currentType==null)
				{	
//					logger.log("No current entity to finalize");
				}
				else
				{	// finalize the current entity
					AbstractEntity<?> entity = convertSvmToEntity(currentStartPos, currentEndPos, currentType, article);
					result.addEntity(entity);
//					logger.log("Finalizing the current entity: "+entity);
					// mark the fact there is now no current entity 
					currentType = null;
					currentStartPos = -1;
					currentEndPos = -1;
					previousBeginning = null;
				}
				
				previousType = null;
				previousBeginning = null;
				
				logger.decreaseOffset();
			}
				
			// at least one overlapping entity
			else
			{	WordEntity we = weMap.values().iterator().next();
				int wordStart = we.getStartPosition();
				int wordEnd = we.getEndPosition();
				String word = rawText.substring(wordStart,wordEnd);
//				logger.log("Processing word \""+word+"\" ("+wordStart+"-"+wordEnd+")");

//				logger.log("Word does overlap with at least one entity >> beginning/inside word");
				logger.increaseOffset();
				// add overlap to raw output
				rawOutput.append("Overlap: \""+word+"\" ("+wordStart+")\n");
				for(Entry<AbstractRecognizer, WordEntity> entry: weMap.entrySet())
				{	AbstractRecognizer recognizer = entry.getKey();
					WordEntity wordEntity = entry.getValue();
					AbstractEntity<?> entity = wordEntity.getEntity();
					Boolean beginning = wordEntity.isBeginning();
					String bio;
					if(beginning)
						bio = "B";
					else
						bio = "I";
					rawOutput.append("\t"+recognizer.getName()+" [BIO="+bio+"]: "+entity+"\n");
				}
				
				// convert to SVM input
				svm_node[] x = convertEntityWordToSvm(previousType,previousBeginning,weMap,article);
//				logger.log("Convert to SVM format: x="+x.toString());
				rawOutput.append("x={");
				for(svm_node xx: x)
					rawOutput.append(xx.index+":"+xx.value+" ");
				rawOutput.replace(rawOutput.length()-1, rawOutput.length(), "}\n");
				
				// process SVM prediction
				double y = svm.svm_predict(svmModel,x);
				rawOutput.append("y="+y);
//				logger.log("Process SVM prediction: "+y);
			
				// convert to actual entity
				EntityType type = convertSvmToEntityType(y);
				Boolean beginning = convertSvmToEntityBio(y);
				// outside word
				if(type==null)
				{	
//					logger.log("SVM decision: outside word");
					logger.increaseOffset();
					// if there's a current entity
					if(currentType==null)
					{
//						logger.log("No current entity to finalize");
					}
					else
					{	// finalize the current entity
						AbstractEntity<?> entity = convertSvmToEntity(currentStartPos, currentEndPos, currentType, article);
						result.addEntity(entity);
//						logger.log("Finalizing the current entity: "+entity);
						// mark the fact there is now no current entity 
						currentType = null;
						currentStartPos = -1;
						currentEndPos = -1;
					}

					previousType = null;
					previousBeginning = null;
					
					logger.decreaseOffset();
				}
				// first inside/beginning word after an outside word (or first of the whole text)
				else if(currentType==null)
				{	currentType = type;
					currentStartPos = wordStart;
					currentEndPos = wordEnd;
//					logger.log("SVM decision: beginning/inside word after an outside word >> start new entity");
					
					previousType = type;
					previousBeginning = true;
				}
				// beginning word (new entity) or type change (also considered to be a new entity?)
				else if(beginning || type!=currentType)
				{	
//					logger.log("SVM decision: beginning word or inside word with type change");
					logger.increaseOffset();
					// finalize the current entity
					AbstractEntity<?> entity = convertSvmToEntity(currentStartPos, currentEndPos, currentType, article);
					result.addEntity(entity);
//					logger.log("Finalizing the current entity: "+entity);
					rawOutput.append(">> entity="+entity+"\n");
					// start the new one
//					logger.log("And starting a new one");
					currentType = type;
					currentStartPos = wordStart;
					currentEndPos = wordEnd;
					
					previousType = type;
					previousBeginning = true;
					
					logger.decreaseOffset();
				}
				// inside word
				else
				{	
//					logger.log("SVM decision: inside word >> updating current entity");
					// update the current entity
					currentEndPos = wordEnd;
					
					previousType = type;
					previousBeginning = false;
				}

				logger.decreaseOffset();
			}
		}
		logger.log("Word processing complete");
		logger.decreaseOffset();
	}
}
