package fr.univavignon.nerwip.tools.time;	

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
 * This exception is rised when trying to compare two dates, and
 * one of them is not complete enough to perform the comparison (i.e.
 * it misses the year, or the month, etc.).
 * 
 * @author Vincent Labatut
 */
public class IncompleteDateException extends RuntimeException
{	/** Class id */
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a new date exception.
	 * 
	 * @param msg
	 * 		Message to display.
	 * @param date1
	 * 		Date causing the problem.
	 * @param date2
	 * 		Other date causing the problem.
	 */
	public IncompleteDateException(String msg, Date date1, Date date2)
	{	super(msg);
	
		this.date1 = date1;
		this.date2 = date2;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** First date causing the problem */
	private Date date1;
	/** Second date causing the problem */
	private Date date2;
	
	/**
	 * Returns the first date causing the exception.
	 * 
	 * @return
	 * 		First cause of the exception.
	 */
	public Date getFirstDate()
	{	return date1;
	}

	/**
	 * Returns the second date causing the exception.
	 * 
	 * @return
	 * 		First cause of the exception.
	 */
	public Date getSecondDate()
	{	return date2;
	}
}
