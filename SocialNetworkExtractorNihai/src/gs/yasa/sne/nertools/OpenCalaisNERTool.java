package gs.yasa.sne.nertools;

import gs.yasa.outputunifier.opencalais.OpenCalaisOutputReader;
import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;







public class OpenCalaisNERTool implements NERTool {

	@Override
	public ArrayList<Annotation> annotate(String text) {
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		try {
			HttpPost method = new HttpPost("http://api.opencalais.com/tag/rs/enrich");
			method.setHeader("x-calais-licenseID", "9xa5dyjmce22gtuanjq9rkt9");
			method.setHeader("Content-Type", "text/raw; charset=UTF-8");
			method.setHeader("Accept", "xml/rdf");
			method.setEntity(new StringEntity(text, "UTF-8"));
			HttpClient client = new DefaultHttpClient();
			HttpResponse response= client.execute(method);
			InputStream stream = response.getEntity().getContent();
			InputStreamReader streamReader = new InputStreamReader(stream,"UTF-8");
			BufferedReader bufferedReader = new BufferedReader(streamReader);
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = bufferedReader.readLine())!=null)
				builder.append(line+"\n");
			String annotatedText = builder.toString();
			
			OpenCalaisOutputReader reader = new OpenCalaisOutputReader();
			annotations = reader.read(annotatedText);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return annotations;
	}

	@Override
	public AnnotationTool getName() {
		// TODO Auto-generated method stub
		return AnnotationTool.OPENCALAIS;
	}

}
