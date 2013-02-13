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
package org.squashtest.tm.web.internal.controller.execution;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.campaign.TestSuiteExecutionProcessingService;
import org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueFinder;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
/**
 * Helper class for Controllers which need to show classic and optimized execution runners.
 * 
 * @author Gregory Fouquet
 * 
 */
@Component
public class ExecutionRunnerControllerHelper {
	
	public static final String TEST_PLAN_ITEM_URL_PATTERN = "/test-suites/{0,number,####}/test-plan/{1,number,####}";
	public static final String NEXT_EXECUTION_URL = "/test-suites/{0,number,####}/test-plan/{1,number,####}/next-execution/runner";
	public static final String CURRENT_STEP_URL_PATTERN = "/execute/{0,number,####}/step/";
	
	
	public static final String COMPLETION_POPUP_TITLE = "popup.title.info";
	public static final String COMPLETED_SUITE_MESSAGE = "squashtm.action.exception.testsuite.end";
	public static final String COMPLETED_STEP_MESSAGE = "execute.alert.test.complete";
	
	
	private interface FetchStepCommand {
		ExecutionStep firstFirstRunnable(long executionId);
		ExecutionStep findStepAtIndex(long executionId, int stepIndex);
	}
	
	private FetchStepCommand FETCHER = new FetchStepCommand() {
		
		@Override
		public ExecutionStep firstFirstRunnable(long executionId) {
			return executionProcessingService.findRunnableExecutionStep(executionId);
		}
		
		@Override
		public ExecutionStep findStepAtIndex(long executionId, int stepIndex) {
			
			int stepCount = executionProcessingService.findTotalNumberSteps(executionId);
			
			if (stepIndex >= stepCount) {
				return executionProcessingService.findStepAt(executionId, stepCount - 1);
			}

			ExecutionStep executionStep = executionProcessingService.findStepAt(executionId, stepIndex);

			if (executionStep == null) {
				executionStep = executionProcessingService.findStepAt(executionId, stepCount - 1);
			}

			return executionStep;
		}
	};
	

	private ExecutionProcessingService executionProcessingService;
	
	
	private TestSuiteExecutionProcessingService testSuiteExecutionProcessingService;

	@ServiceReference
	public void setExecutionProcessingService(ExecutionProcessingService executionProcService) {
		this.executionProcessingService = executionProcService;
	}
	
	@ServiceReference
	public void setTestSuiteExecutionProcessingService(TestSuiteExecutionProcessingService testSuiteExecutionProcessingService) {
		this.testSuiteExecutionProcessingService = testSuiteExecutionProcessingService;
	}
	
	@Inject
	private MessageSource messageSource;
	
	
	@Inject
	private DenormalizedFieldValueFinder denormalizedFieldValueFinder;
	// ************************** step model population methods *****************************
	
	public void populateStepAtIndexModel(long executionId, int stepIndex, Model model) {
		
		Execution execution = executionProcessingService.findExecution(executionId);
		ExecutionStep executionStep = FETCHER.findStepAtIndex(executionId, stepIndex);

		_populateExecutionStepModel(execution, executionStep, model);
	}
	
	
	/**
	 * @deprecated unused
	 * @param executionId
	 * @param model
	 */
	@Deprecated
	public void populateFirstRunnableStepIndexModel(long executionId, Model model){
		
		Execution execution = executionProcessingService.findExecution(executionId);
		ExecutionStep executionStep = FETCHER.firstFirstRunnable(executionId);
		
		_populateExecutionStepModel(execution, executionStep, model);
	
	}
	
	private void _populateExecutionStepModel(Execution execution, ExecutionStep executionStep, Model model){
		
		int stepOrder = 0;
		int total = execution.getSteps().size();
		
		Set<ExecutionStatus> statusSet = Collections.emptySet();
		List<DenormalizedFieldValue> denormalizedFieldValues = Collections.emptyList();
		if(executionStep != null){
			stepOrder = executionStep.getExecutionStepOrder();
			statusSet = executionStep.getLegalStatusSet();
			denormalizedFieldValues = denormalizedFieldValueFinder.findAllForEntity(executionStep);
		}

		model.addAttribute("execution", execution);
		model.addAttribute("executionStep", executionStep);
		model.addAttribute("denormalizedFieldValues", denormalizedFieldValues);
		model.addAttribute("totalSteps", total );
		model.addAttribute("executionStatus", statusSet );
		model.addAttribute("hasNextStep", stepOrder != (total - 1));	
		
		addCurrentStepUrl(execution.getId(), model);
	}
	
	


	public void populateExecutionPreview(final long executionId, Model model){
		popuplateExecutionPreview(executionId, false, false, model);
	}
	
	
	public void popuplateExecutionPreview(final long executionId, boolean isOptimized, boolean isTestSuiteMode, Model model){
		
		Execution execution = executionProcessingService.findExecution(executionId);
		int totalSteps = executionProcessingService.findTotalNumberSteps(executionId);
		
		RunnerState state = createNewRunnerState(isOptimized, isTestSuiteMode);
		state.setPrologue(true);
		
		model.addAttribute("execution", execution);
		model.addAttribute("config", state);
		model.addAttribute("totalSteps", totalSteps);
		
	}
	
	
	
	// ********************** display-mode-specific methods ****************************

	public void populateClassicTestSuiteModel(final long executionId, Model model) {
	
		Execution execution = executionProcessingService.findExecution(executionId);
		IterationTestPlanItem itpi = execution.getTestPlan();
		TestSuite ts = itpi.getTestSuite();
		
		
		model.addAttribute("optimized", false);
		model.addAttribute("suitemode", true);
		
		addTestPlanItemUrl(ts.getId(), itpi.getId(), model);
		addHasNextTestCase(ts.getId(), itpi.getId(), model);
		addCurrentStepUrl(executionId, model);
		
	}
	
	public void populateClassicSingleModel(Model model) {

		model.addAttribute("optimized", false);
		model.addAttribute("suitemode", false);
	}
	

	public void populateOptimizedTestSuiteModel(long executionId, Model model){
		
		Execution execution = executionProcessingService.findExecution(executionId);
		IterationTestPlanItem itpi = execution.getTestPlan();
		TestSuite ts = itpi.getTestSuite();
		
		
		model.addAttribute("optimized", false);
		model.addAttribute("suitemode", true);
		
		addTestPlanItemUrl(ts.getId(), itpi.getId(), model);
		addHasNextTestCase(ts.getId(), itpi.getId(), model);
		addCurrentStepUrl(executionId, model);
		
	}
	
	public void populateOptimizedSingleModel(Model model) {

		model.addAttribute("optimized", false);
		model.addAttribute("suitemode", false);
	}
	
	
	
	// ******************* IEO context model stuffing methods *******************************
	
	
	public RunnerState initOptimizedSingleContext(long executionId, String contextPath, Locale locale){
		
	
		RunnerState state = createNewRunnerState(true, false);
		
		_stuffWithPopupMessages(state, locale);
		_stuffWithPrologueStatus(executionId, state);
		_stuffWithEntitiesInfos(executionId, state, contextPath);
		
		
		return state;
	}
	
	public RunnerState initOptimizedTestSuiteContext(long testSuiteId, String contextPath, Locale locale){
		
		Execution execution = testSuiteExecutionProcessingService.startResume(testSuiteId);
		
		RunnerState state = createNewRunnerState(true, true);
		
		_stuffWithPopupMessages(state, locale);
		_stuffWithPrologueStatus(execution.getId(), state);
		_stuffWithEntitiesInfos(execution.getId(), state, contextPath);
		_stuffWithSuiteRelatedInfos(execution, state, contextPath);
		
		return state;
	}
	
	public RunnerState createNextOptimizedTestSuiteContext(long testSuiteId, long testPlanItemId, String contextPath, Locale locale){
		
		Execution execution = testSuiteExecutionProcessingService.startResumeNextExecution(testSuiteId, testPlanItemId);
		
		RunnerState state = createNewRunnerState(true, true);
		
		_stuffWithPopupMessages(state, locale);
		_stuffWithPrologueStatus(execution.getId(), state);
		_stuffWithEntitiesInfos(execution.getId(), state, contextPath);
		_stuffWithSuiteRelatedInfos(execution, state, contextPath);
		
		return state;
	}
	
	// ******************* IEO runner state factory methods *************************
	
	
	public RunnerState createNewRunnerState(boolean isOptimized, boolean isTestSuiteMode){
		
		RunnerState state = new RunnerState();
		
		state.setOptimized(isOptimized);
		state.setTestSuiteMode(isTestSuiteMode);
		
		return state;
		
	}
	
	
	private void _stuffWithPrologueStatus(long executionId, RunnerState state){
		if (executionProcessingService.wasNeverRun(executionId)){
			state.setPrologue(true);
		}
		else{
			state.setPrologue(false);
		}
	}

	private void _stuffWithEntitiesInfos(long executionId, RunnerState state, String contextPath){
		
		ExecutionStep step = executionProcessingService.findRunnableExecutionStep(executionId);
		int totalSteps = executionProcessingService.findTotalNumberSteps(executionId);
		
		boolean wasNeverExecuted = executionProcessingService.wasNeverRun(executionId);
		int stepOrder = (wasNeverExecuted) ? 0 : step.getExecutionStepOrder()+1;
		
		String currentStepUrl = contextPath + "/" + MessageFormat.format( CURRENT_STEP_URL_PATTERN, executionId);
		
		state.setBaseStepUrl(currentStepUrl);
		
		state.setCurrentExecutionId(executionId);
		state.setCurrentStepId(step.getId());
		
		state.setFirstStepIndex(0);
		state.setLastStepIndex(totalSteps);
		state.setCurrentStepIndex(stepOrder);	//+1 here : the interface uses 1-based counter
		state.setCurrentStepStatus(step.getExecutionStatus());
		
	}
	
	private void _stuffWithSuiteRelatedInfos(Execution execution, RunnerState state, String contextPath){
		
		IterationTestPlanItem item = execution.getTestPlan();
		TestSuite suite = item.getTestSuite();
		
		boolean hasNextTestCase = testSuiteExecutionProcessingService.hasMoreExecutableItems(suite.getId(),
				item.getId());
		
		String nextExecutionUrl = contextPath + "/" + MessageFormat.format(NEXT_EXECUTION_URL, suite.getId(), item.getId());
		
		state.setLastTestCase(! hasNextTestCase);
		state.setNextTestCaseUrl(nextExecutionUrl);
		
		
	}
	
	private void _stuffWithPopupMessages(RunnerState state, Locale locale){
		String popupTitle = messageSource.getMessage(COMPLETION_POPUP_TITLE, null, locale);
		String completeTestMessage = messageSource.getMessage(COMPLETED_STEP_MESSAGE, null, locale);
		String completeSuiteMessage = messageSource.getMessage(COMPLETED_SUITE_MESSAGE, null, locale);
		
		state.setCompleteTitle(popupTitle);
		state.setCompleteTestMessage(completeTestMessage);
		state.setCompleteSuiteMessage(completeSuiteMessage);
	}
	
	// ************************ private stuff **************************
	
	private void addTestPlanItemUrl(long testSuiteId, long testPlanItemId, Model model) {
		String testPlanItemUrl = MessageFormat.format(TEST_PLAN_ITEM_URL_PATTERN, testSuiteId, testPlanItemId);
		model.addAttribute("testPlanItemUrl", testPlanItemUrl);
	}

	private void addHasNextTestCase(long testSuiteId, long testPlanItemId, Model model) {
		boolean hasNextTestCase = testSuiteExecutionProcessingService.hasMoreExecutableItems(testSuiteId,
				testPlanItemId);
		model.addAttribute("hasNextTestCase", hasNextTestCase);
	}


	private void addCurrentStepUrl(long executionId, Model model) {
		String currentStepUrl = MessageFormat.format( CURRENT_STEP_URL_PATTERN, executionId);
		model.addAttribute("currentStepUrl", currentStepUrl);
	}
	

}
