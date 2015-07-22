package tr.edu.gsu.nerwip.eventcomparison.stringsimilaritytools;

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


import org.apache.commons.lang3.StringUtils;
import java.lang.Math;

import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class is used to calculate the similarity
 * between two strings based on the Normalized
 * Levenshtein Distance metric.
 * <br/>
 * <b>Note:</b> the Levenshtein Distance metric used here
 * is defined by the number of changes needed to change 
 * one String into another, where each change is a single 
 * character modification (deletion, insertion or substitution).
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class NLDistance 
{
	
    /////////////////////////////////////////////////////////////////
    // LOGGING			/////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /** Common object used for logging */
    protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	

    /**
    * This method calculate the Normalized Levenshtein  
    * Distance between two strings.
    * 
    * @param S
    * 		The first string.
    * @param T
    *       The second string.
    * @return
    * 		The Normalized Levenshtein Distance.
    */
	public static double getLevNorm(String S, String T) 
	{   logger.increaseOffset();
	    double levNorm = 0;
		int levDistance = 0;
		levDistance =  StringUtils.getLevenshteinDistance(S, T);
		
		int longestStringLength;
		longestStringLength = Math.max(S.length(), T.length());
		
		//logger.log("longestStringLength : " + longestStringLength);
	    logger.log("Levenshtein Distance between " + S + " and " + T + ":" + levDistance);
		
		levNorm = (double) levDistance / longestStringLength ;
		logger.log("The Normalized Levenshtein Distance between " + S + " and " + T + " =  " + levNorm);
		
        return levNorm;
        
	}
	
}
