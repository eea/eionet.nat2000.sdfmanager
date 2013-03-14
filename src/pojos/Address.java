package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA


import java.util.HashSet;
import java.util.Set;

/**
 * address generated by hbm2java
 */
public class Address  implements java.io.Serializable {


     private int addressId;
     private String addressAdminUnit;
     private String addressLocatorDesig;
     private String addressLocatorName;
     private String addressAddressArea;
     private String addressPostName;
     private String addressPostCode;
     private String addressThoroughFare;
     private Set resp = new HashSet(0);
     private Set mgmtBody = new HashSet(0);

     /**
      * Constructor
      */
     public Address() {
     }

     /**
      *
      * @param addressId
      */
     public Address(int addressId) {
        this.addressId = addressId;
     }
     /**
      *
      * @param addressId
      * @param addressName
      * @param addressAddress
      * @param addressEmail
      * @param addressAdminUnit
      * @param addressLocatorDesig
      * @param addressLocatorName
      * @param addressAddressArea
      * @param addressPostName
      * @param addressPostCode
      * @param addressThoroughFare
      * @param resp
      * @param mgmtBody
      */
     public Address(int addressId, String addressName, String addressAddress, String addressEmail,String addressAdminUnit, String addressLocatorDesig,String addressLocatorName,String addressAddressArea,String addressPostName,String addressPostCode,String addressThoroughFare,Set resp, Set mgmtBody) {
       this.addressId = addressId;
       this.addressAdminUnit = addressAdminUnit;
       this.addressLocatorDesig = addressLocatorDesig;
       this.addressLocatorName = addressLocatorName;
       this.addressAddressArea = addressAddressArea;
       this.addressPostName = addressPostName;
       this.addressPostCode = addressPostCode;
       this.addressThoroughFare = addressThoroughFare;
       this.resp = resp;
       this.mgmtBody = mgmtBody;
    }
    /**
     *
     * @return Address indentifier
     */
    public int getAddressId() {
        return this.addressId;
    }
    /**
     *
     * @param addressId
     */
    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }
    /**
     *
     * @return set of respondant of the site
     */
    public Set getResp() {
        return this.resp;
    }
    /**
     *
     * @param resp
     */
    public void setResp(Set resp) {
        this.resp = resp;
    }


    /**
     * @return the addressAdminUnit
     */
    public String getAddressAdminUnit() {
        return addressAdminUnit;
    }

    /**
     * @return the addressLocatorDesig
     */
    public String getAddressLocatorDesig() {
        return addressLocatorDesig;
    }

    /**
     * @return the addressLocatorNamr
     */
    public String getAddressLocatorNamr() {
        return getAddressLocatorName();
    }

    /**
     * @return the addressAddressArea
     */
    public String getAddressAddressArea() {
        return addressAddressArea;
    }

    /**
     * @return the addressPostName
     */
    public String getAddressPostName() {
        return addressPostName;
    }

    /**
     * @return the addressPostCode
     */
    public String getAddressPostCode() {
        return addressPostCode;
    }

    /**
     * @param addressPostCode the addressPostCode to set
     */
    public void setAddressPostCode(String addressPostCode) {
        this.addressPostCode = addressPostCode;
    }

    /**
     * @return the addressThoroughFare
     */
    public String getAddressThoroughFare() {
        return addressThoroughFare;
    }

    /**
     * @param addressAdminUnit the addressAdminUnit to set
     */
    public void setAddressAdminUnit(String addressAdminUnit) {
        this.addressAdminUnit = addressAdminUnit;
    }

    /**
     * @param addressLocatorDesig the addressLocatorDesig to set
     */
    public void setAddressLocatorDesig(String addressLocatorDesig) {
        this.addressLocatorDesig = addressLocatorDesig;
    }

    /**
     * @return the addressLocatorName
     */
    public String getAddressLocatorName() {
        return addressLocatorName;
    }

    /**
     * @param addressLocatorName the addressLocatorName to set
     */
    public void setAddressLocatorName(String addressLocatorName) {
        this.addressLocatorName = addressLocatorName;
    }

    /**
     * @param addressAddressArea the addressAddressArea to set
     */
    public void setAddressAddressArea(String addressAddressArea) {
        this.addressAddressArea = addressAddressArea;
    }

    /**
     * @param addressPostName the addressPostName to set
     */
    public void setAddressPostName(String addressPostName) {
        this.addressPostName = addressPostName;
    }

    /**
     * @param addressThoroughFare the addressThoroughFare to set
     */
    public void setAddressThoroughFare(String addressThoroughFare) {
        this.addressThoroughFare = addressThoroughFare;
    }

}


