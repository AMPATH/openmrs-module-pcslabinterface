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

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for miscellaneous convenience functions used by the REST API
 */
public class RestUtil {
	/**
	 * Name of global property containing list of valid client IP addresses
	 */
	public final static String GP_ALLOWED_IP_LIST = PcsLabInterfaceConstants.MODULE_ID + ".rest_allowed_ip_list";

	/**
	 * Maximum number of results to return for any single call (0 for unlimited)
	 */
	public final static String GP_MAX_RESULTS = PcsLabInterfaceConstants.MODULE_ID + ".max_patient_results";

	/**
	 * Length of time (in milliseconds) before refreshing global properties
	 */
	public final static long PROPERTY_REFRESH_INTERVAL = 600000;
	
	/**
	 * Privilege required to access any REST API calls
	 */
	public static final String REST_API_PRIVILEGE = "Access REST API";

	private static final Log log = LogFactory.getLog(RestUtil.class);
	
	private static Object lock = new Object(); // used for synchronizing
	private static List<String[]> allowedIpList = null;
	private static long propertiesUpdatedTime = 0;
	private static int maxResults = 0;

	/**
	 * Validate credentials
	 * 
	 * @param auth
	 *            BASIC Authentication credentials -- i.e., "username:password"
	 *            encoded with Base64
	 * @return <code>true</code> if credentials are valid for the API
	 */
	public static boolean allowUser(String auth) throws Base64DecodingException{
		if (auth == null)
			return false; // only BASIC authentication

		if (!auth.toUpperCase().startsWith("BASIC "))
			return false; // only BASIC authentication

		// Get encoded user and password following "BASIC "
		String userpassEncoded = auth.substring(6);

		String[] userpassDecoded = new String(Base64.decode(userpassEncoded))
				.split(":");
		//user left both username and password fields empty or just password field
		//This does not take care of the situation where only username field is empty
		if(userpassDecoded.length < 2)
			return false;
		
		String username = userpassDecoded[0];
		String password = userpassDecoded[1];
		
		try {
			Context.authenticate(username, password);
		} catch (ContextAuthenticationException e) {
			log.error("could not authenticate user " + username);
		}
		return Context.isAuthenticated() && Context.hasPrivilege(REST_API_PRIVILEGE);
	}

	/**
	 * Tests whether or not a client's IP address is allowed to have access to
	 * the REST API (based on a global property)
	 * 
	 * @param remoteAddress
	 *            address of the remote client
	 * @return <code>true</code> if client should be allowed access
	 */
	public static boolean allowRemoteAddress(String remoteAddress) {
		String[] remoteIp = remoteAddress.split("\\.");
		List<String[]> allowed = getAllowedIpList();
		for (String[] addr : allowed) {
			boolean match = true;
			for (int i = 0; i < addr.length; i++) {
				if (!addr[i].equals(remoteIp[i]) && !addr[i].equals("*")) {
					match = false;
					break;
				}
			}
			if (match)
				return true;
		}
		return false;
	}

	/**
	 * Return the maximum number of results that should be returned by any call
	 * 
	 * @return maximum number of results to return (0 for unlimited)
	 */
	public static int getMaxResults() {
		if (globalPropertiesDirty())
			updateGlobalProperties();
		return maxResults;
	}

	/**
	 * Fetch the list of allowed IP addresses
	 * 
	 * @return allowed IP addresses
	 */
	private static List<String[]> getAllowedIpList() {
		if (globalPropertiesDirty())
			updateGlobalProperties();
		return allowedIpList;
	}

	/**
	 * Check if global properties have either not been loaded or have grown
	 * stale
	 * 
	 * @return <code>true</code> if global properties need to be fetched from
	 *         the database
	 */
	private static boolean globalPropertiesDirty() {
		boolean dirty = false;
		synchronized (lock) {
			dirty = (allowedIpList == null || (new Date().getTime() - propertiesUpdatedTime) > PROPERTY_REFRESH_INTERVAL);
		}
		return dirty;
	}

	/**
	 * Update global property settings from the database
	 */
	public static void updateGlobalProperties() {
		synchronized (lock) {
			// Update allowed IP list
			String allowedIpListProperty = Context.getAdministrationService()
					.getGlobalProperty(GP_ALLOWED_IP_LIST, "");
			String[] propList = allowedIpListProperty.split("[\\s,]+");
			allowedIpList = new ArrayList<String[]>();
			for (String allowedAddress : propList) {
				allowedIpList.add(allowedAddress.split("\\."));
			}
			// Update max results
			String maxResultProperty = Context.getAdministrationService()
					.getGlobalProperty(GP_MAX_RESULTS, "0");
			try {
				maxResults = Integer.parseInt(maxResultProperty);
			} catch (NumberFormatException e) {
				maxResults = 0;
			}
			propertiesUpdatedTime = new Date().getTime();
		}
	}

}
