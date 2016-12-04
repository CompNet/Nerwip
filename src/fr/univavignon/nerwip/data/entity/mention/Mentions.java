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

import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.time.TimeFormatting;
import fr.univavignon.nerwip.tools.xml.XmlNames;
import fr.univavignon.nerwip.tools.xml.XmlTools;

/**
 * Class representing a list of mentions,
 * with some meta-data related to how
 * they were obtained.
 * 
 * @author Vincent Labatut
 */
public class Mentions
{	
	/**
	 * Builds a Mentions object with current
	 * date and the reference recognizer.
	 */
	public Mentions()
	{	initDates();
		recognizer = ProcessorName.REFERENCE;
	}
	
	/**
	 * Builds a Mentions object with current
	 * date and specified recognizer.
	 * 
	 * @param recognizer
	 * 		Recognizer used to get the mentions.
	 */
	public Mentions(ProcessorName recognizer)
	{	initDates();
		this.recognizer = recognizer;
	}
	
//	/**
//	 * Builds a Mentions object with specified
//	 * date and recognizer.
//	 * 
//	 * @param recognizer
//	 * 		Recognizer used to get the mentions.
//	 * @param date
//	 * 		Date the mentions were detected.
//	 */
//	public Mentions(ProcessorName recognizer, Date date)
//	{	this.creationDate = date;
//		this.modificationDate = date;
//		this.recognizer = recognizer;
//	}
	
	/**
	 * Builds a Mentions object with specified
	 * dates and recognizer.
	 * 
	 * @param recognizer
	 * 		Recognizer used to get the mentions.
	 * @param creationDate
	 * 		Date the mentions were detected.
	 * @param modificationDate
	 * 		Date the mentions were last modified.
	 */
	public Mentions(ProcessorName recognizer, Date creationDate, Date modificationDate)
	{	this.creationDate = creationDate;
		this.modificationDate = modificationDate;
		this.recognizer = recognizer;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Date the mentions were detected/annotated */
	private Date creationDate = null;
	/** Date the mentions were last modified (mainly for manual annotation files) */
	private Date modificationDate = null;
	
	/**
	 * Sets the detection date
	 * to the current time.
	 */
	private void initDates()
	{	Calendar cal = Calendar.getInstance();
		creationDate = cal.getTime();
		modificationDate = cal.getTime();
	}
	
	/**
	 * Returns the date these mentions
	 * were detected.
	 * 
	 * @return
	 * 		Date these mentions were detected.
	 */
	public Date getCreationDate()
	{	return creationDate;
	}
	
	/**
	 * Returns the date these mentions
	 * were last modified.
	 * 
	 * @return
	 * 		Date these mentions were modified.
	 */
	public Date getModificationDate()
	{	return modificationDate;
	}
	
	/**
	 * Changes the date these mentions
	 * were detected.
	 * 
	 * @param creationDate
	 * 		Date of the detection.
	 */
	public void setCreationDate(Date creationDate)
	{	this.creationDate = creationDate;
	}
	
	/**
	 * Changes the date these mentions
	 * were last modified.
	 * 
	 * @param modificationDate
	 * 		Date of the modification.
	 */
	public void setModificationDate(Date modificationDate)
	{	this.modificationDate = modificationDate;
	}
	
	/////////////////////////////////////////////////////////////////
	// RECOGNIZER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Recognizer which detected these mentions (or {@link ProcessorName#REFERENCE} for manual annotations) */
	private ProcessorName recognizer = null;
	
	/**
	 * Returns the recognizer which detected
	 * these mentions.
	 * 
	 * @return
	 * 		Name of the recognizer having detected these mentions.
	 */
	public ProcessorName getRecognizer()
	{	return recognizer;
	}
	
	/**
	 * Changes the recognizer which detected
	 * these mentions.
	 * 
	 * @param recognizer
	 * 		New name of the recognizer having detected these mentions.
	 */
	public void setRecognizer(ProcessorName recognizer)
	{	this.recognizer = recognizer;
	}
	
	/////////////////////////////////////////////////////////////////
	// EDITOR			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Human person that originally annotated these mentions (only relevant if the recognizer name is REFERENCE) */
	private String editor = null;
	
	/**
	 * Returns the name of the person which
	 * originally annotated these mentions.
	 * This is relevant only if the recognizer
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
	 * annotated these mentions.
	 * 
	 * @param editor
	 * 		New editor name.
	 */
	public void setEditor(String editor)
	{	this.editor = editor;
	}
	
	/////////////////////////////////////////////////////////////////
	// MENTIONS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of mentions */
	private final List<AbstractMention<?>> mentions = new ArrayList<AbstractMention<?>>();
	
	/**
	 * Returns the whole list
	 * of detected mentions.
	 * 
	 * @return
	 * 		List of Mention objects.
	 */
	public List<AbstractMention<?>> getMentions()
	{	return mentions;
	}
	
	/**
	 * Returns the mention at the specified position (in the list of 
	 * mentions, not in the text).
	 * 
	 * @param index
	 * 		Position of the required mention.
	 * @return
	 * 		Mention at the specified position.
	 */
	public AbstractMention<?> getMentionAt(int index)
	{	AbstractMention<?> result = mentions.get(index);
		return result;
	}

	/**
	 * Returns the list of mentions overlapping the specified range.
	 * The parameters are expressed in terms of characters in the original
	 * text.
	 * 
	 * @param startPos
	 * 		Position of the first character in the specified range.
	 * @param endPos
	 * 		Position of the last character+1 in the specified range.
	 * @return
	 * 		List of the concerned mentions.
	 */
	public List<AbstractMention<?>> getMentionsIn(int startPos, int endPos)
	{	List<AbstractMention<?>> result = new ArrayList<AbstractMention<?>>();
		
		for(AbstractMention<?> mention: mentions)
		{	if(mention.containsPosition(startPos) || mention.containsPosition(endPos-1)
				|| (mention.startPos>=startPos && mention.startPos<endPos)
				|| (mention.endPos>=startPos && mention.endPos<endPos))
				result.add(mention);
		}
		
		return result;
	}

	/**
	 * Adds a new mention to the list.
	 * <br/>
	 * No redundance check is performed. 
	 * 
	 * @param mention
	 * 		Mention to add to the list.
	 */
	public void addMention(AbstractMention<?> mention)
	{	mentions.add(mention);
	}
	
	/**
	 * Adds all the mentions from the specified object
	 * to this object.
	 * <br/>
	 * No redundance check is performed. 
	 * 
	 * @param mentions
	 * 		Mentions to add to the list.
	 */
	public void addMentions(Mentions mentions)
	{	List<AbstractMention<?>> list = mentions.getMentions();
		this.mentions.addAll(list);
	}

	/**
	 * Adds to this object all the mentions contained in 
	 * the specified list.
	 * <br/>
	 * No redundance check is performed. 
	 * 
	 * @param mentions
	 * 		List of mentions to add to the list.
	 */
	public void addMentions(List<AbstractMention<?>> mentions)
	{	this.mentions.addAll(mentions);
	}

	/**
	 * Removes the specified mention
	 * from this list.
	 * 
	 * @param mention
	 * 		Mention object to be removed from the list.
	 */
	public void removeMention(AbstractMention<?> mention)
	{	mentions.remove(mention);
	}

	/**
	 * Returns a sublist containing only the mentions of
	 * a certain type.
	 * 
	 * @param type
	 * 		The desired mention type.
	 * @return
	 * 		List of mentions of this type.
	 */
	public List<AbstractMention<?>> getMentionsByType(EntityType type)
	{	List<AbstractMention<?>> result = new ArrayList<AbstractMention<?>>();
		for(AbstractMention<?> mention: mentions)
		{	EntityType t = mention.getType();
			if(t==type)
				result.add(mention);
		}
		return result;
	}
	
	/**
	 * Sorts the mentions in the list
	 * depending on their position (first
	 * to last).
	 */
	public void sortByPosition()
	{	Collections.sort(mentions);
	}
	
	/**
	 * Takes a list of mentions of various types, and returns
	 * only those with the specified type.
	 * 
	 * @param list
	 * 		List of mentions.
	 * @param type
	 * 		Mention type of interest.
	 * @return
	 * 		A sublist of the original mention list.
	 */
	public static List<AbstractMention<?>> filterByType(List<AbstractMention<?>> list, EntityType type)
	{	List<AbstractMention<?>> result = new ArrayList<AbstractMention<?>>();
		
		for(AbstractMention<?> mention: list)
		{	EntityType t = mention.getType();
			if(t==type)
				result.add(mention);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// POSITIONS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Shifts to the right all mentions located after 
	 * position {@code start}, by {@code length} characters.
	 * 
	 * @param start
	 * 		Starting position of the right shifting.
	 * @param length
	 * 		Magnitude of the shifting, in characters.
	 * @param text
	 * 		Full text of the concerned article (used
	 * 		to update the mention string values.
	 */
	public void rightShiftMentionPositions(int start, int length, String text)
	{	Iterator<AbstractMention<?>> it = mentions.iterator();
		while(it.hasNext())
		{	AbstractMention<?> mention = it.next();
			boolean keep = rightShiftMentionPosition(mention, start, length, text);
			if(!keep)
				it.remove();
		}
	}
	
	/**
	 * Shifts the specified mention by {@code length} characters 
	 * to the right, provided it is located after position 
	 * {@code start}, 
	 * 
	 * @param mention
	 * 		The mention which should be shifted.
	 * @param start
	 * 		Starting position of the right shifting.
	 * @param length
	 * 		Magnitude of the shifting, in characters.
	 * @param text
	 * 		Full text of the concerned article (used
	 * 		to update the mention string value.
	 * @return
	 * 		Whether the mention is still valid ({@code true}) or is now empty ({@code false}).
	 */
	public boolean rightShiftMentionPosition(AbstractMention<?> mention, int start, int length, String text)
	{	boolean result = false;
	
		// start position
		int startPos = mention.getStartPos();
		if(start<=startPos)
			startPos = Math.min(startPos+length, text.length());
		
		// end position
		int endPos = mention.getEndPos();
		if(start<endPos)
			endPos = Math.min(endPos+length, text.length());
		
		// update mentions
		if(startPos<endPos)
		{	result = true;
			mention.setStartPos(startPos);
			mention.setEndPos(endPos);
			String valueStr = text.substring(startPos,endPos);
			mention.setStringValue(valueStr);
		}
		
		return result;
	}
	
	/**
	 * Shifts to the left all mentions located after 
	 * position {@code start}, by {@code length} characters.
	 * 
	 * @param start
	 * 		Starting position of the left shifting.
	 * @param length
	 * 		Magnitude of the shifting, in characters.
	 * @param text
	 * 		Full text of the concerned article (used
	 * 		to update the mentions string values.
	 */
	public void leftShiftMentionPositions(int start, int length, String text)
	{	Iterator<AbstractMention<?>> it = mentions.iterator();
		while(it.hasNext())
		{	AbstractMention<?> mention = it.next();
			boolean keep = leftShiftMentionPosition(mention, start, length, text);
			if(!keep)
				it.remove();
		}
	}

	/**
	 * Shifts the specified mention by {@code length} characters 
	 * to the left, provided it is located after position 
	 * {@code start}, 
	 * 
	 * @param mention
	 * 		The mention which should be shifted.
	 * @param start
	 * 		Starting position of the left shifting.
	 * @param length
	 * 		Magnitude of the shifting, in characters.
	 * @param text
	 * 		Full text of the concerned article (used
	 * 		to update the mention string value.
	 * @return
	 * 		Whether the mention is still valid ({@code true}) or is now empty ({@code false}).
	 */
	public boolean leftShiftMentionPosition(AbstractMention<?> mention, int start, int length, String text)
	{	boolean result = false;
		
		// start position
		int startPos = mention.getStartPos();
		if(start<=startPos)
			startPos = Math.max(startPos-length, start);
		
		// end position
		int endPos = mention.getEndPos();
		if(start<endPos)
			endPos = Math.max(endPos-length, start);
		
		// update mention
		if(startPos<endPos)
		{	result = true;
			mention.setStartPos(startPos);
			mention.setEndPos(endPos);
			String valueStr = text.substring(startPos,endPos);
			mention.setStringValue(valueStr);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// MENTION COMPARISON	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks if the specified mention overlaps (spatially) with one
	 * of the mentions present in this Mentions object.
	 * 
	 * @param mention
	 * 		Mention of interest.
	 * @return
	 * 		{@code true} iff it intersects an existing mention.
	 */
	public boolean isMentionOverlapping(AbstractMention<?> mention)
	{	boolean result = mention.overlapsWithOne(mentions);
		return result;
	}
	
	/**
	 * Takes the output of several recognizers,
	 * and compare the resulting mentions.
	 * The returned list contains maps of
	 * overlapping mentions detected
	 * by distinct recognizers, and which are considered
	 * to be the same mention.
	 * 
	 * @param mentions
	 * 		Mentions detected by several tools.
	 * @return
	 * 		List of maps of equivalent mentions.
	 */
	public static List<Map<InterfaceRecognizer,AbstractMention<?>>> identifyOverlaps(Map<InterfaceRecognizer,Mentions> mentions)
	{	List<Map<InterfaceRecognizer,AbstractMention<?>>> result = new ArrayList<Map<InterfaceRecognizer,AbstractMention<?>>>();
		
		// sort all mentions
		for(Mentions e: mentions.values())
			e.sortByPosition();
		
		// init iterators
		Map<InterfaceRecognizer,Iterator<AbstractMention<?>>> iterators = new HashMap<InterfaceRecognizer, Iterator<AbstractMention<?>>>();
		for(InterfaceRecognizer recognizer: mentions.keySet())
		{	Mentions e = mentions.get(recognizer);
			Iterator<AbstractMention<?>> it = e.getMentions().iterator();
			if(it.hasNext())
				iterators.put(recognizer,it);
		}
		
		// init current mentions
		Map<AbstractMention<?>,InterfaceRecognizer> current = new HashMap<AbstractMention<?>, InterfaceRecognizer>();
		for(InterfaceRecognizer recognizer: iterators.keySet())
		{	Iterator<AbstractMention<?>> it = iterators.get(recognizer);
			AbstractMention<?> mention = it.next();
			current.put(mention,recognizer);
		}
		
		// detect overlapping mentions
		while(iterators.size()>1)
		{	// init map
			Map<InterfaceRecognizer,AbstractMention<?>> map = new HashMap<InterfaceRecognizer, AbstractMention<?>>();
			
			// identify the first mention
			Iterator<AbstractMention<?>> it = current.keySet().iterator();
			AbstractMention<?> first = it.next();
			while(it.hasNext())
			{	AbstractMention<?> mention = it.next();
				if(mention.precedes(first))
					first = mention;
			}
			
			// compare other mentions to the the first one
			it = current.keySet().iterator();
			Map<AbstractMention<?>,InterfaceRecognizer> newCurrent = new HashMap<AbstractMention<?>, InterfaceRecognizer>();
			while(it.hasNext())
			{	AbstractMention<?> mention = it.next();
				InterfaceRecognizer recognizer = current.get(mention);
				
				if(mention.overlapsWith(first))
				{	// update map
					map.put(recognizer,mention);
					
					// update iterator and mention list
					Iterator<AbstractMention<?>> i = iterators.get(recognizer);
					if(i.hasNext())
					{	AbstractMention<?> newMention = i.next();
						newCurrent.put(newMention,recognizer);
					}
					else
						iterators.remove(recognizer);
				}
				else
					newCurrent.put(mention,recognizer);
			}
			
			// update mention list
			current = newCurrent;
			
			result.add(map);
		}
		
		// add the remaining mentions
		if(!iterators.isEmpty())
		{	Entry<InterfaceRecognizer,Iterator<AbstractMention<?>>> entry = iterators.entrySet().iterator().next();
		InterfaceRecognizer recognizer = entry.getKey();
			Iterator<AbstractMention<?>> it = entry.getValue();
			while(it.hasNext())
			{	Map<InterfaceRecognizer,AbstractMention<?>> map = new HashMap<InterfaceRecognizer, AbstractMention<?>>();
				AbstractMention<?> mention = it.next();
				map.put(recognizer,mention);
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
	 * builds the corresponding Mentions object,
	 * which contains both mentions and meta-data.
	 * 
	 * @param dataFile
	 * 		The XML file to be read.
	 * @return
	 * 		The list of mentions and meta-data stored in the file.
	 * 
	 * @throws SAXException
	 * 		Problem while reading the file.
	 * @throws IOException
	 * 		Problem while reading the file.
	 * @throws ParseException 
	 * 		Problem while parsing the date.
	 */
	public static Mentions readFromXml(File dataFile) throws SAXException, IOException, ParseException
	{	Mentions result = readFromXml(dataFile, null);
		return result;
	}
	
	/**
	 * Reads the specified XML file, and 
	 * builds the corresponding Mentions object,
	 * which contains both mentions and meta-data.
	 * <br/>
	 * The specified Entities are used to initialize
	 * the mentions. Unless it is {@code null}, in
	 * which case the method behaves like
	 * {@link #readFromXml(File)}.
	 * 
	 * @param dataFile
	 * 		The XML file to be read.
	 * @param entities
	 * 		Previously loaded entities (can be {@code null}).
	 * @return
	 * 		The list of mentions and meta-data stored in the file.
	 * 
	 * @throws SAXException
	 * 		Problem while reading the file.
	 * @throws IOException
	 * 		Problem while reading the file.
	 * @throws ParseException 
	 * 		Problem while parsing the date.
	 */
	public static Mentions readFromXml(File dataFile, Entities entities) throws SAXException, IOException, ParseException
	{	// schema file
		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_MENTION_SCHEMA;
		File schemaFile = new File(schemaPath);

		// load file
		Element element = XmlTools.getRootFromFile(dataFile,schemaFile);
		
		// get recognizer
		String recognizerStr = element.getAttributeValue(XmlNames.ATT_SOURCE);
		ProcessorName recognizer = ProcessorName.valueOf(recognizerStr);
		
		// get dates
		String creationDateStr = element.getAttributeValue(XmlNames.ATT_CREATION);
		Date creationDate = TimeFormatting.parseXmlTime(creationDateStr);
		String modificationDateStr = element.getAttributeValue(XmlNames.ATT_MODIFICATION);
		Date modificationDate = TimeFormatting.parseXmlTime(modificationDateStr);
		
		// get editor
		String editor = element.getAttributeValue(XmlNames.ATT_EDITOR);
		
		// get mentions
		Mentions result = new Mentions(recognizer, creationDate, modificationDate);
		result.setEditor(editor);
		List<Element> elements = element.getChildren(XmlNames.ELT_MENTION);
		for(Element e: elements)
		{	AbstractMention<?> mention = AbstractMention.importFromElement(e, recognizer, entities);
			result.addMention(mention);
		}
		Collections.sort(result.mentions);

		return result;
	}

	/**
	 * Write this Mentions object under the form of
	 * a XML file using our own format.
	 * 
	 * @param dataFile
	 * 		File to contain the mentions.
	 * 
	 * @throws IOException
	 * 		Problem while writing the file.
	 */
	public void writeToXml(File dataFile) throws IOException
	{	writeToXml(dataFile,null);
	}
	
	/**
	 * Write this Mentions object under the form of
	 * a XML file using our own format. The entity
	 * ids are also written in the XML file. Unless
	 * the {@code entities} parameter is {@code null},
	 * in which case the method behaves like
	 * {@link #writeToXml(File)}.
	 * 
	 * @param dataFile
	 * 		File to contain the mentions.
	 * @param entities
	 * 		Existing entities.
	 * 
	 * @throws IOException
	 * 		Problem while writing the file.
	 */
	public void writeToXml(File dataFile, Entities entities) throws IOException
	{	// schema file
		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_MENTION_SCHEMA;
		File schemaFile = new File(schemaPath);
		
		// build xml document
		Element element = new Element(XmlNames.ELT_MENTIONS);
		
		// insert recognizer attribute
		Attribute recognizerAttr = new Attribute(XmlNames.ATT_SOURCE, recognizer.toString());
		element.setAttribute(recognizerAttr);
		
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
		
		// insert mention elements
		Collections.sort(mentions);
		for(AbstractMention<?> mention: mentions)
		{	Element mentionElt = mention.exportAsElement(entities);
			element.addContent(mentionElt);
		}
		
		// record file
		XmlTools.makeFileFromRoot(dataFile,schemaFile,element);
	}
}
