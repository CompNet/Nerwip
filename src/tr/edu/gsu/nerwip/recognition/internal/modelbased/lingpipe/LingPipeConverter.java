package tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe;

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

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.data.entity.mention.AbstractMention;
import tr.edu.gsu.nerwip.data.entity.mention.Mentions;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.ProcessorName;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;

/**
 * This class is the converter associated to LingPipe.
 * It is able to convert the objects outputed by this recognizer
 * into those compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * 
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public class LingPipeConverter extends AbstractInternalConverter<Chunking>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the recognizer.
	 */
	public LingPipeConverter(String nerFolder)
	{	super(ProcessorName.LINGPIPE, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, Chunking chunking) throws ConverterException
	{	Mentions result = new Mentions(processorName);
		
		String text = chunking.charSequence().toString();
		for(Chunk chunk: chunking.chunkSet())
		{	EntityType type = EntityType.valueOf(chunk.type());
			int startPos = chunk.start();
			int endPos = chunk.end();
			String valueStr = text.substring(startPos,endPos);
			AbstractMention<?> mention = AbstractMention.build(type, startPos, endPos, processorName, valueStr);
			result.addMention(mention);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, Chunking chunking) throws IOException
	{	// convert to string
		StringBuffer mentions = new StringBuffer();
		for(Chunk chunk: chunking.chunkSet())
			mentions.append(chunk.toString()+"\n");
		String text = mentions.toString();
		
		//record
		super.writeRawResultsStr(article, text);
	}
}
