/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem.common;

import static dev.langchain4j.internal.Utils.isNullOrEmpty;

public class AbstractOllamaLMInfrastructure {
	
	public static final String OLLAMA_BASE_URL = System.getenv("OLLAMA_BASE_URL");
	
	private static final String MODEL_NAME = OllamaImage.TINY_DOLPHIN_MODEL;
	
	public static LC4jOllamaContainer ollama;
	
	static {
		if (isNullOrEmpty(OLLAMA_BASE_URL)) {
			String localOllamaImage = OllamaImage.localOllamaImage(MODEL_NAME);
			ollama = new LC4jOllamaContainer(OllamaImage.resolve(OllamaImage.OLLAMA_IMAGE, localOllamaImage))
			        .withModel(MODEL_NAME);
			ollama.start();
			ollama.commitToImage(localOllamaImage);
		}
	}
	
	public static String ollamaBaseUrl(LC4jOllamaContainer ollama) {
		if (isNullOrEmpty(OLLAMA_BASE_URL)) {
			return ollama.getEndpoint();
		} else {
			return OLLAMA_BASE_URL;
		}
	}
}
