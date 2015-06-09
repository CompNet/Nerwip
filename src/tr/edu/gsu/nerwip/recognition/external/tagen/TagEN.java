package tr.edu.gsu.nerwip.recognition.external.tagen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalRecognizer;
import tr.edu.gsu.nerwip.tools.file.FileNames;
//import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;

/**
 * This class acts as an interface with TagEN.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * <li>{@code ignorePronouns}: {@code true}</li>
 * <li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * 
 * 
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class TagEN extends AbstractExternalRecognizer<TagENConverter>
{	
	/**
	 * Builds and sets up an object representing TagEN tool.
	 * @param ignorePronouns
	 *      Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 *      Whether or not stop words should be excluded from the
	 *      detection.
	 */

	public TagEN(boolean ignorePronouns, boolean exclusionOn)
	{	super(false, ignorePronouns, exclusionOn);
	
		setIgnoreNumbers(false);
		
		//init converter
		converter = new TagENConverter(getFolder());
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public RecognizerName getName()
	{	return RecognizerName.TAGEN;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getFolder()
	{	String result = getName().toString();
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "exclude=" + exclusionOn;
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES 	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entities recognized by TagEN */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList
	(
		EntityType.DATE, 
		EntityType.LOCATION, 
		EntityType.PERSON
		//EntityType.PERCENT
	);

	@Override
	public List<EntityType> getHandledEntityTypes() 
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES 		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this recognizer can treat */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList
	(	
		ArticleLanguage.EN, 
		ArticleLanguage.FR
	);

	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// PROCESSING 			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	public BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    public BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }
    
	@Override
	protected String detectEntities(Article article) throws RecognizerException
	{	logger.increaseOffset();
		String result = null;
		
		//logger.log("Début du programme");
        try {
        	// write article raw text in a file
        	String text = article.getRawText();
        	//String path = "/home/sabrine/TagEN/input.txt";
        	String path = FileNames.FO_TAGEN + File.separator + "input.txt";
			File file = new File(path);
        	FileTools.writeTextFile(file, text);
        	
        	String cmd = "chmod +x res/ner/tagen/tagen" ;
        	logger.log("start first process");
        	Process pr = Runtime.getRuntime().exec(cmd);
        	logger.log("finish process 1");
        	
        	String cmd1 = "sudo res/ner/tagen" ;
        	logger.log("start process");
        	Process pr1 = Runtime.getRuntime().exec(cmd1);
        	logger.log("finish process ");
        	
        	
        	//String command = "/home/sabrine/TagEN/./tagen :mucfr /home/sabrine/TagEN/input.txt";
        	//String command = "chmod +x " + FileNames.FO_TAGEN + File.separator + " ." + File.separator + "tagen" +  " :mucfr "  + file;
        	
        	
        	logger.log("start process 2");
        	//String command = "chmod +x res/ner/tagen/ ./res/ner/tagen/tagen res/ner/tagen/tagen.conf res/ner/tagen/input.txt";
        	String command = "res/ner/tagen/ ./res/ner/tagen/tagen :mucfr res/ner/tagen/input.txt";
        	
        	//String command = "sudo " + FileNames.FO_TAGEN + File.separator + "." + File.separator + "tagen :mucfr " + file;
        	//String command = "chmod +x " + FileNames.FO_TAGEN;
        	//alias tagen=~/tagen/tagen
            //String command = "alias tagen=res/ner/tagen/tagen";
        	
        	
        	
            Process p = Runtime.getRuntime().exec(command);
            
            BufferedReader output = getOutput(p);
            BufferedReader error = getError(p);
            String ligne = "";
            
         

            while ((ligne = output.readLine()) != null) {
               logger.log(ligne);
            }
           
            while ((ligne = error.readLine()) != null) {
                logger.log(ligne);
            }

            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.log("finish process 2");
        
        // extract text from input.tag.txt
    
        String tagOutput = null;
        
     
        try {
        	InputStream ips = new FileInputStream("/home/sabrine/TagEN/input.tag.txt");
        	//InputStream ips = new FileInputStream("res/ner/tagen/input.tag.txt");
        	
        	//InputStream ips = new FileInputStream(FileNames.FO_TAGEN + File.separator + "input.txt");
        	
 
        	 //originalText = IOUtils.toString(ips).trim();
        	
        	
        	InputStreamReader ipsr = new InputStreamReader(ips);
        	BufferedReader br = new BufferedReader(ipsr);
        	String ligne;
        	while ((ligne = br.readLine()) != null) {
        	logger.log(ligne);
        	result += ligne + "\n";
        	}
        	br.close();
        	} catch (Exception e) {
        		tagOutput =  e.toString();
        	
        	} 
        //logger.log(tagOutput);
        

		
		
		
		logger.decreaseOffset();
		return result;
		
        }
}
