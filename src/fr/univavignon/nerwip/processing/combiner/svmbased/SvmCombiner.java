package fr.univavignon.nerwip.processing.combiner.svmbased;

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

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.combiner.AbstractCombinerDelegateRecognizer.SubeeMode;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteCombiner;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.Illinois;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipe;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlp;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.Stanford;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalais;
import fr.univavignon.nerwip.processing.internal.modelless.subee.Subee;

/**
 * This combiner relies on a SVM to perform its combination. 
 * The SVM must have been trained before, in a separate process, 
 * using the class {@link SvmTrainer} on the same settings.
 * <br/>
 * The recognizers handled by this combiner are:
 * <ul>
 * 		<li>Illinois NET (see {@link Illinois})</li>
 * 		<li>LingPipe (see {@link LingPipe})</li>
 * 		<li>OpenCalais (see {@link OpenCalais})</li>
 * 		<li>OpenNLP (see {@link OpenNlp})</li>
 * 		<li>Stanford NER (see {@link Stanford})</li>
 * 		<li>Subee (see {@link Subee})</li>
 * </ul>
 * Various options allow changing the behavior of this combiner:
 * <ul>
 * 		<li>{@code combineMode}: How the combination is performed. It can be
 * 		mention-by-mention or word-by-word. In the former case, the SVM
 * 		cannot handle mention positions, so it is resolved through a
 * 		voting process, not unlike what is performed by {@link VoteCombiner}.
 * 		Various vote processes are proposed, see {@link CombineMode}.</li>
 * 		<li>{@code useCategories}: whether the SVM should use article categories
 * 		as input, to try improving its prediction. It is independent from
 * 		whether categories are used or not during the voting process.</li>
 * 		<li>{@code subeeMode}: whether to use our recognizer {@link Subee}, and if yes,
 * 		how to use it. See {@code SubeeMode}.</li>
 * 		<li>{@code useRecall}: whether or not recall should be used, in the case
 * 		there is some voting involved in the combination.</li>
 * </ul>
 * 
 * @author Vincent Labatut
 */
public class SvmCombiner extends AbstractProcessor implements InterfaceRecognizer
{	
	/**
	 * Builds a new SVM-based combiner.
	 *
	 * @param loadModelOnDemand
	 * 		Whether or not the model should be loaded when initializing this
	 * 		recognizer, or only when necessary. 
	 * @param specific 
	 *		Whether to use the standalone recognizers with their default models 
	 *		({@code false}), or ones specifically trained on our corpus ({@code true}).
	 * @param useCategories 
	 * 		Indicates if categories should be used when combining mentions.
	 * @param combineMode
	 * 		 Indicates how mentions should be combined.
	 * @param subeeMode
	 * 		Indicates how our recognizer {@link Subee} is used (if it's used). 
	 *
	 * @throws ProcessorException
	 * 		Problem while loading some combiner or tokenizer.
	 */
	public SvmCombiner(boolean loadModelOnDemand, boolean specific, boolean useCategories, CombineMode combineMode, SubeeMode subeeMode) throws ProcessorException
	{	delegateRecognizer = new SvmCombinerDelegateRecognizer(this,loadModelOnDemand,specific,useCategories,combineMode,subeeMode);
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.SVMCOMBINER;
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
	private SvmCombinerDelegateRecognizer delegateRecognizer;
	
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
	public SvmCombinerDelegateRecognizer getDelegateRecognizer()
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
