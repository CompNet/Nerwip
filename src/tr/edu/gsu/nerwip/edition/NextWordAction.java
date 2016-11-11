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

import java.awt.event.ActionEvent;

import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;

/**
 * Modified version of the {@link NextWordAction} class
 * from {@link DefaultEditorKit}. The changes allows
 * a different selection behavior in the JTextPane.
 * 
 * @author Vincent Labatut
 */
class NextWordAction extends TextAction
{	/** Class id */
	private static final long serialVersionUID = 1L;

	/**
     * Create this action with the appropriate identifier.
     * 
     * @param nm  
     * 		The name of the action, {@code Action.NAME}.
     * @param select 
     * 		Whether to extend the selection when
     *  	changing the caret position.
     */
    NextWordAction(String nm, boolean select)
    {	super(nm);
        this.select = select;
    }

    /** Whether the action is used for browsing ({@code false}) or selection ({@code true}) */
    private boolean select;
    
    @Override
	public void actionPerformed(ActionEvent e)
    {	JTextComponent target = getTextComponent(e);
    	Document document = target.getDocument();
    	int end = document.getLength();
    	
        if(target!=null)
        {	int offs = target.getCaretPosition();
            boolean failed = false;
            int oldOffs = offs;
            Element curPara = Utilities.getParagraphElement(target, offs);
            try
            {	
/////// 	VL: this block was added 
            	// check for trailing "'"
        		if(offs<end-2)
        		{	String c = target.getText(offs, 2);
        			if(c.equals("'s"))
        				offs = offs + 2;
        		}
        		// check for trailing space
        		if(offs<end-1)
            	{	String c = target.getText(offs, 1);
            		if(c.equals(" "))
            			offs = offs + 1;
            	}
/////// 	VL: end of modification            	
            	
            	offs = Utilities.getNextWord(target, offs);
            	
/////// 	VL: this block was added 
            	// remove trailing space from selection 
        		if(offs>0)			
            	{	String c = target.getText(offs-1, 1);
            		if(c.equals(" "))
            			offs = offs - 1;
            	}
        		// remove trailing "'s" from selection 
        		if(offs>1)
            	{	String c = target.getText(offs-2, 2);
            		if(c.equals("'s"))
            			offs = offs - 2;
            	}
/////// 	VL: end of modification            	
            	
            	if(offs>=curPara.getEndOffset() && oldOffs!=curPara.getEndOffset()-1)
        		{	// we should first move to the end of current
                    // paragraph (bug #4278839)
                    offs = curPara.getEndOffset() - 1;
        		}
            }
            catch (BadLocationException bl)
            {	if (offs != end)
                {	if(oldOffs != curPara.getEndOffset() - 1)
                	{	offs = curPara.getEndOffset() - 1;
                    }
                	else
                	{	offs = end;
                	}
                }
                else
                {	failed = true;
                }
            }
            if(!failed)
            {	if(select)
            	{	target.moveCaretPosition(offs);
                }
            	else
            	{	target.setCaretPosition(offs);
                }
            }
            else
            {	UIManager.getLookAndFeel().provideErrorFeedback(target);
            }
        }
    }
} 
