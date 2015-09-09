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
  private request request;
  static class request {
    @SerializedName("query")
    private String query;           
   }
  
   @SerializedName("response")
   private List<responseImpl> response;
   static class responseImpl {
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
 
  public int responseSize() {
	  if (response == null) {
		  return -1;
	  } else {
		  return response.size();
	  }
	  
  }
  public boolean hasError() {
    return this.errorMessage != null? true: false;
  }
  public List<FuzzyResult> getResponses() {
	  List<FuzzyResult> results = new ArrayList<FuzzyResult>();
      for (responseImpl im : response) {
          FuzzyResult result = new FuzzyResult(im.name, im.title, im.score);
          results.add(result);
      }
      return results;     	  
  }
}
