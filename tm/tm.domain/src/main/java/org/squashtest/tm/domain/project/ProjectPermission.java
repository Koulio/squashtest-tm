/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.domain.project;

import org.squashtest.tm.security.acls.PermissionGroup;

/**
 * This class represents an agregation of permissions (read, write and so on) which can be given to a user and which
 * have a scope of object identities.
 *
 *	This class is specific to projects permission
 * @author Gregory Fouquet
 *
 */
public class ProjectPermission {

	private GenericProject project;
	private PermissionGroup permissionGroup;
	
	public ProjectPermission(GenericProject project, PermissionGroup permissionGroup) {
		this.project = project;
		this.permissionGroup = permissionGroup;
	}

	public GenericProject getProject() {
		return project;
	}

	public void setProject(GenericProject project) {
		this.project = project;
	}

	public PermissionGroup getPermissionGroup() {
		return permissionGroup;
	}

	public void setPermissionGroup(PermissionGroup permissionGroup) {
		this.permissionGroup = permissionGroup;
	}
}
