package fr.univavignon.common.tools.strings;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;
import fr.univavignon.tools.strings.StringTools;

/**
 * This class contains various methods used when processing strings.
 *  
 * @author Vincent Labatut
 */
public class CommonStringTools
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
		// language detection
		String texts[] = 
		{	// french
			"Le Bureau des Légendes Saison 2 streaming vf HD Au sein de la DGSE (Direction Générale de la Sécurité Extérieure), un département appelé le Bureau des légendes (BDL) pilote à distance les agents les plus importants des services de renseignements français : les clandestins. En immersion dans des pays hostiles, leur mission consiste à repérer les personnes susceptibles d'être recrutées comme source de renseignements. Opérant dans l'ombre 'sous légende', c'est-à-dire sous une identité fabriquée de toutes pièces, ils vivent durant de longues années dans une duplicité permanente.De retour d'une mission clandestine de six années à Damas, notre héros - plus connu sous le nom de code Malotru - est promu au sein du BDL et reprend peu à peu pied dans sa vraie vie. Mais contrairement à toute procédure de sécurité, il semble ne pas abandonner sa légende et l'identité sous laquelle il vivait en Syrie.",
			// english
			"Washington is the 18th largest state with an area of 71,362 square miles (184,827 sq km), and the 13th most populous state with over 7 million people. Approximately 60 percent of Washington's residents live in the Seattle metropolitan area, the center of transportation, business, and industry along the Puget Sound region of the Salish Sea, an inlet of the Pacific Ocean consisting of numerous islands, deep fjords, and bays carved out by glaciers. The remainder of the state consists of deep temperate rainforests in the west, mountain ranges in the west, central, northeast and far southeast, and a semi-arid basin region in the east, central, and south, given over to intensive agriculture. Washington is the second most populous state on the West Coast and in the Western United States, after California. Mount Rainier, an active stratovolcano, is the state's highest elevation at almost 14,411 feet (4,392 m) and is the most topographically prominent mountain in the contiguous United States.",
			// spanish
			"Fue nombrado en homenaje al líder de las fuerzas estadounidenses de la Guerra de la Independencia de EE. UU. de 1776 y primer presidente de Estados Unidos, George Washington. Los nombres de muchas ciudades y condados de Estados Unidos rinden homenaje a diversos presidentes estadounidenses, pero el estado de Washington es el único estado en ser nombrado en homenaje a un presidente estadounidense. Para diferenciarla de la capital de Estados Unidos, Washington D. C., en Estados Unidos, se suele llamar 'estado de Washington' al estado y 'D. C.' (abreviatura de 'Distrito de Columbia', District of Columbia en inglés), 'ciudad federal' o 'ciudad de Washington' o a la capital nacional.",
			// german
			"Gemessen an seiner Fläche steht Washington unter den US-Bundesstaaten mit 184.665 Quadratkilometern an 18. Stelle, gemessen an seiner Bevölkerung von 6.724.540 Einwohnern an 13. Stelle (Stand 2010). Der Großteil der Bevölkerung konzentriert sich rund um den Puget Sound, eine etwa 150 km lange, inselreiche und weitverzweigte Bucht im Westen des Staates, an dem auch die Hauptstadt Olympia sowie Seattle, die mit Abstand größte Stadt, liegen."
		};
		for(String text: texts)
		{	System.out.println(text);
			ArticleLanguage lg = detectLanguage(text, false);
			System.out.println(">> "+lg);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	public static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
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
	
	/////////////////////////////////////////////////////////////////
	// FREQUENCIES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Compute the total word frequencies for the specified text.
	 * If the language is specified, the stop-words are not counted.
	 *  
	 * @param text
	 * 		The text to process.
	 * @param language 
	 * 		Language of the text, or {@code null} if stop-words should be counted.
	 * @return
	 * 		A map associating a frequency to each word appearing at least once.
	 */
	public static Map<String,Integer> computeWordFrequencies(String text, ArticleLanguage language)
	{	List<String> texts = new ArrayList<String>();
		texts.add(text);
		Map<String,Integer> result = computeWordFrequencies(texts, language);
		return result;
	}
	
	/**
	 * Compute the total word frequencies for the specified list of texts.
	 * If the language is specified, the stop-words are not counted.
	 * <br/>
	 * The processed text is supposed to be clean.
	 *  
	 * @param texts
	 * 		A list of texts.
	 * @param language 
	 * 		Language of the text, or {@code null} if stop-words should be counted.
	 * @return
	 * 		A map associating a frequency to each word appearing at least once.
	 */
	public static Map<String,Integer> computeWordFrequencies(Collection<String> texts, ArticleLanguage language)
	{	Map<String,Integer> result = new HashMap<String,Integer>();
		
		// init the list of stopwords
		List<String> stopWords;
		if(language!=null)
			stopWords = StopWordsManager.getStopWords(language);
		else
			stopWords = new ArrayList<String>();
		
		// process each text
		for(String text: texts)
		{	String cleanText = text.replaceAll("\\n", " ");
			cleanText = cleanText.replaceAll("\\d+"," ");			// we ignore digits
			cleanText = StringTools.removePunctuation(cleanText);	// and punctuation
			cleanText = cleanText.toLowerCase();
			
			String[] tokens = cleanText.split(" ");
			for(String token: tokens)
			{	if(!stopWords.contains(token))
				{	Integer c = result.get(token);
					if(c==null)
						c = 0;
					c++;
					result.put(token,c);
				}
			}
		}
		
		return result;
	}

	/**
	 * Compute the total word frequencies for the specified list of tokens.
	 * Unlike in {@link #computeWordFrequencies(Collection, ArticleLanguage)},
	 * the text is supposed to have been tokenized, so the list is not a list 
	 * of texts, but a list of tokens, which are directly compared. They are
	 * also supposed to be already normalized: no cleaning is performed by the
	 * method. Also, it does not distinguish stopwords from other words. 
	 *  
	 * @param tokens
	 * 		A list of tokens.
	 * @return
	 * 		A map associating a frequency to each token appearing at least once.
	 */
	public static Map<String,Integer> computeFrequenciesFromTokens(Collection<String> tokens)
	{	Map<String,Integer> result = new HashMap<String,Integer>();
		
		// process each token
		for(String token: tokens)
		{	Integer c = result.get(token);
			if(c==null)
				c = 0;
			c++;
			result.put(token,c);
		}
		
		return result;
	}
}
