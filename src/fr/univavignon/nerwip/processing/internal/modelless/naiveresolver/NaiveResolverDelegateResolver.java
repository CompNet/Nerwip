package fr.univavignon.nerwip.processing.internal.modelless.naiveresolver;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.AbstractEntity;
import fr.univavignon.common.data.entity.AbstractNamedEntity;
import fr.univavignon.common.data.entity.AbstractValuedEntity;
import fr.univavignon.common.data.entity.Entities;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateResolver;

/**
 * This class implements a naive resolver relying on the surface form of the 
 * mentions identified at the previous step of the process (i.e. mention recognition).
 * <br/>
 * It uses the Levenshtein distance, and the user must specify a threshold corresponding
 * to the maximal distance allowed for to strings to be considered as similar (and 
 * therefore for the corresponding mentions to be linked to the same entity). 
 * 
 * @author Vincent Labatut
 */
class NaiveResolverDelegateResolver extends AbstractModellessInternalDelegateResolver<Entities>
{
	/**
	 * Builds and sets up an object representing
	 * the naive resolver delegate.
	 * 
	 * @param naiveResolver
	 * 		Recognizer in charge of this delegate.
	 * @param maxDist
	 * 		Maximal distance for two strings to be considered as similar.
	 */
	public NaiveResolverDelegateResolver(NaiveResolver naiveResolver, int maxDist)
	{	super(naiveResolver, false);
		
		this.maxDist = maxDist;
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = resolver.getName().toString();
		
		result = result + "_" + "maxDist=" + maxDist;
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types resolved by Spotlight */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList(EntityType.values());
	
	@Override
	public List<EntityType> getHandledEntityTypes()
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this resolver can treat */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList(ArticleLanguage.values());
	
	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// MAXIMAL DISTANCE		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Maximal distance */
	private int maxDist;

	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Entities resolveCoreferences(Article article, Mentions mentions) throws ProcessorException
	{	logger.log("Start resolving coreferences in article "+article);
		logger.increaseOffset();
		
		// get the list of similarity sets of mentions (using approximate comparisons)
		List<List<AbstractMention<?>>> simsets = initSimsets(mentions);
		// create the entities based on the similarity sets
		Entities result = createEntities(simsets);
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * For the specified list of mentions, process the sets of similar mentions.
	 * Two mentions are considered similar if their surface form is at most at
	 * a certain Leveinshtein distance value. For valued entity, we perform an 
	 * exact comparison. A similarity set is a list of mentions such that each
	 * one is at least similar to another one in the same similarity set.
	 *  
	 * @param mentions
	 * 		Group of mentions to process.
	 * @return
	 * 		The corresponding list of similarity sets.
	 */
	private List<List<AbstractMention<?>>> initSimsets(Mentions mentions)
	{	logger.log("Initializing the list of similarity sets");
		logger.increaseOffset();
		
		// init the sets of mentions
		List<List<AbstractMention<?>>> result = new ArrayList<List<AbstractMention<?>>>();
		List<AbstractMention<?>> mentionList = mentions.getMentions();
		for(AbstractMention<?> mention: mentionList)
		{	List<AbstractMention<?>> simset = new ArrayList<AbstractMention<?>>();
			simset.add(mention);
			result.add(simset);
		}
		
		// iteratively merge similar sets
		boolean change;
		do
		{	change = false;
			int i = 0;
			while(i<result.size()-1)
			{	List<AbstractMention<?>> simset1 = result.get(i);
				int j = i + 1;
				while(j<result.size())
				{	List<AbstractMention<?>> simset2 = result.get(j);
					if(checkIntersection(simset1,simset2))
					{	change = true;
						simset1.addAll(simset2);
						result.remove(j);
					}
					else
						j++;
				}
				i++;
			}
		}
		while(change);

		logger.log("Done: "+result.size()+" sets found");
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Checks if the specified sets of similar mentions have an intersection, i.e.
	 * possess at least one common mention. This is performed by comparing pairs
	 * of mentions, using an approximate comparator.
	 * 
	 * @param simset1
	 * 		First set of similar mentions.
	 * @param simset2
	 * 		Second set of similar mentions.
	 * @return
	 * 		{@code true} iff the sets contain two mentions considered as similar.
	 */
	private boolean checkIntersection(List<AbstractMention<?>> simset1, List<AbstractMention<?>> simset2)
	{	boolean result = false;
	
		Iterator<AbstractMention<?>> it1 = simset1.iterator();
		while(it1.hasNext() && !result)
		{	AbstractMention<?> mention1 = it1.next();
			EntityType type1 = mention1.getType();
			
			Iterator<AbstractMention<?>> it2 = simset2.iterator();
			while(it2.hasNext() && !result)
			{	AbstractMention<?> mention2 = it2.next();
				EntityType type2 = mention2.getType();
				if(type1==type2)
				{	// named entities
					if(type1.isNamed())
					{	String surf1 = mention1.getStringValue().toLowerCase(Locale.ENGLISH);
						String surf2 = mention2.getStringValue().toLowerCase(Locale.ENGLISH);
						int dist = StringUtils.getLevenshteinDistance(surf1, surf2, maxDist);
						result = dist!=-1;
					}
					// valued entities
					else
					{	Comparable<?> value1 = mention1.getValue();
						Comparable<?> value2 = mention2.getValue();
						try
						{	if(value1==null || value2==null)
							{	String name1 = mention1.getStringValue();
								String name2 = mention2.getStringValue();
								result = name1.equalsIgnoreCase(name2);
							}
							else
								result = value1.equals(value2);				
						}
						catch(IllegalArgumentException e)
						{	result = false;
						}
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Creates all the entities based on the specified similarity sets
	 * of mentions. Each entity corresponds to one similarity set.
	 * 
	 * @param simsets
	 * 		List of similarity sets of mentions.
	 * @return
	 * 		The group of corresponding entities
	 */
	private Entities createEntities(List<List<AbstractMention<?>>> simsets)
	{	logger.log("Creating the entities corresponding to the similarity set");
		logger.increaseOffset();
		ProcessorName resolverName = resolver.getName();
		Entities result = new Entities(resolverName);

		// build the entities corresponding to the sets of similar mentions
		int i = 1;
		for(List<AbstractMention<?>> simset: simsets)
		{	logger.log("Similarity set #"+i+"/"+simsets.size());
			logger.increaseOffset();
			
			// find one mention and create the entity (if valued, need a value)
			AbstractEntity entity = null;
			Iterator<AbstractMention<?>> it = simset.iterator();
			int j = 1;
			String name = null;
			EntityType type = null;
			while(it.hasNext() && entity==null)
			{	AbstractMention<?> mention = it.next();
				type = mention.getType();
				if(name==null)
					name = mention.getStringValue();
				logger.log("Mention "+j+"/"+simset.size()+": "+mention);
				
				// init the corresponding entity
				if(type.isNamed())
				{	String surfaceForm = mention.getStringValue();
					entity = AbstractNamedEntity.buildEntity(-1, surfaceForm, type);
				}
				else
				{	Comparable<?> value = mention.getValue();
					if(value!=null)
						entity = AbstractValuedEntity.buildEntity(-1l, value, type);
				}
			}
			
			if(entity==null)
				logger.log("Cannot a build a value for this set ("+name+"): none of its mentions possesses a value");
			else
			{	result.addEntity(entity);
				
				// update the mentions in the similarity set
				it = simset.iterator();
				j = 1;
				while(it.hasNext())
				{	// get the mention
					AbstractMention<?> mention = it.next();
					logger.log("Mention "+j+"/"+simset.size()+": "+mention);

					// update the entity
					if(type.isNamed())
					{	AbstractNamedEntity e = (AbstractNamedEntity)entity;
						String surfaceForm = mention.getStringValue();
						e.addSurfaceForm(surfaceForm);
					}
					
					// update the mention
					mention.setEntity(entity);
					
					j++;
				}
			}
			
			i++;
			logger.decreaseOffset();
		}
		
		logger.log("Done: "+result.getEntities().size()+" entities created");
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Entities convert(Article article, Entities data, Mentions mentions) throws ProcessorException 
	{	// in this case, this method is actually useless
		return data;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    @Override
    protected void writeRawResults(Article article, Entities intRes) throws IOException
    {	Set<AbstractEntity> entities = intRes.getEntities();
    	
    	// build the string
    	StringBuffer string = new StringBuffer();
    	for(AbstractEntity entity: entities)
    		string.append(entity.toString());
    	
        writeRawResultsStr(article, string.toString());
    }
}
