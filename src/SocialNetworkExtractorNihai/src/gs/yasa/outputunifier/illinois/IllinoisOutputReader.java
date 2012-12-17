package gs.yasa.outputunifier.illinois;

import gs.yasa.outputunifier.OutputReader;
import gs.yasa.sne.common.Annotation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class forms an output reader special for Illinois Annotation Tool.
 * @author yasa akbulut
 * @version 1
 */
public class IllinoisOutputReader extends OutputReader{

	
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
	
	
	/* (non-Javadoc)
	 * @see gs.yasa.outputunifier.OutputReader#read(java.io.File)
	 */
	@Override
	public ArrayList<Annotation> read(File infile) throws FileNotFoundException
	{
		//read the file and create a string with the contents
		FileInputStream fileIn = new FileInputStream(infile);
		InputStreamReader reader = new InputStreamReader(fileIn);
		Scanner scanner = new Scanner(reader);
		StringBuilder builder = new StringBuilder();
		while(scanner.hasNextLine())
		{
			builder.append(scanner.nextLine());
		}
		String annotatedString = builder.toString();
		try {
			fileIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return read(annotatedString);
		
	}
	
	/* (non-Javadoc)
	 * @see gs.yasa.outputunifier.OutputReader#read(java.lang.String)
	 */
	@Override
	public ArrayList<Annotation> read(String annotatedString)
	{
		debug = true;
		if(debug)
		{
			try {
				writeFile("/tmp/illinoistooloutfile.txt", annotatedString);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("The annotated string follows:");
			System.out.println(annotatedString);
			System.out.println("---------------\n\n");
		}
		
		//remove the extra and unnecessary spaces (only for illinois) in the document
		annotatedString = fixAddedSpaces(annotatedString);
		
//		try {
//			annotatedString = readFile("/tmp/cleared.txt");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		if(debug)
		{
			System.out.println("The annotated string after the removal of extra spaces follows:");
			System.out.println(annotatedString);
			try {
				writeFile("/tmp/hede.txt", annotatedString);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("---------------\n\n");
		}
		
		//look for annotations
		Pattern searchPattern = Pattern.compile("\\[.+?\\]");
		Matcher matcher = searchPattern.matcher(annotatedString);
		IllinoisAnnotationBuilder annotationBuilder = new IllinoisAnnotationBuilder();
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		while(matcher.find())
		{
			if (matcher.group().contains("  ]")) {
				Annotation annotation = annotationBuilder.build(
						matcher.group(), matcher.start(), matcher.end());
				if (annotation != null) {
					annotations.add(annotation);
				}
			}
		}
		
		if(debug)
		{
			System.out.println("The annotations follow:");
			for (Annotation annotation : annotations)
			{
				System.out.println(annotation);
			}
			System.out.println("Total: "+annotations.size()+" annotation(s)");
			System.out.println("---------------\n\n");
		}
		
		//calculate the positions of the annotations relative to the
		//input document, and fix them
		annotationBuilder.calculateRelativePositions(annotations);
		
		if(debug || true)
		{
			System.out.println("The annotations after the relative position calculation fix follow:");
			for (Annotation annotation : annotations)
			{
				System.out.println(annotation);
			}
			System.out.println("Total: "+annotations.size()+" annotation(s)");
			System.out.println("---------------\n\n");
		}
		
		return annotations;
	}
	
	/**
	 * This method is for reverse engineering, deletes unnecessary spaces
	 * @param annotatedText
	 * @return annotatedString
	 * @author yasa akbulut
	 */
	private String fixAddedSpaces(String annotatedText)
	{
		String propertiesFilePath = "/home/samet/.bin/Dropbox/workspace/config/sne.properties_linux"; 
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(propertiesFilePath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String outputDirectory = prop.getProperty("outputDirectory");
		
		String rawFilePath = outputDirectory + "input";
		
		String rawText = "";
		try {
			rawText = readFile(rawFilePath);
		} catch (IOException e) {
			System.out.println("Raw text file cannot be found at: " + rawFilePath);
			e.printStackTrace();
		}
		CharacterIterator rawFileCharIterator = new StringCharacterIterator(rawText);
		CharacterIterator refFileCharIterator = new StringCharacterIterator(annotatedText);
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
					refChar = refFileCharIterator.next();
				}
			}
			
		}
		String content = stringBuffer.toString();
		return content;
	}
	private static void log(String text) {
		Boolean debug = false;
		if (debug) {
			System.out.println(text);
		}
	}
}
