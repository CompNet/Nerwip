package fr.univavignon.nerwip.processing.internal.modelbased.stanford;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.internal.modelbased.AbstractTrainer;
import fr.univavignon.nerwip.tools.file.FileNames;

/**
 * This class trains the Stanford tool
 * on our corpus. This results in the creation of new
 * model files, which can then be used to perform NER
 * instead of the default models.  
 * 
 * @author Vincent Labatut
 */
public class StanfordTrainer extends AbstractTrainer<List<List<CoreLabel>>>
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
	public StanfordTrainer(StanfordModelName modelName)
	{	this.modelName = modelName;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATA				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the file containing the data */
	private final static String DATA_FILENAME = FileNames.FO_OUTPUT + File.separator + "stanford.data.bin";
	/** Data used when creating CoreLabel objects */
	private final static String KEYS[] = {"word","answer"};
	/** Map of EntityType to Stanford type conversion */
	private final static Map<EntityType, String> CONVERSION_MAP = new HashMap<EntityType, String>();
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put(EntityType.DATE, "DATE");
		CONVERSION_MAP.put(EntityType.LOCATION, "LOCATION");
		CONVERSION_MAP.put(EntityType.ORGANIZATION, "ORGANIZATION");
		CONVERSION_MAP.put(EntityType.PERSON, "PERSON");
		CONVERSION_MAP.put(null, "O");
	}
	
	@Override
	protected String getDataPath()
	{	return DATA_FILENAME;
	}
	
	@Override
	protected List<List<CoreLabel>> mergeData(List<List<CoreLabel>> corpus, List<List<CoreLabel>> article)
	{	List<List<CoreLabel>> result = null;
		
		// first time
		if(corpus==null)
			result = article;
		
		// general case
		else
		{	// builds a new list by merging both existing lists
			result = new ArrayList<List<CoreLabel>>();
			result.addAll(corpus);
			result.addAll(article);
		}
		
		return result;
	}

	@Override
	protected List<List<CoreLabel>> convertData(Article article, Mentions mentions)
	{	logger.increaseOffset();
		logger.log("Processing article "+article.getName());
		
		// prepare result object
		List<List<CoreLabel>> result = new ArrayList<List<CoreLabel>>();
		List<CoreLabel> list = new ArrayList<CoreLabel>();
		result.add(list);
    	
    	String rawText = article.getRawText();
   		int pos = 0;
   		
   		// add each mention under the form of a Stanford object
    	mentions.sortByPosition();
    	List<AbstractMention<?>> entList = mentions.getMentions();
    	for(AbstractMention<?> mention: entList)
    	{	// process the text located before the mention (and after the previous mention)
    		int start = mention.getStartPos();
    		if(start>pos)
    		{	String before = rawText.substring(pos,start);
    			List<CoreLabel> temp = extractCoreLabels(before, null);
    			list.addAll(temp);
    		}
    		
    		// process the current mention
    		{	String mentionStr = mention.getStringValue();
    			EntityType type = mention.getType();
    			List<CoreLabel> temp = extractCoreLabels(mentionStr, type);
    			list.addAll(temp);
    		}
    		pos = mention.getEndPos();
    	}
    	
    	// possibly process the rest of the text
    	int length = rawText.length();
    	if(pos<length)
    	{	String remaining = rawText.substring(pos,length);
			List<CoreLabel> temp = extractCoreLabels(remaining, null);
			list.addAll(temp);
    	}
    	
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Split the text and create a list of CoreLabel objects corresponding
	 * to the words it contains. All of them have the specified type.
	 * 
	 * @param text
	 * 		Text to be split.
	 * @param type
	 * 		Type of the CoreLabel object to create.
	 * @return
	 * 		A list of newly created CoreLabel objects.
	 */
	private List<CoreLabel> extractCoreLabels(String text, EntityType type)
	{	List<CoreLabel> result = new ArrayList<CoreLabel>();
		
//		String splitText[] = text.split("\\W+");				// \W represents all non-word characters, i.e not letters, digits or underscore
//		String splitText[] = text.split("[^\\p{L}0-9]+");		// this one handles diacritics but separators disappear from the array
		String splitText[] = text.split("((?!^)\\b)| ");			// this ones uses word boundaries (\b) and keeps separators

		for(String word: splitText)
		{	word = word.trim();
			if(!word.isEmpty())
			{	String typeStr = CONVERSION_MAP.get(type);
				String values[] = {word, typeStr};
				CoreLabel coreLabel = new CoreLabel(KEYS,values);
				result.add(coreLabel);
			}
		}
		
		return result;
	}
	
	@Override
	protected boolean checkData(File dataFile)
	{	boolean result = dataFile.exists();
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<List<CoreLabel>> loadData(File dataFile) throws IOException
	{	logger.increaseOffset();
		List<List<CoreLabel>> result	= null;
		
		try
		{	// we just read the previously (manually) serialized list of list of CoreLabel objects
			FileInputStream fis = new FileInputStream(dataFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			result = (List<List<CoreLabel>>) ois.readObject();
			
			// and finally, close the stream
			ois.close();
		}
		catch (Exception e)
		{	e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		logger.decreaseOffset();
		return result;
	}

	@Override
	protected void recordData(File dataFile, List<List<CoreLabel>> data) throws IOException
	{	logger.increaseOffset();
		
		try
		{	// we just manually serialize the previously built list of lists of CoreLabel
			FileOutputStream fos = new FileOutputStream(dataFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(data);
			
			// and finally, close the stream
			oos.close();
		}
		catch (Exception e)
		{	e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		logger.decreaseOffset();
	}

	/////////////////////////////////////////////////////////////////
	// MODEL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** An instance of the model object associated to this trainer */
	protected StanfordModelName modelName = null;

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
	@Override
	protected void configure() throws Exception
	{	logger.increaseOffset();
	
		// nothing to do here
				
		logger.decreaseOffset();
	}
	
	/**
	 * Sets up a properties object to be used
	 * during the classifier training.
	 * The values are taken from the configuration 
	 * files associated to the original models.
	 * Commented instructions correspond to
	 * comment lines in the original configuration 
	 * file.
	 * 
	 * @return
	 * 		Created properties object.
	 */
	private Properties setUpProperties()
	{	Properties result = new Properties();
		
//		result.setProperty("trainFile", getDataPath()); 				// we don't use this
//		result.setProperty("testFile", getDataPath()); 					// we don't use this
//		result.setProperty("serializeTo", modelName.getModelFile()); 	// we don't use this
		
		result.setProperty("distSimLexicon", FileNames.FO_STANFORD_CLUSTERS+File.separator+"egw4-reut.512.clusters.txt");
		result.setProperty("useDistSim", "true");
		
		result.setProperty("map", "word=0,answer=1");
		
		result.setProperty("saveFeatureIndexToDisk", "true");
		
		result.setProperty("useClassFeature", "true");
		result.setProperty("useWord", "true");
//		result.setProperty("useWordPairs", "true");
		result.setProperty("useNGrams", "true");
		result.setProperty("noMidNGrams", "true");
		result.setProperty("maxNGramLeng", "6");
		result.setProperty("usePrev", "true");
		result.setProperty("useNext", "true");
//		result.setProperty("useTags", "true");
//		result.setProperty("useWordTag", "true");
		result.setProperty("useLongSequences", "true");
		result.setProperty("useSequences", "true");
		result.setProperty("usePrevSequences", "true");
		result.setProperty("maxLeft", "3");		//2 and 3 work much better than the default 1
		result.setProperty("useTypeSeqs", "true");
		result.setProperty("useTypeSeqs2", "true");
		result.setProperty("useTypeySequences", "true");
		result.setProperty("useOccurrencePatterns", "true");
		result.setProperty("useLastRealWord", "true");
		result.setProperty("useNextRealWord", "true");
		result.setProperty("useReverse", "false");
		result.setProperty("normalize", "true");
//		result.setProperty("normalizeTimex", "true");
		result.setProperty("wordShape", "chris2useLC");
		result.setProperty("useDisjunctive", "true");
		result.setProperty("disjunctionWidth", "5");
//		properties.setProperty("useDisjunctiveShapeInteraction", "true");
		
		result.setProperty("type", "crf");
		
		result.setProperty("readerAndWriter", "edu.stanford.nlp.sequences.ColumnDocumentReaderAndWriter");
		
		result.setProperty("useObservedSequencesOnly", "true");

//		result.setProperty("sigma", "20");
		result.setProperty("useQN", "true");
		result.setProperty("QNsize", "25");
		
		result.setProperty("featureDiffThresh", "0.05");
		
		return result;
	}
	
	@Override
	protected void train(List<List<CoreLabel>> data) throws Exception
	{	logger.increaseOffset();
		
		logger.log("Init training objects");
		// retreive properties object
		Properties properties = setUpProperties();
		// create classifier object
		CRFClassifier<CoreLabel> model = new CRFClassifier<CoreLabel>(properties);
		
		// perform training
		logger.log("Perform training");
		model.train(data);
		
		// record model
		logger.log("Record resulting model");
		String modelFile = modelName.getModelFile();
		model.serializeClassifier(modelFile);
		
		logger.log("Training and recording complete");
		logger.decreaseOffset();
	}
}
