package net.iyiuykular.apps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.MappedByteBuffer;
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
		
		String propertiesFilePath = "/home/samet/.bin/Dropbox/workspace/sne.properties_linux"; 
		Properties prop = new Properties();
		prop.load(new FileInputStream(propertiesFilePath));
		
		String assetDirectory = prop.getProperty("assetDirectory");
		String articleTitle = args[0];
		

		String rawFilePath = assetDirectory + articleTitle + File.separator + articleTitle;
		String referenceFilePath = assetDirectory + articleTitle + File.separator + articleTitle + "_new_reference.txt";
		String newReferenceFilePath = assetDirectory + articleTitle + File.separator + articleTitle + "_new_reference.txt";
		
		
		//If you want to prepare a new ref, select NEWREF. 
		//If you want to test existing reference file against raw file, select TEST.
		//TODO this parameter may be a run time arg.
		String runType = "TEST";
		//String runType = "NEWREF";
		
		
		
		
		String referenceFile = readFile(referenceFilePath);
		
		if (runType == "NEWREF") {
			//We are saving reference file as UTF-8
			writeFile(newReferenceFilePath, referenceFile);
			System.exit(0);
		}
		
		referenceFilePath = newReferenceFilePath;
		referenceFile = readFile(referenceFilePath);
		String rawFile = readFile(rawFilePath);
		
		
		String outText="";	
		outText = referenceFile.replaceAll("<tag name=\"[A-Z]*\" value=\"[a-z]*\"/>", "");
		//System.out.print(outText);
		
		System.out.print(outText);
		System.out.println(outText.length());
		System.out.println(rawFile.length());
		for(int i=0; i<outText.length()-1; i++) {
			System.out.println(rawFile.charAt(i) + " - " + outText.charAt(i));
			if (rawFile.charAt(i) != outText.charAt(i)) {

				System.out.println(rawFile.substring(i-15, i+15));
				System.out.print("[raw: " + rawFile.charAt(i) + "][ref: " + outText.charAt(i) + "]");
				System.out.print("[posInRef: " + referenceFile.indexOf(outText.charAt(i)) + "]");
				System.out.println("[posInRaw: " + i + "]");
				
				//referenceFile = referenceFile.replaceFirst(String.valueOf(outText.charAt(i)), String.valueOf(rawFile.charAt(i)));
				
				if (runType == "TEST") {
					System.exit(1);
				}
			}
		}
		System.out.println("Finished");

	}

}
