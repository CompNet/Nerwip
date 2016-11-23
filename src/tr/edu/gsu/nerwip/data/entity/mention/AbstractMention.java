package tr.edu.gsu.nerwip.data.entity.mention;

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
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ProcessorName;
import tr.edu.gsu.nerwip.tools.xml.XmlNames;

/**
 * Abstract class representing a general mention.
 * 
 * @param <T> 
 * 		Class of the mention value.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public abstract class AbstractMention<T extends Comparable<T>> implements Comparable<AbstractMention<T>>
{	
	/**
	 * General constructor for a mention.
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
	public AbstractMention(int startPos, int endPos, ProcessorName source, String valueStr, T value)
	{	this.startPos = startPos;
		this.endPos = endPos;
		this.source = source;
		this.valueStr = valueStr;
		this.value = value;
	}
	
	/**
	 * Builds an entity of the specified type.
	 * 
	 * @param <T>
	 * 		Class of the mention value.
	 * @param type
	 * 		Type of the mention.
	 * @param startPos
	 * 		Starting position in the text.
	 * @param endPos
	 * 		Ending position in the text.
	 * @param source
	 * 		Tool which detected this mention.
	 * @param valueStr
	 * 		String representation in the text.
	 * @return
	 * 		An object representing the mention.
	 */
	public static <T> AbstractMention<?> build(EntityType type, int startPos, int endPos, ProcessorName source, String valueStr)
	{	AbstractMention<?> result = null;

// debug
//if(valueStr.equals("1934"))
//	System.out.println();
	
		switch(type)
		{	case DATE:
				result = new MentionDate(startPos, endPos, source, valueStr, null);
				break;
			case FUNCTION:
				result = new MentionFunction(startPos, endPos, source, valueStr, null);
				break;
			case LOCATION:
				result = new MentionLocation(startPos, endPos, source, valueStr, null);
				break;
			case MEETING:
				result = new MentionMeeting(startPos, endPos, source, valueStr, null);
				break;
			case ORGANIZATION:
				result = new MentionOrganization(startPos, endPos, source, valueStr, null);
				break;
			case PERSON:
				result = new MentionPerson(startPos, endPos, source, valueStr, null);
				break;
			case PRODUCTION:
				result = new MentionProduction(startPos, endPos, source, valueStr, null);
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
			AbstractMention<?> mention = build(EntityType.PERSON, startPos, endPos, ProcessorName.STANFORD, valueStr); 
			mention.correctMentionSpan();
			System.out.println("\""+valueStr + "\"\t\t>>\t\t" + mention);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// VALUE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Mention value (depends on its type) */
	protected T value = null;
	
	/**
	 * Returns the actual value of this mention.
	 * For numeric mention, it should be the numerical value.
	 * For named mentions, it should be a unique representation
	 * of its semantics. For instance, an ontological concept,
	 * or an id in Freebase.
	 * 
	 * @return
	 * 		Actual value of this mention.
	 */
	public T getValue()
	{	return value;
	}

	/**
	 * Changes the actual value of this mention.
	 * For numeric mention, it should be the numerical value.
	 * For named mentions, it should be a unique representation
	 * of its semantics. For instance, an ontological concept,
	 * or an id in Freebase.
	 * 
	 * @param value
	 * 		New value of this mention.
	 */
	public void setValue(T value)
	{	this.value = value;
	}
	
	/////////////////////////////////////////////////////////////////
	// STRING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String representation of the mention (might also be its value if the mention is named) */
	protected String valueStr = null;
	
	/**
	 * Returns the original string corresponding
	 * to this mention.
	 * 
	 * @return
	 * 		Original string representation of this mention. 
	 */
	public String getStringValue()
	{	return valueStr;
	}

	/**
	 * Changes the original string corresponding
	 * to this mention.
	 * 
	 * @param valueStr
	 * 		New string representation of this mention.
	 */
	public void setStringValue(String valueStr)
	{	this.valueStr = valueStr;
	}
	
	/////////////////////////////////////////////////////////////////
	// CORRECTIONS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Pattern used to check the beginning/end of mentions */
	private static final Pattern PATTERN = Pattern.compile("[\\p{Z}\\p{Punct}]");
	
	/**
	 * Checks if the mention starts/ends with certain
	 * characters: punctuation, spaces, etc.
	 * If it is the case, the mention span is redefined
	 * so that these characters are placed outside
	 * of the mention.
	 */
	public void correctMentionSpan()
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
	 * Returns the type of this mention.
	 * 
	 * @return 
	 * 		The type of this mention.
	 */
	public abstract EntityType getType();
	
	/////////////////////////////////////////////////////////////////
	// POSITION			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Start position of a mention in the text */
	protected int startPos = -1;
	/** End position of a mention in the text */
	protected int endPos = -1;
		
	/**
	 * Returns the starting position of this
	 * mention, in the text.
	 * 
	 * @return
	 * 		Starting position of this mention.
	 */
	public int getStartPos()
	{	return startPos;
	}
	
	/**
	 * Returns the starting position of this
	 * mention, in the text.
	 * 
	 * @param startPos
	 * 		New starting position of this mention.
	 */
	public void setStartPos(int startPos)
	{	this.startPos = startPos;
	}

	/**
	 * Returns the ending position of this
	 * mention, in the text.
	 * 
	 * @return
	 * 		Ending position of this mention.
	 */
	public int getEndPos()
	{	return endPos;
	}
	
	/**
	 * Returns the ending position of this
	 * mention, in the text.
	 * 
	 * @param endPos
	 * 		New ending position of this mention.
	 */
	public void setEndPos(int endPos)
	{	this.endPos = endPos;
	}
	
	/**
	 * Checks if the specified text position
	 * is contained in this mention.
	 * 
	 * @param position
	 * 		Position to be checked.
	 * @return
	 * 		{@code true} iff the specified position
	 * 		is contained in this mention.
	 */
	public boolean containsPosition(int position)
	{	boolean result = position>=startPos && position <=endPos;
		return result;
	}
	
	/**
	 * Checks if the specified position is
	 * located after this mention (i.e. after
	 * its own ending position).
	 * 
	 * @param position
	 * 		Position to be checked.
	 * @return
	 * 		{@code true} iff the position is located
	 * 		after, and out of, this mention.
	 */
	public boolean precedesPosition(int position)
	{	boolean result = position>endPos;
		return result;
	}
	
	/**
	 * Checks if the specified position is
	 * located before this mention (i.e. before
	 * its own starting position).
	 * 
	 * @param position
	 * 		Position to be checked.
	 * @return
	 * 		{@code true} iff the position is located
	 * 		before, and out of, this mention.
	 */
	public boolean succeedesPosition(int position)
	{	boolean result = position<startPos;
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SOURCE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Origin of the mention (what detected it) */
	protected ProcessorName source;
	
	/**
	 * Returns the recognizer which
	 * detected this mention.
	 * 
	 * @return
	 * 		recognizer having detected this mention.
	 */
	public ProcessorName getSource()
	{	return source;
	}

	/**
	 * Changes the recognizer which
	 * detected this mention.
	 * 
	 * @param source
	 * 		New recognizer having detected this mention.
	 */
	public void setSource(ProcessorName source)
	{	this.source = source;
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/**
//	 * Checks if the specified mention overlaps
//	 * with this one. However, one should not
//	 * contain the other. Perfect matches are not
//	 * allowed neither.
//	 * 
//	 * @param mention
//	 * 		Mention to be compared with this one. 
//	 * @return
//	 * 		{@code true} only if they partially overlap.
//	 */
//	public boolean overlapsStrictlyWith(AbstractEntity<?> mention)
//	{	int startPos2 = mention.getStartPos();
//		int endPos2 = mention.getEndPos();
//		
//		boolean result = (startPos2<endPos && endPos2>endPos)
//			|| (startPos<endPos2 && endPos>endPos2);
//		
//		return result;
//	}
	
	/**
	 * Checks if the specified mention overlaps
	 * with this one. Inclusion and perfect match
	 * are also allowed.
	 * 
	 * @param mention
	 * 		Mention to be compared with this one. 
	 * @return
	 * 		{@code true} only if they partially overlap.
	 */
	public boolean overlapsWith(AbstractMention<?> mention)
	{	int startPos2 = mention.getStartPos();
		int endPos2 = mention.getEndPos();
		
		boolean result = (startPos2<=endPos && endPos2>=endPos)
			|| (startPos<=endPos2 && endPos>=endPos2);
		
		return result;
	}

	/**
	 * Checks if this mention overlaps with <i>at least</i>
	 * one of the mentions in the specified list. Inclusion 
	 * and perfect match are also allowed.
	 * 
	 * @param mentions
	 * 		List of mentions to be compared with this one. 
	 * @return
	 * 		{@code true} only if this mention partially overlaps
	 * 		with at least one of the listed mentions.
	 */
	public boolean overlapsWithOne(List<AbstractMention<?>> mentions)
	{	boolean result = false;
		Iterator<AbstractMention<?>> it = mentions.iterator();
		
		while(!result && it.hasNext())
		{	AbstractMention<?> mention = it.next();
			result = overlapsWith(mention);
		}
		
		return result;
	}

//	/**
//	 * Checks if the specified mention is strictly contained
//	 * in this mention. Perfect matches are not allowed.
//	 * 
//	 * @param mention
//	 * 		Mention to be compared with this one. 
//	 * @return
//	 * 		{@code true} only if this mention contained the specified one.
//	 */
//	public boolean containsStrictly(AbstractEntity<?> mention)
//	{	int startPos2 = mention.getStartPos();
//		int endPos2 = mention.getEndPos();
//		
//		boolean result = startPos2>=startPos && endPos2<endPos
//			|| startPos2>startPos && endPos2<=endPos;
//
//		return result;
//	}

	/**
	 * Checks if the specified mention is contained in,
	 * or matches this mention.
	 * 
	 * @param mention
	 * 		Mention to be compared with this one. 
	 * @return
	 * 		{@code true} only if this mention contained the specified one.
	 */
	public boolean contains(AbstractMention<?> mention)
	{	int startPos2 = mention.getStartPos();
		int endPos2 = mention.getEndPos();
		
		boolean result = startPos2>=startPos && endPos2<=endPos;
		
		return result;
	}
	
	/**
	 * Checks if the specified mention matches (spatially)
	 * exactly this mention.
	 * 
	 * @param mention
	 * 		Mention to be compared with this one. 
	 * @return
	 * 		{@code true} only if this mention matches the specified one.
	 */
	public boolean hasSamePosition(AbstractMention<?> mention)
	{	int startPos2 = mention.getStartPos();
		int endPos2 = mention.getEndPos();
	
		boolean result = startPos==startPos2 && endPos==endPos2;
		
		return result;
	}
	
	/**
	 * Returns the length of this mention,
	 * calculated from its positions in the text.
	 * 
	 * @return
	 * 		Length of the mention in characters.
	 */
	public int getLength()
	{	int result = endPos-startPos;
		return result;
	}

	/**
	 * Checks if this mention is located before
	 * the specified mention. Only the starting
	 * position is considered. And using a strict
	 * comparison.
	 * 
	 * @param mention
	 * 		Mention to compare to this one.
	 * @return
	 * 		{@code true} iff this entity is located 
	 * 		before the specified one.
	 */
	public boolean precedes(AbstractMention<?> mention)
	{	int startPos = mention.getStartPos();
		boolean result = this.startPos < startPos;
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// TEXT				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks if the text recorded for this mention
	 * corresponds to the one found in the article
	 * at the mention positions.
	 * 
	 * @param article
	 * 		Article of reference. 
	 * @return 
	 * 		{@code true} iff the article and mention texts are the same.
	 */
	public boolean checkText(Article article)
	{	String text = article.getRawText();
		String valueStr2 = text.substring(startPos,endPos);
		boolean result = valueStr.equals(valueStr2);
		return result;
	}
	
	@Override
	public String toString()
	{	String result = "MENTION(";
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
	 * Returns a representation of this mention
	 * as an XML element.
	 * 
	 * @return
	 * 		An XML element representing this mention.
	 */
	public abstract Element exportAsElement();
	
	/**
	 * Builds a mention from the specified
	 * XML element.
	 * 
	 * @param element
	 * 		XML element representing the mention.
	 * @param source
	 * 		Name of the recognizer which detected the mention.
	 * @return
	 * 		The mention corresponding to the specified element.
	 */
	public static AbstractMention<?> importFromElement(Element element, ProcessorName source)
	{	AbstractMention<?> result = null;
		
		String typeStr = element.getAttributeValue(XmlNames.ATT_TYPE);
		EntityType type = EntityType.valueOf(typeStr);
		switch(type)
		{	case DATE:
				result = MentionDate.importFromElement(element,source);
				break;
			case FUNCTION:
				result = MentionFunction.importFromElement(element,source);
				break;
			case LOCATION:
				result = MentionLocation.importFromElement(element,source);
				break;
			case MEETING:
				result = MentionMeeting.importFromElement(element,source);
				break;
			case ORGANIZATION:
				result = MentionOrganization.importFromElement(element,source);
				break;
			case PERSON:
				result = MentionPerson.importFromElement(element,source);
				break;
			case PRODUCTION:
				result = MentionProduction.importFromElement(element,source);
				break;
		}
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// COMPARABLE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(AbstractMention<T> o)
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
	 * Compare the value of this mention to that
	 * of the specified mention. Both mentions
	 * must contain values of the same type.
	 * 
	 * @param mention
	 * 		The other mention.
	 * @return
	 * 		An integer classically representing the result of the comparison.
	 */
	public int compareValueTo(AbstractMention<T> mention)
	{	T value = mention.getValue();
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
		{	if(obj instanceof AbstractMention<?>)
			{	AbstractMention<?> mention = (AbstractMention<?>)obj;
				int start = mention.getStartPos();
				if(this.startPos==start)
				{	int endPos = mention.getEndPos();
					if(this.endPos==endPos)
					{	String valueStr = mention.getStringValue();
						result = this.valueStr.equals(valueStr);
					}
				}
			}
		}
		
		return result;
	}
}
