
package gs.yasa.outputunifier;

/**
 * This class is an exception class for unsupported tools.
 * @author yasa akbulut
 * @version 1
 */
public class UnsupportedToolException extends Exception {
  /**
   * Constructor, exception for unsupported tools : tools that does not exist
   * in class AnnotationTool
 * @param input
 */
public UnsupportedToolException(String input) {
		// TODO Auto-generated constructor stub
		super("Invalid tool name, or tool not supported:"+input);
  }


  /**
 * version identifier for Serializable class
 */
private static final long serialVersionUID =  1L;

}
