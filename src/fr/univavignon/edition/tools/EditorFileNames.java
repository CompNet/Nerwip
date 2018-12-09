package fr.univavignon.edition.tools;

import java.io.File;

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

import fr.univavignon.tools.files.FileNames;

/**
 * This class contains various methods
 * related to file management.
 *  
 * @author Vincent Labatut
 */
public class EditorFileNames
{	
	/////////////////////////////////////////////////////////////////
	// FOLDERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Folder used to store images */
	public final static String FO_IMAGES = FileNames.FO_RESOURCES + File.separator + "images";
	/** Language XML files for the GUI */
	public final static String FO_LANGUAGE = FileNames.FO_RESOURCES + File.separator + "language";
	
	/////////////////////////////////////////////////////////////////
	// FILES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Configuration file */
	public final static String FI_CONFIGURATION = "edconfig" + FileNames.EX_XML;
	/** Configuration schema */
	public final static String FI_CONFIGURATION_SCHEMA = "edconfig" + FileNames.EX_SCHEMA;
	
	/** HTML file containing the "index" text */
	public final static String FI_HELP_PAGE = "help" + FileNames.EX_HTML;
	
	/** Application icon */
	public final static String FI_ICON_APP = "icon" + FileNames.EX_PNG;
	/** Icon for add */
	public final static String FI_ICON_ADD = "plus" + FileNames.EX_PNG;
	/** Icon for remove */
	public final static String FI_ICON_REMOVE = "remove" + FileNames.EX_PNG;
	/** Icon for show */
	public final static String FI_ICON_SHOW = "view" + FileNames.EX_PNG;
	/** Icon for next */
	public final static String FI_ICON_NEXT = "next" + FileNames.EX_PNG;
	/** Icon for previous */
	public final static String FI_ICON_PREVIOUS = "previous" + FileNames.EX_PNG;
	/** Icon for larger font */
	public final static String FI_ICON_LARGER = "larger" + FileNames.EX_PNG;
	/** Icon for smaller font */
	public final static String FI_ICON_SMALLER = "smaller" + FileNames.EX_PNG;
	/** Icon for saving file */
	public final static String FI_ICON_SAVE = "disk" + FileNames.EX_PNG;
	/** Icon for opening file */
	public final static String FI_ICON_OPEN = "folder" + FileNames.EX_PNG;
	/** Icon for left shift */
	public final static String FI_ICON_LEFT = "left" + FileNames.EX_PNG;
	/** Icon for right shift */
	public final static String FI_ICON_RIGHT = "right" + FileNames.EX_PNG;
	
	/** Laboratory logo */
	public final static String FI_LOGO_LAB = "lia" + FileNames.EX_JPEG;
	/** University logo */
	public final static String FI_LOGO_UNIV = "uapv" + FileNames.EX_GIF;
}
