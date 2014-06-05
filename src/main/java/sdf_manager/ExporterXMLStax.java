package sdf_manager;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.w3c.dom.Document;

/**
 *
 * @author charbda
 */
public class ExporterXMLStax implements Exporter {

    private Logger logger;
    private String fileName;
    private Writer writer;
    private int counter;
    private String encoding;
    private FileWriter outFile;
    private PrintWriter out;
    private ArrayList<String> sitecodes = new ArrayList<String>();
    private String logExportFileName;
    private FileWriter logErrorFile;

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExporterXMLStax.class.getName());

    /**
     *
     * @param logger
     */
    public ExporterXMLStax(Logger logger) {
        this.logger = logger;
        init();
    }

    /**
     *
     * @param logger
     * @param encoding
     * @param logFileName
     * @param xmlFileName
     */
    public ExporterXMLStax(Logger logger, String encoding, String logFileName, String xmlFileName) {
        this.logger = logger;
        this.encoding = encoding;
        this.logExportFileName = logFileName;
        this.fileName = xmlFileName;
        init();
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
            // e.printStackTrace();
            ExporterXMLStax.log.error("An error has accurred in initLogFile. Error Message :::" + e.getMessage());
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
            // e.printStackTrace();
            ExporterXMLStax.log.error("An error has accurred in initLogFile. Error Message :::" + e.getMessage());
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
    @Override
    public boolean processDatabase(String fileName) {
        this.fileName = fileName;
        log("Creating requested XML document: " + fileName);
        log("Processing data...");
        this.loadSitecodes();
        this.initWriter();
        boolean isOK = this.saveConfig();

        return isOK;
    }

    /**
     *
     */
    void initWriter() {
        try {
            FileWriter fstream = new FileWriter(this.fileName);
            this.writer = new BufferedWriter(fstream);
            this.counter = 0;
            // Close the output stream
        } catch (Exception e) {
            log("ERROR initWriter()" + e.getMessage());
            ExporterXMLStax.log.error("An error has accurred in initWriter. Error Message :::" + e.getMessage());
        }
    }

    /**
     *
     */
    void finalizeWriter() {
        try {
            this.writer.close();
        } catch (IOException e) {
            log("ERROR finalizeWriter()");
            ExporterXMLStax.log.error("ERROR finalizeWriter():::" + e.getMessage());

        }
    }

    /**
     *
     */
    void finalizeWriterError() {
        try {

            this.writer.close();
            if ((new File(this.fileName)).exists()) {
                (new File(this.fileName)).delete();
            }
        } catch (IOException e) {
            log("ERROR finalizeWriter()" + e.getMessage());
            ExporterXMLStax.log.error("ERROR finalizeWriterError():::" + e.getMessage());

        }
    }

    /**
     *
     */
    void loadSitecodes() {
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();
            String hql = "select site.siteCode from Site as site order by site.siteCode";
            Iterator<?> itrSites = session.createQuery(hql).iterate();
            log("iterating...");
            while (itrSites.hasNext()) {
                Object tuple = itrSites.next();
                String sitecode = (String) tuple;
                this.sitecodes.add(sitecode);
            }
            tx.commit();
            session.close();
        } catch (Exception e) {
            log("ERROR loadSitecodes()" + e.getMessage());
            ExporterXMLStax.log.error("ERROR loadSitecodes():::" + e.getMessage());
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
     * @param src
     * @param fieldName
     * @return
     */
    String fmt(String src, String fieldName) {
        /*
         * basically a debugging function, printing out null fields
         * but could be used for other purposes as well
         */
        if (src == null) {
            return "";
        } else {
            // return getString(src);
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
            String strVal = "0.00";
            if (val != new Double(0)) {
                strVal = val.toString();
            }
            // return fmt(val.toString(), fieldName);
            return fmt(strVal, fieldName);
        } else {
            return fmt("0.00", fieldName);
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
            String strVal = "0";
            if (val != new Integer(0)) {
                strVal = val.toString();
            }
            return fmt(strVal, fieldName);
        } else {
            return fmt("0", fieldName);
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
     * @param literal
     * @throws IOException
     */
    void writeXMLLiteral(String literal) throws IOException {
        this.writer.write(literal);
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmtToLowerCase(Character val, String fieldName) {
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
    String fmtToUpperCase(Character val, String fieldName) {
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
            if (result != null && result.length == 0) {
                return null;
            } // don't enter empty string in the database
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
            ExporterXMLStax.log.error("Failed extracting field: " + fieldName
                    + ". The field could have an erroneous name. Please verify.\nError:::" + e.getMessage());

            JOptionPane.showMessageDialog(new JFrame(),
                    "Export process has failed.\nPlease check the sdfLog file for more details", "Dialog",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
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
                logErrorFile.write("------------------------------------------------------------"
                        + System.getProperty("line.separator"));
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
                String dateLine = sdf.format(cal.getTime());
                String siteCode = (String) it.next();

                // logErrorFile.write(dateLine + ": An error has been produced in the export process for the site: " + siteCode+
                // System.getProperty("line.separator"));
                ArrayList arraySites = (ArrayList) exportErrorMap.get(siteCode);

                if (!arraySites.isEmpty()) {
                    logErrorFile.write(dateLine + ": Please, check the following sites' fields in the SDF editor:"
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
            // e.printStackTrace();
            ExporterXMLStax.log.error("Error copyToLogExportFile(). " + e.getMessage());
        }
        return fileLog;
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
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean saveConfig() {

        List<String> schemaValidationMessages = new ArrayList<String>();

        // Create a XMLOutputFactory.
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            File schemaFile = SDF_ManagerApp.getXMLSchemaLocalFile();
            String schemaUrl = SDF_ManagerApp.getXMLSchemaURI();

            boolean schemaUrlBroken = Util.isUrlBroken(schemaUrl, false);
            if (schemaUrlBroken) {
                ExporterXMLStax.log.info("Schema URL broken (" + schemaUrl + "), trying with local file: " + schemaFile);
            }

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaUrlBroken ? schemaFactory.newSchema(schemaFile) : schemaFactory.newSchema(new URL(schemaUrl));

            ExporterXMLStax.log.info("Using this schema for validation: " + (schemaUrlBroken ? schemaFile : schemaUrl));

            Document doc = ExporterSiteXML.generateXML(session, this.sitecodes, schema);
            boolean xmlOK = ExporterSiteXML.writeXmlFile(doc, this.fileName);

            if (xmlOK) {

                Source xmlFile = new StreamSource(new File(this.fileName));
                Validator validator = schema.newValidator();
                SchemaValidatonErrorHandler errHandler = new SchemaValidatonErrorHandler();
                validator.setErrorHandler(errHandler);
                validator.validate(xmlFile);
                schemaValidationMessages.addAll(errHandler.getMessagesList());
            }

            if (schemaValidationMessages.isEmpty()) {

                String msg = "Export process has finished succesfully!";
                log(msg);
                ExporterXMLStax.log.info(msg);
                JOptionPane.showMessageDialog(new JFrame(), msg, "Dialog", JOptionPane.INFORMATION_MESSAGE);
            } else {

                String msg =
                        "The data was exported, but there were data validation errors!\n"
                                + "Please check the log file for more details.";
                log(msg);
                JOptionPane.showMessageDialog(new JFrame(), msg, "Dialog", JOptionPane.WARNING_MESSAGE);

                HashMap<String, List<String>> messageMap = new HashMap<String, List<String>>();
                messageMap.put(StringUtils.EMPTY, schemaValidationMessages);

                File fileLog = this.copyToLogExportFile(messageMap);
                if (fileLog != null) {
                    Desktop desktop = null;
                    if (Desktop.isDesktopSupported()) {
                        desktop = Desktop.getDesktop();
                        Desktop.getDesktop().open(fileLog);
                    }
                }
            }

            System.gc();
        } catch (Exception e) {
            ExporterXMLStax.log.error("Error processing xml file:::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed!\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);

        } finally {
            close(session);
        }

        return schemaValidationMessages.isEmpty();
    }

    /**
     *
     * @param eventFactory
     * @param eventWriter
     * @param elementName
     * @param value
     * @throws XMLStreamException
     */
    private static void createItemNode(XMLEventFactory eventFactory, XMLEventWriter eventWriter, String elementName, String value)
            throws XMLStreamException {
        XMLEvent end = eventFactory.createDTD("\n");
        StartElement startElement = eventFactory.createStartElement("", "", elementName);
        eventWriter.add(startElement);
        Characters characters = eventFactory.createCharacters(value);
        eventWriter.add(characters);
        EndElement endElement = eventFactory.createEndElement("", "", elementName);
        eventWriter.add(endElement);
        eventWriter.add(end);
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
}
