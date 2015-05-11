package tr.edu.gsu.nerwip.recognition.internal.modelless.opener;

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
	public Entities convert(Article article, List<String> text) throws ConverterException
	{	logger.increaseOffset();
		Entities result = new Entities(recognizerName);

		
		return result;
	}
	
	
	private AbstractEntity<?> convertElement(Element element, Map<String,Element> metaData, int prevSize)
	{	AbstractEntity<?> result = null;
		
		
		
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
{	i++;
temp = temp + "\n>>> Chunk " + i + "/" + intRes.size() + " <<<\n" + str + "\n";
}
writeRawResultsStr(article, temp);
}

}
