package org.openmrs.module.pcslabinterface.rules;

import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PCS messages need to be entered as headless (no encounter).  The signal
 * for this is removing the PV1 segment from the HL7.
 * 
 * @author jkeiper
 */
public class RemovePV1Segment extends RegexTransformRule {

	// this regex ensures that this line is the PV1
	private Pattern valuePattern = Pattern
			.compile("PV1\\|.+");

	/**
	 * initializes the regex pattern
	 */
	public RemovePV1Segment() {
		// the following regex ensures that this segment is PV1
		super("PV1\\|.+");
	}

	/**
	 * transforms the string by removing the PV1
	 * 
	 * @should remove the PV1 segment
	 */
	@Override
	public String transform(String test) {

		// check for validity
		Matcher m = valuePattern.matcher(test);
		if (!m.matches())
			return test;

		return null;
	}
}
