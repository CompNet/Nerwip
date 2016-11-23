package fr.univavignon.nerwip.data.entity;

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
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.recognition.AbstractProcessor;
import fr.univavignon.nerwip.recognition.ProcessorName;
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
	 * date and the reference source.
	 */
	public Entities()
	{	initDates();
		linker = ProcessorName.REFERENCE;
	}
	
	/**
	 * Builds a Entities object with current
	 * date and specified linker.
	 * 
	 * @param linker
	 * 		Linker used on these entities.
	 */
	public Entities(ProcessorName linker)
	{	initDates();
		this.linker = linker;
	}
	
	/**
	 * Builds a Entities object with specified
	 * dates and linker.
	 * 
	 * @param linker
	 * 		Linker used on these entities.
	 * @param creationDate
	 * 		Date the entities were processed.
	 * @param modificationDate
	 * 		Date the entities were last processed.
	 */
	public Entities(ProcessorName linker, Date creationDate, Date modificationDate)
	{	this.creationDate = creationDate;
		this.modificationDate = modificationDate;
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
	// LINKER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Linker used to link these entities (or {@link ProcessorName#REFERENCE} for manual annotations) */
	private ProcessorName linker = null;
	
	/**
	 * Returns the linker which detected
	 * these entities.
	 * 
	 * @return
	 * 		Name of the linker used to process these entities.
	 */
	public ProcessorName getSource()
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
	
	//TODO same thing for the solver?
	
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
	private long nextInternalId = 0;	//TODO this must be recorded/loaded with the rest of this class
	/** Set of entities */
	private final Set<AbstractEntity> entities = new TreeSet<AbstractEntity>();
	/** Entities by id */
	private final Map<Long,AbstractEntity> entitiesById = new HashMap<Long,AbstractEntity>();
	
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
	 * Returns the entity with the specified id.
	 * 
	 * @param id
	 * 		Unique id of the entity.
	 * @return
	 * 		Entity possessing the specified id, or {@code null]} if none does.
	 */
	public AbstractEntity getEntityById(long id)
	{	AbstractEntity result = entitiesById.get(id);
		return result;
	}
	
	/**
	 * Adds a new entity to the list.
	 * 
	 * @param entity
	 * 		Entity to add to the list.
	 */
	public void addEntity(AbstractEntity entity)
	{	// possibly update the entity id
		if(entity.internalId<0)
		{	entity.internalId = nextInternalId;
			nextInternalId++;
		}
		
		// add the entity
		entities.add(entity);
		entitiesById.put(entity.internalId,entity);
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE ACCESS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/**
//	 * Reads the specified XML file, and 
//	 * builds the corresponding Mentions object,
//	 * which contains both mentions and meta-data.
//	 * 
//	 * @param dataFile
//	 * 		The XML file to be read.
//	 * @return
//	 * 		The list of mentions and meta-data stored in the file.
//	 * 
//	 * @throws SAXException
//	 * 		Problem while reading the file.
//	 * @throws IOException
//	 * 		Problem while reading the file.
//	 * @throws ParseException 
//	 * 		Problem while parsing the date.
//	 */
//	public static Entities readFromXml(File dataFile) throws SAXException, IOException, ParseException
//	{	// schema file
//		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_MENTION_SCHEMA;
//		File schemaFile = new File(schemaPath);
//
//		// load file
//		Element element = XmlTools.getRootFromFile(dataFile,schemaFile);
//		
//		// get source
//		String sourceStr = element.getAttributeValue(XmlNames.ATT_SOURCE);
//		ProcessorName source = ProcessorName.valueOf(sourceStr);
//		
//		// get dates
////String creationDateStr = element.getAttributeValue(XmlNames.ATT_DATE);
////SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy",Locale.ENGLISH);
////Date date = df.parse(creationDateStr);
//		String creationDateStr = element.getAttributeValue(XmlNames.ATT_CREATION);
//		Date creationDate = TimeFormatting.parseXmlTime(creationDateStr);
//		String modificationDateStr = element.getAttributeValue(XmlNames.ATT_MODIFICATION);
//		Date modificationDate = TimeFormatting.parseXmlTime(modificationDateStr);
//		
//		// get editor
//		String editor = element.getAttributeValue(XmlNames.ATT_EDITOR);
//		
//		// get mentions
////Mentions result = new Mentions(source, date);
//		Entities result = new Entities(source, creationDate, modificationDate);
//		result.setEditor(editor);
//		List<Element> elements = element.getChildren(XmlNames.ELT_MENTION);
//		for(Element e: elements)
//		{	AbstractMention<?> mention = AbstractMention.importFromElement(e, source);
//			result.addMention(mention);
//		}
//		Collections.sort(result.mentions);
//
////result.writeToXml(dataFile);
//		return result;
//	}
//
//	/**
//	 * Write this Mentions object under the form of
//	 * a XML file using our own format.
//	 * 
//	 * @param dataFile
//	 * 		File to contain the mentions.
//	 * 
//	 * @throws IOException
//	 * 		Problem while writing the file.
//	 */
//	public void writeToXml(File dataFile) throws IOException
//	{	// schema file
//		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_MENTION_SCHEMA;
//		File schemaFile = new File(schemaPath);
//		
//		// build xml document
//		Element element = new Element(XmlNames.ELT_MENTIONS);
//		
//		// insert source attribute
//		Attribute sourceAttr = new Attribute(XmlNames.ATT_SOURCE, source.toString());
//		element.setAttribute(sourceAttr);
//		
//		// insert date attributes
//		String creationDateStr = TimeFormatting.formatXmlTime(creationDate);
//		Attribute creationDateAttr = new Attribute(XmlNames.ATT_CREATION, creationDateStr);
//		element.setAttribute(creationDateAttr);
//		String modificationDateStr = TimeFormatting.formatXmlTime(modificationDate);
//		Attribute modificationDateAttr = new Attribute(XmlNames.ATT_MODIFICATION, modificationDateStr);
//		element.setAttribute(modificationDateAttr);
//		
//		// insert editor attribute
//		if(editor!=null)
//		{	Attribute editorAttr = new Attribute(XmlNames.ATT_EDITOR, editor);
//			element.setAttribute(editorAttr);
//		}
//		
//		// insert mention elements
//		Collections.sort(mentions);
//		for(AbstractMention<?> mention: mentions)
//		{	Element mentionElt = mention.exportAsElement();
//			element.addContent(mentionElt);
//		}
//		
//		// record file
//		XmlTools.makeFileFromRoot(dataFile,schemaFile,element);
//	}
}
