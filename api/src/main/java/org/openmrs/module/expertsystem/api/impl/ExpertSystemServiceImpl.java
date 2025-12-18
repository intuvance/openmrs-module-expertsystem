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
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.expertsystem.ExpertsystemActivator;
import org.openmrs.module.expertsystem.ExpertsystemConstants;
import org.openmrs.module.expertsystem.api.ExpertSystemService;
import org.openmrs.module.expertsystem.api.dao.ExpertSystemDao;
import org.openmrs.module.expertsystem.dto.MessageDto;
import org.openmrs.module.expertsystem.request.PromptRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
public class ExpertSystemServiceImpl extends BaseOpenmrsService implements ExpertSystemService {
	
	private static final String DEFAULT_TIMEOUT_MINUTES = "15";
	
	@Setter
	private ExpertSystemDao dao;
	
	@Autowired
	public ChatModel expertSystemChatModel;
	
	@Autowired
	private StreamingChatModel expertSystemStreamingChatModel;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	@Autowired
	private Scheduler expertSystemScheduler;
	
	/**
	 * Processes a chat request using the expertsystem's streaming chat model with timeout handling.
	 * <p>
	 * This method executes the chat operation asynchronously using a daemon thread and provides
	 * timeout protection. It streams the response and constructs a final ChatResponse once the
	 * streaming is complete or an error occurs.
	 * </p>
	 * <p>
	 * The method performs the following steps:
	 * <ol>
	 * <li>Reads timeout configuration from environment variables or global properties</li>
	 * <li>Executes the chat operation on a daemon thread using the expert system scheduler</li>
	 * <li>Streams the response and collects the final text</li>
	 * <li>Waits for completion with the configured timeout</li>
	 * <li>Handles timeout and error conditions appropriately</li>
	 * <li>Returns the constructed ChatResponse</li>
	 * </ol>
	 * 
	 * @param promptRequest the chat prompt request containing the user's message and context
	 * @return a ChatResponse containing the AI's response message
	 * @throws RuntimeException if the operation times out or streaming fails
	 * @throws IllegalStateException if the daemon token is not available
	 * @see #chatStream(PromptRequest, Consumer, Consumer, Consumer)
	 * @see ExpertsystemConstants
	 */
	@Override
	public ChatResponse chat(PromptRequest promptRequest) {
		
		String timeoutDuration = StringUtils.defaultIfBlank(System.getenv("TIMEOUT_DURATION"), StringUtils
		        .defaultIfBlank(adminService.getGlobalProperty("expertsystem.timeoutDuration"), DEFAULT_TIMEOUT_MINUTES));
		long timeout = Long.parseLong(timeoutDuration);
		
		return Single.fromCallable(() -> {
			DaemonToken daemonToken = ExpertsystemActivator.getDaemonToken();
			if (daemonToken == null) {
				throw new IllegalStateException(
				        ExpertsystemConstants.RED + "Daemon token not available!" + ExpertsystemConstants.RESET);
			}
			AtomicReference<Throwable> errorRef = new AtomicReference<>();
			AtomicReference<ChatResponse> responseRef = new AtomicReference<>();
			CountDownLatch latch = new CountDownLatch(1);
			chatStream(promptRequest, token -> {}, finalText -> {
				responseRef.set(ChatResponse.builder().aiMessage(AiMessage.from(finalText)).build());
				latch.countDown();
			}, error -> {
				errorRef.set(error);
				latch.countDown();
			});
			boolean completed = latch.await(timeout, TimeUnit.MINUTES);
			if (!completed) {
				throw new RuntimeException(ExpertsystemConstants.RED + "AI response timed out after " + timeout + " minutes!"
				        + ExpertsystemConstants.RESET);
			}
			if (errorRef.get() != null) {
				throw new RuntimeException(ExpertsystemConstants.RED + "AI streaming failed!" + ExpertsystemConstants.RESET,
				        errorRef.get());
			}
			return responseRef.get();
		}).subscribeOn(expertSystemScheduler).blockingGet();
	}
	
	/**
	 * Processes a streaming chat request using the expertsystem's streaming chat model.
	 * <p>
	 * This method executes the chat operation asynchronously in a daemon thread and provides
	 * real-time token streaming, completion handling, and error management.
	 * </p>
	 * <p>
	 * The method performs the following steps:
	 * <ol>
	 * <li>Validates the availability of the daemon token</li>
	 * <li>Converts the prompt request to a chat request with appropriate options</li>
	 * <li>Executes the streaming chat operation in a daemon thread</li>
	 * <li>Streams tokens to the onToken consumer as they are received</li>
	 * <li>Handles completion and error scenarios</li>
	 * </ol>
	 * <p>
	 * Supported options include:
	 * <ul>
	 * <li>temperature - controls response randomness (0.0-1.0)</li>
	 * <li>stop - stop sequences to terminate generation</li>
	 * <li>top_p - nucleus sampling parameter</li>
	 * <li>top_k - top-k sampling parameter</li>
	 * <li>frequency_penalty - penalty for repeated tokens</li>
	 * <li>presence_penalty - penalty for new tokens</li>
	 * <li>max_output_tokens - maximum tokens to generate</li>
	 * <li>tools - tool specifications for function calling</li>
	 * </ul>
	 * 
	 * @param promptRequest the chat prompt request containing messages and options
	 * @param onToken consumer that receives each token as it's generated
	 * @param onComplete consumer that receives the final complete response
	 * @param onError consumer that receives any errors that occur during streaming
	 * @throws IllegalStateException if the daemon token is not available
	 * @see #expertSystemStreamingChatModel
	 * @see DaemonToken
	 * @see Daemon#runInDaemonThreadAndWait(Runnable, DaemonToken)
	 */
	@Override
	public void chatStream(PromptRequest promptRequest, Consumer<String> onToken, Consumer<String> onComplete,
	        Consumer<Throwable> onError) {
		
		DaemonToken daemonToken = ExpertsystemActivator.getDaemonToken();
		if (daemonToken == null) {
			onError.accept(new IllegalStateException(
			        ExpertsystemConstants.RED + "Daemon token not available!" + ExpertsystemConstants.RESET));
			return;
		}
		
		Daemon.runInDaemonThreadAndWait(() -> {
			try {
				String modelName = promptRequest.getModel();
				List<ChatMessage> messages = convertDtoMessagesToChatMessages(promptRequest.getMessages());
				Map<String, Object> options = promptRequest.getOptions();

				ChatRequest.Builder builder = ChatRequest.builder().messages(messages).modelName(modelName);
				
				if (options != null) {
					if (options.get("temperature") instanceof Double) {
						builder.temperature((Double) options.get("temperature"));
					}
					Object stopSequences = options.get("stop");
					if (stopSequences instanceof List) {
						List<String> stops = new ArrayList<>();
						for (Object s : (List<?>) stopSequences) {
							if (s instanceof String) {
								stops.add((String) s);
							}
						}
						if (!stops.isEmpty()) {
							builder.stopSequences(stops);
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
						List<ToolSpecification> toolSpecList = new ArrayList<>();
						for (Object tool : (List<?>) toolSpecs) {
							if (tool instanceof ToolSpecification) {
								toolSpecList.add((ToolSpecification) tool);
							}
						}
						if (!toolSpecList.isEmpty()) {
							builder.toolSpecifications(toolSpecList);
						}
					}
				}
				
				ChatRequest request = builder.build();
				
				expertSystemStreamingChatModel.chat(request, new StreamingChatResponseHandler() {
					
					@Override
					public void onPartialResponse(String token) {
						onToken.accept(token);
					}
					
					@Override
					public void onCompleteResponse(ChatResponse response) {
						try {
							onComplete.accept(response.aiMessage() != null ? response.aiMessage().text() : "");
						}
						finally {
							log.info(ExpertsystemConstants.MAGENTA + "Streaming completed" + ExpertsystemConstants.RESET);
						}
					}
					
					@Override
					public void onError(Throwable error) {
						log.error(ExpertsystemConstants.RED + "Streaming chat failed!" + ExpertsystemConstants.RESET, error);
						onError.accept(error);
					}
				});
				
			}
			catch (Throwable throwable) {
				onError.accept(throwable);
			}
		}, daemonToken);
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
		log.info("Converted {} DTO messages to {} ChatMessage objects", messageData.size(), chatMessages.size());
		return chatMessages;
	}
}
