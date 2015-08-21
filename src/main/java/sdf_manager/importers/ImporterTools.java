package sdf_manager.importers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilities for SDF Manager Importers
 * @author George Sofianos
 *
 */
public class ImporterTools {
	
	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImporterTools.class.getName());
	
	/**
	 * Converts String into Date, with strict interpretation.
	 * @param siteCode - the code of the site the date field is a part of
	 * @param fieldName - the name of the date field
	 * @param str - the string that needs to be parsed
	 * @return date object or null
	 */
    public static Date parseXmlDate(String siteCode, String fieldName, String str) {
    	try {    		    
	        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");   
	        fmt.setLenient(false); /* With lenient parsing, the parser may use heuristics to interpret inputs that do not precisely match this object's format. 
	        With strict parsing, inputs must match this object's format. */ 
	        return fmt.parse(str);
    	} catch (ParseException ex) {
    		log.error("Can't parse date for site: " + siteCode + " and field: " + fieldName);  		
    		return null;
    	}
    }

}