package tr.edu.gsu.nerwip.data.entity;

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

/**
 * Represents all spatial relationships between text positions.
 * 
 * @author Vincent Labatut
 */
public enum PositionRelation
{	/**
	 * The first entity completely preceeds the second one:<br/>
	 * <code>
	 * 1111------- | 111111----				<br/>
	 * -------2222 | ------22222
	 * </code>
	 */
	COMPLETE_PRECEDES,
	
	/**
	 * The first entity completely succeeds the second one:<br/>
	 * <code>
	 * -------1111 | ------11111				<br/>
	 * 2222------- | 222222-----
	 * </code>
	 */
	COMPLETE_SUCCEDES,
	
	/**
	 * The first entity precedes the second one, but they overlap:<br/>
	 * <code>
	 * 11111111--- | 111111-----				<br/>
	 * ---22222222 | -----222222
	 * </code>
	 */
	PARTIAL_PRECEDES,
	
	/**
	 * The first entity succeeds the second onen but they overlap:<br/>
	 * <code>
	 * ---11111111 | -----111111 				<br/>
	 * 22222222--- | 222222-----
	 * </code>
	 */
	PARTIAL_SUCCEDES,
	
	/**
	 * The first entity completely contains the second one:<br/>
	 * <code>
	 * 11111111111 | ---11111111 | 11111111--- 	<br/>
	 * ---22222--- | ---22222--- | ---22222---
	 * </code>
	 */
	CONTAINS,

	/**
	 * The first entity is completely contained in the second one:<br/>
	 * <code>
	 * ---11111--- | ---11111--- | ---11111--- 	<br/>
	 * 22222222222 | 22222222--- | ---22222222 
	 * </code>
	 */
	IS_CONTAINED,
	
	/**
	 * Both entities occupy the exact same position:<br/>
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
	 * 		Beginning of the first entity.
	 * @param end1
	 * 		End of the first entity.
	 * @param start2
	 * 		Beginning of the second entity.
	 * @param end2
	 * 		End of the second entity.
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
	 * @param entity1
	 * 		First entity.
	 * @param start2
	 * 		Beginning of the second entity.
	 * @param end2
	 * 		End of the second entity.
	 * @return
	 * 		Relative position.
	 */
	public static PositionRelation getRelation(AbstractEntity<?> entity1, int start2, int end2)
	{	int start1 = entity1.getStartPos();
		int end1 = entity1.getEndPos();
		PositionRelation result = getRelation(start1, end1, start2, end2);
		return result;
	}
	
	/**
	 * Processes the relation between the specified text positions.
	 * 
	 * @param entity1
	 * 		First entity.
	 * @param entity2
	 * 		Second entity.
	 * @return
	 * 		Relative position of the entities.
	 */
	public static PositionRelation getRelation(AbstractEntity<?> entity1, AbstractEntity<?> entity2)
	{	int start1 = entity1.getStartPos();
		int end1 = entity1.getEndPos();
		int start2 = entity2.getStartPos();
		int end2 = entity2.getEndPos();
		PositionRelation result = getRelation(start1, end1, start2, end2);
		return result;
	}
}
