package gs.yasa.articleretriever;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import gs.yasa.wikipediareader.WikipediaReader;
import gs.yasa.wikipediareader.WikipediaReaderException;


/**
 * This is class is same as WikipediaReader Class except this class saves two output
 * files in a local adress.
 * @author yasa akbulut
 * @version 1
 */
public class ArticleRetriever {


	/**
	 * Generates two text files and saves them into a local adress.
	 * @param args
	 * @author yasa akbulut
	 */
	public static void main(String[] args) {
		if(args.length<1)
		{
			System.out.println("Usage: java -jar articleRetriever.jar link_to_wikipedia_article");
			System.out.println("This program produces two files: one containing the textual content of an article, " +
					"and the other containing the textual content with hyperlinks.");
			System.out.println("The word count displayed by the program is approximative.");
			System.exit(-1);
		}
		String outfile = "";
		String[] splitted = args[0].split("/");
		outfile = splitted[splitted.length-1];

		WikipediaReader reader = new WikipediaReader();
		try {
			String text = reader.getText(args[0]);
			String textWithLinks = reader.getTextWithLinks(args[0]);
			FileOutputStream outStream = new FileOutputStream(outfile);
			OutputStreamWriter writer = new OutputStreamWriter(outStream);
			writer.write(text);
			writer.close();
			outStream.close();
			
			System.out.println("Article text saved in: "+ outfile);
			FileOutputStream outStream2 = new FileOutputStream(outfile+"(withLinks)");
			writer = new OutputStreamWriter(outStream2);
			writer.write(textWithLinks);
			writer.close();
			outStream2.close();
			System.out.println("Article text with links saved in: "+ outfile + "(withLinks)");
			int wordCount = text.length() - text.replaceAll(" ", "").replaceAll("\n", "").length();
			System.out.println("Article contains "+wordCount+" words. (approximate value)");
		} catch (WikipediaReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
