/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import pojos.Habitat;
import pojos.Impact;
import pojos.Map;
import pojos.Mgmt;
import pojos.MgmtBody;
import pojos.OtherSpecies;
import pojos.Resp;
import pojos.Site;
import pojos.SiteRelation;
import pojos.Species;
import sdf_manager.SDF_ManagerApp;

public class ValidateSite {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ValidateSite.class.getName());

    /**
     *
     */
    public ValidateSite() {

    }

    /**
     *
     * @param site
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static ArrayList<String> validate(Site site) {

        boolean isEmeraldMode = SDF_ManagerApp.isEmeraldMode();
        ArrayList<String> errorList = new ArrayList<String>();

        try {
            Date siteCompDate = site.getSiteCompDate();
            if (siteCompDate == null) {
                errorList.add("Compilation Date, in Identification section\n");
            }

            Date siteUpdateDate = site.getSiteUpdateDate();
            if (siteCompDate != null && siteUpdateDate != null) {
                if (siteCompDate.compareTo(siteUpdateDate) > 0) {
                    errorList.add("Compilation Date and Update Date, in  Identification section\n");
                }
            }

            Resp respondent = site.getResp();
            if (respondent != null) {
                if (!isEmeraldMode) {
                    // Natura2000 mode.
                    if (StringUtils.isBlank(respondent.getRespName())) {
                        if (StringUtils.isBlank(respondent.getRespEmail())) {
                            errorList.add("Name or Email in Identification section (Respondent tab)\n");
                        }
                    }
                    if (StringUtils.isBlank(respondent.getRespAddress()) && StringUtils.isBlank(respondent.getRespAdminUnit())) {
                        errorList.add("Address in Identification section (Respondent tab)\n");
                    }
                } else {
                    // EMERALD mode.
                    if (StringUtils.isBlank(respondent.getRespAddress())) {
                        errorList.add("At least respondent address in Identification -> Respondent tab is required\n");
                    }
                }
            }

            // Check site type presence.

            Character siteType = site.getSiteType();
            if (siteType == null) {
                errorList.add("Site Type\n");
            }

            // Check site classification dates.

            if (!isEmeraldMode) {
                if (("A").equals(siteType.toString())) {
                    if (site.getSiteSpaDate() == null) {
                        errorList.add("Date site classificated as SPA in Identification section (Dates tab)\n");
                    }
                } else if (("B").equals(siteType.toString())) {
                    if (site.getSiteSciPropDate() == null) {
                        errorList.add("Date site proposed as SCI in Identification section (Dates tab)\n");
                    }
                } else {
                    if (site.getSiteSpaDate() == null) {
                        errorList.add("Date site classificated as SPA in Identification section (Dates tab)\n");
                    }
                    if (site.getSiteSciPropDate() == null) {
                        errorList.add("Date site proposed as SCI in Identification section (Dates tab)\n");
                    }
                }
            } else {
                Date proposedASCI = site.getSiteProposedAsciDate();
                Date confirmedCandidateASCI = site.getSiteConfirmedCandidateAsciDate();
                Date confirmedASCI = site.getSiteConfirmedAsciDate();
                Date designatedASCI = site.getSiteDesignatedAsciDate();

                if (!ValidateSite.isDatesAscendingOrder(proposedASCI, confirmedCandidateASCI, confirmedASCI, designatedASCI)) {
                    errorList.add("Chronologial order of site ASCI dates (Identification -> Dates)\n");
                }
            }

            // Check site regions and bio-regions.

            Set siteRegions = site.getRegions();
            if (siteRegions == null || siteRegions.isEmpty()) {
                errorList.add("Administrative Region code and name (NUTS) in Location section)\n");
            }

            Set siteBioRegions = site.getSiteBiogeos();
            if (siteBioRegions == null || siteBioRegions.isEmpty()) {
                errorList.add("Biogeographical Region in Location section)\n");
            }

            // Check habitats.

            Set siteHabitats = site.getHabitats();
            Iterator siteHabitatsIter = siteHabitats.iterator();
            boolean hasHabitatsInfo = siteHabitats != null && !siteHabitats.isEmpty();

            while (hasHabitatsInfo && siteHabitatsIter.hasNext()) {

                Habitat habitat = (Habitat) siteHabitatsIter.next();
                String habitatCode = habitat.getHabitatCode();

                if (StringUtils.isBlank(habitatCode)) {
                    errorList.add("The code of the habitat. (Ecological Info-Habitat Type  section)\n");
                }

                boolean dAssesment = false;

                Character habitatRepresentativity = habitat.getHabitatRepresentativity();
                if (habitatRepresentativity == null || (("-").equals(habitatRepresentativity.toString()))) {
                    errorList.add("Representativity of the habitat whose code is: " + habitatCode
                            + ". (Ecological Info-Habitat Type  section)\n");
                } else if (("D").equals(habitatRepresentativity.toString())) {
                    dAssesment = true;
                }

                ValidateSite.log.info("*******************************Habitat==>" + habitatCode
                        + "<==h.getHabitatRepresentativity()==>" + habitatRepresentativity + "<==");

                if (habitat.getHabitatDataQuality() == null && !dAssesment) {
                    errorList.add("Data Quality of the habitat whose code is: " + habitatCode
                            + ". (Ecological Info-Habitat Type  section)\n");
                }

                ValidateSite.log.info("Habitat==>" + habitatCode + "<==h.getHabitatRelativeSurface()==>"
                        + habitat.getHabitatRelativeSurface() + "<==");

                if (habitat.getHabitatRelativeSurface() == null && !dAssesment) {
                    errorList.add("Relative Surface of the habitat whose code is: " + habitatCode
                            + ". (Ecological Info-Habitat Type  section)\n");
                }

                ValidateSite.log.info("Habitat==>" + habitatCode + "<==h.getHabitatConservation()==>"
                        + habitat.getHabitatConservation() + "<==");

                if (habitat.getHabitatConservation() == null && !dAssesment) {
                    errorList.add("Conservation of the habitat whose code is: " + habitatCode
                            + ". (Ecological Info-Habitat Type  section)\n");
                }

                if (habitat.getHabitatGlobal() == null && !dAssesment) {
                    errorList.add("Global of the habitat whose code is: " + habitatCode
                            + ". (Ecological Info-Habitat Type  section)\n");
                }
            }

            // Check species.

            boolean speciesInfo = false;
            boolean birdsSPA = false;

            Set siteSpecies = site.getSpecieses();
            if (siteSpecies != null && !siteSpecies.isEmpty()) {

                Iterator speciesIter = siteSpecies.iterator();
                while (speciesIter.hasNext()) {

                    Species species = (Species) speciesIter.next();

                    String speciesCode = species.getSpeciesCode();
                    String speciesName = species.getSpeciesName();
                    Character speciesGroup = species.getSpeciesGroup();

                    if (!isEmeraldMode) {
                        // SPA-related stuff relevant only in non-Emerald mode.
                        if (speciesGroup != null && !(("-").equals(speciesGroup.toString()))) {
                            if ((speciesGroup.toString()).equals("B")) {
                                birdsSPA = true;
                            }
                        } else {
                            errorList.add("Group of the species. (Ecological Info - Species Type  section)\n");
                        }
                    }

                    if (StringUtils.isBlank(speciesName)) {
                        errorList.add("Scientific name of the species for the code : " + speciesCode + " and the group :"
                                + speciesGroup + ". (Ecological Info - Species Type  section)\n");
                    }

                    boolean dAssesment = false;
                    Character speciesPopulation = species.getSpeciesPopulation();

                    if (speciesPopulation != null && (("D").equals(speciesPopulation.toString()))) {
                        dAssesment = true;
                    }

                    if (species.getSpeciesDataQuality() == null && !dAssesment) {
                        errorList.add("Data Quality of the species for the code : " + speciesCode + ", species name : "
                                + speciesName + " and the group :" + speciesGroup
                                + ". (Ecological Info - Species Type  section)\n");
                    }

                    if (species.getSpeciesConservation() == null && !dAssesment) {
                        errorList.add("Conservation of the species for the code : " + speciesCode + ", species name : "
                                + speciesName + " and the group :" + speciesGroup
                                + ". (Ecological Info - Species Type  section)\n");
                    }

                    if (species.getSpeciesIsolation() == null && !dAssesment) {
                        errorList.add("Isolation of the species for the code : " + speciesCode + ", species name : " + speciesName
                                + " and the group  :" + speciesGroup + ". (Ecological Info - Species Type  section)\n");
                    }

                    if (species.getSpeciesGlobal() == null && !dAssesment) {
                        errorList.add("Global of the species for the code : " + speciesCode + ", species name : " + speciesName
                                + " and the group  :" + speciesGroup + ". (Ecological Info - Species Type  section)\n");
                    }
                }

                speciesInfo = true;
            }

            // Check other species.

            Set siteOtherSpecies = site.getOtherSpecieses();
            if (siteOtherSpecies != null && !siteOtherSpecies.isEmpty()) {

                Iterator otherSpeciesIter = siteOtherSpecies.iterator();
                while (otherSpeciesIter.hasNext()) {

                    OtherSpecies otherSpecies = (OtherSpecies) otherSpeciesIter.next();

                    String otherSpeciesCode = otherSpecies.getOtherSpeciesCode();
                    String otherSpeciesName = otherSpecies.getOtherSpeciesName();
                    String otherSpeciesGroup = otherSpecies.getOtherSpeciesGroup();

                    if (otherSpeciesGroup == null || (("-").equals(otherSpeciesGroup))) {
                        errorList.add("Group of the species (Other species). (Ecological Info - Other Species Type  section)\n");
                    }

                    if (StringUtils.isBlank(otherSpeciesName)) {
                        errorList.add("Scientific name of the species (Other species) for the code : " + otherSpeciesCode
                                + " and the group :" + otherSpeciesGroup + " . (Ecological Info - Other Species Type  section)\n");
                    }
                }
            }

            if (!hasHabitatsInfo && !speciesInfo) {
                errorList.add("Non habitats or species. (Ecological Info)\n");
            }

            if (!isEmeraldMode) {
                // SPA-related stuff relevant only in non-Emerald mode.
                if ((("A").equals(siteType.toString())) || (("C").equals(siteType.toString()))) {
                    if (!birdsSPA) {
                        errorList.add("No birds in SPA site. (Ecological Info)\n");
                    }
                }
            }

            // Check things on DESCRIPTION tab.

            boolean posImpact = false;
            boolean negImpact = false;

            Set siteImpacts = site.getImpacts();
            if (siteImpacts != null && !siteImpacts.isEmpty()) {

                Iterator impactIter = siteImpacts.iterator();
                while (impactIter.hasNext()) {

                    Impact impact = (Impact) impactIter.next();

                    Character impactType = impact.getImpactType();
                    String impactCode = impact.getImpactCode();

                    if (("P").equals(impactType.toString())) {
                        posImpact = true;
                    } else if (("N").equals(impactType.toString())) {
                        negImpact = true;
                    } else {

                    }

                    if (StringUtils.isBlank(impactCode)) {
                        errorList.add("Impact code. (Description - Pressures and Threads section)\n");
                    }

                    Character impactRank = impact.getImpactRank();
                    if (impactRank == null || (("-").equals(impactRank.toString()))) {
                        errorList.add("Rank of the impact whose code is: " + impactCode
                                + ". (Description - Pressures and Threads section)\n");
                    }

                    Character impactOccurrence = impact.getImpactOccurrence();
                    if (impactOccurrence == null || (("-").equals(impactOccurrence.toString()))) {
                        errorList.add("Occurence of the impact whose code is: " + impactCode
                                + ". (Description - Pressures and Threads section)\n");
                    }
                }

                if (!posImpact || !negImpact) {
                    errorList.add("There has to be at least one positive or negative impact.\n");
                }
            } else {
                errorList.add("Impacts. (Description - Pressures and Threads section)\n");
            }

            // Check things on PROTECTION tab.

            Set siteRelations = site.getSiteRelations();
            if (siteRelations != null && !siteRelations.isEmpty()) {

                Iterator relationsIter = siteRelations.iterator();
                while (relationsIter.hasNext()) {

                    SiteRelation relation = (SiteRelation) relationsIter.next();

                    String siteRelationSitename = relation.getSiteRelationSitename();
                    Character siteRelationType = relation.getSiteRelationType();

                    if (StringUtils.isBlank(siteRelationSitename)) {
                        errorList.add("Site Name of the Relation National Site. (Protection Status - Relation with other sites section)\n ");
                    }

                    if (siteRelationType == null) {
                        errorList.add("Relation National Site whose site name is: " + siteRelationSitename
                                + ". . (Protection Status - Relation with other sites section)\n ");
                    }
                }
            }

            // Check things on MANAGEMENT tab.

            Mgmt siteManagement = site.getMgmt();
            if (siteManagement != null) {

                Set managementBodies = siteManagement.getMgmtBodies();
                Iterator managementBodiesIter = managementBodies.iterator();
                while (managementBodiesIter.hasNext()) {

                    MgmtBody managementBody = (MgmtBody) managementBodiesIter.next();

                    String bodyOrg = managementBody.getMgmtBodyOrg();
                    String bodyAddress = managementBody.getMgmtBodyAddress();
                    String bodyAdminUnit = managementBody.getMgmtBodyAdminUnit();

                    if (StringUtils.isBlank(bodyOrg)) {
                        errorList.add("Organisation of the Management Bodies. (Management section)\n");
                    }

                    if (StringUtils.isBlank(bodyAddress)) {
                        if (StringUtils.isBlank(bodyAdminUnit)) {
                            errorList.add("Address of the Management Bodies. (Management section)\n");
                        }
                    }
                }

                Character managementStatus = siteManagement.getMgmtStatus();
                if (managementStatus != null) {
                    if (("Y").equals(managementStatus.toString().toUpperCase()) && siteManagement.getMgmtPlans().isEmpty()) {
                        errorList.add("Management Plans. (Management section)\n");
                    }
                }
            } else {
                errorList.add("Management. (Management section)\n");
            }

            Map map = site.getMap();
            if (map == null) {
                errorList.add("Map. (Maps section)\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ValidateSite.log.error("An error has occurred validating site. Error Message:::" + e.getMessage());
        }

        return errorList;
    }

    /**
     * Return true if the given dates are in chronologically ascending order, otherwise return false.
     * Null dates are simply ignored and not compared.
     *
     * @param dates The array of dates to compare.
     * @return true/false
     */
    public static boolean isDatesAscendingOrder(Date... dates) {

        if (dates == null || dates.length <= 1) {
            return true;
        }

        boolean result = true;
        if (dates != null && dates.length > 1) {

            Date prevDate = dates[0];
            for (int i = 1; i < dates.length; i++) {

                Date thisDate = dates[i];
                if (thisDate != null && prevDate != null && prevDate.after(thisDate)) {
                    result = false;
                    break;
                }

                if (thisDate != null) {
                    prevDate = thisDate;
                }
            }
        }

        return result;
    }
}
