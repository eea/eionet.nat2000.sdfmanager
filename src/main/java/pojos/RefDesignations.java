package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA



/**
 * RefDesignations generated by hbm2java
 */
public class RefDesignations implements java.io.Serializable {


     private int refDesignationsId;
     private String refDesignationsCode;
     private String refDesignationsDescr;
     private Character refDesignationsCategory;

    public RefDesignations() {
    }


    public RefDesignations(int refDesignationsId) {
        this.refDesignationsId = refDesignationsId;
    }
    public RefDesignations(int refDesignationsId, String refDesignationsCode, String refDesignationsDescr, Character refDesignationsCategory) {
       this.refDesignationsId = refDesignationsId;
       this.refDesignationsCode = refDesignationsCode;
       this.refDesignationsDescr = refDesignationsDescr;
       this.refDesignationsCategory = refDesignationsCategory;
    }

    public int getRefDesignationsId() {
        return this.refDesignationsId;
    }

    public void setRefDesignationsId(int refDesignationsId) {
        this.refDesignationsId = refDesignationsId;
    }
    public String getRefDesignationsCode() {
        return this.refDesignationsCode;
    }

    public void setRefDesignationsCode(String refDesignationsCode) {
        this.refDesignationsCode = refDesignationsCode;
    }
    public String getRefDesignationsDescr() {
        return this.refDesignationsDescr;
    }

    public void setRefDesignationsDescr(String refDesignationsDescr) {
        this.refDesignationsDescr = refDesignationsDescr;
    }
    public Character getRefDesignationsCategory() {
        return this.refDesignationsCategory;
    }

    public void setRefDesignationsCategory(Character refDesignationsCategory) {
        this.refDesignationsCategory = refDesignationsCategory;
    }
}


