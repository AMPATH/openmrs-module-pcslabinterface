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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.module.pcslabinterface.rules.TransformRule;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Transactional
public class PcsLabInterfaceQueueProcessor {
	private static final Log log = LogFactory
			.getLog(PcsLabInterfaceQueueProcessor.class);
	private static Boolean isRunning = Boolean.valueOf(false);
	private static final Pattern keyPattern = Pattern
			.compile("R01\\|([a-zA-Z0-9]+)\\|");

	/**
	 * processes a given queue into the HL7 in queue
	 * 
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceQueueProcessor#transformNextLabMessage()
	 * @param labMessage
	 */
	public void parseLabMessage(LabMessage labMessage) {
		log.debug("Transforming LabMessage");

		// build the HL7 message
		HL7InQueue hl7InQueue = new HL7InQueue();

		// pre-process the HL7 message
		String hl7Message = preProcessMessage(labMessage.getData());
		hl7InQueue.setHL7Data(hl7Message);

		// TODO: do something better than this for choosing HL7Source
		hl7InQueue.setHL7Source(Context.getHL7Service().getHL7Source(
				Integer.valueOf(1)));

		// generate the source key
		String hl7SourceKey = String.valueOf(labMessage.getLabMessageId());
		// if possible, extract the source key from the lab message
		// TODO use an HL7 parser to find it
		Matcher m = keyPattern.matcher(labMessage.getData());
		if (m.find())
			hl7SourceKey = m.group(1);
		hl7InQueue.setHL7SourceKey(hl7SourceKey);

		// save the HL7 message
		Context.getHL7Service().saveHL7InQueue(hl7InQueue);

		// archive the queue
		PcsLabInterfaceService pcsService = (PcsLabInterfaceService) Context
				.getService(PcsLabInterfaceService.class);

		LabMessageArchive labMessageArchive = new LabMessageArchive(labMessage);
		pcsService.createLabMessageArchive(labMessageArchive);
		pcsService.deleteLabMessage(labMessage);

		pcsService.garbageCollect();
	}

	/**
	 * process the message and apply rules
	 * 
	 * @param data the message to be processed
	 * @return results of processing the message
	 * @should remove commas from HIV Viral Loads
	 * @should correct values with modifiers from HIV Viral Loads
	 * @should process values with both commas and modifiers from HIV Viral
	 *         Loads
	 */
	protected String preProcessMessage(String data) {
		// TODO '\r' happens to be the character between lines at this time, but
		// this may not always be the case. we should make this more flexible to
		// recognize line endings
		String[] lines = data
				.split(PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE);
		List<String> results = new ArrayList<String>();

		// loop through lines of the HL7
		for (String line : lines) {
			// loop through transform rules
			for (TransformRule rule : PcsLabInterfaceConstants
					.TRANSFORM_RULES())
				if (rule.matches(line))
					// TODO perhaps expect a list back from transform() so we
					// can addAll() results
					line = rule.transform(line);
			// append the line to the results
			results.add(line);
		}

		return StringUtils.collectionToDelimitedString(results,
				PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE);
	}

	/**
	 * picks the next queue and transforms it
	 * 
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceQueueProcessor#processLabMessageQueue()
	 * @return
	 */
	public boolean transformNextLabMessage() {
		boolean transformOccurred = false;
		PcsLabInterfaceService pcsService = null;
		try {
			pcsService = (PcsLabInterfaceService) Context
					.getService(PcsLabInterfaceService.class);
		} catch (APIException e) {
			log.debug("PcsLabInterfaceService not found");
			return false;
		}
		LabMessage labMessage;
		if ((labMessage = pcsService.getNextLabMessage()) != null) {
			parseLabMessage(labMessage);
			transformOccurred = true;
		}
		return transformOccurred;
	}

	/**
	 * iterates over queue contents for transformation
	 * 
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceQueueTask#execute()
	 * @throws APIException
	 */
	public void processLabMessageQueue() throws APIException {
		synchronized (isRunning) {
			if (isRunning.booleanValue()) {
				log.warn("PcsLabInterfaceQueue processor aborting (another processor already running)");
				return;
			}
			isRunning = Boolean.valueOf(true);
		}
		try {
			log.debug("Start processing PcsLabInterface queue");
			log.debug("PcsLabInterface processor hash: " + super.hashCode());
			while (transformNextLabMessage())
				;
			log.debug("Done processing PcsLabInterface queue");
		} finally {
			isRunning = Boolean.valueOf(false);
		}
	}
}
