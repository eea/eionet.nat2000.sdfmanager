/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sdf_manager;

import pojos.Habitat;
import pojos.HabitatClass;
import pojos.Impact;
import pojos.OtherSpecies;
import pojos.Ownership;
import pojos.Region;
import pojos.Resp;
import pojos.SiteBiogeo;
import pojos.Species;

/**
 *
 * @author anon
 */
public class SDFVerifier {

    /**
     *
     * @param siteCode
     * @return
     */
    public static int verifySiteCode(String siteCode) {
       if (siteCode.length() != 9) {       
            return 1;
        }
       if (!siteCode.matches("^[A-Z][A-Z][A-Z0-9]*")) {            
            return 2;
        }
        return 0;
    }

    /**
     *
     * @param siteName
     * @return
     */
    public static int verifySiteName(String siteName) {
        if (siteName.length() == 0){
            return 1;
        }else{
            return 0;
        }
    }

    /**
     *
     * @param siteType
     * @return
     */
    public static int verifySiteType(String siteType) {
        if (siteType.length() != 1){
            return 1;
        }
        if (!siteType.equalsIgnoreCase("A") && !siteType.equalsIgnoreCase("B") && !siteType.equalsIgnoreCase("C")){
            return 2;
        }else{
            return 0;
        }
    }

    /**
     *
     * @param date
     * @return
     */
    public static int verifyDate(String date) {
        if (!date.matches("^[0-9]{4}-[0-9]{2}[\\-0-9]{0,3}")){
            return 1;
        }
        return 0;
    }

    /**
     *
     * @param val
     * @return
     */
    public static int verifyStringNotEmpty(String val) {
        if (val.length() != 0){
            return 0;
        }
        else{
            return 1;
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public static int verifyCoordinate(String val) {
        return verifyDouble(val);
    }

    /**
     *
     * @param val
     * @return
     */
    public static int verifyDouble(String val) {
        try {
            Double d = Double.parseDouble(val);
            return 0;
        }
        catch (Exception e) {
            return 1;
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public static int verifyChar(String val) {
        if (val.length() == 1){
            return 0;
        }
        else{
            return 1;
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public static int verifyInt(String val) {
        try {
            int d = Integer.parseInt(val);
            return 0;
        }
        catch (Exception e) {
            return 1;
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public static int verifyBoolean(String val) {
        if (val != null && (val.equalsIgnoreCase("TRUE") || val.equalsIgnoreCase("FALSE"))){
            return 0;
        }
        else{
            return 1;
        }
    }

    /**
     *
     * @param resp
     * @return
     */
    public static int checkRespondent (Resp resp) {
        //check if Respondent object worth saving
        boolean res = resp.getRespName() != null && resp.getRespAddress() != null &&
                resp.getRespEmail() != null && !resp.getRespName().equals("") &&
                ! resp.getRespAddress().equals("") && !resp.getRespEmail().equals("");
        if (res){
            return 0;
        }
        else{
            return 1;
        }
    }

    /**
     *
     * @param region
     * @return
     */
    public static int checkRegion (Region region) {
        boolean res = region.getRegionCode() != null && region.getRegionName() != null &&
                !region.getRegionCode().equals("") && !region.getRegionName().equals("");
        if (res){
            return 0;
        }
        else{
            return 1;
        }
    }

    /**
     *
     * @param siteBiogeo
     * @return
     */
    public static int checkBioRegion (SiteBiogeo siteBiogeo) {        
        if (siteBiogeo.getBiogeo() == null){
            return 1;
        }
        else{
            return 0;
        }
    }

    /**
     *
     * @param habitat
     * @return
     */
    public static int checkHabitat (Habitat habitat) {        
        if (habitat.getHabitatCode() != null){
            return 0;
        }
        else {
            return 1;
        }
    }

    /**
     *
     * @param species
     * @return
     */
    public static int checkSpecies (Species species) {        
        if (species.getSpeciesCode() != null){
            return 0;
        }
        else{
            return 1;
        }
    }

    /***
     *
     * @param species
     * @return
     */
    public static int checkOSpecies (OtherSpecies species) {        
        if (species.getOtherSpeciesCode() != null){
            return 0;
        }
        else {
            return 1;
        }
    }

    /**
     *
     * @param hClass
     * @return
     */
    public static int checkhClass (HabitatClass hClass) {        
        if (hClass.getHabitatClassCode() != null){
            return 0;
        }
        else{
            return 1;
        }
    }

    /**
     *
     * @param impact
     * @return
     */
    public static int checkImpact (Impact impact) {        
        if (impact.getImpactCode() != null){
            return 0;
        }
        else{
            return 1;
        }
    }

    /**
     * 
     * @param o
     * @return
     */
    public static int checkOwnership (Ownership o) {        
        return 0;
    }
}
