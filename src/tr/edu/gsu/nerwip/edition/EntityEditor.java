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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;
import org.xml.sax.SAXException;

import tr.edu.gsu.nerwip.data.article.ArticleCategory;
import tr.edu.gsu.nerwip.data.entity.AbstractEntity;
import tr.edu.gsu.nerwip.data.entity.Entities;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.edition.language.Language;
import tr.edu.gsu.nerwip.edition.language.LanguageLoader;
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
import tr.edu.gsu.nerwip.tools.xml.XmlNames;
import tr.edu.gsu.nerwip.tools.xml.XmlTools;
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
	 * Builds a new, empty EntityEditor. Data must be 
	 * provided through the {@link #setArticle(String)} 
	 * method.
	 * 
	 * @throws ParseException
	 * 		Problem while loading the configuration file or the first article. 
	 * @throws IOException 
	 * 		Problem while loading the configuration file or the first article. 
	 * @throws SAXException 
	 * 		Problem while loading the configuration file or the first article. 
	 * @throws ParserConfigurationException 
	 * 		Problem while loading the configuration file. 
	 */
	public EntityEditor() throws SAXException, IOException, ParseException, ParserConfigurationException
	{	// init the corpus folder
		String articlePath = retrieveSettings();
		
		// set up tooltip popup speed
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
		
        // add icon
		String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_APP;
		File iconFile = new File(iconPath);
		Image img = ImageIO.read(iconFile);
		frame.setIconImage(img);
        
		frame.setVisible(true);
		
		if(articlePath.isEmpty())
			mustSetCorpus = true;
		else
			setArticle(articlePath);
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
		
		// change look and feel
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
//		UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		try
		{	LookAndFeelInfo tab[] = UIManager.getInstalledLookAndFeels();
			int i = 0;
			boolean found = false;
			while(i<tab.length && !found)
			{	LookAndFeelInfo info = tab[i];
				if("Nimbus".equals(info.getName()))
				{	UIManager.setLookAndFeel(info.getClassName());
		            found = true;
		        }
				i++;
		    }
		} catch (Exception e)
		{	// Nimbus not available
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			logger.log("WARNING: could not find the Nimbus Look-and-feel");
		}
		
		// check if the settings file already exist
		File settingsFile = new File(EntityEditor.CONFIG_PATH);
		boolean mustCreate = !settingsFile.exists();
		
		// set up viewer
		Locale.setDefault(Locale.ENGLISH);
		EntityEditor viewer = new EntityEditor();
		
		// possibly ask the user to init some values
		if(mustCreate)
			viewer.doFirstLaunch();
		else if(viewer.getMustSetCorpus())
			viewer.doSetCorpus();
		
		// select specific NER tools
//		List<String> prefixes = Arrays.asList(new String[]
//		{	
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
//		});
//		viewer.setPrefixes(prefixes);
		
		// set up article by name
//		String articleName = "Aart_Kemink";
//		String articleName = "Seamus_Brennan";
//		String articleName = "John_Zorn";
//		String articleName = "Fleur_Pellerin";
		
		// set up article by number
//		ArticleList articles = ArticleLists.getArticleList();
//		File article = articles.get(250);
//		File article = articles.get(0);
//		articleName = article.getName();
		
//		String articlePath = FileNames.FO_OUTPUT + File.separator + articleName;
//		viewer.setArticle(articlePath);
	}
	
	/////////////////////////////////////////////////////////////////
	// FRAME			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Frame to contain tabs and entities */
	private JFrame frame;
	/** Version of this application */
	private static final String APP_VERSION = "v2.3";
	/** Name of this application */
	private static final String APP_NAME = "Entity Editor";
	/** Title of this application */
	private static final String TITLE = "Nerwip - " + APP_NAME + " " + APP_VERSION;
	/** Article name */
	private String articleName = "";
	
	/**
	 * Updates the title of the frame, depending
	 * on whether the reference must be recorded or not.
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
		initFontActions();
		initBrowseActions();
		initLinksActions();
		initSettingsActions();
		initInformationActions();
		initQuitActions();
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITIES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used to set up entity names */
	private final static String STR_ENTITY = "Entity";
	
	/** Colors associated to the entities */ 
	protected static final Map<EntityType,Color> ENTITY_COLOR = new HashMap<EntityType,Color>();
	{	ENTITY_COLOR.put(EntityType.DATE, Color.PINK);
		ENTITY_COLOR.put(EntityType.FUNCTION, new Color(180,180,180));
		ENTITY_COLOR.put(EntityType.LOCATION, Color.ORANGE);
		ENTITY_COLOR.put(EntityType.MEETING, new Color(218,112,214));
		ENTITY_COLOR.put(EntityType.ORGANIZATION, Color.CYAN);
		ENTITY_COLOR.put(EntityType.PERSON, Color.YELLOW);
		ENTITY_COLOR.put(EntityType.PRODUCTION, Color.GREEN);
	}
	
	/** Shortcut letters associated to the entities */ 
	private static final Map<EntityType,Character> ENTITY_LETTER = new HashMap<EntityType,Character>();
	{	ENTITY_LETTER.put(EntityType.DATE, 'D');
		ENTITY_LETTER.put(EntityType.FUNCTION, 'F');
		ENTITY_LETTER.put(EntityType.LOCATION, 'L');
		ENTITY_LETTER.put(EntityType.MEETING, 'M');
		ENTITY_LETTER.put(EntityType.ORGANIZATION, 'O');
		ENTITY_LETTER.put(EntityType.PERSON, 'P');
		ENTITY_LETTER.put(EntityType.PRODUCTION, 'Q');
	}
	
	/////////////////////////////////////////////////////////////////
	// TAB PANES		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Tab to represent a tool entities. */
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
		boolean editable = entities==references && editableReference;
		
		EntityEditorPanel panel = new EntityEditorPanel(this, text, linkedText, entities, references, params, modeState, switches, linkState, editable, name);
		if(fontSize!=null)
			panel.setFontSize(fontSize);
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
	private final static String MISC_POSITION = "MiscPosition";
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
			statusInformationLabel.setPreferredSize(new Dimension(100, 16));
			statusInformationLabel.setBorder(BorderFactory.createLoweredBevelBorder()); 
			statusBar.add(BorderLayout.CENTER,statusInformationLabel);
		}

//		statusBar.add(BorderLayout.CENTER, new JSeparator(SwingConstants.VERTICAL));
		
		// create position label
		{	statusPositionLabel = new JLabel(language.getText(MISC_POSITION),JLabel.LEFT);
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
	 * @param length
	 * 		New length of the displayed text,
	 * 		or {@code null} for no length at all.
	 */
	public void updateStatusPosition(Integer pos, Integer length)
	{	String text = language.getText(MISC_POSITION) + " ";
		if(pos==null)
			text = text + "...";
		else
			text = text + pos.toString();
		
		text = text + "/";
		if(length==null)
			text = text + "...";
		else
			text = text + length.toString();
		
		statusPositionLabel.setText(text);
	}
	
	/////////////////////////////////////////////////////////////////
	// TOOL BAR			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Button allowing to save the current article*/
	private JButton saveButton = null;
	/** Button allowing to load a different article */
	private JButton loadButton = null;
	/** Tool bar with the entity types buttons */
	private JPanel toolBar = null;
	/** Colored panels containing the entity-related buttons */
	private Map<EntityType,JPanel> entityPanels = null;
	/** Map of entity type view buttons */
	private Map<EntityType,JToggleButton> entityViewButtons = null;
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
	/** Button increasing the font size */
	private JButton largerFontButton = null;
	/** Button decreasing the font size */
	private JButton smallerFontButton = null;
		
	/**
	 * Creates and populates the tool bar.
	 * 
	 * @throws IOException
	 * 		Problem while loading the icons. 
	 */
	private void initToolBar() throws IOException
	{	int iconSize = 30;
		int buttonSize = 40;
		Dimension buttonDim = new Dimension(buttonSize,buttonSize);
		Dimension labelDim = new Dimension(70,20);
		
		// panel
		toolBar = new JPanel();
		toolBar.setBorder(BorderFactory.createRaisedBevelBorder());
		LayoutManager layout = new BoxLayout(toolBar, BoxLayout.LINE_AXIS);
//		LayoutManager layout = new FlowLayout(FlowLayout.LEFT);
		toolBar.setLayout(layout);
		//toolBar.setPreferredSize(new Dimension(100, 20));
		frame.getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

		// init file panel
	   {	JPanel filePanel = new JPanel();
			LayoutManager lay = new BoxLayout(filePanel, BoxLayout.PAGE_AXIS);
			filePanel.setLayout(lay);
			toolBar.add(filePanel);
	    	toolBar.add(Box.createHorizontalStrut(5));
			// save
			{	String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_SAVE;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				saveButton = new JButton(saveAction);
				saveButton.setToolTipText(language.getTooltip(ACTION_SAVE));
				saveButton.setText(null);
				saveButton.setIcon(new ImageIcon(img));
				saveButton.setMinimumSize(buttonDim);
				saveButton.setMaximumSize(buttonDim);
				saveButton.setPreferredSize(buttonDim);
				saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
				filePanel.add(saveButton);
			}
			//load
			{	String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_OPEN;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				loadButton = new JButton(loadAction);
				loadButton.setToolTipText(language.getTooltip(ACTION_LOAD));
				loadButton.setText(null);
				loadButton.setIcon(new ImageIcon(img));
				loadButton.setMinimumSize(buttonDim);
				loadButton.setMaximumSize(buttonDim);
				loadButton.setPreferredSize(buttonDim);
				loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
				filePanel.add(loadButton);
			}
		}
		
		// init entity panels
		entityPanels = new HashMap<EntityType, JPanel>();
		Map<EntityType, JPanel> innerEntityPanels = new HashMap<EntityType, JPanel>();
		for(EntityType type: EntityType.values())
		{	JPanel entPanel = new JPanel();
			LayoutManager lay = new BoxLayout(entPanel, BoxLayout.LINE_AXIS);
//			LayoutManager lay = new GridLayout(3,1);
			entPanel.setLayout(lay);
//			entPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			String name = language.getText(STR_ENTITY+StringTools.initialize(type.toString()));
//			entPanel.setBorder(BorderFactory.createTitledBorder(name));
			entPanel.setBorder(BorderFactory.createEtchedBorder());
			entPanel.setBackground(ENTITY_COLOR.get(type));
//			JLabel label = new JLabel(name,SwingConstants.CENTER);
			VerticalLabel label = new VerticalLabel(name, SwingConstants.CENTER);
			label.setRotation(VerticalLabel.ROTATE_LEFT);
			label.setMinimumSize(labelDim);
			label.setMaximumSize(labelDim);
			label.setPreferredSize(labelDim);
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			entPanel.add(label);
			JPanel panel = new JPanel();
			lay = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
			panel.setLayout(lay);
			panel.setBackground(null);
			entPanel.add(panel);
	    	innerEntityPanels.put(type,panel);
	    	entityPanels.put(type,entPanel);
	    	toolBar.add(entPanel);
	    	
	    	toolBar.add(Box.createHorizontalStrut(1));
//	    	JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
//	    	toolBar.add(separator);
//	    	toolBar.add(Box.createHorizontalStrut(5));
	    }
		
		// view entity types
		{	entityViewButtons = new HashMap<EntityType, JToggleButton>();
			// get the icon
			String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_SHOW;
			File iconFile = new File(iconPath);
			Image img = ImageIO.read(iconFile);
			img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
			// create the buttons
			for(EntityType type: EntityType.values())
			{	JPanel panel = innerEntityPanels.get(type);
				Action action = entityViewActions.get(type);
				JToggleButton button = new JToggleButton(action);
//				PaintableToggleButton button = new PaintableToggleButton(action);
//				String name = StringTools.initialize(type.toString());
//				String name = ENTITY_LETTER.get(type).toString();			
//				button.setText(name);
				button.setText(null);
				button.setIcon(new ImageIcon(img));
//				button.setBackground(ENTITY_COLOR.get(type));
				button.setMinimumSize(buttonDim);
				button.setMaximumSize(buttonDim);
				button.setPreferredSize(buttonDim);
				button.setAlignmentX(Component.CENTER_ALIGNMENT);
				entityViewButtons.put(type, button);
				panel.add(button);			
			}
		}
		
//		toolBar.add(Box.createHorizontalGlue());
//    	toolBar.add(Box.createHorizontalStrut(10));
//    	toolBar.add(new JSeparator(SwingConstants.VERTICAL));
//    	toolBar.add(Box.createHorizontalStrut(5));
		
		// center subpanel
    	JPanel centerPanel = new JPanel();
		{	LayoutManager lay = new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS);
//			LayoutManager lay = new GridLayout(3,1);
			centerPanel.setLayout(lay);
			toolBar.add(centerPanel);
//	    	toolBar.add(Box.createHorizontalStrut(10));
		}
		
		// mode
		{	ButtonGroup group = new ButtonGroup();
			JPanel panel = new JPanel();
			// setup panel
			{	LayoutManager lay = new BoxLayout(panel, BoxLayout.LINE_AXIS);
				panel.setLayout(lay);
				panel.setAlignmentX(Component.CENTER_ALIGNMENT);
				centerPanel.add(Box.createVerticalGlue());
				centerPanel.add(panel);
				centerPanel.add(Box.createVerticalGlue());
			}
			// display types
			{	modeTypesButton = new JRadioButton(modeTypesAction);
				modeTypesButton.setText(language.getText(ACTION_MODE_TYPES));
				modeTypesButton.setToolTipText(language.getTooltip(ACTION_MODE_TYPES));
				panel.add(modeTypesButton);
				group.add(modeTypesButton);
			}
			// display comparison
			{	modeCompButton = new JRadioButton(modeCompAction);
				modeCompButton.setText(language.getText(ACTION_MODE_COMP));
				modeCompButton.setToolTipText(language.getTooltip(ACTION_MODE_COMP));
				panel.add(modeCompButton);
				group.add(modeCompButton);
			}
		}
		
//		toolBar.add(Box.createHorizontalGlue());
		
		// edit entities
		{	entityInsertButtons = new HashMap<EntityType, JButton>();
			// get the icon
			String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_ADD;
			File iconFile = new File(iconPath);
			Image img = ImageIO.read(iconFile);
			img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
			// create the buttons
			for(EntityType type: EntityType.values())
			{	JPanel panel = innerEntityPanels.get(type);
				Action action = entityInsertActions.get(type);
				JButton button = new JButton(action);
//				Character name = ENTITY_LETTER.get(type);
//				button.setText(name.toString());
				button.setText(null);
				button.setIcon(new ImageIcon(img));
//				button.setBackground(ENTITY_COLOR.get(type));
				button.setMinimumSize(buttonDim);
				button.setMaximumSize(buttonDim);
				button.setPreferredSize(buttonDim);
				button.setAlignmentX(Component.CENTER_ALIGNMENT);
				entityInsertButtons.put(type, button);
				panel.add(button);			
			}
		}
		
		// remove entity
		{	// get the icon
			String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_REMOVE;
			File iconFile = new File(iconPath);
			Image img = ImageIO.read(iconFile);
			img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
			// create the button
			entityDeleteButton = new JButton(entityDeleteAction);
//			String name = ACTION_REMOVE.substring(0,1);
//			entityDeleteButton.setText(name);
			entityDeleteButton.setText(null);
			entityDeleteButton.setIcon(new ImageIcon(img));
			Dimension dim = new Dimension(155,buttonSize);
			entityDeleteButton.setMinimumSize(dim);
			entityDeleteButton.setMaximumSize(dim);
			entityDeleteButton.setPreferredSize(dim);
			entityDeleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			centerPanel.add(entityDeleteButton);
//			centerPanel.add(Box.createVerticalGlue());
//			centerPanel.add(Box.createVerticalStrut(2));
		}
		
//		toolBar.add(Box.createHorizontalGlue());
		
		// right subpanel
    	JPanel rightPanel = new JPanel();
		{	LayoutManager lay = new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS);
			rightPanel.setLayout(lay);
			toolBar.add(rightPanel);
//	    	toolBar.add(Box.createHorizontalStrut(10));
		}

		// browse
		{	JPanel panel = new JPanel();
			// setup panel
			{	LayoutManager lay = new BoxLayout(panel, BoxLayout.LINE_AXIS);
				panel.setLayout(lay);
				rightPanel.add(panel);
			}
			// previous article
			{	// get the icon
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_PREVIOUS;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				// create the button
				prevButton = new JButton(prevAction);
//				prevButton.setText("<");					//"\u25C0"
				prevButton.setText(null);
				prevButton.setIcon(new ImageIcon(img));
				prevButton.setMinimumSize(buttonDim);
				prevButton.setMaximumSize(buttonDim);
				prevButton.setPreferredSize(buttonDim);
				panel.add(prevButton);
			}
			// next article
			{	// get the icon
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_NEXT;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				// create the button
				nextButton = new JButton(nextAction);
//				nextButton.setText(">");					//"\u25B6"
				nextButton.setText(null);
				nextButton.setIcon(new ImageIcon(img));
				nextButton.setMinimumSize(buttonDim);
				nextButton.setMaximumSize(buttonDim);
				nextButton.setPreferredSize(buttonDim);
				panel.add(nextButton);
			}
		}
		
		// browse
		{	JPanel panel = new JPanel();
			// setup panel
			{	LayoutManager lay = new BoxLayout(panel, BoxLayout.LINE_AXIS);
				panel.setLayout(lay);
				rightPanel.add(panel);
			}
			// smaller font
			{	// get the icon
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_SMALLER;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				// create the button
				smallerFontButton = new JButton(smallerFontAction);
				smallerFontButton.setText(null);
				smallerFontButton.setIcon(new ImageIcon(img));
				smallerFontButton.setMinimumSize(buttonDim);
				smallerFontButton.setMaximumSize(buttonDim);
				smallerFontButton.setPreferredSize(buttonDim);
				panel.add(smallerFontButton);
			}
			// larger font
			{	// get the icon
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_LARGER;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				// create the button
				largerFontButton = new JButton(largerFontAction);
				largerFontButton.setText(null);
				largerFontButton.setIcon(new ImageIcon(img));
				largerFontButton.setMinimumSize(buttonDim);
				largerFontButton.setMaximumSize(buttonDim);
				largerFontButton.setPreferredSize(buttonDim);
				panel.add(largerFontButton);
			}
		}
		
		toolBar.add(Box.createHorizontalGlue());
	}
	
	/////////////////////////////////////////////////////////////////
	// MENU BAR			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for menu definition */
	private final static String MENU_FILE = "MenuFile";
	/** String used for menu definition */
	private final static String MENU_EDIT = "MenuEdit";
	/** String used for menu definition */
	private final static String MENU_VIEW = "MenuView";
	/** String used for menu definition */
	private final static String MENU_SETTINGS = "MenuSettings";
	/** String used for menu definition */
	private final static String MENU_HELP = "MenuHelp";
	/** String used for menu definition */
	private final static String MENU_LANGUAGE = "MenuLanguage";
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
	/** Menu item of the corpus selection action */
	private JMenuItem miCorpus;
	/** Menu item of the editor selection action */
	private JMenuItem miEditor;
	/** Menu item of the last corpus option action */
	private JCheckBoxMenuItem miLastCorpus;
	/** Menu item of the last article option action */
	private JCheckBoxMenuItem miLastArticle;
	/** Menu item of the edition switch */
	private JCheckBoxMenuItem miEditable;
	/** Menu item of the decrease font size action*/
	private JMenuItem miSmallerFont;
	/** Menu item of the increase font size action*/
	private JMenuItem miLargerFont;
	/** Menu item of the index action */
	private JMenuItem miIndex;
	/** Menu item of the about action */
	private JMenuItem miAbout;
	/** Menu item of the show links */
	private JCheckBoxMenuItem miLinks;
	/** Menu item of the language selection */
	private JMenu mLanguage;
	/** Menu items of the available languages */
	private List<JRadioButtonMenuItem> miLanguages;
	
	/**
	 * Initializes the menu bar of this editor.
	 * 
	 * @throws IOException
	 * 		Problem while loading the icons. 
	 */
	private void initMenuBar() throws IOException
	{	int iconSize = 13;
		menuBar = new JMenuBar();
		
		// file menu
		{	JMenu menu = new JMenu(language.getText(MENU_FILE));
			menuBar.add(menu);
			
			{	miOpen = new JMenuItem(loadAction);
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_OPEN;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				miOpen.setIcon(new ImageIcon(img));
				menu.add(miOpen);
			}
			
			{	miPrev = new JMenuItem(prevAction);
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_PREVIOUS;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				miPrev.setIcon(new ImageIcon(img));
				menu.add(miPrev);
			}
			
			{	miNext = new JMenuItem(nextAction);
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_NEXT;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				miNext.setIcon(new ImageIcon(img));
				menu.add(miNext);
			}
			
			menu.addSeparator();
			
			{	miSave = new JMenuItem(saveAction);
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_SAVE;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				miSave.setIcon(new ImageIcon(img));
				menu.add(miSave);
			}
			
			{	miSaveCopy = new JMenuItem(copyAction);
				menu.add(miSaveCopy);
			}
			
			menu.addSeparator();
			
			{	miClose = new JMenuItem(quitAction);
				menu.add(miClose);
			}
		}
		
		// edit menu
		{	JMenu menu = new JMenu(language.getText(MENU_EDIT));
			menuBar.add(menu);
			
			// insert entities
			Color from = Color.BLACK;
			for(EntityType type: EntityType.values())
			{	Action action = entityInsertActions.get(type);
				JMenuItem jmi = new JMenuItem(action);
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_ADD;
				File iconFile = new File(iconPath);
				BufferedImage img0 = ImageIO.read(iconFile);
				Color to = ENTITY_COLOR.get(type);
//				to = new Color((int)(to.getRed()/1.5),(int)(to.getGreen()/1.5),(int)(to.getBlue()/1.5));
				BufferedImageOp lookup = new LookupOp(new ColorMapper(from, to), null);
				img0 = lookup.filter(img0, null);
				Image img = img0.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				jmi.setIcon(new ImageIcon(img));
				menu.add(jmi);
			}
			
			menu.addSeparator();

			// remove entity
			{	miRemove = new JMenuItem(entityDeleteAction);
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_REMOVE;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				miRemove.setIcon(new ImageIcon(img));
				menu.add(miRemove);
			}
		}
		
		// view menu
		{	JMenu menu = new JMenu(language.getText(MENU_VIEW));
			menuBar.add(menu);
			
			// insert entities
			entityViewCheck = new HashMap<EntityType, JCheckBoxMenuItem>();
			Color from = Color.BLACK;
			for(EntityType type: EntityType.values())
			{	Action action = entityViewActions.get(type);
				JCheckBoxMenuItem  jmcbi = new JCheckBoxMenuItem(action);
//				jmcbi.setSelected(true);
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_SHOW;
				File iconFile = new File(iconPath);
				BufferedImage img0 = ImageIO.read(iconFile);
				Color to = ENTITY_COLOR.get(type);
//				to = new Color((int)(to.getRed()/1.5),(int)(to.getGreen()/1.5),(int)(to.getBlue()/1.5));
				BufferedImageOp lookup = new LookupOp(new ColorMapper(from, to), null);
				img0 = lookup.filter(img0, null);
				Image img = img0.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				jmcbi.setIcon(new ImageIcon(img));
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

			{	miLinks = new JCheckBoxMenuItem(showLinksAction);
				menu.add(miLinks);
			}
			
			menu.addSeparator();
			
			{	miLargerFont = new JMenuItem(largerFontAction);
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_LARGER;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				miLargerFont.setIcon(new ImageIcon(img));
				menu.add(miLargerFont);
			}

			{	miSmallerFont = new JMenuItem(smallerFontAction);
				String iconPath = FileNames.FO_IMAGES + File.separator + FileNames.FI_ICON_SMALLER;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				miSmallerFont.setIcon(new ImageIcon(img));
				menu.add(miSmallerFont);
			}
		}
		
		// settings menu
		{	JMenu menu = new JMenu(language.getText(MENU_SETTINGS));
			menuBar.add(menu);
			
			miCorpus = new JMenuItem(corpusAction);
			menu.add(miCorpus);
			
			miEditor = new JMenuItem(editorAction);
			menu.add(miEditor);
			
			menu.addSeparator();

			miLastCorpus = new JCheckBoxMenuItem(lastCorpusAction);
			menu.add(miLastCorpus);
			
			miLastArticle = new JCheckBoxMenuItem(lastArticleAction);
			menu.add(miLastArticle);
			
			menu.addSeparator();

			miEditable = new JCheckBoxMenuItem(editableAction);
			menu.add(miEditable);
			
			menu.addSeparator();
			
			mLanguage = new JMenu(language.getText(MENU_LANGUAGE));
			menu.add(mLanguage);
			miLanguages = new ArrayList<JRadioButtonMenuItem>();
			ButtonGroup group = new ButtonGroup();
			for(Action action: languageActions)
			{	JRadioButtonMenuItem mi = new JRadioButtonMenuItem(action);
				String languageName = (String) action.getValue(Action.NAME);
				String iconPath = FileNames.FO_IMAGES + File.separator + languageName.toLowerCase(Locale.ENGLISH) + FileNames.EX_PNG;
				File iconFile = new File(iconPath);
				Image img = ImageIO.read(iconFile);
				img = img.getScaledInstance(iconSize,iconSize,Image.SCALE_SMOOTH);
				mi.setIcon(new ImageIcon(img));
				mLanguage.add(mi);
				miLanguages.add(mi);
				group.add(mi);
			}
		}
		
		// about menu
		{	JMenu menu = new JMenu(language.getText(MENU_HELP));
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
	private final static String ACTION_DISPLAY = "ActionDisplay";
	/** Map of entity type view actions */
	private Map<EntityType, Action> entityViewActions = null;
	
	/**
	 * Initializes the actions related to
	 * the display of entity types.
	 */
	private void initEntityViewActions()
	{	entityViewActions = new HashMap<EntityType, Action>();
		for(EntityType type: EntityType.values())
		{	final EntityType t = type;
			String typeStr = language.getTooltip(STR_ENTITY+StringTools.initialize(type.toString()));
			String name = language.getText(ACTION_DISPLAY) + " " + typeStr;
			Action action = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	switchEntityView(t);
			    }
			};
			entityViewActions.put(type,action);
			int initial = ENTITY_LETTER.get(type);
			action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift "+((char)initial)));
			typeStr = language.getText(STR_ENTITY+StringTools.initialize(type.toString()));
			action.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_DISPLAY)+" "+typeStr);
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
	{	JToggleButton button = entityViewButtons.get(type);
//		JCheckBoxMenuItem item = entityViewCheck.get(type);
//		Action action = entityViewActions.get(type);
//		boolean state = (Boolean)action.getValue(Action.SELECTED_KEY);
		boolean state = button.isSelected();

		// update tabpanes
		int count = tabbedPane.getComponentCount();
		for(int i=0;i<count;i++)
		{	EntityEditorPanel panel = (EntityEditorPanel)tabbedPane.getComponentAt(i);
			panel.switchType(type);
			Action action = entityInsertActions.get(type);
			action.setEnabled(state);
		}
		
		// update toolbar (color)
		JPanel panel = entityPanels.get(type);
		Color color = ENTITY_COLOR.get(type);
		if(state)
			panel.setBackground(color);
		else
		{	int r = Math.min(color.getRed()/2,255);
			int g = Math.min(color.getGreen()/2,255);
			int b = Math.min(color.getBlue()/2,255);
			Color c = new Color(r,g,b);
			panel.setBackground(c);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// ENTITY EDITION	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for action definition */
	private final static String ACTION_INSERT = "ActionInsert";
	/** Map of entity type insert actions */
	private Map<EntityType, Action> entityInsertActions = null;
	/** String used for action definition */
	private final static  String ACTION_REMOVE = "ActionRemove";
	/** Action allowing to delete an entity */
	private Action entityDeleteAction = null;

	/**
	 * Initializes actions related to the
	 * edition of entities in the reference file.
	 */
	private void initEntityEditionActions()
	{	// remove entity
		{	String name = language.getText(ACTION_REMOVE);
			entityDeleteAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent e)
				{	removeEntity();
			    }
			};
			entityDeleteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt R"));
			entityDeleteAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_REMOVE));
			entityDeleteAction.setEnabled(false);
		}
		
		// insert entities
		entityInsertActions = new HashMap<EntityType, Action>();
		for(EntityType type: EntityType.values())
		{	final EntityType t = type;
			String typeStr = language.getText(STR_ENTITY+StringTools.initialize(type.toString()));
			String name = language.getText(ACTION_INSERT) + " " + typeStr;
			Action action = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent e)
				{	insertEntity(t);
			    }
			};
			entityInsertActions.put(type, action);
			char initial = ENTITY_LETTER.get(type);
			action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt "+initial));
			action.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_INSERT)+" "+typeStr);
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
	private final static String ACTION_MODE_TYPES = "ActionModeTypes";
	/** String used for action definition */
	private final static String ACTION_MODE_COMP = "ActionModeComp";
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
		{	String name = language.getText(ACTION_MODE_TYPES);
			modeTypesAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	switchDisplayMode(true);
			    }
			};
			modeTypesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift T"));
			modeTypesAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_MODE_TYPES));
			modeTypesAction.putValue(Action.SELECTED_KEY, true);
		}
		
		// display comparisons
		{	String name = language.getText(ACTION_MODE_COMP);
			modeCompAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	switchDisplayMode(false);
			    }
			};
			modeCompAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift C"));
			modeCompAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_MODE_COMP));
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
	/** String used for action definition */
	private final static String ACTION_LINKS = "ActionLinks";
	/** Action showing/hidding hyperlinks */
	private Action showLinksAction = null;
	
	/**
	 * Initializes actions related to 
	 * hyperlinks.
	 */
	private void initLinksActions()
	{	String name = language.getText(ACTION_LINKS);
		showLinksAction = new AbstractAction(name)
		{	/** Class id */
			private static final long serialVersionUID = 1L;
			
			@Override
		    public void actionPerformed(ActionEvent evt)
			{	showLinks();
		    }
		};
		showLinksAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift H"));
		showLinksAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_LINKS));
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
	// FONT SIZE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Current font size */
	private Integer fontSize = null;
	/** String used for action definition */
	private final static String ACTION_LARGER = "ActionLarger";
	/** Action increasing the font size */
	private Action largerFontAction = null;
	/** String used for action definition */
	private final static String ACTION_SMALLER = "ActionSmaller";
	/** Action decreasing the font size */
	private Action smallerFontAction = null;
	
	/**
	 * Initializes actions related to 
	 * font size.
	 */
	private void initFontActions()
	{	// smaller font
		{	String name = language.getText(ACTION_SMALLER);
			smallerFontAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	changeFontSize(-1);
			    }
			};
			smallerFontAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control SUBTRACT"));
			smallerFontAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_SMALLER));
		}
		
		// larger font
		{	String name = language.getText(ACTION_LARGER);
			largerFontAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	changeFontSize(+1);
			    }
			};
			largerFontAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control ADD"));
			largerFontAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_LARGER));
		}
	}
	
	/**
	 * Modifies the size of the font used to display the text.
	 * 
	 * @param delta
	 * 		How much to decrease/increase the font.
	 */
	private void changeFontSize(int delta)
	{	for(Component c:tabbedPane.getComponents())
		{	EntityEditorPanel panel = (EntityEditorPanel) c;
			panel.changeFontSize(delta);
			fontSize = panel.getFontSize();
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// BROWSE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for action definition */
	private final static String ACTION_PREV = "ActionPrevious";
	/** Action giving access to the previous article in the folder */
	private Action prevAction = null;
	/** String used for action definition */
	private final static String ACTION_NEXT = "ActionNext";
	/** Action giving access to the next article in the folder */
	private Action nextAction = null;
	
	/**
	 * Initializes actions related to 
	 * browing articles in the output folder.
	 */
	private void initBrowseActions()
	{	// previous article
		{	String name = language.getText(ACTION_PREV);
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
			prevAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
			prevAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_PREV));
		}
		
		// next article
		{	String name = language.getText(ACTION_NEXT);
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
			nextAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
			nextAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_NEXT));
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE ACCESS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Dialog title */
	private final static String DIALOG_PROPOSE_SAVING = "DialogProposeSaving";
	/** String used for action definition */
	private final static String ACTION_LOAD = "ActionLoad";
	/** Action allowing loading an existing set of files */
	private Action loadAction = null;
	/** String used for action definition */
	private final static String ACTION_SAVE = "ActionSave";
	/** Action allowing recording the current reference file */
	private Action saveAction = null;
	/** String used for action definition */
	private final static String ACTION_COPY = "ActionCopy";
	/** Action allowing recording a copy of the current reference file */
	private Action copyAction = null;
	
	/**
	 * Initializes the actions related
	 * to file loading and saving.
	 */
	private void initFileActions()
	{	// open
		{	String name = language.getText(ACTION_LOAD);
			loadAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	loadArticle();
			    }
			};
			loadAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
			loadAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_LOAD));
		}

		// save
		{	String name = language.getText(ACTION_SAVE);
			saveAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	saveAll();
			    }
			};
			saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
			saveAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_SAVE));
			saveAction.setEnabled(false);
		}
		
		// save copy
		{	String name = language.getText(ACTION_COPY);
			copyAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	saveReferenceCopy();
			    }
			};
			String initial = name.substring(0,1);
			copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift S"));
			copyAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_COPY));
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
			String pathStr = corpusFolder + File.separator + name;
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
	 * Lets the user record modified reference entities before loading
	 * another article or quitting the application.
	 * <br/>
	 * The method returns a boolean indicating if the action was canceled 
	 * or not.
	 *
	 * @return
	 * 		{@code false} iff the action was canceled.
	 */
	private boolean proposeSaving()
	{	boolean result = true;
		int answer = JOptionPane.showConfirmDialog(
			frame, 
			language.getTooltip(DIALOG_PROPOSE_SAVING), 
			language.getText(DIALOG_PROPOSE_SAVING), 
			JOptionPane.YES_NO_CANCEL_OPTION
		);
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
	private final static String ACTION_QUIT = "ActionQuit";
	/** Action letting the user close the application */
	private Action quitAction = null;

	/**
	 * Initializes actions related
	 * to quitting the application.
	 */
	private void initQuitActions()
	{	String name = language.getText(ACTION_QUIT);
		quitAction = new AbstractAction(name)
		{	/** Class id */
			private static final long serialVersionUID = 1L;
			
			@Override
		    public void actionPerformed(ActionEvent evt)
			{	closeWindow();
		    }
		};
		quitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
		quitAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_QUIT)+" "+APP_NAME);
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
		{	// update configuration file
			try
			{	recordSettings();
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
			
			// close the application
			frame.dispose();
			System.exit(0);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// SETTINGS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String used for action definition */
	private final static String ACTION_CORPUS = "ActionCorpus";
	/** Action triggering the corpus dialog */
	private Action corpusAction = null;
	/** String used for action definition */
	private final static String ACTION_EDITOR = "ActionEditor";
	/** Action triggering the editor dialog */
	private Action editorAction = null;
	/** String used for action definition */
	private final static String ACTION_LAST_CORPUS = "ActionLastCorpus";
	/** Action switching the last corpus option */
	private Action lastCorpusAction = null;
	/** String used for action definition */
	private final static String ACTION_LAST_ARTICLE = "ActionLastArticle";
	/** Action switching the last article option */
	private Action lastArticleAction = null;
	/** String used for action definition */
	private final static String ACTION_EDITABLE = "ActionEditable";
	/** Action switching the last article option */
	private Action editableAction = null;
	/** Action switching the last article option */
	private List<Action> languageActions = null;
	
	/**
	 * Initializes actions related to the settings.
	 */
	private void initSettingsActions()
	{	// corpus
		{	String name = language.getText(ACTION_CORPUS);
			corpusAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	changeCorpusFolder();
			    }
			};
			corpusAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_CORPUS));
		}
		
		// editor
		{	String name = language.getText(ACTION_EDITOR);
			editorAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	changeEditorName();
			    }
			};
			editorAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_EDITOR));
		}
		
		// last corpus
		{	String name = language.getText(ACTION_LAST_CORPUS);
			lastCorpusAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	switchLastCorpusOption();
			    }
			};
			lastCorpusAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_LAST_CORPUS));
			lastCorpusAction.putValue(Action.SELECTED_KEY, useLastCorpus);
		}
		
		
		// last article
		{	String name = language.getText(ACTION_LAST_ARTICLE);
			lastArticleAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	switchLastArticleOption();
			    }
			};
			lastArticleAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_LAST_ARTICLE));
			lastArticleAction.putValue(Action.SELECTED_KEY, useLastArticle);
		}
		
		// editable reference
		{	String name = language.getText(ACTION_EDITABLE);
			editableAction = new AbstractAction(name)
			{	/** Class id */
				private static final long serialVersionUID = 1L;
				
				@Override
			    public void actionPerformed(ActionEvent evt)
				{	switchEditableOption();
			    }
			};
			editableAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_EDITABLE));
			editableAction.putValue(Action.SELECTED_KEY, editableReference);
		}
		
		// language
		{	languageActions = new ArrayList<Action>();
			String langPath = FileNames.FO_LANGUAGE;
			List<File> files = FileTools.getFilesEndingWith(langPath, FileNames.EX_XML);
			for(File file: files)
			{	String name0 = file.getName();
				name0 = name0.substring(0, name0.lastIndexOf('.'));// removing extension
				final String name = StringTools.initialize(name0);
				Action action = new AbstractAction(name)
				{	/** Class id */
					private static final long serialVersionUID = 1L;
					
					@Override
				    public void actionPerformed(ActionEvent evt)
					{	changeLanguage(name);
				    }
				};
				action.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(MENU_LANGUAGE)+" "+name);
				action.putValue(Action.SELECTED_KEY, name.equals(languageName));
				languageActions.add(action);
			}
		}
	}
	
	/**
	 * Changes the value of the {@link #useLastCorpus} switch.
	 */
	private void switchLastCorpusOption()
	{	useLastCorpus = !useLastCorpus;
	}
	
	/**
	 * Changes the value of the {@link #useLastArticle} switch.
	 */
	private void switchLastArticleOption()
	{	useLastArticle = !useLastArticle;
	}
	
	/**
	 * Changes the value of the {@link #editableReference} switch.
	 */
	private void switchEditableOption()
	{	editableReference = !editableReference;
		if(tabbedPane.getTabCount()>0)
		{	EntityEditorPanel eep = (EntityEditorPanel) tabbedPane.getComponentAt(0);
			eep.setEditable(editableReference);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// INFORMATION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Dialog displayed for the action "index" */
	private TextDialog indexDialog = null;
	/** String used for action definition */
	private final static String ACTION_INDEX = "ActionIndex";
	/** String used as the "index" dialog title */
	private final static String DIALOG_INDEX = "DialogIndex";
	/** Action triggering the 'index' dialog */
	private Action indexAction = null;
	/** String used for action definition */
	private final static String ACTION_ABOUT = "ActionAbout";
	/** String used as the "index" dialog title */
	private final static String DIALOG_ABOUT = "DialogAbout";
	/** Action triggering the 'about' dialog */
	private Action aboutAction = null;

	/**
	 * Initializes actions related
	 * to help.
	 */
	private void initInformationActions()
	{	// index
		{	String name = language.getText(ACTION_INDEX);
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
			indexAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_INDEX)+" "+APP_NAME);
		}
		
		// about
		{	String name = language.getText(ACTION_ABOUT);
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
			aboutAction.putValue(Action.SHORT_DESCRIPTION, language.getTooltip(ACTION_ABOUT)+" "+APP_NAME);
		}
	}
	
	/**
	 * Displays the help index dialog.
	 * Its content is retrieved from
	 * a file containing html source code.
	 */
	private void displayHelpIndex()
	{	try
		{	//indexDialog = new TextDialog(frame, language.getText(DIALOG_INDEX)+" "+APP_NAME, FileNames.FI_HELP_PAGE); //TODO not written yet
			String htmlPath = FileNames.FO_LANGUAGE + File.separator + language.getName().toLowerCase() + FileNames.EX_HTML; 
			indexDialog = new TextDialog(
				frame, 
				language.getText(DIALOG_INDEX)+" "+APP_NAME,
				language.getTooltip(DIALOG_INDEX),
				htmlPath//"README.md"
			);
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
			+ "Université d'Avignon<br/>"
			+ "Laboratoire Informatique d'Avignon (LIA)<br/>"
			+ "<a href=\"http://lia.univ-avignon.fr\">http://lia.univ-avignon.fr</a><br/>"
			+ "(c) Yasa Akbulut 2011 (<i>Annotation Viewer</i>)<br/>"
			+ "(c) Vincent Labatut 2013-15"
			+ "</html>";
		JOptionPane.showMessageDialog(
			frame, 
			string,
			language.getText(DIALOG_ABOUT)+" "+APP_NAME,
			JOptionPane.INFORMATION_MESSAGE
		);
	}
	
	/////////////////////////////////////////////////////////////////
	// FILE CHOOSER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Title of a dialog box */
	private final static String DIALOG_ARTICLE_CHOOSER = "DialogArticleChooser";
	/** Component used to select the article to open */
	private JFileChooser articleChooser;
	/** Title of a dialog box */
	private final static String DIALOG_REFERENCE_CHOOSER = "DialogReferenceChooser";
	/** Component used to save the reference */
	private JFileChooser referenceChooser;
	/** Title of a dialog box */
	private final static String DIALOG_CORPUS_CHOOSER = "DialogCorpusChooser";
	/** Component used to select the corpus main folder */
	private JFileChooser corpusChooser;
	/** Indicates if the reference was modified since the last save: 0=no, 1=ref entities, 2=text */
	private int changed = 0;
	
	/**
	 * Creates and initializes the file chooser.
	 */
	private void initFileChooser()
	{	articleChooser = new JFileChooser(corpusFolder);
		articleChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		articleChooser.setDialogTitle(language.getText(DIALOG_ARTICLE_CHOOSER));
		
		referenceChooser = new JFileChooser(corpusFolder);
		referenceChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		referenceChooser.setDialogTitle(language.getText(DIALOG_REFERENCE_CHOOSER));
		
		corpusChooser = new JFileChooser(corpusFolder);
		corpusChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		corpusChooser.setDialogTitle(language.getText(DIALOG_CORPUS_CHOOSER));
	}
	
	/**
	 * Updates the saved indicator and the corresponding GUI elements.
	 * <br/>
	 * The value 0 means there is no change anymore (we probably've 
	 * just recorded them); the value 1 means only reference entities 
	 * were modified; and the value 2 means the text was modified.  
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
	// LANGUAGE				/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Dialog box title */
	private final static String DIALOG_LANGUAGE_ERROR = "DialogLanguageError";
	/** Dialog box content */
	private final static String DIALOG_LANGUAGE_ERROR_END = "DialogLanguageErrorEnd";
	/** Dialog box title */
	private final static String DIALOG_RESTART = "DialogRestart";
	/** Name of the GUI language */
	private String languageName = "english";
	/** Language of the GUI */
	private Language language = null;
	
	/**
	 * Changes the current GUI language.
	 * 
	 * @param languageName
	 * 		New language.
	 */
	private void changeLanguage(String languageName)
	{	if(!languageName.equals(this.languageName))
		{	Language newLang = null;
			// try to load the new language
			try
			{	newLang = LanguageLoader.loadLanguage(languageName);
			}
			catch (Exception e)
			{	String msg = e.getMessage();
				String langName = StringTools.initialize(languageName);
				String string = "<html>"
					+ language.getTooltip(DIALOG_LANGUAGE_ERROR)+" "+langName+":<br/>"
					+ msg + "<br/>"
					+ language.getText(DIALOG_LANGUAGE_ERROR_END)
					+ "</html>";
				JOptionPane.showMessageDialog(
					frame, 
					string, 
					language.getText(DIALOG_LANGUAGE_ERROR), 
					JOptionPane.ERROR_MESSAGE
				);
			}
			
			// if the language could be loaded
			if(newLang!=null)
			{	// ask to restart
				JOptionPane.showMessageDialog(
					frame, 
					language.getTooltip(DIALOG_RESTART), 
					language.getText(DIALOG_RESTART), 
					JOptionPane.WARNING_MESSAGE
				);
				
				// set up the new language
				language = newLang;
				this.languageName = languageName;
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// SETTINGS VALUES		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Dialog box title */
	private final static String DIALOG_CORPUS_ERROR = "DialogCorpusError";
	/** Dialog box title */
	private final static String DIALOG_CHANGE_EDITOR = "DialogChangeEditor";
	/** Dialog box text */
	private final static String DIALOG_FIRST_LAUNCH = "DialogFirstLaunch";
	/** Dialog box text */
	private final static String DIALOG_FIRST_LAUNCH_ASKED = "DialogFirstLaunchAsked";
	/** Dialog box text */
	private final static String DIALOG_FIRST_LAUNCH_NAME = "DialogFirstLaunchName";
	/** Dialog box text */
	private final static String DIALOG_FIRST_LAUNCH_FOLDER = "DialogFirstLaunchFolder";
	/** Dialog box text */
	private final static String DIALOG_FIRST_LAUNCH_WARNING = "DialogFirstLaunchWarning";
	/** Dialog box text */
	private final static String DIALOG_FIRST_LAUNCH_MAIN = "DialogFirstLaunchMain";
	/** Dialog box text */
	private final static String DIALOG_SET_CORPUS = "DialogSetCorpus";
	/** Dialog box text */
	private final static String DIALOG_SET_CORPUS_SELECT = "DialogSetCorpusSelect";

	/** Full path of the configuration file */
	public final static String CONFIG_PATH = System.getProperty("user.home") + File.separator + FileNames.FI_CONFIGURATION;
//	private final static String CONFIG_PATH = FileNames.FO_MISC + File.separator + FileNames.FI_CONFIGURATION;
	/** Main folder containing the whole corpus */
	private String corpusFolder = null;
	/** Name of the person currently annotating the articles */
	private String currentEditor = "N/A";
	/** Whether or not to use the last loaded corpus */
	private boolean useLastCorpus = true;
	/** Whether or not to use the last loaded article */
	private boolean useLastArticle = true;
	/** Whether or not the user can edit the reference text */
	private boolean editableReference = false;
	/** Whether or not the corpus folder should be set by the user */
	private boolean mustSetCorpus = false;
	
	/**
	 * Changes the folder containing the whole corpus.
	 */
	private void changeCorpusFolder()
	{	int returnVal = corpusChooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{	File file = corpusChooser.getSelectedFile();
			try
			{	// set up the new folder
				String tempFolder = file.getCanonicalPath();
				
				// possibly switch to its first article (if there's one)
				File cf = new File(tempFolder);
				ArticleList articles = ArticleLists.getArticleList(cf);
				if(!articles.isEmpty())
				{	corpusFolder = tempFolder;
					File article = articles.get(0);
					String articleName = article.getName();
					String articlePath = corpusFolder + File.separator + articleName;
					setArticle(articlePath);
				}
				
				// otherwise, display an error message (=no corpus)
				else
				{	corpusFolder = tempFolder;
					JOptionPane.showMessageDialog(
						frame, 
						language.getTooltip(DIALOG_CORPUS_ERROR), 
						language.getText(DIALOG_CORPUS_ERROR), 
						JOptionPane.ERROR_MESSAGE
					);
				}
				
				cf = new File(corpusFolder);
				corpusChooser.setCurrentDirectory(cf);
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
	 * Changes the name of the current editor.
	 */
	private void changeEditorName()
	{	String answer = (String)JOptionPane.showInputDialog(frame,
				language.getTooltip(DIALOG_CHANGE_EDITOR),
				language.getText(DIALOG_CHANGE_EDITOR),
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                currentEditor);
		if(answer!=null && !answer.isEmpty())
			currentEditor = answer;
	}

	/**
	 * Record the editor settings in the
	 * appropriate XML file.
	 * 
	 * @throws IOException 
	 * 		Problem while recording the configuration file. 
	 */
	private void recordSettings() throws IOException
	{	// check folder
		File folder = new File(FileNames.FO_MISC);
		if(!folder.exists())
			folder.mkdirs();
		// schema file
		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_CONFIGURATION_SCHEMA;
		File schemaFile = new File(schemaPath);
		
		// build xml document
		Element root = new Element(XmlNames.ELT_CONFIGURATION);

		// use
		{	Element useElt = new Element(XmlNames.ELT_USE);
			String useLastCorpusStr = Boolean.toString(useLastCorpus);
			useElt.setAttribute(XmlNames.ATT_CORPUS, useLastCorpusStr);
			String useLastArticleStr = Boolean.toString(useLastArticle);
			useElt.setAttribute(XmlNames.ATT_ARTICLE, useLastArticleStr);
			root.addContent(useElt);
		}
		
		// last
		{	Element lastElt = new Element(XmlNames.ELT_LAST);
			Element corpusElt = new Element(XmlNames.ELT_CORPUS);
			corpusElt.setText(corpusFolder);
			lastElt.addContent(corpusElt);
			if(currentArticle!=null)
			{	Element articleElt = new Element(XmlNames.ELT_ARTICLE);
				articleElt.setText(currentArticle);
				lastElt.addContent(articleElt);
			}
			root.addContent(lastElt);
		}
		
		// editor
		if(currentEditor!=null)
		{	Element editorElt = new Element(XmlNames.ELT_EDITOR);
			editorElt.setText(currentEditor);
			root.addContent(editorElt);
		}
		
		// text
		{	Element textElt = new Element(XmlNames.ELT_TEXT);
			if(fontSize!=null)
				textElt.setAttribute(XmlNames.ATT_FONT_SIZE,fontSize.toString());
			textElt.setAttribute(XmlNames.ATT_EDITABLE,Boolean.toString(editableReference));
			root.addContent(textElt);
		}
		
		// language
		{	Element languageElt = new Element(XmlNames.ELT_LANGUAGE);
			languageElt.setText(languageName);
			root.addContent(languageElt);
		}
		
		// record file
		File configFile = new File(CONFIG_PATH);
		XmlTools.makeFileFromRoot(configFile,schemaFile,root);
	}
	
	/**
	 * Retrieves the configuration file content,
	 * and initializes the corpus folder.
	 * 
	 * @return
	 * 		Path of the first article to load (for later).
	 * 
	 * @throws IOException
	 * 		Problem while loading the configuration file. 
	 * @throws SAXException 
	 * 		Problem while loading the configuration file. 
	 * @throws ParserConfigurationException 
	 * 		Problem while loading the language file. 
	 */
	private String retrieveSettings() throws SAXException, IOException, ParserConfigurationException
	{	Locale.setDefault(Locale.ENGLISH);
		// get the predefined folder
		corpusFolder = FileNames.FO_OUTPUT; 
//		corpusFolder = System.getProperty("user.home");
		String articlePath = "";
		
		// schema file
		String schemaPath = FileNames.FO_SCHEMA+File.separator+FileNames.FI_CONFIGURATION_SCHEMA;
		File schemaFile = new File(schemaPath);
	
		// load file
		File configFile = new File(CONFIG_PATH);
		if(configFile.exists())
		{	Element root = XmlTools.getRootFromFile(configFile,schemaFile);
			
			// use
			{	Element useElt = root.getChild(XmlNames.ELT_USE);
				String useLastCorpusStr = useElt.getAttributeValue(XmlNames.ATT_CORPUS);
				useLastCorpus = Boolean.parseBoolean(useLastCorpusStr);
				String useLastArticleStr = useElt.getAttributeValue(XmlNames.ATT_ARTICLE);
				useLastArticle = Boolean.parseBoolean(useLastArticleStr);
			}
		
			// last
			{	Element lastElt = root.getChild(XmlNames.ELT_LAST);
				if(lastElt!=null)
				{	if(useLastCorpus)
					{	Element corpusElt = lastElt.getChild(XmlNames.ELT_CORPUS);
						corpusFolder = corpusElt.getValue().trim();
					}
					if(useLastArticle)
					{	Element articleElt = lastElt.getChild(XmlNames.ELT_ARTICLE);
						if(articleElt!=null)
						{	articlePath = articleElt.getValue().trim();
							File f1 = new File(corpusFolder);
							File f2 = new File(articlePath);
							if(!f2.getParentFile().equals(f1))
								articlePath = "";
						}
					}
				}
			}
			
			// editor
			{	Element editorElt = root.getChild(XmlNames.ELT_EDITOR);
				if(editorElt!=null)
					currentEditor = editorElt.getValue().trim();
			}
			
			// font size
			{	Element textElt = root.getChild(XmlNames.ELT_TEXT);
				String fontSizeStr = textElt.getAttributeValue(XmlNames.ATT_FONT_SIZE);
				if(fontSizeStr!=null)
					fontSize = Integer.parseInt(fontSizeStr);
				String editableReferenceStr = textElt.getAttributeValue(XmlNames.ATT_EDITABLE);
				editableReference = Boolean.parseBoolean(editableReferenceStr);
			}
			
			// language
			{	Element languageElt = root.getChild(XmlNames.ELT_LANGUAGE);
				if(languageElt!=null)
					languageName = languageElt.getValue().trim();
				language = LanguageLoader.loadLanguage(languageName);
			}
		}
		
		// check if the corpus folder exists
		File cff = new File(corpusFolder);
		if(!cff.exists())
		{	corpusFolder = System.getProperty("user.home");
			articlePath = "";
		}
		else if(!articlePath.isEmpty())
		{	cff = new File(articlePath);
			if(!cff.exists())
				articlePath = "";
		}
		if(articlePath.isEmpty())
		{	ArticleList articles = ArticleLists.getArticleList(new File(corpusFolder));
			if(articles!=null && !articles.isEmpty())
			{	File article = articles.get(0);
				String articleName = article.getName();
				articlePath = corpusFolder + File.separator + articleName;
			}
		}
		
		return articlePath;
	}
	
	/**
	 * Forces the user to set up the settings for the first launch.
	 */
	public void doFirstLaunch()
	{	String msg = "<html>"
			+ language.getTooltip(DIALOG_FIRST_LAUNCH)+" "+TITLE+".<hr/>"
			+ language.getText(DIALOG_FIRST_LAUNCH_ASKED)
			+ "<ol><li>"+language.getText(DIALOG_FIRST_LAUNCH_NAME)+"</li>"
			+ "<li>"+language.getText(DIALOG_FIRST_LAUNCH_FOLDER)+" "
			+ "(<u>"+language.getText(DIALOG_FIRST_LAUNCH_WARNING)+"</u> "
			+ language.getText(DIALOG_FIRST_LAUNCH_MAIN)+")</li></ol>"
			+ "</html>";
		JOptionPane.showMessageDialog(
			frame,
			msg,
			language.getText(DIALOG_FIRST_LAUNCH),
			JOptionPane.WARNING_MESSAGE
		);
		
		changeEditorName();
		changeCorpusFolder();
	}
	
	/**
	 * Forces the user to set up the settings for the first launch.
	 */
	public void doSetCorpus()
	{	String msg = "<html>"
			+ language.getTooltip(DIALOG_SET_CORPUS)+"<hr/>"
			+ language.getText(DIALOG_SET_CORPUS_SELECT)+"<br/>"
			+ "<u>"+language.getText(DIALOG_FIRST_LAUNCH_WARNING)+"</u> "
			+ language.getText(DIALOG_FIRST_LAUNCH_MAIN)
			+ "</html>";
		JOptionPane.showMessageDialog(
			frame, 
			msg,
			language.getText(DIALOG_SET_CORPUS),
			JOptionPane.WARNING_MESSAGE
		);
		
		changeCorpusFolder();
	}
	
	/**
	 * Indicates if the corpus folder
	 * should be set by the user.
	 * 
	 * @return
	 * 		{@code true} iff the corpus folder must be set.
	 */
	public boolean getMustSetCorpus()
	{	return mustSetCorpus;
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
		File cf = new File(corpusFolder);
		ArticleList folders = ArticleLists.getArticleList(cf);
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
			File cf = new File(corpusFolder);
			ArticleList articles = ArticleLists.getArticleList(cf);
			File f = new File(currentArticle);
			currentIndex = articles.indexOf(f);
		
			// retrieve texts
			File rawFile = new File(articlePath + File.separator + FileNames.FI_RAW_TEXT);
			currentRawText = FileTools.readTextFile(rawFile);
			File linkedFile = new File(articlePath + File.separator + FileNames.FI_LINKED_TEXT);
			if(linkedFile.exists())
				currentLinkedText = FileTools.readTextFile(linkedFile);
			else
				currentLinkedText = FileTools.readTextFile(rawFile);
			
			// get article name
			File temp = new File(articlePath);
			articleName = temp.getName();
			
			// update position
			updateStatusPosition(currentRawText.length(),currentRawText.length());
			
			// get entity files
			Map<String,File> entityFiles = getEntityFiles(articlePath);
			
			// open them to get the entities
			Map<String,Entities> entityLists = getEntityLists(entityFiles);
			
			// clear existing article
			String selectedTab = this.selectedTab;
			tabbedPane.removeChangeListener(this);
			tabbedPane.removeAll();
			tabbedPane.addChangeListener(this);
			statusInformationTexts.clear();
			
			// put them in a tab
			String refName = RecognizerName.REFERENCE.toString();
			Entities references = entityLists.get(refName);
			if(references==null)
			{	references = new Entities(currentEditor);
				updateSaved(1);
				updateTitle();
			}
			else
			{	entityLists.remove(refName);
				if(references.getEntities().isEmpty())
					references.setEditor(currentEditor);
			}
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
			
			// update title
			String editorStr ="[edt: " + currentEditor;
			String refEdt = references.getEditor();
			if(refEdt!=null && !refEdt.equals(currentEditor))
				editorStr = editorStr + " / " + refEdt;
			editorStr = editorStr + "]";
			frame.setTitle(TITLE+" " + editorStr + " - " + articleName + " " + (currentIndex+1) + "/" + articles.size());
			
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
		
		// update status bar
		updateStatusPosition(pos,currentRawText.length());
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
		
		// update status bar
		updateStatusPosition(start,currentRawText.length());
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

// TODO fixer les raccourcis clavier (pas utiliser l'initiale de la commande)
// TODO recherche guigui
// TODO corriger script mac >> voir msg sur ordi fred

// TODO rajouter les références biblio
// TODO faire mes propres annotations
// TODO compléter le guide avec des exemples
/**
 * La droite du PS >> PS = organisation
 * député de haute vienne >> tout, fonction
 * fils de job >> fonction, personne de référence pas annotée
 * député maire émile labruissière >> tout=personne + fonction dedans
 * décorations = productions
 * "congrès de 1948" = meeting 
 * "Fils de Louis-Albert Baurens et de Marie-Louise Mauret, Alexandre Baurens épousa, en février 1926, Georgette Bessagnet," fils de ?
 * que faire des durées ?
*/
