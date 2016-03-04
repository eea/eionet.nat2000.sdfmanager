package sdf_manager.util;

import static org.junit.Assert.assertEquals;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import sdf_manager.SDF_ManagerApp;

import com.mysql.jdbc.Connection;


public class SDF_MysqlDatabaseTest {

    /**
     * returns database connection
     */
    private Connection getDBConnection(String mode) throws Exception {

        ResourceBundle bundle = ResourceBundle.getBundle("liquibase");

        Properties properties = new Properties();
        String driver = bundle.getString("driver");
        String url = bundle.getString("url");

        if (mode.equals(SDF_ManagerApp.EMERALD_MODE)) {
            url = StringUtils.replaceOnce(url, "natura2000", "emerald");
        }
        properties.setProperty("http://www.dbunit.org/properties/datatypeFactory", "org.dbunit.ext.mysql.MySqlDataTypeFactory");
        properties.setProperty("user", bundle.getString("username"));
        properties.setProperty("password", bundle.getString("password"));

        Class.forName(driver);
        Connection jdbcConn = (Connection)DriverManager.getConnection(url, properties);

        return jdbcConn;
    }


    /**
     * deafault connection is natura 2000 mode.
     * @return db connection
     * @throws Exception if fail
     */
    private Connection getDBConnection() throws Exception {
        return getDBConnection(SDF_ManagerApp.NATURA_2000_MODE);
    }

    /**
     * This method should be split up.
     */
    @Test
    public void checkDBCreationAndPopulation() throws Exception {
        String msgError;
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            msgError = SDF_MysqlDatabase.createOrUpdateDatabaseTables(con, false, null);
            assertEquals(null, msgError);

            // The SDF_MysqlDatabase.createNaturaDB closes the connection
            con = getDBConnection();


            int num;

            st = con.createStatement();
            // Check how many species are in the database
            // 2516 from insert_ref_species.sql and 13 from Update_RefSpecies_version3.sql
            // 13 from Update_RefSpecies_version3.sql
            // 116 from ver 4.2 update: updates/4.2/update_species_42.sql
            // -1 from ver 4.2.1 update: updates/4.2.1/update_species_421.sql
            rs = st.executeQuery("SELECT COUNT(*) FROM ref_species");
            rs.next();
            num = rs.getInt(1);
            //ref_species count is sum of updates from different releases of the tool, see above
            assertEquals(2516 + 13 + 116 - 1, num);

            // Check how many NUTS codes are loaded
            // 299 from insert_ref_nuts.sql
            rs = st.executeQuery("SELECT COUNT(*) FROM ref_nuts");
            rs.next();
            num = rs.getInt(1);
            assertEquals(299, num);

            // Check how many habitats are loaded
            // 231 from insert_ref_habitats.sql and 2 from Update_RefHabitats_version3.sql
            rs = st.executeQuery("SELECT COUNT(*) FROM ref_habitats");
            rs.next();
            num = rs.getInt(1);
            assertEquals(231 + 2, num);

            // Check how many data quality codes are loaded
            // 4 from insert_ref_data_quality.sql and 1 from insert_RefDataQual_version3.sql
            rs = st.executeQuery("SELECT COUNT(*) FROM ref_data_quality");
            rs.next();
            num = rs.getInt(1);
            assertEquals(4 + 1, num);

            // Check that Alter_Ref_Birds_table.sql and insert_birds.sql are run
            // 78 from insert_birds.sql plus one from insert_birds_new.sql have an alternative scientific name
            rs = st.executeQuery("SELECT COUNT(*) FROM ref_birds WHERE REF_BIRDS_ALT_SCIENTIFIC_NAME != ''");
            rs.next();
            num = rs.getInt(1);
            assertEquals(78 + 1, num);

            // Check that the REF_BIRDS_CODE_NEW is up to date - i.e. insert_birds_new.sql has been run
            // birds A527 and A567 are being changed to 1 by insert_birds_new.sql
            rs =
                    st.executeQuery("SELECT COUNT(*) FROM ref_birds WHERE REF_BIRDS_CODE_NEW='1' AND REF_BIRDS_CODE IN ('A527','A567')");
            rs.next();
            num = rs.getInt(1);
            assertEquals(2, num);

            // Check that the REF_BIRDS_CODE_NEW is up to date - i.e. insert_birds_new.sql has been run
            // birds A261 and A276 are being changed to 0 by insert_birds_new.sql
            rs =
                    st.executeQuery("SELECT COUNT(*) FROM ref_birds WHERE REF_BIRDS_CODE_NEW='0' AND REF_BIRDS_CODE IN ('A261','A276')");
            rs.next();
            num = rs.getInt(1);
            assertEquals(2, num);
        } finally {

            SDF_MysqlDatabase.closeQuietly(con);
        }

    }



    @Test
    public void pathForDbScripts() throws Exception {
        String path = SDF_MysqlDatabase.getScriptPath("XX");
        //System.out.println(path);
        assertEquals(1, 1);
    }
}
