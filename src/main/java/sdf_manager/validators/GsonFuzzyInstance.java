package sdf_manager.validators;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author George Sofianos
 */
public class GsonFuzzyInstance {  
  
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

   /**
    * Constructor
    * @param request
    * @param response 
    */
  
  public int responseSize() {
    return response.size();
  }
  public boolean hasError() {
    return this.errorMessage != null? true: false;
  }
  public List<AcceptedValidatorTableRow> getResponses() {
      List results = new ArrayList<FuzzyValidatorTableRow>();
      for (responseImpl im : response) {
          FuzzyValidatorTableRow i = new FuzzyValidatorTableRow(im.name,im.title,im.score);
          results.add(i);
      }
      return results;
  }
}
