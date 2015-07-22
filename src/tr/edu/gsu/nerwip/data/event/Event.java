package tr.edu.gsu.nerwip.data.event;

import tr.edu.gsu.nerwip.data.entity.EntityFunction;
import tr.edu.gsu.nerwip.data.entity.EntityLocation;
import tr.edu.gsu.nerwip.data.entity.EntityOrganization;
import tr.edu.gsu.nerwip.data.entity.EntityPerson;
import tr.edu.gsu.nerwip.data.entity.EntityProduction;
import tr.edu.gsu.nerwip.data.entity.EntityDate;


/**
 * This class represents an event.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class Event {
	
	 
	
    /////////////////////////////////////////////////////////////////
    // ATTRIBUTES			/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
	/** Person type entity */
	private EntityPerson person;
	/** Location type entity */
	private EntityLocation location;
	/** Organization type entity */
	private EntityOrganization organization;
	/** Function type entity */
	private EntityFunction function;
	/** Production type entity */
	private EntityProduction production;
	/** Date type entity */
	private EntityDate date;
	
	
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
	public void addPerson(EntityPerson pers)
	{ person = pers;
	}
	
	/**
	 * Add location type to an event.
	 * 
	 * @param loc
	 * 		Location type entity.
	 */
	public void addLocation(EntityLocation loc)
	{ location = loc;
	}
	
	/**
	 * Add organization type to an event.
	 * 
	 * @param org
	 * 		Organization type entity.
	 */
	public void addOrganization(EntityOrganization org)
	{ organization = org;
	}
	
	/**
	 * Add function type to an event.
	 * 
	 * @param func
	 * 		Function type entity.
	 */
	public void addFunction(EntityFunction func)
	{ function = func;
	}
	
	/**
	 * Add production type to an event.
	 * 
	 * @param prod
	 * 		Production type entity.
	 */
	public void addProduction(EntityProduction prod)
	{ production = prod;
	}
	
	/**
	 * Add date type to an event.
	 * 
	 * @param d
	 * 		Date type entity.
	 */
	public void addDate(EntityDate d)
	{ date = d;
	}
	
	
	

	/**
	 * Returns the person type of an
	 * event.
	 * 
	 * @return
	 * 		Person Type of the event.
	 */
	public EntityPerson getPerson()
	{	return person;
	}
	
	/**
	 * Returns the person type of an
	 * event.
	 * 
	  * @param person
	 * 		New person type of the event.
	 */
	public void setPerson(EntityPerson person)
	{	this.person = person;
	}
	
	
	/**
	 * Returns the location type of an
	 * event.
	 * 
	 * @return
	 * 		Location Type of the event.
	 */
	public EntityLocation getLocation()
	{	return location;
	}
	
	/**
	 * Returns the location type of an
	 * event.
	 * 
	  * @param location
	 * 		New location type of the event.
	 */
	public void setLocation(EntityLocation location)
	{	this.location = location;
	}
	
	
	/**
	 * Returns the production type of an
	 * event.
	 * 
	 * @return
	 * 		Production Type of the event.
	 */
	public EntityProduction getProduction()
	{	return production;
	}
	
	/**
	 * Returns the production type of an
	 * event.
	 * 
	  * @param production
	 * 		New production type of the event.
	 */
	public void setProduction(EntityProduction production)
	{	this.production = production;
	}
	
	/**
	 * Returns the function type of an
	 * event.
	 * 
	 * @return
	 * 		Function Type of the event.
	 */
	public EntityFunction getFunction()
	{	return function;
	}
	
	/**
	 * Returns the function type of an
	 * event.
	 * 
	  * @param function
	 * 		New function type of the event.
	 */
	public void setFunction(EntityFunction function)
	{	this.function = function;
	}
	
	/**
	 * Returns the organization type of an
	 * event.
	 * 
	 * @return
	 * 		Organization Type of the event.
	 */
	public EntityOrganization getOrganization()
	{	return organization;
	}
	
	/**
	 * Returns the organization type of an
	 * event.
	 * 
	  * @param organization
	 * 		New organization type of the event.
	 */
	public void setOrganization(EntityOrganization organization)
	{	this.organization = organization;
	}
	
	/**
	 * Returns the date type of an
	 * event.
	 * 
	 * @return
	 * 		Date Type of the event.
	 */
	public EntityDate getDate()
	{	return date;
	}
	
	/**
	 * Returns the date type of an
	 * event.
	 * 
	  * @param date
	 * 		New date type of the event.
	 */
	public void setSate(EntityDate date)
	{	this.date = date;
	}
	

}
