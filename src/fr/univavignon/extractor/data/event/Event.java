package fr.univavignon.extractor.data.event;

import java.util.Set;
import java.util.TreeSet;

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
	/////////////////////////////////////////////////////////////////
    // PERSONS			/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/** Person type entity */
	private MentionPerson person;
	/** Location type entity */
	private MentionLocation location;
	/** Organization type entity */
	private MentionOrganization organization;
	/** Function type entity */
	private MentionFunction function;
	/** Production type entity */
	private MentionProduction production;
	/** Meeting type entity */
	private MentionMeeting meeting;
	/** Date type entity */
	private MentionDate date;
	
	//TODO question : how to compare the entities >> need to distinguish mentions and entities
	
	/**
	* Constructor for an event.
	*/
	public Event() {}
	
	/**
	 * Add person type to an event.
	 * 
	 * @param pers
	 * 		Person type entity.
	 */
	public void addPerson(MentionPerson pers)
	{ person = pers;
	}
	
	/**
	 * Add location type to an event.
	 * 
	 * @param loc
	 * 		Location type entity.
	 */
	public void addLocation(MentionLocation loc)
	{ location = loc;
	}
	
	/**
	 * Add organization type to an event.
	 * 
	 * @param org
	 * 		Organization type entity.
	 */
	public void addOrganization(MentionOrganization org)
	{ organization = org;
	}
	
	/**
	 * Add function type to an event.
	 * 
	 * @param func
	 * 		Function type entity.
	 */
	public void addFunction(MentionFunction func)
	{ function = func;
	}
	
	/**
	 * Add production type to an event.
	 * 
	 * @param prod
	 * 		Production type entity.
	 */
	public void addProduction(MentionProduction prod)
	{ production = prod;
	}
	
	/**
	 * Add meeting type to an event.
	 * 
	 * @param m
	 * 		Meeting type entity.
	 */
	public void addMeeting(MentionMeeting m)
	{ meeting = m;
	}
	
	/**
	 * Add date type to an event.
	 * 
	 * @param d
	 * 		Date type entity.
	 */
	public void addDate(MentionDate d)
	{ date = d;
	}
	
	
	

	/**
	 * Returns the person type of an
	 * event.
	 * 
	 * @return
	 * 		Person Type of the event.
	 */
	public MentionPerson getPerson()
	{	return person;
	}
	
	/**
	 * Returns the person type of an
	 * event.
	 * 
	  * @param person
	 * 		New person type of the event.
	 */
	public void setPerson(MentionPerson person)
	{	this.person = person;
	}
	
	
	/**
	 * Returns the location type of an
	 * event.
	 * 
	 * @return
	 * 		Location Type of the event.
	 */
	public MentionLocation getLocation()
	{	return location;
	}
	
	/**
	 * Returns the location type of an
	 * event.
	 * 
	  * @param location
	 * 		New location type of the event.
	 */
	public void setLocation(MentionLocation location)
	{	this.location = location;
	}
	
	
	/**
	 * Returns the production type of an
	 * event.
	 * 
	 * @return
	 * 		Production Type of the event.
	 */
	public MentionProduction getProduction()
	{	return production;
	}
	
	/**
	 * Returns the production type of an
	 * event.
	 * 
	  * @param production
	 * 		New production type of the event.
	 */
	public void setProduction(MentionProduction production)
	{	this.production = production;
	}
	
	/**
	 * Returns the function type of an
	 * event.
	 * 
	 * @return
	 * 		Function Type of the event.
	 */
	public MentionFunction getFunction()
	{	return function;
	}
	
	/**
	 * Returns the function type of an
	 * event.
	 * 
	  * @param function
	 * 		New function type of the event.
	 */
	public void setFunction(MentionFunction function)
	{	this.function = function;
	}
	
	/**
	 * Returns the organization type of an
	 * event.
	 * 
	 * @return
	 * 		Organization Type of the event.
	 */
	public MentionOrganization getOrganization()
	{	return organization;
	}
	
	/**
	 * Returns the organization type of an
	 * event.
	 * 
	  * @param organization
	 * 		New organization type of the event.
	 */
	public void setOrganization(MentionOrganization organization)
	{	this.organization = organization;
	}
	
	/**
	 * Returns the meeting type of an
	 * event.
	 * 
	 * @return
	 * 		Meeting Type of the event.
	 */
	public MentionMeeting getMeeting()
	{	return meeting;
	}
	
	/**
	 * Returns the meeting type of an
	 * event.
	 * 
	  * @param m
	 * 		New meeting type of the event.
	 */
	public void setMeeting(MentionMeeting m)
	{	this.meeting = m;
	}
	
	/**
	 * Returns the date type of an
	 * event.
	 * 
	 * @return
	 * 		Date Type of the event.
	 */
	public MentionDate getDate()
	{	return date;
	}
	
	/**
	 * Returns the date type of an
	 * event.
	 * 
	  * @param date
	 * 		New date type of the event.
	 */
	public void setSate(MentionDate date)
	{	this.date = date;
	}
	
	@Override
	public String toString()
	{	String result = "EVENT(";
		result = result + "PERSON=" + getPerson();
		result = result + ", LOCATION=" + getLocation();
		result = result + ", ORGANIZATION=" + getOrganization();
		result = result + ", FUNCTION=" + getFunction();
		result = result + ", PRODUCTION=" + getProduction();
		result = result + ", Meeting=" + getMeeting();
		result = result + ", DATE=" + getDate();

		return result;
	}
	
	

	
	}
		
		
	
	
	
	
	
	
	


