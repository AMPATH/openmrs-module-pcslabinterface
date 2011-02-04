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

public class LabMessageArchive {
	private Log log = LogFactory.getLog(this.getClass());

	private Integer labMessageId;
	private String data;
	private User creator;
	private Date dateCreated;
	private String fileSystemUrl;

	/**
	 * Default constructor
	 */
	public LabMessageArchive() {
	}

	/**
	 * create an archive from an existing message
	 * 
	 * @param labMessage
	 */
	public LabMessageArchive(LabMessage labMessage) {
		setData(labMessage.getData());
		setCreator(labMessage.getCreator());
	}

	/**
	 * Gets the lab message that this archive item holds. If data is null and
	 * fileSystemUrl is not null, the data is "lazy loaded" from the filesystem
	 * 
	 * @return the data
	 */
	public String getData() {

		if (data == null && fileSystemUrl != null) {
			// lazy load the form data from the filesystem

			File file = new File(fileSystemUrl);

			if (file.exists()) {
				try {
					data = OpenmrsUtil.getFileAsString(file);
					return data;
				} catch (IOException io) {
					log.warn("Unable to lazy load the data from: "
							+ fileSystemUrl, io);
				}
			} else {
				log
						.warn("File system url does not exist for lab message archive item.  Url: '"
								+ fileSystemUrl + "'");
			}

		}

		return data;
	}

	/**
	 * @param formData
	 *            The formData to set.
	 */
	public void setData(String data) {
		this.data = data;
	}

	/**
	 * @return the labMessageId
	 */
	public Integer getLabMessageId() {
		return labMessageId;
	}

	/**
	 * @param labMessageId
	 *            the labMessageId to set
	 */
	public void setLabMessageId(Integer labMessageId) {
		this.labMessageId = labMessageId;
	}

	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the fileSystemUrl
	 */
	public String getFileSystemUrl() {
		return fileSystemUrl;
	}

	/**
	 * @param fileSystemUrl
	 *            the fileSystemUrl to set
	 */
	public void setFileSystemUrl(String fileSystemUrl) {
		this.fileSystemUrl = fileSystemUrl;
	}

}
