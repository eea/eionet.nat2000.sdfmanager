package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA




/**
 * SiteBiogeo generated by hbm2java
 */
public class SiteBiogeo implements java.io.Serializable {


     private SiteBiogeoId id;
     private Biogeo biogeo;
     private Site site;
     private Double biogeoPercent;
    
    public SiteBiogeo() {
    }


    public SiteBiogeo(SiteBiogeoId id, Biogeo biogeo, Site site) {
        this.id = id;
        this.biogeo = biogeo;
        this.site = site;
    }
    public SiteBiogeo(SiteBiogeoId id, Biogeo biogeo, Site site, Double biogeoPercent) {
       this.id = id;
       this.biogeo = biogeo;
       this.site = site;
       this.biogeoPercent = biogeoPercent;
    }

    public SiteBiogeoId getId() {
        return this.id;
    }

    public void setId(SiteBiogeoId id) {
        this.id = id;
    }
    public Biogeo getBiogeo() {
        return this.biogeo;
    }

    public void setBiogeo(Biogeo biogeo) {
        this.biogeo = biogeo;
    }
    public Site getSite() {
        return this.site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
    public Double getBiogeoPercent() {
        return this.biogeoPercent;
    }

    public void setBiogeoPercent(Double biogeoPercent) {
        this.biogeoPercent = biogeoPercent;
    }

    }


