package fr.univavignon.nerwip.processing.combiner.svmbased;

import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;

/**
 * This class is used to represent some partial results when the SVM
 * is in charge of determining the mention positions (i.e. word-based
 * encoding). 
 * 
 * @author Vincent Labatut
 */
class WordMention
{	
	/**
	 * Builds an empty object.
	 */
	public WordMention()
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
	 * @param mention 
	 * 		Mention associated to the considered word.
	 * @param beginning 
	 * 		Whether or not the considered word is at the beginning of the associated mention.
	 */
	public WordMention(int startPos, int endPos, AbstractMention<?> mention, boolean beginning)
	{	this.startPos = startPos;
		this.endPos = endPos;
		this.mention = mention;
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
	// MENTION			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Entity associated to the considered word */
	private AbstractMention<?> mention;

	/**
	 * Returns the mention associated to the considered word.
	 * 
	 * @return
	 * 		Entity associated to the considered word.
	 */
	public AbstractMention<?> getMention()
	{	return mention;
	}

	/**
	 * Changes the mention associated to the considered word.
	 * 
	 * @param mention
	 * 		New mention associated to the considered word.
	 */
	public void setMention(AbstractMention<?> mention)
	{	this.mention = mention;
	}
	
	/**
	 * Returns the type of the mention associated to the considered word.
	 * 
	 * @return
	 * 		Type of the mention associated to the considered word.
	 */
	public EntityType getType()
	{	EntityType result = null;
		if(mention!=null)
			result = mention.getType();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// BEGINNING		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not the considered word is at the beginning of the associated mention */
	private boolean beginning;
	
	/**
	 * Indicates whether or not the considered word is at the 
	 * beginning of the associated mention.
	 * 
	 * @return
	 * 		{@code true} iff the word is at the beginning of the mention.
	 */
	public boolean isBeginning()
	{	return beginning;
	}

	/**
	 * Changes the flag indicating whether or not the considered 
	 * word is at the beginning of the associated mention. 
	 * 
	 * @param beginning
	 * 		{@code true} iff the word is at the beginning of the mention.
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
			+ " b=" + beginning + " - " + mention;
		return result;
	}
}
