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

package org.openmrs.module.pcslabinterface.rules;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;

public class RemovePV1SegmentTest {

	/**
	 * @verifies remove the PV1 segment
	 * @see RemovePV1Segment#transform(String)
	 */
	@Test
	public void transform_shouldRemoveThePV1Segment() throws Exception {
		String hl7string = "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V";

		String expected = null;

		Assert.assertEquals(expected,
				new RemovePV1Segment().transform(hl7string));
	}
}
