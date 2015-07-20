package tr.edu.gsu.extractor.data;

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

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

import tr.edu.gsu.extractor.tools.xml.GraphmlTools;

/**
 * This class represents a graph node.
 * 
 * @author Vincent Labatut
 */
public class Node implements Comparable<Node>
{
	/**
	 * Creates a new node.
	 * 
	 * @param name
	 * 		Name of the node.
	 */
	protected Node(String name)
	{	this.name = name;
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of this node */
	private String name;
	
	/**
	 * Returns the name of this node.
	 * 
	 * @return
	 * 		Name of this node.
	 */
	public String getName()
	{	return name;
	}

	/////////////////////////////////////////////////////////////////
	// LINKS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the links whose source is this node */
	private final Map<Node, Link> linksByTarget = new HashMap<Node, Link>();
	
	/**
	 * Returns the links connecting this node
	 * to the specified target, if such a link
	 * exists, and {@code null} otherwise.
	 * 
	 * @param target
	 * 		The target node.
	 * @return
	 * 		The link connecting this node to the target.
	 */
	public Link getLinkTo(Node target)
	{	Link result = linksByTarget.get(target);
		return result;
	}
	
	/**
	 * Adds a link to this node.
	 * 
	 * @param link
	 * 		New link (its source should be this node).
	 */
	protected void addLink(Link link)
	{	Node target = link.getTarget();
		linksByTarget.put(target, link);
	}
	
	/////////////////////////////////////////////////////////////////
	// PROPERTIES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Additionnal properties */
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
	
	/////////////////////////////////////////////////////////////////
	// GRAPHML			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/**
//	 * Processes the specified graphml element
//	 * and extract the node it contains.
//	 * 
//	 * @param nodeElt
//	 * 		Graphml element.
//	 * @return
//	 * 		A new {@code Node} object.
//	 */
//	public static Node importFromGraphml(Element nodeElt)
//	{	Node result = null;
//		// TODO
//		return result;
//	}
	
	/**
	 * Creates a graphml element representing
	 * this node object.
	 * 
	 * @return
	 * 		A Graphml element.
	 */
	public Element exportNode()
	{	// create element
		Element result = new Element(GraphmlTools.ELT_NODE);
		// add unique name
		result.setAttribute(GraphmlTools.ATT_ID,name);
		// add other properties
		GraphmlTools.exportPropertyValues(properties, "node", result);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(Node node)
	{	String name2 = node.getName();
		int result = name.compareTo(name2);
		return result;
	}
	
	@Override
	public boolean equals(Object o)
	{	boolean result = false;
		if(o instanceof Node)
		{	Node node = (Node)o;
			result = compareTo(node) == 0;
		}
		return result;
	}
	
	@Override
	public int hashCode()
	{	int result = name.hashCode();
		return result;
	}
}
