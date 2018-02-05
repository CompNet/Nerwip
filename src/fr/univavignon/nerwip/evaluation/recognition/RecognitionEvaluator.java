package fr.univavignon.nerwip.evaluation.recognition;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleList;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.evaluation.AbstractEvaluator;
import fr.univavignon.nerwip.evaluation.recognition.measures.AbstractRecognitionMeasure;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.retrieval.ArticleRetriever;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;

/**
 * This class is used to evaluate the performance
 * of recognizers. It requires a collection of manually
 * annotated articles, to be used as references.
 * recognizers are assessed by comparing their estimated
 * mentions to the actual ones. Various measures can
 * be used to perform this comparison.
 * 
 * @author Vincent Labatut
 */
public class RecognitionEvaluator extends AbstractEvaluator<InterfaceRecognizer,AbstractRecognitionMeasure>
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
	public RecognitionEvaluator(List<EntityType> types, List<InterfaceRecognizer> recognizers, ArticleList folders, AbstractRecognitionMeasure template)
	{	super(types,recognizers,folders,template);
	}
	
	/////////////////////////////////////////////////////////////////
	// MEASURES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void initMeasures()
	{	measures = new ArrayList<AbstractRecognitionMeasure>();
		for(InterfaceRecognizer recognizer: recognizers)
		{	AbstractRecognitionMeasure measure = template.build(recognizer,types);
			measures.add(measure);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected List<AbstractRecognitionMeasure> processArticle(File folder) throws ReaderException, IOException, ParseException, SAXException, ProcessorException
	{	logger.increaseOffset();
		List<AbstractRecognitionMeasure> result = new ArrayList<AbstractRecognitionMeasure>();
		
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
		int r = 1;
		for(InterfaceRecognizer recognizer: recognizers)
		{	logger.log("Dealing with recognizer "+recognizer.getName()+" ("+r+"/"+recognizers.size()+")");
			r++;
			
			// check if evaluation results already exist
			String folderPath = folder.getPath() + File.separator + recognizer.getRecognizerFolder();
			File resultsFolder = new File(folderPath);
			String resultsPath = folderPath + File.separator + template.getFileName();
			File resultsFile = new File(resultsPath);
			boolean processNeeded = !resultsFile.exists(); 
			
			// process results
			if(!cache || processNeeded)
			{	logger.log("Processing results");
				Mentions estMentions = recognizer.recognize(article);
				AbstractRecognitionMeasure res = template.build(recognizer, types, refMentions, estMentions, lastCategories);
				
				logger.log("Writing results to cache");
				res.writeNumbers(resultsFolder,name);
				result.add(res);
			}
			
			// load results
			else
			{	logger.log("Results already cached >> just load them");
				AbstractRecognitionMeasure res = template.readNumbers(resultsFolder, recognizer);
				result.add(res);
			}
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		return result;
	}
		
	@Override
	protected void updateCounts(List<AbstractRecognitionMeasure> results)
	{	for(int i=0;i<recognizers.size();i++)
		{	AbstractRecognitionMeasure result = results.get(i);
			AbstractRecognitionMeasure measure = measures.get(i);
			measure.updateCounts(result);
		}
	}

	/////////////////////////////////////////////////////////////////
	// RECORD			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected String getFolder(InterfaceRecognizer recognizer)
	{	String result = recognizer.getRecognizerFolder();
		return result;
	}
}
