/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem.api;

import dev.langchain4j.model.chat.response.ChatResponse;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.expertsystem.ExpertsystemConstants;
import org.openmrs.module.expertsystem.request.PromptRequest;

import java.util.function.Consumer;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface ExpertSystemService extends OpenmrsService {
	
	/**
	 * Performs a synchronous chat request to Ollama. This will wait for the entire response before
	 * returning.
	 * 
	 * @param promptRequest The request containing the prompt.
	 * @return The complete response from the model.
	 */
	@Authorized(ExpertsystemConstants.AI_EXPERT_SYSTEM_MODULE_PRIVILEGE)
	ChatResponse chat(PromptRequest promptRequest);
	
	/**
	 * Streams chat responses token by token, providing real-time updates as the AI generates text.
	 * 
	 * @param promptRequest the request containing the prompt and configuration for the chat
	 * @param onToken consumer that receives each token as it's generated (never null)
	 * @param onComplete consumer called when the entire response has been completed successfully
	 * @param onError consumer called when an error occurs during processing (never null)
	 * @throws IllegalArgumentException if any of the consumers are null
	 * @throws IllegalStateException if the chat service is not properly initialized
	 */
	void chatStream(PromptRequest promptRequest, Consumer<String> onToken, Consumer<String> onComplete,
	        Consumer<Throwable> onError);
}
