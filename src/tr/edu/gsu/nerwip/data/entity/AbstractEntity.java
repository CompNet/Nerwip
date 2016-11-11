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

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.tools.xml.XmlNames;

/**
 * Abstract class representing a general entity.
 * 
 * @param <T> 
 * 		Class of the entity value.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public abstract class AbstractEntity<T extends Comparable<T>> implements Comparable<AbstractEntity<T>>
{	
	/**
	 * General constructor for an entity.
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
	public AbstractEntity(int startPos, int endPos, RecognizerName source, String valueStr, T value)
	{	this.startPos = startPos;
		this.endPos = endPos;
		this.source = source;
		this.valueStr = valueStr;
		this.value = value;
	}
	
	
	//TODO we need to add some id in order to allow unique identification, and therefore coreference resolution
	//or rename this Entity class to something like "instance" or "mention" 
	
	/**
	 * Builds an entity of the specified type.
	 * 
	 * @param <T>
	 * 		Class of the entity value.
	 * @param type
	 * 		Type of the entity.
	 * @param startPos
	 * 		Starting position in the text.
	 * @param endPos
	 * 		Ending position in the text.
	 * @param source
	 * 		Tool which detected this entity.
	 * @param valueStr
	 * 		String representation in the text.
	 * @return
	 * 		An object representing the entity.
	 */
	public static <T> AbstractEntity<?> build(EntityType type, int startPos, int endPos, RecognizerName source, String valueStr)
	{	AbstractEntity<?> result = null;

// debug
//if(valueStr.equals("1934"))
//	System.out.println();
	
		switch(type)
		{	case DATE:
				result = new EntityDate(startPos, endPos, source, valueStr, null);
				break;
			case FUNCTION:
				result = new EntityFunction(startPos, endPos, source, valueStr, null);
				break;
			case LOCATION:
				result = new EntityLocation(startPos, endPos, source, valueStr, null);
				break;
			case MEETING:
				result = new EntityMeeting(startPos, endPos, source, valueStr, null);
				break;
			case ORGANIZATION:
				result = new EntityOrganization(startPos, endPos, source, valueStr, null);
				break;
			case PERSON:
				result = new EntityPerson(startPos, endPos, source, valueStr, null);
				break;
			case PRODUCTION:
				result = new EntityProduction(startPos, endPos, source, valueStr, null);
				break;
		}
		
		return result;
	}
	
	/**
	 * For testing purposes.
	 * 
	 * @param args
	 * 		None needed.
	 */
	public static void main(String[] args)
	{	int startPos = 10;
		String valuesStr[] = 
		{	"Test",
			"Test et retest",
			" Test et retest",
			".Test et retest",
			"Test et retest ",
			"Test et retest.",
			" Test et retest ",
			".Test et retest.",
			" Test et retest.",
			".Test et retest ",
			"  Test et retest  ",
			".!Test et retest,;",
			". Test et retest ;"
		};
		for(String valueStr: valuesStr)
		{	int endPos = startPos + valueStr.length();
			AbstractEntity<?> entity = build(EntityType.PERSON, startPos, endPos, RecognizerName.STANFORD, valueStr); 
			entity.correctEntitySpan();
			System.out.println("\""+valueStr + "\"\t\t>>\t\t" + entity);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// VALUE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Entity value (depends on its type) */
	protected T value = null;
	
	/**
	 * Returns the actual value of this entity.
	 * For numeric entity, it should be the numerical value.
	 * For named entities, it should be a unique representation
	 * of its semantics. For instance, an ontological concept,
	 * or an id in Freebase.
	 * 
	 * @return
	 * 		Actual value of this entity.
	 */
	public T getValue()
	{	return value;
	}

	/**
	 * Changes the actual value of this entity.
	 * For numeric entity, it should be the numerical value.
	 * For named entities, it should be a unique representation
	 * of its semantics. For instance, an ontological concept,
	 * or an id in Freebase.
	 * 
	 * @param value
	 * 		New value of this entity.
	 */
	public void setValue(T value)
	{	this.value = value;
	}
	
	/////////////////////////////////////////////////////////////////
	// STRING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String representation of the entity (might also be its value if the entity is named) */
	protected String valueStr = null;
	
	/**
	 * Returns the original string corresponding
	 * to this entity.
	 * 
	 * @return
	 * 		Original string representation of this entity. 
	 */
	public String getStringValue()
	{	return valueStr;
	}

	/**
	 * Changes the original string corresponding
	 * to this entity.
	 * 
	 * @param valueStr
	 * 		New string representation of this entity.
	 */
	public void setStringValue(String valueStr)
	{	this.valueStr = valueStr;
	}
	
	/////////////////////////////////////////////////////////////////
	// CORRECTIONS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Pattern used to check the beginning/end of entity mentions */
	private static final Pattern PATTERN = Pattern.compile("[\\p{Z}\\p{Punct}]");
	
	/**
	 * Checks if the entity starts/ends with certain
	 * characters: punctuation, spaces, etc.
	 * If it is the case, the entity span is redefined
	 * so that these characters are placed outside
	 * of the entity.
	 */
	public void correctEntitySpan()
	{	boolean found;
		do
		{	found = false;
			Matcher matcher = PATTERN.matcher(valueStr);
		 	while(matcher.find() && !found)
		 	{	int start = matcher.start();
		 		if(start==0)
		 		{	valueStr = valueStr.substring(1);
		 			startPos++;
		 			found = true;
		 		}
		 		else if(start==valueStr.length()-1)
		 		{	valueStr = valueStr.substring(0,valueStr.length()-1);
		 			endPos--;
		 			found = true;
		 		}
		 	}
		}
		while(found);
	}
	
	/////////////////////////////////////////////////////////////////
	// TYPE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the type of this entity.
	 * 
	 * @return 
	 * 		The type of this entity.
	 */
	public abstract EntityType getType();
	
	/////////////////////////////////////////////////////////////////
	// POSITION			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Start position of an entity in the text */
	protected int startPos = -1;
	/** End position of an entity in the text */
	protected int endPos = -1;
		
	/**
	 * Returns the starting position of this
	 * entity, in the text.
	 * 
	 * @return
	 * 		Starting position of this entity.
	 */
	public int getStartPos()
	{	return startPos;
	}
	
	/**
	 * Returns the starting position of this
	 * entity, in the text.
	 * 
	 * @param startPos
	 * 		New starting position of this entity.
	 */
	public void setStartPos(int startPos)
	{	this.startPos = startPos;
	}

	/**
	 * Returns the ending position of this
	 * entity, in the text.
	 * 
	 * @return
	 * 		Ending position of this entity.
	 */
	public int getEndPos()
	{	return endPos;
	}
	
	/**
	 * Returns the ending position of this
	 * entity, in the text.
	 * 
	 * @param endPos
	 * 		New ending position of this entity.
	 */
	public void setEndPos(int endPos)
	{	this.endPos = endPos;
	}
	
	/**
	 * Checks if the specified text position
	 * is contained in this entity.
	 * 
	 * @param position
	 * 		Position to be checked.
	 * @return
	 * 		{@code true} iff the specified position
	 * 		is contained in this entity.
	 */
	public boolean containsPosition(int position)
	{	boolean result = position>=startPos && position <=endPos;
		return result;
	}
	
	/**
	 * Checks if the specified position is
	 * located after this entity (i.e. after
	 * its own ending position).
	 * 
	 * @param position
	 * 		Position to be checked.
	 * @return
	 * 		{@code true} iff the position is located
	 * 		after, and out of, this entity.
	 */
	public boolean precedesPosition(int position)
	{	boolean result = position>endPos;
		return result;
	}
	
	/**
	 * Checks if the specified position is
	 * located before this entity (i.e. before
	 * its own starting position).
	 * 
	 * @param position
	 * 		Position to be checked.
	 * @return
	 * 		{@code true} iff the position is located
	 * 		before, and out of, this entity.
	 */
	public boolean succeedesPosition(int position)
	{	boolean result = position<startPos;
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SOURCE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Origin of the entity (what detected it) */
	protected RecognizerName source;
	
	/**
	 * Returns the NER tool which
	 * detected this entity.
	 * 
	 * @return
	 * 		NER tool having detected this entity.
	 */
	public RecognizerName getSource()
	{	return source;
	}

	/**
	 * Changes the NER tool which
	 * detected this entity.
	 * 
	 * @param source
	 * 		New NER tool having detected this entity.
	 */
	public void setSource(RecognizerName source)
	{	this.source = source;
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/**
//	 * Checks if the specified entity overlaps
//	 * with this one. However, one should not
//	 * contain the other. Perfect matches are not
//	 * allowed neither.
//	 * 
//	 * @param entity
//	 * 		Entity to be compared with this one. 
//	 * @return
//	 * 		{@code true} only if they partially overlap.
//	 */
//	public boolean overlapsStrictlyWith(AbstractEntity<?> entity)
//	{	int startPos2 = entity.getStartPos();
//		int endPos2 = entity.getEndPos();
//		
//		boolean result = (startPos2<endPos && endPos2>endPos)
//			|| (startPos<endPos2 && endPos>endPos2);
//		
//		return result;
//	}
	
	/**
	 * Checks if the specified entity overlaps
	 * with this one. Inclusion and perfect match
	 * are also allowed.
	 * 
	 * @param entity
	 * 		Entity to be compared with this one. 
	 * @return
	 * 		{@code true} only if they partially overlap.
	 */
	public boolean overlapsWith(AbstractEntity<?> entity)
	{	int startPos2 = entity.getStartPos();
		int endPos2 = entity.getEndPos();
		
		boolean result = (startPos2<=endPos && endPos2>=endPos)
			|| (startPos<=endPos2 && endPos>=endPos2);
		
		return result;
	}

	/**
	 * Checks if this entity overlaps with <i>at least</i>
	 * one of the entities in the specified list. Inclusion 
	 * and perfect match are also allowed.
	 * 
	 * @param entities
	 * 		List of entities to be compared with this one. 
	 * @return
	 * 		{@code true} only if this entity partially overlaps
	 * 		with at least one of the listed entities.
	 */
	public boolean overlapsWithOne(List<AbstractEntity<?>> entities)
	{	boolean result = false;
		Iterator<AbstractEntity<?>> it = entities.iterator();
		
		while(!result && it.hasNext())
		{	AbstractEntity<?> entity = it.next();
			result = overlapsWith(entity);
		}
		
		return result;
	}

//	/**
//	 * Checks if the specified entity is strictly contained
//	 * in this entity. Perfect matches are not allowed.
//	 * 
//	 * @param entity
//	 * 		Entity to be compared with this one. 
//	 * @return
//	 * 		{@code true} only if this entity contained the specified one.
//	 */
//	public boolean containsStrictly(AbstractEntity<?> entity)
//	{	int startPos2 = entity.getStartPos();
//		int endPos2 = entity.getEndPos();
//		
//		boolean result = startPos2>=startPos && endPos2<endPos
//			|| startPos2>startPos && endPos2<=endPos;
//
//		return result;
//	}

	/**
	 * Checks if the specified entity is contained in,
	 * or matches this entity.
	 * 
	 * @param entity
	 * 		Entity to be compared with this one. 
	 * @return
	 * 		{@code true} only if this entity contained the specified one.
	 */
	public boolean contains(AbstractEntity<?> entity)
	{	int startPos2 = entity.getStartPos();
		int endPos2 = entity.getEndPos();
		
		boolean result = startPos2>=startPos && endPos2<=endPos;
		
		return result;
	}
	
	/**
	 * Checks if the specified entity matches (spatially)
	 * exactly this entity.
	 * 
	 * @param entity
	 * 		Entity to be compared with this one. 
	 * @return
	 * 		{@code true} only if this entity matches the specified one.
	 */
	public boolean hasSamePosition(AbstractEntity<?> entity)
	{	int startPos2 = entity.getStartPos();
		int endPos2 = entity.getEndPos();
	
		boolean result = startPos==startPos2 && endPos==endPos2;
		
		return result;
	}
	
	/**
	 * Returns the length of this entity,
	 * calculated from its positions in the text.
	 * 
	 * @return
	 * 		Length of the entity in characters.
	 */
	public int getLength()
	{	int result = endPos-startPos;
		return result;
	}

	/**
	 * Checks if this entity is located before
	 * the specified entity. Only the starting
	 * position is considered. And using a strict
	 * comparison.
	 * 
	 * @param entity
	 * 		Entity to compare to this one.
	 * @return
	 * 		{@code true} iff this entity is located 
	 * 		before the specified one.
	 */
	public boolean precedes(AbstractEntity<?> entity)
	{	int startPos = entity.getStartPos();
		boolean result = this.startPos < startPos;
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// TEXT				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks if the text recorded for this entity
	 * corresponds to the one found in the article
	 * at the entity positions.
	 * 
	 * @param article
	 * 		Article of reference. 
	 * @return 
	 * 		{@code true} iff the article and entity texts are the same.
	 */
	public boolean checkText(Article article)
	{	String text = article.getRawText();
		String valueStr2 = text.substring(startPos,endPos);
		boolean result = valueStr.equals(valueStr2);
		return result;
	}
	
	@Override
	public String toString()
	{	String result = "ENTITY(";
		result = result + "STRING=\"" + valueStr+"\"";
		result = result + ", TYPE=" + getType(); 
		result = result + ", POS=("+startPos+","+endPos+")"; 
		result = result + ", SOURCE="+source.toString();
		if(value!=null)
			result = result + ", VALUE=(" + value.toString() + ")"; 
		result = result + ")";
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// XML				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns a representation of this entity
	 * as an XML element.
	 * 
	 * @return
	 * 		An XML element representing this entity.
	 */
	public abstract Element exportAsElement();
	
	/**
	 * Builds an entity from the specified
	 * XML element.
	 * 
	 * @param element
	 * 		XML element representing the entity.
	 * @param source
	 * 		Name of the NER tool which detected the entity.
	 * @return
	 * 		The entity corresponding to the specified element.
	 */
	public static AbstractEntity<?> importFromElement(Element element, RecognizerName source)
	{	AbstractEntity<?> result = null;
		
		String typeStr = element.getAttributeValue(XmlNames.ATT_TYPE);
		EntityType type = EntityType.valueOf(typeStr);
		switch(type)
		{	case DATE:
				result = EntityDate.importFromElement(element,source);
				break;
			case FUNCTION:
				result = EntityFunction.importFromElement(element,source);
				break;
			case LOCATION:
				result = EntityLocation.importFromElement(element,source);
				break;
			case MEETING:
				result = EntityMeeting.importFromElement(element,source);
				break;
			case ORGANIZATION:
				result = EntityOrganization.importFromElement(element,source);
				break;
			case PERSON:
				result = EntityPerson.importFromElement(element,source);
				break;
			case PRODUCTION:
				result = EntityProduction.importFromElement(element,source);
				break;
		}
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// COMPARABLE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(AbstractEntity<T> o)
	{	
//if(o==null)
//	System.out.print("");

		int startPos = o.getStartPos();
		int result = this.startPos - startPos;
		if(result==0)
		{	int endPos = o.getEndPos();
			result = this.endPos - endPos;
			if(result==0)
			{	String valueStr = o.getStringValue();
//if(valueStr==null)
//	System.out.print("");
				result = this.valueStr.compareTo(valueStr);
			}
		}
		return result;
	}
	
	/**
	 * Compare the value of this entity to that
	 * of the specified entity. Both entities
	 * must contain values of the same type.
	 * 
	 * @param entity
	 * 		The other entity.
	 * @return
	 * 		An integer classically representing the result of the comparison.
	 */
	public int compareValueTo(AbstractEntity<T> entity)
	{	T value = entity.getValue();
		int result = this.value.compareTo(value);
		return result;
	}
	
	@Override
	public int hashCode()
	{	String temp = startPos + ":" + endPos + ":" + valueStr + ":" + source;
		int result = temp.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{	boolean result = false;
		
		if(obj!=null)
		{	if(obj instanceof AbstractEntity<?>)
			{	AbstractEntity<?> entity = (AbstractEntity<?>)obj;
				int start = entity.getStartPos();
				if(this.startPos==start)
				{	int endPos = entity.getEndPos();
					if(this.endPos==endPos)
					{	String valueStr = entity.getStringValue();
						result = this.valueStr.equals(valueStr);
					}
				}
			}
		}
		
		return result;
	}
}
