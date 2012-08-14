package gs.yasa.outputunifier.opencalais;

import gs.yasa.outputunifier.OutputReader;
import gs.yasa.sne.common.Annotation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class forms an output reader special for Stanford Manual Annotation Tool.
 * @author yasa akbulut
 * @version 1
 */
public class OpenCalaisOutputReader extends OutputReader{

	/* (non-Javadoc)
	 * @see gs.yasa.outputunifier.OutputReader#read(java.io.File)
	 */
	@Override
	public ArrayList<Annotation> read(File infile) throws FileNotFoundException {
		
		ArrayList<Annotation> result = new ArrayList<Annotation>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document dom = builder.parse(infile);
			return(read(dom.getDocumentElement()));
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see gs.yasa.outputunifier.OutputReader#read(java.lang.String)
	 */
	@Override
	public ArrayList<Annotation> read(String text) {
		ArrayList<Annotation> result = new ArrayList<Annotation>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			try {
				builder = dbf.newDocumentBuilder();
				Document dom = builder.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
				return(read(dom.getDocumentElement()));
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;

			

	}
		
		/**
		 * reads an element through OpenCalais properties in Description
		 * @param rootElement
		 * @return an ArrayList containing results
		 */
		private ArrayList<Annotation> read(Element rootElement)
		{
			ArrayList<Annotation> result = new ArrayList<Annotation>();
			NodeList nodeList = rootElement.getElementsByTagName("rdf:Description");
			ArrayList<Description> elements = new ArrayList<Description>();
			int i;
			for(i=0;i<nodeList.getLength();i++)
			{
				Description element = new Description();
				Node tempElement = nodeList.item(i);
				
				element.properties.put("nodeName", tempElement.getNodeName());
				//System.out.println(tempElement.getNodeName());
				
				NamedNodeMap attributes = tempElement.getAttributes();
				int j;
				for(j=0; j<attributes.getLength(); j++)
				{
					element.properties.put(attributes.item(j).getNodeName(),attributes.item(j).getNodeValue());
					
					//System.out.println("\t\t"+attributes.item(j).getNodeName()+": "+attributes.item(j).getNodeValue());
				}
				NodeList children = tempElement.getChildNodes();
				for(j=0; j<children.getLength(); j++)
				{
					String childNodeName = children.item(j).getNodeName();
					String childNodeValue = ""; 
					if(children.item(j).hasAttributes())
						childNodeValue = children.item(j).getAttributes().item(0).getNodeValue();
					else
						childNodeValue = children.item(j).getTextContent();
					element.properties.put(childNodeName, childNodeValue);
					
					//System.out.println("\t"+children.item(j).getNodeName());
					//System.out.println("\t"+children.item(j).getTextContent());
				}
				elements.add(element);
			}
			
			OpenCalaisAnnotationBuilder annotationBuilder = new OpenCalaisAnnotationBuilder();
			ArrayList<Annotation> annotations = annotationBuilder.build(elements);
			OpenCalaisAnnotationBuilder.fixRelativePositions(rootElement, annotations);
			result.addAll(annotations);
			return result;
		}

	
	
}
