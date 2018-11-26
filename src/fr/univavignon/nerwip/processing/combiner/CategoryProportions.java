package fr.univavignon.nerwip.processing.combiner;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import org.xml.sax.SAXException;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleCategory;
import fr.univavignon.nerwip.tools.file.FileTools;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.retriever.ArticleRetriever;
import fr.univavignon.retriever.reader.ReaderException;

/**
 * This class represents how categories are distributed
 * over some collection of articles. It is used by various
 * combiners.
 * 
 * @author Vincent Labatut
 */
public class CategoryProportions extends HashMap<ArticleCategory,Float>
{	/** Class id */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Builds a new CategoryProportions object containing
	 * all the specified categories in uniform proportions.
	 * 
	 * @return
	 * 		New CategoryProportions instance.
	 */
	public static CategoryProportions buildUniformProportions()
	{	CategoryProportions result = new CategoryProportions();
		List<ArticleCategory> categories = Arrays.asList(ArticleCategory.values());
		float proportion = 1f/categories.size();
		
		for(ArticleCategory category: categories)
			result.put(category, proportion);
		
		return result;
	}
	
	/**
	 * Process the category proportions for the specified
	 * set of articles. Each value represents how often the
	 * corresponding category appears in the corpus, amongst
	 * all assigned categories. The total over all categories
	 * is therefore 1 (categories are not mutually exclusive).
	 * 
	 * @param articles
	 * 		Articles to be considered.
	 * @return
	 * 		Category proportions for these articles.
	 * 
	 * @throws ReaderException
	 * 		Problem while retrieving an article.
	 * @throws IOException
	 * 		Problem while retrieving an article.
	 * @throws ParseException
	 * 		Problem while retrieving an article.
	 * @throws SAXException
	 * 		Problem while retrieving an article.
	 */
	public static CategoryProportions buildProportionsFromCorpus(List<File> articles) throws ReaderException, IOException, ParseException, SAXException
	{	logger.log("Process categories proportions for the specified corpus");
		logger.increaseOffset();
		
		CategoryProportions result = new CategoryProportions();
		List<ArticleCategory> categories = Arrays.asList(ArticleCategory.values());
		int categoryCounts[] = new int[categories.size()];
		Arrays.fill(categoryCounts,0);
		float total = 0;
		
		// process each article
		logger.log("Process each article");
		logger.increaseOffset();
		ArticleRetriever retriever = new ArticleRetriever();
		for(File folder: articles)
		{	logger.log("Process article "+folder.getName());
			logger.increaseOffset();
			
			// get article
			logger.log("Retrieve article");
			String name = folder.getName();
			Article article = retriever.process(name);
			
			// process categories
			List<ArticleCategory> cats = article.getCategories();
			logger.log("Article categories: "+cats.toString());
			for(ArticleCategory cat: cats)
			{	int index = categories.indexOf(cat);
				categoryCounts[index]++;
				total++;
			}
			
			logger.decreaseOffset();
		}
		logger.decreaseOffset();

		logger.log("Normalize categories counts to get proportions: ");
		logger.increaseOffset();
		for(ArticleCategory category: categories)
		{	int index = categories.indexOf(category);
			int count = categoryCounts[index];
			float proportion = count/total;
			result.put(category, proportion);
			logger.log("Category "+category.toString()+": "+proportion);
		}
		logger.decreaseOffset();

		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes the relative weight of each category represented
	 * in an article, depending on the training set data.
	 *  
	 * @param article
	 * 		Article to be considered.
	 * @return
	 * 		Map associating a weight to each concerned category.
	 */
	public Map<ArticleCategory,Float> processCategoryWeights(Article article)
	{	Map<ArticleCategory,Float> result = new HashMap<ArticleCategory, Float>();
		List<ArticleCategory> cats = article.getCategories();
		
		// process total weight
		float total = 0; 
		for(ArticleCategory cat: cats)
		{	float proportion = get(cat);
			total = total + proportion;
		}
		
		// normalize proportions
		for(ArticleCategory cat: cats)
		{	float proportion = get(cat);
			float weight = proportion / total;
			result.put(cat,weight);
		}
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Initializes a new CategoryProportions object by
	 * reading data in the specified file.
	 * 
	 * @param filePath
	 * 		Complete path of the file to read.
	 * @return
	 * 		A new CategoryProportions object.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle the encoding. 
	 */
	public static CategoryProportions loadCategoryProportions(String filePath) throws FileNotFoundException, UnsupportedEncodingException
	{	CategoryProportions result = new CategoryProportions();
		
		Scanner scanner = FileTools.openTextFileRead(filePath, "UTF-8");
		while(scanner.hasNextLine())
		{	String line = scanner.nextLine();
			String temp[] = line.split("\t");
			ArticleCategory category = ArticleCategory.valueOf(temp[0]);
			Float proportion = Float.parseFloat(temp[1]);
			result.put(category, proportion);
		}
		
		scanner.close();
		return result;
	}
	
	/**
	 * Records these category proportions in the specified file.
	 * 
	 * @param filePath
	 * 		Complete path of the file in which to write.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 * @throws UnsupportedEncodingException 
	 * 		Could not handle the encoding.
	 */
	public void recordCategoryProportion(String filePath) throws UnsupportedEncodingException, FileNotFoundException
	{	PrintWriter writer = FileTools.openTextFileWrite(filePath, "UTF-8");
		TreeSet<ArticleCategory> categories = new TreeSet<ArticleCategory>(keySet());
		for(ArticleCategory category: categories)
		{	float proportion = get(category);
			writer.println(category.toString()+"\t"+proportion);
		}
		writer.close();
	}
}
