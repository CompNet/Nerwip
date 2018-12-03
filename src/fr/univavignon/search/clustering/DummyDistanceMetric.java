package fr.univavignon.search.clustering;

import java.util.List;
import java.util.concurrent.ExecutorService;

import jsat.linear.Vec;
import jsat.linear.distancemetrics.DistanceMetric;

/**
 * Minimal implementation of a distance, used to compare events 
 * and cluster them with {@link MyPam} (JStat Library).
 *  
 * @author Vincent Labatut
 */
@SuppressWarnings({ "javadoc", "serial" })
public class DummyDistanceMetric implements DistanceMetric
{	double[][] dist;
	
	private DummyDistanceMetric()
	{	
		//
	}
	
	public DummyDistanceMetric(double[][] dist)
	{	this.dist = dist;
	}
	
	@Override
	public boolean supportsAcceleration()
	{	return false;
	}
	
	@Override
	public double metricBound()
	{	return 1;
	}
	
	@Override
	public boolean isSymmetric()
	{	return false;
	}
	
	@Override
	public boolean isSubadditive()
	{	return false;
	}
	
	@Override
	public boolean isIndiscemible()
	{	return true;
	}
	
	@Override
	public List<Double> getQueryInfo(Vec q)
	{	return null;
	}
	
	@Override
	public List<Double> getAccelerationCache(List<? extends Vec> vecs, ExecutorService threadpool) 
	{	return null;
	}
	
	@Override
	public List<Double> getAccelerationCache(List<? extends Vec> vecs)
	{	return null;
	}
	
	@Override
	public double dist(int a, Vec b, List<Double> qi, List<? extends Vec> vecs, List<Double> cache) 
	{	throw new IllegalArgumentException();
	}
	
	@Override
	public double dist(int a, Vec b, List<? extends Vec> vecs, List<Double> cache) 
	{	throw new IllegalArgumentException();
	}
	
	@Override
	public double dist(int a, int b, List<? extends Vec> vecs, List<Double> cache) 
	{	return dist[a][b];
	}
	
	@Override
	public double dist(Vec a, Vec b)
	{	throw new IllegalArgumentException();
	}
	
    @Override
    public DummyDistanceMetric clone()
    {	DummyDistanceMetric res = new DummyDistanceMetric();
    	res.dist = dist;
		return res;
    }
}
