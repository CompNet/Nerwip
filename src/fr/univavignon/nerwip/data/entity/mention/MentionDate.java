package fr.univavignon.nerwip.data.entity.mention;

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

import org.jdom2.Attribute;
import org.jdom2.Element;

import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.tools.time.Date;
import fr.univavignon.nerwip.tools.xml.XmlNames;

/**
 * This class represents a date mention.
 * 
 * @author Burcu Küpelioğlu
 * @author Vincent Labatut
 */
public class MentionDate extends AbstractMention<Date>
{	
	/**
	 * Builds a new date mention from a date value.
	 * 
	 * @param startPos
	 * 		Starting position in the text.
	 * @param endPos
	 * 		Ending position in the text.
	 * @param source
	 * 		Tool which detected this mention.
	 * @param valueStr
	 * 		String representation in the text.
	 * @param value
	 * 		Actual value of the mention.
	 */
	public MentionDate(int startPos, int endPos, ProcessorName source, String valueStr, Date value)
	{	super(startPos, endPos, source, valueStr, value);
	}
	
	/**
	 * Builds a new date mention without any date value.
	 * 
	 * @param startPos
	 * 		Starting position in the text.
	 * @param endPos
	 * 		Ending position in the text.
	 * @param source
	 * 		Tool which detected this mention.
	 * @param valueStr
	 * 		String representation in the text.
	 */
	public MentionDate(int startPos, int endPos, ProcessorName source, String valueStr)
	{	super(startPos, endPos, source, valueStr, null);
	}
	
	/////////////////////////////////////////////////////////////////
	// TYPE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public EntityType getType()
	{	return EntityType.DATE;
	}

	/////////////////////////////////////////////////////////////////
	// XML				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Element exportAsElement()
	{	Element result = new Element(XmlNames.ELT_MENTION);
		
		Attribute startAttr = new Attribute(XmlNames.ATT_START, Integer.toString(startPos));
		result.setAttribute(startAttr);
		
		Attribute endAttr = new Attribute(XmlNames.ATT_END, Integer.toString(endPos));
		result.setAttribute(endAttr);

		Attribute typeAttr = new Attribute(XmlNames.ATT_TYPE, getType().toString());
		result.setAttribute(typeAttr);
		
		Element stringElt = new Element(XmlNames.ELT_STRING);
		stringElt.setText(valueStr);
		result.addContent(stringElt);
		
		if(value!=null)
		{	Element valueElt = new Element(XmlNames.ELT_VALUE);
			valueElt.setText(value.exportToString());
			result.addContent(valueElt);
		}
		
		return result;
	}
	
	/**
	 * Builds a date mention from the specified
	 * XML element.
	 * 
	 * @param element
	 * 		XML element representing the mention.
	 * @param source
	 * 		Name of the recognizer which detected the mention.
	 * @return
	 * 		The date mention corresponding to the specified element.
	 */
	public static MentionDate importFromElement(Element element, ProcessorName source)
	{	String startStr = element.getAttributeValue(XmlNames.ATT_START);
		int startPos = Integer.parseInt(startStr);
		
		String endStr = element.getAttributeValue(XmlNames.ATT_END);
		int endPos = Integer.parseInt(endStr);
		
		Element stringElt = element.getChild(XmlNames.ELT_STRING);
		String valueStr = stringElt.getText();
		
		Element valueElt = element.getChild(XmlNames.ELT_VALUE);
		Date value = null;
		if(valueElt!=null)
		{	String valueString = valueElt.getText();
			value = Date.importFromString(valueString);
		}
		
		MentionDate result =  new MentionDate(startPos, endPos, source, valueStr, value);
		return result;
	}
}
