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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.time.TimeFormatting;
import tr.edu.gsu.nerwip.tools.xml.XmlNames;
import tr.edu.gsu.nerwip.tools.xml.XmlTools;

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
	 * Builds an entities object with current
	 * date and reference source, and the specified
	 * editor name.
	 * 
	 * @param editor
	 * 		Editor's name for these annotations.
	 */
	public Entities(String editor)
	{	initDate();
		source = RecognizerName.REFERENCE;
		this.editor = editor;
	}
	
	/**
	 * Builds an entities object with current
	 * date and specified source.
	 * 
	 * @param source
	 * 		Source of the entities (a NER tool).
	 */
	public Entities(RecognizerName source)
	{	initDate();
		this.source = source;
	}
	
	/**
	 * Builds an entities object with specified
	 * date and source.
	 * 
	 * @param source
	 * 		Source of the entities (a NER tool).
	 * @param date
	 * 		Date the entities were detected.
	 */
	public Entities(RecognizerName source, Date date)
	{	this.date = date;
		this.source = source;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Date the entities were detected */
	private Date date = null;
	
	/**
	 * Sets the detection date
	 * to the current time.
	 */
	private void initDate()
	{	Calendar cal = Calendar.getInstance();
		date = cal.getTime();
	}
	
	/**
	 * Returns the date these entities
	 * were detected.
	 * 
	 * @return
	 * 		Date these entities were detected.
	 */
	public Date getDate()
	{	return date;
	}
	
	/**
	 * Changes the date these entities
	 * were detected.
	 * 
	 * @param date
	 * 		Date of the detection.
	 */
	public void setDate(Date date)
	{	this.date = date;
	}
	
	/////////////////////////////////////////////////////////////////
	// SOURCE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** NER tool which detected these entities (or {@link RecognizerName#REFERENCE} for a manual annotations) */
	private RecognizerName source = null;
	
	/**
	 * Returns the NER tool which detected
	 * these entities.
	 * 
	 * @return
	 * 		Name of the NER tool having detected these entities.
	 */
	public RecognizerName getSource()
	{	return source;
	}
	
	/**
	 * Changes the NER tool which detected
	 * these entities.
	 * 
	 * @param source
	 * 		New name of the NER tool having detected these entities.
	 */
	public void setSource(RecognizerName source)
	{	this.source = source;
	}
	
	/////////////////////////////////////////////////////////////////
	// EDITOR			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Human person that originally annotated these entities (only relevant if the recognizer name is REFERENCE) */
	private String editor = null;
	
	/**
	 * Returns the name of the person which
	 * originally annotated these entities.
	 * This is relevant only if the recognizer
	 * name is {@link RecognizerName#REFERENCE},
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
	/** List of entities */
	private final List<AbstractEntity<?>> entities = new ArrayList<AbstractEntity<?>>();
	
	/**
	 * Returns the whole list
	 * of detected entities.
	 * 
	 * @return
	 * 		List of entity objects.
	 */
	public List<AbstractEntity<?>> getEntities()
	{	return entities;
	}
	
	/**
	 * Returns the entity at the specified position (in the list of 
	 * entities, not in the text).
	 * 
	 * @param index
	 * 		Position of the required entity.
	 * @return
	 * 		Entity at the specified position.
	 */
	public AbstractEntity<?> getEntityAt(int index)
	{	AbstractEntity<?> result = entities.get(index);
		return result;
	}

	/**
	 * Returns the list of entities overlapping the specified range.
	 * The parameters are expressed in terms of characters in the original
	 * text.
	 * 
	 * @param startPos
	 * 		Position of the first character in the specified range.
	 * @param endPos
	 * 		Position of the last character+1 in the specified range.
	 * @return
	 * 		List of the concerned entities.
	 */
	public List<AbstractEntity<?>> getEntitiesIn(int startPos, int endPos)
	{	List<AbstractEntity<?>> result = new ArrayList<AbstractEntity<?>>();
		
		for(AbstractEntity<?> entity: entities)
		{	if(entity.containsPosition(startPos) || entity.containsPosition(endPos-1))
				result.add(entity);
		}
		
		return result;
	}

	/**
	 * Adds a new entity in the list.
	 * <br/>
	 * No redundance check is performed. 
	 * 
	 * @param entity
	 * 		Entity to add to the list.
	 */
	public void addEntity(AbstractEntity<?> entity)
	{	entities.add(entity);
	}
	
	/**
	 * Adds all the entities from the specifid object
	 * to this object.
	 * <br/>
	 * No redundance check is performed. 
	 * 
	 * @param entities
	 * 		Entities to add to the list.
	 */
	public void addEntities(Entities entities)
	{	List<AbstractEntity<?>> list = entities.getEntities();
		this.entities.addAll(list);
	}

	/**
	 * Adds to this object all the entities contained in 
	 * the specified list.
	 * <br/>
	 * No redundance check is performed. 
	 * 
	 * @param entities
	 * 		List of entities to add to the list.
	 */
	public void addEntities(List<AbstractEntity<?>> entities)
	{	this.entities.addAll(entities);
	}

	/**
	 * Removes the specified entity
	 * from this list.
	 * 
	 * @param entity
	 * 		Entity object to be removed from the list.
	 */
	public void removeEntity(AbstractEntity<?> entity)
	{	entities.remove(entity);
	}

	/**
	 * Returns a sublist containing only the entities of
	 * a certain type.
	 * 
	 * @param type
	 * 		The desired entity type.
	 * @return
	 * 		List of entities of this type.
	 */
	public List<AbstractEntity<?>> getEntitiesByType(EntityType type)
	{	List<AbstractEntity<?>> result = new ArrayList<AbstractEntity<?>>();
		for(AbstractEntity<?> entity: entities)
		{	EntityType t = entity.getType();
			if(t==type)
				result.add(entity);
		}
		return result;
	}
	
	/**
	 * Sorts the entities in the list
	 * depending on their position (first
	 * to last).
	 */
	public void sortByPosition()
	{	Collections.sort(entities);
	}
	
	/**
	 * Takes a list of entities of various types, and returns
	 * only those with the specified type.
	 * 
	 * @param list
	 * 		List of entities.
	 * @param type
	 * 		Entity type of interest.
	 * @return
	 * 		A sublist of the original entity list.
	 */
	public static List<AbstractEntity<?>> filterByType(List<AbstractEntity<?>> list, EntityType type)
	{	List<AbstractEntity<?>> result = new ArrayList<AbstractEntity<?>>();
		
		for(AbstractEntity<?> entity: list)
		{	EntityType t = entity.getType();
			if(t==type)
				result.add(entity);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// POSITIONS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Shifts to the right all entities located after 
	 * position {@code start}, by {@code length} characters.
	 * 
	 * @param start
	 * 		Starting position of the right shifting.
	 * @param length
	 * 		Magnitude of the shifting, in characters.
	 * @param text
	 * 		Full text of the concerned article (used
	 * 		to update the entity string values.
	 */
	public void rightShiftEntityPositions(int start, int length, String text)
	{	for(AbstractEntity<?> entity: entities)
		{	// start position
			int startPos = entity.getStartPos();
			if(start<=startPos)
				startPos = startPos + length;
			
			// end position
			int endPos = entity.getEndPos();
			if(start<endPos)
				endPos = endPos + length;
			
			// update entity
			entity.setStartPos(startPos);
			entity.setEndPos(endPos);
			String valueStr = text.substring(startPos,endPos);
			entity.setStringValue(valueStr);
		}
	}
	
	/**
	 * Shifts to the left all entities located after 
	 * position {@code start}, by {@code length} characters.
	 * 
	 * @param start
	 * 		Starting position of the left shifting.
	 * @param length
	 * 		Magnitude of the shifting, in characters.
	 * @param text
	 * 		Full text of the concerned article (used
	 * 		to update the entity string values.
	 */
	public void leftShiftEntityPositions(int start, int length, String text)
	{	Iterator<AbstractEntity<?>> it = entities.iterator();
		while(it.hasNext())
		{	AbstractEntity<?> entity = it.next();
			
			// start position
			int startPos = entity.getStartPos();
			if(start<=startPos)
				startPos = Math.max(startPos-length, start);
			
			// end position
			int endPos = entity.getEndPos();
			if(start<endPos)
				endPos = Math.max(endPos-length, start);
			
			// update entity
			if(startPos==endPos)
				it.remove();
			else
			{	entity.setStartPos(startPos);
				entity.setEndPos(endPos);
				String valueStr = text.substring(startPos,endPos);
				entity.setStringValue(valueStr);
			}
		}
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY COMPARISON	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks if the specified entity overlaps (spatially) with one
	 * of the entities present in this Entities object.
	 * 
	 * @param entity
	 * 		Entity of interest.
	 * @return
	 * 		{@code true} iff it intersects an existing entity.
	 */
	public boolean isEntityOverlapping(AbstractEntity<?> entity)
	{	boolean result = entity.overlapsWithOne(entities);
		return result;
	}
	
	/**
	 * Takes the output of several NER tools,
	 * and compare the resulting entities.
	 * The returned list contains maps of
	 * overlapping entities, i.e. entites detected
	 * by distinct NER tools, and which are considered
	 * to be the same entity.
	 * 
	 * @param entities
	 * 		Entities detected by several tools.
	 * @return
	 * 		List of maps of equivalent entities.
	 */
	public static List<Map<AbstractRecognizer,AbstractEntity<?>>> identifyOverlaps(Map<AbstractRecognizer,Entities> entities)
	{	List<Map<AbstractRecognizer,AbstractEntity<?>>> result = new ArrayList<Map<AbstractRecognizer,AbstractEntity<?>>>();
		
		// sort all entities
		for(Entities e: entities.values())
			e.sortByPosition();
		
		// init iterators
		Map<AbstractRecognizer,Iterator<AbstractEntity<?>>> iterators = new HashMap<AbstractRecognizer, Iterator<AbstractEntity<?>>>();
		for(AbstractRecognizer recognizer: entities.keySet())
		{	Entities e = entities.get(recognizer);
			Iterator<AbstractEntity<?>> it = e.getEntities().iterator();
			if(it.hasNext())
				iterators.put(recognizer,it);
		}
		
		// init current entities
		Map<AbstractEntity<?>,AbstractRecognizer> current = new HashMap<AbstractEntity<?>, AbstractRecognizer>();
		for(AbstractRecognizer recognizer: iterators.keySet())
		{	Iterator<AbstractEntity<?>> it = iterators.get(recognizer);
			AbstractEntity<?> entity = it.next();
			current.put(entity,recognizer);
		}
		
		// detect overlapping entities
		while(iterators.size()>1)
		{	// init map
			Map<AbstractRecognizer,AbstractEntity<?>> map = new HashMap<AbstractRecognizer, AbstractEntity<?>>();
			
			// identify the first entity
			Iterator<AbstractEntity<?>> it = current.keySet().iterator();
			AbstractEntity<?> first = it.next();
			while(it.hasNext())
			{	AbstractEntity<?> entity = it.next();
				if(entity.precedes(first))
					first = entity;
			}
			
			// compare other entities to the the first one
			it = current.keySet().iterator();
			Map<AbstractEntity<?>,AbstractRecognizer> newCurrent = new HashMap<AbstractEntity<?>, AbstractRecognizer>();
			while(it.hasNext())
			{	AbstractEntity<?> entity = it.next();
				AbstractRecognizer recognizer = current.get(entity);
				
				if(entity.overlapsWith(first))
				{	// update map
					map.put(recognizer,entity);
					
					// update iterator and entity list
					Iterator<AbstractEntity<?>> i = iterators.get(recognizer);
					if(i.hasNext())
					{	AbstractEntity<?> newEntity = i.next();
						newCurrent.put(newEntity,recognizer);
					}
					else
						iterators.remove(recognizer);
				}
				else
					newCurrent.put(entity,recognizer);
			}
			
			// update entity list
			current = newCurrent;
			
			result.add(map);
		}
		
		// add the remaining entities
		if(!iterators.isEmpty())
		{	Entry<AbstractRecognizer,Iterator<AbstractEntity<?>>> entry = iterators.entrySet().iterator().next();
			AbstractRecognizer recognizer = entry.getKey();
			Iterator<AbstractEntity<?>> it = entry.getValue();
			while(it.hasNext())
			{	Map<AbstractRecognizer,AbstractEntity<?>> map = new HashMap<AbstractRecognizer, AbstractEntity<?>>();
				AbstractEntity<?> entity = it.next();
				map.put(recognizer,entity);
				result.add(map);
			}
		}
		
		return result;
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
		
		// get source
		String sourceStr = element.getAttributeValue(XmlNames.ATT_SOURCE);
		RecognizerName source = RecognizerName.valueOf(sourceStr);
		
		// get date
		String dateStr = element.getAttributeValue(XmlNames.ATT_DATE);
		Date date = TimeFormatting.parseDate(dateStr);
		
		// get editor
		String editor = element.getAttributeValue(XmlNames.ATT_EDITOR);
		
		// get entities
		Entities result = new Entities(source, date);
		result.setEditor(editor);
		List<Element> elements = element.getChildren(XmlNames.ELT_ENTITY);
		for(Element e: elements)
		{	AbstractEntity<?> entity = AbstractEntity.importFromElement(e, source);
			result.addEntity(entity);
		}
		Collections.sort(result.entities);

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
		
		// insert source attribute
		Attribute sourceAttr = new Attribute(XmlNames.ATT_SOURCE, source.toString());
		element.setAttribute(sourceAttr);
		
		// insert date attribute
		String dateStr = TimeFormatting.formatDate(date);
		Attribute dateAttr = new Attribute(XmlNames.ATT_DATE, dateStr);
		element.setAttribute(dateAttr);
		
		// insert editor attribute
		if(editor!=null)
		{	Attribute editorAttr = new Attribute(XmlNames.ATT_EDITOR, editor);
			element.setAttribute(editorAttr);
		}
		
		// insert entity elements
		Collections.sort(entities);
		for(AbstractEntity<?> entity: entities)
		{	Element entityElt = entity.exportAsElement();
			element.addContent(entityElt);
		}
		
		// record file
		XmlTools.makeFileFromRoot(dataFile,schemaFile,element);
	}
}
