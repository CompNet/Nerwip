package tr.edu.gsu.nerwip.recognition.external;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.mention.Mentions;
import tr.edu.gsu.nerwip.recognition.AbstractConverter;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.tools.file.FileTools;

/**
 * This class represents a converter for an external recognizer,
 * i.e. a tool executed outside of Nerwip. It reads a file generated
 * by the recognizer and converts it to our internal format.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractExternalConverter extends AbstractConverter
{	
	/**
	 * Builds a new external converter.
	 * 
	 * @param recognizerName
	 * 		Name of the associated recognizer.
	 * @param nerFolder
	 * 		Name of the associated recognizer folder.
	 * @param rawFile
	 * 		Name of the raw file (i.e. external format).
	 */
	public AbstractExternalConverter(RecognizerName recognizerName, String nerFolder, String rawFile)
	{	super(recognizerName,nerFolder,rawFile);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Convert the file (supposedly) externally generated by the recognizer,
	 * into the mention list used internally by Nerwip. The file to be
	 * converted is passed as a string. 
	 * 
	 * @param article
	 * 		The considered article.
	 * @param mentionsStr
	 * 		String representation of the detected mentions.
	 * @return
	 * 		List of mentions detected by the associated recognizer.
	 * 
	 * @throws ConverterException
	 * 		Problem while performing the conversion.
	 */
	public abstract Mentions convert(Article article, String mentionsStr) throws ConverterException;
	
	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Reads the file generated externally by the
	 * associated recognizer to store the detected mentions.
	 * 
	 * @param article 
	 * 		Concerned article.
	 * @return 
	 * 		String representation of the file content.
	 * 
	 * @throws FileNotFoundException 
	 * 		Problem while reading the file.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle the encoding.
	 */
	protected String readRawResults(Article article) throws FileNotFoundException, UnsupportedEncodingException
	{	File file = getRawFile(article);
	
		String result = FileTools.readTextFile(file, "UTF-8");
		return result;
	}
}
