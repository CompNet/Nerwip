package tr.edu.gsu.nerwip.tools.dbpedia;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import org.apache.http.client.ClientProtocolException;


import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

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
 * This class contains methods used to retrieve the DBpedia
 * types associated to entities. 
 * 
 * @author Sabrine Ayachi
 * @author Vincent Labatut
 */
public class DbTypeTools 
{
	
   /////////////////////////////////////////////////////////////////
   //LOGGING			/////////////////////////////////////////////
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
   * 		a List containing the DBpedia types of this entity.*/
   public static List<String> getAllTypes(String entity) 
   {   logger.increaseOffset();
       //List<String> result = null;
       List<String> result = new ArrayList<String>();
   
       //adress of the SPARQL endpoint
       String service="http://dbpedia.org/sparql";
   
       //SPARQL query
       String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
       "PREFIX res: <http://dbpedia.org/resource/>" +
	   "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
       "select ?type where {<http://fr.dbpedia.org/resource/" + entity + "> rdf:type ?type.}";
   
       QueryExecution e=QueryExecutionFactory.sparqlService(service, query);
       ResultSet rs=e.execSelect();
       while (rs.hasNext()) 
       {
    	   QuerySolution qs=rs.nextSolution();
       
           String res = qs.toString();
           logger.log("res=" + res);
           
       }
       
       e.close();
      
       logger.decreaseOffset();
       return result;
       
   }
   
}
