package fr.univavignon.nerwip.data.entity;

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

import java.util.Set;
import java.util.TreeSet;

/**
 * Abstract class representing a named entity, i.e. an entity
 * that can appears under different names.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractNamedEntity extends AbstractEntity
{	
	/**
	 * General constructor for a named entity.
	 * 
	 * @param mainName
	 * 		Main string representation of the entity to create.
	 * @param internalId
	 * 		Internal id of the entity to create.
	 */
	public AbstractNamedEntity(String mainName, long internalId)
	{	super(internalId);
		
		this.mainName = mainName;
	}
	
	/////////////////////////////////////////////////////////////////
	// MAIN NAME		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Main string representation of this entity */
	protected String mainName = null;
	
	/**
	 * Returns the main string representation of this entity.
	 * 
	 * @return
	 * 		Original string representation of this entity. 
	 */
	public String getMainName()
	{	return mainName;
	}

	/**
	 * Changes the main string representation of this entity.
	 * 
	 * @param mainName
	 * 		New main string representation of this entity.
	 */
	public void setMainName(String mainName)
	{	this.mainName = mainName;
	}
	
	/////////////////////////////////////////////////////////////////
	// ALL NAMES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** All strings representing this entity */
	protected Set<String> allNames = new TreeSet<String>();
	
	/**
	 * Returns all the strings representing this entity.
	 * 
	 * @return
	 * 		Set of strings representing this entity. 
	 */
	public Set<String> getAllNames()
	{	return allNames;
	}

	/**
	 * Add one name to this entity.
	 * 
	 * @param name
	 * 		New name for this entity.
	 */
	public void addName(String name)
	{	allNames.add(name);
	}
	
	/**
	 * Removes one of the names of this entity.
	 * 
	 * @param name
	 * 		The name to remove.
	 */
	public void removeName(String name)
	{	allNames.remove(name);
	}
}
