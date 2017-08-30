package fr.univavignon.nerwip.data.entity;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.time.TimeFormatting;
import fr.univavignon.nerwip.tools.xml.XmlNames;
import fr.univavignon.nerwip.tools.xml.XmlTools;

/**
 * Class representing a list of entities,
 * with some meta-data related to how
 * they were obtained.
 * 
 * @author Vincent Labatut
 */
public class Entities
{	
	/**
	 * Builds an Entities object with current
	 * date and the reference resolver (and no
	 * linker).
	 */
	public Entities()
	{	initDates();
		resolver = ProcessorName.REFERENCE;
		linker = null;
	}
	
	/**
	 * Builds a Entities object with current
	 * date and specified resolver.
	 * 
	 * @param resolver
	 * 		Resolver used on these entities.
	 */
	public Entities(ProcessorName resolver)
	{	initDates();
		this.resolver = resolver;
		linker = null;
	}
	
	/**
	 * Builds a Entities object with current
	 * date and specified resolver and linker.
	 * 
	 * @param resolver
	 * 		Resolver used on these entities.
	 * @param linker
	 * 		Linker used on these entities.
	 */
	public Entities(ProcessorName resolver, ProcessorName linker)
	{	initDates();
		this.resolver = resolver;
		this.linker = linker;
	}
	
	/**
	 * Builds a Entities object with specified
	 * dates and linker.
	 * 
	 * @param resolver
	 * 		Resolver used on these entities.
	 * @param linker
	 * 		Linker used on these entities.
	 * @param creationDate
	 * 		Date the entities were processed.
	 * @param modificationDate
	 * 		Date the entities were last processed.
	 */
	private Entities(ProcessorName resolver, ProcessorName linker, Date creationDate, Date modificationDate)
	{	this.creationDate = creationDate;
		this.modificationDate = modificationDate;
		this.resolver = resolver;
		this.linker = linker;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Date the entities were processed */
	private Date creationDate = null;
	/** Date the entities were last modified (mainly for manual annotation files) */
	private Date modificationDate = null;
	
	/**
	 * Sets the processing date
	 * to the current time.
	 */
	private void initDates()
	{	Calendar cal = Calendar.getInstance();
		creationDate = cal.getTime();
		modificationDate = cal.getTime();
	}
	
	/**
	 * Returns the date these entities
	 * were processed.
	 * 
	 * @return
	 * 		Date these entities were detected.
	 */
	public Date getCreationDate()
	{	return creationDate;
	}
	
	/**
	 * Returns the date these entities
	 * were last modified.
	 * 
	 * @return
	 * 		Date these entities were modified.
	 */
	public Date getModificationDate()
	{	return modificationDate;
	}
	
	/**
	 * Changes the date these entities
	 * were originally processed.
	 * 
	 * @param creationDate
	 * 		Date of the detection.
	 */
	public void setCreationDate(Date creationDate)
	{	this.creationDate = creationDate;
	}
	
	/**
	 * Changes the date these entities
	 * were last modified.
	 * 
	 * @param modificationDate
	 * 		Date of the modification.
	 */
	public void setModificationDate(Date modificationDate)
	{	this.modificationDate = modificationDate;
	}
	
	/////////////////////////////////////////////////////////////////
	// RESOLVER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Resolver used to process these entities (or {@link ProcessorName#REFERENCE} for manual annotations) */
	private ProcessorName resolver = null;
	
	/**
	 * Returns the resolver applied to
	 * these entities.
	 * 
	 * @return
	 * 		Name of the resolver used to process these entities.
	 */
	public ProcessorName getResolver()
	{	return resolver;
	}
	
	/**
	 * Changes the resolver used to process these entities.
	 * 
	 * @param resolver
	 * 		New name of the resolver used to process these entities.
	 */
	public void setResolver(ProcessorName resolver)
	{	this.resolver = resolver;
	}
	
	/////////////////////////////////////////////////////////////////
	// LINKER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Linker used to process these entities (or {@link ProcessorName#REFERENCE} for manual annotations) */
	private ProcessorName linker = null;
	
	/**
	 * Returns the linker which detected
	 * these entities.
	 * 
	 * @return
	 * 		Name of the linker used to process these entities.
	 */
	public ProcessorName getLinker()
	{	return linker;
	}
	
	/**
	 * Changes the linker used to process these entities.
	 * 
	 * @param linker
	 * 		New name of the linker used to process these entities.
	 */
	public void setLinker(ProcessorName linker)
	{	this.linker = linker;
	}
	
	/////////////////////////////////////////////////////////////////
	// EDITOR			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Human person that originally annotated these entities (only relevant if the recognizer name is {@link ProcessorName#REFERENCE}) */
	private String editor = null;
	
	/**
	 * Returns the name of the person which
	 * originally annotated these entities.
	 * This is relevant only if the linker
	 * name is {@link ProcessorName#REFERENCE},
	 * otherwise the method returns {@code null}.
	 * 
	 * @return
	 * 		Name of the editor's name, or {@code null}
	 * 		if this is not a reference, of if this nale
	 * 		was not previously set.
	 */
	public String getEditor()
	{	return editor;
	}
	
	/**
	 * Changes the name of the person which originally 
	 * annotated these entities.
	 * 
	 * @param editor
	 * 		New editor name.
	 */
	public void setEditor(String editor)
	{	this.editor = editor;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITIES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Counter used to number entities */
	private long nextInternalId = 0;
	/** Set of entities */
	private final Set<AbstractEntity> entities = new TreeSet<AbstractEntity>();
	/** Entities by internal id */
	private final Map<Long,AbstractEntity> entitiesById = new HashMap<Long,AbstractEntity>();
	/** Named entities by external id */
	private final Map<String,Map<String,Map<EntityType,AbstractNamedEntity>>> namedEntitiesByExternalId = new HashMap<String,Map<String,Map<EntityType,AbstractNamedEntity>>>();
	/** Named entities by name */
	private final Map<String,List<AbstractNamedEntity>> namedEntitiesByName = new HashMap<String,List<AbstractNamedEntity>>();
	/** Valued entities by value */
	private final Map<Comparable<?>,AbstractValuedEntity<?>> valuedEntitiesByValue = new HashMap<Comparable<?>,AbstractValuedEntity<?>>();
	
	/**
	 * Returns the whole set
	 * of processed entities.
	 * 
	 * @return
	 * 		Set of Entity objects.
	 */
	public Set<AbstractEntity> getEntities()
	{	return entities;
	}
	
	/**
	 * Returns the set of all entities of a certain types. Note that
	 * the returned set is a collection of generic entities, not of
	 * the specifically typed entities.
	 * 
	 * @param type
	 * 		Desired type. 
	 * @return
	 * 		All the entities possessing the specified type.
	 */
	public Set<AbstractEntity> getEntitiesByType(EntityType type)
	{	Set<AbstractEntity> result = new TreeSet<AbstractEntity>();
		
		for(AbstractEntity entity: entities)
		{	EntityType eType = entity.getType();
			if(eType==type)
				result.add(entity);
		}
		
		return result;
	}
	
	/**
	 * Returns the entity with the specified internal id.
	 * 
	 * @param id
	 * 		Unique internal id of the entity.
	 * @return
	 * 		Entity possessing the specified internal id, or {@code null]} if none does.
	 */
	public AbstractEntity getEntityById(long id)
	{	AbstractEntity result = entitiesById.get(id);
		return result;
	}
	
	/**
	 * Returns the map of named entities with the specified external id.
	 * Indeed, the same external id can be shared by several distinct instance
	 * of the entity, possessing each a different type.
	 * 
	 * @param id
	 * 		Unique external id of the named entity.
	 * @param knowledgeBase
	 * 		Knowledge base which delivered the external id.
	 * @return
	 * 		Named entities possessing the specified external id (can be empty).
	 */
	public Map<EntityType,AbstractNamedEntity> getNamedEntityByExternalId(String id, String knowledgeBase)
	{	Map<EntityType,AbstractNamedEntity> result = new HashMap<EntityType, AbstractNamedEntity>();
		Map<String,Map<EntityType,AbstractNamedEntity>> map = namedEntitiesByExternalId.get(knowledgeBase);
		if(map!=null)
			result = map.get(id);
		return result;
	}

	/**
	 * Returns the named entity with the specified external id.
	 * 
	 * @param id
	 * 		Unique external id of the named entity.
	 * @param knowledgeBase
	 * 		Knowledge base which delivered the external id.
	 * @param type
	 * 		Type of the desired entity.
	 * @return
	 * 		Named entity possessing the specified external id, or {@code null]} if none does.
	 */
	public AbstractNamedEntity getNamedEntityByExternalId(String id, String knowledgeBase, EntityType type)
	{	AbstractNamedEntity result = null;
		Map<String,Map<EntityType,AbstractNamedEntity>> map = namedEntitiesByExternalId.get(knowledgeBase);
		if(map!=null)
		{	Map<EntityType,AbstractNamedEntity> map2 = map.get(id);
			if(map2!=null)
				result = map2.get(type);
		}
		return result;
	}

	/**
	 * Returns the first named entity found with at least one 
	 * of the specified external id.
	 * 
	 * @param ids
	 * 		Map associating knowledge bases and external ids.
	 * @param type
	 * 		Type of the desired entity.
	 * @return
	 * 		Named entity possessing at least one of the specified external id, 
	 * 		or {@code null]} if none does.
	 */
	public AbstractNamedEntity getNamedEntityByExternalIds(Map<String,String> ids, EntityType type)
	{	AbstractNamedEntity result = null;
		
		Iterator<Entry<String,String>> it = ids.entrySet().iterator();
		while(result==null && it.hasNext())
		{	Entry<String,String> entry = it.next();
			String kb = entry.getKey();
			String id = entry.getValue();
			Map<String,Map<EntityType,AbstractNamedEntity>> map = namedEntitiesByExternalId.get(kb);
			if(map!=null)
			{	Map<EntityType,AbstractNamedEntity> map2 = map.get(id);
				if(map2!=null)
					result = map2.get(type);
			}
		}
		
		return result;
	}

	/**
	 * Returns a list of named entities with the specified name.
	 *  
	 * @param name
	 * 		Name of the targeted entity, which may not be unique.
	 * @return
	 * 		A list (possibly empty) of named entities with the specified name.
	 */
	public List<AbstractNamedEntity> getNamedEntitiesByName(String name)
	{	List<AbstractNamedEntity> result = namedEntitiesByName.get(name);
		if(result==null)
			result = new ArrayList<AbstractNamedEntity>();
		else
			result = new ArrayList<AbstractNamedEntity>(result);
		return result;
	}

	/**
	 * Returns a list of named entities with the specified name and type.
	 *  
	 * @param name
	 * 		Name of the targeted entity, which may not be unique.
	 * @param type
	 * 		Type of the targeted entity.
	 * @return
	 * 		A list (possibly empty) of named entities with the specified name
	 * 		and type.
	 */
	public List<AbstractNamedEntity> getNamedEntitiesByNameType(String name, EntityType type)
	{	List<AbstractNamedEntity> result = getNamedEntitiesByName(name);
		Iterator<AbstractNamedEntity> it = result.iterator();
		while(it.hasNext())
		{	AbstractNamedEntity entity = it.next();
			EntityType entType = entity.getType();
			if(!entType.equals(type))
				it.remove();
		}
		return result;
	}

	/**
	 * Returns the valued entity with the specified value.
	 * 
	 * @param value
	 * 		Value of the entity.
	 * @return
	 * 		Valued entity possessing the specified value, or {@code null]} if none does.
	 */
	public AbstractValuedEntity<?> getValuedEntityByValue(Comparable<?> value)
	{	AbstractValuedEntity<?> result = valuedEntitiesByValue.get(value);
		return result;
	}
	
	/**
	 * Adds a new entity to the list.
	 * 
	 * @param entity
	 * 		Entity to add to the list.
	 */
	public void addEntity(AbstractEntity entity)
	{	if(entity==null)
			throw new NullPointerException("Trying to add a null entity to this Entities object");
	
		// possibly update the entity id
		if(entity.internalId<0)
		{	entity.internalId = nextInternalId;
			nextInternalId++;
		}
		else
		{	if(entitiesById.containsKey(entity.internalId))
				throw new IllegalArgumentException("This Entities object already contains an entity with the same internal id.");
			else
				nextInternalId = Math.max(nextInternalId, entity.internalId+1);
		}
		
		// add the entity
		entities.add(entity);
		entitiesById.put(entity.internalId,entity);
		// add the named entity
		if(entity instanceof AbstractNamedEntity)
		{	// map by id
			AbstractNamedEntity namedEntity = (AbstractNamedEntity)entity;
			EntityType type = namedEntity.getType();
			Set<Entry<String, String>> entrySet = namedEntity.getExternalIds().entrySet();
			for(Entry<String, String> entry: entrySet)
			{	String kb = entry.getKey();
				String id = entry.getValue();
				Map<String, Map<EntityType,AbstractNamedEntity>> map = namedEntitiesByExternalId.get(kb);
				if(map==null)
				{	map = new HashMap<String, Map<EntityType,AbstractNamedEntity>>();
					namedEntitiesByExternalId.put(kb,map);
				}
				Map<EntityType,AbstractNamedEntity> map2 = map.get(id);
				if(map2==null)
				{	map2 = new HashMap<EntityType,AbstractNamedEntity>();
					map.put(id,map2);
				}
				if(map2.containsKey(type))
					throw new IllegalArgumentException("Trying to add a named entity possessing the same external id ("+kb+":"+id+") than an already existing one.");
				else
					map2.put(type,namedEntity);
			}
			// map by name
			Set<String> names = namedEntity.getSurfaceForms();
			for(String name: names)
			{	List<AbstractNamedEntity> list = namedEntitiesByName.get(name);
				if(list==null)
				{	list = new ArrayList<AbstractNamedEntity>();
					namedEntitiesByName.put(name,list);
				}
				list.add(namedEntity);
			}
		}
		// add the valued entity
		else if(entity instanceof AbstractValuedEntity)
		{	AbstractValuedEntity<?> valuedEntity = (AbstractValuedEntity<?>)entity;
			Comparable<?> value = valuedEntity.getValue();
			if(valuedEntitiesByValue.containsKey(value))
				throw new IllegalArgumentException("Trying to add a valued entity possessing the same value ("+value+") than an already existing one.");
			else
				valuedEntitiesByValue.put(value,valuedEntity);
		}
	}
	
	/**
	 * Remove an entity from this object.
	 * 
	 * @param entity
	 * 		Entity to remove.
	 */
	public void removeEntity(AbstractEntity entity)
	{	entities.remove(entity);
		entitiesById.remove(entity.internalId);
		
		// remove the named entity
		if(entity instanceof AbstractNamedEntity)
		{	// map by id
			AbstractNamedEntity namedEntity = (AbstractNamedEntity)entity;
			EntityType type = namedEntity.getType();
			Set<Entry<String, String>> entrySet = namedEntity.getExternalIds().entrySet();
			for(Entry<String, String> entry: entrySet)
			{	String kb = entry.getKey();
				String id = entry.getValue();
				Map<String, Map<EntityType,AbstractNamedEntity>> map = namedEntitiesByExternalId.get(kb);
				if(map!=null)
				{	Map<EntityType,AbstractNamedEntity> map2 = map.get(id);
					if(map2!=null)
						map2.remove(type);
				}
			}
			// map by name
			Set<String> names = namedEntity.getSurfaceForms();
			for(String name: names)
			{	List<AbstractNamedEntity> list = namedEntitiesByName.get(name);
				if(list!=null)
					list.remove(namedEntity);
			}
		}
		
		// remove the valued entity
		else if(entity instanceof AbstractValuedEntity)
		{	AbstractValuedEntity<?> valuedEntity = (AbstractValuedEntity<?>)entity;
			Comparable<?> value = valuedEntity.getValue();
			valuedEntitiesByValue.remove(value);
		}
	}
	
	/**
	 * Adds the new entities to this existing collection, merging
	 * the new ones with the existing ones when they are similar,
	 * and updating the concerned mentions when merge occurs.
	 * 
	 * @param newEntities
	 * 		New entities to insert in the existing collection.
	 * @param mentions
	 * 		Mentions referring to the new entities, to be updated.
	 */
	public void unifyEntities(Entities newEntities, Mentions mentions)
	{	// init entity conversion map (new > old)
		Map<AbstractNamedEntity,AbstractNamedEntity> map = new HashMap<AbstractNamedEntity,AbstractNamedEntity>();
		for(AbstractEntity newEntity: newEntities.getEntities())
		{	// only process named entities (ignore dates)
			if(newEntity instanceof AbstractNamedEntity)
			{	// get the new entity ids
				AbstractNamedEntity namedEntity = (AbstractNamedEntity)newEntity;
				EntityType type = namedEntity.getType();
				Map<String,String> exIds = namedEntity.getExternalIds();
				// look for an existing entity with similar ids
				AbstractNamedEntity oldEntity = getNamedEntityByExternalIds(exIds,type);
				// can be used later for substitution (oldEntry possibly null, here)
				if(oldEntity!=null)
				{	// possibly complete the old entity with the new one
					oldEntity.completeWith(namedEntity);
					// add to the conversion map
					map.put(namedEntity, oldEntity);
				}
				// otherwise, if nothing found, add to existing collection
				else
				{	// this allows reseting the internal id to a value consistent with the existing collection
					newEntity.setInternalId(-1);
					// insert in the new collection
					addEntity(newEntity);
				}
			}
		}
		
		// use the map to update the mentions with the substitution entities
		for(AbstractMention<?> mention: mentions.getMentions())
		{	AbstractEntity entity = mention.getEntity();
			// only focus on the named entities
			if(entity instanceof AbstractNamedEntity)
			{	AbstractNamedEntity namedEntity = (AbstractNamedEntity)entity;
				AbstractNamedEntity oldEntity = map.get(namedEntity);
				if(oldEntity!=null)
					mention.setEntity(oldEntity);
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE ACCESS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Reads the specified XML file, and 
	 * builds the corresponding Entities object,
	 * which contains both entities and meta-data.
	 * 
	 * @param dataFile
	 * 		The XML file to be read.
	 * @return
	 * 		The list of entities and meta-data stored in the file.
	 * 
	 * @throws SAXException
	 * 		Problem while reading the file.
	 * @throws IOException
	 * 		Problem while reading the file.
	 * @throws ParseException 
	 * 		Problem while parsing the date.
	 */
	public static Entities readFromXml(File dataFile) throws SAXException, IOException, ParseException
	{	// schema file
		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_ENTITY_SCHEMA;
		File schemaFile = new File(schemaPath);

		// load file
		Element element = XmlTools.getRootFromFile(dataFile,schemaFile);
		
		// get resolver
		String resolverStr = element.getAttributeValue(XmlNames.ATT_RESOLVER);
		ProcessorName resolver = ProcessorName.valueOf(resolverStr);
		// possibly get linker
		ProcessorName linker = null; 
		String linkerStr = element.getAttributeValue(XmlNames.ATT_LINKER);
		if(linkerStr!=null)
			linker = ProcessorName.valueOf(linkerStr);
		
		// get dates
		String creationDateStr = element.getAttributeValue(XmlNames.ATT_CREATION);
		Date creationDate = TimeFormatting.parseXmlTime(creationDateStr);
		String modificationDateStr = element.getAttributeValue(XmlNames.ATT_MODIFICATION);
		Date modificationDate = TimeFormatting.parseXmlTime(modificationDateStr);
		
		// get editor
		String editor = element.getAttributeValue(XmlNames.ATT_EDITOR);
		
		// get entities
		Entities result = new Entities(resolver, linker, creationDate, modificationDate);
		result.setEditor(editor);
		List<Element> elements = element.getChildren(XmlNames.ELT_ENTITY);
		for(Element e: elements)
		{	AbstractEntity entity = AbstractEntity.importFromElement(e);
			result.addEntity(entity);
		}
//		Collections.sort(result.entities);

		return result;
	}

	/**
	 * Write this Entities object under the form of
	 * a XML file using our own format.
	 * 
	 * @param dataFile
	 * 		File to contain the entities.
	 * 
	 * @throws IOException
	 * 		Problem while writing the file.
	 */
	public void writeToXml(File dataFile) throws IOException
	{	// schema file
		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_ENTITY_SCHEMA;
		File schemaFile = new File(schemaPath);
		
		// build xml document
		Element element = new Element(XmlNames.ELT_ENTITIES);
		
		// insert resolver attribute
		Attribute resolverAttr = new Attribute(XmlNames.ATT_RESOLVER, resolver.toString());
		element.setAttribute(resolverAttr);
		// possibly insert linker attribute
		if(linker!=null)
		{	Attribute linkerAttr = new Attribute(XmlNames.ATT_LINKER, linker.toString());
			element.setAttribute(linkerAttr);
		}
		
		// insert date attributes
		String creationDateStr = TimeFormatting.formatXmlTime(creationDate);
		Attribute creationDateAttr = new Attribute(XmlNames.ATT_CREATION, creationDateStr);
		element.setAttribute(creationDateAttr);
		String modificationDateStr = TimeFormatting.formatXmlTime(modificationDate);
		Attribute modificationDateAttr = new Attribute(XmlNames.ATT_MODIFICATION, modificationDateStr);
		element.setAttribute(modificationDateAttr);
		
		// insert editor attribute
		if(editor!=null)
		{	Attribute editorAttr = new Attribute(XmlNames.ATT_EDITOR, editor);
			element.setAttribute(editorAttr);
		}
		
		// insert entity elements
//		Collections.sort(entities);
		for(AbstractEntity entity: entities)
		{	Element entityElt = entity.exportAsElement();
			element.addContent(entityElt);
		}
		
		// record file
		XmlTools.makeFileFromRoot(dataFile,schemaFile,element);
	}
}
