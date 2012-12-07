package gs.yasa.evaluator.gui;


import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import gs.yasa.evaluator.ToolOutput;



public class MainWindow {
	/**
	 * a frame to contain the results
	 */
	JFrame frame;
	/**
	 * a tab to contain the each tool's outcome result
	 */
	JTabbedPane tabbedPane;
	/**
	 * a HashMap to contain the google chart results for outcomes
	 */
	HashMap<String, ArrayList<Integer>> googleChartResults; 
	/**
	 * Generates a main window with tabs to show the outcomes from tools and google
	 * chart results
	 * @author yasa akbulut
	 */
	public MainWindow()
	{
		frame = new JFrame("Main Window");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(1000, 500);
		tabbedPane = new JTabbedPane();		
		frame.setContentPane(tabbedPane);
		googleChartResults =  new HashMap<String, ArrayList<Integer>>();
	}
	
	/**
	 * Sets visibility of a frame
	 * @author yasa akbulut
	 */
	public void show()
	{
		frame.setVisible(true);
	}
	/**
	 * Adds a tab which contains outcomes for each tool
	 * @param output
	 * @author yasa akbulut
	 */
	public void addResultsTab(ToolOutput output)
	{
		ResultsPanel panel = new ResultsPanel(output);
		tabbedPane.add(output.getToolName()+"("+output.getToolConfiguration()+")", panel);
		googleChartResults.put(output.getToolName()+"("+output.getToolConfiguration()+")"
				,panel.getGoogleChartValues());
		
	}
	
	/**
	 * Adds a tab for google chart for statistical for each outcome
	 * @author yasa akbulut
	 */
	public void addChartTab()
	{
		JPanel panel = new JPanel(new BorderLayout());
		try {
			URL url = new URL(getGoogleChartCode());
			ImageIcon icon = new ImageIcon(url);
			JLabel chart = new JLabel(icon);
			panel.add(chart);
			tabbedPane.add("chart",panel);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Sets chart properties; colors, parameters, etc.
	 * @return
	 * @author yasa akbulut
	 */
	public String getGoogleChartCode()
	{
		StringBuilder colors = new StringBuilder();
		colors.append("67f94d,");
		colors.append("43a333,");
		colors.append("4d6ef9,");
		colors.append("3349a3,");
		colors.append("f9be4d,");
		colors.append("a37c33,");
		colors.append("a33433,");
		colors.append("f94f4d");
		ArrayList<StringBuilder> chartDataLines = new ArrayList<StringBuilder>();
		ArrayList<String> chartLabelList =  new ArrayList<String>();
		int size = 0; // 8 for now
		for (Entry<String, ArrayList<Integer>> entry : googleChartResults.entrySet()) {
			size = entry.getValue().size();
		}
		
		
		for(int i=0; i<size; i++)
		{
			chartDataLines.add(new StringBuilder());
		}
		
		for (Entry<String, ArrayList<Integer>> entry: googleChartResults.entrySet()) {
			ArrayList<Integer> values = entry.getValue();
			for (int i = 0; i < values.size(); i++) {
				chartDataLines.get(i).append(values.get(i)+",");
			}
			chartLabelList.add(entry.getKey());
		}
		
		StringBuilder chartLabels = new StringBuilder();
		ListIterator<String> labelsIterator = chartLabelList.listIterator();
		while(labelsIterator.hasNext())
			labelsIterator.next();
		while(labelsIterator.hasPrevious())
		{
			chartLabels.append("|"+labelsIterator.previous());
		}
		
		StringBuilder finalString = new StringBuilder();
		for (StringBuilder stringBuilder : chartDataLines) {
			stringBuilder.deleteCharAt(stringBuilder.length()-1);
			finalString.append(stringBuilder.toString()+"|"); 
			
		}

		finalString.deleteCharAt(finalString.length()-1);
		
		

		
		return "https://chart.googleapis.com/chart?" +
				"cht=bhs" +
				"&chco=" + colors.toString() +
				"&chd=t:" + finalString.toString() +
				"&chxt=x,y" +
				"&chxl=1:" + chartLabels.toString() +
				"&chs=600x260" +
				"&chdl=True%20Positive%20(CT)" +
					"|True%20Positive%20(IT)" +
					"|Partial%20Positive%20(CT)" +
					"|Partial%20Positive%20(IT)" +
					"|Excess%20Positives%20(CT)" +
					"|Excess%20Positives%20(IT)" +
					"|False%20Positives" +
					"|False%20Negatives" +
				"&chtt=Comparaison%20des%20outils%20NER%20a%20partir%20d'un%20texte%20Wikipedia" +
				"&chds=0,1000";
	}
	
}
