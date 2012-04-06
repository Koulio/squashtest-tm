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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UsersGroup;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;

/**
 * 
 * 
 * //TODO : should be in core.service
 * 
 * 
 * Will handle CRUD on Squash user accounts, groups, permissions and the like. For all operations about user
 * authentication, the said operation will be delegated to the UserAuthenticationManagerService.
 * 
 * Security should ensure that :
 * 
 * - access to user informations (both reading and writing) are opened to the said user ROLE_ADMIN authority only, - the
 * rest requires ROLE_ADMIN authority only.
 * 
 * @author bsiri
 * 
 */

public interface AdministrationService {

	/* ************** User administration section ******************* */

	/* ** accessible both by admin and the current user ** */

	List<Project> findAllProjects();

	void modifyUserFirstName(long userId, String newName);

	void modifyUserLastName(long userId, String newName);

	void modifyUserLogin(long userId, String newLogin);

	void modifyUserEmail(long userId, String newEmail);

	/* ** now its admin only ** */

	User findUserById(long userId);

	List<User> findAllUsers();

	FilteredCollectionHolder<List<User>> findAllUsersFiltered(CollectionFilter filter);

	List<UsersGroup> findAllUsersGroup();

	void addUser(User aUser, long groupId, String password);

	void modifyUserActiveParam(long userId, boolean active);

	void setUserGroupAuthority(long userId, long groupId);

	void modifyWelcomeMessage(String welcomeMessage);

	String findWelcomeMessage();

	void checkLoginAvailability(String login);

	void modifyLoginMessage(String loginMessage);

	String findLoginMessage();
}
