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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.SAXException;

import fr.univavignon.extractor.data.event.Event;
import fr.univavignon.extractor.data.graph.Graph;
import fr.univavignon.extractor.data.graph.Link;
import fr.univavignon.extractor.data.graph.Node;
import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.article.ArticleList;
import fr.univavignon.nerwip.data.entity.Entities;
import fr.univavignon.nerwip.data.entity.EntityPerson;
import fr.univavignon.nerwip.data.entity.KnowledgeBase;
import fr.univavignon.nerwip.data.entity.MentionsEntities;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.InterfaceLinker;
import fr.univavignon.nerwip.processing.InterfaceRecognizer;
import fr.univavignon.nerwip.processing.InterfaceResolver;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.combiner.straightcombiner.StraightCombiner;
import fr.univavignon.nerwip.processing.internal.modelless.naiveresolver.NaiveResolver;
import fr.univavignon.nerwip.processing.internal.modelless.wikidatalinker.WikiDataLinker;
import fr.univavignon.nerwip.retrieval.ArticleRetriever;
import fr.univavignon.nerwip.retrieval.reader.ReaderException;
import fr.univavignon.nerwip.tools.corpus.ArticleLists;
import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * Extract an interaction network from a corpus
 * and the corresponding entities. The network
 * nodes are persons, whereas the links correspond
 * to co-participations to events.
 * 
 * @author Vincent Labatut
 */
public class CoparticipationNetworkExtraction
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
	{	// set up recognizer
		InterfaceRecognizer recognizer = new StraightCombiner();
		
		// set up resolver
		int maxDist = 4;
		InterfaceResolver resolver = new NaiveResolver(recognizer, maxDist);
		resolver.setCacheEnabled(false);
		
		// set up linker
		boolean revision = true;
		InterfaceLinker linker = new WikiDataLinker(resolver, revision);
		linker.setCacheEnabled(false);
		
		// extract network
		float minSim = 0.8f;
		extractNetwork(linker, minSim);
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
	/** Node property for full name */
	private final static String PROP_NAME = "fullname";
	/** Node property for Wikidata id */
	private final static String PROP_WIKI_ID = "wikidataid";
	/** Link property for weight */
	private final static String PROP_WEIGHT = "weight";
	
	/**
	 * Extract a co-participation network from a corpus of biographical texts
	 * and the entities detected in this same corpus. 
	 * 
	 * @param linker
	 * 		The linker to apply (or previously applied).
	 * @param minSim
	 * 		Threshold to consider that two instance of events actually
	 * 		correspond to the same one. The value must be in in [0;1].
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
	private static void extractNetwork(InterfaceLinker linker, float minSim) throws ProcessorException, ParseException, SAXException, IOException, ReaderException
	{	logger.log("Extract entity network");
		logger.increaseOffset();
		
		// init graph
		Graph graph = new Graph("Person entity co-participation graph", false);
		graph.addNodeProperty(PROP_FREQ,"int");
		graph.addNodeProperty(PROP_NAME,"string");
		graph.addNodeProperty(PROP_WIKI_ID,"string");
		graph.addLinkProperty(PROP_WEIGHT,"double");
		String filename = "coparticipation-entities-persons" + FileNames.EX_GRAPHML;
		
		// init temp list
		List<Event> events = new ArrayList<Event>();
		Set<EntityPerson> persons = new TreeSet<EntityPerson>();
		
		// extract events from each article, and retrieve entities
		logger.log("Read all article entities");
		logger.increaseOffset();
		ArticleList folders = ArticleLists.getArticleList();
		int i = 1;
		Entities entities = null;
		for(File folder: folders)
		{	logger.log("Process article "+folder.getName()+" ("+i+"/"+folders.size()+")");
			logger.increaseOffset();
			
			// get the article text
			logger.log("Retrieve the article");
			String name = folder.getName();
			ArticleRetriever retriever = new ArticleRetriever();
			Article article = retriever.process(name);
			String rawText = article.getRawText();
			
			// retrieve the mentions and their corresponding entities
			logger.log("Retrieve the mentions");
			logger.increaseOffset();
			Mentions mentions = null;
			Entities tmpEntities = null;
			MentionsEntities me = linker.link(article);
			mentions = me.mentions;
			tmpEntities = me.entities;
			// update the global entities
			if(entities==null)
			{	entities = tmpEntities;
			}
			else
				entities.unifyEntities(tmpEntities, mentions);
			logger.decreaseOffset();
			
			// process each sentence
			logger.log("Process each sentence");
			List<Integer> sentencePos = StringTools.getSentencePositions(rawText);
			sentencePos.add(rawText.length()); // to mark the end of the last sentence
			Iterator<Integer> it = sentencePos.iterator();
			int sp = -1;	// beginning of the sentence
			int ep = -1;	// end of the sentence
			do
			{	// update current positions
				if(sp==-1)
					sp = it.next();
				else
					sp = ep;
				ep = it.next();
					
				// build an event from the sentence
				List<AbstractMention<?>> involvedMentions = mentions.getMentionsIn(sp, ep);
				Event event = new Event(involvedMentions);
				// add to event list
				events.add(event);
				
				// get the persons and add to the general list
				Set<EntityPerson> pers = event.getPersons();
				persons.addAll(pers);
			}
			while(it.hasNext());
			
			logger.decreaseOffset();
			i++;
		}
		logger.decreaseOffset();
		
		// insert the persons in the graph (even isolates)	
		logger.log("Create one node for each person in the entity collection (including isolates)");
		for(EntityPerson person: persons)
		{	// create the node
			long id = person.getInternalId();
			String idStr = Long.toString(id);
			Node node = graph.retrieveNode(idStr);
			// set the full name
			String fullname = person.getName();
			node.setProperty(PROP_NAME, fullname);
			// set the Wikidata id
			String wikiId = person.getExternalId(KnowledgeBase.WIKIDATA_ID);
			if(wikiId!=null)
				node.setProperty(PROP_WIKI_ID, wikiId);
		}
		
		// process each event and compare it to the others
		logger.log("Process each event and compare it to the others");
		for(int e1=0;e1<events.size();e1++)
		{	Event event1 = events.get(e1);
			Set<EntityPerson> persons1 = event1.getPersons();
			
			// first, we connect all involved persons
			List<EntityPerson> pers = new ArrayList<EntityPerson>(persons1);
			for(int p1=0;p1<pers.size()-1;p1++)
			{	// get the first id
				EntityPerson person1 = pers.get(p1);
				long id1 = person1.getInternalId();
				String idStr1 = Long.toString(id1);
				
				// process all other nodes
				for(int p2=p1+1;p2<pers.size();p2++)
				{	// get the second id
					EntityPerson person2 = pers.get(p2);
					long id2 = person2.getInternalId();
					String idStr2 = Long.toString(id2);
					// create/increment the link
					Link link = graph.retrieveLink(idStr1, idStr2);
					link.incrementFloatProperty(PROP_WEIGHT, 1f);
				}
			}
			
			// increment the frequences of the concerned nodes
			for(EntityPerson person1: persons1)
			{	long id1 = person1.getInternalId();
				String idStr1 = Long.toString(id1);
				Node node = graph.retrieveNode(idStr1);
				node.incrementIntProperty(PROP_FREQ);
			}
			
			// then we compare to other events and possibly add some other links
			if(e1<events.size()-1)
			for(int e2=e1+1;e2<events.size();e2++)
			{	// similarity between the events
				Event event2 = events.get(e2);
				float sim = event1.processSimilarity(event2);
				// if enough, create links between the concerned persons
				if(sim>minSim)
				{	// process each person in event #1 
					for(EntityPerson person1: persons1)
					{	// get the first id
						long id1 = person1.getInternalId();
						String idStr1 = Long.toString(id1);
						// connect to each person in event #2
						Set<EntityPerson> persons2 = event2.getPersons();
						for(EntityPerson person2: persons2)
						{	// get the second id
							long id2 = person2.getInternalId();
							String idStr2 = Long.toString(id2);
							// create/increment the link
							Link link = graph.retrieveLink(idStr1, idStr2);
							link.incrementFloatProperty(PROP_WEIGHT, sim);
						}
					}
				}
			}
		}
		
		// display summary
		logger.log("Article processing complete.");
		int n = graph.getNodeSize();
		int m = graph.getLinkSize();
		logger.log("Total number of nodes in the graph: "+n);	
		logger.log("Total number of links in the graph: "+m);
		float d = m / (n*(n-1f)/2);
		logger.log("Graph density: "+d);	
		
		// export the graph as a graphml file
		logger.log("Export graph as XML");
		String netPath = FileNames.FO_OUTPUT + File.separator + filename;
		File netFile = new File(netPath);
		graph.writeToXml(netFile);
		
		// export the collection of unified entities 
		if(entities!=null)
		{	logger.log("Export the unified entity set (to the corpus root)");
			String path = FileNames.FO_OUTPUT + File.separator + FileNames.FI_ENTITY_LIST;
			File entFile = new File(path);
			entities.writeToXml(entFile);
		}
		
		logger.decreaseOffset();
	}
}
