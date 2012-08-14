
package gs.yasa.outputunifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
/**
 * abstract class that contains two methods to read file and string
 * @author yasa akbulut
 * @version 1
 */
public abstract class OutputReader {

public boolean debug =  false;

  /**
   * abstract method to read a file
   * @author yasa akbulut
 * @param infile
 * @return
 * @throws FileNotFoundException if file does not found
 */
public abstract ArrayList<gs.yasa.sne.common.Annotation> read(File infile) throws FileNotFoundException ;

  /**
   * abstract method to read a string
   * @author yasa akbulut
 * @param text
 * @return
 */
public abstract ArrayList<gs.yasa.sne.common.Annotation> read(String text) ;

}
