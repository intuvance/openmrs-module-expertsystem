/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem.utils;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.AdministrationService;

/**
 * A utility class for retrieving configuration values and default constants.
 */
public final class ConfigurationUtils {
	
	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private ConfigurationUtils() {
	}
	
	public static final int CORE_POOL_SIZE = 2;
	
	public static final int MAXIMUM_POOL_SIZE = 4;
	
	public static final int KEEP_ALIVE_TIME_SECONDS = 90;
	
	public static final int QUEUE_CAPACITY = 50;
	
	/**
	 * Inner class to group all default values related to the Ollama model configuration. This helps
	 * in organizing the constants logically.
	 */
	public static final class OllamaDefaults {
		
		public static final String DEFAULT_BASE_URL = "http://localhost:11434";
		
		public static final String DEFAULT_MODEL_NAME = "meditron:7b";
		
		public static final String DEFAULT_TEMPERATURE = "0.7";
		
		public static final String DEFAULT_TIMEOUT_MINUTES = "15";
		
		public static final String DEFAULT_THINKING_STATUS = "false";
		
		private OllamaDefaults() {
		}
	}
	
	/**
	 * Retrieves a configuration value from either environment variables or global properties, with
	 * a string default fallback.
	 * <p>
	 * This method checks for a value in the following priority order:
	 * <ol>
	 * <li>Environment variable with the specified name</li>
	 * <li>Global property from the provided {@link AdministrationService}</li>
	 * <li>Default string value if neither is available</li>
	 * </ol>
	 * 
	 * @param adminService The OpenMRS {@link AdministrationService} to fetch global properties.
	 * @param envVar The name of the environment variable to check first.
	 * @param globalProperty The name of the global property to check if the environment variable is
	 *            not set.
	 * @param defaultValue The default string value to return if neither environment variable nor
	 *            global property is available.
	 * @return The configuration value from the environment variable, global property, or default
	 *         string.
	 */
	public static String getConfigurationValue(AdministrationService adminService, String envVar, String globalProperty,
	        String defaultValue) {
		String envValue = System.getenv(envVar);
		if (StringUtils.isNotEmpty(envValue)) {
			return envValue;
		}
		String globalPropValue = adminService.getGlobalProperty(globalProperty);
		if (StringUtils.isNotEmpty(globalPropValue)) {
			return globalPropValue;
		}
		return defaultValue;
	}
}
