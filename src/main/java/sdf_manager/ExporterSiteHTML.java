package sdf_manager;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

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
import pojos.SiteOwnership;
import pojos.SiteRelation;
import pojos.Species;
import sdf_manager.util.SDF_Util;
import sdf_manager.util.XmlGenerationUtils;

import com.lowagie.text.pdf.BaseFont;

/**
 *
 * @author charbda
 */
public class ExporterSiteHTML implements Exporter {

    /**
     * Flag to indicate if there have been errors in export.
     */
    private boolean hasErrors = false;

    private String siteCode;
    private String fileName;
    //private Writer writer;
    private ArrayList sitecodes = new ArrayList();
    private File outFile;
    private FileWriter outFileWriter;
    private PrintWriter out;
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExporterSiteHTML.class .getName());

    /**
     *
     * @param siteCode
     */
    public ExporterSiteHTML(String siteCode, String logFile) {
        this.siteCode = siteCode;
        //this.encoding = "UTF-8";
        this.initLogFile(logFile);
    }

     /**
      * Logs to the common SDFManager log file.
      * @param msg text to log
      */
     public void log(String msg) {
         ExporterSiteHTML.log.info(msg);
     }

     /**
      *
      * @param fileName
      */
     public void initLogFile(String fileName) {
         try {
            outFile = new File(fileName);
            outFileWriter = new FileWriter(outFile);
            out = new PrintWriter(outFileWriter);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }

     /**
      * Logs to special Site HTMl log file.
      * @param msg text to log
      */
     private void logToFile(String msg) {
         out.write(msg);
         if (!msg.endsWith("\n")) {
             out.write("\n");
         }
         hasErrors = true;
     }

    /**
     *
     * @param fileName
     * @return
     */
    @Override
    public boolean processDatabase(String fileName) {
        ExporterSiteHTML.log.info("Starting processDatabase. The file name is:::" + fileName);
        try {
            SDF_Util.getProperties();
            this.fileName = fileName;
            this.loadSitecodes();

            this.processDatabase();

        } catch (Exception e) {
            log("An error has occurred in processDatabase. Error Message:::" + e.getMessage() + "\n PLease check the log file for more details");
            //e.printStackTrace();
            ExporterSiteHTML.log.error("An error has occurred in processDatabase. Error Message:::" + e.getMessage());
            return false;
        } finally {
            IOUtils.closeQuietly(out);
            //no need to have an empty logfile if no errors
            if (!hasErrors) {
                FileUtils.deleteQuietly(outFile);
            }
        }
        return true;
    }

    /**
     * Loads the data of the site.
     */
    public void loadSitecodes() {
        ExporterSiteHTML.log.info("Loading the data of the site:::" + siteCode);
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {

            String hql = "select site.siteCode from Site as site where site.siteCode='" + siteCode + "' order by site.siteCode";

            Iterator itrSites = session.createQuery(hql).iterate();

            while (itrSites.hasNext()) {
                Object tuple = itrSites.next();
                String sitecode = (String) tuple;
                this.sitecodes.add(sitecode);
            }
            tx.commit();
            session.close();
        } catch (Exception e) {
            tx.rollback();
            log("An error has occurred in loadSitecodes. Error Message:::" + e.getMessage());
            //e.printStackTrace();
            ExporterSiteHTML.log.error("An error has occurred in loadSitecodes. Error Message:::" + e.getMessage());
        }
    }

    /**
     *
     * @return
     */
    public Document processDatabase() {
        OutputStream os = null;
        try {
             SDF_Util.getProperties();
             DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = dbfac.newDocumentBuilder();

             Session session = HibernateUtil.getSessionFactory().openSession();
             Iterator itrSites = this.sitecodes.iterator();
             int flush = 0;
             ExporterSiteHTML.log.info("Parsing sitecodes...");

             Document doc = docBuilder.newDocument();
             Element sdfs = doc.createElement("sdfs");
             doc.appendChild(sdfs);

             while (itrSites.hasNext()) {
                Element sdf = doc.createElement("sdf");
                Element siteIdentification = doc.createElement("siteIdentification");

                Site site = (Site) session.get(Site.class,(String) itrSites.next()); // results.get(i);

                siteIdentification.appendChild(doc.createElement("siteType")).appendChild(doc.createTextNode(fmt(Character.toString(site.getSiteType()), "siteType")));
                siteIdentification.appendChild(doc.createElement("siteCode")).appendChild(doc.createTextNode(fmt(site.getSiteCode().toUpperCase(), "siteCode")));
                ExporterSiteHTML.log.info("Parsing sitecode:::" + site.getSiteCode());
                siteIdentification.appendChild(doc.createElement("siteName")).appendChild(doc.createTextNode(fmt(site.getSiteName(), "siteName")));

                if (site.getSiteCompDate() != null) {
                    siteIdentification.appendChild(doc.createElement("compilationDate")).appendChild(doc.createTextNode(fmt(SDF_Util.getFormatDateToXML(site.getSiteCompDate()), "compilationDate")));
                }

                if (site.getSiteUpdateDate() != null) {
                    siteIdentification.appendChild(doc.createElement("updateDate")).appendChild(doc.createTextNode(fmt(SDF_Util.getFormatDateToXML(site.getSiteUpdateDate()), "updateDate")));
                }

                Resp resp = site.getResp();
                if (resp != null) {

                    Element respNode = doc.createElement("respondent");
                    respNode.appendChild(doc.createElement("name")).appendChild(doc.createTextNode(fmt(resp.getRespName(), "respName")));
                    if (resp.getRespAddressArea() != null && !resp.getRespAddressArea().equals("")) {

                        Element addresElem = doc.createElement("address");

                        // THE NAME DOES NOT MATCH THEIR RESPECTIVES
                        addresElem.appendChild(doc.createElement("adminUnit")).appendChild(doc.createTextNode(fmt(resp.getRespAdminUnit(), "adminUnit")));
                        addresElem.appendChild(doc.createElement("thoroughfare")).appendChild(doc.createTextNode(fmt(resp.getRespLocatorName(), "locatorName")));
                        addresElem.appendChild(doc.createElement("locatorDesignator")).appendChild(doc.createTextNode(fmt(resp.getRespThoroughFare(), "thoroughfare")));
                        addresElem.appendChild(doc.createElement("postCode")).appendChild(doc.createTextNode(fmt(resp.getRespAddressArea(), "addressArea")));
                        addresElem.appendChild(doc.createElement("postName")).appendChild(doc.createTextNode(fmt(resp.getRespPostName(), "postName")));
                        addresElem.appendChild(doc.createElement("addressArea")).appendChild(doc.createTextNode(fmt(resp.getRespPostCode(), "postCode")));
                        addresElem.appendChild(doc.createElement("locatorName")).appendChild(doc.createTextNode(fmt(resp.getRespLocatorDesig(), "locatorDesignator")));
                        respNode.appendChild(addresElem);

                    } else {
                        Element addresElem = doc.createElement("address");
                        addresElem.appendChild(doc.createElement("addressArea")).appendChild(doc.createTextNode(fmt(resp.getRespAddress(), "addressArea")));
                        respNode.appendChild(addresElem);
                    }

                    respNode.appendChild(doc.createElement("email")).appendChild(doc.createTextNode(fmt(resp.getRespEmail(), "respEmail")));
                    siteIdentification.appendChild(respNode);
                }



                if (SDF_ManagerApp.isEmeraldMode()) {
                    XmlGenerationUtils.appendDateElement(site.getSiteProposedAsciDate(), siteIdentification, "asciProposalDate",
                            doc);
                    if (site.getSiteProposedAsciDate() == null) {
                        XmlGenerationUtils.appendDateElement(XmlGenerationUtils.nullDate(), siteIdentification,
                                "asciProposalDate", doc);
                    }
                    XmlGenerationUtils.appendDateElement(site.getSiteConfirmedCandidateAsciDate(), siteIdentification,
                            "asciConfirmedCandidateDate", doc);
                    XmlGenerationUtils.appendDateElement(site.getSiteConfirmedAsciDate(), siteIdentification,
                            "asciConfirmationDate", doc);
                    XmlGenerationUtils.appendDateElement(site.getSiteDesignatedAsciDate(), siteIdentification,
                            "asciDesignationDate", doc);

                    siteIdentification.appendChild(doc.createElement("asciLegalReference")).appendChild(
                            doc.createTextNode(fmt(site.getSiteAsciLegalRef(), "asciLegalReference")));

                } else {

                    if (site.getSiteSpaDate() != null) {
                        siteIdentification.appendChild(doc.createElement("spaClassificationDate"))
                                .appendChild(
                                        doc.createTextNode(fmt(SDF_Util.getFormatDateToXML(site.getSiteSpaDate()),
                                                "spaClassificationDate")));
                    } else {
                        siteIdentification.appendChild(doc.createElement("spaClassificationDate")).appendChild(
                                doc.createTextNode(fmt("0000-00", "spaClassificationDate")));
                    }

                    siteIdentification.appendChild(doc.createElement("spaLegalReference")).appendChild(
                            doc.createTextNode(fmt(site.getSiteSpaLegalRef(), "spaLegalReference")));

                    if (site.getSiteSciPropDate() != null) {
                        siteIdentification.appendChild(doc.createElement("sciProposalDate"))
                                .appendChild(
                                        doc.createTextNode(fmt(SDF_Util.getFormatDateToXML(site.getSiteSciPropDate()),
                                                "sciProposalDate")));
                    }

                    if (site.getSiteSciConfDate() != null) {
                        siteIdentification.appendChild(doc.createElement("sciConfirmationDate")).appendChild(
                                doc.createTextNode(fmt(SDF_Util.getFormatDateToXML(site.getSiteSciConfDate()),
                                        "sciConfirmationDate")));
                    }

                    if (site.getSiteSacDate() != null) {
                        siteIdentification.appendChild(doc.createElement("sacDesignationDate")).appendChild(
                                doc.createTextNode(fmt(SDF_Util.getFormatDateToXML(site.getSiteSacDate()), "sacDesignationDate")));
                    }

                    siteIdentification.appendChild(doc.createElement("sacLegalReference")).appendChild(
                            doc.createTextNode(fmt(site.getSiteSacLegalRef(), "sacLegalReference")));

                }
                siteIdentification.appendChild(doc.createElement("explanations")).appendChild(doc.createTextNode(fmt(site.getSiteExplanations(), "explanations")));
                sdf.appendChild(siteIdentification);

                /**************LOCATION***************/

                Element location = doc.createElement("siteLocation");
                location.appendChild(doc.createElement("longitude")).appendChild(doc.createTextNode(fmt(site.getSiteLongitude(), "longitude")));
                location.appendChild(doc.createElement("latitude")).appendChild(doc.createTextNode(fmt(site.getSiteLatitude(), "latitude")));
                location.appendChild(doc.createElement("area")).appendChild(doc.createTextNode(fmt(site.getSiteArea(), "area")));
                location.appendChild(doc.createElement("marineAreaPercentage")).appendChild(doc.createTextNode(fmt(site.getSiteMarineArea(), "marineArea")));
                location.appendChild(doc.createElement("siteLength")).appendChild(doc.createTextNode(fmt(site.getSiteLength(), "siteLength")));

                /*regions*/
                Element regions = doc.createElement("adminRegions");
                Set siteRegions = site.getRegions();
                Iterator itr = siteRegions.iterator();
                while (itr.hasNext()) {
                    Region r = (Region) itr.next();
                    Element rElem = doc.createElement("region");
                    rElem.appendChild(doc.createElement("code")).appendChild(doc.createTextNode(fmt(r.getRegionCode(), "regionCode")));
                    //descomentado--> adaptar nuevo xml
                    rElem.appendChild(doc.createElement("name")).appendChild(doc.createTextNode(fmt(r.getRegionName(), "regionName")));
                    regions.appendChild(rElem);
                }
                //adaptacion al nuevo xml
                location.appendChild(regions);


                /*bioregions*/
                Element biogeoRegions = doc.createElement("biogeoRegions");
                Set siteBioRegions = site.getSiteBiogeos();
                if (!(siteBioRegions.isEmpty())) {
                   Iterator itbr = siteBioRegions.iterator();
                   while (itbr.hasNext()) {
                        SiteBiogeo s = (SiteBiogeo) itbr.next();
                        Element biogeoElement = doc.createElement("biogeoRegions");
                        Biogeo b = s.getBiogeo();
                        //this XMl doesn't need validate, so it's better to use bioregion name instead bioregion code
                        biogeoElement.appendChild(doc.createElement("code")).appendChild(doc.createTextNode(fmt(b.getBiogeoName(), "bioRegionCode")));
                        biogeoElement.appendChild(doc.createElement("percentage")).appendChild(doc.createTextNode(fmt(s.getBiogeoPercent(), "biogeoPercent")));
                        location.appendChild(biogeoElement);
                   }

                }

                sdf.appendChild(location);

                /********ECOLOGICAL INFORMATION***********/
                //adptacion nuevo XML
                Element ecologicalInformation = doc.createElement("ecologicalInformation");

                /************HABITATS****************/
                Element habitatsTypes = doc.createElement("habitatTypes");

                Set siteHabs = site.getHabitats();
                itr = siteHabs.iterator();
                while (itr.hasNext()) {
                    Habitat h = (Habitat) itr.next();
                    Element hElem = doc.createElement("habitatType");
                    hElem.appendChild(doc.createElement("code")).appendChild(doc.createTextNode(fmt(h.getHabitatCode(), "habitatCode")));
                    hElem.appendChild(doc.createElement("priorityFormOfHabitatType")).appendChild(doc.createTextNode(fmt(toBoolean(h.getHabitatPriority()), "habitatPriority")));
                    hElem.appendChild(doc.createElement("nonPresenceInSite")).appendChild(doc.createTextNode(fmt(toBoolean(h.getHabitatNp()), "habitatNp")));
                    hElem.appendChild(doc.createElement("coveredArea")).appendChild(doc.createTextNode(fmt(h.getHabitatCoverHa(), "habitatCover")));
                    hElem.appendChild(doc.createElement("caves")).appendChild(doc.createTextNode(fmt(h.getHabitatCaves(), "habitatCaves")));

                    hElem.appendChild(doc.createElement("observationDataQuality")).appendChild(doc.createTextNode(fmt(h.getHabitatDataQuality(), "habitatDataQuality")));
                    hElem.appendChild(doc.createElement("representativity")).appendChild(doc.createTextNode(fmt(h.getHabitatRepresentativity(), "habitatRepresentativity")));
                    hElem.appendChild(doc.createElement("relativeSurface")).appendChild(doc.createTextNode(fmt(h.getHabitatRelativeSurface(), "relativeSurface")));
                    hElem.appendChild(doc.createElement("conservation")).appendChild(doc.createTextNode(fmt(h.getHabitatConservation(), "habitatConservation")));
                    hElem.appendChild(doc.createElement("global")).appendChild(doc.createTextNode(fmt(h.getHabitatGlobal(), "habitatGlobal")));

                    habitatsTypes.appendChild(hElem);

                }
                ecologicalInformation.appendChild(habitatsTypes);

                 /************SPECIES****************/
                Element specieses = doc.createElement("species");
                Set siteSpecies = site.getSpecieses();
                itr = siteSpecies.iterator();
                while (itr.hasNext()) {

                    Species s = (Species) itr.next();
                    Element sElem = doc.createElement("speciesPopulation");
                    sElem.appendChild(doc.createElement("speciesGroup")).appendChild(doc.createTextNode(fmt(s.getSpeciesGroup(), "speciesGroup")));
                    sElem.appendChild(doc.createElement("speciesCode")).appendChild(doc.createTextNode(fmt(s.getSpeciesCode(), "speciesCode")));
                    sElem.appendChild(doc.createElement("scientificName")).appendChild(doc.createTextNode(fmt(s.getSpeciesName(), "speciesName")));

                    sElem.appendChild(doc.createElement("sensitiveInfo")).appendChild(doc.createTextNode(fmt(toBoolean(s.getSpeciesSensitive()), "speciesSensitive")));
                    sElem.appendChild(doc.createElement("nonPresenceInSite")).appendChild(doc.createTextNode(fmt(toBoolean(s.getSpeciesNp()), "speciesNP")));
                    sElem.appendChild(doc.createElement("populationType")).appendChild(doc.createTextNode(fmtToLowerCase(s.getSpeciesType(), "speciesType")));

                    Element popElem = doc.createElement("populationSize");
                    popElem.appendChild(doc.createElement("lowerBound")).appendChild(doc.createTextNode(fmt(s.getSpeciesSizeMin(), "speciesSizeMin")));
                    popElem.appendChild(doc.createElement("upperBound")).appendChild(doc.createTextNode(fmt(s.getSpeciesSizeMax(), "speciesSizeMax")));
                    popElem.appendChild(doc.createElement("countingUnit")).appendChild(doc.createTextNode(fmt(s.getSpeciesUnit(), "speciesUnit")));
                    sElem.appendChild(popElem);

                    if (s.getSpeciesCategory() != null) {
                        if (!("").equals(s.getSpeciesCategory().toString())) {
                            sElem.appendChild(doc.createElement("abundanceCategory")).appendChild(doc.createTextNode(fmtToUpperCase(s.getSpeciesCategory(), "speciesCategory")));
                        }
                    }

                    sElem.appendChild(doc.createElement("dataQuality")).appendChild(doc.createTextNode(fmt(s.getSpeciesDataQuality(), "speciesQuality")));
                    // sElem.appendChild(doc.createElement("observationDataQuality")).appendChild(doc.createTextNode(fmt(s.getSpeciesDataQuality(), "speciesQuality")));
                    sElem.appendChild(doc.createElement("population")).appendChild(doc.createTextNode(fmt(s.getSpeciesPopulation(), "speciesPopulation")));
                    sElem.appendChild(doc.createElement("conservation")).appendChild(doc.createTextNode(fmt(s.getSpeciesConservation(), "speciesConservation")));

                    sElem.appendChild(doc.createElement("isolation")).appendChild(doc.createTextNode(fmt(s.getSpeciesIsolation(), "speciesIsolation")));
                    sElem.appendChild(doc.createElement("global")).appendChild(doc.createTextNode(fmt(s.getSpeciesGlobal(), "speciesGlobal")));


                    specieses.appendChild(sElem);
                }

                siteSpecies = site.getOtherSpecieses();
                itr = siteSpecies.iterator();
                while (itr.hasNext()) {

                    OtherSpecies s = (OtherSpecies) itr.next();
                    Element sElem = doc.createElement("speciesPopulation");

                    sElem.appendChild(doc.createElement("speciesGroup")).appendChild(doc.createTextNode(fmt(s.getOtherSpeciesGroup(), "ospeciesGroup")));
                    sElem.appendChild(doc.createElement("speciesCode")).appendChild(doc.createTextNode(fmt(s.getOtherSpeciesCode(), "ospeciesCode")));
                    sElem.appendChild(doc.createElement("scientificName")).appendChild(doc.createTextNode(fmt(s.getOtherSpeciesName(), "ospeciesName")));
                    sElem.appendChild(doc.createElement("sensitiveInfo")).appendChild(doc.createTextNode(fmt(toBoolean(s.getOtherSpeciesSensitive()), "ospeciesSensitive")));

                    if (s.getOtherSpeciesNp() != null) {
                        if (!(("").equals(s.getOtherSpeciesNp().toString()))) {
                            sElem.appendChild(doc.createElement("nonPresenceInSite")).appendChild(doc.createTextNode(fmt(toBoolean(s.getOtherSpeciesNp()), "ospeciesNP")));
                        }
                    }

                    Element popElem = doc.createElement("populationSize");
                    popElem.appendChild(doc.createElement("lowerBound")).appendChild(doc.createTextNode(fmt(s.getOtherSpeciesSizeMin(), "speciesSizeMin")));
                    popElem.appendChild(doc.createElement("upperBound")).appendChild(doc.createTextNode(fmt(s.getOtherSpeciesSizeMax(), "speciesSizeMax")));
                    popElem.appendChild(doc.createElement("countingUnit")).appendChild(doc.createTextNode(fmt(s.getOtherSpeciesUnit(), "speciesUnit")));
                    sElem.appendChild(popElem);
                    if (s.getOtherSpeciesCategory() != null) {
                        if (!(("").equals(s.getOtherSpeciesCategory().toString()))) {
                            sElem.appendChild(doc.createElement("abundanceCategory")).appendChild(doc.createTextNode(fmt(s.getOtherSpeciesCategory(), "ospeciesCategory")));
                        }

                    }

                     //modificar porque es un tree primero es motivations y despues el nodo motivation (solo en el caso que haya motivations es other species en caso contrario
                    //es species
                     if (s.getOtherSpeciesMotivation() != null && !(("").equals(s.getOtherSpeciesMotivation()))) {
                        Element sElemMot = doc.createElement("motivations");

                        String strMotivation = s.getOtherSpeciesMotivation();
                        StringTokenizer st2 = new StringTokenizer(strMotivation, ",");

                        while (st2.hasMoreElements()) {
                           String mot = (String) st2.nextElement();
                           sElemMot.appendChild(doc.createElement("motivation")).appendChild(doc.createTextNode(fmt(mot, "ospeciesMotivation")));
                           sElem.appendChild(sElemMot);
                        }
                    }

                    specieses.appendChild(sElem);
                }
                ecologicalInformation.appendChild(specieses);

                sdf.appendChild(ecologicalInformation);


                /**************DESCRIPTION***********************/
                Element description = doc.createElement("siteDescription");
                Set classes = site.getHabitatClasses();
                itr = classes.iterator();

                while (itr.hasNext()) {
                    HabitatClass h = (HabitatClass) itr.next();
                    Element cElem = doc.createElement("habitatClass");
                    cElem.appendChild(doc.createElement("code")).appendChild(doc.createTextNode(fmt(h.getHabitatClassCode(), "habitatClassCode")));
                    cElem.appendChild(doc.createElement("coveragePercentage")).appendChild(doc.createTextNode(fmt(h.getHabitatClassCover(), "habitatClassCover")));
                    description.appendChild(cElem);
                }

                description.appendChild(doc.createElement("otherSiteCharacteristics")).appendChild(doc.createTextNode(fmt(site.getSiteCharacteristics(), "otherSiteCharacteristics")));
                description.appendChild(doc.createElement("qualityAndImportance")).appendChild(doc.createTextNode(fmt(site.getSiteQuality(), "qualityAndImportance")));
                Element impacts = doc.createElement("impacts");
                Set siteImpacts = site.getImpacts();
                itr = siteImpacts.iterator();

                while (itr.hasNext()) {

                    Element iElem = doc.createElement("impact");
                    Impact im = (Impact) itr.next();
                    iElem.appendChild(doc.createElement("code")).appendChild(doc.createTextNode(fmt(im.getImpactCode(), "impactCode")));

                    iElem.appendChild(doc.createElement("rank")).appendChild(doc.createTextNode(fmt(im.getImpactRank(), "impactRank")));

                    if (im.getImpactPollutionCode() != null) {
                        if (!("").equals(im.getImpactPollutionCode().toString())) {
                            iElem.appendChild(doc.createElement("pollutionCode")).appendChild(doc.createTextNode(fmt(im.getImpactPollutionCode(), "impactPollution")));
                        }

                    }

                    iElem.appendChild(doc.createElement("occurrence")).appendChild(doc.createTextNode(fmt(im.getImpactOccurrence(), "impactOccurrece")));

                    String impacType = "";
                    if (im.getImpactType() != null) {
                         if (("P").equals(im.getImpactType().toString())) {
                            impacType = "Positive";
                         } else {
                             impacType = "Negative";
                         }
                    }
                    iElem.appendChild(doc.createElement("natureOfImpact")).appendChild(doc.createTextNode(fmt(impacType, "natureOfImpact")));
                    impacts.appendChild(iElem);
                }

                description.appendChild(impacts);

                Element ownership = doc.createElement("ownership");
                Set owners = site.getSiteOwnerships();
                itr = owners.iterator();
                while (itr.hasNext()) {
                    SiteOwnership o = (SiteOwnership) itr.next();
                    Ownership o2 = o.getOwnership();
                    Element oElem = doc.createElement("ownershipPart");
                    oElem.appendChild(doc.createElement("ownershiptype")).appendChild(doc.createTextNode(fmt(o2.getOwnershipCode(), "ownershipType")));
                    oElem.appendChild(doc.createElement("percent")).appendChild(doc.createTextNode(fmt(o.getOwnershipPercent(), "ownershipPercent")));
                    ownership.appendChild(oElem);
                }

                description.appendChild(ownership);

                Element documentation = doc.createElement("documentation");
                Doc docObj = site.getDoc();
                if (docObj != null) {

                    documentation.appendChild(doc.createElement("description")).appendChild(doc.createTextNode(fmt(docObj.getDocDescription(), "docDescription")));
                    Set docLinks = docObj.getDocLinks();
                    itr = docLinks.iterator();
                    Element links = doc.createElement("links");
                    while (itr.hasNext()) {
                        DocLink docLink = (DocLink) itr.next();
                        links.appendChild(doc.createElement("link")).appendChild(doc.createTextNode(fmt(docLink.getDocLinkUrl(), "linkURL")));
                    }
                    documentation.appendChild(links);
                    description.appendChild(documentation);
                }
                sdf.appendChild(description);

                /********PROTECTION**********/
                Element protection = doc.createElement("siteProtection");

                Element natDesigs = doc.createElement("nationalDesignations");
                Set dsigs = site.getNationalDtypes();
                itr = dsigs.iterator();
                while (itr.hasNext()) {
                   NationalDtype dtype = (NationalDtype) itr.next();
                   Element nElem = doc.createElement("nationalDesignation");
                   nElem.appendChild(doc.createElement("designationCode")).appendChild(doc.createTextNode(fmt(dtype.getNationalDtypeCode(), "dtypecode")));
                   nElem.appendChild(doc.createElement("cover")).appendChild(doc.createTextNode(fmt(dtype.getNationalDtypeCover(), "dtypecover")));
                   natDesigs.appendChild(nElem);
                }
                protection.appendChild(natDesigs);


                Set rels = site.getSiteRelations();
                if (!rels.isEmpty()) {
                    Element relations = doc.createElement("relations");
                    Element nationalRelations = doc.createElement("nationalRelationships");
                    Element internationalRelations = doc.createElement("internationalRelationships");

                    itr = rels.iterator();
                    while (itr.hasNext()) {

                        SiteRelation rel = (SiteRelation) itr.next();
                        Element rElem;
                        Character scope = rel.getSiteRelationScope();

                         if (("N").equals(scope.toString())) {
                            rElem = doc.createElement("nationalRelationship");
                            rElem.appendChild(doc.createElement("designationCode")).appendChild(doc.createTextNode(fmt(rel.getSiteRelationCode(), "relationCode")));
                            nationalRelations.appendChild(rElem);
                        } else if (("I").equals(scope.toString())) {
                            rElem =  doc.createElement("internationalRelationship");
                            rElem.appendChild(doc.createElement("convention")).appendChild(doc.createTextNode(fmt(rel.getSiteRelationConvention(), "relationConvention")));
                            internationalRelations.appendChild(rElem);
                        } else {
//                            log("Relation type undefined, ignoring relation: " + scope.toString());
                            continue;
                        }
                        rElem.appendChild(doc.createElement("siteName")).appendChild(doc.createTextNode(fmt(rel.getSiteRelationSitename(), "relationSite")));
                        rElem.appendChild(doc.createElement("type")).appendChild(doc.createTextNode(fmt(rel.getSiteRelationType(), "relationType")));
                        rElem.appendChild(doc.createElement("cover")).appendChild(doc.createTextNode(fmt(rel.getSiteRelationCover(), "relationCover")));
                    }
                    relations.appendChild(nationalRelations);
                    relations.appendChild(internationalRelations);

                    protection.appendChild(relations);
                }

                protection.appendChild(doc.createElement("siteDesignationAdditional")).appendChild(doc.createTextNode(fmt(site.getSiteDesignation(), "siteDesignation")));
                sdf.appendChild(protection);

                /******************MANAGEMENT************************/

                Element mgmtElem = doc.createElement("siteManagement");
                Mgmt mgmt = site.getMgmt();
                if (mgmt != null) {
                    // Management Body
                    Set bodies = mgmt.getMgmtBodies();
                    itr = bodies.iterator();
                    Element bodiesElem = doc.createElement("managementBodies");
                    while (itr.hasNext()) {

                        MgmtBody bodyObj = (MgmtBody) itr.next();
                        Element bElem = doc.createElement("managementBody");
                        bElem.appendChild(doc.createElement("organisation")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyOrg(), "mgmtBodyOrg")));
                        //if el campo addressunestructured esta vacio entonces addres es un tipo complejo (implementar) en caso contrario
                         if (bodyObj.getMgmtBodyAddressArea() != null && !bodyObj.getMgmtBodyAddressArea().equals("")) {
                            Element addresElem = doc.createElement("address");

                            addresElem.appendChild(doc.createElement("adminUnit")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyAdminUnit(), "adminUnit") + "  "));
                            addresElem.appendChild(doc.createElement("locatorDesignator")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyThroughFare(), "thoroughfare") + "  "));
                            addresElem.appendChild(doc.createElement("locatorName")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyLocatorDesignator(), "locatorDesignator") + "  "));
                            addresElem.appendChild(doc.createElement("addressArea")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyPostCode(), "postCode") + "  "));
                            addresElem.appendChild(doc.createElement("postName")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyPostName(), "postName") + "  "));
                            addresElem.appendChild(doc.createElement("postCode")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyAddressArea(), "addressArea") + "  "));
                            addresElem.appendChild(doc.createElement("thoroughfare")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyLocatorName(), "locatorName") + "  "));

                            bElem.appendChild(addresElem);
                        } else {
                            Element addresElem = doc.createElement("address");
                            addresElem.appendChild(doc.createElement("addressArea")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyAddress(), "addressArea")));
                            bElem.appendChild(addresElem);
                        }


                        bElem.appendChild(doc.createElement("email")).appendChild(doc.createTextNode(fmt(bodyObj.getMgmtBodyEmail(), "mgmtBodyMail")));
                        bodiesElem.appendChild(bElem);
                    }
                    mgmtElem.appendChild(bodiesElem);

                    // Management Plan

                    Character status = mgmt.getMgmtStatus();
                    Element mgmtExists = (Element) mgmtElem.appendChild(doc.createElement("exists"));
                    if (status != null) {
                        mgmtExists.appendChild(doc.createTextNode(fmt(Character.toUpperCase(status), "mgmtExists")));
                    }
                    Set plans = mgmt.getMgmtPlans();
                    itr = plans.iterator();
                    Element plansElem = doc.createElement("managementPlans");
                    plansElem.appendChild(mgmtExists);
                    while (itr.hasNext()) {
                        MgmtPlan planObj = (MgmtPlan) itr.next();
                        Element pElem = doc.createElement("managementPlan");
                        pElem.appendChild(doc.createElement("name")).appendChild(doc.createTextNode(fmt(planObj.getMgmtPlanName(), "mgmtPlanName")));
                        pElem.appendChild(doc.createElement("url")).appendChild(doc.createTextNode(fmt(planObj.getMgmtPlanUrl(), "mgmtPlanUrl")));
                        plansElem.appendChild(pElem);
                    }
                    mgmtElem.appendChild(plansElem);

                    mgmtElem.appendChild(doc.createElement("conservationMeasures")).appendChild(doc.createTextNode(fmt(mgmt.getMgmtConservMeasures(), "conservationMeasures")));
                }
                sdf.appendChild(mgmtElem);

                Map map = site.getMap();
                Element mapElem = doc.createElement("map");
                if (map != null) {
                    mapElem.appendChild(doc.createElement("InspireID")).appendChild(doc.createTextNode(fmt(map.getMapInspire(), "mapInspireID")));

                    Boolean bMap;
                    if (map.getMapPdf() != null && map.getMapPdf().equals(Short.valueOf("1"))) {
                        bMap = true;
                    } else {
                        bMap = false;
                    }
                    mapElem.appendChild(doc.createElement("pdfProvided")).appendChild(doc.createTextNode(fmt(bMap, "mapPDF")));
                    mapElem.appendChild(doc.createElement("mapReference")).appendChild(doc.createTextNode(fmt(map.getMapReference(), "mapRef")));
                }
                sdf.appendChild(mapElem);

                if (flush++ % 100 == 0) {
                    session.clear();
                }
                sdfs.appendChild(sdf);
            }

            //All the data is stored in the node instead of the document.
            Source source = new DOMSource(doc);

            // File file = new File(this.fileName);
            File file = new File("xsl" + File.separator + this.siteCode + ".html");

            Result result = new StreamResult(file.toURI().getPath());

            TransformerFactory tFactory = TransformerFactory.newInstance();
            String xslFileName = SDF_ManagerApp.isEmeraldMode() ? "EmeraldSiteXSL.xsl" : "SiteXSL.xsl";

            Source xsl = new StreamSource("." + File.separator + "xsl" + File.separator + xslFileName);
            Templates template = tFactory.newTemplates(xsl);
            Transformer transformer = template.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                    "siteName name otherSiteCharacteristics"
                    + " qualityAndImportance selectiveBasis derogationJustification comments "
                    + "location licensedJustification licensingAuthority licenseValidFrom licenseValidUntil otherType "
                    + "method activity reason");

            transformer.transform(source, result);

            String pdfPath = new File("").getAbsolutePath() + File.separator + "xsl" + File.separator + this.siteCode + ".pdf";
            // File inputFile = new File(this.fileName);
            File inputFile = new File("xsl" + File.separator + this.siteCode + ".html");
            os = new FileOutputStream(new File(pdfPath));

            ITextRenderer renderer = new ITextRenderer();

            ITextFontResolver fontResolver=renderer.getFontResolver();
            String rootFolder = System.getProperty("user.dir");
            fontResolver.addFont(rootFolder + File.separator + "lib" + File.separator + "fonts"
                    + File.separator + "arialuni.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            renderer.setDocument(inputFile);
            renderer.layout();
            renderer.createPDF(os);

            Desktop desktop = null;
            // Before more Desktop API is used, first check
            // whether the API is supported by this particular
            // virtual machine (VM) on this particular host.
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                Desktop.getDesktop().open(file);
            }
            return null;
        } catch (TransformerConfigurationException e) {
            //e.printStackTrace();
            ExporterSiteHTML.log.error("A TransformerConfigurationException has occurred in processDatabase. Error Message:::" + e.getMessage());
            return null;
        } catch (TransformerException e) {
            //e.printStackTrace();
            ExporterSiteHTML.log.error("A TransformerException has occurred in processDatabase. Error Message:::" + e.getMessage());
            return null;
        } catch (FileNotFoundException e) {
            ExporterSiteHTML.log.error("A FileNotFoundException has occurred in processDatabase. Error Message:::" + e.getMessage());
            //e.printStackTrace();
            return null;
        } catch (IOException e) {
            ExporterSiteHTML.log.error("An IOException has occurred in processDatabase. Error Message:::" + e.getMessage());
            //e.printStackTrace();
            return null;
        } catch (Exception e) {
            //e.printStackTrace();
            ExporterSiteHTML.log.error("A general exception has occurred in processDatabase. Error Message:::" + e.getMessage());
            return null;
        } finally {
            IOUtils.closeQuietly(os);
        }

    }

    /**
     *
     * @param i
     * @return
     */
    Boolean toBool(Short i) {
         if (i == null) {
             return false;
         }
         if (i.compareTo(new Short(i)) > 0) {
             return true;
         } else {
             return false;
         }
     }

     /**
     *
     * @param i
     * @return
     */
    Boolean toBoolean(Short i) {
         if (i == null) {
             return false;
         }
         if (i > 0) {
             return true;
         } else {
             return false;
         }
     }

     /**
      *
      * @param src
      * @param fieldName
      * @return
      */
     String fmt(String src, String fieldName) {
        /* basically a debugging function, printing out null fields
           but could be used for other purposes as well */
        if (src == null) {
            return "";
        } else {
            return src;
        }
    }

     /**
      *
      * @param date
      * @param fieldName
      * @return
      */
      String fmt(Date date, String fieldName) {
        if (date != null) {
            return fmt(date.toString(), fieldName);
        } else {
            return fmt((String) null, fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmt(Double val, String fieldName) {
        if (val != null) {
            return fmt(val.toString(), fieldName);
        } else {
            return fmt((String) null, fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmt(Integer val, String fieldName) {
        if (val != null) {
            return fmt(val.toString(), fieldName);
        } else {
            return fmt((String) null, fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmt(Boolean val, String fieldName) {
        if (val != null) {
            return fmt(val.toString(), fieldName);
        } else {
            return fmt((String) null, fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmt(Character val, String fieldName) {
        if (val != null) {
            return fmt(val.toString(), fieldName);
        } else {
            return fmt((String) null, fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmtToLowerCase (Character val, String fieldName) {
        if (val != null) {
            return fmt(val.toString().toLowerCase(), fieldName);
        } else {
            return fmt((String) null, fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
     String fmtToUpperCase (Character val, String fieldName) {
        if (val != null) {
            return fmt(val.toString().toUpperCase(), fieldName);
        } else {
            return fmt((String) null, fieldName);
        }
    }

    /**
     *
     * @param filename
     * @return
     */
    @Override
    public ArrayList createXMLFromDataBase(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param fieldName
     * @return
     */
    private String getString(String fieldName) {
         try {

             byte[] result = fieldName.getBytes();
             //String tmp = result != null ? new String(result) : null;
             if (result != null && result.length == 0) {
                 //log("Empty string for: " + fieldName);
                 return null;
             } //don't enter empty string in the database
             else {
                 if (result != null) {
                     Charset charset = Charset.forName("UTF-8");
                     CharsetDecoder decoder = charset.newDecoder();
                     decoder.onMalformedInput(CodingErrorAction.REPLACE);
                     decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
                     CharBuffer cbuf = decoder.decode(ByteBuffer.wrap(result));
                     return cbuf.toString().trim();
                 } else {
                     return null;
                 }
             }
         } catch (Exception e) {
             logToFile("Failed extracting field: " + fieldName + ". The field could have an erroneous name. Please verify.");
             //e.printStackTrace();
             ExporterSiteHTML.log.error("Failed extracting field: " + fieldName
                     + ". The field could have an erroneous name. Please verify.");
             return null;
         }
     }

}
