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
    public static final String LOCAL_PROPERTIES_FILE = CURRENT_PATH + File.separator + "sdf.properties";

    /** seed file name for SDF properties. */
    public static final String SEED_PROPERTIES_FILE = CURRENT_PATH + File.separator + "config"
            + File.separator + "seed_sdf.properties";

    /** possibly existing old DB properties. */
    public static final String OLD_DB_PROPERTIES_FILE = CURRENT_PATH + File.separator + "lib"
            + File.separator + "sdf_database.properties";

    /**
     * constant for application Natura 2000 mode.
     */
    public static final String NATURA_2000_MODE = "Natura2000";

    private static SettingsDialog settingsDialog;

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
        log.info("start");
        String errorMesg = null;
        getApplication().getMainFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        settingsDialog = null;
        try {
            initializeLogger();

            // either there is one or the other we foudn the props:
            if (propsFileExists() || oldDbPropsExists()) {

                // take Dbprops from old db props
                if (!propsFileExists()) {
                    log.info("No sdf.properties file, take Db props from the old props file.");
                    Map<String, String> props = new HashMap<String, String>(15);

                    // old DB props:
                    Properties oldDbProps = PropertyUtils.readProperties(OLD_DB_PROPERTIES_FILE);
                    props.put("db.host", oldDbProps.getProperty("host"));
                    props.put("db.port", oldDbProps.getProperty("port"));
                    props.put("db.user", oldDbProps.getProperty("user"));
                    props.put("db.password", oldDbProps.getProperty("password"));

                    // default Natura 2000?
                    props.put("application.mode", NATURA_2000_MODE);

                    log.info("Getting seed properties from " + SEED_PROPERTIES_FILE);
                    Properties seedProps = PropertyUtils.readProperties(SEED_PROPERTIES_FILE);
                    for (Object key : seedProps.keySet()) {
                        props.put((String) key, seedProps.getProperty((String) key));
                    }

                    PropertyUtils.writePropsToFile(LOCAL_PROPERTIES_FILE, props);
                    log.info("properties stored to " + LOCAL_PROPERTIES_FILE);

                }

                log.info("Launching...");
                properties = PropertyUtils.readProperties(LOCAL_PROPERTIES_FILE);
                mode = properties.getProperty("application.mode");
                errorMesg = SDF_MysqlDatabase.createNaturaDB(properties);

                if (errorMesg != null) {
                    log.error("db Error: " + errorMesg);
                    JOptionPane.showMessageDialog(null, "A DB error has occured:" + errorMesg + "\n please check and change the database settings in the appearing dialog", "DB Error",
                            JOptionPane.ERROR_MESSAGE);


                    settingsDialog = new SettingsDialog(null, true);
                    settingsDialog.setModal(true);
                    settingsDialog.setVisible(true);

                } else {
                    log.info("run importTool");
                    launch(SDF_ManagerApp.class, args);
                }

            } else {
                log.info("No sdf.properties file in the application root folder and no lib/sdf_database.properties.");
                settingsDialog = new SettingsDialog(null, true);
                settingsDialog.setModal(true);
                settingsDialog.setVisible(true);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "A general error has occurred." + errorMesg, "Dialog",
                    JOptionPane.ERROR_MESSAGE);
            log.error("Error::::" + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }

    }

    /**
     * settings entered for the first time.
     * they are stored and DB connection established.
     * @param dialog Settings dialog
     */
    public static void settingsEntered(SettingsDialog dialog, String[] args) {

        try {
            //init seed properties
            Map<String, String> props = new HashMap<String, String>(15);

            Properties seedProps = PropertyUtils.readProperties(SEED_PROPERTIES_FILE);
            for (Object key : seedProps.keySet()) {
                props.put((String)key, seedProps.getProperty((String)key));
            }
            String dbHost = dialog.getTxtDatabaseHost().getText();
            String dbPort = dialog.getTxtDatabasePort().getText();

            String dbUser = dialog.getTxtDatabaseUser().getText();
            String dbPassword = dialog.getTxtDatabasePassword().getText();

            String appMode = dialog.getRdbtnNatura().isSelected() ? NATURA_2000_MODE : EMERALD_MODE;


            props.put("db.host", dbHost);
            props.put("db.port", dbPort);
            props.put("db.user", dbUser);
            props.put("db.password", dbPassword);
            props.put("application.mode", appMode);


            PropertyUtils.writePropsToFile(LOCAL_PROPERTIES_FILE, props);
            log.info("properties stored to " + LOCAL_PROPERTIES_FILE);

            mode = appMode;

            log.info("create database");
            properties = PropertyUtils.readProperties(LOCAL_PROPERTIES_FILE);
            String errorMesg = SDF_MysqlDatabase.createNaturaDB(properties);
            if (errorMesg != null) {
                JOptionPane.showMessageDialog(null, "An error has occurred when creating DB:" + errorMesg
                        + "\n Please check and change the settings in the appearing dialog.", "DB Error",
                        JOptionPane.ERROR_MESSAGE);
                log.error("Error creating database: " + errorMesg);
            } else {
                dialog.dispose();
                log.info("running importTool");
                launch(SDF_ManagerApp.class, args);
            }


        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error has occurred in saving settings." + e.getMessage(), "Dialog",
                    JOptionPane.ERROR_MESSAGE);
            log.error("Error::::" + e.getMessage());
            e.printStackTrace();
            dialog.dispose();
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
            JOptionPane.showMessageDialog(null, "The process has been falied.::", "Dialog", JOptionPane.ERROR_MESSAGE);
            log.error(e.getMessage());
            throw new RuntimeException("Unable to load logging property " + LOG_PROPERTIES_FILE);
        }
    }

   /**
     * Checks if sdf.properties file is created and can be used.
     * @return true if file exists
     */
    private static boolean propsFileExists() {
        File file = new File(LOCAL_PROPERTIES_FILE);
        return file.exists() && !file.isDirectory() && file.canRead();
    }

    /**
     * Checks if sdf_database.properties file exists from the previous installation.
     * @return true if file exists
     */

    private static boolean oldDbPropsExists() {
        File file = new File(OLD_DB_PROPERTIES_FILE);
        return file.exists() && !file.isDirectory() && file.canRead();
    }

    public static String getMode() {
        return mode;
    }

    /**
     * True if application is running in EMERALD mode.
     * @return mode indication
     */
    public static boolean isEmeraldMode() {
        return mode.equals(EMERALD_MODE);
    }

}
