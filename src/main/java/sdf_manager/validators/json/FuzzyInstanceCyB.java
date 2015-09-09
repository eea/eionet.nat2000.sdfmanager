package sdf_manager.validators.json;

import com.google.gson.annotations.SerializedName;

import sdf_manager.validators.model.FuzzyResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for a JSON representation of fuzzy species data.
 * @author George Sofianos
 */
public class FuzzyInstanceCyB {  
  
  @SerializedName("errorMessage")
  private String errorMessage;
  @SerializedName("request")
  private Request request;
  static class Request {
    @SerializedName("query")
    private String query;   
    public Request() {
    	// no-args constructor
    }
   }
  
   @SerializedName("response")
   private List<Response> responses;
   static class Response {
      @SerializedName("acceptedTaxonUuids")
      private List<String> acceptedTaxonUuids;
      @SerializedName("name")
      private String name;
      @SerializedName("nameUuids")
      private List<String> nameUuids;
      @SerializedName("score")
      private BigDecimal score;
      @SerializedName("taxonConceptUuids")
      private List<String> taxonConceptUuids;
      @SerializedName("title")
      private String title;      

      public Response() {
      	// no-args constructor
      }
	  public String getName() {
		  return name;
	  }
	
	  public BigDecimal getScore() {
		  return score;
	  }
	
	  public String getTitle() {
		  return title;
	  }
      
   }
    
   	public FuzzyInstanceCyB() {
    	// no-args constructor
   	}
   	public int responseSize() {
   		if (responses == null) {
   			return -1;
   		} else {
   			return responses.size();
   		}	  
   	}
	public boolean hasError() {
		return this.errorMessage != null? true: false;
	}
	public List<FuzzyResult> getResponses() {
		List<FuzzyResult> results = new ArrayList<FuzzyResult>();
		for (Response response : responses) {
			FuzzyResult result = new FuzzyResult(response.name, response.title, response.score);
			results.add(result);
		}
		return results;     	  
	}
}
