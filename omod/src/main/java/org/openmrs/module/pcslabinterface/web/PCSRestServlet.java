package org.openmrs.module.pcslabinterface.web;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;
import org.openmrs.module.pcslabinterface.web.resource.FindPatientResource;
import org.openmrs.module.pcslabinterface.web.resource.PatientResource;
import org.openmrs.module.pcslabinterface.web.resource.RestResource;
import org.openmrs.module.pcslabinterface.web.resource.RestResource.Operation;
import org.openmrs.module.pcslabinterface.web.resource.RestResource.OutputType;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Hashtable;

public class PCSRestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(PCSRestServlet.class);
    private static final String base64ErrorMessage = "Could not base64 decode input password ";

	/**
	 * Name for servlet within the servlet mapping (follows "/servletModule/" in
	 * URL)
	 */
	public static final String SERVLET_NAME_API = "pcsapi";
	public static final String SERVLET_NAME_JSON = "pcsjson";
	public static final double PATIENT_LIST_XML_VERSION = 1.0;
	/**
	 * Internally held list of resources. Currently hardcoded.
	 */
	private static Hashtable<String, RestResource> resources = new Hashtable<String, RestResource>();
	static {
		resources.put("patient", new PatientResource());
		resources.put("findPatient", new FindPatientResource());
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
        try {
            handleRequest(Operation.GET, request, response);
        } catch (Base64DecodingException e) {
            log.error(base64ErrorMessage + e.getMessage());
        }
    }

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
        try {
            handleRequest(Operation.POST, request, response);
        } catch (Base64DecodingException e) {
            log.error(base64ErrorMessage + e.getMessage());
        }
    }

	@Override
	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
        try {
            handleRequest(Operation.PUT, request, response);
        } catch (Base64DecodingException e) {
            log.error(base64ErrorMessage + e.getMessage());
        }
    }

	@Override
	protected void doDelete(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
        try {
            handleRequest(Operation.DELETE, request, response);
        } catch (Base64DecodingException e) {
            log.error(base64ErrorMessage + e.getMessage());
        }
    }

	/**
	 * Handle all requests to the API -- i.e., authenticate to the API, restrict
	 * access based on client's IP address, parse the request URL, and pass
	 * control to the appropriate resource.
	 * 
	 * @param operation HTTP operation being performed (e.g., GET, POST, PUT, DELETE)
	 * @param request HTTP request
	 * @param response HTTP response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleRequest(Operation operation, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Base64DecodingException {

		// Implement BASIC Authentication and restrict by client IP address
		String auth = request.getHeader("Authorization");
		String remoteAddress = request.getRemoteAddr();
		if (!RestUtil.allowUser(auth)
				|| !RestUtil.allowRemoteAddress(remoteAddress)) {
			// Not allowed
			response.setHeader("WWW-Authenticate",
					"BASIC realm=\"OpenMRS Rest API\"");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		// Parse the request URL, removing the rest module name along with the
		// mapping to this servlet
		String url = request.getRequestURI();
		int pos = url.indexOf("/" + PcsLabInterfaceConstants.MODULE_ID + "/")
				+ PcsLabInterfaceConstants.MODULE_ID.length() + 2;
		String target = url.substring(pos);

		OutputType outputType = null;

		// Remove servlet name from URL (gets us to this servlet)
		if (target.startsWith(SERVLET_NAME_API + "/")) {
			target = target.substring(SERVLET_NAME_API.length() + 1);
			outputType = OutputType.XML;
			response.setHeader("Content-type", "text/xml");
		} else if (target.startsWith(SERVLET_NAME_JSON + "/")) {
			target = target.substring(SERVLET_NAME_JSON.length() + 1);
			outputType = OutputType.JSON;
			response.setHeader("Content-type", "application/json");
		}

		// If we have a matching resource, let it handle the request
		for (String resourceName : resources.keySet()) {
			if (target.startsWith(resourceName + "/")
					|| target.startsWith(resourceName)) {

				String restRequest = URLDecoder.decode(
						target.substring(resourceName.length()
								+ (target.startsWith(resourceName + "/") ? 1
										: 0)), "UTF-8");
				try {
					resources.get(resourceName).handleRequest(operation,
							outputType, restRequest, request, response);

				} catch (APIAuthenticationException e) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
				}
				return;
			}
		}

		// If no matching resources were found, return an error
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);

	}

}
