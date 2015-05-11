package tr.edu.gsu.nerwip.recognition.internal.modelless.opener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import tr.edu.gsu.nerwip.data.article.Article;
import tr.edu.gsu.nerwip.data.entity.EntityType;
import tr.edu.gsu.nerwip.recognition.RecognizerException;
import tr.edu.gsu.nerwip.recognition.RecognizerName;
import tr.edu.gsu.nerwip.recognition.internal.modelless.AbstractModellessInternalRecognizer;
import tr.edu.gsu.nerwip.recognition.internal.modelless.opencalais.OpenCalaisConverter;
import tr.edu.gsu.nerwip.tools.keys.KeyHandler;

public class OpeNER extends AbstractModellessInternalRecognizer<List<String>,OpeNERConverter>
{
	public OpeNER(boolean ignorePronouns, boolean exclusionOn)
	{	super(false,ignorePronouns,exclusionOn);
		
		// init converter
		converter = new OpeNERConverter(getFolder());
	}
	
/////////////////////////////////////////////////////////////////
// NAME				/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
@Override
public RecognizerName getName()
{	return RecognizerName.OPENER;
}

/////////////////////////////////////////////////////////////////
// FOLDER			/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
@Override	
public String getFolder()
{	String result = getName().toString();

result = result + "_" + "ignPro=" + ignorePronouns;
result = result + "_" + "exclude=" + exclusionOn;

return result;
}

/////////////////////////////////////////////////////////////////
// ENTITIES			/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
/** List of entities recognized by OpenCalais */
private static final List<EntityType> HANDLED_TYPES = Arrays.asList(
EntityType.DATE,
EntityType.LOCATION,
EntityType.ORGANIZATION,
EntityType.PERSON
);

@Override
public List<EntityType> getHandledEntityTypes()
{	return HANDLED_TYPES;
}

/////////////////////////////////////////////////////////////////
// PROCESSING	 		/////////////////////////////////////////
/////////////////////////////////////////////////////////////////


@Override
protected List<String> detectEntities(Article article) throws RecognizerException
{	logger.increaseOffset();
//String result;
List<String> result = new ArrayList<String>();
String text = article.getRawText();


try
{	// define HTTP message
logger.log("Build OpeNER http message");
String url = "http://opener.olery.com/ner";
HttpPost method = new HttpPost(url);
//Request parameters 
List<NameValuePair> params = new ArrayList<NameValuePair>();
params.add(new BasicNameValuePair("input", text ));
params.add(new BasicNameValuePair("language", "fr" ));
method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));


// send to opener
logger.log("Send message to OpeNER");
HttpClient client = new DefaultHttpClient();
HttpResponse response = client.execute(method);
InputStream stream = response.getEntity().getContent();
InputStreamReader streamReader = new InputStreamReader(stream,"UTF-8");
BufferedReader bufferedReader = new BufferedReader(streamReader);

// read answer
logger.log("Read OpeNER answer");
StringBuilder builder = new StringBuilder();
String line;
int nbr = 0;
while((line = bufferedReader.readLine())!=null)
{	builder.append(line+"\n");
nbr++;

 logger.log("Line:" +line);				}
 logger.log("Lines read: "+nbr);
 String answer = builder.toString();
 result.add(answer);

}



catch (UnsupportedEncodingException e)
{	e.printStackTrace();
throw new RecognizerException(e.getMessage());
}
catch (ClientProtocolException e)
{	e.printStackTrace();
throw new RecognizerException(e.getMessage());
}
catch (IOException e)
{	e.printStackTrace();
throw new RecognizerException(e.getMessage());
}

logger.decreaseOffset();


// logger.decreaseOffset();
 return result;
}
}

