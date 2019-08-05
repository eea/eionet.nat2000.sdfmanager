package sdf_manager.util;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility classes for generating XMl.
 *
 * @author Kaido Laine
 */
public final class XmlGenerationUtils {

    /** prevent initialization. */
    private XmlGenerationUtils() {

    }

    /**
     * Adds date elem to the xml if date is not empty.
     *
     * @param date
     *            date type property of an object
     * @param siteIdentification
     *            site Elem in teh xml
     * @param nodeName
     *            name for the element node
     * @param doc
     *            xml document object
     */
    public static void appendDateElement(Date date, Element siteIdentification, String nodeName, Document doc) {
        if (date != null) {
            siteIdentification.appendChild(doc.createElement(nodeName)).appendChild(
                    doc.createTextNode(SDF_Util.getFormatDateToXML(date)));
        }

    }

}
