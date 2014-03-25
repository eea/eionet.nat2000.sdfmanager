/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pojos;


public class RefPopulation {
    private int refPopulationId;
    private String refPopulationName;
    private String refPopulationCode;

    /**
     *
     */
    public RefPopulation(){

    }

    /**
     * @return the refPopulationId
     */
    public int getRefPopulationId() {
        return refPopulationId;
    }

    /**
     * @param refPopulationId the refPopulationId to set
     */
    public void setRefPopulationId(int refPopulationId) {
        this.refPopulationId = refPopulationId;
    }

    /**
     * @return the refPopulationName
     */
    public String getRefPopulationName() {
        return refPopulationName;
    }

    /**
     * @param refPopulationName the refPopulationName to set
     */
    public void setRefPopulationName(String refPopulationName) {
        this.refPopulationName = refPopulationName;
    }

    /**
     * @return the refPopulationCode
     */
    public String getRefPopulationCode() {
        return refPopulationCode;
    }

    /**
     * @param refPopulationCode the refPopulationCode to set
     */
    public void setRefPopulationCode(String refPopulationCode) {
        this.refPopulationCode = refPopulationCode;
    }
}