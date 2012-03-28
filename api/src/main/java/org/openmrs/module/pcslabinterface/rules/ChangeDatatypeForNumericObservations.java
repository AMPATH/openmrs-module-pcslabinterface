package org.openmrs.module.pcslabinterface.rules;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openmrs.api.context.Context;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceService;

/**
 * PCS is sending some numeric data in OBX segments with ST datatypes.
 * 
 * @author jkeiper
 */
public class ChangeDatatypeForNumericObservations extends RegexTransformRule {

	// this regex ensures that the value has only digits and/or commas in it
	private Pattern valuePattern = Pattern
			.compile("OBX\\|\\d*\\|ST\\|(\\d+)\\^.*");

	// cache of numeric concept ids
	private static List<Integer> numericConceptIds = null;
	
	/**
	 * initializes the regex pattern for matching on a specific concept
	 * 
	 * @should match any OBX with an ST datatype
	 */
	public ChangeDatatypeForNumericObservations() {
		// the following regex ensures that the OBX references an ST datatype
		super("OBX\\|\\d*\\|ST\\|.*");
	}

	/**
	 * transforms the test string by replacing ST with NM and
	 * appending a comment (NTE segment) with the original value
	 * if the concept is supposed to be numeric
	 * 
	 * @should replace ST with NM for numeric concepts
	 * @should add a comment describing change in datatype
	 * @should leave segment alone if concept is not numeric
	 */
	@Override
	public String transform(String test) {
		// check for numeric concept
		Matcher m = valuePattern.matcher(test);
		if (!m.matches())
			return test;

		// yank the value from the test string
		String value = m.group(1);

		// fail if concept is not numeric
		if (!getNumericConceptIds().contains(Integer.parseInt(value)))
			return test;

		// replace first occurrence of ST with NM
		test = test.replaceFirst("\\|ST\\|", "\\|NM\\|");

		// append a comment describing the change
		return test.concat(PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE)
				.concat("NTE|||")
				.concat(PcsLabInterfaceConstants.LAB_VALUE_MODIFIED)
				.concat(PcsLabInterfaceConstants.ST_TO_NM);
	}

	private List<Integer> getNumericConceptIds() {
		if (numericConceptIds == null)
			numericConceptIds = Context.getService(PcsLabInterfaceService.class).getNumericConceptIds();
		
		return numericConceptIds;
	}
}
