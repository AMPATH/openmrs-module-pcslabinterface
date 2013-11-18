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

public class ConvertPV1ToPD1Test {

	/**
	 * @verifies convert the PV1 segment to a PD1
	 * @see ConvertPV1ToPD1#transform(String)
	 */
	@Test
	public void transform_shouldConvertThePV1SegmentToAPD1() throws Exception {
		String hl7string = "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V";
		String expected = "PD1|||Unknown Location^D^1^^^AMRS^L^AMPATH|1^Super User (1-8)";
		Assert.assertEquals(expected, new ConvertPV1ToPD1().transform(hl7string));
	}
}
