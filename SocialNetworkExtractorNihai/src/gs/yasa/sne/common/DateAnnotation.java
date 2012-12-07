package gs.yasa.sne.common;

import tr.edu.gsu.burcu.dateextractor.Date;

/**
 * This class defines an annotation for type date
 * @author yasa akbulut
 * @version 1
 * 
 */
public class DateAnnotation extends Annotation {

	/**
	 * version identifier for Serializable class
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * day value of a date
	 */
	private int day;
	/**
	 * month value of a date
	 */
	private int month;
	/**
	 * year value of a date
	 */
	private int year;
	
	/**
	 * Constructor, takes a Date object parameter, turns to a Annotation object 
	 * @author yasa akbulut
	 * @param date
	 */

	public DateAnnotation(Date date) {
		super("", AnnotationType.DATE, date.getPosStart(), date.getPosEnd(), "");
		super.setEntityName(date.getDay()+"/"+date.getMonth()+"/"+date.getYear());
		this.day = date.getDay();
		this.month = date.getMonth();
		this.year = date.getYear();
		super.setSource(AnnotationTool.DATEPARSER);
	}
	/**
	 * Constructor, same here with setting his source
	 * @author yasa akbulut
	 * @param date
	 * @param source
	 */
	public DateAnnotation(Date date, AnnotationTool source)
	{
		this(date);
		this.setSource(source);
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}
	
	

}
