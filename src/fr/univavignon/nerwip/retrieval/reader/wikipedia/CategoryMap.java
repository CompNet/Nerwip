package fr.univavignon.nerwip.retrieval.reader.wikipedia;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Element;
import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.ArticleCategory;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.xml.XmlNames;
import fr.univavignon.nerwip.tools.xml.XmlTools;

/**
 * Allows converting some text to an {@link ArticleCategory}.
 * Used when retrieving the category from an external source.
 * The map itself is stored in a specific XML file.
 * 
 * @author Vincent Labatut
 */
public class CategoryMap
{	
	/**
	 * Builds a new map, using the specified 
	 * XML file.
	 * 
	 * @param fileName
	 * 		XML file to be loaded to populate this new map.
	 */
	public CategoryMap(String fileName)
	{	read(fileName);
	}
	
	/////////////////////////////////////////////////////////////////
	// DATA				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map associating a category to certain words */
	private final HashMap<String,ArticleCategory> map = new HashMap<String, ArticleCategory>();
	/** Expressions to be ignored while converting text to categories */
	private final Set<String> ignoreStartsWith = new TreeSet<String>();
	/** Expressions to be ignored while converting text to categories */
	private final Set<String> ignoreEndsWith = new TreeSet<String>();
	
	/**
	 * Returns the category associated to the
	 * specified string, or {@code null} if the
	 * string is not present in the map.
	 * 
	 * @param key
	 * 		String to be processed.
	 * @return
	 * 		Associated category.
	 */
	public ArticleCategory get(String key)
	{	ArticleCategory result = map.get(key);
		return result;
	}

	/**
	 * Checks if the appropriate "ignore" set
	 * contains prefix/postfix (depending on
	 * parameter {@code starts} of the specified
	 * string.
	 * 
	 * @param key
	 * 		String to be processed.
	 * @param starts
	 * 		Whether its start or end should be considered.
	 * @return
	 * 		{@code true} iff a corresponding pre/postfix is found
	 * 		in the predefined "ignore" sets.
	 */
	private boolean isIgnored(String key, boolean starts)
	{	boolean result = false;
		
		// get the right iterator
		Iterator<String> it;
		if(starts)
			it = ignoreStartsWith.iterator();
		else
			it = ignoreEndsWith.iterator();
		
		// check the set
		while(!result && it.hasNext())
		{	String temp = it.next();
			if(starts)
				result = key.startsWith(temp);
			else
				result = key.endsWith(temp);
		}
		
		return result;
	}

	/**
	 * Checks if the specified expression
	 * should be ignored, depending on
	 * how it starts/ends and the content of
	 * both predefined "ignore" sets.
	 * 
	 * @param key
	 * 		String to be analyzed. 
	 * @return
	 * 		{@çode true} iff a pre/postfix fitting
	 * 		the specified string could be found in the
	 * 		"ignore" sets.
	 */
	public boolean isIgnored(String key)
	{	boolean result = isIgnored(key,true);
		result = result || isIgnored(key,false);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Reads the specified XML file, and
	 * populate this map with its content.
	 * 
	 * @param fileName
	 * 		The XML file to read.
	 */
	private void read(String fileName)
	{	// schema file
		String schemaPath = FileNames.FO_SCHEMA + File.separator + FileNames.FI_CATMAP_SCHEMA;
		File schemaFile = new File(schemaPath);

		try
		{	// load file
			String fullName = FileNames.FO_RETRIEVAL + File.separator + fileName;
			File dataFile = new File(fullName);
			Element root = XmlTools.getRootFromFile(dataFile,schemaFile);
			
			// accept
			Element acceptElt = root.getChild(XmlNames.ELT_ACCEPT);
			if(acceptElt!=null)
				processAcceptElement(acceptElt);
			
			// reject
			Element rejectElt = root.getChild(XmlNames.ELT_REJECT);
			if(rejectElt!=null)
				processRejectElement(rejectElt);
		}
		catch (SAXException e)
		{	e.printStackTrace();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	/**
	 * Reads the categories associated with
	 * specific text expressions.
	 * 
	 * @param root
	 * 		Element containing the text/categories associations.
	 */
	private void processAcceptElement(Element root)
	{	List<Element> catElts = (List<Element>)root.getChildren(XmlNames.ELT_CATEGORY);
		for(Element catElt: catElts)
		{	String catStr = catElt.getAttributeValue(XmlNames.ATT_NAME);
			ArticleCategory category = null;
			if(catStr!=null)
				category = ArticleCategory.valueOf(catStr);
			processCategoryElement(category,catElt);
		}
	}
	
	/**
	 * Reads all the keys associated
	 * to the specified category,
	 * and add them to this map.
	 * 
	 * @param category
	 * 		Currently processed category. 
	 * @param root
	 * 		XML element containing the keys.
	 */
	private void processCategoryElement(ArticleCategory category, Element root)
	{	List<Element> keyElts = (List<Element>)root.getChildren(XmlNames.ELT_KEY);
		for(Element keyElt: keyElts)
		{	String key = keyElt.getValue();
			map.put(key,category);
		}
	}

	/**
	 * Reads the expressions one should ignore,
	 * either used to compare with the beginning
	 * or the end of the considered text.
	 * 
	 * @param root
	 * 		Element containing the text/categories associations.
	 */
	private void processRejectElement(Element root)
	{	// start expressions
		Element startsWithElt = root.getChild(XmlNames.ELT_STARTS_WITH);
		if(startsWithElt!=null)
			processXxxsWithElement(startsWithElt,true);
			
		// end expressions
		Element endsWithElt = root.getChild(XmlNames.ELT_ENDS_WITH);
		if(endsWithElt!=null)
			processXxxsWithElement(endsWithElt,false);
	}

	/**
	 * Reads all the keys to be ignored,
	 * adds them to the appropriate set.
	 * 
	 * @param root
	 * 		XML element containing the keys.
	 * @param starts
	 * 		Consider either the "start" or "end" set. 
	 */
	private void processXxxsWithElement(Element root, boolean starts)
	{	List<Element> keyElts = (List<Element>)root.getChildren(XmlNames.ELT_KEY);
		for(Element keyElt: keyElts)
		{	String key = keyElt.getValue();
			if(starts)
				ignoreStartsWith.add(key);
			else
				ignoreEndsWith.add(key);
		}
	}
}