package gs.yasa.taglinks;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


/**
 * This class searches matches for entities for a given Wikipedia article title
 * using links in the article.
 * @author yasa akbulut
 * @version 1
 *
 */
public class FreebaseUtil {

	/**
	 * This method gets types of the entities in a Wikipedia article.
	 * It takes as parameter the title of an article and starts a query
	 * for entities and their types defined on 
	 * http://api.freebase.com/api/service/mqlread.
	 * Those entities are the hyperlinks in a Wikipedia article.
	 * @author yasa akbulut
	 * @param wikipediaTitle : title of a Wikipedia article
	 * @return resultList, an ArrayList containing the entities and their types
	 */
	public static ArrayList<String> getTypes(String wikipediaTitle)  
	{
		ArrayList<String> resultList = new ArrayList<String>();
		try
		{
		String query = "[{ \"name\": null, " +
		"\"type\": [{}], " +
		"\"key\": " +
			"[{ \"namespace\": \"/wikipedia/en\", " +
			"\"value\": \""+ wikipediaTitle +"\" }] }]";
		String query_envelope = "{\"query\":" + query + "}";
		String service_url = "http://api.freebase.com/api/service/mqlread";
		String url = service_url  + "?query=" + URLEncoder.encode(query_envelope, "UTF-8");
		
		HttpClient httpclient = new DefaultHttpClient();   
		HttpResponse response = httpclient.execute(new HttpGet(url));
		
		JSONParser parser = new JSONParser();
		JSONObject json_data = (JSONObject)parser.parse(EntityUtils.toString(response.getEntity()));
		JSONArray results = new JSONArray();
		results = (JSONArray)json_data.get("result");
		
		if(results!=null)
		{
			for (Object result : results) {
				JSONArray types = (JSONArray) ((JSONObject)result).get("type");
				for(Object type: types)
				{
					resultList.add(((JSONObject)type).get("id").toString());
				}
			}
		}
		
		}catch (UnsupportedEncodingException e) {
			// TODO: handle exception
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resultList;
	}
	
	/**
	 * This method applies reverse engineering
	 * @author yasa akbulut
	 * @param input
	 * @return
	 */
	public static String escapeMqlKey(String input)
	{
		String regex = "[A-Za-z0-9_-]";
		StringBuilder builder = new StringBuilder();
		int i=0;
		for(i=0; i<input.length(); i++)
		{
			if(input.substring(i, i+1).matches(regex))
			{
				builder.append(input.substring(i, i+1));
			}
			else
			{
				int code = input.charAt(i);
				builder.append(String.format("$%04x", code).toUpperCase());
			}
		}
		return builder.toString();
	}
}
