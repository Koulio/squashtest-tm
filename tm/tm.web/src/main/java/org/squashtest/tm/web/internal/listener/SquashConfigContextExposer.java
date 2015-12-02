/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.internal.annotation.ApplicationComponent;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static org.squashtest.tm.service.configuration.ConfigurationService.Properties.MILESTONE_FEATURE_ENABLED;

/**
 * This listener exposes various application-wide configuration properties in the ServletContext (aka Application scope)
 *
 * @author Gregory Fouquet
 */
@ApplicationComponent
public class SquashConfigContextExposer implements ServletContextListener, ApplicationListener<ContextStartedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SquashConfigContextExposer.class);
	/**
	 * Attribute name of the app scope boolean property which indicates if the milestones feature is enabled.
	 */
	public static final String MILESTONE_FEATURE_ENABLED_CONTEXT_ATTR = "milestoneFeatureEnabled";

	private ServletContextEvent sce;
	private ContextStartedEvent cse;

	@Inject
	private ConfigurationService configurationService;

	/**
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		this.sce = sce;
		exposeMilestoneFeatEnabled(sce);

	}

	private synchronized void exposeMilestoneFeatEnabled(ServletContextEvent sce) {
		if (this.sce == null || this.cse == null) {
			return;
		}
		// NOTE: should return when there is no value
		boolean enabled = configurationService.getBoolean(MILESTONE_FEATURE_ENABLED);
		LOGGER.info("Read global configuration param '{}' with param '{}'",
			MILESTONE_FEATURE_ENABLED, enabled);

		sce.getServletContext().setAttribute(MILESTONE_FEATURE_ENABLED_CONTEXT_ATTR, enabled);
	}

	/**
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// NOOP

	}

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {
		this.cse = event;
		exposeMilestoneFeatEnabled(this.sce);
	}
}
