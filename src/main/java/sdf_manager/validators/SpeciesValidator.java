package sdf_manager.validators;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import sdf_manager.util.PropertyUtils;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Handles the accepted species cybertaxonomy webservice
 * @author George Sofianos
 */
public final class SpeciesValidator {
    private String QUERY_PROTOCOL = null;
    private String NAME_CATALOG_URL = null;
    private String ACCEPTED_JSON_PATH = null;
    private String FUZZY_JSON_PATH = null;  
    private String FUZZY_SEARCH_ACCURACY = null;
    private String FUZZY_SEARCH_HITS = null;
    private String FUZZY_SEARCH_TYPE = null;
    private int CONNECTION_TIMEOUT = 0;
    private Properties props = null;
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpeciesValidator.class .getName());
    
    /**
     * Default Constructor
     */
    public SpeciesValidator() {
    	try {
			props = PropertyUtils.readProperties("sdf.properties");
			QUERY_PROTOCOL = props.getProperty("cdm.connection.protocol");
			NAME_CATALOG_URL = props.getProperty("cdm.connection.host");
			ACCEPTED_JSON_PATH = props.getProperty("cdm.json.accepted");
			FUZZY_JSON_PATH = props.getProperty("cdm.json.fuzzy");
			FUZZY_SEARCH_ACCURACY = props.getProperty("cdm.fuzzy.accuracy");
			FUZZY_SEARCH_TYPE = props.getProperty("cdm.fuzzy.type");
			FUZZY_SEARCH_HITS = props.getProperty("cdm.fuzzy.hits");
			CONNECTION_TIMEOUT = Integer.parseInt(props.getProperty("cdm.connection.timeout")) * 1000; //time in milisecs
		} catch (IOException e) {
			log.error("Can't read CDM connection properties");			
		}
    }
    /**
     * Returns a list of results of a fuzzy query
     * @param name - A species name to search for
     * @return a list of results of a fuzzy query
     * @throws IOException - If http connection fails
     * @throws URISyntaxException - when there is a wrong uri
     */
    public List<IValidatorResultsRow> doQueryFuzzy(String name) throws IOException, URISyntaxException {
        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(
            ProxySelector.getDefault());
        CloseableHttpClient httpclient = HttpClients.custom()
            .setRoutePlanner(routePlanner)
            .build();                
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(QUERY_PROTOCOL);
        uriBuilder.setHost(NAME_CATALOG_URL);
        uriBuilder.setPath(FUZZY_JSON_PATH);
        uriBuilder.setParameter("query", name.toLowerCase());
        uriBuilder.setParameter("accuracy", FUZZY_SEARCH_ACCURACY);
        uriBuilder.setParameter("hits", FUZZY_SEARCH_HITS);
        uriBuilder.setParameter("type", FUZZY_SEARCH_TYPE);
                
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setSocketTimeout(CONNECTION_TIMEOUT)
            .build();                               
        URI simpleQueryUri = uriBuilder.build();        
        HttpGet httpGet = new HttpGet(simpleQueryUri);        
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = httpclient.execute(httpGet);        
        
        GsonFuzzyInstance jsonObject;
        try {
            HttpEntity entity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 300) { 
                throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }          
            if (entity == null) {
              throw new ClientProtocolException("Response contains no content");
            }
            ContentType contentType = ContentType.getOrDefault(entity);
            if (!contentType.getMimeType().equals(ContentType.APPLICATION_JSON.getMimeType())) {
                throw new ClientProtocolException("Unexpected content type " + contentType);
            }
            Charset charset = contentType.getCharset();
            if (charset == null) {
                charset = StandardCharsets.UTF_8;
            }
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            String responseJsonString = EntityUtils.toString(entity);
            JsonArray array = parser.parse(responseJsonString).getAsJsonArray();
            JsonElement responseJson = array.get(0);
            jsonObject = gson.fromJson(responseJson, GsonFuzzyInstance.class);                                                                                    
        } finally {
            response.close();
        }
        if (jsonObject.responseSize() > 0) {
            return jsonObject.getResponses();
        }
        else {
          return null;
        }
    } 
    /**
     * Returns a list of results of an accepted species query
     * @param names - list of species names to search for
     * @return a list of results of an accepted species query
     * @throws IOException - If http connection fails
     * @throws URISyntaxException - when there is a wrong uri
     */
    public List<IValidatorResultsRow> doQueryAccepted(List<String> names) throws IOException, URISyntaxException {
        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(
            ProxySelector.getDefault());
        CloseableHttpClient httpclient = HttpClients.custom()
            .setRoutePlanner(routePlanner)
            .build();   
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(QUERY_PROTOCOL);
        uriBuilder.setHost(NAME_CATALOG_URL);
        uriBuilder.setPath(ACCEPTED_JSON_PATH);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        for (int i = 0; i < names.size(); i++) {
        	NameValuePair a = new BasicNameValuePair("query", names.get(i).toLowerCase()); 
        	parameters.add(a);
        }
        uriBuilder.setParameters(parameters);
        
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setSocketTimeout(CONNECTION_TIMEOUT)
            .build();  
        URI simpleQueryUri = uriBuilder.build();
        HttpGet httpGet = new HttpGet(simpleQueryUri);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = httpclient.execute(httpGet);        
        
        List<GsonAcceptedInstance> resultJsonList;
        try {
            HttpEntity entity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 300) { 
                throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }          
            if (entity == null) {
              throw new ClientProtocolException("Response contains no content");
            }
            ContentType contentType = ContentType.getOrDefault(entity);
            if (!contentType.getMimeType().equals(ContentType.APPLICATION_JSON.getMimeType())) {
                throw new ClientProtocolException("Unexpected content type " + contentType);
            }
            Charset charset = contentType.getCharset();
            if (charset == null) {
                charset = StandardCharsets.UTF_8;
            }
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            String responseJsonString = EntityUtils.toString(entity);
            JsonArray array = parser.parse(responseJsonString).getAsJsonArray();            
            resultJsonList = Arrays.asList(gson.fromJson(array, GsonAcceptedInstance[].class));                                                                                    
        } finally {
            response.close();
        }        
        boolean hasOnlyErrors = true;
        for (GsonAcceptedInstance in : resultJsonList) {
        	if (!in.hasError()) {
                hasOnlyErrors = false;
                break;
            }
        }
        if (hasOnlyErrors) { 
        	return null; 
      	}
        List<IValidatorResultsRow> rows = new ArrayList<IValidatorResultsRow>(); 
        for (GsonAcceptedInstance in : resultJsonList) {
        	if (in.responseSize() > 0) {      
                rows.addAll(in.getResponses());
            } 
        }
        if (rows != null && !rows.isEmpty()) { 
        	return rows;
        } else {
        	return null;
        }
        
    }
}
