package org.openmrs.module.pcslabinterface.rules;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;
import org.openmrs.test.Verifies;

public class RemoveCommasFromHIVViralLoadsTest {
	/**
	 * @see {@link RemoveCommasFromHIVViralLoads#transform(String)}
	 */
	@Test
	@Verifies(value = "should add a comment containing the original value", method = "transform(String)")
	public void transform_shouldAddACommentContainingTheOriginalValue()
			throws Exception {
		String hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||123,456|||||||||20080206";

		String expected = "OBX|1|NM|856^HIV Viral Load^99DCT||123456|||||||||20080206"
				+ PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE
				+ "NTE|||"
				+ PcsLabInterfaceConstants.LAB_VALUE_MODIFIED
				+ "123,456";

		Assert.assertEquals(expected,
				new RemoveCommasFromHIVViralLoads().transform(hl7string));
	}

	/**
	 * @see {@link RemoveCommasFromHIVViralLoads#transform(String)}
	 */
	@Test
	@Verifies(value = "should remove commas from the original value", method = "transform(String)")
	public void transform_shouldRemoveCommasFromTheOriginalValue()
			throws Exception {
		String hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||>123,456,789|||||||||20080206";

		String expected = "OBX|1|NM|856^HIV Viral Load^99DCT||>123456789|||||||||20080206"
				+ PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE
				+ "NTE|||"
				+ PcsLabInterfaceConstants.LAB_VALUE_MODIFIED
				+ ">123,456,789";

		Assert.assertEquals(expected,
				new RemoveCommasFromHIVViralLoads().transform(hl7string));
	}

	/**
	 * @see {@link RemoveCommasFromHIVViralLoads#RemoveCommasFromHIVViralLoads()}
	 */
	@Test
	@Verifies(value = "should match numeric and structured text OBX segments for HIV Viral Load with commas in the value", method = "RemoveCommasFromHIVViralLoads()")
	public void RemoveCommasFromHIVViralLoads_shouldMatchNumericAndStructuredTextOBXSegmentsForHIVViralLoadWithCommasInTheValue()
			throws Exception {
		// with commas
		String hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||123,456,789|||||||||20080206";
		Assert.assertEquals(true,
				new RemoveCommasFromHIVViralLoads().matches(hl7string));

        // with ST instead of NM
		hl7string = "OBX|1|ST|856^HIV Viral Load^99DCT||123,456,789|||||||||20080206";
		Assert.assertEquals(true,
				new RemoveCommasFromHIVViralLoads().matches(hl7string));

		// without commas
		hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||123456789|||||||||20080206";
		Assert.assertEquals(false,
				new RemoveCommasFromHIVViralLoads().matches(hl7string));
	}

	/**
	 * @see {@link RemoveCommasFromHIVViralLoads#RemoveCommasFromHIVViralLoads()}
	 */
	@Test
	@Verifies(value = "should match values with other characters as long as there is at least one comma", method = "RemoveCommasFromHIVViralLoads()")
	public void RemoveCommasFromHIVViralLoads_shouldMatchValuesWithOtherCharactersAsLongAsThereIsAtLeastOneComma()
			throws Exception {
		// with commas
		String hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||>123,456,789|||||||||20080206";
		Assert.assertEquals(true,
				new RemoveCommasFromHIVViralLoads().matches(hl7string));

		// without commas
		hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||p123456789|||||||||20080206";
		Assert.assertEquals(false,
				new RemoveCommasFromHIVViralLoads().matches(hl7string));
	}

	/**
	 * @see {@link RemoveCommasFromHIVViralLoads#RemoveCommasFromHIVViralLoads()}
	 */
	@Test
	@Verifies(value = "work for any OBX referencing 856 regardless of name", method = "RemoveCommasFromHIVViralLoads()")
	public void RemoveCommasFromHIVViralLoads_shouldWorkForAnyOBXReferencing856RegardlessOfName()
			throws Exception {
		String hl7string = "OBX|1|NM|856^Ack - foo Bar^99DCT||>123,456,789|||||||||20080206";

		String expected = "OBX|1|NM|856^Ack - foo Bar^99DCT||>123456789|||||||||20080206"
				+ PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE
				+ "NTE|||"
				+ PcsLabInterfaceConstants.LAB_VALUE_MODIFIED
				+ ">123,456,789";

		Assert.assertEquals(expected,
				new RemoveCommasFromHIVViralLoads().transform(hl7string));
	}

}