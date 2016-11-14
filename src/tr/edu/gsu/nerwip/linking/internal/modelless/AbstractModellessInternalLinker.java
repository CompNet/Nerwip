package tr.edu.gsu.nerwip.linking.internal.modelless;

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

import tr.edu.gsu.nerwip.linking.LinkerException;
import tr.edu.gsu.nerwip.linking.internal.AbstractInternalConverter;
import tr.edu.gsu.nerwip.linking.internal.AbstractInternalLinker;

/**
 * This class is used to represent or implement linkers invocable 
 * internally, i.e. programmatically, from within Nerwip, and not
 * using any model, i.e. external files to be loaded (as opposed to
 * {@link AbstractModelBasedInternalLinker} linker.
 * 
 * @param <T>
 * 		Class of the converter associated to this linker.
 * @param <U>
 * 		Class of the internal representation of the mentions resulting from the detection.
 * 		 
 * @author Vincent Labatut
 */
public abstract class AbstractModellessInternalLinker<U,T extends AbstractInternalConverter<U>> extends AbstractInternalLinker<U,T>
{	
	/**
	 * Builds a new internal linker,
	 * using the specified options.
	 */
	public AbstractModellessInternalLinker()
	{	super();
	}
	
	/////////////////////////////////////////////////////////////////
	// MISC				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected void prepareLinker() throws LinkerException
	{	// nothing to do here
	}
}
