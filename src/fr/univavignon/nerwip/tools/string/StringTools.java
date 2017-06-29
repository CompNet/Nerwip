package fr.univavignon.nerwip.tools.string;

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

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Combinations;

/**
 * This class contains various methods
 * used when processing strings.
 *  
 * @author Vincent Labatut
 */
public class StringTools
{
	/**
	 * Tests the {@link #splitText(String, int)} method.
	 * 
	 * @param args
	 * 		No need.
	 */
	public static void main(String[] args)
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
	}

	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Compares two strings while ignoring their case */
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
			string1 = string1.replaceAll("«", "\"");
			string2 = string2.replaceAll("«", "\"");
			string1 = string1.replaceAll("»", "\"");
			string2 = string2.replaceAll("»", "\"");
			
			// compare
			int result = string1.compareTo(string2);
			return result;
		}	
	};
	
	/**
	 * Compare the specified characters, using {@link #COMPARATOR},
	 * i.e. ignoring case and diacritics.
	 * 
	 * @param c1
	 * 		First character to compare.
	 * @param c2
	 * 		Second character to compare.
	 * @return
	 * 		Integer representing a classic comparison result.
	 */
	public static int compareCharsRelaxed(int c1, int c2)
	{	
//if(c1=='û')
//	System.out.print("");
		
		String s1 = new String(new int[]{c1},0,1);
		String s2 = new String(new int[]{c2},0,1);
		
		int result = COMPARATOR.compare(s1, s2);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CHARACTER TYPES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks whether the specified character
	 * is a punctuation mark or not.
	 * 
	 * @param c
	 * 		The character of interest.
	 * @return
	 * 		{@code true} iff the character is a punctuation mark.
	 */
	public static boolean isPunctuation(int c)
	{	String string = new String(new int[]{c},0,1);
		boolean result = Pattern.matches("\\p{Punct}", string);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// INITIALS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Changes the case of the specified String,
	 * so that the first letter is upper case
	 * and the rest is lower case.
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
	 * Checks if the specified string
	 * starts with an upercase character.
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
	/** Accepted punctuation marks */
	private final static String PUNCTUATION = "'()<>:,\\-!.\";&@%+";
	/** Regex used to detect HTML hyperlink tags */
	private final static Pattern HL_PATTERN = Pattern.compile("</?a ?[^>]*>");
	/** Regex used to detect non-latin letters */
	private final static Pattern NL_PATTERN = Pattern.compile("[^"+PUNCTUATION+"\\sA-Za-z0-9]");
	
	/**
	 * Cleans the specified string, in order to remove characters
	 * causing problems when detecting named entity mentions.
	 *    
	 * @param input
	 * 		The string to process.
	 * @return
	 * 		Cleaned string.
	 */
	public static String cleanText(String input)
	{	String output = input;
		
		String previous = output;
		do
		{	previous = output;
			
			// move punctuation out of hyperlinks
			String punctuation = "[ \\n\\.,;]";
			output = output.replaceAll("<a ([^>]*?)>("+punctuation+"*)([^<]*?)("+punctuation+"*)</a>","$2<a $1>$3</a>$4");
			output = output.replaceAll("<a ([^>]*?)>(\\()([^<]*?)(\\))</a>","$2<a $1>$3</a>$4");
			output = output.replaceAll("<a ([^>]*?)>(\\[)([^<]*?)(\\])</a>","$2<a $1>$3</a>$4");
			
			// process the text which does not belong to hyperlink tags (i.e. the raw, non-html text)
			String tmpStr = "";
			int prevPos = 0;
			Matcher matcher = HL_PATTERN.matcher(output);
			while(matcher.find())
			{	int startPos = matcher.start();
				int endPos = matcher.end();
				String substr = output.substring(prevPos,startPos);
				substr = cleanInnerText(substr);
				String tagStr = output.substring(startPos,endPos);
				tmpStr = tmpStr + substr + tagStr;
				prevPos = endPos;
			}
			int startPos = output.length();
			String substr = output.substring(prevPos,startPos);
			substr = cleanInnerText(substr);
			tmpStr = tmpStr + substr;
			output = tmpStr;
		}
		while(!output.equals(previous));
		
		output = output.trim();
		return output;
	}
	
	/**
	 * Clean the text which is not in HTML hyperlink tags.
	 * These method is used by {@link #cleanText(String)}.
	 * 
	 * @param input
	 * 		The text to clean.
	 * @return
	 * 		The cleaned text.
	 */
	private static String cleanInnerText(String input)
	{	String output = input;
		
		// replace all white spaces by regular spaces
		// new line and tabs are not affected
		output = output.replaceAll("\\p{Z}", " "); // \p{Z} includes more different whitespaces than \s
		// replace tabs by simple spaces
		output = output.replaceAll("\\t", " ");
		
		// replace multiple consecutive spaces by a single one 
		output = output.replaceAll("( )+", " ");

		// normalize newlines
		output = output.replaceAll("(\\r)+", "\n");
		// replace multiple consecutive newlines by a single one 
		output = output.replaceAll("(\\n)+", "\n");
		
		// remove spaces at the end of lines 
		output = output.replaceAll(" \\n", "\n");
		
		// replace multiple space-separated punctuations by single ones 
//			output = output.replaceAll("; ;", ";");
//			output = output.replaceAll(", ,", ",");
//			output = output.replaceAll(": :", ":");
//			output = output.replaceAll("\\. \\.", "\\.");
		
		// replace multiple consecutive punctuation marks by a single one 
		output = output.replaceAll("([\\.,;:] )[\\.,;:]", "$1");

		// remove spaces before dots 
		output = output.replaceAll(" \\.", ".");
		
		// remove various combinations of punctuation marks
		output = output.replaceAll("\\(;", "(");

		// adds a final dot when it is missing at the end of a sentence (itself detected thanks to the new line)
//			output = output.replaceAll("([^(\\.|\\-)])\\n", "$1.\n");
		
		// insert a space after a coma, when missing
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
		output = output.replaceAll("ﬁ", "fi");
		output = output.replaceAll("ﬂ", "fl");
		output = output.replaceAll("ﬀ", "ff");
		output = output.replaceAll("ﬃ", "ffi");
		output = output.replaceAll("ﬄ", "ffl");
		output = output.replaceAll("œ", "oe");
		output = output.replaceAll("Œ", "Oe");
		output = output.replaceAll("ꝏ", "oo");
		output = output.replaceAll("Ꝏ", "Oo");
		output = output.replaceAll("ﬆ", "st");
		output = output.replaceAll("ꜩ", "tz");
		output = output.replaceAll("Ꜩ", "Tz");
		output = output.replaceAll("ᵫ", "ue");
		
		// replace certain punctuation marks (list of characters obtained from Wikipedia)
			// apostrophe and variants
			output = output.replaceAll("[’’ʼ`´ʹʻʽʾʿˈˊʹ΄՚᾽᾿′Ꞌꞌ＇︐︑՝]","'");
			// opening brackets
			output = output.replaceAll("[(\\[{❴〈⧼❬❰❮〈〈⸤⸤｢｢「⌜⸢⟦⌈⌊⟆⟓⟬⟮⦃⦅⦇⦉⦋⦏⦑⦓⦕⦗⧘⧚❨❪❲⁅⸦⸨〔〖〘〚【（［｛]", "(");
			// closing brackets
			output = output.replaceAll("[)\\]}❵〉⧽❭❱❯〉〉⸥⸥｣｣」⌝⸣⟧⌉⌋⟅⟔⟭⟯⦄⦆⦈⦊⦌⦐⦒⦔⦖⦘⧙⧛❩❫❳⁆⸧⸩〕〗〙〛】）］｝]", ")");
			// colons and variants
			output = output.replaceAll("[:：ː]",":");
			// coma and variants
			output = output.replaceAll("[,،⸲⸴⹁、﹐﹑，､‚]",",");
			// hyphens and variants \u2012 \u2013 \u2014 \u2015 \u2053
			output = output.replaceAll("[-‐‑֊᠆﹣－‒–—―⁓=*_/⁄∕／\\\\]","-");
			// ellipsis and variants
			output = output.replaceAll("[…᠁⋯⋰⋱︙⋮]","...");
			// exclamation mark and variants
			output = output.replaceAll("[ǃ‼⁈⁉⚠❕❗❢❣ꜝꜞꜟ﹗！🕴᥄]","!");
			// period and variants
			output = output.replaceAll("[⸼·]",".");
			// opening double quotes
			output = output.replaceAll(  "[«‹„⟪《『⸂⸄⸉⸌〝｟] ?", "\"");
			// closing double quotes
			output = output.replaceAll(" ?[»›“⟫》』⸃⸅⸊⸍〞｠]", "\"");
			// question mark and variants
			output = output.replaceAll("[⁇﹖⁈⁉‽]","?");
			// semicolon and variants
			output = output.replaceAll("[;؛⁏፤；︔﹔⍮⸵;]",";");
		
		// replace 2 consecutive single quotes by 1 double quote
		output = output.replaceAll("''+", "\"");
		// remove empty quotes
		output = output.replaceAll("\"\"", "");
	
		// remove spaces after opening parenthesis
		output = output.replaceAll("\\( +", "(");
		// remove spaces before closing parenthesis
		output = output.replaceAll(" +\\)", ")");
		// remove empty parentheses
		output = output.replaceAll("\\(\\)", "");
		
		// removes characters which are neither punctuation, whitespaces, letters or digits
//		output = output.replaceAll("[^"+PUNCTUATION+"\\s\\p{L}\\d]", "");
		output = output.replaceAll("[^"+PUNCTUATION+"\\s\\p{L}0-9]", "");
		
		// removes non-latin letters
		String diacLess = removeDiacritics(output);
		Matcher matcher = NL_PATTERN.matcher(diacLess);
		String tmp = "";
		int prevPos = 0;
		while(matcher.find())
		{	int pos = matcher.start();
			tmp = tmp + output.substring(prevPos,pos);
			prevPos = pos + 1;
		}
		tmp = tmp + output.substring(prevPos,output.length());
		output = tmp;
		
		return output;
	}
	
	/////////////////////////////////////////////////////////////////
	// SPACES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Process the specified string in order to remove
	 * space character-related problems.
	 *  
	 * @param string
	 * 		The original string (not modified).
	 * @return
	 * 		Modified version of the input string.
	 */
	public static String cleanSpaces(String string)
	{	String result = string;
		
		if(result!=null)
		{	// replace all white spaces by regular spaces
			result = result.replaceAll("\\s", " ");
			
			// replace all consecutive spaces by a single one
			result = result.replaceAll(" +", " ");
			
			// remove initial/final spaces
			result = result.trim();
		}
		
		return result;
	}

	/**
	 * Process the specified string in order to replace
	 * non-standard whitespace characters. The number
	 * of characters in the text is not modified
	 * (unlike {@link #cleanSpaces(String)}).
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
		
		// for some reason, certain characters are missed by the above instruction
		result = result.replace('ł','l');		
		result = result.replace('Ł','L');
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SPLIT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** 
	 * Pattern used to detect sentences when splitting text 
	 * (taken from Ruchira Gayan Ranaweera's answer from 
	 * http://stackoverflow.com/questions/21430447/how-to-split-paragraphs-into-sentences) 
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
				char candidates[] = {'\n','\r',';',',','|',':'};
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
				// if none found, exception
				if(start==prevEnd)
				{	// TODO we could force-split between words, it's better than nothing
					String sentence = text.substring(start,curEnd);
					throw new IllegalArgumentException("The sentence \""+sentence+"\" ("+(curEnd-start)+" chars) is too long and cannot be split for maxSize="+maxSize);
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
	// DISTANCE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /**
    * This method calculates the normalized Levenshtein  
    * distance between two strings. It returns a value between 0 (similar)
    * and 1 (maximally dissimilar)
    * 
    * @param str1
    * 		The first string.
    * @param str2
    *       The second string.
    * @return
    * 		A real value between 0 and 1 corresponding to the normalized Levenshtein distance.
    */
	public static double getNormalizedLevenshtein(String str1, String str2) 
	{	// process the regular distance
		double dist = StringUtils.getLevenshteinDistance(str1, str2);
	    //System.out.println("Levenshtein Distance between " + str1 + " and " + str2 + ":" + dist);
		
		// get the max length
		int maxLength = Math.max(str1.length(), str2.length());
		
		double result = dist / maxLength ;
		//System.out.println("Normalized Levenshtein Distance between " + str1 + " and " + str2 + " =  " + levNorm);
		
        return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PROPER NAMES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Generates all possible human names from a string representing
	 * the full name. This methods allows considering various combinations
	 * of lastname(s) and firstname(s).
	 * 
	 * @param names
	 * 		All the surface forms of the entity, should contain several names 
	 * 		separated by spaces.
	 * @return
	 * 		A list of strings corresponding to alternative forms of the 
	 * 		original name.
	 */
	public static List<String> getPossibleNames(Set<String> names)
	{	List<String> result = new ArrayList<String>();
		for(String name: names)
		{	if(!result.contains(name))
				result.add(name);
			String split[] = name.split(" ");
			
			for(int i=1;i<split.length;i++)
			{	// fix the last names
				String lastnames = "";
				for(int j=i;j<split.length;j++)
					lastnames = lastnames + split[j].trim() + " ";
				lastnames = lastnames.trim();
				
				// we try to fix the last names and get all combinations of firstnames 
				for(int j=1;j<i;j++)
				{	Combinations combi = new Combinations(i,j);
					Iterator<int[]> it = combi.iterator();
					while(it.hasNext())
					{	int indices[] = it.next();
						String firstnames = "";
						for(int index: indices)
							firstnames = firstnames + split[index].trim() + " ";
						String fullname = firstnames+lastnames;
						if(!result.contains(fullname))
							result.add(fullname);
					}
				}
				
				// we also try only the lastnames
				if(!result.contains(lastnames))
					result.add(lastnames);
			}
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Takes a string and a position in this string,
	 * and returns a part of the string centered around
	 * the specified postion, using the specified range
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
