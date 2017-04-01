package fr.univavignon.nerwip.tools.time;	

import java.text.DateFormatSymbols;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.LocalDate;
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
 * the form of a {@link Date} object. This class was 
 * designed for the English language only.
 * 
 * @author Vincent Labatut
 */
public class DateParserFr
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
	{	DateFormatSymbols dfs = DateFormatSymbols.getInstance(Locale.FRENCH);
		String[] months = dfs.getMonths();
		for(String month: months)
		{	String str = month.toLowerCase();
			str = Normalizer.normalize(str, Form.NFD) .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			if(!str.isEmpty())
				MONTH_LONG.add(str);
		}
		String[] shortMonths = dfs.getShortMonths();
		for(String month: shortMonths)
		{	String str = month.toLowerCase();
			str = Normalizer.normalize(str, Form.NFD) .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			if(!str.isEmpty())
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
	private static final String QUALIFIER_EARLY = "debut";
	/** Represents the "mid" qualifier */
	private static final String QUALIFIER_MID_SHORT = "mi";
	/** Represents the "mid" qualifier */
	private static final String QUALIFIER_MID_LONG = "milieu";
	/** Represents the "late" qualifier */
	private static final String QUALIFIER_LATE = "fin";
	
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
		else if(qualifier.equals(QUALIFIER_MID_SHORT) || qualifier.equals(QUALIFIER_MID_LONG))
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
		else if(qualifier.equals(QUALIFIER_MID_SHORT) || qualifier.equals(QUALIFIER_MID_LONG))
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
		
		if(special.equals("jour de l'an"))
		{	result[0] = new Date(1,1,year);
			result[1] = new Date(1,1,year);
		}
		else if(special.equals("lundi de paques") && year>0)
		{	Date easter = Date.getEasterDate(year);
			LocalDate temp = LocalDate.of(year, easter.getMonth(), easter.getDay());
			temp = temp.plusDays(1);
			result[0] = new Date(temp.getDayOfMonth(),temp.getMonthValue(),year);
			result[1] = new Date(temp.getDayOfMonth(),temp.getMonthValue(),year);
		}
		else if(special.equals("paques"))
		{	result[0] = Date.getEasterDate(year);
			result[1] = Date.getEasterDate(year);
		}
		else if(special.equals("fete du travail"))
		{	result[0] = new Date(1,5,year);
			result[1] = new Date(1,5,year);
		}
		else if(special.equals("ascension"))
		{	Date easter = Date.getEasterDate(year);
			LocalDate temp = LocalDate.of(year, easter.getMonth(), easter.getDay());
			temp = temp.plusDays(39);
			result[0] = new Date(temp.getDayOfMonth(),temp.getMonthValue(),year);
			result[1] = new Date(temp.getDayOfMonth(),temp.getMonthValue(),year);
		}
		else if(special.equals("pentecote"))
		{	Date easter = Date.getEasterDate(year);
			LocalDate temp = LocalDate.of(year, easter.getMonth(), easter.getDay());
			temp = temp.plusDays(49);
			result[0] = new Date(temp.getDayOfMonth(),temp.getMonthValue(),year);
			result[1] = new Date(temp.getDayOfMonth(),temp.getMonthValue(),year);
		}
		else if(special.equals("transfiguration"))
		{	result[0] = new Date(6,8,year);
			result[1] = new Date(6,8,year);
		}
		else if(special.equals("assomption"))
		{	result[0] = new Date(15,8,year);
			result[1] = new Date(15,8,year);
		}
		else if(special.equals("toussaint"))
		{	result[0] = new Date(1,11,year);
			result[1] = new Date(1,11,year);
		}
		else if(special.equals("noel"))
		{	result[0] = new Date(25,12,year);
			result[1] = new Date(25,12,year);
		}
		else if(special.startsWith("saint") && special.contains("sylvestre"))
		{	result[0] = new Date(31,12,year);
			result[1] = new Date(31,12,year);
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
		if(special.startsWith("dix-huiti") || special.startsWith("18") || special.startsWith("xviii") || special.startsWith("xiix"))
			year = 1700;
		else if(special.startsWith("dix-septi") || special.startsWith("17") || special.startsWith("xvii"))
			year = 1600;
		else if(special.startsWith("seizi") || special.startsWith("16") || special.startsWith("xvi"))
			year = 1500;
		else if(special.startsWith("treizi") || special.startsWith("13") || special.startsWith("xiii"))
			year = 1200;
		else if(special.startsWith("quatorzi") || special.startsWith("14") || special.startsWith("xiv"))
			year = 1300;
		else if(special.startsWith("dix-neuvi") || special.startsWith("19") || special.startsWith("xix"))
			year = 1800;
		else if(special.startsWith("vingt") && special.contains("uni") || special.startsWith("21") || special.startsWith("xxi"))
			year = 2000;
		else if(special.startsWith("douzi") || special.startsWith("12") || special.startsWith("xii"))
			year = 1100;
		else if(special.startsWith("quinzi") || special.startsWith("15") || special.startsWith("xv"))
			year = 1400;
		else if(special.startsWith("onzi") || special.startsWith("11") || special.startsWith("xi"))
			year = 1000;
		else if(special.startsWith("vingti") || special.startsWith("20") || special.startsWith("xx"))
			year = 1900;
		else if(special.startsWith("dixi") || special.startsWith("10") || special.startsWith("x"))
			year = 900;
		
		if(year!=0)
		{	result[0] = new Date(1,1,year);
			result[1] = new Date(31,12,year+99);
		}
		
		return result;
	}
	
	/**
	 * Parse a string representing a day in an ordinal
	 * form (1st, 2nd, etc.).
	 * 
	 * @param dayStr
	 * 		String ordinal representation of the day.
	 * @return
	 * 		Integer representation of the day.
	 */
	private static int parseOrdinalDay(String dayStr)
	{	int result = 0;
		if(dayStr.equalsIgnoreCase("premier"))
			result = 1;
		else
		{	dayStr = dayStr.substring(0,2);
			try
			{	result = Integer.parseInt(dayStr);
			}
			catch(NumberFormatException e)
			{	dayStr = dayStr.substring(0,1);
				result = Integer.parseInt(dayStr);
			}
		}
		return result;
	}
	
	/**
	 * Parse a string representing a decade (90s)
	 * and returns the corresponding numerical value.
	 * 
	 * @param decadeStr
	 * 		String representation of the decade
	 * @return
	 * 		Integer representation of the decade.
	 */
	private static int parseDecade(String decadeStr)
	{	// possible remove an anglosaxon final "s" and parse
		if(decadeStr.endsWith("s"))
			decadeStr = decadeStr.substring(0,decadeStr.length()-1);
		int result = Integer.parseInt(decadeStr);
		// possibly adds the century
		if(result<100)
			result = result + 1900;
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
	/** Name of the group corresponding to the first month (or only) in the regex pattern */
	private static final String GROUP_MONTH1 = "month1";
	/** Name of the group corresponding to the second month in the regex pattern */
	private static final String GROUP_MONTH2 = "month2";
	/** Name of the group corresponding to the qualifier in the regex pattern */
	private static final String GROUP_QUALIFIER = "qualifier";
	/** Name of the group corresponding to a special day in the regex pattern */
	private static final String GROUP_SPECIAL_DAY = "specialday";
	/** Name of the group corresponding to the first (or only) year in the regex pattern */
	private static final String GROUP_YEAR1 = "year1";
	/** Name of the group corresponding to the second year in the regex pattern */
	private static final String GROUP_YEAR2 = "year2";
	
	/////////////////////////////////////////////////////////////////
	// EXPRESSIONS	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Represents various forms of hyphens (or equivalent characters) */
	private static final String EXPR_HYPHEN = "(-|–|/)";
	/** Qualifies a following date */
	private static final String EXPR_QUALIFIER = "((?<"+GROUP_QUALIFIER+">"+QUALIFIER_EARLY+"|"+QUALIFIER_MID_SHORT+"|"+QUALIFIER_MID_LONG+"|"+QUALIFIER_LATE+")("+EXPR_HYPHEN+"| ))";
	/** Represents a year of the form 1981 */
	private static final String EXPR_YEAR_FULL1 = "(?<"+GROUP_YEAR1+">(1\\d{3})|(20\\d{2}))";
	/** Represents a year of the form 77 */
	private static final String EXPR_YEAR_SHORT1 = "(?<"+GROUP_YEAR1+">\\d{2})";
	/** Represents a year of the form 77 */
	private static final String EXPR_YEAR_SHORT2 = "(?<"+GROUP_YEAR2+">\\d{2})";
	/** Represents a decade of the form 1990s */
	private static final String EXPR_DECADE_FULL = "(?<"+GROUP_DECADE+">(1\\d|20)\\d0s?)";
	/** Represents a decade of the form 90s */
	private static final String EXPR_DECADE_SHORT = "(?<"+GROUP_DECADE+">\\d0s?)";
	/** Represents a month of the form janvier */
	private static final String EXPR_MONTH1_LONG = "(?<"+GROUP_MONTH1+">"+printList(MONTH_LONG)+")";
	/** Represents a month of the form janvier */
	private static final String EXPR_MONTH2_LONG = "(?<"+GROUP_MONTH2+">"+printList(MONTH_LONG)+")";
	/** Represents a month of the form janv */
	private static final String EXPR_MONTH1_SHORT = "(?<"+GROUP_MONTH1+">"+printList(MONTH_SHORT)+")";
	/** Represents a month of the form janv */
	private static final String EXPR_MONTH2_SHORT = "(?<"+GROUP_MONTH2+">"+printList(MONTH_SHORT)+")";
	/** Represents a day of the form 31 */
	private static final String EXPR_DAY_INT_BASE = "((0|1|2)?\\d)|30|31";
	/** Represents a day of the form 31 */
	private static final String EXPR_DAY1_INT = "(?<"+GROUP_DAY1+">"+EXPR_DAY_INT_BASE+")";
	/** Represents a day of the form 31 */
	private static final String EXPR_DAY2_INT = "(?<"+GROUP_DAY2+">"+EXPR_DAY_INT_BASE+")";
	/** Represents a day of the form 1er */
	private static final String EXPR_DAY_ORDINAL = "(?<"+GROUP_DAY1+">1"+EXPR_HYPHEN+"?er|premier)"; 
	/** Represents a century of the form onzieme siecle */
	private static final String EXPR_CENTURY_LONG = "(?<"+GROUP_CENTURY+">(dix|onz|douz|treiz|quatorz|quinz|seiz|dix-sept|dix-huit|dix-neuf|vingt et un|vingt-et-un|vingt)"+EXPR_HYPHEN+"?ieme siecle)";
	/** Represents a century of the form 11eme siecle */
	private static final String EXPR_CENTURY_SHORT = "(?<"+GROUP_CENTURY+">((1\\d|21)|(xviii|xiix|xiii|xvii|xxi|xix|xiv|xii|xvi|xv|xi|xx|x))"+EXPR_HYPHEN+"?i?e(me)? siecle)";
	/** Represents special days such as religious fests, etc. */
	private static final String EXPR_SPECIAL_DAY ="(?<"+GROUP_SPECIAL_DAY+">(jour de l'an)|((lundi de )?paques)|(fete du travail)|(ascension)|(pentecote)|(transfiguration)|(assomption)|(toussaint)|(noel)|((s(t|aint)("+EXPR_HYPHEN+"| )sylvestre)))";
	
	/////////////////////////////////////////////////////////////////
	// PATTERNS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of patterns used to detect dates based on the previous regexps */
	private static final List<DatePattern> PATTERNS = Arrays.asList(
		// "fin mai 2010"
		new DatePattern("\\b"+EXPR_QUALIFIER+EXPR_MONTH1_LONG+",? "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
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
		// "fin avril"
		new DatePattern("\\b"+EXPR_QUALIFIER+EXPR_MONTH1_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH1);
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
		
		// "début des années 1990"
		new DatePattern("\\b"+EXPR_QUALIFIER+"des annees "+EXPR_DECADE_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// decade
				String decadeStr = matcher.group(GROUP_DECADE);
				int decade = parseDecade(decadeStr);
				// qualifier
				String qualif = matcher.group(GROUP_QUALIFIER);
				Date dates[] = parseQualifier(qualif,decade);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		// "début des années 90" or "début des années '90"
		new DatePattern("\\b"+EXPR_QUALIFIER+"des annees '?"+EXPR_DECADE_SHORT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// decade
				String decadeStr = matcher.group(GROUP_DECADE);
				int decade = parseDecade(decadeStr);
				// qualifier
				String qualif = matcher.group(GROUP_QUALIFIER);
				Date dates[] = parseQualifier(qualif,decade);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		
		// "Noël 2001"
		new DatePattern("\\b"+EXPR_SPECIAL_DAY+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// special day
				String special = matcher.group(GROUP_SPECIAL_DAY);
				Date dates[] = parseSpecialDay(special,year);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		// "Noël"
		new DatePattern("\\b"+EXPR_SPECIAL_DAY)
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// special day
				String special = matcher.group(GROUP_SPECIAL_DAY);
				Date dates[] = parseSpecialDay(special,0);
				// result
				Period result = new Period(dates[0],dates[1]);
				return result;
			}
		},
		
		// "vingtième siècle" or "vingtième Siecle"
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
		// "11ème siècle" or "XIème siècle"
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
		
		// "18-20 avril 1889" 
		new DatePattern("\\b"+EXPR_DAY1_INT+EXPR_HYPHEN+EXPR_DAY2_INT+" "+EXPR_MONTH1_LONG+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// start day
				String startDayStr = matcher.group(GROUP_DAY1);
				int startDay = Integer.parseInt(startDayStr);
				// end day
				String endDayStr = matcher.group(GROUP_DAY2);
				int endDay = Integer.parseInt(endDayStr);
				// result
				Date startDate = new Date(startDay,month,year);
				Date endDate = new Date(endDay,month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "1er-20 avril 1889" 
		new DatePattern("\\b"+EXPR_DAY_ORDINAL+EXPR_HYPHEN+EXPR_DAY2_INT+" "+EXPR_MONTH1_LONG+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// start day
				String startDayStr = matcher.group(GROUP_DAY1);
				int startDay = parseOrdinalDay(startDayStr);
				// end day
				String endDayStr = matcher.group(GROUP_DAY2);
				int endDay = Integer.parseInt(endDayStr);
				// result
				Date startDate = new Date(startDay,month,year);
				Date endDate = new Date(endDay,month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "4-6 octobre"
		new DatePattern("\\b"+EXPR_DAY1_INT+EXPR_HYPHEN+EXPR_DAY2_INT+" "+EXPR_MONTH1_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// start day
				String startDayStr = matcher.group(GROUP_DAY1);
				int startDay = Integer.parseInt(startDayStr);
				// end day
				String endDayStr = matcher.group(GROUP_DAY2);
				int endDay = Integer.parseInt(endDayStr);
				// result
				Date startDate = new Date(startDay,month,0);
				Date endDate = new Date(endDay,month,0);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "1er-6 octobre"
		new DatePattern("\\b"+EXPR_DAY_ORDINAL+EXPR_HYPHEN+EXPR_DAY2_INT+" "+EXPR_MONTH1_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// start day
				String startDayStr = matcher.group(GROUP_DAY1);
				int startDay = parseOrdinalDay(startDayStr);
				// end day
				String endDayStr = matcher.group(GROUP_DAY2);
				int endDay = Integer.parseInt(endDayStr);
				// result
				Date startDate = new Date(startDay,month,0);
				Date endDate = new Date(endDay,month,0);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "du 12 au 21 aout 2013"
		new DatePattern("\\bdu "+EXPR_DAY1_INT+" au "+EXPR_DAY2_INT+" "+EXPR_MONTH1_LONG+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// start day
				String startDayStr = matcher.group(GROUP_DAY1);
				int startDay = Integer.parseInt(startDayStr);
				// end day
				String endDayStr = matcher.group(GROUP_DAY2);
				int endDay = Integer.parseInt(endDayStr);
				// result
				Date startDate = new Date(startDay,month,year);
				Date endDate = new Date(endDay,month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "du 1er au 21 aout 2013"
		new DatePattern("\\bdu "+EXPR_DAY_ORDINAL+" au "+EXPR_DAY2_INT+" "+EXPR_MONTH1_LONG+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// start day
				String startDayStr = matcher.group(GROUP_DAY1);
				int startDay = parseOrdinalDay(startDayStr);
				// end day
				String endDayStr = matcher.group(GROUP_DAY2);
				int endDay = Integer.parseInt(endDayStr);
				// result
				Date startDate = new Date(startDay,month,year);
				Date endDate = new Date(endDay,month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "du 12 au 21 aout"
		new DatePattern("\\bdu "+EXPR_DAY1_INT+" au "+EXPR_DAY2_INT+" "+EXPR_MONTH1_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// start day
				String startDayStr = matcher.group(GROUP_DAY1);
				int startDay = Integer.parseInt(startDayStr);
				// end day
				String endDayStr = matcher.group(GROUP_DAY2);
				int endDay = Integer.parseInt(endDayStr);
				// result
				Date startDate = new Date(startDay,month,0);
				Date endDate = new Date(endDay,month,0);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "du 1er au 21 aout"
		new DatePattern("\\bdu "+EXPR_DAY_ORDINAL+" au "+EXPR_DAY2_INT+" "+EXPR_MONTH1_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// start day
				String startDayStr = matcher.group(GROUP_DAY1);
				int startDay = parseOrdinalDay(startDayStr);
				// end day
				String endDayStr = matcher.group(GROUP_DAY2);
				int endDay = Integer.parseInt(endDayStr);
				// result
				Date startDate = new Date(startDay,month,0);
				Date endDate = new Date(endDay,month,0);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		
		// "20 avril 1889" 
		new DatePattern("\\b"+EXPR_DAY1_INT+" "+EXPR_MONTH1_LONG+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// day
				String dayStr = matcher.group(GROUP_DAY1);
				int day = Integer.parseInt(dayStr);
				// result
				Date startDate = new Date(day,month,year);
				Date endDate = new Date(day,month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "1er avril 1889" 
		new DatePattern("\\b"+EXPR_DAY_ORDINAL+" "+EXPR_MONTH1_LONG+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// day
				String dayStr = matcher.group(GROUP_DAY1);
				int day = parseOrdinalDay(dayStr);
				// result
				Date startDate = new Date(day,month,year);
				Date endDate = new Date(day,month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "5 sept. 1887"
		new DatePattern("\\b"+EXPR_DAY1_INT+" "+EXPR_MONTH1_SHORT+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// day
				String dayStr = matcher.group(GROUP_DAY1);
				int day = Integer.parseInt(dayStr);
				// result
				Date startDate = new Date(day,month,year);
				Date endDate = new Date(day,month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "1er sept. 1887"
		new DatePattern("\\b"+EXPR_DAY_ORDINAL+" "+EXPR_MONTH1_SHORT+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// day
				String dayStr = matcher.group(GROUP_DAY1);
				int day = parseOrdinalDay(dayStr);
				// result
				Date startDate = new Date(day,month,year);
				Date endDate = new Date(day,month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "6 octobre"
		new DatePattern("\\b"+EXPR_DAY1_INT+" "+EXPR_MONTH1_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// day
				String dayStr = matcher.group(GROUP_DAY1);
				int day = Integer.parseInt(dayStr);
				// result
				Date startDate = new Date(day,month,0);
				Date endDate = new Date(day,month,0);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "1er octobre"
		new DatePattern("\\b"+EXPR_DAY_ORDINAL+" "+EXPR_MONTH1_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// day
				String dayStr = matcher.group(GROUP_DAY1);
				int day = parseOrdinalDay(dayStr);
				// result
				Date startDate = new Date(day,month,0);
				Date endDate = new Date(day,month,0);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		
		// "de septembre a decembre 1996"
		new DatePattern("\\bd(e |')"+EXPR_MONTH1_LONG+" a "+EXPR_MONTH2_LONG+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// start month
				String startMonthStr = matcher.group(GROUP_MONTH1);
				int startMonth = parseMonth(startMonthStr);
				// end month
				String endMonthStr = matcher.group(GROUP_MONTH2);
				int endMonth = parseMonth(endMonthStr);
				// result
				Date startDate = new Date(startMonth,year);
				Date endDate = new Date(endMonth,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "septembre-decembre 1996"
		new DatePattern("\\b"+EXPR_MONTH1_LONG+EXPR_HYPHEN+EXPR_MONTH2_LONG+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// start month
				String startMonthStr = matcher.group(GROUP_MONTH1);
				int startMonth = parseMonth(startMonthStr);
				// end month
				String endMonthStr = matcher.group(GROUP_MONTH2);
				int endMonth = parseMonth(endMonthStr);
				// result
				Date startDate = new Date(startMonth,year);
				Date endDate = new Date(endMonth,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "octobre 1926"
		new DatePattern("\\b"+EXPR_MONTH1_LONG+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// result
				Date startDate = new Date(month,year);
				Date endDate = new Date(month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "avril"
		new DatePattern("\\b"+EXPR_MONTH1_LONG+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// result
				Date startDate = new Date(month,0);
				Date endDate = new Date(month,0);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		
		// "sept.-dec. 1996"
		new DatePattern("\\b"+EXPR_MONTH1_SHORT+EXPR_HYPHEN+EXPR_MONTH2_SHORT+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// start month
				String startMonthStr = matcher.group(GROUP_MONTH1);
				int startMonth = parseMonth(startMonthStr);
				// end month
				String endMonthStr = matcher.group(GROUP_MONTH2);
				int endMonth = parseMonth(endMonthStr);
				// result
				Date startDate = new Date(startMonth,year);
				Date endDate = new Date(endMonth,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "déc. 1996"
		new DatePattern("\\b"+EXPR_MONTH1_SHORT+" "+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// month
				String monthStr = matcher.group(GROUP_MONTH1);
				int month = parseMonth(monthStr);
				// result
				Date startDate = new Date(month,year);
				Date endDate = new Date(month,year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		
		// "de 2002 a 06"
		new DatePattern("\\bde "+EXPR_YEAR_FULL1+" a "+EXPR_YEAR_SHORT2+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// start year
				String startYearStr = matcher.group(GROUP_YEAR1);
				int startYear = Integer.parseInt(startYearStr);
				// end year
				String endYearStr = matcher.group(GROUP_YEAR2);
				int endYear = Integer.parseInt(endYearStr);
				endYear = endYear + (startYear/100)*100;
				// result
				Date startDate = new Date(startYear);
				Date endDate = new Date(endYear);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		
		// "annees 1990s"
		new DatePattern("\\bannees "+EXPR_DECADE_FULL+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// decade
				String decadeStr = matcher.group(GROUP_DECADE);
				int decade = parseDecade(decadeStr);
				// result
				Date startDate = new Date(decade);
				Date endDate = new Date(decade+9);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "1977-85"
		new DatePattern("\\b"+EXPR_YEAR_FULL1+EXPR_HYPHEN+EXPR_YEAR_SHORT2+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// start year
				String startYearStr = matcher.group(GROUP_YEAR1);
				int startYear = Integer.parseInt(startYearStr);
				// end year
				String endYearStr = matcher.group(GROUP_YEAR2);
				int endYear = Integer.parseInt(endYearStr);
				endYear = endYear + (startYear/100)*100;
				// result
				Date startDate = new Date(startYear);
				Date endDate = new Date(endYear);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "2002-3"
		new DatePattern("\\b"+EXPR_YEAR_FULL1+EXPR_HYPHEN+"(?<"+GROUP_YEAR2+">\\d)\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// start year
				String startYearStr = matcher.group(GROUP_YEAR1);
				int startYear = Integer.parseInt(startYearStr);
				// end year
				String endYearStr = matcher.group(GROUP_YEAR2);
				int endYear = Integer.parseInt(endYearStr);
				endYear = endYear + (startYear/10)*10;
				// result
				Date startDate = new Date(startYear);
				Date endDate = new Date(endYear);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "2010"
		new DatePattern("\\b"+EXPR_YEAR_FULL1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				// result
				Date startDate = new Date(year);
				Date endDate = new Date(year);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		
		// "annees 90" or "annees '90s"
		new DatePattern("\\bannees '?"+EXPR_DECADE_SHORT+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// decade
				String decadeStr = matcher.group(GROUP_DECADE);
				int decade = parseDecade(decadeStr);
				// result
				Date startDate = new Date(decade);
				Date endDate = new Date(decade+9);
				Period result = new Period(startDate,endDate);
				return result;
			}
		},
		// "'83"
		new DatePattern("'"+EXPR_YEAR_SHORT1+"\\b")
		{	@Override
			public Period extractDate(String text, Matcher matcher)
			{	// year
				String yearStr = matcher.group(GROUP_YEAR1);
				int year = Integer.parseInt(yearStr);
				year = year + 1900;
				// result
				Date startDate = new Date(year);
				Date endDate = new Date(year);
				Period result = new Period(startDate,endDate);
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
	{	logger.log("Start parsing date \""+text+"\"");
		logger.increaseOffset();
		
		Period result = null;
		text = text.trim().toLowerCase(Locale.ENGLISH);
		text = Normalizer.normalize(text, Form.NFD) .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		
		Iterator<DatePattern> it = PATTERNS.iterator();
		while(it.hasNext() && result==null)
		{	DatePattern datePattern = it.next();
			Pattern pattern = datePattern.pattern;
			Matcher matcher = pattern.matcher(text);
			if(matcher.find())
				result = datePattern.extractDate(text,matcher);
		}
		
		logger.log("Result:"+result);
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// TESTING		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/**
//	 * Just for tests.
//	 * 
//	 * @param args
//	 * 		None needed.
//	 */
//	public static void main(String[] args) //rajouter XXè
//	{	String dateStr;
//		dateStr="fin mai 2010";parseDate(dateStr);
//		dateStr="fin avril";parseDate(dateStr);
//		dateStr="début des années 1990";parseDate(dateStr);
//		dateStr="début des années 90";parseDate(dateStr);
//		dateStr="début des années '90";parseDate(dateStr);
//		dateStr="Noël 2001";parseDate(dateStr);
//		dateStr="Noël";parseDate(dateStr);
//		dateStr="vingtième siècle";parseDate(dateStr);
//		dateStr="vingtième Siecle";parseDate(dateStr);
//		dateStr="11ème siècle";parseDate(dateStr);
//		dateStr="XIème siècle";parseDate(dateStr);
//		dateStr="18-20 avril 1889";parseDate(dateStr);
//		dateStr="1er-20 avril 1889";parseDate(dateStr);
//		dateStr="4-6 octobre";parseDate(dateStr);
//		dateStr="1er-6 octobre";parseDate(dateStr);
//		dateStr="du 12 au 21 aout 2013";parseDate(dateStr);
//		dateStr="du 1er au 21 aout 2013";parseDate(dateStr);
//		dateStr="du 12 au 21 aout";parseDate(dateStr);
//		dateStr="du 1er au 21 aout";parseDate(dateStr);
//		dateStr="20 avril 1889";parseDate(dateStr); 
//		dateStr="1er avril 1889";parseDate(dateStr); 
//		dateStr="5 sept. 1887";parseDate(dateStr);
//		dateStr="1er sept. 1887";parseDate(dateStr);
//		dateStr="6 octobre";parseDate(dateStr);
//		dateStr="1er octobre";parseDate(dateStr);
//		dateStr="de septembre à decembre 1996";parseDate(dateStr);
//		dateStr="septembre-decembre 1996";parseDate(dateStr);
//		dateStr="octobre 1926";parseDate(dateStr);
//		dateStr="avril";parseDate(dateStr);
//		dateStr="sept.-dec. 1996";parseDate(dateStr);
//		dateStr="déc. 1996";parseDate(dateStr);
//		dateStr="de 2002 a 06";parseDate(dateStr);
//		dateStr="années 1990";parseDate(dateStr);
//		dateStr="1977-85";parseDate(dateStr);
//		dateStr="2002-3";parseDate(dateStr);
//		dateStr="2010";parseDate(dateStr);
//		dateStr="années 90";parseDate(dateStr);
//		dateStr="années '90";parseDate(dateStr);
//		dateStr="'83";parseDate(dateStr);
//	}
}
