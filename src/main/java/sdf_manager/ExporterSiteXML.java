package sdf_manager;

import java.io.*;
import java.net.URL;
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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
import sdf_manager.validatorErrors.CustomErrorMessage;
import sdf_manager.validatorErrors.KeywordsBasedCustomErrorMessageCondition;
import sdf_manager.validatorErrors.ValidatorErrorMessagesPreparator;

/**
 *
 * @author charbda
 */
public class ExporterSiteXML implements Exporter {

    private Logger logger;

    private String siteCode;
    private String fileName;
    private Writer writer;
    private int counter;
    private String encoding;
    private ArrayList sitecodes = new ArrayList();
    private boolean xmlFileOK = false;
    private ArrayList xmlNoValidFields = new ArrayList();
    private FileWriter outFile;
    private PrintWriter out;
    private String logExportFileName;
    private FileWriter logErrorFile;
    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(ExporterSiteXML.class.getName());

    /**
     *
     * @param logger
     * @param siteCode
     */
    public ExporterSiteXML(Logger logger, String encoding, String siteCode) {
        this.logger = logger;
        this.siteCode = siteCode;
        this.encoding = encoding;
    }

    public ExporterSiteXML(Logger logger, String encoding, String siteCode, String logFile) {
        this.logger = logger;
        this.siteCode = siteCode;
        this.encoding = encoding;
        this.initLogFile(logFile);
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
            ExporterSiteXML.log.error("Error:::initLogFile()::" + e.getMessage());
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
    public ArrayList createXMLFromDataBase(String fileName) {
        this.fileName = fileName;
        log("Creating requested XML document: " + fileName);
        ExporterSiteXML.log.info("Creating requested XML document: " + fileName);
        log("Processing data...");
        this.loadSitecodes();
        this.initWriter();

        this.xmlNoValidFields = this.processDatabase();
        return this.xmlNoValidFields;
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
            ExporterSiteXML.log.error("Error:::initWriter()::" + e.getMessage());
        }
    }

    /**
     *
     */
    void finalizeWriter() {
        try {
            this.writer.close();
        } catch (IOException e) {
            log("ERROR finalizeWriter()" + e.getMessage());
            ExporterSiteXML.log.error("Error:::finalizeWriter()::" + e.getMessage());
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
            ExporterSiteXML.log.error("Error:::finalizeWriterError()::" + e.getMessage());
        }
    }

    /**
     *
     */
    void loadSitecodes() {
    	Session session = HibernateUtil.getSessionFactory().openSession();
        try {            
            session.getTransaction().begin();
            String hql = "select site.siteCode from Site as site where site.siteCode='" + siteCode + "' order by site.siteCode";
            Iterator itrSites = session.createQuery(hql).iterate();
            log("iterating...");
            while (itrSites.hasNext()) {
                Object tuple = itrSites.next();
                String sitecode = (String) tuple;
                this.sitecodes.add(sitecode);
            }
            session.getTransaction().commit();            
        } catch (Exception e) {
            log("ERROR loadSitecodes()" + e.getMessage());
            ExporterSiteXML.log.error("Error:::loadSitecodes()::" + e.getMessage());
        } finally {
        	session.close();
        }
    }

    /**
     *
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ArrayList processDatabase() {

        Session session = HibernateUtil.getSessionFactory().openSession();
        final ArrayList xmlValidFields = new ArrayList();
        boolean xmlOK = false;

        try {
            File schemaFile = SDF_ManagerApp.getXMLSchemaLocalFile();
            String schemaUrl = SDF_ManagerApp.getXMLSchemaURI();

            boolean schemaUrlBroken = Util.isUrlBroken(schemaUrl, false);
            if (schemaUrlBroken) {
                ExporterSiteXML.log.info("Schema URL broken (" + schemaUrl + "), trying with local file: " + schemaFile);
            }
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaUrlBroken ? schemaFactory.newSchema(schemaFile) : schemaFactory.newSchema(new URL(schemaUrl));

            ExporterSiteXML.log.info("Using this schema for validation: " + (schemaUrlBroken ? schemaFile : schemaUrl));

            Document doc = ExporterSiteXML.generateXML(session, this.sitecodes, schema);
            xmlOK = writeXmlFile(doc, this.fileName);

            /**
             * XML Validation
             */
            if (xmlOK) {
                Source xmlFile = new StreamSource(new File(this.fileName));

                Validator validator = schema.newValidator();


                final CustomErrorMessage customErrorMessage = new CustomErrorMessage().addMessage("cvc-complex-type.2.4.a: Invalid content was found starting with element 'spaLegalReference'.At least one SCI or SAC date must be provided (sciProposalDate, sciConfirmationDate or sacDesignationDate).")
                        .addMessageCondition(new KeywordsBasedCustomErrorMessageCondition(new String[]{"cvc-complex-type.2.4.a","spaLegalReference"
                                ,"spaClassificationDate"}));
                final ValidatorErrorMessagesPreparator validatorErrorMessagesPreparator = new ValidatorErrorMessagesPreparator().
                        setDisplayColumnNumber(true).setDisplayLineNumber(true).addCustomErrorMessage(customErrorMessage);

                // Error Handler
                validator.setErrorHandler(new ErrorHandler() {
                    @Override
                    public void warning(SAXParseException exception) throws SAXException {
                        xmlValidFields.add("< Line: " + exception.getLineNumber() + ", Column: " + exception.getColumnNumber()
                                + " > " + exception.getMessage());
                    }

                    @Override
                    public void fatalError(SAXParseException exception) throws SAXException {
                        xmlValidFields.add("< Line: " + exception.getLineNumber() + ", Column: " + exception.getColumnNumber()
                                + " > " + exception.getMessage());
                    }

                    @Override
                    public void error(SAXParseException exception) throws SAXException {
                        String customMessage = validatorErrorMessagesPreparator.prepareMessageBasedOnValidationException(exception);
                        if(customMessage!=null){
                            xmlValidFields.add(customMessage);
                        }
                        else {
                            xmlValidFields.add("< Line: " + exception.getLineNumber() + ", Column: " + exception.getColumnNumber()
                                    + " > " + exception.getMessage());
                        }
                    }
                });

                // Validate Call
                validator.validate(xmlFile);
            }

            this.finalizeWriter();
            System.gc();
        } catch (ParserConfigurationException e) {
            ExporterSiteXML.log.error("Error, parsing xml.Error::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);
            // e.printStackTrace();
        } catch (SAXException e) {
            ExporterSiteXML.log.error("Error, parsing xml.Error::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);
            // e.printStackTrace();
        } catch (Exception e) {
            ExporterSiteXML.log.error("Error, parsing xml.Error::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);
            // e.printStackTrace();
        } finally {
            session.close();
        }
        return xmlValidFields;

    }

    /**
     *
     * @param i
     * @return
     */
    public static Boolean toBool(Short i) {
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
    public static String fmt(String src, String fieldName) {
        /*
         * basically a debugging function, printing out null fields
         * but could be used for other purposes as well
         */
        if (src == null) {
            return "";
        } else {
            // return getString(src);
            return ConversionTools.replaceBadSymbols(src);
        }
    }

    /**
     *
     * @param date
     * @param fieldName
     * @return
     */
    public static String fmt(Date date, String fieldName) {
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
    public static String fmt(Double val, String fieldName) {
        if (val != null) {
            String strVal = "0.00";
            if (val != new Double(0)) {
                strVal = val.toString();
            }
            return fmt(strVal, fieldName);
        } else {
            return fmt("", fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    public static String fmt(Integer val, String fieldName) {
        if (val != null) {
            String strVal = "0";
            if (val != new Integer(0)) {
                strVal = val.toString();
            }
            return fmt(strVal, fieldName);
        } else {
            return fmt("", fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    public static String fmt(Boolean val, String fieldName) {
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
    public static String fmt(Character val, String fieldName) {
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
    public static String fmtToLowerCase(Character val, String fieldName) {
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
    public static String fmtToUpperCase(Character val, String fieldName) {
        if (val != null) {
            return fmt(val.toString().toUpperCase(), fieldName);
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
     * @param node
     */
    void writeXMLNode(Node node) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(node);
            Result result = new StreamResult(writer);
            // Write the DOM document to the file
            Transformer xformer;
            xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            xformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "string");
            xformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                    "siteName name otherSiteCharacteristics siteDesignation description"
                            + " qualityAndImportance conservationMeasures ");
            xformer.transform(source, result);
            if (++this.counter % 50 == 0) {
                // this.writer.flush();
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ExporterSiteXML.log.error("Error, writeXMLNode().Error::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);

        }

    }

    /**
     *
     * @param doc
     * @param filePath
     * @return
     */
    public static Boolean writeXmlFile(Document doc, String filePath) {

        FileOutputStream outputStream = null;
        try {
            // Prepare the DOM document for writing
            Source domSource = new DOMSource(doc);

            // Prepare the output file
            File file = new File(filePath);
            outputStream = new FileOutputStream(file);
            Result streamResult = new StreamResult(outputStream);

            // Write the DOM document to the file
            Transformer domTransformer = TransformerFactory.newInstance().newTransformer();

            domTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            domTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            domTransformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                    "siteName name otherSiteCharacteristics siteDesignation description"
                            + " qualityAndImportance conservationMeasures adminUnit locatorDesignator locatorName addressArea"
                            + " postName postCode thoroughfare addressUnstructured email spaLegalReference sacLegalReference"
                            + " explanations siteDesignationAdditional organisation conservationMeasures InspireID mapReference");

            domTransformer.transform(domSource, streamResult);
            return true;
        } catch (TransformerConfigurationException e) {
            ExporterSiteXML.log.error("Error, writeXMLNode().Error::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);

            return false;
        } catch (TransformerException e) {
            ExporterSiteXML.log.error("Error, writeXmlFile().Error::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            ExporterSiteXML.log.error("Error, writeXmlFile().Error::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     *
     * @param filename
     * @return
     */
    @Override
    public boolean processDatabase(String filename) {
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
            log("Failed extracting field: " + fieldName + ". The field could have an erroneous name. Please verify.", 2);
            // e.printStackTrace();
            ExporterSiteXML.log.error("Failed extracting field: " + fieldName
                    + ". The field could have an erroneous name. Please verify.\n.Error::" + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details",
                    "Dialog", JOptionPane.ERROR_MESSAGE);

            return null;
        }
    }

    /**
     *
     * @param i
     * @return
     */
    public static Boolean toBoolean(Short i) {
        if (i == null) {
            return null;
        }
        if (i > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Utility method that generates XML for sites by the given site-codes, following the given XML schema, and querying sites
     * info from the given Hibernate session.
     *
     * @param session Hibernate session.
     * @param sitecodes Site-codes
     * @param schema XML schema.
     * @return The generated DOM.
     * @throws Exception If any sort of error happens.
     */
    @SuppressWarnings("rawtypes")
    public static Document generateXML(Session session, ArrayList<String> sitecodes, Schema schema) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();

        dbfac.setSchema(schema);

        DocumentBuilder builder = dbfac.newDocumentBuilder();
        Document doc = builder.newDocument();

        Iterator<String> itrSites = sitecodes.iterator();
        int flush = 0;

        ExporterSiteXML.log.info("Parsing sitecodes...");

        Element sdfs = doc.createElement("sdfs");
        sdfs.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Package p = SDF_ManagerApp.class.getPackage();
        String version = p.getImplementationVersion();
        sdfs.setAttribute("version","SDF-Natura2000-V"+version  );
        String schemaUri = SDF_ManagerApp.getXMLSchemaURI();
        sdfs.setAttribute("xsi:noNamespaceSchemaLocation", schemaUri);

        doc.appendChild(sdfs);

        while (itrSites.hasNext()) {
            Element sdf = doc.createElement("sdf");
            Element siteIdentification = doc.createElement("siteIdentification");

            Site site = (Site) session.get(Site.class, itrSites.next()); // results.get(i);

            // log("Processing site: " + site.getSiteCode());
            ExporterSiteXML.log.info("Processing site: " + site.getSiteCode());

            if (site.getSiteType() != null && !(("").equals(site.getSiteType()))) {
                siteIdentification.appendChild(doc.createElement("siteType")).appendChild(
                        doc.createTextNode(fmt(Character.toString(site.getSiteType()), "siteType")));
            } else {
                // xmlValidFields.add("Site Type, in Identification section\n");
            }

            siteIdentification.appendChild(doc.createElement("siteCode")).appendChild(
                    doc.createTextNode(fmt(site.getSiteCode(), "siteCode")));

            if (site.getSiteName() != null && !(("").equals(site.getSiteName()))) {
                siteIdentification.appendChild(doc.createElement("siteName")).appendChild(
                        doc.createTextNode(fmt(site.getSiteName(), "siteName")));
            } else {
                // xmlValidFields.add("Site Name, in Identification section\n");
            }

            if (site.getSiteCompDate() != null) {
                siteIdentification.appendChild(doc.createElement("compilationDate")).appendChild(
                        doc.createTextNode(fmt(SDF_Util.getFormatDateToXML(site.getSiteCompDate()), "compilationDate")));
            } else {
                // xmlValidFields.add("Compilation Date, in Identification section\n");
            }

            if (site.getSiteUpdateDate() != null) {
                siteIdentification.appendChild(doc.createElement("updateDate")).appendChild(
                        doc.createTextNode(fmt(SDF_Util.getFormatDateToXML(site.getSiteUpdateDate()), "updateDate")));
            } else {
                // siteIdentification.appendChild(doc.createElement("updateDate")).appendChild(doc.createTextNode(SDF_Constants.NULL_DATE));
            }

            if (site.getSiteCompDate() != null && site.getSiteUpdateDate() != null) {
                if (site.getSiteCompDate().compareTo(site.getSiteUpdateDate()) > 0) {
                    // xmlValidFields.add("Compilation Date and Update Date, in  Identification section\n");
                }
            }

            Resp resp = site.getResp();
            if (resp != null) {
                Element respNode = doc.createElement("respondent");
                respNode.appendChild(doc.createElement("name")).appendChild(
                        doc.createTextNode(fmt(resp.getRespName(), "respName")));
                if (resp.getRespAddress() != null && !(("").equals(resp.getRespAddress()))) {

                    respNode.appendChild(doc.createElement("addressUnstructured")).appendChild(
                            doc.createTextNode(fmt(resp.getRespAddress(), "respAddress")));
                } else if (resp.getRespAdminUnit() != null && !(resp.getRespAdminUnit().equals(""))) {
                    Element addresElem = doc.createElement("address");
                    addresElem.appendChild(doc.createElement("adminUnit")).appendChild(
                            doc.createTextNode(fmt(resp.getRespAdminUnit(), "adminUnit")));
                    addresElem.appendChild(doc.createElement("locatorDesignator")).appendChild(
                            doc.createTextNode(fmt(resp.getRespLocatorDesig(), "locatorDesignator")));
                    addresElem.appendChild(doc.createElement("locatorName")).appendChild(
                            doc.createTextNode(fmt(resp.getRespLocatorName(), "locatorName")));
                    addresElem.appendChild(doc.createElement("addressArea")).appendChild(
                            doc.createTextNode(fmt(resp.getRespAddressArea(), "addressArea")));
                    addresElem.appendChild(doc.createElement("postName")).appendChild(
                            doc.createTextNode(fmt(resp.getRespPostName(), "postName")));
                    addresElem.appendChild(doc.createElement("postCode")).appendChild(
                            doc.createTextNode(fmt(resp.getRespPostCode(), "postCode")));
                    addresElem.appendChild(doc.createElement("thoroughfare")).appendChild(
                            doc.createTextNode(fmt(resp.getRespThoroughFare(), "thoroughfare")));
                    respNode.appendChild(addresElem);

                } else {
                    // xmlValidFields.add("Address in Identification section (Respondent tab)\n");
                }

                respNode.appendChild(doc.createElement("email")).appendChild(
                        doc.createTextNode(fmt(resp.getRespEmail(), "respEmail")));
                if (resp.getRespName() == null || (("").equals(resp.getRespName()))) {
                    if (resp.getRespEmail() == null || (("").equals(resp.getRespEmail()))) {
                        // xmlValidFields.add("Name or Email in Identification section (Respondent tab)\n");
                    }
                }

                siteIdentification.appendChild(respNode);
            }

            String siteType = null;
            if (site.getSiteType() != null) { 
            	siteType = Character.toString(site.getSiteType());
            } else {
            	siteType = null;
            }

            boolean isEmeraldMode = SDF_ManagerApp.isEmeraldMode();

            // Site classification dates and explanation.

            if (isEmeraldMode) {
                XmlGenerationUtils.appendDateElement(site.getSiteProposedAsciDate(), siteIdentification, "asciProposalDate", doc);
                if (("B").equals(siteType) && site.getSiteProposedAsciDate() == null) {
                    siteIdentification.appendChild(doc.createElement("asciProposalDate")).appendChild(
                            doc.createTextNode(fmt("0000-00", "asciProposalDate")));
                }
                XmlGenerationUtils.appendDateElement(site.getSiteConfirmedCandidateAsciDate(), siteIdentification,
                        "asciCandidateConfirmationDate", doc);
                XmlGenerationUtils.appendDateElement(site.getSiteConfirmedAsciDate(), siteIdentification, "asciConfirmationDate",
                        doc);
                XmlGenerationUtils.appendDateElement(site.getSiteDesignatedAsciDate(), siteIdentification, "asciDesignationDate",
                        doc);

                siteIdentification.appendChild(doc.createElement("asciDesignationLegalReference")).appendChild(
                        doc.createTextNode(fmt(site.getSiteAsciLegalRef(), "asciDesignationLegalReference")));
            } else {
                XmlGenerationUtils.appendDateElement(site.getSiteSpaDate(), siteIdentification, "spaClassificationDate", doc);
              /**  if (("B").equals(siteType) && site.getSiteSpaDate() == null) {
                    siteIdentification.appendChild(doc.createElement("spaClassificationDate")).appendChild(
                            doc.createTextNode(fmt("0000-00", "spaClassificationDate")));
                }
               **/
              if(site.getSiteSpaLegalRef()!=null &&!site.getSiteSpaLegalRef().equals("")) {
                  siteIdentification.appendChild(doc.createElement("spaLegalReference")).appendChild(
                          doc.createTextNode(fmt(site.getSiteSpaLegalRef(), "spaLegalReference")));
              }
                XmlGenerationUtils.appendDateElement(site.getSiteSciPropDate(), siteIdentification, "sciProposalDate", doc);
                XmlGenerationUtils.appendDateElement(site.getSiteSciConfDate(), siteIdentification, "sciConfirmationDate", doc);
                XmlGenerationUtils.appendDateElement(site.getSiteSacDate(), siteIdentification, "sacDesignationDate", doc);

                siteIdentification.appendChild(doc.createElement("sacLegalReference")).appendChild(
                        doc.createTextNode(fmt(site.getSiteSacLegalRef(), "sacLegalReference")));
            }

            siteIdentification.appendChild(doc.createElement("explanations")).appendChild(
                    doc.createTextNode(fmt(site.getSiteExplanations(), "explanations")));
            sdf.appendChild(siteIdentification);

            // Site location info.

            Element location = doc.createElement("siteLocation");
            location.appendChild(doc.createElement("longitude")).appendChild(
                    doc.createTextNode(fmt(site.getSiteLongitude(), "longitude")));
            location.appendChild(doc.createElement("latitude")).appendChild(
                    doc.createTextNode(fmt(site.getSiteLatitude(), "latitude")));
            location.appendChild(doc.createElement("area")).appendChild(doc.createTextNode(fmt(site.getSiteArea(), "area")));

            location.appendChild(doc.createElement("marineAreaPercentage")).appendChild(
                    doc.createTextNode(fmt(site.getSiteMarineArea(), "marineArea")));

            if (site.getSiteLength() != null) {
            	location.appendChild(doc.createElement("siteLength")).appendChild(
            			doc.createTextNode(fmt(site.getSiteLength(), "siteLength")));
            }

            // Bio-regions info.

            Element regions = doc.createElement("adminRegions");
            Set siteRegions = site.getRegions();
            if (!(siteRegions.isEmpty())) {
                Iterator itr = siteRegions.iterator();
                while (itr.hasNext()) {
                    Region r = (Region) itr.next();
                    Element rElem = doc.createElement("region");
                    rElem.appendChild(doc.createElement("code")).appendChild(
                            doc.createTextNode(fmt(r.getRegionCode(), "regionCode")));
                    rElem.appendChild(doc.createElement("name")).appendChild(
                            doc.createTextNode(fmt(r.getRegionName(), "regionName")));
                    regions.appendChild(rElem);
                }
                location.appendChild(regions);
            } else {
                // xmlOK = false;
                // xmlValidFields.add("Administrative Region code and name (NUTS) in Location section)\n");
            }
            location.appendChild(regions);

            Set siteBioRegions = site.getSiteBiogeos();
            if (!(siteBioRegions.isEmpty())) {
                Iterator itbr = siteBioRegions.iterator();
                while (itbr.hasNext()) {
                    SiteBiogeo s = (SiteBiogeo) itbr.next();
                    Element biogeoElement = doc.createElement("biogeoRegions");
                    Biogeo b = s.getBiogeo();
                    biogeoElement.appendChild(doc.createElement("code")).appendChild(
                            doc.createTextNode(fmt(b.getBiogeoCode(), "bioRegionCode")));
                    
                    if (s.getBiogeoPercent() != null) {
                    	biogeoElement.appendChild(doc.createElement("percentage")).appendChild(
                    			doc.createTextNode(fmt(s.getBiogeoPercent(), "biogeoPercent")));
                    }
                    
                    location.appendChild(biogeoElement);
                }

            } else {
                // xmlValidFields.add("Biogeographical Region in Location section)\n");
            }

            sdf.appendChild(location);

            // Site ecological information.

            Element ecologicalInformation = doc.createElement("ecologicalInformation");

            // Site habitat types.

            boolean habitatInfo = false;
			if (site.getHabitats().size() > 0) {
				Element habitatsTypes = doc.createElement("habitatTypes");

				Set siteHabs = site.getHabitats();
				Iterator itr = siteHabs.iterator();
				
				
				if (!siteHabs.isEmpty()) {
					habitatInfo = true;
				}
				while (itr.hasNext()) {
					Habitat h = (Habitat) itr.next();
					Element hElem = doc.createElement("habitatType");
					if (h.getHabitatCode() != null && !(h.getHabitatCode().equals(""))) {
						hElem.appendChild(doc.createElement("code"))
								.appendChild(doc.createTextNode(fmt(h.getHabitatCode(), "habitatCode")));
					} else {
						// xmlValidFields.add("The code of the habitat.
						// (Ecological Info-Habitat Type section)\n");
					}

					boolean dAssesment = false;
					boolean representativityNotNull = false;
					if (h.getHabitatRepresentativity() != null
							&& !(("-").equals(h.getHabitatRepresentativity().toString()))) {
						if (("D").equals(h.getHabitatRepresentativity().toString())) {
							dAssesment = true;
						}
						representativityNotNull = true;
					} else {
						// xmlValidFields.add("Representativity of the habitat
						// whose code is: " +
						// h.getHabitatCode()+". (Ecological Info-Habitat Type
						// section)\n");
					}

					if (h.getHabitatPriority() != null) {
						hElem.appendChild(doc.createElement("priorityFormOfHabitatType")).appendChild(
								doc.createTextNode(fmt(toBoolean(h.getHabitatPriority()), "habitatPriority")));
					}
					if (h.getHabitatNp() != null) {
						hElem.appendChild(doc.createElement("nonpresentInSite")).appendChild(
								doc.createTextNode(fmt(toBoolean(h.getHabitatNp()), "habitatNp")));
					}
					if (h.getHabitatCoverHa() != null) {
						hElem.appendChild(doc.createElement("coveredArea")).appendChild(
								doc.createTextNode(fmt(h.getHabitatCoverHa(), "habitatCover")));
					}
					if (h.getHabitatCaves() != null) {
						hElem.appendChild(doc.createElement("caves")).appendChild(
								doc.createTextNode(fmt(h.getHabitatCaves(), "habitatCaves")));
					}
					if (h.getHabitatDataQuality() != null) {
						hElem.appendChild(doc.createElement("observationDataQuality"))
								.appendChild(doc.createTextNode(fmt(h.getHabitatDataQuality(), "habitatDataQuality")));
					} else {
						if (!dAssesment) {
							// xmlValidFields.add("Data Quality of the habitat
							// whose code is: " +
							// h.getHabitatCode()+". (Ecological Info-Habitat
							// Type section)\n");
						} else {
							hElem.appendChild(doc.createElement("observationDataQuality"))
									.appendChild(doc.createTextNode(fmt("-", "habitatDataQuality")));
						}
					}

					if (representativityNotNull) {
						hElem.appendChild(doc.createElement("representativity")).appendChild(
								doc.createTextNode(fmt(h.getHabitatRepresentativity(), "habitatRepresentativity")));
					}

					if (h.getHabitatRelativeSurface() != null) {
						hElem.appendChild(doc.createElement("relativeSurface"))
								.appendChild(doc.createTextNode(fmt(h.getHabitatRelativeSurface(), "relativeSurface")));
					} else {
						if (!dAssesment) {
							// xmlValidFields.add("Relative Surface of the
							// habitat whose code is: " +
							// h.getHabitatCode()+". (Ecological Info-Habitat
							// Type section)\n");
						} else {
							hElem.appendChild(doc.createElement("relativeSurface"))
									.appendChild(doc.createTextNode(fmt("-", "relativeSurface")));
						}
					}

					if (h.getHabitatConservation() != null && !(h.getHabitatConservation().equals("-"))) {
						hElem.appendChild(doc.createElement("conservation")).appendChild(
								doc.createTextNode(fmt(h.getHabitatConservation(), "habitatConservation")));
					} else {
						if (!dAssesment) {
							// xmlValidFields.add("Conservation of the habitat
							// whose code is: " +
							// h.getHabitatCode()+". (Ecological Info-Habitat
							// Type section)\n");
						} else {
							hElem.appendChild(doc.createElement("conservation"))
									.appendChild(doc.createTextNode(fmt("-", "habitatConservation")));
						}

					}

					if (h.getHabitatGlobal() != null && !(h.getHabitatGlobal().equals("-"))) {
						hElem.appendChild(doc.createElement("global"))
								.appendChild(doc.createTextNode(fmt(h.getHabitatGlobal(), "habitatGlobal")));
					} else {
						if (!dAssesment) {
							// xmlValidFields.add("Global of the habitat whose
							// code is: " +
							// h.getHabitatCode()+". (Ecological Info-Habitat
							// Type section)\n");
						} else {
							hElem.appendChild(doc.createElement("global"))
									.appendChild(doc.createTextNode(fmt("-", "habitatGlobal")));
						}
					}

					habitatsTypes.appendChild(hElem);
				}
				ecologicalInformation.appendChild(habitatsTypes);
			}
            // Site species info.
			boolean speciesInfo = false;
            Element specieses = doc.createElement("species");
            Set siteSpecies = site.getSpecieses();
            boolean birdsSPA = false;
            if (!siteSpecies.isEmpty()) {
                Iterator itsr = siteSpecies.iterator();
                while (itsr.hasNext()) {
                    Species s = (Species) itsr.next();
                    Element sElem = doc.createElement("speciesPopulation");

                    if (s.getSpeciesGroup() != null && s.getSpeciesGroup() != '-') {
                        if ((s.getSpeciesGroup().toString()).equals("B")) {
                            birdsSPA = true;
                        }
                        sElem.appendChild(doc.createElement("speciesGroup")).appendChild(
                                doc.createTextNode(fmt(s.getSpeciesGroup(), "speciesGroup")));
                    } else {
                        // xmlValidFields.add("Group of the species. (Ecological Info - Species Type  section)\n");
                    }

                    String speciesCode = "";
                    if (s.getSpeciesCode() != null) {
                        speciesCode = s.getSpeciesCode();

                    }
                    sElem.appendChild(doc.createElement("speciesCode")).appendChild(
                            doc.createTextNode(fmt(speciesCode, "speciesCode")));
                    if (s.getSpeciesName() != null && !(s.getSpeciesName().equals(""))) {
                        sElem.appendChild(doc.createElement("scientificName")).appendChild(
                                doc.createTextNode(fmt(s.getSpeciesName(), "speciesName")));
                    } else {
                        // xmlValidFields.add("Scientific name of the species for the code : " +
                        // s.getSpeciesCode()+", species name : " + s.getSpeciesName()+" and the group :" +
                        // s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                    }

                    if (s.getSpeciesSensitive() != null) {
                    	sElem.appendChild(doc.createElement("sensitiveInfo")).appendChild(
                    			doc.createTextNode(fmt(toBoolean(s.getSpeciesSensitive()), "speciesSensitive")));
                    }
                    if (s.getSpeciesNp() != null) {
                    	sElem.appendChild(doc.createElement("nonpresentInSite")).appendChild(
                    			doc.createTextNode(fmt(toBoolean(s.getSpeciesNp()), "speciesNP")));
                    }
                    
                    boolean dAssesment = false;
                    if (s.getSpeciesPopulation() != null && ("D").equals(s.getSpeciesPopulation())) {
                        dAssesment = true;
                    }
                    sElem.appendChild(doc.createElement("populationType")).appendChild(
                            doc.createTextNode(fmtToLowerCase(s.getSpeciesType(), "speciesType")));

                    Element popElem = doc.createElement("populationSize");
                    popElem.appendChild(doc.createElement("lowerBound")).appendChild(
                            doc.createTextNode(fmt(s.getSpeciesSizeMin(), "speciesSizeMin")));
                    popElem.appendChild(doc.createElement("upperBound")).appendChild(
                            doc.createTextNode(fmt(s.getSpeciesSizeMax(), "speciesSizeMax")));
                    popElem.appendChild(doc.createElement("countingUnit")).appendChild(
                            doc.createTextNode(fmt(s.getSpeciesUnit(), "speciesUnit")));
                    sElem.appendChild(popElem);

                    if (s.getSpeciesCategory() != null) {
                        sElem.appendChild(doc.createElement("abundanceCategory")).appendChild(
                                doc.createTextNode(fmtToUpperCase(s.getSpeciesCategory(), "speciesCategory")));
                    }

                    if (s.getSpeciesDataQuality() != null && !(s.getSpeciesDataQuality().equals(""))) {
                        sElem.appendChild(doc.createElement("observationDataQuality")).appendChild(
                                doc.createTextNode(fmt(s.getSpeciesDataQuality(), "speciesQuality")));
                    } else {
                        if (dAssesment) {
                            sElem.appendChild(doc.createElement("observationDataQuality")).appendChild(
                                    doc.createTextNode(fmt("-", "speciesQuality")));
                        } else {
                            // xmlValidFields.add("Data Quality of the species for the code : " +
                            // s.getSpeciesCode()+", species name : " + s.getSpeciesName()+" and the group :" +
                            // s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                        }
                    }

                    sElem.appendChild(doc.createElement("population")).appendChild(
                            doc.createTextNode(fmt(s.getSpeciesPopulation(), "speciesPopulation")));
                    if (s.getSpeciesConservation() != null && s.getSpeciesConservation() != '-') {
                        sElem.appendChild(doc.createElement("conservation")).appendChild(
                                doc.createTextNode(fmt(s.getSpeciesConservation(), "speciesConservation")));
                    } else {
                        if (dAssesment) {
                            sElem.appendChild(doc.createElement("conservation")).appendChild(
                                    doc.createTextNode(fmt("-", "speciesConservation")));
                        } else {
                            // xmlValidFields.add("Conservation of the species for the code : " +
                            // s.getSpeciesCode()+", species name : " + s.getSpeciesName()+" and the group :" +
                            // s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                        }

                    }

                    if (s.getSpeciesIsolation() != null && s.getSpeciesIsolation() != '-') {
                        sElem.appendChild(doc.createElement("isolation")).appendChild(
                                doc.createTextNode(fmt(s.getSpeciesIsolation(), "speciesIsolation")));
                    } else {
                        if (dAssesment) {
                            sElem.appendChild(doc.createElement("isolation")).appendChild(
                                    doc.createTextNode(fmt("-", "speciesIsolation")));
                        } else {
                            // xmlValidFields.add("Isolation of the species for the code : " +
                            // s.getSpeciesCode()+", species name : " + s.getSpeciesName()+" and the group :" +
                            // s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                        }

                    }

                    if (s.getSpeciesGlobal() != null && s.getSpeciesGlobal() != '-') {
                        sElem.appendChild(doc.createElement("global")).appendChild(
                                doc.createTextNode(fmt(s.getSpeciesGlobal(), "speciesGlobal")));
                    } else {
                        if (dAssesment) {
                            sElem.appendChild(doc.createElement("global")).appendChild(
                                    doc.createTextNode(fmt("-", "speciesGlobal")));
                        } else {
                            // xmlValidFields.add("Global of the species for the code : " + s.getSpeciesCode()+", species name : " +
                            // s.getSpeciesName()+" and the group :" +
                            // s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                        }

                    }
                    specieses.appendChild(sElem);
                }
                speciesInfo = true;
            }

            Set siteOtherSpecies = site.getOtherSpecieses();
            if (!siteOtherSpecies.isEmpty()) {
                Iterator itosr = siteOtherSpecies.iterator();
                while (itosr.hasNext()) {
                    OtherSpecies s = (OtherSpecies) itosr.next();
                    Element sElem = doc.createElement("speciesPopulation");

                    if (s.getOtherSpeciesGroup() != null && !(s.getOtherSpeciesGroup().equals("-"))) {
                        sElem.appendChild(doc.createElement("speciesGroup")).appendChild(
                                doc.createTextNode(fmt(s.getOtherSpeciesGroup(), "ospeciesGroup")));
                    } else {
                        // xmlValidFields.add("Group of the species (no cataloged species). (Ecological Info - Other Species Type  section)\n");
                    }

                    String speciesCode = "";
                    if (s.getOtherSpeciesCode() != null) {
                        speciesCode = s.getOtherSpeciesCode();
                    }
                    sElem.appendChild(doc.createElement("speciesCode")).appendChild(
                            doc.createTextNode(fmt(speciesCode, "ospeciesCode")));

                    if (s.getOtherSpeciesName() != null && !(s.getOtherSpeciesName().equals(""))) {
                        sElem.appendChild(doc.createElement("scientificName")).appendChild(
                                doc.createTextNode(fmt(s.getOtherSpeciesName(), "ospeciesName")));
                    } else {
                        // xmlValidFields.add("Scientific name of the species (no cataloged species) for the code : " +
                        // s.getOtherSpeciesCode()+", species name : " + s.getOtherSpeciesName()+" and the group : " +
                        // s.getOtherSpeciesGroup()+". . (Ecological Info - Other Species Type  section)\n");
                    }

                    if (s.getOtherSpeciesSensitive() != null) {
                    	sElem.appendChild(doc.createElement("sensitiveInfo")).appendChild(
                    			doc.createTextNode(fmt(toBoolean(s.getOtherSpeciesSensitive()), "ospeciesSensitive")));
                    }

                    if (s.getOtherSpeciesNp() != null && !(("").equals(s.getOtherSpeciesNp()))) {
                        sElem.appendChild(doc.createElement("nonpresentInSite")).appendChild(
                                doc.createTextNode(fmt(toBoolean(s.getOtherSpeciesNp()), "ospeciesNP")));
                    }

                    Element popElem = doc.createElement("populationSize");
                    popElem.appendChild(doc.createElement("lowerBound")).appendChild(
                            doc.createTextNode(fmt(s.getOtherSpeciesSizeMin(), "speciesSizeMin")));
                    popElem.appendChild(doc.createElement("upperBound")).appendChild(
                            doc.createTextNode(fmt(s.getOtherSpeciesSizeMax(), "speciesSizeMax")));
                    popElem.appendChild(doc.createElement("countingUnit")).appendChild(
                            doc.createTextNode(fmt(s.getOtherSpeciesUnit(), "speciesUnit")));
                    sElem.appendChild(popElem);
                    if (s.getOtherSpeciesCategory() != null && s.getOtherSpeciesCategory() != '-') {
                        sElem.appendChild(doc.createElement("abundanceCategory")).appendChild(
                                doc.createTextNode(fmt(s.getOtherSpeciesCategory(), "ospeciesCategory")));
                    }

                    // modificar porque es un tree primero es motivations y despues el nodo motivation (solo en el caso que haya
                    // motivations es other species en caso contrario
                    // es species
                    if (s.getOtherSpeciesMotivation() != null && !(("").equals(s.getOtherSpeciesMotivation()))) {
                        Element sElemMot = doc.createElement("motivations");

                        String strMotivation = s.getOtherSpeciesMotivation();

                        StringTokenizer st2 = new StringTokenizer(strMotivation, ",");

                        while (st2.hasMoreElements()) {
                            String mot = (String) st2.nextElement();
                            sElemMot.appendChild(doc.createElement("motivation")).appendChild(
                                    doc.createTextNode(fmt(mot, "ospeciesMotivation")));
                            sElem.appendChild(sElemMot);
                        }
                    }
                    specieses.appendChild(sElem);
                }
            }

            if (!habitatInfo && !speciesInfo) {
                // xmlValidFields.add("No habitats, species. (Ecological Info)\n");
            }

            //if ("A".equals(siteType) || "C".equals(siteType)) {
            //    if (!birdsSPA) {
                    // xmlValidFields.add("No birds in SPA site. (Ecological Info)\n");
            //    }
            //}

            ecologicalInformation.appendChild(specieses);

            sdf.appendChild(ecologicalInformation);

            /************** DESCRIPTION ***********************/
            Element description = doc.createElement("siteDescription");
            Set classes = site.getHabitatClasses();
            Iterator ithr = classes.iterator();
            while (ithr.hasNext()) {
                HabitatClass h = (HabitatClass) ithr.next();
                Element cElem = doc.createElement("habitatClass");
                cElem.appendChild(doc.createElement("code")).appendChild(
                        doc.createTextNode(fmt(h.getHabitatClassCode(), "habitatClassCode")));
                cElem.appendChild(doc.createElement("coveragePercentage")).appendChild(
                        doc.createTextNode(fmt(h.getHabitatClassCover(), "habitatClassCover")));
                description.appendChild(cElem);
            }

            description.appendChild(doc.createElement("otherSiteCharacteristics")).appendChild(
                    doc.createTextNode(fmt(site.getSiteCharacteristics(), "otherSiteCharacteristics")));
            description.appendChild(doc.createElement("qualityAndImportance")).appendChild(
                    doc.createTextNode(fmt(site.getSiteQuality(), "qualityAndImportance")));
            Element impacts = doc.createElement("impacts");

            Set siteImpacts = site.getImpacts();
            boolean posImpact = false;
            boolean negImpact = false;
            if (!siteImpacts.isEmpty()) {
                Iterator itir = siteImpacts.iterator();

                while (itir.hasNext()) {
                    Element iElem = doc.createElement("impact");
                    Impact im = (Impact) itir.next();

                    if (im.getImpactCode() != null && !(im.getImpactCode().equals(""))) {
                        iElem.appendChild(doc.createElement("impactCode")).appendChild(
                                doc.createTextNode(fmt(im.getImpactCode(), "impactCode")));
                    } else {
                        // xmlValidFields.add("Impact code. (Description - Pressures and Threads section)\n");
                    }

                    if (im.getImpactRank() != null && !(im.getImpactRank().equals("-"))) {
                        iElem.appendChild(doc.createElement("rank")).appendChild(
                                doc.createTextNode(fmt(im.getImpactRank(), "impactRank")));
                    } else {
                        // xmlValidFields.add("Rank of the impact whose code is: " +
                        // im.getImpactCode()+". (Description - Pressures and Threads section)\n");
                    }

                    if (im.getImpactPollutionCode() != null && !(("").equals(im.getImpactPollutionCode()))) {
                        iElem.appendChild(doc.createElement("pollutionCode")).appendChild(
                                doc.createTextNode(fmt(im.getImpactPollutionCode(), "impactPollution")));
                    }

                    if (im.getImpactOccurrence() != null && !(im.getImpactOccurrence().equals("-"))) {
                        iElem.appendChild(doc.createElement("occurrence")).appendChild(
                                doc.createTextNode(fmt(im.getImpactOccurrence(), "impactOccurrece")));
                    } else {
                        // xmlValidFields.add("Occurence of the impact whose code is: " +
                        // im.getImpactCode()+". (Description - Pressures and Threads section)\n");
                    }

                    String impacType = "";
                    if (im.getImpactType() != null && (im.getImpactType().toString()).equals("P")) {
                        impacType = "Positive";
                        posImpact = true;
                    } else if (im.getImpactType() != null && (im.getImpactType().toString()).equals("N")) {
                        negImpact = true;
                        impacType = "Negative";
                    } else {

                    }
                    iElem.appendChild(doc.createElement("natureOfImpact")).appendChild(
                            doc.createTextNode(fmt(impacType, "natureOfImpact")));
                    impacts.appendChild(iElem);
                }
                description.appendChild(impacts);
                if (!posImpact || !negImpact) {
                    // xmlValidFields.add("There has to be at least one positive and negative impact.\n");
                }
            } else {
                // xmlValidFields.add("Impacts. (Description - Pressures and Threads section)\n");
            }

            Element ownership = doc.createElement("ownership");
            Set owners = site.getSiteOwnerships();
            if (!owners.isEmpty()) {
                Iterator itor = owners.iterator();
                while (itor.hasNext()) {
                    SiteOwnership o = (SiteOwnership) itor.next();
                    Ownership o2 = o.getOwnership();
                    Element oElem = doc.createElement("ownershipPart");

                    if (o2.getOwnershipCode() != null && !(o2.getOwnershipCode().equals("-"))) {
                        oElem.appendChild(doc.createElement("ownershiptype")).appendChild(
                                doc.createTextNode(fmt(o2.getOwnershipCode(), "ownershipType")));
                    } else {
                        // xmlValidFields.add("Type of Ownership. (Description - Documentation section)\n");
                    }

                    oElem.appendChild(doc.createElement("percent")).appendChild(
                            doc.createTextNode(fmt(o.getOwnershipPercent(), "ownershipPercent")));
                    ownership.appendChild(oElem);
                }

            }
            description.appendChild(ownership);

            Element documentation = doc.createElement("documentation");
            Doc docObj = site.getDoc();
            if (docObj != null) {
                documentation.appendChild(doc.createElement("description")).appendChild(
                        doc.createTextNode(fmt(docObj.getDocDescription(), "docDescription")));
                Set docLinks = docObj.getDocLinks();
                Iterator itdocr = docLinks.iterator();
                Element links = doc.createElement("links");
                while (itdocr.hasNext()) {
                    DocLink docLink = (DocLink) itdocr.next();
                    links.appendChild(doc.createElement("link")).appendChild(
                            doc.createTextNode(fmt(docLink.getDocLinkUrl(), "linkURL")));
                }
                documentation.appendChild(links);
                description.appendChild(documentation);
            }
            sdf.appendChild(description);
            /******** PROTECTION **********/
            Element protection = doc.createElement("siteProtection");

            Element natDesigs = doc.createElement("nationalDesignations");
            Set dsigs = site.getNationalDtypes();
            if (!dsigs.isEmpty()) {
                Iterator itdr = dsigs.iterator();
                while (itdr.hasNext()) {
                    NationalDtype dtype = (NationalDtype) itdr.next();
                    Element nElem = doc.createElement("nationalDesignation");
                    if (dtype.getNationalDtypeCode() != null && !(dtype.getNationalDtypeCode().equals("-"))) {
                        nElem.appendChild(doc.createElement("designationCode")).appendChild(
                                doc.createTextNode(fmt(dtype.getNationalDtypeCode(), "dtypecode")));
                    } else {
                        // xmlValidFields.add("Designation code of the Nationals Designations. (Protection Status - Designation Types section)\n");
                    }

                    nElem.appendChild(doc.createElement("cover")).appendChild(
                            doc.createTextNode(fmt(dtype.getNationalDtypeCover(), "dtypecover")));
                    natDesigs.appendChild(nElem);
                }

            }
            protection.appendChild(natDesigs);

            Set rels = site.getSiteRelations();
            Element relations = doc.createElement("relations");
            Element nationalRelations = doc.createElement("nationalRelationships");
            Element internationalRelations = doc.createElement("internationalRelationships");
            if (!rels.isEmpty()) {

                Iterator itre = rels.iterator();
                while (itre.hasNext()) {
                    SiteRelation rel = (SiteRelation) itre.next();
                    Element rElem;
                    Character scope = rel.getSiteRelationScope();
                    if (("N").equals(scope.toString())) {
                        rElem = doc.createElement("nationalRelationship");
                        rElem.appendChild(doc.createElement("designationCode")).appendChild(
                                doc.createTextNode(fmt(rel.getSiteRelationCode(), "relationCode")));
                        nationalRelations.appendChild(rElem);
                    } else if (("I").equals(scope.toString())) {
                        rElem = doc.createElement("internationalRelationship");
                        rElem.appendChild(doc.createElement("convention")).appendChild(
                                doc.createTextNode(fmt(rel.getSiteRelationConvention(), "relationConvention")));
                        internationalRelations.appendChild(rElem);
                    } else {
                        // log("Relation type undefined, ignoring relation: " + scope.toString());
                        continue;
                    }

                    if (rel.getSiteRelationSitename() != null && !(rel.getSiteRelationSitename().equals(""))) {
                        rElem.appendChild(doc.createElement("siteName")).appendChild(
                                doc.createTextNode(fmt(rel.getSiteRelationSitename(), "relationSite")));
                    } else {
                        // xmlValidFields.add("Site Name of the Relation National Site. (Protection Status - Relation with other sites section)\n ");
                    }

                    if (rel.getSiteRelationType() != null && !(rel.getSiteRelationType().equals("-"))) {
                        rElem.appendChild(doc.createElement("type")).appendChild(
                                doc.createTextNode(fmt(rel.getSiteRelationType(), "relationType")));
                    } else {
                        // xmlValidFields.add("Relation National Site whose site name is: " +
                        // rel.getSiteRelationSitename()+". . (Protection Status - Relation with other sites section)\n ");
                    }

                    rElem.appendChild(doc.createElement("cover")).appendChild(
                            doc.createTextNode(fmt(rel.getSiteRelationCover(), "relationCover")));
                }
            }
            relations.appendChild(nationalRelations);
            relations.appendChild(internationalRelations);
            protection.appendChild(relations);
            protection.appendChild(doc.createElement("siteDesignationAdditional")).appendChild(
                    doc.createTextNode(fmt(site.getSiteDesignation(), "siteDesignation")));
            sdf.appendChild(protection);
            /****************** MANAGEMENT ************************/
            Element mgmtElem = doc.createElement("siteManagement");
            Mgmt mgmt = site.getMgmt();
            if (mgmt != null) {
                /*** Mangement Body **/
                Set bodies = mgmt.getMgmtBodies();
                Iterator itrbody = bodies.iterator();
                Element bodiesElem = doc.createElement("managementBodies");
                while (itrbody.hasNext()) {
                    MgmtBody bodyObj = (MgmtBody) itrbody.next();
                    Element bElem = doc.createElement("managementBody");

                    if (bodyObj.getMgmtBodyOrg() != null && !(bodyObj.getMgmtBodyOrg().equals(""))) {
                        bElem.appendChild(doc.createElement("organisation")).appendChild(
                                doc.createTextNode(fmt(bodyObj.getMgmtBodyOrg(), "mgmtBodyOrg")));
                    } else {
                        // xmlValidFields.add("Organisation of the Management Bodies. (Management section)\n");
                    }

                    // if el campo addressunestructured esta vacio entonces addres es un tipo complejo (implementar) en caso
                    // contrario
                    if (bodyObj.getMgmtBodyAdminUnit() != null && !(bodyObj.getMgmtBodyAdminUnit().equals(""))) {
                        Element addresElem = doc.createElement("address");
                        if (bodyObj.getMgmtBodyAdminUnit() != null && !(bodyObj.getMgmtBodyAdminUnit().equals(""))) {
                            addresElem.appendChild(doc.createElement("adminUnit")).appendChild(
                                    doc.createTextNode(fmt(bodyObj.getMgmtBodyAdminUnit(), "adminUnit")));
                        } else {
                            // xmlValidFields.add("Address of the Management Bodies. (Management section)\n");
                        }

                        addresElem.appendChild(doc.createElement("locatorDesignator")).appendChild(
                                doc.createTextNode(fmt(bodyObj.getMgmtBodyLocatorDesignator(), "locatorDesignator")));
                        addresElem.appendChild(doc.createElement("locatorName")).appendChild(
                                doc.createTextNode(fmt(bodyObj.getMgmtBodyLocatorName(), "locatorName")));
                        addresElem.appendChild(doc.createElement("addressArea")).appendChild(
                                doc.createTextNode(fmt(bodyObj.getMgmtBodyAddressArea(), "addressArea")));
                        addresElem.appendChild(doc.createElement("postName")).appendChild(
                                doc.createTextNode(fmt(bodyObj.getMgmtBodyPostName(), "postName")));
                        addresElem.appendChild(doc.createElement("postCode")).appendChild(
                                doc.createTextNode(fmt(bodyObj.getMgmtBodyPostName(), "postCode")));
                        addresElem.appendChild(doc.createElement("thoroughfare")).appendChild(
                                doc.createTextNode(fmt(bodyObj.getMgmtBodyThroughFare(), "thoroughfare")));
                        bElem.appendChild(addresElem);
                    } else if (bodyObj.getMgmtBodyAddress() != null && !(bodyObj.getMgmtBodyAddress().equals(""))) {
                        bElem.appendChild(doc.createElement("addressUnstructured")).appendChild(
                                doc.createTextNode(fmt(bodyObj.getMgmtBodyAddress(), "addressUnstructured")));
                    } else {
                        // xmlValidFields.add("Address of the Management Bodies. (Management section)\n");
                    }

                    bElem.appendChild(doc.createElement("email")).appendChild(
                            doc.createTextNode(fmt(bodyObj.getMgmtBodyEmail(), "mgmtBodyMail")));
                    bodiesElem.appendChild(bElem);
                }
                mgmtElem.appendChild(bodiesElem);

                /*** Mangement Plan **/
                Character status = null;
                if (mgmt.getMgmtStatus() != null) {
                    status = mgmt.getMgmtStatus();
                } else {
                    status = 'N';
                }

                Element mgmtExists = (Element) mgmtElem.appendChild(doc.createElement("exists"));
                if (status == null || ("").equals(status)) {
                    status = 'N';
                }

                mgmtExists.appendChild(doc.createTextNode(status.toString()));
                Set plans = mgmt.getMgmtPlans();
                if (!plans.isEmpty()) {
                    Iterator itpr = plans.iterator();
                    Element plansElem = doc.createElement("managementPlans");
                    plansElem.appendChild(mgmtExists);
                    while (itpr.hasNext()) {
                        MgmtPlan planObj = (MgmtPlan) itpr.next();
                        Element pElem = doc.createElement("managementPlan");
                        pElem.appendChild(doc.createElement("name")).appendChild(
                                doc.createTextNode(fmt(planObj.getMgmtPlanName(), "mgmtPlanName")));
                        pElem.appendChild(doc.createElement("url")).appendChild(
                                doc.createTextNode(fmt(planObj.getMgmtPlanUrl(), "mgmtPlanUrl")));
                        plansElem.appendChild(pElem);
                    }
                    mgmtElem.appendChild(plansElem);
                } else if (status != null && status.toString().toUpperCase().equals("Y") && plans.isEmpty()) {
                    // xmlValidFields.add("Management Plans. (Management section)\n");
                }

                mgmtElem.appendChild(doc.createElement("conservationMeasures")).appendChild(
                        doc.createTextNode(fmt(mgmt.getMgmtConservMeasures(), "conservationMeasures")));
            } else {
                // xmlValidFields.add("Management. (Management section)\n");
            }

            sdf.appendChild(mgmtElem);
            Map map = site.getMap();
            Element mapElem = doc.createElement("map");
            if (map != null) {
                if (map != null) {
                    mapElem.appendChild(doc.createElement("InspireID")).appendChild(
                            doc.createTextNode(fmt(map.getMapInspire(), "mapInspireID")));
                    if (map.getMapPdf() != null) {
                    	mapElem.appendChild(doc.createElement("pdfProvided")).appendChild(
                    			doc.createTextNode(fmt(toBoolean(map.getMapPdf()), "mapPDF")));
                    }
                    mapElem.appendChild(doc.createElement("mapReference")).appendChild(
                            doc.createTextNode(fmt(map.getMapReference(), "mapRef")));
                }
            } else {
                // xmlValidFields.add("Map. (Maps section)\n");
            }
            sdf.appendChild(mapElem);
            sdfs.appendChild(sdf);
        }

        return doc;

    }
}
