package sdf_manager;

import java.awt.Desktop;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import pojos.Biogeo;
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
import pojos.Ownership;
import pojos.Region;
import pojos.Resp;
import pojos.Site;
import pojos.SiteBiogeo;
import pojos.SiteBiogeoId;
import pojos.SiteOwnership;
import pojos.SiteOwnershipId;
import pojos.SiteRelation;
import pojos.Species;
import sdf_manager.importers.ImporterTools;
import sdf_manager.util.SDF_Constants;
import sdf_manager.util.SDF_Util;

public class ImporterXMLStax extends AbstractImporter implements Importer {

    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(ImporterXMLStax.class.getName());
    //private Logger logger;
    //private String encoding;
    private String fileName;
    private FileWriter outFile;
    private PrintWriter out;

    /**
     *
     * @param logger
     * @param encoding
     * @param logFile
     * @param fileName
     */
    public ImporterXMLStax(Logger logger, String encoding, String logFile, String fileName) {
        super(logger, logFile);
  }



    /**
     *
     * @param fileName
     * @return
     */
    public boolean validateAndProcessDB(String fileName) {
        Session session = null;

        try {
            String dbSchemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
            Properties properties = new Properties();
            // properties.load(new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" +
            // File.separator + "sdf_database.properties"));
            properties.load(new FileInputStream(SDF_ManagerApp.LOCAL_PROPERTIES_FILE));
            session = HibernateUtil.getSessionFactory().openSession();

            ImporterXMLStax.log.info("Import process is starting");
            log("Import process is starting.", true);

            this.processDatabase(session, fileName);

        } catch (Exception e) {
            // //e.printStackTrace();
            ImporterXMLStax.log.error("Error in import process:::" + e);
            return false;
        } finally {
            session.clear();
            session.close();
            closeLogFile();
        }
        return true;
    }

    /**
     *
     * @param session
     * @return
     * @throws SQLException
     */
    private ArrayList loadSpecies(Session session) throws SQLException {
        ArrayList siteList = new ArrayList();

        String hql = "from Site";
        try {
            Query q = session.createQuery(hql);
            Iterator itr = q.iterate();

            while (itr.hasNext()) {
                Site rs = (Site) itr.next();
                siteList.add(rs);
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ImporterXMLStax.log.error("Error loading Species:::" + e);
        }
        return siteList;
    }

    /**
     * Checks how many of the sites in the siteList exist in the database. Also, performs a check on whether the regions of each site are of level 2.
     * The regions that are not of level 2 are returned as the value of the returned {@link HashMap}.
     * 
     * @param conn
     */
//    private HashMap validateSites(Session session, ArrayList siteList) {
//        HashMap siteHasHDB = new HashMap();
//        try {
//            int j = 0;
//
//            for (int i = 0; i < siteList.size(); i++) {
//                Transaction tx = session.beginTransaction();
//                try {
//                    Site site = (Site) siteList.get(i);
//                    String sitecode = site.getSiteCode();
//                    ImporterXMLStax.log.info("validating sites:::" + sitecode);
//
//                    log("validating site: " + sitecode, true);
//                    boolean siteExists = SDF_Util.validateSite(session, sitecode);
//                    Set regionSiteList = site.getRegions();
//                    Iterator regionsIterator = regionSiteList.iterator();
//                    ArrayList nutsNoOK = new ArrayList();
//                    while (regionsIterator.hasNext()) {
//                        Region nuts = (Region) regionsIterator.next();
//                        String regionCode = nuts.getRegionCode();
//                        if (!isRegionLevel2(session, regionCode)) {
//                            nutsNoOK.add(regionCode);
//                        }
//                    }
//                    if (siteExists) {
//                        siteHasHDB.put(site.getSiteCode(), nutsNoOK);
//                    }
//                    tx.commit();
//                } catch (Exception e) {
//                    tx.rollback();
//                    break;
//                }
//                if (++j % 20 == 0) {
//                    session.flush();
//                    session.clear();
//                }
//            }
//
//        } catch (Exception e) {
//            ImporterXMLStax.log.error("Error validating Site:::" + e);
//            return siteHasHDB;
//        }
//        return siteHasHDB;
//
//    }

    /**
     *
     * @param fileName
     * @return
     */
    @Override
    public boolean processDatabase(String fileName) {
        return validateAndProcessDB(fileName);
    }

    /**
     *
     * @param session
     * @param fileName
     * @return
     */
    public boolean processDatabase(Session session, String fileName) {

        this.fileName = fileName;
        boolean isOK = true;
        String siteCode = null;
        try {
            ImporterXMLStax.log.info("Import process from file:::" + fileName);

            log("Init Import process.");
            // File fXmlFile = new File((new File(fileName)).getPath());
            InputStream is = new BufferedInputStream(new FileInputStream((new File(fileName)).getPath()));

            XMLInputFactory factory = XMLInputFactory.newInstance();
            // factory.setProperty("report-cdata-event", Boolean.TRUE);
            factory.setProperty("javax.xml.stream.isCoalescing", true);
            XMLStreamReader parser = factory.createXMLStreamReader(is);

            Site site = null;
            Resp resp = null;
            Region region = null;
            Mgmt mgmt = null;
            MgmtBody mgmtBody = null;
            MgmtPlan mgmtPlan = null;
            SiteBiogeo bioRegion = null;
            Habitat habitat = null;
            Species species = null;
            OtherSpecies oSpecies = null;
            HabitatClass habClass = null;
            Impact impact = null;
            SiteOwnership ownerShip = null;
            Doc doc = null;
            DocLink docLink = null;
            NationalDtype nDType = null;
            SiteRelation natRelation = null;
            SiteRelation intRelation = null;
            Map map = null;
            StringBuffer strMotivation = null;
            String speciesGroup = null;
            boolean bioReg = false;
            ArrayList siteList = new ArrayList();
            String siteIdent = null;
            
            List<String> restrictedCodes = new ArrayList<String>();
            
            while (parser.hasNext()) {
                String localName = printEventInfo(parser);

                if (localName != null) {
                    // parser.next();
                    String localData = printEventInfoData(parser);
                    if (localData != null) {
                        if (localName.equals("sdf")) {
                            site = new Site();
                            siteIdent = "SiteIdentification";
                        }

                        // SITE IDENTIFICATION
                        else if (localName.equals("siteType")) {
                            site.setSiteType(localData.charAt(0));
                        } else if (localName.equals("siteCode")) {
                            siteCode = localData;
                            log("Processing site: " + localData, true);
                            site.setSiteCode(localData);
                            if (SDF_Util.validateSite(session, siteCode)) {
                            	restrictedCodes.add(siteCode);
                            }
                        } else if (localName.equals("siteName") && siteIdent != null) {
                            site.setSiteName(localData);
                        } else if (localName.equals("compilationDate")) {
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteCompDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                        } else if (localName.equals("updateDate")) {
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteUpdateDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                        } else if (localName.equals("spaClassificationDate")) {
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteSpaDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                        } else if (localName.equals("spaLegalReference")) {
                            site.setSiteSpaLegalRef(localData);
                        } else if (localName.equals("sciProposalDate")) {
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteSciPropDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                        } else if (localName.equals("sciConfirmationDate")) {
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteSciConfDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                        } else if (localName.equals("sacDesignationDate")) {
                            ImporterXMLStax.log.info("************localData==>" + localData + "<==");
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteSacDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                            ImporterXMLStax.log.info("************site.getSiteSacDate()==>" + site.getSiteSacDate() + "<==");
                        } else if (localName.equals("sacLegalReference")) {
                            site.setSiteSacLegalRef(localData);
                        } else if (localName.equals("explanations")) {
                            site.setSiteExplanations(localData);
                        } else if (localName.equals("asciProposalDate")) {
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteProposedAsciDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                        } else if (localName.equals("asciCandidateConfirmationDate")) {
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteConfirmedCandidateAsciDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                        } else if (localName.equals("asciConfirmationDate")) {
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteConfirmedAsciDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                        } else if (localName.equals("asciDesignationDate")) {
                            if (!(SDF_Constants.NULL_DATE).equals(localData)) {
                                site.setSiteDesignatedAsciDate(ImporterTools.parseXmlDate(siteCode, localName, localData));
                            }
                        } else if (localName.equals("asciDesignationLegalReference")) {
                            site.setSiteAsciLegalRef(localData);
                        }

                        // RESPONDENT
                        else if (localName.equals("respondent")) {
                            resp = new Resp();
                        } else if (localName.equals("name") && resp != null) {
                            resp.setRespName(localData);
                        } else if (localName.equals("adminUnit") && resp != null) {
                            resp.setRespAdminUnit(localData);
                        } else if (localName.equals("locatorDesignator") && resp != null) {
                            resp.setRespLocatorDesig(localData);
                        } else if (localName.equals("locatorName") && resp != null) {
                            resp.setRespLocatorName(localData);
                        } else if (localName.equals("addressArea") && resp != null) {
                            resp.setRespAddressArea(localData);
                        } else if (localName.equals("postName") && resp != null) {
                            resp.setRespPostName(localData);
                        } else if (localName.equals("postCode") && resp != null) {
                            resp.setRespPostCode(localData);
                        } else if (localName.equals("thoroughfare") && resp != null) {
                            resp.setRespThoroughFare(localData);
                        } else if (localName.equals("addressUnstructured") && resp != null) {
                            resp.setRespAddress(localData);
                        } else if (localName.equals("email") && resp != null) {
                            resp.setRespEmail(localData);
                        } else if (localName.equals("respondent_end")) {
                            site.setResp(resp);
                            resp = null;
                        }

                        // SITE LOCATION
                        else if (localName.equals("longitude")) {
                            site.setSiteLongitude(Double.parseDouble(localData));
                        } else if (localName.equals("latitude")) {
                            site.setSiteLatitude(Double.parseDouble(localData));
                        } else if (localName.equals("area")) {
                            site.setSiteArea(Double.parseDouble(localData));
                        } else if (localName.equals("marineAreaPercentage")) {
                            site.setSiteMarineArea(Double.parseDouble(localData));
                        } else if (localName.equals("siteLength")) {
                            site.setSiteLength(Double.parseDouble(localData));
                        }

                        // ADMIN REGIONS
                        else if (localName.equals("region")) {
                            region = new Region();
                            ImporterXMLStax.log.info("creating the region");
                        } else if (localName.equals("code") && region != null) {
                            region.setRegionCode(localData);
                        } else if (localName.equals("name") && region != null) {
                            region.setRegionName(localData);
                        } else if (localName.equals("region_end")) {
                            region.setSite(site);
                            site.getRegions().add(region);
                            region = null;
                        }

                        // BIOREGIONS
                        else if (localName.equals("biogeoRegions")) {
                            bioRegion = new SiteBiogeo();
                        } else if (localName.equals("code") && bioRegion != null) {
                            int biogeoId = getBiogeoId(session, localData);
                            Biogeo biogeo = (Biogeo) session.load(Biogeo.class, biogeoId);
                            SiteBiogeoId id = new SiteBiogeoId(site.getSiteCode(), biogeo.getBiogeoId());
                            bioRegion = new SiteBiogeo(id, biogeo, site);
                        } else if (localName.equals("percentage") && bioRegion != null) {
                            bioRegion.setBiogeoPercent(Double.parseDouble(localData));
                        } else if (localName.equals("biogeoRegions_end")) {
                            site.getSiteBiogeos().add(bioRegion);
                            bioRegion = null;
                            bioReg = false;
                        }

                        // BIOREGIONS
                        else if (localName.equals("habitatType")) {
                            habitat = new Habitat();
                        } else if (localName.equals("code") && habitat != null) {
                            habitat.setHabitatCode(localData);
                        } else if (localName.equals("priorityFormOfHabitatType") && habitat != null) {
                            if (localData.equals("true")) {
                                habitat.setHabitatPriority(Short.parseShort("1"));
                            } else {
                                habitat.setHabitatPriority(Short.parseShort("0"));
                            }
                        } else if (localName.equals("nonpresentInSite") && habitat != null) {
                            if (localData.equals("true")) {
                                habitat.setHabitatNp(Short.parseShort("1"));
                            } else {
                                habitat.setHabitatNp(Short.parseShort("0"));
                            }
                        } else if (localName.equals("coveredArea") && habitat != null) {
                            if (!localData.equals("0.00")) {
                                habitat.setHabitatCoverHa(Double.parseDouble(localData));
                            }
                        } else if (localName.equals("caves") && habitat != null) {
                            if (!localData.equals("0.00")) {
                                habitat.setHabitatCaves(Integer.parseInt(localData));
                            }
                        } else if (localName.equals("observationDataQuality") && habitat != null) {
                            habitat.setHabitatDataQuality(localData);
                        } else if (localName.equals("representativity") && habitat != null) {
                            habitat.setHabitatRepresentativity(localData.charAt(0));
                        } else if (localName.equals("relativeSurface") && habitat != null) {
                            habitat.setHabitatRelativeSurface(localData.charAt(0));
                        } else if (localName.equals("conservation") && habitat != null) {
                            habitat.setHabitatConservation(localData.charAt(0));
                        } else if (localName.equals("global") && habitat != null) {
                            habitat.setHabitatGlobal(localData.charAt(0));
                        } else if (localName.equals("habitatType_end")) {
                            habitat.setSite(site);
                            site.getHabitats().add(habitat);
                            habitat = null;
                        }

                        // SPECIES
                        else if (localName.equals("speciesPopulation")) {
                            species = new Species();
                            speciesGroup = null;
                        } else if (localName.equals("speciesGroup") && species != null) {
                            if (localData.length() > 1) {
                                speciesGroup = localData;
                            } else {
                                species.setSpeciesGroup(localData.charAt(0));
                            }
                        } else if (localName.equals("speciesCode") && species != null) {
                            species.setSpeciesCode(localData);
                        } else if (localName.equals("scientificName") && species != null) {
                            species.setSpeciesName(localData);
                        } else if (localName.equals("sensitiveInfo") && species != null) {
                            if (localData.equals("true")) {
                                species.setSpeciesSensitive(Short.parseShort("1"));
                            } else {
                                species.setSpeciesSensitive(Short.parseShort("0"));
                            }
                        } else if (localName.equals("nonpresentInSite") && species != null) {
                            if (localData.equals("true")) {
                                species.setSpeciesNp(Short.parseShort("1"));
                            } else {
                                species.setSpeciesNp(Short.parseShort("0"));
                            }
                        } else if (localName.equals("populationType") && species != null) {
                            species.setSpeciesType(localData.charAt(0));
                        } else if (localName.equals("lowerBound") && species != null) {
                            species.setSpeciesSizeMin(Integer.parseInt(localData));
                        } else if (localName.equals("upperBound") && species != null) {
                            species.setSpeciesSizeMax(Integer.parseInt(localData));
                        } else if (localName.equals("countingUnit") && species != null) {
                            species.setSpeciesUnit(localData);
                        } else if (localName.equals("abundanceCategory") && species != null) {
                            species.setSpeciesCategory(localData.charAt(0));
                        } else if (localName.equals("observationDataQuality") && species != null) {
                            species.setSpeciesDataQuality(localData);
                        } else if (localName.equals("population") && species != null) {
                            species.setSpeciesPopulation(localData.charAt(0));
                        } else if (localName.equals("conservation") && species != null) {
                            species.setSpeciesConservation(localData.charAt(0));
                        } else if (localName.equals("isolation") && species != null) {
                            species.setSpeciesIsolation(localData.charAt(0));
                        } else if (localName.equals("global") && species != null) {
                            species.setSpeciesGlobal(localData.charAt(0));
                        } else if (localName.equals("motivations") && species != null) {
                            strMotivation = new StringBuffer();
                            oSpecies = new OtherSpecies();
                            oSpecies.setOtherSpeciesCode(species.getSpeciesCode());
                            if (speciesGroup != null) {
                                oSpecies.setOtherSpeciesGroup(speciesGroup);
                            } else {
                                oSpecies.setOtherSpeciesGroup(species.getSpeciesGroup().toString());
                            }

                            oSpecies.setOtherSpeciesName(species.getSpeciesName());
                            oSpecies.setOtherSpeciesCategory(species.getSpeciesCategory());
                            oSpecies.setOtherSpeciesNp(species.getSpeciesNp());
                            oSpecies.setOtherSpeciesSensitive(species.getSpeciesSensitive());
                            oSpecies.setOtherSpeciesSizeMax(species.getSpeciesSizeMax());
                            oSpecies.setOtherSpeciesSizeMin(species.getSpeciesSizeMin());
                            oSpecies.setOtherSpeciesUnit(species.getSpeciesUnit());
                            oSpecies.setSite(site);
                            species = null;
                        } else if (localName.equals("motivation") && oSpecies != null) {
                            strMotivation.append(localData);
                            strMotivation.append(",");
                        } else if (localName.equals("motivations_end") && oSpecies != null) {
                            String motivation = strMotivation.toString();
                            if (motivation != null && !("").equals(motivation)) {
                                motivation = motivation.substring(0, motivation.length() - 1);
                                strMotivation = null;
                                oSpecies.setOtherSpeciesMotivation(motivation);
                            }
                        } else if (localName.equals("speciesPopulation_end") && oSpecies != null) {
                            site.getOtherSpecieses().add(oSpecies);
                            oSpecies = null;
                        } else if (localName.equals("speciesPopulation_end") && species != null) {
                            species.setSite(site);
                            site.getSpecieses().add(species);
                            species = null;
                        } // HABITAT CLASS
                        else if (localName.equals("habitatClass")) {
                            habClass = new HabitatClass();
                        } else if (localName.equals("code") && habClass != null) {
                            habClass.setHabitatClassCode(localData);
                        } else if (localName.equals("coveragePercentage") && habClass != null) {
                            habClass.setHabitatClassCover(Double.parseDouble(localData));
                        } else if (localName.equals("habitatClass_end")) {
                            habClass.setSite(site);
                            site.getHabitatClasses().add(habClass);
                            habClass = null;
                        }

                        // OTHER CHARACTERISTICS
                        else if (localName.equals("otherSiteCharacteristics")) {
                            site.setSiteCharacteristics(localData);
                        }

                        // QUALITY AND IMNPORTANCE
                        else if (localName.equals("qualityAndImportance")) {
                            site.setSiteQuality(localData);
                        }

                        // IMPACTS
                        else if (localName.equals("impact")) {
                            impact = new Impact();
                        } else if ((localName.equals("code") || localName.equals("impactCode")) && impact != null) {
                            impact.setImpactCode(localData);
                            impact.setImpactRank(localData.charAt(0));

                        } else if (localName.equals("rank") && impact != null) {
                            if (localData.equals("A")) {
                                impact.setImpactRank("H".charAt(0));
                            } else if (localData.equals("B")) {
                                impact.setImpactRank("M".charAt(0));
                            } else if (localData.equals("C")) {
                                impact.setImpactRank("L".charAt(0));
                            } else {
                                impact.setImpactRank(localData.charAt(0));
                            }
                        } else if (localName.equals("pollutionCode") && impact != null) {
                            impact.setImpactPollutionCode(localData.charAt(0));
                        } else if (localName.equals("occurrence") && impact != null) {
                            impact.setImpactOccurrence(localData.charAt(0));
                        } else if (localName.equals("natureOfImpact") && impact != null) {
                            impact.setImpactType(localData.charAt(0));
                        } else if (localName.equals("impact_end")) {
                            impact.setSite(site);
                            site.getImpacts().add(impact);
                            impact = null;
                        }

                        // OWNERSHIP
                        else if (localName.equals("ownershiptype")) {
                            // impact.setImpactType(localData.charAt(0));
                            Ownership owner = new Ownership();
                            owner.setOwnershipType(localData);
                            int ownerShipId = getOwnerShipId(session, localData);
                            if (ownerShipId != -1) {
                                owner.setOwnershipId(ownerShipId);
                                SiteOwnershipId id = new SiteOwnershipId(owner.getOwnershipId(), site.getSiteCode());
                                ownerShip = new SiteOwnership(id, owner, site);
                            }
                        } else if (localName.equals("percent") && ownerShip != null) {
                            ownerShip.setOwnershipPercent(Double.parseDouble(localData));
                        } else if (localName.equals("ownershipPart_end") && ownerShip != null) {
                            ownerShip.setSite(site);
                            site.getSiteOwnerships().add(ownerShip);
                            ownerShip = null;
                        }

                        // DOCUMENTATION
                        else if (localName.equals("documentation")) {
                            doc = new Doc();
                            saveAndReloadSession(session, doc, restrictedCodes, siteCode);
                        } else if (localName.equals("description") && doc != null) {
                            doc.setDocDescription(localData);
                            site.setDoc(doc);
                            saveAndReloadSession(session, doc, restrictedCodes, siteCode);
                        }

                        // LINK
                        else if (localName.equals("links")) {

                        } else if (localName.equals("link")) {
                            docLink = new DocLink();
                            docLink.setDoc(doc);
                            docLink.setDocLinkUrl(localData);
                            doc.getDocLinks().add(docLink);
                            saveAndReloadSession(session, docLink, restrictedCodes, siteCode);
                            ImporterXMLStax.log.info("docLink link ==>" + localData);
                        } else if (localName.equals("link_end")) {
                            docLink = null;
                        } else if (localName.equals("links_end")) {
                            // saveAndReloadSession(session, doc);
                        } else if (localName.equals("documentation_end")) {
                            site.setDoc(doc);
                            saveAndReloadSession(session, doc, restrictedCodes, siteCode);
                            doc = null;
                        }

                        // NATIONAL DESIGNATION
                        else if (localName.equals("nationalDesignation")) {
                            nDType = new NationalDtype();
                        } else if (localName.equals("designationCode") && nDType != null) {
                            nDType.setNationalDtypeCode(localData);
                        } else if (localName.equals("cover") && nDType != null) {
                            nDType.setNationalDtypeCover(Double.parseDouble(localData));
                        } else if (localName.equals("nationalDesignation_end")) {
                            nDType.setSite(site);
                            site.getNationalDtypes().add(nDType);
                            nDType = null;
                        }

                        // RELATIONS
                        else if (localName.equals("nationalRelationship")) {
                            siteIdent = null;
                            natRelation = new SiteRelation();
                            natRelation.setSiteRelationScope('N');
                        } else if (localName.equals("designationCode") && natRelation != null) {
                            natRelation.setSiteRelationCode(localData);
                        } else if (localName.equals("siteName") && natRelation != null) {
                            natRelation.setSiteRelationSitename(localData);
                        } else if (localName.equals("type") && natRelation != null) {
                            natRelation.setSiteRelationType(localData.charAt(0));
                        } else if (localName.equals("cover") && natRelation != null) {
                            natRelation.setSiteRelationCover(Double.parseDouble(localData));
                        } else if (localName.equals("nationalRelationship_end")) {
                            natRelation.setSite(site);
                            site.getSiteRelations().add(natRelation);
                            natRelation = null;
                        } else if (localName.equals("internationalRelationship")) {
                            siteIdent = null;
                            intRelation = new SiteRelation();
                            intRelation.setSiteRelationScope('I');
                        } else if (localName.equals("convention") && intRelation != null) {
                            intRelation.setSiteRelationConvention(localData);
                        } else if (localName.equals("siteName") && intRelation != null) {
                            intRelation.setSiteRelationSitename(localData);
                        } else if (localName.equals("type") && intRelation != null) {
                            intRelation.setSiteRelationType(localData.charAt(0));
                        } else if (localName.equals("cover") && intRelation != null) {
                            intRelation.setSiteRelationCover(Double.parseDouble(localData));
                        } else if (localName.equals("internationalRelationship_end")) {
                            intRelation.setSite(site);
                            site.getSiteRelations().add(intRelation);
                            intRelation = null;
                        }

                        // ADDITIONAL DESIGNATION
                        else if (localName.equals("siteDesignationAdditional")) {
                            site.setSiteDesignation(localData);
                        }

                        // MANAGEMENT
                        else if (localName.equals("siteManagement")) {
                            mgmt = new Mgmt();
                            saveAndReloadSession(session, mgmt, restrictedCodes, siteCode);
                        }

                        // MANAGEMENT BODY
                        else if (localName.equals("managementBody")) {
                            mgmtBody = new MgmtBody();
                        } else if (localName.equals("organisation") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyOrg(localData);
                        } else if (localName.equals("adminUnit") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyAdminUnit(localData);
                        } else if (localName.equals("locatorDesignator") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyLocatorDesignator(localData);
                        } else if (localName.equals("locatorName") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyLocatorName(localData);
                        } else if (localName.equals("addressArea") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyAddressArea(localData);
                        } else if (localName.equals("postName") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyPostName(localData);
                        } else if (localName.equals("postCode") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyPostCode(localData);
                        } else if (localName.equals("thoroughfare") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyThroughFare(localData);
                        } else if (localName.equals("addressUnstructured") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyAddress(localData);
                        } else if (localName.equals("email") && mgmtBody != null) {
                            mgmtBody.setMgmtBodyEmail(localData);
                        } else if (localName.equals("managementBody_end")) {
                            mgmtBody.setMgmt(mgmt);
                            saveAndReloadSession(session, mgmtBody, restrictedCodes, siteCode);
                            mgmt.getMgmtBodies().add(mgmtBody);
                            // saveAndReloadSession(session, mgmt);
                            mgmtBody = null;
                        }

                        // MANAGEMENT
                        else if (localName.equals("exists") && mgmt != null) {
                            mgmt.setMgmtStatus(localData.charAt(0));
                        }

                        // MANAGEMENT PLANS
                        else if (localName.equals("managementPlan")) {
                            mgmtPlan = new MgmtPlan();
                        } else if (localName.equals("name") && mgmtPlan != null) {
                            mgmtPlan.setMgmtPlanName(localData);
                        } else if (localName.equals("url") && mgmtPlan != null) {
                            mgmtPlan.setMgmtPlanUrl(localData);
                        } else if (localName.equals("managementPlan_end")) {
                            mgmtPlan.setMgmt(mgmt);
                            saveAndReloadSession(session, mgmtPlan, restrictedCodes, siteCode);
                            mgmt.getMgmtPlans().add(mgmtPlan);
                            mgmtPlan = null;
                            ImporterXMLStax.log.info("Add managementPlan class to mgmt and set it to null");
                        } else if (localName.equals("conservationMeasures") && mgmt != null) {
                            mgmt.setMgmtConservMeasures(localData);
                        } else if (localName.equals("siteManagement_end")) {
                            saveAndReloadSession(session, mgmt, restrictedCodes, siteCode);
                            site.setMgmt(mgmt);
                            mgmt = null;
                        }

                        // MAP
                        else if (localName.equals("map")) {
                            map = new Map();
                        } else if (localName.equals("InspireID") && map != null) {
                            map.setMapInspire(localData);
                        } else if (localName.equals("pdfProvided") && map != null) {
                            if (("true").equals(localData)) {
                                map.setMapPdf(Short.parseShort("1"));
                            } else {
                                map.setMapPdf(Short.parseShort("0"));
                            }
                        } else if (localName.equals("mapReference") && map != null) {
                            map.setMapReference(localData);
                        } else if (localName.equals("map_end")) {
                            site.setMap(map);
                            saveAndReloadSession(session, map, restrictedCodes, siteCode);
                            map = null;
                        } else if (localName.equals("sdf_end")) {
                            Calendar cal = Calendar.getInstance();
                            site.setSiteDateCreation(cal.getTime());
                            saveAndReloadSession(session, site, restrictedCodes, siteCode);
                            session.flush();
                            site = null;
                        }
                    }

                } // localname != null

            } // while
            parser.close();

            if (restrictedCodes.size()==0){
            	ImporterXMLStax.log.info("Import process has finished succesfully");
            	log("Import process has finished succesfully");
            	javax.swing.JOptionPane.showMessageDialog(new Frame(), "Import Processing has finished succesfully.", "Dialog",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
            	String message;
            	if (restrictedCodes.size()==1) {
            		message = "Import process has finished without errors. " + restrictedCodes.size() + " site was not saved, as it was already present in the db.";
            	}
            	else {
            		message = "Import process has finished without errors. " + restrictedCodes.size() + " sites were not saved, as they were already present in the db.";
            	}
            	ImporterXMLStax.log.info(message);
        		log(message);
        		javax.swing.JOptionPane.showMessageDialog(new Frame(), message, "Dialog",
                        JOptionPane.INFORMATION_MESSAGE);
        		File fileLog = SDF_Util.copyToLogImportFileList(restrictedCodes, "OldDB");
        		if (fileLog != null) {
        			Desktop desktop = null;
        			if (Desktop.isDesktopSupported()) {
        				desktop = Desktop.getDesktop();
        				try {
        					Desktop.getDesktop().open(fileLog);
        				} catch (Exception ex) {
        					this.log.error("The error: " + ex.getMessage());
        				}
        			}
        		}
            }
        }  catch (Exception ex) {
            ex.printStackTrace();
            log("An error occurred in the Import Process");
            ImporterXMLStax.log.error("An error occurred in the Import Process.:::" + ex);
            JOptionPane.showMessageDialog(new Frame(), "An error occurred in the Import Process", "Dialog",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            session.clear();
        }
        return true;
    }

    
    /**
     *
     * @param reader
     * @return
     * @throws XMLStreamException
     */
    private static String printEventInfo(XMLStreamReader reader) throws XMLStreamException {
        int eventCode = reader.next();
        String localName = null;
        switch (eventCode) {
            case 1:
                localName = reader.getLocalName();
                break;
            case 2:
                localName = reader.getLocalName() + "_end";
                break;
        }
        return localName;

    }

    /**
     *
     * @param reader
     * @return
     * @throws XMLStreamException
     */
    private static String printEventInfoData(XMLStreamReader reader) throws XMLStreamException {
        int eventCode = reader.next();
        String localData = null;
        switch (eventCode) {
            case 4:
                localData = reader.getText();
                break;
            case 12:
                break;
        }
        return localData;

    }

    /**
     *
     * @param session
     * @param site
     */
    private void saveAndReloadSession(Session session, Site site, List<String> restrictedSiteCodes, String currentSiteCode) {
        /* saving main site obj */
    	if (this.isSaveAllowed(restrictedSiteCodes, currentSiteCode)) {
    		log("Saving in DB:::" + site.getSiteCode());
            ImporterXMLStax.log.info("Saving site with code : " + site.getSiteCode());
    		Transaction tr = session.beginTransaction();
    		session.saveOrUpdate(site);
    		tr.commit();
    		session.flush();
    	}
    	else {
            ImporterXMLStax.log.info("Site with code : " + site.getSiteCode() + " already exists. Did not save.");
            log("Site with code:::" + site.getSiteCode()+ "::already exists in DB.");
    	}

    }

    /***
     *
     * @param session
     * @param mgmt
     */
    private void saveAndReloadSession(Session session, Mgmt mgmt, List<String> restrictedSiteCodes, String currentSiteCode) {
        /* saving main site obj */
    	if (this.isSaveAllowed(restrictedSiteCodes, currentSiteCode)) {
    		Transaction tr = session.beginTransaction();
    		session.saveOrUpdate(mgmt);
    		tr.commit();
    		session.flush();
    	}

    }

    /**
     *
     * @param session
     * @param mgmtBody
     */
    private void saveAndReloadSession(Session session, MgmtBody mgmtBody, List<String> restrictedSiteCodes, String currentSiteCode) {
        /* saving main site obj */
    	if (this.isSaveAllowed(restrictedSiteCodes, currentSiteCode)) {
    		Transaction tr = session.beginTransaction();
    		session.saveOrUpdate(mgmtBody);
    		tr.commit();
    		session.flush();
    	}

    }

    /**
     *
     * @param session
     * @param mgmtPlan
     */
    private void saveAndReloadSession(Session session, MgmtPlan mgmtPlan, List<String> restrictedSiteCodes, String currentSiteCode) {
        /* saving main site obj */
        if (this.isSaveAllowed(restrictedSiteCodes, currentSiteCode)) {
        	Transaction tr = session.beginTransaction();
        	session.saveOrUpdate(mgmtPlan);
        	tr.commit();
        	session.flush();
        }

    }

    /**
     *
     * @param session
     * @param map
     */
    private void saveAndReloadSession(Session session, Map map, List<String> restrictedSiteCodes, String currentSiteCode) {
        /* saving main site obj */
    	if (this.isSaveAllowed(restrictedSiteCodes, currentSiteCode)) {
    		Transaction tr = session.beginTransaction();
    		session.saveOrUpdate(map);
    		// session.merge(site);
    		tr.commit();
    		session.flush();
    	}

    }

    /**
     *
     * @param session
     * @param doc
     */
    private void saveAndReloadSession(Session session, Doc doc, List<String> restrictedSiteCodes, String currentSiteCode) {
        /* saving main site obj */
    	if (this.isSaveAllowed(restrictedSiteCodes, currentSiteCode)) {
    		Transaction tr = session.beginTransaction();
    		session.saveOrUpdate(doc);
    		try {
    			tr.commit();
    		} catch (Exception e) {
    			tr.rollback();
    		}
    		session.flush();
    	}
    }

    /**
     *
     * @param session
     * @param doc
     */
    private void saveAndReloadSession(Session session, DocLink doc, List<String> restrictedSiteCodes, String currentSiteCode) {
        /* saving main site obj */
    	if (this.isSaveAllowed(restrictedSiteCodes, currentSiteCode)) {
    		Transaction tr = session.beginTransaction();
    		session.saveOrUpdate(doc);
    		tr.commit();
    		session.flush();
    	}
    }

    /**
     * Checks whether the current site is part of the set of sites that already exist in the database and thus are not allowed to be stored again.
     * 
     * @param restrictedSiteCodes
     * @param currentSiteCode
     * @return
     */
    private boolean isSaveAllowed(List<String> restrictedSiteCodes, String currentSiteCode) {
    	if (restrictedSiteCodes.contains(currentSiteCode)) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }
    /**
     *
     * @param ownerShipType
     * @return
     */
    private int getOwnerShipId(Session session, String ownerShipType) {
        int ownerShipId = -1;
        String hql = "select ow.ownershipId from Ownership ow where ow.ownershipCode like '" + ownerShipType + "'";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        if (itr.hasNext()) {
            ownerShipId = ((Integer) itr.next()).intValue();
        }
        return ownerShipId;
    }

    /**
     *
     * @param biogeoCode
     * @return
     */
    private int getBiogeoId(Session session, String biogeoCode) {
        int biogeoId = 0;
        String hql = "select distinct biogeo.biogeoId from Biogeo biogeo where biogeo.biogeoCode like '" + biogeoCode + "'";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();

        if (itr.hasNext()) {
            biogeoId = ((Integer) itr.next()).intValue();
        }
        return biogeoId;

    }

    /**
     *
     * @param regionCode
     * @return
     */
    private boolean isRegionLevel2(Session session, String regionCode) {        
        boolean nutsOK = false;

        ImporterXMLStax.log.info("Validating Region Code");
        String hql = "select n.REF_NUTS_DESCRIPTION from ref_nuts where REF_NUTS_CODE='" + regionCode + "'";

        try {
            Query q = session.createQuery(hql);
            if (q.uniqueResult() != null) {
                nutsOK = true;
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ImporterXMLStax.log.error("Error loading Region Description:::" + e);

        }
        return nutsOK;
    }

}
