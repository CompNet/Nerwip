package tr.edu.gsu.nerwip.linking.internal.modelless.spotlight;

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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.data.entity.mention.AbstractMention;
import tr.edu.gsu.nerwip.data.entity.mention.MentionDate;
import tr.edu.gsu.nerwip.data.entity.mention.Mentions;
import tr.edu.gsu.nerwip.linking.internal.modelless.AbstractModellessInternalLinker;
import tr.edu.gsu.nerwip.linking.LinkerException;
import tr.edu.gsu.nerwip.linking.LinkerName;
import tr.edu.gsu.nerwip.linking.internal.modelless.AbstractModellessInternalLinker;

/**
 * Takes advantage of the DBpedia Spotlight Web service to link
 * mentions to entities from the DBpedia database.
 * 
 * @author Vincent Labatut
 */
public class DbpSpotlight extends AbstractModellessInternalLinker<List<MentionDate>, DbpSpotlightConverter>
{
	/**
	 * Builds and sets up an object representing
	 * a DBpedia Spotlight linker.
	 */
	public DbpSpotlight()
	{	super();
		
		// init converter
		converter = new DbpSpotlightConverter(getFolder());
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public LinkerName getName()
	{	return LinkerName.SPOTLIGHT;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = getName().toString();
		
		// no options
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types detected by this linker */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList(
		EntityType.DATE //TODO complete
	);
	
	@Override
	public List<EntityType> getHandledMentionTypes()
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
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected List<MentionDate> linkEntities(Article article, Mentions mentions, Entities entities) throws LinkerException
	{	logger.increaseOffset();
		List<MentionDate> result = new ArrayList<MentionDate>();
		List<AbstractMention<?>> temp = new ArrayList<AbstractMention<?>>();
		String text = article.getRawText();
		
		logger.log("Process each registered pattern");
		for(Pattern pattern: PATTERNS)
		{	Matcher matcher = pattern.matcher(text);
			while(matcher.find())
			{	int startPos = matcher.start();
				int endPos = matcher.end();
				String valueStr = matcher.group();
				MentionDate mention = new MentionDate(startPos, endPos, getName(), valueStr);
				if(positionAlreadyUsed(mention,temp)==null)
				{	result.add(mention);
					temp.add(mention);
				}
			}
		}
		
		logger.decreaseOffset();
		return result;
	}
}
