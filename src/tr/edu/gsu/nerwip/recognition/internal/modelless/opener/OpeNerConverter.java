package tr.edu.gsu.nerwip.recognition.internal.modelless.opener;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
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

/**
 * This class is the converter associated to OpeNer.
 * It is able to convert the text outputed by this NER tool
 * into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class OpeNerConverter extends AbstractInternalConverter<List<String>>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the NER tool.
	 * @param parenSplit 
	 * 		Indicates whether mentions containing parentheses
	 * 		should be split (e.g. "Limoges (Haute-Vienne)" is plit 
	 * 		in two distinct entities).
	 */
	public OpeNerConverter(String nerFolder, boolean parenSplit)
	{	super(RecognizerName.OPENER, nerFolder, FileNames.FI_OUTPUT_TEXT);
	
		this.parenSplit = parenSplit;
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of string to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("PERSON", EntityType.PERSON);
		CONVERSION_MAP.put("LOCATION", EntityType.LOCATION);
		CONVERSION_MAP.put("ORGANIZATION", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("DATE", EntityType.DATE);
	}
	/*
	 * Note: ignored entity types
	 * (cf https://github.com/opener-project/kaf/wiki/KAF-structure-overview#nerc)
	 * Time
	 * Money
	 * Percent
	 * Misc (phone number, id card, bank number, addresses...)
	 */
	
	/////////////////////////////////////////////////////////////////
	// XML NAMES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Element containing the list of all tokenized words */
	private final static String ELT_TEXT = "text";
	/** Element containing the list of all identified terms */
	private final static String ELT_TERMS = "terms";
	/** Element representing a term */
	private final static String ELT_TERM = "term";
	/** Element containing the list of all detected entities */
	private final static String ELT_ENTITIES = "entities";
	/** Element representing an entity */
	private final static String ELT_ENTITY = "entity";
	/** Element containing the list of mentions to an entity */
	private final static String ELT_REFERENCES = "references";
	/** Element containing the list of external references for an entity */
	private final static String ELT_EXTERNAL_REFERENCES = "externalReferences";
	/** Element representing an external reference for an entity */
	private final static String ELT_EXTERNAL_REFERENCE = "externalReference";
	/** Element representing a mention to an entity (in a reference element) or a reference to a word (in a term element) */
	private final static String ELT_SPAN = "span";
	/** Element refering to a term constituting an entity mention */
	private final static String ELT_TARGET = "target";
	/** Element representing a word (word form) */
	private final static String ELT_WF = "wf";

	/** Attribute representing the id of a word */
	private final static String ATT_WID = "wid";
	/** Attribute representing the id of a term */
	private final static String ATT_TID = "tid";
	/** Attribute representing the id of a term or word in a target element */
	private final static String ATT_ID = "id";
	/** Attribute representing the type of an entity */
	private final static String ATT_TYPE = "type";
	/** Attribute representing the starting position of a word in the text */
	private final static String ATT_OFFSET = "offset";
	/** Attribute representing the length of a word in the text */
	private final static String ATT_LENGTH = "length";
	/** Attribute representing a knowledge base in an external reference */
	private final static String ATT_RESOURCE = "resource";
	/** Attribute representing a knowledge base id in an external reference */
	private final static String ATT_REFERENCE = "resource";
	/** Attribute representing a confidence score in an external reference */
	private final static String ATT_CONFIDENCE = "confidence";
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if mentions containing parentheses should be split */
	private boolean parenSplit = true;
	
	@Override
	public Entities convert(Article article, List<String> data) throws ConverterException
	{	logger.increaseOffset();
		Entities result = new Entities(recognizerName);
		
		logger.log("Processing each part of data and its associated answer");
		Iterator<String> it = data.iterator();
		logger.increaseOffset();
		int i = 0;
		int prevSize = 0;
		while(it.hasNext())
		{	i++;
			logger.log("Processing part "+i+"/"+data.size()/2);
			String originalText = it.next();
			String openerAnswer = it.next();
			
			try
			{	// build DOM
				logger.log("Build DOM");
				SAXBuilder sb = new SAXBuilder();
				Document doc = sb.build(new StringReader(openerAnswer));
				Element root = doc.getRootElement();
				
				// index all the detected words
				logger.log("Index all detected words");
				Map<String,Element> wordMap = new HashMap<String,Element>();
				Element textElt = root.getChild(ELT_TEXT);
				List<Element> wordElts = textElt.getChildren(ELT_WF);
				for(Element wordElt: wordElts)
				{	String wid = wordElt.getAttributeValue(ATT_WID); 
					wordMap.put(wid,wordElt);
				}
				
				// index all the detected terms
				logger.log("Index all detected terms");
				Map<String,Element> termMap = new HashMap<String,Element>();
				Element termsElt = root.getChild(ELT_TERMS);
				List<Element> termElts = termsElt.getChildren(ELT_TERM);
				for(Element termElt: termElts)
				{	String tid = termElt.getAttributeValue(ATT_TID); 
					termMap.put(tid,termElt);
				}
				
				// process all entity elements
				logger.log("Create entity objects");
				Element entitiesElt = root.getChild(ELT_ENTITIES);
				if(entitiesElt!=null)
				{	List<Element> entityElts = entitiesElt.getChildren(ELT_ENTITY);
					for(Element entityElt: entityElts)
					{	AbstractEntity<?> entity = convertElement(entityElt, wordMap, termMap, prevSize, originalText);
						if(entity!=null)
						{	// possibly split in two distinct, smaller entities when containing parentheses
							AbstractEntity<?>[] temp = processParentheses(entity);
							if(temp==null)
								result.addEntity(entity);
							else
							{	for(AbstractEntity<?> t: temp)
									result.addEntity(t);
							}
						}
					}
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
	 * an entity.
	 *  
	 * @param element
	 * 		Element to process.
	 * @param wordMap
	 * 		List of identified words.
	 * @param termMap
	 * 		List of identified terms.
	 * @param prevSize
	 * 		Size of the parts already processed. 
	 * @param part
	 * 		Part of the original text currently processed. 
	 * @return
	 * 		The resulting entity, or {@code null} if its
	 * 		type is not supported.
	 */
	private AbstractEntity<?> convertElement(Element element, Map<String,Element> wordMap, Map<String,Element> termMap, int prevSize, String part)
	{	AbstractEntity<?> result = null;
		
		String typeCode = element.getAttributeValue(ATT_TYPE);
		EntityType type = CONVERSION_MAP.get(typeCode);
		
		if(type!=null)
		{	// internal references
			Element referencesElt = element.getChild(ELT_REFERENCES);
			
			// process each span element
			List<Element> spanElts = referencesElt.getChildren(ELT_SPAN);
			if(spanElts.size()>1)
				logger.log("WARNING: several mentions to the same entity, this info could be used!");
			for(Element spanElt: spanElts)
			{	// process each target (=term) in the span
				List<Element> targetElts = spanElt.getChildren(ELT_TARGET);
				int startPos = -1;
				int endPos = 0;
				for(Element targetElt: targetElts)
				{	// get the refered term id
					String id = targetElt.getAttributeValue(ATT_ID);
					// get the corresponding term element
					Element termElt = termMap.get(id);
					
					// retrieve its constituting words
					Element spanElt2 = termElt.getChild(ELT_SPAN);
					List<Element> targetElts2 = spanElt2.getChildren(ELT_TARGET);
					int startPos2 = -1;
					int endPos2 = 0;
					for(Element targetElt2: targetElts2)
					{	// get the referred word id
						String id2 = targetElt2.getAttributeValue(ATT_ID);
						// get the corresponding word element
						Element wordElt = wordMap.get(id2);
						
						// get its position
						String offsetStr = wordElt.getAttributeValue(ATT_OFFSET);
						int offset = Integer.parseInt(offsetStr);
						String lengthStr = wordElt.getAttributeValue(ATT_LENGTH);
						int length = Integer.parseInt(lengthStr);
						
						// update term position
						if(startPos2<0)
							startPos2 = offset;
						endPos2 = offset + length;
					}
					
					// update mention position
					if(startPos<0)
						startPos = startPos2;
					endPos = endPos2;
				}
				
				// create entity
				String valueStr = part.substring(startPos, endPos);
				startPos = startPos + prevSize;
				endPos = endPos + prevSize;
				result = AbstractEntity.build(type, startPos, endPos, recognizerName, valueStr);
				
				// TODO we could add a unique code to the several mentions of the same entity
			}
			
			// external references
			Element extReferencesElt = element.getChild(ELT_EXTERNAL_REFERENCES);
			if(extReferencesElt!=null)
			{	// TODO we could retrieve the assocated knowledge base references
				// https://github.com/opener-project/kaf/wiki/KAF-structure-overview#nerc
				List<Element> extReferenceElts = extReferencesElt.getChildren(ELT_EXTERNAL_REFERENCE);
				logger.log("Found the following external references for entity "+result);
				logger.increaseOffset();
					for(Element extReferenceElt: extReferenceElts)
					{	String resource = extReferenceElt.getAttributeValue(ATT_RESOURCE);
						String reference = extReferenceElt.getAttributeValue(ATT_REFERENCE);
						String confidence = extReferenceElt.getAttributeValue(ATT_CONFIDENCE);
						logger.log("resource:"+resource+" reference:"+reference+" confidence:"+confidence);
					}
				logger.decreaseOffset();
			}
		}
		
		return result;
	}
	
	/**
	 * On strings such as "Limoges (Haute-Vienne)", OpeNer tends to detect a single entity
	 * when there are actually two ("Limoges" and "Haute-Vienne"). This method allows to
	 * post-process such results, in order to get both entities.
	 * 
	 * @param entity
	 * 		The original entity, containing both mentions.
	 * @return
	 * 		An array containing the two smaller mentions, or {@code null} if
	 * 		the specified entity was not of the desired form.
	 */
	private AbstractEntity<?>[] processParentheses(AbstractEntity<?> entity)
	{	AbstractEntity<?>[] result = null;
		
		if(parenSplit && !(entity instanceof EntityDate))
		{	// get entity info
			String original = entity.getStringValue();
			int startPos = entity.getStartPos();
			EntityType type = entity.getType();
			RecognizerName source = entity.getSource();
	
			// analyze the original string
			int startPar = original.lastIndexOf('(');
			int endPar = original.lastIndexOf(')');
			if(startPar!=-1 && endPar!=-1 && startPar<endPar  // we need both opening and closing parentheses
					&& !(startPar==0 && endPar==original.length()-1)) // to avoid treating things like "(Paris)" 
			{	// first entity
				String valueStr1 = original.substring(0,startPar);
				int startPos1 = startPos;
				int endPos1 = startPos + startPar;
				AbstractEntity<?> entity1 = AbstractEntity.build(type, startPos1, endPos1, source, valueStr1);
//if(valueStr1.isEmpty())
//	System.out.print("");

				// second entity
				String valueStr2 = original.substring(startPar+1,endPar);
				int startPos2 = startPos + startPar + 1;
				int endPos2 = startPos + endPar;
				AbstractEntity<?> entity2 = AbstractEntity.build(type, startPos2, endPos2, source, valueStr2);
//if(valueStr2.isEmpty())
//	System.out.print("");
				
				result = new AbstractEntity<?>[]{entity1,entity2};
			}
		}
		
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
					temp = temp + "\n>>> Part " + (i/2) + "/" + intRes.size()/2 + " - OpeNer Response <<<\n" + kafTxt + "\n";
        		}
        		catch (JDOMException e)
        		{	e.printStackTrace();
        		}
        	}
    	}
        
        writeRawResultsStr(article, temp);
    }
}
