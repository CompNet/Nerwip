package gs.yasa.outputunifier.illinois;

import gs.yasa.outputunifier.OutputReader;
import gs.yasa.sne.common.Annotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class forms an output reader special for Illinois Annotation Tool.
 * @author yasa akbulut
 * @version 1
 */
public class IllinoisOutputReader extends OutputReader{

	
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
		if(debug)
		{
			System.out.println("The annotated string follows:");
			System.out.println(annotatedString);
			System.out.println("---------------\n\n");
		}
		
		//remove the extra and unnecessary spaces (only for illinois) in the document
		annotatedString = fixAddedSpaces(annotatedString);

		if(debug)
		{
			System.out.println("The annotated string after the removal of extra spaces follows:");
			System.out.println(annotatedString);
			System.out.println("---------------\n\n");
		}
		
		//look for annotations
		Pattern searchPattern = Pattern.compile("\\[.+?\\]");
		Matcher matcher = searchPattern.matcher(annotatedString);
		IllinoisAnnotationBuilder annotationBuilder = new IllinoisAnnotationBuilder();
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		while(matcher.find())
		{
			annotations.add(annotationBuilder.build(matcher.group(),
					matcher.start(), matcher.end()));
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
		
		if(debug)
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
		String annotatedString = annotatedText;
		annotatedString=annotatedString.replaceAll("\\( ", "\\(");
		annotatedString=annotatedString.replaceAll(" \\)", "\\)");
		annotatedString=annotatedString.replaceAll(" ,", ",");
		annotatedString=annotatedString.replaceAll(" ;", ";");
		annotatedString=annotatedString.replaceAll(" \\. ", ". ");
		annotatedString=annotatedString.replaceAll("\\] '", "\\]'");
		annotatedString=annotatedString.replaceAll(" 's", "'s");
		annotatedString=annotatedString.replaceAll("\" (.+?) \"", "\"$1\"");
		return annotatedString;
	}
}
