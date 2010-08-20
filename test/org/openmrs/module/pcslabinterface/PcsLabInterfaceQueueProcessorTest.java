package org.openmrs.module.pcslabinterface;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

public class PcsLabInterfaceQueueProcessorTest {
	/**
	 * @see {@link PcsLabInterfaceQueueProcessor#preProcessMessage(String)}
	 */
	@Test
	@Verifies(value = "should remove commas from HIV Viral Loads", method = "preProcessMessage(String)")
	public void preProcessMessage_shouldRemoveCommasFromHIVViralLoads()
			throws Exception {
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
				+ "PID|||3^^^^||John3^Doe^||\r"
				+ "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|NM|856^HIV Viral Load^99DCT||123,456|||||||||20080206\r"
				+ "OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206";

		String expected = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
				+ "PID|||3^^^^||John3^Doe^||\r"
				+ "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|NM|856^HIV Viral Load^99DCT||123456|||||||||20080206\r"
				+ "NTE|||PCSLabInterface modified value; original was: 123,456\r"
				+ "OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206";

		Assert.assertEquals(expected, new PcsLabInterfaceQueueProcessor()
				.preProcessMessage(hl7string));
	}
}