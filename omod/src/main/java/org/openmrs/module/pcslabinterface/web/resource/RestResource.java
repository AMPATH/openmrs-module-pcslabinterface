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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Represents a RESTful resource (e.g., patient) that can be accessed through
 * RESTful calls
 */
public interface RestResource {

    // Rest Operations
    public enum Operation {GET, POST, PUT, DELETE};

    // Rest output content type options
    public enum OutputType {XML, JSON};

    /**
     * Used to pass request to the given resource
     *
     * @param operation
     *            REST operation (e.g., GET, POST, PUT, DELETE)
     * @param outputType type of output (e.g. xml, json)
     * @param restRequest
     *            fragment of request URL beyond resource reference
     * @param request
     *            HTTP request
     * @param response
     *            HTTP response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void handleRequest(Operation operation, OutputType outputType, String restRequest,
                              HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

}

