package tr.edu.gsu.nerwip.recognition.internal.modelless.nero;

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

public class NeroConverter extends AbstractInternalConverter<String>
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
    public Entities convert(Article article, String text) throws ConverterException
   {	
        String NeroAnswer = new String();
        String res = new String();
       // Entities entities = new Entities(recognizerName);
        try
        {	// build DOM
        	logger.log("Build DOM");
        	SAXBuilder sb = new SAXBuilder();
        	Document doc = sb.build(new StringReader(text));
        	Element root = doc.getRootElement();
        	Element resultElt = root.getChild("result");
        	NeroAnswer = resultElt.getValue();
        	logger.log (">>>>result = " + NeroAnswer);
        	String originalText = article.getRawText();
        	logger.log(">>>>originalText = " +  originalText);
        	
        /*	char co = originalText.charAt(0);
        	int i = 0;
        	char cr = NeroAnswer.charAt(0);
        	int j = 0;
        	do {
        		co = originalText.charAt(i); 
        		cr = NeroAnswer.charAt(j);
        		if (co == cr)
        		{ res = res + co ;
        		i++;
        		j++;
        		}
        		else if (DiacriticalChar(co)==true && cr==' ')
        		{ res = res + co;
        		i++;
        		}
        		else if (cr== '<')
        			if (NeroAnswer.charAt(i+1)!='/')
        			{String word = funct(i, NeroAnswer);
        			res = res + word + '>';
        			i = word.length() + 1 ;
        			if ( NeroAnswer.charAt(i) == ' ')
        			{ i++ ;
        			}
        			}
        			else if (NeroAnswer.charAt(i+1) =='/')
        			{
        				if ( NeroAnswer.charAt(i-1) == ' ' )
        				{ j-- ;
        				String word1 = funct(i, NeroAnswer);
        				res = res + word1 + '>';
        				}
        				}
        			else logger.log ("error1");
        		else if (DiacriticalChar(co)==false && cr==' ')
        		{ j++;
        		}
        		else if (IsNumber(co) == true) 
        		{ String word2 = PassNumber(i, NeroAnswer);
        		res = res + word2;
        		i = i + word2.length();
        		String word3 = PassNumeric(j, NeroAnswer);
        		j = j + word3.length() + 1;
        		}
        		else logger.log("error2");
        		}
        	while (i <= originalText.length()); 
        	
        	//String regex = "<loc> | </loc> | <pers> | </pers> | <org> | </org> | <fonc> | </fonc> | <time> |</time>";
			  String regex = "<(.*)>"; 
			  res1.replaceAll(regex, " ");
			  boolean equal = res1.equals(originalText);
			  logger.log("equal : " + equal); */
        	
        	  // 2ème algo
        	char co = originalText.charAt(0);
        	int k = 0;
        	char cc = res1.charAt(0);
        	int l = 0;
        	Entities entities = new Entities(recognizerName);
        	AbstractEntity<?> entity = null;
        	do {
        		co = originalText.charAt(k); 
        		cc = res1.charAt(l);
        		if (co == cr)
        		{ k++;
        		l++;
        		}
        		else { int start = k;
        		//entityType type  = traiter cc (loc)
        		String typeCode = Type(l, res1);
        		EntityType type = CONVERSION_MAP.get(typeCode);
        		l = l + typeCode.length() + 2;
        		String name = " "; 
        		do {//rouge}
        			while ();
        			do {
        				name = name + originalText(k);
        				k++;
        				l++;
        			}
        			while(co = cr);
        			if (res1.charAt(l) = '<' && res1.charAt(l+1) = '/')
        			{ int end = k;
        			entity = AbstractEntity.build(type, start, end, recognizerName, name);
        			entities.addEntity(entity);
        			String ch = Type(l, res1);
        			l = l + ch.length() + 3;
        			}
        		}
        		}
        		}
        	
        	}
        catch (JDOMException e)
        {	e.printStackTrace();
		}
     catch (IOException e)
		{	e.printStackTrace();
		} 
     return entities;
     }




/**
 * Receives a character  and return
 * if it's a diacritical character 
 *  
 * @param c
 * 		character to process.
 * @return
 * 		boolean result.
 */
public boolean DiacriticalChar(char c)
{
	char[] letters = new char[] { 'é', 'è', 'ê', 'ë', 'à', 'â', 'î', 'ï', 'ô', 'ù', 'û', 'ü', 'ÿ', 'æ', 'œ', 'ç' };
	for (char x : letters) 
	{
		if (x == c) 
		{
        return true;
        }
		}
	return false;
	}


public String funct(int i, String ch)
{ String result = new String();
do { result = result + ch.charAt(i);
     i++;

}
while (ch.charAt(i)!= '>');
return result;

}

/**
 * Receives a character  and return
 * if it's a number 
 *  
 * @param c
 * 		character to process.
 * @return
 * 		boolean result.
 */

public boolean IsNumber(char c)
{
	char[] letters = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	for (char x : letters) {
		if (x == c) {
			return true;
			}
		}
	return false;
	}


////passer le nombre
public String PassNumber(int i, String ch)
{
	String result = new String();
	do { 
		result = result + ch.charAt(i);
		}
	while (IsNumber(ch.charAt(i)) == true);
	return result;
	}


/**
 * Receives a postion i  and a string
 * and return a string composed of the digital 
 * words existing from the i position. 
 *  
 * @param i
 * 		position of the character to start process with.
 * @return
 * 		the digital string.
 */

public String PassNumeric(int i, String ch)
{ String result = new String();
do { result = result + ch.charAt(i); }
while (ch.charAt(i) != '>');
	return result;
}

public String Type(int i, string ch)
{ String type = new String();
i++;
do {
type = type + ch.charAt(i);
i++;
}
while ( ch.charAt(i) != '>');
return type;

	}

	

/////////////////////////////////////////////////////////////////
// RAW				/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////

protected void writeRawResults(Article article, String text) throws IOException
{	logger.log("rawresultfinal");
}
	
	

}
