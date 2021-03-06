package tr.edu.gsu.nerwip.edition;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011 Yasa Akbulut, Burcu Küpelioğlu & Vincent Labatut
 * Copyright 2012 Burcu Küpelioğlu, Samet Atdağ & Vincent Labatut
 * Copyright 2013 Samet Atdağ & Vincent Labatut
 * Copyright 2014-15 Vincent Labatut
 * 
 * This file is part of Nerwip - Named Entity Extraction in Wikipedia Pages.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is distributed in the hope 
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Nerwip - Named Entity Extraction in Wikipedia Pages.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tr.edu.gsu.nerwip.tools.file.FileTools;

/**
 * This class displays a dialog containing
 * a text area. The text content is retrieved
 * from the specified file.
 * 
 * @author Vincent Labatut
 */
public class TextDialog extends JDialog implements ActionListener
{	/** Class id */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * Builds a new dialog using the
	 * specified title and text file.
	 * The content of the text file
	 * is automatically loaded and
	 * displayed in the dialog.
	 * 
	 * @param parentFrame
	 * 		Frame containing this dialog.
	 * @param title
	 * 		Title of the dialog.
	 * @param textFile
	 * 		File containing the dialog text.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while loading the text file.
	 */
	public TextDialog(JFrame parentFrame, String title, String textFile) throws FileNotFoundException
	{	// call the parent
		super(parentFrame);

		// set options
		setTitle(title);
		setSize(800, 400);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// create components
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		add(panel);

		// add text area
		JScrollPane scrollPane = initContent(textFile);
		panel.add(scrollPane,BorderLayout.CENTER);
		
		// add button
		JButton button = new JButton();  
		button.addActionListener(this);  
		button.setText("OK");  
		panel.add(button,BorderLayout.SOUTH);  
		
		textPane.setCaretPosition(0);
	}
	
	/////////////////////////////////////////////////////////////////
	// TEXT C0NTENT		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Panel containing the text */
	private JEditorPane textPane;
	
	/**
	 * Builds and return the text container
	 * for this dialog.
	 * 
	 * @param textFile
	 * 		File containing the text to be displayed.
	 * @return
	 * 		Scroll pane containing the text.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while loading the text file.
	 */
	private JScrollPane initContent(String textFile) throws FileNotFoundException
	{	textPane = new JEditorPane();
		textPane.setContentType("text/html");
		JScrollPane result = new JScrollPane(textPane);
		textPane.setToolTipText("Click OK to close");
		textPane.setEditable(false);

		// get file content
		String text = FileTools.readTextFile(textFile);
		textPane.setText(text);
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// ACTION LISTENER	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void actionPerformed(ActionEvent arg0)
	{	setVisible(false);  
		dispose();
	}
}
