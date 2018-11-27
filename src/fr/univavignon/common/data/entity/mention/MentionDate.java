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

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.AbstractEntity;
import fr.univavignon.common.data.entity.Entities;
import fr.univavignon.common.data.entity.EntityDate;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.tools.time.Date;
import fr.univavignon.common.tools.time.DateParser;
import fr.univavignon.common.tools.time.Period;
import fr.univavignon.nerwip.processing.ProcessorName;

/**
 * This class represents a date mention.
 * 
 * @author Burcu Küpelioğlu
 * @author Vincent Labatut
 */
public class MentionDate extends AbstractMention<Period>
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
	{	super(startPos, endPos, source, valueStr, new Period(value));
	}
	
	/**
	 * Builds a new date mention from a period value.
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
	public MentionDate(int startPos, int endPos, ProcessorName source, String valueStr, Period value)
	{	super(startPos, endPos, source, valueStr, value);
	}
	
	/**
	 * Builds a new date mention without any date or period value.
	 * 
	 * @param startPos
	 * 		Starting position in the text.
	 * @param endPos
	 * 		Ending position in the text.
	 * @param source
	 * 		Tool which detected this mention.
	 * @param valueStr
	 * 		String representation in the text.
	 * @param language 
	 * 		Language of the text: required to parse it and retrieve the value.
	 */
	public MentionDate(int startPos, int endPos, ProcessorName source, String valueStr, ArticleLanguage language)
	{	super(startPos, endPos, source, valueStr, DateParser.parseDate(valueStr, language));
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
			valueElt.setText(value.exportToString());
			result.addContent(valueElt);
		}
		
		return result;
	}
	
	/**
	 * Builds a date mention from the specified
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
	 * 		The date mention corresponding to the specified element.
	 */
	public static MentionDate importFromElement(Element element, ProcessorName source, Entities entities)
	{	String startStr = element.getAttributeValue(ATT_START);
		int startPos = Integer.parseInt(startStr);
		
		String endStr = element.getAttributeValue(ATT_END);
		int endPos = Integer.parseInt(endStr);
		
		Element stringElt = element.getChild(ELT_STRING);
		String valueStr = stringElt.getText();
		
		Element valueElt = element.getChild(ELT_VALUE);
		Period value = null;
		if(valueElt!=null)
		{	String valueString = valueElt.getText();
			value = Period.importFromString(valueString);
		}
		
		MentionDate result =  new MentionDate(startPos, endPos, source, valueStr, value);
		
		if(entities!=null)
		{	String entityIdStr = element.getAttributeValue(ATT_ENTITY_ID);
			long entityId = Long.parseLong(entityIdStr);
			AbstractEntity entity = entities.getEntityById(entityId);
			if(entity==null)
//				entity = new EntityDate(value,entityId);
				throw new IllegalArgumentException("Did not find the entity (id: "+entityId+") refered to in mention "+result);
			if(entity instanceof EntityDate)
			{	EntityDate entityDate = (EntityDate)entity;
				result.setEntity(entityDate);
			}
			else
				throw new IllegalArgumentException("Trying to associate an entity of type "+entity.getType()+" to a mention of type "+result.getType());
		}
		
		return result;
	}
}
