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


import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.web.WebUtil;

import java.text.SimpleDateFormat;

/**
 * Facilitates the encoding of <code>org.openmrs.Patient</org> objects to/from JSON
 */
public class PatientJsonConverter {

    /**
     * All dates are reported in YYYY-MM-DD format
     */
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Convert a patient object into JSON
     *
     * @param patient object to marshal into JSON
     * @return JSON String version of patient
     */
    public static String encode(Patient patient) {
        StringBuffer json = new StringBuffer();

        json.append("{");

        if (patient.getBirthdate() != null) {
            addOptionalElement(json, "birthdate", dateFormatter.format(patient.getBirthdate()));
            json.append("\"birthdateEstimated\":\"").append(patient.getBirthdateEstimated()).append("\",");
        }
        json.append("\"gender\":\"").append(WebUtil.escapeQuotes(patient.getGender())).append("\",");

        json.append("\"identifierList\":[");
        boolean first = true;
        for (PatientIdentifier pid : patient.getActiveIdentifiers()) {
            if (!first)
                json.append(",");
            json.append("{");
            if (pid.isPreferred())
                json.append("\"preferred\":\"1\",");
            json.append("\"type\":\"").append(WebUtil.escapeQuotes(pid.getIdentifierType().getName())).append("\",");
            // TODO: should encode invalid chars in name
            json.append("\"identifier\":\"").append(WebUtil.escapeQuotes(pid.getIdentifier())).append("\"");
            json.append("}");
            first = false;
        }
        json.append("],");

        json.append("\"name\":{");
        PersonName name = patient.getPersonName();
        boolean hasContent = addOptionalElement(json, "prefix", name.getPrefix());
        hasContent |= addOptionalElement(json, "givenName", name.getGivenName());
        hasContent |= addOptionalElement(json, "middleName", name.getMiddleName());
        hasContent |= addOptionalElement(json, "familyName", name.getFamilyName());
        hasContent |= addOptionalElement(json, "familyName2", name.getFamilyName2());
        hasContent |= addOptionalElement(json, "familyNameSuffix", name.getFamilyNameSuffix());
        hasContent |= addOptionalElement(json, "degree", name.getDegree());
        if (hasContent)
            json.deleteCharAt(json.length()-1); // delete last comma if at least something was added
        json.append("},");

        json.append("\"addressList\":[");
        first = true;
        for (PersonAddress address : patient.getAddresses()) {
            if (!first)
                json.append(",");
            json.append("{");
            if (address.getPreferred())
                json.append("\"preferred\":\"1\",");
            hasContent = addOptionalElement(json, "address1", address.getAddress1());
            hasContent |= addOptionalElement(json, "address2", address.getAddress2());
            hasContent |= addOptionalElement(json, "cityVillage", address.getCityVillage());
            hasContent |= addOptionalElement(json, "neighborhoodCell", address.getNeighborhoodCell());
            hasContent |= addOptionalElement(json, "region", address.getRegion());
            hasContent |= addOptionalElement(json, "subregion", address.getSubregion());
            hasContent |= addOptionalElement(json, "countyDistrict", address.getCountyDistrict());
            hasContent |= addOptionalElement(json, "stateProvince", address.getStateProvince());
            hasContent |= addOptionalElement(json, "country", address.getCountry());

            if (hasContent)
                json.deleteCharAt(json.length()-1); // delete last comma if at least something was added

            json.append("}");
            first = false;
        }
        json.append("]");

        json.append("}");

        return json.toString();
    }

    /**
     * Convenience method for rendering JSON elements
     *
     * @param json buffer for output
     * @param attrName name of element
     * @param value the value for the element. if null, then nothing is added to the output buffer
     * @return true if an element was added
     */
    private static boolean addOptionalElement(StringBuffer json, String attrName, String value) {
        if (value == null || "".equals(value))
            return false;

        json.append("\"");
        json.append(attrName);
        json.append("\":\"");
        json.append(WebUtil.escapeQuotes(value));
        json.append("\",");

        return true;
    }

    /**
     * Create patient from JSON representation
     *
     * @param xml JSON-encoded patient
     * @return Patient objected created from details in JSON
     */
    public static Patient decode(String xml) {
        throw new RuntimeException("Patient decoding not yet implemented");
    }

}

