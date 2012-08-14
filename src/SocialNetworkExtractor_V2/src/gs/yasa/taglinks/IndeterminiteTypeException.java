package gs.yasa.taglinks;

/**
 * This is a class to determine an exception
 * @author yasa akbulut
 * @version 1
 * @Override Exception
 *
 */
public class IndeterminiteTypeException extends Exception {

	/**
	 * version identifier for Serializable class
	 */
	private static final long serialVersionUID = 1666283285307312646L;

	/**
	 * This method is an exception for types that doesn't exist in class AnnotationType
	 * @author yasa akbulut
	 * @param typeList
	 */
	public IndeterminiteTypeException(String typeList)
	{
		super(typeList);
	}
}
