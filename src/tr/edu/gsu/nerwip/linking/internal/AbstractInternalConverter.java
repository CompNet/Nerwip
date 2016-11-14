package tr.edu.gsu.nerwip.linking.internal;

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
import java.io.IOException;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.linking.AbstractConverter;
import tr.edu.gsu.nerwip.linking.LinkerName;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.tools.file.FileTools;

/**
 * This class represents a converter for an internal linker,
 * i.e. a tool executed programmatically from Nerwip.
 * It is able to convert data from the tool format towards
 * Nerwip objects. 
 * 
 * @param <T>
 * 		Internal representation of an entity used by the linker.
 * 		 
 * @author Vincent Labatut
 */
public abstract class AbstractInternalConverter<T> extends AbstractConverter
{	
	/**
	 * Builds a new internal converter.
	 * 
	 * @param linkerName
	 * 		Name of the associated linker.
	 * @param nerFolder
	 * 		Name of the associated linker folder.
	 * @param rawFile
	 * 		Name of the raw file (i.e. external format).
	 */
	public AbstractInternalConverter(LinkerName linkerName, String nerFolder, String rawFile)
	{	super(linkerName,nerFolder,rawFile);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Convert the specified objects, used internally by the associated NER
	 * tool, into the mention list used internally by Nerwip.  
	 * 
	 * @param entity
	 * 		The concerned entity.
	 * @param data
	 * 		Result of the linking process for the specified entity.
	 * @return
	 * 		A String representation of the raw linking result.
	 * 
	 * @throws ConverterException
	 * 		Problem while performing the conversion.
	 */
	public abstract String convert(AbstractEntity entity, T data) throws ConverterException;

	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Write the raw results obtained for the specified article.
	 * This method is meant for internal tools (those executed
	 * programmatically).
	 * 
	 * @param article
	 * 		Concerned article.
	 * @param results
	 * 		String representation of the linker result.		
	 * 
	 * @throws IOException 
	 * 		Problem while recording the file.
	 */
	protected void writeRawResultsStr(Article article, String results) throws IOException
	{	File file = getRawFile(article);
		File folder = file.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		FileTools.writeTextFile(file, results, "UTF-8");
	}

	/**
	 * Records the results of the linking task
	 * in a text file, for archiving purposes.
	 * 
	 * @param article
	 * 		Concerned article.
	 * @param intRes
	 * 		Result of the mention detection, represented using the format internal to the linker.
	 * 
	 * @throws IOException
	 * 		Problem while writing the file.
	 */
	protected abstract void writeRawResults(Article article, T intRes) throws IOException;
}

//TODO see which kind of conversion we actually need.