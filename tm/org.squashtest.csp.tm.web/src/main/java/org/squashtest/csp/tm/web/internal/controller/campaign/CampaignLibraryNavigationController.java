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
package org.squashtest.csp.tm.web.internal.controller.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignFolder;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.service.CampaignLibraryNavigationService;
import org.squashtest.csp.tm.service.LibraryNavigationService;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.csp.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.csp.tm.web.internal.model.builder.CampaignLibraryTreeNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.IterationNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.TestSuiteNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

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

	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;
	@Inject
	private Provider<IterationNodeBuilder> iterationNodeBuilder;
	@Inject
	private Provider<CampaignLibraryTreeNodeBuilder> campaignLibraryTreeNodeBuilder;
	@Inject
	private Provider<TestSuiteNodeBuilder> suiteNodeBuilder;


	private CampaignLibraryNavigationService campaignLibraryNavigationService;

	@ServiceReference
	public void setCampaignLibraryNavigationService(CampaignLibraryNavigationService campaignLibraryNavigationService) {
		this.campaignLibraryNavigationService = campaignLibraryNavigationService;
	}



	@RequestMapping(value = "/drives/{libraryId}/content/new-campaign", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewCampaignToLibraryRootContent(@PathVariable Long libraryId, @Valid @ModelAttribute("add-campaign") Campaign newCampaign) {

		campaignLibraryNavigationService.addCampaignToCampaignLibrary(
				libraryId, newCampaign);



		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("CampaignCreationController : creation of a new Campaign, name : " + newCampaign.getName()
					+ ", description : " + newCampaign.getDescription());
		}

		return createTreeNodeFromLibraryNode(newCampaign);

	}


	@RequestMapping(value = "/folders/{folderId}/content/new-campaign", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewCampaignToFolderContent(@PathVariable long folderId,
			@Valid @ModelAttribute("add-campaign") Campaign newCampaign) {

		campaignLibraryNavigationService.addCampaignToCampaignFolder(folderId,
				newCampaign);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("CampaignCreationController : creation of a new Campaign, name : " + newCampaign.getName()
					+ ", description : " + newCampaign.getDescription() + " in folder " + folderId);
		}

		return createTreeNodeFromLibraryNode(newCampaign);

	}
	@Override
	protected LibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode> getLibraryNavigationService() {
		return campaignLibraryNavigationService;
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(CampaignLibraryNode model) {
		return campaignLibraryTreeNodeBuilder.get().setNode(model).build();
	}

	@Override
	protected String getEditFolderViewName() {
		return "fragment/campaigns/edit-folder";
	}

	@Override
	protected String getShowLibraryViewName() {
		return "page/campaign-libraries/show-campaign-library";
	}

	@RequestMapping(value = "/files/{campaignId}/content/new-iteration", method = RequestMethod.POST)
	public @ResponseBody JsTreeNode addNewIterationToCampaign(@Valid @ModelAttribute("add-iteration") Iteration newIteration, @PathVariable long campaignId) {
		
		int newIterationIndex = campaignLibraryNavigationService.addIterationToCampaign(newIteration, campaignId);
		
		
		return createIterationTreeNode(newIteration, newIterationIndex);
	}

	
	// TODO : remove that method ? It looks like it's mapped to nothing
	public @ResponseBody
	@Deprecated
	JsTreeNode addNewIterationToCampaign(long campaignId,
			long iterationId, boolean copy) {
		Iteration newIteration = campaignLibraryNavigationService
				.findIteration(iterationId);
		int newIterationIndex = 0;
		if (copy == false){
			newIterationIndex = campaignLibraryNavigationService
			.addIterationToCampaign(newIteration, campaignId);
		}else{
			newIterationIndex = campaignLibraryNavigationService
			.copyIterationToCampaign(campaignId, iterationId);
		}

		return createIterationTreeNode(newIteration, newIterationIndex);
	}



	private JsTreeNode createIterationTreeNode(Iteration iteration, int iterationIndex) {
		return iterationNodeBuilder.get().setModel(iteration).setIterationIndex(iterationIndex).build();
	}

	@RequestMapping(value = "/files/{campaignId}/content", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> getCampaignIterationsTreeModel(@PathVariable long campaignId) {
		List<Iteration> iterations = campaignLibraryNavigationService.findIterationsByCampaignId(campaignId);
		return createCampaignIterationsModel(iterations);
	}
	
	
	@RequestMapping(value = "/resources/{resourceId}/content", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> getIterationTestSuitesTreeModel(@PathVariable("resourceId") long iterationId){
		
		List<TestSuite> testSuites = campaignLibraryNavigationService.findIterationContent(iterationId);
		
		return createIterationTestSuitesModel(testSuites);
	
	}

	private @ResponseBody
	List<JsTreeNode> createCampaignIterationsModel(List<Iteration> iterations) {
		List<JsTreeNode> res = new ArrayList<JsTreeNode>();

		for (int i = 0; i < iterations.size(); i++) {
			Iteration iteration = iterations.get(i);
			res.add(createIterationTreeNode(iteration, i));
		}

		return res;
	}
	
	
	
	private List<JsTreeNode> createIterationTestSuitesModel(List<TestSuite> suites){
		TestSuiteNodeBuilder nodeBuilder = suiteNodeBuilder.get();
		JsTreeNodeListBuilder<TestSuite> listBuilder = new JsTreeNodeListBuilder<TestSuite>(nodeBuilder);
		
		return listBuilder.setModel(suites).build();

	}
	
	
	

	private @ResponseBody
	List<JsTreeNode> createCopiedIterationsModel(
			List<Iteration> newIterations, int nextIterationNumber) {
		
		List<JsTreeNode> res = new ArrayList<JsTreeNode>();
		
		for (Iteration iteration : newIterations) {
			res.add(createIterationTreeNode(iteration, nextIterationNumber));
			nextIterationNumber ++;
		}

		return res;
	}





	@RequestMapping(value="/campaign-tree", method=RequestMethod.GET)
	public @ResponseBody List<JsTreeNode> getRootModel(){
		List<CampaignLibrary> linkableLibraries = campaignLibraryNavigationService.findLinkableCampaignLibraries();
		List<JsTreeNode> rootModel = createLinkableLibrariesModel(linkableLibraries);
		return rootModel;
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<CampaignLibrary> linkableLibraries) {
		// TODO use JsTreeListrWhatever builder instead
		DriveNodeBuilder builder = driveNodeBuilder.get();
		List<JsTreeNode> linkableLibrariesModel = new ArrayList<JsTreeNode>();

		for (CampaignLibrary library : linkableLibraries) {
			JsTreeNode libraryNode = builder.setModel(library).build();
			linkableLibrariesModel.add(libraryNode);
		}
		return linkableLibrariesModel;
	}
	

	@RequestMapping(value="/delete-iterations/simulate", method = RequestMethod.POST, params = {"nodeIds[]"})
	public @ResponseBody String simulateIterationDeletion(@RequestParam("nodeIds[]") List<Long> nodeIds, Locale locale){
		List<SuppressionPreviewReport> reportList = campaignLibraryNavigationService.simulateIterationDeletion(nodeIds);
		
		StringBuilder builder = new StringBuilder();
		
		for (SuppressionPreviewReport report : reportList){
			builder.append(report.toString(getMessageSource(), locale));
			builder.append("\n");
		}
		
		return builder.toString();
		
	}


	@RequestMapping(value="/delete-iterations/confirm", method=RequestMethod.DELETE, params= {"nodeIds[]"})
	public @ResponseBody List<Long> confirmIterationsDeletion(@RequestParam("nodeIds[]") List<Long> nodeIds){
		
		return campaignLibraryNavigationService.deleteIterations(nodeIds);	
	}
	
	@RequestMapping(value="/copy", method= RequestMethod.POST)
	public @ResponseBody List<JsTreeNode> copyIterations(@RequestParam("object-ids[]") Long[] iterationsIds, 
							  @RequestParam("destination-id") long campaignId, 
							  @RequestParam("destination-type") String destType,
							  @RequestParam("next-iteration-number") int nextIterationNumber){
		List<Iteration> iterationsList;
		
		if (destType.equals("campaign")){
			iterationsList = campaignLibraryNavigationService.copyIterationsToCampaign(campaignId, iterationsIds);
		}
		else{
			throw new IllegalArgumentException("copy nodes : cannot paste iterations to : "+destType);
		}
		return createCopiedIterationsModel(iterationsList, nextIterationNumber);
	}
	
	

}
