package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA


import java.util.HashSet;
import java.util.Set;

/**
 * Map generated by hbm2java
 */
public class Map implements java.io.Serializable {


     private int mapId;
     private String mapInspire;
     private Short mapPdf;
     private String mapReference;
     private Set sites = new HashSet(0);
    /**
     * 
     */
    public Map() {
    }

    /**
     *
     * @param mapId
     */
    public Map(int mapId) {
        this.mapId = mapId;
    }
    /**
     *
     * @param mapId
     * @param mapInspire
     * @param mapPdf
     * @param mapReference
     * @param sites
     */
    public Map(int mapId, String mapInspire, Short mapPdf, String mapReference, Set sites) {
       this.mapId = mapId;
       this.mapInspire = mapInspire;
       this.mapPdf = mapPdf;
       this.mapReference = mapReference;
       this.sites = sites;
    }
    /**
     *
     * @return
     */
    public int getMapId() {
        return this.mapId;
    }
    /**
     *
     * @param mapId
     */
    public void setMapId(int mapId) {
        this.mapId = mapId;
    }
    /**
     *
     * @return
     */
    public String getMapInspire() {
        return this.mapInspire;
    }
    /**
     *
     * @param mapInspire
     */
    public void setMapInspire(String mapInspire) {
        this.mapInspire = mapInspire;
    }
    /**
     *
     * @return
     */
    public Short getMapPdf() {
        return this.mapPdf;
    }
    /**
     *
     * @param mapPdf
     */
    public void setMapPdf(Short mapPdf) {
        this.mapPdf = mapPdf;
    }
    /**
     *
     * @return
     */
    public String getMapReference() {
        return this.mapReference;
    }
    /**
     *
     * @param mapReference
     */
    public void setMapReference(String mapReference) {
        this.mapReference = mapReference;
    }
    /**
     *
     * @return
     */
    public Set getSites() {
        return this.sites;
    }
    /**
     *
     * @param sites
     */
    public void setSites(Set sites) {
        this.sites = sites;
    }

}


