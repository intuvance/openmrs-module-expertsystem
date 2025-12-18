/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem.web.websocket;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.expertsystem.ExpertsystemConstants;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@Slf4j
public class ExpertsystemSocket {
	
	@OnOpen
	public void onOpen(Session session) {
		log.info(ExpertsystemConstants.GREEN + "WebSocket OPENED: {}" + ExpertsystemConstants.RESET, session.getId());
	}
	
	@OnMessage
	public String onMessage(String message, Session session) {
		log.info(ExpertsystemConstants.MAGENTA + "WebSocket RECEIVED: {} from {}" + ExpertsystemConstants.RESET, message,
		    session.getId());
		return message;
	}
	
	@OnClose
	public void onClose(Session session, CloseReason reason) {
		log.info(ExpertsystemConstants.GOLD + "WebSocket CLOSED: {} ({})" + ExpertsystemConstants.RESET, session.getId(),
		    reason);
	}
	
	@OnError
	public void onError(Session session, Throwable error) {
		log.error(ExpertsystemConstants.RED + "WebSocket ERROR: {}" + ExpertsystemConstants.RESET,
		    session != null ? session.getId() : "n/a", error);
	}
}
