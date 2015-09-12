package tr.edu.gsu.nerwip.eventcomparison;

import java.util.ArrayList;
import java.util.List;

import tr.edu.gsu.nerwip.data.event.Event;
import tr.edu.gsu.nerwip.tools.dbspotlight.SpotlightTools;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;

public class EventComparison {
	
	public double compareOnePairOfEvents(Event e1, Event e2, String text)
	{
		double pairSimilarity =0;
		double locationSimilarity;
		double organizationSimilarity;
		double personSimilarity;
		double functionSimilarity;
		double dateSimilarity;
		double productionSimilarity;
		double meetingSimilarity;
		
		//comparer les entités de type person
		
		
		
		List<String> entityList = new ArrayList<String>();
		List<String> offsetList = new ArrayList<String>();
		entityList = SpotlightTools.getEntitySpotlight(text);
		//offsetList = SpotlightTools.getOffsetSpotlight(text);
		
		if (e1.getLocation() != null & e2.getLocation() != null)
		{   //comparer les entités de type location
			//e1.getLocation().
			if (entityList.contains(e1.getLocation().getValue()) == true)
			{ int index = entityList.indexOf(e1.getLocation().getValue());
			  //String entity = entityList.get(index);
			  String offset = offsetList.get(index);
			  int offfset = Integer.parseInt(offset);
			  if ((e1.getLocation().getStartPos()) == offfset)
			  {}
				
			}
	    }
		else locationSimilarity = 0;
		
		if (e1.getOrganization() != null & e2.getOrganization() != null)
		{//comparer les entités de type organization
		}
		else organizationSimilarity = 0;
		
		if (e1.getFunction() != null & e2.getFunction() != null)
		{//comparer les entités de type function
			
		}
		else functionSimilarity = 0;
		if (e1.getProduction() != null & e2.getProduction() != null)
		{//comparer les entités de type production
		}
		else productionSimilarity = 0;
		if (e1.getMeeting() != null & e2.getMeeting() != null)
		{//comparer les entités de type production
		}
		else meetingSimilarity = 0;
		
		return pairSimilarity;
		
		
	}
	
	public void compareAllPairsOfEvents( List<Event> events, String text)
	{
		double similarity=0;
		for (int i=0; i<=events.size()-1; i++)
		{
			for (int j=0; j<=events.size()-1; j++)
			{
				if (i!=j && j>i)
				{
					similarity = compareOnePairOfEvents(events.get(i), events.get(j), text);
			        System.out.println("similarity between event " + i + " and event " + j + " = " + similarity);
			        
				}
				
			}
			
		}
		
	}

}
