package fr.univavignon.nerwip.processing.internal.modelless.spotlight;

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

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.Entities;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.InterfaceResolver;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateLinker;

/**
 * This class acts as an interface with the DBpedia Spotlight Web service, 
 * more precisely its service focusing on linking entities to unique 
 * identifiers.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
class SpotlightDelegateLinker extends AbstractModellessInternalDelegateLinker<List<String>>
{
	/**
	 * Builds and sets up an object representing
	 * the Spotlight recognizer.
	 * 
	 * @param spotlight
	 * 		Linker in charge of this delegate.
	 * @param minConf 
	 * 		Minimal confidence for the recognized mentions (used only in case of recognition).
	 */
	public SpotlightDelegateLinker(Spotlight spotlight, float minConf)
	{	super(spotlight, false);
		
		this.minConf = minConf;
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = linker.getName().toString();
		
		result = result + "_" + "minConf=" + minConf;
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types linked by Spotlight */
	private static final List<EntityType> HANDLED_TYPES = Arrays.asList
	(	EntityType.DATE,
		EntityType.LOCATION,
		EntityType.ORGANIZATION,
		EntityType.PERSON
	);

	@Override
	public List<EntityType> getHandledEntityTypes()
	{	return HANDLED_TYPES;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of languages this recognizer can treat */
	private static final List<ArticleLanguage> HANDLED_LANGUAGES = Arrays.asList
	(	ArticleLanguage.EN,
		ArticleLanguage.FR
	);

	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = HANDLED_LANGUAGES.contains(language);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// MINIMAL CONFIDENCE	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Minimal confidence */
	private float minConf;

	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected List<String> linkEntities(Article article, Mentions mentions, Entities entities) throws ProcessorException
	{	//List<String> result = SpotlightTools.invokeAnnotate(article, minConf);
		List<String> result = SpotlightTools.invokeDisambiguate(article, mentions);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void convert(Article article, Mentions mentions, Entities entities, List<String> data) throws ProcessorException 
	{	ProcessorName linkerName = linker.getName();
		ArticleLanguage language = article.getLanguage();
		InterfaceResolver resolver = linker.getResolver();
		if(resolver==null)
			resolver = (InterfaceResolver)linker;
		InterfaceRecognizer recognizer = resolver.getRecognizer();
		
		// if spotlight is also the recognizer
		if(recognizer==null)
			SpotlightTools.convertSpotlightToNerwip(data, linkerName, mentions, entities, true, language);
		// otherwise, if spotlight is only the resolver+linker
		else
			SpotlightTools.convertSpotlightToNerwip(data, linkerName, mentions, entities, false, language);
		
		// we possibly need to use the delegate resolver to complete the missing entities
		logger.log("Complete mentions/entities (in case of missing entities)");
		((Spotlight)resolver).delegateResolver.completeEntities(mentions,entities);
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
    protected void writeRawResults(Article article, List<String> intRes) throws IOException
    {	InterfaceResolver resolver = linker.getResolver();
		if(resolver==null)
			resolver = (InterfaceResolver)linker;
		InterfaceRecognizer recognizer = resolver.getRecognizer();
    	
    	// number of parts
    	int total;
    	if(recognizer==null)
    		total = intRes.size()/2;
    	else
    		total = intRes.size()/3;
        
    	// build the string
    	StringBuffer string = new StringBuffer();
    	int i = 1;
        Iterator<String> it = intRes.iterator();
        while(it.hasNext())
        {	String originalText = it.next();
        	// original text
			string.append("\n>>> Part " + i + "/" + total + " - Original text <<<\n" + originalText + "\n");
			// converted text
        	if(recognizer==null)
        	{	String convertedText = it.next();
        		string.append("\n>>> Part " + i + "/" + total + " - Converted text <<<\n" + convertedText + "\n");
        	}
        	// spotlight response
        	String spotlightAnswer = it.next();
        	{	try
        		{	// build DOM
					SAXBuilder sb = new SAXBuilder();
					Document doc = sb.build(new StringReader(spotlightAnswer));
					Format format = Format.getPrettyFormat();
					format.setIndent("\t");
					format.setEncoding("UTF-8");
					XMLOutputter xo = new XMLOutputter(format);
					String xmlTxt = xo.outputString(doc);
					
					// add SpotLight format
					string.append("\n>>> Part " + i + "/" + total + " - SpotLight Response <<<\n" + xmlTxt + "\n");
        		}
        		catch (JDOMException e)
        		{	e.printStackTrace();
        		}
        	}
        	i++;
    	}
        
        writeRawResultsStr(article, string.toString());
    }
}
