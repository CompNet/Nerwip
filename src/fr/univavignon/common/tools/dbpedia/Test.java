package fr.univavignon.common.tools.dbpedia;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.article.ArticleList;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.common.tools.corpus.ArticleLists;
import fr.univavignon.common.tools.dbpedia.DbIdTools;
import fr.univavignon.common.tools.dbpedia.DbTypeTools;
import fr.univavignon.common.tools.keys.KeyHandler;
import fr.univavignon.edition.MentionEditor;
import fr.univavignon.extraction.event.Event;
import fr.univavignon.nerwip.evaluation.recognition.RecognitionEvaluator;
import fr.univavignon.nerwip.evaluation.recognition.measures.AbstractRecognitionMeasure;
import fr.univavignon.nerwip.evaluation.recognition.measures.RecognitionIstanbulMeasure;
import fr.univavignon.nerwip.evaluation.recognition.measures.RecognitionLilleMeasure;
import fr.univavignon.nerwip.evaluation.recognition.measures.RecognitionMucMeasure;
import fr.univavignon.nerwip.processing.InterfaceLinker;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.InterfaceResolver;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.combiner.fullcombiner.FullCombiner;
import fr.univavignon.nerwip.processing.combiner.straightcombiner.StraightCombiner;
import fr.univavignon.nerwip.processing.combiner.svmbased.CombineMode;
import fr.univavignon.nerwip.processing.combiner.svmbased.SvmCombiner;
import fr.univavignon.nerwip.processing.combiner.svmbased.SvmTrainer;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteCombiner;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteMode;
import fr.univavignon.nerwip.processing.combiner.votebased.VoteTrainer;
import fr.univavignon.nerwip.processing.external.nero.Nero;
import fr.univavignon.nerwip.processing.external.nero.NeroTagger;
import fr.univavignon.nerwip.processing.external.tagen.TagEn;
import fr.univavignon.nerwip.processing.external.tagen.TagEnModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.heideltime.HeidelTime;
import fr.univavignon.nerwip.processing.internal.modelbased.heideltime.HeidelTimeModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.Illinois;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.IllinoisModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.illinois.IllinoisTrainer;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipe;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipeModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.lingpipe.LingPipeTrainer;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlp;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlpModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.opennlp.OpenNlpTrainer;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.Stanford;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.StanfordModelName;
import fr.univavignon.nerwip.processing.internal.modelbased.stanford.StanfordTrainer;
import fr.univavignon.nerwip.processing.internal.modelless.dateextractor.DateExtractor;
import fr.univavignon.nerwip.processing.internal.modelless.naiveresolver.NaiveResolver;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalais;
import fr.univavignon.nerwip.processing.internal.modelless.opencalais.OpenCalaisLanguage;
import fr.univavignon.nerwip.processing.internal.modelless.opener.OpeNer;
import fr.univavignon.nerwip.processing.internal.modelless.spotlight.Spotlight;
import fr.univavignon.nerwip.processing.internal.modelless.wikidatalinker.WikiDataLinker;
import fr.univavignon.nerwip.processing.internal.modelless.wikipediadater.WikipediaDater;
import fr.univavignon.nerwip.tools.file.NerwipFileNames;
import fr.univavignon.retrieval.ArticleRetriever;
import fr.univavignon.retrieval.wikipedia.WikipediaReader;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;
import fr.univavignon.tools.strings.StringTools;

/**
 * This class is used to launch some processes
 * testing the various features.
 * 
 * @author Vincent Labatut
 */
@SuppressWarnings({ "unused" })
public class Test
{	/**
	 * Basic main function, launches the
	 * required test. Designed to be modified
	 * and launched from Eclipse, no command-line
	 * options.
	 * 
	 * @param args
	 * 		None needed.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	public static void main(String[] args) throws Exception
	{	testDbIdRetriever();
		testDbTypeRetriever();
		testWikiTypeRetriever();
		
		logger.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// RETRIEVAL	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests the feature allowing to automatically
	 * retrieve the DBpedia ids of entities.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testDbIdRetriever() throws Exception
	{	logger.setName("Test-DbIdRetriever");
		logger.log("Start retrieving ids");
		logger.increaseOffset();
		
		
		DbIdTools.getId("Paris");
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve the DBpedia types of entities.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testDbTypeRetriever() throws Exception
	{	logger.setName("Test-DbTypeRetriever");
		logger.log("Start retrieving types");
		logger.increaseOffset();
		
		DbTypeTools.getAllTypes("Barack_Obama");
		
		logger.decreaseOffset();
	}
	
	/**
	 * Tests the feature allowing to automatically
	 * retrieve the Wikidata types of entities.
	 * 
	 * @throws Exception
	 * 		Something went wrong...
	 */
	private static void testWikiTypeRetriever() throws Exception
	{	logger.setName("Test-WikiTypeRetriever");
		logger.log("Start retrieving types");
		logger.increaseOffset();
		
		String title = "Barack%20Obama";
		
		// retrieve all types
		List<String> types = new ArrayList<String>();
		logger.log("Types retrieved for "+title+":");
		logger.increaseOffset();
			logger.log(types);
		logger.decreaseOffset();
		
		logger.log("Type retrieval complete");
		logger.decreaseOffset();
	}
}
