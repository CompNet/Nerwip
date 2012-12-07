package gs.yasa.sne.common;

public class ImproperCandidateException extends Exception {


	/**
	 * version identifier for Serializable class
	 */
	private static final long serialVersionUID = 1L;

	public ImproperCandidateException(AnnotationCandidate candidate)
	{
		super(candidate.toString());
	}
	
}
