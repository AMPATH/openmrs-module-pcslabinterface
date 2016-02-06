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
package org.openmrs.module.pcslabinterface.web.resource;

import com.thoughtworks.xstream.XStream;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.pcslabinterface.web.PCSRestServlet;
import org.openmrs.module.pcslabinterface.web.RestUtil;
import org.openmrs.module.pcslabinterface.web.converter.PatientJsonConverter;
import org.openmrs.module.pcslabinterface.web.converter.PatientXmlConverter;
import org.openmrs.web.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;

/**
 * Provides a simple RESTful access to patients
 */
public class PatientResource implements RestResource {

    /**
     * Handle all requests to this resource
     */
    public void handleRequest(Operation operation, OutputType outputType,
                              String restRequest, HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        switch (operation) {

            case GET:
                Collection<Patient> patientList = Context.getPatientService()
                        .getPatients(null, restRequest, null, false);

                printPatientList(out, outputType, patientList);

                break;

            case POST:
            case PUT:
            case DELETE:
                response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                return;
        }

    }

    /**
     * Auto generated method comment
     *
     * @param out
     * @param outputType
     * @param patientList
     */
    public static void printPatientList(PrintWriter out, OutputType outputType,
                                        Collection<Patient> patientList) {
        if (outputType == OutputType.XML) {
            out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.print("<patientList version=\""
                    + PCSRestServlet.PATIENT_LIST_XML_VERSION + "\">");
            int i = 0;
            int max = RestUtil.getMaxResults();
            for (Patient patient : patientList) {
                out.print(xmlEncodePatient(patient));
                i++;
                if (max > 0 && i >= max)
                    break; // if max set, abort before exceeding
            }
            out.print("</patientList>");
        } else if (outputType == OutputType.JSON) {
            out.print("[");
            int i = 0;
            int max = RestUtil.getMaxResults();
            for (Patient patient : patientList) {
                if (i != 0)
                    out.print(",");
                out.print(jsonEncodePatient(patient));
                i++;
                if (max > 0 && i >= max)
                    break; // if max set, abort before exceeding
            }
            out.print("]");
        }
    }

    /**
     * Convert a patient object into XML
     *
     * @param patient object to marshal into XML
     * @return XML version of patient
     */
    private static String xmlEncodePatient(Patient patient) {
        XStream xstream = new XStream();
        xstream.registerConverter(new PatientXmlConverter());
        xstream.alias("patient", Patient.class);
        return xstream.toXML(patient);
    }

    /**
     * Convert a patient object into JSON
     *
     * @param patient object to marshal into JSON
     * @return JSON String version of patient
     */
    private static String jsonEncodePatient(Patient patient) {
        return PatientJsonConverter.encode(patient);
    }
}
