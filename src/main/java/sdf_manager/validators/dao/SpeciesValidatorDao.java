package sdf_manager.validators.dao;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sdf_manager.validators.json.AcceptedInstanceCoL;
import sdf_manager.validators.json.FuzzyInstanceCyB;
import sdf_manager.validators.model.FuzzyResult;
import sdf_manager.validators.model.ValidatorTableRow;

/**
 * Handles the accepted species cybertaxonomy webservice
 * @author George Sofianos
 */
public class SpeciesValidatorDao implements ValidatorDao {
    private String CDM_QUERY_PROTOCOL = null;
    private String COL_QUERY_PROTOCOL = null;
    private String CDM_HOST_URL = null;
    private String COL_HOST_URL = null;
    private String COL_ACCEPTED_JSON_PATH = null;
    private String CDM_ACCEPTED_JSON_PATH = null;
    private String FUZZY_JSON_PATH = null;  
    private String FUZZY_SEARCH_ACCURACY = null;
    private String FUZZY_SEARCH_HITS = null;
    private String FUZZY_SEARCH_TYPE = null;
    private int CONNECTION_TIMEOUT = 0;    
    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(SpeciesValidatorDao.class .getName());
    
    /**
     * Constructor from Properties
     */
    public SpeciesValidatorDao(Properties props) {    	
    	CDM_QUERY_PROTOCOL = props.getProperty("cdm.connection.protocol");
		CDM_HOST_URL = props.getProperty("cdm.connection.host");
		CDM_ACCEPTED_JSON_PATH = props.getProperty("cdm.json.accepted");
		FUZZY_JSON_PATH = props.getProperty("cdm.json.fuzzy");
		FUZZY_SEARCH_ACCURACY = props.getProperty("cdm.fuzzy.accuracy");
		FUZZY_SEARCH_TYPE = props.getProperty("cdm.fuzzy.type");
		FUZZY_SEARCH_HITS = props.getProperty("cdm.fuzzy.hits");
		COL_QUERY_PROTOCOL = props.getProperty("col.connection.protocol");
		COL_HOST_URL = props.getProperty("col.connection.host");
		COL_ACCEPTED_JSON_PATH = props.getProperty("col.json.accepted");
		CONNECTION_TIMEOUT = Integer.parseInt(props.getProperty("cdm.connection.timeout")) * 1000; //time in milisecs		
    }
    
    protected String getJsonResponse(URIBuilder uriBuilder) throws URISyntaxException, ClientProtocolException, IOException {
    	SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(
                ProxySelector.getDefault());
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRoutePlanner(routePlanner)
                .build();                
               
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setSocketTimeout(CONNECTION_TIMEOUT)
            .build();                               
        URI simpleQueryUri = uriBuilder.build();        
        HttpGet httpGet = new HttpGet(simpleQueryUri);        
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = httpclient.execute(httpGet);     	            
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
            String responseJsonString = EntityUtils.toString(entity);
            return responseJsonString;
        } finally {
            response.close();            
        }        
    }
    
    /**
     * Returns a list of results of a fuzzy query
     * @param name - A species name to search for
     * @return a list of results of a fuzzy query
     * @throws IOException - If http connection fails
     * @throws URISyntaxException - when there is a wrong uri
     */
    public List<FuzzyResult> doQueryFuzzy(String name) throws IOException, URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(CDM_QUERY_PROTOCOL);
        uriBuilder.setHost(CDM_HOST_URL);
        uriBuilder.setPath(FUZZY_JSON_PATH);
        uriBuilder.setParameter("query", name.toLowerCase());
        uriBuilder.setParameter("accuracy", FUZZY_SEARCH_ACCURACY);
        uriBuilder.setParameter("hits", FUZZY_SEARCH_HITS);
        uriBuilder.setParameter("type", FUZZY_SEARCH_TYPE);        
        
        String responseJsonString = getJsonResponse(uriBuilder);
        FuzzyInstanceCyB jsonObject;
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();        
        JsonArray array = parser.parse(responseJsonString).getAsJsonArray();
        JsonElement responseJson = array.get(0);
        jsonObject = gson.fromJson(responseJson, FuzzyInstanceCyB.class);  
        
        if (jsonObject.responseSize() > 0) {
            return jsonObject.getResponses();
        } else {
          return null;
        }
    } 
    
    /**
     * Returns a list of results of an accepted species query
     * @param names - list of species names to search for
     * @return a list of results of an accepted species query
     * @throws IOException - If http connection fails
     * @throws URISyntaxException - when there is a wrong uri
     * @throws ValidatorDaoException 
     */
    public List<ValidatorTableRow> doQueryAccepted(List<String> names) throws IOException, URISyntaxException, ValidatorDaoException {     
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(COL_QUERY_PROTOCOL);
        uriBuilder.setHost(COL_HOST_URL);
        uriBuilder.setPath(COL_ACCEPTED_JSON_PATH);  
        List<ValidatorTableRow> rows = new ArrayList<ValidatorTableRow>();
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        for (int i = 0; i < names.size(); i++) {    	
	    	NameValuePair name = new BasicNameValuePair("name", names.get(i));
	    	parameters.clear();
	    	parameters.add(name);        
	        parameters.add(new BasicNameValuePair("format", "json"));
	        parameters.add(new BasicNameValuePair("response", "full"));
	        uriBuilder.setParameters(parameters);
	        String responseJsonString = getJsonResponse(uriBuilder);
   
	        Gson gson = new Gson();
	        JsonParser parser = new JsonParser();   
	        JsonElement parsedJson = parser.parse(responseJsonString);
	    	JsonObject object = parsedJson.getAsJsonObject();            
	    	AcceptedInstanceCoL result = gson.fromJson(object, AcceptedInstanceCoL.class);                                                                                            
	        if (result.hasError()) {
	        	log.info(result.getError());	        	
	        } else if (result.responseSize() > 0) {	        		        	    
                rows.addAll(result.getResponses());
            } 	        
    	}
        if (rows != null && !rows.isEmpty()) { 
        	return rows;
        } else {
        	return null;
        }        
    }
}
