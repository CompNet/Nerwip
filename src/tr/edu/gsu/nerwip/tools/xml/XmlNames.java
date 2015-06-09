package tr.edu.gsu.nerwip.tools.xml;

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
 * This class contains XML-related strings.
 * 
 * @author Vincent Labatut
 */
public class XmlNames
{	
	/////////////////////////////////////////////////////////////////
	// CUSTOM ATTRIBUTES	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Represents... a date! */
	public static final String ATT_DATE = "date";
	/** Entity end position */
	public static final String ATT_END = "end";
	/** Id associated to some key */
	public static final String ATT_KEYID = "id";
	/** Some object name */
	public static final String ATT_NAME = "name";
	/** NER tool used to detect the entity */
	public static final String ATT_SOURCE = "source";
	/** Entity start position */
	public static final String ATT_START = "start";
	/** Entity type */
	public static final String ATT_TYPE = "type";
	/** Some value (generally associated to a name or key) */
	public static final String ATT_VALUE = "value";

	/////////////////////////////////////////////////////////////////
	// CUSTOM ELEMENTS		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Set of texts to be associated to categories */
	public static final String ELT_ACCEPT = "accept";
	/** Author of an article */
	public static final String ELT_AUTHOR = "author";
	/** List of article authors */
	public static final String ELT_AUTHORS = "authors";
	/** Category of article (military, scientist, etc.) */
	public static final String ELT_CATEGORY = "category";
	/** Dates associated to an article */
	public static final String ELT_DATES = "dates";
	/** Text expressions which must be ignored because of how they end, when retreiving categories */
	public static final String ELT_ENDS_WITH = "endsWith";
	/** A list of entities */
	public static final String ELT_ENTITIES = "entities";
	/** An entity in a list of entities */
	public static final String ELT_ENTITY = "entity";
	/** Some text key */
	public static final String ELT_KEY = "key";
	/** Language of an article */
	public static final String ELT_LANGUAGE = "language";
	/** Date of modification of an article */
	public static final String ELT_MODIFICATION_DATE = "modification";
	/** Date of publishing of an article */
	public static final String ELT_PUBLISHING_DATE = "publishing";
	/** Various properties of an article */
	public static final String ELT_PROPERTIES = "properties";
	/** Set of texts not to be associated to categories */
	public static final String ELT_REJECT = "reject";
	/** Date of retrieval of an article */
	public static final String ELT_RETRIEVAL_DATE = "retrieval";
	/** Text expressions which must be ignored because of how they start, when retreiving categories */
	public static final String ELT_STARTS_WITH = "startsWith";
	/** String describing an entity */
	public static final String ELT_STRING = "string";
	/** Title of an article */
	public static final String ELT_TITLE = "title";
	/** Address of an article */
	public static final String ELT_URL = "url";
	/** Actual value of an entity (might differ from its textual representation */
	public static final String ELT_VALUE = "value";
}
