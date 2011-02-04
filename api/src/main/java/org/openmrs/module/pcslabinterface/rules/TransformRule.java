package org.openmrs.module.pcslabinterface.rules;

public interface TransformRule {
	
	/**
	 * answers whether this rule applies to this line
	 * 
	 * @param test single line of an HL7 message to be tested
	 * @return true or false
	 */
	public boolean matches(String test);

	/**
	 * modify the HL7 line
	 * 
	 * @param test single line of an HL7 message to be transformed
	 * @return the transformed string
	 */
	public String transform(String test);
}
