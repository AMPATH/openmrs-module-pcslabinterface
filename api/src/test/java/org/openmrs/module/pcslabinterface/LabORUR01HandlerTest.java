package org.openmrs.module.pcslabinterface;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
		assertTrue("There should not be any encounters created for a lab report", encForPatient3.isEmpty());
		
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
}