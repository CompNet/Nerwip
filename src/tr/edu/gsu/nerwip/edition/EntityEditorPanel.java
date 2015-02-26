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
import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
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
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TextAction;

import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerName;

/**
 * This class implements a panel designed to
 * display a text and highlights a list of entities.
 * Each entity is displayed in a specific color.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
public class EntityEditorPanel extends JPanel implements AdjustmentListener, DocumentListener, CaretListener
{	/** Version identifier for Serializable class */
	private static final long serialVersionUID = 8501538849713791317L;

	/**
	 * Builds a panel meant to display the specified text
	 * and highlight the specified entities. The tooltip
	 * is used to indicate the tool parameters (the tool
	 * name appears in the title of this tab pane).  
	 * 
	 * @param mainEditor
	 * 		{@link EntityEditor} window of the application. 
	 * @param text
	 * 		Full text of the article.
	 * @param linkedText
	 * 		Linked text of the article.
	 * @param entities
	 * 		List of entities to display.
	 * @param references 
	 * 		List of reference entities (used for the mode).
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
	 * 		Folder associated to the estimated entities (relative to the article folder).
	 */
	public EntityEditorPanel(EntityEditor mainEditor, String text, String linkedText, Entities entities, Entities references, String tooltip,
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
		
		// set scrollbar
		scrollBar = scrollPane.getVerticalScrollBar();
		scrollBar.addAdjustmentListener(this);
		
		// remove annoying key bidings
		KeyStroke remove = KeyStroke.getKeyStroke("control shift O");
		InputMap im = textPane.getInputMap(JComponent.WHEN_FOCUSED);
		im.put(remove, "none");

		// prevent text edition
		textPane.setEditable(editable);
		if(editable)
			textPane.getDocument().addDocumentListener(this);
		else
			textPane.getCaret().setVisible(true);
		
		// change selection behavior
		TextAction action = new NextWordAction("selection-next-word", true);
		textPane.getActionMap().put("selection-next-word",action);
		
		// listen to position changes
		textPane.addCaretListener(this);
		
		// entities
		this.entities = entities;
		this.references = references;
		entitySwitches.putAll(typeDispl);
		
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
	private EntityEditor mainEditor;
	
	/////////////////////////////////////////////////////////////////
	// FOLDER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Folder associated to the estimated entities */
	private String folder = null;
	
	/**
	 * Returns the folder associated
	 * to the estimated entities.
	 * 
	 * @return
	 * 		Folder containing the estimated entities.
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
	// ENTITIES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Styles used to highlight entities */
	private final Map<EntityType, Style> entityStyles = new HashMap<EntityType, Style>();
	/** Whether or not entities of some type should be highlighted */
	private final Map<EntityType, Boolean> entitySwitches = new HashMap<EntityType, Boolean>();
	/** Style used to un-highlight entities */
	private Style noStyle = null;
	/** List of entities to display */
	private Entities entities = null;
	/** List of reference entities */
	private Entities references = null;

	/**
	 * Returns the list of estimated entities 
	 * handled by this panel.
	 * 
	 * @return
	 * 		List of estimated entities displayed by this panel.
	 */
	public Entities getEntities()
	{	return entities;
	}
	
	/**
	 * Returns the list of reference entities 
	 * handled by this panel.
	 * 
	 * @return
	 * 		List of reference entities displayed by this panel.
	 */
	public Entities getReferences()
	{	return references;
	}
	
	/**
	 * Changes the flag indicating if the specified
	 * entity type should be displayed or not.
	 * 
	 * @param type
	 * 		Type of the concerned entities.
	 */
	public void switchType(EntityType type)
	{	boolean flag = !entitySwitches.get(type);
		entitySwitches.put(type,flag);
		updateHighlighting();
	}
	
	/**
	 * Initializes the styles used
	 * to highlight entities depending
	 * on their type.
	 */
	private void initStyles()
	{	StyledDocument doc = textPane.getStyledDocument();
		noStyle = doc.addStyle("nostyle", null);
		for(EntityType type: EntityType.values())
		{	// style
			Style style = doc.addStyle(type.toString(), null);
			StyleConstants.setBackground(style, type.getColor());
			entityStyles.put(type, style);
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
	 * Updates entity highlighting depending
	 * on the state of the switches.
	 */
	public void updateHighlighting()
	{	// reset styles
		StyledDocument document = textPane.getStyledDocument();
		document.setCharacterAttributes(0,document.getLength(),noStyle,true);
		
		// types highlighting
		if(mode)
		{	List<AbstractEntity<?>> entityList = entities.getEntities();
			for (AbstractEntity<?> entity : entityList)
			{	EntityType type = entity.getType();
				boolean flag = entitySwitches.get(type);
//if(entity.getEndPos()>document.getLength())
//	System.out.println("erreur!!! length="+document.getLength()+" mais end="+entity.getEndPos());
				if(flag)
				{	Style style = entityStyles.get(type);
					int startPos = entity.getStartPos();
					int endPos = entity.getEndPos();
					int length = endPos - startPos;
if(endPos>document.getLength())
	throw new IllegalArgumentException("Entity out of article: "+entity);
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
			List<AbstractEntity<?>> entityList = entities.getEntities();
			Collections.sort(entityList);
			List<AbstractEntity<?>> referenceList = references.getEntities();
			Collections.sort(referenceList);
			Iterator<AbstractEntity<?>> itEst = entityList.iterator();
			Iterator<AbstractEntity<?>> itRef = referenceList.iterator();
			if(itRef.hasNext() && itEst.hasNext())
			{	AbstractEntity<?> est = null;
				AbstractEntity<?> ref = null;
				int indexEst = 0;
				int indexRef = 0;
				do
				{	if(est==null)
						est = itEst.next();
					if(ref==null)
						ref = itRef.next();
				
					EntityType estType = est.getType();
					if(!entitySwitches.get(estType))
						est = null;
					else
					{	EntityType refType = ref.getType();
						if(!entitySwitches.get(refType))
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
			{	AbstractEntity<?> est = itEst.next();
				EntityType estType = est.getType();
				if(entitySwitches.get(estType))
				{	int startEst = est.getStartPos();
					int endEst = est.getEndPos();
					excessParts.add(new int[]{startEst,endEst});
				}
			}
			while(itRef.hasNext())
			{	AbstractEntity<?> ref = itRef.next();
				EntityType refType = ref.getType();
				if(entitySwitches.get(refType))
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
	// ENTITY EDITION	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Adds a new entity to the list
	 * represented in this panel.
	 * The entity is automatically generated
	 * using the current selection. If no
	 * text is currently selected but the
	 * cursor is inside an existing entity,
	 * then its type is changed for
	 * the specified one.
	 * 
	 * @param type 
	 * 		Type of the new entity.
	 * @return
	 * 		The created entity, or {@code null} if none was created.
	 * 		In case of type change, the entity is returned only if
	 * 		its type was changed.
	 */
	public AbstractEntity<?> insertEntity(EntityType type)
	{	AbstractEntity<?> result = null;
		int start = textPane.getSelectionStart();
		int end = textPane.getSelectionEnd();
		int position = textPane.getCaretPosition();
		
		// no selection: try changing the current entity
		if(start==end)
			result = changeEntityType(type,position);
		
		// text selected: create a new entity
		else
			result = insertEntity(type,start,end);
				
		return result;
	}
	
	/**
	 * Changes the type of the last inserted entity
	 * containing the specified position, so that
	 * its new type is the specified type. If there
	 * is no entity there, or if its type is already
	 * the specified one, then no change is performed.
	 * 
	 * @param type
	 * 		New type of the entity.
	 * @param position
	 * 		Position of the cursor.
	 * @return
	 * 		The entity whose type was changed, or {@code null} otherwise.
	 */
	private AbstractEntity<?> changeEntityType(EntityType type, int position)
	{	AbstractEntity<?> result = null;
		List<AbstractEntity<?>> entityList = entities.getEntities();
		int index = 0;
		
		// retrieve the last entity at this position 
		ListIterator<AbstractEntity<?>> it = entityList.listIterator(entityList.size());
		while(result==null && it.hasPrevious())
		{	// get the entity
			AbstractEntity<?> entity = it.previous();
			int startPos = entity.getStartPos();
			int endPos = entity.getEndPos();
			// check its position
			if(startPos<=position && position<=endPos)
			{	// check its type: only affect visible entities
				EntityType t = entity.getType();
				if(entitySwitches.get(t) && t!=type)
				{	// update entity
					index = entityList.indexOf(entity);
					it.remove();
					RecognizerName source = entity.getSource();
					String valueStr = entity.getStringValue();
					entity = AbstractEntity.build(type, startPos, endPos, source, valueStr);
					result = entity;
					
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
			entityList.add(index, result);
		
		return result;
	}
	
	/**
	 * Insert a new entity, of the specified
	 * type, at the specified position.
	 * 
	 * @param type
	 * 		Type of the new entity.
	 * @param start
	 * 		Start position of the new entity.
	 * @param end
	 * 		End position of the new entity.
	 * @return
	 * 		The created entity.
	 */
	private AbstractEntity<?> insertEntity(EntityType type, int start, int end)
	{	AbstractEntity<?> result = null;
		
		// update entities
		String valueStr = textPane.getSelectedText();
		RecognizerName source = RecognizerName.REFERENCE;
		result = AbstractEntity.build(type, start, end, source, valueStr);
		entities.addEntity(result);
		
		// update display
		updateHighlighting();
		
		// unselect text
		int pos = textPane.getCaretPosition();
		textPane.setSelectionEnd(pos);
		textPane.setSelectionStart(pos);
		
		return result;
	}
	
	/**
	 * Remove an entity from the list
	 * represented in this panel.
	 * The entity at the current carret
	 * position is removed, if there
	 * is any. All entities in the selection
	 * are removed, if some text is selected.
	 * <br/>
	 * Entities not currently displayed
	 * are ignored.
	 * 
	 * @return
	 * 		The removed entities, under the form of a list.
	 */
	public List<AbstractEntity<?>> removeEntities()
	{	List<AbstractEntity<?>> result = null; 
		
		// get the current selection / cursor position
		int start = textPane.getSelectionStart();
		int end = textPane.getSelectionEnd();
		int position = textPane.getCaretPosition();
		if(start!=end)
			result = removeEntities(start,end);
		else
		{	result = new ArrayList<AbstractEntity<?>>();
			AbstractEntity<?> entity = removeEntity(position);
			if(entity!=null)
				result.add(entity);
		}
		
		return result;
	}
	
	/**
	 * Removes the first entity found
	 * at the specified position.
	 * 
	 * @param position
	 * 		Position of the entity to remove.
	 * @return
	 * 		Entity removed (can be {@code null}).
	 */
	private AbstractEntity<?> removeEntity(int position)
	{	AbstractEntity<?> result = null;
		List<AbstractEntity<?>> entityList = entities.getEntities();
		
		// retrieve the last entity at this position 
		ListIterator<AbstractEntity<?>> it = entityList.listIterator(entityList.size());
		while(result==null && it.hasPrevious())
		{	// get the entity
			AbstractEntity<?> entity = it.previous();
			int startPos = entity.getStartPos();
			int endPos = entity.getEndPos();
			// check its position
			if(startPos<=position && position<=endPos)
			{	// check its type: only remove visible entities
				EntityType type = entity.getType();
				if(entitySwitches.get(type))
				{	// update entities
					it.remove();
					result = entity;
					
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
	 * Remove all entities intersecting
	 * the current selection.
	 * <br/>
	 * Entities not currently displayed
	 * are ignored.
	 * 
	 * @param start 
	 * 		Start of the selection.
	 * @param end 
	 * 		End of the selection.
	 * @return
	 * 		The removed entities, under the form of a list.
	 */
	private List<AbstractEntity<?>> removeEntities(int start, int end)
	{	List<AbstractEntity<?>> result = new ArrayList<AbstractEntity<?>>();
		List<AbstractEntity<?>> entityList = entities.getEntities();
		 
		Iterator<AbstractEntity<?>> it = entityList.iterator();
		while(it.hasNext())
		{	// get the entity
			AbstractEntity<?> entity = it.next();
			int startPos = entity.getStartPos();
			int endPos = entity.getEndPos();
			// check its position
			if(startPos>=start && startPos<=end
				|| endPos>=start && endPos<=end)
			{	// check its type: only remove visible entities
				EntityType type = entity.getType();
				if(entitySwitches.get(type))
				{	// update entities
					it.remove();
					result.add(entity);
					
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
//	 * Add a reference entities 
//	 * and updates the panel accordingly.
//	 * 
//	 * @param entity
//	 * 		Reference entity to add.
//	 */
//	public void insertReference(AbstractEntity<?> entity)
//	{	references.add(entity);
//		updateHighlighting();
//	}
//	
//	/**
//	 * Remove some reference entities 
//	 * and updates the panel accordingly.
//	 * 
//	 * @param entities
//	 * 		Reference entities to be removed.
//	 */
//	public void removeReferences(List<AbstractEntity<?>> entities)
//	{	references.removeAll(entities);
//		updateHighlighting();
//	}
	
	/////////////////////////////////////////////////////////////////
	// TEXT EDITION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Method called by the main window when
	 * some text is inserted in the reference panel.
	 * The text must be modified in each tool-related
	 * panel, and the entities must be shifted accordingly
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
			entities.rightShiftEntityPositions(start,length,rawText);
//			references.rightShiftEntityPositions(start,length);
		}
		catch (BadLocationException e)
		{	e.printStackTrace();
		}
	
	}
	
	/**
	 * Method called by the main window when
	 * some text is removed in the reference panel.
	 * The text must be modified in each tool-related
	 * panel, and the entities must be shifted accordingly
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
			entities.leftShiftEntityPositions(start,length,rawText);
//			references.leftShiftEntityPositions(start,length);
		}
		catch (BadLocationException e)
		{	e.printStackTrace();
		}
	}

	/**
	 * Checks if the specified positions are located
	 * right before or after an existing reference entity.
	 * If that is the case, the method returns {@code true}.
	 * 
	 * @param startPos
	 * 		Start position to be considered.
	 * @return
	 * 		{@code true} iff the positions are located right
	 * 		before or after an existing reference entity.
	 */
	protected boolean isEntityAdjacent(int startPos)
	{	boolean result = false;
		
		Iterator<AbstractEntity<?>> it = references.getEntities().iterator();
		while(it.hasNext() && !result)
		{	AbstractEntity<?> entity = it.next();
			int startPos0 = entity.getStartPos();
			int endPos0 = entity.getEndPos();
			result = startPos==startPos0 || startPos==endPos0;
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// DISPLAY MODE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Current display mode */
	private boolean mode = false;
	/** List of text parts common to both estimated and reference entities */
	private List<int[]> commonParts = null;
	/** List of text parts present only in reference entities */
	private List<int[]> missingParts = null;
	/** List of text parts present only in estimated entities */
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
			
//			// if the text is right before or after an entity: do not highlight it
//			boolean entityAdjacent = isEntityAdjacent(start);
			
			// no need to update estimated entities, since they're the same than reference, in this specific case
			String rawText = document.getText(0, document.getLength());
			references.rightShiftEntityPositions(start,length,rawText);
			mainEditor.textInserted(start,text);
			
			// update linked text
			linkedText = mainEditor.getCurrentLinkedText();
			
//			if(entityAdjacent)
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
			
			// no need to update estimated entities, since they're the same than reference, in this specific case
			String rawText = document.getText(0, document.getLength());
			references.leftShiftEntityPositions(start,length,rawText);
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
		int dot = arg0.getDot();
		mainEditor.updateStatusPosition(dot);
	}
}
