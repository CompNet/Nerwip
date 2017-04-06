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
	
	/** Artsy artist */
	ARTSY_ARTIST,
	
	/** Australian Dictionary of Biography */
	AU_DICT_BIO,
	
	/** Auckland Art Gallery artist */
	AUCK_ART_GALL_ART,
	
	/** Babelio author */
	BABELIO_AUTH,
	
	/** BanQ author */
	BAN_Q_AUTH,
	
	/** Banque de noms de lieux du Québec */
	BANQ_LIEUX_QUEBEC,
	
	/** Bayerisches Musiker-Lexikon Online */
	BAYER_MUSIK_LEX,
	
	/** BBC Things */
	BBC_THINGS,
	
	/** Place code used by Belgium Statistics edit */
	BE_NIS_INS,
	
	/** Belgian ODIS database */
	BE_ODIS,
	
	/** Belvedere artist */
	BELVEDERE_ART,
	
	/** Benezit Dictionary of Artists */
	BENEZIT,
	
	/** BiblioNet author */
	BIBLIONET_AUTHOR,
	
	/** Brazilian municipality code */
	BR_MUNI,
	
	/** British Museum person-institution */
	BRIT_MUS_PERSINST,
	/** British Museum place */
	BRIT_MUS_PLACE,
	/** British Museum thesaurus */
	BRIT_MUS_THES,
	
	/** Bureau du patrimoine de Seine-Saint-Denis */
	BUR_PAT_SSD,
	
	/** Biblioteca Virtual Miguel de Cervantes */
	BVMC_PERS,
	
	/** CALIS (China Academic Library & Information System) edit */
	CALIS,
	
	/** National Library of Catalonia - BNC */
	CANTIC,
	
	/** Catholic Encyclopedia */
	CATHO_ENCYC,
	
	/** CBS municipality code */
	CBS_MUNI,
	
	/** Consortium of European Research Libraries thesaurus */
	CERL,
	
	/** Person in the CESAR database of French theatre of the seventeenth and eighteenth centuries */
	CESAR_PERS,
	
	/** Identifier for cultural properties in Switzerland */
	CH_PCP,
	
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
	
	/** C-SPAN person */
	CSPAN_PERS,
	
	/** Annuaire de la France savante XVIIe-XXe du CTHS */
	CTHS_PERS,
	
	/** Cultureel Woordenboek (Dutch ENcyclopedia) */
	CULT_WOORD,
	
	/** Cycling Archives ID (cyclist) */
	CYCL_ARCHIV,
	
	/** data.gouv.fr */
	DATA_GOUV_FR,
	
	/** DBNL-website for Dutch language authors */
	DBNL,
	
	/** DBpedia database */
	DBPEDIA,
	
	/** Dewey Decimal Classification */
	DEWEY,
	
	/** Dialnet author */
	DIALNET_AUTH,
	
	/** Dictionary of Art Historians */
	DICO_ART_HIST,
	
	/** Discogs artist ID */
	DISCOG_ARTIST,
	
	/** Dizionario Biografico degli Italiani */
	DIZIO_BIO_IT,
	
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
	
	/** elCinema person */
	EL_CINE_PERS,
	
	/** Elonet person ID */
	ELONET_PERS,
	
	/** ELSTAT geographical code */
	ELSTAT,
	
	/** EMLO - Early Modern Letters Online project run by the Bodleian Library */
	EMLO,
	
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
	
	/** French Sculpture Census artist ID */
	FR_SCULPT_ART,
	
	/** Freebase database */
	FREEBASE,
	
	/** Global Anabaptist Mennonite Encyclopedia Online */
	GAMEO,
	
	/** genealogics.org person */
	GENEA_ORG_PERS,
	
	/** Geni.com profile */
	GENI_COM_USER,
	
	/** GeoNames database */
	GEONAMES,
	
	/** German municipality key */
	GER_MUNI_KEY,
	
	/** German regional key */
	GER_REGI_KEY,
	
	/** Getty - Art & Architecture Thesaurus by the Getty Research Institute */
	GETTY_AAT,
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
	
	/** GS1 Prefix, the first three digits, usually identifying the national GS1 Member Organization to which the manufacturer is registered */
	GS1_COUNTRY,
	
	/** Nine-character UK Government Statistical Service code */
	GSS,
	
	/** Common Thesaurus of Audiovisual Archives (Dutch) */
	GTAA,
	
	/** Guardian topic */
	GUARDIAN_TOPIC,
	
	/** HDS/HLS/DHS/DSS: Historical Dictionary of Switzerland */
	HDS,
	
	/** IATA airport code */
	IATA,
	
	/** IMDb - Internet Movie Database */
	IMDB,
	
	/** International Music Score Library Project */
	IMSLP,
	
	/** INE municipality code */
	INE_MUNI,
	
	/** INSEE countries and foreign territories code */
	INSEE_COUNTRY,
	/** INSEE department code */
	INSEE_DEPT,
	/** INSEE municipality code */
	INSEE_MUNICIP,
	/** INSEE region code */
	INSEE_REGION,
	
	/** Instagram username */
	INSTRAGRAM,
	
	/** Internet Broadway Database person */
	INT_BROAD_DB_PERS,
	
	/** Internet Speculative Fiction Database - Author */
	ISFDB_AUTH,
	
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
	
	/** ISTAT */
	ISTAT,
	
	/** Italian Chamber of Deputies */
	IT_CHAMB_DEP,
	/** Italian cadastre code */
	IT_CAD_CODE,
	/** Italian Senate of the Republic */
	IT_SEN_REP,
	
	/** Jewish Encyclopedia ID (Russian) */
	JEW_ENCYC_RUSS,
	
	/** Kansallisbiografia */
	KANSAL_BIO,
	
	/** Key to English Place-Names - KEPN */
	KEPN,
	
	/** Person, in the Kinopoisk.ru database */
	KINOPOISK_PERS,
	
	/** KLfG Critical Dictionary of foreign contemporary literature */
	KLFG,
	
	/** Korean Movie Database - KMDb */
	KMDB,
	
	/** Komponisten der Gegenwart -- Munzinger Archiv */
	KOMP_GEG,
	/** Komponisten der Gegenwart -- Munzinger Archiv */
	KOMP_GEG_IBA,
	
	/** KulturNav-id */
	KULTURNAV,
	
	/** Kunstindeks Danmark Artist */
	KUNST_DAN_ART,
	
	/** Library and Archives Canada */
	LAC,
	
	/** Legal Entity */
	LEGAL_ENT,
	
	/** Base Léonore des membres de l'ordre de la Légion d'honneur */
	LEONORE,
	
	/** Librivox author */
	LIBRIVOX_AUTH,
	
	/** Library of Congress */
	LIB_CONGR,
	
	/** Mémoire des hommes - French govermnment database indexing all french soldier war casualties */
	MEM_HOM,
	
	/** Past or present MEP in a directory of all members of the European Parliament */
	MEP_DIR,
	
	/** Diseases and other medically relevant concepts, in the MeSH descriptor database */
	MESH,
	
	/** Mobile country code */
	MOBILE_COUNTRY_CODE,
	
	/** MoMA artist */
	MOMA_ART,
	
	/** Sujet du Monde diplomatique */
	MONDE_DIPLO,
	
	/** MovieMeter director */
	MOVIE_METER_DIR,
	
	/** Musée d'Orsay artist */
	MUSEE_ORSAY_ART,
	
	/** MusicBrainz area ID */
	MUSIC_BRAINZ_AREA,
	/** MusicBrainz artist ID */
	MUSIC_BRAINZ_ARTIST,
	/** MusicBrainz place ID */
	MUSIC_BRAINZ_PLACE,
	
	/** Musopen composer */
	MUSOPEN_COMP,
	
	/** MySpace ID */
	MYSPACE_USER,
	
	/** National Diet Library (Japan) */
	NAT_DIET_LIB,
	
	/** National-Football-Teams.com player ID */
	NAT_FOOT_PLR,
	
	/** National Gallery of Victoria artist */
	NAT_GAL_VICT_ART,
	
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
	/** LNB - National Library of Latvia */
	NAT_LIB_LV,
	/** BIBSYS - Bibliothèque nationale de Norvège */
	NAT_LIB_NO,
	/** National Library of Poland */
	NAT_LIB_PL,
	/** National Library of Romania */
	NAT_LIB_RO,
	/** Russian State Library */
	NAT_LIB_RU_PERS,
	/** Vatican Library */
	NAT_LIB_VA,
	
	/** Nationalmuseum Sweden artist */
	NAT_MUS_SE_ART,
	
	/** National Portrait Gallery (London) person ID */
	NAT_PORT_GALL_PERS,
	
	/** National Thesaurus for Author Names */
	NAT_THES_AUTH,
	
	/** NGA artist - National Gallery of Art in Washington DC */
	NGA_ART,
	
	/** Vegetti Catalog of Fantastic Literature */
	NILF_AUTH,
	
	/** Czech National Authority Database - National Library of Czech Republic */
	NKCR_AUT,
	
	/** Notable Names Database */
	NNDB,
	
	/** Nobel prize */
	NOBEL_PRIZE,
	
	/** National and University Library in Zagreb */
	NSK,
	
	/** NUKAT Center of the University of Warsaw Library */
	NUKAT,
	
	/** Nomenclature of Territorial Units for Statistics */
	NUTS_CODE,
	
	/** NYT topic */
	NYT,
	
	/** Classification on Objects territory of municipal formations (Russia) */
	OKTMO,
	
	/** OmegaWiki Defined Meaning */
	OMEGA_WIKI_MEANING,
	
	/** Omni topic */
	OMNI_TOPIC,
	
	/** OpenCorporates */
	OPEN_CORP,
	
	/** OpenDomesday settlement */
	OPEN_DOME,
	
	/** Open Library */
	OPEN_LIB,
	
	/** Author in the openMLOL digital library of cultural resources */
	OPEN_MLOL,
	
	/** OpenPlaques subject */
	OPEN_PLAQUE,
	
	/** Openpolis */
	OPEN_POLIS,
	
	/** Oxford Biography Index */
	OX_BIO_IDX,
	
	/** Member of the Parliamentary Assembly of the Council of Europe */
	PACE,
	
	/** Parlement & Politiek */
	PARL_POLIT,
	
	/** Person in the Prosopography of Anglo-Saxon England edit */
	PASE,
	
	/** Patrimonio Inmueble de Andalucía */
	PATR_INM_AND,
	
	/** J. Paul Getty Museum artist */
	PAUL_GETTY_ART,
	
	/** People Australia */
	PEOPLE_AUSTR,
	
	/** Perlentaucher -- German online magazine */
	PERLENTAUCHER,
	
	/** Persée, an open access digital library of French-language scholarly journals */
	PERSEE,
	
	/** Pinterest username */
	PINTEREST_USER,
	
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
	
	/** rodovid.org -- Family tree */
	RODOVID,
	
	/** Runeberg author */
	RUNEBERG_AUTH,
	
	/** Web-based edition of Joachim von Sandrart’s "Teutscher Academie der Edlen Bau, Bild- und Mahlerey-Künste" */
	SANDRART_PER,
	
	/** Serbian Academy of Sciences and Arts */
	SANU,
	
	/** Scope.dk person */
	SCOPE_DK_PERS,
	
	/** Association football (soccer) player, manager or referee at the Scoresway website */
	SCORESWAY_PERS,
	
	/** National Library of Sweden Libris library catalog */
	SELIBR,
	
	/** Database from the French Senate website */
	SENAT_FR,
	
	/** Swedish Film Database (SFDb) */
	SFDB,
	
	/** Smithsonian American Art Museum: person/institution */
	SMITH_AM_ART,
	
	/** Soccerway player ID */
	SOCCERWAY_PLYR,
	
	/** Songkick artist */
	SONGKICK_ART,
	
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
	
	/** Tate artist */
	TATE_ART,
	
	/** TED speaker */
	TED_SPEAKER,
	/** TED topic */
	TED_TOPIC,
	
	/** Te Papa artist */
	TE_PAPA_ART,
	
	/** Teuchos */
	TEUCHOS,
	
	/** Theatricalia person */
	THEATR_PERS,
	
	/** Thyssen-Bornemisza artist */
	THYSSEN_BORN_ART,
	
	/** TheFinalBall player ID */
	TFB_PLYR,
	
	/** TOID - TOpographic IDentifier assigned by the Ordnance Survey to identify a feature in Great Britain */
	TOID,
	
	/** Transfermarkt player ID */
	TRANSFERMARKT_PLYR,
	
	/** Enciclopedia Treccani */
	TRECCANI,
	
	/** Place (region, hotel, restaurant, attraction), in TripAdvisor */
	TRIPADVISOR_PLACE,
	
	/** Trismegistos Geo ID */
	TRISMEGISTOS_GEO,
	
	/** Twitter username */
	TWITTER_USER,
	
	/** Art UK artist */
	UK_ART,
	/** UK National Archives */
	UK_NAT_ARCHIV,
	
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
	
	/** Place in the University of Portsmouth's Vision of Britain database */
	VISION_PORT,
	
	/** The Vogue List */
	VOGUE_LIST,
	
	/** Web Gallery of Art */
	WEB_GALL_ART,
	
	/** Where On Earth IDentifier */
	WOEID,
	
	/** WomenWriters */
	WOM_WRIT,
	
	/** Worldfootball.net ID */
	WORLD_FOOT,
	
	/** World Heritage criteria */
	WORLD_HER_CRIT,
	
	/** World Heritage Site */
	WORLD_HER_SITE,
	
	/** YouTube channel */
	YOUTUBE_USER;
	
}
