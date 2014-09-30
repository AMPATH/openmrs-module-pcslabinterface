package org.openmrs.module.pcslabinterface.rules;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 *
 */

public class RemoveAfbConfirmationNullValueSegmentsTest {
    /**
     * @verifies only match confirmation  with null values
     * @see RemoveAfbConfirmationNullValueSegments#RemoveAfbConfirmationNullValueSegments()
     */
    @Test
    @Verifies(value = "Should only match Confirmation with Null values",method="matches")
    public void matches_shouldOnlyMatchConfirmationWithNullValues() throws Exception {
        String withNullValue = "OBX|2|CWE|2311^CONFIRMATION^99DCT||^^99DCT||||||X|||201305310449|||||201305311722";
        String withoutNullValue = "OBX|2|CWE|2311^CONFIRMATION^99DCT||1240^Some Concept^99DCT||||||X|||201305310449|||||201305311722";

        Assert.assertTrue("Does not match DNA PCR with null", new RemoveAfbConfirmationNullValueSegments().matches(withNullValue));
        Assert.assertFalse("Matches DNA PCR with non-null", new RemoveAfbConfirmationNullValueSegments().matches(withoutNullValue));
    }

    /**
     * @verifies return a null string if matched
     * @see RemoveAfbConfirmationNullValueSegments#transform(String)
     */
    @Test
    @Verifies(value = "Should Return a null if string is matched", method = "transform")
    public void transform_shouldReturnANullStringIfMatched() throws Exception {
        String input = "OBX|2|CWE|2311^CONFIRMATION^99DCT||^^99DCT||||||X|||201305310449|||||201305311722";
        RegexTransformRule rule = new RemoveAfbConfirmationNullValueSegments();
        Assert.assertTrue(rule.matches(input) && null == rule.transform(input));
    }

    @Test
    @Verifies(value="Should Return the same value if not matched", method = "transform")
    public void transform_shouldReturnInputIfNOtMatched() throws Exception {
        String input = "OBX|2|CWE|2311^CONFIRMATION^99DCT||1240^Some Concept^99DCT||||||X|||201305310449|||||201305311722";
        RegexTransformRule rule = new RemoveAfbConfirmationNullValueSegments();
        Assert.assertTrue(!rule.matches(input) && input.equals(rule.transform(input)));
    }
}
