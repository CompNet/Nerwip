package tr.edu.gsu.nerwip.recognition.internal.modelbased.heideltime;

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

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityDate;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.time.Date;
import tr.edu.gsu.nerwip.tools.xml.XmlNames;

/**
 * This class is the converter associated to HeidelTime.
 * It is able to convert the text outputed by this NER tool
 * into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * <br/>
 * In Nerwip, we don't need all the data output by HeidelTime
 * as formalized in the TIMEX3 standard.
 * <ul>
 *  <li>Attribute {@code type}:</li>
 * 	<ul>
 * 	  <li>{@code DATE}: calendar time, kept (<i>Friday, October 1, 1999</i>)</li>
 * 	  <li>{@code TIME}: time of the day, kept only if it refers to a specific date (<i>ten minutes to three</i>, but <i>the morning of January 31</i>)</li>
 * 	  <li>{@code DURATION}: time period, ignored (<i>three weeks</i>)</li>
 * 	  <li>{@code SET}: repetition, ignored (<i>twice a week</i>)</li>
 *  </ul>
 *  <li>Attribute {@code value}: only for {@code DATE} (when numerical) and {@code TIME} (when a date is specified numerically)</li>
 * </ul>
 * 
 * See the <a href="http://www.timeml.org/tempeval2/tempeval2-trial/guidelines/timex3guidelines-072009.pdf">
 * Guidelines for Temporal Expression Annotation for English for TempEval 2010</a> for more details, as well
 * as the <a href="http://timeml.org/site/publications/timeMLdocs/timeml_1.2.1.html#timex3">official 
 * TimeML specifications</a>
 * 
 * 
 * @author Vincent Labatut
 */
public class HeidelTimeConverter extends AbstractInternalConverter<String>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the NER tool.
	 */
	public HeidelTimeConverter(String nerFolder)
	{	super(RecognizerName.HEIDELTIME, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Header we need to remove before parsing the output text */
	private static final String ORIGINAL_DOCTYPE = "<!DOCTYPE TimeML SYSTEM \"TimeML.dtd\">";
	/** Value of the TIMEX3 attribute "type" for a calendar date */
	private static final String TYPE_DATE = "DATE";
	/** Value of the TIMEX3 attribute "type" for a day time (and possibly date) */
	private static final String TYPE_TIME = "TIME";
	/** Separte time from date in certain TIMEX3 values */
	private static final String TIME_SEPARATOR = "T";
	/** Separte time from date in certain TIMEX3 values */
	private static final String WEEK_CODE = "W";
	/** Before Christ code */
	private static final String BC_CODE = "BC";
	/** List of strings not acceptable as year values */
	private static final List<String> YEAR_BLACKLIST = Arrays.asList(
		"PAST_REF","PRESENT_REF","FUTURE_REF",
		"UNDEF"
//		"XXXX"
	);
	/** List of strings not acceptable as month values */
	private static final List<String> MONTH_BLACKLIST = Arrays.asList(
		"H1","H2",				// half (=semester)
		"Q1","Q2","Q3","Q4",	// quarters
		"WI","SP","SU","FA"		// seasons
	);
	/** Begining of the value string when only the date is specified */
	private static final String TIME_PREFIX = "XXXX-XX-XX";
	
	@Override
	public Entities convert(Article article, String data) throws ConverterException
	{	logger.increaseOffset();
		Entities result = new Entities(recognizerName);
		
		// parse the xml source
		logger.log("Parsing the XML source previously produced by HeidelTime");
		Element root;
		try
		{	String xmlSource = data.replace(ORIGINAL_DOCTYPE, "");
			xmlSource = xmlSource.replaceAll("&", "&amp;"); // needed to handle possible "&" chars (we suppose the original text does not contain any entity)
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(xmlSource));
			root = doc.getRootElement();
		}
		catch (JDOMException e)
		{	//e.printStackTrace();
			System.err.println(data);
			throw new ConverterException(e.getMessage());
		}
		catch (IOException e)
		{	//e.printStackTrace();
			throw new ConverterException(e.getMessage());
		}

		// process the xml document
		logger.log("Processing the resulting XML document");
		logger.increaseOffset();
		int index = -1; //-1 and not zero, because a new line is inserted at the beginning of the article in the XML file 
		XMLOutputter xo = new XMLOutputter();
		List<Content> children = root.getContent();
		for(Content child: children)
		{	// text content is just counted
			if(child instanceof Text)
			{	Text t = (Text)child;
				String str = t.getText();
				int length = str.length();
				logger.log("("+index+")"+str+ "[["+length+"]]");
				index = index + length;
			}
			
			// elements are processed individually
			else if(child instanceof Element)
			{	Element e = (Element)child;
				String str = e.getText();
				int length = str.length();
				logger.log("("+index+")"+xo.outputString(e)+ "[["+length+"]]");
				EntityDate entity = convertElement(e, index);
				if(entity!=null)
					result.addEntity(entity);
				index = index + length;
			}
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Receives an XML element, and processes it to
	 * extract a date entity.
	 * 
	 * @param element
	 * 		Element to process.
	 * @param index
	 * 		Current position in the original text (in characters).
	 * @return
	 * 		The created entity, or {@code null} if it was not
	 * 		possible to create it due to a lack of information.
	 */
	private EntityDate convertElement(Element element, int index)
	{	logger.increaseOffset();
		EntityDate result = null;
		XMLOutputter xo = new XMLOutputter();
				
		// check if the element does not contain any lower element
		List<Element> children = element.getChildren();
		if(!children.isEmpty())
			logger.log("WARNING: detected some encapsulated elements in "+xo.outputString(element));
		
		else
		{	String txType = element.getAttributeValue(XmlNames.ATT_TYPE);
			// we only focus on dates and date-times
			if(txType.equals(TYPE_DATE) || txType.equals(TYPE_TIME))
			{	String valueStr = element.getAttributeValue(XmlNames.ATT_VALUE);
				String text = element.getText();
				Date date = parseTimex3Value(valueStr,text);
				if(date==null)
					logger.log("WARNING: could not parse the date/time in element "+xo.outputString(element)); //TODO WARNING: 
				else
				{	int length = text.length();
					result = (EntityDate) AbstractEntity.build(EntityType.DATE, index, index+length, recognizerName, text);
					result.setValue(date);
				}
			}
			else
				logger.log("Element not describing a date/time (ignored): "+xo.outputString(element));
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Parse the specified text to extract a date.
	 * If the string does not contain a numerical date,
	 * then the method returns {@code null}.
	 * 
	 * @param value
	 * 		A TIMEX3 value string supposed to contain a date.
	 * @param text
	 * 		The text associated to the TIMEX3 element (for debugging).
	 * @return
	 * 		A custom object representing contained same date,
	 * 		or {@code null} if some critical information is missing.
	 */
	private Date parseTimex3Value(String value, String text)
	{	Date result = null;
		int year = 0;
		int month = 0;
		int day = 0;
	
		// check if the string contains only a time
		if(value.startsWith(TIME_PREFIX))
			logger.log("There is only a time, not a precise date (original text: "+text+") >> discarding the whole date");
		
		// check if it's an AD year (by opposition to BC)
		else if(value.startsWith(BC_CODE))
			logger.log("The date contains a before-christ (BC) year (original text: "+text+") >> discarding the whole date");
		
		// relevant date
		else
		{	// break down the string
			String s[] = value.split("-");	//UNDEF-REF-hour-PLUS-5 UNDEF-REF-week-WE-PLUS-1 XXXX-XX-XXT07:00
			
			// process the year
			// check if the year is acceptable
			if(YEAR_BLACKLIST.contains(s[0]))
				logger.log("There is no precise year (original text: "+text+") >> the year is discarded, as well as the whole date");
			else
			{	try
				{	year = Integer.parseInt(s[0]);
					// for cases like "the 1950s", HeidelTime inexplicably returns the value 195
					// we try to define a workaround for this case
					if(year>99 && year <1000)
					{	logger.log("The year should not be <1000: HeidelTime returned the value "+year+" for the string \""+text+"\" >> multiplying by 10"); 
						year = year * 10;
					}
				}
				catch(NumberFormatException e)
				{	//e.printStackTrace();
					logger.log("WARNING: could not parse the year in string \""+value+"\" (original text: "+text+") >> discarding the whole date");
				}
				
				// process the month
				if(year!=0 && s.length>1)
				{	// check if the month is acceptable
					if(MONTH_BLACKLIST.contains(s[1]))
						logger.log("There is no precise month (original text: "+text+") >> the month is discarded, as well as the day");
					else
					{	// is the second date component actually a week number?
						if(s[1].startsWith(WEEK_CODE))
						{	String weekStr = s[1].substring(1);
							int week = Integer.parseInt(weekStr);
							Calendar cal = Calendar.getInstance();
							cal.setWeekDate(year, week, Calendar.MONDAY);
							logger.log("We got a week number \""+value+"\" (original text: "+text+") >> converted to a month: "+month+"");
						}
						else	
						{	try
							{	month = Integer.parseInt(s[1]);
							}
							catch(NumberFormatException e)
							{	//e.printStackTrace();
									logger.log("WARNING: could not parse the month string \""+value+"\" (original text: "+text+") >> discarding it, as well as the day");
							}
						}
						
						// process the day
						if(month!=0 && s.length>2)
						{	try
							{	String s2[] = s[2].split(TIME_SEPARATOR); // ignore the possible time indicated after the date (separated by a T)
								day = Integer.parseInt(s2[0]);
							}
							catch(NumberFormatException e)
							{	//e.printStackTrace();
								logger.log("WARNING: could not parse the day in string \""+value+"\" (original text: "+text+") >> discarding it"); 
							}
						}
					}
				}
			}
			
			// build date object
			if(year!=0)
				result = new Date(day,month,year);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, String intRes) throws IOException
	{	writeRawResultsStr(article, intRes);
	}
}
