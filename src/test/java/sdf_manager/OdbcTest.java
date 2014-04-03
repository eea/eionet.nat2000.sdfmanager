package sdf_manager;

import org.junit.Test;
import static org.junit.Assert.assertNotEquals;

public class OdbcTest {

    /**
     * The JdbcOdbcDriver is needed on Windows as it is an integral part of the
     * application logic. It is not needed when we test on Linux.
     */
    @Test
    public void isOdbcDriverLoadable() throws Exception {
        String fs = System.getProperty("file.separator");
        //System.out.println(System.getProperty("os.name"));

        if ("\\".equals(fs)) {
            //try {
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            //} catch (ClassNotFoundException e) {
            //    System.out.println("Class JdbcOdbcDriver not found - needed on Windows");
            //}
        } else {
            assertNotEquals("\\", fs);
        }
    }
}
