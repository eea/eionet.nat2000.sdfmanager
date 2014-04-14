/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jdesktop.application.ResourceMap;

import sdf_manager.SDF_ManagerApp;

/*
 *
 */
public class SDF_Util {

    private final static Logger log = Logger.getLogger(SDF_Util.class .getName());

    /**
     * Format date to XML file
     * @param date
     * @return
     */
    public static String getFormatDateToXML(Date date) {
        SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM");
        return formateador.format(date);
    }

    /**
     *
     */
    public static void getProperties() {
        Properties props = System.getProperties();
        PropertyConfigurator.configure("log4j.properties");
    }

     /***
      *
      * @return
      */
    public static Properties getSDFProperties() {

        Properties properties = null;
        try {
            properties = new Properties();
            properties.load(new FileInputStream(new java.io.File("").getAbsolutePath() + "\\database\\sdf.properties"));
        } catch (FileNotFoundException e) {
            SDF_Util.log.error("sdf.properties file is missing. Error message:::" + e.getMessage());
        } catch (IOException e) {
            SDF_Util.log.error("An error has occurred. Error message:::" + e.getMessage());
        } catch (Exception e) {
            SDF_Util.log.error("Ageneral exception has occurred. Error message:::" + e.getMessage());
        } finally {
            return properties;
        }
    }

    /**
     *
     * @param strPercent
     * @return
     */
    public static boolean validatePercent(String strPercent) {
        boolean percentOK = true;
        try {
            double percent = (new Double(strPercent)).doubleValue();
            if (percent > 100) {
                percentOK = false;
            }
            if (percent < 0) {
                percentOK = false;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            SDF_Util.log.error("Percent not valid");
            percentOK = false;
        }
        return percentOK;
    }

    /**
     *
     * @param session
     * @param sitecode
     * @return
     */
     public static boolean validateSite(Session session, String sitecode) {
        String hql = " from Site where siteCode='" + sitecode + "'";
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
            SDF_Util.log.error("An error has occurred validating site. Error Message==>" + e.getMessage());
            return false;
        }

     }

     /**
      *
      * @param sitesDB
      * @param importType
      * @return
      */
    public static File copyToLogImportFile(HashMap sitesDB, String importType) {
        File fileLog = null;
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy hh:mm:ss");
            SimpleDateFormat sdfName = new SimpleDateFormat("ddMMyyyy");
            String dateLine = sdf.format(cal.getTime());
            String dateName = sdfName.format(cal.getTime());
            String filePath = (new File("")).getAbsolutePath();
            fileLog = new File(filePath + "//logs//logErrorImport_" + importType + "_" + dateName + ".log");
            FileWriter logErrorFile = new FileWriter(fileLog);

            Set sitesSet = sitesDB.keySet();

            Iterator it = sitesSet.iterator();
            //logErrorFile.write(dateLine + ":  following sites are already stored in Data Base: " + System.getProperty("line.separator") );
            while (it.hasNext()) {
                String siteCode = (String) it.next();
                logErrorFile.write(dateLine + "The following nuts of the site ::" + siteCode + " don't belong to level 2." + System.getProperty("line.separator"));

                ArrayList nutsList = (ArrayList) sitesDB.get(siteCode);
                if (nutsList != null && !(nutsList.isEmpty())) {

                    for (int i = 0; i < nutsList.size(); i++) {
                        String nutCode = (String) nutsList.get(i);
                        logErrorFile.write("           " + nutCode + System.getProperty("line.separator"));
                    }

                }

                logErrorFile.flush();
            }
            logErrorFile.flush();
            logErrorFile.close();
           // write("LOG file : exportLog" + formatDate + ".log");
        } catch (Exception e) {
            e.printStackTrace();
            SDF_Util.log.error("An error has occurred writting log file for import process. Error Message==>" + e.getMessage());
        }
        return fileLog;
    }


     /**
      *
      * @param sitesDB
      * @param importType
      * @return
      */
     public static File copyToLogImportFileList(ArrayList sitesDB, String importType) {
        File fileLog = null;
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy hh:mm:ss");
            SimpleDateFormat sdfName = new SimpleDateFormat("ddMMyyyy");
            String dateLine = sdf.format(cal.getTime());
            String dateName = sdfName.format(cal.getTime());
            String filePath = (new File("")).getAbsolutePath();
            fileLog = new File(filePath + "//logs//logErrorImport_" + importType + "_" + dateName + ".log");
            FileWriter logErrorFile = new FileWriter(fileLog);

            //Set sitesSet = sitesDB.entrySet();

            Iterator it = sitesDB.iterator();
            logErrorFile.write(dateLine + ": The following sites are already stored in Data Base: " + System.getProperty("line.separator"));
            while (it.hasNext()) {
                String siteCode = (String) it.next();
                logErrorFile.write("     " + siteCode + System.getProperty("line.separator"));
                logErrorFile.flush();
            }
            logErrorFile.flush();
            logErrorFile.close();
            // write("LOG file : exportLog" + formatDate + ".log");
       } catch (Exception e) {
           //e.printStackTrace();
           SDF_Util.log.error("An error has occurred writting log file for import process. Error Message==>" + e.getMessage());
       }
       return fileLog;
    }

     /**
      *
      * @param exportErrorList
      * @param logFileName
      * @return
      * @throws IOException
      */
     public static File copyToLogErrorSite(ArrayList exportErrorList, String logFileName) throws IOException {
        File fileLog = null;
        try {

            // String logFileName =  dirPath + System.getProperty("file.separator") + "exportSiteLog" + formatDate + ".log";
            fileLog = new File(logFileName);
            FileWriter logErrorFile = new FileWriter(fileLog);
            if (!exportErrorList.isEmpty()) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdfFormat = new SimpleDateFormat("dd-MM-yyy hh:mm:ss");
                String dateLine = sdfFormat.format(calendar.getTime());
                logErrorFile.write(dateLine + ": Please, check the following fields of the site in the SDF editor:" + System.getProperty("line.separator"));
                Iterator itSite = exportErrorList.iterator();
                while (itSite.hasNext()) {
                    String lineExport = (String) itSite.next();
                    logErrorFile.write(dateLine + ": " + lineExport + System.getProperty("line.separator"));
                    logErrorFile.flush();
                }
            }
            logErrorFile.flush();
            logErrorFile.close();
       } catch (Exception e) {
          //e.printStackTrace();
            SDF_Util.log.error("An error has occurred writting log file. Error Message==>" + e.getMessage());
       }
       return fileLog;
    }


     /**
      * returns correct icon for the control from the resourcemap depending on the mode.
      * @param resourceMap resourcemap
      * @param labelName labelname
      * @param mode Natura 2000 or emerald
      * @return icon resource
      */
     public static Icon getIconForLabel(ResourceMap resourceMap, String labelName, String mode) {
         if (mode.equals(SDF_ManagerApp.EMERALD_MODE)) {
             labelName += ".emerald";
         }

         //SDF_Util.log.info("getting icon for " + labelName);
         return resourceMap.getIcon(labelName);

     }

}
