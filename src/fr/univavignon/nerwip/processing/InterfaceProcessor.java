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

import fr.univavignon.nerwip.data.article.Article;
import fr.univavignon.nerwip.data.entity.mention.Mentions;

/**
 * TODO
 * 		 
 * @author Vincent Labatut
 */
public interface InterfaceProcessor
{	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Return the (standardized) name of this processor.
	 * 
	 * @return 
	 * 		Name of this tool.
	 */
	public ProcessorName getName();
	
	/////////////////////////////////////////////////////////////////
	// CACHING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Indicates whether or not caching is
	 * enabled for this processor.
	 *  
	 * @return
	 * 		{@code true} iff caching is enabled.
	 */
	public boolean doesCache();
	
	/**
	 * Changes the cache flag. If {@code true}, the {@link InterfaceRecognizer#recognize(Article) process},
	 * {@link InterfaceResolver#resolve(Article, Mentions, InterfaceRecognizer)} and 
	 * {@code InterfaceLinker#link(Article, Mentions, Entities, InterfaceRecognizer, InterfaceResolver)}
	 * methods will first check if the results already
	 * exist as a file. In this case, they will be loaded
	 * from this file. Otherwise, the process will be
	 * conducted normally, then recorded.
	 * 
	 * @param enabled
	 * 		If {@code true}, the (possibly) cached files are used.
	 */
	public void setCacheEnabled(boolean enabled);
	
	/////////////////////////////////////////////////////////////////
	// RAW RESULTS	 		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the flag regarding the outputting of the processor
	 * raw results (i.e. before conversion to our format) in a text file.
	 * Useful for debugging, but it takes space. By default, this is disabled.
	 * <br/>
	 * Note that for external tools, this file generally <i>must</i> be produced,
	 * since it is used for communicating with the external tool. In this
	 * case, if this option is disabled, the file is deleted when not needed
	 * anymore.
	 * 
	 * @return
	 * 		{@code true} if the processor is set to output a text file.
	 */
	public boolean doesOutputRawResults();

	/**
	 * Changes the flag regarding the outputting of the processor
	 * raw results (i.e. before conversion to our format) in a text file.
	 * Useful for debugging, but it takes space. By default, this is disabled.
	 * <br/>
	 * Note that for external tools, this file generally <i>must</i> be produced,
	 * since it is used for communicating with the external tool. In this
	 * case, if this option is disabled, the file is deleted when not needed
	 * anymore.
	 * 
	 * @param enabled
	 * 		{@code true} to output a text file.
	 */
	public void setOutputRawResults(boolean enabled);
}
