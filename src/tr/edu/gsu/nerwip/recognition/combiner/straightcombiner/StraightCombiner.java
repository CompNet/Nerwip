package tr.edu.gsu.nerwip.recognition.combiner.straightcombiner;

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
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.combiner.AbstractCombiner;
import tr.edu.gsu.nerwip.recognition.external.nero.Nero;
import tr.edu.gsu.nerwip.recognition.external.nero.Nero.NeroTagger;
import tr.edu.gsu.nerwip.recognition.external.tagen.TagEn;
import tr.edu.gsu.nerwip.recognition.external.tagen.TagEnModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.heideltime.HeidelTime;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.heideltime.HeidelTimeModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais.OpenCalais;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais.OpenCalaisLanguage;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opener.OpeNer;

/**
 * This combiner uses uniform weighting on a selection of tools
 * able to handle French. The way the votes are conducted is different
 * from VoteCombiner though, because VoteCombiner is designed to handle
 * a group of tools able to detect the same entity types. This is not
 * the case here, for instance some tools are able to detect functions 
 * whereas others cannot.
 * <br/>
 * Here is the principle of the voting process. We call 'activated tool' a tool
 * which has detected an entity for a given expression. When there is at least
 * one activated tool:
 * <ol>
 * 	<li>Type vote: we keep the majority entity type, among all activated tools.</li>
 *  <li>Existence vote: only the tools able to handle the selected type can vote.
 *      If the activated tools are majority among them, the process goes on.</li>
 *  <li>Position vote: all activated tool vote, the majority positions win.
 * </ol> 
 * <br/>
 * The NER tools used by this combiner are:
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
public class StraightCombiner extends AbstractCombiner
{	
	/**
	 * Builds a new straight combiner.
	 *  
	 * @throws RecognizerException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	public StraightCombiner() throws RecognizerException
	{	super();
		
		initRecognizers();
		setSubCacheEnabled(cache);

		initConverter();
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public RecognizerName getName()
	{	return RecognizerName.STRAIGHTCOMBINER;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
//		result = result + "_" + "combi="+combiner.toString();
		
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
	/** List of entities recognized by this combiner */
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
	// TOOLS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void initRecognizers() throws RecognizerException
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
	protected Entities combineEntities(Article article, Map<AbstractRecognizer,Entities> entities, StringBuffer rawOutput) throws RecognizerException
	{	logger.increaseOffset();
		String text = article.getRawText();
		Entities result = new Entities(getName());
		
		// get overlapping entities
		logger.log("Get the list of overlapping entities");
		List<Map<AbstractRecognizer, AbstractEntity<?>>> overlaps = Entities.identifyOverlaps(entities);
		
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
			
			// determine entity type
			EntityType type = voteForType(map);
			rawOutput.append("Type="+type+"\n");
			
			// determine entity existence
			boolean existence = voteForExistence(map, type);
			rawOutput.append("Existence="+existence+"\n");
			
			if(existence)
			{	// determine entity position
				int pos[] = voteForPosition(map);
				rawOutput.append("Position=("+pos[0]+","+pos[1]+")\n");
				
				// build new, appropriate entity
				int startPos = pos[0];
				int endPos = pos[1];
				String valueStr = text.substring(startPos,endPos);
				AbstractEntity<?> entity = AbstractEntity.build(type, startPos, endPos, getName(), valueStr);
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
	 * Combine the NER tools results, in order to determine the
	 * type of the entity represented by the specified group.
	 * 
	 * @param map 
	 * 		Group of estimated entities.
	 * @return 
	 * 		Type of the entity represnted by the group.
	 */
	protected EntityType voteForType(Map<AbstractRecognizer, AbstractEntity<?>> map)
	{	logger.log("Start voting for type: ");
		logger.increaseOffset();
		Map<EntityType,Float> typeScores = new HashMap<EntityType, Float>();
		
		// process votes
		for(AbstractRecognizer recognizer: recognizers)
		{	AbstractEntity<?> entity = map.get(recognizer);
			if(entity!=null)
			{	EntityType type = entity.getType();
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
	 * Combine the NER tools results, in order to determine if
	 * the group of estimated entities corresponds to an actual
	 * entity.
	 * 
	 * @param map 
	 * 		Group of estimated entities.
	 * @param type 
	 * 		Estimated type for the treated entity.
	 * @return 
	 * 		{@code true} iff the conclusion is that the entity is correct.
	 */
	protected boolean voteForExistence(Map<AbstractRecognizer, AbstractEntity<?>> map, EntityType type)
	{	logger.log("Start voting for existence:");
		logger.increaseOffset();
		
		float voteFor = 0;
		float voteAgainst = 0;
		
		for(AbstractRecognizer recognizer: recognizers)
		{	List<EntityType> handledTypes = recognizer.getHandledEntityTypes();
			if(handledTypes.contains(type))
			{	AbstractEntity<?> entity = map.get(recognizer);
				if(entity==null)
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
	 * Combine the NER tools results, in order to determine the
	 * position of the entity represented by the specified group.
	 * 
	 * @param map 
	 * 		Group of estimated entities.
	 * @return 
	 * 		An array of two integers corresponding to the entity position.
	 */
	protected int[] voteForPosition(Map<AbstractRecognizer, AbstractEntity<?>> map)
	{	logger.log("Start voting for position:");
		logger.increaseOffset();
		Map<Integer,Float> startScores = new HashMap<Integer, Float>();
		Map<Integer,Float> endScores = new HashMap<Integer, Float>();
		
		// pro votes
		for(AbstractRecognizer recognizer: recognizers)
		{	AbstractEntity<?> entity = map.get(recognizer);
		
			// check existence
			if(entity!=null)
			{	// start position
				int startPos = entity.getStartPos();
				Float startScore = startScores.get(startPos);
				if(startScore==null)
					startScore = 0f;
				startScore = startScore + 1;
				startScores.put(startPos,startScore);
				
				// end position
				int endPos = entity.getEndPos();
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
