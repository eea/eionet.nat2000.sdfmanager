/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pojos;


public class RefQuality {
    private int refQualityId;
    private String refQualityName;
    private String refQualityCode;
    private String refQualitySpecies;
    
    /**
     * 
     */
    public RefQuality(){
        
    }

    /**
     * @return the refQualityId
     */
    public int getRefQualityId() {
        return refQualityId;
    }

    /**
     * @param refQualityId the refQualityId to set
     */
    public void setRefQualityId(int refQualityId) {
        this.refQualityId = refQualityId;
    }

    /**
     * @return the refQualityName
     */
    public String getRefQualityName() {
        return refQualityName;
    }

    /**
     * @param refQualityName the refQualityName to set
     */
    public void setRefQualityName(String refQualityName) {
        this.refQualityName = refQualityName;
    }

    /**
     * @return the refQualityCode
     */
    public String getRefQualityCode() {
        return refQualityCode;
    }

    /**
     * @param refQualityCode the refQualityCode to set
     */
    public void setRefQualityCode(String refQualityCode) {
        this.refQualityCode = refQualityCode;
    }

    /**
     * @return the refQualitySpecies
     */
    public String getRefQualitySpecies() {
        return refQualitySpecies;
    }

    /**
     * @param refQualitySpecies the refQualitySpecies to set
     */
    public void setRefQualitySpecies(String refQualitySpecies) {
        this.refQualitySpecies = refQualitySpecies;
    }
}
