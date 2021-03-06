package pojos;
// Generated 01-fvr.-2011 13:10:18 by Hibernate Tools 3.2.1.GA




/**
 * Species generated by hbm2java
 */
public class Species implements java.io.Serializable, Comparable {


    private int speciesId;
    private Site site;
    private Character speciesGroup;
    private String speciesCode;
    private String speciesName;
    private Short speciesSensitive;
    private Short speciesNp;
    private Character speciesType;
    private Integer speciesSizeMin;
    private Integer speciesSizeMax;
    private String speciesUnit;
    private Character speciesCategory;
    private String speciesDataQuality;
    private Character speciesPopulation;
    private Character speciesConservation;
    private Character speciesIsolation;
    private Character speciesGlobal;

    public Species() {
    }


    public Species(int speciesId) {
        this.speciesId = speciesId;
    }

    public Species(int speciesId, Site site, Character speciesGroup, String speciesCode, String speciesName, Short speciesSensitive, Short speciesNp, Character speciesType, Integer speciesSizeMin, Integer speciesSizeMax, String speciesUnit, Character speciesCategory, String speciesDataQuality, Character speciesPopulation, Character speciesConservation, Character speciesIsolation, Character speciesGlobal) {
       this.speciesId = speciesId;
       this.site = site;
       this.speciesGroup = speciesGroup;
       this.speciesCode = speciesCode;
       this.speciesName = speciesName;
       this.speciesSensitive = speciesSensitive;
       this.speciesNp = speciesNp;
       this.speciesType = speciesType;
       this.speciesSizeMin = speciesSizeMin;
       this.speciesSizeMax = speciesSizeMax;
       this.speciesUnit = speciesUnit;
       this.speciesCategory = speciesCategory;
       this.speciesDataQuality = speciesDataQuality;
       this.speciesPopulation = speciesPopulation;
       this.speciesConservation = speciesConservation;
       this.speciesIsolation = speciesIsolation;
       this.speciesGlobal = speciesGlobal;
    }

    public int getSpeciesId() {
        return this.speciesId;
    }

    public void setSpeciesId(int speciesId) {
        this.speciesId = speciesId;
    }
    public Site getSite() {
        return this.site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
    public Character getSpeciesGroup() {
        return this.speciesGroup;
    }

    public void setSpeciesGroup(Character speciesGroup) {
        this.speciesGroup = speciesGroup;
    }
    public String getSpeciesCode() {
        return this.speciesCode;
    }

    public void setSpeciesCode(String speciesCode) {
        this.speciesCode = speciesCode;
    }
    public String getSpeciesName() {
        return this.speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }
    public Short getSpeciesSensitive() {
        return this.speciesSensitive;
    }

    public void setSpeciesSensitive(Short speciesSensitive) {
        this.speciesSensitive = speciesSensitive;
    }
    public Short getSpeciesNp() {
        return this.speciesNp;
    }

    public void setSpeciesNp(Short speciesNp) {
        this.speciesNp = speciesNp;
    }
    public Character getSpeciesType() {
        return this.speciesType;
    }

    public void setSpeciesType(Character speciesType) {
        this.speciesType = speciesType;
    }
    public Integer getSpeciesSizeMin() {
        return this.speciesSizeMin;
    }

    public void setSpeciesSizeMin(Integer speciesSizeMin) {
        this.speciesSizeMin = speciesSizeMin;
    }
    public Integer getSpeciesSizeMax() {
        return this.speciesSizeMax;
    }

    public void setSpeciesSizeMax(Integer speciesSizeMax) {
        this.speciesSizeMax = speciesSizeMax;
    }
    public String getSpeciesUnit() {
        return this.speciesUnit;
    }

    public void setSpeciesUnit(String speciesUnit) {
        this.speciesUnit = speciesUnit;
    }
    public Character getSpeciesCategory() {
        return this.speciesCategory;
    }

    public void setSpeciesCategory(Character speciesCategory) {
        this.speciesCategory = speciesCategory;
    }
    public String getSpeciesDataQuality() {
        return this.speciesDataQuality;
    }

    public void setSpeciesDataQuality(String speciesDataQuality) {
        this.speciesDataQuality = speciesDataQuality;
    }
    public Character getSpeciesPopulation() {
        return this.speciesPopulation;
    }

    public void setSpeciesPopulation(Character speciesPopulation) {
        this.speciesPopulation = speciesPopulation;
    }
    public Character getSpeciesConservation() {
        return this.speciesConservation;
    }

    public void setSpeciesConservation(Character speciesConservation) {
        this.speciesConservation = speciesConservation;
    }
    public Character getSpeciesIsolation() {
        return this.speciesIsolation;
    }

    public void setSpeciesIsolation(Character speciesIsolation) {
        this.speciesIsolation = speciesIsolation;
    }
    public Character getSpeciesGlobal() {
        return this.speciesGlobal;
    }

    public void setSpeciesGlobal(Character speciesGlobal) {
        this.speciesGlobal = speciesGlobal;
    }

    @Override
    public String toString() {
        return getSpeciesCode();
    }

    public int compareTo(Object o) {
        Species r = (Species) o;
        if (this.getSpeciesName() != null) {
            return this.getSpeciesName().compareTo(r.getSpeciesName());
        } else {
            return 0;
        }

    }

}


