package fr.univavignon.nerwip.tools.ner;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.univavignon.common.data.article.ArticleList;
import fr.univavignon.common.tools.corpus.ArticleLists;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

/**
 * This class contains various methods to manage the files describing
 * NER results. In particular: move some files, remove some annotation 
 * results, etc.
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class ResultsManagement
{	
	/**
	 * Method used to do some punctual processing.
	 * 
	 * @param args
	 * 		None needed.
	 * 
	 * @throws Exception 
	 * 		Whatever.
	 */
	public static void main(String[] args) throws Exception
	{	logger.setName("Results-Management");
		
		ArticleList articles = ArticleLists.getArticleList();
//		ArticleList articles = ArticleLists.getArticleList("training.set.txt");
//		ArticleList articles = ArticleLists.getArticleList("testing.set.txt");

		String targetFolder = "c:/Temp";
		
//		moveNerResults(articles, targetFolder);
		
		boolean loadOnDemand = true;
		InterfaceRecognizer temp[] =
		{	
//			new DateExtractor(),
//			new WikipediaDater(),
				
//			new Illinois(IllinoisModelName.CONLL_MODEL, loadOnDemand, false, false, false),
//			new Illinois(IllinoisModelName.CONLL_MODEL, loadOnDemand, false, false, true),	// LOC, ORG, PERS
//			new Illinois(IllinoisModelName.CONLL_MODEL, loadOnDemand, false, true,  false),
//			new Illinois(IllinoisModelName.CONLL_MODEL, loadOnDemand, false, true,  true),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, loadOnDemand, false, false, false),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, loadOnDemand, false, false, true),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, loadOnDemand, false, true,  false),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, loadOnDemand, false, true,  true),
//			new Illinois(IllinoisModelName.NERWIP_MODEL, loadOnDemand, false, false, false),
//			new Illinois(IllinoisModelName.NERWIP_MODEL, loadOnDemand, false, false, true),
//			new Illinois(IllinoisModelName.NERWIP_MODEL, loadOnDemand, false, true, false),	// LOC, ORG, PERS
//			new Illinois(IllinoisModelName.NERWIP_MODEL, loadOnDemand, false, true, true),
					
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, false, false, false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, false, false, true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, false, true,  false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, false, true,  true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, true,  false, false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, true,  false, true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, true,  true,  false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, false, true,  true,  true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  false, false, false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  false, false, true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  false, true,  false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  false, true,  true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  true,  false, false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  true,  false, true),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  true,  true,  false),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, loadOnDemand, true,  true,  true,  true),	// LOC, ORG, PERS
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, false, false, false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, false, false, true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, false, true,  false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, false, true,  true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, true,  false, false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, true,  false, true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, true,  true,  false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, false, true,  true,  true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  false, false, false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  false, false, true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  false, true,  false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  false, true,  true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  true,  false, false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  true,  false, true),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  true,  true,  false),	// 
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, loadOnDemand, true,  true,  true,  true),	// LOC, ORG, PERS
			
//			new OpenCalais(false, false),
//			new OpenCalais(false, true),
//			new OpenCalais(true,  false),	// (DATE), LOC, ORG, PERS	
//			new OpenCalais(true,  true),	
			
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, false,false),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, false,true),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, true, false),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,loadOnDemand, true, true),	// DATE, LOC, ORG, PERS
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, false,false),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, false,true),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, true, false),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,loadOnDemand, true, true),	// LOC, ORG, PERS
			
//			new Stanford(StanfordModelName.CONLL_MODEL, loadOnDemand, false, false),
//			new Stanford(StanfordModelName.CONLL_MODEL, loadOnDemand, false, true),
//			new Stanford(StanfordModelName.CONLL_MODEL, loadOnDemand, true,  false),
//			new Stanford(StanfordModelName.CONLL_MODEL, loadOnDemand, true,  true),
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, loadOnDemand, false, false),	// LOC, ORG, PERS
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, loadOnDemand, false, true),
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, loadOnDemand, true,  false),
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, loadOnDemand, true,  true),
//			new Stanford(StanfordModelName.MUC_MODEL, loadOnDemand, false, false),		// DATE, LOC, ORG, PERS
//			new Stanford(StanfordModelName.MUC_MODEL, loadOnDemand, false, true),
//			new Stanford(StanfordModelName.MUC_MODEL, loadOnDemand, true,  false),
//			new Stanford(StanfordModelName.MUC_MODEL, loadOnDemand, true,  true),
//			new Stanford(StanfordModelName.NERWIP_MODEL, loadOnDemand, false, false),	// 
//			new Stanford(StanfordModelName.NERWIP_MODEL, loadOnDemand, false, true),	// 
//			new Stanford(StanfordModelName.NERWIP_MODEL, loadOnDemand, true,  false),	// 
//			new Stanford(StanfordModelName.NERWIP_MODEL, loadOnDemand, true,  true),	// LOC, ORG, PERS
			
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, false, false),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, false, true),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, true, false),
//			new VoteCombiner(loadOnDemand, false, VoteMode.UNIFORM, true, true),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED, false, false),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED, false, true),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED, true, false),
//			new VoteCombiner(loadOnDemand, false, VoteMode.WEIGHTED, true, true),
			
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, false, false),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, false, true),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, true, false),
//			new VoteCombiner(loadOnDemand, true, VoteMode.UNIFORM, true, true),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED, false, false),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED, false, true),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED, true, false),
//			new VoteCombiner(loadOnDemand, true, VoteMode.WEIGHTED, true, true),
			
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_UNIFORM),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.MENTION_WEIGHTED),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_SINGLE),
//			new SvmCombiner(loadOnDemand, false, false, CombineMode.CHUNK_PREVIOUS),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_UNIFORM),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.MENTION_WEIGHTED),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_SINGLE),
//			new SvmCombiner(loadOnDemand, false, true, CombineMode.CHUNK_PREVIOUS),

//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_UNIFORM),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.MENTION_WEIGHTED),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_SINGLE),
//			new SvmCombiner(loadOnDemand, true, false, CombineMode.CHUNK_PREVIOUS),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_UNIFORM),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.MENTION_WEIGHTED),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_SINGLE),
//			new SvmCombiner(loadOnDemand, true, true, CombineMode.CHUNK_PREVIOUS),
					
//			new FullCombiner(CombinerName.SVM),
//			new FullCombiner(CombinerName.VOTE)
		};
		List<InterfaceRecognizer> recognizers = Arrays.asList(temp);
//		moveNerResults(recognizers, articles, targetFolder);
		
		removeNerResults(articles);
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// DELETION		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Removes the files produced during NER detection,
	 * but not the article files: raw text,
	 * reference mentions, original html file, property file,
	 * etc. Only the specified articles are processed.
	 * 
	 * @param articles
	 * 		Concerned articles.
	 */
	private static void removeNerResults(ArticleList articles)
	{	logger.log("Deleting NER results");
		logger.increaseOffset();
		
		// clean each article
		logger.log("Processing each article");
		logger.increaseOffset();
		for(File article: articles)
		{	logger.log("Processing article "+article.getName());
			logger.increaseOffset();
			
			// remove subfolders
			File subfolders[] = article.listFiles(FileTools.FILTER_DIRECTORY);
			for(File subfolder: subfolders)
			{	logger.log("Removing "+subfolder.getName());
				FileTools.delete(subfolder);
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
	
		logger.decreaseOffset();
		logger.log("NER results deletion over");
	}
	
	/**
	 * Removes the files produced during NER detection,
	 * but not the article files: raw text,
	 * reference mentions, original html file, property file,
	 * etc. Only the specified articles are processed.
	 * <br/>
	 * Unlike {@link #removeNerResults(ArticleList)}, the focus 
	 * is only on the removal of files produced by the specified 
	 * recognizer.
	 * 
	 * @param recognizer
	 * 		Recognizer whose results files must be removed from
	 * 		the output folder.
	 * @param articles
	 * 		Concerned articles.
	 */
	private static void removeNerResults(InterfaceRecognizer recognizer, ArticleList articles)
	{	List<InterfaceRecognizer> recognizers = new ArrayList<InterfaceRecognizer>();
		recognizers.add(recognizer);
		removeNerResults(recognizers, articles);
	}

	/**
	 * Removes the files produced during NER detection,
	 * but not the article files: raw text,
	 * reference mentions, original html file, property file,
	 * etc. Only the specified articles are processed.
	 * <br/>
	 * Unlike {@link #removeNerResults(ArticleList)}, the focus 
	 * is only on the removal of files produced by all the specified 
	 * recognizers.
	 * 
	 * @param recognizers
	 * 		List of recognizers whose results files must be removed from
	 * 		the output folder.
	 * @param articles
	 * 		Concerned articles.
	 */
	private static void removeNerResults(List<InterfaceRecognizer> recognizers, ArticleList articles)
	{	logger.log("Deleting NER results for "+recognizers.toString());
		logger.increaseOffset();
		
		// clean each article
		logger.log("Processing each article");
		logger.increaseOffset();
		for(File article: articles)
		{	logger.log("Processing article "+article.getName());
			String path = article.getAbsolutePath();
			logger.increaseOffset();
			
			// process each recognizer
			for(InterfaceRecognizer recognizer: recognizers)
			{	String fStr = recognizer.getRecognizerFolder();
				String fPath = path + File.separator + fStr;
				File f = new File(fPath);
				
				if(f.exists())
				{	logger.log("Removing "+f.getName());
					FileTools.delete(f);
				}
				else
					logger.log("Folder "+f.getName()+" does not exist");
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
	
		logger.decreaseOffset();
		logger.log("NER results deletion over");
	}

	/////////////////////////////////////////////////////////////////
	// MOVE			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Moves the files produced during NER detection,
	 * but not the article files: raw text,
	 * reference mentions, original html file, property file,
	 * etc. Only the specified articles are processed. The
	 * files are moved to the specified targeted folder.
	 * 
	 * @param articles
	 * 		Concerned articles.
	 * @param targetPath
	 * 		Path of the destination folder.
	 */
	private static void moveNerResults(ArticleList articles, String targetPath)
	{	logger.log("Moving NER results for all recognizers");
		logger.log("Destination folder: "+targetPath);
		logger.increaseOffset();
		
		// possibly create root destination folder
		File target = new File(targetPath);
		if(!target.exists())
			target.mkdirs();
		
		// clean each article
		logger.log("Processing each article");
		logger.increaseOffset();
		for(File article: articles)
		{	logger.log("Processing article "+article.getName());
			String aStr = article.getName();
			logger.increaseOffset();
			
			// possibly create article destination folder
			String pathArt = targetPath + File.separator +aStr;
			File targetArt = new File(pathArt);
			if(!targetArt.exists())
				targetArt.mkdirs();
			
			// process each subfolder
			File subfolders[] = article.listFiles(FileTools.FILTER_DIRECTORY);
			for(File subfolder: subfolders)
			{	String nStr = subfolder.getName();
				String nPath = pathArt + File.separator + nStr;
				File n = new File(nPath);
			
				logger.log("Moving '"+subfolder.getAbsolutePath()+"' to '"+n.getAbsolutePath()+"'");
				boolean result = FileTools.move(subfolder,n);
//				boolean result = subfolder.renameTo(n);
				if(!result)
					logger.log("WARNING: all files could not be moved");
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
	
		logger.decreaseOffset();
		logger.log("NER results move over");
	}
	
	/**
	 * Moves the files produced during NER detection,
	 * but not the article files: raw text,
	 * reference mentions, original html file, property file,
	 * etc. Only the specified articles are processed. The
	 * files are moved to the specified targeted folder.
	 * <br/>
	 * Unlike {@link #moveNerResults(ArticleList,String)}, the focus 
	 * is only on the processing of files produced by the specified 
	 * recognizers.
	 * 
	 * @param recognizer
	 * 		Recognizer whose results files must be moved from
	 * 		the output folder to the target folder.
	 * @param articles
	 * 		Concerned articles.
	 * @param targetPath
	 * 		Path of the destination folder.
	 */
	private static void moveNerResults(InterfaceRecognizer recognizer, ArticleList articles, String targetPath)
	{	List<InterfaceRecognizer> recognizers = new ArrayList<InterfaceRecognizer>();
		recognizers.add(recognizer);
		moveNerResults(recognizers, articles, targetPath);
	}
	
	/**
	 * Moves the files produced during NER detection,
	 * but not the article files: raw text,
	 * reference mentions, original html file, property file,
	 * etc. Only the specified articles are processed. The
	 * files are moved to the specified targeted folder.
	 * <br/>
	 * Unlike {@link #moveNerResults(ArticleList,String)}, the focus 
	 * is only on the processing of files produced by all the specified 
	 * recognizers.
	 * 
	 * @param recognizers
	 * 		List of recognizers whose results files must be moved from
	 * 		the output folder to the target folder.
	 * @param articles
	 * 		Concerned articles.
	 * @param targetPath
	 * 		Path of the destination folder.
	 */
	private static void moveNerResults(List<InterfaceRecognizer> recognizers, ArticleList articles, String targetPath)
	{	logger.log("Moving NER results for "+recognizers.toString());
		logger.log("Destination folder: "+targetPath);
		logger.increaseOffset();
		
		// possibly create root destination folder
		File target = new File(targetPath);
		if(!target.exists())
			target.mkdirs();
		
		// clean each article
		logger.log("Processing each article");
		logger.increaseOffset();
		for(File article: articles)
		{	logger.log("Processing article "+article.getName());
			String path = article.getAbsolutePath();
			String aStr = article.getName();
			logger.increaseOffset();
			
			// possibly create article destination folder
			String pathArt = targetPath + File.separator +aStr;
			File targetArt = new File(pathArt);
			if(!targetArt.exists())
				targetArt.mkdirs();
			
			// process each recognizer
			for(InterfaceRecognizer recognizer: recognizers)
			{	String fStr = recognizer.getRecognizerFolder();
				String fPath = path + File.separator + fStr;
				File f = new File(fPath);
				
				if(f.exists())
				{	String nPath = pathArt + File.separator + fStr;
					File n = new File(nPath);
					
					logger.log("Moving '"+f.getAbsolutePath()+"' to '"+n.getAbsolutePath()+"'");
					boolean result = FileTools.move(f,n);
//					boolean result = f.renameTo(n);
					if(!result)
						logger.log("WARNING: all files could not be moved");
				}
				else
					logger.log("Folder '"+fStr+"' does not exist for this article");
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
	
		logger.decreaseOffset();
		logger.log("NER results move over");
	}
}
