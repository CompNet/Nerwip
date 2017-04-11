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

import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.nerwip.data.entity.EntityDate;
import fr.univavignon.nerwip.data.entity.EntityFunction;
import fr.univavignon.nerwip.data.entity.EntityLocation;
import fr.univavignon.nerwip.data.entity.EntityMeeting;
import fr.univavignon.nerwip.data.entity.EntityOrganization;
import fr.univavignon.nerwip.data.entity.EntityPerson;
import fr.univavignon.nerwip.data.entity.EntityProduction;

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
