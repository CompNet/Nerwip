package gs.yasa.outputunifier.stanfordmanual;

import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;
import gs.yasa.sne.common.AnnotationType;

import java.util.ArrayList;
import java.util.HashMap;



/**
 * This class sets Stanford Manual Annotation Tool's setting for an annotation
 * @author yasa akbulut
 * @version 1
 *
 */
public class StanfordMATAnnotationBuilder {
/**
 * a HashMap to contain tool's configurations and settings
 */
private HashMap<String, AnnotationType> conversionMap;
	
/**
 * Constructor. Converse entity types in unique form
 * @author yasa akbulut
 */
	public StanfordMATAnnotationBuilder()
	{
		conversionMap = new HashMap<String, AnnotationType>();
		//initialization with possible types
		conversionMap.put("PERSON", AnnotationType.PERSON);
		conversionMap.put("LOCATION", AnnotationType.LOCATION);
		conversionMap.put("ORGANIZATION", AnnotationType.ORGANIZATION);
		conversionMap.put("MISC", AnnotationType.MISC);
		conversionMap.put("DATE", AnnotationType.DATE);
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
	public Annotation build(String entityName, String entityType,int startPos,
			int endPos, String rawText)
	{
	 
		return new Annotation(entityName,conversionMap.get(entityType),
				startPos,endPos,rawText,AnnotationTool.STANFORD);
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
		}
	}
}
