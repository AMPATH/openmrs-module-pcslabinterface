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

public class LabMessage {
	private Log log;
	private int labMessageId;
	private String data;
	private User creator;
	private Date dateCreated;
	private String fileSystemUrl;

	public LabMessage() {
		this.log = LogFactory.getLog(super.getClass());
	}

	public int getLabMessageId() {
		return this.labMessageId;
	}

	public void setLabMessageId(int labMessageId) {
		this.labMessageId = labMessageId;
	}

	public String getData() {
		if ((this.data == null) && (this.fileSystemUrl != null)) {
			File file = new File(this.fileSystemUrl);

			if (file.exists())
				try {
					this.data = OpenmrsUtil.getFileAsString(file);
				} catch (IOException io) {
					this.log.warn("Unable to lazy load the data from: "
							+ this.fileSystemUrl, io);
					this.log
							.warn("File system url does not exist for lab message item.  Url: '"
									+ this.fileSystemUrl + "'");
				}
		}
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
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
