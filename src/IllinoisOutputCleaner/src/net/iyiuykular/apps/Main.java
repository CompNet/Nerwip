package net.iyiuykular.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String rawFilePath = "data/originalfile.txt";
		String referenceFilePath = "data/afterannotation.txt"; 
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
			} else {
//				if (rawChar == 'C') {
//					StringBuffer tmpBuffer = stringBuffer;
//					//System.out.println(tmpBuffer.toString());
//				}
				if (refChar == '[') {
					StringBuilder s = new StringBuilder();
					for (int i=0; i<3; i++) {
						s.append(refFileCharIterator.next());
					}
					if (s.toString() == "PER") {
						stringBuffer.append(refChar);
						while(refChar != ']') {
							refChar = refFileCharIterator.next();
							stringBuffer.append(refChar);
						}
						System.out.println(stringBuffer.toString());
						refChar = refFileCharIterator.next();	
					} else {
						stringBuffer.append("[");
						stringBuffer.append(s.toString());
						for (int i=0; i<3; i++) {
							rawFileCharIterator.next();
						}
					}
					
//				} else if(rawChar == ' ') {
//					stringBuffer.append(rawChar);
//					rawChar = rawFileCharIterator.next();
				} else {
					stringBuffer.append(rawChar);
					StringBuffer tmp = stringBuffer;
					//System.out.println(tmp);
					rawChar = rawFileCharIterator.next();
					refChar = refFileCharIterator.next();
				}
			}
			
		}
		String content = stringBuffer.toString();
		System.out.println(content);
		System.out.println("FINISHED");
	}

}
