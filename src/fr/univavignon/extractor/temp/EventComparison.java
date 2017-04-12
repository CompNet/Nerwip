package fr.univavignon.extractor.temp;

/* Nerwip - Named Entity Extraction in Wikipedia Pages
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.univavignon.extractor.data.event.Event;
import fr.univavignon.nerwip.tools.string.StringTools;

/**
 * This class compares pairs of events 
 * which have different persons 
 * by calculating their total similarity. 
 * 
 * @author Sabrine Ayachi
 */
public class EventComparison
{	/**
	 * Calculates the similarity between two 
	 * dates.
	 * 
	 * @param date1
	 * 		The first date.
	 * @param date2
	 *      The second date.
	 * @return
	 * 		The similarity between these dates.
	 */
	public static double dateSimilarity(String date1, String date2)
	{	double dateSimilarity =0;
		if(date1 == date2)
		{	dateSimilarity = 1;}
			String c1 = convertDate(date1);
			String [] splits1 = c1.split(" ");
			String c2 = convertDate(date2);
			String [] splits2 = c2.split(" ");
			if(splits1[3] == splits2[3]) //the same year
			{	if ((splits1[1] == splits2[1]) & (splits1[2] == splits2[2]))
					dateSimilarity = 1; 
				else
					dateSimilarity = 0.5;
			}
			else 
				dateSimilarity = 0;

			return dateSimilarity;
	}

	/**
	 * This method calculates the number 
	 * of entity types found on a pair of events.
	 * 
	 * @param e1
	 * 		The first event.
	 * @param e2
	 *       The second event.
	 * @return
	 * 		The number of entity types found in both events .
	 */
	public static int entityTypesNumber(Event e1, Event e2)
	{	int n = 0;
		
		if((e1.getPerson() != null) || (e2.getPerson() != null))
			n++;
		if((e1.getFunction() != null) || (e2.getFunction() != null))
			n++;
		if((e1.getDate() != null) || (e2.getDate() != null))
			n++;
		if((e1.getOrganization() != null) || (e2.getOrganization() != null))
			n++;
		if((e1.getLocation() != null) || (e2.getLocation() != null))
			n++;
		if((e1.getProduction() != null) || (e2.getProduction() != null))
			n++;
		if((e1.getMeeting() != null) || (e2.getMeeting() != null))
			n++;

		return n;
	}

	/**
	 * Calculates the similarity between
	 * two events.(It is used only for pair of events 
	 * having different persons)
	 * 
	 * @param e1
	 * 		The first event.
	 * @param e2
	 *      The second event.
	 * @param text
	 *      ???
	 * @return
	 * 		The similarity between these events.
	 */
	public static double compareOnePairOfEvents(Event e1, Event e2, String text)
	{	//text is the answer of spotlight
		double pairSim = 0;
		double locationSim = 0;
		double organizationSim = 0;
		double personSim = 0;
		double functionSim = 0;
		double dateSim = 0;
		double productionSim = 0;
		double meetingSim = 0;
		
		//List<String> entityList = new ArrayList<String>();
		//List<String> offsetList = new ArrayList<String>();
		//entityList = SpotlightTools.getEntitySpotlight(text);
		//offsetList = SpotlightTools.getOffsetSpotlight(text);

		int n = entityTypesNumber(e1, e2);

		if (e1.getPerson() != null & e2.getPerson() != null)
		{	//compare person type entities
			String uri1 = verifyPerson(e1, text);
			String uri2 = verifyPerson(e2, text);
			if ((uri1 != null) & (uri2 != null)) 
			{	if (uri1 == uri2)
					personSim = 1;
				else 
					personSim = 0;
			}
			else 
				personSim = StringTools.getNormalizedLevenshtein(e1.getPerson().getStringValue(), e2.getPerson().getStringValue()); 
		}
		else
		{	personSim = 0;
			System.out.print("person similarity is null");
		}

		if(e1.getLocation() != null & e2.getLocation() != null)
		{	//compare location type entities
			String uri1 = verifyLocation(e1, text);
			String uri2 = verifyLocation(e2, text);
			if((uri1 != null) & (uri1 != null)) 
			{	if (uri1 == uri2)
					locationSim = 1;
				else 
					locationSim = 0;
			}
			else 
				locationSim = StringTools.getNormalizedLevenshtein(e1.getLocation().getStringValue(), e2.getLocation().getStringValue());
		}
		else 
		{	locationSim = 0;
			System.out.println("location similarity is null");
		}

		if (e1.getOrganization() != null & e2.getOrganization() != null)
		{	//compare organization type entities
			String uri1 = verifyOrganization(e1, text);
			String uri2 = verifyOrganization(e2, text);
			if((uri1 != null) & (uri2 != null)) 
			{	if (uri1 == uri2)
					organizationSim = 1;
				else 
					organizationSim = 0;
			}
			else 
				organizationSim = StringTools.getNormalizedLevenshtein(e1.getOrganization().getStringValue(), e2.getOrganization().getStringValue());
		}
		else
		{	organizationSim = 0;
			System.out.println("organization similarity is null");
		}

		if(e1.getFunction() != null & e2.getFunction() != null)
		{	//compare function type entities
			String uri1 = verifyFunction(e1, text);
			String uri2 = verifyFunction(e2, text);
			if((uri1 != null) & (uri2 != null)) 
			{	if (uri1 == uri2)
					functionSim = 1;
				else 
					functionSim = 0;
			}
			else 
				functionSim = StringTools.getNormalizedLevenshtein(e1.getFunction().getStringValue(), e2.getFunction().getStringValue());
		}
		else 
		{	functionSim = 0;
			System.out.println("function similarity is null");
		}

		if (e1.getProduction() != null & e2.getProduction() != null)
		{	//compare production type entities
			String uri1 = verifyProduction(e1, text);
			String uri2 = verifyProduction(e2, text);
			if((uri1 != null) & (uri2 != null)) 
			{	if (uri1 == uri2)
					productionSim = 1;
				else 
					productionSim = 0;
			}
			else 
				productionSim = StringTools.getNormalizedLevenshtein(e1.getProduction().getStringValue(), e2.getProduction().getStringValue());
		}
		else 
		{	productionSim = 0;
			System.out.println("production similarity is null");
		}
		
		if (e1.getMeeting() != null & e2.getMeeting() != null)
		{	//compare meeting type entities
			String uri1 = verifyMeeting(e1, text);
			String uri2 = verifyMeeting(e2, text);
			if((uri1 != null) & (uri2 != null)) 
			{	if (uri1 == uri2)
					meetingSim = 1;
				else 
					meetingSim = 0;
			}
			else 
				meetingSim = StringTools.getNormalizedLevenshtein(e1.getMeeting().getStringValue(), e2.getMeeting().getStringValue()); 
		}
		else 
		{	meetingSim = 0;
			System.out.println("meeting similarity is null");
		}
		
		if(e1.getDate() != null & e2.getDate() != null)
		{	//compare date type entities
			dateSim = dateSimilarity(e1.getDate().getStringValue(), e2.getDate().getStringValue());
			System.out.println("date similarity algorithm");
		}
		else 
		{	dateSim = 0;
			System.out.println("date similarity is null");
		}

		pairSim = (1/n) * (personSim + meetingSim + locationSim + functionSim + productionSim + organizationSim + dateSim);
		System.out.println("pairsimilarity = " + pairSim);
		
		return pairSim;
	}
	
	/**
	 * ???????
	 * 
	 * @param events
	 * 		????????????
	 * @param text
	 * 		????????????
	 */
	public static void compareAllPairsOfEvents( List<Event> events, String text)
	{	double similarity=0;
		for(int i=0; i<=events.size()-1; i++)
		{	for(int j=0; j<=events.size()-1; j++)
			{	if(i!=j && j>i)
				{	similarity = compareOnePairOfEvents(events.get(i), events.get(j), text);
					System.out.println("event " + i + events.get(i));
					System.out.println("event " + j + events.get(j));
					
					System.out.println("similarity between event " + i + " and event " + j + " = " + similarity);
				}
			}
		}
	}
}
