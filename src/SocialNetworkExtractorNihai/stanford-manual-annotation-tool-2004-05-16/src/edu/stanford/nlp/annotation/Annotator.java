package edu.stanford.nlp.annotation;

import edu.stanford.nlp.swing.MenuSavvyUndoManager;
import edu.stanford.nlp.swing.SmartCaret;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
<pre>
A simple Java editor that supports the easy addition of xml-style tags to
mark off portions of text.  It displays the tagging via color-coded regions.
This tool is designed for tagging training data for input to information
extraction models.

Usage: <code>java edu.stanford.nlp.annotation.Annotator [tagsFileName]</code>

The optional tags file will contain a list of tag names on separate lines,
and can be created from within Annotator.

@author Miler Lee, Stanford University, miler@cs.stanford.edu
@author Huy Nguyen (htnguyen@cs.stanford.edu)
@author Joseph Smarr (jsmarr@stanford.edu)

Changes in 1.4 - 5/16/03
-------------
Added batch processing to annotate all files in a folder
Added undo/redo to main window
Added smart text selection while dragging (ala MS Word)
Improved save/save as functionality
File name displayed in the title
Added ability to remove tags surrounding caret/selection
Added keyboard shortcuts for tagging (control+[1-9])
Added ability to automatically tag all other occurences of text inside tags

Changes in 1.3.1 - 11/2/02
-------------
Made program more suitable for external calling, by moving the 
WindowListener setup into the constructor.
Cleaned the external API.

Changes in 1.3 - 8/29/01
-------------
Changed the name of the application from Tagger to Annotator.

New feature that automatically grays out non-essential html, and gives the
option to un-gray it.

More intelligent "Save changes first" scheme.

Made the tag toolbar immobile.

Opening a file starts the editor at the top of the file, rather than at the
bottom as before.

Known bug: color changing dialog doesn't distinguish between ok/cancel
Known limitations: no undo, mildly unintuitive save tag file scheme;
		   add option of interpreting a document using a normal
		   html tag scheme, as well as the xml one



Changes in 1.2 - 8/3/01
-------------
Added warnings for questionable tagging activity (ie, tagging that splits
up an existing html tag).  Changed the tag nomenclature:

<tag name="xxxx" value="start"/>

and

<tag name="xxxx" value="end"/>



Changes in 1.11 - 8/1/01
-------------
Dealt with nested tags -- outer-most tag defines the color that the text
is, regardless of the other start/end tags nested in the block.


Changes in 1.1 - 7/25/01
-------------
Added the option to change the color of tags
</pre>
 */
public class Annotator extends JFrame {

    private static String ABOUT = "Annotator 1.3.1 - 2 November 2002";
    private static int WIDTH = 500;
    private static int HEIGHT = 400;
    private static int NUM_TAGS = 12;
    
    private final JFileChooser fc = new JFileChooser();    
    private final JColorChooser colorChooser = new JColorChooser();

    private boolean SuppressWarnings = false;
    private JMenuItem warningsMenuItem;
    private DefaultStyledDocument doc = new DefaultStyledDocument();
    private final JTextPane tp;
    private JToolBar toolbar = new JToolBar();
    private JMenu tagsMenu;
    private JMenu viewMenu;
    private JScrollPane scrollPane;
    private Hashtable colorMaps;
    private Vector tags = new Vector();
    private Vector buttons = new Vector();
    private Vector defaultColors = null;
    private boolean tagsChanged = false;
    private int defaultColorIndex = 0;
    private boolean hideHtml = true;
    private SimpleAttributeSet invisible = new SimpleAttributeSet();
    private String cache="";
    private ActionListener actor = new ActionPerformer();
    
    private MenuSavvyUndoManager undoManager;
    private JMenuItem undo,redo;
    
    private File curFile;
        
    // List of TaggedStrings recording the strings that have been tagged and
    // what tags they have been given    
    private ArrayList taggedStrings=new ArrayList();
    private TaggedStringComparator tsCmp=new TaggedStringComparator();
    
    // For batch processing
    private boolean batchMode; // whether we are in batch processing mode or not
    private String sourceDir; // directory containing files to be annotated
    private String targetDir; // directory to which to save annotated files
    private File[] files; // list of files to be annotated
    private int fileIndex;
    
    private BatchProcessDialog bpDlg;
    private JMenuItem prevItem;
    private JMenuItem nextItem;
    private JMenuItem savePrevItem;
    private JMenuItem saveNextItem;
    private JToolBar batchBar;
    private JButton prevButton;
    private JButton nextButton;
    private JButton savePrevButton;
    private JButton saveNextButton;
    private JTextField fileNumField;
    private JLabel numFilesLabel;
            
    /** Create an Annotator JFrame.  This can be called programmatically.
     *  @param tagFile A filename that contains a list of tags.  This can be 
     *       <code>null</code>, and then the annotator starts with no 
     *       predefined tags
     *  @param listener A WindowListener for JFrame events (closing, etc.)
     *       This may be <code>null</code>, and then the default 
     *       WindowListener is used
     */
    public Annotator(String tagFile, WindowListener listener) {
	super("Annotator");	        
        
	StyleConstants.setForeground(invisible, new Color(230, 230, 230));

	tp = new JTextPane(doc);
        tp.setCaret(new SmartCaret());
	scrollPane = new JScrollPane(tp);
	scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
	
	getContentPane().add(scrollPane, BorderLayout.CENTER);       
        
	toolbar = InitButtons(tagFile);
	getContentPane().add(toolbar, BorderLayout.EAST);
        
	JMenuBar menubar = AddMenuBar();
	//getContentPane().add(menubar, BorderLayout.NORTH);

        setJMenuBar(menubar);

        // adds undo functionality
        undoManager=new MenuSavvyUndoManager(undo,redo);
        tp.getDocument().addUndoableEditListener(undoManager);

        // sets up keyboard shortcuts for tagging
        // control+[1..9] is like clicking the [1..9]'th tag button
        tp.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) 
            { 
                int code=ke.getKeyCode();
                if(code>=KeyEvent.VK_1 && code<=KeyEvent.VK_9 && ke.isControlDown())
                {
                    int tagIndex=code-KeyEvent.VK_1; // ctrl-1 is first tag, etc.
                    if(tagIndex<tags.size())
                        TagText((String)tags.elementAt(tagIndex));
                }
            }
        });
                
	if (listener != null) {
	    addWindowListener(listener);
	}
        setLocation(20 + (int) (Math.random() * 40),
                    20 + (int) (Math.random() * 40));                
        
        // create batch processing dialog
        bpDlg=new BatchProcessDialog(this,true);
        bpDlg.setFileChooser(fc);                
        batchBar=makeBatchToolbar();
        enableBatchProcess(false);
        
        getContentPane().add(batchBar, BorderLayout.NORTH);
    }

    private class ActionPerformer implements ActionListener {

	public void actionPerformed(ActionEvent e) {
	    String com = e.getActionCommand();

	    if (com.equals("Open"))
		OpenFile();

	    else if (com.equals("Save"))
		SaveFile(true);

            else if (com.equals("Save As"))
                SaveFile(false);
            
            else if (com.equals("Batch Process"))
                initBatchProcess();
            
            else if (com.equals("Next File"))
                openNextFile(false);
            
            else if (com.equals("Previous File"))
                openPrevFile(false);
            
            else if (com.equals("Save and Next"))
                openNextFile(true);
            
            else if (com.equals("Save and Previous"))
                openPrevFile(true);
            
            
	    else if (com.equals("Exit"))
		Exit();
        
        else if(com.startsWith("Undo")) {
        try { undoManager.undo(); }
        catch(CannotUndoException cue) { Toolkit.getDefaultToolkit().beep(); }
        }
        
        else if(com.startsWith("Redo")) {
        try { undoManager.redo(); }
        catch(CannotRedoException cre) { Toolkit.getDefaultToolkit().beep(); }
        }

	    else if (com.equals("Cut"))
		tp.cut();

	    else if (com.equals("Copy"))
		tp.copy();

	    else if (com.equals("Paste"))
		tp.paste();

	    else if (com.equals("Select all"))
		tp.selectAll();

            else if (com.equals("Tag All Occurrences"))
                TagAllOccurrences();          
            
	    else if (com.equals("Show all HTML")) {
		hideHtml = false;
		warningsMenuItem = new JMenuItem("Hide all HTML");
		warningsMenuItem.addActionListener(this);
		viewMenu.remove(0);
		viewMenu.add(warningsMenuItem);	    
		OutputDocument(tp.getText());
	    }

	    else if (com.equals("Hide all HTML")) {
		hideHtml = true;
		warningsMenuItem = new JMenuItem("Show all HTML");
		warningsMenuItem.addActionListener(this);
		viewMenu.remove(0);
		viewMenu.add(warningsMenuItem);	    
		OutputDocument(tp.getText());
	    }

	    else if (com.equals("Load tag file"))
		LoadTags();
	
	    else if (com.equals("Save tag file"))
		SaveTags(true);

	    else if (com.equals("Save tag file without colors"))
		SaveTags(false);

	    else if (com.equals("Add tag"))
		AddTag();

	    else if (com.equals("Change tag color"))
		ChangeTagColor();
        
        else if (com.equals("Untag selected region"))
        removeTag();

	    else if (com.equals("Update display"))
		OutputDocument(tp.getText());

	    else if (com.equals("Suppress warnings")) {
		SuppressWarnings = true;
		warningsMenuItem = new JMenuItem("Enable warnings");
		warningsMenuItem.addActionListener(this);
		tagsMenu.remove(6);
		tagsMenu.add(warningsMenuItem);
	    }

	    else if (com.equals("Enable warnings")) {
		SuppressWarnings = false;
		warningsMenuItem = new JMenuItem("Suppress warnings");
		warningsMenuItem.addActionListener(this);
		tagsMenu.remove(6);
		tagsMenu.add(warningsMenuItem);
	    }

	    else if (com.equals("About"))
		showAbout();

	    else
		TagText(com);
	}

    }


/****************
  FILE ACTIONS
 ****************/

    /** 
     * Opens the file chooser to allow the user to select a file to open.  
     * Then calls OpenFile(File) to load the file.
     */
    private void OpenFile() {   
        int val = fc.showOpenDialog(this);
        if (val == JFileChooser.APPROVE_OPTION) 
        OpenFile(fc.getSelectedFile());
        // if opening a single file, disable batch processing if active
        if(batchMode) enableBatchProcess(false);
    }

    /** 
     * Loads the given file into the annotator, first asking to save the current
     * file if dirty.
     */
    private void OpenFile(File file) { OpenFile(file,false); }
    
    /** 
     * Loads the given file into the annotator, first asking to save the current
     * file if dirty.  <tt>annotated</tt> is used by batch mode to mark in the
     * title which documents have already been annotated.
     */
    private void OpenFile(File file, boolean annotated) {
        if(file==null) return;
        
        if (ConfirmNoSave()) {
            String line;
            String contents = "";
            
            try {            
                ByteArrayOutputStream buffer = new ByteArrayOutputStream((int)file.length());
                BufferedReader input = new BufferedReader(new FileReader(file));	    

                HtmlCleaner cleaner = new HtmlCleaner(input, buffer);
                cleaner.setAcceptableTags(HtmlCleaner.getDefaultAcceptableTags());
                cleaner.setIgnoreAttributes(true);
		cleaner.clean();
                
                //while ((line = input.readLine()) != null) {
                //    contents += line + "\n";
                //}
                //input.close();
                contents = buffer.toString();
                curFile=file;
            } catch (FileNotFoundException e) {
                ErrorMessage("Could not find file "+file.getPath());
            } catch (IOException ie) {
                ErrorMessage("There was a problem reading in the file.");
            } catch (com.quiotix.html.parser.ParseException pe) {
                ErrorMessage("Could not parse file "+file.getName());
            }  catch(com.quiotix.html.parser.TokenMgrError e) {
		ErrorMessage("Could not open file "+file.getPath()+".  Probably error parsing HTML.");
	    }
        
            OutputDocument(contents);

            //for save changes...
            cache = new String(tp.getText());

            tp.setCaretPosition(0);
            setTitle("Annotator - "+(annotated ? "(Annotated) ": "")+file.getName());
            undoManager.discardAllEdits();
            taggedStrings.clear();
        }
    }

    /** 
     * Writes the document to a file.  If <tt>overwrite</tt> is true and 
     * batchMode is false overwrites the existing file.  If batchMode is true
     * writes to a file with the same name as the current file in the target 
     * directory.  Otherwise, opens up a file chooser to select the target file.
     */
    private boolean SaveFile(boolean overwrite) {
        File file;
        
        if(batchMode==true) {
            String targetFile=targetDir+File.separator+curFile.getName();
            file=new File(targetFile);
        } else if(overwrite==false || curFile==null) { 
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) 
                file = fc.getSelectedFile();
            else return false;
        } else file = curFile;

        if(file != null) {
            SaveFile(file);           
            return true;
        }
        return false;
    }

    /** Writes the document to the given file. */
    private void SaveFile(File file) {
        if(file==null) return; 
        try {
            DataOutputStream out =
                new DataOutputStream(new FileOutputStream(file));
            out.writeBytes(tp.getText());
            out.close();
        } catch (IOException e) {
            ErrorMessage("There was a problem saving the file");
        }
        cache = new String(tp.getText());   //for confirm save
        curFile = file;                
    }

  private void Exit() {
    if (ConfirmNoSave()) {
      if (!ConfirmTagSave()) return;
      System.exit(0);
    }
  }


  private boolean ConfirmNoSave() {
    //    if(doc.getLength() != 0) {
    if (!cache.equals(tp.getText())) {  //added
      int n = JOptionPane.showConfirmDialog(this,
	       "Save changes first?", "Confirm",
	       JOptionPane.YES_NO_CANCEL_OPTION);
      if (n == JOptionPane.YES_OPTION) {
	if (!SaveFile(true)) return true;
      }
      else if (n == JOptionPane.CANCEL_OPTION) {
	return false;
      }
    }
    return true;
  }



/****************
  TAG ACTIONS
 ****************/
  
  private void TagText(String com) {
    SimpleAttributeSet attr = (SimpleAttributeSet) colorMaps.get(com);
    if (attr != null) {
      String st = tp.getSelectedText();
      
      // remembers the tag given to the selected string
      taggedStrings.add(new TaggedString(st,com));
            
      if (st!=null && st.length()>0 && CheckSelection(st)) {
        try {          

            tp.replaceSelection("");
            int selectionStart=tp.getSelectionStart();
            String startTag="<tag name=\""+com+"\" value=\"start\"/>";
            String endTag="<tag name=\""+com+"\" value=\"end\"/>";
            doc.insertString(selectionStart,startTag+st+endTag,attr);
            tp.setSelectionStart(selectionStart+startTag.length());
            tp.setSelectionEnd(selectionStart+startTag.length()+st.length());
            tp.requestFocus(); // go back to text once you click a button

        } catch (BadLocationException ex) {}
      }
    }    
  }
  
  // iterates over all the strings in taggedStrings, matching
  private void TagAllOccurrences() {
    Collections.sort(taggedStrings,tsCmp);
       
    for(Iterator iter=taggedStrings.iterator(); iter.hasNext(); ) {
        TaggedString ts=(TaggedString)iter.next();
        
        int index=doc.getLength();
        try {
            while((index=doc.getText(0,doc.getLength()).lastIndexOf(ts.string,index))!=-1) {
                // check to see if it's been tagged
                // inside a tag if searching backwards, we see a start tag 
                // before an end tag
                String text=doc.getText(0,doc.getLength());
                if(text.lastIndexOf("\"start\"",index)<=text.lastIndexOf("\"end\"",index)) {                   
                    SimpleAttributeSet attr=(SimpleAttributeSet)colorMaps.get(ts.tag);
                    if(attr!=null) {                    
                        doc.remove(index,ts.string.length());
                        doc.insertString(index, 
                                 "<tag name=\""+ts.tag+"\" value=\"start\"/>"+ts.string+
                                 "<tag name=\""+ts.tag+"\" value=\"end\"/>", attr);                    
                    }

                }                          
                index--;
            }            
        } catch(BadLocationException e) {
            e.printStackTrace(); // should never happen
        }
    }    
  }   
  
  /** Class to package a string and the tag it was given. */
  private class TaggedString {
    public String string;
    public String tag;    
    
    public TaggedString(String string, String tag) {
        this.string=string;
        this.tag=tag;
    }
  }
  
  /** 
   * Class for comparing two tagged strings based on the string length of the
   * <tt>string</tt> element.
   */
  private class TaggedStringComparator implements Comparator {
    /** 
     * Returns o2.string.length()-o1.string.length().  For sorting the string
     * from longest length to shortest length
     */
    public int compare(Object o1, Object o2) {        
        return(((TaggedString)o2).string.length()-((TaggedString)o1).string.length());
    }
    
    public boolean equals(Object o) {
        return(this==o);
    }
  }
  
  private boolean CheckSelection(String str) {
    if (SuppressWarnings) return true;

    boolean warn = false;
    
    if (str == null) return true;
    int l_index = 32767, r_index = -1;
    
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == '<') {
	l_index = i;
      }
      if (str.charAt(i) == '>') {
	r_index = i;
	if (r_index < l_index) {
	  warn = true;
	  break;
	}
      }
    }
    if (l_index > r_index && l_index < 32767) warn = true;

    //Now check if the new tag is smack in the middle of an old tag
    if (!warn) {
      int n = tp.getSelectionStart();
      int prev = n - 40;
      if (prev < 0) prev = 0;

      try {
	String before = doc.getText(prev, n-prev);
	for (int i = before.length()-1; i >= 0; i--) {
	  if (before.charAt(i) == '<') warn = true;
	  else if (before.charAt(i) == '>') break;
	}
      } catch (BadLocationException b) {}
    }


    if (warn) {
      int m = JOptionPane.showConfirmDialog(this,
               "Another tag may be split by this TAG operation.  Proceed anyway?",
               "Warning", JOptionPane.YES_NO_OPTION);
      if (m == JOptionPane.NO_OPTION) return false;
    }
    return true;




    //    else return true;
    
    //    return true;
    /*
    //Check that a tag is not being broken up
    int start = tp.getSelectionStart() - 1;
    int prev = start - 40;
    if (prev < 0) prev = 0;

    try {
      String str = doc.getText(prev, (start-prev)+1);
      int n = str.indexOf('<');
      if (n != -1) {
	String str2 = str.substring(n);
	System.out.println(str2);
	boolean matches = true;
	for (int i = 0; i < str2.length(); i++) {
	  if (str2.charAt(i) == '<') matches = false;
	  else if (str2.charAt(i) == '>') matches = true;
	}
	if (!matches) {
	  int m = JOptionPane.showConfirmDialog(this,
	         "Another tag may be split by this TAG operation.  Proceed anyway?",
                 "Warning", JOptionPane.YES_NO_OPTION);
	  if (m == JOptionPane.NO_OPTION) return false;
	}
      }
    } catch (BadLocationException be) {}

    return true;
    */
  }

  /** Removes tags surrounding the cursor. Fails silently. */
  private void removeTag()
  {
      int pos=tp.getCaretPosition();
      int selectionStart=tp.getSelectionStart();
      int selectionEnd=tp.getSelectionEnd();
      String text=null;
      try { text=doc.getText(0,doc.getLength()); }
      catch(BadLocationException e) { e.printStackTrace(); return; } // shouldn't happen
      
      // looks for an end tag to the right of the caret
      int nextTagStart=text.indexOf("<tag ",pos);
      if(nextTagStart==-1) return; // no end tag to the right
      int nextTagEnd=text.indexOf('>',nextTagStart);
      if(nextTagEnd==-1) return; // tag doesn't end (shouldn't happen)
      int endValue=text.indexOf(" value=\"end\"/>",nextTagStart);
      if(endValue==-1 || endValue>nextTagEnd) return; // not an end tag
      
      int prevTagStart=text.lastIndexOf("<tag ",pos-1);
      if(prevTagStart==-1) return; // no start tag to the left
      int prevTagEnd=text.indexOf('>',prevTagStart);
      if(prevTagEnd==-1 || prevTagEnd>pos) return; // tag doesn't end (shouldn't happen)
      int startValue=text.indexOf(" value=\"start\"/>",prevTagStart);
      if(startValue==-1 || startValue>prevTagEnd) return; // not a start tag
      
      // have a valid start and end tag -> delete them and restore caret/selection
      try
      {
          String content=text.substring(prevTagEnd+1,nextTagStart); // tagged text
          doc.remove(prevTagStart,nextTagEnd+1-prevTagStart); // remove whole tagged region
          doc.insertString(prevTagStart,content,null); // add back tagged text w/o formatting
          tp.setSelectionStart(selectionStart-prevTagEnd+prevTagStart-1);
          tp.setSelectionEnd(selectionEnd-prevTagEnd+prevTagStart-1);          
      }
      catch(BadLocationException e) { e.printStackTrace(); } // shouldn't happen
  }
  
  private void LoadTags() {
    if (ConfirmTagSave()) {
      if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	toolbar = MakeButtons(fc.getSelectedFile());
	OutputDocument(tp.getText());
	tagsChanged = false;
      }
    }
  }


  private boolean ConfirmTagSave() {
    if (tagsChanged) {
      int n = JOptionPane.showConfirmDialog(this,
		 "Save changes to current tag file first?", "Opening",
		 JOptionPane.YES_NO_CANCEL_OPTION);
      if (n == JOptionPane.YES_OPTION) {
	if (!SaveTags(true)) return false;
      }
      else if (n == JOptionPane.CANCEL_OPTION) {
	return false;
      }
    }
    return true;
  }


  private boolean SaveTags(boolean saveColor) {
    int val = fc.showSaveDialog(this);
    if (val == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
      
      try {
	DataOutputStream out =
	  new DataOutputStream(new FileOutputStream(file));
	for (int i = 0; i < tags.size(); i++) {
	  String contents = (String) tags.elementAt(i);
	  if (saveColor) {
	    SimpleAttributeSet attr =
	      (SimpleAttributeSet) colorMaps.get(contents);
	    Color c = StyleConstants.getForeground(attr);
	    contents += "\t" + Integer.toString(c.getRed()) + "\t"
	      + Integer.toString(c.getGreen()) + "\t"
              + Integer.toString(c.getBlue());
	  }
	  contents += "\n";
	  out.writeBytes(contents);
	}
	out.close();
	tagsChanged = false;
	return true;
      } catch (IOException e) {
	ErrorMessage("There was a problem saving the tag file");
      }
    }
    return false;
  }


  private void AddTag() {
    String n = JOptionPane.showInputDialog(this,
		  "Name of new tag", "Tag creation",
		  JOptionPane.INFORMATION_MESSAGE);

    if (n != null) {      
      tagsChanged = true;
      tags.addElement(n);

      Color newColor = GetColor(n,
	    (Color)defaultColors.elementAt(defaultColorIndex%NUM_TAGS));

      if (newColor == null) {
	newColor = (Color)defaultColors.elementAt(defaultColorIndex%NUM_TAGS);
      }
      defaultColorIndex++;

      SimpleAttributeSet attr = new SimpleAttributeSet();
      StyleConstants.setForeground(attr, newColor);
      colorMaps.put(n, attr);

      JButton button = new JButton(n, new ColorIcon(newColor));
      button.addActionListener(actor);
      toolbar.add(button);
      buttons.addElement(button);
    }
  }


  private void ChangeTagColor() {
    //dialog box to pick which tag to replace
    if (tags.isEmpty()) return;

    final JList list = new JList(tags);
    final JScrollPane sp = new JScrollPane(list);
    sp.setPreferredSize(new Dimension(200, 100));
    sp.setMinimumSize(new Dimension(200, 100));
    sp.setAlignmentX(LEFT_ALIGNMENT);

    JPanel jp = new JPanel();
    jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
    jp.add(sp);
    jp.add(Box.createRigidArea(new Dimension(0, 5)));
    jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 100));

    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    buttonPane.add(Box.createHorizontalGlue());

    final JButton changeButton = new JButton("Select");
    final JButton cancelButton = new JButton("Cancel");

    buttonPane.add(changeButton);
    buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPane.add(cancelButton);

    final JDialog d = new JDialog(this, "Choose a tag");
    d.getContentPane().add(sp, BorderLayout.CENTER);
    d.getContentPane().add(buttonPane, BorderLayout.SOUTH);

    cancelButton.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	      d.setVisible(false);
	    }
         }
    );

    changeButton.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	      int n = list.getSelectedIndex();
	      d.setVisible(false);
	      if (n != -1) {
		SimpleAttributeSet attr = (SimpleAttributeSet)
		  colorMaps.get(tags.elementAt(n));
		Color c = GetColor((String) tags.elementAt(n),
				   StyleConstants.getForeground(attr));
		if (c != null) {
		  StyleConstants.setForeground(attr, c);
		  ((JButton) buttons.elementAt(n)).setIcon(new ColorIcon(c));
		  OutputDocument(tp.getText());
		  tagsChanged = true;
		}
	      }
	    }
         }
    );

    d.pack();
    d.setLocationRelativeTo(this);
    d.setVisible(true);
  }


  private void OutputDocument(String contents) {
    String substr, tagText;
    String currTag = "";
    SimpleAttributeSet currAttr = null;
    try {
      doc = new DefaultStyledDocument();
      doc.addUndoableEditListener(undoManager);
      tp.setDocument(doc);
      
      StringTokenizer st = new StringTokenizer(contents, "<>", true);
      
      while (st.hasMoreTokens()) {
	substr = st.nextToken();
	if (substr.equals("<")) {
	  tagText = st.nextToken();  //this gets everything inside the tag
	  if (tagText.startsWith("tag")) {
	    String name = "";
	    String value = "";
	    StringTokenizer st2 = new StringTokenizer(tagText, " \t\r\n=\"",
						      false);
	    String xmlAttribute = "";
	    while (st2.hasMoreTokens()) {
	      xmlAttribute = st2.nextToken();
	      if (xmlAttribute.equals("name")) {
		name = st2.nextToken();
	      }
	      else if (xmlAttribute.equals("value")) {
		value = st2.nextToken();
	      }
	    }

	    if (currTag.equals("") && value.equals("start")
		                 && !name.equals("")) {
	      SimpleAttributeSet attr =
		(SimpleAttributeSet) colorMaps.get(name);
	      if (attr != null) {
		currTag = name;
		currAttr = attr;
	      }
	    }
	    else if (name.equals(currTag) && value.equals("end")) {
	      substr+=tagText;
	      substr+=st.nextToken();    //to get the >
	      doc.insertString(doc.getLength(), substr, currAttr);
	      currAttr = null;
	      currTag = "";
	      continue;
	    }	      

	  }
	  else {
	    if (hideHtml) {
	      substr = substr + tagText;
	      substr = substr + st.nextToken();
	      doc.insertString(doc.getLength(), substr, invisible);
	      continue;
	    }
	  }
	  substr = substr + tagText;
	}
	doc.insertString(doc.getLength(), substr, currAttr);
      }   

      
    } catch(BadLocationException be) {
    } catch(IllegalArgumentException ie) {
    } catch(NullPointerException np) {}

    if (!currTag.equals("")) ErrorMessage("Unterminated tag \"" + currTag
					  + "\"");
  }


/****************
  MENUBAR
 ****************/

  private JMenuBar AddMenuBar() {
    JMenuBar menubar = new JMenuBar();
    
    JMenu fileMenu = new JMenu("File");
    menubar.add(fileMenu);
    
    JMenu editMenu = new JMenu("Edit");
    menubar.add(editMenu);

    viewMenu = new JMenu("View");   //this one's global
    menubar.add(viewMenu);
    
    tagsMenu = new JMenu("Tags");
    menubar.add(tagsMenu);
    
    JMenu aboutMenu = new JMenu("About");
    menubar.add(aboutMenu);


    //FILE MENU
    JMenuItem menuitem = new JMenuItem("Open");
    menuitem.setMnemonic('O');
    menuitem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O,
						   java.awt.Event.CTRL_MASK));
    menuitem.addActionListener(actor);
    fileMenu.add(menuitem);
    
    menuitem = new JMenuItem("Save");
    menuitem.setMnemonic('S');
    menuitem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
						   java.awt.Event.CTRL_MASK));
    menuitem.addActionListener(actor);
    fileMenu.add(menuitem);
    
    menuitem = new JMenuItem("Save As");   
    menuitem.addActionListener(actor);
    fileMenu.add(menuitem);
    
    fileMenu.add(new JSeparator());    
    
    // submenu for batch processing actions
    JMenu submenu = new JMenu("Batch Processing");
    
    menuitem = new JMenuItem("Batch Process");
    menuitem.setMnemonic('B');
    // use CTRL-MASK because everything else here does...
    menuitem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B,
						   java.awt.Event.CTRL_MASK));
    menuitem.addActionListener(actor);    
    submenu.add(menuitem);
    
    submenu.add(new JSeparator());
    
    nextItem = new JMenuItem("Next File");
    nextItem.setMnemonic('N');
    nextItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT,
						   java.awt.Event.CTRL_MASK));
    nextItem.addActionListener(actor);    
    submenu.add(nextItem);
    
    prevItem = new JMenuItem("Previous File");
    prevItem.setMnemonic('P');
    prevItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,
						   java.awt.Event.CTRL_MASK));
    prevItem.addActionListener(actor);    
    submenu.add(prevItem);        
    
    saveNextItem = new JMenuItem("Save and Next");    
    saveNextItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT,						   
                                                   java.awt.Event.ALT_MASK));
    saveNextItem.addActionListener(actor);    
    submenu.add(saveNextItem);
    
    savePrevItem = new JMenuItem("Save and Previous");    
    savePrevItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,
                                                   java.awt.Event.ALT_MASK));
    savePrevItem.addActionListener(actor);    
    submenu.add(savePrevItem);        
        
    fileMenu.add(submenu);   
    
    fileMenu.add(new JSeparator());
    
    menuitem = new JMenuItem("Exit");
    menuitem.setMnemonic('X');
    menuitem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q,
						   java.awt.Event.CTRL_MASK));
    menuitem.addActionListener(actor);
    fileMenu.add(menuitem);
    
    
    //EDIT MENU
    undo = new JMenuItem("Undo");
    undo.setMnemonic('U');
    undo.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
                        java.awt.Event.CTRL_MASK));
    undo.addActionListener(actor);
    editMenu.add(undo);
    
    redo = new JMenuItem("Redo");
    redo.setMnemonic('R');
    redo.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y,
                        java.awt.Event.CTRL_MASK));
    redo.addActionListener(actor);
    editMenu.add(redo);
    
    editMenu.addSeparator();
    
    menuitem = new JMenuItem("Cut");
    menuitem.setMnemonic('T');
    menuitem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X,
						   java.awt.Event.CTRL_MASK));
    menuitem.addActionListener(actor);
    editMenu.add(menuitem);
    
    menuitem = new JMenuItem("Copy");
    menuitem.setMnemonic('C');
    menuitem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C,
						   java.awt.Event.CTRL_MASK));
    menuitem.addActionListener(actor);
    editMenu.add(menuitem);
    
    menuitem = new JMenuItem("Paste");
    menuitem.setMnemonic('P');
    menuitem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V,
						   java.awt.Event.CTRL_MASK));
    menuitem.addActionListener(actor);
    editMenu.add(menuitem);
    
    menuitem = new JMenuItem("Select All");
    menuitem.setMnemonic('S');
    menuitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,Event.CTRL_MASK));
    menuitem.addActionListener(actor);
    menuitem.setEnabled(false);
    editMenu.add(menuitem);

    editMenu.addSeparator();
    
    menuitem = new JMenuItem("Untag selected region");
    menuitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,Event.CTRL_MASK));
    menuitem.addActionListener(actor);
    editMenu.add(menuitem);    

    menuitem = new JMenuItem("Tag All Occurrences");
    menuitem.setMnemonic('A');
    menuitem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,
						   java.awt.Event.ALT_MASK));
    menuitem.addActionListener(actor);
    editMenu.add(menuitem);
    
    
    

    //VIEW MENU
    menuitem = new JMenuItem("Show all HTML");
    menuitem.addActionListener(actor);
    viewMenu.add(menuitem);
    
    
    //TAGS MENU
    menuitem = new JMenuItem("Load tag file");
    menuitem.addActionListener(actor);
    tagsMenu.add(menuitem);
    
    menuitem = new JMenuItem("Add tag");
    menuitem.addActionListener(actor);
    tagsMenu.add(menuitem);
    
    menuitem = new JMenuItem("Change tag color");
    menuitem.addActionListener(actor);
    tagsMenu.add(menuitem);
    
    menuitem = new JMenuItem("Save tag file");
    menuitem.addActionListener(actor);
    tagsMenu.add(menuitem);
    
    menuitem = new JMenuItem("Save tag file without colors");
    menuitem.addActionListener(actor);
    tagsMenu.add(menuitem);
    
    menuitem = new JMenuItem("Update display");
    menuitem.addActionListener(actor);
    tagsMenu.add(menuitem);

    warningsMenuItem = new JMenuItem("Suppress warnings");
    warningsMenuItem.addActionListener(actor);
    tagsMenu.add(warningsMenuItem);

    
    //ABOUT MENU
    menuitem = new JMenuItem("About");
    menuitem.addActionListener(actor);
    aboutMenu.add(menuitem);
    
    return menubar;
  }


/****************
  TOOLBAR
 ****************/

  private JToolBar InitButtons(String fileName) {
    if (fileName == null) {
      return MakeButtons(null);
    }
    File f = new File(fileName);
    return MakeButtons(f);
  }


  private JToolBar MakeButtons(File f) {
    if (defaultColors == null) InitColors();
    
    tags.clear();
    toolbar.removeAll();
    buttons.clear();
    toolbar.setFloatable(false);
    
    try {
      toolbar.setOrientation(javax.swing.JToolBar.VERTICAL);
    } catch (IllegalArgumentException e) {}
    
    colorMaps = new Hashtable();
    
    if (f == null) return toolbar;
        
    try {
      BufferedReader input = new BufferedReader(new FileReader(f));
      
      String line;
      try {
	while ((line = input.readLine()) != null) {
	  if (line.equals("")) continue;
	  
	  StringTokenizer st = new StringTokenizer(line, "\t");
	  String tagName = st.nextToken();
	  Color newColor = null;
	  int r = 0, g = 0, b = 0;
	  if (st.hasMoreTokens()) {
	    r = Integer.parseInt(st.nextToken()) % 256;
	    if (st.hasMoreTokens())
	      g = Integer.parseInt(st.nextToken()) % 256;
	    if (st.hasMoreTokens())
	      b = Integer.parseInt(st.nextToken()) % 256;
	    newColor = new Color(r, g, b);
	  }
	  
	  tags.addElement(tagName);
	  
	  if (newColor == null) {
	    newColor =
	      (Color)defaultColors.elementAt(defaultColorIndex%NUM_TAGS);
	    defaultColorIndex++;
	  }
	  
	  SimpleAttributeSet attr = new SimpleAttributeSet();
	  StyleConstants.setForeground(attr, newColor);
	  colorMaps.put(tagName, attr);
	  JButton button = new JButton(tagName, new ColorIcon(newColor));
	  button.addActionListener(actor);
	  toolbar.add(button);
	  buttons.addElement(button);
	}
	input.close();

      } catch(IOException ie) {
	ErrorMessage("There was a problem loading the tag file");
      } catch (NumberFormatException nfe) {
	ErrorMessage("The tag file is not formatted properly");
	try {input.close();} catch (IOException ioe) {}
      }
    } catch(FileNotFoundException e) {
      ErrorMessage("Tag file not found");
    }
    
    toolbar.revalidate();
    toolbar.repaint();
    return toolbar;
  }

    // Batch Processing Code
  
    /** 
     * Opens the batch processing dialog to obtain the source and target 
     * directories, and makes the batch processing toolbar visible, and the 
     * menu items available.
     */
    private void initBatchProcess() {
        bpDlg.setLocation(getLocationOnScreen().x+(getWidth()-bpDlg.getWidth())/2,
                          getLocationOnScreen().y+(getHeight()-bpDlg.getHeight())/2);
        bpDlg.show();

        int status=bpDlg.getStatus();
        if(status==BatchProcessDialog.OK_OPTION) {
            sourceDir=bpDlg.getSourceDir();
            targetDir=bpDlg.getTargetDir();
            try {
                File file=new File(sourceDir);
                files=file.listFiles(new BatchProcessFileFilter(bpDlg.getUnannotatedOnly()));
            } catch(Exception e) {
                System.err.println("Error loading directory "+sourceDir);
            }
            numFilesLabel.setText("of "+files.length);
            openFile(1);
            enableBatchProcess(true);
        }
    }

    /** 
     * File filter for accepting only files.  If <tt>unannotatedOnly</tt> is
     * true, accepts only files that aren't in the target directory.
     */
    private class BatchProcessFileFilter implements FileFilter {
        private boolean unannotatedOnly;
        
        public BatchProcessFileFilter(boolean unannotatedOnly) {
            this.unannotatedOnly=unannotatedOnly;
        }
        
        public boolean accept(File file) {
            if(file.isFile()) {
                // check to see if the file is in the target directory
                if(unannotatedOnly) {
                    File targetFile=new File(targetDir+File.separator+file.getName());
                    if(targetFile.exists()) return false;
                }
                return true;
            }
            return false;
        }
    }
    
    /** 
     * Utility method for disabling/enabling batch mode as well as the 
     * relevant GUI components.
     */
    private void enableBatchProcess(boolean enabled) {
        batchMode=enabled;
        nextItem.setEnabled(enabled);
        saveNextItem.setEnabled(enabled);
        prevItem.setEnabled(enabled);
        savePrevItem.setEnabled(enabled);
        batchBar.setVisible(enabled);
    }
    
    /** 
     * Scrolls backwards one file in the list in batch processing mode.  If
     * <tt>autosave</tt> is true, automatically saves the currently open file. 
     * Otherwise, prompts the user to save the file if dirty.
     */
    private void openPrevFile(boolean autoSave) {
        if(fileIndex-1<=0) return;
        if(autoSave) SaveFile(true);
        openFile(fileIndex-1);                        
    }

    /** 
     * Scrolls forward one file in the list in batch processing mode.  If
     * <tt>autosave</tt> is true, automatically saves the currently open file. 
     * Otherwise, prompts the user to save the file if dirty.
     */
    private void openNextFile(boolean autoSave) {
        if(fileIndex+1>files.length) return;
        if(autoSave) SaveFile(true);
        openFile(fileIndex+1);
    }

    /**
     * Opens the file at the given index in batch processing mode. 
     * The index starts at 1, so it's offset by 1 from the actual file array.
     * Assumes that the given index is valid (i.e. >0 && <=files.length).
     * Stores the given index in global fileIndex.
     */
    private void openFile(int index) {
        fileIndex=index;
        fileNumField.setText(String.valueOf(index));
        
        File file=new File(targetDir+File.separator+files[index-1].getName());
        // if the file was previously tagged, open the tagged version
        if(file.exists()) OpenFile(file,true);
        else OpenFile(files[index-1]);        
        
        prevItem.setEnabled(index>1);
        savePrevItem.setEnabled(index>1);
        savePrevButton.setEnabled(index>1);
        prevButton.setEnabled(index>1);
        nextItem.setEnabled(index<files.length);
        saveNextItem.setEnabled(index<files.length);
        saveNextButton.setEnabled(index<files.length);        
        nextButton.setEnabled(index<files.length);
    }
  
    // Batch Processing Toolbar
    private JToolBar makeBatchToolbar() {
        JToolBar batchBar=new JToolBar();

        savePrevButton=new JButton("<< SAVE");
        savePrevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { openPrevFile(true); }
        });        
        batchBar.add(savePrevButton);

        prevButton=new JButton("<<");
        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { openPrevFile(false); }
        });    
        batchBar.add(prevButton);

        nextButton=new JButton(">>");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { openNextFile(false); }
        });    
        batchBar.add(nextButton);    

        saveNextButton=new JButton("SAVE >>");
        saveNextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { openNextFile(true); }
        });    
        batchBar.add(saveNextButton);

        batchBar.addSeparator(new Dimension(10,20));

        fileNumField=new JTextField();
        fileNumField.setPreferredSize(new Dimension(30,20));
        fileNumField.setMaximumSize(new Dimension(30,20));
        fileNumField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { 
                int index=Integer.parseInt(fileNumField.getText());
                if(index<=0 || index>files.length) 
                    fileNumField.setText(String.valueOf(fileIndex));
                else openFile(index);
            }
        });
        batchBar.add(fileNumField);

        batchBar.addSeparator(new Dimension(10,20));
        
        numFilesLabel=new JLabel("of");
        batchBar.add(numFilesLabel);

        return batchBar;
    }

/****************
  UTILITY
 ****************/

  private void showAbout() {
    JOptionPane.showMessageDialog(this,
	 ABOUT + 
         "\nMiler Lee\nStanford University\nmiler@cs.stanford.edu",
	 "About Annotator", JOptionPane.INFORMATION_MESSAGE);	    
  }


  private void ErrorMessage(String msg) {
    JOptionPane.showMessageDialog(this, msg,
	         "Error", JOptionPane.OK_OPTION);
  }


  private Color GetColor(String tag, Color initialColor) {
    JPanel jp = new JPanel();
    
    final JLabel tc = new JLabel(tag);
    tc.setBackground(new Color(255, 255, 255));

    jp.add(tc, BorderLayout.CENTER);

    colorChooser.setPreviewPanel(jp);
    colorChooser.setColor(initialColor);
    tc.setForeground(initialColor);
    colorChooser.getSelectionModel().addChangeListener(
	  new ChangeListener() {
              public void stateChanged(ChangeEvent e) {
                  Color newColor = colorChooser.getColor();
                  tc.setForeground(newColor);
              }
          }
    );

    JDialog d = JColorChooser.createDialog(this, "Choose tag color", true,
					   colorChooser, actor, actor);
    d.setVisible(true);
    return colorChooser.getColor();

  }


  private void InitColors() {
    defaultColors = new Vector(NUM_TAGS);
    defaultColors.addElement( new Color(255, 0, 0) );
    defaultColors.addElement( new Color(0, 153, 0) );
    defaultColors.addElement( new Color(0, 0, 255) );
    defaultColors.addElement( new Color(255, 102, 0));
    defaultColors.addElement( new Color(255, 0, 255));
    defaultColors.addElement( new Color(0, 153, 255));
    defaultColors.addElement( new Color(153, 51, 0));
    defaultColors.addElement( new Color(0, 102, 102));
    defaultColors.addElement( new Color(255, 0, 153));
    defaultColors.addElement( new Color(153, 153, 153));
    defaultColors.addElement( new Color(204, 153, 51));
    defaultColors.addElement( new Color(0, 0, 153));
  }


  static class ColorIcon implements Icon {
    Color color;
    
    public ColorIcon(Color c) { color = c; }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(color);
      g.fillRect(x, y, getIconWidth(), getIconHeight());
    }
    
    public int getIconWidth() { return 10; }
    public int getIconHeight() { return 10; }
  }


  /**Annotator is run from the command line:<br>
     <code>java edu.stanford.nlp.annotation.Annotator [tagFileName]</code>
     @param args TagFileName command line argument (optional)
  */

  public static void main(String[] args) {
    WindowListener l = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	System.exit(0);
      }
    };

    String filename = null;
    if (args.length > 0) {
	filename = args[0];
    }

    JFrame frame = new Annotator(filename, l);
    frame.pack();
    frame.setVisible(true);
  }

}
