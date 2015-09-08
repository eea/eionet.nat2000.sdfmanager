package sdf_manager.validators;

public class NameIdPair {
	
	String id;
	String name;

	public NameIdPair() {
		// TODO Auto-generated constructor stub
	}
	public NameIdPair(String id, String name) {
		this();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	//Simple hack to show value in JTable
	@Override
	public String toString() {
		return this.name; 				
	}	

}
