package fr.univavignon.edition.language;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.xml.sax.SAXException;

import fr.univavignon.edition.tools.EditorFileNames;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.xml.XmlTools;

/**
 * Class used to load the language-dependent text
 * put in the GUI components.
 * 
 * @author Vincent Labatut
 */
public class LanguageLoader
{	
	/** XML schema file used to store GUI texts */
	private final static String FI_LANGUAGE = "language" + FileNames.EX_SCHEMA;
	
	/** Code of some textual GUI component or group */
	private final static String ATT_NAME = "name";
	/** Text to be displayed in some textual GUI component */
	private final static String ATT_VALUE = "value";
	/** Tooltip associated to some GUI component */
	private final static String ATT_TOOLTIP = "tooltip";
	/** A group of GUI texts */
	private final static String ELT_GROUP = "group";
	/** Text properties in the editor */
	private final static String ELT_TEXT = "text";
	
	/**
	 * Main method to load a language XML file.
	 * 
	 * @param name
	 * 		Name of the language to load.
	 * @return
	 * 		{@code Language} object resulting from the loading.
	 * 
	 * @throws ParserConfigurationException
	 * 		Problem while accessing the XML file or its schema.
	 * @throws SAXException
	 * 		Problem while accessing the XML file or its schema.
	 * @throws IOException
	 * 		Problem while accessing the XML file or its schema.
	 */
	public static Language loadLanguage(String name) throws ParserConfigurationException, SAXException, IOException
	{	// set up data file
		String dataPath = EditorFileNames.FO_LANGUAGE + File.separator + name.toLowerCase() + FileNames.EX_XML;
		File dataFile = new File(dataPath);
		
		// set up schema file
		String schemaPath = FileNames.FO_SCHEMA + File.separator + FI_LANGUAGE;
		File schemaFile = new File(schemaPath);
		
		// load XML file
		Element root = XmlTools.getRootFromFile(dataFile,schemaFile);
		
		// build result
		Language result = new Language();
		loadLanguageElement(root,result);
		result.setName(name);
		return result;
	}
	
	/**
	 * Process a language element.
	 * 
	 * @param root
	 * 		The language element to process.
	 * @param result
	 * 		The resulting object which is updated.
	 */
	private static void loadLanguageElement(Element root, Language result)
	{	List<Element> elements = root.getChildren(ELT_GROUP);
		Iterator<Element> i = elements.iterator();
		while(i.hasNext())
		{	Element temp = i.next();
			loadGroupElement(temp,"",result);
		}
	}
	
	/**
	 * Process a group element.
	 * 
	 * @param root
	 * 		The group element to process.
	 * @param name
	 * 		Name of the current group.
	 * @param result
	 * 		The resulting object which is updated.
	 */
	private static void loadGroupElement(Element root, String name, Language result)
	{	String key = root.getAttribute(ATT_NAME).getValue().trim();
		String newName = name+key;
		// text
		{	List<Element> elements = root.getChildren(ELT_TEXT);
			Iterator<Element> i = elements.iterator();
			while(i.hasNext())
			{	Element temp = i.next();
				loadTextElement(temp,newName,result);
			}
		}
		// other groups
		{	List<Element> elements = root.getChildren(ELT_GROUP);
			Iterator<Element> i = elements.iterator();
			while(i.hasNext())
			{	Element temp = i.next();
				loadGroupElement(temp,newName,result);
			}
		}
	}
	
	/**
	 * Process a text element.
	 * 
	 * @param root
	 * 		The text element to process.
	 * @param name
	 * 		Name of the current group.
	 * @param result
	 * 		The resulting object which is updated.
	 */
	private static void loadTextElement(Element root, String name, Language result) 
	{	// name
		String key = root.getAttribute(ATT_NAME).getValue().trim();
		String newName = name+key;
		
		// text
		String text = root.getAttribute(ATT_VALUE).getValue().trim();
		
		//  tooltip
		String tooltip = null;
		Attribute att = root.getAttribute(ATT_TOOLTIP);
		if(att!=null)
			tooltip = att.getValue().trim();

		// insert
		result.addText(newName,text,tooltip);
	}
}