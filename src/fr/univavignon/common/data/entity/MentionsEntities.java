package fr.univavignon.common.data.entity;

import fr.univavignon.common.data.entity.mention.Mentions;

/**
 * Class representing a couple of Entities and Mentions
 * objects, generated/updated by the same tool.
 * <br/>
 * This class is just a convenient way of returning both
 * types at once in resolvers and linkers. This was considered
 * a better practice than passing empty Entities and Mentions
 * objects. 
 * 
 * @author Vincent Labatut
 */
public class MentionsEntities
{	
	/**
	 * Builds an Entities object with current
	 * date and the reference source.
	 * 
	 * @param mentions
	 * 		Set of mentions. 
	 * @param entities 
	 * 		Set of entities. 
	 */
	public MentionsEntities(Mentions mentions, Entities entities)
	{	this.mentions = mentions;
		this.entities = entities;
	}
	
	/** Set of {@link Entities} */ 
	public Entities entities;
	/** Set of {@link Mentions} */ 
	public Mentions mentions;
}
