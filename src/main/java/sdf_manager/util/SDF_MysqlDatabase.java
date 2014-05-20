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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sdf_manager.SDF_ManagerApp;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

/**
 *
 *
 * @author
 */
public class SDF_MysqlDatabase {

    private static final Logger log = Logger.getLogger(SDF_MysqlDatabase.class.getName());

    /**
     * Create the JDBC URL, open a connection to the database and set up tables.
     *
     * @return error message or empty string if database was created
     */
    public static String createNaturaDB(Properties properties) throws SQLException, Exception {
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            SDF_MysqlDatabase.log.info("Connection to MySQL: user==>" + properties.getProperty("db.user") + "<==password==>"
                    + properties.getProperty("db.password") + "<==");
            con =
                    (Connection) DriverManager.getConnection("jdbc:mysql://" + properties.getProperty("db.host") + ":"
                            + properties.getProperty("db.port") + "/", properties.getProperty("db.user"),
                            properties.getProperty("db.password"));
            boolean schemaExists = createDatabaseSchema(con);

            // create new connection to the specific schema to avoid aliases in all SQLs
            String dbSchemaName = isEmeraldMode() ? "emerald" : "natura2000";
            con =
                    (Connection) DriverManager.getConnection("jdbc:mysql://" + properties.getProperty("db.host") + ":"
                            + properties.getProperty("db.port") + "/" + dbSchemaName, properties.getProperty("db.user"),
                            properties.getProperty("db.password"));

            return createOrUpdateDatabaseTables(con, schemaExists);
        } catch (SQLException sqle) {
            return sqle.getMessage();
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * checks and creates DB schema instance and user 'sa' for the application mode. no tables created
     * also creates and oppulates reference tables
     * @param con
     *            db connection (without schema specified)
     * @return error message or empty string if everything fine
     * @throws SQLException
     *             if creation fails
     */
    public static boolean createDatabaseSchema(Connection con) throws Exception {
        String msgError = null;

        Statement stDBExist = null;
        ResultSet rsDBEXist = null;
        Statement stDBUser = null;
        boolean schemaExists = false;

        // dataBase exists
        String schemaFileName = isEmeraldMode() ? "CreateEmeraldSchema.sql" : "CreateSDFSchema.sql";
        String sqlDBUser = "select * from mysql.user where user='sa'";
        stDBUser = con.createStatement();

        rsDBEXist = stDBUser.executeQuery(sqlDBUser);
        if (rsDBEXist.next()) {
            SDF_MysqlDatabase.log.info("User 'sa' already exist");
            schemaFileName = isEmeraldMode() ? "CreateEMERALDOnlySchema.sql" : "CreateSDFOnlySchema.sql";
        }

        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        String sqlDBExist = "SELECT SCHEMA_NAME as name FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + schemaName + "'";
        stDBExist = con.createStatement();
        rsDBEXist = stDBExist.executeQuery(sqlDBExist);

        if (rsDBEXist.next()) {
            schemaExists = true;

            //String name = rsDBEXist.getString("name");

            //if (name != null && !(("").equals(name))) {





                if (isRefSpeciesUpdated(con, stDBExist)) {
                    SDF_MysqlDatabase.log.info(schemaName + " Schema DB already exists and ref species table is OK");
                } else {
                    SDF_MysqlDatabase.log.info("Drop Schema " + schemaName);
                    String sql = "drop schema " + schemaName;
                    Statement st = con.createStatement();
                    st.executeUpdate(sql);
                    SDF_MysqlDatabase.log.info("Recreate Schema " + schemaName);
                    msgError = createMySQLDBSchema(con, schemaFileName);
                    //should be Natura2000 if reaching here
                    //TODO add populate Emerald depending on mode>
                    String msgErrorPopulate = populateRefTables(con);
                    if (msgErrorPopulate != null) {
                        msgError = msgError + "\n" + msgErrorPopulate;
                    }

                }




            //}
        } else {
            msgError = createMySQLDBSchema(con, schemaFileName);
        }

        //throw exception du to the old design
        if (StringUtils.isNotBlank(msgError)) {
            throw new Exception("Schema creation fails " + msgError);
        }

        return schemaExists;

    }

    /**
     * Create the Natura2000 database or make upgrades in the tables.
     *
     * @param con
     *            database connection
     * @param schemaExists
     *            if schema is existing previously or was created during this session
     * @return error message. if no errors empty string
     * @throws SQLException if sql error
     * @throws Exception
     */
    public static String createOrUpdateDatabaseTables(Connection con, boolean schemaExists) throws SQLException, Exception {

        String msgError = null;

        Statement stDBExist = null;
        ResultSet rsDBEXist = null;
        Statement stDBUser = null;
        String schemaFileName = "";
        try {

            String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";

            if (schemaExists) {

                // Create Data Base
                // String name = rsDBEXist.getString("name");
                // if (name != null && !(("").equals(name))) {
                // if (isRefSpeciesUpdated(con, stDBExist)) {
                // SDF_MysqlDatabase.log.info(schemaName + " Schema DB already exists and ref species table is OK");
                // } else {
                // SDF_MysqlDatabase.log.info("Drop Schema");
                // String sql = "drop schema " + schemaName;
                // Statement st = con.createStatement();
                // st.executeUpdate(sql);
                // SDF_MysqlDatabase.log.info("Recreate Schema");
                // msgError = createMySQLDB(con, schemaFileName);
                // String msgErrorPopulate = populateRefTables(con);
                // if (msgErrorPopulate != null) {
                // msgError = msgError + "\n" + msgErrorPopulate;
                // }
                // }

                // db and user created:

                if (isRefBirdsUpdated(con, stDBExist)) {
                    SDF_MysqlDatabase.log.info(schemaName + " Schema DB already exists and ref birds table is OK");
                } else {
                    SDF_MysqlDatabase.log.info("Recreate Ref Birds table");
                    msgError = alterRefBirds(con);
                    msgError = populateRefBirds(con);
                }

                if (isHabitatUpdated(con, stDBExist)) {
                    SDF_MysqlDatabase.log.info("Habitat table is already updated");
                } else {
                    SDF_MysqlDatabase.log.info("Add a new column to habitat table");
                    msgError = alterHabitat(con);
                }
                if (isRefTablesExist(con, stDBExist)) {
                    SDF_MysqlDatabase.log.info("Ref Tables are already updated");
                } else {
                    SDF_MysqlDatabase.log.info("Create Ref tables");
                    msgError = createRefTables(con);
                    String msgErrorPopulate = populateRefTables(con);
                    if (msgErrorPopulate != null) {
                        msgError = msgError + "\n" + msgErrorPopulate;
                    }
                }

                // Sept 2013. Version 3. Create ReleaseDBUpdates.

                if (isReleaseDBUpdatesExist(con, stDBExist)) {
                    SDF_MysqlDatabase.log.info("ReleaseDBUpdates exists");
                } else {
                    SDF_MysqlDatabase.log.info("Create ReleaseDBUpdates");
                    msgError = createReleaseDBUpdates(con);
                    String msgErrorPopulate = PopulateReleaseDBUpdates(con);
                    String msgErrorAlterHabitat = alterHabitatQual(con);
                    String msgErrorAlterDataQual = InsertRefDataQual(con);
                    String msgErrorUpdateRefHabitats = UpdateRefHabitats(con);
                    // tabla ref species
                    String msgErrorUpdateVersion3Done = UpdateVersion3Done(con);
                    if (msgErrorPopulate != null || msgErrorAlterHabitat != null || msgErrorAlterDataQual != null
                            || msgErrorUpdateRefHabitats != null || msgErrorUpdateVersion3Done != null) {
                        msgError =
                                msgError + "\n" + msgErrorPopulate + "\n" + msgErrorAlterHabitat + "\n" + msgErrorAlterDataQual
                                        + "\n" + msgErrorUpdateRefHabitats + "\n" + msgErrorUpdateVersion3Done;
                    }
                }
                if (isSpecCroatiaExist(con, stDBExist)) {
                    SDF_MysqlDatabase.log.info("Ref Table species Croatia are already inserted");
                } else {
                    SDF_MysqlDatabase.log.info("Inserting Ref species Croatia");
                    String msgErrorPopulateSpec = UpdateRefSpeciesCroatia(con);
                    if (msgErrorPopulateSpec != null) {
                        msgError = msgError + "\n" + msgErrorPopulateSpec;
                    }
                }

                // Sept 2013

                // EMERALD
                if (SDF_ManagerApp.isEmeraldMode()) {
                    if (isEmeraldRefTablesExist(con, stDBExist)) {
                        SDF_MysqlDatabase.log.info("EMERALD Ref Tables are already updated");
                    } else {
                        SDF_MysqlDatabase.log.info("Create EMERALD Ref tables");
                        msgError = createEMERALDRefTables(con);
                        String msgErrorPopulate = populateEMERALDRefTables(con);
                        if (msgErrorPopulate != null) {
                            msgError = msgError + "\n" + msgErrorPopulate;
                        }
                    }
                }

                // CREATE database:

                // cannot reach this block:
                // } else {
                // msgError = createMySQLDB(con, schemaFileName);
                // String msgErrorPopulate = populateRefTables(con);
                // if (msgErrorPopulate != null) {
                // msgError = msgError + "\n" + msgErrorPopulate;
                // }
                // }

                if (!isDateTypeColumnsLongText(con, stDBUser)) {
                    String msgErrorPopulate = alterDateColumnsType(con);
                    if (msgErrorPopulate != null) {
                        msgError = msgError + "\n" + msgErrorPopulate;
                    }

                }

                // emerald
                if (SDF_ManagerApp.isEmeraldMode() && !isEmeraldUpdatesdone(con, stDBExist)) {
                    SDF_MysqlDatabase.log.info("Emerald updates:");
                    String msgErrorEmerald = doEmeraldUpdates(con);
                    if (msgErrorEmerald != null) {
                        msgError = msgError + "\n" + msgErrorEmerald;
                    }
                }

                // create DB
            } else {
                msgError = createMySQLDBTables(con, schemaFileName);
                String msgErrorPopulate = populateRefTables(con);
                if (msgErrorPopulate != null) {
                    msgError = msgError + "\n" + msgErrorPopulate;
                }

                if (SDF_ManagerApp.isEmeraldMode()) {
                    String msgErrorEmerald = createEMERALDRefTables(con);
                    if (msgErrorEmerald != null) {
                        msgError = msgError + "\n" + msgErrorEmerald;
                    }

                    msgErrorEmerald = populateEMERALDRefTables(con);
                    if (msgErrorEmerald != null) {
                        msgError = msgError + "\n" + msgErrorEmerald;
                    }
                }

                // Sept 2013. Version 3. Create ReleaseDBUpdates.
                if (isReleaseDBUpdatesExist(con, stDBExist)) {
                    SDF_MysqlDatabase.log.info("ReleaseDBUpdates exists");
                } else {
                    SDF_MysqlDatabase.log.info("Create ReleaseDBUpdates");
                    msgError = createReleaseDBUpdates(con);
                    String msgErrorPopulateRel = PopulateReleaseDBUpdates(con);
                    String msgErrorAlterHabitat = alterHabitatQual(con);
                    String msgErrorAlterDataQual = InsertRefDataQual(con);
                    String msgErrorUpdateRefHabitats = UpdateRefHabitats(con);
                    String msgErrorUpdateVersion3Done = UpdateVersion3Done(con);
                    if (msgErrorPopulateRel != null || msgErrorAlterHabitat != null || msgErrorAlterDataQual != null
                            || msgErrorUpdateRefHabitats != null || msgErrorUpdateVersion3Done != null) {
                        msgError =
                                msgError + "\n" + msgErrorPopulate + "\n" + msgErrorAlterHabitat + "\n" + msgErrorAlterDataQual
                                        + "\n" + msgErrorUpdateRefHabitats + "\n" + msgErrorUpdateVersion3Done;
                    }
                }
                if (isSpecCroatiaExist(con, stDBExist)) {
                    SDF_MysqlDatabase.log.info("Ref Table species Croatia are already inserted");
                } else {
                    SDF_MysqlDatabase.log.info("Inserting Ref species Croatia");
                    String msgErrorPopulateSpec = UpdateRefSpeciesCroatia(con);
                    if (msgErrorPopulateSpec != null) {
                        msgError = msgError + "\n" + msgErrorPopulateSpec;
                    }
                }
                // Sept 2013

            }

        } catch (SQLException s) {
            JOptionPane.showMessageDialog(new JFrame(), "Error in Data Base", "Dialog", JOptionPane.ERROR_MESSAGE);
            SDF_MysqlDatabase.log.error("Error in Data Base:::" + s.getMessage());
            throw s;

        } catch (Exception e) {
            msgError =
                    "The connection to MySQL Data Base has failed.\n"
                            + " Please, Make sure that the parameters (user and password) in the properties file are right";
            JOptionPane.showMessageDialog(new JFrame(), msgError, "Dialog", JOptionPane.ERROR_MESSAGE);
            SDF_MysqlDatabase.log.error("The connection to MySQL Data Base has failed.\n"
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
     * @param con connection
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
            int NumOfCol = rsmd.getColumnCount();
            for (int i = 1; i <= NumOfCol; i++) {
                if ((columnTypeVarchar).equals(rsmd.getColumnTypeName(i)) && rsmd.getColumnDisplaySize(i) <= columnSizeVarchar) {
                    refSpeciesUpdated = false;
                }

            }
        } catch (SQLException e) {
            SDF_MysqlDatabase.log.error("Ref Species is already updated");
        } catch (Exception e) {
            SDF_MysqlDatabase.log.error("Ref Species is already updated");
        } finally {
            return refSpeciesUpdated;
        }
    }

    /**
     * Creates schema for natura2000 or emerald.
     *
     * @param con
     *            connection
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
            SDF_MysqlDatabase.log.info("Creating Schema Data Base");

            fstreamSchema = openScriptFile(schemaFileName);
            InputStreamReader inSchema = new InputStreamReader(fstreamSchema);
            BufferedReader brSchema = new BufferedReader(inSchema);
            String strLineSchema;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineSchema = brSchema.readLine()) != null) {
                st.executeUpdate(strLineSchema);
            }
            // Close the input stream
            inSchema.close();
        } catch (SQLException e) {
            msgErrorCreate = "An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "A general error has been produced: " + e.getMessage();
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            closeStatement(st);
            IOUtils.closeQuietly(fstreamSchema);
            return msgErrorCreate;
        }

    }

    /**
     *
     * @param con
     * @param schemaFileName
     * @return
     * @throws SQLException
     */
    private static String createMySQLDBTables(Connection con, String schemaFileName) throws SQLException {
        boolean mySQLDB = false;
        String msgErrorCreate = null;
        Statement st = null;
        Statement st2 = null;
        Statement stAlter = null;
        Statement stInsert = null;
        FileInputStream fstream = null;
        try {
            SDF_MysqlDatabase.log.info("Creating Schema Data Base");

           /*
            * FileInputStream fstreamSchema = openScriptFile(schemaFileName);
            // FileInputStream fstreamSchema = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + schemaFileName);
            InputStreamReader inSchema = new InputStreamReader(fstreamSchema);
            BufferedReader brSchema = new BufferedReader(inSchema);
            String strLineSchema;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineSchema = brSchema.readLine()) != null) {
                st.executeUpdate(strLineSchema);
            }
            // Close the input stream
            inSchema.close();

            */

            // Open the file that is the first
            // Create tables in Data Base
            SDF_MysqlDatabase.log.info("Creating tables in Data Base");
            fstream = openScriptFile("CreateMySqlTables.sql");
            // FileInputStream fstream = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" +
            // File.separator + "mysqlDB" + File.separator + "CreateMySqlTables.sql");

            InputStreamReader in = new InputStreamReader(fstream, "UTF-8");
            BufferedReader br = new BufferedReader(in);
            String strLine;
            st2 = con.createStatement();
            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                st2.executeUpdate(strLine);
            }
            // Close the input stream
            in.close();

            // EMERALD updates:
            if (SDF_ManagerApp.isEmeraldMode()) {
                SDF_MysqlDatabase.log.info("EMERALD structure changes");
                fstream = openScriptFile("EmeraldChanges.sql");

                in = new InputStreamReader(fstream, "UTF-8");
                br = new BufferedReader(in);

                st2 = con.createStatement();
                // Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    st2.executeUpdate(strLine);
                }
                // Close the input stream
                in.close();
            }

            // Populate data base
            SDF_MysqlDatabase.log.info("Populating tables");
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
            if (files == null) {
                // Either dir does not exist or is not a directory
            } else {
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
                    SDF_MysqlDatabase.log.debug("Loading: " + filename);
                    FileInputStream fsInsert = new FileInputStream(filename);

                    InputStreamReader inInsert = new InputStreamReader(fsInsert, "UTF-8");

                    BufferedReader brInsert = new BufferedReader(inInsert);
                    String strLineInsert;
                    stInsert = con.createStatement();
                    // Read File Line By Line
                    while ((strLineInsert = brInsert.readLine()) != null) {
                        stInsert.executeUpdate(strLineInsert);
                    }
                    // Close the input stream
                    inInsert.close();

                }
            }
            mySQLDB = true;
        } catch (SQLException e) {
            msgErrorCreate = "An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
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
                st.executeUpdate(strLineAlter);
            }
            inAlter.close();

        } catch (SQLException e) {
            msgErrorCreate = "alteColumnDatatype.sql:::An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "alteColumnDatatype.sql:A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
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
     * @param st
     * @return
     */
    private static boolean isRefSpeciesUpdated(Connection con, Statement st) {
        boolean refSpeciesUpdated = false;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            String sql = "select REF_SPECIES_CODE_NEW from " + schemaName + ".ref_species";
            st = con.createStatement();
            st.executeQuery(sql);
            refSpeciesUpdated = true;
        } catch (Exception e) {
            SDF_MysqlDatabase.log.error("Ref Species is already updated");
        } finally {
            return refSpeciesUpdated;
        }
    }

    /**
     *
     * @param con
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
            SDF_MysqlDatabase.log.error("Habitats already updated");
        } finally {

            return habitatUpdated;
        }
    }

    /**
     *
     * @param con
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
            SDF_MysqlDatabase.log.error("Ref Birds is NOT updated");
        } finally {

            return refBirdsUpdated;
        }
    }

    /**
     *
     * @param con
     * @return
     * @throws SQLException
     */
    private static String alterRefBirds(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";

        try {
            SDF_MysqlDatabase.log.info("alterRefBirds....");
            //FileInputStream fstreamAlter = openScriptFile("Alter_Ref_Birds_table.sql");
            // FileInputStream fstreamAlter = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "Alter_Ref_Birds_table.sql");

/*            InputStreamReader inAlter = new InputStreamReader(fstreamAlter);
            BufferedReader brAlter = new BufferedReader(inAlter);
            String strLineAlter;
*/          st = con.createStatement();
            // Read File Line By Line
            String sqlAlter =
                    "ALTER TABLE " + schemaName + ".`ref_birds` ADD COLUMN `REF_BIRDS_CODE_NEW` VARCHAR(1) NULL  "
                    + "AFTER `REF_BIRDS_ANNEXIIIPB` , ADD COLUMN `REF_BIRDS_ALT_SCIENTIFIC_NAME` VARCHAR(1024) NULL  "
                            + "AFTER `REF_BIRDS_CODE_NEW` ;";

//            while ((strLineAlter = brAlter.readLine()) != null) {
                st.executeUpdate(sqlAlter);
//            }
//            inAlter.close();

        } catch (SQLException e) {
            msgErrorCreate = "Alter_Ref_Birds_table.sql:::An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "Alter_Ref_Birds_table.sql::A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
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
     * @return
     * @throws SQLException
     */
    private static String populateRefBirds(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.log.info("populateRefBirds....");

            FileInputStream fstreamInsert = openScriptFile("populateDB" + File.separator + "insert_birds_new.sql");
            // FileInputStream fstreamInsert = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator + "insert_birds_new.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream

            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "insert_birds_new.sql:::An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "insert_birds_new.sql::A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
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
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "alterHabitat::A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
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
            SDF_MysqlDatabase.log.error("Ref tables not exist");
        } finally {
            return refTablesExist;
        }
    }

    /**
     * checks if emerald ref tables data is entered.
     *
     * @param con
     *            connection
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
            SDF_MysqlDatabase.log.error("Ref tables not exist");
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
     *            connection
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
                st.executeUpdate(strLineAlter);
            }
            inAlter.close();

        } catch (SQLException e) {
            msgErrorCreate = scriptFileName + "::An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = scriptFileName + "::A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    private static String populateRefTables(Connection con) throws SQLException {
        return populateRefTablesInFolder(con, "ref_tables");
    }

    private static String populateEMERALDRefTables(Connection con) throws SQLException {
        return populateRefTablesInFolder(con, "ref_emerald");
    }

    /**
     *
     * @param con
     * @return
     * @throws SQLException
     */
    private static String populateRefTablesInFolder(Connection con, String folderName) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        try {

            // Populate data base
            SDF_MysqlDatabase.log.info("Populating Ref tables");
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
            if (files == null) {
                // Either dir does not exist or is not a directory
            } else {
                Arrays.sort(files);
                for (int i = 0; i < files.length; i++) {

                    // Get filename of file or directory
                    File filename = files[i];
                    FileInputStream fsInsert = new FileInputStream(filename);

                    InputStreamReader inInsert = new InputStreamReader(fsInsert, "UTF-8");

                    BufferedReader brInsert = new BufferedReader(inInsert);
                    String strLineInsert;
                    st = con.createStatement();
                    // Read File Line By Line
                    while ((strLineInsert = brInsert.readLine()) != null) {
                        st.executeUpdate(strLineInsert);
                    }
                    // Close the input stream
                    inInsert.close();

                }
            }

        } catch (SQLException e) {
            msgErrorCreate = folderName + "::An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = folderName + "::A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            SDF_MysqlDatabase.log.info("st==" + st);
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    /**
     *
     * Sept 201. Version 3
     */
    private static boolean isReleaseDBUpdatesExist(Connection con, Statement st) {
        boolean tableExists = false;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            String sql = "select * from " + schemaName + ".releasedbupdates";
            st = con.createStatement();
            st.executeQuery(sql);
            tableExists = true;
        } catch (Exception e) {
            tableExists = false;
            SDF_MysqlDatabase.log.error("ReleaseDBUpdates does not exist");
        } finally {
            return tableExists;
        }
    }

    private static String createReleaseDBUpdates(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        try {

            SDF_MysqlDatabase.log.info("createReleaseDBUpdates....");

            FileInputStream fstreamAlter = openScriptFile("createReleaseDBUpdates_version3.sql");
            // FileInputStream fstreamAlter = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator +
            // "database" + File.separator + "mysqlDB" + File.separator + "createReleaseDBUpdates_version3.sql");

            InputStreamReader inAlter = new InputStreamReader(fstreamAlter, "UTF-8");
            BufferedReader brAlter = new BufferedReader(inAlter);
            String strLineAlter;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineAlter = brAlter.readLine()) != null) {
                st.executeUpdate(strLineAlter);
            }
            inAlter.close();

        } catch (SQLException e) {
            msgErrorCreate = "ReleaseDBUpdates.sql:::An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "ReleaseDBUpdates.sql::A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    private static String PopulateReleaseDBUpdates(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";
        try {
            SDF_MysqlDatabase.log.info("populate ReleaseDBUpdates....");
            st = con.createStatement();
            st.executeUpdate("insert ignore into " + schemaName + ".ReleaseDBUpdates values(1,'3','Version3','N')");
        } catch (SQLException e) {
            msgErrorCreate = "insert_ReleaseDBUpdates_version3:An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "insert_ReleaseDBUpdates_version3:A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
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
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "alterHabitatDataQuality:A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }
    }

    private static String InsertRefDataQual(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.log.info("Inserting DD for Habitats in RefDataQuality...");

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
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream

            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "insert_RefDataQual_version3.sql:An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "insert_RefDataQual_version3:A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    private static String UpdateRefHabitats(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.log.info("Updating RefHabitats ...");

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
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream

            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "UpdateRefHabitats.sql:An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "UpdateRefHabitats:A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    private static String UpdateVersion3Done(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        String schemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";

        try {
            SDF_MysqlDatabase.log.info("Updating UpdateVersion3Done ...");
            st = con.createStatement();
            st.executeUpdate("update " + schemaName + ".ReleaseDBUpdates SET UPDATE_DONE='Y' WHERE RELEASE_NUMBER = 3");
        } catch (SQLException e) {
            msgErrorCreate = "UpdateVersion3Done: An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "UpdateVersion3Done general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
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
            SDF_MysqlDatabase.log.error("New species Croatia does not exist");
        } finally {
            return tableExists;
        }
    }

    private static boolean isEmeraldUpdatesdone(Connection con, Statement st) {
        boolean updateDone = false;
        Statement stDBSpec = null;
        ResultSet rsDBEXist = null;

        try {

            String hql =
                    "select 1 from information_schema.columns where table_schema = 'natura2000' and table_name = 'habitat' "
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
            SDF_MysqlDatabase.log.error("Error checking Emrald updates " + e);
        }
        return updateDone;
    }

    private static String UpdateRefSpeciesCroatia(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.log.info("Updating RefSpecies Croatia ...");

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
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream

            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "UpdateRefHabitats.sql:An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "UpdateRefHabitats:A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {
            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

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

            log.info("Test if host is solved: ");
            InetSocketAddress endPoint = new InetSocketAddress(host, Integer.parseInt(port));
            if (endPoint.isUnresolved()) {
                return "Host cannot be resolved.";
            }
            log.info("Test if port is open: host='" + "'; port='" + port + "'");
            socket.connect(endPoint, 1000);

        } catch (IOException ie) {
            return "Mysql is not running or no access to " + host + ":" + port;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        } finally {
            IOUtils.closeQuietly(socket);
        }
        log.info("Testing MySQL existence: ");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/?socketTimeout=2000&user=" + user + "&password=" + pwd;
            DriverManager.getConnection(url);
        } catch (ClassNotFoundException cnfe) {
            return "Mysql database driver is not available.";
        } catch (CommunicationsException ce) {
            return "Mysql database is not available at " + host + ":" + port;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return sqle.getMessage();
        }

        return "";
    }

    private static String doEmeraldUpdates(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.log.info("Doing necessary DB struct updates for EMERALD data structure ...");

            FileInputStream fstreamInsert = openScriptFile("EmeraldChanges.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            // Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                st.executeUpdate(strLineInsert);
            }
            // Close the input stream
            inInsert.close();

        } catch (SQLException e) {
            msgErrorCreate = "EmeraldUpdates:An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "EmeraldUpdates:A general error has been produced";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } finally {

            if (st != null) {
                st.close();
            }
            return msgErrorCreate;
        }

    }

    /**
     * closes DB connection. if sql exception it is logged
     *
     * @param conn
     *            database connection
     */
    public static void closeQuietly(java.sql.Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqle) {
                log.error("Error closing database connection " + sqle);
            }
        }
    }

    static void closeStatement(Statement rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            log.error("Error closing statement " + e);
        }
    }

    /**
     * local method to keep the code cleaner.
     *
     * @return true is application is running in mode
     */
    private static boolean isEmeraldMode() {
        return SDF_ManagerApp.isEmeraldMode();
    }
}
