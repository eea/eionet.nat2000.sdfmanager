package sdf_manager;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Class for common methods of all importers.
 *
 * @author Kaido Laine
 */
public class AbstractImporter {
    /**
     * Class logger.
     */
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(AbstractImporter.class.getName());

    /**
     * Logger of the parent object. Produces log to the tool UI
     */
    private Logger logger;

    /**
     * Special log file writer.
     */
    private FileWriter outFileWriter;

    /**
     * Log file print writer.
     */
    private PrintWriter out;

    /**
     * Output log file.
     */
    private File outFile;

    /**
     * Flag to indicate if logger has logged something to the special log file.
     */
    private boolean hasErrors = false;

    /**
     * Inits logger object.
     *
     * @param logger
     *            Logger of parent object that logs to the UI.
     * @param logFile
     *            special log file of the action
     */
    public AbstractImporter(Logger logger, String logFile) {
        this.logger = logger;
        this.initLogFile(logFile);
    }

    /**
     * Logs message.
     *
     * @param msg
     *            messge
     * @param logToScreen
     *            if true logs also to the tool window
     */
    public void log(String msg, boolean logToScreen) {
        if (logToScreen) {
            this.logger.log(msg);
        }
        logToFile(msg);
    }

    /**
     * Logs only to the screen.
     * @param msg message to be logged
     */
    public void log(String msg) {
        this.logger.log(msg);
    }

    /**
     * Initializes log file.
     *
     * @param fileName
     *            full file path
     */
    public void initLogFile(String fileName) {
        try {
            outFile = new File(fileName);
            outFileWriter = new FileWriter(outFile);
            out = new PrintWriter(outFileWriter);
        } catch (Exception e) {
            LOGGER.error("An error has occurred in initLogFile. Error Message :::" + e.getMessage());
        }
    }

    /**
     * Logs to special log file.
     *
     * @param msg
     *            text to be logged
     */
    void logToFile(String msg) {
        hasErrors = true;
        out.write(msg);
        if (!msg.endsWith("\n")) {
            out.write("\n");
        }
    }

    /**
     * Closes log file stream. If no errors deletes the 0kb file.
     */
    public void closeLogFile() {
        IOUtils.closeQuietly(outFileWriter);
        IOUtils.closeQuietly(out);
        if (!hasErrors) {
            FileUtils.deleteQuietly(outFile);
        }
    }
}
