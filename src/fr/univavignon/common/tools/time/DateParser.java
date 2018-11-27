package fr.univavignon.common.tools.time;	

import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

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
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Parse the specified string and returns a {@link Period} object,
	 * or {@code null} if the method failed to parse the text.
	 * 
	 * @param text
	 * 		Text to parse.
	 * @param language
	 * 		Language of the text.
	 * @return
	 * 		The corresponding {@code Period} (may contain twice the same date),
	 * 		or {@code null} if the text could not be parsed.
	 */
	public static Period parseDate(String text, ArticleLanguage language)
	{	Period result = null;
		
		switch(language)
		{	case EN:
				result = DateParserEn.parseDate(text);
				break;
			case FR:
				result = DateParserFr.parseDate(text);
				break;
		}
		
		if(result!=null && result.getStartDate()==null && result.getEndDate()==null)
			result = null;
		return result;
	}
}
