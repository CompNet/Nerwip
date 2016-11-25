package fr.univavignon.nerwip.tools.corpus.archive;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ConverterException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.external.AbstractExternalConverter;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;

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
	{	super(ProcessorName.REFERENCE, null, FileNames.FI_REFERENCE_TEXT);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Pattern used for conversion */
	private static final Pattern SEARCH_PATTERN = Pattern.compile("<(.+?)>(.+?)</.+?>",Pattern.DOTALL);

	@Override
	public Mentions convert(Article article, String content) throws ConverterException
	{	Mentions result = new Mentions(processorName);
		
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
		
		// extract mentions
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
			ProcessorName source = ProcessorName.REFERENCE;
			String valueStr = matcher.group(2);//matcher.group();
//			String value = matcher.group(2);
			AbstractMention<?> mention = AbstractMention.build(type, startPos, endPos, source, valueStr);
			result.addMention(mention);
		}
		
		// correct positions
		List<AbstractMention<?>> mentionList = result.getMentions();
		int rollingCount = 0;
		for (AbstractMention<?> mention : mentionList)
		{	int startPos = mention.getStartPos();
			int endPos = mention.getEndPos();
			mention.setStartPos(startPos-rollingCount);
			mention.setEndPos(endPos-(rollingCount));
			int length = endPos - startPos;
			int shift = length - mention.getStringValue().length();
			rollingCount = rollingCount + shift;
			mention.setEndPos(mention.getEndPos()-shift);
		}
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected String readRawResults(Article article) throws FileNotFoundException, UnsupportedEncodingException
	{	File file = getRawFile(article);
		Scanner scanner = FileTools.openTextFileRead(file, "UTF-8"); //used to force UTF-8 >> necessary ?
		
		StringBuffer temp = new StringBuffer();
		while(scanner.hasNextLine())
			temp.append(scanner.nextLine());
		
		scanner.close();
		String content = temp.toString();
		return content;
	}
}
