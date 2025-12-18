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

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@Slf4j
public class ExpertsystemSocket {
	
	@OnOpen
	public void onOpen(Session session) {
		log.info("WebSocket opened: {}", session.getId());
	}
	
	@OnMessage
	public String onMessage(String message, Session session) {
		log.info("Received: {} from {}", message, session.getId());
		return "Echo: " + message;
	}
}
