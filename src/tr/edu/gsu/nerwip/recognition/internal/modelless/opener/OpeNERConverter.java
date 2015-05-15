package tr.edu.gsu.nerwip.recognition.internal.modelless.opener;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import javax.xml.parsers.ParserConfigurationException;



//import org.htmlparser.util.NodeList;
//import org.jdom.Document;
//import org.jdom.Element;
import org.jdom.JDOMException;
//import org.jdom.Namespace;
//import org.jdom.input.SAXBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;



import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
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
	@SuppressWarnings("unchecked")
	@Override
	public Entities convert(Article article, String data) throws ConverterException
	{	logger.increaseOffset();
		Entities result = new Entities(recognizerName);
		//String originalText = article.getRawText();
		
		
		
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
		//openerAnswer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + " " + "<entities>" + xmlentities + "</entities>";
		//logger.log(">>>>>>>>>>>>>>openerAnswer:" + openerAnswer); 
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
	        logger.log(">>>>>>>>>>>text: " + extraText);
			found = true;
			}
		if (!found1) 
		{
			logger.log("ERROR: text not found");
			}
		text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + " " + "<text>" + extraText + "</text>";
		logger.log(">>>>>>>>>>>>>>extracting text:" + text); 
		
		
		// extracting entities
		AbstractEntity<?> entity = null;
		
		try {
			
			 
			try {
				/*String xmlRecords = "<entities><entity eid=\"e1\" type=\"DATE\"><name><references><!--ref1--><span><target id=\"t5\" /> <target id=\"t3\" /> <target id=\"t2\" /> </span></references></name>"
				        + "<title>Manager</title></entity><entity eid=\"e1\" type=\"LOCATION\"><name><references><!--ref2--><span><target id=\"t7\" /> <target id=\"t7\" /> <target id=\"t1\" /></span></references></name>"
						+ "<title>M2</title></entity><entity eid=\"e1\" type=\"ORGANIZATION\"><name><references><!--ref3--><span><target id=\"t7\" /> <target id=\"t6\" /> <target id=\"t5\" /></span></references></name>"
				        + "<title>M3</title></entity></entities>";*/
				
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
			    is.setCharacterStream(new StringReader(openerAnswer));

			    Document doc = db.parse(is);
			    NodeList nodes = doc.getElementsByTagName("entity");
			    
			    for (int i = 0; i < nodes.getLength(); i++) {
			        Element element = (Element) nodes.item(i);
                    String type = element.getAttribute("type");
                    logger.log("entities types:" + i + type);
			        NodeList name = element.getElementsByTagName("references");
			        Element line = (Element) name.item(0);
			        logger.log("entities names: " + i + " " + getCharacterDataFromElement(line));
			        
			        
			       
			      //  NodeList tag = element.getElementsByTagName("tag");
			       // Element line1 = (Element) name.item(0);
			       // logger.log("taggs: " + i + " " + getCharacterDataFromElement(line1));
			        

			       /* NodeList title = element.getElementsByTagName("title");
			        line = (Element) title.item(0);
			        logger.log("Title: " + i + " " + getCharacterDataFromElement(line));*/
			      }
			    NodeList node = doc.getChildNodes();
			    String node1 = doc.getNodeName();
			    Node node2 = doc.getParentNode();
			    short node3 = doc.getNodeType();
			   
			    logger.log("node:" + node.toString());
			    logger.log("node1:" + node1.toString());
			    logger.log("node2:" + node2.toString());
			    logger.log("node3:" + node3);
			    
			    
			    
			    NodeList nodes1 = doc.getElementsByTagName("references");
			    for (int i = 0; i < nodes1.getLength(); i++) {
			        Element element = (Element) nodes1.item(i);
                   
			        NodeList name = element.getElementsByTagName("tag");
			        Element line = (Element) name.item(0);
			        logger.log("tag: " + i + " " + getCharacterDataFromElement(line));
			    }
			
			
			
			
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
			/*SAXBuilder sb = new SAXBuilder();
			//Document doc = sb.build(new StringReader(openerAnswer));
			Document doc = sb.build(new StringReader(xmlRecords));
			Element root = doc.getRootElement();
			List<Element> elements = root.getChildren("employee");
			//<List>Element idElt = root.getChild("entity");
			//Element idElt = root.getChild("entity");
			boolean bool = elements.isEmpty();
			String entité = elements.toString();
			
			
			logger.log(">>>>>>>>>>bool:" + bool);
			logger.log(">>>>>>>>>>entité:" + entité);*/
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
		
	    
		
	
	
	
	
	
	
	    return result;
	}
	
	public static String getCharacterDataFromElement(Element e) {
	    Node child = e.getFirstChild();
	    if (child instanceof CharacterData) {
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
{	String temp = "";

writeRawResultsStr(article, temp);
}

}
