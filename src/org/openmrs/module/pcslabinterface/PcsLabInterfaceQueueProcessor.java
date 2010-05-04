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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class PcsLabInterfaceQueueProcessor {
	private static final Log log = LogFactory
			.getLog(PcsLabInterfaceQueueProcessor.class);
	private static Boolean isRunning = Boolean.valueOf(false);

	/**
	 * processes a given queue into the HL7 in queue
	 * 
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceQueueProcessor#transformNextLabMessage()
	 * @param labMessage
	 */
	public void parseLabMessage(LabMessage labMessage) {
		log.debug("Transforming LabMessage");

		String hl7SourceKey = String.valueOf(labMessage.getLabMessageId());

		// build the HL7 message
		HL7InQueue hl7InQueue = new HL7InQueue();
		hl7InQueue.setHL7Data(labMessage.getData());
		// TODO: do something better than this for choosing HL7Source
		hl7InQueue.setHL7Source(Context.getHL7Service().getHL7Source(
				Integer.valueOf(1)));
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
				log
						.warn("PcsLabInterfaceQueue processor aborting (another processor already running)");
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
