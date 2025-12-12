/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem.request;

import lombok.Data;
import dev.langchain4j.agent.tool.ToolSpecification;
import org.openmrs.module.expertsystem.dto.MessageDto;

import java.util.List;
import java.util.Map;

/**
 * A simple data object (POJO) to hold the prompt request data from JSON. Provides convenience
 * getters and setters for common chat options.
 */
@Data
public class PromptRequest {
	
	private String model;
	
	private List<MessageDto> messages;
	
	private Map<String, Object> options;
	
	public PromptRequest() {
		this.options = new java.util.HashMap<>();
	}
	
	public Boolean getThink() {
		return (Boolean) options.get("think");
	}
	
	public void setThink(Boolean think) {
		options.put("think", think);
	}
	
	public Double getTemperature() {
		return (Double) options.get("temperature");
	}
	
	public void setTemperature(Double temperature) {
		options.put("temperature", temperature);
	}
	
	public Double getTopP() {
		return (Double) options.get("top_p");
	}
	
	public void setTopP(Double topP) {
		options.put("top_p", topP);
	}
	
	public Integer getTopK() {
		return (Integer) options.get("top_k");
	}
	
	public void setTopK(Integer topK) {
		options.put("top_k", topK);
	}
	
	public Double getFrequencyPenalty() {
		return (Double) options.get("frequency_penalty");
	}
	
	public void setFrequencyPenalty(Double frequencyPenalty) {
		options.put("frequency_penalty", frequencyPenalty);
	}
	
	public Double getPresencePenalty() {
		return (Double) options.get("presence_penalty");
	}
	
	public void setPresencePenalty(Double presencePenalty) {
		options.put("presence_penalty", presencePenalty);
	}
	
	public Integer getMaxOutputTokens() {
		return (Integer) options.get("max_output_tokens");
	}
	
	public void setMaxOutputTokens(Integer maxOutputTokens) {
		options.put("max_output_tokens", maxOutputTokens);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getStopSequences() {
		return (List<String>) options.get("stop");
	}
	
	public void setStopSequences(List<String> stopSequences) {
		options.put("stop", stopSequences);
	}
	
	@SuppressWarnings("unchecked")
	public List<ToolSpecification> getToolSpecifications() {
		return (List<ToolSpecification>) options.get("tools");
	}
	
	public void setToolSpecifications(List<ToolSpecification> toolSpecifications) {
		options.put("tools", toolSpecifications);
	}
}
