package tr.edu.gsu.nerwip.data.entity;

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

import org.jdom.Attribute;
import org.jdom.Element;

import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.tools.xml.XmlNames;

/**
 * Class representing a location entity.
 * 
 * @author Vincent Labatut
 */
public class EntityLocation extends AbstractEntity<String>
{	
	/**
	 * Builds a new location entity.
	 * 
	 * @param startPos
	 * 		Starting position in the text.
	 * @param endPos
	 * 		Ending position in the text.
	 * @param source
	 * 		Tool which detected this entity.
	 * @param valueStr
	 * 		String representation in the text.
	 * @param value
	 * 		Actual value of the entity (can be the same as {@link #valueStr}).
	 */
	public EntityLocation(int startPos, int endPos, RecognizerName source, String valueStr, String value)
	{	super(startPos, endPos, source, valueStr, value);
	}
	
	/**
	 * Builds a new date location without a value.
	 * 
	 * @param startPos
	 * 		Starting position in the text.
	 * @param endPos
	 * 		Ending position in the text.
	 * @param source
	 * 		Tool which detected this entity.
	 * @param valueStr
	 * 		String representation in the text.
	 */
	public EntityLocation(int startPos, int endPos, RecognizerName source, String valueStr)
	{	super(startPos, endPos, source, valueStr, null);
	}
	
	/////////////////////////////////////////////////////////////////
	// TYPE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public EntityType getType()
	{	return EntityType.LOCATION;
	}

	/////////////////////////////////////////////////////////////////
	// XML				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Element exportAsElement()
	{	Element result = new Element(XmlNames.ELT_ENTITY);
		
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
			valueElt.setText(value);
			result.addContent(valueElt);
		}
		
		return result;
	}

	/**
	 * Builds a location entity from the specified
	 * XML element.
	 * 
	 * @param element
	 * 		XML element representing the entity.
	 * @param source
	 * 		Name of the NER tool which detected the entity.
	 * @return
	 * 		The location entity corresponding to the specified element.
	 */
	public static EntityLocation importFromElement(Element element, RecognizerName source)
	{	String startStr = element.getAttributeValue(XmlNames.ATT_START);
		int startPos = Integer.parseInt(startStr);
		
		String endStr = element.getAttributeValue(XmlNames.ATT_END);
		int endPos = Integer.parseInt(endStr);
		
		Element stringElt = element.getChild(XmlNames.ELT_STRING);
		String valueStr = stringElt.getText();
		
		Element valueElt = element.getChild(XmlNames.ELT_VALUE);
		String value = null;
		if(valueElt!=null)
			value = valueElt.getText();
		
		EntityLocation result =  new EntityLocation(startPos, endPos, source, valueStr, value);
		return result;
	}
}
