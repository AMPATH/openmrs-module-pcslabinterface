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
package org.openmrs.module.pcslabinterface.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.pcslabinterface.*;
import org.openmrs.util.OpenmrsUtil;

public class PcsLabInterfaceServiceImpl implements PcsLabInterfaceService {
	private Log log = LogFactory.getLog(super.getClass());
	private PcsLabInterfaceDAO dao;

	public void setDao(PcsLabInterfaceDAO dao) {
		this.dao = dao;
	}
	
	public SortedMap<String, String> getSystemVariables() {
		if (!(Context.hasPrivilege("View Administration Functions"))) {
			throw new APIAuthenticationException(
					"Privilege required: View Administration Functions");
		}
		
		TreeMap<String, String> systemVariables = new TreeMap<String, String>();
		systemVariables.put("PCSLABINTERFACE_GP_QUEUE_DIR", PcsLabInterfaceUtil
				.getQueueDir().getAbsolutePath());
		
		systemVariables.put("PCSLABINTERFACE_GP_QUEUE_ARCHIVE_DIR",
				PcsLabInterfaceUtil.getArchiveDir(null).getAbsolutePath());

		return systemVariables;
	}

	public void createLabMessage(LabMessage labMessage)
			throws PcsLabInterfaceException {
		User creator = Context.getAuthenticatedUser();
		if (labMessage.getDateCreated() == null) {
			labMessage.setDateCreated(new Date());
		}
		File queueDir = PcsLabInterfaceUtil.getQueueDir();

		File outFile = OpenmrsUtil.getOutFile(queueDir, labMessage
				.getDateCreated(), creator);

		FileWriter writer = null;
		try {
			writer = new FileWriter(outFile);

			writer.write(labMessage.getData());
		} catch (IOException io) {
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				this.log.debug("Error creating queue item", e);
			}
		}
	}

	public Collection<LabMessage> getLabMessages() {
		List<LabMessage> messages = new Vector<LabMessage>();

		File queueDir = PcsLabInterfaceUtil.getQueueDir();

		if (!(queueDir.exists())) {
			this.log.warn("Unable to open queue directory: " + queueDir);
			return messages;
		}

		for (File file : queueDir.listFiles()) {
			LabMessage queueItem = new LabMessage();
			queueItem.setFileSystemUrl(file.getAbsolutePath());
			queueItem.setDateCreated(new Date(file.lastModified()));
			messages.add(queueItem);
		}

		return messages;
	}

	public void deleteLabMessage(LabMessage labMessage) {

		if ((labMessage == null) || (labMessage.getFileSystemUrl() == null)) {
			throw new PcsLabInterfaceException(
					"Unable to load LabMessage with empty file system url");
		}
		File file = new File(labMessage.getFileSystemUrl());
		log.debug("file path is " + file.getAbsolutePath());
		if (file.exists()) {
			// TODO: move file to an archive, not delete it

			if (!file.delete())
				throw new PcsLabInterfaceException(
						"Unable to delete file from queue: "
								+ file.getAbsolutePath()
								+ " -- check file and folder write permissions");
		}
	}

	/**
	 * grabs the next file in the incoming queue (from the filesystem)
	 * 
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceQueueProcessor#transformNextLabMessage()
	 * @return PcsLabInterfaceQueue the next item in the queue
	 */
	public LabMessage getNextLabMessage() {
		File queueDir = PcsLabInterfaceUtil.getQueueDir();

		File[] arr$ = queueDir.listFiles();
		int len$ = arr$.length;
		int i$ = 0;
		if (i$ < len$) {
			File file = arr$[i$];
			LabMessage queueItem = new LabMessage();
			queueItem.setFileSystemUrl(file.getAbsolutePath());
			queueItem.setDateCreated(new Date(file.lastModified()));
			return queueItem;
		}

		return null;
	}

	public Integer getLabMessageQueueSize() {
		File queueDir = PcsLabInterfaceUtil.getQueueDir();

		return Integer.valueOf(queueDir.list().length);
	}

	/**
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceService#createLabMessageArchive(org.openmrs.module.pcslabinterface.LabMessageArchive)
	 */
	public void createLabMessageArchive(LabMessageArchive LabMessageArchive) {
		User creator = Context.getAuthenticatedUser();

		File queueDir = PcsLabInterfaceUtil.getArchiveDir(LabMessageArchive
				.getDateCreated());

		File outFile = PcsLabInterfaceUtil.getOutFile(queueDir,
				LabMessageArchive.getDateCreated(), creator);

		// write the queue's data to the file
		try {
			PcsLabInterfaceUtil.stringToFile(LabMessageArchive.getData(),
					outFile);
		} catch (IOException io) {
			throw new PcsLabInterfaceException(
					"Unable to save formentry archive", io);
		}

	}

	/**
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceService#getLabMessageArchives()
	 */
	public Collection<LabMessageArchive> getLabMessageArchives() {
		List<LabMessageArchive> archives = new Vector<LabMessageArchive>();

		File archiveDir = PcsLabInterfaceUtil.getArchiveDir(null);

		if (archiveDir.exists() == false) {
			log.warn("Unable to open archive directory: " + archiveDir);
			return archives;
		}

		// loop over all files in archive dir and create lazy archive items
		for (File file : archiveDir.listFiles()) {
			LabMessageArchive queueItem = new LabMessageArchive();
			queueItem.setFileSystemUrl(file.getAbsolutePath());
			queueItem.setDateCreated(new Date(file.lastModified()));
			archives.add(queueItem);
		}

		return archives;
	}

	/**
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceService#deleteLabMessageArchive(org.openmrs.module.pcslabinterface.LabMessageArchive)
	 */
	public void deleteLabMessageArchive(LabMessageArchive LabMessageArchive) {
		if (LabMessageArchive == null
				|| LabMessageArchive.getFileSystemUrl() == null)
			throw new PcsLabInterfaceException(
					"Unable to load LabMessageArchive with empty file system url");

		File file = new File(LabMessageArchive.getFileSystemUrl());

		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceService#getLabMessageArchiveSize()
	 */
	public Integer getLabMessageArchiveSize() {
		File archiveDir = PcsLabInterfaceUtil.getArchiveDir(null);

		return archiveDir.list().length;
	}

	public void garbageCollect() {
		System.gc();
	}

	/**
	 * Get a list of all valid numeric concept ids
	 * 
	 * @return 
	 */
	public List<Integer> getNumericConceptIds() {
		return dao.getNumericConceptIds();
	}

}
