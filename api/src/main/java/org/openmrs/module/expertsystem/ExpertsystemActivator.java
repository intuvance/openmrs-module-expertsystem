/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.expertsystem;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.springframework.stereotype.Component;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */

@SuppressWarnings("ALL")
@Slf4j
@Component
public class ExpertsystemActivator extends BaseModuleActivator implements DaemonTokenAware {
	
	private static DaemonToken daemonToken;
	
	public static String getPropertyValue(String property, String defaultValue) {
		if (System.getProperties().containsKey(property)) {
			return System.getProperty(property);
		}
		return Context.getRuntimeProperties().getProperty(property, defaultValue);
	}
	
	public static DaemonToken getDaemonToken() {
		return daemonToken;
	}
	
	@Override
	public void setDaemonToken(DaemonToken token) {
		daemonToken = token;
	}
	
	@Override
	public void started() {
		log.info(ExpertsystemConstants.GREEN + "Started AI Expert System module" + ExpertsystemConstants.RESET);
	}
	
	public void shutdown() {
		log.info(ExpertsystemConstants.RED + "Shutdown AI Expert System module" + ExpertsystemConstants.RESET);
	}
}
