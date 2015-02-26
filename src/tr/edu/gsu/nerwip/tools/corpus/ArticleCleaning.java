package tr.edu.gsu.nerwip.tools.corpus;

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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.StringTools;

/**
 * This class was used to clean articles, in order to avoid problems 
 * when evaluating NER performance.
 * <br/>
 * This cleaning concerns the removal of certain characters such as 
 * invisible spaces, non-standard punctuation marks, etc.
 * <br/>
 * It is an ad-hoc fix, used to solve  a problem regarding an already 
 * existing set of annotated files. Not to be used on newly retrieved 
 * articles.
 * 
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public class ArticleCleaning
{	
	/**
	 * Launches the cleaning process, consisting
	 * in removing/replacing certain characters
	 * from the texts.
	 * 
	 * @param args
	 * 		None needed.
	 * @throws Exception
	 * 		Problem while comparing.
	 */
	public static void main(String[] args) throws Exception
	{	boolean includeAnnotatedText = false;
		cleanAll(includeAnnotatedText);
	}
		
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Cleans each article in the corpus.
	 * 
	 * @param processAnnotated
	 * 		Whether annotated text should be processed, or not.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the file
	 */
	private static void cleanAll(boolean processAnnotated) throws IOException
	{	logger.log("Clean all articles in the out folder");
		logger.increaseOffset();
		
		// get the list of articles
		List<File> articles = ArticleLists.getArticleList();
		for(File article: articles)
			cleanArticle(article,processAnnotated);
		
		logger.decreaseOffset();
		logger.log("All articles cleaned");
	}
	
	/**
	 * Clean the specified article.
	 * 
	 * @param article
	 * 		Folder of the article to clean.
	 * @param processAnnotated
	 * 		Whether annotated text should be processed, or not.
	 * 
	 * @throws IOException 
	 * 		Problem while accessing the files.
	 */
	private static void cleanArticle(File article, boolean processAnnotated) throws IOException
	{	String name = article.getName();
		logger.log("Cleaning article "+name);
		logger.increaseOffset();
		
		// retrieve text content
		String rawPath = article.getPath() + File.separator + FileNames.FI_RAW_TEXT;
		logger.log("Retrieve raw text: "+rawPath);
		String raw = FileTools.readTextFile(rawPath);
		String linkedPath = article.getPath() + File.separator + FileNames.FI_LINKED_TEXT;
		logger.log("Retrieve linked text: "+linkedPath);
		String linked = FileTools.readTextFile(linkedPath);
		String annotatedPath = article.getPath() + File.separator + FileNames.FI_REFERENCE_TEXT;
		String annotated = null;
		if(processAnnotated)
		{	logger.log("Retrieve annotated text: "+annotatedPath);
			annotated = FileTools.readTextFile(annotatedPath);
			annotated = annotated.replaceAll("<tag name=\"MISC\" value=\"start\"/>", "");
			annotated = annotated.replaceAll("<tag name=\"MISC\" value=\"end\"/>", "");
		}
		String tab[] = {raw,linked,annotated};
		
		// compare content
		for(int i=0;i<tab.length;i++)
		{		
//System.out.println(i);
			tab[i] = replaceChars(tab[i]);
			tab[i] = StringTools.cleanSpaces(tab[i]);
		}
		
		// record new texts
		FileTools.writeTextFile(rawPath, tab[0]);
		FileTools.writeTextFile(linkedPath, tab[1]);
		if(processAnnotated)
			FileTools.writeTextFile(annotatedPath, tab[2]);
		
		logger.log("Cleaning over for article "+name);
		logger.decreaseOffset();
	}
	
	/**
	 * Replaces some characters causing
	 * problems by more appropriate ones.
	 *  
	 * @param text
	 * 		Original text.
	 * @return
	 * 		Modified text.
	 */
	public static String replaceChars(String text)
	{	String result = text;
		if(text!=null)
		{	

// note: if decode needed, see http://sourceforge.net/p/jerichohtml/discussion/350025/thread/b27712bb/?limit=25#510b/806c/7545/225d/7302/bda5
			
			
//{	Pattern pattern = Pattern.compile("&.*;");
//	Matcher matcher = pattern.matcher(result);
//	if(matcher.find())
//		System.out.print("");
//}
		
		
//if(text.contains("&#160;"))
//	System.out.print("");
//if(text.contains("&amp;"))
//	System.out.print("");
//if(text.contains("\ufeff"))
//	System.out.print("");
		
			result = result.trim();
			result = result.replaceAll("&amp;", "&");
			result = result.replace('\ufeff', ' ');
			result = result.replace('\u200e', ' ');
			result = result.replaceAll("&#160;"," ");
			result = result.replaceAll("&gt;",">");
			result = result.replaceAll("&lt;","<");
			result = result.replaceAll("&quot;","\"");
		
{	Pattern pattern = Pattern.compile("&.*;");
	Matcher matcher = pattern.matcher(result);
	if(matcher.find())
		logger.log("WARNING: found a non-treated html entity in \""+result.substring(matcher.start(),matcher.start()+50)+"\"");
}
		}
		
		return result;
	}
}
