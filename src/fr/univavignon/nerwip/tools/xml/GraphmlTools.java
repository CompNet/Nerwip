package fr.univavignon.nerwip.tools.xml;

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

import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Element;

/**
 * This class contains Graphml-related names
 * and methods.
 * 
 * @author Vincent Labatut
 */
public class GraphmlTools
{	
	/////////////////////////////////////////////////////////////////
	// GENERAL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Graphml namespace */
	public static final String NAMESPACE_URL = "http://graphml.graphdrawing.org/xmlns";
	/** Graphml schema */
	public static final String SCHEMA_URL = "http://graphml.graphdrawing.org/xmlns/1.0/graphml-structure.xsd";
	
	/////////////////////////////////////////////////////////////////
	// ATTRIBUTES			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Graphml attribute name */
	public static final String ATT_ATTR_NAME = "attr.name";
	/** Graphml attribute name */
	public static final String ATT_ATTR_TYPE = "attr.type";
	/** Graphml attribute name */
	public static final String ATT_EDGEDEFAULT = "edgedefault";
	/** Graphml attribute name */
	public static final String ATT_FOR = "for";
	/** Graphml attribute name */
	public static final String ATT_ID = "id";
	/** Graphml attribute name */
	public static final String ATT_KEY = "key";
	/** Graphml attribute name */
	public static final String ATT_SOURCE = "source";
	/** Graphml attribute name */
	public static final String ATT_TARGET = "target";

	/////////////////////////////////////////////////////////////////
	// ELEMENTS				/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Graphml element name */
	public static final String ELT_DATA = "data";
	/** Graphml element name */
	public static final String ELT_EDGE = "edge";
	/** Graphml element name */
	public static final String ELT_GRAPH = "graph";
	/** Graphml element name */
	public static final String ELT_GRAPHML = "graphml";
	/** Graphml element name */
	public static final String ELT_KEY = "key";
	/** Graphml element name */
	public static final String ELT_NODE = "node";

	/////////////////////////////////////////////////////////////////
	// VALUES				/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Graphml attribute value */
	public static final String VAL_DIRECTED = "directed";
	/** Graphml attribute value */
	public static final String VAL_UNDIRECTED = "undirected";

	/////////////////////////////////////////////////////////////////
	// PROCESS				/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Adds the property values to the specified Graphml element.
	 * 
	 * @param properties
	 * 		Map of property values.
	 * @param mode
	 * 		Type of concerned object (graph, node, edge).
	 * @param element
	 * 		Element of the Graphml document.
	 */
	public static void exportPropertyValues(Map<String,String> properties, String mode, Element element)
	{	for(Entry<String,String> entry: properties.entrySet())
		{	String property = entry.getKey();
			String value = entry.getValue();
			Element dataElt = new Element(GraphmlTools.ELT_DATA);
			
			String keyStr = mode.subSequence(0,1) + "_" + property;
			dataElt.setAttribute(GraphmlTools.ATT_KEY,keyStr);
			dataElt.setText(value);
			
			element.addContent(dataElt);
		}
	}
}
