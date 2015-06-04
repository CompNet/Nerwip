package tr.edu.gsu.nerwip.recognition.external.nero;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.ConverterException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.external.AbstractExternalConverter;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.string.StringTools;

/**
 * This class is the converter associated to Nero. It is able to convert the
 * text outputed by this NER tool into objects compatible with Nerwip. 
 * <br/>
 * It can also read/write these results using raw text and our XML format.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class NeroConverter extends AbstractExternalConverter
{	
	/**
	 * Builds a new converter using the specified info.
	 * 
	 * @param nerFolder
	 *            Folder used to stored the results of the NER tool.
	 */
	public NeroConverter(String nerFolder)
	{	super(RecognizerName.NERO, nerFolder, FileNames.FI_OUTPUT_TEXT);
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP 	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of URI to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	/** List of ignored entity types */
	private final static List<String> IGNORED_TYPES = Arrays.asList(
		"amount"
	);
	
	/** Initialization of the conversion map */
	static 
	{	CONVERSION_MAP.put("time", EntityType.DATE);
		CONVERSION_MAP.put("loc", EntityType.LOCATION);
		CONVERSION_MAP.put("org", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("fonc", EntityType.FUNCTION);
		CONVERSION_MAP.put("pers", EntityType.PERSON);
	}

	/////////////////////////////////////////////////////////////////
	// PROCESS 			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Entities convert(Article article, String data) throws ConverterException
	{	Entities result = new Entities(recognizerName);
		String originalText = article.getRawText();

		LinkedList<EntityType> types = new LinkedList<EntityType>();
		LinkedList<Integer> startPos1 = new LinkedList<Integer>();
//		LinkedList<Integer> startPos2 = new LinkedList<Integer>();
		LinkedList<String> tags = new LinkedList<String>();
		
		int i1 = 0;
		int i2 = 0;
		int c1 = originalText.codePointAt(i1);
		int c2 = data.codePointAt(i2);
		
		// possibly pass a starting newline character 
		if(c2=='\n')
		{	i2++;
			c2 = data.codePointAt(i2);
		}
		
		while(i1<originalText.length() && i2<data.length())
		{	c1 = originalText.codePointAt(i1);
			c2 = data.codePointAt(i2);
			
			// beginning of a tag
			if(c2=='<')
			{	//int k2 = i2;
				i2++; 
				c2 = data.codePointAt(i2);
				
				// closing tag
				if(c2=='/')
				{	int j2 = data.indexOf('>', i2);
					String tag = data.substring(i2+1,j2);
					String tag0 = tags.pop();
					if(!tag.equalsIgnoreCase(tag0))
					{	String msg = StringTools.highlightPosition(i2, data, 20);
						logger.log("WARNING: opening tag ("+tag0+") different from closing tag ("+tag+"):\n"+msg);
					}
					i2 = j2 + 1;
					EntityType type = types.pop();
					int sp1 = startPos1.pop();
//					int sp2 = startPos2.pop();
					if(type!=null)
					{
//						String valueStr = data.substring(sp2,k2);
						String valueStr = originalText.substring(sp1,i1);
						AbstractEntity<?> entity = AbstractEntity.build(type, sp1, i1, RecognizerName.NERO, valueStr);
						result.addEntity(entity);
					}
				}
				
				// opening tag
				else
				{	int j2 = data.indexOf('>', i2);
					String tag = data.substring(i2,j2);
					i2 = j2 + 1;
					tags.push(tag);
					EntityType type = CONVERSION_MAP.get(tag);
					if(type==null && !IGNORED_TYPES.contains(tag))
						throw new ConverterException("Found an unknown tag : "+tag);
					types.push(type);
					startPos1.push(i1);
//					startPos2.push(i2);
				}
			}
			
			// other character (than '<')
			else
			{	// similar characters
				if(StringTools.compareCharsRelaxed(c1,c2)==0)// || c2==65533)
				{	// everything's normal
					// >> go to next chars in both texts
					i1++; 
					i2++; 
				}
				
				// different chars, but space in the original text
				else if(c1==' ')
				{	// Nero probably ate a space
					// >> go to next char in the original text only
					i1++; 
				}
				
				// different chars, but space in the annotated text
				else if(c2==' ')
				{	// if right before or right after a tag (in the annotated text), Nero probably added a space
					// >> go to next char in the annotated text
					int before = data.codePointAt(i2-1);
					int after = data.codePointAt(i2+1);
					if(before=='>' || after=='<' || after=='\n')
					{	i2++; 
					}
					// otherwise, if punctuation in the original text, Nero probably ate this punctuation mark
					// >> go to next char in the original text
					else if(Pattern.matches("\\p{Punct}", originalText.substring(i1,i1+1)) && originalText.charAt(i1+1)==' ')
					{	i1++; 
					}
					// else, we have a problem!
					else
					{	String msg1 = StringTools.highlightPosition(i1, originalText, 20);
						String msg2 = StringTools.highlightPosition(i2, data, 20);
						throw new ConverterException("Problem at position :\n"+msg1+"\n"+msg2);
					}
				}

				// problem : display a specific error message
				else
				{	String msg1 = StringTools.highlightPosition(i1, originalText, 20);
					String msg2 = StringTools.highlightPosition(i2, data, 20);
					throw new ConverterException("Found a supernumerary character which is not space :\n"+msg1+"\n"+msg2);
				}
			}
		}
		
		// check if we actually processed the whole texts
		if(i1<originalText.length())
		{	// possibly consume the final newline chars
			do
			{	c1 = originalText.codePointAt(i1);
				i1++;
			}
			while(i1<originalText.length() && c1=='\n');
			if(i1<originalText.length())
			{	String msg1 = StringTools.highlightPosition(i1, originalText, 20);
				throw new ConverterException("Didn't reach the end of the original text\n"+msg1);
			}
		}
		else if(i2<data.length())
		{	String msg2 = StringTools.highlightPosition(i2, data, 20);
			throw new ConverterException("Didn't reach the end of the annotated text\n"+msg2);
		}
		
		return result;
	}
	
//	public Entities convert(Article article, String data)
//			throws ConverterException {
//		Entities result = new Entities(recognizerName);
//		String originalText = article.getRawText();
//		AbstractEntity<?> entity = null;
//		// 2ème algo
//		char co = originalText.charAt(0);
//		int i = 0;
//		char cc = data.charAt(0);
//		int j = 0;
//		do {
//			co = originalText.charAt(i);
//			cc = data.charAt(j);
//			if (co == cc) {
//				i++;
//				j++;
//			} else {
//				int start = i;
//				String typeCode = type(j, data);
//				EntityType type = CONVERSION_MAP.get(typeCode);
//				j = j + typeCode.length() + 2; // check l value here
//
//				String name = " ";
//				while (closeTag(j, data) == false) {
//					do {
//						name = name + originalText.charAt(i);
//						i++;
//						j++;
//					} while (co == cc);
//					if (data.charAt(j) == '<' && data.charAt(j + 1) == '/') {
//						int end = i;
//						entity = AbstractEntity.build(type, start, end,
//								recognizerName, name);
//						result.addEntity(entity);
//						String ch = type(j, data);
//						j = j + ch.length() + 3;
//					} else {
//						Entities list = f(i, j, originalText, data);
//						result.addEntities(list);
//					}
//				}
//			}
//		} while (i <= originalText.length());
//		return result;
//	}
//
//	public Entities f(int i, int j, String text1, String text2) {
//		Entities entities = null;
//		Entities result = new Entities(recognizerName);
//		AbstractEntity<?> entity = null;
//		int start = i;
//		char co = text1.charAt(i);
//		char cc = text2.charAt(j);
//		String typeCode = type(j, text2);
//		EntityType type = CONVERSION_MAP.get(typeCode);
//		j = j + typeCode.length() + 2; // check l value here
//		String name = " ";
//		while (closeTag(j, text2) == false) {
//			do {
//				name = name + text1.charAt(i);
//				i++;
//				j++;
//			} while (co == cc);
//			if (text2.charAt(j) == '<' && text2.charAt(j + 1) == '/') {
//				int end = i;
//				entity = AbstractEntity.build(type, start, end, recognizerName,
//						name);
//				result.addEntity(entity);
//				String ch = type(j, text2);
//				j = j + ch.length() + 3;
//			} else {
//				Entities list = f(i, j, text1, text2);
//				result.addEntities(list);
//			}
//		}
//		return entities;
//	}
//
//	public String type(int i, String ch) {
//		String type = new String();
//		i++;
//		do {
//			type = type + ch.charAt(i);
//			i++;
//		} while (ch.charAt(i) != '>');
//		return type;
//	}
//
//	// necessary :algo 2 :to know if i pos is clos tag
//	public boolean closeTag(int i, String ch) {
//		boolean close = false;
//		if (ch.charAt(i) == '<') {
//			if (ch.charAt(i + 1) == '/')
//				close = true;
//		}
//		return close;
//	}
}
