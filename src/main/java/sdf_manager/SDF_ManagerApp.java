/*
 * SDF_ManagerApp.java
 */

package sdf_manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import sdf_manager.util.PropertyUtils;
import sdf_manager.util.SDF_MysqlDatabase;

/**
 * The main class of the application.
 */
public class SDF_ManagerApp extends SingleFrameApplication {
    private final static Logger log = Logger.getLogger(SDF_ManagerApp.class .getName());
    /** current path of the application. */
    public static final String CURRENT_PATH = (new File("")).getAbsolutePath();
    private static final String LOG_PROPERTIES_FILE = CURRENT_PATH + File.separator + "log4j.properties";

    /** file name for local properties. */
    public static final String LOCAL_PROPERTIES_FILE = CURRENT_PATH + File.separator + "local.properties";

    /**
     * constant for application Natura 2000 mode.
     */
    public static final String NATURA_2000_MODE = "Natura2000";

    /**
     * constant for application EMERALD mode.
     */
    public static final String EMERALD_MODE = "EMERALD";

    /**
     * local.properties
     */
    private static Properties properties;


    /**
     * N2k or EMERALD.
     * specified in props file
     * default N2k mode
     */
    private static String mode  = NATURA_2000_MODE;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        try {
            properties = PropertyUtils.readProperties(LOCAL_PROPERTIES_FILE);
            mode = properties.getProperty("mode");
            log.info("mode form props=" + mode);
        } catch (Exception e) {
            log.error("Error reading properties " + e);
        }
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
        SettingsDialog settingsDialog = null;
        try {
            initializeLogger();

            //if props file not exist open the first dialog to enter the values
            if (!propsFileExists()) {
                log.info("No local.properties file.");
                //StartupSettings startup = new StartupSettings(this);
                //startup.set
                settingsDialog = new SettingsDialog(null, true);
                settingsDialog.setModal(true);
                settingsDialog.setVisible(true);

                //settingsDialog.dispose();

            } else {
                properties = PropertyUtils.readProperties(LOCAL_PROPERTIES_FILE);

                errorMesg = SDF_MysqlDatabase.createNaturaDB(properties);

                if (errorMesg != null) {
                    log.info("Error");
                } else {
                    log.info("run importTool");
                    launch(SDF_ManagerApp.class, args);
                }
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(new JFrame(), "A general error has occurred." + errorMesg, "Dialog", JOptionPane.ERROR_MESSAGE);
            log.error("Error::::" + e.getMessage());

            e.printStackTrace();
        }
       }

    /**
     * settings entered for the first time.
     * they are stored and DB connection established.
     * @param dialog Settings dialog
     */
    public static void settingsEntered(SettingsDialog dialog, String[] args) {

        try {
            String dbHost = dialog.getTxtDatabaseHost().getText();
            String dbPort = dialog.getTxtDatabasePort().getText();

            String dbUser = dialog.getTxtDatabaseUser().getText();
            String dbPassword = dialog.getTxtDatabasePassword().getText();

            String appMode = dialog.getRdbtnNatura().isSelected() ? NATURA_2000_MODE : EMERALD_MODE;

            Map<String, String> props = new HashMap<String, String>(5);

            props.put("host", dbHost);
            props.put("port", dbPort);
            props.put("user", dbUser);
            props.put("password", dbPassword);
            props.put("mode", appMode);

            //test if DB exists:
            //SDF_MysqlDatabase.testConnection(props);

            PropertyUtils.writePropsToFile(LOCAL_PROPERTIES_FILE, props);
            log.info("properties stored");

            log.info("running importTool");

            launch(SDF_ManagerApp.class, args);


        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "An error has occurred in saving settings." + e.getMessage(), "Dialog",
                    JOptionPane.ERROR_MESSAGE);
            log.error("Error::::" + e.getMessage());

            e.printStackTrace();
        } finally {
            dialog.dispose();
            log.info("dialog disposed");
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

    /**
     * Checks if local.properties file is created and can be used.
     * @return true if file exists
     */
    private static boolean propsFileExists() {
        File file = new File(LOCAL_PROPERTIES_FILE);
        return file.exists() && !file.isDirectory() && file.canRead();
    }

    public static String getMode() {
        return mode;
    }
}
