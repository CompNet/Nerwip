package fr.univavignon.nerwip.processing.external.tagen;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
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

/**
 * This class acts as an interface with TagEN.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 	<li>{@code model}: {@link TagEnModelName#MUC_MODEL} for French and 
 * 					   {@link TagEnModelName#WIKI_MODEL} for English</li>
 * 	<li>{@code ignorePronouns}: {@code true}</li>
 * 	<li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class TagEn extends AbstractProcessor implements InterfaceRecognizer
{	
	/**
	 * Builds and sets up an object representing the TagEN tool.
	 * 
	 * @param model
	 *      Model used to perform the mention detection.
	 * @param ignorePronouns
	 *      Whether or not pronouns should be excluded from the detection.
	 * @param ignoreNumbers
	 *      Whether or not numbers should be excluded from the detection.
	 * @param exclusionOn
	 *      Whether or not stop words should be excluded from the detection.
	 */
	public TagEn(TagEnModelName model, boolean ignorePronouns, boolean ignoreNumbers, boolean exclusionOn)
	{	delegateRecognizer = new TagEnDelegateRecognizer(this, model, ignorePronouns, ignoreNumbers, exclusionOn);
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.TAGEN;
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
	private TagEnDelegateRecognizer delegateRecognizer;
	
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
}