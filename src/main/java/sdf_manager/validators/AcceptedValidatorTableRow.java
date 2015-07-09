package sdf_manager.validators;

/**
 *
 * @author George Sofianos
 */
public class AcceptedValidatorTableRow {
    private String acceptedName;
    private String kingdom;
    private String family;
    private String score;

    public AcceptedValidatorTableRow(String acceptedName, String kingdom, String family, String score) {
        this.acceptedName = acceptedName;
        this.kingdom = kingdom;
        this.family = family;
        this.score = score;
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

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }        
  
}
