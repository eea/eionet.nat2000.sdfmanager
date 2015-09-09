package sdf_manager.validators.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import sdf_manager.validators.AcceptedNameTriple;
import sdf_manager.validators.NameIdPair;
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
		@SerializedName("id")
		private String id;
		@SerializedName("name")
		private String name;
		@SerializedName("rank")
		private String rank;
		@SerializedName("name_status")
		private String nameStatus;
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
			public List<Classification> getClassification() {
				return classification;
			}
			public AcceptedName(String id, String name, String rank, String nameStatus) {				
				this.id = id;
				this.name = name;
				this.rank = rank;
				this.nameStatus = nameStatus;				
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
			@SerializedName("name_html")
			private String name_html;
			
			public Classification(String id, String name, String rank, String name_html) {
				this.id = id;
				this.name = name;
				this.rank = rank;
				this.name_html = name_html;
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

			public String getName_html() {
				return name_html;
			}
			
		}
		public Result(String id, String name, String rank, String nameStatus) {
			this.id = id;
			this.name = name;
			this.rank = rank;
			this.nameStatus = nameStatus;
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
		public List<Classification> getClassification() {
			return classification;
		}
		public AcceptedName getAcceptedName() {
			return acceptedName;
		}
	}
	
	public List<ValidatorTableRow> getResponses() {
		List<ValidatorTableRow> list = new ArrayList<ValidatorTableRow>();
		for (Result result : results) {
			if (result.getRank().equalsIgnoreCase(Result.SPECIES)) {
				List<Result.Classification> classificationList = new ArrayList<Result.Classification>();
				String id = result.getId();
				String name = result.getName();
				NameIdPair nameId = new NameIdPair(id, name);
				String status = result.getNameStatus();		
				String kingdom = null;
				String family = null;	
				String acceptedName = null;
				String acceptedId = null;
				if (status.equalsIgnoreCase(Result.SYNONYM)) {
					Result.AcceptedName acceptedNameObj = result.getAcceptedName();
					if (acceptedNameObj.getRank().equalsIgnoreCase(Result.SPECIES)) {
						acceptedName = acceptedNameObj.getName();
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
					list.add(row);
					
					nameId = new NameIdPair(acceptedNameObj.getId(), acceptedNameObj.getName());					
					row = new ValidatorTableRow(nameId, kingdom, family, new AcceptedNameTriple(true, acceptedId, acceptedName));
					list.add(row);
				} else {				
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
					list.add(row);
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
		// TODO Auto-generated constructor stub
	}

}
