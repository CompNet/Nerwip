package fr.univavignon.search.events;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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

import java.util.Date;

/**
 * Object used to represent a ground truth event, manually
 * annotated by the user and placed in the appropriate file.
 * 
 * @author Vincent Labatut
 */
public class ReferenceEvent
{	
	/**
	 * Builds a new reference event using the specified id,
	 * name and date.
	 * 
	 * @param id
	 * 		Unique id of this reference event. 
	 * @param name 
	 * 		Unique name of this reference event. 
	 * @param date
	 * 		Date of this reference event. 
	 * @param parent
	 * 		Unique parent id of this reference event, or
	 * 		{@code null} if no parent. 
	 */
	public ReferenceEvent(int id, String name, Date date, ReferenceEvent parent)
	{	this.id = id;
		this.name = name;
		this.date = date;
		this.parent = parent;
	}
	
	/////////////////////////////////////////////////////////////////
	// ID			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Unique id of this reference event */
	private int id;
	
	/**
	 * Returns the unique id of this reference event.
	 * 
	 * @return
	 * 		The id of this reference event.
	 */
	public int getId()
	{	return id;
	}
	
	/////////////////////////////////////////////////////////////////
	// NAME			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Unique name of this reference event */
	private String name;
	
	/**
	 * Returns the unique name of this reference event.
	 * 
	 * @return
	 * 		The name of this reference event.
	 */
	public String getName() 
	{	return name;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATE			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Date of this reference event */
	private Date date;
	
	/**
	 * Returns the date of this reference event.
	 * 
	 * @return
	 * 		The date of this reference event.
	 */
	public Date getDate() 
	{	return date;
	}
	
	/**
	 * Checks whether the date of this reference event is contained
	 * in the specified temporal period.
	 * 
	 * @param startDate
	 * 		Beginning of the period.
	 * @param endDate
	 * 		Ending of the period.
	 * @return
	 * 		{@code true} iff the date is within the period.
	 */
	public boolean isWithinPeriod(Date startDate, Date endDate)
	{	boolean result = (date.equals(startDate) || date.after(startDate))
			&& (date.equals(endDate) || date.before(endDate));
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// ID			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Parent of this reference event (or {@code null} if no parent) */
	private ReferenceEvent parent = null;
	
	/**
	 * Returns the parent of this reference event
	 * (of {@code null} if no parent).
	 * 
	 * @return
	 * 		The parent of this reference event (or 
	 * 		{@code null} if no parent).
	 */
	public ReferenceEvent getParent()
	{	return parent;
	}
	
	/**
	 * Returns the top ancestor of this reference event
	 * (of {@code this} if no parent).
	 * 
	 * @return
	 * 		The top ancestor of this reference event (or 
	 * 		{@code this} if no parent).
	 */
	public ReferenceEvent getAncestor()
	{	ReferenceEvent result = this;
		if(parent!=null)
			result = parent.getAncestor();
		return result;
	}
}
