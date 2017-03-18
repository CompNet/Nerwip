package fr.univavignon.nerwip.processing;

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
 * Each value of this enum type represents one way of processing
 * named entities, be it detecting their mentions, linking a mentions
 * to entites or resolving entity coreferences. 
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public enum ProcessorName
{	
	/////////////////////////////////////////////////////////////////
	// INTERNAL 3rd PARTY TOOLS		/////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** HeidelTime, a recognizer for dates */
	HEIDELTIME, 
	/** Illinois Named Entity Tagger, a recognizer */
	ILLINOIS, 
	/** Alias-i Ling Pipe, a recognizer */
	LINGPIPE,
	/** OpenCalais Web Service, a recognizer */
	OPENCALAIS,
	/** OpeNer Web Service, a recognizer */
	OPENER,
	/** Apache OpenNLP, a recognizer */
	OPENNLP, 
	/** DBpedia Spotlight Web Service, a recognizer, a resolver and a linker */
	SPOTLIGHT,
	/** Stanford Named Entity Recognizer, a recognizer */ 
	STANFORD, 
	
	/////////////////////////////////////////////////////////////////
	// INTERNAL CUSTOM TOOLS	/////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Tool specifically developped to recognize dates */
	DATEEXTRACTOR,
	/** Takes advantage of hyperlinks to recognize named entity mentions */ 
	SUBEE,
	/** Tool specifically developped to recognize dates in Wikipedia articles */
	WIKIPEDIADATER,
	
	/////////////////////////////////////////////////////////////////
	// EXTERNAL TOOLS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** NERO, a recognizer */
	NERO,
	/** TagEN, a recognizer */
	TAGEN,
	
	/////////////////////////////////////////////////////////////////
	// COMBINERS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Combination of WikipediaDater for dates and another combiner for the other types */
	FULLCOMBINER,
	/** Combination of HeidelTime for dates and OpenCalais for the other types */
	STRAIGHTCOMBINER,
	/** Combination of several recognizers through an SVM classifier */
	SVMCOMBINER,
	/** Combination of several recognizers through a simple voting process */
	VOTECOMBINER,
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Manually annotated text */
	REFERENCE;
}
