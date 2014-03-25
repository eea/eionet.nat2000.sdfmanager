/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pojos;


public class RefCategory {
    private int refCategoryId;
    private String refCategoryName;
    private String refCategoryCode;
    private String refCategorySpecies;
    
    /**
     * 
     */
    public RefCategory(){
        
    }

    /**
     * @return the refCategoryId
     */
    public int getRefCategoryId() {
        return refCategoryId;
    }

    /**
     * @param refCategoryId the refCategoryId to set
     */
    public void setRefCategoryId(int refCategoryId) {
        this.refCategoryId = refCategoryId;
    }

    /**
     * @return the refCategoryName
     */
    public String getRefCategoryName() {
        return refCategoryName;
    }

    /**
     * @param refCategoryName the refCategoryName to set
     */
    public void setRefCategoryName(String refCategoryName) {
        this.refCategoryName = refCategoryName;
    }

    /**
     * @return the refCategoryCode
     */
    public String getRefCategoryCode() {
        return refCategoryCode;
    }

    /**
     * @param refCategoryCode the refCategoryCode to set
     */
    public void setRefCategoryCode(String refCategoryCode) {
        this.refCategoryCode = refCategoryCode;
    }

    /**
     * @return the refCategorySpecies
     */
    public String getRefCategorySpecies() {
        return refCategorySpecies;
    }

    /**
     * @param refCategorySpecies the refCategorySpecies to set
     */
    public void setRefCategorySpecies(String refCategorySpecies) {
        this.refCategorySpecies = refCategorySpecies;
    }
}
