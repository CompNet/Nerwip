package gs.yasa.evaluator;

import gs.yasa.sne.common.Annotation;

import java.util.ArrayList;



/**
 * This class generates an annotation and a result lists based on a tool and its
 * configuration
 * @author yasa akbulut
 * @version 1
 */
public class ToolOutput {

	/**
	 * the name of the tool
	 */
	private String toolName;
	/**
	 * a tool's configuration
	 */
	private String toolConfiguration;
	/**
	 * an annotation list
	 */
	public ArrayList<Annotation> annotations;
	/**
	 * a result list after evaluation
	 */
	public ArrayList<EvaluationOutcome> results;
	
	/**
	 * This method takes as parameters a tool name and its configuration and
	 * generates an annotation and result list
	 * @param toolName
	 * @param toolConfiguration
	 * @author yasa akbulut
	 */
	public ToolOutput(String toolName, String toolConfiguration)
	{
		annotations = new ArrayList<Annotation>();
		results = new ArrayList<EvaluationOutcome>();
		this.toolName = toolName;
		this.toolConfiguration = toolConfiguration;
	}
	
	public String getToolName() {
		return toolName;
	}
	public String getToolConfiguration() {
		return toolConfiguration;
	}	
	
	@Override
	public String toString()
	{
		return toolName + "(" + toolConfiguration + ")";
	}
	
}
