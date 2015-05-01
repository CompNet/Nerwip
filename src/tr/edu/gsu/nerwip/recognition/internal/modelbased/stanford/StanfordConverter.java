package tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford;

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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.BeforeAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * This class is the converter associated to Stanford.
 * It is able to convert the text outputed by this NER tool
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
	 * 		Folder used to stored the results of the NER tool.
	 */
	public StanfordConverter(String nerFolder)
	{	super(RecognizerName.STANFORD, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of Stanford type to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	/** This name denotes token which are not entities */
	private final static String NOT_ENTITY = "O";
	
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
//	/** Pattern used to fiund entities in the NER output */
//	private final static Pattern SEARCH_PATTERN = Pattern.compile("<(.+?)>(.+?)</.+?>",Pattern.DOTALL);
	
// old version	
//	@Override
//	public Entities convert(List<List<CoreLabel>> data) throws ConverterException
//	{	Entities result = new Entities(recognizerName);
//				
//		// extract entities from text
//		Matcher matcher = SEARCH_PATTERN.matcher(text);
//		List<AbstractEntity<?>> entities = result.getEntities();
//		while(matcher.find())
//		{	String typeStr = matcher.group(1);
//			ArticleCategory type = CONVERSION_MAP.get(typeStr);
//			int startPos = matcher.start();
//			int endPos = matcher.end();
//			String valueStr = matcher.group();
//			String value = matcher.group(2);
//			AbstractEntity<?> entity = AbstractEntity.build(type, startPos, endPos, recognizerName, valueStr, value);
//			entities.add(entity);
//		}
//		
//		// adjust the positions of entities
//		fixPositions(entities);
//
//		return result;
//	}

	@Override
	public Entities convert(Article article, List<List<CoreLabel>> data) throws ConverterException
	{	Entities result = new Entities(recognizerName);
		
		// consecutive words with the same type are not considered as a single entity by Stanford
		// (at least in the default models)
		// ex: John Zorn will be recognized as two persons (John and Zorn) 
		// some specific process must be conducted, in order to merge them

		// process each sentence separately
		for(List<CoreLabel> sentence: data)
		{	// reset previously detected entity info
			EntityType prevType = null;
			AbstractEntity<?> lastEntity = null;
			
			// process each word separately, trying to merge them
			for(CoreLabel word: sentence)
			{	String typeStr = word.get(CoreAnnotations.AnswerAnnotation.class);
				EntityType type = CONVERSION_MAP.get(typeStr);
				
				// ignore entities whose type was not recognized
				if(type!=null)
				{	// get the potential entity data
					int startPos = word.get(CharacterOffsetBeginAnnotation.class);
					int endPos = word.get(CharacterOffsetEndAnnotation.class);
					String valueStr = word.get(OriginalTextAnnotation.class);
//					String value = word.get(ValueAnnotation.class);
//					String value = word.get(TextAnnotation.class);
					String before = word.get(BeforeAnnotation.class);
					
					// check for continuity with the previous entity
					if(type!=prevType)
					{	// case where we start a new entity
						AbstractEntity<?> entity = AbstractEntity.build(type, startPos, endPos, recognizerName, valueStr);
						result.addEntity(entity);
						lastEntity = entity;
					}
					
					else //if(prevType==type)
					{	// case where we update (recreate, actually) the previous entity
						startPos = lastEntity.getStartPos();
						valueStr = lastEntity.getStringValue() + before + valueStr;
						AbstractEntity<?> entity = AbstractEntity.build(type, startPos, endPos, recognizerName, valueStr);
						result.addEntity(entity);
						result.removeEntity(lastEntity);
						lastEntity = entity;
					}
				}
				prevType = type;
			}
		}

		return result;
	}
	
	/**
	 * Corrects the position of the entities.
	 * This method was used when we were using
	 * the string output of the Stanford tool.
	 * Now, we use directly objects representing
	 * entities, and we don't need it anymore.
	 * Kept for archive purposes.
	 * 
	 * @param entities
	 * 		List of entities whose positions must be fixed.
	 * 
	 * @deprecated 
	 * 		We now directly use the objects outputed
	 * 		by Stanford, and not a single String any more.
	 */
	@SuppressWarnings("unused")
	private void fixPositions(List<AbstractEntity<?>> entities)
	{	int rollingCount = 0;
		
		for(AbstractEntity<?> entity: entities)
		{	int startPos = entity.getStartPos() - rollingCount;
			int endPos = entity.getEndPos() - rollingCount;
			
			int length = endPos - startPos;
			int shift = length - entity.getStringValue().length();
			rollingCount = rollingCount + shift;
			endPos = endPos - shift;

			entity.setStartPos(startPos);
			entity.setEndPos(endPos);
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
				if(!typeStr.equals(NOT_ENTITY))
				{	String string = expression.toString();
					buffer.append(string+"\n");
				}
			}
		}
		
		writeRawResultsStr(article, buffer.toString());
	}
}
