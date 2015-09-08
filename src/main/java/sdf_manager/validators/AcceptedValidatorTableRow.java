package sdf_manager.validators;

/**
 *
 * @author George Sofianos
 */
public class AcceptedValidatorTableRow implements ValidatorResultsRow {
    private NameIdPair nameId;
    private String kingdom;
    private String family;
    private AcceptedNameTriple acceptedNameTriple;

    public AcceptedValidatorTableRow(NameIdPair nameId, String kingdom, String family, AcceptedNameTriple acceptedNameTriple) {
        this.nameId = nameId;
        this.kingdom = kingdom;
        this.family = family;        
        this.acceptedNameTriple = acceptedNameTriple;
    }
    
    public NameIdPair getNameId() {
		return nameId;
	}


	public void setNameId(NameIdPair nameId) {
		this.nameId = nameId;
	}


	public String getKingdom() {
        return kingdom;
    }

    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }
	
	@Override
	public Object[] getRow() { 
		return new Object[] {this.nameId, this.kingdom, this.family, this.acceptedNameTriple};
	}
    
}
