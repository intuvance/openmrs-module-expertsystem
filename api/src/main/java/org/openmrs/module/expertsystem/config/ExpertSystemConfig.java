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
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Contains module's config.
 */
@Component("expertSystemConfig")
public class ExpertSystemConfig {
	
	@Bean
	public ChatModel chatModel() {
		return OllamaChatModel.builder()
				.baseUrl("http://localhost:11434") 
				.modelName("mistral")
				.temperature(0.7)
				.logRequests(true)
				.logResponses(true)
				.build();
	}
}
