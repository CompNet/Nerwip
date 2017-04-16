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

import java.util.ArrayList;
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
import fr.univavignon.nerwip.tools.time.Period;

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
	
//	/**
//	 * Compare the persons of this event with those of the
//	 * specified one.
//	 * <br/>
//	 * We simply use Jaccard's coefficient (ratio of the 
//	 * cardinalities of the set union to the set intersection).
//	 * 
//	 * @param persons
//	 * 		Persons of the other event.
//	 * @return
//	 * 		A float measuring the similarity between both groups of persons.
//	 */
//	private float processPersonSimilarity(Set<EntityPerson> persons)
//	{	float result = 0;
//		
//		Set<EntityPerson> union = new TreeSet<EntityPerson>(this.persons);
//		union.addAll(persons);
//		float numerator = union.size();
//		
//		Set<EntityPerson> inter = new TreeSet<EntityPerson>(this.persons);
//		union.retainAll(persons);
//		float denominator = inter.size();
//		
//		if(denominator>0)
//			result = numerator / denominator; 
//		return result;
//	}
	
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
	
	/**
	 * Compare the persons of this event with those of the
	 * specified one.
	 * <br/>
	 * We simply use Jaccard's coefficient (ratio of the 
	 * cardinalities of the set union to the set intersection).
	 * 
	 * @param locations
	 * 		Persons of the other event.
	 * @return
	 * 		A float measuring the similarity between both groups of persons.
	 */
	private float processLocationSimilarity(Set<EntityLocation> locations)
	{	float result = 0;
		
		Set<EntityLocation> union = new TreeSet<EntityLocation>(this.locations);
		union.addAll(locations);
		float numerator = union.size();
		
		Set<EntityLocation> inter = new TreeSet<EntityLocation>(this.locations);
		union.retainAll(locations);
		float denominator = inter.size();
		
		if(denominator>0)
			result = numerator / denominator; 
		return result;
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
	
	/**
	 * Compare the organizations of this event with those of the
	 * specified one.
	 * <br/>
	 * We simply use Jaccard's coefficient (ratio of the 
	 * cardinalities of the set union to the set intersection).
	 * 
	 * @param organizations
	 * 		Organizations of the other event.
	 * @return
	 * 		A float measuring the similarity between both groups of organizations.
	 */
	private float processOrganizationSimilarity(Set<EntityOrganization> organizations)
	{	float result = 0;
		
		Set<EntityOrganization> union = new TreeSet<EntityOrganization>(this.organizations);
		union.addAll(organizations);
		float numerator = union.size();
		
		Set<EntityOrganization> inter = new TreeSet<EntityOrganization>(this.organizations);
		union.retainAll(organizations);
		float denominator = inter.size();
		
		if(denominator>0)
			result = numerator / denominator; 
		return result;
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
	
//	/**
//	 * Compare the functions of this event with those of the
//	 * specified one.
//	 * <br/>
//	 * We simply use Jaccard's coefficient (ratio of the 
//	 * cardinalities of the set union to the set intersection).
//	 * 
//	 * @param functions
//	 * 		Functions of the other event.
//	 * @return
//	 * 		A float measuring the similarity between both groups of functions.
//	 */
//	private float processFunctionSimilarity(Set<EntityFunction> functions)
//	{	float result = 0;
//		
//		Set<EntityFunction> union = new TreeSet<EntityFunction>(this.functions);
//		union.addAll(functions);
//		float numerator = union.size();
//		
//		Set<EntityFunction> inter = new TreeSet<EntityFunction>(this.functions);
//		union.retainAll(functions);
//		float denominator = inter.size();
//		
//		if(denominator>0)
//			result = numerator / denominator; 
//		return result;
//	}

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
	
//	/**
//	 * Compare the productions of this event with those of the
//	 * specified one.
//	 * <br/>
//	 * We simply use Jaccard's coefficient (ratio of the 
//	 * cardinalities of the set union to the set intersection).
//	 * 
//	 * @param productions
//	 * 		Productions of the other event.
//	 * @return
//	 * 		A float measuring the similarity between both groups of productions.
//	 */
//	private float processProductionSimilarity(Set<EntityProduction> productions)
//	{	float result = 0;
//		
//		Set<EntityProduction> union = new TreeSet<EntityProduction>(this.productions);
//		union.addAll(productions);
//		float numerator = union.size();
//		
//		Set<EntityProduction> inter = new TreeSet<EntityProduction>(this.productions);
//		union.retainAll(productions);
//		float denominator = inter.size();
//		
//		if(denominator>0)
//			result = numerator / denominator; 
//		return result;
//	}

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
	
//	/**
//	 * Compare the organizations of this event with those of the
//	 * specified one.
//	 * <br/>
//	 * We simply use Jaccard's coefficient (ratio of the 
//	 * cardinalities of the set union to the set intersection).
//	 * 
//	 * @param meetings
//	 * 		Organizations of the other event.
//	 * @return
//	 * 		A float measuring the similarity between both groups of organizations.
//	 */
//	private float processMeetingSimilarity(Set<EntityMeeting> meetings)
//	{	float result = 0;
//		
//		Set<EntityMeeting> union = new TreeSet<EntityMeeting>(this.meetings);
//		union.addAll(meetings);
//		float numerator = union.size();
//		
//		Set<EntityMeeting> inter = new TreeSet<EntityMeeting>(this.meetings);
//		union.retainAll(meetings);
//		float denominator = inter.size();
//		
//		if(denominator>0)
//			result = numerator / denominator; 
//		return result;
//	}
	
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
	
	/**
	 * Compare the dates of this event with those of the
	 * specified one, in terms of overlap. Returns the maximal
	 * proportion of overlap over all pairs of dates taken in
	 * both sets.
	 * 
	 * @param dates
	 * 		Dates of the other event.
	 * @return
	 * 		A float measuring the max overlap over both groups of dates.
	 */
	private float processMaxDateOverlap(Set<EntityDate> dates)
	{	float result = 0;
		
		// get the sets of periods
		List<Period> periods1 = new ArrayList<Period>();
		for(EntityDate entity: this.dates)
		{	Period period = entity.getValue();
			periods1.add(period);
		}
		List<Period> periods2 = new ArrayList<Period>();
		for(EntityDate entity: dates)
		{	Period period = entity.getValue();
			periods2.add(period);
		}
		
		// compare them
		for(int i=0;i<periods1.size()-1;i++)
		{	Period period1 = periods1.get(i);
			for(int j=i+1;j<periods2.size()-1;j++)
			{	Period period2 = periods2.get(j);
				float tmp = period1.processOverlap(period2);
				if(tmp>result)
					result = tmp;
			}
		}
		
		return result;
	}
	
	/**
	 * Compare the dates of this event with those of the
	 * specified one, in terms of separation interval.
	 * Returns a score ranging from 0 (the periods overlap)
	 * to 1 (periods separated by an interval of at least 
	 * three times the shortest period).
	 * 
	 * @param dates
	 * 		Dates of the other event.
	 * @return
	 * 		A float measuring the min interval between both groups of dates.
	 */
	private float processMinDateInterval(Set<EntityDate> dates)
	{	float result = 0;
		
		// get the sets of periods
		List<Period> periods1 = new ArrayList<Period>();
		for(EntityDate entity: this.dates)
		{	Period period = entity.getValue();
			periods1.add(period);
		}
		List<Period> periods2 = new ArrayList<Period>();
		for(EntityDate entity: dates)
		{	Period period = entity.getValue();
			periods2.add(period);
		}
		
		// compare them
		for(int i=0;i<periods1.size()-1;i++)
		{	Period period1 = periods1.get(i);
			for(int j=i+1;j<periods2.size()-1;j++)
			{	Period period2 = periods2.get(j);
				float tmp = period1.processInterval(period2);
				if(tmp<result)
					result = tmp;
			}
		}
		
		// normalize
		float limit = 3;
		result = Math.min(result, limit) / limit;//after three times the shortest period, we consider everything's similar
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
    // SIMILARITY		/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/**
	 * Processes a value between 0 and 1 representing the similarity
	 * between this event and the specified one.
	 * <br/>
	 * Each type of entity is processed separately. Only the types for
	 * which at least one event possesses such entities count.
	 * 
	 * @param event
	 * 		Event of comparison.
	 * @return
	 * 		A float representing the inter-event similarity.
	 */
	public float processSimilarity(Event event)
	{	float result = 0;
		
		/*
		 *                         DATE   FUNC   LOC   MEET   ORG   PERS   PROD
		 * - Production:
		 *   - Principle: two persons connected to the same intellectual object (ex book) are very likely to know each other.  
		 *   - If involved at least once, the production should be similar in both occurrences.
		 *   - There is no meaning in comparing one event with a production and another one without any production.
		 *   - Date helps characterizing the production: compare only if present in both events.
		 *   - Similar production alone is enough to state two events are similar (organizations, persons, places, don't matter).
		 *   - What about: functions, meetings? do we suppose they cannot co-occur?
		 *   
		 * - Functions 
		 *   - Principle: two persons holding the same position around the same time are likely to know each other.
		 *   - But functions don't matter much if different (common spatio temporal elements can be enough)
		 *   - Date should either overlap or be close enough (direct succession?)
		 *   - Place is important (ex. deputé de l'Essone)
		 *   - Persons/organizations do not matter.
		 *   - What about: meetings, productions? no co-occurrence?
		 *   
		 * - Meetings
		 *   - Principle: two persons participating in the same meeting are likely to know each other.
		 *   - Date and location matter a lot.
		 *   - Persons, organizations do not matter.
		 *   - functions, production: no co-occurrence?
		 *  
		 *  - When no function / meeting / production, we focus on spatio temporal elements
		 *    - Dates must overlap, or be close
		 *    - Locations are important.
		 *    - Organizations strengthen the similarity
		 *    - No function, meeting or production.
		 * 
		 */
		
		boolean doSpatioTemporal = true;
		
		// one event contains a production
		if(!productions.isEmpty() || !event.productions.isEmpty())
		{	int factor = 0;
			float sum = 0;
			int norm = 0;
			
			// productions
			Set<EntityProduction> tmp = new TreeSet<EntityProduction>(productions);
			tmp.retainAll(event.productions);
			factor = tmp.size();
			
			// dates
			if(!dates.isEmpty() && !event.dates.isEmpty())
			{	sum = sum + processMaxDateOverlap(event.dates);
				norm++;
			}
			
			result = factor * (sum / norm);
			doSpatioTemporal = false;
		}
		
		// one event contains a function
		if(!functions.isEmpty() || !event.functions.isEmpty())
		{	int factor = 0;
			float sum = 0;
			int norm = 0;
			
			// functions
			Set<EntityFunction> tmp = new TreeSet<EntityFunction>(functions);
			tmp.retainAll(event.functions);
			factor = tmp.size();
			
			// dates
			float dateSim = processMaxDateOverlap(event.dates);
			if(dateSim==0)
				dateSim = 0.5f*(1-processMinDateInterval(event.dates));
			else 
				dateSim = 0.5f*dateSim + 0.5f;
			sum = sum + dateSim;
			norm++;
			
			// locations
			sum = sum + processLocationSimilarity(event.locations);
			norm++;
			
			// overall score
			float score = factor*(sum/norm);
			if(score>result)
				result = score;
			doSpatioTemporal = factor==0;
		}
		
		// one event contains a meeting
		if(!meetings.isEmpty() || !event.meetings.isEmpty())
		{	int factor = 0;
			float sum = 0;
			int norm = 0;
			
			// meetings
			Set<EntityMeeting> tmp = new TreeSet<EntityMeeting>(meetings);
			tmp.retainAll(event.meetings);
			factor = tmp.size();
			
			// dates
			sum = sum + processMaxDateOverlap(event.dates);
			norm++;
			
			// locations
			sum = sum + processLocationSimilarity(event.locations);
			norm++;
			
			// overall score
			float score = factor*(sum/norm);
			if(score>result)
				result = score;
			doSpatioTemporal = false;
		}
		
		// only spatio-temporal events
		if(doSpatioTemporal)
		{	float sum = 0;
			int norm = 0;
		
			// dates
			float dateSim = processMaxDateOverlap(event.dates);
			if(dateSim==0)
				dateSim = 0.5f*(1-processMinDateInterval(event.dates));
			else 
				dateSim = 0.5f*dateSim + 0.5f;
			sum = sum + dateSim;
			norm++;
			
			// locations
			sum = sum + processLocationSimilarity(event.locations);
			norm++;
			
			// organizations
			sum = sum + processOrganizationSimilarity(event.organizations);
			norm++;
			
			float score = sum/norm;
			if(score>result)
				result = score;
		}
		
//		// very basic version
//		int norm = 0;
//		if(!dates.isEmpty() || !event.dates.isEmpty())
//		{	float score = processDateSimilarity(event.dates);
//			result = result + score;
//			norm++;
//		}
//		if(!functions.isEmpty() || !event.functions.isEmpty())
//		{	float score = processFunctionSimilarity(event.functions);
//			result = result + score;
//			norm++;
//		}
//		if(!locations.isEmpty() || !event.locations.isEmpty())
//		{	float score = processLocationSimilarity(event.locations);
//			result = result + score;
//			norm++;
//		}
//		if(!meetings.isEmpty() || !event.meetings.isEmpty())
//		{	float score = processMeetingSimilarity(event.meetings);
//			result = result + score;
//			norm++;
//		}
//		if(!organizations.isEmpty() || !event.organizations.isEmpty())
//		{	float score = processOrganizationSimilarity(event.organizations);
//			result = result + score;
//			norm++;
//		}
//		if(!persons.isEmpty() || !event.persons.isEmpty())
//		{	float score = processPersonSimilarity(event.persons);
//			result = result + score;
//			norm++;
//		}
//		if(!productions.isEmpty() || !event.productions.isEmpty())
//		{	float score = processProductionSimilarity(event.productions);
//			result = result + score;
//			norm++;
//		}
//		result = result / norm;
		
		return result;
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
