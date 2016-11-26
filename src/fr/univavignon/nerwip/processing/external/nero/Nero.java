package fr.univavignon.nerwip.processing.external.nero;

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
 * This class acts as an interface with Nero.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * <li>{@code tagger}: {@code CRF}</li>
 * <li>{@code flat}: {@code true}</li>
 * <li>{@code ignorePronouns}: {@code true}</li>
 * <li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * Official Nero website: <a
 * href="https://nero.irisa.fr/">https://nero.irisa.fr/</a>
 * <br/>
 * <b>Warning:</b> it should be noted Nero was originally designed 
 * to treat speech transcriptions, and is therefore not very 
 * robust when handling punctuation. It is also very sensitive to 
 * specific characters like {@code û} or {@code ë}, or combinations 
 * of characters such as newline {@code '\n'} followed by 
 * {@code '"'}. Those should be avoided at all cost in the
 * parsed text, otherwise the delegate will not be able to process 
 * Nero's output.
 * <br/>
 * Nero was tested only on Linux system.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class Nero extends AbstractProcessor implements InterfaceRecognizer
{	
	/**
	 * Builds and sets up an object representing the Nero tool.
	 * 
	 * @param neroTagger
	 * 		NeroTagger used by Nero (CRF or FST).
	 * @param flat
	 * 		Whether mentions can contain other mentions ({@code false}) or
	 * 		are mutually exclusive ({@code true}).
	 * @param ignorePronouns
	 *      Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 *      Whether or not stop words should be excluded from the
	 *      detection.
	 */
	public Nero(NeroTagger neroTagger, boolean flat, boolean ignorePronouns, boolean exclusionOn)
	{	delegateRecognizer = new NeroDelegateRecognizer(this, neroTagger, flat, ignorePronouns, exclusionOn);
	}

	/////////////////////////////////////////////////////////////////
	// NAME 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.NERO;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String getFolder()
	{	String result = null;
		//TODO
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RECOGNIZER 			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Delegate in charge of recognizing entity mentions */
	private NeroDelegateRecognizer delegateRecognizer;
	
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
