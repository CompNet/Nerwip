package fr.univavignon.nerwip.processing.combiner.straightcombiner;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.combiner.AbstractCombinerDelegateRecognizer;
import fr.univavignon.nerwip.processing.external.nero.Nero;
import fr.univavignon.nerwip.processing.external.nero.NeroTagger;
import fr.univavignon.nerwip.processing.external.tagen.TagEn;
import fr.univavignon.nerwip.processing.external.tagen.TagEnModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.heideltime.HeidelTime;
import fr.univavignon.nerwip.processing.internal.modelbased.heideltime.HeidelTimeModelName;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalais;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalaisLanguage;
import fr.univavignon.nerwip.processing.internal.modelless.opener.OpeNer;

/**
 * This combiner uses uniform weighting on a selection of tools
 * able to handle French. The way the votes are conducted is different
 * from VoteCombiner though, because VoteCombiner is designed to handle
 * a group of tools able to detect the same entity types. This is not
 * the case here, for instance some tools are able to detect functions 
 * whereas others cannot.
 * <br/>
 * Here is the principle of the voting process. We call 'activated tool' a tool
 * which has detected a mention for a given expression. When there is at least
 * one activated tool:
 * <ol>
 * 	<li>Type vote: we keep the majority entity type, among all activated tools.</li>
 *  <li>Existence vote: only the tools able to handle the selected type can vote.
 *      If the activated tools are majority among them, the process goes on.</li>
 *  <li>Position vote: all activated tool vote, the majority positions win.
 * </ol> 
 * <br/>
 * The recognizers used by this combiner are:
 * <ul>
 * 		<li>HeidelTime (dates)</li>
 * 		<li>Nero (dates, functions, persons, locations, organizations and productions)</li>
 * 		<li>OpenCalais (dates, persons, locations and organizations)</li>
 * 		<li>OpeNer (dates, persons, locations and organizations)</li>
 * 		<li>TagEn (dates, persons, locations and organizations)</li>
 * </ul>
 * There is no option to change its behavior (yet).
 * 
 * @author Vincent Labatut
 */
class StraightCombinerDelegateRecognizer extends AbstractCombinerDelegateRecognizer
{	
	/**
	 * Builds a new straight combiner.
	 *  
	 * @param straightCombiner
	 * 		Recognizer in charge of this delegate.
	 * 
	 * @throws ProcessorException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	public StraightCombinerDelegateRecognizer(StraightCombiner straightCombiner) throws ProcessorException
	{	super(straightCombiner);
		
		initRecognizers();
		setSubCacheEnabled(recognizer.doesCache());
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
//		result = result + "_" + "combi="+combiner.toString();
		
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
		EntityType.DATE,
		EntityType.FUNCTION,
		EntityType.LOCATION,
		EntityType.ORGANIZATION,
		EntityType.PERSON,
		EntityType.PRODUCTION
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
//		ArticleLanguage.EN,
		ArticleLanguage.FR
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
	
		// HeidelTime
	      {	logger.log("Init HeidelTime (Dates only)");
			HeidelTimeModelName modelName = HeidelTimeModelName.FRENCH_NARRATIVES;
			boolean loadModelOnDemand = true;
			boolean doIntervalTagging = false;
			HeidelTime heidelTime = new HeidelTime(modelName, loadModelOnDemand, doIntervalTagging);
			recognizers.add(heidelTime);
		}
		
		// Nero
		  {	logger.log("Init Nero");
			NeroTagger neroTagger = NeroTagger.CRF;
			boolean flat = true;
			boolean ignorePronouns = false;
			boolean exclusionOn = false;
			Nero nero = new Nero(neroTagger, flat, ignorePronouns, exclusionOn);
//			nero.setCacheEnabled(false);
			recognizers.add(nero);
		}
		
		// OpenCalais
		  {	logger.log("Init OpenCalais");
			OpenCalaisLanguage lang = OpenCalaisLanguage.FR;
			boolean ignorePronouns = false;
			boolean exclusionOn = false;
			OpenCalais openCalais = new OpenCalais(lang, ignorePronouns, exclusionOn);
			recognizers.add(openCalais);
		}
		
		// OpeNer
		  {	logger.log("Init OpeNer");
			boolean parenSplit = true;
			boolean ignorePronouns = false;
			boolean exclusionOn = false;
			OpeNer opeNer = new OpeNer(parenSplit, ignorePronouns, exclusionOn);
//			opeNer.setCacheEnabled(false);
			recognizers.add(opeNer);
		}
		
		// TagEn
		  {	logger.log("Init TagEn");
			TagEnModelName model = TagEnModelName.MUC_MODEL;
			boolean ignorePronouns = false;
			boolean exclusionOn = false;
			TagEn tagEn = new TagEn(model, ignorePronouns, exclusionOn);
			recognizers.add(tagEn);
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
		ArticleLanguage language = article.getLanguage();
		ProcessorName recognizerName = recognizer.getName();
		String text = article.getRawText();
		Mentions result = new Mentions(recognizerName);
		
		// get overlapping mentions
		logger.log("Get the list of overlapping mentions");
		List<Map<InterfaceRecognizer, AbstractMention<?>>> overlaps = Mentions.identifyOverlaps(mentions);
		
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
			
			// determine entity type
			EntityType type = voteForType(map);
			rawOutput.append("Type="+type+"\n");
			
			// determine mention existence
			boolean existence = voteForExistence(map, type);
			rawOutput.append("Existence="+existence+"\n");
			
			if(existence)
			{	// determine mention position
				int pos[] = voteForPosition(map);
				rawOutput.append("Position=("+pos[0]+","+pos[1]+")\n");
				
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
					if(mention==null)
						logger.log("Could not infer the value associated to the mention");
				}
				if(mention!=null)
				{	result.addMention(mention);
					rawOutput.append(">> Mention="+endPos+"\n\n");
				}
				else
					logger.log("Could not build the mention");
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		
	    logger.decreaseOffset();
		return result;
	}

	/**
	 * Combine the recognizers results, in order to determine the
	 * type of the mention represented by the specified group.
	 * 
	 * @param map 
	 * 		Group of estimated mentions.
	 * @return 
	 * 		Type of the mention represnted by the group.
	 */
	protected EntityType voteForType(Map<InterfaceRecognizer, AbstractMention<?>> map)
	{	logger.log("Start voting for type: ");
		logger.increaseOffset();
		Map<EntityType,Float> typeScores = new HashMap<EntityType, Float>();
		
		// process votes
		for(InterfaceRecognizer recognizer: recognizers)
		{	AbstractMention<?> mention = map.get(recognizer);
			if(mention!=null)
			{	EntityType type = mention.getType();
				Float typeScore = typeScores.get(type);
				if(typeScore==null)
					typeScore = 0f;
				typeScore = typeScore + 1;
				typeScores.put(type,typeScore);
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

	/**
	 * Combine the recognizers results, in order to determine if
	 * the group of estimated mentions corresponds to an actual
	 * mention.
	 * 
	 * @param map 
	 * 		Group of estimated mentions.
	 * @param type 
	 * 		Estimated type for the treated mention.
	 * @return 
	 * 		{@code true} iff the conclusion is that the mention is correct.
	 */
	protected boolean voteForExistence(Map<InterfaceRecognizer, AbstractMention<?>> map, EntityType type)
	{	logger.log("Start voting for existence:");
		logger.increaseOffset();
		
		float voteFor = 0;
		float voteAgainst = 0;
		
		for(InterfaceRecognizer recognizer: recognizers)
		{	List<EntityType> handledTypes = recognizer.getRecognizedEntityTypes();
			if(handledTypes.contains(type))
			{	AbstractMention<?> mention = map.get(recognizer);
				if(mention==null)
					voteAgainst = voteAgainst + 1;
				else
					voteFor = voteFor + 1;
			}
		}
		
		float total = voteFor - voteAgainst;
		boolean result = total>=0;
		logger.log("Votes: FOR="+voteFor+" - AGAINST="+voteAgainst+" = "+total+" >> "+result);
		
		logger.decreaseOffset();
		logger.log("Result of the vote for existence: "+result);
		return result;
	}

	/**
	 * Combine the recognizers results, in order to determine the
	 * position of the mention represented by the specified group.
	 * 
	 * @param map 
	 * 		Group of estimated mentions.
	 * @return 
	 * 		An array of two integers corresponding to the mention position.
	 */
	protected int[] voteForPosition(Map<InterfaceRecognizer, AbstractMention<?>> map)
	{	logger.log("Start voting for position:");
		logger.increaseOffset();
		Map<Integer,Float> startScores = new HashMap<Integer, Float>();
		Map<Integer,Float> endScores = new HashMap<Integer, Float>();
		
		// pro votes
		for(InterfaceRecognizer recognizer: recognizers)
		{	AbstractMention<?> mention = map.get(recognizer);
		
			// check existence
			if(mention!=null)
			{	// start position
				int startPos = mention.getStartPos();
				Float startScore = startScores.get(startPos);
				if(startScore==null)
					startScore = 0f;
				startScore = startScore + 1;
				startScores.put(startPos,startScore);
				
				// end position
				int endPos = mention.getEndPos();
				Float endScore = endScores.get(endPos);
				if(endScore==null)
					endScore = 0f;
				endScore = endScore + 1;
				endScores.put(endPos,endScore);
			}
		}
		
		int result[] = getPositionFromScores(startScores,endScores);
		logger.decreaseOffset();
		logger.log("Result of the vote for position: startPos="+result[0]+", endPos="+result[1]);
		return result;
	}
}
