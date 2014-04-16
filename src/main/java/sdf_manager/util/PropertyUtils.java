package sdf_manager.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * util methods for handling properties.
 *
 * @author Kaido Laine
 */
public final class PropertyUtils {

    /** class logger. */
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
        //Properties prop = new Properties();

        Properties prop = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<Object>(super.keySet()));
            }
        };

        FileOutputStream output = null;

        try {
            List<String> keys = new ArrayList<String>();
            keys.addAll(props.keySet());
            Collections.sort(keys);
            output = new FileOutputStream(fileName);

            for (String propName : keys) {
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


    /**
     * raeds properties from the file.
     * @param fileLocation file full path
     * @return properties container
     * @throws FileNotFoundException if file not found
     * @throws IOException if reading fails
     */
    public static Properties readProperties(String fileLocation) throws FileNotFoundException, IOException {
        FileInputStream input = null;
        Properties props = new Properties();
        try {
            input = new FileInputStream(fileLocation);
            props.load(input);

        } finally {
            IOUtils.closeQuietly(input);
        }
        return props;
    }
}
