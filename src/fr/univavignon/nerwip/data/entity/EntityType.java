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

/**
 * Type of the entities and mentions. 
 * The value {@code null} can be used to represent an unknown type.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 * @author Sabrine Ayachi
 */
public enum EntityType
{	
	/** A Date or any temporal entity (Christmas...) */
	DATE,
	
	/** The role associated to a title: general, president, king, pope... */
	FUNCTION,
	
	/** The name of a geographical place, artificial place, etc. */
	LOCATION,
	
	/** Any congress, conference... */
	MEETING,
	
	/** Any organization name: institution, company, assocation... */
	ORGANIZATION,
	
	/** Any real or fiction person or animal, or any group of those */
	PERSON,
	
	/** A human production: works, buildings, awards, etc. */
	PRODUCTION; 
}
