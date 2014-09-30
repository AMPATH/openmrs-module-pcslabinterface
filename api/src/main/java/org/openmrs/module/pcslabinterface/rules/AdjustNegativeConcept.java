package org.openmrs.module.pcslabinterface.rules;

import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Several numeric values for HIV Viral Load are simply digits with commas in
 * the thousands and millions places. These should be converted to valid HL7
 * numeric values by stripping the commas and adding a comment.
 *
 * We prevent this class from being instantiated because we want users to be more specific in type of messages
 * that have to be transformed.
 * 
 * @author Willa
 */
public abstract class AdjustNegativeConcept extends RegexTransformRule {

    /**
     * This constructor is used for testing purposes
     */
	public AdjustNegativeConcept() {
        super ("OBX\\|\\d*\\|CWE\\|\\d+\\^.+\\^99DCT\\|[^\\|]*\\|(\\^Negative\\^99DCT)\\|.*");
    }
    /**
	 * initializes the regex pattern for matching on a specific concept
	 *
	 * @should match only numeric OBX segments for HIV Viral Load with commas in the value
	 * @should match values with other characters as long as there is at least one comma
	 */
	public AdjustNegativeConcept(String pattern) {
		super(pattern);
	}

	/**
	 * transforms the test string by stripping commas from the value and
	 * appending a comment (NTE segment) with the original value
	 * 
	 * @should replace improper concept reference
	 * @should not replace proper concept reference
	 */
	@Override
	public String transform(String test) {
		// check to make sure the value is truly just numbers and commas
		Matcher m = getRegex().matcher(test);
		if (!m.matches())
			return test;

		// replace all occurrence of value with newValue
        m.reset();
        StringBuffer sb = new StringBuffer(test);
        while(m.find()){
            sb = sb.replace(m.start(1),m.end(1),"664^NEGATIVE^99DCT");
        }
        System.out.println(sb);

		// append a comment describing the change
		return sb.append(PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE)
				.append("NTE|||")
				.append(PcsLabInterfaceConstants.LAB_VALUE_MODIFIED)
				.append("^Negative^99DCT").toString();
	}
}
