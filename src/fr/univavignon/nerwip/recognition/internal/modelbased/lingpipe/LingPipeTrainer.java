package fr.univavignon.nerwip.recognition.internal.modelbased.lingpipe;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.recognition.internal.modelbased.AbstractTrainer;
import fr.univavignon.nerwip.tools.file.FileNames;

/**
 * This class trains the LingPipe NER
 * on our corpus. This results in the creation of new
 * model files, which can then be used to perform NER
 * instead of the default models.  
 * <br/>
 * A part of this code was inspired by/retrieved from
 * the original LingPipe classes. 
 * 
 * @author Vincent Labatut
 */
public class LingPipeTrainer extends AbstractTrainer<Chunking>
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
	public LingPipeTrainer(LingPipeModelName modelName)
	{	this.modelName = modelName;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATA				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the file containing the data */
	private final static String DATA_FILENAME = FileNames.FO_OUTPUT + File.separator + "lingpipe.data.bin";
	/** Map of EntityType to LingPipe type conversion */
	private final static Map<EntityType, String> CONVERSION_MAP = new HashMap<EntityType, String>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put(EntityType.DATE, "DATE");
		CONVERSION_MAP.put(EntityType.LOCATION, "LOCATION");
		CONVERSION_MAP.put(EntityType.ORGANIZATION, "ORGANIZATION");
		CONVERSION_MAP.put(EntityType.PERSON, "PERSON");
	}
	
	@Override
	protected String getDataPath()
	{	return DATA_FILENAME;
	}
	
	@Override
	protected Chunking mergeData(Chunking corpus, Chunking article)
	{	Chunking result = null;
		
		// first time
		if(corpus==null)
			result = article;
		
		// general case
		else
		{	// builds a new chunking object by merging both texts
			String seq = corpus.charSequence() + " " + article.charSequence();
			ChunkingImpl temp = new ChunkingImpl(seq);
			result = temp;
			
			// add each corpus chunk as is
			temp.addAll(corpus.chunkSet());
			
			// add each article chunk with updated start/end
			int offset = corpus.charSequence().length() + 1;
			Set<Chunk> chunks = article.chunkSet();
			for(Chunk chunk: chunks)
			{	int start = chunk.start() + offset;
				int end = chunk.end() + offset;
				String type = chunk.type();
				Chunk copy = ChunkFactory.createChunk(start, end, type); 
				temp.add(copy);
			}
		}
		
		return result;
	}

	@Override
	protected Chunking convertData(Article article, Mentions mentions)
	{	logger.increaseOffset();
		logger.log("Processing article "+article.getName());
		
    	// init chunking object
		String rawText = article.getRawText();
    	ChunkingImpl result = new ChunkingImpl(rawText);
				
    	// add each mention under the form of a Chunk object
    	mentions.sortByPosition();
    	List<AbstractMention<?>> entList = mentions.getMentions();
    	for(AbstractMention<?> mention: entList)
    	{	int start = mention.getStartPos();
    		int end = mention.getEndPos();
    		EntityType type = mention.getType();
    		String typeStr = CONVERSION_MAP.get(type);
    		Chunk chunk = ChunkFactory.createChunk(start, end, typeStr);
    		result.add(chunk);
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
	protected Chunking loadData(File dataFile) throws IOException
	{	logger.increaseOffset();
		ChunkingImpl result	= null;
		
		try
		{	// we just read the previously (manually) serialized Chunking object
			FileInputStream fis = new FileInputStream(dataFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			// first, read the full text
			String text = (String)ois.readObject();
			result = new ChunkingImpl(text);
			
			// then, read each chunk
			int chunkSize = ois.readInt();
			for(int i=0;i<chunkSize;i++)
			{	int start = ois.readInt();
				int end = ois.readInt();
				String type = (String)ois.readObject();
				Chunk chunk = ChunkFactory.createChunk(start, end, type);
				result.add(chunk);
			}
			
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
	protected void recordData(File dataFile, Chunking data) throws IOException
	{	logger.increaseOffset();
		
		try
		{	// we just manually serialize the previously built Chunking object
			FileOutputStream fos = new FileOutputStream(dataFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			// first, write the full text
			String text = data.charSequence().toString();
			oos.writeObject(text);
			
			// then, write each chunk
			Set<Chunk> chunks = data.chunkSet();
			oos.writeInt(chunks.size());
			for(Chunk chunk: chunks)
			{	int start = chunk.start();
				oos.writeInt(start);
				int end = chunk.end();
				oos.writeInt(end);
				String type = chunk.type();
				oos.writeObject(type);
			}
			
			// and finally, close the stream
			oos.close();
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
	protected LingPipeModelName modelName = null;

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
	/** Number of underlying chunkings rescored (default value) */
	private static final int NUM_CHUNKING_RESCORED = 64;
	/** N-gram length for all models (default value) */
	private static final int N_GRAM = 12;
	/** Number of characters in the training and run-time character sets (default value) */
	private static final int NUM_CHARS = 256;
	/** Underlying language-model interpolation ratios (default value) */
	private static final double INTERPOLATION_RATIO = 12;
	/** Whether to smooth tags in underlying chunker (default value) */
	private static final boolean SMOOTH_TAGS = true;
	
	@Override
	protected void configure() throws Exception
	{	logger.increaseOffset();
	
		// nothing to do here
				
		logger.decreaseOffset();
	}
	
	@Override
	protected void train(Chunking data) throws Exception
	{	logger.log("Perform training and record resulting model");
		logger.increaseOffset();
		
		// perform training (source code and values taken from the demo class TrainConll2002)
		TokenizerFactory factory = IndoEuropeanTokenizerFactory.INSTANCE;
		CharLmRescoringChunker chunkerEstimator = new CharLmRescoringChunker(factory, NUM_CHUNKING_RESCORED, N_GRAM, NUM_CHARS, INTERPOLATION_RATIO, SMOOTH_TAGS);
		chunkerEstimator.handle(data);
		
		// record model
		File modelFile = modelName.getModelFile();
		AbstractExternalizable.compileTo(chunkerEstimator,modelFile);
		
		logger.decreaseOffset();
		logger.log("Training and recording complete");
	}
}
