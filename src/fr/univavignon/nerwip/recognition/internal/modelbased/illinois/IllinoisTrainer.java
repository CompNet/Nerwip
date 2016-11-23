package fr.univavignon.nerwip.recognition.internal.modelbased.illinois;

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
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import LBJ2.classify.TestDiscrete;
import LBJ2.learn.BatchTrainer;
import LBJ2.learn.SparseNetworkLearner;
import LBJ2.parse.LinkedVector;
import LBJ2.parse.Parser;
import edu.illinois.cs.cogcomp.LbjNer.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.LbjNer.ExpressiveFeatures.TwoLayerPredictionAggregationFeatures;
import edu.illinois.cs.cogcomp.LbjNer.InferenceMethods.PredictionsAndEntitiesConfidenceScores;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.Data;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.LearningCurveMultiDataset;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NETesterMultiDataset;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.TextChunkRepresentationManager;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.LearningCurveMultiDataset.SampleReader;
import edu.illinois.cs.cogcomp.LbjNer.ParsingProcessingData.PlainTextReader;
import edu.illinois.cs.cogcomp.LbjNer.ParsingProcessingData.TaggedDataReader;
import edu.illinois.cs.cogcomp.LbjNer.ParsingProcessingData.TaggedDataWriter;
import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.recognition.internal.modelbased.AbstractTrainer;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.file.FileTools;

/**
 * This class trains the Illinois Named Entity Tagger
 * on our corpus. This results in the creation of new
 * model files, which can then be used to perform NER
 * instead of the default models.  
 * <br/>
 * A part of this code was inspired by/retrieved from
 * the original Illinois classes. 
 * 
 * @author Vincent Labatut
 */
public class IllinoisTrainer extends AbstractTrainer<Data>
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
	public IllinoisTrainer(IllinoisModelName modelName)
	{	this.modelName = modelName;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATA				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the file containing the data */
	private final static String DATA_FILENAME = FileNames.FO_OUTPUT + File.separator + "illinois.data" + FileNames.EX_TEXT;
	/** Map of EntityType to Illinois type conversion */
	private final static Map<EntityType, String> CONVERSION_MAP = new HashMap<EntityType, String>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put(EntityType.DATE, "DATE");
		CONVERSION_MAP.put(EntityType.LOCATION, "LOC");
		CONVERSION_MAP.put(EntityType.ORGANIZATION, "ORG");
		CONVERSION_MAP.put(EntityType.PERSON, "PER");
	}

	@Override
	protected String getDataPath()
	{	return DATA_FILENAME;
	}
	
	@Override
	protected Data mergeData(Data corpus, Data article)
	{	Data result = null;
		
		// first time
		if(corpus==null)
			result = article;
		
		// general case
		else
		{	result = corpus;
			Vector<NERDocument> docCorp = corpus.documents;
			Vector<NERDocument> docArt = article.documents;
			docCorp.addAll(docArt);
		}
		
		return result;
	}

	@Override
	protected Data convertData(Article article, Mentions mentions)
	{	logger.increaseOffset();
		logger.log("Processing article "+article.getName());
		
		// normalize raw text the Illinois way, as in NETagPlain (line 38)
		String rawText = article.getRawText();
		String normText = PlainTextReader.normalizeText(rawText);
    	Vector<LinkedVector> sentences = PlainTextReader.parseText(normText);

    	// init data object
    	String name = article.getName();
    	NERDocument document = new NERDocument(sentences, name);
    	Data result = new Data(document);
				
    	// Illinois stuff
    	document = result.documents.get(0);
    	sentences = document.sentences;
    	Iterator<LinkedVector> itSent = sentences.iterator();
    	
    	// if there're no mention at all in the reference (shouldn't be the case, though)
    	mentions.sortByPosition();
    	List<AbstractMention<?>> mentionList = mentions.getMentions();
    	if(mentionList.isEmpty())
    	{	while(itSent.hasNext())
	    	{	LinkedVector sentence = itSent.next();
    			for(int i=0;i<sentence.size();i++)
    			{	NEWord word = (NEWord)sentence.get(i);
    				word.neLabel = "O";
    			}
	    	}
    	}
    	
    	// otherwise
    	else
    	{	// mention stuff
    		Iterator<AbstractMention<?>> itEnt = mentionList.iterator();
	    	AbstractMention<?> currentMention = itEnt.next();
			EntityType currentType = currentMention.getType();
			String currentLabel = "B-"+CONVERSION_MAP.get(currentType);
			
			// complete each word with the appropriate annotation
	    	int currentPosition = 0;
	    	// process each sentence in the document
	    	while(itSent.hasNext() && currentMention!=null)
	    	{	LinkedVector sentence = itSent.next();
	    		// process each word in the sentence
	    		int i = 0; 
	    		while(i<sentence.size() && itEnt.hasNext())
		    	{	// get word info
	    			NEWord word = (NEWord)sentence.get(i);
	    			String wStr = word.form;
	    			currentPosition = rawText.indexOf(wStr, currentPosition);
	    			
	    			// compare position to mention, possibly go to next mention (theretically not more than once)
	    			while(currentMention!=null && currentMention.precedesPosition(currentPosition))
	    			{	if(itEnt.hasNext())
	    				{	currentMention = itEnt.next();
	        				currentType = currentMention.getType();
	        				currentLabel = "B-"+CONVERSION_MAP.get(currentType);
	    				}
	    				else
	    					currentMention = null;
	    			}
	    			
	    			// check if word belongs to mention
	    			if(currentMention!=null && currentMention.containsPosition(currentPosition))
	    			{	word.neLabel = currentLabel;
	    				if(currentLabel.startsWith("B"))
	    					currentLabel = "I" + currentLabel.substring(1);
	    			}
	    			else
	    				word.neLabel = "O";
	
	    			// go to next word
		    		i++;
		    	}
	    	}
    	}
    	
		logger.decreaseOffset();
		return result;
	}
	
	@Override
	protected boolean checkData(File dataFile)
	{	boolean result = dataFile.exists();
		return result;
	}

	@Override
	protected Data loadData(File dataFile) throws IOException
	{	logger.increaseOffset();
		Data result	= null;
		
		try
		{	// we directly use the Illinois method
			String path = dataFile.toString();
			String format = "-r";
			String docname = "training file";
			NERDocument document;
			document = TaggedDataReader.readFile(path, format, docname);
			
			result = new Data(document);
		}
		catch (Exception e)
		{	e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		logger.decreaseOffset();
		return result;
	}

	@Override
	protected void recordData(File dataFile, Data data) throws IOException
	{	logger.increaseOffset();

		try
		{	// we directly use the Illinois method
			String path = dataFile.toString();
			String format = "-r";
			NEWord.LabelToLookAt labelType = NEWord.LabelToLookAt.GoldLabel;
			TaggedDataWriter.writeToFile(path, data, format, labelType);
		}
		catch (Exception e)
		{	e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		// close file and finish
		logger.decreaseOffset();
	}

	/////////////////////////////////////////////////////////////////
	// MODEL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** An instance of the model object associated to this trainer */
	protected IllinoisModelName modelName = null;

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
	
		logger.log("Read Illinois configuration file");
		modelName.loadConfig();
		logger.log("Configuration done");
				
		logger.decreaseOffset();
	}
	
	@Override
	protected void train(Data data) throws Exception
	{	logger.increaseOffset();
		
		// pre-processing
		logger.log("Pre-process training set");
		ExpressiveFeaturesAnnotator.annotate(data);
		Vector<Data> train = new Vector<Data>();
		train.addElement(data);
		Vector<Data> test = new Vector<Data>();
		test.addElement(data);
		logger.log("Pre-processing complete");
		
		// perform training and record model
		logger.log("Perform training and record resulting model");
		int fixedNumIterations = -1;
		getLearningCurve(train, test, fixedNumIterations);
		logger.log("Training and recording complete");
		
		logger.decreaseOffset();
	}
	
	/**
	 * This method was taken from {@link LearningCurveMultiDataset},
	 * and modified so that is does not rely on Unix commands but
	 * only on Java instructions instead.
	 * 
	 * @param trainDataSet
	 * 		Training set.
	 * @param testDataSet
	 * 		Test set (can be the same as training set).
	 * @param fixedNumIterations
	 * 		Number of iterations used for training, or -1 for no specified limit.
	 * @throws Exception
	 * 		Problem during training.
	 * 
	 * @author Cognitive Computation Group
	 * @author Vincent Labatut
	 * 		A few modification.
	 */
	private void getLearningCurve(Vector<Data> trainDataSet,Vector<Data> testDataSet,int fixedNumIterations) throws Exception
	{	  
		double bestF1Level1 = -1;
		int bestRoundLevel1 = 0;
		NETaggerLevel1 tagger1 = new NETaggerLevel1(ParametersForLbjCode.currentParameters.pathToModelFile+".level1",ParametersForLbjCode.currentParameters.pathToModelFile+".level1.lex");
		tagger1.forget();

		for(int dataId=0;dataId<trainDataSet.size();dataId++){
			Data trainData = trainDataSet.elementAt(dataId);
			if(ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PredictionsLevel1")){
				//PredictionsAndEntitiesConfidenceScores.annotateWithGoldLabelsWithOmissions(trainData, 0.1, 0.2);
				PredictionsAndEntitiesConfidenceScores.getAndMarkEntities(trainData,NEWord.LabelToLookAt.GoldLabel);
				TwoLayerPredictionAggregationFeatures.setLevel1AggregationFeatures(trainData, true,"training time- induced from gold labels");
			}
		}

				
		System.out.println("Pre-extracting the training data for Level 1 classifier");
		// VL modification starts here
		{	String prefix = modelName.getModelFilePrefix()+".level1.prefetchedTrainData";
			List<File> tempFiles = FileTools.getFilesStartingWith(FileNames.FO_ILLINOIS_MODELS,prefix);
			for(File f: tempFiles)
				f.delete();
		}
		// VL modification starts here
//		Runtime rt = Runtime.getRuntime();
//		Process pr = rt.exec("rm ./ "+ParametersForLbjCode.currentParameters.pathToModelFile+".level1.prefetchedTrainData*");
//		pr.waitFor();
		// VL modification ends here
		{	String prefix = modelName.getModelFilePrefix()+".level1.prefetchedTestData";
			List<File> tempFiles = FileTools.getFilesStartingWith(FileNames.FO_ILLINOIS_MODELS,prefix);
			for(File f: tempFiles)
				f.delete();
		}
		// VL modification ends here
//		pr = rt.exec("rm ./ "+ParametersForLbjCode.currentParameters.pathToModelFile+".level1.prefetchedTestData*");
//		pr.waitFor();

		BatchTrainer bt1train = prefetchAndGetBatchTrainer(tagger1, trainDataSet, ParametersForLbjCode.currentParameters.pathToModelFile+".level1.prefetchedTrainData");
		//BatchTrainerByParts bt1train = prefetchAndGetBatchTrainer(tagger1, trainDataSet, ParametersForLbjCode.currentParameters.pathToModelFile+".level1.prefetchedTrainData", 100);
		System.out.println("Pre-extracting the testing data for Level 1 classifier");
		BatchTrainer bt1test = prefetchAndGetBatchTrainer(tagger1, testDataSet, ParametersForLbjCode.currentParameters.pathToModelFile+".level1.prefetchedTestData");
		//BatchTrainerByParts bt1test = prefetchAndGetBatchTrainer(tagger1, testDataSet, ParametersForLbjCode.currentParameters.pathToModelFile+".level1.prefetchedTestData", 1);
		Parser testParser1 = bt1test.getParser();
		//Parser testParser1 = bt1test.inputParser;
		
		for (int i = 0; (fixedNumIterations==-1&& i < 200 && i-bestRoundLevel1<10) || (fixedNumIterations>0&&i<=fixedNumIterations); ++i) {
			System.out.println("Learning first level classifier; round "+i);
			bt1train.train(1);
			//bt1train.trainOneRound();
	
			System.out.println("Testing level 1 classifier;  on prefetched data, round: "+i);
			testParser1.reset();
			TestDiscrete simpleTest = new TestDiscrete();
			simpleTest.addNull("O");
			TestDiscrete.testDiscrete(simpleTest, tagger1, null,  testParser1, true, 0);
							
			double f1Level1 = simpleTest.getOverallStats()[2];
			if (f1Level1 > bestF1Level1) {
				bestF1Level1 = f1Level1;
				bestRoundLevel1 = i;
				tagger1.save();
			}

			if (i % 5 == 0)
				System.err.println(i  + " rounds.  Best so far for Level1 : (" + bestRoundLevel1 + ")=" + bestF1Level1);
		}
		// VL modification starts here
		{	String prefix = modelName.getModelFilePrefix()+".level1.prefetchedTrainData";
			List<File> tempFiles = FileTools.getFilesStartingWith(FileNames.FO_ILLINOIS_MODELS,prefix);
			for(File f: tempFiles)
				f.delete();
		}
		// VL modification starts here
//		pr = rt.exec("rm ./ "+ParametersForLbjCode.currentParameters.pathToModelFile+".level1.prefetchedTrainData*");
//		pr.waitFor();
		// VL modification ends here
		{	String prefix = modelName.getModelFilePrefix()+".level1.prefetchedTestData";
			List<File> tempFiles = FileTools.getFilesStartingWith(FileNames.FO_ILLINOIS_MODELS,prefix);
			for(File f: tempFiles)
				f.delete();
		}
		// VL modification ends here
//		pr = rt.exec("rm ./ "+ParametersForLbjCode.currentParameters.pathToModelFile+".level1.prefetchedTestData*");
//		pr.waitFor();

		System.out.println("Testing level 1 classifier, final performance: ");
		TestDiscrete[] results = NETesterMultiDataset.printAllTestResultsAsOneDataset(testDataSet,tagger1,null, false);
		double f1Level1 = results[0].getOverallStats()[2];
		System.out.println("Level 1; round "+bestRoundLevel1 + "\t" + f1Level1);

		
		NETaggerLevel2 tagger2 = new NETaggerLevel2(ParametersForLbjCode.currentParameters.pathToModelFile+".level2",ParametersForLbjCode.currentParameters.pathToModelFile+".level2.lex");
		tagger2.forget();
		if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PatternFeatures")||
							ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PredictionsLevel1")) {
			double bestF1Level2 = -1;
			int bestRoundLevel2 = 0;
			
			// VL modification starts here
			{	String prefix = modelName.getModelFilePrefix()+".level2.prefetchedTrainData";
				List<File> tempFiles = FileTools.getFilesStartingWith(FileNames.FO_ILLINOIS_MODELS,prefix);
				for(File f: tempFiles)
					f.delete();
			}
			// VL modification starts here
//			pr = rt.exec("rm ./ "+ParametersForLbjCode.currentParameters.pathToModelFile+".level2.prefetchedTrainData*");
//			pr.waitFor();
			// VL modification ends here
			{	String prefix = modelName.getModelFilePrefix()+".level2.prefetchedTestData";
				List<File> tempFiles = FileTools.getFilesStartingWith(FileNames.FO_ILLINOIS_MODELS,prefix);
				for(File f: tempFiles)
					f.delete();
			}
			// VL modification ends here
//			pr = rt.exec("rm ./ "+ParametersForLbjCode.currentParameters.pathToModelFile+".level2.prefetchedTestData*");
//			pr.waitFor();
			System.out.println("Pre-extracting the training data for Level 2 classifier");
			BatchTrainer bt2train = prefetchAndGetBatchTrainer(tagger2, trainDataSet, ParametersForLbjCode.currentParameters.pathToModelFile+".level2.prefetchedTrainData");
			//BatchTrainerByParts bt2train = prefetchAndGetBatchTrainer(tagger2, trainDataSet, ParametersForLbjCode.currentParameters.pathToModelFile+".level2.prefetchedTrainData", 100);
			System.out.println("Pre-extracting the testing data for Level 2 classifier");
			BatchTrainer bt2test = prefetchAndGetBatchTrainer(tagger2, testDataSet, ParametersForLbjCode.currentParameters.pathToModelFile+".level2.prefetchedTestData");
			//BatchTrainerByParts bt2test = prefetchAndGetBatchTrainer(tagger2, testDataSet, ParametersForLbjCode.currentParameters.pathToModelFile+".level2.prefetchedTestData",1);
			Parser testParser2 = bt2test.getParser();
			//Parser testParser2 = bt2test.inputParser;
			
			for (int i = 0; (fixedNumIterations==-1&& i < 200 && i-bestRoundLevel2<10) || (fixedNumIterations>0&&i<=fixedNumIterations); ++i) {
				System.out.println("Learning Level2 ; round "+i);
				bt2train.train(1);
				//bt2train.trainOneRound();
				
				System.out.println("Testing level 2 classifier;  on prefetched data, round: "+i);
				testParser2.reset();
				TestDiscrete simpleTest = new TestDiscrete();
				simpleTest.addNull("O");
				TestDiscrete.testDiscrete(simpleTest, tagger2, null,  testParser2, true, 0);

		
				double f1Level2 = simpleTest.getOverallStats()[2];
				if (f1Level2 > bestF1Level2) {
					bestF1Level2 = f1Level2;
					bestRoundLevel2 = i ;
					tagger2.save();
				}

				if ( i % 5 == 0)
					System.err.println(i + " rounds.  Best so far for Level2 : (" + bestRoundLevel2 + ") " + bestF1Level2);
			}
			// VL modification starts here
			{	String prefix = modelName.getModelFilePrefix()+".level2.prefetchedTrainData";
				List<File> tempFiles = FileTools.getFilesStartingWith(FileNames.FO_ILLINOIS_MODELS,prefix);
				for(File f: tempFiles)
					f.delete();
			}
			// VL modification starts here
//			pr = rt.exec("rm ./ "+ParametersForLbjCode.currentParameters.pathToModelFile+".level2.prefetchedTrainData*");
//			pr.waitFor();
			// VL modification ends here
			{	String prefix = modelName.getModelFilePrefix()+".level2.prefetchedTestData";
				List<File> tempFiles = FileTools.getFilesStartingWith(FileNames.FO_ILLINOIS_MODELS,prefix);
				for(File f: tempFiles)
					f.delete();
			}
			// VL modification ends here
//			pr = rt.exec("rm ./ "+ParametersForLbjCode.currentParameters.pathToModelFile+".level2.prefetchedTestData*");
//			pr.waitFor();

			System.out.println("Testing both levels ...");
			results = NETesterMultiDataset.printAllTestResultsAsOneDataset(testDataSet,tagger1,tagger2,false);
			f1Level1 = results[0].getOverallStats()[2];
			double f1Level2 = results[1].getOverallStats()[2];
			System.out.println("Level1: bestround="+ bestRoundLevel1 + "\t F1=" + f1Level1+"\t Level2: bestround="+ bestRoundLevel2 + "\t F1=" + f1Level2);
		}
		
		
		/*
		 * This will override the models forcing to save the iteration we're interested in- the fixedNumIterations iteration, the last one.
		 * But note - both layers will be saved for this iteration. If the best performance for one of the layers came before the final
		 * iteration, we're in a small trouble- the performance will decrease 
		 */
		if(fixedNumIterations>-1){
			tagger1.save();
			tagger2.save();			
		}
	}

	/**
	 * This method is also taken from {@link LearningCurveMultiDataset},
	 * since it is used by {@link #getLearningCurve(Vector, Vector, int)}.
	 * <br/>
	 * <b>Original comment:</b> Parts is the number of parts to which we split the data.
	 * in training - if you have a lot of samples- use 100 partitions	
	 * otherwise, the zip doesn't work on training files larger than 4G.
	 * 
	 * @param classifier 
	 * 		?
	 * @param dataSets
	 * 		? 
	 * @param exampleStorePath
	 * 		? 
	 * @return
	 * 		?
	 * 
	 * @author Cognitive Computation Group
	 */
	private BatchTrainer prefetchAndGetBatchTrainer(SparseNetworkLearner classifier, Vector<Data> dataSets, String exampleStorePath) {
		System.out.println("Pre-extracting the training data for Level 1 classifier");
		for(int dataId=0;dataId<dataSets.size();dataId++){
			Data data = dataSets.elementAt(dataId);
			TextChunkRepresentationManager.changeChunkRepresentation(
				TextChunkRepresentationManager.EncodingScheme.BIO, 
				ParametersForLbjCode.currentParameters.taggingEncodingScheme, 
				data, NEWord.LabelToLookAt.GoldLabel);
		}
		BatchTrainer bt = new BatchTrainer(classifier, new SampleReader(dataSets), 1000);
		classifier.setLexicon(bt.preExtract(exampleStorePath));
		//BatchTrainerByParts bt = new BatchTrainerByParts(new SampleReader(dataSets), classifier, parts, exampleStorePath);
		for(int dataId=0; dataId<dataSets.size(); dataId++) {
			Data trainData = dataSets.elementAt(dataId);
			TextChunkRepresentationManager.changeChunkRepresentation(
					ParametersForLbjCode.currentParameters.taggingEncodingScheme, 
					TextChunkRepresentationManager.EncodingScheme.BIO, 
					trainData, NEWord.LabelToLookAt.GoldLabel);
		}
		return bt;
	}
}
