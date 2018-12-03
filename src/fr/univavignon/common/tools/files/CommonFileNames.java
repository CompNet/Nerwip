package fr.univavignon.common.tools.files;

import fr.univavignon.tools.files.FileNames;

/**
 * This class contains various methods
 * related to file management.
 *  
 * @author Vincent Labatut
 */
public class CommonFileNames
{	
	/////////////////////////////////////////////////////////////////
	// FOLDERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////
	// FILES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** XML schema file used to record article properties  */
	public final static String FI_PROPERTY_SCHEMA = "properties" + FileNames.EX_SCHEMA;
	/** File containing the properties of the article */
	public final static String FI_PROPERTIES = "properties" + FileNames.EX_XML;
	/** File containing original page */
	public final static String FI_ORIGINAL_PAGE = "original" + FileNames.EX_HTML;
	/** File containing the raw text */
	public final static String FI_RAW_TEXT = "raw" + FileNames.EX_TEXT;
	/** XML schema file used to record mentions  */
	public final static String FI_MENTION_SCHEMA = "mentions" + FileNames.EX_SCHEMA;
	/** XML schema file used to record entities  */
	public final static String FI_ENTITY_SCHEMA = "entities" + FileNames.EX_SCHEMA;
	/** XML file containing the mentions estimated by a recognizer or completed by a resolver, in a normalized format */
	public final static String FI_MENTION_LIST = "mentions" + FileNames.EX_XML;
	/** XML file containing the entities detected by a resolver or linked by a linker, in a normalized format */
	public final static String FI_ENTITY_LIST = "entities" + FileNames.EX_XML;
}
