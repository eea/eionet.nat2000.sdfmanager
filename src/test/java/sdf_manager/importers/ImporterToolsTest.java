package sdf_manager.importers;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ImporterToolsTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testParseXmlDate() {
		String wrongDate = "2012-13"; // Month 13 doesn't exist, so we expect an exception			
		Date x = ImporterTools.parseXmlDate("test", "test", wrongDate);		
		assertNull("Set lenient to false", x);
	}
	@Test
	public void testParseMdbDate() {
		String wrongDate = "201213"; // Month 13 doesn't exist, so we expect an exception			
		Date x = ImporterTools.parseMdbDate("test", "test", wrongDate);		
		assertNull("Set lenient to false", x);
	}

}
