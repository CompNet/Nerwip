package tr.edu.gsu.nerwip.recognition.internal.modelless.opener;

import java.io.IOException;
import java.io.StringReader;
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
public class OpeNERConverter extends AbstractInternalConverter<List<String>>
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
	{	CONVERSION_MAP.put("PERSON", EntityType.PERSON);
		CONVERSION_MAP.put("PLACE", EntityType.LOCATION);
		CONVERSION_MAP.put("ORGANIZATION", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("DATE", EntityType.DATE);
		
	}
	
//	/** Pattern previously used to adjust entity positions */ 
//	private final static Pattern DOC_PATTERN = Pattern.compile("\\n\\n");
	
	
/////////////////////////////////////////////////////////////////
// XML NAMES		/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
	/** Element of the ner format */
	private final static String ELT_ENTITY="entity";
	/** Element of the ner format */
	private final static String ELT_REFERENCES="references";
	/** Element of the ner format */
	private final static String ELT_WF="wf";
	
	/** Attribute of the OC format */
	private final static String ATT_TYPE="type";
	/** Attribute of the OC format */
	private final static String ATT_WID="wid";
	/** Attribute of the OC format */
	private final static String ATT_OFFSET="offset";
	/** Attribute of the OC format */
	private final static String ATT_LENGTH="length";
	
	/** Item of the OC format */
	private final static String ITEM_ID="id";
	
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	//@SuppressWarnings("unchecked")
	@Override
	public Entities convert(Article article, List<String> data) throws ConverterException
	{	logger.increaseOffset();
		Entities result = new Entities(recognizerName);
		
		logger.log("Processing each chunk of data and the associated answer");
		Iterator<String> it = data.iterator();
		logger.increaseOffset();
		int i = 0;
		int prevSize = 0;
		while(it.hasNext())
		{	i++;
			logger.log("Processing chunk "+i+"/"+data.size()/2);
			String originalText = it.next();
			String openerAnswer = it.next();
			
			// extracting entities part from opeNer result text
			logger.log("extracting entities part from opener answer");
			String openerEntities = extractEntities(openerAnswer);
			
			// extracting tokens part from opener result text
			logger.log("extracting tokens part from opener answer");			
			String tokens = extractTokenizedText(openerAnswer);
			
			try {
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(openerEntities));
	            Document doc = db.parse(is);
				NodeList nodes = doc.getElementsByTagName(ELT_ENTITY);
				NodeList nodes1 = doc.getElementsByTagName(ELT_REFERENCES);
				String id = null;
				//String idend = null;
				String wid = null;
				for (int m = 0; m < nodes.getLength(); m++)
				{
					Element element = (Element) nodes.item(m);
				    Element element1 = (Element) nodes1.item(m);
	                // parsing entities types
				    String typeStr = element.getAttribute(ATT_TYPE);
	                logger.log("type entity" + m + ": " +  typeStr);
	                EntityType type = CONVERSION_MAP.get(typeStr);
				    // parsing entities names
	                NodeList name = element.getElementsByTagName(ELT_REFERENCES);
				    Element line = (Element) name.item(0);
				    String valueStr = getCharacterDataFromElement(line);
				    logger.log("name entity" + m + ": "  + valueStr);
				    
				        
				    // parsing entities ids
		            Node lastChild = element1.getLastChild();
		            Node child = lastChild.getFirstChild(); //target
		            id = child.getAttributes().getNamedItem(ITEM_ID).getNodeValue();
		            //logger.log("id:" + m + id);
		                
		            // extracting endPos & startPos
		            id = id.replace(id.charAt(0), 'w');
		            //idend = "w" + nbr;
		            //logger.log("idend:" + idend);
		            //logger.log("id:" + m + id); 
		                
		            DocumentBuilder db2 = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				    InputSource is2 = new InputSource();
					is2.setCharacterStream(new StringReader(tokens));
	                Document doc2 = db2.parse(is2);
					NodeList nodes2 = doc2.getElementsByTagName(ELT_WF);
					
					int j = 0;
					String offset = null;
					String length = null;
					do 
					{
						Element element2 = (Element) nodes2.item(j);
						wid = element2.getAttribute(ATT_WID);
						if (id.equals(wid)) {						
							offset = element2.getAttribute(ATT_OFFSET);
							//logger.log("offset: " + offset);
							length = element2.getAttribute(ATT_LENGTH);
							//logger.log("length: " + length);
							int off = Integer.parseInt(offset);
							int startPos = off;
							logger.log("startPos entity" + m + ": " + startPos);

							//logger.log("off: " + off);
							int leng = Integer.parseInt(length);
							//logger.log("leng: " + leng);				                         
							int endPos = off + leng - 1;						
							logger.log("endPos entity" + m + ": " + endPos);
							//entity = AbstractEntity.build(type, startPos, endPos, recognizerName, valueStr);	
							//logger.log("entity value" + entity.getStringValue());
							//boolean check = entity.checkText(article);												
							//result.addEntity(entity);
							
							
							
							
							
						}
						/*if (idend.equals(wid)) 
						{offset = element2.getAttribute(ATT_OFFSET);
						length = element2.getAttribute(ATT_LENGTH);
						int off = Integer.parseInt(offset);
						int leng = Integer.parseInt(length);
						//logger.log("leng: " + leng);				                         
						endPos = off + leng - 1;						
						logger.log("endPos entity" + m + ": " + endPos);
							
						}*/
					    //else {logger.log("ERROR" + m);}
					    j++;
					    //logger.log("j= " + j);
					    
					    //logger.log(">>>>wid =" + wid + " ||  id =" + id);
					    //logger.log("id =" + id);
					    
					    }
					while((id != wid) && (j < nodes2.getLength()) );
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
			
		}
		return result;
	}
			
			
			
			
		
		
		
		
		
		
		
		
		
		
		
		
		// extracting entities part from opeNer result text
		/*logger.log("extracting entities part from opener result");
		String openerAnswer = null;
		
		openerAnswer = extractEntities(data);
		
		// extracting tokens part from opener result text
		logger.log("extracting tokens part from opener result");
		String tokens = null;
		tokens = extractTokenizedText(data);*/
	 
		
		// extracting entities				
		/*try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(openerAnswer));
            Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName(ELT_ENTITY);
			NodeList nodes1 = doc.getElementsByTagName(ELT_REFERENCES);
			String id = null;
			String idend = null;
			String wid = null;
			for (int m = 0; m < nodes.getLength(); m++)
			{
				Element element = (Element) nodes.item(m);
			    Element element1 = (Element) nodes1.item(m);
                // parsing entities types
			    String typeStr = element.getAttribute(ATT_TYPE);
                logger.log("type entity" + m + ": " +  typeStr);
                type = CONVERSION_MAP.get(typeStr);
			    // parsing entities names
                NodeList name = element.getElementsByTagName(ELT_REFERENCES);
			    Element line = (Element) name.item(0);
			    valueStr = getCharacterDataFromElement(line);
			    logger.log("name entity" + m + ": "  + valueStr);
			    //int nbesp = valueStr.split(' '.toString());
			    //int nbrsp = numberSpace(valueStr);
			    //nbrsp++;
			    //String nbr = String.valueOf(nbrsp);
			    
			        
			    // parsing entities ids
	            Node lastChild = element1.getLastChild();
	            Node child = lastChild.getFirstChild(); //target
	            id = child.getAttributes().getNamedItem(ITEM_ID).getNodeValue();
	            //logger.log("id:" + m + id);
	                
	            // extracting endPos & startPos
	            id = id.replace(id.charAt(0), 'w');
	            //idend = "w" + nbr;
	            //logger.log("idend:" + idend);
	            //logger.log("id:" + m + id); 
	                
	            DocumentBuilder db2 = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			    InputSource is2 = new InputSource();
				is2.setCharacterStream(new StringReader(tokens));
                Document doc2 = db2.parse(is2);
				NodeList nodes2 = doc2.getElementsByTagName(ELT_WF);
				
				int j = 0;
				String offset = null;
				String length = null;
				do 
				{
					Element element2 = (Element) nodes2.item(j);
					wid = element2.getAttribute(ATT_WID);
					if (id.equals(wid)) {						
						offset = element2.getAttribute(ATT_OFFSET);
						//logger.log("offset: " + offset);
						length = element2.getAttribute(ATT_LENGTH);
						//logger.log("length: " + length);
						int off = Integer.parseInt(offset);
						startPos = off;
						logger.log("startPos entity" + m + ": " + startPos);

						//logger.log("off: " + off);
						int leng = Integer.parseInt(length);
						//logger.log("leng: " + leng);				                         
						endPos = off + leng - 1;						
						logger.log("endPos entity" + m + ": " + endPos);
						//entity = AbstractEntity.build(type, startPos, endPos, recognizerName, valueStr);	
						logger.log("entity value" + entity.getStringValue());
						//boolean check = entity.checkText(article);												
						//result.addEntity(entity);
						
						
						
						
						
					}
					
				    j++;
				   
				    
				    }
				while((id != wid) && (j < nodes2.getLength()) );
				}
			}
		/*catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	/**
	 * Receives an element and get 
	 * data from it.
	 *  
	 * @param element
	 * 		Element to process.
	 * @return
	 * 		The part correspending to entities.
	 */
	public static String getCharacterDataFromElement(Element element) 
	{	Node child = element.getFirstChild();
	if (child instanceof CharacterData) 
	{
		CharacterData cd = (CharacterData) child;
	    return cd.getData();
	    }
	return "";
	}
	
	/**
	 * Receives the result data of opener 
	 * and extract from it the part correspending 
	 * to entities.
	 *  
	 * @param data
	 * 		String data to process.
	 * @return
	 * 		The part correspending to entities.
	 */
	
	public String extractEntities (String data)
	{
		String openerAnswer = null;	    
	    Pattern pattern = Pattern.compile("(?<=<entities>).*.(?=</entities>)");
		Matcher matcher = pattern.matcher(data);
        String xmlentities = new String();
		boolean found = false;
		while (matcher.find()) 
		{
			xmlentities = matcher.group().toString();
	        //logger.log(">>>>>>>>>>>xml entities: " + xmlentities);
			found = true;
			}
		if (!found)
		{
			logger.log("ERROR: text not found");
			}
		openerAnswer =  "<entities>" + xmlentities + "</entities>";
		openerAnswer = openerAnswer.replaceAll("\\p{Space}\\p{Space}|\\p{Space}\\p{Space}\\p{Space}|\\p{Space}\\p{Space}\\p{Space}\\p{Space}", "");
		logger.log(">>>>>>>>>>>>>>entities:" + openerAnswer);
		return openerAnswer;
	}
	
	/**
	 * Receives the result data of opener 
	 * and extract from it the part correspending 
	 * to tokenized text.
	 *  
	 * @param data
	 * 		String data to process.
	 * @return
	 * 		The tokenized text.
	 */
	
	
	
	
	public String extractTokenizedText(String data)
	{  String extraText = null;
	   String tokens = null;    
       Pattern pattern = Pattern.compile("(?<=<text>).*.(?=</text>)");
	   Matcher matcher = pattern.matcher(data);
    
	   boolean found = false;
	   while (matcher.find())
	   {
		extraText = matcher.group().toString();
        //logger.log(">>>>>>>>>>>text: " + extraText);
		found = true;
		}
	   if (!found)
	   {
		   logger.log("ERROR: text not found");
		   }
	   tokens = "<text>" + extraText + "</text>";
	   tokens = tokens.replaceAll("\\p{Space}\\p{Space}|\\p{Space}\\p{Space}\\p{Space}|\\p{Space}\\p{Space}\\p{Space}\\p{Space}", "");
	   logger.log(">>>>>>>>>>>>>>tokens:" + tokens);
	   return tokens;
	   }
	
	/**
	 * Receives the name of entity  
	 * and returns the number of space  
	 * in this string.
	 *  
	 * @param name
	 * 		String name to process.
	 * @return
	 * 		String number of space.
	 */
	
	public int numberSpace(String name)
	{   int nbres = 0;
		for (int i=0; i<name.length(); i++)
		{char ch = name.charAt(i);
		
		if (ch == ' ')
			nbres++ ;
		}
		
		return nbres ;
		}
	

	
	
 	    
 	
 	
	
	
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
		temp = temp + "\n>>> Chunk " + (i/2) + "/" + intRes.size() + " - OpeNER Response <<<\n" + str + "\n";
}
writeRawResultsStr(article, temp);
}

}
