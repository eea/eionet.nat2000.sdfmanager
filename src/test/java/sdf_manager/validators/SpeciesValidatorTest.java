package sdf_manager.validators;

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


public class SpeciesValidatorTest {

	private SpeciesValidator testClass;
	private String myJsonResult;
	
	@Before
	public void setUp() throws Exception {
		 testClass = mock(SpeciesValidator.class);		 		 		  
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
		List<ValidatorResultsRow> testList = testClass.doQueryFuzzy("Anything");
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
		List<String> names = Arrays.asList("Anything", "Anything2");
		List<ValidatorResultsRow> testList = testClass.doQueryAccepted(names);
		assertEquals("Test", 1, testList.size());	
	}

}
