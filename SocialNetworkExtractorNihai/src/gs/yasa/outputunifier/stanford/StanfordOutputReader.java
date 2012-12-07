package gs.yasa.outputunifier.stanford;

import gs.yasa.outputunifier.OutputReader;
import gs.yasa.sne.common.Annotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * This class forms an output reader special for Stanford Annotation Tool.
 * @author yasa akbulut
 * @version 1
 */
public class StanfordOutputReader extends OutputReader {
	
	/* (non-Javadoc)
	 * @see gs.yasa.outputunifier.OutputReader#read(java.io.File)
	 */
	@Override
	public ArrayList<Annotation> read(File infile) throws FileNotFoundException {
		//read the file and create a string with the contents
		FileInputStream fileIn = new FileInputStream(infile);
		// Samet, toggle blocks below. 
		
		// Old
		//InputStreamReader reader = new InputStreamReader(fileIn);
		// Old finished.	
		
		// New
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(fileIn, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// New finished.
		
		Scanner scanner = new Scanner(reader);
		StringBuilder builder = new StringBuilder();
		while(scanner.hasNextLine())
		{
			builder.append(scanner.nextLine()+" ");
		}
		try {
			fileIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scanner.close();
		String annotatedString = builder.toString();
		return read(annotatedString);
		
	}

	/* (non-Javadoc)
	 * @see gs.yasa.outputunifier.OutputReader#read(java.lang.String)
	 */
	@Override
	public ArrayList<Annotation> read(String annotatedString) {
		//if(debug)
		if(true)
		{
			System.out.print("[Stanford]");
			System.out.println("The annotated string follows:");
			System.out.println(annotatedString);
			System.out.println("---------------\n\n");
		} 
		//"<(.+?)>(.+?)<.+?>" causes problems with newline inside tags
		//we have to use Pattern.DOTALL to deal with that
		
		Pattern searchPattern = Pattern.compile("<(.+?)>(.+?)</.+?>",Pattern.DOTALL);
		Matcher matcher = searchPattern.matcher(annotatedString);
		StanfordAnnotationBuilder annotationBuilder = new StanfordAnnotationBuilder();
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		while(matcher.find())
		{
			annotations.add(annotationBuilder.build(matcher.group(2), matcher.group(1),
					matcher.start(), matcher.end(), matcher.group()));
		}
		annotationBuilder.calculateRelativePositions(annotations);
		return annotations;
	}

}
