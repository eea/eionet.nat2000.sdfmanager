package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA




/**
 * Region generated by hbm2java
 */
public class Region implements java.io.Serializable, Comparable {


     private int regionId;
     private Site site;
     private String regionCode;
     private String regionName;
     
    public Region() {
    }


    public Region(int regionId) {
        this.regionId = regionId;
    }
    public Region(int regionId, Site site, String regionCode, String regionName) {
       this.regionId = regionId;
       this.site = site;
       this.regionCode = regionCode;
       this.regionName = regionName;
    }

    public int getRegionId() {
        return this.regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }
    public Site getSite() {
        return this.site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
    public String getRegionCode() {
        return this.regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }
    public String getRegionName() {
        return this.regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
    @Override
    public String toString() {
        return getRegionCode();
    }

    public int compareTo(Object o) {
        Region r = (Region) o;
        return this.getRegionCode().compareTo(r.getRegionCode());
    }

   }


