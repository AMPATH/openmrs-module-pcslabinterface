/**
 * 
 */
package org.openmrs.module.pcslabinterface.rules;

import java.util.regex.Pattern;


/**
 * an abstract TransformRule based on regular expression pattern matching
 */
abstract public class RegexTransformRule implements TransformRule {

	private Pattern regex = null;

	/**
	 * default constructor (empty)
	 */
	public RegexTransformRule() {
	}
	
	/**
	 * custom constructor for initializing the regex pattern

	 * @param pattern the pattern to use for regex checking
	 */
	public RegexTransformRule(String pattern) {
		regex = Pattern.compile(pattern);
	}
	
	/**
	 * @return the regex
	 */
	public Pattern getRegex() {
		return regex;
	}

	/**
	 * @param regex the regex to set
	 */
	public void setRegex(Pattern regex) {
		this.regex = regex;
	}

	/**
	 * Guarantees that the regex pattern for this rule can be found within the test string
	 * 
	 * @see org.openmrs.module.pcslabinterface.rules.TransformRule#matches(java.lang.String)
	 */
	public boolean matches(String test) {
		if (getRegex() != null)
			return getRegex().matcher(test).find();
		return false;
	}

	/**
	 * @see org.openmrs.module.pcslabinterface.rules.TransformRule#transform(java.lang.String)
	 */
	public String transform(String test) {
		return test;
	}

}
