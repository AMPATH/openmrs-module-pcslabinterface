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
import org.openmrs.scheduler.tasks.AbstractTask;

public class ProcessPcsLabInterfaceQueueTask extends AbstractTask {
	private static Log log = LogFactory
			.getLog(ProcessPcsLabInterfaceQueueTask.class);

	private PcsLabInterfaceQueueProcessor processor = null;

	public ProcessPcsLabInterfaceQueueTask() {
		if (this.processor == null)
			this.processor = new PcsLabInterfaceQueueProcessor();
	}

	public void execute() {
		Context.openSession();
		log.debug("Processing PcsLabInterface queue ... ");
		try {
			if (!(Context.isAuthenticated()))
				authenticate();
			this.processor.processLabMessageQueue();
		} catch (APIException e) {
			throw e;
		} finally {
			Context.closeSession();
		}
	}

	public void shutdown() {
		log.debug("Shutting down ProcessPcsLabInterfaceQueue task ...");
		this.processor = null;
	}
}
