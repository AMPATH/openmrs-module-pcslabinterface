/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.pcslabinterface;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.CWE;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.DLD;
import ca.uhn.hl7v2.model.v25.datatype.DT;
import ca.uhn.hl7v2.model.v25.datatype.DTM;
import ca.uhn.hl7v2.model.v25.datatype.FT;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.datatype.IS;
import ca.uhn.hl7v2.model.v25.datatype.NM;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.datatype.TM;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.NK1;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PD1;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7Service;
import org.openmrs.hl7.handler.ORUR01Handler;
import org.openmrs.hl7.handler.ProposingConceptException;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.validator.ObsValidator;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Custom Lab ORU_R01 handler; removes encounters from the obs for lab results
 * and attempts to find patients by identifier.  This class closely mimics the
 * code in OpenMRS 1.8's ORUR01Handler class.
 */
public class LabORUR01Handler extends ORUR01Handler {

	private Log log = LogFactory.getLog(LabORUR01Handler.class);

	private List<String> allowedSendingApps = Arrays.asList(
			"refpacs",
			"pcslabplus",
			"eid"
	);

	/**
	 * Processes an ORU R01 event message
	 *
	 * @should process messages with PCSLabPlus formatted PID segments
	 * @should process value grouped obs
	 * @should allow default ORUR01Handler to handle messages that are not from REFPACS or PCS
	 * @should create an encounter with no form if no form id is provided
	 * @should create an encounter with no encounter type if none is provided
	 * @should not create an encounter if no PV1 segment is in the message
	 * @should throw an HL7Exception if a null value is in OBX5
	 * @should look for location in PD1 segment
	 */
	public Message processMessage(Message message) throws ApplicationException {

		if (!(message instanceof ORU_R01))
			throw new ApplicationException("Invalid message sent to ORU_R01 handler");

		log.debug("Processing ORU_R01 message");

		Message response;
		try {
			ORU_R01 oru = (ORU_R01) message;

			// if this is consumable, consume it
			if (isLabMessage(oru))
				response = processLabORU_R01(oru);

				// otherwise, let the ORUR01Handler class do the work
			else
				response = super.processMessage(message);
		} catch (ClassCastException e) {
			log.error("Error casting " + message.getClass().getName() + " to ORU_R01", e);
			throw new ApplicationException("Invalid message type for handler");
		} catch (HL7Exception e) {
			log.error("Error while processing ORU_R01 message", e);
			throw new ApplicationException(e);
		}

		log.debug("Finished processing ORU_R01 message");

		return response;
	}

	/**
	 * Custom ORU_R01 processor for labs
	 */
	private Message processLabORU_R01(ORU_R01 oru) throws HL7Exception {

		// extract segments for convenient use below
		MSH msh = getMSH(oru);
		PID pid = getPID(oru);
		List<NK1> nk1List = getNK1List(oru);
		ORC orc = getORC(oru);
		// we're using the ORC assoc with first OBR to hold data enterer and
		// date entered for now

		// Obtain message control id (unique ID for message from sending
		// application)
		String messageControlId = msh.getMessageControlID().getValue();

		if (log.isDebugEnabled())
			log.debug("Found HL7 message in inbound queue with control id = " + messageControlId);

		HL7Service hl7Service = Context.getHL7Service();

		// create the encounter
		Patient patient = getPatientByIdentifier(pid);
		if (log.isDebugEnabled())
			log.debug("Processing HL7 message for patient " + patient.getPatientId());

		// create an encounter
		Encounter encounter = createEncounter(msh, patient, oru, orc);

		// do the discharge to location logic
		try {
			updateHealthCenter(patient, oru);
		} catch (Exception e) {
			log.error("Error while processing Discharge To Location (" + messageControlId + ")", e);
		}

		// process NK1 (relationship) segments
		for (NK1 nk1 : nk1List)
			processNK1(patient, nk1);

		// list of concepts proposed in the obs of this encounter.
		// these proposals need to be created after the encounter
		// has been created
		List<ConceptProposal> conceptProposals = new ArrayList<ConceptProposal>();

		// create observations
		if (log.isDebugEnabled())
			log.debug("Creating observations for message " + messageControlId + "...");

		// keep a list of obses that need value reference ids
		List<List<Obs>> valueGroups = new ArrayList<List<Obs>>();

		ORU_R01_PATIENT_RESULT patientResult = oru.getPATIENT_RESULT();

		int numObr = patientResult.getORDER_OBSERVATIONReps();
		for (int i = 0; i < numObr; i++) {
			if (log.isDebugEnabled())
				log.debug("Processing OBR (" + i + " of " + numObr + ")");
			ORU_R01_ORDER_OBSERVATION orderObs = patientResult.getORDER_OBSERVATION(i);

			// the parent obr
			OBR obr = orderObs.getOBR();

			// if we're not ignoring this obs group, create an
			// Obs grouper object that the underlying obs objects will use
			Obs obsGrouper = null;
			Concept obrConcept = getConcept(obr.getUniversalServiceIdentifier(), messageControlId);
            if (obrConcept != null && !PcsLabInterfaceUtil.isObrConceptIgnored(obrConcept)) {
				// maybe check for a parent obs group from OBR-29 Parent ?

				// create an obs for this obs group too
				obsGrouper = new Obs();
				obsGrouper.setConcept(obrConcept);
				obsGrouper.setPerson(encounter.getPatient());
				obsGrouper.setEncounter(encounter);
				Date datetime = getDatetime(obr);
				if (datetime == null)
                    datetime = getDateTime(getORC(oru));
                if(datetime == null) datetime = getDatetime(msh.getDateTimeOfMessage());
                if(datetime == null) datetime = new Date();
				obsGrouper.setObsDatetime(datetime);
				obsGrouper.setLocation(encounter.getLocation());
				obsGrouper.setCreator(encounter.getCreator());

				// set comments if there are any
				StringBuilder comments = new StringBuilder();
				ORU_R01_ORDER_OBSERVATION parent = (ORU_R01_ORDER_OBSERVATION) obr.getParent();
				int totalNTEs = parent.getNTEReps();
				for (int iNTE = 0; iNTE < totalNTEs; iNTE++)
					for (FT obxComment : parent.getNTE(iNTE).getComment()) {
						if (comments.length() > 0)
							comments.append(" ");
						comments.append(obxComment.getValue());
					}
				// only set comments if there are any
				if (StringUtils.hasText(comments.toString()))
					obsGrouper.setComment(comments.toString());

				// add this obs as another row in the obs table
				encounter.addObs(obsGrouper);
			}

			// loop over the obs and create each object, adding it to the encounter
			int numObs = orderObs.getOBSERVATIONReps();
			HL7Exception errorInHL7Queue = null;
			for (int j = 0; j < numObs; j++) {
				if (log.isDebugEnabled())
					log.debug("Processing OBS (" + j + " of " + numObs + ")");

				OBX obx = orderObs.getOBSERVATION(j).getOBX();
				try {
					log.debug("Parsing observation");
					List<Obs> obses = parseObs(encounter, obx, obr, messageControlId);

					// Do not include the obs if not found
					if (obses == null) {
//						throw new HL7Exception("null value in OBX-5");
                        continue;
					}

					// initialize and increment value group id counters
					if (obses.size() > 1) {
						valueGroups.add(obses);
					}

					// process each obs
					for (Obs obs : obses) {
						if (obs != null) {
                            //Validate the obs
                            Validator validator = new ObsValidator();
                            Errors errors = new BeanPropertyBindingResult(obs,"obs");
                            validator.validate(obs,errors);
                            if(errors.hasErrors()) {
                                throw new HL7Exception("Error(s) in obs associated with OBX: "
                                        + PipeParser.encode(obx, new EncodingCharacters('|', "^~\\&"))
                                        + errors.getAllErrors().toString());
                            }
							// if we're backfilling an encounter, don't use
							// the creator/dateCreated from the encounter
							if (encounter.getEncounterId() != null) {
								obs.setCreator(getEnterer(orc));
								obs.setDateCreated(new Date());
							}

							// set the obsGroup on this obs
							if (obsGrouper != null)
								// set the obs to the group.  This assumes the group is already
								// on the encounter and that when the encounter is saved it will
								// propagate to the children obs
								obsGrouper.addGroupMember(obs);

							else {
								// set this obs on the encounter object that we
								// will be saving later
								log.debug("Obs is not null. Adding to encounter object");
								encounter.addObs(obs);
								log.debug("Done with this obs");
							}
						}
					}
				} catch (ProposingConceptException proposingException) {
					Concept questionConcept = proposingException.getConcept();
					String value = proposingException.getValueName();
					//if the sender never specified any text for the proposed concept
					if (value != null && !value.equals(""))
						conceptProposals.add(createConceptProposal(encounter, questionConcept, value));
					else {
						errorInHL7Queue = new HL7Exception(Context.getMessageSourceService().getMessage(
								"Hl7.proposed.concept.name.empty"), proposingException);
						break;//stop any further processing of current message
					}

				} catch (HL7Exception e) {
//					errorInHL7Queue = e;
                    throw e;
				} finally {
					// Handle obs-level exceptions
					if (errorInHL7Queue != null) {
						throw new HL7Exception("Improperly formatted OBX: "
								+ PipeParser.encode(obx, new EncodingCharacters('|', "^~\\&")),
								HL7Exception.DATA_TYPE_ERROR, errorInHL7Queue);
					}
				}
			}

		}

		if (log.isDebugEnabled()) {
			log.debug("Finished creating observations");
			log.debug("Current thread: " + Thread.currentThread());
			log.debug("Creating the encounter object");
		}

		if (isValidEncounter(encounter)) {
			saveEncounter(encounter);

			// Notify HL7 service that we have created a new encounter, allowing
			// features/modules to trigger on HL7-generated encounters.
			// -This can be removed once we have a obs_group table and all
			// obs can be created in memory as part of the encounter *before* we
			// call EncounterService.createEncounter().  For now, making obs groups
			// requires that one obs be created (in the database) before others can
			// be linked to it, forcing us to save the encounter prematurely."
			//
			// NOTE: The above referenced fix is now done.  This method is
			// deprecated and will be removed in the next release.  All modules
			// should modify their AOP methods to hook around
			// EncounterService.createEncounter(Encounter).
			hl7Service.encounterCreated(encounter);
		} else {
			saveObsFromEncounter(encounter);
		}

		// loop over the proposed concepts and save each to the database
		// now that the encounter is saved
		for (ConceptProposal proposal : conceptProposals) {
			Context.getConceptService().saveConceptProposal(proposal);
		}

		// flush the session to make sure the obses are saved
		Context.flushSession();

		// set value group ids for those that need it
		for (List<Obs> valueGroup : valueGroups) {
			Obs initialObs = valueGroup.get(0);
			for (Obs obs : valueGroup) {
				obs.setValueGroupId(initialObs.getObsId());

				// let the first obs be the group member, but remove the rest
				if (!obs.equals(initialObs)) {
					obs.setObsGroup(null);
				}
			}
		}

		// flush the session again make sure the obses are saved
		Context.flushSession();

		return oru;
	}

	private void saveObsFromEncounter(Encounter encounter) throws HL7Exception {
		// save each obs but not the encounter object
		// can't use getAllObs() method here because of how cascade saving is done
		for (Obs obs : encounter.getObsAtTopLevel(false)) {
			nullifyObsEncounter(obs);
			// the changeMessage here is not used
            Context.getObsService().saveObs(obs, "new stand-alone observation from orur01 handler");
		}
	}

	private void saveEncounter(Encounter encounter) throws HL7Exception {
		Context.getEncounterService().saveEncounter(encounter);
	}

	/**
	 * process an NK1 segment and add relationships if needed
	 */

	protected void processNK1(Patient patient, NK1 nk1) throws HL7Exception {
		// guarantee we are working with our custom coding system
		String relCodingSystem = nk1.getRelationship().getNameOfCodingSystem().getValue();
		if (!relCodingSystem.equals(HL7Constants.HL7_LOCAL_RELATIONSHIP))
			throw new HL7Exception("Relationship coding system '" + relCodingSystem + "' unknown in NK1 segment.");

		// get the relationship type identifier
		String relIdentifier = nk1.getRelationship().getIdentifier().getValue();

		// validate the format of the relationship identifier
		if (!Pattern.matches("[0-9]+[AB]", relIdentifier))
			throw new HL7Exception("Relationship type '" + relIdentifier + "' improperly formed in NK1 segment.");

		// get the type ID
		Integer relTypeId;
		try {
			relTypeId = Integer.parseInt(relIdentifier.substring(0, relIdentifier.length() - 1));
		} catch (NumberFormatException e) {
			throw new HL7Exception("Relationship type '" + relIdentifier + "' improperly formed in NK1 segment.");
		}

		// find the relationship type
		RelationshipType relType = Context.getPersonService().getRelationshipType(relTypeId);
		if (relType == null)
			throw new HL7Exception("Relationship type '" + relTypeId + "' in NK1 segment not found");

		// find the relative
		Person relative = getRelative(nk1);

		// determine if the patient is person A or B; the relIdentifier indicates
		// the relative's side of the relationship, so the patient is the inverse
		boolean patientIsPersonA = relIdentifier.endsWith("B");
		boolean patientCanBeEitherPerson = relType.getbIsToA().equals(relType.getaIsToB());

		// look at existing relationships to determine if a new one is needed
		Set<Relationship> rels = new HashSet<Relationship>();
		if (relative != null) {
			if (patientCanBeEitherPerson || patientIsPersonA)
				rels.addAll(Context.getPersonService().getRelationships(patient, relative, relType));
			if (patientCanBeEitherPerson || !patientIsPersonA)
				rels.addAll(Context.getPersonService().getRelationships(relative, patient, relType));
		}

		// create a relationship if none is found
		if (rels.isEmpty()) {

			// check the relative's existence
			if (relative == null) {
				// create one based on NK1 information
				relative = Context.getHL7Service().createPersonFromNK1(nk1);
				if (relative == null)
					throw new HL7Exception("could not create a new relative from NK1 segment");
			}

			// create the relationship
			Relationship relation = new Relationship();
			if (patientCanBeEitherPerson || patientIsPersonA) {
				relation.setPersonA(patient);
				relation.setPersonB(relative);
			} else {
				relation.setPersonA(relative);
				relation.setPersonB(patient);
			}
			relation.setRelationshipType(relType);
			Context.getPersonService().saveRelationship(relation);
		}
	}

	/**
	 * This is needed for hl7 messages that don't want to create an encounter. The encounter object
	 * is just used as a temporary container for obs and then is thrown away. We are deleting that
	 * here. <br/>
	 * This is needed instead of using encounter.getAllObs because of how cascading and saving works
	 * with hibernate
	 *
	 * @param obs       the obs to set the given encounter onto (and its child objects)
	 * @param encounter the encounter to set
	 */
	private void recursivelySetEncounter(Obs obs, Encounter encounter) {
		obs.setEncounter(encounter);
		if (obs.hasGroupMembers())
			for (Obs childObs : obs.getGroupMembers())
				recursivelySetEncounter(childObs, encounter);
	}

    private void nullifyObsEncounter(final Obs obs) {
        recursivelySetEncounter(obs,null);
    }
	/**
	 * Validates an encounter
	 */
	private boolean isValidEncounter(Encounter encounter) throws HL7Exception {
		if (encounter.getEncounterDatetime() == null ||
				encounter.getProvider() == null ||
				encounter.getPatient() == null)
			return false;
		return true;
	}

	private MSH getMSH(ORU_R01 oru) {
		return oru.getMSH();
	}

	private PID getPID(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getPATIENT().getPID();
	}

	/**
	 * finds NK1 segments in an ORU_R01 message. all HAPI-rendered Messages have at least one NK1
	 * segment but if the original message truly does not contain an NK1, the setID will be null on
	 * the generated NK1
	 *
	 * @param oru ORU_R01 message to be parsed for NK1 segments
	 * @return list of not-null NK1 segments
	 * @throws HL7Exception
	 */
	public List<NK1> getNK1List(ORU_R01 oru) throws HL7Exception {
		List<NK1> res = new ArrayList<NK1>();
		// there will always be at least one NK1, even if the original message does not contain one
		for (int i = 0; i < oru.getPATIENT_RESULT().getPATIENT().getNK1Reps(); i++)
			// if the setIDNK1 value is null, this NK1 is blank
			if (oru.getPATIENT_RESULT().getPATIENT().getNK1(i).getSetIDNK1().getValue() != null)
				res.add(oru.getPATIENT_RESULT().getPATIENT().getNK1(i));
		return res;
	}

	private PV1 getPV1(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
	}

	private PD1 getPD1(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getPATIENT().getPD1();
	}

	private ORC getORC(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();
	}

	/**
	 * This method does not call the database to create the encounter row. The encounter is only
	 * created after all obs have been attached to it Creates an encounter pojo to be attached
	 * later. This method does not create an encounterId
	 */
	private Encounter createEncounter(MSH msh, Patient patient, ORU_R01 oru, ORC orc) throws HL7Exception {

		PV1 pv1 = getPV1(oru);

		// get the referenced encounterId if it is in the PV1 segment
		Integer encounterId = null;

		// look for the encounter id in PV1-19
		CX visitNumber = pv1.getVisitNumber();
		try {
			encounterId = Integer.valueOf(visitNumber.getIDNumber().getValue());
		} catch (NumberFormatException e) {
			// pass
		}

		// the encounter we will return
		Encounter encounter;

		// if an encounterId was passed in, assume that these obs are
		// going to be appended to it.  Fetch the old encounter from
		// the database
		if (encounterId != null) {
			encounter = Context.getEncounterService().getEncounter(encounterId);
		} else {
			// if no encounter_id was passed in, this is a new
			// encounter, create the object
			encounter = new Encounter();

			Date encounterDate = getEncounterDate(pv1);
			Provider provider = getProvider(oru);
			Location location = getLocation(oru);
			Form form = getForm(msh);
			EncounterType encounterType = getEncounterType(msh, form);
            User creator = getEnterer(orc);
			//			Date dateEntered = getDateEntered(orc); // ignore this since we have no place in the data model to store it

			encounter.setEncounterDatetime(encounterDate);
            if(provider != null) encounter.setProvider(null,provider);
			encounter.setPatient(patient);
			encounter.setLocation(location);
			encounter.setForm(form);
			encounter.setEncounterType(encounterType);
			encounter.setCreator(creator);
			encounter.setDateCreated(new Date());
		}

		return encounter;
	}

	/**
	 * Returns a list of observations built from a single OBX, one for each value.
	 */
	private List<Obs> parseObs(Encounter encounter, OBX obx, OBR obr, String uid) throws HL7Exception, ProposingConceptException {
		Varies[] values = obx.getObservationValue();

		// bail out if no values were found
		if (values == null || values.length < 1)
			return null;

		String hl7Datatype = values[0].getName();

		List<Obs> obses = new ArrayList<Obs>();

		for (Varies obxValue : values) {
			// process each obx5 value
			Type obx5 = obxValue.getData();
			obses.add(parseObs(encounter, obx, obr, obx5, hl7Datatype, uid));
		}

		return obses;
	}

	/**
	 * Creates the Obs pojo from the OBX message
	 */
	private Obs parseObs(Encounter encounter, OBX obx, OBR obr, Type obx5, String hl7Datatype, String uid) throws HL7Exception, ProposingConceptException {
		if (log.isDebugEnabled())
			log.debug("parsing observation: " + obx);

		if (log.isDebugEnabled())
			log.debug("  datatype = " + hl7Datatype);
		Concept concept = getConcept(obx.getObservationIdentifier(), uid);
		if (log.isDebugEnabled())
			log.debug("  concept = " + concept.getConceptId());
		ConceptName conceptName = getConceptName(obx.getObservationIdentifier());
		if (log.isDebugEnabled())
			log.debug("  concept-name = " + conceptName);

		Date datetime = getDatetime(obx);
		if (log.isDebugEnabled())
			log.debug("  timestamp = " + datetime);
		if (datetime == null)
			datetime = encounter.getEncounterDatetime();

		Obs obs = new Obs();
		obs.setPerson(encounter.getPatient());
		obs.setConcept(concept);
		obs.setEncounter(encounter);
		obs.setObsDatetime(datetime);
		obs.setLocation(encounter.getLocation());
		obs.setCreator(encounter.getCreator());
		obs.setDateCreated(encounter.getDateCreated());

		// set comments if there are any
		StringBuilder comments = new StringBuilder();
		ORU_R01_OBSERVATION parent = (ORU_R01_OBSERVATION) obx.getParent();
		// iterate over all OBX NTEs
		for (int i = 0; i < parent.getNTEReps(); i++)
			for (FT obxComment : parent.getNTE(i).getComment()) {
				if (comments.length() > 0)
					comments.append(" ");
				comments = comments.append(obxComment.getValue());
			}
		// only set comments if there are any
		if (StringUtils.hasText(comments.toString()))
			obs.setComment(comments.toString());

		if ("NM".equals(hl7Datatype)) {
			String value = ((NM) obx5).getValue();
			if (value == null || value.length() == 0) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			} else if (value.equals("0") || value.equals("1")) {
				Integer conceptId = concept.getConceptId();
				concept = concept.hydrate(concept.getConceptId().toString());
				if (concept == null)
					throw new HL7Exception("Could not find concept #" + conceptId);

				obs.setConcept(concept);
				if (concept.getDatatype().isBoolean())
					obs.setValueBoolean(value.equals("1"));
				else if (concept.getDatatype().isNumeric())
					try {
						obs.setValueNumeric(Double.valueOf(value));
					} catch (NumberFormatException e) {
						throw new HL7Exception("numeric (NM) value '" + value + "' is not numeric for concept #"
								+ concept.getConceptId() + " (" + conceptName.getName() + ") in message " + uid, e);
					}
				else if (concept.getDatatype().isCoded()) {
					Concept answer = value.equals("1") ? Context.getConceptService().getTrueConcept() : Context
							.getConceptService().getFalseConcept();
					boolean isValidAnswer = false;
					Collection<ConceptAnswer> conceptAnswers = concept.getAnswers();
					if (conceptAnswers != null && conceptAnswers.size() > 0) {
						for (ConceptAnswer conceptAnswer : conceptAnswers) {
							if (conceptAnswer.getAnswerConcept().equals(answer)) {
								obs.setValueCoded(answer);
								isValidAnswer = true;
								break;
							}
						}
					}
					//answer the boolean answer concept was't found
					if (!isValidAnswer)
						throw new HL7Exception(answer.toString() + " is not a valid answer for obs with uuid " + uid);
				} else {
					//throw this exception to make sure that the handler doesn't silently ignore bad hl7 message
					throw new HL7Exception("Can't set boolean concept answer for concept with id "
							+ obs.getConcept().getConceptId());
				}
			} else {
				try {
					obs.setValueNumeric(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new HL7Exception("numeric (NM) value '" + value + "' is not numeric for concept #"
							+ concept.getConceptId() + " (" + conceptName.getName() + ") in message " + uid, e);
				}
			}
		} else if ("CWE".equals(hl7Datatype)) {
			log.debug("  CWE observation");
			CWE value = (CWE) obx5;
			String valueIdentifier = value.getIdentifier().getValue();
			log.debug("    value id = " + valueIdentifier);
			String valueName = value.getText().getValue();
			log.debug("    value name = " + valueName);
			if (isConceptProposal(valueIdentifier)) {
				if (log.isDebugEnabled())
					log.debug("Proposing concept");
				throw new ProposingConceptException(concept, valueName);
			} else {
				log.debug("    not proposal");
				try {
					Concept valueConcept = getConcept(value, uid);
					obs.setValueCoded(valueConcept);
					if (OpenmrsUtil.nullSafeEquals(HL7Constants.HL7_LOCAL_DRUG,
							value.getNameOfAlternateCodingSystem().getValue())) {
						Drug valueDrug = new Drug();
						valueDrug.setDrugId(new Integer(value.getAlternateIdentifier().getValue()));
						obs.setValueDrug(valueDrug);
					} else {
						ConceptName valueConceptName = getConceptName(value);
						if (valueConceptName != null) {
							if (log.isDebugEnabled()) {
								log.debug("    value concept-name-id = " + valueConceptName.getConceptNameId());
								log.debug("    value concept-name = " + valueConceptName.getName());
							}
							obs.setValueCodedName(valueConceptName);
						}
					}
				} catch (NumberFormatException e) {
					throw new HL7Exception("Invalid concept ID '" + valueIdentifier + "' for OBX-5 value '" + valueName
							+ "'");
				}
			}
			if (log.isDebugEnabled())
				log.debug("  Done with CWE");
		} else if ("CE".equals(hl7Datatype)) {
			CE value = (CE) obx5;
			String valueIdentifier = value.getIdentifier().getValue();
			String valueName = value.getText().getValue();
			if (isConceptProposal(valueIdentifier)) {
				throw new ProposingConceptException(concept, valueName);
			} else {
				try {
					obs.setValueCoded(getConcept(value, uid));
					obs.setValueCodedName(getConceptName(value));
				} catch (NumberFormatException e) {
					throw new HL7Exception("Invalid concept ID '" + valueIdentifier + "' for OBX-5 value '" + valueName
							+ "'");
				}
			}
		} else if ("DT".equals(hl7Datatype)) {
			DT value = (DT) obx5;
			Date valueDate = getDate(value.getYear(), value.getMonth(), value.getDay(), 0, 0, 0);
			if (value == null || valueDate == null) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			obs.setValueDatetime(valueDate);
		} else if ("TS".equals(hl7Datatype)) {
			DTM value = ((TS) obx5).getTime();
			Date valueDate = getDate(value.getYear(), value.getMonth(), value.getDay(), value.getHour(), value.getMinute(),
					value.getSecond());
			if (value == null || valueDate == null) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			obs.setValueDatetime(valueDate);
		} else if ("TM".equals(hl7Datatype)) {
			TM value = (TM) obx5;
			Date valueTime = getDate(0, 0, 0, value.getHour(), value.getMinute(), value.getSecond());
			if (value == null || valueTime == null) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			obs.setValueDatetime(valueTime);
		} else if ("ST".equals(hl7Datatype)) {
			ST value = (ST) obx5;
			if (value == null || value.getValue() == null || value.getValue().trim().length() == 0) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			obs.setValueText(value.getValue());
		} else {
			// unsupported data type
			// TODO: support RP (report), SN (structured numeric)
			// do we need to support BIT just in case it slips thru?
			throw new HL7Exception("Unsupported observation datatype '" + hl7Datatype + "'");
		}

		return obs;
	}

	/**
	 * Derive a concept name from the CWE component of an hl7 message.
	 */
	private ConceptName getConceptName(CWE cwe) throws HL7Exception {
		ST altIdentifier = cwe.getAlternateIdentifier();
		ID altCodingSystem = cwe.getNameOfAlternateCodingSystem();
		return getConceptName(altIdentifier, altCodingSystem);
	}

	/**
	 * Derive a concept name from the CE component of an hl7 message.
	 */
	private ConceptName getConceptName(CE ce) throws HL7Exception {
		ST altIdentifier = ce.getAlternateIdentifier();
		ID altCodingSystem = ce.getNameOfAlternateCodingSystem();
		return getConceptName(altIdentifier, altCodingSystem);
	}

	/**
	 * Derive a concept name from the CWE component of an hl7 message.
	 */
	private ConceptName getConceptName(ST altIdentifier, ID altCodingSystem) throws HL7Exception {
		if (altIdentifier != null) {
			if (OpenmrsUtil.nullSafeEquals(HL7Constants.HL7_LOCAL_CONCEPT_NAME,
					altCodingSystem.getValue())) {
				String hl7ConceptNameId = altIdentifier.getValue();
				return getConceptName(hl7ConceptNameId);
			}
		}

		return null;
	}

	/**
	 * Utility method to retrieve the openmrs ConceptName specified in an hl7 message observation
	 * segment. This method assumes that the check for 99NAM has been done already and is being
	 * given an openmrs conceptNameId
	 *
	 * @param hl7ConceptNameId internal ConceptNameId to look up
	 * @return ConceptName from the database
	 * @throws HL7Exception
	 */
	private ConceptName getConceptName(String hl7ConceptNameId) throws HL7Exception {
		ConceptName specifiedConceptName = null;
		if (hl7ConceptNameId != null) {
			// get the exact concept name specified by the id
			try {
				Integer conceptNameId = new Integer(hl7ConceptNameId);
				specifiedConceptName = new ConceptName();
				specifiedConceptName.setConceptNameId(conceptNameId);
			} catch (NumberFormatException e) {
				// if it is not a valid number, more than likely it is a bad hl7 message
				log.debug("Invalid concept name ID '" + hl7ConceptNameId + "'", e);
			}
		}
		return specifiedConceptName;

	}

	private boolean isConceptProposal(String identifier) {
		return OpenmrsUtil.nullSafeEquals(identifier, OpenmrsConstants.PROPOSED_CONCEPT_IDENTIFIER);
	}

	private Date getDate(int year, int month, int day, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		// Calendar.set(MONTH, int) is zero-based, Hl7 is not
		cal.set(year, month - 1, day, hour, minute, second);
		return cal.getTime();
	}

	/**
	 * Get an openmrs Concept object out of the given hl7 coded element
	 *
	 * @param codedElement ce to pull from
	 * @param uid          unique string for this message for any error reporting purposes
	 * @return new Concept object
	 * @throws HL7Exception if parsing errors occur
	 */
	private Concept getConcept(CE codedElement, String uid) throws HL7Exception {
		String hl7ConceptId = codedElement.getIdentifier().getValue();

		String codingSystem = codedElement.getNameOfCodingSystem().getValue();
		return getConcept(hl7ConceptId, codingSystem, uid);
	}

	/**
	 * Get an openmrs Concept object out of the given hl7 coded with exceptions element
	 *
	 * @param codedElement cwe to pull from
	 * @param uid          unique string for this message for any error reporting purposes
	 * @return new Concept object
	 * @throws HL7Exception if parsing errors occur
	 */
	private Concept getConcept(CWE codedElement, String uid) throws HL7Exception {
		String hl7ConceptId = codedElement.getIdentifier().getValue();

		String codingSystem = codedElement.getNameOfCodingSystem().getValue();
		return getConcept(hl7ConceptId, codingSystem, uid);
	}

	/**
	 * Get a concept object representing this conceptId and coding system.<br/>
	 * If codingSystem is 99DCT, then a new Concept with the given conceptId is returned.<br/>
	 * Otherwise, the coding system is looked up in the ConceptMap for an openmrs concept mapped to
	 * that code.
	 *
	 * @param hl7ConceptId the given hl7 conceptId
	 * @param codingSystem the coding system for this conceptid (e.g. 99DCT)
	 * @param uid          unique string for this message for any error reporting purposes
	 * @return a Concept object or null if no conceptId with given coding system found
	 */
	protected Concept getConcept(String hl7ConceptId, String codingSystem, String uid) throws HL7Exception {
		if (OpenmrsUtil.nullSafeEquals(HL7Constants.HL7_LOCAL_CONCEPT,
				codingSystem)) {
            // the concept is local
			try {
                return Context.getConceptService().getConcept(Integer.valueOf(hl7ConceptId));
			} catch (NumberFormatException e) {
				throw new HL7Exception("Invalid concept ID '" + hl7ConceptId + "' in hl7 message with uid: " + uid);
			}
		} else {
			// the concept is not local, look it up in our mapping
            Concept concept = Context.getConceptService().getConceptByMapping(hl7ConceptId, codingSystem);
			if (concept == null)
				log.error("Unable to find concept with code: " + hl7ConceptId + " and mapping: " + codingSystem
						+ " in hl7 message with uid: " + uid);
			return concept;
		}
	}

	/**
	 * Pull the timestamp for this obx out. if an invalid date is found, null is returned
	 */
	private Date getDatetime(OBX obx) throws HL7Exception {
		TS ts = obx.getDateTimeOfTheObservation();
		return getDatetime(ts);
	}

	/**
	 * Pull the timestamp for this obr out. if an invalid date is found, null is returned
	 */
	private Date getDatetime(OBR obr) throws HL7Exception {
        if(obr==null) return null;
		TS ts = obr.getObservationDateTime();
		return getDatetime(ts);
	}

    private Date getDateTime(ORC orc) throws HL7Exception {
        if(orc==null) return null;
        TS ts = orc.getDateTimeOfTransaction();
        return getDatetime(ts);
    }
	/**
	 * Return a java date object for the given TS
	 */
	private Date getDatetime(TS ts) throws HL7Exception {
		Date datetime = null;
		DTM value = ts.getTime();

		if (value.getYear() == 0 || value.getValue() == null)
			return null;

		try {
			datetime = getDate(value.getYear(), value.getMonth(), value.getDay(), value.getHour(), value.getMinute(), value
					.getSecond());
		} catch (DataTypeException e) {
			log.warn("could not process the date " + value + ", returning null", e);
		}
		return datetime;

	}

	private Date getEncounterDate(PV1 pv1) throws HL7Exception {
		return tsToDate(pv1.getAdmitDateTime());
	}

	private Provider getProviderBySystemId(String systemId) {
		return Context.getService(PcsLabInterfaceService.class).getProviderBySystemId(systemId);
	}

	private Provider getProvider(ORU_R01 oru) throws HL7Exception {
		// prefer PD1
        Provider provider = getProvider(getPD1(oru));

        if(provider == null) {      //Try PV1
            provider = getProvider(getPV1(oru));
        }

		return provider;
	}

	private Provider getProvider(PV1 pv1) throws HL7Exception {
        if(pv1==null) return null;
		if (pv1.getAttendingDoctor().length == 0)
			return null;

        return getProvider(pv1.getAttendingDoctor(0));
	}

    private Provider getProvider(PD1 pd1) throws HL7Exception {
        if(pd1==null) return null;
        if(pd1.getPatientPrimaryCareProviderNameIDNo().length==0){
            return null;
        }

        //Get the first one (Usually there is only one anyway)
        return getProvider(pd1.getPatientPrimaryCareProviderNameIDNo(0));
    }

    private Provider getProvider(XCN hl7Provider) throws HL7Exception {
        if(hl7Provider==null) return null;

        // PCS sends systemIds for provider identifier
        String identifier = hl7Provider.getIDNumber().getValue();
        if (identifier.matches("\\d+-\\d")) {
            return getProviderBySystemId(identifier);
        }

        Integer personId = Context.getHL7Service().resolvePersonId(hl7Provider);
        if (personId == null)
            throw new HL7Exception("Could not resolve provider");

        return getProviderByPersonId(personId);
    }

    private Provider getProviderByPersonId(Integer personId) {
        List<Provider> providers = (List<Provider>)Context.getProviderService().getProvidersByPerson(new Person(personId));
        if(providers!=null && !providers.isEmpty()) {
            return providers.get(0);
        }
        return null;
    }
	private Patient getPatient(PID pid) throws HL7Exception {
		Integer patientId = Context.getHL7Service().resolvePatientId(pid);
		if (patientId == null)
			throw new HL7Exception("Could not resolve patient");
		Patient patient = new Patient();
		patient.setPatientId(patientId);
		return patient;
	}

	/**
	 * gets a relative based on an NK1 segment
	 */
	private Person getRelative(NK1 nk1) throws HL7Exception {
		// if there are no associated party identifiers, the person will not exist
		if (nk1.getNextOfKinAssociatedPartySIdentifiers().length < 1)
			return null;
		// find the related person via given IDs
		return Context.getHL7Service().resolvePersonFromIdentifiers(nk1.getNextOfKinAssociatedPartySIdentifiers());
	}

	private Location getLocation(ORU_R01 oru) throws HL7Exception {
		Integer locationId = null;

		// try PV1 first
		PV1 pv1 = getPV1(oru);
		PL hl7Location = pv1.getAssignedPatientLocation();
		if (hl7Location.getPointOfCare().getValue() != null) {
			locationId = Context.getHL7Service().resolveLocationId(hl7Location);
		}

		// try PD1 second
		if (locationId == null) {
			PD1 pd1 = getPD1(oru);
			if (pd1.getPatientPrimaryFacility().length > 0) {
				locationId = Integer.parseInt(pd1.getPatientPrimaryFacility(0).getIDNumber().getValue());
			}
		}

		// give up easily if nothing found
		if (locationId == null)
			return null;

		// return a shell location
		Location location = new Location();
		location.setLocationId(locationId);
		return location;
	}

	private Form getForm(MSH msh) throws HL7Exception {
		Integer formId = null;
		try {
			formId = Integer.parseInt(msh.getMessageProfileIdentifier(0).getEntityIdentifier().getValue());
		} catch (Exception e) {
			log.warn("Error parsing form id from message, using null form");
		}

		// must get entire form object in order to get its metadata
		// (encounterType) later
		Form form = null;

		if (formId != null)
			form = Context.getFormService().getForm(formId);

		return form;
	}

	private EncounterType getEncounterType(MSH msh, Form form) {
		if (form != null)
			return form.getEncounterType();
		// TODO: resolve encounter type from MSH data
		return null;
	}

	private User getEnterer(ORC orc) throws HL7Exception {
		XCN hl7Enterer = orc.getEnteredBy(0);
        String idNumber = hl7Enterer.getIDNumber().getValue();
        String familyName = hl7Enterer.getFamilyName().getSurname().getValue();
        String giveName = hl7Enterer.getGivenName().getValue();
        if(idNumber==null && familyName==null && giveName==null) return null;
        if(idNumber.length()==0 && familyName.length()==0 & giveName.length()==0) return null;
        Integer entererId = Context.getHL7Service().resolveUserId(hl7Enterer);
		if (entererId == null)
			throw new HL7Exception("Could not resolve enterer");
		User enterer = new User();
		enterer.setUserId(entererId);
		return enterer;
	}

	//TODO: Debug (and use) methods in HL7Util instead
	private Date tsToDate(TS ts) throws HL7Exception {
		// need to handle timezone
		String dtm = ts.getTime().getValue();

		if (dtm == null)
			return null;

		int year = Integer.parseInt(dtm.substring(0, 4));
		int month = (dtm.length() >= 6 ? Integer.parseInt(dtm.substring(4, 6)) - 1 : 0);
		int day = (dtm.length() >= 8 ? Integer.parseInt(dtm.substring(6, 8)) : 1);
		int hour = (dtm.length() >= 10 ? Integer.parseInt(dtm.substring(8, 10)) : 0);
		int min = (dtm.length() >= 12 ? Integer.parseInt(dtm.substring(10, 12)) : 0);
		int sec = (dtm.length() >= 14 ? Integer.parseInt(dtm.substring(12, 14)) : 0);
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, min, sec);
		// if (cal.getTimeZone().getRawOffset() != timeZoneOffsetMillis) {
		// TimeZone tz = (TimeZone)TimeZone.getDefault().clone();
		// tz.setRawOffset(timeZoneOffsetMillis);
		// cal.setTimeZone(tz);
		// }
		return cal.getTime();
	}

	/**
	 * Creates a ConceptProposal object that will need to be saved to the database at a later point.
	 */
	private ConceptProposal createConceptProposal(Encounter encounter, Concept concept, String originalText) {
		// value is a proposed concept, create a ConceptProposal
		// instead of an Obs for this observation
		// TODO: at this point if componentSeparator (^) is in text,
		// we'll only use the text before that delimiter!
		ConceptProposal conceptProposal = new ConceptProposal();
		conceptProposal.setOriginalText(originalText);
		conceptProposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
		conceptProposal.setEncounter(encounter);
		conceptProposal.setObsConcept(concept);
		return conceptProposal;
	}

	private void updateHealthCenter(Patient patient, ORU_R01 oru) {
		PV1 pv1 = getPV1(oru);

		// Update patient's location if it has changed
		if (log.isDebugEnabled())
			log.debug("Checking for discharge to location");

		DLD dld = pv1.getDischargedToLocation();
		log.debug("DLD = " + dld);
		if (dld == null)
			return;
		IS hl7DischargeToLocation = dld.getDischargeLocation();
		log.debug("is = " + hl7DischargeToLocation);
		if (hl7DischargeToLocation == null)
			return;
		String dischargeToLocation = hl7DischargeToLocation.getValue();
		log.debug("dischargeToLocation = " + dischargeToLocation);
		if (dischargeToLocation != null && dischargeToLocation.length() > 0) {
			if (log.isDebugEnabled())
				log.debug("Patient discharged to " + dischargeToLocation);
			// Ignore anything past the first subcomponent (or component)
			// delimiter
			for (int i = 0; i < dischargeToLocation.length(); i++) {
				char ch = dischargeToLocation.charAt(i);
				if (ch == '&' || ch == '^') {
					dischargeToLocation = dischargeToLocation.substring(0, i);
					break;
				}
			}
			Integer newLocationId = Integer.parseInt(dischargeToLocation);
			// Hydrate a full patient object from patient object containing only
			// identifier
			patient = Context.getPatientService().getPatient(patient.getPatientId());

			PersonAttributeType healthCenterAttrType = Context.getPersonService().getPersonAttributeTypeByName(
					"Health Center");

			if (healthCenterAttrType == null) {
				log.error("A person attribute type with name 'Health Center' is not defined but patient "
						+ patient.getPatientId() + " is trying to change their health center to " + newLocationId);
				return;
			}

			PersonAttribute currentHealthCenter = patient.getAttribute("Health Center");

			if (currentHealthCenter == null || !currentHealthCenter.equals(newLocationId.toString())) {
				PersonAttribute newHealthCenter = new PersonAttribute(healthCenterAttrType, newLocationId.toString());

				log.debug("Updating patient's location from " + currentHealthCenter + " to " + newLocationId);

				// add attribute (and void old if there is one)
				patient.addAttribute(newHealthCenter);

				// save the patient and their new attribute
				Context.getPatientService().savePatient(patient);
			}

		}
		log.debug("finished discharge to location method");
	}

	/**
	 * finds patient by matching on identifier
	 */
	private Patient getPatientByIdentifier(PID pid) throws HL7Exception {

		Patient p = null;

		try {
			p = getPatient(pid);
		} catch (HL7Exception e) {
			log.info("Could not find patient by id, trying identifiers.");
		}

		// if we found the patient by normal means, return it
		if (p != null)
			return p;

		// otherwise, look for patient by identifier, ignoring the identifier type
		CX[] patientIdentifierList = pid.getPatientIdentifierList();
		if (patientIdentifierList.length == 0)
			throw new HL7Exception("No patient identifiers found in PID segment.");

		Set<String> identifiers = new HashSet<String>();
		Set<Patient> matchingPatients = new HashSet<Patient>();
		for (CX identifier : patientIdentifierList) {
			String idNumber = identifier.getIDNumber().getValue();
			String checkDigit = identifier.getCheckDigit().getValue();
			if (!(idNumber.contains("-")) && checkDigit != null)
				idNumber = idNumber + "-" + checkDigit;
			identifiers.add(idNumber);
			matchingPatients.addAll(Context.getPatientService().getPatients(null, idNumber, null, true));
		}
		if ((matchingPatients.isEmpty()) || (matchingPatients.size() != 1))
			throw new HL7Exception("Could not find patient matching identifier(s): " + OpenmrsUtil.join(identifiers, ", "));

		p = matchingPatients.iterator().next();
		log.info("Matching hl7 PID patient_id: " + p.getPatientId() + ", " + p.getPatientIdentifier());

		return p;
	}

    /**
     * LabORUR01Handler#isLabMessage checks whether a message is a Lab message or not.
     * @param message
     * @return  true or false
     */
    public boolean isLabMessage(Message message) {
        if (message != null && "ORU_R01".equals(message.getName())) {
            ORU_R01 oru = (ORU_R01) message;
            MSH msh = getMSH(oru);
            String sendingApplication = msh.getSendingApplication().getNamespaceID().getValue();

            // TODO remove these tests when we are ready to handle all ORU^R01 messages
            // check for special indicators

            log.debug(oru.getMSH()+" Sending application is: "+ sendingApplication);

            if (!StringUtils.hasLength(sendingApplication))
                return false;

            if (allowedSendingApps.contains(sendingApplication.toLowerCase()))
                return true;
        }

        return false;
    }
}
