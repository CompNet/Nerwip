package gs.yasa.annotationviewer;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

import javax.swing.text.BadLocationException;

import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;



/**
 * This class sets annotations style, show configurations for each tool. Then shows
 * a file on a frame, with its annotations and tool configuration.
 * @author yasa akbulut
 * @version 1
 */
public class AnnotationViewer {


	/**
	 * Takes an input file as parameter. Sets annotations' style and shows them on
	 * a frame with the input text. Adds a tab for each tool.Shows configuration for
	 * each tool.
	 * @param args
	 * @author yasa akbulut
	 */
	public static void main(String[] args) throws BadLocationException {
		if(args.length<1)
		{
			System.out.println("Usage: java -jar AnnotationViewer.jar input_directory/");
			System.exit(-1);
		}
		File directory = new File(args[0]);
		File[] files = directory.listFiles();
		HashMap<String, ArrayList<Annotation>> toolAnnotations = new HashMap<String, ArrayList<Annotation>>();
		String inputText = "";
		for (File file : files) {
			if(file.isFile())
			{
				try {
					System.out.println("Trying to load \""+file.getName()+"\"");
					FileInputStream fileIn = new FileInputStream(file);
					if(file.getName().equalsIgnoreCase("input"))
					{
						//Samet: switch the lines below. 
						InputStreamReader reader = new InputStreamReader(fileIn, "UTF8");
						//InputStreamReader reader = new InputStreamReader(fileIn);
						Scanner scanner = new Scanner(reader);
						StringBuilder stringBuilder = new StringBuilder();
						while(scanner.hasNextLine())
						{
							stringBuilder.append(scanner.nextLine());
							stringBuilder.append('\n');
						}
						inputText = stringBuilder.toString();
						scanner.close();
						reader.close();

					}else
					{
						ObjectInputStream objectIn = new ObjectInputStream(fileIn);
						

						String[] splittedFileName = file.getName().split("\\.");
						System.out.println("The tool \""+splittedFileName[0]+"\" with the configuration \""+splittedFileName[1]+"\" is loading..");
						
						
						ArrayList<Annotation> tempAnnotations = new ArrayList<Annotation>();
						Annotation tempAnnotation = null;
						try{
							while((tempAnnotation=(Annotation)objectIn.readObject())!=null)
							{
								tempAnnotations.add(tempAnnotation);
							}
						}catch(EOFException e)
						{
							toolAnnotations.put(file.getName(), tempAnnotations);
							System.out.println(toolAnnotations.size());
							objectIn.close();
							fileIn.close();
							System.out.println("End of \""+file.getName()+"\" reached.");
						}
						
						
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		MainWindow mainWindow = new MainWindow();
		for (Entry<String, ArrayList<Annotation>> annotations : toolAnnotations.entrySet()) {
			String[] splittedToolName = annotations.getKey().split("\\.");
			mainWindow.addAnnotationTab(inputText, annotations.getValue(), splittedToolName[0], splittedToolName[1]);
		}
		mainWindow.show();
	}
	
	/**
	 * This method takes an input text and a list of annotations. Shows in a frame
	 * the text with annotations on it.
	 * @param annotations
	 * @param inputText
	 * @author yasa akbulut
	 */
	public void showAnnotations(ArrayList<Annotation> annotations, String inputText)
	{
		MainWindow mainWindow =  new MainWindow();
		HashMap<AnnotationTool, ArrayList<Annotation>> toolAnnotations = new HashMap<AnnotationTool, ArrayList<Annotation>>();
		for (Annotation annotation : annotations) {
			if(!toolAnnotations.containsKey(annotation.getSource()))
				toolAnnotations.put(annotation.getSource(), new ArrayList<Annotation>());	
			toolAnnotations.get(annotation.getSource()).add(annotation);
		}
		for (Entry<AnnotationTool, ArrayList<Annotation>> tool : toolAnnotations.entrySet()) {
			mainWindow.addAnnotationTab(inputText, tool.getValue(),
					tool.getKey().name(), "default");
		}
		mainWindow.show();
	}
}
