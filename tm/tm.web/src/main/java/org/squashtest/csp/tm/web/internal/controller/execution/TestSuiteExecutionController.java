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

import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

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
import org.squashtest.csp.tm.domain.TestPlanItemNotExecutableException;
import org.squashtest.csp.tm.domain.TestPlanTerminatedOrNoStepsException;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/test-suites/{testSuiteId}/test-plan")
public class TestSuiteExecutionController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteExecutionController.class);

	private static class RequestMappings {
		public static final String SHOW_STEP_INFO = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}";
		public static final String STEP_PAGE_PREVIEW = "/{testPlanItemId}/executions/{executionId}/steps/index/prologue";
		public static final String SHOW_EXECUTION_RUNNER = "/{testPlanItemId}/executions/{executionId}/runner";
		public static final String INIT_EXECUTION_RUNNER = "/execution/runner";
		public static final String TEST_EXECUTION_BEFORE_INIT = "/execution/test-runner";
		public static final String INIT_NEXT_EXECUTION_RUNNER = "/{testPlanItemId}/next-execution/runner";
		public static final String DELETE_ALL_EXECUTIONS = "/executions";
	}

	private static class ViewNames {
		public static final String RUNNER_VIEW_PATTERN = "/test-suites/{0,number,####}/test-plan/{1,number,####}/executions/{2,number,####}/runner";
		public static final String CLASSIC_RUNNER_VIEW_PATTERN = RUNNER_VIEW_PATTERN + "?classic";
		public static final String OPTIMIZED_RUNNER_VIEW_PATTERN = RUNNER_VIEW_PATTERN + "?optimized";
		public static final String OPTIMIZED_RUNNER_VIEW_PATTERN_WITH_URL = OPTIMIZED_RUNNER_VIEW_PATTERN + "&ieoIFrameUrl={3}";
	}

	public static final String TEST_PLAN_ITEM_URL_PATTERN = "/test-suites/{0,number,####}/test-plan/{1,number,####}";
	public static final String CURRENT_STEP_URL_PATTERN = "/test-suites/{0,number,####}/test-plan/{1,number,####}/executions/{2,number,####}/steps/index/";
	
	
	private static final String OPTIMIZED_RUNNER_MAIN = "page/executions/ieo-main-page";

	
	private TestSuiteExecutionProcessingService testSuiteExecutionProcessingService;
	

	@Inject
	private ExecutionRunnerControllerHelper helper;
	

	@ServiceReference
	public void setTestSuiteExecutionProcessingService(TestSuiteExecutionProcessingService testSuiteExecutionProcessingService) {
		this.testSuiteExecutionProcessingService = testSuiteExecutionProcessingService;
	}


	public TestSuiteExecutionController() {
		super();
	}
	

	
	
	//redirects to something served by ExecutionProcessingController
	private String getRedirectExecURL(long executionId, boolean optimized, boolean suitemode){
		return "/execute/"+executionId+"?optimized="+optimized+"&suitemode="+suitemode;
	}
	
	
	@RequestMapping(value = RequestMappings.TEST_EXECUTION_BEFORE_INIT, method = RequestMethod.POST, params = {"mode=start-resume"})
	public @ResponseBody void testStartResumeExecutionInClassicRunner(@PathVariable long testSuiteId) {
		try{
			testSuiteExecutionProcessingService.startResume(testSuiteId);
		}
		catch(TestPlanItemNotExecutableException e){
			throw new TestPlanTerminatedOrNoStepsException();
		}
	}
	

	
	@RequestMapping(value = RequestMappings.INIT_EXECUTION_RUNNER, params = {"optimized=false", "suitemode=true"})
	public String startResumeExecutionInClassicRunner(@PathVariable long testSuiteId, Model model) {
		
		Execution execution = testSuiteExecutionProcessingService.startResume(testSuiteId);

		return "redirect:"+ getRedirectExecURL(execution.getId(), false, true);
		
	}
	
	
	@RequestMapping(value = RequestMappings.INIT_EXECUTION_RUNNER, params = {"optimized=true", "suitemode=true"})
	public String startResumeExecutionInClassicRunner(@PathVariable long testSuiteId, Model model, HttpServletRequest context, Locale locale) {
		
		RunnerState state = helper.initOptimizedTestSuiteContext(testSuiteId, context.getContextPath(), locale);
		model.addAttribute("config", state);
		
		return OPTIMIZED_RUNNER_MAIN;
		
		
	}
	
	

	//that method will create if necessary the next execution then redirect a view to its runner
	@RequestMapping(value = RequestMappings.INIT_NEXT_EXECUTION_RUNNER, params={"optimized", "suitemode"}, headers = "Accept=text/html")
	public String moveToNextTestCase(@PathVariable("testPlanItemId") long testPlanItemId,
									 @PathVariable("testSuiteId") long testSuiteId, 
									 @RequestParam("optimized") boolean optimized,
									 @RequestParam("suitemode") boolean suitemode){
		
		Execution exec = testSuiteExecutionProcessingService.startResumeNextExecution(testSuiteId, testPlanItemId);
		
		return "redirect:"+ getRedirectExecURL(exec.getId(), optimized, suitemode);
		
	}
		
	
	//that method will create if necessary the next execution then return the RunnerState that corresponds to it
	//note that most of the time it corresponds to an ieo working in test suite mode so we skip 'optimized' and 'suitemode' parameters here
	@RequestMapping(value = RequestMappings.INIT_NEXT_EXECUTION_RUNNER, params={"optimized", "suitemode"}, headers = "Accept=application/json")
	@ResponseBody
	public RunnerState getNextTestCaseRunnerState(@PathVariable("testPlanItemId") long testPlanItemId,
									 @PathVariable("testSuiteId") long testSuiteId, 
									 HttpServletRequest context,
									 Locale locale){
		
		testSuiteExecutionProcessingService.startResumeNextExecution(testSuiteId, testPlanItemId);
		
		RunnerState state = helper.initOptimizedTestSuiteContext(testSuiteId, context.getContextPath(), locale);
		
		return state;
		
	}	

	
	// ************************** private stuffs ****************************
	

	
	
	/*




	@RequestMapping(value = RequestMappings.SHOW_EXECUTION_RUNNER, method = RequestMethod.GET, params = "optimized")
	public String showOptimizedExecutionRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, Model model, @RequestParam(value="ieoIFrameUrl", required=false) String ieoIFrameUrl) {
		if (ieoIFrameUrl==null) {
			ieoIFrameUrl = "";
		}
		populateExecutionRunnerModel(testSuiteId, testPlanItemId, executionId, ieoIFrameUrl, model);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Will show OER for test suite using model :" + model.asMap());
		}

		return "page/executions/ieo-execute-execution";
	}



	
	@RequestMapping(value = RequestMappings.SHOW_EXECUTION_RUNNER, method = RequestMethod.GET, params = "classic")
	public String showClassicExecutionRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, Model model) {
		populateExecutionRunnerModel(testSuiteId, testPlanItemId, executionId, "", model);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Will show Classic exec runner for test suite using model :" + model.asMap());
		}

		return "page/executions/execute-execution";
	}
	
	@RequestMapping(value = RequestMappings.SHOW_EXECUTION_RUNNER, method = RequestMethod.GET, params = "classic")
	public String showClassicExecutionRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, Model model) {

		return showStepPreviewRunner(testSuiteId, testPlanItemId, executionId, model);
	}	
	
	





	@RequestMapping(value = RequestMappings.INIT_NEXT_EXECUTION_RUNNER, method = RequestMethod.POST, params = "optimized")
	public String startResumeNextExecutionInOptimizedRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId, @RequestParam(value="ieoIFrameUrl", required=false) String ieoIFrameUrl) {
		Execution execution = testSuiteExecutionProcessingService.startResumeNextExecution(testSuiteId, testPlanItemId);

		if (ieoIFrameUrl == null){
			ieoIFrameUrl = "";
		}
		
		return "redirect:"
				+ MessageFormat.format(ViewNames.OPTIMIZED_RUNNER_VIEW_PATTERN_WITH_URL, testSuiteId, execution.getTestPlan().getId(),
						execution.getId(), ieoIFrameUrl);
	}

	@RequestMapping(value = RequestMappings.INIT_NEXT_EXECUTION_RUNNER, method = RequestMethod.POST, params = "classic")
	public String startResumeNextExecutionInClassicRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId  ) {
		Execution execution = testSuiteExecutionProcessingService.startResumeNextExecution(testSuiteId, testPlanItemId);
		return "redirect:"
		+ MessageFormat.format(ViewNames.CLASSIC_RUNNER_VIEW_PATTERN, testSuiteId, execution.getTestPlan().getId(),
				execution.getId());
		
	}

	private void populateExecutionRunnerModel(long testSuiteId, long testPlanItemId, long executionId, String ieoIFrameUrl, Model model) {
		helper.populateClassicRunnerModel(executionId, model);

		addTestSuiteTestPlanItemData(testSuiteId, testPlanItemId, model);
		addCurrentStepUrl(model, testSuiteId, testPlanItemId, executionId);
		addCUrrentIFrameUrl(model, ieoIFrameUrl);
	}

	private void addTestSuiteTestPlanItemData(long testSuiteId, long testPlanItemId, Model model) {
		addHasNextTestCase(testSuiteId, testPlanItemId, model);
		addHasPreviousTestCase(testSuiteId, testPlanItemId, model);
		addTestPlanItemUrl(testSuiteId, testPlanItemId, model);
	}
	
	private void addCUrrentIFrameUrl(Model model, String ieoIFrameUrl) {
		model.addAttribute("urlIFrame", ieoIFrameUrl);
	}

	//**** copypasta from now on. rework asap 

	@RequestMapping(value = RequestMappings.SHOW_STEP_INFO, method = RequestMethod.GET)
	public String showStepInClassicRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @PathVariable int stepIndex, Model model) {
		populateExecutionStepModel(testSuiteId, testPlanItemId, executionId, stepIndex, model);

		return "page/executions/execute-execution";

	}
	
	@RequestMapping(value = RequestMappings.STEP_PAGE_PREVIEW, method=RequestMethod.GET)
	public String showStepPreviewRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, Model model){
		
		populateExecutionPreviewModel(testSuiteId, testPlanItemId, executionId,  model);
		
		return "execute-execution-preview.html";
	}


	private void populateExecutionStepModel(long testSuiteId, long testPlanItemId, long executionId, int stepIndex,
			Model model) {
		helper.populateExecutionStepModel(executionId, stepIndex, model);

		addTestSuiteTestPlanItemData(testSuiteId, testPlanItemId, model);
		addCurrentStepUrl(model, testSuiteId, testPlanItemId, executionId);
	}
	
	private void populateExecutionPreviewModel(long testSuiteId, long testPlanItemId, long executionId, Model model){

		helper.populateClassicRunnerModel(executionId, model);
		addTestSuiteTestPlanItemData(testSuiteId, testPlanItemId, model);
		addCurrentStepUrl(model, testSuiteId, testPlanItemId, executionId);
	}
	

	@RequestMapping(value = RequestMappings.SHOW_STEP_INFO, method = RequestMethod.GET, params = "ieo")
	public String showStepInOptimizedRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @PathVariable int stepIndex, Model model) {
		populateExecutionStepModel(testSuiteId, testPlanItemId, executionId, stepIndex, model);

		return "page/executions/ieo-fragment-step-information";

	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}/menu", method = RequestMethod.GET)
	public String getOptimizedRunnerToolboxFragment(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @PathVariable int stepIndex, Model model) {
		populateExecutionStepModel(testSuiteId, testPlanItemId, executionId, stepIndex, model);

		return "fragment/executions/step-information-toolbox";

	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}/general", method = RequestMethod.GET)
	public ModelAndView getMenuInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex) {
		ExecutionStep executionStep = executionProcessingService.findStepAt(executionId, stepIndex);

		ModelAndView mav = new ModelAndView("fragment/executions/step-information-fragment");

		mav.addObject("auditableEntity", executionStep);
		mav.addObject("withoutCreationInfo", true);

		return mav;

	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}/new-step-infos", method = RequestMethod.GET)
	@ResponseBody
	public String getNewStepInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex) {
		JsonSimpleData obj = new JsonSimpleData();

		Execution execution = executionProcessingService.findExecution(executionId);
		Integer total = execution.getSteps().size();
		ExecutionStep executionStep = executionProcessingService.findStepAt(executionId, stepIndex);

		if (executionStep == null) {
			executionStep = executionProcessingService.findStepAt(executionId, total - 1);
		}

		obj.addAttr("executionStepOrder", executionStep.getExecutionStepOrder().toString());
		obj.addAttr("executionStepId", executionStep.getId().toString());

		return obj.toString();
	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepId}", method = RequestMethod.POST, params = "executionStatus")
	@ResponseBody
	public void updateExecutionMode(@RequestParam String executionStatus, @PathVariable long stepId) {
		ExecutionStatus status = ExecutionStatus.valueOf(executionStatus);
		executionProcessingService.setExecutionStepStatus(stepId, status);
	}

	// *** end copypasta 



	@RequestMapping(value = RequestMappings.DELETE_ALL_EXECUTIONS, method = RequestMethod.DELETE )
	public @ResponseBody void deleteAllExecutions(@PathVariable long testSuiteId) {
		 testSuiteExecutionProcessingService.deleteAllExecutions(testSuiteId);
	}
*/
}
