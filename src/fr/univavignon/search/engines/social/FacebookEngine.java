package fr.univavignon.search.engines.social;

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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import facebook4j.Category;
import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.PagableList;
import facebook4j.Page;
import facebook4j.Paging;
import facebook4j.Post;
import facebook4j.Reaction;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.User;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;
import fr.univavignon.common.data.article.ArticleLanguage;
import fr.univavignon.search.results.SocialSearchResult;
import fr.univavignon.search.tools.files.SearchFileNames;
import fr.univavignon.tools.files.FileNames;
import fr.univavignon.tools.files.FileTools;
import fr.univavignon.common.tools.keys.KeyHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class uses the Facebook API to search the Web.
 * <br/>
 * Resources: 
 * http://facebook4j.github.io/en/index.html#source_code
 * http://facebook4j.github.io/en/code-examples.html
 * https://developers.facebook.com/docs/graph-api/using-graph-api
 * http://stackoverflow.com/questions/13165589/facebook-api-access-with-username-password-via-client-software#
 * <br/>
 * Site used to get the page/user ids: https://findmyfbid.in/
 * 
 * @author Vincent Labatut
 */
public class FacebookEngine extends AbstractSocialEngine
{
	/**
	 * Initializes the object used to search Facebook.
	 * 
	 * @param seed
	 * 		Name of the page/user/account used to start the search.
	 * @param startDate
	 * 		Start of the period we want to consider, 
	 * 		or {@code null} for no constraint.
	 * @param endDate
	 * 		End of the period we want to consider,
	 * 		or {@code null} for no constraint.
	 * @param doExtendedSearch
	 * 		If {@code true}, the search returns the posts by the commenting
	 * 		users, for the specified period. 
	 * @param language
	 * 		Targeted language. 
	 * 
	 * @throws IOException
	 * 		Problem while logging in Facebook. 
	 * @throws MalformedURLException 
	 * 		Problem while logging in Facebook. 
	 * @throws FailingHttpStatusCodeException 
	 * 		Problem while logging in Facebook. 
	 * @throws URISyntaxException 
	 * 		Problem while logging in Facebook. 
	 */
	public FacebookEngine(String seed, Date startDate, Date endDate, boolean doExtendedSearch, ArticleLanguage language) throws FailingHttpStatusCodeException, MalformedURLException, IOException, URISyntaxException
	{	super(seed,startDate,endDate,doExtendedSearch);
		
		switch(language)
		{	case EN:
				pageLanguage = "lang_en";
				break;
			case FR:
				pageCountry = "countryFR";
				pageLanguage = "lang_fr";
				break;
		}
		
		// logging in and getting the access token
		String login = KeyHandler.KEYS.get(USER_LOGIN);
		String pwd = KeyHandler.KEYS.get(USER_PASSWORD);	
		String accessToken = getAccessToken(login,pwd);
		
		// setting up the FB session
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthAppId(KeyHandler.KEYS.get(APP_ID));
		cb.setOAuthAppSecret(KeyHandler.KEYS.get(APP_SECRET));
		cb.setOAuthAccessToken(accessToken);
		cb.setOAuthPermissions("email, publish_stream, id, name, first_name, last_name, read_stream , generic");
		cb.setAppSecretProofEnabled(true);
		cb.setUseSSL(true); 
		cb.setJSONStoreEnabled(true);
		Configuration config = cb.build();
		factory = new FacebookFactory(config);
	}
	
	/////////////////////////////////////////////////////////////////
	// USER/PAGE IDS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map of predefined Facebook page ids (associated to exact names) */
	private final static Map<String,String> PAGE_IDS = new HashMap<String,String>();
	/** Map of predefined Facebook user ids (associated to exact names) */
	private final static Map<String,String> USER_IDS = new HashMap<String,String>();
	/**
	 * Loads both maps: the first column is the exact name (must match the query),
	 * the second one is the page id, and the third one is the user id (can be empty).
	 */
	static
	{	String filePath = FileNames.FO_MISC + File.separator + SearchFileNames.FI_FACEBOOK_IDS;
		try
		{	logger.log("Loading the predefined Facebook user and page ids");
			logger.increaseOffset();
			Scanner scanner = FileTools.openTextFileRead(filePath, "UTF-8");
			int c = 0;
			while(scanner.hasNextLine())
			{	c++;
				String line = scanner.nextLine();
				String tmp[] = line.split("\t");
				String name = tmp[0];
				String pageId = tmp[1];
				if(pageId.isEmpty())
					pageId = null;
				PAGE_IDS.put(name, pageId);
				if(tmp.length>2)
				{	String userId = tmp[2];
					if(userId.isEmpty())
						userId = null;
					USER_IDS.put(name, userId);
				}
			}
			logger.log("Loaded " + c + " entries");
			logger.increaseOffset();
		} 
		catch (FileNotFoundException e) 
		{	e.printStackTrace();
		} 
		catch (UnsupportedEncodingException e) 
		{	e.printStackTrace();
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// SERVICE		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the FB user login */
	private static final String URL_LOGIN = "https://www.facebook.com/v2.9/dialog/oauth?client_id=437488563263592&response_type=token&redirect_uri=";
	/** Redirection URL used during login */
	private static final String URL_REDIRECT = "https://www.facebook.com/connect/login_success.html";
	/** Redirection URL used during login */
	private static final String URL_PARAM_ACCESS = "access_token=";
	/** Name of the FB user login */
	private static final String USER_LOGIN = "FacebookUserLogin";
	/** Name of the FB user password */
	private static final String USER_PASSWORD = "FacebookUserPassword";
//	/** Object used to format dates in the query */
//	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	/** Name of the id of the FB app */
	private static final String APP_ID = "FacebookAppId";
	/** Name of the FB app secret */
	private static final String APP_SECRET = "FacebookAppSecret";
//	/** Number of results returned for one request */
//	private static final long PAGE_SIZE = 10; // max seems to be only 10!
	
	/**
	 * Log in Facebook, using the specified user login and password.
	 * <br/>
	 * This method is adapted from the StackOverflow post by Nicola Marcacci Rossi:
	 * http://stackoverflow.com/a/13214455/1254730
	 * 
	 * @param username
	 * 		Login of the user.
	 * @param password
	 * 		Password associated to the user login.
	 * @return
	 * 		Access token.
	 * 
	 * @throws FailingHttpStatusCodeException
	 * 		Problem while logging.
	 * @throws MalformedURLException
	 * 		Problem while logging.
	 * @throws IOException
	 * 		Problem while logging.
	 * @throws URISyntaxException 
	 * 		Problem while logging.
	 */
	private static String getAccessToken(String username, String password) throws FailingHttpStatusCodeException, MalformedURLException, IOException, URISyntaxException 
	{	logger.log("Logging in Facebook");
		logger.increaseOffset();
		
		logger.log("Initializing Web client");
		WebClient wc = new WebClient();
		WebClientOptions opt = wc.getOptions();
		opt.setCssEnabled(false);
		opt.setJavaScriptEnabled(false);
		
		// go to the FB homepage
		logger.log("Going to FB connection page");
		String url = URL_LOGIN+URLEncoder.encode(URL_REDIRECT,"UTF-8"); 
		HtmlPage page = wc.getPage(url);
		HtmlForm form = (HtmlForm) page.getElementById("login_form");
		
		// setup the login and password
		form.getInputByName("email").setValueAttribute(username);
		form.getInputByName("pass").setValueAttribute(password);
		
		// search the ok button and click
		logger.log("Entering user info and connecting");
		HtmlButton button = form.getButtonByName("login");
		button.click();
		
		// get the redirected page
		logger.log("Following redirection");
		HtmlPage currentPage = (HtmlPage) wc.getCurrentWindow().getEnclosedPage();
		URL currentUrl = currentPage.getUrl();
		String newUrl = currentUrl.toString();
		logger.log(newUrl);
		int startIdx = newUrl.indexOf(URL_PARAM_ACCESS);
		int endIdx = newUrl.indexOf("&", startIdx+1);
		if(endIdx==-1)
			endIdx = newUrl.length();
		String result = newUrl.substring(startIdx+URL_PARAM_ACCESS.length(),endIdx);
		logger.log("Access token: "+result);
	    
		wc.close();
		logger.decreaseOffset();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATA			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Textual name of this engine */
	public static final String ENGINE_NAME = "Facebook";

	@Override
	public String getName()
	{	return ENGINE_NAME;
	}
	
	/////////////////////////////////////////////////////////////////
	// PARAMETERS	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Focus on pages hosted in a certain country */
	public String pageCountry = null;	// actually not used anymore
	/** Focus on pages in a certain language */
	public String pageLanguage = null;	// actually not used anymore
//	/** Whether the result should be sorted by date, or not (in this case: by relevance). If {@link #sortByDate} is not {@code null}, only the specified time range is treated. */
//	public boolean sortByDate = false;
//	/** Date range the search should focus on. It should take the form YYYYMMDD:YYYYMMDD, or {@code null} for no limit. If {@link #sortByDate} is set to {@code false}, this range is ignored. */
//	public String dateRange = null;
	/** Maximal number of results (can be less if facebook does not provide) */
	public static final int MAX_RES_NBR = 100;

	/////////////////////////////////////////////////////////////////
	// BUILDER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Object used to build Facebook instances */
	private FacebookFactory factory;
	
	/////////////////////////////////////////////////////////////////
	// SEARCH		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected List<SocialSearchResult> search(String keywords)  throws IOException
	{	logger.log("Searching Facebook");
		logger.increaseOffset();
		List<SocialSearchResult> result = new ArrayList<SocialSearchResult>();
		
		// search parameters
		logger.log("Keywords: "+keywords);
		String name;
		if(seed==null)
		{	logger.log("No seed specified >> using the kewords to get the initial page");
			name = keywords;
		}
		else
		{	logger.log("Search starting from the page of "+seed);
			name = seed;
		}
		
		// setup API parameters
		Reading reading = null;
		if(startDate!=null && endDate!=null)
		{	reading = new Reading();
			reading.since(startDate);
			reading.until(endDate);
		}
		
		Facebook facebook = factory.getInstance();
		try
		{	List<String> ids = new ArrayList<String>();
			
			// look for the name in the predefined ids
			logger.log("Look up the name to find a predefined FB id:");
			logger.increaseOffset();
			{	String pageId = PAGE_IDS.get(name);
				if(pageId!=null)
					ids.add(pageId);
				logger.log("Page id: "+pageId);
				String userId = USER_IDS.get(name);
				if(userId!=null)
					ids.add(userId);
				logger.log("User id: "+userId);
			}
			logger.decreaseOffset();
			
			// if there is no predefined id for this name, we look it up on facebook
			if(ids.isEmpty())
			{	logger.log("No predefined id found for \""+name+"\" >> we look it up through FB");
				String id = getPageOrUserId(name,facebook);
				if(id==null)
					logger.log("We could not find any id for the targeted person");
				else
					ids.add(id);
			}
			
			// process all remaining pages or user ids
			for(String id: ids)
			{	List<SocialSearchResult> res = retrieveContent(name, id, facebook, reading);
				result.addAll(res);
			}
		} 
		catch (FacebookException e) 
		{	//System.err.println(e.getMessage());
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Returns a FB id for the specified name. We first look for a page,
	 * and if we don't find one, for a user. This is because public persons
	 * usually use a page for their communication. 
	 * <br/>
	 * If at some point we get several pages or users compatible with the
	 * specified name, we use the first one (we trust the order of the results
	 * returned by the FB API is relevant and favors public figures). 
	 *  
	 * @param keywords
	 * 		Name of the targeted person.
	 * @param facebook
	 * 		Current instance of the search engine. 
	 * @return
	 * 		A string corresponding to the best id found, or {@code null}
	 * 		if node could be found at all.
	 * 
	 * @throws FacebookException
	 * 		Problem while accessing the Facebook API. 
	 */
	private String getPageOrUserId(String keywords, Facebook facebook) throws FacebookException
	{	String result = null;
		logger.increaseOffset();
		
		logger.log("Look for the FB page corresponding to \""+keywords+"\"");
		logger.increaseOffset();
		{	ResponseList<Page> pages =  facebook.searchPages(keywords);
			if(pages.isEmpty())
				logger.log("No page found at all");
			else
			{	logger.log("Found at least "+pages.size()+" pages");
				List<Page> list = new ArrayList<Page>();
				logger.increaseOffset();
				{	for(Page page: pages)
					{	String pageId = page.getId();
						String pageName = page.getName();
						if(pageName.equalsIgnoreCase(keywords))
						{	logger.log("Title="+pageName+", id="+pageId+" >> seems relevant");
							list.add(page);
						}
						else
							logger.log("Title="+pageName+", id="+pageId+" >> rejected because the name is not exactly the same");
					}
				}
				logger.decreaseOffset();
				
				logger.log("Found "+list.size()+" pages fitting the name");
				Page page = list.get(0);
				result = page.getId();
				logger.log("WARNING: we trust the FB search engine... and use only the first one it returned ("+result+")");
			}
		}
		logger.decreaseOffset();

		if(result!=null)
			logger.log("An appropriate page was found, so we do not look for a user id");
		else
		{	logger.log("Look for the FB user corresponding to \""+keywords+"\"");
			logger.increaseOffset();
			{	ResponseList<User> users =  facebook.searchUsers(keywords);
				if(users.isEmpty())
					logger.log("No user found at all");
				else
				{	logger.log("Found at least "+users.size()+" users");
					List<User> list = new ArrayList<User>();
					logger.increaseOffset();
					{	for(User user: users)
						{	String userId = user.getId();
							String userName = user.getName();
							if(userName.equalsIgnoreCase(keywords))
							{	logger.log("Name="+userName+", id="+userId+" >> seems relevant");
								list.add(user);
							}
							else
								logger.log("Name="+userName+", id="+userId+" >> rejected because the name is not exactly the same");
						}
					}
					logger.decreaseOffset();
					
					logger.log("Found "+list.size()+" users fitting the name");
					User user = list.get(0);
					result = user.getId();
					logger.log("WARNING: we trust the FB search engine... and use only the first one it returned ("+result+")");
				}
			}
			logger.decreaseOffset();
		}
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Retrieves the post authored by the specified id (possibly for the period encoded in the reading).
	 * The direct comments are also retrieved, as well as the posts published by their commenters on
	 * the same period.
	 *  
	 * @param name
	 * 		Name of the seed person.
	 * @param id
	 * 		Id of the seed person.
	 * @param facebook
	 * 		Current instance of the FB search engine.
	 * @param reading
	 * 		Graph API reading options (in our case: dates).
	 * 		
	 * @return
	 * 		The list of posts associated to the page or user.
	 * 
	 * @throws FacebookException
	 * 		Problem while accessing the FB API.
	 */
	private List<SocialSearchResult> retrieveContent(String name, String id, Facebook facebook, Reading reading) throws FacebookException
	{	logger.log("Retrieving the content for id "+id);
		logger.increaseOffset();
		List<SocialSearchResult> result = new ArrayList<SocialSearchResult>();
		
		// get all the posts of the targeted id
		List<Post> pagePosts = getPosts(id, facebook, reading);
		
        // get the comments associated to all the targeted posts
		Set<String> authorIds = new TreeSet<String>();
		Map<String,String> authorNames = new HashMap<String,String>();
		logger.log("Retrieving the comments for each post (for the specified period, if any)");
		logger.increaseOffset();
		for(Post post: pagePosts)
		{	// get the message text
			String msg = post.getMessage();
			if(msg==null)
				logger.log("No message in the current post");
			else
			{	msg = msg.replaceAll("\\s+", " ");
				logger.log("Message: \""+msg+"\"");
				// get the meta-data
				String ctntId = post.getId();
				Date date = post.getCreatedTime();
				Category auth = post.getFrom();
				String authName;
				if(auth==null)
					authName = name;	//TODO should remove this parameter name, we shouldn't need it
				else
					authName = auth.getName();
				boolean original = seed==null;
				// create the post object
				SocialSearchResult p = new SocialSearchResult(ctntId, authName, date, getName(), msg, original);
				p.url = post.getLink();
				result.add(p);
				
				// get the number of likes
				Reading rdg = new Reading();
			    rdg.limit(0);
				rdg.summary();
				ResponseList<Reaction> listL = facebook.getPostReactions(ctntId, rdg);
				int likes = listL.getSummary().getTotalCount();
				p.likes = likes;
				// get the number of shares
				rdg.fields("shares");
				Post tempPost = facebook.getPost(ctntId, rdg);
				Integer shares = tempPost.getSharesCount();
				p.shares = shares;
				logger.log("Found "+likes+" likes (and variants) and "+shares+" shares");
				
				// retrieve the comments associated to the message
				List<Comment> comments = getComments(post, facebook, reading);
				// add them to the current post
				for(Comment comment: comments)
				{	// get the message text
					msg = comment.getMessage();
					msg = msg.replaceAll("\\s+", " ");
					// get the meta-data
					ctntId = comment.getId();
					date = comment.getCreatedTime();
					auth = comment.getFrom();
					authName = auth.getName();
					// create the post object
					SocialSearchResult com = new SocialSearchResult(ctntId, authName, date, getName(), msg, false);
					p.comments.add(com);
					// add the comment author to the list
					String authId = auth.getId();
					authorIds.add(authId);
					authorNames.put(authId, authName);
				}
			}
		}
		logger.decreaseOffset();
		
		// get the authors posts for this period
		logger.log("Retrieving the posts of the commenting authors (for the specified period, if any)");
		logger.increaseOffset();
		for(String authId: authorIds)
		{	logger.log("Processing id "+authId);
			logger.increaseOffset();
			List<Post> authPosts = getPosts(authId, facebook, reading);
			for(Post post: authPosts)
			{	// get the message text
				String msg = post.getMessage();
				if(msg==null)
					logger.log("The current post has no textual content");
				else
				{	msg = msg.replaceAll("\\s+", " ");
					logger.log("Message: \""+msg+"\"");
					
					// get the meta-data
					String ctntId = post.getId();
					Date date = post.getCreatedTime();
					Category auth = post.getFrom();
					String authName;
					if(auth==null)
					{	authName = authorNames.get(authId);
						if(authName==null)
							authName = "N/A";
					}
					else
						authName = auth.getName();
					// create the post object
					SocialSearchResult p = new SocialSearchResult(ctntId, authName, date, getName(), msg, false);
					p.url = post.getLink();
					result.add(p);
					
					// get the number of likes
					Reading rdg = new Reading();
				    rdg.limit(0);
					rdg.summary();
					ResponseList<Reaction> listL = facebook.getPostReactions(ctntId, rdg);
					int likes = listL.getSummary().getTotalCount();
					p.likes = likes;
					// get the number of shares
					rdg.fields("shares");
					Post tempPost = facebook.getPost(ctntId, rdg);
					Integer shares = tempPost.getSharesCount();
					p.shares = shares;
					logger.log("Found "+likes+" likes (and variants) and "+shares+" shares");
					
					// TODO we do not get the comments, this time (we could if needed)
				}
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Gets all the posts for the specified page or user.
	 *  
	 * @param id
	 * 		Id of the page or user of of interest.
	 * @param facebook
	 * 		Current Facebook instance.
	 * @param reading
	 * 		Graph API reading options (in our case: dates).
	 * @return
	 * 		The list of all posts associated to the page or user.
	 * 
	 * @throws FacebookException
	 * 		Problem while accessing the comments.
	 */
	private List<Post> getPosts(String id, Facebook facebook, Reading reading) throws FacebookException
	{	logger.log("Retrieving the posts of the FB page/user of id "+id);
		logger.increaseOffset();
		
		List<Post> result = new ArrayList<Post>();
		Paging<Post> paging;
		
		int i = 1;
		ResponseList<Post> postPage = facebook.getPosts(id,reading);
        do 
        {	logger.log("Processing post page #"+i);
			logger.increaseOffset();
        	i++;
        	
        	// add the post of the current page to the overall list
        	logger.log("Found "+postPage.size()+" posts in the current page");
        	for(Post post: postPage)
        		logger.log(post.getCreatedTime()+": "+post.getMessage());
        	result.addAll(postPage);
        	
        	// try to get the next page of posts
			paging = postPage.getPaging();
            postPage = null;
            if(paging!=null)
            {	logger.log("Getting the next page of posts");
            	postPage = facebook.fetchNext(paging);
            }
			logger.decreaseOffset();
        } 
        while(postPage!= null);
        
		logger.log("Total posts found for the targeted page/user: "+result.size());
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Gets all the comments for the specified post.
	 *  
	 * @param post
	 * 		Post of interest.
	 * @param facebook
	 * 		Current Facebook instance.
	 * @param reading
	 * 		Graph API reading options (in our case: dates).
	 * @return
	 * 		The list of all comments associated to the post.
	 * 
	 * @throws FacebookException
	 * 		Problem while accessing the comments.
	 */
	private List<Comment> getComments(Post post, Facebook facebook, Reading reading) throws FacebookException 
	{	logger.log("Retrieving comments");
		logger.increaseOffset();
		
		List<Comment> result = new ArrayList<Comment>();
		Paging<Comment> paging;
		
		// iteratively get each page of comments
        int i = 1;
//        PagableList<Comment> commentPage = post.getComments();
        String postId = post.getId();
        PagableList<Comment> commentPage = facebook.getPostComments(postId, reading);
        do 
        {	logger.log("Comment page #"+i);
			logger.increaseOffset();
			i++;
			
			// add the comments of the current result page to the result list
        	result.addAll(commentPage);
        	logger.log("Found "+commentPage.size()+" comments on the current result page");
        	List<String> commentsStr = new ArrayList<String>();
        	for(Comment comment: commentPage)
        		commentsStr.add(comment.getMessage());
			logger.log(commentsStr);
        	
			// TODO we could go and fetch the answers to these comments, but that does not seem necessary
			
        	// try to get the next page of comments
            paging = commentPage.getPaging();
            commentPage = null;
            if(paging!=null)
            {	logger.log("Getting the next result page");
        		commentPage = facebook.fetchNext(paging);
            }
            logger.decreaseOffset();
		} 
        while(commentPage!= null);
		logger.log("Total comments found for this post: "+result.size());
	    
		logger.decreaseOffset();
	    return result;
	}
	
//	/**
//	 * Gets the total number of likes for the specified post.
//	 *  
//	 * @param id
//	 * 		Id of the page or user of of interest.
//	 * @param facebook
//	 * 		Current Facebook instance.
//	 * @param reading
//	 * 		Graph API reading options (in our case: dates).
//	 * @return
//	 * 		The list of all posts associated to the page or user.
//	 * 
//	 * @throws FacebookException
//	 * 		Problem while accessing the comments.
//	 */
//	private List<Post> getPosts(String id, Facebook facebook, Reading reading) throws FacebookException
//	{	logger.log("Retrieving the posts of the FB page/user of id "+id);
//		logger.increaseOffset();
//		
//		List<Post> result = new ArrayList<Post>();
//		Paging<Post> paging;
//		
//		int i = 1;
//		ResponseList<Post> postPage = facebook.getPosts(id,reading);
//        do 
//        {	logger.log("Processing post page #"+i);
//			logger.increaseOffset();
//        	i++;
//        	
//        	// add the post of the current page to the overall list
//        	logger.log("Found "+postPage.size()+" posts in the current page");
//        	for(Post post: postPage)
//        		logger.log(post.getCreatedTime()+": "+post.getMessage());
//        	result.addAll(postPage);
//        	
//        	// try to get the next page of posts
//			paging = postPage.getPaging();
//            postPage = null;
//            if(paging!=null)
//            {	logger.log("Getting the next page of posts");
//            	postPage = facebook.fetchNext(paging);
//            }
//			logger.decreaseOffset();
//        } 
//        while(postPage!= null);
//        
//		logger.log("Total posts found for the targeted page/user: "+result.size());
//		logger.decreaseOffset();
//		return result;
//	}
	
	/////////////////////////////////////////////////////////////////
	// TEST			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Method used to test/debug this class.
	 * 
	 * @param args
	 * 		None needed.
	 * @throws Exception
	 * 		All exceptions are thrown.
	 */
	public static void main(String[] args) throws Exception
	{	Date startDate = new GregorianCalendar(2017,4,7).getTime();//new GregorianCalendar(2017,3,6).getTime();//null;
		Date endDate = new GregorianCalendar(2017,4,8).getTime();//new GregorianCalendar(2017,3,10).getTime();//null;
		FacebookEngine fe = new FacebookEngine(null, startDate, endDate, true, ArticleLanguage.FR);
		fe.search("François Hollande");
	}
}
