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
package org.squashtest.csp.tm.web.internal.controller.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.service.RequirementVersionManagerService;
import org.squashtest.csp.tm.service.VerifyingTestCaseManagerService;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;

/**
 * Controller for verified requirements management page.
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
public class VerifyingTestCaseManagerController {
	private static final Logger LOGGER = LoggerFactory.getLogger(VerifyingTestCaseManagerController.class);
	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";

	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;

	@Inject
	private MessageSource messageSource;

	private VerifyingTestCaseManagerService verifyingTestCaseManager;
	private RequirementVersionManagerService requirementVersionFinder;

	private final DataTableMapper verifyingTcMapper = new DataTableMapper("verifying-test-cases", TestCase.class,
			Project.class).initMapping(5).mapAttribute(Project.class, 2, "name", String.class)
			.mapAttribute(TestCase.class, 3, "name", String.class)
			.mapAttribute(TestCase.class, 4, "executionMode", TestCaseExecutionMode.class);

	@ServiceReference
	public void setVerifyingTestCaseManager(VerifyingTestCaseManagerService verifyingTestCaseManagerService) {
		this.verifyingTestCaseManager = verifyingTestCaseManagerService;
	}

	@ServiceReference
	public void setRequirementVersionFinder(RequirementVersionManagerService requirementVersionManagerService) {
		this.requirementVersionFinder = requirementVersionManagerService;
	}

	@RequestMapping(value = "/requirement-versions/{requirementVersionId}/verifying-test-cases/manager", method = RequestMethod.GET)
	public String showManager(@PathVariable long requirementVersionId, Model model) {
		RequirementVersion requirementVersion = requirementVersionFinder.findById(requirementVersionId);
		List<TestCaseLibrary> linkableLibraries = verifyingTestCaseManager.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries);

		model.addAttribute("requirementVersion", requirementVersion);
		model.addAttribute("linkableLibrariesModel", linkableLibrariesModel);

		return "page/requirements/show-verifying-testcase-manager";
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries) {
		DriveNodeBuilder builder = driveNodeBuilder.get();

		List<JsTreeNode> linkableLibrariesModel = new ArrayList<JsTreeNode>();

		for (TestCaseLibrary library : linkableLibraries) {
			JsTreeNode libraryNode = builder.setModel(library).build();
			linkableLibrariesModel.add(libraryNode);
		}
		return linkableLibrariesModel;
	}

	@RequestMapping(value = "/requirement-versions/{requirementVersionId}/verifying-test-cases", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addVerifyingTestCasesToRequirement(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long requirementVersionId) {
		verifyingTestCaseManager.addVerifyingTestCasesToRequirementVersion(testCasesIds, requirementVersionId);
	}

	@RequestMapping(value = "/requirement-versions/{requirementVersionId}/non-verifying-test-cases", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void removeVerifyingTestCasesFromRequirement(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long requirementVersionId) {
		verifyingTestCaseManager.removeVerifyingTestCasesFromRequirementVersion(testCasesIds, requirementVersionId);
	}

	@RequestMapping(value = "/requirement-versions/{requirementVersionId}/verifying-test-cases/{testCaseId}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeVerifyingTestCaseFromRequirement(@PathVariable long testCaseId, @PathVariable long requirementVersionId) {
		verifyingTestCaseManager.removeVerifyingTestCaseFromRequirementVersion(testCaseId, requirementVersionId);
	}

	@RequestMapping(value = "/requirement-versions/{requirementVersionId}/verifying-test-cases/table", params = "sEcho")
	public @ResponseBody
	DataTableModel getVerifiedTestCasesTableModel(@PathVariable long requirementVersionId,
			DataTableDrawParameters params, Locale locale) {
		PagingAndSorting filter = new DataTableMapperPagingAndSortingAdapter(params, verifyingTcMapper);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ReqModController : filterin " + params.getsSortDir_0() + " for "
					+ verifyingTcMapper.pathAt(params.getiSortCol_0()));
		}

		PagedCollectionHolder<List<TestCase>> holder = verifyingTestCaseManager.findAllByRequirementVersion(
				requirementVersionId, filter);

		return buildVerifyingTestCasesTableModel(holder, params.getsEcho(), locale);
	}

	private DataTableModel buildVerifyingTestCasesTableModel(PagedCollectionHolder<List<TestCase>> holder,
			String sEcho, Locale locale) {
		DataTableModel model = new DataTableModel(sEcho);
		String type = "";
		List<TestCase> testCases = holder.getPagedItems();

		for (int i = 0; i < testCases.size(); i++) {
			TestCase tc = testCases.get(i);

			type = formatExecutionMode(tc.getExecutionMode(), locale);

			model.addRow(new Object[] { tc.getId(), holder.getFirstItemIndex() + i + 1, tc.getProject().getName(),
					tc.getName(), type, "" });
		}

		model.displayRowsFromTotalOf(holder.getTotalNumberOfItems());
		return model;
	}

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return messageSource.getMessage(mode.getI18nKey(), null, locale);
	}

}
