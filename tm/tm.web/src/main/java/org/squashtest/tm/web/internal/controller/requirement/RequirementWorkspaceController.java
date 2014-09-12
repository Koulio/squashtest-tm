/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.requirement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;
import org.squashtest.tm.web.internal.helper.InternationalizableComparator;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;

@Controller
@RequestMapping("/requirement-workspace")
public class RequirementWorkspaceController extends WorkspaceController<RequirementLibraryNode<?>> {
	@Inject
	@Named("squashtest.tm.service.RequirementsWorkspaceService")
	private WorkspaceService<Library<RequirementLibraryNode<?>>> workspaceService;

	@Inject
	@Named("requirement.driveNodeBuilder")
	private Provider<DriveNodeBuilder<RequirementLibraryNode<?>>> driveNodeBuilderProvider; 

	
	@Inject
	private RequirementLibraryNavigationService requirementLibraryNavigationService;
	
	
	@Override
	protected WorkspaceService<Library<RequirementLibraryNode<?>>> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "page/requirement-workspace";
	}
	
	@Override
	protected void populateModel(Model model, Locale locale) {
		
		List<Library<RequirementLibraryNode<?>>> libraries = workspaceService.findAllImportableLibraries();
		List<RequirementCategory> categories = sortCategories();
		
		model.addAttribute("editableLibraries", libraries);
		model.addAttribute("categories", categories);
		
		
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceType()
	 */
	protected WorkspaceType getWorkspaceType() {
		return WorkspaceType.REQUIREMENT_WORKSPACE;
	}
	
	private List<RequirementCategory> sortCategories(){
		InternationalizableComparator comparator = new InternationalizableComparator(getI18nHelper());
		List<RequirementCategory> categories = Arrays.asList(RequirementCategory.values());
		Collections.sort(categories, comparator);
		return categories;
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#driveNodeBuilderProvider()
	 */
	@Override
	protected Provider<DriveNodeBuilder<RequirementLibraryNode<?>>> driveNodeBuilderProvider() {
		return driveNodeBuilderProvider;
	}

	@Override
	protected String[] getNodeParentsInWorkspace(Long elementId) {
		List<String> parents = requirementLibraryNavigationService.getParentNodesAsStringList(elementId);
		return parents.toArray(new String[parents.size()]); 
	}

	@Override
	protected String getTreeElementIdInWorkspace(Long elementId) {
		return "Requirement-"+elementId;
	}
	
}
