package fr.univavignon.nerwip.data.entity;

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

/**
 * Abstract class representing a valued entity, i.e. an entity
 * associated to some specific numerical value.
 * 
 * @param <T>
 * 		Type of the entity value.
 *  
 * @author Vincent Labatut
 */
public abstract class AbstractValuedEntity<T extends Comparable<T>> extends AbstractEntity
{	
	/**
	 * General constructor for a valued entity.
	 * 
	 * @param value
	 * 		Numerical value associated to the entity to create.
	 * @param internalId
	 * 		Internal id of the entity to create.
	 */
	public AbstractValuedEntity(T value, long internalId)
	{	super(internalId);
		
		this.value = value;
	}
	
	/////////////////////////////////////////////////////////////////
	// VALUE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Value corresponding to this entity */
	protected T value = null;
	
	/**
	 * Returns the value of this entity.
	 * 
	 * @return
	 * 		Value of this entity. 
	 */
	public T getValue()
	{	return value;
	}

	/**
	 * Changes the value of this entity.
	 * 
	 * @param value
	 * 		New value of this entity.
	 */
	public void setValue(T value)
	{	this.value = value;
	}

	/////////////////////////////////////////////////////////////////
	// OBJECT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = getType().toString()+"(";
		result = result + "ID=" + internalId + "";
		result = result + ", VALUE=\"" + value + "\"";
		result = result + ")";
		return result;
	}
}
