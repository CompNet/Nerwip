package gs.yasa.annotationviewer;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import gs.yasa.sne.common.Annotation;




/**
 * A main window that contains tabs and annotations.
 * @author yasa akbulut
 * @version 1
 *
 */
public class MainWindow {

	/**
	 * frame to contain tabs and annotations
	 */
	JFrame frame;
	/**
	 * tab to represent a tool's annotations.
	 */
	JTabbedPane tabbedPane;
	
	/**
	 * Constructor
	 * @author yasa akbulut
	 */
	public MainWindow()
	{
		frame = new JFrame("Main Window");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(1000, 500);
		tabbedPane = new JTabbedPane();		
		frame.setContentPane(tabbedPane);
	}
	
	/**
	 * Shows the annotation frame.
	 * @author yasa akbulut
	 */
	public void show()
	{
		frame.setVisible(true);
	}
	
	/**
	 * Adds a new tab for each annotation tool.
	 * @param input
	 * @param annotations
	 * @param toolName
	 * @param toolConfigurationName
	 * @author yasa akbulut
	 */
	public void addAnnotationTab(String input, ArrayList<Annotation> annotations, String toolName, String toolConfigurationName)
	{
		AnnotationViewerPanel viewer = new AnnotationViewerPanel(input);
		viewer.styleText(annotations);

		tabbedPane.add(toolName+"("+toolConfigurationName+")", viewer);
		
	}

}
