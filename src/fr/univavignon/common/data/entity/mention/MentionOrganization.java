package fr.univavignon.common.data.entity.mention;

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

import org.jdom2.Attribute;
import org.jdom2.Element;

import fr.univavignon.common.data.entity.AbstractEntity;
import fr.univavignon.common.data.entity.Entities;
import fr.univavignon.common.data.entity.EntityOrganization;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.nerwip.processing.ProcessorName;

/**
 * Class representing an organization mention.
 * 
 * @author Vincent Labatut
 */
public class MentionOrganization extends AbstractMention<String>
{	
	/**
	 * Builds a new organization mention.
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
	 * 		Actual value of the mention (can be the same as {@link #valueStr}).
	 */
	public MentionOrganization(int startPos, int endPos, ProcessorName source, String valueStr, String value)
	{	super(startPos, endPos, source, valueStr, value);
	}
	
	/**
	 * Builds a new organization without a value.
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
	public MentionOrganization(int startPos, int endPos, ProcessorName source, String valueStr)
	{	super(startPos, endPos, source, valueStr, null);
	}
	
	/////////////////////////////////////////////////////////////////
	// TYPE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public EntityType getType()
	{	return EntityType.ORGANIZATION;
	}

	/////////////////////////////////////////////////////////////////
	// XML				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Element exportAsElement(Entities entities)
	{	Element result = new Element(Mentions.ELT_MENTION);
		
		Attribute startAttr = new Attribute(ATT_START, Integer.toString(startPos));
		result.setAttribute(startAttr);
		
		Attribute endAttr = new Attribute(ATT_END, Integer.toString(endPos));
		result.setAttribute(endAttr);

		Attribute typeAttr = new Attribute(ATT_TYPE, getType().toString());
		result.setAttribute(typeAttr);
		
		if(entities!=null && entity!=null)
		{	long entityId = entity.getInternalId();
			Attribute entityIdAttr = new Attribute(ATT_ENTITY_ID, Long.toString(entityId));
			result.setAttribute(entityIdAttr);
		}
		
		Element stringElt = new Element(ELT_STRING);
		stringElt.setText(valueStr);
		result.addContent(stringElt);
		
		if(value!=null)
		{	Element valueElt = new Element(ELT_VALUE);
			valueElt.setText(value);
			result.addContent(valueElt);
		}
		
		return result;
	}
	
	/**
	 * Builds an organization mention from the specified
	 * XML element, using the specified set of entities
	 * or completing it if necessary (i.e. missing entity).
	 * If {@code entities} is {@code null}, we suppose this
	 * entity does not have any associated entity, and this 
	 * method does not try to retrieve it.
	 * 
	 * @param element
	 * 		XML element representing the mention.
	 * @param source
	 * 		Name of the recognizer which detected the mention.
	 * @param entities
	 * 		Known entities as of now (can be {@code null}).
	 * @return
	 * 		The organization mention corresponding to the specified element.
	 */
	public static MentionOrganization importFromElement(Element element, ProcessorName source, Entities entities)
	{	String startStr = element.getAttributeValue(ATT_START);
		int startPos = Integer.parseInt(startStr);
		
		String endStr = element.getAttributeValue(ATT_END);
		int endPos = Integer.parseInt(endStr);
		
		Element stringElt = element.getChild(ELT_STRING);
		String valueStr = stringElt.getText();
		
		Element valueElt = element.getChild(ELT_VALUE);
		String value = null;
		if(valueElt!=null)
			value = valueElt.getText();
		
		MentionOrganization result =  new MentionOrganization(startPos, endPos, source, valueStr, value);

		if(entities!=null)
		{	String entityIdStr = element.getAttributeValue(ATT_ENTITY_ID);
			long entityId = Long.parseLong(entityIdStr);
			AbstractEntity entity = entities.getEntityById(entityId);
			if(entity==null)
//				entity = new EntityOrganization(value,entityId);
				throw new IllegalArgumentException("Did not find the entity (id: "+entityId+") refered to in mention "+result);
			if(entity instanceof EntityOrganization)
			{	EntityOrganization entityOrg = (EntityOrganization)entity;
				entityOrg.addSurfaceForm(valueStr);
				result.setEntity(entityOrg);
			}
			else
				throw new IllegalArgumentException("Trying to associate an entity of type "+entity.getType()+" to a mention of type "+result.getType());
		}
		
		return result;
	}
}
