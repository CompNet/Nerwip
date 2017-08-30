package fr.univavignon.nerwip.processing;

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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.AbstractEntity;
import fr.univavignon.nerwip.data.entity.AbstractNamedEntity;
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.MentionsEntities;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * The linking process can be implemented either directly in the processor
 * class, or preferably in a delegate class. In the latter case, the delegate
 * must be based on this class.
 * 		 
 * @author Vincent Labatut
 */
public abstract class AbstractDelegateLinker
{	
	/**
	 * Builds a new delegate linker,
	 * using the specified default options.
	 * 
	 * @param linker
	 * 		Linker associated to this delegate.
	 * @param revision
	 * 		Whether or not merge entities previously considered
	 * 		as distinct, but turning out to be linked to the same id.
	 */
	public AbstractDelegateLinker(InterfaceLinker linker, boolean revision)
	{	this.linker = linker;
		
		this.revision = revision;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// LINKER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Linker associated to this delegate */
	protected InterfaceLinker linker;
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the folder containing the results of this
	 * delegate linker.
	 * <br/>
	 * This name takes into account the name of the tool, but also 
	 * the parameters it uses. It can also be used just whenever a 
	 * string representation of the tool and its parameters is needed.
	 * 
	 * @return 
	 * 		Name of the appropriate folder.
	 */
	public abstract String getFolder();
	
	/**
	 * Returns the path of the folder containing the results of this
	 * delegate linker, by considering the associated recognizer and
	 * resolver. It uses {@link #getFolder()}.
	 * 
	 * @return
	 * 		Path of the appropriate folder.
	 */
	public String getFullFolder()
	{	InterfaceResolver resolver = linker.getResolver();
		if(resolver==null)
			resolver = (InterfaceResolver)linker;
		
		InterfaceRecognizer recognizer = resolver.getRecognizer();
		if(recognizer==null)
			recognizer = (InterfaceRecognizer)resolver;
		
		String result = "";
		result = result + File.separator + recognizer.getRecognizerFolder();
		result = result + File.separator + resolver.getResolverFolder();
		result = result + File.separator + getFolder();
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the types of entities this linker
	 * can handle with its current model/parameters.
	 * 
	 * @return 
	 * 		A list of entity types.
	 */
	public abstract List<EntityType> getHandledEntityTypes();
	
	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks whether the specified language is supported by this
	 * linker, given its current settings (parameters, model...).
	 * 
	 * @param language
	 * 		The language to be checked.
	 * @return 
	 * 		{@code true} iff this linker supports the specified
	 * 		language, with its current parameters (model, etc.).
	 */
	public abstract boolean canHandleLanguage(ArticleLanguage language);
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Applies this processor to the specified article,
	 * in order to link entities to unique identifiers in 
	 * databases such as DBpedia or Freelink.
	 * <br/>
	 * The recognizer and resolver that were set up for this linker will 
	 * automatically be applied, or their results will be loaded if their
	 * cache is enabled (and the results are cached). The corresponding 
	 * {@code Mentions} object will be completed and returned wit the
	 * {@link Entities}.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		Sets of the resulting mentions and entities.
	 * 
	 * @throws ProcessorException
	 * 		Problem while resolving co-occurrences. 
	 */
	public abstract MentionsEntities delegateLink(Article article) throws ProcessorException;
	
	/////////////////////////////////////////////////////////////////
	// MERGING ENTITIES		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if this linker should revise the previously resolved entities */
	protected boolean revision;
	
	/**
	 * Looks for entities having one common external id, and merges them.
	 * Then, updates the corresponding mentions.
	 * 
	 * @param mentions
	 * 		Previously detected mentions.
	 * @param entities
	 * 		Previously resolved and linked entities.
	 */
	protected void mergeEntites(Mentions mentions, Entities entities)
	{	logger.increaseOffset();
		
		Set<AbstractEntity> newEntityList = new TreeSet<AbstractEntity>();
		// process separately each entity type
		for(EntityType type: EntityType.values())
		{	// only focus on named entities
			if(type.isNamed())
			{	// get the list containing only entities of this type
				List<AbstractEntity> entityList = new ArrayList<AbstractEntity>(entities.getEntitiesByType(type));
				
				// process each entity iteratively
				int i = 0;
				while(i<entityList.size())
				{	AbstractNamedEntity entity1 = (AbstractNamedEntity)entityList.get(i);
					newEntityList.add(entity1);
					logger.log("Comparing entity "+entity1+" to the remaining entities");
					logger.increaseOffset();
					
					// compare the entity to the rest of the list
					int j = i + 1;
					while(j<entityList.size())
					{	AbstractNamedEntity entity2 = (AbstractNamedEntity)entityList.get(j);
						// if the entities are equivalent
						if(entity1.doExternalIdsIntersect(entity2))
						{	logger.log("Merging entity "+entity2);
							// complete the first entity with the second one
							entity1.completeWith(entity2);
							// remove the second one from the list (it is now redundant)
							entityList.remove(j);
							entities.removeEntity(entity2);
							// update all the mentions using the second entity
							mentions.switchEntity(entity2, entity1);
						}
						// otherwise, just go to the next one
						else
							j++;
					}
					
					i++;
					logger.decreaseOffset();
				}
			}
		}
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// XML FILES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the XML file associated to the specified
	 * article, and representing the linked entities.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		A {@code File} object representing the associated XML result file.
	 */
	public File getEntitiesXmlFile(Article article)
	{	String path = article.getFolderPath()
			+ File.separator + getFullFolder()
			+ File.separator + FileNames.FI_ENTITY_LIST;
		
		File result = new File(path);
		return result;
	}
	
	/**
	 * Returns the XML file associated to the specified
	 * article, and representing the updated mentions.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		A {@code File} object representing the associated XML result file.
	 */
	public File getMentionsXmlFile(Article article)
	{	String path = article.getFolderPath()
			+ File.separator + getFullFolder()
			+ File.separator + FileNames.FI_MENTION_LIST;
	
		File result = new File(path);
		return result;
	}
	
	/**
	 * Write the XML results obtained for the specified article.
	 * This method is meant for both internal and external tools.
	 * 
	 * @param article
	 * 		Concerned article.
	 * @param mentions
	 * 		List of the detected mentions.
	 * @param entities
	 * 		List of the detected entities.
	 * @throws IOException
	 * 		Problem while writing the file.
	 */
	public void writeXmlResults(Article article, Mentions mentions, Entities entities) throws IOException
	{	// possibly write the results as a resolver, if this processor is both a resolver and a linker
		InterfaceResolver resolver = linker.getResolver();
		if(resolver==null)
		{	resolver = (InterfaceResolver)linker;
			resolver.writeResolverResults(article, mentions, entities);
		}
		
		// data files
		File mentionsFile = getMentionsXmlFile(article);
		File entitiesFile = getEntitiesXmlFile(article);
		
		// check folder
		File folder = mentionsFile.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		// create files
		mentions.writeToXml(mentionsFile, entities);
		entities.writeToXml(entitiesFile);
	}
	
	/**
	 * Read the XML representation of the results
	 * previously processed by the linker, for the 
	 * specified article.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		The sets of mentions and entities stored in the XML files.
	 * 
	 * @throws SAXException
	 * 		Problem while reading the file.
	 * @throws IOException
	 * 		Problem while reading the file.
	 * @throws ParseException 
	 * 		Problem while parsing a date. 
	 */
	public MentionsEntities readXmlResults(Article article) throws SAXException, IOException, ParseException
	{	File entitiesFile = getEntitiesXmlFile(article);
		Entities entities = Entities.readFromXml(entitiesFile);
		File mentionsFile = getMentionsXmlFile(article);
		Mentions mentions = Mentions.readFromXml(mentionsFile,entities);
		MentionsEntities result = new MentionsEntities(mentions, entities);
		
//		File dataFile = getEntitiesXmlFile(article);
//		Entities result = Entities.readFromXml(dataFile);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the raw result file associated to the specified
	 * article, i.e. the file possibly generated externally
	 * by the linker.
	 * <br/>
	 * Nothing to do with the raw <i>text</i> of the article,
	 * i.e. its plain textual content.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		A {@code File} object representing the associated raw result file.
	 */
	public File getRawFile(Article article)
	{	String path = article.getFolderPath()
			+ File.separator + getFullFolder()
			+ File.separator + FileNames.FI_OUTPUT_TEXT;
		
		File result = new File(path);
		return result;
	}
	
	/**
	 * Tries to delete the file containing the raw results.
	 * Returns a boolean indicating success ({@code true})
	 * or failure ({@code false}).
	 * 
	 * @param article
	 * 		Concerned article.
	 * @return
	 * 		{@code true} iff the file could be deleted.
	 */
	public boolean deleteRawFile(Article article)
	{	boolean result = false;
		File rawFile = getRawFile(article);
		if(rawFile!=null && rawFile.exists() && rawFile.isFile())
			result = rawFile.delete();
		return result;
	}
}
