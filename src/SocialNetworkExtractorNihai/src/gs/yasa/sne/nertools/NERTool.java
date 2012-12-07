package gs.yasa.sne.nertools;

import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;

import java.util.ArrayList;



/**
 * @author yasa akbulut
 * @version 1
 *
 */
public interface NERTool {

	public ArrayList<Annotation> annotate(String text);
	public AnnotationTool getName();
	
}
