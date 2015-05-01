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
import java.io.FileNotFoundException;
import java.util.List;

import tr.edu.gsu.nerwip.tools.corpus.archive.ArticleComparison;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class was used to detect problematic annotated articles. Sometimes, 
 * there is a difference between the raw and linked versions of the same
 * article. This is possible due to bugs in the editor, or plain mistakes.
 * <br/>
 * This version is an up-to-date version of {@link ArticleComparison}.
 * 
 * @author Vincent Labatut
 */
@SuppressWarnings({ "deprecation", "javadoc" })
public class TextComparison
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
	{	logger.setName("Text-Comparison");
		
		compareAll();
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
	 * the linked version, after having removed the
	 * links. The goal is to make sure both versions
	 * are exactly similar, besides the links.
	 * 
	 * @return
	 * 		Number of incorrect articles found in the corpus.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing a file.
	 */
	private static int compareAll() throws FileNotFoundException
	{	logger.log("Process all articles in the out folder");
		logger.increaseOffset();
		int result = 0;
		
		// get the list of articles
		List<File> articles = ArticleLists.getArticleList();
		for(File article: articles)
		{	if(!compareVersions(article))
				result++;
		}		
		
		logger.decreaseOffset();
		logger.log("Process over, number of incorrect articles="+result);
		return result;
	}
	
	/**
	 * Compares the raw text version of an article with
	 * the linked version, after having removed the
	 * links. The goal is to make sure both versions
	 * are exactly similar, besides the links.
	 * 
	 * @param article
	 * 		Folder of the article to check.
	 * @return
	 * 		{@code true} iff both texts are exactly similar, besides the annotations.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while reading the files.
	 */
	private static boolean compareVersions(File article) throws FileNotFoundException
	{	String name = article.getName();
		logger.log("Comparing two versions of the same article "+name);
		logger.increaseOffset();
		
		// retrieve raw text
		String rawPath = article.getPath() + File.separator + FileNames.FI_RAW_TEXT;
		logger.log("Retrieve raw text: "+rawPath);
		String raw = FileTools.readTextFile(rawPath);
		
		// retrieve linked text
		String linkedPath = article.getPath() + File.separator + FileNames.FI_LINKED_TEXT;
		logger.log("Retrieve linked text: "+linkedPath);
		String linked = FileTools.readTextFile(linkedPath);
		
		// compare content
		boolean result = compareContent(raw, linked);
		
		logger.log("Comparison over for article "+name+", result="+result);
		logger.decreaseOffset();
		return result;
	}

	/**
	 * Compares the raw text version of an article with
	 * the linked version, after having removed the
	 * links. The goal is to make sure both versions
	 * are exactly similar, besides the links.
	 * 
	 * @param raw
	 * 		Raw text.
	 * @param linked
	 * 		Linked text (raw text + hyperlinks).
	 * @return
	 * 		{@code true} iff both texts are exactly similar, besides the links.
	 */
	private static boolean compareContent(String raw, String linked)
	{	logger.log("Comparing the article contents");
		logger.increaseOffset();
		
		// remove links from the annotated file
		String delinked = linked.replaceAll("<a (.*?)>", "");
		delinked = delinked.replaceAll("</a>", "");
		
		// compare each character one by one
		boolean result = true;
		int i;
		for(i=0; i<raw.length(); i++)
		{	char r = raw.charAt(i);
			
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
		if(i<delinked.length())
			result = false;
		
		logger.log("Comparison over: result="+result);
		logger.decreaseOffset();
		return result;
	}
}
