package gs.yasa.evaluator;

/**
 * This class forms outcome types and prints them
 * @author yasa akbulut
 * @version 1
 */
public class EvaluationOutcome {
	
	/**
	 * different types of outcomes
	 */
	public OutcomeClasses outcomeClass;
	/**
	 * the type of the outcome result
	 */
	public boolean correctType = false;	

	@Override
	public String toString()
	{
		return outcomeClass.toString() + "(correct type:" + correctType + ")";
	}
	
}
