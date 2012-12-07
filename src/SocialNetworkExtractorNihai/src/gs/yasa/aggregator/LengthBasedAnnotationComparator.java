package gs.yasa.aggregator;

import gs.yasa.sne.common.Annotation;

import java.util.Comparator;




/**This class compare two annotations by their length
 * @author yasa akbulut
 * @version 1
 * @Override Comparator
 */
public class LengthBasedAnnotationComparator implements Comparator<Annotation> {

	@Override
	public int compare(Annotation o1, Annotation o2) {
		return (o1.getLength()-o2.getLength());
	
	}

}
