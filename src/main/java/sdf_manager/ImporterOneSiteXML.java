package sdf_manager;

import java.awt.Frame;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;



import org.hibernate.Query;
import pojos.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sdf_manager.util.SDF_Constants;
import sdf_manager.util.SDF_Util;


public class ImporterOneSiteXML implements Importer {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImporterOneSiteXML.class .getName());
    private Logger logger;
    private String encoding;
    private String fileName;
    private FileWriter outFile;
    private PrintWriter out;
    private String siteCode;

    /**
     *
     * @param logger
     * @param encoding
     * @param logFile
     * @param fileName
     */
    public ImporterOneSiteXML(Logger logger, String encoding, String logFile, String fileName, String siteCode) {
        this.logger = logger;
        this.encoding = encoding;
        this.initLogFile(logFile);
        this.siteCode = siteCode;
    }

    /**
     *
     */
    void init() {
    }

    /**
     *
     * @param msg
     */
    public void log(String msg) {
         this.logger.log(msg);
     }

    /**
     *
     * @param msg
     * @param priority
     */
    public void log(String msg, int priority) {
         if (priority == 1) {
            this.logger.log(msg);
            logToFile(msg);
         } else {
            logToFile(msg);
         }
     }

    /**
     *
     * @param fileName
     */
    public void initLogFile(String fileName) {
         try {

            outFile = new FileWriter(fileName);
            out = new PrintWriter(outFile);
         }
         catch (Exception e) {
             ImporterOneSiteXML.log.error("An error has occurred in initLogFile. Error Message :::" + e.getMessage());
             //e.printStackTrace();
         }
     }

     /**
      *
      */
     public void closeLogFile() {
         try {
             out.close();
             outFile.close();
         }
         catch (Exception e) {
             ImporterOneSiteXML.log.error("An error has occurred in closeLogFile. Error Message :::" + e.getMessage());
             //e.printStackTrace();
         }
     }


     /**
      *
      * @param msg
      */
     void logToFile(String msg) {
         out.write(msg);
         if (!msg.endsWith("\n")) {
             out.write("\n");
         }
     }


     /**
      *
      */
     void flushFile() {
         out.flush();
     }


    /**
     *
     * @param fileName
     * @return
     */
    public boolean validateAndProcessDB(String fileName) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            ImporterOneSiteXML.log.info("Init validate process");
            boolean sitesDB = validateSites(session);
            ImporterOneSiteXML.log.info("Validation has finished");
            log("Validation has finished.",1);
            if (!sitesDB) {
                ImporterOneSiteXML.log.info("Import process is starting");
                log("Import process is starting.",1);
                this.processDatabase(session, fileName);
            } else {
                ImporterOneSiteXML.log.error("Error in validation");
                log("Error in validation.",1);
                JOptionPane.showMessageDialog(new JFrame(), "Some sites are already stored in Data Base. Please check the log file for details", "Dialog",JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            session.flush();
            session.clear();

        } catch (Exception e) {
            ////e.printStackTrace();
            ImporterOneSiteXML.log.error("Error in import process:::" + e.getMessage());
            return false;
        } finally {
            session.clear();
            session.close();
        }
        return true;
    }


    /**
      *
      * @param conn
      */
     private boolean validateSites(Session session) {
        boolean siteInDB = false;
        try {
           if (SDF_Util.validateSite(session, this.siteCode)) {
               siteInDB = true;
           }
           session.flush();
           session.clear();
        } catch (Exception e) {
            ImporterOneSiteXML.log.error("Error validating Site:::" + e.getMessage());
        }
        return siteInDB;


     }

     /**
      *
      * @param fileName
      * @return
      */
     public boolean processDatabase(String fileName) {
        return  validateAndProcessDB(fileName);
     }

     /**
      *
      * @param session
      * @param fileName
      * @return
      */
     public boolean processDatabase(Session session,String fileName) {
        this.fileName = fileName;
        boolean isOK = true;
        try {

            ImporterOneSiteXML.log.info("Import process from file:::" + fileName);

            log("Init Import process.");

            isOK = getNodeSite(session, fileName);
            if (isOK) {
                ImporterOneSiteXML.log.info("Import process has finished succesfully");
                log("Import process has finished succesfully" );
                javax.swing.JOptionPane.showMessageDialog(new Frame(), "Import Processing has finished succesfully.", "Dialog",JOptionPane.INFORMATION_MESSAGE);;
            } else {
               log("It's been produced an error in the Import Process" );
                ImporterOneSiteXML.log.error("It's been produced an error in the Import Process.\nPlease check the log file for more details");
                JOptionPane.showMessageDialog(new Frame(), "It's been produced an error in the Import Process.Please check the log file for more details", "Dialog",JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            //ex.printStackTrace();
            log("It's been produced an error in the Import Process" );
            ImporterOneSiteXML.log.error("It's been produced an error in the Import Process.:::" + ex.getMessage());
            JOptionPane.showMessageDialog(new Frame(), "It's been produced an error in the Import Process", "Dialog",JOptionPane.ERROR_MESSAGE);
            isOK = false;
        } finally {
            session.clear();
        }
        return isOK;
    }


    /**
     *
     * @param session
     * @param site
     */
    private void saveAndReloadSession(Session session, Site site) {
       /*saving main site obj*/
        Transaction tr = session.beginTransaction();
        session.saveOrUpdate(site);
        tr.commit();
       session.flush();

    }

    /***
     *
     * @param session
     * @param mgmt
     */
    private void saveAndReloadSession(Session session, Mgmt mgmt) {
       /*saving main site obj*/
        Transaction tr = session.beginTransaction();
        session.saveOrUpdate(mgmt);
        tr.commit();
       session.flush();

    }

    /**
     *
     * @param session
     * @param mgmtBody
     */
    private void saveAndReloadSession(Session session, MgmtBody mgmtBody) {
       /*saving main site obj*/
        Transaction tr = session.beginTransaction();
        session.saveOrUpdate(mgmtBody);
        tr.commit();
       session.flush();

    }

    /**
     *
     * @param session
     * @param mgmtPlan
     */
    private void saveAndReloadSession(Session session, MgmtPlan mgmtPlan) {
       /*saving main site obj*/
        Transaction tr = session.beginTransaction();
        session.saveOrUpdate(mgmtPlan);
        tr.commit();
       session.flush();

    }

    /**
     *
     * @param session
     * @param map
     */
    private void saveAndReloadSession(Session session, Map map) {
       /*saving main site obj*/
        Transaction tr = session.beginTransaction();
        session.saveOrUpdate(map);
        tr.commit();
       session.flush();

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
    private int getBiogeoId( Session session, String biogeoCode) {
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

        //SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

        boolean nutsOK = false;

        ImporterOneSiteXML.log.info("Validating Region Code");
        String hql="select n.REF_NUTS_DESCRIPTION from natura2000.ref_nuts where REF_NUTS_CODE='" + regionCode + "'";

        try {
            Query q = session.createQuery(hql);
            if (q.uniqueResult() != null) {
               nutsOK = true;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            ImporterOneSiteXML.log.error("Error loading Region Descrption:::" + e.getMessage());

        }
        return nutsOK;
    }


    private boolean getNodeSite(Session session,String fileName) {

        boolean importOK = false;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder;
            Document document = null;
            XPathExpression expr = null;
            builder = factory.newDocumentBuilder();
            document = builder.parse(fileName);

            // Create a XPathFactory

            XPathFactory xFactory = XPathFactory.newInstance();

            // Create a XPath object
            XPath xpath = xFactory.newXPath();

            /******/
            Site site= new Site();

            // Compile the XPath expression
            log("Processing ...:" + this.siteCode);
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/siteCode/text()");

            // Run the query and get a nodeset
            Object result = expr.evaluate(document, XPathConstants.NODE);

            // Cast the result to a DOM NodeList
            Node nodes = (Node) result;
            //Site code
            site.setSiteCode(nodes.getNodeValue());

            //Site Type
            log("Processing Site Type");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/siteType/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            nodes = (Node) result;
            site.setSiteType(nodes.getNodeValue().charAt(0));


            //Site Name
            log("Processing Site Name");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/siteName/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            nodes = (Node) result;
            site.setSiteName(nodes.getNodeValue());

            //Compilation Date
            log("Processing Compilation Date");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/compilationDate/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
               nodes = (Node) result;
               if (nodes.getNodeValue() != null && !((SDF_Constants.NULL_DATE).equals(nodes.getNodeValue()))) {
                   site.setSiteCompDate(ConversionTools.convertStringToDate(nodes.getNodeValue()));
               }
            }


            //Update Date
            log("Processing Update Date");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/updateDate/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                if (nodes.getNodeValue() != null  && !((SDF_Constants.NULL_DATE).equals(nodes.getNodeValue()))) {
                   site.setSiteUpdateDate(ConversionTools.convertStringToDate(nodes.getNodeValue()));
                }
            }


            //SPA Classification Date
            log("Processing SPA Classification Date");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/spaClassificationDate/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                if (nodes.getNodeValue() != null  && !((SDF_Constants.NULL_DATE).equals(nodes.getNodeValue()))) {
                    site.setSiteSpaDate(ConversionTools.convertStringToDate(nodes.getNodeValue()));
                }
            }

            //SPA Legal Reference
            log("Processing SPA Legal Reference");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/spaLegalReference/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                if (nodes.getNodeValue() != null) {
                    site.setSiteSpaLegalRef(nodes.getNodeValue());
                }
            }


            //SCI Confirmation Date
            log("Processing SCI Confirmation Date");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/sciConfirmationDate/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                if (nodes.getNodeValue() != null  && !((SDF_Constants.NULL_DATE).equals(nodes.getNodeValue()))) {
                    site.setSiteSciConfDate(ConversionTools.convertStringToDate(nodes.getNodeValue()));
                }
            }


            //SCI Proposal Date
            log("Processing SCI Proposal Date");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/sciProposalDate/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                if (nodes.getNodeValue() != null  && !((SDF_Constants.NULL_DATE).equals(nodes.getNodeValue()))) {
                    site.setSiteSciPropDate(ConversionTools.convertStringToDate(nodes.getNodeValue()));
                }
            }


            //SAC Designation Date
            log("Processing SAC Designation Date");
            ImporterOneSiteXML.log.info("Processing SAC Designation Date");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/sacDesignationDate/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
              nodes = (Node) result;
              if (nodes.getNodeValue() != null  && !((SDF_Constants.NULL_DATE).equals(nodes.getNodeValue()))) {
                   site.setSiteSacDate(ConversionTools.convertStringToDate(nodes.getNodeValue()));
              }
            }


            //SAC Designation Date
            log("Processing SAC Legal Reference");
            ImporterOneSiteXML.log.info("Processing SAC Legal Reference");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/sacLegalReference/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                site.setSiteSacLegalRef(nodes.getNodeValue());
            }


            //Explanations
            log("Processing Explanations");
            ImporterOneSiteXML.log.info("Processing Explanations");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/explanations/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                site.setSiteExplanations(nodes.getNodeValue());
            }

            //Respondent-Name
            log("Processing Respondent-Name");
            ImporterOneSiteXML.log.info("Processing Respondent-Name");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/name/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            Resp resp = new Resp();
            if (result != null) {
                nodes = (Node) result;
                resp.setRespName(nodes.getNodeValue());
            }


            //Respondent-Admin Unit
            log("Processing Respondent-Admin Unit");
            ImporterOneSiteXML.log.info("Processing Respondent-Admin Unit");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/address/adminUnit/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                resp.setRespAdminUnit(nodes.getNodeValue());
            }


            //Respondent-Locator Designator
            log("Processing Respondent-Locator Designator");
            ImporterOneSiteXML.log.info("Processing Respondent-Locator Designator");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/address/locatorDesignator/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                resp.setRespLocatorDesig(nodes.getNodeValue());
            }


            //Respondent-Locator Name
            log("Processing Respondent-Locator Name");
            ImporterOneSiteXML.log.info("Processing Respondent-Locator Name");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/address/locatorName/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                resp.setRespLocatorName(nodes.getNodeValue());
            }


            //Respondent-Address Area
            log("Processing Respondent-Address Area");
            ImporterOneSiteXML.log.info("Processing Respondent-Address Area");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/address/addressArea/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                resp.setRespAddressArea(nodes.getNodeValue());
            }


            //Respondent-Post Name
            log("Processing Respondent-Post Name");
            ImporterOneSiteXML.log.info("Processing Respondent-Post Name");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/address/postName/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                resp.setRespPostName(nodes.getNodeValue());
            }


            //Respondent-Post Code
            log("Processing Respondent-Post Code");
            ImporterOneSiteXML.log.info("Processing Respondent-Post Code");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/address/postCode/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                resp.setRespPostCode(nodes.getNodeValue());
            }


            //Respondent-Thoroughfare
            log("Processing Respondent-Thoroughfare");
            ImporterOneSiteXML.log.info("Processing Respondent-Thoroughfare");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/address/thoroughfare/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                resp.setRespThoroughFare(nodes.getNodeValue());
            }


            //Respondent-Unstructured Address
            log("Processing Respondent-Unstructured Address");
            ImporterOneSiteXML.log.info("Processing Respondent-Unstructured Address");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/addressUnstructured/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                resp.setRespAddress(nodes.getNodeValue());
            }


            //Respondent-Email
            log("Processing Respondent-Email");
            ImporterOneSiteXML.log.info("Processing Respondent-Email");
            expr = xpath.compile("//sdf/siteIdentification[siteCode='" + this.siteCode + "']/respondent/email/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
               nodes = (Node) result;
               resp.setRespEmail(nodes.getNodeValue());
            }

            site.setResp(resp);


            //Site Location-Longitude
            log("Processing Site Location-Longitude");
            ImporterOneSiteXML.log.info("Processing Site Location-Longitude");
            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/longitude/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                site.setSiteLongitude(ConversionTools.stringToDouble(nodes.getNodeValue()));
            }

            //Site Location-Latitude
            log("Processing Site Location-Latitude");
            ImporterOneSiteXML.log.info("Processing Site Location-Latitude");
            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/latitude/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                site.setSiteLatitude(ConversionTools.stringToDouble(nodes.getNodeValue()));
            }

            //Site Location-Area
            log("Processing Site Location-Area");
            ImporterOneSiteXML.log.info("Processing Site Location-Area");
            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/area/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                site.setSiteArea(ConversionTools.stringToDouble(nodes.getNodeValue()));
            }

            //Site Location-Marine Area
            log("Processing Site Location-Marine Area");
            ImporterOneSiteXML.log.info("Processing Site Location-Marine Area");
            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/marineAreaPercentage/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                site.setSiteMarineArea(ConversionTools.stringToDouble(nodes.getNodeValue()));
            }

            //Site Location-Length
            log("Processing Site Location-Length");
            ImporterOneSiteXML.log.info("Processing Site Location-Length");
            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/siteLength/text()");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                nodes = (Node) result;
                site.setSiteLength(ConversionTools.stringToDouble(nodes.getNodeValue()));
            }


            //Site Location-Admin Regions
            log("Processing Site Location-Admin Regions");
            ImporterOneSiteXML.log.info("Processing Site Location-Admin Regions");
            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/adminRegions");
            // Run the query and get a nodeset
            result = expr.evaluate(document, XPathConstants.NODE);
            if (result != null) {
                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/adminRegions/region");
                result = expr.evaluate(document, XPathConstants.NODESET);
                if (result != null) {
                    NodeList nodeList = (NodeList) result;
                    for (int i = 1; i <= nodeList.getLength(); i++) {
                        Region region = new Region();
                        String regCode = null;
                        String regName = null;
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/adminRegions/region[" + i + "]/code/text()");
                        result = expr.evaluate(document, XPathConstants.NODE);
                        if (result != null) {
                            Node nDataReg = (Node) result;
                            regCode = nDataReg.getNodeValue();
                            //Site Location-Admin Regions-Region
                            log("         Processing Region:::" + regCode);
                            ImporterOneSiteXML.log.info("Processing Site Location-Admin Regions-Region:::" + regCode);
                            boolean regionOK=isRegionLevel2(session, regCode);
                            if (!regionOK) {
                                log("         Region Code:::" + regCode + " doesn't belong to nut level 2:::");
                                ImporterOneSiteXML.log.info("Region Code:::" + regCode + " doesn't belong to nut level 2:::");
                            }


                        }

                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/adminRegions/region[" + i + "]/name/text()");
                        result = expr.evaluate(document, XPathConstants.NODE);
                        if (result != null) {
                            Node nDataReg = (Node) result;
                            regName = nDataReg.getNodeValue();
                        }
                        if (regCode != null) {
                            region.setRegionCode(regCode);
                        }
                        if (regName != null) {
                            region.setRegionName(regName);
                        }
                        region.setSite(site);
                        site.getRegions().add(region);

                    }
                }

                //Site Location-BioRegions
                log("Processing Site Location-BioRegions");
                ImporterOneSiteXML.log.info("Processing Site Location-BioRegions");
                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/biogeoRegions");
                // Run the query and get a nodeset
                result = expr.evaluate(document, XPathConstants.NODESET);
                Set bioRegion = null;
                if (result != null) {
                    NodeList nodeBio = (NodeList) result;
                    bioRegion = new HashSet();
                    for (int i = 1; i <= nodeBio.getLength(); i++) {
                        SiteBiogeo siteBio = null;
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/biogeoRegions[" + i + "]/code/text()");
                        result = expr.evaluate(document, XPathConstants.NODE);
                        if (result != null) {
                            Node nBioReg = (Node) result;
                            //siteBio.set
                            String biogeoCode = nBioReg.getNodeValue();
                            //Site Location-BioRegions-Biogeo
                            log("         Processing BioRegion:::" + biogeoCode);
                            ImporterOneSiteXML.log.info("Processing Site Location-BioRegions:::" + biogeoCode);
                            int biogeoId = getBiogeoId(session, biogeoCode);
                            Biogeo biogeo = (Biogeo) session.load(Biogeo.class, biogeoId);
                            SiteBiogeoId id = new SiteBiogeoId(site.getSiteCode(), biogeo.getBiogeoId());
                            siteBio = new SiteBiogeo(id, biogeo, site);
                        }
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteLocation/biogeoRegions[" + i + "]/percentage/text()");
                        result = expr.evaluate(document, XPathConstants.NODE);
                        if (result != null) {
                            Node nBioRegPer = (Node) result;
                            //siteBio.set
                            String biogeoPercent = nBioRegPer.getNodeValue();
                            if (siteBio != null) {
                                siteBio.setBiogeoPercent(new Double(biogeoPercent));
                            }
                        }
                        if (siteBio != null) {
                            site.getSiteBiogeos().add(siteBio);
                        }
                    }
                }

                //Ecological Information-Habitat
                log("Processing Ecological Information-Habitat");
                ImporterOneSiteXML.log.info("Processing Ecological Information-Habitat");
                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes");
                // Run the query and get a nodeset
                result = expr.evaluate(document, XPathConstants.NODE);
                if (result != null) {
                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType");
                        // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODESET);
                    if (result != null) {
                        NodeList nodeHab = (NodeList) result;
                        for (int i = 1; i <= nodeHab.getLength(); i++) {
                           Habitat habitat = new Habitat();
                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/code/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                habitat.setHabitatCode(nodeData.getNodeValue());
                            }

                            //Ecological Information-Habitat
                            log("         Processing Habitat:::" + habitat.getHabitatCode());
                            ImporterOneSiteXML.log.info("Processing Habitat:::" + habitat.getHabitatCode());

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/priorityFormOfHabitatType/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                if (Boolean.parseBoolean(nodeData.getNodeValue())) {
                                    habitat.setHabitatPriority(Short.parseShort("0"));
                                } else {
                                    habitat.setHabitatPriority(Short.parseShort("1"));
                                }
                            }

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/nonPresenceInSite/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                if (Boolean.parseBoolean(nodeData.getNodeValue())) {
                                    habitat.setHabitatNp(Short.parseShort("0"));
                                } else {
                                    habitat.setHabitatNp(Short.parseShort("1"));
                                }
                            }

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/coveredArea/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                habitat.setHabitatCover(Double.parseDouble(nodeData.getNodeValue()));
                            }

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/caves/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                habitat.setHabitatCaves(Integer.parseInt(nodeData.getNodeValue()));
                            }

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/observationDataQuality/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                habitat.setHabitatDataQuality(nodeData.getNodeValue());
                            }

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/representativity/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                if (nodeData.getNodeValue() != null) {
                                    habitat.setHabitatRepresentativity(nodeData.getNodeValue().charAt(0));
                                }
                            }

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/relativeSurface/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                if (nodeData.getNodeValue() != null) {
                                    habitat.setHabitatRelativeSurface(nodeData.getNodeValue().charAt(0));
                                }
                            }

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/conservation/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                if (nodeData.getNodeValue() != null) {
                                    habitat.setHabitatConservation(nodeData.getNodeValue().charAt(0));
                                }
                            }

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/habitatTypes/habitatType[" + i + "]/global/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                if (nodeData.getNodeValue() != null) {
                                    habitat.setHabitatGlobal(nodeData.getNodeValue().charAt(0));
                                }
                            }
                            habitat.setSite(site);
                            site.getHabitats().add(habitat);
                        }
                    }
                }


                //Ecological Information-Species
                log("Processing Ecological Information-Species");
                ImporterOneSiteXML.log.info("Processing Ecological Information-Species");
                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species");
                // Run the query and get a nodeset
                result = expr.evaluate(document, XPathConstants.NODE);
                if (result != null) {
                    //NodeList nodeList = (NodeList) result;
                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation");
                    // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODESET);
                    if (result != null) {
                        NodeList nodeList = (NodeList) result;
                        for (int i = 1; i <= nodeList.getLength(); i++) {
                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/motivations/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                OtherSpecies species = new OtherSpecies();
                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/speciesGroup/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    species.setOtherSpeciesGroup(nodeData.getNodeValue());
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/speciesCode/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setOtherSpeciesCode(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/scientificName/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setOtherSpeciesName(nodeData.getNodeValue());
                                    }
                                }

                                //Ecological Information-Species
                                log("         Processing Species:::" + species.getOtherSpeciesCode() + "::: Species name ::" + species.getOtherSpeciesName());
                                ImporterOneSiteXML.log.info("Processing Species:::" + species.getOtherSpeciesCode() + "::: Species name ::" + species.getOtherSpeciesName());

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/sensitiveInfo/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (("true").equals(nodeData.getNodeValue())) {
                                        species.setOtherSpeciesSensitive(Short.parseShort("1"));
                                    } else {
                                        species.setOtherSpeciesSensitive(Short.parseShort("0"));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/nonPresenceInSite/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (("true").equals(nodeData.getNodeValue())) {
                                        species.setOtherSpeciesNp(Short.parseShort("1"));
                                    } else {
                                        species.setOtherSpeciesNp(Short.parseShort("0"));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/populationSize/lowerBound/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setOtherSpeciesSizeMin(Integer.parseInt(nodeData.getNodeValue()));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/populationSize/upperBound/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setOtherSpeciesSizeMax(Integer.parseInt(nodeData.getNodeValue()));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/populationSize/countingUnit/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setOtherSpeciesUnit(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/abundanceCategory/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setOtherSpeciesCategory(nodeData.getNodeValue().charAt(0));
                                    }
                                }


                                 expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/motivations/motivation");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODESET);
                                if (result != null) {
                                    NodeList nodeMot = (NodeList) result;
                                    StringBuffer strMot = new StringBuffer();
                                    for (int j = 1; j <= nodeMot.getLength(); j++) {
                                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/motivations/motivation[" + j + "]/text()");
                                        // Run the query and get a nodeset
                                        result = expr.evaluate(document, XPathConstants.NODE);
                                        if (result != null) {
                                            Node nodeMotv = (Node) result;
                                            if (nodeMotv.getNodeValue() != null) {
                                              strMot.append(nodeMotv.getNodeValue());
                                              strMot.append(",");
                                            }
                                        }
                                    }
                                    String motiva = strMot.toString();
                                    motiva = motiva.substring(0, motiva.length() - 1);
                                    species.setOtherSpeciesMotivation(motiva);
                                }
                                species.setSite(site);
                                site.getOtherSpecieses().add(species);
                            } else {
                                Species species = new Species();
                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/speciesGroup/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    species.setSpeciesGroup(nodeData.getNodeValue().charAt(0));
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/speciesCode/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesCode(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/scientificName/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesName(nodeData.getNodeValue());
                                    }
                                }

                                //Ecological Information-Species
                                log("         Processing Species:::" + species.getSpeciesCode() + "::: Species name ::" + species.getSpeciesName());
                                ImporterOneSiteXML.log.info("Processing Species:::" + species.getSpeciesCode() + "::: Species name ::" + species.getSpeciesName());

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/sensitiveInfo/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                        if (("true").equals(nodeData.getNodeValue())) {
                                            species.setSpeciesSensitive(Short.parseShort("1"));
                                        } else {
                                            species.setSpeciesSensitive(Short.parseShort("0"));
                                        }
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/nonPresenceInSite/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                        if (("true").equals(nodeData.getNodeValue())) {
                                            species.setSpeciesNp(Short.parseShort("1"));
                                        } else {
                                            species.setSpeciesNp(Short.parseShort("0"));
                                        }
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/populationType/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesType(nodeData.getNodeValue().charAt(0));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/populationSize/lowerBound/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesSizeMin(Integer.parseInt(nodeData.getNodeValue()));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/populationSize/upperBound/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesSizeMax(Integer.parseInt(nodeData.getNodeValue()));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/populationSize/countingUnit/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesUnit(nodeData.getNodeValue());
                                    }
                                }


                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/abundanceCategory/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesCategory(nodeData.getNodeValue().charAt(0));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/observationDataQuality/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesDataQuality(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/population/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesPopulation(nodeData.getNodeValue().charAt(0));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/conservation/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesConservation(nodeData.getNodeValue().charAt(0));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/isolation/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesIsolation(nodeData.getNodeValue().charAt(0));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/ecologicalInformation/species/speciesPopulation[" + i + "]/global/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      species.setSpeciesGlobal(nodeData.getNodeValue().charAt(0));
                                    }
                                }
                                species.setSite(site);
                                site.getSpecieses().add(species);
                            }
                        }
                    }
                }


                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription");
                // Run the query and get a nodeset
                result = expr.evaluate(document, XPathConstants.NODE);

                if (result != null) {
                    //Site Description-Habitat Class
                    log("Processing Site Description-Habitat Class");
                    ImporterOneSiteXML.log.info("Processing Site Description-Habitat Class");
                    //NodeList nodeList = (NodeList) result;
                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/habitatClass");
                     // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODESET);
                    if (result != null) {
                        NodeList nodeHabClass = (NodeList) result;
                        for (int i = 1; i <= nodeHabClass.getLength(); i++) {
                            HabitatClass habClass = new HabitatClass();

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/habitatClass[" + i + "]/code/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                if (nodeData.getNodeValue() != null) {
                                  habClass.setHabitatClassCode(nodeData.getNodeValue());
                                }
                            }

                            //Site Description-Habitat Class
                            log("         Processing Habitat Class:::" + habClass.getHabitatClassCode());
                            ImporterOneSiteXML.log.info("Processing Habitat Class:::" + habClass.getHabitatClassCode());

                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/habitatClass[" + i + "]/coveragePercentage/text()");
                            // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                if (nodeData.getNodeValue() != null) {
                                  habClass.setHabitatClassCover(Double.parseDouble(nodeData.getNodeValue()));
                                }
                            }
                            habClass.setSite(site);
                            site.getHabitatClasses().add(habClass);
                        }
                    }

                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/otherSiteCharacteristics/text()");
                     // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {
                        Node nodeData = (Node) result;
                        if (nodeData.getNodeValue() != null) {
                          site.setSiteCharacteristics(nodeData.getNodeValue());
                        }
                    }

                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/qualityAndImportance/text()");
                     // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {
                        Node nodeData = (Node) result;
                        if (nodeData.getNodeValue() != null) {
                          site.setSiteQuality(nodeData.getNodeValue());
                        }
                    }


                    //Site Description-Impacts
                    log("Processing Site Description-Impacts");
                    ImporterOneSiteXML.log.info("Processing ite Description-Impacts");
                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/impacts");

                     // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {

                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/impacts/impact");
                         // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODESET);
                        if (result != null) {
                            NodeList nodeImpacts = (NodeList) result;
                            for (int i = 1; i <= nodeImpacts.getLength(); i++) {
                                Impact impact = new Impact();
                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/impacts/impact[" + i + "]/impactCode/text()");
                                // expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/impacts/impact[" + i + "]/code/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      impact.setImpactCode(nodeData.getNodeValue());
                                    }
                                }

                                //Site Description-Impact
                                log("         Processing Impact:::" + impact.getImpactCode());
                                ImporterOneSiteXML.log.info("Processing Impact:::" + impact.getImpactCode());

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/impacts/impact[" + i + "]/rank/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                        if (nodeData.getNodeValue().equals("A")) {
                                            impact.setImpactRank("H".charAt(0));
                                        } else if (nodeData.getNodeValue().equals("B")) {
                                            impact.setImpactRank("M".charAt(0));
                                        } else if (nodeData.getNodeValue().equals("C")) {
                                            impact.setImpactRank("L".charAt(0));
                                        } else {
                                            impact.setImpactRank(nodeData.getNodeValue().charAt(0));
                                        }
                                        // impact.setImpactRank(nodeData.getNodeValue().charAt(0));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/impacts/impact[" + i + "]/pollutionCode/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      impact.setImpactPollutionCode(nodeData.getNodeValue().charAt(0));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/impacts/impact[" + i + "]/occurrence/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      impact.setImpactOccurrence(nodeData.getNodeValue().charAt(0));
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/impacts/impact[" + i + "]/natureOfImpact/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      impact.setImpactType(nodeData.getNodeValue().charAt(0));
                                    }
                                }
                                impact.setSite(site);
                                site.getImpacts().add(impact);
                            }
                        }
                    }

                    //Site Description-OwnerShip
                    log("Processing Site Description-OwnerShip");
                    ImporterOneSiteXML.log.info("Processing Site Description-OwnerShip");
                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/ownership");
                     // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/ownership/ownershipPart");
                         // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODESET);
                        if (result != null) {
                            NodeList nodeOwner = (NodeList) result;
                            for (int i = 1; i <= nodeOwner.getLength(); i++) {
                                SiteOwnership ownerShip = new SiteOwnership();
                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/ownership/ownershipPart[" + i + "]/ownershiptype/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                        Ownership owner = new Ownership();
                                        owner.setOwnershipType(nodeData.getNodeValue());

                                        //Site Description-OwnerShip
                                        log("         Processing OwnerShip:::" + owner.getOwnershipType());
                                        ImporterOneSiteXML.log.info("Processing OwnerShip:::" + owner.getOwnershipType());
                                        int ownerShipId = getOwnerShipId(session, nodeData.getNodeValue());
                                        if (ownerShipId != -1) {
                                            owner.setOwnershipId(ownerShipId);
                                            SiteOwnershipId id = new SiteOwnershipId(owner.getOwnershipId(), site.getSiteCode());
                                            //siteOwnerShip.setId(id);
                                            ownerShip = new SiteOwnership(id, owner, site);
                                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/ownership/ownershipPart[" + i + "]/percent/text()");
                                            // Run the query and get a nodeset
                                            result = expr.evaluate(document, XPathConstants.NODE);
                                            if (result != null) {
                                                Node nodeDataPercent = (Node) result;
                                                if (nodeData.getNodeValue() != null) {
                                                    ownerShip.setOwnershipPercent(Double.parseDouble(nodeDataPercent.getNodeValue()));
                                                }
                                            }
                                        }
                                    }
                                }
                                ownerShip.setSite(site);
                                site.getSiteOwnerships().add(ownerShip);
                            }
                        }
                    }

                    //Site Description-Documentation
                    log("Processing Site Description-Documentation");
                    ImporterOneSiteXML.log.info("Processing Site Description-Documentation");
                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/documentation");
                     // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {
                        Doc doc = new Doc();
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/documentation/description/text()");
                        // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODE);
                        if (result != null) {
                            Node nodeData = (Node) result;
                            if (nodeData.getNodeValue() != null) {
                                doc.setDocDescription(nodeData.getNodeValue());
                            }
                        }

                        //Site Description-Documentation-Links
                        log("Processing Site Description-Documentation-Links");
                        ImporterOneSiteXML.log.info("Processing Site Description-Documentation-Links");
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/documentation/links");
                        // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODESET);
                        if (result != null) {
                            NodeList nodeLinks = (NodeList) result;
                            for (int i = 1; i <= nodeLinks.getLength(); i++) {
                                DocLink link = new DocLink();
                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/documentation/links/link/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                Node nodeData = (Node) result;
                                if (nodeData != null && nodeData.getNodeValue() != null) {
                                    link.setDocLinkUrl(nodeData.getNodeValue());
                                }
                                link.setDoc(doc);
                                doc.getDocLinks().add(link);
                            }
                        }
                        site.setDoc(doc);
                    }
                }

                //Site Description-Documentation-Other Site Characteristics
                log("Processing Site Description-Documentation-Other Site Characteristics");
                ImporterOneSiteXML.log.info("Processing Site Description-Documentation-Other Site Characteristics");
                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/otherSiteCharacteristics/text()");
                 // Run the query and get a nodeset
                result = expr.evaluate(document, XPathConstants.NODE);
                if (result != null) {
                    Node nodeData = (Node) result;
                    if (nodeData.getNodeValue() != null) {
                      site.setSiteCharacteristics(nodeData.getNodeValue());
                    }
                }

                //Site Description-Documentation-Quality and Importance
                log("Processing Site Description-Documentation-Quality and Importance");
                ImporterOneSiteXML.log.info("Processing Site Description-Documentation-Quality and Importance");
                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteDescription/qualityAndImportance/text()");
                 // Run the query and get a nodeset
                result = expr.evaluate(document, XPathConstants.NODE);
                if (result != null) {
                    Node nodeData = (Node) result;
                    if (nodeData.getNodeValue() != null) {
                      site.setSiteQuality(nodeData.getNodeValue());
                    }
                }


                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection");
                 // Run the query and get a nodeset
                result = expr.evaluate(document, XPathConstants.NODE);
                if (result != null) {
                    //Site Protection-National Designations
                    log("Processing Site Protection-National Designations");
                    ImporterOneSiteXML.log.info("Processing Site Protection-National Designations");

                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/nationalDesignations");
                     // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                      if (result != null) {
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/nationalDesignations/nationalDesignation");
                         // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODESET);
                        if (result != null) {
                            NodeList nodeNDesig = (NodeList) result;
                            for (int i = 1; i <= nodeNDesig.getLength(); i++) {
                                NationalDtype nationalDType = new NationalDtype();
                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/nationalDesignations/nationalDesignation[" + i + "]/designationCode/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      nationalDType.setNationalDtypeCode(nodeData.getNodeValue());
                                    }
                                }

                                //Site Description-National Designation
                                log("         Processing National Designation:::" + nationalDType.getNationalDtypeCode());
                                ImporterOneSiteXML.log.info("Processing National Designation:::" + nationalDType.getNationalDtypeCode());
                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/nationalDesignations/nationalDesignation[" + i + "]/cover/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      nationalDType.setNationalDtypeCover(Double.parseDouble(nodeData.getNodeValue()));
                                    }
                                }
                                nationalDType.setSite(site);
                                site.getNationalDtypes().add(nationalDType);
                            }
                        }
                     }

                     //Site Protection-Relations
                     log("Processing Site Protection-Relations");
                     ImporterOneSiteXML.log.info("Processing Site Protection-Relations");
                     expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations");
                     // Run the query and get a nodeset
                     result = expr.evaluate(document, XPathConstants.NODE);
                     if (result != null) {
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/nationalRelationships");
                         // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODE);
                        if (result != null) {
                            //Site Protection-National Relations
                            log("Processing Site Protection-National Relations");
                            ImporterOneSiteXML.log.info("Processing Site Protection-National Relations");
                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/nationalRelationships/nationalRelationship");
                             // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODESET);
                            if (result != null) {
                                NodeList nodeNRel = (NodeList) result;
                                for (int i = 1; i <= nodeNRel.getLength(); i++) {
                                    SiteRelation rel = new SiteRelation();
                                    rel.setSiteRelationScope('N');

                                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/nationalRelationships/nationalRelationship[" + i + "]/designationCode/text()");
                                    // Run the query and get a nodeset
                                    result = expr.evaluate(document, XPathConstants.NODE);
                                    if (result != null) {
                                        Node nodeData = (Node) result;
                                        if (nodeData.getNodeValue() != null) {
                                          rel.setSiteRelationCode(nodeData.getNodeValue());
                                        }
                                    }

                                    //Site Description-National Relation
                                    log("         Processing National Relation:::" + rel.getSiteRelationCode());
                                    ImporterOneSiteXML.log.info("Processing National Relation:::" + rel.getSiteRelationCode());

                                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/nationalRelationships/nationalRelationship[" + i + "]/siteName/text()");
                                    // Run the query and get a nodeset
                                    result = expr.evaluate(document, XPathConstants.NODE);
                                    if (result != null) {
                                        Node nodeData = (Node) result;
                                        if (nodeData.getNodeValue() != null) {
                                          rel.setSiteRelationSitename(nodeData.getNodeValue());
                                        }
                                    }

                                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/nationalRelationships/nationalRelationship[" + i + "]/type/text()");
                                    // Run the query and get a nodeset
                                    result = expr.evaluate(document, XPathConstants.NODE);
                                    if (result != null) {
                                        Node nodeData = (Node) result;
                                        if (nodeData.getNodeValue() != null) {
                                          rel.setSiteRelationType(nodeData.getNodeValue().charAt(0));
                                        }
                                    }

                                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/nationalRelationships/nationalRelationship[" + i + "]/cover/text()");
                                    // Run the query and get a nodeset
                                    result = expr.evaluate(document, XPathConstants.NODE);
                                    if (result != null) {
                                        Node nodeData = (Node) result;
                                        if (nodeData.getNodeValue() != null) {
                                          rel.setSiteRelationCover(Double.parseDouble(nodeData.getNodeValue()));
                                        }
                                    }
                                    rel.setSite(site);
                                    site.getSiteRelations().add(rel);
                                }
                            }
                        }


                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/internationalRelationships");
                         // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODE);
                        if (result != null) {
                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/internationalRelationships/internationalRelationship");
                             // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODESET);
                            if (result != null) {
                                //Site Protection-International Relations
                                log("Processing Site Protection-International Relations");
                                ImporterOneSiteXML.log.info("Processing Site Protection-International Relations");
                                NodeList nodeNRel = (NodeList) result;
                                for (int i = 1; i <= nodeNRel.getLength(); i++) {
                                    SiteRelation rel = new SiteRelation();
                                    rel.setSiteRelationScope('I');

                                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/internationalRelationships/internationalRelationship[" + i + "]/convention/text()");
                                    // Run the query and get a nodeset
                                    result = expr.evaluate(document, XPathConstants.NODE);
                                    if (result != null) {
                                        Node nodeData = (Node) result;
                                        if (nodeData.getNodeValue() != null) {
                                          rel.setSiteRelationConvention(nodeData.getNodeValue());
                                        }
                                    }

                                    //Site Description-International Relation
                                    log("         Processing International Relation:::" + rel.getSiteRelationConvention());
                                    ImporterOneSiteXML.log.info("Processing International Relation:::" + rel.getSiteRelationConvention());

                                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/internationalRelationships/internationalRelationship[" + i + "]/siteName/text()");
                                    // Run the query and get a nodeset
                                    result = expr.evaluate(document, XPathConstants.NODE);
                                    if (result != null) {
                                        Node nodeData = (Node) result;
                                        if (nodeData.getNodeValue() != null) {
                                          rel.setSiteRelationSitename(nodeData.getNodeValue());
                                        }
                                    }

                                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/internationalRelationships/internationalRelationship[" + i + "]/type/text()");
                                    // Run the query and get a nodeset
                                    result = expr.evaluate(document, XPathConstants.NODE);
                                    if (result != null) {
                                        Node nodeData = (Node) result;
                                        if (nodeData.getNodeValue() != null) {
                                          rel.setSiteRelationType(nodeData.getNodeValue().charAt(0));
                                        }
                                    }

                                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/internationalRelationships/internationalRelationship[" + i + "]/cover/text()");
                                    // Run the query and get a nodeset
                                    result = expr.evaluate(document, XPathConstants.NODE);
                                    if (result != null) {
                                        Node nodeData = (Node) result;
                                        if (nodeData.getNodeValue() != null) {
                                          rel.setSiteRelationCover(Double.parseDouble(nodeData.getNodeValue()));
                                        }
                                    }
                                    rel.setSite(site);
                                    site.getSiteRelations().add(rel);
                                }
                            }

                            //Site Protection-Additional Designation
                            log("Processing Site Protection-Additional Designation");
                            ImporterOneSiteXML.log.info("Processing Site Protection-Additional Designation");
                            expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteProtection/relations/siteDesignationAdditional/text()");
                             // Run the query and get a nodeset
                            result = expr.evaluate(document, XPathConstants.NODE);
                            if (result != null) {
                                Node nodeData = (Node) result;
                                if (nodeData.getNodeValue() != null) {
                                  site.setSiteDesignation(nodeData.getNodeValue());
                                }
                            }
                        }
                    }
                }


                //Site Management
                log("Processing Site Management");
                ImporterOneSiteXML.log.info("Processing Site Management");
                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement");
                // Run the query and get a nodeset
                result = expr.evaluate(document, XPathConstants.NODE);
                if (result != null) {
                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies");
                     // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    Mgmt mgmt = new Mgmt();
                    saveAndReloadSession(session, mgmt);
                    if (result != null) {
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody");
                         // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODESET);
                        if (result != null) {
                            NodeList nodeMgmtBody = (NodeList) result;
                            for (int i = 1; i <= nodeMgmtBody.getLength(); i++) {
                                MgmtBody mgmtBody = new MgmtBody();

                                //Site Management Body
                                log("         Processing Site Management Body");
                                ImporterOneSiteXML.log.info("Processing Site Management Body");
                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/organisation/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyOrg(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/address/adminUnit/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyAdminUnit(nodeData.getNodeValue());
                                    }
                                }


                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/address/locatorDesignator/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyLocatorDesignator(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/address/locatorName/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyLocatorName(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/address/addressArea/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyAddressArea(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/address/postName/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyPostName(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/address/postCode/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyPostCode(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/address/thoroughfare/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyThroughFare(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/addressUnstructured/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyAddress(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementBodies/managementBody[" + i + "]/email/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtBody.setMgmtBodyEmail(nodeData.getNodeValue());
                                    }
                                }

                                mgmtBody.setMgmt(mgmt);
                                saveAndReloadSession(session, mgmtBody);
                                mgmt.getMgmtBodies().add(mgmtBody);
                            }
                        }

                    }
                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementPlans");
                     // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {
                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementPlans/planExistType/text()");
                        // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODE);
                        if (result != null) {
                            Node nodeData = (Node) result;
                            if (nodeData.getNodeValue() != null) {
                                mgmt.setMgmtStatus(nodeData.getNodeValue().charAt(0));
                            } else {
                                mgmt.setMgmtStatus('N');
                            }
                        } else {
                            mgmt.setMgmtStatus('N');
                        }

                        expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementPlans/managementPlan");
                         // Run the query and get a nodeset
                        result = expr.evaluate(document, XPathConstants.NODESET);
                        if (result != null) {
                            NodeList nodeMgmtBody = (NodeList) result;
                            for (int i = 1; i <= nodeMgmtBody.getLength(); i++) {
                                MgmtPlan mgmtPlan = new MgmtPlan();
                                //Site Management Plan
                                log("         Processing Site Management Plan");
                                ImporterOneSiteXML.log.info("Processing Site Management Body");
                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementPlans/managementPlan[" + i + "]/name/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtPlan.setMgmtPlanName(nodeData.getNodeValue());
                                    }
                                }

                                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/managementPlans/managementPlan[" + i + "]/url/text()");
                                // Run the query and get a nodeset
                                result = expr.evaluate(document, XPathConstants.NODE);
                                if (result != null) {
                                    Node nodeData = (Node) result;
                                    if (nodeData.getNodeValue() != null) {
                                      mgmtPlan.setMgmtPlanUrl(nodeData.getNodeValue());
                                    }
                                }
                                mgmtPlan.setMgmt(mgmt);
                                saveAndReloadSession(session, mgmtPlan);
                                mgmt.getMgmtPlans().add(mgmtPlan);
                            }
                        }
                    }

                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/siteManagement/conservationMeasures/text()");
                    // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {
                        Node nodeData = (Node) result;
                        if (nodeData.getNodeValue() != null) {
                          mgmt.setMgmtConservMeasures(nodeData.getNodeValue());
                        }
                    }
                    saveAndReloadSession(session, mgmt);
                    site.setMgmt(mgmt);
                }

                //Map
                log("Processing Map");
                ImporterOneSiteXML.log.info("Processing Map");
                expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/map");
                     // Run the query and get a nodeset
                result = expr.evaluate(document, XPathConstants.NODE);
                if (result != null) {
                    Map map = new Map();
                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/map/InspireID/text()");
                    // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {
                        Node nodeData = (Node) result;
                        if (nodeData.getNodeValue() != null) {
                          map.setMapInspire(nodeData.getNodeValue());
                        }
                    }

                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/map/pdfProvided/text()");
                    // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {
                        Node nodeData = (Node) result;
                        if (nodeData.getNodeValue() != null) {
                            if (("true").equals(nodeData.getNodeValue())) {
                                map.setMapId(0);
                            } else {
                                map.setMapId(1);
                            }
                        }
                    }

                    expr = xpath.compile("//sdf[siteIdentification/siteCode='" + this.siteCode + "']/map/mapReference/text()");
                    // Run the query and get a nodeset
                    result = expr.evaluate(document, XPathConstants.NODE);
                    if (result != null) {
                        Node nodeData = (Node) result;
                        if (nodeData.getNodeValue() != null) {
                          map.setMapReference(nodeData.getNodeValue());
                        }
                    }
                    saveAndReloadSession(session, map);
                    site.setMap(map);
                }
            }

            Calendar cal = Calendar.getInstance();
            site.setSiteDateCreation(cal.getTime());
            saveAndReloadSession(session, site);
            importOK = true;
        } catch (Exception e) {
            importOK = false;
            //e.printStackTrace();
            ImporterOneSiteXML.log.info("Impor process has failed, the error message :" + e.getMessage());
        }
        return importOK;

    }


}

