package sdf_manager.util;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;


public class ImporterUtilTest {

    private static final Logger LOGGER = Logger.getLogger(ImporterUtilTest.class);

    @Test
    public void testFixAndGetDouble() throws Exception {

        Assert.assertTrue(11 == ImporterUtils.fixAndGetDouble("11"));

        Assert.assertTrue(11.3 == ImporterUtils.fixAndGetDouble("11,3"));
        Assert.assertTrue(1.234 == ImporterUtils.fixAndGetDouble("1.234"));
        Assert.assertTrue(12.3 == ImporterUtils.fixAndGetDouble("12,300"));

    }

    @Test
    public void tesGetStringUtf8() {
        String value = "Меня зовут Василий";
        Assert.assertEquals("Меня зовут Василий", ImporterUtils.getString(value, "UTF-8"));
    }


    /**
     *
     */
    @Test
    public void testLocaleUtf8() {
        // Verify that javac can handle "Elektra" in the Greek alphabet
        char elektraBytes[] = {
            '\u0397', '\u03bb', '\u03ad', '\u03ba', '\u03c4', '\u03c1', '\u03b1'
        };
        String elektra = new String(elektraBytes);
        Assert.assertEquals("Dude, set your locale to UTF-8", "Ηλέκτρα", elektra);
    }
}