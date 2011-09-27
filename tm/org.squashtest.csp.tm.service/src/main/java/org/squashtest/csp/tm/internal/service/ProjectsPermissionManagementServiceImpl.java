/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.core.security.acls.model.ObjectAclService;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectPermission;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.service.ProjectsPermissionManagementService;


@Service("squashtest.tm.service.ProjectsPermissionManagementService")
public class ProjectsPermissionManagementServiceImpl implements
		ProjectsPermissionManagementService {
	
	private static final String NAMESPACE = "squashtest.acl.group.tm";
	private static final String PROJECT_CLASS_NAME = "org.squashtest.csp.tm.domain.project.Project";
	
	private ObjectAclService aclService;
	
	@Inject
	ProjectDao projectDao;
	
	@Inject
	UserDao userDao;
	
	@ServiceReference
	public void setObjectAclService(ObjectAclService aclService) {
		this.aclService = aclService;
	}

	@Override
	public List<PermissionGroup> findAllPossiblePermission() {
		return aclService.findAllPermissionGroupsByNamespace(NAMESPACE);
	}

	@Override
	public void deleteUserProjectOldPermission(String userLogin, long projectId) {
		ObjectIdentity entityRef =  new ObjectIdentityImpl(Project.class, projectId);
		aclService.removeAllResponsibilities(userLogin, entityRef);
	}

	@Override
	public List<ProjectPermission> findProjectPermissionByLogin(String userLogin) {
		List<ProjectPermission> newResult = new ArrayList<ProjectPermission>();
		List<Object[]> result = aclService.retrieveClassAclGroupFromUserLogin(userLogin, PROJECT_CLASS_NAME);
		for (Object[] objects : result) {
			Project project = projectDao.findById((Long) objects[0]);
			newResult.add(new ProjectPermission(project, (PermissionGroup) objects[1]));
		}
		return newResult;
	}

	@Override
	public List<Project> findProjectWithoutPermissionByLogin(String userLogin) {
		List<Long> idList = aclService.findObjectWithoutPermissionByLogin(userLogin, PROJECT_CLASS_NAME);
		if(idList == null || idList.isEmpty()){
			return null;
		}
		return projectDao.findByIdList(idList);
	}

	@Override
	public void addNewPermissionToProject(long userId, long projectId, String permissionName) {
		ObjectIdentity entityRef =  new ObjectIdentityImpl(Project.class, projectId);
		User user = userDao.findById(userId);
		aclService.addNewResponsibility(user.getLogin(), entityRef, permissionName);
	}

	@Override
	public void removeProjectPermission(long userId, long projectId) {
		ObjectIdentity entityRef =  new ObjectIdentityImpl(Project.class, projectId);
		User user = userDao.findById(userId);
		aclService.removeAllResponsibilities(user.getLogin(), entityRef);
	}
}
