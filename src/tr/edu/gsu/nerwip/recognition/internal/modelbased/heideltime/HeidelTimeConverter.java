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
import java.util.List;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

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
	/** Value of the TIMEX3 attribute "type" for a calendar date */
	private static final String TYPE_DATE = "DATE";
	/** Value of the TIMEX3 attribute "type" for a day time (and possibly date) */
	private static final String TYPE_TIME = "TIME";
	
	@SuppressWarnings("unchecked")
	@Override
	public Entities convert(Article article, String data) throws ConverterException
	{	logger.increaseOffset();
		Entities result = new Entities(recognizerName);
		
		// parse the xml source
		logger.log("Parsing the XML source previously produced by HeidelTime");
		Element root;
		try
		{	String xmlSource = data.replace("<!DOCTYPE TimeML SYSTEM \"TimeML.dtd\">", "");
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new StringReader(xmlSource));
			root = doc.getRootElement();
		}
		catch (JDOMException e)
		{	//e.printStackTrace();
			throw new ConverterException(e.getMessage());
		}
		catch (IOException e)
		{	//e.printStackTrace();
			throw new ConverterException(e.getMessage());
		}

		// process the xml document
		logger.log("Processing the resulting XML document");
		logger.increaseOffset();
		int index = 0;
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
				result.addEntity(entity);
				index = index + length;
			}
		}

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
	 * 		The created entity.
	 */
	@SuppressWarnings("unchecked")
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
				Date date = parseTimex3Value(valueStr);
				if(date==null)
					logger.log("WARNING: Could not parse the date/time in element "+xo.outputString(element));
				else
				{	String text = element.getText();
					int length = text.length();
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
	 * @return
	 * 		A custom object representing contained same date.
	 */
	private Date parseTimex3Value(String value)
	{	// 1977-02-24
		String s[] = value.split("-");
		int year = Integer.parseInt(s[0]);
		int month = 0;
		if(s.length>1)
			month = Integer.parseInt(s[1]);
		int day = 0;
		if(s.length>2)
			day = Integer.parseInt(s[2]);
		
		// build date object
		Date result = new Date(year,month,day);
		
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
