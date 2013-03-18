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
package org.squashtest.tm.web.internal.controller.testcase;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.service.testcase.TestStepModificationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.helper.VerifiedRequirementActionSummaryBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter.SortedAttributeSource;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
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
public class VerifiedRequirementsManagerController {
	/**
	 * 
	 */

	private static final String REQUIREMENTS_IDS = "requirementsIds[]";

	@Inject
	private InternationalizationHelper internationalizationHelper;
	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;
	@Inject
	private TestCaseModificationService testCaseModificationService;
	@Inject
	private TestStepModificationService testStepService;
	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;
	@Inject
	private RequirementLibraryFinderService requirementLibraryFinder;

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/manager", method = RequestMethod.GET)
	public String showTestCaseManager(@PathVariable long testCaseId, Model model) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel();
		
		model.addAttribute("testCase", testCase);
		model.addAttribute("linkableLibrariesModel", linkableLibrariesModel);
		
		return "page/test-cases/show-verified-requirements-manager";
	}

	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirement-versions/manager", method = RequestMethod.GET)
	public String showTestStepManager(@PathVariable long testStepId, Model model) {
		TestStep testStep = testStepService.findById(testStepId);
		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel();
		
		model.addAttribute("testStep", testStep);
		model.addAttribute("linkableLibrariesModel", linkableLibrariesModel);
		
		return "page/test-cases/show-step-verified-requirements-manager";
	}

	private List<JsTreeNode> createLinkableLibrariesModel() {
		List<RequirementLibrary> linkableLibraries = requirementLibraryFinder.findLinkableRequirementLibraries();
		DriveNodeBuilder builder = driveNodeBuilder.get();
		List<JsTreeNode> linkableLibrariesModel = new ArrayList<JsTreeNode>();

		for (RequirementLibrary library : linkableLibraries) {
			JsTreeNode libraryNode = builder.setModel(library).build();
			linkableLibrariesModel.add(libraryNode);
		}
		return linkableLibrariesModel;
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirements", method = RequestMethod.POST, params = REQUIREMENTS_IDS)
	public @ResponseBody
	Map<String, Object> addVerifiedRequirementsToTestCase(@RequestParam(REQUIREMENTS_IDS) List<Long> requirementsIds,
			@PathVariable long testCaseId) {
		Collection<VerifiedRequirementException> rejections = verifiedRequirementsManagerService
				.addVerifiedRequirementsToTestCase(requirementsIds, testCaseId);

		return buildSummary(rejections);

	}

	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirements", method = RequestMethod.POST, params = REQUIREMENTS_IDS)
	public @ResponseBody
	Map<String, Object> addVerifiedRequirementsToTestStep(@RequestParam(REQUIREMENTS_IDS) List<Long> requirementsIds,
			@PathVariable long testStepId) {
		Collection<VerifiedRequirementException> rejections = verifiedRequirementsManagerService
				.addVerifiedRequirementsToTestStep(requirementsIds, testStepId);

		return buildSummary(rejections);

	}

	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirement-versions/{requirementVersionId}", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> addVerifiedRequirementToTestStep(@PathVariable long requirementVersionId,
			@PathVariable long testStepId) {
		Collection<VerifiedRequirementException> rejections = verifiedRequirementsManagerService
				.addVerifiedRequirementsToTestStep(Arrays.asList(requirementVersionId), testStepId);

		return buildSummary(rejections);

	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/{oldVersionId}", method = RequestMethod.POST)
	@ResponseBody
	public int changeVersion(@PathVariable long testCaseId, @PathVariable long oldVersionId,
			@RequestParam(VALUE) long newVersionId) {

		List<Long> oldVersion = new ArrayList<Long>();
		oldVersion.add(oldVersionId);
		List<Long> newVersion = new ArrayList<Long>();
		newVersion.add(newVersionId);

		int newVersionNumber = verifiedRequirementsManagerService.changeVerifiedRequirementVersionOnTestCase(
				oldVersionId, newVersionId, testCaseId);

		return newVersionNumber;
	}

	private Map<String, Object> buildSummary(Collection<VerifiedRequirementException> rejections) {
		return VerifiedRequirementActionSummaryBuilder.buildAddActionSummary(rejections);
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/{requirementVersionsIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeVerifiedRequirementVersionsFromTestCase(@PathVariable List<Long> requirementVersionsIds,
			@PathVariable long testCaseId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementVersionsFromTestCase(requirementVersionsIds,
				testCaseId);

	}

	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirement-versions/{requirementVersionsIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeVerifiedRequirementVersionsFromTestStep(@PathVariable List<Long> requirementVersionsIds,
			@PathVariable long testStepId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementVersionsFromTestStep(requirementVersionsIds,
				testStepId);

	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions", params = {
			RequestParams.S_ECHO_PARAM, "includeCallSteps" })
	@ResponseBody
	public DataTableModel getTestCaseWithCallStepsVerifiedRequirementsTableModel(@PathVariable long testCaseId,
			final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting pas = new DataTableMapperPagingAndSortingAdapter(params, verifiedRequirementVersionsMapper);

		PagedCollectionHolder<List<VerifiedRequirement>> holder = verifiedRequirementsManagerService
				.findAllVerifiedRequirementsByTestCaseId(testCaseId, pas);

		return new TestCaseWithCalledStepsVerifiedRequirementsDataTableModelHelper(locale, internationalizationHelper)
				.buildDataModel(holder, params.getsEcho());

	}

	private static final class TestCaseWithCalledStepsVerifiedRequirementsDataTableModelHelper extends
			TestCaseVerifiedRequirementsDataTableModelHelper {

		public TestCaseWithCalledStepsVerifiedRequirementsDataTableModelHelper(Locale locale,
				InternationalizationHelper internationalizationHelper) {
			super(locale, internationalizationHelper);
		}

		@Override
		public Map<String, Object> buildItemData(VerifiedRequirement item) {
			Map<String, Object> resMap = super.buildItemData(item);
			resMap.put("directlyVerified", item.isDirectVerification());
			return resMap;
		}
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTestCaseVerifiedRequirementsTableModel(@PathVariable long testCaseId,
			final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting pagingAndSorting = new DataTableMapperPagingAndSortingAdapter(params,
				verifiedRequirementVersionsMapper);

		PagedCollectionHolder<List<VerifiedRequirement>> holder = verifiedRequirementsManagerService
				.findAllDirectlyVerifiedRequirementsByTestCaseId(testCaseId, pagingAndSorting);

		return new TestCaseVerifiedRequirementsDataTableModelHelper(locale, internationalizationHelper).buildDataModel(
				holder, params.getsEcho());
	}

	/**
	 * gets the table model for step's verified requirement versions.
	 * 
	 * @param params
	 *            : the {@link DataTableDrawParameters}
	 * @param testStepId
	 *            : the id of the concerned {@link TestStep}
	 * @return a {@link DataTableModel} for the table of verified {@link RequirementVersion}
	 */
	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirement-versions", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTestStepVerifiedRequirementTableModel(DataTableDrawParameters params,
			@PathVariable long testStepId) {
		PagingAndSorting paging = new DataTableMapperPagingAndSortingAdapter(params, verifiedRequirementVersionsMapper);
		Locale locale = LocaleContextHolder.getLocale();
		PagedCollectionHolder<List<VerifiedRequirement>> holder = verifiedRequirementsManagerService
				.findAllDirectlyVerifiedRequirementsByTestStepId(testStepId, paging);
		;
		return new TestStepVerifiedRequirementsDataTableModelHelper(locale, internationalizationHelper, testStepId)
				.buildDataModel(holder, params.getsEcho());
	}

	private static class VerifiedRequirementsDataTableModelHelper extends DataTableModelHelper<VerifiedRequirement> {
		private InternationalizationHelper internationalizationHelper;
		private Locale locale;

		private VerifiedRequirementsDataTableModelHelper(Locale locale,
				InternationalizationHelper internationalizationHelper) {
			this.locale = locale;
			this.internationalizationHelper = internationalizationHelper;
		}

		@Override
		public Map<String, Object> buildItemData(VerifiedRequirement item) {
			Map<String, Object> res = new HashMap<String, Object>();
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("name", item.getName());
			res.put("project", item.getProject().getName());
			res.put("reference", item.getReference());
			res.put("versionNumber", item.getVersionNumber());
			res.put("criticality",
					internationalizationHelper.getMessage(item.getCriticality().getI18nKey(), null, locale));
			res.put("category", internationalizationHelper.getMessage(item.getCategory().getI18nKey(), null, locale));
			res.put("status", item.getStatus().toString());
			res.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return res;
		}
	}

	private static class TestCaseVerifiedRequirementsDataTableModelHelper extends
			VerifiedRequirementsDataTableModelHelper {

		private TestCaseVerifiedRequirementsDataTableModelHelper(Locale locale,
				InternationalizationHelper internationalizationHelper) {
			super(locale, internationalizationHelper);
		}

		@Override
		public Map<String, Object> buildItemData(VerifiedRequirement item) {
			Map<String, Object> res = super.buildItemData(item);
			res.put("verifyingSteps", getVerifyingSteps(item));
			return res;
		}

		private String getVerifyingSteps(VerifiedRequirement item) {
			String result = "";
			Set<ActionTestStep> steps = item.getVerifyingSteps();
			if (!steps.isEmpty()) {
				if (steps.size() == 1) {
					ActionTestStep step = steps.iterator().next();
					result = "<span class='verifyingStep' dataId='" + step.getId() + "'>" + step.getIndex() + 1
							+ "</span>";
				} else {
					result = "&#42;";
				}
			}
			return result;
		}
	}

	private static final class TestStepVerifiedRequirementsDataTableModelHelper extends
			VerifiedRequirementsDataTableModelHelper {
		private long stepId;

		private TestStepVerifiedRequirementsDataTableModelHelper(Locale locale,
				InternationalizationHelper internationalizationHelper, long stepId) {
			super(locale, internationalizationHelper);
			this.stepId = stepId;
		}

		@Override
		public Map<String, Object> buildItemData(VerifiedRequirement item) {
			Map<String, Object> res = super.buildItemData(item);
			res.put("verifiedByStep", item.hasStepAsVerifying(stepId));
			res.put("empty-link-checkbox", "");
			return res;
		}

	}

	private DatatableMapper<String> verifiedRequirementVersionsMapper = new NameBasedMapper(7)
			.mapAttribute(RequirementVersion.class, "id", String.class, "entity-id")
			.mapAttribute(RequirementVersion.class, "name", String.class, "name")
			.mapAttribute(Project.class, "name", String.class, "project")
			.mapAttribute(RequirementVersion.class, "reference", String.class, "reference")
			.mapAttribute(RequirementVersion.class, "versionNumber", String.class, "versionNumber")
			.mapAttribute(RequirementVersion.class, "criticality", String.class, "criticality")
			.mapAttribute(RequirementVersion.class, "category", String.class, "category");

}
