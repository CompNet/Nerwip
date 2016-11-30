package fr.univavignon.nerwip.edition;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-16 Vincent Labatut et al.
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
import java.awt.Color;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TextAction;

import fr.univavignon.nerwip.data.entity.EntityType;
import fr.univavignon.nerwip.data.entity.mention.AbstractMention;
import fr.univavignon.nerwip.data.entity.mention.Mentions;
import fr.univavignon.nerwip.processing.ProcessorName;

/**
 * This class implements a panel designed to
 * display a text and highlights a list of mentions.
 * Each mention is displayed in a specific color.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class MentionEditorPanel extends JPanel implements AdjustmentListener, DocumentListener, CaretListener
{	/** Version identifier for Serializable class */
	private static final long serialVersionUID = 8501538849713791317L;

	/**
	 * Builds a panel meant to display the specified text
	 * and highlight the specified mentions. The tooltip
	 * is used to indicate the tool parameters (the tool
	 * name appears in the title of this tab pane).  
	 * 
	 * @param mainEditor
	 * 		{@link MentionEditor} window of the application. 
	 * @param text
	 * 		Full text of the article.
	 * @param linkedText
	 * 		Linked text of the article.
	 * @param mentions
	 * 		List of mentions to display.
	 * @param references 
	 * 		List of reference mentions (used for the mode).
	 * @param tooltip
	 * 		Complementary information.
	 * @param mode
	 * 		Display mode ({@code true} for types, {@code false} for comparison).
	 * @param typeDispl
	 * 		Boolean display switches for each entity type.
	 * @param linkState
	 * 		Display switch for hyperlink display.
	 * @param editable
	 * 		Whether or not text can be modified in this panel (only for reference).
	 * @param folder
	 * 		Folder associated to the estimated mentions (relative to the article folder).
	 */
	public MentionEditorPanel(MentionEditor mainEditor, String text, String linkedText, Mentions mentions, Mentions references, String tooltip,
		boolean mode, Map<EntityType,Boolean> typeDispl, boolean linkState, boolean editable, String folder)
	{	super(new BorderLayout());
		this.mainEditor = mainEditor;
		this.folder = folder;
		
		this.showHyperlinks = linkState;
		this.linkedText = linkedText;
		
		// setup panel
		textPane = new JTextPane();
		JScrollPane scrollPane = new JScrollPane(textPane);
		add(scrollPane);
		textPane.setText(text);
		textPane.setToolTipText(tooltip);
		SimpleAttributeSet sa = new SimpleAttributeSet();
		StyleConstants.setAlignment(sa, StyleConstants.ALIGN_JUSTIFIED);
		textPane.getStyledDocument().setParagraphAttributes(0, text.length(), sa, false);
		
		// set scrollbar
		scrollBar = scrollPane.getVerticalScrollBar();
		scrollBar.addAdjustmentListener(this);
		
		// remove annoying key bidings
		KeyStroke remove = KeyStroke.getKeyStroke("control shift O");
		InputMap im = textPane.getInputMap(JComponent.WHEN_FOCUSED);
		im.put(remove, "none");

		// prevent/allow text edition
		setEditable(editable);
		
		// change selection behavior
		TextAction action = new NextWordAction("selection-next-word", true);
		textPane.getActionMap().put("selection-next-word",action);
		
		// listen to position changes
		textPane.addCaretListener(this);
		
		// mentions
		this.mentions = mentions;
		this.references = references;
		mentionSwitches.putAll(typeDispl);
		
		// highlighting styles
		initStyles();
		
		// display mode
		this.mode = mode;
		
		// update
		updateHighlighting();
	}
	
	/////////////////////////////////////////////////////////////////
	// PANEL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Panel used for display */
	private JTextPane textPane;
	
	/////////////////////////////////////////////////////////////////
	// FONT SIZE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Modifies the size of the font used to display the text.
	 * 
	 * @param delta
	 * 		How much to decrease/increase the font.
	 */
	public void changeFontSize(int delta)
	{	Font font = textPane.getFont();
		int size = Math.max(font.getSize() + delta,5);
		String name = font.getName();
		int style = font.getStyle();
		Font newFont = new Font(name,style,size);
		textPane.setFont(newFont);
	}
	
	/**
	 * Modifies the size of the font used to display the text.
	 * 
	 * @param size
	 * 		New font size.
	 */
	public void setFontSize(int size)
	{	Font font = textPane.getFont();
		String name = font.getName();
		int style = font.getStyle();
		Font newFont = new Font(name,style,size);
		textPane.setFont(newFont);
	}

	/**
	 * Returns the current size of the font used to display the text.
	 * 
	 * @return
	 * 		The font size.
	 */
	public int getFontSize()
	{	Font font = textPane.getFont();
		int result = font.getSize();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// EDITABLE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Focus listener used when disabling edition in the textpane */
	private final FocusListener focusListener = new FocusListener()
	{	@Override
		public void focusLost(FocusEvent e)
		{	//
		}
		
		@Override
		public void focusGained(FocusEvent e)
		{	textPane.getCaret().setVisible(true);	
		}
	};
	
	/**
	 * Sets up the textpane in order to make it editable
	 * or non-editable.
	 * 
	 * @param editable
	 * 		If {@code true}, the textpane is editable.
	 */
	public void setEditable(boolean editable)
	{	textPane.setEditable(editable);
		if(editable)
		{	textPane.getDocument().addDocumentListener(this);
			textPane.removeFocusListener(focusListener);
		}
		else
		{	textPane.getCaret().setVisible(true);
			textPane.getDocument().removeDocumentListener(this);
			textPane.addFocusListener(focusListener);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// SCROLLBAR		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Vertical scrollbar of the scrollpane */
	private JScrollBar scrollBar;
	
	/**
	 * Changes the position of the current
	 * vertical scroll bar.
	 * 
	 * @param index
	 * 		New position of the scrollbar.
	 */
	public void setScrollPosition(int index)
	{	scrollBar.setValue(index);
	}
	
	/////////////////////////////////////////////////////////////////
	// MAIN EDITOR		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** CleanReferenceFiles window of the application */
	private MentionEditor mainEditor;
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Folder associated to the estimated mentions */
	private String folder = null;
	
	/**
	 * Returns the folder associated
	 * to the estimated mentions.
	 * 
	 * @return
	 * 		Folder containing the estimated mentions.
	 */
	public String getFolder()
	{	return folder;
	}

	/////////////////////////////////////////////////////////////////
	// ADJUSTMENT LISTENER	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e)
	{	int index = scrollBar.getValue();
		mainEditor.setScrollPosition(index,this);
	}
	
	/////////////////////////////////////////////////////////////////
	// HYPERLINKS			/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Pattern used to detect hyperlinks */
	private static final Pattern PATTERN_OPEN = Pattern.compile("<a[^>]+>");
	/** Pattern used to detect hyperlinks */
	private static final String TAG_CLOSE = "</a>";
	/** Text including hyperlinks */
	private String linkedText = null;
	/** Whether or not hyperlinks should be displayed */
	boolean showHyperlinks = false;
	
	/**
	 * Switches on/off the display
	 * of hyperlinks.
	 */
	public void switchHyperlinks()
	{	showHyperlinks = !showHyperlinks;
		updateHighlighting();
	}
	
	/////////////////////////////////////////////////////////////////
	// MENTIONS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Styles used to highlight mentions */
	private final Map<EntityType, Style> mentionStyles = new HashMap<EntityType, Style>();
	/** Whether or not mentions of some type should be highlighted */
	private final Map<EntityType, Boolean> mentionSwitches = new HashMap<EntityType, Boolean>();
	/** Style used to un-highlight mentions */
	private Style noStyle = null;
	/** List of mentions to display */
	private Mentions mentions = null;
	/** List of reference mentions */
	private Mentions references = null;

	/**
	 * Returns the list of estimated mentions 
	 * handled by this panel.
	 * 
	 * @return
	 * 		List of estimated mentions displayed by this panel.
	 */
	public Mentions getMentions()
	{	return mentions;
	}
	
	/**
	 * Returns the list of reference mentions 
	 * handled by this panel.
	 * 
	 * @return
	 * 		List of reference mentions displayed by this panel.
	 */
	public Mentions getReferences()
	{	return references;
	}
	
	/**
	 * Changes the flag indicating if the specified
	 * entity type should be displayed or not.
	 * 
	 * @param type
	 * 		Type of the concerned mentions.
	 */
	public void switchType(EntityType type)
	{	boolean flag = !mentionSwitches.get(type);
		mentionSwitches.put(type,flag);
		updateHighlighting();
	}
	
	/**
	 * Initializes the styles used
	 * to highlight mentions depending
	 * on their type.
	 */
	private void initStyles()
	{	StyledDocument doc = textPane.getStyledDocument();
		noStyle = doc.addStyle("nostyle", null);
		for(EntityType type: EntityType.values())
		{	// style
			Style style = doc.addStyle(type.toString(), null);
			StyleConstants.setBackground(style, MentionEditor.MENTION_COLOR.get(type));
			mentionStyles.put(type, style);
		}
		
		// mode
		commonStyle = doc.addStyle("common", null);
		StyleConstants.setBackground(commonStyle, Color.GREEN);
		missingStyle = doc.addStyle("missing", null);
		StyleConstants.setBackground(missingStyle, Color.RED);
		excessStyle = doc.addStyle("excess", null);
		StyleConstants.setBackground(excessStyle, Color.YELLOW);
		
		// hyperlinks
		linkStyle = doc.addStyle("link", null);
		StyleConstants.setForeground(linkStyle, Color.BLUE);
		StyleConstants.setUnderline(linkStyle, true);
		
		// excess
		searchStyle = doc.addStyle("search", null);
		StyleConstants.setBackground(searchStyle, Color.MAGENTA);
		StyleConstants.setFontSize(searchStyle, 20);
	}
	
	/**
	 * Updates mention highlighting depending
	 * on the state of the switches.
	 */
	public void updateHighlighting()
	{	// reset styles
		StyledDocument document = textPane.getStyledDocument();
		document.setCharacterAttributes(0,document.getLength(),noStyle,true);
		
		// types highlighting
		if(mode)
		{	List<AbstractMention<?,?>> mentionList = mentions.getMentions();
			for (AbstractMention<?,?> mention : mentionList)
			{	EntityType type = mention.getType();
				boolean flag = mentionSwitches.get(type);
//if(mention.getEndPos()>document.getLength())
//	System.out.println("erreur!!! length="+document.getLength()+" mais end="+mention.getEndPos());
				if(flag)
				{	Style style = mentionStyles.get(type);
					int startPos = mention.getStartPos();
					int endPos = mention.getEndPos();
					int length = endPos - startPos;
if(endPos>document.getLength())
	throw new IllegalArgumentException("Mention out of article: "+mention);
					document.setCharacterAttributes(startPos,length,style,true);
				}
			}
		}
		
		// comparison highlighting
		else
		{	commonParts = new ArrayList<int[]>();
			missingParts = new ArrayList<int[]>();
			excessParts = new ArrayList<int[]>();
//			if(!references.isEmpty())
			List<AbstractMention<?,?>> mentionList = mentions.getMentions();
			Collections.sort(mentionList);
			List<AbstractMention<?,?>> referenceList = references.getMentions();
			Collections.sort(referenceList);
			Iterator<AbstractMention<?,?>> itEst = mentionList.iterator();
			Iterator<AbstractMention<?,?>> itRef = referenceList.iterator();
			if(itRef.hasNext() && itEst.hasNext())
			{	AbstractMention<?,?> est = null;
				AbstractMention<?,?> ref = null;
				int indexEst = 0;
				int indexRef = 0;
				do
				{	if(est==null)
						est = itEst.next();
					if(ref==null)
						ref = itRef.next();
				
					EntityType estType = est.getType();
					if(!mentionSwitches.get(estType))
						est = null;
					else
					{	EntityType refType = ref.getType();
						if(!mentionSwitches.get(refType))
							ref = null;
						else
						{	int startEst = Math.max(est.getStartPos(),indexEst);
							int startRef = Math.max(ref.getStartPos(),indexRef);
							int endEst = est.getEndPos();
							int endRef = ref.getEndPos();
							if(startEst<startRef)
							{	if(endEst<startRef)
								{	excessParts.add(new int[]{startEst,endEst});
									est = null;
								}
								else
								{	excessParts.add(new int[]{startEst,startRef});
									if(endEst<endRef)
									{	commonParts.add(new int[]{startRef,endEst});
										est = null;
										indexRef = endEst;
									}
									else
									{	commonParts.add(new int[]{startRef,endRef});
										ref = null;
										indexEst = endRef;
									}
								}
							}
							else if(startEst==startRef)
							{	if(endEst<endRef)
								{	commonParts.add(new int[]{startRef,endEst});
									est = null;
									indexRef = endEst;
								}
								else if(endEst==endRef)
								{	commonParts.add(new int[]{startEst,endEst});
									est = null;
									ref = null;
								}
								else
								{	commonParts.add(new int[]{startRef,endRef});
									ref = null;
									indexEst = endRef;
								}
							}
							else //if(startEst>startRef)
							{	if(endRef<startEst)
								{	missingParts.add(new int[]{startRef,endRef});
									ref = null;
								}
								else
								{	missingParts.add(new int[]{startRef,startEst});
									if(endRef<endEst)
									{	commonParts.add(new int[]{startEst,endRef});
										ref = null;
										indexEst = endRef;
									}
									else
									{	commonParts.add(new int[]{startEst,endEst});
										est = null;
										indexRef = endEst;
									}
								}
							}
						}
					}
				}
				while((est!=null || itEst.hasNext()) 
					&& (ref!=null || itRef.hasNext()));
			}
			while(itEst.hasNext())
			{	AbstractMention<?,?> est = itEst.next();
				EntityType estType = est.getType();
				if(mentionSwitches.get(estType))
				{	int startEst = est.getStartPos();
					int endEst = est.getEndPos();
					excessParts.add(new int[]{startEst,endEst});
				}
			}
			while(itRef.hasNext())
			{	AbstractMention<?,?> ref = itRef.next();
				EntityType refType = ref.getType();
				if(mentionSwitches.get(refType))
				{	int startRef = ref.getStartPos();
					int endRef = ref.getEndPos();
					missingParts.add(new int[]{startRef,endRef});
				}
			}
			
			for (int[] coord: commonParts)
			{	int startPos = coord[0];
				int endPos = coord[1];
				int length = endPos - startPos;
				if(length>0)
					document.setCharacterAttributes(startPos,length,commonStyle,true);
			}
			for (int[] coord: missingParts)
			{	int startPos = coord[0];
				int endPos = coord[1];
				int length = endPos - startPos;
				if(length>0)
					document.setCharacterAttributes(startPos,length,missingStyle,true);
			}
			for (int[] coord: excessParts)
			{	int startPos = coord[0];
				int endPos = coord[1];
				int length = endPos - startPos;
				if(length>0)
					document.setCharacterAttributes(startPos,length,excessStyle,true);
			}
		}
		
		// hyperlinks are possibly displayed, on top of the other formatting
		if(showHyperlinks)
		{	int offset = 0;
			Matcher matcher = PATTERN_OPEN.matcher(linkedText);
			while(matcher.find())
			{	int startPos1 = matcher.start();
				int endPos1 = matcher.end();
				int length1 = endPos1 - startPos1;
				int startPos = startPos1 - offset;
				offset = offset + length1;
				//System.out.println("startPos1="+startPos1+" endPos1="+endPos1+" length1="+length1+" offset="+offset+"("+matcher.group()+")");				
							
				int startPos2 = linkedText.indexOf(TAG_CLOSE, endPos1);
				int length2 = TAG_CLOSE.length();
				//System.out.println("startPos2="+startPos2+" length2="+length2+"("+matcher.group()+")");				
				int endPos = startPos2 - offset;
				int length = endPos - startPos;
				//System.out.println("startPos="+startPos+" endPos="+endPos+" length="+length);				
				document.setCharacterAttributes(startPos,length,linkStyle,false);
				offset = offset + length2;
				//System.out.println("offset="+offset);
			}
		}
		
		// highlight additional stuff (punctual/debug use)
		// TODO would be good to integrate this in the GUI as a menuitem with text field, letting the user directly enter a regex
//		try
//		{	
////			Pattern pattern = Pattern.compile("([A-Za-z]|\\d)\\.[A-Za-z]");
//			Pattern pattern = Pattern.compile("(\\b(o|O)rder|(o|O)rdre|(l|L)(e|é)gion)\\b");
//			Matcher matcher = pattern.matcher(document.getText(0, document.getLength()));
//			while(matcher.find())
//			{	int startPos = matcher.start();
//				int endPos = matcher.end();
//				int length = endPos - startPos;
//				document.setCharacterAttributes(startPos,length,searchStyle,true);
//				//System.out.println("doc length: "+document.getLength()+" startPos:"+startPos+" endPos:"+endPos);				
//			}
//		}
//		catch (BadLocationException e)
//		{	e.printStackTrace();
//		}
	}

	/////////////////////////////////////////////////////////////////
	// MENTION EDITION	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Adds a new mention to the list
	 * represented in this panel.
	 * The mention is automatically generated
	 * using the current selection. If no
	 * text is currently selected but the
	 * cursor is inside an existing mention,
	 * then its type is changed for
	 * the specified one.
	 * 
	 * @param type 
	 * 		Type of the new mention.
	 * @return
	 * 		The created mention, or {@code null} if none was created.
	 * 		In case of type change, the mention is returned only if
	 * 		its type was changed.
	 */
	public AbstractMention<?,?> insertMention(EntityType type)
	{	AbstractMention<?,?> result = null;
		int start = textPane.getSelectionStart();
		int end = textPane.getSelectionEnd();
		int position = textPane.getCaretPosition();
		
		// no selection: try changing the current mention
		if(start==end)
			result = changeEntityType(type,position);
		
		// text selected: create a new mention
		else
			result = insertMention(type,start,end);
				
		return result;
	}
	
	/**
	 * Changes the type of the last inserted mention
	 * containing the specified position, so that
	 * its new type is the specified type. If there
	 * is no mention there, or if its type is already
	 * the specified one, then no change is performed.
	 * 
	 * @param type
	 * 		New type of the mention.
	 * @param position
	 * 		Position of the cursor.
	 * @return
	 * 		The mention whose type was changed, or {@code null} otherwise.
	 */
	private AbstractMention<?,?> changeEntityType(EntityType type, int position)
	{	AbstractMention<?,?> result = null;
		List<AbstractMention<?,?>> mentionList = mentions.getMentions();
		int index = 0;
		
		// retrieve the last mention at this position 
		ListIterator<AbstractMention<?,?>> it = mentionList.listIterator(mentionList.size());
		while(result==null && it.hasPrevious())
		{	// get the mention
			AbstractMention<?,?> mention = it.previous();
			int startPos = mention.getStartPos();
			int endPos = mention.getEndPos();
			// check its position
			if(startPos<=position && position<=endPos)
			{	// check its type: only affect visible mentions
				EntityType t = mention.getType();
				if(mentionSwitches.get(t) && t!=type)
				{	// update mention
					index = mentionList.indexOf(mention);
					it.remove();
					ProcessorName source = mention.getSource();
					String valueStr = mention.getStringValue();
					mention = AbstractMention.build(type, startPos, endPos, source, valueStr);
					result = mention;
					
					// update display
					updateHighlighting();
					
					// unselect text
					int pos = textPane.getCaretPosition();
					textPane.setSelectionEnd(pos);
					textPane.setSelectionStart(pos);
				}
			}
		}
		
		if(result!=null)
			mentionList.add(index, result);
		
		return result;
	}
	
	/**
	 * Insert a new mention, of the specified
	 * type, at the specified position.
	 * 
	 * @param type
	 * 		Type of the new mention.
	 * @param start
	 * 		Start position of the new mention.
	 * @param end
	 * 		End position of the new mention.
	 * @return
	 * 		The created mention.
	 */
	private AbstractMention<?,?> insertMention(EntityType type, int start, int end)
	{	AbstractMention<?,?> result = null;
		
		// update mentions
		String valueStr = textPane.getSelectedText();
		ProcessorName source = ProcessorName.REFERENCE;
		result = AbstractMention.build(type, start, end, source, valueStr);
		mentions.addMention(result);
		
		// update display
		updateHighlighting();
		
		// unselect text
		int pos = textPane.getCaretPosition();
		textPane.setSelectionEnd(pos);
		textPane.setSelectionStart(pos);
		
		return result;
	}
	
	/**
	 * Remove a mention from the list represented in this panel.
	 * The mention at the current carret position is removed, if there
	 * is any. All mentions in the selection are removed, if some text is selected.
	 * <br/>
	 * Mentions not currently displayed are ignored.
	 * 
	 * @return
	 * 		The removed mentions, under the form of a list.
	 */
	public List<AbstractMention<?,?>> removeMentions()
	{	List<AbstractMention<?,?>> result = null; 
		
		// get the current selection / cursor position
		int start = textPane.getSelectionStart();
		int end = textPane.getSelectionEnd();
		int position = textPane.getCaretPosition();
		if(start!=end)
			result = removeMentions(start,end);
		else
		{	result = new ArrayList<AbstractMention<?,?>>();
			AbstractMention<?,?> mention = removeMention(position);
			if(mention!=null)
				result.add(mention);
		}
		
		return result;
	}
	
	/**
	 * Removes the first mention found at the specified position.
	 * 
	 * @param position
	 * 		Position of the mention to remove.
	 * @return
	 * 		Removed mention (can be {@code null}).
	 */
	private AbstractMention<?,?> removeMention(int position)
	{	AbstractMention<?,?> result = null;
		List<AbstractMention<?,?>> mentionList = mentions.getMentions();
		
		// retrieve the last mention at this position 
		ListIterator<AbstractMention<?,?>> it = mentionList.listIterator(mentionList.size());
		while(result==null && it.hasPrevious())
		{	// get the mention
			AbstractMention<?,?> mention = it.previous();
			int startPos = mention.getStartPos();
			int endPos = mention.getEndPos();
			// check its position
			if(startPos<=position && position<=endPos)
			{	// check its type: only remove visible mentions
				EntityType type = mention.getType();
				if(mentionSwitches.get(type))
				{	// update mentions
					it.remove();
					result = mention;
					
					// update display
					updateHighlighting();
					
					// unselect text
					int pos = textPane.getCaretPosition();
					textPane.setSelectionEnd(pos);
					textPane.setSelectionStart(pos);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Remove all mentions intersecting the current selection.
	 * <br/>
	 * Mentions not currently displayed are ignored.
	 * 
	 * @param start 
	 * 		Start of the selection.
	 * @param end 
	 * 		End of the selection.
	 * @return
	 * 		The removed mentions, under the form of a list.
	 */
	private List<AbstractMention<?,?>> removeMentions(int start, int end)
	{	List<AbstractMention<?,?>> result = new ArrayList<AbstractMention<?,?>>();
		List<AbstractMention<?,?>> mentionList = mentions.getMentions();
		 
		Iterator<AbstractMention<?,?>> it = mentionList.iterator();
		while(it.hasNext())
		{	// get the mention
			AbstractMention<?,?> mention = it.next();
			int startPos = mention.getStartPos();
			int endPos = mention.getEndPos();
			// check its position
			if(startPos>=start && startPos<=end
				|| endPos>=start && endPos<=end)
			{	// check its type: only remove visible mentions
				EntityType type = mention.getType();
				if(mentionSwitches.get(type))
				{	// update mentions
					it.remove();
					result.add(mention);
					
					// update display
					updateHighlighting();
					
					// unselect text
					int pos = textPane.getCaretPosition();
					textPane.setSelectionEnd(pos);
					textPane.setSelectionStart(pos);
				}
			}
		}
		
		return result;
	}
	
//	/**
//	 * Add a reference mentions 
//	 * and updates the panel accordingly.
//	 * 
//	 * @param mention
//	 * 		Reference mention to add.
//	 */
//	public void insertReference(AbstractEntity<?> mention)
//	{	references.add(mention);
//		updateHighlighting();
//	}
//	
//	/**
//	 * Remove some reference mentions 
//	 * and updates the panel accordingly.
//	 * 
//	 * @param mentions
//	 * 		Reference mentions to be removed.
//	 */
//	public void removeReferences(List<AbstractEntity<?>> mentions)
//	{	references.removeAll(mentions);
//		updateHighlighting();
//	}
	
	/**
	 * Shift the mentions located after the current cursor position.
	 * The shift direction depends on the parameter sign: negative for
	 * left and positive for right.
	 * <br/>
	 * Mentions not currently displayed are ignored.
	 * 
	 * @param offset
	 * 		Magnitude and direction of the shifting.
	 * @return
	 * 		The shifted mentions, under the form of a list.
	 */
	public List<AbstractMention<?,?>> shiftMentions(int offset)
	{	// get the current cursor position
		int position = textPane.getCaretPosition();
		String rawText = null;
		try
		{	Document document = textPane.getDocument();
			rawText = document.getText(0, document.getLength());
		}
		catch (BadLocationException e)
		{	e.printStackTrace();
		}
		
		List<AbstractMention<?,?>> result = new ArrayList<AbstractMention<?,?>>();
		List<AbstractMention<?,?>> mentionList = mentions.getMentions();
		
		Iterator<AbstractMention<?,?>> it = mentionList.iterator();
		while(it.hasNext())
		{	// get the mention
			AbstractMention<?,?> mention = it.next();
			int startPos = mention.getStartPos();
			int endPos = mention.getEndPos();
			// check its type: only move visible mentions
			EntityType type = mention.getType();
			if(mentionSwitches.get(type))
			{	// upadte mention position
				boolean keep;
				if(offset<0)
					keep = mentions.leftShiftMentionPosition(mention, position, -offset, rawText);
				else
					keep = mentions.rightShiftMentionPosition(mention, position, offset, rawText);
				if(!keep)
				{	it.remove();
					result.add(mention);
				}
				else
				{	if(startPos!=mention.getStartPos() || endPos!=mention.getEndPos())
						result.add(mention);
				}
				// update display
				updateHighlighting();
			}
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// TEXT EDITION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Method called by the main window when
	 * some text is inserted in the reference panel.
	 * The text must be modified in each tool-related
	 * panel, and the mentions must be shifted accordingly
	 * (in terms of positions).
	 * 
	 * @param start
	 * 		Starting position of the inserted text.
	 * @param text
	 * 		Inserted text.
	 * @param linkedText
	 * 		Updated (hyper)linked text. 
	 */
	public void textInserted(int start, String text, String linkedText)
	{	this.linkedText = linkedText;
		try
		{	Document document = textPane.getDocument();
			document.insertString(start, text, null);
			
			String rawText = document.getText(0, document.getLength());
			int length = text.length();
			mentions.rightShiftMentionPositions(start,length,rawText);
//			references.rightShiftMentionPositions(start,length);
		}
		catch (BadLocationException e)
		{	e.printStackTrace();
		}
	}
	
	/**
	 * Method called by the main window when
	 * some text is removed in the reference panel.
	 * The text must be modified in each tool-related
	 * panel, and the mentions must be shifted accordingly
	 * (in terms of positions).
	 * 
	 * @param start
	 * 		Starting position of the removed text.
	 * @param length
	 * 		Length of the removed text.
	 * @param linkedText
	 * 		Updated (hyper)linked text. 
	 */
	public void textRemoved(int start, int length, String linkedText)
	{	this.linkedText = linkedText;
		try
		{	Document document = textPane.getDocument();
			document.remove(start, length);
			
			String rawText = document.getText(0, document.getLength());
			mentions.leftShiftMentionPositions(start,length,rawText);
//			references.leftShiftMentionPositions(start,length);
		}
		catch (BadLocationException e)
		{	e.printStackTrace();
		}
	}

	/**
	 * Checks if the specified positions are located
	 * right before or after an existing reference mention.
	 * If that is the case, the method returns {@code true}.
	 * 
	 * @param startPos
	 * 		Start position to be considered.
	 * @return
	 * 		{@code true} iff the positions are located right
	 * 		before or after an existing reference mention.
	 */
	protected boolean isMentionAdjacent(int startPos)
	{	boolean result = false;
		
		Iterator<AbstractMention<?,?>> it = references.getMentions().iterator();
		while(it.hasNext() && !result)
		{	AbstractMention<?,?> mention = it.next();
			int startPos0 = mention.getStartPos();
			int endPos0 = mention.getEndPos();
			result = startPos==startPos0 || startPos==endPos0;
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// DISPLAY MODE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Current display mode */
	private boolean mode = false;
	/** List of text parts common to both estimated and reference mentions */
	private List<int[]> commonParts = null;
	/** List of text parts present only in reference mentions */
	private List<int[]> missingParts = null;
	/** List of text parts present only in estimated mentions */
	private List<int[]> excessParts = null;
	/** Style used for common parts */
	private Style commonStyle = null;
	/** Style used for missing parts */
	private Style missingStyle = null;
	/** Style used for excess parts */
	private Style excessStyle = null;
	/** Style used for hyperlinks */
	private Style linkStyle = null;
	/** Style with various uses */
	private Style searchStyle = null;
	
	/**
	 * Switch between the 'type' and the 'comparison'
	 * display modes.
	 * 
	 * @param mode
	 * 		The new display mode.
	 */
	public void switchMode(boolean mode)
	{	this.mode = mode;
		updateHighlighting();
	}

	/////////////////////////////////////////////////////////////////
	// DOCUMENT LISTENER	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void changedUpdate(DocumentEvent e)
	{	// just for style >> we ignore that
	}

	@Override
	public void insertUpdate(DocumentEvent e)
	{	try
		{	int start = e.getOffset();
			int length = e.getLength();
			Document document = textPane.getDocument();
			String text = document.getText(start, length);
			
//			// if the text is right before or after a mention: do not highlight it
//			boolean mentionAdjacent = isMentionAdjacent(start);
			
			// no need to update estimated mentions, since they're the same than reference, in this specific case
			String rawText = document.getText(0, document.getLength());
			references.rightShiftMentionPositions(start,length,rawText);
			mainEditor.textInserted(start,text);
			
			// update linked text
			linkedText = mainEditor.getCurrentLinkedText();
			
//			if(mentionAdjacent)
//			{	SwingUtilities.invokeLater(new Runnable()
//				{	@Override
//					public void run()
//					{	updateHighlighting();
//					}
//				});	
//			}
		}
		catch (BadLocationException e1)
		{	e1.printStackTrace();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e)
	{	try
		{	int start = e.getOffset();
			int length = e.getLength();
			Document document = textPane.getDocument();
			
			// no need to update estimated mentions, since they're the same than reference, in this specific case
			String rawText = document.getText(0, document.getLength());
			references.leftShiftMentionPositions(start,length,rawText);
			mainEditor.textRemoved(start,length);
			
			// update linked text
			linkedText = mainEditor.getCurrentLinkedText();
		}
		catch (BadLocationException e1)
		{	e1.printStackTrace();
		}
	}

	/////////////////////////////////////////////////////////////////
	// CARET LISTENER		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void caretUpdate(CaretEvent arg0)
	{	// possibly change selection
//		String txt = textPane.getSelectedText();
//		if(txt!=null)
//		{	int end = textPane.getSelectionEnd();
//			boolean update = false;
//			while(!txt.isEmpty() && txt.endsWith(" "))
//			{	txt = txt.substring(0,txt.length()-1);
//				end--;
//				update = true;
//			}
//			if(update)
//			{	final int e = end;
//				SwingUtilities.invokeLater(new Runnable()
//				{	@Override
//					public void run()
//					{	textPane.setSelectionEnd(e);
//				    }
//				});
//			}
//		}
		
		// fetch position
		String text = textPane.getText();
		int length = text.length();
		int dot = arg0.getDot();
		mainEditor.updateStatusPosition(dot,length);
	}
}
