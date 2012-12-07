
package gs.yasa.outputunifier;

import gs.yasa.outputunifier.opencalais.OpenCalaisOutputReader;
import gs.yasa.outputunifier.stanford.StanfordOutputReader;
import gs.yasa.outputunifier.stanfordmanual.StanfordMATOutputReader;
import gs.yasa.sne.common.Annotation;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
/**
 * This class takes an annotated file, finds its tool, according to tool selects an
 * output reader and unifies the output as a serialized object.
 * @author yasa akbulut
 * @version 1
 */
public class OutputUnifier {
  /**
   * This method selects which of the output readers will be used.
 * @param input
 * @return
 * @throws UnsupportedToolException
 * @author yasa akbulut
 */
private static OutputReader selectOutputReader(String input) throws UnsupportedToolException
  {
		OutputReader reader;
		if(input.equalsIgnoreCase("stanfordmanual"))
		{
			reader=new StanfordMATOutputReader();
		}else
		{
			if(input.equalsIgnoreCase("stanford")||input.equalsIgnoreCase("reference"))
			{
				reader = new StanfordOutputReader();
			}else
			{
				if(input.equalsIgnoreCase("opencalais"))
				{
					reader = new OpenCalaisOutputReader();
				}else
				{
					throw new UnsupportedToolException(input);
				}
			}
		}
		return reader;
  }

  /**
   * This method creates a serialized object based on each annotation tool's
   * specifications.
   * @param args
   * @author yasa akbulut
 * @throws IOException 
 * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException, IOException
  {
		OutputReader reader = null;
		File infile = null;
		File outfile = null;
		if(args.length<1)
		{
			printUsage();
			System.exit(-1);
		}else
		{
			
			String propertiesFilePath = "/home/samet/.bin/Dropbox/workspace/sne.properties_linux"; 
			Properties prop = new Properties();
			prop.load(new FileInputStream(propertiesFilePath));
			
			String assetDirectory = prop.getProperty("assetDirectory");
			String articleTitle = args[1];
			String outputDirectory = prop.getProperty("outputDirectory");
			String referenceFilePath = assetDirectory + File.separator + articleTitle + File.separator + articleTitle + "_new_reference.txt";
			String unifiedReferenceFilePath = outputDirectory + File.separator + articleTitle + "_unified_reference.default";
			
			try {
				if(args[0].equalsIgnoreCase("auto"))
				{
					File inputDir = new File(referenceFilePath);
					File outputDir = new File(unifiedReferenceFilePath);
					if(inputDir.isDirectory()&&outputDir.isDirectory())
					{
						File[] inputFiles = inputDir.listFiles();
						for (File inputFile : inputFiles) {
							if(inputFile.isFile())
							{
								String[] splittedName = inputFile.getName().split("\\.");
								ArrayList<String> arguments =  new ArrayList<String>();
								arguments.add(splittedName[0]);
								arguments.add(inputFile.getAbsolutePath());
								arguments.add(outputDir.getAbsolutePath()+"/"+inputFile.getName());
								System.out.println("Calling the tool "+arguments.get(0)+" on the file "+arguments.get(1)+" and will be outputted to "+arguments.get(2)+"\n\n");
								String[] argumentList = new String[3];
								argumentList =  arguments.toArray(argumentList);
								main(argumentList);
								
							}
						}
					}
					System.exit(0);
				}else
				{
					reader = selectOutputReader(args[0]);
					reader.debug=true;
					
					infile = new File(referenceFilePath);
//					if(args.length==3)
//						outfile = new File(args[2]);
					outfile = new File(unifiedReferenceFilePath);
					
					System.out.println(outfile);
					System.out.println(infile);
				}
			} catch (UnsupportedToolException e) {
				System.out.println(e.getMessage());
				System.exit(-2);
			}
		}
		
		
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		try {
			annotations.addAll(reader.read(infile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (NullPointerException n1)
		{
			n1.printStackTrace();
		}
		
		for (Annotation annotation : annotations) {
			System.out.println(annotation);
			System.out.println("bittim normalde.");
		}
		
		
		
	
		if(outfile!=null)
		{
			try {
//				FileWriter fstream = new FileWriter(outfile + ".txt");
//				BufferedWriter out = new BufferedWriter(fstream);
//				for (Annotation annotation : annotations) {
//					out.write(annotation.toString() + "\n");
//				}
//				//Close the output stream
//				out.close();
				
				FileOutputStream fileOut = new FileOutputStream(outfile);
				ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
				for (Annotation annotation : annotations) {
					objectOut.writeObject(annotation);
				}
				objectOut.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
  }

  /**
 * User guide method
 * @author yasa akbulut
 */
private static void printUsage()
  {
		// TODO Auto-generated method stub
		System.out.println("OutputUnifier - by Yasa Akbulut");
		System.out.println("Usage: OutputUnifier [tool] [input file] [output file]");
		System.out.println("where [tool] is one of the following: illinois stanford opencalais reference");
		System.out.println("or");
		System.out.println("       OutputUnifier auto [input directory] [output directory]");
		
		
		
  }

}
