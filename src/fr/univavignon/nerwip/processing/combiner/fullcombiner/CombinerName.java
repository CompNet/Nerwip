package fr.univavignon.nerwip.processing.combiner.fullcombiner;

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

/**
 * Represents the combiner used to process locations,
 * organizations and persons.
 * 
 * @author Vincent Labatut
 */
public enum CombinerName
{	/** Use the best SVM-based combiner configuration */
	SVM("svm"),
	/** Use the best vote-based combiner configuration */
	VOTE("vote");
	
	/** String representing the parameter value */
	private String name;
	
	/**
	 * Builds a new CombinerName value
	 * to be used as a parameter.
	 * 
	 * @param name
	 * 		String representing the parameter value.
	 */
	CombinerName(String name)
	{	this.name = name;
	}
	
	@Override
	public String toString()
	{	return name;
	}
}
