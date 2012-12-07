package gs.yasa.aggregator;
import gs.yasa.sne.common.Annotation;

import java.io.Serializable;
import java.util.Comparator;




/** Compares two annotations based on their positions.
 * The annotation having a lower starting position is smaller.
 * In case of equality of starting positions, the longest annotation
 * is smaller (comes first in a sort).
 * @author yasa
 *
 */
public class PositionBasedAnnotationComparator implements
		Comparator<Annotation>, Serializable {


	/**
	 * version identifier for Serializable class
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(Annotation o1, Annotation o2) {
		// TODO Auto-generated method stub
		if(o1.getStartPos()>o2.getStartPos())
		{
			return 1;
		}else
		{
			if(o1.getStartPos()==o2.getStartPos())
			{
				if(o1.getEndPos()>o2.getEndPos())
					return 1;
				else
				{
					if(o1.getEndPos()==o2.getEndPos())
						return 0;
					else
						return -1;
				}
			}else
			{
				return -1;
			}
		}
	}

}
