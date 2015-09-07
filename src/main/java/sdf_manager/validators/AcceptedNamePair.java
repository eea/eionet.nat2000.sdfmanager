package sdf_manager.validators;

public class AcceptedNamePair {

	private boolean accepted;
	private String acceptedName;
	
	public AcceptedNamePair() {
		// TODO Auto-generated constructor stub
	}
	public AcceptedNamePair(boolean accepted, String acceptedName) {
		this();
		this.accepted = accepted;
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
