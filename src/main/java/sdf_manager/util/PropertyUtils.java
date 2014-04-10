package sdf_manager.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * util methods for handling properties.
 *
 * @author Kaido Laine
 */
public final class PropertyUtils {

    private static final Logger LOGGER = Logger.getLogger(PropertyUtils.class);

    /**
     * do not create instance.
     */
    private PropertyUtils() {

    }

    /**
     * creates a props file from the given props.
     *
     * @param fileName
     *            full file path
     * @param props
     *            properties hash
     *  @throws IOException if writing fails
     */
    public static void writePropsToFile(String fileName, Map<String, String> props) throws IOException {
        Properties prop = new Properties();
        FileOutputStream output = null;

        try {

            output = new FileOutputStream(fileName);

            for (String propName : props.keySet()) {
                LOGGER.info("name " + propName);
                String v = props.get(propName);
                LOGGER.info("valye " + v);
                prop.setProperty(propName, v);
            }

            prop.store(output, null);

        } finally {
            IOUtils.closeQuietly(output);
        }
    }

}
