package fr.univavignon.nerwip.recognition.external.nero;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.recognition.ConverterException;
import fr.univavignon.nerwip.recognition.ProcessorName;
import fr.univavignon.nerwip.recognition.external.AbstractExternalConverter;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * This class is the converter associated to Nero. It is able to convert the
 * text outputed by this recognizer into objects compatible with Nerwip. 
 * <br/>
 * It can also read/write these results using raw text and our XML format.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class NeroConverter extends AbstractExternalConverter
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 *            Folder used to stored the results of the recognizer.
	 */
	public NeroConverter(String nerFolder)
	{	super(ProcessorName.NERO, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of URI to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	/** List of ignored entity types */
	private final static List<String> IGNORED_TYPES = Arrays.asList(
		"amount",
		"unk"
	);
	
	/** Initialization of the conversion map */
	static 
	{	CONVERSION_MAP.put("fonc", EntityType.FUNCTION);
		CONVERSION_MAP.put("loc", EntityType.LOCATION);
		CONVERSION_MAP.put("org", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("pers", EntityType.PERSON);
		CONVERSION_MAP.put("prod", EntityType.PRODUCTION);
		CONVERSION_MAP.put("time", EntityType.DATE);
	}
//	/** Ignored characters */
//	private final static List<Character> IGNORED_CHARS = Arrays.asList(
//		'œ','Œ', //this was put in the method used to clean article content
//		'æ','Æ'
//	);
	
	/////////////////////////////////////////////////////////////////
	// PROCESS 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, String data) throws ConverterException
	{	Mentions result = new Mentions(processorName);
		String originalText = article.getRawText();

		LinkedList<EntityType> types = new LinkedList<EntityType>();
		LinkedList<Integer> startPos1 = new LinkedList<Integer>();
//		LinkedList<Integer> startPos2 = new LinkedList<Integer>();
		LinkedList<String> tags = new LinkedList<String>();
		
		int i1 = 0;
		int i2 = 0;
		int c1 = originalText.codePointAt(i1);
		int c2 = data.codePointAt(i2);
		
		// possibly pass starting newline characters 
		while(c1=='\n')
		{	i1++;
			c1 = data.codePointAt(i1);
		}
		while(c2=='\n')
		{	i2++;
			c2 = data.codePointAt(i2);
		}
		
		while(i1<originalText.length() && i2<data.length())
		{	c1 = originalText.codePointAt(i1);
			c2 = data.codePointAt(i2);
if(i2>2800)
	System.out.print("");
			
			// beginning of a tag
			if(c2=='<')
			{	int k2 = i2;
				i2++; 
				c2 = data.codePointAt(i2);
				
				// closing tag
				if(c2=='/')
				{	int j2 = data.indexOf('>', i2);
					String tag = data.substring(i2+1,j2);
					String tag0 = tags.pop();
					if(!tag.equalsIgnoreCase(tag0))
					{	String msg = StringTools.highlightPosition(i2, data, 20);
						logger.log("WARNING: opening tag ("+tag0+") different from closing tag ("+tag+"):\n"+msg);
					}
					i2 = j2 + 1;
					EntityType type = types.pop();
					int sp1 = startPos1.pop();
//					int sp2 = startPos2.pop();
					if(type!=null)
					{
//						String valueStr = data.substring(sp2,k2);
						String valueStr = originalText.substring(sp1,i1);
						AbstractMention<?> mention = AbstractMention.build(type, sp1, i1, ProcessorName.NERO, valueStr);
						mention.correctMentionSpan(); // to remove some spaces located at the end of mentions
						result.addMention(mention);
					}
				}
				
				// opening tag
				else
				{	int j2 = data.indexOf('>', i2);
					String tag = data.substring(i2,j2);
					i2 = j2 + 1;
					tags.push(tag);
					EntityType type = CONVERSION_MAP.get(tag);
					if(type==null && !IGNORED_TYPES.contains(tag))
					{	if(tag.isEmpty())
						{	int end = Math.min(j2+40, data.length());
							String msg = data.substring(k2, end);
							logger.log("WARNING: found an empty tag, settling for a date ("+msg+"[...])");
							type = EntityType.DATE;
						}
						else
						{	String msg = StringTools.highlightPosition(k2, data, 20);
							throw new ConverterException("Found an unknown tag : \""+tag+"\" at "+msg);
						}
					}
					types.push(type);
					startPos1.push(i1);
//					startPos2.push(i2);
				}
			}
			
			// other character (than '<')
			else
			{	
//if(c1=='œ') // debug
//	System.out.print("");

				// similar characters
				if(//IGNORED_CHARS.contains((char)c1) || 
						StringTools.compareCharsRelaxed(c1,c2)==0)// || c2==65533)
				{	// everything's normal
					// >> go to next chars in both texts
					i1++; 
					i2++; 
				}
				
				else
				{	boolean moved = false;
				
					// pass all non-letter and non-digit characters
					if(!Character.isLetterOrDigit(c1))//c1==' ' || c1=='\n' || StringTools.isPunctuation(c1))
					{	i1++;
						moved = true;
					}
					
					// pass all non-letter and non-digit characters
					if(!Character.isLetterOrDigit(c2))//c2==' ' || c2=='\n' || StringTools.isPunctuation(c2))
					{	i2++;
						moved = true;
					}
					
					// if both are letters or digits (but different), we have a problem
					if(!moved)
					{	String msg1 = StringTools.highlightPosition(i1, originalText, 20);
						String msg2 = StringTools.highlightPosition(i2, data, 20);
						throw new ConverterException("Found an untreatable character:\n"+msg1+"\n"+msg2);
					}
				}
			}
		}
		
		// check if we actually processed the whole texts
		if(i1<originalText.length())
		{	
//			// possibly consume the final newline chars
//			do
//			{	c1 = originalText.codePointAt(i1);
//				i1++;
//			}
//			while(i1<originalText.length() && (c1=='\n' || c1==' '));
			
			// possibly consume all non-letter characters
			c1 = originalText.codePointAt(i1);
			while(i1<originalText.length() && !Character.isLetterOrDigit(c1))
			{	i1++;
				if(i1<originalText.length())
					c1 = originalText.codePointAt(i1);
			}
			
			if(i1<originalText.length())
			{	String msg1 = StringTools.highlightPosition(i1, originalText, 20);
				throw new ConverterException("Didn't reach the end of the original text\n"+msg1);
			}
		}
		else if(i2<data.length())
		{	// possibly consume all non-letter characters
			boolean insideTag = false;
			c2 = data.codePointAt(i2);
			while(i2<data.length() && (!Character.isLetterOrDigit(c2)) || insideTag)
			{	if(c2=='<')
					insideTag = true;
				else if(c2=='>')
					insideTag = false;
				i2++;
				if(i2<data.length())
					c2 = data.codePointAt(i2);
			}
			
			if(i2<data.length())
			{	String msg2 = StringTools.highlightPosition(i2, data, 20);
				throw new ConverterException("Didn't reach the end of the annotated text\n"+msg2);
			}
		}
		
		return result;
	}
}
