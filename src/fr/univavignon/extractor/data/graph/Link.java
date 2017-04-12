package fr.univavignon.extractor.data.graph;

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

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

import fr.univavignon.nerwip.tools.xml.GraphmlTools;

/**
 * This class represents a graph link.
 * 
 * @author Vincent Labatut
 */
public class Link implements Comparable<Link>
{
	/**
	 * Creates a new link.
	 * 
	 * @param source
	 * 		Source node of the link.
	 * @param target
	 * 		Target node of the link.
	 */
	protected Link(Node source, Node target)
	{	this.source = source;
		this.target = target;
	}

	/////////////////////////////////////////////////////////////////
	// SOURCE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Source node of this link */
	private Node source;
	
	/**
	 * Returns the source node of this link.
	 * 
	 * @return
	 * 		Source node of this link.
	 */
	public Node getSource()
	{	return source;
	}

	/////////////////////////////////////////////////////////////////
	// TARGET			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Target node of this link */
	private Node target;
	
	/**
	 * Returns the target node of this link.
	 * 
	 * @return
	 * 		Target node of this link.
	 */
	public Node getTarget()
	{	return target;
	}

	/////////////////////////////////////////////////////////////////
	// PROPERTIES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Additional properties */
	protected final Map<String,String> properties = new HashMap<String, String>();
	
	/**
	 * Sets the specified property with the
	 * specified value. Note the property itself
	 * (by opposition to the value) must be registered 
	 * first in the graph.
	 * 
	 * @param name
	 * 		(Unique) name of the property.
	 * @param value
	 * 		Value associated to the property.
	 */
	public void setProperty(String name, String value)
	{	String oldValue = properties.get(name);
		if(oldValue==null)
			throw new IllegalArgumentException("Unknown property ("+name+")");
		else
		properties.put(name, value);
	}
	
	/**
	 * Increments the value of the specified property.
	 * The property must already exist, and it must
	 * be an integer.
	 * 
	 * @param name
	 * 		Name of the property.
	 */
	public void incrementIntProperty(String name)
	{	String valueStr = properties.get(name);
		int value = Integer.parseInt(valueStr) + 1;
		valueStr = Integer.toString(value);
		properties.put(name, valueStr);
	}
	
	/**
	 * Increments the value of the specified property 
	 * with an increment value equal to delta.
	 * The property must already exist.
	 * 
	 * @param name
	 * 		Name of the property.
	 * @param delta
	 *      The increment value
	 */
	public void incrementIntProperty(String name, int delta)
	{	String valueStr = properties.get(name);
		int value = Integer.parseInt(valueStr) + delta;
		valueStr = Integer.toString(value);
		properties.put(name, valueStr);
	}
	
	/**
	 * Increments the value of the specified property 
	 * with an increment value equal to delta.
	 * The property must already exist.
	 * 
	 * @param name
	 * 		Name of the property.
	 * @param delta
	 *      The increment value
	 */
	public void incrementFloatProperty(String name, float delta)
	{	String valueStr = properties.get(name);
		float value = Float.parseFloat(valueStr) + delta;
		valueStr = Float.toString(value);
		properties.put(name, valueStr);
	}
	
	/////////////////////////////////////////////////////////////////
	// GRAPHML			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/**
//	 * Processes the specified graphml element
//	 * and extract the link it contains.
//	 * 
//	 * @param linkElt
//	 * 		Graphml element.
//	 * @return
//	 * 		A new {@code Link} object.
//	 */
//	public static Link importFromGraphml(Element linkElt)
//	{	Link result = null;
//		// TODO
//		return result;
//	}
	
	/**
	 * Creates a graphml element representing
	 * this link object.
	 * 
	 * @return
	 * 		A Graphml element.
	 */
	public Element exportLink()
	{	// create element
		Element result = new Element(GraphmlTools.ELT_EDGE);
		// add source and target
		String sourceName = source.getName();
		result.setAttribute(GraphmlTools.ATT_SOURCE,sourceName);
		String targetName = target.getName();
		result.setAttribute(GraphmlTools.ATT_TARGET,targetName);
		// add other properties
		GraphmlTools.exportPropertyValues(properties, "edge", result);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns some internal name for this link.
	 * 
	 * @return
	 * 		Internal link name.
	 */
	private String getName()
	{	String result = source.getName() + ">" + target.getName();
		return result;
	}
	
	@Override
	public int compareTo(Link link)
	{	String name1 = getName();
		String name2 = link.getName();
		int result = name1.compareTo(name2);
		return result;
	}
	
	@Override
	public boolean equals(Object o)
	{	boolean result = false;
		if(o instanceof Link)
		{	Link node = (Link)o;
			result = compareTo(node) == 0;
		}
		return result;
	}
	
	@Override
	public int hashCode()
	{	String name = getName();
		int result = name.hashCode();
		return result;
	}
}
