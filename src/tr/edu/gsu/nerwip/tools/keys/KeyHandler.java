package tr.edu.gsu.nerwip.tools.keys;

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
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.xml.XmlNames;
import tr.edu.gsu.nerwip.tools.xml.XmlTools;

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
	// KEYS			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the keys */
	public static final Map<String,String> KEYS = loadKeys();

	/////////////////////////////////////////////////////////////////
	// LOADING		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Loads the list of keys as set by the user in
	 * the appropriate XML file.
	 * 
	 * @return
	 * 		A map containing each named key.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String,String> loadKeys()
	{	// set up file names
		String dataFileName = FileNames.FO_MISC + File.separator + FileNames.FI_KEY_LIST;
		File dataFile = new File(dataFileName);
		String schemaFileName = FileNames.FO_SCHEMA + File.separator + FileNames.FI_KEY_SCHEMA;
		File schemaFile = new File(schemaFileName);
		
		Map<String, String> result = new HashMap<String, String>();
		try
		{	// load XML file
			Element keysElt = XmlTools.getRootFromFile(dataFile, schemaFile);
			
			// populate map
			List<Element> keyElts = keysElt.getChildren(XmlNames.ELT_KEY);
			for(Element keyElt: keyElts)
			{	String name = keyElt.getAttributeValue(XmlNames.ATT_NAME);
				String value = keyElt.getAttributeValue(XmlNames.ATT_VALUE);
				// ignore empty keys or names
				if(!name.isEmpty() && !value.isEmpty())
					result.put(name, value);
			}
		}
		catch (SAXException | IOException e)
		{	e.printStackTrace();
		}
		
		return result;
	}
}
