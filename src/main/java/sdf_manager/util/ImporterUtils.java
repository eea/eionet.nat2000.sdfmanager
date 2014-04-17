package sdf_manager.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * General Uril methods for importer.
 *
 * @author Kaido Laine
 */
public final class ImporterUtils {

    /** class logger. */
    private static final Logger LOGGER = Logger.getLogger(ImporterUtils.class);

    /**
     * prevent init.
     */
    private ImporterUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * returns values of double values if field type is actually string in the db.
     * format may be inserted with comma although it must be dot.
     * @param str value expected to be double
     * @return double value
     */
    public static Double fixAndGetDouble(String str) {
        try {

            if (StringUtils.isNotBlank(str)) {
                str = str.replace(',', '.');
                return Double.valueOf(str);
            }

            return null;
        } catch (Exception e) {
            LOGGER.error("Failed extracting value expected to be double: " + str + ". Error:::" + e.getMessage());
            return null;
        }
    }
}
