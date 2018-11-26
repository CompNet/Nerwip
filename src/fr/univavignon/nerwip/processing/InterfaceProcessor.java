package fr.univavignon.nerwip.processing;

import fr.univavignon.common.data.article.Article;

/**
 * Interface implemented by all classes able to perform
 * at least one task among mention recognition, entity linking
 * and coreference resolution.
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
	// RECOGNIZER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Indicates if this processor was configured as a recognizer
	 * when built.
	 *  
	 * @return
	 * 		{@code true} iff this processor is currently a recognizer.
	 */
	public boolean isRecognizer();
	
	/////////////////////////////////////////////////////////////////
	// RESOLVER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Indicates if this processor was configured as a resolver
	 * when built.
	 *  
	 * @return
	 * 		{@code true} iff this processor is currently a resolver.
	 */
	public boolean isResolver();
	
	/////////////////////////////////////////////////////////////////
	// LINKER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Indicates if this processor was configured as a linker
	 * when built.
	 *  
	 * @return
	 * 		{@code true} iff this processor is currently a linker.
	 */
	public boolean isLinker();
	
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
	 * {@link InterfaceResolver#resolve(Article)} and 
	 * {@code InterfaceLinker#link(Article)}
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
