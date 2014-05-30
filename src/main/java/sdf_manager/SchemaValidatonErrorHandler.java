package sdf_manager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Implementation of {@link ErrorHandler} that returns all received messages via {@link #getMessagesList()}.
 *
 * @author Jaanus
 */
public class SchemaValidatonErrorHandler implements ErrorHandler {

    /** Template for warning message. */
    private static final String WARNING_TEMPLATE = "<Line {0}, Column {1}> <WARN> {2}";

    /** Template for error message. */
    private static final String ERROR_TEMPLATE = "<Line {0}, Column {1}> <ERROR> {2}";

    /** Template for fatal message. */
    private static final String FATAL_TEMPLATE = "<Line {0}, Column {1}> <FATAL> {2}";

    /** List of string messages received from the below callbacks. */
    private List<String> messagesList = new ArrayList<String>();

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(SAXParseException e) throws SAXException {

        messagesList.add(MessageFormat.format(WARNING_TEMPLATE, e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException e) throws SAXException {
        messagesList.add(MessageFormat.format(ERROR_TEMPLATE, e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        messagesList.add(MessageFormat.format(FATAL_TEMPLATE, e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
    }

    /**
     * @return the messagesList
     */
    public List<String> getMessagesList() {
        return messagesList;
    }
}
