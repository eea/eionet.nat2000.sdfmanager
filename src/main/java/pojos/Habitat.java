package pojos;
// Generated 01-fevr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA



/**
 * Habitat generated by hbm2java
 */
public class Habitat implements java.io.Serializable, Comparable {


     private int habitatId;
     private Site site;
     private String habitatCode;
     private Short habitatPriority;
     private Short habitatNp;
     private Double habitatCover;
     private Integer habitatCaves;
     private String habitatDataQuality;
     private Character habitatRepresentativity;
     private Character habitatRelativeSurface;
     private Character habitatConservation;
     private Character habitatGlobal;
     private Double habitatCoverHa;

    /**
     *
     */
    public Habitat() {
    }

    /**
     *
     * @param habitatId
     */
    public Habitat(int habitatId) {
        this.habitatId = habitatId;
    }
    /**
     *
     * @param habitatId
     * @param site
     * @param habitatCode
     * @param habitatPriority
     * @param habitatNp
     * @param habitatCover
     * @param habitatCaves
     * @param habitatDataQuality
     * @param habitatRepresentativity
     * @param habitatRelativeSurface
     * @param habitatConservation
     * @param habitatGlobal
     */
    public Habitat(int habitatId, Site site, String habitatCode, Short habitatPriority, Short habitatNp, Double habitatCover, Integer habitatCaves, String habitatDataQuality, Character habitatRepresentativity, Character habitatRelativeSurface, Character habitatConservation, Character habitatGlobal) {
       this.habitatId = habitatId;
       this.site = site;
       this.habitatCode = habitatCode;
       this.habitatPriority = habitatPriority;
       this.habitatNp = habitatNp;
       this.habitatCover = habitatCover;
       this.habitatCaves = habitatCaves;
       this.habitatDataQuality = habitatDataQuality;
       this.habitatRepresentativity = habitatRepresentativity;
       this.habitatRelativeSurface = habitatRelativeSurface;
       this.habitatConservation = habitatConservation;
       this.habitatGlobal = habitatGlobal;
    }
    /**
     *
     * @return The identifier of the habitat
     */
    public int getHabitatId() {
        return this.habitatId;
    }
    /**
     *
     * @param habitatId
     */
    public void setHabitatId(int habitatId) {
        this.habitatId = habitatId;
    }
    /**
     *
     * @return The site
     */
    public Site getSite() {
        return this.site;
    }
    /**
     *
     * @param site
     */
    public void setSite(Site site) {
        this.site = site;
    }
    /**
     *
     * @return
     */
    public String getHabitatCode() {
        return this.habitatCode;
    }
    /**
     *
     * @param habitatCode
     */
    public void setHabitatCode(String habitatCode) {
        this.habitatCode = habitatCode;
    }
    /**
     *
     * @return The priority of the habitat
     */
    public Short getHabitatPriority() {
        return this.habitatPriority;
    }
    /**
     *
     * @param habitatPriority
     */
    public void setHabitatPriority(Short habitatPriority) {
        this.habitatPriority = habitatPriority;
    }
    /**
     *
     * @return The NP of the habitat
     */
    public Short getHabitatNp() {
        return this.habitatNp;
    }
    /**
     *
     * @param habitatNp
     */
    public void setHabitatNp(Short habitatNp) {
        this.habitatNp = habitatNp;
    }
    /**
     *
     * @return The cover of the habitat
     */
    public Double getHabitatCover() {
        return this.habitatCover;
    }
    /**
     *
     * @param habitatCover
     */
    public void setHabitatCover(Double habitatCover) {
        this.habitatCover = habitatCover;
    }
    /**
     *
     * @return The caves of the habitat
     */
    public Integer getHabitatCaves() {
        return this.habitatCaves;
    }
    /**
     *
     * @param habitatCaves
     */
    public void setHabitatCaves(Integer habitatCaves) {
        this.habitatCaves = habitatCaves;
    }
    /**
     *
     * @return The data quality of the habitat
     */
    public String getHabitatDataQuality() {
        return this.habitatDataQuality;
    }
    /**
     *
     * @param habitatDataQuality
     */
    public void setHabitatDataQuality(String habitatDataQuality) {
        this.habitatDataQuality = habitatDataQuality;
    }
    /**
     *
     * @return The representativity of the habitat
     */
    public Character getHabitatRepresentativity() {
        return this.habitatRepresentativity;
    }
    /**
     *
     * @param habitatRepresentativity
     */
    public void setHabitatRepresentativity(Character habitatRepresentativity) {
        this.habitatRepresentativity = habitatRepresentativity;
    }
    /**
     *
     * @return The relative surface of the habitat
     */
    public Character getHabitatRelativeSurface() {
        return this.habitatRelativeSurface;
    }
    /**
     *
     * @param habitatRelativeSurface
     */
    public void setHabitatRelativeSurface(Character habitatRelativeSurface) {
        this.habitatRelativeSurface = habitatRelativeSurface;
    }
    /**
     *
     * @return The conservation of the habitat
     */
    public Character getHabitatConservation() {
        return this.habitatConservation;
    }
    /**
     *
     * @param habitatConservation
     */
    public void setHabitatConservation(Character habitatConservation) {
        this.habitatConservation = habitatConservation;
    }
    /**
     *
     * @return The global of the habitat
     */
    public Character getHabitatGlobal() {
        return this.habitatGlobal;
    }
    /**
     *
     * @param habitatGlobal
     */
    public void setHabitatGlobal(Character habitatGlobal) {
        this.habitatGlobal = habitatGlobal;
    }

    @Override
    public String toString() {
        return getHabitatCode();
    }

    public int compareTo(Object o) {
        Habitat r = (Habitat) o;
        return this.getHabitatCode().compareTo(r.getHabitatCode());
    }

    /**
     * @return the habitatCoverHa
     */
    public Double getHabitatCoverHa() {
        return habitatCoverHa;
    }

    /**
     * @param habitatCoverHa the habitatCoverHa to set
     */
    public void setHabitatCoverHa(Double habitatCoverHa) {
        this.habitatCoverHa = habitatCoverHa;
    }
}


