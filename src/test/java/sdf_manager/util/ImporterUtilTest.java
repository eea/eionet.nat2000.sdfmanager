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


}