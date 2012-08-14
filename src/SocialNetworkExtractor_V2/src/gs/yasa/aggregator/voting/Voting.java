package gs.yasa.aggregator.voting;

import gs.yasa.sne.common.AnnotationTool;

import java.util.HashMap;



/**sets a value for each of annotation tools according to annotations' positions
 * and type.
 * 
 * @author yasa akbulut
 * 
 * @version 1
 * 
 */
public class Voting {

	/**
	 * contains position voting by each tool 
	 */
	public static HashMap<AnnotationTool, Integer> positionVotePowers;
	/**
	 * contains type voting by each tool
	 */
	public static HashMap<AnnotationTool, Integer> typeVotePowers;
	
	/**This method initializes a value to each tool by type and positions
	 * and adds in a HashMap.
	 * @author yasa akbulut   
	 */
	public static void initialize()
	{
		positionVotePowers = new HashMap<AnnotationTool, Integer>();
		positionVotePowers.put(AnnotationTool.HUMAN, 10);
		positionVotePowers.put(AnnotationTool.TAGLINKS, 4);
		positionVotePowers.put(AnnotationTool.OPENCALAIS, 2);
		positionVotePowers.put(AnnotationTool.ILLINOIS, 1);
		positionVotePowers.put(AnnotationTool.STANFORD, 1);
		positionVotePowers.put(AnnotationTool.DATEPARSER, 1);

		
		typeVotePowers = new HashMap<AnnotationTool, Integer>();
		typeVotePowers.put(AnnotationTool.HUMAN, 15);
		typeVotePowers.put(AnnotationTool.TAGLINKS, 4);
		typeVotePowers.put(AnnotationTool.OPENCALAIS, 3);
		typeVotePowers.put(AnnotationTool.ILLINOIS, 1);
		typeVotePowers.put(AnnotationTool.STANFORD, 1);
		typeVotePowers.put(AnnotationTool.DATEPARSER, 1);
	}
	
}
