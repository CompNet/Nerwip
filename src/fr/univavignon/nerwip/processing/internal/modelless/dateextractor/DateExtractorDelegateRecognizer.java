package fr.univavignon.nerwip.processing.internal.modelless.dateextractor;

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
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.MentionDate;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateRecognizer;
import fr.univavignon.nerwip.processing.internal.modelless.wikipediadater.WikipediaDater;

/**
 * This class implements our first date extractor.
 * It focuses on classic date formats (eg 31 august 1903).
 * <br/>
 * However it is not efficient enough on WP articles, because
 * it incorrectly recognizes many irrelevent expressions as 
 * dates. This is partly due to the too many {@link SimpleDateFormat} 
 * it relies uppon. A more suitable tool was tailored for
 * Wikipedia articles, cf. {@link WikipediaDater}.
 * 
 * @author Burcu Küpelioğlu
 */
class DateExtractorDelegateRecognizer extends AbstractModellessInternalDelegateRecognizer<List<MentionDate>>
{
	/**
	 * Builds and sets up an object representing
	 * our date extractor.
	 * 
	 * @param dateExtractor
	 * 		Recognizer in charge of this delegate.
	 */
	public DateExtractorDelegateRecognizer(DateExtractor dateExtractor)
	{	super(dateExtractor,false,false,true,false);
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
		// no options
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types detected by this recognizer */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList(
		EntityType.DATE
	);
	
	@Override
	public List<EntityType> getHandledEntityTypes()
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this recognizer can treat */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList(
		ArticleLanguage.EN
	);

	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PATTERNS		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Patterns used to detect full dates */
	private static final List<SimpleDateFormat> DAY_MONTH_YEAR_PATTERNS = Arrays.asList(
		new SimpleDateFormat("dd MMMM yyyy",Locale.ENGLISH),
		new SimpleDateFormat("dd MMMM, yyyy",Locale.ENGLISH),
		new SimpleDateFormat("MMMM dd, yyyy",Locale.ENGLISH),
		new SimpleDateFormat("dd/MMMM/yyyy",Locale.ENGLISH),
		new SimpleDateFormat("dd.MMMM.yyyy",Locale.ENGLISH),
		new SimpleDateFormat("dd-MMMM-yyyy",Locale.ENGLISH),
		new SimpleDateFormat("dd MM yyyy",Locale.ENGLISH), 
		new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH),
		new SimpleDateFormat("dd.MM.yyyy",Locale.ENGLISH),
		new SimpleDateFormat("yyyyy.MMMMM.dd",Locale.ENGLISH)
	);
	/** Patterns used to detect dates with only month and year (no day) */ 
	private static final List<SimpleDateFormat> MONTH_YEAR_PATTERNS = Arrays.asList(
		new SimpleDateFormat("MMMM yyyy",Locale.ENGLISH),
		new SimpleDateFormat("MMM yyyy",Locale.ENGLISH)
	);
	/** Patterns used to detect dates with only day and month (no year) */ 
	private static final List<SimpleDateFormat> DAY_MONTH_PATTERNS = Arrays.asList(
		new SimpleDateFormat("dd MMMM",Locale.ENGLISH)
	);
	/** Patterns used to detect dates with only month (no day, no year) */
	private static final List<SimpleDateFormat> MONTH_PATTERNS = Arrays.asList(
			new SimpleDateFormat("MMMM",Locale.ENGLISH)
	);
	/** Patterns used to detect dates with only year (no day, no month) */
	private static final List<SimpleDateFormat> YEAR_PATTERNS = Arrays.asList(
			new SimpleDateFormat("yyyy",Locale.ENGLISH)
	);

	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected List<MentionDate> detectMentions(Article article) throws ProcessorException
	{	logger.increaseOffset();
		List<MentionDate> result = new ArrayList<MentionDate>();
		
		String text = article.getRawText();
		
		logger.log("Process day-month-year patterns");
		extractDatePattern(text,result,DAY_MONTH_YEAR_PATTERNS,true,true,true);
		
		logger.log("Process month-year patterns");
		extractDatePattern(text,result,MONTH_YEAR_PATTERNS,false,true,true);
		
		logger.log("Process day-month patterns");
		extractDatePattern(text,result,DAY_MONTH_PATTERNS,true,true,false);
		
		logger.log("Process month patterns");
		extractDatePattern(text,result,MONTH_PATTERNS,false,true,false);
		
		logger.log("Process year patterns");
		extractDatePattern(text,result,YEAR_PATTERNS,false,false,true);
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Applies the specified list of patterns,
	 * in order to detect classic dates.
	 * 
	 * @param text
	 * 		Whole text of the article.
	 * @param mentions
	 * 		List of already detected dates.
	 * @param patterns
	 * 		List of patterns to apply.
	 * @param dayFlag
	 * 		Whether or not the pattern allows retrieving day numbers.
	 * @param monthFlag
	 * 		Whether or not the pattern allows retrieving month numbers.
	 * @param yearFlag
	 * 		Whether or not the pattern allows retrieving years numbers.
	 */
	public void extractDatePattern(String text, List<MentionDate> mentions, List<SimpleDateFormat> patterns, boolean dayFlag, boolean monthFlag, boolean yearFlag)
	{	ParsePosition pos = new ParsePosition(0);
		List<AbstractMention<?>> temp = new ArrayList<AbstractMention<?>>();
		
		while(pos.getIndex()<text.length())
		{	int startPos = pos.getIndex();
//if(startPos<text.length()-3 && text.substring(startPos,startPos+3).equalsIgnoreCase("nov"))
//	System.out.println();
//if(startPos<text.length()-14 && text.substring(startPos,startPos+14).equalsIgnoreCase("82nd battalion"))
//	System.out.print("");
			if((startPos==0 || isSeparator(text,startPos-1))
				&& !isSeparator(text, startPos)	
				&& !positionAlreadyUsed(startPos,temp))
			{	Date date = null;
				int endPos = -1;
				
				// find a date
				Iterator<SimpleDateFormat> it = patterns.iterator();
				while(it.hasNext() && date==null)
				{	DateFormat df = it.next();
					df.setLenient(false); // strict respect of the format
					date = df.parse(text, pos);
					endPos = pos.getIndex();
//					if(date!=null)
//					{	// we ignore numeric values of 1 or 2 digits
//						// (years of the form "92" are very rare)
//						if(endPos-startPos<3)
//							date = null;
//						else if(endPos-startPos==3)
//						{	// we ignore numeric values of 3 digits
//							if(!Character.isLetter(text.charAt(startPos)) 
//								// we ignore months abbreviations included in words
//								|| endPos<text.length() && !isSeparator(text, endPos))
//								date = null;
//						}
//					}
				}
				
				// build the corresponding mention
				if(date!=null) 
				{	// build date
					Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
					calendar.setTime(date);
					int year = 0;
					if(yearFlag)
						year = calendar.get(Calendar.YEAR);
					int month = 0;
					if(monthFlag)
							month = calendar.get(Calendar.MONTH) + 1;
					int day = 0;
					if(dayFlag)
						day = calendar.get(Calendar.DAY_OF_MONTH);
					fr.univavignon.common.tools.time.Date value = new fr.univavignon.common.tools.time.Date(day,month,year);
					
					// build mention
					String valueStr = text.substring(startPos, endPos);
					MentionDate mention = new MentionDate(startPos, endPos, recognizer.getName(), valueStr, value); 
					mentions.add(mention);
					temp.add(mention);
				}
			}
			pos.setIndex(startPos+1);
		}

	}

	/////////////////////////////////////////////////////////////////
	// SECONDARY METHODS	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks if the character at the specified
	 * position is a separator, i.e. not a letter
	 * or a digit.
	 * 
	 * @param text
	 * 		The complete text.
	 * @param position
	 * 		The position of the character of interest.
	 * @return
	 * 		{@code true} iff the character is not a letter or a digit.
	 */
	private boolean isSeparator(String text, int position)
	{	char c = text.charAt(position);
		boolean result = !Character.isDigit(c) && !Character.isLetter(c);
		return result;
	}
	
//	private void completeDates(List<MentionDate> mentions)
//	{	for(Date d:dates){
//			int index = dates.indexOf(d);
//			if(d.getYear() == 0) 
//				if( dates.get(index+1).getYear() != 0 && // if the next date in the list has a year
//				(dates.get(index+1).getPosEnd() - dates.get(index).getPosStart()) < 30) //and it is closer than 30 chars (!?)
//					d.setYear(dates.get(index+1).getYear()); //then we use its year value
//				else if(index != 0 && dates.get(index-1).getYear() != 0)
//					d.setYear(dates.get(index-1).getYear()); //otherwise, we use the year of the previous date
//		}
//	}

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, List<MentionDate> mentions) throws ProcessorException
	{	Mentions result = new Mentions(recognizer.getName());
		
		for(MentionDate mention: mentions)
			result.addMention(mention);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, List<MentionDate> mentions) throws IOException
	{	StringBuffer string = new StringBuffer();
		
		for(MentionDate mention: mentions)
			string.append(mention.toString() + "\n");
			
		writeRawResultsStr(article, string.toString());
	}
}
