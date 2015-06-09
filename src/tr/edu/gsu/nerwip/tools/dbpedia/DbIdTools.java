package tr.edu.gsu.nerwip.tools.dbpedia;

import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

import com.hp.hpl.jena.query.*;

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


/**
 * This class handles DBpedia ids, and more particularly
 * the mapping between named entities and their DBpedia
 * ids.
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */


public class DbIdTools {
	
/////////////////////////////////////////////////////////////////
//LOGGING			/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
/** Common object used for logging */
protected static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

/**
 * This method takes an entity as parameter,
 * and retrieves its DBpedia id.
 * 
 * @param entity
 * 		Name of the entity.
 * @return
 * 		A String describing the DBpedia id.
*/
public static String getId(String entity) 
{  logger.increaseOffset();
   String result = null;
   //adress of the SPARQL endpoint
   String service="http://dbpedia.org/sparql";
   //SPARQL query
   String query = "select ?wikiPageID where {<http://fr.dbpedia.org/resource/"+ entity + ">"
		   +"dbpedia-owl:wikiPageID ?wikiPageID.}";
   
   QueryExecution e=QueryExecutionFactory.sparqlService(service, query);
   ResultSet rs=e.execSelect();
   while (rs.hasNext()) {
       QuerySolution qs=rs.nextSolution();
       logger.log("qs=" + qs);
       result = qs.toString();
   }
   
   
   
   
   return result;
}

}
