/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem.web.controller;

import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.expertsystem.ExpertsystemConstants;
import org.openmrs.module.expertsystem.api.ExpertSystemService;
import org.openmrs.module.expertsystem.request.PromptRequest;
import org.openmrs.module.expertsystem.utils.ConfigurationUtils;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;

@Slf4j
@Controller(value = "expertSystemRestController")
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/" + ExpertsystemConstants.EXPERT_SYSTEM_MODULE_ID)
public class ExpertSystemRestController extends MainResourceController {
	
	@Autowired
	@Qualifier("expertSystemService")
	private ExpertSystemService expertSystemService;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	@RequestMapping(value = "/models", method = RequestMethod.GET)
	public ResponseEntity<?> getModels() throws Exception {
		String ollamaBaseUrl = ConfigurationUtils.getConfigurationValue(adminService, "OLLAMA_BASE_URL",
		    "expertsystem.ollamaBaseUrl", ConfigurationUtils.OllamaDefaults.DEFAULT_BASE_URL);
		RestTemplate restTemplate = new RestTemplate();
		String response;
		try {
			response = restTemplate.getForObject(ollamaBaseUrl + "/api/tags", String.class);
		}
		catch (RestClientException error) {
			log.error("Cannot get A.I models! {}", error.getMessage());
			throw new Exception(error);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/prompt", method = RequestMethod.POST)
	public ResponseEntity<String> prompt(@Valid @RequestBody PromptRequest promptRequest, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return ResponseEntity.badRequest()
					.body("Invalid request: " + bindingResult.getAllErrors()
							.get(0).getDefaultMessage());
		}

		try {
			ChatResponse response = expertSystemService.chat(promptRequest);
			return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		} catch (Exception error) {
			log.error("AI request failed!", error);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("AI processing failed! " + error.getMessage());
		}
	}
}
