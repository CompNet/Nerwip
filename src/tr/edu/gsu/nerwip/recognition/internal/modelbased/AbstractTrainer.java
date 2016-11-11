package tr.edu.gsu.nerwip.recognition.internal.modelbased;

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
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleList;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.data.entity.mention.AbstractMention;
import tr.edu.gsu.nerwip.data.entity.mention.Mentions;
import tr.edu.gsu.nerwip.retrieval.ArticleRetriever;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This abstract class is a frame for implementing
 * a training process, for NER tools supporting it.
 * First, the training set is parsed to retrieve
 * mentions and convert them to a format suitable
 * for training. These data are cached in a single file.
 * Then, the training is performed, resulting in
 * the production of one or several model files
 * (depending on the NER tool), located in a pre-specified
 * folder. New models can then be used normally
 * to identify named entity mentions.
 * 
 * @param <T>
 * 		Type of the data needed by the NER for training. 
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractTrainer<T>
{	
	/**
	 * Creates a new trainer.
	 */
	public AbstractTrainer()
	{	// nothing special to do here
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CACHING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not cache should be used */
	protected boolean cache = true;
	
	/**
	 * Changes the cache flag. If {@code true}, the {@link #process(ArticleList) process}
	 * method will first check if the input data already
	 * exist as a file. In this case, they will be loaded
	 * from this file. Otherwise, the process will be
	 * conducted normally, then recorded.
	 * 
	 * @param enabled
	 * 		If {@code true}, the (possibly) cached files are used.
	 */
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
	}
	
	/////////////////////////////////////////////////////////////////
	// MODEL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of types
	 * supported during this training.
	 * 
	 * @return
	 * 		A list of supported {@link EntityType}.
	 */
	protected abstract List<EntityType> getHandledEntityTypes();
	
	/////////////////////////////////////////////////////////////////
	// DATA				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the path of the file
	 * containing the pre-processed 
	 * training data.
	 * 
	 * @return
	 * 		The path of the data file.
	 */
	protected abstract String getDataPath();
	
	/**
	 * Analyses existing annotation and reference files,
	 * in order to extract the data necessary for the training.
	 * The result is cached as a file in the output folder.
	 * 
	 * @param folders 
	 * 		List of articles to be processed.
	 * @return
	 * 		A data object representing the training data.
	 * 
	 * @throws IOException 
	 * 		Problem while loading references or recording converted data.
	 * @throws ParseException 
	 * 		Problem while loading references.
	 * @throws SAXException 
	 * 		Problem while loading references.
	 * @throws ReaderException 
	 * 		Problem while loading references.
	 */
	private T prepareData(ArticleList folders) throws IOException, ReaderException, ParseException, SAXException
	{	logger.log("Preparing the data for training");
		logger.increaseOffset();
		T result = null;
	
		// if the data files exist, load. 
		String dataPath = getDataPath();
		File dataFile = new File(dataPath);
		if(cache && checkData(dataFile))
			result = loadData(dataFile);
		
		// otherwise, process data
		else
		{	// process each article
			for(File folder: folders)
			{	// get article
				String name = folder.getName();
				ArticleRetriever retriever = new ArticleRetriever();
				Article article = retriever.process(name);
					
				// get reference mentions
				Mentions mentions = article.getReferenceMentions();
				// keep only those allowed for this training
				filterReferenceMentions(mentions);
				
				// convert
				T conv = convertData(article,mentions);
				result = mergeData(result,conv);
			}
			
			// record the resulting data
			recordData(dataFile, result);
		}
	
		logger.decreaseOffset();
		logger.log("Data preparation complete");
		return result;
	}
	
	/**
	 * Builds a new data object by merging
	 * both specified data objects. The content
	 * of the first one is copied first, then
	 * that of the second one. The resulting
	 * data object is suitable for the training
	 * of the considered NER tool.
	 * 
	 * @param data1
	 * 		One data object.
	 * @param data2
	 * 		The other data object.
	 * @return
	 * 		A merge of both data objects.
	 */
	protected abstract T mergeData(T data1, T data2);
	
	/**
	 * Convert the specified mentions to a format 
	 * which is suitable for training.
	 * 
	 * @param article
	 * 		Article to process.
	 * @param mentions
	 * 		Reference mentions.
	 * @return
	 * 		Mentions represented using a format suitable for training.
	 */
	protected abstract T convertData(Article article, Mentions mentions);
	
//	/**
//	 * Completes the specified data object using
//	 * the specified reference mention type and estimated
//	 * mentions. The reference type can be {@code null}, 
//	 * if no mention actually exists. 
//	 * 
//	 * @param data
//	 * 		SVM data object to be completed.
//	 * @param index
//	 * 		Position in the data object.
//	 * @param article
//	 * 		Article to process.
//	 * @param recognizers
//	 * 		List of concerned NER tools.
//	 * @param refType
//	 * 		Type of the reference mention, 
//	 * 		or {@code null} if no reference mention. 
//	 * @param estMentions
//	 * 		Corresponding estimated mentions.
//	 */
//	protected abstract void convertData(svm_problem data, int index, Article article, List<AbstractRecognizer> recognizers, EntityType refType, Map<AbstractRecognizer,AbstractMention<?>> estMentions);
	
	/**
	 * Verifies if the specified data file(s) already exist.
	 * 
	 * @param dataFile
	 * 		File to be checked.
	 * @return
	 * 		{@code true} iff the cached file exists (and can be loaded).
	 */
	protected abstract boolean checkData(File dataFile);
	
	/**
	 * Load the data contained in the specified file.
	 * These data are stored according to the specific
	 * NER format.
	 * 
	 * @param dataFile
	 * 		File containing the data to be read.
	 * @return
	 * 		Data in a format suitable for training.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the data file.
	 */
	protected abstract T loadData(File dataFile) throws IOException;

	/**
	 * Records the specified data as a file,
	 * for caching purposes.
	 * 
	 * @param dataFile
	 * 		File to contain the SVM data.
	 * @param data
	 * 		Data to be recorded.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the data file.
	 */
	protected abstract void recordData(File dataFile, T data) throws IOException;

	/**
	 * Receives a set of entities, and retains
	 * only those allowed for this training
	 * (based on their types).
	 * 
	 * @param mentions
	 * 		Raw set of entities.
	 */
	protected void filterReferenceMentions(Mentions mentions)
	{	List<EntityType> typeList = getHandledEntityTypes();
		List<AbstractMention<?>> entList = mentions.getMentions();
		Iterator<AbstractMention<?>> it = entList.iterator();
		while(it.hasNext())
		{	AbstractMention<?> mention = it.next();
			EntityType type = mention.getType();
			if(!typeList.contains(type))
				it.remove();
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Trains the NER on the specified data.
	 * 
	 * @param folders
	 * 		List of concered articles.
	 * 
	 * @throws Exception 
	 * 		Problem during training.
	 */
	public void process(ArticleList folders) throws Exception
	{	logger.increaseOffset();
		
		// possibly load some configuration data
		configure();
		
		// prepare the data
		T data = prepareData(folders);
		
		// train the NER
		train(data);
		
		logger.decreaseOffset();
	}

	/**
	 * Loads a configuration file,
	 * or do whatever is needed before
	 * starting preparing and processing
	 * the data.
	 * 
	 * @throws Exception 
	 * 		Problem during configuration.
	 */
	protected abstract void configure() throws Exception;
	
	/**
	 * Method applying the actual training
	 * on the specified data, then recording
	 * the resulting model in the appropriate way.
	 * 
	 * @param data
	 * 		Data used for training.
	 * 
	 * @throws Exception 
	 * 		Problem during training.
	 */
	protected abstract void train(T data) throws Exception;
}
