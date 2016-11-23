package fr.univavignon.nerwip.recognition.internal.modelless.spotlight;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.recognition.ConverterException;
import fr.univavignon.nerwip.recognition.ProcessorName;
import fr.univavignon.nerwip.recognition.internal.AbstractInternalConverter;
import fr.univavignon.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to DBpedia SpotLight.
 * It is able to convert the text outputed by this recognizer
 * into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class DpbSpotlightConverter extends AbstractInternalConverter<List<String>>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the recognizer.
	 */
	public DpbSpotlightConverter(String nerFolder)
	{	super(ProcessorName.SPOTLIGHT, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Prefix used to distinguish types from DBpedia classes or concepts */
	private final static String TYPE_PREFIX = "Schema:";
	/** List of strings to ignore during the type conversion */
	private final static List<String> IGNORE_LIST = Arrays.asList(
		TYPE_PREFIX+"Language",
		TYPE_PREFIX+"Product"
	);
	/** Map of string to mention type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put(TYPE_PREFIX+"Event", EntityType.MEETING);
		CONVERSION_MAP.put(TYPE_PREFIX+"Person", EntityType.PERSON);
		CONVERSION_MAP.put(TYPE_PREFIX+"Place", EntityType.LOCATION);
		CONVERSION_MAP.put(TYPE_PREFIX+"Organization", EntityType.ORGANIZATION);
		CONVERSION_MAP.put(TYPE_PREFIX+"CreativeWork", EntityType.PRODUCTION);
	}
	
	/////////////////////////////////////////////////////////////////
	// XML NAMES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Element containing the list of all detected mentions */
	private final static String ELT_RESOURCES = "Resources";
	/** Element describing one detected mention */
	private final static String ELT_RESOURCE = "Resource";

	/** Attribute representing the DBpedia URI associated to a mention */
	private final static String ATT_URI = "URI";
	/** List of types associated to a mention */
	private final static String ATT_TYPES = "types";
	/** Position of a mention in the original text */
	private final static String ATT_OFFSET = "offset";
	/** Number of occurrences of the entity in Wikipedia */
	private final static String ATT_SUPPORT = "support";
	/** Surface form of the mention */
	private final static String ATT_SURFACE_FORM = "surfaceForm";
	/** Relevance score (how good the second entity choice is compared to the returned one) */
	private final static String ATT_SECOND_RANK = "percentageOfSecondRank";
	/** Don't know what this is */
	private final static String ATT_SIMILARITY_SCORE = "similarityScore";
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, List<String> data) throws ConverterException
	{	logger.increaseOffset();
		Mentions result = new Mentions(processorName);
		
		logger.log("Processing each part of data and its associated answer");
		Iterator<String> it = data.iterator();
		logger.increaseOffset();
		int i = 0;
		int prevSize = 0;
		while(it.hasNext())
		{	i++;
			logger.log("Processing part "+i+"/"+data.size()/2);
			String originalText = it.next();
			String spotlightAnswer = it.next();
			
			try
			{	// build DOM
				logger.log("Build DOM");
				SAXBuilder sb = new SAXBuilder();
				Document doc = sb.build(new StringReader(spotlightAnswer));
				Element root = doc.getRootElement();
				
				// process each detected mention
				logger.log("Process each detected mention");
				Element eltResources = root.getChild(ELT_RESOURCES);
				List<Element> eltResourceList = eltResources.getChildren(ELT_RESOURCE);
				for(Element eltResource: eltResourceList)
				{	AbstractMention<?> mention = convertElement(eltResource,prevSize,originalText);
					if(mention!=null)
						result.addMention(mention);
					//TODO maybe a type could simply be retrieved from the DBpedia URI...
				}
				
				// update size
				prevSize = prevSize + originalText.length();
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
	 * a mention.
	 *  
	 * @param element
	 * 		Element to process.
	 * @param prevSize
	 * 		Size of the parts already processed. 
	 * @param part
	 * 		Part of the original text currently processed. 
	 * @return
	 * 		The resulting mention, or {@code null} if its
	 * 		type is not supported.
	 */
	private AbstractMention<?> convertElement(Element element, int prevSize, String part)
	{	AbstractMention<?> result = null;
		logger.increaseOffset();
		
		// parse the element
		StringBuffer msg = new StringBuffer();
			// surface form (check: compare with original text)
			String surfaceForm = element.getAttributeValue(ATT_SURFACE_FORM);
			msg.append("SurfaceForm="+surfaceForm);
			// type associated to the mention/entity
			String types = element.getAttributeValue(ATT_TYPES);
			msg.append(" Types="+types);
			// URI associated to the entity
			String uri = element.getAttributeValue(ATT_URI);
			msg.append(" URI="+uri);
			// position of the mention
			String offsetStr = element.getAttributeValue(ATT_OFFSET);
			int offset = Integer.parseInt(offsetStr);
			msg.append(" Offset="+offset);
			// support of the entity in Wikipedia
			String supportStr = element.getAttributeValue(ATT_SUPPORT);
			int support = Integer.parseInt(supportStr);
			msg.append(" Support="+support);
			// relevance score 
			String secondRankStr = element.getAttributeValue(ATT_SECOND_RANK);
			Float secondRank = Float.parseFloat(secondRankStr);
			msg.append(" PerSecondRank="+secondRank);
			// don't know exactly what this is...
			String similarityScoreStr = element.getAttributeValue(ATT_SIMILARITY_SCORE);
			Float similarityScore = Float.parseFloat(similarityScoreStr);
			msg.append(" SimilarityScore="+similarityScore);
		logger.log(msg.toString());
			
		// get the mention type
		EntityType type = null;
		String[] temp = types.split(",");
		Set<EntityType> typeList = new TreeSet<EntityType>(); 
		Set<String> candidateList = new TreeSet<String>(); 
		for(String tmp: temp)
		{	if(!IGNORE_LIST.contains(tmp))
			{	EntityType t = CONVERSION_MAP.get(tmp);
				if(t==null)
				{	if(tmp.startsWith(TYPE_PREFIX))
						candidateList.add(tmp);
				}
				else
					typeList.add(t);
			}
		}
		
		// continue only if there's a type
		if(typeList.isEmpty())
		{	// this is mainly in case we miss some important types when reverse engineering Spotlight's output
			if(!candidateList.isEmpty())
				logger.log("WARNING: could not find any type, when some of them started with "+TYPE_PREFIX+": "+candidateList.toString());
			else
				logger.log("Entity without a type >> ignoring");
		}
		else
		{	// get the type
			type = typeList.iterator().next();
			if(typeList.size()>1)
				logger.log("WARNING: several different types for the same entity ("+typeList.toString()+") >> Selecting the first one ("+type+")");
			
			// get the mention position
			int startPos = prevSize + offset;
			int length = surfaceForm.length();
			int endPos = startPos + length;
			
			// compare detected surface form and original text
			String valueStr = part.substring(offset, offset+length);
			if(!valueStr.equalsIgnoreCase(surfaceForm))
				logger.log("WARNING: the original and returned texts differ: \""+valueStr+"\" vs. \""+surfaceForm+"\"");
			else
			{	result = AbstractMention.build(type, startPos, endPos, processorName, valueStr);
				logger.log("Created mention "+result.toString());
			}
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    @Override
    protected void writeRawResults(Article article, List<String> intRes) throws IOException
    {	String temp = "";
        int i = 0;
        for(String str: intRes)
        {
        	i++;
        	if(i%2==1)
        		temp = temp + "\n>>> Part " + ((i+1)/2) + "/" + intRes.size()/2 + " - Original Text <<<\n" + str + "\n";
        	else
        	{	try
        		{	// build DOM
					SAXBuilder sb = new SAXBuilder();
					Document doc = sb.build(new StringReader(str));
					Format format = Format.getPrettyFormat();
					format.setIndent("\t");
					format.setEncoding("UTF-8");
					XMLOutputter xo = new XMLOutputter(format);
					String kafTxt = xo.outputString(doc);
					
					// add formatted kaf
					temp = temp + "\n>>> Part " + (i/2) + "/" + intRes.size()/2 + " - Spotlight Response <<<\n" + kafTxt + "\n";
        		}
        		catch (JDOMException e)
        		{	e.printStackTrace();
        		}
        	}
    	}
        
        writeRawResultsStr(article, temp);
    }
}
