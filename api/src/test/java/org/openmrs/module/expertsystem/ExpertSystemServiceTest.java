/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.expertsystem.common.AbstractOllamaLMInfrastructure;
import org.openmrs.module.expertsystem.common.OllamaImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is a unit test, which verifies logic in ExpertSystemService. It doesn't extend
 * BaseModuleContextSensitiveTest, thus it is run without the in-memory DB and Spring context.
 */
public class ExpertSystemServiceTest extends AbstractOllamaLMInfrastructure {
	
	private static final Logger log = LoggerFactory.getLogger(ExpertSystemServiceTest.class);
	
	@Before
	public void setupMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	static final String MODEL_NAME = OllamaImage.TINY_DOLPHIN_MODEL;
	
	ChatModel model = OllamaChatModel.builder().baseUrl(ollamaBaseUrl(ollama)).modelName(MODEL_NAME).temperature(0.0)
	        .logRequests(true).logResponses(true).build();
	
	@Test
	public void chat_should_generate_valid_response() {
		
		UserMessage userMessage = UserMessage.from("What is the name of the process by which the body breaks down food?");
		ChatResponse response = model.chat(userMessage);
		
		AiMessage aiMessage = response.aiMessage();
		assertThat(aiMessage.text()).contains("digestion");
		assertThat(aiMessage.toolExecutionRequests()).isEmpty();
		
		ChatResponseMetadata metadata = response.metadata();
		assertThat(metadata.modelName()).isEqualTo(MODEL_NAME);
		
		TokenUsage tokenUsage = metadata.tokenUsage();
		assertThat(tokenUsage.inputTokenCount()).isPositive();
		assertThat(tokenUsage.outputTokenCount()).isPositive();
		assertThat(tokenUsage.totalTokenCount()).isEqualTo(tokenUsage.inputTokenCount() + tokenUsage.outputTokenCount());
		assertThat(metadata.finishReason()).isEqualTo(FinishReason.STOP);
	}
}
