package fr.univavignon.tools.web;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import fr.univavignon.nerwip.tools.log.HierarchicalLogger;
import fr.univavignon.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class contains a set of methods related to Web communication.
 * 
 * @author Vincent Labatut
 */
public class WebTools
{	
	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// ANSWERS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Receives an object representing an HTTP answer, reads its content
	 * and returns the corresponding string.
	 *  
	 * @param response
	 * 		Object representing the HTTP answer.
	 * @return
	 * 		A String representing the content of the answer.
	 * 
	 * @throws IllegalStateException
	 * 		Problem while reading the answer.
	 * @throws IOException
	 * 		Problem while reading the answer.
	 */
	public static String readAnswer(HttpResponse response) throws IllegalStateException, IOException
	{	logger.log("Read HTTP answer");
		logger.increaseOffset();
		
		// init reader
		HttpEntity entity = response.getEntity();
		InputStream stream = entity.getContent();
		InputStreamReader streamReader = new InputStreamReader(stream,"UTF-8");
		BufferedReader bufferedReader = new BufferedReader(streamReader);
		
		// read answer
		StringBuffer stringBuffer = new StringBuffer();
		String line;
		int nbr = 0;
		while((line = bufferedReader.readLine())!=null)
		{	stringBuffer.append(line+"\n");
			nbr++;
			logger.log("Line:" +line);
		}
		logger.log("Lines read: "+nbr);
		
		String result = stringBuffer.toString();
		logger.decreaseOffset();
		return result;
	}
}