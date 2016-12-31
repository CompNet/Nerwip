package fr.univavignon.nerwip.processing.internal.modelless.spotlight;

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
import fr.univavignon.nerwip.data.entity.MentionsEntities;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.InterfaceLinker;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.InterfaceResolver;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;

/**
 * This class acts as an interface with the DBpedia Spotlight Web service.
 * <br/>
 * Recommended parameter values:
// * <ul>
// * 		<li>{@code parenSplit}: {@code true}</li>
// * 		<li>{@code ignorePronouns}: {@code true}</li>
// * 		<li>{@code exclusionOn}: {@code false}</li>
// * </ul>
 * <br/>
 * Official Spotlight website: 
 * <a href="http://spotlight.dbpedia.org">
 * http://spotlight.dbpedia.org</a>
 * <br/>
 * TODO Spotlight is available as a set of Java libraries. We could directly 
 * integrate them in Nerwip instead of using the Web service.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class Spotlight extends AbstractProcessor implements InterfaceRecognizer, InterfaceResolver, InterfaceLinker
{
	/**
	 * Builds and sets up an object representing
	 * the Spotlight recognizer, resovler and linker.
	 * 
	 * @param minConf 
	 * 		Minimal confidence for the mentions returned by the recognizer.
	 */
	public Spotlight(float minConf)
	{	// recognize
		delegateRecognizer = new SpotlightDelegateRecognizer(this, minConf);
		this.recognizer = null;
		// resolve
		delegateResolver = new SpotlightDelegateResolver(this, minConf);
		this.resolver = null;
		// link
		delegateLinker = new SpotlightDelegateLinker(this, minConf);
	}
	
	/**
	 * Builds and sets up an object representing
	 * the Spotlight resolver and linker. The specified recognizer
	 * is used in place of Spotlight.
	 * <br/>
	 * <b>Important:</b> the resolver and linker go together, one cannot
	 * invoke the latter separately. So, if the linker method of this class
	 * is later invoked, the provided resolved data will simply be ignored:
	 * only the result of the recognition will be fetched Spotlight (and 
	 * moreover, to the resolver, not the linker). Maybe the linker could
	 * be invoked separately if we would use the Java local version of
	 * Spotlight. 
	 * 
	 * @param recognizer
	 * 		Processor used to recognize the entity mentions.
	 */
	public Spotlight(InterfaceRecognizer recognizer)
	{	// recognize
		delegateRecognizer = null;
		this.recognizer = recognizer;
		// resolve
		delegateResolver = new SpotlightDelegateResolver(this, 0);
		this.resolver = null;
		// link
		delegateLinker = new SpotlightDelegateLinker(this, 0);
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public ProcessorName getName()
	{	return ProcessorName.SPOTLIGHT;
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getRecognizerFolder()
	{	String result = delegateRecognizer.getFolder();
		return result;
	}

	@Override
	public String getResolverFolder() 
	{	String result = delegateResolver.getFolder();
		return result;
	}

	@Override
	public String getLinkerFolder()
	{	String result = delegateLinker.getFolder();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RECOGNIZER	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Recognizer applied before this resolver or linker (can be different from the delegate) */
	protected InterfaceRecognizer recognizer = null;
	/** Delegate in charge of recognizing entity mentions */
	protected SpotlightDelegateRecognizer delegateRecognizer;
	
	@Override
	public InterfaceRecognizer getRecognizer()
	{	return recognizer;
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

	/////////////////////////////////////////////////////////////////
	// RESOLVER		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Resolver applied before this linker (can be different from the delegate) */ //TODO actually: with spotlight, the resolver and linker must be Spotlight. 
	protected InterfaceResolver resolver = null;	//TODO this must be removed in the case of spotlight (keep for now for latter copy to another tool class)
	/** Delegate in charge of recognizing entity mentions */
	protected SpotlightDelegateResolver delegateResolver;

	@Override
	public InterfaceResolver getResolver()
	{	return resolver;
	}
	
	@Override
	public List<EntityType> getResolvedEntityTypes()
	{	List<EntityType> result = delegateResolver.getHandledEntityTypes();
		return result;
	}

	@Override
	public boolean canResolveLanguage(ArticleLanguage language) 
	{	boolean result = delegateResolver.canHandleLanguage(language);
		return result;
	}
	
	@Override
	public MentionsEntities resolve(Article article) throws ProcessorException
	{	MentionsEntities result = delegateResolver.delegateResolve(article);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// LINKER		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Delegate in charge of recognizing entity mentions */
	protected SpotlightDelegateLinker delegateLinker;
	
	@Override
	public List<EntityType> getLinkedEntityTypes()
	{	List<EntityType> result = delegateLinker.getHandledEntityTypes();
		return result;
	}

	@Override
	public boolean canLinkLanguage(ArticleLanguage language) 
	{	boolean result = delegateLinker.canHandleLanguage(language);
		return result;
	}
	
	@Override
	public MentionsEntities link(Article article) throws ProcessorException
	{	MentionsEntities result = delegateLinker.delegateLink(article);
		return result;
	}
}
