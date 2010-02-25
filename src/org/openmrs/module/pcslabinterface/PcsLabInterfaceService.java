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
package org.openmrs.module.pcslabinterface;

import java.util.Collection;
import java.util.SortedMap;
import org.openmrs.annotation.Authorized;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract interface PcsLabInterfaceService {
	@Transactional(readOnly = true)
	public abstract SortedMap<String, String> getSystemVariables();

	@Authorized(value = { "Add PcsLabInterface Queue", "PcsLabInterface" }, requireAll = true)
	public abstract void createPcsLabInterfaceQueue(
			PcsLabInterfaceQueue paramPcsLabInterfaceQueue);

	@Authorized( { "Delete PcsLabInterface Queue" })
	public abstract void deletePcsLabInterfaceQueue(
			PcsLabInterfaceQueue paramPcsLabInterfaceQueue);

	@Authorized( { "View PcsLabInterface Queue" })
	public abstract Collection<PcsLabInterfaceQueue> getPcsLabInterfaceQueues();

	@Transactional(readOnly = true)
	@Authorized( { "View PcsLabInterface Queue" })
	public abstract PcsLabInterfaceQueue getNextPcsLabInterfaceQueue();

	@Transactional(readOnly = true)
	@Authorized( { "View PcsLabInterface Queue" })
	public abstract Integer getPcsLabInterfaceQueueSize();

	public abstract void garbageCollect();
}
