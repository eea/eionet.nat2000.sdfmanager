package sdf_manager.validators.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import sdf_manager.validators.dao.SpeciesValidatorDao;
import sdf_manager.validators.model.FuzzyResult;
import sdf_manager.validators.model.ValidatorTableRow;


public class SpeciesValidatorTest {

	private SpeciesValidatorDao testClass;
	private String myJsonResult;
	
	@Before
	public void setUp() throws Exception {
		 testClass = mock(SpeciesValidatorDao.class);		 		 		  
	}

	/**
	 * Tests validator method for fuzzy results
	 * @throws Exception
	 */
	@Test
	public void testDoQueryFuzzy() throws Exception {				
		InputStream inputStream = getClass().getResourceAsStream("/validatorfuzzytest.json");
		myJsonResult = IOUtils.toString(inputStream);		
		when(testClass.getJsonResponse(isA(URIBuilder.class))).thenReturn(myJsonResult);
		when(testClass.doQueryFuzzy("Anything")).thenCallRealMethod();
		List<FuzzyResult> testList = testClass.doQueryFuzzy("Anything");
		assertEquals("Test", 3, testList.size());		
	}

	/**
	 * Tests validator method for accepted results
	 * @throws Exception
	 */
	@Test
	public void testDoQueryAccepted() throws Exception {	
		InputStream inputStream = getClass().getResourceAsStream("/validatoracceptedtest.json");
		myJsonResult = IOUtils.toString(inputStream);		
		when(testClass.getJsonResponse(isA(URIBuilder.class))).thenReturn(myJsonResult);
		when(testClass.doQueryAccepted(Matchers.anyListOf(String.class))).thenCallRealMethod();
		List<String> names = Arrays.asList("Anything");
		List<ValidatorTableRow> testList = testClass.doQueryAccepted(names);
		assertEquals("Test", 2, testList.size());	
	}

}
