package sdf_manager.validators;

import com.google.gson.annotations.SerializedName;

public class AcceptedInstanceCoL implements AcceptedInstance {

	@SerializedName("name")
	private String name;
	@SerializedName("results")
	private results results;
	
	static class results {
		@SerializedName("id")
		private String id;
		
		public String getId() {
			return id;
		}
	}
		
	public AcceptedInstanceCoL() {
		// TODO Auto-generated constructor stub
	}

}
