package gs.yasa.taglinks;

/**
 * This class adjusts a position value
 * @author yasa akbulut
 * @version 1
 *
 */
public class Position {

	private int position;
	
	/**
	 * Constructor, initialize position value at zero
	 * @author yasa akbulut
	 */
	public Position()
	{
		position = 0;
	}
	/**
	 * Constructor, initialize position value to the value passed as parameter
	 * @param value
	 */
	public Position(int value)
	{
		position = value;
	}
	
	/**
	 * This method increments position value by adding parameter value to it 
	 * @param value
	 */
	public void increment(int value)
	{
		position+=value;
	}

	public int getPosition()
	{
		return position;
	}
	
	public void setPosition(int value)
	{
		position = value;
	}
	
}
