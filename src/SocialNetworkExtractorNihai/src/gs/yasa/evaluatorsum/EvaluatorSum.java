package gs.yasa.evaluatorsum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class EvaluatorSum {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int sum[][] = new int[5][8];
		int i,j;
		for(i=0;i<5;i++){
			for (j=0;j<8;j++){
				sum[i][j] = 0;
			}
		}
		
		if(args.length<2)
		{
			System.out.println("EvaluatorSum by BurcuKupelioglu");
			System.out.println("Usage: java -jar evaluatorsum.jar [input filename] [output filename]");
			System.exit(-1);
		}
		File file = new File(args[0]);
		File out = new File(args[1]);
		if(file.isFile())
		{
			
				//System.out.println("Trying to load \""+file.getName()+"\"");
				FileInputStream fileIn;
				try {
					fileIn = new FileInputStream(file);
					
					InputStreamReader reader = new InputStreamReader(fileIn);
					Scanner scanner = new Scanner(reader);
					
					while(scanner.hasNext()){
						int toolid = scanner.nextInt();
						for(j=0;j<8;j++)
							sum[toolid][j] += scanner.nextInt();
					}
					
					scanner.close();
					reader.close();
					
					FileWriter writer = new FileWriter(out);
					for(i=0;i<5;i++){
						writer.write("\n\nToolid:"+i+"\n");
						for (j=0;j<8;j++){
							writer.write("sum"+j+" = "+sum[i][j]+"\n");
						}
					}
					writer.close();
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					

				
		}
	}

}
