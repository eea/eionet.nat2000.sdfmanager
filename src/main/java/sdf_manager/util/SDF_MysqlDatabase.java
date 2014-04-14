/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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

import org.apache.log4j.Logger;

import com.mysql.jdbc.Connection;


/**
 *
 *
 * @author
 */
public class SDF_MysqlDatabase {

    private final static Logger log = Logger.getLogger(SDF_MysqlDatabase.class .getName());

    /**
     * Create the JDBC URL, open a connection to the database and set up tables.
     *
     *
     * @return the connection to the database
     */
    public static String createNaturaDB(Properties properties) throws SQLException, Exception {
        Connection con;
        Class.forName("com.mysql.jdbc.Driver");
        SDF_MysqlDatabase.log.info("Connection to MySQL: user==>" + properties.getProperty("user")
                + "<==password==>" + properties.getProperty("password") + "<==");
        con = (Connection) DriverManager.getConnection("jdbc:mysql://"
                + properties.getProperty("host") + ":" + properties.getProperty("port")
                + "/", properties.getProperty("user"), properties.getProperty("password"));
        return createNaturaDB(con);
    }

    /**
     * Create the Natura2000 database.
     * @return
     * @throws SQLException
     * @throws Exception
     */
    public static String createNaturaDB(Connection con) throws SQLException, Exception {

        String msgError = null;

        Statement stDBExist = null;
        ResultSet rsDBEXist = null;
        Statement stDBUser = null;
        ResultSet rsDBUser = null;
        try {
            try {

               //dataBase exist??
                String schemaFileName = "CreateSDFSchema.sql";
                String sqlDBUser = "select * from mysql.user where user='sa'";
                stDBUser = con.createStatement();

                rsDBEXist = stDBUser.executeQuery(sqlDBUser);
                if (rsDBEXist.next()) {
                    SDF_MysqlDatabase.log.info("User=sa, already exist");
                    schemaFileName = "CreateSDFOnlySchema.sql";
                }

                String sqlDBExist = "SELECT SCHEMA_NAME as name FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'natura2000'";
                stDBExist = con.createStatement();
                rsDBEXist = stDBExist.executeQuery(sqlDBExist);

                if (rsDBEXist.next()) {
                   //Create Data Base
                    String name = rsDBEXist.getString("name");

                    if (name != null && !(("").equals(name))) {
                        if (isRefSpeciesUpdated(con, stDBExist)) {
                            SDF_MysqlDatabase.log.info("natura2000 Schema DB already exists and ref species table is OK");
                        } else {
                            SDF_MysqlDatabase.log.info("Drop Schema");
                            String sql = "drop schema natura2000";
                            Statement st = con.createStatement();
                            st.executeUpdate(sql);
                            SDF_MysqlDatabase.log.info("Recreate Schema");
                            msgError = createMySQLDB(con, schemaFileName);
                            String msgErrorPopulate = populateRefTables(con);
                            if (msgErrorPopulate != null) {
                                msgError = msgError + "\n" + msgErrorPopulate;
                            }
                        }
                        if (isRefBirdsUpdated(con, stDBExist)) {
                             SDF_MysqlDatabase.log.info("natura2000 Schema DB already exists and ref birds table is OK");
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


                        //Sept 2013. Version 3. Create ReleaseDBUpdates.

                        if (isReleaseDBUpdatesExist(con, stDBExist)) {
                            SDF_MysqlDatabase.log.info("ReleaseDBUpdates exists");
                        } else {
                           SDF_MysqlDatabase.log.info("Create ReleaseDBUpdates");
                            msgError = createReleaseDBUpdates(con);
                            String msgErrorPopulate = PopulateReleaseDBUpdates(con);
                            String msgErrorAlterHabitat = alterHabitatQual(con);
                            String msgErrorAlterDataQual = InsertRefDataQual(con);
                            String msgErrorUpdateRefHabitats = UpdateRefHabitats(con);
                            //tabla ref species
                            String msgErrorUpdateVersion3Done = UpdateVersion3Done(con);
                            if (msgErrorPopulate != null || msgErrorAlterHabitat != null || msgErrorAlterDataQual != null || msgErrorUpdateRefHabitats != null || msgErrorUpdateVersion3Done != null) {
                                msgError = msgError + "\n" + msgErrorPopulate  + "\n"
                                      + msgErrorAlterHabitat  + "\n" + msgErrorAlterDataQual  + "\n"
                                      + msgErrorUpdateRefHabitats  + "\n" + msgErrorUpdateVersion3Done;
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

                        //Sept 2013


                    } else {
                        msgError = createMySQLDB(con, schemaFileName);
                        String msgErrorPopulate = populateRefTables(con);
                        if (msgErrorPopulate != null) {
                            msgError = msgError + "\n" + msgErrorPopulate;
                        }
                    }
                    if (!isDateTypeColumnsLongText(con, stDBUser)) {
                        String msgErrorPopulate = alterDateColumnsType(con);
                        if (msgErrorPopulate != null) {
                            msgError = msgError + "\n" + msgErrorPopulate;
                        }

                    }

                } else {
                    msgError = createMySQLDB(con, schemaFileName);
                    String msgErrorPopulate = populateRefTables(con);
                    if (msgErrorPopulate != null) {
                         msgError = msgError + "\n" + msgErrorPopulate;
                    }

                    //Sept 2013. Version 3. Create ReleaseDBUpdates.
                    if (isReleaseDBUpdatesExist(con, stDBExist)) {
                        SDF_MysqlDatabase.log.info("ReleaseDBUpdates exists");
                    } else {
                        SDF_MysqlDatabase.log.info("Create ReleaseDBUpdates");
                        msgError = createReleaseDBUpdates(con);
                        String msgErrorPopulateRel = PopulateReleaseDBUpdates(con);
                        String msgErrorAlterHabitat = alterHabitatQual(con);
                        String msgErrorAlterDataQual = InsertRefDataQual(con);
                        String msgErrorUpdateRefHabitats = UpdateRefHabitats(con);
                        //tabla ref species
                        String msgErrorUpdateVersion3Done = UpdateVersion3Done(con);
                        if (msgErrorPopulateRel != null || msgErrorAlterHabitat != null || msgErrorAlterDataQual != null || msgErrorUpdateRefHabitats != null || msgErrorUpdateVersion3Done != null) {
                            msgError = msgError + "\n" + msgErrorPopulate  + "\n"
                                    + msgErrorAlterHabitat  + "\n" + msgErrorAlterDataQual  + "\n"
                                    + msgErrorUpdateRefHabitats  + "\n" + msgErrorUpdateVersion3Done;
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
                    //Sept 2013


                }


            } catch (SQLException s) {
                JOptionPane.showMessageDialog(new JFrame(), "Error in Data Base", "Dialog", JOptionPane.ERROR_MESSAGE);
                SDF_MysqlDatabase.log.error("Error in Data Base:::" + s.getMessage());
                throw s;
            }
        } catch (Exception e) {
            msgError = "The connection to MySQL Data Base has failed.\n"
                    + " Please, Make sure that the parameters (user and password) in the properties file are right";
            JOptionPane.showMessageDialog(new JFrame(), msgError, "Dialog", JOptionPane.ERROR_MESSAGE);
            SDF_MysqlDatabase.log.error("The connection to MySQL Data Base has failed.\n"
                    + " Please, Make sure that the parameters (user and password) in the properties file are right.::" + e.getMessage());
            throw e;
        } finally {
            if (rsDBEXist != null) {
                rsDBEXist.close();
            }
            if (stDBExist != null) {
            }
            con.close();
        }
        return msgError;
    }

    /**
     * Method to validate if the datatype of comlumns:
     * SITE_EXPLANATIONS,SITE_SAC_LEGAL_REF,SITE_SPA_LEGAL_REF
     * of the table: site
     * in DB are longtext instead of varchar(512)
     * @param con
     * @param st
     * @return
     * @throws SQLException
     */
    private static boolean isDateTypeColumnsLongText(Connection con, Statement st) throws SQLException {
        boolean refSpeciesUpdated = true;

        //It's necessary to compare not only datatyp but alos the size of the column

        String columnTypeVarchar = "VARCHAR";
        int columnSizeVarchar = 512;
        try {
            String sql = "SELECT SITE_EXPLANATIONS,SITE_SAC_LEGAL_REF,SITE_SPA_LEGAL_REF FROM natura2000.site";
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
     *
     * @param con
     * @param schemaFileName
     * @return
     * @throws SQLException
     */
    private static String createMySQLDB(Connection con, String schemaFileName) throws SQLException {
        boolean mySQLDB = false;
        String msgErrorCreate = null;
        Statement st = null;
        Statement st2 = null;
        Statement stAlter = null;
        Statement stInsert = null;
        try {
            SDF_MysqlDatabase.log.info("Creating Schema Data Base");

            FileInputStream fstreamSchema = openScriptFile(schemaFileName);
            //FileInputStream fstreamSchema = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + schemaFileName);
            InputStreamReader inSchema = new InputStreamReader(fstreamSchema);
            BufferedReader brSchema = new BufferedReader(inSchema);
            String strLineSchema;
            st = con.createStatement();
            //Read File Line By Line
            while ((strLineSchema = brSchema.readLine()) != null) {
                st.executeUpdate(strLineSchema);
            }
            //Close the input stream
            inSchema.close();

            // Open the file that is the first
            // Create tables in Data Base
            SDF_MysqlDatabase.log.info("Creating tables in Data Base");
            FileInputStream fstream = openScriptFile("CreateMySqlTables.sql");
            //FileInputStream fstream = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "CreateMySqlTables.sql");

            InputStreamReader in = new InputStreamReader(fstream, "UTF-8");
            BufferedReader br = new BufferedReader(in);
            String strLine;
            st2 = con.createStatement();
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                st2.executeUpdate(strLine);
            }
            //Close the input stream
            in.close();

            //Populate data base
            SDF_MysqlDatabase.log.info("Populating tables");
            File dir = new File(getScriptPath("populateDB"));
            //File dir = new File(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "populateDB");

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
                    //Read File Line By Line
                    while ((strLineInsert = brInsert.readLine()) != null) {
                        stInsert.executeUpdate(strLineInsert);
                    }
                    //Close the input stream
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
              //FileInputStream fstreamAlter = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "alteColumnDatatype.sql");

              InputStreamReader inAlter = new InputStreamReader(fstreamAlter, "UTF-8");
              BufferedReader brAlter = new BufferedReader(inAlter);
              String strLineAlter;
              st = con.createStatement();
              //Read File Line By Line
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

        try {
              String sql = "select REF_SPECIES_CODE_NEW from natura2000.ref_species";
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

        try {
              String sql = "select HABITAT_COVER_HA from natura2000.habitat";
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
        try {
              String sql = "select REF_BIRDS_CODE_NEW from natura2000.ref_BIRDS";
              st = con.createStatement();
              st.executeQuery(sql);
              refBirdsUpdated = true;
        } catch (Exception e) {
            refBirdsUpdated = false;
            SDF_MysqlDatabase.log.error("Ref Birds is already updated");
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

        try {
            SDF_MysqlDatabase.log.info("alterRefBirds....");
              FileInputStream fstreamAlter = openScriptFile("Alter_Ref_Birds_table.sql");
              //FileInputStream fstreamAlter = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "Alter_Ref_Birds_table.sql");

              InputStreamReader inAlter = new InputStreamReader(fstreamAlter);
              BufferedReader brAlter = new BufferedReader(inAlter);
              String strLineAlter;
              st = con.createStatement();
              //Read File Line By Line
              String sqlAlter = "ALTER TABLE `natura2000`.`ref_birds` ADD COLUMN `REF_BIRDS_CODE_NEW` VARCHAR(1) NULL  AFTER `REF_BIRDS_ANNEXIIIPB` , ADD COLUMN `REF_BIRDS_ALT_SCIENTIFIC_NAME` VARCHAR(1024) NULL  AFTER `REF_BIRDS_CODE_NEW` ;";
              while ((strLineAlter = brAlter.readLine()) != null) {
                st.executeUpdate(sqlAlter);
              }
              inAlter.close();


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

            FileInputStream fstreamInsert = openScriptFile("insert_birds_new.sql");
            //FileInputStream fstreamInsert = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator + "insert_birds_new.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            //Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                st.executeUpdate(strLineInsert);
            }
          //Close the input stream

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

        try {
              st = con.createStatement();
              //Read File Line By Line
              String sqlAlter = "ALTER TABLE `natura2000`.`habitat` ADD COLUMN `HABITAT_COVER_HA` DOUBLE NULL;";
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
        boolean refBirdsUpdated = false;

        try {
            String sql = "select * from natura2000.ref_impact_rank";
            st = con.createStatement();
            st.executeQuery(sql);
            refBirdsUpdated = true;
        } catch (Exception e) {
            refBirdsUpdated = false;
            SDF_MysqlDatabase.log.error("Ref tables not exist");
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
    private static String createRefTables(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        try {
              FileInputStream fstreamAlter = openScriptFile("CreateRefTables.sql");
              //FileInputStream fstreamAlter = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "CreateRefTables.sql");

              InputStreamReader inAlter = new InputStreamReader(fstreamAlter, "UTF-8");
              BufferedReader brAlter = new BufferedReader(inAlter);
              String strLineAlter;
              st = con.createStatement();
              //Read File Line By Line
              while ((strLineAlter = brAlter.readLine()) != null) {
                  st.executeUpdate(strLineAlter);
              }
              inAlter.close();

        } catch (SQLException e) {
            msgErrorCreate = "CreateRefTables.sqll:::An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
      } catch (Exception e) {
            msgErrorCreate = "CreateRefTables.sql::A general error has been produced";
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
    private static String populateRefTables(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;
        try {

            //Populate data base
            SDF_MysqlDatabase.log.info("Populating Ref tables");
            File dir = new File(getScriptPath("populateDB" + File.separator + "ref_tables"));
            //File dir = new File(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator + "ref_tables");

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
                    //Read File Line By Line
                    while ((strLineInsert = brInsert.readLine()) != null) {
                        st.executeUpdate(strLineInsert);
                    }
                   //Close the input stream
                    inInsert.close();

                }
            }

        } catch (SQLException e) {
            msgErrorCreate = "insert_birds_new.sql:::An error has been produced in database";
            SDF_MysqlDatabase.log.error(msgErrorCreate + ".::::" + e.getMessage());
        } catch (Exception e) {
            msgErrorCreate = "insert_birds_new.sql::A general error has been produced";
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

        try {
            String sql = "select * from natura2000.ReleaseDBUpdates";
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
            //FileInputStream fstreamAlter = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "createReleaseDBUpdates_version3.sql");

            InputStreamReader inAlter = new InputStreamReader(fstreamAlter, "UTF-8");
            BufferedReader brAlter = new BufferedReader(inAlter);
            String strLineAlter;
            st = con.createStatement();
            //Read File Line By Line
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

        try {
            SDF_MysqlDatabase.log.info("populate ReleaseDBUpdates....");
            st = con.createStatement();
            st.executeUpdate("insert ignore into natura2000.ReleaseDBUpdates values(1,'3','Version3','N')");
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

        try {
            st = con.createStatement();
            //Read File Line By Line
            String sqlAlter = "ALTER TABLE `natura2000`.`habitat` MODIFY COLUMN `HABITAT_DATA_QUALITY` varchar(2);";
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
            //FileInputStream fstreamInsert = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator + "insert_RefDataQual_version3.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            //Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                st.executeUpdate(strLineInsert);
            }
          //Close the input stream

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
            //FileInputStream fstreamInsert = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator + "Update_RefHabitats_version3.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            //Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                st.executeUpdate(strLineInsert);
            }
          //Close the input stream

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

        try {
            SDF_MysqlDatabase.log.info("Updating UpdateVersion3Done ...");
            st = con.createStatement();
            st.executeUpdate("update natura2000.ReleaseDBUpdates SET UPDATE_DONE='Y' WHERE RELEASE_NUMBER = 3");
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

        try {

            String hql = "select REF_SPECIES_CODE from natura2000.ref_species where REF_SPECIES_CODE = '6337'";
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

    private static String UpdateRefSpeciesCroatia(Connection con) throws SQLException {
        String msgErrorCreate = null;
        Statement st = null;

        try {
            SDF_MysqlDatabase.log.info("Updating RefSpecies Croatia ...");

            FileInputStream fstreamInsert = openScriptFile("populateDB" + File.separator + "Update_RefSpecies_version3.sql");
            //FileInputStream fstreamInsert = new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "mysqlDB" + File.separator + "populateDB" + File.separator + "Update_RefSpecies_version3.sql");

            InputStreamReader inInsert = new InputStreamReader(fstreamInsert, "UTF-8");
            BufferedReader brInsert = new BufferedReader(inInsert);
            String strLineInsert;
            st = con.createStatement();
            //Read File Line By Line
            while ((strLineInsert = brInsert.readLine()) != null) {
                st.executeUpdate(strLineInsert);
            }
          //Close the input stream

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
        return new java.io.File("").getAbsolutePath()
            + File.separator + "database"
            + File.separator + "mysqlDB"
            + File.separator + scriptName;
    }


    /**
     * testing if user entered props in the system settings screen are correct.
     * @param host database host
     * @param port port
     * @param user DB user name
     * @param pwd password
     * @returns error message
     */
    public static String testConnection(String host, String port, String user, String pwd) {
        log.info("Testing MySQL: ");
        try {
            DriverManager.getConnection("jdbc:mysql://"
                    + host + ":" + port
                    + "/", user, pwd);
        } catch (SQLException sqle) {
               return sqle.getMessage();
        }

        return "";
    }


}
