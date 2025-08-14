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

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.expertsystem.ExpertSystemConstants;
import org.openmrs.module.expertsystem.api.ExpertSystemService;
import org.openmrs.module.expertsystem.request.PromptRequest;
import org.openmrs.module.expertsystem.web.ErrorHandler;
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

import javax.validation.Valid;

@Slf4j
@Controller(value = "expertSystemRestController")
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/" + ExpertSystemConstants.EXPERT_SYSTEM_MODULE_ID)
public class ExpertSystemRestController extends MainResourceController {

	@Autowired
	@Qualifier("expertSystemService")
	private ExpertSystemService expertSystemService;
	
	@RequestMapping(value = "/prompt", method=RequestMethod.POST, consumes="application/json")
	public ResponseEntity<String> prompt(
			@Valid
			@RequestBody PromptRequest promptRequest,
			final BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return new ResponseEntity<>(new ErrorHandler()
					.getFirstErrorMessage(bindingResult), HttpStatus.BAD_REQUEST);
		}
		
		String result = expertSystemService.chat(promptRequest);

		if (result != null) {
			log.info("{}", result);
			return new ResponseEntity<>(result, HttpStatus.OK);
		} else {
			log.warn("No response, please ensure that Ollama is running locally!");
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}
}
