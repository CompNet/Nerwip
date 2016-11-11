package tr.edu.gsu.nerwip.recognition.internal.modelless;

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

import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalRecognizer;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.AbstractModelBasedInternalRecognizer;

/**
 * This class is used to represent or implement NER tools invocable 
 * internally, i.e. programmatically, from within Nerwip, and not
 * using any model, i.e. external files to be loaded (as opposed to
 * {@link AbstractModelBasedInternalRecognizer} recognizers.
 * 
 * @param <T>
 * 		Class of the converter associated to this recognizer.
 * @param <U>
 * 		Class of the internal representation of the mentions resulting from the detection.
 * 		 
 * @author Vincent Labatut
 */
public abstract class AbstractModellessInternalRecognizer<U,T extends AbstractInternalConverter<U>> extends AbstractInternalRecognizer<U,T>
{	
	/**
	 * Builds a new internal recognizer,
	 * using the specified options.
	 * 
	 * @param trim
	 * 		Whether or not the beginings and ends of mentions should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be ignored.
	 * @param exclusionOn
	 * 		Whether or not stop words should be ignored.
	 */
	public AbstractModellessInternalRecognizer(boolean trim, boolean ignorePronouns, boolean exclusionOn)
	{	super(trim,ignorePronouns,exclusionOn);
	}
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void prepareRecognizer() throws RecognizerException
	{	// nothing to do here
	}
}
