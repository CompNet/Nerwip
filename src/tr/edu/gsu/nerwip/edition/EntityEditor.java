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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.evaluation.ArticleList;
import tr.edu.gsu.nerwip.recognition.AbstractRecognizer;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.combiner.AbstractCombiner.SubeeMode;
import tr.edu.gsu.nerwip.recognition.combiner.fullcombiner.FullCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.fullcombiner.FullCombiner.Combiner;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.svmbased.SvmCombiner.CombineMode;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner;
import tr.edu.gsu.nerwip.recognition.combiner.votebased.VoteCombiner.VoteMode;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.Illinois;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.illinois.IllinoisModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe.LingPipe;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.lingpipe.LingPipeModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp.OpenNlp;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.opennlp.OpenNlpModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.Stanford;
import tr.edu.gsu.nerwip.recognition.internal.modelbased.stanford.StanfordModelName;
import tr.edu.gsu.nerwip.recognition.internal.modelless.dateextractor.DateExtractor;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais.OpenCalais;
import tr.edu.gsu.nerwip.recognition.internal.modelless.subee.Subee;
import tr.edu.gsu.nerwip.recognition.internal.modelless.wikipediadater.WikipediaDater;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLogger;
import tr.edu.gsu.nerwip.tools.log.HierarchicalLoggerManager;
import tr.edu.gsu.nerwip.tools.string.LinkTools;
import tr.edu.gsu.nerwip.tools.string.StringTools;
import tr.edu.gsu.nerwip.tools.corpus.ArticleLists;
import tr.edu.gsu.nerwip.tools.file.FileNames;
import tr.edu.gsu.nerwip.tools.file.FileTools;

/**
 * Window used to display and edit annotated texts,
 * i.e. texts with identified named entities.
 * It relies on the use of the {@link EntityEditorPanel}.
 * 
 * @author Yasa Akbulut
 * @author Vincent Labatut
 */
@SuppressWarnings("unused")
public class EntityEditor implements WindowListener, ChangeListener
{
	/**
	 * Builds a new, empty EntityEditor.
	 * Data must be provided through the
	 * {@link #setArticle(String)} method.
	 */
	public EntityEditor()
	{	// set up tooltip popup speed
		ToolTipManager.sharedInstance().setInitialDelay(400);
		
		// create frame
		frame = new JFrame(TITLE);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(1200, 500);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);

		// init actions
		initActions();
		
		// add file chooser
		initFileChooser();
		
		// add menu bar
		initMenuBar();
		
		// add tool bar
		initToolBar();
		
		// add tabbed pane
//		tabbedPane = new MovableTabbedPane();
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);
		frame.getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);
		
		// add status bar
		initStatusBar();
		
        setStatusInformation("Waiting...");
		frame.setVisible(true);
	}
	
	/////////////////////////////////////////////////////////////////
	// MAIN				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Launches the editor allowing to display entities
	 * and modify references. 
	 * 
	 * @param args
	 * 		None needed.
	 *  
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	public static void main(String[] args) throws Exception
	{	HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
		logger.setEnabled(false);
		
		// set up viewer
		Locale.setDefault(Locale.ENGLISH);
		EntityEditor viewer = new EntityEditor();
		
		// select specific NER tools
		List<String> prefixes = Arrays.asList(new String[]
		{	
//			new DateExtractor().getFolder(),
//			
//			new Illinois(IllinoisModelName.CONLL_MODEL, true, false, false, false).getFolder(),
//			new Illinois(IllinoisModelName.CONLL_MODEL, true, false, false, true).getFolder(),	// LOC, ORG, PERS
//			new Illinois(IllinoisModelName.CONLL_MODEL, true, false, true,  false).getFolder(),
//			new Illinois(IllinoisModelName.CONLL_MODEL, true, false, true,  true).getFolder(),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, true, false, false, false).getFolder(),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, true, false, false, true).getFolder(),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, true, false, true,  false).getFolder(),
//			new Illinois(IllinoisModelName.ONTONOTES_MODEL, true, false, true,  true).getFolder(),
//			new Illinois(IllinoisModelName.NERWIP_MODEL, true, true, false, false).getFolder(),
//			new Illinois(IllinoisModelName.NERWIP_MODEL, true, true, false, true).getFolder(),
//			new Illinois(IllinoisModelName.NERWIP_MODEL, true, true, true, false).getFolder(),	// LOC, ORG, PERS
//			new Illinois(IllinoisModelName.NERWIP_MODEL, true, true, true, true).getFolder(),
//				
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, false, false, false, false).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, false, false, false, true).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, false, false, true,  false).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, false, false, true,  true).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, false, true,  false, false).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, false, true,  false, true).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, false, true,  true,  false).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, false, true,  true,  true).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, true,  false, false, false).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, true,  false, false, true).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, true,  false, true,  false).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, true,  false, true,  true).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, true,  true,  false, false).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, true,  true,  false, true).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, true,  true,  true,  false).getFolder(),
//			new LingPipe(LingPipeModelName.PREDEFINED_MODEL, true, true,  true,  true,  true).getFolder(),	// LOC, ORG, PERS
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, false, false, false, false).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, false, false, false, true).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, false, false, true,  false).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, false, false, true,  true).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, false, true,  false, false).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, false, true,  false, true).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, false, true,  true,  false).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, false, true,  true,  true).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, true,  false, false, false).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, true,  false, false, true).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, true,  false, true,  false).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, true,  false, true,  true).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, true,  true,  false, false).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, true,  true,  false, true).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, true,  true,  true,  false).getFolder(),	// ?
//			new LingPipe(LingPipeModelName.NERWIP_MODEL, true, true,  true,  true,  true).getFolder(),	// ?
//			
//			new OpenCalais(false, false).getFolder(),
//			new OpenCalais(false, true).getFolder(),
//			new OpenCalais(true,  false).getFolder(),	// (DATE), LOC, ORG, PERS	
//			new OpenCalais(true,  true).getFolder(),
//			
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,true, false,false).getFolder(),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,true, false,true).getFolder(),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,true, true, false).getFolder(),
//			new OpenNlp(OpenNlpModelName.ORIGINAL_MODEL,true, true, true).getFolder(),	// DATE, LOC, ORG, PERS
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,true, false,false).getFolder(),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,true, false,true).getFolder(),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,true, true, false).getFolder(),
//			new OpenNlp(OpenNlpModelName.NERWIP_MODEL,true, true, true).getFolder(),	// LOC, ORG, PERS
//			
//			new Stanford(StanfordModelName.CONLL_MODEL, true, false, false).getFolder(),
//			new Stanford(StanfordModelName.CONLL_MODEL, true, false, true).getFolder(),
//			new Stanford(StanfordModelName.CONLL_MODEL, true, true,  false).getFolder(),
//			new Stanford(StanfordModelName.CONLL_MODEL, true, true,  true).getFolder(),
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, true, false, false).getFolder(),	// LOC, ORG, PERS
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, true, false, true).getFolder(),
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, true, true,  false).getFolder(),
//			new Stanford(StanfordModelName.CONLLMUC_MODEL, true, true,  true).getFolder(),
//			new Stanford(StanfordModelName.MUC_MODEL, true, false, false).getFolder(),		// DATE, LOC, ORG, PERS
//			new Stanford(StanfordModelName.MUC_MODEL, true, false, true).getFolder(),
//			new Stanford(StanfordModelName.MUC_MODEL, true, true,  false).getFolder(),
//			new Stanford(StanfordModelName.MUC_MODEL, true, true,  true).getFolder(),
//			new Stanford(StanfordModelName.NERWIP_MODEL, true, false, false).getFolder(),		// DATE, LOC, ORG, PERS
//			new Stanford(StanfordModelName.NERWIP_MODEL, true, false, true).getFolder(),
//			new Stanford(StanfordModelName.NERWIP_MODEL, true, true,  false).getFolder(),
//			new Stanford(StanfordModelName.NERWIP_MODEL, true, true,  true).getFolder(),
//			
//			new Subee(false,false,false,false,false).getFolder(),
//			new Subee(false,false,false,false,true).getFolder(),
//			new Subee(false,false,false,true,false).getFolder(),
//			new Subee(false,false,false,true,true).getFolder(),
//			new Subee(false,false,true,false,false).getFolder(),
//			new Subee(false,false,true,false,true).getFolder(),
//			new Subee(false,false,true,true,false).getFolder(),
//			new Subee(false,false,true,true,true).getFolder(),
//			new Subee(false,true,false,false,false).getFolder(),
//			new Subee(false,true,false,false,true).getFolder(),
//			new Subee(false,true,false,true,false).getFolder(),
//			new Subee(false,true,false,true,true).getFolder(),
//			new Subee(false,true,true,false,false).getFolder(),
//			new Subee(false,true,true,false,true).getFolder(),
//			new Subee(false,true,true,true,false).getFolder(),
//			new Subee(false,true,true,true,true).getFolder(),
//			new Subee(true,false,false,false,false).getFolder(),
//			new Subee(true,false,false,false,true).getFolder(),
//			new Subee(true,false,false,true,false).getFolder(),
//			new Subee(true,false,false,true,true).getFolder(),
//			new Subee(true,false,true,false,false).getFolder(),
//			new Subee(true,false,true,false,true).getFolder(),
//			new Subee(true,false,true,true,false).getFolder(),
//			new Subee(true,false,true,true,true).getFolder(),
//			new Subee(true,true,false,false,false).getFolder(),
//			new Subee(true,true,false,false,true).getFolder(),
//			new Subee(true,true,false,true,false).getFolder(),
//			new Subee(true,true,false,true,true).getFolder(),
//			new Subee(true,true,true,false,false).getFolder(),
//			new Subee(true,true,true,false,true).getFolder(),
//			new Subee(true,true,true,true,false).getFolder(),
//			new Subee(true,true,true,true,true).getFolder(),
//			
//			new WikipediaDater().getFolder(),
//			
//			new VoteCombiner(true, false, VoteMode.UNIFORM, false, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, false, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, false, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, false, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, false, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, false, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, true, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, true, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, true, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, true, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, true, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.UNIFORM, true, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, false, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.ALL).getFolder(),
//			
//			new VoteCombiner(true, true, VoteMode.UNIFORM, false, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, false, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, false, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, false, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, false, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, false, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, true, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, true, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, true, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, true, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, true, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.UNIFORM, true, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, false, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, false, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, true, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_OVERALL, true, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, false, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, false, true, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, true, false, SubeeMode.ALL).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.NONE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.SINGLE).getFolder(),
//			new VoteCombiner(true, true, VoteMode.WEIGHTED_CATEGORY, true, true, SubeeMode.ALL).getFolder(),
//			
//			new SvmCombiner(true, false, false, CombineMode.ENTITY_UNIFORM, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.ENTITY_UNIFORM, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.ENTITY_UNIFORM, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.CHUNK_SINGLE, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.CHUNK_SINGLE, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.ENTITY_UNIFORM, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.ENTITY_UNIFORM, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.ENTITY_UNIFORM, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.CHUNK_SINGLE, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.CHUNK_SINGLE, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, false, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL).getFolder(),
//
//			new SvmCombiner(true, true, false, CombineMode.ENTITY_UNIFORM, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.ENTITY_UNIFORM, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.ENTITY_UNIFORM, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.CHUNK_SINGLE, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.CHUNK_SINGLE, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, false, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.ENTITY_UNIFORM, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.ENTITY_UNIFORM, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.ENTITY_UNIFORM, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.ENTITY_WEIGHTED_OVERALL, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.ENTITY_WEIGHTED_CATEGORY, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.CHUNK_SINGLE, SubeeMode.ALL).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.NONE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.SINGLE).getFolder(),
//			new SvmCombiner(true, true, true, CombineMode.CHUNK_PREVIOUS, SubeeMode.ALL).getFolder(),
//				
//			new FullCombiner(Combiner.SVM).getFolder(),
//			new FullCombiner(Combiner.VOTE).getFolder()
		});
		viewer.setPrefixes(prefixes);
		
		// set up article by name
		String articleName = "Aart_Kemink";
//		String articleName = "Seamus_Brennan";
//		String articleName = "John_Zorn";
//		String articleName = "Fleur_Pellerin";
		
		// set up article by number
		ArticleList articles = ArticleLists.getArticleList();
//		File article = articles.get(250);
		File article = articles.get(0);
		articleName = article.getName();
		
		String articlePath = FileNames.FO_OUTPUT + File.separator + articleName;
		viewer.setArticle(articlePath);
	}
	
	/////////////////////////////////////////////////////////////////
	// FRAME			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Frame to contain tabs and abstractEntities */
	private JFrame frame;
	/** Title of this application */
	private static final String TITLE_SHORT = "Entity Editor";
	/** Title of this application */
	private static final String TITLE = "Nerwip - " + TITLE_SHORT + " v2";
	/** Article name */
	private String articleName = "";
	
	/**
	 * Updates the title of
	 * the frame, depending
	 * on whether the reference
	 * must be recorded or not.
	 */
	private void updateTitle()
	{	String title = frame.getTitle();
		
		if(changed>0 && !title.endsWith("*"))
		{	title = title + "*";
			frame.setTitle(title);
		}
		
		else if(changed==0 && title.endsWith("*"))
		{	title = title.substring(0,title.length()-1); 
			frame.setTitle(title);
		}
	}
	
	/**
	 * Initializes all actions necessary
	 * to create buttons, menu items
	 * and other gui components.
	 */
	private void initActions()
	{	initFileActions();
		initEntityViewActions();
		initEntityEditionActions();
		initDisplayModeActions();
		initBrowseActions();
		initLinksActions();
		initInformationActions();
		initQuitActions();
	}
	
	/////////////////////////////////////////////////////////////////
	// TAB PANES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Tab to represent a tool's abstractEntities. */
//	private MovableTabbedPane tabbedPane;
	private JTabbedPane tabbedPane;
	/** Selected tab */
	private String selectedTab = null;
	
	/**
	 * Adds a new tab for each annotation tool.
	 * 
	 * @param text
	 * 		Complete text of the article.
	 * @param linkedText
	 * 		Linked text of the article.
	 * @param entities
	 * 		List of estimated entities.
	 * @param references
	 * 		List of reference entities. 
	 * @param name
	 * 		Full name of the tool.
	 */
	private void addTab(String text, String linkedText, Entities entities, Entities references, String name)
	{	String temp[] = name.split("_");
		String algoName = temp[0];
		String status = "";
		String params = null;
		if(temp.length>1)
		{	params = "<html>";
			for(int i=1;i<temp.length;i++)
			{	params = params + temp[i] + "<br>";
				status = status + temp[i] + ", ";
			}
			params = params.substring(0,params.length()-4);
			status = status.substring(0,status.length()-2);
			params = params + "</html>";
		}
		
		// update status
		statusInformationTexts.add(status);

		// get display mode
		boolean modeState = (Boolean)modeTypesAction.getValue(Action.SELECTED_KEY);
		// get type switches
		Map<EntityType,Boolean> switches = new HashMap<EntityType, Boolean>();
		for(EntityType type: EntityType.values())
		{	Action action = entityViewActions.get(type);
			boolean state = (Boolean)action.getValue(Action.SELECTED_KEY);
			switches.put(type,state);
		}
		// get hyperlink switch
		boolean linkState = (Boolean)showLinksAction.getValue(Action.SELECTED_KEY);
		
		// create and add panel
		boolean editable = entities==references;
		EntityEditorPanel panel = new EntityEditorPanel(this, text, linkedText, entities, references, params, modeState, switches, linkState, editable, name);
		tabbedPane.add(algoName, panel);
	}

	/**
	 * Changes the scrollbar position
	 * for all panels at the same time.
	 * (This allows synchronzing them).
	 *  
	 * @param index
	 * 		New position of the scrollbar.
	 * @param source
	 * 		Panel first changing its position.
	 */
	public void setScrollPosition(int index, EntityEditorPanel source)
	{	for(Component c:tabbedPane.getComponents())
		{	if(c!=source)
			{	EntityEditorPanel panel = (EntityEditorPanel) c;
				panel.setScrollPosition(index);
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// STATUS BAR		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Panel containing the status bar labels */
	private JPanel statusBar;
	/** Texts displayed in the status bar */
	private List<String> statusInformationTexts = new ArrayList<String>();
	/** Label displaying information */
	private JLabel statusInformationLabel;
	/** Text displayed in the position label */
	private String STR_STATUS_POSITION = "Pos.: ";
	/** Label displaying position */
	private JLabel statusPositionLabel;
	
	/**
	 * Initialises the status bar,
	 * at the bottom of the window.
	 * It displays information regarding
	 * the currently displayed NER tool
	 * and the position of the cursor
	 * in the text.
	 */
	private void initStatusBar()
	{	// create panel
		{	Container contentPane = frame.getContentPane();
			statusBar = new JPanel();
			statusBar.setLayout(new BorderLayout(2,2)); 
			statusBar.setPreferredSize(new Dimension(100, 20));
			contentPane.add(statusBar, java.awt.BorderLayout.SOUTH);
		}
        
		// create information label
		{	statusInformationLabel = new JLabel("",JLabel.LEFT);
//statusInformationLabel.setOpaque(true);
//statusInformationLabel.setBackground(Color.RED);		
//        	statusInformationLabel.setPreferredSize(new Dimension(100, 16));
//			statusInformationLabel.setBorder(BorderFactory.createLoweredBevelBorder()); 
        	statusBar.add(BorderLayout.WEST,statusInformationLabel);
		}

//		statusBar.add(BorderLayout.CENTER, new JSeparator(SwingConstants.VERTICAL));
		
		// create position label
		{	statusPositionLabel = new JLabel(STR_STATUS_POSITION,JLabel.LEFT);
//statusPositionLabel.setOpaque(true);
//statusPositionLabel.setBackground(Color.BLUE);		
	        statusPositionLabel.setPreferredSize(new Dimension(100, 16));
			statusPositionLabel.setBorder(BorderFactory.createLoweredBevelBorder()); 
			statusBar.add(BorderLayout.EAST,statusPositionLabel);
		}
	}
	
	/**
	 * Changes the content of the status bar.
	 * 
	 * @param text
	 * 		New text to display.
	 */
	private void setStatusInformation(String text)
	{	statusInformationLabel.setText(text);
	}
	
	/**
	 * Changes the position displayed
	 * in the status bar.
	 * 
	 * @param pos
	 * 		New position to be displayed,
	 * 		or {@code null} for no position at all.
	 */
	public void updateStatusPosition(Integer pos)
	{	if(pos==null)
			statusPositionLabel.setText(STR_STATUS_POSITION);
		else
			statusPositionLabel.setText(STR_STATUS_POSITION+pos.toString());
	}
	
	/////////////////////////////////////////////////////////////////
	// TOOL BAR			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Tool bar with the entity types buttons */
	private JPanel toolBar = null;
	/** Map of entity type view buttons */
	private Map<EntityType,PaintableToggleButton> entityViewButtons = null;
	/** Map of entity type insert buttons */
	private Map<EntityType,JButton> entityInsertButtons = null;
	/** Button allowing to delete an entity */
	private JButton entityDeleteButton = null;
	/** Button controling the display mode */
	private JRadioButton modeTypesButton = null;
	/** Button controling the display mode */
	private JRadioButton modeCompButton = null;
	/** Button giving access to the previous article in the folder */
	private JButton prevButton = null;
	/** Button giving access to the next article in the folder */
	private JButton nextButton = null;
		
	/**
	 * Creates and populates the tool bar.
	 */
	private void initToolBar()
	{	// panel
		toolBar = new JPanel();
		toolBar.setBorder(BorderFactory.createRaisedBevelBorder());
		LayoutManager layout = new BoxLayout(toolBar, BoxLayout.LINE_AXIS);
		toolBar.setLayout(layout);
		//toolBar.setPreferredSize(new Dimension(100, 20));
		frame.getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

		// view entity types
		entityViewButtons = new HashMap<EntityType, PaintableToggleButton>();
		for(EntityType type: EntityType.values())
		{	Action action = entityViewActions.get(type);
			PaintableToggleButton button = new PaintableToggleButton(action);
			String name = StringTools.initialize(type.toString());
			button.setText(name);
//			button.setBorder(BorderFactory.createLineBorder(type.getColor()));
//			button.setSelected(false);
			button.setBackground(type.getColor());
//			button.getActionMap().put(name, action);
//			KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
//			button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, name);
			entityViewButtons.put(type, button);
	    	toolBar.add(button);
		}
		
		toolBar.add(Box.createHorizontalGlue());
		
		// mode
		ButtonGroup group = new ButtonGroup();
		{	// display types
			modeTypesButton = new JRadioButton(modeTypesAction);
			modeTypesButton.setText(STR_MODE_TYPES);
//			modeTypesButton.setSelected(true);
			toolBar.add(modeTypesButton);
			group.add(modeTypesButton);
		}
		{	// display comparison
			modeCompButton = new JRadioButton(modeCompAction);
			modeCompButton.setText(STR_MODE_COMP);
			toolBar.add(modeCompButton);
			group.add(modeCompButton);
		}
		
		toolBar.add(Box.createHorizontalGlue());
		
		// edit entities
		entityInsertButtons = new HashMap<EntityType, JButton>();
		for(EntityType type: EntityType.values())
		{	Action action = entityInsertActions.get(type);
			JButton button = new JButton(action);
			String name = type.toString().substring(0,1);
			button.setText(name);
			button.setBackground(type.getColor());
			entityInsertButtons.put(type, button);
	    	toolBar.add(button);
		}
		
		// remove entity
		{	entityDeleteButton = new JButton(entityDeleteAction);
			String name = STR_ENT_REMOVE.substring(0,1);
			entityDeleteButton.setText(name);
	    	toolBar.add(entityDeleteButton);
		}
		
		toolBar.add(Box.createHorizontalGlue());
		
		// browse
		{	// previous article
			{	prevButton = new JButton(prevAction);
				prevButton.setText("<");					//"\u25C0"
				toolBar.add(prevButton);
			}
			// next article
			{	nextButton = new JButton(nextAction);
				nextButton.setText(">");					//"\u25B6"
				toolBar.add(nextButton);
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// MENU BAR			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for menu definition */
	private final static String MENU_FILE = "File";
	/** String used for menu definition */
	private final static String MENU_EDIT = "Edit";
	/** String used for menu definition */
	private final static String MENU_VIEW = "View";
	/** String used for menu definition */
	private final static String MENU_HELP = "Help";
	/** Menu bar of this editor */
	private JMenuBar menuBar;
	/** Menu item of the open action */
	private JMenuItem miOpen;
	/** Menu item of the previous article action */
	private JMenuItem miPrev;
	/** Menu item of the next article action */
	private JMenuItem miNext;
	/** Menu item of the save action */
	private JMenuItem miSave;
	/** Menu item of the save a copy action */
	private JMenuItem miSaveCopy;
	/** Menu item of the close action */
	private JMenuItem miClose;
	/** Menu item of the remove entity action */
	private JMenuItem miRemove;
	/** Menu item of the entity views */
	private Map<EntityType,JCheckBoxMenuItem> entityViewCheck = null;
	/** Menu item of the type display mode */
	private JRadioButtonMenuItem riTypes;
	/** Menu item of the comparison display mode */
	private JRadioButtonMenuItem riComparison;
	/** Menu item of the index action */
	private JMenuItem miIndex;
	/** Menu item of the about action */
	private JMenuItem miAbout;
	/** Menu item of the show links */
	private JCheckBoxMenuItem miLinks;
	
	/**
	 * Initializes the menu bar of this editor.
	 */
	private void initMenuBar()
	{
		menuBar = new JMenuBar();
		
		// file menu
		{	JMenu menu = new JMenu(MENU_FILE);
			menuBar.add(menu);
			
			miOpen = new JMenuItem(loadAction);
			menu.add(miOpen);
			
			miPrev = new JMenuItem(prevAction);
			menu.add(miPrev);
			
			miNext = new JMenuItem(nextAction);
			menu.add(miNext);
			
			menu.addSeparator();
			
			miSave = new JMenuItem(saveAction);
			menu.add(miSave);
			
			miSaveCopy = new JMenuItem(copyAction);
			menu.add(miSaveCopy);
			
			menu.addSeparator();
			
			miClose = new JMenuItem(quitAction);
			menu.add(miClose);
		}
		
		// edit menu
		{	JMenu menu = new JMenu(MENU_EDIT);
			menuBar.add(menu);
			
			// insert entities
			for(EntityType type: EntityType.values())
			{	Action action = entityInsertActions.get(type);
				JMenuItem jmi = new JMenuItem(action);
				menu.add(jmi);
			}
			
			menu.addSeparator();

			// remove entity
			miRemove = new JMenuItem(entityDeleteAction);
			menu.add(miRemove);
		}
		
		// view menu
		{	JMenu menu = new JMenu(MENU_VIEW);
			menuBar.add(menu);
			
			// insert entities
			entityViewCheck = new HashMap<EntityType, JCheckBoxMenuItem>();
			for(EntityType type: EntityType.values())
			{	Action action = entityViewActions.get(type);
				JCheckBoxMenuItem  jmcbi = new JCheckBoxMenuItem(action);
//				jmcbi.setSelected(true);
				entityViewCheck.put(type,jmcbi);
				menu.add(jmcbi);
			}
			
			menu.addSeparator();

			// display mode
			ButtonGroup group = new ButtonGroup();
			{	// display types
				riTypes = new JRadioButtonMenuItem(modeTypesAction);
//				riTypes.setSelected(true);
				group.add(riTypes);
				menu.add(riTypes);
			}
			{	// display comparisons
				riComparison = new JRadioButtonMenuItem(modeCompAction);
				group.add(riComparison);
				menu.add(riComparison);
			}
			
			menu.addSeparator();

			miLinks = new JCheckBoxMenuItem(showLinksAction);
			menu.add(miLinks);
		}
		
		// about menu
		{	JMenu menu = new JMenu(MENU_HELP);
			menuBar.add(menu);
			
			miIndex = new JMenuItem(indexAction);
			menu.add(miIndex);
			
			miAbout = new JMenuItem(aboutAction);
			menu.add(miAbout);
		}

		frame.setJMenuBar(menuBar);
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY VIEW		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for action definition */
	private final static String STR_ENT_VIEW = "View ";
	/** String used for action definition */
	private final static String STR_ENT_DISPLAY = "Display/hide ";
	/** String used for action definition */
	private final static String STR_ENT_ENTITIES = " entities";
	/** Map of entity type view actions */
	private Map<EntityType, Action> entityViewActions = null;

	/**
	 * Initializes the actions related to
	 * the display of entity types.
	 */
	private void initEntityViewActions()
	{	List<Integer> initials = new ArrayList<Integer>();
	
		entityViewActions = new HashMap<EntityType, Action>();
		for(EntityType type: EntityType.values())
		{	final EntityType t = type;
			String typeStr = StringTools.initialize(type.toString());
			String name = STR_ENT_VIEW + typeStr + STR_ENT_ENTITIES;
			Action action = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	switchEntityView(t);
			    }
			};
			entityViewActions.put(type,action);
			int initial = type.toString().charAt(0);
			while(initials.contains(initial)) // allows avoiding setting the same shortcut twice
				initial = initial + 1;
			initials.add(initial);
			action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift "+((char)initial)));
			action.putValue(Action.SHORT_DESCRIPTION, STR_ENT_DISPLAY+typeStr+STR_ENT_ENTITIES);
			action.putValue(Action.SELECTED_KEY, true);
		}
	}

	/**
	 * Hides/displays entities depending
	 * on their type.
	 * 
	 * @param type
	 * 		Type of the entities to hide/display.
	 */
	private void switchEntityView(EntityType type)
	{	PaintableToggleButton button = entityViewButtons.get(type);
//		JCheckBoxMenuItem item = entityViewCheck.get(type);
//		Action action = entityViewActions.get(type);
//		boolean state = (Boolean)action.getValue(Action.SELECTED_KEY);
		int count = tabbedPane.getComponentCount();
		for(int i=0;i<count;i++)
		{	EntityEditorPanel panel = (EntityEditorPanel)tabbedPane.getComponentAt(i);
			panel.switchType(type);
			boolean state = button.isSelected();
			Action action = entityInsertActions.get(type);
			action.setEnabled(state);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY EDITION	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for action definition */
	private final static String STR_ENT_INSERT = "Insert ";
	/** String used for action definition */
	private final static String STR_ENT_INSERT_TT = "Insert or convert ";
	/** String used for action definition */
	private final static String STR_ENT_ENTITY = " entity";
	/** Map of entity type insert actions */
	private Map<EntityType, Action> entityInsertActions = null;
	/** String used for action definition */
	private final static  String STR_ENT_REMOVE = "Remove";
	/** Action allowing to delete an entity */
	private Action entityDeleteAction = null;

	/**
	 * Initializes actions related to the
	 * edition of entities in the reference file.
	 */
	private void initEntityEditionActions()
	{	List<Integer> initials = new ArrayList<Integer>();
		
		// remove entity
		{	String name = STR_ENT_REMOVE+STR_ENT_ENTITY;
			entityDeleteAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent e)
				{	removeEntity();
			    }
			};
			String initial = name.substring(0,1);
			initials.add((int)initial.charAt(0));
			entityDeleteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt "+initial));
			entityDeleteAction.putValue(Action.SHORT_DESCRIPTION, name);
			entityDeleteAction.setEnabled(false);
		}
		
		// insert entities
		entityInsertActions = new HashMap<EntityType, Action>();
		for(EntityType type: EntityType.values())
		{	final EntityType t = type;
			String typeStr = StringTools.initialize(type.toString());
			String name = STR_ENT_INSERT + typeStr + STR_ENT_ENTITY;
			Action action = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent e)
				{	insertEntity(t);
			    }
			};
			entityInsertActions.put(type, action);
			int initial = type.toString().charAt(0);
			while(initials.contains(initial)) // allows avoiding setting the same shortcut twice
				initial = initial + 1;
			initials.add(initial);
			action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt "+((char)initial)));
			action.putValue(Action.SHORT_DESCRIPTION, STR_ENT_INSERT_TT+typeStr+STR_ENT_ENTITY);
			action.setEnabled(false);
		}
	}

	/**
	 * Creates a new entity in the
	 * currently selected tab, and therefore
	 * the corresponding text.
	 * <br/>
	 * The entity corresponds to the currently
	 * selected text. If none is selected, then
	 * no entity is created.
	 * 
	 * @param type
	 * 		Type of the entity to be created.
	 */
	private void insertEntity(EntityType type)
	{	EntityEditorPanel tab = (EntityEditorPanel)tabbedPane.getSelectedComponent();
		AbstractEntity<?> entity = tab.insertEntity(type);
		if(entity!=null)
		{	// update title
			updateSaved(1);
			updateTitle();
			
			//int selectedTab = tabbedPane.getSelectedIndex();
			int size = tabbedPane.getTabCount();
			for(int i=0;i<size;i++)
			{	//if(i!=selectedTab)
				{	EntityEditorPanel pane = (EntityEditorPanel)tabbedPane.getComponentAt(i);
//					pane.insertReference(entity);
					pane.updateHighlighting();
				}
			}
		}
	}
	
	/**
	 * Remove the entity in the
	 * currently selected tab, and therefore
	 * the corresponding text.
	 * <br/>
	 * The concerned entity is the one at
	 * the current position of the cursor.
	 * If the cursor is not included in
	 * any entity, then none is removed.
	 */
	private void removeEntity()
	{	EntityEditorPanel tab = (EntityEditorPanel)tabbedPane.getSelectedComponent();
		List<AbstractEntity<?>> entityList = tab.removeEntities();
		if(!entityList.isEmpty())
		{	// update title
			updateSaved(1);
			updateTitle();
			
			int size = tabbedPane.getTabCount();
			for(int i=0;i<size;i++)
			{	EntityEditorPanel pane = (EntityEditorPanel)tabbedPane.getComponentAt(i);
//				pane.removeReferences(entities);
				pane.updateHighlighting();
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// DISPLAY MODE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for action definition */
	private final static String STR_MODE_TYPES = "Types";
	/** String used for action definition */
	private final static String STR_MODE_TYPES_TT = "Display entity types";
	/** String used for action definition */
	private final static String STR_MODE_COMP = "Comparison";
	/** String used for action definition */
	private final static String STR_MODE_COMP_TT = "Compare entities to reference";
	/** Action controling the display of types */
	private Action modeTypesAction = null;
	/** Action controling the display of comparisons */
	private Action modeCompAction = null;

	/**
	 * Initializes the actions related to
	 * the way entities are displayed.
	 */
	private void initDisplayModeActions()
	{	// display entity types
		{	String name = STR_MODE_TYPES;
			modeTypesAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	switchDisplayMode(true);
			    }
			};
			String initial = name.substring(0,1);
			modeTypesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift "+initial));
			modeTypesAction.putValue(Action.SHORT_DESCRIPTION, STR_MODE_TYPES_TT);
			modeTypesAction.putValue(Action.SELECTED_KEY, true);
		}
		
		// display comparisons
		{	String name = STR_MODE_COMP;
			modeCompAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	switchDisplayMode(false);
			    }
			};
			String initial = name.substring(0,1);
			modeCompAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift "+initial));
			modeCompAction.putValue(Action.SHORT_DESCRIPTION, STR_MODE_COMP_TT);
			modeCompAction.putValue(Action.SELECTED_KEY, false);
		}
	}
	
	/**
	 * Changes the way entities are displayed:
	 * either their types, or a spatial comparison
	 * with the reference.
	 * 
	 * @param mode
	 * 		If {@code true}, then types are displayed.
	 * 		Otherwise, comparisons are displayed.
	 */
	private void switchDisplayMode(boolean mode)
	{	int count = tabbedPane.getComponentCount();
		for(int i=0;i<count;i++)
		{	EntityEditorPanel panel = (EntityEditorPanel)tabbedPane.getComponentAt(i);
			panel.switchMode(mode);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// HYPERLINKS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Action showing/hidding hyperlinks */
	private Action showLinksAction = null;
	/** String used for action definition */
	private final static String STR_LINKS_TT = "Show/Hide the article hyperlinks";
	/** String used for action definition */
	private final static String STR_LINKS = "Hyperlinks";
	
	/**
	 * Initializes actions related to 
	 * hyperlinks.
	 */
	private void initLinksActions()
	{	String name = STR_LINKS;
		showLinksAction = new AbstractAction(name)
		{	/** Class id */
			private static final long serialVersionUID = 1L;
			
			@Override
		    public void actionPerformed(ActionEvent evt)
			{	showLinks();
		    }
		};
		showLinksAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift H"));
		showLinksAction.putValue(Action.SHORT_DESCRIPTION, STR_LINKS_TT);
		showLinksAction.putValue(Action.SELECTED_KEY, false);
	}
	
	/**
	 * Switches on/off the display
	 * of hyperlinks in the text panels.
	 */
	private void showLinks()
	{	int count = tabbedPane.getComponentCount();
		for(int i=0;i<count;i++)
		{	EntityEditorPanel panel = (EntityEditorPanel)tabbedPane.getComponentAt(i);
			panel.switchHyperlinks();
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// BROWSE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for action definition */
	private final static String STR_PREV = "Previous article";
	/** String used for action definition */
	private final static String STR_PREV_TT = "Go to the previous article";
	/** Action giving access to the previous article in the folder */
	private Action prevAction = null;
	/** String used for action definition */
	private final static String STR_NEXT = "Next article";
	/** String used for action definition */
	private final static String STR_NEXT_TT = "Go to the next article";
	/** Action giving access to the next article in the folder */
	private Action nextAction = null;
	
	/**
	 * Initializes actions related to 
	 * browing articles in the output folder.
	 */
	private void initBrowseActions()
	{	// previous article
		{	String name = STR_PREV;
			prevAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	try
					{	setArticle(-1);
					}
					catch (SAXException e)
					{	e.printStackTrace();
					}
					catch (IOException e)
					{	e.printStackTrace();
					}
					catch (ParseException e)
					{	e.printStackTrace();
					}
			    }
			};
			String initial = name.substring(0,1);
			prevAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control "+initial));
			prevAction.putValue(Action.SHORT_DESCRIPTION, STR_PREV_TT);
		}
		
		// next article
		{	String name = STR_NEXT;
			nextAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	try
					{	setArticle(+1);
					}
					catch (SAXException e)
					{	e.printStackTrace();
					}
					catch (IOException e)
					{	e.printStackTrace();
					}
					catch (ParseException e)
					{	e.printStackTrace();
					}
			    }
			};
			String initial = name.substring(0,1);
			nextAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control "+initial));
			nextAction.putValue(Action.SHORT_DESCRIPTION, STR_NEXT_TT);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE ACCESS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for action definition */
	private final static String STR_LOAD = "Open article";
	/** String used for action definition */
	private final static String STR_LOAD_TT = "Load the files related to an article";
	/** Action allowing loading an existing set of files */
	private Action loadAction = null;
	/** String used for action definition */
	private final static String STR_SAVE = "Save";
	/** String used for action definition */
	private final static String STR_SAVE_TT = "Record the modified text and entities";
	/** Action allowing recording the current reference file */
	private Action saveAction = null;
	/** String used for action definition */
	private final static String STR_COPY = "Save copy of reference";
	/** String used for action definition */
	private final static String STR_COPY_TT = "Save a copy of the reference entities";
	/** Action allowing recording a copy of the current reference file */
	private Action copyAction = null;
	
	/**
	 * Initializes the actions related
	 * to file loading and saving.
	 */
	private void initFileActions()
	{	// open
		{	String name = STR_LOAD;
			loadAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	loadArticle();
			    }
			};
			String initial = name.substring(0,1);
			loadAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control "+initial));
			loadAction.putValue(Action.SHORT_DESCRIPTION, STR_LOAD_TT);
		}

		// save
		{	String name = STR_SAVE;
			saveAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	saveAll();
			    }
			};
			String initial = name.substring(0,1);
			saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control "+initial));
			saveAction.putValue(Action.SHORT_DESCRIPTION, STR_SAVE_TT);
			saveAction.setEnabled(false);
		}
		
		// save copy
		{	String name = STR_COPY;
			copyAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	saveReferenceCopy();
			    }
			};
			String initial = name.substring(0,1);
			copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift "+initial));
			copyAction.putValue(Action.SHORT_DESCRIPTION, STR_COPY_TT);
			copyAction.setEnabled(false); //TODO maybe other actions should be disabled too, when there's no article currently open?
		}
	}

	/**
	 * Loads all the files related
	 * to some article, and displays them.
	 */
	private void loadArticle()
	{	int returnVal = articleChooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{	File file = articleChooser.getSelectedFile();
			String name = file.getName();
			String pathStr = FileNames.FO_OUTPUT + File.separator + name;
			try
			{	setArticle(pathStr);
			}
			catch (SAXException e1)
			{	e1.printStackTrace();
			}
			catch (IOException e1)
			{	e1.printStackTrace();
			}
			catch (ParseException e)
			{	e.printStackTrace();
			}
        }
	}
	
	/**
	 * Records modified entities and text.
	 */
	private void saveAll()
	{	// record reference entities
		try
		{	String fileName = currentArticle + File.separator + FileNames.FI_ENTITY_LIST;
			File file = new File(fileName);
			int index = tabbedPane.getSelectedIndex();
			EntityEditorPanel panel = (EntityEditorPanel) tabbedPane.getComponentAt(index);
			Entities references = panel.getReferences();
			references.writeToXml(file);
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
		
		// if the text was modified
		if(changed>1)
		{	int size = tabbedPane.getTabCount();
			for(int i=0;i<size;i++)
			{	EntityEditorPanel panel = (EntityEditorPanel)tabbedPane.getComponentAt(i);
				Entities references = panel.getReferences();
				Entities entities = panel.getEntities();
				if(references!=entities)
				{	String folder = panel.getFolder();
					String fileName = currentArticle + File.separator + folder + File.separator + FileNames.FI_ENTITY_LIST;
					File file = new File(fileName);
					try
					{	entities.writeToXml(file);
					}
					catch (IOException e)
					{	e.printStackTrace();
					}
				}
			}
			
			// record raw text
			File rawFile = new File(currentArticle + File.separator + FileNames.FI_RAW_TEXT);
			try
			{	FileTools.writeTextFile(rawFile, currentRawText);
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
			
			// record linked text
			File linkedFile = new File(currentArticle + File.separator + FileNames.FI_LINKED_TEXT);
			try
			{	FileTools.writeTextFile(linkedFile, currentLinkedText);
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
		}
		
		// update gui
		updateSaved(0);
		updateTitle();
	}
	
	/**
	 * Records a copy of the current 
	 * reference entities.
	 */
	private void saveReferenceCopy()
	{	int returnVal = referenceChooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{	File file = referenceChooser.getSelectedFile();
			try
			{	int index = tabbedPane.getSelectedIndex();
				EntityEditorPanel panel = (EntityEditorPanel) tabbedPane.getComponentAt(index);
				Entities entities = panel.getEntities();
				entities.writeToXml(file);
				updateTitle();
			}
			catch (IOException e1)
			{	e1.printStackTrace();
			}
        }
	}

	/**
	 * Lets the user record modified
	 * reference entities before loading
	 * another article or quitting the application.
	 * The method returns a boolean indicating
	 * if the action was cancel or not.
	 *
	 * @return
	 * 		{@code false} iff the action was canceled.
	 */
	private boolean proposeSaving()
	{	boolean result = true;
		int answer = JOptionPane.showConfirmDialog(frame, "The reference has been modified. Do you want to record these changes?", "Reference modified", JOptionPane.YES_NO_CANCEL_OPTION);
		if(answer==JOptionPane.YES_OPTION)
			saveAll();
		else if(answer==JOptionPane.CANCEL_OPTION)
			result = false;
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// QUIT				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for action definition */
	private final static String STR_QUIT = "Quit "+TITLE_SHORT;
	/** String used for action definition */
	private final static String STR_QUIT_TT = "Quit "+TITLE_SHORT;
	/** Action letting the user close the application */
	private Action quitAction = null;

	/**
	 * Initializes actions related
	 * to quitting the application.
	 */
	private void initQuitActions()
	{	String name = STR_QUIT;
		quitAction = new AbstractAction(name)
		{	/** Class id */
			private static final long serialVersionUID = 1L;
			
			@Override
		    public void actionPerformed(ActionEvent evt)
			{	closeWindow();
		    }
		};
		String initial = name.substring(0,1);
		quitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control "+initial));
		quitAction.putValue(Action.SHORT_DESCRIPTION, STR_QUIT_TT);
	}

	/**
	 * Checks if saving is required,
	 * then close the window.
	 */
	private void closeWindow()
	{	boolean action = true;
		// possibly save
		if(changed>0)
			action = proposeSaving();
		// then only, quit
		if(action)
		{	frame.dispose();
			System.exit(0);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// INFORMATION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Dialog displayed for the action "index" */
	private TextDialog indexDialog = null;
	/** String used for action definition */
	private final static String STR_INDEX = "Index";
	/** String used for action definition */
	private final static String STR_INDEX_TT = "Display basic instructions regarding how to use "+TITLE_SHORT;
	/** String used as the "index" dialog title */
	private final static String STR_INDEX_T = "Help with " + TITLE_SHORT;
	/** Action triggering the 'index' dialog */
	private Action indexAction = null;
	/** String used for action definition */
	private final static String STR_ABOUT = "About";
	/** String used for action definition */
	private final static String STR_ABOUT_TT = "Display general information about "+TITLE_SHORT;
	/** Action triggering the 'about' dialog */
	private Action aboutAction = null;

	/**
	 * Initializes actions related
	 * to help.
	 */
	private void initInformationActions()
	{	// index
		{	String name = STR_INDEX;
			indexAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	displayHelpIndex();
			    }
			};
//			String initial = name.substring(0,1);
//			indexAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control "+initial));
			indexAction.putValue(Action.SHORT_DESCRIPTION, STR_INDEX_TT);
		}
		
		// about
		{	String name = STR_ABOUT;
			aboutAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	displayHelpAbout();
			    }
			};
//			String initial = name.substring(0,1);
//			aboutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control "+initial));
			aboutAction.putValue(Action.SHORT_DESCRIPTION, STR_ABOUT_TT);
		}
	}
	
	/**
	 * Displays the help index dialog.
	 * Its content is retrieved from
	 * a file containing html source code.
	 */
	private void displayHelpIndex()
	{	try
		{	indexDialog = new TextDialog(frame, STR_INDEX_T, FileNames.FI_HELP_PAGE);
			indexDialog.setVisible(true);
		}
		catch (FileNotFoundException e)
		{	e.printStackTrace();
		}
	}
	
	/**
	 * Displays the about dialog.
	 */
	private void displayHelpAbout()
	{	String string = "<html>"
			+"<h1>"+TITLE+"</h1><br/>"
			+ "Galatasaray University<br/>"
			+ "BIT Lab - Complex Networks research group<br/>"
			+ "<a href=\"http://bit.gsu.edu.tr/compnet\">http://bit.gsu.edu.tr/compnet</a><br/>"
			+ "(c) Yasa Akbulut 2011 (as Annotation Viewer)<br/>"
			+ "(c) Vincent Labatut 2013"
			+ "</html>";
		JOptionPane.showMessageDialog(frame, string);
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE CHOOSER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Component used to select the article to open */
	private JFileChooser articleChooser;
	/** Component used to save the reference */
	private JFileChooser referenceChooser;
	/** Indicates if the reference was modified since the last save: 0=no, 1=ref entities, 2=text */
	private int changed = 0;
	
	/**
	 * Creates and initializes the file chooser.
	 */
	private void initFileChooser()
	{	File folder = new File(FileNames.FO_OUTPUT);
		
		articleChooser = new JFileChooser(folder);
		articleChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		articleChooser.setDialogTitle("Select an article folder");
		
		referenceChooser = new JFileChooser(folder);
		referenceChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		referenceChooser.setDialogTitle("Select the new reference file");
	}
	
	/**
	 * Updates the save indicator
	 * and the corresponding GUI elements.
	 * The value 0 means there is no change anymore
	 * (we probably've just recorded them); the
	 * value 1 means only reference entities were
	 * modified; and the value 2 means the text
	 * was modified.  
	 * 
	 * @param changed
	 * 		Changes the current status.
	 */
	private void updateSaved(int changed)
	{	if(changed==0 || this.changed<changed)
			this.changed = changed;
		
		boolean enabled = changed>0;
		saveAction.setEnabled(enabled);
	}
	
	/////////////////////////////////////////////////////////////////
	// FOLDER NAMES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of allowed subfolder names (can be empty) */
	private final List<String> prefixes = new ArrayList<String>();
	
	/**
	 * Restricts the displayed results only
	 * to subfolders whose names are amongst
	 * the ones specified in the parameter.
	 * The reference file is always displayed,
	 * when present. When the prefixes list
	 * is empty, then all results are displayed.
	 * <br/>
	 * This method is meant to speed up the display
	 * of numerous results, which can otherwise make
	 * the GUI veeeery slow.
	 *  
	 * @param prefixes
	 * 		List of subfolder names to display,
	 * 		or an empty list to display everything.
	 */
	private void setPrefixes(List<String> prefixes)
	{	this.prefixes.clear();
		this.prefixes.addAll(prefixes);
	}
	
	/////////////////////////////////////////////////////////////////
	// CHANGE LISTENER	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void stateChanged(ChangeEvent arg0)
	{	Object source = arg0.getSource();
		
		// tabbed pane
		if(source==tabbedPane)
		{	int index = tabbedPane.getSelectedIndex();
		
			// update status
			String status = statusInformationTexts.get(index);
			setStatusInformation(status);
			
			// update actions
			String title = tabbedPane.getTitleAt(index);
			boolean activation = title.equals(RecognizerName.REFERENCE.toString());
			for(EntityType type: EntityType.values())
			{	Action action = entityInsertActions.get(type);
				action.setEnabled(activation);
			}
			entityDeleteAction.setEnabled(activation);
			//saveAction.setEnabled(activation);
			copyAction.setEnabled(activation);
			
			// update selected panel name
			selectedTab = tabbedPane.getTitleAt(index)+status;
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// CONTENT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Filter used to select only entity XML files */
	private static final FilenameFilter FILTER = FileTools.createFilter(FileNames.FI_ENTITY_LIST);
	/** Article currently displayed */
	private String currentArticle = null;
	/** Index of the currently displayed article */
	private int currentIndex = -1;
	/** Raw text of the current article */
	private String currentRawText = null;
	/** Linked text of the current article */
	private String currentLinkedText = null;

	/**
	 * Returns the hyperlinked text
	 * corresponding to the article
	 * which is currently displayed.
	 *  
	 * @return
	 * 		Hyperlinked text.
	 */
	public String getCurrentLinkedText()
	{	return currentLinkedText;
	}
	
	/**
	 * Get the files containing entities,
	 * from the path of the article.
	 * 
	 * @param articlePath
	 * 		File path of the article.
	 * @return
	 * 		List of File objects.
	 */
	private Map<String,File> getEntityFiles(String articlePath)
	{	Map<String,File> result = new HashMap<String, File>();
		
		// get the reference file
		File articleFolder = new File(articlePath);
		{	String algoName = RecognizerName.REFERENCE.toString();
			File[] entityFiles = articleFolder.listFiles(FILTER);
			if(entityFiles.length>0)
				result.put(algoName,entityFiles[0]);
		}
		
		// get the estimations
		File[] algoFolders = articleFolder.listFiles();
		for(File algoFolder: algoFolders)
		{	if(algoFolder.isDirectory())
			{	String algoName = algoFolder.getName();
				if(prefixes.contains(algoName) || prefixes.isEmpty())
				{	File[] entityFiles = algoFolder.listFiles(FILTER);
					if(entityFiles.length>0)
						result.put(algoName,entityFiles[0]);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Returns lists of entities,
	 * from lists of entity files.
	 * 
	 * @param entityFiles
	 * 		List of entity files.
	 * @return
	 * 		List of the corresponding entity lists, read from the specified files.
	 * 
	 * @throws SAXException
	 * 		Problem while accessing the files.
	 * @throws IOException
	 * 		Problem while accessing the files.
	 * @throws ParseException 
	 * 		Problem while accessing the files.
	 */
	private Map<String,Entities> getEntityLists(Map<String,File> entityFiles) throws SAXException, IOException, ParseException
	{	Map<String,Entities> result = new HashMap<String, Entities>();
		
		for(Entry<String,File> entry: entityFiles.entrySet())
		{	String algoName = entry.getKey();
			File entityFile = entry.getValue();
			Entities entities = Entities.readFromXml(entityFile);
			result.put(algoName, entities);
		}
		
		return result;
	}
	
	/**
	 * Change the current article for a neighboring
	 * one (in the output folder).
	 * 
	 * @param offset
	 * 		(Signed) distance between the current and required articles.
	 * 	
	 * @throws SAXException
	 * 		Problem while accessing the files.
	 * @throws IOException
	 * 		Problem while accessing the files.
	 * @throws ParseException 
	 * 		Problem while accessing the files.
	 */
	private void setArticle(int offset) throws SAXException, IOException, ParseException
	{	// get the sorted list of articles
		ArticleList folders = ArticleLists.getArticleList();
		if(!folders.isEmpty())
		{	// find and set new article
			int index = 0;
			if(currentIndex>=0)
			{	File current = new File(currentArticle);
				index = folders.indexOf(current);
				index = (index + offset + folders.size()) % folders.size();
			}
			File file = folders.get(index);
			String newArticle = file.getPath();
			setArticle(newArticle);
		}
	}
	
	/**
	 * Changes the content of this EntityEditor,
	 * so that it displays entities for the
	 * specified article.
	 * 
	 * @param articlePath
	 * 		File path of the targetted article.
	 * 	
	 * @throws IOException 
	 * 		Problem while accessing the files.
	 * @throws SAXException
	 * 		Problem while accessing the files.
	 * @throws ParseException 
	 * 		Problem while accessing the files.
	 */
	public void setArticle(String articlePath) throws SAXException, IOException, ParseException
	{	boolean action = true;
		if(changed>0)
			action = proposeSaving();
		if(action)
		{	updateSaved(0);
			
			// update info regarding the current article
			currentArticle = articlePath;
			File f = new File(currentArticle);
			ArticleList articles = ArticleLists.getArticleList();
			currentIndex = articles.indexOf(f);
		
			// retrieve texts
			File rawFile = new File(articlePath + File.separator + FileNames.FI_RAW_TEXT);
			currentRawText = FileTools.readTextFile(rawFile);
			File linkedFile = new File(articlePath + File.separator + FileNames.FI_LINKED_TEXT);
			if(linkedFile.exists())
				currentLinkedText = FileTools.readTextFile(linkedFile);
			else
				currentLinkedText = FileTools.readTextFile(rawFile);
			
			// update title
			File temp = new File(articlePath);
			articleName = temp.getName();
			frame.setTitle(TITLE+" - " + articleName + " " + (currentIndex+1) + "/" + articles.size());
			
			// update position
			updateStatusPosition(null);
			
			// get entity files
			Map<String,File> entityFiles = getEntityFiles(articlePath);
			
			// open them to get the entities
			Map<String,Entities> entityLists = getEntityLists(entityFiles);
			
			// clear existing articles
			String selectedTab = this.selectedTab;
			tabbedPane.removeChangeListener(this);
			tabbedPane.removeAll();
			tabbedPane.addChangeListener(this);
			statusInformationTexts.clear();
			
			// put them in a tab
			String refName = RecognizerName.REFERENCE.toString();
			Entities references = entityLists.get(refName);
			if(references==null)
			{	references = new Entities(RecognizerName.REFERENCE);
				updateSaved(1);
				updateTitle();
			}
			else
				entityLists.remove(refName);
			addTab(currentRawText, currentLinkedText, references, references, refName);
			Set<String> names = new TreeSet<String>(entityLists.keySet());
			for(String name: names)
			{	Entities entities = entityLists.get(name);
				addTab(currentRawText, currentLinkedText, entities, references, name);
			}
			
			// select tab
			if(selectedTab!=null)
			{	int index = 0;
				boolean found = false;
				while(index<tabbedPane.getTabCount() && !found)
				{	String tempTitle = tabbedPane.getTitleAt(index) + statusInformationTexts.get(index);
					if(tempTitle.equals(selectedTab))
						found = true;
					else
						index++;
				}
				if(found)
					tabbedPane.setSelectedIndex(index);
			}
			
			frame.repaint();
		}
//		show();
	}	

	/////////////////////////////////////////////////////////////////
	// TEXT EDITION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Method called by the reference panel when
	 * some text is inserted in the reference panel.
	 * The text must be modified in each tool-related
	 * panel, and the entities must be shifted accordingly
	 * (in terms of positions).
	 * 
	 * @param start
	 * 		Starting position of the inserted text.
	 * @param text
	 * 		Inserted text.
	 */
	public void textInserted(int start, String text)
	{	// update texts
		currentRawText = currentRawText.substring(0,start) + text + currentRawText.substring(start);
		int pos = LinkTools.getLinkedTextPosition(currentLinkedText,start);
		currentLinkedText = currentLinkedText.substring(0,pos) + text + currentLinkedText.substring(pos);
		
		// update panels
		int selectedTab = tabbedPane.getSelectedIndex();
		int size = tabbedPane.getTabCount();
		for(int i=0;i<size;i++)
		{	if(i!=selectedTab)
			{	EntityEditorPanel pane = (EntityEditorPanel)tabbedPane.getComponentAt(i);
				pane.textInserted(start, text, currentLinkedText);
			}
		}
		
		// update title
		updateSaved(2);
		updateTitle();
	}
	
	/**
	 * Method called by the reference panel when
	 * some text is removed in the reference panel.
	 * The text must be modified in each tool-related
	 * panel, and the entities must be shifted accordingly
	 * (in terms of positions).
	 * 
	 * @param start
	 * 		Starting position of the removed text.
	 * @param length
	 * 		Length of the removed text.
	 */
	public void textRemoved(int start, int length)
	{	// update texts
		currentRawText = currentRawText.substring(0,start) + currentRawText.substring(start+length);
		currentLinkedText = LinkTools.removeFromLinkedText(currentLinkedText, start, length);
		currentLinkedText = LinkTools.removeEmptyLinks(currentLinkedText);
		
		// update panels
		int selectedTab = tabbedPane.getSelectedIndex();
		int size = tabbedPane.getTabCount();
		for(int i=0;i<size;i++)
		{	if(i!=selectedTab)
			{	EntityEditorPanel pane = (EntityEditorPanel)tabbedPane.getComponentAt(i);
				pane.textRemoved(start, length, currentLinkedText);
			}
		}
		
		// update title
		updateSaved(2);
		updateTitle();
	}
	
	/////////////////////////////////////////////////////////////////
	// WINDOW LISTENER	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public void windowActivated(WindowEvent arg0)
	{	// nothing to do here
	}

	@Override
	public void windowClosed(WindowEvent arg0)
	{	// nothing to do here
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{	closeWindow();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0)
	{	// nothing to do here
	}

	@Override
	public void windowDeiconified(WindowEvent arg0)
	{	// nothing to do here
	}

	@Override
	public void windowIconified(WindowEvent arg0)
	{	// nothing to do here
	}

	@Override
	public void windowOpened(WindowEvent arg0)
	{	// nothing to do here
	}
}
