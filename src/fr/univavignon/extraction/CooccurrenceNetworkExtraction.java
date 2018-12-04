package fr.univavignon.extraction;

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

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleList;
import fr.univavignon.common.data.entity.AbstractEntity;
import fr.univavignon.common.data.entity.AbstractNamedEntity;
import fr.univavignon.common.data.entity.Entities;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.KnowledgeBase;
import fr.univavignon.common.data.entity.MentionsEntities;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.MentionDate;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.common.tools.corpus.ArticleLists;
import fr.univavignon.common.tools.files.CommonFileNames;
import fr.univavignon.extraction.graph.Graph;
import fr.univavignon.extraction.graph.Link;
import fr.univavignon.extraction.graph.Node;
import fr.univavignon.nerwip.processing.InterfaceLinker;
import fr.univavignon.nerwip.processing.InterfaceProcessor;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.InterfaceResolver;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.combiner.straightcombiner.StraightCombiner;
import fr.univavignon.nerwip.processing.internal.modelless.naiveresolver.NaiveResolver;
import fr.univavignon.nerwip.processing.internal.modelless.wikidatalinker.WikiDataLinker;
import fr.univavignon.retrieval.ArticleRetriever;
import fr.univavignon.retrieval.ReaderException;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;
import fr.univavignon.tools.strings.StringTools;

/**
 * Extract a conceptual network from a corpus
 * and the corresponding entities. The network
 * nodes are entities, whereas the links correspond
 * to co-occurrences (two entities appearing
 * in the same sentence).
 * 
 * @author Vincent Labatut
 */
public class CooccurrenceNetworkExtraction
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
//		extractNetwork(recognizer);
	
		int maxDist = 4;
		InterfaceResolver resolver = new NaiveResolver(recognizer, maxDist);
		resolver.setCacheEnabled(false);
		boolean revision = false;
		InterfaceLinker linker = new WikiDataLinker(resolver, revision);
		linker.setCacheEnabled(false);
		
		extractNetwork(linker);
	}
		
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Node property for mention frequence */
	private final static String PROP_FREQ = "frequence";
	/** Node property for mention type */
	private final static String PROP_TYPE = "type";
	/** Node property for full name */
	private final static String PROP_NAME = "fullname";
	/** Node property for Wikidata id */
	private final static String PROP_WIKI_ID = "wikidataid";
	/** Link property for weight */
	private final static String PROP_WEIGHT = "weight";
	
	/**
	 * Extract a co-occurrence network from a corpus of biographical texts
	 * and the named entities detected in this same corpus. 
	 * <br/>
	 * If the specified processor is simply a recognizer, we compare mention
	 * names. Otherwise, we compare entities, which can help detecting different
	 * surface forms corresponding to the same entity. 
	 * 
	 * @param processor
	 * 		The processor to apply (or previously applied).
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
	private static void extractNetwork(InterfaceProcessor processor) throws ProcessorException, ParseException, SAXException, IOException, ReaderException
	{	logger.log("Extract entity network");
		logger.increaseOffset();
		
		// init graph
		Graph graph = new Graph("Mention co-occurrence graph", false);
		graph.addNodeProperty(PROP_NAME,"string");
		graph.addNodeProperty(PROP_FREQ,"int");
		graph.addNodeProperty(PROP_TYPE,"string");
		graph.addLinkProperty(PROP_WEIGHT,"int");
		String filename = "cooccurrence-mentions-all" + FileNames.EX_GRAPHML;
		
		// node ids
		Map<String,Long> nameToId = new HashMap<String, Long>();
		Map<Long,String> idToName = new HashMap<Long,String>();
		long nextId = 0;
		
		logger.log("Read all article entities");
		logger.increaseOffset();
		ArticleList folders = ArticleLists.getArticleList();
		Map<String,Map<EntityType,Integer>> mainTypes = new HashMap<String, Map<EntityType,Integer>>();
		int i = 0;
		Entities entities = null;
		for(File folder: folders)
		{	logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
			logger.increaseOffset();
			
			// get the article texts
			logger.log("Retrieve the article");
			String name = folder.getName();
			ArticleRetriever retriever = new ArticleRetriever();
			Article article = retriever.process(name);
			String rawText = article.getRawText();
			
			// retrieve the mentions and possibly the corresponding entities
			logger.log("Retrieve the mentions");
			logger.increaseOffset();
			Mentions mentions = null;
			Entities tmpEntities = null;
			if(processor.isLinker())
			{	logger.log("Linker detected");
				InterfaceLinker linker = (InterfaceLinker)processor;
				MentionsEntities me = linker.link(article);
				mentions = me.mentions;
				tmpEntities = me.entities;
			}
			else if(processor.isRecognizer())
			{	logger.log("Recognizer detected");
				InterfaceRecognizer recognizer = (InterfaceRecognizer)processor;
				mentions = recognizer.recognize(article);
			}
			else if(processor.isResolver())
			{	logger.log("Resolver detected");
				InterfaceResolver resolver = (InterfaceResolver)processor;
				MentionsEntities me = resolver.resolve(article);
				mentions = me.mentions;
				tmpEntities = me.entities;
			}
			if(tmpEntities!=null)
			{	graph.addNodeProperty(PROP_WIKI_ID,"string");
				if(entities==null)
				{	entities = tmpEntities;
					graph.setName("Entity co-occurrence graph");
					filename = "cooccurrence-entities-all" + FileNames.EX_GRAPHML;
				}
				else
					entities.unifyEntities(tmpEntities, mentions);
			}
			logger.decreaseOffset();
			
			// process each sentence
			logger.log("Process each sentence");
			List<Integer> sentencePos = StringTools.getSentencePositions(rawText);
			sentencePos.add(rawText.length()); // to mark the end of the last sentence
			int sp = -1;
			for(int ep: sentencePos)
			{	if(sp>=0)
				{	Set<String> conMentions = new TreeSet<String>();
					List<AbstractMention<?>> list = mentions.getMentionsIn(sp, ep);
					for(AbstractMention<?> mention: list)
					{	if(!(mention instanceof MentionDate)) // we don't need the dates
						{	// mention name
							String str;
							if(entities==null)
							{	name = mention.getStringValue();
								Long id = nameToId.get(name);
								if(id==null)
								{	id = nextId;
									nameToId.put(name,id);
									idToName.put(id,name);
									nextId++;
								}
								str = Long.toString(id);
							}
							else
							{	AbstractEntity entity = mention.getEntity();
								long id = entity.getInternalId();
								str = Long.toString(id);
							}
							conMentions.add(str);
							// entity type
							EntityType type = mention.getType();
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
					
					// insert the mentions/entities into the graph
					List<String> connectedEntities = new ArrayList<String>(conMentions);
					int s = connectedEntities.size();
					logger.log("Insert/update "+s+" nodes in the graph");
					for(int j=0;j<connectedEntities.size();j++)
					{	// name
						String entName = connectedEntities.get(j);
						Node node = graph.retrieveNode(entName);
						// occurrences
						node.incrementIntProperty(PROP_FREQ);
					}
					
					// insert the links into the graph
					logger.log("Insert/update "+(s*(s-1)/2)+" links in the graph");
					for(int j=0;j<connectedEntities.size()-1;j++)
					{	String source = connectedEntities.get(j);
						for(int k=j+1;k<connectedEntities.size();k++)
						{	String target = connectedEntities.get(k);
							Link link = graph.retrieveLink(source, target);
							link.incrementIntProperty(PROP_WEIGHT);
						}
					}
				}
			
				sp = ep;
			}
			
			logger.decreaseOffset();
			i++;
		}
		logger.decreaseOffset();
		
		// update the nodes
		for(Node node: graph.getAllNodes())
		{	String name = node.getName();
			
			// setup majority entity types (for mentions, not entities)
			Map<EntityType,Integer> map = mainTypes.get(name);
			EntityType type = getMaxKey(map);
			node.setProperty(PROP_TYPE,type.toString());
			
			// possibly setup the node name
			long id = Long.parseLong(name);
			if(entities==null)
			{	String fullname = idToName.get(id);
				node.setProperty(PROP_NAME, fullname);
			}
			else
			{	AbstractEntity entity = entities.getEntityById(id);
				if(entity instanceof AbstractNamedEntity)
				{	AbstractNamedEntity namedEntity = (AbstractNamedEntity)entity;
					// name
					String fullname = namedEntity.getName();
					node.setProperty(PROP_NAME, fullname);
					// wikidata id
					String wikiId = namedEntity.getExternalId(KnowledgeBase.WIKIDATA_ID);
					if(wikiId!=null)
						node.setProperty(PROP_WIKI_ID, wikiId);
				}
			}
		}
		
		logger.log("Article processing complete.");
		int n = graph.getNodeSize();
		int m = graph.getLinkSize();
		logger.log("Total number of nodes in the graph: "+n+" ("+mainTypes.size()+")");	
		logger.log("Total number of links in the graph: "+m);
		float d = m / (n*(n-1f)/2);
		logger.log("Graph density: "+d);	
		
		logger.log("Export graph as XML");
		String netPath = FileNames.FO_OUTPUT + File.separator + filename;
		File netFile = new File(netPath);
		graph.writeToXml(netFile);
		
		if(entities!=null)
		{	logger.log("Export the unified entity set (to the corpus root)");
			String path = FileNames.FO_OUTPUT + File.separator + CommonFileNames.FI_ENTITY_LIST;
			File entFile = new File(path);
			entities.writeToXml(entFile);
		}
		
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
