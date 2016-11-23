package fr.univavignon.nerwip.edition.language;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Represents all the GUI text for a given language.
 * 
 * @author Vincent Labatut
 */
public class Language
{		
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Language name */
	private String name;
	
	/**
	 * Change the language name.
	 * 
	 * @param name
	 * 		New language name.
	 */
	public void setName(String name)
	{	this.name = name;	
	}
	
	/**
	 * Returns the language name.
	 * 
	 * @return
	 * 		Current language name.
	 */
	public String getName()
	{	return name;
	}
	
	/////////////////////////////////////////////////////////////////
	// TEXT				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Texts associated to GUI components */
	private final HashMap<String, String> texts = new HashMap<String,String>();
	/** Tooltips associated to GUI components */
	private final HashMap<String, String> tooltips = new HashMap<String,String>();

	/**
	 * Adds a new text to this object.
	 * 
	 * @param key
	 * 		Key associated to the text, used to retrieve the text later.
	 * @param value
	 * 		Text itself.
	 * @param tooltip
	 * 		Tooltip associated to the same key.
	 */
	public void addText(String key, String value, String tooltip)
	{	texts.put(key, value);
		if(tooltip!=null)
			tooltips.put(key, tooltip);
	}
	
	/**
	 * Returns the text associated to the specified key.
	 * 
	 * @param key
	 * 		Key of the text.
	 * @return
	 * 		Text associated to that key.
	 */
	public String getText(String key)
	{	String result = texts.get(key);
if(result==null)
	System.err.println(key);
		if(result==null)
			result = key+":N/A";
		return result;
	}
	
	/**
	 * Returns the tooltip associated to the specified key.
	 * 
	 * @param key
	 * 		Key of the tooltip.
	 * @return
	 * 		Tooltip associated to that key.
	 */
	public String getTooltip(String key)
	{	String result = tooltips.get(key);
if(result==null)
	System.err.println(key);
		return result;
	}
	
	/**
	 * Returns the map containing all texts.
	 * 
	 * @return
	 * 		A maps of texts.
	 */
	public HashMap<String, String> getTexts()
	{	return texts;
	}
	
	/**
	 * Returns the map containing all tooltips.
	 * 
	 * @return
	 * 		A maps of tooltips.
	 */
	public HashMap<String, String> getTooltips()
	{	return tooltips;
	}
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Makes a full copy of this object.
	 *  
	 * @return
	 * 		A clone of this object.
	 */
	public Language copy()
	{	Language result = new Language();
		result.setName(name);
		Iterator<Entry<String,String>> it = texts.entrySet().iterator();
		while(it.hasNext())
		{	Entry<String,String> txt = it.next();
			Entry<String,String> tt = it.next();
			String key = txt.getKey();
			String value = txt.getValue();
			String tooltip = tt.getValue();
			result.addText(key,value,tooltip); 
		}
		return result;
	}
}
