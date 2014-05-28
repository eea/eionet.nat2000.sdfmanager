package sdf_manager.util;

import java.io.File;
import java.util.Calendar;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XmlGeneratorUtilTest {
    private static final Logger LOGGER = Logger.getLogger(XmlGeneratorUtilTest.class);

    @Test
    public void testAppendDate() throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        File schemaFile = new File("target/test-classes/testschema.xsd");
        Schema schema = schemaFactory.newSchema(schemaFile);
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();

        dbfac.setSchema(schema);

        DocumentBuilder builder = dbfac.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element siteIdentification = doc.createElement("siteIdentification");
        doc.appendChild(siteIdentification);
        Calendar cal =  Calendar.getInstance();
        cal.set(2012, 11, 0, 0, 0);
        XmlGenerationUtils.appendDateElement(cal.getTime(), siteIdentification, "asciProposalDate", doc);

        NodeList  dateElem = doc.getElementsByTagName("asciProposalDate");

        Assert.assertEquals("2012-11", dateElem.item(0).getTextContent());
    }
}
