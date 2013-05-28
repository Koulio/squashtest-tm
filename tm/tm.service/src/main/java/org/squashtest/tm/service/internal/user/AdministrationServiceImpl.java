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
package org.squashtest.tm.service.internal.user;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.AdministrationStatistics;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup;
import org.squashtest.tm.exception.user.LoginAlreadyExistsException;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.service.internal.repository.AdministrationDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TeamDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.repository.UsersGroupDao;
import org.squashtest.tm.service.internal.security.UserBuilder;
import org.squashtest.tm.service.security.AdministratorAuthenticationService;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.service.user.AuthenticatedUser;
import org.squashtest.tm.service.user.UserAccountService;

/**
 * 
 * @author bsiri
 * 
 */
@Service("squashtest.tm.service.AdministrationService")
@Transactional
public class AdministrationServiceImpl implements AdministrationService {

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private UserDao userDao;

	@Inject
	private UsersGroupDao groupDao;

	@Inject
	private AdministrationDao adminDao;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private TeamDao teamDao;

	@Inject
	private AdministratorAuthenticationService adminAuthentService;

	private final static String WELCOME_MESSAGE_KEY = "WELCOME_MESSAGE";
	private final static String LOGIN_MESSAGE_KEY = "LOGIN_MESSAGE";

	public void setAdministratorAuthenticationService(AdministratorAuthenticationService adminService) {
		this.adminAuthentService = adminService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	/* **************** delegate user section, so is security ************ */

	@Override
	public void modifyUserFirstName(long userId, String newName) {
		userAccountService.modifyUserFirstName(userId, newName);
	}

	@Override
	public void modifyUserLastName(long userId, String newName) {
		userAccountService.modifyUserLastName(userId, newName);
	}

	@Override
	public void modifyUserLogin(long userId, String newLogin) {
		userAccountService.modifyUserLogin(userId, newLogin);
	}

	@Override
	public void modifyUserEmail(long userId, String newEmail) {
		userAccountService.modifyUserEmail(userId, newEmail);
	}

	/* ********************** proper admin section ******************* */
	private static final String HAS_ROLE_ADMIN = "hasRole('ROLE_ADMIN')";

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public AuthenticatedUser findUserById(long userId) {
		User user = userDao.findById(userId);
		boolean hasAuth = adminAuthentService.userExists(user.getLogin());
		return new AuthenticatedUser(user, hasAuth);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<User> findAllUsersOrderedByLogin() {
		return userDao.findAllUsersOrderedByLogin();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<User> findAllActiveUsersOrderedByLogin() {
		return userDao.findAllActiveUsersOrderedByLogin();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public FilteredCollectionHolder<List<User>> findAllActiveUsersFiltered(PagingAndSorting sorter, Filtering filter) {
		List<User> list = userDao.findAllActiveUsers(sorter, filter);
		long count = userDao.findAll().size();
		return new FilteredCollectionHolder<List<User>>(count, list);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<UsersGroup> findAllUsersGroupOrderedByQualifiedName() {
		return groupDao.findAllGroupsOrderedByQualifiedName();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void addUser(User user, long groupId, String password) {
		// FIXME : check the auth login is available when time has come
		createUserWithoutCredentials(user, groupId);
		adminAuthentService.createNewUserPassword(user.getLogin(), password, user.getActive(), true, true, true,
				new ArrayList<GrantedAuthority>());
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void setUserGroupAuthority(long userId, long groupId) {
		UsersGroup group = groupDao.findById(groupId);
		User user = userDao.findById(userId);
		user.setGroup(group);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deactivateUser(long userId) {
		userAccountService.deactivateUser(userId);
		User user = userDao.findById(userId);
		adminAuthentService.deactivateAccount(user.getLogin());
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void activateUser(long userId) {
		userAccountService.activateUser(userId);
	}

	@Override
	public List<Project> findAllProjects() {
		return projectDao.findAll();
	}

	@Override
	public void modifyWelcomeMessage(String welcomeMessage) {
		if (configurationService.findConfiguration(WELCOME_MESSAGE_KEY) == null) {
			configurationService.createNewConfiguration(WELCOME_MESSAGE_KEY, welcomeMessage);
			return;
		}
		configurationService.updateConfiguration(WELCOME_MESSAGE_KEY, welcomeMessage);
	}

	@Override
	public void modifyLoginMessage(String loginMessage) {
		if (configurationService.findConfiguration(LOGIN_MESSAGE_KEY) == null) {
			configurationService.createNewConfiguration(LOGIN_MESSAGE_KEY, loginMessage);
			return;
		}
		configurationService.updateConfiguration(LOGIN_MESSAGE_KEY, loginMessage);
	}

	@Override
	public String findWelcomeMessage() {
		return configurationService.findConfiguration(WELCOME_MESSAGE_KEY);
	}

	@Override
	public String findLoginMessage() {
		return configurationService.findConfiguration(LOGIN_MESSAGE_KEY);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void resetUserPassword(long userId, String newPassword) {
		User user = userDao.findById(userId);
		adminAuthentService.resetUserPassword(user.getLogin(), newPassword);
	}

	/**
	 * @see AdministrationService#findAdministrationStatistics()
	 */
	@Override
	public AdministrationStatistics findAdministrationStatistics() {
		return adminDao.findAdministrationStatistics();
	}

	/**
	 * @see AdministrationService#deassociateTeams(long, List)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deassociateTeams(long userId, List<Long> teamIds) {
		User user = userDao.findById(userId);
		user.removeTeams(teamIds);
	}

	/**
	 * @see AdministrationService#associateToTeams(long, List)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void associateToTeams(long userId, List<Long> teamIds) {
		User user = userDao.findById(userId);
		List<Team> teams = teamDao.findAllByIds(teamIds);
		for (Team team : teams) {
			team.addMember(user);
			user.addTeam(team);
		}

	}

	/**
	 * @see AdministrationService#findSortedAssociatedTeams(long, PagingAndSorting, Filtering)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<Team>> findSortedAssociatedTeams(long userId, PagingAndSorting paging,
			Filtering filtering) {
		List<Team> associatedTeams = teamDao.findSortedAssociatedTeams(userId, paging, filtering);
		long associatedTeamsTotal = teamDao.countAssociatedTeams(userId);
		return new PagingBackedPagedCollectionHolder<List<Team>>(paging, associatedTeamsTotal, associatedTeams);

	}

	/**
	 * @see AdministrationService#findAllNonAssociatedTeams(long)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<Team> findAllNonAssociatedTeams(long userId) {
		return teamDao.findAllNonAssociatedTeams(userId);
	}

	/**
	 * This is not secured on purpose.
	 * 
	 * @see org.squashtest.tm.service.user.AdministrationService#findByLogin(java.lang.String)
	 */
	@Override
	public User findByLogin(String login) {
		return userDao.findUserByLogin(login);
	}

	/**
	 * @see org.squashtest.tm.service.user.AdministrationService#createUserFromLogin(java.lang.String)
	 */
	@Override
	public User createUserFromLogin(@NotNull String login) throws LoginAlreadyExistsException {
		if (findByLogin(login) != null) {
			throw new LoginAlreadyExistsException("User " + login + " cannot be created because it already exists");
		}

		User user = User.createFromLogin(login);
		UsersGroup defaultGroup = groupDao.findByQualifiedName("squashtest.tm.group.User");
		user.setGroup(defaultGroup);

		userDao.persist(user);
		return user;
	}

	/**
	 * @see org.squashtest.tm.service.user.AdministrationService#createUserWithoutCredentials(org.squashtest.tm.domain.users.User,
	 *      long)
	 */
	@Override
	public void createUserWithoutCredentials(User user, long groupId) {
		userDao.checkLoginAvailability(user.getLogin());

		UsersGroup group = groupDao.findById(groupId);

		user.setGroup(group);
		userDao.persist(user);
	}

	/**
	 * @see org.squashtest.tm.service.user.AdministrationService#createAuthentication(long, java.lang.String)
	 */
	@Override
	public void createAuthentication(long userId, String password) throws LoginAlreadyExistsException {
		User user = userDao.findById(userId);

		if (!adminAuthentService.userExists(user.getLogin())) {
			UserDetails auth = UserBuilder.forUser(user.getLogin()).password(password).active(user.getActive()).build();
			adminAuthentService.createUser(auth);

		} else {
			throw new LoginAlreadyExistsException("Authentication data for user '" + user.getLogin()
					+ "' already exists");
		}

	}
}
