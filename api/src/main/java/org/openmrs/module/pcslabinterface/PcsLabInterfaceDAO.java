/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.pcslabinterface;

import org.openmrs.Person;
import org.openmrs.Provider;

import java.util.List;

/**
 *
 * @author jkeiper
 */
public interface PcsLabInterfaceDAO {

	public List<Integer> getNumericConceptIds();

	Provider getProviderBySystemId(String systemId);
}
