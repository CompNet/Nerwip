package fr.univavignon.nerwip.data.entity;

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

import fr.univavignon.nerwip.tools.time.Date;
import fr.univavignon.nerwip.tools.xml.XmlNames;

/**
 * Class representing a date entity, which is a kind of valued entity.
 * 
 * @author Vincent Labatut
 */
public class EntityDate extends AbstractValuedEntity<Date>
{	
	/**
	 * Constructs a date entity.
	 * 
	 * @param value
	 * 		Date of the entity to create.
	 * @param internalId
	 * 		Internal id of the entity to create.
	 */
	public EntityDate(Date value, long internalId)
	{	super(value,internalId);
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
	{	Element result = new Element(XmlNames.ELT_ENTITY);
		
		// set type
		Attribute typeAttr = new Attribute(XmlNames.ATT_TYPE, getType().toString());
		result.setAttribute(typeAttr);
		
		// set internal id
		Attribute internalIdAttr = new Attribute(XmlNames.ATT_ID, Long.toString(internalId));
		result.setAttribute(internalIdAttr);

		// set value
		Element valueElt = new Element(XmlNames.ELT_VALUE);
		valueElt.setText(value.exportToString());
		result.addContent(valueElt);
		
		return result;
	}
	
	/**
	 * Builds a date mention from the specified
	 * XML element.
	 * 
	 * @param element
	 * 		XML element representing the mention.
	 * @return
	 * 		The date entity corresponding to the specified element.
	 */
	public static EntityDate importFromElement(Element element)
	{	// get the date
		Element valueElt = element.getChild(XmlNames.ELT_VALUE);
		String valueStr = valueElt.getText();
		Date value = Date.importFromString(valueStr);
		
		// get the id
		String internalIdStr = element.getAttributeValue(XmlNames.ATT_ID);
		long internalId = Long.parseLong(internalIdStr);
		
		// build the entity
		EntityDate result =  new EntityDate(value,internalId);
		return result;
	}
}
