/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager;

import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import org.hibernate.cfg.Configuration;

import com.healthmarketscience.jackcess.*;
import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pojos.Site;
import sdf_manager.util.ValidateSite;

/**
 *
 * @author charbda
 */
public class ExporterMDB implements Exporter {

    private String table_file = "config" + System.getProperty("file.separator") + "table_names.xml";
    private String table_element = "table";
    private HashMap<String,String> tables;
    private String[] tableKeys = {"name","used_name"};
    private String encoding;
    private Logger logger;
    private String logExportFileName;
    private FileWriter logErrorFile;
    private String fileName;

    private ArrayList sitecodes = new ArrayList();

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExporterMDB.class .getName());

    /**
     *
     * @param logger
     */
    public ExporterMDB(Logger logger, String encoding) {
        this.logger = logger;
        this.encoding=encoding;
        init();
    }

    /**
     *
     * @param logger
     */
    public ExporterMDB(Logger logger, String encoding,  String logFileName, String mdbFileName) {
        this.logger = logger;
        this.encoding=encoding;
        this.logExportFileName = logFileName;
        this.fileName = mdbFileName;
        init();
    }


     /**
     *
     */
    private File validateSites() {
        File fileMDBLog = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();
            String hql = " from Site as site order by site.siteCode";
            Iterator itrSites = session.createQuery(hql).iterate();
            HashMap errorMsgMap = new HashMap();

            while (itrSites.hasNext()) {
                Site site = (Site)itrSites.next();
                ArrayList errMsgList = ValidateSite.validate(site);
                errorMsgMap.put(site.getSiteCode(), errMsgList);
                log("Validating::" + site.getSiteCode());
                this.sitecodes.add(site);
            }
            tx.commit();
            session.close();
            if (errorMsgMap != null && !errorMsgMap.isEmpty()) {
                fileMDBLog = copyToLogExportFile(errorMsgMap);
            }

        }
        catch (Exception e) {
            log("ERROR loadSitecodes()" + e.getMessage());
            ExporterMDB.log.error("ERROR loadSitecodes():::" + e.getMessage());
            e.printStackTrace();
        }
        log("End of Validation");

        return fileMDBLog;
    }



    /**
     *
     */
    void init() {
         this.tables = new HashMap();
         this.parse(this.table_file,this.tables,this.table_element,this.tableKeys);
     }
    /**
     *
     * @param fileName
     * @return
     */
    public boolean processDatabase(String fileName) {

        File fileLog = this.validateSites();

        this.createDatabase(fileName);
        this.copyData(fileName,fileLog);

        return true;
    }



    /**
     *
     * @param fileName
     */
    void createDatabase(String fileName) {
        try {

            com.healthmarketscience.jackcess.Database db = Database.create(new File(fileName));
        } catch (Exception e) {
            log.error("Error createDatabase().:::" + e.getMessage());
            log("Failed to create MDB file.");
        }
    }
    /**
     *
     * @param fileName
     */
    void createSchema(String fileName) {
        Statement stmt = null;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + fileName + ";create=true";
            Connection conn = DriverManager.getConnection( database ,"","");
            Configuration cfg = new Configuration();
            cfg.configure();
            String[] cmds = cfg.generateSchemaCreationScript(new MSAccessDialect());
            stmt = conn.createStatement();
            for (String cmd : cmds) {
                if (!cmd.toLowerCase().startsWith("create table")) continue;
                String sql = cmd.replaceAll("PUBLIC.", "");
                sql = sql.replaceAll("VARCHAR\\([0-9]+\\)", "MEMO");
                stmt.execute(sql);
                log("Executing command in database: " + sql);
                log.info("Executing command in database: " + sql);
            }
            conn.close();
        }
        catch (Exception e) {
            //e.printStackTrace();
            log.error("Error createSchema().:::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details", "Dialog",JOptionPane.ERROR_MESSAGE);
        } finally {
            try{
                stmt.close();
            } catch (SQLException e) {
                log.error("Error createSchema().:::" + e.getMessage());
            }
        }
    }
    /**
     *
     * @param fileName
     */
    void copyData(String fileName, File fileLog) {
        try {
            Class.forName("com.mysql.jdbc.Driver" );
            Configuration cfg = new Configuration();
            cfg.configure();
            Properties props = cfg.getProperties();
            Properties properties = new Properties();
            properties.load(new FileInputStream(new java.io.File("").getAbsolutePath()+"\\database\\sdf_database.properties"));
            Class.forName("com.mysql.jdbc.Driver");

            log("Conecting to MySQL");
            log.info("Connecting to MySQL");

            Connection conn = (Connection) DriverManager.getConnection("jdbc:mysql://" + properties.getProperty("host")+"/natura2000?autoReconnect=true",properties.getProperty("user"),properties.getProperty("password"));
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet rs = dbm.getTables(null, "natura2000", "%" , null);
            com.healthmarketscience.jackcess.Database db = Database.open(new File(fileName));

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (tableName.toUpperCase().startsWith("REF_") || tableName.toUpperCase().equals("COUNTRY")) {
                    continue;
                }
                log("Copying table: " + tableName);

                log.info("Copying table: " + tableName);
                ResultSet data = conn.createStatement().executeQuery("select * from " + tableName);
                db.copyTable(tableName, data);
                log("Copied data to database from table: " + tableName);
                log.info("Copied data to database from table: " + tableName);
            }
            conn.close();


            log("Finishing export process.Closing connection to Data Base");
            ExporterMDB.log.info("Finishing export process");
            if (fileLog != null) {
                log("The validation of the data has been failed,the data in DB is not compliant with SDF the schema.\nPlease check the log file, for more details.");
                JOptionPane.showMessageDialog(new JFrame(), "The validation of the data has been failed,\nthe data is not compliant with SDF the schema.\n Please check the log file, for more details", "Dialog",JOptionPane.INFORMATION_MESSAGE);

                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                    Desktop.getDesktop().open(fileLog);
                }
            } else {
                ExporterMDB.log.error("Export process has finished succesfully");
                JOptionPane.showMessageDialog(new JFrame(), "Export process has finished succesfully.", "Dialog",JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            log("Failed exporting data to database.");
            //e.printStackTrace();
            log.error("Error copyData().:::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details", "Dialog",JOptionPane.ERROR_MESSAGE);

        } finally {

        }
    }


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
                for (int i = 0 ; i < nl.getLength();i++) {
                    Element el = (Element)nl.item(i);
                    String key = this.getTextValue(el,fields[0]);
                    String value = this.getTextValue(el,fields[1]);
                    map.put(key, value);
                }
            }
           //this.dManager.writeLog("Successfully loaded regions from file.\n");
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
            log("Parsing: " + fileName);
            dom = db.parse(fileName);
            return dom.getDocumentElement();
        } catch (ParserConfigurationException pce) {
            //pce.printStackTrace();
            log.error("Error copyData().:::" + pce.getMessage());
            return null;
        } catch (SAXException se) {
            //se.printStackTrace();
            log.error("Error copyData().:::" + se.getMessage());
            return null;
        } catch (IOException ioe) {
            //ioe.printStackTrace();
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
                Element el = (Element)nl.item(0);
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
    public ArrayList createXMLFromDataBase(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }



     /**
     *
     * @param exportErrorMap
     * @return
     */
    private File copyToLogExportFile(HashMap exportErrorMap) {
        File fileLog = null;
        try {
          fileLog = new File(this.logExportFileName);
          logErrorFile = new FileWriter(fileLog);
          Set ErrorSiteKeySet = exportErrorMap.keySet();
          Iterator it = ErrorSiteKeySet.iterator();
          while (it.hasNext()) {
              logErrorFile.write("------------------------------------------------------------" + System.getProperty("line.separator"));
              Calendar cal = Calendar.getInstance();
              SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
              String dateLine = sdf.format(cal.getTime());
              String siteCode = (String) it.next();

              logErrorFile.write(dateLine + ": An error has been produced in the export process for the site: " + siteCode+ System.getProperty("line.separator") );
              ArrayList arraySites = (ArrayList)exportErrorMap.get(siteCode);

              if (!arraySites.isEmpty()) {
                  logErrorFile.write(dateLine + ": Please, check the following fields of te site in the SDF editor:" + System.getProperty("line.separator"));
                  Iterator itSite = arraySites.iterator();
                  while (itSite.hasNext()) {
                     String lineExport = (String)itSite.next();
                     logErrorFile.write("     " + dateLine + ": " + lineExport+ System.getProperty("line.separator"));
                     logErrorFile.flush();
                  }
              }
              logErrorFile.write("------------------------------------------------------------" + System.getProperty("line.separator") );
          }
          logErrorFile.flush();
          logErrorFile.close();
         // write("LOG file : exportLog" + formatDate+".log");
       } catch (Exception e) {
           JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\nPlease check the sdfLog file for more details", "Dialog",JOptionPane.ERROR_MESSAGE);
           e.printStackTrace();
           ExporterMDB.log.error("Error copyToLogExportFile(). " + e.getMessage());
       }
       return fileLog;
    }

}
