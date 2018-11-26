package fr.univavignon.nerwip.processing.internal.modelbased.opennlp;

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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.internal.modelbased.AbstractTrainer;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;

/**
 * This class trains Apache OpenNLP
 * on our corpus. This results in the creation of new
 * model files, which can then be used to perform NER
 * instead of the default models.  
 * <br/>
 * A part of this code was inspired by/retrieved from
 * the original OpenNlp classes. 
 * 
 * @author Vincent Labatut
 */
public class OpenNlpTrainer extends AbstractTrainer<Map<EntityType,List<String>>>
{
	/**
	 * Creates a new trainer for
	 * the specified model. Any
	 * existing model files will be
	 * overwritten.
	 * 
	 * @param modelName
	 * 		Name of the model to be trained.
	 */
	public OpenNlpTrainer(OpenNlpModelName modelName)
	{	this.modelName = modelName;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATA				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the file containing the data */
	private final static String DATA_FILENAME = FileNames.FO_OUTPUT + File.separator + "opennlp.data";
	/** Object used to split text into sentences */
	private SentenceDetectorME sentenceDetector;

	@Override
	protected String getDataPath()
	{	return DATA_FILENAME;
	}
	
	@Override
	protected Map<EntityType,List<String>> mergeData(Map<EntityType,List<String>> corpus, Map<EntityType,List<String>> article)
	{	Map<EntityType,List<String>> result = null;
		
		// first time
		if(corpus==null)
			result = article;
		
		// general case
		else
		{	result = new HashMap<EntityType, List<String>>();
			for(EntityType type: corpus.keySet())
			{	List<String> resultList = new ArrayList<String>();
				result.put(type,resultList);
				
				List<String> corpusList = corpus.get(type);
				resultList.addAll(corpusList);
				
				resultList.add("");	// separates two articles, needed by OpenNlp
				
				List<String> articleList = article.get(type);
				resultList.addAll(articleList);
			}
		}
		
		return result;
	}

	@Override
	protected Map<EntityType,List<String>> convertData(Article article, Mentions mentions)
	{	logger.increaseOffset();
		logger.log("Processing article "+article.getName());
		Map<EntityType,List<String>> result = new HashMap<EntityType, List<String>>();
		
		// split the text into sentences
		String text = article.getRawText();
		Span sentenceSpans[] = sentenceDetector.sentPosDetect(text);
		
		// process separately each entity type
		for(EntityType type: getHandledEntityTypes())
		{	// init the list representing the article
			List<String> list = new ArrayList<String>();
			result.put(type,list);
			
			// init the tags
			String startTag = " <START:" + type.toString().toLowerCase() + "> ";	// the spaces surounding the tag are needed
			String endTag = " <END> ";												// and same here
			
			// reset text stuff
			text = article.getRawText();
			Iterator<AbstractMention<?>> it = mentions.getMentions().iterator();
			AbstractMention<?> currentMention = null;
			if(it.hasNext())
				currentMention = it.next();
			
			// copy each line while inserting the appropriate tags
	    	for(Span sentenceSpan: sentenceSpans)
			{	// get the original sentence
	    		int startSentPos = sentenceSpan.getStart();
				int endSentPos = sentenceSpan.getEnd();
//				String sentence = text.substring(startSentPos,endSentPos);
				
				// rebuild the sentence with additional tags
				int pos = startSentPos;
				String taggedSentence = "";
				boolean goOn = true;
				while(currentMention!=null && goOn)
				{	int startEnt = currentMention.getStartPos();
					int endEnt = currentMention.getEndPos();
					if(startEnt<endSentPos)
					{	if(endEnt<=endSentPos)
						{	EntityType t = currentMention.getType();
							if(t==type)
							{	String startSentence = text.substring(pos,startEnt);
								taggedSentence = taggedSentence + startSentence + startTag;
								String mentionStr = text.substring(startEnt,endEnt);
								taggedSentence = taggedSentence + mentionStr + endTag;
								pos = endEnt;
							}
							if(it.hasNext())
								currentMention = it.next();
							else
								currentMention = null;
						}
						else
						{	// the mention is in-between chunks (ie the sentence detector is wrong)
							// we just ignore it, since the same thing will happen during normal annotation
							if(it.hasNext())
								currentMention = it.next();
							else
								currentMention = null;
							goOn = false;
						}
					}
					else
						goOn = false;
				}
				
				// possibly complete with the rest of the sentence
				if(pos<endSentPos)
				{	String restStr = text.substring(pos, endSentPos);
					taggedSentence = taggedSentence + restStr;
				}
				else if(pos>endSentPos)
					System.err.println("Problem with pos ("+pos+") vs. endSentPos ("+endSentPos+") in OpenNlpTrainer.convertData()");
				
				// add to result
				list.add(taggedSentence);
			}
	    	
//			System.out.println(list.toString());
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	@Override
	protected boolean checkData(File dataFile)
	{	boolean result = true;
		
		List<EntityType> types = getHandledEntityTypes();
		Iterator<EntityType> it = types.iterator();
		while(result && it.hasNext())
		{	EntityType type = it.next();
			String filePath = dataFile.getAbsolutePath() + "." + type.toString().toLowerCase() + FileNames.EX_TEXT;
			File file = new File(filePath);
			result = file.exists();
		}
	
		return result;
	}
	
	@Override
	protected Map<EntityType,List<String>> loadData(File dataFile) throws IOException
	{	logger.increaseOffset();
		logger.log("Loading cached data for each entity type");
		Map<EntityType,List<String>> result = new HashMap<EntityType, List<String>>();
		
		// process each type separately
		logger.increaseOffset();
		for(EntityType type: getHandledEntityTypes())
		{	logger.log("Processing entity type "+type.toString());
			String filePath = dataFile.getAbsolutePath() + "." + type.toString().toLowerCase() + FileNames.EX_TEXT;
			Scanner scanner = FileTools.openTextFileRead(filePath, "UTF-8");
			
			List<String> list = new ArrayList<String>();
			result.put(type,list);
			
			while(scanner.hasNextLine())
			{	String line = scanner.nextLine();
				list.add(line);
			}
			
			scanner.close();
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		return result;
	}

	@Override
	protected void recordData(File dataFile, Map<EntityType,List<String>> data) throws IOException
	{	logger.increaseOffset();
		logger.log("Caching data for each entity type");
		
		// process each type separately
		logger.increaseOffset();
		for(EntityType type: data.keySet())
		{	logger.log("Processing entity type "+type.toString());
			String filePath = dataFile.getAbsolutePath() + "." + type.toString().toLowerCase() + FileNames.EX_TEXT;
			PrintWriter printWriter = FileTools.openTextFileWrite(filePath, "UTF-8");
			
			List<String> text = data.get(type);
			for(String line: text)
				printWriter.println(line);
			
			printWriter.close();
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
	}

	/////////////////////////////////////////////////////////////////
	// MODEL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** An instance of the model object associated to this trainer */
	protected OpenNlpModelName modelName = null;

	@Override
	protected List<EntityType> getHandledEntityTypes()
	{	List<EntityType> result = modelName.getHandledTypes();
		return result;
	}

//	/**
//	 * Returns the path of the file
//	 * containing the model produced
//	 * during training.
//	 * 
//	 * @return
//	 * 		String representing the model file path.
//	 * 
//	 * @author Vincent Labatut
//	 */
//	protected String getModelPath()
//	{	String result = modelName.getModelPath();
//		return result;
//	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Language of the processed texts */
	private static final String language = "en";
	
	@Override
	protected void configure() throws Exception
	{	logger.increaseOffset();
	
		logger.log("Load the object used to split sentences");
		sentenceDetector = modelName.loadSentenceDetector();
		logger.log("Loading completed");
				
		logger.decreaseOffset();
	}
	
	@Override
	protected void train(Map<EntityType,List<String>> data) throws Exception
	{	logger.increaseOffset();
		
		// get file names
		Map<EntityType,File> modelFiles = modelName.getModelFiles();
		
		// perform training
		logger.log("Processing each entity type separately");
		logger.increaseOffset();
		TrainingParameters params = TrainingParameters.defaultParams();	//TODO we could test different training parameters here, if needed
		for(EntityType type: data.keySet())
		{	String typeName = type.toString();
			logger.log("Processing entity type "+typeName);
			List<String> lines = data.get(type);
			
			// builds specific object stream using the list of lines
			ObjectStream<String> os = new OpenNlpObjectStream(lines);
			ObjectStream<NameSample> sampleStream = new NameSampleDataStream(os);

			// invoke OpenNlp training API
			logger.log("Perform training");
			AdaptiveFeatureGenerator generator = null;
			TokenNameFinderModel model = NameFinderME.train(language, typeName.toLowerCase(), sampleStream, params, generator, Collections.<String, Object>emptyMap());
			logger.log("Training complete");

			// record model
			logger.log("Record resulting model");
			File modelFile = modelFiles.get(type);
			FileOutputStream fos = new FileOutputStream(modelFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			model.serialize(bos);
			bos.close(); 
			logger.log("Recording complete");
			
		}
		logger.decreaseOffset();
		
		logger.log("Training and recording complete");
		logger.decreaseOffset();
	}
	
	/**
	 * Custom object stream, to be used to pass the training data
	 * to the OpenNlp training API.
	 * 
	 * @author Vincent Labatut
	 */
	private class OpenNlpObjectStream implements ObjectStream<String>
	{	/** Source object */
		private List<String> source;
		/** Iterator on the source object */
		private Iterator<String> iterator;
		
		/**
		 * Creates an object stream.
		 * 
		 * @param source
		 * 		Source object.
		 */
		public OpenNlpObjectStream(List<String> source)
		{	this.source = source;
			iterator = source.iterator();
		}
	
		@Override
		public String read() throws IOException
		{	String result = null;
			if(iterator.hasNext())
				result = iterator.next();
			return result;
		}

		@Override
		public void reset() throws IOException, UnsupportedOperationException
		{	iterator = source.iterator();
		}

		@Override
		public void close() throws IOException
		{	// nothing to do here
		}	
	}
}
