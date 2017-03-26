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
 * Represents a time period, whose bounds are expressed
 * as two possibly partial dates. If the start and end date
 * are the same, then it is not a period but a punctual date. 
 * 
 * @author Vincent Labatut
 */
public class Period implements Comparable<Period>
{	
	/**
	 * Builds a period based on the two specified
	 * (possibly partial) dates.
	 * 
	 * @param startDate
	 * 		Start of the period.
	 * @param endDate
	 * 		End of the period.
	 */
	public Period(Date startDate, Date endDate)
	{	this.startDate = startDate;
		this.endDate = endDate;
	}
	
	/**
	 * Builds a period based on the single specified
	 * (possibly partial) date.
	 * 
	 * @param date
	 * 		Start and end of the period.
	 */
	public Period(Date date)
	{	this.startDate = date;
		this.endDate = date;
	}
	
	/////////////////////////////////////////////////////////////////
	// START DATE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Start of this period */
	private Date startDate;

	/**
	 * Gets the start date of this period.
	 * 
	 * @return
	 * 		Start date of this period.
	 */
	public Date getStartDate()
	{	return startDate;
	}

//	public void setDay(int day)
//	{	this.day = day;
//	}
	
	/////////////////////////////////////////////////////////////////
	// START DATE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** End of this period */
	private Date endDate;

	/**
	 * Gets the end date of this period.
	 * 
	 * @return
	 * 		End date of this period.
	 */
	public Date getEndDate()
	{	return endDate;
	}
	
	/////////////////////////////////////////////////////////////////
	// TEXT				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = startDate.toString() + "-" + endDate.toString();
		return result;
	}
	
	/**
	 * Returns a string representing this date,
	 * meant to be written in text files.
	 * 
	 * @return
	 * 		A full string representation of this date.
	 */
	public String exportToString()
	{	String result = startDate.exportToString() + "-" + endDate.exportToString();
		return result;
	}
	
	/**
	 * Parse a string representing this date,
	 * meant to be read from a text file,
	 * and returns the corresponding date object.
	 * 
	 * @param string
	 * 		A full string representation of this date.
	 * @return
	 * 		The date built from the string.
	 */
	public static Period importFromString(String string)
	{	String tmp[] = string.split("-");
		Date startDate = Date.importFromString(tmp[0]);
		Date endDate = Date.importFromString(tmp[1]);
		Period result = new Period(startDate,endDate);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests whether the specified date is contained in this period.
	 * 
	 * @param date
	 * 		Considered date.
	 * @return
	 * 		{@code true} iff the date belongs to this period.
	 */
	public boolean contains(Date date)
	{	boolean result = date.isContained(startDate, endDate);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(Period period)
	{	Date startDate2 = period.startDate;
		Date endDate2 = period.endDate;
		
		int result = startDate.compareTo(startDate2);
		if(result==0)
			result = endDate.compareTo(endDate2);
		
		return result;
	}
	
	@Override
	public boolean equals(Object object)
	{	boolean result = false;
		if(object instanceof Period)
		{	Period period = (Period)object;
			result = compareTo(period)==0;
		}
		return result;
	}

	//TODO probably needs to define other comparison methods...
}
