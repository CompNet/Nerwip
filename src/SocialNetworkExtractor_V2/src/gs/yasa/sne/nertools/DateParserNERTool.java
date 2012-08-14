package gs.yasa.sne.nertools;

import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;
import gs.yasa.sne.common.DateAnnotation;

import java.util.ArrayList;
import java.util.List;

import tr.edu.gsu.burcu.dateextractor.Date;
import tr.edu.gsu.burcu.dateextractor.DateExtract;

/**
 * Class adaptor for DateParser
 * @author yasa akbulut
 *
 */
public class DateParserNERTool implements NERTool {

	
	@Override
	public ArrayList<Annotation> annotate(String text) {
		DateExtract de = new DateExtract();
		de.parseDate(text);
		List<Date> dates = de.getDates();
		ArrayList<Annotation> result = new ArrayList<Annotation>();
		for (Date date : dates) {
			result.add(new DateAnnotation(date));
		}
		return result;
	}

	@Override
	public AnnotationTool getName() {
		return AnnotationTool.DATEPARSER;
	}

}
