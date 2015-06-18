package tr.edu.gsu.nerwip.recognition.external.tagen;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleLanguage;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalRecognizer;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;

/**
 * This class acts as an interface with TagEN.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 	<li>{@code model}: {@link TagEnModelName#MUC_MODEL} for French and 
 * 					   {@link TagEnModelName#WIKI_MODEL} for English</li>
 * 	<li>{@code ignorePronouns}: {@code true}</li>
 * 	<li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class TagEn extends AbstractExternalRecognizer<TagEnConverter>
{	
	/**
	 * Builds and sets up an object representing the TagEN tool.
	 * 
	 * @param model
	 *      Model used to perform the entity detection.
	 * @param ignorePronouns
	 *      Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 *      Whether or not stop words should be excluded from the detection.
	 */
	public TagEn(TagEnModelName model, boolean ignorePronouns, boolean exclusionOn)
	{	super(false, ignorePronouns, exclusionOn);
		
		this.model = model;
		setIgnoreNumbers(false);
		
		//init converter
		converter = new TagEnConverter(getFolder());
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
		
		result = result + "_" + "model=" + model.toString();
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES 	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public List<EntityType> getHandledEntityTypes() 
	{	List<EntityType> result = model.getHandledTypes();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES 		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = model.canHandleLanguage(language);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// MODEL		 	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Model used during the entity detection */
	private TagEnModelName model = null;
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING 			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the temporary file generated for TagEn */
	private static final String TEMP_FILE = "temp.txt";
	/** Name of the file storing the TagEn console outputs */
	private static final String CONSOLE_FILE = "console.txt";
	/** TagEn parameters the user does not need to change */
	private static final String STATIC_PARAMS = ""
			+ "--align "		// resets the text exactly like it originally was (cancelling whatever preprocessin/cleaning was performed) 
			+ "--yes";			// automatically answers yes to all question of the type "do you want to overwrite some file?"
	/** Sets the level of verbosity for the TagEn tool */
	private static final Map<Integer,String> VERBOSITY_LEVEL = new HashMap<Integer,String>();
	{	VERBOSITY_LEVEL.put(0, "--silent");
		VERBOSITY_LEVEL.put(1, "--verbose");
		VERBOSITY_LEVEL.put(2, "--Verbose");
	}
	
	/**
	 * Returns the path of the temporary file created for TagEn (containing the article
	 * content).
	 * 
	 * @param article
	 * 		The concerned article.
	 * @return
	 * 		Path of the input file.
	 */
	private String getTempFile(Article article)
	{	String result = article.getFolderPath()
			+ File.separator + getFolder() 
			+ File.separator + TEMP_FILE;
		return result; 
	}
	
	/**
	 * Returns the path of the console file created to store the console 
	 * output of TagEn (for debug purposes).
	 * 
	 * @param article
	 * 		The concerned article.
	 * @return
	 * 		Path of the console file.
	 */
	private String getConsoleFile(Article article)
	{	String result = article.getFolderPath()
			+ File.separator + getFolder() 
			+ File.separator + CONSOLE_FILE;
		return result; 
	}
	
	/**
	 * Some characters must be cleaned from the text to be annotated by
	 * Nero, otherwise it outputs additional characters which makes the
	 * conversion much harder.
	 * 
	 * @param text
	 * 		Original text.
	 * @return
	 * 		Cleaned text.
	 */
	private String cleanText(String text)
	{	String result = text;
		
		result = result.replaceAll("«", "\"");
		result = result.replaceAll("»", "\"");
		
		return result;
	}
	
	@Override
	protected String detectEntities(Article article) throws RecognizerException
	{	logger.increaseOffset();
		String result = null;
		
        try
        {	// write article raw text in a file
        	String text = article.getRawText();
        	text = cleanText(text);
			String inputPath = getTempFile(article);
			File inputFile = new File(inputPath);
			logger.log("Copying the article content in input file "+inputFile);
			FileTools.writeTextFile(inputFile, text);
			
			// invoke the external tool and retrieve its output
			logger.log("Invoking TagEn: ");
			logger.increaseOffset();
				File outputFile = converter.getRawFile(article);
				String outputPath = outputFile.getPath();
				Runtime rt = Runtime.getRuntime();
				String mainCommand = "." + File.separator 
						+ FileNames.FO_TAGEN + File.separator + FileNames.FI_TAGEN_EXE 
						+ " :" + model.getParameter() + " "
						+ STATIC_PARAMS + " " + VERBOSITY_LEVEL.get(2)
						+ " " + inputPath + " " + outputPath;
		    	String[] commands = 
				{	"/bin/sh", "-c", 
					mainCommand
				};
		    	logger.log(Arrays.asList(commands));
				Process proc = rt.exec(commands);
			logger.decreaseOffset();
			
			// standard error (which is actually used by TagEn as the standard output)
			String console = "";
			{	BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				String line;
				while((line=stdError.readLine()) != null)
				{	System.out.println(line);
					console = console + "\n" + line;
				}
			}
			logger.log("Console output:");
			logger.increaseOffset();
				logger.log(console);
			logger.decreaseOffset();
			
			// possibly record the console output (for debug purposes)
			if(outRawResults)
			{	String consolePath = getConsoleFile(article);
				logger.log("Writing the console output in file "+consolePath);
				FileTools.writeTextFile(consolePath, console);
			}
			
			// possibly remove the temp file
			if(!outRawResults)
				inputFile.delete();
			
	        // read the result file
			result = FileTools.readTextFile(outputPath);
        }
		catch (IOException e)
		{	//e.printStackTrace();
			throw new RecognizerException(e.getMessage());
		}
        
		logger.decreaseOffset();
		return result;
    }
}
