/**
 * This file is part of the Squashtest platform.
 * Copyright (C) 2010 - 2015 Henix, henix.fr
 * <p/>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.interceptor.openedentity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.inject.Inject;
import javax.servlet.ServletContext;

/**
 * Groups mutual code to store the information of an access to the view of an entity in the OpenedEntities stored in the ServeletContext.
 * see {@linkplain OpenedEntities}
 *
 * @author mpagnon
 */
public abstract class ObjectViewsInterceptor implements WebRequestInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectViewsInterceptor.class);

	@Inject
	protected ServletContext context;

	@Inject
	private PermissionEvaluationService permissionService;

	protected final boolean addViewerToEntity(String contextAttributeName, Identified object, String userLogin) {
		LOGGER.debug("New view added for {} = {}  Viewer = {}", new Object[]{contextAttributeName, object.getId(), userLogin});
		boolean otherViewers = false;
		if (permissionService.hasMoreThanRead(object)) {
			LOGGER.debug("User has more than readonly in object = true");

			OpenedEntities openedEntities = (OpenedEntities) context.getAttribute(contextAttributeName);
			if (openedEntities != null) {
				otherViewers = openedEntities.addViewerToEntity(object, userLogin);

			}
		} else {
			LOGGER.debug("User has more than readonly in object = false");
		}

		return otherViewers;
	}


}
