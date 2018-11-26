package fr.univavignon.nerwip.processing.internal.modelbased.lingpipe;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;

import java.io.IOException;
import java.util.List;

/**
 * This class acts as a delegate for the mention recognition with 
 * the LingPipe library.
 * It handles mention recognition.
 * See {@link LingPipeDelegateRecognizer} for more details.
 * <br/>
 * Official LingPipe website: <a href="http://alias-i.com/lingpipe">http://alias-i.com/lingpipe/</a>
 * 
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public class LingPipe extends AbstractProcessor implements InterfaceRecognizer
{
	/**
	 * Builds and sets up an object representing
	 * a LingPipe recognizer.
	 * 
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
	public LingPipe(LingPipeModelName chunkingMethod, boolean loadChunkerOnDemand, boolean splitSentences, boolean trim, boolean ignorePronouns, boolean exclusionOn) throws ProcessorException
	{	delegateRecognizer = new LingPipeDelegateRecognizer(this, chunkingMethod, loadChunkerOnDemand, splitSentences, trim, ignorePronouns, exclusionOn);
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.LINGPIPE;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getRecognizerFolder()
	{	String result = delegateRecognizer.getFolder();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RECOGNIZER 			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Delegate in charge of recognizing entity mentions */
	private LingPipeDelegateRecognizer delegateRecognizer;
	
	@Override
	public boolean isRecognizer()
	{	return true;
	}
	
	@Override
	public List<EntityType> getRecognizedEntityTypes()
	{	List<EntityType> result = delegateRecognizer.getHandledEntityTypes();
		return result;
	}

	@Override
	public boolean canRecognizeLanguage(ArticleLanguage language) 
	{	boolean result = delegateRecognizer.canHandleLanguage(language);
		return result;
	}
	
	@Override
	public Mentions recognize(Article article) throws ProcessorException
	{	Mentions result = delegateRecognizer.delegateRecognize(article);
		return result;
	}
	
	@Override
	public void writeRecognizerResults(Article article, Mentions mentions) throws IOException 
	{	delegateRecognizer.writeXmlResults(article, mentions);
	}
	
	/////////////////////////////////////////////////////////////////
	// RESOLVER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean isResolver()
	{	return false;
	}
	
	/////////////////////////////////////////////////////////////////
	// LINKER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public boolean isLinker()
	{	return false;
	}
}
