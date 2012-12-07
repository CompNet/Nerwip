package gs.yasa.outputunifier.opencalais;

import java.util.HashMap;
import java.util.Set;

/**
 * This class describes OpenCalais properties
 * @author yasa akbulut
 * @version 1
 */
public class Description {

	public HashMap<String, String> properties;
	
	/**
	 * Constructor
	 * @author yasa akbulut
	 */
	public Description()
	{
		properties = new HashMap<String, String>();
	}

	/**
	 * This method writes on console all tool's properties
	 */
	public void listAll()
	{
		System.out.println("Description:");
		Set<String> keys = properties.keySet();
		for (String key : keys) {
			System.out.println("\t"+key+":");
			System.out.println("\t\t"+properties.get(key));
		}
	}
	
	/**
	 * This method controls if a property exists
	 * @param propertyName
	 * @return true if tool has this property, else false 
	 * @author yasa akbulut
	 */
	public boolean hasProperty(String propertyName)
	{
		return properties.containsKey(propertyName);
	}
}
