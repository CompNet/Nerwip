package fr.univavignon.nerwip.recognition;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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
 * Problem while performing the conversion
 * of the output of a recognizer. This conversion
 * can be internal (the tool was executed directly
 * from within Nerwip) or external (the recognizer
 * generated a file, which was then processed by
 * Nerwip).
 * 
 * @author Yasa Akbulut
 */
public class ConverterException extends Exception
{	/** Class id */
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a new exception,
	 * without any message.
	 */
	public ConverterException()
	{ super();
	}
	
	/**
	 * Builds a new exception,
	 * using the specified message.
	 * 
	 * @param message
	 * 		Exception message.
	 */
	public ConverterException(String message)
	{	super(message);
	}
}
