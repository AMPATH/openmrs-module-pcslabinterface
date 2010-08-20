package org.openmrs.module.pcslabinterface.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * Several numeric values for HIV Viral Load are simply digits with commas in
 * the thousands and millions places. These should be converted to valid HL7
 * numeric values by stripping the commas and adding a comment.
 * 
 * @author jkeiper
 */
public class RemoveCommasFromHIVViralLoads extends RegexTransformRule {

	// this regex ensures that the value has only digits and/or commas in it
	private Pattern valuePattern = Pattern.compile("OBX\\|\\d*\\|NM\\|856\\^HIV Viral Load\\^99DCT\\|[^\\|]*\\|([0123456789,]+)\\|.*");
	
	/**
	 * initializes the regex pattern for matching on a specific concept 
	 * 
	 * @should match only numeric OBX segments for HIV Viral Load with commas in the value
	 */
	public RemoveCommasFromHIVViralLoads() {
		// the follow regex ensures that the concept is HIV Viral Load and the value has at least one comma in it
		super("OBX\\|\\d*\\|NM\\|856\\^HIV Viral Load\\^99DCT\\|[^\\|]*\\|[^,\\|]*,.*");
	}
	
	/**
	 * transforms the test string by stripping commas from the value and
	 * appending a comment (NTE segment) with the original value
	 * 
	 * @should remove commas from the original value
	 * @should add a comment containing the original value
	 */
	@Override
	public String transform(String test) {
		// check to make sure the value is truly just numbers and commas
		Matcher m = valuePattern.matcher(test);
		if (!m.matches())
			return test;

		// yank the value from the test string
		String value = m.group(1);
		
		// remove the commas
		String newValue = StringUtils.deleteAny(value, ",");
		
		// test to see if newValue really is an Integer
		try {
			Integer.valueOf(newValue);
		} catch (NumberFormatException e) {
			return test;
		}

		// replace first occurrence of value with newValue 
		test = test.replaceFirst(value, newValue);

		// append a comment describing the change
		return test.concat("\rNTE|||PCSLabInterface modified value; original was: ").concat(value);
	}
}
