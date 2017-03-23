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
	
	/** Freebase database */
	FREEBASE,
	
	/** International Standard Name Identifier (subsumes ORCID) */
	ISNI,
	
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
