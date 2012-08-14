package gs.yasa.evaluator.gui;

import gs.yasa.evaluator.EvaluationOutcome;
import gs.yasa.evaluator.OutcomeClasses;
import gs.yasa.evaluator.ToolOutput;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;




/**
 * A class for a result panel
 * @author yasa akbulut
 * @version 1
 */
public class ResultsPanel extends JPanel {

	/**
	 * version identifier for Serializable class
	 */
	private static final long serialVersionUID = -211476105247695610L;

	/**
	 * a text area for correct or incorrect type
	 */
	JTextArea textArea;
	/**
	 * result by each tool
	 */
	HashMap<OutcomeClasses, TypeResult> results;
	/**
	 * Constructor. Adds on a panel which contain the results (every outcome possible)
	 * for each tool.
	 * @param output
	 */
	public ResultsPanel(ToolOutput output)
	{
		super(new BorderLayout());
		textArea = new JTextArea(14,20);
		JScrollPane scrollPane = new JScrollPane(textArea);
		results = new HashMap<OutcomeClasses, TypeResult>();
		for (EvaluationOutcome outcome: output.results) {
			if(results.containsKey(outcome.outcomeClass))
			{
				if(outcome.correctType)
					results.get(outcome.outcomeClass).correctType++;
				else
					results.get(outcome.outcomeClass).incorrectType++;
			}else
			{
				results.put(outcome.outcomeClass, new TypeResult());
				if(outcome.correctType)
					results.get(outcome.outcomeClass).correctType++;
				else
					results.get(outcome.outcomeClass).incorrectType++;
			}
		}
		
		for(OutcomeClasses outcomeClass: OutcomeClasses.values())
		{
			if(!results.containsKey(outcomeClass))
				results.put(outcomeClass, new TypeResult());
		}
		
		for (Entry<OutcomeClasses, TypeResult> entry : results.entrySet()) {
			textArea.append(entry.getKey().toString()+" (correct type):"+entry.getValue().correctType+"\n");
			textArea.append(entry.getKey().toString()+" (incorrect type):"+entry.getValue().incorrectType+"\n");
		}
		super.add(scrollPane);
		
	}
	
	/**
	 * Adds different outcomes possible to google chart
	 * @return an ArrayList of google chart values
	 * @author yasa akbulut
	 */
	public ArrayList<Integer> getGoogleChartValues(){
		ArrayList<Integer> googleChartValues = new ArrayList<Integer>();
		googleChartValues.add(results.get(OutcomeClasses.TRUE_POSITIVE).correctType);
		googleChartValues.add(results.get(OutcomeClasses.TRUE_POSITIVE).incorrectType);
		googleChartValues.add(results.get(OutcomeClasses.PARTIAL_POSITIVE).correctType);
		googleChartValues.add(results.get(OutcomeClasses.PARTIAL_POSITIVE).incorrectType);
		googleChartValues.add(results.get(OutcomeClasses.EXCESS_POSITIVE).correctType);
		googleChartValues.add(results.get(OutcomeClasses.EXCESS_POSITIVE).incorrectType);
		googleChartValues.add(results.get(OutcomeClasses.FALSE_POSITIVE).correctType
				+results.get(OutcomeClasses.FALSE_POSITIVE).incorrectType);
		googleChartValues.add(results.get(OutcomeClasses.FALSE_NEGATIVE).correctType
				+results.get(OutcomeClasses.FALSE_NEGATIVE).incorrectType);
		return googleChartValues;
	}

	
}
