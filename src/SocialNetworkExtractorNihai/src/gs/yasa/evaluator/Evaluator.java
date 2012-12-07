package gs.yasa.evaluator;


import gs.yasa.evaluator.gui.MainWindow;
import gs.yasa.evaluator.gui.TypeResult;
import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationType;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import javax.tools.Tool;


/**
 * This class compares an annotation from a tool with a manual annotated text and shows
 * them in a panel
 * @author yasa akbulut
 * @version 1
 */
public class Evaluator {
	static boolean numbersonly = false;
	/**
	 * Compares two text passed as parameters and generates a result list and posts
	 * them in a panel
	 * @param args
	 * @author yasa akbulut
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {

//		if(args.length<1)
//		{
//			System.out.println("Evaluator by Yasa Akbulut");
//			System.out.println("Usage: java -jar evaluator.jar [input directory] [-o (numbers only)]");
//			System.exit(-1);
//		}else if(args.length>1){
//			numbersonly = true;
//		}
		String propertiesFilePath = "/home/samet/.bin/Dropbox/workspace/config/sne.properties_linux"; 
		Properties prop = new Properties();
		prop.load(new FileInputStream(propertiesFilePath));
		
		String outputDirectory = prop.getProperty("outputDirectory");
		
		File directory = new File(outputDirectory);
		File[] files = directory.listFiles();
		ArrayList<ToolOutput> tools = new ArrayList<ToolOutput>();
		ArrayList<Annotation> reference = new ArrayList<Annotation>();
		for (File file : files) {
			if(file.isFile())
			{
				try {
					//System.out.println("Trying to load \""+file.getName()+"\"");
					FileInputStream fileIn = new FileInputStream(file);
					if(file.getName().equalsIgnoreCase("input"))
					{
						//Samet: switch the lines below for original.
						InputStreamReader reader = new InputStreamReader(fileIn, "UTF8");
						//InputStreamReader reader = new InputStreamReader(fileIn);
						Scanner scanner = new Scanner(reader);
						scanner.close();
						reader.close();

					}else
					{
						ObjectInputStream objectIn = new ObjectInputStream(fileIn);
						if(file.getName().contains("reference"))
						{
							//System.out.println("Loading Reference..");
							Annotation tempAnnotation;
							while((tempAnnotation=(Annotation)objectIn.readObject())!=null)
							{
								reference.add(tempAnnotation);
							}
						}else
						{

							String[] splittedFileName = file.getName().split("\\.");
							ToolOutput tempOutput = new ToolOutput(splittedFileName[0], splittedFileName[1]);
							//System.out.println("The tool \""+splittedFileName[0]+"\" with the configuration \""+splittedFileName[1]+"\" is loading..");
							tools.add(tempOutput);
							Annotation tempAnnotation;
							while((tempAnnotation=(Annotation)objectIn.readObject())!=null)
							{
								tempOutput.annotations.add(tempAnnotation);
							}
						}
						objectIn.close();
						fileIn.close();
					}
				} catch(EOFException e)
				{
					//System.out.println("End of \""+file.getName()+"\" reached.");
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

//		System.out.println("Here's the reference:");
//		for (Annotation annotation : reference) {
//			System.out.println(annotation);
//		}
//		System.out.println("Here are the tools:");
//		for (ToolOutput tool : tools) {
//			System.out.println("Tool \""+tool.getToolName()+"\" with configuration \""+tool.getToolConfiguration()+"\" contains the following:");
//			for (Annotation annotation : tool.annotations) {
//				System.out.println(annotation);
//			}
//		}
		
		
		for (ToolOutput toolOutput: tools) {
			HashMap<String, Integer> detail = new HashMap<String, Integer>();
			detail.put("correctType", 0);
			detail.put("incorrectType", 0);
		}
		
		for (ToolOutput toolOutput : tools) {
			HashMap<String, Integer> detail = new HashMap<String, Integer>();
			ArrayList<Annotation> removeList = new ArrayList<Annotation>();
			ArrayList<Annotation> spokenFor = new ArrayList<Annotation>();
			
			//TRUE POSITIVES
			for (Annotation referenceAnnotation : reference) {
				for (Annotation toolAnnotation : toolOutput.annotations) {
					if(referenceAnnotation.getStartPos()== toolAnnotation.getStartPos()
						&& referenceAnnotation.getEndPos() == toolAnnotation.getEndPos())
					{
						EvaluationOutcome outcome = new EvaluationOutcome();
						outcome.outcomeClass = OutcomeClasses.TRUE_POSITIVE;
						if(referenceAnnotation.getEntityType() == toolAnnotation.getEntityType()) {
							outcome.correctType = true;
							detail.put("correctType", detail.get("correctType"));
						} else {
							outcome.correctType = false;
						}
						
						
						toolOutput.results.add(outcome);
						removeList.add(toolAnnotation);
						if(!spokenFor.contains(referenceAnnotation))
							spokenFor.add(referenceAnnotation);
					}

				}
			}
			for (Annotation annotation : removeList) {
				toolOutput.annotations.remove(annotation);
			}
			removeList.clear();
			
			//EXCESS POSITIVES
			for (Annotation referenceAnnotation : reference) {
				for (Annotation toolAnnotation : toolOutput.annotations) {
					if(toolAnnotation.containsExclusive(referenceAnnotation))
					{
						EvaluationOutcome outcome = new EvaluationOutcome();
						outcome.outcomeClass = OutcomeClasses.EXCESS_POSITIVE;
						if(referenceAnnotation.getEntityType()==toolAnnotation.getEntityType())
							outcome.correctType = true;
						else
							outcome.correctType = false;
						toolOutput.results.add(outcome);
						removeList.add(toolAnnotation);
						if(!spokenFor.contains(referenceAnnotation))
							spokenFor.add(referenceAnnotation);
					}
				}
			}
			for (Annotation annotation : removeList) {
				toolOutput.annotations.remove(annotation);
			}
			removeList.clear();
			
			//PARTIAL POSITIVES & IRREGULARS
			for (Annotation referenceAnnotation : reference) {
				for (Annotation toolAnnotation : toolOutput.annotations) {
					if(toolAnnotation.overlapsWith(referenceAnnotation)
							&&!toolAnnotation.containsExclusive(referenceAnnotation))
					{
						boolean containsOther = false;
						for (Annotation annotation : reference) {
							if(toolAnnotation.containsExclusive(annotation)
									||toolAnnotation.overlapsWith(annotation))
							{
								containsOther = (annotation != referenceAnnotation); 
							}
						}
						
						EvaluationOutcome outcome =  new EvaluationOutcome();
						if(containsOther)
						{
							outcome.outcomeClass = OutcomeClasses.IRREGULAR;
						}else
						{
							outcome.outcomeClass = OutcomeClasses.PARTIAL_POSITIVE;
						}
						if(referenceAnnotation.getEntityType()==toolAnnotation.getEntityType())
							outcome.correctType = true;
						else
							outcome.correctType = false;
						toolOutput.results.add(outcome);
						removeList.add(toolAnnotation);
						if(!spokenFor.contains(referenceAnnotation))
							spokenFor.add(referenceAnnotation);
					}
				}
			}
			for (Annotation annotation : removeList) {
				toolOutput.annotations.remove(annotation);
			}
			removeList.clear();
			

			
			//the rest are false positives.
			for (Annotation annotation : toolOutput.annotations) {
				EvaluationOutcome outcome = new EvaluationOutcome();
				outcome.outcomeClass = OutcomeClasses.FALSE_POSITIVE;
				toolOutput.results.add(outcome);
				removeList.add(annotation);
			}
			for (Annotation annotation : removeList) {
				toolOutput.annotations.remove(annotation);
			}
			removeList.clear();
			
			//FALSE NEGATIVES
			for (Annotation annotation : reference) {
				if(!spokenFor.contains(annotation))
				{
					/*
					 * We don't count entities of type DATE as false negatives for DATEPARSER tool 
					 */
					if((toolOutput.getToolName().equals("DATEPARSER") && annotation.getEntityType() == AnnotationType.DATE)||
							(!toolOutput.getToolName().equals("DATEPARSER") && annotation.getEntityType() != AnnotationType.DATE)){
						EvaluationOutcome outcome = new EvaluationOutcome();
						outcome.outcomeClass = OutcomeClasses.FALSE_NEGATIVE;
						toolOutput.results.add(outcome);
					}
				}
			} 
			

			HashMap<OutcomeClasses, TypeResult> results;
			results = new HashMap<OutcomeClasses, TypeResult>();
			for (EvaluationOutcome outcome: toolOutput.results) {
				if(results.containsKey(outcome.outcomeClass))
				{
					if(outcome.correctType)
						results.get(outcome.outcomeClass).correctType++;
					else
						results.get(outcome.outcomeClass).incorrectType++;
				}else
				{
					results.put(outcome.outcomeClass, new TypeResult());
					if(outcome.correctType)
						results.get(outcome.outcomeClass).correctType++;
					else
						results.get(outcome.outcomeClass).incorrectType++;
				}
			}
			
			
			for(OutcomeClasses outcomeClass: OutcomeClasses.values())
			{
				if(!results.containsKey(outcomeClass))
					results.put(outcomeClass, new TypeResult());
			}
			
			if (numbersonly) {
				if (toolOutput.getToolName().equalsIgnoreCase("STANFORD")) {
					System.out.println(0);
				} else if (toolOutput.getToolName()
						.equalsIgnoreCase("TAGLINKS")) {
					System.out.println(1);
				} else if (toolOutput.getToolName().equalsIgnoreCase(
						"OPENCALAIS")) {
					System.out.println(2);
				} else if (toolOutput.getToolName().equalsIgnoreCase(
						"AGGREGATOR")) {
					System.out.println(3);
				} else if (toolOutput.getToolName().equalsIgnoreCase(
						"DATEPARSER")) {
					System.out.println(4);
				}

				System.out
						.println(results.get(OutcomeClasses.TRUE_POSITIVE).correctType);
				System.out
						.println(results.get(OutcomeClasses.TRUE_POSITIVE).incorrectType);
				System.out
						.println(results.get(OutcomeClasses.PARTIAL_POSITIVE).correctType);
				System.out
						.println(results.get(OutcomeClasses.PARTIAL_POSITIVE).incorrectType);
				System.out
						.println(results.get(OutcomeClasses.EXCESS_POSITIVE).correctType);
				System.out
						.println(results.get(OutcomeClasses.EXCESS_POSITIVE).incorrectType);
				System.out
						.println(results.get(OutcomeClasses.FALSE_POSITIVE).correctType
								+ results.get(OutcomeClasses.FALSE_POSITIVE).incorrectType);
				System.out
						.println(results.get(OutcomeClasses.FALSE_NEGATIVE).correctType
								+ results.get(OutcomeClasses.FALSE_NEGATIVE).incorrectType);
			} else {
				System.out.println(toolOutput.getToolName());

				System.out
						.println("true positives correct_type: "
								+ results.get(OutcomeClasses.TRUE_POSITIVE).correctType);
				System.out
						.println("true positives incorrect_type: "
								+ results.get(OutcomeClasses.TRUE_POSITIVE).incorrectType);
				System.out
						.println("partial positives correct_type: "
								+ results.get(OutcomeClasses.PARTIAL_POSITIVE).correctType);
				System.out
						.println("partial positives incorrect_type: "
								+ results.get(OutcomeClasses.PARTIAL_POSITIVE).incorrectType);
				System.out
						.println("excess positives correct_type: "
								+ results.get(OutcomeClasses.EXCESS_POSITIVE).correctType);
				System.out
						.println("excess positives incorrect_type: "
								+ results.get(OutcomeClasses.EXCESS_POSITIVE).incorrectType);
				System.out
						.println("false positives correct/incorrect_type: "
								+ results.get(OutcomeClasses.FALSE_POSITIVE).correctType
								+ results.get(OutcomeClasses.FALSE_POSITIVE).incorrectType);
				System.out
						.println("false negatives correct/incorrect_type: "
								+ results.get(OutcomeClasses.FALSE_NEGATIVE).correctType
								+ results.get(OutcomeClasses.FALSE_NEGATIVE).incorrectType);
			}
		}

		MainWindow mainWindow = new MainWindow();
		for (ToolOutput toolOutput2 : tools) {
			mainWindow.addResultsTab(toolOutput2);
		}
		
		
		mainWindow.show();
		mainWindow.addChartTab();
		System.out.println(mainWindow.getGoogleChartCode());
		
	}
	
	
	

}
