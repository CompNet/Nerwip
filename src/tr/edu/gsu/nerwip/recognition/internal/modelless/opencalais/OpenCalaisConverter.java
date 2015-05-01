package tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to OpenCalais.
 * It is able to convert the text outputed by this NER tool
 * into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class OpenCalaisConverter extends AbstractInternalConverter<List<String>>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the NER tool.
	 */
	public OpenCalaisConverter(String nerFolder)
	{	super(RecognizerName.OPENCALAIS, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of URI to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Anniversary", EntityType.DATE);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/City", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Company", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Continent", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Country", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Facility", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Holiday", EntityType.DATE);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/MusicGroup", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Organization", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Person", EntityType.PERSON);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/ProvinceOrState", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Region", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/SportsEvent", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/SportsLeague", EntityType.ORGANIZATION);
	}
	
//	/** Pattern previously used to adjust entity positions */ 
//	private final static Pattern DOC_PATTERN = Pattern.compile("\\n\\n");
	
	/////////////////////////////////////////////////////////////////
	// XML NAMES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the RDF namespace */
	private final static String NS_RDF = "rdf";
	/** RDF namespace */
	private Namespace nsRdf = null;
	/** Name of the OC namespace */
	private final static String NS_C = "c";
	/** OC namespace */
	private Namespace nsC = null;

	/** Element of the OC format */
	private final static String ELT_DESCRIPTION = "Description";
	/** Element of the OC format */
	private final static String ELT_DETECTION = "detection";
//	/** Element of the OC format */
//	private final static String ELT_NAME = "name";
	/** Element of the OC format */
	private final static String ELT_SUBJECT = "subject";
	
	/** Attribute of the OC format */
	private final static String ATT_ABOUT = "about";
//	/** Attribute of the OC format */
//	private final static String ATT_DOCUMENT = "document";
	/** Attribute of the OC format */
	private final static String ELT_EXACT = "exact";
	/** Attribute of the OC format */
	private final static String ELT_OFFSET = "offset";
	/** Attribute of the OC format */
	private final static String ELT_LENGTH = "length";
	/** Attribute of the OC format */
	private final static String ATT_RESOURCE = "resource";
	/** Attribute of the OC format */
	private final static String ELT_TYPE = "type";

	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	@Override
	public Entities convert(Article article, List<String> text) throws ConverterException
	{	logger.increaseOffset();
		Entities result = new Entities(recognizerName);

		logger.log("Processing each chunk of data and the associated answer");
		Iterator<String> it = text.iterator();
		logger.increaseOffset();
		int i = 0;
		int prevSize = 0;
		while(it.hasNext())
		{	i++;
			logger.log("Processing chunk "+i+"/"+text.size()/2);
			String originalText = it.next();
			String ocAnswer = it.next();
			
			try
			{	// build DOM
				logger.log("Build DOM");
				SAXBuilder sb = new SAXBuilder();
				Document doc = sb.build(new StringReader(ocAnswer));
				Element root = doc.getRootElement();
				nsRdf = root.getNamespace(NS_RDF);
				nsC = root.getNamespace(NS_C);
				
				// separate data from metadata
				logger.log("Separate data from meta-data");
				List<Element> elements = root.getChildren(ELT_DESCRIPTION,nsRdf);
				Map<String,Element> metaData = new HashMap<String,Element>();
				List<Element> data = new ArrayList<Element>();
				for(Element element: elements)
				{	if(element.getChild(ELT_DETECTION,nsC)==null)
					{	String about = element.getAttributeValue(ATT_ABOUT,nsRdf); 
						metaData.put(about,element);
					}
					else
						data.add(element);
				}
				
				// create entities
				logger.log("Create entity objects");
				for(Element element: data)
				{	AbstractEntity<?> entity = convertElement(element, metaData, prevSize);
					if(entity!=null)
						result.addEntity(entity);
				}
				
				// update size
				prevSize = prevSize + originalText.length();
				
				// fix entity positions
	//			fixRelativePositions(root, result);
			}
			catch (JDOMException e)
			{	e.printStackTrace();
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Receives an XML element and extract
	 * the information necessary to create
	 * an entity.
	 *  
	 * @param element
	 * 		Element to process.
	 * @param metaData
	 * 		Complementary information used to retreive certain details.
	 * @param prevSize
	 * 		Size of the chunks already processed. 
	 * @return
	 * 		The resulting entity.
	 */
	private AbstractEntity<?> convertElement(Element element, Map<String,Element> metaData, int prevSize)
	{	AbstractEntity<?> result = null;
		
		// get subject of the instance
		Element subjElt = element.getChild(ELT_SUBJECT,nsC);
		String subject = subjElt.getAttributeValue(ATT_RESOURCE,nsRdf);
		Element metadataElt = metaData.get(subject);
//if(metadataElt.getChild(ELT_NAME,nsC)==null)
//	System.out.print("");
		
		// get entity type from metadata
		Element typeElt = metadataElt.getChild(ELT_TYPE,nsRdf);
		String typeCode = typeElt.getAttributeValue(ATT_RESOURCE,nsRdf);
		EntityType type = CONVERSION_MAP.get(typeCode);
		
		// possibly create entity 
		if(type!=null)
		{	Element valueElt = element.getChild(ELT_EXACT,nsC);
			String valueStr = valueElt.getText();
			Element startElt = element.getChild(ELT_OFFSET,nsC);
			String startStr = startElt.getText();
			int startPos = prevSize + Integer.parseInt(startStr);
			Element lengthElt = element.getChild(ELT_LENGTH,nsC);
			String lengthStr = lengthElt.getText();
			int length = Integer.parseInt(lengthStr);
			int endPos = startPos + length;
			result = AbstractEntity.build(type, startPos, endPos, recognizerName, valueStr);
		}
		
		return result;
	}
	
//	/**
//	 * Method previously used to adjust
//	 * the position of entities.
//	 * 
//	 * @param root
//	 * 		Root of the XML document.
//	 * @param entities
//	 * 		List of entities to be corrected.
//	 */
//	@SuppressWarnings("unchecked")
//	private void fixRelativePositions(Element root, List<AbstractEntity<?>> entities)
//	{	if(!entities.isEmpty())
//		{	// retrieve full text as recorded in the XML document
//			Element elt = null;
//			List<Element> list = root.getChildren();
//			Iterator<Element> it = list.iterator();
//			while(elt==null)
//			{	elt = it.next();
//				Element docElt = elt.getChild(ATT_DOCUMENT,nsC);
//				if(docElt!=null)
//					elt = docElt;
//			}
////			((CDATA)elt.getContent().iterator().next()).getValue();
//			String fullText = elt.getText();
//			
//			// adjust entity positions accordingly
//			Matcher matcher = DOC_PATTERN.matcher(fullText);
//			while(matcher.find())
//			{	int matcherStart = matcher.start();
//				for(AbstractEntity<?> entity : entities)
//				{	int startPos = entity.getStartPos();
//					if(startPos > matcherStart)
//					{	int newStart = startPos - 1;
//						entity.setStartPos(newStart);
//						int newEnd = entity.getEndPos() - 1;
//						entity.setEndPos(newEnd);
//					}
//				}
//			}
//		}
//	}

	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, List<String> intRes) throws IOException
	{	String temp = "";
		int i = 0;
		for(String str: intRes)
		{	i++;
			if(i%2==1)
				temp = temp + "\n>>> Chunk " + ((i+1)/2) + "/" + intRes.size() + " - Original Text <<<\n" + str + "\n";
			else
				temp = temp + "\n>>> Chunk " + (i/2) + "/" + intRes.size() + " - OpenCalais Response <<<\n" + str + "\n";
		}
		writeRawResultsStr(article, temp);
	}
}
