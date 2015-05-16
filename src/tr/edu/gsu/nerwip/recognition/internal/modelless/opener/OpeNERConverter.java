package tr.edu.gsu.nerwip.recognition.internal.modelless.opener;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import javax.xml.parsers.ParserConfigurationException;






//import org.htmlparser.util.NodeList;
//import org.jdom.Document;
//import org.jdom.Element;
//import org.jdom.JDOMException;
//import org.jdom.Namespace;
//import org.jdom.input.SAXBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
//import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;






import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
//import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to OpeNER.
 * It is able to convert the text outputed by this NER tool
 * into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * 
 * @author Sabrine Ayachi
 * 
 */
public class OpeNERConverter extends AbstractInternalConverter<String>
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the NER tool.
	 */
	public OpeNERConverter(String nerFolder)
	{	super(RecognizerName.OPENER, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of URI to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("Person", EntityType.PERSON);
		CONVERSION_MAP.put("Place", EntityType.LOCATION);
		CONVERSION_MAP.put("Organization", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("Date", EntityType.DATE);
		
	}
	
//	/** Pattern previously used to adjust entity positions */ 
//	private final static Pattern DOC_PATTERN = Pattern.compile("\\n\\n");
	
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	//@SuppressWarnings("unchecked")
	@Override
	public Entities convert(Article article, String data) throws ConverterException
	{	logger.increaseOffset();
		Entities result = new Entities(recognizerName);
		AbstractEntity<?> entity = null;
		int startPos;
		int endPos;
		String valueStr = null;
		EntityType typeStr = null;
		
		// extracting entities part from opeNer result text
	    String openerAnswer = null;
	    logger.log(">>>>>>>>>>extracting entities from opener result");
	    Pattern pattern = Pattern.compile("(?<=<entities>).*.(?=</entities>)");
		Matcher matcher = pattern.matcher(data);
        String xmlentities = new String();
		boolean found = false;
		while (matcher.find()) 
		{
			xmlentities = matcher.group().toString();
	        logger.log(">>>>>>>>>>>xml entities: " + xmlentities);
			found = true;
			}
		if (!found)
		{
			logger.log("ERROR: text not found");
			}
		openerAnswer =  "<entities>" + xmlentities + "</entities>";
		openerAnswer = openerAnswer.replaceAll("\\p{Space}\\p{Space}|\\p{Space}\\p{Space}\\p{Space}|\\p{Space}\\p{Space}\\p{Space}\\p{Space}", "");
		logger.log(">>>>>>>>>>>>>>openerAnswer:" + openerAnswer); 
		
		
		// extracting text part from opeNer result text
		String extraText = null;
	    logger.log(">>>>>>>>>>extracting text from opener result");
	    Pattern pattern1 = Pattern.compile("(?<=<text>).*.(?=</text>)");
		Matcher matcher1 = pattern1.matcher(data);
        String text = new String();
		boolean found1 = false;
		while (matcher1.find()) 
		{
			extraText = matcher1.group().toString();
	        //logger.log(">>>>>>>>>>>text: " + extraText);
			found = true;
			}
		if (!found1) 
		{
			logger.log("ERROR: text not found");
			}
		text = "<text>" + extraText + "</text>";
		text = text.replaceAll("\\p{Space}\\p{Space}|\\p{Space}\\p{Space}\\p{Space}|\\p{Space}\\p{Space}\\p{Space}\\p{Space}", "");
		logger.log(">>>>>>>>>>>>>>extracting text:" + text); 
		
		
		// extracting entities				
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(openerAnswer));
            Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("entity");
			NodeList nodes1 = doc.getElementsByTagName("references");
			String id = null;
			String wid = null;
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Element element = (Element) nodes.item(i);
			    Element element1 = (Element) nodes1.item(i);
                // parsing entities types
			    String type = element.getAttribute("type");
                logger.log("entities types:" + i + type);
                typeStr = CONVERSION_MAP.get(type);
			    // parsing entities names
                NodeList name = element.getElementsByTagName("references");
			    Element line = (Element) name.item(0);
			    valueStr = getCharacterDataFromElement(line);
			    logger.log("entities names: " + i + " " + valueStr);
			    
			        
			    // parsing entities ids
	            Node lastChild = element1.getLastChild();
	            Node child = lastChild.getFirstChild(); //target
	            id = child.getAttributes().getNamedItem("id").getNodeValue();
	            //logger.log("id:" + i + id);
	                
	            // extracting endPos & startPos
	            id = id.replace(id.charAt(0), 'w');
	            logger.log("id:" + i + id); 
	                
	            DocumentBuilder db2 = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			    InputSource is2 = new InputSource();
				is2.setCharacterStream(new StringReader(text));
                Document doc2 = db2.parse(is2);
				NodeList nodes2 = doc2.getElementsByTagName("wf");
				
				int j = 0;
				String offset = null;
				String length = null;
				do 
				{
					Element element2 = (Element) nodes2.item(j);
					wid = element2.getAttribute("wid");				    
					//logger.log("wid " + wid);
					//logger.log("id " + id);
					boolean egal = id.equals(wid);				         
					logger.log("egal : " + egal);
					if (id.equals(wid)) { 
						logger.log("cas id == wid");
						offset = element2.getAttribute("offset");
						logger.log("offset: " + offset);
						length = element2.getAttribute("length");
						logger.log("length: " + length);
						int off = Integer.parseInt(offset);
						startPos = off;
						logger.log("startPos: " + startPos);

						logger.log("off: " + off);
						int leng = Integer.parseInt(length);
						logger.log("leng: " + leng);				                         
						endPos = off + leng - 1;						
						logger.log("endPos: " + endPos);
						entity = AbstractEntity.build(typeStr, startPos, endPos, recognizerName, valueStr);
						logger.log("im here:"); 
						result.addEntity(entity);
						logger.log("im here:"); 
					}
				    else logger.log("error" + i);
				    j++;
				    logger.log("je suis là:");}
				while(id != wid && j < nodes2.getLength() );
				}
			}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		}
	
	
	@SuppressWarnings("javadoc")
	public static String getCharacterDataFromElement(Element e) 
	{	Node child = e.getFirstChild();
	if (child instanceof CharacterData) 
	{
		CharacterData cd = (CharacterData) child;
	    return cd.getData();
	    }
	return "";
	}
	
	
/////////////////////////////////////////////////////////////////
// RAW				/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
@Override
protected void writeRawResults(Article article, String intRes) throws IOException
{	writeRawResultsStr(article, intRes);
}

}
