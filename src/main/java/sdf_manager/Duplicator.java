/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sdf_manager;


import java.util.Iterator;
import java.util.Set;
import pojos.Site;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pojos.Doc;
import pojos.DocLink;
import pojos.Habitat;
import pojos.HabitatClass;
import pojos.Impact;
import pojos.Map;
import pojos.Mgmt;
import pojos.MgmtBody;
import pojos.MgmtPlan;
import pojos.NationalDtype;
import pojos.OtherSpecies;
import pojos.Region;
import pojos.Resp;
import pojos.SiteBiogeo;
import pojos.SiteBiogeoId;
import pojos.SiteOwnership;
import pojos.SiteOwnershipId;
import pojos.SiteRelation;
import pojos.Species;

/**
 *
 * @author charbda
 */
public class Duplicator {
    


    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Duplicator.class .getName());
    /**
     *
     */
    public Duplicator() {
        
    }
    /**
     * This method duplicates species
     * @param inSpecies
     * @return
     */
    public Species duplicateSpeciesNoPopulation(Species inSpecies) {      
        Species outSpecies = new Species();
        duplicateSpeciesNoPopulation(inSpecies, outSpecies);
        return outSpecies;        
    }

    /**
     * This method duplicates species without population data
     * @param inSpecies
     * @param outSpecies
     */
    public void duplicateSpeciesNoPopulation(Species inSpecies, Species outSpecies) {
        log.info("Duplicating species...");
        /* duplicate species without population data*/
        outSpecies.setSite(inSpecies.getSite());        
        outSpecies.setSpeciesCode(inSpecies.getSpeciesCode());
        outSpecies.setSpeciesConservation(inSpecies.getSpeciesConservation());        
        outSpecies.setSpeciesGlobal(inSpecies.getSpeciesGlobal());
        outSpecies.setSpeciesGroup(inSpecies.getSpeciesGroup());
        outSpecies.setSpeciesIsolation(inSpecies.getSpeciesIsolation());
        outSpecies.setSpeciesName(inSpecies.getSpeciesName());
        outSpecies.setSpeciesNp(inSpecies.getSpeciesNp());
        outSpecies.setSpeciesPopulation(inSpecies.getSpeciesPopulation());
        outSpecies.setSpeciesSensitive(inSpecies.getSpeciesSensitive());
        outSpecies.setSpeciesDataQuality(inSpecies.getSpeciesDataQuality());
        outSpecies.setSpeciesCategory(inSpecies.getSpeciesCategory());
        outSpecies.setSpeciesSizeMax(inSpecies.getSpeciesSizeMax());
        outSpecies.setSpeciesSizeMin(inSpecies.getSpeciesSizeMin());
        outSpecies.setSpeciesType(inSpecies.getSpeciesType());
        outSpecies.setSpeciesUnit(inSpecies.getSpeciesUnit());       
    }
    /**
     *
     * @param inSite
     * @param outSite
     * @return
     */
    public Site duplicateSite(Site inSite, Site outSite) {
        log.info("Duplicating site...");
        outSite.setSiteName(inSite.getSiteName());
        outSite.setSiteType(inSite.getSiteType());
        outSite.setSiteCompDate(inSite.getSiteCompDate());
        outSite.setSiteUpdateDate(inSite.getSiteUpdateDate());
        Resp resp = inSite.getResp();
        if (resp != null) {
           Resp nResp = new Resp();
           nResp.setRespAddress(resp.getRespAddress());
           nResp.setRespEmail(resp.getRespEmail());
           nResp.setRespName(resp.getRespName());
           nResp.setRespAdminUnit(resp.getRespAdminUnit());
           nResp.setRespAddressArea(resp.getRespAddressArea());
           nResp.setRespLocatorDesig(resp.getRespLocatorDesig());
           nResp.setRespLocatorName(resp.getRespLocatorName());
           nResp.setRespPostCode(resp.getRespPostCode());
           nResp.setRespPostName(resp.getRespPostName());
           nResp.setRespThoroughFare(resp.getRespThoroughFare());           
           nResp.getSites().add(outSite);
           this.saveAndReloadObj(nResp);
           outSite.setResp(nResp);
        }
        outSite.setSiteSpaDate(inSite.getSiteSpaDate());
        outSite.setSiteSpaLegalRef(inSite.getSiteSpaLegalRef());
        outSite.setSiteSciPropDate(inSite.getSiteSciPropDate());
        outSite.setSiteSciConfDate(inSite.getSiteSciConfDate());
        outSite.setSiteSacDate(inSite.getSiteSacDate());
        outSite.setSiteSacLegalRef(inSite.getSiteSacLegalRef());
        outSite.setSiteExplanations(inSite.getSiteExplanations());
        outSite.setSiteArea(inSite.getSiteArea());
        outSite.setSiteLongitude(inSite.getSiteLongitude());
        outSite.setSiteLatitude(inSite.getSiteLatitude());
        outSite.setSiteMarineArea(inSite.getSiteMarineArea());
        outSite.setSiteLength(inSite.getSiteLength());
        Set regions = inSite.getRegions();
        Iterator itr = regions.iterator();
        while (itr.hasNext()) {
            Region r = (Region) itr.next();
            Region nR = new Region();
            nR.setRegionCode(r.getRegionCode());
            nR.setRegionName(r.getRegionName());            
            nR.setSite(outSite);            
            outSite.getRegions().add(nR);
        }
        regions = inSite.getSiteBiogeos();
        itr = regions.iterator();
        while (itr.hasNext()) {
            SiteBiogeo sb = (SiteBiogeo) itr.next();
            SiteBiogeo nSb = new SiteBiogeo();
            nSb.setBiogeo(sb.getBiogeo());
            nSb.setBiogeoPercent(sb.getBiogeoPercent());
            nSb.setSite(outSite);
            SiteBiogeoId id= new SiteBiogeoId(outSite.getSiteCode(),nSb.getBiogeo().getBiogeoId());
            nSb.setId(id);            
            outSite.getSiteBiogeos().add(nSb);
            //saveAndReloadObj(outSite);
        }
        Set habitats = inSite.getHabitats();
        itr = habitats.iterator();        
        while (itr.hasNext()) {
            Habitat h = ((Habitat)itr.next());
            Habitat nH = new Habitat();
            nH.setHabitatCaves(h.getHabitatCaves());
            nH.setHabitatCode(h.getHabitatCode());
            nH.setHabitatConservation(h.getHabitatConservation());
            nH.setHabitatCover(h.getHabitatCover());
            nH.setHabitatDataQuality(h.getHabitatDataQuality());
            nH.setHabitatGlobal(h.getHabitatGlobal());
            nH.setHabitatNp(h.getHabitatNp());
            nH.setHabitatPriority(h.getHabitatPriority());
            nH.setHabitatRelativeSurface(h.getHabitatRelativeSurface());
            nH.setHabitatRepresentativity(h.getHabitatRepresentativity());
            nH.setSite(outSite);
            outSite.getHabitats().add(nH);
        }
        Set species = inSite.getSpecieses();
        itr = species.iterator();
        while(itr.hasNext()) {
            Species s = (Species) itr.next();
            Species nS = new Species();
            nS.setSite(outSite);
            nS.setSpeciesCategory(s.getSpeciesCategory());
            nS.setSpeciesCode(s.getSpeciesCode());
            nS.setSpeciesConservation(s.getSpeciesConservation());
            nS.setSpeciesDataQuality(s.getSpeciesDataQuality());
            nS.setSpeciesGlobal(s.getSpeciesGlobal());
            nS.setSpeciesGroup(s.getSpeciesGroup());
            nS.setSpeciesIsolation(s.getSpeciesIsolation());
            nS.setSpeciesName(s.getSpeciesName());
            nS.setSpeciesNp(s.getSpeciesNp());
            nS.setSpeciesPopulation(s.getSpeciesPopulation());
            nS.setSpeciesSensitive(s.getSpeciesSensitive());
            nS.setSpeciesSizeMax(s.getSpeciesSizeMax());
            nS.setSpeciesSizeMin(s.getSpeciesSizeMin());
            nS.setSpeciesType(s.getSpeciesType());
            nS.setSpeciesUnit(s.getSpeciesUnit());
            outSite.getSpecieses().add(nS);
        }
        species = inSite.getOtherSpecieses();
        itr = species.iterator();
        while(itr.hasNext()) {
            OtherSpecies s = (OtherSpecies) itr.next();
            OtherSpecies nS = new OtherSpecies();
            nS.setSite(outSite);
            nS.setOtherSpeciesCategory(s.getOtherSpeciesCategory());
            nS.setOtherSpeciesCode(s.getOtherSpeciesCode());
            nS.setOtherSpeciesGroup(s.getOtherSpeciesGroup());
            nS.setOtherSpeciesMotivation(s.getOtherSpeciesMotivation());
            nS.setOtherSpeciesName(s.getOtherSpeciesName());
            nS.setOtherSpeciesNp(s.getOtherSpeciesNp());
            nS.setOtherSpeciesSensitive(s.getOtherSpeciesSensitive());
            nS.setOtherSpeciesSizeMax(s.getOtherSpeciesSizeMax());
            nS.setOtherSpeciesSizeMin(s.getOtherSpeciesSizeMin());
            nS.setOtherSpeciesUnit(s.getOtherSpeciesUnit());
            outSite.getOtherSpecieses().add(nS);
        }
        habitats = inSite.getHabitatClasses();
        itr = habitats.iterator();
        while(itr.hasNext()) {
            HabitatClass h = (HabitatClass) itr.next();
            HabitatClass nH = new HabitatClass();
            nH.setHabitatClassCode(h.getHabitatClassCode());
            nH.setHabitatClassCover(h.getHabitatClassCover());
            nH.setHabitatClassDescription(h.getHabitatClassDescription());
            nH.setSite(outSite);
            outSite.getHabitatClasses().add(nH);
        }
        outSite.setSiteCharacteristics(inSite.getSiteCharacteristics());                
        outSite.setSiteQuality(inSite.getSiteQuality());
        Set impacts = inSite.getImpacts();
        itr = impacts.iterator();
        while (itr.hasNext()) {
            Impact i = (Impact) itr.next();
            Impact nI = new Impact();
            nI.setImpactCode(i.getImpactCode());
            nI.setImpactOccurrence(i.getImpactOccurrence());
            nI.setImpactPollutionCode(i.getImpactPollutionCode());
            nI.setImpactRank(i.getImpactRank());
            nI.setImpactType(i.getImpactType());
            nI.setSite(outSite);
            outSite.getImpacts().add(nI);
        }
        Set owners = inSite.getSiteOwnerships();
        itr = owners.iterator();
        while (itr.hasNext()) {
            SiteOwnership o = (SiteOwnership) itr.next();
            SiteOwnership nO = new SiteOwnership();
            nO.setOwnership(o.getOwnership());
            nO.setOwnershipPercent(o.getOwnershipPercent());
            SiteOwnershipId id = new SiteOwnershipId(o.getOwnership().getOwnershipId(),outSite.getSiteCode());
            nO.setId(id);   
            outSite.getSiteOwnerships().add(nO);
            //saveAndReloadObj(outSite);
        }
        Doc doc = inSite.getDoc();
        if (doc != null) {
            Doc nDoc = new Doc();
            nDoc.setDocDescription(doc.getDocDescription());
            nDoc.getSites().add(outSite);
            saveAndReloadObj(nDoc);
            Iterator itr2 = doc.getDocLinks().iterator();            
            while (itr2.hasNext()) {
                DocLink link = (DocLink) itr2.next();
                DocLink nLink = new DocLink();
                nLink.setDoc(nDoc);
                nLink.setDocLinkUrl(link.getDocLinkUrl());
                //saveAndReloadObj(nLink);
                nDoc.getDocLinks().add(nLink);
            }                        
            outSite.setDoc(nDoc);
            //saveAndReloadObj(outSite);            
        }
        Set designations = inSite.getNationalDtypes();
        itr = designations.iterator();
        while (itr.hasNext()) {
            NationalDtype dtype = (NationalDtype) itr.next();
            NationalDtype nDtype = new NationalDtype();
            nDtype.setNationalDtypeCode(dtype.getNationalDtypeCode());
            nDtype.setNationalDtypeCover(dtype.getNationalDtypeCover());
            nDtype.setSite(outSite);
            outSite.getNationalDtypes().add(nDtype);
        }
        Set relations = inSite.getSiteRelations();
        itr = relations.iterator();
        while (itr.hasNext()) {
            SiteRelation rel = (SiteRelation) itr.next();
            SiteRelation nRel = new SiteRelation();
            nRel.setSiteRelationCode(rel.getSiteRelationCode());
            nRel.setSiteRelationConvention(rel.getSiteRelationConvention());
            nRel.setSiteRelationCover(rel.getSiteRelationCover());
            nRel.setSiteRelationScope(rel.getSiteRelationScope());
            nRel.setSiteRelationSitename(rel.getSiteRelationSitename());
            nRel.setSiteRelationType(rel.getSiteRelationType());
            nRel.setSite(outSite);
            outSite.getSiteRelations().add(nRel);
        }
        outSite.setSiteDesignation(inSite.getSiteDesignation());
        Mgmt mgmt = inSite.getMgmt();
        if (mgmt != null) {
            Mgmt nMgmt = new Mgmt();
            nMgmt.setMgmtConservMeasures(mgmt.getMgmtConservMeasures());
            Set bodies = mgmt.getMgmtBodies();
            itr = bodies.iterator();
            while (itr.hasNext()) {
                MgmtBody body = (MgmtBody) itr.next();
                MgmtBody nBody = new MgmtBody();
                nBody.setMgmtBodyAddress(body.getMgmtBodyAddress());
                nBody.setMgmtBodyEmail(body.getMgmtBodyEmail());
                nBody.setMgmtBodyOrg(body.getMgmtBodyOrg());
                nBody.setMgmtBodyAdminUnit(body.getMgmtBodyAdminUnit());
                nBody.setMgmtBodyAddressArea(body.getMgmtBodyAddressArea());
                nBody.setMgmtBodyLocatorDesignator(body.getMgmtBodyLocatorDesignator());
                nBody.setMgmtBodyLocatorName(body.getMgmtBodyLocatorName());
                nBody.setMgmtBodyPostCode(body.getMgmtBodyPostCode());
                nBody.setMgmtBodyPostName(body.getMgmtBodyPostName());
                nBody.setMgmtBodyThroughFare(body.getMgmtBodyThroughFare());
                nBody.setMgmt(nMgmt);
                nMgmt.getMgmtBodies().add(nBody);
            }
            Set plans = mgmt.getMgmtPlans();
            itr = plans.iterator();
            while (itr.hasNext()) {
                MgmtPlan plan = (MgmtPlan) itr.next();
                MgmtPlan nPlan = new MgmtPlan();
                nPlan.setMgmtPlanName(plan.getMgmtPlanName());
                nPlan.setMgmtPlanUrl(plan.getMgmtPlanUrl());
                nPlan.setMgmt(nMgmt);
                nMgmt.getMgmtPlans().add(nPlan);
            }
            nMgmt.setMgmtStatus(mgmt.getMgmtStatus());
            this.saveAndReloadObj(nMgmt);
            outSite.setMgmt(nMgmt);
        }
        Map map = inSite.getMap();
        if (map != null) {
            Map nMap = new Map();
            nMap.setMapInspire(map.getMapInspire());
            nMap.setMapPdf(map.getMapPdf());
            nMap.setMapReference(map.getMapReference());            
            nMap.getSites().add(outSite);            
            this.saveAndReloadObj(nMap);
            outSite.setMap(nMap);
        }
        log.info("End Duplicating site...");
        return outSite;
    }
    /**
     * 
     * @param o
     */
    private void saveAndReloadObj(Object o) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tr = session.beginTransaction();
        session.saveOrUpdate(o);
        tr.commit();
        session.flush();
        session.close();
    }
}
