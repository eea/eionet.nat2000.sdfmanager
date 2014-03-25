package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;




/**
 * MgmtBody generated by hbm2java
 */
public class MgmtBody  implements java.io.Serializable {


     private int mgmtBodyId;
     private Mgmt mgmt;
     private String mgmtBodyOrg;
     private String mgmtBodyAddress;
     private String mgmtBodyEmail;
     private String mgmtBodyAdminUnit;
     private String mgmtBodyLocatorDesignator;
     private String mgmtBodyLocatorName;
     private String mgmtBodyAddressArea;
     private String mgmtBodyPostCode;
     private String mgmtBodyPostName;
     private String mgmtBodyThroughFare;
     private Set mgmtAddressArea = new HashSet(0);


    public MgmtBody() {
    }


    public MgmtBody(int mgmtBodyId) {
        this.mgmtBodyId = mgmtBodyId;
    }
    public MgmtBody(int mgmtBodyId, Mgmt mgmt, String mgmtBodyOrg, String mgmtBodyAddress, String mgmtBodyEmail,String mgmtBodyAdminUnit,String mgmtBodyLocatorDesignator,String mgmtBodyLocatorName,String mgmtBodyAddressArea,String mgmtBodyPostCode,String mgmtBodyPostName,String mgmtBodyThroughFare) {
       this.mgmtBodyId = mgmtBodyId;
       this.mgmt = mgmt;
       this.mgmtBodyOrg = mgmtBodyOrg;
       this.mgmtBodyAddress = mgmtBodyAddress;
       this.mgmtBodyEmail = mgmtBodyEmail;
       this.mgmtBodyAdminUnit = mgmtBodyAdminUnit;
       this.mgmtBodyLocatorDesignator = mgmtBodyLocatorDesignator;
       this.mgmtBodyLocatorName = mgmtBodyLocatorName;
       this.mgmtBodyAddressArea = mgmtBodyAddressArea;
       this.mgmtBodyPostCode = mgmtBodyPostCode;
       this.mgmtBodyPostName = mgmtBodyPostName;
       this.mgmtBodyThroughFare = mgmtBodyThroughFare;

    }

    public int getMgmtBodyId() {
        return this.mgmtBodyId;
    }

    public void setMgmtBodyId(int mgmtBodyId) {
        this.mgmtBodyId = mgmtBodyId;
    }
    public Mgmt getMgmt() {
        return this.mgmt;
    }

    public void setMgmt(Mgmt mgmt) {
        this.mgmt = mgmt;
    }
    public String getMgmtBodyOrg() {
        return this.mgmtBodyOrg;
    }

    public void setMgmtBodyOrg(String mgmtBodyOrg) {
        this.mgmtBodyOrg = mgmtBodyOrg;
    }
    public String getMgmtBodyAddress() {
        return this.mgmtBodyAddress;
    }

    public void setMgmtBodyAddress(String mgmtBodyAddress) {
        this.mgmtBodyAddress = mgmtBodyAddress;
    }
    public String getMgmtBodyEmail() {
        return this.mgmtBodyEmail;
    }

    public void setMgmtBodyEmail(String mgmtBodyEmail) {
        this.mgmtBodyEmail = mgmtBodyEmail;
    }

     /**
     * @return the mgmtAdminUnit
     */
    public String getMgmtBodyAdminUnit() {
        return mgmtBodyAdminUnit;
    }

    /**
     * @param mgmtAdminUnit the mgmtAdminUnit to set
     */
    public void setMgmtBodyAdminUnit(String mgmtBodyAdminUnit) {
        this.mgmtBodyAdminUnit = mgmtBodyAdminUnit;
    }

    /**
     * @return the mgmtLocatorDesignator
     */
    public String getMgmtBodyLocatorDesignator() {
        return mgmtBodyLocatorDesignator;
    }

    /**
     * @param mgmtLocatorDesignator the mgmtLocatorDesignator to set
     */
    public void setMgmtBodyLocatorDesignator(String mgmtBodyLocatorDesignator) {
        this.mgmtBodyLocatorDesignator = mgmtBodyLocatorDesignator;
    }

    /**
     * @return the mgmtLocatorName
     */
    public String getMgmtBodyLocatorName() {
        return mgmtBodyLocatorName;
    }

    /**
     * @param mgmtLocatorName the mgmtLocatorName to set
     */
    public void setMgmtBodyLocatorName(String mgmtBodyLocatorName) {
        this.mgmtBodyLocatorName = mgmtBodyLocatorName;
    }

    /**
     * @return the mgmtAddressArea
     */
    public String getMgmtBodyAddressArea() {
        return mgmtBodyAddressArea;
    }

    /**
     * @param mgmtAddressArea the mgmtAddressArea to set
     */
    public void setMgmtBodyAddressArea(String mgmtBodyAddressArea) {
        this.mgmtBodyAddressArea = mgmtBodyAddressArea;
    }

    /**
     * @return the mgmtPostCode
     */
    public String getMgmtBodyPostCode() {
        return mgmtBodyPostCode;
    }

    /**
     * @param mgmtPostCode the mgmtPostCode to set
     */
    public void setMgmtBodyPostCode(String mgmtBodyPostCode) {
        this.mgmtBodyPostCode = mgmtBodyPostCode;
    }

    /**
     * @return the mgmtPostName
     */
    public String getMgmtBodyPostName() {
        return mgmtBodyPostName;
    }

    /**
     * @param mgmtPostName the mgmtPostName to set
     */
    public void setMgmtBodyPostName(String mgmtBodyPostName) {
        this.mgmtBodyPostName = mgmtBodyPostName;
    }

    /**
     * @return the mgmtThroughFare
     */
    public String getMgmtBodyThroughFare() {
        return mgmtBodyThroughFare;
    }

    /**
     * @param mgmtThroughFare the mgmtThroughFare to set
     */
    public void setMgmtBodyThroughFare(String mgmtBodyThroughFare) {
        this.mgmtBodyThroughFare = mgmtBodyThroughFare;
    }

    /**
     * @return the mgmtAddressArea
     */
    public Set getMgmtAddressArea() {
        return mgmtAddressArea;
    }

    /**
     * @param mgmtAddressArea the mgmtAddressArea to set
     */
    public void setMgmtAddressArea(Set mgmtAddressArea) {
        this.mgmtAddressArea = mgmtAddressArea;
    }
}

