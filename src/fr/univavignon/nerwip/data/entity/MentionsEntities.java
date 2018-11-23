package fr.univavignon.nerwip.data.entity;

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

import fr.univavignon.nerwip.data.entity.mention.Mentions;

/**
 * Class representing a couple of Entities and Mentions
 * objects, generated/updated by the same tool.
 * <br/>
 * This class is just a convenient way of returning both
 * types at once in resolvers and linkers. This was considered
 * a better practice than passing empty Entities and Mentions
 * objects. 
 * 
 * @author Vincent Labatut
 */
public class MentionsEntities
{	
	/**
	 * Builds an Entities object with current
	 * date and the reference source.
	 * 
	 * @param mentions
	 * 		Set of mentions. 
	 * @param entities 
	 * 		Set of entities. 
	 */
	public MentionsEntities(Mentions mentions, Entities entities)
	{	this.mentions = mentions;
		this.entities = entities;
	}
	
	/** Set of {@link Entities} */ 
	public Entities entities;
	/** Set of {@link Mentions} */ 
	public Mentions mentions;
}
