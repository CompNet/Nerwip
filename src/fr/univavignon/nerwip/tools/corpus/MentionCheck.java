package fr.univavignon.nerwip.tools.corpus;

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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class contains various methods used to verify that reference mentions
 * are correct: no overlap, consistent position, no empty mentions, etc.
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class MentionCheck
{	
	/**
	 * Method used to do some punctual processing.
	 * 
	 * @param args
	 * 		None needed.
	 * 
	 * @throws Exception 
	 * 		Whatever.
	 */
	public static void main(String[] args) throws Exception
	{	logger.setName("Mention-check");
		List<File> articles = ArticleLists.getArticleList();
		
//		checkMentionConsistance(articles);
//		checkMentionOverlap(articles);
		checkMentionEmptiness(articles);
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CONSISTANCE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Parse the xml reference associated to the specified
	 * articles and for each mention, checks if the string
	 * of the mention string is equal to that retrieved from the text
	 * using the start and end positions of the mention. If it's not
	 * the case, a message is displayed to allow manual correction.
	 * 
	 * @param articles
	 * 		List of folders corresponding to articles.
	 * 
	 * @throws IOException 
	 * 		Problem while accessing the articles.
	 * @throws SAXException 
	 * 		Problem while accessing the articles.
	 * @throws ParseException 
	 * 		Problem while accessing the articles.
	 */
	private static void checkMentionConsistance(List<File> articles) throws ParseException, SAXException, IOException
	{	logger.log("Checking all mentions");
		logger.increaseOffset();
		
		int problems = 0;
		
		logger.log("Processing each article separately");
		logger.increaseOffset();
		for(File file: articles)
		{	String name = file.getName();
			logger.log("Processing article \"" + name + "\"");
			logger.increaseOffset();
			Article article = Article.read(name);
			String text = article.getRawText();
			
			Mentions mentions = article.getReferenceMentions();
			List<AbstractMention<?>> list = mentions.getMentions();
			
			for(AbstractMention<?> mention: list)
			{	int start = mention.getStartPos();
				int end = mention.getEndPos();
				String textStr = text.substring(start, end);
				String mentionStr = mention.getStringValue();
				if(!mentionStr.equals(textStr))
				{	logger.log("Problem with mention "+mention+" mention=\""+mentionStr+"\" vs. text=\""+textStr+"\"");
					problems++;
				}
			}
			
			logger.decreaseOffset();
			logger.log("Article '" + name + "' completed");
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Mention check over: "+problems+" detected");
	}

	/////////////////////////////////////////////////////////////////
	// OVERLAP			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Parses the xml reference associated to the specified
	 * articles and for each mention, checks if there exist
	 * overlapping mentions. If some are detected, a warning 
	 * message is displayed.
	 * 
	 * @param articles
	 * 		List of folders corresponding to articles.
	 * 
	 * @throws IOException 
	 * 		Problem while accessing the articles.
	 * @throws SAXException 
	 * 		Problem while accessing the articles.
	 * @throws ParseException 
	 * 		Problem while accessing the articles.
	 */
	private static void checkMentionOverlap(List<File> articles) throws ParseException, SAXException, IOException
	{	logger.log("Checking all mentions");
		logger.increaseOffset();
		
		int problems = 0;
		
		logger.log("Processing each article separately");
		logger.increaseOffset();
		for(File file: articles)
		{	String name = file.getName();
			logger.log("Processing article \"" + name + "\"");
			logger.increaseOffset();
			Article article = Article.read(name);
			String text = article.getRawText();
			
			Mentions mentions = article.getReferenceMentions();
			mentions.sortByPosition();
			List<AbstractMention<?>> list = mentions.getMentions();
			
			Iterator<AbstractMention<?>> it = list.iterator();
			AbstractMention<?> current = null;
			AbstractMention<?> prev = null;
			while(it.hasNext())
			{	prev = current;
				current = it.next();
				if(prev!=null)
				{	if(prev.overlapsWith(current))
					{	logger.log("Overlapping mentions detected:");
						logger.increaseOffset();
						logger.log("Previous mention: "+prev); 
						logger.log("Current mention: "+current); 
						logger.decreaseOffset();
						problems++;
					}
				}
			}
			
			logger.decreaseOffset();
			logger.log("Article '" + name + "' completed");
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Mention check over: "+problems+" detected");
	}

	/////////////////////////////////////////////////////////////////
	// EMPTINESS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Parses the xml reference associated to the specified
	 * articles and for each mention, checks if it contains
	 * at least one character. Otherwise, a warning message 
	 * is displayed.
	 * 
	 * @param articles
	 * 		List of folders corresponding to articles.
	 * 
	 * @throws IOException 
	 * 		Problem while accessing the articles.
	 * @throws SAXException 
	 * 		Problem while accessing the articles.
	 * @throws ParseException 
	 * 		Problem while accessing the articles.
	 */
	private static void checkMentionEmptiness(List<File> articles) throws ParseException, SAXException, IOException
	{	logger.log("Checking all mentions");
		logger.increaseOffset();
		
		int problems = 0;
		
		logger.log("Processing each article separately");
		logger.increaseOffset();
		for(File file: articles)
		{	String name = file.getName();
			logger.log("Processing article \"" + name + "\"");
			logger.increaseOffset();
			Article article = Article.read(name);
			String text = article.getRawText();
			
			Mentions mentions = article.getReferenceMentions();
			mentions.sortByPosition();
			List<AbstractMention<?>> list = mentions.getMentions();
			
			for(AbstractMention<?> mention: list)
			{	int start = mention.getStartPos();
				int end = mention.getEndPos();
				if(start==end)
				{	logger.log("Problem with mention "+mention+": it is empty.");
					problems++;
				}
			}
			
			logger.decreaseOffset();
			logger.log("Article '" + name + "' completed");
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		logger.log("Mention check over: "+problems+" detected");
	}
}
