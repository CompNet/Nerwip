package tr.edu.gsu.extractor.temp.eventextraction;

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

import tr.edu.gsu.extractor.data.event.Event;
import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.data.entity.mention.AbstractMention;
import tr.edu.gsu.nerwip.data.entity.mention.MentionDate;
import tr.edu.gsu.nerwip.data.entity.mention.MentionFunction;
import tr.edu.gsu.nerwip.data.entity.mention.MentionLocation;
import tr.edu.gsu.nerwip.data.entity.mention.MentionMeeting;
import tr.edu.gsu.nerwip.data.entity.mention.MentionOrganization;
import tr.edu.gsu.nerwip.data.entity.mention.MentionPerson;
import tr.edu.gsu.nerwip.data.entity.mention.MentionProduction;
import tr.edu.gsu.nerwip.data.entity.mention.Mentions;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.StringTools;

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
    * @param mentions
    *       Mentions detected in this article.
    * @return
    * 		Extracted events.
    */
   public static List<Event> extractEvents(Article article, Mentions mentions)
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
	    	{   List<AbstractMention<?>> le = mentions.getMentionsIn(sp, ep);
		    	List<AbstractMention<?>> persons = Mentions.filterByType(le,EntityType.PERSON);
		 		List<AbstractMention<?>> locations = Mentions.filterByType(le,EntityType.LOCATION);
		 		List<AbstractMention<?>> orgs = Mentions.filterByType(le,EntityType.ORGANIZATION);
		 		List<AbstractMention<?>> functs = Mentions.filterByType(le,EntityType.FUNCTION);
		 		List<AbstractMention<?>> products = Mentions.filterByType(le,EntityType.PRODUCTION);
		 		List<AbstractMention<?>> meetings = Mentions.filterByType(le,EntityType.MEETING);
		 		List<AbstractMention<?>> dates = Mentions.filterByType(le,EntityType.DATE);
		 		// only go on if there is at least one person
				if(!persons.isEmpty())
				{ 	if(locations.size()>=1 || orgs.size()>=1 || functs.size()>=1 || products.size()>=1 || meetings.size()>=1)
					{   Event event = new Event();
					    events.add(event);
					    eventNbr++;
					    for(AbstractMention<?> entity: persons)
					    {	MentionPerson person = (MentionPerson)entity;
					    	event.addPerson(person);
					    }
				    
					    if(locations.size()==1)
				    	{	for(AbstractMention<?> entity: locations)
				    		{	MentionLocation location = (MentionLocation)entity;
				    			event.addLocation(location);
			    			}
				    	}
				        else
				        {	for (int i=1; i<=locations.size()-1; i++)
				    		{	Event event1 = new Event();
				    			events.add(event1);
			    				eventNbr++;
		    					for(AbstractMention<?> entity: persons)
		    					{	MentionPerson person = (MentionPerson)entity;
		    						event1.addPerson(person);
	    						}
					       
	    						MentionLocation location1 = (MentionLocation)locations.get(i);
    							event1.addLocation(location1);
    							//logger.log("event1 : " + event1.toString());
    							logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
				    		}
				        }
					    
					    if(orgs.size()==1)
					    {	for(AbstractMention<?> entity: orgs)
				    		{	MentionOrganization org = (MentionOrganization)entity;
				    			event.addOrganization(org);
			    			}
					    }
					    else 
					    {	for(int i=1; i<=orgs.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
				    			eventNbr++;
			    				for(AbstractMention<?> entity: persons)
			    				{	MentionPerson person = (MentionPerson)entity;
			    					event1.addPerson(person);
			    				}
		    					MentionOrganization org1 = (MentionOrganization)orgs.get(i);
	    						event1.addOrganization(org1);
    							//logger.log("event1 : " + event1.toString());
	    						logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
					    	}
					    }
				
					    if(functs.size()==1)
					    {	for(AbstractMention<?> entity: functs)
					    	{	MentionFunction funct = (MentionFunction)entity;
					    		event.addFunction(funct);
					    	}
					    }
					    else 
					    {	for (int i=1; i<=functs.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
				    			eventNbr++;
				    			for(AbstractMention<?> entity: persons)
				    			{	MentionPerson person = (MentionPerson)entity;
				    				event1.addPerson(person);
				    			}
			    				MentionFunction func1 = (MentionFunction)functs.get(i);
			    				event1.addFunction(func1);
			    				//logger.log("event1 : " + event1.toString());
			    				logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
					    	}
					    }
				
					    if(products.size()==1)
					    {	for(AbstractMention<?> entity: products)
					    	{	MentionProduction product = (MentionProduction)entity;
					    		event.addProduction(product);
				    		}
					    }
					    else 
					    {	for(int i=1; i<=products.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
					    		eventNbr++;
					    		for(AbstractMention<?> entity: persons)
					    		{	MentionPerson person = (MentionPerson)entity;
					    			event1.addPerson(person);
					    		}
				    			MentionProduction product1 = (MentionProduction)products.get(i);
				    			event1.addProduction(product1);
				    			//logger.log("event1 : " + event1.toString());
				    			logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
					    	}
					    }

					    if(meetings.size()==1)
					    {	for(AbstractMention<?> entity: products)
					    	{	MentionMeeting meeting = (MentionMeeting)entity;
					    		event.addMeeting(meeting);
					    	}
					    }
					    else 
					    {	for (int i=1; i<=meetings.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
					    		eventNbr++;
					    		for(AbstractMention<?> entity: persons)
					    		{	MentionPerson person = (MentionPerson)entity;
					    			event1.addPerson(person);
					    		}
					    		MentionMeeting meet1 = (MentionMeeting)meetings.get(i);
					    		event1.addMeeting(meet1);
					    		//logger.log("event1 : " + event1.toString());
					    		logger.log(Arrays.asList("Event found for sentence \""+rawText.substring(sp,ep)+"\"",event1.toString()));
					    	}
					    }
				  
					    if(dates.size()==1 | dates.size()==2)
					    {	for(AbstractMention<?> entity: dates)
					    	{	MentionDate date = (MentionDate)entity;
					    		event.addDate(date);
					    	}
					    }
					    else 
					    {	for (int i=2; i<=dates.size()-1; i++)
					    	{	Event event1 = new Event();
					    		events.add(event1);
				    			eventNbr++;
				    			for(AbstractMention<?> entity: persons)
				    			{	MentionPerson person = (MentionPerson)entity;
				    				event1.addPerson(person);
				    			}
				    			MentionDate date1 = (MentionDate)dates.get(i);
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
