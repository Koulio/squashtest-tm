/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.requirement;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.helper.VerifiedRequirementActionSummaryBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * Controller for verified requirements management page.
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
public class VerifyingTestCaseManagerController {

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder;

	@Inject
	private InternationalizationHelper i18nHelper;

	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManager;

	@Inject
	private RequirementVersionManagerService requirementVersionFinder;

	/*
	 * Kind of a hack : we rely on mDataProp sent by squash table. IndexBasedMapper looks up into mataProps unmarchalled
	 * as a Map<String, String>. The found value is used as a key in a Map<Long, Object>, so it breaks.
	 * 
	 * So we use a named-base with column indexes as names.
	 */
	private final DatatableMapper<String> verifyingTcMapper = new NameBasedMapper(6)
	.mapAttribute("project-name", "name", Project.class)
	.mapAttribute("tc-reference", "reference", TestCase.class)
	.mapAttribute("tc-name", "name", TestCase.class)
	.mapAttribute("tc-type", "executionMode", TestCase.class);


	@RequestMapping(value = "/requirement-versions/{requirementVersionId}/verifying-test-cases/manager", method = RequestMethod.GET)
	public String showManager(@PathVariable long requirementVersionId, Model model, @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		RequirementVersion requirementVersion = requirementVersionFinder.findById(requirementVersionId);
		List<TestCaseLibrary> linkableLibraries = verifyingTestCaseManager.findLinkableTestCaseLibraries();
		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries, openedNodes);
		DefaultPagingAndSorting pas = new DefaultPagingAndSorting("Project.name");
		DataTableModel verifyingTCModel = buildVerifyingTestCaseModel(requirementVersionId, pas, "");


		model.addAttribute("requirement", requirementVersion.getRequirement()); // this is done because of RequirementViewInterceptor
		model.addAttribute("requirementVersion", requirementVersion);
		model.addAttribute("linkableLibrariesModel", linkableLibrariesModel);
		model.addAttribute("verifyingTestCaseModel", verifyingTCModel);

		return "page/requirements/show-verifying-testcase-manager";
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries, String[] openedNodes) {
		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);

		DriveNodeBuilder<TestCaseLibraryNode> nodeBuilder = driveNodeBuilder.get();

		return new JsTreeNodeListBuilder<TestCaseLibrary>(nodeBuilder)
				.expand(expansionCandidates)
				.setModel(linkableLibraries)
				.build();

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/requirement-versions/{requirementVersionId}/verifying-test-cases/{testCaseIds}", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> addVerifyingTestCasesToRequirement(@PathVariable("testCaseIds") List<Long> testCasesIds, @PathVariable long requirementVersionId) {
		Map<String, Collection<?>> rejectionsAndIds =
				verifyingTestCaseManager.addVerifyingTestCasesToRequirementVersion(testCasesIds, requirementVersionId);
		Collection<VerifiedRequirementException> rejections = (Collection<VerifiedRequirementException>) rejectionsAndIds.get(VerifyingTestCaseManagerService.REJECTION_KEY);
		Collection<Long> ids = (Collection<Long>) rejectionsAndIds.get(VerifyingTestCaseManagerService.IDS_KEY);
		Map<String, Object>  result = buildSummary(rejections);
		result.put("linkedIds" , ids);
		return result;
	}

	private Map<String, Object> buildSummary(Collection<VerifiedRequirementException> rejections) {
		return VerifiedRequirementActionSummaryBuilder.buildAddActionSummary(rejections);
	}

	@RequestMapping(value = "/requirement-versions/{requirementVersionId}/verifying-test-cases/{testCaseIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeVerifyingTestCaseFromRequirement(@PathVariable("requirementVersionId") long requirementVersionId,
			@PathVariable("testCaseIds") List<Long> testCaseIds ) {
		verifyingTestCaseManager.removeVerifyingTestCasesFromRequirementVersion(testCaseIds, requirementVersionId);
	}


	@RequestMapping(value = "/requirement-versions/{requirementVersionId}/verifying-test-cases/table", params = RequestParams.S_ECHO_PARAM)
	public @ResponseBody
	DataTableModel getVerifiedTestCasesTableModel(@PathVariable long requirementVersionId,
			DataTableDrawParameters params) {

		PagingAndSorting filter = new DataTableSorting(params, verifyingTcMapper);

		return buildVerifyingTestCaseModel(requirementVersionId, filter, params.getsEcho());

	}

	protected DataTableModel buildVerifyingTestCaseModel(long requirementVersionId, PagingAndSorting pas, String sEcho){
		PagedCollectionHolder<List<TestCase>> holder = verifyingTestCaseManager.findAllByRequirementVersion(
				requirementVersionId, pas);

		return new VerifyingTestCasesTableModelHelper(i18nHelper).buildDataModel(holder, sEcho);
	}




}
