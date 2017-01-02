package fr.univavignon.extractor;

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

import fr.univavignon.extractor.data.graph.Graph;
import fr.univavignon.extractor.data.graph.Link;
import fr.univavignon.extractor.data.graph.Node;
import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleList;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.MentionDate;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.combiner.straightcombiner.StraightCombiner;
import fr.univavignon.nerwip.retrieval.ArticleRetriever;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.tools.corpus.ArticleLists;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.nerwip.tools.string.StringTools;

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
	{	InterfaceRecognizer recognizer = new StraightCombiner();
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
	 * 		The recognizer to apply (or previously applied).
	 * 
	 * @throws ProcessorException
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
	private static void extractNetwork(InterfaceRecognizer recognizer) throws ProcessorException, ParseException, SAXException, IOException, ReaderException
	{	logger.log("Extract entity network");
		
		// init graph
		Graph graph = new Graph("All Mentions", false);
		graph.addNodeProperty("Occurrences","xsd:integer");
		graph.addNodeProperty("Type","xsd:string");
		graph.addLinkProperty("Weight","xsd:integer");
		
		logger.log("Read all article entities");
		logger.increaseOffset();
		ArticleList folders = ArticleLists.getArticleList();
		Map<String,Map<EntityType,Integer>> mainTypes = new HashMap<String, Map<EntityType,Integer>>();
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
			
			// retrieve the mentions
			logger.log("Retrieve the mentions");
			Mentions mentions = recognizer.recognize(article);
			
			// process each sentence
			logger.log("Process each sentence");
			List<Integer> sentencePos = StringTools.getSentencePositions(rawText);
			sentencePos.add(rawText.length()); // to mark the end of the last sentence
			int sp = -1;
			for(int ep: sentencePos)
			{	if(sp>=0)
				{	Set<String> conMentions = new TreeSet<String>();
					List<AbstractMention<?>> list = mentions.getMentionsIn(sp, ep);
					for(AbstractMention<?> entity: list)
					{	if(!(entity instanceof MentionDate)) // we don't need the dates
						{	// entity name
							String str = entity.getStringValue(); // TODO ideally, this would rather be a unique id (after DBpedia is integrated, maybe?)
							conMentions.add(str);
							// entity type
							EntityType type = entity.getType();
							Map<EntityType,Integer> map = mainTypes.get(str);
							if(map==null)
							{	map = new HashMap<EntityType, Integer>();
								mainTypes.put(str,map);
							}
							Integer count = map.get(type);
							if(count==null)
								count = 0;
							count++;
							map.put(type, count);
						}
					}
					List<String> connectedEntities = new ArrayList<String>(conMentions);
					
					// insert the entities into the graph
					int s = connectedEntities.size();
					logger.log("Insert/update "+s+" nodes in the graph");
					for(int j=0;j<connectedEntities.size();j++)
					{	// name
						String entName = connectedEntities.get(j);
						Node node = graph.retrieveNode(entName);
						// occurrences
						node.incrementIntProperty("Occurrences");
					}
					
					// insert the links into the graph
					logger.log("Insert/update "+(s*(s-1)/2)+" links in the graph");
					for(int j=0;j<connectedEntities.size()-1;j++)
					{	String source = connectedEntities.get(j);
						for(int k=j+1;k<connectedEntities.size();k++)
						{	String target = connectedEntities.get(k);
							Link link = graph.retrieveLink(source, target);
							link.incrementIntProperty("Weight");
						}
					}
				}
			
				sp = ep;
			}
			
			logger.decreaseOffset();
			i++;
		}
		
		// setup majority entity types
		for(Node node: graph.getAllNodes())
		{	String name = node.getName();
			Map<EntityType,Integer> map = mainTypes.get(name);
			EntityType type = getMaxKey(map);
			node.setProperty("Type",type.toString());
		}
		
		logger.log("Article processing complete.");
		int n = graph.getNodeSize();
		int m = graph.getLinkSize();
		logger.log("Total number of nodes in the graph: "+n+" ("+mainTypes.size()+")");	
		logger.log("Total number of links in the graph: "+m);
		float d = m / (n*(n-1f)/2);
		logger.log("Graph density: "+d);	
		logger.decreaseOffset();
		
		logger.log("Export graph as XML");
		String netPath = FileNames.FO_OUTPUT + File.separator + "all-entities.graphml";
		File netFile = new File(netPath);
		graph.writeToXml(netFile);
			
		logger.decreaseOffset();
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
