package fr.univavignon.nerwip.tools.time;	

import java.text.DateFormatSymbols;
import java.time.YearMonth;
import java.util.ArrayList;

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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * Sets of methods used to find dates in natural text,
 * extract them and get a formal representation under
 * the form of a {@link Date} object.
 * 
 * @author Vincent Labatut
 */
public class DateParser
{	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// MONTHS			 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of month long names */
	private static final List<String> MONTH_LONG = new ArrayList<String>();
	/** List of month short names */
	private static final List<String> MONTH_SHORT = new ArrayList<String>();
	/** Initializes both month lists */
	static
	{	DateFormatSymbols dfs = DateFormatSymbols.getInstance(Locale.ENGLISH);
		String[] months = dfs.getMonths();
		for(String month: months)
		{	String str = month.toLowerCase();
			MONTH_LONG.add(str);
		}
		String[] shortMonths = dfs.getShortMonths();
		for(String month: shortMonths)
		{	String str = month.toLowerCase();
			MONTH_SHORT.add(str);
		}
	}
	
	/**
	 * Used internally to convert a string list to a single string.
	 * 
	 * @param list
	 * 		List of strings.
	 * @return
	 * 		String representing the list.
	 */
	private static String printList(List<String> list)
	{	String result = "";
		Iterator<String> it = list.iterator();
		while(it.hasNext())
		{	String string = it.next();
			result = result + string;
			if(it.hasNext())
				result = result + "|";
		}
		return result;
	}
	
	/**
	 * Receives a text representation of a month (can be
	 * a name or a number) and returns the corresponding
	 * number (starting from 1 for January).
	 * 
	 * @param text
	 * 		Text representation of the month.
	 * @return
	 * 		Numeric representation of the month.
	 */
	private static int parseMonth(String text)
	{	int result = -1;
		text = text.trim();
		
		try
		{	result = Integer.parseInt(text);
		}
		catch(NumberFormatException e)
		{	result = MONTH_LONG.indexOf(text);
			if(result==-1)
				result = MONTH_SHORT.indexOf(text);
			if(result!=-1)
				result++;
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// QUALIFIERS		 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Represents the "early" qualifier */
	private static final String QUALIFIER_EARLY = "early";
	/** Represents the "mid" qualifier */
	private static final String QUALIFIER_MID = "mid";
	/** Represents the "late" qualifier */
	private static final String QUALIFIER_LATE = "late";
	
	/**
	 * Convert a month qualifier to the appropriate pair of dates.
	 * 
	 * @param qualifier
	 * 		String representing a month qualifier (early, mid, late).
	 * @param month
	 * 		Considered month.
	 * @param year
	 * 		Considered year, or 0 if unknown.
	 * @return
	 * 		The corresponding pair of dates.
	 */
	private static int[] parseQualifier(String qualifier, int month, int year)
	{	int result[] = new int[2];
		
		if(qualifier.equals(QUALIFIER_EARLY))
		{	result[0] = 1;
			result[1] = 10;
		}
		else if(qualifier.equals(QUALIFIER_MID))
		{	result[0] = 11;
			result[1] = 20;
		}
		else if(qualifier.equals(QUALIFIER_LATE))
		{	result[0] = 21;
			if(year==0)
			{	if(month==2)
					result[1] = 28;
				else if(month==1 || month==3 || month==5 || month==7 || month==8 || month==10 || month==12)
					result[1] = 31;
				else
					result[1] = 30;
			}
			else
			{	YearMonth ym = YearMonth.of(year, month);
				result[1] = ym.lengthOfMonth();
			}
		}
		
		return result;
	}
	
	/**
	 * Convert a decade qualifier to the appropriate pair of dates.
	 * 
	 * @param qualifier
	 * 		String representing a decade qualifier (early, mid, late).
	 * @param decade
	 * 		Considered decade.
	 * @return
	 * 		The corresponding pair of dates.
	 */
	private static Date[] parseQualifier(String qualifier, int decade)
	{	Date result[] = new Date[2];
		
		if(qualifier.equals(QUALIFIER_EARLY))
		{	result[0] = new Date(1,1,decade);
			result[1] = new Date(31,12,decade+2);
		}
		else if(qualifier.equals(QUALIFIER_MID))
		{	result[0] = new Date(1,1,decade+3);
			result[1] = new Date(31,12,decade+6);
		}
		else if(qualifier.equals(QUALIFIER_LATE))
		{	result[0] = new Date(1,1,decade+7);
			result[1] = new Date(31,12,decade+9);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// MISC				 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Convert a special day description to the appropriate pair of dates.
	 * 
	 * @param special
	 * 		String representing a special day (Christmas, May day, etc).
	 * @param year
	 * 		Considered year (or 0 if unknown).
	 * @return
	 * 		The corresponding pair of dates.
	 */
	private static Date[] parseSpecialDay(String special, int year)
	{	Date result[] = new Date[2];
		
		if(special.equals("may day"))
		{	result[0] = new Date(1,5,year);
			result[1] = new Date(1,5,year);
		}
		else if(special.startsWith("christmas") || special.startsWith("x-mas") || special.startsWith("xmas"))
		{	result[0] = new Date(25,12,year);
			result[1] = new Date(25,12,year);
		}
		else if(special.equals("new year's day"))
		{	result[0] = new Date(1,1,year);
			result[1] = new Date(1,1,year);
		}
		else if(special.equals("new year's eve"))
		{	result[0] = new Date(31,12,year);
			result[1] = new Date(31,12,year);
		}
		else if(special.equals("9/11"))
		{	result[0] = new Date(11,9,2001);
			result[1] = new Date(11,9,2001);
		}
		
		return result;
	}
	
	/**
	 * Convert a century to the appropriate pair of dates.
	 * 
	 * @param special
	 * 		String representing a century.
	 * @return
	 * 		The corresponding pair of dates.
	 */
	private static Date[] parseCentury(String special)
	{	Date result[] = new Date[2];
		int year = 0;
		if(special.startsWith("tenth") || special.startsWith("10"))
			year = 900;
		else if(special.startsWith("eleventh") || special.startsWith("11"))
			year = 1000;
		else if(special.startsWith("twelfth") || special.startsWith("12"))
			year = 1100;
		else if(special.startsWith("thirteenth") || special.startsWith("13"))
			year = 1200;
		else if(special.startsWith("fourteenth") || special.startsWith("14"))
			year = 1300;
		else if(special.startsWith("fifteenth") || special.startsWith("15"))
			year = 1400;
		else if(special.startsWith("sixteenth") || special.startsWith("16"))
			year = 1500;
		else if(special.startsWith("seventeenth") || special.startsWith("17"))
			year = 1600;
		else if(special.startsWith("eighteenth") || special.startsWith("18"))
			year = 1700;
		else if(special.startsWith("nineteenth") || special.startsWith("19"))
			year = 1800;
		else if(special.startsWith("twentieth") || special.startsWith("20"))
			year = 1900;
		else if(special.startsWith("twenty") || special.startsWith("21"))
			year = 2000;
		
		if(year!=0)
		{	result[0] = new Date(1,1,year);
			result[1] = new Date(31,12,year+99);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// REGEX GROUPS		 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the group corresponding to the century in the regex pattern */
	private static final String GROUP_CENTURY = "century";
	/** Name of the group corresponding to the first (or only) day in the regex pattern */
	private static final String GROUP_DAY1 = "day1";
	/** Name of the group corresponding to the second day in the regex pattern */
	private static final String GROUP_DAY2 = "day2";
	/** Name of the group corresponding to the decade in the regex pattern */
	private static final String GROUP_DECADE = "decade";
	/** Name of the group corresponding to the month in the regex pattern */
	private static final String GROUP_MONTH = "month";
	/** Name of the group corresponding to the qualifier in the regex pattern */
	private static final String GROUP_QUALIFIER = "qualifier";
	/** Name of the group corresponding to the year in the regex pattern */
	private static final String GROUP_YEAR = "year";
	
	/////////////////////////////////////////////////////////////////
	// EXPRESSIONS	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Represents various forms of hyphens (or equivalent characters) */
	private static final String EXPR_HYPHEN = "(-|–|/)";
	/** Qualifies a following date */
	private static final String EXPR_QUALIFIER = "((?<"+GROUP_QUALIFIER+">"+QUALIFIER_EARLY+"|"+QUALIFIER_MID+"|"+QUALIFIER_LATE+")("+EXPR_HYPHEN+"| ))";
	/** Represents a year of the form 1981 */
	private static final String EXPR_YEAR_FULL = "(?<"+GROUP_YEAR+">(1\\d{3})|(20\\d{2}))";
	/** Represents a year of the form 77 */
	private static final String EXPR_YEAR_SHORT = "(?<"+GROUP_YEAR+">\\d{2})";
	/** Represents a decade of the form 1990s */
	private static final String EXPR_DECADE_FULL = "(?<"+GROUP_DECADE+">(1\\d|20)\\d0s)";
	/** Represents a decade of the form 90s */
	private static final String EXPR_DECADE_SHORT = "(?<"+GROUP_DECADE+">\\d0s)";
	/** Represents a month of the form January */
	private static final String EXPR_MONTH_LONG = "(?<"+GROUP_MONTH+">"+printList(MONTH_LONG)+")";
	/** Represents a month of the form jan */
	private static final String EXPR_MONTH_SHORT = "(?<"+GROUP_MONTH+">"+printList(MONTH_SHORT)+")";
	/** Represents a month of the form 01 */
	@SuppressWarnings("unused")
	private static final String EXPR_MONTH_INT = "(?<"+GROUP_MONTH+">(0?\\d)|(1(0|1|2))";
	/** Represents a day of the form 31 */
	private static final String EXPR_DAY_INT_BASE = "((0|1|2)?\\d)|30|31";
	/** Represents a day of the form 31 */
	private static final String EXPR_DAY1_INT = "(?<"+GROUP_DAY1+">"+EXPR_DAY_INT_BASE+")";
	/** Represents a day of the form 31 */
	private static final String EXPR_DAY2_INT = "(?<"+GROUP_DAY2+">"+EXPR_DAY_INT_BASE+")";
	/** Represents a day of the form 31st */
	private static final String EXPR_DAY_ORDINAL = "(?<"+GROUP_DAY1+">((((0|1|2)?[3-9])|10|11|12|20|30)th)|((0?2|22|32)nd)|((0?1|21|31)st))"; 
	/** Represents a century of the form eleventh century */
	private static final String EXPR_CENTURY_LONG = "((?<"+GROUP_CENTURY+">((ten|eleven|twelf|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twentie)th)|twenty("+EXPR_HYPHEN+"| )first)( |"+EXPR_HYPHEN+")century)";
	/** Represents a century of the form 11th century */
	private static final String EXPR_CENTURY_SHORT = "((?<"+GROUP_CENTURY+">((1\\d|20)th)|21st)( |"+EXPR_HYPHEN+")century)";
	/** Represents an anniversary, of the form 40th anniversary */
	private static final String EXPR_ANNIVERSARY_SHORT = "((\\d*(0th|1st|2nd|3rd|[4-9]th))( |"+EXPR_HYPHEN+")anniversary)";
	/** Represents special days such as religious fests, etc. */
	private static final String EXPR_SPECIAL_DAY ="((may day)|((christ|x-?)mas( day)?)|(new year's (day|eve))|(9/11))";
	
	/////////////////////////////////////////////////////////////////
	// PATTERNS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of patterns used to detect dates based on the previous regexps */
	private static final List<DatePattern> PATTERNS = Arrays.asList(
		// "late may, 2010" or "late may 2010"
		new DatePattern("\\b"+EXPR_QUALIFIER+EXPR_MONTH_LONG+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH);
				int month = parseMonth(monthStr);
				// days
				String qualif = matcher.group(GROUP_QUALIFIER);
				int days[] = parseQualifier(qualif,month,year);
				// result
				Date startDate = new Date(days[0],month,year);
				Date endDate = new Date(days[1],month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "late april of 1968"
		new DatePattern("\\b"+EXPR_QUALIFIER+EXPR_MONTH_LONG+" of "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH);
				int month = parseMonth(monthStr);
				// days
				String qualif = matcher.group(GROUP_QUALIFIER);
				int days[] = parseQualifier(qualif,month,year);
				// result
				Date startDate = new Date(days[0],month,year);
				Date endDate = new Date(days[1],month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "late april"
		new DatePattern("\\b"+EXPR_QUALIFIER+EXPR_MONTH_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH);
				int month = parseMonth(monthStr);
				// days
				String qualif = matcher.group(GROUP_QUALIFIER);
				int days[] = parseQualifier(qualif,month,0);
				// result
				Date startDate = new Date(days[0],month,0);
				Date endDate = new Date(days[1],month,0);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		
		// "early 1990s"
		new DatePattern("\\b"+EXPR_QUALIFIER+EXPR_DECADE_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// decade
				String decadeStr = matcher.group(GROUP_DECADE);
				decadeStr = decadeStr.substring(0,decadeStr.length()-1);
				int decade = Integer.parseInt(decadeStr);
				// qualifier
				String qualif = matcher.group(GROUP_QUALIFIER);
				Date dates[] = parseQualifier(qualif,decade);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		// "early 90s" or "early '90s"
		new DatePattern("\\b"+EXPR_QUALIFIER+"'?"+EXPR_DECADE_SHORT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// decade
				String decadeStr = matcher.group(GROUP_DECADE);
				decadeStr = decadeStr.substring(0,decadeStr.length()-1);
				int decade = Integer.parseInt(decadeStr);
				if(decade<100)
					decade = decade + 1900;
				// qualifier
				String qualif = matcher.group(GROUP_QUALIFIER);
				Date dates[] = parseQualifier(qualif,decade);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		
		// "May Day 2001"
		new DatePattern("\\b"+EXPR_SPECIAL_DAY+" "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR);
				int year = Integer.parseInt(yearStr);
				// special day
				String special = matcher.group(GROUP_QUALIFIER);
				Date dates[] = parseSpecialDay(special,year);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		// "May Day"
		new DatePattern("\\b"+EXPR_SPECIAL_DAY)
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// special day
				String special = matcher.group(GROUP_QUALIFIER);
				Date dates[] = parseSpecialDay(special,0);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		
		// "twentieth-century" or "twentieth century"
		new DatePattern("\\b"+EXPR_CENTURY_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// century
				String century = matcher.group(GROUP_CENTURY);
				Date dates[] = parseCentury(century);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		// "11th-century" or "11th century"
		new DatePattern("\\b"+EXPR_CENTURY_SHORT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// century
				String century = matcher.group(GROUP_CENTURY);
				Date dates[] = parseCentury(century);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		
		// "40th-anniversary" or "40th anniversary"
		new DatePattern("\\b"+EXPR_ANNIVERSARY_SHORT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "18-20 April 1889" or "18-20 April, 1889" or "18-20 april 1889" or "18-20 april, 1889" 
		new DatePattern("\\b"+EXPR_DAY1_INT+EXPR_HYPHEN+EXPR_DAY2_INT+" "+EXPR_MONTH_LONG+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "4-6 October" or "4-6 october"
		new DatePattern("\\b"+EXPR_DAY1_INT+EXPR_HYPHEN+EXPR_DAY2_INT+" "+EXPR_MONTH_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "12 to 21 August, 2013" or "12 to 21 august, 2013" or "12 to 21 August 2013" or "12 to 21 august 2013"
		new DatePattern("\\b"+EXPR_DAY1_INT+" to "+EXPR_DAY2_INT+" "+EXPR_MONTH_LONG+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "12 to 21 August" or "12 to 21 august"
		new DatePattern("\\b"+EXPR_DAY1_INT+" to "+EXPR_DAY2_INT+" "+EXPR_MONTH_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "20 April 1889" or "20 April, 1889" or "20 april 1889" or "20 april, 1889" 
		new DatePattern("\\b"+EXPR_DAY1_INT+" "+EXPR_MONTH_LONG+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "5 Sep 1887" or "5 sep 1887"
		new DatePattern("\\b"+EXPR_DAY1_INT+" "+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "4 of October, 1975" or "4 of october, 1975" or "4 of October 1975" or "4 of october 1975"
		new DatePattern("\\b"+EXPR_DAY1_INT+" of "+EXPR_MONTH_LONG+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "6 October" or "6 october"
		new DatePattern("\\b"+EXPR_DAY1_INT+" "+EXPR_MONTH_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "10th of April 2004" or "10th of april 2004"
		new DatePattern("\\b"+EXPR_DAY_ORDINAL+" of "+EXPR_MONTH_LONG+" "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "10th of April" or "10th of april"
		new DatePattern("\\b"+EXPR_DAY_ORDINAL+" of "+EXPR_MONTH_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "October 25–26, 1821" or "october 25–26, 1821" or "October 25–26 1821" or "october 25–26 1821"
		new DatePattern("\\b"+EXPR_MONTH_LONG+" "+EXPR_DAY1_INT+EXPR_HYPHEN+EXPR_DAY2_INT+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "October 25–26, 1821" or "october 25–26, 1821" or "October 25–26 1821" or "october 25–26 1821"
		new DatePattern("\\b"+EXPR_MONTH_LONG+" "+EXPR_DAY1_INT+EXPR_HYPHEN+EXPR_DAY2_INT+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "March 6 and 8, 1918" or "march 6 and 8, 1918" or "March 6 and 8 1918" or "march 6 and 8 1918"
		new DatePattern("\\b"+EXPR_MONTH_LONG+" "+EXPR_DAY1_INT+" and "+EXPR_DAY2_INT+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "May 30, 1914" or "may 30, 1914" or "May 30 1914" or "may 30 1914"
		new DatePattern("\\b"+EXPR_MONTH_LONG+" "+EXPR_DAY1_INT+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "December 4 of 1922" or "december 4 of 1922"
		new DatePattern("\\b"+EXPR_MONTH_LONG+" "+EXPR_DAY1_INT+" of "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "December 9th 2010" or "december 9th 2010" or "December 9th, 2010" or "december 9th, 2010"
		new DatePattern("\\b"+EXPR_MONTH_LONG+" "+EXPR_DAY_ORDINAL+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "February the 20th of 2010" or "february the 20th of 2010"
		new DatePattern("\\b"+EXPR_MONTH_LONG+" the "+EXPR_DAY_ORDINAL+" of "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "October 6" or "october 6"
		new DatePattern("\\b"+EXPR_MONTH_LONG+" "+EXPR_DAY1_INT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "October, 1926" or "October 1926" or "october, 1926" or "october 1926"
		new DatePattern("\\b"+EXPR_MONTH_LONG+",? "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "April of 1968" or "april of 1968"
		new DatePattern("\\b"+EXPR_MONTH_LONG+" of "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "April"
		new DatePattern("\\b"+EXPR_MONTH_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "Sep-Dec 1996" or "sep-dec 1996" or "Sep-dec 1996" or "sep-Dec 1996"
		new DatePattern("\\b"+EXPR_MONTH_SHORT+EXPR_HYPHEN+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "Dec 1996" or "dec 1996"
		new DatePattern("\\b"+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "from 2002 to 06"
		new DatePattern("\\bfrom "+EXPR_YEAR_FULL+" to "+EXPR_YEAR_SHORT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "1990s"
		new DatePattern("\\b"+EXPR_DECADE_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "1977-85"
		new DatePattern("\\b"+EXPR_YEAR_FULL+EXPR_HYPHEN+EXPR_YEAR_SHORT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "2002-3"
		new DatePattern("\\b"+EXPR_YEAR_FULL+EXPR_HYPHEN+"\\d\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "2010"
		new DatePattern("\\b"+EXPR_YEAR_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		
		// "90s" or "'90s"
		new DatePattern("(\\b|')"+EXPR_DECADE_SHORT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		},
		// "'83"
		new DatePattern("'"+EXPR_YEAR_SHORT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	Period result = null;
				return result;
			}
		}
	);
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Parse the specified string and returns a {@link Period} object.
	 * 
	 * @param text
	 * 		Text to parse.
	 * @return
	 * 		The corresponding {@code Period} (may contain twice the same date).
	 */
	protected static Period parseDate(String text)
	{	logger.log("Start parsin date "+text);
		logger.increaseOffset();
		
		Period result = null;
		text = text.trim().toLowerCase(Locale.ENGLISH);
		
		Iterator<DatePattern> it = PATTERNS.iterator();
		while(it.hasNext() && result==null)
		{	DatePattern datePattern = it.next();
			Pattern pattern = datePattern.pattern;
			Matcher matcher = pattern.matcher(text);
			if(matcher.find())
				result = datePattern.extractDate(text,matcher);
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// TESTING		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Just for tests.
	 * 
	 * @param args
	 * 		None needed.
	 */
	public static void main(String[] args)
	{	
//		for(DatePattern dp: PATTERNS)
//			System.out.println(dp.pattern);
	
		DateFormatSymbols dfs = DateFormatSymbols.getInstance(Locale.ENGLISH);
		String[] months = dfs.getMonths();
		System.out.println(months.toString());
		String[] shortMonths = dfs.getShortMonths();
		System.out.println(shortMonths.toString());
	}
}
