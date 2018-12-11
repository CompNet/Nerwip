package fr.univavignon.tools.strings;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

/**
 * This class contains various methods used when processing strings.
 * <br/>
 * We consider a text to be clean if it contains only Latin letters
 * (possibly with diacritics), digits, punctuation as defined in
 * {@link #PUNCTUATION}, regular spaces and new lines {@code '\n'}.
 * This holds for an article body: in addition, an article title must
 * not contain new lines or double quotes {@code '"'}, so that
 * we can include it in CSV files.
 *  
 * @author Vincent Labatut
 */
public class StringTools
{
	/**
	 * Tests various methods of this class.
	 * 
	 * @param args
	 * 		No need.
	 * 
	 * @throws Exception
	 * 		Whatever exception was thrown. 
	 */
	public static void main(String[] args) throws Exception
	{	
		// test split
//		String text = "This is a first sentence. Cela semble marcher très bien."
//			+ "What if no space after dot? Or even other punctuation marks!\n"
//			+ "Et même plein de points !?! Ou... des nombres 12.32 et 12,65.\n"
//			+ "On pourrait aussi avoir des abréviations comme M.Dupont ou M. Dupont ; "
//			+ "enfin, there could be spaces and stuff in between sentences.   Like this.  End.";
//		splitText(text, 70);
		
		// sentence positions
//		List<Integer> pos = getSentencePositions(text);
//		System.out.println(text);
//		Iterator<Integer> it = pos.iterator();
//		int current = it.next();
//		for(int i=0;i<text.length();i++)
//		{	if(current==i)
//			{	if(it.hasNext())
//					current = it.next();
//				System.out.print("^");
//			}
//			else
//				System.out.print(" ");
//		}
		
		// test clean
//		String str = " abc\u00A0defg h\ni\rk\tl";
//		String res = replaceSpaces(str);
//		System.out.println("\""+str+"\" vs \""+res+"\"");
//		System.out.println((int)(str.charAt(0))+" vs "+(int)(res.charAt(0)));
//		System.out.println((int)(str.charAt(4))+" vs "+(int)(res.charAt(4)));
//		System.out.println((int)(str.charAt(9))+" vs "+(int)(res.charAt(9)));
//		System.out.println((int)(str.charAt(11))+" vs "+(int)(res.charAt(11)));
//		System.out.println((int)(str.charAt(13))+" vs "+(int)(res.charAt(13)));
//		System.out.println((int)(str.charAt(15))+" vs "+(int)(res.charAt(15)));
		
		// test expr
//		List<Integer> list = extractValues("1,2,3,8,9,10-18,56,98,2,6,8");
//		System.out.println(list);
		
		// test rtl cleaning
//		String str = removeNonLatinChars("sdsfsd fتثتqsdsq");
//		System.out.println(str);
		
		// non-latin character removal
//		removeNonLatinLetters("Super Mario Bros. Anime Movie Restored (Best Quality!) . English subbed . スーパーマリオブラザーズ ピーチ姫救出大作戦!");
		
		// clean spaces
//		String res = cleanSpaces("fdssd\n dsfsdf\nsd dsf sdfsd fdsf    sdfsdf  sdfsd\n\n\nsdfsdf");
//		System.out.println(res);
		
		// test clean text
		String text = "zeriou fke ? R dfikalnfsd po ! SZ : dsqd 4485. Fio 89% dezidj, defsoui ; ezrofd 98% fdskds !!";
		String cleaned = cleanText(text,Locale.FRENCH);
		System.out.println(cleaned);
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	public static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// ET			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map used to replaced "&" by the appropriate full word */
	private static final Map<Locale,String> ET_MAP = new HashMap<Locale,String>();
	static
	{	ET_MAP.put(Locale.ENGLISH,"and");
		ET_MAP.put(Locale.FRENCH, "et");
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** 
	 * Compares two strings while ignoring their case as well as diacritics.
	 * This method is meant to be applied on <i>clean</i> text.
	 * <br/>
	 * It is currently used to compare the surface forms of entities.
	 * It is also used by the method {@link #compareCharsRelaxed(int, int)},
	 * which is itself use during the post-processing of Nero results.
	 */
	public static final Comparator<String> COMPARATOR = new Comparator<String>()
	{	@Override
		public int compare(String s1, String s2)
		{	// remove accents
			String string1 = removeDiacritics(s1);
			String string2 = removeDiacritics(s2);
			
			// remove case
			string1 = string1.toUpperCase(Locale.ENGLISH);
			string2 = string2.toUpperCase(Locale.ENGLISH);
			
			// normalize double quotes
			// >> there should be none of them after cleaning, now
//			string1 = string1.replaceAll("«", "\"");
//			string2 = string2.replaceAll("«", "\"");
//			string1 = string1.replaceAll("»", "\"");
//			string2 = string2.replaceAll("»", "\"");
			
			// compare
			int result = string1.compareTo(string2);
			return result;
		}	
	};
	
	/**
	 * Compare the specified characters, using {@link #COMPARATOR},
	 * i.e. ignoring case and diacritics. This method is meant to be
	 * applied to <i>clean</i> text.
	 * 
	 * @param c1
	 * 		First character to compare.
	 * @param c2
	 * 		Second character to compare.
	 * @return
	 * 		Integer representing a classic comparison result.
	 */
	public static int compareCharsRelaxed(int c1, int c2)
	{	String s1 = new String(new int[]{c1},0,1);
		String s2 = new String(new int[]{c2},0,1);
		
		int result = COMPARATOR.compare(s1, s2);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PUNCTUATION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Accepted punctuation marks, in a clean text */
	private final static String PUNCTUATION = "'():,\\-!?.\";&@%+";
	
	/**
	 * Checks whether the specified character is a punctuation mark or not.
	 * This holds only for <i>clean</i> texts (cf. the class documentation).
	 * 
	 * @param c
	 * 		The character of interest.
	 * @return
	 * 		{@code true} iff the character is a punctuation mark.
	 */
	public static boolean isPunctuation(int c)
	{	int pos = PUNCTUATION.indexOf(c);
		boolean result = pos>=0;
		
		return result;
	}
	
	/**
	 * Removes the punctuation as defined in {@link #PUNCTUATION}. It also
	 * removes multiple consecutive regular spaces. The goal is to get a string
	 * that is easy to tokenize.
	 * <br/>
	 * This method is meant to be applied on <i>clean</i> texts or titles 
	 * (it does not handle fancy characters).
	 * 
	 * @param str
	 * 		The text to process.
	 * @return
	 * 		The same text without any punctuation.
	 */
	public static String removePunctuation(String str)
	{	String result = str.replaceAll("["+PUNCTUATION+"]"," ");
		result = result.replaceAll(" +", " ");
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// INITIALS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Changes the case of the specified {@code String},
	 * so that the first letter is upper case and the rest is lower case.
	 * This can be used when handling proper names. The method does not
	 * check if the string contains several distinct words.
	 * 
	 * @param string
	 * 		String to process.
	 * @return
	 * 		Normalized string resulting of the change in cases.
	 */
	public static String initialize(String string)
	{	String first = string.substring(0,1);
		String rest = string.substring(1);
		String result = first.toUpperCase(Locale.ENGLISH) + rest.toLowerCase(Locale.ENGLISH);
		return result;
	}
	
	/**
	 * Checks if the specified string starts with an uppercase character.
	 * 
	 * @param string
	 * 		The string of interest.
	 * @return 
	 * 		{@code true} iff the string starts with an uppercase.
	 */
	public static boolean hasInitial(String string)
	{	char initial = string.charAt(0);
		boolean result = !Character.isLowerCase(initial); 
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// CLEAN			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of allowed "latin" characters, used when cleaning articles */
	private final static List<Character> LATIN_CHARS = new ArrayList<Character>();
	static
	{	String latinStr = " \n";
		// add lowercase letters
		latinStr = latinStr + "abcdefghijklmnopqrstuvwxyz";
		// add uppercase letters
		latinStr = latinStr + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		// add digits
		latinStr = latinStr + "0123456789";
		// add (accepted) punctuation
		latinStr = latinStr + PUNCTUATION;
		// add all these chars to the list
		for(int i=0;i<latinStr.length();i++)
		{	char c = latinStr.charAt(i);
			LATIN_CHARS.add(c);
		}
	}
	
	/**
	 * Cleans the specified string, in order to remove characters
	 * causing problems when detecting named entity mentions (or
	 * other processing).
	 * <br/>
	 * To summarize, we do the following:
	 * <ul>
	 * 	<li>Clean problems related to the variants of the space character, using {@link #cleanSpaces(String)}.</li>
	 * 	<li>Iteratively clean the text using {@link #cleanInnerText(String,Locale)}, until stability.</li>
	 * </ul>
	 *    
	 * @param input
	 * 		The string to process.
	 * @param language 
	 * 		Language of the considered text.
	 * @return
	 * 		Cleaned string.
	 */
	public static String cleanText(String input, Locale language)
	{	String output = input;
		
		if(input!=null)
		{	// clean spaces
			output = cleanSpaces(output);
			
			String previous = output;
			do
			{	previous = output;
				output = cleanInnerText(output,language);
			}
			while(!output.equals(previous));
			
			output = output.trim();
		}
		return output;
	}
	
	/**
	 * Clean some text taken from an article, and which is not a URL.
	 * This method is meant used only by {@link #cleanText(String,Locale)}.
	 * <br/>
	 * It involves numerous operations:
	 * <ul>
	 * 	<li>Replace all fancy whitespaces (except newlines, but including tabs) by regular spaces {@code ' '}.</li>
	 * 	<li>Replace all fancy newlines by {@code '\n'}.</li>
	 * 	<li>Replace the remaining multiple consecutive whitespaces by single ones.</li>
	 * 	<li>Remove spaces at the end of lines.</li>
	 * 	<li>Replace multiple consecutive punctuation marks by single ones.</li>
	 * 	<li>Remove spaces before dots.</li>
	 * 	<li>Remove various impossible combinations of punctuation marks, such as {@code "(;"} (which becomes {@code "("}.</li>
	 * 	<li>Replace ligatures by two characters, e.g. {@code "Æ"} becomes {@code "Ae"}.</li>
	 * 	<li>Normalize puntuation marks, e.g. all the variants of the hyphen are replaced by the regular {@code '-'}.</li>
	 * 	<li>Remove spaces located right before closing parentheses or right after opening ones.</li>
	 * 	<li>Replace 2 consecutive single quotes by a double quote.</li>
	 * 	<li>Remove empty double quotes and parentheses.</li>
	 * 	<li>Remove remaining characters which are neither punctuation, whitespaces, letters or digits.</li>
	 * 	<li>Remove all remaining non-latin letters, using method {@link #removeNonLatinLetters(String)}.</li>
	 * </ul>
	 * 
	 * @param input
	 * 		The text to clean.
	 * @param language
	 * 		Language of the considered text.
	 * @return
	 * 		The cleaned text.
	 */
	private static String cleanInnerText(String input, Locale language)
	{	String output = input;
		
		// replace all white spaces by regular spaces
		// new line and tabs are not affected
		output = output.replaceAll("\\p{Z}", " "); // \p{Z} includes more different whitespaces than \s
		// replace tabs by simple spaces
		output = output.replaceAll("\\t", " ");
		
		// replace multiple consecutive spaces by a single one 
		output = output.replaceAll("( )+", " ");

//		// normalize newlines
//		output = output.replaceAll("\\r", "\n");
		// replace multiple consecutive newlines by a single one 
		output = output.replaceAll("\\n\\n+", "\n\n");
		
		// remove spaces at the end of lines 
		output = output.replaceAll(" \\n", "\n");
		
		// replace multiple space-separated punctuation marks by single ones 
//			output = output.replaceAll("; ;", ";");
//			output = output.replaceAll(", ,", ",");
//			output = output.replaceAll(": :", ":");
//			output = output.replaceAll("\\. \\.", "\\.");
		
		// replace multiple consecutive punctuation marks by a single one 
		output = output.replaceAll("([\\.,;:] )\\1", "$1");

		// remove spaces before dots 
		output = output.replaceAll(" \\.", ".");
		
		// remove various combinations of punctuation marks
		output = output.replaceAll("\\(;", "(");

		// adds a final dot when it is missing at the end of a sentence (itself detected thanks to the new line)
//			output = output.replaceAll("([^(\\.|\\-)])\\n", "$1.\n");
		
		// insert a space after a coma, when missing (pb when English numbers are present, like 3,000, or french ones like 12,34) 
//			output = output.replaceAll(",([^ _])", ", $1");

		// insert a space after a semi-column, when missing
//			output = output.replaceAll(";([^ _])", "; $1");
		
		// replace ligatures by two characters
		// note : the normalizer does not seem to work well for most ligatures
		// cf. http://stackoverflow.com/questions/7171377/separating-unicode-ligature-characters
		output = output.replaceAll("ꜳ", "aa");
		output = output.replaceAll("Ꜳ", "Aa");
		output = output.replaceAll("æ", "ae");
		output = output.replaceAll("Æ", "Ae");
		output = output.replaceAll("ꜵ", "ao");
		output = output.replaceAll("Ꜵ", "Ao");
		output = output.replaceAll("ꜷ", "au");
		output = output.replaceAll("Ꜷ", "Au");
		output = output.replaceAll("ꜹ", "av");
		output = output.replaceAll("Ꜹ", "Av");
		output = output.replaceAll("ꜻ", "av");
		output = output.replaceAll("Ꜻ", "Av");
		output = output.replaceAll("🙰", "et");
		output = output.replaceAll("ﬀ", "ff");
		output = output.replaceAll("ﬃ", "ffi");
		output = output.replaceAll("ﬄ", "ffl");
		output = output.replaceAll("ﬁ", "fi");
		output = output.replaceAll("ﬂ", "fl");
		output = output.replaceAll("œ", "oe");
		output = output.replaceAll("Œ", "Oe");
		output = output.replaceAll("ꝏ", "oo");
		output = output.replaceAll("Ꝏ", "Oo");
		output = output.replaceAll("ẞ", "ss");
		output = output.replaceAll("ß", "ss");
		output = output.replaceAll("ﬆ", "st");
		output = output.replaceAll("ﬅ", "st");
		output = output.replaceAll("Þ", "Th");
		output = output.replaceAll("þ", "th");
		output = output.replaceAll("ꜩ", "tz");
		output = output.replaceAll("Ꜩ", "Tz");
		output = output.replaceAll("ᵫ", "ue");
		output = output.replaceAll("Ꝡ", "Vy");
		output = output.replaceAll("ꝡ", "vy");
		// phonetic transcription
		output = output.replaceAll("ȸ", "db");
		output = output.replaceAll("ʣ", "dz");
		output = output.replaceAll("ʥ", "dz");
		output = output.replaceAll("ʤ", "dz");
		output = output.replaceAll("ʩ", "fn");
		output = output.replaceAll("Ĳ", "IJ");
		output = output.replaceAll("ĳ", "ij");
		output = output.replaceAll("ʪ", "ls");
		output = output.replaceAll("ʫ", "lz");
		output = output.replaceAll("ɮ", "lz");
		output = output.replaceAll("ȹ", "qp");
		output = output.replaceAll("ʨ", "tc");
		output = output.replaceAll("ʦ", "ts");
		output = output.replaceAll("ʧ", "ts");
		output = output.replaceAll("ꭐ", "ui");
		output = output.replaceAll("ꭑ", "ui");
		// digrapgs
		output = output.replaceAll("Ǳ", "DZ");
		output = output.replaceAll("ǲ", "Dz");
		output = output.replaceAll("ǳ", "dz");
		output = output.replaceAll("Ǆ", "DZ");
		output = output.replaceAll("ǅ", "Dz");
		output = output.replaceAll("ǆ", "dz");
		output = output.replaceAll("Ĳ", "IJ");
		output = output.replaceAll("ĳ", "ij");
		output = output.replaceAll("Ǉ", "LJ");
		output = output.replaceAll("ǈ", "Lj");
		output = output.replaceAll("ǉ", "lj");
		output = output.replaceAll("Ǌ", "NJ");
		output = output.replaceAll("ǋ", "Nj");
		output = output.replaceAll("ǌ", "nk");
		// look like diacritics, but are not considered as such
		// A
		output = output.replaceAll("Ⱥ", "A");
		output = output.replaceAll("ⱥ", "a");
		output = output.replaceAll("Ą́", "Á");
		output = output.replaceAll("ą́", "á");
		output = output.replaceAll("Ą̃", "A");
		output = output.replaceAll("ą̃", "a");
		output = output.replaceAll("Ā̀", "À");
		output = output.replaceAll("ā̀", "à");
		output = output.replaceAll("A̋", "A");
		output = output.replaceAll("a̋", "a");
		output = output.replaceAll("ᶏ", "a");
		// B
		output = output.replaceAll("Ƀ", "B");
		output = output.replaceAll("ƀ", "b");
		output = output.replaceAll("Ꞗ", "B");
		output = output.replaceAll("ꞗ", "b");
		output = output.replaceAll("ᵬ", "b");
		output = output.replaceAll("ᶀ", "b");
		// C
		output = output.replaceAll("Ȼ", "C");
		output = output.replaceAll("ȼ", "c");
		output = output.replaceAll("Ꞓ", "C");
		output = output.replaceAll("ꞓ", "c");
		output = output.replaceAll("Ƈ", "C");
		output = output.replaceAll("ƈ", "c");
		output = output.replaceAll("ɕ", "c");
		output = output.replaceAll("ꞔ", "c");
		// D
		output = output.replaceAll("Ɖ", "D");
		output = output.replaceAll("ɖ", "d");
		output = output.replaceAll("Đ", "D");
		output = output.replaceAll("đ", "d");
		output = output.replaceAll("D̦", "D");
		output = output.replaceAll("d̦", "d");
		output = output.replaceAll("Ð", "D");
		output = output.replaceAll("ð", "d");
		output = output.replaceAll("Ɗ", "D");
		output = output.replaceAll("ɗ", "d");
		output = output.replaceAll("ᵭ", "d");
		output = output.replaceAll("ᶁ", "d");
		output = output.replaceAll("ᶑ", "d");
		output = output.replaceAll("ȡ", "d");
		// E
		output = output.replaceAll("Ɇ", "E");
		output = output.replaceAll("ɇ", "e");
		output = output.replaceAll("Ê̄", "E");
		output = output.replaceAll("ê̄", "e");
		output = output.replaceAll("Ê̌", "E");
		output = output.replaceAll("ê̌", "e");
		output = output.replaceAll("Ė́", "E");
		output = output.replaceAll("ė́", "e");
		output = output.replaceAll("Ė̃", "E");
		output = output.replaceAll("ė̃", "e");
		output = output.replaceAll("Ę́", "E");
		output = output.replaceAll("ę́", "e");
		output = output.replaceAll("Ę̃", "E");
		output = output.replaceAll("ę̃", "e");
		output = output.replaceAll("E̋", "E");
		output = output.replaceAll("e̋", "e");
		output = output.replaceAll("E̩", "E");
		output = output.replaceAll("e̩", "e");
		output = output.replaceAll("È̩", "È");
		output = output.replaceAll("è̩", "è");
		output = output.replaceAll("É̩", "É");
		output = output.replaceAll("é̩", "é");
		output = output.replaceAll("ᶒ", "e");
		output = output.replaceAll("ⱸ", "e");
		output = output.replaceAll("ꬴ", "e");
		output = output.replaceAll("ꬳ", "e");
		output = output.replaceAll("ꬳ", "e");
		// F
		output = output.replaceAll("Ꞙ", "F");
		output = output.replaceAll("ꞙ", "f");
		output = output.replaceAll("Ƒ", "F");
		output = output.replaceAll("ƒ", "f");
		output = output.replaceAll("ᵮ", "f");
		output = output.replaceAll("ᶂ", "f");
		// G
		output = output.replaceAll("Ꞡ", "G");
		output = output.replaceAll("ꞡ", "g");
		output = output.replaceAll("Ǥ", "G");
		output = output.replaceAll("ǥ", "g");
		output = output.replaceAll("G̃", "G");
		output = output.replaceAll("g̃", "g");
		output = output.replaceAll("Ɠ", "G");
		output = output.replaceAll("ɠ", "g");
		output = output.replaceAll("ᶃ", "g");
		output = output.replaceAll("ꬶ", "g");
		// H
		output = output.replaceAll("Ħ", "H");
		output = output.replaceAll("ħ", "h");
		output = output.replaceAll("H̱", "H");
		output = output.replaceAll("ẖ", "h");
		output = output.replaceAll("Ⱨ", "H");
		output = output.replaceAll("ⱨ", "h");
		output = output.replaceAll("Ɦ", "H");
		output = output.replaceAll("ɦ", "h");
		output = output.replaceAll("ꞕ", "h");
		// I
		output = output.replaceAll("Ɨ", "I");
		output = output.replaceAll("ɨ", "i");
		output = output.replaceAll("i̇́", "i");
		output = output.replaceAll("i̇̀", "i");
		output = output.replaceAll("i̇̃", "i");
		output = output.replaceAll("Į́", "i");
		output = output.replaceAll("į̇́", "i");
		output = output.replaceAll("Į̃", "i");
		output = output.replaceAll("į̇̃", "i");
		output = output.replaceAll("Ī̀", "I");
		output = output.replaceAll("ī̀", "i");
		output = output.replaceAll("I̋", "I");
		output = output.replaceAll("i̋", "i");
		output = output.replaceAll("Ɨ", "I");
		output = output.replaceAll("ɨ", "i");
		output = output.replaceAll("İ", "I");
		output = output.replaceAll("ᶖ", "i");
		output = output.replaceAll("ı", "i");
		// J
		output = output.replaceAll("Ɉ", "J");
		output = output.replaceAll("ɉ", "j");
		output = output.replaceAll("J̌", "J");
		output = output.replaceAll("ǰ", "j");
		output = output.replaceAll("ȷ", "j");
		output = output.replaceAll("Ʝ", "J");
		output = output.replaceAll("ʝ", "j");
		output = output.replaceAll("j̇̃", "j");
		output = output.replaceAll("ɟ", "j");
		output = output.replaceAll("ʄ", "j");
		// K
		output = output.replaceAll("Ꝃ", "K");
		output = output.replaceAll("ꝃ", "k");
		output = output.replaceAll("Ꞣ", "K");
		output = output.replaceAll("ꞣ", "k");
		output = output.replaceAll("Ꝁ", "K");
		output = output.replaceAll("ꝁ", "k");
		output = output.replaceAll("Ꝅ", "K");
		output = output.replaceAll("ꝅ", "k");
		output = output.replaceAll("Ƙ", "K");
		output = output.replaceAll("ƙ", "k");
		output = output.replaceAll("Ⱪ", "K");
		output = output.replaceAll("ⱪ", "k");
		output = output.replaceAll("ᶄ", "k");
		// L
		output = output.replaceAll("Ƚ", "L");
		output = output.replaceAll("ƚ", "l");
		output = output.replaceAll("Ⱡ", "L");
		output = output.replaceAll("ⱡ", "l");
		output = output.replaceAll("Ꝉ", "L");
		output = output.replaceAll("ꝉ", "l");
		output = output.replaceAll("Ł", "L");
		output = output.replaceAll("ł", "l");
		output = output.replaceAll("ᴌ", "l");
		output = output.replaceAll("L̃", "L");
		output = output.replaceAll("l̃", "l");
		output = output.replaceAll("Ɫ", "L");
		output = output.replaceAll("ɫ", "l");
		output = output.replaceAll("Ɬ", "L");
		output = output.replaceAll("ɬ", "l");
		output = output.replaceAll("ꞎ", "l");
		output = output.replaceAll("ꬷ", "l");
		output = output.replaceAll("ꬸ", "l");
		output = output.replaceAll("ꬹ", "l");
		output = output.replaceAll("ᶅ", "l");
		output = output.replaceAll("ɭ", "l");
		output = output.replaceAll("ȴ", "l");
		// M
		output = output.replaceAll("M̋", "M");
		output = output.replaceAll("m̋", "m");
		output = output.replaceAll("M̃", "M");
		output = output.replaceAll("m̃", "m");
		output = output.replaceAll("ᵯ", "m");
		output = output.replaceAll("ᶆ", "m");
		output = output.replaceAll("Ɱ", "M");
		output = output.replaceAll("ɱ", "m");
		output = output.replaceAll("ꬺ", "m");
		// N
		output = output.replaceAll("Ꞥ", "N");
		output = output.replaceAll("ꞥ", "n");
		output = output.replaceAll("N̈", "N");
		output = output.replaceAll("n̈", "n");
		output = output.replaceAll("Ɲ", "N");
		output = output.replaceAll("ɲ", "n");
		output = output.replaceAll("Ŋ", "N");
		output = output.replaceAll("ŋ", "n");
		output = output.replaceAll("Ꞑ", "N");
		output = output.replaceAll("ꞑ", "n");
		output = output.replaceAll("ᵰ", "n");
		output = output.replaceAll("ᶇ", "n");
		output = output.replaceAll("ɳ", "n");
		output = output.replaceAll("ȵ", "n");
		output = output.replaceAll("ꬻ", "n");
		output = output.replaceAll("ꬼ", "n");
		// O
		output = output.replaceAll("Ɵ", "O");
		output = output.replaceAll("ɵ", "o");
		output = output.replaceAll("Ꝋ", "O");
		output = output.replaceAll("ꝋ", "o");
		output = output.replaceAll("Ø", "O");
		output = output.replaceAll("ø", "o");
		output = output.replaceAll("O͘", "O");
		output = output.replaceAll("o͘", "o");
		output = output.replaceAll("Ǿ", "Ó");
		output = output.replaceAll("ǿ", "ó");
		output = output.replaceAll("O̩", "Ó");
		output = output.replaceAll("o̩", "ó");
		output = output.replaceAll("Ò̩", "Ò");
		output = output.replaceAll("ò̩", "ò");
		output = output.replaceAll("Ó̩", "Ó");
		output = output.replaceAll("ó̩", "ó");
		output = output.replaceAll("Ꝍ", "O");
		output = output.replaceAll("ꝍ", "o");
		output = output.replaceAll("ⱺ", "o");
		// P
		output = output.replaceAll("Ᵽ", "P");
		output = output.replaceAll("ᵽ", "p");
		output = output.replaceAll("Ꝑ", "P");
		output = output.replaceAll("ꝑ", "p");
		output = output.replaceAll("Ƥ", "P");
		output = output.replaceAll("ƥ", "p");
		output = output.replaceAll("Ꝓ", "P");
		output = output.replaceAll("ꝓ", "p");
		output = output.replaceAll("Ꝕ", "P");
		output = output.replaceAll("ꝕ", "p");
		output = output.replaceAll("P̃", "P");
		output = output.replaceAll("p̃", "p");
		output = output.replaceAll("ᵱ", "p");
		output = output.replaceAll("ᶈ", "p");
		// Q
		output = output.replaceAll("Ꝙ", "Q");
		output = output.replaceAll("ꝙ", "q");
		output = output.replaceAll("Ꝗ", "Q");
		output = output.replaceAll("ꝗ", "q");
		output = output.replaceAll("ɋ", "q");
		output = output.replaceAll("ʠ", "q");
		// R
		output = output.replaceAll("Ꞧ", "R");
		output = output.replaceAll("ꞧ", "r");
		output = output.replaceAll("Ɍ", "R");
		output = output.replaceAll("ɍ", "r");
		output = output.replaceAll("R̃", "R");
		output = output.replaceAll("r̃", "r");
		output = output.replaceAll("Ɽ", "R");
		output = output.replaceAll("ɽ", "r");
		output = output.replaceAll("ᵲ", "r");
		output = output.replaceAll("ᶉ", "r");
		output = output.replaceAll("ꭉ", "r");
		// S
		output = output.replaceAll("Ꞩ", "S");
		output = output.replaceAll("ꞩ", "s");
		output = output.replaceAll("S̩", "S");
		output = output.replaceAll("s̩", "s");
		output = output.replaceAll("Ȿ", "S");
		output = output.replaceAll("ȿ", "s");
		output = output.replaceAll("ʂ", "s");
		output = output.replaceAll("ᶊ", "s");
		output = output.replaceAll("ᵴ", "s");
		// T
		output = output.replaceAll("Ⱦ", "T");
		output = output.replaceAll("ⱦ", "t");
		output = output.replaceAll("Ŧ", "T");
		output = output.replaceAll("ŧ", "t");
		output = output.replaceAll("Ƭ", "T");
		output = output.replaceAll("ƭ", "t");
		output = output.replaceAll("Ʈ", "T");
		output = output.replaceAll("ʈ", "t");
		output = output.replaceAll("T̈", "T");
		output = output.replaceAll("ẗ", "t");
		output = output.replaceAll("ᵵ", "t");
		output = output.replaceAll("ƫ", "t");
		output = output.replaceAll("ȶ", "t");
		// U
		output = output.replaceAll("Ʉ", "U");
		output = output.replaceAll("ʉ", "u");
		output = output.replaceAll("Ų́", "Ú");
		output = output.replaceAll("ų́", "ú");
		output = output.replaceAll("Ų̃", "Ũ");
		output = output.replaceAll("ų̃", "ũ");
		output = output.replaceAll("Ū̀", "Ù");
		output = output.replaceAll("ū̀", "ù");
		output = output.replaceAll("Ū́", "Ú");
		output = output.replaceAll("ū́", "ú");
		output = output.replaceAll("Ū̃", "Ũ");
		output = output.replaceAll("ū̃", "ũ");
		output = output.replaceAll("Ʉ", "U");
		output = output.replaceAll("ʉ", "u");
		output = output.replaceAll("Ꞹ", "U");
		output = output.replaceAll("ꞹ", "u");
		output = output.replaceAll("ᶙ", "u");
		output = output.replaceAll("ꭒ", "u");
		// V
		output = output.replaceAll("Ꝟ", "V");
		output = output.replaceAll("ꝟ", "v");
		output = output.replaceAll("Ʋ", "V");
		output = output.replaceAll("ʋ", "v");
		output = output.replaceAll("Ỽ", "V");
		output = output.replaceAll("ỽ", "v");
		output = output.replaceAll("ᶌ", "v");
		output = output.replaceAll("ⱱ", "v");
		output = output.replaceAll("ⱴ", "v");
		// W
		output = output.replaceAll("W̊", "W");
		output = output.replaceAll("ẘ", "w");
		output = output.replaceAll("Ⱳ", "W");
		output = output.replaceAll("ⱳ", "w");
		// X
		output = output.replaceAll("X́", "X");
		output = output.replaceAll("x́", "x");
		output = output.replaceAll("X̂", "X");
		output = output.replaceAll("x̂", "x");
		output = output.replaceAll("X̌", "X");
		output = output.replaceAll("x̌", "x");
		output = output.replaceAll("X̧", "X");
		output = output.replaceAll("x̧", "x");
		output = output.replaceAll("X̱", "X");
		output = output.replaceAll("x̱", "x");
		output = output.replaceAll("X̣", "X");
		output = output.replaceAll("x̣", "x");
		output = output.replaceAll("ᶍ", "x");
		// Y
		output = output.replaceAll("Ɏ", "Y");
		output = output.replaceAll("ɏ", "y");
		output = output.replaceAll("Y̊", "Y");
		output = output.replaceAll("ẙ", "y");
		output = output.replaceAll("Ƴ", "Y");
		output = output.replaceAll("ƴ", "y");
		output = output.replaceAll("Ỿ", "Y");
		output = output.replaceAll("ỿ", "y");
		// Z
		output = output.replaceAll("Ƶ", "Z");
		output = output.replaceAll("ƶ", "z");
		output = output.replaceAll("Ȥ", "Z");
		output = output.replaceAll("ȥ", "z");
		output = output.replaceAll("Ⱬ", "Z");
		output = output.replaceAll("ⱬ", "z");
		output = output.replaceAll("Ɀ", "Z");
		output = output.replaceAll("ɀ", "z");
		output = output.replaceAll("ᵶ", "z");
		output = output.replaceAll("ᶎ", "z");
		output = output.replaceAll("ʐ", "z");
		output = output.replaceAll("ʑ", "z");
		
		// misc chars
		output = output.replaceAll("²", "2");
		
		// replace certain punctuation marks (list of characters obtained from Wikipedia)
			// apostrophe and variants
			output = output.replaceAll("[’’ʼ`´ʹʻʽʾʿˈˊʹ΄՚᾽᾿′Ꞌꞌ＇︐︑՝‘‛❛❜]","'");
			// opening brackets
			output = output.replaceAll("[(\\[{❴〈⧼❬❰❮〈〈⸤⸤｢｢「⌜⸢⟦⌈⌊⟆⟓⟬⟮⦃⦅⦇⦉⦋⦏⦑⦓⦕⦗⧘⧚❨❪❲⁅⸦⸨〔〖〘〚【（［｛<]", "(");
			// closing brackets
			output = output.replaceAll("[)\\]}❵〉⧽❭❱❯〉〉⸥⸥｣｣」⌝⸣⟧⌉⌋⟅⟔⟭⟯⦄⦆⦈⦊⦌⦐⦒⦔⦖⦘⧙⧛❩❫❳⁆⸧⸩〕〗〙〛】）］｝>]", ")");
			// colons and variants
			output = output.replaceAll("[:：ː]",":");
			// comma and variants
			output = output.replaceAll("[,،⸲⸴⹁、﹐﹑，､‚❟]",",");
			// hyphens and variants \u2012 \u2013 \u2014 \u2015 \u2053
			output = output.replaceAll("[-‐‑֊᠆﹣－‒–—―⁓=*_/⁄∕／\\\\]","-");
			// ellipsis and variants
			output = output.replaceAll("[…᠁⋯⋰⋱︙⋮]","...");
			// exclamation mark and variants
			output = output.replaceAll("[ǃ‼⁈⁉⚠❕❗❢❣ꜝꜞꜟ﹗！🕴᥄]","!");
			// period and variants
			output = output.replaceAll("[⸼ּ	᛫．]",".");
			// centered dots and bullets
			output = output.replaceAll("[··•‧∘∙⋅⏺●◦⚫⦁⸰⸱⸳・ꞏ･𐄁]","-");
			// opening double quotes
			output = output.replaceAll("[«‹”‟⟪《『⸂⸄⸉⸌〝〟🙷｟❝❠]", "\"");	// NOTE: at first, we were removing also the space in xxx " xxx " to get xxx "xxx". but some opening " are used as closing ones, and inversely
			// closing double quotes
			output = output.replaceAll("[»›“„⟫》』⸃⸅⸊⸍〞🙸🙶｠❞⹂]", "\"");
			// question mark and variants
			output = output.replaceAll("[⁇﹖⁈⁉‽]","?");
			// semicolon and variants
			output = output.replaceAll("[;؛⁏፤；︔﹔⍮⸵;]",";");
		
		// replace space-separated & by the full word
		String repl = "/";
		if(language!=null)
			repl = ET_MAP.get(language);
		output = output.replaceAll(" & "," "+repl+" ");
		// remove the remaining & (not space-separated)
		output = output.replaceAll("&","-");
		
		// replace 2 consecutive single quotes by 1 double quote
		output = output.replaceAll("''+", "\"");
		// remove empty double quotes
		output = output.replaceAll("\"\"", "");
		
		// remove spaces after opening parenthesis
		output = output.replaceAll("\\( +", "(");
		// remove spaces before closing parenthesis
		output = output.replaceAll(" +\\)", ")");
		// remove empty parentheses
		output = output.replaceAll("\\(\\)", "");
		
		// remove characters which are neither punctuation, whitespaces, letters or digits
//		output = output.replaceAll("[^"+PUNCTUATION+"\\s\\p{L}\\d]", "");
		output = output.replaceAll("[^"+PUNCTUATION+"\\s\\p{L}0-9]", "");
		
		// remove non-latin characters
		output = removeNonLatinLetters(output);
		
		return output;
	}
	
	/**
	 * Clean a string representing an article title. It is cleaned like
	 * regular text using {@link #cleanText(String,Locale)}, then new lines and
	 * double quotes are removed (so that this string can be put in a CSV
	 * file if needed).
	 * 
	 * @param title
	 * 		Original raw title.
	 * @param language
	 * 		Language of the title to process.
	 * @return
	 * 		Clean version of the title.
	 */
	public static String cleanTitle(String title, Locale language)
	{	String result = cleanText(title, language);
		result = result.replaceAll("\"", "");
		result = result.replaceAll("\\n", " ");
		result = result.replaceAll(" +"," ");
		return result;
	}
	
	/**
	 * Normalizes the string representing a mention. This consists in
	 * using lowercase only, removing newlines and punctuation, and 
	 * trimming.
	 * <br/>
	 * This method is meant for clean text.
	 * 
	 * @param name
	 * 		String to normalize.
	 * @return
	 * 		String after normalization.
	 */
	public static String cleanMentionName(String name)
	{	String result = null;
		if(name!=null)
		{	result = name.toLowerCase();
			result = result.replaceAll("\n"," ");
			result = StringTools.removePunctuation(result);
//			result = StringTools.removeDiacritics(result);	//TODO maybe drop the accents?
			result = result.trim();
		}
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SPACES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Process the specified string in order to remove space character-related problems. 
	 * All whitespaces are replaced by standard spaces ' ', excepted '\n'. All new line
	 * character variants are replaced by '\n'. All consecutive redundant whitespaces are 
	 * removed. The text is also trimmed (leading and trailing whitespaces are removed).
	 * <br/>
	 * This method is meant to be used only by {@link #cleanText(String,Locale)}.
	 *  
	 * @param string
	 * 		The original string (not modified).
	 * @return
	 * 		Modified version of the input string.
	 */
	private static String cleanSpaces(String string)
	{	String result = string;
		
		if(result!=null)
		{	// replace all carriage return chars by newline ones
			result = result.replace('\r', '\n');
			// replace all consecutive new line chars by a single one //TODO should we accept "\n\n" to get paragraphs?
			result = result.replaceAll("\\n\\n+", "\n\n");
			
			// replace all white spaces (except newline chars) by regular spaces
			result = result.replaceAll("[\\s&&[^\\n]]", " ");
			// replace all consecutive spaces by a single one
			result = result.replaceAll(" +", " ");
			
			// remove initial/final spaces
			result = result.trim();
		}
		
		return result;
	}

	/**
	 * Process the specified string in order to <i>replace</i> non-standard 
	 * whitespace characters. The number of characters in the text is 
	 * not modified (unlike {@link #cleanSpaces(String)}). Tabs and new lines
	 * are not affected.
	 *  
	 * @param string
	 * 		The original string (not modified).
	 * @return
	 * 		Modified version of the input string.
	 */
	public static String replaceSpaces(String string)
	{	String result = string;
		
		if(result!=null)
		{	// replace all white spaces by regular spaces
			// new line and tabs are not affected
			result = result.replaceAll("\\p{Z}", " "); // \p{Z} includes more different whitespaces than \s
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// LETTERS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks if the specified string contains any letters.
	 * 
	 * @param string
	 * 		String to analyze.
	 * @return
	 * 		{@code true} iff the string contains at least one letters.
	 */
	public static boolean hasNoLetter(String string)
	{	Pattern r = Pattern.compile("[a-zA-Z]");
		Matcher m = r.matcher(string);
		boolean result = !m.find();
		return result;
	}

	/**
	 * Remove diacritics from the specified text.
	 * <br/>
	 * Taken from <a href="http://stackoverflow.com/questions/15190656/easy-way-to-remove-utf-8-accents-from-a-string">
	 * http://stackoverflow.com/questions/15190656/easy-way-to-remove-utf-8-accents-from-a-string</a>.
	 * <br/>
	 * Some exotic characters are treated separately: they look like they possess
	 * some diacritics (e.g. 'Ł'), but still they are not affected by Java's normalization. 
	 * So we process them manually.
	 * 
	 * @param text
	 * 		String to process, containing diacritics.
	 * @return
	 * 		Same string, but without the diacritics.
	 */
	public static String removeDiacritics(String text)
	{	String result = 
//		Normalizer.normalize(text, Form.NFD)
		Normalizer.normalize(text, Form.NFKD)	// catches supposedly more diacritics
			.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		
		// for some reason, some characters look like they have a diacritic
		// but are actually considered as completely different characters
		// and as such, they are missed by the above instruction
		result = result.replaceAll("Ⱥ", "A");
		result = result.replaceAll("ⱥ", "a");
		result = result.replaceAll("Ą́", "A");
		result = result.replaceAll("ą́", "a");
		result = result.replaceAll("Ą̃", "A");
		result = result.replaceAll("ą̃", "a");
		result = result.replaceAll("Ā̀", "A");
		result = result.replaceAll("ā̀", "a");
		result = result.replaceAll("A̋", "A");
		result = result.replaceAll("a̋", "a");
		result = result.replaceAll("ᶏ", "a");
		
		result = result.replaceAll("Ƀ", "B");
		result = result.replaceAll("ƀ", "b");
		result = result.replaceAll("Ꞗ", "B");
		result = result.replaceAll("ꞗ", "b");
		result = result.replaceAll("ᵬ", "b");
		result = result.replaceAll("ᶀ", "b");
		
		result = result.replaceAll("Ȼ", "C");
		result = result.replaceAll("ȼ", "c");
		result = result.replaceAll("Ꞓ", "C");
		result = result.replaceAll("ꞓ", "c");
		result = result.replaceAll("Ƈ", "C");
		result = result.replaceAll("ƈ", "c");
		result = result.replaceAll("ɕ", "c");
		result = result.replaceAll("ꞔ", "c");
		
		result = result.replaceAll("Ɖ", "D");
		result = result.replaceAll("ɖ", "d");
		result = result.replaceAll("Đ", "D");
		result = result.replaceAll("đ", "d");
		result = result.replaceAll("D̦", "D");
		result = result.replaceAll("d̦", "d");
		result = result.replaceAll("Ð", "D");
		result = result.replaceAll("ð", "d");
		result = result.replaceAll("Ɗ", "D");
		result = result.replaceAll("ɗ", "d");
		result = result.replaceAll("ᵭ", "d");
		result = result.replaceAll("ᶁ", "d");
		result = result.replaceAll("ᶑ", "d");
		result = result.replaceAll("ȡ", "d");

		result = result.replaceAll("Ɇ", "E");
		result = result.replaceAll("ɇ", "e");
		result = result.replaceAll("Ê̄", "E");
		result = result.replaceAll("ê̄", "e");
		result = result.replaceAll("Ê̌", "E");
		result = result.replaceAll("ê̌", "e");
		result = result.replaceAll("Ė́", "E");
		result = result.replaceAll("ė́", "e");
		result = result.replaceAll("Ė̃", "E");
		result = result.replaceAll("ė̃", "e");
		result = result.replaceAll("Ę́", "E");
		result = result.replaceAll("ę́", "e");
		result = result.replaceAll("Ę̃", "E");
		result = result.replaceAll("ę̃", "e");
		result = result.replaceAll("E̋", "E");
		result = result.replaceAll("e̋", "e");
		result = result.replaceAll("E̩", "E");
		result = result.replaceAll("e̩", "e");
		result = result.replaceAll("È̩", "E");
		result = result.replaceAll("è̩", "e");
		result = result.replaceAll("É̩", "E");
		result = result.replaceAll("é̩", "e");
		result = result.replaceAll("ᶒ", "e");
		result = result.replaceAll("ⱸ", "e");
		result = result.replaceAll("ꬴ", "e");
		result = result.replaceAll("ꬳ", "e");
		result = result.replaceAll("ꬳ", "e");
		
		result = result.replaceAll("Ꞙ", "F");
		result = result.replaceAll("ꞙ", "f");
		result = result.replaceAll("Ƒ", "F");
		result = result.replaceAll("ƒ", "f");
		result = result.replaceAll("ᵮ", "f");
		result = result.replaceAll("ᶂ", "f");
		
		result = result.replaceAll("Ꞡ", "G");
		result = result.replaceAll("ꞡ", "g");
		result = result.replaceAll("Ǥ", "G");
		result = result.replaceAll("ǥ", "g");
		result = result.replaceAll("G̃", "G");
		result = result.replaceAll("g̃", "g");
		result = result.replaceAll("Ɠ", "G");
		result = result.replaceAll("ɠ", "g");
		result = result.replaceAll("ᶃ", "g");
		result = result.replaceAll("ꬶ", "g");
		
		result = result.replaceAll("Ħ", "H");
		result = result.replaceAll("ħ", "h");
		result = result.replaceAll("H̱", "H");
		result = result.replaceAll("ẖ", "h");
		result = result.replaceAll("Ⱨ", "H");
		result = result.replaceAll("ⱨ", "h");
		result = result.replaceAll("Ɦ", "H");
		result = result.replaceAll("ɦ", "h");
		result = result.replaceAll("ꞕ", "h");
		
		result = result.replaceAll("Ɨ", "I");
		result = result.replaceAll("ɨ", "i");
		result = result.replaceAll("i̇́", "i");
		result = result.replaceAll("i̇̀", "i");
		result = result.replaceAll("i̇̃", "i");
		result = result.replaceAll("Į́", "i");
		result = result.replaceAll("į̇́", "i");
		result = result.replaceAll("Į̃", "i");
		result = result.replaceAll("į̇̃", "i");
		result = result.replaceAll("Ī̀", "I");
		result = result.replaceAll("ī̀", "i");
		result = result.replaceAll("I̋", "I");
		result = result.replaceAll("i̋", "i");
		result = result.replaceAll("Ɨ", "I");
		result = result.replaceAll("ɨ", "i");
		result = result.replaceAll("İ", "I");
		result = result.replaceAll("ᶖ", "i");
		result = result.replaceAll("ı", "i");
		
		result = result.replaceAll("Ɉ", "J");
		result = result.replaceAll("ɉ", "j");
		result = result.replaceAll("J̌", "J");
		result = result.replaceAll("ǰ", "j");
		result = result.replaceAll("ȷ", "j");
		result = result.replaceAll("Ʝ", "J");
		result = result.replaceAll("ʝ", "j");
		result = result.replaceAll("j̇̃", "j");
		result = result.replaceAll("ɟ", "j");
		result = result.replaceAll("ʄ", "j");
		
		result = result.replaceAll("Ꝃ", "K");
		result = result.replaceAll("ꝃ", "k");
		result = result.replaceAll("Ꞣ", "K");
		result = result.replaceAll("ꞣ", "k");
		result = result.replaceAll("Ꝁ", "K");
		result = result.replaceAll("ꝁ", "k");
		result = result.replaceAll("Ꝅ", "K");
		result = result.replaceAll("ꝅ", "k");
		result = result.replaceAll("Ƙ", "K");
		result = result.replaceAll("ƙ", "k");
		result = result.replaceAll("Ⱪ", "K");
		result = result.replaceAll("ⱪ", "k");
		result = result.replaceAll("ᶄ", "k");
		
		result = result.replaceAll("Ƚ", "L");
		result = result.replaceAll("ƚ", "l");
		result = result.replaceAll("Ⱡ", "L");
		result = result.replaceAll("ⱡ", "l");
		result = result.replaceAll("Ꝉ", "L");
		result = result.replaceAll("ꝉ", "l");
		result = result.replaceAll("Ł", "L");
		result = result.replaceAll("ł", "l");
		result = result.replaceAll("ᴌ", "l");
		result = result.replaceAll("L̃", "L");
		result = result.replaceAll("l̃", "l");
		result = result.replaceAll("Ɫ", "L");
		result = result.replaceAll("ɫ", "l");
		result = result.replaceAll("Ɬ", "L");
		result = result.replaceAll("ɬ", "l");
		result = result.replaceAll("ꞎ", "l");
		result = result.replaceAll("ꬷ", "l");
		result = result.replaceAll("ꬸ", "l");
		result = result.replaceAll("ꬹ", "l");
		result = result.replaceAll("ᶅ", "l");
		result = result.replaceAll("ɭ", "l");
		result = result.replaceAll("ȴ", "l");
		
		result = result.replaceAll("M̋", "M");
		result = result.replaceAll("m̋", "m");
		result = result.replaceAll("M̃", "M");
		result = result.replaceAll("m̃", "m");
		result = result.replaceAll("ᵯ", "m");
		result = result.replaceAll("ᶆ", "m");
		result = result.replaceAll("Ɱ", "M");
		result = result.replaceAll("ɱ", "m");
		result = result.replaceAll("ꬺ", "m");
		
		result = result.replaceAll("Ꞥ", "N");
		result = result.replaceAll("ꞥ", "n");
		result = result.replaceAll("N̈", "N");
		result = result.replaceAll("n̈", "n");
		result = result.replaceAll("Ɲ", "N");
		result = result.replaceAll("ɲ", "n");
		result = result.replaceAll("Ŋ", "N");
		result = result.replaceAll("ŋ", "n");
		result = result.replaceAll("Ꞑ", "N");
		result = result.replaceAll("ꞑ", "n");
		result = result.replaceAll("ᵰ", "n");
		result = result.replaceAll("ᶇ", "n");
		result = result.replaceAll("ɳ", "n");
		result = result.replaceAll("ȵ", "n");
		result = result.replaceAll("ꬻ", "n");
		result = result.replaceAll("ꬼ", "n");
		
		result = result.replaceAll("Ɵ", "O");
		result = result.replaceAll("ɵ", "o");
		result = result.replaceAll("Ꝋ", "O");
		result = result.replaceAll("ꝋ", "o");
		result = result.replaceAll("Ø", "O");
		result = result.replaceAll("ø", "o");
		result = result.replaceAll("O͘", "O");
		result = result.replaceAll("o͘", "o");
		result = result.replaceAll("Ǿ", "O");
		result = result.replaceAll("ǿ", "o");
		result = result.replaceAll("O̩", "O");
		result = result.replaceAll("o̩", "o");
		result = result.replaceAll("Ò̩", "O");
		result = result.replaceAll("ò̩", "o");
		result = result.replaceAll("Ó̩", "O");
		result = result.replaceAll("ó̩", "o");
		result = result.replaceAll("Ꝍ", "O");
		result = result.replaceAll("ꝍ", "o");
		result = result.replaceAll("ⱺ", "o");
		
		result = result.replaceAll("Ᵽ", "P");
		result = result.replaceAll("ᵽ", "p");
		result = result.replaceAll("Ꝑ", "P");
		result = result.replaceAll("ꝑ", "p");
		result = result.replaceAll("Ƥ", "P");
		result = result.replaceAll("ƥ", "p");
		result = result.replaceAll("Ꝓ", "P");
		result = result.replaceAll("ꝓ", "p");
		result = result.replaceAll("Ꝕ", "P");
		result = result.replaceAll("ꝕ", "p");
		result = result.replaceAll("P̃", "P");
		result = result.replaceAll("p̃", "p");
		result = result.replaceAll("ᵱ", "p");
		result = result.replaceAll("ᶈ", "p");
		
		result = result.replaceAll("Ꝙ", "Q");
		result = result.replaceAll("ꝙ", "q");
		result = result.replaceAll("Ꝗ", "Q");
		result = result.replaceAll("ꝗ", "q");
		result = result.replaceAll("ɋ", "q");
		result = result.replaceAll("ʠ", "q");
		
		result = result.replaceAll("Ꞧ", "R");
		result = result.replaceAll("ꞧ", "r");
		result = result.replaceAll("Ɍ", "R");
		result = result.replaceAll("ɍ", "r");
		result = result.replaceAll("R̃", "R");
		result = result.replaceAll("r̃", "r");
		result = result.replaceAll("Ɽ", "R");
		result = result.replaceAll("ɽ", "r");
		result = result.replaceAll("ᵲ", "r");
		result = result.replaceAll("ᶉ", "r");
		result = result.replaceAll("ꭉ", "r");
		
		result = result.replaceAll("Ꞩ", "S");
		result = result.replaceAll("ꞩ", "s");
		result = result.replaceAll("S̩", "S");
		result = result.replaceAll("s̩", "s");
		result = result.replaceAll("Ȿ", "S");
		result = result.replaceAll("ȿ", "s");
		result = result.replaceAll("ʂ", "s");
		result = result.replaceAll("ᶊ", "s");
		result = result.replaceAll("ᵴ", "s");
		
		result = result.replaceAll("Ⱦ", "T");
		result = result.replaceAll("ⱦ", "t");
		result = result.replaceAll("Ŧ", "T");
		result = result.replaceAll("ŧ", "t");
		result = result.replaceAll("Ƭ", "T");
		result = result.replaceAll("ƭ", "t");
		result = result.replaceAll("Ʈ", "T");
		result = result.replaceAll("ʈ", "t");
		result = result.replaceAll("T̈", "T");
		result = result.replaceAll("ẗ", "t");
		result = result.replaceAll("ᵵ", "t");
		result = result.replaceAll("ƫ", "t");
		result = result.replaceAll("ȶ", "t");
		
		result = result.replaceAll("Ʉ", "U");
		result = result.replaceAll("ʉ", "u");
		result = result.replaceAll("Ų́", "U");
		result = result.replaceAll("ų́", "u");
		result = result.replaceAll("Ų̃", "U");
		result = result.replaceAll("ų̃", "u");
		result = result.replaceAll("Ū̀", "U");
		result = result.replaceAll("ū̀", "u");
		result = result.replaceAll("Ū́", "U");
		result = result.replaceAll("ū́", "u");
		result = result.replaceAll("Ū̃", "U");
		result = result.replaceAll("ū̃", "u");
		result = result.replaceAll("Ʉ", "U");
		result = result.replaceAll("ʉ", "u");
		result = result.replaceAll("Ꞹ", "U");
		result = result.replaceAll("ꞹ", "u");
		result = result.replaceAll("ᶙ", "u");
		result = result.replaceAll("ꭒ", "u");
		
		result = result.replaceAll("Ꝟ", "V");
		result = result.replaceAll("ꝟ", "v");
		result = result.replaceAll("Ʋ", "V");
		result = result.replaceAll("ʋ", "v");
		result = result.replaceAll("Ỽ", "V");
		result = result.replaceAll("ỽ", "v");
		result = result.replaceAll("ᶌ", "v");
		result = result.replaceAll("ⱱ", "v");
		result = result.replaceAll("ⱴ", "v");
		
		result = result.replaceAll("W̊", "W");
		result = result.replaceAll("ẘ", "w");
		result = result.replaceAll("Ⱳ", "W");
		result = result.replaceAll("ⱳ", "w");
		
		result = result.replaceAll("X́", "X");
		result = result.replaceAll("x́", "x");
		result = result.replaceAll("X̂", "X");
		result = result.replaceAll("x̂", "x");
		result = result.replaceAll("X̌", "X");
		result = result.replaceAll("x̌", "x");
		result = result.replaceAll("X̧", "X");
		result = result.replaceAll("x̧", "x");
		result = result.replaceAll("X̱", "X");
		result = result.replaceAll("x̱", "x");
		result = result.replaceAll("X̣", "X");
		result = result.replaceAll("x̣", "x");
		result = result.replaceAll("ᶍ", "x");
		
		result = result.replaceAll("Ɏ", "Y");
		result = result.replaceAll("ɏ", "y");
		result = result.replaceAll("Y̊", "Y");
		result = result.replaceAll("ẙ", "y");
		result = result.replaceAll("Ƴ", "Y");
		result = result.replaceAll("ƴ", "y");
		result = result.replaceAll("Ỿ", "Y");
		result = result.replaceAll("ỿ", "y");
		
		result = result.replaceAll("Ƶ", "Z");
		result = result.replaceAll("ƶ", "z");
		result = result.replaceAll("Ȥ", "Z");
		result = result.replaceAll("ȥ", "z");
		result = result.replaceAll("Ⱬ", "Z");
		result = result.replaceAll("ⱬ", "z");
		result = result.replaceAll("Ɀ", "Z");
		result = result.replaceAll("ɀ", "z");
		result = result.replaceAll("ᵶ", "z");
		result = result.replaceAll("ᶎ", "z");
		result = result.replaceAll("ʐ", "z");
		result = result.replaceAll("ʑ", "z");
		
		return result;
	}
	
	/**
	 * Removes all the non-latin letters, as they are generally not supported
	 * by the recognizers (or other processors).
	 * <br/>
	 * This method is meant to be used only by {@link #cleanInnerText(String,Locale)}.
	 * 
	 * @param input
	 * 		Original string.
	 * @return
	 * 		Same string, without the non-latin letters.
	 */
	private static String removeNonLatinLetters(String input)
	{	logger.increaseOffset();
//		boolean disp = input.length()>100000;
		String result = input;
		
		if (input!=null)
		{	// first version, regex-based: problems when dealing with bidirectional texts (eg english + arabic)
//			String diacLess = removeDiacritics(result);
//			Matcher matcher = NL_PATTERN.matcher(diacLess);
//			String tmp = "";
//			int prevPos = 0;
//			while(matcher.find())
//			{	int pos = matcher.start();
//				tmp = tmp + result.substring(prevPos,pos);
//				prevPos = pos + 1;
//			}
//			if(prevPos<result.length())
//				tmp = tmp + result.substring(prevPos,result.length());
//			result = tmp;
			
			// second version: brute force, but seems more robust
//			String diacLess = removeDiacritics(result);			
//			StringBuffer tmp = new StringBuffer();
//			for(int i=0;i<diacLess.length();i++) 
//			{	
////				if(disp && (i+1)%50000==0)
////					logger.log("Processing char "+(i+1)+"/"+diacLess.length());
//				
//				char c = diacLess.charAt(i);
//				if(LATIN_CHARS.contains(c))
//				{	char cc = input.charAt(i);
//					tmp.append(cc);
//				}
//	        }
//			result = tmp.toString();
			/*  pb when dealing with non-latin diacritics.
			 * 	example : "Super Mario Bros. Anime Movie Restored (Best Quality!) . English subbed . スーパーマリオブラザーズ ピーチ姫救出大作戦!"
			 */
			
			// third version: even bruter force
			StringBuffer tmp = new StringBuffer();
			for(int i=0;i<result.length();i++) 
			{	String charStr = Character.toString(result.charAt(i));
				String diacLess = removeDiacritics(charStr);
				char c = diacLess.charAt(0);
				if(LATIN_CHARS.contains(c))
				{	char cc = input.charAt(i);
					tmp.append(cc);
				}
	        }
			result = tmp.toString();
	    }
		
		logger.decreaseOffset();
		 return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SPLIT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** 
	 * Pattern used to detect sentences when splitting text 
	 * (taken from Ruchira Gayan Ranaweera's answer on 
	 * <a href="http://stackoverflow.com/questions/21430447/how-to-split-paragraphs-into-sentences">StackOverflow</a>) 
	 */ 
	private static final Pattern SENTENCE_PATTERN = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE);
    
	/**
	 * Returns the position of the first character of each sentence in the
	 * specified text. The sentence splitter is very basic, we consider 
	 * either the newline character, or the presence of a dot followed by 
	 * a space and not preceeded by an uppercase letter.
	 *  
	 * @param text
	 * 		The text to process.
	 * @return
	 * 		A list of integers corresponding to sentence positions in the text.
	 */
	public static List<Integer> getSentencePositions(String text)
	{	List<Integer> result = new ArrayList<Integer>();
		
		Matcher matcher = SENTENCE_PATTERN.matcher(text);
		while(matcher.find())
		{	//String sentence = matcher.group();
			int startPos = matcher.start();
			result.add(startPos);
		}
		
		return result;
	}
	
	/**
	 * Breaks down the specified text in chunks of {@code maxSize} characters.
	 * <br/>
	 * A sentence splitter is used to perform the split between two sentences.
	 * It is a very simple one, we consider either the newline character,
	 * or the presence of a dot followed by a space and not preceeded by an
	 * uppercase letter. 
	 * 
	 * @param text
	 * 		The text to split.
	 * @param maxSize
	 * 		The maximal size of the chunks to produce.
	 * @return
	 * 		A list of smaller chunks corresponding to a broken down version of 
	 * 		the original text.
	 */
	public static List<String> splitText(String text, int maxSize)
	{	List<String> result = new ArrayList<String>();
		//System.out.println(text); // debug
		
		// identify the sentences
		Matcher matcher = SENTENCE_PATTERN.matcher(text);
		
		// init
		int start = 0;
		int prevEnd = 0;
		//String sentence;
		int curEnd;
		boolean goOn = true;
		if(matcher.find())
		{	//sentence = matcher.group();
			curEnd = matcher.end();
		}
		else
		{	//sentence = text;
			curEnd = text.length();
		}
		
		// build the chunks
		do
		{	//System.out.println("Sentence: "+sentence); // debug
			int length = curEnd - start;
			
			// sentence too long for maxSize
			if(length > maxSize)
			{	// if only one sentence: must split using a lesser criterion
				char candidates[] = {'\n','\r','!','?',':',';',',','"','\'','|','(',' ','-','.'};
				int i = 0;
				int from;
				while(i<candidates.length && start==prevEnd)
				{	from = start;
					do
					{	from = text.indexOf(candidates[i], from+1);
						if(from!=-1 && (from-start)<maxSize)
							prevEnd = from;
					}
					while(from!=-1 && (from-start)<maxSize);
					i++;
				}
					
				// if none found...
				if(start==prevEnd)
				{	//String sentence = text.substring(start,curEnd);
					//throw new IllegalArgumentException("The sentence \""+sentence+"\" ("+(curEnd-start)+" chars) is too long and cannot be split for maxSize="+maxSize);
					// just one single word? there must a problem... cut anyway!
					prevEnd = start + maxSize;
				}
				else
					prevEnd ++;
				
				// force the inclusion of a possible ending space
				char c = text.charAt(prevEnd);
				while(Character.isWhitespace(c))
				{	prevEnd++;
					c = text.charAt(prevEnd);
				}
				
				// add the part of text to the result list
				String part = text.substring(start, prevEnd);
				result.add(part);
				start = prevEnd;
				//sentence = text.substring(prevEnd,curEnd);
			}
			
			// get the next sentence
			else
			{	goOn = matcher.find();
				if(goOn)
				{	prevEnd = curEnd;
					//sentence = matcher.group();
					curEnd = matcher.end();
				}
			}
		}
		while(goOn);
		
		if(start<text.length())
		{	String part = text.substring(start);
			result.add(part);
		}
		
		//for(String str: result) // debug
		//	System.out.print(str);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// VALUES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Takes a string of the form 1,4,8,12-15,78 and returns the
	 * corresponding values as an integer list (1,4,8,12,13,14,15,78).
	 * 
	 * @param expression
	 * 		String expression.
	 * @return
	 * 		Corresponding integer list.
	 */
	public static List<Integer> extractValues(String expression)
	{	Set<Integer> temp = new TreeSet<Integer>();
		String s1[] = expression.split(",");
		for(String str1: s1)
		{	if(!str1.isEmpty())
			{	String s2[] = str1.split("-");
				if(s2.length==1)
				{	int val = Integer.parseInt(str1);
					temp.add(val);
				}
				else if(s2.length==2)
				{	int start = Integer.parseInt(s2[0]);
					int end = Integer.parseInt(s2[1]);
					for(int val=start;val<=end;val++)
						temp.add(val);
				}
				else
					throw new IllegalArgumentException("Incorrect expression: "+expression);
			}
		}
		
		List<Integer> result = new ArrayList<Integer>(temp);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Takes a string and a position in this string,
	 * and returns a part of the string centered around
	 * the specified position, using the specified range
	 * to define the interval.
	 * <br/>
	 * Another line is used to mark the exact position
	 * with a ^. This function is used mainly for debug
	 * purposes.
	 * 
	 * @param pos
	 * 		The position to highlight.
	 * @param string
	 * 		The concerned text.
	 * @param range
	 * 		The range used to process the interval.
	 * @return
	 * 		The highlighted string.
	 */
	public static String highlightPosition(int pos, String string, int range)
	{	String sep = "[...]";
		
		// process the interval
		int beginIndex = Math.max(0, pos-range);
		int endIndex = Math.min(string.length(), pos+range);
		int posIndex = pos-beginIndex;
		
		// define the result string
		String result = "";
		if(beginIndex>0)
		{	result = result + sep;
			posIndex = posIndex + sep.length();
		}
		result = result + string.substring(beginIndex, endIndex);
		if(endIndex<string.length())
			result = result + sep;
		result = result + "\n";

		for(int i=0;i<posIndex;i++)
		{	if(result.charAt(i)=='\n' || result.charAt(i)=='\r')
				result = result + "\n";
			else
				result = result + " ";
		}
		result = result + "^";
		
		return result;
	}
}
