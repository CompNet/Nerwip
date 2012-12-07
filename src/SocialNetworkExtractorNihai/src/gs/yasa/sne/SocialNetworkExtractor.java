package gs.yasa.sne;


import gs.yasa.aggregator.Aggregator;
import gs.yasa.annotationviewer.AnnotationViewer;
import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;
import gs.yasa.sne.nertools.DateParserNERTool;
import gs.yasa.sne.nertools.IllinoisNERTool;
import gs.yasa.sne.nertools.LinkedEntityRecognizerNERTool;
import gs.yasa.sne.nertools.NERTool;
import gs.yasa.sne.nertools.OpenCalaisNERTool;
import gs.yasa.sne.nertools.StanfordNERTool;
import gs.yasa.wikipediareader.WikipediaReader;
import gs.yasa.wikipediareader.WikipediaReaderException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Scanner;




/**
 * This class generates annotations by all of annotation tools. Unifies their output.
 * Compares them with eachother through votes, and generates a new annotation.
 * @author yasa akbulut
 * @version 1
 *
 */
@SuppressWarnings("unused")
public class SocialNetworkExtractor {

	
	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    
		    //Samet, toggle commented lines.
		    //return Charset.defaultCharset().decode(bb).toString();
		    return Charset.forName("UTF-8").decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}
	

	/**
	 * This method calls different methods of different classes. First WikipediaReader is executed to read a Wikipedia article.
	 * Then, the article is annotated by each tool. Their outputs are unified then
	 * aggregated.
	 * @author yasa akbulut
	 * @version 1
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) {
		
		
		
		
		// TODO Auto-generated method stub
		
		// TODO: argument checking
		//String entryPoint = "http://en.wikipedia.org/wiki/Aleksander_Kwa%C5%9Bniewski";
		//String entryPoint = "http://en.wikipedia.org/wiki/Jerzy_Buzek";
//		String entryPoint = "http://en.wikipedia.org/wiki/Kazimierz_Marcinkiewicz";

		//String entryPoint ="http://en.wikipedia.org/wiki/Hans-Gert_P%C3%B6ttering";
		//String entryPoint = "http://en.wikipedia.org/wiki/Joseph_Daul";
		WikipediaReader reader = new WikipediaReader(true);
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		try {
			
			//FileWriter debugFile = new FileWriter("debug");
			/*
			String text = readFile("/home/burcu/Desktop/Annotation/annotations/Aart_Kemink/Aart_Kemink");
			String textWithLinks = readFile("/home/burcu/Desktop/Annotation/annotations/Aart_Kemink/Aart_Kemink(withLinks)");
			*/
			
			//String propertiesFilePath = "C:\\Users\\samet\\Dropbox\\workspace\\sne.properties";
			String propertiesFilePath = "/home/samet/.bin/Dropbox/workspace/sne.properties_linux"; 
			Properties prop = new Properties();
			prop.load(new FileInputStream(propertiesFilePath));
			
			String assetDirectory = prop.getProperty("assetDirectory");
			//String articleTitle = prop.getProperty("articleTitle");
			String articleTitle = args[0];
			String outputDirectory = prop.getProperty("outputDirectory");
			
			String rawFilePath = assetDirectory + articleTitle + File.separator + articleTitle;
			
			
			String text = readFile(rawFilePath);
			String textWithLinks = readFile(rawFilePath + "(withLinks)");
			
			//for online mode uncomment these 2 lines
//			String text = reader.getText(entryPoint);
//			String textWithLinks = reader.getTextWithLinks(entryPoint);
			
//			debugFile.append("The article "+entryPoint+" contained the following text:\n");
//			debugFile.append(text);
//			debugFile.append("\n\n\nThe article "+entryPoint+" contained the following text with links:\n");
//			debugFile.append(text);
			
			
			
			//initialize the tools
			ArrayList<NERTool> tools = new ArrayList<NERTool>();
			tools.add(new StanfordNERTool());
			tools.add(new IllinoisNERTool());
			tools.add(new OpenCalaisNERTool());
			//tools.add(new LinkedEntityRecognizerNERTool());
			tools.add(new DateParserNERTool());
			//perform the annotations
			for (NERTool nerTool : tools) {
				try
				{
					if(nerTool instanceof DateParserNERTool)
						System.out.println("parser");
					ArrayList<Annotation> result = new ArrayList<Annotation>();
					if(nerTool instanceof LinkedEntityRecognizerNERTool)
						result = nerTool.annotate(textWithLinks);
					else	
						result = nerTool.annotate(text);
					annotations.addAll(result);
				}catch(NullPointerException e)
				{
					e.printStackTrace();
				}
			}
			Aggregator aggregator = new Aggregator();
			@SuppressWarnings("unchecked")
			ArrayList<Annotation> clone = (ArrayList<Annotation>)annotations.clone();
			ArrayList<Annotation> aggregatedAnnotations = aggregator.aggregate(clone);
			annotations.addAll(aggregatedAnnotations);
//			debugFile.close();
			
		
			//AnnotationViewer viewer = new AnnotationViewer();
			//viewer.showAnnotations(annotations, text);
		
			//String outputDirectory = "/home/samet/.bin/Dropbox/workspace/data/clearance/tmp/";
			
			//uncomment to output the files
			HashMap<NERTool, FileOutputStream> outputStreams = new HashMap<NERTool, FileOutputStream>();
			for (NERTool nerTool : tools) {
				try {
					outputStreams.put(nerTool, new FileOutputStream(outputDirectory + nerTool.getName() + ".default"));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			System.out.println("===================================================================");
			for (Entry<NERTool, FileOutputStream> outputStream : outputStreams.entrySet()) {
				try {
					ObjectOutputStream objectStream = new ObjectOutputStream(outputStream.getValue());
					for (Annotation annotation : annotations) {
						if(annotation.getSource().equals(outputStream.getKey().getName()))
						{
							objectStream.writeObject(annotation);
							System.out.println(annotation);
						}
					}
					objectStream.flush();
					objectStream.close();
					outputStream.getValue().flush();
					outputStream.getValue().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("===================================================================");
			
			//uncomment to output aggregated annotations to a file
			try {
				String aggregatorOutputPath = outputDirectory + "AGGREGATOR.default";
				FileOutputStream fos1 = new FileOutputStream(aggregatorOutputPath);
				ObjectOutputStream oos1 = new ObjectOutputStream(fos1);
				for (Annotation annotation : annotations) {
					if(annotation.getSource().equals(AnnotationTool.AGGREGATOR))
						oos1.writeObject(annotation);
				}
				oos1.flush();
				oos1.close();
				fos1.flush();
				fos1.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//uncomment to output all annotations to a file
//			try {
//				FileOutputStream fos1 = new FileOutputStream("annotations");
//				ObjectOutputStream oos1 = new ObjectOutputStream(fos1);
//				for (Annotation annotation : annotations) {
//					oos1.writeObject(annotation);
//				}
//				oos1.flush();
//				oos1.close();
//				fos1.flush();
//				fos1.close();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
			
//			for (Annotation annotation : annotations) {
//				System.out.println(annotation);
//			}
			
			//AnnotationViewer av = new AnnotationViewer();
			//av.showAnnotations(annotations, text);
			
//		} catch (WikipediaReaderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
