package net.iyiuykular.apps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class Main {

	
	private static String readFile(String path) throws IOException {
		FileInputStream stream = new FileInputStream	(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.forName("UTF-8").decode(bb).toString();
		} finally {
			stream.close();
		}
	}
	
	private static void writeFile(String path, String content) throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(path), "UTF-8"));
		try {
			out.write(content);
		} finally {
			out.close();
		}
	}
	private static void log(String text) {
		Boolean debug = false;
		if (debug) {
			System.out.println(text);
		}
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String rawFilePath = "data/originalfile.txt";
		String referenceFilePath = "data/afterannotation.txt";
		
		rawFilePath = "/tmp/annotations/Adolf_hitler/Adolf_hitler";
		referenceFilePath = "/tmp/annotated.txt";
		// TODO Auto-generated method stub
		String rawFile = readFile(rawFilePath);
		String referenceFile = readFile(referenceFilePath);
		
		CharacterIterator rawFileCharIterator = new StringCharacterIterator(rawFile);
		CharacterIterator refFileCharIterator = new StringCharacterIterator(referenceFile);
		StringBuffer stringBuffer = new StringBuffer();
		char rawChar = rawFileCharIterator.first();
		char refChar = refFileCharIterator.first();
		
		for(; rawChar != rawFileCharIterator.DONE && refChar != refFileCharIterator.DONE;) {

			if (rawChar == refChar) {
				stringBuffer.append(String.valueOf(rawChar));
				rawChar = rawFileCharIterator.next();
				refChar = refFileCharIterator.next();
				log(stringBuffer.toString());
			} else {
				if (refChar == '[') {
					StringBuilder s = new StringBuilder();
					for (int i=0; i<3; i++) {
						s.append(refFileCharIterator.next());
					}
					String threeChars = s.toString();
					if (threeChars.equals("PER") || threeChars.equals("MIS") || threeChars.equals("ORG") || threeChars.equals("LOC")) {
						StringBuilder detectedAnnotation = new StringBuilder();
						detectedAnnotation.append("[");
						detectedAnnotation.append(threeChars);
						
						// Annotation types are represented with three chars, except MISC type.
						if (threeChars.toString().equals("MIS")) {
							detectedAnnotation.append("C");
							refChar = refFileCharIterator.next();
						}
						
						// Add another space, coming after annotation type:
						detectedAnnotation.append(" ");
						refChar = refFileCharIterator.next();
												
						while(refChar != ']') {
							refChar = refFileCharIterator.next();
							detectedAnnotation.append(refChar);
							log(detectedAnnotation.toString());
						}
						stringBuffer.append(detectedAnnotation.toString());
						
						// Annotation is represented as this: [PER Person Name  ]
						// So that we substract 8 chars to find real length of annotation.
						int trimmedAnnotationLength = detectedAnnotation.toString().length() - 8;
						if (threeChars.equals("MIS")) {
							trimmedAnnotationLength--;
						}
						for(int i=0; i<trimmedAnnotationLength; i++) {
							rawChar = rawFileCharIterator.next();
						}
						
						log(stringBuffer.toString());
						refChar = refFileCharIterator.next();	
					} else {
						stringBuffer.append("[");
						stringBuffer.append(s.toString());
						for (int i=0; i<3; i++) {
							rawFileCharIterator.next();
						}
					}
				} else if (rawChar == ';'){
					if (refChar == ' ' && refFileCharIterator.next() == ';') {
						refChar = ';';
					}
				} else if (rawChar == '\n') {
					stringBuffer.append(rawChar);
					rawChar = rawFileCharIterator.next();
				} else {
					//stringBuffer.append(rawChar);
					//StringBuffer tmp = stringBuffer;
					//rawChar = rawFileCharIterator.next();
					refChar = refFileCharIterator.next();
				}
			}
			
		}
		String content = stringBuffer.toString();
		System.out.println(content);
		writeFile("/tmp/cleared.txt", content);
		System.out.println("FINISHED");
	}

}
