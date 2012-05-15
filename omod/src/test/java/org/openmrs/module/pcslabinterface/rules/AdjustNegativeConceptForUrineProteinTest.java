package org.openmrs.module.pcslabinterface.rules;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;
import org.openmrs.test.Verifies;

public class AdjustNegativeConceptForUrineProteinTest {

    /**
	 * @see {@link AdjustNegativeConceptForUrineProtein#transform(java.lang.String)
	 */
	@Test
	@Verifies(value = "should replace improper concept reference", method = "transform(String)")
	public void transform_shouldReplaceImproperConceptReference()
			throws Exception {
		String hl7string = "OBX|1|CWE|2339^URINE Protein^99DCT||^Negative^99DCT|||||||||20080206";

		String expected = "OBX|1|CWE|2339^URINE Protein^99DCT||664^NEGATIVE^99DCT|||||||||20080206"
				+ PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE
				+ "NTE|||"
				+ PcsLabInterfaceConstants.LAB_VALUE_MODIFIED
				+ "^Negative^99DCT";

        String actual = new AdjustNegativeConceptForUrineProtein().transform(hl7string);

		Assert.assertEquals(expected, actual);
	}

    /**
	 * @see {@link AdjustNegativeConceptForUrineProtein#transform(java.lang.String)
	 */
	@Test
	@Verifies(value = "should not replace proper concept reference", method = "transform(String)")
	public void transform_shouldNotReplaceProperConceptReference()
			throws Exception {
		String hl7string = "OBX|1|CWE|2339^URINE Protein^99DCT||664^NEGATIVE^99DCT|||||||||20080206";
        String actual = new AdjustNegativeConceptForUrineProtein().transform(hl7string);
		Assert.assertEquals(hl7string, actual);
	}
}