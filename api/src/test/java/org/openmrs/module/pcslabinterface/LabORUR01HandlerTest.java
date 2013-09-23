package org.openmrs.module.pcslabinterface;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

import ca.uhn.hl7v2.app.MessageTypeRouter;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.GenericParser;

/**
 * Test class for LabORUR01Handler
 */
public class LabORUR01HandlerTest extends BaseModuleContextSensitiveTest {

	// test data to load before all tests (from OpenMRS core)
	protected static final String ORU_INITIAL_DATA_XML = "org/openmrs/hl7/include/ORUTest-initialData.xml";

	// hl7 parser for all tests
	protected static GenericParser parser = new GenericParser();
	
	// hl7 router for all tests
	private static MessageTypeRouter router = new MessageTypeRouter();

	// registering the lab ORU^R01 handler
	static {
		router.registerApplication("ORU", "R01", new LabORUR01Handler());
	}
	
	/**
	 * Run this before each unit test in this class. This adds the hl7 specific data to the initial
	 * and demo data done in the "@Before" method in {@link BaseContextSensitiveTest}.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(ORU_INITIAL_DATA_XML);
	}
	
	/**
	 * @see {@link LabORUR01Handler#processMessage(Message)}
	 */
	@Test
	@Verifies(value = "should process messages with PCSLabPlus formatted PID segments", method = "processMessage(Message)")
	public void processMessage_shouldProcessMessagesWithPCSLabPlusFormattedPIDSegments()
			throws Exception {
		ObsService obsService = Context.getObsService();
		
		// largely borrowed from another test; simply changed the PID format
		String hl7string = "MSH|^~\\&|PCSLABPLUS|PCS|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABC101083591|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||12345^^M10^AMRS^MR||John3^Doe^\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		        + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";
		
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		Patient patient = new Patient(3);
		
		// check for an encounter
		List<Encounter> encForPatient3 = Context.getEncounterService().getEncountersByPatient(patient);
		assertNotNull(encForPatient3);
//		assertTrue("There should not be any encounters created for a lab report", encForPatient3.isEmpty());
		
		// check for any obs
		List<Obs> obsForPatient3 = obsService.getObservationsByPerson(patient);
		assertNotNull(obsForPatient3);
		assertTrue("There should be some obs created for #3", obsForPatient3.size() > 0);
		
		// check for the return visit date obs 
		Concept returnVisitDateConcept = new Concept(5096);
		Calendar cal = Calendar.getInstance();
		cal.set(2008, Calendar.FEBRUARY, 29, 0, 0, 0);
		Date returnVisitDate = cal.getTime();
		List<Obs> returnVisitDateObsForPatient3 = obsService.getObservationsByPersonAndConcept(patient,
		    returnVisitDateConcept);
		assertEquals("There should be a return visit date", 1, returnVisitDateObsForPatient3.size());
		
		Obs firstObs = (Obs) returnVisitDateObsForPatient3.toArray()[0];
		cal.setTime(firstObs.getValueDatetime());
		Date firstObsValueDatetime = cal.getTime();
		assertEquals("The date should be the 29th", returnVisitDate.toString(), firstObsValueDatetime.toString());
	}

	/**
	 * @verifies ignore messages originating from anywhere but REFPACS or PCS
	 * @see LabORUR01Handler#canProcess(ca.uhn.hl7v2.model.Message)
	 */
	@Test
	public void canProcess_shouldIgnoreMessagesOriginatingFromAnywhereButREFPACSOrPCS() throws Exception {
		String hl7string = "MSH|^~\\&|PCSLABPLUS|PCS|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABC101083591|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
				+ "PID|||12345^^M10^AMRS^MR||John3^Doe^\r"
				+ "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
				+ "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";

		Message hl7message = parser.parse(hl7string);

		assertTrue(new LabORUR01Handler().canProcess(hl7message));

		hl7string = "MSH|^~\\&|REFPACS|IU|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABC101083591|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
				+ "PID|||12345^^M10^AMRS^MR||John3^Doe^\r"
				+ "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
				+ "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";

		hl7message = parser.parse(hl7string);

		assertTrue(new LabORUR01Handler().canProcess(hl7message));

		hl7string = "MSH|^~\\&|FORMENTRY|AMPATH|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABC101083591|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
				+ "PID|||12345^^M10^AMRS^MR||John3^Doe^\r"
				+ "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
				+ "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";

		hl7message = parser.parse(hl7string);

		assertFalse(new LabORUR01Handler().canProcess(hl7message));
	}

	/**
	 * @verifies process value grouped obs
	 * @see LabORUR01Handler#processMessage(ca.uhn.hl7v2.model.Message)
	 */
	@Test
	public void processMessage_shouldProcessValueGroupedObs() throws Exception {
		ObsService obsService = Context.getObsService();

		// largely borrowed from another test
		String hl7string = "MSH|^~\\&|REFPACS|IU|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABC101083591|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
				+ "PID|||3^^^^||John3^Doe^||\r"
				+ "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|CWE|1558^PATIENT CONTACT METHOD^99DCT||1555^PHONE^99DCT~1726^FOLLOW-UP ACTION^99DCT|||||||||20080206\r"
				+ "OBX|5|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";

		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);

		Patient patient = new Patient(3);

		// check for an encounter
		List<Encounter> encForPatient3 = Context.getEncounterService().getEncountersByPatient(patient);
		Assert.assertNotNull(encForPatient3);
		Assert.assertEquals("There should be an encounter created", 1, encForPatient3.size());

		// check for any obs
		List<Obs> obsForPatient3 = obsService.getObservationsByPerson(patient);
		Assert.assertNotNull(obsForPatient3);
		Assert.assertTrue("There should be some obs created for #3", obsForPatient3.size() > 0);

		// check for civil status observation(s)
		Concept concept = Context.getConceptService().getConcept(1558);
		List<Obs> actual = obsService.getObservationsByPersonAndConcept(patient, concept);

		Assert.assertEquals("There should be two observations of contact method", 2, actual.size());

		Integer valueGroupId = actual.get(0).getValueGroupId();
		Assert.assertEquals("The value group id should be the same for both observations", valueGroupId,
				actual.get(1).getValueGroupId());

		Obs valueGroupIndex = Context.getObsService().getObs(valueGroupId);
		Assert.assertNotNull("The index observation does not really exist", valueGroupIndex);
	}

	/**
	 * @verifies create an encounter with no form if no form id is provided
	 * @see LabORUR01Handler#processMessage(ca.uhn.hl7v2.model.Message)
	 */
	@Test
	public void processMessage_shouldCreateAnEncounterWithNoFormIfNoFormIdIsProvided() throws Exception {
		String hl7string = "MSH|^~\\&|PCSLABPLUS|PCS|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABC101083591|P|2.5|1||||||||\r"
				+ "PID|||3^^^^L||John3^Doe^\r"
				+ "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
				+ "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";

		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);

		Patient patient = new Patient(3);

		// check for an encounter
		List<Encounter> encForPatient3 = Context.getEncounterService().getEncountersByPatient(patient);
		assertNotNull(encForPatient3);
		assertFalse(encForPatient3.isEmpty());
		assertTrue(encForPatient3.size() == 1);

		Encounter e = encForPatient3.get(0);
		assertNull(e.getForm());
	}

	/**
	 * @verifies create an encounter with no encounter type if none is provided
	 * @see LabORUR01Handler#processMessage(ca.uhn.hl7v2.model.Message)
	 */
	@Test
	public void processMessage_shouldCreateAnEncounterWithNoEncounterTypeIfNoneIsProvided() throws Exception {
		String hl7string = "MSH|^~\\&|PCSLABPLUS|PCS|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABC101083591|P|2.5|1||||||||\r"
				+ "PID|||3^^^^L||John3^Doe^\r"
				+ "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
				+ "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";

		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);

		Patient patient = new Patient(3);

		// check for an encounter
		List<Encounter> encForPatient3 = Context.getEncounterService().getEncountersByPatient(patient);
		assertNotNull(encForPatient3);
		assertFalse(encForPatient3.isEmpty());
		assertTrue(encForPatient3.size() == 1);

		Encounter e = encForPatient3.get(0);
		assertNull(e.getEncounterType());
	}

	/**
	 * @verifies not create an encounter if no PV1 segment is in the message
	 * @see LabORUR01Handler#processMessage(ca.uhn.hl7v2.model.Message)
	 */
	@Test
	public void processMessage_shouldNotCreateAnEncounterIfNoPV1SegmentIsInTheMessage() throws Exception {
		String hl7string = "MSH|^~\\&|PCSLABPLUS|PCS|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|ABC101083591|P|2.5|1||||||||\r"
				+ "PID|||3^^^^L||John3^Doe^\r"
				+ "ORC|RE||||||||20080226102537|1^Super User\r"
				+ "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
				+ "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
				+ "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";

		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);

		Patient patient = new Patient(3);

		// check for an encounter
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
		assertNotNull(encounters);
		assertTrue(encounters.isEmpty());

		// check for obses
		List<Obs> obses = Context.getObsService().getObservationsByPerson(patient);
		assertFalse(obses.isEmpty());
		assertEquals(obses.size(), 2);
	}
}