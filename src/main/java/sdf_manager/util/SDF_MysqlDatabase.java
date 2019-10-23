/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import sdf_manager.ProgressDialog;
import sdf_manager.SDF_ManagerApp;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

/**
 *
 *
 * @author
 */
public class SDF_MysqlDatabase {

    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SDF_MysqlDatabase.class.getName());

    /**
     * Create the JDBC URL, open a connection to the database and set up tables.
     *
     * @param properties
     *            database properties
     * @param dialog
     *            Progress dialog for user feedback
     * @return error message or empty string if database was created
     */
    public static String createNaturaDB(Properties properties, ProgressDialog dialog) throws Exception {
        Connection con = null;
        String msgError = null;
        String host = properties.getProperty("db.host");
        String port = properties.getProperty("db.port");
        String user = properties.getProperty("db.user");
        String pwd = properties.getProperty("db.password");

        dialog.setLabel("Testing database connection...");
        String connectionValidation = testConnection(host, port, user, pwd);

        if (StringUtils.isNotBlank(connectionValidation)) {
            return connectionValidation;
        }

        try {
            Class.forName("com.mysql.jdbc.Driver");
            SDF_MysqlDatabase.LOGGER.info("Connection to MySQL: user==>" + properties.getProperty("db.user") + "<==password==>"
                    + properties.getProperty("db.password") + "<==");

            String dbUrl = "jdbc:mysql://" + host + ":" + port + "/";

            SDF_MysqlDatabase.LOGGER.info("database connection URL: " + dbUrl);
            con = (Connection) DriverManager.getConnection(dbUrl, user, pwd);
            dialog.setLabel("Creating database schema...");
            boolean schemaExists = createDatabaseSchema(con, dialog);

            boolean refTalesNeedUpdating = false;
            String schemaName = isEmeraldMode() ? "emerald" : "natura2000";

            if (schemaExists) {
                if (isRefSpeciesUpdated(con)) {
                    SDF_MysqlDatabase.LOGGER.info(schemaName + " Schema DB already exists and ref species table is OK");
                } else {
                    SDF_MysqlDatabase.LOGGER.info("Drop Schema " + schemaName);
                    String sql = "drop schema " + schemaName;
                    Statement st = con.createStatement();
                    st.executeUpdate(sql);
                    st.close();

                    String schemaFileName = isEmeraldMode() ? "CreateEmeraldSchema.sql" : "CreateSDFSchema.sql";
                    SDF_MysqlDatabase.LOGGER.info("Recreate Schema " + schemaName);
                    dialog.setLabel("Creating tables ...");
                    msgError = createMySQLDBSchema(con, schemaFileName);

                    refTalesNeedUpdating = true;

                }

                // create new connection to the specific schema to avoid aliases in all SQLs
            }

            con =
                    (Connection) DriverManager.getConnection("jdbc:mysql://" + properties.getProperty("db.host") + ":"
                            + properties.getProperty("db.port") + "/" + schemaName, properties.getProperty("db.user"),
                            properties.getProperty("db.password"));

            if (refTalesNeedUpdating) {
                dialog.setLabel("Populating lookup tables...");
                msgError = populateRefTables(con, dialog);

                if (StringUtils.isNotBlank(msgError)) {
                    // TODO change the design of returning error messages as method results
                    throw new SQLException(msgError);
                }
            }

            dialog.setLabel("Updating database tables...");
            return createOrUpdateDatabaseTables(con, schemaExists, dialog);
        } catch (SQLException sqle) {
            return sqle.getMessage();
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * checks and creates DB schema instance and user 'sa' for the application mode. no tables created also creates and populates
     * reference tables
     *
     * @param con
     *            db connection (without schema specified)
     * @return error message or empty string if everything fine
     * @throws Exception
     *             if creation fails
     */
    public static boolean createDatabaseSchema(Connection con, ProgressDialog dialog) throws Exception {
        String msgError = null;

        Statement stDBExist = null;
        ResultSet rsDBEXist = null;
        // Statement stDBUser = null;
        boolean schemaExists = false;

        String schemaFileName = isEmeraldMode() ? "CreateEmeraldSchema.sql" : "CreateSDFSchema.sql";

        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        String sqlDBExist = "SELECT SCHEMA_NAME as name FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + schemaName + "'";
        stDBExist = con.createStatement();
        rsDBEXist = stDBExist.executeQuery(sqlDBExist);

        if (rsDBEXist.next()) {
            schemaExists = true;

            if (isRefSpeciesUpdated(con)) {
                SDF_MysqlDatabase.LOGGER.info(schemaName + " Schema DB already exists and ref species table is OK");
            } else {
                SDF_MysqlDatabase.LOGGER.info("Drop Schema " + schemaName);
                String sql = "drop schema " + schemaName;
                Statement st = con.createStatement();
                st.executeUpdate(sql);
                SDF_MysqlDatabase.LOGGER.info("Recreate Schema " + schemaName);
                msgError = createMySQLDBSchema(con, schemaFileName);

                // should be Natura2000 if reaching here
                String msgErrorPopulate = populateRefTables(con, dialog);
                if (msgErrorPopulate != null) {
                    msgError = msgError + "\n" + msgErrorPopulate;
                }

            }

            // }
        } else {
            msgError = createMySQLDBSchema(con, schemaFileName);
        }

        // throw exception du to the old design
        if (StringUtils.isNotBlank(msgError)) {
            throw new Exception("Schema creation fails " + msgError);
        }

        return schemaExists;

    }

    /**
     * Create the Natura2000 database or make upgrades in the tables.
     *
     * @param con
     *            database connection database connection
     * @param schemaExists
     *            if schema is existing previously or was created during this session
     * @return error message. if no errors empty string
     * @throws Exception
     *             if error happens in sql
     */
    public static String createOrUpdateDatabaseTables(Connection con, boolean schemaExists, ProgressDialog dialog)
            throws Exception {

        String msgError = null;

        Statement stDBExist = null;
        ResultSet rsDBEXist = null;
        Statement stDBUser = null;
        String schemaFileName = "";
        try {

            String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";

            if (schemaExists) {
                // check if DB updates needed (reference tables already updated in the parent method)
                if (isRefBirdsUpdated(con, stDBExist)) {
                    SDF_MysqlDatabase.LOGGER.info(schemaName + " Schema DB already exists and ref birds table is OK");
                } else {
                    logD(dialog, "Create and populate birds lookup table...");
                    SDF_MysqlDatabase.LOGGER.info("Recreate Ref Birds table");
                    msgError = alterRefBirds(con);
                    msgError = populateRefBirds(con);
                }

                if (isHabitatUpdated(con, stDBExist)) {
                    SDF_MysqlDatabase.LOGGER.info("Habitat table is already updated");
                } else {
                    SDF_MysqlDatabase.LOGGER.info("Add a new column to habitat table");
                    msgError = alterHabitat(con);
                }
                if (isRefTablesExist(con, stDBExist)) {
                    SDF_MysqlDatabase.LOGGER.info("Ref Tables are already updated");
                } else {
                    logD(dialog, "Create Refs tables...");
                    SDF_MysqlDatabase.LOGGER.info("Create Ref tables");
                    msgError = createRefTables(con);
                    String msgErrorPopulate = populateRefTables(con, dialog);
                    if (msgErrorPopulate != null) {
                        msgError = msgError + "\n" + msgErrorPopulate;
                    }
                }

                // Sept 2013. Version 3. Create ReleaseDBUpdates.

                if (isReleaseDBUpdatesExist(con, "3")) {
                    SDF_MysqlDatabase.LOGGER.info("ReleaseDBUpdates exists");
                } else {
                    SDF_MysqlDatabase.LOGGER.info("Create ReleaseDBUpdates");
                    logD(dialog, "Release 3 updates...");
                    msgError = createReleaseDBUpdates(con);
                    String msgErrorPopulate = populateReleaseDBUpdates(con, "3", "1");
                    String msgErrorAlterHabitat = alterHabitatQual(con);
                    String msgErrorAlterDataQual = insertRefDataQual(con);
                    String msgErrorUpdateRefHabitats = updateRefHabitats(con);
                    // tabla ref species
                    String msgErrorUpdateVersion3Done = updateVersionDone(con, "3");
                    if (msgErrorPopulate != null || msgErrorAlterHabitat != null || msgErrorAlterDataQual != null
                            || msgErrorUpdateRefHabitats != null || msgErrorUpdateVersion3Done != null) {
                        msgError =
                                msgError + "\n" + msgErrorPopulate + "\n" + msgErrorAlterHabitat + "\n" + msgErrorAlterDataQual
                                        + "\n" + msgErrorUpdateRefHabitats + "\n" + msgErrorUpdateVersion3Done;
                    }
                }
                if (isSpecCroatiaExist(con, stDBExist)) {
                    SDF_MysqlDatabase.LOGGER.info("Ref Table species Croatia are already inserted");
                } else {
                    logD(dialog, "Insert species...");
                    SDF_MysqlDatabase.LOGGER.info("Inserting Ref species Croatia");
                    String msgErrorPopulateSpec = updateRefSpeciesCroatia(con);
                    if (msgErrorPopulateSpec != null) {
                        msgError = msgError + "\n" + msgErrorPopulateSpec;
                    }
                }

                // EMERALD
                if (SDF_ManagerApp.isEmeraldMode()) {
                    if (isEmeraldRefTablesExist(con, stDBExist)) {
                        SDF_MysqlDatabase.LOGGER.info("EMERALD Ref Tables are already updated");
                    } else {
                        logD(dialog, "EMERALD ref tables...");
                        SDF_MysqlDatabase.LOGGER.info("Create EMERALD Ref tables");
                        msgError = createEMERALDRefTables(con);
                        String msgErrorPopulate = populateEMERALDRefTables(con, dialog);
                        if (msgErrorPopulate != null) {
                            msgError = msgError + "\n" + msgErrorPopulate;
                        }
                    }

                    ensureEmeraldCodeLengths(con, schemaName);
                }

                if (!isDateTypeColumnsLongText(con, stDBUser)) {
                    String msgErrorPopulate = alterDateColumnsType(con);
                    if (msgErrorPopulate != null) {
                        msgError = msgError + "\n" + msgErrorPopulate;
                    }

                }

                // emerald
                if (SDF_ManagerApp.isEmeraldMode() && !isEmeraldUpdatesdone(con)) {
                    SDF_MysqlDatabase.LOGGER.info("Emerald updates:");
                    logD(dialog, "EMERALD updates...");
                    String msgErrorEmerald = doEmeraldUpdates(con);
                    if (msgErrorEmerald != null) {
                        msgError = msgError + "\n" + msgErrorEmerald;
                    }
                }

                // create DB from scratch
            } else {
                logD(dialog, "Create database tables...");
                msgError = createMySQLDBTables(con, schemaFileName, dialog);
                logD(dialog, "Populate lookup tables...");
                String msgErrorPopulate = populateRefTables(con, dialog);
                if (msgErrorPopulate != null) {
                    msgError = msgError + "\n" + msgErrorPopulate;
                }

                if (SDF_ManagerApp.isEmeraldMode()) {
                    logD(dialog, "Create and populate EMERALD lookup tables...");
                    String msgErrorEmerald = createEMERALDRefTables(con);
                    if (msgErrorEmerald != null) {
                        msgError = msgError + "\n" + msgErrorEmerald;
                    }

                    msgErrorEmerald = populateEMERALDRefTables(con, dialog);
                    if (msgErrorEmerald != null) {
                        msgError = msgError + "\n" + msgErrorEmerald;
                    }
                }

                // Sept 2013. Version 3. Create ReleaseDBUpdates.
                if (isReleaseDBUpdatesExist(con, "3")) {
                    SDF_MysqlDatabase.LOGGER.info("ReleaseDBUpdates exists");
                } else {
                    logD(dialog, "Release 3 updates...");
                    SDF_MysqlDatabase.LOGGER.info("Create ReleaseDBUpdates");
                    msgError = createReleaseDBUpdates(con);
                    String msgErrorPopulateRel = populateReleaseDBUpdates(con, "3", "1");
                    String msgErrorAlterHabitat = alterHabitatQual(con);
                    String msgErrorAlterDataQual = insertRefDataQual(con);
                    String msgErrorUpdateRefHabitats = updateRefHabitats(con);
                    String msgErrorUpdateVersion3Done = updateVersionDone(con, "3");
                    if (msgErrorPopulateRel != null || msgErrorAlterHabitat != null || msgErrorAlterDataQual != null
                            || msgErrorUpdateRefHabitats != null || msgErrorUpdateVersion3Done != null) {
                        msgError =
                                msgError + "\n" + msgErrorPopulate + "\n" + msgErrorAlterHabitat + "\n" + msgErrorAlterDataQual
                                        + "\n" + msgErrorUpdateRefHabitats + "\n" + msgErrorUpdateVersion3Done;
                    }
                }
                if (isSpecCroatiaExist(con, stDBExist)) {
                    SDF_MysqlDatabase.LOGGER.info("Ref Table species Croatia are already inserted");
                } else {
                    logD(dialog, "Species lookup...");
                    SDF_MysqlDatabase.LOGGER.info("Inserting Ref species Croatia");
                    String msgErrorPopulateSpec = updateRefSpeciesCroatia(con);
                    if (msgErrorPopulateSpec != null) {
                        msgError = msgError + "\n" + msgErrorPopulateSpec;
                    }
                }

            }

            // Ensure that ASCI date fields (specific to EMERALD mode) are present in sites table.
            ensureSiteAsciDateFieldsPresent(con, schemaName);

            // release 4.2 updates
            if (!isReleaseDBUpdatesExist(con, "4.2")) {
            //if (!isRelease42UpdatesPresent(con, schemaName)) {
                logD(dialog, "Performing release 4.2 updates");
                String msgErrorPopulateRel = populateReleaseDBUpdates(con, "4.2", "2");
                populateRefTablesInFolder(con, "updates" + File.separator + "4.2", dialog);
                logD(dialog, "Release 4.2 updates done");
                updateVersionDone(con, "4.2");
            }

            // release 4.2.1 updates
            if (!isReleaseDBUpdatesExist(con, "4.2.1")) {
            //if (!isRelease42UpdatesPresent(con, schemaName)) {
                logD(dialog, "Performing release 4.2.1 updates");
                //ver no field needs altering BEFORE updates
                Statement st = con.createStatement();
                st.executeUpdate("ALTER TABLE `releasedbupdates` CHANGE COLUMN `RELEASE_NUMBER` `RELEASE_NUMBER` VARCHAR(12) NOT NULL");
                st.close();

                String msgErrorPopulateRel = populateReleaseDBUpdates(con, "4.2.1", "3");
                populateRefTablesInFolder(con, "updates" + File.separator + "4.2.1", dialog);
                logD(dialog, "Release 4.2.1 updates done");
                updateVersionDone(con, "4.2.1");
            }
            
            // release 4.2.3 updates
            if (isEmeraldMode() && !isReleaseDBUpdatesExist(con, "4.2.3")) {
            	logD(dialog, "Performing release 4.2.3 updates");
                String msgErrorPopulateRel = populateReleaseDBUpdates(con, "4.2.3", "4");
                populateRefTablesInFolder(con, "updates" + File.separator + "4.2.3", dialog);
                logD(dialog, "Release 4.2.3 updates done");
                updateVersionDone(con, "4.2.3");
            }

            // release 4.3.1 updates
            if (isEmeraldMode() && !isReleaseDBUpdatesExist(con, "4.3.1")) {
                logD(dialog, "Performing release 4.3.1 updates");
                String msgErrorPopulateRel = populateReleaseDBUpdates(con, "4.3.1", "5");
                populateRefTablesInFolder(con, "updates" + File.separator + "4.3.1", dialog);
                logD(dialog, "Release 4.3.1 updates done");
                updateVersionDone(con, "4.3.1");
            }

             // release 4.3.2 updates
            if (isEmeraldMode() && !isReleaseDBUpdatesExist(con, "4.3.2")) {
                logD(dialog, "Performing release 4.3.2 updates");
                populateReleaseDBUpdates(con, "4.3.2", "6");
                populateRefTablesInFolder(con, "updates" + File.separator + "4.3.2", dialog);
                logD(dialog, "Release 4.3.2 updates done");
                updateVersionDone(con, "4.3.2");
            }

            // release 4.3.3 updates
            if (isEmeraldMode() && !isReleaseDBUpdatesExist(con, "4.3.3")) {
                logD(dialog, "Performing release 4.3.3 updates");
                populateReleaseDBUpdates(con, "4.3.3", "7");
                populateRefTablesInFolder(con, "updates" + File.separator + "4.3.3", dialog);
                logD(dialog, "Release 4.3.3 updates done");
                updateVersionDone(con, "4.3.3");
            }

        } catch (SQLException s) {
            JOptionPane.showMessageDialog(new JFrame(), "Error in Data Base", "Dialog", JOptionPane.ERROR_MESSAGE);
            SDF_MysqlDatabase.LOGGER.error("Error in Data Base:::" + s.getMessage());
            throw s;

        } catch (Exception e) {
            msgError =
                    "The connection to MySQL Data Base has failed.\n"
                            + " Please, Make sure that the parameters (user and password) in the properties file are right";
            JOptionPane.showMessageDialog(new JFrame(), msgError, "Dialog", JOptionPane.ERROR_MESSAGE);
            SDF_MysqlDatabase.LOGGER.error("The connection to MySQL Data Base has failed.\n"
                    + " Please, Make sure that the parameters (user and password) in the properties file are right.::"
                    + e.getMessage());
            throw e;
        } finally {
            if (rsDBEXist != null) {
                rsDBEXist.close();
            }
            if (stDBExist != null) {
                stDBExist.close();
            }
            // not good habit to close connection given as a parameter
            // con.close();
        }
        return msgError;
    }

    /**
     * Method to validate the datatype of columns. Validates if SITE_EXPLANATIONS,SITE_SAC_LEGAL_REF,SITE_SPA_LEGAL_REF of the
     * table: site in DB are longtext instead of varchar(512).
     *
     * @param con
     *            database connection connection
     * @param st
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("finally")
    private static boolean isDateTypeColumnsLongText(Connection con, Statement st) throws SQLException {
        boolean refSpeciesUpdated = true;

        // It's necessary to compare not only datatype but also the size of the column

        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        String columnTypeVarchar = "VARCHAR";
        int columnSizeVarchar = 512;
        try {
            String sql = "SELECT SITE_EXPLANATIONS,SITE_SAC_LEGAL_REF,SITE_SPA_LEGAL_REF FROM " + schemaName + ".site";
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int numOfCol = rsmd.getColumnCount();
            for (int i = 1; i <= numOfCol; i++) {
                if ((columnTypeVarchar).equals(rsmd.getColumnTypeName(i)) && rsmd.getColumnDisplaySize(i) <= columnSizeVarchar) {
                    refSpeciesUpdated = false;
                }

            }
        } catch (SQLException e) {
            SDF_MysqlDatabase.LOGGER.error("Ref Species is already updated");
        } catch (Exception e) {
            SDF_MysqlDatabase.LOGGER.error("Ref Species is already updated");
        } finally {
            return refSpeciesUpdated;
        }
    }

    /**
     * Creates schema for natura2000 or emerald.
     *
     * @param con
     *            database connection connection
     * @param schemaFileName
     *            file of sql script
     * @return error msg
     * @throws SQLException
     *             if sql error
     */

    @SuppressWarnings("finally")
    private static String createMySQLDBSchema(Connection con, String schemaFileName) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        FileInputStream fstreamSchema = null;

        try {
            SDF_MysqlDatabase.LOGGER.info("Creating Schema Data Base");

            fstreamSchema = openScriptFile(schemaFileName);
            InputStreamReader inSchema = new InputStreamReader(fstreamSchema);
            BufferedReader brSchema = new BufferedReader(inSchema);
            String strLineSchema;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineSchema = brSchema.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineSchema);
                if(isLineEmptyOrComment(strLineSchema)){
                    continue;
                }
                st.executeUpdate(strLineSchema);
            }
            // Close the input stream
            inSchema.close();
        } catch (SQLException e) {
            msgErrorCreate = "An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "A general error has been produced: " + e.getMessage();
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            closeQuietly(st);
            IOUtils.closeQuietly(fstreamSchema);
            return msgErrorCreate;
        }

    }

    /**
     *
     * @param con
     *            database connection
     * @param schemaFileName
     * @return
     * @throws SQLException
     */
    private static String createMySQLDBTables(Connection con, String schemaFileName, ProgressDialog dialog) throws SQLException {
        boolean mySQLDB = false;
        String msgErrorCreate = null;
        Statement st = null;
        Statement st2 = null;
        Statement stAlter = null;
        Statement stInsert = null;
        FileInputStream fstream = null;
        try {
            SDF_MysqlDatabase.LOGGER.info("Creating Schema Data Base");

            /*
             * FileInputStream fstreamSchema = openScriptFile(schemaFileName); // FileInputStream fstreamSchema = new
             * FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + // "database" + File.separator + "mysqlDB"
             * + File.separator + schemaFileName); InputStreamReader inSchema = new InputStreamReader(fstreamSchema); BufferedReader
             * brSchema = new BufferedReader(inSchema); String strLineSchema; st = con.createStatement(); // Read File Line By Line
             * while ((strLineSchema = brSchema.readLine()) != null) { st.executeUpdate(strLineSchema); } // Close the input stream
             * inSchema.close();
             */

            // Open the file that is the first
            // Create tables in Data Base
            SDF_MysqlDatabase.LOGGER.info("Creating tables in Data Base");
            fstream = openScriptFile("CreateMySqlTables.sql");
            // FileInputStream fstream = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" +
            // File.separator + "mysqlDB" + File.separator + "CreateMySqlTables.sql");

            InputStreamReader in = new InputStreamReader(fstream, "UTF-8");
            BufferedReader br = new BufferedReader(in);
            String strLine;
            st2 = con.createStatement();
            int counter = 1;
            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLine);
                if(isLineEmptyOrComment(strLine)){
                    continue;
                }
                logD(dialog, "Executing statement " + counter);
                counter++;
                st2.executeUpdate(strLine);
            }
            // Close the input stream
            in.close();

            // EMERALD updates:
            if (SDF_ManagerApp.isEmeraldMode()) {
                SDF_MysqlDatabase.LOGGER.info("EMERALD structure changes");
                fstream = openScriptFile("EmeraldChanges.sql");

                in = new InputStreamReader(fstream, "UTF-8");
                br = new BufferedReader(in);

                st2 = con.createStatement();
                // Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLine);
                    if(isLineEmptyOrComment(strLine)){
                        continue;
                    }
                    st2.executeUpdate(strLine);
                }
                // Close the input stream
                in.close();
            }

            // Populate data base
            SDF_MysqlDatabase.LOGGER.info("Populating tables");
            File dir = new File(getScriptPath("populateDB"));
            // File dir = new File(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB"
            // + File.separator + "populateDB");

            // The list of files can also be retrieved as File objects
            File[] files = dir.listFiles();

            // This filter only returns directories
            FileFilter fileFilter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.isDirectory();
                }
            };
            files = dir.listFiles(fileFilter);
            if (files != null) {
                Comparator<File> cmpFunc = new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return f1.getPath().compareToIgnoreCase(f2.getPath());
                    }
                };
                Arrays.sort(files, cmpFunc);
                for (int i = 0; i < files.length; i++) {

                    // Get filename of file or directory
                    File filename = files[i];
                    logD(dialog, "Loading " + filename.getName());
                    SDF_MysqlDatabase.LOGGER.debug("Loading: " + filename);
                    FileInputStream fsInsert = new FileInputStream(filename);

                    InputStreamReader inInsert = new InputStreamReader(fsInsert, "UTF-8");

                    BufferedReader brInsert = new BufferedReader(inInsert);
                    String strLineInsert;
                    stInsert = con.createStatement();
                    // Read File Line By Line
                    while ((strLineInsert = brInsert.readLine()) != null) {
                        SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineInsert);
                        if(isLineEmptyOrComment(strLineInsert)){
                            continue;
                        }
                        stInsert.executeUpdate(strLineInsert);
                    }
                    // Close the input stream
                    inInsert.close();

                }
            }
            mySQLDB = true;
        } catch (SQLException e) {
            msgErrorCreate = "An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            if (st2 != null) {
                st2.close();
            }
            if (stInsert != null) {
                stInsert.close();
            }

            IOUtils.closeQuietly(fstream);
            return msgErrorCreate;
        }

    }

    /**
     *
     * @param con
     *            database connection
     * @return
     * @throws SQLException
     */
    private static String alterDateColumnsType(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        try {
            FileInputStream fstreamAlter = openScriptFile("alteColumnDatatype.sql");
            // FileInputStream fstreamAlter = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "alteColumnDatatype.sql");

            InputStreamReader inAlter = new InputStreamReader(fstreamAlter, "UTF-8");
            BufferedReader brAlter = new BufferedReader(inAlter);
            String strLineAlter;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineAlter = brAlter.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineAlter);
                if(isLineEmptyOrComment(strLineAlter)){
                    continue;
                }
                st.executeUpdate(strLineAlter);
            }
            inAlter.close();

        } catch (SQLException e) {
            msgErrorCreate = "alteColumnDatatype.sql:::An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "alteColumnDatatype.sql:A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    /**
     *
     * @param con
     *            database connection
     * @param st
     * @return
     */
    private static boolean isRefSpeciesUpdated(Connection con) {
        boolean refSpeciesUpdated = false;

        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        Statement st = null;
        try {
            String sql = "select REF_SPECIES_CODE_NEW from " + schemaName + ".ref_species";
            st = con.createStatement();
            st.executeQuery(sql);
            refSpeciesUpdated = true;
        } catch (Exception e) {
            SDF_MysqlDatabase.LOGGER.error("Ref Species is already updated: " + e);
        } finally {
            closeQuietly(st);
            return refSpeciesUpdated;
        }
    }

    /**
     *
     * @param con
     *            database connection
     * @param st
     * @return
     */
    private static boolean isHabitatUpdated(Connection con, Statement st) {
        boolean habitatUpdated = false;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            String sql = "select HABITAT_COVER_HA from " + schemaName + ".habitat";
            st = con.createStatement();
            st.executeQuery(sql);
            habitatUpdated = true;
        } catch (Exception e) {
            SDF_MysqlDatabase.LOGGER.error("Habitats already updated");
        } finally {

            return habitatUpdated;
        }
    }

    /**
     *
     * @param con
     *            database connection
     * @param st
     * @return
     */
    private static boolean isRefBirdsUpdated(Connection con, Statement st) {
        boolean refBirdsUpdated = false;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            String sql = "select ref_birds_code_new from " + schemaName + ".ref_birds";
            st = con.createStatement();
            st.executeQuery(sql);
            refBirdsUpdated = true;
        } catch (Exception e) {
            refBirdsUpdated = false;
            SDF_MysqlDatabase.LOGGER.error("Ref Birds is NOT updated");
        } finally {

            return refBirdsUpdated;
        }
    }

    /**
     *
     * @param con
     *            database connection
     * @return
     * @throws SQLException
     */
    private static String alterRefBirds(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";

        try {
            SDF_MysqlDatabase.LOGGER.info("alterRefBirds....");
            // FileInputStream fstreamAlter = openScriptFile("Alter_Ref_Birds_table.sql");
            // FileInputStream fstreamAlter = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "Alter_Ref_Birds_table.sql");

            /*
             * InputStreamReader inAlter = new InputStreamReader(fstreamAlter); BufferedReader brAlter = new
             * BufferedReader(inAlter); String strLineAlter;
             */st = con.createStatement();
            // Read File Line By Line
            String sqlAlter =
                    "ALTER TABLE " + schemaName + ".`ref_birds` ADD COLUMN `REF_BIRDS_CODE_NEW` VARCHAR(1) NULL  "
                            + "AFTER `REF_BIRDS_ANNEXIIIPB` , ADD COLUMN `REF_BIRDS_ALT_SCIENTIFIC_NAME` VARCHAR(1024) NULL  "
                            + "AFTER `REF_BIRDS_CODE_NEW` ;";

            // while ((strLineAlter = brAlter.readLine()) != null) {
            st.executeUpdate(sqlAlter);
            // }
            // inAlter.close();

        } catch (SQLException e) {
            msgErrorCreate = "Alter_Ref_Birds_table.sql:::An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "Alter_Ref_Birds_table.sql::A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }
    }

    /**
     *
     * @param con
     *            database connection
     * @return
     * @throws SQLException
     */
    private static String populateRefBirds(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.LOGGER.info("populateRefBirds....");

            FileInputStream fstreamInsert = openScriptFile("populateDB" + File.separator + "insert_birds_new.sql");
            // FileInputStream fstreamInsert = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator + "insert_birds_new.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineInsert);
                if(isLineEmptyOrComment(strLineInsert)){
                    continue;
                }
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream

            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "insert_birds_new.sql:::An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "insert_birds_new.sql::A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    /**
     *
     * @param con
     *            database connection
     * @return
     * @throws SQLException
     */
    private static String alterHabitat(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            st = con.createStatement();
            // Read File Line By Line
            String sqlAlter = "ALTER TABLE " + schemaName + ".`habitat` ADD COLUMN `HABITAT_COVER_HA` DOUBLE NULL;";
            st.executeUpdate(sqlAlter);
        } catch (SQLException e) {
            msgErrorCreate = "alterHabitat:::An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "alterHabitat::A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    /**
     *
     * @param con
     *            database connection
     * @param st
     * @return
     */
    private static boolean isRefTablesExist(Connection con, Statement st) {
        boolean refTablesExist = false;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            String sql = "select * from " + schemaName + ".ref_impact_rank";
            st = con.createStatement();
            st.executeQuery(sql);
            refTablesExist = true;
        } catch (Exception e) {
            refTablesExist = false;
            SDF_MysqlDatabase.LOGGER.error("Ref tables not exist");
        } finally {
            return refTablesExist;
        }
    }

    /**
     * checks if emerald ref tables data is entered.
     *
     * @param con
     *            database connection connection
     * @param st
     *            statement
     * @return boolean
     */
    private static boolean isEmeraldRefTablesExist(Connection con, Statement st) {
        boolean refTablesExist = false;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            String sql = "select count(1) from " + schemaName + ".ref_nuts_emerald";
            st = con.createStatement();
            st.executeQuery(sql);
            refTablesExist = true;
        } catch (Exception e) {
            refTablesExist = false;
            SDF_MysqlDatabase.LOGGER.error("Ref tables not exist");
        }
        return refTablesExist;
    }

    private static String createRefTables(Connection con) throws SQLException {
        return createTablesFromScriptFile(con, "CreateRefTables.sql");
    }

    private static String createEMERALDRefTables(Connection con) throws SQLException {
        return createTablesFromScriptFile(con, "CreateEmeraldRefTables.sql");
    }

    /**
     * executes all SQLs in the script.
     *
     * @param con
     *            database connection connection
     * @param script
     *            file
     * @return
     * @throws SQLException
     */
    private static String createTablesFromScriptFile(Connection con, String scriptFileName) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        try {
            FileInputStream fstreamAlter = openScriptFile(scriptFileName);

            InputStreamReader inAlter = new InputStreamReader(fstreamAlter, "UTF-8");
            BufferedReader brAlter = new BufferedReader(inAlter);
            String strLineAlter;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineAlter = brAlter.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineAlter);
                if(isLineEmptyOrComment(strLineAlter)){
                    continue;
                }
                st.executeUpdate(strLineAlter);
            }
            inAlter.close();

        } catch (SQLException e) {
            msgErrorCreate = scriptFileName + "::An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = scriptFileName + "::A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    private static String populateRefTables(Connection con, ProgressDialog dialog) throws SQLException {
        return populateRefTablesInFolder(con, "ref_tables", dialog);
    }

    private static String populateEMERALDRefTables(Connection con, ProgressDialog dialog) throws SQLException {
        return populateRefTablesInFolder(con, "ref_emerald", dialog);
    }

    /**
     *
     * @param con
     *            database connection
     * @param folderName
     *            folder where reference tables sqls reside
     * @return error message
     * @throws SQLException
     */
    private static String populateRefTablesInFolder(Connection con, String folderName, ProgressDialog dialog) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        try {

            // Populate data base
            SDF_MysqlDatabase.LOGGER.info("Populating Ref tables");
            File dir = new File(getScriptPath("populateDB" + File.separator + folderName));

            // The list of files can also be retrieved as File objects
            File[] files = dir.listFiles();

            // This filter only returns directories
            FileFilter fileFilter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.isDirectory();
                }
            };
            files = dir.listFiles(fileFilter);
            if (files != null) {
                Arrays.sort(files);
                for (int i = 0; i < files.length; i++) {

                    // Get filename of file or directory
                    File filename = files[i];
                    SDF_MysqlDatabase.LOGGER.info("FileName:"+ filename.getName());

                    logD(dialog, "Populating " + filename.getName());
                    FileInputStream fsInsert = new FileInputStream(filename);

                    InputStreamReader inInsert = new InputStreamReader(fsInsert, "UTF-8");

                    BufferedReader brInsert = new BufferedReader(inInsert);
                    String strLineInsert;
                    st = con.createStatement();
                    // Read File Line By Line
                    while ((strLineInsert = brInsert.readLine()) != null) {
                        SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineInsert);
                        if(isLineEmptyOrComment(strLineInsert)){
                            continue;
                        }
                        st.executeUpdate(strLineInsert);
                    }
                    // Close the input stream
                    inInsert.close();

                }
            }

        } catch (SQLException e) {
            msgErrorCreate = folderName + "::An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = folderName + "::A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            SDF_MysqlDatabase.LOGGER.info("st==" + st);
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }


    /***
     * Identifies if a new Line Read from an SQL File, is either empty or a comment .
     * */
    private static boolean isLineEmptyOrComment(String newLine) {
        newLine = newLine.trim();
        if (newLine.isEmpty()) {
            SDF_MysqlDatabase.LOGGER.info("Line is Empty.");
            return true;
        } else if (newLine.startsWith("#") || newLine.startsWith("--") || newLine.startsWith("/*") || newLine.endsWith("*/")) {
            SDF_MysqlDatabase.LOGGER.info("Line is a Comment.");
            return true;
        } else {
            return false;
        }
    }



    /**
     *
     * Sept 201. Version 3
     */
    private static boolean isReleaseDBUpdatesExist(Connection con, String ver) {
        boolean tableExists = false;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        Statement st = null;
        try {
            String sql = "select * from " + schemaName + ".releasedbupdates where RELEASE_NUMBER = '" + ver + "' and UPDATE_DONE = 'Y'";
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            boolean isEmpty = ! rs.first();
            if (!isEmpty) {
            	tableExists = true;
            } else {            	
            	SDF_MysqlDatabase.LOGGER.error("ReleaseDBUpdates does not exist");
            }            
        } catch (Exception e) {
    		// do nothing
        } finally {
            closeQuietly(st);
            return tableExists;
        }
    }

    private static String createReleaseDBUpdates(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        try {

            SDF_MysqlDatabase.LOGGER.info("createReleaseDBUpdates....");

            FileInputStream fstreamAlter = openScriptFile("createReleaseDBUpdates_version3.sql");
            // FileInputStream fstreamAlter = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "createReleaseDBUpdates_version3.sql");

            InputStreamReader inAlter = new InputStreamReader(fstreamAlter, "UTF-8");
            BufferedReader brAlter = new BufferedReader(inAlter);
            String strLineAlter;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineAlter = brAlter.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineAlter);
                if(isLineEmptyOrComment(strLineAlter)){
                    continue;
                }
                st.executeUpdate(strLineAlter);
            }
            inAlter.close();

        } catch (SQLException e) {
            msgErrorCreate = "ReleaseDBUpdates.sql:::An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "ReleaseDBUpdates.sql::A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    private static String populateReleaseDBUpdates(Connection con, String version, String releaseId) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            SDF_MysqlDatabase.LOGGER.info("populate ReleaseDBUpdates....");
            st = con.createStatement();
            st.executeUpdate("insert ignore into " + schemaName + ".ReleaseDBUpdates values(" + releaseId + ",'" + version + "','Version" + version + "' ,'N')");
        } catch (SQLException e) {
            msgErrorCreate = "insert_ReleaseDBUpdates_version :An error has been produced in database ver=" + version;
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "insert_ReleaseDBUpdates_version:A general error has been produced ver=" + version;
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }
    }

    private static String alterHabitatQual(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            st = con.createStatement();
            // Read File Line By Line
            String sqlAlter = "ALTER TABLE " + schemaName + ".`habitat` MODIFY COLUMN `HABITAT_DATA_QUALITY` varchar(2);";
            st.executeUpdate(sqlAlter);
        } catch (SQLException e) {
            msgErrorCreate = "alterHabitatDataQuality:An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "alterHabitatDataQuality:A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }
    }

    private static String insertRefDataQual(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.LOGGER.info("Inserting DD for Habitats in RefDataQuality...");

            FileInputStream fstreamInsert = openScriptFile("populateDB" + File.separator + "insert_RefDataQual_version3.sql");
            // FileInputStream fstreamInsert = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator +
            // "insert_RefDataQual_version3.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineInsert);
                if(isLineEmptyOrComment(strLineInsert)){
                    continue;
                }
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream

            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "insert_RefDataQual_version3.sql:An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "insert_RefDataQual_version3:A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    private static String updateRefHabitats(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.LOGGER.info("Updating RefHabitats ...");

            FileInputStream fstreamInsert = openScriptFile("populateDB" + File.separator + "Update_RefHabitats_version3.sql");
            // FileInputStream fstreamInsert = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator +
            // "Update_RefHabitats_version3.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineInsert);
                if(isLineEmptyOrComment(strLineInsert)){
                    continue;
                }
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream

            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "UpdateRefHabitats.sql:An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "UpdateRefHabitats:A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    private static String updateVersionDone(Connection con, String ver) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";

        try {
            SDF_MysqlDatabase.LOGGER.info("Updating UpdateVersion Done ... ver " + ver);
            st = con.createStatement();
            st.executeUpdate("update " + schemaName + ".ReleaseDBUpdates SET UPDATE_DONE='Y' WHERE RELEASE_NUMBER = '"
                    + ver + "'");
        } catch (SQLException e) {
            msgErrorCreate = "UpdateVersion3Done: An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "UpdateVersion3Done general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    private static boolean isSpecCroatiaExist(Connection con, Statement st) {
        boolean tableExists = false;
        Statement stDBSpec = null;
        ResultSet rsDBEXist = null;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {

            String hql = "select ref_species_code from " + schemaName + ".ref_species where REF_SPECIES_CODE = '6337'";
            stDBSpec = con.createStatement();

            rsDBEXist = stDBSpec.executeQuery(hql);
            if (rsDBEXist.next()) {
                tableExists = true;
            } else {
                tableExists = false;
            }

        } catch (Exception e) {
            tableExists = false;
            SDF_MysqlDatabase.LOGGER.error("New species Croatia does not exist");
        } finally {
            return tableExists;
        }
    }

    /**
     * checks if db strucure updates for emerald mode are done.
     *
     * @param con
     *            database connection db connection
     * @return true if updates already exist
     */
    private static boolean isEmeraldUpdatesdone(Connection con) {
        boolean updateDone = false;
        Statement stDBSpec = null;
        ResultSet rsDBEXist = null;

        try {

            String hql =
                    "select 1 from information_schema.columns where table_schema = 'emerald' and table_name = 'habitat' "
                            + "and column_name = 'habitat_code' and column_type='varchar(9)'";
            stDBSpec = con.createStatement();

            rsDBEXist = stDBSpec.executeQuery(hql);
            if (rsDBEXist.next()) {
                updateDone = true;
            } else {
                updateDone = false;
            }

        } catch (Exception e) {
            updateDone = false;
            SDF_MysqlDatabase.LOGGER.error("Error checking Emrald updates " + e);
        }
        return updateDone;
    }

    private static String updateRefSpeciesCroatia(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.LOGGER.info("Updating RefSpecies Croatia ...");

            FileInputStream fstreamInsert = openScriptFile("populateDB" + File.separator + "Update_RefSpecies_version3.sql");
            // FileInputStream fstreamInsert = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator +
            // "Update_RefSpecies_version3.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineInsert);
                if(isLineEmptyOrComment(strLineInsert)){
                    continue;
                }
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream

            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "UpdateRefHabitats.sql:An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "UpdateRefHabitats:A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    /**
     * opens a text file.
     *
     * @param scriptName
     *            file name
     * @return stream of the file
     * @throws Exception
     *             if i/o error
     */
    private static FileInputStream openScriptFile(String scriptName) throws Exception {
        return new FileInputStream(getScriptPath(scriptName));
    }

    /**
     * Find the path to the database script.
     */
    static String getScriptPath(String scriptName) {
        return new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator
                + scriptName;
    }

    /**
     * testing if user entered props in the system settings screen are correct.
     *
     * @param host
     *            database host
     * @param port
     *            port
     * @param user
     *            DB user name
     * @param pwd
     *            password
     * @return error message
     */
    public static String testConnection(String host, String port, String user, String pwd) {
        Socket socket = new Socket();
        try {

            LOGGER.info("Test if host is solved: ");
            InetSocketAddress endPoint = new InetSocketAddress(host, Integer.parseInt(port));
            if (endPoint.isUnresolved()) {
                return "Host name " + host + " cannot be resolved. \n" + "Check if there is a typo in the host name";
            }
            LOGGER.info("Test if port is open: host='" + host + "'; port='" + port + "'");
            socket.connect(endPoint, 1000);

        } catch (IOException ie) {
            LOGGER.error("Error: " + ie);
            return "No access to specified host:port " + host + ":" + port + "\n" + "Potential reasons:\n"
                    + "1. MySql is not running at the specified location. \n"
                    + "2. Firewall is blocking access to the host:port. \n"
                    + "3. There is a proxy configured with no bypassing exception to the specified host.";
        } catch (Exception e) {
            LOGGER.error("Error: " + e);
            return e.getMessage();
        } finally {
            IOUtils.closeQuietly(socket);
        }
        LOGGER.info("Testing MySQL existence: ");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/?socketTimeout=2000&user=" + user + "&password=" + pwd;
            DriverManager.getConnection(url);
        } catch (ClassNotFoundException cnfe) {
            return "MySql database driver is not available.";
        } catch (CommunicationsException ce) {
            return "Mysql database is not available at " + host + ":" + port;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return sqle.getMessage();
        }

        return "";
    }

    /**
     * updates for emerald schema.
     *
     * @param con
     *            db connection
     * @return error string or null if no errors
     * @throws SQLException
     *             if sQL fails
     */
    private static String doEmeraldUpdates(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.LOGGER.info("Doing necessary DB struct updates for EMERALD data structure ...");

            FileInputStream fstreamInsert = openScriptFile("EmeraldChanges.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                SDF_MysqlDatabase.LOGGER.debug("SQL Statement to be executed:"+strLineInsert);
                if(isLineEmptyOrComment(strLineInsert)){
                    continue;
                }
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream
            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "EmeraldUpdates:An error has been produced in database";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "EmeraldUpdates:A general error has been produced";
            SDF_MysqlDatabase.LOGGER.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            closeQuietly(st);
        }

        return msgErrorCreate;
    }

    /**
     * Local method to reflect mode from main app.
     *
     * @return true is application is running in mode
     */
    private static boolean isEmeraldMode() {
        return SDF_ManagerApp.isEmeraldMode();
    }

    /**
     * Closes the DB connection. if sql exception occurs it is logged to log file
     *
     * @param conn
     *            database connection
     */
    public static void closeQuietly(java.sql.Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqle) {
                LOGGER.error("Error closing database connection " + sqle);
            }
        }
    }

    /**
     * Utility method for closing SQL result set.
     *
     * @param rs
     *            The result set.
     */
    public static void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqle) {
                // Ignore deliberately.
            }
        }
    }

    /**
     * Utility method for closing SQL statement.
     *
     * @param stmt
     *            The statement.
     */
    public static void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException sqle) {
                // Ignore deliberately.
            }
        }
    }

    /**
     * Utility method that ensures valid lengths for various codes in EMERALD-specific database. To be called in EMERALD mode only!
     *
     * @param conn
     *            DB connection.
     * @param schemaName
     *            DB schema name.
     */
    private static void ensureEmeraldCodeLengths(Connection conn, String schemaName) {

        if (conn == null || StringUtils.isBlank(schemaName)) {
            LOGGER.warn("Cannot check EMERALD code lengths: DB connetion or schema name is null/blank.");
            return;
        }

        LOGGER.info("Ensuring valid lengths for various codes in database " + schemaName);

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate("ALTER TABLE habitat CHANGE COLUMN HABITAT_CODE HABITAT_CODE VARCHAR(9) NULL DEFAULT NULL");
            stmt.executeUpdate("ALTER TABLE region CHANGE COLUMN REGION_CODE REGION_CODE VARCHAR(9) NULL DEFAULT NULL");
        } catch (Exception e) {
            LOGGER.error("Failure when ensuring valid lengths for various codes in EMERALD-specific database: " + e, e);
        } finally {
            closeQuietly(stmt);
        }
    }

    /**
     * Utility method that ensures the ASCI date fields (specific to EMERALD mode) are present in sites table.
     *
     * @param conn
     *            SQL connection to use.
     * @param schemaName
     *            Schema (i.e. database) name where the sites table is expected to be present.
     */
    private static void ensureSiteAsciDateFieldsPresent(Connection conn, String schemaName) {

        if (conn == null || StringUtils.isBlank(schemaName)) {
            LOGGER.warn("Cannot check existence of ASCI date fields: DB connetion or schema name is null/blank.");
            return;
        }

        String tableName = "site";
        LOGGER.info("Checking existence of ASCI date fields in table \"" + tableName + "\" in database " + schemaName);

        HashSet<String> existingColumns = new HashSet<String>();
        ResultSet rs = null;
        try {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            if (dbMetaData != null) {

                rs = dbMetaData.getColumns(null, schemaName, tableName, null);
                while (rs.next()) {
                    String colName = rs.getString(4);
                    if (colName != null) {
                        existingColumns.add(colName.toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed querying columns of \"" + schemaName + "." + tableName + "\": ", e);
        } finally {
            closeQuietly(rs);
        }

        LOGGER.info("Existing columns in \"" + schemaName + "." + tableName + "\": " + existingColumns);

        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            // SITE_ASCI_PROP_DATE
            if (!existingColumns.contains("SITE_ASCI_PROP_DATE")) {
                stmt.executeUpdate("ALTER TABLE site ADD COLUMN SITE_ASCI_PROP_DATE DATE DEFAULT NULL");
            }

            // SITE_ASCI_CONF_CAND_DATE
            if (!existingColumns.contains("SITE_ASCI_CONF_CAND_DATE")) {
                stmt.executeUpdate("ALTER TABLE site ADD COLUMN SITE_ASCI_CONF_CAND_DATE DATE DEFAULT NULL");
            }

            // SITE_ASCI_CONF_DATE
            if (!existingColumns.contains("SITE_ASCI_CONF_DATE")) {
                stmt.executeUpdate("ALTER TABLE site ADD COLUMN SITE_ASCI_CONF_DATE DATE DEFAULT NULL");
            }

            // SITE_ASCI_DESIG_DATE
            if (!existingColumns.contains("SITE_ASCI_DESIG_DATE")) {
                stmt.executeUpdate("ALTER TABLE site ADD COLUMN SITE_ASCI_DESIG_DATE DATE DEFAULT NULL");
            }

            // SITE_ASCI_LEGAL_REF
            if (!existingColumns.contains("SITE_ASCI_LEGAL_REF")) {
                stmt.executeUpdate("ALTER TABLE site ADD COLUMN SITE_ASCI_LEGAL_REF LONGTEXT DEFAULT NULL");
            }
        } catch (Exception e) {
            LOGGER.error("Failed creating ASCI date fields: " + e, e);
        } finally {
            closeQuietly(stmt);
        }
    }

    /**
     * Logs status to progress dialog label.
     *
     * @param dlg
     *            dialog window
     * @param msg
     *            message text
     */
    private static void logD(ProgressDialog dlg, String msg) {
        if (dlg != null) {
            dlg.setLabel(msg);
        }
    }

    private static boolean isRelease42UpdatesPresent(Connection conn, String schemaName) {

        if (conn == null || StringUtils.isBlank(schemaName)) {
            LOGGER.warn("Cannot check existence of ver 4.2 updates : DB connetion or schema name is null/blank.");
            return false;
        }

        String tableName = "ref_species";
        LOGGER.info("Checking existence of RES 6 date fields in table \"" + tableName + "\" in database " + schemaName);

        HashSet<String> existingColumns = new HashSet<String>();
        ResultSet rs = null;
        try {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            if (dbMetaData != null) {

                rs = dbMetaData.getColumns(null, schemaName, tableName, null);
                while (rs.next()) {
                    String colName = rs.getString(4);
                    if (colName != null) {
                        existingColumns.add(colName.toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed querying columns of \"" + schemaName + "." + tableName + "\": ", e);
        } finally {
            closeQuietly(rs);
        }

        return existingColumns.contains("REF_SPECIES_RES_6");
    }
}
