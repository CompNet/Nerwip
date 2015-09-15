package tr.edu.gsu.nerwip.graphextraction;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.SAXException;

import tr.edu.gsu.extractor.data.Graph;
import tr.edu.gsu.extractor.data.Link;
import tr.edu.gsu.extractor.data.Node;
import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.article.ArticleList;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.data.event.Event;
import tr.edu.gsu.nerwip.eventcomparison.EventComparison;
import tr.edu.gsu.nerwip.eventextraction.EventExtraction;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.combiner.straightcombiner.StraightCombiner;
import tr.edu.gsu.nerwip.retrieval.ArticleRetriever;
import tr.edu.gsu.nerwip.retrieval.reader.ReaderException;
import tr.edu.gsu.nerwip.tools.corpus.ArticleLists;
import tr.edu.gsu.nerwip.tools.dbspotlight.SpotlightTools;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

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

/**
 * Extract an event  network from a corpus
 * and the corresponding entities. The network
 * nodes are person entities, whereas the links correspond
 * to co-participation to the same event.
 * 
 * @author Sabrine Ayachi
 */
public class GraphExtraction {
	
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
   
   
   /**
	 * Extract network wich represents the 
	 * co-participation of two persons to 
	 * the same event from a corpus of biographic 
	 * texts and the named entities detected in this 
	 * same corpus.
	 * 
	 * @param recognizer
	 * 		The NER tool to apply (or previously applied).
 * @throws Exception 
	 */
   private static void extractNetwork(AbstractRecognizer recognizer)  throws Exception
   {
	   logger.log("Extract event network");
		
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
		Entities entities = null;
		Entities allEntities =  new Entities();
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
			entities = recognizer.process(article);
			allEntities.addEntities(entities);
			
			logger.decreaseOffset();
			i++;
			
		}
		int nb = allEntities.getEntities().size();
		List<AbstractEntity<?>> personEntities = new ArrayList<AbstractEntity<?>>();
		List<AbstractEntity<?>> ent = allEntities.getEntities();
	    for(AbstractEntity<?> e: ent)
	    { EntityType entityType = e.getType();
		  String type = entityType.toString();
		  if (type == "PERSON")
			  { 
			  personEntities.add(e);}
		  }
	    int p = personEntities.size();
		logger.log("Inserting nodes in the graph");
			
		for(int j=0;j<p;j++)
		{// name
			AbstractEntity<?> personEntity = personEntities.get(j);
			String entName = personEntity.getStringValue();
			Node node = graph.retrieveNode(entName);
			node.setProperty("Name", entName);
			
		}
		
		// insert the links into the graph
		logger.log("Insert links in the graph"); 
		List<Event> allEventsList = new ArrayList<Event>(); // list of all events of the corpus
		for(File folder: folders)
		{
			logger.log("Process article "+folder.getName()+" ("+(i+1)+"/"+folders.size()+")");
			logger.increaseOffset();
				
			// get the article texts
			logger.log("Retrieve the article");
			String name = folder.getName();
			ArticleRetriever retriever = new ArticleRetriever();
			Article article = retriever.process(name);
			String rawText = article.getRawText();
				
			// retrieve the entities
			logger.log("Retrieve the entities");
			entities = recognizer.process(article);

			List<Event> extractedEvents = EventExtraction.extractEvents(article, entities); 
			allEventsList.addAll(extractedEvents);
			
			logger.decreaseOffset();
			i++;
			
		}
		int nbEvents = allEventsList.size(); // number of events in the corpus
		for (int k=0; k<= nbEvents -1; k++)
			{
			for (int j=0; j<= nbEvents -1; j++)
			{
				if (k!=j && j>k)
				{
					// text is answer
					//String xmlText = SpotlightTools.process(entities, article);
					//String answer = SpotlightTools.disambiguate(xmlText);
					//logger.log("answer = " + answer);
					String answer = SpotlightTools.SpotlightAllCorpus();
					
						
					if (allEventsList.get(k).getPerson().getStringValue() != allEventsList.get(j).getPerson().getStringValue()) 
					{
						double similarity = EventComparison.compareOnePairOfEvents(allEventsList.get(k), allEventsList.get(j), answer);
						logger.log("event " + i + allEventsList.get(k));
						logger.log("event " + j + allEventsList.get(j));
						
				        logger.log("similarity between event " + k + " and event " + j + " = " + similarity);
				        if (similarity >= 0.5)
				        {
				        	String source = allEventsList.get(k).getPerson().getStringValue();
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
   