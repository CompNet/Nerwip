package gs.yasa.sne.common;

import java.io.Serializable;

/**
 * Defines an annotation's properties and has methods to compare two annotations by
 * their positions
 * @author yasa
 * @version 1
 * @Override Serializable
 */
public class Annotation implements Serializable{

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
	private String entityName;
	/**
	 * type of an entity; PERSON, DATE, etc.
	 */
	private AnnotationType entityType;
	/**
	 * source of annotation, a tool name or human
	 */
	private AnnotationTool source;
	
	public Annotation(String entityName, AnnotationType entityType,
			int startPos, int endPos, String rawString)
	{
		this.entityName = entityName;
		this.entityType = entityType;
		this.startPos = startPos;
		this.endPos = endPos;
		this.rawString = rawString;
	}
	
	public Annotation(String entityName, AnnotationType entityType,
			int startPos, int endPos, String rawString, AnnotationTool source)
	{
		this(entityName, entityType, startPos, endPos, rawString);
		this.source = source;
	}
	
	
	@Override
	public String toString()
	{
		return "Annotation(NAME=\""+entityName+"\", " +
				"TYPE="+entityType+", POS=("+startPos+","+endPos+"), SOURCE="+source.toString()+ ", RAW=(" + rawString + ")" + ")";	
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
	

	
	public AnnotationTool getSource() {
		return source;
	}


	public void setSource(AnnotationTool source) {
		this.source = source;
	}


	/**
	 * Controls overlapping in an exclusive fashion
	 * @author yasa akbulut
	 * @param a
	 * @return
	 */
	public boolean overlapsWith(Annotation a)
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
	public boolean containsExclusive(Annotation a)
	{
		if(a.getStartPos()>=startPos && a.getEndPos()<=endPos && a.getLength()<getLength())
			return true;
		return false;
	}

	/**
	 * Controls if the annotation is the same or contains the annotation passed as
	 * parameter
	 * @author yasa akbulut
	 * @param a
	 * @return true if annotation contains or the same with the parameter annotation
	 * else returns false
	 */
	public boolean contains(Annotation a)
	{
		if(a.getStartPos()>=startPos && a.getEndPos()<=endPos && a.getLength()<=getLength())
			return true;
		return false;
	}
	
	/**
	 * Compares the annotation passed as parameter with 
	 * @param a
	 * @return true if two annotations start an end with same positions, else false 
	 */
	public boolean hasSamePositions(Annotation a)
	{
		return (this.startPos==a.startPos)&&(this.endPos==a.endPos);
	}
	
	/**
	 * Calculates the length of an annotation
	 * @author yasa akbulut
	 * @return length value, integer
	 */
	public int getLength() {
		return endPos-startPos;
	}

	
	
}
