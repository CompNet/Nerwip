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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Attribute;
import org.jdom2.Element;

import fr.univavignon.nerwip.tools.xml.XmlNames;

/**
 * Abstract class representing a named entity, i.e. an entity
 * that can appears under different names.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractNamedEntity extends AbstractEntity
{	
	/**
	 * General constructor for a named entity.
	 * 
	 * @param name
	 * 		Main string representation of the entity to create.
	 * @param internalId
	 * 		Internal id of the entity to create.
	 */
	public AbstractNamedEntity(String name, long internalId)
	{	super(internalId);
		
		this.name = name;
		surfaceForms.add(name);
	}
	
	/**
	 * Builds a named entity of the specified type, using the specified
	 * name and id.
	 * 
	 * @param internalId
	 * 		Id of the entity to build ({@code -1} to automatically define it 
	 * 		when inserting in an {@link Entities} object).
	 * @param name
	 * 		Name of the entity to build.
	 * @param type
	 * 		Entity type of the entity to build.
	 * @return
	 * 		The built entity.
	 */
	public static AbstractNamedEntity buildEntity(long internalId, String name, EntityType type)
	{	AbstractNamedEntity result = null;
		switch(type)
		{	case FUNCTION:
				result = new EntityFunction(name,internalId);
				break;
			case LOCATION:
				result = new EntityLocation(name,internalId);
				break;
			case MEETING:
				result = new EntityMeeting(name,internalId);
				break;
			case ORGANIZATION:
				result = new EntityOrganization(name,internalId);
				break;
			case PERSON:
				result = new EntityPerson(name,internalId);
				break;
			case PRODUCTION:
				result = new EntityProduction(name,internalId);
				break;
		}
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// MAIN NAME		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Main string representation of this entity */
	protected String name = null;
	
	/**
	 * Returns the main string representation of this entity.
	 * 
	 * @return
	 * 		Original string representation of this entity. 
	 */
	public String getName()
	{	return name;
	}

	/**
	 * Changes the main string representation of this entity.
	 * 
	 * @param name
	 * 		Change the main surface form of this entity.
	 */
	public void setName(String name)
	{	this.name = name;
	}
	
	/////////////////////////////////////////////////////////////////
	// SURFACE FORMS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** All strings representing this entity */
	protected Set<String> surfaceForms = new TreeSet<String>();
	
	/**
	 * Returns all the strings representing this entity.
	 * 
	 * @return
	 * 		Set of strings representing this entity. 
	 */
	public Set<String> getSurfaceForms()
	{	return surfaceForms;
	}

	/**
	 * Add one name to this entity.
	 * 
	 * @param surfaceForm
	 * 		New name for this entity.
	 */
	public void addSurfaceForm(String surfaceForm)
	{	surfaceForms.add(surfaceForm);
	}
	
	/**
	 * Removes one of the names of this entity.
	 * 
	 * @param surfaceForm
	 * 		The name to remove.
	 */
	public void removeSurfaceForm(String surfaceForm)
	{	surfaceForms.remove(surfaceForm);
	}

	/////////////////////////////////////////////////////////////////
	// EXTERNAL IDS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Internal ids of this entity */
	protected Map<KnowledgeBase,String> externalIds = new HashMap<KnowledgeBase,String>();
	
	/**
	 * Returns the requested external id of this entity, or {@code null}
	 * if none was defined.
	 * 
	 * @param knowledgeBase 
	 * 		Name of the concerned knowledge base.
	 * @return
	 * 		External id of this entity in the knowledge base. 
	 */
	public String getExternalId(KnowledgeBase knowledgeBase)
	{	return externalIds.get(knowledgeBase);
	}
	
	/**
	 * Returns the maps of external ids of this entity.
	 * 
	 * @return
	 * 		External ids of this entity in the knowledge bases. 
	 */
	public Map<KnowledgeBase,String> getExternalIds()
	{	return externalIds;
	}
	
	/**
	 * Changes the internal id of this entity, for
	 * the specified knowledge base.
	 * 
	 * @param knowledgeBase 
	 * 		Name of the knowledge base.
	 * @param externalId
	 * 		External id of this entity in the knowledge base.
	 */
	public void setExternalId(KnowledgeBase knowledgeBase, String externalId)
	{	externalIds.put(knowledgeBase,externalId);
	}
	
	/////////////////////////////////////////////////////////////////
	// XML				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Element exportAsElement()
	{	Element result = new Element(XmlNames.ELT_ENTITY);
		
		// set type
		{	Attribute typeAttr = new Attribute(XmlNames.ATT_TYPE, getType().toString());
			result.setAttribute(typeAttr);
		}
		// set internal id
		{	Attribute internalIdAttr = new Attribute(XmlNames.ATT_ID, Long.toString(internalId));
			result.setAttribute(internalIdAttr);
		}
		// set main name
		{	Attribute nameAttr = new Attribute(XmlNames.ATT_NAME, name);
			result.setAttribute(nameAttr);
		}
		// set surface forms
		{	Element surfaceFormsElt = new Element(XmlNames.ELT_SURFACE_FORMS);
			for(String surfaceForm: surfaceForms)
			{	Element surfaceFormElt = new Element(XmlNames.ELT_SURFACE_FORM);
				surfaceFormElt.setText(surfaceForm);
				surfaceFormsElt.addContent(surfaceFormElt);
			}
		}
		// external ids
		{	Element externalIdsElt = new Element(XmlNames.ELT_EXTERNAL_IDS);
			for(Entry<KnowledgeBase,String> entry: externalIds.entrySet())
			{	// retrieve the data
				KnowledgeBase kb = entry.getKey();
				String externalId = entry.getValue();
				// set up id element
				Element externalIdElt = new Element(XmlNames.ELT_EXTERNAL_ID);
				externalIdElt.setText(externalId);
				// set up knowledge base attribute
				Attribute kbAttr = new Attribute(XmlNames.ATT_KNOWLEDGE_BASE, kb.toString());
				externalIdElt.setAttribute(kbAttr);
				// add to result
				externalIdsElt.addContent(externalIdElt);
			}
		}
		return result;
	}

	/**
	 * Builds a function entity from the specified
	 * XML element.
	 * 
	 * @param element
	 * 		XML element representing the entity.
	 * @param type
	 * 		Type of the entity to extract from the element.
	 * @return
	 * 		The entity built from the element.
	 */
	public static AbstractNamedEntity importFromElement(Element element, EntityType type)
	{	// get the id
		String internalIdStr = element.getAttributeValue(XmlNames.ATT_ID);
		long internalId = Long.parseLong(internalIdStr);
		
		// get the name
		String name = element.getAttributeValue(XmlNames.ATT_NAME);
		
		// build the entity
		EntityLocation result =  new EntityLocation(name,internalId);
		AbstractNamedEntity.buildEntity(internalId, name, type);

		// get the surface forms
		{	Element surfaceFormsElt = element.getChild(XmlNames.ELT_SURFACE_FORMS);
			List<Element> surfaceFormList = surfaceFormsElt.getChildren();
			for(Element surfaceFormElt: surfaceFormList)
			{	String surfaceForm = surfaceFormElt.getText();
				result.addSurfaceForm(surfaceForm);
			}
		}
		// get the external ids
		{	Element externalIdsElt = element.getChild(XmlNames.ELT_EXTERNAL_IDS);
			List<Element> externalIdsList = externalIdsElt.getChildren();
			for(Element externalIdElt: externalIdsList)
			{	// external id
				String externalId = externalIdElt.getText();
				// knowledge base name
				String kbStr = element.getAttributeValue(XmlNames.ATT_KNOWLEDGE_BASE);
				KnowledgeBase kb = KnowledgeBase.valueOf(kbStr);
				// add to result
				result.setExternalId(kb,externalId);
			}
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// OBJECT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = getType().toString()+"(";
		result = result + "ID=" + internalId + "";
		result = result + ", NAME=\"" + name +"\"";
		if(!externalIds.isEmpty())
		{	Entry<KnowledgeBase,String> entry = externalIds.entrySet().iterator().next();
			KnowledgeBase kb = entry.getKey();
			String id = entry.getValue();
			result = result + ", " + kb.toString() + "=\"" + id +"\"";
		}
		result = result + ")";
		return result;
	}
}
