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

import java.util.Calendar;
import java.util.Locale;

/**
 * Represents a possibly partial date,
 * i.e. a date for which we don't
 * have all the necessary information.
 * 
 * @author Burcu Küpelioğlu
 * @author Vincent Labatut
 */
public class Date implements Comparable<Date>
{	
	/**
	 * Builds a full date.
	 * 
	 * @param day
	 * 		Day number in the month.
	 * @param month
	 * 		Month number in the year.
	 * @param year
	 * 		Full year.
	 */
	public Date(int day, int month, int year)
	{	this.day = day;
		this.month = month;
		this.year = year;
	}
	
	/**
	 * Builds a partial date.
	 * 
	 * @param month
	 * 		Month number in the year.
	 * @param year
	 * 		Full year.
	 */
	public Date(int month, int year)
	{	this.day = 0;
		this.month = month;
		this.year = year;
	}
	
	/**
	 * Builds a partial date.
	 * 
	 * @param year
	 * 		Full year.
	 */
	public Date(int year)
	{	this.day = 0;
		this.month = 0;
		this.year = year;
	}
	
	/**
	 * Builds a date from a standard Java
	 * date instance.
	 * 
	 * @param date
	 * 		A {@link java.util.Date} object.
	 */
	public Date(java.util.Date date)
	{	Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTime(date);
		this.day = cal.get(Calendar.DAY_OF_MONTH);
		this.month = cal.get(Calendar.MONTH+1);
		this.year = cal.get(Calendar.YEAR);
	}
	
	/////////////////////////////////////////////////////////////////
	// DAY				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of the day in the month (starting from 1) */
	private int day = 0;

	/**
	 * Gets the day of this date,
	 * or 0 if it is unknown.
	 * 
	 * @return
	 * 		Day number in the month.
	 */
	public int getDay()
	{	return day;
	}

//	public void setDay(int day)
//	{	this.day = day;
//	}
	
	/////////////////////////////////////////////////////////////////
	// MONTH			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/** List of month names in English */
//	private static final List<String> MONTHS = Arrays.asList("january","february","march","april","may","june","july","august","september","october","november","december");
	/** Number of the month in the year (starting from 1) */
	private int month = 0;
	
	/**
	 * Gets the month of this date,
	 * or 0 if it is unknown.
	 * 
	 * @return
	 * 		Month number in the year.
	 */
	public int getMonth()
	{	return month;
	}
	
//	public void setMonth(int month)
//	{	this.month = month;
//	}

	/////////////////////////////////////////////////////////////////
	// YEAR				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Year */
	private int year = 0;
	
	/**
	 * Gets the day of this date,
	 * or 0 if it is unknown.
	 * 
	 * @return
	 * 		Year of this date.
	 */
	public int getYear()
	{	return year;
	}

//	public void setYear(int year)
//	{	this.year = year;
//	}

	/////////////////////////////////////////////////////////////////
	// TEXT				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = "";
		
		if(day==0)
			result = result + "?";
		else
			result = result + day;
		result = result + "/";
		if(month==0)
			result = result + "?";
		else
			result = result + month;
		result = result + "/";
		if(year==0)
			result = result + "?";
		else
			result = result + year;
		
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
	{	String result = day+"/"+month+"/"+year;
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
	public static Date importFromString(String string)
	{	int day = 0;
		int month = 0;
		int year = 0;
		
		String temp[] = string.split("/");
		int i = temp.length - 1;
		year = Integer.parseInt(temp[i]);
		i--;
		month = Integer.parseInt(temp[i]);
		i--;
		day = Integer.parseInt(temp[i]);
		
		Date result = new Date(day,month,year);
		return result;
	}
	
//	/** Pattern used to find years in text */
//	private static final Pattern YEAR_PATTERN = Pattern.compile("(\\D|^)(18|19|20)\\d\\d(\\D|$)");
//	/** Pattern used to find months in text */
//	private static final Pattern MONTH_PATTERN = Pattern.compile("january|february|march|april|may|june|july|august|september|october|november|december",Pattern.CASE_INSENSITIVE);
//	/** Pattern used to find days in text */
//	private static final Pattern DAY_PATTERN = Pattern.compile("(\\D|^)(([0-1]\\d)|[1-9]|30|31)(\\D|$)");
//	
//	/**
//	 * Parse a Wikipedia string representing a date.
//	 * <br/> 
//	 * TODO This should actually be generalized,
//	 * since we might parse different kinds of
//	 * format if using other sources than WP.
//	 *  
//	 * @param string
//	 * 		String containing the date.
//	 * @return
//	 * 		The corresponding custom date object.
//	 */
//	public static Date parse(String string)
//	{	int day = 0;
//		int month = 0;
//		int year = 0;
////if(string.equals("October 6"))	//"March 10, 1969"
////	System.out.println();
//		// purely numerical format
////		if(string.contains("/"))
////		{	String temp[] = string.split("/");
////			int i = temp.length - 1;
////			year = Integer.parseInt(temp[i]);
////			if(i>0)
////			{	i--;
////				month = Integer.parseInt(temp[i]);
////				if(i>0)
////				{	i--;
////					day = Integer.parseInt(temp[i]);
////				}
////			}
////		}
////		
////		// partly textual format
////		else
//		{	// year
//			Matcher yearMatcher = YEAR_PATTERN.matcher(string);
//			if(yearMatcher.find())
//				year = retrieveInt(yearMatcher, string);
////			else
////				System.out.println("Date.parse: could not find a year in text '"+string+"'");
//			
//			// month
//			Matcher monthMatcher = MONTH_PATTERN.matcher(string);
//			if(monthMatcher.find())
//			{	int monthStart = monthMatcher.start();
//				int monthEnd = monthMatcher.end();
//				String monthStr = string.substring(monthStart,monthEnd).toLowerCase();
//				month = MONTHS.indexOf(monthStr)+1;
//				
//				// day
//				Matcher dayMatcher = DAY_PATTERN.matcher(string);
//				if(dayMatcher.find())
//					day = retrieveInt(dayMatcher, string);
//			}
//		}
//		
//		Date result = new Date(day,month,year);
//		return result;
//	}
//	
//	/**
//	 * Method used when parsing WP dates,
//	 * in order to get the integer value corresponding
//	 * to a year, month or day.
//	 * 
//	 * @param matcher
//	 * 		Matcher which has found something. 
//	 * @param string
//	 * 		Complete string.
//	 * @return
//	 * 		Integer value of the matched expression.
//	 */
//	private static int retrieveInt(Matcher matcher, String string)
//	{	int yearStart = matcher.start();
//		int yearEnd = matcher.end();
//		String yearStr = string.substring(yearStart,yearEnd);
//		if(!Character.isDigit(yearStr.charAt(0)))
//			yearStr = yearStr.substring(1);
//		if(!Character.isDigit(yearStr.charAt(yearStr.length()-1)))
//			yearStr = yearStr.substring(0,yearStr.length()-1);
//		int result = Integer.parseInt(yearStr);
//		return result;
//	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests whether this date is contained in the specified period.
	 * 
	 * @param start
	 * 		Start date of the period.
	 * @param end
	 * 		End date of the period.
	 * @return
	 * 		{@code true} iff this date belongs to the period.
	 */
	public boolean isContained(Date start, Date end)
	{	boolean result = this.compareTo(start)>=0 && this.compareTo(end)<=0;
		return result;
	}
	
	/**
	 * Checks if the specified date is compatible with this date,
	 * i.e. if they match exactly when ignoring their missing parts.
	 * 
	 * @param date
	 * 		The date to compare to this date.
	 * @return
	 * 		{@code true} iff they match exactly when ignoring their
	 * 		missing parts.
	 */
	public boolean isCompatible(Date date)
	{	Date d1 = new Date(day,month,year);
		Date d2 = new Date(date.day,date.month,date.year);
		
		if(d1.year==0 && d2.year!=0)
			d2.year = 0;
		else if (d1.year!=0 && d2.year==0)
			d1.year = 0;
		if(d1.month==0 && d2.month!=0)
			d2.month = 0;
		else if (d1.month!=0 && d2.month==0)
			d1.month = 0;
		if(d1.day==0 && d2.day!=0)
			d2.day = 0;
		else if (d1.day!=0 && d2.day==0)
			d1.day = 0;
		
		boolean result = d1.equals(d2);
		return result;
	}

	@Override
	public int compareTo(Date date)
	{	int result;
	
		// year
		if((year==0 && date.year!=0) || (year!=0 && date.year==0))
			throw new IllegalArgumentException("The dates '"+this+"' and '"+date+"' cannot be compared, because the year is unknown for exactly one of them");
		else
		{	result = year - date.year;
		
			// month
			if(result==0)
			{	if((month==0 && date.month!=0) || (month!=0 && date.month==0))
					throw new IllegalArgumentException("The dates '"+this+"' and '"+date+"' cannot be compared, because the month is unknown for exactly one of them");
				else
				{	result = month - date.month;
				
					// day
					if(result==0)
					{	if((day==0 && date.day!=0) || (day!=0 && date.day==0))
							throw new IllegalArgumentException("The dates '"+this+"' and '"+date+"' cannot be compared, because the day is unknown for exactly one of them");
						else
						{	result = day - date.day;
							
						}
					}
				}
			}
			
		}
		return result;
	}
	
	@Override
	public boolean equals(Object object)
	{	boolean result = false;
		if(object instanceof Date)
		{	Date date = (Date)object;
			result = compareTo(date)==0;
		}
		return result;
	}
}
