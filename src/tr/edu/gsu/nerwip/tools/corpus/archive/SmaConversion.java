package tr.edu.gsu.nerwip.tools.corpus.archive;

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
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;

/**
 * This class is a specific form of external converter, used
 * to process files generated using 
 * <a href="http://nlp.stanford.edu/software/">Stanford Manual Annotation Tool<a>.
 * 
 * @deprecated 
 * 		Stanford annotator is not used anymore, 
 * 		we have our own application for that.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class SmaConversion extends AbstractExternalConverter
{	
	/**
	 * Builds a new SMAT converter.
	 */
	public SmaConversion()
	{	super(RecognizerName.REFERENCE, null, FileNames.FI_REFERENCE_TEXT);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Pattern used for conversion */
	private static final Pattern SEARCH_PATTERN = Pattern.compile("<(.+?)>(.+?)</.+?>",Pattern.DOTALL);

	@Override
	public Entities convert(Article article, String content) throws ConverterException
	{	Entities result = new Entities(recognizerName);
		
	// change the form of tags
		// quick and dirty fix. a better version would be to replace the main regex.
		// i.e. <(.+?)>...
		content = content.replaceAll("<tag name=\"PERSON\" value=\"start\"/>", "<PERSON>");
		content = content.replaceAll("<tag name=\"PERSON\" value=\"end\"/>", "</PERSON>");
		content = content.replaceAll("<tag name=\"ORGANIZATION\" value=\"start\"/>", "<ORGANIZATION>");
		content = content.replaceAll("<tag name=\"ORGANIZATION\" value=\"end\"/>", "</ORGANIZATION>");
		content = content.replaceAll("<tag name=\"LOCATION\" value=\"start\"/>", "<LOCATION>");
		content = content.replaceAll("<tag name=\"LOCATION\" value=\"end\"/>", "</LOCATION>");
		content = content.replaceAll("<tag name=\"DATE\" value=\"start\"/>", "<DATE>");
		content = content.replaceAll("<tag name=\"DATE\" value=\"end\"/>", "</DATE>");
		
		// extract entities
		//"<(.+?)>(.+?)<.+?>" causes problems with newline inside tags
		// we have to use Pattern.DOTALL to deal with that
		Matcher matcher = SEARCH_PATTERN.matcher(content);
		while(matcher.find())
		{	String typeStr = matcher.group(1);
if(typeStr.equals("/ORGANIZATION"))
	System.out.print("");
if(typeStr.equals("/PERSON"))
	System.out.print("");
if(typeStr.equals("/LOCATION"))
	System.out.print("");
			EntityType type = EntityType.valueOf(typeStr);
			int startPos = matcher.start();
			int endPos = matcher.end();
			RecognizerName source = RecognizerName.REFERENCE;
			String valueStr = matcher.group(2);//matcher.group();
//			String value = matcher.group(2);
			AbstractEntity<?> entity = AbstractEntity.build(type, startPos, endPos, source, valueStr);
			result.addEntity(entity);
		}
		
		// correct positions
		List<AbstractEntity<?>> entityList = result.getEntities();
		int rollingCount = 0;
		for (AbstractEntity<?> entity : entityList)
		{	int startPos = entity.getStartPos();
			int endPos = entity.getEndPos();
			entity.setStartPos(startPos-rollingCount);
			entity.setEndPos(endPos-(rollingCount));
			int length = endPos - startPos;
			int shift = length - entity.getStringValue().length();
			rollingCount = rollingCount + shift;
			entity.setEndPos(entity.getEndPos()-shift);
		}
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected String readRawResults(Article article) throws FileNotFoundException
	{	File file = getRawFile(article);
		Scanner scanner = FileTools.openTextFileRead(file); //used to force UTF-8 >> necessary ?
		
		StringBuffer temp = new StringBuffer();
		while(scanner.hasNextLine())
			temp.append(scanner.nextLine());
		
		scanner.close();
		String content = temp.toString();
		return content;
	}
}
