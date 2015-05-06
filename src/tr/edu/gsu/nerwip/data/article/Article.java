package tr.edu.gsu.nerwip.data.article;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011 Yasa Akbulut, Burcu Küpelioğlu & Vincent Labatut
 * Copyright 2012 Burcu Küpelioğlu, Samet Atdağ & Vincent Labatut
 * Copyright 2013 Samet Atdağ & Vincent Labatut
 * Copyright 2014-15 Vincent Labatut
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jdom.Element;
import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;
import tr.edu.gsu.nerwip.tools.xml.XmlNames;
import tr.edu.gsu.nerwip.tools.xml.XmlTools;

/**
 * This class represents an article, i.e. all
 * the relevant data extracted from the source page.
 * 
 * @author Vincent Labatut
 */
public class Article
{
	/**
	 * Creates a new article.
	 * 
	 * @param name
	 * 		Name of the article, also the name of its folder.
	 */
	public Article(String name)
	{	this.name = name;
		
		initFiles();
	}

	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of this article, also the name of its folder */
	private String name;
	
	/**
	 * Returns the name of this article.
	 * 
	 * @return
	 * 		Name of this article.
	 */
	public String getName()
	{	return name;
	}
	
	/////////////////////////////////////////////////////////////////
	// TITLE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Title of this article, also the name of the described person */
	private String title;
	
	/**
	 * Returns the title of this article.
	 * 
	 * @return
	 * 		Title of this article.
	 */
	public String getTitle()
	{	return title;
	}
	
	/**
	 * Changes the title of this article.
	 * 
	 * @param title
	 * 		New title for this article.
	 */
	public void setTitle(String title)
	{	this.title = title;
	}

	/////////////////////////////////////////////////////////////////
	// SOURCE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Address of the source page */
	private URL url = null;
	
	/**
	 * Returns the source URL of this article.
	 * 
	 * @return
	 * 		URL of this article.
	 */
	public URL getUrl()
	{	return url;
	}

	/**
	 * Changes the source URL of this article.
	 * 
	 * @param url
	 * 		New url of this article.
	 */
	public void setUrl(URL url)
	{	this.url = url;
	}

	/////////////////////////////////////////////////////////////////
	// LANGUAGE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Language of the article (only one allowed) */
	private ArticleLanguage language;
	
	/**
	 * Returns the language of this article.
	 * 
	 * @return
	 * 		Language of this article.
	 */
	public ArticleLanguage getLanguage()
	{	return language;
	}
	
	/**
	 * Changes the language of this article.
	 * 
	 * @param language
	 * 		New language for this article.
	 */
	public void setLanguage(ArticleLanguage language)
	{	this.language = language;
	}

	/////////////////////////////////////////////////////////////////
	// DATE RETRIEVED	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Date the page was last retrieved */
	private Date date = null;
	/** Used to read/write dates */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm",Locale.ENGLISH);
	
	/**
	 * Returns the retrieval date of this article.
	 * 
	 * @return
	 * 		Date of retrieval of this article.
	 */
	public Date getDate()
	{	return date;
	}

	/**
	 * Changes the date this article was retrieved.
	 * 
	 * @param date
	 * 		New date of retrieval.
	 */
	public void setDate(Date date)
	{	this.date = date;
	}

	/**
	 * Sets the date to the current date.
	 */
	public void initDate()
	{	Calendar calendar = Calendar.getInstance();
		date = calendar.getTime();
	}
	
	/////////////////////////////////////////////////////////////////
	// CATEGORIES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Categories of the source page (military, science, etc.) */
	private final List<ArticleCategory> categories = new ArrayList<ArticleCategory>();
	
	/**
	 * Returns the categories of this article
	 * (military, science, etc.).
	 * 
	 * @return
	 * 		Categories of this article.
	 */
	public List<ArticleCategory> getCategories()
	{	return categories;
	}

	/**
	 * Changes the categories of this article
	 * (military, science, etc.).
	 * 
	 * @param categories
	 * 		New categories of this article.
	 */
	public void setCategories(Collection<ArticleCategory> categories)
	{	this.categories.clear();
		this.categories.addAll(categories);
	}

	/////////////////////////////////////////////////////////////////
	// ORIGINAL PAGE	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Original source code of the web page */
	private String originalPage = null;
	
	/**
	 * Returns the original source code
	 * of this page.
	 * 
	 * @return
	 * 		Original source code of this page.
	 */
	public String getOriginalPage()
	{	return originalPage;
	}

	/**
	 * Changes the original source code
	 * of this page.
	 * 
	 * @param originalPage
	 * 		New original source code of this page.
	 */
	public void setOriginalPage(String originalPage)
	{	this.originalPage = originalPage;
	}

	/////////////////////////////////////////////////////////////////
	// FOLDER & FILES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Folder containing the cached files */
	private String folderPath = null;
	/** Original html file */
	private File originalFile = null;
	/** File containing the raw text */
	private File rawFile = null;
	/** File containing the text with hyperlinks */
	private File linkedFile = null;
	/** File containing the article metadata */
	private File propertiesFile = null;
	
	/**
	 * Initializes all file-related
	 * variables.
	 */
	private void initFiles()
	{	folderPath = FileNames.FO_OUTPUT + File.separator + name;
		originalFile = new File(folderPath + File.separator + FileNames.FI_ORIGINAL_PAGE);
		rawFile = new File(folderPath + File.separator + FileNames.FI_RAW_TEXT);
		linkedFile = new File(folderPath + File.separator + FileNames.FI_LINKED_TEXT);
		propertiesFile = new File(folderPath + File.separator + FileNames.FI_PROPERTIES);
	}
	
	/**
	 * Returns the path of the folder containing
	 * the cached files corresponding to this article.
	 * 
	 * @return
	 * 		Path of this article files.
	 */
	public String getFolderPath()
	{	return folderPath;
	}

	/////////////////////////////////////////////////////////////////
	// RAW TEXT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Raw text */
	private String rawText = null;

	/**
	 * Returns the raw text
	 * of this page.
	 * 
	 * @return
	 * 		Raw text of this page.
	 */
	public String getRawText()
	{	return rawText;
	}

	/**
	 * Changes the raw text
	 * of this page.
	 * 
	 * @param rawText
	 * 		New raw text of this page.
	 */
	public void setRawText(String rawText)
	{	this.rawText = rawText;
	}

	/////////////////////////////////////////////////////////////////
	// LINKED TEXT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Raw text with hyperlinks */
	private String linkedText = null;

	/**
	 * Returns the linked text
	 * of this page.
	 * 
	 * @return
	 * 		Linked text of this page.
	 */
	public String getLinkedText()
	{	return linkedText;
	}

	/**
	 * Changes the linked text
	 * of this page.
	 * 
	 * @param linkedText
	 * 		New linkedText of this page.
	 */
	public void setLinkedText(String linkedText)
	{	this.linkedText = linkedText;
	}

	/////////////////////////////////////////////////////////////////
	// READ				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Checks if the article corresponding
	 * to the specified article name/title
	 * was previously recorded.
	 * 
	 * @param name
	 * 		Name/title of the article of interest.
	 * @return
	 * 		{@code true} iff the specified article was recorded.
	 */
	public static boolean isCached(String name)
	{	
//		for(int i=0;i<name.length();i++)
//			System.out.println(i+": '"+name.charAt(i)+"'=='"+"Ahmet_Davutoglu".charAt(i)+"' >> "+(name.charAt(i)=="Ahmet_Davutoglu".charAt(i)));
		
		String folderPath = FileNames.FO_OUTPUT + File.separator + name;
//		File originalFile = new File(folderPath + File.separator + FileNames.FI_ORIGINAL_PAGE);
		File rawFile = new File(folderPath + File.separator + FileNames.FI_RAW_TEXT);
		File linkedFile = new File(folderPath + File.separator + FileNames.FI_LINKED_TEXT);
//		File propertiesFile = new File(folderPath + File.separator + FileNames.FI_PROPERTIES);
		
		boolean result = rawFile.exists() && linkedFile.exists() /*&& originalFile.exists()*/;
		return result;
	}
	
	/**
	 * Reads an article from file. The location
	 * of the article is automatically inferred
	 * from its name/title.
	 * 
	 * @param name
	 * 		Name/title of the article.
	 * @return
	 * 		The corresponding Article object.
	 * 
	 * @throws ParseException
	 * 		Problem while accessing the article files.
	 * @throws SAXException
	 * 		Problem while accessing the article files.
	 * @throws IOException
	 * 		Problem while accessing the article files.
	 */
	public static Article read(String name) throws ParseException, SAXException, IOException
	{	Article result = new Article(name);
		
		// properties
		if(result.propertiesFile.exists())
			result.readProperties();
		else
			// if the file does not exist, we create it
			result.writeProperties();
		
		// original page
		if(result.originalFile.exists())
		{	String originalPage = FileTools.readTextFile(result.originalFile);
			result.setOriginalPage(originalPage);
		}
		
		// raw text
		String rawText = FileTools.readTextFile(result.rawFile);
		result.setRawText(rawText);

		// raw text with hyperlinks
		if(result.linkedFile.exists())
		{	String linkedText = FileTools.readTextFile(result.linkedFile);
			result.setLinkedText(linkedText);
		}
		
		return result;
	}
	
	/**
	 * Reads the properties of this article 
	 * in an XML file.
	 * 
	 * @throws ParseException
	 * 		Problem while accessing the file.
	 * @throws SAXException
	 * 		Problem while accessing the file.
	 * @throws IOException
	 * 		Problem while accessing the file.
	 */
	private void readProperties() throws ParseException, SAXException, IOException
	{	// schema file
		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_PROPERTY_SCHEMA;
		File schemaFile = new File(schemaPath);

		// load file
		Element root = XmlTools.getRootFromFile(propertiesFile,schemaFile);
		
		// article title
		{	Element titleElt = root.getChild(XmlNames.ELT_TITLE);
			if(titleElt!=null)
			{	String titleStr = titleElt.getTextTrim();
				this.title = titleStr;
			}
		}

		// origine url
		{	Element urlElt = root.getChild(XmlNames.ELT_URL);
			if(urlElt!=null)
			{	String urlStr = urlElt.getTextTrim();
				URL url = new URL(urlStr);
				this.url = url;
			}
		}
		
		// language
		{	Element languageElt = root.getChild(XmlNames.ELT_LANGUAGE);
			if(languageElt!=null)
			{	String languageStr = languageElt.getTextTrim().toUpperCase(Locale.ENGLISH);
				ArticleLanguage language = ArticleLanguage.valueOf(languageStr);
				this.language = language;
			}
		}
		
		// retrieval date
		{	Element dateElt = root.getChild(XmlNames.ELT_DATE);
			if(dateElt!=null)
			{	String dateStr = dateElt.getTextTrim();
				Date date = DATE_FORMAT.parse(dateStr);
				this.date = date;
			}
		}
		
		// categories of biography
		{	Element catElt = root.getChild(XmlNames.ELT_CATEGORY);
			if(catElt!=null)
			{	String catsStr = catElt.getTextTrim().toUpperCase(Locale.ENGLISH);
				String temp[] = catsStr.split(" ");
				categories.clear();
				for(String catStr: temp)
				{	ArticleCategory cat = ArticleCategory.valueOf(catStr);
					categories.add(cat);
				}
			}
		}
	}

	/////////////////////////////////////////////////////////////////
	// WRITE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Writes this article in
	 * its predefined folder. All
	 * files are writen, possibly
	 * erasing pre-existing content.
	 * 
	 * @throws IOException
	 * 		Problem while writing this article files. 
	 */
	public void write() throws IOException
	{	// original html code
// now already done in the reader class
//		if(originalPage!=null)
//			FileTools.writeTextFile(originalFile,originalPage);

		// raw text only
		FileTools.writeTextFile(rawFile,rawText);
		
		// raw text with hyperlinks
		FileTools.writeTextFile(linkedFile,linkedText);
		
		// properties
		writeProperties();
	}
	
	/**
	 * Writes the properties of this article
	 * in an XML file.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the file.
	 */
	private void writeProperties() throws IOException
	{	// check folder
		File folder = propertiesFile.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		// schema file
		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_PROPERTY_SCHEMA;
		File schemaFile = new File(schemaPath);
		
		// build xml document
		Element root = new Element(XmlNames.ELT_PROPERTIES);

		// article title
		if(title!=null)
		{	Element titleElt = new Element(XmlNames.ELT_TITLE);
			titleElt.setText(title);
			root.addContent(titleElt);
		}
		
		// origine url
		if(url!=null)
		{	String urlStr = url.toString();
			Element urlElt = new Element(XmlNames.ELT_URL);
			urlElt.setText(urlStr);
			root.addContent(urlElt);
		}
		
		// language
		if(language!=null)
		{	String languageStr = language.toString();
			Element languageElt = new Element(XmlNames.ELT_LANGUAGE);
			languageElt.setText(languageStr);
			root.addContent(languageElt);
		}
		
		// retrieval date
		if(date!=null)
		{	String dateStr = DATE_FORMAT.format(date);
			Element dateElt = new Element(XmlNames.ELT_DATE);
			dateElt.setText(dateStr);
			root.addContent(dateElt);
		}
		
		// categories of biography
		if(!categories.isEmpty())
		{	String catStr = "";
			for(ArticleCategory category: categories)
				catStr = catStr + category.toString() + " ";
			catStr = catStr.substring(0,catStr.length()-1);
			Element catElt = new Element(XmlNames.ELT_CATEGORY);
			catElt.setText(catStr);
			root.addContent(catElt);
		}
		
		// record file
		XmlTools.makeFileFromRoot(propertiesFile,schemaFile,root);
	}

	/////////////////////////////////////////////////////////////////
	// ENTITIES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of reference entities
	 * for this article.
	 * 
	 * @return
	 * 		The list of reference entities.
	 * 		
	 * @throws IOException
	 * 		Problem while accessing the file.
	 * @throws SAXException
	 * 		Problem while accessing the file.
	 * @throws ParseException 
	 * 		Problem while accessing the file.
	 */
	public Entities getReferenceEntities() throws SAXException, IOException, ParseException
	{	String path = folderPath + File.separator + FileNames.FI_ENTITY_LIST;
		File file = new File(path);
		Entities result = Entities.readFromXml(file);
		return result;
	}
	
	/**
	 * Returns the list of entities
	 * for this article, as estimated
	 * by the specified NER tool.
	 * 
	 * @param recognizer
	 * 		Concerned NER tool.
	 * @return
	 * 		The list of reference entities.
	 * 		
	 * @throws IOException
	 * 		Problem while accessing the file.
	 * @throws SAXException
	 * 		Problem while accessing the file.
	 * @throws ParseException 
	 * 		Problem while accessing the file.
	 */
	public Entities getEstimatedEntities(AbstractRecognizer recognizer) throws SAXException, IOException, ParseException
	{	String path = folderPath + File.separator + recognizer.getFolder() + File.separator + FileNames.FI_ENTITY_LIST;
		File file = new File(path);
		Entities result = Entities.readFromXml(file);
		return result;
	}
}
