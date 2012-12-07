package net.iyiuykular.apps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Properties;




public class Main {
	
	// Read file into String.
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
	
	public static void main(String[] args) throws IOException {
		
		String propertiesFilePath = "/home/samet/.bin/Dropbox/workspace/config/sne.properties_linux"; 
		Properties prop = new Properties();
		prop.load(new FileInputStream(propertiesFilePath));
		
		String assetDirectory = prop.getProperty("assetDirectory");
		String articleTitle = args[0];
		

		String rawFilePath = assetDirectory + articleTitle + File.separator + articleTitle;
		String referenceFilePath = assetDirectory + articleTitle + File.separator + articleTitle + "(reference)";
		//String newReferenceFilePath = assetDirectory + articleTitle + "\\" + articleTitle + "_reference.txt";
		
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
				if (rawChar == 'C') {
					StringBuffer tmpBuffer = stringBuffer;
					//System.out.println(tmpBuffer.toString());
				}
				if (refChar == '<') {
					stringBuffer.append(refChar);
					while(refChar != '>') {
						refChar = refFileCharIterator.next();
						stringBuffer.append(refChar);
					}
					System.out.println(stringBuffer.toString());
					refChar = refFileCharIterator.next();
				} else if(rawChar == ' ') {
					stringBuffer.append(rawChar);
					rawChar = rawFileCharIterator.next();
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
		writeFile(assetDirectory + articleTitle + File.separator + articleTitle + "_new_reference.txt", content);
		//writeFile("C:\\Users\\samet\\Dropbox\\workspace\\data\\clearance\\annotations\\Hosni_Mubarak\\Hosni_Mubarak_new_reference.txt", content);
		System.out.println("Finished.");
	}
}
