package sdf_manager.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for font management  
 * @author George Sofianos
 *
 */
public final class FontsUtil {

	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(FontsUtil.class);	
	public static Font openSansSemiBoldItalic;
	
	private FontsUtil() {
		// do nothing
	}
	/**
	 * Loads application wide fonts
	 *  
	 */
	public static void loadFonts() {
		logger.info("Loading application-wide fonts");
		InputStream stream = FontsUtil.class.getResourceAsStream("/fonts/opensans/OpenSans-SemiboldItalic.ttf");
		try {				
			openSansSemiBoldItalic = Font.createFont(Font.TRUETYPE_FONT, stream);
		} catch (FontFormatException e) {
			logger.error("Wrong font format" + e);
		} catch (IOException e) {
			logger.error("Could not read font file" + e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				logger.error(e);
			}			
		}
	}
	
	public static Font getFont(Font font, float size) {							
		Font derivedFont = font.deriveFont(size);
		return derivedFont;
	}

}
