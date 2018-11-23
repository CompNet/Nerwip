package fr.univavignon.nerwip.edition;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
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
import java.io.UnsupportedEncodingException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fr.univavignon.nerwip.tools.file.FileTools;

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
	 * @param tooltip 
	 * 		Tooltip message displaye in the textpane.
	 * @param textFile
	 * 		File containing the dialog text.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while loading the text file.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle the encoding.
	 */
	public TextDialog(JFrame parentFrame, String title, String tooltip, String textFile) throws FileNotFoundException, UnsupportedEncodingException
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
		JScrollPane scrollPane = initContent(textFile,tooltip);
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
	 * @param tooltip 
	 * 		Tooltip message.
	 * @return
	 * 		Scroll pane containing the text.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while loading the text file.
	 * @throws UnsupportedEncodingException
	 * 		Could not handle the encoding.
	 */
	private JScrollPane initContent(String textFile, String tooltip) throws FileNotFoundException, UnsupportedEncodingException
	{	textPane = new JEditorPane();
		textPane.setContentType("text/html");
		JScrollPane result = new JScrollPane(textPane);
		textPane.setToolTipText(tooltip);
		textPane.setEditable(false);

		// get file content
		String text = FileTools.readTextFile(textFile, "UTF-8");
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
