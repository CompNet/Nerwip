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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.jdom2.Comment;
import org.jdom2.Element;

import tr.edu.gsu.extractor.tools.xml.GraphmlTools;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.time.TimeFormatting;
import tr.edu.gsu.nerwip.tools.xml.XmlTools;

/**
 * This class represents a graph, i.e.
 * a set of nodes and the links between them.
 * The goal is just to store some data, not
 * to process complicated calculations.
 * 
 * @author Vincent Labatut
 */
public class Graph
{
	/**
	 * Creates a new graph.
	 * 
	 * @param name
	 * 		Name of the graph.
	 * @param directed
	 * 		Whether the graph links are directed or not. 
	 */
	public Graph(String name, boolean directed)
	{	this.name = name;
		this.directed = directed;
	}

	/////////////////////////////////////////////////////////////////
	// DIRECTED			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether the graph links are directed or not */
	private boolean directed = false;
	
	/**
	 * Indicates whether the graph links are directed or not.
	 * 
	 * @return
	 * 		{@code true} iff the graph is directed.
	 */
	public boolean isDirected()
	{	return directed;
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of this graph */
	private String name;
	
	/**
	 * Returns the name of this graph.
	 * 
	 * @return
	 * 		Name of this graph.
	 */
	public String getName()
	{	return name;
	}

	/////////////////////////////////////////////////////////////////
	// PROPERTIES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the graph properties and their associated values */
	private final Map<String,String> properties = new HashMap<String, String>();
	/** Map containing all the graph properties and their associated data types */
	private final Map<String,String> propertyTypes = new HashMap<String, String>();
	
	/**
	 * Creates a new property for the graph.
	 * 
	 * @param name
	 * 		Unique name of the property.
	 * @param type
	 * 		Data type of the property value.
	 * @param value
	 * 		Value of the property.
	 */
	public void setProperty(String name, String type, String value)
	{	// add data type
		propertyTypes.put(name, type);
		
		// set the value
		properties.put(name, value);
	}
	
	/**
	 * Returns the appropriate init value
	 * for the specified data type. This
	 * method is used when creating new
	 * properties.
	 * 
	 * @param type
	 * 		Data type.
	 * @return
	 * 		Corresponding init value.
	 */
	private String selectInitValue(String type)
	{	String result = null;
		switch(type)
		{	case "xsd:string":
				result = "NA";
				break;
			case "xsd:integer":
			case "xsd:int":
				result = "0";
				break;
			case "xsd:float":
			case "xsd:double":
				result = "0";
				break;
			
		}
		return result;
	}
	
	/**
	 * Adds the property types to the specified element.
	 * 
	 * @param propertyTypes
	 * 		Map of property types.
	 * @param mode
	 * 		Type of concerned object (graph, node, edge).
	 * @param element
	 * 		Root element of the Graphml document.
	 */
	private void exportPropertyTypes(Map<String,String> propertyTypes, String mode, Element element)
	{	for(Entry<String,String> entry: propertyTypes.entrySet())
		{	String property = entry.getKey();
			String type = entry.getValue();
			Element keyElt = new Element(GraphmlTools.ELT_KEY);
			
			String idStr = mode.subSequence(0,1) + "_" + property;
			keyElt.setAttribute(GraphmlTools.ATT_ID,idStr);
			keyElt.setAttribute(GraphmlTools.ATT_FOR,mode);
			keyElt.setAttribute(GraphmlTools.ATT_ATTR_NAME,property);
			keyElt.setAttribute(GraphmlTools.ATT_ATTR_TYPE,type);
			
			element.addContent(keyElt);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// NODES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the graph nodes */
	private final Map<String,Node> nodesByName = new HashMap<String, Node>();
	/** Map containing all the node properties and their associated data types */
	private final Map<String,String> nodePropertyTypes = new HashMap<String, String>();
	
	/**
	 * Returns the node whose name is specified.
	 * If no such node exists, it is first created
	 * the returned.
	 * 
	 * @param name
	 * 		Name of the node of interest.	
	 * @return
	 * 		Node of interest. 
	 */
	public Node retrieveNode(String name)
	{	// look for an existing node with the specified name
		Node result = nodesByName.get(name);
		
		// if none, create it
		if(result==null)
		{	result = new Node(name);
		
			// add to local map
			nodesByName.put(name,result);
			
			// set node properties
			for(Entry<String,String> entry: nodePropertyTypes.entrySet())
			{	String pName = entry.getKey();
				String type = entry.getValue();
				String value = selectInitValue(type);
				result.properties.put(pName, value);
			}
		}
		return result;
	}

	/**
	 * Creates a new property for the nodes.
	 * 
	 * @param name
	 * 		Unique name of the property.
	 * @param type
	 * 		Data type of the property values.
	 */
	public void addNodeProperty(String name, String type)
	{	// add data type
		nodePropertyTypes.put(name, type);
		
		// select init value
		String value = selectInitValue(type);

		// set the init value in each node
		for(Node node: nodesByName.values())
			node.properties.put(name, value);
	}
	
	/////////////////////////////////////////////////////////////////
	// LINKS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the graph links, ranked by source node */
	private final Map<Node,Link> linksBySource = new HashMap<Node, Link>();
	/** Map containing all the graph links, ranked by target node */
	private final Map<Node,Link> linksByTarget = new HashMap<Node, Link>();
	/** Map containing all the link properties and their associated data types */
	private final Map<String,String> linkPropertyTypes = new HashMap<String, String>();
	
	/**
	 * Retrieve the link between the specified nodes.
	 * If no such link exist, it is first created
	 * then returned. The nodes must exist.
	 * 
	 * @param sourceName
	 * 		Name of the source node of the link.
	 * @param targetName
	 * 		Name of the target node of the link.
	 * @return
	 * 		Link connecting the source to the target nodes.
	 */
	public Link retrieveLink(String sourceName, String targetName)
	{	Node source = nodesByName.get(sourceName);
		Node target = nodesByName.get(targetName);
		Link result = retrieveLink(source, target);
		return result;
	}
	
	/**
	 * Retrieve the link between the specified nodes.
	 * If no such link exist, it is first created
	 * then returned. The nodes must exist.
	 * 
	 * @param source
	 * 		Source node of the link.
	 * @param target
	 * 		Target node of the link.
	 * @return
	 * 		Link connecting the source to the target nodes.
	 */
	public Link retrieveLink(Node source, Node target)
	{	Link result = source.getLinkTo(target);
		
		// link not found
		if(result==null)
		{	// if undirected graph, we look for the reverse link 
			if(!directed)
				result = target.getLinkTo(source);
			
			// link not found: we create it
			if(result==null)
			{	// directed graph
				if(directed)
				{	result = new Link(source,target);
					source.addLink(result);
				}
				// undirected graph
				else
				{	// even for undirected graphs, we prefer to order nodes 
					if(source.compareTo(target)<0)
					{	result = new Link(source,target);
						source.addLink(result);
					}
					else
					{	result = new Link(target,source);
						target.addLink(result);
					}
				}
				
				// add to local maps
				Node s = result.getSource();
				linksBySource.put(s,result);
				Node t = result.getTarget();
				linksByTarget.put(t,result);
				
				// set link properties
				for(Entry<String,String> entry: linkPropertyTypes.entrySet())
				{	String name = entry.getKey();
					String type = entry.getValue();
					String value = selectInitValue(type);
					result.properties.put(name, value);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Creates a new property for the links.
	 * 
	 * @param name
	 * 		Unique name of the property.
	 * @param type
	 * 		Data type of the property values.
	 */
	public void addLinkProperty(String name, String type)
	{	// add data type
		linkPropertyTypes.put(name, type);
		
		// select init value
		String value = selectInitValue(type);

		// set the init value in each link
		for(Link link: linksBySource.values())
			link.properties.put(name, value);
	}

	/////////////////////////////////////////////////////////////////
	// GRAPHML			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/**
//	 * Processes the specified graphml element
//	 * and extract the graph it contains.
//	 * 
//	 * @param graphElt
//	 * 		Graphml element.
//	 * @return
//	 * 		A new {@code Graph} object.
//	 */
//	public static Graph readFromGraphml(Element graphElt)
//	{	Graph result = null;
//		// T O D O
//		return result;
//	}
	
	/**
	 * Creates a Graphml element representing
	 * this graph object.
	 * 
	 * @return
	 * 		A Graphml element.
	 */
	private Element exportGraph()
	{	// init the root element
		Element result = new Element(GraphmlTools.ELT_GRAPHML);
		
		// add the property types
		exportPropertyTypes(propertyTypes, "graph", result);
		exportPropertyTypes(nodePropertyTypes, "node", result);
		exportPropertyTypes(linkPropertyTypes, "link", result);
		
		// put the signature
		Comment comment = new Comment("File generated by Nerwip on "+TimeFormatting.formatCurrentXmlTime());
		result.addContent(comment);
		
		// add graph element
		Element graphElt = new Element(GraphmlTools.ELT_GRAPH);
		graphElt.setAttribute(GraphmlTools.ATT_ID,name);
		String edgedefaultStr = GraphmlTools.VAL_DIRECTED;
		if(!directed)
			edgedefaultStr = GraphmlTools.VAL_UNDIRECTED;
		graphElt.setAttribute(GraphmlTools.ATT_EDGEDEFAULT,edgedefaultStr);
		GraphmlTools.exportPropertyValues(properties, "graph", graphElt);
		result.addContent(graphElt);

		// add node elements
		comment = new Comment("Node list");
		graphElt.addContent(comment);
		TreeSet<Node> nodes = new TreeSet<Node>(nodesByName.values());
		for(Node node: nodes)
		{	Element nodeElt = node.exportNode();
			graphElt.addContent(nodeElt);
		}
		
		// add link elements
		comment = new Comment("Link list");
		graphElt.addContent(comment);
		TreeSet<Link> links = new TreeSet<Link>(linksBySource.values());
		for(Link link: links)
		{	Element linkElt = link.exportLink();
			graphElt.addContent(linkElt);
		}

		return result;
	}
	
	/**
	 * Export this graph as a Graphml file.
	 * 
	 * @param dataFile
	 * 		File object to create.
	 *  
	 * @throws IOException
	 * 		Problem while accessing a file. 
	 */
	public void writeToXml(File dataFile) throws IOException
	{	// schema file
		String schemaPath = FileNames.FO_SCHEMA + File.separator + FileNames.FI_GRAPHML_SCHEMA;
		File schemaFile = new File(schemaPath);
		
		// build xml document
		Element element = exportGraph();
		
		// record file
		XmlTools.makeFileFromRoot(dataFile,schemaFile,element);
	}
}
