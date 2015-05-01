package tr.edu.gsu.nerwip.recognition.combiner.svmbased;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.evaluation.ArticleList;
import tr.edu.gsu.nerwip.evaluation.Evaluator;
import tr.edu.gsu.nerwip.evaluation.measure.AbstractMeasure;
import tr.edu.gsu.nerwip.evaluation.measure.LilleMeasure;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.combiner.CategoryProportions;
import tr.edu.gsu.nerwip.recognition.combiner.VoteWeights;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner.CombineMode;
import tr.edu.gsu.nerwip.retrieval.ArticleRetriever;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.time.TimeFormatting;

/**
 * This class trains a SVM on the corpus. It uses the 
 * outputs of differnt NER tools as the SVM inputs, and
 * uses the reference files as the theoretical outputs.
 * <br/>
 * Some of the source code is inspired by, or directly
 * comes from the original classes. Cf. 
 * <a href="http://www.csie.ntu.edu.tw/~cjlin/papers/guide/guide.pdf">this document</a>.
 * <br/>
 * Additional references :
 * <ul>
 * 	<li><a href="http://www.csie.ntu.edu.tw/~cjlin/papers/guide/guide.pdf">Guide</a></li>
 * 	<li><a href="http://www.csie.ntu.edu.tw/~cjlin/libsvm/">Site</a></li>
 * 	<li><a href="http://www.bios.unc.edu/~kosorok/codes_rfe/spider/Optimization/libsvm/libsvm-2.31/README">Readme</a></li>
 * </ul>
 * The resulting model is automatically recorded,
 * and will be loaded later when the combiner will be used.
 * 
 * @author Vincent Labatut
 */
public class SvmTrainer
{	
	/**
	 * Creates a new trainer
	 * for the specified SVM combiner.
	 * 
	 * @param combiner
	 * 		SVM combiner considered for the training.
	 * 
	 * @throws RecognizerException
	 * 		Problem while creating the dummy combiner.
	 */
	public SvmTrainer(SvmCombiner combiner) throws RecognizerException
	{	this.combiner = combiner;
	}
	
	/////////////////////////////////////////////////////////////////
	// MODEL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** An instance of the combiner object associated to this trainer */
	protected SvmCombiner combiner = null;
	
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
	 * Changes the cache flag. If {code true}, the {@link #process(ArticleList,boolean) process}
	 * method will first check if the input data already
	 * exists as a file. In this case, they will be loaded
	 * from this file. Otherwise, the process will be
	 * conducted normally, then recorded (cached).
	 * 
	 * @param enabled
	 * 		If {@code true}, the (possibly) cached files are used.
	 */
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
	}

	/**
	 * Enable/disable the caches of each individual
	 * NER tool used by the combiner of this trainer.
	 * By default, the caches are set to the default
	 * values of the individual recognizers.
	 * 
	 * @param enabled
	 * 		Whether or not the combiner cache should be enabled.
	 */
	public void setSubCacheEnabled(boolean enabled)
	{	combiner.setCacheEnabled(enabled);
	}

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
	private String getDataPath()
	{	String base = combiner.getFolder();
		String result = FileNames.FO_OUTPUT + File.separator + "svm.data." + base + FileNames.EX_TXT;
		return result;
	}
	
	/**
	 * Analyses existing annotation and reference files,
	 * in order to extract the data necessary for the SVM
	 * to run. The result is cached as a file in the output
	 * folder.
	 * 
	 * @param recognizers 
	 * 		List of NER tools to be applied.
	 * @param folders 
	 * 		List of articles to be processed.
	 * @return
	 * 		A 'problem' object representing the corpus and estimated data.
	 * 
	 * @throws IOException 
	 * 		Problem while loading references or recording converted data.
	 * @throws ParseException 
	 * 		Problem while loading references.
	 * @throws SAXException 
	 * 		Problem while loading references.
	 * @throws ReaderException 
	 * 		Problem while loading references.
	 * @throws RecognizerException 
	 * 		Problem while applying a NER tool.
	 */
	private svm_problem prepareData(List<AbstractRecognizer> recognizers, ArticleList folders) throws IOException, SAXException, ParseException, ReaderException, RecognizerException
	{	logger.increaseOffset();
		svm_problem result = null;
	
		// if the data file exist >> load
		String dataPath = getDataPath();
		File dataFile = new File(dataPath);
		if(cache && dataFile.exists())
			result = loadData(dataFile);

		// otherwise >> process data
		else
		{	// process each article
			for(File folder: folders)
			{	// get article
				String name = folder.getName();
				ArticleRetriever retriever = new ArticleRetriever();
				Article article = retriever.process(name);
					
				Map<AbstractRecognizer,Entities> entities = new HashMap<AbstractRecognizer, Entities>();
				
				// get reference entities
				Entities refEntities = article.getReferenceEntities();
				// keep only those allowed for this training
				combiner.filterType(refEntities);
				entities.put(null, refEntities);
				
				// get estimated entities for each recognizer
				for(AbstractRecognizer recognizer: recognizers)
				{	Entities estEntites = recognizer.process(article);
					combiner.filterType(estEntites);
					entities.put(recognizer, estEntites);
				}
				
				// convert all these entities to something the SVM can process
				svm_problem conv = convertEntities(article,entities);
				// add to the rest of the data
				result = mergeData(result,conv);
			}
			
			// record the resulting SVM data
			recordData(dataFile, result);
		}
	
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Builds a new data object by merging
	 * both specified data object. The content
	 * of the first one is copied first, then
	 * that of the second one.
	 * 
	 * @param data1
	 * 		One SVM data object.
	 * @param data2
	 * 		The other SVM data object.
	 * @return
	 * 		A merge of both data objects.
	 */
	private svm_problem mergeData(svm_problem data1, svm_problem data2)
	{	svm_problem result = new svm_problem();
		
		if(data1==null)
			result = data2;
		
		else
		{	// init new object
			result.l = data1.l + data2.l;
			result.x = new svm_node[result.l][];
			result.y = new double[result.l];
			
			// fill new object
			for(int i=0;i<data1.l;i++)
			{	result.y[i] = data1.y[i];
				result.x[i] = data1.x[i];
			}
			for(int i=data1.l;i<result.l;i++)
			{	result.y[i] = data2.y[i-data1.l];
				result.x[i] = data2.x[i-data1.l];
			}
		}
		
		return result;
	}
	
	/**
	 * Loads the data contained in the specified file.
	 * This data must follow the svmlight format:
	 * <br/>
	 * {@code <output> 1:input 2:input .... m:input}
	 * 
	 * @param dataFile
	 * 		File containing the data to be read.
	 * @return
	 * 		A 'problem' object representing the read data.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the data file.
	 * 
	 * @author Chih-Chung Chang
	 * @author Chih-Jen Lin
	 * @author Vincent Labatut
	 * 		(A few adaptations)
	 */
	private svm_problem loadData(File dataFile) throws IOException
	{	logger.increaseOffset();
		// open file
		FileReader fr = new FileReader(dataFile);
		BufferedReader fp = new BufferedReader(fr);
		
		// get values
		List<Double> vy = new ArrayList<Double>();
		List<svm_node[]> vx = new ArrayList<svm_node[]>();
		int maxIndex = 0;

		String line = fp.readLine();
		while(line!=null)
		{	// split line
			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
			
			// get output
			String yStr = st.nextToken();
			double yVal = Double.valueOf(yStr).doubleValue();
			vy.add(yVal);
			
			// get inputs
			int m = st.countTokens()/2;
			svm_node x[] = new svm_node[m];
			for(int j=0;j<m;j++)
			{	x[j] = new svm_node();
				String xIdxStr = st.nextToken();
				x[j].index = Integer.parseInt(xIdxStr);
				String xValStr = st.nextToken();
				x[j].value = Double.valueOf(xValStr).doubleValue();
			}
			if(m>0) 
				maxIndex = Math.max(maxIndex, x[m-1].index);
			vx.add(x);
			
			// read next line
			line = fp.readLine();
		}

		// build 'problem' object
		svm_problem result = new svm_problem();
		// add size
		result.l = vy.size();
		// add input values
		result.x = new svm_node[result.l][];
		for(int i=0;i<result.l;i++)
			result.x[i] = vx.get(i);
		// add output values
		result.y = new double[result.l];
		for(int i=0;i<result.l;i++)
			result.y[i] = vy.get(i);

		// close file and finish
		fp.close();
		logger.decreaseOffset();
		return result;
	}

	/**
	 * Records the specified data as a file,
	 * for caching purposes. 
	 * 
	 * @param dataFile
	 * 		File to contain the SVM data.
	 * @param data
	 * 		Data to be recorded (a 'problem' object)
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the data file.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the data file.
	 */
	private void recordData(File dataFile, svm_problem data) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.increaseOffset();
		
		// open file
		PrintWriter printWriter = FileTools.openTextFileWrite(dataFile);
		
		// record data
		for(int i=0;i<data.l;i++)
		{	StringBuffer line = new StringBuffer();
		
			// add output values
			double y = data.y[i];
			line.append(y);

			// add input values
			svm_node[] xs = data.x[i];
			for(int j=0;j<xs.length;j++)
			{	svm_node x = xs[j];
				line.append("\t"+x.index+":"+x.value);
			}
			
			// write to file
			printWriter.println(line);
		}
		
		// close file and finish
		logger.decreaseOffset();
		printWriter.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Convert the specified entities to a format appropriate
	 * for SVM.
	 * 
	 * @param article
	 * 		Article to process.
	 * @param entities
	 * 		Reference and estimated entities.
	 * @return
	 * 		A 'problem' object, representing SVM inputs and outputs
	 * 		using an appropriate format.
	 */
	private svm_problem convertEntities(Article article, Map<AbstractRecognizer,Entities> entities)
	{	logger.increaseOffset();
		svm_problem result;
		CombineMode combineMode = combiner.getCombineMode();
		
		if(combineMode.isChunkBased())
			result = convertEntitiesByWord(article, entities);
		else
			result = convertEntitiesByEntity(article, entities);
		
		logger.decreaseOffset();
		return result;
	}

	/**
	 * Convert the specified entities to a format appropriate for SVM.
	 * The conversion is processed entity group by entity group.
	 * 
	 * @param article
	 * 		Article to process.
	 * @param entities
	 * 		Reference and estimated entities.
	 * @return
	 * 		A 'problem' object, representing SVM inputs and outputs
	 * 		using an appropriate format.
	 */
	private svm_problem convertEntitiesByEntity(Article article, Map<AbstractRecognizer,Entities> entities)
	{	logger.increaseOffset();
		logger.log("Processing article "+article.getName());
		
		// retrieve overlapping entities
		List<Map<AbstractRecognizer,AbstractEntity<?>>> overlaps = Entities.identifyOverlaps(entities);
		
		// init data object
		svm_problem result = new svm_problem();
		result.l = overlaps.size();
		result.x = new svm_node[result.l][];
		result.y = new double[result.l];
		int index = 0;
		
		// convert to SVM format
		for(Map<AbstractRecognizer, AbstractEntity<?>> overlap: overlaps)
		{	Map<AbstractRecognizer, AbstractEntity<?>> estimations = overlap;
			AbstractEntity<?> refEntity = overlap.get(null);
			EntityType refType = null;
			
			// get reference entity type
			if(refEntity!=null)
			{	refType = refEntity.getType();
				estimations = new HashMap<AbstractRecognizer, AbstractEntity<?>>(overlap);
				estimations.remove(null);
			}
			
			// convert entities
			convertEntityGroupToSvm(result,index,article,refType,estimations);
			
			index++;
		}
		
		logger.decreaseOffset();
		return result;
	}

	/**
	 * Convert the specified entities to a format appropriate for SVM.
	 * The conversion is processed word by word.
	 * 
	 * @param article
	 * 		Article to process.
	 * @param entities
	 * 		Reference and estimated entities.
	 * @return
	 * 		A 'problem' object, representing SVM inputs and outputs
	 * 		using an appropriate format.
	 */
	private svm_problem convertEntitiesByWord(Article article, Map<AbstractRecognizer,Entities> entities)
	{	logger.increaseOffset();
		logger.log("Processing article "+article.getName());
		String rawText = article.getRawText();
		
		// retrieve word-entity couples
		List<Map<AbstractRecognizer,WordEntity>> wordEntities = combiner.identifyWordEntityOverlaps(article,entities);
		// count non-empty maps (length of the output)
		int size = 0;
		for(Map<AbstractRecognizer,WordEntity> weMap: wordEntities)
		{	if(!weMap.isEmpty())
				size++;
		}
		
		// init data object
		logger.log("Init SVM data object");
		svm_problem result = new svm_problem();
		result.l = size;
		result.x = new svm_node[result.l][];
		result.y = new double[result.l];
		
		// convert to SVM format
		logger.log("Convert data to SVM format");
		logger.increaseOffset();
		int index = 0;
		Boolean prevBeginning = null;
		EntityType prevType = null;
		for(Map<AbstractRecognizer,WordEntity> weMap: wordEntities)
		{	Map<AbstractRecognizer,WordEntity> estimations = weMap;
			
			if(weMap.isEmpty())
			{	prevBeginning = null;
				prevType = null;
			}
			
			else
			{	WordEntity refWe = weMap.get(null);
				
				// remove reference word-entity
				if(refWe!=null)
				{	String word = rawText.substring(refWe.getStartPosition(),refWe.getEndPosition());
					logger.log("Processing word \""+word+"\" ("+refWe.getEntity()+")");
					
					estimations = new HashMap<AbstractRecognizer, WordEntity>(weMap);
					estimations.remove(null);
				}
				else
					logger.log("Reference word is empty");
				
				// convert entities
				convertEntityWordToSvm(result,index,article,prevType,prevBeginning,refWe,estimations);
				
				// update previous info
				if(refWe==null)
				{	prevBeginning = null;
					prevType = null;
				}
				else
				{	prevBeginning = refWe.isBeginning();
					prevType = refWe.getType();
				}
				
				index++;
			}
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		return result;
	}

	/**
	 * Completes the specified SVM {@code data} object using
	 * the specified reference entity type and estimated
	 * entities. The reference type can be {@code null}, 
	 * if no entity actually exists. 
	 * 
	 * @param data
	 * 		SVM data object to be completed.
	 * @param index
	 * 		Position in the data object.
	 * @param article
	 * 		Article to process.
	 * @param refType
	 * 		Type of the reference entity, 
	 * 		or {@code null} if no reference entity. 
	 * @param estEntities
	 * 		Corresponding estimated entities.
	 */
	private void convertEntityGroupToSvm(svm_problem data, int index, Article article, EntityType refType, Map<AbstractRecognizer,AbstractEntity<?>> estEntities)
	{	logger.increaseOffset();
		
		// use combiner to process output
		logger.log("Converting output");
		data.y[index] = convertOutputToSvm(refType);
		
		// use combiner to process inputs
		logger.log("Converting inputs");
		data.x[index] = combiner.convertEntityGroupToSvm(estEntities, article);
		
		logger.decreaseOffset();
	}
	
	/**
	 * Completes the specified SVM {@code data} object using
	 * the specified reference and estimated word-entity couples.
	 * The reference type can be {@code null}, if no entity actually 
	 * exists.
	 * 
	 * @param data
	 * 		SVM data object to be completed.
	 * @param index
	 * 		Position in the data object.
	 * @param article
	 * 		Article to process.
	 * @param prevType
	 * 		Type used for the previous chunk.
	 * @param prevBeginning
	 * 		BIO state used for the previous chunk.
	 * @param refWe
	 * 		Reference word-entity couple, or {@code null} if none. 
	 * @param estWe
	 * 		Corresponding estimated word-entity couples.
	 */
	private void convertEntityWordToSvm(svm_problem data, int index, Article article, EntityType prevType, Boolean prevBeginning, WordEntity refWe, Map<AbstractRecognizer,WordEntity> estWe)
	{	logger.increaseOffset();
		
		// use combiner to process output
		logger.log("Converting output");
		data.y[index] = convertOutputToSvm(refWe);
		
		// use combiner to process inputs
		logger.log("Converting inputs");
		data.x[index] = combiner.convertEntityWordToSvm(prevType, prevBeginning, estWe, article);
if(data.x[index]==null || index==131)
	System.out.print("");
		
		logger.decreaseOffset();
	}

	/**
	 * Converts the specified type to a
	 * double value the SVM can interpret.
	 * <br/>
	 * This method is used during training,
	 * when using the entity-by-entity mode.
	 * 
	 * @param type
	 * 		Entity type to be converted.
	 * @return
	 * 		The corresponding {@code double} value.
	 */
	protected double convertOutputToSvm(EntityType type)
	{	double result = 0;
		
		// no entity was detected >> null type
		if(type==null)
			result = 1;
		// an entity was detected >> just get its position
		else
		{	List<EntityType> handledTypes = combiner.getHandledEntityTypes();
			result = handledTypes.indexOf(type) + 2;
		}
		
		return result;
	}

	/**
	 * Converts the specified type to a
	 * double value the SVM can interpret.
	 * <br/>
	 * This method is used during training,
	 * when using the word-by-word mode.
	 * 
	 * @param wordEntity
	 * 		Couple word-entity to be converted.
	 * @return
	 * 		The corresponding {@code double} value.
	 */
	protected double convertOutputToSvm(WordEntity wordEntity)
	{	logger.log("Word-entity to be converted: "+wordEntity);
		logger.increaseOffset();
		double result = 0;
		
		// no entity was detected >> null type
		if(wordEntity==null)
			result = 1;
		// an entity was detected >> just get its position
		else
		{	AbstractEntity<?> entity = wordEntity.getEntity();
			int wordStart = wordEntity.getStartPosition();
			List<EntityType> handledTypes = combiner.getHandledEntityTypes();
			EntityType type = entity.getType();
			result = (handledTypes.indexOf(type)+1)*2;
			int startPos = entity.getStartPos();
			if(wordStart>startPos)
				result++;
		}
		
		logger.decreaseOffset();
		logger.log("result: "+result);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// SVM PARAMETERS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Limit used when estimating the best parameters throught the grid method */
//	private final static double RESOLUTION_LIMIT = 0.01; TODO original value
	private final static double RESOLUTION_LIMIT = 0.1;
	/** Size of the memory cache (in MB) */ 
	private final static int CACHE_SIZE = 1024;
	/** Structure used to record the previously processed values when applying the grid method */
	private final Map<Double,Map<Double,Double>> gridValues = new HashMap<Double, Map<Double,Double>>();
	
	/**
	 * Sets the initial parameters of the SVM.
	 * Some of them are then estimated using the
	 * grid method, as advised in the original
	 * documentation (cf. <a href="http://www.csie.ntu.edu.tw/~cjlin/papers/guide/guide.pdf">this document</a>).
	 * 
	 * @return 
	 * 		An object representing the SVM parameters
	 */
	private svm_parameter initParameters()
	{	svm_parameter result = new svm_parameter();
		
		result.svm_type = svm_parameter.C_SVC;
		result.kernel_type = svm_parameter.RBF;
//		result.gamma;
		result.eps = 0.001;
//		result.C;
		
		/* 
		 * From the original documentation:
		 * 
		 * nr_weight, weight_label, and weight are used to change the penalty
		 * for some classes (If the weight for a class is not changed, it is
		 * set to 1). This is useful for training classifier using unbalanced
		 * input data or with asymmetric misclassification cost.
		 * 
		 * nr_weight is the number of elements in the array weight_label and
		 * weight. Each weight[i] corresponds to weight_label[i], meaning that
		 * the penalty of class weight_label[i] is scaled by a factor of weight[i].
		 * 
		 * If you do not want to change penalty for any of the classes,
		 * just set nr_weight to 0.
		 */
		result.nr_weight = 0; 
//		result.weight_label;
//		result.weight;
//		result.shrinking;
//		result.probability;
		
		return result;
	}
	
	/**
	 * Estimates the best parameters using the grid method.
	 * 
	 * @param foldNbr
	 * 		Number of folds for the cross-validation step.
	 * @param c
	 * 		Lower and upper bounds, and step value for parameter C of the SVM.
	 * @param gamma
	 * 		Lower and upper bounds, and step value for parameter gamma of the SVM.
	 * @param data
	 * 		Training data.
	 * @return
	 * 		Estimated parameters.
	 */
	private svm_parameter estimateParametersGrid(int foldNbr, double c[], double gamma[], svm_problem data)
	{	svm_parameter result = initParameters();
		gridValues.clear();
		double score = applyGridMethod(result, foldNbr, c, gamma, data);
		logger.log("score: "+score);
		return result;
	}
	
	/**
	 * Recursively estimates the best parameters using the grid method.
	 * 
	 * @param parameters
	 * 		Current parameter values.
	 * @param foldNbr
	 * 		Number of folds for the cross-validation step.
	 * @param c
	 * 		Lower and upper bounds, and step value for parameter C of the SVM.
	 * @param gamma
	 * 		Lower and upper bounds, and step value for parameter gamma of the SVM.
	 * @param data
	 * 		Training data.
	 * @return
	 * 		Score obtained with the estimated parameters. 
	 */
	private double applyGridMethod(svm_parameter parameters, int foldNbr, double c[], double gamma[], svm_problem data)
	{	logger.increaseOffset();
		logger.log("Parameters: c[]="+Arrays.toString(c)+" gamma[]="+Arrays.toString(gamma));
		double cOpt = 0;
		double gammaOpt = 0;
		double scoreOpt = 0;
		
		logger.log("Processing all parameters combinations");
		logger.increaseOffset();
		int nbr = 0;
		for(double cVal=c[0];cVal<=c[1];cVal=cVal+c[2])
		{	// check if the c value was previously processed
			Map<Double,Double> cMap = gridValues.get(cVal);
			if(cMap==null)
			{	cMap = new HashMap<Double, Double>();
				gridValues.put(cVal, cMap);
			}
			
			for(double gammaVal=gamma[0];gammaVal<=gamma[1];gammaVal=gammaVal+gamma[2])
			{	// check if the gamma value was previously processed
				Double score = cMap.get(gammaVal);
				
				// not process before, so we do it now
				if(score==null)
				{	// update SVM parameters
					parameters.C = Math.pow(2,cVal);
					parameters.gamma = Math.pow(2,gammaVal);
					logger.log("cVal="+cVal+" ; gammaVal="+gammaVal+" ; parameters.C="+parameters.C+" ; parameters.gamma="+parameters.gamma);
	
					// process performance
					long startTime = System.currentTimeMillis();
						score = applyCrossValidation(parameters, data, foldNbr);
						cMap.put(gammaVal, score);
					long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					logger.log("score="+score+" time elapsed: "+TimeFormatting.formatDuration(elapsedTime));
				}
				
				// possibly update optimal parameter values
				if(score>scoreOpt)
				{	scoreOpt = score;
					cOpt = cVal;
					gammaOpt = gammaVal;
				}
				
				nbr++;
			}
		}
		logger.decreaseOffset();
		logger.log("cOpt="+cOpt+" ; gammaOpt="+gammaOpt+" ; scoreOpt="+scoreOpt);
		
		// possibly repeat the process to obtain better parameters
		boolean update = true;
		logger.log("c="+c[2]+" ; gamma="+gamma[2]+" (resolution limit="+RESOLUTION_LIMIT+")");
		if(Math.abs(c[2])>=RESOLUTION_LIMIT && Math.abs(gamma[2])>=RESOLUTION_LIMIT)
		{	// update bounds: general case
			if(nbr>9)
			{	c[0] = cOpt - c[2];
				c[1] = cOpt + c[2];
				gamma[0] = gammaOpt - gamma[2];
				gamma[1] = gammaOpt + gamma[2];
			}
			// special case : only 9 points in the grid >> we switch to the finer resolution right now
			else
			{	c[0] = cOpt - c[2]/10;
				c[1] = cOpt + c[2]/10;
				gamma[0] = gammaOpt - gamma[2]/10;
				gamma[1] = gammaOpt + gamma[2]/10;
			}
			
			// update resolution
			c[2] = c[2] / 10;
			gamma[2] = gamma[2] / 10;
			
			// apply grid method at a finer resolution
			double scoreOptTemp = applyGridMethod(parameters, foldNbr, c, gamma, data);
			
			// if not better, reset to current best
			update = scoreOptTemp<=scoreOpt;
		}
			
		// update parameter object with current best
		if(update)	
		{	parameters.C = Math.pow(2,cOpt);
			parameters.gamma = Math.pow(2,gammaOpt);
		}	
		
		logger.decreaseOffset();
		return scoreOpt;
	}
	
	/////////////////////////////////////////////////////////////////
	// VOTE DATA		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes all data required for voting (if some
	 * voting is involved in the combination process set
	 * up for the considered combiner).
	 * 
	 * @param folders
	 * 		List of articles.
	 * 
	 * @throws ReaderException
	 * 		Problem while retrieving an article.
	 * @throws IOException
	 * 		Problem while retrieving an article.
	 * @throws ParseException
	 * 		Problem while retrieving an article.
	 * @throws SAXException
	 * 		Problem while retrieving an article.
	 * @throws RecognizerException 
	 * 		Problem applying the evaluator.
	 * @throws ConverterException 
	 * 		Problem applying the evaluator.
	 */
	private void processVoteData(ArticleList folders) throws ReaderException, IOException, ParseException, SAXException, ConverterException, RecognizerException
	{	CombineMode combineMode = combiner.getCombineMode();
		if(combineMode.hasWeights())
		{	// vote weights
			{	// process
				List<EntityType> types = combiner.getHandledEntityTypes();
				List<AbstractRecognizer> recognizers = combiner.getRecognizers();
				AbstractMeasure measure = new LilleMeasure(null);
				Evaluator evaluator = new Evaluator(types, recognizers, folders, measure);
				evaluator.process();
				List<String> names = Arrays.asList(
					LilleMeasure.SCORE_FP,
					LilleMeasure.SCORE_FR
				);
				boolean byCategory = combineMode==CombineMode.ENTITY_WEIGHTED_CATEGORY;
				VoteWeights voteWeights = VoteWeights.buildWeightsFromEvaluator(evaluator,names,byCategory);
				
				// record
				String filePath = combiner.getVoteWeightsPath();
				voteWeights.recordVoteWeights(filePath);
			}
			
			// category proportions
			if(combineMode==CombineMode.ENTITY_WEIGHTED_CATEGORY)
			{	// process
				CategoryProportions result = CategoryProportions.buildProportionsFromCorpus(folders);
				
				// record
				String filePath = combiner.getCategoryProportionsPath();
				result.recordCategoryProportion(filePath);
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Perform a cross-validation on the specified data, using
	 * the specified parameters.
	 * 
	 * @param parameters 
	 * 		Parameters of the SVM.
	 * @param data 
	 * 		Data to process.
	 * @param nbrFold 
	 * 		Number of cross-validation folds.
	 * @return 
	 * 		Percent of correctly classified instances.
	 * 
	 * @author Chih-Chung Chang
	 * @author Chih-Jen Lin
	 * @author Vincent Labatut
	 * 		(Simplification)
	 */
	private double applyCrossValidation(svm_parameter parameters, svm_problem data, int nbrFold)
	{	logger.increaseOffset();
		int totalCorrect = 0;
		double[] target = new double[data.l];

		// apply cross validation
		svm.svm_cross_validation(data,parameters,nbrFold,target);
		
		// count correctly classified instances
		for(int i=0;i<data.l;i++)
		{	if(target[i] == data.y[i])
				totalCorrect++;
		}
		
		double result = totalCorrect /(double)data.l;
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		logger.log("Cross Validation Accuracy = "+df.format(100*result)+"%");
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Trains the SVM on the specified data,
	 * for the specified entity types.
	 * 
	 * @param folders
	 * 		List of concered articles.
	 * @param useDefaultParams 
	 * 		Whether default parameters should be used (by opposition to 
	 * 		an ad hoc estimation of the most appropriate ones)
	 * 
	 * @throws IOException
	 * 		Problem while accessing a file. 
	 * @throws SAXException
	 * 		Problem while accessing an entity file. 
	 * @throws ParseException
	 * 		Problem while accessing an entity file. 
	 * @throws ReaderException
	 * 		Problem while accessing a file. 
	 * @throws RecognizerException
	 * 		Problem while applying a NER tool. 
	 * @throws ConverterException 
	 * 		Problem while processing a NER tool performance. 
	 */
	public void process(ArticleList folders, boolean useDefaultParams) throws IOException, SAXException, ParseException, ReaderException, RecognizerException, ConverterException
	{	logger.increaseOffset();
		
		// get the recognizers
		List<AbstractRecognizer> recognizers = combiner.getRecognizers();
		// prepare the data
		logger.log("Preparing the training data");
		svm_problem data = prepareData(recognizers,folders);
		
		// init parameters
		logger.log("Initializing parameters");
		logger.log("Number of features: "+data.x[0].length);
		svm_parameter parameters;
		if(useDefaultParams)
		{	parameters = initParameters();
			parameters.C = 1;							// default value : 1
			parameters.gamma = 1f/data.x[0].length;		// default value : 1/num_features
			logger.log("Use default parameters: C="+parameters.C+" gamma="+parameters.gamma);
		}
		else
		{	// estimate optimal parameters
			logger.log("Estimate optimal parameters");
			int foldNbr = 10;
			// recommended starting values
//			double c[] = {-5, 15, 2};		
//			double gamma[] = {-15, 3, 2};
//			logger.log("Use recommended values: c[]="+Arrays.toString(c)+" gamma[]="+Arrays.toString(gamma));
			// focus on the default value
			double c[] = {-1,1,1};									// 2^0 = 1
			double arg = Math.log(1f/data.x[0].length)/Math.log(2); // 2^log2(1/num_features) = 1/num_features
			double gamma[] = {arg-1,arg+1,1};						// log2(x) = ln(x)/ln(2)
			logger.log("Focus on default values: c[]="+Arrays.toString(c)+" gamma[]="+Arrays.toString(gamma));
			parameters = estimateParametersGrid(foldNbr,c,gamma,data);
		}
		parameters.cache_size = CACHE_SIZE;
		
		// train the SVM
		logger.log("Training the model");
		svm_model model = svm.svm_train(data,parameters);

		// record the model
		logger.log("Recording the model");
		String modelPath = combiner.getSvmModelPath();
		svm.svm_save_model(modelPath,model);
		
		// possibly process and record the voting weights
		logger.log("Possibly recording the voting weights");
		processVoteData(folders);
		
		logger.log("Training over");
		logger.decreaseOffset();
	}

	/**
	 * Trains the SVM on the specified data,
	 * for the specified entity types and SVM parameters.
	 * 
	 * @param folders
	 * 		List of concered articles.
	 * @param c
	 * 		First SVM parameter.
	 * @param gamma
	 * 		Second SVM parameter.
	 * 
	 * @throws IOException
	 * 		Problem while accessing a file. 
	 * @throws SAXException
	 * 		Problem while accessing an entity file. 
	 * @throws ParseException
	 * 		Problem while accessing an entity file. 
	 * @throws ReaderException
	 * 		Problem while accessing a file. 
	 * @throws RecognizerException
	 * 		Problem while applying a NER tool. 
	 * @throws ConverterException 
	 * 		Problem while processing a NER tool performance. 
	 */
	public void process(ArticleList folders, double c, double gamma) throws IOException, SAXException, ParseException, ReaderException, RecognizerException, ConverterException
	{	logger.increaseOffset();
		
		// get the recognizers
		List<AbstractRecognizer> recognizers = combiner.getRecognizers();
		// prepare the data
		logger.log("Preparing the training data");
		svm_problem data = prepareData(recognizers,folders);
		
		// init parameters
		logger.log("Initializing parameters");
		logger.log("Number of features: "+data.x[0].length);
		svm_parameter parameters = initParameters();
		parameters.C = c;				// FYI, default value is 1
		parameters.gamma = gamma;		// FYI, default value is 1/num_features
		logger.log("Use parameters: C="+parameters.C+" gamma="+parameters.gamma);
		parameters.cache_size = CACHE_SIZE;
		
		// train the SVM
		logger.log("Training the model");
		svm_model model = svm.svm_train(data,parameters);

		// record the model
		logger.log("Recording the model");
		String modelPath = combiner.getSvmModelPath();
		svm.svm_save_model(modelPath,model);
		
		// possibly process and record the voting weights
		logger.log("Possibly recording the voting weights");
		processVoteData(folders);
		
		logger.log("Training over");
		logger.decreaseOffset();
	}
}
