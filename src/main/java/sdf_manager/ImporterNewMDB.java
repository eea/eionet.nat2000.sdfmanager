package sdf_manager;

import java.awt.Desktop;
import java.awt.Frame;
import java.io.File;
import pojos.*;

import java.io.FileWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.hibernate.Query;

import pojos.Species;
import sdf_manager.util.SDF_Util;


/**
 *
 * @author anon
 */
public class ImporterNewMDB implements Importer {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImporterNewMDB.class .getName());

    private Logger logger;
    private String encoding;
    private FileWriter outFile;
    private PrintWriter out;
    private String accessVersion;

    /**
     *
     * @param logger
     * @param encoding
     * @param logFile
     * @param accessVersion
     */
    public ImporterNewMDB(Logger logger, String encoding, String logFile, String accessVersion) {
        this.logger = logger;
        this.encoding = encoding;
        this.accessVersion = accessVersion;
        this.initLogFile(logFile);
        this.init();

    }

    /**
     *
     */
    void init() {
        //this.tables = new HashMap();
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
        } catch (Exception e) {
            ImporterNewMDB.log.error("Error::" + e.getMessage());
            //////e.printStackTrace();
        }
    }

    /**
     *
     */
    public void closeLogFile() {
        try {
            out.close();
            outFile.close();
        } catch (Exception e) {
            ImporterNewMDB.log.error("Error::" + e.getMessage());
           //////e.printStackTrace();
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
    public boolean processDatabase(String fileName) {
        Connection conn = null;
        boolean importOk = false;
        String msgValidError = "";

        try {
            /*
            ArrayList siteList = this.loadSpecies(conn);
            //ArrayList sitesDB = validateSites(siteList);
            HashMap sitesDB = validateSites(siteList);
            ImporterNewMDB.log.info("Validation has finished");
            log("Validation has finished.", 1);

            if (sitesDB != null && (sitesDB.isEmpty())) {
               ImporterNewMDB.log.info("Import process is starting");
               log("Import process is starting.", 1);
               importOk =this.processSites(conn, fileName);
               //importOk = true;
            } else {
                importOk = false;
                ImporterNewMDB.log.error("Error in validation");
                ImporterNewMDB.log.error("Some sites are already stored in Data Base. Please check the log file for details");
                log("Error in validation.", 1);
                JOptionPane.showMessageDialog(new JFrame(), "Some sites are already stored in Data Base. Please check the log file for details", "Dialog", JOptionPane.INFORMATION_MESSAGE);
                File fileLog = SDF_Util.copyToLogImportFile(sitesDB, "NewDB");
                if (fileLog != null) {
                    Desktop desktop = null;
                    if (Desktop.isDesktopSupported()) {
                        desktop = Desktop.getDesktop();
                        Desktop.getDesktop().open(fileLog);
                    }

                }
            }*/

            conn = getConnection(fileName);

            if (conn != null) {
                ArrayList<Site> siteList = this.loadSpecies(conn);
                importOk = validateAndProcessSites(conn, siteList);
                //HashMap<String, ArrayList<String>> sitesDB = validateSites(siteList);
                ImporterNewMDB.log.info("Validation has finished");
                log("Validation has finished.", 1);
/*
                if (sitesDB != null && (sitesDB.isEmpty())) {
                   ImporterNewMDB.log.info("Import process is starting");
                   log("Import process is starting.", 1);
                   importOk =this.processSites(conn, fileName);
                } else {
                    importOk = false;
                    ImporterNewMDB.log.error("Error in validation");
                    ImporterNewMDB.log.error("Some sites are already stored in Data Base. Please check the log file for details");
                    log("Error in validation.", 1);
                    JOptionPane.showMessageDialog(new JFrame(), "Some sites are already stored in Data Base. Please check the log file for details", "Dialog", JOptionPane.INFORMATION_MESSAGE);
                    File fileLog = SDF_Util.copyToLogImportFile(sitesDB, "NewDB");
                    if (fileLog != null) {
                        Desktop desktop = null;
                        if (Desktop.isDesktopSupported()) {
                            desktop = Desktop.getDesktop();
                            Desktop.getDesktop().open(fileLog);
                        }

                    }
                }
*/
            } else {
                importOk = false;
                msgValidError = "A DB error occurs. Please check the SDF_log file for more details";
            }

        } catch (Exception e) {
            ImporterNewMDB.log.error("Error in processDatabase::" + e.getMessage());
            importOk = false;
            importOk = false;
             //e.printStackTrace();
            //return importOk;
        } finally {
            if (importOk) {
                javax.swing.JOptionPane.showMessageDialog(new Frame(), "Import Processing has finished succesfully.", "Dialog", JOptionPane.INFORMATION_MESSAGE);;
            } else {
                log("Import is stopped.There are some errors in import process.", 1);
                javax.swing.JOptionPane.showMessageDialog(new Frame(), "There are some errors in import process.\n Please, check the SDF_Log file for more details", "Dialog", JOptionPane.ERROR_MESSAGE);;
            }

        }
        return importOk;
    }


     /**
      *
      * @param conn
      */
     private HashMap<String, ArrayList<String>> validateSites(ArrayList siteList) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        HashMap<String, ArrayList<String>> siteHasHDB = new HashMap<String, ArrayList<String>>();
        try {
            int j = 0;
            for (int i = 0; i < siteList.size(); i++) {
               try {
                   Site site = (Site) siteList.get(i);
                   Transaction tx = session.beginTransaction();
                   ImporterNewMDB.log.info("Validating site: " + site.getSiteCode());
                   log("Validating site: " + site.getSiteCode(), 1);

                   boolean siteInDB = false;
                   if (SDF_Util.validateSite(session, site.getSiteCode())) {
                       siteInDB = true;
                   }
                   Set regionSiteList = site.getRegions();
                   Iterator itr = regionSiteList.iterator();
                   ArrayList<String> nutsNoOK = new ArrayList<String>();
                   while (itr.hasNext()) {
                       String nuts = (String) itr.next();
                        if (!isRegionLevel2(nuts)) {
                            nutsNoOK.add(nuts);
                        }
                   }
                   if (siteInDB) {
                       siteHasHDB.put(site.getSiteCode(), nutsNoOK);
                   }
                   tx.commit();
              } catch (Exception e) {
                ImporterNewMDB.log.error("Error: " + e.getMessage());
                break;
              }
              if (++j % 20 == 0) {
                 session.flush();
                 session.clear();
             }
          }

        } catch (Exception e) {
            ImporterNewMDB.log.error("Error: " + e.getMessage());
            return siteHasHDB;
        } finally {
            session.close();
        }
        return siteHasHDB;

     }

    /**
     *
     * @param fileName
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    Connection getConnection(String fileName) throws ClassNotFoundException, SQLException {
        try {
             if (accessVersion.equals("2003")) {
                 /*open read-only*/
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                //añadido , *.accdb) a la cadena db
                String db = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};Dbq=" + fileName + ";";
                Connection conn = DriverManager.getConnection(db, "", "");
                return conn;
             } else {
                /*open read-only*/
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                //añadido , *.accdb) a la cadena db
                String db = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + fileName + ";";
                Connection conn = DriverManager.getConnection(db, "", "");
                return conn;
             }
         } catch (ClassNotFoundException e) {
             //e.printStackTrace();
             ImporterNewMDB.log.error("Error conecting to MS Access DB. Error Message:::" + e.getMessage());
             return null;
         } catch (SQLException e) {
             //e.printStackTrace();
             ImporterNewMDB.log.error("Error conecting to MS Access DB. Error Message:::" + e.getMessage());
             return null;
         } catch (Exception e) {
             //e.printStackTrace();
             ImporterNewMDB.log.error("Error conecting to MS Access DB. Error Message:::" + e.getMessage());
             return null;
         }
    }


    /**
     *
     * @param conn
     * @throws SQLException
     */
    private ArrayList<Site> loadSpecies(Connection conn) throws SQLException {

        ArrayList<Site> siteList = new ArrayList<Site>();
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql = "from Site";
        try {
            Query q = session.createQuery(hql);
            Iterator itr = q.iterate();
            while (itr.hasNext()) {
                Site rs = (Site) itr.next();
                siteList.add(rs);
            }
        } catch (Exception e) {
            ImporterNewMDB.log.error("Error: " + e.getMessage());
        }
        return siteList;
    }

    /**
     * New method for validating and process only the ones that doesn't exist
     */
    boolean validateAndProcessSites(Connection conn, ArrayList siteList) throws SQLException {

        ArrayList<String> notProcessedSiteCodesList = new ArrayList<String>();
        boolean processOK = false;

        String sql = "select site_code from site";
        Session session = null;
        Statement stmt = null;

        //HashMap<String, ArrayList<String>> siteHasHDB = new HashMap<String, ArrayList<String>>();
        try {
            int j = 0;
            session = HibernateUtil.getSessionFactory().openSession();
            //loadSpecies(conn, session);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Site site = new Site();
                String sitecode = rs.getString("site_code");

                this.log.info("Validating site: " + sitecode);
                log("Validating site: " + sitecode, 1);
                try {
                    if (SDF_Util.validateSite(session, sitecode)) {
                        notProcessedSiteCodesList.add(sitecode);
                    } else {
                        Transaction tx = session.beginTransaction();
                        log("processing: " + sitecode, 1);
                        site.setSiteCode(sitecode);
                        processSite(conn, session, site);
                        processSpecies(conn, session, site);
                        processOtherSpecies(conn, session, site);
                        processHabitats(conn, session, site);
                        processHabitatClasses(conn, session, site);
                        processRegions(conn, session, site);
                        processRelations(conn, session, site);
                        processDTypes(conn, session, site);
                        processImpacts(conn, session, site);
                        processSiteoOwnerShips(conn, session, site);
                        tx.commit();
                        processOK = true;
                    }
                } catch (Exception e) {
                    this.log.error("Failed processing site: " + sitecode + " .The error: " + e.getMessage());
                    log("failed processing site: " + sitecode, 1);
                    //break;
                }

            }

        } catch (Exception e) {
            ImporterNewMDB.log.error("Error: " + e.getMessage());
        } finally {
            session.close();
        }

        /**
         * If a sitecode already exists, show log file with the conflictive sitecodes
         */
        if (notProcessedSiteCodesList != null && !notProcessedSiteCodesList.isEmpty()) {

            ImporterNewMDB.log.error("Error in validation:. Error Message: Some sites are already stored in Data Base. Please check the log file for details");
            log("Error in validation.", 1);
            //msgValidError = "Some sites are already stored in Data Base. Please check the log file for details";

            File fileLog = SDF_Util.copyToLogImportFileList(notProcessedSiteCodesList, "OldDB");
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

          return processOK;
    }


    /**
     *
     * @param conn
     * @throws SQLException
     */
    private boolean processSites(Connection conn, String fileName) throws SQLException {

        boolean processOK = false;
        Session session = new Configuration().configure().setProperty("hibernate.jdbc.batch_size", "20").setProperty("hibernate.cache.use_second_level_cache", "false").buildSessionFactory().openSession();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            if (conn == null) {
                conn = getConnection(fileName);
            }
            if (conn !=  null) {
                String sql = "select site_code from site";
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                int i = 0;
                int j = 0;

                while (rs.next()) {
                    Site site = new Site();

                    String sitecode = getString(rs, "SITE_CODE");
                    try {
                        Transaction tx = session.beginTransaction();
                        log("processing: " + sitecode, 1);
                        site.setSiteCode(sitecode);
                        processSite(conn, session, site);
                        processSpecies(conn, session, site);
                        processOtherSpecies(conn, session, site);
                        processHabitats(conn, session, site);
                        processHabitatClasses(conn, session, site);
                        processRegions(conn, session, site);
                        processRelations(conn, session, site);
                        processDTypes(conn, session, site);
                        processImpacts(conn, session, site);
                        processSiteoOwnerShips(conn, session, site);
                        tx.commit();
                        processOK = true;
                    } catch (Exception e) {
                        //e.printStackTrace();
                        ImporterNewMDB.log.error("Failed processing site: " + sitecode + " Error: " + e.getMessage());
                        log("Failed processing site: " + sitecode, 1);
                        processOK = false;
                        break;
                    }


                    if (++i % 20 == 0) {
                        session.flush();
                        session.clear();
                    }
                    j++;
                }
                ImporterNewMDB.log.info("Finishing import process.Closing connection to Data Base");
                log("Finishing import process.Closing connection to Data Base");
            } else {
                processOK = false;

            }

        } catch (SQLException e) {
            ImporterNewMDB.log.error(" SQL Error: " + e.getMessage());
            processOK = false;
            //e.printStackTrace();
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            processOK = false;
            //e.printStackTrace();
        } finally {
            stmt.close();
           // rs.close();
            if (!conn.isClosed()) {
                conn.close();
            }
            session.close();

        }
        return processOK;
    }

    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processSite(Connection conn, Session session, Site site) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try {

            String sql = "select * from SITE where SITE_CODE ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Double tmpDouble;
            int tmpInt;
            Date tmpDate;

            while (rs.next()) {

                tmpStr = getString(rs, "SITE_NAME");
                if (tmpStr != null) {
                    site.setSiteName(tmpStr);
                }

                tmpStr = getString(rs, "SITE_TYPE");
                if (tmpStr != null) {

                    site.setSiteType(tmpStr.charAt(0));
                }

                tmpDate = rs.getDate("SITE_COMP_DATE");
                if (tmpDate != null) {
                    site.setSiteCompDate(tmpDate);
                }
                tmpDate = rs.getDate("SITE_UPDATE_DATE");
                if (tmpDate != null) {
                    site.setSiteUpdateDate(tmpDate);
                }
                tmpDate = rs.getDate("SITE_SCI_PROP_DATE");
                if (tmpDate != null) {
                    site.setSiteSciPropDate(tmpDate);
                }
                tmpDate = rs.getDate("SITE_SCI_CONF_DATE");
                if (tmpDate != null) {
                    site.setSiteSciConfDate(tmpDate);
                }
                tmpDate = rs.getDate("SITE_SPA_DATE");
                if (tmpDate != null) {
                    site.setSiteSpaDate(tmpDate);
                }

                tmpStr = getString(rs, "SITE_SPA_LEGAL_REF");
                if (tmpStr != null) {
                    site.setSiteSpaLegalRef(tmpStr);
                }

                tmpDate = rs.getDate("SITE_SAC_DATE");
                if (tmpDate != null) {
                    site.setSiteSacDate(tmpDate);
                }

                tmpStr = getString(rs, "SITE_SAC_LEGAL_REF");
                if (tmpStr != null) {
                    site.setSiteSacLegalRef(tmpStr);
                }

                tmpStr = getString(rs, "SITE_EXPLANATIONS");
                if (tmpStr != null) {
                    site.setSiteExplanations(tmpStr);
                }

                tmpInt = rs.getInt("RESP_ID");
                if (tmpInt != 0) {
                    Resp resp = new Resp();
                    //resp.setRespId(tmpInt);


                    session.save(resp);
                    resp = getRespData(tmpInt, conn, resp, session);

                    resp.getSites().add(site);
                    session.save(resp);
                    site.setResp(resp);
                }

                tmpDouble = rs.getDouble("SITE_AREA");
                if (tmpDouble != null) {
                    site.setSiteArea(tmpDouble);
                }

                tmpDouble = rs.getDouble("SITE_MARINE_AREA");
                if (tmpDouble != null) {
                    site.setSiteMarineArea(tmpDouble);
                }

                tmpDouble = rs.getDouble("SITE_LENGTH");
                if (tmpDouble != null) {
                    site.setSiteLength(tmpDouble);
                }

                Double longitude = rs.getDouble("SITE_LONGITUDE");
                if (longitude != 0) {
                    site.setSiteLongitude(longitude);
                }

                Double latitude = rs.getDouble("SITE_LATITUDE");
                if (latitude != null) {
                    site.setSiteLatitude(latitude);
                }

                tmpStr = getString(rs, "SITE_DESIGNATION");
                if (tmpStr != null) {
                    site.setSiteDesignation(tmpStr);
                }

                tmpStr = getString(rs, "SITE_QUALITY");
                if (tmpStr != null) {
                    site.setSiteQuality(tmpStr);
                }

                tmpStr = getString(rs, "SITE_CHARACTERISTICS");
                if (tmpStr != null) {
                    site.setSiteCharacteristics(tmpStr);
                }

                tmpInt = rs.getInt("DOC_ID");
                if (tmpInt != 0) {
                    Doc doc = new Doc();
                    //doc.setDocId(tmpInt);
                    session.save(doc);
                    doc = getDocData(tmpInt, conn, doc, session);
                    doc = getDocucumentLinks(tmpInt, conn, doc, session);

                    doc.getSites().add(site);
                    session.save(doc);
                    site.setDoc(doc);
                }

                tmpInt = rs.getInt("MGMT_ID");
                if (tmpInt != 0) {
                    Mgmt mgmt = new Mgmt();
                    session.save(mgmt);
                    mgmt = getMgmtData(tmpInt, conn, mgmt, session);
                    mgmt = getMgmtBodyList(tmpInt, conn, mgmt, session);
                    mgmt = getMgmtPlanList(tmpInt, conn, mgmt, session);

                    mgmt.getSites().add(site);
                    session.save(mgmt);
                    site.setMgmt(mgmt);
                }
                tmpInt = rs.getInt("MAP_ID");
                if (tmpInt != 0) {
                    Map mapSite = new Map();
                    session.save(mapSite);
                    mapSite = getMapData(tmpInt, conn, mapSite, session);

                    site.setMap(mapSite);
                }

                Calendar cal = Calendar.getInstance();
                site.setSiteDateCreation(cal.getTime());
                session.save(site);

                Set<SiteBiogeo> itSet = loadSiteBiogeoBySite(conn, session, site);

                if (itSet != null) {
                    Iterator itReg = itSet.iterator();
                    while (itReg.hasNext()) {
                        //String regionKey = (String) itReg.next();
                        SiteBiogeo regionKey = (SiteBiogeo) itReg.next();

                        //int region_key = ((Integer) regionCodes.get(regionKey)).intValue();

                        /*int region_key =loadBiogeoRegionId(regionKey);
                        Biogeo biogeo = (Biogeo) session.load(Biogeo.class, region_key);

                        SiteBiogeoId id = new SiteBiogeoId(site.getSiteCode(), biogeo.getBiogeoId());
                        //siteBiogeo.setId(id);
                        SiteBiogeo siteBiogeo = new SiteBiogeo(id, biogeo, site);
                        site.getSiteBiogeos().add(siteBiogeo);*/
                        site.getSiteBiogeos().add(regionKey);
                    //session.save(siteBiogeo);
//                 }
                    }

                }

            }
            stmt.close();
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            ////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            ////e.printStackTrace();
        } finally {
            stmt.close();
            //rs.close();
        }

    }

    /**
     *
     * @param conn
     * @param session
     * @param site
     * @return
     * @throws SQLException
     */
    private Set<SiteBiogeo> loadSiteBiogeoBySite(Connection conn, Session session, Site site) throws SQLException {
        Set<SiteBiogeo> siteBiogeoSet = new HashSet<SiteBiogeo>(0);
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from SITE_BIOGEO where site_code ='" + site.getSiteCode() + "'";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {

                int biogeId = rs.getInt("BIOGEO_ID");
                double percent = rs.getDouble("BIOGEO_PERCENT");

                Biogeo biogeo = (Biogeo) session.load(Biogeo.class, biogeId);

                SiteBiogeoId id = new SiteBiogeoId(site.getSiteCode(), biogeo.getBiogeoId());

                SiteBiogeo siteBiogeo = new SiteBiogeo(id, biogeo, site);

                siteBiogeo.setBiogeoPercent(percent);
                site.getSiteBiogeos().add(siteBiogeo);
                siteBiogeoSet.add(siteBiogeo);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            ImporterNewMDB.log.error("Error loading site biogeo data ::: Error Message:::" + e.getMessage());
        } finally {
            stmt.close();
        }
        return siteBiogeoSet;
    }


    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processSpecies(Connection conn, Session session, Site site) throws SQLException {

        /***All the species are in the same table**/
        Statement stmt = null;
        ResultSet rs = null;
        try {

            String sql = "select * from SPECIES where site_code ='" + site.getSiteCode() + "'";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Short tmpShort;
            int tmpInt;
            String spName = "";

            while (rs.next()) {
                Species species = new Species();

                tmpStr = getString(rs, "SPECIES_GROUP");
                if (tmpStr != null) {
                    species.setSpeciesGroup(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "species_code");
                if (tmpStr != null) {
                    species.setSpeciesCode(tmpStr);
                }

                tmpStr = getString(rs, "species_name");
                if (tmpStr != null) {
                    species.setSpeciesName(tmpStr);
                    spName = tmpStr;
                }

                tmpShort = rs.getShort("SPECIES_SENSITIVE");
                if (tmpShort != 0) {
                    species.setSpeciesSensitive(tmpShort);
                }

                tmpShort = rs.getShort("SPECIES_NP");
                if (tmpShort != 0) {
                    species.setSpeciesNp(tmpShort);
                }

                tmpStr = getString(rs, "SPECIES_TYPE");
                if (tmpStr != null) {
                    species.setSpeciesType(tmpStr.charAt(0));
                }

                tmpInt = rs.getInt("SPECIES_SIZE_MIN");
                if (tmpInt != 0) {
                    species.setSpeciesSizeMin(tmpInt);
                }

                tmpInt = rs.getInt("SPECIES_SIZE_MAX");
                if (tmpInt != 0) {
                    species.setSpeciesSizeMax(tmpInt);
                }

                tmpStr = getString(rs, "SPECIES_UNIT");
                if (tmpStr != null) {
                    species.setSpeciesUnit(tmpStr);
                }

                tmpStr = getString(rs, "SPECIES_CATEGORY");
                if (tmpStr != null) {
                    species.setSpeciesCategory(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "SPECIES_DATA_QUALITY");
                if (tmpStr != null) {
                    //species.setSpeciesDataQuality(tmpStr.charAt(0));
                    species.setSpeciesDataQuality(tmpStr);
                }

                tmpStr = getString(rs, "species_population");
                if (tmpStr != null) {
                    species.setSpeciesPopulation(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "species_conservation");
                if (tmpStr != null) {
                    species.setSpeciesConservation(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "species_isolation");
                if (tmpStr != null) {
                    species.setSpeciesIsolation(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "species_global");
                if (tmpStr != null) {
                    species.setSpeciesGlobal(tmpStr.charAt(0));
                }
                species.setSite(site);
                site.getSpecieses().add(species);
            }

        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();

        } finally {
            stmt.close();
           // rs.close();
        }

    }

    /**
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processOtherSpecies(Connection conn, Session session, Site site) throws SQLException {

        Statement stmt = null;
        ResultSet rs = null;
        try {

            String sql = "select * from OTHER_SPECIES where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            String spName = "";
            Short tmpShort;
            int tmpInt;

            while (rs.next()) {
                OtherSpecies otherSpecies = new OtherSpecies();

                tmpStr = getString(rs, "OTHER_SPECIES_GROUP");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesGroup(tmpStr);
                }

                tmpStr = getString(rs, "OTHER_SPECIES_CODE");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesCode(tmpStr);
                }

                tmpStr = getString(rs, "OTHER_SPECIES_NAME");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesName(tmpStr);
                    spName = getString(rs, "OTHER_SPECIES_NAME");
                }

                tmpShort = rs.getShort("OTHER_SPECIES_SENSITIVE");
                if (tmpShort != 0) {
                    otherSpecies.setOtherSpeciesSensitive(tmpShort);
                }

                tmpShort = rs.getShort("OTHER_SPECIES_NP");
                if (tmpShort != 0) {
                    otherSpecies.setOtherSpeciesNp(tmpShort);
                }

                tmpInt = rs.getInt("OTHER_SPECIES_SIZE_MIN");
                if (tmpInt != 0) {
                    otherSpecies.setOtherSpeciesSizeMin(tmpInt);
                }

                tmpInt = rs.getInt("OTHER_SPECIES_SIZE_MAX");
                if (tmpInt != 0) {
                    otherSpecies.setOtherSpeciesSizeMax(tmpInt);
                }

                tmpStr = getString(rs, "OTHER_SPECIES_UNIT");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesUnit(tmpStr);
                }

                tmpStr = getString(rs, "OTHER_SPECIES_CATEGORY");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesCategory(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "OTHER_SPECIES_MOTIVATION");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesMotivation(tmpStr);
                }
                otherSpecies.setSite(site);
                site.getOtherSpecieses().add(otherSpecies);
            }

        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
           // rs.close();
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
            String sql = "select * from HABITAT where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;
            Short tmpShort;
            int tmpInt;
            while (rs.next()) {
                Habitat habitat = new Habitat();

                tmpStr = getString(rs, "HABITAT_CODE");
                if (tmpStr != null) {
                    habitat.setHabitatCode(tmpStr);
                }
                tmpShort = rs.getShort("HABITAT_PRIORITY");

                if (tmpShort != 0) {
                    habitat.setHabitatPriority(tmpShort);
                }

                tmpDouble = rs.getDouble("HABITAT_COVER");
                if (tmpDouble != 0) {
                    habitat.setHabitatCover(tmpDouble);
                }

                tmpDouble = rs.getDouble("HABITAT_COVER_HA");
                if (tmpDouble != 0) {
                    habitat.setHabitatCoverHa(tmpDouble);
                }

                tmpShort = rs.getShort("HABITAT_NP");
                if (tmpShort != 0) {
                    habitat.setHabitatNp(tmpShort);
                }

                tmpInt = rs.getInt("HABITAT_CAVES");
                if (tmpInt != 0) {
                    habitat.setHabitatCaves(tmpInt);
                }
                tmpStr = getString(rs, "HABITAT_DATA_QUALITY");
                if (tmpStr != null) {
                    habitat.setHabitatDataQuality(tmpStr);
                }

                tmpStr = getString(rs, "HABITAT_REPRESENTATIVITY");
                if (tmpStr != null) {
                    habitat.setHabitatRepresentativity(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "HABITAT_RELATIVE_SURFACE");
                if (tmpStr != null) {
                    habitat.setHabitatRelativeSurface(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "HABITAT_CONSERVATION");
                if (tmpStr != null) {
                    habitat.setHabitatConservation(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "HABITAT_GLOBAL");
                if (tmpStr != null) {
                    habitat.setHabitatGlobal(tmpStr.charAt(0));
                }

                habitat.setSite(site);
                site.getHabitats().add(habitat);
                //session.save(habitat);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
           // rs.close();
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
            String sql = "select * from HABITAT_CLASS where site_code ='" + site.getSiteCode() + "'";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Double tmpDouble;
            while (rs.next()) {
                HabitatClass habitat = new HabitatClass();

                tmpStr = getString(rs, "HABITAT_CLASS_CODE");
                if (tmpStr != null) {
                    habitat.setHabitatClassCode(tmpStr);
                }

                tmpStr = getString(rs, "HABITAT_CLASS_DESCRIPTION");
                if (tmpStr != null) {
                    habitat.setHabitatClassDescription(tmpStr);
                }

                tmpDouble = rs.getDouble("HABITAT_CLASS_COVER");
                if (tmpDouble != 0) {
                    habitat.setHabitatClassCover(tmpDouble);
                }
                habitat.setSite(site);
                site.getHabitatClasses().add(habitat);
                //session.save(habitat);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
           // rs.close();
        }

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
            String sql = "select * from REGION where site_code ='" + site.getSiteCode() + "'";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            while (rs.next()) {
                Region region = new Region();

                tmpStr = getString(rs, "region_code");
                if (tmpStr != null) {

                    /*just get NUT2 level*/
                    if (tmpStr.length() > 4) {
                        tmpStr = tmpStr.substring(0, 4);
                    }
                    if (tmpStr.equals("0") || tmpStr.equals("00")) {
                        tmpStr = site.getSiteCode().substring(0, 2) + "ZZ";
                        region.setRegionName("Marine");
                        log(String.format("\tConverting marine region code (0 or 00) to NUTS code '%s'", tmpStr), 2);
                    } else {
                        try {
                            Iterator itr = session.createQuery("from RefNuts as rn where rn.refNutsCode like '" + tmpStr + "'").iterate();

                            if (itr.hasNext()) {
                                RefNuts rn = (RefNuts) itr.next();
                                if (rn.getRefNutsDescription() != null) {
                                    region.setRegionName(rn.getRefNutsDescription());
                                } else {
                                    region.setRegionName(rn.getRefNutsDescription());
                                    log(String.format("\tCouldn't match NUTS code (%s). Encoding anyway.", tmpStr), 2);
                                }

                            } else {
                                log(String.format("\tCouldn't match NUTS code (%s). Encoding anyway.", tmpStr), 2);
                            }
                        } catch (Exception e) {
                            ////e.printStackTrace();
                            ImporterNewMDB.log.error("An error has occurred in import process. Region section::Error Message:::" + e.getMessage());
                        }
                    }
                    region.setRegionCode(tmpStr);
                }
                region.setSite(site);
                site.getRegions().add(region);
                //session.save(region);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
           // rs.close();
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
            String sql = "select * from SITE_RELATION where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;
            while (rs.next()) {
                SiteRelation relation = new SiteRelation();

                tmpStr = getString(rs, "SITE_RELATION_CODE");
                if (tmpStr != null) {
                    relation.setSiteRelationCode(tmpStr);
                }

                tmpStr = getString(rs, "SITE_RELATION_SCOPE");
                if (tmpStr != null && tmpStr.toUpperCase().startsWith("I")) {
                    relation.setSiteRelationScope('I');
                } else {
                    relation.setSiteRelationScope('N');
                }

                tmpStr = getString(rs, "site_relation_sitename");
                if (tmpStr != null) {
                    relation.setSiteRelationSitename(tmpStr);
                }

                tmpStr = getString(rs, "site_relation_type");
                if (tmpStr != null) {
                    relation.setSiteRelationType(tmpStr.charAt(0));
                }
                tmpStr = getString(rs, "SITE_RELATION_CONVENTION");
                if (tmpStr != null) {
                    relation.setSiteRelationConvention(tmpStr);
                }

                tmpDouble = getDouble(rs, "SITE_RELATION_COVER");
                if (tmpDouble != 0) {
                    relation.setSiteRelationCover(tmpDouble);
                }
                relation.setSite(site);
                site.getSiteRelations().add(relation);
                session.save(relation);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
           // rs.close();
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
            String sql = "select * from NATIONAL_DTYPE where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;
            while (rs.next()) {
                NationalDtype dType = new NationalDtype();

                tmpStr = getString(rs, "NATIONAL_DTYPE_CODE");
                if (tmpStr != null) {
                    dType.setNationalDtypeCode(tmpStr);
                }

                tmpDouble = rs.getDouble("NATIONAL_DTYPE_COVER");
                if (tmpDouble != 0) {
                    dType.setNationalDtypeCover(tmpDouble);
                }
                dType.setSite(site);
                site.getNationalDtypes().add(dType);
                session.save(dType);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
           // rs.close();
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
            String sql = "select * from IMPACT where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;
            while (rs.next()) {
                Impact impact = new Impact();

                tmpStr = getString(rs, "IMPACT_TYPE");
                if (tmpStr != null) {
                    impact.setImpactType(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "IMPACT_RANK");
                if (tmpStr != null) {
                    impact.setImpactRank(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "IMPACT_CODE");
                if (tmpStr != null) {
                    impact.setImpactCode(tmpStr);
                }

                tmpStr = getString(rs, "IMPACT_POLLUTION_CODE");
                if (tmpStr != null) {
                    impact.setImpactPollutionCode(tmpStr.charAt(0));
                }

                tmpStr = getString(rs, "IMPACT_OCCURRENCE");
                if (tmpStr != null) {
                    impact.setImpactOccurrence(tmpStr.charAt(0));
                }
                impact.setSite(site);
                site.getImpacts().add(impact);
                session.save(impact);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
            //rs.close();
        }

    }

    /***
     *
     * @param conn
     * @param session
     * @param site
     * @throws SQLException
     */
    private void processSiteoOwnerShips(Connection conn, Session session, Site site) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from site_ownership where site_code ='" + site.getSiteCode() + "'";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Integer tmpInt;
            Double tmpDouble;
            while (rs.next()) {
                SiteOwnership siteOwnerShip = new SiteOwnership();

                tmpDouble = rs.getDouble("OWNERSHIP_PERCENT");
                if (tmpDouble != 0) {
                    siteOwnerShip.setOwnershipPercent(tmpDouble);
                }

                tmpInt = rs.getInt("OWNERSHIP_ID");
                if (tmpInt != null) {
                    SiteOwnershipId siteOwnerShipId = new SiteOwnershipId(tmpInt, site.getSiteCode());
                    siteOwnerShip.setId(siteOwnerShipId);
                }


                siteOwnerShip.setSite(site);
                site.getSiteOwnerships().add(siteOwnerShip);
                session.save(siteOwnerShip);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
            //rs.close();
        }

    }

    /**
     *
     * @param mgmtId
     * @param conn
     * @param doc
     * @param session
     * @return
     * @throws SQLException
     */
    private Doc getDocData(int docId, Connection conn, Doc doc, Session session) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from DOC where doc_id =" + docId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String description = rs.getString("DOC_DESCRIPTION");
                if (description != null) {
                    doc.setDocDescription(description);
                }
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
           stmt.close();
           //rs.close();
        }

        return doc;


    }

    /**
     *
     * @param respId
     * @param conn
     * @param resp
     * @param session
     * @return
     * @throws SQLException
     */
    private Resp getRespData(int respId, Connection conn, Resp resp, Session session) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from RESP where resp_id =" + respId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {

                String respName = rs.getString("RESP_NAME");
                String respAddress = rs.getString("RESP_ADDRESS");
                String respEmail = rs.getString("RESP_EMAIL");
                String respAdminUnit = rs.getString("RESP_ADMINUNIT");
                String respLocatorDesig = rs.getString("RESP_LOCATORDESIGNATOR");
                String respLocatorName = rs.getString("RESP_LOCATORNAME");
                String respAddressArea = rs.getString("RESP_ADDRESSAREA");
                String respPostName = rs.getString("RESP_POSTNAME");
                String respPostCode = rs.getString("RESP_POSTCODE");
                String respThoughfare = rs.getString("RESP_THOROUGHFARE");


                if (respName != null && !(("").equals(respName))) {
                    resp.setRespName(respName);
                }
                if (respAddress != null && !(("").equals(respAddress))) {
                    resp.setRespAddress(respAddress);
                }
                if (respEmail != null && !(("").equals(respEmail))) {
                    resp.setRespEmail(respEmail);
                }
                if (respAdminUnit != null && !(("").equals(respAdminUnit))) {
                    resp.setRespAdminUnit(respAdminUnit);
                }
                if (respAddressArea != null && !(("").equals(respAddressArea))) {
                    resp.setRespAddressArea(respAddressArea);
                }
                if (respLocatorDesig != null && !(("").equals(respLocatorDesig))) {
                    resp.setRespLocatorDesig(respLocatorDesig);
                }
                if (respLocatorName != null && !(("").equals(respLocatorName))) {
                    resp.setRespLocatorName(respLocatorName);
                }
                if (respPostCode != null && !(("").equals(respPostCode))) {
                    resp.setRespPostCode(respPostCode);
                }
                if (respPostName != null && !(("").equals(respPostName))) {
                    resp.setRespPostName(respPostName);
                }
                if (respThoughfare != null && !(("").equals(respThoughfare))) {
                    resp.setRespThoroughFare(respThoughfare);
                }

            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
           stmt.close();
           //rs.close();
        }

        return resp;


    }

    /**
     *
     * @param mapId
     * @param conn
     * @param mapSite
     * @param session
     * @return
     * @throws SQLException
     */
     private Map getMapData(int mapId, Connection conn, Map mapSite, Session session) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from MAP where map_id =" + mapId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {

                String mapInspired = rs.getString("MAP_INSPIRE");
                short mapPDF = rs.getShort("MAP_PDF");
                String mapRef = rs.getString("MAP_REFERENCE");

                if (mapInspired != null) {
                    mapSite.setMapInspire(mapInspired);
                }
                if (mapPDF != 0) {
                    mapSite.setMapPdf(mapPDF);
                }
                if (mapRef != null) {
                    mapSite.setMapReference(mapRef);
                }

            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
           stmt.close();
           //rs.close();
        }

        return mapSite;


    }

    /**
     *
     * @param mgmtId
     * @param conn
     * @param mgmt
     * @param session
     * @return
     * @throws SQLException
     */
    private Mgmt getMgmtData(int mgmtId, Connection conn, Mgmt mgmt, Session session) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from MGMT where mgmt_id =" + mgmtId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {

                String mgmtStatus = rs.getString("MGMT_STATUS");
                String mgmtConsMeasures = rs.getString("MGMT_CONSERV_MEASURES");

                if (mgmtStatus != null && !(("").equals(mgmtStatus))) {
                    mgmt.setMgmtStatus(mgmtStatus.charAt(0));
                }
                if (mgmtConsMeasures != null && !(("").equals(mgmtConsMeasures))) {
                    mgmt.setMgmtConservMeasures(mgmtConsMeasures);
                }

            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
           stmt.close();
           //rs.close();
        }

        return mgmt;


    }

    /**
     *
     * @param mgmtId
     * @param conn
     * @param mgmt
     * @param session
     * @return
     * @throws SQLException
     */
    private Mgmt getMgmtBodyList(int mgmtId, Connection conn, Mgmt mgmt, Session session) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from MGMT_BODY where mgmt_id =" + mgmtId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                MgmtBody mgmtBody = new MgmtBody();

                String mgmtBodyOrg = rs.getString("MGMT_BODY_ORG");
                String mgmtBodyAddress = rs.getString("MGMT_BODY_ADDRESS");
                String mgmtBodyEmail = rs.getString("MGMT_BODY_EMAIL");
                String mgmtBodyAdminUnit = rs.getString("MGMT_ADMINUNIT");
                String mgmtBodyLocatorDesig = rs.getString("MGMT_LOCATORDESIGNATOR");
                String mgmtBodyLocatorName = rs.getString("MGMT_LOCATORNAME");
                String mgmtBodyAddressArea = rs.getString("MGMT_ADDRESSAREA");
                String mgmtBodyPostName = rs.getString("MGMT_POSTNAME");
                String mgmtBodyPostCode = rs.getString("MGMT_POSTCODE");
                String mgmtBodyThorughfare = rs.getString("MGMT_THOROUGHFARE");


                if (mgmtBodyOrg != null && !(("").equals(mgmtBodyOrg))) {
                    mgmtBody.setMgmtBodyOrg(mgmtBodyOrg);
                }
                if (mgmtBodyAddress != null && !(("").equals(mgmtBodyAddress))) {
                    mgmtBody.setMgmtBodyAddress(mgmtBodyAddress);
                }
                if (mgmtBodyEmail != null && !(("").equals(mgmtBodyEmail))) {
                    mgmtBody.setMgmtBodyEmail(mgmtBodyEmail);
                }
                if (mgmtBodyAdminUnit != null && !(("").equals(mgmtBodyAdminUnit))) {
                    mgmtBody.setMgmtBodyAdminUnit(mgmtBodyAdminUnit);
                }
                if (mgmtBodyAddressArea != null && !(("").equals(mgmtBodyAddressArea))) {
                    mgmtBody.setMgmtBodyAddressArea(mgmtBodyAddressArea);
                }
                if (mgmtBodyLocatorDesig != null && !(("").equals(mgmtBodyLocatorDesig))) {
                    mgmtBody.setMgmtBodyLocatorDesignator(mgmtBodyLocatorDesig);
                }
                if (mgmtBodyLocatorName != null && !(("").equals(mgmtBodyLocatorName))) {
                    mgmtBody.setMgmtBodyLocatorName(mgmtBodyLocatorName);
                }
                if (mgmtBodyPostCode != null && !(("").equals(mgmtBodyPostCode))) {
                    mgmtBody.setMgmtBodyPostCode(mgmtBodyPostCode);
                }
                if (mgmtBodyPostName != null && !(("").equals(mgmtBodyPostName))) {
                    mgmtBody.setMgmtBodyPostName(mgmtBodyPostName);
                }
                if (mgmtBodyThorughfare != null && !(("").equals(mgmtBodyThorughfare))) {
                    mgmtBody.setMgmtBodyThroughFare(mgmtBodyThorughfare);
                }

                mgmtBody.setMgmt(mgmt);
                mgmt.getMgmtBodies().add(mgmtBody);
                session.save(mgmtBody);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
           stmt.close();
           //rs.close();
        }

        return mgmt;


    }

    /**
     *
     * @param mgmtId
     * @param conn
     * @param mgmt
     * @param session
     * @return
     * @throws SQLException
     */
    private Mgmt getMgmtPlanList(int mgmtId, Connection conn, Mgmt mgmt, Session session) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from MGMT_PLAN where mgmt_id =" + mgmtId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                MgmtPlan mgmtPlan = new MgmtPlan();

                String mgmtPlanName = rs.getString("MGMT_PLAN_NAME");
                String mgmtPlanUrl = rs.getString("MGMT_PLAN_URL");

                if (mgmtPlanName != null && !(("").equals(mgmtPlanName))) {
                    mgmtPlan.setMgmtPlanName(mgmtPlanName);
                }
                if (mgmtPlanUrl != null && !(("").equals(mgmtPlanUrl))) {
                    mgmtPlan.setMgmtPlanUrl(mgmtPlanUrl);
                }

                mgmtPlan.setMgmt(mgmt);
                mgmt.getMgmtPlans().add(mgmtPlan);
                session.save(mgmtPlan);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
           // rs.close();
        }
        return mgmt;
    }

    /**
     *
     * @param docId
     * @param conn
     * @param doc
     * @param session
     * @return
     * @throws SQLException
     */
    private Doc getDocucumentLinks(int docId, Connection conn, Doc doc, Session session) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from DOC_LINK where doc_id =" + docId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                DocLink docLink = new DocLink();
                String docLinkUrl = rs.getString("DOC_LINK_URL");

                if (docLinkUrl != null && !(("").equals(docLinkUrl))) {
                    docLink.setDocLinkUrl(docLinkUrl);
                }

                docLink.setDoc(doc);
                doc.getDocLinks().add(docLink);
                session.save(docLink);
            }
        } catch (SQLException e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
            throw e;
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());
            //////e.printStackTrace();
        } finally {
            stmt.close();
           // rs.close();
        }
        return doc;


    }


    /**
     *
     * @param rs
     * @param fieldName
     * @return
     */
    String getString(ResultSet rs, String fieldName) {

         try {
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
         } catch (Exception e) {
             ImporterNewMDB.log.error("Failed extracting field: " + fieldName + ". The field could have an erroneous name. Error: " + e.getMessage());
             log("Failed extracting field: " + fieldName + ". The field could have an erroneous name. Please verify.", 2);
             //////e.printStackTrace();
             return null;
         }
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
            ImporterNewMDB.log.error("Failed extracting field: " + fieldName + ". Error:::" + e.getMessage());
            log("Failed extracting field: " + fieldName, 1);
            return null;
        }
    }



    /**
     *
     * @param regionKey
     * @return
     */
    private int loadBiogeoRegionId(String regionKey) {
        int biogeoId = 0;
         try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();
            String hql = "select bio.biogeoId from Biogeo as bio where bio.biogeoCode='" + regionKey + "'";
            Iterator itr = session.createQuery(hql).iterate();

            while (itr.hasNext()) {
                Integer tuple = (Integer) itr.next();
                biogeoId = tuple.intValue();
            }
            tx.commit();
            session.close();
        } catch (Exception e) {
            ImporterNewMDB.log.error(" Error: " + e.getMessage());

            //////e.printStackTrace();
        }
        return biogeoId;
    }


    /**
     *
     * @param regionCode
     * @return
     */
    private boolean isRegionLevel2(String regionCode) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        boolean nutsOK = false;

        ImporterNewMDB.log.info("Validating Region Code");
        String hql = "select n.REF_NUTS_DESCRIPTION from natura2000.ref_nuts where REF_NUTS_CODE='" + regionCode + "'";

        try {
            Query q = session.createQuery(hql);
            if (q.uniqueResult() != null) {
               nutsOK = true;
            }
        } catch (Exception e) {
            ////e.printStackTrace();
            ImporterNewMDB.log.error("Error loading Region Descrption:::" + e.getMessage());

        }
        return nutsOK;
    }
}
