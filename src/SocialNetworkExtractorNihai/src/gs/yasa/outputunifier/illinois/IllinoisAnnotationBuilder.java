package gs.yasa.outputunifier.illinois;

import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;
import gs.yasa.sne.common.AnnotationType;

import java.util.ArrayList;
import java.util.HashMap;




/**
 * This class sets Illinois Annotation Tool's setting for an annotation
 * @author yasa akbulut
 * @version 1
 *
 */
public class IllinoisAnnotationBuilder {

	/**
	 * a HashMap to contain tool's configurations and settings
	 */
	private HashMap<String, AnnotationType> conversionMap;

	/**
	 * Constructor. Converse entity types in unique form
	 * @author yasa akbulut
	 */
	public IllinoisAnnotationBuilder()
	{
		conversionMap = new HashMap<String, AnnotationType>();
		//initialization with possible types
		conversionMap.put("PER", AnnotationType.PERSON);
		conversionMap.put("LOC", AnnotationType.LOCATION);
		conversionMap.put("ORG", AnnotationType.ORGANIZATION);
		conversionMap.put("MISC", AnnotationType.MISC);
	}
	
	/**
	 * Builds a new annotations list by setting tool name
	 * @param entityName
	 * @param entityType
	 * @param startPos
	 * @param endPos
	 * @param rawText
	 * @return an annotations list
	 * @author yasa akbulut
	 */	
	public Annotation build(String rawText,int startPos, int endPos)
	{
		String[] splittedText = rawText.split(" ",2);
		AnnotationType entityType = conversionMap.get(splittedText[0].substring(1));
		String entityName = "";
		Annotation result = null;
		try
		{
		 entityName = splittedText[1].substring(0, splittedText[1].length()-3);
		 result = new Annotation(entityName,entityType,startPos,endPos,rawText,AnnotationTool.ILLINOIS);
		}catch(StringIndexOutOfBoundsException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Calculates a relative position for an annotation by subtracting the shift
	 * @param annotations
	 * @author yasa akbulut
	 */
	public void calculateRelativePositions(ArrayList<Annotation> annotations)
	{
		int rollingCount = 0;
		for (Annotation annotation : annotations) {
			int startPos=annotation.getStartPos();
			int endPos=annotation.getEndPos();
			annotation.setStartPos(startPos-rollingCount);
			annotation.setEndPos(endPos-(rollingCount));
			int length = endPos-startPos;
			int shift = length - annotation.getEntityName().length();
			rollingCount+=shift;
			annotation.setEndPos(annotation.getEndPos()-shift);
			System.out.println(annotation);
		}
	}
}
