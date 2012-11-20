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
package org.squashtest.csp.tm.web.internal.controller.execution;

import static org.squashtest.csp.tm.web.internal.helper.JEditablePostParams.VALUE;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.service.ExecutionProcessingService;
import org.squashtest.csp.tm.web.internal.model.jquery.JsonSimpleData;

@Controller
@RequestMapping("/execute/{executionId}")
public class ExecutionProcessingController {

	private static final String STEP_INFORMATION_FRAGMENT = "fragment/executions/step-information-fragment";
	
	private static final String IE0_STEP_VIEW = "page/executions/ieo-execute-execution";
	private static final String STEP_PAGE_VIEW = "page/executions/execute-execution";
	private static final String STEP_PAGE_PREVIEW = "execute-execution-preview.html";

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionProcessingController.class);

	@Inject
	private ExecutionRunnerControllerHelper helper;

	private ExecutionProcessingService executionProcService;

	@ServiceReference
	public void setExecutionProcService(ExecutionProcessingService executionProcService) {
		this.executionProcService = executionProcService;
	}

	private void addCurrentStepUrl(long executionId, Model model) {
		model.addAttribute("currentStepUrl", "/execute/" + executionId + "/step/");
	}
	
	
	private String getRedirectToPrologue(long executionId, boolean optimized, boolean suitemode){
		return "/execute/"+executionId+"/step/prologue?optimized="+optimized+"&suitemode="+suitemode;
	}
	
	private String getRedirectToStep(long executionId, int stepIndex, boolean optimized, boolean suitemode){
		return "/execute/"+executionId+"/step/"+stepIndex+"?optimized="+optimized+"&suitemode="+suitemode;
	}
	
	
	
	//************************** getters for the main execution fragments **************************************
	
	
	@RequestMapping(method = RequestMethod.GET, params={"optimized", "suitemode"})
	public String executeFirstRunnableStep(@PathVariable("executionId") long executionId,
										   @RequestParam("optimized") boolean optimized,
										   @RequestParam("suitemode") boolean suitemode,
										   Model model){
		
		if (executionProcService.wasNeverRan(executionId)){
			return "redirect:"+getRedirectToPrologue(executionId, optimized, suitemode);
		}
		else{
			int stepIndex = executionProcService.findRunnableExecutionStep(executionId).getExecutionStepOrder();
			return "redirect:"+getRedirectToStep(executionId, stepIndex, optimized, suitemode);
		}
		
		
	}


	@RequestMapping(value = "/step/prologue", method = RequestMethod.GET, params={"optimized", "suitemode"})
	public String getExecutionPrologue(@PathVariable("executionId") long executionId, 
									   @RequestParam("optimized") boolean optimized, 
									   @RequestParam("suitemode") boolean suitemode,
									   Model model){
		
		addCurrentStepUrl(executionId, model);
		helper.popuplateExecutionPreview(executionId, optimized, suitemode, model);
		
		return STEP_PAGE_PREVIEW;	
		
	}
	

	@RequestMapping(value = "/step/{stepIndex}", method = RequestMethod.GET, params={"optimized=false", "suitemode=false"})
	public String getClassicSingleExecutionStepFragment(@PathVariable long executionId, 
												  @PathVariable int stepIndex, 										  
												  Model model) {
		
		helper.populateFirstRunnableStepModel(executionId, stepIndex, model);
		helper.populateClassicSingleModel(model);
		
		return STEP_PAGE_VIEW;

	}
	
	@RequestMapping(value = "/step/{stepIndex}", method = RequestMethod.GET, params={"optimized=false", "suitemode=true"})
	public String getClassicTestSuiteExecutionStepFragment(@PathVariable long executionId, 
												  @PathVariable int stepIndex, 										  
												  Model model) {
		
		helper.populateFirstRunnableStepModel(executionId, stepIndex, model);
		helper.populateClassicTestSuiteModel(executionId, model);
		
		return STEP_PAGE_VIEW;

	}

	@RequestMapping(value = "/step/{stepIndex}", method = RequestMethod.GET, params = {"optimized=true", "suitemode=false"})
	public String getOptimizedSingleExecutionStepFragment(@PathVariable long executionId, 
													@PathVariable int stepIndex,
													Model model) {
		
		helper.populateFirstRunnableStepModel(executionId, stepIndex, model);
		helper.populateOptimizedSingleModel(model);	
		
		return IE0_STEP_VIEW;

	}

	@RequestMapping(value = "/step/{stepIndex}", method = RequestMethod.GET, params = {"optimized=true", "suitemode=true"})
	public String getOptimizedTestSuiteExecutionStepFragment(@PathVariable long executionId, 
													@PathVariable int stepIndex,
													Model model) {
		
		helper.populateFirstRunnableStepModel(executionId, stepIndex, model);
		helper.populateOptimizedTestSuiteModel(executionId, model);
		
		return IE0_STEP_VIEW;

	}
	
	
	
	// ************************* other stuffs ********************************************
	
	

	@RequestMapping(value = "/step/{stepIndex}/general", method = RequestMethod.GET)
	public ModelAndView getMenuInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex) {
		
		ExecutionStep executionStep = executionProcService.findStepAt(executionId, stepIndex);

		ModelAndView mav = new ModelAndView(STEP_INFORMATION_FRAGMENT);

		mav.addObject("auditableEntity", executionStep);
		mav.addObject("withoutCreationInfo", true);

		return mav;

	}

	@RequestMapping(value = "/step/{stepId}", method = RequestMethod.POST, params = { "id=execution-comment", VALUE })
	@ResponseBody
	public String updateComment(@RequestParam(VALUE) String newComment, @PathVariable("stepId") Long stepId) {
		executionProcService.setExecutionStepComment(stepId, newComment);
		LOGGER.trace("ExecutionStep " + stepId.toString() + ": updated comment to " + newComment);
		return newComment;
	}

	@RequestMapping(value = "/step/{stepIndex}/new-step-infos", method = RequestMethod.GET)
	@ResponseBody
	public String getNewStepInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex) {

		JsonSimpleData obj = new JsonSimpleData();

		Execution execution = executionProcService.findExecution(executionId);
		Integer total = execution.getSteps().size();
		ExecutionStep executionStep = executionProcService.findStepAt(executionId, stepIndex);

		if (executionStep == null) {
			executionStep = executionProcService.findStepAt(executionId, total - 1);
		}

		obj.addAttr("executionStepOrder", executionStep.getExecutionStepOrder().toString());
		obj.addAttr("executionStepId", executionStep.getId().toString());

		return obj.toString();
	}

	@RequestMapping(value = "/step/{stepId}", method = RequestMethod.POST, params = "executionStatus")
	@ResponseBody
	public void updateExecutionMode(@RequestParam String executionStatus, @PathVariable("stepId") long stepId) {
		ExecutionStatus status = ExecutionStatus.valueOf(executionStatus);
		executionProcService.setExecutionStepStatus(stepId, status);
	}

}
