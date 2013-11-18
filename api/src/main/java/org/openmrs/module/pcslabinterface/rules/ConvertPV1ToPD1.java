package org.openmrs.module.pcslabinterface.rules;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * PCS messages need to be entered as headless (no encounter).  The signal
 * for this is removing the PV1 segment from the HL7.
 *
 * @author jkeiper
 */
public class ConvertPV1ToPD1 extends RegexTransformRule {

	/**
	 * initializes the regex pattern
	 */
	public ConvertPV1ToPD1() {
		// the following regex ensures that this segment is PV1
		super("PV1\\|.+");
	}

	/**
	 * transforms the string by removing the PV1
	 *
	 * @should convert the PV1 segment to a PD1
	 */
	@Override
	public String transform(String test) {

		String[] testParts = test.split("\\|");

		// give up silently if PV1 does not have enough information
		if (testParts.length < 8) {
			return test;
		}

		String location = testParts[3];
		String[] locationParts = location.split("\\^");

		// give up silently if PV1 does not have enough information
		if (locationParts.length < 2) {
			return test;
		}

		String locationId = locationParts[0];
		String locationName = locationParts[1];

		String provider = testParts[7];

		List<String> out = new ArrayList<String>();
		out.add("PD1");
		out.add("");
		out.add("");
		out.add(locationName + "^D^" + locationId + "^^^AMRS^L^AMPATH");
		out.add(provider);

		return StringUtils.join(out, "|");
	}
}
