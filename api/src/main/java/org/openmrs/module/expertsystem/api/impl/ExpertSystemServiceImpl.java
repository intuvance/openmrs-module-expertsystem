/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem.api.impl;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.expertsystem.api.ExpertSystemService;
import org.openmrs.module.expertsystem.api.dao.ExpertSystemDao;
import org.openmrs.module.expertsystem.dto.MessageDto;
import org.openmrs.module.expertsystem.request.PromptRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExpertSystemServiceImpl extends BaseOpenmrsService implements ExpertSystemService {
	
	@Setter
	ExpertSystemDao dao;
	
	@Autowired
	ChatModel expertSystemChatModel;
	
	@Override
	public ChatResponse chat(PromptRequest promptRequest) {
		String modelName = promptRequest.getModel();
		List<ChatMessage> messages = convertDtoMessagesToChatMessages(promptRequest.getMessages());
		Map<String, Object> options = promptRequest.getOptions();

		ChatRequest.Builder builder = ChatRequest.builder();
		builder.messages(messages);
		builder.modelName(modelName);
		if (options != null) {
			if (options.get("temperature") instanceof Double) {
				builder.temperature((Double) options.get("temperature"));
			}
			Object stopSequences = options.get("stop");
			if (stopSequences instanceof List) {
				List<?> rawSequences = (List<?>) stopSequences;
				List<String> listOfSequences = new java.util.ArrayList<>();
				for (Object sequence : rawSequences) {
					if (sequence instanceof String) {
						listOfSequences.add((String) sequence);
					}
				}
				if (!listOfSequences.isEmpty()) {
					builder.stopSequences(listOfSequences);
				}
			}
			if (options.get("top_p") instanceof Double) {
				builder.topP((Double) options.get("top_p"));
			}
			if (options.get("top_k") instanceof Integer) {
				builder.topK((Integer) options.get("top_k"));
			}
			if (options.get("frequency_penalty") instanceof Double) {
				builder.frequencyPenalty((Double) options.get("frequency_penalty"));
			}
			if (options.get("presence_penalty") instanceof Double) {
				builder.presencePenalty((Double) options.get("presence_penalty"));
			}
			if (options.get("max_output_tokens") instanceof Integer) {
				builder.maxOutputTokens((Integer) options.get("max_output_tokens"));
			}
			Object toolSpecs = options.get("tools");
			if (toolSpecs instanceof List) {
				List<?> rawTools = (List<?>) toolSpecs;
				List<ToolSpecification> toolSpecList = new java.util.ArrayList<>();
				for (Object tool : rawTools) {
					if (tool instanceof ToolSpecification) {
						toolSpecList.add((ToolSpecification) tool);
					}
				}
				if (!toolSpecList.isEmpty()) {
					builder.toolSpecifications(toolSpecList);
				}
			}
		}
		ChatRequest actualChatRequest = builder.build();
		return expertSystemChatModel.chat(actualChatRequest);
	}
	
	/**
	 * Converts a list of MessageDto objects to a list of specific ChatMessage implementations.
	 * 
	 * @param messageData The list of MessageDto objects from the request
	 * @return A list of properly typed ChatMessage objects
	 */
	private List<ChatMessage> convertDtoMessagesToChatMessages(List<MessageDto> messageData) {
		List<ChatMessage> chatMessages = new ArrayList<>();

		if (messageData == null || messageData.isEmpty()) {
			log.warn("No messages provided in the request");
			return chatMessages;
		}

		for (MessageDto messageDto : messageData) {
			if (messageDto == null || messageDto.getType() == null) {
				log.warn("Skipping invalid message DTO: {}", messageDto);
				continue;
			}
			String type = messageDto.getType();
			try {
				ChatMessage message;
				switch (type) {
					case "USER":
						message = UserMessage.from(messageDto.getText());
						break;
					case "SYSTEM":
						message = SystemMessage.from(messageDto.getText());
						break;
					case "AI":
						message = AiMessage.from(messageDto.getText());
						break;
					case "CUSTOM":
						log.warn("Skipping CUSTOM message - constructor not implemented");
						continue;
					default:
						log.warn("Unknown message type: {}. Skipping message.", type);
						continue; 
				}
				chatMessages.add(message);
				log.debug("Added message of type: {}", type);
			} catch (Exception error) {
				log.error("Failed to create message from DTO: {}", error.getMessage(), error);
			}
		}
		log.info("Converted {} DTO messages to {} ChatMessage objects",
				messageData.size(), chatMessages.size());
		return chatMessages;
	}
}
