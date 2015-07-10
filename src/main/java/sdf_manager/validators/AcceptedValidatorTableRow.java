package sdf_manager.validators;

/**
 *
 * @author George Sofianos
 */
public class AcceptedValidatorTableRow implements IValidatorResultsRow {
    private String acceptedName;
    private String kingdom;
    private String family;    

    public AcceptedValidatorTableRow(String acceptedName, String kingdom, String family) {
        this.acceptedName = acceptedName;
        this.kingdom = kingdom;
        this.family = family;        
    }

    public String getAcceptedName() {
        return acceptedName;
    }

    public void setAcceptedName(String acceptedName) {
        this.acceptedName = acceptedName;
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
    
}
