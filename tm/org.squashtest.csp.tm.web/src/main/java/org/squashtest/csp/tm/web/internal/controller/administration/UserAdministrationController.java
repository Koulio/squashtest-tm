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
package org.squashtest.csp.tm.web.internal.controller.administration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.tm.domain.audit.AuditableMixin;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectPermission;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UsersGroup;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.AdministrationService;
import org.squashtest.csp.tm.service.ProjectsPermissionManagementService;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTablePagedFilter;

@Controller
@RequestMapping("/users")
public class UserAdministrationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserAdministrationController.class);
	
	private AdministrationService adminService;
	private ProjectsPermissionManagementService permissionService;
	
	@Inject
	private MessageSource messageSource;

	@ServiceReference
	public void setAdministrationService(AdministrationService adminService){
		this.adminService = adminService;
	}
	
	@ServiceReference
	public void setProjectsPermissionManagementService(ProjectsPermissionManagementService permissionService){
		this.permissionService = permissionService;
	}
	
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public ModelAndView getUserList() {
		
		ModelAndView mav = new ModelAndView("page/users/users-list");
		List<UsersGroup> list = adminService.findAllUsersGroup();
		mav.addObject("usersGroupList", list);
		return mav;
	}

	@RequestMapping(value="/add", method=RequestMethod.POST)
	public @ResponseBody void addNewUser(@Valid @ModelAttribute("add-user") User user, @RequestParam long groupId, @Valid @RequestParam String password){
		adminService.addUser(user, groupId, password);
	}
	
	
	@RequestMapping(value="/table", method=RequestMethod.GET)
	public @ResponseBody DataTableModel getTable(final DataTableDrawParameters params, final Locale locale) {
		LOGGER.trace("TestCaseModificationController: getStepsTableModel called ");

		CollectionFilter filter = createCollectionFilter(params);

		FilteredCollectionHolder<List<User>> holder = adminService.findAllUsersFiltered(filter);
		
		
		return new DataTableModelHelper<User>(){
			@Override
			public Object[] buildItemData(User item) {
				AuditableMixin newP = (AuditableMixin) item;
				return new Object[] { item.getId(),
						getCurrentIndex(),
						item.getLogin(),
						item.getFirstName(),
						item.getLastName(),
						item.getEmail(),
						formatDate(newP.getCreatedOn(), locale),
						formatString(newP.getCreatedBy(), locale),
						formatDate(newP.getLastModifiedOn(), locale),
						formatString(newP.getLastModifiedBy(), locale)};
			}
		}.buildDataModel(holder, filter.getFirstItemIndex()+1, params.getsEcho());
	}
	
	private CollectionFilter createCollectionFilter(final DataTableDrawParameters params) {
		return new DataTablePagedFilter(params);
	}
	
	@RequestMapping(value="/{userId}/info", method=RequestMethod.GET)
	public ModelAndView getUserInfos(@PathVariable long userId){
		User user = adminService.findUserById(userId);
		List<UsersGroup> usersGroupList = adminService.findAllUsersGroup();
		ModelAndView mav = new ModelAndView("page/users/user-info");	
		mav.addObject("usersGroupList", usersGroupList);
		mav.addObject("user", user);
		return mav;
	}
	
	@RequestMapping(value="/{userId}/change-group", method=RequestMethod.POST)
	public @ResponseBody void changeUserGroup(@PathVariable long userId, @RequestParam long groupId){
		adminService.setUserGroupAuthority(userId, groupId);
	}
	
	@RequestMapping(value = "/{userId}" ,method = RequestMethod.POST, params = { "id=user-login", "value" })
	@ResponseBody
	public String updateLogin(@RequestParam("value") String userLogin, @PathVariable long userId) {
		adminService.modifyUserLogin(userId, userLogin);
		return userLogin;
	}
	
	@RequestMapping(value = "/{userId}" ,method = RequestMethod.POST, params = { "id=user-first-name", "value" })
	@ResponseBody
	public String updateFirstName(@RequestParam("value") String firstName, @PathVariable long userId) {
		adminService.modifyUserFirstName(userId, firstName);
		return firstName;
	}
	
	@RequestMapping(value = "/{userId}" ,method = RequestMethod.POST, params = { "id=user-last-name", "value" })
	@ResponseBody
	public String updateLastName(@RequestParam("value") String lastName, @PathVariable long userId) {
		adminService.modifyUserLastName(userId, lastName);
		return lastName;
	}
	
	@RequestMapping(value = "/{userId}" ,method = RequestMethod.POST, params = { "id=user-email", "value" })
	@ResponseBody
	public String updateEmail(@RequestParam("value") String email, @PathVariable long userId) {
		adminService.modifyUserEmail(userId, email);
		return email;
	}
	
	//*********************************************************************************
	@RequestMapping(value="/{userId}/add-permission", method=RequestMethod.POST)
	public @ResponseBody void addNewPermission(@RequestParam("project") long projectId, @PathVariable long userId, @RequestParam String permission){
		permissionService.addNewPermissionToProject(userId, projectId, permission);
	}
	
	@RequestMapping(value="/{userId}/remove-permission", method=RequestMethod.POST)
	public @ResponseBody void removePermission(@RequestParam("project") long projectId, @PathVariable long userId){
		permissionService.removeProjectPermission(userId, projectId);
	}
	
	@RequestMapping(value = "/{userId}/permission-popup" ,method = RequestMethod.GET)
	public ModelAndView getPermissionPopup(@PathVariable long userId) {
		User user = adminService.findUserById(userId);
		List<PermissionGroup> permissionList = permissionService.findAllPossiblePermission();
		List<Project> projectList = permissionService.findProjectWithoutPermissionByLogin(user.getLogin());
		
		ModelAndView mav = new ModelAndView("fragment/users/user-permission-popup");
		mav.addObject("user", user);
		mav.addObject("projectList", projectList);
		mav.addObject("permissionList", permissionList);
		return mav;
	}
	
	@RequestMapping(value = "/{userId}/permission-table" ,method = RequestMethod.GET)
	public ModelAndView getPermissionTable(@PathVariable long userId) {
		User user = adminService.findUserById(userId);
		List<ProjectPermission> projectPermissions = permissionService.findProjectPermissionByLogin(user.getLogin());
		List<PermissionGroup> permissionList = permissionService.findAllPossiblePermission();
		
		ModelAndView mav = new ModelAndView("fragment/users/user-permission-table");
		mav.addObject("user", user);
		mav.addObject("permissionList", permissionList);
		mav.addObject("projectPermissionList", projectPermissions);
		return mav;
	}

	private String formatString(String arg, Locale locale){
		if (arg==null){
			return formatNoData(locale);
		} else {
			return arg;
		}
	}

	private String formatDate(Date date, Locale locale){
		try{
			String format = messageSource.getMessage("squashtm.dateformat", null, locale);
			return new SimpleDateFormat(format).format(date);
		}
		catch(Exception anyException){
			return formatNoData(locale);
		}

	}
	private String formatNoData(Locale locale){
		return messageSource.getMessage("squashtm.nodata",null, locale);
	}
}
