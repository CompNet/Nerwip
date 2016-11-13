package tr.edu.gsu.extractor.temp.tools.mediawiki;

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

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

/**
 * This class contains methods used to retrieve the Freebase
 * types associated to Wikipedia articles. 
 * 
 * @author Sabrine Ayachi
 *
 */
public class WikiTypeTools 
{	/////////////////////////////////////////////////////////////////
	// LOGGING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

   /**
    * This method takes a name of entity,
    * and retrieves all its Wikidata types.
    * <br/>
    * Those types must then be processed in order to
    * get the corresponding {@link EntityType} or
    * article category.
    * 
    * @param entity
    * 		Name of the entity.
    * @return
    * 		a List containing the Wikidata types of this entity.
    * 
    * @throws IOException 
    * 		Problem while retrieving the FB types.
    * @throws ClientProtocolException 
    * 		Problem while retrieving the FB types.
    * @throws ParseException
    * 		Problem while retrieving the FB types.
    */
	public static List<String> getAllTypes(String entity) throws ClientProtocolException, IOException   
	{	logger.increaseOffset();
		List<String> result = null;

		return result;
	}
}
