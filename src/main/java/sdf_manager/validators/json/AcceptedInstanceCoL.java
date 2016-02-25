package sdf_manager.validators.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import sdf_manager.validators.AcceptedNameTriple;
import sdf_manager.validators.NameIdPair;
import sdf_manager.validators.dao.ValidatorDaoException;
import sdf_manager.validators.model.ValidatorTableRow;

public class AcceptedInstanceCoL implements AcceptedInstance {
	@SerializedName("error_message")
	private String errorMessage;
	
	@SerializedName("name")
	private String name;
	@SerializedName("results")
	private List<Result> results;
	
	static class Result {
		private final static String SPECIES = "Species";
		private final static String KINGDOM = "Kingdom";
		private final static String FAMILY = "Family";
		private final static String SYNONYM = "synonym";
		private final static String AMBIGUOUS_SYNONYM = "ambiguous synonym";
		private final static String COMMON = "common name";
		@SerializedName("id")
		private String id;
		@SerializedName("name")
		private String name;
		@SerializedName("rank")
		private String rank;
		@SerializedName("name_status")
		private String nameStatus;
		@SerializedName("name_html")
		private String nameHtml;
		@SerializedName("accepted_name")
		private AcceptedName acceptedName;
		static class AcceptedName {
			@SerializedName("id")
			private String id;
			@SerializedName("name")
			private String name;
			@SerializedName("rank")
			private String rank;
			@SerializedName("name_status")
			private String nameStatus;
			@SerializedName("name_html")
			private String nameHtml;
			@SerializedName("classification")
			private List<Classification> classification;
			public String getId() {
				return id;
			}
			public String getName() {
				return name;
			}
			public String getRank() {
				return rank;
			}
			public String getNameStatus() {
				return nameStatus;
			}			
			public String getNameHtml() {
				return nameHtml;
			}
			public List<Classification> getClassification() {
				return classification;
			}
			public AcceptedName() {
	        	// no-args constructor
			}					
		}
		@SerializedName("classification")
		private List<Classification> classification;
		static class Classification {
			@SerializedName("id")
			private String id;
			@SerializedName("name")
			private String name;
			@SerializedName("rank")
			private String rank;
			
			public Classification() {
	        	// no-args constructor
			}

			public String getId() {
				return id;
			}

			public String getName() {
				return name;
			}

			public String getRank() {
				return rank;
			}
			
		}
		public Result() {
        	// no-args constructor
		}
		public String getId() {
			return id;
		}
		public String getRank() {
			return rank;
		}
		public String getName() {
			return name;
		}
		public String getNameStatus() {
			return nameStatus;
		}		
		public String getNameHtml() {
			return nameHtml;
		}
		public List<Classification> getClassification() {
			return classification;
		}
		public AcceptedName getAcceptedName() {
			return acceptedName;
		}
	}
	
	public List<ValidatorTableRow> getResponses() throws ValidatorDaoException {
		List<ValidatorTableRow> list = new ArrayList<ValidatorTableRow>();
		for (Result result : results) {			
			if (result.getRank() == null && result.getNameStatus() == null) {
				throw new ValidatorDaoException("JSON response has wrong formatting.");								
			} else if ((result.getRank() == null && result.getNameStatus().equalsIgnoreCase(Result.COMMON)) || result.getRank().equalsIgnoreCase(Result.SPECIES)) {
				List<Result.Classification> classificationList = new ArrayList<Result.Classification>();
				String id = result.getId();
				String name = result.getNameHtml();
				NameIdPair nameId = new NameIdPair(id, name);
				String status = result.getNameStatus();		
				String kingdom = null;
				String family = null;	
				String acceptedName = null;
				String acceptedId = null;
				if (status.equalsIgnoreCase(Result.SYNONYM) || status.equalsIgnoreCase(Result.AMBIGUOUS_SYNONYM) || status.equalsIgnoreCase(Result.COMMON)) {
					// Result is a synonym or a common name.
					Result.AcceptedName acceptedNameObj = result.getAcceptedName();
					if (acceptedNameObj.getRank().equalsIgnoreCase(Result.SPECIES)) {
						acceptedName = acceptedNameObj.getNameHtml();
						acceptedId = acceptedNameObj.getId();
					}
					classificationList = acceptedNameObj.getClassification();
					for (Result.Classification classification : classificationList) {
						if (classification.getRank().equalsIgnoreCase(Result.KINGDOM)) {
							kingdom = classification.getName();
						} else if (classification.getRank().equalsIgnoreCase(Result.FAMILY)) {
							family = classification.getName();
						}
						if (kingdom != null && family != null) break;
					}					
					ValidatorTableRow row = new ValidatorTableRow(nameId, kingdom, family, new AcceptedNameTriple(false, acceptedId, acceptedName));
					if (status.equalsIgnoreCase(Result.SYNONYM) || status.equalsIgnoreCase(Result.AMBIGUOUS_SYNONYM)) {
						if (!list.contains(row)) {
							list.add(row);	
						}					
					}				
					nameId = new NameIdPair(acceptedNameObj.getId(), acceptedNameObj.getNameHtml());					
					row = new ValidatorTableRow(nameId, kingdom, family, new AcceptedNameTriple(true, acceptedId, acceptedName));
					if (!list.contains(row)) {
						list.add(row);
					}					
				} else {			
					// the result is an accepted species
					classificationList = result.getClassification();
					for (Result.Classification classification : classificationList) {
						if (classification.getRank().equalsIgnoreCase(Result.KINGDOM)) {
							kingdom = classification.getName();
						} else if (classification.getRank().equalsIgnoreCase(Result.FAMILY)) {
							family = classification.getName();
						}
						if (kingdom != null && family != null) break;
					}
					ValidatorTableRow row = new ValidatorTableRow(nameId, kingdom, family, new AcceptedNameTriple(true, id, name));
					if (!list.contains(row)) { 
						list.add(row);
					}
				}
			}
		}
		return list;
	}
	public int responseSize() {
		return results.size();
	}
	public boolean hasError() {
		return this.errorMessage.equalsIgnoreCase("")? false: true;
	}
	public String getError() {
		if (this.hasError()) {
			return this.errorMessage;
		}
		return null;
	}
	public AcceptedInstanceCoL() {
    	// no-args constructor
	}

}
