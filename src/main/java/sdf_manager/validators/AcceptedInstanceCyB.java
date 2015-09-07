package sdf_manager.validators;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;


/**
 * Class for a JSON representation of accepted species data.
 * @author George Sofianos
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

        public classification(String Kingdom, String Phylum, String Class, String Order, String Family, String Genus, String Species) {
          this.Kingdom = Kingdom;
          this.Phylum = Phylum;
          this.Class = Class;
          this.Order = Order;
          this.Family = Family;
          this.Genus = Genus;
          this.Species = Species;
        }        
      }      
      @SerializedName("rank")
      private String rank;

      public responseImpl(String acceptedName, String authorship, classification classification, String rank) {
        this.acceptedName = acceptedName;
        this.authorship = authorship;
        this.classification = classification;
        this.rank = rank;
      }  
   }
  public AcceptedInstanceCyB(request request, List<responseImpl> response) {
    this.request = request;
    this.response = response;
  }
  public int responseSize() {
    return response.size();
  }
  public List<ValidatorResultsRow> getResponses() {
	  String query = request.getQuery();
	  //boolean queryEqualsAccepted = true;
      List<ValidatorResultsRow> results = new ArrayList<ValidatorResultsRow>();
      for (responseImpl im : response) {
    	  if (!im.acceptedName.equalsIgnoreCase(query)) {
    		  AcceptedValidatorTableRow i = new AcceptedValidatorTableRow(query, im.classification.Kingdom, im.classification.Family, new AcceptedNamePair(false, im.acceptedName));
    		  results.add(i);
    	  }
          AcceptedValidatorTableRow i = new AcceptedValidatorTableRow(im.acceptedName,im.classification.Kingdom,im.classification.Family, new AcceptedNamePair(true, im.acceptedName));
          results.add(i);
      }
      return results;
  }
  
  public boolean hasError() {
    return this.errorMessage != null? true: false;
  }
}
