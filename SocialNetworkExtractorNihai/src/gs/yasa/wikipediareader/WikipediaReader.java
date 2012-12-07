package gs.yasa.wikipediareader;

import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.AbstractNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * This class generates two text from a Wikipedia URL. One is brute text. The second
 * is a text with links.
 * @author yasa akbulut
 * @version 1
 */
public class WikipediaReader {

	private boolean debug;
	
	/**
	 * Constructor
	 * @param debug
	 * @author yasa akbulut
	 */
	public WikipediaReader(boolean debug)
	{
		this.debug = debug;
	}

	/**
	 * Constructor
	 * @author yasa akbulut
	 */
	public WikipediaReader()
	{
		this(false);
	}

	public static void main(String args[])
	{
		if(args.length!=1)
		{
			printUsage();
			System.exit(0);
		}
		WikipediaReader r = new WikipediaReader();
		try {
			System.out.println(r.processArticle(args[0], false));
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Creates a brute text file from a Wikipedia URL
	 * @param url
	 * @return
	 * @throws WikipediaReaderException
	 * @author yasa akbulut
	 */
	public String getText(String url) throws WikipediaReaderException
	{
		try {
			return processArticle(url, false);
		} catch (ParserException e) {
			throw new WikipediaReaderException(e.getMessage());
		}
	}

	/**
	 * Creates a text file with links from a Wikipedia URL
	 * @param url
	 * @return
	 * @throws WikipediaReaderException
	 * @author yasa akbulut
	 */
	public String getTextWithLinks(String url) throws WikipediaReaderException
	{
		try {
			return processArticle(url, true);
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			throw new WikipediaReaderException(e.getMessage());
		}
	}

	/**
	 * Pulls a text from a Wikipedia URL without images, tags, etc.
	 * @param url
	 * @param links
	 * @return
	 * @throws ParserException
	 * @author yasa akbulut
	 */
	private String processArticle(String url, boolean links) throws ParserException
		{
			long startTime = System.currentTimeMillis();
			Parser parser = new Parser(url);
			long endTime = System.currentTimeMillis();
			if(debug)
				System.out.println("It took "+(endTime-startTime)+" ms to parse the text.");
			NodeList paragraphList = parser.parse(new TagNameFilter("p"));
			StringBuilder stringBuilder = new StringBuilder();
			for(int i=0; i<paragraphList.size(); i++)
			{
				CompositeTag paragraph = (CompositeTag) paragraphList.elementAt(i);	
				NodeList paragraphChildren = paragraph.getChildren();
				int elements = paragraphChildren.size();
				for(int k=0; k<elements; k++)
				{
					AbstractNode currentTag = (AbstractNode)paragraphChildren.elementAt(k);
					if(currentTag instanceof LinkTag)
					{
						LinkTag linkTag = (LinkTag)currentTag;
						
	//					System.out.println(linkTag.getLink());
	//					System.out.println(linkTag.extractLink());
						if(!linkTag.getLinkText().equalsIgnoreCase("citation needed"))//||!linkTag.getLinkText().equalsIgnoreCase("not in citation given"));
						{
							if(linkTag.getAttribute("class")!=null)
							{
								if(!linkTag.getAttribute("class").contains("image")
										&&!linkTag.getLink().contains("#"))
								{
									if(linkTag.getAttribute("class").contains("external")
											||linkTag.getAttribute("class").contains("new"))
									{
										if(!linkTag.getLinkText().matches("\\[(.+?)\\]"))
										{
											stringBuilder.append(linkTag.getLinkText());
										}
									}else
									{
										if(links)
											stringBuilder.append(currentTag.toHtml());
										else	
											stringBuilder.append(linkTag.getLinkText());
									}
								}
								
							}else
							{
								if(!linkTag.getLink().contains("#"))
								{
									if(links)
										stringBuilder.append(currentTag.toHtml());
									else	
										stringBuilder.append(linkTag.getLinkText());
								}
							}
						}
					}
					
					if(currentTag instanceof TextNode)
					{
						stringBuilder.append(currentTag.toHtml());
					}	
					
				}
			stringBuilder.append("\n");
			}
			return cleanText(stringBuilder.toString());
		}

	/**
	 * A method for reverse engineering with characters.  
	 * @param input
	 * @return
	 */
	private String cleanText(String input)
	{
		String output = input;
		output=output.replaceAll(" \\.", ".");
		output=output.replaceAll(" +", " ");
		output=output.replaceAll("\\( +", "(");
		output=output.replaceAll(" +\\)", ")");
		output=output.replaceAll("\\[\\]", "");
		output=output.replaceAll("([^\\.])\\n", "$1.\n");
		output=output.replaceAll(",([^ ])", ", $1");
		output=output.replaceAll(";([^ ])", "; $1");
		return output;
		
	}

	/**
	 * A guide how to use.
	 * @author yasa akbulut
	 */
	private static void printUsage() {
		System.out.println("WikipediaReader");
		System.out.println("\tGets the content of a Wikipedia article.");
		System.out.println("USAGE: java -jar wikipediaReader.jar [link to wikipedia article]");
		System.out.println("example: java -jar wikipediaReader.jar http://en.wikipedia.org/wiki/Hans-Gert_P%C3%B6ttering");
		
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
