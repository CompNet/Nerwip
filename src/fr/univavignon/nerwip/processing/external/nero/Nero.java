package fr.univavignon.nerwip.processing.external.nero;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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
import java.util.List;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.external.AbstractExternalProcessor;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * This class acts as an interface with Nero.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * <li>{@code tagger}: {@code CRF}</li>
 * <li>{@code flat}: {@code true}</li>
 * <li>{@code ignorePronouns}: {@code true}</li>
 * <li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * Official Nero website: <a
 * href="https://nero.irisa.fr/">https://nero.irisa.fr/</a>
 * <br/>
 * <b>Warning:</b> it should be noted Nero was originally designed 
 * to treat speech transcriptions, and is therefore not very 
 * robust when handling punctuation. It is also very sensitive to 
 * specific characters like {@code û} or {@code ë}, or combinations 
 * of characters such as newline {@code '\n'} followed by 
 * {@code '"'}. Those should be avoided at all cost in the
 * parsed text, otherwise the {@link NeroConverter} will not
 * be able to process Nero's output.
 * <br/>
 * Nero was tested only on Linux system.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class Nero extends AbstractExternalProcessor<NeroConverter>
{	
	/**
	 * Builds and sets up an object representing the Nero tool.
	 * 
	 * @param neroTagger
	 * 		NeroTagger used by Nero (CRF or FST).
	 * @param flat
	 * 		Whether mentions can contain other mentions ({@code false}) or
	 * 		are mutually exclusive ({@code true}).
	 * @param ignorePronouns
	 *      Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 *      Whether or not stop words should be excluded from the
	 *      detection.
	 */
	public Nero(NeroTagger neroTagger, boolean flat, boolean ignorePronouns, boolean exclusionOn)
	{	super(false, ignorePronouns, exclusionOn);
		
		this.neroTagger = neroTagger;
		this.flat = flat;
		
		setIgnoreNumbers(false);
		
		// init converter
		converter = new NeroConverter(getFolder());
	}

	/////////////////////////////////////////////////////////////////
	// NAME 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.NERO;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getFolder()
	{	String result = getName().toString();

		result = result + "_" + "tagger=" + neroTagger;
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "exclude=" + exclusionOn;

		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES 	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types recognized by Nero */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList
	(
		EntityType.DATE, 
		EntityType.FUNCTION, 
		EntityType.LOCATION, 
		EntityType.ORGANIZATION,
		EntityType.PERSON,
		EntityType.PRODUCTION
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
//		ArticleLanguage.EN, 
		ArticleLanguage.FR
	);

	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// TAGGER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** NeroTagger used by Nero */
	private NeroTagger neroTagger = null;
	
	/**
	 * Represents the neroTagger used by Nero.
	 * 
	 * @author Vincent Labatut
	 */
	public enum NeroTagger
	{	/** Use the Conditional Random Fields neroTagger */
		CRF,
		/** Use the Finite State Transducer neroTagger */
		FST;
	}

	/////////////////////////////////////////////////////////////////
	// PROCESSING 			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether mentions can contain other mentions ({@code false}) or are mutually exclusive ({@code true}) */
	private boolean flat = false;
	/** Switch used to enable the detection of non-flat mentions */
	private final static String FLAT_SWITCH = "-f2h";
	/** Name of the temporary file generated for Nero */
	private static final String TEMP_NAME = "temp";
	/** Maximal size of text part processed at once */
	private static final int MAX_SIZE = 25000;

	/**
	 * Returns the path of the temporary file
	 * created for Nero (containing the article
	 * content).
	 * 
	 * @param article
	 * 		The concerned article.
	 * @param part
	 * 		The concerned part of the article.
	 * @return
	 * 		Path of the temporary file.
	 */
	private String getTempFile(Article article, int part)
	{	String result = article.getFolderPath()
			+ File.separator + getFolder() 
			+ File.separator + TEMP_NAME
			+ "." + part + FileNames.EX_TEXT;
		return result; 
	}
	
	@Override
	protected String detectMentions(Article article) throws ProcessorException
	{	logger.increaseOffset();
		StringBuffer tempRes = new StringBuffer();
		String text = article.getRawText();

		// debug
//		String val = System.getenv("PATH");
//		System.out.println(val);
//		val = System.getenv("IRISA_NE");
//		System.out.println(val);
		
		// we need to break down the text: Nero can't handle more than 100000 chars at once
		// (at least on the test computer)
		List<String> parts = StringTools.splitText(text, MAX_SIZE);

		for(int i=0;i<parts.size();i++)
		{	logger.log("Processing Nero part #"+(i+1)+"/"+parts.size());
			logger.increaseOffset();
			String part = parts.get(i);
			
			try
			{	// write article raw text in a temp file
				part = cleanText(part);
				String tempPath = getTempFile(article,i);
				File tempFile = new File(tempPath);
				logger.log("Copying the article content in partial temp file "+tempFile);
//				FileTools.writeTextFile(tempFile, part, "UTF-8");
				FileTools.writeTextFile(tempFile, part, "ISO-8859-1");
				
				// invoke the external tool and retrieve its output
				logger.log("Invoking Nero: ");
				logger.increaseOffset();
					Runtime rt = Runtime.getRuntime();
					String mainCommand = "cat " + tempPath + " | " 
						+ "." + File.separator + FileNames.FO_NERO_SCRIPTS + File.separator 
						+ FileNames.FI_NERO_BASH + " " + neroTagger.toString();
				    if(!flat)
				    	mainCommand = mainCommand + " " + FLAT_SWITCH;
			    	String[] commands = 
					{	"/bin/sh", "-c", 
						mainCommand
					};
			    	logger.log(Arrays.asList(commands));
					Process proc = rt.exec(commands);
//		    		Process proc = rt.exec("/bin/sh -c echo $PATH"); // debug
				logger.decreaseOffset();
			
				// standard error
				String error = "";
				{	//BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream(),"ISO-8859-1"));
					String line;
					while((line=stdError.readLine()) != null)
					{	System.out.println(line);
						error = error + "\n" + line;
					}
				}
				if(!error.isEmpty())
				{	logger.log("Some error(s) occured:");
					logger.increaseOffset();
						logger.log(error);
					logger.decreaseOffset();
				}
				
				// standard output
				if(error.isEmpty())
				{	String res = "";
					//BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream(),"ISO-8859-1"));
					String line;
					while((line=stdInput.readLine()) != null)
					{	System.out.println(line);
						res = res + "\n" + line;
					}
					tempRes.append(res);
					logger.log("Raw results:");
					logger.increaseOffset();
						logger.log(res);
					logger.decreaseOffset();
					
					// possibly record the raw results (for debug purposes)
					if(outRawResults)
					{	File rrF = converter.getRawFile(article);
						logger.log("Writing the raw results in file "+rrF);
						FileTools.writeTextFile(rrF, res, "UTF-8");
					}
				}
				else
					throw new ProcessorException(error);
				
				// possibly remove the temp file
				if(!outRawResults)
					tempFile.delete();
				
				logger.decreaseOffset();
			}
			catch (IOException e)
			{	//e.printStackTrace();
				throw new ProcessorException(e.getMessage());
			}
		}
		
		logger.decreaseOffset();
		String result = tempRes.toString();
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
		
		result = result.replaceAll("ë", "e");
		result = result.replaceAll("û", "u");
		
		return result;
	}
}
