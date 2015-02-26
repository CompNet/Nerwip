package tr.edu.gsu.nerwip.recognition.combiner.svmbased;

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

import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.EntityType;

/**
 * This class is used to represent some partial results when the SVM
 * is in charge of determining the entity positions (i.e. word-based
 * encoding). 
 * 
 * @author Vincent Labatut
 */
public class WordEntity
{	
	/**
	 * Builds an empty object.
	 */
	public WordEntity()
	{
		//
	}
	
	/**
	 * Builds a new object using
	 * the specified parameters.
	 * 
	 * @param startPos
	 * 		Start position of the word in the whole text. 
	 * @param endPos
	 * 		End position of the word in the whole text. 
	 * @param entity 
	 * 		Entity associated to the considered word.
	 * @param beginning 
	 * 		Whether or not the considered word is at the beginning of the associated entity.
	 */
	public WordEntity(int startPos, int endPos, AbstractEntity<?> entity, boolean beginning)
	{	this.startPos = startPos;
		this.endPos = endPos;
		this.entity = entity;
		this.beginning = beginning;
	}
	
	/////////////////////////////////////////////////////////////////
	// POSITION			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** The starting startPos of the word in the whole article */
	private int startPos; 
	/** The ending startPos of the word in the whole article */
	private int endPos; 
	
	/**
	 * Returns the starting position of the considered word.
	 * 
	 * @return
	 * 		Starting position of the considered word.
	 */
	public int getStartPosition()
	{	return startPos;
	}

	/**
	 * Changes the starting position of the considered word.
	 * 
	 * @param startPos
	 * 		New starting position of the considered word.
	 */
	public void setStartPosition(int startPos)
	{	this.startPos = startPos;
	}
	
	/**
	 * Returns the ending position of the considered word.
	 * 
	 * @return
	 * 		Ending position of the considered word.
	 */
	public int getEndPosition()
	{	return endPos;
	}

	/**
	 * Changes the ending position of the considered word.
	 * 
	 * @param endPos
	 * 		New ending position of the considered word.
	 */
	public void setEndPosition(int endPos)
	{	this.endPos = endPos;
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Entity associated to the considered word */
	private AbstractEntity<?> entity;

	/**
	 * Returns the entity associated to the considered word.
	 * 
	 * @return
	 * 		Entity associated to the considered word.
	 */
	public AbstractEntity<?> getEntity()
	{	return entity;
	}

	/**
	 * Changes the entity associated to the considered word.
	 * 
	 * @param entity
	 * 		New entity associated to the considered word.
	 */
	public void setEntity(AbstractEntity<?> entity)
	{	this.entity = entity;
	}
	
	/**
	 * Returns the type of the entity associated to the considered word.
	 * 
	 * @return
	 * 		Type of the entity associated to the considered word.
	 */
	public EntityType getType()
	{	EntityType result = null;
		if(entity!=null)
			result = entity.getType();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// BEGINNING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not the considered word is at the beginning of the associated entity */
	private boolean beginning;
	
	/**
	 * Indicates whether or not the considered word is at the 
	 * beginning of the associated entity.
	 * 
	 * @return
	 * 		{@code true} iff the word is at the beginning of the entity.
	 */
	public boolean isBeginning()
	{	return beginning;
	}

	/**
	 * Changes the flag indicating whether or not the considered 
	 * word is at the beginning of the associated entity. 
	 * 
	 * @param beginning
	 * 		{@code true} iff the word is at the beginning of the entity.
	 */
	public void setBeginning(boolean beginning)
	{	this.beginning = beginning;
	}

	/////////////////////////////////////////////////////////////////
	// STRING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = "("+startPos+","+endPos+")"
			+ " b=" + beginning + " - " + entity;
		return result;
	}
}
