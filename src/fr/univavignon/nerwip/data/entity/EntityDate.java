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

import fr.univavignon.nerwip.tools.time.Date;

/**
 * Class representing a date entity, which is a kind of valued entity.
 * 
 * @author Vincent Labatut
 */
public abstract class EntityDate extends AbstractValuedEntity<Date>
{	
	/**
	 * Constructs a date entity.
	 * 
	 * @param value
	 * 		Date of the entity to create.
	 * @param internalId
	 * 		Internal id of the entity to create.
	 */
	public EntityDate(Date value, long internalId)
	{	super(value,internalId);
	}
	
	/////////////////////////////////////////////////////////////////
	// OBJECT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = "DATE(";
		result = result + "VALUE=\"" + value+"\"";
		result = result + ")";
		return result;
	}
}
