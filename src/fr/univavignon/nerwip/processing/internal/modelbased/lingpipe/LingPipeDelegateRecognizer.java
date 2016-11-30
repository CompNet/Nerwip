package fr.univavignon.nerwip.processing.internal.modelbased.lingpipe;

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

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelbased.AbstractModelbasedInternalDelegateRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class acts as an interface with the LingPipe library.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code chunkingMethod}: {@link LingPipeModelName#PREDEFINED_MODEL PREDEFINED_MODEL}</li>
 * 		<li>{@code splitSentences}: {@code true}</li>
 * 		<li>{@code trim}: {@code true}</li>
 * 		<li>{@code ignorePronouns}: {@code true}</li>
 * 		<li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * <br/>
 * Official LingPipe website: <a href="http://alias-i.com/lingpipe">http://alias-i.com/lingpipe/</a>
 * 
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public class LingPipeDelegateRecognizer extends AbstractModelbasedInternalDelegateRecognizer<Chunking, LingPipeModelName>
{
	/**
	 * Builds and sets up an object representing
	 * a LingPipe recognizer.
	 * 
	 * @param lingPipe
	 * 		Recognizer in charge of this delegate.
	 * @param chunkingMethod
	 * 		Method used to detect mentions.
	 * @param loadChunkerOnDemand
	 * 		Whether or not the chunker should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param splitSentences
	 * 		Whether or not the text should be processed one sentence at once.
	 * @param trim
	 * 		Whether or not the beginings and ends of mentions should be 
	 * 		cleaned from any non-letter/digit chars.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop words should be excluded from the detection.
	 * 
	 * @throws ProcessorException 
	 *		Problem while initializing the model.
	 */
	public LingPipeDelegateRecognizer(LingPipe lingPipe, LingPipeModelName chunkingMethod, boolean loadChunkerOnDemand, boolean splitSentences, boolean trim, boolean ignorePronouns, boolean exclusionOn) throws ProcessorException
	{	super(lingPipe,chunkingMethod,loadChunkerOnDemand,trim,ignorePronouns,exclusionOn);
		this.splitSentences = splitSentences;
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
		result = result + "_" + "chunk=" + modelName;
		result = result + "_" + "splitSent=" + splitSentences;
		result = result + "_" + "trim=" + trim;
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void updateHandledEntityTypes()
	{	handledTypes = new ArrayList<EntityType>();
		List<EntityType> temp = modelName.getHandledTypes();
		handledTypes.addAll(temp);
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = modelName.canHandleLanguage(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CHUNKING METHOD 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Whether or not the text should be processed one sentence at once */
    private boolean splitSentences;
    /** Object used to detect sentences */
	private Chunker sentenceChunker;
	/** Object used to detect mentions */
	private Chunker chunker;
   
    @Override
	protected boolean isLoadedModel()
    {	boolean result = chunker!=null;
    	return result;
    }
    
    @Override
	protected void resetModel()
    {	chunker = null;
    }

	@Override
	protected void loadModel() throws ProcessorException
    {	// sentence chunker
		//if(splitSentences)
    	{	logger.log("Build sentence splitter");
			TokenizerFactory tokenizerFactory = new IndoEuropeanTokenizerFactory();
			SentenceModel sentenceModel = new IndoEuropeanSentenceModel();
			sentenceChunker = new SentenceChunker(tokenizerFactory,sentenceModel);
    	}
    	
    	// word chunker
    	try
    	{	chunker = modelName.loadData();
		}
    	catch (ClassNotFoundException e)
    	{	e.printStackTrace();
    		throw new ProcessorException(e.getMessage());
		}
    	catch (IOException e)
    	{	e.printStackTrace();
			throw new ProcessorException(e.getMessage());
		}
    }
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Chunking detectMentions(Article article)
	{	logger.increaseOffset();
		String text = article.getRawText();
		Chunking result = null;
				
		// split sentences (seems to improves performance)
		if(splitSentences)
		{	logger.log("Split text into sentences");
			Chunking sentences = sentenceChunker.chunk(text);
			ChunkingImpl res = new ChunkingImpl(text);
			
			// process each sentence separately
			logger.log("Process each sentence separately");
			logger.increaseOffset();
			for(Chunk sentence: sentences.chunkSet())
			{	// get sentence text
				int startIndex = sentence.start();
				int endIndex = sentence.end();
				logger.log("Process sentence ["+startIndex+","+endIndex+"]");
				String sentenceStr = text.substring(startIndex,endIndex);
				
				// look for mentions
				Chunking words = chunker.chunk(sentenceStr);
				logger.log("Found "+words.chunkSet().size()+" raw words");
				// add them to final result
				for(Chunk word: words.chunkSet())
				{	int start = word.start() + startIndex;
					int end = word.end() + startIndex;
					String type = word.type();
					double score = word.score();
					Chunk temp = ChunkFactory.createChunk(start, end, type, score);
					res.add(temp);
				}
			}
			logger.decreaseOffset();
			result = res;
		}

		// no sentence splitting
		else
		{	result = chunker.chunk(text);
		}
		
		logger.decreaseOffset();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, Chunking chunking) throws ProcessorException
	{	ProcessorName recognizerName = recognizer.getName();
		Mentions result = new Mentions(recognizerName);
		
		String text = chunking.charSequence().toString();
		for(Chunk chunk: chunking.chunkSet())
		{	EntityType type = EntityType.valueOf(chunk.type());
			int startPos = chunk.start();
			int endPos = chunk.end();
			String valueStr = text.substring(startPos,endPos);
			AbstractMention<?,?> mention = AbstractMention.build(type, startPos, endPos, recognizerName, valueStr);
			result.addMention(mention);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, Chunking chunking) throws IOException
	{	// convert to string
		StringBuffer mentions = new StringBuffer();
		for(Chunk chunk: chunking.chunkSet())
			mentions.append(chunk.toString()+"\n");
		String text = mentions.toString();
		
		//record
		super.writeRawResultsStr(article, text);
	}
}
