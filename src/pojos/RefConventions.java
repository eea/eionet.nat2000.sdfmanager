/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pojos;

/**
 *
 * @author charbda
 */
public class RefConventions {

    private int refConventionsId;
    private String refConventionsName;
    private String refConventionsCode;

    public RefConventions() {

    }

    public RefConventions(int refConventionsId, String refConventionsName) {
        this.refConventionsId = refConventionsId;
        this.refConventionsName = refConventionsName;
    }

    public int getRefConventionsId() {
        return refConventionsId;
    }

    public void setRefConventionsId(int refConventionsId) {
        this.refConventionsId = refConventionsId;
    }

    public String getRefConventionsName() {
        return refConventionsName;
    }

    public void setRefConventionsName(String refConventionsName) {
        this.refConventionsName = refConventionsName;
    }

    /**
     * @return the refConventionsCode
     */
    public String getRefConventionsCode() {
        return refConventionsCode;
    }

    /**
     * @param refConventionsCode the refConventionsCode to set
     */
    public void setRefConventionsCode(String refConventionsCode) {
        this.refConventionsCode = refConventionsCode;
    }
}
