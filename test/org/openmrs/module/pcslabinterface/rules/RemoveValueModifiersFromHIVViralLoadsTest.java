package org.openmrs.module.pcslabinterface.rules;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;
import org.openmrs.test.Verifies;

public class RemoveValueModifiersFromHIVViralLoadsTest {
	/**
	 * @see {@link RemoveValueModifiersFromHIVViralLoads#RemoveValueModifiersFromHIVViralLoads()}
	 */
	@Test
	@Verifies(value = "should match only numeric OBX segments for HIV Viral Load with a modifier before the value", method = "RemoveValueModifiersFromHIVViralLoads()")
	public void RemoveValueModifiersFromHIVViralLoads_shouldMatchOnlyNumericOBXSegmentsForHIVViralLoadWithAModifierBeforeTheValue()
			throws Exception {
		// with modifier
		String hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||>123456789|||||||||20080206";
		Assert.assertEquals(true,
				new RemoveValueModifiersFromHIVViralLoads().matches(hl7string));

		// without modifier
		hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||123456789|||||||||20080206";
		Assert.assertEquals(false,
				new RemoveValueModifiersFromHIVViralLoads().matches(hl7string));
	}

	/**
	 * @see {@link RemoveValueModifiersFromHIVViralLoads#transform(String)}
	 */
	@Test
	@Verifies(value = "should decrease value by one if using less than modifier", method = "transform(String)")
	public void transform_shouldDecreaseValueByOneIfUsingLessThanModifier()
			throws Exception {
		String hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||<400|||||||||20080206";

		String expected = "OBX|1|NM|856^HIV Viral Load^99DCT||399|||||||||20080206\r"
				+ "NTE|||"
				+ PcsLabInterfaceConstants.LAB_VALUE_MODIFIED
				+ "<400";

		Assert.assertEquals(expected,
				new RemoveValueModifiersFromHIVViralLoads()
						.transform(hl7string));
	}

	/**
	 * @see {@link RemoveValueModifiersFromHIVViralLoads#transform(String)}
	 */
	@Test
	@Verifies(value = "should increase value by one if using greater than modifier", method = "transform(String)")
	public void transform_shouldIncreaseValueByOneIfUsingGreaterThanModifier()
			throws Exception {
		String hl7string = "OBX|1|NM|856^HIV Viral Load^99DCT||>750000|||||||||20080206";

		String expected = "OBX|1|NM|856^HIV Viral Load^99DCT||750001|||||||||20080206\r"
				+ "NTE|||"
				+ PcsLabInterfaceConstants.LAB_VALUE_MODIFIED
				+ ">750000";

		Assert.assertEquals(expected,
				new RemoveValueModifiersFromHIVViralLoads()
						.transform(hl7string));
	}
}