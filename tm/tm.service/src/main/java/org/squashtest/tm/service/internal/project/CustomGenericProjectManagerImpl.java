/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

package org.squashtest.tm.service.internal.project;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.project.AdministrableProject;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.project.ProjectVisitor;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.tm.exception.NoBugTrackerBindingException;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.service.internal.repository.BugTrackerBindingDao;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.testautomation.service.InsecureTestAutomationManagementService;
import org.squashtest.tm.service.project.CustomGenericProjectManager;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.ObjectIdentityService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.security.acls.PermissionGroup;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("CustomGenericProjectManager")
@Transactional
public class CustomGenericProjectManagerImpl implements CustomGenericProjectManager {
	
	private static final String IS_ADMIN_OR_MANAGER = "hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')";
	private static final String IS_ADMIN = "hasRole('ROLE_ADMIN')";
	
	@Inject
	private GenericProjectDao genericProjectDao;
	@Inject
	private BugTrackerBindingDao bugTrackerBindingDao;
	@Inject
	private BugTrackerDao bugTrackerDao;
	@Inject
	private SessionFactory sessionFactory;
	@Inject
	private UserDao userDao;
	@Inject
	private ObjectIdentityService objectIdentityService;
	@Inject
	private Provider<GenericToAdministrableProject> genericToAdministrableConvertor;
	@Inject
	private ProjectsPermissionManagementService permissionsManager;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private InsecureTestAutomationManagementService autotestService;
	@Inject
	private ProjectDeletionHandler projectDeletionHandler;

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomGenericProjectManagerImpl.class);

	/**
	 * @see org.squashtest.tm.service.project.CustomGenericProjectManager#findSortedProjects(org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize(IS_ADMIN_OR_MANAGER)
	public PagedCollectionHolder<List<GenericProject>> findSortedProjects(PagingAndSorting pagingAndSorting, Filtering filter) {
		List<GenericProject> projects;
		
		if (filter.isDefined()){
			projects = genericProjectDao.findProjectsFiltered(pagingAndSorting, "%"+filter.getFilter()+"%");
		}
		else{
			projects = genericProjectDao.findAll(pagingAndSorting);
		}
		
		long count = genericProjectDao.countGenericProjects();
		return new PagingBackedPagedCollectionHolder<List<GenericProject>>(pagingAndSorting, count, projects);
	}

	@Override
	@PreAuthorize(IS_ADMIN)
	public void persist(GenericProject project) {
		Session session = sessionFactory.getCurrentSession();

		CampaignLibrary cl = new CampaignLibrary();
		project.setCampaignLibrary(cl);
		session.persist(cl);

		RequirementLibrary rl = new RequirementLibrary();
		project.setRequirementLibrary(rl);
		session.persist(rl);

		TestCaseLibrary tcl = new TestCaseLibrary();
		project.setTestCaseLibrary(tcl);
		session.persist(tcl);

		session.persist(project);
		session.flush(); // otherwise ids not available

		objectIdentityService.addObjectIdentity(project.getId(), project.getClass());
		objectIdentityService.addObjectIdentity(tcl.getId(), tcl.getClass());
		objectIdentityService.addObjectIdentity(rl.getId(), rl.getClass());
		objectIdentityService.addObjectIdentity(cl.getId(), cl.getClass());

	}

	/**
	 * @see org.squashtest.tm.service.project.CustomGenericProjectManager#coerceTemplateIntoProject(long)
	 */
	@Override	
	@PreAuthorize(IS_ADMIN)
	public void coerceTemplateIntoProject(long templateId) {
		Project project = genericProjectDao.coerceTemplateIntoProject(templateId);

		objectIdentityService.addObjectIdentity(templateId, Project.class);
		permissionsManager.copyAssignedUsersFromTemplate(project, templateId);
		permissionsManager.removeAllPermissionsFromProjectTemplate(templateId);
		objectIdentityService.removeObjectIdentity(templateId, ProjectTemplate.class);
	}

	@Override
	@PreAuthorize(IS_ADMIN_OR_MANAGER)
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

	@Override
	public AdministrableProject findAdministrableProjectById(long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		return genericToAdministrableConvertor.get().convertToAdministrableProject(genericProject);
	}

	private void checkManageProjectOrAdmin(GenericProject genericProject) {
		genericProject.accept(new ProjectVisitor() {

			@Override
			public void visit(ProjectTemplate projectTemplate) {
				PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(
						projectTemplate, "MANAGEMENT"));

			}

			@Override
			public void visit(Project project) {
				PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(project,
						"MANAGEMENT"));

			}
		});

	}

	@Override
	public void addNewPermissionToProject(long userId, long projectId, String permission) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		permissionsManager.addNewPermissionToProject(userId, projectId, permission);
	}

	@Override
	public void removeProjectPermission(long userId, long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		permissionsManager.removeProjectPermission(userId, projectId);

	}

	@Override
	public List<UserProjectPermissionsBean> findUserPermissionsBeansByProject(long projectId) {
		return permissionsManager.findUserPermissionsBeanByProject(projectId);
	}
	
	@Override
	public PagedCollectionHolder<List<UserProjectPermissionsBean>> findUserPermissionsBeanByProject(
			PagingAndSorting sorting, Filtering filtering, long projectId) {
		return permissionsManager.findUserPermissionsBeanByProject(sorting, filtering, projectId);
	}

	@Override
	public List<PermissionGroup> findAllPossiblePermission() {
		return permissionsManager.findAllPossiblePermission();
	}

	@Override
	public List<User> findUserWithoutPermissionByProject(long projectId) {
		return permissionsManager.findUserWithoutPermissionByProject(projectId);
	}

	@Override
	public User findUserByLogin(String userLogin) {
		return userDao.findUserByLogin(userLogin);
	}

	// ********************************** Test automation section *************************************

	@Override
	public void bindTestAutomationProject(long projectId, TestAutomationProject taProject) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		TestAutomationProject persistedProject = autotestService.persistOrAttach(taProject);
		genericProject.bindTestAutomationProject(persistedProject);
	}

	@Override
	public TestAutomationServer getLastBoundServerOrDefault(long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		if (genericProject.hasTestAutomationProjects()) {
			return genericProject.getServerOfLatestBoundProject();
		}

		else {
			return autotestService.getDefaultServer();
		}
	}

	@Override
	public List<TestAutomationProject> findBoundTestAutomationProjects(long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		return genericProjectDao.findBoundTestAutomationProjects(projectId);
	}

	@Override
	public void unbindTestAutomationProject(long projectId, long taProjectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		genericProject.unbindTestAutomationProject(taProjectId);

	}

	// ********************************** bugtracker section *************************************

	@Override
	public void changeBugTracker(long projectId, Long newBugtrackerId) {

		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		BugTracker newBugtracker = bugTrackerDao.findById(newBugtrackerId);
		if (newBugtracker != null) {
			changeBugTracker(project, newBugtracker);
		} else {
			throw new UnknownEntityException(newBugtrackerId, BugTracker.class);
		}

	}

	@Override
	public void changeBugTracker(GenericProject project, BugTracker newBugtracker) {
		LOGGER.debug("changeBugTracker for project " + project.getId() + " bt: " + newBugtracker.getId());
		checkManageProjectOrAdmin(project);
		// the project doesn't have bug-tracker connection yet
		if (!project.isBugtrackerConnected()) {
			BugTrackerBinding bugTrackerBinding = new BugTrackerBinding(project.getName(), newBugtracker, project);
			project.setBugtrackerBinding(bugTrackerBinding);
		}
		// the project has a bug-tracker connection
		else {
			// and the new one is different from the old one
			if (projectBugTrackerChanges(newBugtracker.getId(), project)) {
				project.getBugtrackerBinding().setBugtracker(newBugtracker);
			}
		}
	}

	private boolean projectBugTrackerChanges(Long newBugtrackerId, GenericProject project) {
		boolean change = true;
		BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
		long bugtrackerId = bugtrackerBinding.getBugtracker().getId();
		if (bugtrackerId == newBugtrackerId) {
			change = false;
		}
		return change;
	}

	@Override
	public void removeBugTracker(long projectId) {
		LOGGER.debug("removeBugTracker for project " + projectId);
		GenericProject project = genericProjectDao.findById(projectId);	
		checkManageProjectOrAdmin(project);
		if (project.isBugtrackerConnected()) {
			BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
			project.removeBugTrackerBinding();
			bugTrackerBindingDao.remove(bugtrackerBinding);
		}
	}

	@Override
	public void changeBugTrackerProjectName(long projectId, String projectBugTrackerName) {
		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
		if (bugtrackerBinding == null) {
			throw new NoBugTrackerBindingException();
		}
		bugtrackerBinding.setProjectName(projectBugTrackerName);

	}

}
