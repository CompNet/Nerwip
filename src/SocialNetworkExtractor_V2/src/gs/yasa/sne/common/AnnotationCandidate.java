package gs.yasa.sne.common;

import java.io.Serializable;

import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;
import gs.yasa.sne.common.AnnotationType;


/**
 * @author yasa
 *
 */
public class AnnotationCandidate implements Serializable{


	/**
	 * version identifier for Serializable class
	 */
	private static final long serialVersionUID = 5280218305120247420L;
	/**
	 * 
	 */
	private String rawString;
	/**
	 * start position of an entity
	 */
	private int startPos;
	/**
	 * end position of an entity
	 */
	private int endPos;
	/**
	 * 
	 */
	private String entityName;
	/**
	 * 
	 */
	private AnnotationType entityType=null;
	/**
	 * boolean to define the validity of an entitys
	 */
	private boolean valid = true; //for example, starts with uppercase?
	
	/**
	 * Contructor 
	 * @author yasa akbulut
	 * @param entityName
	 * @param entityType
	 * @param startPos
	 * @param endPos
	 * @param rawString
	 */
	public AnnotationCandidate(String entityName, AnnotationType entityType,
			int startPos, int endPos, String rawString)
	{
		this.entityName = entityName;
		this.entityType = entityType;
		this.startPos = startPos;
		this.endPos = endPos;
		this.rawString = rawString;
	}
	
	/**
	 * Constructor
	 * @author yasa akbulut
	 */
	public AnnotationCandidate()
	{
		
	}
	
	@Override
	public String toString()
	{
		return "Annotation(NAME=\""+entityName+"\", " +
				"TYPE="+entityType+", POS=("+startPos+","+endPos+"))";	
	}

	public String getRawString() {
		return rawString;
	}

	public void setRawString(String rawString) {
		this.rawString = rawString;
	}

	public int getStartPos() {
		return startPos;
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public int getEndPos() {
		return endPos;
	}

	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public AnnotationType getEntityType() {
		return entityType;
	}

	public void setEntityType(AnnotationType entityType) {
		this.entityType = entityType;
	}

	
	/**
	 * Controls overlapping in an exclusive fashion
	 * @param a
	 * @return
	 */
	public boolean overlapsWith(AnnotationCandidate a)
	{
		if((a.getStartPos()<endPos&&a.getEndPos()>endPos)
				|| (startPos<a.getEndPos()&&endPos>a.getEndPos()))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Controls if annotation contains an annotation passed as parameter
	 * @author yasa akbulut
	 * @param a
	 * @return true if contains, if not false
	 */
	public boolean contains(AnnotationCandidate a)
	{
		if(a.getStartPos()>=startPos && a.getEndPos()<=endPos && a.getLength()<getLength())
			return true;
		return false;
	}


	/**
	 * Calculates the length of an annotation
	 * @author yasa akbulut
	 * @return length value, integer
	 */
	public int getLength() {
		return endPos-startPos;
	}

	
	/**
	 * 
	 * @return
	 * @throws ImproperCandidateException
	 */
	public Annotation toAnnotation() throws ImproperCandidateException
	{
		if(entityType==null)
			throw new ImproperCandidateException(this);
		return new Annotation(entityName, entityType, startPos, endPos, rawString, AnnotationTool.TAGLINKS);
	}



	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	
	
}
