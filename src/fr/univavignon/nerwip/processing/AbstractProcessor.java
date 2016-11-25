package fr.univavignon.nerwip.processing;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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

import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class is used to represent or implement recognizers, resolvers and linkers.
 * The former case corresponds to external tools, i.e. applications
 * executed externally. The latter to tools invocable internally,
 * i.e. programmatically, from within Nerwip. 
 * 		 
 * @author Yasa Akbulut
 * @author Samet Atdağ
 * @author Vincent Labatut
 */
public abstract class AbstractProcessor implements InterfaceProcessor
{	
	/**
	 * Builds a new processor, with default parameters.
	 */
	public AbstractProcessor()
	{	
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CACHING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not cache should be used */
	protected boolean cache = true;
	
	@Override
	public boolean doesCache()
	{	return cache;
	}
	
	@Override
	public void setCacheEnabled(boolean enabled)
	{	this.cache = enabled;
	}
	
	/////////////////////////////////////////////////////////////////
	// RAW RESULTS	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Whether or not to write the raw results in a text file (for debug purposes) */
	protected boolean outRawResults = false;
	
	@Override
	public boolean doesOutputRawResults()
	{	return outRawResults;
	}
	
	@Override
	public void setOutputRawResults(boolean enabled)
	{	this.outRawResults = enabled;
	}
	
	/////////////////////////////////////////////////////////////////
	// STRING		 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	ProcessorName name = getName();
		String result = name.toString();
		return result;
	}
}
