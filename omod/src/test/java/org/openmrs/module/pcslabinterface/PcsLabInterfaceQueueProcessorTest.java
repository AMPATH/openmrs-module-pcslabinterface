package org.openmrs.module.pcslabinterface;

import ca.uhn.hl7v2.model.Message;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PcsLabInterfaceQueueProcessorTest extends BaseModuleContextSensitiveTest {

	protected static final String ORU_INITIAL_DATA_XML = "org/openmrs/hl7/include/ORUTest-initialData.xml";
	protected static final String SAMPLE_HL7_PATH = "org/openmrs/module/pcslabinterface/test/include/sample.hl7";
	
	/**
	 * @see {@link PcsLabInterfaceQueueProcessor#preProcessMessage(String)}
	 */
	@Test
	@Verifies(value = "should remove commas from HIV Viral Loads", method = "preProcessMessage(String)")
	public void preProcessMessage_shouldRemoveCommasFromHIVViralLoads()
			throws Exception {
		String hl7string = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||123,456|||||||||20080206",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");

		String expected = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||123456|||||||||20080206",
			"NTE|||" + PcsLabInterfaceConstants.LAB_VALUE_MODIFIED + "123,456",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");

		String actual = new PcsLabInterfaceQueueProcessor().preProcessMessage(hl7string);
		
		Assert.assertEquals(expected, actual);
	}

	/**
	 * @see {@link PcsLabInterfaceQueueProcessor#preProcessMessage(String)}
	 */
	@Test
	@Verifies(value = "should correct values with modifiers from HIV Viral Loads", method = "preProcessMessage(String)")
	public void preProcessMessage_shouldCorrectValuesWithModifiersFromHIVViralLoads()
			throws Exception {
		String hl7string = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||>500|||||||||20080206",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");

		String expected = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||501|||||||||20080206",
			"NTE|||" + PcsLabInterfaceConstants.LAB_VALUE_MODIFIED + ">500",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");

		Assert.assertEquals(expected, new PcsLabInterfaceQueueProcessor()
				.preProcessMessage(hl7string));

		hl7string = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||<500|||||||||20080206",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");

		expected = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||499|||||||||20080206",
			"NTE|||" + PcsLabInterfaceConstants.LAB_VALUE_MODIFIED + "<500",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");

		Assert.assertEquals(expected, new PcsLabInterfaceQueueProcessor()
				.preProcessMessage(hl7string));
	}

	/**
	 * @see {@link PcsLabInterfaceQueueProcessor#preProcessMessage(String)}
	 */
	@Test
	@Verifies(value = "should correct HIV Viral Load values with modifiers and spaces", method = "preProcessMessage(String)")
	public void preProcessMessage_shouldCorrectHIVViralLoadValuesWithModifiersAndSpaces()
			throws Exception {
		String hl7string = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||< 40|||||||||20080206",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");

		String expected = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||39|||||||||20080206",
			"NTE|||" + PcsLabInterfaceConstants.LAB_VALUE_MODIFIED + "< 40",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");

		String actual = new PcsLabInterfaceQueueProcessor().preProcessMessage(hl7string);
		
		Assert.assertEquals(expected, actual);
	}

	/**
	 * @see {@link PcsLabInterfaceQueueProcessor#preProcessMessage(String)}
	 */
	@Test
	@Verifies(value = "should process values with both commas and modifiers from HIV Viral Loads", method = "preProcessMessage(String)")
	public void preProcessMessage_shouldProcessValuesWithBothCommasAndModifiersFromHIVViralLoads()
			throws Exception {
		String hl7string = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||>750,000|||||||||20080206",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");
			
		String expected = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|856^HIV Viral Load^99DCT||750001|||||||||20080206",
			"NTE|||" + PcsLabInterfaceConstants.LAB_VALUE_MODIFIED + ">750,000",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");
		
		String actual = new PcsLabInterfaceQueueProcessor().preProcessMessage(hl7string);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * @see {@link PcsLabInterfaceQueueProcessor#preProcessMessage(String)}
	 */
	@Test
	@Verifies(value = "should adjust negative concept for urine protein", method = "preProcessMessage(String)")
	public void preProcessMessage_shouldAdjustNegativeConceptForUrineProtein()
			throws Exception {
		String hl7string = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|CWE|2339^URINE Protein^99DCT||^Negative^99DCT|||||||||20080206",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");

		String expected = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|CWE|2339^URINE Protein^99DCT||664^NEGATIVE^99DCT|||||||||20080206",
			"NTE|||" + PcsLabInterfaceConstants.LAB_VALUE_MODIFIED + "^Negative^99DCT",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");
		
		Assert.assertEquals(expected, new PcsLabInterfaceQueueProcessor().preProcessMessage(hl7string));
	}

	/**
	 * @see {@link PcsLabInterfaceQueueProcessor#preProcessMessage(String)}
	 */
	@Test
	@Verifies(value = "should change ST to NM for numeric concepts", method = "preProcessMessage(String)")
	public void preProcessMessage_shouldChangeSTToNMForNumericConcepts()
			throws Exception {
		String hl7string = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABCJqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|ST|5497^CD4 COUNT^99DCT||100.0|||||||||20080206",
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");
		
		String expected = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABCJqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|NM|5497^CD4 COUNT^99DCT||100.0|||||||||20080206",
			"NTE|||" + PcsLabInterfaceConstants.LAB_VALUE_MODIFIED + PcsLabInterfaceConstants.ST_TO_NM,
			"OBX|2|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");
		
		String actual = new PcsLabInterfaceQueueProcessor().preProcessMessage(hl7string);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * @see {@link PcsLabInterfaceQueueProcessor#preProcessMessage(String)}
	 */
	@Test
	@Verifies(value = "should remove DNA PCR test with null value", method = "preProcessMessage(String)")
	public void preProcessMessage_shouldRemoveDNAPCRTestWithNullValue()
			throws Exception {
		String hl7string = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABCJqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|CWE|1030^DNA CAPCTM^99DCT||1^Foo^99DCT|||||||||20080206",
			"OBX|2|CWE|1030^DNA ABBORT^99DCT||^^99DCT|||||||||20080206",
			"OBX|3|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");
		
		String expected = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABCJqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||3^^^^||John3^Doe^||",
			"ORC|RE||||||||20080226102537|1^Super User",
			"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT",
			"OBX|1|CWE|1030^DNA CAPCTM^99DCT||1^Foo^99DCT|||||||||20080206",
			"OBX|3|NM|856^HIV Viral Load^99DCT||54321|||||||||20080206");
		
		String actual = new PcsLabInterfaceQueueProcessor().preProcessMessage(hl7string);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * @see {@link LabORUR01Handler#processMessage(Message)}
	 */
	@Test
	public void problemMessageTest()
			throws Exception {
		
		// set up the database
		executeDataSet(ORU_INITIAL_DATA_XML);

		URL url = this.getClass().getClassLoader().getResource(SAMPLE_HL7_PATH);
		File file = new File(url.toURI());

		String hl7string = OpenmrsUtil.getFileAsString(file);
		
		String expectedFromFile = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|PCS|HL7LISTENER|AMRS.ELD|20120102010203||ORU^R01|ABC1234567890|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||12345^^M10^AMRS^MR||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20120102|||||||V", 
			"ORC|RE||||||||20120102010203|1^Super User", 
			"OBR|1|||90^CD3/CD4^99PCS", 
			"OBX|1|NM|5497^CD3 %^99DCT||55.5|^%|55 - 85||||F|20120101||201201020102|||||201202010201", 
			"OBX|2|NM|5497^CD4 %^99DCT||50|^%|31 - 60||||F|20120101||201201020102|||||201202010201", 
			"OBX|3|NM|5497^CD3 abs^99DCT||700|^cells/uL|690 - 2540||||F|20120101||201201020102|||||201202010201", 
			"OBX|4|NM|5497^CD4 abs^99DCT||500|^cells/uL|445 - 1497||||F|20120101||201201020102|||||201202010201", 
			"OBX|5|NM|5497^Total Lymphocytes^99DCT||1050.05|^Cell/uL|||||F|||201201020102|||||201202010201", 
			"OBX|6|NM|5497^T H/S RATIO^99DCT||0.5||||||F|||201201020102|||||201202010201\n");

		// check to be sure the expected file format is replicable
		Assert.assertEquals(expectedFromFile, hl7string);
		
//		// create a lab message ...
//		LabMessage lm = new LabMessage();
//		lm.setFileSystemUrl(SAMPLE_HL7_PATH);
//		String actual = lm.getData();

		String expectedFromLabMessage = PcsLabInterfaceTestUtil.makeMessage(
			"MSH|^~\\&|PCSLABPLUS|PCS|HL7LISTENER|AMRS.ELD|20120102010203||ORU^R01|ABC1234567890|P|2.5|1||||||||16^AMRS.ELD.FORMID",
			"PID|||12345^^M10^AMRS^MR||John3^Doe^||",
			"PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20120102|||||||V", 
			"ORC|RE||||||||20120102010203|1^Super User", 
			"OBR|1|||90^CD3/CD4^99PCS", 
			"OBX|1|NM|5497^CD3 %^99DCT||55.5|^%|55 - 85||||F|20120101||201201020102|||||201202010201", 
			"OBX|2|NM|5497^CD4 %^99DCT||50|^%|31 - 60||||F|20120101||201201020102|||||201202010201", 
			"OBX|3|NM|5497^CD3 abs^99DCT||700|^cells/uL|690 - 2540||||F|20120101||201201020102|||||201202010201", 
			"OBX|4|NM|5497^CD4 abs^99DCT||500|^cells/uL|445 - 1497||||F|20120101||201201020102|||||201202010201", 
			"OBX|5|NM|5497^Total Lymphocytes^99DCT||1050.05|^Cell/uL|||||F|||201201020102|||||201202010201", 
			"OBX|6|NM|5497^T H/S RATIO^99DCT||0.5||||||F|||201201020102|||||201202010201",
			"",
			"",
			"");
		
		// check to be sure the trailing whitespace is removed from the file
//		Assert.assertEquals(expectedFromLabMessage, actual);
		
		Message message = null;
		
		try {
			message = Context.getHL7Service().parseHL7String(expectedFromLabMessage);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			System.out.println(OpenmrsUtil.shortenedStackTrace(sw.toString()));
			throw new Exception(e);
		}
		
		Context.getHL7Service().processHL7Message(message);
	}

	/**
	 * @verifies not process EID messages
	 * @see PcsLabInterfaceQueueProcessor#preProcessMessage(String)
	 */
	@Test
	public void preProcessMessage_shouldNotProcessEIDMessages() throws Exception {
		String hl7string = "MSH|^~\\&|EID|PCS|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABC101083591|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
				+ "PID|||12345^^M10^AMRS^MR||John3^Doe^\r"
				+ "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
				+ "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";

		String actual = new PcsLabInterfaceQueueProcessor().preProcessMessage(hl7string);

		assertEquals(hl7string, actual);
	}
}