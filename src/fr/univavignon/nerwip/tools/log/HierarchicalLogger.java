package fr.univavignon.nerwip.tools.log;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.univavignon.nerwip.tools.file.FileNames;
import fr.univavignon.nerwip.tools.time.TimeFormatting;

/**
 * Wrapper allowing to introduce
 * a hierarchy in the {@code Logger} API.
 * Messages are displayed with an offset
 * the user can control manually.
 * This can be used to reproduce
 * graphically the way methods
 * are chained, e.g.:</br>
 * {@code method1}</br>
 * {@code ..method2}</br>
 * {@code ....method3}</br>
 * {@code ..method4}</br>
 * where {@code method1} calls {@code method2}
 * and {@code method4}, and {@code method2}
 * calls {@code method3}.
 * 
 * @version 1.2
 * 
 * @author Vincent Labatut
 */
public class HierarchicalLogger
{	
	/**
	 * Builds a new hierarchical logger
	 * with the specifed name.
	 * 
	 * @param name
	 * 		Name of the new logger.
	 */
	HierarchicalLogger(String name)
	{	this.name = name;
	}

    /////////////////////////////////////////////////////////////////
	// ACTIVE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates whether or not this logger should log messages */
	private boolean enabled = true;

	/**
	 * Disable/enable this logger.
	 * When disabled, no message
	 * is logged anymore.
	 * 
	 * @param enabled
	 * 		If {@code true}, this logger logs message.
	 */
	public void setEnabled(boolean enabled)
	{	this.enabled = enabled;
	}
	
	
    /////////////////////////////////////////////////////////////////
	// NAME			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** General name of the logger */
	private String name = null;
	
	/**
	 * Change the name of this logger.
	 * 
	 * @param name
	 * 		New name of this logger.
	 */
	public void setName(String name)
	{	this.name = name;
	}
	
	/////////////////////////////////////////////////////////////////
	// THREADS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of threads managed since the begining of the logging */
	private int count = 0;
    /** Classic loggers used by this hierarchical logger */
	private final Map<Long,Logger> loggerMap = new HashMap<Long, Logger>();
	
	/**
	 * Retrieves or builds a basic logger
	 * in order to handle internally a thread.
	 * 
	 * @return
	 * 		A logger assigned to the current thread.
	 */
	private synchronized Logger getLogger()
	{	Thread thread = Thread.currentThread();
		long id = thread.getId();
		Logger result = loggerMap.get(id);
		if(result==null)
		{	try
			{	String loggerName = name + "." + count;
				thread.setName("Thread#"+count);
				// console handler
				ConsoleHandler ch = new ConsoleHandler();
				ch.setLevel(Level.ALL);
				HierarchicalFormatter formatter = new HierarchicalFormatter(10000,count);
				ch.setFormatter(formatter);
				
				// file handler
				String filename = FileNames.FO_LOG + File.separator 
					+ TimeFormatting.formatCurrentFileTime() + "."
					+  loggerName + "." 
					+ "%g"								// replaced by the file number during runtime
					+ LOG_EXTENSION;
				int size = 1024*1024*10;
				FileHandler fh = new FileHandler(filename,size,100);
				fh.setLevel(Level.ALL);
				fh.setEncoding("UTF-8");
				formatter = new HierarchicalFormatter(0,count);
				fh.setFormatter(formatter);
				
				// logger
				result = Logger.getLogger(loggerName);
				result.setLevel(Level.ALL);
				result.addHandler(ch);
				result.addHandler(fh);
				result.setUseParentHandlers(false);
				
				loggerMap.put(id,result);
				offsetMapLock.lock();
					offsetMap.put(id,0);
				offsetMapLock.unlock();
				count++;
			}
			catch(SecurityException e)
			{	e.printStackTrace();
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
		}
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Extension of the log file names (actually simple text files) */  
	private static final String LOG_EXTENSION = ".log";
	
	/**
	 * Closes all existing loggers.
	 */
	public synchronized void close()
	{	for(Logger logger: loggerMap.values())
		{	Handler[] handlers = logger.getHandlers();
			for(Handler handler: handlers)
			{	if(handler instanceof FileHandler)
					handler.close();
			}
		}		
	}
	
    /////////////////////////////////////////////////////////////////
	// OFFSET		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Offsets used to represent the current levels in the method calls hierarchies */
	private final Map<Long,Integer> offsetMap = new HashMap<Long, Integer>();
	/** corresponding lock */
	private final Lock offsetMapLock = new ReentrantLock();
    /**
     * Increases the current offset
     * of this logger.
    */
    public void increaseOffset()
    {	long id = Thread.currentThread().getId();
		offsetMapLock.lock();
    		Integer offset = offsetMap.get(id);
    		if(offset==null)
    			offset = 0;
    		offset++;
    		offsetMap.put(id,offset);
    	offsetMapLock.unlock();
    }
    
    /**
     * Decreases the current offset
     * of this logger.
     */
    public void decreaseOffset()
    {	long id = Thread.currentThread().getId();
		offsetMapLock.lock();
			Integer offset = offsetMap.get(id);
			if(offset==null)
				offset = 0;
			offset--;
			offsetMap.put(id,offset);
		offsetMapLock.unlock();
	}
    
    /**
     * Retrieves the offset associated to the
     * current thread.
     * 
     * @return
     * 		The offset of the current thread.
     */
    public int getOffset()
    {	long id = Thread.currentThread().getId();
		offsetMapLock.lock();
		int result = offsetMap.get(id);
		offsetMapLock.unlock();
		return result;
	}
    
	/////////////////////////////////////////////////////////////////
	// LOGGING		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /**
     * Logs a new message.
     * 
     * @param msg 
     * 		The message to be logged.
     */
    public void log(String msg)
    {	List<String> msgs = new ArrayList<String>();
    	msgs.add(msg);
    	log(msgs);
    }

    /**
     * Logs a collection of new messages.
     * 
     * @param msg 
     * 		The collection of messages to be logged.
     */
    public void log(Collection<String> msg)
    {	if(enabled)
    	{	if(msg==null || msg.isEmpty())
				msg = Arrays.asList("");
			Logger logger = getLogger();
    		Object params[] = {msg,getOffset()};
    		logger.log(Level.INFO,msg.iterator().next(),params);
    	}
    }
}
