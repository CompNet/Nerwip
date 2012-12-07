package gs.yasa.sne.nertools;

import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;
import gs.yasa.taglinks.TagLinks;

import java.util.ArrayList;



public class LinkedEntityRecognizerNERTool implements NERTool {

	@Override
	public ArrayList<Annotation> annotate(String text) {
		TagLinks tool = new TagLinks();
		return tool.annotateText(text);
	}

	@Override
	public AnnotationTool getName() {
		// TODO Auto-generated method stub
		return AnnotationTool.TAGLINKS;
	}

}
