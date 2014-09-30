package org.openmrs.module.pcslabinterface.rules;

import java.util.regex.Matcher;

/**
 * PCS sends Confirmation results with null value, this rule stripes all segments of such type
 * to prevent errors in processing.
 */

public class RemoveAfbConfirmationNullValueSegments extends RegexTransformRule{
    // this regex ensures that the segment is for DNA PCR with a null value
    private static final String pattern = "OBX\\|\\d*\\|CWE\\|2311\\^CONFIRMATION\\^99DCT\\|\\|\\^\\^99DCT\\|.+";

    /**
     * initializes the regex pattern for matching
     *
     * @should only match Confirmation with null values
     */
    public RemoveAfbConfirmationNullValueSegments() {
        super(pattern);
    }

    /**
     * completely replaces the OBX with an NTE segment indicating it was removed
     *
     * @should return a null string if matched
     */
    @Override
    public String transform(String test) {
        // check for numeric concept
        Matcher m = getRegex().matcher(test);
        if (!m.matches())
            return test;

        // return nothing ... so that the test can be removed
        return null;
    }
}
