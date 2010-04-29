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

//import java.io.StringWriter;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.transform.Result;
//import javax.xml.transform.Source;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerConfigurationException;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.stream.StreamResult;
//import javax.xml.transform.stream.StreamSource;
//import javax.xml.xpath.XPath;
//import javax.xml.xpath.XPathFactory;
//import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
//import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.springframework.transaction.annotation.Transactional;
//import org.w3c.dom.Document;

@Transactional
public class PcsLabInterfaceQueueProcessor {
	private static final Log log = LogFactory
			.getLog(PcsLabInterfaceQueueProcessor.class);
//	private DocumentBuilderFactory documentBuilderFactory;
//	private XPathFactory xPathFactory;
//	private TransformerFactory transformerFactory;
	private static Boolean isRunning = Boolean.valueOf(false);

	/**
	 * processes a given queue into the HL7 in queue
	 * 
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceQueueProcessor#transformNextPcsLabInterfaceQueue()
	 * @param pcsLabInterfaceQueue
	 */
	public void parsePcsLabInterfaceQueue(PcsLabInterfaceQueue pcsLabInterfaceQueue) {
		log.debug("Transforming PcsLabInterface queue");

		String hl7SourceKey = String.valueOf(pcsLabInterfaceQueue
					.getPcsLabInterfaceQueueId());

		// build the HL7 message
		HL7InQueue hl7InQueue = new HL7InQueue();
		hl7InQueue.setHL7Data(pcsLabInterfaceQueue.getFormData());
		// TODO: do something better than this for choosing HL7Source
		hl7InQueue.setHL7Source(Context.getHL7Service().getHL7Source(
				Integer.valueOf(1)));
		hl7InQueue.setHL7SourceKey(hl7SourceKey);
		
		// save the HL7 message
		Context.getHL7Service().saveHL7InQueue(hl7InQueue);

		// archive the queue
		PcsLabInterfaceService pcsService = 
			(PcsLabInterfaceService) Context.getService(PcsLabInterfaceService.class);
		
		pcsService.deletePcsLabInterfaceQueue(pcsLabInterfaceQueue);

		pcsService.garbageCollect();
	}

//	public void transformPcsLabInterfaceQueue(
//			PcsLabInterfaceQueue pcsLabInterfaceQueue) {
//		log.debug("Transforming PcsLabInterface queue");
//		String formData = pcsLabInterfaceQueue.getFormData();
//		FormService formService = Context.getFormService();
//		Integer formId = null;
//		String hl7SourceKey = null;
//		String errorDetails = null;
//		try {
//			DocumentBuilderFactory dbf = getDocumentBuilderFactory();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			XPathFactory xpf = getXPathFactory();
//			XPath xp = xpf.newXPath();
//			Document doc = db.parse(IOUtils.toInputStream(formData));
//			formId = Integer.valueOf(Integer.parseInt(xp.evaluate("/form/@id",
//					doc)));
//			hl7SourceKey = xp.evaluate("/form/header/uid", doc);
//		} catch (Exception e) {
//			errorDetails = e.getMessage();
//			log
//					.error("Error while parsing pcslabinterface ("
//							+ pcsLabInterfaceQueue.getPcsLabInterfaceQueueId()
//							+ ")", e);
//		}
//
//		if (formId == null) {
//			setFatalError(pcsLabInterfaceQueue,
//					"Error retrieving form ID from data", errorDetails);
//
//			return;
//		}
//
//		if ((hl7SourceKey == null) || (hl7SourceKey.length() < 1)) {
//			hl7SourceKey = String.valueOf(pcsLabInterfaceQueue
//					.getPcsLabInterfaceQueueId());
//		}
//
//		String xsltDoc = formService.getForm(formId).getXslt();
//
//		StringWriter outWriter = new StringWriter();
//		Source source = new StreamSource(IOUtils.toInputStream(formData));
//		Source xslt = new StreamSource(IOUtils.toInputStream(xsltDoc));
//		Result result = new StreamResult(outWriter);
//
//		TransformerFactory tf = getTransformerFactory();
//		String out = null;
//		errorDetails = null;
//		try {
//			Transformer t = tf.newTransformer(xslt);
//			t.transform(source, result);
//			out = outWriter.toString();
//		} catch (TransformerConfigurationException e) {
//			errorDetails = e.getMessage();
//			log.error(errorDetails, e);
//		} catch (TransformerException e) {
//			errorDetails = e.getMessage();
//			log.error(errorDetails, e);
//		}
//
//		if (out == null) {
//			setFatalError(pcsLabInterfaceQueue, "Unable to transform to HL7",
//					errorDetails);
//
//			return;
//		}
//
//		HL7InQueue hl7InQueue = new HL7InQueue();
//		hl7InQueue.setHL7Data(out.toString());
//		hl7InQueue.setHL7Source(Context.getHL7Service().getHL7Source(
//				Integer.valueOf(1)));
//		hl7InQueue.setHL7SourceKey(hl7SourceKey);
//		Context.getHL7Service().createHL7InQueue(hl7InQueue);
//
//		PcsLabInterfaceService pcsLabInterfaceService = (PcsLabInterfaceService) Context
//				.getService(PcsLabInterfaceService.class);
//		pcsLabInterfaceService.deletePcsLabInterfaceQueue(pcsLabInterfaceQueue);
//
//		pcsLabInterfaceService.garbageCollect();
//	}

	/**
	 * picks the next queue and transforms it
	 * 
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceQueueProcessor#processPcsLabInterfaceQueue()
	 * @return
	 */
	public boolean transformNextPcsLabInterfaceQueue() {
		boolean transformOccurred = false;
		PcsLabInterfaceService pcsService = null;
		try {
			pcsService = (PcsLabInterfaceService) Context
					.getService(PcsLabInterfaceService.class);
		} catch (APIException e) {
			log.debug("PcsLabInterfaceService not found");
			return false;
		}
		PcsLabInterfaceQueue pcsQueue;
		if ((pcsQueue = pcsService.getNextPcsLabInterfaceQueue()) != null) {
			parsePcsLabInterfaceQueue(pcsQueue);
			transformOccurred = true;
		}
		return transformOccurred;
	}

//	private DocumentBuilderFactory getDocumentBuilderFactory() {
//		if (this.documentBuilderFactory == null)
//			this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
//		return this.documentBuilderFactory;
//	}
//
//	private XPathFactory getXPathFactory() {
//		if (this.xPathFactory == null)
//			this.xPathFactory = XPathFactory.newInstance();
//		return this.xPathFactory;
//	}
//
//	private TransformerFactory getTransformerFactory() {
//		if (this.transformerFactory == null) {
//			System.setProperty("javax.xml.transform.TransformerFactory",
//					"net.sf.saxon.TransformerFactoryImpl");
//
//			this.transformerFactory = TransformerFactory.newInstance();
//		}
//		return this.transformerFactory;
//	}
//
//	private void setFatalError(PcsLabInterfaceQueue pcsLabInterfaceQueue,
//			String error, String errorDetails) {
//		PcsLabInterfaceService pcsLabInterfaceService = (PcsLabInterfaceService) Context
//				.getService(PcsLabInterfaceService.class);
//		pcsLabInterfaceService.deletePcsLabInterfaceQueue(pcsLabInterfaceQueue);
//	}

	/**
	 * iterates over queue contents for transformation
	 *
	 * @see org.openmrs.module.pcslabinterface.PcsLabInterfaceQueueTask#execute()
	 * @throws APIException
	 */
	public void processPcsLabInterfaceQueue() throws APIException {
		synchronized (isRunning) {
			if (isRunning.booleanValue()) {
				log.warn("PcsLabInterfaceQueue processor aborting (another processor already running)");
				return;
			}
			isRunning = Boolean.valueOf(true);
		}
		try {
			log.debug("Start processing PcsLabInterface queue");
			log.debug("PcsLabInterface processor hash: " + super.hashCode());
			while (transformNextPcsLabInterfaceQueue())
				;
			log.debug("Done processing PcsLabInterface queue");
		} finally {
			isRunning = Boolean.valueOf(false);
		}
	}
}
