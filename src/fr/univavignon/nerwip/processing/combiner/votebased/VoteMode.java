package fr.univavignon.nerwip.processing.combiner.votebased;

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

/**
 * Enumeration used to configure how
 * the vote is performed.
 * 
 * @author Vincent Labatut
 */
public enum VoteMode
{	/** Each recognizer accounts for one vote */
	UNIFORM("Unif"),
	/** Overall scores are used to determine vote weights */
	WEIGHTED_OVERALL("WghtOvrl"),
	/** Category-related scores are used to determine vote weights */
	WEIGHTED_CATEGORY("WghtCat");

	/** String representing the parameter value */
	private String name;
	
	/**
	 * Builds a new vote mode value
	 * to be used as a parameter.
	 * 
	 * @param name
	 * 		String representing the parameter value.
	 */
	VoteMode(String name)
	{	this.name = name;
	}
	
	/**
	 * Indicates if this vote mode requires weights.
	 * 
	 * @return
	 * 		{@code true} if vote weights are required.
	 */
	public boolean hasWeights()
	{	boolean result = this==WEIGHTED_OVERALL
			|| this==WEIGHTED_CATEGORY;
		return result;
	}
	
	@Override
	public String toString()
	{	return name;
	}
}
