/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.pcslabinterface.web.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;

import java.text.SimpleDateFormat;

/**
 * Converts a Patient to XML
 */
public class PatientXmlConverter implements Converter {

    public boolean canConvert(Class clazz) {
        return clazz.equals(Patient.class);
    }

    /**
     * All dates are reported in YYYY-MM-DD format
     */
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat(
            "yyyy-MM-dd");

    public void marshal(Object value, HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        Patient patient = (Patient) value;

        if (patient.getBirthdate() != null)
            writer.addAttribute("birthdate", dateFormatter.format(patient.getBirthdate()));
        writer.addAttribute("birthdateEstimated", nullSafeString(patient.getBirthdateEstimated()));
        writer.addAttribute("gender", patient.getGender());

        writer.startNode("identifierList");
        for (PatientIdentifier pid : patient.getIdentifiers()) {
            writer.startNode("identifier");
            if (pid.isVoided())
                writer.addAttribute("voided", "1");
            if (pid.isPreferred())
                writer.addAttribute("preferred", "1");
            writer.addAttribute("type", pid.getIdentifierType().getName());
            // TODO: should encode invalid chars in name
            writer.setValue(pid.getIdentifier());
            writer.endNode();
        }
        writer.endNode();

        PersonName name = patient.getPersonName();
        if (name != null) {
            writer.startNode("name");
            addOptionalElement(writer, "prefix", name.getPrefix());
            addOptionalElement(writer, "givenName", name.getGivenName());
            addOptionalElement(writer, "middleName", name.getMiddleName());
            addOptionalElement(writer, "familyName", name.getFamilyName());
            addOptionalElement(writer, "familyName2", name.getFamilyName2());
            addOptionalElement(writer, "degree", name.getDegree());
            writer.endNode();
        }

        writer.startNode("addressList");
        for (PersonAddress address : patient.getAddresses()) {
            writer.startNode("address");
            if (address.getPreferred())
                writer.addAttribute("preferred", "1");
            addOptionalElement(writer, "address1", address.getAddress1());
            addOptionalElement(writer, "address2", address.getAddress2());
            addOptionalElement(writer, "cityVillage", address.getCityVillage());
            addOptionalElement(writer, "countyDistrict", address.getCountyDistrict());
            addOptionalElement(writer, "stateProvince", address.getStateProvince());
            addOptionalElement(writer, "country", address.getCountry());
            writer.endNode();
        }
        writer.endNode();
    }

    private static String nullSafeString(Object o) {
        if (o != null)
            return o.toString();
        return "";
    }

    /**
     * Convenience method for rendering XML elements
     *
     * @param writer
     *            XML output writer
     * @param nodeName
     *            name of node for element
     * @param value
     *            the value for the element. if null, then nothing is added to
     *            the output buffer
     */
    private void addOptionalElement(HierarchicalStreamWriter writer, String nodeName, String value) {
        if (value != null) {
            writer.startNode(nodeName);
            writer.setValue(value);
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        return null;
    }

}
