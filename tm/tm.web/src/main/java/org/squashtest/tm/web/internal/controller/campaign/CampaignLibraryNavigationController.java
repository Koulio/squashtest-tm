/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.campaign;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.campaign.export.CampaignExportCSVModel;
import org.squashtest.tm.domain.campaign.export.CampaignExportCSVModel.Row;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.exception.library.RightsUnsuficientsForOperationException;
import org.squashtest.tm.service.campaign.CampaignFinder;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.campaign.CampaignFormModel.CampaignFormModelValidator;
import org.squashtest.tm.web.internal.controller.campaign.IterationFormModel.IterationFormModelValidator;
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.model.builder.CampaignLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.IterationNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.builder.TestSuiteNodeBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import com.google.common.base.Optional;

/**
 * Controller which processes requests related to navigation in a {@link CampaignLibrary}.
 *
 * @author Gregory Fouquet
 *
 */
@Controller
@RequestMapping(value = "/campaign-browser")
public class CampaignLibraryNavigationController extends
LibraryNavigationController<CampaignLibrary, CampaignFolder, CampaignLibraryNode> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignLibraryNavigationController.class);

	private static final String REMOVE_FROM_ITER = "remove_from_iter";

	/**
	 * This PermissionEvaluationService should only be used when batch-creating iteration tree nodes from the same campaign,
	 * ie nodes which which have the same permissions
	 * <p/>
	 * It will only query the real PES once per permission  / role.
	 * <p/>
	 * Otherwise, it would create many short lived DB transaction in a single web request, which has measurable effects on performance.
	 * <p/>
	 * Objects of this class should be discarded immediately after creating the iterations.
	 *
	 * @author Gregory Fouquet
	 * @since 1.11.6
	 * @since 1.12.1
	 */
	private class ShortCutPermissionEvaluator implements PermissionEvaluationService {
		private Boolean hasRole;
		private Map<String, Boolean> perms = new HashMap<>();
		private Map<String[], Map<String,Boolean>> hasRolePerms = new HashMap<String[], Map<String, Boolean>>();

		@Override
		public boolean hasRoleOrPermissionOnObject(String role, String permission, Object object) {
			return this.hasRole(role) || this.hasPermissionOnObject(permission, object);
		}

		@Override
		public boolean hasPermissionOnObject(String permission, Object entity) {
			Boolean res = perms.get(permission);
			if (res == null) {
				res = permissionEvaluator.hasPermissionOnObject(permission, entity);
				perms.put(permission, res);

			}

			return res;
		}

		@Override
		public boolean hasRoleOrPermissionOnObject(String role, String permission, Long entityId, String entityClassName) {
			return permissionEvaluator.hasRoleOrPermissionOnObject(role, permission, entityId, entityClassName);
		}

		@Override
		public boolean canRead(Object object) {
			return permissionEvaluator.canRead(object);
		}

		@Override
		public boolean hasMoreThanRead(Object object) {
			return permissionEvaluator.hasMoreThanRead(object);
		}

		@Override
		public boolean hasRole(String role) {
			if (hasRole == null) {
				hasRole = permissionEvaluator.hasRole(role);
			}
			return hasRole;
		}

		@Override
		public boolean hasPermissionOnObject(String permission, Long entityId, String entityClassName) {
			return permissionEvaluator.hasPermissionOnObject(permission, entityId, entityClassName);
		}

		@Override
		public Map<String, Boolean> hasRoleOrPermissionsOnObject(String role, String[] permissions, Object entity) {
			Map<String, Boolean> res = hasRolePerms.get(permissions);
			if (res == null) {
				res = permissionEvaluator.hasRoleOrPermissionsOnObject(role, permissions, entity);
				hasRolePerms.put(permissions, res);
			}
			return res;
		}

		@Override
		public Collection<String> permissionsOn(@NotNull String className, long id) {
			return permissionEvaluator.permissionsOn(className, id);
		}

	}

	;

	@Inject
	@Named("campaign.driveNodeBuilder")
	private Provider<DriveNodeBuilder<CampaignLibraryNode>> driveNodeBuilder;

	@Inject
	private Provider<IterationNodeBuilder> iterationNodeBuilder;
	@Inject
	private Provider<CampaignLibraryTreeNodeBuilder> campaignLibraryTreeNodeBuilder;
	@Inject
	private Provider<TestSuiteNodeBuilder> suiteNodeBuilder;
	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;
	@Inject
	private CampaignFinder campaignFinder;
	@Inject
	private ExecutionFinder executionFinder;
	@Inject
	private IterationModificationService iterationModificationService;
	@Inject
	private PermissionEvaluationService permissionEvaluator;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@RequestMapping(value = "/drives/{libraryId}/content/new-campaign", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewCampaignToLibraryRootContent(@PathVariable Long libraryId,
			@RequestBody CampaignFormModel campaignForm)
					throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(campaignForm, "add-campaign");
		CampaignFormModelValidator validator = new CampaignFormModelValidator(getMessageSource());
		validator.validate(campaignForm, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		Campaign newCampaign = campaignForm.getCampaign();
		Map<Long, RawValue> customFieldValues = campaignForm.getCufs();

		campaignLibraryNavigationService.addCampaignToCampaignLibrary(libraryId, newCampaign, customFieldValues);

		return createTreeNodeFromLibraryNode(newCampaign);

	}

	@RequestMapping(value = "/folders/{folderId}/content/new-campaign", method = RequestMethod.POST)
	public @ResponseBody
 JsTreeNode addNewCampaignToFolderContent(@PathVariable long folderId,
			@RequestBody CampaignFormModel campaignForm)
					throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(campaignForm, "add-campaign");
		CampaignFormModelValidator validator = new CampaignFormModelValidator(getMessageSource());
		validator.validate(campaignForm, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		Campaign newCampaign = campaignForm.getCampaign();
		Map<Long, RawValue> customFieldValues = campaignForm.getCufs();


		campaignLibraryNavigationService.addCampaignToCampaignFolder(folderId, newCampaign, customFieldValues);

		return createTreeNodeFromLibraryNode(newCampaign);

	}

	@Override
	protected LibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode> getLibraryNavigationService() {
		return campaignLibraryNavigationService;
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(CampaignLibraryNode model) {
		CampaignLibraryTreeNodeBuilder builder = campaignLibraryTreeNodeBuilder.get();

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		if (activeMilestone.isPresent()) {
			builder.filterByMilestone(activeMilestone.get());
		}

		return builder.setNode(model).build();
	}

	@RequestMapping(value = "/campaigns/{campaignId}/content/new-iteration", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewIterationToCampaign(@PathVariable long campaignId, @RequestBody IterationFormModel iterationForm)
			throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(iterationForm, "add-iteration");
		IterationFormModelValidator validator = new IterationFormModelValidator(getMessageSource());
		validator.validate(iterationForm, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		Iteration newIteration = iterationForm.getIteration();
		Map<Long, RawValue> customFieldValues = iterationForm.getCufs();
		boolean copyTestPlan = iterationForm.isCopyTestPlan();

		int newIterationIndex = campaignLibraryNavigationService.addIterationToCampaign(newIteration, campaignId,
				copyTestPlan, customFieldValues);

		return createIterationTreeNode(newIteration, newIterationIndex);
	}

	private JsTreeNode createIterationTreeNode(Iteration iteration, int iterationIndex) {
		return iterationNodeBuilder.get().setModel(iteration).setIndex(iterationIndex).build();
	}

	private JsTreeNode createBatchedIterationTreeNode(Iteration iteration, int iterationIndex, PermissionEvaluationService permissionEvaluationService) {
		return new IterationNodeBuilder(permissionEvaluationService).setModel(iteration).setIndex(iterationIndex).build();
	}

	private JsTreeNode createTestSuiteTreeNode(TestSuite testSuite) {
		return suiteNodeBuilder.get().setModel(testSuite).build();
	}

	@RequestMapping(value = "/campaigns/{campaignId}/content", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> getCampaignIterationsTreeModel(@PathVariable long campaignId) {
		List<Iteration> iterations = campaignLibraryNavigationService.findIterationsByCampaignId(campaignId);
		return createCampaignIterationsModel(iterations);
	}

	@RequestMapping(value = "/iterations/{resourceId}/content", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> getIterationTestSuitesTreeModel(@PathVariable("resourceId") long iterationId) {

		List<TestSuite> testSuites = campaignLibraryNavigationService.findIterationContent(iterationId);

		return createIterationTestSuitesModel(testSuites);
	}


	/*
	 * Special implementation of moveNodes(...) when the destination type is "campaigns"
	 * (non-Javadoc)
	 * @see org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController#moveNodes(java.lang.Long[], long, java.lang.String)
	 */
	@RequestMapping(value = "/campaigns/{destinationId}/content/{nodeIds}", method = RequestMethod.PUT)
	public @ResponseBody void moveNodes(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
			@PathVariable("destinationId") long destinationId) {

		/*
		 * Evolution 5169.
		 *
		 * One can move iterations within a same campaign. But it makes sense only if an index is supplied too.
		 * So, this version of moveNodes - that uses no index - is of no interest for us : we just do nothing.
		 *
		 * For other destination types though we can proceed with the super implementation.
		 */


	}

	@RequestMapping(value = "/campaigns/{destinationId}/content/{nodeIds}/{position}", method = RequestMethod.PUT)
	public @ResponseBody void moveNodes(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
			@PathVariable("destinationId") long destinationId,
			@PathVariable("position") int position) {

		try {
			campaignLibraryNavigationService.moveIterationsWithinCampaign(destinationId, nodeIds, position);
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}

	}


	private @ResponseBody
	List<JsTreeNode> createCampaignIterationsModel(List<Iteration> iterations) {
		List<JsTreeNode> res = new ArrayList<>();

		PermissionEvaluationService pev = new ShortCutPermissionEvaluator();

		for (int i = 0; i < iterations.size(); i++) {
			Iteration iteration = iterations.get(i);
			res.add(createBatchedIterationTreeNode(iteration, i, pev));
		}

		return res;
	}

	private List<JsTreeNode> createIterationTestSuitesModel(List<TestSuite> suites) {
		TestSuiteNodeBuilder nodeBuilder = suiteNodeBuilder.get();
		JsTreeNodeListBuilder<TestSuite> listBuilder = new JsTreeNodeListBuilder<>(nodeBuilder);

		return listBuilder.setModel(suites).build();

	}

	private @ResponseBody
	List<JsTreeNode> createCopiedIterationsModel(List<Iteration> newIterations, int nextIterationNumber) {
		int iterationIndex = nextIterationNumber;
		List<JsTreeNode> res = new ArrayList<>();

		PermissionEvaluationService pev = new ShortCutPermissionEvaluator();

		for (Iteration iteration : newIterations) {
			res.add(createBatchedIterationTreeNode(iteration, iterationIndex, pev));
			iterationIndex++;
		}

		return res;
	}

	private @ResponseBody
	List<JsTreeNode> createCopiedTestSuitesModel(List<TestSuite> newTestSuites) {

		List<JsTreeNode> res = new ArrayList<>();

		for (TestSuite testSuite : newTestSuites) {
			res.add(createTestSuiteTreeNode(testSuite));
		}

		return res;
	}

	@RequestMapping(value = "/drives", method = RequestMethod.GET, params = { "linkables" })
	public @ResponseBody
	List<JsTreeNode> getLinkablesRootModel() {
		List<CampaignLibrary> linkableLibraries = campaignLibraryNavigationService.findLinkableCampaignLibraries();
		return createLinkableLibrariesModel(linkableLibraries);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<CampaignLibrary> linkableLibraries) {
		JsTreeNodeListBuilder<CampaignLibrary> listBuilder = new JsTreeNodeListBuilder<>(
				driveNodeBuilder.get());

		return listBuilder.setModel(linkableLibraries).build();
	}

	@RequestMapping(value = "/iterations/{iterationIds}/deletion-simulation", method = RequestMethod.GET)
	public @ResponseBody
	Messages simulateIterationDeletion(@PathVariable("iterationIds") List<Long> iterationIds, Locale locale) {

		List<SuppressionPreviewReport> reportList = campaignLibraryNavigationService
				.simulateIterationDeletion(iterationIds);

		Messages messages = new Messages();
		for (SuppressionPreviewReport report : reportList) {
			messages.addMessage(report.toString(getMessageSource(), locale));
		}

		return messages;
	}

	@RequestMapping(value = "/iterations/{iterationIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	OperationReport confirmIterationsDeletion(@PathVariable("iterationIds") List<Long> iterationIds) {

		return campaignLibraryNavigationService.deleteIterations(iterationIds);
	}

	@RequestMapping(value = "/test-suites/{suiteIds}/deletion-simulation", method = RequestMethod.GET)
	public @ResponseBody
	Messages simulateSuiteDeletion(@PathVariable("suiteIds") List<Long> suiteIds, Locale locale) {
		List<SuppressionPreviewReport> reportList = campaignLibraryNavigationService.simulateSuiteDeletion(suiteIds);

		Messages messages = new Messages();
		for (SuppressionPreviewReport report : reportList) {
			messages.addMessage(report.toString(getMessageSource(), locale));
		}

		return messages;

	}

	@RequestMapping(value = "/test-suites/{suiteIds}", params = { REMOVE_FROM_ITER }, method = RequestMethod.DELETE)
	public @ResponseBody
 OperationReport confirmSuitesDeletion(@PathVariable("suiteIds") List<Long> suiteIds,
			@RequestParam(REMOVE_FROM_ITER) boolean removeFromIter) {

		return campaignLibraryNavigationService.deleteSuites(suiteIds, removeFromIter);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/iterations/new", method = RequestMethod.POST, params = {
			"nodeIds[]", "next-iteration-index" })
	public @ResponseBody
	List<JsTreeNode> copyIterations(@RequestParam("nodeIds[]") Long[] nodeIds,
			@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId,
			@RequestParam("next-iteration-index") int nextIterationIndex) {

		List<Iteration> iterationsList;
		iterationsList = campaignLibraryNavigationService.copyIterationsToCampaign(campaignId, nodeIds);
		return createCopiedIterationsModel(iterationsList, nextIterationIndex);
	}

	@RequestMapping(value = "/iterations/{iterationId}/test-suites/new", method = RequestMethod.POST, params = { "nodeIds[]" })
	public @ResponseBody
	List<JsTreeNode> copyTestSuites(@RequestParam("nodeIds[]") Long[] nodeIds,
			@PathVariable("iterationId") long iterationId) {

		List<TestSuite> testSuiteList;
		testSuiteList = iterationModificationService.copyPasteTestSuitesToIteration(nodeIds, iterationId);
		return createCopiedTestSuitesModel(testSuiteList);

	}

	@RequestMapping(value = "/export-campaign/{campaignId}", method = RequestMethod.GET, params = "export=csv")
	public @ResponseBody
	FileSystemResource exportCampaign(@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId,
			@RequestParam(value = "exportType", defaultValue = "S") String exportType, HttpServletResponse response) {

		Campaign campaign = campaignFinder.findById(campaignId);
		CampaignExportCSVModel model = campaignLibraryNavigationService.exportCampaignToCSV(campaignId, exportType);

		// prepare the response
		response.setContentType("application/octet-stream");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

		response.setHeader("Content-Disposition", "attachment; filename=" + "EXPORT_CPG_" + exportType + "_"
				+ campaign.getName().replace(" ", "_") + "_" + sdf.format(new Date()) + ".csv");

		File exported = exportToFile(model);
		return new FileSystemResource(exported);
	}

	// Export Campaign from Execution
	@RequestMapping(value = "/export-campaign-by-execution/{executionId}", method = RequestMethod.GET, params = "export=csv")
	public @ResponseBody
	FileSystemResource exportCampaignByExecution(@PathVariable(RequestParams.EXECUTION_ID) long executionId,
			@RequestParam(value = "exportType", defaultValue = "S") String exportType, HttpServletResponse response) {

		Execution execution = executionFinder.findById(executionId);
		Campaign campaign = campaignFinder.findById(execution.getCampaign().getId());

		CampaignExportCSVModel model = campaignLibraryNavigationService.exportCampaignToCSV(execution.getCampaign().getId(), exportType);

		// prepare the response
		response.setContentType("application/octet-stream");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

		response.setHeader("Content-Disposition", "attachment; filename=" + "EXPORT_CPG_" + exportType + "_"
				+ campaign.getName().replace(" ", "_") + "_" + sdf.format(new Date()) + ".csv");

		File exported = exportToFile(model);
		return new FileSystemResource(exported);
	}

	// Milestone dashboard

	@RequestMapping(value = "/dashboard-milestones-statistics", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public @ResponseBody
 CampaignStatisticsBundle getStatisticsAsJson() {

		return campaignLibraryNavigationService.gatherCampaignStatisticsBundleByMilestone();
	}

	@RequestMapping(value = "/dashboard-milestones", method = RequestMethod.GET, produces = ContentTypes.TEXT_HTML)
	public ModelAndView getDashboard(Model model) {

		ModelAndView mav = new ModelAndView("fragment/campaigns/campaign-milestone-dashboard");

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		CampaignStatisticsBundle csbundle = campaignLibraryNavigationService
				.gatherCampaignStatisticsBundleByMilestone();

		mav.addObject("milestone", activeMilestone.get());
		mav.addObject("dashboardModel", csbundle);

		boolean allowsSettled = (csbundle.getCampaignTestCaseStatusStatistics().getNbSettled() > 0);
		boolean allowsUntestable = (csbundle.getCampaignTestCaseStatusStatistics().getNbUntestable() > 0);

		mav.addObject("allowsSettled", allowsSettled);
		mav.addObject("allowsUntestable", allowsUntestable);

		return mav;
	}

	@RequestMapping(value = "/dashboard-milestones", method = RequestMethod.GET, produces = ContentTypes.TEXT_HTML, params="printmode")
	public ModelAndView getDashboard(Model model,
			@RequestParam(value = "printmode", defaultValue = "false") Boolean printmode) {

		ModelAndView mav = getDashboard(model);
		mav.setViewName("page/campaign-workspace/show-campaign-milestone-dashboard");
		mav.addObject("printmode", printmode);

		return mav;
	}

	private File exportToFile(CampaignExportCSVModel model) {

		File file;
		PrintWriter writer = null;
		try {
			file = File.createTempFile("export-requirement", "tmp");
			file.deleteOnExit();

			writer = new PrintWriter(file);

			// print header
			Row header = model.getHeader();
			writer.write(header.toString() + "\n");

			// print the rest
			Iterator<Row> iterator = model.dataIterator();
			while (iterator.hasNext()) {
				Row datarow = iterator.next();
				String cleanRowValue = HTMLCleanupUtils.htmlToText(datarow.toString()).replaceAll("\\n", "")
						.replaceAll("\\r", "");
				writer.write(cleanRowValue + "\n");
			}

			writer.close();

			return file;
		} catch (IOException e) {
			LOGGER.error("campaign export : I/O failure while creating the temporary file : " + e.getMessage());
			throw new RuntimeException(e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

	}

}
