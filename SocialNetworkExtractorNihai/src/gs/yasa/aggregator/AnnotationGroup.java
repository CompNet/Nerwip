package gs.yasa.aggregator;

import gs.yasa.sne.common.Annotation;

import java.util.ArrayList;


/**
 * This class represents an annotation group; annotations that can belong to a same
 * entity.
 * @author yasa akbulut
 * @version 1
 *
 */
public class AnnotationGroup {

	public ArrayList<Annotation> annotations;
	private String name;
	
	/**
	 * Constructor
	 * @author yasa akbulut
	 */
	public AnnotationGroup()
	{
		annotations = new ArrayList<Annotation>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
