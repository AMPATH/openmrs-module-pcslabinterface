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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

public class PcsLabInterfaceQueue {
	private Log log;
	private int pcsLabInterfaceQueueId;
	private String formData;
	private User creator;
	private Date dateCreated;
	private String fileSystemUrl;

	public PcsLabInterfaceQueue() {
		this.log = LogFactory.getLog(super.getClass());
	}

	public int getPcsLabInterfaceQueueId() {
		return this.pcsLabInterfaceQueueId;
	}

	public void setPcsLabInterfaceQueueId(int pcsLabInterfaceQueueId) {
		this.pcsLabInterfaceQueueId = pcsLabInterfaceQueueId;
	}

	public String getFormData() {
		if ((this.formData == null) && (this.fileSystemUrl != null)) {
			File file = new File(this.fileSystemUrl);

			if (file.exists())
				try {
					this.formData = OpenmrsUtil.getFileAsString(file);
				} catch (IOException io) {
					this.log.warn("Unable to lazy load the formData from: "
							+ this.fileSystemUrl, io);
					this.log
							.warn("File system url does not exist for pcslabinterface queue item.  Url: '"
									+ this.fileSystemUrl + "'");
				}
		}
		return this.formData;
	}

	public void setFormData(String formData) {
		this.formData = formData;
	}

	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getFileSystemUrl() {
		return this.fileSystemUrl;
	}

	public void setFileSystemUrl(String fileSystemUrl) {
		this.fileSystemUrl = fileSystemUrl;
	}
}
