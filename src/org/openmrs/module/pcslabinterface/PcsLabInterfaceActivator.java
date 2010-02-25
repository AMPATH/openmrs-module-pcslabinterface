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

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;

public class PcsLabInterfaceActivator implements Activator {
	private Log log;

	public PcsLabInterfaceActivator() {
		this.log = LogFactory.getLog(super.getClass());
	}

	public void startup() {
		this.log.info("Starting the PcsLabInterface module");

		Properties runtimeProperties = Context.getRuntimeProperties();

		//PcsLabInterfaceConstants.PCSLABINTERFACE_CABEXTRACT_LOCATION = runtimeProperties
		//		.getProperty("pcslabinterface.cabextract_location");
		//PcsLabInterfaceConstants.PCSLABINTERFACE_LCAB_LOCATION = runtimeProperties
		//		.getProperty("pcslabinterface.lcab_location");
	}

	public void shutdown() {
		this.log.info("Shutting down the PcsLabInterface module");
	}
}
