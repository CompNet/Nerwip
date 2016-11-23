package fr.univavignon.nerwip.tools.string;

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

/**
 * This class contains various methods
 * used when processing strings representing hyperlinks.
 *  
 * @author Vincent Labatut
 */
public class LinkTools
{
	/////////////////////////////////////////////////////////////////
	// POSITION			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Gets some position in a linked text. The 
	 * specified position is expressed relatively
	 * to the text without links. The returned position
	 * concerns the linked text. Here, 'linked' means
	 * there are html hyperlinks in the text.
	 * 
	 * @param linkedText
	 * 		Text with hyperlinks.
	 * @param position
	 * 		Position in the link-less text.
	 * @return
	 * 		Same position, but in the linked text.
	 */
	public static int getLinkedTextPosition(String linkedText, int position)
	{	int p = 0;
		int result = 0;
		boolean open = false;
		
		// parse text until position is reached
		while(p<position)
		{	char c = linkedText.charAt(result);
			
			if(open)
				open = c!='>';
			else
			{	open = c=='<';
				if(!open)
					p++;
			}
			
			result++;
		}
		
		if(result<linkedText.length())
		{	char c = linkedText.charAt(result);
			if(c=='<')
				result = linkedText.indexOf(">", result) + 1;
		}
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// REMOVAL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Removes a piece of text from the specified linked text,
	 * without dammaging the hyperlinks. If an hyperlink
	 * becomes empty after the removal, then it is itself removed.
	 * 
	 * @param linkedText
	 * 		Original text with hyperlinks.
	 * @param position
	 * 		Start of the piece of text to be removed,
	 * 		expressed without regards for the links.
	 * @param length
	 * 		Length ot the piece of text to be removed,
	 * 		expressed in characters and without regards for the links.
	 * @return
	 * 		Shortened text.
	 */
	public static String removeFromLinkedText(String linkedText, int position, int length)
	{	String result = linkedText;
		int pos = getLinkedTextPosition(linkedText,position);
		
		for(int i=0;i<length;i++)
		{	char c = result.charAt(pos);
			if(c=='<')
				pos = result.indexOf(">", pos) + 1;
			result = result.substring(0,pos) + result.substring(pos+1);
		}
		
		return result;
	}

	/**
	 * Removes empty hyperlinks (i.e. whose element does
	 * not have any text content) from the specified
	 * linked text.
	 * 
	 * @param linkedText
	 * 		Original text with hyperlinks.
	 * @return
	 * 		Linked text without empty hyperlinks.
	 */
	public static String removeEmptyLinks(String linkedText)
	{	String result = linkedText;
		int idx = result.indexOf("></");
		
		while(idx!=-1)
		{	int startPos = idx;
			while(result.charAt(startPos)!='<')
				startPos--;
			int endPos = result.indexOf(">", idx+2);
			result = result.substring(0,startPos) + result.substring(endPos+1);
			idx = result.indexOf("></");
		}
		
		return result;
	}
}
