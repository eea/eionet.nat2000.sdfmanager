/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pojos.Biogeo;
import pojos.Doc;
import pojos.Habitat;
import pojos.HabitatClass;
import pojos.Impact;
import pojos.Mgmt;
import pojos.MgmtBody;
import pojos.NationalDtype;
import pojos.OtherSpecies;
import pojos.RefBirds;
import pojos.RefImpacts;
import pojos.RefNuts;
import pojos.RefNutsEmerald;
import pojos.RefSpecies;
import pojos.Region;
import pojos.Resp;
import pojos.Site;
import pojos.SiteBiogeo;
import pojos.SiteBiogeoId;
import pojos.SiteRelation;
import pojos.Species;
import sdf_manager.importers.ImporterTools;
import sdf_manager.util.ImporterUtils;
import sdf_manager.util.SDF_MysqlDatabase;
import sdf_manager.util.SDF_Util;


/**
 *
 * @author charbda
 */
public class ImporterSiteMDB extends AbstractImporter implements Importer {

     private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImporterSiteMDB.class .getName());

     private String table_file = "config" + System.getProperty("file.separator") + "table_names.xml";

     private String field_file = "config" + System.getProperty("file.separator") + "field_maps.xml";
     private String table_element = "table";
     private String field_element = "field";
     private HashMap<String, String> tables;
     private HashMap<String, String> fields;
     private String[] tableKeys = {"name", "used_name"};
     private String[] fieldKeys = {"reference", "oldname"};
     private String encoding;
     private HashMap speciesByCode = new HashMap();
     private HashMap speciesByName = new HashMap();
     private FileWriter outFile;
     private PrintWriter out;
     private String importDate;
     private ArrayList sitesDB = new ArrayList();
     private HashMap nutsKO = new HashMap();
     private String accessVersion;
     private String siteCode;

     /**
      *
      * @param logger
      * @param encoding
      * @param logFile
      * @param accessVersion
      * @param siteCode
      */
     public ImporterSiteMDB(Logger logger, String encoding, String logFile, String accessVersion, String siteCode) {
         super(logger, logFile);
         //this.logger = logger;
         this.encoding = encoding;
         this.accessVersion = accessVersion;
         this.siteCode = siteCode;
         //this.initLogFile(logFile);
         this.init();
     }

     /**
      *
      */
     void init() {
         this.tables = new HashMap();
         this.fields = new HashMap();
         this.parse(this.table_file, this.tables, this.table_element, this.tableKeys);
         this.parse(this.field_file, this.fields, this.field_element, this.fieldKeys);
     }

     /**
      *
      * @return
      */
     private String getImportDate() {
         Calendar cal = Calendar.getInstance();
         SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
         return sdf.format(cal.getTime());
     }


     /**
      *
      * @param fileName
      * @return
      */
      @Override
    public boolean processDatabase(String fileName) {

        Connection conn = null;
        boolean saveOK = false;
        String msgValidError = "";
        Session session = null;
        try {

            conn = getConnection(fileName);
            //TODO study if jacksess helps to solve unicode issues?
            //Database db = Database.create(new File(fileName));

            if (conn != null) {

                if (!checkTables(conn)) {
                    log("Failed to find all tables in the database. Please check the database schema and configuration files.", true);
                    ImporterSiteMDB.log.error("Failed to find all tables in the database. Please check the database schema and configuration files");
                    msgValidError = "Failed to find all tables in the database. Please check the database schema and configuration files";
                    saveOK = false;
                    return false;

                }
                if (!validateSite(conn, this.siteCode)) {
                    log("The site code: " + this.siteCode + " is not in Data Base.", true);
                    ImporterSiteMDB.log.error("The site code: " + this.siteCode + " is not in Data Base.");
                    msgValidError = "The site code: " + this.siteCode + " is not in Data Base.";
                    saveOK = false;
                    return false;
                }

                this.importDate = getImportDate();

                ImporterSiteMDB.log.info("Validation has finished");
                log("Validation has finished.", true);

                session = HibernateUtil.getSessionFactory().openSession();
                boolean siteInDB = validateSites(conn, session);

                if (!siteInDB) {
                   ImporterSiteMDB.log.info("Import process is starting");
                    log("Import process is starting.", true);

                    saveOK =  processSites(conn, session);

                    if (this.nutsKO != null && !(this.nutsKO.isEmpty())) {
                        saveOK = false;
                        ImporterSiteMDB.log.error("Error in validation:. Error Message: The code of some regions are wrong. Please check the log file for details");
                        log("Error in validation.", true);
                        msgValidError = "The code of some regions are wrong. Please check the log file for details";

                        File fileLog = SDF_Util.copyToLogImportFile(this.nutsKO, "OldDB");
                        if (fileLog != null) {
                            Desktop desktop = null;
                            if (Desktop.isDesktopSupported()) {
                                desktop = Desktop.getDesktop();
                                Desktop.getDesktop().open(fileLog);
                            }

                        }
                    }
                } else {
                    saveOK = false;
                    ImporterSiteMDB.log.error("Error in validation:. Error Message: Some sites are already stored in Data Base. Please check the log file for details");
                    log("Error in validation.", true);
                    msgValidError = "Some sites are already stored in Data Base. Please check the log file for details";

                    File fileLog = SDF_Util.copyToLogErrorSite(this.sitesDB, "OldDB");
                    if (fileLog != null) {
                        Desktop desktop = null;
                        if (Desktop.isDesktopSupported()) {
                            desktop = Desktop.getDesktop();
                            Desktop.getDesktop().open(fileLog);
                        }

                    }
                }
            } else {
                saveOK = false;
                msgValidError = "A DB error occurs. Please check the SDF_log file for more details";
            }
        } catch (Exception e) {
            ImporterSiteMDB.log.error("ERROR in processDatabase::" + e.getMessage());
            saveOK = false;
        } finally {
            session.clear();
            session.close();
            SDF_MysqlDatabase.closeQuietly(conn);

            if (saveOK) {
                JOptionPane.showMessageDialog(new JFrame(), "Import Processing has finished succesfully.", "Dialog",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(new JFrame(), "There are some errors in import process.\n" + msgValidError,
                        "Dialog", JOptionPane.INFORMATION_MESSAGE);
            }
            closeLogFile();

        }
     return saveOK;
     }
     /**
      *
      * @param fileName
      * @return
      * @throws ClassNotFoundException
      * @throws SQLException
      */
    private Connection getConnection(String fileName) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        try {

            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            conn = DriverManager.getConnection("jdbc:ucanaccess://" + fileName);

            return conn;
        } catch (ClassNotFoundException e) {
            ImporterSiteMDB.log.error("Error conecting to MS Access DB. Error Message:::" + e.getMessage());
            return null;
        } catch (SQLException e) {
            ImporterSiteMDB.log.error("Error conecting to MS Access DB. Error Message:::" + e.getMessage());
            return null;

            // } catch (IllA)
        } catch (Exception e) {
            SDF_MysqlDatabase.closeQuietly(conn);
            ImporterSiteMDB.log.error("Error conecting to MS Access DB. Error Message:::" + e.getMessage());
            return null;
        }
    }

     /**
      *
      * @param conn
      * @return
      */
     boolean checkTables(Connection conn) {
         try {
            DatabaseMetaData dbm = conn.getMetaData();

            Iterator itr = this.tables.keySet().iterator();
             while (itr.hasNext()) {

                String tmpStr = this.tables.get(itr.next());
                ResultSet rs = dbm.getTables(null, null, tmpStr , null);
                if (!rs.next()) {
                   ImporterSiteMDB.log.error("Could not find table: " + tmpStr);
                    log("Could not find table: " + tmpStr, true);
                    return false;
                }
            }
         } catch (Exception e) {
            ImporterSiteMDB.log.error("Failed processing tables: " + e.getMessage());
             log("Failed processing tables.");
             return false;
         }
         return true;
     }


     /**
      *
      */
     void freeSpecies() {
         this.speciesByCode.clear();
         this.speciesByName.clear();
     }


     /**
      *
      * @param conn
      */
     void loadSpecies(Connection conn, Session session) {
        String hql = "from RefSpecies";
        try {
            Query q = session.createQuery(hql);
            Iterator itr = q.iterate();
            while (itr.hasNext()) {
                RefSpecies rs = (RefSpecies) itr.next();
                String code = rs.getRefSpeciesCode();
                String name = rs.getRefSpeciesName();
                Character group = rs.getRefSpeciesGroup();
                if (code != null) {
                    this.speciesByCode.put(code, new Object[]{name, group});
                }
                if (name != null) {
                    this.speciesByName.put(name, new Object[]{code, group});
                }
            }
            hql = "select refBirds from RefBirds as refBirds";

            q = session.createQuery(hql);
            itr = q.iterate();
            while (itr.hasNext()) {
                RefBirds rs = (RefBirds) itr.next();
                String code = rs.getRefBirdsCode();
                String name = rs.getRefBirdsName();
                Character group = 'B';
                if (code != null) {
                    this.speciesByCode.put(code, new Object[]{name, group});
                }
                if (name != null) {
                    this.speciesByName.put(name, new Object[]{code, group});
                }
            }
        } catch (Exception e) {
           ImporterSiteMDB.log.error("Failed loading species: " + e.getMessage());
        }
     }

     /**
      *
      * @param conn
      */
     boolean processSites(Connection conn, Session session) {
        boolean processOK = false;
        try {
            loadSpecies(conn, session);

            Site site = new Site();
            try {
                   Transaction tx = session.beginTransaction();
                   log("processing: " + this.siteCode, true);
                   site.setSiteCode(this.siteCode);
                   processBiotop(conn, session, site);
                   processSpecies(conn, session, site);
                   processHabitats(conn, session, site);
                   processHabitatClasses(conn, session, site);
                   processRegions(conn, session, site);
                   processRelations(conn, session, site);
                   processDTypes(conn, session, site);
                   processImpacts(conn, session, site);
                   tx.commit();
                   processOK = true;

            } catch (Exception e) {
                processOK = false;
               ImporterSiteMDB.log.error("Failed processing site: " + this.siteCode + " .The error: " + e.getMessage());
                log("failed processing site: " + this.siteCode, true);
            }

           ImporterSiteMDB.log.info("Finishing import process.Closing connection to Data Base");
           log("Finishing import process.Closing connection to Data Base");
        } catch (Exception e) {
           ImporterSiteMDB.log.error("The error: " + e.getMessage());
           processOK = false;
           return false;
        } finally {
            session.clear();
        }
        return processOK;
     }


     /**
      *
      * @param rs
      * @param fieldName
      * @return
      */
     Double getDouble(ResultSet rs, String fieldName) {
         try {
             return rs.getDouble(fieldName);
         } catch (Exception e) {
             ImporterSiteMDB.log.error("Failed extracting field: " + fieldName + ". Error:::" + e.getMessage());
             log("Failed extracting field: " + fieldName, true);
             return null;
         }
     }
     /**
      *
      * @param rs
      * @param fieldName
      * @return
      */
     String getString(ResultSet rs, String fieldName) {
         try {

             String value = rs.getString(fieldName);
             return ImporterUtils.getString(value, this.encoding);

/*             if (!("UTF-8").equals(this.encoding)) {

                byte[] result = rs.getBytes(fieldName);
                 if (result != null && result.length == 0) {
                     return null;
                 } //don't enter empty string in the database
                 else {
                     if (result != null) {
                         Charset charset = Charset.forName(this.encoding);
                         CharsetDecoder decoder = charset.newDecoder();
                         decoder.onMalformedInput(CodingErrorAction.REPLACE);
                         decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
                         CharBuffer cbuf = decoder.decode(ByteBuffer.wrap(result));
                         return cbuf.toString().trim();
                     } else {
                         return null;
                     }
                 }
             } else {
                 return rs.getString(fieldName);
             }
*/
         } catch (Exception e) {
             log("Failed extracting field: " + fieldName + ". The field could have an erroneous name. Please verify.", false);
            ImporterSiteMDB.log.error("Failed extracting field: "
                    + fieldName + ".The field could have an erroneous name.Error:::" + e.getMessage());
             return null;
         }
     }
     /**
      *
      * @param rs
      * @param fieldName
      * @return
      */
     Character getChar(ResultSet rs, String fieldName) {
         String tmp = getString(rs, fieldName);
         if (tmp != null && !tmp.equals("")) {
             return tmp.charAt(0);
         } else {

             return null;
         }

     }
     /**
      *
      * @param rs
      * @param fieldName
      * @return
      */
      Boolean getBoolean(ResultSet rs, String fieldName) {
         try {
             Boolean bol = rs.getBoolean(fieldName);
             return bol;
         } catch (Exception e) {
             ImporterSiteMDB.log.error("Failed extracting field: " + fieldName + ".The field could have an erroneous name.Error:::" + e.getMessage());
             return Boolean.valueOf(false);
         }
     }
     /**
      *
      * @param c
      * @return
      */
     Character getType(Character c) {
         if (c == null) {
             return null;
         }
         String strC = c.toString().toUpperCase();
         //c = Character.toUpperCase(c);
         if (strC.equals("C")) {
             return 'C';
         } else if (strC.equals("A") || strC.equals("D") || strC.equals("F") || strC.equals("H") || strC.equals("J")) {
         //else if (c.equals('A') || c.equals('D') || c.equals('F') || c.equals('H') || c.equals('J')) {

            return 'A';
         } else if (strC.equals("B") || strC.equals("E") || strC.equals("G") || strC.equals("I") || strC.equals("K")) {
         //else if (c.equals('B') || c.equals('E') || c.equals('G') || c.equals('I') || c.equals('K')) {
            return 'B';
         } else {
             return Character.toUpperCase(c);
         }
     }


     /***
      *
      * @param conn
      * @param session
      * @return
      */
     boolean validateSites(Connection conn, Session session) {
        String hql = " from Site where siteCode='" + this.siteCode + "'";
        try {
            Query q = session.createQuery(hql);
            Iterator itr = q.iterate();
            if (itr.hasNext()) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            ImporterSiteMDB.log.error("Error:::" + e.getMessage());
            return false;
        }

     }

     /**
      *
      * @param conn
      * @param sitecode
      * @return
      */
     private boolean validateSite(Connection conn, String sitecode) {

        try {
             String sql = "select * from " + this.tables.get("biotop") + " where sitecode ='" + sitecode + "'";
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);

             if (rs.next()) {
                return true;
             } else {
                return false;
             }

        } catch (Exception e) {
            //e.printStackTrace();
            ImporterSiteMDB.log.error("Error:::" + e.getMessage());
            return false;
        }

     }



     /**
      *
      * @param conn
      * @param session
      * @param site
      */
     void processBiotop(Connection conn, Session session, Site site) {
         try {
             String sql = "select * from " + this.tables.get("biotop") + " where sitecode ='" + site.getSiteCode() + "'";
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);

             String tmpStr;
             Double tmpDouble;
             Date tmpDate;
             Character tmpChar;
             while (rs.next()) {
                 tmpStr = getString(rs, this.fields.get("sitename"));
                 if (tmpStr != null) {
                     site.setSiteName(tmpStr);
                 }
                 log("Processing Site Type");
                 ImporterSiteMDB.log.info("Processing Site Type");
                 tmpChar = getChar(rs, this.fields.get("sitetype"));
                 if (tmpChar != null) {
                     site.setSiteType(getType(tmpChar));
                 }

                 log("Processing Compilation Date");
                 ImporterSiteMDB.log.info("Processing Compilation Date");
                 tmpDate = ImporterTools.parseMdbDate(getString(rs, this.fields.get("compilation_date")), site.getSiteCode(), "compilation_date");
                 if (tmpDate != null) {
                     site.setSiteCompDate(tmpDate);
                 }

                 log("Processing Update Date");
                 ImporterSiteMDB.log.info("Processing Update Date");
                 tmpDate = ImporterTools.parseMdbDate(getString(rs, this.fields.get("update_date")), site.getSiteCode(), "update_date");
                 if (tmpDate != null) {
                     site.setSiteUpdateDate(tmpDate);
                 }

                 log("Processing SCI Proposal Date");
                 ImporterSiteMDB.log.info("Processing SCI Proposal Date");
                 tmpDate = ImporterTools.parseMdbDate(getString(rs, this.fields.get("sci_prop_date")), site.getSiteCode(), "sci_prop_date");
                 if (tmpDate != null) {
                     if (!SDF_ManagerApp.isEmeraldMode()) {
                         site.setSiteSciPropDate(tmpDate);
                     } else {
                         site.setSiteProposedAsciDate(tmpDate);
                     }
                 }

                 log("Processing SCI Confirmed Date");
                 ImporterSiteMDB.log.info("Processing SCI Confirmed Date");
                 tmpDate = ImporterTools.parseMdbDate(getString(rs, this.fields.get("sci_conf_date")), site.getSiteCode(), "sci_conf_date");
                 if (tmpDate != null) {
                     if (!SDF_ManagerApp.isEmeraldMode()) {
                         site.setSiteSciConfDate(tmpDate);
                     } else {
                         site.setSiteConfirmedAsciDate(tmpDate);
                     }
                 }

                 if (!SDF_ManagerApp.isEmeraldMode()) {
                     log("Processing SPA Classified Date");
                     ImporterSiteMDB.log.info("Processing SPA Classified Date");
                     tmpDate = ImporterTools.parseMdbDate(getString(rs, this.fields.get("spa_date")), site.getSiteCode(), "spa_date");
                     if (tmpDate != null) {
                         site.setSiteSpaDate(tmpDate);
                     }

                     log("Processing SAC Date");
                     ImporterSiteMDB.log.info("Processing SAC Date");
                     tmpDate = ImporterTools.parseMdbDate(getString(rs, this.fields.get("sac_date")), site.getSiteCode(), "sac_date");
                     if (tmpDate != null) {
                         site.setSiteSacDate(tmpDate);
                     }
                 }

                 log("Processing Respondent");
                 ImporterSiteMDB.log.info("Processing Respondent");
                 tmpStr = getString(rs, this.fields.get("respondent"));
                 if (tmpStr != null) {

                    Resp resp = new Resp();
                    resp.setRespAddress(tmpStr);
                    resp.getSites().add(site);
                    session.save(resp);
                    site.setResp(resp);
                 }

                 log("Processing Site Location-Area");
                 ImporterSiteMDB.log.info("Processing Site Location-Area");

                 if (SDF_ManagerApp.isEmeraldMode()) {
                     tmpStr = rs.getString(this.fields.get("area"));
                     tmpDouble = ImporterUtils.fixAndGetDouble(tmpStr);

                 } else {
                     tmpDouble = getDouble(rs, this.fields.get("area"));
                 }
                 if (tmpDouble != null) {
                     site.setSiteArea(tmpDouble);
                 }


                 //marine area not existing in old ver of MSACCESS
//                 log("Processing Site Location-Marine Area");
//                 ImporterSiteMDB.log.info("Processing Site Location-Marine Area");
//                 tmpDouble = getMarineArea(conn, site.getSiteCode());
//                 if (tmpDouble != null) {
//                     site.setSiteMarineArea(tmpDouble);
//                 }

                 log("Processing Site Location-Length");
                 ImporterSiteMDB.log.info("Processing Site Location-Length");
                 if (SDF_ManagerApp.isEmeraldMode()) {
                     tmpStr = rs.getString(this.fields.get("site_length"));
                     tmpDouble = ImporterUtils.fixAndGetDouble(tmpStr);
                 } else {
                     tmpDouble = getDouble(rs, this.fields.get("site_length"));
                 }
                 if (tmpDouble != null) {
                     site.setSiteLength(tmpDouble);
                 }

                 log("Processing Site Location-Longitude");
                 ImporterSiteMDB.log.info("Processing Site Location-Longitude");
                 String sign = getString(rs, this.fields.get("lon_ew"));
                 Double deg = getDouble(rs, this.fields.get("lon_deg"));
                 Double min = getDouble(rs, this.fields.get("lon_min"));
                 Double sec = getDouble(rs, this.fields.get("lon_sec"));
                 Double longitude = this.convertCoordinate(1, sign, deg, min, sec);
                 if (longitude != null) {
                     site.setSiteLongitude(longitude);
                 }

                 log("Processing Site Location-Latitude");
                 ImporterSiteMDB.log.info("Processing Site Location-Latitude");
                 deg = getDouble(rs, this.fields.get("lat_deg"));
                 min = getDouble(rs, this.fields.get("lat_min"));
                 sec = getDouble(rs, this.fields.get("lat_sec"));
                 Double latitude = this.convertCoordinate(2, sign, deg, min, sec);
                 if (latitude != null) {
                     site.setSiteLatitude(latitude);
                 }

                 log("Processing Designation");
                 ImporterSiteMDB.log.info("Processing Site Location-Designation");
                 tmpStr = getString(rs, this.fields.get("designation"));
                 if (tmpStr != null) {
                     site.setSiteDesignation(tmpStr);
                 }

                 log("Processing Quality");
                 ImporterSiteMDB.log.info("Processing Site Location-Quality");
                 tmpStr = tmpStr = getString(rs, this.fields.get("quality"));
                 if (tmpStr != null) {
                     site.setSiteQuality(tmpStr);
                 }

                 log("Processing Other Site Characteristics");
                 ImporterSiteMDB.log.info("Processing Site Location-Other Site Characteristics");
                 tmpStr = getString(rs, this.fields.get("characteristics"));
                 if (tmpStr != null) {
                     site.setSiteCharacteristics(tmpStr);
                 }

                 log("Processing Documentation");
                 ImporterSiteMDB.log.info("Processing Site Location-Documentation");
                 tmpStr = getString(rs, this.fields.get("documentation"));
                 if (tmpStr != null) {
                    Doc doc = new Doc();
                    doc.setDocDescription(tmpStr);
                    doc.getSites().add(site);
                    session.save(doc);
                    site.setDoc(doc);
                 }

                 log("Processing Site Management");
                 ImporterSiteMDB.log.info("Processing Site Management");
                 tmpStr = getString(rs, this.fields.get("mgmt_plan"));
                 String tmpStrMgmtBody = getString(rs, this.fields.get("mgmt_body"));
                 if (tmpStr != null) {
                    Mgmt mgmt = new Mgmt();
                    mgmt.setMgmtConservMeasures(tmpStr);
                    mgmt.setMgmtStatus('Y');
                    session.save(mgmt);
                    /*MgmtPlan mgmtPlan = new MgmtPlan();
                    mgmtPlan.setMgmtPlanName(tmpStr);
                    mgmtPlan.setMgmt(mgmt);
                    mgmt.getMgmtPlans().add(mgmtPlan);
                    session.save(mgmtPlan);*/
                    if (tmpStrMgmtBody != null) {

                        MgmtBody mgmtBody = new MgmtBody();
                        mgmtBody.setMgmtBodyOrg(tmpStrMgmtBody);
                        mgmtBody.setMgmt(mgmt);
                        mgmt.getMgmtBodies().add(mgmtBody);
                        session.save(mgmtBody);
                    }

                    mgmt.getSites().add(site);
                    session.save(mgmt);
                    site.setMgmt(mgmt);

                 } else {
                     Mgmt mgmt = new Mgmt();
                     mgmt.setMgmtStatus('N');
                     session.save(mgmt);
                     if (tmpStrMgmtBody != null) {

                        MgmtBody mgmtBody = new MgmtBody();
                        mgmtBody.setMgmtBodyOrg(tmpStrMgmtBody);
                        mgmtBody.setMgmt(mgmt);
                        mgmt.getMgmtBodies().add(mgmtBody);
                        session.save(mgmtBody);

                    }
                    mgmt.getSites().add(site);
                    session.save(mgmt);
                    site.setMgmt(mgmt);
                 }

                log("Processing Bioregions");
                ImporterSiteMDB.log.info("Processing Bioregions");

                ArrayList regions = new ArrayList();
                regions.add("ALPINE");
                regions.add("ATLANTIC");
                regions.add("BOREAL");
                regions.add("CONTINENT");
                regions.add("MACARONES");
                regions.add("MEDITERR");
                regions.add("PANNONIC");
                regions.add("STEPPIC");
                regions.add("ANATOL");
                regions.add("ARCTIC");
                regions.add("PONTIC");
                regions.add("BLACKSEA");

                for (int i = 0; i < regions.size(); i++) {
                    Boolean region = getBoolean(rs, (String) regions.get(i));
                    region = region == null ? false : region;
                    if (region) {
                        String bioRegCode = translateBioRegions((String) regions.get(i));
                        int biogeoId = getBioRegionId(session, bioRegCode);
                        Biogeo biogeo = (Biogeo) session.load(Biogeo.class, biogeoId);
                        SiteBiogeoId id = new SiteBiogeoId(site.getSiteCode(), biogeo.getBiogeoId());
                        SiteBiogeo siteBiogeo = new SiteBiogeo(id, biogeo, site);
                        site.getSiteBiogeos().add(siteBiogeo);
                    }
             }
             Calendar cal = Calendar.getInstance();
             site.setSiteDateCreation(cal.getTime());
             session.save(site);
           }
           stmt.close();
         } catch (Exception e) {
             ImporterSiteMDB.log.error("Error:::" + e.getMessage());
         }

     }

     /**
      *
      * @param oldBioRegName
      * @return
      */
     private String translateBioRegions(String oldBioRegName) {
         String newBioRegCode = "";
         if (oldBioRegName.equals("ALPINE")) {
             newBioRegCode = "alpine";
         } else if (oldBioRegName.equals("ATLANTIC")) {
             newBioRegCode = "atlantic";
         } else if (oldBioRegName.equals("BOREAL")) {
             newBioRegCode = "boreal";
         } else if (oldBioRegName.equals("CONTINENT")) {
             newBioRegCode = "continental";
         } else if (oldBioRegName.equals("MACARONES")) {
             newBioRegCode = "macaronesian";
         } else if (oldBioRegName.equals("MEDITERR")) {
             newBioRegCode = "mediterranean";
         } else if (oldBioRegName.equals("PANNONIC")) {
             newBioRegCode = "pannonian";
         } else if (oldBioRegName.equals("STEPPIC")) {
             newBioRegCode = "steppic";
         } else if (oldBioRegName.equals("ANATOL")) {
             newBioRegCode = "anatolian";
         } else if (oldBioRegName.equals("ARCTIC")) {
             newBioRegCode = "arctic";
         } else if (oldBioRegName.equals("PONTIC")) {
              newBioRegCode = "blacksea";
         } else if (oldBioRegName.equals("BLACKSEA")) {
              newBioRegCode = "blacksea";
         } else {

         }


         return newBioRegCode;
     }

    /**
     *
     * @param session
     * @param bioRegCode
     * @return
     */
    private int getBioRegionId(Session session, String bioRegCode) {
        Integer b = 0;
        try {
            String hql = "select biogeoId from Biogeo biogeo where biogeo.biogeoCode like '" + bioRegCode + "'";
            Query q = session.createQuery(hql);
            b = (Integer) q.uniqueResult();

        } catch (Exception e) {
            ImporterSiteMDB.log.error("An error has occurred, searching the bioregion Id. Error Message:::" + e.getMessage());
        }
        return b.intValue();
    }

     /**
      *
      * @param conn
      * @param siteCode
      * @return
      * @throws SQLException
      */
     private Double getMarineArea(Connection conn, String siteCode) throws SQLException {
         Double marineArea = null;
         try {
             String sql = "select MarineArea from Areas where SiteCode='" + siteCode + "'";

             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);

             while (rs.next()) {
                marineArea = rs.getDouble("MarineArea");
             }

         } catch (SQLException e) {
            ImporterSiteMDB.log.error("Areas table doesn't exist in this data base.Error:::" + e.getMessage());
         } catch (Exception e) {
            ImporterSiteMDB.log.error("Error:::" + e.getMessage());
         }
         return marineArea;
     }
     /**
      *
      * @param table
      * @param code
      * @param name
      * @return
      */
    Character getGroup(String table, String code, String name) {
        Character c = null;
        if (table.equals(this.tables.get("bird"))) {
            c = 'B';
        }
        if (table.equals(this.tables.get("fishes"))) {
            c = 'F';
        }
        if (table.equals(this.tables.get("invert"))) {
            c = 'I';
        }
        if (table.equals(this.tables.get("mammal"))) {
            c = 'M';
        }
        if (table.equals(this.tables.get("plant"))) {
            c = 'P';
        } else {
            if (code != null && (!code.equals(""))) {
                Object[] res = (Object[]) this.speciesByCode.get(code);
                if (res != null) {
                    c = new Character((Character) res[1]);
                }
            } else if (name != null && (!name.equals(""))) {
                Object[] res = (Object[]) this.speciesByName.get(name);
                if (res != null) {
                    c = new Character((Character) res[1]);
                }
            }
        }
        return c;
    }


    /**
     *
     * @param val
     * @return
     */
    private String getPopCategoryN(String val) {
        String uVal = val.toUpperCase();
        if (uVal.equals("C") || uVal.equals("R") || uVal.equals("P") || uVal.equals("V")) {
            return val.toUpperCase();
        } else {
            return null;
        }
    }


    /**
     *
     * @param val
     * @return
     */
    private String preparePopField(String val) {
        String retVal = val.replaceAll("[^a-zA-Z0-9><~=\\-]", "");
        return retVal;
    }


    /**
     *
     * @param val
     * @return
     */
    private boolean isPair(String val) {
        if (val.startsWith("p") || val.endsWith("p")) {
                return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param val
     * @return
     */
    private Object[] getMinMax(String val) {
        Object[] result = {null, null, null, null, null};
        val = val.toLowerCase();
            /* some know cases with direct translation */
        if (val.matches("[0-9]+")) {
            Integer intVal = convertToIntN(val);
            result[0] = intVal;
            result[1] = intVal;
            result[2] = "i";
            result[4] = "G";
            return result;
        }
        if (val.matches("[ip]{0,1}[0-9]+[ip]{0,1}")) {
            String unit = null;
            if (val.startsWith("i") || val.startsWith("p")) {
                unit = val.substring(0, 1);
            } else if (val.endsWith("i") || val.endsWith("p")) {
                unit = val.substring(val.length() - 1, val.length() - 0);
            }
            Integer intVal = convertToIntN(val);
            result[0] = intVal;
            result[1] = intVal;
            result[2] = unit;
            result[4] = "G";
            return result;
        }
        if (val.matches("[ip]{0,1}[crvp]")) {
            //String unit = null;
            String popType = null;
            if ((val.length() > 1) && (val.startsWith("i") || val.startsWith("p"))) {
                //unit = val.substring(0, 1);
                popType = val.substring(1, 2);
            }
            //else popType = unit = val.substring(0, 1);
            else {
                popType = val.substring(0, 1);
            }
            result[0] = null;
            result[1] = null;
            result[2] = null; //no unit after all, is useless
            result[3] = popType.toUpperCase();
            return result;
        }
        if (val.matches("[ip]{0,1}[><=~][0-9]+[ip]{0,1}")) {
            String sign = "";
            if (val.startsWith("i") || val.startsWith("p")) {
                result[2] = val.substring(0, 1);
                sign = val.substring(1, 2).toUpperCase();
            } else if (val.endsWith("i") || val.endsWith("p")) {
                result[2] = val.substring(val.length() - 1, val.length());
                sign = val.substring(0, 1).toUpperCase();
            } else {
                sign = val.substring(0, 1).toUpperCase();
            }
            Integer intVal = convertToIntN(val);
            if (sign.equals(">")) {
                result[0] = intVal;
                result[4] = "M";
            } else if (sign.equals("=")) {
                result[0] = intVal;
                result[1] = intVal;
                result[4] = "G";
            } else if (sign.equals("~")) {
                result[0] = intVal;
                result[1] = intVal;
                result[4] = "M";
            } else if (sign.equals("<")) {
                result[0] = 1;
                result[1] = intVal;
                result[4] = "M";
            }
            result[3] = null;
            return result;
        }
        if (val.matches(".*-.*")) {
            String[] tokens = val.split("-");
            Object[] tok1 = this.getMinMax(tokens[0]);
            Object[] tok2 = this.getMinMax(tokens[1]);
            if (tok1[0] != null) {
                result[0] = tok1[0];
            } else if (tok1[1] != null) {
                result[0] = tok1[1];
            } else if (tok2[0] != null) {
                result[0] = tok1[0];
            } else if (tok2[1] != null) {
                result[0] = tok1[1];
            }
            if (tok2[1] != null) {
                result[1] = tok2[1];
            } else if (tok2[0] != null) {
                result[1] = tok2[0];
            } else if (tok1[1] != null) {
                result[1] = tok1[1];
            } else if (tok1[0] != null) {
                result[1] = tok1[0];
            }
            result[2] = isPair(val) ? "p" : "i";
            result[3] = tok1[3] != null ? tok1[3] : (tok2[3] != null) ? tok2[3] : null;
            result[4] = tok1[4] != null ? tok1[4] : (tok2[4] != null) ? tok2[4] : null;
            return result;
        }
        return result;
    }

    /**
     *
     * @param species
     * @param popString
     */
    private void processPopulationSize(Species species, String popString) {
        /* set population min/max/unit/category/data quality */
        String val = preparePopField(popString);
        Object[] tokens = {null, null, null, null, null};
        tokens = getMinMax(val);
        if (tokens[0] != null) {
            species.setSpeciesSizeMin((Integer) tokens[0]);
        }
        if (tokens[1] != null) {
            species.setSpeciesSizeMax((Integer) tokens[1]);
        }
        if (tokens[2] != null) {
            species.setSpeciesUnit((String) tokens[2]);
        }
        if (tokens[3] != null) {
            species.setSpeciesCategory(((String) tokens[3]).charAt(0));
        }
        if (tokens[4] != null) {
            //species.setSpeciesDataQuality(((String) tokens[4]).charAt(0));
            species.setSpeciesDataQuality((String) tokens[4]);
        }
        log(String.format("\tExtracting population (%s) for (%s): min=%s, max=%s, unit=%s, category=%s, quality=%s",
                popString, species.getSpeciesName(), tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]), false);
    }


    /**
     *
     * @param spName
     * @param resident
     * @param breeding
     * @param wintering
     * @param staging
     * @return
     */
    private Object[] processPopulationSize(String spName, String resident, String breeding, String wintering, String staging) {
        /*return population min/max/unit/catgory/data quality */
        Object[] result = {null, null, null, null, null};
        String resident2 = preparePopField(resident);
        String breeding2 = preparePopField(breeding);
        String wintering2 = preparePopField(wintering);
        String staging2 = preparePopField(staging);
        int min = 0; int max = 0;
        String popType = null; String literal = "";
        popType = getPopCategoryN(resident2);
        if (popType == null) {
            popType = getPopCategoryN(breeding2);
        }
        if (popType == null) {
            popType = getPopCategoryN(wintering2);
        }
        if (popType == null) {
            popType = getPopCategoryN(staging2);
        }
        Object[] tokens = {null, null, null, null, null};
        if (!resident2.equals("")) {
            tokens = getMinMax(resident2);
            literal = resident;
        } else if (!breeding2.equals("")) {
            tokens = getMinMax(breeding2);
            literal = breeding;
        } else if (!wintering2.equals("")) {
            tokens = getMinMax(wintering2);
            literal = wintering;
        } else if (!staging2.equals("")) {
            tokens = getMinMax(staging2);
            literal = staging;
        }
        result[0] = tokens[0];
        result[1] = tokens[1];
        result[2] = tokens[2];
        result[3] = tokens[3];
        result[4] = tokens[4];
        log(String.format("\tExtracting population (%s) for (%s): min=%s, max=%s, unit=%s, category=%s, quality=%s",
                literal, spName, tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]), false);
        return result;
    }


    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processSpecies(Connection conn, Session session, Site site) throws SQLException {
        /****** attention: need to add species categories and perhaps check existence of codes and names *****/
        /***Add species groups**/
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String[] tables = {this.tables.get("amprep"), this.tables.get("bird"), this.tables.get("fishes"), this.tables.get("invert"),
                                this.tables.get("mammal"), this.tables.get("plant"), this.tables.get("spec")};
            log("Processing Species");
            ImporterSiteMDB.log.info("Processing Species");
            for (int i = 0; i < tables.length; i++) {
                String sql = "select * from " + tables[i] + " where sitecode ='" + site.getSiteCode() + "'";
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                String tmpStr;
                Character tmpChar;
                String spName = "";
                while (rs.next()) {
                    if (!tables[i].equals("spec")) {
                        Species species = new Species();

                        tmpStr = getString(rs, this.fields.get("species_code"));
                        if (tmpStr != null) {
                            species.setSpeciesCode(tmpStr);
                        }

                        tmpStr = getString(rs, this.fields.get("species_name"));
                        if (tmpStr != null) {
                            species.setSpeciesName(tmpStr);
                        }
                        spName = tmpStr;

                        log("      Processing Species Code:::" + species.getSpeciesCode() + ":: Species Name:::" + species.getSpeciesName());
                        ImporterSiteMDB.log.info("Processing Species Code:::" + species.getSpeciesCode() + ":: Species Name:::" + species.getSpeciesName());
                        tmpChar = getChar(rs, this.fields.get("species_population"));
                        if (tmpChar != null) {
                            species.setSpeciesPopulation(tmpChar);
                        }
                        tmpChar = getChar(rs, this.fields.get("species_conservation"));
                        if (tmpChar != null) {
                            species.setSpeciesConservation(tmpChar);
                        }
                        tmpChar = getChar(rs, this.fields.get("species_isolation"));
                        if (tmpChar != null) {
                            species.setSpeciesIsolation(tmpChar);
                        }
                        tmpChar = getChar(rs, this.fields.get("species_global"));
                        if (tmpChar != null) {
                            species.setSpeciesGlobal(tmpChar);
                        }
                        tmpChar = getGroup(tables[i], species.getSpeciesCode(), species.getSpeciesName());
                        if (tmpChar != null) {
                            species.setSpeciesGroup(tmpChar);
                        }
                        String resident, breeding, wintering, staging;
                        resident = breeding = wintering = staging = "";
                        tmpStr = getString(rs, this.fields.get("species_resident"));
                        boolean saveDefault = true; //if no other category is found, the default will be saved.
                        if (tmpStr != null) {
                            saveDefault = false;
                            resident = tmpStr;
                            species.setSpeciesType('p');
                            processPopulationSize(species, resident);
                            species.setSite(site);
                            site.getSpecieses().add(species);
                        }
                        if (!tables[i].equals("plant")) {
                            tmpStr = getString(rs, this.fields.get("species_breeding"));
                            if (tmpStr != null) {
                                saveDefault = false;
                                breeding = tmpStr;
                                Species newSpecies = new Duplicator().duplicateSpeciesNoPopulation(species);
                                newSpecies.setSpeciesType('r');
                                processPopulationSize(newSpecies, breeding);
                                newSpecies.setSite(site);
                                site.getSpecieses().add(newSpecies);
                            }
                            tmpStr = getString(rs, this.fields.get("species_wintering"));
                            if (tmpStr != null) {
                                saveDefault = false;
                                wintering = tmpStr;
                                Species newSpecies = new Duplicator().duplicateSpeciesNoPopulation(species);
                                newSpecies.setSpeciesType('w');
                                processPopulationSize(newSpecies, wintering);
                                newSpecies.setSite(site);
                                site.getSpecieses().add(newSpecies);
                            }
                            tmpStr = getString(rs, this.fields.get("species_staging"));
                            if (tmpStr != null) {
                                saveDefault = false;
                                staging = tmpStr;
                                Species newSpecies = new Duplicator().duplicateSpeciesNoPopulation(species);
                                newSpecies.setSpeciesType('c');
                                processPopulationSize(newSpecies, staging);
                                newSpecies.setSite(site);
                                site.getSpecieses().add(newSpecies);
                            }
                        }
                        if (saveDefault) {
                            species.setSite(site);
                            site.getSpecieses().add(species);
                        }
                        //session.save(species);
                    } else if (tables[i].equals(this.tables.get("spec"))) {
                        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");

                        OtherSpecies oSpecies = new OtherSpecies();
                        if (!SDF_ManagerApp.isEmeraldMode()) {
                            tmpStr = getString(rs, this.fields.get("species_code"));
                            if (tmpStr != null) {
                                oSpecies.setOtherSpeciesCode(tmpStr);
                            }
                        }
                        tmpStr = getString(rs, this.fields.get("species_name"));
                        if (tmpStr != null) {
                            System.out.println(":: NAME ==> " + tmpStr);
                            oSpecies.setOtherSpeciesName(tmpStr);
                        }

                        String strPopulationInput = getString(rs, this.fields.get("species_population"));
                        if (strPopulationInput != null) {

                            if (Pattern.matches("([C,V,R,P])", strPopulationInput) && strPopulationInput.length() == 1)
                            {
                                System.out.println(":: MATCH 0");

                                // Get Population Char
                                oSpecies.setOtherSpeciesCategory(strPopulationInput.charAt(0));
                            } else if (strPopulationInput.length() > 1) {

                                String strMinMaxPatterString = "(\\d+)-(\\d+).*";
                                String strNumberAndCharacter = "(\\d+)+\\W*+([C,V,R,P]).*";
                                String strOnlyNumber = "(\\d+).*";
                                String strMinPatternString = "(>\\d+).*";
                                String strMaxPatternString = "(<\\d+).*";


                                // Finds patterns like "20-100" and get min-max
                                if (Pattern.matches(strMinMaxPatterString, strPopulationInput)) {

                                    System.out.println(":: MATCH 1");

                                    Pattern pattern = Pattern.compile("(\\d+-\\d+)");
                                    Matcher matcher = pattern.matcher(strPopulationInput);

                                    // Check all occurance
                                    while (matcher.find()) {
                                        try {
                                            String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                            String[] arr = tmp.split("-");
                                            oSpecies.setOtherSpeciesSizeMin(new Integer(arr[0]));
                                            oSpecies.setOtherSpeciesSizeMax(new Integer(arr[1]));
                                        } catch (Exception ex) { }
                                    }

                                // Finds patterns like "100 A" and get min and max from number and type from char
                                } else if (Pattern.matches(strNumberAndCharacter, strPopulationInput)) {
                                    System.out.println(":: MATCH 2");

                                    Pattern pattern = Pattern.compile("(\\d+)");
                                    Matcher matcher = pattern.matcher(strPopulationInput);
                                    while (matcher.find()) {
                                        try {
                                            String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                            oSpecies.setOtherSpeciesSizeMin(new Integer(tmp));
                                            oSpecies.setOtherSpeciesSizeMax(new Integer(tmp));
                                        } catch (Exception ex) { }
                                    }

                                    pattern = Pattern.compile("([C,V,R,P])");
                                    matcher = pattern.matcher(strPopulationInput);
                                    while (matcher.find()) {
                                        try {
                                            String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                            oSpecies.setOtherSpeciesCategory(tmp.charAt(0));
                                        } catch (Exception ex) { }
                                    }

                                // Finds patterns like ">100" and get min
                                } else if (Pattern.matches(strMinPatternString, strPopulationInput)) {
                                    System.out.println(":: MATCH 3");

                                    Pattern pattern = Pattern.compile("(\\d+)");
                                    Matcher matcher = pattern.matcher(strPopulationInput);
                                    while (matcher.find()) {
                                        try {
                                            String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                            oSpecies.setOtherSpeciesSizeMin(new Integer(tmp));
                                        } catch (Exception ex) { }
                                    }

                                // Finds patterns like "<100" and get max
                                } else if (Pattern.matches(strMaxPatternString, strPopulationInput)) {
                                    System.out.println(":: MATCH 4");

                                    Pattern pattern = Pattern.compile("(\\d+)");
                                    Matcher matcher = pattern.matcher(strPopulationInput);
                                    while (matcher.find()) {
                                        try {
                                            String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                            oSpecies.setOtherSpeciesSizeMax(new Integer(tmp));
                                        } catch (Exception ex) { }
                                    }

                                } else if (Pattern.matches(strOnlyNumber, strPopulationInput)) {
                                    System.out.println(":: MATCH 5");
                                    Pattern pattern = Pattern.compile("(\\d+)");
                                    Matcher matcher = pattern.matcher(strPopulationInput);
                                    while (matcher.find()) {
                                        try {
                                            String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                            oSpecies.setOtherSpeciesSizeMin(new Integer(tmp));
                                            oSpecies.setOtherSpeciesSizeMax(new Integer(tmp));
                                        } catch (Exception ex) { }
                                    }
                                }

                            }

                        }

                        log("      Processing Other Species Code:::" + oSpecies.getOtherSpeciesCode() + ":: Other Species Name:::" + oSpecies.getOtherSpeciesName());
                        ImporterSiteMDB.log.info("Processing Other Species Code:::" + oSpecies.getOtherSpeciesCode() + ":: Other Species Name:::" + oSpecies.getOtherSpeciesName());

                        tmpStr = getString(rs, this.fields.get("species_motivation"));
                        if (tmpStr != null) {
                            /* value is either A, B, C or D*/
                            tmpStr = tmpStr.toUpperCase();
                            oSpecies.setOtherSpeciesMotivation(tmpStr);
                        }
                        tmpChar = getChar(rs, this.fields.get("species_group"));
                        if (tmpChar != null) {
                            oSpecies.setOtherSpeciesGroup(tmpChar.toString());
                        }
                        tmpChar = getGroup(tables[i], oSpecies.getOtherSpeciesCode(), oSpecies.getOtherSpeciesName());
                        if (tmpChar != null) {
                            oSpecies.setOtherSpeciesGroup(tmpChar.toString());
                        }
                        oSpecies.setSite(site);
                        site.getOtherSpecieses().add(oSpecies);
                        //session.save(oSpecies);
                    }
                }


            }
        } catch (SQLException e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } catch (Exception e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } finally {
            rs.close();
            stmt.close();
        }
    }


    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processHabitats(Connection conn, Session session, Site site) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from " + tables.get("habit1") + " where sitecode ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;
            log("Processing Habitats");
            ImporterSiteMDB.log.info("Processing Habitats");
            while (rs.next()) {
                Habitat habitat = new Habitat();
                tmpStr = getString(rs, this.fields.get("habitat_code"));
                if (tmpStr != null) {
                    habitat.setHabitatCode(tmpStr);
                }

                log("      Processing Habitat:::" + habitat.getHabitatCode());
                ImporterSiteMDB.log.info("Processing Habitat:::" + habitat.getHabitatCode());

                tmpDouble = getDouble(rs, this.fields.get("habitat_cover"));
                if (tmpDouble != null) {
                    habitat.setHabitatCover(tmpDouble);
                }
                tmpChar = getChar(rs, this.fields.get("habitat_global"));
                if (tmpChar != null) {
                    habitat.setHabitatGlobal(tmpChar);
                }
                tmpChar = getChar(rs, this.fields.get("habitat_representativity"));
                if (tmpChar != null) {
                    habitat.setHabitatRepresentativity(tmpChar);
                }
                tmpChar = getChar(rs, this.fields.get("habitat_relative_surface"));
                if (tmpChar != null) {
                    habitat.setHabitatRelativeSurface(tmpChar);
                }
                tmpChar = getChar(rs, this.fields.get("habitat_conservation"));
                if (tmpChar != null) {
                    habitat.setHabitatConservation(tmpChar);
                }
                habitat.setSite(site);
                site.getHabitats().add(habitat);
            }
        } catch (SQLException e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } catch (Exception e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } finally {
            rs.close();
            stmt.close();
        }
    }


    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processDTypes(Connection conn, Session session, Site site) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from " + tables.get("desigc") + " where sitecode ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;

            log("Processing National Designation Type");
            ImporterSiteMDB.log.info("Processing National Designation Type");
            while (rs.next()) {
                NationalDtype dType = new NationalDtype();
                tmpStr = getString(rs, this.fields.get("national_designation_code"));
                if (tmpStr != null) {
                    dType.setNationalDtypeCode(tmpStr);
                }
                log("      Processing National Designation Type :::" + dType.getNationalDtypeCode());
                ImporterSiteMDB.log.info("Processing National Designation Type :::" + dType.getNationalDtypeCode());

                tmpDouble = getDouble(rs, this.fields.get("national_designation_cover"));
                if (tmpDouble != null) {
                    dType.setNationalDtypeCover(tmpDouble);
                }
                dType.setSite(site);
                site.getNationalDtypes().add(dType);
            }
        } catch (SQLException e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } catch (Exception e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } finally {
            rs.close();
            stmt.close();
        }
    }


    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processRelations(Connection conn, Session session, Site site) throws SQLException {
         Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from " + tables.get("desigr") + " where sitecode ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;

            log("Processing Relations");
            ImporterSiteMDB.log.info("Processing Relations");
            while (rs.next()) {
                SiteRelation relation = new SiteRelation();
                tmpStr = getString(rs, this.fields.get("relation_code"));
                if (tmpStr != null) {
                    relation.setSiteRelationCode(tmpStr);
                    log("      Processing Relation Code :::" + relation.getSiteRelationCode());
                    ImporterSiteMDB.log.info("Processing Relation Code :::" + relation.getSiteRelationCode());
                    if (tmpStr.toUpperCase().startsWith("IN")) {
                        relation.setSiteRelationScope('I');
                    } else {
                        relation.setSiteRelationScope('N');
                    }
                }

                tmpStr = getString(rs, this.fields.get("relation_name"));
                if (tmpStr != null) {
                    relation.setSiteRelationSitename(tmpStr);
                }
                tmpChar = getChar(rs, this.fields.get("relation_type"));
                if (tmpChar != null) {
                    relation.setSiteRelationType(tmpChar);
                }
                tmpDouble = getDouble(rs, this.fields.get("relation_cover"));
                if (tmpDouble != null) {
                    relation.setSiteRelationCover(tmpDouble);
                }
                relation.setSite(site);
                site.getSiteRelations().add(relation);
            }
         } catch (SQLException e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } catch (Exception e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } finally {
            rs.close();
            stmt.close();
        }
    }


    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processHabitatClasses(Connection conn, Session session, Site site) throws SQLException {
         Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from " + tables.get("habit2") + " where sitecode ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Double tmpDouble;

            log("Processing Habitat Class");
            ImporterSiteMDB.log.info("Processing Habitat Class");
            while (rs.next()) {
                HabitatClass habitat = new HabitatClass();

                tmpStr = getString(rs, this.fields.get("habitat_class_code"));
                if (tmpStr != null) {
                    habitat.setHabitatClassCode(tmpStr);
                }

                log("      Processing Habitat Class Code :::" + habitat.getHabitatClassCode());
                ImporterSiteMDB.log.info("Processing Habitat Class Code :::" + habitat.getHabitatClassCode());

                tmpDouble = getDouble(rs, this.fields.get("habitat_class_cover"));
                if (tmpDouble != null) {
                    habitat.setHabitatClassCover(tmpDouble);
                }
                String deschabClass = getDescHabitatClass(session, habitat.getHabitatClassCode());
                habitat.setHabitatClassDescription(deschabClass);
                habitat.setSite(site);
                site.getHabitatClasses().add(habitat);
            }
         } catch (SQLException e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } catch (Exception e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } finally {
            rs.close();
            stmt.close();
        }
    }

    /**
     *
     * @param session
     * @param habClassCode
     * @return
     */
    private String getDescHabitatClass(Session session, String habClassCode) {
        String descHabClass = "";
        String tableName = "RefHabClasses";
        String hql = "select refHabClassesDescrEn from " + tableName + " where refHabClassesCode like '" + habClassCode + "'";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        if (itr.hasNext()) {
            descHabClass = (String) itr.next();
        } else {
            ImporterSiteMDB.log.info("The description of the habitat class::" + habClassCode + " is missing.");
        }
        return descHabClass;
    }


    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processRegions(Connection conn, Session session, Site site) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from " + tables.get("regcod") + " where sitecode ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            ArrayList nutsList = new ArrayList();
            log("Processing Regions");
            ImporterSiteMDB.log.info("Processing Regions");
            while (rs.next()) {
                Region region = new Region();
                tmpStr = getString(rs, this.fields.get("region_code"));
                log("      Processing Region Code :::" + tmpStr);
                ImporterSiteMDB.log.info("Processing Regions Code :::" + tmpStr);
                if (tmpStr != null) {
                    /*just get NUT2 level*/
                    if (tmpStr.length() > 4) {

                        if (SDF_ManagerApp.isEmeraldMode() && StringUtils.startsWith(tmpStr, "'")) {
                            tmpStr = tmpStr.substring(1, 5);
                        } else {
                            tmpStr = tmpStr.substring(0, 4);
                        }

                    }
                    if (tmpStr.equals("0") || tmpStr.equals("00")) {
                        tmpStr = site.getSiteCode().substring(0, 2) + "ZZ";
                        region.setRegionName("Marine");
                        log(String.format("\tConverting marine region code (0 or 00) to NUTS code '%s'", tmpStr), false);
                    } else {
                        try {
                            String tableName = SDF_ManagerApp.isEmeraldMode() ? "RefNutsEmerald" : "RefNuts";
                            Iterator itr =  session.createQuery(" from " + tableName + " as rn where rn.refNutsCode like '"
                                    + tmpStr + "'").iterate();
                            if (itr.hasNext()) {
                                if (SDF_ManagerApp.isEmeraldMode()) {
                                    RefNutsEmerald rne = (RefNutsEmerald) itr.next();
                                    region.setRegionName(rne.getRefNutsDescription());
                                } else {
                                    RefNuts rn = (RefNuts) itr.next();
                                    region.setRegionName(rn.getRefNutsDescription());
                                }
                            } else {
                                nutsList.add(tmpStr);
                                log(String.format("\tCouldn't match NUTS code (%s). Encoding anyway.", tmpStr), false);

                            }
                        } catch (Exception e) {
                           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
                        }
                    }
                    region.setRegionCode(tmpStr);
                }
                if (nutsList != null && !(nutsList.isEmpty())) {
                    this.nutsKO.put(site.getSiteCode(), nutsList);
                }
                region.setSite(site);
                site.getRegions().add(region);
            }
        } catch (SQLException e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } catch (Exception e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } finally {
            rs.close();
            stmt.close();
        }
    }

    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processImpacts(Connection conn, Session session, Site site) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from " + tables.get("actvty") + " where sitecode ='" + site.getSiteCode() + "'";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;

            log("Processing Impacts ::::: ");
            ImporterSiteMDB.log.info("Processing Impacts ::::: ");

            while (rs.next()) {


                Impact impact = new Impact();
                Impact impactClon = null;
                String impactCodeOld = getString(rs, this.fields.get("impact_code"));

                if (impactCodeOld != null) {

                    if (impactCodeOld != null && !(("").equals(impactCodeOld))) {
                        ImporterSiteMDB.log.info(":: is going to get impact code...");
                        tmpStr = getImpactCode(impactCodeOld);


                        if (tmpStr != null) {
                            impact.setImpactCode(tmpStr);

                            // IN_OUT
                            tmpChar = getChar(rs, this.fields.get("impact_ocurrence"));
                            if (tmpChar != null) {
                                impact.setImpactOccurrence(tmpChar);
                            }

                            // INTENSITY
                            tmpChar = getChar(rs, this.fields.get("impact_rank"));
                            if (tmpChar != null) {
                                if (tmpChar.equals('A')) {
                                    impact.setImpactRank('H');
                                } else if (tmpChar.equals('B')) {
                                    impact.setImpactRank('M');
                                } else if (tmpChar.equals('C')) {
                                    impact.setImpactRank('L');
                                } else {
                                    impact.setImpactRank(tmpChar);
                                }
                                //impact.setImpactRank(tmpChar);
                            }

                            // INFLUENCE
                            tmpChar = getChar(rs, this.fields.get("impact_type"));
                            if (tmpChar != null) {

                                if (("+").equals(tmpChar.toString())) {
                                    impact.setImpactType('P');
                                } else if (("-").equals(tmpChar.toString())) {
                                    impact.setImpactType('N');
                                } else if (("0").equals(tmpChar.toString())) {
                                    impactClon = new Impact();
                                    impactClon.setImpactOccurrence(impact.getImpactOccurrence());
                                    impactClon.setImpactRank(impact.getImpactRank());
                                    impactClon.setImpactCode(impact.getImpactCode());
                                    impactClon.setImpactType('N');
                                    impact.setImpactType('P');
                                } else if (("O").equals(tmpChar.toString())) {
                                    impactClon = new Impact();
                                    impactClon = impact;
                                    impactClon.setImpactType('N');
                                    impact.setImpactType('P');
                                } else {
                                    ImporterSiteMDB.log.info("Impact code is not valid");
                                }

                            }
                            impact.setSite(site);
                            if (impactClon != null) {
                               impactClon.setSite(site);
                               site.getImpacts().add(impactClon);
                            }
                            site.getImpacts().add(impact);
                        }

                    }
                }
            }

        } catch (SQLException e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } catch (Exception e) {
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        } finally {
            rs.close();
            stmt.close();
        }
    }

   /**
     *
     * @param impactCodeOld
     * @return
     * @throws SQLException
     */
    private String getImpactCode(String impactCodeOld) throws SQLException {

        String impactCode = null;

        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql = "from RefImpacts where refImpactsOldcode ='" + impactCodeOld + "' order by ref_Impacts_Code";
        try {
            Query q = session.createQuery(hql);
            RefImpacts impact = (RefImpacts) q.uniqueResult();
            if (impact != null) {
               impactCode = impact.getRefImpactsCode();
            }

        } catch (Exception e) {
            //e.printStackTrace();
           ImporterSiteMDB.log.error("Error:::" + e.getMessage());
        }
        return impactCode;

    }


    /**
     *
     * @param type
     * @param sign
     * @param deg
     * @param min
     * @param sec
     * @return
     */
     Double convertCoordinate(int type, String sign, Double deg, Double min, Double sec) {
         /**
          * 1 = longitude; 2 = latitude
          */
         Double coord = 0.0;
         if (type == 1) {
             sign = sign == null ? "E" : sign.toUpperCase();//default to east
             coord = sign.equals("E") ? coord : (sign.equals("W") ? -coord : coord); //unknown default to east as well

         }
         deg = deg == null ? 0.0 : deg;
         min = min == null ? 0.0 : sec;
         sec = sec == null ? 0.0 : sec;
         coord = deg + ((min / 60)) + ((sec / 3600));
         return coord == 0.0 ? null : coord;
     }
     /**
      *
      * @param oldType
      * @return
      */
     String convertType(char oldType) {
         oldType = Character.toUpperCase(oldType);
         char[] spa = {'A', 'D', 'F', 'H', 'J'};
         char[] sci = {'B', 'E', 'G', 'I', 'K'};
         if (ArrayUtils.contains(spa, oldType)) {
             return "A";
         } else if (ArrayUtils.contains(sci, oldType)) {
             return "B";
         } else if (oldType == 'C') {
             return "C";
         } else {
            return "";
        }
     }

     /**
      *
      * @param num
      * @return
      */
     int converToInt(String num) {
         try {
             return Integer.parseInt(num);
         } catch (Exception e) {
            ImporterSiteMDB.log.error("Error:::" + e.getMessage());
             return 1;
         }
     }
     /**
      *
      * @param num
      * @return
      */
     Integer convertToIntN(String num) {
         try {
             return Integer.parseInt(num.replaceAll("[^0-9]", ""));
         } catch (Exception e) {
            ImporterSiteMDB.log.error("Error:::" + e.getMessage());
             return null;
         }
     }
     /**
      *
      * @param fileName
      * @param map
      * @param topElement
      * @param fields
      * @return
      */
     public Boolean parse(String fileName, HashMap map, String topElement, String[] fields) {
       Element root = this.parseXML(fileName);
       if (root != null) {
           NodeList nl = root.getElementsByTagName(topElement);
           if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    String key = this.getTextValue(el, fields[0]);
                    String value = this.getTextValue(el, fields[1]);
                    map.put(key, value);
                }
            }
           return true;
       } else {
           return false;
       }

    }
     /**
      *
      * @param fileName
      * @return
      */
    public Element parseXML(String fileName) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom;
        try {
            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            //parse using builder to get DOM representation of the XML file
            log("Parsing: " + fileName, true);
            dom = db.parse(fileName);
            return dom.getDocumentElement();
        } catch (ParserConfigurationException pce) {
           ImporterSiteMDB.log.error("Error`parsing XML:::" + pce.getMessage());
            return null;
        } catch (SAXException se) {
           ImporterSiteMDB.log.error("Error`parsing XML:::" + se.getMessage());
            return null;
        } catch (IOException ioe) {
           ImporterSiteMDB.log.error("Error`parsing XML:::" + ioe.getMessage());
            return null;
        }
     }
    /**
     *
     * @param ele
     * @param tagName
     * @return
     */
     private String getTextValue(Element ele, String tagName) {
            String textVal = "";
            NodeList nl = ele.getElementsByTagName(tagName);
            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);
                Node n = el.getFirstChild();
                if (n != null) {
                    textVal = el.getFirstChild().getNodeValue();
                } else {
                    textVal = "";
                }
            }
            return textVal;
      }

     public Connection c(String fileName) throws Exception {
         return getConnection(fileName);
     }
}
