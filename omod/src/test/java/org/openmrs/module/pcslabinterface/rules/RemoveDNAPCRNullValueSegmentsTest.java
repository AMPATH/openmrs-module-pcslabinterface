package org.openmrs.module.pcslabinterface.rules;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

public class RemoveDNAPCRNullValueSegmentsTest {
	/**
	 * @see {@link RemoveDNAPCRNullValueSegments#RemoveDNAPCRNullValueSegments()}
	 */
	@Test
	@Verifies(value = "should only match DNA PCR tests with null values", method = "RemoveDNAPCRNullValueSegments()")
	public void RemoveDNAPCRNullValueSegments_shouldOnlyMatchDNAPCRTestsWithNullValues()
			throws Exception {
		// with null value
		String hl7string = "OBX|1|CWE|1030^DNA ABBORT^99DCT||^^99DCT|||||||||20080206";
		Assert.assertEquals(true,
				new RemoveDNAPCRNullValueSegments().matches(hl7string));

		// with actual value
		hl7string = "OBX|1|CWE|1030^DNA ABBORT^99DCT||1^Ack^99DCT|||||||||20080206";
		Assert.assertEquals(false,
				new RemoveDNAPCRNullValueSegments().matches(hl7string));

		// with wrong test
		hl7string = "OBX|1|CWE|1031^DNA ABBORT^99DCT||1^Ack^99DCT|||||||||20080206";
		Assert.assertEquals(false,
				new RemoveDNAPCRNullValueSegments().matches(hl7string));
	}

	
	/**
	 * @see {@link RemoveDNAPCRNullValueSegments#transform(String)}
	 */
	@Test
	@Verifies(value = "should return a null string if matched", method = "transform(String)")
	public void transform_shouldReturnANullStringIfMatched()
			throws Exception {		
		
		String hl7string = "OBX|1|CWE|1030^DNA ABBORT^99DCT||^^99DCT|||||||||20080206";
		String expected = null;

		Assert.assertEquals(expected,
				new RemoveDNAPCRNullValueSegments().transform(hl7string));
	}
	
}