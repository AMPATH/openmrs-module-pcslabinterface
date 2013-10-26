package org.openmrs.module.pcslabinterface.rules;

import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PCS messages have form references in the MSH; we do not have a form
 * for labs, so this should be removed.
 * 
 * @author jkeiper
 */
public class RemovePV1Segment extends RegexTransformRule {

	// this regex ensures that the MSH is referencing a form
	private Pattern valuePattern = Pattern
			.compile("MSH\\|.+\\|\\d+\\^AMRS.ELD.FORMID");

	/**
	 * initializes the regex pattern
	 *
	 * @should match only numeric OBX segments for HIV Viral Load with commas in the value
	 * @should match values with other characters as long as there is at least one comma
	 */
	public RemovePV1Segment() {
		// the following regex ensures that the MSH is referencing a form
		super("MSH\\|.+\\|\\d\\^AMRS.ELD.FORMID");
	}

	/**
	 * transforms the string by removing the form information
	 * 
	 * @should replace improper concept reference
	 * @should not replace proper concept reference
	 */
	@Override
	public String transform(String test) {
		// check to make sure the value is truly just numbers and commas
		Matcher m = valuePattern.matcher(test);
		if (!m.matches())
			return test;

		// take off the chunk we don't want
		test = test.replaceFirst("\\^Negative\\^99DCT", "664^NEGATIVE^99DCT");

		// append a comment describing the change
		return test.concat(PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE)
				.concat("NTE|||")
				.concat(PcsLabInterfaceConstants.LAB_VALUE_MODIFIED)
				.concat("^Negative^99DCT");
	}
}
