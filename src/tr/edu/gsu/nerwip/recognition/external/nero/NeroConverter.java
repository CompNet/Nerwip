package tr.edu.gsu.nerwip.recognition.external.nero;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;




import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.codec.binary.StringUtils;
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
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalConverter;
import tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to Nero.
 * It is able to convert the text outputed by this NER tool
 * into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class NeroConverter extends AbstractExternalConverter
{
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the NER tool.
	 */
	public NeroConverter(String nerFolder)
	{	super(RecognizerName.NERO, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}
	
	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of URI to entity type conversion */
	
    private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("time", EntityType.DATE);
		CONVERSION_MAP.put("loc", EntityType.LOCATION);
		CONVERSION_MAP.put("org", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("fonc", EntityType.FUNCTION);
		CONVERSION_MAP.put("pers", EntityType.PERSON);
	}
		
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	@Override
    public Entities convert(Article article, String data) throws ConverterException
	{   Entities result = new Entities(recognizerName);
		String originalText = article.getRawText();
		AbstractEntity<?> entity = null;
		// 2ème algo
		char co = originalText.charAt(0);
		int i = 0;
		char cc = data.charAt(0);
		int j = 0;
		do {
			co = originalText.charAt(i);
			cc = data.charAt(j);
			if (co == cc) {
				i++;
				j++;
			} else {
				int start = i;
				String typeCode = type(j, data);
				EntityType type = CONVERSION_MAP.get(typeCode);
				j = j + typeCode.length() + 2; // check l value here

				String name = " ";
				while (closeTag(j, data) == false)
					;
				{
					do {
						name = name + originalText.charAt(i);
						i++;
						j++;
					} while (co == cc);
					if (data.charAt(j) == '<' && data.charAt(j + 1) == '/') {
						int end = i;
						entity = AbstractEntity.build(type, start, end,
								recognizerName, name);
						result.addEntity(entity);
						String ch = type(j, data);
						j = j + ch.length() + 3;
					} else {
						Entities list = f(i, j, originalText, data);
						result.addEntities(list);
					}
				}
			}
		} while (i <= originalText.length());
		return result;
	}

	public Entities f(int i, int j, String text1, String text2)
	{	Entities entities = null;
		Entities result = new Entities(recognizerName);
		AbstractEntity<?> entity = null;
		int start = i;
		char co = text1.charAt(i);
		char cc = text2.charAt(j);
		String typeCode = type(j, text2);
		EntityType type = CONVERSION_MAP.get(typeCode);
		j = j + typeCode.length() + 2; // check l value here
		String name = " ";
		while (closeTag(j, text2) == false)
		{
			do {
				name = name + text1.charAt(i);
				i++;
				j++;
			} while (co == cc);
			if (text2.charAt(j) == '<' && text2.charAt(j + 1) == '/') {
				int end = i;
				entity = AbstractEntity.build(type, start, end, recognizerName,
						name);
				result.addEntity(entity);
				String ch = type(j, text2);
				j = j + ch.length() + 3;
			} else {
				Entities list = f(i, j, text1, text2);
				result.addEntities(list);
			}
		}
		return entities;
	}

	public String type(int i, String ch)
	{	String type = new String();
		i++;
		do {
			type = type + ch.charAt(i);
			i++;
		} while (ch.charAt(i) != '>');
		return type;
	}

	// necessary :algo 2 :to know if i pos is clos tag
	public boolean closeTag(int i, String ch)
	{	boolean close = false;
		if (ch.charAt(i) == '<') {
			if (ch.charAt(i + 1) == '/')
				close = true;
		}
		return close;
	}

	/////////////////////////////////////////////////////////////////
	// RAW				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	protected void writeRawResults(Article article, String intRes) throws IOException 
	{	logger.log("rawresultfinal");
		writeRawResultsStr(article, intRes);
	}
}
