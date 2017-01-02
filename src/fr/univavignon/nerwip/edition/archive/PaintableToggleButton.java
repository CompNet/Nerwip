package fr.univavignon.nerwip.edition.archive;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Action;
import javax.swing.JToggleButton;

/**
 * JToggleButton whose selected state actually
 * displays the background color. The original
 * {@link #paint} method was retrieved from
 * <a href="http://www.velocityreviews.com/forums/t131773-change-jtogglebutton-selected-color.html">Velocity Reviews</a>.
 * <br/>
 * <b>Source :</b> <a href="http://www.velocityreviews.com/forums/t131773-change-jtogglebutton-selected-color.html">http://www.velocityreviews.com/forums/t131773-change-jtogglebutton-selected-color.html</a>
 * 
 * @author s1w
 * @author Vincent Labatut
 */
class PaintableToggleButton extends JToggleButton
{	/** Class id */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Builds a new button
	 * from the specified action.
	 * 
	 * @param action
	 * 		Action attached to this button.
	 */
	public PaintableToggleButton(Action action)
	{	super(action);
	}
	
	/////////////////////////////////////////////////////////////////
	// SELECTED BACKGROUND	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Color modifier */
	private final static int MODIF = 50;
	/** Color used to paint background when selected */
	private Color darkBackground = null;
	
	@Override
	public void setBackground(Color color)
	{	super.setBackground(color);
		
		// derive darker color
		int r = Math.max(0,color.getRed()-MODIF);
		int g = Math.max(0,color.getGreen()-MODIF);
		int b = Math.max(0,color.getBlue()-MODIF);
		darkBackground = new Color(r,g,b);
	}
	
	/////////////////////////////////////////////////////////////////
	// PAINT		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void paintComponent(Graphics g)
	{	super.paintComponent(g);
		
		if(darkBackground!=null)
		{	if(isSelected())
		    {	int w = getWidth();
			    int h = getHeight();
			    g.setColor(getBackground());
			    g.fillRect(0, 0, w, h);
			    g.setColor(Color.BLACK);
			    String text = getText();
			    g.drawString(text, (w - g.getFontMetrics().stringWidth(text))/2 + 1, (h + g.getFontMetrics().getAscent())/2 - 1);
			}
			else
		    {	int w = getWidth();
			    int h = getHeight();
			    g.setColor(darkBackground);
			    g.fillRect(0, 0, w, h);
			    g.setColor(Color.GRAY);
			    String text = getText();
			    g.drawString(text, (w - g.getFontMetrics().stringWidth(text))/2 + 1, (h + g.getFontMetrics().getAscent())/2 - 1);
			}
		} 
	}
}