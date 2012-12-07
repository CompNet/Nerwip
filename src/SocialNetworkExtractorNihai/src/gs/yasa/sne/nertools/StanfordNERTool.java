package gs.yasa.sne.nertools;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import gs.yasa.outputunifier.stanford.StanfordOutputReader;
import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;




public class StanfordNERTool implements NERTool{
	
	private String classifierPath;
	
	public StanfordNERTool()
	{
		this("lib/nertools/stanford/classifiers/ner-eng-ie.crf-4-conll-distsim.ser.gz");
	}
	public StanfordNERTool(String classifier)
	{
		this.classifierPath = classifier;
	}

	public String getClassifierPath() {
		return classifierPath;
	}
	
	
	public String runTool(String inputText)
	{
		String annotatedText = "";
		try {
			//some special treatment for stanford
			String text = inputText.replaceAll("Å‚", "l");
			text = text.replaceAll("Å�", "L");
			text = text.replaceAll("Å", "A");
			text = text.replaceAll("â€“", "-");
			text = text.replaceAll("½", "-");
			text = text.replaceAll("…", "-");
			text = text.replaceAll("ö", "-");
			text = text.replaceAll("â€”", "-"); //yes, "â€”" is actually different from "â€“" 
			
			String asciitext = Normalizer.normalize(text, Normalizer.Form.NFKD);
			String regex = "[\\p{InCombiningDiacriticalMarks}]+";
			asciitext = new String(asciitext.replaceAll(regex, "").getBytes("ascii"), "ascii");
			AbstractSequenceClassifier classifier = CRFClassifier.getClassifier(classifierPath);
			annotatedText= classifier.classifyWithInlineXML(asciitext);
	
			
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return annotatedText;
	}
	
	
	@Override
	public ArrayList<Annotation> annotate(String text)
	{
		String annotatedString = runTool(text);
		StanfordOutputReader reader = new StanfordOutputReader();
		return reader.read(annotatedString);
	}
	@Override
	public AnnotationTool getName() {
		// TODO Auto-generated method stub
		return AnnotationTool.STANFORD;
	}
	
	
}
