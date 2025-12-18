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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.expertsystem.ExpertsystemConstants;
import org.openmrs.module.expertsystem.api.ExpertSystemService;
import org.openmrs.module.expertsystem.dto.MessageDto;
import org.openmrs.module.expertsystem.request.PromptRequest;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class ExpertsystemSocket {
	
	private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
	
	private static final Map<String, String> requestToSession = new ConcurrentHashMap<>();
	
	private static final Map<String, List<String>> userToSessions = new ConcurrentHashMap<>();
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	private ExpertSystemService expertSystemService() {
		return Context.getService(ExpertSystemService.class);
	}
	
	/**
	 * Called when WebSocket connection is opened. Registers session and associates with user ID.
	 */
	@OnOpen
	public void onOpen(Session session) {
		String userId = session.getRequestParameterMap().getOrDefault("userId", Collections.singletonList("anonymous"))
		        .get(0);
		sessions.put(session.getId(), session);
		userToSessions.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(session.getId());
		log.info(ExpertsystemConstants.GREEN + "WS CONNECTED session:{} user:{}" + ExpertsystemConstants.RESET,
		    session.getId(), userId);
	}
	
	/**
	 * Called when WebSocket connection is closed. Cleans up session resources.
	 * 
	 * @param session WebSocket session
	 * @param reason Close reason
	 */
	@OnClose
	public void onClose(Session session, CloseReason reason) {
		cleanUp(session.getId(), reason != null ? reason.toString() : "Normal");
	}
	
	/**
	 * Called when WebSocket error occurs. Logs error and cleans up session.
	 * 
	 * @param session WebSocket session
	 * @param throwable Error exception
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		log.error(ExpertsystemConstants.RED + "WS ERROR: {}" + ExpertsystemConstants.RESET, session.getId(), throwable);
		cleanUp(session.getId(), "Error: " + throwable.getMessage());
	}
	
	/**
	 * Called when WebSocket message is received. Processes prompt request and initiates chat stream.
	 * 
	 * @param session WebSocket session
	 * @param message JSON message payload
	 */
	@OnMessage
	public void onMessage(Session session, String message) {
		if (session == null || !session.isOpen()) {
			log.warn(ExpertsystemConstants.GOLD + "Received message on closed/null session" + ExpertsystemConstants.RESET);
			return;
		}
		try {
			JsonNode json = objectMapper.readTree(message);
			JsonNode requestIdNode = json.get("requestId");
			String requestId;
			if (requestIdNode == null || requestIdNode.isNull() || StringUtils.isBlank(requestIdNode.asText())) {
				requestId = UUID.randomUUID().toString();
				log.warn(ExpertsystemConstants.GOLD + "Missing requestId in payload; generated server-side: {}" + ExpertsystemConstants.RESET, requestId);
			} else {
				requestId = requestIdNode.asText();
			}
			
			requestToSession.put(requestId, session.getId());
			
			String model = json.has("model") ? json.get("model").asText() : "meditron:7b";
			List<MessageDto> messages = new ArrayList<>();
			if (json.has("messages") && json.get("messages").isArray()) {
				for (JsonNode msgNode : json.get("messages")) {
					MessageDto msg = new MessageDto();
					msg.setType(msgNode.has("type") ? msgNode.get("type").asText() : "USER");
					msg.setText(msgNode.has("text") ? msgNode.get("text").asText() : "");
					messages.add(msg);
				}
			}
			
			Map<String, Object> options = new HashMap<>();
			if (json.has("options") && json.get("options").isObject()) {
				Iterator<Map.Entry<String, JsonNode>> fields = json.get("options").fields();
				while (fields.hasNext()) {
					Map.Entry<String, JsonNode> entry = fields.next();
					String key = entry.getKey();
					JsonNode value = entry.getValue();
					
					if ("tools".equals(key) && value.isArray()) {
						List<Object> tools = new ArrayList<>();
						for (JsonNode toolNode : value) {
							tools.add(objectMapper.convertValue(toolNode, Map.class));
						}
						options.put("tools", tools);
					} else if (value.isNumber()) {
						options.put(key, value.numberValue());
					} else if (value.isBoolean()) {
						options.put(key, value.booleanValue());
					} else if (value.isTextual()) {
						options.put(key, value.asText());
					} else {
						options.put(key, objectMapper.convertValue(value, Object.class));
					}
				}
			}
			
			PromptRequest promptRequest = new PromptRequest();
			promptRequest.setModel(model);
			promptRequest.setMessages(messages);
			promptRequest.setOptions(options);
			expertSystemService()
					.chatStream(promptRequest, token -> sendToken(requestId, token),
			    finalText -> sendDone(requestId, finalText), error -> sendError(requestId, error));
		}
		catch (Exception exception) {
			sendErrorDirect(session, exception);
		}
	}
	
	/**
	 * Sends token response to client.
	 * 
	 * @param requestId Request identifier
	 * @param token Token data
	 */
	private void sendToken(String requestId, String token) {
		Session session = resolveSession(requestId);
		Map<String, Object> tokens = new HashMap<>();
		tokens.put("type", "token");
		tokens.put("requestId", requestId);
		tokens.put("data", token);
		if (session == null || !session.isOpen()) {
			log.warn(ExpertsystemConstants.GOLD + "Cannot send token; session not found or closed for requestId={}" + ExpertsystemConstants.RESET, requestId);
			return;
		}
		send(session, tokens);
	}
	
	/**
	 * Sends completion response to client.
	 * 
	 * @param requestId Request identifier
	 * @param finalText Completion text
	 */
	private void sendDone(String requestId, String finalText) {
		Session session = resolveSession(requestId);
		Map<String, Object> operations = new HashMap<>();
		operations.put("type", "done");
		operations.put("requestId", requestId);
		operations.put("data", finalText);
		if (session == null || !session.isOpen()) {
			log.warn(ExpertsystemConstants.GOLD + "Cannot execute sendDone; session not found or closed for requestId={}" + ExpertsystemConstants.RESET, requestId);
			return;
		}
		send(session, operations);
		requestToSession.remove(requestId);
	}
	
	/**
	 * Sends error response to client.
	 * 
	 * @param requestId Request identifier
	 * @param error Error exception
	 */
	private void sendError(String requestId, Throwable error) {
		Session session = resolveSession(requestId);
		Map<String, Object> errors = new HashMap<>();
		errors.put("type", "error");
		errors.put("requestId", requestId);
		errors.put("data", error != null ? error.getMessage() : "Unknown error!");
		if (session == null || !session.isOpen()) {
			log.warn(ExpertsystemConstants.GOLD + "Cannot send error; session not found for requestId={}" + ExpertsystemConstants.RESET, requestId);
			return;
		}
		send(session, errors);
		requestToSession.remove(requestId);
	}
	
	/**
	 * Cleans up session resources. Removes session from tracking maps. WS Session lifecycle is
	 * container-managed (do NOT close manually)
	 * 
	 * @param sessionId Session identifier
	 * @param reason Close reason
	 */
	private void cleanUp(String sessionId, String reason) {
		if (sessionId == null) return;
		sessions.remove(sessionId);
		requestToSession.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
		userToSessions.values().forEach(list -> list.remove(sessionId));
		log.info(ExpertsystemConstants.GOLD + "WS CLOSED: {} Reason: {}" + ExpertsystemConstants.RESET, sessionId, reason);
	}
	
	/**
	 * Resolves session by request ID. WS Session lifecycle is container-managed (do NOT close manually)
	 * 
	 * @param requestId Request identifier
	 * @return Session or null if not found
	 */
	private Session resolveSession(String requestId) {
		if (requestId == null) return null;
		String sessionId = requestToSession.get(requestId);
		return sessionId != null ? sessions.get(sessionId) : null;
	}

	/**
	 * Sends payload to WebSocket session.
	 * WS Session lifecycle is container-managed (do NOT closemanually)
	 * 
	 * @param session WebSocket session
	 * @param payload JSON payload to send
	 */
	private void send(Session session, Map<String, Object> payload) {
		if (session == null || !session.isOpen()) {
			log.warn(ExpertsystemConstants.GOLD + "Cannot send payload; session closed or null!" + ExpertsystemConstants.RESET);
			return;
		}
		try {
			session.getAsyncRemote().sendText(objectMapper.writeValueAsString(payload));
		}
		catch (Exception exception) {
			log.error(ExpertsystemConstants.RED +"WS send failed for session {}: {}" + ExpertsystemConstants.RESET, session.getId(), exception.getMessage());
		}
	}

	/**
	 * Sends an error message directly to the session if it's open.
	 * @param session The WebSocket session to send the error to
	 * @param error The Throwable error to include in the message
	 */
	private void sendErrorDirect(Session session, Throwable error) {
		if (session == null || !session.isOpen()) {
			return;
		}
		Map<String, Object> errors = new HashMap<>();
		errors.put("type", "error");
		errors.put("data", error != null ? error.getMessage() : "Unknown error!");
		send(session, errors);
	}

}
