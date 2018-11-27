package fr.univavignon.nerwip.processing.internal.modelless.opencalais;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
 * 
 * This file is part of Nerwip - Named Entity Extraction in Wikipedia Pages.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is distributed in the hope 
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Nerwip - Named Entity Extraction in Wikipedia Pages.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import fr.univavignon.common.data.article.Article;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.common.data.entity.EntityType;
import fr.univavignon.common.data.entity.mention.AbstractMention;
import fr.univavignon.common.data.entity.mention.Mentions;
import fr.univavignon.common.tools.keys.KeyHandler;
import fr.univavignon.nerwip.processing.ProcessorException;
import fr.univavignon.nerwip.processing.internal.modelless.AbstractModellessInternalDelegateRecognizer;
import fr.univavignon.tools.strings.StringTools;
import fr.univavignon.tools.web.WebTools;

/**
 * This class acts as an interface with the OpenCalais Web service.
 * <br/>
 * Recommended parameter values:
 * <ul>
 * 		<li>{@code ignorePronouns}: {@code true}</li>
 * 		<li>{@code exclusionOn}: {@code false}</li>
 * </ul>
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
class OpenCalaisDelegateRecognizer extends AbstractModellessInternalDelegateRecognizer<List<String>>
{	// user guide: http://new.opencalais.com/wp-content/uploads/2015/06/Thomson-Reuters-Open-Calais-API-User-Guide-v3.pdf
	
	/**
	 * Builds and sets up an object representing
	 * an OpenCalais recognizer.
	 * 
	 * @param openCalais
	 * 		Recognizer in charge of this delegate.
	 * @param lang
	 * 		Selected language.
	 * @param ignorePronouns
	 * 		Whether or not pronouns should be excluded from the detection.
	 * @param exclusionOn
	 * 		Whether or not stop-words should be excluded from the detection.
	 */
	public OpenCalaisDelegateRecognizer(OpenCalais openCalais, OpenCalaisLanguage lang, boolean ignorePronouns, boolean exclusionOn)
	{	super(openCalais, false, ignorePronouns, true, exclusionOn);
		
		selectedLanguage = lang;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override	
	public String getFolder()
	{	String result = recognizer.getName().toString();
		
		result = result + "_" + "ignPro=" + ignorePronouns;
		result = result + "_" + "ignNbr=" + ignoreNumbers;
		result = result + "_" + "exclude=" + exclusionOn;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ENTITY TYPES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public List<EntityType> getHandledEntityTypes()
	{	List<EntityType> result = selectedLanguage.getHandledTypes(); 
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Language we want OpenCalais to process */
	private OpenCalaisLanguage selectedLanguage;
	
	@Override
	public boolean canHandleLanguage(ArticleLanguage language)
	{	boolean result = selectedLanguage.handlesLanguage(language);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Web service URL */
//	private static final String SERVICE_URL = "http://api.opencalais.com/tag/rs/enrich"; // old version (pre 08/2015)
	private static final String SERVICE_URL = "https://api.thomsonreuters.com/permid/calais";
	/** Key name for OpenCalais */
	public static final String KEY_NAME = "OpenCalais";
	/** Maximal request size */
	private static final int MAX_SIZE = 10000;
	/** Delay between two remote invocations (4 queries per second max, as of 08/2015) */
	private static final long DELAY = 250;
	
	@Override
	protected List<String> detectMentions(Article article) throws ProcessorException
	{	logger.increaseOffset();
		List<String> result = new ArrayList<String>();
		String text = article.getRawText();

		// check if the key was set
		String key = KeyHandler.KEYS.get(KEY_NAME);
		if(key==null)
			throw new NullPointerException("In order to use OpenCalais, you first need to set up your user key in file res/misc/keys.xml using the exact name \"OpenCalais\".");
		
		// we need to break down the text: OpenCalais can't handle more than 10000 chars at once
//		List<String> parts = new ArrayList<String>();
//		while(text.length()>95000)
//		{	int index = text.indexOf("\n",90000) + 1;
//			String part = text.substring(0, index);
//			parts.add(part);
//			text = text.substring(index);
//		}
//		parts.add(text);
		List<String> parts = StringTools.splitText(text, MAX_SIZE);
		
		for(int i=0;i<parts.size();i++)
		{	logger.log("Processing OpenCalais part #"+(i+1)+"/"+parts.size());
			logger.increaseOffset();
			String part = parts.get(i);
			
			try
			{	// define HTTP message
				logger.log("Build OpenCalais HTTP message");
				HttpPost method = new HttpPost(SERVICE_URL);
//				method.setHeader("x-calais-licenseID", key);	// old version (pre 08/2015)
				method.setHeader("x-ag-access-token", key);
				method.setHeader("Content-Type", "text/raw; charset=UTF-8");
//				method.setHeader("Accept", "xml/rdf");			// old version (pre 08/2015)
				method.setHeader("outputFormat", "xml/rdf");				
				method.setEntity(new StringEntity(part, "UTF-8"));
				
				// send to open calais
				logger.log("Send message to OpenCalais");
				HttpClient client = new DefaultHttpClient();
				HttpResponse response = client.execute(method);
				
				// read answer
				String answer = WebTools.readAnswer(response);
				result.add(part);
				result.add(answer);
				
				// sleep a bit
	            try
	            {	Thread.sleep(DELAY);
				}
	            catch (InterruptedException e)
	            {	e.printStackTrace();
				}
			}
			catch (UnsupportedEncodingException e)
			{	e.printStackTrace();
				throw new ProcessorException(e.getMessage());
			}
			catch (ClientProtocolException e)
			{	e.printStackTrace();
				throw new ProcessorException(e.getMessage());
			}
			catch (IOException e)
			{	e.printStackTrace();
				throw new ProcessorException(e.getMessage());
			}
			
			logger.decreaseOffset();
		}
	
		logger.decreaseOffset();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// TYPE CONVERSION MAP	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of URI to entity type conversion */
	private final static Map<String, EntityType> CONVERSION_MAP = new HashMap<String, EntityType>();
	
	/** Initialization of the conversion map */
	static
	{	CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Anniversary", EntityType.DATE);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/City", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Company", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Continent", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Country", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/EntertainmentAwardEvent", EntityType.PRODUCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Facility", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Holiday", EntityType.DATE);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Movie", EntityType.PRODUCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/MusicAlbum", EntityType.PRODUCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/MusicGroup", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/NaturalFeature", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/OperatingSystem", EntityType.PRODUCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Organization", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Person", EntityType.PERSON);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/PharmaceuticalDrug", EntityType.PRODUCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Position", EntityType.FUNCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Product", EntityType.PRODUCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/ProgrammingLanguage", EntityType.PRODUCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/ProvinceOrState", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/PublishedMedium", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/RadioProgram", EntityType.PRODUCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/RadioStation", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/Region", EntityType.LOCATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/SportsEvent", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/SportsLeague", EntityType.ORGANIZATION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/TVShow", EntityType.PRODUCTION);
		CONVERSION_MAP.put("http://s.opencalais.com/1/type/em/e/TVStation", EntityType.ORGANIZATION);
	}
	
//	/** Pattern previously used to adjust entity positions */ 
//	private final static Pattern DOC_PATTERN = Pattern.compile("\\n\\n");
	
	/////////////////////////////////////////////////////////////////
	// XML NAMES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the RDF namespace */
	private final static String NS_RDF = "rdf";
	/** RDF namespace */
	private Namespace nsRdf = null;
	/** Name of the OC namespace */
	private final static String NS_C = "c";
	/** OC namespace */
	private Namespace nsC = null;

	/** Element of the OC format */
	private final static String ELT_DESCRIPTION = "Description";
	/** Element of the OC format */
	private final static String ELT_DETECTION = "detection";
//	/** Element of the OC format */
//	private final static String ELT_NAME = "name";
	/** Element of the OC format */
	private final static String ELT_SUBJECT = "subject";
	
	/** Attribute of the OC format */
	private final static String ATT_ABOUT = "about";
//	/** Attribute of the OC format */
//	private final static String ATT_DOCUMENT = "document";
	/** Attribute of the OC format */
	private final static String ELT_EXACT = "exact";
	/** Attribute of the OC format */
	private final static String ELT_OFFSET = "offset";
	/** Attribute of the OC format */
	private final static String ELT_LENGTH = "length";
	/** Attribute of the OC format */
	private final static String ATT_RESOURCE = "resource";
	/** Attribute of the OC format */
	private final static String ELT_TYPE = "type";

	/////////////////////////////////////////////////////////////////
	// CONVERSION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public Mentions convert(Article article, List<String> text) throws ProcessorException
	{	logger.increaseOffset();
		ArticleLanguage language = article.getLanguage();
		Mentions result = new Mentions(recognizer.getName());

		logger.log("Processing each part of data and its associated answer");
		Iterator<String> it = text.iterator();
		logger.increaseOffset();
		int i = 0;
		int prevSize = 0;
		while(it.hasNext())
		{	i++;
			logger.log("Processing part "+i+"/"+text.size()/2);
			String originalText = it.next();
			String ocAnswer = it.next();
			
			try
			{	// build DOM
				logger.log("Build DOM");
				SAXBuilder sb = new SAXBuilder();
				Document doc = sb.build(new StringReader(ocAnswer));
				Element root = doc.getRootElement();
				nsRdf = root.getNamespace(NS_RDF);
				nsC = root.getNamespace(NS_C);
				
				// separate data from metadata
				logger.log("Separate data from meta-data");
				List<Element> elements = root.getChildren(ELT_DESCRIPTION,nsRdf);
				Map<String,Element> metaData = new HashMap<String,Element>();
				List<Element> data = new ArrayList<Element>();
				for(Element element: elements)
				{	if(element.getChild(ELT_DETECTION,nsC)==null)
					{	String about = element.getAttributeValue(ATT_ABOUT,nsRdf); 
						metaData.put(about,element);
					}
					else
						data.add(element);
				}
				
				// create mentions
				logger.log("Create mention objects");
				for(Element element: data)
				{	AbstractMention<?> mention = convertElement(element, metaData, prevSize, language);
					if(mention!=null)
						result.addMention(mention);
				}
				
				// update size
				prevSize = prevSize + originalText.length();
				
				// fix mention positions
	//			fixRelativePositions(root, result);
			}
			catch (JDOMException e)
			{	e.printStackTrace();
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Receives an XML element and extract
	 * the information necessary to create
	 * a mention.
	 *  
	 * @param element
	 * 		Element to process.
	 * @param metaData
	 * 		Complementary information used to retrieve certain details.
	 * @param prevSize
	 * 		Size of the parts already processed.
	 * @param language
	 * 		Language of the processed article.
	 * @return
	 * 		The resulting mention.
	 */
	private AbstractMention<?> convertElement(Element element, Map<String,Element> metaData, int prevSize, ArticleLanguage language)
	{	AbstractMention<?> result = null;
		
		// get subject of the instance
		Element subjElt = element.getChild(ELT_SUBJECT,nsC);
		String subject = subjElt.getAttributeValue(ATT_RESOURCE,nsRdf);
		Element metadataElt = metaData.get(subject);
//if(metadataElt.getChild(ELT_NAME,nsC)==null)
//	System.out.print("");
		
		// get entity type from metadata
		Element typeElt = metadataElt.getChild(ELT_TYPE,nsRdf);
		String typeCode = typeElt.getAttributeValue(ATT_RESOURCE,nsRdf);
		EntityType type = CONVERSION_MAP.get(typeCode);
		
		// possibly create mention 
		if(type!=null)
		{	Element valueElt = element.getChild(ELT_EXACT,nsC);
			String valueStr = valueElt.getText();
			Element startElt = element.getChild(ELT_OFFSET,nsC);
			String startStr = startElt.getText();
			int startPos = prevSize + Integer.parseInt(startStr);
			Element lengthElt = element.getChild(ELT_LENGTH,nsC);
			String lengthStr = lengthElt.getText();
			int length = Integer.parseInt(lengthStr);
			int endPos = startPos + length;
			result = AbstractMention.build(type, startPos, endPos, recognizer.getName(), valueStr, language);
		}
		
		return result;
	}
	
//	/**
//	 * Method previously used to adjust
//	 * the position of mentions.
//	 * 
//	 * @param root
//	 * 		Root of the XML document.
//	 * @param mentions
//	 * 		List of mentions to be corrected.
//	 */
//	@SuppressWarnings("unchecked")
//	private void fixRelativePositions(Element root, List<AbstractEntity<?>> mentions)
//	{	if(!mentions.isEmpty())
//		{	// retrieve full text as recorded in the XML document
//			Element elt = null;
//			List<Element> list = root.getChildren();
//			Iterator<Element> it = list.iterator();
//			while(elt==null)
//			{	elt = it.next();
//				Element docElt = elt.getChild(ATT_DOCUMENT,nsC);
//				if(docElt!=null)
//					elt = docElt;
//			}
////			((CDATA)elt.getContent().iterator().next()).getValue();
//			String fullText = elt.getText();
//			
//			// adjust mention positions accordingly
//			Matcher matcher = DOC_PATTERN.matcher(fullText);
//			while(matcher.find())
//			{	int matcherStart = matcher.start();
//				for(AbstractEntity<?> mention : mentions)
//				{	int startPos = mention.getStartPos();
//					if(startPos > matcherStart)
//					{	int newStart = startPos - 1;
//						mention.setStartPos(newStart);
//						int newEnd = mention.getEndPos() - 1;
//						mention.setEndPos(newEnd);
//					}
//				}
//			}
//		}
//	}

	/////////////////////////////////////////////////////////////////
	// RAW FILE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void writeRawResults(Article article, List<String> intRes) throws IOException
	{	String temp = "";
		int i = 0;
		for(String str: intRes)
		{	i++;
			if(i%2==1)
				temp = temp + "\n>>> Part " + ((i+1)/2) + "/" + intRes.size() + " - Original Text <<<\n" + str + "\n";
			else
				temp = temp + "\n>>> Part " + (i/2) + "/" + intRes.size() + " - OpenCalais Response <<<\n" + str + "\n";
		}
		writeRawResultsStr(article, temp);
	}
}
