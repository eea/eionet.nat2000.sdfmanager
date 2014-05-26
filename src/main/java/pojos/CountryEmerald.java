
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pojos;


public class CountryEmerald implements java.io.Serializable {


     private int countryId;
     private String countryCode;
     private String countryName;

   /**
    *
    */
    public CountryEmerald() {
    }

    /**
     *
     * @param docId
     */
    public CountryEmerald(int countryId) {
        this.countryId = countryId;
    }

    /**
     * @return the cuntryId
     */
    public int getCountryId() {
        return countryId;
    }

    /**
     * @param cuntryId the cuntryId to set
     */
    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    /**
     * @return the countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * @param countryCode the countryCode to set
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @return the countryName
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * @param countryName the countryName to set
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}


