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
import java.util.List;
import java.util.SortedMap;

import org.openmrs.annotation.Authorized;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract interface PcsLabInterfaceService {
	@Transactional(readOnly = true)
	public abstract SortedMap<String, String> getSystemVariables();

	@Authorized(value = { "Add PcsLabInterface Queue", "PcsLabInterface" }, requireAll = true)
	public abstract void createLabMessage(LabMessage paramPcsLabInterfaceQueue);

	@Authorized( { "Delete PcsLabInterface Queue" })
	public abstract void deleteLabMessage(LabMessage paramPcsLabInterfaceQueue);

	@Authorized( { "View PcsLabInterface Queue" })
	public abstract Collection<LabMessage> getLabMessages();

	@Transactional(readOnly = true)
	@Authorized( { "View PcsLabInterface Queue" })
	public abstract LabMessage getNextLabMessage();

	@Transactional(readOnly = true)
	@Authorized( { "View PcsLabInterface Queue" })
	public abstract Integer getLabMessageQueueSize();

	public abstract void garbageCollect();

	/**
	 * Creates a file in
	 * PcsLabInterfaceConstants.PCSLABINTERFACE_GP_QUEUE_ARCHIVE_DIR for the
	 * data in this archive item
	 * 
	 * @param formEntryArchive
	 *            object containing form data to save in the processing archive
	 */
	@Authorized( { PcsLabInterfaceConstants.PRIV_ADD_LAB_MESSAGE_ARCHIVE })
	public void createLabMessageArchive(LabMessageArchive labMessageArchive);

	/**
	 * Get all lab message archive items
	 * 
	 * @return list of lab message archive items
	 */
	@Transactional(readOnly = true)
	@Authorized( { PcsLabInterfaceConstants.PRIV_VIEW_LAB_MESSAGE_ARCHIVE })
	public Collection<LabMessageArchive> getLabMessageArchives();

	/**
	 * Delete the given lab message archive from the system
	 * 
	 * @param LabMessageArchive
	 *            to be deleted
	 */
	@Authorized( { PcsLabInterfaceConstants.PRIV_DELETE_LAB_MESSAGE_ARCHIVE })
	public void deleteLabMessageArchive(LabMessageArchive labMessageArchive);

	/**
	 * Get the number of lab message archive items
	 * 
	 * @return integer number of archive items
	 */
	@Transactional(readOnly = true)
	@Authorized( { PcsLabInterfaceConstants.PRIV_VIEW_LAB_MESSAGE_ERROR })
	public Integer getLabMessageArchiveSize();

	@Transactional(readOnly = true)
	public List<Integer> getNumericConceptIds();

}
