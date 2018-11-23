package fr.univavignon.nerwip.tools.string;

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

import java.io.IOException;
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

import com.google.common.base.Optional;
import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

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
		
		// language detection
//		String texts[] = 
//		{	// french
//			"Le Bureau des Légendes Saison 2 streaming vf HD Au sein de la DGSE (Direction Générale de la Sécurité Extérieure), un département appelé le Bureau des légendes (BDL) pilote à distance les agents les plus importants des services de renseignements français : les clandestins. En immersion dans des pays hostiles, leur mission consiste à repérer les personnes susceptibles d'être recrutées comme source de renseignements. Opérant dans l'ombre 'sous légende', c'est-à-dire sous une identité fabriquée de toutes pièces, ils vivent durant de longues années dans une duplicité permanente.De retour d'une mission clandestine de six années à Damas, notre héros - plus connu sous le nom de code Malotru - est promu au sein du BDL et reprend peu à peu pied dans sa vraie vie. Mais contrairement à toute procédure de sécurité, il semble ne pas abandonner sa légende et l'identité sous laquelle il vivait en Syrie.",
//			// english
//			"Washington is the 18th largest state with an area of 71,362 square miles (184,827 sq km), and the 13th most populous state with over 7 million people. Approximately 60 percent of Washington's residents live in the Seattle metropolitan area, the center of transportation, business, and industry along the Puget Sound region of the Salish Sea, an inlet of the Pacific Ocean consisting of numerous islands, deep fjords, and bays carved out by glaciers. The remainder of the state consists of deep temperate rainforests in the west, mountain ranges in the west, central, northeast and far southeast, and a semi-arid basin region in the east, central, and south, given over to intensive agriculture. Washington is the second most populous state on the West Coast and in the Western United States, after California. Mount Rainier, an active stratovolcano, is the state's highest elevation at almost 14,411 feet (4,392 m) and is the most topographically prominent mountain in the contiguous United States.",
//			// spanish
//			"Fue nombrado en homenaje al líder de las fuerzas estadounidenses de la Guerra de la Independencia de EE. UU. de 1776 y primer presidente de Estados Unidos, George Washington. Los nombres de muchas ciudades y condados de Estados Unidos rinden homenaje a diversos presidentes estadounidenses, pero el estado de Washington es el único estado en ser nombrado en homenaje a un presidente estadounidense. Para diferenciarla de la capital de Estados Unidos, Washington D. C., en Estados Unidos, se suele llamar 'estado de Washington' al estado y 'D. C.' (abreviatura de 'Distrito de Columbia', District of Columbia en inglés), 'ciudad federal' o 'ciudad de Washington' o a la capital nacional.",
//			// german
//			"Gemessen an seiner Fläche steht Washington unter den US-Bundesstaaten mit 184.665 Quadratkilometern an 18. Stelle, gemessen an seiner Bevölkerung von 6.724.540 Einwohnern an 13. Stelle (Stand 2010). Der Großteil der Bevölkerung konzentriert sich rund um den Puget Sound, eine etwa 150 km lange, inselreiche und weitverzweigte Bucht im Westen des Staates, an dem auch die Hauptstadt Olympia sowie Seattle, die mit Abstand größte Stadt, liegen."
//		};
//		for(String text: texts)
//		{	System.out.println(text);
//			ArticleLanguage lg = detectLanguage(text, false);
//			System.out.println(">> "+lg);
//		}
		
		// non-latin character removal
//		removeNonLatinLetters("Super Mario Bros. Anime Movie Restored (Best Quality!) . English subbed . スーパーマリオブラザーズ ピーチ姫救出大作戦!");
		
		// clean spaces
		String res = cleanSpaces("fdssd\n dsfsdf\nsd dsf sdfsd fdsf    sdfsdf  sdfsd\n\n\nsdfsdf");
		System.out.println(res);
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	public static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
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
	private final static String PUNCTUATION = "'():,\\-!.\";&@%+";
	
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
	/** Regex used to detect HTML hyperlink tags */
	private final static Pattern HL_PATTERN = Pattern.compile("</?a ?[^>]*>");
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
	 * other processing). This method is meant to be applied to
	 * both raw and linked text (i.e. text containing hyperlinks or not).
	 * <br/>
	 * To summarize, we do the following:
	 * <ul>
	 * 	<li>Clean problems related to the variants of the space character, using {@link #cleanSpaces(String)}.</li>
	 * 	<li>Move certain characters (such as punctuation and newlines) out of hyperlinks.</li>
	 * 	<li>Clean the text using {@link #cleanInnerText(String,ArticleLanguage)} (except URLs, which are kept as is).</li>
	 * </ul>
	 *    
	 * @param input
	 * 		The string to process.
	 * @param language 
	 * 		Language of the considered text.
	 * @return
	 * 		Cleaned string.
	 */
	public static String cleanText(String input, ArticleLanguage language)
	{	String output = input;
		
		if(input!=null)
		{	// clean spaces
			output = cleanSpaces(output);
			
			String previous = output;
			do
			{	previous = output;
				
				// move certain punctuation marks and special characters out of hyperlinks
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
					substr = cleanInnerText(substr,language);
					String tagStr = output.substring(startPos,endPos);
					tmpStr = tmpStr + substr + tagStr;
					prevPos = endPos;
				}
				int startPos = output.length();
				String substr = output.substring(prevPos,startPos);
				substr = cleanInnerText(substr,language);
				tmpStr = tmpStr + substr;
				output = tmpStr;
			}
			while(!output.equals(previous));
			
			output = output.trim();
		}
		return output;
	}
	
	/**
	 * Clean some text taken from an article, and which is not a URL.
	 * This method is meant used only by {@link #cleanText(String,ArticleLanguage)}.
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
	private static String cleanInnerText(String input, ArticleLanguage language)
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
			output = output.replaceAll("[(\\[{❴〈⧼❬❰❮〈〈⸤⸤｢｢「⌜⸢⟦⌈⌊⟆⟓⟬⟮⦃⦅⦇⦉⦋⦏⦑⦓⦕⦗⧘⧚❨❪❲⁅⸦⸨〔〖〘〚【（［｛<]", "(");
			// closing brackets
			output = output.replaceAll("[)\\]}❵〉⧽❭❱❯〉〉⸥⸥｣｣」⌝⸣⟧⌉⌋⟅⟔⟭⟯⦄⦆⦈⦊⦌⦐⦒⦔⦖⦘⧙⧛❩❫❳⁆⸧⸩〕〗〙〛】）］｝>]", ")");
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
		
		// replace space-separated & by the full word
		String repl = "/";
		if(language!=null)
			repl = language.getEt();
		output = output.replaceAll(" & "," "+repl+" ");
		// remove the remaining & (not space-separated)
		output = output.replaceAll("&","/");
		
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
	 * regular text using {@link #cleanText(String,ArticleLanguage)}, then new lines and
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
	public static String cleanTitle(String title, ArticleLanguage language)
	{	String result = cleanText(title,language);
		result = result.replaceAll("\"", "");
		result = result.replaceAll("\\n", " ");
		result = result.replaceAll(" +"," ");
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
	 * This method is meant to be used only by {@link #cleanText(String,ArticleLanguage)}.
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
		
		// for some reason, certain characters are missed by the above instruction
		result = result.replace('ł','l');		
		result = result.replace('Ł','L');
//		result = result.replace('Š','S');
//		result = result.replace('š','s');
//		result = result.replace('Č','C');
//		result = result.replace('č','c');
//		result = result.replace('Ž','Z');
//		result = result.replace('ž','z');
		
		return result;
	}
	
	/**
	 * Removes all the non-latin letters, as they generally not supported
	 * by the recognizers (or other processors).
	 * <br/>
	 * This method is meant to be used only by {@link #cleanInnerText(String,ArticleLanguage)}.
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
	// LANGUAGE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Object used to detect the language of a text */
	private static LanguageDetector LANGUAGE_DETECTOR = null;
	/** Object used by the language detector for long texts */
	private static TextObjectFactory TEXT_FACTORY_LONG = null;
	/** Object used by the language detector for short texts */
	private static TextObjectFactory TEXT_FACTORY_SHORT = null;

	/**
	 * Detects the language in the specified text, and return the corresponding enum value.
	 * If the language does not correspond to one of the enumerated languages, then the
	 * method returns {@code null}.
	 * 
	 * @param text
	 * 		The text whose language we want to detect. 
	 * @param shortText
	 * 		Whether the text is short ({@code true}) or long ({@code false}). 
	 * @return 
	 * 		The {@link ArticleLanguage} value associated to the detected language,
	 * 		or {@code null} if the language could be recognized or is not enumerated.
	 *  
	 * @throws IOException 
	 * 		Problem while initializing the library.
	 */
	public static ArticleLanguage detectLanguage(String text, boolean shortText) throws IOException
	{	ArticleLanguage result = null;
		
		if(text!=null && !text.isEmpty())
		{	// init language detector
			if(LANGUAGE_DETECTOR==null)
			{	List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
				LANGUAGE_DETECTOR = LanguageDetectorBuilder.create(NgramExtractors.standard())
					.withProfiles(languageProfiles)
					.build();
			}
			// init text factory
			TextObjectFactory textObjectFactory;
			if(shortText)
			{	if(TEXT_FACTORY_SHORT==null)
					TEXT_FACTORY_SHORT = CommonTextObjectFactories.forDetectingShortCleanText();
				textObjectFactory = TEXT_FACTORY_SHORT;
			}
			else
			{	if(TEXT_FACTORY_LONG==null)
				TEXT_FACTORY_LONG = CommonTextObjectFactories.forDetectingOnLargeText();
				textObjectFactory = TEXT_FACTORY_LONG;
			}
			
			// process the text
			TextObject textObject = textObjectFactory.forText(text);
			Optional<LdLocale> lang = LANGUAGE_DETECTOR.detect(textObject);
			LdLocale loc = null;
			if(lang.isPresent())
				loc = lang.get();
			else
			{	List<DetectedLanguage> dls = LANGUAGE_DETECTOR.getProbabilities(textObject);
				double maxProba = 0;
				for(DetectedLanguage dl: dls)
				{	double proba = dl.getProbability();
					if(proba>maxProba)
					{	loc = dl.getLocale();
						maxProba = proba;
					}
				}
			}
			if(loc!=null)
			{	String iso = loc.getLanguage();
				switch(iso)
				{	case "fr": 
						result = ArticleLanguage.FR;
						break;
					case "en": 
						result = ArticleLanguage.EN;
						break;
				}
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
