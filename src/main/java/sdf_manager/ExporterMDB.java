/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pojos.Site;
import sdf_manager.util.SDF_MysqlDatabase;
import sdf_manager.util.ValidateSite;

import com.healthmarketscience.jackcess.Database.FileFormat;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.util.ImportUtil;

/**
 *
 * @author charbda
 */
public class ExporterMDB implements Exporter {

    private String table_file = "config" + System.getProperty("file.separator") + "table_names.xml";
    private String table_element = "table";
    private HashMap<String, String> tables;
    private String[] tableKeys = {"name", "used_name"};
    private String encoding;
    private Logger logger;
    private String logExportFileName;
    private FileWriter logErrorFile;
    private String fileName;

    private ArrayList sitecodes = new ArrayList();

    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(ExporterMDB.class.getName());

    /**
     *
     * @param logger
     */
    public ExporterMDB(Logger logger, String encoding) {
        this.logger = logger;
        this.encoding = encoding;
        init();
    }

    /**
     *
     * @param logger
     */
    public ExporterMDB(Logger logger, String encoding, String logFileName, String mdbFileName) {
        this.logger = logger;
        this.encoding = encoding;
        this.logExportFileName = logFileName;
        this.fileName = mdbFileName;
        init();
    }

    /**
     *
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private File validateSites() {

        File fileMDBLog = null;
        HashMap<String, List<String>> validationErrorsMap = new HashMap<String, List<String>>();
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            String hql = " from Site as site order by site.siteCode";
            Iterator sitesIter = session.createQuery(hql).iterate();

            while (sitesIter.hasNext()) {

                Site site = (Site) sitesIter.next();
                String siteCode = site.getSiteCode();

                log("Validating::" + siteCode);

                List<String> validationErrors = ValidateSite.validate(site);
                if (validationErrors != null && !validationErrors.isEmpty()) {
                    validationErrorsMap.put(siteCode, validationErrors);
                }

                this.sitecodes.add(site);
            }

            commit(tx);

            if (!validationErrorsMap.isEmpty()) {
                fileMDBLog = copyToLogExportFile(validationErrorsMap);
            }
        } catch (Exception e) {
            log("ERROR loadSitecodes()" + e.getMessage());
            ExporterMDB.log.error("ERROR loadSitecodes():::" + e.getMessage());
            e.printStackTrace();
        } finally {
            close(session);
        }
        log("End of Validation");

        return fileMDBLog;
    }

    /**
     * Utility method for committing a Hibernate transaction null-safely and quietly.
     *
     * @param tx Hibernate transaction.
     */
    private void commit(Transaction tx) {
        try {
            tx.commit();
        } catch (Exception e) {
            // Ignore deliberately;
        }
    }

    /**
     * Utility method for closing Hibernate session null-safely and quietly.
     *
     * @param session The Hibernate session.
     */
    private static void close(Session session) {
        try {
            if (session != null) {
                session.close();
            }
        } catch (HibernateException e) {
            // Ignore deliberately.
        }
    }

    /**
     *
     */
    void init() {
        this.tables = new HashMap();
        this.parse(this.table_file, this.tables, this.table_element, this.tableKeys);
    }

    /**
     *
     * @param fileName
     * @return
     */
    @Override
    public boolean processDatabase(String fileName) {

        File validationErrorsLogFile = this.validateSites();

        com.healthmarketscience.jackcess.Database database = null;
        try {
            database = DatabaseBuilder.create(FileFormat.V2007, new File(fileName));
            if (database != null) {
                copyData(fileName, validationErrorsLogFile);
            } else {
                String msg = "The created MS Access database object is null!";
                log.error(msg);
                log(msg);
            }
        } catch (Exception e) {
            String msg = "Error creating MS Access database at " + fileName;
            log.error(msg, e);
            log(msg);
        } finally {
            if (database != null) {
                try {
                    database.close();
                } catch (IOException e) {
                    // Ignore deliberately.
                }
            }
        }

        return true;
    }

    /**
     *
     * @param fileName
     *
     
     TODO: REMOVE THIS CODE
    void createSchema(String fileName) {
        Statement stmt = null;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + fileName + ";create=true";
            Connection conn = DriverManager.getConnection(database, "", "");
            Configuration cfg = new Configuration();
            cfg.configure();
            String[] cmds = cfg.generateSchemaCreationScript(new MSAccessDialect());
            stmt = conn.createStatement();
            for (String cmd : cmds) {
                if (!cmd.toLowerCase().startsWith("create table")) {
                    continue;
                }
                String sql = cmd.replaceAll("PUBLIC.", "");
                sql = sql.replaceAll("VARCHAR\\([0-9]+\\)", "MEMO");
                stmt.execute(sql);
                log("Executing command in database: " + sql);
                log.info("Executing command in database: " + sql);
            }
            conn.close();
        } catch (Exception e) {
            // e.printStackTrace();
            log.error("Error createSchema().:::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("Error createSchema().:::" + e.getMessage());
            }
        }
    }*/

    /**
     *
     * @param fileName
     */
    void copyData(String fileName, File validationErrorsLogFile) {

        // The MS Access database we're going to write.
        com.healthmarketscience.jackcess.Database accessDb = null;

        // Connection to the SQL database we're about to query and copy into MS Access.
        Connection conn = null;
        ResultSet rsTables = null;
        ResultSet rsTableRows = null;
        Statement stmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Configuration cfg = new Configuration();
            cfg.configure();
            Properties properties = new Properties();
            properties.load(new FileInputStream(SDF_ManagerApp.LOCAL_PROPERTIES_FILE));
            Class.forName("com.mysql.cj.jdbc.Driver");

            log("Connecting to MySQL");
            log.info("Connecting to MySQL");

            String dbHost = properties.getProperty("db.host");
            String dbPort = properties.getProperty("db.port");
            String dbUser = properties.getProperty("db.user");
            String dbPassword = properties.getProperty("db.password");

            String dbSchemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
            String dbConnUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbSchemaName + "?useSSL=false&autoReconnect=true";

            conn = DriverManager.getConnection(dbConnUrl, dbUser, dbPassword);
            DatabaseMetaData dbMetaData = conn.getMetaData();

            rsTables = dbMetaData.getTables(dbSchemaName, null, "%", null);
            accessDb = DatabaseBuilder.open(new File(fileName));

            while (rsTables.next()) {

                String tableName = rsTables.getString("TABLE_NAME");
                if (tableName.toUpperCase().startsWith("REF_") || StringUtils.startsWith(tableName.toUpperCase(), "COUNTRY") || tableName.toUpperCase().equals("RELEASEDBUPDATES")) {
                    continue;
                }

                log("Copying table: " + tableName);
                log.info("Copying table: " + tableName);

                stmt = conn.createStatement();
                rsTableRows = stmt.executeQuery("select * from " + tableName);

                ImportUtil.importResultSet(rsTableRows, accessDb, tableName);

                SDF_MysqlDatabase.closeQuietly(rsTableRows);

                log("Copied data to database from table: " + tableName);
                log.info("Copied data to database from table: " + tableName);
            }
            SDF_MysqlDatabase.closeQuietly(rsTables);
            SDF_MysqlDatabase.closeQuietly(conn);

            String msg = "Finishing export process, closing connection to database.";
            log(msg);
            ExporterMDB.log.info(msg);

            if (validationErrorsLogFile != null) {

                msg = "The data was exported, but there were data validation errors!\nPlease check the log file for more details.";
                log(msg);
                JOptionPane
                        .showMessageDialog(new JFrame(), msg, "Dialog", JOptionPane.WARNING_MESSAGE);

                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                    Desktop.getDesktop().open(validationErrorsLogFile);
                }
            } else {
                msg = "Export process has finished succesfully!";
                ExporterMDB.log.info(msg);
                JOptionPane.showMessageDialog(new JFrame(), msg, "Dialog", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            log("Failed exporting data to database.");
            log.error("Error copyData().:::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);

        } finally {
            SDF_MysqlDatabase.closeQuietly(rsTableRows);
            SDF_MysqlDatabase.closeQuietly(rsTables);
            SDF_MysqlDatabase.closeQuietly(conn);
            if (accessDb != null) {
                try {
                    accessDb.close();
                } catch (Exception e) {
                    // Ignore deliberately.
                }
            }
        }
    }

    /**
     *
     * @param msg
     */
    void log(String msg) {
        this.logger.log(msg);
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
            // this.dManager.writeLog("Successfully loaded regions from file.\n");
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
            log("Parsing: " + fileName);
            dom = db.parse(fileName);
            return dom.getDocumentElement();
        } catch (ParserConfigurationException pce) {
            // pce.printStackTrace();
            log.error("Error copyData().:::" + pce.getMessage());
            return null;
        } catch (SAXException se) {
            // se.printStackTrace();
            log.error("Error copyData().:::" + se.getMessage());
            return null;
        } catch (IOException ioe) {
            // ioe.printStackTrace();
            log.error("Error copyData().:::" + ioe.getMessage());
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

    /**
     *
     * @param filename
     * @return
     */
    @SuppressWarnings("rawtypes")
    @Override
    public ArrayList createXMLFromDataBase(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param exportErrorMap
     * @return
     */
    @SuppressWarnings("rawtypes")
    private File copyToLogExportFile(HashMap exportErrorMap) {
        File fileLog = null;
        try {
            fileLog = new File(this.logExportFileName);
            logErrorFile = new FileWriter(fileLog);
            Set ErrorSiteKeySet = exportErrorMap.keySet();
            Iterator it = ErrorSiteKeySet.iterator();
            while (it.hasNext()) {
                logErrorFile.write("------------------------------------------------------------"
                        + System.getProperty("line.separator"));
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
                String dateLine = sdf.format(cal.getTime());
                String siteCode = (String) it.next();

                logErrorFile.write(dateLine + ": An error has been produced in the export process for the site: " + siteCode
                        + System.getProperty("line.separator"));
                ArrayList arraySites = (ArrayList) exportErrorMap.get(siteCode);

                if (!arraySites.isEmpty()) {
                    logErrorFile.write(dateLine + ": Please, check the following fields of the site in the SDF editor:"
                            + System.getProperty("line.separator"));
                    Iterator itSite = arraySites.iterator();
                    while (itSite.hasNext()) {
                        String lineExport = (String) itSite.next();
                        logErrorFile.write("     " + dateLine + ": " + lineExport + System.getProperty("line.separator"));
                        logErrorFile.flush();
                    }
                }
                logErrorFile.write("------------------------------------------------------------"
                        + System.getProperty("line.separator"));
            }
            logErrorFile.flush();
            logErrorFile.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(),
                    "Export process has failed.\nPlease check the sdfLog file for more details", "Dialog",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            ExporterMDB.log.error("Error copyToLogExportFile(). " + e.getMessage());
        }
        return fileLog;
    }

}
