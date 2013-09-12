package sdf_manager;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
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
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pojos.*;


import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import sdf_manager.util.SDF_Constants;
import sdf_manager.util.SDF_Util;

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
    private ArrayList sitecodes = new ArrayList();
    private String logExportFileName;
    private FileWriter logErrorFile;


    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExporterXMLStax.class .getName());

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
         }
         else {
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
         catch(Exception e) {
            // e.printStackTrace();
             ExporterXMLStax.log.error("An error has accurred in initLogFile. Error Message :::"+e.getMessage());
         }
     }

     /**
      *
      */
     public void closeLogFile(){
         try {
             out.close();
             outFile.close();
         }
         catch(Exception e) {
             // e.printStackTrace();
             ExporterXMLStax.log.error("An error has accurred in initLogFile. Error Message :::"+e.getMessage());
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
        this.fileName = fileName;
        log("Creating requested XML document: "  + fileName);
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
            //Close the output stream
        }catch (Exception e) {
            log("ERROR initWriter()"+e.getMessage());
            ExporterXMLStax.log.error("An error has accurred in initWriter. Error Message :::"+e.getMessage());
        }
    }

    /**
     *
     */
    void finalizeWriter(){
        try {
            this.writer.close();
        } catch (IOException e) {
            log("ERROR finalizeWriter()");
            ExporterXMLStax.log.error("ERROR finalizeWriter():::"+e.getMessage());

        }
    }

    /**
     *
     */
    void finalizeWriterError(){
        try {

            this.writer.close();
            if((new File(this.fileName)).exists()){
                (new File(this.fileName)).delete();
            }
        } catch (IOException e) {
            log("ERROR finalizeWriter()"+e.getMessage());
            ExporterXMLStax.log.error("ERROR finalizeWriterError():::"+e.getMessage());

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
            Iterator itrSites = session.createQuery(hql).iterate();
            log("iterating...");
            while (itrSites.hasNext()) {
                Object tuple = itrSites.next();
                String sitecode = (String)tuple;
                this.sitecodes.add(sitecode);
            }
            tx.commit();
            session.close();
        }
        catch (Exception e) {
            log("ERROR loadSitecodes()"+e.getMessage());
            ExporterXMLStax.log.error("ERROR loadSitecodes():::"+e.getMessage());
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
         if (i.compareTo(new Short(i)) > 0){
             return true;
         }
         else{
             return false;
         }
     }

     /**
      *
      * @param src
      * @param fieldName
      * @return
      */
     String fmt (String src, String fieldName) {
        /* basically a debugging function, printing out null fields
           but could be used for other purposes as well */
        if (src == null) {
           return "";
        }
        else{
            //return getString(src);
            return src;
        }
    }

    /**
     *
     * @param date
     * @param fieldName
     * @return
     */
    String fmt (Date date, String fieldName) {
        if (date != null){
            return fmt(date.toString(),fieldName);
        }
        else{
            return fmt((String)null,fieldName);
        }
    }
     /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmt (Double val, String fieldName) {
        if (val != null){
           String strVal = "0.00";
           if(val != new Double(0)){
               strVal = val.toString();
           }
            //return fmt(val.toString(),fieldName);
            return fmt(strVal,fieldName);
        }
        else {
            return fmt("0.00",fieldName);
        }
    }
    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmt (Integer val, String fieldName) {
        if (val != null){
           String strVal = "0";
           if(val != new Integer(0)){
               strVal = val.toString();
           }
            return fmt(strVal,fieldName);
        }
        else{
            return fmt("0",fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmt (Boolean val, String fieldName) {
        if (val != null){
            return fmt(val.toString(),fieldName);
        }
        else{
            return fmt((String)null,fieldName);
        }
    }

    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
    String fmt (Character val, String fieldName) {
        if (val != null){
            return fmt(val.toString(),fieldName);
        }
        else{
            return fmt((String)null,fieldName);
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
    String fmtToLowerCase (Character val, String fieldName) {
        if (val != null){
            return fmt(val.toString().toLowerCase(),fieldName);
        }
        else{
            return fmt((String)null,fieldName);
        }
    }
    /**
     *
     * @param val
     * @param fieldName
     * @return
     */
     String fmtToUpperCase (Character val, String fieldName) {
        if (val != null){
            return fmt(val.toString().toUpperCase(),fieldName);
        }
        else{
            return fmt((String)null,fieldName);
        }
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
     * @param fieldName
     * @return
     */
    private String getString(String fieldName) {
        try {
             byte[] result = fieldName.getBytes();
             if (result != null && result.length == 0) {
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
                 }else{
                    return null;
                }
            }
        }catch (Exception e) {
             ExporterXMLStax.log.error("Failed extracting field: " + fieldName + ". The field could have an erroneous name. Please verify.\nError:::"+e.getMessage());

             JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\nPlease check the sdfLog file for more details", "Dialog",JOptionPane.ERROR_MESSAGE);
             return null;
         }
     }

    /**
     *
     * @param exportErrorMap
     * @return
     */
    private File copyToLogExportFile(HashMap exportErrorMap){
        File fileLog = null;
        try {
          fileLog = new File(this.logExportFileName);
          logErrorFile = new FileWriter(fileLog);
          Set ErrorSiteKeySet = exportErrorMap.keySet();
          Iterator it = ErrorSiteKeySet.iterator();
          while(it.hasNext()){
              logErrorFile.write("------------------------------------------------------------" + System.getProperty("line.separator"));
              Calendar cal = Calendar.getInstance();
              SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
              String dateLine = sdf.format(cal.getTime());
              String siteCode = (String) it.next();

              logErrorFile.write(dateLine + ": An error has been produced in the export process for the site: "+siteCode+ System.getProperty("line.separator") );
              ArrayList arraySites = (ArrayList)exportErrorMap.get(siteCode);

              if(!arraySites.isEmpty()){
                  logErrorFile.write(dateLine + ": Please, check the following fields of te site in the SDF editor:" + System.getProperty("line.separator"));
                  Iterator itSite = arraySites.iterator();
                  while(itSite.hasNext()){
                     String lineExport = (String)itSite.next();
                     logErrorFile.write("     "+dateLine + ": " + lineExport+ System.getProperty("line.separator"));
                     logErrorFile.flush();
                  }
              }
              logErrorFile.write("------------------------------------------------------------"+ System.getProperty("line.separator") );
          }
          logErrorFile.flush();
          logErrorFile.close();
       }catch (Exception e) {
           JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\nPlease check the sdfLog file for more details", "Dialog",JOptionPane.ERROR_MESSAGE);
           //e.printStackTrace();
           ExporterXMLStax.log.error("Error copyToLogExportFile(). "+e.getMessage());
       }
       return fileLog;
    }

    /**
     *
     * @param i
     * @return
     */
    Boolean toBoolean(Short i) {
         if (i == null){
             return false;
         }
         if (i > 0){
             return true;
         }
         else{
             return false;
         }
     }


    /**
     *
     */
    public boolean saveConfig(){
        // Create a XMLOutputFactory
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        Session session = HibernateUtil.getSessionFactory().openSession();
        // Create XMLEventWriter
        boolean xmlOK = true;
        try{
            HashMap xmlErrorSiteHash = new HashMap();
            XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(this.fileName),"utf-8" );
            // Create a EventFactory
            XMLEventFactory eventFactory = XMLEventFactory.newInstance();
            XMLEvent end = eventFactory.createDTD("\n");
            XMLEvent tab = eventFactory.createDTD("\t");
            // Create and write Start Tag
            StartDocument startDocument = eventFactory.createStartDocument();
            EndDocument endDocument = eventFactory.createEndDocument();
            eventWriter.add(startDocument);

             //create config open tag
            eventWriter.add(end);

            // Create config open tag
            StartElement sdfsStartElement = eventFactory.createStartElement("","", "sdfs");                                  
            eventWriter.add(sdfsStartElement);
            
            eventWriter.add(eventFactory.createAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"));
            eventWriter.add(eventFactory.createAttribute("xsi:noNamespaceSchemaLocation", "http://dd.eionet.europa.eu/schemas/natura2000/sdf_v1.xsd"));
            
            eventWriter.add(end);
            Iterator itrSites = this.sitecodes.iterator();


            int flush = 0;
            log("Parsing sitecodes...");
            ExporterXMLStax.log.info("Parsing sitecodes...");
            int i=0;
            while (itrSites.hasNext()) {
                ArrayList xmlValidFields = new ArrayList();


                eventWriter.add(tab);eventWriter.add(tab);
                StartElement itemSdfStartElement = eventFactory.createStartElement("", "", "sdf");
                eventWriter.add(itemSdfStartElement);
                eventWriter.add(end);

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement itemSiteIdentStartElement = eventFactory.createStartElement("", "", "siteIdentification");
                eventWriter.add(itemSiteIdentStartElement);
                eventWriter.add(end);




                Site site =  (Site) session.get(Site.class,(String)itrSites.next());
                log("Processing site: " + site.getSiteCode());
                ExporterXMLStax.log.info("Processing site: " + site.getSiteCode());
                if(site.getSiteType() != null && !(("").equals(site.getSiteType().toString()))){
                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "siteType", fmt(Character.toString(site.getSiteType()),"siteType"));

                }else{
                    xmlValidFields.add("Site Type, in Identification section\n");
                    xmlOK = false;
                }
                ExporterXMLStax.log.info("Processing getSiteType: " + site.getSiteType());

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "siteCode", fmt(site.getSiteCode(),"siteCode"));

                if(site.getSiteName() != null && !(("").equals(site.getSiteName()))){
                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "siteName", fmt(site.getSiteName(),"siteName"));
                }else{
                    xmlValidFields.add("Site Name, in Identification section\n");
                    xmlOK = false;
                }

                if(site.getSiteCompDate() != null){
                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "compilationDate", fmt(SDF_Util.getFormatDateToXML(site.getSiteCompDate()),"compilationDate"));
                }else{
                    xmlValidFields.add("Compilation Date, in Identification section\n");
                    xmlOK = false;
                }

                if(site.getSiteUpdateDate() != null){
                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "updateDate", fmt(SDF_Util.getFormatDateToXML(site.getSiteUpdateDate()),"updateDate"));
                }else{
                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "updateDate", SDF_Constants.NULL_DATE);
                }

                if(site.getSiteCompDate() !=null && site.getSiteUpdateDate() != null){
                    if (site.getSiteCompDate().compareTo(site.getSiteUpdateDate())> 0){
                        xmlValidFields.add("Compilation Date and Update Date, in  Identification section\n");
                        xmlOK = false;
                    }
                }

                Resp resp = site.getResp();
                if (resp != null) {

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    StartElement respStartElement = eventFactory.createStartElement("", "", "respondent");
                    eventWriter.add(respStartElement);
                    eventWriter.add(end);
                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "name", fmt(resp.getRespName(),"respName"));

                    if(resp.getRespAddress() != null &&  !(("").equals(resp.getRespAddress()))){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "addressUnstructured", fmt(resp.getRespAddress(),"respAddress"));
                    } else if(resp.getRespAdminUnit() != null && !(resp.getRespAdminUnit().equals(""))){

                        //eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        //StartElement respAddressStartElement = eventFactory.createStartElement("", "", "respondent");
                        //eventWriter.add(respAddressStartElement);
                        //eventWriter.add(end);

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "adminUnit", fmt(resp.getRespAdminUnit(),"adminUnit"));

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "locatorDesignator", fmt(resp.getRespLocatorDesig(),"locatorDesignator"));
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "locatorName", fmt(resp.getRespLocatorName(),"locatorName"));

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "addressArea", fmt(resp.getRespAddressArea(),"addressArea"));

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "postName", fmt(resp.getRespPostName(),"postName"));

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "postCode", fmt(resp.getRespPostCode(),"postCode"));

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "thoroughfare", fmt(resp.getRespThoroughFare(),"thoroughfare"));


                    }else{
                           xmlValidFields.add("Address in Identification section (Respondent tab)\n");
                           xmlOK = false;
                    }

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "email", fmt(resp.getRespEmail(),"email"));

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    EndElement respEndElement = eventFactory.createEndElement("", "", "respondent");
                    eventWriter.add(respEndElement);
                    eventWriter.add(end);

                }

                String siteType = site.getSiteType().toString();
                Calendar cal = Calendar.getInstance();
                cal.set(0, 0, 0);
                Date dateNull = cal.getTime();
                if(("A").equals(siteType)){
                    if(site.getSiteSpaDate() != null){
                       eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                       createItemNode(eventFactory, eventWriter, "spaClassificationDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSpaDate()),"spaClassificationDate"));

                    }else{
                       xmlOK = false;
                       xmlValidFields.add("Date site classificated as SPA in Identification section (Dates tab)\n");
                    }

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "spaLegalReference", fmt(site.getSiteSpaLegalRef(),"spaLegalReference"));

                    if(site.getSiteSciPropDate() != null){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sciProposalDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSciPropDate()),"sciProposalDate"));
                    }else{
                        eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sciProposalDate", SDF_Constants.NULL_DATE);
                    }

                    if(site.getSiteSciConfDate()!= null){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sciConfirmationDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSciConfDate()),"sciConfirmationDate"));
                    }else{
                       eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                       createItemNode(eventFactory, eventWriter, "sciConfirmationDate", SDF_Constants.NULL_DATE);
                    }

                    if(site.getSiteSacDate() != null ){
                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sacDesignationDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSacDate()),"sacDesignationDate"));
                    }else{
                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sacDesignationDate", SDF_Constants.NULL_DATE);
                    }

                }else if(("B").equals(siteType)){
                    if(site.getSiteSpaDate() != null){
                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "spaClassificationDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSpaDate()),"spaClassificationDate"));
                    }else{
                        xmlOK = false;
                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "spaClassificationDate", SDF_Constants.NULL_DATE);
                    }

                    eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "spaLegalReference", fmt(site.getSiteSpaLegalRef(),"spaLegalReference"));

                    if(site.getSiteSciPropDate() != null){
                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sciProposalDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSciPropDate()),"sciProposalDate"));
                    }else{
                        xmlOK = false;
                        xmlValidFields.add("Date Site proposed as SCI in Identification section (Dates tab)\n");
                    }

                    if(site.getSiteSciConfDate()!= null){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sciConfirmationDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSciConfDate()),"sciConfirmationDate"));
                    }else{
                       eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                       createItemNode(eventFactory, eventWriter, "sciConfirmationDate", SDF_Constants.NULL_DATE);
                    }

                    if(site.getSiteSacDate() != null){
                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sacDesignationDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSacDate()),"sacDesignationDate"));
                    }else{
                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sacDesignationDate", SDF_Constants.NULL_DATE);
                    }


                }else if(("C").equals(siteType)){
                    if(site.getSiteSpaDate() != null){
                        eventWriter.add(tab);eventWriter.add(tab);  eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "spaClassificationDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSpaDate()),"spaClassificationDate"));
                    }else{
                        xmlOK = false;
                        xmlValidFields.add("Date site classificated as SPA in Identification section (Dates tab)\n");
                    }

                    eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "spaLegalReference", fmt( site.getSiteSpaLegalRef(),"spaLegalReference"));

                    if(site.getSiteSciPropDate() != null){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sciProposalDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSciPropDate()),"sciProposalDate"));
                    }else{
                        xmlOK = false;
                        xmlValidFields.add("Date Site proposed as SCI in Identification section (Dates tab)\n");
                    }
                    if(site.getSiteSciConfDate()!= null){
                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sciConfirmationDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSciConfDate()),"sciConfirmationDate"));
                     }else{
                       eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                       createItemNode(eventFactory, eventWriter, "sciConfirmationDate", SDF_Constants.NULL_DATE);
                    }

                    if(site.getSiteSacDate() != null){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sacDesignationDate", fmt( SDF_Util.getFormatDateToXML(site.getSiteSacDate()),"sacDesignationDate"));
                    }else{
                        eventWriter.add(tab);eventWriter.add(tab);  eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sacDesignationDate", SDF_Constants.NULL_DATE);
                    }

                }

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "sacLegalReference", fmt( site.getSiteSacLegalRef(),"sacLegalReference"));

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "explanations", fmt( site.getSiteExplanations(),"explanations"));

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement siteIdentEndElement = eventFactory.createEndElement("", "", "siteIdentification");
                eventWriter.add(siteIdentEndElement);
                eventWriter.add(end);

                //Site Location
                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement siteLocStartElement = eventFactory.createStartElement("", "", "siteLocation");
                eventWriter.add(siteLocStartElement);
                eventWriter.add(end);

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "longitude", fmt(site.getSiteLongitude(),"longitude"));

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "latitude", fmt(site.getSiteLatitude(),"latitude"));

                eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "area", fmt(site.getSiteArea(),"area"));

                eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "marineAreaPercentage", fmt(site.getSiteMarineArea(),"marineArea"));

                eventWriter.add(tab);eventWriter.add(tab);  eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "siteLength", fmt(site.getSiteLength(),"siteLength"));

                /*regions*/
                Set siteRegions = site.getRegions();
                eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                StartElement adRegStartElement = eventFactory.createStartElement("", "", "adminRegions");
                eventWriter.add(adRegStartElement);
                eventWriter.add(end);

                if(!(siteRegions.isEmpty())){
                   Iterator itr = siteRegions.iterator();
                   while (itr.hasNext()) {
                        Region r = (Region) itr.next();
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement regStartElement = eventFactory.createStartElement("", "", "region");
                        eventWriter.add(regStartElement);
                        eventWriter.add(end);

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "code", fmt(r.getRegionCode(),"regionCode"));

                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "name", fmt(r.getRegionName(),"regionName"));

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        EndElement regEndElement = eventFactory.createEndElement("", "", "region");
                        eventWriter.add(regEndElement);
                        eventWriter.add(end);
                   }
                }else{
                    xmlValidFields.add("Administrative Region code and name (NUTS) in Location section)\n");
                    xmlOK = false;
                }

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement regEndElement = eventFactory.createEndElement("", "", "adminRegions");
                eventWriter.add(regEndElement);
                eventWriter.add(end);


                /*bioregions*/

                Set siteBioRegions = site.getSiteBiogeos();
                if(!(siteBioRegions.isEmpty())){
                   Iterator itbr = siteBioRegions.iterator();
                   while (itbr.hasNext()) {
                        SiteBiogeo s = (SiteBiogeo) itbr.next();

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement bioStartElement = eventFactory.createStartElement("", "", "biogeoRegions");
                        eventWriter.add(bioStartElement);
                        eventWriter.add(end);

                        Biogeo b = s.getBiogeo();

                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "code", fmt(b.getBiogeoCode(),"bioRegionCode"));

                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        String sPercentage = fmt(s.getBiogeoPercent(),"biogeoPercent");
                        if (sPercentage.equals("0.00")){
                            createItemNode(eventFactory, eventWriter, "percentage", fmt("","biogeoPercent"));
                        } else{
                            createItemNode(eventFactory, eventWriter, "percentage", fmt(s.getBiogeoPercent(),"biogeoPercent"));
                        }

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        EndElement bioEndElement = eventFactory.createEndElement("", "", "biogeoRegions");
                        eventWriter.add(bioEndElement);
                        eventWriter.add(end);

                   }

                }else{
                     xmlValidFields.add("Biogeographical Region in Location section)\n");
                   xmlOK = false;
                }
                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement siteLocEndElement = eventFactory.createEndElement("", "", "siteLocation");
                eventWriter.add(siteLocEndElement);
                eventWriter.add(end);



                //Ecological Info
                eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);
                StartElement EcoInfoStartElement = eventFactory.createStartElement("", "", "ecologicalInformation");
                eventWriter.add(EcoInfoStartElement);
                eventWriter.add(end);

                //Habitats
                eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                StartElement habStartElement = eventFactory.createStartElement("", "", "habitatTypes");
                eventWriter.add(habStartElement);
                eventWriter.add(end);


                Set siteHabs = site.getHabitats();
                Iterator itr = siteHabs.iterator();
                boolean habitatInfo = false;
                boolean speciesInfo = false;

                if(!siteHabs.isEmpty()){
                    habitatInfo=true;
                }
                while (itr.hasNext()) {
                    Habitat h = (Habitat) itr.next();

                    eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    StartElement habitatStartElement = eventFactory.createStartElement("", "", "habitatType");
                    eventWriter.add(habitatStartElement);
                    eventWriter.add(end);


                    if(h.getHabitatCode() != null && !(h.getHabitatCode().equals(""))){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "code", fmt(h.getHabitatCode(),"habitatCode"));
                    }else{
                        xmlValidFields.add("The code of the habitat. (Ecological Info-Habitat Type  section)\n");
                        xmlOK = false;
                    }

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "priorityFormOfHabitatType", fmt(toBoolean(h.getHabitatPriority()),"habitatPriority"));

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "nonpresentInSite", fmt(toBoolean(h.getHabitatNp()),"habitatNp"));

                    eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "coveredArea", fmt(h.getHabitatCoverHa(),"habitatCover"));

                    String sCaves = fmt(h.getHabitatCaves(),"habitatCaves");
                    if (sCaves.equals("0")){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "caves", fmt("","habitatCaves"));
                    } else {
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "caves", fmt(h.getHabitatCaves(),"habitatCaves"));
                    }


                    boolean repOK=false;
                    if(h.getHabitatRepresentativity() != null && !(("-").equals(h.getHabitatRepresentativity().toString()))){
                        /*eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "representativity", fmt(h.getHabitatRepresentativity(),"habitatRepresentativity"));*/
                        if(("D").equals(h.getHabitatRepresentativity().toString())){
                           repOK=true;
                        }

                    }else{
                      xmlValidFields.add("Representativity of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                      xmlOK = false;
                    }


                    if(h.getHabitatDataQuality() != null && !(h.getHabitatDataQuality().equals("-"))){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "observationDataQuality", fmt(h.getHabitatDataQuality(),"habitatDataQuality"));
                    }else{
                        if(!repOK){
                           xmlValidFields.add("Data Quality of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                           xmlOK = false;
                        }else{
                          eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                          createItemNode(eventFactory, eventWriter, "observationDataQuality", "");
                        }

                    }

                    //if(repOK){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "representativity", fmt(h.getHabitatRepresentativity(),"habitatRepresentativity"));
                    //}

                    if(h.getHabitatRelativeSurface() != null){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "relativeSurface", fmt(h.getHabitatRelativeSurface(),"relativeSurface"));
                    }else{
                        if(!repOK){
                           xmlValidFields.add("Relative Surface of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                           xmlOK = false;
                        }else{
                          eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                          createItemNode(eventFactory, eventWriter, "relativeSurface", "");
                        }

                    }

                    if(h.getHabitatConservation() != null){
                        eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "conservation", fmt(h.getHabitatConservation(),"habitatConservation"));
                    }else{
                        if(!repOK){
                           xmlValidFields.add("Conservation of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                           xmlOK = false;
                        }else{
                          eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                          createItemNode(eventFactory, eventWriter, "conservation", "");
                        }

                    }

                    if(h.getHabitatGlobal() != null){
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "global", fmt(h.getHabitatGlobal(),"habitatGlobal"));
                    }else{
                        if(!repOK){
                           xmlValidFields.add("Global of the habitat whose code is: "+h.getHabitatCode()+". (Ecological Info-Habitat Type  section)\n");
                           xmlOK = false;
                        }else{
                          eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                          createItemNode(eventFactory, eventWriter, "global", "");
                        }

                    }

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    EndElement habitatEndElement = eventFactory.createEndElement("", "", "habitatType");
                    eventWriter.add(habitatEndElement);
                    eventWriter.add(end);
                }

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement habEndElement = eventFactory.createEndElement("", "", "habitatTypes");
                eventWriter.add(habEndElement);
                eventWriter.add(end);

                //Species
                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement spStartElement = eventFactory.createStartElement("", "", "species");
                eventWriter.add(spStartElement);
                eventWriter.add(end);

                Set siteSpecies = site.getSpecieses();
                boolean birdsSPA = false;
                if(!siteSpecies.isEmpty()){
                    Iterator itsr = siteSpecies.iterator();
                    while(itsr.hasNext()) {
                        Species s = (Species) itsr.next();

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement speciesStartElement = eventFactory.createStartElement("", "", "speciesPopulation");
                        eventWriter.add(speciesStartElement);
                        eventWriter.add(end);

                        if(s.getSpeciesGroup() != null && !(("-").equals(s.getSpeciesGroup().toString()))){
                            //birds in SPA sites are mandatory
                            if((s.getSpeciesGroup().toString()).equals("B")){
                               birdsSPA = true;
                            }
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "speciesGroup", fmt(s.getSpeciesGroup(),"speciesGroup"));
                        }else{
                            xmlValidFields.add("Group of the species. (Ecological Info - Species Type  section)\n");
                            xmlOK = false;
                        }

                        String speciesCode ="";
                        if(s.getSpeciesCode() != null){
                            speciesCode =s.getSpeciesCode();
                        }
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "speciesCode", fmt(speciesCode,"speciesCode"));

                        if(s.getSpeciesName() != null && !(s.getSpeciesName().equals(""))){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "scientificName", fmt(s.getSpeciesName(),"speciesName"));
                        }else{
                            xmlValidFields.add("Scientific name of the species for the group of species: "+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                            xmlOK = false;
                        }

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sensitiveInfo", fmt(toBoolean(s.getSpeciesSensitive()),"speciesSensitive"));

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "nonpresentInSite", fmt(toBoolean(s.getSpeciesNp()),"speciesNP"));

                        boolean dAssesment=false;
                        if(s.getSpeciesPopulation() != null && ("D").equals(s.getSpeciesPopulation().toString())){
                            dAssesment=true;
                        }
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "populationType", fmtToLowerCase(s.getSpeciesType(),"speciesType"));


                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement popSizeStartElement = eventFactory.createStartElement("", "", "populationSize");
                        eventWriter.add(popSizeStartElement);
                        eventWriter.add(end);

                            String sLowerBound = fmt(s.getSpeciesSizeMin(),"speciesSizeMin");
                            if (sLowerBound.equals("0")){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "lowerBound", fmt("","speciesSizeMin"));
                            } else {
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "lowerBound", fmt(s.getSpeciesSizeMin(),"speciesSizeMin"));
                            }

                            String sUpperBound = fmt(s.getSpeciesSizeMax(),"speciesSizeMax");
                            if (sUpperBound.equals("0")){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "upperBound", fmt("","speciesSizeMax"));
                            } else {
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "upperBound", fmt(s.getSpeciesSizeMax(),"speciesSizeMax"));
                            }

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "countingUnit", fmt(s.getSpeciesUnit(),"speciesUnit"));


                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        EndElement popSizeEndElement = eventFactory.createEndElement("", "", "populationSize");
                        eventWriter.add(popSizeEndElement);
                        eventWriter.add(end);

                        if(s.getSpeciesCategory() != null){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "abundanceCategory", fmtToUpperCase(s.getSpeciesCategory(),"speciesCategory"));
                        }

                        if(s.getSpeciesDataQuality() != null){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "observationDataQuality", fmt(s.getSpeciesDataQuality(),"speciesQuality"));
                        }else{
                            if(!dAssesment){
                                xmlValidFields.add("Data Quality of the species for the code : "+s.getSpeciesCode()+", species name : "+s.getSpeciesName()+" and the group : "+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                            }else{
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "observationDataQuality", "");
                            }

                        }

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "population", fmt(s.getSpeciesPopulation(),"speciesPopulation"));

                        if(s.getSpeciesConservation() != null){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "conservation", fmt(s.getSpeciesConservation(),"speciesConservation"));
                        }else{
                            if(!dAssesment){
                                xmlValidFields.add("Conservation of the species for the code : "+s.getSpeciesCode()+", species name : "+s.getSpeciesName()+" and the group : "+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                            }else{
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "conservation", "");
                            }

                        }

                        if(s.getSpeciesIsolation() != null){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "isolation", fmt(s.getSpeciesIsolation(),"speciesIsolation"));
                        }else{
                            if(!dAssesment){
                                xmlValidFields.add("Isolation of the for the code : "+s.getSpeciesCode()+", species name : "+s.getSpeciesName()+" and the group : "+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");
                            }else{
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "isolation", "");
                            }

                        }

                        if(s.getSpeciesGlobal() != null){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "global", fmt(s.getSpeciesGlobal(),"speciesGlobal"));
                        }else{
                            if(!dAssesment){
                                xmlValidFields.add("Global of the species for the code : "+s.getSpeciesCode()+", species name : "+s.getSpeciesName()+" and the group : "+s.getSpeciesGroup()+". (Ecological Info - Species Type  section)\n");

                            }else{
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "global", "");
                            }

                        }

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        EndElement speciesEndElement = eventFactory.createEndElement("", "", "speciesPopulation");
                        eventWriter.add(speciesEndElement);
                        eventWriter.add(end);

                        speciesInfo=true;
                     }
                }


                //Other Species
                Set siteOtherSpecies = site.getOtherSpecieses();
                if(!siteOtherSpecies.isEmpty()){
                    Iterator itosr = siteOtherSpecies.iterator();
                    while(itosr.hasNext()) {
                        OtherSpecies s = (OtherSpecies) itosr.next();

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement speciesStartElement = eventFactory.createStartElement("", "", "speciesPopulation");
                        eventWriter.add(speciesStartElement);
                        eventWriter.add(end);

                        if(s.getOtherSpeciesGroup() != null && !(("-").equals(s.getOtherSpeciesGroup()))){
                            //birds in SPA sites are mandatory
                            if((s.getOtherSpeciesGroup().toString()).equals("B")){
                               birdsSPA = true;
                            }
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "speciesGroup", fmt(s.getOtherSpeciesGroup(),"speciesGroup"));
                        }else{
                            xmlValidFields.add("Group of the species (no cataloged species). (Ecological Info - Other Species Type  section)\n");
                            xmlOK = false;
                        }

                        String speciesCode="";
                        if(s.getOtherSpeciesCode() != null){
                            speciesCode=s.getOtherSpeciesCode();
                        }
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "speciesCode", fmt(speciesCode,"ospeciesCode"));

                        if(s.getOtherSpeciesName() != null && !(s.getOtherSpeciesName().equals(""))){
                           eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                           createItemNode(eventFactory, eventWriter, "scientificName", fmt(s.getOtherSpeciesName(),"ospeciesName"));
                        }else{
                             xmlValidFields.add("Scientific name of the species (no cataloged species) for the code : "+s.getOtherSpeciesCode()+", species name : "+s.getOtherSpeciesName()+" and the group : "+s.getOtherSpeciesGroup()+". . (Ecological Info - Other Species Type  section)\n");
                           xmlOK = false;
                        }

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "sensitiveInfo", fmt(toBoolean(s.getOtherSpeciesSensitive()),"ospeciesSensitive"));

                        if(s.getOtherSpeciesNp() != null && !("").equals(s.getOtherSpeciesNp())){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "nonpresentInSite", fmt(toBoolean(s.getOtherSpeciesNp()),"ospeciesNP"));
                        }

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement popSizeStartElement = eventFactory.createStartElement("", "", "populationSize");
                        eventWriter.add(popSizeStartElement);
                        eventWriter.add(end);



                            String sLowerBound = fmt(s.getOtherSpeciesSizeMin(),"speciesSizeMin");
                            if (sLowerBound.equals("0")){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);  eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "lowerBound", "");
                            } else {
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);  eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "lowerBound", fmt(s.getOtherSpeciesSizeMin(),"speciesSizeMin"));
                            }


                            String sUpperBound = fmt(s.getOtherSpeciesSizeMax(),"speciesSizeMax");
                            if (sUpperBound.equals("0")){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);  eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "upperBound", "");
                            } else {
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);  eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "upperBound", fmt(s.getOtherSpeciesSizeMax(),"speciesSizeMax"));
                            }

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);  eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "countingUnit", fmt(s.getOtherSpeciesUnit(),"speciesUnit"));

                       eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                       EndElement popSizeEndElement = eventFactory.createEndElement("", "", "populationSize");
                       eventWriter.add(popSizeEndElement);
                       eventWriter.add(end);

                       if(s.getOtherSpeciesCategory() != null){
                           eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                           createItemNode(eventFactory, eventWriter, "abundanceCategory", fmt(s.getOtherSpeciesCategory(),"ospeciesCategory"));
                       }

                      //modificar porque es un tree primero es motivations y despues el nodo motivation (solo en el caso que haya motivations es other species en caso contrario
                      //es species
                      if(s.getOtherSpeciesMotivation() != null && !(("").equals(s.getOtherSpeciesMotivation()))){
                          String str = s.getOtherSpeciesMotivation();

                          eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                          StartElement motStartElement = eventFactory.createStartElement("", "", "motivations");
                          eventWriter.add(motStartElement);
                          eventWriter.add(end);

                          StringTokenizer st2 = new StringTokenizer(str,",");

                          while(st2.hasMoreElements()){
                            String mot = (String)st2.nextElement();
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "motivation", fmt(mot,"ospeciesMotivation"));
                          }

                          eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                          EndElement motEndElement = eventFactory.createEndElement("", "", "motivations");
                          eventWriter.add(motEndElement);
                          eventWriter.add(end);
                      }

                      eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                      EndElement speciesEndElement = eventFactory.createEndElement("", "", "speciesPopulation");
                      eventWriter.add(speciesEndElement);
                      eventWriter.add(end);
                     }
                }

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement speciesEndElement = eventFactory.createEndElement("", "", "species");
                eventWriter.add(speciesEndElement);
                eventWriter.add(end);

                if(!habitatInfo && !speciesInfo){
                    xmlValidFields.add("Non habitats, species. (Ecological Info)\n");
                    xmlOK=false;
                }

                if(((site.getSiteType().toString()).equals("A")) || ((site.getSiteType().toString()).equals("C"))){
                    if(!birdsSPA){
                        xmlValidFields.add("No birds in SPA site. (Ecological Info)\n");
                        xmlOK=false;
                    }
                }


                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement ecoInfoEndElement = eventFactory.createEndElement("", "", "ecologicalInformation");
                eventWriter.add(ecoInfoEndElement);
                eventWriter.add(end);

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement siteDescStartElement = eventFactory.createStartElement("", "", "siteDescription");
                eventWriter.add(siteDescStartElement);
                eventWriter.add(end);

                Set classes = site.getHabitatClasses();
                Iterator ithr = classes.iterator();
                while (ithr.hasNext()) {
                    HabitatClass h = (HabitatClass) ithr.next();

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    StartElement habClassStartElement = eventFactory.createStartElement("", "", "habitatClass");
                    eventWriter.add(habClassStartElement);
                    eventWriter.add(end);


                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "code", fmt(h.getHabitatClassCode(),"habitatClassCode"));

                    eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "coveragePercentage", fmt(h.getHabitatClassCover(),"habitatClassCover"));

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    EndElement habClassEndElement = eventFactory.createEndElement("", "", "habitatClass");
                    eventWriter.add(habClassEndElement);
                    eventWriter.add(end);
                }

                eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "otherSiteCharacteristics", fmt(site.getSiteCharacteristics(),"otherSiteCharacteristics"));

                eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "qualityAndImportance", fmt(site.getSiteQuality(),"qualityAndImportance"));

                eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                StartElement impactStartElement = eventFactory.createStartElement("", "", "impacts");
                eventWriter.add(impactStartElement);
                eventWriter.add(end);

                Set siteImpacts = site.getImpacts();
                boolean posImpact= false;
                boolean negImpact= false;
                if(!siteImpacts.isEmpty()){
                    Iterator itir = siteImpacts.iterator();

                    while (itir.hasNext()) {
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement impStartElement = eventFactory.createStartElement("", "", "impact");
                        eventWriter.add(impStartElement);
                        eventWriter.add(end);

                        Impact im = (Impact) itir.next();

                        if(im.getImpactCode() != null && !(im.getImpactCode().equals(""))){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "impactCode", fmt(im.getImpactCode(),"impactCode"));
                        }else{
                            xmlValidFields.add("Impact code. (Description - Pressures and Threads section)\n");
                           xmlOK = false;
                        }

                        if(im.getImpactRank() != null){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "rank", fmt(im.getImpactRank(),"impactRank"));
                        }else{
                             xmlValidFields.add("Rank of the impact whose code is: "+im.getImpactCode()+". (Description - Pressures and Threads section)\n");
                           xmlOK = false;
                        }

                        if(im.getImpactPollutionCode() != null){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "pollutionCode", fmt(im.getImpactPollutionCode(),"impactPollution"));
                        }

                        if(im.getImpactOccurrence() != null){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "occurrence", fmt(im.getImpactOccurrence(),"impactOccurrece"));
                        }else{
                           xmlValidFields.add("Occurence of the impact whose code is: "+im.getImpactCode()+". (Description - Pressures and Threads section)\n");
                           xmlOK = false;
                        }

                        String impacType = "";
                        if (im.getImpactType() != null && (("P").equals(im.getImpactType().toString()))){
                            impacType ="Positive";
                            posImpact= true;
                        }
                        else if (im.getImpactType() != null && (("N").equals(im.getImpactType().toString()))){
                            impacType = "Negative";
                            negImpact= true;
                        }else{

                        }
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab); eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "natureOfImpact", fmt(impacType,"natureOfImpact"));

                       eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                       EndElement impEndElement = eventFactory.createEndElement("", "", "impact");
                       eventWriter.add(impEndElement);
                       eventWriter.add(end);

                    }

                    if(!posImpact || !negImpact){
                        xmlValidFields.add("There has to be at least one positive and negative impact.\n");
                        xmlOK = false;
                    }
                }else{
                    xmlValidFields.add("Impacts. (Description - Pressures and Threads section)\n");
                    xmlOK = false;
                }

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement habClassEndElement = eventFactory.createEndElement("", "", "impacts");
                eventWriter.add(habClassEndElement);
                eventWriter.add(end);



                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement impStartElement = eventFactory.createStartElement("", "", "ownership");
                eventWriter.add(impStartElement);
                eventWriter.add(end);

                Set owners = site.getSiteOwnerships();
                if(!owners.isEmpty()){
                    Iterator itor = owners.iterator();
                    while(itor.hasNext()) {
                        SiteOwnership o = (SiteOwnership) itor.next();
                        Ownership o2 = o.getOwnership();

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement ownerStartElement = eventFactory.createStartElement("", "", "ownershipPart");
                        eventWriter.add(ownerStartElement);
                        eventWriter.add(end);

                        if(o2.getOwnershipCode() != null && !(o2.getOwnershipCode().equals("-"))){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "ownershiptype", fmt(o2.getOwnershipCode(),"ownershipType"));
                        }else{
                            xmlValidFields.add("Type of Ownership. (Description - Documentation section)\n");
                            xmlOK = false;
                        }

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "percent", fmt(o.getOwnershipPercent(),"ownershipPercent"));

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        EndElement ownerEndElement = eventFactory.createEndElement("", "", "ownershipPart");
                        eventWriter.add(ownerEndElement);
                        eventWriter.add(end);

                    }

                }

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement ownerEndElement = eventFactory.createEndElement("", "", "ownership");
                eventWriter.add(ownerEndElement);
                eventWriter.add(end);

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement docStartElement = eventFactory.createStartElement("", "", "documentation");
                eventWriter.add(docStartElement);
                eventWriter.add(end);

                Doc docObj = site.getDoc();
                if (docObj != null) {

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "description", fmt(docObj.getDocDescription(),"docDescription"));
                    Set docLinks = docObj.getDocLinks();
                    Iterator itdocr = docLinks.iterator();

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    StartElement ownerStartElement = eventFactory.createStartElement("", "", "links");
                    eventWriter.add(ownerStartElement);
                    eventWriter.add(end);
                    while (itdocr.hasNext()) {

                        /*eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement linkStartElement = eventFactory.createStartElement("", "", "link");
                        eventWriter.add(linkStartElement);
                        eventWriter.add(end);*/


                        DocLink docLink = (DocLink) itdocr.next();
                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        createItemNode(eventFactory, eventWriter, "link", fmt(docLink.getDocLinkUrl(),"linkURL"));

                        /*eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        EndElement linkEndElement = eventFactory.createEndElement("", "", "link");
                        eventWriter.add(linkEndElement);
                        eventWriter.add(end);  */
                    }
                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    EndElement linksEndElement = eventFactory.createEndElement("", "", "links");
                    eventWriter.add(linksEndElement);
                    eventWriter.add(end);
                }

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement docsEndElement = eventFactory.createEndElement("", "", "documentation");
                eventWriter.add(docsEndElement);
                eventWriter.add(end);

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement siteDescEndElement = eventFactory.createEndElement("", "", "siteDescription");
                eventWriter.add(siteDescEndElement);
                eventWriter.add(end);


                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement siteProStartElement = eventFactory.createStartElement("", "", "siteProtection");
                eventWriter.add(siteProStartElement);
                eventWriter.add(end);


                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement natDesStartElement = eventFactory.createStartElement("", "", "nationalDesignations");
                eventWriter.add(natDesStartElement);
                eventWriter.add(end);

                Set dsigs = site.getNationalDtypes();
                if(!dsigs.isEmpty()){
                    Iterator itdr = dsigs.iterator();
                    while(itdr.hasNext()) {
                       NationalDtype dtype = (NationalDtype) itdr.next();

                       eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                       StartElement natDesigStartElement = eventFactory.createStartElement("", "", "nationalDesignation");
                       eventWriter.add(natDesigStartElement);
                       eventWriter.add(end);

                       if(dtype.getNationalDtypeCode() != null && !(dtype.getNationalDtypeCode().equals("-"))){
                           eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                           createItemNode(eventFactory, eventWriter, "designationCode", fmt(dtype.getNationalDtypeCode(),"dtypecode"));
                       }else{
                            xmlValidFields.add("Designation code of the Nationals Designations. (Protection Status - Designation Types section)\n");
                            xmlOK = false;
                       }

                       eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                       createItemNode(eventFactory, eventWriter, "cover", fmt(dtype.getNationalDtypeCover(),"dtypecover"));

                       eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                       EndElement natDesigEndElement = eventFactory.createEndElement("", "", "nationalDesignation");
                       eventWriter.add(natDesigEndElement);
                       eventWriter.add(end);
                    }
                }


                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement natDesEndElement = eventFactory.createEndElement("", "", "nationalDesignations");
                eventWriter.add(natDesEndElement);
                eventWriter.add(end);

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement relStartElement = eventFactory.createStartElement("", "", "relations");
                eventWriter.add(relStartElement);
                eventWriter.add(end);

                Set rels = site.getSiteRelations();


                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement natRelStartElement = eventFactory.createStartElement("", "", "nationalRelationships");
                eventWriter.add(natRelStartElement);
                eventWriter.add(end);

                if(!rels.isEmpty()){
                    Iterator itre = rels.iterator();
                    while (itre.hasNext()) {
                        SiteRelation rel = (SiteRelation) itre.next();
                        Character scope = rel.getSiteRelationScope();
                        if (("N").equals(scope.toString())) {

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            StartElement natRelationStartElement = eventFactory.createStartElement("", "", "nationalRelationship");
                            eventWriter.add(natRelationStartElement);
                            eventWriter.add(end);

                            if (rel.getSiteRelationCode() != null && !(rel.getSiteRelationCode().equals(""))){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "designationCode", fmt(rel.getSiteRelationCode(),"designationCode"));
                            } else {
                                xmlValidFields.add("Site Code of the Relation National Site. (Protection Status - Relation with other sites section)\n ");
                                xmlOK = false;
                            }

                            if(rel.getSiteRelationSitename() != null && !(rel.getSiteRelationSitename().equals(""))){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "siteName", fmt(rel.getSiteRelationSitename(),"relationSite"));
                            }else{
                                xmlValidFields.add("Site Name of the Relation National Site. (Protection Status - Relation with other sites section)\n ");
                                xmlOK = false;
                            }

                            if(rel.getSiteRelationType() != null){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "type", fmt(rel.getSiteRelationType(),"relationType"));
                            }else{
                                xmlValidFields.add("Relation National Site whose site name is: "+rel.getSiteRelationSitename()+". . (Protection Status - Relation with other sites section)\n ");
                                xmlOK = false;
                            }

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "cover", fmt(rel.getSiteRelationCover(),"relationCover"));

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            EndElement natRelationEndElement = eventFactory.createEndElement("", "", "nationalRelationship");
                            eventWriter.add(natRelationEndElement);
                            eventWriter.add(end);

                        }

                    }
                }


                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement natRelEndElement = eventFactory.createEndElement("", "", "nationalRelationships");
                eventWriter.add(natRelEndElement);
                eventWriter.add(end);

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement intRelStartElement = eventFactory.createStartElement("", "", "internationalRelationships");
                eventWriter.add(intRelStartElement);
                eventWriter.add(end);

                if(!rels.isEmpty()){
                    Iterator itre = rels.iterator();
                    while (itre.hasNext()) {
                        SiteRelation rel = (SiteRelation) itre.next();
                        Character scope = rel.getSiteRelationScope();
                        if (("I").equals(scope.toString())) {

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            StartElement intRelationStartElement = eventFactory.createStartElement("", "", "internationalRelationship");
                            eventWriter.add(intRelationStartElement);
                            eventWriter.add(end);

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "convention", fmt(rel.getSiteRelationConvention(),"relationConvention"));

                            if(rel.getSiteRelationSitename() != null && !(rel.getSiteRelationSitename().equals(""))){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "siteName", fmt(rel.getSiteRelationSitename(),"relationSite"));
                            }else{
                                xmlValidFields.add("Site Name of the Relation National Site. (Protection Status - Relation with other sites section)\n ");
                                xmlOK = false;
                            }

                            if(rel.getSiteRelationType() != null){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "type", fmt(rel.getSiteRelationType(),"relationType"));
                            }else{
                                xmlValidFields.add("Relation National Site whose site name is: "+rel.getSiteRelationSitename()+". . (Protection Status - Relation with other sites section)\n ");
                                xmlOK = false;
                            }

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "cover", fmt(rel.getSiteRelationCover(),"relationCover"));


                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            EndElement intRelationEndElement = eventFactory.createEndElement("", "", "internationalRelationship");
                            eventWriter.add(intRelationEndElement);
                            eventWriter.add(end);

                        }

                    }
                }


                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement intRelEndElement = eventFactory.createEndElement("", "", "internationalRelationships");
                eventWriter.add(intRelEndElement);
                eventWriter.add(end);

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement relEndElement = eventFactory.createEndElement("", "", "relations");
                eventWriter.add(relEndElement);
                eventWriter.add(end);

                /**************************************************************************
                 * SITE DESIGNATION
                 **************************************************************************/

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                createItemNode(eventFactory, eventWriter, "siteDesignationAdditional", fmt(site.getSiteDesignation() ,"siteDesignationAdditional"));

                /*******/

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement siteProEndElement = eventFactory.createEndElement("", "", "siteProtection");
                eventWriter.add(siteProEndElement);
                eventWriter.add(end);


                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement siteMgmtStartElement = eventFactory.createStartElement("", "", "siteManagement");
                eventWriter.add(siteMgmtStartElement);
                eventWriter.add(end);

                Mgmt mgmt = site.getMgmt();
                if (mgmt != null) {
                    /***Mangement Body**/
                    Set bodies = mgmt.getMgmtBodies();
                    Iterator itrbody = bodies.iterator();

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    StartElement mgmtBodyStartElement = eventFactory.createStartElement("", "", "managementBodies");
                    eventWriter.add(mgmtBodyStartElement);
                    eventWriter.add(end);

                    while (itrbody.hasNext()) {
                        MgmtBody bodyObj = (MgmtBody) itrbody.next();

                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        StartElement mgmtBodStartElement = eventFactory.createStartElement("", "", "managementBody");
                        eventWriter.add(mgmtBodStartElement);
                        eventWriter.add(end);

                        if(bodyObj.getMgmtBodyOrg() != null && !(bodyObj.getMgmtBodyOrg().equals(""))){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "organisation", fmt(bodyObj.getMgmtBodyOrg(),"mgmtBodyOrg"));
                        }else{
                            xmlValidFields.add("Organisation of the Management Bodies. (Management section)\n");
                           xmlOK = false;
                        }

                        if(bodyObj.getMgmtBodyAdminUnit() != null && !(bodyObj.getMgmtBodyAdminUnit().equals(""))){

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            StartElement addressStartElement = eventFactory.createStartElement("", "", "address");
                            eventWriter.add(addressStartElement);
                            eventWriter.add(end);


                            if(bodyObj.getMgmtBodyAdminUnit() != null && !(bodyObj.getMgmtBodyAdminUnit().equals(""))){
                                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                                createItemNode(eventFactory, eventWriter, "adminUnit", fmt(bodyObj.getMgmtBodyAdminUnit(),"adminUnit"));
                            }else{
                                xmlValidFields.add("Address of the Management Bodies. (Management section)\n");
                               xmlOK = false;
                            }

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "locatorDesignator", fmt(bodyObj.getMgmtBodyLocatorDesignator(),"locatorDesignator"));

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "locatorName", fmt(bodyObj.getMgmtBodyLocatorName(),"locatorName"));

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "addressArea", fmt(bodyObj.getMgmtBodyAddressArea(),"addressArea"));

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "postName", fmt(bodyObj.getMgmtBodyPostName(),"postName"));

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "postCode", fmt(bodyObj.getMgmtBodyPostCode(),"postCode"));

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "thoroughfare", fmt(bodyObj.getMgmtBodyThroughFare(),"thoroughfare"));



                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            EndElement addressEndElement = eventFactory.createEndElement("", "", "address");
                            eventWriter.add(addressEndElement);

                            eventWriter.add(end);
                        }else if(bodyObj.getMgmtBodyAddress() != null && !(bodyObj.getMgmtBodyAddress().equals(""))){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "addressUnstructured", fmt(bodyObj.getMgmtBodyAddress(),"addressUnstructured"));
                        }else{
                             xmlValidFields.add("Address of the Management Bodies. (Management section)\n");
                             xmlOK = false;
                        }

                        if(bodyObj.getMgmtBodyEmail() != null && !(bodyObj.getMgmtBodyEmail().equals(""))){
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "email", fmt(bodyObj.getMgmtBodyEmail(),"email"));
                        }else{
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "email","");
                        }


                        eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                        EndElement mgmtBodEndElement = eventFactory.createEndElement("", "", "managementBody");
                        eventWriter.add(mgmtBodEndElement);
                        eventWriter.add(end);
                    }

                   eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                   EndElement mgmtBodyEndElement = eventFactory.createEndElement("", "", "managementBodies");
                   eventWriter.add(mgmtBodyEndElement);
                   eventWriter.add(end);
                    /***Mangement Plan**/
                   Character status = mgmt.getMgmtStatus();

                   eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                   StartElement mgmtPlansStartElement = eventFactory.createStartElement("", "", "managementPlans");
                   eventWriter.add(mgmtPlansStartElement);
                   eventWriter.add(end);

                   if(status == null || ("").equals(status.toString())){
                       status='N';
                   }

                   eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                   createItemNode(eventFactory, eventWriter, "exists", status.toString());

                   Set plans = mgmt.getMgmtPlans();
                   if(!plans.isEmpty()){
                        Iterator itpr = plans.iterator();

                        while (itpr.hasNext()) {
                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            StartElement mgmtPlanStartElement = eventFactory.createStartElement("", "", "managementPlan");
                            eventWriter.add(mgmtPlanStartElement);
                            eventWriter.add(end);
                            MgmtPlan planObj = (MgmtPlan) itpr.next();

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "name", fmt(planObj.getMgmtPlanName(),"mgmtPlanName"));

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            createItemNode(eventFactory, eventWriter, "url", fmt(planObj.getMgmtPlanUrl(),"mgmtPlanUrl"));

                            eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                            EndElement mgmtPlanEndElement = eventFactory.createEndElement("", "", "managementPlan");
                            eventWriter.add(mgmtPlanEndElement);
                            eventWriter.add(end);
                        }
                   }else if(status != null && ("Y").equals(status.toString().toUpperCase()) && plans.isEmpty()){
                        xmlValidFields.add("Management Plans. (Management section)\n");
                        xmlOK = false;
                   }

                   eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                   EndElement mgmtPlansEndElement = eventFactory.createEndElement("", "", "managementPlans");
                   eventWriter.add(mgmtPlansEndElement);
                   eventWriter.add(end);

                   eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                   createItemNode(eventFactory, eventWriter, "conservationMeasures", fmt(mgmt.getMgmtConservMeasures(),"conservationMeasures"));
                }else{
                   xmlValidFields.add("Management. (Management section)\n");
                   xmlOK = false;
                }



                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement siteMgmtEndElement = eventFactory.createEndElement("", "", "siteManagement");
                eventWriter.add(siteMgmtEndElement);
                eventWriter.add(end);


                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                StartElement siteMapStartElement = eventFactory.createStartElement("", "", "map");
                eventWriter.add(siteMapStartElement);
                eventWriter.add(end);

                Map map = site.getMap();
                if(map != null){
                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "InspireID", fmt(map.getMapInspire(),"mapInspireID"));

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "pdfProvided", fmt(toBoolean(map.getMapPdf()),"mapPDF"));

                    eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                    createItemNode(eventFactory, eventWriter, "mapReference", fmt(map.getMapReference(),"mapRef"));

                }else{
                    xmlValidFields.add("Map. (Maps section)\n");
                    xmlOK = false;
                }

                eventWriter.add(tab);eventWriter.add(tab);eventWriter.add(tab);
                EndElement siteMapEndElement = eventFactory.createEndElement("", "", "map");
                eventWriter.add(siteMapEndElement);
                eventWriter.add(end);


                eventWriter.add(tab);eventWriter.add(tab);
                EndElement sdfEndElement = eventFactory.createEndElement("", "", "sdf");
                eventWriter.add(sdfEndElement);
                eventWriter.add(end);

                if (flush++ % 100 == 0) {
                    session.flush();
                    session.clear();
                }
                if(!xmlValidFields.isEmpty()){
                    xmlErrorSiteHash.put(site.getSiteCode(), xmlValidFields);
                }

            }

            /*************************************/
           EndElement sdfsEndElement = eventFactory.createEndElement("", "", "sdfs");
           eventWriter.add(sdfsEndElement);
           eventWriter.add(end);

           eventWriter.add(endDocument);
           eventWriter.close();


           if(xmlErrorSiteHash.isEmpty()){
                log("Export done.");
                ExporterXMLStax.log.error("Export process has finished succesfully");
                JOptionPane.showMessageDialog(new JFrame(), "Export process has finished succesfully.", "Dialog",JOptionPane.ERROR_MESSAGE);
           }else{
                log("The validation of the data has been failed,\nthe XML is not compliant with SDF the schema.\nPlease check the log file, for more details.");
                JOptionPane.showMessageDialog(new JFrame(), "The validation of the data has been failed, the XML is not compliant with SDF the schema.\n Please check the log file, for more details", "Dialog",JOptionPane.INFORMATION_MESSAGE);
                File fileLog = this.copyToLogExportFile(xmlErrorSiteHash);
                if(fileLog != null){
                    Desktop desktop = null;
                    if (Desktop.isDesktopSupported()) {
                        desktop = Desktop.getDesktop();
                        Desktop.getDesktop().open(fileLog);
                    }

                }

            }
            System.gc();

        }catch(Exception e){
            //e.printStackTrace();
            ExporterXMLStax.log.error("Error processing xml file:::"+e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Export process has failed.\n Please check sdfLog file for more details", "Dialog",JOptionPane.ERROR_MESSAGE);

        }finally{
            session.close();
        }
        return xmlOK;

  }


    /**
     *
     * @param eventFactory
     * @param eventWriter
     * @param elementName
     * @param value
     * @throws XMLStreamException
     */
     private static void createItemNode(XMLEventFactory eventFactory, XMLEventWriter eventWriter, String elementName, String value) throws XMLStreamException{
        XMLEvent end = eventFactory.createDTD("\n");
        StartElement startElement = eventFactory.createStartElement("", "", elementName);
        eventWriter.add(startElement);
        Characters characters = eventFactory.createCharacters(value);
        eventWriter.add(characters);
        EndElement endElement = eventFactory.createEndElement("", "", elementName);
        eventWriter.add(endElement);
        eventWriter.add(end);
    }

}
