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
	/** Identifiant de la personne ou de l'organisme dans la base AGORHA de l'INHA */
	AGORHA,
	
	/** Archives du Spectacle */
	ARCH_SPECT,
	
	/** Banque de noms de lieux du Québec */
	BANQ_LIEUX_QUEBEC,
	
	/** BiblioNet author */
	BIBLIONET_AUTHOR,
	
	/** British Museum person-institution */
	BRIT_MUS_PERSINST,
	/** British Museum place */
	BRIT_MUS_PLACE,
	
	/** Biblioteca Virtual Miguel de Cervantes */
	BVMC_PERS,
	
	/** National Library of Catalonia - BNC */
	CANTIC,
	
	/** Consortium of European Research Libraries thesaurus */
	CERL,
	
	/** CONOR.SI database */
	CONOR_SI,
	
	/** Country calling code */
	COUNTRY_CC,
	
	/** Annuaire de la France savante XVIIe-XXe du CTHS */
	CTHS_PERS,
	
	/** Cultureel Woordenboek (Dutch ENcyclopedia) */
	CULT_WOORD,
	
	/** DBNL-website for Dutch language authors */
	DBNL,
	
	/** DBpedia database */
	DBPEDIA,
	
	/** Dewey Decimal Classification */
	DEWEY,
	
	/** Discogs artist ID */
	DISCOG_ARTIST,
	
	/** directory.mozilla.org */
	DMOZ,
	
	/** Directory of sources of Landesarchiv Liechtenstein */
	EARCHIV_LI,
	
	/** Bibliotheca Alexandrina */
	EGAXA,
	
	/** Encyclopædia Britannica Online ID */
	ENCYC_BRIT,
	
	/** ESPNcricinfo - Cricket players... */
	ESPN_CRICKET,
	
	/** Facebook database */
	FACEBOOK_PLACES,
	
	/** WorldCat's FAST Linked Data */
	FAST_ID,
	
	/** identifier of the German Filmportal.de */
	FILM_PORT,
	
	/** US country codes */
	FIPS_10_4,
	
	/** Freebase database */
	FREEBASE,
	
	/** genealogics.org person */
	GENEA_ORG_PERS,
	
	/** GeoNames database */
	GEONAMES,
	
	/** Gran Enciclopèdia Catalana */
	GRAN_ENCIC_CATAL,
	
	/** Gemeinsame Normdatei - Integrated Authority File */ 
	GND,
	
	/** Common Thesaurus of Audiovisual Archives (Dutch) */
	GTAA,
	
	/** IMDb - Internet Movie Database */
	IMDB,
	
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
	
	/** Komponisten der Gegenwart -- Munzinger Archiv */
	KOMP_GEG,
	/** Komponisten der Gegenwart -- Munzinger Archiv */
	KOMP_GEG_IBA,
	
	/** Librivox author */
	LIBRIVOX_AUTH,
	
	/** Library of Congress */
	LIB_CONGR,
	
	/** Mobile country code */
	MOBILE_COUNTRY_CODE,
	
	/** Sujet du Monde diplomatique */
	MONDE_DIPLO,
	
	/** MusicBrainz artist ID */
	MUSIC_BRAINZ_ARTIST,
	
	/** National Diet Library (Japan) */
	NAT_DIET_LIB,
	
	/** NLA - National Library of Australia */
	NAT_LIB_AU,
	/** Biblioteca Nacional de España (BNE) */
	NAT_LIB_ES,
	/** Database of the Bibliothèque nationale de France (BNF) */
	NAT_LIB_FR,
	/** National Library of Greece */
	NAT_LIB_GR,
	/** National Library of Israel */
	NAT_LIB_IL,
	/** National Library Service (SBN) of Italy */
	NAT_LIB_IT,
	/** BIBSYS - Bibliothèque nationale de Norvège */
	NAT_LIB_NO,
	/** Vatican Library */
	NAT_LIB_VA,
	
	/** National Portrait Gallery (London) person ID */
	NAT_PORT_GALL_PERS,
	
	/** National Thesaurus for Author Names */
	NAT_THES_AUTH,
	
	/** Czech National Authority Database - National Library of Czech Republic */
	NKCR_AUT,
	
	/** Notable Names Database */
	NNDB,
	
	/** NUKAT Center of the University of Warsaw Library */
	NUKAT,
	
	/** Nomenclature of Territorial Units for Statistics */
	NUTS_CODE,
	
	/** NYT topic */
	NYT,
	
	/** Open Library */
	OPEN_LIB,
	
	/** Author in the openMLOL digital library of cultural resources */
	OPEN_MLOL,
	
	/** OpenPlaques subject */
	OPEN_PLAQUE,
	
	/** Oxford Biography Index */
	OX_BIO_IDX,
	
	/** Member of the Parliamentary Assembly of the Council of Europe */
	PACE,
	
	/** People Australia */
	PEOPLE_AUSTR,
	
	/** Perlentaucher -- German online magazine */
	PERLENTAUCHER,
	
	/** Persée, an open access digital library of French-language scholarly journals */
	PERSEE,
	
	/** Project Gutenberg author */
	PROJ_GUT_AUTHOR,
	
	/** Portuguese National Library */
	PTBNP,
	
	/** Postal code */
	POSTAL_CODE,
	
	/** Quora topic ID */
	QUORA_TOPIC,
	
	/** National Library of Sweden Libris library catalog */
	SELIBR,
	
	/** Stanford Encyclopedia of Philosophy */
	STAN_ENC_PHIL,
	
	/** SUDOC database (Système Universitaire de Documentation - French national university library system) */
	SUDOC,
	
	/** Database from the French Senate website */
	SENAT_FR,
	
	/** Sycomore database (French MPs) */
	SYCOMORE,
	
	/** TED topic */
	TED_TOPIC,
	
	/** Getty Union List of Artist Names */
	ULAN,
	
	/** Virtual International Authority File */
	VIAF,
	
	/** WikiData database */
	WIKI_DATA,
	/** WikiTree genealogy website */
	WIKI_TREE,
	
	/** Where On Earth IDentifier */
	WOEID;
}
