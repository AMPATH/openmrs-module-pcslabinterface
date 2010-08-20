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
	 * @see org.openmrs.module.pcslabinterface.rules.TransformRule#matches(java.lang.String)
	 */
	public boolean matches(String test) {
		if (getRegex() != null)
			return getRegex().matcher(test).matches();
		return false;
	}

	/**
	 * @see org.openmrs.module.pcslabinterface.rules.TransformRule#transform(java.lang.String)
	 */
	public String transform(String test) {
		return test;
	}

}
