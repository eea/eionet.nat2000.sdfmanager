/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
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
import org.hibernate.cfg.Configuration;
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
import sdf_manager.util.ImporterUtils;
import sdf_manager.util.SDF_MysqlDatabase;
import sdf_manager.util.SDF_Util;

/**
 * Importer for old MS Access file and EMERALD files.
 *
 * @author charbda
 */
public class ImporterMDB implements Importer {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImporterMDB.class.getName());

    /**
     * table file for N2K.
     */
    private static final String TABLE_FILE_NAME_N2K = "table_names.xml";

    /**
     * table file for EMERALD.
     */
    private static final String TABLE_FILE_NAME_EMERALD = "table_names_emerald.xml";

    /**
     * field mappings file for EMERALD.
     */
    private static final String FIELD_FILE_NAME_EMERALD = "field_maps_emerald.xml";

    /**
     * field mappings file for N2K.
     */
    private static final String FIELD_FILE_NAME_N2K = "field_maps.xml";

    /**
     * full path of table file. Default: n2k.
     */
    private String tableFile = "config" + System.getProperty("file.separator") + TABLE_FILE_NAME_N2K;

    /**
     * full path of field file. Default: n2k.
     */
    private String fieldFile = "config" + System.getProperty("file.separator") + FIELD_FILE_NAME_N2K;

    private String table_element = "table";
    private String field_element = "field";
    private HashMap<String, String> tables;
    private HashMap<String, String> fields;
    private String[] tableKeys = {"name", "used_name"};
    private String[] fieldKeys = {"reference", "oldname"};
    private Logger logger;
    private String encoding;
    private HashMap speciesByCode = new HashMap();
    private HashMap speciesByName = new HashMap();
    private FileWriter outFile;
    private PrintWriter out;
    private String importDate;
    private ArrayList sitesDB = new ArrayList();
    private HashMap nutsKO = new HashMap();
    private String accessVersion;

    /**
     *
     * @param logger
     * @param encoding
     * @param logFile
     * @param accessVersion
     */
    public ImporterMDB(Logger logger, String encoding, String logFile, String accessVersion) {
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
        this.tables = new HashMap<String, String>();
        this.fields = new HashMap<String, String>();
        this.parse(this.tableFile, this.tables, this.table_element, this.tableKeys);
        this.parse(this.fieldFile, this.fields, this.field_element, this.fieldKeys);
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
    @Override
    public void initLogFile(String fileName) {
        try {
            outFile = new FileWriter(fileName);
            out = new PrintWriter(outFile);
        } catch (Exception e) {
            ImporterMDB.log.error("ERROR::" + e.getMessage());
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
            ImporterMDB.log.error("ERROR::" + e.getMessage());
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
        try {
            conn = getConnection(fileName);

            if (conn != null) {

                if (!checkTables(conn)) {
                    log("Failed to find all tables in the database. Please check the database schema and configuration files.", 1);
                    ImporterMDB.log
                            .error("Failed to find all tables in the database. Please check the database schema and configuration files");
                    msgValidError =
                            "Failed to find all tables in the database. Please check the database schema and configuration files";
                    saveOK = false;
                    return false;
                }
                this.importDate = getImportDate();
                // this.sitesDB = validateSites(conn);

                saveOK = validateAndProcessSites(conn);

            } else {
                saveOK = false;
                msgValidError = "A DB error occurs. Please check the SDF_log file for more details";
            }
        } catch (Exception e) {
            ImporterMDB.log.error("ERROR in processDatabase::" + e.getMessage());
            // e.printStackTrace();
            saveOK = false;
        } finally {
            SDF_MysqlDatabase.closeQuietly(conn);
            if (saveOK) {
                JOptionPane.showMessageDialog(new JFrame(), "Import Processing has finished succesfully.", "Dialog",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                String msgError = "There are some errors in import process";
                JOptionPane.showMessageDialog(new JFrame(), "There are some errors in import process.\n" + msgValidError,
                        "Dialog", JOptionPane.INFORMATION_MESSAGE);
            }
            return saveOK;
        }
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
            // java.util.Properties prop = new java.util.Properties();
            // prop.put("charSet", "UTF-8");

            if (accessVersion.equals("2003")) {
                /* open read-only */
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                String db =
                        "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};Dbq=" + fileName
                                + ";useUnicode=true;characterEncoding=UTF-8";
                // String db = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};Dbq=" + fileName + ";";
                conn = DriverManager.getConnection(db, "", "");

            } else {
                /* open read-only */
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                // añadido , *.accdb) a la cadena db
                // String db = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + fileName + ";";
                String db =
                        "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + fileName
                                + ";useUnicode=true;characterEncoding=UTF-8";
                conn = DriverManager.getConnection(db, "", "");
            }

        } catch (ClassNotFoundException e) {
            ImporterMDB.log.error("Error conecting to MS Access DB. Error Message:::" + e.getMessage());
            // e.printStackTrace();
        } catch (SQLException e) {
            ImporterMDB.log.error("Error conecting to MS Access DB. Error Message:::" + e.getMessage());
            // e.printStackTrace();
        } catch (Exception e) {
            ImporterMDB.log.error("Error conecting to MS Access DB. Error Message:::" + e.getMessage());
            // e.printStackTrace();
        } finally {
            return conn;
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
                ResultSet rs = dbm.getTables(null, null, tmpStr, null);
                if (!rs.next()) {
                    ImporterMDB.log.error("Could not find table: " + tmpStr);
                    log("Could not find table: " + tmpStr, 1);
                    return false;
                }
            }
        } catch (Exception e) {
            ImporterMDB.log.error("Failed processing tables: " + e.getMessage());
            // e.printStackTrace();
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
                    this.speciesByCode.put(code, new Object[] {name, group});
                }
                if (name != null) {
                    this.speciesByName.put(name, new Object[] {code, group});
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
                    this.speciesByCode.put(code, new Object[] {name, group});
                }
                if (name != null) {
                    this.speciesByName.put(name, new Object[] {code, group});
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ImporterMDB.log.error("Failed loading species: " + e.getMessage());
        }
    }

    /**
     * New method for validating and process only the ones that doesn't exist
     */
    boolean validateAndProcessSites(Connection conn) throws SQLException {

        ArrayList<String> notProcessedSiteCodesList = new ArrayList<String>();
        boolean processOK = false;

        String sql = "select sitecode from " + this.tables.get("biotop") + " order by sitecode";
        Session session = null;
        Statement stmt = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            loadSpecies(conn, session);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // int i = 0;

            while (rs.next()) {

                Site site = new Site();
                String sitecode = rs.getString(this.fields.get("sitecode"));

                // Validate each site
                try {

                    ImporterMDB.log.info("Validating site: " + sitecode);
                    log("Validating site: " + sitecode, 1);

                    if (SDF_Util.validateSite(session, sitecode)) {
                        notProcessedSiteCodesList.add(sitecode);
                    } else {
                        log("Processing: " + sitecode, 1);
                        ImporterMDB.log.info("Processing: " + sitecode);

                        // BIOTOP
                        Transaction tx = session.beginTransaction();
                        site.setSiteCode(sitecode);
                        processBiotop(conn, session, site);
                        tx.commit();
                        session.flush();

                        // HABITATS
                        tx = session.beginTransaction();
                        processHabitats(conn, session, site);
                        tx.commit();
                        session.flush();

                        // HABITAT CLASSES
                        tx = session.beginTransaction();
                        processHabitatClasses(conn, session, site);
                        tx.commit();
                        session.flush();

                        // REGIONS
                        tx = session.beginTransaction();
                        processRegions(conn, session, site);
                        tx.commit();
                        session.flush();

                        // RELATIONS
                        tx = session.beginTransaction();
                        processRelations(conn, session, site);
                        tx.commit();
                        session.flush();

                        // DTYPES
                        tx = session.beginTransaction();
                        processDTypes(conn, session, site);
                        tx.commit();
                        session.flush();

                        // IMPACTS
                        tx = session.beginTransaction();
                        processImpacts(conn, session, site);
                        tx.commit();
                        session.flush();

                        // SPECIES
                        processSpecies(conn, session, site);

                        processOK = true;
                        session.flush();
                        session.evict(site);

                    }

                } catch (Exception e) {
                    ImporterMDB.log.error("Failed processing site: " + sitecode + ". Error:::" + e.getMessage());
                    log("Failed processing site: " + sitecode, 1);
                    break;
                }

            }

            ImporterMDB.log.info("Finishing import process.Closing connection to Data Base");
            log("Finishing import process.Closing connection to Data Base");

        } catch (Exception e) {

            ImporterMDB.log.error("The error: " + e.getMessage());
            // e.printStackTrace();
            processOK = false;
            return false;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            session.clear();
            session.close();
        }

        /**
         * If a sitecode already exists, show log file with the conflictive sitecodes
         */
        if (notProcessedSiteCodesList != null && !notProcessedSiteCodesList.isEmpty()) {

            ImporterMDB.log
                    .error("Error in validation:. Error Message: Some sites are already stored in Data Base. Please check the log file for details");
            log("Error in validation.", 1);
            // msgValidError = "Some sites are already stored in Data Base. Please check the log file for details";

            File fileLog = SDF_Util.copyToLogImportFileList(notProcessedSiteCodesList, "OldDB");
            if (fileLog != null) {
                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                    try {
                        Desktop.getDesktop().open(fileLog);
                    } catch (Exception ex) {
                        ImporterMDB.log.error("The error: " + ex.getMessage());
                    }
                }

            }
            // return false;
        }

        return processOK;
    }

    /**
     *
     * @param conn
     */
    private ArrayList<String> validateSites(Connection conn) throws SQLException {

        String sql = "select sitecode from " + this.tables.get("biotop");
        Session session =
                new Configuration().configure().setProperty("hibernate.jdbc.batch_size", "20")
                        .setProperty("hibernate.cache.use_second_level_cache", "false").buildSessionFactory().openSession();
        ArrayList<String> siteCodeDB = new ArrayList<String>();
        Statement stmt = null;

        try {
            loadSpecies(conn, session);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            int i = 0;

            while (rs.next()) {

                Site site = new Site();
                String sitecode = rs.getString(this.fields.get("sitecode"));

                try {
                    Transaction tx = session.beginTransaction();
                    ImporterMDB.log.info("Validating site: " + sitecode);
                    log("Validating site: " + sitecode, 1);

                    if (SDF_Util.validateSite(session, sitecode)) {
                        siteCodeDB.add(sitecode);
                    }
                    tx.commit();

                } catch (Exception e) {
                    ImporterMDB.log.error("Failed processing site: " + sitecode + ". Error:::" + e.getMessage());
                    log("Failed processing site: " + sitecode, 1);
                    break;
                }

                if (++i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ImporterMDB.log.error("Error: " + e.getMessage());
            return siteCodeDB;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            session.close();
        }
        return siteCodeDB;
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
            // e.printStackTrace();
            ImporterMDB.log.error("Failed extracting field: " + fieldName + ". Error:::" + e.getMessage());
            log("Failed extracting field: " + fieldName, 1);
            return null;
        }
    }

    /**
     *
     * @param rs
     * @param fieldName
     * @return
     */
    private String getString(ResultSet rs, String fieldName) {
        try {
            if (!("UTF-8").equals(this.encoding)) {
                byte[] result = rs.getBytes(fieldName);
                if (result != null && result.length == 0) {
                    return null;
                } // don't enter empty string in the database
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

                String tmp = rs.getString(fieldName);
                if (fieldName == this.fields.get("sitename")) {
                    PrintStream sysout = new PrintStream(System.out, true, "UTF-8");
                    sysout.print(">>" + tmp);
                }
                return tmp;

            }

        } catch (Exception e) {
            e.printStackTrace();
            log("Failed extracting field: " + fieldName + ". The field could have an erroneous name. Please verify.", 2);
            ImporterMDB.log.error("Failed extracting field: " + fieldName + ".The field could have an erroneous name.Error:::"
                    + e.getMessage());
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
            // ImporterMDB.log.error("Failed extracting field: " + fieldName + ".The field could have an erroneous name.Error:::" +
            // e.getMessage());
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
        // c = Character.toUpperCase(c);
        if (strC.equals("C")) {
            return 'C';
        } else if (strC.equals("A") || strC.equals("D") || strC.equals("F") || strC.equals("H") || strC.equals("J")) {
            // else if (c.equals('A') || c.equals('D') || c.equals('F') || c.equals('H') || c.equals('J')) {

            return 'A';
        } else if (strC.equals("B") || strC.equals("E") || strC.equals("G") || strC.equals("I") || strC.equals("K")) {
            // else if (c.equals('B') || c.equals('E') || c.equals('G') || c.equals('I') || c.equals('K')) {
            return 'B';
        } else {
            return Character.toUpperCase(c);
        }
    }

    /**
     *
     * @param conn
     * @param session
     * @param sitecode
     * @return
     */
    boolean validateSite(Connection conn, Session session, String sitecode) {
        String hql = " from Site where siteCode='" + sitecode + "'";
        boolean isOK = false;
        try {
            Query q = session.createQuery(hql);
            Iterator itr = q.iterate();
            if (itr.hasNext()) {
                isOK = true;
            }

        } catch (Exception e) {
            // e.printStackTrace();
            ImporterMDB.log.error("Error:::" + e.getMessage());
        } finally {
            return isOK;
        }

    }

    /**
     *
     * @param conn
     * @param session
     * @param site
     */
    void processBiotop(Connection conn, Session session, Site site) {

        Statement stmt = null;

        try {
            String sql = "select * from " + this.tables.get("biotop") + " where sitecode ='" + site.getSiteCode() + "'";
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            String tmpStr;
            Double tmpDouble;
            Date tmpDate;
            Character tmpChar;

            while (rs.next()) {
                tmpStr = getString(rs, this.fields.get("sitename"));

                ImporterMDB.log.info("tmpStr==>" + tmpStr + "<==");
                if (tmpStr != null) {
                    site.setSiteName(tmpStr);
                }

                tmpChar = getChar(rs, this.fields.get("sitetype"));
                if (tmpChar != null) {
                    site.setSiteType(getType(tmpChar));
                }
                tmpDate = this.convertToDate(getString(rs, this.fields.get("compilation_date")));
                if (tmpDate != null) {
                    site.setSiteCompDate(tmpDate);
                }
                tmpDate = this.convertToDate(getString(rs, this.fields.get("update_date")));
                if (tmpDate != null) {
                    site.setSiteUpdateDate(tmpDate);
                }

                tmpDate = this.convertToDate(getString(rs, this.fields.get("sci_prop_date")));
                if (tmpDate != null) {
                    if (!SDF_ManagerApp.isEmeraldMode()) {
                        site.setSiteSciPropDate(tmpDate);
                    } else {
                        site.setSiteProposedAsciDate(tmpDate);
                    }
                }
                tmpDate = this.convertToDate(getString(rs, this.fields.get("sci_conf_date")));
                if (tmpDate != null) {
                    if (!SDF_ManagerApp.isEmeraldMode()) {
                        site.setSiteSciConfDate(tmpDate);
                    } else {
                        site.setSiteConfirmedAsciDate(tmpDate);
                    }
                }

                // EMERALD does not have SAC and SPA dates.
                if (!SDF_ManagerApp.isEmeraldMode()) {
                    tmpDate = this.convertToDate(getString(rs, this.fields.get("spa_date")));
                    if (tmpDate != null) {
                        site.setSiteSpaDate(tmpDate);
                    }

                    tmpDate = this.convertToDate(getString(rs, this.fields.get("sac_date")));
                    if (tmpDate != null) {
                        site.setSiteSacDate(tmpDate);
                    }
                }
                tmpStr = getString(rs, this.fields.get("respondent"));
                if (tmpStr != null) {
                    Resp resp = new Resp();
                    resp.setRespAddress(tmpStr);
                    resp.getSites().add(site);
                    session.save(resp);
                    site.setResp(resp);
                }

                // area and site_length are textual in emerald:
                if (SDF_ManagerApp.isEmeraldMode()) {
                    tmpStr = rs.getString(this.fields.get("area"));
                    tmpDouble = ImporterUtils.fixAndGetDouble(tmpStr);
                    site.setSiteArea(tmpDouble);

                    tmpStr = rs.getString(this.fields.get("site_length"));
                    tmpDouble = ImporterUtils.fixAndGetDouble(tmpStr);
                    site.setSiteLength(tmpDouble);
                } else {
                    tmpDouble = getDouble(rs, this.fields.get("area"));
                    site.setSiteArea(tmpDouble);
                    tmpDouble = getDouble(rs, this.fields.get("site_length"));
                    site.setSiteLength(tmpDouble);
                }

                String sign = getString(rs, this.fields.get("lon_ew"));
                Double deg = getDouble(rs, this.fields.get("lon_deg"));
                Double min = getDouble(rs, this.fields.get("lon_min"));
                Double sec = getDouble(rs, this.fields.get("lon_sec"));
                Double longitude = this.convertCoordinate(1, sign, deg, min, sec);
                if (longitude != null) {
                    site.setSiteLongitude(longitude);
                }
                deg = getDouble(rs, this.fields.get("lat_deg"));
                min = getDouble(rs, this.fields.get("lat_min"));
                sec = getDouble(rs, this.fields.get("lat_sec"));
                Double latitude = this.convertCoordinate(2, sign, deg, min, sec);
                if (latitude != null) {
                    site.setSiteLatitude(latitude);
                }
                tmpStr = getString(rs, this.fields.get("designation"));
                if (tmpStr != null) {
                    site.setSiteDesignation(tmpStr);
                }
                tmpStr = getString(rs, this.fields.get("quality"));
                if (tmpStr != null) {
                    site.setSiteQuality(tmpStr);
                }

                tmpStr = getString(rs, this.fields.get("characteristics"));
                if (tmpStr != null) {
                    site.setSiteCharacteristics(tmpStr);
                }

                tmpStr = getString(rs, this.fields.get("documentation"));

                if (tmpStr != null) {
                    Doc doc = new Doc();
                    doc.setDocDescription(tmpStr);
                    doc.getSites().add(site);
                    session.save(doc);
                    site.setDoc(doc);
                }
                tmpStr = getString(rs, this.fields.get("mgmt_plan"));

                String tmpStrMgmtBody = getString(rs, this.fields.get("mgmt_body"));

                if (tmpStr != null) {

                    Mgmt mgmt = new Mgmt();

                    mgmt.setMgmtConservMeasures(tmpStr);
                    mgmt.setMgmtStatus('Y');
                    session.save(mgmt);
                    /*
                     * MgmtPlan mgmtPlan = new MgmtPlan();
                     * mgmtPlan.setMgmtPlanName(tmpStr);
                     * mgmtPlan.setMgmt(mgmt);
                     * mgmt.getMgmtPlans().add(mgmtPlan);
                     * session.save(mgmtPlan);
                     */
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

                ArrayList<String> regions = new ArrayList<String>();
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

                    Boolean region = getBoolean(rs, regions.get(i));
                    region = region == null ? false : region;
                    if (region) {
                        String bioRegCode = translateBioRegions(regions.get(i));
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
        } catch (SQLException e) {
            // e.printStackTrace();
            ImporterMDB.log.error("Error:::" + e.getMessage());
        } catch (Exception e) {
            // e.printStackTrace();
            ImporterMDB.log.error("Error:::" + e.getMessage());
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
            // e.printStackTrace();
            ImporterMDB.log.error("An error has occurred, searching the bioregion Id. Error Message:::" + e.getMessage());
        }
        return b.intValue();
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
    private Object[] getMinMax(String value) {
        Object[] result = {null, null, null, null, null};
        String val = value.toLowerCase();
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
            // String unit = null;
            String popType = null;
            if ((val.length() > 1) && (val.startsWith("i") || val.startsWith("p"))) {
                // unit = val.substring(0, 1);
                popType = val.substring(1, 2);
            } else {
                popType = val.substring(0, 1);
            }
            result[0] = null;
            result[1] = null;
            result[2] = null; // no unit after all, is useless
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
            // species.setSpeciesDataQuality(((String) tokens[4]).charAt(0));
            species.setSpeciesDataQuality((String) tokens[4]);
        }
        log(String.format("\tExtracting population (%s) for (%s): min=%s, max=%s, unit=%s, category=%s, quality=%s", popString,
                species.getSpeciesName(), tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]), 2);
        ImporterMDB.log.info(String.format(
                "\tExtracting population (%s) for (%s): min=%s, max=%s, unit=%s, category=%s, quality=%s", popString,
                species.getSpeciesName(), tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]));
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
        /* return population min/max/unit/catgory/data quality */
        Object[] result = {null, null, null, null, null};
        String resident2 = preparePopField(resident);
        String breeding2 = preparePopField(breeding);
        String wintering2 = preparePopField(wintering);
        String staging2 = preparePopField(staging);
        int min = 0;
        int max = 0;
        String popType = null;
        String literal = "";
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
        log(String.format("\tExtracting population (%s) for (%s): min=%s, max=%s, unit=%s, category=%s, quality=%s", literal,
                spName, tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]), 2);
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
        /*** Add species groups **/
        Statement stmt = null;
        ResultSet rs = null;
        try {

            String[] tables =
                    {this.tables.get("amprep"), this.tables.get("bird"), this.tables.get("fishes"), this.tables.get("invert"),
                            this.tables.get("mammal"), this.tables.get("plant"), this.tables.get("spec")};
            for (int i = 0; i < tables.length; i++) {
                Transaction tx = session.beginTransaction();
                String sql = "select * from " + tables[i] + " where sitecode ='" + site.getSiteCode() + "'";
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                String tmpStr;
                Character tmpChar;

                while (rs.next()) {

                    if (!tables[i].equals("spec")) {

                        Species species = new Species();
                        tmpStr = getString(rs, this.fields.get("species_code"));
                        if (tmpStr != null) {
                            species.setSpeciesCode(tmpStr);
                        }
                        tmpChar = getChar(rs, this.fields.get("species_population"));
                        if (tmpChar != null) {
                            species.setSpeciesPopulation(tmpChar);
                        }
                        tmpStr = getString(rs, this.fields.get("species_name"));
                        if (tmpStr != null) {
                            species.setSpeciesName(tmpStr);
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

                        String resident = "";
                        String breeding = "";
                        String wintering = "";
                        String staging = "";

                        tmpStr = getString(rs, this.fields.get("species_resident"));

                        boolean saveDefault = true; // if no other category is found, the default will be saved.
                        if (tmpStr != null && !tmpStr.trim().equals("")) {
                            saveDefault = false;
                            resident = tmpStr;
                            species.setSpeciesType('p');
                            processPopulationSize(species, resident);
                            species.setSite(site);
                            site.getSpecieses().add(species);
                        }
                        if (!tables[i].equals("plant")) {
                            tmpStr = getString(rs, this.fields.get("species_breeding"));
                            if (tmpStr != null && !tmpStr.trim().equals("")) {
                                saveDefault = false;
                                breeding = tmpStr;
                                Species newSpecies = new Duplicator().duplicateSpeciesNoPopulation(species);
                                newSpecies.setSpeciesType('r');
                                processPopulationSize(newSpecies, breeding);
                                newSpecies.setSite(site);
                                site.getSpecieses().add(newSpecies);
                            }
                            tmpStr = getString(rs, this.fields.get("species_wintering"));
                            if (tmpStr != null && !tmpStr.trim().equals("")) {
                                saveDefault = false;
                                wintering = tmpStr;
                                Species newSpecies = new Duplicator().duplicateSpeciesNoPopulation(species);
                                newSpecies.setSpeciesType('w');
                                processPopulationSize(newSpecies, wintering);
                                newSpecies.setSite(site);
                                site.getSpecieses().add(newSpecies);
                            }
                            tmpStr = getString(rs, this.fields.get("species_staging"));
                            if (tmpStr != null && !tmpStr.trim().equals("")) {
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
                        // session.save(species);
                    } else if (tables[i].equals(this.tables.get("spec"))) {
                        OtherSpecies oSpecies = new OtherSpecies();

                        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");

                        // EMERALD DB does not have species code in SPEC:
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

                            if (Pattern.matches("([C,V,R,P])", strPopulationInput) && strPopulationInput.length() == 1) {
                                System.out.println(":: MATCH 0");

                                // Get Population Char
                                oSpecies.setOtherSpeciesCategory(strPopulationInput.charAt(0));
                            } else if (strPopulationInput.length() > 1) {

                                String strMinMaxPatterString = "(\\d+)-(\\d+).*";
                                String strNumberAndCharacter = "(\\d+)+\\W*+([C,V,R,P]).*";
                                String strOnlyNumbers = "(\\d+).*";
                                String strOnlyNumber = "([0-9])";
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
                                        } catch (Exception ex) {
                                        }
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
                                        } catch (Exception ex) {
                                        }
                                    }

                                    pattern = Pattern.compile("([C,V,R,P])");
                                    matcher = pattern.matcher(strPopulationInput);
                                    while (matcher.find()) {
                                        try {
                                            String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                            oSpecies.setOtherSpeciesCategory(tmp.charAt(0));
                                        } catch (Exception ex) {
                                        }
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
                                        } catch (Exception ex) {
                                        }
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
                                        } catch (Exception ex) {
                                        }
                                    }

                                } else if (Pattern.matches(strOnlyNumbers, strPopulationInput)) {
                                    System.out.println(":: MATCH 5");

                                    Pattern pattern = Pattern.compile("(\\d+)");
                                    Matcher matcher = pattern.matcher(strPopulationInput);
                                    while (matcher.find()) {
                                        try {
                                            String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                            oSpecies.setOtherSpeciesSizeMin(new Integer(tmp));
                                            oSpecies.setOtherSpeciesSizeMax(new Integer(tmp));
                                        } catch (Exception ex) {
                                        }
                                    }
                                } else if (Pattern.matches(strOnlyNumber, strPopulationInput)) {
                                    System.out.println(":: MATCH 6");

                                    Pattern pattern = Pattern.compile("(\\d)");
                                    Matcher matcher = pattern.matcher(strPopulationInput);
                                    while (matcher.find()) {
                                        try {
                                            String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                            oSpecies.setOtherSpeciesSizeMin(new Integer(tmp));
                                            oSpecies.setOtherSpeciesSizeMax(new Integer(tmp));
                                        } catch (Exception ex) {
                                        }
                                    }
                                }

                            } else if (Pattern.matches("(\\d)", strPopulationInput)) {
                                System.out.println(":: MATCH 7");

                                Pattern pattern = Pattern.compile("(\\d)");
                                Matcher matcher = pattern.matcher(strPopulationInput);
                                while (matcher.find()) {
                                    try {
                                        String tmp = strPopulationInput.substring(matcher.start(), matcher.end());
                                        oSpecies.setOtherSpeciesSizeMin(new Integer(tmp));
                                        oSpecies.setOtherSpeciesSizeMax(new Integer(tmp));
                                    } catch (Exception ex) {
                                    }
                                }
                            }

                        }

                        tmpStr = getString(rs, this.fields.get("species_motivation"));
                        if (tmpStr != null) {
                            /* value is either A, B, C or D */
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
                        // session.save(oSpecies);
                    }
                }

                rs.close();
                stmt.close();
                tx.commit();
                session.flush();

            }
        } catch (SQLException e) {
            // e.printStackTrace();
            ImporterMDB.log.error("Error:::" + e.getMessage());
        } catch (Exception e) {
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
        } finally {
            /*
             * rs.close();
             * stmt.close();
             */
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
            while (rs.next()) {
                Habitat habitat = new Habitat();
                tmpStr = getString(rs, this.fields.get("habitat_code"));
                if (tmpStr != null) {
                    habitat.setHabitatCode(tmpStr);
                }
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
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
        } catch (Exception e) {
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
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

            while (rs.next()) {
                NationalDtype dType = new NationalDtype();
                tmpStr = getString(rs, this.fields.get("national_designation_code"));
                if (tmpStr != null) {
                    dType.setNationalDtypeCode(tmpStr);
                }
                tmpDouble = getDouble(rs, this.fields.get("national_designation_cover"));
                if (tmpDouble != null) {
                    dType.setNationalDtypeCover(tmpDouble);
                }
                dType.setSite(site);
                site.getNationalDtypes().add(dType);
            }
        } catch (SQLException e) {
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
        } catch (Exception e) {
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
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

            while (rs.next()) {
                SiteRelation relation = new SiteRelation();
                tmpStr = getString(rs, this.fields.get("relation_code"));
                if (tmpStr != null) {
                    relation.setSiteRelationCode(tmpStr);
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
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
        } catch (Exception e) {
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
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

            while (rs.next()) {
                HabitatClass habitat = new HabitatClass();

                tmpStr = getString(rs, this.fields.get("habitat_class_code"));
                if (tmpStr != null) {
                    habitat.setHabitatClassCode(tmpStr);
                }
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
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
        } catch (Exception e) {
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
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
            log.info("The description of the habitat class::" + habClassCode + " is missing.");
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
            while (rs.next()) {
                Region region = new Region();
                tmpStr = getString(rs, this.fields.get("region_code"));
                if (tmpStr != null) {
                    /* just get NUT2 level */
                    if (tmpStr.length() > 4) {

                        // in some EMERALD DB code starts with unnecessary apostrophe
                        if (SDF_ManagerApp.isEmeraldMode() && StringUtils.startsWith(tmpStr, "'")) {
                            tmpStr = tmpStr.substring(1, 5);
                        } else {
                            tmpStr = tmpStr.substring(0, 4);
                        }
                    }
                    // concatenating with 'ZZ' for unknown works only for N2k
                    if (!SDF_ManagerApp.isEmeraldMode() && (tmpStr.equals("0") || tmpStr.equals("00"))) {
                        tmpStr = site.getSiteCode().substring(0, 2) + "ZZ";
                        region.setRegionName("Marine");
                        log(String.format("\tConverting marine region code (0 or 00) to NUTS code '%s'", tmpStr), 2);
                    } else {
                        try {
                            String tableName = SDF_ManagerApp.isEmeraldMode() ? "RefNutsEmerald" : "RefNuts";
                            Iterator itr =
                                    session.createQuery(" from " + tableName + " as rn where rn.refNutsCode like '" + tmpStr + "'")
                                            .iterate();

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
                                log(String.format("\tCouldn't match NUTS code (%s). Encoding anyway.", tmpStr), 2);

                            }
                        } catch (Exception e) {
                            ImporterMDB.log.error("Error:::" + e.getMessage());
                            // e.printStackTrace();
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
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
        } catch (Exception e) {
            ImporterMDB.log.error("Error:::" + e.getMessage());
            // e.printStackTrace();
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

            while (rs.next()) {

                Impact impact = new Impact();
                Impact impactClon = null;
                String impactCodeOld = getString(rs, this.fields.get("impact_code"));

                if (impactCodeOld != null) {

                    if (impactCodeOld != null && !(("").equals(impactCodeOld))) {

                        tmpStr = getImpactCode(impactCodeOld);

                        if (tmpStr != null) {

                            impact.setImpactCode(tmpStr);

                            // IN_OUT
                            tmpChar = getChar(rs, this.fields.get("impact_ocurrence"));
                            if (tmpChar != null) {
                                impact.setImpactOccurrence(Character.toLowerCase(tmpChar));
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
                            }

                            // INFLUENCE
                            tmpChar = getChar(rs, this.fields.get("impact_type"));

                            if (tmpChar != null && tmpStr != "null") {

                                if (("+").equals(tmpChar.toString())) {
                                    impact.setImpactType('P');
                                } else if (("-").equals(tmpChar.toString())) {
                                    impact.setImpactType('N');
                                } else if (("0").equals(tmpChar.toString()) || ("O").equals(tmpChar.toString())
                                        || ("o").equals(tmpChar.toString())) {
                                    impactClon = new Impact();
                                    impactClon.setImpactOccurrence(impact.getImpactOccurrence());
                                    impactClon.setImpactRank(impact.getImpactRank());
                                    impactClon.setImpactCode(impact.getImpactCode());
                                    impactClon.setImpactType('N');
                                    impact.setImpactType('P');
                                } else {
                                    ImporterMDB.log.info("Impact code is not valid");
                                }

                                impact.setSite(site);
                                if (impactClon != null) {
                                    impactClon.setSite(site);
                                    site.getImpacts().add(impactClon);
                                }
                                if (impact != null) {
                                    site.getImpacts().add(impact);
                                }

                            } else {
                                ImporterMDB.log.info("IMPACT NOT IMPORTING :: " + impactCodeOld);
                            }

                            // session.flush();
                            // session.evict(impact);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            ImporterMDB.log.error("Error:::" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            ImporterMDB.log.error("Error:::" + e.getMessage());
            e.printStackTrace();
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
            // e.printStackTrace();
            ImporterMDB.log.error("Error:::" + e.getMessage());
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
    Double convertCoordinate(int type, String sig, Double degree, Double minute, Double second) {

        /**
         * 1 = longitude; 2 = latitude
         */
        Double deg = degree == null ? 0.0 : degree;
        Double min = minute == null ? 0.0 : second;
        Double sec = second == null ? 0.0 : second;

        Double tmp = degree + (minute / 60) + (second / 3600);

        if (type == 1) {
            String sign = sig == null ? "E" : sig.toUpperCase(); // default to east
            tmp = sign.equals("E") ? tmp : (sign.equals("W") ? -tmp : tmp); // unknown default to east as well
        }
        return tmp == 0.0 ? null : tmp;

    }

    /**
     *
     * @param oldType
     * @return
     */
    String convertType(char oldType) {

        Character charOldType = Character.toUpperCase(oldType);
        char[] spa = {'A', 'D', 'F', 'H', 'J'};
        char[] sci = {'B', 'E', 'G', 'I', 'K'};
        if (ArrayUtils.contains(spa, charOldType)) {
            return "A";
        } else if (ArrayUtils.contains(sci, charOldType)) {
            return "B";
        } else if (("C").equals(charOldType.toString())) {
            return "C";
        } else {
            return "";
        }
    }

    /**
     *
     * @param sdate
     * @return
     */
    Date convertToDate(String sDate) {
        if (sDate == null || (("").equals(sDate))) {
            return null;
        }
        if (sDate.length() < 6) {
            ImporterMDB.log.error("\tDate doesn't match size: " + sDate);
            return null;
        }

        String month = sDate.substring(4, 6);
        String year = sDate.substring(0, 4);
        int imonth = this.converToInt(month);
        int iyear = this.converToInt(year);
        Date d = new Date();
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(iyear, imonth - 1, 1);
        d = cal.getTime();
        return d;
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
            ImporterMDB.log.error("Error:::" + e.getMessage());
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
            ImporterMDB.log.error("Error:::" + e.getMessage());
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
            // Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using builder to get DOM representation of the XML file
            log("Parsing: " + fileName, 1);
            dom = db.parse(fileName);
            return dom.getDocumentElement();
        } catch (ParserConfigurationException pce) {
            // pc//e.printStackTrace();
            ImporterMDB.log.error("Error`parsing XML:::" + pce.getMessage());
            return null;
        } catch (SAXException se) {
            // s//e.printStackTrace();
            ImporterMDB.log.error("Error`parsing XML:::" + se.getMessage());
            return null;
        } catch (IOException ioe) {
            ImporterMDB.log.error("Error`parsing XML:::" + ioe.getMessage());
            // io//e.printStackTrace();
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

}
