package gs.yasa.outputunifier.opencalais;

import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;
import gs.yasa.sne.common.AnnotationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



/**
 * This class sets OpenCalais' settings for an annotation
 * @author yasa akbulut
 * @version 1
 *
 */
public class OpenCalaisAnnotationBuilder {

	/**
	 * contains different entity types which belongs to OpenCalais
	 */
	private HashMap<String, AnnotationType> conversionMap;

	/**
	 * contains a list of pronouns which are annotated as person
	 */
	private ArrayList<String> pronouns;
	
	/**
	 * Constructor. Makes conversion between different OpenCalais' entity types and 
	 * pronouns to the provisions 
	 * @author yasa akbulut
	 */
	public OpenCalaisAnnotationBuilder()
	{
		conversionMap = new HashMap<String, AnnotationType>();
		conversionMap.put("http://s.opencalais.com/1/type/em/e/City", AnnotationType.LOCATION);
		conversionMap.put("http://s.opencalais.com/1/type/em/e/Person", AnnotationType.PERSON);
		conversionMap.put("http://s.opencalais.com/1/type/em/e/Country", AnnotationType.LOCATION);
		conversionMap.put("http://s.opencalais.com/1/type/em/e/Organization", AnnotationType.ORGANIZATION);
		conversionMap.put("http://s.opencalais.com/1/type/em/e/Facility", AnnotationType.ORGANIZATION);
		
		pronouns = new ArrayList<String>();
		pronouns.add("he");
		pronouns.add("him");
		pronouns.add("himself");
		pronouns.add("his");
		pronouns.add("she");
		pronouns.add("her");
		pronouns.add("herself");
		pronouns.add("it");
		pronouns.add("its");
		pronouns.add("they");
		pronouns.add("them");
		pronouns.add("their");
		pronouns.add("i");
		pronouns.add("me");
		pronouns.add("my");
		pronouns.add("you");
		pronouns.add("your");
	}
	
	
	/**
	 * Builds a new annotations list by setting tool name
	 * @param entityName
	 * @param entityType
	 * @param startPos
	 * @param endPos
	 * @param rawText
	 * @return an annotations list
	 * @author yasa akbulut
	 */
	public ArrayList<Annotation> build(ArrayList<Description> elements)
	{
		ArrayList<Annotation> result = new ArrayList<Annotation>();
		for (Description element : elements) {
			if(element.hasProperty("c:detection"))
			{
				//System.out.println("Detection found:"+element.properties.get("c:detection"));
				//element.listAll();
				for (Description subjectelement : elements) {
					if(subjectelement.hasProperty("rdf:about"))
					{
						if(subjectelement.properties.get("rdf:about").equals(element.properties.get("c:subject")))
						{
							if(subjectelement.hasProperty("c:name"))
							{

								if(conversionMap.containsKey(subjectelement.properties.get("rdf:type")))
								{
									if(!isPronoun(element.properties.get("c:exact")))
									{
										result.add(new Annotation(
											element.properties.get("c:exact"),
											conversionMap.get(subjectelement.properties.get("rdf:type")),
											Integer.parseInt(element.properties.get("c:offset")),
											Integer.parseInt(element.properties.get("c:offset"))+Integer.parseInt(element.properties.get("c:length")),
											subjectelement.properties.get("c:name"),AnnotationTool.OPENCALAIS));
									}
								}//else we're not interested
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * @param s
	 * @return true if the parameter string is a pronoun, false else
	 */
	private boolean isPronoun(String s)
	{
		for (String pronoun : pronouns) {
			if(pronoun.equalsIgnoreCase(s))
				return true;
		}
		return false;
		
	}

	/**
	 * Calculates a relative position for an annotation by subtracting the shift
	 * @param annotations
	 * @author yasa akbulut
	 */
	public static void fixRelativePositions(Element rootElement, ArrayList<Annotation> annotations)
	{
		Pattern searchPattern = Pattern.compile("\\n\\n");
		String input= "";
		NodeList children = rootElement.getChildNodes();
		int i;
		for(i=0; i<children.getLength(); i++)
		{
			NodeList grandchildren = children.item(i).getChildNodes();
			int j;
			for(j=0; j<grandchildren.getLength(); j++)
			{
				if(grandchildren.item(j).getNodeName().equalsIgnoreCase("c:document"))
				{
					input = grandchildren.item(j).getTextContent();
				}
			}
		}
		Matcher matcher = searchPattern.matcher(input);
		while(matcher.find())
		{
			for (Annotation annotation : annotations) {
				if (annotation.getStartPos()>matcher.start())
				{
					annotation.setStartPos(annotation.getStartPos()-1);
					annotation.setEndPos(annotation.getEndPos()-1);
				}
			}
		}
	}
}
