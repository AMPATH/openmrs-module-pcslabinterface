/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.pcslabinterface;

import org.springframework.util.StringUtils;

/**
 *
 * @author jkeiper
 */
public class PcsLabInterfaceTestUtil {
	
	public static String makeMessage(String... lines) {
		return StringUtils.arrayToDelimitedString(lines, PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE);
	}
}
