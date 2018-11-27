package fr.univavignon.nerwip.processing.combiner.votebased;

import java.io.IOException;

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

import java.util.List;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.Illinois;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipe;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlp;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.Stanford;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalais;

/**
 * This combiner relies on a vote-based process SVM to perform its combination. 
 * The data required for votting must have been trained calculated before, in a 
 * separate process, using the class {@link VoteTrainer}, using the same settings.
 * <br/>
 * The recognizers handled by this combiner are:
 * <ul>
 * 		<li>Illinois NET (see {@link Illinois})</li>
 * 		<li>LingPipe (see {@link LingPipe})</li>
 * 		<li>OpenCalais (see {@link OpenCalais})</li>
 * 		<li>OpenNLP (see {@link OpenNlp})</li>
 * 		<li>Stanford NER (see {@link Stanford})</li>
 * </ul>
 * Various options allow changing the behavior of this combiner:
 * <ul>
 * 		<li>{@code voteMode}: How the vote is performed: using uniform weights (each 
 * 		recognizer has the same importance) or using performance-related weights
 * 		(a recognizer has more importance if it was good on the test data). 
 * 		See {@link VoteMode}.</li>
 * 		<li>{@code existVote}: whether a vote should be conducted to determine
 * 		the existence of a mention. Otherwise, if at least one recognizer detects
 * 		something, we suppose a mention exists (increases the number of false 
 * 		positves).</li>
 * 		<li>{@code useRecall}: whether or not recall should be used to process weights.</li>
 * </ul>
 * 
 * @author Yasa Akbulut
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public class VoteCombiner extends AbstractProcessor implements InterfaceRecognizer
{	
	/**
	 * Builds a new vote-based combiner.
	 *
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param specific 
	 *		Whether to use the standalone recognizers with their default models 
	 *		({@code false}), or ones specifically trained on our corpus ({@code true}).
	 * @param voteMode 
	 * 		Indicates how recognizers vote.
	 * @param useRecall
	 * 		 Indicates if recall should be used when voting.
	 * @param existVote
	 * 		Indicates if mention existence should be voted. 
	 *
	 * @throws ProcessorException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	public VoteCombiner(boolean loadModelOnDemand, boolean specific, VoteMode voteMode, boolean useRecall, boolean existVote) throws ProcessorException
	{	delegateRecognizer = new VoteCombinerDelegateRecognizer(this,loadModelOnDemand,specific,voteMode,useRecall,existVote);
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.VOTECOMBINER;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER 			/////////////////////////////////////////////
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
	private VoteCombinerDelegateRecognizer delegateRecognizer;
	
	@Override
	public boolean isRecognizer()
	{	return true;
	}
	
	/**
	 * Access to the delegate recognizer is required when training.
	 * 
	 * @return
	 * 		The delegate recognizer of this processor.
	 */
	public VoteCombinerDelegateRecognizer getDelegateRecognizer()
	{	return delegateRecognizer;
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
