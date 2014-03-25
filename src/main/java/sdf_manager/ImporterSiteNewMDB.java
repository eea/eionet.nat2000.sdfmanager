package sdf_manager;

import java.awt.Frame;
import pojos.*;

import java.io.FileWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
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
public class ImporterSiteNewMDB implements Importer {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImporterSiteNewMDB.class .getName());

    private String[] biotopFields = {""};
    private Logger logger;
    private String encoding;
    private HashMap speciesByCode = new HashMap();
    private HashMap speciesByName = new HashMap();
    private FileWriter outFile;
    private PrintWriter out;
    private String accessVersion;
    private String siteCode;

    /**
     *
     * @param logger
     * @param encoding
     * @param logFile
     * @param accessVersion
     * @param sitecode
     */
    public ImporterSiteNewMDB(Logger logger, String encoding, String logFile, String accessVersion, String sitecode) {
        this.logger = logger;
        this.encoding = encoding;
        this.accessVersion=accessVersion;
        this.siteCode=sitecode;
        this.initLogFile(logFile);
        this.init();

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
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error("Error::"+e.getMessage());
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
            ImporterSiteNewMDB.log.error("Error::"+e.getMessage());
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
    public boolean processDatabase(String fileName){
        Connection conn = null;
        boolean importOk = false;
        try {

            boolean sitesDB = validateSites();
            ImporterSiteNewMDB.log.info("Validation has finished");
            log("Validation has finished.",1);
            if(sitesDB){
               ImporterSiteNewMDB.log.info("Import process is starting");
               log("Import process is starting.",1);
               importOk =this.processSites(conn,fileName);
            }else{
                importOk = false;
                ImporterSiteNewMDB.log.error("Error in validation");
                ImporterSiteNewMDB.log.error("Some sites are already stored in Data Base. Please check the log file for details");
                log("Error in validation.",1);
                JOptionPane.showMessageDialog(new JFrame(), "Some sites are already stored in Data Base. Please check the log file for details", "Dialog",JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            ImporterSiteNewMDB.log.error("Error validating sites::"+e.getMessage());
            importOk = false;
            return importOk;
        }finally{
            if(importOk){
                javax.swing.JOptionPane.showMessageDialog(new Frame(),"Import Processing has finished succesfully.", "Dialog",JOptionPane.INFORMATION_MESSAGE);;
            }else{
                log("Import is stopped.There are some errors in import process.",1);
                javax.swing.JOptionPane.showMessageDialog(new Frame(),"There are some errors in import process.\n Please, check the SDF_Log file for more details", "Dialog",JOptionPane.ERROR_MESSAGE);;
            }

        }
        return importOk;
    }


     /**
      *
      * @param conn
      */
     private boolean validateSites() {

         Session session = HibernateUtil.getSessionFactory().openSession();

         boolean siteInDB=false;
         try {
               if(!SDF_Util.validateSite(session,this.siteCode)){
                   siteInDB=true;
               }
               session.flush();
               session.clear();
        }catch (Exception e) {
            //e.printStackTrace();
            ImporterSiteNewMDB.log.error("Error: " + e.getMessage());
        }
        finally {
            session.close();
        }
        return siteInDB;


     }
    /**
     *
     * @param fileName
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    Connection getConnection(String fileName) throws ClassNotFoundException, SQLException {
        try{
             if(accessVersion.equals("2003")){
                 /*open read-only*/
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                //añadido , *.accdb) a la cadena db

                String db = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};Dbq=" + fileName + ";";
                Connection conn = DriverManager.getConnection(db, "", "");
                return conn;
             }else{
                /*open read-only*/
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                //añadido , *.accdb) a la cadena db
                String db = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + fileName + ";";
                Connection conn = DriverManager.getConnection(db, "", "");
                return conn;
             }
         }catch(ClassNotFoundException e){
             ImporterSiteNewMDB.log.error("Error conecting to MS Access DB. Error Message:::"+e.getMessage());
             return null;
         }catch(SQLException e){
             ImporterSiteNewMDB.log.error("Error conecting to MS Access DB. Error Message:::"+e.getMessage());
             return null;
         }catch(Exception e){
             ImporterSiteNewMDB.log.error("Error conecting to MS Access DB. Error Message:::"+e.getMessage());
             return null;
         }
    }


    /**
     *
     * @param conn
     * @throws SQLException
     */
    private boolean processSites(Connection conn,String fileName) throws SQLException {
        boolean processOK = false;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            if(conn == null){
                conn = getConnection(fileName);
            }
            if(conn !=  null){
                String sql = "select site_code from site where site_code = '"+this.siteCode+"'";
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    Site site = new Site();

                    ImporterSiteNewMDB.log.info("en processsites");

                    String sitecode = getString(rs,"SITE_CODE");
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
                        ImporterSiteNewMDB.log.error("Failed processing site: " + sitecode+" Error: " + e.getMessage());
                        log("Failed processing site: " + sitecode, 1);
                        processOK = false;
                    }
                }
                ImporterSiteNewMDB.log.info("Finishing import process.Closing connection to Data Base");
                log("Finishing import process.Closing connection to Data Base");
            }else{
                processOK = false;
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" SQL Error: " + e.getMessage());
            processOK = false;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            processOK = false;
        } finally {
            stmt.close();
            if(!conn.isClosed()){
                conn.close();
            }
            session.flush();
            session.clear();
            session.close();
            return processOK;
        }
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
            if (rs.next()) {
                tmpStr = getString(rs,"SITE_NAME");
                if (tmpStr != null) {
                    site.setSiteName(tmpStr);
                }

                log("Processing: Site Type");
                ImporterSiteNewMDB.log.info("Processing: Site Type");
                tmpStr = getString(rs,"SITE_TYPE");
                if (tmpStr != null) {
                    site.setSiteType(tmpStr.charAt(0));
                }

                log("Processing: Compilation Date");
                ImporterSiteNewMDB.log.info("Processing: Compilation Date");
                tmpDate = rs.getDate("SITE_COMP_DATE");
                if (tmpDate != null) {
                    site.setSiteCompDate(tmpDate);
                }

                log("Processing: Update Date");
                ImporterSiteNewMDB.log.info("Processing: Update Date");
                tmpDate = rs.getDate("SITE_UPDATE_DATE");
                if (tmpDate != null) {
                    site.setSiteUpdateDate(tmpDate);
                }

                log("Processing: SCI Proposal Date");
                ImporterSiteNewMDB.log.info("Processing: SCI Proposal Date");
                tmpDate = rs.getDate("SITE_SCI_PROP_DATE");
                if (tmpDate != null) {
                    site.setSiteSciPropDate(tmpDate);
                }

                log("Processing: SCI Confirmed Date");
                ImporterSiteNewMDB.log.info("Processing: SCI Confirmed Date");
                tmpDate = rs.getDate("SITE_SCI_CONF_DATE");
                if (tmpDate != null) {
                    site.setSiteSciConfDate(tmpDate);
                }

                log("Processing: SPA Classified Date");
                ImporterSiteNewMDB.log.info("Processing: SPA Classified Date");
                tmpDate = rs.getDate("SITE_SPA_DATE");
                if (tmpDate != null) {
                    site.setSiteSpaDate(tmpDate);
                }

                log("Processing: SPA Legal Reference");
                ImporterSiteNewMDB.log.info("Processing: SPA Legal Reference");
                tmpStr = getString(rs,"SITE_SPA_LEGAL_REF");
                if (tmpStr != null) {
                    site.setSiteSpaLegalRef(tmpStr);
                }

                log("Processing: SAC Designated Date");
                ImporterSiteNewMDB.log.info("Processing: SAC Designated Date");
                tmpDate = rs.getDate("SITE_SAC_DATE");
                if (tmpDate != null) {
                    site.setSiteSacDate(tmpDate);
                }

                log("Processing: SAC Legal Reference");
                ImporterSiteNewMDB.log.info("Processing: SAC Legal Reference");
                tmpStr = getString(rs,"SITE_SAC_LEGAL_REF");
                if (tmpStr != null) {
                    site.setSiteSacLegalRef(tmpStr);
                }

                log("Processing: Explanations");
                ImporterSiteNewMDB.log.info("Processing: Explanations");
                tmpStr = getString(rs,"SITE_EXPLANATIONS");
                if (tmpStr != null) {
                    site.setSiteExplanations(tmpStr);
                }

                log("Processing: Respondent");
                ImporterSiteNewMDB.log.info("Processing: Respondent");
                tmpInt = rs.getInt("RESP_ID");
                if (tmpInt != 0) {
                    Resp resp = new Resp();
                    session.save(resp);
                    resp = getRespData(tmpInt, conn, resp, session);
                    resp.getSites().add(site);
                    session.save(resp);
                    site.setResp(resp);
                }

                log("Processing: Site Location-Area");
                ImporterSiteNewMDB.log.info("Processing: Site Location-Area");
                tmpDouble = rs.getDouble("SITE_AREA");
                if (tmpDouble != null) {
                    site.setSiteArea(tmpDouble);
                }

                log("Processing: Site Location-Marine Area");
                ImporterSiteNewMDB.log.info("Processing: Site Location-Marine Area");
                tmpDouble = rs.getDouble("SITE_MARINE_AREA");
                if (tmpDouble != null) {
                    site.setSiteMarineArea(tmpDouble);
                }

                log("Processing: Site Location-Length");
                ImporterSiteNewMDB.log.info("Processing: Site Location-Length");
                tmpDouble = rs.getDouble("SITE_LENGTH");
                if (tmpDouble != null) {
                    site.setSiteLength(tmpDouble);
                }

                log("Processing: Site Location-Longitude");
                ImporterSiteNewMDB.log.info("Processing: Site Location-Longitude");
                Double longitude = rs.getDouble("SITE_LONGITUDE");
                if (longitude != 0) {
                    site.setSiteLongitude(longitude);
                }

                log("Processing: Site Location-Latitude");
                ImporterSiteNewMDB.log.info("Processing: Site Location-Latitude");
                Double latitude = rs.getDouble("SITE_LATITUDE");
                if (latitude != null) {
                    site.setSiteLatitude(latitude);
                }

                log("Processing: Designation");
                ImporterSiteNewMDB.log.info("Processing: Designation");
                tmpStr = getString(rs,"SITE_DESIGNATION");
                if (tmpStr != null) {
                    site.setSiteDesignation(tmpStr);
                }

                log("Processing: Quality");
                ImporterSiteNewMDB.log.info("Processing: Quality");
                tmpStr = getString(rs,"SITE_QUALITY");
                if (tmpStr != null) {
                    site.setSiteQuality(tmpStr);
                }

                log("Processing: Other Site Characteristics");
                ImporterSiteNewMDB.log.info("Processing: Other Site Characteristics");
                tmpStr = getString(rs,"SITE_CHARACTERISTICS");
                if (tmpStr != null) {
                    site.setSiteCharacteristics(tmpStr);
                }

                log("Processing: Documentation");
                ImporterSiteNewMDB.log.info("Processing: Documentation");
                tmpInt = rs.getInt("DOC_ID");
                if (tmpInt != 0) {
                    Doc doc = new Doc();
                    //doc.setDocId(tmpInt);
                    session.save(doc);
                    doc = getDocData(tmpInt, conn, doc, session);
                    log("      Processing: Documentation-Links");
                    ImporterSiteNewMDB.log.info("Processing: Documentation-Links");
                    doc = getDocucumentLinks(tmpInt, conn, doc, session);
                    doc.getSites().add(site);
                    session.save(doc);
                    site.setDoc(doc);
                }

                log("Processing: Site Management");
                ImporterSiteNewMDB.log.info("Processing: Site Management");
                tmpInt = rs.getInt("MGMT_ID");
                if (tmpInt != 0) {
                    Mgmt mgmt = new Mgmt();
                    session.save(mgmt);
                    mgmt = getMgmtData(tmpInt, conn, mgmt, session);
                    log("      Processing: Site Management-Management Body");
                    ImporterSiteNewMDB.log.info("Processing: Site Management-Management Body");
                    mgmt = getMgmtBodyList(tmpInt, conn, mgmt, session);
                    log("      Processing: Site Management-Management Plan");
                    ImporterSiteNewMDB.log.info("Processing: Site Management-Management Plan");
                    mgmt = getMgmtPlanList(tmpInt, conn, mgmt, session);
                    mgmt.getSites().add(site);
                    session.save(mgmt);
                    site.setMgmt(mgmt);
                }
                log("Processing: Map");
                ImporterSiteNewMDB.log.info("Processing: Map");
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

                log("Processing: BioRegions");
                ImporterSiteNewMDB.log.info("Processing: BioRegions");
                Set itSet = loadSiteBiogeoBySite(conn, session,site);
                if(itSet !=null){
                    Iterator itReg = itSet.iterator();
                    while (itReg.hasNext()) {
                        SiteBiogeo regionKey = (SiteBiogeo) itReg.next();
                        site.getSiteBiogeos().add(regionKey);
                    }
                }

            }
            stmt.close();
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
            stmt.close();
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
    private Set loadSiteBiogeoBySite(Connection conn, Session session,Site site) throws SQLException{
        Set siteBiogeoSet=new HashSet(0);
        Statement stmt = null;
        ResultSet rs = null;
        try{
            String sql = "select * from SITE_BIOGEO where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int biogeId = rs.getInt("BIOGEO_ID");
                double percent = rs.getDouble("BIOGEO_PERCENT");
                Biogeo biogeo = (Biogeo) session.load(Biogeo.class, biogeId);
                SiteBiogeoId id = new SiteBiogeoId(site.getSiteCode(), biogeo.getBiogeoId());
                SiteBiogeo siteBiogeo = new SiteBiogeo(id, biogeo,site);
                siteBiogeo.setBiogeoPercent(percent);
                site.getSiteBiogeos().add(siteBiogeo);
                siteBiogeoSet.add(siteBiogeo);
            }
        }catch(Exception e){
            ImporterSiteNewMDB.log.error("Error loading site biogeo data ::: Error Message:::"+e.getMessage());
        }finally{
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
            log("Processing: Species");
            ImporterSiteNewMDB.log.info("Processing: Species");
            while (rs.next()) {
                Species species = new Species();
                tmpStr = getString(rs,"SPECIES_GROUP");
                if (tmpStr != null) {
                    species.setSpeciesGroup(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"species_code");
                if (tmpStr != null) {
                    species.setSpeciesCode(tmpStr);
                }

                tmpStr = getString(rs,"species_name");
                if (tmpStr != null) {
                    species.setSpeciesName(tmpStr);
                    spName = tmpStr;
                }

                log("      Processing: Species Code::"+species.getSpeciesCode()+":: Species Name:::"+species.getSpeciesName());
                ImporterSiteNewMDB.log.info("Processing: Species Code::"+species.getSpeciesCode()+":: Species Name:::"+species.getSpeciesName());
                tmpShort = rs.getShort("SPECIES_SENSITIVE");
                if (tmpShort != 0) {
                    species.setSpeciesSensitive(tmpShort);
                }

                tmpShort = rs.getShort("SPECIES_NP");
                if (tmpShort != 0) {
                    species.setSpeciesNp(tmpShort);
                }

                tmpStr = getString(rs,"SPECIES_TYPE");
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

                tmpStr = getString(rs,"SPECIES_UNIT");
                if (tmpStr != null) {
                    species.setSpeciesUnit(tmpStr);
                }

                tmpStr = getString(rs,"SPECIES_CATEGORY");
                if (tmpStr != null) {
                    species.setSpeciesCategory(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"SPECIES_DATA_QUALITY");
                if (tmpStr != null) {
                    species.setSpeciesDataQuality(tmpStr);
                }

                tmpStr = getString(rs,"species_population");
                if (tmpStr != null) {
                    species.setSpeciesPopulation(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"species_conservation");
                if (tmpStr != null) {
                    species.setSpeciesConservation(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"species_isolation");
                if (tmpStr != null) {
                    species.setSpeciesIsolation(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"species_global");
                if (tmpStr != null) {
                    species.setSpeciesGlobal(tmpStr.charAt(0));
                }
                species.setSite(site);
                site.getSpecieses().add(species);
            }

        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
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
    private void processOtherSpecies(Connection conn, Session session, Site site) throws SQLException {

        Statement stmt = null;
        ResultSet rs = null;
        try {

            String sql = "select * from OTHER_SPECIES where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            String spName = "";
            Short tmpShort;
            int tmpInt;

            log("Processing: Other Species");
            ImporterSiteNewMDB.log.info("Processing: Other Species");
            while (rs.next()) {
                OtherSpecies otherSpecies = new OtherSpecies();

                tmpStr = getString(rs,"OTHER_SPECIES_GROUP");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesGroup(tmpStr);
                }

                tmpStr = getString(rs,"OTHER_SPECIES_CODE");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesCode(tmpStr);
                }

                tmpStr = getString(rs,"OTHER_SPECIES_NAME");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesName(tmpStr);
                    spName = getString(rs,"OTHER_SPECIES_NAME");
                }
                log("      Processing: Species Code::"+otherSpecies.getOtherSpeciesCode()+":: Species Name:::"+otherSpecies.getOtherSpeciesName());
                ImporterSiteNewMDB.log.info("Processing: Species Code::"+otherSpecies.getOtherSpeciesCode()+":: Species Name:::"+otherSpecies.getOtherSpeciesName());

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

                tmpStr = getString(rs,"OTHER_SPECIES_UNIT");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesUnit(tmpStr);
                }

                tmpStr = getString(rs,"OTHER_SPECIES_CATEGORY");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesCategory(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"OTHER_SPECIES_MOTIVATION");
                if (tmpStr != null) {
                    otherSpecies.setOtherSpeciesMotivation(tmpStr);
                }
                otherSpecies.setSite(site);
                site.getOtherSpecieses().add(otherSpecies);
            }

        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
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
            String sql = "select * from HABITAT where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;
            Short tmpShort;
            int tmpInt;
            log("Processing: Habitats");
            ImporterSiteNewMDB.log.info("Processing: Habitats");
            while (rs.next()) {
                Habitat habitat = new Habitat();

                tmpStr = getString(rs,"HABITAT_CODE");
                if (tmpStr != null) {
                    habitat.setHabitatCode(tmpStr);
                }
                log("      Processing: Habitat:::"+habitat.getHabitatCode());
                ImporterSiteNewMDB.log.info("Processing: Habitat:::"+habitat.getHabitatCode());

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
                tmpStr = getString(rs,"HABITAT_DATA_QUALITY");
                if (tmpStr != null) {
                    habitat.setHabitatDataQuality(tmpStr);
                }

                tmpStr = getString(rs,"HABITAT_REPRESENTATIVITY");
                if (tmpStr != null) {
                    habitat.setHabitatRepresentativity(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"HABITAT_RELATIVE_SURFACE");
                if (tmpStr != null) {
                    habitat.setHabitatRelativeSurface(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"HABITAT_CONSERVATION");
                if (tmpStr != null) {
                    habitat.setHabitatConservation(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"HABITAT_GLOBAL");
                if (tmpStr != null) {
                    habitat.setHabitatGlobal(tmpStr.charAt(0));
                }

                habitat.setSite(site);
                site.getHabitats().add(habitat);
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
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
            String sql = "select * from HABITAT_CLASS where site_code ='" + site.getSiteCode() + "'";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Double tmpDouble;
            log("Processing: Habitat Class");
            ImporterSiteNewMDB.log.info("Processing: Habitat Class");
            while (rs.next()) {
                HabitatClass habitat = new HabitatClass();

                tmpStr = getString(rs,"HABITAT_CLASS_CODE");
                if (tmpStr != null) {
                    habitat.setHabitatClassCode(tmpStr);
                }

                log("      Processing: Habitat Class:::"+habitat.getHabitatClassCode());
                ImporterSiteNewMDB.log.info("Processing: Habitat Class:::"+habitat.getHabitatClassCode());

                tmpStr = getString(rs,"HABITAT_CLASS_DESCRIPTION");
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
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
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
    private void processRegions(Connection conn, Session session, Site site) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from REGION where site_code ='" + site.getSiteCode() + "'";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            log("Processing: Regions");
            ImporterSiteNewMDB.log.info("Processing: Regions");
            while (rs.next()) {
                Region region = new Region();

                tmpStr = getString(rs,"region_code");
                log("      Processing: Region:::"+tmpStr);
                ImporterSiteNewMDB.log.info("Processing: Region:::"+tmpStr);
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
                                if(rn.getRefNutsDescription() != null){
                                    region.setRegionName(rn.getRefNutsDescription());
                                }else{
                                    region.setRegionName(rn.getRefNutsDescription());
                                    log(String.format("\tCouldn't match NUTS code (%s). Encoding anyway.", tmpStr), 2);
                                }


                            } else {
                                log(String.format("\tCouldn't match NUTS code (%s). Encoding anyway.", tmpStr), 2);
                            }
                        } catch (Exception e) {
                            ImporterSiteNewMDB.log.error("An error has occurred in import process. Region section::Error Message:::"+e.getMessage());
                        }
                    }
                    region.setRegionCode(tmpStr);
                }
                region.setSite(site);
                site.getRegions().add(region);
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
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
            String sql = "select * from SITE_RELATION where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;
            log("Processing: Relations");
            ImporterSiteNewMDB.log.info("Processing: Relations");
            while (rs.next()) {
                SiteRelation relation = new SiteRelation();

                tmpStr = getString(rs,"site_relation_code");
                if (tmpStr != null) {
                    relation.setSiteRelationCode(tmpStr);
                    log("      Processing: Relation:::"+relation.getSiteRelationCode());
                    ImporterSiteNewMDB.log.info("Processing: Relation:::"+relation.getSiteRelationCode());
                   // if (tmpStr.toUpperCase().startsWith("IN")) {
                   //     relation.setSiteRelationScope('I');
                   // } else {
                   //     relation.setSiteRelationScope('N');
                   // }
                }
                
                tmpStr = getString(rs,"site_relation_scope");
                if (tmpStr!=null && tmpStr.toUpperCase().startsWith("I")) {
                	 relation.setSiteRelationScope('I');
                } else {
                    relation.setSiteRelationScope('N');
                }
                
                
                tmpStr = getString(rs,"site_relation_sitename");
                if (tmpStr != null) {
                    relation.setSiteRelationSitename(tmpStr);
                }

                tmpStr = getString(rs,"site_relation_type");
                if (tmpStr != null) {
                    relation.setSiteRelationType(tmpStr.charAt(0));
                }
                tmpStr = getString(rs,"SITE_RELATION_CONVENTION");
                if (tmpStr != null) {
                    relation.setSiteRelationConvention(tmpStr);
                }

                tmpDouble = rs.getDouble("site_relation_cover");
                if (tmpDouble != 0) {
                    relation.setSiteRelationCover(tmpDouble);
                }
                relation.setSite(site);
                site.getSiteRelations().add(relation);
                session.save(relation);
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
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
            String sql = "select * from NATIONAL_DTYPE where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;
            log("Processing: National Designation Type");
            ImporterSiteNewMDB.log.info("Processing: National Designation Type");
            while (rs.next()) {
                NationalDtype dType = new NationalDtype();

                tmpStr = getString(rs,"NATIONAL_DTYPE_CODE");
                if (tmpStr != null) {
                    dType.setNationalDtypeCode(tmpStr);
                }

                log("      Processing: National Designation Type:::"+dType.getNationalDtypeCode());
                ImporterSiteNewMDB.log.info("Processing: National Designation Type:::"+dType.getNationalDtypeCode());

                tmpDouble = rs.getDouble("NATIONAL_DTYPE_COVER");
                if (tmpDouble != 0) {
                    dType.setNationalDtypeCover(tmpDouble);
                }
                dType.setSite(site);
                site.getNationalDtypes().add(dType);
                session.save(dType);
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
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
            String sql = "select * from IMPACT where site_code ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String tmpStr;
            Character tmpChar;
            Double tmpDouble;
            log("Processing: Impacts");
            ImporterSiteNewMDB.log.info("Processing: Impact");
            while (rs.next()) {
                Impact impact = new Impact();

                tmpStr = getString(rs,"IMPACT_TYPE");
                if (tmpStr != null) {
                    impact.setImpactType(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"IMPACT_RANK");
                if (tmpStr != null) {
                    impact.setImpactRank(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"IMPACT_CODE");
                if (tmpStr != null) {
                    impact.setImpactCode(tmpStr);
                }

                log("      Processing: Impact:::"+impact.getImpactCode());
                ImporterSiteNewMDB.log.info("Processing: Impact:::"+impact.getImpactCode());

                tmpStr = getString(rs,"IMPACT_POLLUTION_CODE");
                if (tmpStr != null) {
                    impact.setImpactPollutionCode(tmpStr.charAt(0));
                }

                tmpStr = getString(rs,"IMPACT_OCCURRENCE");
                if (tmpStr != null) {
                    impact.setImpactOccurrence(tmpStr.charAt(0));
                }
                impact.setSite(site);
                site.getImpacts().add(impact);
                session.save(impact);
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
            stmt.close();
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
            log("Processing: OwnerShips");
            ImporterSiteNewMDB.log.info("Processing: OwnerShips");
            while (rs.next()) {
                SiteOwnership siteOwnerShip = new SiteOwnership();

                tmpDouble = rs.getDouble("OWNERSHIP_PERCENT");
                if (tmpDouble != 0) {
                    siteOwnerShip.setOwnershipPercent(tmpDouble);
                }

                tmpInt = rs.getInt("OWNERSHIP_ID");
                if (tmpInt != null) {
                    SiteOwnershipId siteOwnerShipId = new SiteOwnershipId(tmpInt,site.getSiteCode());
                    siteOwnerShip.setId(siteOwnerShipId);
                }

                siteOwnerShip.setSite(site);
                site.getSiteOwnerships().add(siteOwnerShip);
                session.save(siteOwnerShip);
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
            stmt.close();
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
                if(description != null){
                    doc.setDocDescription(description);
                }
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
           stmt.close();
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
                log("      Processing: Respondent-Name");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Name");
                String respName = rs.getString("RESP_NAME");
                log("      Processing: Respondent-Name");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Name");
                String respAddress = rs.getString("RESP_ADDRESS");
                log("      Processing: Respondent-Email");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Email");
                String respEmail = rs.getString("RESP_EMAIL");
                log("      Processing: Respondent-Admin Unit");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Admin Unit");
                String respAdminUnit = rs.getString("RESP_ADMINUNIT");
                log("      Processing: Respondent-Locator Designator");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Locator Designator");
                String respLocatorDesig = rs.getString("RESP_LOCATORDESIGNATOR");
                log("      Processing: Respondent-Locator Name");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Locator Name");
                String respLocatorName = rs.getString("RESP_LOCATORNAME");
                log("      Processing: Respondent-Address Area");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Address Area");
                String respAddressArea = rs.getString("RESP_ADDRESSAREA");
                log("      Processing: Respondent-Post Name");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Post Name");
                String respPostName = rs.getString("RESP_POSTNAME");
                log("      Processing: Respondent-Post Code");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Post Code");
                String respPostCode = rs.getString("RESP_POSTCODE");
                log("      Processing: Respondent-Thoroughfare");
                ImporterSiteNewMDB.log.info("Processing: Respondent-Thoroughfare");
                String respThoughfare = rs.getString("RESP_THOROUGHFARE");


                if(respName != null && !(("").equals(respName))){
                    resp.setRespName(respName);
                }
                if(respAddress != null && !(("").equals(respAddress))){
                    resp.setRespAddress(respAddress);
                }
                if(respEmail != null && !(("").equals(respEmail))){
                    resp.setRespEmail(respEmail);
                }
                if(respAdminUnit != null && !(("").equals(respAdminUnit))){
                    resp.setRespAdminUnit(respAdminUnit);
                }
                if(respAddressArea != null && !(("").equals(respAddressArea))){
                    resp.setRespAddressArea(respAddressArea);
                }
                if(respLocatorDesig != null && !(("").equals(respLocatorDesig))){
                    resp.setRespLocatorDesig(respLocatorDesig);
                }
                if(respLocatorName != null && !(("").equals(respLocatorName))){
                    resp.setRespLocatorName(respLocatorName);
                }
                if(respPostCode != null && !(("").equals(respPostCode))){
                    resp.setRespPostCode(respPostCode);
                }
                if(respPostName != null && !(("").equals(respPostName))){
                    resp.setRespPostName(respPostName);
                }
                if(respThoughfare != null && !(("").equals(respThoughfare))){
                    resp.setRespThoroughFare(respThoughfare);
                }
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
           stmt.close();
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

                if(mapInspired != null){
                    mapSite.setMapInspire(mapInspired);
                }
                if(mapPDF != 0){
                    mapSite.setMapPdf(mapPDF);
                }else{
                    mapSite.setMapPdf(mapPDF);
                }
                if(mapRef != null){
                    mapSite.setMapReference(mapRef);
                }

            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
           stmt.close();
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
                if(mgmtStatus != null && !(("").equals(mgmtStatus))){
                    mgmt.setMgmtStatus(mgmtStatus.charAt(0));
                }
                if(mgmtConsMeasures != null && !(("").equals(mgmtConsMeasures))){
                    mgmt.setMgmtConservMeasures(mgmtConsMeasures);
                }
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
           stmt.close();
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


                if(mgmtBodyOrg != null && !(("").equals(mgmtBodyOrg))){
                    mgmtBody.setMgmtBodyOrg(mgmtBodyOrg);
                }
                if(mgmtBodyAddress != null && !(("").equals(mgmtBodyAddress))){
                    mgmtBody.setMgmtBodyAddress(mgmtBodyAddress);
                }
                if(mgmtBodyEmail != null && !(("").equals(mgmtBodyEmail))){
                    mgmtBody.setMgmtBodyEmail(mgmtBodyEmail);
                }
                if(mgmtBodyAdminUnit != null && !(("").equals(mgmtBodyAdminUnit))){
                    mgmtBody.setMgmtBodyAdminUnit(mgmtBodyAdminUnit);
                }
                if(mgmtBodyAddressArea != null && !(("").equals(mgmtBodyAddressArea))){
                    mgmtBody.setMgmtBodyAddressArea(mgmtBodyAddressArea);
                }
                if(mgmtBodyLocatorDesig != null && !(("").equals(mgmtBodyLocatorDesig))){
                    mgmtBody.setMgmtBodyLocatorDesignator(mgmtBodyLocatorDesig);
                }
                if(mgmtBodyLocatorName != null && !(("").equals(mgmtBodyLocatorName))){
                    mgmtBody.setMgmtBodyLocatorName(mgmtBodyLocatorName);
                }
                if(mgmtBodyPostCode != null && !(("").equals(mgmtBodyPostCode))){
                    mgmtBody.setMgmtBodyPostCode(mgmtBodyPostCode);
                }
                if(mgmtBodyPostName != null && !(("").equals(mgmtBodyPostName))){
                    mgmtBody.setMgmtBodyPostName(mgmtBodyPostName);
                }
                if(mgmtBodyThorughfare != null && !(("").equals(mgmtBodyThorughfare))){
                    mgmtBody.setMgmtBodyThroughFare(mgmtBodyThorughfare);
                }

                mgmtBody.setMgmt(mgmt);
                mgmt.getMgmtBodies().add(mgmtBody);
                session.save(mgmtBody);
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
           stmt.close();
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
    private Mgmt getMgmtPlanList(int mgmtId, Connection conn, Mgmt mgmt, Session session)throws SQLException {
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

                if(mgmtPlanName != null && !(("").equals(mgmtPlanName))){
                    mgmtPlan.setMgmtPlanName(mgmtPlanName);
                }
                if(mgmtPlanUrl != null && !(("").equals(mgmtPlanUrl))){
                    mgmtPlan.setMgmtPlanUrl(mgmtPlanUrl);
                }

                mgmtPlan.setMgmt(mgmt);
                mgmt.getMgmtPlans().add(mgmtPlan);
                session.save(mgmtPlan);
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
            stmt.close();
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
    private Doc getDocucumentLinks(int docId, Connection conn, Doc doc, Session session)throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from DOC_LINK where doc_id =" + docId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                DocLink docLink = new DocLink();
                String docLinkUrl = rs.getString("DOC_LINK_URL");

                if(docLinkUrl != null && !(("").equals(docLinkUrl))){
                    docLink.setDocLinkUrl(docLinkUrl);
                }

                docLink.setDoc(doc);
                doc.getDocLinks().add(docLink);
                session.save(docLink);
            }
        } catch (SQLException e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error(" Error: " + e.getMessage());
        } finally {
            stmt.close();
        }
        return doc;
    }


    /**
     *
     * @param rs
     * @param fieldName
     * @return
     */
    String getString(ResultSet rs,String fieldName) {

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
                 }
                 else{
                     return null;
                 }
             }
         }
         catch (Exception e) {
             ImporterSiteNewMDB.log.error("Failed extracting field: " + fieldName + ". The field could have an erroneous name. Error: " + e.getMessage());
             log("Failed extracting field: " + fieldName + ". The field could have an erroneous name. Please verify.",2);
             return null;
         }
     }


    /**
     *
     * @param regionCode
     * @return
     */
    private boolean isRegionLevel2( String regionCode){
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        boolean nutsOK = false;

        ImporterSiteNewMDB.log.info("Validating Region Code");
        String hql="select n.REF_NUTS_DESCRIPTION from natura2000.ref_nuts where REF_NUTS_CODE='"+regionCode+"'";

        try {
            Query q = session.createQuery(hql);
            if(q.uniqueResult() != null){
               nutsOK = true;
            }
        } catch (Exception e) {
            ImporterSiteNewMDB.log.error("Error loading Region Description:::"+e.getMessage());

        }
        return nutsOK;
    }
}
