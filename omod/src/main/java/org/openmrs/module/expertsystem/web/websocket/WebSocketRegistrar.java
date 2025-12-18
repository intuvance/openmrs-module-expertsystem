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
import org.jspecify.annotations.NonNull;
import org.openmrs.module.expertsystem.ExpertsystemConstants;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

@Slf4j
@Component
public class WebSocketRegistrar implements ApplicationListener<ContextRefreshedEvent> {
	
	private static volatile boolean registered = false;
	
	private final ServletContext servletContext;
	
	public WebSocketRegistrar(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	@Override
	public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
		if (registered) {
			return;
		}
		synchronized (WebSocketRegistrar.class) {
			if (registered) {
				return;
			}
			register();
			registered = true;
		}
	}
	
	private void register() {
		try {
			ServerContainer serverContainer = (ServerContainer) servletContext
			        .getAttribute("javax.websocket.server.ServerContainer");
			if (serverContainer == null) {
				log.error(ExpertsystemConstants.RED + "ServerContainer not found – WebSockets unavailable!"
				        + ExpertsystemConstants.RESET);
				return;
			}
			serverContainer.addEndpoint(ServerEndpointConfig.Builder.create(ExpertsystemSocket.class,
			    "/ws/v1/expertsystem/websocket/tokens").build());
			log.info(ExpertsystemConstants.GREEN + "WebSocket registered at /ws/v1/expertsystem/websocket/tokens"
			        + ExpertsystemConstants.RESET);
		}
		catch (Exception e) {
			log.error(ExpertsystemConstants.RED + "Failed to register WebSocket endpoint" + ExpertsystemConstants.RESET, e);
		}
	}
}
