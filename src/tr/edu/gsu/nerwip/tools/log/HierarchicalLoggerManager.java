package tr.edu.gsu.nerwip.tools.log;

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

import java.util.HashMap;
import java.util.Map;

/**
 * General manager to handle all
 * the created {@link HierarchicalLogger} objects.
 * 
 * @version 1.2
 * 
 * @author Vincent Labatut
 */
public class HierarchicalLoggerManager
{	
    /////////////////////////////////////////////////////////////////
	// RETRIEVE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of already created hierarchical loggers */
	private static final Map<String,HierarchicalLogger> loggers = new HashMap<String, HierarchicalLogger>();
	
	/**
	 * Fetches or creates a new 
	 * <i>anonymous</i> logger (note only
	 * one such logger can exist at once).
	 * 
	 * @return
	 * 		The anonymous logger.
	 */
	public static synchronized HierarchicalLogger getHierarchicalLogger()
	{	HierarchicalLogger result = getHierarchicalLogger(null);
		return result;
	}

	/**
	 * Retrieves an existing hierarchical logger
	 * from its name, or creates a new one
	 * if it does not exist.
	 * 
	 * @param name
	 * 		Name of the requested logger.
	 * @return
	 * 		The retrieved or created logger.
	 */
	public static synchronized HierarchicalLogger getHierarchicalLogger(String name)
	{	HierarchicalLogger result = loggers.get(name);
		if(result==null)
		{	result = new HierarchicalLogger(name);
			loggers.put(name,result);
		}
		
		return result;
	}
	
    /////////////////////////////////////////////////////////////////
	// CLOS			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Closes all existing loggers.
	 */
	public static synchronized void closeLoggers()
	{	for(HierarchicalLogger logger: loggers.values())
			logger.close();
	}
}
