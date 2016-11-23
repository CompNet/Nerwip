package fr.univavignon.nerwip.tools.corpus.archive;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import fr.univavignon.nerwip.tools.corpus.ArticleLists;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class was used to detect problematic annotated articles. Sometimes, 
 * the old Stanford Manual Annotation Tool is changing characters, which 
 * causes problems when evaluating NER performance.
 * 
 * @deprecated 
 * 		Stanford annotator is not used anymore, 
 * 		we have our own application for that.
 * 
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public class ArticleComparison
{	
	/**
	 * Launches the comparison process.
	 * 
	 * @param args
	 * 		None needed.
	 * @throws Exception
	 * 		Problem while comparing.
	 */
	public static void main(String[] args) throws Exception
	{	boolean includeAnnotatedText = false;
		compareAll(includeAnnotatedText);
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
	 * For each article in the corpus,
	 * compares the raw text version of the article with
	 * the annotated version, after having removed the
	 * annotations. The goal is to make sure both versions
	 * are exactly similar, besides the annotations.
	 * 
	 * @param processAnnotated
	 * 		Whether annotated text should be processed, or not.
	 * @return
	 * 		Number of incorrect articles found in the corpus.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing a file.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle encoding.
	 */
	private static int compareAll(boolean processAnnotated) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.log("Process all articles in the out folder");
		logger.increaseOffset();
		int result = 0;
		
		// get the list of articles
		List<File> articles = ArticleLists.getArticleList();
		for(File article: articles)
		{	if(!compareVersions(article,processAnnotated))
				result++;
		}		
		
		logger.decreaseOffset();
		logger.log("Process over, number of incorrect articles="+result);
		return result;
	}
	
	/**
	 * Compares the raw text version of an article with
	 * the annotated version, after having removed the
	 * annotations. The goal is to make sure both versions
	 * are exactly similar, besides the annotations.
	 * 
	 * @param article
	 * 		Folder of the article to check.
	 * @param processAnnotated
	 * 		Whether annotated text should be processed, or not.
	 * @return
	 * 		{@code true} iff both texts are exactly similar, besides the annotations.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while reading the files.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle encoding.
	 */
	private static boolean compareVersions(File article, boolean processAnnotated) throws FileNotFoundException, UnsupportedEncodingException
	{	String name = article.getName();
		logger.log("Comparing two versions of the same article "+name);
		logger.increaseOffset();
		
		// retrieve raw text
		String rawPath = article.getPath() + File.separator + FileNames.FI_RAW_TEXT;
		logger.log("Retrieve raw text: "+rawPath);
		String raw = FileTools.readTextFile(rawPath, "UTF-8");
		
		// retrieve annotated text
		String annotatedPath = article.getPath() + File.separator + FileNames.FI_REFERENCE_TEXT;
		String annotated = null;
		if(processAnnotated)
		{	logger.log("Retrieve annotated text: "+annotatedPath);
			annotated = FileTools.readTextFile(annotatedPath, "UTF-8");
		}
		
		// retrieve linked text
		String linkedPath = article.getPath() + File.separator + FileNames.FI_LINKED_TEXT;
		logger.log("Retrieve linked text: "+linkedPath);
		String linked = FileTools.readTextFile(linkedPath, "UTF-8");
		
		// compare content
		boolean result = compareContent(raw, annotated, linked);
		
		logger.log("Comparison over for article "+name+", result="+result);
		logger.decreaseOffset();
		return result;
	}

	/**
	 * Compares the raw text version of an article with
	 * the annotated version, after having removed the
	 * annotations. The goal is to make sure both versions
	 * are exactly similar, besides the annotations.
	 * 
	 * @param raw
	 * 		Raw text.
	 * @param annotated
	 * 		Annotated text (raw text + mentions).
	 * @param linked
	 * 		Linked text (raw text + hyperlinks).
	 * @return
	 * 		{@code true} iff both texts are exactly similar, besides the annotations.
	 */
	private static boolean compareContent(String raw, String annotated, String linked)
	{	logger.log("Comparing the article contents");
		logger.increaseOffset();
		
		// remove mentions from the annotated file
		String deannotated = null;
		if(annotated!=null)
			deannotated = annotated.replaceAll("<tag name=\"[A-Z]*\" value=\"[a-z]*\"/>", "");
//		String urlRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		String delinked = linked.replaceAll("<a (.*?)>", "");
		delinked = delinked.replaceAll("</a>", "");
		
		// compare each character one by one
		boolean result = true;
		int i;
		for(i=0; i<raw.length(); i++)
		{	char r = raw.charAt(i);
			
			if(deannotated!=null)
			{	if(i<deannotated.length())
				{	char a = deannotated.charAt(i);
					if(r != a)
					{	result = false;
						logger.log("in annotated text: '"+a+"' and '"+r+"' are different ("+String.format("\\u%04x",(int)a)+" and "+String.format("\\u%04x",(int)r)+"), position "+i);
						System.err.println(raw.substring(i,Math.min(raw.length()-1,i+50)));
					}
				}
				else
					result = false;
			}
			
			if(i<delinked.length())
			{	char l = delinked.charAt(i);
				if(r != l)
				{	result = false;
					logger.log("in linked text: '"+l+"' and '"+r+"' are different ("+String.format("\\u%04x",(int)l)+" and "+String.format("\\u%04x",(int)r)+"), position "+i);
					System.err.println(raw.substring(i,Math.min(raw.length()-1,i+50)));				
				}
			}
			else
				result = false;
		}
		if(deannotated!=null && i<deannotated.length())
			result = false;
		if(i<delinked.length())
			result = false;
		
		logger.log("Comparison over: result="+result);
		logger.decreaseOffset();
		return result;
	}
}
