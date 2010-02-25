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
package org.openmrs.module.pcslabinterface.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceQueueProcessor;

public class PcsLabInterfaceQueueProcessorServlet extends HttpServlet {
	private static PcsLabInterfaceQueueProcessor processor;
	private static final long serialVersionUID = -5502982924363644402L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();

		if (!(Context.isAuthenticated())) {
			httpSession.setAttribute("openmrs_error", "auth.session.expired");

			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}

		ServletOutputStream out = response.getOutputStream();
		try {
			getPcsLabInterfaceQueueProcessor().processPcsLabInterfaceQueue();
			out.print("PcsLabInterface queue processor has started");
		} catch (APIException e) {
			out
					.print("PcsLabInterface queue processor failed to start.  Perhaps it is already running?");
		}
	}

	private PcsLabInterfaceQueueProcessor getPcsLabInterfaceQueueProcessor() {
		synchronized (processor) {
			if (processor == null) {
				processor = new PcsLabInterfaceQueueProcessor();
			}
		}
		return processor;
	}
}
