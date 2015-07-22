package tr.edu.gsu.nerwip.eventextraction;

import java.util.ArrayList;
import java.util.List;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.event.Event;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.StringTools;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.EntityFunction;
import tr.edu.gsu.nerwip.data.entity.EntityLocation;
import tr.edu.gsu.nerwip.data.entity.EntityOrganization;
import tr.edu.gsu.nerwip.data.entity.EntityPerson;
import tr.edu.gsu.nerwip.data.entity.EntityType;

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
 * Class representing the extraction of events.
 * 
 * @author Vincent Labatut
 */
public class EventExtraction {
	
    /////////////////////////////////////////////////////////////////
    // LOGGING			/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /** Common object used for logging */
    protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

   /**
    * This method extract events from   
    * article.
    * 
    * @param article
    * 		The article to process.
    * @param entities
    *       Entities detected in this article.
    * @return
    * 		Extracted events.
    */
   public static List<Event> extractEvents(Article article, Entities entities)
   {    logger.log("Extracting events");
	    logger.increaseOffset();
	    List<Event> events = new ArrayList<Event>();
	    
	    //processing article
	    String rawText = article.getRawText();
	    //List<Event> events = new ArrayList<Event>();
		//result.add(events);
	  
	    
	    // retrieving the sentence positions
	    List<Integer> sentencePos = StringTools.getSentencePositions(rawText);
	    sentencePos.add(rawText.length()); // to mark the end of the last sentence
	    int sp = -1;
	    int eventNbr = 0;
	    
	    
	    // for each sentence, we get the detected entities
	    for(int ep: sentencePos)
	    {	if(sp>=0)
	    
	    {   logger.log("sp = " + sp);
	        logger.log("ep = " + ep);
	    	List<AbstractEntity<?>> le = entities.getEntitiesIn(sp, ep);
	    	//logger.log("entities = " + entities.getEntitiesIn(0,16));
	    	logger.log("le = " + le.size());
	    	
	 	    List<AbstractEntity<?>> persons = Entities.filterByType(le,EntityType.PERSON);
	 	    logger.log("persons size = " + persons.size());
	 	    //logger.log("persons = " + persons.toString());
	 		List<AbstractEntity<?>> locations = Entities.filterByType(le,EntityType.LOCATION);
	 		logger.log("loc size = " + locations.size());
	 		//logger.log("locations = " + locations.toString());
	 		List<AbstractEntity<?>> orgs = Entities.filterByType(le,EntityType.ORGANIZATION);
	 		//logger.log("org size = " + orgs.size());
	 		List<AbstractEntity<?>> functs = Entities.filterByType(le,EntityType.FUNCTION);
	 		//logger.log("funct size = " + functs.size());
	 		List<AbstractEntity<?>> products = Entities.filterByType(le,EntityType.PRODUCTION);
	 		//logger.log("products size = " + products.size());
	 		List<AbstractEntity<?>> dates = Entities.filterByType(le,EntityType.DATE);
	 		//logger.log("date size = " + dates.size());
	 		// only go on if there is at least one person
			if(!persons.isEmpty())
			{ if(locations.size()>=1 || orgs.size()>=1 || functs.size()>=1 || products.size()>=1)
			{   
				Event event = new Event();
				events.add(event);
				eventNbr++;
				for(AbstractEntity<?> entity: persons)
				{	EntityPerson person = (EntityPerson)entity;
					event.addPerson(person);
				}
				if(locations.size()==1)
				{
					for(AbstractEntity<?> entity: locations)
					{	EntityLocation location = (EntityLocation)entity;
						event.addLocation(location);
					}
				}
				//else {
					//for(AbstractEntity<?> entity: locations)
					
					
				
				if(orgs.size()==1)
				{
					for(AbstractEntity<?> entity: orgs)
					{	EntityOrganization org = (EntityOrganization)entity;
						event.addOrganization(org);
					}
				}
				if(functs.size()==1)
				{
					for(AbstractEntity<?> entity: functs)
					{	EntityFunction funct = (EntityFunction)entity;
						event.addFunction(funct);
					}
				}
				
				if(dates.size()==1)
				{
					for(AbstractEntity<?> entity: dates)
					{	EntityFunction date = (EntityFunction)entity;
						event.addFunction(date);
					}
				}
				
				logger.log("event : " + event.toString());

			}
			else logger.log("no events found in this sentence");
			
			}
	 				}
	    sp = ep;
	    
	    }
	    logger.decreaseOffset();
		logger.log("Event extraction complete: "+eventNbr+" events are detected in " + article.getName());

	    return events;
	    
	 				}
	 			}
	   
	   
	   
   
	
	
	
