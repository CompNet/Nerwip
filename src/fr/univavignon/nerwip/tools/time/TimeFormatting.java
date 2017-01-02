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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class contains a set of methods linked to time management.
 * They are mainly used by the log classes to add
 * time and date to log messages.
 * 
 * @author Vincent Labatut
 */
public class TimeFormatting
{
	/////////////////////////////////////////////////////////////////
	// HOUR				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** pattern used to format an hour */
	private static final String HOUR_PATTERN = "HH:mm:ss";
	/** format an hour */
	private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat(HOUR_PATTERN,Locale.ENGLISH);
	
	/**
	 * Returns a string representation of the current hour.
	 *  
	 * @return
	 * 		A string representing the current hour.
	 */
	public static String formatCurrentHour()
	{	Calendar cal = Calendar.getInstance();
	    String result = HOUR_FORMAT.format(cal.getTime());
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** pattern used to format a date */
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	/** format a date */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN,Locale.ENGLISH);
	
	/**
	 * Returns a string representation of the current date.
	 *  
	 * @return
	 * 		A string representing the current date.
	 */
	public static String formatCurrentDate()
	{	Calendar cal = Calendar.getInstance();
		String result = DATE_FORMAT.format(cal.getTime());
		return result;
	}

	/**
	 * Returns a string representation of the specified date.
	 *  
	 * @param date 
	 * 		Date to represent as a string.
	 * @return
	 * 		A string representing the specified date.
	 */
	public static String formatDate(Date date)
	{	String result = DATE_FORMAT.format(date);
		return result;
	}
	
	/**
	 * Returns the Date object corresponding
	 * to the specified string.
	 * 
	 * @param dateStr
	 * 		String representation of the date.
	 * @return
	 * 		The corresponding Date object.
	 * 
	 * @throws ParseException
	 * 		Problem while parsing the string.
	 */
	public static Date parseDate(String dateStr) throws ParseException
	{	Date result = DATE_FORMAT.parse(dateStr);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// XML TIME			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** pattern used to format a date and hour in XML files */
	private static final String TIME_PATTERN_XML = "yyyy-MM-dd'T'HH:mm:ss";
	/** format a date and hour in XML files*/
	public static final SimpleDateFormat TIME_FORMAT_XML = new SimpleDateFormat(TIME_PATTERN_XML,Locale.ENGLISH);
	
	/**
	 * Returns an XML string representation of the current date & hour.
	 *  
	 * @return
	 * 		An XML string representing the current date & hour.
	 */
	public static String formatCurrentXmlTime()
	{	Calendar cal = Calendar.getInstance();
	    String result = TIME_FORMAT_XML.format(cal.getTime());
		return result;
	}
	
	/**
	 * Returns an XML string representation of the specified time
	 * in terms of date & hour.
	 * 
	 * @param time 
	 * 		The time to format. 
	 * @return
	 * 		An XML string representing the specified time in terms of date & hour.
	 */
	public static String formatXmlTime(long time)
	{	Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		String result =  TIME_FORMAT_XML.format(cal.getTime());
		return result;
	}
	
	/**
	 * Returns an XML string representation of the specified time
	 * in terms of date & hour.
	 * 
	 * @param date
	 * 		The date to format. 
	 * @return
	 * 		An XML string representing the specified time in terms of date & hour.
	 */
	public static String formatXmlTime(Date date)
	{	String result =  TIME_FORMAT_XML.format(date);
		return result;
	}
	
	/**
	 * Returns the Date object corresponding
	 * to the specified XML string.
	 * 
	 * @param timeStr
	 * 		An XML string representation of the time.
	 * @return
	 * 		The corresponding Date object.
	 * 
	 * @throws ParseException
	 * 		Problem while parsing the string.
	 */
	public static Date parseXmlTime(String timeStr) throws ParseException
	{	Date result = TIME_FORMAT_XML.parse(timeStr);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE TIME		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** pattern used to format a date and hour in file names */
	private static final String TIME_PATTERN_FILE = "yyyy-MM-dd.HH-mm-ss";
	/** format a date and hour in file names */
	public static final SimpleDateFormat TIME_FORMAT_FILE = new SimpleDateFormat(TIME_PATTERN_FILE,Locale.ENGLISH);

	/**
	 * Returns a filename-compatible string representation 
	 * of the current date & hour.
	 *  
	 * @return
	 * 		A filename-compatible string representing the current date & hour.
	 */
	public static String formatCurrentFileTime()
	{	Calendar cal = Calendar.getInstance();
	    String result = TIME_FORMAT_FILE.format(cal.getTime());
		return result;
	}
	
	/**
	 * Returns a filename-compatible string representation 
	 * of the specified time in terms of date & hour.
	 * 
	 * @param time 
	 * 		The time to format. 
	 * @return
	 * 		A filename-compatible string representing the 
	 * 		specified time in terms of date & hour.
	 */
	public static String formatFileTime(long time)
	{	Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		String result = TIME_FORMAT_FILE.format(cal.getTime());
		return result;
	}
	
	/**
	 * Returns a filename-compatible string representation 
	 * of the specified time in terms of date & hour.
	 * 
	 * @param date
	 * 		The date to format. 
	 * @return
	 * 		A filename-compatible string representing the 
	 * 		specified time in terms of date & hour.
	 */
	public static String formatFileTime(Date date)
	{	String result = TIME_FORMAT_FILE.format(date);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// DURATIONS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns a {@code String} representation of
	 * the specified duration. The duration is
	 * expressed in ms whereas the result string
	 * is expressed in days-hours-minutes-seconds.
	 * 
	 * @param duration
	 * 		The duration to be processed (in ms).
	 * @return
	 * 		The corresponding string (in d-h-min-s).
	 */
	public static String formatDuration(long duration)
	{	// processing
		duration = duration / 1000;
		long seconds = duration % 60;
		duration = duration / 60;
		long minutes = duration % 60;
		duration = duration / 60;
		long hours = duration % 24;
		long days = duration / 24;
		
		// generating string
		String result = days + "d " + hours + "h " + minutes + "min " + seconds + "s";
		return result;
	}
}
