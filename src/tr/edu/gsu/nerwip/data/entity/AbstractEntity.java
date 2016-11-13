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

/**
 * Abstract class representing a general entity. An entity
 * can be either named or valued, i.e. associated to one or
 * several string names, which are relatively unique, or to
 * some quantity, amount, date, or other value.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractEntity implements Comparable<AbstractEntity>
{	
	/**
	 * General constructor for an entity.
	 * 
	 * @param internalId
	 * 		Ending position in the text.
	 */
	public AbstractEntity(long internalId)
	{	this.internalId = internalId;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY ID		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Counter used to number entities */
	protected static long nextInternalId = 0;
	/** Internal id of this entity */
	protected long internalId = -1;
	
	/**
	 * Returns the internal id of this entity.
	 * 
	 * @return
	 * 		Internal id of this entity. 
	 */
	public long getInternalId()
	{	return internalId;
	}
	
	/**
	 * Changes the internal id of this entity.
	 * 
	 * @param internalId
	 * 		Internal id of this entity.
	 */
	public void setInternalId(long internalId)
	{	this.internalId = internalId;
	}
	
//	/////////////////////////////////////////////////////////////////
//	// XML				/////////////////////////////////////////////
//	/////////////////////////////////////////////////////////////////
//	/**
//	 * Returns a representation of this entity
//	 * as an XML element.
//	 * 
//	 * @return
//	 * 		An XML element representing this entity.
//	 */
//	public abstract Element exportAsElement();
//	
//	/**
//	 * Builds an entity from the specified
//	 * XML element.
//	 * 
//	 * @param element
//	 * 		XML element representing the entity.
//	 * @param source
//	 * 		Name of the NER tool which detected the entity.
//	 * @return
//	 * 		The entity corresponding to the specified element.
//	 */
//	public static AbstractEntity<?> importFromElement(Element element, RecognizerName source)
//	{	AbstractEntity<?> result = null;
//		
//		String typeStr = element.getAttributeValue(XmlNames.ATT_TYPE);
//		EntityType type = EntityType.valueOf(typeStr);
//		switch(type)
//		{	case DATE:
//				result = EntityDate.importFromElement(element,source);
//				break;
//			case FUNCTION:
//				result = EntityFunction.importFromElement(element,source);
//				break;
//			case LOCATION:
//				result = EntityLocation.importFromElement(element,source);
//				break;
//			case MEETING:
//				result = EntityMeeting.importFromElement(element,source);
//				break;
//			case ORGANIZATION:
//				result = EntityOrganization.importFromElement(element,source);
//				break;
//			case PERSON:
//				result = EntityPerson.importFromElement(element,source);
//				break;
//			case PRODUCTION:
//				result = EntityProduction.importFromElement(element,source);
//				break;
//		}
//		
//		return result;
//	}

	/////////////////////////////////////////////////////////////////
	// COMPARABLE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(AbstractEntity entity)
	{	long temp = internalId - entity.internalId;
		int result = (int)Math.signum(temp);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// OBJECT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(Object obj)
	{	boolean result = false;
		if(obj!=null)
		{	if(obj instanceof AbstractEntity)
			{	AbstractEntity entity = (AbstractEntity)obj;
				result = compareTo(entity)==0;
			}
		}
		
		return result;
	}

	@Override
	public int hashCode()
	{	Long temp = internalId;
		int result = temp.hashCode();
		return result;
	}
}

// TODO represent the hierarchical relationships between entities?
// or just rely on the interrogation of online databases instead?
// TODO the same question araises for the entity multiple names...