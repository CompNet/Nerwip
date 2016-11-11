package tr.edu.gsu.nerwip.eventextraction;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.event.Event;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.StringTools;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.EntityDate;
import tr.edu.gsu.nerwip.data.entity.EntityFunction;
import tr.edu.gsu.nerwip.data.entity.EntityLocation;
import tr.edu.gsu.nerwip.data.entity.EntityMeeting;
import tr.edu.gsu.nerwip.data.entity.EntityOrganization;
import tr.edu.gsu.nerwip.data.entity.EntityPerson;
import tr.edu.gsu.nerwip.data.entity.EntityProduction;
import tr.edu.gsu.nerwip.data.entity.EntityType;

/**
 * Class representing the extraction of events.
 * 
 * @author Sabrine Ayachi
 * 
 */
public class EventExtraction
{	
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
	    
	    // retrieving the sentence positions
	    List<Integer> sentencePos = StringTools.getSentencePositions(rawText);
	    sentencePos.add(rawText.length()); // to mark the end of the last sentence
	    int sp = -1;
	    int eventNbr = 0;
	    
	    // for each sentence, we get the detected entities
	    for(int ep: sentencePos)
	    {	if(sp>=0)
	    	{   List<AbstractEntity<?>> le = entities.getEntitiesIn(sp, ep);
		    	List<AbstractEntity<?>> persons = Entities.filterByType(le,EntityType.PERSON);
		 		List<AbstractEntity<?>> locations = Entities.filterByType(le,EntityType.LOCATION);
		 		List<AbstractEntity<?>> orgs = Entities.filterByType(le,EntityType.ORGANIZATION);
		 		List<AbstractEntity<?>> functs = Entities.filterByType(le,EntityType.FUNCTION);
		 		List<AbstractEntity<?>> products = Entities.filterByType(le,EntityType.PRODUCTION);
		 		List<AbstractEntity<?>> meetings = Entities.filterByType(le,EntityType.MEETING);
		 		List<AbstractEntity<?>> dates = Entities.filterByType(le,EntityType.DATE);
		 		// only go on if there is at least one person
				if(!persons.isEmpty())
				{ 	if(locations.size()>=1 || orgs.size()>=1 || functs.size()>=1 || products.size()>=1 || meetings.size()>=1)
					{   Event event = new Event();
					    events.add(event);
					    eventNbr++;
					    for(AbstractEntity<?> entity: persons)
					    {	EntityPerson person = (EntityPerson)entity;
					    	event.addPerson(person);
					    }
				    
					    if(locations.size()==1)
				    	{	for(AbstractEntity<?> entity: locations)
				    		{	EntityLocation location = (EntityLocation)entity;
				    			event.addLocation(location);
			    			}
				    	}
				        else
				        {	for (int i=1; i<=locations.size()-1; i++)
				    		{	Event event1 = new Event();
				    			events.add(event1);
			    				eventNbr++;
		    					for(AbstractEntity<?> entity: persons)
		    					{	EntityPerson person = (EntityPerson)entity;
		    						event1.addPerson(person);
	    						}
					       
	    						EntityLocation location1 = (EntityLocation)locations.get(i);
    							event1.addLocation(location1);
    							//logger.log("event1 : " + event1.toString());
    							logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
				    		}
				        }
					    
					    if(orgs.size()==1)
					    {	for(AbstractEntity<?> entity: orgs)
				    		{	EntityOrganization org = (EntityOrganization)entity;
				    			event.addOrganization(org);
			    			}
					    }
					    else 
					    {	for(int i=1; i<=orgs.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
				    			eventNbr++;
			    				for(AbstractEntity<?> entity: persons)
			    				{	EntityPerson person = (EntityPerson)entity;
			    					event1.addPerson(person);
			    				}
		    					EntityOrganization org1 = (EntityOrganization)orgs.get(i);
	    						event1.addOrganization(org1);
    							//logger.log("event1 : " + event1.toString());
	    						logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
					    	}
					    }
				
					    if(functs.size()==1)
					    {	for(AbstractEntity<?> entity: functs)
					    	{	EntityFunction funct = (EntityFunction)entity;
					    		event.addFunction(funct);
					    	}
					    }
					    else 
					    {	for (int i=1; i<=functs.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
				    			eventNbr++;
				    			for(AbstractEntity<?> entity: persons)
				    			{	EntityPerson person = (EntityPerson)entity;
				    				event1.addPerson(person);
				    			}
			    				EntityFunction func1 = (EntityFunction)functs.get(i);
			    				event1.addFunction(func1);
			    				//logger.log("event1 : " + event1.toString());
			    				logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
					    	}
					    }
				
					    if(products.size()==1)
					    {	for(AbstractEntity<?> entity: products)
					    	{	EntityProduction product = (EntityProduction)entity;
					    		event.addProduction(product);
				    		}
					    }
					    else 
					    {	for(int i=1; i<=products.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
					    		eventNbr++;
					    		for(AbstractEntity<?> entity: persons)
					    		{	EntityPerson person = (EntityPerson)entity;
					    			event1.addPerson(person);
					    		}
				    			EntityProduction product1 = (EntityProduction)products.get(i);
				    			event1.addProduction(product1);
				    			//logger.log("event1 : " + event1.toString());
				    			logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
					    	}
					    }

					    if(meetings.size()==1)
					    {	for(AbstractEntity<?> entity: products)
					    	{	EntityMeeting meeting = (EntityMeeting)entity;
					    		event.addMeeting(meeting);
					    	}
					    }
					    else 
					    {	for (int i=1; i<=meetings.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
					    		eventNbr++;
					    		for(AbstractEntity<?> entity: persons)
					    		{	EntityPerson person = (EntityPerson)entity;
					    			event1.addPerson(person);
					    		}
					    		EntityMeeting meet1 = (EntityMeeting)meetings.get(i);
					    		event1.addMeeting(meet1);
					    		//logger.log("event1 : " + event1.toString());
					    		logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
					    	}
					    }
				  
					    if(dates.size()==1 | dates.size()==2)
					    {	for(AbstractEntity<?> entity: dates)
					    	{	EntityDate date = (EntityDate)entity;
					    		event.addDate(date);
					    	}
					    }
					    else 
					    {	for (int i=2; i<=dates.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
				    			eventNbr++;
				    			for(AbstractEntity<?> entity: persons)
				    			{	EntityPerson person = (EntityPerson)entity;
				    				event1.addPerson(person);
				    			}
				    			EntityDate date1 = (EntityDate)dates.get(i);
				    			event1.addDate(date1);
				    			//logger.log("event1 : " + event1.toString());
				    			logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
					    	}
					    }

					    logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event.toString()));
					}
					else
						logger.log(Arrays.asList("No event found for sentence \""+rawText.substring(sp,ep)));
				}
	    	}
	    	
	    	sp = ep;
	    }
	    logger.decreaseOffset();
	    logger.log("Event extraction complete: "+eventNbr+" events are detected in " + article.getName());
	    
	    return events;
   }
}
