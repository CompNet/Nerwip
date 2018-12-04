package fr.univavignon.search;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.Scanner;
import java.util.TreeSet;

import jsat.DataSet;
import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.clustering.PAM;
import jsat.linear.DenseVector;
import jsat.linear.Vec;
import jsat.linear.distancemetrics.DistanceMetric;
import jsat.linear.distancemetrics.MahalanobisDistance;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.google.api.services.customsearch.model.Result;
import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.retrieval.AbstractArticleReader;
import fr.univavignon.retrieval.ArticleRetriever;
import fr.univavignon.search.clustering.MyPam;
import fr.univavignon.search.engines.social.AbstractSocialEngine;
import fr.univavignon.search.engines.web.AbstractWebEngine;
import fr.univavignon.search.engines.web.GoogleEngine;
import fr.univavignon.search.clustering.DummyDistanceMetric;
import fr.univavignon.search.results.AbstractSearchResults;
import fr.univavignon.search.Searcher;
import fr.univavignon.search.tools.files.SearchFileNames;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;
import fr.univavignon.tools.strings.StringTools;

/**
 * This class is used to launch some processes
 * testing the various features of the software.
 * 
 * @author Vincent Labatut
 */
@SuppressWarnings({ "unused" })
public class SearchTest
{	/**
	 * Basic main function, launches the
	 * required test. Designed to be modified
	 * and launched from Eclipse, no command-line
	 * options.
	 * 
	 * @param args
	 * 		None needed.
	 * 
	 * @throws Exception
	 * 		Something went wrong... 
	 */
	public static void main(String[] args) throws Exception
	{	
		// retrieval
//		testRetrievalGeneric();
//		testBoilerPipe();
		
		// search
//		testGoogleSearch();
		
		// whole process
		testExtractor();
		
		// compare searches
			// hidalgo
//			compareSearches("Anne_Hidalgo_1", "Anne_Hidalgo_2");
//			compareSearches("Anne_Hidalgo_2", "Anne_Hidalgo_2ext");
//			compareSearches("Anne_Hidalgo_2", "Anne_Hidalgo_www.leparisien.fr");
//			compareSearches("Anne_Hidalgo_2ext", "Anne_Hidalgo_www.leparisien.fr");
//			// helle
//			compareSearches("Cécile_Helle_1", "Cécile_Helle_2");
//			compareSearches("Cécile_Helle_2", "Cécile_Helle_www.laprovence.com");
//			// aubry
//			compareSearches("Martine_Aubry_1", "Martine_Aubry_2");
//			compareSearches("Martine_Aubry_1", "Martine_Aubry_www.lavoixdunord.fr");
		
		// clustering
//		testClustering();
		
		// complete reference
//		FileNames.setOutputFolder("Anne_Hidalgo");
//		String oldFile = FileNames.FO_WEB_SEARCH_RESULTS+File.separator+"old_reference.txt";
//		String newFile = FileNames.FO_WEB_SEARCH_RESULTS+File.separator+FileNames.FI_ANNOTATED_RESULTS;
//		String outFile = FileNames.FO_WEB_SEARCH_RESULTS+File.separator+"merged_reference.txt";
//		completeReference(oldFile,newFile,outFile);
		
		logger.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// COMPLETION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Completes an existing reference file with another, older annotated file,
	 * which contains less entries.
	 * 
	 * @param oldFile
	 * 		Old reference file: contains the classes. We expect the URL as the first column,
	 * 		then the class, and possibly additional fields (which will be kept in the output).  
	 * @param newFile 
	 * 		Current reference file: contains more results. We expect the URL as the first
	 * 		column. The remaining columns are ignored (if one wants to add them in the produced
	 * 		file, one can just edit it <i>a posteriori</i> using some office suite software.
	 * @param outFile 
	 * 		Result of the merge.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the files. 
	 * @throws FileNotFoundException 
	 * 		Problem while accessing the files. 
	 */
	private static void completeReference(String oldFile, String newFile, String outFile) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.log("Merging reference files");
		logger.increaseOffset();
		
		// load the old file
		logger.log("Load old file "+oldFile);
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		int colNbr = 0;
		{	Scanner scanner = FileTools.openTextFileRead(oldFile, "UTF-8");
			while(scanner.hasNextLine())
			{	String line = scanner.nextLine();
				String[] tmp = line.split("\t");
				if(tmp.length>colNbr)
					colNbr = tmp.length;
				String key = tmp[0];
				List<String> values = new ArrayList<String>();
				for(int i=1;i<tmp.length;i++)
					values.add(tmp[i]);
				map.put(key,values);
			}
			scanner.close();
		}
		logger.log("Read "+map.size()+" lines");
		
		// load the new file
		logger.log("Load new file "+newFile);
		List<String> keys = new ArrayList<String>();
		{	Scanner scanner = FileTools.openTextFileRead(newFile, "UTF-8");
			while(scanner.hasNextLine())
			{	String line = scanner.nextLine();
				String[] tmp = line.split("\t");
				String key = tmp[0];
				keys.add(key);
			}
			scanner.close();
		}
		logger.log("Read "+keys.size()+" lines");
		
		// combine the data
		logger.log("Merge and record in output file"+outFile);
		int count = 0;
		{	PrintWriter pw = FileTools.openTextFileWrite(outFile, "UTF-8");
			for(String key: keys)
			{	count++;
				List<String> values = map.get(key);
				if(values==null)
				{	pw.print(key);
					for(int i=1;i<colNbr;i++)
						pw.print("\t ");
				}
				else
				{	pw.print(key);
					for(String value:values)
						pw.print("\t"+value);
				}
				pw.println();
			}
			pw.close();
		}
		logger.log("Wrote "+count+" lines");
		
		logger.decreaseOffset();
		logger.log("Merging done");
	}
	
	/////////////////////////////////////////////////////////////////
	// RETRIEVAL	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests the retrieval of text coming from random Web sites.
	 * 
	 * @throws Exception
	 * 		Problem during the retrieval.
	 */
	private static void testRetrievalGeneric() throws Exception
	{	logger.setName("Test-GenericRetrieval");
		logger.log("Start testing the generic Web page retrieval");
		logger.increaseOffset();
		
		// problem cases:
		// - comments longer than the article text
		// - all the article text in a single element (eg. <p>)
		// - page is a list of articles (instead of just a single one)
		
//		URL url = new URL("http://www.lemonde.fr/culture/article/2014/07/16/la-prise-de-position-d-olivier-py-sur-le-fn-a-heurte-les-avignonnais_4457735_3246.html");
//		URL url = new URL("http://www.lemonde.fr/afrique/article/2015/05/02/au-togo-l-opposition-coincee-apres-son-echec-a-la-presidentielle_4626476_3212.html");
//		URL url = new URL("http://www.lemonde.fr/climat/article/2015/05/04/climat-les-energies-propres-en-panne-de-credits-de-recherche_4626656_1652612.html");
//		URL url = new URL("http://www.lemonde.fr/les-decodeurs/article/2015/05/03/les-cinq-infos-a-retenir-du-week-end_4626595_4355770.html");
//		URL url = new URL("http://www.lemonde.fr/les-decodeurs/article/2015/04/29/seisme-au-nepal-une-perte-economique-superieure-au-pib_4624928_4355770.html");
		
//		URL url = new URL("http://www.liberation.fr/vous/2015/05/04/coeur-carmat-le-deuxieme-greffe-decede-a-son-tour_1289323");
//		URL url = new URL("http://www.liberation.fr/societe/2015/05/04/femmes-en-politique-un-match-contre-les-machos_1289649");
//		URL url = new URL("http://www.liberation.fr/societe/2015/05/03/surveillance-le-flou-du-spectacle_1287003");
			
//		URL url = new URL("http://www.citylocalnews.com/avignon/2015/01/08/grand-avignon-cecile-helle-poursuit-son-lobby-ant-tram");
//		URL url = new URL("http://www.mobilicites.com/011-3377-Avignon-construira-bien-une-ligne-de-tramway.html");
//		URL url = new URL("http://www.citylocalnews.com/avignon/2015/01/26/avignon-des-policiers-a-cheval-bientot-sur-la-barthelasse");
//		URL url = new URL("https://fr-fr.facebook.com/pages/Les-Refondateurs/243905395803677");
//		URL url = new URL("http://www.citylocalnews.com/avignon/2015/01/10/avignon-le-tramway-divisera-t-il-la-majorite-municipale");
//		URL url = new URL("http://destimed.fr/Grand-Orient-de-France-conference");
//		URL url = new URL("http://www.medias-presse.info/le-grand-orient-nous-refait-le-coup-de-la-republique-en-danger/21996"); //xxxxxxxx plus de commentaires que d'article
//		URL url = new URL("http://www.meridienmag.fr/Actualites/ca-roule-pour-le-tramway-du-Grand-Avignon_2010.html");
//		URL url = new URL("http://www.grandavignon.fr/actualites/fiche/avignon-plan-proprete-tous-mobilises-tous-concernes/");
//		URL url = new URL("http://france3-regions.francetvinfo.fr/provence-alpes/2015/01/15/le-grand-avignon-aura-son-tramway-decouvrez-son-trace-632322.html");
//		URL url = new URL("http://www.midilibre.fr/2015/01/11/l-agglo-du-grand-avignon-vote-pour-le-tramway,1109359.php");
//		URL url = new URL("http://www.ville-rail-transports.com/content/21024-Avignon-Les-%C3%A9lus-votent-un-nouveau-projet-de-tram");
//		URL url = new URL("http://www.midilibre.fr/vaucluse/avignon/index_15.php");
//		URL url = new URL("http://www.liberation.fr/politiques/2015/01/08/regionales-vauzelle-ne-se-representera-pas-en-paca_1176199");
//		URL url = new URL("http://www.francebleu.fr/infos/chantier-tramway/le-tramway-en-questions-sur-france-bleu-vaucluse-2061645");
//		URL url = new URL("http://www.busetcar.com/actualites/detail/81676/le-tramway-d-avignon-verra-le-jour.html");
//		URL url = new URL("http://www.mavieenprovence.com/fr/tendances.html");
//		URL url = new URL("http://www.ledauphine.com/vaucluse/2015/01/06/avignon-tram-ou-pas-tram");
//		URL url = new URL("http://avignon.lafrancealunisson.fr/");
//		URL url = new URL("http://www.laprovence.com/actu/politique-en-direct/3203098/tramway-un-vote-convoque-samedi-pour-trancher.html");
//		URL url = new URL("http://www.ventoux-magazine.com/actualites/actualite/aeroport-d-avignon-le-pole-pegase-a-bien-decolle/x2132axyw2vMT79AhRm.html");
//		URL url = new URL("http://www.fm-mag.fr/evenement/antimaconnisme-refus-de-lautre-la-republique-en-danger"); //xxxxxxxx article trop court par rapport à la page
//		URL url = new URL("http://coordination-antinucleaire-sudest.net/2012/index.php?/page/2");
//		URL url = new URL("http://www.atlantico.fr/pepites/ps-michel-vauzelle-ne-se-representera-pas-presidence-region-paca-1945014.html");
//		URL url = new URL("http://www.leravi.org/spip.php?article1935");
//		URL url = new URL("http://www.infoavignon.com/actualites/politique-actualites/grand-avignon-cest-parti-pour-le-tram/");
//		URL url = new URL("http://raphael-helle.blog.lemonde.fr/2015/01/07/je-suis-charlie/");
//		URL url = new URL("http://www.clodelle45autrement.fr/2015/01/images-d-orleans-roller-derby-contre-hell-r-cheeky-dolls-la-rochelle-11-janvier-2015.html");
//		URL url = new URL("http://raphael-helle.blog.lemonde.fr/category/vie-privee/");
//		URL url = new URL("http://www.marcvuillemot.com/tag/var%20et%20intercommunalite/"); //xxxxxxxx liste d'articles

		URL url = new URL("http://www.univ-orleans.fr/espe/colloques");
//		URL url = new URL("http://www.lalogitude.com/albums/nos_adherents/index.html");
//		URL url = new URL("http://leslecturesdececile.fr/categorie/romance-historique/");
//		URL url = new URL("http://tvmag.lefigaro.fr/programme-tv/fiche/rmc-decouverte/serie-documentaire/190829701/highway-thru-hell-usa.html");
//		URL url = new URL("http://www.canalfrance.info/Marche-republicaine%E2%80%89-qui-seront-les-personnalites-presentes_a3173.html");
//		URL url = new URL("http://www.hellfest.fr/fr/pass-1-jour-a1084/");
//		URL url = new URL("https://pastel.archives-ouvertes.fr/tel-01109446/document");
//		URL url = new URL("http://www.gites-de-france-cotedor.com/location-Gite-Meursault-Cote-D-or-21G726.html");
//		URL url = new URL("http://tourner1page.fr/tag/mer/");
//		URL url = new URL("http://www.michelbachlebas.fr/mh-infos-municipales/presse");
//		URL url = new URL("http://prof.planck.fr/index.php?n=People.HFI-DPC");
//		URL url = new URL("http://www.enews-france.com/les-inrocks-secret-des-affaires-informer-n-est-pas-un-delit-128409-p");
//		URL url = new URL("http://www.lefigaro.fr/politique/2015/01/10/01002-20150110ARTFIG00160-l-impressionnante-liste-des-politiques-presents-a-la-marche-republicaine-a-paris.php");
//		URL url = new URL("http://blogs.mediapart.fr/edition/les-invites-de-mediapart/article/280115/secret-des-affaires-informer-n-est-pas-un-delit");
//		URL url = new URL("http://www.zicazic.com/zicazine/index.php?option=content&task=view&id=12130");
//		URL url = new URL("http://www.lesinrocks.com/2015/01/28/actualite/secret-des-affaires-informer-nest-pas-un-delit-11550918/");
//		URL url = new URL("http://univ-poitiers.academia.edu/CCM/Books");
//		URL url = new URL("http://www.tmplab.org/page/2/");
//		URL url = new URL("http://univ-poitiers.academia.edu/CCM");
//		URL url = new URL("http://www.commequiers.com/les-associatons-le-guide.html");
//		URL url = new URL("http://video.lefigaro.fr/figaro/video/la-chute-de-la-premiere-ministre-danoise-sur-les-marches-de-l-elysee/3984216817001/");
//		URL url = new URL("http://www.ville-de-wallers-arenberg.fr/wp/index.php/extensions/s5-tab-show-2/doc_download/88-bi-23");
//		URL url = new URL("http://www.400coups.net/archives/__crochet__/");
//		URL url = new URL("http://rakotoarison.over-blog.com/article-sr-125380563.html");
//		URL url = new URL("http://www.jesuismort.com/biographie_celebrite_anniversaire/5-eme_anniversaire_de_la_mort_de_celebrite.php");
//		URL url = new URL("http://cercletibetverite.unblog.fr/2015/01/");
//		URL url = new URL("http://www.shutupandplaythebooks.com/tu-mourras-moins-bete-marion-montaigne/");
//		URL url = new URL("http://bijoucontemporain.unblog.fr/category/ecole/gerrit-rietveld-academie-nl/");
//		URL url = new URL("https://les5duvin.wordpress.com/tag/salon-des-vins-de-loire/");
//		URL url = new URL("http://seminesaa.hypotheses.org/2842");
//		URL url = new URL("http://www.africine.org/index.php?menu=menuflm&smenu=format&choix_format=8&choix_trie=1");
//		URL url = new URL("http://www.agoravox.fr/tribune-libre/article/le-charlisme-est-un-humanisme-2-162338");
//		URL url = new URL("https://hal-mines-paristech.archives-ouvertes.fr/dumas-01102770/document");
//		URL url = new URL("http://www.babelio.com/livres/Green-Les-aventures-de-HawkFisher-tome-1/29897");
//		URL url = new URL("http://www.influencelesite.com/page/79");
//		URL url = new URL("http://www.unesourisdansmondressing.com/2015/01/22/avancer-malgre-la-tuile/");
//		URL url = new URL("http://www.ladepeche.fr/article/2015/01/06/");
//		URL url = new URL("http://www.cine-loisirs.fr/series/the-originals-2693/saison/2/episode/10");
//		URL url = new URL("http://pppl.blog.lemonde.fr/tag/robert-johnson/");
//		URL url = new URL("http://www.ideozmag.fr/je-suis-charlie-charlisme-est-un-humanisme-union-nationale/");
//		URL url = new URL("http://www.musicme.com/Vybz-Kartel/");
//		URL url = new URL("http://www.influencelesite.com/page/79");
//		URL url = new URL("http://www.unesourisdansmondressing.com/2015/01/22/avancer-malgre-la-tuile/");
//		URL url = new URL("http://www.ladepeche.fr/article/2015/01/06/");
//		URL url = new URL("http://www.cine-loisirs.fr/series/the-originals-2693/saison/2/episode/10");
//		URL url = new URL("http://pppl.blog.lemonde.fr/tag/robert-johnson/");
//		URL url = new URL("http://www.ideozmag.fr/je-suis-charlie-charlisme-est-un-humanisme-union-nationale/");
//		URL url = new URL("http://www.musicme.com/Vybz-Kartel/");
		
		ArticleRetriever retriever = new ArticleRetriever(false);
		retriever.process(url);
		
		logger.decreaseOffset();
		logger.log("Test terminated");
	}
	
	/**
	 * Tests the BoilerPlate library, which allows extracting relevant text from HTML pages.
	 * 
	 * @throws MalformedURLException
	 * 		Problem with the targeted URL.
	 * @throws BoilerpipeProcessingException
	 * 		Exception during the content extraction process.
	 */
	private static void testBoilerPipe() throws MalformedURLException, BoilerpipeProcessingException
	{	URL url = new URL("http://www.lemonde.fr/police-justice/article/2018/02/21/eric-dupond-moretti-supplie-d-eviter-la-prison-a-jerome-cahuzac_5260350_1653578.html");
		String text = ArticleExtractor.INSTANCE.getText(url);
		System.out.println(text);
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Title of the result of the comparison in the CSV file */
	private static final String COL_COMPARISON = "Comparison result";
	
	/**
	 * Loads a CSV file representing a collection of results, and
	 * put them in a map for later use.
	 * 
	 * @param path
	 * 		Path of the CSV file corresponding to the search.
	 * @param data
	 * 		An empty map representing the collection of URLs.
	 * @param engineNames
	 * 		A list meant to contain all search engine names mentioned in the file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while reading the CSV file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while reading the CSV file.
	 */
	private static void loadCSV(String path, Map<String,Map<String,String>> data, Set<String> engineNames) throws FileNotFoundException, UnsupportedEncodingException
	{	// open file
		Scanner scanner = FileTools.openTextFileRead(path,"UTF-8");
		
		// get header
		String header = scanner.nextLine();
		String colNames[] = header.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);	//this is taken from https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
		for(int i=0;i<colNames.length;i++)
			colNames[i] = colNames[i].replace('"',' ').trim();
		
		// get content
		while(scanner.hasNextLine())
		{	String line = scanner.nextLine();
			String tmp[] = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
			Map<String,String> map = new HashMap<String,String>();
			for(int i=0;i<tmp.length;i++)
			{	// add entry to the map
				String key = colNames[i];
				String value = tmp[i].replace('"',' ').trim();
				map.put(key,value);
				// possibly complete list of search engines
				if(startsLikeEngineName(key))
					engineNames.add(key);
			}
			String key = map.get(AbstractSearchResults.COL_URL);
			if(key==null)
				key = map.get(AbstractSearchResults.COL_URL_ID);
			data.put(key,map);
		}
	}
	
	/**
	 * Checks if the specified string starts like one of the search engine
	 * names (including both web search and social medias).
	 * 
	 * @param name
	 * 		The string to check.
	 * @return
	 * 		{@code true} iff it starts like a search engine name.
	 */
	private static boolean startsLikeEngineName(String name)
	{	List<String> engineNames = new ArrayList<String>();
		engineNames.addAll(Arrays.asList(AbstractWebEngine.ENGINE_NAMES));
		engineNames.addAll(Arrays.asList(AbstractSocialEngine.ENGINE_NAMES));
		boolean result = false;
		Iterator<String> it = engineNames.iterator();
		while(it.hasNext() && !result)
		{	String engineName = it.next();
			result = name.startsWith(engineName);
		}
		return result;
	}
	
	/**
	 * Compares the list of URLs resulting from two searches.
	 * 
	 * @param folder1
	 * 		Folder of the first search. 
	 * @param folder2 
	 * 		Folder of the second search.
	 * 
	 * @throws UnsupportedEncodingException 
	 * 		Something went wrong during the comparison.
	 * @throws FileNotFoundException 
	 * 		Something went wrong during the comparison.
	 */
	private static void compareSearches(String folder1, String folder2) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.setName("Compare-Searches");
		logger.log("Compare "+folder1+" vs. "+folder2);
		logger.increaseOffset();
		
		// read both files
		logger.log("Load the files");
		Set<String> engineNames = new TreeSet<String>();
		SearchFileNames.setSearchFolder(folder1);
		String filePath1 = SearchFileNames.FO_WEB_SEARCH_RESULTS + File.separator + SearchFileNames.FI_ARTICLES_ENTITY_FILTER;
		Map<String,Map<String,String>> map1 = new HashMap<String,Map<String,String>>();
		loadCSV(filePath1, map1, engineNames);
		SearchFileNames.setSearchFolder(folder2);
		String filePath2 = SearchFileNames.FO_WEB_SEARCH_RESULTS + File.separator + SearchFileNames.FI_ARTICLES_ENTITY_FILTER;
		Map<String,Map<String,String>> map2 = new HashMap<String,Map<String,String>>();
		loadCSV(filePath2, map2, engineNames);
		SearchFileNames.setSearchFolder(null);

		// set up column names for the output file
		List<String> colNames = new ArrayList<String>(Arrays.asList(
			AbstractSearchResults.COL_TITLE,
			AbstractSearchResults.COL_URL,
			COL_COMPARISON,
			AbstractSearchResults.COL_STATUS,
			AbstractSearchResults.COL_LENGTH
		));
		colNames.addAll(engineNames);
		
		// compare the two files
		logger.log("Compare them");
		List<Map<String,String>> only1 = new ArrayList<Map<String,String>>();
		List<Map<String,String>> only2 = new ArrayList<Map<String,String>>();
		List<Map<String,String>> both = new ArrayList<Map<String,String>>();
		for(Entry<String,Map<String,String>> entry: map1.entrySet())
		{	String key = entry.getKey();
			Map<String,String> vals1 = entry.getValue();
			Map<String,String> vals2 = map2.get(key);
			if(vals2==null)
			{	vals1.put(COL_COMPARISON, "Only #1");
				only1.add(vals1);
			}
			else
			{	Map<String,String> vals = new HashMap<String,String>();
				for(String colName: colNames)
				{	String val1 = vals1.get(colName);
					String val2 = vals2.get(colName);
					String val;
					if(val1==null || val1.isEmpty())
					{	if(val2==null || val2.isEmpty())
							val = "";
						else
							val = "<Empty>\n" + val2;
					}
					else
					{	if(val2==null || vals2.isEmpty())
							val = val1 + "\n<Empty>";
						else
						{	if(val1.equalsIgnoreCase(val2))
								val = val1;
							else
								val = val1 + "\n" + val2;
						}
					}
					vals.put(colName, val);
				}
				vals.put(COL_COMPARISON, "Both");
				both.add(vals);
			}
		}
		for(Entry<String,Map<String,String>> entry: map2.entrySet())
		{	String key = entry.getKey();
			Map<String,String> vals1 = map1.get(key);
			if(vals1==null)
			{	Map<String,String> vals2 = entry.getValue();
				vals2.put(COL_COMPARISON, "Only #2");
				only2.add(vals2);
			}
		}
		
		// record comparison outcome
		String filePath = FileNames.FO_OUTPUT + File.separator + "comparison_" + folder1 + "_vs_" + folder2 + FileNames.EX_CSV;
		logger.log("Record the comparison results in file "+filePath);
		PrintWriter pw = FileTools.openTextFileWrite(filePath, "UTF-8");
		// write header
		{	String header = "";
			for(String colName: colNames)
			{	if(!header.isEmpty())
					header = header + ",";
				header = header + "\"" + colName + "\"";
			}
			pw.println(header);
		}
		// write content
		List<List<Map<String,String>>> data = Arrays.asList(only1, only2, both);
		for(List<Map<String,String>> list: data)
		{	for(Map<String,String> vals: list)
			{	String line = "";
				for(String colName: colNames)
				{	if(!line.isEmpty())
						line = line + ",";
					String val = vals.get(colName);
					if(val==null)
						val = "";
					line = line + "\"" + val + "\"";
				}
				pw.println(line);
			}
		}
		pw.close();
		
		logger.decreaseOffset();
	}
	
	/////////////////////////////////////////////////////////////////
	// WHOLE PROCESS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Tests the whole information extraction process.
	 * 
	 * @throws Exception
	 * 		Something went wrong during the search. 
	 */
	@SuppressWarnings("unchecked")
	private static void testExtractor() throws Exception
	{	logger.setName("Extraction");
		Searcher extractor = new Searcher();
		
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
//		Date startDate = df.parse("20170306");
//		Date endDate = df.parse("20170312");
		Date startDate = df.parse("20170227");
		Date endDate = df.parse("20170319");
		boolean filterByPubDate = true;
		boolean filterByEntDate = false;
		boolean doExtendedSocialSearch = true;
		ArticleLanguage language = ArticleLanguage.FR;
		
		List<List<Object>> params = Arrays.asList
		(	Arrays.asList("Anne Hidalgo", "Hidalgo", Arrays.asList(null, "http://www.leparisien.fr/"),
				Arrays.asList("Bruno Julliard", "Jean-Louis Missika", "Ian Brossat", "Christophe Najdovski", "Nathalie Kosciusko-Morizet", "Claude Goasguen", "Brigitte Kuster"))
//			Arrays.asList("Cécile Helle", "Helle", Arrays.asList("http://www.laprovence.com/"), null),
//			Arrays.asList("Martine Aubry", "Aubry", Arrays.asList("http://www.lavoixdunord.fr/"), null),
//			Arrays.asList("Roland Ries", "Ries", null, null)
		);
		
		for(List<Object> param: params)
		{	String keywords = (String)param.get(0);
			String compulsoryExpression = (String)param.get(1);
			
			List<String> websites;
			if(param.get(2)==null)
			{	websites = new ArrayList<String>();
				websites.add(null);
			}
			else
				websites = (List<String>)param.get(2);
			
			List<String> additionalSeeds;
			if(param.get(3)==null)
				additionalSeeds = new ArrayList<String>();
			else
				additionalSeeds = (List<String>)param.get(3);
			
			logger.log("Processing "+keywords);
			logger.increaseOffset();
				extractor.performExtraction(keywords, websites, additionalSeeds, startDate, endDate, filterByPubDate, filterByEntDate, compulsoryExpression, doExtendedSocialSearch, language);
			logger.decreaseOffset();
		}
	}
}

// TODO filter articles that do not contain any event?
// TODO try to compare articles using only their named entities (by opposition to the whole lexicon)
// TODO setup a fuzzy k-means (or some other clustering algorithm allowing overlapping clusters)
