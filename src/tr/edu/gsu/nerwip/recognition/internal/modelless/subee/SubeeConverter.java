package tr.edu.gsu.nerwip.recognition.internal.modelless.subee;

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

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.mention.AbstractMention;
import tr.edu.gsu.nerwip.data.entity.mention.Mentions;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to {@link Subee}.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class SubeeConverter extends AbstractInternalConverter<List<AbstractMention<?>>>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the NER tool.
	 * 
	 * @author Yasa Akbulut
	 * @author Vincent Labatut
	 */
	public SubeeConverter(String nerFolder)
	{	super(RecognizerName.SUBEE, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, List<AbstractMention<?>> mentions) throws ConverterException
	{	Mentions result = new Mentions(recognizerName);
		
		for(AbstractMention<?> mention: mentions)
			result.addMention(mention);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, List<AbstractMention<?>> mentions) throws IOException
	{	StringBuffer string = new StringBuffer();
		
		for(AbstractMention<?> mention: mentions)
			string.append(mention.toString() + "\n");
			
		writeRawResultsStr(article, string.toString());
	}
}
