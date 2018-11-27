package fr.univavignon.tools.html;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * This class contains a set of methods related to HTML managment.
 * 
 * @author Vincent Labatut
 */
public class HtmlTools
{	
	/////////////////////////////////////////////////////////////////
	// TEXT				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the length of the longest uninterrupted text in the
	 * specified element and its descendants. Here, by uninterrupted
	 * we mean: contained in a single text node.
	 * 
	 * @param element
	 * 		The element to process.
	 * @return
	 * 		The length of the longest uninterrupted text found (0 for none). 
	 */
	public static int getMaxTextLength(Element element)
	{	int result = 0;
		
		for(Node node: element.childNodes())
		{	int tempLength = 0;
		
			// html element
			if(node instanceof Element)
			{	Element elt = (Element)node;
				tempLength = getMaxTextLength(elt);
			}
		
			// textual content
			else if(node instanceof TextNode)
			{	TextNode txt = (TextNode)node;
				String txtStr = txt.text();
				tempLength = txtStr.length();
				
			}
			
			// update result
			if(tempLength>result)
				result = tempLength;
		}
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// TIME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Extract a date from the specified TIME html element.
	 *  
	 * @param timeElt
	 * 		HTML element.
	 * @param dateFormat 
	 * 		Format used to parse the date.
	 * @return
	 * 		The corresponding date.
	 */
	public static Date getDateFromTimeElt(Element timeElt, DateFormat dateFormat)
	{	Date result = null;
	
		String valueStr = timeElt.attr(HtmlNames.ATT_DATETIME);
		try
		{	result = dateFormat.parse(valueStr);
		}
		catch (ParseException e)
		{	e.printStackTrace();
		}
	
		return result;
	}
}
