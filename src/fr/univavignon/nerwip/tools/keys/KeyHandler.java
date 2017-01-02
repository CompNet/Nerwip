package fr.univavignon.nerwip.tools.keys;

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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.xml.sax.SAXException;

import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.xml.XmlNames;
import fr.univavignon.nerwip.tools.xml.XmlTools;

/**
 * This class handles the keys associated to the
 * access to certain services such as Freebase
 * or OpenCalais.
 *  
 * @author Vincent Labatut
 */
public class KeyHandler
{	
	/////////////////////////////////////////////////////////////////
	// DATA			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the keys */
	public static final Map<String,String> KEYS = new HashMap<String, String>();
	/** Map containing the ids associated to certain keys */
	public static final Map<String,String> IDS = new HashMap<String, String>();
	
	/** Loads the keys and ids */
	static
	{	loadData();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOADING		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Loads the list of keys as set by the user in
	 * the appropriate XML file, as well as the
	 * ids possibly associated to certain keys.
	 */
	private static void loadData()
	{	// set up file names
		String dataFileName = FileNames.FO_MISC + File.separator + FileNames.FI_KEY_LIST;
		File dataFile = new File(dataFileName);
		String schemaFileName = FileNames.FO_SCHEMA + File.separator + FileNames.FI_KEY_SCHEMA;
		File schemaFile = new File(schemaFileName);
		
		try
		{	// load XML file
			Element keysElt = XmlTools.getRootFromFile(dataFile, schemaFile);
			
			// populate map
			List<Element> keyElts = keysElt.getChildren(XmlNames.ELT_KEY);
			for(Element keyElt: keyElts)
			{	String name = keyElt.getAttributeValue(XmlNames.ATT_NAME);
				String value = keyElt.getAttributeValue(XmlNames.ATT_VALUE);
				String id = keyElt.getAttributeValue(XmlNames.ATT_KEYID);
				
				// ignore empty keys or names
				if(!name.isEmpty() && !value.isEmpty())
				{	KEYS.put(name, value);
					if(id!=null)
						IDS.put(name, id);
				}
			}
//			System.out.println("Test");
		}
		catch (SAXException | IOException e)
		{	e.printStackTrace();
		}
	}
}
