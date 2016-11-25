package fr.univavignon.nerwip.evaluation;

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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleCategory;
import fr.univavignon.nerwip.data.article.ArticleList;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.evaluation.measure.AbstractMeasure;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.ConverterException;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.retrieval.ArticleRetriever;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.nerwip.tools.time.TimeFormatting;

/**
 * This class is used to evaluate the performance
 * of recognizers. It requires a collection of manually
 * annotated articles, to be used as references.
 * recognizers are assessd by comparing their estimated
 * mentions to the actual ones. Various measures can
 * be used to perform this comparison.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class Evaluator
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
	public Evaluator(List<EntityType> types, List<AbstractProcessor> recognizers, ArticleList folders, AbstractMeasure template)
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
	private final List<EntityType> types = new ArrayList<EntityType>();
	/** Evaluated recognizers */
	private final List<AbstractProcessor> recognizers = new ArrayList<AbstractProcessor>();
	/** Articles supporting the evaluation */
	private ArticleList folders;
	
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
	public void setRecognizers(List<AbstractProcessor> recognizers)
	{	this.recognizers.addAll(recognizers);
	}

	/**
	 * Returns the list of recognizers to be evaluated.
	 * 
	 * @return
	 * 		List of evaluated recognizers.
	 */
	public List<AbstractProcessor> getRecognizers()
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
	private AbstractMeasure template = null;
	/** List of measures containing current evaluation data */
	private List<AbstractMeasure> measures = null;

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
	public AbstractMeasure getMeasure(AbstractProcessor recognizer)
	{	int index = recognizers.indexOf(recognizer);
		AbstractMeasure result = measures.get(index);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the last article category processed */
	private List<ArticleCategory> lastCategories = null;
	
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
	 * @throws ConverterException
	 * 		Problem while converting the reference file.
	 * @throws ProcessorException
	 * 		Problem while applying a recognizer.
	 */
	private List<AbstractMeasure> processArticle(File folder) throws ReaderException, IOException, ParseException, SAXException, ConverterException, ProcessorException
	{	logger.increaseOffset();
		List<AbstractMeasure> result = new ArrayList<AbstractMeasure>();
		
		// get article
		logger.log("Retrieve the article");
		String name = folder.getName();
		ArticleRetriever retriever = new ArticleRetriever();
		Article article = retriever.process(name);
		lastCategories = article.getCategories();
		
		// get reference mentions
		Mentions refMentions = article.getReferenceMentions();
		
		// process each recognizer separately
		logger.log("Process each recognizer separately");
		logger.increaseOffset();
		for(AbstractProcessor recognizer: recognizers)
		{	logger.log("Dealing with recognizer "+recognizer.getName());
			
			// check if evaluation results already exist
			String folderPath = folder.getPath() + File.separator + recognizer.getFolder();
			File resultsFolder = new File(folderPath);
			String resultsPath = folderPath + File.separator + template.getFileName();
			File resultsFile = new File(resultsPath);
			boolean processNeeded = !resultsFile.exists(); 
			
			// process results
			if(!cache || processNeeded)
			{	logger.log("Processing results");
				Mentions estMentions = recognizer.recognize(article);
				AbstractMeasure res = template.build(recognizer, types, refMentions, estMentions, lastCategories);
				
				logger.log("Writing results to cache");
				res.writeNumbers(resultsFolder,name);
				result.add(res);
			}
			
			// load results
			else
			{	logger.log("Results already cached >> just load them");
				AbstractMeasure res = template.readNumbers(resultsFolder, recognizer);
				result.add(res);
			}
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		return result;
	}
		
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
	 * @throws ConverterException
	 * 		Problem while converting a reference file.
	 * @throws ProcessorException
	 * 		Problem while applying a recognizer.
	 */
	public void process() throws ReaderException, IOException, ParseException, SAXException, ConverterException, ProcessorException
	{	logger.increaseOffset();
		
		// init
		logger.log("Init measure objects");
		measures = new ArrayList<AbstractMeasure>();
		for(AbstractProcessor recognizer: recognizers)
		{	AbstractMeasure measure = template.build(recognizer,types);
			measures.add(measure);
		}
		
		// process each article
		logger.log("Process each article individually");
		logger.increaseOffset();
		for(File folder: folders)
		{	// get the results
			logger.log("Process article "+folder.getName());
			List<AbstractMeasure> results = processArticle(folder);
			
			// update counts
			logger.log("Update counts");
			for(int i=0;i<recognizers.size();i++)
			{	AbstractMeasure result = results.get(i);
				AbstractMeasure measure = measures.get(i);
				measure.updateCounts(result);
			}
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
			AbstractMeasure measure = measures.get(i);
			String dataName = folders.getName();
			measure.writeNumbers(folder,dataName);
			
			// rename file
			File oldFile = new File(FileNames.FO_OUTPUT + File.separator + measure.getFileName());
			AbstractProcessor recognizer = recognizers.get(i);
			String newName = FileNames.FO_OUTPUT + File.separator 
				+ TimeFormatting.formatCurrentFileTime()
				+ "." + recognizer.getFolder()
				+ "." + measure.getFileName(); 
			File newFile = new File(newName);
			oldFile.renameTo(newFile);
		}
	}

	/////////////////////////////////////////////////////////////////
	// STRING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = template.getName();
		return result;
	}
}
