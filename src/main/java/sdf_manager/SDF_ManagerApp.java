/*
 * SDF_ManagerApp.java
 */

package sdf_manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import sdf_manager.util.SDF_MysqlDatabase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The main class of the application.
 */
public class SDF_ManagerApp extends SingleFrameApplication {
    private final static Logger log = Logger.getLogger(SDF_ManagerApp.class .getName());
    private static String pathLog = (new File("")).getAbsolutePath();
    private final static String LOG_PROPERTIES_FILE = pathLog + File.separator + "log4j.properties";

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new SDF_ManagerView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of SDF_ManagerApp
     */
    public static SDF_ManagerApp getApplication() {
        return Application.getInstance(SDF_ManagerApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) throws IOException {
        String errorMesg = null;
        try {
            initializeLogger();
            errorMesg = SDF_MysqlDatabase.createNaturaDB();
            if (errorMesg != null) {
                log.info("Error");
            } else {
                log.info("run importTool");
                launch(SDF_ManagerApp.class, args);
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(new JFrame(), "A general error has occurred." + errorMesg, "Dialog", JOptionPane.ERROR_MESSAGE);
            log.error("Error::::" + e.getMessage());
            //e.printStackTrace();
        }
       }

    /**
     *
     */
    private static void initializeLogger() {
        Properties logProperties = new Properties();

        try {
            // load our log4j properties / configuration file
            logProperties.load(new FileInputStream(LOG_PROPERTIES_FILE));
            PropertyConfigurator.configure(logProperties);
            log.info("Logging initialized.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JFrame(), "The process has been falied.::", "Dialog", JOptionPane.ERROR_MESSAGE);
            log.error(e.getMessage());
            throw new RuntimeException("Unable to load logging property " + LOG_PROPERTIES_FILE);
        }
    }
}
