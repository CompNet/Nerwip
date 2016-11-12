package tr.edu.gsu.nerwip.tools.string;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011 Yasa Akbulut, Burcu Küpelioğlu & Vincent Labatut
 * Copyright 2012 Burcu Küpelioğlu, Samet Atdağ & Vincent Labatut
 * Copyright 2013 Samet Atdağ & Vincent Labatut
 * Copyright 2014-15 Vincent Labatut
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

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
			
			// replace all white spaces by regular spaces
			// new line and tabs are not affected
			output = output.replaceAll("\\p{Z}", " "); // \p{Z} includes more different whitespaces than \s
			
			// move punctuation out of hyperlinks
			String punctuation = "[ \\n\\.,;]";
			output = output.replaceAll("<a ([^>]*?)>("+punctuation+"*)([^<]*?)("+punctuation+"*)</a>","$2<a $1>$3</a>$4");
			output = output.replaceAll("<a ([^>]*?)>(\\()([^<]*?)(\\))</a>","$2<a $1>$3</a>$4");
			output = output.replaceAll("<a ([^>]*?)>(\\[)([^<]*?)(\\])</a>","$2<a $1>$3</a>$4");
			
			// replace multiple consecutive spaces by a single one 
			output = output.replaceAll("( )+", " ");
			
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
			
			// remove spaces after opening parenthesis
			output = output.replaceAll("\\( +", "(");
			// remove spaces before closing parenthesis
			output = output.replaceAll(" +\\)", ")");
			
			// remove various combinations of punctuation marks
			output = output.replaceAll("\\(;", "(");
	
			// remove empty square brackets and parentheses
			output = output.replaceAll("\\[\\]", "");
			output = output.replaceAll("\\(\\)", "");
			
			// adds a final dot when it is missing at the end of a sentence (itself detected thanks to the new line)
//			output = output.replaceAll("([^(\\.|\\-)])\\n", "$1.\n");
			
			// insert a space after a coma, when missing
//			output = output.replaceAll(",([^ _])", ", $1");
	
			// insert a space after a semi-column, when missing
//			output = output.replaceAll(";([^ _])", "; $1");
			
			// replace 2 single quotes by double quotes
			output = output.replaceAll("''+", "\"");
			
			// replace ligatures by two characters
			// note : the normalizer does not seem to work well for most ligature
			// cf. http://stackoverflow.com/questions/7171377/separating-unicode-ligature-characters
			output = output.replaceAll("œ", "oe");
			output = output.replaceAll("Œ", "Oe");
			output = output.replaceAll("æ", "ae");
			output = output.replaceAll("Æ", "Ae");
			output = output.replaceAll("ﬁ", "fi");
			
			// replace certain punctuation marks
			output = output.replaceAll("« ", "\"");
			output = output.replaceAll("«", "\"");
			output = output.replaceAll(" »", "\"");
			output = output.replaceAll("»", "\"");
			output = output.replaceAll("’","'");
			output = output.replaceAll("‒","-");	// \u2012
			output = output.replaceAll("–","-");	// \u2013
			output = output.replaceAll("—","-");	// \u2014
			output = output.replaceAll("―","-");	// \u2015
			output = output.replaceAll("⁓","-");	// \u2053
			
		}
		while(!output.equals(previous));
		
		output = output.trim();
		return output;
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
	 * @param string
	 * 		String to process, containing diacritics.
	 * @return
	 * 		Same string, but withouth the diacritics.
	 */
	public static String removeDiacritics(String string) 
	{	String result = Normalizer.normalize(string, Normalizer.Form.NFD);
		result = result.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
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
		
		// identify the sentences
		Matcher matcher = SENTENCE_PATTERN.matcher(text);
		
		// build the chunks
		int start = 0;
		int prevEnd = 0;
		while(matcher.find())
		{	// for debug
			String sentence = matcher.group();
//			System.out.println(sentence);
			
			int curEnd = matcher.end();
			if(curEnd-start > maxSize)
			{	// sentence too long for maxSize
				if(start==prevEnd)
				{	// we look for semicolons
					int from = start;
					List<Integer> idxs = new ArrayList<Integer>();
					do
					{	int idx = sentence.indexOf(';', from+1);
						from = idx;
						if(idx!=-1)
							idxs.add(idx);
					}
					while(from!=-1 && from<sentence.length());
					if(idxs.isEmpty())
					{	// TODO we could force-split between words, it's better than nothing
						throw new IllegalArgumentException("The sentence \""+sentence+"\" ("+sentence.length()+" chars) is too long to be split using maxSize="+maxSize);
					}
					else
					{	int idx = idxs.get(idxs.size()/2) + 1; // we take the midle ';'
						if(sentence.charAt(idx)==' ')
							idx++;
						prevEnd = start + idx;
					}
				}
				
				// force the inclusion of a possible ending space
				char c = text.charAt(prevEnd);
				while(c==' ' || c=='\n' || c=='\t')
				{	prevEnd++;
					c = text.charAt(prevEnd);
				}
				// add the part of text to the result list
				String part = text.substring(start, prevEnd);
				result.add(part);
				start = prevEnd;
			}
			
			prevEnd = curEnd;
		}
		
		if(start<text.length())
		{	String part = text.substring(start);
			result.add(part);
		}
		
		// for debug
//		System.out.println("result:\n"+result);
		
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
	{	TreeSet<Integer> temp = new TreeSet<Integer>();
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
    * This method calculate the normalized Levenshtein  
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
	{	// process the interval
		int beginIndex = Math.max(0, pos-range);
		int endIndex = Math.min(string.length(), pos+range);
		
		// define the result string
		String result = "";
		if(beginIndex>0)
			result = result + "[...]";
		result = result + string.substring(beginIndex, endIndex);
		if(endIndex<string.length())
			result = result + "[...]";
		result = result + "\n";

		for(int i=0;i<pos-beginIndex+5;i++)
			result = result + " ";
		result = result + "^";
		
		return result;
	}
}
