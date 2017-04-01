package fr.univavignon.nerwip.processing.internal.modelless.wikipediadater;

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
import java.util.List;
import java.util.regex.Pattern;

/**
 * List of patterns used by WikipediaDater for the
 * English language.
 * <br/>
 * It handles the following forms:
 * <ul>
 * 		<li>{@code DD MMMM YYYY} ex: 20 April 1889</li>
 * 		<li>{@code DD MM YYYY} ex: 5 Sep 1887</li>
 * 		<li>{@code DD MMMM, YYYY} ex: 24 December, 1822</li>
 * 		<li>{@code DDth of MMMM YYYY} ex: 10th of April 2004</li>
 * 		<li>{@code MMMM DD, YYYY} ex: May 30, 1914</li>
 * 
 * 		<li>{@code MMMM DD} ex: October 6</li>
 * 		<li>{@code DD MMMM} ex: 6 October</li>
 * 		<li>{@code DDth MMMM} ex: 14 September</li>
 * 		<li>{@code DD of MMMM} ex: 10th of April</li>
 * 
 * 		<li>{@code early/mid/late MMMM, YYYY} ex: late April 1968</li>
 * 		<li>{@code early/mid/late MMMM of YYYY} ex: late April of 1968</li>
 * 		<li>{@code MMMM YYYY} ex: October 1926</li>
 * 		<li>{@code MM YYYY} ex: Dec 1996</li>
 * 		<li>{@code MMMM, YYYY} ex: May, 1977</li>
 * 		<li>{@code MMMM of YYYY} ex: April of 1968</li>
 * 
 * 		<li>{@code early/mid/late MMMM} ex: late May</li>
 * 		<li>{@code MMMM} ex: January</li>
 * 
 * 		<li>{@code dddddddddddd YYYY} ex: May Day 2001</li>
 * 
 * 		<li>{@code early/mid/late YYY0s} ex: early 1990s</li>
 * 		<li>{@code YYYY} ex: 1977</li>
 * 		<li>{@code 'YY} ex: '96</li>
 * 		<li>{@code YYY0s} ex: 1990s</li>
 * 
 * 		<li>{@code CCCC-century} ex: twentieth-century</li>
 * 		<li>{@code CCth-century} ex: 11th-century</li>
 * 		<li>{@code CCth century} ex: 15th century</li>
 * 
 * 		<li>{@code AAth anniversary} ex: 40th anniversary</li>
 * </ul>
 * It also recognizes certain forms of durations:
 * <ul>
 * 		<li>{@code MM-MM YYYY} ex: Feb-Jun 2005</li>
 * 		<li>{@code YYYY-YY} ex: 2006-07 (academic year, season)</li>
 * 		<li>{@code YYYY-YY} ex: 2002-3 (academic year, season)</li>
 * </ul>
 * 
 * @author Vincent Labatut
 */
public class WikipediaDaterPatternsEn
{
	/////////////////////////////////////////////////////////////////
	// PATTERNS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Represents various forms of hyphens (or equivalent characters) */
	private static final String EXPR_HYPHEN = "(-|–|/)";
	/** Qualifies a following date */
	private static final String EXPR_QUALIFIER = "((early|mid|late)("+EXPR_HYPHEN+"| ))";
	/** Represents a year of the form 1981 */
	private static final String EXPR_YEAR_FULL = "((1\\d{3})|(20\\d{2}))";
	/** Represents a year of the form 77 */
	private static final String EXPR_YEAR_SHORT = "(\\d{2})";
	/** Represents a decade of the form 1990s */
	private static final String EXPR_DECADE_FULL = "((1\\d|20)\\d0s)";
	/** Represents a decade of the form 90s */
	private static final String EXPR_DECADE_SHORT = "(\\d0s)";
	/** Represents a month of the form January (upper case) */
	private static final String EXPR_MONTH_LONG_UPPER = "(January|February|March|April|May|June|July|August|September|October|November|December)";
	/** Represents a month of the form january (lower case)*/
	private static final String EXPR_MONTH_LONG_LOWER = "(january|february|march|april|may|june|july|august|september|october|november|december)";
	/** Represents a month of the form January or january (any case) */
	private static final String EXPR_MONTH_LONG_BOTH = "("+EXPR_MONTH_LONG_UPPER+"|"+EXPR_MONTH_LONG_LOWER+")";
	/** Represents a month of the form jan */
	private static final String EXPR_MONTH_SHORT = "((j|J)an|(f|F)eb|(m|M)ar|(a|A)pr|(m|M)ay|(j|J)un|(j|J)ul|(a|A)ug|(s|S)ep|(o|O)ct|(n|N)ov|(d|D)ec)";
//	/** Represents a month of the form 01 */
//	private static final String EXPR_MONTH_INT = "((0?\\d)|(1(0|1|2))";
	/** Represents a day of the form 31 */
	private static final String EXPR_DAY_INT = "(((0|1|2)?\\d)|30|31)";
	/** Represents a day of the form 31st */
	private static final String EXPR_DAY_ORDINAL = "(((((0|1|2)?[3-9])|10|11|12|20|30)th)|((0?2|22|32)nd)|((0?1|21|31)st))"; 
	/** Represents a century of the form eleventh century */
	private static final String EXPR_CENTURY_LONG = "(((((t|T)en|(e|E)leven|(t|T)welf|(t|T)hirteen|(f|F)ourteen|(f|F)ifteen|(s|S)ixteen|(s|S)eventeen|(e|E)ighteen|(n|N)ineteen|(t|T)wentie)th)|(t|T)wenty("+EXPR_HYPHEN+"| )first)( |"+EXPR_HYPHEN+")(c|C)entury)";
	/** Represents a century of the form 11th century */
	private static final String EXPR_CENTURY_SHORT = "((((1\\d|20)th)|21st)( |"+EXPR_HYPHEN+")(c|C)entury)";
	/** Represents an anniversary, of the form 40th anniversary */
	private static final String EXPR_ANNIVERSARY_SHORT = "((\\d*(0th|1st|2nd|3rd|[4-9]th))( |"+EXPR_HYPHEN+")(a|A)nniversary)";
	/** Represents special days such as religious fests, etc. */
	private static final String EXPR_SPECIAL_DAY ="(((M|m)ay (D|d)ay)|((C|c)hristmas (D|d)ay)|((N|n)ew (Y|y)ear's (D|d)ay)|(9/11))";
	
	/** List of patterns used to detect dates based on the previous regexps */
	protected static final List<Pattern> PATTERNS = Arrays.asList(
		// "late May, 2010" or "late May 2010" or "late may, 2010" or "late may 2010"
		Pattern.compile("\\b"+EXPR_QUALIFIER+EXPR_MONTH_LONG_BOTH+",? "+EXPR_YEAR_FULL+"\\b"),
		// "late April of 1968" or "april of 1968"
		Pattern.compile("\\b"+EXPR_QUALIFIER+EXPR_MONTH_LONG_BOTH+" of "+EXPR_YEAR_FULL+"\\b"),
		// "late April" or "late april"
		Pattern.compile("\\b"+EXPR_QUALIFIER+EXPR_MONTH_LONG_BOTH+"\\b"),
		
		// "early 1990s"
		Pattern.compile("\\b"+EXPR_QUALIFIER+EXPR_DECADE_FULL+"\\b"),
		// "early 90s" or "early '90s"
		Pattern.compile("\\b"+EXPR_QUALIFIER+"'?"+EXPR_DECADE_SHORT+"\\b"),
		
		// "May Day 2001"
		Pattern.compile("\\b"+EXPR_SPECIAL_DAY+" "+EXPR_YEAR_FULL+"\\b"),
		// "May Day"
		Pattern.compile("\\b"+EXPR_SPECIAL_DAY),

		// "twentieth-century" or "twentieth century"
		Pattern.compile("\\b"+EXPR_CENTURY_LONG+"\\b"),
		// "11th-century" or "11th century"
		Pattern.compile("\\b"+EXPR_CENTURY_SHORT+"\\b"),
		
		// "40th-anniversary" or "40th anniversary"
		Pattern.compile("\\b"+EXPR_ANNIVERSARY_SHORT+"\\b"),
		
		// "18-20 April 1889" or "18-20 April, 1889" or "18-20 april 1889" or "18-20 april, 1889" 
		Pattern.compile("\\b"+EXPR_DAY_INT+EXPR_HYPHEN+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+",? "+EXPR_YEAR_FULL+"\\b"),
		// "4-6 October" or "4-6 october"
		Pattern.compile("\\b"+EXPR_DAY_INT+EXPR_HYPHEN+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+"\\b"),
		// "12 to 21 August, 2013" or "12 to 21 august, 2013" or "12 to 21 August 2013" or "12 to 21 august 2013"
		Pattern.compile("\\b"+EXPR_DAY_INT+" to "+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+",? "+EXPR_YEAR_FULL+"\\b"),
		// "12 to 21 August" or "12 to 21 august"
		Pattern.compile("\\b"+EXPR_DAY_INT+" to "+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+"\\b"),
		
		// "20 April 1889" or "20 April, 1889" or "20 april 1889" or "20 april, 1889" 
		Pattern.compile("\\b"+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+",? "+EXPR_YEAR_FULL+"\\b"),
		// "5 Sep 1887" or "5 sep 1887"
		Pattern.compile("\\b"+EXPR_DAY_INT+" "+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b"),
		// "4 of October, 1975" or "4 of october, 1975" or "4 of October 1975" or "4 of october 1975"
		Pattern.compile("\\b"+EXPR_DAY_INT+" of "+EXPR_MONTH_LONG_BOTH+",? "+EXPR_YEAR_FULL+"\\b"),
		// "6 October" or "6 october"
		Pattern.compile("\\b"+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+"\\b"),

		// "10th of April 2004" or "10th of april 2004"
		Pattern.compile("\\b"+EXPR_DAY_ORDINAL+" of "+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "10th of April" or "10th of april"
		Pattern.compile("\\b"+EXPR_DAY_ORDINAL+" of "+EXPR_MONTH_LONG_BOTH+"\\b"),
		
		// "October 25–26, 1821" or "october 25–26, 1821" or "October 25–26 1821" or "october 25–26 1821"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+" "+EXPR_DAY_INT+EXPR_HYPHEN+EXPR_DAY_INT+",? "+EXPR_YEAR_FULL+"\\b"),
		// "March 6 and 8, 1918" or "march 6 and 8, 1918" or "March 6 and 8 1918" or "march 6 and 8 1918"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+" "+EXPR_DAY_INT+" and "+EXPR_DAY_INT+",? "+EXPR_YEAR_FULL+"\\b"),
		
		// "May 30, 1914" or "may 30, 1914" or "May 30 1914" or "may 30 1914"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+" "+EXPR_DAY_INT+",? "+EXPR_YEAR_FULL+"\\b"),
		// "December 4 of 1922" or "december 4 of 1922"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+" "+EXPR_DAY_INT+" of "+EXPR_YEAR_FULL+"\\b"),
		// "December 9th 2010" or "december 9th 2010" or "December 9th, 2010" or "december 9th, 2010"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+" "+EXPR_DAY_ORDINAL+",? "+EXPR_YEAR_FULL+"\\b"),
		// "February the 20th of 2010" or "february the 20th of 2010"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+" the "+EXPR_DAY_ORDINAL+" of "+EXPR_YEAR_FULL+"\\b"),
		// "October 6" or "october 6"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+" "+EXPR_DAY_INT+"\\b"),
		
		// "September-December 1996" or "september-december 1996" or "September-december 1996" or "september-December 1996"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+EXPR_HYPHEN+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "October, 1926" or "October 1926" or "october, 1926" or "october 1926"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+",? "+EXPR_YEAR_FULL+"\\b"),
		// "April of 1968" or "april of 1968"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+" of "+EXPR_YEAR_FULL+"\\b"),
		// "April"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_UPPER+"\\b"),
		
		// "Sep-Dec 1996" or "sep-dec 1996" or "Sep-dec 1996" or "sep-Dec 1996"
		Pattern.compile("\\b"+EXPR_MONTH_SHORT+EXPR_HYPHEN+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b"),
		// "Dec 1996" or "dec 1996"
		Pattern.compile("\\b"+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b"),
		
		// "from 2002 to 06"
		Pattern.compile("\\bfrom "+EXPR_YEAR_FULL+" to "+EXPR_YEAR_SHORT+"\\b"),

		// "1990s"
		Pattern.compile("\\b"+EXPR_DECADE_FULL+"\\b"),
		// "1977-85"
		Pattern.compile("\\b"+EXPR_YEAR_FULL+EXPR_HYPHEN+EXPR_YEAR_SHORT+"\\b"),
		// "2002-3"
		Pattern.compile("\\b"+EXPR_YEAR_FULL+EXPR_HYPHEN+"\\d\\b"),
		// "2010"
		Pattern.compile("\\b"+EXPR_YEAR_FULL+"\\b"),
		
		// "90s" or "'90s"
		Pattern.compile("(\\b|')"+EXPR_DECADE_SHORT+"\\b"),
		// "'83"
		Pattern.compile("'"+EXPR_YEAR_SHORT+"\\b")
	);
}
