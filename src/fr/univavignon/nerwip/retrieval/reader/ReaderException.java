package fr.univavignon.nerwip.retrieval.reader;

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
 * This exception is thrown when a problem
 * happens while retrieving a text.
 * 
 * @author Yasa Akbulut
 */
public class ReaderException extends Exception
{	/** Class id */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception,
	 * with no message.
	 */
	public ReaderException()
	{	super();
	}
	
	/**
	 * Creates a new exception,
	 * with a specific message.
	 * 
	 * @param s
	 * 		Message of the exception.
	 */
	public ReaderException(String s)
	{	super(s);
	}
}
