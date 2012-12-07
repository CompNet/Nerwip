package gs.yasa.sne.nertools;


import gs.yasa.outputunifier.illinois.IllinoisOutputReader;
import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;

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
import java.util.ArrayList;
import java.util.Scanner;

import LbjTagger.NETagPlain;
import LbjTagger.Parameters;
import LbjTagger.ParametersForLbjCode;






public class IllinoisNERTool implements NERTool {

	private String config;
	
	public IllinoisNERTool(String config)
	{
		this.config = config;
	}
	
	public IllinoisNERTool()
	{
		this("lib/nertools/illinois/Config/allLayer1.config");
		//this("Config/allLayer1.config");
	}
	
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
	
	public String runTool(String text)
	{
		Parameters.readConfigAndLoadExternalData(config);
		ParametersForLbjCode.forceNewSentenceOnLineBreaks=true;
		FileOutputStream stream;
		String annotatedString ="";
		try {
			String inputFileName = "/tmp/"+System.currentTimeMillis();
			String outputFileName = "/tmp/out-"+System.currentTimeMillis();
			stream = new FileOutputStream(inputFileName);
			OutputStreamWriter writer = new OutputStreamWriter(stream);
			writer.write(text);
			writer.close();
			stream.close();
			
//			writeFile(inputFileName, text);
			
			NETagPlain.tagFile(inputFileName, outputFileName, false);
			FileInputStream instream = new FileInputStream(outputFileName);
			InputStreamReader reader = new InputStreamReader(instream);
			Scanner scanner = new Scanner(reader);
			StringBuilder builder = new StringBuilder();
			while(scanner.hasNextLine())
			{
				builder.append(scanner.nextLine());
			}
			annotatedString = builder.toString();
//			annotatedString = readFile(outputFileName);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return annotatedString;
	}

	@Override
	public ArrayList<Annotation> annotate(String text) {
		String annotatedString=runTool(text);
		IllinoisOutputReader reader = new IllinoisOutputReader();
		return reader.read(annotatedString);
	}

	@Override
	public AnnotationTool getName() {
		// TODO Auto-generated method stub
		return AnnotationTool.ILLINOIS;
	}
	
}
