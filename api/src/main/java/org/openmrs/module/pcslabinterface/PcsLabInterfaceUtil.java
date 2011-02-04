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
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class PcsLabInterfaceUtil {
	private static Log log = LogFactory.getLog(PcsLabInterfaceUtil.class);

	/**
	 * Cached directory where queue items are stored
	 * 
	 * @see #getQueueDir()
	 */
	private static File queueDir = null;

	/**
	 * Cached directory where gp says archive items are stored
	 * 
	 * @see #getArchiveDir()
	 */
	private static String archiveFileName = null;

	/**
	 * Gets the directory where the user specified their queues were being
	 * stored
	 * 
	 * @return directory in which to find queued items
	 */
	public static File getQueueDir() {
		if (queueDir == null) {
			AdministrationService as = Context.getAdministrationService();
			String folderName = as.getGlobalProperty(
					"pcslabinterface.queue_dir", "pcslabinterface/queue");
			queueDir = OpenmrsUtil
					.getDirectoryInApplicationDataDirectory(folderName);
			if (log.isDebugEnabled()) {
				log
						.debug("Loaded pcslabinterface queue directory from global properties: "
								+ queueDir.getAbsolutePath());
			}
		}
		return queueDir;
	}

	/**
	 * Gets the directory where the user specified their archives were being
	 * stored NOTE: stolen from FormEntry
	 * 
	 * @param optional
	 *            Date to specify the folder this should possibly be sorted into
	 * @return directory in which to store archived items
	 */
	public static File getArchiveDir(Date d) {
		// cache the global property location so we don't have to hit the db
		// everytime
		if (archiveFileName == null) {
			AdministrationService as = Context.getAdministrationService();
			archiveFileName = as
					.getGlobalProperty(
							PcsLabInterfaceConstants.PCSLABINTERFACE_GP_QUEUE_ARCHIVE_DIR,
							PcsLabInterfaceConstants.PCSLABINTERFACE_GP_QUEUE_ARCHIVE_DIR_DEFAULT);
		}

		// replace %Y %M %D in the folderName with the date
		String folderName = replaceVariables(archiveFileName, d);

		// get the file object for this potentially new file
		File incomingArchiveDir = OpenmrsUtil
				.getDirectoryInApplicationDataDirectory(folderName);

		if (log.isDebugEnabled())
			log
					.debug("Loaded formentry archive directory from global properties: "
							+ incomingArchiveDir.getAbsolutePath());

		return incomingArchiveDir;
	}

	public static String replaceVariables(String str, Date d) {
		Calendar calendar = Calendar.getInstance();
		if (d != null) {
			calendar.setTime(d);
		}
		int year = calendar.get(1);
		str = str.replace("%Y", Integer.toString(year));

		int month = calendar.get(2) + 1;
		String monthString = Integer.toString(month);
		if (month < 10)
			monthString = "0" + monthString;
		str = str.replace("%M", monthString);

		int day = calendar.get(5);
		String dayString = Integer.toString(day);
		if (day < 10)
			dayString = "0" + dayString;
		str = str.replace("%D", dayString);

		int week = calendar.get(3);
		String weekString = Integer.toString(week);
		if (week < 10)
			weekString = "0" + week;
		str = str.replace("%w", weekString);

		int weekmonth = calendar.get(4);
		String weekmonthString = Integer.toString(weekmonth);
		if (weekmonth < 10)
			weekmonthString = "0" + weekmonthString;
		str = str.replace("%W", weekmonthString);

		return str;
	}

	/**
	 * Gets an out File object. If date is not provided, the current timestamp
	 * is used. If user is not provided, the user id is not put into the
	 * filename. Assumes dir is already created
	 * 
	 * @param dir
	 *            directory to make the random filename in
	 * @param date
	 *            optional Date object used for the name
	 * @param user
	 *            optional User creating this file object
	 * @return file new file that is able to be written to
	 */
	public static File getOutFile(File dir, Date date, User user) {

		File outFile;
		do {
			// format to print date in filename
			DateFormat dateFormat = new SimpleDateFormat(
					"yyyy.MM.dd-HHmm-ssSSS");

			// use current date if none provided
			if (date == null)
				date = new Date();

			StringBuilder filename = new StringBuilder();

			// the start of the filename is the time so we can do some sorting
			filename.append(dateFormat.format(date));

			// insert the user id if they provided it
			if (user != null) {
				filename.append("-");
				filename.append(user.getUserId());
				filename.append("-");
			}

			// the end of the filename is a random number between 0 and 10000
			filename.append((int) (Math.random() * 10000));
			filename.append(".txt");

			outFile = new File(dir, filename.toString());

			// set to null to avoid very minimal possibility of an infinite loop
			date = null;

		} while (outFile.exists());

		return outFile;
	}

	public static void stringToFile(String fileContents, File outFile)
			throws IOException {
		FileWriter writer = new FileWriter(outFile);

		writer.write(fileContents);

		writer.close();
	}
}
