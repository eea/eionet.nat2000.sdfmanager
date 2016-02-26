package sdf_manager.validators;

public class AcceptedNameTriple {

	private boolean accepted;
	private String acceptedName;
	private String acceptedId;
	
	public AcceptedNameTriple() {
		// TODO Auto-generated constructor stub
	}
	public AcceptedNameTriple(boolean accepted, String acceptedId, String acceptedName) {
		this();
		this.accepted = accepted;
		this.acceptedId = acceptedId;
		this.acceptedName = acceptedName;				
	}
	public boolean isAccepted() {
		return accepted;
	}

	public String getAcceptedName() {
		return acceptedName;
	}
	public void setAcceptedName(String acceptedName) {
		this.acceptedName = acceptedName;
	}
		
	public String getAcceptedId() {
		return acceptedId;
	}
	public void setAcceptedId(String acceptedId) {
		this.acceptedId = acceptedId;
	}
	//Simple hack to show value in JTable
	@Override
	public String toString() {
		if (accepted) { 
			return "Yes";
		} else {
			return "No";
		}
	}	
}
