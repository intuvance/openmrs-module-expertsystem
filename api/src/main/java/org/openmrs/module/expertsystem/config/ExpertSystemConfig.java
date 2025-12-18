/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component("expertSystemConfig")
public class ExpertSystemConfig {
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	private static final String DEFAULT_BASE_URL = "http://localhost:11434";
	
	public static final int CORE_POOL_SIZE = 2;
	
	private static final String DEFAULT_TEMPERATURE = "0.7";
	
	public static final int MAXIMUM_POOL_SIZE = 4;
	
	private static final String DEFAULT_THINKING = "false";
	
	public static final int KEEP_ALIVE_TIME = 90;
	
	public static final int QUEUE_CAPACITY = 50;
	
	private static final String DEFAULT_MODEL_NAME = "meditron:7b";
	
	private static final String DEFAULT_TIMEOUT_MINUTES = "15";
	
	/**
	 * Creates and configures an Ollama ChatModel bean for the expertsystem.
	 * <p>
	 * This method builds an Ollama chat model with the following configurations:
	 * <ul>
	 * <li>Timeout: Configured via {@code config.timeoutMinutes}</li>
	 * <li>Base URL: Configured via {@code config.baseUrl}</li>
	 * <li>Model name: Configured via {@code config.modelName}</li>
	 * <li>Think parameter: Configured via {@code config.think}</li>
	 * <li>Temperature: Configured via {@code config.temperature}</li>
	 * <li>Request logging: Enabled (true)</li>
	 * <li>Response logging: Enabled (true)</li>
	 * </ul>
	 * 
	 * @return An OllamaChatModel instance configured for the expert system
	 * @throws IllegalArgumentException if any required configuration parameters are invalid
	 * @see OllamaChatModel
	 * @see ModelConfig
	 */
	@Bean
	public ChatModel expertSystemChatModel() {
		ModelConfig config = getModelConfig();
		return OllamaChatModel.builder().timeout(Duration.ofMinutes(config.timeoutMinutes)).baseUrl(config.baseUrl)
		        .modelName(config.modelName).think(config.think).temperature(config.temperature).logRequests(true)
		        .logResponses(true).build();
	}
	
	/**
	 * Creates and configures a streaming Ollama ChatModel bean for the expertsystem.
	 * <p>
	 * This method builds a streaming Ollama chat model with the following configurations:
	 * <ul>
	 * <li>Timeout: Configured via {@code config.timeoutMinutes}</li>
	 * <li>Base URL: Configured via {@code config.baseUrl}</li>
	 * <li>Model name: Configured via {@code config.modelName}</li>
	 * <li>Think parameter: Configured via {@code config.think}</li>
	 * <li>Temperature: Configured via {@code config.temperature}</li>
	 * <li>Request logging: Enabled (true)</li>
	 * <li>Response logging: Enabled (true)</li>
	 * </ul>
	 * <p>
	 * The streaming capability allows for real-time response processing as the model generates
	 * output.
	 * 
	 * @return A StreamingChatModel instance configured for the expert system
	 * @throws IllegalArgumentException if any required configuration parameters are invalid
	 * @see OllamaStreamingChatModel
	 * @see ModelConfig
	 * @see StreamingChatModel
	 */
	@Bean
	public StreamingChatModel expertSystemStreamingChatModel() {
		ModelConfig config = getModelConfig();
		return OllamaStreamingChatModel.builder().timeout(Duration.ofMinutes(config.timeoutMinutes)).baseUrl(config.baseUrl)
		        .modelName(config.modelName).think(config.think).temperature(config.temperature).logRequests(true)
		        .logResponses(true).build();
	}
	
	/**
	 * Creates a thread pool executor for AI processing tasks with controlled resource usage.
	 * <p>
	 * The executor is configured with:
	 * <ul>
	 * <li>2 core threads to handle baseline workload</li>
	 * <li>4 maximum threads to handle peak loads</li>
	 * <li>90-second keep-alive time to reclaim idle threads</li>
	 * <li>50-task bounded queue for back-pressure</li>
	 * <li>Custom thread factory for named daemon threads</li>
	 * </ul>
	 * 
	 * @return configured {@link ExecutorService} for AI processing
	 */
	@Bean(destroyMethod = "shutdown")
	public ExecutorService expertSystemExecutor() {
		return new ThreadPoolExecutor(Integer.parseInt(getExecutorConfig("expertsystem.corePoolSize", CORE_POOL_SIZE)),
		        Integer.parseInt(getExecutorConfig("expertsystem.maximumPoolSize", MAXIMUM_POOL_SIZE)),
		        Integer.parseInt(getExecutorConfig("expertsystem.keepAliveTime", KEEP_ALIVE_TIME)), TimeUnit.SECONDS,
		        new LinkedBlockingQueue<>(Integer.parseInt(getExecutorConfig("expertsystem.queueCapacity", QUEUE_CAPACITY))),
		        runnable -> {
			        Thread thread = new Thread(runnable);
			        thread.setName("expertsystem-reactor");
			        thread.setDaemon(true);
			        return thread;
		        });
	}
	
	/**
	 * Creates a reactive scheduler backed by the expertsystem executor.
	 * <p>
	 * This scheduler provides reactive stream support for AI processing tasks using the configured
	 * thread pool.
	 * </p>
	 * 
	 * @param expertSystemExecutor the executor service to back the scheduler
	 * @return {@link Scheduler} for reactive AI processing
	 */
	@Bean
	public Scheduler expertSystemScheduler(ExecutorService expertSystemExecutor) {
		return Schedulers.from(expertSystemExecutor);
	}
	
	/**
	 * Retrieves a configuration value from the admin service's global properties, with a default
	 * integer fallback.
	 * <p>
	 * This method checks if the specified global property exists in the admin service and returns
	 * its value. If the property is not found or is empty, it returns the string representation of
	 * the provided default value.
	 * 
	 * @param globalProperty the name of the global property to retrieve from the admin service
	 * @param defaultValue the integer value to use as fallback when the global property is not
	 *            available
	 * @return the global property value if found and non-empty, otherwise the string representation
	 *         of the default value
	 * @see #getConfigurationValue(String, String, String)
	 */
	private String getExecutorConfig(String globalProperty, int defaultValue) {
		return StringUtils.isNotEmpty(adminService.getGlobalProperty(globalProperty)) ? adminService
		        .getGlobalProperty(globalProperty) : String.valueOf(defaultValue);
	}
	
	/**
	 * Constructs and returns a ModelConfig object containing all Ollama model configuration
	 * parameters.
	 * <p>
	 * This method gathers configuration from multiple sources (environment variables and global
	 * properties) and parses them into the appropriate data types before creating the configuration
	 * object.
	 * <p>
	 * The method retrieves the following configuration parameters:
	 * <ul>
	 * <li>Base URL for the Ollama service</li>
	 * <li>Chat model name to use</li>
	 * <li>Temperature parameter for response randomness</li>
	 * <li>Timeout duration in minutes</li>
	 * <li>Thinking mode flag</li>
	 * </ul>
	 * 
	 * @return a fully configured ModelConfig instance containing all Ollama model parameters
	 * @throws NumberFormatException if temperature or timeout values cannot be parsed as numbers
	 * @throws IllegalArgumentException if boolean parsing fails for the thinking parameter
	 * @see ModelConfig
	 * @see #getConfigurationValue(String, String, String)
	 */
	private ModelConfig getModelConfig() {
		String ollamaBaseUrl = getConfigurationValue("OLLAMA_BASE_URL", "expertsystem.ollamaBaseUrl", DEFAULT_BASE_URL);
		String ollamaChatModel = getConfigurationValue("OLLAMA_CHAT_MODEL", "expertsystem.ollamaChatModel",
		    DEFAULT_MODEL_NAME);
		String modelTemperature = getConfigurationValue("MODEL_TEMPERATURE", "expertsystem.modelTemperature",
		    DEFAULT_TEMPERATURE);
		String timeoutDuration = getConfigurationValue("TIMEOUT_DURATION", "expertsystem.timeoutDuration",
		    DEFAULT_TIMEOUT_MINUTES);
		String enableThinking = getConfigurationValue("ENABLE_THINKING", "expertsystem.enableThinking", DEFAULT_THINKING);
		
		double temperature = Double.parseDouble(modelTemperature);
		long timeoutMinutes = Long.parseLong(timeoutDuration);
		boolean think = Boolean.parseBoolean(enableThinking);
		
		return new ModelConfig(ollamaBaseUrl, ollamaChatModel, temperature, timeoutMinutes, think);
	}
	
	/**
	 * Retrieves a configuration value from either environment variables or global properties, with
	 * a string default fallback.
	 * <p>
	 * This method checks for a value in the following priority order:
	 * <ol>
	 * <li>Environment variable with the specified name</li>
	 * <li>Global property from the admin service</li>
	 * <li>Default string value if neither is available</li>
	 * </ol>
	 * <p>
	 * This provides a flexible configuration mechanism where environment variables take precedence
	 * over global properties, which in turn take precedence over hardcoded defaults.
	 * 
	 * @param envVar the name of the environment variable to check first
	 * @param globalProperty the name of the global property to check if environment variable is not
	 *            set
	 * @param defaultValue the default string value to return if neither environment variable nor
	 *            global property is available
	 * @return the configuration value from environment variable, global property, or default string
	 */
	private String getConfigurationValue(String envVar, String globalProperty, String defaultValue) {
		return StringUtils.isNotEmpty(System.getenv(envVar)) ? System.getenv(envVar) : StringUtils.isNotEmpty(adminService
		        .getGlobalProperty(globalProperty)) ? adminService.getGlobalProperty(globalProperty) : defaultValue;
	}
	
	/**
	 * Inner class representing the configuration parameters for an Ollama chat model.
	 * <p>
	 * This configuration includes:
	 * <ul>
	 * <li>Base URL for the Ollama service</li>
	 * <li>Model name to use</li>
	 * <li>Temperature parameter for response randomness</li>
	 * <li>Timeout duration in minutes</li>
	 * <li>Thinking mode flag</li>
	 * </ul>
	 * <p>
	 * All fields are final and immutable, making this class thread-safe and suitable for use as a
	 * configuration object that can be shared across different components.
	 */
	private static class ModelConfig {
		
		final String baseUrl;
		
		final String modelName;
		
		final double temperature;
		
		final long timeoutMinutes;
		
		final boolean think;
		
		/**
		 * Creates a new ModelConfig instance with the specified parameters.
		 * 
		 * @param baseUrl the base URL of the Ollama service
		 * @param modelName the name of the Ollama model
		 * @param temperature the temperature parameter (0.0-1.0)
		 * @param timeoutMinutes the timeout duration in minutes
		 * @param think flag indicating thinking mode enabled
		 */
		ModelConfig(String baseUrl, String modelName, double temperature, long timeoutMinutes, boolean think) {
			this.baseUrl = baseUrl;
			this.modelName = modelName;
			this.temperature = temperature;
			this.timeoutMinutes = timeoutMinutes;
			this.think = think;
		}
	}
}
