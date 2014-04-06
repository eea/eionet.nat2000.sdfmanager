package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA




/**
 * SiteOwnership generated by hbm2java
 */
public class SiteOwnership implements java.io.Serializable{


     private SiteOwnershipId id;
     private Ownership ownership;
     private Site site;
     private Double ownershipPercent;

     

    public SiteOwnership() {
    }


    public SiteOwnership(SiteOwnershipId id, Ownership ownership, Site site) {
        this.id = id;
        this.ownership = ownership;
        this.site = site;
    }
    public SiteOwnership(SiteOwnershipId id, Ownership ownership, Site site, Double ownershipPercent) {
       this.id = id;
       this.ownership = ownership;
       this.site = site;
       this.ownershipPercent = ownershipPercent;
    }

    public SiteOwnershipId getId() {
        return this.id;
    }

    public void setId(SiteOwnershipId id) {
        this.id = id;
    }
    public Ownership getOwnership() {
        return this.ownership;
    }

    public void setOwnership(Ownership ownership) {
        this.ownership = ownership;
    }
    public Site getSite() {
        return this.site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
    public Double getOwnershipPercent() {
        return this.ownershipPercent;
    }

    public void setOwnershipPercent(Double ownershipPercent) {
        this.ownershipPercent = ownershipPercent;
    }

       
}


