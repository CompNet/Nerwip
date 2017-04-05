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
	
	/** AllMovie artist */
	ALL_MOVIE_ARTIST,
	
	/** AllMusic artist */
	ALL_MUSIC_ARTIST,
	
	/** AlloCiné person */
	ALLO_CINE_PERS,
	
	/** Archives du Spectacle */
	ARCH_SPECT,
	
	/** Banque de noms de lieux du Québec */
	BANQ_LIEUX_QUEBEC,
	
	/** Belgian ODIS database */
	BE_ODIS,
	
	/** BiblioNet author */
	BIBLIONET_AUTHOR,
	
	/** British Museum person-institution */
	BRIT_MUS_PERSINST,
	/** British Museum place */
	BRIT_MUS_PLACE,
	/** British Museum thesaurus */
	BRIT_MUS_THES,
	
	/** Biblioteca Virtual Miguel de Cervantes */
	BVMC_PERS,
	
	/** National Library of Catalonia - BNC */
	CANTIC,
	
	/** CBS municipality code */
	CBS_MUNI,
	
	/** Consortium of European Research Libraries thesaurus */
	CERL,
	
	/** Chemins de mémoire - Locations */
	CHEM_MEM_LOC,
	
	/** Author in CiNii - Scholarly and Academic Information Navigator */
	CINII,
	
	/** Cultural Objects Name Authority */
	CONA,
	
	/** CONOR.SI database */
	CONOR_SI,
	
	/** Country calling code */
	COUNTRY_CC,
	
	/** Czech film database ČSFD */
	CSFD,
	
	/** Annuaire de la France savante XVIIe-XXe du CTHS */
	CTHS_PERS,
	
	/** Cultureel Woordenboek (Dutch ENcyclopedia) */
	CULT_WOORD,
	
	/** data.gouv.fr */
	DATA_GOUV_FR,
	
	/** DBNL-website for Dutch language authors */
	DBNL,
	
	/** DBpedia database */
	DBPEDIA,
	
	/** Dewey Decimal Classification */
	DEWEY,
	
	/** Dictionary of Art Historians */
	DICO_ART_HIST,
	
	/** Discogs artist ID */
	DISCOG_ARTIST,
	
	/** directory.mozilla.org */
	DMOZ,
	
	/** Person in the Danish National Filmography */
	DNF,
	
	/** Digital Object Identifier */
	DOI,
	
	/** Directory of sources of Landesarchiv Liechtenstein */
	EARCHIV_LI,
	
	/** Bibliotheca Alexandrina */
	EGAXA,
	
	/** Elonet person ID */
	ELONET_PERS,
	
	/** Encyclopædia Britannica Online ID */
	ENCYC_BRIT,
	
	/** Public libraries maintained by the Spanish Ministry of Education, Culture and Sport */
	ES_LEM,
	
	/** ESPNcricinfo - Cricket players... */
	ESPN_CRICKET,
	
	/** EU transparency register */
	EU_TRANS_REG,
	
	/** Facebook database */
	FACEBOOK_PLACES,
	
	/** Facebook ID */
	FACEBOOK_USER,
	
	/** WorldCat's FAST Linked Data */
	FAST_ID,
	
	/** Finnish municipality number */
	FI_MUNI,
	
	/** identifier of the German Filmportal.de */
	FILM_PORT,
	
	/** US country codes */
	FIPS_10_4,
	/** FIPS 55-3 (locations in the US) */
	FIPS_55_3,
	
	/** FootballDatabase.eu ID */
	FOOT_DB,
	
	/** Monument in the Mérimée database of French cultural heritage */
	FR_MERIMEE,
	
	/** Archives Nationales (French National Archives) - Producer record */
	FR_NAT_ARCHIV_PROD,
	
	/** French National Assembly Lobbyist */
	FR_NAT_ASS_LOBBY,
	
	/** French diocesan architects */
	FR_DIOC_ARCHI,
	
	/** Freebase database */
	FREEBASE,
	
	/** Global Anabaptist Mennonite Encyclopedia Online */
	GAMEO,
	
	/** genealogics.org person */
	GENEA_ORG_PERS,
	
	/** GeoNames database */
	GEONAMES,
	
	/** German municipality key */
	GER_MUNI_KEY,
	
	/** German regional key */
	GER_REGI_KEY,
	
	/** Getty Thesaurus of Geographic Names */
	GETTY_GEO,
	
	/** Gran Enciclopèdia Catalana */
	GRAN_ENCIC_CATAL,
	
	/** Gemeinsame Normdatei - Integrated Authority File */ 
	GND,
	
	/** Geographic Names Information System */
	GNIS,
	
	/** Great Aragonese Encyclopedia */
	GREAT_ARAG_ENCYC,
	
	/** Great Russian Encyclopedia Online */
	GREAT_RUSS_ENCYC,
	
	/** Institutional identifier from the GRID.ac global research identifier database */
	GRID_AC,
	
	/** Common Thesaurus of Audiovisual Archives (Dutch) */
	GTAA,
	
	/** Guardian topic */
	GUARDIAN_TOPIC,
	
	/** HDS/HLS/DHS/DSS: Historical Dictionary of Switzerland */
	HDS,
	
	/** IMDb - Internet Movie Database */
	IMDB,
	
	/** INSEE countries and foreign territories code */
	INSEE_COUNTRY,
	/** INSEE department code */
	INSEE_DEPT,
	/** INSEE municipality code */
	INSEE_MUNICIP,
	
	/** International Securities Identification Number */
	ISIN,
	
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
	/**  International Organization for Standardization - country name that has been deleted from ISO 3166-1 since its first publication in 1974 */
	ISO_3166_3,
	
	/** Person, in the Kinopoisk.ru database */
	KINOPOISK_PERS,
	
	/** Komponisten der Gegenwart -- Munzinger Archiv */
	KOMP_GEG,
	/** Komponisten der Gegenwart -- Munzinger Archiv */
	KOMP_GEG_IBA,
	
	/** Legal Entity */
	LEGAL_ENT,
	
	/** Base Léonore des membres de l'ordre de la Légion d'honneur */
	LEONORE,
	
	/** Librivox author */
	LIBRIVOX_AUTH,
	
	/** Library of Congress */
	LIB_CONGR,
	
	/** Mobile country code */
	MOBILE_COUNTRY_CODE,
	
	/** Sujet du Monde diplomatique */
	MONDE_DIPLO,
	
	/** MovieMeter director */
	MOVIE_METER_DIR,
	
	/** Musée d'Orsay artist */
	MUSEE_ORSAY_ART,
	
	/** MusicBrainz artist ID */
	MUSIC_BRAINZ_ARTIST,
	
	/** MySpace ID */
	MYSPACE_USER,
	
	/** National Diet Library (Japan) */
	NAT_DIET_LIB,
	
	/** National-Football-Teams.com player ID */
	NAT_FOOT_PLR,
	
	/** NLA - National Library of Australia */
	NAT_LIB_AU,
	/** Biblioteca Nacional de España (BNE) */
	NAT_LIB_ES,
	/** Database of the Bibliothèque nationale de France (BNF) */
	NAT_LIB_FR,
	/** National Library of Greece */
	NAT_LIB_GR,
	/** National Library of Ireland */
	NAT_LIB_IE,
	/** National Library of Israel */
	NAT_LIB_IL,
	/** National Library Service (SBN) of Italy */
	NAT_LIB_IT,
	/** BIBSYS - Bibliothèque nationale de Norvège */
	NAT_LIB_NO,
	/** National Library of Poland */
	NAT_LIB_PL,
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
	
	/** Nobel prize */
	NOBEL_PRIZE,
	
	/** NUKAT Center of the University of Warsaw Library */
	NUKAT,
	
	/** Nomenclature of Territorial Units for Statistics */
	NUTS_CODE,
	
	/** NYT topic */
	NYT,
	
	/** Classification on Objects territory of municipal formations (Russia) */
	OKTMO,
	
	/** OpenCorporates */
	OPEN_CORP,
	
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
	
	/** Patrimonio Inmueble de Andalucía */
	PATR_INM_AND,
	
	/** People Australia */
	PEOPLE_AUSTR,
	
	/** Perlentaucher -- German online magazine */
	PERLENTAUCHER,
	
	/** Persée, an open access digital library of French-language scholarly journals */
	PERSEE,
	
	/** Localisation géographique historique - identifiant de la base Pleiades.stoa.org */
	PLEIADES,
	
	/** PORT-network film database: identifier for a person */
	PORT_PERS,
	
	/** Project Gutenberg author */
	PROJ_GUT_AUTHOR,
	
	/** Portuguese National Library */
	PTBNP,
	
	/** Postal code */
	POSTAL_CODE,
	
	/** Quora topic ID */
	QUORA_TOPIC,
	
	/** RKDartists */
	RKD_ART,
	
	/** Unique identifier for organisations in the publishing industry supply chain */
	RINGGOLD,
	
	/** Web-based edition of Joachim von Sandrart’s "Teutscher Academie der Edlen Bau, Bild- und Mahlerey-Künste" */
	SANDRART_PER,
	
	/** National Library of Sweden Libris library catalog */
	SELIBR,
	
	/** Database from the French Senate website */
	SENAT_FR,
	
	/** Swedish Film Database (SFDb) */
	SFDB,
	
	/** Stanford Encyclopedia of Philosophy */
	STAN_ENC_PHIL,
	
	/** SUDOC database (Système Universitaire de Documentation - French national university library system) */
	SUDOC,
	
	/** Identifier for a building in the Structurae database */
	STRUCTURAE,
	
	/** Swiss municipality */
	SWISS_MUNI,
	
	/** Sycomore database (French MPs) */
	SYCOMORE,
	
	/** TED topic */
	TED_TOPIC,
	
	/** Teuchos */
	TEUCHOS,
	
	/** TheFinalBall player ID */
	TFB_PLYR,
	
	/** Enciclopedia Treccani */
	TRECCANI,
	
	/** Place (region, hotel, restaurant, attraction), in TripAdvisor */
	TRIPADVISOR_PLACE,
	
	/** Trismegistos Geo ID */
	TRISMEGISTOS_GEO,
	
	/** Twitter username */
	TWITTER_USER,
	
	/** Getty Union List of Artist Names */
	ULAN,
	
	/** United Nations M.49 code */
	UN_M49,
	
	/** Country code by the United Nations Development Programme */
	UNDP_COUNTRY,
	
	/** Geographic location code mantained by UN-ECE */
	UNECE_LOCODE,
	
	/** Virtual International Authority File */
	VIAF,
	
	/** WikiData database */
	WIKI_DATA,
	/** WikiTree genealogy website */
	WIKI_TREE,
	
	/** Where On Earth IDentifier */
	WOEID,
	
	/** Worldfootball.net ID */
	WORLD_FOOT,
	
	/** World Heritage criteria */
	WORLD_HER_CRIT,
	
	/** World Heritage Site */
	WORLD_HER_SITE,
	
	/** YouTube channel */
	YOUTUBE_USER;
	
}
