package gs.yasa.outputunifier.stanfordmanual;


import gs.yasa.outputunifier.OutputReader;
import gs.yasa.sne.common.Annotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class forms an output reader special for Stanford Manual Annotation Tool.
 * @author yasa akbulut
 * @version 1
 */
public class StanfordMATOutputReader extends OutputReader {
	// Samet: I've added this method for debug purposes. 
	// You can remove this when you think it is unnecessary. 
	private static String readFile(String path) throws IOException {
		FileInputStream stream = new FileInputStream(new File(path));
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
	
	/* (non-Javadoc)
	 * @see gs.yasa.outputunifier.OutputReader#read(java.io.File)
	 */
	@Override
	public ArrayList<Annotation> read(File infile) throws FileNotFoundException {
		
		//Samet: Usage of debug method
		String inputFileContent = "";
		try {
			inputFileContent= readFile(infile.getAbsolutePath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//read the file and create a string with the contents
		FileInputStream fileIn = new FileInputStream(infile);
		
		// Samet, toggle the blocks below:
		
		// Old
		//InputStreamReader reader = new InputStreamReader(fileIn);
		// Old finished
		
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
		System.out.println(annotatedString);
		return read(annotatedString);
		
	}

	/* (non-Javadoc)
	 * @see gs.yasa.outputunifier.OutputReader#read(java.lang.String)
	 */
	@Override
	public ArrayList<Annotation> read(String annotatedString) {
		if(debug)
		{
			System.out.println("The annotated string follows:");
			System.out.println(annotatedString);
			System.out.println("---------------\n\n");
		}
		//quick and dirty fix. a better version would be to replace the main regex.
		//i.e. <(.+?)>...
		annotatedString = annotatedString.replaceAll("<tag name=\"PERSON\" value=\"start\"/>", "<PERSON>");
		annotatedString = annotatedString.replaceAll("<tag name=\"PERSON\" value=\"end\"/>", "</PERSON>");
		annotatedString = annotatedString.replaceAll("<tag name=\"ORGANIZATION\" value=\"start\"/>", "<ORGANIZATION>");
		annotatedString = annotatedString.replaceAll("<tag name=\"ORGANIZATION\" value=\"end\"/>", "</ORGANIZATION>");
		annotatedString = annotatedString.replaceAll("<tag name=\"LOCATION\" value=\"start\"/>", "<LOCATION>");
		annotatedString = annotatedString.replaceAll("<tag name=\"LOCATION\" value=\"end\"/>", "</LOCATION>");
		annotatedString = annotatedString.replaceAll("<tag name=\"MISC\" value=\"start\"/>", "<MISC>");
		annotatedString = annotatedString.replaceAll("<tag name=\"MISC\" value=\"end\"/>", "</MISC>");
		annotatedString = annotatedString.replaceAll("<tag name=\"DATE\" value=\"start\"/>", "<DATE>");
		annotatedString = annotatedString.replaceAll("<tag name=\"DATE\" value=\"end\"/>", "</DATE>");
		System.out.println(annotatedString);
		
		//"<(.+?)>(.+?)<.+?>" causes problems with newline inside tags
		//we have to use Pattern.DOTALL to deal with that
		
		Pattern searchPattern = Pattern.compile("<(.+?)>(.+?)</.+?>",Pattern.DOTALL);
		Matcher matcher = searchPattern.matcher(annotatedString);
		StanfordMATAnnotationBuilder annotationBuilder = new StanfordMATAnnotationBuilder();
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		while(matcher.find())
		{
			annotations.add(annotationBuilder.build(matcher.group(2), matcher.group(1),
					matcher.start(), matcher.end(), matcher.group()));
			System.out.println(matcher.group(2) + " - " + matcher.group(1) + " "
					+ matcher.start() + " - " + matcher.end() + "-" + matcher.group());
		}
		annotationBuilder.calculateRelativePositions(annotations);
		return annotations;
	}

}
