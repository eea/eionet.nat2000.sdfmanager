package sdf_manager.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

/**
 * General Uril methods for importer.
 *
 * @author Kaido Laine
 */
public final class ImporterUtils {

    /** class logger. */
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ImporterUtils.class);

    /**
     * prevent init.
     */
    private ImporterUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * returns values of double values if field type is actually string in the db. format may be inserted with comma although it
     * must be dot.
     *
     * @param str
     *            value expected to be double
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

    /**
     * Returns given String value in specified encoding.
     * If UTF-8 the value is not changed
     * @param resultStr Value of the field
     * @param encoding
     *            encoding, supported by Java
     * @return String
     */
    public static String getString(String resultStr, String encoding) {
        try {
            if (!("UTF-8").equalsIgnoreCase(encoding)) {
                if (StringUtils.isBlank(resultStr)) {
                    return null;
                } else {
                    if ("Cyrillic (cp866)".equals(encoding)) {
                       return convertUtfToSpecialCyrillic(resultStr);
                    } else {

                        byte[] result = resultStr.getBytes();

                        Charset charset = Charset.forName(encoding);
                        CharsetDecoder decoder = charset.newDecoder();
                        decoder.onMalformedInput(CodingErrorAction.REPLACE);
                        decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

                        CharBuffer cbuf = decoder.decode(ByteBuffer.wrap(result));
                    return cbuf.toString().trim();
                    }
                }
            } else {
                return resultStr;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed converting value: " + resultStr + " to encoding " + encoding);
            return null;
        }
    }

    /**
     * Special conversion to handle emerald tools cyrillic encoding.
     * @param value value in utf-8
     * @return value in cyrillic
     * @throws Exception if fail in conversion
     */
    private static String convertUtfToSpecialCyrillic(String value) throws Exception {
        byte[] skipBytes = value.getBytes();

        byte[] other = new byte[skipBytes.length];
        int j = 0, k = 0;
        //194 + x + 66?
        byte lastByte = 0;
        for (byte b : skipBytes) {

            if (b + 256 != 195 && b + 256 != 194) {
                //Ukrainian special chars 194 + byte:
                if (lastByte + 256 == 194) {
                    if (b + 256 == 179) {
                        other[j] = (byte)105;
                    } else if (b + 256 == 191) {
                        other[j] = (byte)245;
                    } else if (b + 256 == 187) {
                        other[j] = (byte)243;
                    } else {
                        other [j] = (byte) (b + 48);
                    }
                } else if (b + 256 >= 176 && b + 256 <= 199) {
                    other [j] = (byte) (b + 48);
                } else {
                    other[j] = b;
                }
                j++;
            }

            lastByte = b;
        }
        return (new String(other, "cp866")).trim();
    }

}
