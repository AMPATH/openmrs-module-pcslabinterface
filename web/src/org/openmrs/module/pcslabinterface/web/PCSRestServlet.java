package org.openmrs.module.pcslabinterface.web;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;
import org.openmrs.module.restmodule.RestUtil;
import org.openmrs.module.restmodule.web.FindPatientResource;
import org.openmrs.module.restmodule.web.HL7MessageResource;
import org.openmrs.module.restmodule.web.ObsResource;
import org.openmrs.module.restmodule.web.PatientResource;
import org.openmrs.module.restmodule.web.RestResource;
import org.openmrs.module.restmodule.web.RestResource.Operation;
import org.openmrs.module.restmodule.web.RestResource.OutputType;

/**
 * This is a direct copy of the REST Module's RestServlet
 * 
 * TODO break out REST Module's handleRequest method to be overridden by
 * subclasses
 */
public class PCSRestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * Name for servlet within the servlet mapping (follows "/servletModule/" in
	 * URL)
	 */
	public static final String SERVLET_NAME_API = "pcsapi";
	public static final String SERVLET_NAME_JSON = "pcsjson";
	public static final double OBS_LIST_XML_VERSION = 1.0;
	public static final double PATIENT_LIST_XML_VERSION = 1.0;
	/**
	 * Internally held list of resources. Currently hardcoded.
	 */
	private static Hashtable<String, RestResource> resources = new Hashtable<String, RestResource>();
	static {
		resources.put("patient", new PatientResource());
		resources.put("findPatient", new FindPatientResource());
		resources.put("obs", new ObsResource());
		resources.put("hl7", new HL7MessageResource());
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleRequest(Operation.GET, request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleRequest(Operation.POST, request, response);
	}

	@Override
	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleRequest(Operation.PUT, request, response);
	}

	@Override
	protected void doDelete(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleRequest(Operation.DELETE, request, response);
	}

	/**
	 * Handle all requests to the API -- i.e., authenticate to the API, restrict
	 * access based on client's IP address, parse the request URL, and pass
	 * control to the appropriate resource.
	 * 
	 * @param operation
	 *            HTTP operation being performed (e.g., GET, POST, PUT, DELETE)
	 * @param request
	 *            HTTP request
	 * @param response
	 *            HTTP response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleRequest(Operation operation, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

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
