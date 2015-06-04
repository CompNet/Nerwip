package tr.edu.gsu.nerwip.recognition;

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
 * Each value of this enum type
 * represents one way of detecting
 * named entities. 
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public enum RecognizerName
{	
	/////////////////////////////////////////////////////////////////
	// INTERNAL 3rd PARTY TOOLS		/////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** HeidelTime */
	HEIDELTIME, 
	/** Illinois Named Entity Tagger */
	ILLINOIS, 
	/** Alias-i Ling Pipe */
	LINGPIPE,
	/** OpenCalais Web Service */
	OPENCALAIS,
	/** Apache OpenNLP */
	OPENNLP, 
	/** Stanford Named Entity Recognizer */ 
	STANFORD, 
	/** OpeNER Web Service */
	OPENER,
	
	
	/////////////////////////////////////////////////////////////////
	// INTERNAL CUSTOM TOOLS	/////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Takes advantage of hyperlinks to detect named entities */ 
	SUBEE,
	/** Tool specifically developped to detect dates */
	DATEEXTRACTOR,
	/** Tool specifically developped to detect dates in Wikipedia articles */
	WIKIPEDIADATER,
	
	/////////////////////////////////////////////////////////////////
	// EXTERNAL TOOLS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** NERO tool */
	NERO,
	/** TagEN  tool */
	TAGEN,
	
	/** TAGEN */
	TAGEN,
	
	/////////////////////////////////////////////////////////////////
	// COMBINERS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Combination of several NER tools through a simple voting process */
	VOTECOMBINER,
	/** Combination of several NER through an SVM classifier */
	SVMCOMBINER,
	/** Combination of WikipediaDater for dates and another combiner for the other types */
	FULLCOMBINER,
	/** Combination of HeidelTime for dates and OpenCalais for the other types */
	STRAIGHTCOMBINER,
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Manually annotated text */
	REFERENCE;
}
