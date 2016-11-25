package fr.univavignon.nerwip.processing.internal.modelbased.stanford;

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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.BeforeAnnotation;
import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ConverterException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.AbstractInternalConverter;
import fr.univavignon.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to Stanford.
 * It is able to convert the text outputed by this recognizer
 * into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class StanfordConverter extends AbstractInternalConverter<List<List<CoreLabel>>>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the recognizer.
	 */
	public StanfordConverter(String nerFolder)
	{	super(ProcessorName.STANFORD, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of Stanford type to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	/** This name denotes token which are not mentions */
	private final static String NOT_MENTION = "O";
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("DATE", EntityType.DATE);
		CONVERSION_MAP.put("LOCATION", EntityType.LOCATION);
		CONVERSION_MAP.put("ORGANIZATION", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("PERSON", EntityType.PERSON);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/** Pattern used to find mentions in the NER output */
//	private final static Pattern SEARCH_PATTERN = Pattern.compile("<(.+?)>(.+?)</.+?>",Pattern.DOTALL);
	
// old version	
//	@Override
//	public Mentions convert(List<List<CoreLabel>> data) throws ConverterException
//	{	Mentions result = new Mentions(processorName);
//				
//		// extract mentions from text
//		Matcher matcher = SEARCH_PATTERN.matcher(text);
//		List<AbstractEntity<?>> mentions = result.getMentions();
//		while(matcher.find())
//		{	String typeStr = matcher.group(1);
//			ArticleCategory type = CONVERSION_MAP.get(typeStr);
//			int startPos = matcher.start();
//			int endPos = matcher.end();
//			String valueStr = matcher.group();
//			String value = matcher.group(2);
//			AbstractEntity<?> mention = AbstractEntity.build(type, startPos, endPos, processorName, valueStr, value);
//			mentions.add(mention);
//		}
//		
//		// adjust the positions of mentions
//		fixPositions(mentions);
//
//		return result;
//	}

	@Override
	public Mentions convert(Article article, List<List<CoreLabel>> data) throws ConverterException
	{	Mentions result = new Mentions(processorName);
		
		// consecutive words with the same type are not considered as a single mention by Stanford
		// (at least in the default models)
		// ex: John Zorn will be recognized as two persons (John and Zorn) 
		// some specific process must be conducted, in order to merge them

		// process each sentence separately
		for(List<CoreLabel> sentence: data)
		{	// reset previously detected mention info
			EntityType prevType = null;
			AbstractMention<?> lastMention = null;
			
			// process each word separately, trying to merge them
			for(CoreLabel word: sentence)
			{	String typeStr = word.get(CoreAnnotations.AnswerAnnotation.class);
				EntityType type = CONVERSION_MAP.get(typeStr);
				
				// ignore mentions whose type was not recognized
				if(type!=null)
				{	// get the potential mention data
					int startPos = word.get(CharacterOffsetBeginAnnotation.class);
					int endPos = word.get(CharacterOffsetEndAnnotation.class);
					String valueStr = word.get(OriginalTextAnnotation.class);
//					String value = word.get(ValueAnnotation.class);
//					String value = word.get(TextAnnotation.class);
					String before = word.get(BeforeAnnotation.class);
					
					// check for continuity with the previous mention
					if(type!=prevType)
					{	// case where we start a new mention
						AbstractMention<?> mention = AbstractMention.build(type, startPos, endPos, processorName, valueStr);
						result.addMention(mention);
						lastMention = mention;
					}
					
					else //if(prevType==type)
					{	// case where we update (recreate, actually) the previous mention
						startPos = lastMention.getStartPos();
						valueStr = lastMention.getStringValue() + before + valueStr;
						AbstractMention<?> mention = AbstractMention.build(type, startPos, endPos, processorName, valueStr);
						result.addMention(mention);
						result.removeMention(lastMention);
						lastMention = mention;
					}
				}
				prevType = type;
			}
		}

		return result;
	}
	
	/**
	 * Corrects the position of the mentions.
	 * This method was used when we were using
	 * the string output of the Stanford tool.
	 * Now, we use directly objects representing
	 * mentions, and we don't need it anymore.
	 * Kept for archive purposes.
	 * 
	 * @param mentions
	 * 		List of mentions whose positions must be fixed.
	 * 
	 * @deprecated 
	 * 		We now directly use the objects outputed
	 * 		by Stanford, and not a single String any more.
	 */
	@SuppressWarnings("unused")
	private void fixPositions(List<AbstractMention<?>> mentions)
	{	int rollingCount = 0;
		
		for(AbstractMention<?> mention: mentions)
		{	int startPos = mention.getStartPos() - rollingCount;
			int endPos = mention.getEndPos() - rollingCount;
			
			int length = endPos - startPos;
			int shift = length - mention.getStringValue().length();
			rollingCount = rollingCount + shift;
			endPos = endPos - shift;

			mention.setStartPos(startPos);
			mention.setEndPos(endPos);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, List<List<CoreLabel>> data) throws IOException
	{	StringBuffer buffer = new StringBuffer();
		
		for(List<CoreLabel> sentence: data)
		{	for(CoreLabel expression: sentence)
			{	String typeStr = expression.get(CoreAnnotations.AnswerAnnotation.class);
				// we ignore tokens without type
				if(!typeStr.equals(NOT_MENTION))
				{	String string = expression.toString();
					buffer.append(string+"\n");
				}
			}
		}
		
		writeRawResultsStr(article, buffer.toString());
	}
}
