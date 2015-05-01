package tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp;

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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import opennlp.tools.util.Span;
import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to OpenCalais.
 * It is able to convert the text outputed by this NER tool
 * into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * 
 * @author Vincent Labatut
 */
public class OpenNlpConverter extends AbstractInternalConverter<Map<EntityType,List<Span>>>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the NER tool.
	 */
	public OpenNlpConverter(String nerFolder)
	{	super(RecognizerName.OPENNLP, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Entities convert(Article article, Map<EntityType,List<Span>> data) throws ConverterException
	{	Entities result = new Entities(recognizerName);
		
		String rawText = article.getRawText();
		for(Entry<EntityType,List<Span>> entry: data.entrySet())
		{	EntityType type = entry.getKey();
			List<Span> spans = entry.getValue();
			for(Span span: spans)
			{	// build internal representation of the entity
				int startPos = span.getStart();
				int endPos = span.getEnd();
				String valueStr = rawText.substring(startPos,endPos);
				AbstractEntity<?> entity = AbstractEntity.build(type, startPos, endPos, recognizerName, valueStr);
				
				// ignore overlapping entities
//				if(!result.hasEntity(entity))	//TODO don't remember if i'm supposed to change that, or what?
					result.addEntity(entity);
			}
		}	

		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, Map<EntityType,List<Span>> intRes) throws IOException
	{	StringBuffer string = new StringBuffer();
		
		String rawText = article.getRawText();
		for(Entry<EntityType,List<Span>> entry: intRes.entrySet())
		{	EntityType type = entry.getKey();
			List<Span> spans = entry.getValue();
			for(Span span: spans)
			{	// build internal representation of the entity
				int startPos = span.getStart();
				int endPos = span.getEnd();
				String valueStr = rawText.substring(startPos,endPos);
				string.append("["+type.toString()+" '"+valueStr+"' ("+startPos+","+endPos+")]\n");
			}
		}
		
		writeRawResultsStr(article, string.toString());
	}
}
