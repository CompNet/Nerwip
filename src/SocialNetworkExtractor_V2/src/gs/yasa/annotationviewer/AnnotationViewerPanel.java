package gs.yasa.annotationviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationType;



/**
 * This class sets the display colors for each entity type on the frame.
 * @author yasa akbulut
 * @version 1
 *
 */
public class AnnotationViewerPanel extends JPanel {


	/**
	 * version identifier for Serializable class
	 */
	private static final long serialVersionUID = 8501538849713791317L;

	/**
	 * keeps entity types 
	 */
	private HashMap<AnnotationType, Style> annotationStyles;
	private boolean debug = false;
	
	JTextPane textPane;
	JPanel panel;
	
	/**
	 * This method sets a display color to each type of entity:
	 * person - yellow, location - orange, organization - cyan, misc - light gray,
	 * date - pink.  
	 * @param input
	 * @author yasa akbulut
	 */
	public AnnotationViewerPanel(String input)
	{
		super(new BorderLayout());
		textPane = new JTextPane();
		JScrollPane scrollPane = new JScrollPane(textPane);
		super.add(scrollPane);
		textPane.setText(input);
		StyledDocument doc = textPane.getStyledDocument();
		Style person = doc.addStyle("Yellow", null);
		StyleConstants.setBackground(person, Color.YELLOW);
		Style location = doc.addStyle("Orange", null);
		StyleConstants.setBackground(location, Color.ORANGE);
		Style organization = doc.addStyle("Cyan", null);
		StyleConstants.setBackground(organization, Color.CYAN);
		Style misc = doc.addStyle("LightGray", null);
		StyleConstants.setBackground(misc, Color.LIGHT_GRAY);
		Style date = doc.addStyle("Pink", null);
		StyleConstants.setBackground(date, Color.PINK);
		annotationStyles = new HashMap<AnnotationType, Style>();
		annotationStyles.put(AnnotationType.PERSON, person);
		annotationStyles.put(AnnotationType.LOCATION, location);
		annotationStyles.put(AnnotationType.ORGANIZATION, organization);
		annotationStyles.put(AnnotationType.MISC, misc);
		annotationStyles.put(AnnotationType.DATE, date);
		
	}
	
	/**
	 * This method enlists each entity type to a HashMap; annotationstyles if one
	 * detected.
	 * @param annotations
	 * @author yasa akbulut
	 */
	public void styleText(ArrayList<Annotation> annotations)
	{
		for (Annotation annotation : annotations) {
			StyledDocument document = textPane.getStyledDocument();
			if(debug)
				System.out.println(annotation);
			if(annotation.getEntityType()!=null)
			{		
				if(annotation.getEntityType().equals(AnnotationType.DATE))
					System.out.print("vbhnmj");
				Style style;
				if(annotation.getEntityType()==null)
				{
					System.out.println("NULL TYPE DETECTED:");
					System.out.println(annotation);
					style = annotationStyles.get(AnnotationType.MISC);
				}else
				{
					style = annotationStyles.get(annotation.getEntityType());
				}
				
				document.setCharacterAttributes(annotation.getStartPos(),
						annotation.getEndPos()-annotation.getStartPos(),
						style,
						true);
			}
		}
	}
	
	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}
	
}
