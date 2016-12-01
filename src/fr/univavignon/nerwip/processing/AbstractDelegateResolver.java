package fr.univavignon.nerwip.processing;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.AbstractEntity;
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * TODO 
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
	 */
	public AbstractDelegateResolver(InterfaceResolver resolver)
	{	this.resolver = resolver;
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
	 * If {@code mentions} is {@code null}, the recognizer is applied to get
	 * the mentions. If {@code recognizer} is this object and must be applied, 
	 * then Nerwip tries to perform simultaneously mention recognition and coreference 
	 * resolution, if the processor allows it. Otherwise, the same processor is applied 
	 * separately for both tasks.
	 * <br/>
	 * Note the {@code Mention} object will be completed so as to point towards 
	 * their assigned entities.
	 * 
	 * @param article
	 * 		Article to be processed.
	 * @param mentions
	 * 		List of the previously recognized mentions.
	 * @param recognizer
	 * 		Processor used to recognize the entity mentions.
	 * @return
	 * 		List of the entities associated to the mentions.
	 * 
	 * @throws ProcessorException
	 * 		Problem while resolving co-occurrences. 
	 */
	public abstract Entities delegateResolve(Article article, Mentions mentions, InterfaceRecognizer recognizer) throws ProcessorException;

	/////////////////////////////////////////////////////////////////
	// XML FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the XML file associated to the specified
	 * article.
	 * 
	 * @param article
	 * 		Article to process.
	 * @return
	 * 		A {@code File} object representing the associated XML result file.
	 */
	public File getXmlFile(Article article)
	{	String resultsFolder = article.getFolderPath();
		String resolverFolder = getFolder();
		if(resolverFolder!=null)
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
	{	// data file
		File file = getXmlFile(article);
		
		// check folder
		File folder = file.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		mentions.writeToXml(file,entities);
	}
	
	/**
	 * Read the XML representation of the results
	 * previously processed by the resolver, for the 
	 * specified article.
	 * 
	 * @param article
	 * 		Article to process.
	 * @param mentions
	 * 		Set of existing mentions, to be completed by this method.
	 * @param entities
	 * 		Empty set of entities, to be completed by this method.
	 * 
	 * @throws SAXException
	 * 		Problem while reading the file.
	 * @throws IOException
	 * 		Problem while reading the file.
	 * @throws ParseException 
	 * 		Problem while parsing a date. 
	 */
	public <T extends AbstractEntity> void readXmlResults(Article article, Mentions mentions, Entities entities) throws SAXException, IOException, ParseException
	{	File dataFile = getXmlFile(article);
		
		Mentions temp = Mentions.readFromXml(dataFile,entities);
		
		Iterator<AbstractMention<?,?>> it1 = mentions.getMentions().iterator();
		Iterator<AbstractMention<?,?>> it2 = temp.getMentions().iterator();
		while(it1.hasNext() && it2.hasNext())
		{	AbstractMention<?,?> m1 = it1.next();
			AbstractMention<?,?> m2 = it2.next();
			if(m1.equals(m2))
			{	T entity = //TODO might need to go back to unchecked entity in mentions
				m1.setEntity(entity);
				
			}
		}
		
		if(it1.hasNext() || it2.hasNext())
			throw new IllegalArgumentException("problem in readXmlResults: different numbers of mentions");
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
		if(resolverFolder!=null)
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
