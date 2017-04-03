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
	/** Banque de noms de lieux du Québec */
	BANQ_LIEUX_QUEBEC,
	
	/** Database of the Bibliothèque nationale de France */
	BNF,
	
	/** British Museum place */
	BRIT_MUS_PLACE,
	
	/** National Library of Catalonia - BNC */
	CANTIC,
	
	/** Country calling code */
	COUNTRY_CC,
	
	/** Cultureel Woordenboek (Dutch ENcyclopedia) */
	CULT_WOORD,
	
	/** DBpedia database */
	DBPEDIA,
	
	/** Dewey Decimal Classification */
	DEWEY,
	
	/** Discogs artist ID */
	DISCOG_ARTIST,
	
	/** directory.mozilla.org */
	DMOZ,
	
	/** Encyclopædia Britannica Online ID */
	ENCYC_BRIT,
	
	/** ESPNcricinfo - Cricket players... */
	ESPN_CRICKET,
	
	/** Facebook database */
	FACEBOOK_PLACES,
	
	/** US country codes */
	FIPS_10_4,
	
	/** Freebase database */
	FREEBASE,
	
	/** GeoNames database */
	GEONAMES,
	
	/** Gran Enciclopèdia Catalana */
	GRAN_ENCIC_CATAL,
	
	/** Gemeinsame Normdatei - Integrated Authority File */ 
	GND,
	
	/** INSEE countries and foreign territories code */
	INSEE_COUNTRY,
	
	/** INSEE department code */
	INSEE_DEPT,
	
	/** INSEE municipality code */
	INSEE_MUNICIP,
	
	/** International Standard Name Identifier (subsumes ORCID) */
	ISNI,
	
	/** International Organization for Standardization - countries, alphabetic code */
	ISO_3166_1_ALPHA2,
	
	/** International Organization for Standardization - countries, alphabetic code */
	ISO_3166_1_ALPHA3,
	
	/** International Organization for Standardization - countries, numeric code */
	ISO_3166_1_NUM,
	
	/**  International Organization for Standardization - principal subdivisions of countries*/
	ISO_3166_2,
	
	/** Library of Congress */
	LIB_CONGR,
	
	/** Mobile country code */
	MOBILE_COUNTRY_CODE,
	
	/** Sujet du Monde diplomatique */
	MONDE_DIPLO,
	
	/** MusicBrainz artist ID */
	MUSIC_BRAINZ_ARTIST,
	
	/** National Library of Israel */
	NAT_LIB_ISRAEL,
	
	/** National Diet Library (Japan) */
	NAT_DIET_LIB,
	
	/** Nomenclature of Territorial Units for Statistics */
	NUTS_CODE,
	
	/** NYT topic */
	NYT,
	
	/** Postal code */
	POSTAL_CODE,
	
	/** Quora topic ID */
	QUORA_TOPIC,
	
	/** SUDOC database (Système Universitaire de Documentation - French national university library system) */
	SUDOC,
	
	/** Database from the French Senate website */
	SENAT_FR,
	
	/** Sycomore database (French MPs) */
	SYCOMORE,
	
	/** TED topic */
	TED_TOPIC,
	
	/** Virtual International Authority File */
	VIAF,
	
	/** WikiData database */
	WIKIDATA,
	
	/** Where On Earth IDentifier */
	WOEID;
}
