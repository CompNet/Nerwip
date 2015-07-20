package tr.edu.gsu.extractor;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011 Yasa Akbulut, Burcu Küpelioğlu & Vincent Labatut
 * Copyright 2012 Burcu Küpelioğlu, Samet Atdağ & Vincent Labatut
 * Copyright 2013 Samet Atdağ & Vincent Labatut
 * Copyright 2014-15 Vincent Labatut
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
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.SAXException;

import tr.edu.gsu.extractor.data.Graph;
import tr.edu.gsu.extractor.data.Link;
import tr.edu.gsu.extractor.data.Node;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.EntityDate;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.tools.string.StringTools;
import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleList;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.combiner.straightcombiner.StraightCombiner;
import tr.edu.gsu.nerwip.retrieval.ArticleRetriever;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.tools.corpus.ArticleLists;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * Extract a conceptual network from a corpus
 * and the corresponding entities. The network
 * nodes are entities, whereas the links correspond
 * to co-occurrences (two entities appearing
 * in the same sentence).
 * 
 * @author Vincent Labatut
 */
public class NetworkExtraction
{	
	/**
	 * Launches the extraction process.
	 * 
	 * @param args
	 * 		None needed.
	 * @throws Exception
	 * 		Problem while extracting.
	 */
	public static void main(String[] args) throws Exception
	{	AbstractRecognizer recognizer = new StraightCombiner();
		extractNetwork(recognizer);
	}
		
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Extract a co-occurrence network from a corpus of biographic texts
	 * and the named entities detected in this same corpus.
	 * 
	 * @param recognizer
	 * 		The NER tool to apply (or previously applied).
	 * 
	 * @throws RecognizerException
	 * 		Problem while retrieving the detected entities.
	 * @throws ParseException
	 * 		Problem while retrieving the article.
	 * @throws SAXException
	 * 		Problem while accessing a file.
	 * @throws IOException
	 * 		Problem while accessing a file.
	 * @throws ReaderException
	 * 		Problem while accessing a file.
	 */
	private static void extractNetwork(AbstractRecognizer recognizer) throws RecognizerException, ParseException, SAXException, IOException, ReaderException
	{	// init graph
		Graph graph = new Graph("All Entities", false);
		graph.addNodeProperty("Occurrences","xsd:integer");
		graph.addNodeProperty("Type","xsd:string");
		graph.addLinkProperty("Weight","xsd:integer");
		
		logger.log("Read all article entities");
		ArticleList folders = ArticleLists.getArticleList();
		int i = 0;
		for(File folder: folders)
		{	logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
			logger.increaseOffset();
			
			// get the article texts
			logger.log("Retrieve the article");
			String name = folder.getName();
			ArticleRetriever retriever = new ArticleRetriever();
			Article article = retriever.process(name);
			String rawText = article.getRawText();
			
			// retrieve the entities
			logger.log("Retrieve the entities");
			Entities entities = recognizer.process(article);
			
			// process each sentence
			Set<String> conEntities = new TreeSet<String>();
			Map<String,Map<EntityType,Integer>> conTypes = new HashMap<String, Map<EntityType,Integer>>();
			List<Integer> sentencePos = StringTools.getSentencePositions(rawText);
			sentencePos.add(rawText.length()); // to mark the end of the last sentence
			int sp = -1;
			for(int ep: sentencePos)
			{	if(sp>=0)
				{	List<AbstractEntity<?>> list = entities.getEntitiesIn(sp, ep);
					for(AbstractEntity<?> entity: list)
					{	if(!(entity instanceof EntityDate)) // we don't need the dates
						{	// entity name
							String str = entity.getStringValue(); // TODO ideally, this would rather be a unique id (after DBpedia is integrated, maybe?)
							conEntities.add(str);
							// entity type
							EntityType type = entity.getType();
							Map<EntityType,Integer> map = conTypes.get(str);
							if(map==null)
							{	map = new HashMap<EntityType, Integer>();
								conTypes.put(name,map);
							}
							Integer count = map.get(type);
							if(count==null)
								count = 0;
							count++;
							map.put(type, count);
						}
					}
				}
				sp = ep;
			}
			List<String> connectedEntities = new ArrayList<String>(conEntities);
			
			// insert the entities into the graph
			for(int j=0;j<connectedEntities.size()-1;j++)
			{	// name
				String entName = connectedEntities.get(j);
				Node node = graph.retrieveNode(entName);
				// occurrences
				node.incrementIntProperty("Occurrences");
				// type
				Map<EntityType,Integer> map = conTypes.get(entName);
				EntityType type = getMaxKey(map);
				node.setProperty("Occurrences",type.toString());
			}
			
			// insert the links into the graph
			for(int j=0;j<connectedEntities.size()-1;j++)
			{	String source = connectedEntities.get(j);
				for(int k=j+1;k<connectedEntities.size();k++)
				{	String target = connectedEntities.get(k);
					Link link = graph.retrieveLink(source, target);
					link.incrementIntProperty("Weight");
				}
			}
			
			logger.decreaseOffset();
			i++;
		}
	}
	
	/**
	 * Returns the key associated to the first
	 * largest value.
	 * 
	 * @param map
	 * 		A map containing comparable values.
	 * @return
	 * 		The key associated to the first largest value.
	 */
	private static <T,U extends Comparable<U>> T getMaxKey(Map<T,U> map)
	{	T result = null;
		U maxValue = null;
		for(Entry<T,U> entry: map.entrySet())
		{	T key = entry.getKey();
			U value = entry.getValue();
			if(maxValue==null)
			{	maxValue = value;
				result = key;
			}
			else
			{	if(maxValue.compareTo(value)<0)
				{	maxValue = value;
					result = key;
				}
			}
		}
		
		return result;
	}
}
