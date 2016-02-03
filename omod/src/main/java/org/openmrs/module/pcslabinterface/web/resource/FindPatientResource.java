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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;

/**
 * Provides RESTful access to the patient search facilities of OpenMRS
 */
public class FindPatientResource implements RestResource {

    /**
     * Handle all requests to this patient search resource
     */
    public void handleRequest(Operation operation, OutputType outputType,
                              String restRequest, HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        switch (operation) {

            case GET:
                List<Patient> patientList = Context.getPatientService()
                        .getPatients(restRequest);

                PatientResource.printPatientList(out, outputType, patientList);

                break;

            case POST:
            case PUT:
            case DELETE:
                response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                return;
        }

    }

}