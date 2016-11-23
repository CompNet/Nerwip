package fr.univavignon.nerwip.tools.xml;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import fr.univavignon.nerwip.tools.file.FileNames;

/**
 * This class contains a set of methods related to XML managment.
 * 
 * @author Vincent Labatut
 */
public class XmlTools
{	
	/////////////////////////////////////////////////////////////////
	// INITIALIZATION	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Maps of document builders, each one corresponding to a specifiec XML schema */
	public static final HashMap<String,DocumentBuilder> DOCUMENT_BUILDERS = new HashMap<String,DocumentBuilder>();	
	
	/** Populates the map of document builders */
	static
	{	try
		{	init();
		}
		catch (SAXException e)
		{	e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{	e.printStackTrace();
		}
	}

	/**
	 * Retrieves all schemas in the corresponding folder,
	 * load them and put them in the {@code #DOCUMENT_BUILDERS} map.
	 * 
	 * @throws SAXException
	 * 		Problem while retrieving one the schemas.
	 * @throws ParserConfigurationException
	 * 		Problem while retrieving one the schemas.
	 */
	public static void init() throws SAXException, ParserConfigurationException
	{	// init
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    schemaFactory.setErrorHandler(new ErrorHandler()
	    {	@Override
        	public void error(SAXParseException e) throws SAXException
	    	{   throw e;
	    	}
	    	@Override
    		public void fatalError(SAXParseException e) throws SAXException
	    	{   throw e;
	    	}
	    	@Override
        	public void warning(SAXParseException e) throws SAXException
	    	{   throw e;
	    	}
	    });
	    
	    // loading all schemas
//	    System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.parsers.SAXParser");
//	    System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
		File folder = new File(FileNames.FO_SCHEMA);
		File[] files = folder.listFiles();
		for(int i=0;i<files.length;i++)
		{	if(files[i].isFile())
			{	String name = files[i].getName();
//if(!name.equals("graphml.xsd")) // URL doesn't respond, at the time of testing			
{				Schema schema = schemaFactory.newSchema(files[i]);
				// DOM parser
				DocumentBuilderFactory documentBuilderfactory = DocumentBuilderFactory.newInstance();
		        documentBuilderfactory.setNamespaceAware(true);
		        documentBuilderfactory.setIgnoringElementContentWhitespace(true);
		        documentBuilderfactory.setSchema(schema);
		        DocumentBuilder builder = documentBuilderfactory.newDocumentBuilder();
		        builder.setErrorHandler(new ErrorHandler()
		        {   @Override
		        	public void fatalError(SAXParseException e) throws SAXException
		        	{   throw e;
		        	}
		        	@Override
	        		public void error(SAXParseException e) throws SAXParseException
			    	{   throw e;
			    	}
		        	@Override
		        	public void warning(SAXParseException e) throws SAXParseException
			        {   throw e;
			        }
				});
				DOCUMENT_BUILDERS.put(name,builder);
}
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// ACCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Open and reads the file corresponding to an XML document,
	 * parses it using the specified XML schema file,
	 * and returns the result as a JDom object.
	 *  
	 * @param dataFile
	 * 		The XML document to be parsed.
	 * @param schemaFile
	 * 		The XML schema file to be used for validation.
	 * @return
	 * 		A JDom {@link Element} corresponding to the result of the parsing.
	 * 
	 * @throws SAXException
	 * 		Problem while parsing the XML files.
	 * @throws IOException
	 * 		Problem while retrieving the XML file.
	 */
	public static Element getRootFromFile(File dataFile, File schemaFile) throws SAXException, IOException
	{	// init
		FileInputStream in = new FileInputStream(dataFile);
		BufferedInputStream inBuff = new BufferedInputStream(in);
		
		// JAXP
		DocumentBuilder bldr = DOCUMENT_BUILDERS.get(schemaFile.getName());
		org.w3c.dom.Document doc;
		try
		{	doc = bldr.parse(inBuff);
		}
		catch (SAXException e)
		{	System.out.println(dataFile+" : "+schemaFile);
			throw e;
		}
		catch (IOException e)
		{	System.out.println(dataFile+" : "+schemaFile);
			throw e;
		}
		
		// JDOM
		DOMBuilder builder = new DOMBuilder();
        Document document = builder.build(doc);
		
        // root
		Element result = document.getRootElement();
		inBuff.close();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// CREATION			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Creates a new xml file using the specified element
	 * as a root. The schema path is used to bound the
	 * resulting document to a specific, local schema.
	 * 
	 * @param dataFile
	 * 		The xml file to be created.
	 * @param schemaFile
	 * 		The xml schema to be mentioned.
	 * @param root
	 * 		The root element of the document.
	 * 
	 * @throws IOException
	 * 		Problem when recording the new xml document.
	 */
	public static void makeFileFromRoot(File dataFile, File schemaFile, Element root) throws IOException
	{	// open file stream
		FileOutputStream out = new FileOutputStream(dataFile);
		BufferedOutputStream outBuf = new BufferedOutputStream(out);
		
		// create document
		Document document = new Document(root);
		
		// schema
		String schemaPath = schemaFile.getPath();
		File tempFile = new File(dataFile.getPath()).getParentFile();
		while(tempFile!=null)
		{	tempFile = tempFile.getParentFile();
			schemaPath = ".."+File.separator+schemaPath;
		}
		// Namespace sch = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
	    Namespace sch = Namespace.getNamespace("xsi",XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		root.addNamespaceDeclaration(sch);
		root.setAttribute("noNamespaceSchemaLocation",schemaPath,sch);
		// define output format
		Format format = Format.getPrettyFormat();
		format.setIndent("\t");
		format.setEncoding("UTF-8");
		
		// create outputter
		XMLOutputter outputter = new XMLOutputter(format);
		
		// write in the stream
	    outputter.output(document,outBuf);
	    
	    // close the stream
	    outBuf.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// CONTENT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the complete text contained directly or indirectly
	 * in the specified element. This means the result includes
	 * text containing by its descendants.
	 * 
	 * @param element
	 * 		Element whose containt must be processed.
	 * @return
	 * 		The concatenation of all text nodes under the specified element.
	 */
	public static String getRecText(Element element)
	{	String result = "";
		
		List<Content> children = element.getContent();
		for(Content child: children)
		{	if(child instanceof Text)
			{	Text text = (Text)child;
				result = result + text.getText();
			}
			else if(child instanceof Element)
			{	Element elt = (Element)child;
				String temp = getRecText(elt);
				result = result + temp;
			}
		}
		
		return result;
	}
}
