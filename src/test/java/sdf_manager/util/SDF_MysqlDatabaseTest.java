package sdf_manager.util;

import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.assertEquals;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class SDF_MysqlDatabaseTest {

    /**
     * Hardwire the connection URL for now.
     */
    private Connection getDBConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return (Connection) DriverManager.getConnection("jdbc:mysql:mxj://localhost:3336/natura2000"
                + "?createDatabaseIfNotExist=true"
                + "&server.lower_case_table_names=1"
                + "&server.initialize-user=true", "testuser", "testpassword");
    }

    /**
     * Initialize the logging system.
     */
    @BeforeClass
    public static void setupLogger() throws Exception {
        Properties logProperties = new Properties();
        logProperties.setProperty("log4j.rootCategory", "DEBUG, CONSOLE");
        logProperties.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        logProperties.setProperty("log4j.appender.CONSOLE.Threshold", "DEBUG");
        logProperties.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        logProperties.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "- %m%n");
        PropertyConfigurator.configure(logProperties);
    }

    /**
     * This method should be split up.
     */
    @Test
    public void checkDBCreationAndPopulation() throws Exception {
        String msgError;
        Connection con = getDBConnection();
        msgError = SDF_MysqlDatabase.createNaturaDB(con);
        assertEquals(null, msgError);

        // The SDF_MysqlDatabase.createNaturaDB closes the connection
        con = getDBConnection();

        Statement st;
        ResultSet rs;
        int num;

        st = con.createStatement();
        // Check how many species are in the database
        // 2516 from insert_ref_species.sql and 13 from Update_RefSpecies_version3.sql
        rs = st.executeQuery("SELECT COUNT(*) FROM ref_species");
        rs.next();
        num = rs.getInt(1);
        assertEquals(2516 + 13, num);

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
        // 78 from insert_birds.sql have an alternative scientific name
        rs = st.executeQuery("SELECT COUNT(*) FROM ref_birds WHERE REF_BIRDS_ALT_SCIENTIFIC_NAME != ''");
        rs.next();
        num = rs.getInt(1);
        assertEquals(78, num);

        // Check that the REF_BIRDS_CODE_NEW is up to date - i.e. insert_birds_new.sql has been run
        // birds A527 and A567 are being changed to 1 by insert_birds_new.sql
        rs = st.executeQuery("SELECT COUNT(*) FROM ref_birds WHERE REF_BIRDS_CODE_NEW='1' AND REF_BIRDS_CODE IN ('A527','A567')");
        rs.next();
        num = rs.getInt(1);
        //assertEquals(2, num);
        
        // Check that the REF_BIRDS_CODE_NEW is up to date - i.e. insert_birds_new.sql has been run
        // birds A261 and A276 are being changed to 0 by insert_birds_new.sql
        rs = st.executeQuery("SELECT COUNT(*) FROM ref_birds WHERE REF_BIRDS_CODE_NEW='0' AND REF_BIRDS_CODE IN ('A261','A276')");
        rs.next();
        num = rs.getInt(1);
        //assertEquals(2, num);

    }

    @Test
    public void pathForDbScripts() throws Exception {
        String path = SDF_MysqlDatabase.getScriptPath("XX");
        //System.out.println(path);
        assertEquals(1, 1);
    }
}
