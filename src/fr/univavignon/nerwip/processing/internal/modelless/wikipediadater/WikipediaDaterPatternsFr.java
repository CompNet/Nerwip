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
 * 		<li>{@code DD MMMM YYYY} ex: 20 avril 1889</li>
 * 		<li>{@code DD MM YYYY} ex: 5 fév 1887</li>
 * 
 * 		<li>{@code MMMM DD} ex: 6 octobre</li>
 * 
 * 		<li>{@code MMMM YYYY} ex: octobre 1926</li>
 * 		<li>{@code MM YYYY} ex: déc 1996</li>
 * 		<li>{@code early/mid/late MMMM, YYYY} ex: fin avril 1968</li>
 * 
 * 		<li>{@code MMMM} ex: janvier</li>
 * 		<li>{@code early/mid/late MMMM} ex: fin mai</li>
 * 
 * 		<li>{@code dddddddddddd YYYY} ex: Noël 2001</li>
 * 
 * 		<li>{@code début/mi/fin YYYY} ex: fin 1977</li>
 * 		<li>{@code début/mi/fin YY} ex: fin 77</li>
 * 		<li>{@code YYYY} ex: 1977</li>
 * 		<li>{@code 'YY} ex: '96</li>
 * 
 * 		<li>{@code début/milieu/fin des années YYY0} ex: début des années 1990</li>
 * 		<li>{@code début/milieu/fin des années Y0} ex: début des années 90</li>
 * 		<li>{@code années YYY0s} ex: années 1990</li>
 * 		<li>{@code années Y0} ex: années 90</li>
 * 
 * 		<li>{@code CCCC siècle} ex: vingtième siècle</li>
 * 		<li>{@code CCème siècle} ex: 20ème siècle</li>
 * 		<li>{@code CCème siècle} ex: XXème siècle</li>
 * 		<li>{@code CC siècle} ex: XX siècle</li>
 * 
 * 		<li>{@code AAth anniversary} ex: 40th anniversary</li>
 * </ul>
 * It also recognizes certain forms of durations:
 * <ul>
 * 		<li>{@code YYYY-YY} ex: 2006-07 (academic year, season)</li>
 * 		<li>{@code YYYY-YY} ex: 2002-3 (academic year, season)</li>
 * </ul>
 * 
 * @author Vincent Labatut
 */
public class WikipediaDaterPatternsFr
{
	/////////////////////////////////////////////////////////////////
	// PATTERNS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Represents various forms of hyphens (or equivalent characters) */
	private static final String EXPR_HYPHEN = "(-|–|/)";
	/** Qualifies a following date */
	private static final String EXPR_QUALIFIER = "((d(é|e)but|mi|milieu|fin)("+EXPR_HYPHEN+"| ))";
	/** Represents a year of the form 1981 */
	private static final String EXPR_YEAR_FULL = "((1\\d{3})|(20\\d{2}))";
	/** Represents a year of the form 77 */
	private static final String EXPR_YEAR_SHORT = "(\\d{2})";
	/** Represents a decade of the form 1990 */
	private static final String EXPR_DECADE_FULL = "((1\\d|20)\\d0s?)";
	/** Represents a decade of the form 90 */
	private static final String EXPR_DECADE_SHORT = "(\\d0s?)";
	/** Represents a month of the form Janvier (upper case) */
	private static final String EXPR_MONTH_LONG_UPPER = "(Janvier|F(é|e)vrier|Mars|Avril|Mai|Juin|Juillet|Ao(û|u)t|Septembre|Octobre|Novembre|D(é|e)cembre)";
	/** Represents a month of the form janvier (lower case)*/
	private static final String EXPR_MONTH_LONG_LOWER = "(janvier|f(é|e)vrier|mars|avril|mai|juin|juillet|ao(û|u)t|septembre|octobre|novembre|d(é|e)cembre)";
	/** Represents a month of the form Janvier or janvier (any case) */
	private static final String EXPR_MONTH_LONG_BOTH = "("+EXPR_MONTH_LONG_UPPER+"|"+EXPR_MONTH_LONG_LOWER+")";
	/** Represents a month of the form janv */
	private static final String EXPR_MONTH_SHORT = "((j|J)anv.?|(f|F)(é|e)vr.?|(m|M)ars|(a|A)vr.?|(m|M)ai|(j|J)uin|(j|J)uil.?|(a|A)o(û|u)t|(s|S)ept.?|(o|O)ct.?|(n|N)ov.?|(d|D)(é|e)c.?)";
//	/** Represents a month of the form 01 */
//	private static final String EXPR_MONTH_INT = "((0?\\d)|(1(0|1|2))";
	/** Represents a day of the form 31 */
	private static final String EXPR_DAY_INT = "(((0|1|2)?\\d)|30|31)";
	/** Represents a day of the form 1er */
	private static final String EXPR_DAY_ORDINAL = "(1"+EXPR_HYPHEN+"?er|premier)"; 
	/** Represents a century of the form onzième siècle */
	private static final String EXPR_CENTURY_LONG = "(((d|D)ix|(o|O)nz|(d|D)ouz|(t|T)reiz|(q|Q)uatorz|(q|Q)uinz|(s|S)eiz|(d|D)ix-sept|(d|D)ix-huit|(d|D)ix-neuf|(v|V)ingt et un|(v|V)ingt-et-un|vingt)"+EXPR_HYPHEN+"?i(è|e)me (S|s)i(è|e)cle)";
	/** Represents a century of the form 11ème siècle */
	private static final String EXPR_CENTURY_SHORT = "(((1\\d|21)|(XVIII|XVII|XIIX|XIII|XIX|XIV|XII|XXI|XVI|XV|XI|XX|X))"+EXPR_HYPHEN+"?i?(è|e)(me)? (S|s)i(è|e)cle)";
	/** Represents an anniversary, of the form 40ème anniversaire */
	private static final String EXPR_ANNIVERSARY_SHORT = "((\\d*"+EXPR_HYPHEN+"?i?(è|e)me) (a|A)nniversaire)";
	/** Represents special days such as religious fests, etc. */
	private static final String EXPR_SPECIAL_DAY ="(((j|J)our de l'(a|A)n)|(((L|l)undi de )?(P|p)(â|a)ques)|((F|f)(ê|e)te du (t|T)ravail)|((A|a)scension)|((P|p)entec(ô|o)te)|((T|t)ransfiguration)|((A|a)ssomption)|((T|t)oussaint)|((N|n)o(e|ë)l)|(((S|s)(t|aint)("+EXPR_HYPHEN+"| )(S|s)ylvestre)))";
	
	/** List of patterns used to detect dates based on the previous regexps */
	protected static final List<Pattern> PATTERNS = Arrays.asList(
		// "fin Mai 2010" or "fin mai 2010"
		Pattern.compile("\\b"+EXPR_QUALIFIER+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "fin Avril" or "fin avril"
		Pattern.compile("\\b"+EXPR_QUALIFIER+EXPR_MONTH_LONG_BOTH+"\\b"),
		
		// "début des années 1990"
		Pattern.compile("\\b"+EXPR_QUALIFIER+"des ann(é|e)es "+EXPR_DECADE_FULL+"\\b"),
		// "début des années 90" or "début des années '90"
		Pattern.compile("\\b"+EXPR_QUALIFIER+"des ann(é|e)es '?"+EXPR_DECADE_SHORT+"\\b"),
		
		// "Noël 2001"
		Pattern.compile("\\b"+EXPR_SPECIAL_DAY+" "+EXPR_YEAR_FULL+"\\b"),
		// "Noël"
		Pattern.compile("\\b"+EXPR_SPECIAL_DAY),

		// "vingtième siècle" or "vingtième Siecle"
		Pattern.compile("\\b"+EXPR_CENTURY_LONG+"\\b"),
		// "11ème siècle" or "XIème siècle"
		Pattern.compile("\\b"+EXPR_CENTURY_SHORT+"\\b"),
		
		// "40th-anniversary" or "40th anniversary"
		Pattern.compile("\\b"+EXPR_ANNIVERSARY_SHORT+"\\b"),
		
		// "18-20 April 1889" or or "18-20 april 1889"
		Pattern.compile("\\b"+EXPR_DAY_INT+EXPR_HYPHEN+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "1er-20 April 1889" or or "1er-20 april 1889"
		Pattern.compile("\\b"+EXPR_DAY_ORDINAL+EXPR_HYPHEN+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "4-6 Octobre" or "4-6 octobre"
		Pattern.compile("\\b"+EXPR_DAY_INT+EXPR_HYPHEN+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+"\\b"),
		// "1er-6 Octobre" or "1er-6 octobre"
		Pattern.compile("\\b"+EXPR_DAY_ORDINAL+EXPR_HYPHEN+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+"\\b"),
		// "du 12 au 21 août 2013" or "du 12 au 21 Août 2013"
		Pattern.compile("\\bdu "+EXPR_DAY_INT+" au "+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "du 1er au 21 août 2013" or "du 1er au 21 Août 2013"
		Pattern.compile("\\bdu "+EXPR_DAY_ORDINAL+" au "+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "du 12 au 21 Août" or "du 12 au 21 août"
		Pattern.compile("\\bdu "+EXPR_DAY_INT+" au "+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+"\\b"),
		// "du 1er au 21 Août" or "du 1er au 21 août"
		Pattern.compile("\\bdu "+EXPR_DAY_ORDINAL+" au "+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+"\\b"),
		
		// "20 Avril 1889" or or "20 avril 1889" 
		Pattern.compile("\\b"+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "1er Avril 1889" or or "1er avril 1889" 
		Pattern.compile("\\b"+EXPR_DAY_ORDINAL+" "+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "5 Sept. 1887" or "5 sept. 1887"
		Pattern.compile("\\b"+EXPR_DAY_INT+" "+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b"),
		// "1er Sept. 1887" or "1er sept. 1887"
		Pattern.compile("\\b"+EXPR_DAY_ORDINAL+" "+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b"),
		// "6 Octobre" or "6 octobre"
		Pattern.compile("\\b"+EXPR_DAY_INT+" "+EXPR_MONTH_LONG_BOTH+"\\b"),
		// "1er Octobre" or "1er octobre"
		Pattern.compile("\\b"+EXPR_DAY_ORDINAL+" "+EXPR_MONTH_LONG_BOTH+"\\b"),

		// "de Septembre à Décembre 1996" or "de septembre à décembre 1996" or "de Septembre à décembre 1996" or "de septembre à Décembre 1996"
		Pattern.compile("\\bd(e |')"+EXPR_MONTH_LONG_BOTH+" (à|a) "+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "Septembre-Décembre 1996" or "septembre-décembre 1996" or "Septembre-décembre 1996" or "septembre-Décembre 1996"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+EXPR_HYPHEN+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// ""Octobre 1926" or "octobre 1926"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_BOTH+" "+EXPR_YEAR_FULL+"\\b"),
		// "April"
		Pattern.compile("\\b"+EXPR_MONTH_LONG_UPPER+"\\b"),
		
		// "Sept.-Déc. 1996" or "sept.-déc. 1996" or "Sept.-déc. 1996" or "sept.-Déc. 1996"
		Pattern.compile("\\b"+EXPR_MONTH_SHORT+EXPR_HYPHEN+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b"),
		// "Dec 1996" or "dec 1996"
		Pattern.compile("\\b"+EXPR_MONTH_SHORT+" "+EXPR_YEAR_FULL+"\\b"),
		
		// "de 2002 à 06"
		Pattern.compile("\\bde "+EXPR_YEAR_FULL+" (à|a) "+EXPR_YEAR_SHORT+"\\b"),

		// "années 1990"
		Pattern.compile("\\bann(é|e)es "+EXPR_DECADE_FULL+"\\b"),
		// "1977-85"
		Pattern.compile("\\b"+EXPR_YEAR_FULL+EXPR_HYPHEN+EXPR_YEAR_SHORT+"\\b"),
		// "2002-3"
		Pattern.compile("\\b"+EXPR_YEAR_FULL+EXPR_HYPHEN+"\\d\\b"),
		// "2010"
		Pattern.compile("\\b"+EXPR_YEAR_FULL+"\\b"),
		
		// "années 90s" or "années '90s"
		Pattern.compile("\\bann(é|e)s '?"+EXPR_DECADE_SHORT+"\\b"),
		// "'83"
		Pattern.compile("'"+EXPR_YEAR_SHORT+"\\b")
	);
}
