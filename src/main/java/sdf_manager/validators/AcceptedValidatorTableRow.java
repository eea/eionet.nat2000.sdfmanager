package sdf_manager.validators;

/**
 *
 * @author George Sofianos
 */
public class AcceptedValidatorTableRow implements ValidatorResultsRow {
    private String name;
    private String kingdom;
    private String family;
    private AcceptedNamePair acceptedNamePair;

    public AcceptedValidatorTableRow(String name, String kingdom, String family, AcceptedNamePair acceptedNamePair) {
        this.name = name;
        this.kingdom = kingdom;
        this.family = family;        
        this.acceptedNamePair = acceptedNamePair;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
		return new Object[] {this.name, this.kingdom, this.family, this.acceptedNamePair};
	}
    
}
