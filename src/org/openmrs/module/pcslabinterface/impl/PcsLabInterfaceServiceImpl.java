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
import org.openmrs.module.pcslabinterface.PcsLabInterfaceException;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceQueue;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceService;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceUtil;
import org.openmrs.util.OpenmrsUtil;

public class PcsLabInterfaceServiceImpl implements PcsLabInterfaceService {
	private Log log = LogFactory.getLog(super.getClass());

	public SortedMap<String, String> getSystemVariables() {
		if (!(Context.hasPrivilege("View Administration Functions"))) {
			throw new APIAuthenticationException(
					"Privilege required: View Administration Functions");
		}
		TreeMap<String, String> systemVariables = new TreeMap<String, String>();
		systemVariables.put("PCSLABINTERFACE_GP_QUEUE_DIR", PcsLabInterfaceUtil
				.getQueueDir().getAbsolutePath());
		systemVariables.put("PCSLABINTERFACE_GP_QUEUE_ARCHIVE_DIR", PcsLabInterfaceUtil
				.getArchiveDir(null).getAbsolutePath());

		return systemVariables;
	}

	public void createPcsLabInterfaceQueue(
			PcsLabInterfaceQueue pcsLabInterfaceQueue)
			throws PcsLabInterfaceException {
		User creator = Context.getAuthenticatedUser();
		if (pcsLabInterfaceQueue.getDateCreated() == null) {
			pcsLabInterfaceQueue.setDateCreated(new Date());
		}
		File queueDir = PcsLabInterfaceUtil.getQueueDir();

		File outFile = OpenmrsUtil.getOutFile(queueDir, pcsLabInterfaceQueue
				.getDateCreated(), creator);

		FileWriter writer = null;
		try {
			writer = new FileWriter(outFile);

			writer.write(pcsLabInterfaceQueue.getFormData());
		} catch (IOException io) {
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				this.log.debug("Error creating queue item", e);
			}
		}
	}

	public Collection<PcsLabInterfaceQueue> getPcsLabInterfaceQueues() {
		List<PcsLabInterfaceQueue> queues = new Vector<PcsLabInterfaceQueue>();

		File queueDir = PcsLabInterfaceUtil.getQueueDir();

		if (!(queueDir.exists())) {
			this.log.warn("Unable to open queue directory: " + queueDir);
			return queues;
		}

		for (File file : queueDir.listFiles()) {
			PcsLabInterfaceQueue queueItem = new PcsLabInterfaceQueue();
			queueItem.setFileSystemUrl(file.getAbsolutePath());
			queueItem.setDateCreated(new Date(file.lastModified()));
			queues.add(queueItem);
		}

		return queues;
	}

	public void deletePcsLabInterfaceQueue(
			PcsLabInterfaceQueue pcsLabInterfaceQueue) {

		if ((pcsLabInterfaceQueue == null)
				|| (pcsLabInterfaceQueue.getFileSystemUrl() == null)) {
			throw new PcsLabInterfaceException(
					"Unable to load pcsLabInterfaceQueue with empty file system url");
		}
		File file = new File(pcsLabInterfaceQueue.getFileSystemUrl());
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

	public PcsLabInterfaceQueue getNextPcsLabInterfaceQueue() {
		File queueDir = PcsLabInterfaceUtil.getQueueDir();

		File[] arr$ = queueDir.listFiles();
		int len$ = arr$.length;
		int i$ = 0;
		if (i$ < len$) {
			File file = arr$[i$];
			PcsLabInterfaceQueue queueItem = new PcsLabInterfaceQueue();
			queueItem.setFileSystemUrl(file.getAbsolutePath());
			queueItem.setDateCreated(new Date(file.lastModified()));
			return queueItem;
		}

		return null;
	}

	public Integer getPcsLabInterfaceQueueSize() {
		File queueDir = PcsLabInterfaceUtil.getQueueDir();

		return Integer.valueOf(queueDir.list().length);
	}

	public void garbageCollect() {
		System.gc();
	}
}
