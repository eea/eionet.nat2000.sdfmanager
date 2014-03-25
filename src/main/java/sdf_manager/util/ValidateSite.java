/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

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


public class ValidateSite {


    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ValidateSite.class .getName());

    /**
     *
     */
    public ValidateSite(){

    }


    /**
     *
     * @param site
     * @return
     */
    public static ArrayList<String> validate(Site site){
        ArrayList<String> errorList = new ArrayList<String>();
        try{

            if(site.getSiteCompDate() == null){
                errorList.add("Compilation Date, in Identification section\n");
            }

            if(site.getSiteCompDate() != null && site.getSiteUpdateDate() != null){
                if (site.getSiteCompDate().compareTo(site.getSiteUpdateDate())>0){
                   errorList.add("Compilation Date and Update Date, in  Identification section\n");
                }
            }

            Resp resp = site.getResp();
            if (resp != null) {
                if(resp.getRespName() == null || (("").equals(resp.getRespName()))){
                    if(resp.getRespEmail() == null || (("").equals(resp.getRespEmail()))){
                       errorList.add("Name or Email in Identification section (Respondent tab)\n");
                    }
                }
                if((resp.getRespAddress() == null ||  (("").equals(resp.getRespAddress()))) && (resp.getRespAdminUnit() == null || (("").equals(resp.getRespAdminUnit())))){
                    errorList.add("Address in Identification section (Respondent tab)\n");
                }
            }

           if(site.getSiteType() == null){
               errorList.add("Site Type\n");
           }
           if(("A").equals(site.getSiteType().toString())){
                if(site.getSiteSpaDate() == null){
                    errorList.add("Date site classificated as SPA in Identification section (Dates tab)\n");
                }
            }else if(("B").equals(site.getSiteType().toString())){
                if(site.getSiteSciPropDate() == null){
                    errorList.add("Date Site proposed as SCI in Identification section (Dates tab)\n");
                }
            }else{
                if(site.getSiteSpaDate() == null){
                     errorList.add("Date site classificated as SPA in Identification section (Dates tab)\n");
                }
                if(site.getSiteSciPropDate() == null){
                    errorList.add("Date Site proposed as SCI in Identification section (Dates tab)\n");
                }
            }

            /**************LOCATION***************/
            /*regions*/
            Set siteRegions = site.getRegions();
            if(siteRegions.isEmpty()){
                errorList.add("Administrative Region code and name (NUTS) in Location section)\n");
            }

            Set siteBioRegions = site.getSiteBiogeos();
            if(siteBioRegions.isEmpty()){
                errorList.add("Biogeographical Region in Location section)\n");
            }

            /********ECOLOGICAL INFORMATION***********/

            /************HABITATS****************/
            Set siteHabs = site.getHabitats();
            Iterator itr = siteHabs.iterator();
            boolean habitatInfo = false;
            boolean speciesInfo= false;

            if(!siteHabs.isEmpty()){
                habitatInfo = true;
            }
            while (itr.hasNext()) {
                Habitat h = (Habitat) itr.next();

                if(h.getHabitatCode() == null || (("").equals(h.getHabitatCode()))){
                    errorList.add("The code of the habitat. (Ecological Info-Habitat Type  section)\n");
                }

                boolean dAssesment = false;
                if(h.getHabitatRepresentativity() == null || (("-").equals(h.getHabitatRepresentativity().toString()))){
                    errorList.add("Representativity of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                }else if(("D").equals(h.getHabitatRepresentativity().toString())){
                    dAssesment = true;
                }
    ValidateSite.log.info("*******************************Habitat==>"+h.getHabitatCode()+"<==h.getHabitatRepresentativity()==>"+h.getHabitatRepresentativity()+"<==");
                if(h.getHabitatDataQuality() == null && !dAssesment){
                      errorList.add("Data Quality of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                }
ValidateSite.log.info("Habitat==>"+h.getHabitatCode()+"<==h.getHabitatRelativeSurface()==>"+h.getHabitatRelativeSurface()+"<==");
                if(h.getHabitatRelativeSurface() == null && !dAssesment){
                      errorList.add("Relative Surface of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                }
ValidateSite.log.info("Habitat==>"+h.getHabitatCode()+"<==h.getHabitatConservation()==>"+h.getHabitatConservation()+"<==");
                if(h.getHabitatConservation() == null  && !dAssesment){
                      errorList.add("Conservation of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                }

                if(h.getHabitatGlobal() == null  && !dAssesment){
                      errorList.add("Global of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                }
            }

             /************SPECIES****************/

            Set siteSpecies = site.getSpecieses();
            boolean birdsSPA = false;
            if(!siteSpecies.isEmpty()){
                Iterator itsr = siteSpecies.iterator();
                while(itsr.hasNext()) {
                    Species s = (Species) itsr.next();
                    if(s.getSpeciesGroup() != null && !(("-").equals(s.getSpeciesGroup().toString()))){
                        if((s.getSpeciesGroup().toString()).equals("B")){
                                birdsSPA = true;
                        }
                    }else{
                        errorList.add("Group of the species. (Ecological Info - Species Type  section)\n");
                    }

                    if(s.getSpeciesName() == null || (("").equals(s.getSpeciesName()))){
                        errorList.add("Scientific name of the species for the code : "+s.getSpeciesCode()+" and the group :"+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                    }

                    boolean dAssesment = false;
                    if(s.getSpeciesPopulation() != null && (("D").equals(s.getSpeciesPopulation().toString()))){
                        dAssesment = true;
                    }
                    if(s.getSpeciesDataQuality() == null && !dAssesment){
                        errorList.add("Data Quality of the species for the code : "+s.getSpeciesCode()+", species name : "+s.getSpeciesName()+" and the group :"+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                    }
                    if(s.getSpeciesConservation() == null && !dAssesment){
                          errorList.add("Conservation of the species for the code : "+s.getSpeciesCode()+", species name : "+s.getSpeciesName()+" and the group :"+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                    }

                    if(s.getSpeciesIsolation() == null  && !dAssesment){
                          errorList.add("Isolation of the species for the code : "+s.getSpeciesCode()+", species name : "+s.getSpeciesName()+" and the group  :"+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                     }

                    if(s.getSpeciesGlobal() == null && !dAssesment){
                          errorList.add("Global of the species for the code : "+s.getSpeciesCode()+", species name : "+s.getSpeciesName()+" and the group  :"+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                    }
                }
                speciesInfo=true;
            }


            Set siteOtherSpecies = site.getOtherSpecieses();
            if(!siteOtherSpecies.isEmpty()){
                Iterator itosr = siteOtherSpecies.iterator();
                 while(itosr.hasNext()) {
                    OtherSpecies s = (OtherSpecies) itosr.next();
                    if(s.getOtherSpeciesGroup() == null || (("-").equals(s.getOtherSpeciesGroup()))){
                        errorList.add("Group of the species (Other species). (Ecological Info - Other Species Type  section)\n");
                    }

                    if(s.getOtherSpeciesName() == null || (("").equals(s.getOtherSpeciesName()))){
                        errorList.add("Scientific name of the species (Other species) for the code : "+s.getOtherSpeciesCode()+" and the group :"+s.getOtherSpeciesGroup()+" . (Ecological Info - Other Species Type  section)\n");
                    }
                }

            }

            if(!habitatInfo && !speciesInfo){
                 errorList.add("Non habitat or  species. (Ecological Info)\n");
            }
            if((("A").equals(site.getSiteType().toString())) || (("C").equals(site.getSiteType().toString()))){
               if(!birdsSPA){
                errorList.add("No birds in SPA site. (Ecological Info)\n");
            }
           }


            /**************DESCRIPTION***********************/
            Set siteImpacts = site.getImpacts();
            boolean posImpact= false;
            boolean negImpact= false;
            if(!siteImpacts.isEmpty()){
                Iterator itir = siteImpacts.iterator();
                while (itir.hasNext()) {
                    Impact im = (Impact) itir.next();
                    if(("P").equals(im.getImpactType().toString())){
                        posImpact= true;
                    }else if(("N").equals(im.getImpactType().toString())){
                        negImpact= true;
                    }else{

                    }

                    if(im.getImpactCode() == null || (("").equals(im.getImpactCode()))){
                        errorList.add("Impact code. (Description - Pressures and Threads section)\n");
                    }

                    if(im.getImpactRank() == null || (("-").equals(im.getImpactRank().toString()))){
                        errorList.add("Rank of the impact whose code is: "+im.getImpactCode()+". (Description - Pressures and Threads section)\n");
                    }

                    if(im.getImpactOccurrence() == null || (("-").equals(im.getImpactOccurrence().toString()))){
                       errorList.add("Occurence of the impact whose code is: "+im.getImpactCode()+". (Description - Pressures and Threads section)\n");
                    }
                }
                if(!posImpact || !negImpact){
                    errorList.add("There has to be at least one positive and negative impact.\n");
                }
            }else{
                 errorList.add("Impacts. (Description - Pressures and Threads section)\n");
            }

            /********PROTECTION**********/

            Set rels = site.getSiteRelations();
            if(!rels.isEmpty()){
                Iterator itre = rels.iterator();

                while (itre.hasNext()) {
                    SiteRelation rel = (SiteRelation) itre.next();

                    if(rel.getSiteRelationSitename() == null || (("").equals(rel.getSiteRelationSitename()))){
                      errorList.add("Site Name of the Relation National Site. (Protection Status - Relation with other sites section)\n ");
                    }

                    if(rel.getSiteRelationType() == null){
                        errorList.add("Relation National Site whose site name is: "+rel.getSiteRelationSitename()+". . (Protection Status - Relation with other sites section)\n ");
                    }
                }
            }

            /******************MANAGEMENT************************/

            Mgmt mgmt = site.getMgmt();
            if (mgmt != null) {
                /***Mangement Body**/
                Set bodies = mgmt.getMgmtBodies();
                Iterator itrbody = bodies.iterator();

                while (itrbody.hasNext()) {
                    MgmtBody bodyObj = (MgmtBody) itrbody.next();

                    if(bodyObj.getMgmtBodyOrg() == null || (("").equals(bodyObj.getMgmtBodyOrg()))){
                       errorList.add("Organisation of the Management Bodies. (Management section)\n");
                    }

                    if(bodyObj.getMgmtBodyAddress() == null || (("").equals(bodyObj.getMgmtBodyAddress()))){
                        if(bodyObj.getMgmtBodyAdminUnit() == null || (("").equals(bodyObj.getMgmtBodyAdminUnit()))){
                            errorList.add("Address of the Management Bodies. (Management section)\n");
                        }
                    }
                }

                /***Mangement Plan**/
                if(mgmt.getMgmtStatus() !=null){
                    if(("Y").equals(mgmt.getMgmtStatus().toString().toUpperCase()) && mgmt.getMgmtPlans().isEmpty()){
                        errorList.add("Management Plans. (Management section)\n");
                    }
                }
            }else{
                  errorList.add("Management. (Management section)\n");
            }

            Map map = site.getMap();
            if(map == null){
                errorList.add("Map. (Maps section)\n");
            }
        }catch(Exception e){
            e.printStackTrace();
           ValidateSite.log.error("An error has occurred validating site. Error Message:::"+e.getMessage());
        }
        return errorList;
    }

}
