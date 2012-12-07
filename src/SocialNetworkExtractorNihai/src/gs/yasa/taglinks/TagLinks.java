package gs.yasa.taglinks;


import gs.yasa.annotationviewer.MainWindow;
import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationCandidate;
import gs.yasa.sne.common.AnnotationType;
import gs.yasa.sne.common.ImproperCandidateException;
import gs.yasa.wikipediareader.WikipediaReader;
import gs.yasa.wikipediareader.WikipediaReaderException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;





/**
 * This class takes a text with links, generates an annotation list from the input
 * text.
 * @author yasa akbulut
 * @version 1
 *
 */
public class TagLinks {

	
	/**
	 * Takes an article from Wikipedia, generates an annotated text from it using
	 * FreeBase database.
	 * @param inputTextWithLinks
	 * @return
	 */
	public ArrayList<Annotation> annotateText(String inputTextWithLinks)
	{
		return annotateText(inputTextWithLinks, "");
	}
	
	/**
	 * Takes an article from Wikipedia, generates an annotated text from it using
	 * FreeBase database.
	 * @param inputTextWithLinks
	 * @param pageTitle
	 * @return
	 * @author yasa akbulut
	 */
	public ArrayList<Annotation> annotateText(String inputTextWithLinks, String pageTitle)
	{	
		ArrayList<AnnotationCandidate> annotationCandidates = new ArrayList<AnnotationCandidate>();
		Parser parser;
		try {
			parser = new Parser("<p>"+inputTextWithLinks+"</p>");
			NodeList linkList = parser.parse(new TagNameFilter("a"));
			int size = linkList.size();
			for(int i=0; i<size; i++)
			{
				LinkTag linkTag = (LinkTag)linkList.elementAt(i);
				String[] linkParts = linkTag.getLink().split("/");
				
				AnnotationCandidate candidate = new AnnotationCandidate(
						linkTag.getLinkText(),
						null,
						linkTag.getStartPosition()-3, //3 is the length of "<p>" we added to the beginning. 
						linkTag.getEndPosition()-3, //same here.
						linkTag.toHtml());
				if(isCapitalized(candidate.getEntityName()))
				{
					String wikipediaTitle = URLDecoder.decode(linkParts[linkParts.length-1], "UTF-8");
					String wikipediaTitleEscaped = FreebaseUtil.escapeMqlKey(wikipediaTitle);
					System.out.println(wikipediaTitle);
					System.out.println(wikipediaTitleEscaped);
					ArrayList<String> possibleTypes = FreebaseUtil.getTypes(wikipediaTitleEscaped);
					try
					{
						candidate.setEntityType(determineType(possibleTypes));
					}catch(IndeterminiteTypeException e)
					{
						e.printStackTrace();
						candidate.setValid(false);
					}
				}else
				{
					candidate.setValid(false);
				}
				annotationCandidates.add(candidate);
			}
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //tags are needed so that htmlparser understands it is content, and not a filename.
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		//use the page title -- to be added TODO
//		if(pageTitle.length()>0)
//		{
//			Pattern titlePattern = Pattern.compile("[\\x20]("+pageTitle+")([\\.\\x20]+)");
//			Matcher m = titlePattern.matcher(inputTextWithLinks);
//			while(m.find())
//			{
//				AnnotationCandidate
//			}
//		}
		
		
		ArrayList<AnnotationCandidate> secondaryCandidates = new ArrayList<AnnotationCandidate>();
		for (AnnotationCandidate annotationCandidate : annotationCandidates) {
			String entityName = annotationCandidate.getEntityName();
			Pattern p = Pattern.compile("[\\x20]("+entityName+")([\\.\\x20]+)");
			Matcher m = p.matcher(inputTextWithLinks);
			while(m.find())
			{
				AnnotationCandidate candidate = new AnnotationCandidate();
				candidate.setEntityName(entityName);
				
				String trimmedString = m.group(1);
				int startPosFix = m.group().indexOf(trimmedString);
				int endPosFix = m.group().length()-trimmedString.length()-startPosFix;
					
				candidate.setStartPos(m.start()+startPosFix);
				candidate.setEndPos(m.end()-endPosFix);
				candidate.setRawString(m.group());
				candidate.setEntityType(annotationCandidate.getEntityType());
				secondaryCandidates.add(candidate);
			}
		}
		
 
		for (AnnotationCandidate annotationCandidate : annotationCandidates) {
			int start = annotationCandidate.getStartPos();
			int offset = annotationCandidate.getRawString().length() - annotationCandidate.getEntityName().length();
			inputTextWithLinks = inputTextWithLinks.replace(annotationCandidate.getRawString(), annotationCandidate.getEntityName());
			annotationCandidate.setStartPos(start);
			annotationCandidate.setEndPos(start+annotationCandidate.getEntityName().length());
			for (AnnotationCandidate annotationCandidateOther : annotationCandidates) {
				if(annotationCandidateOther.getStartPos()>start)
				{
					annotationCandidateOther.setStartPos(annotationCandidateOther.getStartPos()-offset);
					annotationCandidateOther.setEndPos(annotationCandidateOther.getEndPos()-offset);
				}
			}
			for (AnnotationCandidate secondaryCandidate : secondaryCandidates) {
				if(secondaryCandidate.getStartPos()>start)
				{
					secondaryCandidate.setStartPos(secondaryCandidate.getStartPos()-offset);
					secondaryCandidate.setEndPos(secondaryCandidate.getEndPos()-offset);
				}
			}
		}
		annotationCandidates.addAll(secondaryCandidates);
		
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		for (AnnotationCandidate annotationCandidate : annotationCandidates) {
			//annotationCandidate.setEntityType(AnnotationType.MISC);
			try {
				if(annotationCandidate.isValid())
					annotations.add(annotationCandidate.toAnnotation());
			} catch (ImproperCandidateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		
		return annotations;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length<1)
		{
			System.out.println("LinkedEntityRecognizer - Yasa Akbulut");
			System.out.println("Usage: java -jar linkedEntityRecognizer.jar [link to wikipedia article] [output file]");
		}
		boolean debug = true;
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		String inputTextWithLinks = "";
		String inputTextWithoutLinks = "";
		
		System.out.println("Trying to open: "+args[0]);
		try {
			WikipediaReader reader = new WikipediaReader(debug);
			inputTextWithLinks = reader.getTextWithLinks(args[0]);
			inputTextWithoutLinks = reader.getText(args[0]);
			TagLinks tool = new TagLinks();
			annotations = tool.annotateText(inputTextWithLinks);
			
		}catch (WikipediaReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println("Input text with tags:");
//		System.out.println(inputTextWithLinks);
//		System.out.println("Input text without tags:");
//		System.out.println(inputTextWithoutLinks);
		
		File outfile = new File("linkedentityrecognizer.default");
		
		if(outfile!=null)
		{
			try {
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
	
		
		MainWindow window = new MainWindow();
		window.addAnnotationTab(inputTextWithoutLinks, annotations, "4th tool", "default");
		window.show();


		
	}

	/**
	 * This method takes as parameter a link designating an entity and controls if
	 * it starts with an upper case or not. If the entity does not exist return value
	 * is set to false
	 * @author yasa akbulut
	 * @param linkText
	 * @return true if a linked entity starts with an upper case. false if this link 
	 * does not exist or starts with a lower case
	 */
	private static boolean isCapitalized(String linkText) { //TODO: the dailyWTF candidate?
		if(linkText==null)
			return false;
		else
		{
			return !Character.isLowerCase(linkText.charAt(0));
		}
	}
	
	/**
	 * This method receives as parameter a list of possible entity types
	 * and changes them into correct forms of types
	 * @author yasa akbulut
	 * @param possibleTypes
	 * @return correct form of type
	 * @throws IndeterminiteTypeException if the type is not defined
	 */
	private static AnnotationType determineType(ArrayList<String> possibleTypes) throws IndeterminiteTypeException
	{
		StringBuilder sb = new StringBuilder(); //type list to be used in an exception
		for (String string : possibleTypes) {
			sb.append(string+"; ");
			if(string.endsWith("person"))
				return AnnotationType.PERSON;
			if(string.endsWith("location"))
				return AnnotationType.LOCATION;
			if(string.endsWith("organization"))
				return AnnotationType.ORGANIZATION;
			if(string.endsWith("governmental_body"))
				return AnnotationType.ORGANIZATION;
			//TODO: Add more types
				
		}
		throw new IndeterminiteTypeException(sb.toString());
	}

}
