package fr.univavignon.nerwip.processing.internal.modelless.wikidatalinker;

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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.jdom2.JDOMException;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.AbstractEntity;
import fr.univavignon.common.data.entity.AbstractNamedEntity;
import fr.univavignon.common.data.entity.Entities;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.common.tools.wikimedia.WmCommonTools;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateLinker;

/**
 * This implements the actual work of entity linking for 
 * {@link WikiDataLinker}.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
class WikiDataLinkerDelegateLinker extends AbstractModellessInternalDelegateLinker<Entities>
{
	/**
	 * Builds and sets up an object representing
	 * the WikiDataLinker delegate for linking.
	 * 
	 * @param wikiDataLinker
	 * 		Linker in charge of this delegate.
	 * @param revision
	 * 		Whether or not merge entities previously considered
	 * 		as distinct, but turning out to be linked to the same id.
	 */
	public WikiDataLinkerDelegateLinker(WikiDataLinker wikiDataLinker, boolean revision)
	{	super(wikiDataLinker, revision);
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = linker.getName().toString();
		
		result = result + "_" + "revision=" + revision;
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types linked by WikiDataLinker */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList
	(	EntityType.FUNCTION,
		EntityType.LOCATION,
		EntityType.MEETING,
		EntityType.ORGANIZATION,
		EntityType.PERSON,
		EntityType.PRODUCTION
	);

	@Override
	public List<EntityType> getHandledEntityTypes()
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this recognizer can treat */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList
	(	ArticleLanguage.EN,
		ArticleLanguage.FR
	);

	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Entities linkEntities(Article article, Mentions mentions, Entities entities) throws ProcessorException
	{	Set<AbstractEntity> entityList = entities.getEntities();
		logger.log("Starting linking entities ("+entityList.size()+")");
		logger.increaseOffset();
		ArticleLanguage language = article.getLanguage();
		
		int i = 1;
		for(AbstractEntity entity: entityList)
		{	if(entity instanceof AbstractNamedEntity)
			{	logger.log("Processing named entity "+i+"/"+entityList.size()+": "+entity);
				AbstractNamedEntity e = (AbstractNamedEntity)entity;
				try
				{	WmCommonTools.lookupNamedEntity(e, language);
				}
				catch (ClientProtocolException e1)
				{	//e1.printStackTrace();
					throw new ProcessorException(e1.getMessage());
				}
				catch (IOException e1)
				{	//e1.printStackTrace();
					throw new ProcessorException(e1.getMessage());
				}
				catch (JDOMException e1)
				{	//e1.printStackTrace();
					throw new ProcessorException(e1.getMessage());
				}
			}
			else
				logger.log("Entity "+i+"/"+entityList.size()+" is not named: "+entity);
			i ++;
		}
		
		logger.decreaseOffset();
		return entities;
	}

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void convert(Article article, Mentions mentions, Entities entities, Entities data) throws ProcessorException 
	{	ProcessorName linkerName = linker.getName();
		// in the specific case of this linker, both entities and data objects are the same
		entities.setLinker(linkerName);
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
    protected void writeRawResults(Article article, Entities entities) throws IOException
    {	String string = entities.toString();
        writeRawResultsStr(article, string);
    }
}
