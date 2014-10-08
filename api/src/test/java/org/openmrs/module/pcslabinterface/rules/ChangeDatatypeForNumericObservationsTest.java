package org.openmrs.module.pcslabinterface.rules;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * @should leave segment alone if concept is not numeric
 **/
public class ChangeDatatypeForNumericObservationsTest extends BaseModuleContextSensitiveTest {

    /**
	 * @see {@link ChangeDatatypeForNumericObservations#transform(java.lang.String)
	 */
	@Test
	@Verifies(value = "should replace ST with NM for numeric concepts", method = "transform(String)")
	public void transform_shouldReplaceSTWithNMForNumericConcepts() throws Exception {
		String hl7string = "OBX|1|ST|5497^CD4 COUNT^99DCT||100.0|||||||||20080206";

		String expected = "OBX|1|NM|5497^CD4 COUNT^99DCT||100.0|||||||||20080206"
				+ PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE
				+ "NTE|||"
				+ PcsLabInterfaceConstants.LAB_VALUE_MODIFIED
				+ PcsLabInterfaceConstants.ST_TO_NM;

        String actual = new ChangeDatatypeForNumericObservations().transform(hl7string);

		Assert.assertEquals(expected, actual);
	}

    /**
	 * @see {@link ChangeDatatypeForNumericObservations#transform(java.lang.String)
	 */
	@Test
	@Verifies(value = "should leave segment alone if concept is not numeric", method = "transform(String)")
	public void transform_shouldLeaveSegmentAloneIfConceptIsNotNumeric() throws Exception {
		String hl7string = "OBX|1|ST|19^FAVORITE FOOD, NON-CODED^99DCT||Spam|||||||||20080206";

        String actual = new ChangeDatatypeForNumericObservations().transform(hl7string);

		Assert.assertEquals(hl7string, actual);
	}


}