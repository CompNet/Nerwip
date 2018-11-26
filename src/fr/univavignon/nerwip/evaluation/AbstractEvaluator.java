package fr.univavignon.nerwip.evaluation;

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
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import fr.univavignon.common.data.article.ArticleCategory;
import fr.univavignon.common.data.article.ArticleList;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.nerwip.processing.InterfaceProcessor;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.nerwip.tools.time.TimeFormatting;
import fr.univavignon.retriever.reader.ReaderException;

/**
 * This class is used to evaluate the performance
 * of recognizers. It requires a collection of manually
 * annotated articles, to be used as references.
 * recognizers are assessed by comparing their estimated
 * mentions to the actual ones. Various measures can
 * be used to perform this comparison.
 * 
 * @param <T>
 * 		TODO 
 * @param <U>
 * 		TODO 
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public abstract class AbstractEvaluator<T extends InterfaceProcessor, U extends AbstractMeasure>
{	
	/**
	 * Builds a new evaluator, 
	 * using the specified options.
	 * 
	 * @param types
	 * 		Restricts the evaluation to the specified entity types only.
	 * @param recognizers
	 * 		Recognizers to be evaluated.
	 * @param folders
	 * 		Articles to use during the evaluation.
	 * @param template 
	 * 		Object used to process performances.
	 */
	public AbstractEvaluator(List<EntityType> types, List<T> recognizers, ArticleList folders, U template)
	{	this.types.addAll(types);
		this.recognizers.addAll(recognizers);
		this.folders = folders;
		this.template = template;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// OPTIONS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Types considered during the evaluation */
	protected final List<EntityType> types = new ArrayList<EntityType>();
	/** Evaluated recognizers */
	protected final List<T> recognizers = new ArrayList<T>();
	/** Articles supporting the evaluation */
	protected ArticleList folders;
	
	/**
	 * Changes the types used during
	 * evaluation.
	 * 
	 * @param types
	 * 		Types considered when performing the evaluation.
	 */
	public void setEntityTypes(List<EntityType> types)
	{	this.types.addAll(types);
	}

	/**
	 * Changes the recognizers to be evaluated.
	 * 
	 * @param recognizers
	 * 		Recognizers considered when performing the evaluation.
	 */
	public void setRecognizers(List<T> recognizers)
	{	this.recognizers.addAll(recognizers);
	}

	/**
	 * Returns the list of recognizers to be evaluated.
	 * 
	 * @return
	 * 		List of evaluated recognizers.
	 */
	public List<T> getRecognizers()
	{	return recognizers;
	}
	
//	/**
//	 * Changes the articles used during
//	 * evaluation.
//	 * 
//	 * @param folder
//	 * 		Folder containing the articles to use.
//	 */
//	public void setFolder(File folder)
//	{	folders.addAll(Arrays.asList(folder.listFiles()));
//	}

	/////////////////////////////////////////////////////////////////
	// CACHING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not the evaluator should use caching */
	protected boolean cache = true;
	
	/**
	 * Changes the caching flag.
	 * If {@code true}, the evaluator will
	 * check if result files already exist.
	 * If it is the case, then they will be
	 * simply loaded. Otherwise, the regular
	 * evaluation process will take place.
	 * <br>
	 * This cache parameter does not affect
	 * whether or not the recognizers caches
	 * are enabled or disabled.
	 * 
	 * @param enabled
	 * 		New cache option.
	 */
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
	}

	/////////////////////////////////////////////////////////////////
	// MEASURES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Object used to create new measure instances when needed */
	protected U template = null;
	/** List of measures containing current evaluation data */
	protected List<U> measures = null;

	/**
	 * Initialises the Measure objects.
	 */
	protected abstract void initMeasures();
	
	/**
	 * Returns the measure object processed for the
	 * specified recognizer. This method is meant to 
	 * access results <i>after</i> the evaluator has been applied.
	 * 
	 * @param recognizer
	 * 		Recognizer of interest.
	 * @return
	 * 		Corresponding measure object.
	 */
	public U getMeasure(T recognizer)
	{	int index = recognizers.indexOf(recognizer);
		U result = measures.get(index);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the last article category processed */
	protected List<ArticleCategory> lastCategories = null;
	
	/**
	 * Evaluate the recognizers on
	 * the specified article. The method
	 * will first retrieve the article,
	 * the reference mentions, apply the recognizers, 
	 * get their estimated mentions and compare them 
	 * to the reference.
	 * 
	 * @param folder
	 * 		Folder containing the article of interest.
	 * @return
	 * 		List of measure objects, each one representing the evaluation of a single recognizer.
	 *  
	 * @throws ReaderException
	 * 		Problem while retrieving the article.
	 * @throws IOException
	 * 		Problem while accessing a file.
	 * @throws ParseException
	 * 		Problem while accessing a file.
	 * @throws SAXException
	 * 		Problem while accessing a file.
	 * @throws ProcessorException
	 * 		Problem while applying a recognizer.
	 */
	protected abstract List<U> processArticle(File folder) throws ReaderException, IOException, ParseException, SAXException, ProcessorException;
	
	/**
	 * Update the counts for each concerned measure.
	 * 
	 * @param results
	 * 		List of current results, to be used during the update.
	 */
	protected abstract void updateCounts(List<U> results);
	
	/**
	 * Starts the evaluation process.
	 * 
	 * @throws ReaderException
	 * 		Problem while retrieving an article.
	 * @throws IOException
	 * 		Problem while accessing a file.
	 * @throws ParseException
	 * 		Problem while accessing a file.
	 * @throws SAXException
	 * 		Problem while accessing a file.
	 * @throws ProcessorException
	 * 		Problem while applying a recognizer.
	 */
	public void process() throws ReaderException, IOException, ParseException, SAXException, ProcessorException
	{	logger.increaseOffset();
		
		// init
		logger.log("Init measure objects");
		initMeasures();
		
		// process each article
		logger.log("Process each article individually");
		logger.increaseOffset();
		int f = 1;
		for(File folder: folders)
		{	// get the results
			logger.log("Process article "+folder.getName()+ "("+f+"/"+folders.size()+")");
			f++;
			List<U> results = processArticle(folder);
			
			// update counts
			logger.log("Update counts");
			updateCounts(results);
		}
		logger.decreaseOffset();
		
		// record the results
		writeResults();
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// RECORD			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Writes the result of the evaluation
	 * in a text file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the file.
	 */
	private void writeResults() throws FileNotFoundException, UnsupportedEncodingException
	{	File folder = new File(FileNames.FO_OUTPUT);
		
		for(int i=0;i<measures.size();i++)
		{	// record values
			U measure = measures.get(i);
			String dataName = folders.getName();
			measure.writeNumbers(folder,dataName);
			
			// rename file
			File oldFile = new File(FileNames.FO_OUTPUT + File.separator + measure.getFileName());
			T recognizer = recognizers.get(i);
			String newName = FileNames.FO_OUTPUT + File.separator 
				+ TimeFormatting.formatCurrentFileTime()
				+ "." + getFolder(recognizer)
				+ "." + measure.getFileName(); 
			File newFile = new File(newName);
			oldFile.renameTo(newFile);
		}
	}
	
	/**
	 * Returns the data folder associated to the specified processor.
	 * 
	 * @param processor
	 * 		Processed whose data folder is desired. 
	 * @return
	 * 		A {@code String} representing the folder path.
	 */
	protected abstract String getFolder(T processor);
	
	/////////////////////////////////////////////////////////////////
	// STRING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = template.getName();
		return result;
	}
}
