package fr.univavignon.nerwip.recognition.internal.modelbased.illinois;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import LBJ2.parse.LinkedVector;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.Data;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NETagPlain;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NEWord;
import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.recognition.ConverterException;
import fr.univavignon.nerwip.recognition.ProcessorName;
import fr.univavignon.nerwip.recognition.internal.AbstractInternalConverter;
import fr.univavignon.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to the Illinois 
 * Named Entity Tagger. It is able to convert the text outputed 
 * by this recognizer into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * <br/>
 * <b>Note:</b> A part of the main method was adapted 
 * from {@link NETagPlain#tagData}.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class IllinoisConverter extends AbstractInternalConverter<Data>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to store the results of the recognizer.
	 */
	public IllinoisConverter(String nerFolder)
	{	super(ProcessorName.ILLINOIS, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of Illinois type to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("DATE", EntityType.DATE);
		CONVERSION_MAP.put("LOC", EntityType.LOCATION);
		CONVERSION_MAP.put("ORG", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("PER", EntityType.PERSON);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, Data data) throws ConverterException
	{	Mentions result = new Mentions(processorName);
	
		String text = article.getRawText();
		int position = 0;
		Vector<NERDocument> documents = data.documents;
		// processing each Illinois document separately
	    for(NERDocument document: documents)
	    {	// an Illinois document is a list of sentences
	    	Vector<LinkedVector> sentences = document.sentences;
	    	
	    	// processing Illinois sentence separately
	        for(LinkedVector sentence: sentences)
	        {	boolean open = false;
	            
	        	// initializing structures to represent each Illinois word in the sentence
	            String[] predictions = new String[sentence.size()];
	            String[] words = new String[sentence.size()];
	            for(int j=0;j<sentence.size();j++)
	            {	NEWord word = (NEWord)sentence.get(j);
	            	predictions[j] = word.neTypeLevel2;
	            	words[j] = word.form;
	            }
	            
	            // identifying series of Illinois words corresponding to (single) mentions
	            EntityType type = null;
	            int startPos = -1;
	            for(int j=0;j<sentence.size();j++)
	            {	// look for the word in the original text
	            	int temp = position;
	            	position = text.indexOf(words[j], temp);
    				if(position==-1)
    					throw new ConverterException("Cannot find \""+words[j]+"\" in the text, from position "+temp);
	            	
    				// possibly start a new mention if we find a B- marker
	            	if (predictions[j].startsWith("B-")
	            			// of if the previous word is internal, but the previous word was of a different type
	            			|| (j>0 && predictions[j].startsWith("I-") && (!predictions[j-1].endsWith(predictions[j].substring(2)))))
	            	{	String typeStr = predictions[j].substring(2);
	            		type = CONVERSION_MAP.get(typeStr);
	            		if(type!=null)
	            		{	open = true;
	            			startPos = position;
	            		}
	            	}
	            	
	            	// updating current position in the original text
	            	position = position + words[j].length();
	            	
	            	// complete the current mention (which possibly contains several words)
	            	if(open)
	            	{	boolean close = false;
	            		if(j==sentence.size()-1)
	            		{	close = true;
	            		}
	            		else
	            		{	if(predictions[j+1].startsWith("B-"))
	            				close = true;
	            			else if(predictions[j+1].equals("O"))
	            				close = true;
	            			else if(predictions[j+1].indexOf('-')>-1 && (!predictions[j].endsWith(predictions[j+1].substring(2))))
	            				close = true;
	            		}
	            		if(close)
	            		{	// consider all detected words to constitute the mention
	            			String valueStr = text.substring(startPos,position);
	            			AbstractMention<?> mention = AbstractMention.build(type, startPos, position, processorName, valueStr);
	            			result.addMention(mention);
	            			// reset variables to (possibly) start a new mention
	            			open = false;
	            			type = null;
	            			startPos = -1;
	            		}
	            	}
	            }
		    }
	    }

		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, Data data) throws IOException
	{	StringBuffer buffer = new StringBuffer();
		
		Vector<NERDocument> documents = data.documents;
	    for(NERDocument document: documents)
	    {	 Vector<LinkedVector> sentences = document.sentences;
	    	
	        for(LinkedVector vector: sentences)
	        {	boolean open = false;
	            
	            String[] predictions = new String[vector.size()];
	            String[] words = new String[vector.size()];
	            for(int j=0;j<vector.size();j++)
	            {	NEWord word = (NEWord)vector.get(j);
	            	predictions[j] = word.neTypeLevel2;
	            	words[j] = word.form;
	            }
	            
	            for(int j=0;j<vector.size();j++)
	            {	if (predictions[j].startsWith("B-") || (j>0&&predictions[j].startsWith("I-") && (!predictions[j-1].endsWith(predictions[j].substring(2)))))
	            	{	buffer.append("[" + predictions[j].substring(2) + " ");
	            		open = true;
	            	}
	            	
	            	buffer.append(words[j]+ " ");
	            	if(open)
	            	{	boolean close = false;
	            		if(j==vector.size()-1)
	            		{	close = true;
	            		}
	            		else
	            		{	if(predictions[j+1].startsWith("B-"))
	            				close = true;
	            			else if(predictions[j+1].equals("O"))
	            				close = true;
	            			else if(predictions[j+1].indexOf('-')>-1&&(!predictions[j].endsWith(predictions[j+1].substring(2))))
	            				close = true;
	            		}
	            		if(close)
	            		{	buffer.append(" ] ");
	            			open=false;
	            		}
	            	}
	            }
		    }
	    }
		
		writeRawResultsStr(article, buffer.toString());
	}
}
