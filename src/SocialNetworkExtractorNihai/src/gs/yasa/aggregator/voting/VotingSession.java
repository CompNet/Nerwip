package gs.yasa.aggregator.voting;

import java.util.HashMap;

/**This class starts a voting session to set the winner tool
 * 
 * @author yasa akbulut
 * 
 * @version 1
 *
 * @param <T>
 */
public class VotingSession<T> {

	/**
	 * contains hey values and vote values for each tool
	 */
	private HashMap<T, Integer> votes;
	
	/**constructor
	 * @author yasa akbulut
	 */
	public VotingSession()
	{
		votes = new HashMap<T, Integer>();
	}
	
	/**
	 * @param candidate
	 * @param i
	 */
	public void vote(T candidate, Integer i)
	{
		if(!votes.containsKey(candidate))
			votes.put(candidate, 0);
		votes.put(candidate, votes.get(candidate)+i);
	}
	
	/**This method 
	 * @author yasa akbulut
	 * @return returns the winner candidate tool if exists, null otherwise
	 */
	public T getWinningCandidate()
	{
		for (T candidate : votes.keySet()) {
			boolean won = true;
			for (T other : votes.keySet()) {
				if(votes.get(candidate)<votes.get(other))
					won=false;
			}
			if(won)
				return candidate;
		}
		return null;
	}
	
	
}
