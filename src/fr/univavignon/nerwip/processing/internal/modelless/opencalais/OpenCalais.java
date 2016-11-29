package fr.univavignon.nerwip.processing.internal.modelless.opencalais;

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
 * This class acts as an interface with the OpenCalais Web service.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code ignorePronouns}: {@code true}</li>
 * 		<li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * Official OpenCalais website: <a href="http://www.opencalais.com/">http://www.opencalais.com/</a>
 * <br/>
 * <b>Note:</b> if you use this tool, make sure you set up your license key
 * in the file res/misc/key.xml using the exact name "OpenCalais".
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class OpenCalais extends AbstractProcessor implements InterfaceRecognizer
{	// user guide: http://new.opencalais.com/wp-content/uploads/2015/06/Thomson-Reuters-Open-Calais-API-User-Guide-v3.pdf
	
	/**
	 * Builds and sets up an object representing
	 * an OpenCalais recognizer.
	 * 
	 * @param lang
	 * 		Selected language.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop-words should be excluded from the detection.
	 */
	public OpenCalais(OpenCalaisLanguage lang, boolean ignorePronouns, boolean exclusionOn)
	{	delegateRecognizer = new OpenCalaisDelegateRecognizer(this,lang,ignorePronouns,exclusionOn);
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.OPENCALAIS;
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
	// RECOGNIZER	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Delegate in charge of recognizing entity mentions */
	private OpenCalaisDelegateRecognizer delegateRecognizer;
	
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
