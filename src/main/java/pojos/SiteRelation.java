package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA




/**
 * SiteRelation generated by hbm2java
 */
public class SiteRelation implements java.io.Serializable {


    private int siteRelationId;
    private Site site;
    private Character siteRelationScope;
    private String siteRelationCode;
    private String siteRelationConvention;
    private String siteRelationSitename;
    private Double siteRelationCover;
    private Character siteRelationType;

    public SiteRelation() {
    }


    public SiteRelation(int siteRelationId) {
        this.siteRelationId = siteRelationId;
    }
    public SiteRelation(int siteRelationId, Site site, Character siteRelationScope, String siteRelationCode, String siteRelationConvention, String siteRelationSitename, Double siteRelationCover, Character siteRelationType) {
       this.siteRelationId = siteRelationId;
       this.site = site;
       this.siteRelationScope = siteRelationScope;
       this.siteRelationCode = siteRelationCode;
       this.siteRelationConvention = siteRelationConvention;
       this.siteRelationSitename = siteRelationSitename;
       this.siteRelationCover = siteRelationCover;
       this.siteRelationType = siteRelationType;
    }

    public int getSiteRelationId() {
        return this.siteRelationId;
    }

    public void setSiteRelationId(int siteRelationId) {
        this.siteRelationId = siteRelationId;
    }
    public Site getSite() {
        return this.site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
    public Character getSiteRelationScope() {
        return this.siteRelationScope;
    }

    public void setSiteRelationScope(Character siteRelationScope) {
        this.siteRelationScope = siteRelationScope;
    }
    public String getSiteRelationCode() {
        return this.siteRelationCode;
    }

    public void setSiteRelationCode(String siteRelationCode) {
        this.siteRelationCode = siteRelationCode;
    }
    public String getSiteRelationConvention() {
        return this.siteRelationConvention;
    }

    public void setSiteRelationConvention(String siteRelationConvention) {
        this.siteRelationConvention = siteRelationConvention;
    }
    public String getSiteRelationSitename() {
        return this.siteRelationSitename;
    }

    public void setSiteRelationSitename(String siteRelationSitename) {
        this.siteRelationSitename = siteRelationSitename;
    }
    public Double getSiteRelationCover() {
        return this.siteRelationCover;
    }

    public void setSiteRelationCover(Double siteRelationCover) {
        this.siteRelationCover = siteRelationCover;
    }
    public Character getSiteRelationType() {
        return this.siteRelationType;
    }

    public void setSiteRelationType(Character siteRelationType) {
        this.siteRelationType = siteRelationType;
    }

}


