package org.openmrs.module.pcslabinterface.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Several numeric values for HIV Viral Load are simply digits with commas in
 * the thousands and millions places. These should be converted to valid HL7
 * numeric values by stripping the commas and adding a comment.
 * 
 * @author jkeiper
 */
public class RemoveValueModifiersFromHIVViralLoads extends RegexTransformRule {

	// this regex ensures that the value has only digits and/or commas in it
	private Pattern valuePattern = Pattern.compile("OBX\\|\\d*\\|NM\\|856\\^HIV Viral Load\\^99DCT\\|[^\\|]*\\|([<>]\\d+)\\|.*");
	
	/**
	 * initializes the regex pattern for matching on a specific concept 
	 * 
	 * @should match only numeric OBX segments for HIV Viral Load with a modifier before the value
	 */
	public RemoveValueModifiersFromHIVViralLoads() {
		// the follow regex ensures that the concept is HIV Viral Load and the value has at least one comma in it
		super("OBX\\|\\d*\\|NM\\|856\\^HIV Viral Load\\^99DCT\\|[^\\|]*\\|[<>]\\d+\\|.*");
	}
	
	/**
	 * transforms the test string by stripping commas from the value and
	 * appending a comment (NTE segment) with the original value
	 * 
	 * @should decrease value by one if using less than modifier
	 * @should increase value by one if using greater than modifier
	 */
	@Override
	public String transform(String test) {
		// check to make sure the value is what we expect
		Matcher m = valuePattern.matcher(test);
		if (!m.matches())
			return test;

		// yank the value from the test string
		String value = m.group(1);

		// get modifier from the first character
		char modifier = value.charAt(0);
		
		// remove modifier from the value
		value = value.substring(1);
		
		Integer newValue = null;
		// test to see if newValue really is an Integer
		try {
			newValue = Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return test;
		}
		
		// modify the value based on the modifier; < means -1, > means + 1
		if (newValue != null)
			newValue = (modifier == '<') ? newValue - 1 : newValue + 1;
			
		// replace first occurrence of value with newValue 
		test = test.replaceFirst(modifier + value, newValue.toString());

		// append a comment describing the change
		return test.concat("\rNTE|||PCSLabInterface modified value; original was: " + modifier).concat(value);
	}
}
