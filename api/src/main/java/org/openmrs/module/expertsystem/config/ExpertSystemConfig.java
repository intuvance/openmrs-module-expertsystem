/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Contains module's config.
 */
@Component("expertSystemConfig")
public class ExpertSystemConfig {
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	private static final String DEFAULT_BASE_URL = "http://localhost:11434";
	
	private static final String DEFAULT_MODEL_NAME = "meditron";
	
	private static final String DEFAULT_TEMPERATURE = "0.7";
	
	private static final String DEFAULT_TIMEOUT_MINUTES = "15"; // 5 minutes
	
	private static final String DEFAULT_THINKING = "false";
	
	@Bean
	public ChatModel expertSystemChatModel() {
		
		String ollamaBaseUrl = StringUtils.isNotEmpty(System.getenv("OLLAMA_BASE_URL")) ? System.getenv("OLLAMA_BASE_URL")
		        : StringUtils.isNotEmpty(adminService.getGlobalProperty("expertsystem.ollamaBaseUrl")) ? adminService
		                .getGlobalProperty("expertsystem.ollamaBaseUrl") : DEFAULT_BASE_URL;
		
		String ollamaChatModel = StringUtils.isNotEmpty(System.getenv("OLLAMA_CHAT_MODEL")) ? System
		        .getenv("OLLAMA_CHAT_MODEL") : StringUtils.isNotEmpty(adminService
		        .getGlobalProperty("expertsystem.ollamaChatModel")) ? adminService
		        .getGlobalProperty("expertsystem.ollamaChatModel") : DEFAULT_MODEL_NAME;
		
		String modelTemperature = StringUtils.isNotEmpty(System.getenv("MODEL_TEMPERATURE")) ? System
		        .getenv("MODEL_TEMPERATURE") : StringUtils.isNotEmpty(adminService
		        .getGlobalProperty("expertsystem.modelTemperature")) ? adminService
		        .getGlobalProperty("expertsystem.modelTemperature") : DEFAULT_TEMPERATURE;
		
		String timeoutDuration = StringUtils.isNotEmpty(System.getenv("TIMEOUT_DURATION")) ? System
		        .getenv("TIMEOUT_DURATION") : StringUtils.isNotEmpty(adminService
		        .getGlobalProperty("expertsystem.timeoutDuration")) ? adminService
		        .getGlobalProperty("expertsystem.timeoutDuration") : DEFAULT_TIMEOUT_MINUTES;
		
		String enableThinking = StringUtils.isNotEmpty(System.getenv("ENABLE_THINKING")) ? System.getenv("ENABLE_THINKING")
		        : StringUtils.isNotEmpty(adminService.getGlobalProperty("expertsystem.enableThinking")) ? adminService
		                .getGlobalProperty("expertsystem.enableThinking") : DEFAULT_THINKING;
		
		double temperature = Double.parseDouble(modelTemperature);
		long timeoutMinutes = Long.parseLong(timeoutDuration);
		boolean think = Boolean.parseBoolean(enableThinking);
		
		return OllamaChatModel.builder().baseUrl(ollamaBaseUrl).modelName(ollamaChatModel)
		        .timeout(Duration.ofMinutes(timeoutMinutes)).think(think).temperature(temperature).logRequests(true)
		        .logResponses(true).build();
	}
}
