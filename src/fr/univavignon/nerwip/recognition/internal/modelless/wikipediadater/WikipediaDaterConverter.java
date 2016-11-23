package fr.univavignon.nerwip.recognition.internal.modelless.wikipediadater;

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
import java.util.List;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.mention.MentionDate;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.recognition.ConverterException;
import fr.univavignon.nerwip.recognition.ProcessorName;
import fr.univavignon.nerwip.recognition.internal.AbstractInternalConverter;
import fr.univavignon.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to {@link WikipediaDater}.
 * 
 * @author Vincent Labatut
 */
public class WikipediaDaterConverter extends AbstractInternalConverter<List<MentionDate>>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the recognizer.
	 */
	public WikipediaDaterConverter(String nerFolder)
	{	super(ProcessorName.WIKIPEDIADATER, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, List<MentionDate> mentions) throws ConverterException
	{	Mentions result = new Mentions(processorName);
		
		for(MentionDate mention: mentions)
			result.addMention(mention);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, List<MentionDate> mentions) throws IOException
	{	StringBuffer string = new StringBuffer();
		
		for(MentionDate mention: mentions)
			string.append(mention.toString() + "\n");
			
		writeRawResultsStr(article, string.toString());
	}
}
