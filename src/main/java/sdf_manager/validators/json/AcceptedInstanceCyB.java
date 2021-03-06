package sdf_manager.validators.json;

import com.google.gson.annotations.SerializedName;

import sdf_manager.validators.model.ValidatorTableRow;

import java.util.ArrayList;
import java.util.List;


/**
 * Class for a JSON representation of accepted species data.
 * @author George Sofianos
 * 
 */
public class AcceptedInstanceCyB implements AcceptedInstance {  
  @SerializedName("errorMessage")
  private String errorMessage;
  @SerializedName("request")
  private request request;
  static class request {
    @SerializedName("query")
    private String query;

    public request(String query) {
      this.query = query;
    }
    public String getQuery() {
      return this.query;
    }
   }
  
   @SerializedName("response")
   private List<responseImpl> response;
   static class responseImpl {
      @SerializedName("acceptedName")
      private String acceptedName;
      @SerializedName("authorship")
      private String authorship;
      @SerializedName("classification")
      private classification classification;
      static class classification {
        @SerializedName("Kingdom")
        private String Kingdom;
        @SerializedName("Phylum")
        private String Phylum;
        @SerializedName("Class")
        private String Class;
        @SerializedName("Order")
        private String Order;
        @SerializedName("Family")
        private String Family;
        @SerializedName("Genus")
        private String Genus;
        @SerializedName("Species")
        private String Species;

        public classification() {
        	// no-args constructor
        }        
      }      
      @SerializedName("rank")
      private String rank;

      public responseImpl() {
      }  
   }
  public AcceptedInstanceCyB(request request, List<responseImpl> response) {
    this.request = request;
    this.response = response;
  }
  public int responseSize() {
    return response.size();
  }
  public List<ValidatorTableRow> getResponses() {
	  String query = request.getQuery();
	  //boolean queryEqualsAccepted = true;
      List<ValidatorTableRow> results = new ArrayList<ValidatorTableRow>();
      for (responseImpl im : response) {
    	  if (!im.acceptedName.equalsIgnoreCase(query)) {
    		  //AcceptedValidatorTableRow i = new AcceptedValidatorTableRow(query, im.classification.Kingdom, im.classification.Family, new AcceptedNamePair(false, im.acceptedName));
    		 // results.add(i);
    	  }
          //AcceptedValidatorTableRow i = new AcceptedValidatorTableRow(im.acceptedName,im.classification.Kingdom,im.classification.Family, new AcceptedNamePair(true, im.acceptedName));
          //results.add(i);
      }
      return results;
  }
  
  public boolean hasError() {
    return this.errorMessage != null? true: false;
  }
}
