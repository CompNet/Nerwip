package tr.edu.gsu.nerwip.recognition.external.tagen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;

/**
 * This class is the converter associated to TagEN.
 * It is able to convert the text outputed by this NER tool
 * into objects compatible with Nerwip.
 * <br/>
 * It can also read/write these results using raw text
 * and our XML format.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class TagENConverter extends AbstractExternalConverter
{
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 * 		Folder used to stored the results of the NER tool.
	 */
	public TagENConverter(String nerFolder)
	{	super(RecognizerName.TAGEN, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}
	
	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of URI to entity type conversion */
	
    private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("date", EntityType.DATE);
		CONVERSION_MAP.put("location", EntityType.LOCATION);
		CONVERSION_MAP.put("organization", EntityType.ORGANIZATION);
		//CONVERSION_MAP.put("percent", EntityType.PERCENT);
		CONVERSION_MAP.put("person", EntityType.PERSON);
	}
	
	
	public String type(int i, String ch) 
    {   String type = new String();
        i++;
        do {
        	type = type + ch.charAt(i);
        	i++;
        	}
        while ( ch.charAt(i) != '>');
        return type;
        
    }
    
    public boolean closeTag(int i, String ch) 
    {
 	   boolean close = false;
 	   if (ch.charAt(i) == '<')
 	   {
 		   if (ch.charAt(i+1) == '/')
            close = true;
 		   }
 	   else close = false;
 	   return close;
 	   
    }
		
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
    public Entities convert(Article article, String data) throws ConverterException
	{   Entities result = new Entities(recognizerName);
		//String originalText = article.getRawText();
		//AbstractEntity<?> entity = null;
		
		//process output text
        logger.log(" start processing output");
        String text = null;
        String text2 = null;
        String regex1 = "<enamex>|</enamex>|<timex>|</timex>|<numex>|</numex>|<range>|</range>";

        text = data.replaceAll(regex1, "");
        logger.log("text: " + text);
        
      //processing
            String originalText = null;
      		text = text.replaceAll("null", "");
      		try {
      			//InputStream ips = new FileInputStream(FileNames.FO_TAGEN + File.separator + "input.txt");
      			InputStream ips = new FileInputStream("/home/sabrine/TagEN/input.txt");
      			//InputStream ips = new FileInputStream("res/ner/tagen/input.txt");
      			
      			originalText = IOUtils.toString(ips).trim();
      			logger.log("originalText = " + originalText);
      		} catch (IOException e) {
      			// TODO Auto-generated catch block
      			e.printStackTrace();
      		}
      		
      		
      		char co = originalText.charAt(0);
    	    int i = 0;
    	    char cc = text.charAt(0);
    	    int j = 0;
    	    int startPos = 0;
    	    int endPos = 0;
    	    do {
    	    	co = originalText.charAt(i); 
    	        cc = text.charAt(j);
    	        if (co == cc)
    	           { i++;
    	             j++;}
    	        else 
    	           {
    	        	   int start = i;
    	        	   
    	        	   startPos = start ;
    	        	   logger.log("startPos = " + startPos);
    	               String typeCode = type(j, text);
    	               //System.out.println("j= " + j);
    	               logger.log("typecode= " + typeCode);
    	               EntityType type = CONVERSION_MAP.get(typeCode);
    	               j = j + typeCode.length() + 2; // check l value here
    	               //logger.log("j= " + j);
    	        		
    	               String name = " ";
    	               //System.out.println("test= " + closeTag(j, text));
    	               boolean test = closeTag(j, text); 
    	               //while (  test == false );
    	               if (test == false)
    	               { //System.out.println("here1");
    	            	   do {
    	            		   name = name + text.charAt(j);		   
    	        			   i++;
    	        			   j++;
    	        			  
    	        			   }
    	            	   while(text.charAt(j) != '<');
    	            	   logger.log("name=" + name);
    	            	   if (text.charAt(j) == '<' && text.charAt(j+1) == '/')
    	            	   { int end = i;
    	            	   endPos = end ;
    	            	   logger.log("endPos= " + endPos);
    	            	   if((type!=null) & (startPos != 0) & (endPos != 0))
   			            {
   			            	AbstractEntity<?> entity = AbstractEntity.build(type, startPos, endPos, recognizerName, name);						
   						    if(entity!=null)
   						    {
   						    	result.addEntity(entity);
   						    	
   						    }
   						    
   			            }
    	        		     String ch = type(j, text);
    	        		     j = j + ch.length() + 2;
    	        		     //logger.log("j= " +j);
    	        		     //logger.log("i= " +i);
    	        		     
    	        		     }
    	            	   else {
    	            		  // Entities list = f(i, j, originalText, text);
    	        			  // result.addEntities(list);
    	        			   }
    	            	   }
    	               }
    	           }
    	    	
    	    	
    	    while (i < originalText.length());
    	    
		
		return result;
	}
	
	
/////////////////////////////////////////////////////////////////
// RAW				/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
    //@Override
   /* protected void writeRawResults(Article article, String intRes) throws IOException
    {	String temp = "";


    writeRawResultsStr(article, temp);

    }*/
}
