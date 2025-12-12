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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import java.util.List;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.utility.DockerImageName;

public class OllamaImage {
	
	public static final String OLLAMA_IMAGE = "ollama/ollama:latest";
	
	public static String localOllamaImage(String modelName) {
		return String.format("tc-%s-%s", OllamaImage.OLLAMA_IMAGE, modelName);
	}
	
	public static final String TINY_DOLPHIN_MODEL = "tinydolphin";
	
	public static final String LLAMA_3_1 = "llama3.1";
	
	public static final String LLAMA_3_2 = "llama3.2";
	
	public static final String LLAMA_3_2_VISION = "llama3.2-vision";
	
	public static final String QWEN3_06B = "qwen3:0.6b";
	
	public static final String BESPOKE_MINICHECK = "bespoke-minicheck";
	
	public static final String ALL_MINILM_MODEL = "all-minilm";
	
	public static final String GRANITE_3_GUARDIAN = "granite3-guardian";
	
	@SuppressWarnings("resource")
	public static DockerImageName resolve(String baseImage, String localImageName) {
		DockerImageName dockerImageName = DockerImageName.parse(baseImage);
		DockerClient dockerClient = DockerClientFactory.instance().client();
		List<Image> images = dockerClient.listImagesCmd().withReferenceFilter(localImageName).exec();
		if (images.isEmpty()) {
			return dockerImageName;
		}
		return DockerImageName.parse(localImageName).asCompatibleSubstituteFor(baseImage);
	}
}
