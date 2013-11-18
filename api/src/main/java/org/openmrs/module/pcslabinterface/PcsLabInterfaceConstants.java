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

import org.openmrs.module.pcslabinterface.rules.AdjustNegativeConceptForUrineProtein;
import org.openmrs.module.pcslabinterface.rules.ChangeDatatypeForNumericObservations;
import org.openmrs.module.pcslabinterface.rules.ConvertPV1ToPD1;
import org.openmrs.module.pcslabinterface.rules.RemoveCommasFromHIVViralLoads;
import org.openmrs.module.pcslabinterface.rules.RemoveConfirmationNullValueSegments;
import org.openmrs.module.pcslabinterface.rules.RemoveDNAPCRNullValueSegments;
import org.openmrs.module.pcslabinterface.rules.RemoveValueModifiersFromHIVViralLoads;
import org.openmrs.module.pcslabinterface.rules.TransformRule;

public class PcsLabInterfaceConstants {

	public static final String PCSLABINTERFACE_GP_QUEUE_DIR = "pcslabinterface.queue_dir";
	public static final String PCSLABINTERFACE_GP_QUEUE_DIR_DEFAULT = "pcslabinterface/queue";
	public static final String PCSLABINTERFACE_GP_QUEUE_ARCHIVE_DIR = "pcslabinterface.queue_archive_dir";
	public static final String PCSLABINTERFACE_GP_QUEUE_ARCHIVE_DIR_DEFAULT = "pcslabinterface/archive/%Y/%M";

	public static final String PRIV_ADD_LAB_MESSAGE_ARCHIVE = "Add PCSLabInterface Message Archive";
	public static final String PRIV_VIEW_LAB_MESSAGE_ARCHIVE = "View PcsLabInterface Message Archive";
	public static final String PRIV_DELETE_LAB_MESSAGE_ARCHIVE = "Delete PcsLabInterface Message Archive";
	public static final String PRIV_VIEW_LAB_MESSAGE_ERROR = "View PcsLabInterface Message Error";

	public static final String LAB_VALUE_MODIFIED = "PCS Value: ";
	public static final String ST_TO_NM = "originally ST datatype";
	public static final String MESSAGE_EOL_SEQUENCE = "\r";
	public static final String MODULE_ID = "pcslabinterface";

	private static TransformRule[] rules = null;

	public static final TransformRule[] TRANSFORM_RULES() {
		if (rules == null)
			rules = new TransformRule[]{
					new ChangeDatatypeForNumericObservations(),
					new RemoveCommasFromHIVViralLoads(),
					new RemoveValueModifiersFromHIVViralLoads(),
					new AdjustNegativeConceptForUrineProtein(),
					new RemoveDNAPCRNullValueSegments(),
					new RemoveConfirmationNullValueSegments(),
					new ConvertPV1ToPD1()
			};
		return rules;
	}

	;

}
