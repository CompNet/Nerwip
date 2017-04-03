package fr.univavignon.nerwip.data.entity;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
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
 * Knowledge based used for linking (i.e. associating an URI or
 * any other unique id to an entity).
 * 
 * @author Vincent Labatut
 */
public enum KnowledgeBase
{	
	/** Database of the Bibliothèque nationale de France */
	BNF,

	/** DBpedia database */
	DBPEDIA,
	
	/** Dewey Decimal Classification */
	DEWEY,
	
	/** Encyclopædia Britannica Online ID */
	ENCYC_BRIT,
	
	/** Facebook database */
	FACEBOOK_PLACES,
	
	/** Freebase database */
	FREEBASE,
	
	/** GeoNames database */
	GEONAMES,
	
	/** Gran Enciclopèdia Catalana */
	GRAN_ENCIC_CATAL,
	
	/** Gemeinsame Normdatei - Integrated Authority File */ 
	GND,
	
	/** INSEE municipality code */
	INSEE_MUNICIP,
	
	/** International Standard Name Identifier (subsumes ORCID) */
	ISNI,
	
	/** Library of Congress */
	LIB_CONGR,
	
	/** Postal code */
	POSTAL_CODE,
	
	/** SUDOC database (Système Universitaire de Documentation - French national university library system) */
	SUDOC,
	
	/** Database from the French Senate website */
	SENAT_FR,
	
	/** Sycomore database (French MPs) */
	SYCOMORE,
	
	/** Virtual International Authority File */
	VIAF,
	
	/** WikiData database */
	WIKIDATA;
}
