package sdf_manager.validators;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    final static String QUERY_PROTOCOL = "http";
    final static String NAME_CATALOG_URL = "api.cybertaxonomy.org";
    final static String ACCEPTED_JSON_PATH = "/col/name_catalogue/accepted.json";
    final static String FUZZY_JSON_PATH = "/col/name_catalogue/fuzzy.json";
    final static String TAXON_JSON_PATH = "/col/name_catalogue/taxon.json";
    final static int CONNECTION_TIMEOUT = 60 * 1000; // timeout in millis
    
    public List<IValidatorResultsRow> doQuery(String method, String name) throws IOException, URISyntaxException {
        if (method.equalsIgnoreCase("accepted")) {
            return doQueryAccepted(name);
        }
        if (method.equalsIgnoreCase("fuzzy")) {
            return doQueryFuzzy(name);            
        }
        return null;
    }
    public List<IValidatorResultsRow> doQueryAccepted(String name) throws IOException, URISyntaxException {
        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(
            ProxySelector.getDefault());
        CloseableHttpClient httpclient = HttpClients.custom()
            .setRoutePlanner(routePlanner)
            .build();   
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(QUERY_PROTOCOL);
        uriBuilder.setHost(NAME_CATALOG_URL);
        uriBuilder.setPath(ACCEPTED_JSON_PATH);
        uriBuilder.setParameter("query", name.toLowerCase());
        
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setSocketTimeout(CONNECTION_TIMEOUT)
            .build();  
        URI simpleQueryUri = uriBuilder.build();
        HttpGet httpGet = new HttpGet(simpleQueryUri);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = httpclient.execute(httpGet);        
        
        GsonAcceptedInstance jsonObject;
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
            jsonObject = gson.fromJson(responseJson, GsonAcceptedInstance.class);                                                                                    
        } finally {
            response.close();
        }        
        if (jsonObject.hasError()) {
          return null;
        }
        if (jsonObject.responseSize() > 0) {      
          return jsonObject.getResponses();
        } else {
          return null;
        }
    }
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
        uriBuilder.setParameter("accuracy", "0.6");
        uriBuilder.setParameter("hits", "10");
        uriBuilder.setParameter("type", "name");
                
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
    public List<IValidatorResultsRow> doQueryAcceptedList(List<String> names) throws IOException, URISyntaxException {
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
            //JsonElement responseJson = array.get(0);
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
