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

import dev.langchain4j.model.chat.ChatModel;
import lombok.Setter;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.expertsystem.api.ExpertSystemService;
import org.openmrs.module.expertsystem.api.dao.ExpertSystemDao;
import org.openmrs.module.expertsystem.request.PromptRequest;

public class ExpertSystemServiceImpl extends BaseOpenmrsService implements ExpertSystemService {

	@Setter
	ExpertSystemDao dao;

	@Setter
	ChatModel chatModel;

	@Override
	public String chat(PromptRequest promptRequest) {
		return chatModel.chat(promptRequest
				.getUserMessage());
	}
}
