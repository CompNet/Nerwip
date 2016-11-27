package fr.univavignon.nerwip.processing.combiner.svmbased;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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
 * the mention combination is performed.
 */
public enum CombineMode
{	/** Each recognizer accounts for one vote */
	MENTION_UNIFORM("EntUnif"),
	/** Overall scores are used to determine vote weights */
	MENTION_WEIGHTED_OVERALL("EntWghtOvrl"),
	/** Category-related scores are used to determine vote weights */
	MENTION_WEIGHTED_CATEGORY("EntWghtCat"),
	/** No vote at all (the SVM handles everything), processing one chunk at a time */
	CHUNK_SINGLE("ChunkSngl"),
	/** No vote at all (the SVM handles everything), using the previous chunk*/
	CHUNK_PREVIOUS("ChunkPrev");

	/** String representing the parameter value */
	private String name;
	
	/**
	 * Builds a new combine mode value
	 * to be used as a parameter.
	 * 
	 * @param name
	 * 		String representing the parameter value.
	 */
	CombineMode(String name)
	{	this.name = name;
	}
	
	/**
	 * Indicates if this combine mode requires
	 * vote weights.
	 * 
	 * @return
	 * 		{@code true} if vote weights are required.
	 */
	public boolean hasWeights()
	{	boolean result = this==MENTION_WEIGHTED_OVERALL
			|| this==MENTION_WEIGHTED_CATEGORY;
		return result;
	}
	
	/**
	 * Indicates if this combine mode is chunk-based
	 * vote weights.
	 * 
	 * @return
	 * 		{@code true} if no vote is used (only the SVM).
	 */
	public boolean isChunkBased()
	{	boolean result = this==CHUNK_SINGLE
			|| this==CHUNK_PREVIOUS;
		return result;
	}
	
	@Override
	public String toString()
	{	return name;
	}
}
