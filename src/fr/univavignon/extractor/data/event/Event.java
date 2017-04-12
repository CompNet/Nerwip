package fr.univavignon.extractor.data.event;

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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.nerwip.data.entity.EntityDate;
import fr.univavignon.nerwip.data.entity.EntityFunction;
import fr.univavignon.nerwip.data.entity.EntityLocation;
import fr.univavignon.nerwip.data.entity.EntityMeeting;
import fr.univavignon.nerwip.data.entity.EntityOrganization;
import fr.univavignon.nerwip.data.entity.EntityPerson;
import fr.univavignon.nerwip.data.entity.EntityProduction;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.MentionDate;
import fr.univavignon.nerwip.data.entity.mention.MentionFunction;
import fr.univavignon.nerwip.data.entity.mention.MentionLocation;
import fr.univavignon.nerwip.data.entity.mention.MentionMeeting;
import fr.univavignon.nerwip.data.entity.mention.MentionOrganization;
import fr.univavignon.nerwip.data.entity.mention.MentionPerson;
import fr.univavignon.nerwip.data.entity.mention.MentionProduction;

/**
 * This class represents an event, i.e. a set of related entities.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class Event
{	
	/**
	* Builds an empty event.
	*/
	public Event() 
	{	
		
	}
	
	/**
	* Builds an event based on the specified
	* list of mentions.
	* 
	* @param mentions
	*	Mentions describing the event. 	 
	*/
	public Event(List<AbstractMention<?>> mentions) 
	{	for(AbstractMention<?> mention: mentions)
		{	if(mention instanceof MentionDate)
			{	EntityDate date = (EntityDate)mention.getEntity();
				addDate(date);
			}
			else if(mention instanceof MentionFunction)
			{	EntityFunction function = (EntityFunction)mention.getEntity();
				addFunction(function);
			}
			else if(mention instanceof MentionLocation)
			{	EntityLocation location = (EntityLocation)mention.getEntity();
				addLocation(location);
			}
			else if(mention instanceof MentionMeeting)
			{	EntityMeeting meeting = (EntityMeeting)mention.getEntity();
				addMeeting(meeting);
			}
			else if(mention instanceof MentionOrganization)
			{	EntityOrganization organization = (EntityOrganization)mention.getEntity();
				addOrganization(organization);
			}
			else if(mention instanceof MentionPerson)
			{	EntityPerson person = (EntityPerson)mention.getEntity();
				addPerson(person);
			}
			else if(mention instanceof MentionProduction)
			{	EntityProduction production = (EntityProduction)mention.getEntity();
				addProduction(production);
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
    // PERSONS			/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/** Person type entities */
	private Set<EntityPerson> persons = new TreeSet<EntityPerson>();

	/**
	 * Add a person entity to this event.
	 * If it is already present, it will not be
	 * added another time.
	 * 
	 * @param person
	 * 		The new person entity.
	 */
	public void addPerson(EntityPerson person)
	{	persons.add(person);
	}
	
	/**
	 * Add several person entities to this event.
	 * If they are already present, they will not be
	 * added another time.
	 * 
	 * @param persons
	 * 		The new person entities.
	 */
	public void addPersons(Collection<EntityPerson> persons)
	{	persons.addAll(persons);
	}
	
	/**
	 * Returns the set of person entities for this event..
	 * 
	 * @return
	 * 		The set of person entities for this event, 
	 * 		possibly an empty one.
	 */
	public Set<EntityPerson> getPersons()
	{	return persons;
	}
	
	/////////////////////////////////////////////////////////////////
    // LOCATIONS		/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/** Location type entities */
	private Set<EntityLocation> locations = new TreeSet<EntityLocation>();

	/**
	 * Add a location entity to this event.
	 * If it is already present, it will not be
	 * added another time.
	 * 
	 * @param location
	 * 		The new location entity.
	 */
	public void addLocation(EntityLocation location)
	{	locations.add(location);
	}
	
	/**
	 * Add several location entities to this event.
	 * If they are already present, they will not be
	 * added another time.
	 * 
	 * @param locations
	 * 		The new location entities.
	 */
	public void addLocations(Collection<EntityLocation> locations)
	{	locations.addAll(locations);
	}
	
	/**
	 * Returns the set of location entities for this event..
	 * 
	 * @return
	 * 		The set of location entities for this event, 
	 * 		possibly an empty one.
	 */
	public Set<EntityLocation> getLocations()
	{	return locations;
	}

	/////////////////////////////////////////////////////////////////
    // ORGANIZATIONS	/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/** Organization type entities */
	private Set<EntityOrganization> organizations = new TreeSet<EntityOrganization>();

	/**
	 * Add an organization entity to this event.
	 * If it is already present, it will not be
	 * added another time.
	 * 
	 * @param organization
	 * 		The new organization entity.
	 */
	public void addOrganization(EntityOrganization organization)
	{	organizations.add(organization);
	}
	
	/**
	 * Add several organization entities to this event.
	 * If they are already present, they will not be
	 * added another time.
	 * 
	 * @param organizations
	 * 		The new organization entities.
	 */
	public void addOrganizations(Collection<EntityOrganization> organizations)
	{	organizations.addAll(organizations);
	}
	
	/**
	 * Returns the set of organization entities for this event..
	 * 
	 * @return
	 * 		The set of organization entities for this event, 
	 * 		possibly an empty one.
	 */
	public Set<EntityOrganization> getOrganizations()
	{	return organizations;
	}

	/////////////////////////////////////////////////////////////////
    // FUNCTIONS		/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/** Function type entities */
	private Set<EntityFunction> functions = new TreeSet<EntityFunction>();

	/**
	 * Add a function entity to this event.
	 * If it is already present, it will not be
	 * added another time.
	 * 
	 * @param function
	 * 		The new function entity.
	 */
	public void addFunction(EntityFunction function)
	{	functions.add(function);
	}
	
	/**
	 * Add several function entities to this event.
	 * If they are already present, they will not be
	 * added another time.
	 * 
	 * @param functions
	 * 		The new function entities.
	 */
	public void addFunctions(Collection<EntityFunction> functions)
	{	functions.addAll(functions);
	}
	
	/**
	 * Returns the set of function entities for this event..
	 * 
	 * @return
	 * 		The set of function entities for this event, 
	 * 		possibly an empty one.
	 */
	public Set<EntityFunction> getFunctions()
	{	return functions;
	}

	/////////////////////////////////////////////////////////////////
    // PRODUCTIONS		/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/** Production type entities */
	private Set<EntityProduction> productions = new TreeSet<EntityProduction>();

	/**
	 * Add a production entity to this event.
	 * If it is already present, it will not be
	 * added another time.
	 * 
	 * @param production
	 * 		The new production entity.
	 */
	public void addProduction(EntityProduction production)
	{	productions.add(production);
	}
	
	/**
	 * Add several production entities to this event.
	 * If they are already present, they will not be
	 * added another time.
	 * 
	 * @param productions
	 * 		The new production entities.
	 */
	public void addProductions(Collection<EntityProduction> productions)
	{	productions.addAll(productions);
	}
	
	/**
	 * Returns the set of production entities for this event..
	 * 
	 * @return
	 * 		The set of production entities for this event, 
	 * 		possibly an empty one.
	 */
	public Set<EntityProduction> getProductions()
	{	return productions;
	}

	/////////////////////////////////////////////////////////////////
    // MEETINGS			/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/** Meeting type entities */
	private Set<EntityMeeting> meetings = new TreeSet<EntityMeeting>();

	/**
	 * Add a meeting entity to this event.
	 * If it is already present, it will not be
	 * added another time.
	 * 
	 * @param meeting
	 * 		The new meeting entity.
	 */
	public void addMeeting(EntityMeeting meeting)
	{	meetings.add(meeting);
	}
	
	/**
	 * Add several meeting entities to this event.
	 * If they are already present, they will not be
	 * added another time.
	 * 
	 * @param meetings
	 * 		The new meeting entities.
	 */
	public void addMeetings(Collection<EntityMeeting> meetings)
	{	meetings.addAll(meetings);
	}
	
	/**
	 * Returns the set of meeting entities for this event..
	 * 
	 * @return
	 * 		The set of meeting entities for this event, 
	 * 		possibly an empty one.
	 */
	public Set<EntityMeeting> getMeetings()
	{	return meetings;
	}
	
	/////////////////////////////////////////////////////////////////
    // DATES			/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/** Date type entities */
	private Set<EntityDate> dates = new TreeSet<EntityDate>();

	/**
	 * Add a date entity to this event.
	 * If it is already present, it will not be
	 * added another time.
	 * 
	 * @param date
	 * 		The new date entity.
	 */
	public void addDate(EntityDate date)
	{	dates.add(date);
	}
	
	/**
	 * Add several date entities to this event.
	 * If they are already present, they will not be
	 * added another time.
	 * 
	 * @param dates
	 * 		The new date entities.
	 */
	public void addDates(Collection<EntityDate> dates)
	{	dates.addAll(dates);
	}
	
	/**
	 * Returns the set of date entities for this event..
	 * 
	 * @return
	 * 		The set of date entities for this event, 
	 * 		possibly an empty one.
	 */
	public Set<EntityDate> getDates()
	{	return dates;
	}
	
	/////////////////////////////////////////////////////////////////
    // TEXT				/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = "EVENT(";
		String sep = "";
		if(!persons.isEmpty())
		{	result = result + "PERSONS=" + persons;
			sep = ", ";
		}
		if(!locations.isEmpty())
		{	result = result + sep + "LOCATIONS=" + locations;
			sep = ", ";
		}
		if(!organizations.isEmpty())
		{	result = result + sep + "ORGANIZATIONS=" + organizations;
			sep = ", ";
		}
		if(!functions.isEmpty())
		{	result = result + sep + "FUNCTIONS=" + functions;
			sep = ", ";
		}
		if(!productions.isEmpty())
		{	result = result + sep + "PRODUCTIONS=" + productions;
			sep = ", ";
		}
		if(!meetings.isEmpty())
		{	result = result + sep + "MEETINGS=" + meetings;
			sep = ", ";
		}
		if(!dates.isEmpty())
		{	result = result + sep + "DATES=" + dates;
		}
		result = result + ")";
		return result;
	}
}
