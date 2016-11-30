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
 * Class representing a function entity, which is a kind of named entity.
 * 
 * @author Vincent Labatut
 */
public class EntityFunction extends AbstractNamedEntity
{	
	/**
	 * Constructs a function entity.
	 * 
	 * @param mainName
	 * 		Main string representation of the entity to create.
	 * @param internalId
	 * 		Internal id of the entity to create.
	 */
	public EntityFunction(String mainName, long internalId)
	{	super(mainName,internalId);
	}
	
	/////////////////////////////////////////////////////////////////
	// TYPE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public EntityType getType()
	{	return EntityType.FUNCTION;
	}
}
