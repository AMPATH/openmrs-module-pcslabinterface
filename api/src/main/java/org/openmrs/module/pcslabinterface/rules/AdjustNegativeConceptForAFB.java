package org.openmrs.module.pcslabinterface.rules;

/**
 * Several numeric values for HIV Viral Load are simply digits with commas in
 * the thousands and millions places. These should be converted to valid HL7
 * numeric values by stripping the commas and adding a comment.
 * 
 * @author Willa
 */
public class AdjustNegativeConceptForAFB extends AdjustNegativeConcept {

	// this regex ensures that the value has only digits and/or commas in it
	private static final String pattern = "OBX\\|\\d*\\|CWE\\|2339\\^AFB Microscopy sputum\\^99DCT\\|[^\\|]*\\|(\\^Negative\\^99DCT)\\|.*";

	/**
	 * initializes the regex pattern for matching on a specific concept
	 *
	 * @should match only numeric OBX segments for HIV Viral Load with commas in the value
	 * @should match values with other characters as long as there is at least one comma
	 */
	public AdjustNegativeConceptForAFB() {
        super(pattern);
	}
}
