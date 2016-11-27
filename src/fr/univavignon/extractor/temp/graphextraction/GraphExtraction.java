package fr.univavignon.extractor.temp.graphextraction;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.univavignon.extractor.data.event.Event;
import fr.univavignon.extractor.data.graph.Graph;
import fr.univavignon.extractor.data.graph.Link;
import fr.univavignon.extractor.data.graph.Node;
import fr.univavignon.extractor.temp.eventcomparison.EventComparison;
import fr.univavignon.extractor.temp.eventextraction.EventExtraction;
import fr.univavignon.extractor.temp.tools.dbspotlight.SpotlightTools;
import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleList;
import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.AbstractProcessor;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.combiner.straightcombiner.StraightCombiner;
import fr.univavignon.nerwip.retrieval.ArticleRetriever;
import fr.univavignon.nerwip.tools.corpus.ArticleLists;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * Extract an event  network from a corpus
 * and the corresponding entities. The network
 * nodes are person entities, whereas the links correspond
 * to co-participation to the same event.
 * 
 * @author Sabrine Ayachi
 */
public class GraphExtraction
{	/**
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
   
   
   /**
	 * Extract network wich represents the 
	 * co-participation of two persons to 
	 * the same event from a corpus of biographic 
	 * texts and the named entities detected in this 
	 * same corpus.
	 * 
	 * @param recognizer
	 * 		The recognizer to apply (or previously applied).
	 * @throws Exception 
	 */
   private static void extractNetwork(InterfaceRecognizer recognizer)  throws Exception
   {	logger.log("Extract event network");
		
		//init graph
		Graph graph = new Graph("Entities_Events", false);
		graph.addNodeProperty("Name","xsd:string");
		graph.addLinkProperty("Weight","xsd:double");
		
		logger.log("Read all article entities");
		logger.increaseOffset();
		ArticleList folders = ArticleLists.getArticleList();
		Map<String,Map<EntityType,Integer>> mainTypes = new HashMap<String, Map<EntityType,Integer>>();
		int i = 0;
		int nbr = 0;
		Mentions mentions = null;
		Mentions allEntities =  new Mentions();
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
			mentions = recognizer.recognize(article);
			allEntities.addMentions(mentions);
			
			logger.decreaseOffset();
			i++;
			
		}
		int nb = allEntities.getMentions().size();
		List<AbstractMention<?>> personEntities = new ArrayList<AbstractMention<?>>();
		List<AbstractMention<?>> ent = allEntities.getMentions();
	    for(AbstractMention<?> e: ent)
	    {	EntityType entityType = e.getType();
	    	String type = entityType.toString();
    		if (type == "PERSON")
    			personEntities.add(e);
		}
    	int p = personEntities.size();
    	logger.log("Inserting nodes in the graph");
		
    	for(int j=0;j<p;j++)
    	{	// name
    		AbstractMention<?> personEntity = personEntities.get(j);
    		String entName = personEntity.getStringValue();
    		Node node = graph.retrieveNode(entName);
    		node.setProperty("Name", entName);
    	}
    	
    	// insert the links into the graph
    	logger.log("Insert links in the graph"); 
    	List<Event> allEventsList = new ArrayList<Event>(); // list of all events of the corpus
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
			mentions = recognizer.recognize(article);
			
			List<Event> extractedEvents = EventExtraction.extractEvents(article, mentions); 
			allEventsList.addAll(extractedEvents);
		
			logger.decreaseOffset();
			i++;
    	}
    	
    	int nbEvents = allEventsList.size(); // number of events in the corpus
    	for (int k=0; k<= nbEvents -1; k++)
		{	for (int j=0; j<= nbEvents -1; j++)
			{	if (k!=j && j>k)
				{	// text is answer
					//String xmlText = SpotlightTools.process(entities, article);
					//String answer = SpotlightTools.disambiguate(xmlText);
					//logger.log("answer = " + answer);
					String answer = SpotlightTools.SpotlightAllCorpus();
					
					if (allEventsList.get(k).getPerson().getStringValue() != allEventsList.get(j).getPerson().getStringValue()) 
					{	double similarity = EventComparison.compareOnePairOfEvents(allEventsList.get(k), allEventsList.get(j), answer);
						logger.log("event " + i + allEventsList.get(k));
						logger.log("event " + j + allEventsList.get(j));
						
				        logger.log("similarity between event " + k + " and event " + j + " = " + similarity);
				        if (similarity >= 0.5)
				        {	String source = allEventsList.get(k).getPerson().getStringValue();
			        		String target = allEventsList.get(j).getPerson().getStringValue();
			        		Link link = graph.retrieveLink(source, target);
			        		link.incrementIntProperty("Weight", similarity);
			        		nbr ++;
				        }
					}
				}
			}
		}
    	
		int nbr1 = graph.getLinkSize();
		logger.log("nbr1 = " + nbr1);
		logger.log(nbr + "links are inserted");
		logger.log("Article processing complete.");
		int n = graph.getNodeSize();
		int m = graph.getLinkSize();
		logger.log("Total number of nodes in the graph: "+n+" ("+mainTypes.size()+")");	
		logger.log("Total number of links in the graph: "+m);
		float d = m / (n*(n-1f)/2);
		logger.log("Graph density: "+d);	
		logger.decreaseOffset();
				
		logger.log("Export graph as XML");
		String netPath = FileNames.FO_OUTPUT + File.separator + "graph-events.graphml";
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
   