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
package org.squashtest.tm.web.internal.controller.users;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.project.ProjectsPermissionFinder;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.web.internal.security.authentication.AuthenticationProviderContext;


@Controller
@RequestMapping("/user-account")
public class UserAccountController {

	private UserAccountService userService;

	@Inject
	private MilestoneManagerService milestoneManager;

	private ProjectsPermissionFinder permissionFinder;

	@Inject
	private AuthenticationProviderContext authenticationProviderContext;

	@ServiceReference
	public void setProjectsPermissionFinderService(ProjectsPermissionFinder permissionFinder) {
		this.permissionFinder = permissionFinder;
	}
	@ServiceReference
	public void setUserAccountService(UserAccountService service){
		this.userService=service;
	}

	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView getUserAccountDetails(){
		User user = userService.findCurrentUser();
		List<Milestone> milestoneList = new ArrayList<>();
		try {
			milestoneList = milestoneManager.findAllVisibleToCurrentUser();
		} catch (Exception e) {
			// TODO: handle exception
		}
		;
		List<ProjectPermission> projectPermissions = permissionFinder.findProjectPermissionByUserLogin(user.getLogin());

		// Sort List
		/*
		 * Collections.sort(milestoneList, new Comparator<Milestone>() {
		 * 
		 * @Override public int compare(Milestone m1, Milestone m2) { return m1.getLabel().compareTo(m2.getLabel()); }
		 * });
		 */
		Collections.sort(milestoneList, new SortMilestoneList());

		ModelAndView mav = new ModelAndView("page/users/user-account");
		mav.addObject("user", user);
		mav.addObject("milestoneList", milestoneList);
		mav.addObject("projectPermissions", projectPermissions);
		return mav;

	}

	@RequestMapping(value="/update", method=RequestMethod.POST, params={"oldPassword", "newPassword"})
	@ResponseBody
	public void changePassword(@ModelAttribute @Valid PasswordChangeForm form){
		userService.setCurrentUserPassword(form.getOldPassword(), form.getNewPassword());
	}

	@RequestMapping(value="/update", method=RequestMethod.POST, params={"id=user-account-email", VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String updateUserEmail(@RequestParam(VALUE) String email){
		userService.setCurrentUserEmail(email);
		return HtmlUtils.htmlEscape(email);
	}


	@ModelAttribute("authenticationProvider")
	AuthenticationProviderFeatures getAuthenticationProviderModelAttribute() {
		return authenticationProviderContext.getCurrentProviderFeatures();
	}

	public class SortMilestoneList implements Comparator<Milestone> {
		@Override
		public int compare(Milestone m1, Milestone m2) {
			return m1.getLabel().compareTo(m2.getLabel());
		}
	}
}
