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
import java.util.List;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.AbstractEntity;
import fr.univavignon.nerwip.data.entity.AbstractNamedEntity;
import fr.univavignon.nerwip.data.entity.AbstractValuedEntity;
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.MentionsEntities;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * The resolution process can be implemented either directly in the processor
 * class, or preferably in a delegate class. In the latter case, the delegate
 * must be based on this class.
 * 		 
 * @author Vincent Labatut
 */
public abstract class AbstractDelegateResolver
{	
	/**
	 * Builds a new delegate resolver,
	 * using the specified default options.
	 * 
	 * @param resolver
	 * 		Resolver associated to this delegate.
	 * @param resolveHomonyms
	 * 		Whether unresolved named entities should be resolved based
	 * 		on exact homonymy, or not.
	 */
	public AbstractDelegateResolver(InterfaceResolver resolver, boolean resolveHomonyms)
	{	this.resolver = resolver;
	
		this.resolveHomonyms = resolveHomonyms;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// RESOLVER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Resolver associated to this delegate */
	protected InterfaceResolver resolver;

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the folder containing the results of this
	 * delegate resolver.
	 * <br/>
	 * This name takes into account the name of the tool, but also 
	 * the parameters it uses. It can also be used just whenever a 
	 * string representation of the tool and its parameters is needed.
	 * 
	 * @return 
	 * 		Name of the appropriate folder.
	 */
	public abstract String getFolder();
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the types of entities this resolver
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
	 * resolver, given its current settings (parameters, model...).
	 * 
	 * @param language
	 * 		The language to be checked.
	 * @return 
	 * 		{@code true} iff this resolver supports the specified
	 * 		language, with its current parameters (model, etc.).
	 */
	public abstract boolean canHandleLanguage(ArticleLanguage language);
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Applies this processor to the specified article,
	 * in order to resolve co-occurrences.
	 * <br/>
	 * The recognizer that was set up for this resolver will automatically
	 * be applied, or its results will be loaded if its cache is enabled 
	 * (and the results are cached). The corresponding {@code Mentions}
	 * object will be completed and returned with the {@link Entities}.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @return
	 * 		Sets of the resulting mentions and entities.
	 * 
	 * @throws ProcessorException
	 * 		Problem while resolving co-occurrences. 
	 */
	public abstract MentionsEntities delegateResolve(Article article) throws ProcessorException;
	
	/**
	 * If the resolver did not associate any entity to a mention, this method
	 * creates an <i>ad hoc</i> entity specially for the mention, and associates
	 * it to the mention. So, it creates entities and modifies existing mentions.
	 * 
	 * @param mentions
	 * 		Current mentions.
	 * @param entities
	 * 		Current entities.
	 */
	protected void complete(Mentions mentions, Entities entities)
	{	List<AbstractMention<?>> mentionList = mentions.getMentions();
		for(AbstractMention<?> mention: mentionList)
		{	AbstractEntity entity = mention.getEntity();
			if(entity==null)
			{	EntityType type = mention.getType();
				if(type.isNamed())
				{	String name = mention.getStringValue();
					if(resolveHomonyms)
					{	List<AbstractNamedEntity> list = entities.getNamedEntitiesByName(name);
						if(list.size()==1)
							entity = list.get(0);
						else if(list.size()>1)
							logger.log("WARNING: several entities already have the same name >> creating a new one despite the user will to relate homonyms.");
					}
					if(entity==null)
					{	entity = AbstractNamedEntity.buildEntity(-1, name, type);
						entities.addEntity(entity);
					}
				}
				else
				{	// for the values, there is no homonymy risk, but we don't add any entity if the value is missing
					Comparable<?> value = mention.getValue();
					if(value!=null)
					{	entity = entities.getValuedEntityByValue(value);
						if(entity==null)
						{	entity = AbstractValuedEntity.buildEntity(-1, value, type);
							entities.addEntity(entity);
						}
					}
				}
				mention.setEntity(entity);
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// RESOLVE HOMONYMS		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not unresolved named entities should be resolved through homonymy */
	protected boolean resolveHomonyms = true;
	
	/////////////////////////////////////////////////////////////////
	// XML FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the XML file associated to the specified
	 * article, and representing the resolved entities.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		A {@code File} object representing the associated XML result file.
	 */
	public File getEntitiesXmlFile(Article article)
	{	String resultsFolder = article.getFolderPath();
		String resolverFolder = getFolder();
		
		InterfaceRecognizer recognizer = resolver.getRecognizer();
		if(recognizer==null)
			resultsFolder = resultsFolder + File.separator + resolverFolder;
		else
			resultsFolder = resultsFolder + File.separator + recognizer.getRecognizerFolder();
		
		resultsFolder = resultsFolder + File.separator + resolverFolder;
		String filePath = resultsFolder + File.separator + FileNames.FI_ENTITY_LIST;
		
		File result = new File(filePath);
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
	{	String resultsFolder = article.getFolderPath();
		String resolverFolder = getFolder();
	
		InterfaceRecognizer recognizer = resolver.getRecognizer();
		if(recognizer==null)
			resultsFolder = resultsFolder + File.separator + resolverFolder;
		else
			resultsFolder = resultsFolder + File.separator + recognizer.getRecognizerFolder();
		
		resultsFolder = resultsFolder + File.separator + resolverFolder;
		String filePath = resultsFolder + File.separator + FileNames.FI_MENTION_LIST;
		
		File result = new File(filePath);
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
	{	// data files
		File entitiesFile = getEntitiesXmlFile(article);
		File mentionsFile = getMentionsXmlFile(article);
		
		// check folder
		File folder = entitiesFile.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		// create files
		entities.writeToXml(entitiesFile);
		mentions.writeToXml(mentionsFile,entities);
	}
	
	/**
	 * Read the XML representation of the results
	 * previously processed by the resolver, for the 
	 * specified article.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		Sets of previously processed mentions and entities.
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
		return result;
		
//		Iterator<AbstractMention<?>> it1 = mentions.getMentions().iterator();
//		Iterator<AbstractMention<?>> it2 = temp.getMentions().iterator();
//		while(it1.hasNext() && it2.hasNext())
//		{	AbstractMention<?> m1 = it1.next();
//			AbstractMention<?> m2 = it2.next();
//			if(m1.equals(m2))
//			{	AbstractEntity entity = m2.getEntity();
//				m1.setEntity(entity);
//			}
//		}
//		
//		if(it1.hasNext() || it2.hasNext())
//			throw new IllegalArgumentException("ERROR: different numbers of mentions in the existing and loaded mention sets");
	}

	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the raw result file associated to the specified
	 * article, i.e. the file possibly generated externally
	 * by the resolver.
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
	{	String resultsFolder = article.getFolderPath();
		String resolverFolder = getFolder();
		
		InterfaceRecognizer recognizer = resolver.getRecognizer();
		if(recognizer==null)
			resultsFolder = resultsFolder + File.separator + resolverFolder;
		else
			resultsFolder = resultsFolder + File.separator + recognizer.getRecognizerFolder();
		
		resultsFolder = resultsFolder + File.separator + resolverFolder;
		String filePath = resultsFolder + File.separator + FileNames.FI_OUTPUT_TEXT;
		
		File result = new File(filePath);
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
