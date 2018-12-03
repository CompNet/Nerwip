package fr.univavignon.search.clustering;

import java.util.Arrays;

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

import java.util.Set;
import java.util.TreeSet;

/**
 * Class processing the Silhouette measure, which assesses cluster quality
 * when doing cluster analysis.
 * https://en.wikipedia.org/wiki/Silhouette_(clustering)
 * 
 * @author Vincent Labatut
 */
public class Silhouette
{	
	/////////////////////////////////////////////////////////////////
	// TEXT				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Processes the Silhouette measure for the specified distance matrix
	 * and partition (i.e. clustering). In the partition, the element numbering 
	 * must start from zero.
	 * 
	 * @param dist
	 * 		Distance matrix.
	 * @param partition
	 * 		Considered flat clustering, as a partition.
	 * @return
	 * 		A real value representing the Silhouette value.
	 */
	public static double processSilhouette(double dist[][], Set<Set<Integer>> partition)
	{	// process similarity between each instance and each cluster
		double[] intraDists = new double[dist.length];
		double[] bestInterDists = new double[dist.length];
		Arrays.fill(bestInterDists, 1);
		TreeSet<Integer> singletons = new TreeSet<Integer>();
		for(Set<Integer> part: partition)
		{	// processing the instances belonging to the current cluster
			for(int i: part)
			{	intraDists[i] = 0;
				if(part.size()==1)
					singletons.add(i);
				else
				{	for(int j: part)
					{	if(i!=j)
							intraDists[i] = intraDists[i] + dist[i][j];
					}
					intraDists[i] = intraDists[i] / (part.size()-1);
				}
			}
			
			// process the other instances
			Set<Integer> others = new TreeSet<Integer>();
			for(int i=0;i<dist.length;i++)
			{	if(!part.contains(i))
					others.add(i);
			}
			for(int i: others)
			{	double tempDist = 0;
				for(int j: part)
					tempDist = tempDist + dist[i][j];
				tempDist = tempDist / part.size();
				if(tempDist<bestInterDists[i])
					bestInterDists[i] = tempDist;
			}
		}
		
		// processing the measure for each instance
		double[] indivSil = new double[dist.length];
		for(int i=0;i<dist.length;i++)
		{	if(singletons.contains(i) || intraDists[i]==bestInterDists[i])
				indivSil[i] = 0;
			else if(intraDists[i]<bestInterDists[i])
				indivSil[i] = 1 - intraDists[i]/bestInterDists[i];
			else
				indivSil[i] = bestInterDists[i]/intraDists[i] - 1;
		}
		
		// average to get the overall silhouette
		double sum = 0;
		for(double s: indivSil)
			sum = sum + s;
		double result = sum / indivSil.length;
		
		return result;
	}
}
