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

import java.util.Collection;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import tr.edu.gsu.nerwip.tools.time.TimeFormatting;

/**
 * Displays the log in a hierarchical way,
 * using the offset associated to the
 * log record by the logger.
 * 
 * @version 1.2
 * 
 * @author Vincent Labatut
 */
public class HierarchicalFormatter extends SimpleFormatter
{	
	/**
	 * Creates a new formatter with
	 * the specified limit for
	 * a line of text.
	 * 
	 * @param maxCols
	 * 		Limit of a line of text.
	 */
	public HierarchicalFormatter(int maxCols)
	{	super();
		this.maxCols = maxCols;
	}
	
	/**
	 * Creates a new formatter with
	 * the specified limit for a line of text
	 * and the specified thread id. 
	 * 
	 * @param maxCols
	 * 		Column limit for a line of text.
	 * @param threadNbr
	 * 		Number of the concerned thread.
	 */
	public HierarchicalFormatter(int maxCols, int threadNbr)
	{	this(maxCols);
		this.threadNbr = threadNbr;
	}
	
	/////////////////////////////////////////////////////////////////
	// THREAD		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of the concerned threads */
	private Integer threadNbr = null;
	
	/////////////////////////////////////////////////////////////////
	// FORMAT		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Largest line allowed */
	private int maxCols = 0;
	
	@Override
	public synchronized String format(LogRecord record)
	{	String result = "";
		Object[] parameters = record.getParameters();

		// hierarchical formatting
		if(parameters!=null && parameters.length>0)
		{	@SuppressWarnings("unchecked")
			Collection<String> msg = (Collection<String>)parameters[0];
			int offset = (Integer)parameters[1];
			
			// init
			String hour = TimeFormatting.formatTime(record.getMillis())+" ";
			int length = hour.length();
			String space = "";
			for(int i=0;i<length;i++)
				space = space + " ";
			String lvl = "";
			for(int i=0;i<offset;i++)
				lvl = lvl + ".";
			if(threadNbr!=null)
			{	hour = hour + "(" + threadNbr + ") ";
				space = space + "    ";
			}
			
			// display
			int i=0;
			for(String m: msg)
			{	String pre;
				if(i==0)
					pre = hour;
				else
					pre = space;
				result = result + pre + lvl + m;
				
				if(maxCols>0)
				{	String temp = null;
					while(result.length()>maxCols)
					{	if(temp==null)
							temp = result.substring(0,maxCols);
						else
							temp = temp + "\n" + result.substring(0,maxCols);
						result = result.substring(maxCols);
					}
					if(temp==null)
						temp = result;
					else
						temp = temp + "\n" + result;
					result = temp;
				}
				
				result = result + "\n";
				i++;
			}
		}
		
		// classic formatting
		else
			result = super.format(record);
		
		return result;
	}	
}