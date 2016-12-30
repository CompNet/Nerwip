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

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleLanguage;
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.ProcessorName;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateResolver;

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
 * integrate them in Nerwip.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class SpotlightDelegateResolver extends AbstractModellessInternalDelegateResolver<List<String>>
{
	/**
	 * Builds and sets up an object representing
	 * the Spotlight recognizer.
	 * 
	 * @param spotlight
	 * 		Recognizer in charge of this delegate.
	 * @param minConf 
	 * 		Minimal confidence for the returned entities.
	 */
	public SpotlightDelegateResolver(Spotlight spotlight, float minConf)
	{	super(spotlight);
		
		this.minConf = minConf;
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = resolver.getName().toString();
		
		result = result + "_" + "minConf=" + minConf;
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of entity types resolved by Spotlight */
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
	/** List of languages this resolver can treat */
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
	protected List<String> resolveCoreferences(Article article, Mentions mentions) throws ProcessorException
	{	List<String> result = null;
		InterfaceRecognizer recognizer = resolver.getRecognizer();
		
		// if spotlight is also the recognizer
		if(recognizer==null)
			result = SpotlightTools.invokeAnnotate(article, minConf);
		// otherwise, if spotlight is only the resolver
		else
			result = SpotlightTools.invokeDisambiguate(article, mentions);
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Entities convert(Article article, List<String> data, Mentions mentions) throws ProcessorException 
	{	ProcessorName resolverName = resolver.getName();
		Entities result = new Entities(resolverName);
		InterfaceRecognizer recognizer = resolver.getRecognizer();

		// if spotlight is also the recognizer
		if(recognizer==null)
			SpotlightTools.convertSpotlightToNerwip(data, resolverName, mentions, result, true);
		// otherwise, if spotlight is only the resolver
		else
			SpotlightTools.convertSpotlightToNerwip(data, resolverName, mentions, result, false);

		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    @Override
    protected void writeRawResults(Article article, List<String> intRes) throws IOException
    {	String temp = "";
        int i = 0;
        for(String str: intRes)
        {
        	i++;
        	if(i%2==1)
        		temp = temp + "\n>>> Part " + ((i+1)/2) + "/" + intRes.size()/2 + " - Original text <<<\n" + str + "\n";
        	else
        	{	try
        		{	// build DOM
					SAXBuilder sb = new SAXBuilder();
					Document doc = sb.build(new StringReader(str));
					Format format = Format.getPrettyFormat();
					format.setIndent("\t");
					format.setEncoding("UTF-8");
					XMLOutputter xo = new XMLOutputter(format);
					String xmlTxt = xo.outputString(doc);
					
					// add SpotLight format
					temp = temp + "\n>>> Part " + (i/2) + "/" + intRes.size()/2 + " - SpotLight Response <<<\n" + xmlTxt + "\n";
        		}
        		catch (JDOMException e)
        		{	e.printStackTrace();
        		}
        	}
    	}
        
        writeRawResultsStr(article, temp);
    }
}
