package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA



/**
 * SiteBiogeoId generated by hbm2java
 */
public class SiteBiogeoId  implements java.io.Serializable {


     private String siteCode;
     private int biogeoId;

    public SiteBiogeoId() {
    }

    public SiteBiogeoId(String siteCode, int biogeoId) {
       this.siteCode = siteCode;
       this.biogeoId = biogeoId;
    }

    public String getSiteCode() {
        return this.siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }
    public int getBiogeoId() {
        return this.biogeoId;
    }

    public void setBiogeoId(int biogeoId) {
        this.biogeoId = biogeoId;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof SiteBiogeoId) ) return false;
		 SiteBiogeoId castOther = ( SiteBiogeoId ) other;

		 return ( (this.getSiteCode()==castOther.getSiteCode()) || ( this.getSiteCode()!=null && castOther.getSiteCode()!=null && this.getSiteCode().equals(castOther.getSiteCode()) ) )
 && (this.getBiogeoId()==castOther.getBiogeoId());
   }

   public int hashCode() {
         int result = 17;

         result = 37 * result + ( getSiteCode() == null ? 0 : this.getSiteCode().hashCode() );
         result = 37 * result + this.getBiogeoId();
         return result;
   }


}


