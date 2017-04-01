package fr.univavignon.nerwip.processing.internal.modelless.wikipediadater;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.MentionDate;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateRecognizer;

/**
 * Detects most date formats used in Wikipedia articles.
 * Only focuses on dates, though. The value of the dates
 * is not retrieved, just the string representing it.
 * <br/>
 * See the auxiliary classes to see the supported formats:
 * {@link WikipediaDaterPatternsEn} and {@link WikipediaDaterPatternsFr}.
 * 
 * @author Vincent Labatut
 */
public class WikipediaDaterDelegateRecognizer extends AbstractModellessInternalDelegateRecognizer<List<MentionDate>>
{
	/**
	 * Builds and sets up an object representing
	 * a wikipedia dater.
	 * 
	 * @param wikipediaDater
	 * 		Recognizer in charge of this delegate.
	 */
	public WikipediaDaterDelegateRecognizer(WikipediaDater wikipediaDater)
	{	super(wikipediaDater,false,false,false,false);
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
		// no options
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types detected by this recognizer */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList(
		EntityType.DATE
	);
	
	@Override
	public List<EntityType> getHandledEntityTypes()
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this recognizer can treat */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList(
		ArticleLanguage.EN,
		ArticleLanguage.FR
	);

	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PATTERNS_MAP		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of patterns used to detect dates based on the previous regexps */
	private static final Map<ArticleLanguage,List<Pattern>> PATTERNS_MAP = new HashMap<ArticleLanguage,List<Pattern>>();
	/** Init the lists of patterns for all supported language */
	static
	{	PATTERNS_MAP.put(ArticleLanguage.EN, WikipediaDaterPatternsEn.PATTERNS);
		PATTERNS_MAP.put(ArticleLanguage.FR, WikipediaDaterPatternsFr.PATTERNS);
	}

	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected List<MentionDate> detectMentions(Article article) throws ProcessorException
	{	logger.increaseOffset();
		List<MentionDate> result = new ArrayList<MentionDate>();
		List<AbstractMention<?>> temp = new ArrayList<AbstractMention<?>>();
		String text = article.getRawText();
		
		logger.log("Process each registered pattern");
		ArticleLanguage language = article.getLanguage();
		List<Pattern> patterns = PATTERNS_MAP.get(language);
		for(Pattern pattern: patterns)
		{	Matcher matcher = pattern.matcher(text);
			while(matcher.find())
			{	int startPos = matcher.start();
				int endPos = matcher.end();
				String valueStr = matcher.group();
				MentionDate mention = new MentionDate(startPos, endPos, recognizer.getName(), valueStr);
				if(positionAlreadyUsed(mention,temp)==null)
				{	result.add(mention);
					temp.add(mention);
				}
			}
		}
		
		logger.decreaseOffset();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, List<MentionDate> mentions) throws ProcessorException
	{	Mentions result = new Mentions(recognizer.getName());
		
		for(MentionDate mention: mentions)
			result.addMention(mention);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, List<MentionDate> mentions) throws IOException
	{	StringBuffer string = new StringBuffer();
		
		for(MentionDate mention: mentions)
			string.append(mention.toString() + "\n");
			
		writeRawResultsStr(article, string.toString());
	}
}
