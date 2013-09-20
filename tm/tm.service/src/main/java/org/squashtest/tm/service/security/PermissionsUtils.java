/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.security;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;

public final class PermissionsUtils {

	private PermissionsUtils(){
		super();
	}
	
	public static final void checkPermission(PermissionEvaluationService permissionService, SecurityCheckableObject... checkableObjects) {
		for (SecurityCheckableObject object : checkableObjects) {
			if (!permissionService
					.hasRoleOrPermissionOnObject("ROLE_ADMIN", object.getPermission(), object.getObject())) {
				throw new AccessDeniedException("Access is denied");
				
			}
		}
	}
	
	public static final void checkPermission(PermissionEvaluationService permissionService, List<Long> ids, String permission, String entityClassName) {
		for (Long entityId : ids) {
			if (!permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", permission, entityId, entityClassName)) {
				throw new AccessDeniedException("Access is denied");
				
			}
		}
	}
}
