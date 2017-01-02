package fr.univavignon.nerwip.data.entity.mention;

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
 * Represents all spatial relationships between text positions.
 * 
 * @author Vincent Labatut
 */
public enum PositionRelation
{	/**
	 * The first mention completely preceeds the second one:<br/>
	 * <code>
	 * 1111------- | 111111----				<br/>
	 * -------2222 | ------22222
	 * </code>
	 */
	COMPLETE_PRECEDES,
	
	/**
	 * The first mention completely succeeds the second one:<br/>
	 * <code>
	 * -------1111 | ------11111				<br/>
	 * 2222------- | 222222-----
	 * </code>
	 */
	COMPLETE_SUCCEDES,
	
	/**
	 * The first mention precedes the second one, but they overlap:<br/>
	 * <code>
	 * 11111111--- | 111111-----				<br/>
	 * ---22222222 | -----222222
	 * </code>
	 */
	PARTIAL_PRECEDES,
	
	/**
	 * The first mention succeeds the second onen but they overlap:<br/>
	 * <code>
	 * ---11111111 | -----111111 				<br/>
	 * 22222222--- | 222222-----
	 * </code>
	 */
	PARTIAL_SUCCEDES,
	
	/**
	 * The first mention completely contains the second one:<br/>
	 * <code>
	 * 11111111111 | ---11111111 | 11111111--- 	<br/>
	 * ---22222--- | ---22222--- | ---22222---
	 * </code>
	 */
	CONTAINS,

	/**
	 * The first mention is completely contained in the second one:<br/>
	 * <code>
	 * ---11111--- | ---11111--- | ---11111--- 	<br/>
	 * 22222222222 | 22222222--- | ---22222222 
	 * </code>
	 */
	IS_CONTAINED,
	
	/**
	 * Both mentions occupy the exact same position:<br/>
	 * <code>
	 * --1111111-- 								<br/>
	 * --2222222--
	 * </code>
	 */
	PERFECT_MATCH;
	
	/**
	 * Processes the relation between the specified text positions.
	 * 
	 * @param start1
	 * 		Beginning of the first mention.
	 * @param end1
	 * 		End of the first mention.
	 * @param start2
	 * 		Beginning of the second mention.
	 * @param end2
	 * 		End of the second mention.
	 * @return
	 * 		Relative position.
	 */
	public static PositionRelation getRelation(int start1, int end1, int start2, int end2)
	{	PositionRelation result = null;
		
		if(start1<start2)
		{	if(end1<start2)
				result = COMPLETE_PRECEDES;
			else if(end1==start2)
				result = PARTIAL_PRECEDES;
			else if(end1>start2)
			{	if(end1<end2)
					result = PARTIAL_PRECEDES;
				else if(end1==end2)
					result = CONTAINS;
				else if(end1>end2)
					result = CONTAINS;
			}
		}

		else if(start1==start2)
		{	if(end1<end2)
				result = IS_CONTAINED;
			else if(end1==end2)
				result = PERFECT_MATCH;
			else if(end1>end2)
				result = CONTAINS;
		}
		
		else if(start1>start2)
		{	if(start1<end2)
			{	if(end1<end2)
					result = IS_CONTAINED;
				else if(end1==end2)
					result = IS_CONTAINED;
				else if(end1>end2)
					result = PARTIAL_SUCCEDES;
			}
			else if(start1==end2)
				result = PARTIAL_SUCCEDES;
			else if(start1>end2)
				result = COMPLETE_SUCCEDES;
		}
		
		return result;
	}
	
	/**
	 * Processes the relation between the specified text positions.
	 * 
	 * @param mention1
	 * 		First mention.
	 * @param start2
	 * 		Beginning of the second mention.
	 * @param end2
	 * 		End of the second mention.
	 * @return
	 * 		Relative position.
	 */
	public static PositionRelation getRelation(AbstractMention<?> mention1, int start2, int end2)
	{	int start1 = mention1.getStartPos();
		int end1 = mention1.getEndPos();
		PositionRelation result = getRelation(start1, end1, start2, end2);
		return result;
	}
	
	/**
	 * Processes the relation between the specified text positions.
	 * 
	 * @param mention1
	 * 		First mention.
	 * @param mention2
	 * 		Second mention.
	 * @return
	 * 		Relative position of the mentions.
	 */
	public static PositionRelation getRelation(AbstractMention<?> mention1, AbstractMention<?> mention2)
	{	int start1 = mention1.getStartPos();
		int end1 = mention1.getEndPos();
		int start2 = mention2.getStartPos();
		int end2 = mention2.getEndPos();
		PositionRelation result = getRelation(start1, end1, start2, end2);
		return result;
	}
}
